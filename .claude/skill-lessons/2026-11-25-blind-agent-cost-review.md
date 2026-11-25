# capture · what pr-review's blind agents bought and cost, 2026-11-24 20:35 to 2026-11-25 14:30 · 2026-11-25

A hand-written capture, not a run record. Written by a session the owner asked whether the blind agents are still
worth their memory, tokens and time. It changed no skill file. Four blind refuter rounds checked it
(`~/.claude/blind-runs/2026-11-25-blindcost-refute{1,2,3,4}`).

- **Rounds measured:** 15 finished rounds that started round agents since pr-review 0.39.0 (13 counted in detail,
  plus stockmanagement #71 round 9 and #80 round 2). Per-run cost and wall time: `runs.tsv` in the artifact folder
  below, made by `scripts/runs.py`. Cost is the maximum `total_cost_usd` over a run's result events; it is a lower
  bound, since a run killed at its deadline loses its last segment's cost.
- **Cost, the 13 rounds:** 38 agents, $75.94. Whole-PR agents $27.18; $24.58 of it beside per-criterion agents at
  5400 s, $2.60 in rounds without criteria. Six agents ran to the 90-minute deadline.
- **Posted comments resting on an agent's finding:** 8, all non-blocking: #71 r8 G, A, HJ, I (I re-raises the
  review's own round-6 survivor PR5); #80 r1 D2 and T; #83 r1 D; #85 r1 E. None came from the whole-PR agent
  alone: it co-carried #80's D2 and T with the behaviour and structure agents. Not counted by that metric: its
  leads on pre-existing defects (#68's NonUniqueResultException, which #60's round had noted), its re-raise of #71 r8's
  item E, and blind coverage of hunks outside the criteria where only one criterion agent ran (#57, #68, #78).
- **Per-criterion stop line, replayed today:** pre-registered, blind-judged, calibration passing on both subjects
  (artifact folder below). Backed finds were 15 of 18 key-runs at 75 minutes and 7 of 18 at 25, with 4 more named
  without backing at 25. The 75-minute arm was at least as good on all six keys and better on five. The
  pre-registered rule printed "move to 25"; that verdict was declined after seeing the data, on effect size, because
  the rule flags only a key the 25-minute arm misses in all three runs. Cost: $8.67 for the six 25-minute runs, $18.01
  for the six 75-minute runs.
- **Whole-PR agent at 25 minutes beside per-criterion agents:** not adopted. Under the load those rounds ran in, 9
  of its 10 runs passed 30 minutes of wall time. The one round at 1800 s, #67 r1, was killed with no report. The
  overnight whole-PR replay (6 vs 5 of 27) has no power on the finds that justified keeping the agent: K86 was found in
  0 of 6 runs, the private-ticket subject's keys in 2 of 30 key-runs.
- **Same brief, two machines:** #80 r1's behaviour brief was killed at its 90-minute deadline in the 00:15 burst (load
  about 170) and finished in 29.7 to 38.4 minutes today. #71 r8's lifecycle brief took 47.4 minutes at load about 86
  and 26.9 to 42.3 minutes today.
- **Usage limits, from the agents' own logs:** the 5-hour limit stopped agents at about 02:07 EAT and the weekly limit
  at 02:40. Blind runs started in the five hours before 02:07 recorded $94.69 (57 runs).
- **Limit designs refuted:**
  - A `blind-run` slot limit. Its other callers default to a 2700 s deadline and would count as heavy. A modelled
    burst made 15 of 20 criterion agents owed.
  - A Step 2 start gate using `pgrep -f 'bin/blind-run'`. It counts one agent as up to three processes, and its
    wait had no deadline.
  - Round marker files with a bounded wait, then starting anyway. In a burst like 00:15 this either queues for hours
    or overloads.
  - So the cap is the owner's trade-off between latency and overload, as `2026-11-25-blind-agent-concurrency-load.md`
    already says.
- **Defects found in `blind-run`:**
  - Its summary line prints the last result event's turns and minutes: "1 turns, 0.2 min" for pr69-whole's 51 turns
    and 66.9 minutes, checked by calling its own `report()`.
  - A deadline kill prints no figures (`blind-run:210-212`).
  - A run whose agent backgrounds its work and ends its turn ends with no report (blindz-s1-a75-1).
- **0.40.0's REVERT ON** can be tripped by a starved whole-PR agent (the concurrency note). Only rounds whose agents
  were not starved should count.
- **Artifacts:** `artifacts/2026-11-25-per-criterion-stop-line-replay/` (protocol, briefs, scripts, mappings,
  `score-output.txt`). Run reports are in `~/.claude/blind-runs/2026-11-25-blindz-*` and `2026-11-25-zjudge-s{1,2}`.
- **Owner's decisions pending:**
  - whether to drop the whole-PR agent beside per-criterion agents;
  - a cap on concurrent reviews or agents.

## Addendum, 2026-11-25 19:00 · the owner asked for a recommendation on the two decisions

- **New finding for the whole-PR agent beside per-criterion agents:** in stockmanagement #71 round 10 (agents started
  17:21), it was the only source of SUB1. SUB1 weakens submit's privilege from MUTATE to APP and survives the whole
  suite (191 tests; the C1 control kills 19; `results/full12b.out` in session 7268afe1's scratchpad). The review plans
  to post it. No other round-10 report raises it. It belongs to the privilege-swap family that #71's whole-PR
  reviewers have raised since round 5 and that the review set aside in rounds 6-7.
- **Cost shares, from transcripts** (`artifacts/.../scripts/share.py`, `tcost.py`, `prices.py`):
  - Pricing: claude-opus-5-5 list prices in $/M were solved exactly from 176 blind runs' `modelUsage`: input 4,
    output 20, cache read 0.2, 1-hour write 8.
  - Calibration: computed cost equals the recorded `total_cost_usd` on 7 runs; without de-duplication the same
    transcript reads 2.2 times too high.
  - Across 10 reviewed PRs:
    - review sessions $229-231 (7268afe1 still running);
    - round agents $97.30, 28.5%;
    - all blind runs $111.90;
    - whole-PR agents beside per-criterion agents $29.92, 8.8%.
  - Opt-in agents would save at most about 28% of a review's spend.
- **Concurrency, more data:**
  - 17:21-18:30: four reviews started 13 round agents within 13 minutes (#71 r10, core #6502 r1, #6835 r3, #6157 r5).
    Three full pr-review blind sessions (pc3901) and refuters ran alongside. Load was 55-85 (per pr71-r10-lifecycle).
    Every agent reported, taking 16-65 minutes.
  - 11:41: two rounds (9 agents) at load about 60. Some agents dropped planned mutation or suite runs (pr71-r9-balances,
    sm80-r2-behaviour).
  - The night burst was 10 rounds (29 agents) plus 6 dlr replay agents.
  - So round or agent count predicts starvation poorly; the reviews' own builds and other sessions' work are part of
    the load.
- **Refuted this evening:**
  - `blindcost-refute5` refuted a machine-wide cap of five agents queued in `blind-run`:
    - five is not supported by the data;
    - it is below `SKILL.md:38`'s six-agent round;
    - posting waits for each agent "until reported or past its deadline", so a queued agent holds posting with no
      bound;
    - agents are not the only load.
  - `blindcost-refute6` refuted a start-of-round wait limiting reviews to two rounds:
    - there is no round-in-progress state to count;
    - a bounded wait ends in either no cap or owed agents;
    - it would hold up pr-harden's reviewers;
    - two rounds were already starved while seven reported.
  - Admission with deferral: #83's D and #85's E came from rounds that started 7th and 8th. `queue_sim.py` at K=6
    delays them 121 and 142 minutes on the recorded walls, but finishes every round 5-60 minutes earlier on walls / 3.
    The data can't tell which.
- **Correction:** a criteria round averages 3.6 agents (43 in 12 rounds). 4-5 agents is about one round, not two.
- **Recommended to the owner:**
  - Keep the whole-PR agent.
  - Build no concurrency cap into the skill until load is recorded. In practice, avoid a burst the size of the night
    one.
  - Make three fixes:
    - `blind-run` samples the load average while each agent runs;
    - its summary line prints wall minutes and total turns, on deadline kills too;
    - 0.40.0's REVERT ON skips starved rounds, starved meaning an agent was killed at its deadline or its report names
      load or slowness.
  - Implementing them needs the owner's go-ahead and a skill-retro pass.
