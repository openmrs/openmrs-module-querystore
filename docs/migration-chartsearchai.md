# Migrating chartsearchai's Elasticsearch pipeline to querystore

> **Status: historical and superseded.** This captures the migration assumptions that preceded the
> current service split. ChartSearchAI is a thin OpenMRS authorization, session, persistence, and
> streaming relay; med-agent-hub owns clinical context assembly and the clinical answer stages.

Notes capturing what came out of the design discussion, for the team that takes on the migration. The architectural decisions live in [adr.md](./adr.md); this doc is the chartsearchai-specific gap analysis.

## What chartsearchai uses Elasticsearch for today

- Single shared index `chartsearchai-patient-records` per deployment.
- Patient records (encounter, obs, condition, diagnosis, allergy, order, program enrollment, medication dispense) stored as documents with a `text` field and a `dense_vector` field.
- Hybrid search via Elasticsearch's RRF retriever combining BM25 and kNN (auto-detects OpenSearch vs. Elasticsearch).
- Indexed lazily on first patient access, plus a bulk backfill task and AOP hooks on save methods for incremental updates.
- Embedding model: `all-MiniLM-L6-v2` (384-dim, monolingual English, ~256-token cap).
- Patient access gated by an "AI Query Patient Data" privilege plus OpenMRS patient access checks.
- Best-effort sync — errors on the indexing path are logged and swallowed. Falls back to the in-process embedding pipeline if Elasticsearch is unreachable.
- Two consumer-facing retrieval paths: the question-conditioned hybrid retrieval above (preFilter), plus a full-chart path that sends every record for a patient to the LLM unfiltered. The full-chart path is backed today by `ChartCache` / `ChartCacheInvalidator`, which serialize via `PatientRecordLoader` and invalidate on writes.

## What's structurally compatible

| chartsearchai needs | querystore provides | Notes |
|---|---|---|
| Per-resource text + embedding documents | `text` + `embedding` fields per [Decision 6](./adr.md#decision-6-document-model--text-embeddings-and-structured-metadata) | querystore documents are denser (more structured metadata) |
| Hybrid BM25 + kNN | Both indexed; consumer issues RRF or other fusion at query time | [Decision 6](./adr.md#decision-6-document-model--text-embeddings-and-structured-metadata) |
| Patient-scoped queries | `patient_uuid` on every doc | |
| Resource citation | `resource_type` + `resource_uuid` | |
| Recency / date filtering | `record_date` field on every doc | [Decision 7](./adr.md#decision-7-date-separation--excluded-from-embeddings-included-at-query-time) |
| Locale-aware concept names | [Decision 8](./adr.md#decision-8-locale-specific-serialization-with-multilingual-embeddings) | |
| Voiding handling | [Decision 10](./adr.md#decision-10-voided-records--deleted-from-the-read-store-not-marked) | |
| Encounter-scoped events | [Decision 12](./adr.md#decision-12-sync-mechanism--events-first-aop-as-last-resort-gap-filler) + gap inventory | |
| Indexed types: obs, conditions, diagnoses, orders, allergies, programs, dispense | [Decision 6](./adr.md#decision-6-document-model--text-embeddings-and-structured-metadata) example documents | querystore is a superset (also patient, encounter, visit) |
| Full-chart-to-LLM path (currently `ChartCache` + `PatientRecordLoader`) | `QueryStoreService.getPatientChart(patientUuid)` per [Decision 15](./adr.md#decision-15-full-chart-retrieval--unfiltered-per-patient-enumeration) | Returns every indexed document for the patient, `record_date` desc, no relevance filtering. The chartsearchai-side cache + invalidator disappear; querystore's sync pipeline keeps the data current |

## What blocks migration today

Authorization and the v1 consumer API surface are settled by [Decision 14](./adr.md#decision-14-authorization-and-consumer-api-surface): chartsearchai's "AI Query Patient Data" privilege stays a chartsearchai-side concern, and querystore enforces core's per-patient access checks via `PatientService.getPatient` on its Java service boundary. These querystore [open questions](./adr.md#open-questions) are tracked before chartsearchai can switch (item 1, Patient merge, is now resolved):

1. **[Patient merge handling](./adr.md#patient-merge-handling)** — *Resolved.* chartsearchai handles this via AOP today; querystore instead rides the incidental `SaveServiceEvent<PersonMergeLog>` that `mergePatients` fires (core emits no dedicated merge event), reconciling with the same shape chartsearchai uses — re-index the surviving patient + delete-by-patient the merged-away one.
2. **[Initial backfill / bootstrap](./adr.md#initial-backfill--bootstrap)** — chartsearchai indexes lazily on first chart access plus a bulk task. querystore must specify a clear bootstrap path before it can replace that.
3. **[Long-text chunking for embeddings](./adr.md#long-text-chunking-for-embeddings)** — chartsearchai's MiniLM has a 256-token cap; long obs are silently truncated. querystore must resolve chunking strategy before parity, or it inherits the same silent-truncation problem.
4. **[Sync reliability and reconciliation](./adr.md#sync-reliability-and-reconciliation)** — chartsearchai's "best-effort, swallow errors" model is acceptable for an in-process index but not for a shared read store. Production-grade event sync needs durable subscription, dead-letter handling, and reconciliation.

## Structural deltas chartsearchai will absorb

These are not querystore gaps — they are migration work on the chartsearchai side.

1. **Per-type indices vs. single shared index.** querystore is `querystore_obs`, `querystore_condition`, etc. ([Decision 4](./adr.md#decision-4-per-type-indices-over-a-single-index)). chartsearchai's retrieval layer either queries the wildcard `querystore_*` (recommended; promoted to the official cross-type convention by [Decision 13](./adr.md#decision-13-module-extension-spi-service-provider-interface-for-custom-resource-types)) or updates to multi-index queries.
2. **Embedding model alignment.** querystore embeds at index time with the model picked under [Decision 8](./adr.md#decision-8-locale-specific-serialization-with-multilingual-embeddings) (multilingual-e5 class). chartsearchai must switch its query-time embedder to the same model — the embedding-model contract from [Decision 13](./adr.md#decision-13-module-extension-spi-service-provider-interface-for-custom-resource-types) applies to every consumer. Embeddings from different models are not comparable; mismatch produces silently broken kNN results.

## Backend tier selection

Querystore's backend is pluggable per [Decision 3](./adr.md#decision-3-pluggable-backend-spi-with-three-reference-implementations) — MySQL, embedded Lucene, or Elasticsearch — selected via the `querystore.backend` configuration property. chartsearchai's current four pipelines map onto this:

| chartsearchai pipeline today | Closest querystore tier | Notes |
|---|---|---|
| `embedding` (MySQL embeddings + in-process keyword scoring) | `mysql` | querystore improves on this by using MySQL FULLTEXT for keyword rather than in-process scoring |
| `lucene` (Lucene-only BM25, no embeddings) | not directly available | querystore's `lucene` tier always carries both BM25 and brute-force cosine kNN (Lucene 8.x has no native HNSW; see [ADR Decision 3](./adr.md#decision-3-pluggable-backend-spi-with-three-reference-implementations) Consequences) — there is no embeddings-free option |
| `hybrid` (MySQL embeddings + Lucene BM25, in-process RRF) | `lucene` | querystore stores both vectors and text in Lucene rather than splitting across MySQL and Lucene; functionally equivalent at the consumer layer |
| `elasticsearch` (single shared ES index, RRF retriever) | `elasticsearch` | querystore restructures into per-type indices but keeps RRF fusion on the same retriever path |

When chartsearchai migrates, each deployment picks one querystore tier. chartsearchai itself does not need to know which tier is running, because [Decision 14](./adr.md#decision-14-authorization-and-consumer-api-surface)'s `QueryStoreService` is tier-agnostic — the same Java API call returns results regardless of backend.

CI eval implications: chartsearchai's existing eval dataset (153 records, query-recall benchmarks) was developed against ES-class hybrid retrieval. Running it against the default MySQL tier will report systematically lower recall/precision than ES, masking real regressions. The migration team must pick the eval tier explicitly — same as production, ES-pinned for fidelity, or all three for parity — when wiring up the eval suite against querystore. (See Decision 3 Consequences for the tier-drift concern.)

## What stays in chartsearchai unchanged

All query-time logic stays where it is — orthogonal to whether the index lives in chartsearchai's process or in querystore:

- Adaptive filtering: gap detection, similarity ratio, z-score gate, coherence filter, type boost.
- Post-retrieval scoring and absent-data detection.
- LLM inference (local llama-server or remote OpenAI-compatible API).
- Prompt assembly, citation formatting, recency cap, input validation.
- Streaming SSE API.
- Audit log and rate limiting.

These are layered above the index. The migration touches retrieval (where the index lives) but not reasoning.

## Suggested migration order

1. Resolve querystore open questions 1–4 above — design and ship.
2. chartsearchai switches its query-time embedding model to querystore's choice (test fixture and config change; small).
3. chartsearchai swaps both retrieval paths to the `QueryStoreService` Java surface ([Decision 14](./adr.md#decision-14-authorization-and-consumer-api-surface)): the preFilter path uses `search` / `searchByPatient` against `querystore_*`; the full-chart-to-LLM path uses `getPatientChart` ([Decision 15](./adr.md#decision-15-full-chart-retrieval--unfiltered-per-patient-enumeration)).
4. chartsearchai removes its AOP indexing code, the `chartsearchai-patient-records` index, and the `ChartCache` / `ChartCacheInvalidator` pair (the latter superseded by `getPatientChart` per Decision 15).
5. Validate with chartsearchai's existing eval dataset (153 records, query-recall benchmarks) against the new pipeline.
