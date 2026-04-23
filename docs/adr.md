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
8. [Locale-Specific Serialization with Multilingual Embeddings](#decision-8-locale-specific-serialization-with-multilingual-embeddings)

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
5. **Future-proof for cross-patient search.** Per-patient chart search works fine with either approach since the patient_uuid filter narrows the scope. But cross-patient search at scale benefits significantly from type-specific indices.

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
  "value_numeric": 11.2,
  "units": "mmol/L",
  "interpretation": "ABNORMAL",
  "location_uuid": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "location_name": "Kenyatta National Hospital",
  "provider_uuid": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
  "provider_name": "Dr. Ochieng"
}
```

**Condition** (openmrs_conditions index):
```json
{
  "patient_uuid": "8a7b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d",
  "resource_type": "condition",
  "resource_uuid": "d4e5f6a7-8b9c-0d1e-2f3a-4b5c6d7e8f9a",
  "date": "2023-06-10",
  "text": "Condition: Type 2 Diabetes Mellitus. Status: ACTIVE. Verification: CONFIRMED",
  "embedding": [0.015, -0.062, 0.044, ...],
  "concept_uuid": "5cd3f6a0-26fe-102b-80cb-0017a47871b2",
  "concept_name": "Type 2 Diabetes Mellitus",
  "clinical_status": "ACTIVE",
  "verification_status": "CONFIRMED",
  "end_date": null,
  "location_uuid": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "location_name": "Kenyatta National Hospital",
  "provider_uuid": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
  "provider_name": "Dr. Ochieng"
}
```

**Diagnosis** (openmrs_diagnoses index):
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
  "certainty": "CONFIRMED",
  "rank": "Primary",
  "location_uuid": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "location_name": "Kenyatta National Hospital",
  "provider_uuid": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
  "provider_name": "Dr. Ochieng"
}
```

**Drug Order** (openmrs_drug_orders index):
```json
{
  "patient_uuid": "8a7b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d",
  "resource_type": "drug_order",
  "resource_uuid": "a7b8c9d0-1e2f-3a4b-5c6d-7e8f9a0b1c2d",
  "date": "2025-01-10",
  "text": "Drug order: Metformin 500mg. Dose: 1.0 Tablet(s) Oral twice daily. Duration: 30 Day(s). Quantity: 60.0 Tablet(s). Action: NEW. Urgency: ROUTINE",
  "embedding": [0.042, -0.028, 0.053, ...],
  "concept_uuid": "9ab2c4d6-48ef-223c-a1eb-2238c69083d4",
  "concept_name": "Metformin",
  "drug_uuid": "f1a2b3c4-5d6e-7f8a-9b0c-1d2e3f4a5b6c",
  "drug_name": "Metformin 500mg",
  "dose": 1.0,
  "dose_units": "Tablet(s)",
  "route": "Oral",
  "frequency": "twice daily",
  "duration": 30,
  "duration_units": "Day(s)",
  "quantity": 60.0,
  "quantity_units": "Tablet(s)",
  "action": "NEW",
  "urgency": "ROUTINE",
  "location_uuid": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "location_name": "Kenyatta National Hospital",
  "provider_uuid": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
  "provider_name": "Dr. Ochieng"
}
```

**Allergy** (openmrs_allergies index):
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
  "location_uuid": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "location_name": "Kenyatta National Hospital",
  "provider_uuid": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
  "provider_name": "Dr. Ochieng"
}
```

**Patient Program** (openmrs_programs index):
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
  "outcome": null,
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
  "drug_uuid": "f1a2b3c4-5d6e-7f8a-9b0c-1d2e3f4a5b6c",
  "drug_name": "Metformin 500mg",
  "status": "Completed",
  "quantity": 60.0,
  "quantity_units": "Tablet(s)",
  "dose": 1.0,
  "dose_units": "Tablet(s)",
  "route": "Oral",
  "frequency": "twice daily",
  "date_handed_over": "2025-01-10",
  "location_uuid": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "location_name": "Kenyatta National Hospital",
  "provider_uuid": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
  "provider_name": "Dr. Ochieng"
}
```

**Test Order** (openmrs_test_orders index):
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
  "location_uuid": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "location_name": "Kenyatta National Hospital",
  "provider_uuid": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
  "provider_name": "Dr. Ochieng"
}
```

### Field descriptions

| Field | Purpose |
|---|---|
| `patient_uuid` | Filter search to a single patient's chart, or aggregate across patients |
| `resource_type` | Distinguish clinical record types (e.g., "obs", "condition", "diagnosis", "drug_order", "test_order", "allergy", "program", "medication_dispense"); route documents to the correct per-type index |
| `resource_uuid` | Link back to the source record in OpenMRS (e.g., the obs UUID, condition UUID, order UUID, or allergy UUID depending on the resource_type) |
| `date` | Date range filtering and sorting (e.g., "labs from last 6 months", "most recent vital signs") |
| `text` | BM25 keyword search matches against it; the embedding model was run on it; the LLM reads it when generating answers |
| `embedding` | Dense vector for semantic similarity search (e.g., "blood sugar control" matching an HbA1c result) |
| `concept_uuid` | Exact filtering by concept without relying on text matching (e.g., "all HbA1c results for this patient") |
| `concept_name` | Human-readable concept name in the deployment's configured locale (see [Decision 8](#decision-8-locale-specific-serialization-with-multilingual-embeddings)); supports keyword search and display |
| `concept_class` | Filter by category of clinical data (e.g., "Test", "Drug", "Diagnosis") |
| `value_numeric` | Numeric range queries (e.g., "HbA1c values above 7", "systolic BP over 140") |
| `units` | Filter or group by unit of measurement |
| `interpretation` | Filter by clinical interpretation (e.g., "all abnormal results") |
| `location_uuid` | Exact filtering by location, avoiding ambiguity from duplicate or similar location names |
| `location_name` | Human-readable location name for display, keyword search, and aggregation (e.g., "obs count per facility") |
| `provider_uuid` | Exact filtering by provider, avoiding ambiguity from duplicate or similar provider names |
| `provider_name` | Human-readable provider name for display, keyword search, and workload analysis |

### Rationale
Each component serves distinct purposes that the others cannot fulfill:

- **Text chunk** (`text`): BM25 keyword search matches against it. The embedding model was run on it. The LLM reads it when generating answers. Without it, you can find a match but have nothing to display or feed to the LLM.
- **Vector embedding** (`embedding`): Enables semantic similarity search. Without it, you can only do keyword matching.
- **Structured metadata** (all other fields): Enables precise filtering, sorting, aggregation, and linking back to source records. Neither keyword nor semantic search can reliably answer queries like "labs from last 6 months with value above 7 at Kenyatta National Hospital."

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

### Rationale
1. **Clinicians search in their own language.** A French-speaking clinician will type "glycémie" not "blood glucose." The serialized text and BM25 index should match the language they use.
2. **Multilingual embedding models handle cross-language similarity.** Models like `multilingual-e5` are trained across 100+ languages and produce comparable vectors for semantically equivalent text regardless of language.
3. **Single serialization per record keeps storage and indexing simple.** Storing multiple locale variants per record would multiply storage cost and complicate synchronization without proportional benefit.
4. **Consistent with OpenMRS conventions.** OpenMRS already resolves concept names to the configured locale throughout its UI and APIs.

### Consequences
- The embedding model must be multilingual. Monolingual models (e.g., English-only) should not be used.
- Deployments that change their default locale after initial indexing will need to re-serialize and re-index existing records.
- Cross-deployment searches (e.g., a research network spanning French and English sites) would require additional consideration, potentially storing an English canonical form alongside the localized text.
