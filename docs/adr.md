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
9. [Coded Fields — Store Both UUID and Name](#decision-9-coded-fields--store-both-uuid-and-name)
10. [Voided Records — Deleted from the Read Store, Not Marked](#decision-10-voided-records--deleted-from-the-read-store-not-marked)
11. [Retired Metadata — Data References Preserved, Names Snapshotted](#decision-11-retired-metadata--data-references-preserved-names-snapshotted)
12. [Sync Mechanism — Events First, AOP as Last-Resort Gap Filler](#decision-12-sync-mechanism--events-first-aop-as-last-resort-gap-filler)

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
Data in OpenMRS spans multiple resource types: patients, encounters, visits, appointments, observations, conditions, diagnoses, drug orders, test orders, allergies, patient programs, and medication dispenses. These types have different fields and different query patterns. The data can be stored in a single Elasticsearch index with a `resource_type` discriminator or in separate per-type indices.

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
  "value_coded_uuid": null,
  "value_coded_name": null,
  "value_text": null,
  "value_datetime": null,
  "value_boolean": null,
  "units": "mmol/L",
  "interpretation": "ABNORMAL",
  "status": "FINAL",
  "comment": null,
  "obs_group_uuid": null,
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

**Condition** (openmrs_conditions index):
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

**Drug Order** (openmrs_drug_orders index):
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
  "comment": null,
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

**Patient** (openmrs_patients index):
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

**Encounter** (openmrs_encounters index):
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

**Visit** (openmrs_visits index):
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

**Appointment** (openmrs_appointments index):
```json
{
  "patient_uuid": "8a7b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d",
  "resource_type": "appointment",
  "resource_uuid": "1cdaf57a-6322-b468-1c9d-c735bac38488",
  "date": "2025-04-10",
  "text": "Appointment: Adult Diabetes Clinic follow-up. Status: Scheduled. Service: Adult Diabetes Clinic at Kenyatta National Hospital",
  "embedding": [0.028, -0.039, 0.064, ...],
  "service_uuid": "2deb068b-7433-c579-2dae-d846cbd49599",
  "service_name": "Adult Diabetes Clinic",
  "service_type_uuid": "3efc179c-8544-d68a-3ebf-e957dce5a6a0",
  "service_type_name": "Follow-up",
  "status": "Scheduled",
  "start_date_time": "2025-04-10T09:00:00",
  "end_date_time": "2025-04-10T09:30:00",
  "appointment_kind": "Scheduled",
  "comment": null,
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
| `resource_type` | Distinguish record types (e.g., "obs", "condition", "diagnosis", "drug_order", "test_order", "allergy", "program", "medication_dispense", "patient", "encounter", "visit", "appointment"); route documents to the correct per-type index |
| `resource_uuid` | Link back to the source record in OpenMRS (e.g., the obs UUID, condition UUID, order UUID, allergy UUID, patient UUID, encounter UUID, etc., depending on the resource_type) |
| `date` | Date range filtering and sorting (e.g., "labs from last 6 months", "most recent vital signs") |
| `text` | BM25 keyword search matches against it; the embedding model was run on it; the LLM reads it when generating answers |
| `embedding` | Dense vector for semantic similarity search (e.g., "blood sugar control" matching an HbA1c result) |
| `concept_uuid` | Exact filtering by concept without relying on text matching (e.g., "all HbA1c results for this patient") |
| `concept_name` | Human-readable concept name in the deployment's configured locale (see [Decision 8](#decision-8-locale-specific-serialization-with-multilingual-embeddings)); supports keyword search and display |
| `concept_class` | Filter by category of clinical data (e.g., "Test", "Drug", "Diagnosis") |
| `value_numeric` | Numeric range queries (e.g., "HbA1c values above 7", "systolic BP over 140") |
| `value_coded_uuid` | Exact filtering by coded answer concept (e.g., "all HIV-positive results", "all Yes answers to a symptom question") — null for numeric or text obs |
| `value_coded_name` | Human-readable coded answer name for display and keyword search — null for numeric or text obs |
| `value_text` | Raw free-text observation value for substring search and display — null for numeric or coded obs |
| `value_datetime` | (obs) Date/time observation value (e.g., "Date of last menstrual period"); null for non-datetime obs |
| `value_boolean` | (obs) True/false observation value (e.g., "Pregnant: yes/no"); null for non-boolean obs |
| `obs_group_uuid` | (obs) UUID of the parent obs when this obs is part of a group (e.g., a BP panel with systolic and diastolic children); null for ungrouped obs |
| `status` | (obs / medication_dispense / appointment) Lifecycle state — for obs: FINAL / PRELIMINARY / AMENDED; for dispense: status of the dispense; for appointment: scheduling state |
| `comment` | Free-text clinician note attached to the record (obs, allergy, appointment); supports BM25 search |
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
| `reactions` | (allergies) Flat array of reaction names in the deployment's configured locale. Reaction UUIDs are intentionally omitted: reactions are always used as a refinement filter alongside `allergen_uuid`, never as the primary query axis (nobody queries "all patients with anaphylaxis" without first filtering by allergen or patient). Name-based matching is sufficient in this secondary role. The tradeoff accepted is that names are locale-dependent and mutable — if reaction-level UUID filtering becomes a real use case, adding a parallel `reaction_uuids` array is a serializer change plus a full re-index of `openmrs_allergies`; no schema migration or data loss is involved since the query store can be rebuilt from source at any time (see [Decision 1](#decision-1-cqrs-pattern--separate-read-store-from-transactional-database)). |
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
| `start_date_time` / `end_date_time` | (visits / appointments) Full timestamp boundaries; visits and appointments are time-ranged rather than single-instant events. For visits, `end_date_time` is null when the visit is still active |
| `active` | (visits) Boolean computed from `end_date_time IS NULL`; redundant but enables faster filtering of active vs. closed visits |
| `indication_uuid` / `indication_name` | (visits) Coded reason for the visit; UUID enables locale-independent filtering per [Decision 9](#decision-9-coded-fields--store-both-uuid-and-name) |
| `encounter_uuids` | (visits) Array of encounter UUIDs that belong to this visit; lets a visit document be the entry point for traversal without a reverse lookup |
| `service_uuid` / `service_name` | (appointments) Coded clinical service (e.g., Adult Diabetes Clinic); UUID enables locale-independent filtering per [Decision 9](#decision-9-coded-fields--store-both-uuid-and-name) |
| `service_type_uuid` / `service_type_name` | (appointments) Sub-categorization within a service (e.g., Follow-up, Initial visit) |
| `appointment_kind` | (appointments) Scheduled / WalkIn / Virtual; distinct from `status` which tracks the appointment's lifecycle |
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
| Delete from index | Document is removed from Elasticsearch | Simplest reads; no audit on read side |
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
1. Data documents that reference retired metadata are kept unchanged. No data is removed, retracted, or rewritten in response to a retirement event.
2. No `retired` flag is added to data documents.
3. Denormalized metadata names (e.g., `concept_name`, `location_name`, `provider_name`, `drug_name`, `encounter_type_name`, `form_name`, `program_name`, `service_name`, etc.) reflect the value at index time. They are not re-fetched when the underlying metadata is retired or renamed.
4. Direct metadata indices (e.g., a hypothetical `openmrs_concepts` for picker UX) are not in scope. If introduced later, their retirement-handling rules are a separate decision and will likely differ — those indices exist precisely to drive new-entry filtering, where retirement *is* a primary query axis.

### Rationale
1. **Retirement is forward-looking, not retroactive.** Its purpose is to prevent future use of a metadata entry, not to invalidate historical references. A diabetes diagnosis recorded against a since-retired concept is still a real diagnosis — the patient was diagnosed, the record reflects what happened. Treating retirement as if it were voiding would silently rewrite clinical history.
2. **Adding a `retired` field gives consumers nothing actionable.** Unlike voided data (which must be hidden by default for safety), retired metadata is *expected* to appear in historical references. There is no default filter consumers should apply, so the field would only add storage cost and confusion.
3. **Denormalized names go stale on any metadata change.** Renames during retirement are one instance of a broader phenomenon — concept names, location names, and provider names all drift. The query store accepts eventual consistency on metadata names per [Decision 1](#decision-1-cqrs-pattern--separate-read-store-from-transactional-database); re-indexing is the standard remedy when freshness matters. Treating retirement renames specially would create a one-off code path for a problem that already has a general solution.
4. **Metadata-state queries belong in core.** Questions like "is this concept currently retired?" or "what concept replaced this retired one?" are metadata management concerns, not query workloads over clinical data. The read store is optimized for the latter; the former is core's job.

### Consequences
- Sync logic ignores retirement events on metadata records. Only voiding (data records) and changes to indexed data trigger read-store mutations.
- Denormalized metadata names may be out of sync with core after a retirement-driven rename until the next re-index. Consumers requiring authoritative metadata names must consult core or trigger a re-sync.
- Aggregations over denormalized names (e.g., "obs count grouped by `concept_name`") may attribute records to the pre-retirement name. UUID-based aggregations are unaffected, which reinforces [Decision 9](#decision-9-coded-fields--store-both-uuid-and-name)'s rationale for storing both UUID and name.
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

### Consequences
- This module depends on the OpenMRS Event module and a JMS broker (ActiveMQ by default). Deployments must run this infrastructure.
- A gap inventory must be maintained: for each indexed resource type (obs, conditions, diagnoses, drug_orders, test_orders, allergies, programs, medication_dispense, patients, encounters, visits, appointments), record whether core emits create / update / void events and at what granularity. Gaps drive either upstream PRs to core or a scoped AOP shim.
- Any AOP introduced as a gap filler must be documented with the entity type it covers, the core gap it works around, and a removal plan tied to a future core version.
- Event payloads in OpenMRS are often minimal (UUID + action). Handlers therefore fetch the full entity from core after receiving an event. This means the sync path performs reads against the transactional database — acceptable, but worth noting since it couples sync throughput to core's read performance.
- Lost events on broker restart are possible. Reliability, monitoring, and reconciliation are not solved by this decision and are tracked as separate open questions.
- The initial bootstrap / backfill mechanism is not specified here. The steady-state mechanism only handles changes from the moment the projection is running; getting from "empty index" to "in sync" is a separate concern, also tracked below.

---

## Open Questions

Design questions that have been recognized but not yet resolved. Each item below is self-contained and should be deleted from this list once it is promoted to a numbered decision above. New items can be appended as they are surfaced.

### Initial backfill / bootstrap
[Decision 12](#decision-12-sync-mechanism--events-first-aop-as-last-resort-gap-filler) covers steady-state sync but not how the read store reaches "in sync" the first time, after a full rebuild, or after adding a new indexed resource type to an existing deployment. Likely shape: a one-time service-API scan that paginates through every entity of each type, serializes it, generates embeddings, and writes through index aliases. Decision needed on chunking strategy, throttling to avoid overloading core, embedding-generation throughput, progress tracking, and how the steady-state event subscription is started without missing events emitted during the backfill window.

### Sync reliability and reconciliation
The Event module can lose events on broker restart, and consumer-side failures can drop messages even when delivery succeeded. [Decision 12](#decision-12-sync-mechanism--events-first-aop-as-last-resort-gap-filler) acknowledges this without solving it. Decision needed on: durable subscription configuration, dead-letter handling for permanently-failing events, periodic reconciliation (e.g., per-patient or per-type record-count comparisons against core to detect drift), and a remediation path when drift is detected (targeted re-sync vs. full rebuild). Related to but distinct from the bootstrap question above.

### Embedding model versioning
[Decision 8](#decision-8-locale-specific-serialization-with-multilingual-embeddings) specifies a model class (multilingual-e5) but not a specific model identifier or upgrade path. Embeddings from different models are not comparable, so a model change is a full re-index of every vector. Needs a decision on model pinning, a per-document `embedding_model_version` field, and how model upgrades coordinate with index aliases (see next item).

### Re-index / alias strategy
Multiple decisions ([8](#decision-8-locale-specific-serialization-with-multilingual-embeddings), [11](#decision-11-retired-metadata--data-references-preserved-names-snapshotted), and any future serializer change) imply re-indexing as the remedy. Doing this without downtime requires writing through aliases (e.g., `openmrs_obs` → `openmrs_obs_v1`, with atomic swap to `_v2` after backfill). No decision covers the alias convention, the cutover protocol, or how dual-writes are coordinated during the swap window. Several existing claims rely on this being possible.

### Authorization
Core enforces role-based privileges over patient data. The read store currently has no auth model defined. Options include: enforcing core's privileges at the query API, fronting Elasticsearch with a service that applies them, or treating the store as trusted-callers-only. The choice has material privacy implications — leaking sensitive obs (HIV status, mental health, etc.) via an unauthenticated query endpoint is the failure mode to avoid. Should be decided before any consumer is given direct access.

### Patient merge handling
When two patients are merged in core, all their clinical data is reassigned to the surviving UUID. The read store needs corresponding logic — at minimum, repointing every document keyed by the merged-away `patient_uuid`. Common operational reality in OpenMRS deployments. Decision needed on whether merges trigger an in-place update, a delete + re-index of the merged-away patient's data, or a different mechanism — and how to handle the in-flight inconsistency window.

### Concept-set and hierarchy queries
A query like "all glucose-related results" should match HbA1c, FPG, RBS, and other related concepts without enumerating every variant UUID at query time. OpenMRS has concept sets and concept hierarchies that could be denormalized into documents (e.g., a `concept_ancestor_uuids` array per obs). Decision needed on whether to support such queries directly in the index or to expand them at query time using a separate concept-relations service.

### Timestamp time-zone convention
Documents mix date-only fields (`date`, `birthdate`) with timestamp fields (`start_date_time`, `end_date_time`, `date_handed_over`). The time zone for timestamps is unspecified. UTC is the obvious default, but OpenMRS data often originates in deployment-local time and is stored without a zone offset. The convention needs to be explicit so consumers know how to interpret a value like `start_date_time = "2025-03-15T09:30:00"` and so date-range filters match consistently.

### Person vs Patient model
The `openmrs_patients` index conflates Person attributes (name, gender, birthdate, addresses, attributes) with Patient attributes (identifiers). In OpenMRS core these are separate entities — a Person can exist without being a Patient (e.g., providers, relatives). The current flattening is appropriate for a read-side projection focused on patient queries, but should be made explicit so downstream consumers do not expect an `openmrs_persons` resource type to also exist or look for non-patient Persons in this index.
