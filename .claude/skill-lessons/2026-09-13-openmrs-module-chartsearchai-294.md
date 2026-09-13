# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #294 / PR 416 · 2026-09-13
outcome: converged
rounds: 2 (pr-harden)   cycles: 2 (harden, Phase 2 ran 5 passes in cycle 1)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-294/8adeb630-c50b-4ace-a868-86d6ff3ff1fb.jsonl

## Refuted by measurement
- "the demote-only remedy would leave this class's cases red / this case separates UNVERIFIABLE from DEMOTE_ONLY on the yes side" -> building the demote-only remedy leaves all three CodesOnlyActiveOrderGroundingContextTest cases GREEN; the case separates UNVERIFIABLE from GRADED. · cost: 1 Phase-2 pass
- "moving the disposition test inside the budget branch reddens a #284 case" -> it reddens the case's own three siblings (compoundClaim_/drugReference_/safetyFinding_doesNotConsumeTheEntailmentCap...) and no #284 case moves. · cost: 1 Phase-2 pass
- "the cosine-fail case is the ONLY one that can arrange the demote-only boundary" -> the cap case added in the same commit also publishes false there. Second refuted exclusivity claim about one family; fixed by DELETING the claim shape rather than rewording. · cost: 1 Phase-2 pass
- "a javadoc named test covers an off-target stamp" -> stamping FALSE on every injected drug_reference mapping left the WHOLE build green; that test reads only the active_drug_order mapping. Hole was real (UNVERIFIABLE outranks DEMOTE_ONLY, so it would silently kill the #106/#122 off-topic false). · cost: 1 Phase-2 pass, closed by a new test
- "at least one caller is a canary the walk does not check and never did" -> the walk ends in assertEquals against a singleton, which an empty list fails; the canary is real. · cost: 1 pr-harden round (non-blocking)
- efficiency lens refuted its own brief's premise: the second harden commit added ZERO real injections to CitationGroundingVerifierTest (those cases already drove one via injectedActiveOrderText).
- the ADR's entailment-cap "+" bullet cited an A/B nothing in the repo reproduced; replaced with a committed cap-boundary test that reddens on rule removal.

## Raised by a fresh agent, missed by the author
- [harden p1] Seven+ stale enumerations of "why a grounding verdict is withheld" after adding a fourth reason; the one that mattered most was RecordReference.getGrounded()'s own javadoc — the public accessor a consumer is pointed at. · non-blocking · cost: 1 pass
- [harden p1] A new helper's javadoc forbade hand-building an active-order mapping while a hand-builder survived 3 lines away in the same file. · cost: 1 pass
- [harden p2] ORPHANED JAVADOC created by the pass that swept for orphans (third recurrence in this project). · cost: 1 pass
- [harden p2] A descriptor-TAIL guard is silently disarmed by a rung added BELOW the widest: a 12th-parameter rung plus a second writer left both guards green, 11/11. Closed by a prefix canary. · cost: 1 pass
- [harden p3] A vacuity precondition computed from the COMPLEMENT of the stamped set, so the very mutation the case exists for defeated it and reported the wrong cause. · cost: 1 pass
- [harden p3] ADR Decision 80's two-axis framing falsified; the injector javadoc naming it as that argument's home HAD been updated, the ADR had not.
- [harden p4] eval docstring discounted the new null cause on "the six cases below are condition-shaped"; CASES holds EIGHT and the last two are medication questions — the shape most likely to cite this record.
- [pr-harden r1] The only PR-round finding, and it was a false claim about what an assertion covers — the same defect class the whole run kept hitting.

## Where a skill blocked or contradicted this run
- harden `gate-state --count-edits`: with no recorded head from an earlier cycle the commit half falls back to `@{u}..HEAD`, so a converged cycle read `edits=6`. The helper prints the caveat; pushing made it read 0. Cost: one confusing measurement, no rework.
- pr-harden round-1 reviewer died on a session 429. The documented lever (cheaper model) is refused by a PreToolUse hook, so the only remaining levers are a leaner brief and waiting. Leaner brief + the reset window converged on attempt 2. The leaner brief also told the reviewer to spend budget on claims it DOUBTED rather than re-confirming documented mutations — the first attempt had exhausted itself re-running them.

## Declined
- (none) — every finding across 5 harden passes and 2 PR rounds was implemented.

## Assumptions review overturned
- "#294 reserves the remedy choice, so the deliverable is the measurement" -> the measurement was already merged (#410); what was left of the ticket was the remedy, and the issue is still open on its own defect headline.
- "a wire-sited, display-keyed carve-out is #294's own first candidate and therefore the route" -> deciding it at the GRADING layer is strictly better: no change to groundedForWire, no new carve-out inside #201's guard, and it removes the Tier-2 call and cap slot a wire carve-out cannot.
