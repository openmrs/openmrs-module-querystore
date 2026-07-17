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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.querystore.api.QueryStoreService;
import org.openmrs.module.querystore.model.QueryDocument;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * POJO tests for the {@code /querystore/patientrecord} read endpoint (ADR Decision 16): dispatch across
 * the three read methods, the embedding-exclusion + paging response shape, and the 400/404/privilege
 * guards. Like {@link QueryStoreRestControllerTest}, the controller is instantiated directly with its
 * services stubbed (the omod has no DB-backed context-test harness); HTTP routing is verified against a
 * live server.
 */
public class PatientRecordEndpointTest {

	private static final String PATIENT = "patient-uuid";

	private final QueryStoreRestController controller = new QueryStoreRestController();

	private final QueryStoreService queryStore = mock(QueryStoreService.class);

	private final PatientService patients = mock(PatientService.class);

	private void wire() {
		controller.setQueryStoreService(queryStore);
		controller.setPatientService(patients);
	}

	@After
	public void clearContext() {
		Context.clearUserContext();
	}

	@Test
	public void fullChart_returnsAllRecordsPagedWithTrueTotal_andNoEmbedding() {
		authenticate();
		wire();
		when(patients.getPatientByUuid(PATIENT)).thenReturn(new Patient());
		when(queryStore.getPatientChart(PATIENT)).thenReturn(Arrays.asList(
		        doc("obs", "r1", LocalDate.of(2026, 1, 15), "Fasting blood glucose: 11.2 mmol/L"),
		        doc("condition", "r2", LocalDate.of(2025, 12, 1), "Condition: Type 2 Diabetes. Status: ACTIVE")));

		ResponseEntity<Object> response = controller.getPatientRecords(PATIENT, null, null, null);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		Map<?, ?> body = body(response);
		assertEquals("a full chart carries the true total", Integer.valueOf(2), body.get("totalCount"));
		List<?> results = (List<?>) body.get("results");
		assertEquals(2, results.size());
		Map<?, ?> first = (Map<?, ?>) results.get(0);
		assertEquals("obs", first.get("resourceType"));
		assertEquals("r1", first.get("resourceUuid"));
		assertEquals("2026-01-15", first.get("date"));
		assertEquals("Fasting blood glucose: 11.2 mmol/L", first.get("text"));
		assertTrue("metadata passes through", ((Map<?, ?>) first.get("metadata")).containsKey("obs_group_uuid"));
		assertFalse("the embedding vector must never be exposed", first.containsKey("embedding"));
		assertFalse("full-chart rows carry no rank", first.containsKey("rank"));
		verify(queryStore).getPatientChart(PATIENT);
		verify(queryStore, never()).searchByPatient(anyString(), anyString(), anyInt());
	}

	@Test
	public void patientAndQuery_dispatchesToRankedSearch_withRankAndNullTotal() {
		authenticate();
		wire();
		when(patients.getPatientByUuid(PATIENT)).thenReturn(new Patient());
		when(queryStore.searchByPatient(PATIENT, "glucose", 50)).thenReturn(Arrays.asList(
		        doc("obs", "r1", LocalDate.of(2026, 1, 15), "Fasting blood glucose: 11.2 mmol/L"),
		        doc("obs", "r2", LocalDate.of(2025, 6, 1), "Random glucose: 9.0 mmol/L")));

		ResponseEntity<Object> response = controller.getPatientRecords(PATIENT, "glucose", null, null);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		Map<?, ?> body = body(response);
		assertNull("a ranked top-K window has no browseable total", body.get("totalCount"));
		List<?> results = (List<?>) body.get("results");
		assertEquals(Integer.valueOf(1), ((Map<?, ?>) results.get(0)).get("rank"));
		assertEquals(Integer.valueOf(2), ((Map<?, ?>) results.get(1)).get("rank"));
	}

	@Test
	public void queryOnly_dispatchesToCrossPatientSearch() {
		authenticate();
		wire();
		when(queryStore.search("glucose", 50)).thenReturn(Arrays.asList(
		        doc("obs", "r1", LocalDate.of(2026, 1, 15), "Fasting blood glucose: 11.2 mmol/L")));

		ResponseEntity<Object> response = controller.getPatientRecords(null, "glucose", null, null);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(1, ((List<?>) body(response).get("results")).size());
		verify(patients, never()).getPatientByUuid(anyString());
	}

	@Test
	public void fullChart_pagesWithStartIndexAndEmitsPrevNextLinks() {
		authenticate();
		wire();
		when(patients.getPatientByUuid(PATIENT)).thenReturn(new Patient());
		List<QueryDocument> five = new ArrayList<QueryDocument>();
		for (int i = 0; i < 5; i++) {
			five.add(doc("obs", "r" + i, LocalDate.of(2026, 1, 1), "rec " + i));
		}
		when(queryStore.getPatientChart(PATIENT)).thenReturn(five);

		ResponseEntity<Object> response = controller.getPatientRecords(PATIENT, null, 2, 2); // limit=2, startIndex=2

		Map<?, ?> body = body(response);
		assertEquals(Integer.valueOf(5), body.get("totalCount"));
		List<?> results = (List<?>) body.get("results");
		assertEquals("the middle page holds records 2 and 3", 2, results.size());
		assertEquals("r2", ((Map<?, ?>) results.get(0)).get("resourceUuid"));
		List<?> links = (List<?>) body.get("links");
		assertNotNull("a middle page carries prev + next links", links);
		assertEquals(2, links.size());
	}

	@Test
	public void returns400_whenNeitherPatientNorQuery() {
		authenticate();
		wire();
		ResponseEntity<Object> response = controller.getPatientRecords(null, null, null, null);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test
	public void returns400_whenLimitNonPositive() {
		authenticate();
		wire();
		ResponseEntity<Object> response = controller.getPatientRecords(PATIENT, null, 0, null);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		verify(patients, never()).getPatientByUuid(anyString());
	}

	@Test
	public void returns404_whenPatientUnknown() {
		authenticate();
		wire();
		when(patients.getPatientByUuid("ghost")).thenReturn(null);
		ResponseEntity<Object> response = controller.getPatientRecords("ghost", null, null, null);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		// a bogus patient must not reach the read store (avoids a wasted cold-touch projection)
		verify(queryStore, never()).getPatientChart(anyString());
	}

	@Test(expected = ContextAuthenticationException.class)
	public void requiresGetPatientsPrivilege() {
		// The endpoint reads patient data; a caller lacking Get Patients must be rejected up front.
		Context.setUserContext(new UserContext(null) {

			@Override
			public boolean hasPrivilege(String privilege) {
				return false;
			}
		});
		controller.getPatientRecords(PATIENT, null, null, null);
	}

	private static QueryDocument doc(String type, String uuid, LocalDate date, String text) {
		QueryDocument d = new QueryDocument();
		d.setResourceType(type);
		d.setResourceUuid(uuid);
		d.setDate(date);
		d.setText(text);
		d.setEmbedding(new float[] { 0.1f, 0.2f, 0.3f }); // present so the exclusion assertion is meaningful
		d.putMetadata("obs_group_uuid", "grp-" + uuid);
		return d;
	}

	private static Map<?, ?> body(ResponseEntity<Object> response) {
		return (Map<?, ?>) response.getBody();
	}

	/** Authenticates the thread with an all-privileges user so the up-front requirePrivilege gate passes. */
	private static void authenticate() {
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
				return true;
			}
		});
	}
}
