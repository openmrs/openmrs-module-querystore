# defect · an unattended run dies on the gate's own awaiting-yield · pipeline · 2026-08-26
outcome: fixed — pr-harden 0.11.0, resolve-ticket 0.11.0, ticket-pool 0.8.0, pool-run, both gate copies
harness: the real hook, invoked from a real `claude -p` session; plus a 10-case table over the script

## The defect

**BOTH** Stop-hook gates — `pr-harden-gate.sh` and `harden-cycle-gate.sh` — ALLOW a stop while
`awaiting` is non-empty and fresh, each on the same stated reason. #310 died inside `/harden`, so the
harden gate is the one that allowed it and fixing only pr-harden's would have left the expensive half
open. Their headers state the reason: "the harness re-invokes the orchestrator when the agent completes, so yielding mid-await does
not end the run, it is how the run proceeds."

That premise is true of an ATTENDED session and false of `claude -p`, which exits when its turn ends.
For an unattended run the yield IS the death, and because allowing is `exit 0` it is silent.

## Evidence

- #297: last state write `awaiting=[{agent: "refute plan #297 pass 1"}]`, final message *"I've read
  and reproduced the ticket, written the plan, and dispatched the refutation gate. Here's where things
  stand"*, then exit. 51 turns, $9.62, no PR, plan and reproduction discarded. Ledger: `no-pr`.
- #310: same signature in `/harden` phase 2 pass 3. 1365 turns, 95 min, $76.72, no PR, 10 orphaned
  agent worktrees left behind. Its log shows 15 `awaiting` writes; the final value is not recoverable
  from the tail, so this run is CONSISTENT with the cause, not proof of it.
- Neither log contains any gate text (0 hits for `pr-harden-gate`, `Stop hook`, `mid-run` across 4227
  lines). That is what allowing looks like, and it is NOT evidence the hook did not run.
- Hooks DO reach `-p` sessions. Probed the same day: a planted "phase: building" entry blocked a
  headless session — 42 lines for a two-word prompt, `mid-run` x6, `Stop hook` x3, $0.34. An earlier
  inference in this session that headless sessions are ungoverned was measured WRONG.
- The driver's watchdog cannot see it: `quiet_seconds` catches a hang, and this is a clean exit.

## The fix

The allow is now scoped to attended sessions. The signal is a pid-stamped marker file the driver holds
for the life of the run (`pipeline/unattended/<sanitised-cwd>.json`), NOT a field in the state entry:
the skill rewrites that entry at its Step 1 and would drop a seeded field silently, fail-open into the
defect. The marker carries the driver pid because a SIGKILLed driver leaves the file behind, and a
stale marker must not make an interactive session in that checkout unattended.

Also: `pool-run` classifies a run that ended with `awaiting` non-empty as `died-yielding` rather than
`no-pr` — the cause is known and is not the run's own judgement. Placed AFTER the `aborted`
reclassification, so a deliberate abort still wins.

## Pinned by

`skills/pr-harden/gate-test.sh` (10 cases) and `skills/harden/gate-test.sh` (9), through the real
scripts: the new block, the stale-marker-inert
case, and six that pin today's behaviour (attended yield still allows, converged still allows,
override still allows). The unattended cases are red on the old scripts (2 of 9 measured against the pre-patch harden copy)
and green after.
Verified end-to-end twice: hook invoked directly, and a real `claude -p` session receiving the block.

## Residue, not fixed here

- The state entry is still keyed on `$PWD` alone with no owner, so an interactive session in a
  checkout the pool is driving is still blocked by the pool's entry — see
  [[2026-08-26-pwd-keyed-gate-false-positive]]. Worktree-per-ticket-run would retire it.
- A blocked session's remedy path invites it to EDIT the shared state file. Two probes did so and both
  touched only their own entry, verified by diff. Structurally it is how a co-located session could
  delete an entry that is not its own.
- The ticket run shares one checkout per repo, so pool-vs-human collision is unguarded.
