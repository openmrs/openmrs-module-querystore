/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.bridge;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.serialization.ObsRecordSerializer;
import org.springframework.aop.AfterReturningAdvice;

/**
 * Migration-bridge advice on {@link org.openmrs.api.ObsService} (ADR Decision 12 "Migration
 * bridge"). For each {@code saveObs / voidObs / unvoidObs / purgeObs} call this projects the obs
 * (and its group members, walked recursively) through the same {@code serialize → embed → index}
 * pipeline the events-first handlers will drive once core publishes obs events.
 *
 * <p><b>Removal marker.</b> Delete this class — and its {@code <advice>} entry in
 * {@code omod/src/main/resources/config.xml} — when the events-first subscriber for obs ships and
 * has been verified at parity. ADR Decision 12 requires the issue ID for that subscriber to be
 * cited here; until the issue is filed at aspect-merge time it stays as {@code TBD}:
 * <pre>Removal trigger: TBD (events-first obs subscriber)</pre>
 *
 * <p><b>Synchronous serialization, asynchronous index.</b> The advice runs inside the originating
 * transaction so {@link Obs#getGroupMembers(boolean)} and other lazy navigations work against an
 * open Hibernate session. The advice serializes here, then registers an after-commit callback that
 * embeds and writes off the request thread (see {@link AfterCommitDispatcher}).
 *
 * <p><b>Per-node voided policy.</b> The advised method (save / void / unvoid) is treated only as a
 * trigger; the read-store effect for each node in the obs tree is decided by the node's voided
 * flag per ADR Decision 10 — voided node → delete, non-voided node → serialize + index. This
 * catches the case where a {@code saveObs} mutates the voided flag (UI "void this obs" flows that
 * set the flag and resave), where a {@code voidObs} cascades to members, and where a
 * {@code saveObs} on a parent group contains a mix of voided and non-voided members.
 * {@code purgeObs} is the only method that bypasses the per-node policy: the obs row is being
 * unconditionally removed from core, so every reachable obs is deleted from the read store
 * regardless of voided flag.
 *
 * <p>Per OpenMRS module conventions, advice instances are constructed by the framework via
 * reflection from a {@code <advice>} entry in {@code config.xml}; Spring dependencies are
 * therefore resolved lazily through {@link Context#getRegisteredComponent}.
 */
public class ObsIndexingAdvice implements AfterReturningAdvice {

	private static final Log log = LogFactory.getLog(ObsIndexingAdvice.class);

	static final Set<String> TRIGGER_METHODS = new HashSet<>(
	        Arrays.asList("saveObs", "voidObs", "unvoidObs", "purgeObs"));

	@Override
	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) {
		String name = method.getName();
		if (!TRIGGER_METHODS.contains(name)) {
			return;
		}

		Obs obs = obsFrom(returnValue, args);
		if (obs == null) {
			return;
		}

		try {
			dispatch(obs, "purgeObs".equals(name));
		}
		catch (RuntimeException e) {
			// Best-effort per ADR Decision 12. Failures during serialization or dispatch must not
			// propagate back to the clinical-thread caller (the obs save already succeeded).
			log.warn("ObsIndexingAdvice failed for " + name + "; swallowing per ADR Decision 12", e);
		}
	}

	private void dispatch(Obs root, boolean purge) {
		ObsRecordSerializer serializer = serializer();
		BridgeIndexer indexer = indexer();
		AfterCommitDispatcher dispatcher = dispatcher();

		List<Obs> tree = collectTree(root);
		List<QueryDocument> toIndex = new ArrayList<>(tree.size());
		List<String> toDelete = new ArrayList<>(purge ? tree.size() : 0);
		for (Obs node : tree) {
			if (purge || node.getVoided()) {
				toDelete.add(node.getUuid());
			} else {
				QueryDocument doc = serializer.serialize(node);
				if (doc != null) {
					toIndex.add(doc);
				}
			}
		}

		if (toIndex.isEmpty() && toDelete.isEmpty()) {
			return;
		}
		String resourceType = serializer.getResourceType();
		dispatcher.dispatch(() -> {
			// Per-entity failure isolation: a single poison document (e.g., embedder throws on a
			// pathological text) must not skip its sibling members. Matches TypeBootstrapper's
			// per-entity skip-on-failure semantics. The dispatcher's outer guard remains as a
			// last-resort catch for anything that escapes here.
			for (QueryDocument doc : toIndex) {
				try {
					indexer.index(doc);
				}
				catch (RuntimeException e) {
					log.warn("Bridge skipping index for obs/" + doc.getResourceUuid()
					        + " due to failure", e);
				}
			}
			for (String uuid : toDelete) {
				try {
					indexer.delete(resourceType, uuid);
				}
				catch (RuntimeException e) {
					log.warn("Bridge skipping delete for obs/" + uuid + " due to failure", e);
				}
			}
		});
	}

	/**
	 * Walks {@code root} and its group members depth-first via {@code getGroupMembers(true)} so
	 * voided members are still visited — the per-node policy in {@link #dispatch} decides delete
	 * vs index for each one.
	 */
	static List<Obs> collectTree(Obs root) {
		List<Obs> out = new ArrayList<>();
		collect(root, out);
		return out;
	}

	private static void collect(Obs node, List<Obs> out) {
		out.add(node);
		Set<Obs> members = node.getGroupMembers(true);
		if (members == null) {
			// Obs.getGroupMembers(true) returns the raw field, which is null on a freshly
			// constructed obs that never had members added.
			return;
		}
		for (Obs child : members) {
			collect(child, out);
		}
	}

	private static Obs obsFrom(Object returnValue, Object[] args) {
		if (returnValue instanceof Obs) {
			return (Obs) returnValue;
		}
		if (args != null && args.length > 0 && args[0] instanceof Obs) {
			return (Obs) args[0];
		}
		return null;
	}

	ObsRecordSerializer serializer() {
		return Context.getRegisteredComponent("querystore.serializer.obs", ObsRecordSerializer.class);
	}

	BridgeIndexer indexer() {
		return Context.getRegisteredComponent("querystore.bridge.indexer", BridgeIndexer.class);
	}

	AfterCommitDispatcher dispatcher() {
		return Context.getRegisteredComponent("querystore.bridge.dispatcher", AfterCommitDispatcher.class);
	}
}
