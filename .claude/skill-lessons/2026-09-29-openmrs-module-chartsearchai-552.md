# resolve-ticket · openmrs-module-chartsearchai · #552 / PR #557 · 2026-09-29
outcome: converged
rounds: 3   cycles: 3 (harden)   verifier: ran (works at runtime, b30fca1e; head 98c7a919 production classes byte-identical)
context: no compaction · peak not surfaced
transcript: /Users/danielkayiwa/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-552/6acf7946-8a62-4499-b4b1-a3f64dae5af5.jsonl

## Refuted by measurement
- "a mutation of the stamp's KEY in ContraindicationChips.add is pinned by adding a second substance" -> it reddened nothing: the arm records a substance's orders immediately before adding that substance's chips, so a wrong key reads the same list; the no-order cases are also unreachable by key/gate mutations because the in-play arm adds chips before any record (probe printed recorded=[]) · cost: 0 rounds, two harden probes
- plan A2 "keep displayNamesADrug on the wire" -> refuted by the refutation gate's second pass citing reference/CLAUDE.md's own "ask before DISPLACING, never before labelling" rule · cost: one gate re-run

## Raised by a fresh agent, missed by the author
- [gate] reference/CLAUDE.md had 6 bytes of budget left; the planned instruction bullet would fail ProjectInstructionsGuardTest · blocking (gate) · cost: 1 gate re-run
- [gate] codes-only orders must be listed on the wire (A2) · blocking (gate) · cost: 0 rounds
- [harden P2] namedPartners javadoc/README/controller comment claimed contraindications "name no active order" · substantive · cost: 1 harden cycle
- [harden P2] the proposal-[] test could not fail (chart had no orders) · substantive · cost: 1 harden cycle
- [harden P2] javadoc orphaned by inserted constants in ChartSearchAiChartAlertsTest (the memory's known pattern, 6th time) · polish
- [r1] drug-in-play contraindications marked aboutACurrentMedication (#402) carried [] where the owner's "every current-medication contraindication chip" covered them; the author had recorded it as a residue (assumption A4) · blocking · cost: 1 round
- [r1] the rule belongs in reference/CLAUDE.md, paid for by trimming within budget · non-blocking · implemented
- [r2] the current-medication gate in add became load-bearing after r1 and was unpinned · non-blocking · implemented
- [r2] one identity chip lists different orders by arm (Nexium on /chartalerts, not on a proposal); ADR 125 overclaimed · non-blocking · documented

## Where a skill blocked or contradicted this run
- harden:Phase 2 — escalated twice (substantive doc universal; a test that could not fail), so Phase 2 ran three times; each escalation was a real finding
- resolve-ticket:Step 3 — the gate's first blocking objection (budget) was settled by deletion; the re-run raised a second, also settled (outcome 2)

## Declined
- (none)

## Assumptions review overturned
- A4 "scope is addActiveOrderContraindications only; drug-in-play current-med chips carry [] as a residue" -> both arms stamp; drug-in-play lists ordersEstablishing, wire-only (round 1)
- A2 "population unchanged (displayNamesADrug)" -> every resolved order on the wire; filter kept only on the sentence projection (refutation gate)
- "no reference/CLAUDE.md bullet because of the budget" -> bullet added, paid for by two redundant preamble sentences (round 1)
