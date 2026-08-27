#!/bin/bash
# Stop-hook gate for the `harden` skill's termination contract.
#
# The contract: /harden is complete only when one cycle produces ZERO edits — a full Phase 1 +
# Phase 2, or the documentation pass the skill's classification rule allows in its place.
# The skill states that four times and it still got broken, because nothing outside the model
# enforced it. This does.
#
# Contract with the skill: at the end of every cycle it writes an entry to the state file below,
# keyed by the repo it is hardening:
#
#   { "/abs/path/to/repo": { "cycle": 4, "edits": 3, "ts": 1755400000, "override": false,
#                            "awaiting": [ { "agent": "phase2 quality", "since": 1755400000 } ] } }
#
# `awaiting` is what lets a cycle wait for its own subagents. Phase 2 spawns them, so a cycle is
# routinely blocked on one with nothing to do but yield — and without this field the gate refuses that
# yield, making a cycle waiting correctly indistinguishable from one that quit. Measured: a Phase 2
# pass blocked on a background agent tripped this hook on every yield and had to burn in-turn sleep
# loops to stay alive. A non-empty, fresh `awaiting` therefore allows the stop; the harness re-invokes
#
# THAT HOLDS ONLY FOR AN ATTENDED SESSION. A `claude -p` process exits when its turn ends, so nothing
# re-invokes it and the yield IS the death — measured 2026-08-26 on issue #310, which ended in Phase 2
# pass 3 at 1365 turns and $76.72 with committed work, no PR and ten orphaned worktrees. The allow is
# therefore scoped to attended sessions; see the marker check below.
# the session when the agent completes, so yielding mid-await is how the cycle proceeds, not how it
# ends. The obligation is to CLEAR the field on any terminal outcome, so the allow is bounded by
# AWAIT_TTL and an agent that has not returned inside it counts as dead rather than outstanding.
# Same field, same semantics as pr-harden-gate.sh, which solved this first.
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
AWAIT_TTL=3600      # 1h; an awaited subagent that has not returned in this long is dead, not running.
                    # Generous on purpose: a Phase 2 agent may run a full root build and drive
                    # mutations. Still far under STALE_AFTER, so a forgotten `awaiting` cannot outlive
                    # the run that wrote it.

allow() { exit 0; }

[ -f "$STATE" ] || allow
command -v jq >/dev/null 2>&1 || allow

# The repo being hardened is the directory the session is running in.
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
# WHAT THIS DOES AND DOES NOT FIX, restated after worktrees. The key is the WORKING TREE, not the
# repository, and under the pool driver each ticket is worked in its own `git worktree` — so two runs
# on one repository have two keys and two entries, which is what makes concurrent tickets safe. What
# is NOT fixed is two sessions in the SAME directory, which is the interactive case: they key alike
# and the later writer wins, the loser's stamp simply overwritten. `owner` tells one session's entry
# from another's there; it does not give them one entry each.
#
# The key is also the PHYSICAL path now (`pwd -P`), matching `gate-state`, which writes it with
# `realpath`. They used to disagree — logical here, resolved there — and a mismatch finds no entry,
# which is this hook's fail-OPEN case.
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

# A background agent this cycle delegated to is outstanding: allow the yield, whatever `edits` says.
# Fail open on anything unparseable, like every other check here.
AWAITING=$(jq -r '[(.awaiting // [])[] | (.since // 0)] | length' <<<"$ENTRY" 2>/dev/null) || allow
case "$AWAITING" in ''|*[!0-9]*) AWAITING=0 ;; esac
# An UNATTENDED run has no next turn: `claude -p` exits when the turn ends, so for it a yield
# mid-await is not how the cycle proceeds but how the run dies, silently. The authoritative signal is
# a pid-stamped marker the pool driver holds for the life of the run — not a field in this entry,
# which the skill rewrites and would silently drop. A stale marker whose owner is gone must not make
# an interactive session unattended, so the pid is checked for liveness. Absent or unparseable, this
# is false and an attended cycle keeps exactly the behaviour documented above.
UNATTENDED=$(jq -r 'if .unattended == true then "true" else "false" end' <<<"$ENTRY" 2>/dev/null) || allow
case "$UNATTENDED" in true|false) ;; *) UNATTENDED=false ;; esac
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
        + "yielding mid-await does not continue the cycle, it ends the run with the work unfinished. "
        + "Collect that agent IN THIS TURN, clear the awaiting entry in "
        + "~/.claude/harden-state.json, and finish the cycle. Do NOT hand back to the user, do NOT "
        + "report progress as if finished, and do NOT ask whether to continue; if you are stopping "
        + "deliberately, take the labelled override so the deviation is on the record."),
      systemMessage: ("unattended harden cycle yielded with agents outstanding (" + $a
        + ") — there is no next turn; collect them in-turn")
    }'
    exit 0
  fi
fi

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
