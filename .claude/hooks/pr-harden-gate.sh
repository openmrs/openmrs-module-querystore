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
# THAT PREMISE HOLDS ONLY FOR AN ATTENDED SESSION, and taking it as universal is what let two
# unattended runs die here. Measured 2026-08-26: a `claude -p` process exits when its turn ends, so
# nothing re-invokes it and the yield IS the death. Issue #297 wrote
# `awaiting=[{agent: "refute plan #297 pass 1"}]`, narrated "dispatched the refutation gate. Here is
# where things stand", and ended — 51 turns, no PR, its plan and reproduction discarded; the gate
# allowed it, silently, because allowing is exit 0. Issue #310 died with the same signature in
# /harden pass 3, at 1365 turns and $76.72. So the allow is now scoped to attended sessions and an
# unattended yield is blocked with an instruction to collect the agent in-turn. Hooks DO reach `-p`
# sessions — probed the same day, feedback delivered and captured in the stream — so the absence of
# any gate text in those two logs was never evidence that the hook had not run.
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

# The PHYSICAL path, symlinks resolved. `$PWD` is LOGICAL, and every writer of this file keys on
# the resolved cwd — so with a symlink anywhere in the path (`/tmp` on macOS, a symlinked home) the
# two disagreed and no entry was found, which is this hook's fail-OPEN case: a run with findings
# outstanding could stop and nothing would say why. Resolve on both sides or neither.
KEY="$(pwd -P)"

ENTRY=$(jq -c --arg k "$KEY" '.[$k] // empty' "$STATE" 2>/dev/null) || allow
[ -n "$ENTRY" ] || allow

OVERRIDE=$(jq -r '.override // false' <<<"$ENTRY" 2>/dev/null) || allow
[ "$OVERRIDE" = "true" ] && allow

TS=$(jq -r '.ts // 0' <<<"$ENTRY" 2>/dev/null) || allow
case "$TS" in ''|*[!0-9]*) allow ;; esac
NOW=$(date +%s)
[ "$((NOW - TS))" -lt "$STALE_AFTER" ] || allow

# Is PID an ancestor of this hook process? 0 = yes, 1 = the walk reached the top without meeting it
# (positively somebody else's), 2 = could not be established. The three stay distinct because only 1
# may relax anything. Callers validate PID first; the numeric guard is belt-and-braces for a later one.
owns_this_session() {
  local target="$1" p=$$ up depth=0
  case "$target" in ''|*[!0-9]*) return 2 ;; esac
  while [ "$depth" -lt 40 ]; do
    [ "$p" = "$target" ] && return 0
    up=$(ps -o ppid= -p "$p" 2>/dev/null | tr -d '[:space:]')
    case "$up" in
      ''|*[!0-9]*) return 2 ;;   # ps told us nothing usable — indeterminate, never "somebody else's"
      0|1) return 1 ;;          # reached the top without meeting the target
    esac
    p="$up"; depth=$((depth + 1))
  done
  return 2
}

# WHOSE ENTRY IS THIS? Measured live 2026-08-26
# (~/.claude/skill-lessons/2026-08-26-pwd-keyed-gate-false-positive.md): this state is keyed on the
# CHECKOUT, so an interactive session opened in a checkout the pool was working was stopped by an entry
# belonging to a different live `claude -p /resolve-ticket` run. The entry was present, fresh and
# parseable, so every fail-open case here correctly declined to cover it — they enumerate the cases
# where the ENTRY is unusable, never the case where it is perfectly good and somebody else's. Both
# remedies the block then offers damage the owner: `override: true` disarms the live run's gate for the
# rest of its life, and "continue the phases" puts a second session in one worktree.
#
# So the skill stamps `owner` with its own claude pid (`$PPID` from a tool shell IS that process, and
# the hook is a child of it, so the ancestry test above answers "did I write this entry").
#
# ASK IT OF THE ENTRY, NEVER OF THE UNATTENDED MARKER. The first version of this check inferred entry
# ownership from marker ownership, and review measured what that costs: a live foreign marker allowed
# EVERY block path, so an interactive `/harden` in a pool-worked checkout silently lost its own
# termination contract — `edits: 7` allowed, `phase: fixing` allowed. The marker answers whether THIS
# session is unattended and nothing else; it is a different question about a different file, and the
# two coincide only in the incident above.
#
# An UNSTAMPED entry keeps the behaviour that predates this check, so nothing is relaxed on the
# strength of a missing field, and an indeterminate walk keeps it too: losing the unattended guard back
# is the more expensive direction, since that guard exists for a run that died at 1365 turns with no PR.
# A DEAD owner allows, because no session can advance an entry whose writer is gone and blocking then
# offers only the two damaging remedies above; `STALE_AFTER` used to reach that case six hours later.
#
# WHAT THIS DOES NOT FIX: the state is still keyed on `$PWD`, so two runs in one checkout share one
# entry and the later writer wins — the loser's stamp is simply overwritten. This tells one session's
# entry from another's; it does not give them one entry each.
OWNER_PID=$(jq -r '.owner // empty' <<<"$ENTRY" 2>/dev/null) || OWNER_PID=""
case "$OWNER_PID" in
  ''|*[!0-9]*) ;;   # unstamped: block per the contract, exactly as before this check existed
  *)
    if kill -0 "$OWNER_PID" 2>/dev/null; then
      owns_this_session "$OWNER_PID"
      case $? in
        1) allow ;;   # a LIVE session that is not this one owns this entry
      esac            # 0 = ours, 2 = cannot tell: fall through and hold us to the contract
    else
      allow           # the owning session is gone; nobody here can advance its run
    fi
    ;;
esac

# A background agent this run is waiting on. Checked before the phase switch on purpose: a yield is
# equally correct whether the awaited agent is the refutation gate (phase "building"), a reviewer
# (phase "init"/"fixing") or a fixer spawned after a review that found something (phase "reviewed",
# blocking > 0). Fail open on anything unparseable, like every other check here.
AWAITING=$(jq -r '[(.awaiting // [])[] | (.since // 0)] | length' <<<"$ENTRY" 2>/dev/null) || allow
case "$AWAITING" in ''|*[!0-9]*) AWAITING=0 ;; esac
# An UNATTENDED run has no next turn. `claude -p` exits when the turn ends, so for it a yield
# mid-await is not how the run proceeds — it is how the run dies, silently and with its work
# unpublished. Absent or unparseable, this is false, so an attended session keeps exactly the
# behaviour documented above.
UNATTENDED=$(jq -r 'if .unattended == true then "true" else "false" end' <<<"$ENTRY" 2>/dev/null) || allow
case "$UNATTENDED" in true|false) ;; *) UNATTENDED=false ;; esac

# The authoritative signal is a marker file the driver holds for the life of the run, NOT the field
# above: the skill rewrites its own state entry at its Step 1 and would drop a seeded field, silently
# and fail-open into the very defect this closes. The marker carries the driver pid, because a driver
# killed with SIGKILL leaves the file behind and a stale marker must not make an interactive session
# in this checkout unattended. The field above is checked first and NOTHING WRITES IT TODAY — it is
# there for a caller that can set it without the skill clobbering it, and until one exists the
# marker is the only live producer. Do not read the pair as redundancy.
MARKER="$HOME/.claude/pipeline/unattended/$(printf '%s' "$PWD" | tr '/' '_' | sed 's/^_*//').json"
if [ -f "$MARKER" ]; then
  OWNER=$(jq -r '.pid // empty' "$MARKER" 2>/dev/null)
  case "$OWNER" in
    ''|*[!0-9]*) ;;
    *)
      if kill -0 "$OWNER" 2>/dev/null; then
        # A live marker in this checkout is only OURS if its driver is an ancestor of this process. A
        # co-located pool run does not make an interactive session unattended — that session has a next
        # turn. Indeterminate keeps the old answer, which is the conservative one here.
        owns_this_session "$OWNER"
        case $? in
          1) ;;
          *) UNATTENDED=true ;;
        esac
      fi
      ;;
  esac
fi

if [ "$AWAITING" -gt 0 ]; then
  NEWEST=$(jq -r '[(.awaiting // [])[] | (.since // 0)] | max' <<<"$ENTRY" 2>/dev/null) || allow
  case "$NEWEST" in ''|*[!0-9]*) NEWEST=0 ;; esac
  if [ "$((NOW - NEWEST))" -lt "$AWAIT_TTL" ]; then
    [ "$UNATTENDED" = "true" ] || allow
    AGENTS=$(jq -r '[(.awaiting // [])[] | (.agent // "?")] | join(", ")' <<<"$ENTRY" 2>/dev/null) || AGENTS="?"
    jq -n --arg a "$AGENTS" '{
      decision: "block",
      reason: ("This run is UNATTENDED and you ended your turn with a background agent outstanding: "
        + $a + ". An unattended run has no next turn — the process exits when the turn ends, so "
        + "yielding mid-await does not continue the run, it ends it, with the work unpublished. "
        + "Collect that agent IN THIS TURN, clear the awaiting entry in "
        + "~/.claude/pr-harden-state.json, and carry on with the phases the skill defines. Do NOT "
        + "hand back to the user, do NOT report progress as if finished, and do NOT ask whether to "
        + "continue; if you are aborting, take one of the labelled abort conditions and set "
        + "override:true with its reason so the deviation is on the record."),
      systemMessage: ("unattended run yielded with agents outstanding (" + $a
        + ") — there is no next turn; collect them in-turn")
    }'
    exit 0
  fi
fi

PHASE=$(jq -r '.phase // empty' <<<"$ENTRY" 2>/dev/null) || allow
PR=$(jq -r '.pr // "?"' <<<"$ENTRY" 2>/dev/null)
ROUND=$(jq -r '.round // "?"' <<<"$ENTRY" 2>/dev/null)

case "$PHASE" in
  building)
    # A partial mode's remaining phases are not the full pipeline's, so naming the full list would
    # instruct the run to do work its own mode excludes. `mode` is whatever the skill recorded.
    MODE=$(jq -r '.mode // empty' <<<"$ENTRY" 2>/dev/null) || allow
    if [ "$MODE" = "--plan-only" ]; then
      jq -n '{
        decision: "block",
        reason: ("resolve-ticket is mid-run in --plan-only, which ends at the close of Step 3. Finish "
          + "the plan and the refutation gate, then take the terminus the skill defines for a partial "
          + "mode: CLEAR this repo'"'"'s entry from ~/.claude/pr-harden-state.json and report. Do not "
          + "reach for override:true — the override records a deviation, and a partial mode reaching "
          + "its own defined terminus is not one. Do NOT hand back before the gate has returned."),
        systemMessage: "resolve-ticket (--plan-only): plan or refutation gate still owed"
      }'
      exit 0
    fi
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
