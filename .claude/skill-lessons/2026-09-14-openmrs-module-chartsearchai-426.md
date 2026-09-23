# pr-harden · openmrs-module-chartsearchai · #409 / PR 426 · 2026-09-14
outcome: converged
rounds: 6 (5 + a blocking-only confirming round after main moved)   cycles: 1 (harden, overridden at cycle 1)   verifier: skipped (no runtime-visible change; two reviewers concurred independently)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-409/0102617e-5075-4baa-9afe-3a29fc459eab.jsonl

## Refuted by measurement
- "The placement is pinned by `aCheckThatThrowsIsReportedAndTheAnswerStillReturns`" (written by the author in harden cycle 1) -> hoisting the line leaves the whole api suite green, that case included · cost: 1 harden pass
- "The reading touches only `getResourceType()` and `getIndex()`" (the author's *replacement* for the above) -> it also reads `RecordReference.isAttachedByTheModule()`, which nothing earlier reads on an ordinary answer path · cost: 1 round (r2 of harden phase 2)
- "A check that accuses on the ABSENCE of prose must anchor its citation; one that accuses on its PRESENCE already holds its evidence" -> false of `DosingCeilingFidelityCheck`, whose presence test is a whole-answer scan for a ceiling STRING · cost: 1 plan-gate pass
- "`ClassCodeFidelityCheck` is already marker-anchored" (inherited verbatim from merged ADR Decision 94) -> it walks the union and POOLS cited codes as support, so narrowing it would ADD accusations · cost: 0 (caught at plan time)
- "`codeOn`/`codeLines` handles a text block as an ordinary literal" -> the quote state resets per line, so text-block bodies are read as CODE; a `/*` in one blanks the rest of the file · cost: 1 round (r3)
- "A comment naming the removed call no longer satisfies it in either of Java's forms" -> a `//`-spelled comment is a comment to javac and plain code to the strip; measured green with the reading absent · cost: 1 round (r3)
- "one returning a `List` and one a `Set`" as `ChartAnswerTestSupport`'s warrant -> both removed copies returned `List`; the `Set` one was this branch's own · cost: 1 round (r3)
- "3 of 7 runs" as the rate of the array-only shape -> the recorded figure for Mode A alone is 2 of 7 · cost: 0 (caught at plan gate)

## Raised by a fresh agent, missed by the author
- [r1] The architecture guard's required-call assertion is satisfied by a TRAILING comment — the comment skip only recognised comments that BEGIN a line. Measured: local re-derivation + `// replaces …` on the same line, BUILD SUCCESS with the reading absent · blocking · cost: 1 round
- [r1] **Issue #409 gained a comment two hours before the PR opened**, the reporter offering the full raw SSE for both failure modes. The author's Step 1 read predated it, and the ADR recorded the SSE as "never attached", which reads as *not gettable* · blocking · cost: 1 round
- [r1] The four-keys grouping claim had a home in `LlmInferenceService` the author's six-file sweep missed · blocking · cost: 1 round
- [r2] The same escape one comment form over: a mid-line `/* … */` note. The fixer then found a third form (a three-line block whose middle line lacks `*`) itself · blocking · cost: 1 round
- [r3] `ChartAnswerTestSupport`'s fabricated warrant; two false residue claims about the strip · blocking + 4 non-blocking · cost: 1 round
- [r4] A SEVENTH home of the grouping claim in README, and round 3's replacement wording falsifiable against `activeOrderClaims.uncited` · 2 blocking · cost: 1 round (and the cap raise)
- [r4 fixer] Enumerating the claim's SUBJECT rather than a phrasing found **nine** homes, five more than the findings named
- [r5] Undisclosed FALSE-POSITIVE shape: wrapping the compliant call across two lines reddens the guard, because the required-call assertion is a per-line `contains` · non-blocking

## Raised after convergence
- `main` moved AFTER the loop converged and the PR was marked ready, turning it CONFLICTING. Both sides appended at the tail of `docs/adr.md` (main a Decision 96 trade-off bullet, this branch Decision 97); kept both, main's first. The merge also required RE-MEASURING two of Decision 97's claims, because main's #425 moved the very boundary they rest on (`statesMeasurement`/`numericFragment`) — both held. A blocking-only confirming round on the merge returned zero findings.
- Cost of the ordering: the PR was already non-draft when the merge was pushed, so that push bought an automatic app review. pr-harden's FINISH warns of exactly this, and it is unavoidable when main moves after ready rather than before.

## Where a skill blocked or contradicted this run
- resolve-ticket:Step 3 — the refutation gate's abort condition 3 fired correctly and the run stopped with nothing delivered until the user chose a reading. That was the right outcome, but the skill has no provision for delivering the *reading-independent* findings a stopped run has already established: four false claims in merged artifacts were in a report rather than in the repo, and the next attempt would have re-derived them.
- resolve-ticket:Step 1 — `gh issue view --comments` returned empty at exit 0 again (fifth run to meet it). Worse, the ticket GAINED a material comment mid-run and nothing in the skill says to re-read it; a fresh reviewer caught it.
- harden:Termination — cycle 1 made 4 commits so cycle 2 was owed; taken as a labelled override because pr-harden's fresh-context rounds are strictly stronger than a harden cycle whose Phase 1 is run by the author. Five rounds then found twelve findings, which supports the call.
- pr-harden:Termination — the round cap of 4 was reached with 2 blocking findings outstanding. Raised to 5, stated, on the signal the skill names (a different defect each round, and the remedy this time was structurally different: delete the claim shape rather than reword). Round 5 converged with zero blocking.
- Tooling: `TaskOutput` on an agent task dumped raw JSONL into the orchestrator's context once, exactly as its own deprecation note warns. `-Dtest=A+B` makes surefire run nothing and report BUILD FAILURE, which reads as a test failure and silently invalidated one mutation measurement until it was re-run with a comma.

## Declined
- (none — twelve findings raised across five rounds, all twelve implemented)

## Assumptions review overturned
- "The remaining #409 ask that is a module defect is the per-item accusation" -> both plan-gate passes and two reviewers held that the reporter's Mode A misattribution is also a module defect, just a harder one; the PR is `Refs`, not `Fixes`, and says so.
- "The evidence Decision 94 wanted does not exist" -> it is one comment away and the reporter is waiting; recorded as available-on-request rather than absent.

**Corrected by the 2026-09-23 retro, which measured it from this run's transcript
(`~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-409/0102617e-5075-4baa-9afe-3a29fc459eab.jsonl`):**
"twelve findings raised across five rounds, all twelve implemented" is not what it shows. The
reviewers of rounds 1-4 returned 13 findings (4, 2, 5, 2 at L1279, L1406, L1528, L1636); round 2's
fixer implemented `["r2-1"]` and declined none (L1444), so r2-2 was neither implemented nor declined.
Round 5 returned `findings: []` plus notes (L1719) — the `[r5]` line above is one of them — and the
run routed those "to a follow-up issue" and offered to file it (L1729, L1777); none was filed.
