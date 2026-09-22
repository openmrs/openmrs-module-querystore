#!/bin/bash
# Same shape as pr-harden's gate-test.sh, for harden-cycle-gate.sh (harden-state.json, `phase1`/`phase2`,
# with the pre-0.34 `edits` rule surviving for entries that carry no `phase1`).
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

# THE PHASE CONTRACT. `/harden` ends when Phase 1 has converged and the one Phase 2 pass that
# follows it has run without escalating; `edits` is reported and gates nothing.
#
# The KNOWN-BAD CONTROL is RUN, not recorded. Point this suite at the pre-0.34 hook —
# `git show <pre-0.34>:.claude/hooks/harden-cycle-gate.sh > /tmp/old && bash "$0" /tmp/old` — and
# every failure is a case from this block: an implementation that still reads the edit count cannot
# pass them. No tally is written down, for the reason the header 40 lines up gives; the first draft
# of this comment recorded one and it went stale two commits later, when two cases were added below.
# Some cases here pass under BOTH hooks on purpose, exercising the override, awaiting, staleness and
# ownership guards the new predicate sits behind and which did not change; they pin that it is still
# behind them and discriminate nothing on their own. The first two are the sharpest pair: they
# invert, one in each direction.
run_case "phase1 open with edits 0 -> block (an edit count does not end it)" block \
  "{\"cycle\":1,\"phase1\":\"open\",\"edits\":0,\"ts\":$NOW}"
run_case "converged + phase2 done with edits 99 -> allow (nor does it extend it)" allow \
  "{\"cycle\":1,\"phase1\":\"converged\",\"phase2\":\"done\",\"edits\":99,\"ts\":$NOW}"
run_case "phase1 open -> block" block "{\"cycle\":1,\"phase1\":\"open\",\"ts\":$NOW}"
run_case "converged, phase2 pending -> block (the one Phase 2 pass is owed)" block \
  "{\"cycle\":1,\"phase1\":\"converged\",\"phase2\":\"pending\",\"ts\":$NOW}"
run_case "converged, phase2 ABSENT -> block (absent defaults to pending)" block \
  "{\"cycle\":1,\"phase1\":\"converged\",\"ts\":$NOW}"
# An escalation resumes Phase 1, so it is written as `phase1: open` and blocks on that. There is
# no `phase2: escalated`; the first draft had one and it wedged a run that then converged, because
# `--phase1 converged` did not clear it and this hook tested it first. The retired value is now
# simply unrecognised, which fails open -- pinned two cases below.
run_case "escalation is phase1 open, and blocks" block \
  "{\"cycle\":1,\"phase1\":\"open\",\"phase2\":\"pending\",\"ts\":$NOW}"
run_case "phase1 open with a stale phase2 done -> block" block \
  "{\"cycle\":1,\"phase1\":\"open\",\"phase2\":\"done\",\"ts\":$NOW}"
run_case "unrecognised phase1 -> allow (fail open)" allow \
  "{\"cycle\":1,\"phase1\":\"maybe\",\"edits\":3,\"ts\":$NOW}"
# An unrecognised `phase2` does NOT fail open, and these four are why. Only `done` licenses a stop,
# which is decidable without interpreting the value — and the check used to sit ahead of the
# `phase1` test, so any unknown value disarmed an unambiguous `open`. `escalated` is the one that
# makes it real rather than theoretical: harden 0.34.0 wrote it, hours before this, so a run that
# escalated under it and then upgraded had its gate silently removed. The first two cases below were
# written the other way round and pinned the hole as correct; they are the reason it survived two
# review passes.
run_case "unrecognised phase2 -> block (only 'done' ends a run)" block \
  "{\"cycle\":1,\"phase1\":\"converged\",\"phase2\":\"soon\",\"ts\":$NOW}"
run_case "the RETIRED phase2 escalated, converged -> block" block \
  "{\"cycle\":1,\"phase1\":\"converged\",\"phase2\":\"escalated\",\"ts\":$NOW}"
run_case "the RETIRED phase2 escalated, phase1 OPEN -> block (the 0.34.0 migration case)" block \
  "{\"cycle\":2,\"phase1\":\"open\",\"phase2\":\"escalated\",\"ts\":$NOW}"
run_case "an unrecognised phase2 never overrides an open phase1" block \
  "{\"cycle\":1,\"phase1\":\"open\",\"phase2\":\"soon\",\"ts\":$NOW}"
run_case "phase1 open + override -> allow" allow \
  "{\"cycle\":1,\"phase1\":\"open\",\"ts\":$NOW,\"override\":true}"
run_case "phase1 open + awaiting fresh, attended -> allow (yield)" allow \
  "{\"cycle\":1,\"phase1\":\"open\",\"ts\":$NOW,\"awaiting\":$AW}"
run_case "phase1 open + awaiting fresh, marker LIVE -> block" block \
  "{\"cycle\":1,\"phase1\":\"open\",\"ts\":$NOW,\"awaiting\":$AW}" $$
run_case "phase1 open but STALE -> allow" allow \
  "{\"cycle\":1,\"phase1\":\"open\",\"ts\":$((NOW - 25000))}"

# An entry with no `cycle` must still render a copyable command. `--cycle ?` is what it used to
# emit, and argparse takes an int, so the instruction could not be run; the flag is now omitted.
run_case "no cycle number -> still blocks" block \
  "{\"phase1\":\"open\",\"ts\":$NOW}"

# ...and the command it hands back must be RUNNABLE. `--cycle` is required by the writer, so the
# first repair — omitting the flag when there was no number — swapped an unparseable argument for a
# missing one, and a case asserting only `block` could not tell. This one reads the text.
run_cmd_case() { # name expected_substring entry_json
  local name="$1" want="$2" entry="$3" work="$TMP/work"
  mkdir -p "$work"; rm -rf "$TMP/.claude/pipeline/unattended"
  jq -n --arg k "$work" --argjson e "$entry" '{($k): $e}' > "$STATE"
  local got; got=$(cd "$work" && HOME="$TMP" bash "$HOOK" 2>/dev/null | jq -r '.reason // ""')
  if [[ "$got" == *"$want"* ]]; then PASS=$((PASS+1)); echo "  ok   $name"
  else FAIL=$((FAIL+1)); echo "  FAIL $name: no '$want' in: ${got:0:160}"; fi
}
run_cmd_case "the emitted command names a cycle even when the entry has none" \
  "harden-set --cycle 1 --phase1" "{\"phase1\":\"open\",\"ts\":$NOW}"
run_cmd_case "and uses the entry's cycle when it has one" \
  "harden-set --cycle 7 --phase1" "{\"cycle\":7,\"phase1\":\"converged\",\"ts\":$NOW}"

# A run that has STAMPED this entry and stated no verdict owes one. Chosen by the absence of
# `phase1` alone, this fell to the legacy branch — and at `edits: 0`, the ordinary reading on a
# clean tree, the zero-edit rule allowed it. `edits: 0` is the case that matters here.
run_case "run stamped, no verdict, 0 edits -> block" block \
  "{\"run\":\"r1\",\"cycle\":1,\"edits\":0,\"ts\":$NOW}"
run_case "run stamped, no verdict, N edits -> block" block \
  "{\"run\":\"r1\",\"cycle\":1,\"edits\":9,\"ts\":$NOW}"
run_case "run stamped and complete -> allow" allow \
  "{\"run\":\"r1\",\"cycle\":1,\"edits\":9,\"phase1\":\"converged\",\"phase2\":\"done\",\"ts\":$NOW}"
# A 0.34.0-0.36.0 entry carries a real verdict and no run id. Judging it on an edit count would
# throw the verdict away, so the legacy branch needs BOTH halves of its condition.
run_case "no run but a verdict -> the phase contract, not the edit count" block \
  "{\"cycle\":1,\"edits\":0,\"phase1\":\"open\",\"ts\":$NOW}"

# LEGACY entries — no run id AND no verdict, written by a /harden that predates the contract. The zero-edit rule
# still applies to them, so a run already in flight when this changed is not silently disarmed.
run_case "legacy: edits 0 -> allow (converged)" allow "{\"cycle\":2,\"edits\":0,\"ts\":$NOW}"
run_case "legacy: edits 3, no awaiting -> block" block "{\"cycle\":2,\"edits\":3,\"ts\":$NOW}"
# A legacy entry with no usable cycle number used to print NOTHING and be read as allow: jq's
# `empty` from `tonumber?` on "?" propagates through the concatenation and suppresses the whole
# object. Silence from this hook is indistinguishable from a deliberate allow, so these two are the
# fail-open that mattered most, and neither expects a number in the message.
run_case "legacy: edits 3, NO cycle -> block" block "{\"edits\":3,\"ts\":$NOW}"
run_case "legacy: edits 3, non-numeric cycle -> block" block \
  "{\"cycle\":\"abc\",\"edits\":3,\"ts\":$NOW}"
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
