# measurement · resolve-ticket pipeline wall clock · openmrs-module-chartsearchai · 2026-08-24
outcome: measurement only — seven proposals derived from it were refuted and reverted, see REJECTED.md
harness: ~/.claude/bin/run-timing.py <session>.jsonl   (validated independently: the timeline
         partition is exact — busy + idle-on-agent + other-idle = window, for all four runs — and the
         45-min run cut is inert, since the largest intra-run gap is 39 min and the smallest post-run
         gap is 345 min)

This is capture, not derivation. It records what four runs cost and which figures survived an
adversarial re-derivation, so the next attempt at making the pipeline faster starts from measured
ground instead of re-deriving it wrongly, as this one did.

## Runs read

| run | to `gh pr ready` | rounds | agents spawned |
|---|---|---|---|
| #284 / PR 304 | 138 min | 1 | 10 |
| #268 / PR 306 | 233 min | 1 | 11 |
| #269 / PR 307 | 234 min | 1 | 17 (2 of them post-run, in skill-retro) |
| #298 / PR 301 | 313 min | 3 | 17 |

Transcripts: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-openmrs-module-chartsearchai/
{a8d8799f…, 9f704564…, 5e151d3b…, 18907fcb…}.jsonl

## Reproduced by an independent re-derivation

- **The run is a chain of sequential agent waves and idles through nearly all of them.** Waves per
  run: 6, 6, 11, 13. Minutes the orchestrator was idle with an agent outstanding: 70, 107, 145, 171.
  Minutes it did useful work while an agent was outstanding: 2.2, 11.8, 6.1, 4.6. The overlap is
  essentially zero — that is the finding with the most headroom in it, and it survived every attack.
- **Agents run unbounded, 10-27 min, and a multi-agent wave costs its slowest member.** One Phase 2
  wave returned at 12.2 / 22.4 / 12.6 / 14.0 min, another at 12.1 / 26.7 / 20.4 / 16.6, all members
  spawned within 90 s of each other. 24 of 55 spawns exceeded 15 min.
- **The plan-refutation gate ran as two serial waves in all four runs**: 6+9, 11+15, 8+12, 5+10 min.
- **Every one of the 55 `Agent` spawns left `model` unset**, and 54 of 55 used `general-purpose`.
- **Both state-file records are verbatim**: #302 ("the awaits were being written only to
  harden-state.json, so the gate fired mid-run with agents live") and #298 ("no `awaiting` field …
  Cost: two ten-minute in-turn wait loops"). #298 UNDERSTATES its own cost — its transcript carries
  six sleep-poll loops totalling 42.6 min, each polling an agent's output file for byte-stability.

## Refuted — figures a first pass published and a re-derivation broke

- **"The build is not a cost, 1-3% of the run."** False, and it was the load-bearing claim of a
  "don't optimise here" recommendation. Every Bash call containing `mvn`: 34/73/48/41 calls and
  14.8/37.6/24.1/28.1 min per run = **6.6-15.9%**. Restricted to `mvn -o clean install` alone:
  14/27/20/23 calls, 11.5-24.6 min, 6.7-10.6%. In #268 the builds are half the whole pr-harden loop.
  Cause of the error: the first classifier keyed on commands that *begin* with `mvn` and silently
  dropped every compound call, where most builds actually live.
- **Turn / token / Bash-call ranges.** The published "379-582 turns, 487-848k output tokens,
  220-254 Bash calls" were measured to the transcript's idle cut, while the wall-clock totals beside
  them were measured to `gh pr ready` — 4 to 123 minutes earlier. Truncated consistently at
  PR-ready: 309/521/346/383 turns, 399k/777k/549k/592k output tokens, 175/253/168/147 Bash calls.
- **"Orchestrator generating, 91-145 min."** That bucket includes tool-execution latency: #298's
  145 minutes contain 73 minutes of Bash time. Generation is at most half of it.
- **Phase splits do not sum.** #268 Steps 1-3 is 53 (not 51); #269 is 55 (not 45); #298's loop is
  147 from PR-created or 135 from `/pr-harden`, and its transcript has no `/resolve-ticket` mark at
  all, so its first phase is measured from a different boundary than the other three rows.
- **"70-171 min had an agent outstanding"** mislabels the figure: that is the idle-with-agent time.
  The agent-outstanding union is 72 / 119 / 151 / 175.
- **"A bound of 10-15 min would save 40-60 min."** The most generous re-cost — every over-bound
  agent truncated at its bound, losing nothing — yields at most 16 / 43 / 46 / 56 min (12-19% of a
  run), and the residual critical path is then the orchestrator's own time, which a bound does not
  touch. Against that: the findings the records CREDIT came from agents that ran past the proposed
  bounds — #298's design-improving r1 finding from a 21.6-min agent, its only blocking finding from
  a 19.4-min agent, #269's real coverage gap from a 19.7-min P2r4 agent. Whether a bounded agent
  would have found the same thing earlier is not decidable from these transcripts in either
  direction.
- **"Replace scripts cost 25 minutes of wall clock."** The 92 replace-shaped calls in #298 do total
  24.1 min — but 35 of them run `mvn` inside the same call and account for 23.5 of those minutes.
  The 32 calls that are a pure replacement execute in **4.3 seconds total** (median 0.1 s). What is
  actually recoverable is generation: 18.5-21.1 min and 128k-179k output tokens per run sit in the
  turns that EMIT those scripts, and only the heredoc boilerplate of that would go away.
- **Mechanical agents do not occur.** Of 55 spawns, 54 are refute/review/fix/verify. The one
  non-review spawn in the corpus fired 62 minutes after PR-ready, inside skill-retro. Mechanical
  agents are 0.0 of the 733 minutes of agent latency, so no measurement here can say what tiering
  them would save.
- **`claim-lint` (a prototype, now removed) caught 0 of the ~15 false claims the four records quote**,
  including the one genuine stale tally in the corpus — "145 containment-only pairs" -> 143 — which
  its regex misses on the hyphenated compound. Most of the corpus's false claims are universals,
  which the tool deliberately does not check.

## What this leaves standing, for whoever tries next

The zero-overlap wave chain is the real headroom, and neither of the two obvious ways at it survived
this pass: bounding agents cannot be shown safe against the findings the records credit, and merging
the gate's two passes into one wave is refuted by the records (see REJECTED.md, P1). Untested
directions, in the order the measurement favours them: making the orchestrator useful DURING a wave
(2-12 minutes of 70-171 are used today); the build share, which is 5-16% and was wrongly dismissed;
and the ~19 min/run of generation spent emitting edit scripts, which is a real cost even though the
wall-clock attribution first offered for it was wrong.
