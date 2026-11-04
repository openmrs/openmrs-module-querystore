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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.querystore.LogCapture;

/**
 * Pins {@link BootstrapLauncher}'s daemon-token contract via its {@code DaemonExecutor} seam, so the
 * launch handoff can be verified without spawning a real daemon thread or standing up Spring. The
 * launched task body (which calls {@code BootstrapService.bootstrap()} through {@link
 * org.openmrs.api.context.Context}) is deliberately NOT executed here — these tests assert that the
 * task and token are handed to the executor, not what the task does once running; the bootstrap run
 * itself is exercised by the autostart path and end-to-end.
 */
public class BootstrapLauncherTest {

	@Test
	public void launchAsync_returnsFalseAndHandsOffNothing_whenNoDaemonToken() {
		BootstrapLauncher launcher = new BootstrapLauncher();
		final int[] launches = { 0 };
		launcher.setDaemonExecutor((task, token) -> launches[0]++);

		// No token wired: the bootstrap reads global properties for its embedder/backend and so
		// cannot run without the daemon-user UserContext the token provides. The launcher must
		// refuse rather than spawn a context-less thread.
		assertFalse(launcher.launchAsync());
		assertEquals("must not hand any task to the daemon executor without a token", 0, launches[0]);
	}

	@Test
	public void launchAsync_handsTaskAndWiredTokenToDaemonExecutor_whenTokenPresent() {
		BootstrapLauncher launcher = new BootstrapLauncher();
		final Runnable[] capturedTask = { null };
		final DaemonToken[] capturedToken = { null };
		final int[] launches = { 0 };
		launcher.setDaemonExecutor((task, token) -> {
			capturedTask[0] = task;
			capturedToken[0] = token;
			launches[0]++;
		});
		DaemonToken token = new DaemonToken("token-launch");
		launcher.setDaemonToken(token);

		assertTrue(launcher.launchAsync());
		assertEquals(1, launches[0]);
		assertSame("the wired token must be the one handed to the daemon thread", token, capturedToken[0]);
		assertNotNull("a bootstrap task must be handed off to the daemon executor", capturedTask[0]);
	}

	@Test
	public void launchAsync_collapsesOverlappingRequestsToOneInFlightBootstrap() {
		BootstrapLauncher launcher = new BootstrapLauncher();
		final int[] launches = { 0 };
		// Executor that never runs the task, so the first launch stays "in flight" for the duration
		// of the test — modelling a long-running bootstrap with a second request arriving mid-run.
		launcher.setDaemonExecutor((task, token) -> launches[0]++);
		launcher.setDaemonToken(new DaemonToken("token-idempotent"));

		assertTrue(launcher.launchAsync());
		assertTrue("a duplicate launch while one is in flight is still reported as accepted",
		    launcher.launchAsync());
		assertEquals("must not spawn a second daemon bootstrap while one is in flight", 1, launches[0]);
	}

	@Test
	public void launchAsync_releasesInFlightFlag_afterTheTaskCompletes() {
		BootstrapLauncher launcher = new BootstrapLauncher();
		final int[] launches = { 0 };
		// Run the task as a daemon thread would — execute it and isolate any throwable from the
		// caller — so the task's finally runs and clears the in-flight flag. (The task's
		// Context.getService lookup throws here, with no Spring context; that stands in for "the
		// run finished" and must still release the flag.)
		launcher.setDaemonExecutor((task, token) -> {
			launches[0]++;
			try {
				task.run();
			}
			catch (RuntimeException isolatedLikeADaemonThread) {
				// swallow — a real daemon thread surfaces this via the thread-exception handler,
				// not to the launching caller
			}
		});
		launcher.setDaemonToken(new DaemonToken("token-release"));

		launcher.launchAsync();
		launcher.launchAsync();

		assertEquals("the flag must be released once a run completes, so the next request launches afresh",
		    2, launches[0]);
	}

	@Test
	public void launchAsync_releasesInFlightFlag_whenSubmissionItselfThrows() {
		BootstrapLauncher launcher = new BootstrapLauncher();
		final int[] attempts = { 0 };
		// Submission fails before the task ever reaches a thread (e.g. thread-creation failure), so
		// the task's finally cannot run — launchAsync must release the flag itself and rethrow.
		launcher.setDaemonExecutor((task, token) -> {
			attempts[0]++;
			throw new IllegalStateException("thread creation failed");
		});
		launcher.setDaemonToken(new DaemonToken("token-submit-fail"));

		try {
			launcher.launchAsync();
			fail("a submission failure must propagate, not be swallowed");
		}
		catch (IllegalStateException expected) {
			// expected
		}
		try {
			launcher.launchAsync();
			fail("retry also throws (executor still failing) — proving the flag was released, not stuck");
		}
		catch (IllegalStateException expected) {
			// expected
		}
		assertEquals("a stuck flag would short-circuit the retry as a duplicate; both attempts must"
		        + " reach the executor", 2, attempts[0]);
	}

	@Test
	public void launchResyncAsync_returnsFalseAndHandsOffNothing_whenNoDaemonToken() {
		BootstrapLauncher launcher = new BootstrapLauncher();
		final int[] launches = { 0 };
		launcher.setDaemonExecutor((task, token) -> launches[0]++);

		// Same token contract as launchAsync: the re-walk reads GPs for its embedder/backend and so
		// needs the daemon-user UserContext the token provides; refuse rather than spawn a context-less
		// thread.
		assertFalse(launcher.launchResyncAsync("obs"));
		assertEquals("must not hand any task to the daemon executor without a token", 0, launches[0]);
	}

	@Test
	public void launchResyncAsync_handsTaskAndWiredTokenToDaemonExecutor_whenTokenPresent() {
		BootstrapLauncher launcher = new BootstrapLauncher();
		final Runnable[] capturedTask = { null };
		final DaemonToken[] capturedToken = { null };
		final int[] launches = { 0 };
		launcher.setDaemonExecutor((task, token) -> {
			capturedTask[0] = task;
			capturedToken[0] = token;
			launches[0]++;
		});
		DaemonToken token = new DaemonToken("token-resync");
		launcher.setDaemonToken(token);

		assertTrue(launcher.launchResyncAsync("obs"));
		assertEquals(1, launches[0]);
		assertSame("the wired token must be the one handed to the daemon thread", token, capturedToken[0]);
		assertNotNull("a re-sync task must be handed off to the daemon executor", capturedTask[0]);
	}

	@Test
	public void launchResyncAsync_collapsesWhileOneIsRunning_andRunsAgainAfter() {
		// Single-flight per type: repeated requests while a re-sync runs join it rather than piling up
		// daemon threads on the type lock; once it finishes, the next request starts a new one.
		BootstrapLauncher launcher = new BootstrapLauncher();
		final java.util.List<Runnable> tasks = new java.util.ArrayList<>();
		launcher.setDaemonExecutor((task, token) -> tasks.add(task));
		launcher.setDaemonToken(new DaemonToken("token-resync-twice"));

		assertTrue(launcher.launchResyncAsync("obs"));
		assertTrue("a duplicate reports the running re-sync", launcher.launchResyncAsync("obs"));
		assertTrue("another type runs alongside", launcher.launchResyncAsync("condition"));
		assertEquals(2, tasks.size());

		tasks.get(0).run(); // finishes (its service lookup throws without a context; the flag still clears)
		assertTrue(launcher.launchResyncAsync("obs"));
		assertEquals(3, tasks.size());
	}
	@Test
	public void launchPatientWarm_collapsesRepeatsPerPatient_andReportsWarmingUntilTheTaskEnds() {
		BootstrapLauncher launcher = new BootstrapLauncher();
		final List<Runnable> tasks = new ArrayList<>();
		launcher.setDaemonExecutor((task, token) -> tasks.add(task));
		launcher.setDaemonToken(new DaemonToken("token-warm"));

		assertTrue(launcher.launchPatientWarm("patient-1", () -> true));
		assertTrue("a repeat for the same patient reports success", launcher.launchPatientWarm("patient-1", () -> true));
		assertEquals("but hands off only one warm", 1, tasks.size());
		assertTrue(launcher.isWarming("patient-1"));
		assertFalse(launcher.isWarming("patient-2"));

		tasks.get(0).run();
		assertFalse(launcher.isWarming("patient-1"));
		assertTrue(launcher.launchPatientWarm("patient-1", () -> true));
		assertEquals(2, tasks.size());
	}

	@Test
	public void launchPatientWarm_marksAThrowingWarmFailed_untilALaterOneSucceeds() {
		BootstrapLauncher launcher = SynchronousLaunchers.create();

		assertTrue(launcher.launchPatientWarm("patient-1", () -> {
			throw new IllegalStateException("projection blew up");
		}));
		assertTrue(launcher.hasFailed("patient-1"));
		assertFalse("a failed warm no longer reads as in flight", launcher.isWarming("patient-1"));
		assertFalse(launcher.hasFailed("patient-2"));

		assertTrue("a failed patient can be warmed again", launcher.launchPatientWarm("patient-1", () -> true));
		assertFalse("and success clears the failure", launcher.hasFailed("patient-1"));
	}

	@Test
	public void launchPatientWarm_refusesBeyondTheConcurrencyCap_andWithoutAToken() {
		BootstrapLauncher launcher = new BootstrapLauncher();
		final int[] launches = { 0 };
		launcher.setDaemonExecutor((task, token) -> launches[0]++);

		assertFalse("no token, no warm", launcher.launchPatientWarm("patient-0", () -> true));

		launcher.setDaemonToken(new DaemonToken("token-warm-cap"));
		for (int i = 1; i <= BootstrapLauncher.DEFAULT_MAX_CONCURRENT_PROJECTIONS; i++) {
			assertTrue(launcher.launchPatientWarm("patient-" + i, () -> true));
		}
		assertFalse("one past the cap is refused", launcher.launchPatientWarm("patient-x", () -> true));
		assertTrue("with every slot busy, a repeat for a patient whose warm holds one is still in flight",
		    launcher.launchPatientWarm("patient-1", () -> true));
		assertFalse(launcher.isWarming("patient-x"));
		assertEquals(BootstrapLauncher.DEFAULT_MAX_CONCURRENT_PROJECTIONS, launches[0]);
	}

	@Test
	public void launchPatientWarm_logsStartAndDuplicateAtDebug_withoutThePatientUuid() {
		BootstrapLauncher launcher = new BootstrapLauncher();
		launcher.setDaemonExecutor((task, token) -> { });
		launcher.setDaemonToken(new DaemonToken("token-warm-log"));

		try (LogCapture log = LogCapture.of(BootstrapLauncher.class)) {
			launcher.launchPatientWarm("patient-uuid-1", () -> true);
			launcher.launchPatientWarm("patient-uuid-1", () -> true);

			List<String> debug = log.at("DEBUG");
			assertEquals(2, debug.size());
			assertTrue(debug.get(0), debug.get(0).startsWith("Patient warm starting"));
			assertTrue(debug.get(1), debug.get(1).contains("ignoring duplicate"));
			for (String line : log.lines()) {
				assertFalse(line, line.contains("patient-uuid-1"));
			}
		}
	}

	@Test
	public void launchPatientWarm_failureWarnsWithoutThePatientUuid_andNamesItOnlyAtDebug() {
		BootstrapLauncher launcher = SynchronousLaunchers.create();

		try (LogCapture log = LogCapture.of(BootstrapLauncher.class)) {
			launcher.launchPatientWarm("patient-uuid-2", () -> {
				throw new IllegalStateException("boom");
			});

			assertEquals(1, log.at("WARN").size());
			assertFalse(log.at("WARN").get(0).contains("patient-uuid-2"));
			assertTrue(log.at("DEBUG").stream().anyMatch(l -> l.contains("patient-uuid-2")));
		}
	}

	@Test
	public void launchPatientWarm_runsOnNotStarted_wheneverThisCallStartsNoWarm() {
		// r3 code N2 / sec N1, r4 code B1: the caller undoes its own setup whenever its work will not run,
		// including when another call's warm for the patient is in flight.
		List<Runnable> parked = new ArrayList<>();
		BootstrapLauncher launcher = SynchronousLaunchers.parked(1, parked);
		int[] notStarted = { 0 };
		Runnable count = () -> notStarted[0]++;

		assertTrue(launcher.launchPatientWarm("patient-1", () -> true, count));
		assertEquals("started", 0, notStarted[0]);
		assertTrue("no slot, but this patient's warm is in flight", launcher.launchPatientWarm("patient-1", () -> true, count));
		assertEquals(1, notStarted[0]);
		assertFalse("no slot, nothing in flight for this patient", launcher.launchPatientWarm("patient-2", () -> true, count));
		assertEquals(2, notStarted[0]);

		BootstrapLauncher twoSlots = SynchronousLaunchers.parked(2, parked);
		assertTrue(twoSlots.launchPatientWarm("patient-4", () -> true, count));
		assertTrue("a slot is free, but this patient's warm is in flight", twoSlots.launchPatientWarm("patient-4", () -> true, count));
		assertEquals(3, notStarted[0]);

		BootstrapLauncher noToken = new BootstrapLauncher();
		assertFalse(noToken.launchPatientWarm("patient-3", () -> true, count));
		assertEquals("no daemon to start it", 4, notStarted[0]);
	}

	@Test
	public void launchPatientWarm_thatCannotBeHandedToAThread_runsOnNotStarted_andRethrows() {
		// r3 code N1.
		BootstrapLauncher launcher = SynchronousLaunchers.failingToStart(() -> 0L);
		int[] notStarted = { 0 };

		try {
			launcher.launchPatientWarm("patient-1", () -> true, () -> notStarted[0]++);
			fail("expected the executor's failure");
		}
		catch (IllegalStateException expected) {
			// pass
		}
		assertEquals(1, notStarted[0]);
		assertFalse(launcher.isWarming("patient-1"));
	}

	@Test
	public void syncProjections_andBackgroundWarms_shareOneBound() {
		List<Runnable> parked = new ArrayList<>();
		BootstrapLauncher launcher = SynchronousLaunchers.parked(1, parked);
		final int[] ran = { 0 };

		assertTrue("the one slot goes to a background warm", launcher.launchPatientWarm("patient-1", () -> true));
		assertFalse("a synchronous projection finds no slot in time", launcher.runWithProjectionSlot("patient-sync", () -> ++ran[0] > 0, 50));
		assertFalse("nor does a second warm", launcher.launchPatientWarm("patient-2", () -> true));
		assertEquals(0, ran[0]);

		parked.get(0).run();
		assertTrue("the warm released its slot", launcher.runWithProjectionSlot("patient-sync", () -> ++ran[0] > 0, 50));
		assertEquals(1, ran[0]);
		assertTrue("and the synchronous projection released it in turn", launcher.launchPatientWarm("patient-2", () -> true));
	}

	@Test
	public void runWithProjectionSlot_releasesTheSlotWhenTheWorkThrows() {
		List<Runnable> parked = new ArrayList<>();
		BootstrapLauncher launcher = SynchronousLaunchers.parked(1, parked);

		try {
			launcher.runWithProjectionSlot("patient-sync", () -> {
				throw new IllegalStateException("projection blew up");
			}, 50);
			fail("expected the work's exception");
		}
		catch (IllegalStateException expected) {
			// pass
		}
		assertTrue(launcher.launchPatientWarm("patient-1", () -> true));
	}

	@Test
	public void failedPatients_areBounded_oldestForgottenFirst() {
		BootstrapLauncher launcher = SynchronousLaunchers.create();
		launcher.setFailedChartDropper(uuid -> true);
		for (int i = 0; i <= BootstrapLauncher.MAX_FAILED_PATIENTS; i++) {
			launcher.launchPatientWarm("patient-" + i, () -> {
				throw new IllegalStateException("x");
			});
		}

		assertFalse("the first failure is forgotten past the bound", launcher.hasFailed("patient-0"));
		assertTrue(launcher.hasFailed("patient-" + BootstrapLauncher.MAX_FAILED_PATIENTS));
	}

	@Test
	public void pastTheBound_theEldestFailureIsForgottenOnlyOnceItsChartIsDropped_withTheMapUnlocked() throws Exception {
		// sec r1 S2, r2 S1. While the chart is being dropped the patient must still read failed, so
		// the entry stays in place until the dropper reports the chart gone.
		BootstrapLauncher launcher = SynchronousLaunchers.create();
		List<String> dropped = new ArrayList<>();
		final boolean[] stillFailedDuringDrop = { false };
		final boolean[] lockFree = { false };
		launcher.setFailedChartDropper(uuid -> {
			dropped.add(uuid);
			// Another thread reads the failure map: it would block if this thread held its lock.
			final boolean[] failed = { false };
			Thread reader = new Thread(() -> failed[0] = launcher.hasFailed(uuid));
			reader.start();
			try {
				reader.join(2000);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			lockFree[0] = !reader.isAlive();
			stillFailedDuringDrop[0] = failed[0];
			return true;
		});

		for (int i = 0; i <= BootstrapLauncher.MAX_FAILED_PATIENTS; i++) {
			failWarm(launcher, "patient-" + i);
		}
		failWarm(launcher, "patient-1");

		assertEquals("one dropped, the least recently failed; a repeat failure is not a new entry",
		    java.util.Collections.singletonList("patient-0"), dropped);
		assertFalse("forgotten once its chart is gone", launcher.hasFailed("patient-0"));
		assertTrue("the dropper ran with the failure map unlocked", lockFree[0]);
		assertTrue("and the patient still read failed while it ran", stillFailedDuringDrop[0]);
	}

	@Test
	public void aFailureWhoseChartCannotBeDropped_isKept_unchanged_andTheMapShrinksBackOnceDropsWork() {
		long[] now = { 1_000_000L };
		BootstrapLauncher launcher = SynchronousLaunchers.create(() -> now[0]);
		failWarm(launcher, "stuck");
		long backoff = launcher.millisUntilRetry("stuck");
		List<String> attempts = new ArrayList<>();
		final boolean[] works = { false };
		launcher.setFailedChartDropper(uuid -> {
			attempts.add(uuid);
			if ("stuck".equals(uuid) && !works[0]) {
				throw new IllegalStateException("delete failed");
			}
			return works[0];
		});

		for (int i = 0; i < BootstrapLauncher.MAX_FAILED_PATIENTS + 3; i++) {
			failWarm(launcher, "patient-" + i);
		}

		assertTrue("a chart that could not be dropped still reads failed", launcher.hasFailed("stuck"));
		assertEquals("and its failure is untouched: no extra failure counted", backoff, launcher.millisUntilRetry("stuck"));
		assertTrue("each new failure tried one drop and stopped at it", attempts.size() <= 4);

		works[0] = true;
		attempts.clear();
		failWarm(launcher, "patient-last");

		assertEquals("back to the bound: the four over it dropped", 5, attempts.size());
		int remembered = 0;
		for (String uuid : attempts) {
			remembered += launcher.hasFailed(uuid) ? 1 : 0;
		}
		assertEquals(0, remembered);
		assertTrue(launcher.hasFailed("patient-last"));
	}

	@Test
	public void aSecondProjectionForAPatientInFlight_waitsForIt_andDoesNotProjectAgain() throws Exception {
		// Every slot is busy, one of them with this patient's warm. The second projection must wait on
		// that warm, not queue for a slot: queued, it would get the warm's slot when the warm finished
		// and project the patient a second time.
		java.util.List<Runnable> parked = new java.util.ArrayList<>();
		BootstrapLauncher launcher = SynchronousLaunchers.parked(1, parked);
		assertTrue(launcher.launchPatientWarm("patient-1", () -> true));
		final boolean[] ran = { false };
		final boolean[] result = { false };
		Thread waiter = new Thread(() -> result[0] = launcher.runWithProjectionSlot("patient-1", () -> ran[0] = true, 5000));
		waiter.start();
		Thread.sleep(100);

		parked.get(0).run();
		waiter.join(5000);

		assertTrue("the waiter saw the in-flight projection finish", result[0]);
		assertFalse("and did not project again", ran[0]);
	}

	@Test
	public void aSecondProjectionForAPatientInFlight_leavesTheOtherSlotsFree() throws Exception {
		java.util.List<Runnable> parked = new java.util.ArrayList<>();
		BootstrapLauncher launcher = SynchronousLaunchers.parked(2, parked);
		assertTrue(launcher.launchPatientWarm("patient-1", () -> true));
		Thread waiter = new Thread(() -> launcher.runWithProjectionSlot("patient-1", () -> true, 5000));
		waiter.start();
		Thread.sleep(100);

		assertTrue("another patient can still start", launcher.launchPatientWarm("patient-2", () -> true));
		parked.get(0).run();
		waiter.join(5000);
	}

	@Test
	public void aWaiterGivesUp_whenTheProjectionInFlightOutlastsItsWait() {
		java.util.List<Runnable> parked = new java.util.ArrayList<>();
		BootstrapLauncher launcher = SynchronousLaunchers.parked(1, parked);
		assertTrue(launcher.launchPatientWarm("patient-1", () -> true));

		assertFalse(launcher.runWithProjectionSlot("patient-1", () -> true, 50));
	}

	@Test
	public void runExclusiveProjection_waitsForTheOneInFlight_thenRunsItsOwn() throws Exception {
		java.util.List<Runnable> parked = new java.util.ArrayList<>();
		BootstrapLauncher launcher = SynchronousLaunchers.parked(1, parked);
		assertTrue(launcher.launchPatientWarm("patient-1", () -> true));
		final boolean[] ran = { false };
		final boolean[] result = { false };
		Thread reindex = new Thread(() -> result[0] = launcher.runExclusiveProjection("patient-1", () -> ran[0] = true, 5000));
		reindex.start();
		Thread.sleep(100);
		assertFalse("not while the warm runs", ran[0]);

		parked.get(0).run();
		reindex.join(5000);

		assertTrue(result[0]);
		assertTrue("a forced reindex runs its own projection after the warm", ran[0]);
	}

	@Test
	public void aFailureIsClearedOnlyByAFullProjection() {
		BootstrapLauncher launcher = SynchronousLaunchers.create();
		try {
			launcher.runWithProjectionSlot("patient-1", () -> {
				throw new IncompleteProjectionException("Cold-touch projection for patient patient-1 was incomplete: 1 type(s) failed, 0 record(s) not projected");
			}, 50);
			fail("expected the partial projection to propagate");
		}
		catch (IncompleteProjectionException expected) {
			// pass
		}
		assertTrue("a synchronous failure is recorded too", launcher.hasFailed("patient-1"));

		launcher.runWithProjectionSlot("patient-1", () -> false, 50);
		assertTrue("a projection that found the chart there and skipped proves nothing", launcher.hasFailed("patient-1"));

		launcher.runWithProjectionSlot("patient-1", () -> true, 50);
		assertFalse("a full projection clears it", launcher.hasFailed("patient-1"));
	}

	@Test
	public void aFailedPatient_isDueForRetryOnlyAfterABackoffThatDoublesUpToACap() {
		long[] now = { 1_000_000L };
		BootstrapLauncher launcher = SynchronousLaunchers.create(() -> now[0]);
		long base = BootstrapLauncher.RETRY_BACKOFF_BASE_MILLIS;

		assertEquals("never failed, nothing to wait for", 0, launcher.millisUntilRetry("patient-1"));
		failWarm(launcher, "patient-1");
		assertEquals(base, launcher.millisUntilRetry("patient-1"));
		now[0] += base - 1;
		assertEquals(1, launcher.millisUntilRetry("patient-1"));
		now[0] += 1;
		assertEquals("due once the backoff has run out", 0, launcher.millisUntilRetry("patient-1"));

		failWarm(launcher, "patient-1");
		assertEquals("a second failure in a row waits twice as long", 2 * base, launcher.millisUntilRetry("patient-1"));

		for (int i = 0; i < 40; i++) {
			failWarm(launcher, "patient-1");
		}
		assertEquals("a poison chart backs off to the cap, not forever", BootstrapLauncher.MAX_RETRY_BACKOFF_MILLIS,
		    launcher.millisUntilRetry("patient-1"));
		assertTrue(launcher.hasFailed("patient-1"));

		assertTrue(launcher.launchPatientWarm("patient-1", () -> true));
		assertFalse("a full projection clears the failure", launcher.hasFailed("patient-1"));
		assertEquals(0, launcher.millisUntilRetry("patient-1"));
		failWarm(launcher, "patient-1");
		assertEquals("and the count starts again", base, launcher.millisUntilRetry("patient-1"));
	}

	private static void failWarm(BootstrapLauncher launcher, String patientUuid) {
		launcher.launchPatientWarm(patientUuid, () -> {
			throw new IllegalStateException("projection blew up");
		});
	}

	@Test
	public void projectionSlots_areClampedTo32() {
		java.util.List<Runnable> parked = new java.util.ArrayList<>();
		BootstrapLauncher launcher = SynchronousLaunchers.parked(1000, parked);
		int started = 0;
		for (int i = 0; i < 40; i++) {
			if (launcher.launchPatientWarm("patient-" + i, () -> true)) {
				started++;
			}
		}
		assertEquals(BootstrapLauncher.MAX_PROJECTION_SLOTS, started);
	}
}
