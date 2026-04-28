# Migrating chartsearchai's Elasticsearch pipeline to querystore

Notes capturing what came out of the design discussion, for the team that takes on the migration. The architectural decisions live in [adr.md](./adr.md); this doc is the chartsearchai-specific gap analysis.

## What chartsearchai uses Elasticsearch for today

- Single shared index `chartsearchai-patient-records` per deployment.
- Patient records (encounter, obs, condition, diagnosis, allergy, order, program enrollment, medication dispense) stored as documents with a `text` field and a `dense_vector` field.
- Hybrid search via Elasticsearch's RRF retriever combining BM25 and kNN (auto-detects OpenSearch vs. Elasticsearch).
- Indexed lazily on first patient access, plus a bulk backfill task and AOP hooks on save methods for incremental updates.
- Embedding model: `all-MiniLM-L6-v2` (384-dim, monolingual English, ~256-token cap).
- Patient access gated by an "AI Query Patient Data" privilege plus OpenMRS patient access checks.
- Best-effort sync — errors on the indexing path are logged and swallowed. Falls back to the in-process embedding pipeline if Elasticsearch is unreachable.

## What's structurally compatible

| chartsearchai needs | querystore provides | Notes |
|---|---|---|
| Per-resource text + embedding documents | `text` + `embedding` fields per [Decision 6](./adr.md#decision-6-document-model--text-embeddings-and-structured-metadata) | querystore documents are denser (more structured metadata) |
| Hybrid BM25 + kNN | Both indexed; consumer issues RRF or other fusion at query time | [Decision 6](./adr.md#decision-6-document-model--text-embeddings-and-structured-metadata) |
| Patient-scoped queries | `patient_uuid` on every doc | |
| Resource citation | `resource_type` + `resource_uuid` | |
| Recency / date filtering | `date` field on every doc | [Decision 7](./adr.md#decision-7-date-separation--excluded-from-embeddings-included-at-query-time) |
| Locale-aware concept names | [Decision 8](./adr.md#decision-8-locale-specific-serialization-with-multilingual-embeddings) | |
| Voiding handling | [Decision 10](./adr.md#decision-10-voided-records--deleted-from-the-read-store-not-marked) | |
| Encounter-scoped events | [Decision 12](./adr.md#decision-12-sync-mechanism--events-first-aop-as-last-resort-gap-filler) + gap inventory | |
| Indexed types: obs, conditions, diagnoses, orders, allergies, programs, dispense | [Decision 6](./adr.md#decision-6-document-model--text-embeddings-and-structured-metadata) example documents | querystore is a superset (also patient, encounter, visit, appointment) |

## What blocks migration today

Five querystore [open questions](./adr.md#open-questions) must be resolved before chartsearchai can switch:

1. **[Authorization](./adr.md#authorization)** — chartsearchai relies on the "AI Query Patient Data" privilege and OpenMRS patient access checks. Without an auth model in querystore, chartsearchai must apply access control client-side or migrating breaks privacy guarantees (HIV status, mental health, etc.).
2. **[Patient merge handling](./adr.md#patient-merge-handling)** — chartsearchai handles this via AOP today. querystore needs to define repointing / re-indexing behavior on merge events, including whether core emits the event at all.
3. **[Initial backfill / bootstrap](./adr.md#initial-backfill--bootstrap)** — chartsearchai indexes lazily on first chart access plus a bulk task. querystore must specify a clear bootstrap path before it can replace that.
4. **[Long-text chunking for embeddings](./adr.md#long-text-chunking-for-embeddings)** — chartsearchai's MiniLM has a 256-token cap; long obs are silently truncated. querystore must resolve chunking strategy before parity, or it inherits the same silent-truncation problem.
5. **[Sync reliability and reconciliation](./adr.md#sync-reliability-and-reconciliation)** — chartsearchai's "best-effort, swallow errors" model is acceptable for an in-process index but not for a shared read store. Production-grade event sync needs durable subscription, dead-letter handling, and reconciliation.

## Structural deltas chartsearchai will absorb

These are not querystore gaps — they are migration work on the chartsearchai side.

1. **Per-type indices vs. single shared index.** querystore is `openmrs_obs`, `openmrs_condition`, etc. ([Decision 4](./adr.md#decision-4-per-type-indices-over-a-single-index)). chartsearchai's retrieval layer either queries the wildcard `openmrs_*` (recommended; promoted to the official cross-type convention by [Decision 13](./adr.md#decision-13-module-extension-spi-service-provider-interface-for-custom-resource-types)) or updates to multi-index queries.
2. **Embedding model alignment.** querystore embeds at index time with the model picked under [Decision 8](./adr.md#decision-8-locale-specific-serialization-with-multilingual-embeddings) (multilingual-e5 class). chartsearchai must switch its query-time embedder to the same model — the embedding-model contract from [Decision 13](./adr.md#decision-13-module-extension-spi-service-provider-interface-for-custom-resource-types) applies to every consumer. Embeddings from different models are not comparable; mismatch produces silently broken kNN results.

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

1. Resolve querystore open questions 1–5 above — design and ship.
2. chartsearchai switches its query-time embedding model to querystore's choice (test fixture and config change; small).
3. chartsearchai swaps its retrieval layer to query `openmrs_*` against querystore.
4. chartsearchai removes its AOP indexing code and `chartsearchai-patient-records` index.
5. Validate with chartsearchai's existing eval dataset (153 records, query-recall benchmarks) against the new pipeline.
