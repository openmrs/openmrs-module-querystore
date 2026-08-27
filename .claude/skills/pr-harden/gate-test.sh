#!/bin/bash
# Exercises pr-harden-gate.sh through its real entry point: a temp cwd, a real state file, the
# actual hook. Asserts allow (no "block" on stdout) vs block.
set -uo pipefail
HOOK="${1:?hook path}"
# Resolve to an absolute path and prove it exists BEFORE any case runs. Each case invokes the hook
# after `cd`-ing into a temp worktree, so a relative argument silently stops resolving there: `bash`
# fails, the output is empty, and every case reads as "allow". Measured 2026-08-27 — run from a repo
# root with `.claude/hooks/pr-harden-gate.sh`, this suite reported "passed=8 failed=4" with its four
# block cases inverted (harden's suite reported 8/3; that is its figure, not this one), and a suite
# whose cases all expect `allow` would have reported a clean pass over a hook that never ran once.
case "$HOOK" in
  /*) ;;
  *) HOOK="$(cd "$(dirname "$HOOK")" 2>/dev/null && pwd)/$(basename "$HOOK")" ;;
esac
[ -f "$HOOK" ] || { echo "gate-test: no such hook: $1" >&2; exit 2; }
# The PHYSICAL temp dir. The hook keys on `pwd -P`, so a fixture keyed on the logical path
# (`/var/...` for macOS's `/private/var/...`) writes an entry the hook cannot find — and
# "not found" is its fail-OPEN case, so every block case would silently read as a pass.
TMP=$(cd "$(mktemp -d)" && pwd -P); STATE="$TMP/pr-harden-state.json"; PASS=0; FAIL=0
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

# OWNERSHIP (2026-08-26-pwd-keyed-gate-false-positive.md). The recorded defect verbatim: an interactive
# session stopped by a `phase: building` entry belonging to a live `claude -p /resolve-ticket` run in the
# same checkout. The entry's own `owner` stamp is what answers that; the unattended MARKER answers a
# different question about a different file, and conflating them cost every block path — the third case
# below is that regression, pinned.
sleep 300 >/dev/null 2>&1 &
FOREIGN=$!
disown "$FOREIGN" 2>/dev/null || true
run_case "entry owned by a LIVE foreign session, phase building -> allow" allow \
  "{\"phase\":\"building\",\"blocking\":0,\"pr\":null,\"round\":1,\"ts\":$NOW,\"owner\":$FOREIGN}"
run_case "entry owned by a LIVE foreign session, reviewed+blocking -> allow" allow \
  "{\"phase\":\"reviewed\",\"blocking\":2,\"pr\":9,\"round\":2,\"ts\":$NOW,\"owner\":$FOREIGN}"
run_case "foreign marker but the entry is OURS -> block (contract holds)" block \
  "{\"phase\":\"building\",\"blocking\":0,\"pr\":null,\"round\":1,\"ts\":$NOW,\"owner\":$$}" "$FOREIGN"
run_case "entry owner DEAD -> allow (nobody can advance it)" allow \
  "{\"phase\":\"building\",\"blocking\":0,\"pr\":null,\"round\":1,\"ts\":$NOW,\"owner\":999999}"
run_case "entry UNSTAMPED, phase building -> block (unchanged by ownership)" block \
  "{\"phase\":\"building\",\"blocking\":0,\"pr\":null,\"round\":1,\"ts\":$NOW}"
kill "$FOREIGN" 2>/dev/null

echo "passed=$PASS failed=$FAIL"; rm -rf "$TMP"; [ "$FAIL" -eq 0 ]
