# resolve-ticket + harden + pr-harden · openmrs-module-chartsearchai · #379 / PR 386 · 2026-09-08
outcome: converged
rounds: 3 (pr-harden)   cycles: 4 (harden)   verifier: ran (works at runtime, on the merging head 66e31981)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-379/543b1087-f05b-4d65-a792-289971b80038.jsonl

## Refuted by measurement
- "the ticket asks for the record-number rendering to be built" -> already merged as c0d6a4bf, gated off; the maintainer's two comments had RUN the owed measurement and it fails. The live deliverable was the first of the three gates those comments name. · cost: 0 (found at Step 1, reading comments)
- Plan v1: "uncited is scoped to the claim's SENTENCE, failing toward silence" -> gate pass 2 cited ADR Decision 76's own measured-and-rejected sentence unit and showed the refutation transfers; the RUN unit (plan v0) was right on new grounds. · cost: 1 gate pass
- "the maintainer's answer states five active-order claims" -> `grep -o ' interacts with active order '` over the verbatim answer gives FOUR against five orders named; the last sentence's second order carries no "interacts with". · cost: 0 (caught at the gate)
- "activeOrderClaims is the base misattributedOrderCitations is a share of" -> FALSE: one claim can offer several misattributed citations. Refuted by a Phase 2 agent driving the real pipeline (5 claims, 3 misattributed citations, 0 uncited), and contradicted by a sentence twenty lines away in the same file. Took 3 cycles to clear all 7 homes. · cost: 3 cycles
- "#377's byte-identical measurement of removing the next-occurrence scan bound" -> true of the ACCUSATION only; substituting it moves an uncited claim to cited with the whole suite green. · cost: 1 harden cycle + 1 pr-harden round
- "the measurement the ticket names has not been run" -> stale in 4 homes (README, config.xml's operator-facing description, the GP constant's javadoc, Decision 77) after README alone was corrected. config.xml was missed because the sweep was doc-scoped. · cost: 2 cycles
- "metric_score.py accepts that hazard only because its key has no Java spelling to drift from" -> false; `attachedByTheModule` is written as a literal at ChartSearchAiRestController:1214. · cost: 1 cycle

## Raised by a fresh agent, missed by the author
- [harden P2r1] `offersChartEvidence` and `refusal` were each other's complement over `referenceGroup` — one spelling of a two-valued classification written two ways, green until a third group is minted · non-blocking · cost: 0
- [harden P2r1] the class javadoc restated three of `ActiveOrderClaims`' four canonical sections while declaring that type canonical · non-blocking · cost: 0
- [harden P2r2] the new instruction-file directive told a maintainer to compare against `REFERENCE_GROUP_REFERENCE` — the exact form the code's own javadoc calls the hazard · non-blocking · cost: 0
- [harden P2r3] the pre-existing "ONE conservatism … removing this one was measured byte-identical" bullet was falsified BY this change: the two halves fail safe in opposite directions · non-blocking · cost: 0
- [pr-harden r1] every wire fixture was `ActiveOrderClaims(n, n)`, so transposing the two `map.put` arms in `serializeActiveOrderClaims` shipped GREEN — the two published numbers rested on review alone. Proved by running the transposition against a full root build. · BLOCKING · cost: 1 round
- [pr-harden r1] the next-occurrence scan bound became load-bearing for the claim count and nothing pinned it · non-blocking · cost: 0
- [pr-harden r2] Decision 81's "one mutation, both cases" was stale in the round that wrote it — round 1 added a third case the same mutation reddens · non-blocking · cost: 0

## Where a skill blocked or contradicted this run
- harden:Phase 2 — four agents in isolated worktrees on a SHARED branch ref: the cycle-2 confirming agent committed on `fix/379-…`, moving the orchestrator's branch pointer under it and leaving its working copy holding the pre-fix text of two files. Recovered by `git checkout HEAD -- <paths>` after confirming nothing of the orchestrator's was in them. Later briefs added an explicit "do not commit". · cost: ~1 recovery step
- pr-harden:step 4 — the fixer ran in an isolated worktree and left its work UNCOMMITTED there, per the brief; the orchestrator had to `git -C <agent-worktree> diff | git apply`. The skill says "leave your edits in the working tree" without noting that an isolated worktree makes that a different tree. · cost: 1 step
- Both pass-4 harden agents died simultaneously on a session rate limit (429). Cleared the awaits and retried once, combined into one agent, after the reset. · cost: ~1 wait
- resolve-ticket:Step 2 / ProjectInstructionsGuardTest — the root CLAUDE.md had 15 bytes of headroom, so the rule this change owes could not go there. Followed Decision 77's own precedent and put the directive in `reference/CLAUDE.md` (894 bytes spare), recording the reason in Decision 81. · cost: 0

## Declined
- (none — nothing was declined in any pr-harden round, and no harden finding was deferred without being applied)

## Assumptions review overturned
- "the presence half can share the accusation's clause-bounded run and stay conservative" -> the conservatism INVERTS (the accusation fails silent, the count fails loud); kept the shared unit and recorded the opposite residue, pinned by `aClaimWhoseOnlyChartCitationSitsInTheNextClauseIsCountedUncited` (gate pass 2)
- "publishing a pair needs no asymmetric fixture because the accessors are pinned" -> the WIRE binding is a separate property and was pinned by nothing (pr-harden round 1)
