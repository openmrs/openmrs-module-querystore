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
import java.util.Collections;
import java.util.List;

import org.openmrs.module.querystore.api.QueryStoreService;
import org.openmrs.module.querystore.backend.WriteResult;
import org.openmrs.module.querystore.embedding.EmbeddingProvider;
import org.openmrs.module.querystore.model.QueryDocument;

/**
 * Shared recording fixtures for the sync-pipeline unit tests — the {@link RecordingService},
 * {@link ZeroEmbedder}, and {@link ImmediateDispatcher} shapes consumed by {@code RecordProjectorTest}
 * and the events-consumer tests ({@code org.openmrs.module.querystore.events}). Centralising them
 * keeps each test class small and avoids clipboard-copy drift.
 *
 * <p>Public for cross-package reuse by the events tests. Not unified with the bootstrap tests' own
 * (subtly different) recording-service variants — out of scope.
 */
public final class SyncTestSupport {

	private SyncTestSupport() {
	}

	public static final class RecordingService implements QueryStoreService {
		public final List<QueryDocument> indexed = new ArrayList<>();

		public final List<String[]> deleted = new ArrayList<>();

		public final List<String> bulkDeletedPatients = new ArrayList<>();

		@Override
		public WriteResult index(QueryDocument document) {
			indexed.add(document);
			return WriteResult.success();
		}

		@Override
		public void delete(String resourceType, String resourceUuid) {
			deleted.add(new String[]{resourceType, resourceUuid});
		}

		@Override
		public void bulkDeleteByPatient(String patientUuid) {
			bulkDeletedPatients.add(patientUuid);
		}

		@Override public List<QueryDocument> searchByPatient(String p, String q, int l) {
			return Collections.emptyList();
		}

		@Override public List<QueryDocument> search(String q, int l) { return Collections.emptyList(); }

		@Override public List<QueryDocument> getPatientChart(String patientUuid) { return Collections.emptyList(); }
		@Override public org.openmrs.module.querystore.model.ContextSlice getContextSlice(String patientUuid, String question, org.openmrs.module.querystore.model.ContextSliceRequest request) { return new org.openmrs.module.querystore.model.ContextSlice(java.util.Collections.emptyList(), 0, false); }

		@Override public void onStartup() { }

		@Override public void onShutdown() { }
	}

	public static final class ZeroEmbedder implements EmbeddingProvider {

		@Override public int getDimensions() { return 8; }

		@Override public float[] embed(String text) { return new float[8]; }
	}

	/**
	 * Synchronous dispatcher: runs the task immediately on the calling thread instead of registering
	 * an after-commit callback. Keeps test assertions synchronous and counts dispatches so the
	 * tests can pin "was the advice triggered?" without observing executor threads.
	 */
	public static final class ImmediateDispatcher extends AfterCommitDispatcher {

		public int count;

		public ImmediateDispatcher() {
			super(new SyncExecutor());
		}

		@Override
		public void dispatch(Runnable task) {
			count++;
			task.run();
		}
	}
}
