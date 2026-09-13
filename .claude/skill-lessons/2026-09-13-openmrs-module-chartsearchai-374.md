# resolve-ticket + harden + pr-harden · openmrs-module-chartsearchai · #374 / PR 411 · 2026-09-13
outcome: converged
rounds: 2 (pr-harden)   cycles: 11 (harden)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-374/b3075594-ac69-4afb-a319-0fed9a92d6fa.jsonl

## Refuted by measurement
- "A demotion of a condition rule's rank would be unobservable — a condition rule has no co-keyed rival" -> unmeasured, and wrong in the rank space: CURATED_RULE is 1 and demotes onto SELF_NAMED_RULE_WITHOUT_A_NOTE's 0. Two further attempts to support the same conclusion were also refuted (the two constants sharing a key space — CURATED_RULE's javadoc says the opposite; and ContraindicationRouteVariantTest.oneCuratedRuleAuthoredTwiceRaisesOneChip exhibiting a co-keyed CURATED_RULE rival — both its rules answer selfNamedAllergyRule). Ended by deleting the CLAIM SHAPE, not by a fourth attempt. · cost: gate pass 2 + cycles 6, 9
- "a primitive boolean is a JDK type, which is what the bridges are not" (the XML rationale) -> both wrappers XStream refuses are JDK types and the non-JDK ChartOrderBridge marshals fine; the criterion is Collections' immutable collection wrappers. Written in cycle 1, caught in cycle 2, then found in a SECOND home in cycle 4. · cost: 2 cycles
- "#374's Reachability section is wrong in both halves" -> one of its two witnesses (GI bleeding) IS a free-text condition and satisfies the disjunct it was offered against; only the allergen case refutes, and that is outside the sentence's condition-token scope. · cost: 1 round
- "the Dexibuprofen allergen is CODED" -> unestablishable from that measurement: PatientClinicalContext holds allergy tokens as plain lowercased strings and the validator never sees whether the record was coded. Came from the REVIEWER's own evidence and would have been propagated. · cost: 0 (caught by the fixer in the same round)
- "the seed's class-token allergy rules are nsaid and penicillin" -> three: aminoglycoside (Gentamicin) missed. Measured by driving selfNamedAllergyRule over every rule the seed publishes. · cost: 1 cycle
- "liveCode's residues can only make a count too low, so the assertion fires" -> fails OPEN: a // or /* inside a string literal hides a SECOND live put and the count stays at the expected 1. The rewrite that introduced this claim was itself the correction of a different false claim. · cost: 1 cycle

## Raised by a fresh agent, missed by the author
- [harden c1] The source pin was defeatable: comment out the real put, re-derive from `detail` beside it -> 181/181 omod tests green, because the sniff agreed with every fixture chip. Fixed in the FIXTURES (an identical-detail chip pair), not by hardening the regex. · blocking · cost: 1 cycle
- [harden c7] corroboratedByTheChart returns true UNCONDITIONALLY for a curated allergy rule that is not self-named, so a CLASS-token chip publishes false with the chart never asked — missing from both enumerations of what a published `false` can mean, and reachable on the BUNDLED seed. A client-contract defect. · cost: 1 cycle
- [harden c3,c4,c5,c8,c9,c10] Eighteen homes of a claim the change falsifies, found one per cycle by SAMPLING. The last two were pairs (a README paragraph and the javadoc summarising it) and one was at a site that never names the key. · cost: 6 cycles
- [pr r1] The client contract documented the weakness of `false` exhaustively and framed `true` entirely around the accident case — but on the bundled seed no accident is reachable and every `true` is a clinically CORRECT match the corroboration test over-hedges. A client following the guidance would render a possible-coincidence qualifier on exactly the correct findings. · blocking · cost: 1 round
- [pr r2] Both omod fixtures' `true` chips carry a CONDITION sentence, so nothing on the wire side exercises the allergy-rule `true` the change also publishes. Demonstrated by narrowing the put with `&& getDetail().contains("active condition")` — source pin satisfied, 181 tests green. · non-blocking, filed as #412 · cost: 0
- [verifier] PatientClinicalContextBuilder.addDrugName reads a lazy Drug association its own javadoc calls "a plain String column"; one LazyInitializationException abandons the whole active-order read and stamps the chart unread. Pre-existing, filed as #413.

## Where a skill blocked or contradicted this run
- harden:Termination — ten cycles each found exactly one more prose home by sampling an unbounded corpus; the loop could not terminate that way. What ended it was the skill's own "change the KIND of question" rule: enumerating the population by SUBJECT (the corroboration answer, the chip's key set, the accessor's visibility) rather than by phrasing bounded it at 59 sentences, all checkable. Worth considering whether that move deserves to be stated as the remedy for one-finding-per-cycle, not only for repeated false claims.
- pr-harden:FINISH — correctly stopped me applying r2-1, which I was inclined to fix. The rule that FINISH must not edit the cleared sha is what kept the handed-over sha equal to the reviewed and verified one.
- Agent tool — one Phase 2 agent died on a session rate limit (429). Retried with a leaner brief after the reset and it converged, matching the recorded pattern.

## Declined
- (none in either pr-harden round — round 1's fixer implemented both findings, round 2 raised nothing blocking)
- chartsearchai.drugSafety.findingsRenderedByClient ships `true` while config.xml:325 says "Default: false" and LlmProvider.java:1269 says it "ships false" — if we ship without fixing it, a maintainer reading either site believes a stock install's user message is byte-identical to pre-#403, which it is not since findingProse resolves SUMMARISED. Pre-existing on main (28dbed9d, #403), out of this PR's scope; reported, not filed.

## Assumptions review overturned
- A2 (the key carries the flag's existing meaning, so it also reads true for the self-named ALLERGY population) -> held, but incomplete in a way I did not anticipate: the NON-self-named allergy population publishes a `false` that is not a measurement at all, which neither the accessor nor README named until cycle 7.
- "the accident-of-spelling case is what `true` means" -> overturned by pr r1: on the shipped data that case is unreachable and `true` marks correct findings the wording rule over-hedges.
