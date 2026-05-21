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

/**
 * Per-type bootstrap state persisted to {@code querystore_bootstrap_progress}. The
 * {@code cursorDateChanged}/{@code cursorUuid} pair is the resume point: the next page is "records
 * whose effective dateChanged is strictly after the cursor, ordered ascending, tie-broken by UUID."
 * Persisted after each page so an interrupted run resumes without re-projecting.
 *
 * <p>{@link #getBackend() backend} records the {@link
 * org.openmrs.module.querystore.backend.BackendStore} this row belongs to — the value of
 * {@code querystore.backend} in effect when the row was last written (which is also where any
 * non-null cursor points). The dispatcher compares it with the currently-resolved backend and
 * resets the row when they disagree, so a GP flip from one backend to another doesn't inherit a
 * "completed" cursor pointing into a different backend's empty index. After a reset, the backend
 * is stamped with the new value while the cursor is null — i.e., "this row will track the new
 * backend from the start." Null on rows written before this field existed; treated as a mismatch
 * on first post-upgrade run.
 */
public class BootstrapProgress {

	private String resourceType;

	private BootstrapStatus status = BootstrapStatus.NOT_STARTED;

	private Instant cursorDateChanged;

	private String cursorUuid;

	private long documentsIndexed;

	private Instant startedAt;

	private Instant completedAt;

	private String failureMessage;

	private String backend;

	public BootstrapProgress() {
	}

	public BootstrapProgress(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public BootstrapStatus getStatus() {
		return status;
	}

	public void setStatus(BootstrapStatus status) {
		this.status = status;
	}

	public Instant getCursorDateChanged() {
		return cursorDateChanged;
	}

	public void setCursorDateChanged(Instant cursorDateChanged) {
		this.cursorDateChanged = cursorDateChanged;
	}

	public String getCursorUuid() {
		return cursorUuid;
	}

	public void setCursorUuid(String cursorUuid) {
		this.cursorUuid = cursorUuid;
	}

	public long getDocumentsIndexed() {
		return documentsIndexed;
	}

	public void setDocumentsIndexed(long documentsIndexed) {
		this.documentsIndexed = documentsIndexed;
	}

	public Instant getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(Instant startedAt) {
		this.startedAt = startedAt;
	}

	public Instant getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(Instant completedAt) {
		this.completedAt = completedAt;
	}

	public String getFailureMessage() {
		return failureMessage;
	}

	public void setFailureMessage(String failureMessage) {
		this.failureMessage = failureMessage;
	}

	public String getBackend() {
		return backend;
	}

	public void setBackend(String backend) {
		this.backend = backend;
	}
}
