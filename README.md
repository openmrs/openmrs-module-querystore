# OpenMRS Query Store Module

**Module ID:** `querystore`

An OpenMRS module that maintains an optimized read-side projection of clinical data, following the Command Query Responsibility Segregation (CQRS) pattern. It synchronizes data from the OpenMRS transactional database into a purpose-built read store designed for **semantic search, free-text retrieval, and embedding-ranked (kNN) clinical search** — the query shapes that transactional SQL and flattened analytical layers like MambaETL do not address.

## Table of Contents

1. [Why a Query Store?](#why-a-query-store)
2. [Scope](#scope)
3. [Why a Module and Not in Core?](#why-a-module-and-not-in-core)
4. [Architecture](#architecture)
5. [Data Model](#data-model)
6. [Supported Clinical Data Types](#supported-clinical-data-types)
7. [Extending with Custom Resource Types](#extending-with-custom-resource-types)
8. [Text Serialization](#text-serialization)
9. [REST API](#rest-api)
10. [Design Decisions](#design-decisions)
11. [License](#license)

## Why a Query Store?

OpenMRS core uses a normalized relational database (MySQL) optimized for transactional clinical workflows — recording observations, placing orders, managing patient programs. These normalized structures are not well-suited for the semantic / free-text / kNN retrieval shape:

- **Patient chart search** combining keyword (BM25) and semantic (embedding kNN) ranking
- **Clinical NLP and AI/ML pipelines** that consume indexed clinical text directly
- **Cross-type retrieval** of a patient's clinical record in a single query via the `querystore_*` wildcard

Querystore solves these specifically. The transactional database remains the source of truth for writes; querystore serves the read-side queries listed above. Structured analytical workloads — aggregations, cohort definitions, time-series reporting, "missing data" queries — are a separate query shape and are not what querystore is for; see [Scope](#scope) below for where those belong.

## Scope

Querystore is one of several read-side projections of OpenMRS clinical data. Each projection is built for a distinct query shape, and using the wrong one wastes effort.

**What querystore is for:**
- Semantic / free-text / embedding-ranked (kNN) retrieval over patient-scoped clinical data
- Hybrid search (BM25 + vector similarity) over indexed clinical records
- Cross-type retrieval across clinical resource types via the `querystore_*` wildcard

**What querystore is not for:**

| Use case | Where to look instead |
|---|---|
| Structured analytics, aggregations, cohort definitions, time-series reporting | Flattened analytical projections like MambaETL where deployed; otherwise the transactional DB or the OpenMRS Reporting module |
| Dictionary lookups — concept synonyms across locales, definitions, code-system mappings, value-set expansion | OCL (Open Concept Lab) or FHIR terminology operations (`$lookup`, `$translate`, `$expand`); the FHIR2 module exposes these on every OpenMRS install |
| "Never referenced" / missing-data queries (drugs never prescribed, locations with no encounters this quarter) | SQL against the transactional DB or an analytical projection — `LEFT JOIN ... IS NULL` / `NOT IN (...)` is both cheaper and more expressive than embedding similarity for finding absences |
| Metadata catalog search for types with only a name + structured fields (e.g., `BillableService`, `OrderType`, `EncounterType`, `VisitType`, `PaymentMode`, `LocationTag`) | Core service APIs and SQL — embedding similarity doesn't add over an exact/fuzzy name match when the catalog carries no rich free text |

Most OpenMRS metadata types fail the indexing criterion. Module authors considering a `ResourceTypeProvider` for a metadata type should first check [Decision 13's *Criteria for contributing a metadata resource type*](docs/adr.md#criteria-for-contributing-a-metadata-resource-type) — the iff rule that this Scope section condenses.

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
    ├── Cross-type clinical retrieval (querystore_* wildcard)
    ├── AI agents and LLM pipelines consuming indexed clinical text
    └── Clinical NLP over indexed records
```

## Data Model

Clinical records are stored as per-type indices (one index per clinical resource type) rather than a single mixed index. This design:

- **Avoids sparse fields** — each index has only the fields relevant to its type
- **Improves query performance** — type-specific queries only scan relevant documents
- **Produces better relevance scoring** — BM25 term frequencies are not diluted across unrelated document types
- **Supports cross-type search** — Elasticsearch wildcard patterns (e.g., `querystore_*`) allow querying across all types when needed

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
| Referral Orders | Referrals to other providers or services |
| Allergies | Drug, food, and environmental allergies |
| Patient Programs | Program enrollments, states, and outcomes |
| Medication Dispense | Dispensing records |
| Patients | Demographics, identifiers, addresses, and attributes |
| Encounters | Clinical encounters with type, providers, location, and form |
| Visits | Time-ranged visits and the encounters they contain |

## Extending with Custom Resource Types

The 12 types above are what the query store indexes out of the box. Modules can contribute their own resource types — patient appointments from an appointments module, financial bills from a billing module, radiology reports, oncology regimens, and so on — through the `ResourceTypeProvider` SPI. Contributed types share the same backend, embedding pipeline, cross-type search, and patient-cascade behavior as the core types; querystore knows nothing about specific modules.

See [`docs/spi-providers.md`](docs/spi-providers.md) for the step-by-step walkthrough — domain entity, serializer, bootstrapper, AOP advice, provider bean, Spring wiring, and verification. The walkthrough is verified by an end-to-end test that builds a `billing_bill` provider from scratch.

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

## REST API

The query store's primary in-JVM consumer surface is `QueryStoreService`. It also exposes a thin
read-only HTTP adapter and operational endpoints under `/ws/rest/v1/querystore/` (requires the
`webservices.rest` module):

- `GET /patientrecord` — expose the existing `getPatientChart`, `searchByPatient`, and `search`
  service methods to authorized non-JVM clients. Full-chart pages include a materialized
  `snapshotId`, strong conditional `ETag`, explicit clinical-date semantics, paging, and JSON
  serialization; the endpoint does not reconcile or rebuild the index on each read.
- `GET /indexingstatus` — per-resource-type bootstrap status and a derived `complete` flag ("is this
  deployment fully indexed?").
- `POST /reindex` `{"patient":"<uuid>"}` — force a full re-projection of one patient without a
  restart (repairs a partially-indexed patient that the lazy cold-touch path won't refresh).
- `POST /reindex` `{"scope":"all"}` — kick off (or resume) the full backfill over every patient on a
  daemon thread without a restart; returns `202` and runs in the background (poll `/indexingstatus`).

See the [REST API reference](docs/rest-api.md) for request/response shapes, privileges, and caveats.

## Design Decisions

See the [Architecture Decision Records](docs/adr.md) for detailed rationale behind the module's design choices.

## License

This project is licensed under the [Mozilla Public License 2.0 with Healthcare Disclaimer (MPL 2.0 HD)](https://openmrs.org/license/).
