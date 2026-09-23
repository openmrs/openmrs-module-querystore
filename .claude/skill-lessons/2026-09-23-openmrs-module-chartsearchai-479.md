# resolve-ticket · openmrs-module-chartsearchai · #479 / PR #486 · 2026-09-23
outcome: converged
rounds: 1   cycles: 1   verifier: skipped (test + fixture + ADR only; no runtime-visible change) — a live A/B on the slot-1 standalone was run as the ticket's own deliverable
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-479/b3b01f8a-fa73-4881-9298-423b5b0a2064.jsonl

## Refuted by measurement
- plan: "removing the fold leg is consistent with Decisions 86/109" (expected removal) -> pre-registered A/B criterion failed: the ketoconazole cell dropped its rating (ratings dropped 0 -> 1), and 3 of 4 flipped answers omit the class relationship (main omits it on the same cells). Leg kept; ticket closed as a measured decision · cost: 0 rounds
- my own test comment "without the warfarin order the model is asked for want of any finding" -> the mutation run showed one finding survives · cost: 1 harden pass

## Raised by a fresh agent, missed by the author
- [gate] M1 was not calibrated against the recorded 108/24,690, and the decision rule gave M1 no role and could pass vacuously (no required flip, no folded-Minor cell) · blocking at the gate · cost: 0 rounds
- [harden P2] the ADR's row counts are "as loaded" (one per orientation, ~2x the file's rows) and never said so · non-blocking
- [r1] Refs should be Fixes: all three items were delivered, including the requested decision · non-blocking, adopted at FINISH
- [r1] the arm figures depend on the context shape (chart ATC set without an ActiveDrugOrder gives 3,015, an ActiveDrugOrder gives 3,018) · non-blocking -> #488
- [r1] SlicedReferenceRowProvenanceTest.SLICES already guards the "field-for-field" claim; the fixture could join it · non-blocking -> #488

## Where a skill blocked or contradicted this run
- resolve-ticket Step 3: the refuter ran while I ran M1 in a separate scratch worktree, to avoid mutating the refuter's tree; that worked and cost nothing
- pr-harden-gate: the Stop hook fired on every yield while background captures ran; I waited in-turn with bounded perl-sleep loops (a foreground `sleep` is refused)
- harden Phase 2 ran as 2 agents covering the 4 lenses rather than 4 agents; this deviation was stated in the report

## Declined
- none

## Assumptions review overturned
- A (plan): "if the leg is kept, the PR says Refs #479" -> replaced by Fixes #479 at FINISH, on r1-1: the ticket asked for a decision on evidence, and the decision was made
