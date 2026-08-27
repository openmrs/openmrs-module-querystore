#!/bin/bash
# Same shape as gate-test.sh, for harden-cycle-gate.sh (harden-state.json, `edits` not `phase`).
set -uo pipefail
HOOK="${1:?hook path}"
# Resolve to an absolute path and prove it exists BEFORE any case runs. Each case invokes the hook
# after `cd`-ing into a temp worktree, so a relative argument silently stops resolving there: `bash`
# fails, the output is empty, and every case reads as "allow". Measured 2026-08-27 — run from a repo
# root with `.claude/hooks/harden-cycle-gate.sh`, this suite reported "passed=8 failed=3" with the
# three block cases inverted, and a suite whose cases all expect `allow` would have reported a clean
# pass over a hook that never ran once.
case "$HOOK" in
  /*) ;;
  *) HOOK="$(cd "$(dirname "$HOOK")" 2>/dev/null && pwd)/$(basename "$HOOK")" ;;
esac
[ -f "$HOOK" ] || { echo "gate-test: no such hook: $1" >&2; exit 2; }
TMP=$(mktemp -d); PASS=0; FAIL=0; NOW=$(date +%s)
mkdir -p "$TMP/.claude"; STATE="$TMP/.claude/harden-state.json"

run_case() { # name expected entry_json [marker_pid]
  local name="$1" expect="$2" entry="$3" mpid="${4:-}" work="$TMP/work"
  mkdir -p "$work"; rm -rf "$TMP/.claude/pipeline/unattended"
  if [ -n "$mpid" ]; then
    mkdir -p "$TMP/.claude/pipeline/unattended"
    local mf; mf="$TMP/.claude/pipeline/unattended/$(printf '%s' "$work" | tr '/' '_' | sed 's/^_*//').json"
    jq -n --argjson pid "$mpid" '{pid:$pid,cwd:"x",since:0}' > "$mf"
  fi
  if [ "$entry" = "none" ]; then echo '{}' > "$STATE"
  else jq -n --arg k "$work" --argjson e "$entry" '{($k): $e}' > "$STATE"; fi
  local out; out=$(cd "$work" && HOME="$TMP" bash "$HOOK" 2>/dev/null)
  local got="allow"; grep -q '"block"' <<<"$out" && got="block"
  if [ "$got" = "$expect" ]; then PASS=$((PASS+1)); echo "  ok   $name ($got)"
  else FAIL=$((FAIL+1)); echo "  FAIL $name: expected $expect, got $got"; fi
}

AW="[{\"agent\":\"phase2 quality\",\"since\":$NOW}]"
run_case "no entry -> allow" allow none
run_case "edits 0 -> allow (converged)" allow "{\"cycle\":2,\"edits\":0,\"ts\":$NOW}"
run_case "edits 3, no awaiting -> block" block "{\"cycle\":2,\"edits\":3,\"ts\":$NOW}"
run_case "awaiting fresh, attended -> allow (yield)" allow "{\"cycle\":2,\"edits\":3,\"ts\":$NOW,\"awaiting\":$AW}"
run_case "awaiting fresh, marker LIVE -> block" block "{\"cycle\":2,\"edits\":3,\"ts\":$NOW,\"awaiting\":$AW}" $$
run_case "awaiting fresh, marker DEAD -> allow" allow "{\"cycle\":2,\"edits\":3,\"ts\":$NOW,\"awaiting\":$AW}" 999999
run_case "awaiting fresh, unattended FIELD -> block" block "{\"cycle\":2,\"edits\":3,\"ts\":$NOW,\"unattended\":true,\"awaiting\":$AW}"
run_case "marker live but edits 0 -> allow" allow "{\"cycle\":2,\"edits\":0,\"ts\":$NOW}" $$
run_case "override -> allow" allow "{\"cycle\":2,\"edits\":3,\"ts\":$NOW,\"override\":true}"

# OWNERSHIP (2026-08-26-pwd-keyed-gate-false-positive.md). A LIVE marker whose pid is not an ancestor
# of the hook is a co-located run that owns this checkout and is not us. Both cases below BLOCK without
# the ownership check — that is what they are for; the `marker LIVE` cases above pass either way,
# because the harness passes its own $$, which IS an ancestor.
sleep 300 >/dev/null 2>&1 &
FOREIGN=$!
disown "$FOREIGN" 2>/dev/null || true
run_case "marker live but FOREIGN owner, edits 3 -> allow" allow "{\"cycle\":2,\"edits\":3,\"ts\":$NOW}" "$FOREIGN"
run_case "marker live but FOREIGN owner, awaiting -> allow" allow "{\"cycle\":2,\"edits\":3,\"ts\":$NOW,\"awaiting\":$AW}" "$FOREIGN"
kill "$FOREIGN" 2>/dev/null

echo "passed=$PASS failed=$FAIL"; rm -rf "$TMP"; [ "$FAIL" -eq 0 ]
