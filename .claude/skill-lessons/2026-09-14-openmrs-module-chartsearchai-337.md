# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #337 / PR422 · 2026-09-14
outcome: converged
rounds: 2   cycles: 3 (harden)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced · one session usage-limit stop mid-run, resumed from disk with nothing lost
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-337/3d2476c6-f9b5-49bb-bfba-79fbb4197d18.jsonl

## Refuted by measurement
- "Closing the ASCII-elision residue means a gap question that is not silencing, and then `."` is a false report again; the two cannot both be had" (ADR Decision 61, standing claim) -> a dots-RUN rule leaves `."` a run of one and untouched; refuted by the shape of the fix · cost: 0 (found at plan time)
- "Reporting a marked partial quotation re-opens the truncation case Decision 78 refuses" (gate pass 1, blocking) -> measured through the real search: `…`, `—` and `[…]` ALREADY reported both the resumption and the fresh-sentence shape, so the rule adds no new kind of report · cost: 1 gate pass
- "Restrict the carve-out to a cut resumed in LOWER CASE" (gate pass 2's own suggestion, and pass 1's remedy) -> unimplementable where the rule belongs: `Words.of` hands the predicate a gap that by construction contains no letter · cost: 1 gate pass
- "#337's second capture is an elision the fix makes visible" (ADR Decision 61's residue bullet, pre-existing) -> that capture marked its cut with NOTHING, so it leaves no terminator and was always reported · cost: 0
- A/A control band published in ADR Decision 95 -> published twice and refuted twice (once as a number no run produced, once as a spread a third rig measured far outside); deleted the claim shape rather than writing a third · cost: 2 harden passes

## Raised by a fresh agent, missed by the author
- [harden P1] Carve-out applied to RECORDS too withdrew the record-sentence exit at the `endSentence` seam, so an answer reproducing an operator note ending in an ellipsis FAITHFULLY was reported — the crying-wolf failure the check must not have · blocking · cost: found before PR
- [r1] The dots-run test was pinned from BELOW only: `>=`->`==` (four-dot cut) and an added end-of-gap test (unspaced cut) each reinstated the defect with all 2140 tests green · blocking · cost: 1 round
- [r1] Decision 95 said option 1 was "refused at Decision 61 and again at Decision 90", distributing a refusal over a reading the record leaves open · blocking · cost: 1 round
- [harden P2/P3/cycle2/cycle3] Six separate passes each found one more false or stale claim in prose the previous pass had written — counts of marker spellings, "the ONE way", "all three", "three of four", singular "the ASCII spelling" · cost: 4 harden passes
- [fixer, unprompted] The marker loop's failure message printed `marker.trim()`, so the spaced and unspaced rows produced byte-identical text — which would have made a future mutation unattributable · non-blocking

## Where a skill blocked or contradicted this run
- harden:Termination — the phase gates are finding-based, so three Phase-2 passes each "converged" while the cycle had edits in it; the cycle gate is what forced cycles 2 and 3, and cycle 3 (single-agent documentation confirm) is the one that found nothing
- pr-harden:Step 1 — the reviewed-sha comparison and the `gate-state` PR-change ledger drop both did real work: the entry carried 2 reviewed shas / 6 declined findings from PR 384 in this same worktree
- Token-grep sweeps failed six times in a row on one claim; what finished it was enumerating the claim's SUBJECT (every sentence in the diff quantifying spellings), which found four homes at once

## Declined
- (none — round 1 declined nothing, and round 2 raised nothing)
- Deferred with failure modes, not declined findings: `mayEndASentence` public arity change with no overload — an out-of-module caller compiled against the 1-arg form breaks at link time; none exists in this repo and an overload would reintroduce the default the design refuses. `unfaithfullyRenderedCitations` widens at upgrade time — a client rendering one badge per entry sees more entries with no change in model behaviour; stated in the PR body since no repo grep reaches it.

## Assumptions review overturned
- "The ellipsis slice is threshold-free" -> true only for the answer operand; on the record side it trades a miss for a false report and the carve-out had to become a per-operand parameter (harden Phase 1)
- "This makes #337's own captures visible" -> it makes none of them newly visible; what it removes is a dependence on a glyph (r1 / ADR correction)
