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

import java.util.List;
import java.util.function.LongSupplier;

import org.openmrs.module.DaemonToken;

/**
 * A real {@link BootstrapLauncher} that runs each task on the calling thread, for tests outside this
 * package (the executor seam is package-private). Warm bookkeeping (in-flight, failed) is the
 * production code's, not a fake's.
 */
public final class SynchronousLaunchers {

	private SynchronousLaunchers() {
	}

	/**
	 * A real launcher, holding at most {@code slots} projections, whose background warms are parked:
	 * handed to the executor and never run, so each holds its slot until the test runs it from
	 * {@code parked}.
	 */
	public static BootstrapLauncher parked(int slots, List<Runnable> parked) {
		BootstrapLauncher launcher = new BootstrapLauncher();
		launcher.setMaxConcurrentProjections(slots);
		launcher.setDaemonExecutor((task, token) -> parked.add(task));
		launcher.setDaemonToken(new DaemonToken("token-parked"));
		return launcher;
	}

	public static BootstrapLauncher create() {
		BootstrapLauncher launcher = new BootstrapLauncher();
		launcher.setDaemonExecutor((task, token) -> task.run());
		launcher.setDaemonToken(new DaemonToken("token-synchronous"));
		return launcher;
	}

	/** {@link #create()}, reading time (epoch millis) from {@code clock}, so a test can step past a retry backoff. */
	public static BootstrapLauncher create(LongSupplier clock) {
		BootstrapLauncher launcher = create();
		launcher.setClock(clock);
		return launcher;
	}
}
