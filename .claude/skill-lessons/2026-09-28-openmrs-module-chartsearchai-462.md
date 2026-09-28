# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #462 / PR #551 · 2026-09-28
outcome: converged
rounds: 1   cycles: 5 (harden)   verifier: skipped (javap -c -p of LocalLlmEngine base vs head identical, positive control differs; change not runtime-visible)
context: no compaction · peak not surfaced
transcript: /Users/danielkayiwa/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-462/52cfcc53-a49d-4a4d-9ebc-d63cc2ead51a.jsonl

## Refuted by measurement
- Plan's call regex `requireListenerMayBeServed\([^;)]*,\s*launchedAtNanos\s*\)` (copied from the #512 sibling) -> cannot match today's call, whose getHttpClient() argument has its own ')'; refuter caught it before code · cost: 0 rounds
- Guard javadoc "a stamp handed in as a parameter ... leaves nothing to slice" -> false with a parameterised overload beside an uncalled no-arg decoy (measured green) · cost: 1 harden cycle
- Residue sentence "a field only startServer sets ... unset in every test" -> the plain form reddens LocalLlmServerAuthTest; only a Math.min with Long.MAX_VALUE default passes · cost: 1 harden cycle

## Raised by a fresh agent, missed by the author
- [harden P2 c1] parameterised waitForServerReady decoy overload; 5-arg gate overload ignoring last arg — both fail-open, guard green · closed by reflection (one declaration per name) + gate named only in readiness + startServer calls waitForServerReady()
- [harden P2 c1] stamp comment "a stamp this method takes for itself cannot be wired to the wrong moment" contradicted by #462's own measurement
- [harden P2 c2] anonymous-class field shadowing launchedAtNanos; gate body counting from a field — named as residue (identity, gate body)
- [harden P2 c3] outer call taking the stamp while a lambda-wrapped gate gets another — closed by requiring the gate call to BEGIN its statement
- [harden P2 c4] relaunch inside readiness's loop — named as residue
- [r1 note] startServer prologue moved into readiness after the stamp passes; block-lambda childAlive arg reddens confusingly · non-blocking notes, in PR body

## Where a skill blocked or contradicted this run
- harden:Phase 2 — adversarial integration agent defeated the text guard a new way on four successive Phase 2 passes; each escalation cost a full Phase 1 + Phase 2 cycle (5 cycles for a ~150-line test change). Ended when the defeats were of kinds already named as residue.

## Declined
- none

## Assumptions review overturned
- none (ticket's "Decision 103" = today's Decision 107 held)
