# resolve-ticket (+harden, pr-harden) · openmrs-module-chartsearchai · #477 → PR #509 · 2026-09-24
outcome: converged (pr-harden round 2: 0 blocking on 5adab0d9, verified same sha); harden ended on a labelled override
rounds: 2   cycles: 5 (harden)   verifier: ran (works at runtime, no repairs)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-477/282ce6a6-0d7e-42a9-ac6f-78885a9d52b0.jsonl

## Refuted by measurement
- plan: raise the finding on drug-in-play questions too, anchored on orders carrying the in-play drug -> two existing tests pin that question's finding list and the anchor read inPlay (answer-dependent); leg deleted at gate pass 1 · cost: 0 rounds
- plan: enumerate candidates via substancesItsDisplayNames (findNamedSubstances) -> reference/CLAUDE.md forbids a candidate set from it; enumerate orderEntries instead (gate pass 2) · cost: 0
- "a set sort in chart order is needed" -> mutation removing it reddened nothing; deleted (harden P1) · cost: 0
- "the strength sort is a defence nothing observes" (main's javadoc) -> false once this finding existed; then false again the other way after r1-2 moved insertion · cost: 2 edits
- README rewording "opens with its strongest finding" -> false beside an allergy finding (grouping key); reverted to main's sentence · cost: 1 harden P2 pass

## Raised by a fresh agent, missed by the author
- [harden P2a] finding silenced #401's "screen related nothing" note (nothingResolved used findings.isEmpty()) · substantive
- [harden P2a] module answer led with the unrated finding (after-every-pair ordering then written) · substantive
- [harden P2b] model path would still lead with it (prompt ranking) -> one chart opened two ways by GP; ordering exemption reverted · substantive
- [harden P2b] chip `drug` label promised by README untested (mutation green) · substantive
- [harden P2c] ordering test could not tell "rank by strength" from "always first" (no Major in fixture) · substantive
- [harden P2d] README rewording false beside allergy finding · substantive
- [r1] chip list order disagreed with module answer (appended after cautions) · non-blocking, implemented
- [r1] in-play negative test pinned #477's still-open silence as spec · non-blocking, implemented
- [refuter p2] edited production file mid-flight as a probe while I was editing; my first CoMedications edit landed on its probe · cost: redo

## Where a skill blocked or contradicted this run
- resolve-ticket Step 1: `gate-state pr-set` at Step 1 merged into a stale #483 entry (pr:483, declined, reviewed_shas) rather than replacing it; noticed at Step 8 via `show`, cleared manually
- harden Phase 2 "runs once" + severity escalation looped 4 times on the run's own fixes (each pass found a real defect introduced by the previous fix); ended on labelled override
- refutation gate agents mutate production files for evidence while the orchestrator works (skill says don't edit while delegated agents run — I edited during gate pass 2)
- reference/CLAUDE.md byte budget left 74 bytes; main's merge consumed it; the rule pointer for the new referent source had to be dropped (r1-5 declined)
- main took ADR Decision 113 mid-run; renumbered to 114

## Declined
- r1-3 local vs systemic presentation — if we ship without it, hydrocortisone cream + tablet raises "possible duplicate therapy" with the change clause, because the finding reads displays and no site
- r1-5 CLAUDE.md referent pointer — a reader of that file alone could miss that this finding answers isAboutACurrentMedication true (javadoc carries it)

## Assumptions review overturned
- A2 (drug-in-play anchor) -> deleted at gate pass 1; screening questions only
- A3 (one chip per substance) -> one per order set (Decision 99), gate pass 1 non-blocking
