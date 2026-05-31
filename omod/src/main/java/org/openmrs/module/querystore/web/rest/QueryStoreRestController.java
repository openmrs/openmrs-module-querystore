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

import org.openmrs.api.context.Context;
import org.openmrs.module.querystore.api.QueryStoreService;
import org.openmrs.module.querystore.bootstrap.BootstrapService;
import org.openmrs.module.querystore.bootstrap.BootstrapStatusReport;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * REST surface for the query store. Currently exposes the bootstrap (initial-backfill) status so
 * operators and deploy pipelines can verify a deployment is <em>fully indexed</em> before trusting
 * its reads — a SQL-dump-seeded deployment bypasses the live indexing bridge and depends on the
 * background bootstrap completing, and the lazy per-patient projection cannot repair an
 * already-partially-indexed patient.
 *
 * <p>It also exposes a per-patient reindex trigger so a stale/partially-indexed patient can be
 * repaired on a live instance without restarting the server (the only other no-restart path, a
 * cold-touch lazy projection, refuses to touch an already-partially-indexed patient).
 *
 * <pre>
 * GET  /ws/rest/v1/querystore/indexingstatus
 *   -&gt; {"complete": false, "types": [{"resourceType":"obs","status":"RUNNING",...}, ...]}
 * POST /ws/rest/v1/querystore/reindex   {"patient":"&lt;uuid&gt;"}
 *   -&gt; {"patient":"&lt;uuid&gt;", "documentsIndexed": 154}
 * </pre>
 */
@Controller
@RequestMapping("/rest/" + RestConstants.VERSION_1 + "/querystore")
public class QueryStoreRestController {

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
	 * Force a full re-projection of one patient (delete + re-index every type), then report the
	 * resulting document count. Synchronous — bounded to one patient and runs in the authenticated
	 * request thread. The delete-before-reindex invariant lives in {@link BootstrapService#reindexPatient}
	 * (unit-tested) so this stays a thin adapter.
	 *
	 * <p>Gated by {@code Manage Global Properties} rather than the read endpoint's {@code Get Patients}:
	 * this is a destructive, expensive maintenance operation (it deletes the patient's documents before
	 * rebuilding), so it requires a system-administration privilege.
	 *
	 * <p>Eventual-consistency note: the delete and the re-projection run under a per-patient lock, but a
	 * concurrent search for the <em>same</em> patient may briefly observe partial or empty results while
	 * the rebuild is in flight (its existence probe runs outside that lock). The full chart is restored
	 * by the time this call returns. Triggering a reindex during active charting on that patient is the
	 * caller's call.
	 */
	@RequestMapping(value = "/reindex", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> reindexPatient(@RequestBody Map<String, String> body) {
		Context.requirePrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);

		String patientUuid = body == null ? null : body.get("patient");
		if (patientUuid == null || patientUuid.trim().isEmpty()) {
			Map<String, Object> error = new HashMap<String, Object>();
			error.put("error", "patient is required");
			return new ResponseEntity<Object>(error, HttpStatus.BAD_REQUEST);
		}

		Context.getService(BootstrapService.class).reindexPatient(patientUuid);

		int documentsIndexed = Context.getService(QueryStoreService.class).getPatientChart(patientUuid).size();
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("patient", patientUuid);
		result.put("documentsIndexed", documentsIndexed);
		return new ResponseEntity<Object>(result, HttpStatus.OK);
	}
}
