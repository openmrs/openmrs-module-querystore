# resolve-ticket (+ harden, pr-harden) · openmrs-module-chartsearchai · #454 / PR #518 · 2026-09-24
outcome: converged
rounds: 2   cycles: 2 (harden: Phase 1 → Phase 2 escalated → Phase 1 → Phase 2)   verifier: skipped (test-only change; no runtime-visible path)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-454/192c2dbd-f265-4d66-bd6e-f0028ce82ce7.jsonl

## Refuted by measurement
- The ticket's recommended fix ("state SAFETY_LIMIT as 16L*1024*1024 … caps how far the ceiling can be raised unnoticed") -> with that literal AND the ticket's own mutation (allowance 1024→3072, 12 MiB ceiling) the suite was 11/0 green; flood peers are cut off at ceiling + kernel slack < 16 MiB. Measured before planning; plan went one step further (fixed-size body one byte over an absolute budget). · cost: 0 (caught pre-plan)
- Own prose, harden cycle 1: "a ceiling raised by megabytes is still cut off short of SAFETY_LIMIT", "every other oversized case … passes wherever that constant is set", "a peer under a raised ceiling still is [cut off]" -> each a false universal once SAFETY_LIMIT became absolute · cost: within Phase 1 passes

## Raised by a fresh agent, missed by the author
- [harden P2 quality] the value was pinned from above only; a 4096-byte ceiling passed the whole suite, while the ticket says "no test pressure in either direction" · substantive → Phase 1 resumed, lower-bound case added · cost: 1 harden cycle
- [harden P2 quality] class javadoc "pins the ceilings from BELOW … so a LOWERED ceiling passes them" self-contradictory, and false for the error ceiling (MAX_ERROR_BODY_BYTES=16 reddens anOrdinaryErrorBodyReachesTheLogWhole) · substantive · same cycle
- [harden P2 quality] "is what every OVERSIZED case asserts" false once the fixed-body case existed; two more homes of it (PEER_EXIT_SECONDS, peerReachedItsSafetyLimit javadocs) found only by the second Phase 2 · polish/re-flag
- [r1] at-ceiling case byte-identical to at-budget case while green · non-blocking · implemented as javadoc naming the distinguishing mutation · cost: 1 blocking-only round

## Where a skill blocked or contradicted this run
- none observed. resolve-ticket's "plan may go past the ticket's recommendation when measurement shows it misses the ticket's own regression" had no explicit rule; handled as an assumption and the gate accepted it.

## Declined
- none

## Assumptions review overturned
- "pinning from below is out of scope (ticket keeps the at-ceiling case relative)" -> overturned by harden Phase 2 (ticket's "either direction"); lower-bound absolute case added in harden cycle 2, before the PR
