/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.api;

/**
 * How far a patient's running projection has got, in resource types: {@code done} of {@code total}
 * have been projected (or have failed and been passed over). Counts types, never records, so it
 * tells a caller nothing about the chart's contents, only how soon to look again.
 */
public final class ProjectionProgress {

	private final int done;

	private final int total;

	public ProjectionProgress(int done, int total) {
		this.done = done;
		this.total = total;
	}

	public int getDone() {
		return done;
	}

	public int getTotal() {
		return total;
	}
}
