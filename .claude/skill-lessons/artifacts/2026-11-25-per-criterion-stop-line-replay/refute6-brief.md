Stop after about 15 minutes of work and report what you have, marking what you did not run.

You are refuting a recommendation about the `pr-review` skill's blind agents. Read only; no network; post, push and
change nothing outside your own folder; touch no running process or server; start no build or server. You may run
the Python scripts named below on the files they read.

## The recommendation

1. **Keep the whole-PR agent beside the per-criterion agents** (pr-review 0.39.0 and 0.40.0 unchanged there).
2. **Run at most two review rounds at once on the machine.** This is first a practice for the owner, who starts the
   reviews, and needs no skill change. If the owner wants batches started unattended, pr-review enforces it when a
   round starts. A round that finds two other rounds in progress waits before it does anything, so no agent is cut
   short, queued mid-round or made owed. No agent-level queue, no cap inside `blind-run`.
3. **Either way:**
   - `blind-run`'s summary line prints wall-clock minutes and total turns, on a deadline kill too.
   - 0.40.0's REVERT ON counts only rounds whose agents were not starved. Starved means an agent hit its deadline,
     or its report lists a build or test its brief asked for as not run.
4. **Not adopted:**
   - making agents opt-in;
   - dropping the whole-PR agent beside per-criterion agents;
   - a machine-wide cap of five agents queued inside `blind-run`, refuted last round: five is unsupported, below
     `SKILL.md:38`'s six-agent round, posting would wait without a bound, and agents are not the only load;
   - round admission at agent start with a bounded wait after which agents are owed. In a burst it defers the later
     rounds' agents mid-review.

## Evidence (check each; say which are wrong)

- **G1, cost shares.** Across 10 reviewed PRs, review sessions $229.31, round agents $97.30, all blind runs $111.90.
  Round agents are 28.5% of the total, and whole-PR agents beside per-criterion agents $29.92 (8.8%). Made by
  `share.py` in the artifacts folder below; #71 rounds 9-10 map to sessions 2877c081 and 7268afe1, the second still
  running.
- **G2, two rounds worked.** #71 round 9 and #80 round 2 started 9 agents at 11:41-11:43 on 2026-11-25. All of them
  reported and none hit the 90-minute deadline, with wall times of 60-85 minutes. Some were cut off before their runs
  finished, which may be the 75-minute stop line.
- **G3, eight failed.** Eight rounds started 23-27 agents at 00:15-00:45: load 130-223, six agents killed at 90
  minutes, several ran nothing, and usage limits hit at 02:07 and 02:40.
- **G4.** No data exists for three to seven rounds at once.
- **G5, what deferral would lose.** Of the eight posted comments resting on agent findings, #83's D and #85's E came
  from rounds that started 5th or later in the 00:15 burst. Under two-round admission with a wait bound of an hour or
  less, those rounds' agents would have been deferred.
- **G6, keeping the whole-PR agent.**
  - In #71 round 10 it was the only source of SUB1. That mutation survives the whole suite (191 tests; the C1 control
    kills 19), and the review plans to post it.
  - SUB1 belongs to a family of privilege swaps that #71's whole-PR reviewers have raised since round 5 and that the
    review set aside in rounds 6-7.
  - In the burst, whole-PR agents often ran nothing.

## Find

The case that breaks it. In particular:
- whether "two rounds" is supported, or another number or shape is better supported;
- whether the start-of-round wait clashes with pr-review's files: Step 1, posting, owed agents, wait-loop deadlines,
  pr-harden-started reviewers, and #72/#73 merging 31-70 minutes into their reviews (`SKILL.md:26`);
- whether G5's deferral claim holds against the burst's real start times (`runs.tsv`);
- whether the starved test is checkable from what agents write.

Files: `~/.claude/skills/pr-review/{SKILL.md,blind-agents.md,posting.md}`, `~/.claude/bin/blind-run`,
`~/.claude/pr-review-rounds/`, `~/.claude/blind-runs/2026-11-2*/log/`, `~/.claude/skill-lessons/REJECTED.md`,
`~/.claude/skill-lessons/2026-11-25-blind-agent-cost-review.md`,
`~/.claude/skill-lessons/2026-11-25-blind-agent-concurrency-load.md`, `~/.claude/projects/` (transcripts), and
`~/.claude/skill-lessons/artifacts/2026-11-25-per-criterion-stop-line-replay/` (`runs.tsv`, `scripts/share.py`,
`scripts/tcost.py`, `scripts/prices.py`, `refute5-brief.md`), plus the last refuter's report at
`~/.claude/blind-runs/2026-11-25-blindcost-refute5/log/report.md`.

Report numbered objections with evidence, CONFIRMED or SUSPECTED; one line per G1-G6 (HOLDS / WRONG / NOT CHECKED);
then what you did not run.
