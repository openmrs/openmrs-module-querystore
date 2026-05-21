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

import java.util.List;

import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.querystore.backend.WriteResult;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.util.PrivilegeConstants;

/**
 * Entry point for indexing and searching the read-side projection of OpenMRS clinical data.
 * Read methods declare their required privileges via {@link Authorized}; core's authorization
 * advice enforces them at call time (ADR decision 14).
 */
public interface QueryStoreService extends OpenmrsService {

	/**
	 * Indexes a clinical record into the query store, routing it to the correct per-type index
	 * based on {@link QueryDocument#getResourceType()}. Internal: invoked by the sync pipeline,
	 * not by consumers.
	 *
	 * <p>Returns a non-null {@link WriteResult} describing whether the write actually landed.
	 * Callers that track persistence accuracy (notably the bootstrap dispatcher, whose
	 * {@code documents_indexed} counter must reflect confirmed writes, not just "the call didn't
	 * throw") MUST consult {@link WriteResult#isSucceeded()} before counting the write as
	 * successful. A returned failure carries the per-doc
	 * {@link org.openmrs.module.querystore.backend.DocFailure} so the caller can log specifics.
	 * Implementations MUST return a {@code WriteResult} (success or failed), never {@code null} —
	 * a null return would NPE inside the bootstrap dispatcher and silently abort the type's run.
	 * Throws {@link IllegalStateException} when the backend isn't wired — a misconfiguration that
	 * the caller should surface, not silently absorb.
	 */
	WriteResult index(QueryDocument document);

	/**
	 * Removes the document with the given resource UUID from the given per-type index. Internal:
	 * invoked by the sync pipeline, not by consumers.
	 */
	void delete(String resourceType, String resourceUuid);

	/**
	 * Removes every document keyed by {@code patientUuid} across every per-type index. Internal:
	 * invoked by the patient-purge bridge advice (and any future REST/admin caller) when a
	 * patient is purged from core so the read-store doesn't retain PHI past the core deletion.
	 * Iterates the cross-session-merged set of indexes/tables on the configured backend, mirroring
	 * the discovery contract of {@code searchByPatient} and {@code existsByPatient}.
	 */
	void bulkDeleteByPatient(String patientUuid);

	/**
	 * Hybrid (BM25 + semantic) search within a patient's chart.
	 *
	 * <p>Cold-patient side effect: when no documents are indexed for {@code patientUuid}, the call
	 * synchronously projects that patient's clinical data before running the search (ADR Open
	 * Question: Initial backfill / bootstrap, "Lazy per-patient projection"). First touch on a
	 * never-indexed patient can therefore block for seconds while serialization and embedding run;
	 * subsequent calls return at steady-state latency. Consumers needing predictable response on
	 * every call should pre-bootstrap or schedule periodic backfills.
	 */
	@Authorized(PrivilegeConstants.GET_PATIENTS)
	List<QueryDocument> searchByPatient(String patientUuid, String query, int limit);

	/**
	 * Hybrid (BM25 + semantic) search across all clinical record types.
	 */
	@Authorized(PrivilegeConstants.GET_PATIENTS)
	List<QueryDocument> search(String query, int limit);
}
