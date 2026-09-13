#!/bin/bash
# Cases for pipeline-rm-shape.py. Usage: bash pipeline-rm-shape-test.sh [path-to-hook]
#
# The hook decides which rm shapes would reach a bypass-immune circuit breaker, so it is exactly the
# kind of predicate the hooks README says must not be edited on inspection alone. Every case below is
# either a shape measured to prompt (2026-09-13), or a real command lifted from a pool-run transcript
# that must keep working.
set -uo pipefail
HOOK=${1:-$HOME/.claude/hooks/pipeline-rm-shape.py}
[ -f "$HOOK" ] || { echo "no hook at $HOOK"; exit 2; }
command -v jq >/dev/null 2>&1 || { echo "jq required"; exit 2; }

WT="$HOME/.claude/pipeline/worktrees/openmrs-openmrs-module-chartsearchai-409"
AGENT="$HOME/Projects/x/.claude/worktrees/agent-a1"
OUTSIDE="$HOME/Projects/openmrs/chartsearchai"
PASS=0; FAIL=0

t() { # cwd command want label
  local out got
  out=$(jq -n --arg c "$2" --arg w "$1" \
        '{tool_name:"Bash",tool_input:{command:$c},cwd:$w}' \
        | env -u CLAUDE_PIPELINE_SESSION -u CLAUDE_PIPELINE_SLOT python3 "$HOOK" 2>/dev/null)
  [ -n "$out" ] && got=DENY || got=ALLOW
  if [ "$got" = "$3" ]; then PASS=$((PASS+1)); printf 'ok   %-5s %s\n' "$got" "$4"
  else FAIL=$((FAIL+1)); printf 'FAIL %-5s (want %s) %s\n' "$got" "$3" "$4"; fi
}
raw() { # payload label   -- must always fail open
  local out rc
  out=$(printf '%s' "$1" | env -u CLAUDE_PIPELINE_SESSION -u CLAUDE_PIPELINE_SLOT \
        python3 "$HOOK" 2>/dev/null); rc=$?
  if [ -z "$out" ] && [ "$rc" -eq 0 ]; then PASS=$((PASS+1)); printf 'ok   ALLOW %s\n' "$2"
  else FAIL=$((FAIL+1)); printf 'FAIL rc=%s %s\n' "$rc" "$2"; fi
}

te() { # cwd command want label  -- same, but WITH pool-run's stamp in the environment
  local out got
  out=$(jq -n --arg c "$2" --arg w "$1" \
        '{tool_name:"Bash",tool_input:{command:$c},cwd:$w}' \
        | CLAUDE_PIPELINE_SESSION=1 python3 "$HOOK" 2>/dev/null)
  [ -n "$out" ] && got=DENY || got=ALLOW
  if [ "$got" = "$3" ]; then PASS=$((PASS+1)); printf 'ok   %-5s %s\n' "$got" "$4"
  else FAIL=$((FAIL+1)); printf 'FAIL %-5s (want %s) %s\n' "$got" "$3" "$4"; fi
}
ts() { # cwd command want label  -- same, but with the SLOT variable a --work session carries
  local out got
  out=$(jq -n --arg c "$2" --arg w "$1" \
        '{tool_name:"Bash",tool_input:{command:$c},cwd:$w}' \
        | CLAUDE_PIPELINE_SLOT=slot-1 python3 "$HOOK" 2>/dev/null)
  [ -n "$out" ] && got=DENY || got=ALLOW
  if [ "$got" = "$3" ]; then PASS=$((PASS+1)); printf 'ok   %-5s %s\n' "$got" "$4"
  else FAIL=$((FAIL+1)); printf 'FAIL %-5s (want %s) %s\n' "$got" "$3" "$4"; fi
}

# --- shapes that would reach the breaker, or are never right in a worktree ---
t "$WT" "rm -rf $WT/*"                                    DENY  "the #409 shape: absolute trailing glob"
t "$WT" "rm -rf ~/.claude/pipeline/worktrees/openmrs-openmrs-module-chartsearchai-409/*" DENY "tilde trailing glob"
t "$WT" "cd $WT && rm -rf *"                              DENY  "bare glob after a cd"
t "$WT" 'rm -rf $WT/*'                                    DENY  "variable the hook cannot resolve"
t "$WT" 'rm -rf $(git rev-parse --show-toplevel)/*'       DENY  "command substitution"
t "$WT" 'rm -rf `pwd`/*'                                  DENY  "backtick substitution"
t "$WT" "rm -rf $WT"                                      DENY  "worktree root, no glob"
t "$WT" "rm -rf ."                                        DENY  "cwd written as dot"
t "$WT" "rm -rf /"                                        DENY  "critical system path"
t "$WT" 'rm -rf $HOME'                                    DENY  "home directory"
t "$AGENT" "rm -rf sub/*"                                 DENY  "subagent worktree is in scope"
SIB="$HOME/.claude/pipeline/worktrees/openmrs-openmrs-module-chartsearchai-294"
t "$WT" "rm -rf $SIB"                                     DENY  "another ticket's worktree root"
t "$WT" "rm -rf $SIB/api/src"                             ALLOW "a path INSIDE another worktree"
t "$WT" 'python3 - <<PY
print("x")
PY
rm -rf '"$WT"'/*'                                         DENY  "heredoc then a real trailing-glob rm"

# --- real pool-run idioms that must keep working ---
t "$WT" "rm -rf target"                                   ALLOW "maven build dir"
t "$WT" 'SC=/tmp/x && rm -rf $SC/omodx'                   ALLOW "scratchpad rm, lifted from a transcript"
t "$WT" "rm -f api/src/test/foo.log"                      ALLOW "a single file"
t "$WT" "rm -f *.log"                                     ALLOW "suffix glob is not a TRAILING glob"
t "$WT" "rm -rf -- target"                                ALLOW "-- end of flags"
t "$WT" "git -C /repo worktree remove --force $WT"        ALLOW "the replacement the hook prescribes"
t "$WT" "git rm --cached foo.txt"                         ALLOW "git rm is not rm"
t "$WT" "git commit -F - <<'MSG'
fix: rm the * from the glob
MSG"                                                      ALLOW "heredoc BODY naming rm and a star"
t "$OUTSIDE" "rm -rf $WT/*"                               ALLOW "operator's own session is out of scope"

# --- fail open ---
raw ''                                                    "empty payload"
raw '{'                                                   "malformed json"
raw '{"tool_name":"Read","tool_input":{}}'                "non-Bash tool"
raw '{"tool_name":"Bash","tool_input":{"command":"ls"}}'  "payload with no cwd"

# --- the driver's non-worktree sessions, reached by the stamp and not by the path ---
RETRO="$HOME/Projects/openmrs/chartsearchai"
te "$RETRO"   "rm -rf ."                                  DENY  "/skill-retro cwd: stamped, so judged"
te "$RETRO"   "rm -rf build/*"                            DENY  "/skill-retro cwd: trailing glob"
ts "$HOME/.claude/pipeline" "rm -rf ."                    DENY  "health probe cwd: SLOT var is enough"
te "$RETRO"   "rm -rf target"                             ALLOW "stamped, but an ordinary rm still passes"

echo "passed=$PASS failed=$FAIL"
[ "$FAIL" -eq 0 ]
