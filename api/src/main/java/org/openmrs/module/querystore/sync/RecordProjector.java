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
 * (inside the originating transaction, so lazy navigations resolve against an open session); the
 * embed + index/delete is handed to {@code dispatcher} to run after commit. Per-node failures inside
 * the dispatched task are caught so one poison record cannot skip its siblings.
 */
public final class RecordProjector {

	private static final Log log = LogFactory.getLog(RecordProjector.class);

	private RecordProjector() {
	}

	public static <T extends BaseOpenmrsData> void project(ClinicalRecordSerializer<T> serializer, T root,
	        boolean purge, RecordIndexer indexer, AfterCommitDispatcher dispatcher) {
		List<T> tree = serializer.collectTree(root);
		List<QueryDocument> toIndex = new ArrayList<>(tree.size());
		List<String> toDelete = new ArrayList<>(purge ? tree.size() : 0);
		for (T node : tree) {
			if (purge || node.getVoided()) {
				toDelete.add(node.getUuid());
			} else {
				QueryDocument doc = serializer.serialize(node);
				if (doc != null) {
					toIndex.add(doc);
				}
			}
		}

		String bulkDeletePatientUuid = purge ? serializer.bulkDeletePatientUuidFor(root) : null;

		if (toIndex.isEmpty() && toDelete.isEmpty() && bulkDeletePatientUuid == null) {
			return;
		}
		String resourceType = serializer.getResourceType();
		dispatcher.dispatch(() -> {
			// Per-entity failure isolation: a single poison document (e.g., embedder throws on a
			// pathological text) must not skip its sibling members. The dispatcher's outer guard
			// remains as a last-resort catch for anything that escapes here.
			for (QueryDocument doc : toIndex) {
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
		});
	}
}
