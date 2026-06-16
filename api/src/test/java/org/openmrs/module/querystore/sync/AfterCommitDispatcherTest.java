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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.DaemonToken;
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

	@Test
	public void wrapped_runnable_routesThroughDaemonContext_whenTokenWired() {
		// Regression guard: before the fix, SyncExecutor pool threads had no UserContext, so
		// the dispatched embedder calls (which read global properties) blew up with
		// "A user context must first be passed to setUserContext()". Setting a daemon token must
		// force the wrapped runnable through runWithDaemonContext.
		AtomicBoolean ranInDaemonPath = new AtomicBoolean();
		AfterCommitDispatcher tokenedDispatcher = new AfterCommitDispatcher(executor) {
			@Override
			void runWithDaemonContext(Runnable task) {
				ranInDaemonPath.set(true);
				task.run();
			}
		};
		tokenedDispatcher.setDaemonToken(new DaemonToken("test-token"));

		AtomicBoolean innerRan = new AtomicBoolean();
		tokenedDispatcher.dispatch(() -> innerRan.set(true));

		assertEquals(1, executor.submitted.size());
		executor.submitted.get(0).run();
		assertTrue("dispatcher routed wrapped runnable through daemon-context hook",
		        ranInDaemonPath.get());
		assertTrue("inner task ran inside the daemon-context wrapper", innerRan.get());
	}

	@Test
	public void runWithDaemonContext_runsTaskInline_whenTokenAbsent() {
		// The legacy test path (no daemon token wired) must continue to invoke the task directly so
		// existing assertions that observe side effects on the pool thread keep working without
		// requiring an OpenMRS Daemon at test time.
		AtomicBoolean ran = new AtomicBoolean();
		dispatcher.runWithDaemonContext(() -> ran.set(true));
		assertTrue("token-less dispatcher must fall back to inline execution", ran.get());
	}

	@Test
	public void runWithDaemonContext_surfacesEmbedderFailure_whenPlatformDaemonSwallowsIt() {
		// Regression guard for the diagnostic chain. The platform's runInDaemonThreadAndWait does
		// NOT surface daemon-thread exceptions (Future.get's ExecutionException is discarded on
		// some platforms; thread.join only routes through UncaughtExceptionHandler on others).
		// Without runWithDaemonContext's per-task capture-rethrow, the embedder's RuntimeException
		// would be invisible to wrap()'s log-and-swallow guard — operators would lose the
		// [querystore-skip] warn-log they rely on to spot poison documents.
		//
		// Inject a DaemonExecutor that mimics the platform's swallow (runs the task, discards any
		// RuntimeException). The production runWithDaemonContext must STILL surface the failure
		// to its caller via the AtomicReference capture-then-rethrow. If a future refactor removes
		// the rethrow, this test breaks.
		dispatcher.setDaemonExecutorForTest((daemonTask, token) -> {
			try {
				daemonTask.run();
			}
			catch (RuntimeException swallowed) {
				// Simulate platform behaviour: exception is lost, caller sees a clean return.
			}
		});
		dispatcher.setDaemonToken(new DaemonToken("test-token"));

		RuntimeException expected = new RuntimeException("simulated embedder failure");
		try {
			dispatcher.runWithDaemonContext(() -> {
				throw expected;
			});
			throw new AssertionError("runWithDaemonContext must rethrow the captured exception");
		}
		catch (RuntimeException actual) {
			assertSame("rethrown exception must be the captured one", expected, actual);
		}
	}

	@Test
	public void dispatch_endToEnd_logsAndSwallowsFailureFromDaemonHop() {
		// End-to-end regression: an embedder throw inside a daemon hop must reach wrap()'s
		// log-and-swallow guard via the dispatcher's normal dispatch path. Pins the chain
		// dispatcher.dispatch → executor.submit(guarded) → guarded → runWithDaemonContext →
		// daemonExecutor (here: simulating platform-swallow) → capture-rethrow → wrap()'s catch.
		dispatcher.setDaemonExecutorForTest((daemonTask, token) -> {
			try {
				daemonTask.run();
			}
			catch (RuntimeException swallowed) {
				// Platform-swallow simulation.
			}
		});
		dispatcher.setDaemonToken(new DaemonToken("test-token"));

		dispatcher.dispatch(() -> {
			throw new RuntimeException("end-to-end embedder failure");
		});
		assertEquals(1, executor.submitted.size());
		// Must not throw — wrap()'s outer catch must absorb the rethrow from runWithDaemonContext.
		executor.submitted.get(0).run();
	}

	private static final class RecordingExecutor extends SyncExecutor {
		final List<Runnable> submitted = new ArrayList<>();

		@Override
		public void submit(Runnable task) {
			submitted.add(task);
		}
	}
}
