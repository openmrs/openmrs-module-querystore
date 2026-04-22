# Architecture Decision Records

This document captures the key architectural decisions for the OpenMRS Query Store Module.

## Table of Contents

1. [CQRS Pattern — Separate Read Store from Transactional Database](#decision-1-cqrs-pattern--separate-read-store-from-transactional-database)
2. [Module, Not Core](#decision-2-module-not-core)
3. [Elasticsearch as the Backing Store](#decision-3-elasticsearch-as-the-backing-store)
4. [Per-Type Indices Over a Single Index](#decision-4-per-type-indices-over-a-single-index)
5. [Plain Text Serialization Over JSON or FHIR](#decision-5-plain-text-serialization-over-json-or-fhir)
6. [Document Model — Text, Embeddings, and Structured Metadata](#decision-6-document-model--text-embeddings-and-structured-metadata)
7. [Date Separation — Excluded from Embeddings, Included at Query Time](#decision-7-date-separation--excluded-from-embeddings-included-at-query-time)

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

### Consequences
- Clinical workflows are unaffected by query workloads.
- The query store can be rebuilt from scratch at any time since core remains the source of truth.
- Consumers must tolerate eventual consistency — there is a brief delay between a write in core and its availability in the query store.
- Two systems must be kept in sync, adding operational complexity.

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

## Decision 3: Elasticsearch as the Backing Store

### Status
Accepted

### Context
The query store needs a backend that supports full-text search (keyword matching), semantic search (vector similarity), structured filtering (by patient, date, concept, etc.), and cross-patient aggregation. Candidates considered:

| Option | Full-text | Vector | Structured filter | Cross-patient scale | Extra infra |
|---|---|---|---|---|---|
| MySQL (existing) | Limited | No native support | Yes | Moderate | None |
| PostgreSQL + pgvector | Yes | Yes | Yes | Good | New database |
| Elasticsearch | Yes | Yes (dense_vector + kNN) | Yes | Excellent | New service |
| Dedicated vector DB (Pinecone, Milvus, Qdrant) | No/limited | Yes | Limited | Excellent | New service |

### Decision
Use Elasticsearch as the primary backing store.

### Rationale
- **Hybrid search in one system.** Elasticsearch supports BM25 full-text search, dense vector kNN search, and structured filtering — all combinable in a single query. This avoids the complexity of coordinating multiple systems.
- **Cross-patient scale.** Unlike brute-force in-application similarity computation, Elasticsearch's inverted indices and HNSW vector indices handle millions of documents efficiently.
- **Mature ecosystem.** Well-documented, battle-tested, widely deployed in healthcare settings.
- **Query DSL.** Rich filtering and aggregation capabilities that go beyond what a pure vector database offers.
- **Some OpenMRS deployments already run Elasticsearch,** reducing the incremental infrastructure cost.

A dedicated vector database was rejected because it would only serve semantic search, requiring a separate system for keyword search and structured filtering. MySQL was rejected because it lacks native vector search and its full-text capabilities are limited compared to Elasticsearch.

### Consequences
- Deployments must run an Elasticsearch instance (separate JVM process, minimum ~1-2GB RAM).
- This adds operational complexity for low-resource settings where infrastructure is constrained.
- The module should be designed so the Elasticsearch dependency can be swapped for an alternative backend in the future if needed.

---

## Decision 4: Per-Type Indices Over a Single Index

### Status
Accepted

### Context
Clinical data in OpenMRS spans multiple resource types: observations, conditions, diagnoses, drug orders, test orders, allergies, patient programs, and medication dispenses. These types have different fields and different query patterns. The data can be stored in a single Elasticsearch index with a `resource_type` discriminator or in separate per-type indices.

### Decision
Use per-type indices (e.g., `openmrs_obs`, `openmrs_conditions`, `openmrs_drug_orders`, etc.) rather than a single mixed index.

### Rationale
1. **No sparse fields.** Each index contains only the fields relevant to its type. A single index would carry empty drug order fields on every obs document and vice versa, wasting storage and slowing queries at scale.
2. **Better query performance.** Type-specific queries (e.g., "all patients with HbA1c above 7") only scan the relevant index rather than skipping irrelevant document types.
3. **Better relevance scoring.** BM25 term frequencies are computed per index. Mixing clinical notes, lab results, and drug orders dilutes term frequencies across unrelated document types, hurting search quality.
4. **Cross-type search is still easy.** Elasticsearch wildcard patterns (e.g., `openmrs_*`) allow querying across all types when needed, providing the same convenience as a single index.
5. **Future-proof for cross-patient search.** Per-patient chart search works fine with either approach since the patient_id filter narrows the scope. But cross-patient search at scale benefits significantly from type-specific indices.

### Consequences
- More indices to manage, though an index template can share common settings across all `openmrs_*` indices.
- Document writes must be routed to the correct index based on resource type.
- Cross-type queries require multi-index search syntax.

---

## Decision 5: Plain Text Serialization Over JSON or FHIR

### Status
Accepted

### Context
Clinical records must be serialized into a text representation that is stored in Elasticsearch and used for two purposes: (1) as input to embedding models for vector generation, and (2) as content read by LLMs when generating answers. The format options are plain labeled text, JSON, XML, or FHIR JSON.

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
- The plain text format is less machine-parseable than JSON — consumers that need structured access should use the structured metadata fields in the Elasticsearch document, not parse the text.
- Changes to the serialization format require re-embedding and re-indexing affected records.

---

## Decision 6: Document Model — Text, Embeddings, and Structured Metadata

### Status
Accepted

### Context
Each document in the query store needs to serve multiple purposes: semantic search, keyword search, structured filtering, LLM answer generation, and linking back to the source record in OpenMRS.

### Decision
Each Elasticsearch document contains three components:

1. **Text chunk** — the plain text serialization of the clinical record.
2. **Vector embedding** — a dense vector computed from the text chunk, stored in a `dense_vector` field.
3. **Structured metadata** — typed fields for filtering and aggregation (patient_id, date, resource_type, resource_id, concept_name, and type-specific fields).

Example document:
```json
{
  "patient_id": 123,
  "resource_type": "obs",
  "resource_id": 456,
  "encounter_id": 789,
  "date": "2025-03-15",
  "text": "Fasting blood glucose: 11.2 mmol/L",
  "embedding": [0.023, -0.041, 0.078, ...],
  "concept_name": "Fasting blood glucose",
  "concept_class": "Test",
  "value_numeric": 11.2,
  "units": "mmol/L",
  "interpretation": "ABNORMAL",
  "location": "Kenyatta National Hospital",
  "provider": "Dr. Ochieng"
}
```

### Rationale
Each component serves distinct purposes that the others cannot fulfill:

- **Text chunk**: BM25 keyword search matches against it. The embedding model was run on it. The LLM reads it when generating answers. Without it, you can find a match but have nothing to display or feed to the LLM.
- **Vector embedding**: Enables semantic similarity search (e.g., "blood sugar control" matching an HbA1c result). Without it, you can only do keyword matching.
- **Structured metadata**: Enables precise filtering (by patient, date range, resource type, numeric value ranges) and aggregation that neither keyword nor semantic search can do well. Also provides the link back to the source record in OpenMRS via resource_type and resource_id.

Elasticsearch's strength is that it can combine all three in a single query — kNN on the vector, BM25 on the text, and filters on the metadata — making the three-component model a natural fit.

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
Exclude observation dates from the text that is embedded. Include dates as structured metadata fields in the Elasticsearch document and prepend them to the text only at LLM prompt assembly time.

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
