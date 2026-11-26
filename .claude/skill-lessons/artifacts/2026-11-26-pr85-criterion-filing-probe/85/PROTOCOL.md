# Filing probe: stockmanagement #85 round 1's decision point (pre-registered 2026-11-26, before any run)

Owner-directed ("fix the pr-review skill so this doesn't happen again"), after the ticket's author raised at
issuecomment-7185869450 what round 1 had set aside: ModuleConstants' static helpers against O3-6352's first acceptance
criterion, "No static settings utility remains".

## Subject
PR 85 at head 8f7bb30, base 17bd512, frozen at 2026-11-24 21:31 UTC (review start). Frozen repository holds only those
two commits' history; bc98c50, 1ef1032 and later master commits are absent (checked). Snapshots: the PR description at
T (the round's own c14 brief), the ticket with the fields pr-review's fetch asks for (no reporter), the conversation
filtered to before T, the four agents' reports verbatim, and the round's four posted comments verbatim (A, B, C, E).

## Items and their labels (fixed now)
- R1, the helpers against criterion 1: FILE (the ticket's author raised it).
- R4 (getGlobalPropertyValue(key, "") taste), R5 (config.xml vs code defaults, pre-existing), R8 (core's message),
  R9 (import move): NOISE if filed (or, for the reader, if their reason is rejected).
- R2 (privilege), R3 (defaults copied), R6 (unbounded numeric settings), R7 (email dropped on an unchanged line):
  REPORTED, not scored. No maintainer has ruled on them, except that on R2 the ticket's author ended (r4705280102)
  where the round's reason did.

## Arms (run names map to arms in private/KEY-private.json, locked during the wave)
- Reviewer, n=5 each: C = pr-review 0.42.0 as live; A = C plus one sentence at the end of Step 2's "The ticket is
  evidence, not a spec" paragraph (skill-diff-A.diff).
- Reader, n=5: B = a blind reader given the round's real set-aside reasons, asked whether each holds.

## Predictions and reading rules (fixed now)
1. C files R1 in at most 2 of 5. If C files it in 3 or more, this decision point does not reproduce the real miss,
   and A's gain is not measured here.
2. A files R1 in at least 4 of 5, and is noisy (files any of R4, R5, R8, R9) in no more runs than C.
3. B rejects R1's reason in at least 4 of 5, and rejects a NOISE item's reason in at most 1 of 5 runs.
A shape is a candidate only if its prediction holds. Scored mechanically (scripts/score.py) before the key is opened.
