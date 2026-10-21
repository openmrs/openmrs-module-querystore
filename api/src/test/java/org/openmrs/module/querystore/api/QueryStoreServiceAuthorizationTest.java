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

import java.util.Collections;

import org.junit.Test;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.querystore.model.ContextSliceRequest;
import org.openmrs.test.BaseModuleContextSensitiveTest;

/**
 * Verifies that the {@code @Authorized(GET_PATIENTS)} annotations on {@link QueryStoreService} are
 * actually enforced at runtime — i.e. that the externally-resolved service is registered behind an
 * authorization-advised proxy (moduleApplicationContext.xml).
 *
 * <p>Regression guard for a real gap: {@code ServiceContext.setService} wraps a <em>bare</em> service
 * bean in an advice-free proxy, so registering the raw {@code QueryStoreServiceImpl} left every
 * {@code @Authorized} annotation decorative — a caller without {@code GET_PATIENTS} could read
 * patient-scoped data. The fix registers the service behind a {@code ProxyFactoryBean} carrying the
 * core {@code authorizationInterceptor}. With the bare-bean wiring these tests fail (no exception is
 * thrown); with the proxy they pass.
 *
 * <p>The authorization advice runs before the method body, so enforcement is asserted independently
 * of whether an index backend is wired in the test context.
 */
public class QueryStoreServiceAuthorizationTest extends BaseModuleContextSensitiveTest {

	@Test(expected = APIAuthenticationException.class)
	public void getPatientChart_shouldEnforceGetPatientsPrivilege() {
		dropPrivileges();
		Context.getService(QueryStoreService.class)
		        .getPatientChart("00000000-0000-0000-0000-000000000000");
	}

	@Test(expected = APIAuthenticationException.class)
	public void searchByPatient_shouldEnforceGetPatientsPrivilege() {
		dropPrivileges();
		Context.getService(QueryStoreService.class)
		        .searchByPatient("00000000-0000-0000-0000-000000000000", "fever", 10);
	}

	@Test(expected = APIAuthenticationException.class)
	public void search_shouldEnforceGetPatientsPrivilege() {
		dropPrivileges();
		Context.getService(QueryStoreService.class).search("fever", 10);
	}

	@Test(expected = APIAuthenticationException.class)
	public void getContextSlice_shouldEnforceGetPatientsPrivilege() {
		dropPrivileges();
		Context.getService(QueryStoreService.class).getContextSlice(
		        "00000000-0000-0000-0000-000000000000", "fever",
		        new ContextSliceRequest(Collections.<String> emptySet(), false));
	}

	@Test(expected = APIAuthenticationException.class)
	public void getPatientChartRead_shouldEnforceGetPatientsPrivilege() {
		dropPrivileges();
		Context.getService(QueryStoreService.class)
		        .getPatientChartRead("00000000-0000-0000-0000-000000000000");
	}

	/**
	 * BaseModuleContextSensitiveTest authenticates as a super-user in setUp; drop that so the
	 * {@code @Authorized(GET_PATIENTS)} gate is exercised by a caller that lacks the privilege.
	 */
	private void dropPrivileges() {
		Context.logout();
	}
}
