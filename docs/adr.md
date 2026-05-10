# Architecture Decision Records

This document captures the key architectural decisions for the OpenMRS Query Store Module.

## Background

Originating discussion: [RFC: A separate read-optimized projection of OpenMRS clinical data](https://talk.openmrs.org/t/rfc-a-separate-read-optimized-projection-of-openmrs-clinical-data/49397) on OpenMRS Talk.

## Conventions

- **The ADR is in draft.** Decisions can be edited, renumbered, or removed in place while we iterate. The append-only and supersession conventions are suspended until the doc stabilizes (or the first non-trivial implementation lands against it).
- **Open questions are mutable.** Items in the [Open Questions](#open-questions) section are added when surfaced, refined as understanding sharpens, and removed when promoted to a numbered decision (or explicitly declared out of scope).

## Table of Contents

1. [CQRS Pattern — Separate Read Store from Transactional Database](#decision-1-cqrs-pattern--separate-read-store-from-transactional-database)
2. [Module, Not Core](#decision-2-module-not-core)
3. [Pluggable Backend SPI with Three Reference Implementations](#decision-3-pluggable-backend-spi-with-three-reference-implementations)
4. [Per-Type Indices Over a Single Index](#decision-4-per-type-indices-over-a-single-index)
5. [Plain Text Serialization Over JSON or FHIR](#decision-5-plain-text-serialization-over-json-or-fhir)
6. [Document Model — Text, Embeddings, and Structured Metadata](#decision-6-document-model--text-embeddings-and-structured-metadata)
7. [Date Separation — Excluded from Embeddings, Included at Query Time](#decision-7-date-separation--excluded-from-embeddings-included-at-query-time)
8. [Locale-Specific Serialization with Multilingual Embeddings](#decision-8-locale-specific-serialization-with-multilingual-embeddings)
9. [Coded Fields — Store Both UUID and Name](#decision-9-coded-fields--store-both-uuid-and-name)
10. [Voided Records — Deleted from the Read Store, Not Marked](#decision-10-voided-records--deleted-from-the-read-store-not-marked)
11. [Retired Metadata — Data References Preserved, Names Snapshotted](#decision-11-retired-metadata--data-references-preserved-names-snapshotted)
12. [Sync Mechanism — Events First, AOP as Last-Resort Gap Filler](#decision-12-sync-mechanism--events-first-aop-as-last-resort-gap-filler)
13. [Module Extension SPI (Service Provider Interface) for Custom Resource Types](#decision-13-module-extension-spi-service-provider-interface-for-custom-resource-types)
14. [Authorization and Consumer API Surface](#decision-14-authorization-and-consumer-api-surface)

[Open Questions](#open-questions)

---

## Decision 1: CQRS Pattern — Separate Read Store from Transactional Database

### Status
Accepted

### Context
OpenMRS core uses a normalized MySQL database optimized for transactional clinical workflows — recording observations, placing orders, managing patient programs. Consumers such as AI applications, analytics tools, and reporting systems need to query this data in ways that normalized transactional schemas are not designed for: full-text search, semantic similarity search, cross-patient aggregation, and large-scale scans.

Running these workloads against the production database risks degrading clinical workflow performance and requires complex queries that fight the normalized schema.

### Decision
Follow the Command Query Responsibility Segregation (CQRS) pattern. OpenMRS core owns the write side (source of truth). This module maintains a separate, denormalized read-side projection optimized for query workloads.

Data flows one way: from core to the query store. The query store is eventually consistent with the transactional database. Clinical events (obs created, order placed, condition updated, etc.) trigger synchronization via OpenMRS events or AOP.

**Self-sufficiency principle.** For any query pattern the read store claims to serve, it must answer the query without round-tripping to core. Core is permitted only for *content fetches* — binary attachments referenced by `value_complex_uri`, full historical/audit lookups the read store deliberately omits (current state only), or admin/system metadata indexed nowhere by design. Going to core to enrich a read-store result with missing fields, to fall back when a frequent query pattern isn't indexed, or to walk object graphs is a smell: the right response is to denormalize the field, decide whether to index the pattern, or model the graph as multi-hop ES queries — not to silently re-couple to core's transactional database. This rule is the test future open questions are evaluated against.

### Consequences
- Clinical workflows are unaffected by query workloads.
- The query store can be rebuilt from scratch at any time since core remains the source of truth.
- Consumers must tolerate eventual consistency — there is a brief delay between a write in core and its availability in the query store.
- Two systems must be kept in sync, adding operational complexity.
- Consumers gain a predictable contract: clinical and analytical queries are served by the read store; bytes and full audit history are fetched from core. New query patterns force an explicit decision (index it, declare it out of scope, or route to core for content) rather than implicit core fallback.

---

## Decision 2: Module, Not Core

### Status
Accepted

### Context
The query store could be implemented within openmrs-core or as a separate module.

### Decision
Implement as an OpenMRS module (`openmrs-module-querystore`), not within openmrs-core.

### Rationale
1. **Not every deployment needs it.** Adding query store infrastructure to core would impose unnecessary overhead on every deployment.
2. **Implementation flexibility.** The backing store could be Elasticsearch, a PostgreSQL read replica, or another technology. Core should not be coupled to a specific search/analytics infrastructure choice.
3. **Independent release cycle.** The module can evolve, upgrade dependencies, and ship fixes without waiting for a core release.
4. **Separation of concerns.** Core owns the write side. The query store is a read-side projection — a fundamentally different concern.
5. **Dependency isolation.** Search and analytics client libraries do not belong in core, where they would affect every module and deployment.

Core provides the hook points (events, AOP, service interfaces) that this module listens to for data changes. The module handles everything else.

### Consequences
- The module must depend on core's service interfaces and event system.
- Deployments that want query store capabilities must install an additional module.
- The module can be swapped or removed without affecting core.

---

## Decision 3: Pluggable Backend SPI with Three Reference Implementations

### Status
Accepted

### Context
The query store needs a backend that supports full-text search (keyword matching), semantic search (vector similarity), structured filtering (by patient, date, concept, etc.), and cross-patient aggregation.

OpenMRS deployments span a wide range of operational profiles. A small rural hospital may run OpenMRS on a single server with limited spare RAM and no infrastructure team to operate a separate search cluster. A large national or research-network deployment may already run Elasticsearch, expects cross-patient analytics over millions of records, and has dedicated operational staff. A single backend choice forces every deployment into the same operational profile — either over-provisioning the small site or under-serving the large one.

Candidate backends considered, with the workloads each handles well:

| Option | Full-text | Vector | Structured filter | Cross-patient scale | Extra infra |
|---|---|---|---|---|---|
| MySQL (reuse core's DB) | Limited (FULLTEXT) | No native support; brute-force in-process | Yes | Per-patient only; cross-patient is O(N) | None |
| Embedded Lucene (in-JVM) | Yes (BM25) | Yes (HNSW kNN since Lucene 9) | Limited (point/range) | Single-host only | None new |
| PostgreSQL + pgvector | Yes | Yes | Yes | Good | New database |
| Elasticsearch / OpenSearch | Yes | Yes (dense_vector / knn_vector + RRF) | Yes | Excellent | New service |
| Dedicated vector DB (Pinecone, Milvus, Qdrant) | No/limited | Yes | Limited | Excellent | New service |

The chartsearchai module — querystore's primary near-term consumer — already supports four selectable retrieval pipelines (`embedding | lucene | hybrid | elasticsearch`) backed by MySQL, Lucene, and Elasticsearch in different combinations. Querystore is being designed as a generic read store that can absorb that capability surface without locking deployments into a single tier.

### Decision
The backing store is pluggable via a Java SPI. Querystore depends on the SPI; it does not depend on a specific backend. Three reference implementations ship with the module, and a deployment selects one via an explicit configuration property (`querystore.backend=mysql|lucene|elasticsearch`). Default is `mysql` so that a fresh install adds no new external service or storage beyond core's existing database. (The in-JVM embedding runtime per [Decision 8](#decision-8-locale-specific-serialization-with-multilingual-embeddings) — model file plus inference library — runs across all tiers and is not what "no new infrastructure" refers to.)

| Tier | Backend | Footprint | Trade-offs accepted |
|---|---|---|---|
| Small | **MySQL** — vectors stored as binary in per-type tables inside core's existing database; brute-force in-process kNN; MySQL FULLTEXT for keyword search | No new external service or storage; reuses core's database | Cross-patient kNN is O(N) and not viable above modest scale; FULLTEXT keyword quality is below Lucene/ES BM25 |
| Medium | **Embedded Lucene** — local FSDirectory in the OpenMRS app-data directory; native BM25 + HNSW kNN; in-process RRF fusion | No new process; local disk only | Single-host; no replication; index loss requires rebuild from core; throughput bounded by one JVM |
| Large | **Elasticsearch / OpenSearch** — separate cluster; native `dense_vector`/`knn_vector` with HNSW; ES retriever or OS hybrid query for RRF fusion | Cross-server replication, cross-patient analytics at scale, native hybrid retrieval | ~1-2 GB+ baseline RAM for a separate JVM; new operational surface |

The SPI is a Java interface (`org.openmrs.module.querystore.backend.BackendStore`) that abstracts the per-type separation primitive (ES index / Lucene directory / MySQL table — see [Decision 4](#decision-4-per-type-indices-over-a-single-index)), document upsert, document delete, BM25 search, kNN search, hybrid search, bulk operations, and per-type schema/mapping creation.

Two invariants every backend must satisfy regardless of tier are pinned here, because they cross too many other decisions to defer:

1. **`resource_uuid`-keyed idempotency.** The same upsert applied twice produces the same stored document; the same delete applied twice is a no-op the second time. Required by event-handler idempotency under [Decision 12](#decision-12-sync-mechanism--events-first-aop-as-last-resort-gap-filler), where at-least-once delivery means the same event may arrive multiple times.
2. **Patient-scoped read at sub-linear cost.** Queries filtered by `patient_uuid` must not require a full-corpus scan. Required by [Decision 14](#decision-14-authorization-and-consumer-api-surface)'s per-patient query model and by chartsearchai's per-chart latency expectations.

#### Backend SPI contract

The SPI surfaces these operations:

- **Lifecycle.** `ensureSchema(resourceType, spec)` (idempotent, lazy table/index/mapping creation) and `deleteSchema(resourceType)` (full-rebuild support).
- **Writes.** `upsert(QueryDocument)` and `delete(resourceType, resourceUuid)`, both keyed by `resource_uuid` for idempotency. Bulk variants `bulkUpsert`, `bulkDelete`, and `bulkDeleteByPatient` cover backfill ([Initial backfill / bootstrap](#initial-backfill--bootstrap)), void-cascade handling, and patient-merge handling ([Patient merge handling](#patient-merge-handling)).
- **Reads.** `bm25(SearchRequest)` and `knn(SearchRequest)` return separately-ranked result sets; `hybrid(SearchRequest)` whose default fuses BM25 + kNN with rank-based RRF at the service layer. The Elasticsearch backend is permitted to override `hybrid` with native RRF when measurable benefit justifies the divergence.
- **Filters.** Structured filter pushdown — `Filter.term`, `Filter.in`, `Filter.range`, and the privileged `Filter.patientScope`. Backends report which filter kinds they support via `BackendCapabilities`.
- **Introspection.** `capabilities()` and `health()`.

**Hybrid fusion location: service layer.** All three reference backends route through service-layer RRF (k=60, rank-based) so backend-specific score distributions do not leak into the fused result. Native RRF is an opt-in override on the cluster tier; uniformity is the default. Tradeoff accepted: a measurable per-query latency penalty on the ES tier versus its native retriever, in exchange for cross-tier parity that the chartsearchai eval workflow can rely on.

**Score semantics: rank is canonical.** `Hit.rank` is 1-based, monotone, and the only cross-backend-comparable signal. `Hit.rawScore` is preserved for telemetry and debugging but is explicitly not comparable across backends or across queries on the same backend. Service-layer RRF consumes `rank` only.

**Bulk-write success semantics: per-document errors surfaced.** `BulkWriteResult` carries `totalRequested`, `succeeded`, and a list of `DocFailure` (resource type, UUID, error message, retryable flag). Per-document failures are never silently swallowed. The sync pipeline ([Decision 12](#decision-12-sync-mechanism--events-first-aop-as-last-resort-gap-filler)) and reconciliation ([Sync reliability and reconciliation](#sync-reliability-and-reconciliation)) rely on this contract.

**Transactional boundaries: durable + visible after return.** Every `upsert`/`delete`/`bulk*` returns only after the backend has acknowledged durability AND a subsequent search from the same JVM will see the write. The SPI does not expose transaction handles; "the write succeeded" means durable + visible. Backends choose how — MySQL uses a per-call transaction against its own session factory (not enrolled in core's transaction, to keep clinical writes from blocking on read-store writes); Lucene calls `commit()`; Elasticsearch sets `refresh=wait_for`. Throughput-oriented relaxation (e.g., batched ES flushes) is deferred to a future revision under the residual SPI open question.

**Capability negotiation.** `BackendCapabilities` declares `supportsKnn`, `supportsHybridNative`, `supportsCrossPatientKnnAtScale`, `recommendedMaxCorpusSize`, and the supported filter kinds. The service layer reads capabilities before dispatching, logs a warning when an operation is supported but not at the requested scale, and the backend either executes at whatever cost it can or throws `UnsupportedBackendOperationException` for hard misses. Specific corpus-size thresholds and admin-visible alerting policy are deferred to [Tier upgrade operational signals](#tier-upgrade-operational-signals).

**Aggregations are out of v1.** Counts, group-by, and other aggregation primitives are not on the SPI. The chartsearchai migration does not need them; future consumers (FHIR analytics, reporting modules) may surface a need, at which point an `aggregate(...)` method is additive.

**Module-contributed types.** Modules contributing custom resource types per [Decision 13](#decision-13-module-extension-spi-service-provider-interface-for-custom-resource-types) reach the SPI through the same `BackendStore` instance the core types use. They call `ensureSchema(<moduleid>_<type>, spec)` once at module startup and never see the underlying tier; this is what lets the MySQL backend grow new tables without runtime DDL outside `ensureSchema` and without per-module Liquibase changesets.

**MySQL backend column strategy.** The MySQL reference backend stores type-specific structured fields as a single JSON column (`metadata_json`) rather than per-type SQL columns. Reasons: module-contributed resource types must work via `ensureSchema(spec)` without querystore-side Liquibase, and a JSON column means one controlled `CREATE TABLE` per type with zero `ALTER`s as fields are added or contributed. MySQL 8 functional indexes on JSON paths cover hot-field filtering; reversible — fields that prove latency-sensitive can be projected into columns later without breaking the SPI.

The document model ([Decision 6](#decision-6-document-model--text-embeddings-and-structured-metadata)) is tier-invariant — the same fields are stored regardless of backend. The embedding pipeline ([Decision 8](#decision-8-locale-specific-serialization-with-multilingual-embeddings)) is also tier-invariant — same model, same vectors, same dimensions. Only the storage primitives, index/query throughput, and which advanced query patterns are viable differ across tiers.

PostgreSQL + pgvector and dedicated vector databases are not in the v1 reference set. They may be added later as additional SPI implementations, subject to the contract pinned in this decision being able to accommodate their transactional and query semantics — see the [Backend SPI contract — residual](#backend-spi-contract--residual) open question for the items still open. The three tiers shipped are picked to span the operational range from "no new infrastructure" to "cluster scale" with one clear option each.

### Rationale
1. **Small-deployment fit is a primary requirement, not an afterthought.** Forcing Elasticsearch on a single-server hospital is operationally untenable. The MySQL tier reuses the database that's already running and already backed up.
2. **Capability honesty over uniform pretense.** Making the tier choice explicit, and documenting what each tier omits (cross-patient kNN at scale, FULLTEXT vs BM25 quality, single-host vs replicated), lets deployments choose with eyes open rather than discovering limits at scale. The alternative — pretending all tiers are equivalent — produces silent quality degradation.
3. **Consumers don't need to know.** Per [Decision 14](#decision-14-authorization-and-consumer-api-surface), querystore's public surface is the Java service `QueryStoreService`. Consumers like chartsearchai never see the backend; they go through the same Java API regardless of tier. Backend choice is internal infrastructure.
4. **Leverages chartsearchai's existing backend code paths.** chartsearchai already supports backend selection via a global property, with MySQL, Lucene, and Elasticsearch all in production-tested code paths. Querystore's backend implementations build on those code paths as starting points, though the per-type structure ([Decision 4](#decision-4-per-type-indices-over-a-single-index)), structured-field document model ([Decision 6](#decision-6-document-model--text-embeddings-and-structured-metadata)), and events-first sync ([Decision 12](#decision-12-sync-mechanism--events-first-aop-as-last-resort-gap-filler)) are evolutions beyond chartsearchai's single-table-discriminator + AOP-only design — not direct ports.
5. **Designing pluggability up front is cheaper than retrofitting it.** Once consumer code is written against an ES-specific API, replacing the backend is invasive. An SPI from day one keeps backend choice deferrable indefinitely.
6. **Tier migration is a re-index, not a data migration.** Per [Decision 1](#decision-1-cqrs-pattern--separate-read-store-from-transactional-database), the read store is rebuildable from core. A deployment that outgrows the MySQL tier moves to Lucene or Elasticsearch by changing the config and rebuilding — no data export, no schema migration. Coordinates with the [Re-index / alias strategy](#re-index--alias-strategy) open question.

### Consequences
- chartsearchai's eval workflow ([migration doc](./migration-chartsearchai.md)) was developed against ES-class hybrid retrieval. Running CI evals on the default MySQL tier will report worse recall/precision than production ES sites would see, masking real regressions. Which tier the eval runs against in CI must be picked explicitly when the migration lands; it is not implied by the default backend choice.
- The module ships and maintains three backends. The SPI surface and each backend's correctness must be exercised in CI. Behavioral parity (same query → same result set, modulo score tie-breaking) is part of the test contract; tier-specific capability gaps are explicitly enumerated by `BackendCapabilities`, not silently tolerated.
- Cross-patient kNN scales differently across tiers and degrades smoothly rather than failing hard. MySQL brute-force is O(N) per query — fine below roughly 100k records, painful past a few million. Embedded Lucene's HNSW kNN works on a single host but is bounded by one JVM's throughput. Only the Elasticsearch tier handles cross-patient kNN at multi-million-document scale with cluster-wide replication. The numbers are approximate; deployments should measure their own corpus.
- Keyword search quality varies by tier. MySQL FULLTEXT is weaker than Lucene/ES BM25 for clinical text (no concept-aware tokenization, weaker term-frequency model). The MySQL tier is functional for keyword search but not optimal.
- The events-first sync pipeline ([Decision 12](#decision-12-sync-mechanism--events-first-aop-as-last-resort-gap-filler)) writes to the read store through the backend SPI, so sync code is backend-agnostic — switching tiers does not require rewriting event handlers.
- The SPI is part of querystore's *internal* contract. It is not a public extension surface — modules that want to plug in a custom backend can, but querystore does not commit to backward compatibility on the SPI the way it does on the consumer-facing `QueryStoreService` ([Decision 14](#decision-14-authorization-and-consumer-api-surface)).
- The `openmrs_<type>` naming convention from [Decision 4](#decision-4-per-type-indices-over-a-single-index) applies uniformly across all three backends.
- Operational documentation must cover all three tiers — install steps, sizing guidance, and "when to upgrade tier" signals; the latter is tracked as the [Tier upgrade operational signals](#tier-upgrade-operational-signals) open question.

---

## Decision 4: Per-Type Indices Over a Single Index

### Status
Accepted

### Context
Data in OpenMRS spans multiple resource types: patients, encounters, visits, observations, conditions, diagnoses, drug orders, test orders, allergies, patient programs, and medication dispenses. These types have different fields and different query patterns. The data can be stored in a single store with a `resource_type` discriminator column/field, or in separate per-type stores.

### Decision
Use per-type stores (e.g., `openmrs_obs`, `openmrs_condition`, `openmrs_drug_order`, etc.) rather than a single mixed store. All names follow `openmrs_<resource_type>` — the suffix equals the document's `resource_type` exactly — so the prefix functions as a stable namespace for everything this module produces and routing code can derive the store name from any document mechanically.

The word "index" is used throughout this ADR as the generic term for the per-type separation primitive. It maps to the backend-specific construct under [Decision 3](#decision-3-pluggable-backend-spi-with-three-reference-implementations):

| Backend | "index" means |
|---|---|
| Elasticsearch / OpenSearch | An ES/OS index |
| Embedded Lucene | A separate Lucene `IndexWriter` / FSDirectory per type |
| MySQL | A separate table per type |

The naming convention is identical across all three.

### Rationale
1. **No sparse fields.** Each index contains only the fields relevant to its type. A single mixed store would carry empty drug order fields on every obs document and vice versa, wasting storage and slowing queries at scale. (chartsearchai today uses a single MySQL table `chartsearchai_embedding` with a `resource_type` discriminator and a single `text_content` column — querystore deliberately does not inherit that shape, because per-type structured fields per [Decision 6](#decision-6-document-model--text-embeddings-and-structured-metadata) require per-type schemas.)
2. **Better query performance.** Type-specific queries (e.g., "all patients with HbA1c above 7") only scan the relevant index rather than skipping irrelevant document types.
3. **Better relevance scoring.** BM25 term frequencies are computed per index. Mixing clinical notes, lab results, and drug orders dilutes term frequencies across unrelated document types, hurting search quality. This applies to Elasticsearch, OpenSearch, and Lucene; the MySQL FULLTEXT engine has analogous per-table statistics.
4. **Cross-type search is still easy.** Wildcard patterns (e.g., `openmrs_*` in ES/OS, multi-directory readers in Lucene, `UNION ALL` over `openmrs_*` tables in MySQL) allow querying across all types when needed, providing the same convenience as a single store.
5. **Future-proof for cross-patient search.** Per-patient chart search works fine with either approach since the patient_uuid filter narrows the scope. Cross-patient search at scale benefits significantly from type-specific indices on backends that support cross-patient kNN at scale (the Elasticsearch tier per [Decision 3](#decision-3-pluggable-backend-spi-with-three-reference-implementations)); the Lucene and MySQL tiers serve cross-patient BM25 and structured queries but not high-volume kNN.

#### Why the `openmrs_` prefix
The prefix scopes every store this module manages under a single namespace. This matters for four reasons, each of which applies across all three backends:

- **Namespace sharing.** Elasticsearch clusters are often shared with other applications, or with multiple OpenMRS environments (dev / staging / prod) on the same cluster. Generic names like `obs`, `patient`, or `report` would collide; `openmrs_` keeps OpenMRS data unambiguously identifiable. The same concern applies to the MySQL backend — querystore tables live in core's existing database alongside core's own tables, and the prefix prevents collisions there too.
- **Reliable wildcard target.** `openmrs_*` matches every index this module produces and nothing else, making it the natural target for cross-type retrieval (the convention promoted by [Decision 13](#decision-13-module-extension-spi-service-provider-interface-for-custom-resource-types)) without having to enumerate names or risk pulling in unrelated indices.
- **Templates and lifecycle policies.** ES/OS index templates apply by name pattern. A single template on `openmrs_*` propagates common settings — shard count, replicas, refresh interval, vector-index parameters — to every OpenMRS index, including module-contributed ones. Without the prefix, templates either get broader than intended or have to be maintained per name. The Lucene and MySQL backends apply per-type settings in code rather than via templates, but the same prefix-based addressing makes "all OpenMRS stores" enumerable.
- **Operational visibility.** A DBA scanning the cluster (or the database) sees at a glance which stores belong to OpenMRS. Useful for capacity planning, audit, and incident response.

### Consequences
- More stores to manage, though an index template (ES/OS) or shared schema-creation routine (Lucene/MySQL) can share common settings across all `openmrs_*` stores.
- Document writes must be routed to the correct store based on resource type. The backend SPI ([Decision 3](#decision-3-pluggable-backend-spi-with-three-reference-implementations)) abstracts this for querystore's service implementation; consumers reach the data through `QueryStoreService` per [Decision 14](#decision-14-authorization-and-consumer-api-surface) and never see the underlying store layout.
- Cross-type queries require backend-specific multi-store syntax (multi-index search in ES/OS, multi-directory reader in Lucene, `UNION ALL` in MySQL). The backend SPI normalises this for the service layer; consumers issue type-agnostic queries against `QueryStoreService` and receive uniform results regardless of tier.

---

## Decision 5: Plain Text Serialization Over JSON or FHIR

### Status
Accepted

### Context
Clinical records must be serialized into a text representation that is stored in the read store and used for two purposes: (1) as input to embedding models for vector generation, and (2) as content read by LLMs when generating answers. The format options are plain labeled text, JSON, XML, or FHIR JSON.

### Decision
Serialize clinical records as labeled plain text.

Example:
```
Drug order: Metformin 500mg. Dose: 1.0 Tablet(s) Oral twice daily.
Duration: 30 Day(s). Quantity: 60.0 Tablet(s). Action: NEW. Urgency: ROUTINE
```

### Rationale
1. **Token efficiency.** Plain text uses roughly half the tokens of JSON and a third of FHIR JSON for the same clinical content. This matters when assembling multiple records into an LLM context window.

    | Format | Example | Approximate tokens |
    |---|---|---|
    | Plain text | `Condition: Diabetes. Status: ACTIVE` | ~8 |
    | JSON | `{"type":"condition","name":"Diabetes","status":"ACTIVE"}` | ~18 |
    | FHIR JSON | Full FHIR Condition resource | ~30+ |

2. **Better embeddings.** Embedding models are trained on natural language. They produce higher quality vectors from prose-like text than from structured formats with braces, brackets, and delimiters.
3. **LLM-friendly.** Labeled plain text is easy for language models to read and reason over. Field labels (e.g., `Dose:`, `Status:`, `Severity:`) provide sufficient structure without delimiter overhead.
4. **Concepts resolved to names.** Serialized text uses human-readable concept names (e.g., "Fasting blood glucose") rather than concept IDs or codes, improving both embedding quality and LLM comprehension.

### Consequences
- A serializer must be implemented for each clinical resource type.
- The plain text format is less machine-parseable than JSON — consumers that need structured access should use the structured metadata fields in the read-store document, not parse the text.
- Changes to the serialization format require re-embedding and re-indexing affected records.

---

## Decision 6: Document Model — Text, Embeddings, and Structured Metadata

### Status
Accepted

### Context
Each document in the query store needs to serve multiple purposes: semantic search, keyword search, structured filtering, LLM answer generation, and linking back to the source record in OpenMRS.

### Decision
Each read-store document contains three components:

1. **Text chunk** — the plain text serialization of the clinical record.
2. **Vector embedding** — a dense vector computed from the text chunk, stored in the backend's vector field type (`dense_vector` in Elasticsearch, `knn_vector` in OpenSearch, a Lucene `KnnFloatVectorField`, or a binary column in MySQL — see [Decision 3](#decision-3-pluggable-backend-spi-with-three-reference-implementations)).
3. **Structured metadata** — typed fields for filtering and aggregation (patient_uuid, date, resource_type, resource_uuid, concept_name, and type-specific fields).

Example documents:

**Observation** (openmrs_obs index):
```json
{
  "patient_uuid": "8a7b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d",
  "resource_type": "obs",
  "resource_uuid": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "date": "2025-03-15",
  "text": "Fasting blood glucose: 11.2 mmol/L",
  "embedding": [0.023, -0.041, 0.078, ...],
  "concept_uuid": "3cd6f600-26fe-102b-80cb-0017a47871b2",
  "concept_name": "Fasting blood glucose",
  "concept_class": "Test",
  "synonyms": ["FBG", "Fasting glucose"],
  "value_numeric": 11.2,
  "value_coded_uuid": null,
  "value_coded_name": null,
  "value_text": null,
  "value_datetime": null,
  "value_boolean": null,
  "value_complex_uri": null,
  "value_complex_handler": null,
  "units": "mmol/L",
  "interpretation": "ABNORMAL",
  "status": "FINAL",
  "comment": null,
  "obs_group_uuid": null,
  "obs_group_concept_name": null,
  "encounter_uuid": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6e",
  "encounter_type_uuid": "e1f2a3b4-5c6d-7e8f-9a0b-1c2d3e4f5a6b",
  "encounter_type_name": "Adult Outpatient Visit",
  "visit_uuid": "f2a3b4c5-6d7e-8f9a-0b1c-2d3e4f5a6b7c",
  "form_uuid": "a3b4c5d6-7e8f-9a0b-1c2d-3e4f5a6b7c8d",
  "form_name": "Adult Outpatient Form",
  "location_uuid": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "location_name": "Kenyatta National Hospital",
  "provider_uuid": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
  "provider_name": "Dr. Ochieng"
}
```

**Group obs member** (openmrs_obs index, member of a vital signs panel — same shape as the obs example, with the group fields populated):
```json
{
  "patient_uuid": "8a7b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d",
  "resource_type": "obs",
  "resource_uuid": "ba8c1d3e-4f5a-6b7c-8d9e-0f1a2b3c4d5e",
  "date": "2025-03-15",
  "text": "Systolic blood pressure: 120 mmHg",
  "embedding": [0.027, -0.038, 0.062, ...],
  "concept_uuid": "5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
  "concept_name": "Systolic blood pressure",
  "concept_class": "Finding",
  "synonyms": ["SBP"],
  "value_numeric": 120,
  "value_coded_uuid": null,
  "value_coded_name": null,
  "value_text": null,
  "value_datetime": null,
  "value_boolean": null,
  "value_complex_uri": null,
  "value_complex_handler": null,
  "units": "mmHg",
  "interpretation": null,
  "status": "FINAL",
  "comment": null,
  "obs_group_uuid": "f1c2d3e4-5b6a-7d8c-9e0f-1a2b3c4d5e6f",
  "obs_group_concept_name": "Vital signs",
  "encounter_uuid": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6e",
  "encounter_type_uuid": "e1f2a3b4-5c6d-7e8f-9a0b-1c2d3e4f5a6b",
  "encounter_type_name": "Adult Outpatient Visit",
  "visit_uuid": "f2a3b4c5-6d7e-8f9a-0b1c-2d3e4f5a6b7c",
  "form_uuid": "a3b4c5d6-7e8f-9a0b-1c2d-3e4f5a6b7c8d",
  "form_name": "Adult Outpatient Form",
  "location_uuid": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "location_name": "Kenyatta National Hospital",
  "provider_uuid": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
  "provider_name": "Dr. Ochieng"
}
```

The stored `text` is just `"Systolic blood pressure: 120 mmHg"`, but the embedding above is computed from `"Vital signs — Systolic blood pressure: 120 mmHg SBP"` per the [Synonyms and group obs convention](#synonyms-and-group-obs-convention) below. No separate parent group-aggregate document is emitted; consumers cluster atomic hits by `obs_group_uuid` when group-level rendering or multivariate reasoning is needed.

**Condition** (openmrs_condition index):
```json
{
  "patient_uuid": "8a7b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d",
  "resource_type": "condition",
  "resource_uuid": "d4e5f6a7-8b9c-0d1e-2f3a-4b5c6d7e8f9a",
  "date": "2023-06-10",
  "text": "Condition: Type 2 Diabetes Mellitus. Status: ACTIVE. Verification: CONFIRMED. Onset: 2020-03-15",
  "embedding": [0.015, -0.062, 0.044, ...],
  "concept_uuid": "5cd3f6a0-26fe-102b-80cb-0017a47871b2",
  "concept_name": "Type 2 Diabetes Mellitus",
  "non_coded": null,
  "clinical_status": "ACTIVE",
  "verification_status": "CONFIRMED",
  "onset_date": "2020-03-15",
  "end_date": null,
  "additional_detail": null,
  "previous_version_uuid": null,
  "encounter_uuid": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6e",
  "encounter_type_uuid": "e1f2a3b4-5c6d-7e8f-9a0b-1c2d3e4f5a6b",
  "encounter_type_name": "Adult Outpatient Visit",
  "visit_uuid": "f2a3b4c5-6d7e-8f9a-0b1c-2d3e4f5a6b7c",
  "form_uuid": "a3b4c5d6-7e8f-9a0b-1c2d-3e4f5a6b7c8d",
  "form_name": "Adult Outpatient Form",
  "location_uuid": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "location_name": "Kenyatta National Hospital",
  "provider_uuid": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
  "provider_name": "Dr. Ochieng"
}
```

**Diagnosis** (openmrs_diagnosis index):
```json
{
  "patient_uuid": "8a7b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d",
  "resource_type": "diagnosis",
  "resource_uuid": "e5f6a7b8-9c0d-1e2f-3a4b-5c6d7e8f9a0b",
  "date": "2025-06-29",
  "text": "Diagnosis: Tuberculosis. Certainty: CONFIRMED. Rank: Primary",
  "embedding": [0.031, -0.019, 0.087, ...],
  "concept_uuid": "7ef4a8b2-36de-112b-90db-1127b58972c3",
  "concept_name": "Tuberculosis",
  "non_coded": null,
  "certainty": "CONFIRMED",
  "rank": "Primary",
  "condition_uuid": null,
  "encounter_uuid": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6e",
  "encounter_type_uuid": "e1f2a3b4-5c6d-7e8f-9a0b-1c2d3e4f5a6b",
  "encounter_type_name": "Adult Outpatient Visit",
  "visit_uuid": "f2a3b4c5-6d7e-8f9a-0b1c-2d3e4f5a6b7c",
  "form_uuid": "a3b4c5d6-7e8f-9a0b-1c2d-3e4f5a6b7c8d",
  "form_name": "Adult Outpatient Form",
  "location_uuid": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "location_name": "Kenyatta National Hospital",
  "provider_uuid": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
  "provider_name": "Dr. Ochieng"
}
```

**Drug Order** (openmrs_drug_order index):
```json
{
  "patient_uuid": "8a7b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d",
  "resource_type": "drug_order",
  "resource_uuid": "a7b8c9d0-1e2f-3a4b-5c6d-7e8f9a0b1c2d",
  "date": "2025-01-10",
  "text": "Drug order: Metformin 500mg. Dose: 1.0 Tablet(s) Oral twice daily. Duration: 30 Day(s). Quantity: 60.0 Tablet(s). Action: NEW. Urgency: ROUTINE. Take with food",
  "embedding": [0.042, -0.028, 0.053, ...],
  "concept_uuid": "9ab2c4d6-48ef-223c-a1eb-2238c69083d4",
  "concept_name": "Metformin",
  "drug_uuid": "f1a2b3c4-5d6e-7f8a-9b0c-1d2e3f4a5b6c",
  "drug_name": "Metformin 500mg",
  "dose": 1.0,
  "dose_units_uuid": "162384AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
  "dose_units": "Tablet(s)",
  "route_uuid": "160240AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
  "route": "Oral",
  "frequency_uuid": "160862AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
  "frequency": "twice daily",
  "duration": 30,
  "duration_units_uuid": "1072AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
  "duration_units": "Day(s)",
  "quantity": 60.0,
  "quantity_units_uuid": "162384AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
  "quantity_units": "Tablet(s)",
  "action": "NEW",
  "urgency": "ROUTINE",
  "dosing_instructions": "Take with food",
  "as_needed": false,
  "as_needed_condition": null,
  "num_refills": 0,
  "care_setting": "Outpatient",
  "previous_order_uuid": null,
  "order_number": "ORD-1234",
  "encounter_uuid": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6e",
  "encounter_type_uuid": "e1f2a3b4-5c6d-7e8f-9a0b-1c2d3e4f5a6b",
  "encounter_type_name": "Adult Outpatient Visit",
  "visit_uuid": "f2a3b4c5-6d7e-8f9a-0b1c-2d3e4f5a6b7c",
  "form_uuid": "a3b4c5d6-7e8f-9a0b-1c2d-3e4f5a6b7c8d",
  "form_name": "Adult Outpatient Form",
  "date_stopped": null,
  "auto_expire_date": "2025-02-09",
  "location_uuid": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "location_name": "Kenyatta National Hospital",
  "provider_uuid": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
  "provider_name": "Dr. Ochieng"
}
```

**Allergy** (openmrs_allergy index):
```json
{
  "patient_uuid": "8a7b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d",
  "resource_type": "allergy",
  "resource_uuid": "b8c9d0e1-2f3a-4b5c-6d7e-8f9a0b1c2d3e",
  "date": "2024-12-29",
  "text": "Allergy: Penicillin (drug allergen). Severity: Severe. Reactions: Anaphylaxis, Rash",
  "embedding": [0.018, -0.055, 0.071, ...],
  "allergen_uuid": "c2d3e4f5-6a7b-8c9d-0e1f-2a3b4c5d6e7f",
  "allergen_name": "Penicillin",
  "allergen_non_coded": null,
  "allergen_type": "DRUG",
  "severity": "Severe",
  "reactions": ["Anaphylaxis", "Rash"],
  "comment": null,
  "location_uuid": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "location_name": "Kenyatta National Hospital",
  "provider_uuid": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
  "provider_name": "Dr. Ochieng"
}
```

**Patient Program** (openmrs_program index):
```json
{
  "patient_uuid": "8a7b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d",
  "resource_type": "program",
  "resource_uuid": "c9d0e1f2-3a4b-5c6d-7e8f-9a0b1c2d3e4f",
  "date": "2024-01-15",
  "text": "Program: HIV Treatment. Enrolled: 2024-01-15. Status: Active. Current state: On ART",
  "embedding": [0.027, -0.038, 0.062, ...],
  "program_uuid": "a3b4c5d6-7e8f-9a0b-1c2d-3e4f5a6b7c8d",
  "program_name": "HIV Treatment",
  "enrollment_date": "2024-01-15",
  "completion_date": null,
  "active": true,
  "outcome_uuid": null,
  "outcome": null,
  "current_state_uuid": "b4c5d6e7-8f9a-0b1c-2d3e-4f5a6b7c8d9e",
  "current_state": "On ART",
  "location_uuid": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "location_name": "Kenyatta National Hospital",
  "provider_uuid": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
  "provider_name": "Dr. Ochieng"
}
```

**Medication Dispense** (openmrs_medication_dispense index):
```json
{
  "patient_uuid": "8a7b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d",
  "resource_type": "medication_dispense",
  "resource_uuid": "d0e1f2a3-4b5c-6d7e-8f9a-0b1c2d3e4f5a",
  "date": "2025-01-10",
  "text": "Dispensed: Metformin 500mg. Status: Completed. Quantity: 60.0 Tablet(s). Dose: 1.0 Tablet(s) Oral twice daily. Handed over: 2025-01-10",
  "embedding": [0.033, -0.047, 0.058, ...],
  "concept_uuid": "9ab2c4d6-48ef-223c-a1eb-2238c69083d4",
  "concept_name": "Metformin",
  "drug_uuid": "f1a2b3c4-5d6e-7f8a-9b0c-1d2e3f4a5b6c",
  "drug_name": "Metformin 500mg",
  "drug_order_uuid": "a7b8c9d0-1e2f-3a4b-5c6d-7e8f9a0b1c2d",
  "status": "Completed",
  "quantity": 60.0,
  "quantity_units_uuid": "162384AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
  "quantity_units": "Tablet(s)",
  "dose": 1.0,
  "dose_units_uuid": "162384AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
  "dose_units": "Tablet(s)",
  "route_uuid": "160240AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
  "route": "Oral",
  "frequency_uuid": "160862AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
  "frequency": "twice daily",
  "date_handed_over": "2025-01-10",
  "was_substituted": false,
  "substitution_type_uuid": null,
  "substitution_type": null,
  "substitution_reason_uuid": null,
  "substitution_reason": null,
  "dispenser_uuid": "c3d4e5f6-7a8b-9c0d-1e2f-3a4b5c6d7e8f",
  "dispenser_name": "Pharm. Wanjiku",
  "encounter_uuid": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6e",
  "encounter_type_uuid": "e1f2a3b4-5c6d-7e8f-9a0b-1c2d3e4f5a6b",
  "encounter_type_name": "Pharmacy Dispense",
  "visit_uuid": "f2a3b4c5-6d7e-8f9a-0b1c-2d3e4f5a6b7c",
  "form_uuid": "a3b4c5d6-7e8f-9a0b-1c2d-3e4f5a6b7c8d",
  "form_name": "Dispense Form",
  "location_uuid": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "location_name": "Kenyatta National Hospital",
  "provider_uuid": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
  "provider_name": "Dr. Ochieng"
}
```

**Test Order** (openmrs_test_order index):
```json
{
  "patient_uuid": "8a7b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d",
  "resource_type": "test_order",
  "resource_uuid": "e1f2a3b4-5c6d-7e8f-9a0b-1c2d3e4f5a6b",
  "date": "2025-06-29",
  "text": "Test order: X-Ray Chest. Laterality: LEFT. Clinical history: Persistent cough for 3 weeks. Action: NEW. Urgency: STAT",
  "embedding": [0.021, -0.034, 0.069, ...],
  "concept_uuid": "8bc3d5e7-59fg-334d-b2fc-3349d7a194e5",
  "concept_name": "X-Ray Chest",
  "action": "NEW",
  "urgency": "STAT",
  "laterality": "LEFT",
  "clinical_history": "Persistent cough for 3 weeks",
  "instructions": null,
  "specimen_source_uuid": null,
  "specimen_source_name": null,
  "care_setting": "Outpatient",
  "previous_order_uuid": null,
  "order_number": "ORD-5678",
  "encounter_uuid": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6e",
  "encounter_type_uuid": "e1f2a3b4-5c6d-7e8f-9a0b-1c2d3e4f5a6b",
  "encounter_type_name": "Adult Outpatient Visit",
  "visit_uuid": "f2a3b4c5-6d7e-8f9a-0b1c-2d3e4f5a6b7c",
  "form_uuid": "a3b4c5d6-7e8f-9a0b-1c2d-3e4f5a6b7c8d",
  "form_name": "Adult Outpatient Form",
  "date_stopped": null,
  "auto_expire_date": null,
  "location_uuid": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "location_name": "Kenyatta National Hospital",
  "provider_uuid": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
  "provider_name": "Dr. Ochieng"
}
```

**Patient** (openmrs_patient index):
```json
{
  "patient_uuid": "8a7b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d",
  "resource_type": "patient",
  "resource_uuid": "8a7b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d",
  "date": "2018-04-22",
  "text": "Patient: Achieng Otieno. Female. Born 1982-07-14. Address: Kibera, Nairobi, Kenya. Identifiers: MRN 100023, National ID 12345678",
  "embedding": [0.012, -0.054, 0.067, ...],
  "given_name": "Achieng",
  "middle_name": null,
  "family_name": "Otieno",
  "gender": "F",
  "birthdate": "1982-07-14",
  "birthdate_estimated": false,
  "age_years": 43,
  "dead": false,
  "death_date": null,
  "cause_of_death_uuid": null,
  "cause_of_death_name": null,
  "identifiers": [
    {
      "type_uuid": "a5d38e09-efcb-4d91-a526-50ce1ba5011a",
      "type_name": "MRN",
      "value": "100023",
      "preferred": true,
      "location_uuid": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d"
    },
    {
      "type_uuid": "b6e49f1a-fdcc-5e02-b637-61df2ca6022b",
      "type_name": "National ID",
      "value": "12345678",
      "preferred": false,
      "location_uuid": null
    }
  ],
  "addresses": [
    {
      "address1": null,
      "city_village": "Kibera",
      "state_province": "Nairobi",
      "postal_code": null,
      "country": "Kenya",
      "preferred": true
    }
  ],
  "attributes": [
    {
      "type_uuid": "c7f5a02b-0edd-6f13-c748-72e03db7033c",
      "type_name": "Telephone",
      "value": "+254712345678"
    }
  ]
}
```

**Encounter** (openmrs_encounter index):
```json
{
  "patient_uuid": "8a7b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d",
  "resource_type": "encounter",
  "resource_uuid": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6e",
  "date": "2025-03-15",
  "text": "Encounter: Adult Outpatient Visit at Kenyatta National Hospital. Provider: Dr. Ochieng (Clinician). Form: Adult Outpatient Form",
  "embedding": [0.019, -0.043, 0.058, ...],
  "encounter_type_uuid": "e1f2a3b4-5c6d-7e8f-9a0b-1c2d3e4f5a6b",
  "encounter_type_name": "Adult Outpatient Visit",
  "visit_uuid": "f2a3b4c5-6d7e-8f9a-0b1c-2d3e4f5a6b7c",
  "form_uuid": "a3b4c5d6-7e8f-9a0b-1c2d-3e4f5a6b7c8d",
  "form_name": "Adult Outpatient Form",
  "location_uuid": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "location_name": "Kenyatta National Hospital",
  "providers": [
    {
      "provider_uuid": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
      "provider_name": "Dr. Ochieng",
      "role_uuid": "d8e6b13c-1fee-7024-d859-83f14ec8044d",
      "role_name": "Clinician"
    }
  ]
}
```

**Visit** (openmrs_visit index):
```json
{
  "patient_uuid": "8a7b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d",
  "resource_type": "visit",
  "resource_uuid": "f2a3b4c5-6d7e-8f9a-0b1c-2d3e4f5a6b7c",
  "date": "2025-03-15",
  "text": "Visit: Outpatient at Kenyatta National Hospital. Indication: Routine follow-up for diabetes",
  "embedding": [0.024, -0.036, 0.061, ...],
  "visit_type_uuid": "e9f7c24d-30ff-8135-e96a-9402fd905155",
  "visit_type_name": "Outpatient",
  "start_date_time": "2025-03-15T09:30:00",
  "end_date_time": "2025-03-15T11:15:00",
  "active": false,
  "indication_uuid": "fab8d35e-4100-9246-fa7b-a513fea16266",
  "indication_name": "Routine follow-up for diabetes",
  "encounter_uuids": [
    "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6e"
  ],
  "location_uuid": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "location_name": "Kenyatta National Hospital",
  "attributes": [
    {
      "type_uuid": "0bc9e46f-5211-a357-0b8c-b624afb27377",
      "type_name": "Insurance Provider",
      "value": "NHIF"
    }
  ]
}
```

### Synonyms and group obs convention

Two cross-cutting conventions apply to documents that carry a primary clinical concept (obs, condition, diagnosis, drug_order, allergy, program, medication_dispense, test_order). Both decouple **stored shape** (what backends persist and consumers cite) from **embedding-input shape** (what the embedder sees), using the three-component model from this decision. Other example documents above omit `synonyms` for brevity, but their serializers populate it on the same rules.

**Synonyms.** Concept synonyms in the deployment's configured locale (per [Decision 8](#decision-8-locale-specific-serialization-with-multilingual-embeddings)) are kept on a `synonyms` field as a list of strings — capped per document for index size, deterministically sorted. The stored `text` field contains the preferred name only, keeping LLM citations clean. Synonyms enrich retrieval through two paths: (1) the indexer concatenates them onto the embedding input so vectors are synonym-aware (a "HTN" query hits a doc whose preferred name is "Hypertension" without forcing every consumer to either strip parenthetical noise from text or implement query-side synonym expansion); (2) backends BM25-index `synonyms` so exact-keyword matches on alternative terms are also handled.

**Group obs.** Obs that are members of a group are indexed as atomic documents with `obs_group_uuid` and `obs_group_concept_name` set on the metadata. No parent (group-aggregate) document is emitted. The group concept name is concatenated onto the embedding input — but not the stored `text` — so a group-level semantic query like "vitals" matches member vectors without the prefix appearing in citations or duplicating across siblings. Multivariate reasoning happens at response time: consumers (LLM-driven or otherwise) cluster atomic hits by `obs_group_uuid` to assemble group-level narratives or reason over sibling values together. This preserves member-level structured filtering (`value_numeric` etc.) and avoids the storage duplication, cascade-on-edit, and citation-ambiguity costs of materializing parent docs.

**Embedding input construction.** For a coded record, the embedding input is `[obs_group_concept_name + " — "] + text + [" " + synonyms.join(" ")]` (bracketed parts conditional on presence). The stored `text` is unaffected. An indexer that has access to the structured fields can reconstruct the embedding input deterministically; round-tripping `text` through the embedder is therefore *not* the contract.

### Serializer conventions

Patterns common to all type-specific serializers, distilled from the Obs and Condition implementations. These are contract-level rules; the abstract base implements the template-method shape that enforces them.

**Cross-cutting `date` source.** The cross-cutting `date` field uses the type's most clinically meaningful "this record was made" timestamp:
- Types with a clinical-event time field (e.g., `obs_datetime` on Obs) use that.
- Types without one (e.g., Condition) fall back to `dateCreated` (the audit field).
- Onset, end, resolution, and other clinical-fact dates remain on dedicated metadata fields per [Decision 7](#decision-7-date-separation--excluded-from-embeddings-included-at-query-time) — they are *not* the cross-cutting `date`.

**Free-text annotations are metadata-only.** Free-text clinician annotations — `comment` on obs, `additional_detail` on condition, and the equivalent on other types — are excluded from the stored `text` field but indexed as metadata for BM25 keyword matching. Citation-clean text is the contract; consumers that want the annotation render it from the metadata field at presentation time.

**Coded-or-free-text fallback.** For types whose primary concept is wrapped in `CodedOrFreeText` (Condition, Diagnosis), the serializer resolves the display name as: coded concept's preferred name when present; otherwise the trimmed `non_coded` string. The non-coded string is also stored on the `non_coded` metadata field. The display name (coded or non-coded) is what appears in `text`; `concept_uuid`/`concept_name`/`synonyms` are populated only on the coded path.

**Status enums.** OpenMRS status enums whose values are HL7-aligned (`Obs.Interpretation`, `Obs.Status`, `ConditionClinicalStatus`, `ConditionVerificationStatus`, etc.) are serialized via `enum.name()` directly. The HL7-aligned constants are stable across OpenMRS versions; downstream consumers query against the enum-name strings (e.g., `interpretation = "ABNORMAL"`).

**Diagnosis rank labelling.** `Diagnosis.rank` is an `Integer` rather than an enum. Core treats `1` as primary (its private `HibernateDiagnosisDAO.PRIMARY_RANK`); any other rank is non-primary. The serializer maps `1 → "Primary"` and any other non-null integer → `"Secondary"`; null is omitted from both `text` and metadata. The same labelled string appears in `text` (`Rank: Primary`) and on the `rank` metadata field — consumers filter and cite against the label, not the integer. This is the only integer-to-label mapping in the serializer layer; HL7-aligned enums use `enum.name()` per the rule above.

**Clinical-date labels in text.** Clinically significant dates appear in `text` as labeled clauses with stable label conventions: `Onset:` for onset, `Resolved:` for end/resolution, `Stopped:` for order discontinuation, `Enrolled:` for program enrollment. The labels are part of the `text` contract — they appear in LLM citations — so they are not free to reword per type.

**Skip semantics.** A serializer returns `null` from `serialize()` when the record produces no useful document — typically when the resolved display name is empty (e.g., an obs with no value, a condition with neither coded nor non-coded text, an obs group parent whose own value is empty). The caller (sync pipeline / backfill task) filters out null documents.

**Single-walk performance.** Each serializer walks its source record exactly once during `populate()`. The preferred concept name is resolved once and threaded into both the text composition and `putConceptFields` (rather than re-walking the names collection); date strings used in both `text` and metadata are formatted once and reused. This matters at bulk-backfill throughput where millions of records pass through the serializer.

### Field descriptions

| Field | Purpose |
|---|---|
| `patient_uuid` | Filter search to a single patient's chart, or aggregate across patients |
| `resource_type` | Distinguish record types (e.g., "obs", "condition", "diagnosis", "drug_order", "test_order", "allergy", "program", "medication_dispense", "patient", "encounter", "visit"); route documents to the correct per-type index |
| `resource_uuid` | Link back to the source record in OpenMRS (e.g., the obs UUID, condition UUID, order UUID, allergy UUID, patient UUID, encounter UUID, etc., depending on the resource_type) |
| `date` | Date range filtering and sorting (e.g., "labs from last 6 months", "most recent vital signs") |
| `text` | BM25 keyword search matches against it; the LLM reads it when generating answers. Embedding input is derived from `text` enriched with `obs_group_concept_name` and `synonyms` per the [Synonyms and group obs convention](#synonyms-and-group-obs-convention); stored `text` itself stays focused for clean citations and is not what the embedder sees |
| `embedding` | Dense vector for semantic similarity search (e.g., "blood sugar control" matching an HbA1c result) |
| `concept_uuid` | Exact filtering by concept without relying on text matching (e.g., "all HbA1c results for this patient") |
| `concept_name` | Human-readable concept name in the deployment's configured locale (see [Decision 8](#decision-8-locale-specific-serialization-with-multilingual-embeddings)); supports keyword search and display |
| `concept_class` | Filter by category of clinical data (e.g., "Test", "Drug", "Diagnosis") |
| `synonyms` | (records with a primary concept) Locale-aware concept synonyms as a list of strings, capped per document and deterministically sorted. BM25-indexed by backends so exact-keyword matches on alternative terms work; concatenated onto embedding input at index time for synonym-aware vectors. Excluded from `text` to keep LLM citations clean. See [Synonyms and group obs convention](#synonyms-and-group-obs-convention) |
| `value_numeric` | Numeric range queries (e.g., "HbA1c values above 7", "systolic BP over 140") |
| `value_coded_uuid` | Exact filtering by coded answer concept (e.g., "all HIV-positive results", "all Yes answers to a symptom question") — null for numeric or text obs |
| `value_coded_name` | Human-readable coded answer name for display and keyword search — null for numeric or text obs |
| `value_text` | Raw free-text observation value for substring search and display — null for numeric or coded obs |
| `value_datetime` | (obs) Date/time observation value (e.g., "Date of last menstrual period"); null for non-datetime obs |
| `value_boolean` | (obs) True/false observation value (e.g., "Pregnant: yes/no"); null for non-boolean obs |
| `value_complex_uri` | (obs) Pointer back to core's complex-obs storage for observations whose value is bound to a `complexHandler` (images, PDFs, audio/video, long free text). Null for non-complex obs. The binary content itself is not stored in the read store; consumers fetch it from core. See the *Complex obs handling* open question for per-handler treatment |
| `value_complex_handler` | (obs) Name of the OpenMRS `complexHandler` that produced the value (e.g., `ImageHandler`, `BinaryDataHandler`, `LongFreeTextHandler`, `MediaHandler`). Routes consumer-side rendering and signals which handler-specific extraction (if any) was applied at index time |
| `obs_group_uuid` | (obs) UUID of the parent obs when this obs is part of a group (e.g., a BP panel with systolic and diastolic children); null for ungrouped obs |
| `obs_group_concept_name` | (obs) Denormalized name of the group concept when this obs is part of a group; null for ungrouped obs. Concatenated onto embedding input (but not stored `text`) so group-level semantic queries match member vectors. See [Synonyms and group obs convention](#synonyms-and-group-obs-convention) |
| `status` | (obs / medication_dispense) Lifecycle state — for obs: FINAL / PRELIMINARY / AMENDED; for dispense: status of the dispense |
| `comment` | Free-text clinician note attached to the record (obs, allergy); supports BM25 search |
| `units` | Filter or group by unit of measurement |
| `interpretation` | Filter by clinical interpretation (e.g., "all abnormal results") |
| `non_coded` | (condition / diagnosis) Free-text label used when the clinician records a condition or diagnosis without selecting a concept; null when a coded `concept_uuid` is present |
| `additional_detail` | (conditions) Free-text annotation captured alongside the condition (e.g., site, severity narrative) |
| `previous_version_uuid` | (conditions) Links to a prior version of this condition record when a condition is edited; null for original entries |
| `condition_uuid` | (diagnoses) Links a diagnosis to its associated condition record when one exists; null otherwise |
| `encounter_uuid` | Group all clinical data from the same encounter; enables "what was recorded during visit X" queries across obs, orders, and diagnoses |
| `encounter_type_uuid` / `encounter_type_name` | (encounter-scoped records) Denormalized encounter type for filtering ("all admission obs", "all dispense events") without joining against the encounter index; UUID enables locale-independent filtering per [Decision 9](#decision-9-coded-fields--store-both-uuid-and-name) |
| `visit_uuid` | (encounter-scoped records) Denormalized visit pointer; lets you aggregate everything from one visit (which may span multiple encounters) without indirection through the encounter |
| `form_uuid` / `form_name` | (encounter-scoped records) Identifies which form captured the data; used for data-quality audits and form-scoped queries |
| `onset_date` | (conditions) Clinical date the condition started; distinct from `date` (the record creation date); included in the embedded text as a clinical fact per [Decision 7](#decision-7-date-separation--excluded-from-embeddings-included-at-query-time) |
| `order_number` | (orders) Human-readable order reference (e.g., ORD-1234) for display and linking back to source UI |
| `date_stopped` | (orders) Date an order was manually discontinued; null if still active; required for filtering active vs. stopped orders |
| `auto_expire_date` | (orders) Scheduled expiry date computed from duration; null if open-ended; required for filtering active vs. expired orders |
| `previous_order_uuid` | (orders) Links to the prior order in a revise/renew/discontinue chain; null for original orders. Required to reconstruct order history without scanning |
| `care_setting` | (orders) Inpatient vs. Outpatient setting; affects clinical interpretation of dose/frequency and is a common filter |
| `dose_units_uuid` / `dose_units` | (drug_order / medication_dispense) Coded unit for the dose amount (e.g., Tablet(s), mg, mL); UUID enables locale-independent filtering and resilience to concept renames per [Decision 9](#decision-9-coded-fields--store-both-uuid-and-name) |
| `quantity_units_uuid` / `quantity_units` | (drug_order / medication_dispense) Coded unit for the dispensed quantity; same UUID/name rationale as `dose_units` |
| `duration_units_uuid` / `duration_units` | (drug_order) Coded unit for order duration (e.g., Day(s), Week(s)); same UUID/name rationale as `dose_units` |
| `route_uuid` / `route` | (drug_order / medication_dispense) Coded administration route (e.g., Oral, Intravenous); UUID enables queries like "all injectable orders" without locale-dependent name matching |
| `frequency_uuid` / `frequency` | (drug_order / medication_dispense) Coded dosing frequency (e.g., twice daily, every 4 hours); UUID enables programmatic filtering of regimens that name-based matching can't reliably express across locales or deployments |
| `dosing_instructions` | (drug_order) Free-text directions to the patient (e.g., "Take with food"); included in the embedded text since it carries clinical meaning |
| `as_needed` / `as_needed_condition` | (drug_order) PRN flag and the condition under which the medication should be taken (e.g., "for pain"); critical to distinguish scheduled vs. PRN regimens |
| `num_refills` | (drug_order) Number of refills authorized; needed for adherence and supply-chain queries |
| `instructions` | (test_order) Free-text instructions to the lab/imaging team (e.g., "fasting required"); distinct from `clinical_history` which describes the patient's situation |
| `specimen_source_uuid` / `specimen_source_name` | (test_order) Specimen type for lab orders (e.g., "Whole blood", "Urine"); null for imaging or non-specimen orders |
| `reactions` | (allergies) Flat array of reaction names in the deployment's configured locale. Reaction UUIDs are intentionally omitted: reactions are always used as a refinement filter alongside `allergen_uuid`, never as the primary query axis (nobody queries "all patients with anaphylaxis" without first filtering by allergen or patient). Name-based matching is sufficient in this secondary role. The tradeoff accepted is that names are locale-dependent and mutable — if reaction-level UUID filtering becomes a real use case, adding a parallel `reaction_uuids` array is a serializer change plus a full re-index of `openmrs_allergy`; no schema migration or data loss is involved since the query store can be rebuilt from source at any time (see [Decision 1](#decision-1-cqrs-pattern--separate-read-store-from-transactional-database)). |
| `current_state_uuid` | (programs) UUID of the current program state concept; enables locale-independent exact filtering (e.g., "all patients currently On ART") per [Decision 9](#decision-9-coded-fields--store-both-uuid-and-name) |
| `outcome_uuid` | (programs) UUID of the program outcome concept; enables locale-independent exact filtering of completed program outcomes per [Decision 9](#decision-9-coded-fields--store-both-uuid-and-name) |
| `drug_order_uuid` | (medication_dispense) UUID of the originating drug order; enables "was this order dispensed?" queries and links dispense records back to their prescriptions |
| `was_substituted` | (medication_dispense) True when a different drug was dispensed than ordered (e.g., generic substitution); pairs with `substitution_type` and `substitution_reason` |
| `substitution_type_uuid` / `substitution_type` | (medication_dispense) Coded type of substitution (e.g., generic, therapeutic); null when `was_substituted` is false |
| `substitution_reason_uuid` / `substitution_reason` | (medication_dispense) Coded reason for the substitution (e.g., out of stock, formulary); null when `was_substituted` is false |
| `dispenser_uuid` / `dispenser_name` | (medication_dispense) Pharmacist or other staff who handed over the medication; distinct from `provider_uuid` (the prescribing clinician) |
| `given_name` / `middle_name` / `family_name` | (patient) Person name components; supports keyword search and display |
| `gender` | (patient) Filter by gender; codes follow OpenMRS conventions (e.g., M, F, O, U) |
| `birthdate` / `birthdate_estimated` | (patient) Date of birth and a flag indicating whether the date was estimated rather than known precisely; required for accurate age-based filtering |
| `age_years` | (patient) Pre-computed age at index time; convenient for "patients over 50" queries without date arithmetic. Note: this is a derived value and goes stale — clients that need point-in-time accuracy should compute from `birthdate` |
| `dead` / `death_date` / `cause_of_death_uuid` / `cause_of_death_name` | (patient) Mortality data; cause is a coded concept stored as UUID + name per [Decision 9](#decision-9-coded-fields--store-both-uuid-and-name) |
| `identifiers` | (patient) Array of identifier objects ({type_uuid, type_name, value, preferred, location_uuid}); enables exact-match lookup by MRN, national ID, etc. across types |
| `addresses` | (patient) Array of address objects with structured city/state/country fields; supports geographic filtering and aggregation |
| `attributes` | (patient / visit) Array of typed attribute objects ({type_uuid, type_name, value}); captures deployment-specific metadata (telephone, insurance, etc.) without hard-coding fields |
| `providers` | (encounters) Array of provider objects ({provider_uuid, provider_name, role_uuid, role_name}); encounters can have multiple providers in different roles, unlike single-provider events |
| `visit_type_uuid` / `visit_type_name` | (visits) Coded visit type (e.g., Outpatient, Inpatient); UUID enables locale-independent filtering per [Decision 9](#decision-9-coded-fields--store-both-uuid-and-name) |
| `start_date_time` / `end_date_time` | (visits) Full timestamp boundaries; visits are time-ranged rather than single-instant events. `end_date_time` is null when the visit is still active |
| `active` | (visits) Boolean computed from `end_date_time IS NULL`; redundant but enables faster filtering of active vs. closed visits |
| `indication_uuid` / `indication_name` | (visits) Coded reason for the visit; UUID enables locale-independent filtering per [Decision 9](#decision-9-coded-fields--store-both-uuid-and-name) |
| `encounter_uuids` | (visits) Array of encounter UUIDs that belong to this visit; lets a visit document be the entry point for traversal without a reverse lookup |
| `location_uuid` | Exact filtering by location, avoiding ambiguity from duplicate or similar location names |
| `location_name` | Human-readable location name for display, keyword search, and aggregation (e.g., "obs count per facility") |
| `provider_uuid` | Exact filtering by provider, avoiding ambiguity from duplicate or similar provider names |
| `provider_name` | Human-readable provider name for display, keyword search, and workload analysis |

### Rationale
Each component serves distinct purposes that the others cannot fulfill:

- **Text chunk** (`text`): BM25 keyword search matches against it. The embedding model was run on it. The LLM reads it when generating answers. Without it, you can find a match but have nothing to display or feed to the LLM.
- **Vector embedding** (`embedding`): Enables semantic similarity search. Without it, you can only do keyword matching.
- **Structured metadata** (all other fields): Enables precise filtering, sorting, aggregation, and linking back to source records. Neither keyword nor semantic search can reliably answer queries like "labs from last 6 months with value above 7 at Kenyatta National Hospital."

Elasticsearch and OpenSearch can combine all three in a single query — kNN on the vector, BM25 on the text, and filters on the metadata — making the three-component model a natural fit on the cluster tier. The Lucene and MySQL tiers achieve the same surface via service-layer RRF over separately-ranked BM25 and kNN results, as pinned in [Decision 3](#decision-3-pluggable-backend-spi-with-three-reference-implementations).

### Consequences
- Storage per document is larger than text-only or embedding-only approaches.
- Index mappings must be maintained for type-specific structured fields.
- Embedding generation adds a processing step during synchronization (either via an external API or a local model).

---

## Decision 7: Date Separation — Excluded from Embeddings, Included at Query Time

### Status
Accepted

### Context
Clinical records have observation dates (when the record was created). These dates are important for clinical reasoning (e.g., "most recent lab result") but affect embedding quality.

### Decision
Exclude observation dates from the text that is embedded. Include dates as structured metadata fields in the read-store document and prepend them to the text only at LLM prompt assembly time.

- **For embedding**: `"Fasting blood glucose: 11.2 mmol/L"`
- **For LLM prompt**: `"(2025-03-15) Fasting blood glucose: 11.2 mmol/L"`
- **For filtering**: `"date": "2025-03-15"` as a structured field

### Rationale
Two identical clinical observations recorded on different dates are semantically identical — they mean the same thing clinically. Including dates in the text would produce different embedding vectors for the same clinical content, reducing retrieval quality. A search for "abnormal blood sugar" should find all abnormal blood sugar results equally, regardless of when they were recorded.

Dates are still available through:
- The `date` metadata field for range filtering and sorting.
- Prepending at prompt assembly time when the LLM needs temporal context for reasoning.

**Exception**: Clinically significant dates that are distinct from the record timestamp should be included in the embedded text — for example, condition resolution dates, order discontinuation dates, or program enrollment dates. These represent clinical facts, not administrative timestamps.

### Consequences
- The serialization layer must distinguish between record timestamps (excluded from text) and clinically significant dates (included in text).
- Prompt assembly logic must prepend dates when constructing LLM input.
- Date-based retrieval relies on structured filtering rather than semantic search, which is more precise anyway.

---

## Decision 8: Locale-Specific Serialization with Multilingual Embeddings

### Status
Accepted

### Context
Concept names in OpenMRS are locale-specific. A concept like "Fasting blood glucose" may be stored as "Glycémie à jeun" in French or "Glukosa darah puasa" in Indonesian. The serialized text used for embedding and search must account for this, since:

- Embeddings are language-sensitive — mixing languages in the same index dilutes search quality.
- BM25 keyword search does not match across languages (e.g., "blood glucose" will not match "Glycémie à jeun").
- The LLM needs consistent language to reason over the chart.

Options considered:

| Option | Pros | Cons |
|---|---|---|
| Serialize in one fixed locale (e.g., English) | Standardized, best monolingual embedding quality | Clinicians may not search in English |
| Serialize in the deployment's default locale | Text matches the language clinicians use | Embedding quality varies by language with monolingual models |
| Serialize in multiple locales per record | Best retrieval across languages | Multiplies storage and indexing cost |
| Serialize in the deployment's locale, embed with a multilingual model | Practical, natural language for clinicians, cross-language similarity | Slightly lower embedding quality than monolingual models |

### Decision
Serialize concept names in the deployment's configured locale and use a multilingual embedding model (e.g., `multilingual-e5`) for vector generation.

**The embedding provider is an SPI.** Querystore depends on an `EmbeddingProvider` interface that exposes `embed(text) -> float[]`, `embedQuery(text) -> float[]` (for dual-encoder models like MedCPT where queries and documents use different encoders), and `getDimensions()`. Querystore ships a default ONNX-based provider, pluggable to any BERT-class model file via configuration (`querystore.embedding.modelFilePath`, `querystore.embedding.vocabFilePath`).

Deployments select the active provider via an explicit configuration property (`querystore.embedding.providerBean=<beanName>`); only one provider is active at a time. Modules may *register* an `EmbeddingProvider` bean — for example, a clinical-domain model packaged by an extension module — but cannot make their bean active. That selection is a deployment-level decision, consistent with [Decision 13](#decision-13-module-extension-spi-service-provider-interface-for-custom-resource-types)'s rule that modules do not pick the embedding model their text is embedded with. The default model packaged with querystore must satisfy the multilingual constraint stated in this decision; any replacement provider must as well. (The bean-name-based selection mechanism is a v1 simplification; a future revision may replace it with a more refactor-resistant qualifier annotation or registry approach if bean-name fragility becomes a real problem in practice.)

The embedding model identifier and its dimensions are part of the public contract surfaced through the SPI per [Decision 13](#decision-13-module-extension-spi-service-provider-interface-for-custom-resource-types) — every consumer that issues kNN queries must embed its query text with the same model querystore used at index time, or vectors are incomparable and results silently break.

### Rationale
1. **Clinicians search in their own language.** A French-speaking clinician will type "glycémie" not "blood glucose." The serialized text and BM25 index should match the language they use.
2. **Multilingual embedding models handle cross-language similarity.** Models like `multilingual-e5` are trained across 100+ languages and produce comparable vectors for semantically equivalent text regardless of language.
3. **Single serialization per record keeps storage and indexing simple.** Storing multiple locale variants per record would multiply storage cost and complicate synchronization without proportional benefit.
4. **Consistent with OpenMRS conventions.** OpenMRS already resolves concept names to the configured locale throughout its UI and APIs.
5. **Pluggable provider, fixed contract.** Different deployments have different inference budgets — a small site may want a 384-dim CPU-friendly model, a research network may want a clinical-domain model, a centralised deployment may proxy to a remote embedding API. The SPI lets all of these coexist with one document model. chartsearchai's `OnnxEmbeddingProvider` already proves this pattern works in practice (model-agnostic, dual-encoder support, dimension auto-detection); querystore inherits that approach rather than reinventing it.

### Consequences
- The embedding model must be multilingual. Monolingual models (e.g., English-only) should not be used.
- Deployments that change their default locale after initial indexing will need to re-serialize and re-index existing records.
- Cross-deployment searches (e.g., a research network spanning French and English sites) would require additional consideration, potentially storing an English canonical form alongside the localized text.
- Swapping the embedding provider is a re-index event — the new model produces different vectors for the same text, and existing kNN results become incomparable to fresh queries until the index is rebuilt. This couples to the [Embedding model versioning](#embedding-model-versioning) and [Re-index / alias strategy](#re-index--alias-strategy) open questions.
- The provider SPI is independent of the backend SPI ([Decision 3](#decision-3-pluggable-backend-spi-with-three-reference-implementations)). Choice of MySQL/Lucene/Elasticsearch does not constrain choice of embedding provider, and vice versa.

---

## Decision 9: Coded Fields — Store Both UUID and Name

### Status
Accepted

### Context
Many fields in the document model reference OpenMRS concepts — allergen, drug, program, program state, outcome, reaction, and others. Each can be represented as a UUID, a human-readable name, or both. The choice affects what query patterns are possible.

### Decision
For coded fields, store both the UUID and the human-readable name. Apply a narrow exception for small, stable, locale-invariant value sets where name-only is acceptable.

Examples following the rule:
- `allergen_uuid` + `allergen_name`
- `concept_uuid` + `concept_name`
- `drug_uuid` + `drug_name`
- `program_uuid` + `program_name`
- `current_state_uuid` + `current_state`
- `outcome_uuid` + `outcome`
- `value_coded_uuid` + `value_coded_name`

Examples following the exception (name only):
- `severity` in allergies — three stable values (Mild, Moderate, Severe) that are unlikely to vary by locale or change over time; programmatic filtering by severity UUID is not a realistic use case

### Rationale
UUID and name serve different consumers and different query patterns:

- **UUID** enables stable, locale-independent programmatic filtering. A developer querying "all patients currently On ART" writes a filter against `current_state_uuid` using the known concept UUID. This works regardless of what locale the deployment uses or whether the concept name is later updated.
- **Name** enables keyword search (BM25 matches against it) and human-readable display. A clinician searching "On ART" by text hits the name field.

Without the UUID, programmatic filtering must use name strings, which breaks when the deployment locale differs from the query or when concept names change. Without the name, keyword search and display require an extra lookup against OpenMRS core.

The exception applies when all three conditions hold: the value set is small (handful of values), the values are stable (unlikely to be renamed), and the values are locale-invariant (the same string is used across all deployments). Allergy severity meets all three. Most other coded fields do not.

### Consequences
- Every coded field requires two document fields instead of one.
- Serializers must resolve both the UUID and the locale-specific name for each coded value at index time.
- When adding a new coded field, the default should be to store both UUID and name unless the exception conditions are explicitly evaluated and met.

---

## Decision 10: Voided Records — Deleted from the Read Store, Not Marked

### Status
Accepted

### Context
OpenMRS uses logical deletion in its transactional database. When a record is voided, the row remains in the underlying tables with a `voided` flag set, preserving audit information on the write side. The read store must decide how to handle void events from core.

Three options were considered:

| Option | Behavior on void event | Trade-off |
|---|---|---|
| Delete from index | Document is removed from the read store | Simplest reads; no audit on read side |
| Keep with `voided` flag | Document stays; every query must filter `voided=false` | Audit available on read side; every consumer must remember the filter |
| Parallel audit index | Move voided documents to a sibling index (e.g., `openmrs_obs_voided`) | Clean separation; doubles index management |

### Decision
On a void event, delete the corresponding document from the read store. No `voided` field is stored on documents.

### Rationale
1. **Audit lives in core, not the projection.** Per [Decision 1](#decision-1-cqrs-pattern--separate-read-store-from-transactional-database), core remains the source of truth and the read store is rebuildable. The audit data model is already designed for the write side. Replicating audit responsibility into a query-optimized projection mixes concerns and makes the read store a partial, lossy copy of something core already does completely.
2. **Filter-everywhere is fragile.** Keeping voided records means every default query must include `voided=false`. A single forgotten filter surfaces clinically retracted data to a clinician or LLM. Deletion is a stronger guarantee than a convention.
3. **Voided records pollute semantic search.** Vectors for retracted records still match in kNN search unless filtered. A voided abnormal lab result could surface as a top semantic match for a query like "blood sugar control" and end up in an LLM prompt — a clinical safety problem, not just a quality issue.
4. **Storage cost.** Voided records consume index space (text + vector + structured metadata) for no read-side benefit, since the canonical record is preserved in core.

The brief eventual-consistency window between a void in core and its propagation to the read store is acceptable, consistent with the trade-off already accepted in [Decision 1](#decision-1-cqrs-pattern--separate-read-store-from-transactional-database). Idempotent delete-by-`resource_uuid` handles late-arriving and duplicate void events without special logic.

### Consequences
- Sync logic must handle void events explicitly, deleting by `resource_uuid` from the appropriate per-type index.
- Consumers needing audit or historical context (e.g., "who recorded a value that was later voided?") must query core directly. The read store cannot answer such questions and should not be expected to.
- Revision-chain pointers (`previous_version_uuid` on conditions, `previous_order_uuid` on orders) may dangle when an earlier version was voided rather than superseded — they will reference UUIDs no longer present in the read store. This is acceptable: the pointer's value is informational ("this revises an earlier record"), and full history retrieval belongs in core.
- "Show deleted" workflows for QA, debugging, or compliance review are not supported by the read store. If such a use case emerges later, it should be addressed by a separate, scoped decision (e.g., a parallel audit index or a time-bounded soft-tombstone) rather than by retrofitting `voided` onto every document.

---

## Decision 11: Retired Metadata — Data References Preserved, Names Snapshotted

### Status
Accepted

### Context
OpenMRS distinguishes two forms of logical removal:

- **Voided** applies to clinical data records (obs, orders, conditions, allergies, encounters, visits, patients). It means "this record was wrong, retract it." Handled by [Decision 10](#decision-10-voided-records--deleted-from-the-read-store-not-marked).
- **Retired** applies to metadata records (concepts, drugs, locations, providers, encounter types, visit types, forms, programs, services). It means "do not use this entry for new records, but historical references to it remain valid."

The two are not interchangeable. Retiring a concept does not invalidate the obs that reference it, in the same way that a clinician leaving a hospital does not invalidate the encounters they conducted while employed there. The read store needs an explicit policy for how retirement of referenced metadata propagates — or doesn't — to data documents.

### Decision
1. Data documents that reference retired or renamed metadata are kept unchanged. No data is removed, retracted, or rewritten in response to a retirement or rename event.
2. No `retired` flag is added to data documents.
3. Denormalized metadata names (e.g., `concept_name`, `location_name`, `provider_name`, `drug_name`, `encounter_type_name`, `form_name`, `program_name`, `service_name`, etc.) reflect the value at index time. They are not re-fetched when the underlying metadata is retired or renamed.
4. Direct metadata indices (e.g., a hypothetical `openmrs_concepts` for picker UX) are not in scope. If introduced later, their retirement-handling rules are a separate decision and will likely differ — those indices exist precisely to drive new-entry filtering, where retirement *is* a primary query axis. The broader question of which knowledge-base resources to project, when, and via what mechanism is tracked as the [Knowledge-base and reference-data projection](#knowledge-base-and-reference-data-projection) open question.

### Rationale
1. **Retirement is forward-looking, not retroactive.** Its purpose is to prevent future use of a metadata entry, not to invalidate historical references. A diabetes diagnosis recorded against a since-retired concept is still a real diagnosis — the patient was diagnosed, the record reflects what happened. Treating retirement as if it were voiding would silently rewrite clinical history.
2. **Adding a `retired` field gives consumers nothing actionable.** Unlike voided data (which must be hidden by default for safety), retired metadata is *expected* to appear in historical references. There is no default filter consumers should apply, so the field would only add storage cost and confusion.
3. **Denormalized names go stale on any metadata change.** Renames during retirement are one instance of a broader phenomenon — concept names, location names, and provider names all drift. The query store accepts eventual consistency on metadata names *and on the embeddings derived from them* per [Decision 1](#decision-1-cqrs-pattern--separate-read-store-from-transactional-database); re-indexing is the standard remedy when freshness matters. Treating retirement renames specially would create a one-off code path for a problem that already has a general solution.
4. **Auto fan-out on metadata-rename events is rejected on cost grounds.** The alternative to accepting staleness — subscribing to rename events and re-projecting every document that references the renamed UUID at sync time — turns rename events into potentially very large operational hammers. A single popular concept rename (e.g., a frequently-recorded diagnosis or vital sign) can touch millions of obs documents, each requiring text re-render and a fresh embedding. Folding that cost into steady-state sync was judged disproportionate to the freshness benefit, given that the staleness is recoverable by re-indexing on demand. This is the load-bearing reason the policy is "accept staleness," not an abstract preference for eventual consistency. The cost may be revisitable via the lighter-weight [Name-refresh re-projection](#name-refresh-re-projection) path tracked below; this decision does not foreclose it.
5. **Metadata-state queries belong in core.** Questions like "is this concept currently retired?" or "what concept replaced this retired one?" are metadata management concerns, not query workloads over clinical data. The read store is optimized for the latter; the former is core's job.

### Consequences
- Sync logic ignores retirement *and rename* events on metadata records. Only voiding (data records) and changes to indexed data trigger read-store mutations. Auto fan-out on metadata renames is rejected on cost grounds (see Rationale point 4); retirement is a no-op by virtue of "data references are preserved."
- Denormalized metadata names may be out of sync with core after a rename (whether retirement-driven or otherwise) until the next re-index. Consumers requiring authoritative metadata names must consult core or trigger a re-sync.
- Aggregations over denormalized names (e.g., "obs count grouped by `concept_name`") may attribute records to the pre-rename name. UUID-based aggregations are unaffected, which reinforces [Decision 9](#decision-9-coded-fields--store-both-uuid-and-name)'s rationale for storing both UUID and name.
- Stale denormalized names extend into the embedding vector. Document text is the input to embedding generation ([Decision 6](#decision-6-document-model--text-embeddings-and-structured-metadata)), so a rename leaves the *old* name baked into both the structured `*_name` field and the embedding vector. Semantic search against the new name will miss those documents until they are re-indexed. UUID-keyed queries and UUID-based aggregations are unaffected. The remedy named in this decision — a targeted re-index of the affected resource type — is itself an expensive operation (a full re-index of `openmrs_obs` in any decent-sized site is millions of documents of embedding-generation cost), so in practice renames may sit unfixed until the staleness accumulates enough to justify that cost. A lighter-weight path that touches only the documents referencing the renamed UUID is discussed in the [Name-refresh re-projection](#name-refresh-re-projection) open question.
- If direct metadata indices are added later, this decision does not apply to them. A separate decision should specify retirement handling for any such index, where keeping retired entries with a `retired=true` flag is the likely choice (the opposite of [Decision 10](#decision-10-voided-records--deleted-from-the-read-store-not-marked)'s approach to voided clinical data, because the use cases are inverted).

---

## Decision 12: Sync Mechanism — Events First, AOP as Last-Resort Gap Filler

### Status
Accepted

### Context
[Decision 1](#decision-1-cqrs-pattern--separate-read-store-from-transactional-database) establishes the CQRS pattern (separate read store, eventually consistent with core) and names "events or AOP" as candidate sync mechanisms without picking one. The choice cascades into coverage, coupling, latency, and operational complexity, and several other decisions implicitly depend on the answer.

Candidates considered:

| Mechanism | Coverage | Coupling | Async | Infra cost |
|---|---|---|---|---|
| OpenMRS Event module | Whatever core publishes | Loose — public event contract | Yes | Adds Event module + JMS broker |
| AOP (pointcuts on services) | Everything routed through service methods | Tight — internal service interfaces | Synchronous unless explicitly wrapped | None additional |
| Database CDC (Debezium / binlog) | Everything, including direct DAO writes | Tight — to core's DB schema | Yes | Requires MySQL binlog enabled and row-based/GTID replication; runtime can be embedded (Debezium Engine) or standalone |
| Polling | Anything queryable with a timestamp | Loose | N/A | None additional |

### Decision
Use the OpenMRS Event module as the primary sync mechanism. Subscribe to create / update / void events for the entity types this module indexes and apply the corresponding mutations to the read store.

Where event coverage is incomplete in the supported core version, the preferred remedy is to patch core to emit the missing event. AOP is permitted only as a targeted gap filler — scoped to specific entity types where patching core is not feasible — and is treated as tech debt to be removed once core catches up.

CDC and polling are excluded for the steady-state sync path. Polling is acceptable only for the initial backfill at install or after a rebuild (tracked as an open question below).

### Rationale
1. **Aligns with CQRS conventions.** Events are the canonical mechanism for read-side projections: core publishes, the projection consumes. [Decision 1](#decision-1-cqrs-pattern--separate-read-store-from-transactional-database)'s "data flows one way: from core to the query store" is most naturally implemented this way.
2. **Established precedent in the OpenMRS ecosystem.** The FHIR2 module already uses the Event module to maintain a derived representation of clinical data. Following the same pattern keeps the operational picture consistent for deployers and avoids introducing a new sync paradigm alongside an existing one.
3. **Decouples from internal service shapes.** AOP pointcuts attach to specific service method signatures; core renames, refactors, and method-extraction routinely break them. Events are a stable public contract that survives core's internal changes.
4. **Async by default.** Event handlers run off the clinical request thread, so indexing latency and embedding generation do not slow clinical workflows. AOP would block the calling thread (and its transaction) unless explicitly wrapped, adding complexity.
5. **Gaps are addressable upstream.** If the supported core version doesn't emit an event needed here, patching core is the right fix and benefits every projection that follows. Falling back to AOP for the remaining gaps preserves coverage without abandoning the events-first model for the entity types that already work.
6. **CDC trades loose code coupling for tight schema coupling.** Debezium tailing the MySQL binlog catches every write regardless of code path, and it does not require Kafka — the embedded Debezium Engine can run inside this module's JVM and write directly to Elasticsearch. The disqualifying problem is not infrastructure footprint but *what's being coupled to*: every column rename, table normalization, or schema migration in core silently breaks the CDC consumer, with no compile-time signal. Row-level deltas also have to be translated back into domain semantics (e.g., reconstructing "obs voided" from a row UPDATE setting `voided=1`), reproducing logic the service layer would have given for free. Enabling MySQL binlog in row-based / GTID mode is a non-trivial change for production DBAs to accept on a transactional clinical database. Together, these costs are disproportionate to the benefit for OpenMRS deployments. Can be revisited if scale or coverage demands force it.
7. **Platform-level CDC is a different proposition.** Adding Debezium to the Event module itself — tracked upstream as [TRUNK-6516](https://openmrs.atlassian.net/browse/TRUNK-6516) under the [TRUNK-6507](https://openmrs.atlassian.net/browse/TRUNK-6507) "Add integration middleware" epic — sidesteps the schema-coupling concern in (6) because core does the row-to-domain translation centrally. Querystore would continue to consume domain events as before, but those events would also cover writes that bypass the service layer today (Liquibase migrations, direct SQL). The events-first decision absorbs that upgrade without modification, and the [Sync reliability and reconciliation](#sync-reliability-and-reconciliation) gap narrows substantially when it lands.

### Consequences
- This module depends on the OpenMRS Event module and a JMS broker (ActiveMQ by default). Deployments must run this infrastructure.
- A gap inventory must be maintained: for each indexed resource type (obs, condition, diagnosis, drug_order, test_order, allergy, program, medication_dispense, patient, encounter, visit), record whether core emits create / update / void / purge events and at what granularity. Purge events are called out explicitly alongside void because cascading deletes (e.g., purging a patient triggers deletion of their obs, orders, encounters) are core's domain knowledge — the read store consumes whatever per-record events core fans out and does not reproduce cascade logic. A missing purge or cascaded-delete event is a coverage gap, not a read-store responsibility. Gaps drive either upstream PRs to core or a scoped AOP shim.
- Any AOP introduced as a gap filler must be documented with the entity type it covers, the core gap it works around, and a removal plan tied to a future core version.
- Event payloads in OpenMRS are often minimal (UUID + action). Handlers therefore fetch the full entity from core after receiving an event. This means the sync path performs reads against the transactional database — acceptable, but worth noting since it couples sync throughput to core's read performance.
- Lost events on broker restart are possible. Reliability, monitoring, and reconciliation are not solved by this decision and are tracked as separate open questions.
- The initial bootstrap / backfill mechanism is not specified here. The steady-state mechanism only handles changes from the moment the projection is running; getting from "empty index" to "in sync" is a separate concern, also tracked below.

---

## Decision 13: Module Extension SPI (Service Provider Interface) for Custom Resource Types

### Status
Accepted

### Context
OpenMRS's module ecosystem ships clinical data beyond what core defines: radiology reports, bed assignments, vaccination records, oncology regimens, custom form outputs, lab extensions, and many others. Consumers (clinical UIs, AI/analytics tools like Chart Search AI, reporting modules) want unified retrieval across all of this data, not just core types.

Three approaches were considered:

| Option | What querystore does | What modules do | Coupling |
|---|---|---|---|
| Closed scope (core types only) | Indexes core types | Ship their own per-module indices | None, but consumers must federate across multiple stores |
| Monolithic querystore | Indexes everything, knows every module's entities | Nothing | Querystore depends on every data-owning module |
| Module extension SPI | Provides shared infrastructure and a contribution interface | Contribute serializers, event subscribers, and own per-type index | Modules depend on querystore; querystore knows nothing about specific modules |

### Decision
Adopt the module extension SPI (Service Provider Interface) model. Querystore exposes a Java interface that data-owning modules implement to contribute custom resource types; querystore discovers implementations at runtime via the OpenMRS module loader and wires them into the indexing pipeline without ever depending on the contributing module directly. Each contributing module:

1. Declares a `resource_type` name namespaced by the contributing module's OpenMRS moduleid: `<moduleid>_<type>` (e.g., `radiology_report` from a module with moduleid `radiology`, `oncology_regimen` from `oncology`, `bedmgmt_assignment` from `bedmgmt`). The corresponding index name is `openmrs_<moduleid>_<type>`. This inherits OpenMRS's existing moduleid-uniqueness guarantee for free — no separate registry or governance process is needed — and matches how OpenMRS already namespaces global properties and event topics. Unprefixed names (`openmrs_obs`, `openmrs_condition`, `openmrs_patient`, etc.) are reserved for the types this module itself indexes from core; module contributions are always prefixed.
2. Provides a serializer that maps the entity to text + structured fields per [Decision 5](#decision-5-plain-text-serialization-over-json-or-fhir).
3. Provides an event subscriber per [Decision 12](#decision-12-sync-mechanism--events-first-aop-as-last-resort-gap-filler), or a scoped AOP shim where the module's services don't yet emit events. The same gap-inventory and tech-debt rules apply.
4. Receives a per-type index `openmrs_<resource_type>` under [Decision 4](#decision-4-per-type-indices-over-a-single-index)'s naming convention.

Querystore retains ownership of the shared infrastructure:
- Backend connection and per-type store lifecycle (creation, mapping/schema, alias management) — abstracted by the backend SPI per [Decision 3](#decision-3-pluggable-backend-spi-with-three-reference-implementations) so the contributing module is unaware which tier is running.
- The embedding pipeline ([Decision 8](#decision-8-locale-specific-serialization-with-multilingual-embeddings)). Modules provide text; querystore embeds. Modules may register an `EmbeddingProvider` bean per the SPI in [Decision 8](#decision-8-locale-specific-serialization-with-multilingual-embeddings), but selecting which provider is active is a deployment-level configuration, not a module-level decision — modules cannot silently override the model their text is embedded with.
- Voiding ([Decision 10](#decision-10-voided-records--deleted-from-the-read-store-not-marked)) and retired-metadata ([Decision 11](#decision-11-retired-metadata--data-references-preserved-names-snapshotted)) handling.
- Locale-aware serialization conventions ([Decision 8](#decision-8-locale-specific-serialization-with-multilingual-embeddings)).
- The self-sufficiency principle ([Decision 1](#decision-1-cqrs-pattern--separate-read-store-from-transactional-database)).

**Cross-cutting field contract.** Every module-contributed document must include the same cross-cutting fields that core-type documents include: `patient_uuid`, `resource_type`, `resource_uuid`, `date`, `text`, `embedding`, and where applicable `location_uuid` / `location_name`, `provider_uuid` / `provider_name`, `encounter_uuid`, `visit_uuid`, `form_uuid` / `form_name`, and `encounter_type_uuid` / `encounter_type_name`. Modules may add as many type-specific fields as they need beyond these. The cross-cutting set is non-negotiable — without it, cross-type queries break.

**Cross-type query convention.** Consumers needing unified retrieval query the `openmrs_*` wildcard pattern. This was already permitted by [Decision 4](#decision-4-per-type-indices-over-a-single-index); promoting it to the official cross-type contract is part of this decision. Per-type queries continue to use the specific index name. Embedding-based queries against the wildcard work because every contributing module embeds via the same pipeline (next point).

**Embedding model contract for query-time consumers.** Because querystore embeds at index time with a single model ([Decision 8](#decision-8-locale-specific-serialization-with-multilingual-embeddings)), every consumer that issues kNN queries must embed its query with the same model. Mismatched models produce incomparable vectors and silently broken results. The embedding model identifier is therefore part of querystore's public contract, surfaced through the SPI for consumers that need it.

### Rationale
1. **Mirrors [Decision 2](#decision-2-module-not-core) one level down.** Just as core should not depend on every module that records clinical data, querystore should not depend on every module that wants to index it. The contribution model lets modules participate without entangling the indexer.
2. **Avoids coupling explosion.** A monolithic querystore becomes a chokepoint for every module's release cycle. The SPI lets modules ship new resource types independently as long as they meet the contract.
3. **Preserves existing decisions uniformly.** Embedding model, locale, voiding, retirement, self-sufficiency — all apply to module-contributed types via the SPI. No parallel rules; the SPI is the surface that enforces existing rules on extension data.
4. **Wildcard cross-type retrieval is already cheap.** Per-type indices made cross-type retrieval inexpensive from the start ([Decision 4](#decision-4-per-type-indices-over-a-single-index)). Modules contributing new indices automatically join the cross-type query surface with no consumer code change.

### Consequences
- The SPI's exact Java interface, registration mechanism, and lifecycle hooks are implementation work. This decision establishes the principle and the contract; the signature is settled at implementation time.
- Modules wanting to contribute clinical data depend on querystore (transitively or directly). Deployments that don't install querystore lose any module-contributed indexing — same property as [Decision 2](#decision-2-module-not-core)'s "not every deployment needs it" applied recursively.
- A module rename (changing its moduleid) would require re-creating its index under the new name and re-syncing. Moduleid changes are rare and disruptive in OpenMRS generally, so this is an acceptable consequence of using moduleid for namespacing rather than a separate, mutable identifier.
- The cross-cutting field contract creates a documentation obligation for every contributing module: their docs must specify which cross-cutting fields are populated and which are intentionally null.
- Module developers gain access to the embedding pipeline without having to re-implement it; they also inherit the rules and cannot, for example, retain voided records or pick a different embedding model. This is a feature, not a constraint.
- Consumers of querystore (UIs, AI/analytics tools) can rely on a single retrieval surface for every patient-scoped clinical type in the deployment, regardless of which module recorded the data.

---

## Decision 14: Authorization and Consumer API Surface

### Status
Accepted

### Context
The ADR specifies what is indexed and how data flows in, but says nothing about how consumers reach the index or how access is controlled. The two questions are coupled: where authorization checks run depends on what surface consumers hit. Picking an authorization model in isolation requires either silently picking an interface or pretending one doesn't exist.

The chartsearchai migration ([migration-chartsearchai.md](./migration-chartsearchai.md)) makes this concrete. chartsearchai today enforces an "AI Query Patient Data" privilege plus core's per-patient access checks. Without an authorization model on the querystore side, a migration either pushes those checks onto every consumer (fragile, easy to forget) or runs unauthenticated queries against patient data — the leakage failure mode the original Authorization open question flagged.

### Decision
1. **Primary consumer surface for v1 is a Java service** (`QueryStoreService`) running in-process inside the OpenMRS JVM. The backend (Elasticsearch, embedded Lucene, or MySQL — see [Decision 3](#decision-3-pluggable-backend-spi-with-three-reference-implementations)) is internal infrastructure; consumers never query it directly. REST, FHIR Search, and other HTTP-fronted surfaces are out of v1 scope but explicitly additive — they layer on top of the same Java service and apply the same enforcement boundary at their own entry points.

2. **Authorization is declared on the service interface via `@Authorized` annotations.** Read methods on `QueryStoreService` carry OpenMRS's `@Authorized` annotation naming the required privilege; core's `AuthorizationAdvice` AOP enforces it before the method body runs and throws `APIAuthenticationException` when the caller lacks it. v1 read methods (`search`, `searchByPatient`) require `PrivilegeConstants.GET_PATIENTS` — the same privilege `PatientService.getPatient` requires. Per-patient post-filtering is *not* applied in v1 — see consequences.

3. **Querystore does not invent its own privilege scheme.** Privilege names follow OpenMRS conventions; deployments configure access through core's role and privilege management. Specialised privileges — chartsearchai's "AI Query Patient Data" being the canonical example — are deployment-defined, assigned to roles in core, and required by *consumers* before they invoke querystore. Querystore enforces what core would enforce on the equivalent direct call; it does not introduce parallel authorization concepts.

4. **The contract is "no result a core API would refuse."** Any document returned by querystore must be one the authenticated caller could have read via core's APIs. If a deployment's policy denies the caller access to certain records (sensitive obs, restricted patients), querystore must not surface those documents. The mechanism — pre-filter the ES query, post-filter the results, or denormalise sensitivity labels into documents — is implementation choice; the contract is binding.

### Rationale
1. **Java service mirrors how OpenMRS modules are normally consumed.** `Context.getService(QueryStoreService.class)` already exists and is what `moduleApplicationContext.xml` wires. Picking it as the v1 surface means consumers integrate the same way they integrate every other OpenMRS service.
2. **`@Authorized` is the OpenMRS-idiomatic pattern.** Every other OpenMRS service declares its privilege requirements this way. Putting the contract on the interface makes it visible to consumers, eliminates imperative auth code in the impl that someone could forget to add to a new method, and lets core's existing AOP advice do the enforcement.
3. **Reusing core's privilege constants avoids divergence.** A bespoke auth model — querystore-specific privilege names, a dedicated permission service — would drift from core over time. Annotating with `PrivilegeConstants.GET_PATIENTS` means any change core makes to that privilege's name or semantics applies to querystore automatically.
4. **Deferring REST / FHIR keeps v1 small without precluding them.** The Java service is the minimal surface chartsearchai needs to migrate. Adding a REST layer later is a controller that authenticates the request (session token, OAuth, whatever the deployment uses) and dispatches to the same `QueryStoreService` — the auth boundary already exists, the new layer routes to it.
5. **Cross-patient search relying on a coarse privilege matches existing practice.** chartsearchai's "AI Query Patient Data" privilege is a coarse gate, not a per-patient check. Adopting the same coarse model for querystore's cross-patient search keeps the migration straightforward and matches operational reality: deployments granting AI/analytics access already trust those callers at the patient-set level.

### Consequences
- **Cross-patient search results may include patients the caller cannot read individually via core.** Coarse `GET_PATIENTS` gates the query; per-result patient-access filtering is not enforced in v1. Deployments needing finer cross-patient enforcement must either restrict who holds the privilege or add post-filtering in their consumer. A follow-up decision can revisit this once a real use case forces the trade-off.
- **Per-patient access modules (`dataFilter`, location-based ACLs, etc.) are not honoured by v1's enforcement.** `@Authorized` runs the static privilege check; it does not invoke a Hibernate-level patient read, so any per-patient filtering those modules add does not fire. Deployments that rely on such modules need either an explicit hook in querystore (a v2 decision) or consumer-side enforcement.
- **Sensitive-data access policy is binding but its mechanism is unspecified.** v1 implementations may pre-filter the ES query (faster), post-filter the results (simpler), or denormalise sensitivity labels into documents (most efficient, most invasive). The contract — no result a core API would refuse on the same privilege grant — holds regardless of mechanism. A future decision may pin one approach if multiple implementations diverge or an audit obligation requires it.
- **Indexing methods (`index`, `delete`) are not `@Authorized`-gated in v1.** They are invoked by the sync pipeline running in a system context. Hardening that surface — either by splitting it onto a sync-only interface or adding a `Manage Querystore`-class privilege — is deferred to v2.
- **External consumers (non-JVM, non-OpenMRS) cannot integrate in v1.** Any such consumer requires a REST or FHIR surface, which is deferred. This is explicitly a v1 limitation, not a permanent one.
- **Audit logging of querystore queries is not mandated by this decision.** Audit (who searched for what, when) is an adjacent concern; it should be addressed by a separate decision if it surfaces as a real requirement.

---

## Open Questions

Design questions that have been recognized but not yet resolved. Each item below is self-contained and should be deleted from this list once it is promoted to a numbered decision above. New items can be appended as they are surfaced.

- [Initial backfill / bootstrap](#initial-backfill--bootstrap)
- [Sync reliability and reconciliation](#sync-reliability-and-reconciliation)
- [Migration-time re-projection hook](#migration-time-re-projection-hook)
- [Event-handler idempotency and ordering](#event-handler-idempotency-and-ordering)
- [Embedding model versioning](#embedding-model-versioning)
- [Long-text chunking for embeddings](#long-text-chunking-for-embeddings)
- [Complex obs handling](#complex-obs-handling)
- [Re-index / alias strategy](#re-index--alias-strategy)
- [Backend SPI contract — residual](#backend-spi-contract--residual)
- [Tier upgrade operational signals](#tier-upgrade-operational-signals)
- [Name-refresh re-projection](#name-refresh-re-projection)
- [PII and data-minimization scopes](#pii-and-data-minimization-scopes)
- [Patient merge handling](#patient-merge-handling)
- [Concept-set and hierarchy queries](#concept-set-and-hierarchy-queries)
- [Knowledge-base and reference-data projection](#knowledge-base-and-reference-data-projection)
- [Timestamp time-zone convention](#timestamp-time-zone-convention)
- [Person vs Patient model](#person-vs-patient-model)
- [Drug-order non-coded drug handling](#drug-order-non-coded-drug-handling)

### Initial backfill / bootstrap
[Decision 12](#decision-12-sync-mechanism--events-first-aop-as-last-resort-gap-filler) covers steady-state sync but not how the read store reaches "in sync" the first time, after a full rebuild, or after adding a new indexed resource type to an existing deployment. Likely shape: a one-time service-API scan that paginates through every entity of each type, serializes it, generates embeddings, and writes through index aliases. Decision needed on chunking strategy, throttling to avoid overloading core, embedding-generation throughput, progress tracking, and how the steady-state event subscription is started without missing events emitted during the backfill window.

### Sync reliability and reconciliation
The Event module can lose events on broker restart, and consumer-side failures can drop messages even when delivery succeeded. [Decision 12](#decision-12-sync-mechanism--events-first-aop-as-last-resort-gap-filler) acknowledges this without solving it. Decision needed on: durable subscription configuration, dead-letter handling for permanently-failing events, periodic reconciliation (e.g., per-patient or per-type record-count comparisons against core to detect drift), and a remediation path when drift is detected (targeted re-sync vs. full rebuild). Related to but distinct from the bootstrap question above.

A separate but related concern is direct-database writes (Liquibase migrations, direct SQL) that bypass the Event module entirely. Today these escape both events and AOP and would be caught only by reconciliation. If [TRUNK-6516](https://openmrs.atlassian.net/browse/TRUNK-6516) ships a Debezium-backed Event module, that gap closes at the platform level (see [Decision 12](#decision-12-sync-mechanism--events-first-aop-as-last-resort-gap-filler) rationale point 7), and reconciliation only needs to cover broker-loss and dropped-message cases. Until then, an additional bridging mechanism is needed for module-shipped Liquibase changesets that rewrite indexed data — discussed separately in the [Migration-time re-projection hook](#migration-time-re-projection-hook) open question.

### Migration-time re-projection hook
Modules occasionally ship Liquibase changesets that rewrite data already projected into the read store — backfilling a column, normalising a value set, fixing data corrupted by a prior bug. These writes bypass the OpenMRS Event module entirely (see [Sync reliability and reconciliation](#sync-reliability-and-reconciliation) above), so the read store does not learn about them through any sync path other than reconciliation, and reconciliation cycles may run far enough apart that the drift is user-visible in the meantime. Closing this gap before [TRUNK-6516](https://openmrs.atlassian.net/browse/TRUNK-6516) lands needs an explicit declaration mechanism: a module that touches indexed data tells querystore at startup which resource types it affected, and querystore re-projects those types (or, more precisely, the affected records within them). The design surface includes:
- **Trigger surface.** How does a module declare the affected scope — an annotation on the Liquibase changeset, a manifest file, an OpenMRS module config property, or a service interface the module implements? Likely an extension of the [Decision 13](#decision-13-module-extension-spi-service-provider-interface-for-custom-resource-types) SPI rather than a parallel mechanism.
- **Lifecycle.** Run once after the changeset applies, or every startup until the module acknowledges completion?
- **Scope granularity.** Resource types only (re-project everything of that type), or row-level (UUIDs of specifically touched records)? Row-level is dramatically cheaper but pushes more work onto the module.
- **Idempotency.** A module redeclaring the same scope across multiple startups should not trigger redundant re-projection.
- **Failure handling.** A re-projection failure during startup: block the module from starting, mark drift as known and continue, or retry on a schedule?
- **Discovery.** How does querystore find these declarations across all installed modules without each module having to register imperatively?

This open question is moot if [TRUNK-6516](https://openmrs.atlassian.net/browse/TRUNK-6516) ships and the OpenMRS Event module emits events for direct-DB writes. Until then, it is the bridge between "events miss this" and "reconciliation eventually catches this."

### Event-handler idempotency and ordering
[Decision 12](#decision-12-sync-mechanism--events-first-aop-as-last-resort-gap-filler) selects a JMS-backed at-least-once delivery model, which permits duplicate delivery (the same update event handled twice) and out-of-order delivery (a void event arriving before the corresponding create, or two updates applied in the wrong order). Distinct from the lost-events concern above: those are events that never arrive; these are events that arrive too many times or in the wrong sequence. Decision needed on: idempotency strategy for each handler (delete-by-uuid is naturally idempotent, but updates and merges are not), how to detect and discard stale updates (e.g., compare an event timestamp or version against the indexed document), and what to do when a void or update arrives for a `resource_uuid` not yet present in the read store (drop, queue for retry, or fetch-and-create from core).

### Embedding model versioning
[Decision 8](#decision-8-locale-specific-serialization-with-multilingual-embeddings) specifies a model class (multilingual-e5) but not a specific model identifier or upgrade path. Embeddings from different models are not comparable, so a model change is a full re-index of every vector. Needs a decision on model pinning, a per-document `embedding_model_version` field, and how model upgrades coordinate with index aliases (see next item). Multimodal model selection (for image / audio embeddings — see *Complex obs handling* below) is a distinct sub-decision under this umbrella, with its own constraint set: shared image-text vector space (CLIP-class architecture, not a generative VLM repurposed as an embedder), multilingual support consistent with Decision 8, license suitable for clinical deployment, and demonstrated performance on medical imagery (X-rays, retinal photos, dermatology) where generic web-trained models routinely underperform. Candidates to evaluate when the time comes include medical-specialized variants (BiomedCLIP, MedCLIP, PubMedCLIP), general CLIP / SigLIP families, and Gemma-derived options (multimodal Gemma, EmbeddingGemma if it ever ships a multimodal variant).

### Long-text chunking for embeddings
Multilingual embedding models have a fixed input window — multilingual-e5 caps at roughly 512 tokens. Clinical notes, discharge summaries, and other narrative text obs values routinely exceed this. [Decision 5](#decision-5-plain-text-serialization-over-json-or-fhir) and [Decision 6](#decision-6-document-model--text-embeddings-and-structured-metadata) assume one text chunk + one embedding per record, which silently fails on long values (the model truncates and the tail of the note is invisible to semantic search). Decision needed on a chunking strategy: parent/child documents in Elasticsearch with chunk-level embeddings rolled up at retrieval, sliding-window chunks, semantic splits, refusing to embed values above a threshold, or some combination. Affects retrieval quality for any unstructured-text obs and should be decided before notes-style data is heavily ingested. Closely tied to *Complex obs handling* below — `LongFreeTextHandler` is exactly the class of long-text values that straddles both questions.

### Complex obs handling
OpenMRS complex obs are observations whose value is produced by a `complexHandler` and stored as a file or stream rather than a primitive — typically images (`ImageHandler`), generic binary (`BinaryDataHandler`, `BinaryStreamHandler`), audio/video (`MediaHandler`), or long narrative text (`LongFreeTextHandler`). The schema now carries `value_complex_uri` + `value_complex_handler` ([Decision 6](#decision-6-document-model--text-embeddings-and-structured-metadata)) so the document records *that* a complex value exists and where to fetch it, but per-handler indexing behavior is not yet specified.

Sub-questions:

- **(a) `LongFreeTextHandler`** — extract the text into `value_text` and apply the long-text chunking strategy above. This is the natural design and the cleanest case.
- **(b) Image / binary / media handlers** — decide whether multimodal embeddings (image-similarity, text-to-image semantic search) are an optional v1 capability or strictly out of v1 scope. The use cases (case-based imaging retrieval, dermatology and retinal screening, cough acoustics) are real and high-value in OpenMRS deployment contexts, but the model-selection risk is non-trivial. See *Embedding model versioning* above for the constraint set: shared vector space, multilingual support, clinical-deployment licensing, performance on medical imagery.
- **(c) Custom handlers** registered by deployments need a documented default — URI reference + handler-name marker, no extraction.
- **(d) Schema future-proofing** — regardless of whether multimodal embeddings are in v1, the schema must not preclude them. Adding `image_embedding` / `audio_embedding` per-modality vector fields later should be additive, not a migration.

A decision should pick the per-handler behavior, the default for unknown handlers, the v1 scope for each modality, and the migration path if multimodal is enabled later.

### Re-index / alias strategy
Multiple decisions ([8](#decision-8-locale-specific-serialization-with-multilingual-embeddings), [11](#decision-11-retired-metadata--data-references-preserved-names-snapshotted), and any future serializer change) imply re-indexing as the remedy. Doing this without downtime requires writing through aliases (e.g., `openmrs_obs` → `openmrs_obs_v1`, with atomic swap to `_v2` after backfill). No decision covers the alias convention, the cutover protocol, or — given [Decision 12](#decision-12-sync-mechanism--events-first-aop-as-last-resort-gap-filler) — how the live event subscription is coordinated during cutover (events arriving mid-rebuild must land somewhere: dual-write to old and new aliases, replay from a snapshot, or a controlled subscription pause). Several existing claims rely on this being possible. The lighter-weight [Name-refresh re-projection](#name-refresh-re-projection) path below is a per-document partial-update operation and does not strictly require alias cutover, but it shares operational concerns (bounded fan-out throughput, coordination with concurrent event-driven updates, failure recovery) with the full re-index path and is best designed alongside it. Tier migration under [Decision 3](#decision-3-pluggable-backend-spi-with-three-reference-implementations) (e.g., a deployment outgrowing the MySQL tier and switching to Elasticsearch) is structurally a re-index from one backend to another and shares the same coordination problem; the alias convention chosen here should generalise across backends.

### Backend SPI contract — residual
The bulk of the SPI contract — operation surface, hybrid fusion location, score semantics, bulk-write semantics, transactional boundaries, and capability-negotiation mechanism — is now pinned in [Decision 3](#decision-3-pluggable-backend-spi-with-three-reference-implementations) and reflected in the `BackendStore` interface that ships with the MySQL reference implementation. What remains:

- **Aggregations.** The v1 SPI does not expose counts, group-by, or other aggregation primitives. The gating signal for adding `aggregate(...)` is whether downstream consumers (FHIR analytics, reporting modules, [Tier upgrade operational signals](#tier-upgrade-operational-signals)) ask for it concretely. Additive change when it lands.
- **Specific corpus-size thresholds and admin alerting.** Decision 3 commits to the *mechanism* (`BackendCapabilities.recommendedMaxCorpusSize` + service-layer warning), but the documented thresholds ("MySQL not recommended above N", "alert at M") depend on real-corpus measurement and are tracked under [Tier upgrade operational signals](#tier-upgrade-operational-signals).
- **ES write-throughput relaxation.** Decision 3 commits the ES backend to `refresh=wait_for` per call for durability + visibility parity. If real ES deployments hit throughput limits, a batched-flush variant (with the latency-vs-staleness contract spelled out) is the residual question. Not actionable until the ES backend is implemented.

### Tier upgrade operational signals
[Decision 3](#decision-3-pluggable-backend-spi-with-three-reference-implementations) makes tier migration cheap structurally (config change + re-index from core) but does not specify *when* a deployment should upgrade. A deployment that installs on the default `mysql` tier and indexes a million obs over six months will hit query-latency degradation gradually; without operational signals, the upgrade decision is reactive (clinicians complain about slow searches) rather than proactive. Decision needed on:

- **Metrics to expose.** Per-query latency percentiles, corpus size by resource type, brute-force kNN scan size — what does querystore surface for ops to observe? At a minimum, the metrics that distinguish "this tier is healthy" from "this tier is degrading" must be exposed cheaply.
- **Threshold guidance.** Documented "upgrade when X" signals (e.g., "MySQL tier no longer recommended above ~500k indexed obs", "Lucene tier no longer recommended beyond single-host throughput limits"). Numbers approximate but committed enough to act on.
- **Auto-recommend or alert?** Should the module surface an admin-visible warning when corpus size or latency crosses the threshold, or is detection purely an ops responsibility?
- **Coordinating tier upgrade with [Re-index / alias strategy](#re-index--alias-strategy).** Tier migration is a re-index, but the operational sequencing (when to dual-write, when to cut over, how long the rebuild takes) is shared with the alias open question and depends on its resolution.

### Name-refresh re-projection
[Decision 11](#decision-11-retired-metadata--data-references-preserved-names-snapshotted) accepts denormalized-name staleness as policy and names a full resource-type re-index as the remedy when freshness matters. That remedy is heavy: a full re-index of `openmrs_obs` in any decent-sized site is millions of documents of embedding-generation cost, so in practice it is unlikely to be invoked routinely, and renames may sit unfixed for long periods. A lighter-weight path would be a **name-refresh re-projection**, triggered by a metadata-rename event: fan out across the documents that reference the renamed UUID and (1) re-render the document's text from its existing structured fields with the new name, (2) re-embed the new text, and (3) update the `*_name` field to match. This is *not* "re-embed only" — the text has to change too, because document text and embedding vector must remain consistent ([Decision 6](#decision-6-document-model--text-embeddings-and-structured-metadata)). The savings versus a full re-index come from (a) not re-fetching the underlying record from core (the obs / encounter / visit row hasn't changed; only the metadata it references did) and (b) scoping to documents that reference the renamed UUID rather than the entire resource type. For small-cardinality renames (a location, an encounter type, an obscure concept) the cost gap is orders of magnitude. Decision needed on: whether to build it (gate: whether the chartsearchai eval workflow exposes meaningful rename-driven recall degradation in practice), how to bound fan-out for large-cardinality renames (a popular concept rename can still touch millions of obs), and how it coordinates with the [Re-index / alias strategy](#re-index--alias-strategy) open question — strictly, name-refresh is a per-document partial update that does not require alias cutover, but it shares operational concerns (throughput, concurrent-event coordination, failure recovery) with the full re-index path and is best designed alongside it.

### PII and data-minimization scopes
The current document model stores full identifiers, names, addresses, and contact attributes in the `openmrs_patient` index, plus provider names everywhere. Different consumers need different shapes of the same data: clinical-care callers need full patient identity; research-analytics callers typically need de-identified or pseudonymized projections. Decision needed on whether the read store maintains one fully-detailed projection with redaction applied at query time, multiple parallel projections at different sensitivity levels, or pushes minimization onto consumers entirely. Adjacent to but distinct from authorization (which governs *who can read*); this governs *what is stored and in what form*.

### Patient merge handling
When two patients are merged in core, all their clinical data is reassigned to the surviving UUID. The read store needs corresponding logic — at minimum, repointing every document keyed by the merged-away `patient_uuid`. Two sub-questions: (a) does core emit a patient-merge event under [Decision 12](#decision-12-sync-mechanism--events-first-aop-as-last-resort-gap-filler)'s sync model, or is this a gap to fill upstream or via scoped AOP, and (b) given a trigger, does the read store handle the merge as an in-place `patient_uuid` rewrite across every affected document, a delete + re-index of the merged-away patient's data, or another mechanism, including how the in-flight inconsistency window is handled.

### Concept-set and hierarchy queries
A query like "all glucose-related results" should match HbA1c, FPG, RBS, and other related concepts without enumerating every variant UUID at query time. OpenMRS has concept sets and concept hierarchies that could be denormalized into documents (e.g., a `concept_ancestor_uuids` array per obs). Decision needed on whether to support such queries directly in the index or to expand them at query time using a separate concept-relations service. A subset of the broader [Knowledge-base and reference-data projection](#knowledge-base-and-reference-data-projection) open question below.

### Knowledge-base and reference-data projection
[Decision 11 point 4](#decision-11-retired-metadata--data-references-preserved-names-snapshotted) puts direct metadata indices — `openmrs_concepts`, `openmrs_locations`, `openmrs_drugs`, terminology mappings — out of v1 scope. The current model denormalizes `*_uuid + *_name` for these references into clinical-data documents but does not project the underlying knowledge base (concept dictionary, code systems, value sets, drug catalogue, location hierarchy, ConceptMap-style relationships) as queryable resources in their own right. This is a deliberate scope choice for v1, but the broader design surface — how knowledge base and instance data combine for retrieval — is currently implicit, split across that point, the [Concept-set and hierarchy queries](#concept-set-and-hierarchy-queries) open question above, and [Decision 13](#decision-13-module-extension-spi-service-provider-interface-for-custom-resource-types)'s SPI extension semantics. Surfacing it here so the position isn't only inferable. Decision needed on:

- **Which resources to project, if any.** Concepts (with definitions, mappings, set memberships, ancestors), value sets, code systems, drug references, location hierarchies, FHIR-shaped ConceptMap mappings — each has its own use-case justification and its own indexing cost.
- **Core querystore concern vs. SPI contribution.** Knowledge-base indices could be owned by querystore directly (consistent with the prefix convention) or pushed entirely to contributing modules via [Decision 13](#decision-13-module-extension-spi-service-provider-interface-for-custom-resource-types). The latter keeps querystore lean but means CIEL- or OCL-aware indexing requires a separate module; the former pulls a substantial domain into core scope.
- **Mixed retrieval semantics.** The `openmrs_*` wildcard already makes mixed retrieval (instance data + knowledge base) syntactically cheap, but ranking, relevance, and embedding-space coherence across structurally different document types (clinical events vs. concept definitions) is not yet thought through. Co-mingling them in a single kNN result set may produce nonsensical orderings without per-type weighting.
- **Relationship to upstream terminology services.** Most OpenMRS deployments use CIEL or OCL/OCL-Online for their dictionary, and FHIR provides standard CodeSystem / ValueSet / ConceptMap shapes. Decision needed on whether querystore re-projects a local snapshot of these (subject to the same staleness concerns as Decision 11) or proxies live to the terminology service. The latter avoids divergence but couples query latency to an external service.

Out-of-v1 by design; the gating signal for re-opening this is whether the chartsearchai eval workflow surfaces queries that need knowledge-base projections to answer well, or whether downstream consumers (FHIR analytics, AI agents reasoning over coded data) request it concretely.

### Timestamp time-zone convention
Documents mix date-only fields (`date`, `birthdate`) with timestamp fields (`start_date_time`, `end_date_time`, `date_handed_over`). The time zone for timestamps is unspecified. UTC is the obvious default, but OpenMRS data often originates in deployment-local time and is stored without a zone offset. The convention needs to be explicit so consumers know how to interpret a value like `start_date_time = "2025-03-15T09:30:00"` and so date-range filters match consistently.

### Person vs Patient model
The `openmrs_patient` index conflates Person attributes (name, gender, birthdate, addresses, attributes) with Patient attributes (identifiers). In OpenMRS core these are separate entities — a Person can exist without being a Patient (e.g., providers, relatives). The current flattening is appropriate for a read-side projection focused on patient queries, but should be made explicit so downstream consumers do not expect an `openmrs_persons` resource type to also exist or look for non-patient Persons in this index.

### Drug-order non-coded drug handling
`DrugOrder` exposes `drugNonCoded` (a free-text drug name used when the clinician records a drug outside the dictionary), the order-side analog of `Condition.nonCoded` and `Diagnosis.nonCoded`. The Decision 6 drug-order example doc and the field-descriptions table do not mention it, and the v1 serializer drops drug orders whose display name resolves to empty — so a non-coded drug with no `concept` and no `Drug` becomes a skipped document. Decision needed on whether to add a third fallback to the display-name resolution chain (`drug.name → concept preferred name → trimmed drugNonCoded`), mirror Condition/Diagnosis by storing it on a `non_coded` (or order-specific `drug_non_coded`) metadata field, and whether `DrugOrder.brandName` / `dispenseAsWritten` deserve indexed surfaces of their own. The same question applies to `Order.orderReasonNonCoded` (the free-text variant of `orderReason`) which is similarly absent from v1.

