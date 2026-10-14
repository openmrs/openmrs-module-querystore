#!/bin/bash
# Which entries does the pr-harden Stop gate read when the session's directory is not the PR's checkout?
#
#   gate_key_probe.sh [GATE]      GATE defaults to ~/.claude/hooks/pr-harden-gate.sh
#
# Builds a scratch HOME and two scratch git repositories with different HEADs: CHECKOUT (where the PR
# is) and SESSION (where the session was launched). Each case writes one state file, runs the gate from
# one directory, and prints the gate's decision (block, or none) and its exit status. Unstamped entries
# (no owner) and no unattended marker, so the gate holds each entry to the contract.
#   1a  in-flight entry (phase fixing, blocking 1) keyed on CHECKOUT, gate run in CHECKOUT  -> control
#   1b  the same entry, gate run in SESSION
#   2   in-flight entry keyed on SESSION, no shas, gate run in SESSION                    -> control
#   3   terminal entry (reviewed, blocking 0) keyed on SESSION, no shas, gate run in SESSION
#   4   terminal entry keyed on SESSION carrying CHECKOUT's HEAD as reviewed sha, gate run in SESSION
# Exits 1 if a control (1a, 2) does not block, so a gate that never blocks cannot pass for one that
# reads elsewhere. Never touches the real ~/.claude state files: HOME is overridden for every run.
set -u
GATE="${1:-$HOME/.claude/hooks/pr-harden-gate.sh}"
T=$(mktemp -d)
mkdir -p "$T/home/.claude" "$T/checkout" "$T/session"
for d in checkout session; do
  git -C "$T/$d" init -q
  git -C "$T/$d" -c user.email=probe@example.invalid -c user.name=probe commit -q --allow-empty -m "$d"
done
C=$(cd "$T/checkout" && pwd -P); S=$(cd "$T/session" && pwd -P)
CSHA=$(git -C "$C" rev-parse HEAD)
NOW=$(date +%s)
entry() {  # key phase blocking reviewed_sha_or_empty
  local shas='[]'; [ -n "$4" ] && shas="[\"$4\"]"
  printf '{"%s": {"pr": 1, "round": 1, "blocking": %s, "phase": "%s", "ts": %s, "override": false, "awaiting": [], "reviewed_shas": %s, "verified_shas": [], "declined": []}}\n' \
    "$1" "$3" "$2" "$NOW" "$shas" > "$T/home/.claude/pr-harden-state.json"
}
run() {  # label dir
  local out rc
  out=$(cd "$2" && echo '{}' | HOME="$T/home" bash "$GATE"); rc=$?
  if printf '%s' "$out" | grep -q '"decision": "block"'; then d=block; elif [ -z "$out" ]; then d=none; else d=other; fi
  echo "$1: decision=$d exit=$rc"
  eval "R_$1=$d"
}
entry "$C" fixing 1 "";        run 1a "$C"; run 1b "$S"
entry "$S" fixing 1 "";        run 2 "$S"
entry "$S" reviewed 0 "";      run 3 "$S"
entry "$S" reviewed 0 "$CSHA"; run 4 "$S"
rm -rf "$T"
if [ "$R_1a" != block ] || [ "$R_2" != block ]; then echo "CONTROL FAILED: 1a or 2 did not block"; exit 1; fi
