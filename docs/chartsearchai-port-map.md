# chartsearchai port map

> **Status: historical and superseded.** This document records the source-analysis used while
> building QueryStore. It is not the current integration design. ChartSearchAI is now a thin
> OpenMRS relay, and med-agent-hub owns clinical context assembly and answer orchestration.

A starting map for the implementation phase: which files in [openmrs-module-chartsearchai](https://github.com/openmrs/openmrs-module-chartsearchai) are worth porting into querystore, which are not, and what each maps to.

The architectural decisions are in [adr.md](./adr.md); the chartsearchai-specific gap analysis is in [migration-chartsearchai.md](./migration-chartsearchai.md). This doc is just a code-level pointer — written so the implementation team doesn't have to re-derive it from first principles.

## Worth porting (intact or with light rework)

| chartsearchai path | Maps to in querystore | Notes |
|---|---|---|
| `serializer/*TextSerializer.java` (8 serializers: Obs, Condition, Diagnosis, Allergy, Order, MedicationDispense, PatientProgram, plus shared ConceptNameUtil) | Per-resource-type serializers, one per `querystore_<type>` index | Output format diverges from [Decision 5](./adr.md#decision-5-plain-text-serialization-over-json-or-fhir)'s labeled-prose convention — chartsearchai uses a structural prefix + free body (`"Clinical observation: Vital signs / Finding — Temperature: 36.7"`). Querystore's labeled-field schema is a re-skin of the same data extraction, not a rewrite |
| `util/ConceptNameUtil.java` | Shared utility | Locale + synonym handling. Caps to 3 synonyms, sorts deterministically. The `(syn. ...)` regex strip path is reusable |
| `util/DateFormatUtil.java` | Shared utility | UTC `yyyy-MM-dd` formatting. Trivial; port and revisit per the [Timestamp time-zone convention](./adr.md#timestamp-time-zone-convention) open question |
| `embedding/OnnxEmbeddingProvider.java` | Default `EmbeddingProvider` impl per [Decision 8](./adr.md#decision-8-locale-specific-serialization-with-multilingual-embeddings) | Already model-agnostic, dual-encoder support, dimension auto-detection, CoreML/CUDA/CPU auto-select. `embed()` is `synchronized` — port the queueing model carefully |
| `embedding/WordPieceTokenizer.java` | Tokenizer used by the default provider | Truncates at `maxSequenceLength` — silent truncation is the [Long-text chunking](./adr.md#long-text-chunking-for-embeddings) bug; port as-is and fix at the chunking layer |
| `serializer/PatientRecordLoader.java` | Per-patient projection driver | Becomes the entry point for the events-first sync handler ([Decision 12](./adr.md#decision-12-sync-mechanism--events-first-aop-as-last-resort-gap-filler)) and the bulk-backfill task |
| `api/HybridRetriever.java` (the `fuseRRF` method specifically, ~30 lines) | In-process RRF fusion for the MySQL and Lucene tiers | Useful template — the rest of the file is question-tuned and should not be ported |
| `api/ElasticsearchQueryBuilder.java` (~190 lines) | ES backend's query DSL | Includes ES-vs-OpenSearch detection, RRF retriever shape (ES) vs hybrid query + search-pipeline (OS). Port intact for the Elasticsearch tier impl |
| `api/ElasticsearchIndexer.java` (mapping building, doc-ID format, bulk-index logic) | ES backend's indexing path | Doc ID format `patientId_resourceType_resourceId` works; mapping auto-creation works. Watch out: per-doc errors in bulk responses are not currently inspected — fix in port |
| `api/EmbeddingIndexer.java` (the `replacePatientEmbeddings` delete-then-insert pattern) | MySQL backend's per-patient atomic re-projection | The transactional shape (delete by patient_id then insert new rows in one `@Transactional`) is the right pattern for void/merge handling per [Decisions 10/11](./adr.md#decision-10-voided-records--deleted-from-the-read-store-not-marked) |
| `api/EmbeddingIndexTask.java` | Initial backfill task | Pages through patients in batches of 50 with flush/clear between batches. Decent template; the [Initial backfill / bootstrap](./adr.md#initial-backfill--bootstrap) open question covers what needs to change |

## Schema reference (chartsearchai_embedding table)

Single shared table with `(resource_type, resource_id)` UNIQUE. `embedding mediumblob` stores raw little-endian float32 (no JSON, no base64). Querystore's MySQL tier deliberately splits into per-type tables per [Decision 4](./adr.md#decision-4-per-type-indices-over-a-single-index), but the binary-encoding choice is worth keeping.

## Not worth porting (LLM-coupled or chartsearchai-specific)

| chartsearchai path | Reason |
|---|---|
| `api/impl/Llm*`, `LlmInferenceService`, `WarmupExecutor`, `ChartSearchServiceRouter` | LLM inference + answer-generation — stays in chartsearchai, layered above querystore |
| `api/impl/RetrievalQuery.java` and the embedding-pipeline scoring stack: `CoherenceFilters`, `ConceptKeywordMatching`, `ConceptNameReranker`, `ConceptRescueAndFilter`, `EmbeddingRankingPipeline`, `RankingPipelineGates`, `RefinementPaths`, `ScoreStatistics`, `ScoredEmbedding`, `SimilarityAndScoringEngine` | ~6000 lines of question-tuned heuristics (gap detection, similarity ratio, z-score gates, type boost, etc.). querystore returns vanilla kNN+BM25+RRF; the quality layer stays in chartsearchai |
| `ChartCache`, `ChartCacheInvalidator` | Replaced by `QueryStoreService.getPatientChart` per [Decision 15](./adr.md#decision-15-full-chart-retrieval--unfiltered-per-patient-enumeration). The cache duplicates what querystore already serializes via the events-first sync pipeline; invalidation is handled by the same pipeline that keeps the index current, so the per-patient cache and its invalidator both disappear on migration |
| AOP advice classes (`EncounterIndexingAdvice`, `ObsIndexingAdvice`, `PatientDataIndexingAdvice`) | Replaced by events-first sync per [Decision 12](./adr.md#decision-12-sync-mechanism--events-first-aop-as-last-resort-gap-filler). Method-name dispatch and per-event handlers are useful as a *coverage map* (which OpenMRS service methods need event subscription) but the AOP mechanism itself is replaced |
| `api/impl/DefaultPatientAccessCheck.java` | Authorization is now `@Authorized`-annotation-based per [Decision 14](./adr.md#decision-14-authorization-and-consumer-api-surface) |
| Audit log table (`chartsearchai_audit_log`) | Audit is a chartsearchai-side concern, not a read-store responsibility |

## Coverage map: which OpenMRS services chartsearchai's AOP advises

Useful as a checklist for events-first subscription coverage. Under core's #6084 events, each service's lifecycle methods emit the typed events querystore handles — `SaveServiceEvent` (covers create + update), `VoidServiceEvent`, `UnvoidServiceEvent`, `RetireServiceEvent`, `UnretireServiceEvent`, `PurgeServiceEvent` (see [Decision 12](./adr.md#decision-12-sync-mechanism--events-first-aop-as-last-resort-gap-filler)):

- `EncounterService` — encounters, plus encounter-scoped obs and diagnoses
- `ObsService` — obs (incl. group flattening)
- `ConditionService`, `DiagnosisService`
- `PatientService` — patient demographics + merge handling
- `OrderService` — drug, test, referral, plain orders
- `ProgramWorkflowService` — patient program enrollment + state changes
- `MedicationDispenseService`

`mergePatients` on `PatientService` is special-cased today (re-index preferred + delete-by-patient on non-preferred). querystore now does the same, triggered off the incidental `SaveServiceEvent<PersonMergeLog>` rather than chartsearchai's AOP — see [Patient merge handling](./adr.md#patient-merge-handling) (resolved).

## Operational quirks worth knowing

- chartsearchai's ES path has a chicken-and-egg bootstrap bug: `EmbeddingIndexTask` only writes to MySQL, and AOP's `reindexIfActive` returns false for never-indexed patients, so ES never auto-populates. Don't replicate; design the bootstrap path for all backends per [Initial backfill / bootstrap](./adr.md#initial-backfill--bootstrap).
- All catch-blocks on the indexing path are `log.error(...)` and continue — no retry, no DLQ, no reconciliation. The [Sync reliability and reconciliation](./adr.md#sync-reliability-and-reconciliation) open question replaces this.
- ES uses a global `chartsearchai-rrf` search pipeline (OS only) for RRF fusion — auto-created on first index. Port the creation logic with the ES backend.
