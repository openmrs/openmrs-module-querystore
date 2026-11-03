#!/bin/bash
# probe-run.sh <dir> <name> <poolrun copy> <reset-ahead-s> <port> [env...]  -> <dir>/out/<name>.out
# Runs probe.py -- pool-run's real account_limit, the probe a `--work` launcher spends -- in a clean
# environment with HOME at a scratch directory, plus whatever env assignments follow. <dir> holds
# scripts/probe.py and bin/stub.py.
set -u
P="$1"; name="$2"; pr="$3"; ahead="$4"; port="$5"; shift 5
home="$P/home-$name"; rm -rf "$home"; mkdir -p "$home"
( cd "$home" && env -i PATH="$PATH" HOME="$home" TERM=dumb LANG=C.UTF-8 \
    ANTHROPIC_API_KEY=stub-not-a-key DISABLE_TELEMETRY=1 CLAUDE_CODE_DISABLE_NONESSENTIAL_TRAFFIC=1 \
    DISABLE_AUTOUPDATER=1 IS_SANDBOX=1 "$@" \
    python3 -I "$P/scripts/probe.py" "$pr" "$P/bin/stub.py" "$ahead" "$P/case-$name" "$port" ) \
  > "$P/out/$name.out" 2>&1
echo "exit=$?" >> "$P/out/$name.out"
