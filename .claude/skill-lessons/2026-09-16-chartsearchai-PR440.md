# pr-harden · openmrs-module-chartsearchai · #439/PR440 · 2026-09-16
outcome: converged (round 5 reported 0 blocking on the handed-over sha a1d19569)
rounds: 5   cycles: n/a   verifier: skipped (stated) — the fix is the absence of an argument from three log statements; no deployment condition can reintroduce it, the level fact was verified from core's shipped log4j2.xml, and all three sites are pinned by tests on the real production loggers
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-chartsearchai/b2cc1733-01a0-4706-a6cf-518055292fb4.jsonl

## Refuted by measurement
- "The sweep found every log line naming a patient's medications" (orchestrator, before round 1) -> a third site existed, DrugSafetyValidator's screening cap WARN. The sweep script filtered log-call ARGUMENTS by identifier name; that call's args were `withheld`/`pairs`, which matched no name-shaped pattern, and when a plain grep did surface the line the orchestrator judged it "counts only" from the message prefix without reading the arguments. · cost: 1 round
- "The renamed cap case asserts no drug name over the whole line and every level" (round 2) -> its private appender raised only DrugSafetyValidator's logger and only to WARN; a log.info of the same details passed all nine cases. · cost: 1 round
- "The three sites are now guarded at the same depth" (round 3, first half) -> true only under one surefire run order. LogCapture.close() restored a level but left the LoggerConfig its constructor installed, and a sibling test's class-named capture then outranked a later package capture. Reproduced by run order alone. · cost: 1 round
- "Each negative asserts over every captured event" (round 3 prose) -> describeAll() rendered a throwable's TYPE only, so names attached to a diagnostic exception passed every guard. · cost: 1 round
- "The probe left all three negatives green" (round 4 reviewer's evidence) -> two of the three could not see that probe at all (their harnesses stub the validator and injector out), so their green measured nothing. Corrected by the fixer, which probed each site separately. · cost: 0 rounds (caught inside the round)
- "Widening the capture to the module root was tried and reverted" (round 2 measurement, recorded in javadoc) -> falsified by round 3's own fix, which removed its cause. A measurement can be invalidated by a later commit ON THE SAME BRANCH. · cost: part of 1 round

## Raised by a fresh agent, missed by the author
- [r1] A third disclosure site the scan did not report · blocking · cost: 1 round
- [r2] Decision 102's residue paragraph covered a site (PatientClinicalContextBuilder) whose reasoning it did not reach · non-blocking
- [r2] README carried a second home of the corrected claim two rows above the corrected one · non-blocking
- [r3] LogCapture's leftover LoggerConfig (order-dependent guard) · blocking · cost: 1 round
- [r3] LlmAnswerExtractor logs the model's off-schema citations JSON — a fourth class the sweep's own criterion reaches · non-blocking
- [r4] The throwable channel · blocking · cost: 1 round
- [r4] A liveness precondition that passes while the negative it protects is vacuous · non-blocking
- [r5] Two guard scopes whose comments cite an argument wider than the scope they have · non-blocking

## Where a skill blocked or contradicted this run
- pr-harden Stop gate fired correctly once: the orchestrator cleared the await after a fixer returned and then ended the turn with phase=fixing. The await mechanism only permits a yield while something is outstanding, so "clear the await" and "end the turn" must not be adjacent.
- The gate's own design forced a useful habit: recording a background BUILD as an await (not just an agent) is what lets an orchestrator yield while a build runs.

## Declined
- (none — every finding in five rounds was implemented; two residues were filed as #441 and #442 with the reasoning recorded in ADR Decision 102 rather than declined on the record)

## Assumptions review overturned
- "A security-scan fix needs no instruction-file rule, per #435's precedent" -> round 5: #435's rule bound ONE directory; this one binds two packages, which the root CLAUDE.md's own criterion assigns to the root file. Recorded as item 6 of #443 rather than applied to a cleared sha.
- "A grep hit plus a message prefix is enough to classify a log line" -> it is not; read the arguments. This produced the run's only genuinely missed disclosure.

## Environment
- A subagent died on a session 429 mid-edit, leaving the reviewer's PROBE in a production file — a throwable carrying six drug names on the cap WARN. The worktree-hash snapshot is what caught it. The surviving half of that agent's work was committed protectively before retrying, so the retry's restore-from-memory could not revert it.
- `-Dtest='A+B'` runs NOTHING. Surefire wants commas. Found by a fixer only because the surefire report mtimes were 90 minutes stale. **Corrected by the 2026-09-16 retro, which measured it: maven exits 1 with `No tests matching pattern …`, not 0 — the flag does not suppress that, and the command this run issued was `mvn … -DfailIfNoTests=false 2>&1 | grep -E … | head -20`, whose status is `head`'s. BUILD FAILURE was in the captured output and went unread, which makes this a third sighting of `harden`'s *Reading the run's own output is what failed next* rather than a new fact about surefire.**
