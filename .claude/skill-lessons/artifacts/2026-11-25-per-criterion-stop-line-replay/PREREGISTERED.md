# Per-criterion stop-line replay, pre-registered 2026-11-25 before any run (written before launch)

Question: do per-criterion agents briefed with a 25-minute stop line (--deadline 1800) find what the same brief with a
75-minute stop line (--deadline 5400) finds?

Subjects (the original round's own brief, line 1 changed for A25 only):
- s1: stockmanagement #71 round 8 lifecycle agent, head ad69ce66acffe6a34f1e63ec3579f521861675e1.
- s2: stockmanagement #80 round 1 behaviour agent, head d7a1eefa50e2aac52b34202c1d3fbeb29ec8f5e8.

Keys (each underlies a posted review comment):
- s1 G: the tests put only receipt and issue types through the lifecycle; making transfer-out completion credit the
  source instead of the destination (TransferOutOperationTypeProcessor) passes the suite.
- s1 I: cancelling an operation in NEW or RETURNED: requiring APPROVE instead of MUTATE (StockManagementServiceImpl,
  about :1602) passes the suite; no test has a mutate-only user cancel a NEW operation.
- s1 H: completing a stock issue straight from NEW: dropping `&& !requiresDispatchAcknowledgement()` from the completion
  check, so an issue can skip dispatch and receipt, passes the suite.
- s1 J: the insufficient-stock check on a direct dispatch from NEW: removing `|| DISPATCH` from the validation
  condition passes the suite; no test dispatches more than the balance straight from NEW.
- s2 T: the moved stock-source text search now trims the search string (HibernateStockSourceDAO), so a query with
  leading or trailing spaces matches differently from master.
- s2 D2: nothing pins that voided stock sources are excluded; the include-voided step of master's removed
  findStockSources_shouldFilterOnAllCriteria was dropped (removing the voided filter passes the suite).

Arms: A25 and A75, 3 runs each per subject, in waves of 4 (one per subject and arm), each wave started on a quiet
machine (no other round agents running, 1-minute load under 15).

Judging: one blind judge per subject over 8 shuffled reports: the 6 runs, the known-good (the original agent's report)
and a known-bad (s1: pr71-r8-unchanged's report; s2: pr57-r1-crit's report). Marks FOUND (named and backed by a run, a
mutation result or a file:line), NAMED (named without backing), ABSENT.

Calibration: void for a subject if its known-good is ABSENT on any key, or its known-bad is not ABSENT on any key.

Decision: per-criterion agents move to a 25-minute stop line (deadline 1800) unless some key is FOUND in at least 2 of
3 A75 runs and 0 of 3 A25 runs (the 2026-11-25 whole-PR replay's switch rule). Withheld on a void calibration.

## Deviation, recorded 13:47 before any report was read or judged
- blindz-s1-a75-1 is excluded: its agent started a 48-minute test chain in the background and ended its turn, and the run
  ended at 18.7 minutes (rc 143) with an 88-byte report ("Chain is running ... I'll report when it finishes"). That is a
  harness failure, not a finding result, and scoring it ABSENT would bias toward 25. It is re-run as blindz-s1-a75-4,
  which takes its place as A75 run 1 at judging.
