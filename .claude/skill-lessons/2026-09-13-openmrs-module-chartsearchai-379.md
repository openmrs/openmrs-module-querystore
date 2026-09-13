# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #379 / PR 414 · 2026-09-13
outcome: converged
rounds: 2 (pr-harden)   cycles: 9 (harden)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced · one session rate-limit interruption between harden cycle 8's pass and its retry
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-379/eb836fab-3ed1-4601-b5cf-578d06fadab1.jsonl

## Refuted by measurement
- "Coverage, not uptake, is the binding constraint — the numbers reach too few attributions" (plan v1's whole premise, supported by one live cell) -> ran the discriminator the refutation gate named, over the 14-cell corpus on the branch point: no cell is fully covered, the spread is narrow (2/5 to 4/8), and the one cell that took the numbers is at the BOTTOM of it. Coverage does not rank the cells. · cost: plan v1 withdrawn before any code
- "Every existing case in InteractionFindingChartOrderBridgeTest stays green under plan v1" -> false; that class sets citeOrderRecords=true for every case and two of them ARE the #347 silence spec. · cost: 0 (gate pass 1)
- "The veto is what forces the uncollapsed index" (my own field javadoc) -> false; the veto composes with the old last-wins map plus its contested set unchanged. recordsOfActiveOrders is the only reading a count cannot answer. · cost: 1 cycle
- "numberByUuid answering with the LAST of two is what this method is for" -> its VALUE is read by nobody; returning a constant leaves the whole api suite green. The method was retired. · cost: 2 cycles
- "ActiveOrderReconciliationTest's new case pins #118's fail-open leg" -> its first version could not fail: both records named the drug, so the name leg answered anyway under the very mutation it was written for. · cost: caught by my own mutation check in the same cycle

## Raised by a fresh agent, missed by the author
- [harden c1] numberByUuid's value is unobservable while its javadoc asserted it load-bearing · non-blocking · cost: 1 cycle
- [harden c2] citableNumberFor's uuid leg and recordsSeveralOrdersName's skip must be ONE predicate — narrowing either alone cites one record as two prescriptions in one clause, measured · substantive · cost: 1 cycle
- [harden c2] the strike test was pinned against a per-order LAST pick but not a FIRST one, the named record sitting at index 0 · substantive · cost: 1 cycle
- [harden c4] FindingChartRecordProvenanceContextTest's "the uuid index is LAST-wins by construction" — an unchanged neighbour the change falsified · cost: 1 cycle
- [harden c5] the same claim surviving in a SECOND home, in the other test file the same CLAUDE.md bullet names · cost: 1 cycle
- [harden c6] three enumerations counting citableNumberFor's refusals, all stale because the branch added one · cost: 1 cycle
- [harden c7] "is not a fourth case" in two homes, one of them the same ADR paragraph as the new parenthesis announcing a fourth · cost: 1 cycle
- [harden c8] the readings count surviving in the class javadoc four lines from the field, after I fixed only the ADR home · cost: 1 cycle
- [pr r1] Decision 80 restated the probe's head as "against this head" with no antecedent; both arms ran the PRE-fix rendering on main @ 6ff1a60e · non-blocking, applied · cost: 0 rounds (applied at FINISH, one blocking-only round owed anyway)
- [pr r1] numbersFor's uuid leg hands the index's own List out of the class where the name leg returns a fresh one · non-blocking, declined to follow-up #415

## Where a skill blocked or contradicted this run
- resolve-ticket Step 3 — the gate's "revise and re-run ONCE" is written for a revision of the same plan; plan v1 was withdrawn entirely and replaced, and a second gate pass on a substantially different plan is arguably a third pass. Read it as allowed; worth stating which the rule means.
- harden Termination — nine cycles, seven of which found only prose defects of progressively narrower classes (stale claim -> stale count -> stale ordinal -> positional pointer). The rule worked exactly as written and each cycle found something real, but the pattern says the cheaper move is to sweep a whole CLASS tree-wide the moment one instance is found, which is what cycles 7 and 8 finally did in Phase 1.
- pr-harden Step 1 — GitHub's `pull/<n>/head` ref LAGGED behind the pushed branch by one commit; `gh pr view --json headRefOid` and `git ls-remote` both had the new sha. Briefing the reviewer off the pull ref would have reviewed the wrong sha. The skill's "the push had not landed when the fetch ran" guard names the symptom; the remedy is to cross-check headRefOid.

## Declined
- numbersFor returns the stored List rather than an unmodifiable view — if we ship without this, nothing breaks today because its single caller reads isEmpty() and discards the result; the latent mode is that a future second caller mutating that list flips numberOfRecord's size()!=1 reading and turns both affirmative refusals into a confident wrong citation, which no case would catch. Filed as #415 rather than taken on a head already reviewed clean.

## Assumptions review overturned
- "The deliverable is the coverage half of #379" -> withdrawn at the refutation gate; the deliverable became the deterministic mis-citation ADR Decision 80 had recorded as owed and deferred conditionally on a measurement that has since been run.
- "This change is dormant on a stock install" -> narrowed: the OUTPUT is dormant, the CODE is not — the DrugOrderRecords constructor and both ungated readings run on every request, and their behaviour-identity had to be proved rather than assumed.
