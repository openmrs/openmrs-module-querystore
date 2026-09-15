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
import static org.junit.Assert.assertNotNull;
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
import org.junit.Test;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.querystore.api.QueryStoreService;
import org.openmrs.module.querystore.bootstrap.BootstrapLauncher;
import org.openmrs.module.querystore.bootstrap.BootstrapService;
import org.openmrs.module.querystore.bootstrap.DriftReport;
import org.openmrs.module.querystore.model.QueryDocument;
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
		verify(bootstrap).reindexPatient("patient-uuid");
		assertEquals("a per-patient reindex must not also launch a global bootstrap", 0, launcher.launches);
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
