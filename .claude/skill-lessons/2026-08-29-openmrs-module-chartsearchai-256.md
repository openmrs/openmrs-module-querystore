# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #256 / PR 329 · 2026-08-29
outcome: converged
rounds: 3   cycles: 4 (harden)   verifier: ran twice (works at runtime; second run on the merged head)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-256/d1f97794-a266-43ce-bb53-a90449c951c5.jsonl

## Refuted by measurement
- "the remainder is in the pairwise arms" (the ticket's own attribution) -> the growth is drugs-in-play x ACTIVE ORDERS, a product, not D^2; a probe attributed 77% of a ten-drug pass to orderPartners, and a chart-less pass (which bounds the pairwise arms) costs 30 ms of 490 · cost: caught before any code, in planning
- "the pairwise arms could stop enumerating at the cap" (the ticket's proposed remedy) -> the #131 ordering requirement does force full enumeration, AND it does not matter: it could recover at most the 30 ms chart-less bound · cost: 0
- refutation-gate objection "ruleAbout is the same D x O product the plan claims to fix" -> structurally right, magnitude wrong: measured 0/0/3/5 sweeps at D=1/2/5/10 even on a chart built so every order shares a subgroup with an in-play drug, ~1% of the total. Fixed anyway (second copy of one defect) rather than accepted · cost: 0 rounds, settled at the gate
- "unambiguouslyNames' own sweep is one scan beside sweeps that were already there" (an UNCHANGED neighbour's javadoc) -> falsified by this change; ruleAbout no longer sweeps per partner code · cost: 1 harden cycle
- "a field REASSIGNED once per pass is the only shape the counting cases cannot see" -> the lazily-initialised field shape IS caught behaviourally (4 of 1556 tests) · cost: 1 harden cycle

## Raised by a fresh agent, missed by the author
- [harden p2] classRelationships took `context` beside the memo and used it only to null-check — two parameters carrying one fact · blocking-equivalent · cost: 1 pass
- [harden p2] MY OWN `git checkout -- <file>` during mutation testing silently reverted five uncommitted production edits; the commit that followed described changes absent from its diff · cost: 1 pass, caught only because a later agent diffed the commit against the claim
- [harden p2-p6] FIVE successive evasions of the structural guards, each demonstrated with the whole suite green, each fix opening the next: a memo field typed as something other than CoMedications; a parenthesised initialiser; an `@SuppressWarnings("…")` prefix; a method reference (`this::entryForAtcCode`); a call-shape needle. Settled only by switching question-type — `getDeclaredFields` for the field budget, name-over-bodies for the resolvers · cost: 5 passes
- [harden p7] ADR numbering collided with origin/main's Decision 52 (#296), and the new decision had no ToC entry; the branch was one commit behind · cost: 1 pass, fixed by rebase
- [r1] the javadoc claimed the memoising overload was the ONLY caller of the uncached sweep and that the guard would redden on a second; both false — while the two overloads shared one name the guard had to permit it at every cache-passing site, so dropping the `cache` argument reinstated a per-(subject,partner,code) walk with 1562 tests green · blocking · cost: 1 round
- [r1] the reflective field-budget guard exempted every `static final` field, so a `static final Map` cache — the idiomatic way to write one — passed it · non-blocking · cost: 0

- [r3] `main` advanced again mid-run (#238 / PR #327, 408 lines in the SAME class) and took Decision 53, the number this branch had used — the second ADR-number collision of one run. Resolved by merging (not rebasing: the branch was already pushed and under review, so a rebase would need a force-push) and renumbering this branch's to 54 · non-blocking · cost: 1 round, requested by the user
- [r3] the PR description went stale at the merge — it cited its own ADR entry by the old number and reported the pre-merge test count · non-blocking · cost: 0

## Where a skill blocked or contradicted this run
- pr-harden:State — `gate-state ... --only pr` is documented but the installed helper rejects `--only`; the flag exists only on `await`/`clear-await`. `reviewed-sha` had to be re-run without it.
- harden:Phase 2 — "spawn four parallel agents" plus "mutate for evidence" is the same hazard the skill warns about one level up; `isolation: "worktree"` resolved it, but the briefs had to carry a NEVER-`mvn install` rule too, since four agents installing into one shared slot-1 repo is the stale-api-jar trap by another route.
- harden:Termination — the doc-only-cycle carve-out kept the run honest but each single-word correction still bought a full confirming cycle; three cycles ended on one edit each.

## Declined
- `PatientClinicalContext.hasActiveDrug` re-folds pass-invariant strings (251,788 `matchesOrderName` calls over 43 distinct names in one ten-drug pass; prototype saved 0.97 ms/drug) — if we ship without it, a ten-drug polypharmacy question costs ~10 ms more than it needs to, on a request measured in hundreds of ms. The fix is in `PatientClinicalContext`/`DrugReference`, which this slice does not touch.
- Sharing one co-medication resolution across a request's TWO validate passes — if we ship without it, each request resolves the chart twice instead of once. It is a change across the injector/validator boundary, not this one.
- Migrating the two older source-scanning copies onto `SourceScan` — if we ship without it, three copies of the idiom exist and can drift; they already differ in block-comment handling. Outside this change.
- Closing the `getDeclaredMethod`-with-a-string-literal evasion — if we ship without it, an author who deliberately writes reflection into a private call in the same class reinstates the sweep with the suite green. Every other closed shape is a way ordinary code gets WRITTEN; this one is not an accident, and no textual guard closes the family, only pushes it one syntax along.

- "a mutation that leaves the suite green shows the guard is dead" -> not when the mutation fails to COMPILE. My first post-merge check of the arm-resolves-the-chart guard reported zero red because `classRelationships` no longer takes `context`, so nothing ran; re-run in a compiling form it reddens two cases. Check the build output, not the test count.

## Assumptions review overturned
- "the source-scan machinery is a third copy, deliberately kept independent (per ChipSubjectOneResolutionTest)" -> that javadoc says only that OrderPartnerNameSourceWritePathTest's FILE LOCATOR is kept apart; ModuleSourceRoot's own javadoc records the repo's real threshold, extraction at the THIRD caller · harden p2
- "a declaration split across lines evades MUTABLE_FIELD" (my own named residue) -> false, `\s+` spans a newline; the real residue was an annotation prefix · harden p4
- "`Fixes #256` may be wrong since the PR refutes the ticket's premise" -> reviewer judged the ticket's two asks both discharged and the extra fix over-delivery; keyword correct · r2
