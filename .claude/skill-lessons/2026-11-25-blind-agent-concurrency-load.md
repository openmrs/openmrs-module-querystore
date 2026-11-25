# capture · concurrent pr-review sessions each starting blind agents overload one machine · 2026-11-25

This is a hand-written capture, not a run record. Written by the session that applied pr-review 0.39.0 and 0.40.0 the
day before.

- **What was running at 01:08:** 23 `blind-run` agents from 8 reviews running at once. Each review started a whole-PR
  agent, and most also started agents per acceptance criterion or area (pr-review 0.39.0 and 0.40.0, Step 2).
- **Load:** averages of 160 to 223 on a 10-core machine, between 00:50 and 01:10.
- **Earlier the same night:** one review with 5 agents plus mutation batches had reached 86.
- **Why it matters:**
  - Each agent's deadline (`blind-agents.md`: 30 or 90 minutes) and stop line assume a machine that has time for it.
  - Under this load, agents do a fraction of the work their budget implies.
  - One 25-minute agent ended in 12.5 minutes, listing its end-to-end server runs as not run.
  - The 0.40.0 REVERT ON counts rounds whose whole-PR agent found nothing new. A starved agent can trip it for the
    wrong reason.
- **Not in the skill:** any cap on how many blind agents run at once across sessions. The per-review cap ("no more than
  six") does not reach it.
- **Cause, not established:** whether the reviews ran in parallel by the owner's choice or through a driver.
- **REOPEN ON:** the owner's decision on a cap, or a second sighting.
