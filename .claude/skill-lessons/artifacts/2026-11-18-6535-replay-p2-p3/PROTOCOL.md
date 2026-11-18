# Do the parked P2 and P3 sentences make pr-review catch what ibacher raised on openmrs-core PR 6535?

Pre-registered 2026-11-18, before any arm ran. Owner-directed. After the 2026-11-18 retro parked P2 and
P3, the owner asked why they needed a retro when they were real issues that would help catch what
ibacher identified. Both REOPEN ON conditions the gate set (REJECTED.md, 2026-11-18 section) name a
replay, and this is that replay. Precedent and harness: the 2026-11-05 owner-directed pass and
`artifacts/2026-11-04-same-defect-replay-53/`.

## Subjects

- **P2 replay.** PR 6535 at `86b96c7d6f8d4fb20d96c8c9d888cfcb01567a3b`, base `master` `a93b6293c9`
  (the merge-base; master did not move between the head's push and the freeze). Frozen at
  2026-11-18 17:00 UTC: ibacher's review 5961392681 was submitted 17:08:21Z and its first comment was
  created 17:03:46Z. 21 files, +967/-68. The PR head has not moved since.
- **P3 replay.** PR 6535 at `a2002caeacef930b67179359d1ceae657ba85af5`, `master` `891b80745b`
  (merge-base `7a525792af`). Frozen at 2026-11-04 21:30 UTC: the round's review 5776130559 was submitted
  21:31:49Z. 17 files, +692/-69. Plus that round's six inline comments as the run's own unposted draft
  (`snapshots/p3/draft-review.json`). Draft D2 is r4473604840, the `read()` block with
  `catch (FileSystemException e)`.
- **Snapshots, per replay.**
  - The PR body: last edited 2026-09-29, so the live body is the frozen one.
  - TRUNK-6682 from REST with `summary,description,status,comment,labels,fixVersions`. Its one comment
    is from 2026-09-14.
  - The conversation, filtered to items created before the freeze:
    - P2: 31 inline comments, 11 reviews and 13 PR comments;
    - P3: 19 inline comments, 6 reviews and 9 PR comments.
  - Scrubbed in P3: the current-position fields (`line`, `commit_id`, `start_line`) are dropped, and
    the codecov comment's body, edited 2026-11-17, is replaced by a note. Neither snapshot holds a
    later commit sha (checked for the ten later PR commits).
- **Isolation.**
  - Per-run clones come from a frozen bare repo that holds only the ancestry of the head and the base,
    built from a full-history clone. The local `openmrs-core` checkout is shallow. The refs are
    `master`, `pr-6535` and `pull/6535/head`.
  - Each clone has a private Maven repository with `~/.m2` as a read-only tail. The application data
    directory and the log4j2 config point at scratch.
  - JDK 21, offline. Calibrated in throwaway clones: `mvn -o -B -pl api -am test
    -Dtest='LocalStorageServiceTest,TextHandlerTest,MediaHandlerTest'
    -Dsurefire.failIfNoSpecifiedTests=false` passes at both heads in 43 s (50 and 45 tests). The
    private repositories stayed empty, and nothing under `~/.OpenMRS` changed.

## Arms (n=3 each per replay, all twelve runs in one wave)

- **C**: `pr-review` 0.26.0 as live, SKILL.md and posting.md, 8371 words. It is the control for both
  replays.
- **E-P2** (`skill-diff-C-to-E-p2.diff`): C plus the gate's reopen wording for P2, inserted in Step 3's
  "The same defect, where the PR didn't look" bullet before "An instance that predates the PR":
  > Lines the PR moves or rewrites are its own code, so read them as new code, such as for a null that
  > throws or a stream an error path leaves open: a probe that reports only differences from the base
  > misses a defect both carry.
- **E-P3** (`skill-diff-C-to-E-p3.diff`): C plus the gate's reopen wording for P3, appended to Step 4's
  suggestion-block bullet:
  > A fix you post is a change you propose: before posting, list for yourself each input and exception
  > type it newly accepts or catches, and read earlier threads on the same lines; where a refutation
  > pass runs, give it the code, not only the claim.
- Slots wt-01..wt-06 are assigned to arms at random per replay (`KEY-private.json`). Within a replay,
  the briefs differ only in skill path, working copy and output path (checked by diff).
- The P2 brief runs Steps 1-3 and outputs findings, as on 2026-11-05. The P3 brief runs Steps 1-4,
  because the P3 sentence sits in Step 4, and outputs the review it would post, with each comment's
  `body`.
- Each run is headless `claude -p --model claude-opus-5-5` at default effort, from its clone.
  - Project memory is empty (canary).
  - `GH_TOKEN` and `GITHUB_TOKEN` are invalid, and `--strict-mcp-config` loads no servers.
  - WebFetch, WebSearch, Agent, Task, Skill and every publishing tool are disallowed.
  - HTTP(S) proxies point at a dead port, with only the model API exempt. This applies only if the
    canary shows the CLI still works and that curl, gh and git cannot reach GitHub or JIRA.
  - Answer-bearing files are at mode 000 for the wave. The audit is calibrated, and it flagged 8 of 8
    planted forbidden reads and 0 of 7 allowed ones.

## Answer keys

- **K-mime (P2, decisive).** At the head, `TextHandler.setContentMetadata` runs
  `mimeType.equals("application/octet-stream")` on `metadata.getMimeType()` with no null check
  (TextHandler.java:118). This is master's line from `TextHandler.java:91-92`, moved.
  - Measured 2026-11-18 at 86b96c7d6f: an `ObjectMetadata` built without a MIME type makes it throw
    `NullPointerException`. The control, with a MIME type set, passes.
  - Raised by r4617880105.
- **K-catch (P3, decisive).** D2's `catch (FileSystemException e)` falls back to the legacy location on
  any file-system failure at the current one. `AccessDeniedException` extends `FileSystemException`
  (JDK 21, checked), so with D2 as posted, a permission failure at the current location returns the
  legacy copy whenever one exists. Raised by r4617836000 and r4617840695.
- **Recorded in P2 runs, not decisive:**
  - K-stream: MediaHandler's RAW_VIEW stream is not closed if a later call fails (r4617890396).
  - K-catch, the same catch, by now in the PR's code (r4617840695).
  - K-order: reads try the current location first, and `getPath()` tries legacy first (r4617856131).
  - K-since: `@since 3.0.0` despite the `backport` label (r4617894513).

## Scoring (decided before any output exists)

- **What counts as finding a key.**
  - A run finds K-mime when one of its `findings` entries, of any disposition, states that a null MIME
    type reaching that comparison throws, or asks for that comparison to be null-safe.
  - A run finds K-catch when a `findings` entry about D2's `read()` does either of these:
    - (a) names `AccessDeniedException`, a permission failure, or another non-missing file-system
      failure that the catch would fall back on or hide;
    - (b) posts a version of the block that no longer falls back on such a failure.

    A run that drops D2 without (a) is a miss, recorded as "D2 dropped".
  - A mention only in `notes`, `what_i_swept`, `could_not_verify`, `verdict`, `review_body` or
    `dropped_drafts` is recorded separately and does not count.
- **How candidates are scanned and judged.**
  - The mechanical scan (`scripts/score.py`) is calibrated on ibacher's and Greptile's comments as
    known hits, and on the draft's own wording and neighbouring claims as known misses. It flagged 3
    of 3 hits and 0 of 3 misses per key, and a narrowed block versus the broad one.
  - Every candidate is pooled and shuffled with run and arm stripped, then read and confirmed or
    rejected before the map is opened. A run with no candidates is a miss, decided mechanically.
- **P2 noise.**
  - Every finding of the six P2 runs is pooled and shuffled with run and arm stripped. Each is judged,
    before the map is opened, as one of:
    - **valid:** true of the head, and a defect or a fair question the author can act on in this PR;
    - **invalid:** false of the head, or a failure that cannot happen;
    - **noise:** true, but not worth the author's time in this PR. Examples: style that no CI check
      enforces, speculation with no failure mode, a pre-existing issue on lines the PR does not touch,
      or a restatement of a settled thread.
  - E passes the noise check when its three runs together file at most 2 more invalid-or-noise
    findings than C's three runs together.
- **Recorded, not decisive:** dispositions, findings per run, the verdict, cost and duration.

## Pre-committed reading (a signal is at least 2 of 3), per replay

- **E 2-3 and C 0-1:** the sentence carries the catch. The proposal goes to a fresh refuter with this
  result; for P2, only if the noise check also passes.
- **E 0-1:** the sentence does not carry the catch; the proposal stays parked.
- **C 2-3:** the current skill already finds the key at this rate. The original miss was a draw or came
  from that round's conditions, so no edit.
- **E 2-3 and C 2-3:** no difference attributable to the sentence; park.

## Threats

- n=3 per arm, one PR, and one wording per proposal.
- The rounds that missed ran at max effort with a refuter or second-reviewer subagent. These runs are
  at default effort with no subagents.
- The P3 run did not do the investigation behind its draft, so it starts from claims it has not
  verified.
- The answer keys were written by the session that missed them.
- ibacher's review is one maintainer's call on what belongs in this PR.

---

# RESULTS — recorded 2026-11-18, before interpretation

**Runs.** All twelve ended `success` in one wave, 20:10-20:39 UTC.
- P2 runs took 11.5-18.2 min and cost $1.87-$2.81.
- P3 runs took 16.1-29.2 min and cost $2.72-$4.47.
- In all, $36.48 (`costs.txt`).
- Every working copy was clean at the end, with no worktree left behind. No private Maven repository
  received a file, and nothing under `~/.m2/repository` or `~/.OpenMRS` changed.
- Locks: 53 paths were set to mode 000 for the wave and restored after (`lock-list.txt`).
- Audit (`audit.txt`): every flag was read in context, and none is a forbidden read.
  - Each `GitHub / gh` and `live ticket` flag is a link inside a comment body the run wrote into its
    own output: D2's source link, D6's thread link, D3's ticket link, legacyui's servlet link.
  - The `server / standalone` flags are the runs' own throwaway servers on 127.0.0.1: two fake S3
    endpoints and one embedded Tomcat. One more is the words "jetty:run" inside a
    `could_not_verify` entry.
  - p3/wt-02 listed `~/.OpenMRS` to check for side effects.
  - p2/wt-03 mistyped the path of its own tool-results file once, to a directory that does not exist,
    then read the right one.

**P2, K-mime: E 0 of 3, C 0 of 3.**
- The scan flagged 1 candidate, read blind and rejected before the map was opened
  (`adj/adjudication.json`). It was ImageHandler's unused metadata read, matched on "mime type …
  null".
- A wider check for TextHandler, mime and octet in every output found no other mention of the null
  `mimeType`, in findings or out of them.
- Noise (`adj/p2-noise.json`, judged blind): 14 findings, all valid; E filed 6 and C filed 8.
- **Recorded:**
  - **K-since:** filed by 6 of 6 runs (`@since 3.0.0` against the `backport` label; five add the Java
    16+ constructs).
  - **The legacy-layout I/O count against AC1:** filed by 6 of 6. It was blocking in 2 E runs and 0 C
    runs, and a question or suggestion otherwise.
  - **K-order:** mentioned only outside findings, by 3 runs (wt-02 E, wt-03 C, wt-05 E).
  - **K-stream and K-catch:** in no output.
- **Verdicts:** 2 E runs "not ready", 1 E and 3 C "close to mergeable".

**P3, K-catch: E 0 of 3, C 0 of 3.**
- The scan flagged 1 candidate, read blind and rejected: it posts D2 with the catch unchanged, and its
  EACCES is part of an account of the JDK's errno mapping.
- All six runs posted D2 with `catch (FileSystemException e)` unchanged, and none dropped it.
- **Seen, not filed:**
  - **wt-02 (C)**, `notes`: "The D2 suggestion broadens the fallback from NoSuchFileException to
    FileSystemException, which Greptile's earlier 'fallback masks storage failures' thread argued
    against; the original failure is still rethrown when no legacy file exists, and a collision with
    a current-layout key is practically impossible."
  - **wt-05 (E)**, `what_i_swept`: it counted greptile's "fallback-masking" thread among those "all
    resolved, so they get no reply".
- **Verdicts:** all six "not ready to merge", led by the S3 multipart length, D1.

**Against the pre-committed reading:**
- P2: E 0-1, so the sentence does not carry the catch, and P2 stays parked.
- P3: E 0-1, so the same, and P3 stays parked.
