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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.LongSupplier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.querystore.SafeLog;

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

	/** Default for {@code querystore.projection.maxConcurrent}. */
	public static final int DEFAULT_MAX_CONCURRENT_PROJECTIONS = 4;

	/** Largest {@code querystore.projection.maxConcurrent} honoured. */
	public static final int MAX_PROJECTION_SLOTS = 32;

	// How many failed patients are remembered. Past this the least recently failed is forgotten, once
	// setFailedChartDropper's dropper has deleted its documents: a failed warm may have left a partial
	// chart, which would otherwise read as indexed once nothing says it failed.
	public static final int MAX_FAILED_PATIENTS = 10_000;

	// Every per-patient projection holds one of these while it runs: a background warm, and a
	// synchronous cold-touch projection on a request thread alike. Daemon spawns a thread per warm and
	// each projection saturates the CPU's cores through the embedder, so without one bound a burst of
	// cold patients would run that many at once. Replaced only by setMaxConcurrentProjections, at
	// module start.
	private volatile Semaphore projectionSlots = new Semaphore(DEFAULT_MAX_CONCURRENT_PROJECTIONS);

	// Patients with a projection in flight (a background warm, or a synchronous one holding a slot),
	// each with a latch released when it finishes: the per-patient in-flight guard, the source of the
	// "projecting" index status, and what a second projection for the patient waits on instead of
	// taking a slot of its own. An entry exists only while its projection holds a slot.
	private final ConcurrentHashMap<String, CountDownLatch> inFlight = new ConcurrentHashMap<>();

	// Resource types with a launchResyncAsync running, so repeated requests share one scan.
	private final Set<String> resyncing = ConcurrentHashMap.newKeySet();

	/** How long a patient whose projection failed once waits before a warm retries it. */
	public static final long RETRY_BACKOFF_BASE_MILLIS = 30_000L;

	/** Longest wait between retries: each failure in a row doubles the wait, up to this. */
	public static final long MAX_RETRY_BACKOFF_MILLIS = 60 * 60_000L;

	private volatile LongSupplier clock = System::currentTimeMillis;

	private volatile Predicate<String> failedChartDropper = patientUuid -> false;

	// Patients whose last warm failed: the source of the "failed" index status, with how many times
	// in a row and when last, for the retry backoff. Bounded, least recently failed first out, so a
	// caller warming uuids that never index cannot grow it forever (see recordFailure). Compound
	// updates lock the map.
	private final Map<String, Failure> failedPatients = Collections.synchronizedMap(new LinkedHashMap<String, Failure>());

	private static final class Failure {

		final int inARow;

		final long atMillis;

		Failure(int inARow, long atMillis) {
			this.inARow = inARow;
			this.atMillis = atMillis;
		}
	}

	/**
	 * Sets how many per-patient projections may run at once ({@code querystore.projection.maxConcurrent}).
	 * Called once at module start, before any projection; a later call does not shrink projections
	 * already holding a slot. Clamped to 1..{@link #MAX_PROJECTION_SLOTS}.
	 */
	public void setMaxConcurrentProjections(int max) {
		this.projectionSlots = new Semaphore(Math.max(1, Math.min(MAX_PROJECTION_SLOTS, max)));
	}

	/**
	 * Runs one patient's projection ({@code work}) on the calling thread once a projection slot is
	 * free, waiting at most {@code timeoutMillis} for one. Backs the synchronous cold-touch projection
	 * and the bounded reindex, so they share the bound with background warms. While it runs the
	 * patient reads as warming: the chart is half written until it finishes, and a concurrent status
	 * read or non-waiting search must not take it as indexed.
	 *
	 * <p>If a projection for the patient is already in flight, this one does not run and takes no
	 * slot: it waits, within the same {@code timeoutMillis}, for that one to finish. Otherwise
	 * {@code work} runs; it returns whether it re-projected the whole chart, which clears a recorded
	 * failure, and a thrown exception records one ({@link #hasFailed}) and propagates.
	 *
	 * @return {@code true} when this call projected, or the projection it waited for finished;
	 *         {@code false} when no slot came free, or the other projection did not finish, in time
	 *         (or the wait was interrupted)
	 */
	public boolean runWithProjectionSlot(String patientUuid, BooleanSupplier work, long timeoutMillis) {
		return runProjection(patientUuid, work, timeoutMillis, true);
	}

	/**
	 * Like {@link #runWithProjectionSlot}, but always runs {@code work} itself: if a projection for
	 * the patient is in flight it waits for that to finish, then takes a slot and runs. For a forced
	 * re-projection (reindex), which must not settle for a projection that started before it.
	 *
	 * @return {@code true} when {@code work} ran; {@code false} when no slot came free in time
	 */
	public boolean runExclusiveProjection(String patientUuid, BooleanSupplier work, long timeoutMillis) {
		return runProjection(patientUuid, work, timeoutMillis, false);
	}

	private boolean runProjection(String patientUuid, BooleanSupplier work, long timeoutMillis, boolean joinInFlight) {
		long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(Math.max(0, timeoutMillis));
		Semaphore slots = this.projectionSlots;
		CountDownLatch done = new CountDownLatch(1);
		while (true) {
			CountDownLatch running = patientUuid == null ? null : inFlight.get(patientUuid);
			if (running != null) {
				if (!awaitUntil(running, deadline)) {
					return false;
				}
				if (joinInFlight) {
					return true;
				}
				continue;
			}
			try {
				if (!slots.tryAcquire(Math.max(0, deadline - System.nanoTime()), TimeUnit.NANOSECONDS)) {
					log.debug("No projection slot came free in time; not projecting");
					return false;
				}
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return false;
			}
			CountDownLatch other = patientUuid == null ? null : inFlight.putIfAbsent(patientUuid, done);
			if (other == null) {
				break;
			}
			// Another projection registered between the check and the slot: let it run, as above.
			slots.release();
			if (!awaitUntil(other, deadline)) {
				return false;
			}
			if (joinInFlight) {
				return true;
			}
		}
		try {
			if (work.getAsBoolean() && patientUuid != null) {
				failedPatients.remove(patientUuid);
			}
			return true;
		}
		catch (RuntimeException e) {
			if (patientUuid != null) {
				recordFailure(patientUuid);
			}
			throw e;
		}
		finally {
			if (patientUuid != null) {
				inFlight.remove(patientUuid, done);
			}
			done.countDown();
			slots.release();
		}
	}

	private static boolean awaitUntil(CountDownLatch latch, long deadlineNanos) {
		try {
			return latch.await(Math.max(0, deadlineNanos - System.nanoTime()), TimeUnit.NANOSECONDS);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		}
	}

	public void setDaemonToken(DaemonToken token) {
		this.daemonToken = token;
	}

	/**
	 * Ensures a global {@link BootstrapService#bootstrap()} is running on a daemon thread, returning
	 * immediately. If one is already in flight, this is a no-op that still reports success — the
	 * caller's intent ("a global reindex is underway") is satisfied either way, and progress is
	 * observed out-of-band via the indexing-status endpoint.
	 *
	 * <p>{@code BootstrapServiceImpl.bootstrap()} catches per-type failures internally and rethrows
	 * none of them except {@code EmbeddingProviderClosedException}, which ends the run once a stop has
	 * closed the embedding provider. The task wraps it only to clear the in-flight flag in a
	 * {@code finally}, so that throw, or an unexpected one (e.g. service lookup failure), still
	 * releases the flag and surfaces via the framework's thread-exception handler.
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
	 * Launches a targeted re-sync of a single resource type on a daemon thread (reconciliation
	 * remediation; see {@link BootstrapService#resyncType(String)}). Like {@link #launchAsync()} it
	 * needs the {@link DaemonToken} for the daemon-user {@code UserContext} the scan's GP reads
	 * require, and resolves the service on the daemon thread.
	 *
	 * <p>Single-flight per type: a request for a type whose re-sync is already running joins it and
	 * starts nothing, so repeated requests cannot pile up daemon threads, each holding a session and a
	 * full scan, on the type's lock. The caller is expected to have validated {@code resourceType}
	 * (e.g. against {@link BootstrapService#getResourceTypeNames()}).
	 *
	 * @return {@code true} once a re-sync of the type is running (newly launched or already);
	 *         {@code false} only when no {@link DaemonToken} is wired yet, in which case nothing runs.
	 */
	public boolean launchResyncAsync(String resourceType) {
		DaemonToken token = this.daemonToken;
		if (token == null) {
			log.warn("Daemon token unavailable; cannot launch type re-sync for " + resourceType
			        + " (BootstrapService.resyncType(String) can still be called programmatically)");
			return false;
		}
		if (!resyncing.add(resourceType)) {
			log.info("Type re-sync already running for " + resourceType + "; ignoring duplicate request");
			return true;
		}
		try {
			daemonExecutor.execute(() -> {
				try {
					log.info("Type re-sync starting for " + resourceType);
					Context.getService(BootstrapService.class).resyncType(resourceType);
					log.info("Type re-sync completed for " + resourceType);
				}
				catch (RuntimeException e) {
					// Fire-and-forget on a daemon thread — no caller upstack to surface to, so log here
					// (mirrors the after-commit sync path's log-and-swallow per ADR Decision 12).
					SafeLog.error(BootstrapLauncher.class, "Type re-sync failed for " + resourceType, e);
				}
				finally {
					resyncing.remove(resourceType);
				}
			}, token);
		}
		catch (RuntimeException e) {
			resyncing.remove(resourceType);
			throw e;
		}
		return true;
	}

	/**
	 * Runs one patient's warm ({@code work}, typically {@link BootstrapService#ensureIndexed}) on a
	 * daemon thread, returning immediately. Backs the omnibar's warm-on-chart-open: the chart is
	 * projected while the user reads the banner, so the first keystroke doesn't pay the cold
	 * projection. {@code work} returns whether it re-projected the whole chart, which clears a
	 * recorded failure; it signals failure by throwing, and the patient then reads as failed
	 * ({@link #hasFailed}) until a later full projection succeeds.
	 *
	 * <p>Idempotent per patient: a request for a patient already in flight is a no-op that reports
	 * success. Bounded across patients by the projection slots it shares with synchronous projections
	 * ({@link #runWithProjectionSlot}): with none free it starts nothing.
	 *
	 * @return {@code true} once a warm for this patient is in flight (newly launched or already
	 *         running); {@code false} when no {@link DaemonToken} is wired yet or no projection slot is
	 *         free, in which case nothing runs and the caller may retry later.
	 */
	public boolean launchPatientWarm(String patientUuid, BooleanSupplier work) {
		DaemonToken token = this.daemonToken;
		if (token == null || patientUuid == null) {
			return false;
		}
		// Slot first, then the in-flight entry, so a patient is only ever in flight while its
		// projection holds a slot: registering first let a racing duplicate report "in flight" for a
		// warm that then found no slot and never ran.
		Semaphore slots = this.projectionSlots;
		if (!slots.tryAcquire()) {
			log.debug("No projection slot free; not launching a warm");
			return inFlight.containsKey(patientUuid);
		}
		CountDownLatch done = new CountDownLatch(1);
		if (inFlight.putIfAbsent(patientUuid, done) != null) {
			slots.release();
			log.debug("Patient warm already in flight; ignoring duplicate");
			return true;
		}
		log.debug("Patient warm starting (" + inFlight.size() + " in flight)");
		try {
			daemonExecutor.execute(() -> {
				try {
					if (work.getAsBoolean()) {
						failedPatients.remove(patientUuid);
					}
				}
				catch (RuntimeException e) {
					// Fire-and-forget: no caller upstack, so the failure is kept for the status read.
					recordFailure(patientUuid);
					SafeLog.warn(BootstrapLauncher.class, "Background patient warm failed (patient uuid at DEBUG)", e);
					log.debug("Background warm failed for patient " + patientUuid);
				}
				finally {
					inFlight.remove(patientUuid, done);
					done.countDown();
					slots.release();
				}
			}, token);
		}
		catch (RuntimeException e) {
			inFlight.remove(patientUuid, done);
			done.countDown();
			slots.release();
			throw e;
		}
		return true;
	}

	/**
	 * True while a projection for this patient is running: a {@link #launchPatientWarm}, or a
	 * synchronous one through {@link #runWithProjectionSlot}.
	 */
	public boolean isWarming(String patientUuid) {
		return patientUuid != null && inFlight.containsKey(patientUuid);
	}

	/**
	 * True when this patient's last projection through this launcher failed and no full projection
	 * has succeeded since. Per JVM and not persisted: a restart forgets it, as does the bound on how
	 * many are remembered.
	 */
	public boolean hasFailed(String patientUuid) {
		return patientUuid != null && failedPatients.containsKey(patientUuid);
	}

	/**
	 * How long until a warm may retry this failed patient: 0 when it may now, or it has not failed.
	 * The wait is {@link #RETRY_BACKOFF_BASE_MILLIS} after one failure and doubles with each failure
	 * in a row, up to {@link #MAX_RETRY_BACKOFF_MILLIS}, so a chart that never projects is retried
	 * now and then rather than on every keystroke, and is never given up on.
	 */
	public long millisUntilRetry(String patientUuid) {
		Failure failure = patientUuid == null ? null : failedPatients.get(patientUuid);
		if (failure == null) {
			return 0;
		}
		long backoff = Math.min(MAX_RETRY_BACKOFF_MILLIS,
		    RETRY_BACKOFF_BASE_MILLIS << Math.min(failure.inARow - 1, 20));
		return Math.max(0, failure.atMillis + backoff - clock.getAsLong());
	}

	private void recordFailure(String patientUuid) {
		synchronized (failedPatients) {
			// Removed and put back, so the bound forgets the least recently failed first.
			Failure previous = failedPatients.remove(patientUuid);
			failedPatients.put(patientUuid, new Failure(previous == null ? 1 : previous.inARow + 1, clock.getAsLong()));
		}
		forgetPastTheBound();
	}

	/**
	 * Brings the failure map back to {@link #MAX_FAILED_PATIENTS}, least recently failed first. A
	 * patient is forgotten only once {@link #setFailedChartDropper its chart has been dropped}: until
	 * then it reads failed, so a search during the drop serves none of its rows. The drop runs outside
	 * the map lock. One that does not succeed keeps the failure as it was (no extra failure counted),
	 * moves it to the back so the next attempt tries another patient, and stops this round, so a
	 * backend that cannot delete is not hammered. The map then runs past its bound by the charts that
	 * could not be dropped, each a real patient with documents, and shrinks back once drops work.
	 */
	private void forgetPastTheBound() {
		while (true) {
			Map.Entry<String, Failure> eldest;
			synchronized (failedPatients) {
				if (failedPatients.size() <= MAX_FAILED_PATIENTS) {
					return;
				}
				eldest = new java.util.AbstractMap.SimpleImmutableEntry<>(failedPatients.entrySet().iterator().next());
			}
			String uuid = eldest.getKey();
			boolean dropped;
			try {
				dropped = failedChartDropper.test(uuid);
			}
			catch (RuntimeException e) {
				SafeLog.warn(BootstrapLauncher.class, "Could not drop a failed chart past the failure bound (patient uuid at DEBUG)", e);
				log.debug("Could not drop the failed chart of patient " + uuid);
				dropped = false;
			}
			synchronized (failedPatients) {
				if (dropped) {
					failedPatients.remove(uuid, eldest.getValue());
					continue;
				}
				if (failedPatients.get(uuid) == eldest.getValue()) {
					failedPatients.remove(uuid);
					failedPatients.put(uuid, eldest.getValue());
				}
				return;
			}
		}
	}

	/**
	 * Drops a failed patient's chart so its failure can be forgotten past {@link #MAX_FAILED_PATIENTS},
	 * and says whether the chart is gone. Called on the thread that recorded a newer failure, outside
	 * this launcher's locks, while the patient still reads failed. Until one is set, no failure is
	 * forgotten while a chart might remain: the default says nothing was dropped.
	 */
	public void setFailedChartDropper(Predicate<String> dropper) {
		this.failedChartDropper = dropper;
	}

	/** Package-private test seam: the time, in epoch millis, the retry backoff is measured against. */
	void setClock(LongSupplier clock) {
		this.clock = clock;
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
