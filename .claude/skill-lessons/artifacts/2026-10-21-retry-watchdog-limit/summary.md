# `CLAUDE_CODE_RETRY_WATCHDOG` at a claude.ai-style usage limit — 2026-10-21

Claude Code 2.1.306, Linux, unless a row says otherwise. `stub.py` stands in for the Messages API:
it answers every `/v1/messages` request with a 429 carrying
`anthropic-ratelimit-unified-status: rejected`, `anthropic-ratelimit-unified-reset: <epoch>` and
`x-should-retry: false` until that reset, and with the text `STUB-OK` after it. In agent mode
(`STUB_MODE=agent`) it serves the FIRST request before any limit, with a call to the `Agent` tool,
so the limit then meets the agent's own request.

Six drivers, each writing its case to `<case>/`:
- `run.sh <case> <seconds to reset> [env…]` — one `claude -p --output-format text` in a clean
  environment (`env -i`, a throwaway `HOME`, a dummy API key, `ANTHROPIC_BASE_URL` at the stub).
  `meta` holds the launch, reset and end as epoch seconds, the exit code and the `claude --version`
  first on the `PATH`; `requests.jsonl` is the stub's log.
- `stream.sh` — the same, but `-p --output-format stream-json --verbose`, the way `pool-run` starts a
  session, with each stream line stamped in seconds from launch (`stream.tsv`); `STUB_MODE=agent` as
  in `run.sh`, and `RUN_TIMEOUT` (default 400) for the seconds `timeout` allows the session. Its
  `meta` records no exit code: the pipeline's status is the stamping loop's.
- `drive.py` — `pool-run`'s real `Session` (loaded read-only from `.claude/pipeline/pool-run`) driven
  against the stub; `result.json` holds its verdict, `session.jsonl` its stream.
- `hooks-probe.sh` — as `run.sh`, with stdin from `/dev/null`, and with a `Stop` and a `StopFailure`
  hook in the throwaway `HOME`'s `settings.json`, each appending its name and the JSON it was handed to
  `hooks.log`. Its `meta` records no end time. `settings-env-probe.sh` is the same with
  `"env": {"CLAUDE_CODE_RETRY_WATCHDOG": "1"}` in that `settings.json` and nothing in the process.
- `probe.py`, run by `probe-run.sh` — `pool-run`'s real `account_limit`, the limit probe a `--work`
  launcher spends, against the stub, in a clean environment plus whatever the case adds; its output
  is the case's `probe.out`.
- `work-carry/drive_carry.py` — a Phase 2 reviewer's driver, run again here: `pool-run`'s real
  `watch_hand_launched` through `pool-test.py`'s own fixtures, a quiet `--work` session whose probes
  are refused twice and then served; its three cases' output is `work-carry/<case>.out`.

`+Ns` below is seconds from launch: a request's `t` in `requests.jsonl` less `START` in `meta` (or
`start` in `result.json`).

| case | env | reset | what happened |
|---|---|---|---|
| control-serving | none | already passed | one request, 200 at +0.6s; `STUB-OK`, exit 0 |
| watchdog-off | none | +90s | one request, 429 at +0.5s; "API Error: Request rejected (429)", exit 1 at +0.6s |
| watchdog-on | `CLAUDE_CODE_RETRY_WATCHDOG=1` | +60s | 429 at +0.6s; next request +59.2s (429: the header's reset is a whole second, so the wait ended just short); 200 at +60.3s; `STUB-OK`, exit 0 |
| watchdog-capped | the same, `…_MAX_USAGE_LIMIT_WAIT_MS=10000` | +90s | one request, 429 at +0.6s; the same API error, exit 1 at +0.6s |
| watchdog-true | `CLAUDE_CODE_RETRY_WATCHDOG=TRUE` | +20s | 429 at +0.5s, 429 at +19.0s, 200 at +20.2s; exit 0 |
| agent-watchdog-on | `CLAUDE_CODE_RETRY_WATCHDOG=1` | +40s | tool call served at +0.6s; the agent's first request (2 messages, a fresh conversation) and the main thread's next (5) both 429 at +0.6s, both again at +39.7s, both 200 at +40.7s; the main thread then answered the agent's completion notice (7 messages, +40.9s); exit 0 |
| agent-watchdog-off | none | +40s | tool call served; the agent's request and the main thread's two all 429 by +0.7s; exit 1 at +0.8s |
| old-cli-watchdog-on | `CLAUDE_CODE_RETRY_WATCHDOG=1`, on Claude Code 2.1.42 (`old-cli-claude.sh`, saved as `claude` in a directory first on `PATH`) | +30s | one request, 429 at +1.9s; "API Error: 429 …", exit 1 at +2.0s: that version has no watchdog, so the variable alone says nothing |
| stream-watchdog-on (`stream.sh`) | `CLAUDE_CODE_RETRY_WATCHDOG=1` | +75s | `system/api_retry` (`error_status` 429, `error` `rate_limit`) at +0s, +30s and +60s, its `retry_delay_ms` counting down 74156, 44156, 14156; the result at +75s; no `rate_limit_event` line at all |
| agent-stall-20s (`stream.sh`, `STUB_MODE=agent`) | `CLAUDE_CODE_RETRY_WATCHDOG=1`, `CLAUDE_ASYNC_AGENT_STALL_TIMEOUT_MS=20000` | +100s | the control: the background agent was reported `failed`, "Agent stalled: no progress for 20s (stream watchdog did not recover)", at +21s, and sent no request after its first; the main thread waited on, 200 at +100.0s, result `success` |
| agent-stall-45s (`stream.sh`, `STUB_MODE=agent`) | the same, `…_STALL_TIMEOUT_MS=45000` | +100s | the agent waited it out: its request and the main thread's 200 at +100.0s, the agent reported `completed` ("STUB-OK") at +100s |
| agent-stall-default (`stream.sh`, `STUB_MODE=agent`, `RUN_TIMEOUT=900`) | `CLAUDE_CODE_RETRY_WATCHDOG=1` alone, so the stall timeout is its default, 600s | +660s | the agent waited past the default: 429 at +1.2s, 200 at +660.0s, reported `completed` ("STUB-OK") at +660s; 23 `api_retry` lines, one every 30s after the first second: the agent's first, counting down from 658746, and 22 of the main thread's, from 658666 |
| hooks-control (`hooks-probe.sh`) | none | already passed | one request, 200 at +0.5s; `STUB-OK`, exit 0; `Stop` ran, `StopFailure` did not |
| hooks-limit (`hooks-probe.sh`) | none | +120s | one request, 429 at +0.6s; the API error, exit 1; `StopFailure` ran, with `"error": "rate_limit"`, and `Stop` did not |
| hooks-watchdog-on (`hooks-probe.sh`) | `CLAUDE_CODE_RETRY_WATCHDOG=1` | +20s | 429 at +0.5s and +19.2s, 200 at +20.4s; `STUB-OK`, exit 0; `Stop` ran, `StopFailure` did not |
| pool-timeout (`drive.py`) | the same, both watchdog variables | +100s | quiet window 45s, timeout 60s: three `api_retry` lines, never quiet for 45s; `killed_for` "exceeded the ticket timeout of 1m00s", `paused_for` null, `rate_limit` null |
| pool-outlasts (`drive.py`) | the same | +100s | quiet window 45s, timeout 200s: five `api_retry` lines; 429 at +0.5s and +99.7s, 200 at +100.9s; exit 0 at 101s, never paused or killed |
| settings-env-watchdog (`settings-env-probe.sh`) | `CLAUDE_CODE_RETRY_WATCHDOG=1` in `settings.json`'s `env`, none in the process | +20s | 429 at +0.5s and +19.1s, 200 at +20.2s; `STUB-OK`, exit 0, `Stop` ran: set there, the watchdog waits whatever the process environment holds |
| pool-stripped (`drive.py`, `Session` dropping the pair) | both watchdog variables passed in | +100s | quiet window 45s, timeout 200s: one request, 429 at +1.0s; the session ended at once, `exit` 1, its result `is_error` with `api_error_status` 429, never killed; the stub sends no `rate_limit_event`, so `rate_limit` and `paused_for` are null |
| probe-before-vars (`probe.py`, `pool-run` before the drop) | both variables in the launcher's environment | +400s | `account_limit` took 305.1s: one 429 at +0.9s, eleven `api_retry` lines, killed at its 300s timeout, answer `refused` |
| probe-after-vars (`probe.py`, `pool-run` with the drop) | the same | +400s | 0.8s: one 429, answer `refused` |
| probe-after-none (`probe.py`, `pool-run` with the drop) | none | +400s | 0.5s: one 429, answer `refused`; the control |

So with the watchdog on, a request at a usage limit sleeps until the reset the response names and
retries, the agent's requests included, and the run ends normally; without it the first 429 ends the
run. A reset further away than `CLAUDE_CODE_RETRY_WATCHDOG_MAX_USAGE_LIMIT_WAIT_MS` is refused at
once, and the variable is read case-insensitively. On a Claude Code without the watchdog (2.1.42
here; its `cli.js` never names the variable) setting it changes nothing. A headless session waiting
this way writes a stream line every 30s, so it neither ends nor goes quiet, the only two points at
which `pool-run`'s `Session` asks whether a limit is in force: it cannot suspend the session, and
the wait ends on its own or at the ticket timeout, whichever comes first (pool-outlasts,
pool-timeout). The stub sends no `rate_limit_event` with the variable or without it (pool-stripped
has none either), so nothing here rests on one.

A turn the limit ends runs the `StopFailure` hooks and not the `Stop` ones (hooks-limit), so a gate,
which is a `Stop` hook, never sees it; under the watchdog the same limit ends nothing, and the turn
that ends after the wait runs `Stop` as a normal one does (hooks-watchdog-on, against the control).

`pool-run` copied its own environment into every headless session it started, so the pair in front
of `pool-run --work`, or exported, reached the launcher's limit probe, which then waited out its
300s timeout instead of being refused (probe-before-vars). `Session` now drops the pair: a session
handed it ends at the limit (pool-stripped) and the probe answers in under a second
(probe-after-vars, against the control). A `--work` session with its carry on is started without the
pair too: in work-carry, once a probe was refused the carry nudged the session as the account served
again, "the claude.ai usage limit that stopped you has reset", with a background agent's transcript
written every half second from the first refusal on (resumed-by-itself) as with nothing written
(control-limit), and sent nothing with no limit (control-no-limit), so under the watchdog the nudge
would land beside an agent that had resumed by itself. A watchdog set in `settings.json`'s `env` is
out of the drop's reach and still waits (settings-env-watchdog, against hooks-limit), so
`pool-run`'s preflight refuses one set in `~/.claude/settings.json` (a project's own settings it
does not read). The stub's console in probe-before-vars shows the same `ConnectionResetError` as
agent-stall-default, on a connection with no request in flight.

The harness reports a background agent `failed` as stalled once it has gone
`CLAUDE_ASYNC_AGENT_STALL_TIMEOUT_MS` without progress; unset, the 2.1.306 binary takes the larger
of the stream idle timeout and 300s, plus 300s, which is 600s unless that timeout is raised. In the
binary each message the agent's query yields re-arms that timer, a few bookkeeping kinds aside, and
the three agent-stall cases agree with a waiting agent's retry, every 30s, being such a message: a
timeout under that interval reported the waiting agent stalled, and one over it, the default
included, let it wait out the limit and complete. They bracket the step rather than show it, since
the stream carries the agent's first retry line only. So with the watchdog on, an agent at a usage
limit is neither reported nor dead until the reset. The stub's own stderr, which `stream.sh` leaves
on the console, printed one `ConnectionResetError` in agent-stall-default while reading a request
line, so on a connection with no request in flight, which the client reset. The request log has
every request answered.

`gate-await-ttl.sh` (output in `gate-await-ttl.out`) runs `.claude/hooks/pr-harden-gate.sh` in a
scratch `HOME` against a resolve-ticket entry in phase `building` whose one await is 600s and then
7200s old: the first yield is allowed, the second blocked ("resolve-ticket: mid-run, no PR opened yet
— the run is not finished"). That is the gates' one-hour `AWAIT_TTL`, which a wait at a limit can
outlast while the awaited agent is still alive.

It then records the same await again with `.claude/pipeline/gate-state await "an agent" --only pr`,
as `pr-harden`'s **State** has it done once for such an agent: the entry holds the 7200s await and a
fresh one, and the yield is allowed. The control, that entry less the fresh await, is blocked again,
so the allow is the await's and not the rest of the write's. Each block's reason now opens with the
await's age and the agents' names, then what pr-harden's State has a run do about an agent that may
still be waiting at a usage limit: nothing in the run's own context marks the hour, because the wait
is inside one request (in agent-watchdog-on the main thread's three requests carry the same five
messages), and only the gate measures an await's age. The output prints the systemMessage and the
reason's opening. The script stops on a missing gate or one that exits non-zero, because a gate that
does not run prints nothing, which is what an allow prints: run from the wrong directory before that
guard, it printed "allow" for all three.

`alias-probe.sh` (output in `alias-probe.out`) defines `resolve-ticket`'s alias in an interactive
bash 5.2 with a probe `claude` first on the `PATH`: `claude --version` through it ran the probe with
both variables set, `1` and `18000000`, and the shell itself was left without them. The control, the
same probe with no alias, saw neither.

What this does NOT show:
- a real claude.ai limit: the stub stands in for it, under an API key rather than a subscription
  login. The binary's watchdog branch (`h5() && Mmn(e)`, then `Kfr` reading
  `anthropic-ratelimit-unified-reset`) checks neither, which is a reading of the code, not a run;
- an interactive session, and what its screen shows while it waits: every case here is `-p`;
- a wait of hours: the longest here is eleven minutes, so whether a long wait is one sleep or
  several is not shown, though the stream shows one retry line every 30s while it lasts;
- a foreground agent: the `Agent` call here ran in the background, the default in this version;
- any Claude Code version other than 2.1.306 and 2.1.42;
- the alias in zsh or any shell but bash: zsh is not installed here.
