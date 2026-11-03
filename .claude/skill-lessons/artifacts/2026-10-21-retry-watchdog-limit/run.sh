#!/bin/bash
# run.sh <label> <seconds-until-reset> [extra env assignments...]
# Starts stub.py with its reset that far ahead, runs one headless `claude -p` against it in a clean
# environment (a throwaway HOME, a dummy key, ANTHROPIC_BASE_URL at the stub), and writes the case to
# <label>/ beside this script: meta (launch, reset and end as epoch seconds, the exit code, the
# version of the `claude` first on the PATH, the extra environment), requests.jsonl (the stub's own
# log), stdout and stderr. STUB_MODE=agent serves the first request with a call to the Agent tool.
set -u
here="$(cd "$(dirname "$0")" && pwd)"
label="${1:?label}"; ahead="${2:?seconds until the reset}"; shift 2
case "$label" in */*|.*|"") echo "label must be a plain name" >&2; exit 2 ;; esac
out="$here/$label"; rm -rf "$out"; mkdir -p "$out"
home="$(mktemp -d)"
port=$(( 20000 + RANDOM % 20000 ))
start=$(python3 -c 'import time; print(time.time())')
reset=$(python3 -c "print($start + $ahead)")
python3 -I "$here/stub.py" "$port" "$reset" "$out/requests.jsonl" "${STUB_MODE:-plain}" &
stub=$!
for _ in $(seq 50); do python3 -c "import socket; socket.create_connection(('127.0.0.1', $port), 0.2)" 2>/dev/null && break; done
( cd "$home" && env -i PATH="$PATH" HOME="$home" TERM=dumb LANG=C.UTF-8 \
    ANTHROPIC_API_KEY=stub-not-a-key ANTHROPIC_BASE_URL="http://127.0.0.1:$port" \
    DISABLE_TELEMETRY=1 CLAUDE_CODE_DISABLE_NONESSENTIAL_TRAFFIC=1 DISABLE_AUTOUPDATER=1 \
    "$@" \
    timeout 400 claude -p "Reply with one word." --output-format text >"$out/stdout" 2>"$out/stderr" )
code=$?
end=$(python3 -c 'import time; print(time.time())')
kill "$stub" 2>/dev/null; wait "$stub" 2>/dev/null
rm -rf "$home"
printf 'START=%s\nRESET=%s\nEND=%s\nEXIT=%s\nCLAUDE=%s\nSTUB_MODE=%s\nENV=%s\n' "$start" "$reset" "$end" \
  "$code" "$(claude --version 2>&1 | head -1)" "${STUB_MODE:-plain}" "$*" > "$out/meta"
python3 - "$out" <<'EOF'
import json, pathlib, sys
o = pathlib.Path(sys.argv[1])
meta = dict(l.split("=", 1) for l in o.joinpath("meta").read_text().splitlines())
start = float(meta["START"])
print(f"== {o.name}: {meta['CLAUDE']} · reset +{float(meta['RESET']) - start:.0f}s · exit {meta['EXIT']}"
      f" · wall {float(meta['END']) - start:.1f}s · {meta['ENV'] or 'no extra env'}")
print("   stdout:", o.joinpath("stdout").read_text().strip().replace("\n", " | ")[:160] or "(empty)")
for line in o.joinpath("requests.jsonl").read_text().splitlines():
    r = json.loads(line)
    print(f"   +{r['t'] - start:6.1f}s {r['status']} {r.get('served', '')}msgs={r.get('n_messages')}")
EOF
