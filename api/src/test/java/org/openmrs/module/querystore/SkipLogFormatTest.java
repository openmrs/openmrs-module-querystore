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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Pins the {@link SkipLogFormat} line shape. Operators (and downstream log shippers) grep on
 * {@code [querystore-skip]} and parse the key=value tail; a refactor that changes the prefix or
 * field order would silently break those workflows without crashing any test. This class is the
 * contract.
 *
 * <p>Assertion style: tests that pin the entire emitted line use exact-string
 * {@code assertEquals} so any field-order or punctuation drift fails; tests that exercise one
 * field's behavior (null-handling, sanitization) use {@code assertTrue(line.contains(...))} so
 * the assertion is targeted to the field under test. Both styles are intentional — keep new
 * tests in whichever style matches the contract being pinned.
 */
public class SkipLogFormatTest {

	@Test
	public void format_emitsGrepTagAndKeyValueFields() {
		String line = SkipLogFormat.format("bootstrap", "condition", "abc-uuid",
		        Boolean.TRUE, "Connection refused");

		// Tag is the load-bearing token operators match on; it must be the literal prefix.
		assertTrue("line starts with the grep tag", line.startsWith("[querystore-skip] "));
		// Fields are key=value pairs in a fixed order so a grok pattern can parse them.
		assertEquals("[querystore-skip] layer=bootstrap type=condition uuid=abc-uuid "
		        + "retryable=true reason=\"Connection refused\"",
		        line);
	}

	@Test
	public void format_unknownRetryableWhenSourceIsNull() {
		// The RuntimeException catch path in TypeBootstrapper has no DocFailure, so the retryable
		// hint is genuinely unknown. The format must surface that rather than misrepresenting it
		// as either true or false.
		String line = SkipLogFormat.format("bootstrap", "obs", "u-1", null, "NPE somewhere");
		assertTrue("retryable rendered as unknown", line.contains("retryable=unknown"));
	}

	@Test
	public void format_renderingNullReasonStillProducesParseableField() {
		// A backend that returned a failed WriteResult with no DocFailure (allowed by the WriteResult
		// API) must still produce a complete reason= field so a grok pattern doesn't choke on the
		// missing value at end-of-line.
		String line = SkipLogFormat.format("sync", "diagnosis", "u-2", Boolean.FALSE, null);
		assertEquals("[querystore-skip] layer=sync type=diagnosis uuid=u-2 "
		        + "retryable=false reason=\"no failure detail\"",
		        line);
	}

	@Test
	public void format_sanitizesReasonNewlinesAndQuotes() {
		// A backend's e.getMessage() that embeds quotes or newlines would either close the reason=
		// field early or split the log entry across multiple lines (log4j only stamps the timestamp
		// once per entry). Either case bypasses the operator's [querystore-skip] grep on the second
		// line and on. The sanitizer keeps every emitted skip line single-line and quote-balanced.
		String line = SkipLogFormat.format("bootstrap", "obs", "u-3", Boolean.TRUE,
		        "I/O error: \"foo\"\nstack frame line");
		assertTrue("no embedded newlines that would split the log entry",
		        line.indexOf('\n') == -1 && line.indexOf('\r') == -1);
		// The reason value's own quotes are downgraded to apostrophes; the wrapping quotes stay
		// matched.
		assertEquals("[querystore-skip] layer=bootstrap type=obs uuid=u-3 "
		        + "retryable=true reason=\"I/O error: 'foo' stack frame line\"",
		        line);
	}

	@Test
	public void skipTagConstantMatchesEmittedPrefix() {
		// Lock the public constant to the literal prefix so external callers (admin tooling,
		// log-grep scripts that import the constant rather than hardcode the string) can rely on it.
		assertTrue(SkipLogFormat.format("bootstrap", "obs", "u-x", null, "x")
		        .startsWith(SkipLogFormat.LOG_SKIP_TAG));
	}

	@Test
	public void format_docFailureOverloadProducesIdenticalLineToExplicitArgs() {
		// The DocFailure overload exists to collapse the WriteResult-failed call sites; if it ever
		// produced a different line shape than the explicit-args version for the same inputs, the
		// two write paths (bootstrap and sync) would emit subtly different formats and operators'
		// grok rules would break asymmetrically.
		org.openmrs.module.querystore.backend.DocFailure f =
		        new org.openmrs.module.querystore.backend.DocFailure(
		                "condition", "abc-uuid", "Connection refused", true);
		assertEquals(SkipLogFormat.format("bootstrap", "condition", "abc-uuid",
		                Boolean.TRUE, "Connection refused"),
		        SkipLogFormat.format("bootstrap", f));
	}

	@Test
	public void format_nullDocFailureRendersUnknownsRatherThanThrowing() {
		// WriteResult.failed(null) is theoretically possible per the API. The overload must produce
		// a parseable line (every field present) rather than NPE the caller. Exact-string assertion
		// per the class-style note above — this pins the entire degenerate line shape, not a single
		// field's behavior.
		assertEquals("[querystore-skip] layer=sync type=null uuid=null "
		        + "retryable=unknown reason=\"no failure detail\"",
		        SkipLogFormat.format("sync", (org.openmrs.module.querystore.backend.DocFailure) null));
	}
}
