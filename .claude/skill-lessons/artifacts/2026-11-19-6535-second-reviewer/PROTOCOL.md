# Does a second, unanchored reviewer make pr-review file what ibacher raised on openmrs-core PR 6535?

Pre-registered 2026-11-19, before any arm ran. Owner-directed: on 2026-11-19 the owner approved running "a replay
on the frozen #6535 head, with and without a second reviewer" before the second-reviewer lesson goes into the skill.

**Why this question.** Over 722 pr-review sessions, a second agent briefed without the review's findings was
credited by the session with something the review missed in 12 of 71 sessions that ran one. It ran in 54 of 59
openmrs-core-folder sessions from 2026-11-05 and 8 of 71 elsewhere, because the lesson lives in that folder's
memory. The ledger parked "a union of two independent single-context reviewers" (REJECTED.md, the 2026-10-23
section, 556 arm U: 1 of 3 pairs against 0 of 3, below its bar), to reopen on a pre-registered measurement where a
union clears its bar. This is that measurement, on a different subject, with the shape the field used: one review
that starts its own unanchored subagent.

## Subject

Identical to the P2 replay of `~/.claude/skill-lessons/artifacts/2026-11-18-6535-replay-p2-p3/PROTOCOL.md`: PR 6535
at `86b96c7d6f8d4fb20d96c8c9d888cfcb01567a3b`, base `master` `a93b6293c9`, frozen at 2026-11-18 17:00 UTC, before
ibacher's review 5961392681. The same snapshots (PR body, TRUNK-6682, the conversation filtered to before the
freeze), copied unchanged. Per-run clones come from a bare copy of that replay's frozen repository. Each of the
six slots has two clones at the head (`wt-0N` and `wt-0N-sub`, 21 files, +967/-68, checked), each with its own
private Maven repository (`~/.m2` as a read-only tail), application data directory and log4j2 config.

## Arms (n=3 each, one wave)

- **C:** `pr-review` 0.27.0 as live (SKILL.md and posting.md, copied 2026-11-19 13:09 before any edit).
- **U:** C plus one paragraph, inserted in Step 3 between *Convergence* and *When an adversarial refutation pass is
  worth it* (`skill-diff-C-to-U.diff`, 92 words):
  > **A second reviewer, for what you did not find.** Before you settle your findings, where you can start a
  > subagent, start one fresh agent and give it only the PR, the problem it claims to solve, the repository and how
  > to build and test there: not your findings, your notes or your verdict. Ask it for defects it can prove by
  > running code, PR against base. Reproduce what it reports before you file any of it. The refutation pass below
  > tests what you found; this one looks for what you did not.
- Slots are assigned to arms at random (`KEY-private.json`). The six briefs differ only in skill path, working
  copy, subagent copy and output path (checked by diff before launch).
- The brief is the 2026-11-18 P2 brief with three changes, identical across arms: a subagent may be started only
  where the skill says so, given the second clone and the same hard constraints; Step 1's open-PR check is
  treated as having found none (that list cannot be reconstructed for the freeze); and the output records
  `subagents`.
- Each run: headless `claude -p --model claude-opus-5-5` at default effort from its clone, `--strict-mcp-config`,
  `GH_TOKEN`/`GITHUB_TOKEN` invalid, HTTP(S) proxies at a dead port with only the model API exempt. WebFetch,
  WebSearch, Skill, Workflow and every publishing tool are disallowed. **Agent and Task are allowed in both arms**,
  so C is the live skill as it runs today, refutation pass included.
- Canary (`logs/canary.jsonl`, 13:21 UTC): curl, gh and git cannot reach GitHub or JIRA; a subagent starts and
  answers; no project memory loads.
- Answer-bearing paths at mode 000 for the wave (`lock-list.txt`, 41 paths): the 2026-11-18 replay workspace and
  its artifacts, the 6535 records and proposal in `~/.claude/skill-lessons/`, the 2026-11-18 session's scratch
  files, and this session's retro notes. Not locked, because other live sessions read them: the openmrs-core and
  webservices project memories and `REJECTED.md`. Reads of those are caught by the audit instead.

## Answer keys (from the 2026-11-18 PROTOCOL; ibacher's review 5961392681)

- **K-mime:** `TextHandler.setContentMetadata` calls `mimeType.equals("application/octet-stream")` with no null
  check (TextHandler.java:118). Raised by r4617880105.
- **K-stream:** MediaHandler's stream is not guaranteed to be closed if a later call fails (r4617890396).
- **K-order:** reads try the current location first while `getPath()` tries legacy first (r4617856131).
- **K-catch:** the `catch (FileSystemException e)` falls back to the legacy location on any file-system failure,
  `AccessDeniedException` included (r4617836000, r4617840695).
- **Not scored:** K-since (`@since 3.0.0` against the `backport` label), filed by 6 of 6 runs on 2026-11-18.

## Scoring (decided before any output exists)

- **A run finds a key** when one of its `findings` entries, of any disposition, states that key's defect or asks
  for its fix, by the 2026-11-18 definitions: K-mime, a null MIME type reaching that comparison throws, or the
  comparison should be null-safe; K-stream, a stream MediaHandler opens is not closed when a later call fails, or
  should be guaranteed closed; K-order, the read order no longer matches `getPath()`'s order, or asks why it
  changed; K-catch, the catch falls back on or hides a permission failure or another non-missing file-system
  failure. A mention only in `notes`, `what_i_swept`, `could_not_verify`, `verdict` or `subagents` is recorded and
  does not count.
- **Decisive measure:** per run, whether it finds at least one of the four keys.
- **Candidates:** `scripts/score.py` (K-order calibration added before launch) flags candidate findings per key.
  Every candidate is pooled and shuffled with run and arm stripped, read and confirmed or rejected before the
  slot map is opened. A run with no confirmed candidate is a miss.
- **Noise:** every finding of the six runs is pooled and shuffled with run and arm stripped and judged, before the
  map is opened, as valid (true of the head, and a defect or fair question the author can act on in this PR),
  invalid (false of the head, or a failure that cannot happen) or noise (true, but not worth the author's time in
  this PR). U passes when its three runs together file at most 2 more invalid-or-noise findings than C's three.
- **Recorded, not decisive:** whether each U run started an unanchored second reviewer and what it reported;
  whether any C run started a subagent; findings per run; dispositions; verdicts; cost; duration.

## Pre-committed reading (a signal is at least 2 of 3)

- **U 2-3 and C 0-1:** the second reviewer carries the catch. The paragraph goes to a fresh refuter with this
  result, and only if the noise check passes.
- **U 0-1:** it does not carry the catch here; the lesson stays parked with this result added.
- **C 2-3:** the current skill already finds a key at this rate, so no edit follows from this replay.
- **U 2-3 and C 2-3:** no difference attributable to the paragraph; park.
- **Compliance:** if fewer than 2 of 3 U runs actually start an unanchored reviewer, the run measures the
  wording's compliance, not the reviewer's value, and is read as such whatever the counts.

## Threats

- n=3 per arm, one PR, one wording.
- Default effort, where the field runs were often at max effort.
- The second reviewer's brief is written by the run, so its quality varies with the run.
- The answer keys were written by the session that missed them; ibacher's review is one maintainer's call.
- The field sessions' second reviewers loaded openmrs-core's project memory; these load none.

---

# RESULTS — recorded 2026-11-19, before interpretation

**Runs.**
- All six ended `success` in one wave, 10:12-10:26 UTC. U runs took 9.6-13.9 min of wall time and C runs 7.1-8.2.
- Cost: U $11.21 (wt-01 $3.47, wt-03 $3.99, wt-05 $3.75) and C $6.36 (wt-02 $1.93, wt-04 $2.24, wt-06 $2.20).
  The canary cost $0.24 and the blind judge $0.54 (`costs.txt`).
- Locks: 41 paths at mode 000 from 10:12:48, all restored at 10:26:34 to their recorded modes (checked).
- Every working copy ended with only the harness's own untracked `.mvn/maven.config`.
- **Deviations** (`private/deviations.txt`):
  - wt-05 wrote its output to a mistyped path, with a `-` where a `/` belongs; it was copied unchanged into `out/p2/`.
  - wt-05 copied a war out of a local docker image, without starting it, to check the 2.8.x Java level. That is
    outside its working copy, with no network and nothing answer-bearing.
- **Audit** (`audit.txt`; the calibration flagged 9 of 9 planted forbidden calls and 0 of 8 allowed ones):
  - 3 flags each in wt-01 and wt-03, and 2 in wt-05. All sit inside the run's own Agent call: the brief passing its
    hard constraints to the subagent ("do not read anything under /Users/danielkayiwa/Projects or …/.claude", "do
    not … run jetty:run"). Each was read in context.
  - The three subagent transcripts: 0 flags.

**Compliance.**
- **U:** 3 of 3 started one unanchored reviewer, at main tool call 12, 19 and 15 of 35, 47 and 45. Each brief
  (427-438 words) carried the PR, the ticket and build instructions, and none of the run's own findings. "backport"
  appears in each, as the ticket's label.
- **C:** none started a subagent.

**Decisive: U 0 of 3, C 0 of 3.** The mechanical scan flagged 0 candidates for K-mime, K-stream, K-order and K-catch
over all 12 findings. The wider check, the keys' vocabulary over every output field and each subagent's final
report, found:
- **K-order, noticed and not filed:**
  - U 3 of 3 noticed it. wt-01's and wt-03's second reviewers reported it, wt-03's with a probe (key
    `complex_obs/both`, a file in each layout). wt-01's main run set it aside: "Not filed: it needs a file at both
    <appdata>/K and <appdata>/storage/encode(K), which the obs key formats don't produce". wt-05's main run put it
    in `notes` only.
  - C 0 of 3 mentioned it anywhere.
- **K-mime, K-stream, K-catch:** in no output and no subagent report. wt-05's reviewer reported "no resource leaks".

**Recorded.**
- **Findings:** U 7 (3, 2, 2), C 5 (2, 2, 1).
- **Two U findings came from the second reviewer:**
  - wt-01 F3: a storage subclass overriding `getData` is bypassed.
  - wt-03 F2: a missing test for the new `APIException` paths.
- **The judge** (`judge/`; fresh, headless, blind to run and arm, checked each claim against the head): 11 valid,
  0 invalid, 1 noise. The noise is wt-01 F3: "no subclass exists outside the test's own
  CountingLocalStorageService". The other ten form two clusters of near-duplicates across runs, mapped by the
  private map: legacy-layout reads still cost 6 calls (every run but wt-06), and backport against `@since 3.0.0`
  plus Java 16+ syntax (every run but wt-03, which mentions it only in `what_i_swept`). So K-since was filed by 5
  of 6 runs, against 6 of 6 on 2026-11-18.
- **Noise check:** U 1 invalid-or-noise, C 0, within the bound of 2. Not decisive, since U is 0-1.
- **Verdicts:** wt-01 "not ready"; the other five "close to mergeable".

**Against the pre-committed reading: U 0-1. The second reviewer does not carry the catch here; the lesson stays parked
with this result added.**
