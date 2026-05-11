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

import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Test;

public class BridgeExecutorTest {

	private BridgeExecutor executor;

	@After
	public void tearDown() {
		if (executor != null) {
			executor.stop();
		}
	}

	@Test
	public void submit_runsTaskOnPool() throws InterruptedException {
		executor = new BridgeExecutor(1);
		executor.start();
		CountDownLatch ran = new CountDownLatch(1);
		executor.submit(ran::countDown);
		assertTrue("task ran within timeout", ran.await(2, TimeUnit.SECONDS));
	}

	@Test
	public void submit_afterStop_isDroppedNotThrown() {
		// Dropping is fine: the bootstrap re-projection path catches up missed writes per ADR
		// Decision 12, and silently dropping in shutdown beats raising into the clinical thread.
		executor = new BridgeExecutor(1);
		executor.start();
		executor.stop();
		executor.submit(() -> {
			throw new AssertionError("should not have run");
		});
	}
}
