# resolve-ticket + harden + pr-harden · openmrs-module-chartsearchai · #357 / PR 364 · 2026-09-02
outcome: converged
rounds: 2   cycles: 8   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-openmrs-module-chartsearchai/27f80050-dff5-427d-a119-88863a4525d6.jsonl

## Refuted by measurement
- "The plan is behaviour-compatible with the existing suite" -> the refutation gate APPLIED the plan and ran the suite: 1 of 1713 failed, `DrugReferenceInjectorTest.aSubFloorInteractionIsNotPromotedEvenWhenThePatientIsOnThatDrug`, whose `assertFalse(contains("warfarin"))` is the ticket's own defect pinned as spec. The gate found it before any code was written. · cost: 0 rounds (found at plan time)
- "Giving the chart-named partners the head of the tail is enough" -> letting that segment fall through to the tail's full-note loop rendered 16 identical copies of one sentence, 1530 chars, no mechanism prose, 9 of her own partners still withheld (shipped KB). · cost: 1 harden cycle
- "promotion means a full note" -> `render` has always fallen back to compact for a promoted note that will not fit; a neighbouring test pins it. · cost: 1 cycle
- "the overshoot is at most one note per segment" -> that segment has no budget check at all; 310 partners renders 7560 chars. · cost: 1 cycle
- "the honest clause is a fixed 76 characters" -> 77. · cost: 1 cycle
- "the order-driven leg was not reached at all" and then "the excerpt cannot yield a visible order-driven filtered segment" -> a confirming agent CONSTRUCTED the arrangement. Two refuted claims of one kind; the fix was to stop explaining and add the test. · cost: 2 cycles

## Raised by a fresh agent, missed by the author
- [harden c1] Segment 2 fell through to the tail's full-note budget loop — 16 identical sentences on a shipped-KB polypharmacy record · blocking-equivalent · cost: 1 cycle
- [harden c1] The dataset tail's one-representative slot was spent on a chart-named note, so the breadth guarantee was unreachable for exactly the patients it was written for — while the test that names itself as its guard stayed green · cost: 1 cycle
- [harden c3] Sorting the filtered segment by severity left the ENTIRE 1724-test suite green while the javadoc beside it said it was unsorted · cost: 1 cycle
- [harden c3] A javadoc orphaned by my own insertion into `DrugReferenceTestSupport` — the failure mode this session's memory warns about, and an earlier sweep in the same change missed it · cost: 1 cycle
- [harden c5,c8] The same exclusivity claim in a THIRD and then confirmed-final home; corrections reached one home, then two, then three · cost: 2 cycles
- [r1] The middle segment's "never invisible" guarantee asserted in three places and enforced by nothing: inserting a budget gate left all 1725 tests green · blocking · cost: 1 round
- [r1] ADR said "none withheld" where the record's own `withheldInteractions` is 355 — true of her partners, not of the named wire field · non-blocking · cost: 1 round
- [r1] ADR Decision 51's proof cited a predicate this change deleted and a `render` shape that is gone · non-blocking · cost: 1 round

## Where a skill blocked or contradicted this run
- pr-harden:Step 1 base-drift check — earned its keep on the first use: #356 merged to `main` mid-run and had taken ADR Decision 65, the number this branch allocated. Caught before round 2 spawned; renumbered to 66 across five homes (two in javadoc/test comments, not the ADR).
- Agent deaths on Opus session limits: 4 harden agents at once, then the round-1 fixer. The lever that worked both times was a cheaper model. The dead fixer left uncommitted partial edits with no report and no green build — discarded and re-run rather than kept.
- harden:Termination — `--count-edits` counts unpushed commits on a branch with no upstream, so cycle 8 read `edits=16` at convergence and only reached 0 after the push. The gate would have blocked a genuinely converged run.
- `git checkout -- <path>` after a mutation probe discarded my own uncommitted production edit (the segment-2/3 fix). Recovered from the PreToolUse hook's backup. The rule "commit before anything mutates the tree" is the one that matters, not the restore command.

## Declined
- [r1-4] The dataset tail's one-representative rule leaves the ticket's own reproduction with no mechanism prose and 1327 of 1500 budget chars unused — "if we ship without this, a future maintainer re-derives the same fourth alternative without knowing it was considered, or re-measures from scratch instead of building on 173-vs-1470 chars and 4-vs-7 notes" — mitigated by recording the trade with those figures in ADR Decision 66's Alternatives rather than leaving it implied.

## Assumptions review overturned
- "The floor's remit is the chips, so ordering the record changes nothing the #84 probe measured" -> the probe measured POSITION, and position is exactly what this change does not preserve. The containment is over what promotion buys; the position half is unmeasured and now says so. (harden cycle 2)
- "No production change exists only to make this testable" -> held, but the reverse bit: three production comments claimed properties nothing pinned, and each took a cycle or a round to close.
