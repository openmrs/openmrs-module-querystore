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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.querystore.api.IndexStatus;
import org.openmrs.module.querystore.api.QueryStoreService;
import org.openmrs.module.querystore.bootstrap.BootstrapService;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * The patient endpoints through Spring's real request dispatch (MockMvc), not by calling handler
 * methods: each status here comes from the controller's {@code @ExceptionHandler} mapping, so a
 * handler that loses its annotation fails these tests rather than letting the framework answer
 * with a stack trace.
 */
public class QueryStoreRestDispatchTest {

	private static final String PATIENT = "dd75c020-1691-11df-97a5-7038c432aabf";

	private QueryStoreService queryStore;

	private MockMvc mvc;

	@Before
	public void setUp() {
		QueryStoreRestController controller = new QueryStoreRestController();
		PatientService patients = mock(PatientService.class);
		when(patients.getPatientByUuid(PATIENT)).thenReturn(new Patient());
		controller.setPatientService(patients);
		BootstrapService bootstrap = mock(BootstrapService.class);
		when(bootstrap.getResourceTypeNames()).thenReturn(Arrays.asList("obs", "condition"));
		controller.setBootstrapService(bootstrap);
		queryStore = mock(QueryStoreService.class);
		controller.setQueryStoreService(queryStore);
		mvc = MockMvcBuilders.standaloneSetup(controller).setMessageConverters(new MappingJackson2HttpMessageConverter())
		        .build();
	}

	@After
	public void clearContext() {
		Context.clearUserContext();
	}

	@Test
	public void aCallerWithoutGetPatients_gets403() throws Exception {
		signIn(false);

		mvc.perform(get("/rest/v1/querystore/patient/" + PATIENT + "/status")).andExpect(status().isForbidden())
		        .andExpect(bodyField("error", "Insufficient privileges"));
	}

	@Test
	public void getObservationsWithoutGetPatients_gets403_onSearchRecentAndWarm() throws Exception {
		// The omnibar shows its query store group to Get Observations, but the server keeps Get
		// Patients: the patient check is core's own getPatientByUuid, which requires it, and without
		// that check search and warm would tell a caller whether a uuid is a patient's chart.
		signInWith(org.openmrs.util.PrivilegeConstants.GET_OBS);

		mvc.perform(get("/rest/v1/querystore/patient/" + PATIENT + "/search").param("q", "cd4"))
		        .andExpect(status().isForbidden());
		mvc.perform(get("/rest/v1/querystore/patient/" + PATIENT + "/recent")).andExpect(status().isForbidden());
		mvc.perform(post("/rest/v1/querystore/patient/" + PATIENT + "/warm")).andExpect(status().isForbidden());
		org.mockito.Mockito.verifyNoInteractions(queryStore);
	}

	@Test
	public void aBadParameter_gets400() throws Exception {
		signIn(true);

		mvc.perform(get("/rest/v1/querystore/patient/" + PATIENT + "/search").param("q", "cd4").param("limit", "0"))
		        .andExpect(status().isBadRequest()).andExpect(bodyField("error", "limit must be between 1 and 50"));
	}

	@Test
	public void anUnknownPatient_gets404() throws Exception {
		signIn(true);

		mvc.perform(get("/rest/v1/querystore/patient/00000000-0000-4000-8000-000000000000/status"))
		        .andExpect(status().isNotFound()).andExpect(bodyField("error", "Patient not found"));
	}

	@Test
	public void aWarmThatCannotStart_gets503WithRetryAfter() throws Exception {
		signIn(true);
		when(queryStore.warm(PATIENT)).thenReturn(IndexStatus.NOT_INDEXED);

		mvc.perform(post("/rest/v1/querystore/patient/" + PATIENT + "/warm")).andExpect(status().isServiceUnavailable())
		        .andExpect(header().string("Retry-After", "5")).andExpect(bodyField("status", "not_indexed"));
	}

	/** The JSON body's top-level {@code field} equals {@code expected}. */
	private static org.springframework.test.web.servlet.ResultMatcher bodyField(String field, String expected) {
		return result -> org.junit.Assert.assertEquals(expected,
		    new com.fasterxml.jackson.databind.ObjectMapper().readTree(result.getResponse().getContentAsString()).path(field)
		            .asText());
	}

	private static void signIn(boolean everyPrivilege) {
		signIn(privilege -> everyPrivilege);
	}

	private static void signInWith(String... privileges) {
		signIn(Arrays.asList(privileges)::contains);
	}

	private static void signIn(java.util.function.Predicate<String> has) {
		Context.setUserContext(new UserContext(null) {

			@Override
			public User getAuthenticatedUser() {
				return new User();
			}

			@Override
			public boolean isAuthenticated() {
				return true;
			}

			@Override
			public boolean hasPrivilege(String privilege) {
				return has.test(privilege);
			}
		});
	}
}
