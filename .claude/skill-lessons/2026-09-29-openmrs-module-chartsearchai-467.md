# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #467 / PR #558 · 2026-09-29
outcome: converged
rounds: 4   cycles: 3 (harden: Phase 2 escalated twice)   verifier: skipped (change is backend-init.sh; standalone never runs it, no Docker daemon) — substitute: whole-entrypoint tests + dash harness + CI
context: no compaction · peak not surfaced
transcript: /Users/danielkayiwa/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-467/09771f80-4abc-44c4-a106-ab830865389b.jsonl

## Refuted by measurement
- "a scan that races a rename can miss an artifact altogether (glob)" -> round-1 fixer showed the 15/3000 losses came from the per-name [ -e ] check, not the glob; natural misses 0/3000 · cost: 0 rounds (ADR corrected in r1)
- first dash race harness (single early rename) read 0/3000 for the known-bad version -> needed a continuous terminal-state renamer as positive control · cost: one measurement
- my fake seed_sql in the dash drive printed empty values -> case mismatch (status vs Status) in the fake's extraction, caught by reading the output
- ADR said the alternative "was measured" -> it was only considered; corrected before commit

## Raised by a fresh agent, missed by the author
- [harden P2a] full volume: state as file CONTENTS cannot be written, so refusal stays fetching: forever · substantive · cost: 1 cycle
- [harden P2b] publisher scan racing a rename publishes a refused artifact as verified, permanently · substantive · cost: 1 cycle
- [harden P2a] guard bypass via gp_set_if_blank; missing test for empty-dir guard (author-found); stale class javadocs (author-found)
- [r1] publisher should compose from the expected artifact set (unrecorded:<id>) instead of what the directory lists · non-blocking · cost: 1 round
- [CI on r2 head] ': >' on a special builtin is fatal under dash, so an unwritable state dir killed the whole start; local /bin/sh is bash on macOS so the suite passed locally · blocking · cost: 1 round

## Where a skill blocked or contradicted this run
- gate-state:harden-set — rejects --only (only await/clear-await/clear take it); one retry
- harden:Phase 2 — ran three times (two escalations); each escalation was a real defect

## Declined
- (none declined; deferred in PR body: gp_set helper, manifestRow helper, pid liveness, (publish) & guard form, 900-miss give-up undriven, pre-existing seed ': >')

## Assumptions review overturned
- "subshell records ''/content for verified" -> file-name encoding (harden P2)
- "value composed from what the directory lists" -> composed from WEIGHTS_ARTIFACTS with unrecorded:<id> (r1)
- "tests under /bin/sh represent the image" -> false on macOS; harness now prefers /bin/dash (r3)
