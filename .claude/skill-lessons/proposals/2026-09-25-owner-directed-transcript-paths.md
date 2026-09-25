# 2026-09-25 third pass — owner-directed: run records name their transcript by a real path

Directed by the owner after the second window, from its parked list
(`proposals/2026-09-25-window-514PR524-loop3.md`, REJECTED.md's block for that window). No new run
record. Two proposals, staged in the querystore working tree for the gate:
`git -C ~/Projects/openmrs/openmrs-module-querystore` is behind, so use `~/Projects/openmrs/querystore`.

## P3 — pr-harden and resolve-ticket: take the transcript's folder AND uuid from the scratchpad path

Both skills' run-record sections (`pr-harden`:1259-1260, `resolve-ticket`:649-650 before the edit) say
"the session uuid is the directory name in this run's scratchpad path, and the transcript is
`~/.claude/projects/<cwd-slug>/<uuid>.jsonl`". Both templates carry `<cwd-slug>` (:1232, :622).
`<cwd-slug>` is defined nowhere, and runs derive it from a directory that can differ from the one the
transcript sits under.

Evidence (measured 2026-09-25; `artifacts/2026-09-25-transcript-check.py` plus a uuid search over
`~/.claude/projects`):
- 11 headers (10 records: #266, #234, #297, #238, #229, #340, #355, #357, #409, #527 twice) name the
  right uuid under the wrong folder. The first nine name the main checkout's folder for a session that
  ran in a pool worktree. #527 names `-Users-danielkayiwa-Projects-openmrs-chartsearchai-527`, while its
  transcript is under `-Users-danielkayiwa-Projects-openmrs-chartsearchai`.
- 1 header (#514-3) names the wrong uuid: `ls <dir>/*.jsonl -t | head -1` (T3:281-282).
- The scratchpad path carries both names. T3's is
  `/private/tmp/claude-501/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-514/7b912efd-…/scratchpad`
  (36 mentions in T3), matching its transcript folder. #527's session `ebc78818-…` has
  `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/ebc78818-…` (347 mentions),
  matching its transcript folder and NOT the folder its record named.
- Cost: every folder miss fails loudly, and a retro recovers by searching for the uuid. The uuid miss
  is silent, because it resolves to a real session on the same PR.

Bar: (a), the same instruction produced 11 wrong headers across 10 records and was bypassed once for
the uuid. Owner-directed.

Edit (both skills, identical): the template line becomes
`transcript: ~/.claude/projects/<folder>/<uuid>.jsonl`, and the sentence becomes:

    bookkeeping: this run's scratchpad path is `…/<folder>/<uuid>/scratchpad`, and the transcript is
    `~/.claude/projects/<folder>/<uuid>.jsonl`, the same two names. Take neither from anywhere else: #527
    recorded the folder of the worktree it had moved into, and #514/PR524's third loop took its uuid from
    a directory listing, which named another session. `no compaction · peak not surfaced` is the expected

Prunes: replaces the sentence in place, net +2 lines per skill. Patch bumps: pr-harden 0.33.1,
resolve-ticket 0.21.2.

## P4 — pool-run: a driver capture names its transcript by the folder the driver already computes

`capture_record` wrote `transcript: ~/.claude/projects/<cwd-slug>/{session_id}.jsonl` (`pool-run`:3179),
the record template's placeholder as a value, in all 4 driver captures (266-driver, 310-driver,
349-driver, 515-driver). The driver computes that folder itself for `silence_since`
(`project_dir_name`, :1208; used at :1243), and its only caller (:3339) has the worktree `wt` in scope
on both the fresh and the resumed path (:3230-3243). Bar (c), shaped like one: the driver writes as a
path a value the record format it writes defines as a path. Owner-directed. Cost so far: 0, since the
`session:` line and the stream path are beside it.

Edit: `capture_record(…, own, say, *, worktree: Path)`, whose line is
`~/.claude/projects/{project_dir_name(worktree)}/{session_id}.jsonl`, with the caller passing
`worktree=wt`.

Test, written first: `test_parallel_run` (which drives the real `run_wave` with a stub `claude`) now
checks each ticket's driver capture. Its `transcript:` line must EQUAL
`~/.claude/projects/{project_dir_name(worktree_path("o/r", ticket))}/{its own session: id}.jsonl`.
- Before the fix: 580 passed, 2 failed, those two ("…/<cwd-slug>/7127f4db-….jsonl").
- After the fix: 582 passed, 0 failed.
- Mutations, each run in a scratch copy of `.claude`: folder from `worktree.parent`, 580 / 2; session
  id suffixed, 580 / 2.

Nothing else reads the line: `pool-run`, `gate-state`, the hooks and `pool-watch` hold no parser of
`transcript:`.

## The gate's reply (fresh read-only agent)

- **P3 template line → APPLY.**
- **P3 sentence → REVISE, blocking twice.**
  - **(1) A headless `claude -p` run has no scratchpad path, and both wrong-uuid records came from
    headless runs.**
    - None of the 46 headless temp roots under `/private/tmp/claude-501` has a `scratchpad/`, against
      136 of 187 interactive ones. 0 of 688 `sdk-cli` transcripts mention their own scratchpad,
      against 461 of 483 `cli` ones.
    - Record writes split: interactive 86, with 12 wrong folders and 0 wrong uuids; headless 27, with
      0 wrong folders and 2 wrong uuids.
    - **So this draft's "The scratchpad path carries both names. T3's is …/scratchpad (36
      mentions)" was false.** T3's temp root holds only `tasks/`, the 36 were `…/7b912efd…/tasks`
      paths, and T3 mentions its scratchpad 0 times (re-measured).
    - Headless runs got the folder right from their cwd, which "take neither from anywhere else"
      would have forbidden.
  - **(2) "The same two names" is false for a resumed session.** #305 (`28d5f7ec`) was resumed from `~`,
    after which its scratchpad sat under `-Users-danielkayiwa`. Re-measured: 193 mentions there and 38
    under its worktree folder; its cwd alternates. Its transcript stayed under the worktree's folder.
    Its L2166 record named the `-Users-danielkayiwa` path, which does not exist, and that is exactly
    what this draft prescribes.
- **Evidence corrections.**
  - A SECOND wrong uuid: #505/PR529 (`…-505.md` second header). It names `e3da4188`, while the writer
    was headless `2463cfcc` (:1138), after `ls <dir>/*.jsonl | tail -1` (:1129). Re-measured, and
    `e3da4188` never mentions PR #529. The artifact's mention-based detector passed it, because
    `e3da4188` wrote that file's first record.
  - #337 and #338's session directories exist (`subagents/`, `tool-results/`); only their `.jsonl` is
    gone, so those headers were right. Re-measured.
  - P4's bar is (a), four captures, not "(c), shaped like one".
- **Replacement A** (uuid from `$CLAUDE_CODE_SESSION_ID`, transcript from the uuid glob). The gate said
  to ship it only after a headless check, which it could not run read-only. **Run here:** `claude -p …
  --output-format stream-json --verbose --dangerously-skip-permissions --session-id 45120e1c-…`, the
  driver's flags (`pool-run`:2668-2673), in a scratch directory. The Bash tool printed
  `SID=45120e1c-…` and exactly one path, that session's transcript under the folder for its cwd.
  Interactive: this session's shell has `db22ea60-…`, and the glob prints its one transcript.
- **P4 driver → APPLY.** `project_dir_name` of each transcript's first `cwd` reproduces its folder for
  1170 of 1170 transcripts. `wt` is the launch cwd on the fresh path and on the resumed one.
- **P4 test → APPLY, two wording fixes.** The check's name overclaimed "the folder claude uses" (it
  pins the folder `silence_since` reads). The comment's "the only pointer" and "while the driver
  computed the folder" were false.
- Pre-existing, noted: `pool-run`:98 pointed at a `session_transcript` that does not exist.

## Applied

Replacement A in both skills, verbatim and reflowed; the template line as staged; P4 as staged, with the
gate's two test-wording fixes; `pool-run`:98's pointer corrected to `project_dir_name`. No second gate
round: every revised text is the gate's own, and the one check it made a condition was run and passed.
pr-harden 0.33.1, resolve-ticket 0.21.2. Net: +4 lines in each skill, +12 in `pool-test.py`, +1 in
`pool-run`. Linter 10 files, 0 findings; `pool-test.py` 582 passed / 0 failed. The new checks fail on
the pre-fix driver (580 / 2) and on two mutations (580 / 2 each).
