# resolve-ticket (+ harden, pr-harden) · openmrs-module-chartsearchai · #330 / PR #332 · 2026-08-30
outcome: converged
rounds: 3 (pr-harden)   cycles: 15 (harden)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-330/8bafa44a-fe0c-4236-b1ca-f8f3964be08c.jsonl

## Refuted by measurement
- The ticket's own scoping ("`token` is the alias, which genuinely varies") -> true within a scan, false across the dataset's life; the alias operand held 50.9-63.5 ms of a 67.8-84.4 ms saving, five sixths of the win the ticket's plan would have left behind · cost: caught at the refutation gate, 0 rounds
- The ticket's "~8 ms" and my plan's first instrumentation -> counters bucketed by the call site INSIDE matchesDrugName rather than by its caller, so `lookupByToken` + `findImpliedSubstances` (26% of comparisons at ten drugs) were invisible. Same shape of mistake the ticket was filed on, one level along · cost: gate pass 1, 0 rounds
- "the four-arm experiment leaves 50.9-63.5 ms of the 68-84 ms" -> arithmetic across two runs, published beside measured figures · cost: 1 cycle
- "It is 76-92 ms" -> a delta of a measurement run the Effect table had already retired, standing under that run's own supersession note · cost: 1 cycle
- "both measurements say it is more than ten times that" -> refuted by the 68 ms lower bound quoted in the same sentence · cost: 1 cycle
- "sweeps ... twice in a pass" -> measured 2/3/4 at one/two/three orders · cost: 1 cycle; then "once more for every further order carrying it" -> 1 and flat for dictionary-mapped orders · cost: 1 cycle; then "once per RESOLVING SITE, and there are two" -> states a count while the next sentence disclaims one · cost: 1 cycle. FOUR successive rules for one count, each measured false. Ended only by publishing the measurement with its arrangement and no rule.
- "main 88.9 -> 12.6 ms" -> main's OWN absolute moved to 107.7 for identical code between two sessions on one box; and the RATIO moves with the arrangement too (70-77%, 73-78%, 75-87% on reviewers' charts) · cost: 1 cycle
- "0 of its 5169 aliases" -> the shipped KB has 8300 alias SLOTS, 5169 distinct; the figure had been carried from CLAUDE.md's own text on a different base · cost: 1 cycle

## Raised by a fresh agent, missed by the author
- [gate 1] The scan-level hoist reached one of three whole-dataset scans · blocking · cost: 0 rounds (caught before code)
- [gate 1] The planned tests were vacuous: an identity test could not distinguish `fold` outside the loop from `fold(...)` inside it; the equivalence test compared two spellings of one code path · blocking · cost: 0 rounds
- [harden c1] The identity guard's ranking and witness halves were EMPTY — `assertEquals(0,0)` passed and reverting BOTH nameMatchStrength hoists left the whole build green · blocking · cost: 1 cycle
- [harden c1] `getAliases()` returned the live list, and index alignment had just become load-bearing: an in-place add gave a silently invisible alias, a remove an IndexOutOfBounds out of the safety pipeline · blocking
- [harden c1] `DrugSafetyValidator.namesSubstance` handed an already-folded clause to `matchesText` — the rule this PR ADDS, violated in-tree at the one site whose javadoc said the fix was blocked on exactly the accessor this PR builds · blocking
- [harden c2-c3] Guards defeated by successive relocations, each with the whole build green: bare assignment, qualified assignment, two line wraps, a getter read, extract-a-helper on the alias scan, extract-a-helper on the token, a qualified receiver inside a helper. Ended only by stating the property POSITIVELY and at class scope
- [pr r1] The accented fixture's alias was LOWER-CASE, so it exercised foldDiacritics and never toLowerCase; `foldedAll` written as `foldDiacritics(value)` — the spelling CLAUDE.md's own fold rule names — passed the entire suite · blocking · cost: 1 round
- [pr r2] The two witness accessors are whitelisted to name the raw list (their witness IS `aliases.get(i)`), so comparing against the RAW alias satisfied every structural assertion, and the accented fixture's single entry took the `matched.size() < 2` early return before the witness pass ran · blocking · cost: 1 round
- [pr r3] Index alignment was pinned against ONE edit shape (null-skip); a duplicate-collapsing edit passed all 1579 tests · non-blocking

## Where a skill blocked or contradicted this run
- harden:Termination — cycles 5-15 were all one ADR section's prose, each cycle finding one real defect in the previous cycle's rewrite. The rule ("delete the unsupported clause rather than replacing it") is right and I applied it repeatedly; what finally ended it was deleting the CLAIM SHAPE (any rule for a count) rather than the claim. Ten cycles.
- gate-state — `--only pr` is documented in pr-harden's State section but the helper rejects it (`unrecognized arguments`). Harmless here (the unscoped write is what a nested run wants anyway), but the doc and the tool disagree.

## Declined
- (none: no finding was declined in any round or cycle)

## Assumptions review overturned
- "the fix is the ticket's named operand" -> both operands, and the unnamed one is five sixths of the win (refutation gate)
- "a `FoldedName` type is over-engineering vs a method taking a folded String" -> kept the type, but the gate was right that the PROSE operand must stay a String, since `namedOccurrences` already establishes that shape (gate 1)
- "the alias half can be pinned behaviourally" -> it cannot where the mutation returns identical answers; but it CAN where the mutation answers differently, which round 2 found and round 1's structural-only framing had obscured
