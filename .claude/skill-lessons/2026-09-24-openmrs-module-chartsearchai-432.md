# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #432 / PR #536 · 2026-09-24
outcome: converged
rounds: 4   cycles: 4 (harden, Phase 2 escalated three times)   verifier: skipped (tests + docs; the one production edit drops a checkcast on the null constant, no runtime behaviour to observe)
context: no compaction · peak not surfaced (run was paused by the pool driver mid-harden and resumed)
transcript: /Users/danielkayiwa/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-432/2348f3bc-6c67-4129-b67d-f313a5913b10.jsonl

## Refuted by measurement
- plan: "exclude RecordMapping's own class file, as the shared helper does" (gate pass 1 made it blocking, the plan adopted it) -> a with-copy method and the 8-arg rung's (Date) null are then invisible; defaulting that rung to a date left the whole api suite green (2530 tests) · cost: one harden cycle
- quality agent: "discontinueOrder ignores the date it is passed" -> bytecode shows ifnonnull; aMomentBefore(new Date()) only when no date is given · cost: 0
- my own git checkout -- restore after a mutation loop discarded an uncommitted production edit (the cast removal); the next mutation results were void and had to be re-run after committing · cost: ~3 builds

## Raised by a fresh agent, missed by the author
- [harden c1 P2] RecordMapping exclusion hid copy methods and the non-carrying rung · substantive · cost: 1 cycle
- [harden c2 P2] RecordMapping::new (invokedynamic method handle) bypasses the invokespecial reading · substantive · cost: 1 cycle
- [harden c3 P2] setter on a de-finalised field bypasses it -> answered by a positive property (field final) rather than another route · substantive · cost: 1 cycle
- [r1] a narrower rung assigning the final field directly (putfield) bypasses it -> exactly one putfield, in the widest constructor · blocking · cost: 1 round
- [r2] root CLAUDE.md had no pointer to the new rule (paid for by trimming) · non-blocking · cost: 1 round (step 3 exception)
- [r3] PR description's "Not done here" made false by r2's fix · blocking · cost: 1 round (same sha re-reviewed; body-only fix)

## Where a skill blocked or contradicted this run
- harden: the "successive agents defeat one more way" loop ran four times on one guard (direct pass, method handle, setter, putfield) before the positive properties (final + single putfield) stopped it; the KIND-of-question rule was applied one cycle later than it could have been
- pr-harden step 1: a body-only blocking fix leaves the head unchanged, so the next round re-reviews the same sha; the step-1 guard treats that as suspect, and the cause here was known and legitimate

## Declined
- (none)

## Assumptions review overturned
- "no CLAUDE.md pointer, because the file is 1 byte under budget" -> r2 fixer trimmed rationale in the same file and added the pointer, round 2
