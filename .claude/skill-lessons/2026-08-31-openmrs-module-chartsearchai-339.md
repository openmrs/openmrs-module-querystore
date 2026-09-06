# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #339 / PR #342 · 2026-08-31
outcome: converged
rounds: 13 (pr-harden) · cycles: 1 (harden, overridden) · verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-339/6ffad669-5d50-44af-b77a-4f2f1b49bd08.jsonl

## Refuted by measurement
- Refutation gate pass 1: "the existing-test churn is 125 live assertion lines across 41 test files, so the direction is wrong" -> a prototype measured 8 failing test METHODS in 5 classes, 7 of them the intended wording change. The gate's number was a grep of MATCHING lines, not of CHANGING ones. The skill's "check a count with the compiler before adopting the design it implies" is what saved the direction. · cost: 0 rounds (caught pre-code)
- Plan revision 2: "the gate is unchanged; only where the question is asked moves" -> false twice over. The row election changed WHAT the gate is asked about (round 3), and the ORDER rung gained a conjunct (round 3/4). Both were asserted in three homes each. · cost: 2 rounds
- Rounds 3-6 built a mechanism refusing a combination prescription's display; round 7 measured it printing 397 false ATC-class claims over 396 shipped-KB arrangements and naming one prescription FOUR ways in one response, and reverted it. The mechanism's purpose was a legibility problem; its consequence was false clinical claims. · cost: 3 rounds built + 1 to revert
- "A memo over the asks restores the per-pass sweep flatness" -> false: the asks themselves grow with the drugs in play (3 pairs at one drug, 7 at two). Only inverting the dataset once per pass works. · cost: 0 (caught by gate pass 2)
- ADR mutation figure "eight cases across as many classes" -> 40 across 8, and it did not reproduce when written. · cost: 1 round

## Raised by a fresh agent, missed by the author
- [r1] The row election was half-migrated: rule chips chart-anchored, class-only chips on canonicalRow · blocking · cost: 1
- [r2] partnerNaming correlated against orderPartners while SubjectRule.partner comes from findForActiveOrders (ATC u NAME) · blocking · cost: 1
- [r3] The election flipped the gate to refuse on route-qualified charted presentations, so a folded chip named one order twice; and the ORDER rung named a partner by a prescription containing its own subject · blocking x2 · cost: 1
- [r4] That conjunct depended on the CHIP's subject, so two chips about one prescription disagreed · blocking · cost: 1
- [r5] The refusal answered null, so a folded chip's two sentences disagreed · blocking · cost: 1
- [r6] classPartnerName never asked the refusal · blocking · cost: 1
- [r7] The refusal's step-back printed a constituent the cited ATC class does not classify · blocking · cost: 1
- [r12] Two rule chips about one fixed-dose combination rendered byte-identically (366 duplicates over 610 products, 0 at base) · blocking · cost: 1
- [r8-r11] Four rounds with NO behavioural defect, each finding one claim in the record: a stale ADR cross-reference, a mutation tally wrong by 5x, an exclusivity claim. Each fix removed the CLAIM FAMILY rather than the number. · blocking x5 · cost: 4

## Where a skill blocked or contradicted this run
- pr-harden:Termination — the default cap of 4 was raised nine times. Every raise met the stated signal (a different defect each round, findings shrinking) and every round found something real, including a behavioural defect at round 12 after four quiet rounds. But nine raises is not what "a round or two at a time" describes, and the run took ~16h. The skill gives no guidance for a change where each fix legitimately opens the next defect in the same area; "spinning" (re-raising) never triggered, so nothing ever said stop.
- pr-harden:Step 1 / the moving base — `main` merged three other PRs during the run (#341, #345, #344), costing three merges, two ADR renumbers and one round's blocking finding. The ADR-number collision the skill warns about fired twice, and both times the renumber sweep missed a site with a DIFFERENT spelling ("Decision 61" vs a bare "ADR 59"). Searching one spelling is not searching the claim.
- Session rate limit killed all four Phase 2 agents simultaneously at once (~3h dead). The skill's dead-agent contract (retry twice, change something) does not fit a limit that will refuse every retry for hours; what worked was a cheap capacity probe before re-spawning.
- harden:Termination — cycle 1 was overridden (Phase 2 round 3's four agents all died on the rate limit). Labelled at the time.
- `git checkout -- <path>` destroyed uncommitted work twice: once for the orchestrator (reverting a measurement mutation on a file carrying intended edits) and once for a fixer, which replayed its edits. The rule is in both skills; it still happened, because the destructive call looks identical to the safe one.

## Declined
- r4-2: a class-only chip and a rule chip can name one prescription by two different ROWS of its substance. If we ship without this, a patient whose prescription display carries the same route parenthetical the KB row uses gets `active order Atropine` beside `active order Atropine (ophthalmic)` in one chip list, which is a regression against the merge base on three of 1831 sweep arrangements — but closing it needs the demotion to be a property of the whole partner while its condition is per RULE, and a class-only chip has no token, so both available fixes reintroduce a worse defect (#187, or round 1's shape).

## Assumptions review overturned
- "The gate is untouched, so no new mis-attribution class exists" -> the gate moved twice and both movements were themselves defects (rounds 3-7).
- "Chips of different subjects naming one order two ways is closed as a consequence" -> refuted by an arrangement already in the suite, before any code was written (gate pass 2).
- "This is a naming change; the blast radius is the chip text" -> the change reached the injected prompt records, the withheld-pair WARN label, the screening arm's extent counts and the class arm's own sentence.
