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

	/**
	 * Returns true when any document keyed by {@code patient_uuid} exists in any per-type store.
	 * Honors the second SPI invariant (patient-scoped reads sub-linear) and is permitted to
	 * short-circuit on the first hit across types. Returns {@code false} for the steady-state
	 * "no data" case (no documents, or no per-type stores yet) silently; transient backend errors
	 * (connection lost, broken index) are logged and also surface as {@code false}. Callers
	 * (auto-index on cold search) treat both as "fall through to indexing," which converges to the
	 * same result whether the backend was empty or transiently unhealthy.
	 */
	boolean existsByPatient(String patientUuid);

	/**
	 * Returns the number of documents in the per-type store for {@code resourceType}, or {@code -1} if
	 * this backend does not implement a count (the {@code default}). Used by drift detection (ADR: Sync
	 * reliability and reconciliation) to compare the live index size against core's expected record
	 * count; {@code -1} means "drift not computable for this tier" so a missing override degrades
	 * gracefully rather than reporting a false zero. An implemented backend returns {@code 0} (not
	 * {@code -1}) when the per-type store does not exist yet — that is a genuine "indexed nothing",
	 * distinct from "can't count".
	 */
	default long countByType(String resourceType) {
		return -1L;
	}

	/**
	 * Returns every document keyed by {@code patient_uuid} across every per-type store, ordered by
	 * {@code record_date} descending with {@code (resource_type, resource_uuid)} as the deterministic
	 * tie-breaker. Backs {@code QueryStoreService.getPatientChart} per ADR Decision 15 — the
	 * full-chart-to-LLM consumer path that needs the patient's entire indexed projection without
	 * relevance ranking or result-count limits. Honors the second SPI invariant: the patient_uuid
	 * filter must push down to the inverted index / FULLTEXT / term filter so the work is bounded
	 * by the patient's document count, not by corpus size.
	 *
	 * <p>Returns an empty list when no per-type stores exist yet, when nothing is indexed for the
	 * patient, or when {@code patientUuid} is null/blank — callers (the service layer) probe with
	 * {@code existsByPatient} first and trigger lazy bootstrap on a true cold start, so an empty
	 * return here means the bootstrap also produced nothing, which is a steady-state outcome.
	 *
	 * <p>Error tolerance is tier-shape-dependent but every implementation honours the "don't throw"
	 * contract: failures are logged, an empty or partial chart is returned, and the LLM caller is
	 * never left mid-prompt with a thrown call. Two shapes are recognised:
	 * <ul>
	 *   <li><b>Per-store partial</b> (MySQL, Lucene) — implementations enumerate per table / index;
	 *   one store failing drops that store's contribution and the rest still appear in the chart.
	 *   This is the preferred shape when the backend's read model has natural per-store
	 *   boundaries.</li>
	 *   <li><b>Call-level all-or-nothing</b> (Elasticsearch) — implementations issue a single
	 *   cross-index search; a call-level failure (cluster red, connection lost) yields an empty
	 *   chart for the whole call. Per-shard failures inside a successful response are absorbed into
	 *   the returned hits silently. The cost of partial fallback (per-index serial searches) is not
	 *   justified at the ES tier in v1, where the wildcard search is the operational hot path.</li>
	 * </ul>
	 * Callers must not interpret an empty list as "patient has no chart" without consulting
	 * {@link #existsByPatient(String)} or accepting that v1 collapses both states to {@code []}.
	 *
	 * <p><b>Cross-call consistency.</b> Each implementation chooses its own snapshot model — the
	 * SPI does not promise referential integrity between documents within a single call's result.
	 * Specifically: MySQL reads all per-type tables in a single JDBC transaction (one read-view
	 * across types); Lucene opens a fresh NRT {@code DirectoryReader} per index inside the loop,
	 * so writes landing between two index iterations can appear in the later index but not the
	 * earlier one; Elasticsearch's wildcard search is shard-snapshot consistent at the search time.
	 * Consumers assembling LLM prompts from a chart with cross-document references (e.g. an obs's
	 * encounter pointer) must tolerate the referenced doc being absent in the same result; the
	 * three shapes converge to "everything indexed at some moment around the call," not "everything
	 * indexed at one atomic instant."
	 *
	 * <p><b>Mid-purge race.</b> A {@link #bulkDeleteByPatient(String)} call interleaved with an
	 * in-flight {@code findAllByPatient(patientUuid)} for the same patient can return a partial
	 * chart on the per-store-partial tiers (MySQL is protected by its single transaction; Lucene
	 * straddles the purge across per-index iterations; ES depends on shard-level timing). The
	 * returned partial chart contains real but stale records for a patient mid-purge — operationally
	 * significant when the purge is privacy-driven. Callers initiating a purge should not assume
	 * concurrent reads have already drained.
	 */
	List<QueryDocument> findAllByPatient(String patientUuid);

	/**
	 * Full-chart read with explicit loss information. Existing backends inherit a complete result;
	 * backends override this method when they know a cap or handled read failure may have omitted
	 * records.
	 */
	default PatientChartRead findPatientChart(String patientUuid) {
		return PatientChartRead.complete(findAllByPatient(patientUuid));
	}

	SearchResult bm25(SearchRequest req);

	SearchResult knn(SearchRequest req);

	/**
	 * Hybrid search. The default fuses {@link #bm25(SearchRequest)} and {@link #knn(SearchRequest)}
	 * via rank-based RRF (see {@link RankFusion}); the ES backend is permitted to override with
	 * native RRF when measurable benefit justifies the divergence. Pure BM25 fallback runs and kNN
	 * is silently skipped when EITHER the request carries no query vector OR the backend declares
	 * {@code !capabilities().supportsKnn()} — a BM25-only contributed backend therefore does not
	 * need to override {@code hybrid} or implement {@code knn}; it returns BM25 alone.
	 */
	default SearchResult hybrid(SearchRequest req) {
		SearchResult bm25 = bm25(req);
		if (req.getQueryVector() == null || !capabilities().supportsKnn()) {
			return bm25;
		}
		SearchResult knn = knn(req);
		return RankFusion.rrf(bm25, knn, req.getLimit());
	}

	// ---------- introspection ----------

	BackendCapabilities capabilities();

	HealthStatus health();
}
