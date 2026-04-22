# OpenMRS Query Store Module

**Module ID:** `querystore`

An OpenMRS module that maintains an optimized read-side projection of clinical data, following the Command Query Responsibility Segregation (CQRS) pattern. It synchronizes data from the OpenMRS transactional database into a purpose-built query store designed for AI applications, analytics, reporting, and any downstream system that needs to consume clinical data efficiently.

## Table of Contents

1. [Why a Query Store?](#why-a-query-store)
2. [Why a Module and Not in Core?](#why-a-module-and-not-in-core)
3. [Architecture](#architecture)
4. [Data Model](#data-model)
5. [Supported Clinical Data Types](#supported-clinical-data-types)
6. [Text Serialization](#text-serialization)
7. [License](#license)

## Why a Query Store?

OpenMRS core uses a normalized relational database (MySQL) optimized for transactional clinical workflows — recording observations, placing orders, managing patient programs. These normalized structures are not well-suited for:

- **AI/ML applications** such as patient chart search (semantic and keyword), risk prediction, clinical NLP, and cohort building
- **Analytics and reporting** that require scanning large volumes of data across patients
- **Full-text and semantic search** over clinical records

A query store solves this by maintaining a separate, denormalized projection of clinical data optimized for reads. The transactional database remains the source of truth for writes; the query store serves reads.

## Why a Module and Not in Core?

OpenMRS follows a modular architecture where core provides the platform and modules extend it. The query store is a module because:

1. **Not every deployment needs it.** Many OpenMRS sites only need the transactional database. Adding query store infrastructure to core would impose unnecessary overhead on every deployment.

2. **Implementation flexibility.** The backing store could be Elasticsearch, a PostgreSQL read replica, or another technology. Core should not be coupled to a specific search/analytics infrastructure choice.

3. **Independent release cycle.** The query store can evolve, upgrade dependencies (e.g., Elasticsearch client versions), and ship fixes without waiting for a core release.

4. **Separation of concerns.** Core owns the write side (source of truth). The query store is a read-side projection — a fundamentally different concern that belongs in its own module.

5. **Dependency isolation.** Search and analytics client libraries do not belong in core, where they would affect every module and deployment.

Core provides the hook points (events, AOP, service interfaces) that this module listens to for data changes. The module handles everything else: serialization, synchronization, indexing, and query APIs.

## Architecture

```
OpenMRS Core (write side / source of truth)
    │
    │  clinical events (obs created, order placed, condition updated, etc.)
    │
    ▼
Query Store Module
    │
    ├── Listens to clinical data changes via OpenMRS events / AOP
    ├── Serializes clinical records into text representations
    ├── Generates vector embeddings for semantic search
    ├── Indexes into the configured query store backend
    │
    ▼
Query Store Backend (e.g., Elasticsearch)
    │
    ├── Per-type indices (obs, conditions, diagnoses, orders, allergies, programs, etc.)
    ├── Full-text search (BM25)
    ├── Semantic search (dense vector kNN)
    ├── Structured filtering (by patient, date, type, concept, etc.)
    │
    ▼
Consumers
    ├── Patient chart search (hybrid keyword + semantic)
    ├── Cross-patient search and cohort identification
    ├── AI/ML pipelines (risk prediction, clinical NLP, outbreak detection)
    ├── Reporting and dashboards
    └── Research and analytics
```

## Data Model

Clinical records are stored as per-type indices (one index per clinical resource type) rather than a single mixed index. This design:

- **Avoids sparse fields** — each index has only the fields relevant to its type
- **Improves query performance** — type-specific queries only scan relevant documents
- **Produces better relevance scoring** — BM25 term frequencies are not diluted across unrelated document types
- **Supports cross-type search** — Elasticsearch wildcard patterns (e.g., `openmrs_*`) allow querying across all types when needed

Each document contains:

- **Text representation** — a plain-text serialization of the clinical record, optimized for both embedding models and LLM consumption
- **Vector embedding** — a dense vector computed from the text, enabling semantic similarity search
- **Structured metadata** — patient ID, date, resource type, concept name, and type-specific fields for precise filtering and aggregation

## Supported Clinical Data Types

| Data Type | Description |
|---|---|
| Observations | Lab results, vitals, assessments, clinical notes |
| Conditions | Active and resolved conditions |
| Diagnoses | Confirmed and provisional diagnoses |
| Drug Orders | Medication prescriptions |
| Test Orders | Lab and radiology orders |
| Allergies | Drug, food, and environmental allergies |
| Patient Programs | Program enrollments, states, and outcomes |
| Medication Dispense | Dispensing records |

## Text Serialization

Clinical records are serialized as labeled plain text rather than structured formats like JSON or FHIR. This approach:

- Is **token-efficient** — plain text uses roughly half the tokens of JSON and a third of FHIR JSON for the same clinical content
- **Embeds well** — embedding models produce better vectors from natural language than from structured formats with braces and delimiters
- Is **LLM-friendly** — labeled plain text is easy for language models to read and reason over
- Preserves **field structure** through labels (e.g., `Dose:`, `Status:`, `Severity:`) without the overhead of delimiters or tags

Example:
```
Drug order: Metformin 500mg. Dose: 1.0 Tablet(s) Oral twice daily.
Duration: 30 Day(s). Quantity: 60.0 Tablet(s). Action: NEW. Urgency: ROUTINE
```

## License

This project is licensed under the [Mozilla Public License 2.0 with Healthcare Disclaimer (MPL 2.0 HD)](https://openmrs.org/license/).
