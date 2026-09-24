# resolve-ticket (+ harden, pr-harden) · openmrs-module-chartsearchai · #433 / PR #511 · 2026-09-24
outcome: converged
rounds: 2   cycles: 3 (harden: two Phase 2 escalations)   verifier: ran (works at runtime, no repairs)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-433/dbfc9042-552f-4c7d-aed9-be5ea137bfe1.jsonl

## Refuted by measurement
- (author, harden P1) "no rule can join two rows of one substance" in the inline gate comment -> true of DDInter only; JsonDrugReferenceSource binds substanceName so a curated file can author one · cost: 0 (caught pre-PR)
- (author, harden P1) ADR "every row of a substance is named by one token" -> isSelfPair's DDInter claim, not true of curated data · cost: 0
- nested reference/CLAUDE.md directive added -> ProjectInstructionsGuardTest size budget red; dropped, ADR carries it · cost: one build

## Raised by a fresh agent, missed by the author
- [harden P2a] pairKeyNames javadoc "reachable wherever one word resolves several entries" false after the change · substantive · cost: 1 escalation
- [harden P2b] the narrowed replacement ("…of one substance beside a second drug") false in the other direction (no-substanceName sources reach it from one word) · substantive · cost: 1 escalation; resolved by deleting the claim shape (mechanism only)
- [harden P2a] ADR placement bullet gave equivalence, not a reason; cited the wrong test for "still states its zero" · non-blocking
- [harden P2a] missed neighbour comment (PairChipExtentContextTest ~385), README/PairChipExtent null list "resolved one" · non-blocking
- [r1] two more homes of the gate stated as ">= 2 entries" (validate comment ~1073, examples doc table + prose) · non-blocking · cost: 1 blocking-only round
- [r2 note] all-vs-any gate boundary pinned only by tests outside the PR's files · note, unimplemented
- [r2 note] pairKeyNames' "33 above-floor rows" KB figure not re-measured for the narrowed population · note, unimplemented

## Where a skill blocked or contradicted this run
- harden:Phase 2 — launched 4 lenses as background agents in an unattended run; ended the turn twice with them outstanding and the Stop hooks fired both times. Fix used: a foreground bounded wait, then foreground Agent calls in one message for later waves. Cost: two wasted turns, no lost work.
- resolve-ticket:Step 3 — the refuter's non-blocking doc-sweep list was incomplete itself; the rest surfaced across harden P2 and r1.

## Declined
- [harden P2a reuse] pass validate's questionSubstances into the arm instead of oneSubstance — if we ship without it, the two sets drift only if one of two adjacent derivations of the same substanceGroupKey set is re-keyed; taking it couples a private arm to an unchecked caller-derived parameter.
- [harden P2c quality] "reachable only where the question also resolves a second substance" — false for sources publishing no substanceName (DrugSafetyQuestionPairInteractionTest.aQuestionNamingOneDrugRaisesNoPairChipForItsOwnRouteVariant).
- [harden P2a quality] add a curated-fixture test pinning entry-gate vs empty-list placement — recorded as a named residue in ADR 115 instead; if we ship without it, a later move of the check into the empty-list branch goes unnoticed and restores the curated self-pair chip.

## Assumptions review overturned
- none. Placement at the entry gate (the ticket's reading) and accepting the curated-source trade stood through both rounds.
