/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.sync;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.serialization.ClinicalRecordSerializer;

/**
 * The {@code serialize → partition → dispatch} projection the events consumer
 * ({@code CoreServiceEventListener}) runs to write a saved/voided/purged record into the read store.
 * (It was originally shared with the AOP migration bridge to guarantee parity; the bridge has since
 * been removed and events is the sole sync path — ADR Decision 12.)
 *
 * <p>Per-type behaviour (which records the save touches; whether a purge sweeps a patient's whole
 * chart) lives on the {@link ClinicalRecordSerializer} — the per-type bean the consumer resolves —
 * via {@link ClinicalRecordSerializer#collectTree} and
 * {@link ClinicalRecordSerializer#bulkDeletePatientUuidFor}, so module-contributed types (ADR
 * Decision 13) get the same treatment without touching this class.
 *
 * <p><b>Disposition is per-node, not per-call.</b> Each node in {@code collectTree(root)} is routed
 * by its own {@code voided} flag (voided → delete, per ADR Decision 10; otherwise serialize +
 * index). {@code purge} is the only call-level override — every node is deleted unconditionally
 * because the core row is gone. The consumer passes {@code purge=true} only for {@code
 * PurgeServiceEvent}; every other event passes {@code false} and lets the entity's own state decide.
 *
 * <p><b>Synchronous serialize, asynchronous write.</b> Serialization runs on the caller's thread
 * (inside the originating transaction, so lazy navigations resolve against an open session and the
 * associations the serializer reads are initialized); the embed + index/delete is handed to
 * {@code dispatcher} to run after commit, which serializes the same nodes again so the write holds
 * what the save committed (see {@link #committedDocument}). Per-node failures inside
 * the dispatched task are caught so one poison record cannot skip its siblings.
 */
public final class RecordProjector {

	private static final Log log = LogFactory.getLog(RecordProjector.class);

	private RecordProjector() {
	}

	/**
	 * The document to write after commit. The save event publishes before the save stamps the audit
	 * dates and runs its save handlers, so the event-time serialization holds pre-save content: a new
	 * condition, diagnosis, allergy or patient has no {@code record_date}, and a new record has no
	 * version. The saved node is the same instance and now holds what was committed, so it is
	 * serialized again. If that fails (a lazy association outside its session), the event-time
	 * document is written as it is, without a version stamped onto pre-save content, so the
	 * bootstrap top-up does not skip it and rewrites it from core. Returns {@code null} when the
	 * committed node has nothing to index or was voided since the event.
	 */
	static <T extends BaseOpenmrsData> QueryDocument committedDocument(ClinicalRecordSerializer<T> serializer,
	        T node, QueryDocument eventTime) {
		try {
			if (Boolean.TRUE.equals(node.getVoided())) {
				return null;
			}
			return serializer.serialize(node);
		}
		catch (RuntimeException e) {
			log.debug("Could not serialize the committed " + serializer.getResourceType() + "/" + node.getUuid()
			        + "; writing the event-time document without a version", e);
			return eventTime;
		}
	}

	public static <T extends BaseOpenmrsData> void project(ClinicalRecordSerializer<T> serializer, T root,
	        boolean purge, RecordIndexer indexer, AfterCommitDispatcher dispatcher) {
		List<T> tree = serializer.collectTree(root);
		List<QueryDocument> toIndex = new ArrayList<>(tree.size());
		List<T> indexedNodes = new ArrayList<>(tree.size());
		List<String> toDelete = new ArrayList<>(purge ? tree.size() : 0);
		for (T node : tree) {
			if (purge || node.getVoided()) {
				toDelete.add(node.getUuid());
			} else {
				QueryDocument doc = serializer.serialize(node);
				if (doc != null) {
					toIndex.add(doc);
					indexedNodes.add(node);
				}
			}
		}

		String bulkDeletePatientUuid = purge ? serializer.bulkDeletePatientUuidFor(root) : null;

		if (toIndex.isEmpty() && toDelete.isEmpty() && bulkDeletePatientUuid == null) {
			return;
		}
		String resourceType = serializer.getResourceType();
		dispatcher.dispatchPrepared(() -> {
			// On the committing thread, right after the commit: the documents of the committed state.
			// The pool worker that embeds and writes runs later, when the same instances may already
			// carry a change a later transaction made and rolled back, so it reads no entity.
			List<QueryDocument> committed = new ArrayList<>(toIndex.size());
			List<String> voided = new ArrayList<>();
			List<QueryDocument> replacements = new ArrayList<>();
			for (int i = 0; i < toIndex.size(); i++) {
				T node = indexedNodes.get(i);
				QueryDocument doc = committedDocument(serializer, node, toIndex.get(i));
				if (doc != null) {
					committed.add(doc);
				} else if (Boolean.TRUE.equals(node.getVoided())) {
					// The save voided the node its event carried: its document goes (Decision 10) and
					// whatever the save wrote in its place is indexed, which no event of its own announces.
					voided.add(node.getUuid());
					try {
						for (T replacement : serializer.replacementsOf(node)) {
							QueryDocument replacementDoc = serializer.serialize(replacement);
							if (replacementDoc != null) {
								replacements.add(replacementDoc);
							}
						}
					}
					catch (RuntimeException e) {
						log.warn("Skipping the replacement of " + resourceType + "/" + node.getUuid()
						        + " due to failure", e);
					}
				}
			}
			return () -> {
				// Per-entity failure isolation: a single poison document (e.g., embedder throws on a
				// pathological text) must not skip its sibling members. The dispatcher's outer guard
				// remains as a last-resort catch for anything that escapes here.
				for (QueryDocument doc : committed) {
					try {
						indexer.index(doc);
					}
					catch (RuntimeException e) {
						log.warn("Skipping index for " + resourceType + "/" + doc.getResourceUuid()
						        + " due to failure", e);
					}
				}
				for (String uuid : voided) {
					try {
						indexer.delete(resourceType, uuid);
					}
					catch (RuntimeException e) {
						log.warn("Skipping delete for " + resourceType + "/" + uuid + " due to failure", e);
					}
				}
				for (QueryDocument doc : replacements) {
					try {
						indexer.index(doc);
					}
					catch (RuntimeException e) {
						log.warn("Skipping index for " + resourceType + "/" + doc.getResourceUuid()
						        + " due to failure", e);
					}
				}
				for (String uuid : toDelete) {
					try {
						indexer.delete(resourceType, uuid);
					}
					catch (RuntimeException e) {
						log.warn("Skipping delete for " + resourceType + "/" + uuid + " due to failure", e);
					}
				}
				if (bulkDeletePatientUuid != null) {
					try {
						indexer.bulkDeleteByPatient(bulkDeletePatientUuid);
					}
					catch (RuntimeException e) {
						log.warn("Skipping bulk-delete-by-patient for " + bulkDeletePatientUuid
						        + " due to failure", e);
					}
				}
			};
		});
	}
}
