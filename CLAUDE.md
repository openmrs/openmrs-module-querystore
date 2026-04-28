# Claude guidance for openmrs-module-querystore

Short orientation for sessions starting implementation work.

## Source of truth

[`docs/adr.md`](docs/adr.md) is the authoritative spec. Implementation must follow it. The conversation history that produced any given decision is *not* authoritative — if the ADR contradicts something a previous session said or remembered, the ADR wins.

When making non-trivial implementation choices: check whether the ADR already covers it. If yes, follow. If no, add to the ADR's [Open Questions](docs/adr.md#open-questions) before committing to a direction. The doc has a Conventions section near the top covering append-only-ness, supersession, and how open questions are mutated.

## Principles to internalize

The ADR is dense; here is the minimal load-bearing set with anchors.

- **CQRS + self-sufficiency** ([Decision 1](docs/adr.md#decision-1-cqrs-pattern--separate-read-store-from-transactional-database)). The read store answers its declared queries without round-tripping to core. Core is permitted only for content fetches (binaries, audit history). Going to core to enrich a result or fall back on un-indexed patterns is a smell — denormalize, or decide whether to index, or declare out of scope.
- **Per-type indices, `openmrs_` prefix** ([Decision 4](docs/adr.md#decision-4-per-type-indices-over-a-single-index)). Core types are `openmrs_<type>`; module-contributed types are `openmrs_<moduleid>_<type>`. Cross-type queries use `openmrs_*`.
- **Plain-text serialization** ([Decision 5](docs/adr.md#decision-5-plain-text-serialization-over-json-or-fhir)). Labeled prose, not JSON or FHIR.
- **Document = text + embedding + structured** ([Decision 6](docs/adr.md#decision-6-document-model--text-embeddings-and-structured-metadata)). Cross-cutting field contract applies to every document.
- **Date separation** ([Decision 7](docs/adr.md#decision-7-date-separation--excluded-from-embeddings-included-at-query-time)). Record timestamps in metadata, not in embedded text. Clinically significant dates (onset, resolution) belong in embedded text.
- **Locale + multilingual embeddings** ([Decision 8](docs/adr.md#decision-8-locale-specific-serialization-with-multilingual-embeddings)).
- **Coded fields = UUID + name** ([Decision 9](docs/adr.md#decision-9-coded-fields--store-both-uuid-and-name)). Default is both. Exception only for small, stable, locale-invariant value sets.
- **Voided → delete, retired → preserve** ([Decisions 10](docs/adr.md#decision-10-voided-records--deleted-from-the-read-store-not-marked) and [11](docs/adr.md#decision-11-retired-metadata--data-references-preserved-names-snapshotted)).
- **Sync = events first** ([Decision 12](docs/adr.md#decision-12-sync-mechanism--events-first-aop-as-last-resort-gap-filler)). AOP only as a scoped, time-bound gap filler tied to a future core PR.
- **Module contributions via SPI** ([Decision 13](docs/adr.md#decision-13-module-extension-spi-service-provider-interface-for-custom-resource-types)). Modules contribute resource types via the contribution interface; querystore knows nothing about specific modules.

## Implementation priorities

The fastest path to a useful querystore is unblocking [chartsearchai's migration](docs/migration-chartsearchai.md). Authorization and the v1 consumer API surface are now settled by [Decision 14](docs/adr.md#decision-14-authorization-and-consumer-api-surface). The remaining four [open questions](docs/adr.md#open-questions) on the chartsearchai-blocking path:

1. [Patient merge handling](docs/adr.md#patient-merge-handling)
2. [Initial backfill / bootstrap](docs/adr.md#initial-backfill--bootstrap)
3. [Long-text chunking for embeddings](docs/adr.md#long-text-chunking-for-embeddings)
4. [Sync reliability and reconciliation](docs/adr.md#sync-reliability-and-reconciliation)

Other open questions (event-handler idempotency, embedding model versioning, complex obs handling, re-index/alias strategy, PII scopes, concept-set queries, time-zone convention, Person vs Patient) can be tackled after these or in parallel, depending on what surfaces during code work.

## Project layout

- `api/` — Java module API source.
- `omod/` — module packaging.
- `docs/adr.md` — architectural decisions (authoritative).
- `docs/migration-chartsearchai.md` — chartsearchai migration gap analysis.
- `pom.xml` — Maven build. Depends on OpenMRS Platform 2.8.0+.

## Workflow notes

- Update `docs/adr.md` as decisions change. Use the supersession convention rather than rewriting accepted decisions.
- When you make an implementation choice the ADR doesn't address: either it's obvious from the principles above, or it's a new open question — add it.
- chartsearchai's eval dataset (153 records, query-recall benchmarks) is a real, available end-to-end validator. Once a basic indexing path exists, re-running those evals against querystore is a fast way to catch design regressions.
