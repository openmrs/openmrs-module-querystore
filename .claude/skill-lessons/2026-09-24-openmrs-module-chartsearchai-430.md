# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #430 / PR #532 · 2026-09-24
outcome: converged
rounds: 1   cycles: 2 (harden: Phase 2 escalated once)   verifier: skipped (tests + javadoc only; no runtime-visible path)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-430/b999b175-0e7f-4228-9b0b-9856884c1111.jsonl

## Refuted by measurement
- Plan's probe "reverse the writer's ceiling order and watch the two cases stay green, then redden" -> reversing reddens setUp's own premise in every test, so the probe cannot isolate the new asserts; a spelling sort (TreeSet) at the writer was the discriminating mutation · cost: 0 (refutation gate)
- My Phase-1 claim "the NBSP naked-decimal case still reaches numericFragment with the order reversed, so it needs no premise" -> false: with [5, 0.5] and the isSpace bug, position 0 reads as stated and the case stays green; measured by a levothyroxine-only reversal + isSpace mutation · cost: 1 harden cycle

## Raised by a fresh agent, missed by the author
- [gate] spelling-sort probe instead of order reversal · blocking · cost: 0
- [gate] aSeparatorAFTERALetter and aFullStopATTACHED share the missing-premise gap (ticket named only two) · non-blocking · cost: 0
- [harden P2 integration] NBSP case shares the gap too; the spelling sort cannot flip 0.5/5 so the author's probe was blind to it · substantive · cost: 1 cycle
- [harden P2 reuse] pointer sentence restated the check's javadoc argument instead of pointing · polish · cost: 0
- [harden P2 x3] 125-char unwrapped javadoc line · polish · cost: 0
- [harden P2 integration] pre-existing: "each unpinned" stale for "4000,2000"; EDGES fixture's "nothing else in the suite going red" exclusivity claim false · out of scope, named in PR body

## Where a skill blocked or contradicted this run
- none

## Declined
- none (round 1 raised no findings)

## Assumptions review overturned
- A2 "premise only in the two named tests" -> extended to all five EDGES separator cases (gate + harden)
