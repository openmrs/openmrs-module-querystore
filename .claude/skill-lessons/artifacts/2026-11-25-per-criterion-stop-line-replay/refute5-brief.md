Stop after about 15 minutes of work and report what you have, marking what you did not run.

You are refuting a recommendation about the `pr-review` skill's blind agents. Read only; no network; post, push and
change nothing outside your own folder; touch no running process or server; start no build or server. You may run
the Python scripts named below on the files they read.

## The recommendation

1. **Keep the whole-PR agent beside the per-criterion agents** (pr-review 0.39.0 and 0.40.0 unchanged there).
2. **Cap pr-review's round agents at five running at once on the machine.** The rest wait in a queue inside
   `~/.claude/bin/blind-run`, and each agent's deadline and stop line start when it starts, not when it is queued. A
   queued agent shows as queued to the review that waits on it, so the review does not take it for dead. Fix refuters
   and other `blind-run` callers take no slot.
3. **Either way:** `blind-run`'s summary line prints wall-clock minutes and total turns, on a deadline kill too; and
   0.40.0's REVERT ON counts only rounds whose agents were not starved by load.
4. **Not adopted:** making agents opt-in; dropping the whole-PR agent beside per-criterion agents.

## Evidence (check each; say which are wrong)

- **F1, cost shares.** Across 10 reviewed PRs (stockmanagement #57, #68, #69, #71 rounds 9-10, #77, #78, #79, #80, #83,
  #85): review sessions $229.07, round agents $97.30, all blind runs $111.90 (fix refuters included). Round agents are
  28.5% of the total. Whole-PR agents beside per-criterion agents: $29.92, 8.8%. Made by `share.py` and `tcost.py`
  from transcripts: prices solved by `prices.py` from 176 blind runs (exact fit); computed cost equals the recorded
  `total_cost_usd` on 7 runs; without de-duplication the same transcript reads 2.2 times too high. Session ids per PR
  come from the scratchpad paths in each round record; #71 rounds 9-10 are sessions 2877c081 and 7268afe1 from
  2026-11-25 05:00Z.
- **F2.** In #71 round 10 the whole-PR agent was the only source of SUB1 (submit's scope privilege MUTATE->APP): it
  survives the whole suite (191 tests) while the C1 control kills 19. The review plans to post it. It is not in the
  findings the review recorded before reading the reports, and no other round-10 agent raised it.
- **F3.** In the 00:15 burst, whole-PR agents often ran nothing: #79 (builds never reached tests), #83 (ran nothing),
  #85 (read only), #69 (no server run). So the earlier count, 0 of 13 rounds with a whole-PR-only posted finding,
  measured overload as much as the agent.
- **F4, why five.**
  - Replay waves of 4-5 agents (13:10-15:00): wall times 17-42 minutes.
  - #71 round 8, 5 agents plus mutation batches at load about 86: 41-55 minutes, all reported.
  - #71 round 10, 5 agents plus the review's mutation runs: 48.5-65.1 minutes, all reported.
  - #71 round 9 with #80 round 2, 9 agents from 11:41: 60-85 minutes, some cut off before their runs finished.
  - The 00:15 burst, 23-27 agents: load 130-223, six killed at 90 minutes, several ran nothing.
- **F5, the latency price.** The burst's 29 agents at about 45 minutes each through five slots take about 4.4 hours.
  In reality they ended by about 02:13, six of them killed, and usage limits stopped work at 02:07 (5-hour) and 02:40
  (weekly).

## Find

The case that breaks it. In particular:
- whether five is supported, or the queue breaks pr-review's flow: posting waits for the agents, wait loops have
  deadlines, owed agents, and the REVERT ON counting;
- whether F1's shares are wrong: sessions mapped to the wrong PR, or sessions doing other work;
- whether one finding (F2) carries item 1;
- a better pick for either decision.

Files: `~/.claude/skills/pr-review/{SKILL.md,blind-agents.md,posting.md}`, `~/.claude/bin/blind-run`,
`~/.claude/pr-review-rounds/`, `~/.claude/blind-runs/2026-11-2*/log/`, `~/.claude/skill-lessons/REJECTED.md`,
`~/.claude/skill-lessons/2026-11-25-blind-agent-cost-review.md`, `~/.claude/projects/` (transcripts), the runs table
`~/.claude/skill-lessons/artifacts/2026-11-25-per-criterion-stop-line-replay/runs.tsv`, #71's results under
`/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-openmrs-core/7268afe1-ae68-4ca9-9cdb-887f1de6b319/scratchpad/results/`,
and the scripts in `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/5db247f5-23fd-4609-a5dc-8b2de0889fe7/scratchpad/`
(`tcost.py`, `share.py`, `prices.py`).

Report numbered objections with evidence, CONFIRMED or SUSPECTED; one line per F1-F5 (HOLDS / WRONG / NOT CHECKED);
then what you did not run.
