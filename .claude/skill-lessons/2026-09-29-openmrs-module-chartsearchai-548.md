# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #548 / PR #554 · 2026-09-29
outcome: did-not-converge (round cap of 4 reached; round 4's one blocker was applied to the PR description — Fixes→Refs — and no confirming round remained)
rounds: 4   cycles: 2 (harden: Phase 2 escalated once)   verifier: ran ×3 (works at runtime; each run first repaired a stale omod on :8081)
context: no compaction · peak not surfaced
transcript: /Users/danielkayiwa/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-548/eb581d71-6c71-4a2d-bc62-d43da63a64f5.jsonl

## Refuted by measurement
- Plan: "questionProposes admits the ticket's question" -> refuter compiled QueryScopeRouter: "Is it safe to add X for her?" fits no shape · cost: one plan revision (grammar widened)
- Plan: Voltaren gel fixture isolates the herOrder gate -> refuter drove validate: Voltaren's display names no diclofenac; used Diclofenac gel 1% · cost: 0
- Round 1 fix "one order is a caution clause" -> live gate: the add answer restated the caution clause and dropped "duplicate" (3/3 runs) · cost: 1 round
- Round 1 clause wording -> live gate: Barbara aspirin answer refused "adding more aspirin" · cost: 1 round
- Round 2 wording fixed the add row -> live gate: #402 residue (a) moved onto "Can I give her prednisone?" (rating no chip carries), unmet at cap · cost: 2 rounds

## Raised by a fresh agent, missed by the author
- [harden P2] clause said "that order" when two orders share one display (count re-derived from labels) · substantive · cost: 1 cycle
- [harden P2] orphaned severalFindingsAboutOneDrug javadoc (inserted helper between javadoc and method) · substantive · cost: 1 cycle
- [r1] one-order gate kept #477's display rule, so brand orders (Advil) got the flag but no chip/clause · blocking · 1 round
- [r1] one-order finding stated "reason to change her medication" against the current-med prompt branch · blocking · 1 round
- [r2] add cell never said "duplicate"; aspirin cell refused · blocking ×2 · 1 round
- [r3] rating row fails; ADR said the opposite · blocking · 1 round
- [r3] combination order: "adding it would duplicate that order" false for a constituent · non-blocking · fixed r3
- [r4] Fixes #548 would auto-close with a gate row unmet · blocking · applied to description at cap

## Where a skill blocked or contradicted this run
- ProjectInstructionsGuardTest budget: base file 6 bytes under cap; raised in its own commit per the guard's javadoc
- pr-harden:round cap — the last blocker lived in the PR description (moves no head), leaving no confirming round; ended on override

## Declined
- r1-3 probe scorer blind to the chip/lead — needs a wire key; a probe A/B over own-drug proposal cells cannot flag a surviving refusal
- r1-4 live gate unmeasured — verifier's job, run each round
- r2-4 two-or-more-order proposal keeps change class — caution would state a false strength; model may understate adding; unmeasured

## Assumptions review overturned
- A2 (clause wording = ticket's suggested wording) -> extended in r2 with "as calls about that medication and not about adding it", and r3 per-combination consequence
- "one-order finding keeps Decision 112's strength" -> caution-current in r1

## Driver capture (pool-run)
outcome as the driver measured it: draft
session: eb581d71-6c71-4a2d-bc62-d43da63a64f5 · 3h38m · 1072 assistant turns · stream: /Users/danielkayiwa/.claude/pipeline/logs/20260928T221801Z-openmrs_openmrs-module-chartsearchai-548.jsonl
- the run left its gate entry unfinished: phase=reviewed blocking=1 round=4 override=True
