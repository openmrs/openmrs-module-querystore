# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #477 / PR #523 · 2026-09-24
outcome: converged
rounds: 2   cycles: 2 (harden: Phase 1 ×2 traversals, Phase 2 ran, escalated once, then once more clean)   verifier: ran (works at runtime, standalone-8082, head de611fd5)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-477/608ffbd8-3a07-431b-b0e8-fc2713a49842.jsonl

## Refuted by measurement
- plan: "append the finding last on a drug question; composeFromFindings left behaviourally untouched" -> refutation gate cited Decision 114's #346 reversal; mutating the composer key reddened the new module-answer case (the strength sort would put the finding above the proposed drug's caution) · cost: 0 (plan time)
- first full build after the gate: 10 class-arm tests reddened; root cause was the shared helper classChipDetails' premise ("every interaction chip over a rule-less dataset is the class arm's") made false · cost: 0 cycles

## Raised by a fresh agent, missed by the author
- [refuter] position of the finding vs the composer's strength sort · blocking · cost: 0
- [refuter] Metformin precondition proved nothing about Metformin (other listed drugs satisfied it) · non-blocking
- [refuter] #397 enumeration clause silently withheld — pin it · non-blocking
- [harden P2 integration] a set of orders sharing ONLY the question's drug restated alreadyInSeveralOrders' fact in the opposite referent (two chips for one fact) · substantive, escalated Phase 1 · cost: 1 cycle
- [harden P2 quality] README warnOnInteractions row and three javadocs still said "a screen's finding"; ADR 116 over-claimed chips/answer agreement · non-blocking
- [r1] gate on questionDrugs vs inPlay unpinned — `!inPlay.isEmpty()` left 2515 green · non-blocking, implemented by fixer · cost: 1 round
- [r2] a question listing her current drugs puts them in askedAbout (note only)

## Where a skill blocked or contradicted this run
- harden: orphan-javadoc sweep caught a real orphan I created by anchoring an insertion on a method signature (the memory's own warning) — sweep worked as designed.

## Declined
- r1-2 #397 clause withheld on a drug question beside two orders sharing an unasked substance — if we ship, the model gets no enumerate-every-finding instruction there and may drop the already-taken finding; chips/findingCitations/unstatedFindingSeverities still carry it.
- r1-3 prompt ranking leads a caution-only proposal with this finding — if we ship, "Can I give her metformin?" may open with her TB duplication while chips list it last.
- reference/CLAUDE.md:65 not naming ordersSharingASubstance as a current-medication source — file 7 bytes under its 76,500 budget; referent is pinned by SubstanceInSeveralActiveOrdersTest.everyFindingAboutTheDrugInPlayReachesTheModelInOneReferent.

## Assumptions review overturned
- "exactly one ordersSharingASubstance finding; alreadyInSeveralOrders stays" held; refined in harden P2: a set whose every shared substance was asked about is skipped.
