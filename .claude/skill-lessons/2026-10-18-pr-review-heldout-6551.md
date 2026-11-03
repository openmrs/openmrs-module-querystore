# measurement · pr-review held-out test of two candidates · openmrs-core PR 6551 · 2026-10-18
outcome: measurement only — neither candidate produced its pre-registered signal on the held-out PR; no proposal drafted
protocol: pre-registered before any arm ran, no amendments; follows `2026-10-18-pr-review-lens-replay-queue96.md`
artifacts: `.claude/skill-lessons/artifacts/2026-10-18-heldout-6551/` — protocol with results, both private keys, all briefs, the techniques file, snapshots, every run's findings, the blind adjudication and the scripts; `python3 scripts/score.py <that dir>` re-derives the table below; `transcripts.txt` maps each run to its persisted session transcript
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-queue/5743c726-b776-450f-a755-a07953eaf17e.jsonl

This is capture, not derivation. The queue PR 96 replay left two candidates standing, and this tests
both on a PR neither was written from.

## Design

- Subject: openmrs-core PR 6551 at `b7cd909d`, base `28e3bab9`, 8 files +216/-6, the 2026-09-11 A/B's
  pin, so that A/B's blind adjudication (13 defects, L01-L13) is the answer key. The PR is still open
  and its head has moved to `f1a653dd`; each clone holds only `b7cd909d`'s ancestry. PR conversation
  withheld, as in the A/B; description and TRUNK-6669 snapshotted as of 2026-09-11.
- Arms, n=3, three waves of one run per arm in random slot order: **C** pr-review 0.19.0; **M** C plus the
  per-condition mutation sentence (the one that found a real extra defect on PR 96); **K** C plus
  `openmrs-verification.md` beside the skill and a Step 3 pointer to it. The file holds every
  verification lesson from the queue work, each citing its queue source (flush to reach the database,
  `NOT_SUPPORTED` to see a transaction, test authorization as a non-admin user, DAO paths skip service
  advice, handler-order ties, rebuild after mutation, liquibase not parsed, `datetime(0)` stamps). Its
  author had read 6551's findings, so it includes every lesson rather than a selection.
- Headless runs from per-run clones, private Maven repositories, canary repeated (NONE). Tool-call
  audit extended to `~/Projects`, `~/.claude` (the A/B's key lives in `skill-lessons`) and other arms'
  skill folders, calibrated on 6 planted forbidden and 4 allowed calls: 0 flagged across 9 runs and
  the adjudicator.

## Measured

| arm | L01 | L02 | L03 | L04 | L04 blocking | L06/L08 | L10 | L12 | other REAL |
|---|---|---|---|---|---|---|---|---|---|
| C | 3/3 | 0/3 | 3/3 | 1/3 | 0/3 | 0/3 | 3/3 | 0/3 | 1 |
| M | 3/3 | 2/3 | 3/3 | 3/3 | 0/3 | 0/3 | 1/3 | 0/3 | 1 |
| K | 3/3 | 1/3 | 3/3 | 2/3 | 0/3 | 0/3 | 1/3 | 2/3 | 3 |

40 findings, 0 NOISE; the 5 unmatched (three defects: the native delete clears the whole second-level
cache, all `getAllowedLocations` tests pass with `User.locations` unmapped, Envers records no removal)
are all REAL. **No pre-registered signal:** K vs C on L04 filed blocking 0 vs 0; M vs C on L06/L08 0 vs
0. The per-condition mutation finding this PR did yield (the unmapped collection) came from C and K,
not M. Two-run differences elsewhere sit among ~30 comparisons at n=3 and are read as noise. Cost:
$15.22 for 9 runs, mean $1.69; adjudicator 257,790 tokens, 21.0 min.

## What the transcripts show (exploratory)

- **The privilege hole (L04) is under-called by framing, not by missing technique.** All 6 L04
  findings were filed as questions. wt-02 (K) ran the exploit with a positive control: as a non-admin
  user holding only Get Locations, `getUser(1)` was refused and `getAllowedLocations(new User(1), tag)`
  returned user 1's list. It then asked whether the exposure was intended. The 2026-09-11 A/B's
  control filed it as a suggestion; its security lens and both blind adjudicators call it blocking.
  `pr-review` tells reviewers to frame "uncertainty about an observed side effect" as a question, and
  says nothing about a demonstrated authorization bypass.
- **The techniques file was read and mostly not applied:** read in 3 of 3 K runs; non-admin probe run
  in 1; `NOT_SUPPORTED` in 0. Every run in every arm flushed, which this PR's own vacuous test invites.
- **One blocker is found only some of the time.** L02 (the cascade to child locations) was found by 3
  of 9 runs, 0 of 3 controls. The A/B's single control run found it. Its conclusion that fan-out found
  "no blocker the single context missed" therefore rests partly on a draw that comes up about one time
  in three. On PR 96, the privilege blocker K1 had the same character (8 of 12 runs).

## Refuted — claims this run's own orchestrator made and the run broke

- **"Give reviewers the technique, not another instruction."** The pre-registered outcome did not
  move, and the one run that applied the technique still under-called the defect it proved.
- **"The per-condition mutation sentence is the one change with a positive signal."** Not reproduced:
  0 of its targets here, and the mutation-shaped finding came from the other two arms.
- **"L04's under-call in the A/B was a verification-depth effect."** The 2026-09-11 record's reading
  (the lens "ran the exploit"). Here a run ran the same exploit and still filed a question, so depth is
  not sufficient; the disposition rule is the remaining suspect, untested.

## Limits

- n=3 per arm, one PR, answer key from one prior adjudication. C at skill 0.19.0 differs from the
  A/B's arm A (0.17.x), so the A/B is context, not a fourth arm.
- The per-run detection rates for L02 and K1 come from 9 and 12 runs of slightly different skill
  copies, pooled; the arms are not identical.
- Not tested: whether a disposition rule ("a privilege bypass you have demonstrated is blocking") or a
  union of independent single-context runs changes the outcome. Both are what this data points at.
