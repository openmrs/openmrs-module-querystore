# resolve-ticket (+ harden, pr-harden) · openmrs-module-chartsearchai · #337 / PR 375 · 2026-09-04
outcome: converged
rounds: 1   cycles: 3   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-337/fd0a2aa9-4c4b-4a00-afc1-cf5448bc189f.jsonl

## Refuted by measurement
- "A client can reach a divergent citation's verbatim text by joining references[].index -> resourceUuid -> the safetyWarnings chip with the same (type, drug)" -> a safety_finding's resourceUuid names the SUBJECT drug alone; one drug screened against two active orders gives two records, two indexes, one resourceUuid, two chips. Two Phase-2 lenses measured it independently by driving the real injector. · cost: 1 phase-2 pass
- "The clauses renderFinding appends appear on no wire key" -> safetyWarnings[].chartOrderBridges publishes that clause's items, on the same chip object the paragraph points a client at. · cost: 1 review round (non-blocking)
- "null covers a cache hit" (plan) -> ChartSearchServiceRouter replays the ChartAnswer OBJECT, so a cache hit states the ORIGINAL request's list. · cost: caught at the refutation gate, 0 rounds
- "The record's sentence can be published" (plan) -> that means splitting a record on mayEndASentence, whose javadoc forbids exactly that; the whole richer-key design died and the change got smaller. · cost: caught at gate pass 1, 0 rounds
- "The statement can ride the early done" (plan) -> searchStreaming calls the check AFTER the ungrounded handoff, deliberately and under a documented latency comment. · cost: caught at gate pass 1, 0 rounds
- "putModuleStatements' null guard exists for a failed check" -> its routine trigger is the async early done; removing it breaks that event for every async stream. And the failed-check path cannot deliver a null at all, because both answer methods re-read patient.getPatientId() in their finally log. Confirmed by driving a throwing Patient through the real search. · cost: 2 phase-2 passes
- "A two-record ordering case pins report order" -> [2, 1] is descending, so it passed under a reversed TreeSet. Three records separate report order from both impositions. · cost: caught in-pass by my own control, 0 rounds
- "Report order is the order the ANSWER diverges in" -> it is RECORD-major; answer position orders divergences only within one record. · cost: 1 phase-2 pass
- Pre-flight: "no LLM endpoint on this machine" -> mis-scoped. The engine is local and self-launching on port 18085 with a bundled model under the standalone's appdata. · cost: 0, but it was carried into two briefs as a known problem

## Raised by a fresh agent, missed by the author
- [phase2] The README client contract's only operational instruction was false in the common multi-order case · blocking-equivalent · cost: 1 pass
- [phase2] Both DECLINE gates of the check could return null with the whole suite green, inverting the null/empty contract every doc rests on · cost: 1 pass
- [phase2] "in report order" promised by two texts and pinned by nothing · cost: 1 pass
- [phase2] theKeyIsWrittenInExactlyOnePlace's javadoc claimed a constant-hoist relocation reddens it; measured, it does not · cost: 1 pass (and the same javadoc had been over-claimed one pass earlier)
- [phase2] Two ADR anchors this branch added resolved to no heading; ProjectInstructionsGuardTest only resolves anchors cited from CLAUDE.md · cost: 1 pass. I then invented a third while fixing them.
- [phase2] Decision 61's "a tie-break with no correctness content" stopped being true once the check's answer was published · cost: 1 pass
- [r1] The chart-order clause IS on the wire, as items · non-blocking · cost: 0 rounds
- [r1] The ddi examples doc introduces the key 570 lines below the table listing which response fields to read · non-blocking · cost: 0 rounds

## Where a skill blocked or contradicted this run
- resolve-ticket Step 1 pre-flight checks for "an LLM endpoint" by looking for a listening port / llama-server process. On this module the engine is local and self-launching, so the check reports a blocker that does not exist, and it was carried into two agent briefs as fact.
- pr-harden Step 0: the state entry for this worktree carried three reviewed_shas from the EARLIER #337 run (PR #345) in the same reused worktree. pr-set preserves that field, so the ledger silently spanned two PRs. Cleared explicitly; worth a note that a reused worktree inherits a ledger.
- Many stale background wait-timers fired as task notifications throughout, one per superseded wait loop. Noise only, but it makes the transcript hard to read.

## Declined
- (none — no finding was declined in any round or pass)

## Assumptions review overturned
- A1 "render verbatim is satisfied by publishing the record's own words on the wire" -> replaced at gate pass 1 by publishing the CITATION only; no prose travels.
- A4 "the record's own SENTENCE is the right unit to publish" -> deleted entirely; the unit is the citation index.
- The plan's null-semantics description ("null covers a cache hit") -> replaced by "a cache hit replays the original request's list", and later by "the one reachable cause is the async early done".
