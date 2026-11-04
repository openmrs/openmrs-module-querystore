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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.querystore.api.IndexStatus;
import org.openmrs.module.querystore.api.QueryResult;
import org.openmrs.module.querystore.api.QuerySpec;
import org.openmrs.module.querystore.api.QueryStoreService;
import org.openmrs.module.querystore.bootstrap.BootstrapLauncher;
import org.openmrs.module.querystore.bootstrap.BootstrapService;
import org.openmrs.module.querystore.bootstrap.DriftReport;
import org.openmrs.module.querystore.bootstrap.IncompleteProjectionException;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Guards the controller's {@code @ExceptionHandler} mapping: a thrown exception must surface as a
 * proper status with a clean {@code {"error": ...}} body, never the framework default of HTTP 200
 * carrying a serialized stack trace.
 *
 * <p>The handlers use no autowired state, so the controller is instantiated directly — no Spring
 * context is loaded. {@code handleAuthFailure} branches on {@link Context#isAuthenticated()}, which
 * reads the thread-bound {@link UserContext}; the tests set one directly to exercise both branches.
 * The HTTP routing that dispatches each exception type to its handler is verified end-to-end against
 * a live server.
 */
public class QueryStoreRestControllerTest {

	private final QueryStoreRestController controller = new QueryStoreRestController();

	/**
	 * The patient endpoints first ask core for the patient. Every uuid but {@link #UNKNOWN_PATIENT} is
	 * one core returns, and the registered types are obs and condition; tests that need otherwise
	 * inject their own.
	 */
	@Before
	public void knownPatientsAndTypes() {
		PatientService patients = mock(PatientService.class);
		when(patients.getPatientByUuid(org.mockito.ArgumentMatchers.anyString())).thenReturn(new Patient());
		when(patients.getPatientByUuid(UNKNOWN_PATIENT)).thenReturn(null);
		Patient voided = new Patient();
		voided.setVoided(true);
		when(patients.getPatientByUuid(VOIDED_PATIENT)).thenReturn(voided);
		controller.setPatientService(patients);
		BootstrapService bootstrap = mock(BootstrapService.class);
		when(bootstrap.getResourceTypeNames()).thenReturn(Arrays.asList("obs", "condition"));
		controller.setBootstrapService(bootstrap);
	}

	private static final String UNKNOWN_PATIENT = "no-such-patient";

	/** Core's getPatientByUuid returns voided patients too. */
	private static final String VOIDED_PATIENT = "voided-patient";

	@After
	public void clearContext() {
		Context.clearUserContext();
	}

	@Test
	public void handleAuthFailure_shouldReturn401WhenUnauthenticated() {
		Context.setUserContext(new UserContext(null)); // no authenticated user => not authenticated
		ResponseEntity<Object> response = controller.handleAuthFailure(new APIAuthenticationException("denied"));
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		assertEquals("Authentication required", errorOf(response));
	}

	@Test
	public void handleAuthFailure_shouldReturn403WhenAuthenticated() {
		// An authenticated caller that was denied a privilege => 403, not 401.
		Context.setUserContext(new UserContext(null) {

			@Override
			public User getAuthenticatedUser() {
				return new User();
			}
		});
		ResponseEntity<Object> response = controller.handleAuthFailure(new APIAuthenticationException("denied"));
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
		assertEquals("Insufficient privileges", errorOf(response));
	}

	@Test
	public void handleMalformedBody_shouldReturn400() {
		ResponseEntity<Object> response = controller.handleMalformedBody(
		        new HttpMessageNotReadableException("bad json"));
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("Malformed request body", errorOf(response));
	}

	@Test
	public void handleMalformedQueryParameter_shouldReturn400() {
		ResponseEntity<Object> response = controller.handleBadParameter(
		        new TypeMismatchException("abc", Integer.class));
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("Malformed request parameter", errorOf(response));
	}

	@Test
	public void malformedTypedParameters_shouldRouteThroughTheHttpExceptionHandler() throws Exception {
		MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();
		String[][] malformed = {
		        { "limit", "abc" },
		        { "startIndex", "1e9" },
		        { "temporal", "yesplease" },
		        { "interpret", "yesplease" }
		};
		for (String[] parameter : malformed) {
			mvc.perform(get("/rest/v1/querystore/patientrecord")
			        .param("patient", "patient-uuid")
			        .param(parameter[0], parameter[1]))
			        .andExpect(status().isBadRequest())
			        .andExpect(content().string("{\"error\":\"Malformed request parameter\"}"));
		}
	}

	@Test
	public void handleUnexpected_shouldReturnCleanInternalServerError() {
		ResponseEntity<Object> response = controller.handleUnexpected(new RuntimeException("sensitive detail"));
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		Map<?, ?> body = (Map<?, ?>) response.getBody();
		assertNotNull(body);
		// Exactly {"error":"Internal error"} — size 1 proves no stack trace / detail leaked.
		assertEquals(1, body.size());
		assertEquals("Internal error", body.get("error"));
	}

	@Test
	public void reindex_launchesBootstrapAndReturns202_whenScopeAll() {
		authenticate();
		RecordingLauncher launcher = new RecordingLauncher(true);
		controller.setBootstrapLauncher(launcher);

		ResponseEntity<Object> response = controller.reindex(Collections.singletonMap("scope", "all"));

		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
		assertEquals("scope:all must trigger exactly one global bootstrap launch", 1, launcher.launches);
		assertEquals(Boolean.TRUE, ((Map<?, ?>) response.getBody()).get("accepted"));
	}

	@Test
	public void contextOnlyParameters_requireContextModeThroughHttp() throws Exception {
		authenticate();
		QueryStoreService queryStore = mock(QueryStoreService.class);
		when(queryStore.search("medications", 50)).thenReturn(Collections.<QueryDocument>emptyList());
		controller.setQueryStoreService(queryStore);
		controller.setMaximumPageSize(Integer.valueOf(100));
		MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();
		String[][] parameters = { { "types", "drug_order" }, { "temporal", "false" }, { "interpret", "true" } };
		for (String[] parameter : parameters) {
			mvc.perform(get("/rest/v1/querystore/patientrecord")
			        .param("q", "medications").param(parameter[0], parameter[1]))
			        .andExpect(status().isBadRequest())
			        .andExpect(content().string("{\"error\":\"types, temporal and interpret require mode=context\"}"));
		}
	}

	@Test
	public void reindex_returns503_whenScopeAllButDaemonTokenUnavailable() {
		authenticate();
		// launchAsync returns false when no daemon token is wired — the server cannot start the
		// async backfill, so the caller must see a 503, not a misleading 202.
		RecordingLauncher launcher = new RecordingLauncher(false);
		controller.setBootstrapLauncher(launcher);

		ResponseEntity<Object> response = controller.reindex(Collections.singletonMap("scope", "all"));

		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
		assertEquals(1, launcher.launches);
		assertNotNull(errorOf(response));
	}

	@Test
	public void reindex_returns400_whenNeitherPatientNorScopeProvided() {
		authenticate();
		// Absence of both must stay a clean rejection — it must NOT silently become a global
		// reindex (that is exactly the accidental-trigger footgun scope:"all" guards against).
		ResponseEntity<Object> response = controller.reindex(Collections.<String, String> emptyMap());
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(errorOf(response));
	}

	@Test
	public void reindex_returns400_whenScopeUnknown() {
		authenticate();
		// An unrecognised scope is a client error, never a fall-through to per-patient or global.
		ResponseEntity<Object> response = controller.reindex(Collections.singletonMap("scope", "everything"));
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(errorOf(response));
	}

	@Test(expected = ContextAuthenticationException.class)
	public void reindex_requiresManageGlobalPropertiesPrivilege() {
		// The endpoint gates an expensive, destructive operation; a caller lacking the privilege must
		// be rejected up front, before any reindex is launched. Pins that the requirePrivilege gate
		// fires — without this, deleting the gate would let any caller trigger a global reindex.
		Context.setUserContext(new UserContext(null) {

			@Override
			public boolean hasPrivilege(String privilege) {
				return false;
			}
		});
		controller.reindex(Collections.singletonMap("scope", "all"));
	}

	@Test
	public void reindex_returns400_whenBothPatientAndScopeAllProvided() {
		authenticate();
		// Ambiguous intent must be rejected, not silently resolved to the expensive global reindex.
		RecordingLauncher launcher = new RecordingLauncher(true);
		controller.setBootstrapLauncher(launcher);
		Map<String, String> body = new HashMap<String, String>();
		body.put("scope", "all");
		body.put("patient", "patient-uuid");

		ResponseEntity<Object> response = controller.reindex(body);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("an ambiguous patient+scope request must not launch a global reindex", 0, launcher.launches);
	}

	@Test
	public void reindex_returns400_whenScopeAllWithResourceType() {
		authenticate();
		// scope:"all" + resourceType is ambiguous (global rebuild vs per-type re-sync) and must be
		// rejected, not silently resolved to the expensive global scan with resourceType ignored.
		RecordingLauncher launcher = new RecordingLauncher(true);
		controller.setBootstrapLauncher(launcher);
		Map<String, String> body = new HashMap<String, String>();
		body.put("scope", "all");
		body.put("resourceType", "obs");

		ResponseEntity<Object> response = controller.reindex(body);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("an ambiguous all+resourceType request must not launch a global reindex", 0, launcher.launches);
	}

	@Test
	public void reindex_acceptsMixedCaseScopeAll() {
		authenticate();
		// A hand-typed "ALL" expresses the same intent; it must not be rejected as an unknown scope.
		RecordingLauncher launcher = new RecordingLauncher(true);
		controller.setBootstrapLauncher(launcher);

		ResponseEntity<Object> response = controller.reindex(Collections.singletonMap("scope", "ALL"));

		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
		assertEquals(1, launcher.launches);
	}

	@Test
	public void reindex_treatsBlankScopeAsAbsent_andDoesNotLaunch() {
		authenticate();
		// A blank/whitespace scope is not a global-reindex request; with no patient it is a 400,
		// and crucially it must not launch a bootstrap.
		RecordingLauncher launcher = new RecordingLauncher(true);
		controller.setBootstrapLauncher(launcher);
		ResponseEntity<Object> response = controller.reindex(Collections.singletonMap("scope", "   "));
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("a blank scope must not launch a global reindex", 0, launcher.launches);
	}

	@Test
	public void indexingStatus_aRegisteredTypeThatNeverStartedIsNotComplete() {
		authenticate();
		BootstrapService bootstrap = mock(BootstrapService.class);
		org.openmrs.module.querystore.bootstrap.BootstrapProgress obs =
		        new org.openmrs.module.querystore.bootstrap.BootstrapProgress("obs");
		obs.setStatus(org.openmrs.module.querystore.bootstrap.BootstrapStatus.COMPLETED);
		when(bootstrap.getStatusReport()).thenReturn(org.openmrs.module.querystore.bootstrap.BootstrapStatusReport
		        .from(Collections.singletonList(obs), Arrays.asList("obs", "condition"), null));
		controller.setBootstrapService(bootstrap);

		Map<?, ?> body = (Map<?, ?>) controller.getIndexingStatus().getBody();

		assertEquals("condition has no row yet, so indexing is not complete", Boolean.FALSE, body.get("complete"));
	}

	@Test
	public void reindex_withPatient_reprojectsThatPatientAndReturnsTheDocumentCount() {
		authenticate();
		// Adapter contract: a per-patient body reprojects that one patient (not a global launch) and
		// reports the resulting chart size. The reproject round-trip itself is covered by
		// BootstrapServiceImplTest; here we pin the controller's routing + response shaping with the
		// services stubbed, since omod has no DB-backed context-test harness.
		BootstrapService bootstrap = mock(BootstrapService.class);
		QueryStoreService queryStore = mock(QueryStoreService.class);
		when(queryStore.getPatientChart("patient-uuid"))
		        .thenReturn(Arrays.asList(new QueryDocument(), new QueryDocument(), new QueryDocument()));
		when(queryStore.reindex("patient-uuid")).thenReturn(IndexStatus.INDEXED);
		controller.setBootstrapService(bootstrap);
		controller.setQueryStoreService(queryStore);
		// Inject a launcher too, only to prove the per-patient path never touches it (routing isolation).
		RecordingLauncher launcher = new RecordingLauncher(true);
		controller.setBootstrapLauncher(launcher);

		ResponseEntity<Object> response = controller.reindex(Collections.singletonMap("patient", "patient-uuid"));

		assertEquals(HttpStatus.OK, response.getStatusCode());
		Map<?, ?> body = (Map<?, ?>) response.getBody();
		assertEquals("patient-uuid", body.get("patient"));
		assertEquals("documentsIndexed must report the reprojected chart's size", Integer.valueOf(3),
		    body.get("documentsIndexed"));
		assertEquals("indexed", body.get("status"));
		verify(queryStore).reindex("patient-uuid");
		org.mockito.Mockito.verify(bootstrap, org.mockito.Mockito.never()).reindexPatient("patient-uuid");
		assertEquals("a per-patient reindex must not also launch a global bootstrap", 0, launcher.launches);
	}

	@Test
	public void reindex_withPatient_isA503_whenNoProjectionSlotIsFree() {
		authenticate();
		QueryStoreService queryStore = mock(QueryStoreService.class);
		when(queryStore.reindex("patient-uuid")).thenReturn(IndexStatus.NOT_INDEXED);
		controller.setQueryStoreService(queryStore);

		ResponseEntity<Object> response = controller.reindex(Collections.singletonMap("patient", "patient-uuid"));

		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
		assertNotNull(response.getHeaders().getFirst("Retry-After"));
		org.mockito.Mockito.verify(queryStore, org.mockito.Mockito.never()).getPatientChart("patient-uuid");
	}

	@Test
	public void aTypeNoCorePrivilegeReads_isDeniedByDefault_andRepeatsAreDropped() {
		authenticate();
		BootstrapService bootstrap = mock(BootstrapService.class);
		when(bootstrap.getResourceTypeNames()).thenReturn(Arrays.asList("obs", "condition", "mymodule_note"));
		controller.setBootstrapService(bootstrap);
		QueryStoreService queryStore = mock(QueryStoreService.class);
		when(queryStore.query(org.mockito.ArgumentMatchers.any(QuerySpec.class)))
		        .thenReturn(QueryResult.empty(IndexStatus.INDEXED, QuerySpec.forPatient("p").query("x").build()));
		controller.setQueryStoreService(queryStore);

		controller.search("patient-uuid", "cd4", null, null, null, null, null, null, null, true);
		controller.search("patient-uuid", "cd4", null, "obs,obs, obs", null, null, null, null, null, true);
		ArgumentCaptor<QuerySpec> captor = ArgumentCaptor.forClass(QuerySpec.class);
		verify(queryStore, org.mockito.Mockito.times(2)).query(captor.capture());
		assertEquals("a contributed type with no read privilege is left out, even for a caller with every privilege",
		    Arrays.asList("obs", "condition"), captor.getAllValues().get(0).getTypes());
		assertEquals(Collections.singletonList("obs"), captor.getAllValues().get(1).getTypes());

		assertTypeNotAvailable(
		    () -> controller.search("patient-uuid", "cd4", null, "mymodule_note", null, null, null, null, null, true));
	}

	@Test
	public void aCallerWhoCanReadNoType_getsA403_notASearchOfEveryType() {
		Context.setUserContext(new UserContext(null) {

			@Override
			public User getAuthenticatedUser() {
				return new User();
			}

			@Override
			public boolean hasPrivilege(String privilege) {
				return PrivilegeConstants.GET_PATIENTS.equals(privilege);
			}
		});
		QueryStoreService queryStore = mock(QueryStoreService.class);
		controller.setQueryStoreService(queryStore);

		try {
			controller.search("patient-uuid", "cd4", null, null, null, null, null, null, null, true);
			throw new AssertionError("expected a 403");
		}
		catch (ContextAuthenticationException expected) {
			// pass: the registered types here are obs and condition, neither readable with Get Patients
		}
		org.mockito.Mockito.verifyNoInteractions(queryStore);
	}

	@Test
	public void reindex_withPatient_reportsAReprojectionThatLeftRecordsOut() {
		authenticate();
		QueryStoreService queryStore = mock(QueryStoreService.class);
		when(queryStore.reindex("patient-uuid")).thenReturn(IndexStatus.FAILED);
		when(queryStore.getPatientChart("patient-uuid")).thenReturn(Arrays.asList(new QueryDocument()));
		controller.setQueryStoreService(queryStore);

		ResponseEntity<Object> response = controller.reindex(Collections.singletonMap("patient", "patient-uuid"));

		assertEquals("a partial re-projection is not a success", HttpStatus.INTERNAL_SERVER_ERROR,
		    response.getStatusCode());
		Map<?, ?> body = (Map<?, ?>) response.getBody();
		assertEquals("patient-uuid", body.get("patient"));
		assertEquals("what did project is still counted", Integer.valueOf(1), body.get("documentsIndexed"));
		assertEquals(IndexStatus.FAILED.wireName(), body.get("status"));
		assertEquals("Re-projection left records out; see the server log", body.get("error"));
	}

	@Test
	public void reindex_launchesTypeResyncAndReturns202_whenScopeType() {
		authenticate();
		// scope:"type" + a known resourceType launches exactly one per-type re-sync (async, 202) and
		// never touches the global launch path. Echoes the resourceType in the body.
		BootstrapService bootstrap = mock(BootstrapService.class);
		when(bootstrap.getResourceTypeNames()).thenReturn(Arrays.asList("obs", "condition"));
		controller.setBootstrapService(bootstrap);
		RecordingLauncher launcher = new RecordingLauncher(true);
		controller.setBootstrapLauncher(launcher);
		Map<String, String> body = new HashMap<String, String>();
		body.put("scope", "type");
		body.put("resourceType", "obs");

		ResponseEntity<Object> response = controller.reindex(body);

		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
		assertEquals("scope:type must launch exactly one per-type re-sync", 1, launcher.resyncLaunches);
		assertEquals("obs", launcher.resyncedType);
		assertEquals("obs", ((Map<?, ?>) response.getBody()).get("resourceType"));
		assertEquals("a per-type re-sync must not launch a global bootstrap", 0, launcher.launches);
	}

	@Test
	public void reindex_returns400_whenScopeTypeMissingResourceType() {
		authenticate();
		RecordingLauncher launcher = new RecordingLauncher(true);
		controller.setBootstrapLauncher(launcher);

		ResponseEntity<Object> response = controller.reindex(Collections.singletonMap("scope", "type"));

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("scope:type with no resourceType must not launch anything", 0, launcher.resyncLaunches);
	}

	@Test
	public void reindex_returns400_whenScopeTypeUnknownResourceType() {
		authenticate();
		// A typo'd type is rejected synchronously here (validated against getResourceTypeNames) rather
		// than 202-then-fail on the daemon thread.
		BootstrapService bootstrap = mock(BootstrapService.class);
		when(bootstrap.getResourceTypeNames()).thenReturn(Arrays.asList("obs", "condition"));
		controller.setBootstrapService(bootstrap);
		RecordingLauncher launcher = new RecordingLauncher(true);
		controller.setBootstrapLauncher(launcher);
		Map<String, String> body = new HashMap<String, String>();
		body.put("scope", "type");
		body.put("resourceType", "nonsuch");

		ResponseEntity<Object> response = controller.reindex(body);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("an unknown resourceType must not launch a re-sync", 0, launcher.resyncLaunches);
	}

	@Test
	public void reindex_returns503_whenScopeTypeButDaemonTokenUnavailable() {
		authenticate();
		// Validation passes, but launchResyncAsync returns false (no daemon token) — 503, not a
		// misleading 202.
		BootstrapService bootstrap = mock(BootstrapService.class);
		when(bootstrap.getResourceTypeNames()).thenReturn(Arrays.asList("obs"));
		controller.setBootstrapService(bootstrap);
		RecordingLauncher launcher = new RecordingLauncher(false);
		controller.setBootstrapLauncher(launcher);
		Map<String, String> body = new HashMap<String, String>();
		body.put("scope", "type");
		body.put("resourceType", "obs");

		ResponseEntity<Object> response = controller.reindex(body);

		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
		assertEquals("validation passed, so a launch was attempted", 1, launcher.resyncLaunches);
	}

	@Test
	public void reindex_returns400_whenBothPatientAndScopeType() {
		authenticate();
		// Ambiguous intent (per-patient vs per-type) must be rejected, never silently resolved.
		RecordingLauncher launcher = new RecordingLauncher(true);
		controller.setBootstrapLauncher(launcher);
		Map<String, String> body = new HashMap<String, String>();
		body.put("scope", "type");
		body.put("resourceType", "obs");
		body.put("patient", "patient-uuid");

		ResponseEntity<Object> response = controller.reindex(body);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("an ambiguous patient+type request must not launch a re-sync", 0, launcher.resyncLaunches);
	}

	@Test
	public void getDrift_returnsTheReportAsAMapWith200() {
		authenticate();
		// Adapter contract: the controller is a thin pass-through over the service's DriftReport.toMap()
		// — it adds no shaping of its own. The drift derivation itself is covered by DriftReportTest and
		// BootstrapServiceImplTest; here we pin routing + the 200 + the body being exactly toMap().
		BootstrapService bootstrap = mock(BootstrapService.class);
		DriftReport report = new DriftReport(Collections.singletonList(
		        new DriftReport.TypeDrift("obs", 100, 90)));
		when(bootstrap.getDrift()).thenReturn(report);
		controller.setBootstrapService(bootstrap);

		ResponseEntity<Object> response = controller.getDrift();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("the body is exactly the report's serialization view", report.toMap(), response.getBody());
		verify(bootstrap).getDrift();
	}

	@Test(expected = ContextAuthenticationException.class)
	public void getDrift_requiresManageGlobalPropertiesPrivilege() {
		// Drift detection runs a COUNT per type across core + backend; it is gated behind the same admin
		// privilege the reindex endpoints use. Pins that the requirePrivilege gate fires up front —
		// without this, deleting the gate would expose the scan to any caller.
		Context.setUserContext(new UserContext(null) {

			@Override
			public boolean hasPrivilege(String privilege) {
				return false;
			}
		});
		controller.getDrift();
	}

	/** Authenticates the thread with a user that holds every privilege, so the controller's
	 *  up-front {@code Context.requirePrivilege} gate passes and the routing logic under test runs. */
	// ---------- patient-scoped consumer endpoints ----------

	@Test
	public void search_mapsEveryParameterOntoTheSpec_andReturnsTheResultMap() {
		authenticate();
		QueryStoreService queryStore = mock(QueryStoreService.class);
		when(queryStore.query(org.mockito.ArgumentMatchers.any(QuerySpec.class)))
		        .thenReturn(QueryResult.empty(IndexStatus.INDEXED, QuerySpec.forPatient("p").query("x").build()));
		controller.setQueryStoreService(queryStore);

		ResponseEntity<Object> response = controller.search("patient-uuid", "cd4", "KEYWORD", "obs, condition", "recent",
		    "concept", "resourceType", 5, 10, false);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("indexed", ((Map<?, ?>) response.getBody()).get("status"));
		ArgumentCaptor<QuerySpec> captor = ArgumentCaptor.forClass(QuerySpec.class);
		verify(queryStore).query(captor.capture());
		QuerySpec spec = captor.getValue();
		assertEquals("patient-uuid", spec.getPatientUuid());
		assertEquals("cd4", spec.getQuery());
		assertEquals(QuerySpec.Mode.KEYWORD, spec.getMode());
		assertEquals(Arrays.asList("obs", "condition"), spec.getTypes());
		assertEquals(QuerySpec.Sort.RECENT, spec.getSort());
		assertEquals(QuerySpec.Collapse.CONCEPT, spec.getCollapse());
		assertEquals(Collections.singleton(QuerySpec.Facet.RESOURCE_TYPE), spec.getFacets());
		assertEquals(5, spec.getLimit());
		assertEquals(10, spec.getOffset());
		assertEquals(false, spec.isWait());
	}

	@Test
	public void search_defaultsToHybridRelevanceUncollapsedAndWaiting() {
		authenticate();
		QueryStoreService queryStore = mock(QueryStoreService.class);
		when(queryStore.query(org.mockito.ArgumentMatchers.any(QuerySpec.class)))
		        .thenReturn(QueryResult.empty(IndexStatus.INDEXED, QuerySpec.forPatient("p").query("x").build()));
		controller.setQueryStoreService(queryStore);

		controller.search("patient-uuid", "cd4", null, null, null, null, null, null, null, true);

		ArgumentCaptor<QuerySpec> captor = ArgumentCaptor.forClass(QuerySpec.class);
		verify(queryStore).query(captor.capture());
		QuerySpec spec = captor.getValue();
		assertEquals(QuerySpec.Mode.HYBRID, spec.getMode());
		assertEquals(QuerySpec.Sort.RELEVANCE, spec.getSort());
		assertEquals(QuerySpec.Collapse.NONE, spec.getCollapse());
		assertEquals(QuerySpec.DEFAULT_LIMIT, spec.getLimit());
		assertEquals(true, spec.isWait());
		assertEquals(Collections.emptyList(), spec.getTypes());
	}

	@Test
	public void search_rejectsBadParametersWith400_beforeTouchingTheService() {
		authenticate();
		QueryStoreService queryStore = mock(QueryStoreService.class);
		controller.setQueryStoreService(queryStore);

		String[] errors = {
		        badRequest(() -> controller.search("p", " ", null, null, null, null, null, null, null, true)),
		        badRequest(() -> controller.search("p", "cd4", "fuzzy", null, null, null, null, null, null, true)),
		        badRequest(() -> controller.search("p", "cd4", null, null, "oldest", null, null, null, null, true)),
		        badRequest(() -> controller.search("p", "cd4", null, null, null, "drug", null, null, null, true)),
		        badRequest(() -> controller.search("p", "cd4", null, null, null, null, "concept", null, null, true)),
		        badRequest(() -> controller.search("p", "cd4", null, null, null, null, null, 51, null, true)),
		        badRequest(() -> controller.search("p", "cd4", null, null, null, null, null, 0, null, true)),
		        badRequest(() -> controller.search("p", "cd4", null, null, null, null, null, null, -1, true)) };

		for (String error : errors) {
			assertNotNull(error);
		}
		org.mockito.Mockito.verifyNoInteractions(queryStore);
	}

	@Test
	public void recent_isANonBlockingNewestFirstBrowseCappedAtMaxPage() {
		authenticate();
		QueryStoreService queryStore = mock(QueryStoreService.class);
		when(queryStore.query(org.mockito.ArgumentMatchers.any(QuerySpec.class)))
		        .thenReturn(QueryResult.empty(IndexStatus.PROJECTING, QuerySpec.forPatient("p").query("x").build()));
		controller.setQueryStoreService(queryStore);

		ResponseEntity<Object> response = controller.recent("patient-uuid", null, "obs", false);

		assertEquals("projecting", ((Map<?, ?>) response.getBody()).get("status"));
		assertEquals(Collections.emptyList(), ((Map<?, ?>) response.getBody()).get("hits"));
		ArgumentCaptor<QuerySpec> captor = ArgumentCaptor.forClass(QuerySpec.class);
		verify(queryStore).query(captor.capture());
		QuerySpec spec = captor.getValue();
		assertEquals(null, spec.getQuery());
		assertEquals(QuerySpec.Sort.RECENT, spec.getSort());
		assertEquals(5, spec.getLimit());
		assertEquals(Collections.singletonList("obs"), spec.getTypes());
		assertEquals(false, spec.isWait());
		assertNotNull("no way to ask recent for the whole chart",
		    badRequest(() -> controller.recent("patient-uuid", QueryStoreRestController.MAX_PAGE + 1, null, false)));
	}

	@Test
	public void warm_returns202WithTheStatus_andStatusReturns200() {
		authenticate();
		QueryStoreService queryStore = mock(QueryStoreService.class);
		when(queryStore.warm("patient-uuid")).thenReturn(IndexStatus.PROJECTING);
		when(queryStore.getIndexStatus("patient-uuid")).thenReturn(IndexStatus.NOT_INDEXED);
		controller.setQueryStoreService(queryStore);

		ResponseEntity<Object> warm = controller.warm("patient-uuid");
		ResponseEntity<Object> status = controller.status("patient-uuid");

		assertEquals(HttpStatus.ACCEPTED, warm.getStatusCode());
		assertEquals("projecting", ((Map<?, ?>) warm.getBody()).get("status"));
		assertEquals(HttpStatus.OK, status.getStatusCode());
		assertEquals("not_indexed", ((Map<?, ?>) status.getBody()).get("status"));
	}

	@Test
	public void warm_onAnIndexedPatient_returns200_sinceNothingWasStarted() {
		authenticate();
		QueryStoreService queryStore = mock(QueryStoreService.class);
		when(queryStore.warm("patient-uuid")).thenReturn(IndexStatus.INDEXED);
		controller.setQueryStoreService(queryStore);

		ResponseEntity<Object> warm = controller.warm("patient-uuid");

		assertEquals(HttpStatus.OK, warm.getStatusCode());
		assertEquals("indexed", ((Map<?, ?>) warm.getBody()).get("status"));
	}

	@Test(expected = ContextAuthenticationException.class)
	public void patientEndpoints_requireGetPatients() {
		Context.setUserContext(new UserContext(null) {

			@Override
			public boolean hasPrivilege(String privilege) {
				return false;
			}
		});
		controller.search("patient-uuid", "cd4", null, null, null, null, null, null, null, true);
	}

	@Test
	public void patientEndpoints_return404ForAnUnknownOrVoidedPatient_withoutTouchingTheService() {
		authenticate();
		QueryStoreService queryStore = mock(QueryStoreService.class);
		controller.setQueryStoreService(queryStore);

		Runnable[] calls = {
		        () -> controller.warm(UNKNOWN_PATIENT),
		        () -> controller.status(UNKNOWN_PATIENT),
		        () -> controller.recent(UNKNOWN_PATIENT, null, null, false),
		        () -> controller.search(UNKNOWN_PATIENT, "cd4", null, null, null, null, null, null, null, true),
		        () -> controller.warm(VOIDED_PATIENT),
		        () -> controller.search(VOIDED_PATIENT, "cd4", null, null, null, null, null, null, null, true) };
		for (Runnable call : calls) {
			try {
				call.run();
				throw new AssertionError("expected a 404");
			}
			catch (PatientNotFoundException e) {
				ResponseEntity<Object> response = controller.handlePatientNotFound(e);
				assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
				assertEquals("Patient not found", errorOf(response));
			}
		}
		org.mockito.Mockito.verifyNoInteractions(queryStore);
	}

	@Test
	public void unknownAndUnreadableNamedTypes_getTheSame403_andAnOverlongQueryIsA400() {
		// Get Patients and Get Observations only. condition is registered but unreadable, mymodule_note
		// is registered with no read privilege, faketype is not registered: the caller must not be able
		// to tell which, so all three get one status and one body that does not echo the name.
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
				return PrivilegeConstants.GET_PATIENTS.equals(privilege) || PrivilegeConstants.GET_OBS.equals(privilege);
			}
		});
		BootstrapService bootstrap = mock(BootstrapService.class);
		when(bootstrap.getResourceTypeNames()).thenReturn(Arrays.asList("obs", "condition", "mymodule_note"));
		controller.setBootstrapService(bootstrap);
		QueryStoreService queryStore = mock(QueryStoreService.class);
		controller.setQueryStoreService(queryStore);
		StringBuilder longQuery = new StringBuilder();
		for (int i = 0; i <= QueryStoreRestController.MAX_QUERY_LENGTH; i++) {
			longQuery.append('a');
		}

		for (String type : new String[] { "faketype", "condition", "mymodule_note" }) {
			assertTypeNotAvailable(() -> controller.search("p", "cd4", null, "obs," + type, null, null, null, null, null, true));
			assertTypeNotAvailable(() -> controller.recent("p", null, type, false));
		}
		assertEquals("q must be at most 500 characters",
		    badRequest(() -> controller.search("p", longQuery.toString(), null, null, null, null, null, null, null, true)));
		org.mockito.Mockito.verifyNoInteractions(queryStore);
	}

	@Test
	public void search_searchesOnlyTypesTheCallerCanReadThroughCore() {
		// Get Patients and Get Observations, but not Get Conditions: an unnamed condition type is left
		// out of the search, and naming it is a 403.
		Context.setUserContext(new UserContext(null) {

			@Override
			public User getAuthenticatedUser() {
				return new User();
			}

			@Override
			public boolean hasPrivilege(String privilege) {
				return PrivilegeConstants.GET_PATIENTS.equals(privilege) || PrivilegeConstants.GET_OBS.equals(privilege);
			}
		});
		QueryStoreService queryStore = mock(QueryStoreService.class);
		when(queryStore.query(org.mockito.ArgumentMatchers.any(QuerySpec.class)))
		        .thenReturn(QueryResult.empty(IndexStatus.INDEXED, QuerySpec.forPatient("p").query("x").build()));
		controller.setQueryStoreService(queryStore);

		controller.search("patient-uuid", "cd4", null, null, null, null, null, null, null, true);
		ArgumentCaptor<QuerySpec> captor = ArgumentCaptor.forClass(QuerySpec.class);
		verify(queryStore).query(captor.capture());
		assertEquals(Collections.singletonList("obs"), captor.getValue().getTypes());

		assertTypeNotAvailable(
		    () -> controller.search("patient-uuid", "cd4", null, "condition", null, null, null, null, null, true));
	}

	@Test
	public void partialResults_areAskedForOnlyByACallerWhoCanReadEveryRegisteredType() {
		// sec r1 B1: Get Conditions and Get Allergies without Get Diagnoses, which projects between them.
		Context.setUserContext(new UserContext(null) {

			@Override
			public User getAuthenticatedUser() {
				return new User();
			}

			@Override
			public boolean hasPrivilege(String privilege) {
				return PrivilegeConstants.GET_PATIENTS.equals(privilege) || PrivilegeConstants.GET_CONDITIONS.equals(privilege)
				        || PrivilegeConstants.GET_ALLERGIES.equals(privilege);
			}
		});
		BootstrapService bootstrap = mock(BootstrapService.class);
		when(bootstrap.getResourceTypeNames()).thenReturn(Arrays.asList("condition", "diagnosis", "allergy"));
		controller.setBootstrapService(bootstrap);
		QueryStoreService queryStore = mock(QueryStoreService.class);
		when(queryStore.query(org.mockito.ArgumentMatchers.any(QuerySpec.class)))
		        .thenReturn(QueryResult.empty(IndexStatus.INDEXED, QuerySpec.forPatient("p").query("x").build()));
		controller.setQueryStoreService(queryStore);

		controller.search("patient-uuid", "malaria", null, null, null, null, null, null, null, false);
		controller.recent("patient-uuid", null, "condition,allergy", false);
		authenticate();
		when(bootstrap.getResourceTypeNames()).thenReturn(Arrays.asList("condition", "diagnosis", "allergy"));
		controller.search("patient-uuid", "malaria", null, "condition", null, null, null, null, null, false);
		when(bootstrap.getResourceTypeNames()).thenReturn(Arrays.asList("condition", "mymodule_note"));
		controller.recent("patient-uuid", null, null, false);

		ArgumentCaptor<QuerySpec> captor = ArgumentCaptor.forClass(QuerySpec.class);
		verify(queryStore, org.mockito.Mockito.times(4)).query(captor.capture());
		assertFalse("cannot read diagnosis", captor.getAllValues().get(0).isPartialResults());
		assertFalse("naming only readable types does not help", captor.getAllValues().get(1).isPartialResults());
		assertTrue("reads every registered type, whatever it names", captor.getAllValues().get(2).isPartialResults());
		assertFalse("an SPI type with no read privilege counts as unreadable", captor.getAllValues().get(3).isPartialResults());
	}

	@Test
	public void canReadEvery_failsClosed_whenNoTypeIsRegistered() {
		authenticate();
		assertFalse(PatientRequests.canReadEvery(Collections.<String> emptyList()));
		assertTrue(PatientRequests.canReadEvery(Arrays.asList("obs", "condition")));
	}

	@Test
	public void search_withEveryReadPrivilege_searchesEveryType() {
		authenticate();
		QueryStoreService queryStore = mock(QueryStoreService.class);
		when(queryStore.query(org.mockito.ArgumentMatchers.any(QuerySpec.class)))
		        .thenReturn(QueryResult.empty(IndexStatus.INDEXED, QuerySpec.forPatient("p").query("x").build()));
		controller.setQueryStoreService(queryStore);

		controller.search("patient-uuid", "cd4", null, null, null, null, null, null, null, true);

		ArgumentCaptor<QuerySpec> captor = ArgumentCaptor.forClass(QuerySpec.class);
		verify(queryStore).query(captor.capture());
		assertEquals("no type list means every type", Collections.emptyList(), captor.getValue().getTypes());
	}

	@Test
	public void offsetPastTheCap_andLongEchoes_areBounded() {
		authenticate();
		controller.setQueryStoreService(mock(QueryStoreService.class));
		StringBuilder longMode = new StringBuilder();
		for (int i = 0; i < 200; i++) {
			longMode.append('m');
		}

		assertEquals("offset must be between 0 and 1000",
		    badRequest(() -> controller.search("p", "cd4", null, null, null, null, null, null, 1001, true)));
		String error = badRequest(() -> controller.search("p", "cd4", longMode.toString(), null, null, null, null, null,
		    null, true));
		assertTrue(error, error.length() < 80);
	}

	@Test
	public void aPathValueShapedLikeNoUuid_isA404WithoutAskingCore() {
		authenticate();
		PatientService patients = mock(PatientService.class);
		controller.setPatientService(patients);
		controller.setQueryStoreService(mock(QueryStoreService.class));

		for (String path : new String[] { "../../etc/passwd", "a b", "0123456789012345678901234567890123456789" }) {
			try {
				controller.status(path);
				throw new AssertionError("expected a 404 for " + path);
			}
			catch (PatientNotFoundException expected) {
				// pass
			}
		}
		org.mockito.Mockito.verifyNoInteractions(patients);
	}

	@Test
	public void warm_thatCannotStart_is503WithRetryAfter() {
		authenticate();
		QueryStoreService queryStore = mock(QueryStoreService.class);
		when(queryStore.warm("patient-uuid")).thenReturn(IndexStatus.NOT_INDEXED);
		when(queryStore.warm("failed-uuid")).thenReturn(IndexStatus.FAILED);
		controller.setQueryStoreService(queryStore);

		for (String uuid : new String[] { "patient-uuid", "failed-uuid" }) {
			ResponseEntity<Object> response = controller.warm(uuid);
			assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
			assertEquals(String.valueOf(QueryStoreRestController.WARM_RETRY_AFTER_SECONDS),
			    response.getHeaders().getFirst("Retry-After"));
		}
		assertEquals("failed", ((Map<?, ?>) controller.warm("failed-uuid").getBody()).get("status"));
	}

	@Test
	public void warm_ofAFailedChartInItsBackoff_saysToRetryWhenTheBackoffRunsOut() {
		// r1 N1: Retry-After is the remaining backoff, rounded up to whole seconds, at least 1.
		authenticate();
		QueryStoreService queryStore = mock(QueryStoreService.class);
		when(queryStore.warm(org.mockito.ArgumentMatchers.anyString())).thenReturn(IndexStatus.FAILED);
		when(queryStore.getRetryAfterMillis("backoff-long")).thenReturn(45_500L);
		when(queryStore.getRetryAfterMillis("backoff-short")).thenReturn(200L);
		when(queryStore.getRetryAfterMillis("no-slot")).thenReturn(0L);
		controller.setQueryStoreService(queryStore);

		assertEquals("46", controller.warm("backoff-long").getHeaders().getFirst("Retry-After"));
		assertEquals("1", controller.warm("backoff-short").getHeaders().getFirst("Retry-After"));
		assertEquals("due, but no slot was free", String.valueOf(QueryStoreRestController.WARM_RETRY_AFTER_SECONDS),
		    controller.warm("no-slot").getHeaders().getFirst("Retry-After"));
		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, controller.warm("backoff-long").getStatusCode());
	}

	/** Runs a call expected to name a type the caller may not search, and checks the one 403 it gets. */
	private void assertTypeNotAvailable(Runnable call) {
		try {
			call.run();
		}
		catch (TypeNotAvailableException e) {
			ResponseEntity<Object> response = controller.handleTypeNotAvailable(e);
			assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
			assertEquals(Collections.singletonMap("error", "Type not available"), response.getBody());
			return;
		}
		throw new AssertionError("expected a 403 Type not available");
	}

	/** Runs a call expected to fail parameter validation and returns the 400 body's error. */
	private String badRequest(Runnable call) {
		try {
			call.run();
		}
		catch (BadParameterException e) {
			ResponseEntity<Object> response = controller.handleBadParameter(e);
			assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
			return errorOf(response);
		}
		throw new AssertionError("expected a 400");
	}

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

	/** Records launch attempts and returns a canned result, so the controller's scope:"all" routing
	 *  is verified without wiring a daemon token or spawning a real bootstrap thread. */
	private static final class RecordingLauncher extends BootstrapLauncher {

		private final boolean result;

		private int launches = 0;

		private int resyncLaunches = 0;

		private String resyncedType;

		private RecordingLauncher(boolean result) {
			this.result = result;
		}

		@Override
		public boolean launchAsync() {
			launches++;
			return result;
		}

		@Override
		public boolean launchResyncAsync(String resourceType) {
			resyncLaunches++;
			resyncedType = resourceType;
			return result;
		}
	}

	private static String errorOf(ResponseEntity<Object> response) {
		return (String) ((Map<?, ?>) response.getBody()).get("error");
	}
}
