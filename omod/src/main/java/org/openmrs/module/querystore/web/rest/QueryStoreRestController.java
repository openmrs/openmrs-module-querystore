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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.querystore.api.QueryStoreService;
import org.openmrs.module.querystore.bootstrap.BootstrapLauncher;
import org.openmrs.module.querystore.bootstrap.BootstrapService;
import org.openmrs.module.querystore.bootstrap.BootstrapStatusReport;
import org.openmrs.module.querystore.model.ContextSlice;
import org.openmrs.module.querystore.model.ContextSliceRequest;
import org.openmrs.module.querystore.model.QueryDocument;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
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

	private static final int DEFAULT_LIMIT = 50;

	/**
	 * Read endpoint over the query store's three read methods (ADR Decision 16):
	 *
	 * <pre>
	 * GET /ws/rest/v1/querystore/patientrecord?patient=&lt;uuid&gt;            -&gt; full chart (getPatientChart), date desc, paged
	 * GET /ws/rest/v1/querystore/patientrecord?patient=&lt;uuid&gt;&amp;q=&lt;text&gt; -&gt; hybrid-ranked top-K (searchByPatient)
	 * GET /ws/rest/v1/querystore/patientrecord?q=&lt;text&gt;                  -&gt; cross-patient ranked top-K (search; coarse gate)
	 * </pre>
	 *
	 * <p>Gated by {@code Get Patients} — the same privilege the read methods carry. The {@code embedding}
	 * vector is never returned, and no {@code score} is emitted (relevance is list order plus a 1-based
	 * {@code rank}). A full chart carries a true {@code totalCount} and pages in memory; ranked results are
	 * a top-K window (null totalCount). The JSON shape is produced and unit-tested in {@link PatientRecordView}.
	 */
	@RequestMapping(value = "/patientrecord", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Object> getPatientRecords(
	        @RequestParam(value = "patient", required = false) String patient,
	        @RequestParam(value = "q", required = false) String q,
	        @RequestParam(value = "limit", required = false) Integer limit,
	        @RequestParam(value = "startIndex", required = false) Integer startIndex,
	        @RequestParam(value = "mode", required = false) String mode,
	        @RequestParam(value = "types", required = false) String types,
	        @RequestParam(value = "temporal", required = false) Boolean temporal,
	        @RequestParam(value = "interpret", required = false) Boolean interpret,
	        @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {
		Context.requirePrivilege(PrivilegeConstants.GET_PATIENTS);

		String patientUuid = StringUtils.trimToNull(patient);
		String query = StringUtils.trimToNull(q);
		int from = startIndex == null ? 0 : startIndex.intValue();
		int size = limit == null ? DEFAULT_LIMIT : limit.intValue();
		if (size <= 0 || from < 0) {
			return errorResponse(HttpStatus.BAD_REQUEST, "limit must be > 0 and startIndex >= 0");
		}
		boolean contextMode = "context".equals(StringUtils.trimToNull(mode));
		if (contextMode && patientUuid == null) {
			return errorResponse(HttpStatus.BAD_REQUEST, "mode=context requires a patient");
		}
		if (patientUuid == null && query == null) {
			return errorResponse(HttpStatus.BAD_REQUEST, "patient or q is required");
		}
		// 404 a bogus patient up front — correct status, and it avoids a wasted cold-touch projection.
		if (patientUuid != null && patientService().getPatientByUuid(patientUuid) == null) {
			return errorResponse(HttpStatus.NOT_FOUND, "No patient with uuid '" + patientUuid + "'");
		}
		if (contextMode) {
			// Tiered context slice (ADR Decision 17 §4): the caller's question interpretation
			// rides as params; each record carries its selection tier. Like ranked windows,
			// slices claim no stable snapshot — no ETag participation.
			Set<String> typeSet = new HashSet<String>();
			if (StringUtils.isNotBlank(types)) {
				for (String type : types.split(",")) {
					if (StringUtils.isNotBlank(type)) {
						typeSet.add(type.trim());
					}
				}
			}
			ContextSliceRequest sliceRequest = new ContextSliceRequest(typeSet, Boolean.TRUE.equals(temporal));
			sliceRequest.setInterpretQuestion(Boolean.TRUE.equals(interpret));
			ContextSlice slice = queryStoreService().getContextSlice(patientUuid, query, sliceRequest);
			Map<String, Object> sliceBody = PatientRecordView.contextPage(slice, from, size);
			return new ResponseEntity<Object>(sliceBody, HttpStatus.OK);
		}

		StringBuilder baseParams = new StringBuilder();
		if (patientUuid != null) {
			baseParams.append("patient=").append(PatientRecordView.encode(patientUuid)).append('&');
		}
		if (query != null) {
			baseParams.append("q=").append(PatientRecordView.encode(query)).append('&');
		}

		boolean ranked = query != null;
		List<QueryDocument> page;
		Integer totalCount;
		String snapshotId = null;
		String pageEtag = null;
		if (patientUuid != null && query == null) {
			// Full chart: enumerate (Decision 15 returns the whole set), then page in memory.
			List<QueryDocument> all = queryStoreService().getPatientChart(patientUuid);
			totalCount = Integer.valueOf(all.size());
			page = slice(all, from, size);
			snapshotId = PatientRecordView.snapshotId(all);
			pageEtag = PatientRecordView.pageEtag(snapshotId, from, size);
			if (etagMatches(ifNoneMatch, pageEtag)) {
				return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
				        .eTag(pageEtag)
				        .header("Cache-Control", "private, no-cache, must-revalidate")
				        .build();
			}
		} else {
			// Ranked top-K: the service has no offset, so fetch from+size and drop the prefix. A ranked
			// query is top-K, not a browseable collection — there is no true total, so totalCount is null.
			List<QueryDocument> top = (patientUuid != null)
			        ? queryStoreService().searchByPatient(patientUuid, query, from + size)
			        : queryStoreService().search(query, from + size);
			page = slice(top, from, size);
			totalCount = null;
		}

		Map<String, Object> body = PatientRecordView.page(page, ranked, from, size, totalCount,
		        baseParams.toString(), snapshotId);
		if (pageEtag != null) {
			return ResponseEntity.ok()
			        .eTag(pageEtag)
			        .header("Cache-Control", "private, no-cache, must-revalidate")
			        .body(body);
		}
		return new ResponseEntity<Object>(body, HttpStatus.OK);
	}

	/** Convenience seam retained for existing direct controller tests. */
	ResponseEntity<Object> getPatientRecords(String patient, String q, Integer limit, Integer startIndex) {
		return getPatientRecords(patient, q, limit, startIndex, null, null, null, null, null);
	}

	/** Convenience seam retained for existing conditional-read controller tests. */
	ResponseEntity<Object> getPatientRecords(String patient, String q, Integer limit, Integer startIndex,
	        String ifNoneMatch) {
		return getPatientRecords(patient, q, limit, startIndex, null, null, null, null, ifNoneMatch);
	}

	/** Convenience seam for the context-mode controller tests (no interpret flag). */
	ResponseEntity<Object> getPatientRecords(String patient, String q, Integer limit, Integer startIndex,
	        String mode, String types, Boolean temporal, String ifNoneMatch) {
		return getPatientRecords(patient, q, limit, startIndex, mode, types, temporal, null, ifNoneMatch);
	}

	private static boolean etagMatches(String ifNoneMatch, String pageEtag) {
		if (ifNoneMatch == null || pageEtag == null) {
			return false;
		}
		for (String candidate : ifNoneMatch.split(",")) {
			String trimmed = candidate.trim();
			if ("*".equals(trimmed) || pageEtag.equals(trimmed)) {
				return true;
			}
		}
		return false;
	}

	/** The sublist [from, from+size) clamped to the list bounds; empty when {@code from} is past the end. */
	private static List<QueryDocument> slice(List<QueryDocument> list, int from, int size) {
		if (from >= list.size()) {
			return Collections.emptyList();
		}
		return new ArrayList<QueryDocument>(list.subList(from, Math.min(from + size, list.size())));
	}

	/** Non-null only when a test injects it; production resolves core's PatientService per call. */
	private PatientService injectedPatientService;

	private PatientService patientService() {
		return injectedPatientService != null ? injectedPatientService : Context.getPatientService();
	}

	/** Visible-for-testing seam: lets the POJO controller test stub patient existence (the 404 path)
	 *  without a Spring context. Production resolves the PatientService via {@link Context}. */
	void setPatientService(PatientService patientService) {
		this.injectedPatientService = patientService;
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
	 * Reindexes the read store, in one of three scopes selected by the request body:
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
	 * <li>{@code {"scope":"type","resourceType":"<type>"}} — reconciliation remediation: force a full
	 * re-walk of one resource type to correct drift surfaced by {@code GET /querystore/drift} (ADR:
	 * Sync reliability and reconciliation). <strong>Asynchronous</strong> (a full single-type scan
	 * can't run in a request thread); returns {@code 202 Accepted}, poll {@code indexingstatus}.
	 * Re-walk only — corrects under-indexing, not stale extras (see {@link BootstrapService#resyncType}).
	 * The {@code resourceType} is validated against {@link BootstrapService#getResourceTypeNames()}
	 * (a {@code 400} for an unknown type) so a typo fails fast rather than silently on the daemon
	 * thread.</li>
	 * </ul>
	 *
	 * <p>The global/type scopes are opt-in via an explicit {@code scope} rather than "absent patient
	 * means everything": absence overlaps with a malformed/truncated request, and resolving that
	 * ambiguity by launching the single most expensive operation in the module is a footgun. A request
	 * with neither {@code patient} nor a {@code scope} is therefore a {@code 400}, and an
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
		String resourceType = StringUtils.trimToNull(body == null ? null : body.get("resourceType"));

		if (scope != null) {
			if ("all".equalsIgnoreCase(scope)) {
				if (patientUuid != null || resourceType != null) {
					// Ambiguous intent: a global reindex is the most expensive op in the module, so do
					// not silently let scope win and ignore a patient/resourceType the caller also sent.
					return errorResponse(HttpStatus.BAD_REQUEST,
					    "Specify only one of patient, scope:\"all\", or scope:\"type\" + resourceType");
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
			if ("type".equalsIgnoreCase(scope)) {
				// Reconciliation remediation: re-walk one drifted type (ADR: Sync reliability and
				// reconciliation). Async like scope:"all" — a full single-type scan can't run in a
				// request thread.
				if (patientUuid != null) {
					return errorResponse(HttpStatus.BAD_REQUEST, "Specify either patient or scope:\"type\", not both");
				}
				if (resourceType == null) {
					return errorResponse(HttpStatus.BAD_REQUEST, "scope:\"type\" requires a resourceType");
				}
				// Validate synchronously (a cheap registry read) so a typo is a 400 here rather than a
				// 202 that fails silently on the daemon thread.
				if (!bootstrapService().getResourceTypeNames().contains(resourceType)) {
					return errorResponse(HttpStatus.BAD_REQUEST, "Unknown resourceType '" + resourceType + "'");
				}
				boolean launched = bootstrapLauncher().launchResyncAsync(resourceType);
				if (!launched) {
					return errorResponse(HttpStatus.SERVICE_UNAVAILABLE,
					    "Cannot start re-sync: the bootstrap daemon is not yet available");
				}
				Map<String, Object> result = new HashMap<String, Object>();
				result.put("accepted", true);
				result.put("resourceType", resourceType);
				return new ResponseEntity<Object>(result, HttpStatus.ACCEPTED);
			}
			return errorResponse(HttpStatus.BAD_REQUEST,
			    "Unknown scope '" + scope + "'; supported scopes are \"all\" and \"type\"");
		}

		if (patientUuid == null) {
			return errorResponse(HttpStatus.BAD_REQUEST, "patient or scope (\"all\" or \"type\") is required");
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
