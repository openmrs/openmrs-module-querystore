/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.bootstrap;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.DaemonToken;

/**
 * Single home for "run the global {@link BootstrapService#bootstrap()} on an OpenMRS daemon thread".
 * Both callers — {@code QueryStoreActivator} autostart and the on-demand reindex REST endpoint
 * ({@code POST /querystore/reindex} with {@code scope:"all"}) — go through here so the daemon-thread
 * launch lives in exactly one place.
 *
 * <p>A {@link DaemonToken} is required: the bootstrap reads global properties for its embedder and
 * backend, which needs the daemon-user {@link org.openmrs.api.context.UserContext} the token sets up
 * on the spawned thread. Until the activator wires the token, {@link #launchAsync()} refuses (returns
 * {@code false}) rather than spawning a context-less thread that would silently misbehave — the
 * caller surfaces that to its own caller (the activator logs and skips; the endpoint returns 503).
 *
 * <p>The token is wired by the activator the same way the sync dispatcher's is (eagerly from
 * {@code setDaemonToken} and again from {@code started()}), mirroring
 * {@link org.openmrs.module.querystore.sync.AfterCommitDispatcher}. The {@link DaemonExecutor} seam
 * over the static {@link Daemon#runInDaemonThread} call lets tests verify the launch handoff without
 * spawning a real thread, again mirroring the dispatcher.
 */
public class BootstrapLauncher {

	private static final Log log = LogFactory.getLog(BootstrapLauncher.class);

	private volatile DaemonToken daemonToken;

	private volatile DaemonExecutor daemonExecutor = Daemon::runInDaemonThread;

	// Collapses overlapping launch requests (autostart racing the endpoint, an admin double-click, a
	// retried script) to a single in-flight run. BootstrapServiceImpl's per-type locks already keep
	// concurrent runs from corrupting progress bookkeeping; this additionally stops redundant daemon
	// threads from piling up parked on those locks and re-scanning an already-complete cursor.
	private final AtomicBoolean running = new AtomicBoolean(false);

	public void setDaemonToken(DaemonToken token) {
		this.daemonToken = token;
	}

	/**
	 * Ensures a global {@link BootstrapService#bootstrap()} is running on a daemon thread, returning
	 * immediately. If one is already in flight, this is a no-op that still reports success — the
	 * caller's intent ("a global reindex is underway") is satisfied either way, and progress is
	 * observed out-of-band via the indexing-status endpoint.
	 *
	 * <p>{@code BootstrapServiceImpl.bootstrap()} catches per-type failures internally and never
	 * rethrows; the task wraps it only to clear the in-flight flag in a {@code finally}, so an
	 * unexpected throw (e.g. service lookup failure) still releases the flag and surfaces via the
	 * framework's thread-exception handler.
	 *
	 * @return {@code true} once a bootstrap is in flight (newly launched or already running);
	 *         {@code false} only when no {@link DaemonToken} is wired yet, in which case nothing runs.
	 */
	public boolean launchAsync() {
		DaemonToken token = this.daemonToken;
		if (token == null) {
			log.warn("Daemon token unavailable; cannot launch bootstrap "
			        + "(BootstrapService.bootstrap() can still be called programmatically)");
			return false;
		}
		// CAS before the submit below (not after) so two racing callers can't both launch: the loser
		// sees running==true and collapses onto the in-flight run. Accepted trade-off: if execute()
		// throws a synchronous RuntimeException (near-impossible — a valid token is not rejected, and
		// native-thread exhaustion surfaces as an OOM Error, not caught here) a duplicate that already
		// observed running==true reports success while this launch fails. Moving the CAS after submit
		// would close that window but reintroduce double-launch, the worse defect — so do not.
		if (!running.compareAndSet(false, true)) {
			log.info("Bootstrap already in flight; ignoring duplicate launch request");
			return true;
		}
		try {
			// Resolve the service on the daemon thread, where the token has set up the UserContext
			// the bootstrap's GP reads need — not on the calling thread.
			daemonExecutor.execute(() -> {
				try {
					log.info("Bootstrap starting");
					Context.getService(BootstrapService.class).bootstrap();
					log.info("Bootstrap completed");
				}
				finally {
					running.set(false);
				}
			}, token);
		}
		catch (RuntimeException e) {
			// The task never made it onto a thread (e.g. thread-creation/token failure), so its
			// finally cannot release the flag. Release it here, else every future launch is stuck
			// being rejected as a duplicate, and rethrow so the failure isn't swallowed.
			running.set(false);
			throw e;
		}
		return true;
	}

	/**
	 * Test seam over {@link Daemon#runInDaemonThread}: the static call cannot be stubbed, so the
	 * launch handoff (task + token) is verified through this indirection. Mirrors
	 * {@link org.openmrs.module.querystore.sync.AfterCommitDispatcher}'s executor seam.
	 */
	interface DaemonExecutor {

		void execute(Runnable task, DaemonToken token);
	}

	/** Package-private — tests live alongside the implementation; production uses the default. */
	void setDaemonExecutor(DaemonExecutor executor) {
		this.daemonExecutor = executor;
	}
}
