# Claude guidance for openmrs-module-querystore

Short orientation for sessions starting implementation work.

## Source of truth

[`docs/adr.md`](docs/adr.md) is the authoritative spec. Implementation must follow it. The conversation history that produced any given decision is *not* authoritative — if the ADR contradicts something a previous session said or remembered, the ADR wins.

When making non-trivial implementation choices: check whether the ADR already covers it. If yes, follow. If no, add to the ADR's [Open Questions](docs/adr.md#open-questions) before committing to a direction. The doc has a Conventions section near the top covering append-only-ness, supersession, and how open questions are mutated.

## Principles to internalize

The ADR is dense; here is the minimal load-bearing set with anchors.

- **CQRS + self-sufficiency** ([Decision 1](docs/adr.md#decision-1-cqrs-pattern--separate-read-store-from-transactional-database)). The read store answers its declared queries without round-tripping to core. Core is permitted only for content fetches (binaries, audit history). Going to core to enrich a result or fall back on un-indexed patterns is a smell — denormalize, or decide whether to index, or declare out of scope.
- **Per-type indices, `querystore_` prefix** ([Decision 4](docs/adr.md#decision-4-per-type-indices-over-a-single-index)). Core types are `querystore_<type>`; module-contributed types are `querystore_<moduleid>_<type>`. Cross-type queries use `querystore_*`.
- **Plain-text serialization** ([Decision 5](docs/adr.md#decision-5-plain-text-serialization-over-json-or-fhir)). Labeled prose, not JSON or FHIR.
- **Document = text + embedding + structured** ([Decision 6](docs/adr.md#decision-6-document-model--text-embeddings-and-structured-metadata)). Cross-cutting field contract applies to every document.
- **Date separation** ([Decision 7](docs/adr.md#decision-7-date-separation--excluded-from-embeddings-included-at-query-time)). Record timestamps in metadata, not in embedded text. Clinically significant dates (onset, resolution) belong in embedded text.
- **Locale + multilingual embeddings** ([Decision 8](docs/adr.md#decision-8-locale-specific-serialization-with-multilingual-embeddings)).
- **Coded fields = UUID + name** ([Decision 9](docs/adr.md#decision-9-coded-fields--store-both-uuid-and-name)). Default is both. Exception only for small, stable, locale-invariant value sets.
- **Voided → delete, retired → preserve** ([Decisions 10](docs/adr.md#decision-10-voided-records--deleted-from-the-read-store-not-marked) and [11](docs/adr.md#decision-11-retired-metadata--data-references-preserved-names-snapshotted)).
- **Sync = events first** ([Decision 12](docs/adr.md#decision-12-sync-mechanism--events-first-aop-as-last-resort-gap-filler)). Core's in-process event infrastructure (PR #6084, merged on 2.9.x) is the target; the module's AOP bridge is the pre-2.9 gap filler, selected via `querystore.syncMode`. CDC (#6151 + the pre-alpha debezium module) is the reserved heavier-coverage path.
- **Module contributions via SPI** ([Decision 13](docs/adr.md#decision-13-module-extension-spi-service-provider-interface-for-custom-resource-types)). Modules contribute resource types via the contribution interface; querystore knows nothing about specific modules.

## Implementation priorities

The fastest path to a useful querystore is unblocking [chartsearchai's migration](docs/migration-chartsearchai.md). Authorization and the v1 consumer API surface are now settled by [Decision 14](docs/adr.md#decision-14-authorization-and-consumer-api-surface). The remaining four [open questions](docs/adr.md#open-questions) on the chartsearchai-blocking path:

1. [Patient merge handling](docs/adr.md#patient-merge-handling)
2. [Initial backfill / bootstrap](docs/adr.md#initial-backfill--bootstrap)
3. [Long-text chunking for embeddings](docs/adr.md#long-text-chunking-for-embeddings)
4. [Sync reliability and reconciliation](docs/adr.md#sync-reliability-and-reconciliation)

Other open questions (migration-time re-projection hook, event-handler idempotency, embedding model versioning, complex obs handling, re-index/alias strategy, backend SPI residual, tier upgrade operational signals, name-refresh re-projection, PII scopes, concept-set queries, knowledge-base and reference-data projection, time-zone convention, Person vs Patient) can be tackled after these or in parallel, depending on what surfaces during code work.

## Project layout

- `api/` — Java module API source.
- `omod/` — module packaging.
- `docs/adr.md` — architectural decisions (authoritative).
- `docs/migration-chartsearchai.md` — chartsearchai migration gap analysis.
- `docs/chartsearchai-port-map.md` — implementation-phase pointer to which chartsearchai files are worth porting and which are not.
- `docs/spi-providers.md` — step-by-step walkthrough for modules contributing custom resource types (verified by `ProviderEndToEndTest`).
- `pom.xml` — Maven build. Depends on OpenMRS Platform 2.8.0+.

## Workflow notes

- Update `docs/adr.md` as decisions change. Use the supersession convention rather than rewriting accepted decisions.
- When you make an implementation choice the ADR doesn't address: either it's obvious from the principles above, or it's a new open question — add it.
- chartsearchai's eval dataset (153 records, query-recall benchmarks) is a real, available end-to-end validator. Once a basic indexing path exists, re-running those evals against querystore is a fast way to catch design regressions.

## Working norms

- **Verify OpenMRS APIs against the jar.** When picking a `Concept`/`Obs`/`Encounter`/etc. method, confirm it exists by disassembling `~/.m2/repository/org/openmrs/api/openmrs-api/*/openmrs-api-*.jar` with `javap -p` (or `javap -c` for behavior) rather than relying on training-data recall. The 2.x line has subtle differences that don't always surface in docs — e.g., `Concept.getSynonyms(Locale)` uses strict `Locale.equals`, while `Concept.getName(Locale)` does language-level fallback. Inventing methods or fields that don't exist is a recurring failure mode.
- **Promote emergent conventions to the ADR.** When two or more implementations of the same shape (per-type serializers, event handlers, backend backends, etc.) settle on the same pattern, capture the rule in the relevant Decision's conventions subsection so a fresh session re-derives it. Conventions that live only in conversation rot across sessions.
- **Iterative review has a stopping rule.** Successive `/simplify` or `/review` passes converge to "nothing actionable" within ~3-5 rounds for a typical slice. If agents start re-flagging items prior passes addressed, or returning stylistic-only findings, stop and ship rather than inventing concerns. Diminishing returns are a signal to commit.
- **Use `/harden` for slice hardening.** When a code slice is structurally complete (implementation done, tests passing, ready to commit), suggest `/harden` rather than manually orchestrating `/simplify` and `/review` loops. The skill runs review-first then simplify-second with built-in stopping rules. Don't auto-invoke it on every edit; it's slice-level work, not edit-level.
- **Handoff prompts stop at review, not commit.** When generating a prompt for a fresh session, sub-agent, or scheduled job, the deliverable is always "build, test, and report back for review" — never "commit" or "push" unless the user has explicitly authorized autonomous shipping. The user stays in the commit loop.
- **Mirror the closest sibling.** A new implementation of an established shape should match the most-similar shipped one. Inventing a divergent shape for similar work is a smell — surface what justifies the divergence before writing code.
- **Run `mvn -pl api install` before claiming success.** Compilation + test pass is the contract for "done." Surface real failures honestly rather than declaring partial success.
