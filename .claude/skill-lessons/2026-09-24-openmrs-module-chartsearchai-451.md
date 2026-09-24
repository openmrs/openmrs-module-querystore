# resolve-ticket · openmrs-module-chartsearchai · #451 / PR #510 · 2026-09-24
outcome: converged
rounds: 1   cycles: 1   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: /Users/danielkayiwa/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-451/bacbdf7b-acad-456a-90be-b5d917bca2f2.jsonl

## Refuted by measurement
- Plan: "convert the two throw sites the ticket names and test instanceof" -> gate pass 1 found on Tomcat a refused write of the classic done / grounded frame arrives as ClientAbortException(IOException), which the OLD cause test happened to classify as disconnect; the ticket's fix alone would regress those to ERROR · cost: 0 (caught at plan time)
- Revised plan: "serialize outside the write for classic done and grounded" -> gate pass 2: the early-done lambda also serialized inside the disconnect try · cost: 0 (settled, applied)

## Raised by a fresh agent, missed by the author
- [gate] classic done / grounded Tomcat regression · blocking · cost: 0 rounds
- [gate] early done serialization inside disconnect try · blocking · cost: 0 rounds
- [harden P2] ERROR-with-throwable assertion could be met by two different log lines; references frame disconnect unpinned; ADR 105 Context still present-tense · polish
- [r1] PR body claimed README/ADR "point at the new test" — false · non-blocking · fixed in FINISH body re-derivation
- [r1] early-done serialization failure untested · non-blocking · declined by fixer

## Where a skill blocked or contradicted this run
- gate-state: bare `await` refused without --run (reaches harden entry); needed `--only pr` for the Step 3 refuter await. Cost: one retry.

## Declined
- r1-1 early-done serialization failure test — if we ship without this, a later edit moving serialization back into the disconnect path passes every test, which matters only once a future wire key carries a bean Jackson cannot serialize; no real input fails today (surrogates, NUL, 30M chars serialize).
- harden P2 efficiency: oversized stack logged twice — if we ship without dropping it, the operator log carries the stack twice per oversized response; kept because grounding callers swallow the APIException.

## Assumptions review overturned
- none
