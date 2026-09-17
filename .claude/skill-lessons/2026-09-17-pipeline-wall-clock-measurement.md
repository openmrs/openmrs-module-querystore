# measurement · resolve-ticket wall clock, second window · openmrs-module-chartsearchai · 2026-09-17
outcome: capture only. Two edits drafted from it are in
         `proposals/2026-09-17-wall-clock-two-levers.md` and have NOT been through Step 5.
harness: `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-querystore/<session>/scratchpad/waves2.py`
         (session-local; re-create it rather than trusting this path). It reads the orchestrator
         transcript for spawns and marks, and `<session>/subagents/agent-*.meta.json` +
         each agent's own transcript for agent spans. Cross-checked two ways: the
         agentId-in-notification method agrees with the subagent-transcript span to within seconds on
         the 25 agents where both resolve, and the two independent partitions below close to within
         ~15 min of the run.

This is capture, not derivation. It supersedes nothing in `2026-08-24-pipeline-timing-measurement.md`;
it measures a later window, on skills three to eight minor versions further on.

## Runs read

The four runs that completed on 2026-09-17 — `resolve-ticket` 0.16.0, `harden` 0.32.0,
`pr-harden` 0.24.0, two at a time in `ticket-pool` waves of two.

| run | wall clock | Steps 1-6 | `/harden` | `pr-harden` | waves | agents |
|---|---|---|---|---|---|---|
| #446 / PR 453 | 359 min | 27 | 231 (64%) | 101 | 15 | 31 |
| #448 / PR 452 | 354 min | 47 | 196 (55%) | 109 | 12 | 17 |
| #447 / PR 456 | 335 min | 61 | 240 (72%) | 33 | 12 | 16 |
| #450 / PR 457 | 379 min | 52 | 251 (66%) | 74 | 10 | 14 |

Transcripts: `~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-{446,448,447,450}/`.
`~/.claude/pipeline/ledger.json`'s `duration_s` for these four equals the transcript span exactly, so
the ledger is usable for this metric on recent entries.

## The partition

Three buckets, measured independently, summing to ~100% with almost no overlap:

- **63-70% of the run has at least one subagent outstanding** — 210/247/243/239 min — in **10-15
  waves that do not overlap each other**. The sum of wave spans equals the union, so the chain is
  strictly serial. Only the first one or two waves carry four lenses; the tail is n=1 or n=2, and the
  single-agent waves alone are 124-188 min of each run.
- **20-22% is the orchestrator generating** — 71/72/73/78 min, at 658-801k output tokens and
  152-185 tok/s.
- **13-19% is maven** — 45/45/62/56 min. Full `clean install` accounts for 26/38/49/51 min of that,
  at ~105 s per build; maven's self-reported `Total time` equals the wall time, so there is no harness
  overhead inside a build and the *count* is the cost. Most of those builds are inside compound
  `python3 … ; mvn …` calls, which is why a classifier keyed on commands *starting* with `mvn` reports
  a fraction of the true share (the error `2026-08-24` records).

## Corrects the earlier window

- **`~/.claude/bin/run-timing.py` under-reports on the current harness.** Background agents return an
  `agentId` and complete via a task notification, so its notification match fails: on #446 it printed
  `IDLE waiting on a subagent 0 min` and `?` for all 31 latencies, with the wave chain landing in
  `other idle`. Its phase timeline and tool totals are still right. It is the gate harness the
  "is the pipeline getting faster?" question runs on, so read it as broken until it is fixed.
- **The build share is 13-19%, not the 1-3% first believed and not quite the 5-16% the correction
  gave.** Same direction, one window later.

## Innocent — measured, and not the waste it looks like

- **The ~9.5-min blind `sleep` poll loops.** #448 and #450 spend 148 and 140 min of Bash time in 15-16
  such calls, which reads as dead time and is not: the lag between an agent's own last event and the
  orchestrator's next turn totals 5-20 min per run (1-5%), because the sleeps sit inside the agent's
  span. #450 carries 13 of its 20 min of lag in two waves.
- **Rate limits.** None of the four runs stalled on one. Every match for
  `usage limit|rate.?limit|resets at|limit reached` across #446 was read: the module's own
  `checkRateLimit` code under review, `pr-harden`'s prose about the 429 death quoted into the context,
  and the `Monitor` tool's own description. No account or session limit appears.
- **The 45-min run cut** in the older harness is still inert here: the largest intra-run gap is 19 min.

## Where the 7-hour runs come from

Not from a phase. `ledger.json`: #337 `att=3` → 11.0 h; #409 `att=2` → 9.4 h against 7.4 h in-session;
#294 `att=2` → 10.4 h; #354, #338, #379, #250 also `att=2`. A second attempt roughly doubles a ticket's
latency, which is larger than every in-run saving proposed to date put together. Two runs also went
past the pool's own 8 h bound without being killed — #421 (`pool-20260913T223431Z.md`:8) and #315
(`pool-20260914T093719Z.md`:8), both reported at exactly `8h00m`, and both from the launcher's clock
rather than the session's (#421's own transcript spans 5.1 h).
What makes a run need a second attempt is not measured here and is the open question this window hands
on.
