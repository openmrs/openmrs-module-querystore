#!/bin/bash
# Same shape as gate-test.sh, for harden-cycle-gate.sh (harden-state.json, `edits` not `phase`).
set -uo pipefail
HOOK="${1:?hook path}"
# Resolve to an absolute path and prove it exists BEFORE any case runs. Each case invokes the hook
# after `cd`-ing into a temp worktree, so a relative argument silently stops resolving there: `bash`
# fails, the output is empty, and every case reads as "allow" — so a suite whose cases all expect
# `allow` reports a clean pass over a hook that never ran once. Measured 2026-08-27 with the guard
# stripped and a relative path from a repo root: every block case in this suite inverted. No count
# is recorded here on purpose; it went stale in the same commit that first wrote it, when two cases
# were added below. Strip the guard and read the failures.
case "$HOOK" in
  /*) ;;
  *) HOOK="$(cd "$(dirname "$HOOK")" 2>/dev/null && pwd)/$(basename "$HOOK")" ;;
esac
[ -f "$HOOK" ] || { echo "gate-test: no such hook: $1" >&2; exit 2; }
# The PHYSICAL temp dir. The hook keys on `pwd -P`, so a fixture keyed on the logical path
# (`/var/...` for macOS's `/private/var/...`) writes an entry the hook cannot find, and every block
# case here then FAILS loudly — measured, not silently passes; `run_case` compares against each
# case's own expectation. The silent-pass argument belongs to the relative-path guard above, whose
# cases all expect `allow`. Keeping the fixture physical is what makes the symlink case below
# meaningful rather than accidental.
TMP=$(cd "$(mktemp -d)" && pwd -P); PASS=0; FAIL=0; NOW=$(date +%s)
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

# OWNERSHIP (2026-08-26-pwd-keyed-gate-false-positive.md). The question is whose ENTRY this is, which
# the entry's own `owner` stamp answers; the unattended MARKER answers a different question about a
# different file. The first version of this check conflated them, and the third case below is what that
# cost: a live foreign marker allowed every block path, so an interactive cycle in a pool-worked
# checkout lost its own termination contract.
sleep 300 >/dev/null 2>&1 &
FOREIGN=$!
disown "$FOREIGN" 2>/dev/null || true
run_case "entry owned by a LIVE foreign session, edits 3 -> allow" allow \
  "{\"cycle\":2,\"edits\":3,\"ts\":$NOW,\"owner\":$FOREIGN}"
run_case "entry owned by a LIVE foreign session, awaiting -> allow" allow \
  "{\"cycle\":2,\"edits\":3,\"ts\":$NOW,\"owner\":$FOREIGN,\"awaiting\":$AW}"
run_case "foreign marker but the entry is OURS -> block (contract holds)" block \
  "{\"cycle\":2,\"edits\":3,\"ts\":$NOW,\"owner\":$$}" "$FOREIGN"
run_case "entry owner absent from the process table -> allow" allow \
  "{\"cycle\":2,\"edits\":3,\"ts\":$NOW,\"owner\":999999}"
run_case "owner 0 must not disarm the gate (kill -0 0 succeeds)" block \
  "{\"cycle\":2,\"edits\":3,\"ts\":$NOW,\"owner\":0}"
run_case "zero-padded own pid is still OURS -> block" block \
  "{\"cycle\":2,\"edits\":3,\"ts\":$NOW,\"owner\":\"00$$\"}"
run_case "entry UNSTAMPED, edits 3 -> block (unchanged by ownership)" block \
  "{\"cycle\":2,\"edits\":3,\"ts\":$NOW}"
run_case "foreign marker, entry ours, awaiting -> allow (we are attended)" allow \
  "{\"cycle\":2,\"edits\":3,\"ts\":$NOW,\"owner\":$$,\"awaiting\":$AW}" "$FOREIGN"
kill "$FOREIGN" 2>/dev/null

# The MARKER key must be PHYSICAL like the state key, because its writer
# (`pool-run.unattended_marker_path` -> `tenant_key` -> `os.path.realpath`) builds the name that way.
# This case reaches the hook through a SYMLINK to the work dir, so `$PWD` is logical and `pwd -P` is
# not: keyed logically the marker is not found, `UNATTENDED` stays false, and the unattended
# awaiting-yield is allowed — the death the marker exists to prevent. No case here could express this
# before, because the fixture builds `TMP` with `pwd -P` and so made the two keys identical.
run_symlink_case() { # name expected entry_json marker_pid
  local name="$1" expect="$2" entry="$3" mpid="$4" work="$TMP/work" link="$TMP/lnk"
  mkdir -p "$work"; rm -rf "$link" "$TMP/.claude/pipeline/unattended"; ln -s "$work" "$link"
  mkdir -p "$TMP/.claude/pipeline/unattended"
  local mf; mf="$TMP/.claude/pipeline/unattended/$(printf '%s' "$work" | tr '/' '_' | sed 's/^_*//').json"
  jq -n --argjson pid "$mpid" '{pid:$pid,cwd:"x",since:0}' > "$mf"
  jq -n --arg k "$work" --argjson e "$entry" '{($k): $e}' > "$STATE"
  local out; out=$(cd "$link" && HOME="$TMP" bash "$HOOK" 2>/dev/null)
  local got="allow"; grep -q '"block"' <<<"$out" && got="block"
  if [ "$got" = "$expect" ]; then PASS=$((PASS+1)); echo "  ok   $name ($got)"
  else FAIL=$((FAIL+1)); echo "  FAIL $name: expected $expect, got $got"; fi
}
run_symlink_case "cwd reached via symlink: live marker still found -> block" block \
  "{\"cycle\":2,\"edits\":3,\"ts\":$NOW,\"owner\":$$,\"awaiting\":$AW}" $$

echo "passed=$PASS failed=$FAIL"; rm -rf "$TMP"; [ "$FAIL" -eq 0 ]
