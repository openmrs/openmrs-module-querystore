/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.web.rest;

/**
 * A named type the caller may not search: not registered, or registered and not readable by the
 * caller. One exception and one answer (403) for both, so the answer does not say which.
 */
class TypeNotAvailableException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	TypeNotAvailableException() {
		super("Type not available");
	}
}
