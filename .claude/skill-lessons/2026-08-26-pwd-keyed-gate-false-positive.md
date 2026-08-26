# defect · pr-harden gate fires on a co-located interactive session · pipeline · 2026-08-26
outcome: observed live, nothing applied. No skill edited; no state entry touched.
evidence: perishable — captured while the run below was in flight.

## What happened

An interactive session in `/Users/danielkayiwa/Projects/openmrs/openmrs-module-chartsearchai`,
answering a question about generalising the skills to other repos, was stopped by
`pr-harden-gate.sh` with "resolve-ticket is mid-run and has not opened its pull request yet".

No resolve-ticket run existed in that session. The state entry it blocked on belonged to a
DIFFERENT, live session:

- `pr-harden-state.json` entry for that path: `phase: building`, `round: 1`, `pr: null`,
  `ts: 1787754996` — 161 s old, i.e. present, fresh and parseable, so the gate's fail-open
  list correctly did not cover it.
- owner: pid 62432, `claude -p /resolve-ticket .../issues/297`, 4 min 23 s old, spawned by
  pool-run pid 38042 (`--ticket 310,297,266`).
- state file mtime 17:36:36 against the #297 run's 17:35 start: the live run wrote it.

## Why it matters

The gate keys on `$PWD` alone (`KEY="$PWD"`). The pool works a repo in ONE checkout, so every
interactive session opened in that checkout while the pool is running inherits a gate for a run
it does not own and cannot advance.

The hook's own remedy instruction makes it worse: it tells the stopped session to either continue
the phases, or set `override: true` with an abort reason. Both damage the live run.

- `override: true` disarms the gate for the #297 run for the rest of its life, so the early stop
  the gate exists to catch would pass unnoticed. The interactive session cannot know it is
  disarming someone else's gate, because the entry says nothing about who owns it.
- "continue the phases" would have a second session implement #297 in the same work tree as
  the session already implementing it.

So the fail-open reasoning in the script's header is sound but incomplete: it enumerates cases where
the ENTRY is unusable, and never the case where the entry is perfectly good and belongs to somebody
else.

## Candidate fix, not proposed as a rule

The entry carries no owner identity. Writing the owning pid and session id into it, and having the
gate allow when the reader is not the owner (or when the owner pid is alive and is not this
process's ancestor), would separate "my run has not converged" from "a co-located run has not
converged". Unverified; wants a second sighting and a check that the pool's own kill path still
leaves a blocking entry blocking for the session that owns it.

Not applied. One sighting.

## Driver capture (pool-run)
outcome as the driver measured it: no-pr
session: b73cb0d4-042f-4e35-a655-0f270432169d · 23m42s · 171 assistant turns · stream: /Users/danielkayiwa/.claude/pipeline/logs/20260826T143511Z-openmrs_openmrs-module-chartsearchai-297.jsonl
- the run left its gate entry unfinished: phase=building blocking=0 round=1 override=False
- this run started on a checkout whose gate state a previous run left behind — pr-harden-state.json: phase=building blocking=1 edits=None pr=None round=1
- this run started on a checkout whose gate state a previous run left behind — harden-state.json: phase=None blocking=None edits=1 pr=None round=None
