# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #515 / PR #540 · 2026-09-25
outcome: converged
rounds: 4   cycles: 3 (harden)   verifier: ran (works at runtime, b0e49d03)
context: no compaction · peak not surfaced
transcript: /Users/danielkayiwa/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-515/1d4126e7-e46f-4406-a584-25ab451be1d8.jsonl

## Refuted by measurement
- Plan: a separate public validator entry point for the listed-drug statement -> refuted at gate pass 1 (ChipSubjectOneResolutionTest forbids a second SubstanceSubjects; a third order resolution) · cost: 0 (plan time)
- Plan: match a finding's subject by resourceKey label == displayLabel -> refuted at gate pass 2 (second resolution; blind to multi-substance labels) · cost: 0
- Assumption: adding a CLAUDE.md bullet -> ProjectInstructionsGuardTest size budget: reference file 2 bytes under, root 23; bullet dropped · cost: one build
- Test assumed ChartAnswer keeps the list by identity (assertSame) -> it copies; changed to equality before it ever passed · cost: 0

## Raised by a fresh agent, missed by the author
- [harden P2] order-driven contraindications carry subject rows though five docs said none · substantive
- [harden P2] listed sentence missing from early done under async grounding · substantive
- [harden P2] listed statement had no own try; a throw would drop every pre-answer finding · substantive
- [harden P2] Java lead cut at the first sentence disagreed with caution_led (line break, "approx.") · substantive
- [r1] #513 item 2 in owner's scope, not deliverable under the mandated anchor; PR must not silently close it · blocking · 1 round
- [r1] findingWithholds untested outside rated interactions · non-blocking
- [r1] question-pair Major missed depending on which drug is the pair subject · non-blocking
- [r2] PR body "does not close #513" made GitHub link #513 as closed (negation ignored) · blocking · 1 round
- [r2] CHANGE/ENDED withholding legs untested; README misattributed pair clause · non-blocking
- [r3] duplicate-therapy (alreadyInSeveralOrders) finding bypassed EndedOrders.stamp -> no subject rows -> never reported · blocking · 1 round

## Where a skill blocked or contradicted this run
- harden:Phase 2 — ran each pass as ONE agent covering four lenses rather than four parallel agents (deviation, stated); still escalated twice
- resolve-ticket:Step 8 — "Fixes" with a negated "does not close #N" sentence elsewhere links #N: closingIssuesReferences check caught it only via r2 reviewer

## Declined
- Screening-arm pair findings carry no subject rows — if we ship without this, a caution lead on a screen question reads [], but that arm runs only when the question names no drug, so no lead proposal exists
- Per-finding getAll() sweep — only on caution lead + withholding finding; cost negligible
- CLAUDE.md bullet — instruction files at byte budget; ArchitectureGuardTest confines writers

## Assumptions review overturned
- "#513 item 2 absorbed by check (1)" (owner premise) -> recorded as undeliverable; #513 stays open (r1)

## Driver capture (pool-run)
outcome as the driver measured it: ready
session: 1d4126e7-e46f-4406-a584-25ab451be1d8 · 2h42m · 908 assistant turns · stream: /Users/danielkayiwa/.claude/pipeline/logs/20260924T233216Z-openmrs_openmrs-module-chartsearchai-515.jsonl
- this run started on a checkout whose gate state a previous run left behind — pr-harden-state.json: phase=building blocking=0 pr=None round=1
