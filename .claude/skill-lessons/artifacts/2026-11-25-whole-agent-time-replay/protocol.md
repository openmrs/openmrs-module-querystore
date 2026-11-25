# measurement · does a 25-minute whole-PR blind agent find what a 75-minute one does · 2026-11-25

Pre-registered before any run. Owner-directed: after pr-review 0.40.0 gave a whole-PR agent without per-criterion agents a
30-minute deadline and a 25-minute stop line, the owner asked "how do we tell Whether a 25-minute agent finds what a
75-minute one does?", and was told the replay below. Once the machine was free, they said "71 has finished".

## Subjects

Each is a frozen head whose defects are on record, and a whole-PR brief that already ran on it. Every brief is the
original, with only its stop line changed.

- **S1: stockmanagement #86 at bc1b59a** (round 2's head), with the round-2 unanchored reviewer's brief.
  - **Repo:** the local stockmanagement checkout, `--ref bc1b59a`.
  - **Original run:** 25 minutes.
  - **K86:** `validate_shouldRejectASourceWithoutARequiredLocationTag` tests only a location with no tags. So a check
    weakened to accept any tagged location (`hasAnyTag` to `!location.getTags().isEmpty()`) passes the suite. It
    needs a location carrying a different tag. Source: `2026-11-24-pr-review-openmrs-module-stockmanagement-86.md`.
- **S2: stockmanagement #71 at 3be1c47** (round 7's head), with the round-7 reviewer's brief.
  - **Repo:** the same checkout, `--ref 3be1c47`.
  - **Original run:** 25 minutes.
  - **Keys**, from round 7 of `2026-11-23-pr-review-openmrs-module-stockmanagement-71.md`:
    - **K71a:** role inheritance in the access path is untested. `getPrivilegeScopes` and `getRecordPrivilegeFilters`
      use `getAllRoles()` and `getAllParentRoles()`, and every fixture role holds its privileges directly.
    - **K71b:** the balance query's per-item match and its `sb.expiration is null` half are untested. The fixture has
      one item and only dated batches.
    - **K71c (B4):** dispatching a SUBMITTED operation requires APPROVE, and no test pins it. The two-role test uses a
      receipt, which cannot be dispatched.
- **S3: a private-ticket PR**, with its whole-PR brief. Its original run was 75 minutes, and it found 2 of its 5
  recorded defects. Its repo, head, brief and keys are under `confidential/`.

## Arms

- **A25:** `--deadline 1800`, and the brief says "Stop after about 25 minutes".
- **A75:** `--deadline 5400`, and the brief says "Stop after about 75 minutes".
- Nothing else differs. Same brief text, repo, commit and `blind-run`, and the same environment:
  - `HTTP(S)_PROXY`, their lower-case forms and `JAVA_TOOL_OPTIONS` proxies pointing at 127.0.0.1:9;
  - `NO_PROXY` set to the Anthropic and claude.ai hosts, localhost and 127.0.0.1;
  - `GH_TOKEN` and `GITHUB_TOKEN` invalid.

## Runs

- Three per arm per subject, 18 in all, run in three waves.
- Each wave runs one A25 and one A75 per subject at the same time, so the two arms face the same machine load.
- The first wave starts once another session's two review agents have finished.

## Judging

- One blind judge per subject, through `bin/blind-run`, after its six runs.
- **What it gets:**
  - the subject's keys;
  - the six reports, plus two calibration reports: the original report on that subject, known to carry its keys, and
    a report from another PR, known to carry none;
  - all eight relabelled R1 to R8 at random, so the arm and the calibration are hidden.
- **What it marks,** for each report and key:
  - **FOUND:** names the defect and backs it with a run, a mutation result or file:line;
  - **NAMED:** names it without backing;
  - **ABSENT:** does not name it.
- **Void if** the judge marks the known-good report ABSENT on any key it carries, or the known-bad report anything but
  ABSENT. That subject is then judged again.

## Decision

- **Switch to 75:** if, on any subject, some key is FOUND in at least 2 of 3 A75 runs and in 0 of 3 A25 runs, the
  whole-PR agent without per-criterion agents (pr-review `blind-agents.md`) moves to the 75-minute setup.
- **Otherwise** it stays at 25.
- **Reported as well, not deciding:** FOUND and NAMED counts per arm, each run's cost and minutes, and whether it
  reached its stop line or deadline.

## Threats

- One machine, three subjects, n=3.
- S2 and S3 are PRs whose tickets list acceptance criteria. Under 0.40.0 their whole-PR agent would already run at
  75, but the measurement is of the agent's time budget, which the subject's ticket does not change.
- `origin/main` and `origin/master` have moved since the original runs, and the commits under review have not.

## Calibration reports and their expected labels (fixed before any run)

- **S1:**
  - known-good: `2026-11-24-pr86-r2-unanchored`'s report, which carries K86 (its finding 1: the any-tag mutant survives,
    so give the location a different tag);
  - known-bad: `2026-11-23-pr71-r5-review`'s report, which never mentions a tag.
- **S2:**
  - known-good: `2026-11-24-pr71-r7-review`'s report, which carries K71c (its finding 1) and K71a (its finding 3);
  - it does not carry K71b. Its finding 4 is a different balance gap, the batching by 100, and K71b was found by that
    round's own enumeration;
  - known-bad: `2026-11-24-pr86-r1-unanchored`'s report.
- **S3:** known-good: its original report, which carries K1 and K4; known-bad: `2026-11-24-pr86-r1-unanchored`'s report.
- **The void rule reads these:** a known-good report marked ABSENT on a key listed here as carried, or a known-bad report
  marked anything but ABSENT, voids that subject's judging.

## Run log

- **00:41:** wave 1 started, after the other session's two agents finished.
- **00:50:** load averaged 223 on 10 cores. At 01:08 the machine ran 23 blind agents from 8 other reviews' sessions, each
  with a whole-PR agent plus criteria agents.
- **About 01:06:** the replay was stopped, to free the machine for those reviews, and because a time-limited agent
  under that load measures the load, not the time budget.
- **What finished:** wave 1's three 25-minute runs, in 11, 12.5 and 22 minutes. One of them listed every end-to-end
  server run as not run.
- **What was stopped:** the three 75-minute runs, which left partial reports. Waves 2 and 3 never started.
- **Nothing here counts.** The replay reruns from wave 1 on a machine without other review agents running.
- **03:04, the rerun.** No blind agents ran and the load was 3.7, so the replay restarted from wave 1. Its runs are
  named `dlr2-…`, and the stopped runs' folders stay as they were.
- **A gate before each wave** waits, for at most 2 hours, until no other session's blind agent runs and the 1-minute
  load is under 20.
- **The load is sampled every minute** for the threats. Nothing else in the protocol changes.
- **04:28, all 18 runs ended,** each with exit 0 and a report. None was stopped by its deadline.
- **04:30, before judging.** Six run reports and one calibration report named a run's own folder, a folder the agent
  made, or its stop line ("about 75 minutes", "the 25-minute limit"), any of which tells the judge the arm or the
  calibration. The judge briefs mask these as `<run folder>`, `<folder>` and `<N>`; the reports themselves are unchanged.
  "Out of time" stays, since a run of either arm can write it.
- **04:33, judging.** Three blind judges, one per subject, through `bin/blind-run`: one turn and $0.19 to $0.25 each.
  `score.py` maps their marks back and applies the rules above. It was first checked on four made-up mark sets, with
  answers known in advance: switch, stay, void, and a missing mark. It gave each the expected answer.

## Result

**Decision: stay at 25.** On no subject was a key FOUND in 2 or more of 3 A75 runs and in none of 3 A25 runs.
pr-review's `blind-agents.md` keeps `--deadline 1800` and the 25-minute stop line for a whole-PR agent without
per-criterion agents.

- **Calibration passed on all three subjects.**
  - Every known-good report was marked FOUND or NAMED on each key it carries.
  - Every known-bad report was marked ABSENT on every key.
- **FOUND, by key,** for A25 and then A75, of 3 runs each. No run was marked NAMED.
  - **S1:** K86 0 and 0. The original run found it, and none of the six replays did.
  - **S2:** K71a 3 and 2, K71b 0 and 1, K71c 1 and 2. One A75 run found all three keys.
  - **S3:** two A25 runs each found one key, each a different one. No A75 run found any. The counts by key are under
    `confidential/`.
  - **Overall:** 6 FOUND marks for A25 and 5 for A75, of 27 each.
- **The cells closest to the switch, rechecked against the reports themselves:**
  - **S2's K71c:** 2 of 3 for A75, 1 of 3 for A25. That A25 report gives the defect as its finding 1, with a mutation
    result: `StockManagementServiceImpl.java:1485`, APPROVE changed to MUTATE, and both classes stay green.
  - **S2's K71b:** at 1 of 3 for A75, it would need a second A75 run. The other two A75 reports test a different gap in
    the same query, a batch expiring today.
  - **S1's K86:** none of the six reports mutates the tag check to accept any tagged location.
  - **S3:** none of the three A75 reports names the keys no A25 run found.
- **Cost and time.** Times are wall minutes from the brief to the report.
  - **A25:** $14.06 for 9 runs, $0.97 to $2.26 each, 4.8 to 15.9 minutes, median 11.8.
  - **A75:** $26.97 for 9 runs, $2.06 to $4.21 each, 7.1 to 29.0 minutes, median 21.4.
  - **No run reached its stop line or its deadline.** Each one stopped on its own.
    - So the arms differ in what the brief says, and in how long the agents then chose to work. They do not differ in a
      limit that cut a run short.
    - One A25 report says it "stopped at the 25-minute limit" after 12.6 minutes.
- **Load.**
  - Wave 1 ran with no other agent, at a 1-minute load of at most 27.5.
  - Waves 2 and 3 overlapped another session's agents, in 21 of 27 and in 23 of 31 minute samples. Their loads reached
    37.1 and 20.0.
  - Each wave paired the two arms, so both faced the same load.
- **Threats, beyond those above:**
  - With n = 3, how often a key is found varies widely from run to run. K86 was found in 1 of 7 runs, counting the
    original.
  - One judge per subject.
  - The known-bad reports come from another PR, so a judge can tell them apart by their content.
  - The judge called two marks close: one A75 run's K71b, on its null-expiry half only, and one A25 run's S3 key,
    reached from a different trigger. Either mark going the other way leaves the decision unchanged.

## Exploratory, after the result: defects outside the keys

The keys are 9 known defects, and a run can find others. Reading the S3 reports for the result above, I came across one
latent hazard outside S3's keys, which two A75 runs measured and no A25 run did. So, before any judging of it, one more
blind judge per subject:
- **What it gets:** only the six run reports, masked as above and relabelled U1 to U6 at random.
- **What it does:** it lists every distinct defect any of them claims, and marks each report FOUND, NAMED or ABSENT on
  each, by the same definitions.
- **What it decides:** nothing. It is reported beside the result, and any change it argues for is the owner's to direct.

### Exploratory result (04:40)

The three judges took one turn each, $0.23 to $0.39. `union_score.py` was first checked on made-up marks with known
answers, including one with a missing mark. The judges checked that each claim was backed, not that it was true.
- **Distinct defects claimed by any run:** 12 on S1, 34 on S2, 18 on S3, so 64 in all.
- **Defects FOUND by at least one run:**
  - A25: 36 (5, 20, 11);
  - A75: 42 (8, 23, 11).
- **Defects claimed, FOUND or NAMED, by at least one run:**
  - A25: 45;
  - A75: 46.
- **FOUND marks:**
  - A25: 51, or 5.7 a run and $0.28 a mark;
  - A75: 65, or 7.2 a run and $0.41 a mark.
- **Defects FOUND by one arm and claimed by no run of the other:** 14 for A25 and 16 for A75.
- **The switch rule's pattern applied to every claim** (FOUND in at least 2 of 3 runs of one arm, and in none of the
  other's):
  - **A75, 3 claims:**
    - S2, no test of a location without a stock party (no privilege granted, yet the user is listed);
    - two on S3: one pre-existing, and one latent hazard the PR left by decision.
  - **A25, 1 claim:** S2, the scope start-date boundary (`activeFrom` equal to today) is not pinned.
- **Reading:** on the 9 known defects, A75 found nothing A25 missed. Across all claims, A75 backed about a quarter more,
  at 1.9 times the cost. Whether that breadth is worth the cost for rounds without per-criterion agents is the owner's
  call. The decision above stands until the owner directs otherwise.

### Spot check (2026-11-25, after the exploratory result)

- One FOUND claim, from an A25 run, was false. The test it called missing existed at the reviewed commit, and removing
  the check that test guards turns it red. FOUND counts therefore overstate true findings by an unknown amount, in
  either arm.
