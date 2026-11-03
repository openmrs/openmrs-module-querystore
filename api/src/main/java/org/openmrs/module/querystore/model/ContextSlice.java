/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A tier-tagged record selection over one patient's chart (ADR Decision 17), in the chart's
 * {@code record_date}-desc order. {@code chartTruncated} is true when a documented cap, a handled
 * backend read failure, or a failed cold touch may have omitted records, so a slice built on an
 * incomplete chart is never silently presented as complete.
 */
public final class ContextSlice {

	private final List<ContextSliceRecord> records;

	private final int chartSize;

	private final boolean chartTruncated;

	private final boolean projectionComplete;

	private final Set<String> effectiveTypes;

	private final boolean temporalApplied;

	private final String chartSnapshotId;

	private final Set<String> withheldTypes;

	public ContextSlice(List<ContextSliceRecord> records, int chartSize, boolean chartTruncated) {
		this(records, chartSize, chartTruncated, false, Collections.<String> emptySet(), false, null);
	}

	public ContextSlice(List<ContextSliceRecord> records, int chartSize, boolean chartTruncated,
	        boolean projectionComplete, Set<String> effectiveTypes, boolean temporalApplied,
	        String chartSnapshotId) {
		this(records, chartSize, chartTruncated, projectionComplete, effectiveTypes, temporalApplied,
		        chartSnapshotId, Collections.<String> emptySet());
	}

	public ContextSlice(List<ContextSliceRecord> records, int chartSize, boolean chartTruncated,
	        boolean projectionComplete, Set<String> effectiveTypes, boolean temporalApplied,
	        String chartSnapshotId, Set<String> withheldTypes) {
		this.withheldTypes = withheldTypes == null ? Collections.<String> emptySet()
		        : Collections.unmodifiableSet(new HashSet<String>(withheldTypes));
		this.records = records == null ? Collections.<ContextSliceRecord> emptyList()
		        : Collections.unmodifiableList(new ArrayList<ContextSliceRecord>(records));
		this.chartSize = chartSize;
		this.chartTruncated = chartTruncated;
		this.projectionComplete = projectionComplete;
		this.effectiveTypes = effectiveTypes == null ? Collections.<String> emptySet()
		        : Collections.unmodifiableSet(new HashSet<String>(effectiveTypes));
		this.temporalApplied = temporalApplied;
		this.chartSnapshotId = chartSnapshotId;
	}

	public List<ContextSliceRecord> getRecords() {
		return records;
	}

	/** The full chart's document count the slice was selected from. */
	public int getChartSize() {
		return chartSize;
	}

	public boolean isChartTruncated() {
		return chartTruncated;
	}

	/** Whether every configured source resource type was fully projected before selection. */
	public boolean isProjectionComplete() {
		return projectionComplete;
	}

	/** The typed-complete scope this slice was SELECTED with — caller types unioned with any
	 *  server-derived interpretation (ADR Decision 18); the selection trace for consumers. */
	public Set<String> getEffectiveTypes() {
		return effectiveTypes;
	}

	/** Every core type the caller holds no core read privilege for, requested or not. They are left
	 *  out of {@link #getEffectiveTypes()} and of the mandatory tier, so a consumer does not take the
	 *  empty result for a patient with no such records (Decision 14 point 4). */
	public Set<String> getWithheldTypes() {
		return withheldTypes;
	}

	/** Whether the recency anchor applied to this slice (caller flag OR derived interpretation). */
	public boolean isTemporalApplied() {
		return temporalApplied;
	}

	/** Fingerprint of the complete chart materialization from which this slice was selected. */
	public String getChartSnapshotId() {
		return chartSnapshotId;
	}
}
