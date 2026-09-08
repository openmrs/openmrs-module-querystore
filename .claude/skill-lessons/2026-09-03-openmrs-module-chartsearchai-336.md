# resolve-ticket (+ harden, pr-harden) · openmrs-module-chartsearchai · #336 / PR 368 · 2026-09-03
outcome: converged
rounds: 2 (pr-harden)   cycles: 3 (harden: 3 Phase-1 passes, 6 Phase-2 passes)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-336/dda2889a-135d-49e2-9a68-6dbdacd97300.jsonl

## Refuted by measurement
- "the list it kept is complete and says so" / "nothing was withheld from it", in four homes -> a partial cede does NOT exempt survivors from `maxPairChips`; the same cap path runs on them (measured `found=55, reported=5` at cap 5). Shipped in the FIX commit and survived two harden Phase-2 passes. · cost: 1 pass
- ADR: "the cede branch was taken 14 times" -> actually 10, and it moves with every test added, so the count was deleted rather than corrected. · cost: 1 pass
- README: "a chip list led by a Major" (an ordering claim I introduced while correcting something else) -> #346's severity ordering is not in the recorded build `77c0f9a2`, and that build's own walkthrough records the Majors at positions 6 and 8. · cost: 1 pass
- ADR: "Each ARM's own `null` is a different and wider thing" -> the two sets are incomparable, neither contains the other; the pre-correction "not the same list" had been right. · cost: 1 pass
- "one known exception" (3 homes) -> a count, four lines above `PairChipExtent`'s own "Do not count the entries or the homes", and it UNDERCOUNTS: Decision 65 already records a second false-zero case. · cost: 1 pass
- Decision 69's provenance: attributing the 3-row table to `main` @ `77c0f9a2` made its own row 2 stale and made a Decision-65 consequence unobservable on the cited build (that build predates #356). · cost: 1 pass
- The ticket's own "one of them Major" -> the live verification found TWO (Warfarin x Ibuprofen and Warfarin x Diclofenac). Corrected from the running server.
- My plan's assumption that "README already carries all three sentences the comment asked for" -> README said the OPPOSITE of the third; refuted at the refutation gate, before code.

## Raised by a fresh agent, missed by the author
- [harden P2 p1] The SCREENING arm has the same defect via a different cede predicate (`InteractionPairs.alreadyReported`), reproduced as `of(0,0)` beside one Major chip. My own ADR paragraph had reasoned only about `StatedInteractionChips` and concluded the sibling was decided differently — the reuse lens measured the one I had missed. · blocking-equivalent · cost: 1 pass + a filed follow-up (#370)
- [harden P1 p1] The partial-cede test could not discriminate the boundary: on an ibuprofen-only chart both readings are `found: 1`, so the "withhold on any cede" mutation reddened NOTHING. Found by my own mutation check, which is why that check is worth running rather than reasoning about.
- [pr-harden r1] Two `Decision 68` -> `69` replacements printed `ok` and never reached disk: the script called `open(path,'w')` only after its loop, and an assertion later in the loop threw. The blocking finding of round 1. · blocking · cost: 1 round
- [pr-harden r1] The guard's TRIGGER was pinned only coarsely — narrowing it to `!chartOwned.isEmpty() && candidates.isEmpty()` was green on the whole suite while reinstating #336's defect for a clinical pair whose sibling entry pairs disagree. Both existing cede cases resolve one entry pair, so neither could see it. · non-blocking, implemented
- [pr-harden r2] The residue was recorded only as narrative in a 60-decision ADR, with no ticket to carry it. · non-blocking, implemented (#370)
- [harden P2 p5] README stated two contradictory readings of `found == reported` in adjacent paragraphs — found independently by three lenses in the same pass, all naming the same clause.

## Where a skill blocked or contradicted this run
- pr-harden:"base drift" — earned its place immediately. `main` merged #366 mid-flight and took ADR Decision 68, colliding with mine. Without that step the collision would have merged as two Decision 68s.
- pr-harden step 1's "fetch the pushed head" — the first fetch returned the pre-push sha; re-fetching was needed. The skill warns of exactly this ("the push had not landed when the fetch ran").
- ProjectInstructionsGuardTest's javadoc forbids raising the size budget in the commit that overflowed it, and the merge pushed CLAUDE.md over. `main` alone was at 84,961 of 85,000, so no rule of any size fitted. Resolved the way the file's own "Documenting a decision" section prescribes — cut EVIDENCE, keep the RULE.
- harden:Phase 2 "spawn four parallel review agents" vs. diminishing returns: on cycle 2 I ran two lenses rather than four (efficiency had been clean 6x with bytecode proof; reuse had machine-checked every pointer). Labelled as a deviation in the report.
- The `no-subagent-model-override` hook refused a `model: sonnet` retry after a session-429 killed an agent, which is correct and is now the documented state — the leaner-brief retry succeeded on the session model.

## Declined
- Sharing `chartOwningTheQuestionsOnlyPair()` with `DuplicateInteractionChipTest` — if we ship without it, nothing breaks: that class builds a different chart (lowercase names, `foldValidator()`), so unifying them would couple two cases that pin different things.
- Promoting the `"named in the question"` literal to a shared constant — if we ship without it, nothing breaks: it is a production literal at one site and my use is one of six inline copies, so a change to it already reddens five other cases first.
- `chartOwned` as `LinkedHashSet` rather than `HashSet` — if we ship without it, nothing breaks: `isEmpty()` is O(1) either way and the set is at most N²/2 small keys; pre-existing, and the iteration order feeds the `found` build order a comment relies on being dataset order.
- Pinning the screening arm's false zero with a test — if we ship without it, the reproduction rests on ADR prose alone and a later change to `alreadyReported` could stale it silently; declined because a test asserting today's `of(0,0)` would assert the defect as intended behaviour, so whoever fixes it would have to delete the test. Recorded in #370 instead, which names the gap.

## Assumptions review overturned
- "The comment's suggested remedy (README wording) is what the ticket wants" -> the value it asked to document was FALSE under the module's own published contract, so the fix is to the value; the refutation gate confirmed README said the opposite of the third requested sentence.
- "The screening arm has no cede, so this decision does not reach it" -> it has one, by a different predicate; reproduced, and the ADR paragraph rewritten from an argument into a measurement.
- "of(0,0), null and of(1,0) are the candidate values for the sibling arm" -> a fourth, of(1,1), was unconsidered; added with the reason it is refused (it counts a chip that arm did not raise).
