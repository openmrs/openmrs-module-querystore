# resolve-ticket + harden + pr-harden · openmrs-module-chartsearchai · #473 / PR #475 · 2026-09-23
outcome: converged (PR #475 ready at 9a56bf3c; Refs #473, Refs #391 — closes neither)
rounds: 4 (3 full + 1 blocking-only after a main merge)   harden cycles: 4 traversals of Phase 1   verifier: ran twice (works at runtime, 3f362bf7 and 9a56bf3c)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-chartsearchai/ebdba95d-a862-4016-aa20-e9122b99ff19.jsonl

## Refuted by measurement
- #473's own proposed mechanism ("join derived edges on one condition" for QT/hepatotox/neuropathy) -> the derived tier holds no QT, liver or neuropathy edge among the reported drugs; only the lactic-acidosis family connects · cost: 0 (measured before planning)
- plan: "strength = withhold, both halves are Major ratings" -> gate pass 2 measured a false link (metformin->methadone hypotension) and cited Decision 86; caution adopted · cost: 0 (gate)
- "one pair raises one chip" in the docs -> a harden Phase 2 agent measured 4 chips for trandolapril/perindopril · cost: 1 harden cycle
- default-on shipping -> round-1 reviewer measured metformin's contraindication sentence read as causal against 52 Major-rated drugs (ACE inhibitors, beta-blockers); tier moved behind a default-off GP · cost: 1 round

## Raised by a fresh agent, missed by the author
- [gate p1] partner naming by entry rung names a combination's constituents as two orders · blocking · cost: 0 (fixed at build, caught again by OneOrderNameAcrossOneResponseTest)
- [harden P2] rationale misquoted Decision 86; "nobody rated" then false again in a later cycle (DDInter can rate the pair) — claim shape deleted · 2 harden cycles
- [harden P2] ProjectInstructions orphaned javadocs x2 (found by the awk check, not an agent)
- [r1] default-on false causal chips on common co-prescriptions · blocking · 1 round
- [r1] co-causes unnamed from the cause side · non-blocking · fixed in r1
- [r2] second chartOrderBridges call site unpinned · blocking · 1 round
- [r2] round-1's test narrowing became a pure loosening once the tier shipped off · blocking · 1 round

## Where a skill blocked or contradicted this run
- resolve-ticket Step 1 — the checkout held two other sessions' uncommitted work and another session's branch; moved to an own worktree (../chartsearchai-473) before any edit. Not a cost, but the skill has no step for "the checkout is not yours" outside the pool driver.
- ProjectInstructionsGuardTest — reference/CLAUDE.md sat 2 bytes under its cap at base; any new rule needs a recorded raise. Took 76,000 -> 76,500 after trimming.
- harden Phase 2 "four agents" — ran 4, then 2, then 1 (delta) across the three Phase 2 passes; deviation stated in-session.

## Declined
- none

## Assumptions review overturned
- A1 drug-in-play arm only -> kept, now tracked in #480
- A2 no GP of its own -> overturned in r1: chartsearchai.drugSafety.derivedFindings, default off
- A3 caution -> held (gate p2, r2 question answered in ADR 110 against #359 row E)
