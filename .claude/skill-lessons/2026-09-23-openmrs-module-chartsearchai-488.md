# resolve-ticket · openmrs-module-chartsearchai · #488 / PR #490 · 2026-09-23
outcome: converged
rounds: 1   cycles: 1   verifier: skipped (tests + ADR prose only; no runtime-visible change)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-488/d52a1da1-2a19-4953-8f02-ef6fc6693e4a.jsonl

## Refuted by measurement
- ticket item 2: "Review reproduced those figures exactly, but only with ... no ActiveDrugOrder" -> a context WITH an ActiveDrugOrder carrying the partner's codes gives identical 101 / 3,015 (1,561 pairs); #479's own harness (recovered from its transcript) built exactly that order · cost: 0
- my plan: "the recorded figures came from S1" -> the gate: S1 and S2 are equal, so the data shows only that S1 ALSO reproduces them; the ADR names both shapes neutrally · cost: 0

## Raised by a fresh agent, missed by the author
- [gate] every new figure is counted over the hand-written level-2 prefilter; state the calibration beside each · non-blocking
- [gate] the 3,005/10 split: why the 10 fold was not investigated, and the ADR had not said so · non-blocking (adopted in harden Phase 2)
- [harden P2] "Calibration for that second figure" lost its antecedent after the insertion; 3,017 was omitted between the two figures given · polish
- [r1] the merged-head reproduction (after #483) was not recorded in the ADR · non-blocking -> #491

## Where a skill blocked or contradicted this run
- gate-state: `await` without `--only` needs `--run`; `--only pr` must go after the subcommand, and my first two tries failed (usage errors, no state damage)
- harden Phase 2: ran 2 agents covering the 4 lenses, not 4 (3-file diff); deviation stated
- resolve-ticket Step 1: `gh issue view` exited 0 with empty output again; `gh api` fallback worked

## Declined
- none

## Assumptions review overturned
- none (A1: item 1, the live prompt experiment, left out under `Refs #488`; round 1 judged that honest)
