# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #412 / PR #427 · 2026-09-14
outcome: converged (round 1, blocking 0)
rounds: 1   cycles: 1 (harden, overridden at cycle 2)   verifier: skipped (no production code; nothing runtime-visible for a standalone to exercise)
context: no compaction · peak not surfaced · one claude.ai session usage limit mid-run, which killed 3 of 4 Phase 2 agents
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-412/b61ffa3a-67d2-4a7e-ac03-e23b97d28dc8.jsonl

## Refuted by measurement
- "The ALLERGEN arm's identity chip takes the public constructor and answers false" (written by the orchestrator into a javadoc as the fix for a DIFFERENT false claim) -> false since #348: that arm goes through `SafetyWarning.recordedAllergenContraindication`, which hardcodes the flag. Found independently by two Phase 2 lenses. · cost: 1 pass
- "the identical-detail pair makes ANY value computed from another published field disagree with the accessor" (pre-existing, the ticket's own second ask) -> false: a value that READS the accessor and narrows it agrees on every chip in that class. · cost: 0 (the ticket named it)
- "the whole build stayed green / read the green build" (chip 9's instruction, pre-existing and true at base) -> made false BY this change: the two fixtures now differ, so no single detail-sniff leaves the build green. Measured by the integration lens by running it. · cost: 1 pass
- Plan-stage: "one of the two omod fixtures" assumed to be the severity-wire one -> held; gate pass 2 raised no blocking objection to it.
- `reference/CLAUDE.md` pointer extension -> attempted, `ProjectInstructionsGuardTest` reddened: the file was at exactly its 76,000-byte budget. Reverted. · cost: 1 build

## Raised by a fresh agent, missed by the author
- [gate p1] The over-claim had THREE homes in the class, not the two the plan enumerated (the third in `liveCode`'s javadoc — the paragraph documenting the source pin's fail-open residue). · blocking · cost: 1 gate pass
- [gate p2] The planned replacement wording carried a false universal of its own ("agrees with the accessor on every chip in that class"). · non-blocking · cost: 0
- [gate p2] The planned residue wording published a per-key enumeration that tracks `serializeSafetyWarnings`' key set — the ADR records that exact enumeration going stale twice. · non-blocking · cost: 0
- [h p2] A production expression hand-copied into javadoc where a live assertion already proves it. · non-blocking · cost: 1 pass
- [h p2] "the source pin below" — a dangling cross-file pointer; then a SECOND one, self-referential, inside the source pin's own javadoc, introduced by the fix for the first. · non-blocking · cost: 2 passes
- [h p2] The same claim given two homes (ADR + javadoc; class javadoc + method javadoc). · non-blocking · cost: 1 pass
- [h p2] The realism citation lands on a chip answering `false`, with no route to the sentence+`true` pairing the fixture exists for. · non-blocking · cost: 1 pass
- [r1] The javadoc credits the wire guard with the whole narrowing axis when each fixture holds one half — the tidy-the-two-back-into-line hazard restored for the other population. · non-blocking · filed as #429

## Where a skill blocked or contradicted this run
- harden:Phase 2 — the session usage limit killed 3 of 4 isolated-worktree lenses mid-work, each after ~8 minutes of real work, with only a one-line partial result. The surviving lens returned three real findings. Re-dispatch after the reset succeeded with no change to the briefs other than a newer head and an applied/deferred list. The limit is not distinguishable in advance from a stalled agent.
- pr-harden:FINISH — "FINISH does not edit the cleared sha" sent a genuinely good non-blocking finding to a follow-up issue rather than into the branch. Correct by the rule and worth recording: the finding's own failure mode is the ticket's defect restored for the other population, which reads as more than a nit.
- resolve-ticket:Step 3 — the gate's "check it, do not estimate it" paid immediately: gate pass 1's blocking objection cited a file:line that had to be read before the plan was revised, and the revision then avoided the re-wording trap pass 2 caught.

## Declined
- (none — no finding was declined; round 1 raised no blocking finding, and every Phase 2 finding was either applied or, in one case, measured and deliberately not acted on: fixture slots 3 and 4 are interchangeable, but that predates this change and deleting one retargets every positional read above it.)

## Assumptions review overturned
- "soften the class javadoc" read as binding two homes -> three homes (gate pass 1), then confirmed complete by gate pass 2 and three later agents.
- "the ALLERGY arm's sentence" as terminology -> "a self-named ALLERGY RULE's sentence"; the module distinguishes the allergen arm (answers false) from an allergy-typed curated rule (can answer true), and a neighbouring javadoc in the same file turns on that distinction. Found by the orchestrator in harden Phase 1, then the conflation recurred in the FIX and was caught by two Phase 2 lenses.
