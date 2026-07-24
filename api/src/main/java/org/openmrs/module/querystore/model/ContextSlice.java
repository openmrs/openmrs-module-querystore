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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A tier-tagged record selection over one patient's chart (ADR Decision 17), in the chart's
 * {@code record_date}-desc order. {@code chartTruncated} surfaces the Decision 15 backend cap
 * explicitly — a slice built on a capped chart is never silently presented as complete.
 */
public final class ContextSlice {

	private final List<ContextSliceRecord> records;

	private final int chartSize;

	private final boolean chartTruncated;

	private final Set<String> effectiveTypes;

	private final boolean temporalApplied;

	public ContextSlice(List<ContextSliceRecord> records, int chartSize, boolean chartTruncated) {
		this(records, chartSize, chartTruncated, Collections.<String> emptySet(), false);
	}

	public ContextSlice(List<ContextSliceRecord> records, int chartSize, boolean chartTruncated,
	        Set<String> effectiveTypes, boolean temporalApplied) {
		this.records = records == null ? Collections.<ContextSliceRecord> emptyList()
		        : Collections.unmodifiableList(records);
		this.chartSize = chartSize;
		this.chartTruncated = chartTruncated;
		this.effectiveTypes = effectiveTypes == null ? Collections.<String> emptySet()
		        : Collections.unmodifiableSet(new HashSet<String>(effectiveTypes));
		this.temporalApplied = temporalApplied;
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

	/** The typed-complete scope this slice was SELECTED with — caller types unioned with any
	 *  server-derived interpretation (ADR Decision 18); the selection trace for consumers. */
	public Set<String> getEffectiveTypes() {
		return effectiveTypes;
	}

	/** Whether the recency anchor applied to this slice (caller flag OR derived interpretation). */
	public boolean isTemporalApplied() {
		return temporalApplied;
	}
}
