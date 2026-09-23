# resolve-ticket (+ harden, pr-harden) · openmrs-module-chartsearchai · #489 / PR #492 · 2026-09-23
outcome: converged
rounds: 1   cycles: 2 (harden: Phase 2 escalated once)   verifier: skipped (standalone cannot drive the model's exact wording; substitute = LlmInferenceServiceEndedOrderStatementContextTest through real search/searchStreaming on the merged head)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-489/c3aa9948-83cf-48c0-af17-7028ece83e02.jsonl

## Refuted by measurement
- plan's tie test "an ended-clarithromycin chip on the shared kit alias" -> no ended clarithromycin chip arises (aspirin x clarithromycin Unknown); refuter measured an ended Omeprazole chip does · cost: 0 (caught at gate)
- orchestrator's own residue probe first read "appended=true" on the known-good control -> Decision 100's partner-completion sentence was appended, not the ended-order one; re-asserting on the STATEMENT text fixed it · cost: 0 (caught by the control)

## Raised by a fresh agent, missed by the author
- [gate] said-twice residue "this drug listed after another after the phrase" missing from plan; nearestBeforeIsOwn javadoc/name stale; own-absent-after branch unspecified · non-blocking · cost: 0
- [harden P2 quality] residue list incomplete: where a drug IS named before the phrase, the phrase-ahead shape still misreads in both directions; removing the old entry made the toward-silence list read complete · substantive (escalated Phase 1) · cost: 1 harden cycle
- [r1] `mine == null` half of the new guard unpinned (`if (other == null)` stays green) · non-blocking · filed #494
- [r1] walk step `return true` short-circuit unpinned (silence direction) · non-blocking · filed #494

## Where a skill blocked or contradicted this run
- pr-harden-gate / resolve-ticket Step 3 — the turn ended once while waiting on the build (Stop hook fired "building"); recovered by an in-turn until-loop. Cost: one turn.
- pr-harden FINISH — main moved by one commit touching docs/adr.md after round 1; no skill rule says whether a clean, non-required merge must be pushed. Took: build the merge in a throwaway worktree (green), do not push, keep the reviewed sha.

## Declined
- none

## Assumptions review overturned
- none
