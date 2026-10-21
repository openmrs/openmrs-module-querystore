#!/bin/bash
# gate-await-ttl.sh <path to pr-harden-gate.sh> <path to gate-state>
# Runs the pr gate in a scratch HOME against a resolve-ticket entry (phase `building`, unstamped)
# whose one await is 600 s and then 7200 s old, and prints what the gate decided each time. Then it
# records the same await again through gate-state, as pr-harden's State has an await past the hour
# recorded again once, and prints the awaits' ages and the gate's decision on that, and on the same
# entry less the await just appended. A block prints its systemMessage and the opening of its reason.
set -u
gate="${1:?path to pr-harden-gate.sh}"; gs="${2:?path to gate-state}"
# Absolute before anything moves the cwd, and present: a gate that cannot be run prints nothing,
# which is exactly what an allow prints.
for f in "$gate" "$gs"; do [ -f "$f" ] || { echo "no such file: $f" >&2; exit 2; }; done
gate="$(cd "$(dirname "$gate")" && pwd -P)/$(basename "$gate")"; gs="$(cd "$(dirname "$gs")" && pwd -P)/$(basename "$gs")"
work="$(mktemp -d)"; mkdir -p "$work/home/.claude" "$work/wt"
key="$(cd "$work/wt" && pwd -P)"
state="$work/home/.claude/pr-harden-state.json"
decide() {
  local decision rc
  decision=$(cd "$key" && echo '{}' | HOME="$work/home" bash "$gate"); rc=$?
  [ "$rc" -eq 0 ] || { echo "$1: the gate exited $rc" >&2; exit 3; }
  if [ -z "$decision" ]; then echo "$1: allow"
  else echo "$1: $(printf '%s' "$decision" | python3 -c 'import json,sys; d=json.load(sys.stdin); print(d["decision"], "-", d["systemMessage"], "| reason opens:", d["reason"][:150])')"
  fi
}
for age in 600 7200; do
  now=$(date +%s)
  python3 -I - "$state" "$key" "$now" "$age" <<'EOF'
import json, sys
path, key, now, age = sys.argv[1], sys.argv[2], int(sys.argv[3]), int(sys.argv[4])
json.dump({key: {"pr": None, "round": 1, "blocking": 0, "phase": "building", "ts": now,
                 "override": False, "awaiting": [{"agent": "an agent", "since": now - age}],
                 "reviewed_shas": [], "verified_shas": [], "declined": []}}, open(path, "w"))
EOF
  decide "await ${age}s old"
done
(cd "$key" && HOME="$work/home" python3 "$gs" await "an agent" --only pr) || { echo "gate-state failed" >&2; exit 3; }
python3 -I - "$state" "$key" <<'EOF'
import json, sys, time
e = json.load(open(sys.argv[1]))[sys.argv[2]]
print("awaits now:", ", ".join(f"{a['agent']} {int(time.time()) - a['since']}s old" for a in e["awaiting"]),
      "| owner:", e.get("owner"))
EOF
decide "the same await recorded again"
# The control: the entry exactly as gate-state left it, less the await it appended, so the allow
# above is shown to come from that await and from nothing else the write changed.
python3 -I - "$state" "$key" <<'EOF2'
import json, sys
d = json.load(open(sys.argv[1]))
d[sys.argv[2]]["awaiting"] = d[sys.argv[2]]["awaiting"][:-1]
json.dump(d, open(sys.argv[1], "w"))
EOF2
decide "that entry less the await recorded again"
rm -rf "$work"
