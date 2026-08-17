#!/bin/bash
# Stop-hook gate for the `harden` skill's termination contract.
#
# The contract: /harden is complete only when one full Phase 1 + Phase 2 cycle produces ZERO edits.
# The skill states that four times and it still got broken, because nothing outside the model
# enforced it. This does.
#
# Contract with the skill: at the end of every cycle it writes an entry to the state file below,
# keyed by the repo it is hardening:
#
#   { "/abs/path/to/repo": { "cycle": 4, "edits": 3, "ts": 1755400000, "override": false } }
#
# edits > 0        -> another cycle is required; this hook blocks the turn from ending.
# edits == 0       -> converged; allow.
# override == true -> the skill took the labelled override; allow (the deviation is on the record).
# no entry         -> no harden run in flight here; allow.
#
# FAIL OPEN, ALWAYS. A gate that wedges every future turn in every repo is far worse than one that
# occasionally lets an early stop through, so every ambiguous case allows the stop: no state file,
# unreadable or malformed JSON, no jq, no entry for this directory, a missing or unparseable edits
# count, or an entry older than STALE_AFTER (a run that was abandoned, crashed, or /clear-ed). Only an
# entry that is present, fresh, parseable and explicitly says edits > 0 blocks.

set -uo pipefail

STATE="$HOME/.claude/harden-state.json"
STALE_AFTER=21600   # 6h; a harden run older than this is abandoned, not in flight

allow() { exit 0; }

[ -f "$STATE" ] || allow
command -v jq >/dev/null 2>&1 || allow

# The repo being hardened is the directory the session is running in.
KEY="$PWD"

ENTRY=$(jq -c --arg k "$KEY" '.[$k] // empty' "$STATE" 2>/dev/null) || allow
[ -n "$ENTRY" ] || allow

OVERRIDE=$(jq -r '.override // false' <<<"$ENTRY" 2>/dev/null) || allow
[ "$OVERRIDE" = "true" ] && allow

TS=$(jq -r '.ts // 0' <<<"$ENTRY" 2>/dev/null) || allow
case "$TS" in ''|*[!0-9]*) allow ;; esac
NOW=$(date +%s)
[ "$((NOW - TS))" -lt "$STALE_AFTER" ] || allow

EDITS=$(jq -r '.edits // empty' <<<"$ENTRY" 2>/dev/null) || allow
case "$EDITS" in ''|*[!0-9]*) allow ;; esac
[ "$EDITS" -gt 0 ] || allow

CYCLE=$(jq -r '.cycle // "?"' <<<"$ENTRY" 2>/dev/null)

# Present, fresh, and edits were made: the contract requires another cycle. `decision: block` on a
# Stop hook feeds the reason back and keeps the turn going rather than ending it.
jq -n --arg c "$CYCLE" --arg e "$EDITS" '{
  decision: "block",
  reason: ("harden termination contract: cycle " + $c + " made " + $e + " edit(s), so it was not the "
    + "last cycle. Run cycle " + (($c|tonumber?) + 1 | tostring) + " — Phase 1 then Phase 2 — and "
    + "record its measured edit count. Do NOT hand back to the user and do NOT ask whether to "
    + "continue; if you are deliberately stopping early, take the labelled override in the skill'"'"'s "
    + "Termination section and set override:true in ~/.claude/harden-state.json so the deviation is "
    + "on the record."),
  systemMessage: ("harden: cycle " + $c + " made " + $e + " edit(s) — another cycle is required")
}'
exit 0
