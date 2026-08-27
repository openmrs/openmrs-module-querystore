#!/bin/bash
# PreToolUse(Bash) safety net for `git checkout -- <path>` / `git restore <path>`.
#
# WHY THIS IS A BACKUP AND NOT A BLOCKER. The failure it exists for is measured across seven records
# and nine incidents (~/.claude/skill-lessons: #302, #284, #268, #269 twice, #250 twice, #308, #317;
# REJECTED.md's running count is authoritative and stood at 5 incidents / 4 records when this was
# written, which is why this line is dated rather than trusted): a mutation probe undone with
# `git checkout -- <path>` on a file that also carried uncommitted INTENDED work discarded that work
# too. In #302 it shipped a commit whose message described changes absent from its diff; in #269 it
# reverted a javadoc correction that then survived three review passes before an agent re-found it.
#
# The obvious hook — refuse when the path has uncommitted modifications — is WRONG, and that matters
# enough to record here so nobody "fixes" this script into it. At the moment of a legitimate
# restore-after-mutation the file is ALWAYS modified: that modification IS the mutation being undone.
# The dangerous case (intended work + mutation) and the safe case (mutation only) are indistinguishable
# from the file's state at restore time; telling them apart needs history this hook does not have. A
# blocker on that predicate fires on every correct use, and a hook with that false-positive rate gets
# disabled, which is strictly worse than no hook.
#
# What IS decidable is the harm. Every record describes the loss as SILENT and discovered later. So:
# copy each modified tracked file aside BEFORE the destructive command runs, outside the repo, and tell
# the model where. Nothing is blocked, there are no false positives, and a loss becomes one `cp` from
# recovery instead of a re-derivation.
#
# OUTSIDE THE REPO, deliberately: an in-repo backup would land in the `git status --porcelain` that the
# harden cycle gate uses as its edit count, fabricating an edit and demanding another cycle, and it
# would be invisible to the `git diff | shasum` residue guard, which does not report untracked paths.
#
# This does NOT replace "commit before you probe" — that is what makes the restore correct in the first
# place, and it remains the rule. This is the net under it.
#
# FAIL OPEN, ALWAYS, and never block: no jq, no git, not a repo, an unparseable payload, a cap
# exceeded, a failed copy — every one of them allows the command unchanged. The worst this script may
# ever do is fail to take a backup.

set -uo pipefail

BACKUP_ROOT="$HOME/.claude/restore-backups"
MAX_FILES=50            # a targeted probe restores one or two files; past this it is a bulk operation
MAX_BYTES=5242880       # 5 MB per file; source files are far under, datasets are not worth copying
RETAIN_DAYS=7

allow() { exit 0; }

command -v jq >/dev/null 2>&1 || allow
command -v git >/dev/null 2>&1 || allow

PAYLOAD=$(cat 2>/dev/null) || allow
[ -n "$PAYLOAD" ] || allow

TOOL=$(jq -r '.tool_name // empty' <<<"$PAYLOAD" 2>/dev/null) || allow
[ "$TOOL" = "Bash" ] || allow

CMD=$(jq -r '.tool_input.command // empty' <<<"$PAYLOAD" 2>/dev/null) || allow
[ -n "$CMD" ] || allow

# Only the PATH forms destroy working-tree content. A bare `git checkout <branch>` cannot: git refuses
# a switch that would overwrite local changes. `git restore --staged` only unstages, so it is excluded
# unless a worktree mode is also named.
DESTRUCTIVE=0
grep -Eq 'git([[:space:]]+-[^[:space:]]+)*[[:space:]]+checkout([[:space:]]|$).*--[[:space:]]+[^[:space:]]' <<<"$CMD" && DESTRUCTIVE=1
if grep -Eq 'git([[:space:]]+-[^[:space:]]+)*[[:space:]]+restore([[:space:]]|$)' <<<"$CMD"; then
  if grep -Eq '\-\-staged' <<<"$CMD" && ! grep -Eq '\-\-worktree' <<<"$CMD"; then :; else DESTRUCTIVE=1; fi
fi
[ "$DESTRUCTIVE" -eq 1 ] || allow

CWD=$(jq -r '.cwd // empty' <<<"$PAYLOAD" 2>/dev/null) || allow
[ -n "$CWD" ] && [ -d "$CWD" ] || allow
cd "$CWD" 2>/dev/null || allow

REPO=$(git rev-parse --show-toplevel 2>/dev/null) || allow
[ -n "$REPO" ] || allow
cd "$REPO" 2>/dev/null || allow

# Every modified tracked file, not an attempt to parse the pathspec. Parsing is where a hook like this
# goes wrong — `.`, a directory, a glob, a compound command — and over-copying is harmless while
# under-copying is the whole failure. -z for paths with spaces or quotes.
# bash 3.2 compatible (macOS ships 3.2 as /bin/bash, which has no `mapfile`): NUL-delimited read loop.
DIRTY=()
while IFS= read -r -d '' f; do
  DIRTY[${#DIRTY[@]}]="$f"
done < <(git diff --name-only -z --diff-filter=ACMR HEAD 2>/dev/null)
[ "${#DIRTY[@]}" -gt 0 ] || allow
[ "${#DIRTY[@]}" -le "$MAX_FILES" ] || allow

STAMP="$(date +%Y%m%d-%H%M%S)-$$"
DEST="$BACKUP_ROOT/$STAMP"
mkdir -p "$DEST" 2>/dev/null || allow

SAVED=0
for f in "${DIRTY[@]}"; do
  [ -f "$f" ] || continue
  SZ=$(wc -c <"$f" 2>/dev/null | tr -d '[:space:]') || continue
  case "$SZ" in ''|*[!0-9]*) continue ;; esac
  [ "$SZ" -le "$MAX_BYTES" ] || continue
  mkdir -p "$DEST/$(dirname "$f")" 2>/dev/null || continue
  cp -p "$f" "$DEST/$f" 2>/dev/null && SAVED=$((SAVED + 1))
done

if [ "$SAVED" -eq 0 ]; then rmdir "$DEST" 2>/dev/null; allow; fi

# Retention sweep, best effort and never fatal.
find "$BACKUP_ROOT" -mindepth 1 -maxdepth 1 -type d -mtime +"$RETAIN_DAYS" -exec rm -rf {} + 2>/dev/null

jq -n --arg d "$DEST" --arg n "$SAVED" '{
  hookSpecificOutput: { hookEventName: "PreToolUse", permissionDecision: "allow" },
  additionalContext: ("Pre-restore backup: " + $n + " modified file(s) copied to " + $d
      + " before this git restore/checkout ran. If it discarded uncommitted work you meant to keep "
      + "(the measured failure this guards — it is silent, and `git diff --stat` afterwards reads as "
      + "\"restored\" rather than \"reverted\"), recover from there rather than re-deriving. Committing "
      + "before a mutation probe is still what makes the restore correct.")
}'
exit 0
