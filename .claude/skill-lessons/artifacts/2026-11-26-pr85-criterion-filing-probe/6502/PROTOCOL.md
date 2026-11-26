# Filing probe, held-out: openmrs-core #6502 round 1's decision point (pre-registered 2026-11-26, before any run)

Held-out case for the shapes developed on stockmanagement #85: here too an agent reported a listed acceptance
criterion missed by its words (TRUNK-6666's fifth, a metric/counter of upgraded accounts) and the round set it aside.
Unlike #85, no maintainer has raised it; rounds on 10-22, 10-26, 11-11, 11-12 and 11-25 each reported it to the user
("your call", "needs your decision") and none posted it.

## Subject
PR 6502 at head c2fee5de40, base 2.8.x at b882f77c80, frozen at 2026-11-25 14:40 UTC. The frozen repository's refs
are those two; its objects are borrowed from the local core checkout (blind-run's own stance: no sandbox). Snapshots:
the PR description (never edited), the ticket with pr-review's fields, the conversation before T, the three agents'
reports verbatim, and the round's posted body and three comments verbatim.

## Items and labels (fixed now)
- R1 (criterion 5 missed by its words): REACH, ground truth unknown. Scored as "filed or not".
- R6 (parallelism-only bumps, Spring's behaviour the thread defers to), R7 (no SHA-1 upgrade without opt-in, a decided
  design), R8 (stale audit fields on the returned User, reasoned and not run): NOISE if filed.
- R2 (log line unpinned), R3 (caller-transaction write loss), R4 (failure test inside the test transaction), R5 (a guard
  nothing reaches, which Step 3 calls a one-line nit): REPORTED, not scored.

## Arms
C = pr-review 0.42.0; A = C plus the same sentence as the #85 probe's arm A (identical skill files). n=5 each.

## Predictions and reading rules (fixed now)
1. C files R1 in at most 2 of 5.
2. A files R1 in at least 4 of 5, and is noisy (files any of R6, R7, R8) in no more runs than C.
If C files R1 in 3 or more, the current text already files it here, and A's reach on this PR is not measured.

# Wave 2, anchored (pre-registered 2026-11-26 after wave 1's unblinding, before any wave-2 run)

Wave 1 result: C filed R1 in 5 of 5 and A in 5 of 5, both noisy in 0 of 5. Reading rule 1 applies: the current text
already files R1 at this decision point, so A's reach on this PR was not measured there.

The real 11-25 round differed from wave 1's decision point in one input: it read its own earlier rounds' notes on AC5
(a tool result at 2026-11-25T14:21:47Z, lines 123 and 161 of the notes it read). Wave 2 adds those two lines verbatim
to "Where the review stands", and nothing else.

Arms, n=5 each, blinded afresh: C and A, same skill files as wave 1. Items and labels unchanged.
Predictions: (1) C files R1 in at most 2 of 5; if 3 or more, the anchor does not reproduce the real round's miss and
A's effect on it is not measured. (2) A files R1 in at least 4 of 5, noisy in no more runs than C.
