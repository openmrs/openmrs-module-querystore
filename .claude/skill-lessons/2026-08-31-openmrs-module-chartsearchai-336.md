# resolve-ticket (+ harden, pr-harden) · openmrs-module-chartsearchai · #336 / PR 341 · 2026-08-31
outcome: converged
rounds: 1   cycles: 2   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-336/54c61f29-c6ba-418a-914d-0016cd15ec61.jsonl

## Refuted by measurement
- "a clinician-facing '10 of 72 shown' needs a per-question container the chip API does not have, so it is a frontend change rather than a module one" (the shipped `maxPairChips()` javadoc, a recorded decision) -> half right: the chip ARRAY has no container, but the RESPONSE is one. A key beside `safetyWarnings` is a module change. · cost: 0 (caught at plan time)
- "the two-line needle prefix now matches twice, so the third line is what makes it unique" (written by me in 4 texts) -> measured: line 1 alone matches 3 times, the two-line prefix already matches exactly once, unique by ONE character (comma vs `)`). The third line buys loud re-targeting, not uniqueness. · cost: 1 Phase-2 pass
- "Every such stub in this repo was retargeted in that commit" (my javadoc) -> two stubs were not; the claim was made from the diff rather than from a sweep, and both were silently inert. · cost: 1 Phase-2 pass
- "the answer, the chips and every `references[].withheldInteractions` byte-identical to a complete screen's" (3 homes) -> a complete screen of that patient reports 18 chips, so byte-identity is impossible; the true measured wording is the ticket's own. · cost: 1 Phase-2 pass
- Three successive drafts of an "N situations / N homes" count, each refuted by the next reviewer (three where the list held two; one home where README held a second; two while the async early-done case was a third). Ended by deleting the CLAIM SHAPE rather than writing a fourth count. · cost: 3 Phase-2 passes

## Raised by a fresh agent, missed by the author
- [harden P2r1] The public `validate(...,Sink)` -> widest-arity delegation was unpinned: mutating it to `null` left the entire build green, so the feature was joined end to end nowhere. · blocking-equivalent · cost: 1 pass
- [harden P2r2] The cut-not-the-cap property was asserted on one arm only; the GP-instead-of-cut mutation survived the whole api suite on the question-pair arm, because every case there ran with the cap BELOW the candidate count. · cost: 1 pass
- [harden P2r3] Recording `(0,0)` inside `validate`'s fail-safe catch left api and omod green — a crashed screen could publish what README tells a client to read as a COMPLETE one. · cost: 1 pass
- [harden P2r3] The both-keys guard read the whole FILE, so splitting `putSafetyChips` into two writers passed the guard whose own message forbids exactly that. · cost: 1 pass
- [harden P2r3] The null enumeration was missing the async early-`done` case — and it is the one a consumer must NOT treat like the others. · cost: 1 pass
- [verify] The standalone's `chartsearchai_audit_log` lacks `reference_slice_records`/`reference_slice_chars`: the module's liquibase never ran because the same SNAPSHOT version was redeployed onto an instance that already had it. Inherited from #229's round, not this PR. · non-blocking, environment

## Where a skill blocked or contradicted this run
- pr-harden:round-1 review — the first reviewer died on an API session rate limit (429), not a stall or nested spawn. The skill's dead-phase contract (retry twice, change something) worked: retry 1 with a leaner brief and a smaller model completed. Worth noting the contract's "change something between attempts" had to be read as "reduce token volume", which is not one of the two examples it gives.
- harden:Phase-2 — four parallel agents in isolated worktrees all reported the local `main` ref was stale by many commits, so `git diff main...HEAD` showed ~15k lines. Every brief had to name `09717dc7...HEAD` explicitly. The skill warns about this for pr-harden's reviewer; the same hazard bites harden's Phase 2 agents and is not stated there.

## Declined
- Making the PROMPT's pairwise list say it is bounded — if we ship without it, the model still writes prose from 10 of 18 pairs with nothing saying so, i.e. the ticket's defect on the channel that shapes the answer text. Declined because a prompt clause is measurable only against the #107 eval gate (four prior prompt levers failed it) and because putting a withheld count into quotable record text is issue #117. Recorded in ADR 59.
- A guard on the once-on-normal-return publication — if we ship without it, a future edit can move publication back into the arms and nothing reddens. No test can exist today: no reachable path throws between an arm and the return. Stated in javadoc and ADR rather than claimed as covered.
- Publishing `withheld` / `isBounded()` — if we ship without them a client subtracts two integers. A derived figure beside a measured one is the shape #261 exists to stop; taken on BOTH the wire and the Java surface after a first draft kept them in Java only.

## Assumptions review overturned
- "A5: `withheld` is not published; the difference is the client's" (wire only) -> extended to the Java surface too, after a reviewer noted the accessors had no production caller and made the ADR's own rule read as a wire-only exception. · harden P2r1
- "the extent is recorded per arm as each finishes" -> published once on `validate`'s normal return, so a pass that degrades to an empty warning list cannot describe chips it discarded. · harden P1r2
