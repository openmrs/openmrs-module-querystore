# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #446 / PR 453 · 2026-09-17
outcome: converged
rounds: 3 (pr-harden)   cycles: 8 (/harden)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-446/1981e903-85eb-4872-a3a4-f99921dcf315.jsonl

## Refuted by measurement
- "DEBUG is the opt-in boundary — the one issue #439 drew for the safety-finding shortfall, and the position ClassCodeFidelityCheck states for record text" (written into production javadoc as the justification for moving the endpoint's error body to DEBUG) -> ADR Decision 102 says the exact opposite, verbatim: "The finding's own alternative, the names at DEBUG, was not taken: a channel nobody needs is not worth the bytes of PHI it writes." ClassCodeFidelityCheck's position is "not logged", not "logged at DEBUG". The DEBUG choice was still right, for a different reason (no first channel exists), but the cited authority refuted it. · cost: 1 cycle
- "ERROR_BODY_BUDGET = 2 MiB, calibrated from ~0.7 MB of macOS loopback slack" -> CI red on Linux at 2.67-2.75 MB, on all three Java jobs, and red at the previous head too. A /harden fresh-reviewer lens had predicted exactly this ("a macOS number doing a cross-platform job") and the orchestrator logged it as a risk rather than acting. · cost: 1 pr-harden round + a red CI run
- "one cumulative counter subsumes per-line/per-chunk" (the change's central design argument, stated as an invariant in BoundedResponseStream's javadoc) -> true of the code, but NOTHING in the committed suite discriminated it: every streaming case sent newline-terminated chunks, so moving the bound onto the parser's accumulated text passed the whole suite, both guards included. The 13 hostile shapes that had tested it lived in a review agent's throwaway harness. · cost: 1 pr-harden round
- A source-text guard asserting "every response.body() sits behind a named bounded reader" -> defeated six ways across five cycles (line wrap; renamed local; helper in another file; the by-name-exempt file hosting the read; LlmResponseParser.parseStreamingResponse satisfying the lookbehind while removing the ceiling; a ceiling constructed with Long.MAX_VALUE). It is a call-graph question wearing a text match's clothing. · cost: ~4 cycles
- "the len clamp prevents the overshoot filling the heap" -> at a ceiling near Long.MAX_VALUE the arithmetic overflowed, allowed became 0, and readAllBytes spun: 780 s of CPU under jstack. The first fix (Math.max(1, ...)) then overran the buffer on the len==0 probe readAllBytes uses. · cost: 1 cycle

## Raised by a fresh agent, missed by the author
- [/harden c4] The oversized APIException carried an IOException cause, and ChartSearchAiRestController reads `getCause() instanceof IOException` as a client disconnect — so the new diagnostic produced NO error event and NO log on /search/stream. Measured end to end through the real controller. · blocking · cost: 1 cycle
- [/harden c5] The endpoint's error body was written to the DEFAULT server log at ERROR, and a hostile endpoint can echo the prompt — the patient's chart — back as one. Pre-existing, but the change was about to pin it as specification with a new test. · blocking · cost: 1 cycle
- [/harden c4] Javadoc orphaned onto a constant: a field with its own doc comment inserted between a rule's javadoc and the rule. The run's own orphan-check script missed it, because the javadoc close was itself a changed line. · cost: 1 cycle
- [pr-harden r2] CI had never been green on Linux. · blocking · cost: 1 round
- [pr-harden r3] SAFETY_LIMIT is derived from the constant under test, so the suite pins that a ceiling exists and not how big it is: BYTE_ALLOWANCE_PER_OUTPUT_TOKEN 1024 -> 3072 leaves 11/11 green. · non-blocking, filed as #454

## Where a skill blocked or contradicted this run
- pr-harden Step 1 — briefing the reviewer to "check out detached" left MY worktree detached; the next phase's fixer then edited in detached HEAD and the branch had to be reattached before committing. Reattaching also hit a leftover agent worktree holding the branch (`--ignore-other-worktrees` was needed). Rounds 2 and 3 briefed "the worktree is already at the head — do not check out anything" and the problem did not recur.
- 35 orphaned `agent-*` worktrees accumulated from isolated /harden agents; nothing reaped them, and one held the PR branch.
- Editing by script: two multi-sub scripts aborted on a failed assert AFTER earlier subs had modified the in-memory string but BEFORE the single write, so three corrections silently never landed — and the follow-up only re-applied the one that had failed. Caught a cycle later by a fresh agent reporting the same three sentences still false. The per-edit-write rule exists for exactly this and was not followed.

## Declined
- (none — no finding was declined in any pr-harden round)

## Assumptions review overturned
- "a cumulative stream ceiling satisfies the ticket's per-line/per-chunk by subsumption" -> still true, but it was an untested premise of the plan and stayed untested through /harden; pr-harden round 1 required the two discriminating peers before it could be believed.
- "the ceiling's exact value is pinned by the suite" -> never was, in either direction; now stated as residue and filed as #454.
