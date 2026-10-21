# `CLAUDE_CODE_RETRY_WATCHDOG` at a claude.ai-style usage limit — 2026-10-21

Claude Code 2.1.306, Linux, unless a row says otherwise. `stub.py` stands in for the Messages API: it
answers every `/v1/messages` request with a 429 carrying `anthropic-ratelimit-unified-status:
rejected`, `anthropic-ratelimit-unified-reset: <epoch>` and `x-should-retry: false` until that reset,
and with the text `STUB-OK` after it. In agent mode (`STUB_MODE=agent`) it serves the FIRST request
before any limit, with a call to the `Agent` tool, so the limit then meets the agent's own request.

Three drivers, each writing its case to `<case>/`:
- `run.sh <case> <seconds to reset> [env…]` — one `claude -p --output-format text` in a clean
  environment (`env -i`, a throwaway `HOME`, a dummy API key, `ANTHROPIC_BASE_URL` at the stub).
  `meta` holds the launch, reset and end as epoch seconds, the exit code and the `claude --version`
  first on the `PATH`; `requests.jsonl` is the stub's log.
- `stream.sh` — the same, but `-p --output-format stream-json --verbose`, the way `pool-run` starts a
  session, with each stream line stamped in seconds from launch (`stream.tsv`).
- `drive.py` — `pool-run`'s real `Session` (loaded read-only from `.claude/pipeline/pool-run`) driven
  against the stub; `result.json` holds its verdict, `session.jsonl` its stream.

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
| pool-timeout (`drive.py`) | the same, both watchdog variables | +100s | quiet window 45s, timeout 60s: three `api_retry` lines, never quiet for 45s; `killed_for` "exceeded the ticket timeout of 1m00s", `paused_for` null, `rate_limit` null |
| pool-outlasts (`drive.py`) | the same | +100s | quiet window 45s, timeout 200s: five `api_retry` lines; 429 at +0.5s and +99.7s, 200 at +100.9s; exit 0 at 101s, never paused or killed |

So with the watchdog on, a request at a usage limit sleeps until the reset the response names and
retries, the agent's requests included, and the run ends normally; without it the first 429 ends the
run. A reset further away than `CLAUDE_CODE_RETRY_WATCHDOG_MAX_USAGE_LIMIT_WAIT_MS` is refused at
once, and the variable is read case-insensitively. On a Claude Code without the watchdog (2.1.42
here; its `cli.js` never names the variable) setting it changes nothing. A headless session waiting
this way writes a stream line every 30s and no `rate_limit_event`, so `pool-run`'s `Session` neither
goes quiet nor learns of the limit: it cannot suspend the session, and its ticket timeout is what
ends it.

`gate-await-ttl.sh` (output in `gate-await-ttl.out`) runs `.claude/hooks/pr-harden-gate.sh` in a
scratch `HOME` against a resolve-ticket entry in phase `building` whose one await is 600s and then
7200s old: the first yield is allowed, the second blocked ("resolve-ticket: mid-run, no PR opened yet
— the run is not finished"). That is the gates' one-hour `AWAIT_TTL`, which a wait at a limit can
outlast while the awaited agent is still alive.

What this does NOT show:
- a real claude.ai limit: the stub stands in for it, under an API key rather than a subscription
  login. The binary's watchdog branch (`h5() && Mmn(e)`, then `Kfr` reading
  `anthropic-ratelimit-unified-reset`) checks neither, which is a reading of the code, not a run;
- an interactive session, and what its screen shows while it waits: every case here is `-p`;
- a wait of hours: the longest here is under two minutes, so whether a long wait is one sleep or
  several is not shown, though the stream shows one retry line every 30s while it lasts;
- a foreground agent: the `Agent` call here ran in the background, the default in this version;
- any Claude Code version other than 2.1.306 and 2.1.42.
