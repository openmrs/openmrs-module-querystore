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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.querystore.api.QueryStoreService;
import org.openmrs.module.querystore.bootstrap.BootstrapLauncher;
import org.openmrs.module.querystore.bootstrap.BootstrapService;
import org.openmrs.module.querystore.bootstrap.BootstrapStatusReport;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.util.PrivilegeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * REST surface for the query store. Currently exposes the bootstrap (initial-backfill) status so
 * operators and deploy pipelines can verify a deployment is <em>fully indexed</em> before trusting
 * its reads — a SQL-dump-seeded deployment bypasses the live indexing path and depends on the
 * background bootstrap completing, and the lazy per-patient projection cannot repair an
 * already-partially-indexed patient.
 *
 * <p>It also exposes a reindex trigger so a stale/partially-indexed instance can be repaired without
 * restarting the server (the only other no-restart path, a cold-touch lazy projection, refuses to
 * touch an already-partially-indexed patient). It reindexes one patient synchronously, or — given an
 * explicit {@code scope:"all"} — launches the global bootstrap over every patient asynchronously,
 * since a full-corpus scan cannot run in a request thread.
 *
 * <pre>
 * GET  /ws/rest/v1/querystore/indexingstatus
 *   -&gt; {"complete": false, "types": [{"resourceType":"obs","status":"RUNNING",...}, ...]}
 * POST /ws/rest/v1/querystore/reindex   {"patient":"&lt;uuid&gt;"}
 *   -&gt; 200 {"patient":"&lt;uuid&gt;", "documentsIndexed": 154}
 * POST /ws/rest/v1/querystore/reindex   {"scope":"all"}
 *   -&gt; 202 {"accepted": true}   (then poll indexingstatus)
 * </pre>
 */
@Controller
@RequestMapping("/rest/" + RestConstants.VERSION_1 + "/querystore")
public class QueryStoreRestController {

	private static final Logger log = LoggerFactory.getLogger(QueryStoreRestController.class);

	/**
	 * Per-resource-type bootstrap status plus a derived {@code complete} flag. Gated by
	 * {@code Get Patients} — the same privilege the query store's read API requires — because a
	 * type's {@code failureMessage} can contain internal record/patient identifiers.
	 */
	@RequestMapping(value = "/indexingstatus", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Object> getIndexingStatus() {
		Context.requirePrivilege(PrivilegeConstants.GET_PATIENTS);

		BootstrapStatusReport report = BootstrapStatusReport.from(
				Context.getService(BootstrapService.class).getStatus());

		// Response shape (keys + values) is produced and unit-tested in BootstrapStatusReport.toMap();
		// the controller stays a thin adapter so the JSON contract isn't hand-typed untested here.
		return new ResponseEntity<Object>(report.toMap(), HttpStatus.OK);
	}

	/**
	 * Per-type drift snapshot — core "expected" count vs live index count (ADR: Sync reliability and
	 * reconciliation). Detection only; the operator reuses {@code POST /reindex} to remediate a gap.
	 * Gated by {@code Manage Global Properties} (not the read endpoint's {@code Get Patients}): it runs
	 * a COUNT per resource type across core and the index, so it is an admin/ops reconciliation tool,
	 * the same audience as {@code reindex}. The JSON shape is produced and unit-tested in
	 * {@link org.openmrs.module.querystore.bootstrap.DriftReport#toMap()}; the controller is a thin adapter.
	 */
	@RequestMapping(value = "/drift", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Object> getDrift() {
		Context.requirePrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
		return new ResponseEntity<Object>(bootstrapService().getDrift().toMap(), HttpStatus.OK);
	}

	/**
	 * Reindexes the read store, in one of two scopes selected by the request body:
	 *
	 * <ul>
	 * <li>{@code {"patient":"<uuid>"}} — force a full re-projection of one patient (delete + re-index
	 * every type), then report the resulting document count. <strong>Synchronous</strong>: bounded to
	 * one patient and runs in the authenticated request thread. The delete-before-reindex invariant
	 * lives in {@link BootstrapService#reindexPatient} (unit-tested) so this stays a thin adapter.</li>
	 * <li>{@code {"scope":"all"}} — launch the global bootstrap over every patient and type.
	 * <strong>Asynchronous</strong>: a full-corpus scan cannot run in a request thread, so it is handed
	 * to a daemon thread via {@link BootstrapLauncher} and the call returns {@code 202 Accepted}
	 * immediately. Progress is observed via {@code GET /querystore/indexingstatus}.</li>
	 * </ul>
	 *
	 * <p>The global scope is opt-in via an explicit {@code scope:"all"} rather than "absent patient
	 * means everything": absence overlaps with a malformed/truncated request, and resolving that
	 * ambiguity by launching the single most expensive operation in the module is a footgun. A request
	 * with neither {@code patient} nor {@code scope:"all"} is therefore a {@code 400}, and an
	 * unrecognised {@code scope} is rejected rather than silently falling through.
	 *
	 * <p>Gated by {@code Manage Global Properties} rather than the read endpoint's {@code Get Patients}:
	 * both scopes are destructive/expensive maintenance operations, so they require a system-
	 * administration privilege.
	 *
	 * <p>Eventual-consistency note (per-patient scope): the delete and the re-projection run under a
	 * per-patient lock, but a concurrent search for the <em>same</em> patient may briefly observe
	 * partial or empty results while the rebuild is in flight (its existence probe runs outside that
	 * lock). The full chart is restored by the time this call returns. Triggering a reindex during
	 * active charting on that patient is the caller's call.
	 */
	@RequestMapping(value = "/reindex", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> reindex(@RequestBody(required = false) Map<String, String> body) {
		Context.requirePrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);

		String scope = StringUtils.trimToNull(body == null ? null : body.get("scope"));
		String patientUuid = StringUtils.trimToNull(body == null ? null : body.get("patient"));

		if (scope != null) {
			if (!"all".equalsIgnoreCase(scope)) {
				return errorResponse(HttpStatus.BAD_REQUEST, "Unknown scope '" + scope + "'; the only supported scope is \"all\"");
			}
			if (patientUuid != null) {
				// Ambiguous intent: a global reindex is expensive, so do not silently let scope win
				// and ignore the patient — the caller might have meant a one-patient reindex.
				return errorResponse(HttpStatus.BAD_REQUEST, "Specify either patient or scope:\"all\", not both");
			}
			boolean launched = bootstrapLauncher().launchAsync();
			if (!launched) {
				// No daemon token wired yet, so the async backfill cannot start. 503 rather than a
				// misleading 202 that would imply work is underway.
				return errorResponse(HttpStatus.SERVICE_UNAVAILABLE,
				    "Cannot start reindex: the bootstrap daemon is not yet available");
			}
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("accepted", true);
			return new ResponseEntity<Object>(result, HttpStatus.ACCEPTED);
		}

		if (patientUuid == null) {
			return errorResponse(HttpStatus.BAD_REQUEST, "patient or scope:\"all\" is required");
		}

		bootstrapService().reindexPatient(patientUuid);

		int documentsIndexed = queryStoreService().getPatientChart(patientUuid).size();
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("patient", patientUuid);
		result.put("documentsIndexed", documentsIndexed);
		return new ResponseEntity<Object>(result, HttpStatus.OK);
	}

	/** Non-null only when a test injects a launcher via {@link #setBootstrapLauncher}; production
	 *  leaves this null and resolves the singleton from the Spring context per call. */
	private BootstrapLauncher injectedBootstrapLauncher;

	/** Resolves the launcher: the test-injected one if present, else the Spring-context singleton. */
	private BootstrapLauncher bootstrapLauncher() {
		return injectedBootstrapLauncher != null ? injectedBootstrapLauncher
		        : Context.getRegisteredComponent("querystore.bootstrap.launcher", BootstrapLauncher.class);
	}

	/** Visible-for-testing seam: lets the POJO controller test exercise the scope:"all" routing
	 *  without a Spring context. Production resolves the launcher via {@link Context}. */
	void setBootstrapLauncher(BootstrapLauncher bootstrapLauncher) {
		this.injectedBootstrapLauncher = bootstrapLauncher;
	}

	/** Non-null only when a test injects collaborators; production leaves these null and resolves
	 *  the services from the Spring context per call (keeping the controller a thin adapter). */
	private BootstrapService injectedBootstrapService;

	private QueryStoreService injectedQueryStoreService;

	private BootstrapService bootstrapService() {
		return injectedBootstrapService != null ? injectedBootstrapService
		        : Context.getService(BootstrapService.class);
	}

	private QueryStoreService queryStoreService() {
		return injectedQueryStoreService != null ? injectedQueryStoreService
		        : Context.getService(QueryStoreService.class);
	}

	/** Visible-for-testing seams: let the POJO controller test exercise the per-patient routing and
	 *  response contract without a Spring context (the real reindex round-trip is covered by
	 *  BootstrapServiceImplTest). Production resolves both services via {@link Context}. */
	void setBootstrapService(BootstrapService bootstrapService) {
		this.injectedBootstrapService = bootstrapService;
	}

	void setQueryStoreService(QueryStoreService queryStoreService) {
		this.injectedQueryStoreService = queryStoreService;
	}

	/**
	 * Maps an authorization failure to a proper status with a clean body. Without this, the framework
	 * serializes the thrown exception as an HTTP 200 carrying a full stack trace — both a misleading
	 * status and an information leak. 401 when the caller is unauthenticated; 403 when authenticated
	 * but lacking the privilege.
	 *
	 * <p>Catches both auth-failure types because they are siblings under {@code APIException}, not
	 * one hierarchy: {@link Context#requirePrivilege} (the up-front gate this controller uses) throws
	 * {@link ContextAuthenticationException}, while the {@code @Authorized} AOP throws
	 * {@link APIAuthenticationException}. The up-front gate is the only auth check that fires on the
	 * current code path, so {@link ContextAuthenticationException} is what you observe today; the
	 * {@link APIAuthenticationException} arm is defense-in-depth so an authorization failure raised by
	 * any future or downstream {@code @Authorized} call still surfaces as 401/403 rather than falling
	 * to the catch-all as a 500.
	 */
	@ExceptionHandler({ ContextAuthenticationException.class, APIAuthenticationException.class })
	@ResponseBody
	public ResponseEntity<Object> handleAuthFailure(APIException e) {
		HttpStatus status = Context.isAuthenticated() ? HttpStatus.FORBIDDEN : HttpStatus.UNAUTHORIZED;
		return errorResponse(status,
		        status == HttpStatus.FORBIDDEN ? "Insufficient privileges" : "Authentication required");
	}

	/**
	 * A malformed JSON request body is a client error — return 400, not the catch-all's 500. (The
	 * framework throws this during {@code @RequestBody} binding, before the handler method runs.)
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseBody
	public ResponseEntity<Object> handleMalformedBody(HttpMessageNotReadableException e) {
		return errorResponse(HttpStatus.BAD_REQUEST, "Malformed request body");
	}

	/**
	 * Catch-all so any other unexpected error returns a clean 500 rather than the framework's default
	 * for a thrown exception on this controller (an HTTP 200 carrying a serialized stack trace). The
	 * detail is logged server-side, never returned to the caller.
	 */
	@ExceptionHandler(Exception.class)
	@ResponseBody
	public ResponseEntity<Object> handleUnexpected(Exception e) {
		log.error("Unexpected error handling a querystore REST request", e);
		return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error");
	}

	private static ResponseEntity<Object> errorResponse(HttpStatus status, String message) {
		Map<String, Object> body = new HashMap<String, Object>();
		body.put("error", message);
		return new ResponseEntity<Object>(body, status);
	}
}
