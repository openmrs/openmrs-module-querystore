#!/bin/bash
# Exercises pr-harden-gate.sh through its real entry point: a temp cwd, a real state file, the
# actual hook. Asserts allow (no "block" on stdout) vs block.
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
run_case "entry owner absent from the process table -> allow" allow \
  "{\"phase\":\"building\",\"blocking\":0,\"pr\":null,\"round\":1,\"ts\":$NOW,\"owner\":999999}"
run_case "owner 0 must not disarm the gate (kill -0 0 succeeds)" block \
  "{\"phase\":\"building\",\"blocking\":0,\"pr\":null,\"round\":1,\"ts\":$NOW,\"owner\":0}"
run_case "zero-padded own pid is still OURS -> block" block \
  "{\"phase\":\"building\",\"blocking\":0,\"pr\":null,\"round\":1,\"ts\":$NOW,\"owner\":\"00$$\"}"
run_case "a co-located pool run must not make us unattended" allow \
  "{\"phase\":\"reviewed\",\"blocking\":0,\"pr\":9,\"round\":2,\"ts\":$NOW,\"owner\":$$,\"awaiting\":[{\"agent\":\"refute\",\"since\":$NOW}]}" "$FOREIGN"
run_case "entry UNSTAMPED, phase building -> block (unchanged by ownership)" block \
  "{\"phase\":\"building\",\"blocking\":0,\"pr\":null,\"round\":1,\"ts\":$NOW}"
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
  "{\"phase\":\"reviewed\",\"blocking\":0,\"pr\":9,\"round\":2,\"ts\":$NOW,\"owner\":$$,\"awaiting\":[{\"agent\":\"refute\",\"since\":$NOW}]}" $$

# THE EXIT CONDITION IS ABOUT A SHA, NOT ABOUT AN EVENT. A reviewer clears sha N; FINISH then edits,
# commits and pushes sha N+1; "a review round reported zero blocking findings" stays true, because it
# was true of N and nothing makes it false. So the gate allowed a handover whose head no reviewer had
# ever seen — and the records show those final edits are not cosmetic (a whitespace normal form
# defined twice, a carve-out pinned at only two markers, an assertion that pinned nothing). Same hole
# on the verifier side: FINISH runs one on the merging head and its finding has no round to reach.
# These cases pin the head against the last recorded reviewed and verified sha. They need a real git
# worktree, so they get their own directory — every `run_case` case runs in a non-repo `work`, which
# is the fail-open path this block's last case pins.
run_git_case() { # name expected entry_json  (@HEAD@ / @HEAD8@ are substituted with the real head)
  local name="$1" expect="$2" entry="$3" work="$TMP/gitwork"
  rm -rf "$work" "$TMP/.claude/pipeline/unattended"; mkdir -p "$work"
  ( cd "$work" && git init -q . \
      && git -c user.email=t@example.invalid -c user.name=t commit -q --allow-empty -m one \
  ) >/dev/null 2>&1
  local head; head=$(git -C "$work" rev-parse HEAD 2>/dev/null)
  [ -n "$head" ] || { FAIL=$((FAIL+1)); echo "  FAIL $name: fixture has no git head"; return; }
  entry=${entry//@HEAD8@/${head:0:8}}
  local h3="${head:0:3}" alt=a; [ "${h3:0:1}" = "a" ] && alt=b
  entry=${entry//@NOTHEAD3@/$alt${h3:1:2}}
  entry=${entry//@HEADUP@/$(printf '%s' "$head" | tr '[:lower:]' '[:upper:]')}
  entry=${entry//@HEAD@/$head}
  jq -n --arg k "$work" --argjson e "$entry" '{($k): $e}' > "$STATE"
  local out; out=$(cd "$work" && HOME="$TMP" bash "$HOOK" 2>/dev/null)
  local got="allow"; grep -q '"block"' <<<"$out" && got="block"
  if [ "$got" = "$expect" ]; then PASS=$((PASS+1)); echo "  ok   $name ($got)"
  else FAIL=$((FAIL+1)); echo "  FAIL $name: expected $expect, got $got"; fi
}

CONV="\"phase\":\"reviewed\",\"blocking\":0,\"pr\":9,\"round\":2,\"ts\":$NOW,\"owner\":$$"
OTHER="0123456789abcdef0123456789abcdef01234567"

run_git_case "converged ON the reviewed sha -> allow" allow \
  "{$CONV,\"reviewed_shas\":[\"@HEAD@\"]}"
run_git_case "converged, head is NOT the reviewed sha -> block (FINISH edited after the review)" block \
  "{$CONV,\"reviewed_shas\":[\"$OTHER\"]}"
run_git_case "the LAST entry decides, not any entry -> block" block \
  "{$CONV,\"reviewed_shas\":[\"@HEAD@\",\"$OTHER\"]}"
run_git_case "an ABBREVIATED reviewed sha still matches the head -> allow" allow \
  "{$CONV,\"reviewed_shas\":[\"@HEAD8@\"]}"
run_git_case "no reviewed_shas recorded -> allow (fail open)" allow "{$CONV}"
run_git_case "reviewed_shas empty -> allow (fail open)" allow \
  "{$CONV,\"reviewed_shas\":[]}"
run_git_case "verified sha is stale while the review is current -> block" block \
  "{$CONV,\"reviewed_shas\":[\"@HEAD@\"],\"verified_shas\":[\"$OTHER\"]}"
run_git_case "reviewed AND verified on this head -> allow" allow \
  "{$CONV,\"reviewed_shas\":[\"@HEAD@\"],\"verified_shas\":[\"@HEAD@\"]}"
run_git_case "verified_shas present but EMPTY -> allow (no verifier was owed)" allow \
  "{$CONV,\"reviewed_shas\":[\"@HEAD@\"],\"verified_shas\":[]}"
# A recorded value is evidence about the head only if it could BE a sha. Against the unguarded first
# draft all three of these BLOCKED, which breaks the fail-open doctrine the hook is built on. Mutate
# the two halves of `looks_like_sha` and read which case answers: dropping the HEX test reddens the
# BRANCH-name case, dropping the FLOOR reddens the 3-character one. The `HEAD` case is caught by
# either half alone, so it pins the original defect and discriminates neither half -- that is why the
# 3-character record here does NOT match the head, since a matching one allows with or without the
# floor and would have measured nothing. The residue is deliberate and stated in the hook: below the
# floor this check does not bind, because its only writer is `gate-state` and a non-sha there is a
# caller bug rather than a state to design for.
run_git_case "a recorded ref NAME is not evidence -> allow (fail open)" allow \
  "{$CONV,\"reviewed_shas\":[\"HEAD\"]}"
run_git_case "a recorded BRANCH name is not evidence -> allow (fail open)" allow \
  "{$CONV,\"reviewed_shas\":[\"fix/337-slug\"]}"
run_git_case "a 3-character record is below the floor -> allow (it cannot decide anything)" allow \
  "{$CONV,\"reviewed_shas\":[\"@NOTHEAD3@\"]}"
run_git_case "an UPPERCASE record of this head still matches -> allow" allow \
  "{$CONV,\"reviewed_shas\":[\"@HEADUP@\"]}"
run_git_case "a non-sha in verified_shas is not evidence either -> allow" allow \
  "{$CONV,\"reviewed_shas\":[\"@HEAD@\"],\"verified_shas\":[\"HEAD\"]}"
run_git_case "the labelled override still releases a mismatched head -> allow" allow \
  "{\"phase\":\"reviewed\",\"blocking\":0,\"pr\":9,\"round\":2,\"ts\":$NOW,\"owner\":$$,\"override\":true,\"reviewed_shas\":[\"$OTHER\"]}"
# Fail open where the answer cannot be established: `work` is not a git worktree, so there is no head
# to compare and the gate must not hold the session on a comparison it could not make.
run_case "a mismatched reviewed sha OUTSIDE a git worktree -> allow (fail open)" allow \
  "{\"phase\":\"reviewed\",\"blocking\":0,\"pr\":9,\"round\":2,\"ts\":$NOW,\"owner\":$$,\"reviewed_shas\":[\"$OTHER\"]}"

echo "passed=$PASS failed=$FAIL"; rm -rf "$TMP"; [ "$FAIL" -eq 0 ]
