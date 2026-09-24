# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #407 / PR #526 · 2026-09-24
outcome: converged
rounds: 3   cycles: 1   verifier: skipped (tests, comments and CLAUDE.md only — no runtime-visible change)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-407/1244e489-9b33-4380-8c35-9b7a7ef2ef8b.jsonl

## Refuted by measurement
- "all three causes reach the list through that one boolean, so one case pins the no-reading path" (plan, adopted from the refuter's own scope objection) -> true only of a regression keyed on reading.states(); a gate on reportsContraindications() or context != null left the reference package green · cost: 1 fixer pass (inside the round-1 non-blocking exception)

## Raised by a fresh agent, missed by the author
- [refute] precondition must assert all three reading leads absent, not two · non-blocking · cost: 0
- [harden p2] test comment "where every other line about the sections lives" false (the membership walk is outside the gate) · non-blocking · cost: 0
- [r1] toggle-off and null-context causes unpinned; per-conjunct gates stay green · non-blocking · cost: 1 fixer + 1 review round
- [r2] "nothing else in this class renders without one" made false by the r1 fix's sibling case (a seam between commits) · note in a blocking-only round; fixed at FINISH · cost: 1 blocking-only round

## Where a skill blocked or contradicted this run
- pr-harden:REVIEW — round-1 reviewer returned mid-flight ("waiting for notifications") with a mutation live in the tree and a build running; the brief said restore before reporting but not "collect builds in the same turn". Resumed via SendMessage; ~20 min of wall clock. Later briefs spelled out the in-turn wait and it did not recur.
- resolve-ticket:Step 3 — refuter recommended narrowing to ONE case (all causes through one boolean); round 1 then asked for the opposite. The refuter's scope objection and the reviewer's coverage finding pulled in opposite directions; the reviewer measured, the refuter reasoned.
- harden:Phase 2 — the quality lens's mutation probe overwrote the surefire report, so the fixer's "green" couldn't be spot-checked from the reports (failures="1" left on disk from a probe); a root re-build was needed.

## Declined
- (none; notes left unimplemented are in the PR body: no-reading ORDER unpinned; adjacent-only dedup unpinned; 2 bytes budget headroom; em-dash aside readability)

## Assumptions review overturned
- "one no-reading case suffices" -> one per cause of statesTheChartsContraindicationReading (round 1)
