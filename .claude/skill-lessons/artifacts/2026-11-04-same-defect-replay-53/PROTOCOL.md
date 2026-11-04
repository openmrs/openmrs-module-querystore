# Does a "same defect, where the PR didn't look" bullet make pr-review find K1 on stockmanagement PR 53?

Pre-registered 2026-11-04, before any arm ran. Owner-directed skill-retro pass. Follows
`2026-11-04-pr-review-stockmanagement-53-missed-input.md`: a `pr-review` 0.24.0 run on this head reported
"ready to merge, nothing for the author to act on", and a maintainer then raised K1 (below), which the
author fixed in this PR (`a9c2a39`, merged 2026-11-04T20:33Z).

## Subject

openmrs/openmrs-module-stockmanagement PR 53 at `d4a9bba8131a84229bccfd05c49fe0dc5f34cce0`, base `master`
`b1c332668fe5cc218e81f8e80a345a510d578cfa`, frozen at 2026-11-04 12:00 UTC (the maintainer's comment is
12:36:17Z). Snapshots: the PR description (last edited 2026-11-03T12:27:37Z, so the live body is the frozen
body) and ticket O3-6201 with its two comments, both created before the freeze. PR conversation withheld,
as in the 2026-10-18 replays. Per-run clones holding only the head's ancestry (127 commits) and a local
`master`; private Maven repository per clone with `~/.m2` as a read-only tail, JDK 21 offline (calibrated:
`StockOperationActionResourceTest` 4 tests pass in 17 s, the private repository stays empty). Reference
copies: webservices.rest at tag 3.0.0, and the frontend at its last `main` commit before the freeze
(`9e9cd98`, later commits pruned).

## Arms (n=3 each, all six runs in one wave, so contention is identical)

- **C** — `pr-review` SKILL.md and posting.md exactly as live (0.24.0).
- **E** — the same, plus one bullet after "Unchanged neighbors" in Step 3's "Trace outward":
  > - **The same defect, where the PR didn't look.** When the change fixes a kind of defect (an unguarded
  > input, a missing null check), look for the same kind on the same path in the code it left alone, such
  > as every other input that reaches the line the PR now protects. An instance that predates the PR blocks
  > only if it defeats the PR's own guarantee; otherwise it is a suggestion, which the author may take as a
  > follow-up.

Same brief for both arms apart from the skill path, working copy and output path (the 2026-10-18 brief
with the subject's facts swapped in). Headless `claude -p --model claude-opus-5-5`, default effort, from
each clone (project memory empty: canary confirmed `memory/` empty and "MEMORY: NONE"). `GH_TOKEN` and
`GITHUB_TOKEN` set invalid (canary: `gh api user` 401), `--strict-mcp-config` with no servers, and
WebFetch, WebSearch, Agent, Task, Skill and every publishing tool disallowed. Slots wt-01..wt-06 assigned
to arms at random; the map is in `KEY-private.json`.

## Answer key

- **K1.** A request with a uuid that resolves to an operation and a `name` that is absent or JSON `null`
  passes the PR's guard, runs the lookup, and fails at `switch (delegate.getName())` with a
  NullPointerException, which REST 3.0.0 returns as HTTP 500. Pre-existing on master. Measured
  2026-11-04 through REST's real `create()` and `BaseRestController.handleException`.

## Scoring (decided before any output exists)

- A run **finds K1** when one of its `findings` entries, of any disposition, states that an absent or null
  action `name` is not rejected (or reaches the switch, or fails with an NPE or 500), or asks for a
  name/action null guard or for `name` to be required. A mention only in `notes`, `what_i_swept` or
  `could_not_verify` is recorded separately and does not count.
- Mechanical candidate scan over each finding's claim, failure_mode and evidence (`scripts/score.py`,
  calibrated before launch on the maintainer's comment as a known hit and on uuid-only findings as known
  misses). Every candidate is then read and confirmed or rejected, pooled and shuffled with run and arm
  stripped, before the map is opened. No candidates in a run means a miss, decided mechanically.
- Recorded, not decisive: K1's disposition (blocking would contradict E's own bullet), findings per run by
  disposition, the verdict, cost.

## Pre-committed reading (a signal is at least 2 of 3)

- **E 2-3 and C 0-1:** the bullet carries the catch; the proposal goes to Step 5's refuter with this result.
- **E 0-1:** the bullet does not carry it; park the proposal, as the 2026-10-18 replays parked theirs.
- **C 2-3:** the current skill already finds K1 at this rate, so the 2026-11-04 miss was a draw or came
  from that run's conditions; no edit.
- **E 2-3 and C 2-3:** no difference attributable to the bullet; park.

## Threats

n=3; one PR; one defect. The missing run was at max effort with the PR conversation and a
second-reviewer subagent; these runs are default effort, conversation withheld, no subagents. The bullet is
one wording. The answer key was written by the session that missed K1.

---

# RESULTS — recorded 2026-11-04/05, before interpretation

**Runs.** All six ended `success` in one wave (4.3-10.2 min, $0.92-$1.74, $7.74 in all; `costs.txt`). Every
working copy was clean at the end. Locks: 9 paths set to mode 000 for the wave and restored after
(`modes.txt`). Audit (`scripts/audit.py`, calibrated first on 7 planted forbidden reads, all flagged, and 7
allowed reads, none flagged): 0 forbidden reads in any of the six transcripts.

**K1 found by: E 2 of 3 (wt-01, wt-04), C 0 of 3 (wt-02, wt-05, wt-06).** The scan flagged 3 candidate
findings; read blind and recorded in `adj/adjudication.json` before the map was opened: two are K1
(both filed `nit`), one is not (the dead validator's uuid rule). No K1 was filed blocking.

**K1 mentioned outside findings, and not filed** (recorded, not counted):
- wt-02 (C), `notes`: "A missing 'name' still NPEs after lookup (pre-existing, out of scope, no
  wrong-operation effect)."
- wt-06 (C), `notes`: "A missing 'name' still causes an NPE (500) in the switch. That is pre-existing and
  outside this ticket, so I did not file it."
- wt-03 (E), `notes`: "A pre-existing, out-of-scope issue: a missing 'name' with a valid uuid NPEs in the
  switch and returns a 500 instead of a 400. No operation is mutated, so the PR's guarantee holds and I did
  not raise it as a finding."
- wt-05 (C) does not mention it.

**Verdicts:** all six "ready to merge".

**Against the pre-committed reading:** E 2-3 and C 0-1, so the bullet carries the catch, and the proposal
goes to Step 5's refuter with this result.

**Corrected after the gate, 2026-11-05.** The wt-03 quote above had dropped "as a finding" (`out/wt-03.json`,
`notes`). Nothing else in these results changed.
