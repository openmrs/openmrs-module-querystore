#!/bin/bash
# Exercises pr-harden-gate.sh through its real entry point: a temp cwd, a real state file, the
# actual hook. Asserts allow (no "block" on stdout) vs block.
set -uo pipefail
HOOK="${1:?hook path}"
TMP=$(mktemp -d); STATE="$TMP/pr-harden-state.json"; PASS=0; FAIL=0
NOW=$(date +%s)

run_case() { # name expected entry_json [marker_pid]
  local name="$1" expect="$2" entry="$3" mpid="${4:-}" work="$TMP/work"
  mkdir -p "$work"
  rm -rf "$TMP/.claude/pipeline/unattended"
  if [ -n "$mpid" ]; then
    mkdir -p "$TMP/.claude/pipeline/unattended"
    local mf; mf="$TMP/.claude/pipeline/unattended/$(printf '%s' "$work" | tr '/' '_' | sed 's/^_*//').json"
    jq -n --argjson pid "$mpid" '{pid:$pid,cwd:"x",since:0}' > "$mf"
  fi
  if [ "$entry" = "none" ]; then echo '{}' > "$STATE"
  else jq -n --arg k "$work" --argjson e "$entry" '{($k): $e}' > "$STATE"; fi
  local out; out=$(cd "$work" && HOME="$TMP" bash "$HOOK" 2>/dev/null)
  # the hook reads $HOME/.claude/pr-harden-state.json
  local got="allow"; grep -q '"block"' <<<"$out" && got="block"
  if [ "$got" = "$expect" ]; then PASS=$((PASS+1)); echo "  ok   $name ($got)"
  else FAIL=$((FAIL+1)); echo "  FAIL $name: expected $expect, got $got"; fi
}

mkdir -p "$TMP/.claude"; STATE="$TMP/.claude/pr-harden-state.json"

run_case "no entry -> allow" allow none
run_case "reviewed, blocking 0 -> allow" allow \
  "{\"phase\":\"reviewed\",\"blocking\":0,\"pr\":9,\"round\":2,\"ts\":$NOW}"
run_case "building, no awaiting -> block" block \
  "{\"phase\":\"building\",\"blocking\":0,\"pr\":null,\"round\":1,\"ts\":$NOW,\"awaiting\":[]}"
run_case "awaiting fresh, attended -> allow (yield)" allow \
  "{\"phase\":\"building\",\"blocking\":0,\"pr\":null,\"round\":1,\"ts\":$NOW,\"awaiting\":[{\"agent\":\"refute\",\"since\":$NOW}]}"
run_case "awaiting fresh, UNATTENDED -> block (no next turn)" block \
  "{\"phase\":\"building\",\"blocking\":0,\"pr\":null,\"round\":1,\"ts\":$NOW,\"unattended\":true,\"awaiting\":[{\"agent\":\"refute\",\"since\":$NOW}]}"
run_case "awaiting STALE, unattended -> block" block \
  "{\"phase\":\"building\",\"blocking\":0,\"pr\":null,\"round\":1,\"ts\":$NOW,\"unattended\":true,\"awaiting\":[{\"agent\":\"refute\",\"since\":$((NOW-7200))}]}"
run_case "override -> allow" allow \
  "{\"phase\":\"building\",\"override\":true,\"pr\":null,\"round\":1,\"ts\":$NOW}"

AWAIT_ENTRY="{\"phase\":\"building\",\"blocking\":0,\"pr\":null,\"round\":1,\"ts\":$NOW,\"awaiting\":[{\"agent\":\"refute\",\"since\":$NOW}]}"
DEAD=999999
run_case "marker with LIVE pid -> block (driver is here)" block "$AWAIT_ENTRY" $$
run_case "marker with DEAD pid -> allow (stale marker inert)" allow "$AWAIT_ENTRY" $DEAD
run_case "marker live, but phase reviewed+0 -> allow" allow \
  "{\"phase\":\"reviewed\",\"blocking\":0,\"pr\":9,\"round\":2,\"ts\":$NOW}" $$

echo "passed=$PASS failed=$FAIL"; rm -rf "$TMP"; [ "$FAIL" -eq 0 ]
