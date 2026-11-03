#!/bin/bash
# settings-env-probe.sh <case> <seconds-until-reset> [extra env assignments...]
# As hooks-probe.sh, and its settings.json also carries `"env": {"CLAUDE_CODE_RETRY_WATCHDOG": "1"}`
# while the process environment does not unless the case passes it, to see whether a watchdog set
# there waits at the stub's limit -- which is what a `pool-run` dropping the variable from a
# session's environment cannot undo.
# SETTINGS_WATCHDOG=<value> puts that value there instead of "1", and `none` leaves the key out; the
# variable itself never reaches `claude`.
set -u
here="$(cd "$(dirname "$0")" && pwd)"
label="${1:?label}"; ahead="${2:?seconds until the reset}"; shift 2
case "$label" in */*|.*|"") echo "label must be a plain name" >&2; exit 2 ;; esac
out="$here/$label"; rm -rf "$out"; mkdir -p "$out"
home="$(mktemp -d)"; mkdir -p "$home/.claude"
log="$out/hooks.log"; : > "$log"
python3 -I - "$home/.claude/settings.json" "$log" "${SETTINGS_WATCHDOG:-1}" <<'EOF'
import json, shlex, sys
path, log, value = sys.argv[1], sys.argv[2], sys.argv[3]
def hook(name):
    cmd = f"{{ echo {name}; cat; echo; }} >> {shlex.quote(log)}"
    return [{"hooks": [{"type": "command", "command": cmd}]}]
json.dump({"env": {} if value == "none" else {"CLAUDE_CODE_RETRY_WATCHDOG": value},
           "hooks": {"Stop": hook("Stop"), "StopFailure": hook("StopFailure")}}, open(path, "w"))
EOF
port=$(( 20000 + RANDOM % 20000 ))
start=$(python3 -c 'import time; print(time.time())')
reset=$(python3 -c "print($start + $ahead)")
python3 -I "$here/stub.py" "$port" "$reset" "$out/requests.jsonl" plain &
stub=$!
for _ in $(seq 50); do python3 -c "import socket; socket.create_connection(('127.0.0.1', $port), 0.2)" 2>/dev/null && break; done
( cd "$home" && env -i PATH="$PATH" HOME="$home" TERM=dumb LANG=C.UTF-8 \
    ANTHROPIC_API_KEY=stub-not-a-key ANTHROPIC_BASE_URL="http://127.0.0.1:$port" \
    DISABLE_TELEMETRY=1 CLAUDE_CODE_DISABLE_NONESSENTIAL_TRAFFIC=1 DISABLE_AUTOUPDATER=1 \
    "$@" \
    timeout 400 claude -p "Reply with one word." --output-format text </dev/null >"$out/stdout" 2>"$out/stderr" )
code=$?
kill "$stub" 2>/dev/null; wait "$stub" 2>/dev/null
rm -rf "$home"
printf 'START=%s\nRESET=%s\nEXIT=%s\nCLAUDE=%s\nENV=%s\nSETTINGS_WATCHDOG=%s\n' "$start" "$reset" "$code" \
  "$(claude --version 2>&1 | head -1)" "$*" "${SETTINGS_WATCHDOG:-1}" > "$out/meta"
echo "== $label: reset +${ahead}s · exit $code · ${*:-no extra env}"
echo "   stdout: $(tr '\n' ' ' < "$out/stdout" | cut -c1-120)"
python3 -I - "$log" <<'EOF'
import json, sys
lines = open(sys.argv[1]).read().split("\n")
i = 0
fired = []
while i < len(lines):
    if lines[i] in ("Stop", "StopFailure"):
        name = lines[i]; i += 1
        try:
            d = json.loads(lines[i])
        except (IndexError, ValueError):
            d = {}
        keep = {k: d[k] for k in ("hook_event_name", "error", "stop_hook_active") if k in d}
        if "error_details" in d:
            keep["error_details"] = str(d["error_details"])[:100]
        fired.append(name)
        print(f"   hook {name}: {json.dumps(keep)}")
    i += 1
if not fired:
    print("   no hook fired")
EOF
