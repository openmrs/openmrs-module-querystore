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

import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.DaemonToken;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Hands a projection task to the {@link BridgeExecutor} once the originating transaction has
 * committed, so indexing never runs against uncommitted state and never blocks the clinical
 * request thread (ADR Decision 12 "Migration bridge"). When no transaction is active the task is
 * submitted immediately; at-least-once semantics still hold via the bootstrap reconciliation path.
 *
 * <p>Failures inside the dispatched task are caught and logged here. The aspect's contract per the
 * ADR is "log and swallow"; per-document failures must never bubble back to the clinical thread
 * (which has already returned anyway) and must not poison subsequent dispatches.
 *
 * <p><b>Thread context.</b> {@link BridgeExecutor}'s worker threads do not carry an OpenMRS
 * {@code UserContext}, but the dispatched indexer transitively reads global properties (the
 * configured embedding provider bean, then its model/vocab paths) and so requires one. We resolve
 * that here by handing the actual task to {@link Daemon#runInDaemonThreadAndWait} from inside the
 * pool thread once a {@link DaemonToken} has been wired in. The pool thread blocks until the
 * daemon thread completes, preserving the executor's bounded-concurrency contract; without the
 * token (unit tests that drive the runnable directly) the task is run inline so existing
 * assertions still observe side effects.
 */
public class AfterCommitDispatcher {

	private static final Log log = LogFactory.getLog(AfterCommitDispatcher.class);

	private final BridgeExecutor executor;

	private volatile DaemonToken daemonToken;

	public AfterCommitDispatcher(BridgeExecutor executor) {
		this.executor = executor;
	}

	/**
	 * Wired by {@link org.openmrs.module.querystore.QueryStoreActivator} once the module receives
	 * its token. Volatile so tasks already in the pool see the update without an extra publish.
	 */
	public void setDaemonToken(DaemonToken token) {
		this.daemonToken = token;
	}

	public void dispatch(Runnable task) {
		Runnable guarded = wrap(task);
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					executor.submit(guarded);
				}
			});
		} else {
			executor.submit(guarded);
		}
	}

	private Runnable wrap(Runnable task) {
		return () -> {
			try {
				runWithDaemonContext(task);
			}
			catch (RuntimeException e) {
				// Log and swallow: the bridge is best-effort per ADR Decision 12. The
				// conditional-upsert-by-version invariant (ADR Decision 3) means a missed AOP
				// projection is corrected by the next save, an event handler when it ships, or
				// the bootstrap pass — none of those overwrite the freshest document.
				log.warn("Bridge projection task failed; swallowing per ADR Decision 12", e);
			}
		};
	}

	/**
	 * Visible for testing. Production path: the dispatched task ran on a {@link BridgeExecutor}
	 * pool thread which has no OpenMRS Context, so we hand the actual work to
	 * {@link Daemon#runInDaemonThreadAndWait} which sets up a daemon-user {@code UserContext} on
	 * a fresh thread and joins. Tests that haven't wired a token (and don't need a Context for
	 * their assertions) get inline execution as a fall-through.
	 *
	 * <p>{@code runInDaemonThreadAndWait} does not surface throwables from the spawned daemon
	 * thread to the caller: on platforms whose implementation wraps the work in a
	 * {@code Future.get()} the {@code ExecutionException} is caught and discarded; on platforms
	 * that use {@code thread.join()} the daemon's exception only reaches the JVM's
	 * {@code UncaughtExceptionHandler}. Either way, without the per-task capture below an
	 * embedder failure inside the daemon would be invisible to {@link #wrap(Runnable)}'s
	 * log-and-swallow guard — the warn-log operators rely on to spot poison documents would be
	 * silently disabled. Capture the exception on the daemon side and rethrow on the pool thread
	 * so the diagnostic chain still fires.
	 */
	void runWithDaemonContext(Runnable task) {
		DaemonToken token = this.daemonToken;
		if (token == null) {
			task.run();
			return;
		}
		AtomicReference<RuntimeException> failure = new AtomicReference<>();
		daemonExecutor.execute(() -> {
			try {
				task.run();
			}
			catch (RuntimeException re) {
				failure.set(re);
			}
		}, token);
		RuntimeException re = failure.get();
		if (re != null) {
			throw re;
		}
	}

	/**
	 * Test seam over {@link Daemon#runInDaemonThreadAndWait}: the static call cannot be stubbed
	 * cleanly without PowerMock, so the dispatcher routes through this functional interface and
	 * tests substitute it via {@link #setDaemonExecutorForTest}. Production keeps the default
	 * (the platform's daemon runner); tests can simulate platform-swallow behaviour and pin
	 * {@link #runWithDaemonContext}'s capture-rethrow logic against it.
	 */
	@FunctionalInterface
	interface DaemonExecutor {
		void execute(Runnable task, DaemonToken token);
	}

	private volatile DaemonExecutor daemonExecutor = Daemon::runInDaemonThreadAndWait;

	void setDaemonExecutorForTest(DaemonExecutor executor) {
		this.daemonExecutor = executor;
	}
}
