#!/bin/bash
# run.sh <label> <seconds-until-reset> [extra env assignments...]
# Starts the stub with its reset that far ahead, runs one headless `claude -p` against it in a clean
# environment, and prints the CLI's exit, output and wall time, then every request the stub saw with
# its arrival relative to the run's start.
set -u
here="$(cd "$(dirname "$0")" && pwd)"
label="$1"; ahead="$2"; shift 2
out="$here/out/$label"; rm -rf "$out"; mkdir -p "$out/home"
port=$(( 20000 + RANDOM % 20000 ))
start=$(python3 -c 'import time; print(time.time())')
reset=$(python3 -c "print($start + $ahead)")
python3 -I "$here/stub.py" "$port" "$reset" "$out/requests.jsonl" "${STUB_MODE:-plain}" &
stub=$!
for _ in $(seq 50); do python3 -c "import socket; socket.create_connection(('127.0.0.1', $port), 0.2)" 2>/dev/null && break; done
cd "$out/home"
env -i PATH="$PATH" HOME="$out/home" TERM=dumb LANG=C.UTF-8 \
  ANTHROPIC_API_KEY=stub-not-a-key ANTHROPIC_BASE_URL="http://127.0.0.1:$port" \
  DISABLE_TELEMETRY=1 CLAUDE_CODE_DISABLE_NONESSENTIAL_TRAFFIC=1 DISABLE_AUTOUPDATER=1 \
  "$@" \
  timeout 400 claude -p "Reply with one word." --output-format text >"$out/stdout" 2>"$out/stderr"
code=$?
end=$(python3 -c 'import time; print(time.time())')
kill "$stub" 2>/dev/null; wait "$stub" 2>/dev/null
python3 - "$out" "$start" "$end" "$code" "$ahead" "$label" <<'EOF'
import json, sys, pathlib
out, start, end, code, ahead, label = sys.argv[1], float(sys.argv[2]), float(sys.argv[3]), sys.argv[4], sys.argv[5], sys.argv[6]
o = pathlib.Path(out)
print(f"== {label}: reset {ahead}s after start · exit {code} · wall {end - start:.1f}s")
print("stdout:", o.joinpath("stdout").read_text().strip()[:300] or "(empty)")
err = o.joinpath("stderr").read_text().strip()
print("stderr:", err[-400:] if err else "(empty)")
reqs = [json.loads(l) for l in o.joinpath("requests.jsonl").read_text().splitlines()] if o.joinpath("requests.jsonl").exists() else []
print(f"requests seen by the stub: {len(reqs)}")
for r in reqs:
    print(f"  +{r["t"] - start:7.1f}s {r["method"]} {r["path"]} -> {r["status"]} {r.get("served","")} msgs={r.get("n_messages")} last={r.get("last","")!r}"
          + (f" stream={r.get('stream')} max_tokens={r.get('max_tokens')}" if 'stream' in r else ""))
EOF
