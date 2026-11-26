/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SafeLogTest {

	private static final String PATIENT = "0c1d2e3f-4a5b-4c6d-8e7f-901234567890";

	@Test
	public void warn_keepsTheExceptionsTypesAndScrubbedMessages_andPutsTheStackAtDebug() {
		RuntimeException failure = new IllegalStateException("projection failed",
		        new IllegalArgumentException("No row with the given identifier exists for patient " + PATIENT));

		try (LogCapture capture = LogCapture.of(SafeLogTest.class)) {
			SafeLog.warn(SafeLogTest.class, "Per-patient projection of obs failed", failure);

			assertEquals(1, capture.at("WARN").size());
			String warn = capture.at("WARN").get(0);
			assertFalse(warn, warn.contains(PATIENT));
			assertFalse("no stack at WARN", warn.endsWith(LogCapture.THROWN));
			assertTrue(warn, warn.startsWith("Per-patient projection of obs failed [java.lang.IllegalStateException: "
			        + "projection failed; caused by java.lang.IllegalArgumentException: No row with the given "
			        + "identifier exists for patient <uuid> (stack at DEBUG)]"));
			assertTrue("the full throwable is at DEBUG",
			    capture.at("DEBUG").contains("Per-patient projection of obs failed" + LogCapture.THROWN));
		}
	}

	@Test
	public void lines_areAttributedToTheCaller_notToSafeLog() {
		// OpenMRS's log pattern prints the location (%C.%M(%L)); every WARN must not read "SafeLog".
		try (LogCapture capture = LogCapture.of(SafeLogTest.class)) {
			SafeLog.warn(SafeLogTest.class, "failed", new RuntimeException("x"));

			assertEquals(2, capture.sources().size());
			for (String source : capture.sources()) {
				assertEquals(SafeLogTest.class.getName(), source);
			}
		}
	}

	@Test
	public void error_isScrubbedTheSameWay() {
		try (LogCapture capture = LogCapture.of(SafeLogTest.class)) {
			SafeLog.error(SafeLogTest.class, "Unexpected error", new RuntimeException("bad uuid " + PATIENT));

			assertEquals("Unexpected error [java.lang.RuntimeException: bad uuid <uuid> (stack at DEBUG)]",
			    capture.at("ERROR").get(0));
		}
	}

	@Test
	public void describe_handlesAMissingMessageAndASelfCausingThrowable() {
		assertEquals("", SafeLog.describe(null));
		assertEquals(" [java.lang.RuntimeException (stack at DEBUG)]", SafeLog.describe(new RuntimeException()));
	}

	@Test
	public void theCallersMessage_isScrubbedOfUuidsAndLineBreaks_atWarn_andKeptAsWrittenAtDebug() {
		try (LogCapture capture = LogCapture.of(SafeLogTest.class)) {
			SafeLog.warn(SafeLogTest.class, "Bootstrap failed at cursor " + PATIENT + "\nFORGED ENTRY", new RuntimeException("x"));

			String warn = capture.at("WARN").get(0);
			assertFalse(warn, warn.contains(PATIENT));
			assertFalse("one line, no forged second entry", warn.contains("\n"));
			assertTrue(warn, warn.startsWith("Bootstrap failed at cursor <uuid> FORGED ENTRY ["));
			assertTrue("DEBUG keeps the message as written",
			    capture.at("DEBUG").get(0).startsWith("Bootstrap failed at cursor " + PATIENT));
		}
	}
}
