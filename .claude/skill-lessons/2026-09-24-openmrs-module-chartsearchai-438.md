# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #438 / PR #517 · 2026-09-24
outcome: converged
rounds: 2   cycles: 1 (harden: Phase 1 two passes, Phase 2 once)   verifier: ran (could not determine via local engine; works at runtime via remote-engine split-delta stand-in, with pre-fix positive control)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-438/f3ad137e-07c2-4842-8334-41f4b6a67a99.jsonl

## Refuted by measurement
- Plan's rejection of the api locus said the preliminary channel does not pass through AnswerExtractingConsumer -> refutation gate showed maybeEmitPreliminaryReasoning does go through it (LlmInferenceService:509-510); argument dropped before code · cost: 0
- First verifier (local llama-server) could not induce a split: llama-server emits whole UTF-8 per delta; the split path became observable only through a fake OpenAI-compatible server behind the remote engine · cost: 1 extra verifier run

## Raised by a fresh agent, missed by the author
- [gate] ADR Decision 101 paragraph recording #438 as open would go stale · non-blocking · cost: 0
- [harden P2] ChunkingStubService duplicated StreamingChartSearchStub's surface; redundant '?'/byte assertions; @throws reason named the wrong hazard; ADR "one delta becomes one frame" (x2) and "needs state in a per-event writer" made false by the change · non-blocking · cost: 0 rounds
- [r1] test javadoc "Each chunk becomes one frame" stale · non-blocking · cost: 1 blocking-only round
- [r2 note] only token/thinking carry separation pinned, not token/preliminary · note, unfixed

## Where a skill blocked or contradicted this run
- resolve-ticket:Step 2 — main moved between my first read (detached HEAD at 627449a7) and branching from origin/main; a script anchor on writeSseEventOrThrow's body failed its assert, surfacing it. Cost: one re-read.
- pr-harden:FINISH — verifier verdict "could not determine" would have meant converged-but-unverified; a substitute instrument (remote engine + fake server) settled it instead.

## Declined
- r1-2 tests via stub rather than real parser chain — if we ship without this, a change to AnswerExtractingConsumer's surrogate emission goes unpinned upstream; the controller joins a split pair from any source.

## Assumptions review overturned
- none
