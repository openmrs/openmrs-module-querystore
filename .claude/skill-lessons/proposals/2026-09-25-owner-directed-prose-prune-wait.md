# 2026-09-25 fourth pass — owner-directed: the wait rule, a stale-prose lever, and a pr-harden prune

Directed by the owner ("can you do all the three?"), picking up items 3-5 of the list given after the
third pass. No new run record.

## Item 4 — the stale-prose lever: bar set BEFORE any measurement (measure-first)

The park, REJECTED.md: "Prose the change made false, found by a fresh agent" is at 17 records, with
REOPEN ON "a mechanical lever (a check a script can run), or a record where such prose reached a merged
head". Candidate lever: a script that, given a change's range, lists
- (A) the lines of PROSE the change ADDED that carry a universal (the words `harden`:489 and
  `pr-harden`'s fixer brief name: any, only, exactly, all, never, the whole, cannot, plus every, none,
  always, nothing); and
- (B) prose lines in the head's tree that mention an identifier the change touched.

**Ground truth:** every instance under that park whose false text lived in the TREE (not a PR body or
an issue) and can be reconstructed from the PR branch history as (base, pre-fix commit, file, false
line). It is assembled by a fresh agent from the records and `pull/<n>/head` history, before the script
is run against it.

**Metric:** an instance is caught when its false line appears in the script's output for
`base..pre-fix`. Report recall over the instances, and the output size (candidate lines) per instance.

**Pass mark, fixed now:** recall ≥ 50% AND median output ≤ 60 candidate lines. Clearing it earns one
harden Phase 1 step, run through the refuter. Failing it parks the lever with the numbers, and nothing
is shipped. Calibration before any figure is quoted: a planted known-good case must be caught, and a
planted known-bad (unrelated) line must not be listed.

## Item 4b — the size-budget pre-check (chartsearchai `ProjectInstructionsGuardTest`)

The park is at 7 records, 1-2 builds each, with REOPEN ON "a skill-side lever, e.g. a budget check the
skills could run before the build". Measure before deciding: the time of the guard run alone against a
root build's test phase, and how often a pipeline change touches an instruction file. Ship a pre-check
only if the expected time saved is positive on those figures.

### Item 4b, measured 2026-09-25 — FAILS its bar, parked

- **Guard alone:** `mvn -o -q -pl api test -Dtest=ProjectInstructionsGuardTest` took 39 s cold and 20 s
  warm (9 tests, 4.1 s of them the tests). Measured in a scratch worktree of chartsearchai at `69f7b5ee`
  with no install, since removed.
- **A root `clean install`:** ~105 s per build (`2026-09-17-pipeline-wall-clock-measurement.md`).
- **Frequency:** 134 chartsearchai PRs merged 2026-08-20 to 2026-09-25, 80 of which touched a
  `CLAUDE.md` (`gh pr list --state merged --json files`; no file list at the 100-entry cap).
- **Arithmetic:** a pre-check on each such PR costs ≥ 80 × 20-39 s ≈ 27-52 min, and more wherever
  several cycles edit an instruction file. The park's 7 records × 1-2 builds × ~105 s ≈ 12-25 min is
  the most it could save. Negative on these figures.
- **REOPEN ON:** a guard that runs without compiling the module, or a record where the overflow cost a
  round rather than a build.

### Item 4a, measured 2026-09-25 — FAILS its bar, parked

- **The lister** (`artifacts/2026-09-25-false-prose-ground-truth/prose-candidates.py`) was built test
  first: its `--selftest` went 5/4 → 9/0.
  - Before any ground-truth run, list B was narrowed from "every identifier on a changed line" to "a
    SUBJECT of the change": the enclosing method and class, a name a changed line declares, or a key
    in one of its string literals. The reason was output size on #524's squash, 1119 → 138.
  - B also lists only UNCHANGED prose, and the over-common cap is 10.
- **The ground truth** (`instances.jsonl`, `unreconstructed.md`, `build.py`, `verify.sh`) was built by a
  fresh agent, with the checker calibrated on three corrupted rows. It holds 102 instances: 59
  `source: record` (the park's own) and 43 `transcript`, over 14 PRs. 46 candidates were
  unreconstructable, 23 of them squashed into the change's first commit (#433, #454, #458, #514-1,
  #505/PR529, #515).
- **Primary result** (the pre-registered set, `source: record`; `eval_prose2.py`, which drives the real
  lister):
  - **31% recall, 18 of 59**: ADDED 18/35, OLD 0/24.
  - Median output 49 lines per change (min 3, max 238), over 19 changes.
  - Calibration: the lister lists a median 0.06% of the tree's prose lines, and the prose lines 40 above
    and below each false line are listed 2 times in 58 (3%). So its hits are not chance.
  - **The size bar is met, and the recall bar (≥ 50%) is not.**
- **Secondary** (same metric):
  - all 102 instances: 32%;
  - record set excluding `borderline` and `false_before_change`: 42%, 16 of 38.
- **Exploratory, not the verdict** (`eval_prose3.py`):
  - ±2 lines: 39%, ADDED 22/35, which is 63%.
  - The broad B the narrowing replaced still catches **0/24 OLD**, at a median of 87 lines.
- **What it teaches:**
  - The universals prong (A) catches about half of the false sentences a change ADDED, at a size a
    sweep can read.
  - OLD prose that a change made false is not reachable by identifier mention, in either design.
    That is `harden`:497's *a home that names the claim's key nowhere*, measured.
- **REOPEN ON:** a universals-only lister under a fresh bar registered before it is measured, on ADDED
  instances this set does not contain (new records). The OLD class needs a different kind of question,
  per `harden`:497's subject enumeration.

**Correction to the 4a block above:** its "±2 lines: 39%, ADDED 22/35" is 23 of 59 overall, so OLD
caught 1 of 24.

## Item 3 — the owner's `~/.claude/CLAUDE.md` wait rule gets a pipeline exception

- **Bar (a):** the Stop gate refused mid-run yields on a background build or a `Monitor` on #479, #489,
  #498 and #505 (REJECTED.md, 2026-09-24 first window, P2), and on #429 and #273 after pr-harden 0.30.0
  (eighth and ninth windows).
- **Reported to the owner, not edited, twice:** REJECTED.md:3951 and :4278. The owner directed it now.
- **Staged:** an exception clause to *Wait on a condition, never on a clock*. The gate's revision is
  below.

## Item 5 — pr-harden prune (0.33.1 → 0.33.2)

- **Drafted** by a fresh agent, as 25 deletions with verbatim anchors and verbatim surviving-home
  quotes: `artifacts/2026-09-25-pr-harden-prune-candidates.json`.
  - Its checker was calibrated on a one-word-changed quote and a one-line-off quote.
  - It rejected 29 passages, with reasons.
- **Applied** by script. Every deletion was a whitespace-normalised unique match (blockquote-aware
  after P11 failed to match), and every in-file surviving-home quote was re-checked afterwards.
- **Result:** 1315 → 1250 lines, with five seams re-wrapped locally. Also added:
  - pr-harden:82-83 now lists `verified_shas` among what `pr-set` drops (`gate-state`:400-409);
  - `pr-harden-gate.sh`'s comment "nobody here can advance its run" is deleted in both copies, since
    the gate's own header retracts it.
- **Checks:** `gate-test.sh` 36/0 on both copies; linter 10 files, 0 findings.

## The gate's reply (fresh read-only agent, over the staged diff)

- **Prune → APPLY, all 25.**
  - Every `preserved` quote is present in its target. The check was calibrated to find kept text and
    to miss deleted text.
  - No sibling skill points at a deleted passage by name.
  - Three of the deleted Anti-patterns bullets had drifted from the body: P16 "Only the declined ledger
    crosses rounds", false against the verifier-report bullet; P19, unscoped against docs-only PRs;
    P23, a stale yield rationale.
  - REJECTED.md has no ruling on Anti-patterns sections.
  - **P16 owes a ledger re-point:** REJECTED.md:3695's park names the deleted bullet as its nearest
    rule, and that is now pr-harden:251-255.
- **Gate comment → APPLY.** Non-blocking: the identical comment was at `harden-cycle-gate.sh`:187 in all
  four copies, against that gate's header at :157-158. Fixed in this pass (47/0 on both repo copies),
  with harden bumped to 0.42.1.
- **`verified_shas` → APPLY.** The sentence had been stale since `ea574f9` added the drop, the same day
  `3c83ce3` wrote it.
- **Wait-rule clause → REVISE, BLOCKING.** "An unattended `claude -p` run ends with its turn" is false.
  - `…20260826T122937Z-…-310.jsonl` has 8 `system init` records (lines 1, 414, 1509, 1548, 1919, 2012,
    2622, 2909) in one process, each re-init following a notification after a turn end. Its `.err`
    reads "Background tasks still running after 600s; terminating. Set
    CLAUDE_CODE_PRINT_BG_WAIT_CEILING_MS=0 to wait indefinitely." Both re-measured here.
  - The true half (the gate refuses such yields) stands, per the #429 and #273 driver logs.
  - Shipped: the gate's text, with the false half deleted. The pointer resolves at pr-harden:1071.
- **Recorded for a later pass, outside this diff:** the same false mechanism is stated in pr-harden:973-974,
  resolve-ticket:273, both gates' comments (`pr-harden-gate.sh`:42-43, :229, :277, :285;
  `harden-cycle-gate.sh`:36-37, :196, :238, :245, two copies each), ticket-pool:390, `pool-run`:3328 and
  :3397, and the 2026-08-26 record.
  - The gates' blocking behaviour is right. The stated reason is wrong: a `-p` run waits on background
    work, up to a ceiling (600 s in #310), and resumes.
  - The current harness's ceiling is unverified.
- **Observed:** "a foreground `sleep` is refused" (the owner's `CLAUDE.md`) is not universal. The gate ran
  `sleep 3`, `sleep 12` and until-loops. It is the owner's sentence, so it is noted, not edited.
