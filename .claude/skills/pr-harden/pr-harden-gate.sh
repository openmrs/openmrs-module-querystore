#!/bin/bash
# Stop-hook gate for the `pr-harden` skill's termination contract.
#
# The contract: a /pr-harden run is complete only when a REVIEW ROUND reports ZERO blocking
# findings. Non-blocking findings do not extend the loop (the fixer applies them anyway — that is
# the point of separating the fixer's scope from the loop's exit condition), and edit counts are
# irrelevant here: unlike /harden, every round is expected to make edits, so "the cycle changed
# nothing" can never be the condition. Only the reviewer's blocking count can end the run, and the
# reviewer that produced it must have been a fresh agent — see the skill.
#
# Contract with the skill: it writes an entry keyed by the repo it is working in:
#
#   { "/abs/path/to/repo": { "pr": 93, "round": 3, "blocking": 2, "phase": "reviewed",
#                            "ts": 1755400000, "override": false,
#                            "awaiting": [ { "agent": "review r3", "since": 1755400000 } ] } }
#
# `awaiting` is what makes an unattended run possible at all. Every phase of this pipeline delegates
# to a background subagent — the refutation gate, and each round's reviewer, fixer and verifier — and
# while one is outstanding the orchestrator has NOTHING to do but yield. Without this field a run
# waiting correctly is indistinguishable from a run that quit, and the gate blocks the former: the
# design assumed synchronous phases and every real phase is asynchronous. So a non-empty, fresh
# `awaiting` allows the yield. That is not a concession — the harness re-invokes the orchestrator when
# the agent completes, so yielding mid-await does not end the run, it is how the run proceeds.
#
# The obligation this puts on the skill: CLEAR `awaiting` the moment a result arrives. An entry left
# behind would let the run stop for real, which is the one thing this hook exists to prevent — so the
# allow is bounded by AWAIT_TTL as well, and an agent that has not returned within it is treated as
# dead rather than outstanding.
#
# awaiting non-empty, fresh -> a background agent this run delegated to is outstanding; ALLOW the
#                              yield, whatever the phase says. Bounded by AWAIT_TTL.
# phase "building"      -> a resolve-ticket run is in flight and has not opened its PR yet; block.
# phase "init"/"fixing" -> a run is in flight and no clean review has been recorded yet; block.
# phase "reviewed", blocking > 0  -> another round is required; block.
# phase "reviewed", blocking == 0 -> converged; allow.
# override == true                -> the skill took the labelled override; allow (on the record).
# no entry                        -> no pr-harden run in flight here; allow.
#
# The `phase` field is what closes the hole the /harden gate leaves open: an entry written before
# the first review still blocks, so a run cannot end by never having reviewed at all.
#
# FAIL OPEN, ALWAYS. A gate that wedges every future turn in every repo is far worse than one that
# occasionally lets an early stop through, so every ambiguous case allows the stop: no state file,
# unreadable or malformed JSON, no jq, no entry for this directory, a missing or unparseable
# blocking count, an unrecognised phase, or an entry older than STALE_AFTER (a run that was
# abandoned, crashed, or /clear-ed). Only a present, fresh, parseable entry that explicitly says
# "not converged" blocks.

set -uo pipefail

STATE="$HOME/.claude/pr-harden-state.json"
STALE_AFTER=21600   # 6h; a pr-harden run older than this is abandoned, not in flight
AWAIT_TTL=3600      # 1h; an awaited subagent that has not returned in this long is dead, not running.
                    # Generous on purpose: a fixer runs a full root `mvn -o clean install` and a
                    # verifier restarts a standalone and drives a real query. Still far under
                    # STALE_AFTER, so a forgotten `awaiting` cannot outlive the run that wrote it.

allow() { exit 0; }

[ -f "$STATE" ] || allow
command -v jq >/dev/null 2>&1 || allow

KEY="$PWD"

ENTRY=$(jq -c --arg k "$KEY" '.[$k] // empty' "$STATE" 2>/dev/null) || allow
[ -n "$ENTRY" ] || allow

OVERRIDE=$(jq -r '.override // false' <<<"$ENTRY" 2>/dev/null) || allow
[ "$OVERRIDE" = "true" ] && allow

TS=$(jq -r '.ts // 0' <<<"$ENTRY" 2>/dev/null) || allow
case "$TS" in ''|*[!0-9]*) allow ;; esac
NOW=$(date +%s)
[ "$((NOW - TS))" -lt "$STALE_AFTER" ] || allow

# A background agent this run is waiting on. Checked before the phase switch on purpose: a yield is
# equally correct whether the awaited agent is the refutation gate (phase "building"), a reviewer
# (phase "init"/"fixing") or a fixer spawned after a review that found something (phase "reviewed",
# blocking > 0). Fail open on anything unparseable, like every other check here.
AWAITING=$(jq -r '[(.awaiting // [])[] | (.since // 0)] | length' <<<"$ENTRY" 2>/dev/null) || allow
case "$AWAITING" in ''|*[!0-9]*) AWAITING=0 ;; esac
if [ "$AWAITING" -gt 0 ]; then
  NEWEST=$(jq -r '[(.awaiting // [])[] | (.since // 0)] | max' <<<"$ENTRY" 2>/dev/null) || allow
  case "$NEWEST" in ''|*[!0-9]*) NEWEST=0 ;; esac
  [ "$((NOW - NEWEST))" -lt "$AWAIT_TTL" ] && allow
fi

PHASE=$(jq -r '.phase // empty' <<<"$ENTRY" 2>/dev/null) || allow
PR=$(jq -r '.pr // "?"' <<<"$ENTRY" 2>/dev/null)
ROUND=$(jq -r '.round // "?"' <<<"$ENTRY" 2>/dev/null)

case "$PHASE" in
  building)
    jq -n --arg r "$ROUND" '{
      decision: "block",
      reason: ("resolve-ticket is mid-run and has not opened its pull request yet, so no review round "
        + "can have reported zero blocking findings. Continue the phases in the resolve-ticket skill "
        + "— plan, refutation gate, failing test, implementation, root mvn install, harden, draft PR "
        + "— and then invoke pr-harden, which owns everything from round 1. Do NOT hand back to the "
        + "user and do NOT ask whether to continue; if you are aborting, take one of the five labelled "
        + "abort conditions in the skill'"'"'s autonomy contract and set override:true in "
        + "~/.claude/pr-harden-state.json with its reason, so the deviation is on the record."),
      systemMessage: "resolve-ticket: mid-run, no PR opened yet — the run is not finished"
    }'
    exit 0
    ;;
  init|fixing)
    jq -n --arg p "$PR" --arg r "$ROUND" --arg ph "$PHASE" '{
      decision: "block",
      reason: ("pr-harden termination contract: a run on PR #" + $p + " is in flight (round " + $r
        + ", phase " + $ph + ") and no review round has yet reported zero blocking findings. The run "
        + "ends on a REVIEW, never on a fix: spawn a fresh reviewer agent (a new subagent — never "
        + "subagent_type \"fork\", which would inherit this context and defeat the whole point), "
        + "record its blocking count, and continue the loop. Do NOT hand back to the user and do "
        + "NOT ask whether to continue; if you are deliberately stopping early, take the labelled "
        + "override in the skill'"'"'s Termination section and set override:true in "
        + "~/.claude/pr-harden-state.json so the deviation is on the record."),
      systemMessage: ("pr-harden: PR #" + $p + " round " + $r + " is mid-flight (" + $ph
        + ") — no clean review recorded yet")
    }'
    exit 0
    ;;
  reviewed) ;;
  *) allow ;;
esac

BLOCKING=$(jq -r '.blocking // empty' <<<"$ENTRY" 2>/dev/null) || allow
case "$BLOCKING" in ''|*[!0-9]*) allow ;; esac
[ "$BLOCKING" -gt 0 ] || allow

# Present, fresh, and the last review found blocking findings: another round is owed.
jq -n --arg p "$PR" --arg r "$ROUND" --arg b "$BLOCKING" '{
  decision: "block",
  reason: ("pr-harden termination contract: round " + $r + " on PR #" + $p + " reported " + $b
    + " blocking finding(s), so it was not the last round. Apply that round'"'"'s findings — all of "
    + "them, blocking and non-blocking alike — commit and push to the PR branch, then run round "
    + (($r|tonumber?) + 1 | tostring) + ": a FRESH reviewer agent (a new subagent, never "
    + "subagent_type \"fork\") over the pushed head. Do NOT hand back to the user and do NOT ask "
    + "whether to continue; if you are deliberately stopping early, take the labelled override in "
    + "the skill'"'"'s Termination section and set override:true in "
    + "~/.claude/pr-harden-state.json so the deviation is on the record."),
  systemMessage: ("pr-harden: PR #" + $p + " round " + $r + " found " + $b
    + " blocking finding(s) — another round is required")
}'
exit 0
