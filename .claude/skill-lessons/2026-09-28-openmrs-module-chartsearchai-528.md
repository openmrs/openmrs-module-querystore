# resolve-ticket · openmrs-module-chartsearchai · #528 / PR #546 · 2026-09-28
outcome: converged
rounds: 3   cycles: 4 (harden, ended on labelled override)   verifier: ran (works at runtime, on fafffa08 and on merged head 1624f4ec)
context: no compaction · peak not surfaced
transcript: /Users/danielkayiwa/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-528/5851be10-262f-4e80-819f-bae353ae34aa.jsonl

## Refuted by measurement
- plan: "+12% prompt tokens" as the cost of dating every record -> audit input_tokens showed +26% to +33% on five larger charts (12% held only on the ticket's 44-record chart) · cost: refutation gate pass 1
- a bytecode comparison of "verified classes vs merged classes" -> 177/189 classes differed, i.e. the saved classes came from a different build/JDK; the comparison was uncalibrated and was discarded in favour of re-running the verifier · cost: one verifier run

## Raised by a fresh agent, missed by the author
- [refute-1] arm C is #66's inline-date baseline, measured worse on E4B's drift metric; the plan never re-ran it · blocking · cost: plan revision
- [refute-2] the committed temporal DB-truth probe (temporal_probe_rc2.py) existed and was not in the plan · blocking · cost: plan revision
- [harden P2] #66's PR table contradicts its commit message on which model the 0.428/95 row is · substantive · cost: 1 escalation
- [harden P2] /prewarm re-prime needs action=restart; a javadoc credited #528 with #74's "explicitly-dated" failure; share-of denominators wrong twice · substantive · cost: 2 escalations
- [r1] the ticket also asked for the drift/presence axis; the README's pure-prompt yes/no A/B was the documented substitute on this cohort · blocking · cost: 1 round (~2.3 h of captures)
- [r1] the new guard used a 9-record fixture; a size-gated return of compression passed the whole suite · non-blocking · cost: 0 (implemented in r1)

## Where a skill blocked or contradicted this run
- harden:Phase 2 — escalated three times on ADR prose this run wrote; ended with the labelled override rather than a fourth four-agent pass
- pr-harden:FINISH — main moved between marking and checking mergeability and took ADR Decision 121; merge + renumber owed a blocking-only round and a second verifier run

## Declined
- none

## Assumptions review overturned
- "the drift metric cannot be run here, so it is omitted" -> replaced in r1 by the presence A/B (capture_probe_yesno.sh + compare_arms.py): 64/64 -> 63/64 verdict-led, false presences 2 -> 2 but in different cells
