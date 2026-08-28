# resolve-ticket + pr-harden · openmrs-module-fhir2 · FM2-700 / PR 629 · 2026-08-28
outcome: converged
rounds: 1 (pr-harden)   cycles: 6 (harden)   verifier: ran (see below)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-fhir2/8d5826e3-4e03-414b-889d-aadc3fd40a1b.jsonl

## Refuted by measurement
- Plan rev 1: "the JPA-criteria migration ee174cd3 moved the predicate onto targetUuid, so the DAO
  predicate is correct and the write path is the root cause" -> `git show ee174cd3` emits
  `equal(join.get("reference"), idPart)`; `git log -S targetUuid` returns exactly a2bc7a9a
  (FM2-682/#607, tagged 4.1.0+4.2.0). The regression is the DAO, not the write path. · cost: 1 gate pass
- Plan rev 1: "fix the write path + liquibase backfill" -> refuted by two citations: nothing in the
  repo tests liquibase at all, and target_uuid is char(38) under STRICT_TRANS_TABLES so a 39-64 char
  FHIR id would turn a currently-succeeding POST into a hard failure. · cost: 1 gate pass
- "a bare id of 39-64 chars" and every other exhaustive/universal claim written into comments: SIX
  separate false sentences, one found per confirming pass, five of them written while correcting the
  previous one. · cost: 5 harden cycles
- Standalone deploy: the fix appeared not to work live -> `.openmrs-lib-cache/fhir2/lib/` held BOTH
  fhir2-api-4.2.0.jar and the new snapshot jar; the stale one shadowed the fix. Wiping the module
  cache dir is required, not just replacing the .omod. · cost: 1 restart

## Raised by a fresh agent, missed by the author
- [harden c1] The `type` predicate was undiscriminated: deleting it outright left the entire Task
  suite green, and the new `reference` arm makes target_type the ONLY thing scoping a bare id.
  · blocking-equivalent · cost: 1 cycle
- [harden c2] Moving `type` INSIDE the OR (scoping only the reference arm) also survived the suite —
  the mirror of the case c1 had just pinned. · cost: 1 cycle
- [harden c1] a2bc7a9a added `target_uuid="<bare reference>"` to every fhir_reference row in both
  shared fixtures in the same commit that made the predicate read that column. That, not the focus
  test's shape, is why CI stayed green through the regression. · cost: 0 (explanatory)
- [harden c1] The integration test asserted nothing about its own precondition, so it would have
  gone on passing via the targetUuid branch if the write path were ever normalised. · cost: 1 cycle
- [harden c4] "a prefixed reference resolves through target_uuid" — no fixture in the repo stores a
  prefixed reference; all 13 rows hold a bare uuid. · cost: 1 cycle
- [harden c5] "the match comes through the targetUuid arm" — that row's `reference` column holds the
  same uuid, so deleting either arm leaves the case green. The test pins the scoping, not the arm.
  · cost: 1 cycle
- [pr-harden r1] nothing. Zero findings on the pushed head.

## Where a skill blocked or contradicted this run
- harden:Phase 2 — `isolation: "worktree"` agents produced 156-byte transcripts that never grew, and
  I read that as a stall and killed two mid-investigation. Their kill notices showed both were
  working. Transcript size/mtime is NOT a liveness signal for these agents; the worktree's own
  `target/` mtime is. · cost: ~20 min and two discarded agents
- pr-harden:COMMIT — I ran `git add -A` before reverting spotless churn and committed 27 unrelated
  reformatted files. Caught immediately, but the skill's warning is about the commit, and the actual
  trap is that `mvn install` re-dirties the tree between the revert and the commit. · cost: 1 amend
- harden:Termination — `git checkout -- <path>` used to restore a mutation probe silently discarded
  an uncommitted comment edit in the same file. The skill warns about this; I still hit it. Restoring
  from a `cp` copy avoided it the second time. · cost: 1 re-apply

## Declined
- `?based-on=<bare uuid>` with no resource type voids the filter and returns every Task (measured
  331/331 live) — if we ship without this, a client filtering by a bare id silently gets the whole
  Task table instead of one Task, and cannot tell. Declined as out of scope: FM2-700's reproduction
  uses the typed form, and fixing it changes behaviour the ticket does not describe. Recorded in the
  PR body and to be filed separately.
- `hasDistinctResults()` not overridden, so duplicate basedOn refs inflate Bundle.total — if we ship
  without this, a Task with two basedOn refs matching one search reports total=2 with 1 entry.
  Declined: pre-existing on master, constructible without this change, and the fix costs a second
  round-trip on every Task search.

## Assumptions review overturned
- "The regression came from the Hibernate->JPA criteria migration" -> it came from FM2-682/#607, a
  one-line change in a feature PR, four commits later. (gate pass 1)
- "The write path is the root cause and must be normalised" -> the read predicate is the regression;
  normalising the write path cannot reach the 331 rows already stored. (gate pass 1)
- "A test that creates the row through the production path pins the fix" -> it pins nothing durably,
  because it relies on target_uuid happening to be null; a fixture with the column absent is what
  pins it. (gate pass 2)
