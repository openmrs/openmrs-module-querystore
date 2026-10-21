#!/bin/bash
# gate-await-ttl.sh <path to pr-harden-gate.sh>
# Runs the pr gate in a scratch HOME against a resolve-ticket entry (phase `building`, unstamped)
# whose one await is 600 s and then 7200 s old, and prints what the gate decided each time.
set -u
gate="${1:?path to pr-harden-gate.sh}"
work="$(mktemp -d)"; mkdir -p "$work/home/.claude" "$work/wt"
key="$(cd "$work/wt" && pwd -P)"
for age in 600 7200; do
  now=$(date +%s)
  python3 -I - "$work/home/.claude/pr-harden-state.json" "$key" "$now" "$age" <<'EOF'
import json, sys
path, key, now, age = sys.argv[1], sys.argv[2], int(sys.argv[3]), int(sys.argv[4])
json.dump({key: {"pr": None, "round": 1, "blocking": 0, "phase": "building", "ts": now,
                 "override": False, "awaiting": [{"agent": "an agent", "since": now - age}],
                 "reviewed_shas": [], "verified_shas": [], "declined": []}}, open(path, "w"))
EOF
  decision=$(cd "$key" && echo '{}' | HOME="$work/home" bash "$gate")
  if [ -z "$decision" ]; then echo "await ${age}s old: allow"
  else echo "await ${age}s old: $(printf '%s' "$decision" | python3 -c 'import json,sys; d=json.load(sys.stdin); print(d["decision"], "-", d["systemMessage"])')"
  fi
done
rm -rf "$work"
