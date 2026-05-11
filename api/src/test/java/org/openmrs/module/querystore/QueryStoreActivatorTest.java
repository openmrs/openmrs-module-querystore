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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.AdministrationService;

public class QueryStoreActivatorTest {

	private QueryStoreActivator activator;

	private AdministrationService admin;

	@Before
	public void setUp() {
		activator = new QueryStoreActivator();
		admin = mock(AdministrationService.class);
	}

	@Test
	public void isAutostartEnabled_returnsTrueWhenGpIsTrue() {
		when(admin.getGlobalPropertyValue(eq(QueryStoreConstants.GP_BOOTSTRAP_AUTOSTART), eq(Boolean.FALSE)))
		        .thenReturn(Boolean.TRUE);
		assertTrue(activator.isAutostartEnabled(admin));
	}

	@Test
	public void isAutostartEnabled_returnsFalseWhenGpIsFalse() {
		when(admin.getGlobalPropertyValue(eq(QueryStoreConstants.GP_BOOTSTRAP_AUTOSTART), eq(Boolean.FALSE)))
		        .thenReturn(Boolean.FALSE);
		assertFalse(activator.isAutostartEnabled(admin));
	}

	@Test
	public void isAutostartEnabled_returnsFalseWhenGpIsAbsent() {
		// getGlobalPropertyValue returns the default (FALSE) when the property doesn't exist;
		// pin that contract so a future change to a non-Boolean default doesn't silently flip
		// the autostart semantics.
		when(admin.getGlobalPropertyValue(eq(QueryStoreConstants.GP_BOOTSTRAP_AUTOSTART), eq(Boolean.FALSE)))
		        .thenReturn(Boolean.FALSE);
		assertFalse(activator.isAutostartEnabled(admin));
	}
}
