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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A read-only, serialization-friendly summary of {@link BootstrapProgress} across all resource
 * types, with a single derived {@link #isComplete()} flag answering "is this deployment fully
 * indexed?". The bootstrap backfill populates the read store one type at a time, ordered by
 * {@code dateChanged}, resuming from a persisted cursor; until every type reaches
 * {@link BootstrapStatus#COMPLETED}, reads can return stale/partial data and the lazy per-patient
 * projection cannot repair an already-partially-indexed patient. This report exists so operators
 * (and deploy pipelines that seed via SQL dump, which bypasses the live indexing bridge) can verify
 * completion instead of guessing. The derivation lives in the api layer so it is unit-testable and
 * reusable; the omod REST controller is a thin adapter over {@link #from(List)}.
 */
public class BootstrapStatusReport {

	private final boolean complete;

	private final List<TypeStatus> types;

	private BootstrapStatusReport(boolean complete, List<TypeStatus> types) {
		this.complete = complete;
		this.types = Collections.unmodifiableList(types);
	}

	/**
	 * Builds the report from raw progress rows. {@link #isComplete()} is true only when there is at
	 * least one tracked type AND every tracked type is {@link BootstrapStatus#COMPLETED} — an empty
	 * progress table (nothing indexed yet) or any RUNNING/FAILED/NOT_STARTED type makes it false.
	 */
	public static BootstrapStatusReport from(List<BootstrapProgress> progress) {
		List<TypeStatus> types = new ArrayList<TypeStatus>();
		boolean complete = progress != null && !progress.isEmpty();
		if (progress != null) {
			for (BootstrapProgress p : progress) {
				if (p.getStatus() != BootstrapStatus.COMPLETED) {
					complete = false;
				}
				types.add(new TypeStatus(p));
			}
		}
		return new BootstrapStatusReport(complete, types);
	}

	/** True only when every tracked resource type has finished — i.e. the deployment is fully indexed. */
	public boolean isComplete() {
		return complete;
	}

	public List<TypeStatus> getTypes() {
		return types;
	}

	/**
	 * Serialization-friendly view for the REST controller: {@code {complete, types:[{...}]}} with
	 * stable key order. Lives here (not in the controller) so the exact response keys are unit-tested
	 * — a typo'd key (e.g. {@code complete} → {@code completed}) silently breaks consumers that poll
	 * for completion, so the key contract must be covered by a test rather than hand-typed untested
	 * in the web layer.
	 */
	public Map<String, Object> toMap() {
		List<Map<String, Object>> typeMaps = new ArrayList<Map<String, Object>>(types.size());
		for (TypeStatus t : types) {
			Map<String, Object> m = new LinkedHashMap<String, Object>();
			m.put("resourceType", t.getResourceType());
			m.put("status", t.getStatus());
			m.put("documentsIndexed", t.getDocumentsIndexed());
			m.put("cursorDateChanged", t.getCursorDateChanged());
			m.put("startedAt", t.getStartedAt());
			m.put("completedAt", t.getCompletedAt());
			m.put("failureMessage", t.getFailureMessage());
			m.put("backend", t.getBackend());
			typeMaps.add(m);
		}
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		response.put("complete", complete);
		response.put("types", typeMaps);
		return response;
	}

	private static String iso(Instant instant) {
		return instant == null ? null : instant.toString();
	}

	/** Per-resource-type view of one {@link BootstrapProgress} row; timestamps as ISO-8601 strings. */
	public static class TypeStatus {

		private final String resourceType;

		private final String status;

		private final long documentsIndexed;

		private final String cursorDateChanged;

		private final String startedAt;

		private final String completedAt;

		private final String failureMessage;

		private final String backend;

		private TypeStatus(BootstrapProgress p) {
			this.resourceType = p.getResourceType();
			this.status = p.getStatus() == null ? null : p.getStatus().name();
			this.documentsIndexed = p.getDocumentsIndexed();
			this.cursorDateChanged = iso(p.getCursorDateChanged());
			this.startedAt = iso(p.getStartedAt());
			this.completedAt = iso(p.getCompletedAt());
			this.failureMessage = p.getFailureMessage();
			this.backend = p.getBackend();
		}

		public String getResourceType() {
			return resourceType;
		}

		public String getStatus() {
			return status;
		}

		public long getDocumentsIndexed() {
			return documentsIndexed;
		}

		public String getCursorDateChanged() {
			return cursorDateChanged;
		}

		public String getStartedAt() {
			return startedAt;
		}

		public String getCompletedAt() {
			return completedAt;
		}

		public String getFailureMessage() {
			return failureMessage;
		}

		public String getBackend() {
			return backend;
		}
	}
}
