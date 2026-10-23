# measurement · harden Phase 1's confirming pass against Phase 2 escalation · 57 public runs · 2026-10-23
outcome: measurement only. A re-review after a cycle's last fix was common, and the Phase 2 after it escalated about as often as after none. No proposal drafted.
protocol: not pre-registered. The hypothesis came first, and the method changed twice when a calibration case contradicted it; both failed methods are kept below.
artifacts: `.claude/skill-lessons/artifacts/2026-10-23-harden-phase1-confirming-pass/` holds the scripts, the input list, each stage's output and the hand classification. `scripts/rederive.sh` rebuilds every stage from this machine's transcripts, compares each with the copy kept there, and prints the table below.
transcript: not named; every figure re-derives from the artifacts

This is capture, not derivation. It tests one remedy for the parked "Phase 2 escalating repeatedly on
the run's own fixes" (`REJECTED.md`, 31 records at :5378). That entry reopens only on "a proposal that
names what would have stopped the chain earlier without losing a real defect each pass found"
(:3998-4000).

## Hypothesis

Suppose Phase 1 ran a confirming review after its last substantive fix, before writing `converged`.
That is harden's "a pass that itself found a substantive (non-cosmetic) issue cannot be the last pass".
Then the Phase 2 that follows would escalate less often, and a run would need fewer cycles. No rule
would be added; an existing one would be enforced.

## Population

- **Records.** Public run records only: the top-level `*.md`, never `confidential/`, whose
  `transcript:` is still on disk. That gives 66 transcripts. 58 load harden, and 57 record gate-state
  traversals: 55 chartsearchai runs, 1 querystore and 1 openmrs-core, dated 2026-09-23 to 2026-10-11,
  most of them 2026-09-23 to 2026-09-29.
- **Traversals.** A traversal runs from a `phase1 open` write (or the harden load) to the next
  `phase1 converged` write. There are 202:
  - 56 are terminal (`phase2 done`);
  - 12 have no recoverable outcome;
  - 7 start at a converged write with no open write;
  - the other 127 are classified: 57 first traversals, and 70 resumed after an escalation.
- **Outcome.** A traversal is ESCALATED when a later `phase1 open` for a higher cycle comes before any
  `phase2 done`. Checked against chartsearchai-514's record (`cycles: 6`): the script finds 6 cycles,
  5 of them escalated.

## Method

1. `phase1-traversals.py` splits each run at its gate writes. It finds each traversal's last edit since
   the previous converged write.
2. `post-edit-digest.py` prints the orchestrator's prose and every tool call's description between that
   edit and the converged write.
3. Each of the 127 blocks was classified by hand in `post-edit-classes.txt`, against the rubric at its
   top:
   - **R**: an evidenced re-review. A tool call after the last substantive edit re-reads the diff, a
     file or prose, sweeps for false or stale claims or other homes, or traces outward. Also counted:
     such a review whose only later edits were cosmetic.
   - **Rc**: a re-review the prose claims, with no tool call that shows it.
   - **M**: mechanical checks only: build, tests, mutation probes, the orphaned-javadoc detector,
     runtime probes.
   - **N**: nothing; or only a check of that one edit; or an edit stated right before `converged`.
   - **0**: no edit in the traversal.
   - **U**: the window cannot tell.

## Measured

| traversal | re-review (R+Rc) | then escalated | mechanical or nothing (M+N) | then escalated |
|---|---|---|---|---|
| first | 24 | 17 (71%) | 21 | 14 (67%) |
| resumed after an escalation | 33 | 15 (45%) | 26 | 14 (54%) |

The same rates for the narrower classes:

| class | first: escalated | resumed: escalated |
|---|---|---|
| evidenced re-review only (R) | 13 of 18 (72%) | 11 of 23 (48%) |
| nothing after the last edit (N) | 6 of 11 (55%) | 5 of 8 (62%) |
| no edit at all (0) | 4 of 8 | none |

15 blocks are unclassifiable (U).

## Reading

- **The confirming pass is not rare.** 57 of the 104 classified traversals that had an edit ran one,
  41 of them with a tool call that shows it.
- **It made no visible difference.** Where it ran, the next Phase 2 escalated about as often as where it
  did not: 71% against 67% after a first traversal, and 45% against 54% after a resumed one. At these
  sizes neither gap is distinguishable from noise.
- **A thorough Phase 1 still escalated.** chartsearchai-514's first traversal recorded ten passes, the
  last a re-read ("Pass 10: the last two homes not re-read since editing"). Its Phase 2 still
  escalated, and did so five times in a row.
- **Conclusion.** In this population, enforcing the confirming pass shows no way to stop an escalation
  chain. That is evidence against this remedy, and the parked entry stays parked.

## What failed first, and why it is kept

- **Counting recorded passes** (`traversal-summary.py`) counted `phase1 open` writes between the
  escalation and `converged`. That measures writes, not review. In chartsearchai-469 cycle 3 the extra
  "pass" is an open write followed by `converged` 14 s later, with nothing between.
- **Counting source reads after the last edit** (`post-edit-summary.py`) reported at most two reads in
  63 of 64 resumed traversals, and read that as "no re-review". The hand classification contradicts it:
  33 of the 70 resumed traversals re-reviewed. It failed two ways:
  - A sweep is often a single grep. chartsearchai-482 cycle 3 re-reviewed with one counted read.
  - It filed reads issued as `cd <dir>; grep …` as builds. chartsearchai-471 cycle 2 ran three such
    sweeps and scored 0.
- **Searching for the transition sentences** measures nothing. Harden's three Phase 1 sentences never
  appear filled in, in any of the 66 transcripts. Only the template text from each skill load does.

## Limits

- **Observational.** A harder change draws both more re-reading and more escalation. So this cannot show
  that a re-review has no effect, only that none is visible here.
- **One unblinded reader.** One reader classified every block, the same reader who proposed the
  hypothesis, and each block's header showed its outcome. A blind re-classification by a fresh agent
  would remove that.
- **Missed edits.** The edit extractor misses Bash edits whose description names none, so a window can
  start before the true last edit. The reader classified from the prose in that case, and U holds what
  the prose did not settle.
- **Narrow sample.** 55 of the 57 runs are chartsearchai, from about one week.

## Addendum, later on 2026-10-23: the runs whose transcripts had moved

The population above came from `public-transcripts.py`, which keeps a record's transcript only at its
recorded path. Five records named a path their transcript had since been moved away from
(`../2026-10-23-retro-window/transcript-presence.out`).

- Three of them load harden: #527, #562 and #564.
- This addendum adds their 8 traversals, 3 first and 5 resumed, with the note's own scripts. The work
  is in `artifacts/2026-10-23-harden-phase1-confirming-pass/relocated-addendum/`, and its README gives
  every command.

**Blind classification.** A fresh agent classified the 8 new blocks against the rubric above.
- Each block had its record and its outcome removed, and was shuffled among 12 of the original blocks
  as calibration: 2 per class, chosen with a fixed seed.
- On the calibration blocks it agreed with this note's reader on 10 of 12 exactly, and on 11 of 12 by
  the groups the tables use (`score-blind.out`). The differences were one N read as 0 and one R read
  as Rc.

**Rates over all 135 classified traversals** (`combined-rates.out`):

| traversal | re-review (R+Rc) | then escalated | mechanical or nothing (M+N) | then escalated |
|---|---|---|---|---|
| first | 25 | 18 (72%) | 23 | 15 (65%) |
| resumed after an escalation | 38 | 18 (47%) | 26 | 14 (54%) |

**The reading stands.** At these sizes neither gap is distinguishable from noise. Enforcing the
confirming pass still shows no way to stop an escalation chain.

**Limits.** Only the 8 new blocks were classified blind; the original 127 are still one unblinded
reader's. The calibration covers 12 blocks.
