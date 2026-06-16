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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Shared executor for the events-first sync pipeline (ADR Decision 12). The events consumer's
 * after-commit dispatcher hands projection tasks here so indexing runs off the clinical request
 * thread, after the originating transaction has committed. Pool size is two threads — enough headroom to overlap
 * one embedding with the next document's submission, but no wider because the default ONNX
 * embedder's {@code embed} is {@code synchronized} (a larger pool would just queue against the
 * embedder's monitor).
 *
 * <p>Wired as a Spring bean with {@code init-method="start"} and {@code destroy-method="stop"} so
 * the executor's lifecycle tracks the module's. Failures inside submitted tasks are caught and
 * logged at the dispatcher, never here — this class is a plain executor wrapper.
 */
public class SyncExecutor {

	private static final Log log = LogFactory.getLog(SyncExecutor.class);

	/** Drain timeout on shutdown. Indexing tasks are short (one embed + one upsert, < 1s under the
	 *  default ONNX embedder); 30s tolerates an in-flight task without stalling module unload more
	 *  than a redeploy can afford. Tasks dropped on timeout are eventually re-projected by the
	 *  bootstrap. */
	private static final long SHUTDOWN_TIMEOUT_SECONDS = 30L;

	private final int poolSize;

	private ExecutorService executor;

	public SyncExecutor() {
		this(2);
	}

	SyncExecutor(int poolSize) {
		this.poolSize = poolSize;
	}

	public void start() {
		ThreadFactory factory = new ThreadFactory() {
			private final AtomicInteger n = new AtomicInteger(1);

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, "querystore-sync-" + n.getAndIncrement());
				t.setDaemon(true);
				return t;
			}
		};
		executor = Executors.newFixedThreadPool(poolSize, factory);
	}

	/**
	 * Shuts the pool down with a bounded drain. Dispatched tasks run inside daemon threads spawned
	 * by {@code Daemon.runInDaemonThreadAndWait} (see {@code AfterCommitDispatcher}); on forced
	 * {@code shutdownNow()} the pool thread's wait is interrupted but the daemon thread is NOT
	 * — its work continues against potentially-closing backend writers and may throw
	 * {@code AlreadyClosedException}, and on a Tomcat redeploy the leaked daemon thread holds the
	 * old classloader until it exits naturally. The window is bounded by SHUTDOWN_TIMEOUT_SECONDS;
	 * proper interrupt-propagation to the daemon thread is a follow-up tied to the broader
	 * sync-shutdown coordination (ADR Decision 12 open).
	 */
	public void stop() {
		if (executor == null) {
			return;
		}
		executor.shutdown();
		try {
			if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
				log.warn("SyncExecutor did not drain within " + SHUTDOWN_TIMEOUT_SECONDS
				        + "s; outstanding tasks are dropped and any in-flight daemon thread will"
				        + " continue against potentially-closing writers (see stop() javadoc)");
				executor.shutdownNow();
			}
		}
		catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			executor.shutdownNow();
		}
	}

	public void submit(Runnable task) {
		if (executor.isShutdown()) {
			log.warn("SyncExecutor is shut down; dropping projection task");
			return;
		}
		executor.submit(task);
	}
}
