#!/bin/bash
# stream.sh <label> <seconds-until-reset> [extra env assignments...]
# As run.sh, but the way pool-run starts a session (`-p --output-format stream-json --verbose`), with
# every stream line stamped by its arrival in seconds from launch in <label>/stream.tsv.
set -u
here="$(cd "$(dirname "$0")" && pwd)"
label="${1:?label}"; ahead="${2:?seconds until the reset}"; shift 2
case "$label" in */*|.*|"") echo "label must be a plain name" >&2; exit 2 ;; esac
out="$here/$label"; rm -rf "$out"; mkdir -p "$out"
home="$(mktemp -d)"
port=$(( 20000 + RANDOM % 20000 ))
start=$(date +%s)
python3 -I "$here/stub.py" "$port" "$(( start + ahead ))" "$out/requests.jsonl" plain &
stub=$!
for _ in $(seq 50); do python3 -c "import socket; socket.create_connection(('127.0.0.1', $port), 0.2)" 2>/dev/null && break; done
( cd "$home" && env -i PATH="$PATH" HOME="$home" TERM=dumb LANG=C.UTF-8 \
    ANTHROPIC_API_KEY=stub-not-a-key ANTHROPIC_BASE_URL="http://127.0.0.1:$port" \
    DISABLE_TELEMETRY=1 CLAUDE_CODE_DISABLE_NONESSENTIAL_TRAFFIC=1 DISABLE_AUTOUPDATER=1 \
    "$@" \
    timeout 400 claude -p "Reply with one word." --output-format stream-json --verbose 2>"$out/stderr" \
    | while IFS= read -r line; do printf '%s\t%s\n' "$(( $(date +%s) - start ))" "$line"; done > "$out/stream.tsv" )
end=$(date +%s)
kill "$stub" 2>/dev/null; wait "$stub" 2>/dev/null
rm -rf "$home"
printf 'START=%s\nRESET=%s\nEND=%s\nCLAUDE=%s\nENV=%s\n' "$start" "$(( start + ahead ))" "$end" \
  "$(claude --version 2>&1 | head -1)" "$*" > "$out/meta"
echo "== $label: reset +${ahead}s · ${*:-no extra env}"
python3 - "$out/stream.tsv" <<'EOF'
import json, sys
for row in open(sys.argv[1]):
    t, line = row.rstrip("\n").split("\t", 1)
    try:
        ev = json.loads(line)
    except ValueError:
        continue
    keep = {k: ev[k] for k in ("attempt", "retry_delay_ms", "error_status", "error") if k in ev}
    if ev.get("type") == "result":
        keep = {"is_error": ev.get("is_error"), "result": str(ev.get("result"))[:40]}
    print(f"   +{t:>3}s {ev.get('type')}/{ev.get('subtype')} {json.dumps(keep)[:160]}")
EOF
