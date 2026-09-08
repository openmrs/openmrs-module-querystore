# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #370 / PR 371 · 2026-09-03
outcome: converged
rounds: 4   cycles: 4 (harden)   verifier: ran (works at runtime; ceding branch itself not reachable through /search)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-370/b5d6b395-6536-4ad2-9e1d-85a9a8c60825.jsonl

## Refuted by measurement
- "The documented predicate is the implemented one" (the @return, PairChipExtent, README and CLAUDE.md all said "ceded every pair") -> the code is `pairs.isEmpty() && cededToDrugInPlayArm`, i.e. kept none having ceded at least one; narrowing the guard to a literal total cede leaves the WHOLE suite green, so the wording invited an edit that reinstates the defect · cost: 1 harden pass + re-raised in r2
- "a ceded pair has its own chip" (round 1's own @return rewrite) -> false: `InteractionPairs.add` runs AFTER the `StatedInteractionChips` gate, so a pair whose drug-in-play chip was collapsed is still recorded and cedeable with no chip of its own · cost: 1 round
- "the key is every field the chip's SENTENCE is made of, so a chip this drops states no word another chip of the same pass does not state already" (round 2's replacement for a false universal) -> also false: `chartOrderBridges` is outside the key and `chartOrderClause` renders each bridge into the injected finding AS WORDS · cost: 1 round
- "there is no drug in play for this one to have screened" (round 2's README mechanism) -> false: an answer-named drug IS in play and the drug-in-play arm chipped it; the real gate is `questionDrugScreened` · cost: 1 round
- "seenPairs only ever drops a duplicate of a pair that was KEPT" (my ADR bullet) -> false: `seenPairs.add` runs before the cede test, so a key's first visit can be the ceded one · cost: 1 harden pass
- "a byte-identical survivor" (mine, post-merge) -> #347 made `chartOrderBridges` published-but-unkeyed, so a collapsed twin can differ on the wire · cost: caught in the merge repair
- Renumbering by the PHRASE `Decision 70` -> missed all eight `Decisions 69 and 70` / `60, 65, 69, 70` sites · cost: 1 round (this is skill text's own warning, and I still did it by phrase)

## Raised by a fresh agent, missed by the author
- [harden p1] README + PairChipExtent both said the trailing `grounded` event carries chips AND a statement; my change makes that false · non-blocking · cost: 0
- [harden p1] a `decision-70` anchor broken because the heading's em dash renders as two hyphens · non-blocking · cost: 0
- [harden p2] the `StatedInteractionChips` collapse filed among "arrangements where found == 0 is FALSE", 16 lines under the paragraph defining that zero as honest · non-blocking · cost: 0
- [harden p3] a sentence I inserted mid-sentence into a javadoc left two trailing fragments on the wrong antecedent, one of them refuted by my own test · non-blocking · cost: 1 pass
- [r1] the guard was pinned only on an entry-backed partner: narrowing the flag to `matched.partner != null` restored the defect with the suite green, for the #155/#290 population · non-blocking · cost: 1 round
- [r1] Decision 71's title and heading claimed a total cede, which the method's own @return forbids as wording · non-blocking · cost: 1 round
- [r2] Decision 65's headline bullet still licensed the fallback widening Decision 71 refuses; only Decision 60 of the three amended decisions had a forward pointer · blocking · cost: 1 round
- [r2/r3] the #356-fallback claim had EIGHT unswept homes and, separately, PairChipExtent stated the predicate over-broadly the other way · blocking · cost: 2 rounds
- [verifier] the ceding arrangement may be structurally unreachable through `/search`: `preAnswerFindings` injects a `safety_finding` naming both substances, so any answer mention is an echo and neither drug enters `inPlay` — the api-level reproduction reaches it only because it passes no mappings · cost: 0, recorded in Decision 71

## Where a skill blocked or contradicted this run
- resolve-ticket Step 4 / pr-harden "State": the branch was cut with `git checkout -b` off a detached HEAD, so it had no upstream until the first push; `gate-state --count-edits` then could not measure the commit half and printed "no upstream, no head from an earlier cycle" — I had to supply the count from `git log` myself. The skill documents both directions of this; it cost one extra command per cycle.
- pr-harden Step 1's stale-ref guard fired usefully in the other direction: reviewer agents in isolated worktrees could not `git fetch origin pull/371/head:pr-371` because a SIBLING agent's worktree already held that branch name. Two reviewers reported it. Briefing them to check out the SHA rather than create a named branch fixed it; worth stating in the brief template.
- pr-harden's retry contract met a **weekly** quota 429 (not a session one): "wait until the stated reset" was useless (reset was the next day) and the per-call `model` lever is hook-refused. A plain retry with a leaner brief succeeded, which suggests the first failure was a burst rather than the weekly cap actually being exhausted — the two are indistinguishable from the error text.

## Declined
- (none — 16 findings across 4 rounds, all implemented)

## Assumptions review overturned
- "the remedy is `null` and Decision 69's refusal of it can be answered by disputing its premise" -> held, and all four rounds accepted it; the refutation gate's blocking objections were zero
- "scope is the `alreadyReported` cede alone; the `StatedInteractionChips` collapse stays an honest zero" -> held, and r3's reviewer re-measured the residue and agreed
- "the collapse leaves a byte-identical survivor" -> replaced by "one stating that same relationship in the same words" after the #347 merge (merge repair), then the per-leg attribution deleted entirely in r2
