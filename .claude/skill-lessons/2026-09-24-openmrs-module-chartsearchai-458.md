# resolve-ticket · openmrs-module-chartsearchai · #458 / PR #520 · 2026-09-24
outcome: converged
rounds: 2   cycles: 1 harden run (Phase 1 re-converged twice after Phase 2 escalations)   verifier: skipped (test-only diff, nothing runtime-visible)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-458/4cec7b02-83d1-4a6a-862e-592480fa6ea3.jsonl

## Refuted by measurement
- Ticket's reproducer (needle -> "interactionRules" passes 4/4) -> re-measured pre-change on 5c5b1c2c: 4/4 green; post-change: red at the control · cost: 0

## Raised by a fresh agent, missed by the author
- [harden P2 #1] method javadoc "this case passes against the pre-change code" became false (the new control requires AboveFloorRules, absent pre-#447) · substantive · cost: one Phase 1 re-convergence
- [harden P2 #1] failure message blamed/ told to swap the needle when the join's read moved to a helper (misdirects; swapping narrows the guard) · polish
- [harden P2 #2] SourceScan class javadoc "Every lookup fails LOUDLY" false for literalOffsets/matches/names — the root claim that let #458's guard go vacuous; second home in StandingChartAlertsTest · substantive · cost: one Phase 1 re-convergence
- [harden P2 #3] StandingChartAlertsTest forbid loop has the same vacuous shape (mutation-proved) · non-blocking, out of scope, named in PR body
- [r1] alias residue: second DrugReference accessor walked by indexed get escapes arm loop AND walk counts (CountingRules counts iterator()) · non-blocking · implemented as javadoc residue in r1 fix

## Where a skill blocked or contradicted this run
- harden: Phase 2 ran three times on a ~15-line test diff because each pass surfaced one substantive prose falsehood; the rule worked as written (each was real) but the 4-agent fan-out per pass is heavy for this size.
- resolve-ticket: the ticket asked for a test-only change, so the "failing test first" step was a mutation (needle rename) run on pre-change code rather than a new failing case.

## Declined
- none

## Assumptions review overturned
- none (assumption: take the ticket's "stronger" option — anchoring the needle in AboveFloorRules.of — survived gate and both rounds)
