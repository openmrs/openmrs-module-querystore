# resolve-ticket · openmrs-module-chartsearchai · #450 / PR 457 · 2026-09-17
outcome: converged
rounds: 2 (pr-harden)   cycles: 4 (harden, ended on labelled override)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-450/bc6d71bf-e2e4-498a-b9f2-7cb0b8fe0a61.jsonl

## Refuted by measurement
- "CLAUDE.md can take one more bullet for this rule" -> the root file is 24,997 bytes of a 25,000 budget; the rule binds one class, so by that file's own file-selection rule it belongs in javadoc + ADR. Settled at the refutation gate, cost 0.
- "Publishing an api test-jar so omod tests can use LogCapture is a contained build change" -> the omod's unpack-dependencies filters by neither classifier nor scope, so it shipped 211+ api test classes plus TestingApplicationContext.xml and chartsearchai-hibernate.cfg.xml inside the released .omod, build exit 0 and suite green. It also falsified a PRODUCTION javadoc's stated reason for a public API (DrugSafetyValidator's StandingChartAlerts factories are public *because* omod declares no api test-jar). Reverted. cost: 1 harden cycle.
- "The three recordOutput() calls are covered" -> deleting all three left the omod suite green, and one of them is the only thing auditing a client gone on the `thinking` frame. cost: 1 cycle.
- "auditAttempted must be set before the save, so the flag is true for every way the attempt can end" -> false: anything that ESCAPES saveAuditLog is thrown before the insert, so the early flag made the finally decline and left a delivered answer unrecorded — #450's own defect inside its own fix. A swallowed persistence failure returns normally, so a flag set after the call is reached anyway. cost: 1 cycle.
- "The gate does not reach the too-large-chart path" -> maybeEmitPreliminaryReasoning runs before the committed pass that raises ChartTooLargeException, and the preview is over a smaller slice, so it is precisely what succeeds when the full chart overflows. cost: 1 cycle.
- "The two audit suites assert exactly one ROW per streaming query, so a pre-persist design writes two" -> they assert saveAuditLog CALLS and the DAO is saveOrUpdate, so the ticket's own recommendation 1 yields one row from two calls. The whole fail-closed decline rested on this. cost: 1 review round.
- "MAX_RESPONSE_BYTES bounds a remote response, so a cap here would be redundant" -> the symbol exists nowhere and #446 is an OPEN finding whose subject is that nothing is bounded. cost: 0 (caught in-cycle).
- "The lambda costs +32 bytes" -> 48; the 32 came from an arm whose stub never fired the consumer, where escape analysis removes the allocation. cost: 0.
- "No single search term finds every home of the no-api-test-jar claim" -> `test-jar` finds all of them; this was the fourth attempt at that parenthetical, so the claim shape was deleted rather than the token replaced. cost: 0.

## Raised by a fresh agent, missed by the author
- [gate2] CLAUDE.md is 3 bytes under its guard's budget · blocking · cost: 0
- [gate2] The source-scan pin for the ERROR level rested on a false premise about LogCapture's reachability · blocking · cost: 0
- [c1] The api test-jar's real cost (see above) · blocking · cost: 1 cycle
- [c1] Three recordOutput() calls undiscriminated · blocking · cost: 1 cycle
- [c2] The attempt flag set before its save · blocking · cost: 1 cycle
- [c2] ControllerLog leaked a LoggerConfig, reintroducing #439's third-round defect · blocking · cost: 1 cycle
- [c3] A cache hit is a third disconnect window, unnamed and untested · blocking · cost: 1 cycle
- [c3] The ADR still said the flag goes up before its save, four lines above the paragraph describing the reversal · blocking · cost: 1 cycle
- [c4] Decision 103 was the only decision missing from the ADR's table of contents; ProjectInstructionsGuardTest checks cited pointers, not the index · blocking · cost: 1 cycle
- [r1] The fail-closed decline rested on a specification about calls, not rows · blocking · cost: 1 round
- [r1] README told a reader a row with a real searchMode is an ordinary row; this branch's own tests refute it · blocking · cost: 1 round
- [r2] Criterion 1 is met only in part (a query that reaches inference and fails before any channel writes no row) and the decline list did not name it · non-blocking · folded into the re-derived body
- [verify] The assigned standalone was running a stale pre-change expansion — no StreamAuditState.class, different controller hash — which the deploy-identity hash caught and the omod timestamp would not have · cost: 0

## Where a skill blocked or contradicted this run
- resolve-ticket Step 3: the gate is defined as at most two passes, and both passes returned blocking objections that SETTLED rather than opened. Correct per the three-outcome rule, but the second pass's objections were about cost/benefit facts (the .omod contents, the byte budget) that only a measurement could supply — the gate being read-only by instruction, the run had to verify them itself afterwards. Worked, but the division of labour is worth noting.
- harden Termination vs the prose loop: four cycles, and from cycle 3 on every finding was in prose the previous cycle wrote — cycle 4's own cut introduced 3 of its 7 findings. Harden's Phase 1 is in-context, so the author rewrites their own paragraphs; the override was taken on that measured ground and pr-harden's separate fixer then produced two blocking findings the author had had four cycles to find.
- pr-harden FINISH: editing the PR body flipped closingIssuesReferences from [] to [450], because "whoever closes #450" parses as a closing keyword. The skill's instruction to re-check the FIELD rather than learn a rule about the prose is what caught it.

## Declined
- (none in either review round; the fixer implemented all three round-1 findings)

## Assumptions review overturned
- "A source scan is an adequate pin for the ERROR level, because LogCapture cannot be reached from omod" -> the premise was false (LogCapture imports nothing from the module and log4j-core is already on omod's test classpath); but the REMEDY the gate implied — publish an api test-jar — turned out to cost more than the assertion buys, so the answer was a small single-purpose capture in the omod test package. Overturned at gate pass 2, then re-decided in cycle 1.
- "One row per query holds because each site flags its attempt before making it" -> holds because each site flags AFTER its save returns. Overturned in cycle 2.

# pr-harden · openmrs-module-chartsearchai · PR 457 · 2026-09-17
outcome: converged
rounds: 2   cycles: 0   verifier: ran (works at runtime, classification repaired)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-450/bc6d71bf-e2e4-498a-b9f2-7cb0b8fe0a61.jsonl

## Refuted by measurement
- "The two audit suites pin one ROW per streaming query" -> they pin one saveAuditLog CALL; HibernateChartSearchAiDAO uses saveOrUpdate, so the ticket's recommendation-1 design yields one row from two calls, and the fail-closed decline's stated reason did not hold. cost: 1 round.
- "A row whose searchMode is one of the three real ones is an ordinary row" -> the fallback files the pipeline's own answer whenever the handoff happened, so a stream that failed in the grounding tail is indistinguishable from a completion. The branch's own tests pin it. cost: 1 round.
- "#450 is closed by this change" (implicitly, by the shape of the decline list) -> criterion 1 is met in part: a query that reaches inference and fails before any consumer channel writes no row. Round 2, non-blocking.

## Raised by a fresh agent, missed by the author
- [r1] The decline rested on a specification about calls rather than rows · blocking · cost: 1 round
- [r1] The README's published reading rule for audit rows was false · blocking · cost: 1 round
- [r1] Two api test javadocs still said the streaming row comes from one of two ChartAnswers · non-blocking · fixed in round 1
- [r2] The fallback's elapsed-time comment rules out substituting 0 but not the likelier slip, passing startTime — which IS discriminable · non-blocking · issue #459
- [r2] The `unknown`-is-a-subset claim is unscoped in README and the ADR while the constant's javadoc scopes it correctly · non-blocking · issue #459
- [r2] "one row per query for any implementation of the consumer contract" is broader than StreamAuditState's own same-thread premise · non-blocking · issue #459
- [verify] The standalone was running a stale pre-change expansion; the deploy-identity hash caught it where the omod timestamp did not
- [verify] Two pool slots share chartsearchai.llm.serverPort 18085 and evict each other's llama-server · environment

## Where a skill blocked or contradicted this run
- pr-harden FINISH: the body re-derivation flipped closingIssuesReferences to [450] via "whoever closes #450". Re-checking the FIELD after the edit — which the skill mandates — caught it; a rule about prose would not have.
- pr-harden Step 6 vs the ticket: the module's INFO line for the fallback row never appeared on the standalone because the console carries WARN and above for this module, so the DB rows had to be the evidence. The known ineffective-log.level pitfall.

## Declined
- (nothing declined in either round)

## Assumptions review overturned
- "The one-row-per-query specification rules out pre-persisting, so the streaming endpoint cannot fail closed" -> it rules out pre-persisting without re-specifying two suites; nothing shows the endpoint cannot fail closed. Round 1.
