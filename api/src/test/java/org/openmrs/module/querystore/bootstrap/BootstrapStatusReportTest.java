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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Unit tests for {@link BootstrapStatusReport#from(List)} — the derivation of the headline
 * "fully indexed?" flag and the serialized response shape. Pure: builds real
 * {@link BootstrapProgress} rows and asserts the report; no mocks, no context.
 */
public class BootstrapStatusReportTest {

	private static BootstrapProgress row(String type, BootstrapStatus status, long docs, String failure) {
		BootstrapProgress p = new BootstrapProgress(type);
		p.setStatus(status);
		p.setDocumentsIndexed(docs);
		p.setBackend("lucene");
		p.setStartedAt(Instant.parse("2026-05-30T19:35:00Z"));
		p.setCursorDateChanged(Instant.parse("2006-10-09T21:00:00Z"));
		if (status == BootstrapStatus.COMPLETED) {
			p.setCompletedAt(Instant.parse("2026-05-31T20:23:40Z"));
		}
		p.setFailureMessage(failure);
		return p;
	}

	@Test
	public void from_shouldReportCompleteWhenEveryTypeCompleted() {
		List<BootstrapProgress> rows = new ArrayList<BootstrapProgress>();
		rows.add(row("obs", BootstrapStatus.COMPLETED, 278396, null));
		rows.add(row("condition", BootstrapStatus.COMPLETED, 4451, null));

		BootstrapStatusReport report = BootstrapStatusReport.from(rows);

		assertTrue("all types COMPLETED => fully indexed", report.isComplete());
		assertEquals(2, report.getTypes().size());
		BootstrapStatusReport.TypeStatus obs = report.getTypes().get(0);
		assertEquals("obs", obs.getResourceType());
		assertEquals("COMPLETED", obs.getStatus());
		assertEquals(278396L, obs.getDocumentsIndexed());
		assertEquals("lucene", obs.getBackend());
		assertEquals("completedAt rendered as ISO-8601", "2026-05-31T20:23:40Z", obs.getCompletedAt());
		assertEquals("2006-10-09T21:00:00Z", obs.getCursorDateChanged());
	}

	@Test
	public void from_shouldReportIncompleteAndSurfaceFailureWhenATypeFailed() {
		List<BootstrapProgress> rows = new ArrayList<BootstrapProgress>();
		rows.add(row("obs", BootstrapStatus.COMPLETED, 278396, null));
		rows.add(row("diagnosis", BootstrapStatus.FAILED, 0,
				"FetchNotFoundException: Patient 49 does not exist"));

		BootstrapStatusReport report = BootstrapStatusReport.from(rows);

		assertFalse("a FAILED type => not fully indexed", report.isComplete());
		assertEquals(2, report.getTypes().size());
		BootstrapStatusReport.TypeStatus diagnosis = report.getTypes().get(1);
		assertEquals("FAILED", diagnosis.getStatus());
		assertTrue("failure message must be surfaced for operators",
				diagnosis.getFailureMessage().contains("FetchNotFoundException"));
		assertNull("a failed type has no completedAt", diagnosis.getCompletedAt());
	}

	@Test
	public void from_shouldReportIncompleteWhenATypeIsStillRunning() {
		List<BootstrapProgress> rows = new ArrayList<BootstrapProgress>();
		rows.add(row("condition", BootstrapStatus.COMPLETED, 4451, null));
		rows.add(row("obs", BootstrapStatus.RUNNING, 278396, null));

		assertFalse("a RUNNING type => backfill still in progress, not complete",
				BootstrapStatusReport.from(rows).isComplete());
	}

	@Test
	public void from_shouldReportIncompleteWhenNoRowsExist() {
		BootstrapStatusReport report = BootstrapStatusReport.from(new ArrayList<BootstrapProgress>());

		assertFalse("empty progress => nothing indexed yet, not complete", report.isComplete());
		assertEquals(0, report.getTypes().size());
	}

	@Test
	public void from_shouldTolerateNullInput() {
		BootstrapStatusReport report = BootstrapStatusReport.from(null);

		assertFalse(report.isComplete());
		assertEquals(Collections.emptyList(), report.getTypes());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void toMap_shouldRenderTheExactSerializedKeysAndValues() {
		// Pins the JSON contract consumers (e.g. a deploy pipeline polling `complete`) depend on.
		// A typo'd key here would silently break them, so the keys are asserted rather than
		// hand-typed untested in the web controller.
		List<BootstrapProgress> rows = new ArrayList<BootstrapProgress>();
		rows.add(row("obs", BootstrapStatus.RUNNING, 320795, null));
		rows.add(row("condition", BootstrapStatus.COMPLETED, 4451, null));

		Map<String, Object> map = BootstrapStatusReport.from(rows).toMap();

		assertEquals("headline completion flag key must be 'complete'", Boolean.FALSE, map.get("complete"));
		List<Map<String, Object>> types = (List<Map<String, Object>>) map.get("types");
		assertEquals(2, types.size());

		Map<String, Object> obs = types.get(0);
		assertEquals("obs", obs.get("resourceType"));
		assertEquals("RUNNING", obs.get("status"));
		assertEquals(Long.valueOf(320795L), obs.get("documentsIndexed"));
		assertEquals("2026-05-30T19:35:00Z", obs.get("startedAt"));
		assertEquals("2006-10-09T21:00:00Z", obs.get("cursorDateChanged"));
		assertNull(obs.get("completedAt"));
		assertNull(obs.get("failureMessage"));
		assertEquals("lucene", obs.get("backend"));
	}
}
