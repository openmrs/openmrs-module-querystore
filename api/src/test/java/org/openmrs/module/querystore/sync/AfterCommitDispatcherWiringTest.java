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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.querystore.api.QueryStoreService;
import org.openmrs.module.querystore.embedding.EmbeddingProvider;
import org.openmrs.module.querystore.model.QueryDocument;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * End-to-end test of the real chain: a live {@link SyncExecutor} pool, a real
 * {@link AfterCommitDispatcher} hitting {@link TransactionSynchronizationManager}, and a real
 * {@link RecordIndexer}. The per-class unit tests pin each component in isolation; this test
 * catches wiring regressions between them — in particular the load-bearing claim that a
 * dispatched task does not run before {@code afterCommit} fires and does run after.
 */
public class AfterCommitDispatcherWiringTest {

	private SyncExecutor executor;

	private AfterCommitDispatcher dispatcher;

	private RecordIndexer indexer;

	private RecordingService service;

	@Before
	public void setUp() {
		executor = new SyncExecutor(2);
		executor.start();
		dispatcher = new AfterCommitDispatcher(executor);
		service = new RecordingService();
		indexer = new RecordIndexer(service, new ZeroEmbedder());
	}

	@After
	public void tearDown() {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.clearSynchronization();
		}
		executor.stop();
	}

	@Test
	public void dispatch_insideActiveTx_deferredUntilAfterCommit_thenRunsOnPool() throws Exception {
		TransactionSynchronizationManager.initSynchronization();
		QueryDocument doc = doc("u-1");

		dispatcher.dispatch(() -> indexer.index(doc));

		// Before commit, the task is queued in the sync list, not on the executor pool. Wait a
		// short beat to make sure the executor stays idle (a buggy implementation that submitted
		// eagerly would have indexed by now).
		LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(50));
		assertEquals("task does not run until commit", 0, service.indexed.size());

		// Fire what Spring would fire on a real commit.
		for (TransactionSynchronization s : TransactionSynchronizationManager.getSynchronizations()) {
			s.afterCommit();
		}

		// Now the task is on the pool; drain by stopping the executor (awaits termination).
		executor.stop();
		assertEquals(1, service.indexed.size());
		assertEquals("u-1", service.indexed.get(0).getResourceUuid());
	}

	@Test
	public void dispatch_withoutActiveTx_runsTaskOnPoolImmediately() {
		assertFalse(TransactionSynchronizationManager.isSynchronizationActive());

		dispatcher.dispatch(() -> indexer.index(doc("u-2")));

		executor.stop();
		assertEquals(1, service.indexed.size());
	}

	@Test
	public void dispatch_taskThrows_executorKeepsAcceptingFurtherWork() throws Exception {
		AtomicInteger successes = new AtomicInteger();

		dispatcher.dispatch(() -> {
			throw new RuntimeException("simulated failure");
		});
		dispatcher.dispatch(successes::incrementAndGet);
		dispatcher.dispatch(successes::incrementAndGet);

		// Stop the executor to drain everything queued.
		executor.stop();
		assertTrue("subsequent tasks still ran despite the failed one", successes.get() >= 2);
	}

	private static QueryDocument doc(String uuid) {
		QueryDocument doc = new QueryDocument();
		doc.setResourceType("obs");
		doc.setResourceUuid(uuid);
		doc.setText("anything");
		return doc;
	}

	private static final class RecordingService implements QueryStoreService {
		final List<QueryDocument> indexed = new ArrayList<>();

		@Override
		public synchronized org.openmrs.module.querystore.backend.WriteResult index(QueryDocument document) {
			indexed.add(document);
			return org.openmrs.module.querystore.backend.WriteResult.success();
		}
		@Override public void delete(String resourceType, String resourceUuid) { }
		@Override public void bulkDeleteByPatient(String patientUuid) { }
		@Override public List<QueryDocument> searchByPatient(String p, String q, int l) {
			return Collections.emptyList();
		}
		@Override public List<QueryDocument> search(String q, int l) { return Collections.emptyList(); }
		@Override public List<QueryDocument> getPatientChart(String patientUuid) { return Collections.emptyList(); }
		@Override public org.openmrs.module.querystore.model.ContextSlice getContextSlice(String patientUuid, String question, org.openmrs.module.querystore.model.ContextSliceRequest request) { return new org.openmrs.module.querystore.model.ContextSlice(java.util.Collections.emptyList(), 0, false); }
		@Override public void onStartup() { }
		@Override public void onShutdown() { }
	}

	private static final class ZeroEmbedder implements EmbeddingProvider {
		@Override public int getDimensions() { return 8; }
		@Override public float[] embed(String text) { return new float[8]; }
	}
}
