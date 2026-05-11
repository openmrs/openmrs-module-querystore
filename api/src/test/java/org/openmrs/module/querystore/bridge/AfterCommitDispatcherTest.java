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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class AfterCommitDispatcherTest {

	private AfterCommitDispatcher dispatcher;

	private RecordingExecutor executor;

	@Before
	public void setUp() {
		executor = new RecordingExecutor();
		dispatcher = new AfterCommitDispatcher(executor);
	}

	@After
	public void tearDown() {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.clearSynchronization();
		}
	}

	@Test
	public void dispatch_withActiveSynchronization_defersToAfterCommit() {
		TransactionSynchronizationManager.initSynchronization();
		AtomicBoolean taskRan = new AtomicBoolean();

		dispatcher.dispatch(() -> taskRan.set(true));

		assertFalse("task does not run until commit fires", taskRan.get());
		assertEquals("task not submitted before commit", 0, executor.submitted.size());

		// Drain the registered synchronizations as Spring would on commit.
		List<TransactionSynchronization> syncs = TransactionSynchronizationManager.getSynchronizations();
		for (TransactionSynchronization s : syncs) {
			s.afterCommit();
		}
		assertEquals("task handed to executor after commit", 1, executor.submitted.size());
		executor.submitted.get(0).run();
		assertTrue("task ran when executor drained", taskRan.get());
	}

	@Test
	public void dispatch_withoutSynchronization_submitsImmediately() {
		AtomicBoolean taskRan = new AtomicBoolean();
		dispatcher.dispatch(() -> taskRan.set(true));
		assertEquals("task submitted immediately when no tx is active", 1, executor.submitted.size());
		executor.submitted.get(0).run();
		assertTrue(taskRan.get());
	}

	@Test
	public void dispatch_taskThrowing_isSwallowed() {
		dispatcher.dispatch(() -> {
			throw new RuntimeException("boom");
		});
		assertEquals(1, executor.submitted.size());
		// Must not propagate — the dispatcher wraps tasks in a log-and-swallow guard so an aspect
		// can never throw an indexing failure back into the clinical request thread.
		executor.submitted.get(0).run();
	}

	private static final class RecordingExecutor extends BridgeExecutor {
		final List<Runnable> submitted = new ArrayList<>();

		@Override
		public void submit(Runnable task) {
			submitted.add(task);
		}
	}
}
