/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.backend;

import java.util.List;

import org.openmrs.module.querystore.model.QueryDocument;

/**
 * Backend SPI for the query store. Implementations live behind {@code querystore.backend} (one of
 * {@code mysql}, {@code lucene}, or {@code elasticsearch} per ADR Decision 3); the service layer
 * depends on this interface, never on a concrete tier.
 *
 * <p>Two invariants every implementation must satisfy regardless of tier:
 * <ol>
 *   <li><b>resource_uuid-keyed idempotency.</b> The same upsert applied twice produces the same
 *   stored document; the same delete applied twice is a no-op the second time.</li>
 *   <li><b>Patient-scoped read at sub-linear cost.</b> Searches with a
 *   {@link Filter#patientScope(String)} filter must not require a full-corpus scan.</li>
 * </ol>
 *
 * <p>Write semantics: every {@code upsert}/{@code delete}/{@code bulk*} call returns only after
 * the backend has acknowledged durability AND a subsequent search from the same JVM will see the
 * write. The SPI does not expose transaction handles; "the write succeeded" means durable +
 * visible.
 */
public interface BackendStore {

	// ---------- lifecycle ----------

	/**
	 * Idempotently creates the per-type store for {@code resourceType} if it does not exist.
	 * Repeated calls with the same spec are a no-op.
	 */
	void ensureSchema(String resourceType, SchemaSpec spec);

	/** Drops the per-type store. Used by tests and by full-rebuild flows. */
	void deleteSchema(String resourceType);

	// ---------- writes ----------

	/**
	 * Writes the document, dropping the call when {@link QueryDocument#getLastModified()} is older
	 * than the stored version (conditional-upsert-by-version contract; see the third SPI invariant
	 * in ADR Decision 3). Null on either side falls back to last-write-wins. The successful result
	 * does not distinguish "applied" from "dropped as stale" — both are no-error outcomes consistent
	 * with idempotency.
	 */
	WriteResult upsert(QueryDocument doc);

	WriteResult delete(String resourceType, String resourceUuid);

	/**
	 * Bulk variant of {@link #upsert(QueryDocument)} carrying the same conditional-upsert-by-version
	 * contract per document. Documents whose {@code last_modified} is older than the stored version
	 * are dropped silently; per-document non-version failures surface via {@link BulkWriteResult}.
	 */
	BulkWriteResult bulkUpsert(List<QueryDocument> docs);

	BulkWriteResult bulkDelete(String resourceType, List<String> resourceUuids);

	/**
	 * Removes every document keyed by the given {@code patient_uuid} across every per-type store.
	 * Used by void-cascade and patient-merge handling; the contract is "after this returns, no
	 * search can return a document for this patient".
	 */
	BulkWriteResult bulkDeleteByPatient(String patientUuid);

	// ---------- reads ----------

	SearchResult bm25(SearchRequest req);

	SearchResult knn(SearchRequest req);

	/**
	 * Hybrid search. Default behavior across all tiers is BM25 + kNN with rank-based RRF fusion
	 * applied at the service layer (see {@code QueryStoreServiceImpl}); the ES backend is
	 * permitted to override with native RRF when measurable benefit justifies the divergence.
	 */
	SearchResult hybrid(SearchRequest req);

	// ---------- introspection ----------

	BackendCapabilities capabilities();

	HealthStatus health();
}
