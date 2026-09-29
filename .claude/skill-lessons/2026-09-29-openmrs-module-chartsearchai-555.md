# resolve-ticket · openmrs-module-chartsearchai · #555 / PR #556 · 2026-09-29
outcome: converged
rounds: 4   cycles: 3 (harden)   verifier: ran (works at runtime, r1 @8e9bc158 and r3 @1e7c80f6; javadoc-only r2 proved bytecode-equivalent by javap)
context: no compaction · peak not surfaced
transcript: /Users/danielkayiwa/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-555/0709ed6d-671a-4ba7-b344-8fbe6bd7400f.jsonl

## Refuted by measurement
- plan v1 "strip the label's trailing parenthetical (DrugReference.displayStem) and credit the stem" -> refuter gate 2 cited reference CLAUDE.md "Matching a drug name" (matchesText is the prose accessor; PR #478 already replaced a label-substring test) · cost: 1 gate re-run, plan redesigned to carry partner rows structurally
- plan v1 subject/ambiguity guard -> probing the real injector showed rule chips name one partner per substance and #477 findings carry no rows; guard unreachable, dropped · cost: 0
- my own javadoc/README claim "a combination order is stated by its constituent's name" (harden cycle 2) -> a real-pipeline test showed the order still appended; claim and test deleted · cost: part of a cycle
- first merged-finding test used the shared-mechanism fixture -> its partners already print plain names, so it could not exercise the merge; replaced by an ivosidenib arrangement found by probing the shipped KB · cost: 0

## Raised by a fresh agent, missed by the author
- [gate1] crediting by stem silently credits #477 findings via the question drug's name · blocking · cost: 1 gate re-run
- [harden p2] README findingPartners paragraph and ChartSearchService.FindingPartnerCoverage canonical javadoc made false by the change · substantive · cost: 2 harden cycles
- [harden p2] collapseSharedMechanisms kept first member's rows, docs said union · non-blocking · cost: 0
- [r1] condition-mediated findings print partners by the parenthetical label but carried no rows · blocking · cost: 1 round
- [r2] comparable() javadoc claimed ICPFC and this check read names alike · non-blocking · cost: 1 blocking-only round
- [r3] matchesText credits any alias, incl. analyte synonyms ("lactate" for Lactic acid) — silent direction · blocking · cost: 1 round
- [r4 note] cross-finding union in statedPartners unpinned; rule chip + #477 finding naming one display lets the substance name state the #477 copy · non-blocking · recorded in PR body

## Where a skill blocked or contradicted this run
- harden: a Phase 2 that finds a now-false doc escalates; ran Phase 2 three times (4+4+2 agents) for doc-only findings — expensive but each found a real false sentence
- pr-harden FINISH: pr-* ref cleanup — refs pr-559-r* from a co-tenant run are visible in the shared ref namespace; left alone

## Declined
- (harden p2) pass partner row group out of chartOrderBridges instead of calling rowsOfSubstance beside it — if chartOrderBridges later changes how it picks the partner's rows, #555's rows keep the old rule; accepted because both call the one function on identical arguments
- (harden p2) shared rows-by-id helper across namesThePartner/namesTheFindingsSubject; unifying OverShippedData with InteractionClaimPairFidelityTest.Arrangement — duplication only; a later fix to one copy may miss the other

## Assumptions review overturned
- "Only an interaction RULE chip's partner carries rows; condition-mediated orders erring toward reporting" -> false: they print the label too, so it was #555's defect; rows added in round 1
- "prose rule over every alias is the right credit" -> narrowed to a row's own name in round 3
