# Headless `claude -p`: what happens to background work when the turn ends — 2026-09-25

Claude Code 2.1.282, macOS. The driver's flags (`pool-run`:2668-2673: `-p … --output-format stream-json
--verbose --dangerously-skip-permissions --session-id <uuid>`), stdin from `/dev/null`, and each stdout
line time-stamped by `run.sh`. Each session was told to start the work in the background, end its turn
with "waiting", and answer "resumed" if re-invoked. `+Ns` is seconds from launch; `meta` holds the epoch
start and end, `err` the stderr, and `out.tsv` the time-stamped stream.

| session | background work | ceiling env | what happened |
|---|---|---|---|
| a | Bash `sleep 900` | default | turn ended +6s; task `killed` +11s; process exit 0 at +12s; not re-invoked |
| b | Bash `sleep 60` | default | the same: `killed` +11s, exit +12s |
| d | Bash `sleep 900` | `CLAUDE_CODE_PRINT_BG_WAIT_CEILING_MS=0` | the same: `killed` +11s, exit +12s; the variable does not keep a Bash task |
| c | agent (its `sleep 90` was refused, so it returned in ~10s) | default | waited; second `system init` at +15s; "resumed" at +20s |
| e | agent, a ~180s bounded loop | default | waited; agent `completed` +196s; re-invoked, "resumed" at +198s |
| f | agent, ~720s in two bounded loops | default | agent `killed` at +608s; stderr "Background tasks still running after 600s; terminating. Set CLAUDE_CODE_PRINT_BG_WAIT_CEILING_MS=0 to wait indefinitely."; exit 0; not re-invoked |
| g | the same as f | `CLAUDE_CODE_PRINT_BG_WAIT_CEILING_MS=0` | waited; agent `completed` +742s; re-invoked, "resumed" at +745s |

So in print mode, a `run_in_background` Bash command is killed within seconds of the turn's end when
nothing else is outstanding, and a background agent is waited for up to 600 s and re-invokes the session if it finishes inside that. The 2.1.282
binary carries the logic: a wind-down deadline with `holdForArmedMonitors` and `ceilingExceeded`, the
stderr line above, and `tengu_print_ceiling_stop_agents`.

What this does NOT show:
- a `Monitor` (not driven here);
- a Bash task running BESIDE an agent. By a code reading of the 2.1.282 binary (a lead, not run),
  the wind-down skips the sweep while an agent or an armed `Monitor` is outstanding;
- session d's environment, which `run.sh` does not record. Its `CLAUDE_CODE_PRINT_BG_WAIT_CEILING_MS=0`
  is in the launch command, in the retro session's transcript;
- whether another harness version differs;
- anything about an ATTENDED session.

Redacted: each session's `system`/`init` record in `out.tsv` is replaced by a stub, because it
lists the machine's tools, MCP servers and account connectors. Nothing else was changed.
