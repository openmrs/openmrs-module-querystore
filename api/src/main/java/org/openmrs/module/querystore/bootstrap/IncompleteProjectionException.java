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

/**
 * A per-patient projection ({@link BootstrapService#ensureIndexed} or
 * {@link BootstrapService#reindexPatient}) attempted every type but left records out. Whatever did
 * project stays indexed. The message names the patient and how many types and records failed, and
 * carries no record content, so a caller may show it.
 */
public class IncompleteProjectionException extends IllegalStateException {

	private static final long serialVersionUID = 1L;

	public IncompleteProjectionException(String message) {
		super(message);
	}
}
