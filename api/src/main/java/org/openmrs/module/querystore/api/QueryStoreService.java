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

import java.util.ArrayList;
import java.util.List;

import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.querystore.backend.BulkWriteResult;
import org.openmrs.module.querystore.backend.DocFailure;
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
	 * Indexes a batch of clinical records in one backend round-trip, routing each to its per-type
	 * index. Internal: invoked by the bootstrap dispatcher to avoid the per-document commit that
	 * dominates backfill wall-clock (one transaction/commit per record — see ADR Decision 3). The
	 * documents need not share a resource type; the backend groups them.
	 *
	 * <p>Returns a non-null {@link BulkWriteResult} whose {@link BulkWriteResult#getSucceeded()} is
	 * the count of confirmed writes and whose {@link BulkWriteResult#getFailures()} carries the
	 * per-doc {@link org.openmrs.module.querystore.backend.DocFailure}s. Like {@link #index}, the
	 * caller MUST consult the result rather than treating "did not throw" as success, and
	 * implementations MUST NOT return {@code null}. An empty input list returns an empty result
	 * (zero requested, zero succeeded) without touching the backend. Throws
	 * {@link IllegalStateException} when the backend isn't wired.
	 *
	 * <p>A {@code default} so existing implementers (test doubles, out-of-tree services) keep
	 * compiling and get a correct — if unbatched — fallback that loops {@link #index}; the production
	 * {@code QueryStoreServiceImpl} overrides it with a single batched backend round-trip, which is
	 * the throughput win the bootstrap needs.
	 */
	default BulkWriteResult bulkIndex(List<QueryDocument> documents) {
		if (documents == null || documents.isEmpty()) {
			return new BulkWriteResult(0, 0, new ArrayList<DocFailure>());
		}
		int succeeded = 0;
		List<DocFailure> failures = new ArrayList<DocFailure>();
		for (QueryDocument doc : documents) {
			WriteResult result = index(doc);
			if (result != null && result.isSucceeded()) {
				succeeded++;
			} else if (result != null) {
				failures.add(result.getFailure());
			} else {
				// index() MUST NOT return null (see its contract). Count a violation as a failure
				// rather than silently dropping it, so succeeded + failures.size() == requested holds.
				failures.add(new DocFailure(doc == null ? null : doc.getResourceType(),
				        doc == null ? null : doc.getResourceUuid(),
				        "index() returned null (SPI contract violation)", false));
			}
		}
		return new BulkWriteResult(documents.size(), succeeded, failures);
	}

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

	/**
	 * Returns every indexed document for the patient — no relevance ranking, no caller-supplied
	 * limit — ordered by {@code record_date} descending with {@code (resource_type, resource_uuid)}
	 * as the deterministic tie-breaker (ADR Decision 15). Backs the full-chart-to-LLM consumer
	 * pattern: the entire patient chart is passed to a model that does its own reasoning instead
	 * of a question-conditioned relevance retrieval. A tier-specific cap applies on Elasticsearch —
	 * see the operational caveats below.
	 *
	 * <p>Cold-patient side effect: identical to {@link #searchByPatient(String, String, int)} —
	 * when no documents are indexed for {@code patientUuid}, the call synchronously projects the
	 * patient's clinical data before returning. First touch on a never-indexed patient blocks for
	 * seconds while serialization and embedding run; subsequent calls return at steady-state
	 * latency. Same one-time cost paid by whichever method first touches the patient.
	 *
	 * <p><b>Sticky partial chart on first-touch failure.</b> If the per-patient projection succeeds
	 * for some resource types but throws for others (e.g. a transient core-DB hiccup during the
	 * Obs scan), the index ends up with a partial chart for that patient. The next call sees
	 * {@code existsByPatient() == true} (the succeeded types are present), short-circuits the
	 * cold-bootstrap path, and returns the partial chart without retrying the failed types. The
	 * patient stays under-projected until the global scheduled scan re-runs (or a write event for
	 * a failed-type resource triggers single-doc indexing). Operators monitoring querystore should
	 * watch for the "Per-patient projection of {type} for patient {uuid} failed" WARN log as the
	 * signal of an under-projected patient. A per-type-per-patient retry marker is tracked under
	 * the ADR's "Lazy per-patient projection" open question.
	 *
	 * <p>Token-budget enforcement is the consumer's responsibility — v1 returns whatever the index
	 * holds. A patient with a decade of vitals can produce thousands of documents totalling
	 * megabytes of text; downstream recency caps or per-type filtering live in prompt assembly,
	 * not here.
	 *
	 * <p><b>ES tier caveat (v1):</b> the Elasticsearch backend issues a single wildcard search
	 * with {@code size = 10 000} (the default {@code max_result_window}); patients with more
	 * documents see the most-recent slice and the older tail is silently dropped at the backend
	 * with a WARN log. MySQL and Lucene are unbounded. Tier-agnostic consumers should expect
	 * truncation above ~10 000 docs on ES until PIT + {@code search_after} pagination lifts the cap
	 * (deferred to v1.1). The LLM full-chart consumer that motivates this method tops out at
	 * context-window size well below 10 000 documents, so the cap is operationally invisible to
	 * the documented use case.
	 */
	@Authorized(PrivilegeConstants.GET_PATIENTS)
	List<QueryDocument> getPatientChart(String patientUuid);
}
