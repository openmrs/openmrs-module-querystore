#!/usr/bin/env python3
"""PreToolUse(Bash) hook: refuse a loop that waits on a GitHub Actions run id nobody checked was found.
The shape: `id=$(gh run list ... --jq '.[0].databaseId')`, then `until [ "$(gh run view $id ...)" = completed ]`. When
the lookup finds no run (wrong sha, the run not created yet, a filter that matches nothing), `id` is empty, `gh run
view` fails on every pass, the condition never holds, and the loop waits forever. On 2026-11-20 one did, from 07:06 to
09:14, until the owner asked "how far?" (session 2723129b); 2026-11-05 and 2026-11-17 had the same stall. Of the 40
commands from 2026-09-28 to 2026-11-20 15:00 UTC that looked a run id up and then polled it in a loop, 21 had this
shape, the three stalls among them, and the other 19 guarded the id, bounded the loop, or polled outside a loop (counted
with this hook's own `unguarded_wait`, 2026-11-20 retro).

It refuses only that shape: the polled variable was assigned from `gh run list`, the loop polling it has no deadline
(SECONDS or `date +%s` compared, or a counter the loop steps), and nothing before the loop ends the command when the
variable is empty, waits until it is not, or tests it right before the loop. A literal run id, a one-shot `gh run view`
and a bounded loop are left alone. A loop that waits for `gh run list` to return a run is outside it: one that ends only
when the id is set vouches for the polling loop after it, but itself waits forever on a wrong sha. 8 of the 40 commands
had one (the guard refuses one of them, for the unguarded poll after it), none measured stalling; CLAUDE.md's "give the
loop a deadline" covers it. An id assigned with backticks or `read` is not seen. It reads the command text only and
fails open on any read failure. Tests: gh-run-wait-guard-test.py beside this file.
"""
import json
import re
import sys

ASSIGN = re.compile(r"\b([A-Za-z_]\w*)=\"?\$\(\s*(?:[A-Za-z_]\w*=\S*\s+)*gh\s+run\s+list\b")
LOOP = re.compile(r"\b(?:until|while)\b(.*?)\bdone\b", re.S)
CMP = r"-(?:gt|ge|lt|le)\s"
# A guard ends the command when the id is empty: `exit` or `return` as a command, first in a block or after a `;`, not
# a word in an echoed string. `[ -n "$id" ] || { sleep; continue; }` inside a find loop retries and then falls through
# with the id still empty, so it is no guard.
LEAVES = r"\s*(?:\{(?:(?:[^{}]|\$\{[^}]*\})*?[;\n])?\s*(?:exit|return)\b|(?:exit|return)\b)"


def counter(v, loop):
    """v is stepped inside the loop: a counter, not a probe's captured result."""
    stepped = (r"\b" + v + r"=\$\(\(\s*\$?" + v + r"\s*[-+]"
               + r"|\(\(\s*(?:" + v + r"\s*(?:\+\+|--|[-+]=)|(?:\+\+|--)" + v + r")"
               + r"|\blet\s+\"?" + v + r"(?:\+\+|--|[-+]=)")
    return bool(re.search(stepped, loop))


def bounded(loop):
    """The loop compares a clock, or a counter it steps. Printing $SECONDS bounds nothing, and nor does comparing a
    probe's own result (`"$(gh run view ...)" -gt 0`, or `n=$(gh run view ...); [ "$n" -gt 0 ]`)."""
    for m in re.finditer(r"\$\{?([A-Za-z_]\w*)\}?\"?\s+" + CMP, loop):
        if m.group(1) == "SECONDS" or counter(m.group(1), loop):
            return True
    if re.search(r"\$\(\([^)]*\bSECONDS\b[^)]*\)\)\"?\s+" + CMP + r"|date\s+\+%s\)\"?\s+" + CMP, loop):
        return True
    for m in re.finditer(r"\(\(((?:[^()]|\([^()]*\))*)\)\)", loop):
        e = m.group(1)
        if re.search(r"[<>]", e) and (re.search(r"\bSECONDS\b|date\s+\+%s", e)
                                      or any(counter(v, loop) for v in re.findall(r"\b([A-Za-z_]\w*)\b", e))):
            return True
    return False


def guarded(v, before):
    """Whether what runs before the polling loop leaves it running only with v set: after v's last lookup, an exit when
    v is empty or a loop that waits until v is set; or a test of v that the polling loop directly follows."""
    ref = r'"?\$\{?' + v + r'\}?"?'
    non_empty = r"(?:\[\[?\s+-n\s+" + ref + r"\s+\]\]?|\[\[?\s+" + ref + r"\s+\]\]?|\btest\s+-n\s+" + ref + r")"
    empty = r"(?:\[\[?\s+-z\s+" + ref + r"\s+\]\]?|\btest\s+-z\s+" + ref + r")"
    exits = re.compile("|".join([
        non_empty + r"\s*\|\|" + LEAVES,
        empty + r"\s*&&" + LEAVES,
        r"\bif\s+" + empty + r"\s*[;\n]\s*then\b(?:(?!\bfi\b).)*?\b(?:exit|return)\b",
        r":\s+\"?\$\{" + v + r":\?",
    ]), re.S)
    gates_it = re.compile(r"(?:" + non_empty + r"\s*&&|\bif\s+" + non_empty + r"\s*[;\n]\s*then)\s*$", re.S)
    waits = re.compile(r"\b(?:until\b(?:(?!\bdone\b).)*?" + non_empty + r"|while\b(?:(?!\bdone\b).)*?" + empty
                       + r")\s*;\s*do\b(?:(?!\bdone\b).)*\bdone\b", re.S)
    # Only what follows the variable's last lookup can vouch for it: a check before it, or before an earlier lookup,
    # says nothing about the value the loop polls. A waiting loop vouches if it looks the variable up itself and ends
    # after that last lookup; one that only waits for it to change, with nothing assigning it, never ends.
    looked_up = [a.start() for a in ASSIGN.finditer(before) if a.group(1) == v]
    last = looked_up[-1] if looked_up else 0
    # Quoted text is only words: "no run for $sha; exit by hand" holds no command. Strings are read left to right so
    # each quote pairs with its own partner. Kept: a bare variable reference ("$id", "${id}") and a parameter check
    # ("${id:?no run}"), since the guards test those.
    def blank(q):
        keep = re.fullmatch(r'"\$(?:[A-Za-z_]\w*|\{[A-Za-z_]\w*(?::\?[^}]*)?\})"', q.group(0))
        return q.group(0) if keep else '""'
    words_out = re.sub(r"'[^']*'|\"(?:[^\"\\]|\\.)*\"", blank, before[last:])
    if exits.search(words_out) or gates_it.search(before):
        return True
    return any(w.end() > last and any(a.group(1) == v for a in ASSIGN.finditer(w.group(0)))
               for w in waits.finditer(before))


def unguarded_wait(cmd):
    """The variable an unbounded loop waits on after `gh run list` looked it up with no empty check, or None."""
    for v in dict.fromkeys(ASSIGN.findall(cmd)):
        poll = re.compile(r"gh\s+run\s+(?:view|watch)\b[^;&|\n]*?\$\{?" + v + r"\b")
        for m in LOOP.finditer(cmd):
            if poll.search(m.group(0)) and not bounded(m.group(0)) and not guarded(v, cmd[:m.start()]):
                return v
    return None


def main():
    try:
        payload = json.load(sys.stdin)
        cmd = payload.get("tool_input", {}).get("command") or ""
    except Exception:  # fail-open: a broken read must never block a command
        return
    v = unguarded_wait(cmd)
    if not v:
        return
    reason = (f"This loop waits on ${v}, which `gh run list` looked up, without checking the lookup found a "
              f"run. When it finds none, ${v} is empty and the loop never ends (2723129b waited two hours that way "
              f"on 2026-11-20). Add `[ -n \"${v}\" ] || {{ echo \"no run found\"; exit 1; }}` before the loop, or "
              f"give the loop a deadline (`|| [ $SECONDS -gt 3600 ]`).")
    json.dump({"hookSpecificOutput": {"hookEventName": "PreToolUse", "permissionDecision": "deny",
                                      "permissionDecisionReason": reason}}, sys.stdout)


if __name__ == "__main__":
    main()
