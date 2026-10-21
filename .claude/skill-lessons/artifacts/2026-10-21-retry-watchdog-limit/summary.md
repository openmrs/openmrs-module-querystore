# `CLAUDE_CODE_RETRY_WATCHDOG` at a claude.ai-style usage limit — 2026-10-21

Claude Code 2.1.306, Linux. `stub.py` stands in for the Messages API: it answers every
`/v1/messages` request with a 429 carrying `anthropic-ratelimit-unified-status: rejected`,
`anthropic-ratelimit-unified-reset: <epoch>` and `x-should-retry: false` until that reset, and with
the text `STUB-OK` after it. `run.sh` starts it with the reset that many seconds after launch, then
runs one `claude -p "Reply with one word." --output-format text` against it in a clean environment
(`env -i`, a fresh `HOME`, a dummy API key, `ANTHROPIC_BASE_URL` at the stub). In agent mode
(`STUB_MODE=agent`) the stub serves the FIRST request before any limit, with a call to the `Agent`
tool, so the limit then meets the agent's own request. `+Ns` is seconds from launch, read off the
stub's own log (`<case>/requests.jsonl`), not off the CLI.

| case | env | reset | what happened |
|---|---|---|---|
| control-serving | none | already passed | one request, 200 at +0.5s; `STUB-OK`, exit 0 |
| watchdog-off | none | +90s | one request, 429 at +0.5s; "API Error: Request rejected (429)", exit 1 at +0.6s |
| watchdog-on | `CLAUDE_CODE_RETRY_WATCHDOG=1` | +60s | 429 at +0.5s; next request +59.2s (429: the header's reset is a whole second, so the wait ended just short); 200 at +60.2s; `STUB-OK`, exit 0 |
| watchdog-capped | the same, `…_MAX_USAGE_LIMIT_WAIT_MS=10000` | +90s | one request, 429 at +0.6s; the same API error, exit 1 at +0.6s |
| watchdog-true | `CLAUDE_CODE_RETRY_WATCHDOG=TRUE` | +20s | 429 at +0.5s, 429 at +19.1s, 200 at +20.3s; exit 0 |
| agent-watchdog-on | `CLAUDE_CODE_RETRY_WATCHDOG=1` | +40s | tool call served at +0.6s; the agent's first request (2 messages) and the main thread's next (5) both 429 at +0.6/+0.7s, both again at +39.5s, both 200 at +40.5/+40.6s; the main thread then answered the agent's completion notice; exit 0 |
| agent-watchdog-off | none | +40s | tool call served; the agent's request and the main thread's two all 429 by +0.6s; exit 1 at +0.7s |
| old-cli-watchdog-on | `CLAUDE_CODE_RETRY_WATCHDOG=1`, run on Claude Code 2.1.42 (`old-cli-claude.sh` first on `PATH`) | +30s | one request, 429 at +1.7s; "API Error: 429 …", exit 1 at +1.8s: that version has no watchdog, so the variable alone says nothing |

So with the watchdog on, a request at a usage limit sleeps until the reset the response names and
retries, the agent's requests included, and the run ends normally; without it the first 429 ends the
run. A reset further away than `CLAUDE_CODE_RETRY_WATCHDOG_MAX_USAGE_LIMIT_WAIT_MS` is refused at
once, and the variable is read case-insensitively. On a Claude Code without the watchdog (2.1.42
here; its `cli.js` never names the variable) setting it changes nothing.

What this does NOT show:
- a real claude.ai limit: the stub stands in for it, under an API key rather than a subscription
  login. The binary's watchdog branch (`h5() && Mmn(e)`, then `Kfr` reading
  `anthropic-ratelimit-unified-reset`) checks neither, which is a reading of the code, not a run;
- an interactive session, and what its screen shows while it waits: every case here is `-p`;
- a wait of hours: the longest here is a minute, so whether a long wait is one sleep or several is
  not shown;
- a foreground agent: the `Agent` call here ran in the background, the default in this version;
- any other Claude Code version.
