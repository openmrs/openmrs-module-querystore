#!/usr/bin/env python3
"""Lists the orchestrator's harden-relevant events in a session transcript, in order.

Usage: harden-events.py <transcript.jsonl> [--text]

One line per event: transcript line, UTC time, kind, detail. Kinds:
  HARDEN   the Skill tool loading harden
  STATE    a harden gate write: gate-state `harden-set` (cycle, phase1, phase2), or a hand-written
           write to harden-state.json (older runs), shown with the phase words the command carries
  COMMIT   a git commit, with the message's first line where the command or an earlier Write of its
           -F file carries it
  P2       an Agent spawn whose description names a Phase 2 lens (reuse, quality, efficiency,
           integration) or Phase 2; AGENT for any other spawn
  EDIT     an Edit/Write/NotebookEdit outside the scratchpad and /tmp
  READ     a Read, Grep or Glob, or a Bash call that only reads (git diff/show/log, grep, sed -n, cat)
  BASH     any other Bash call (builds, probes, scripted edits)
  TEXT     with --text, the orchestrator's prose, first 300 characters
Only the main thread: entries marked isSidechain are a subagent's and are skipped. A tool_use id is
printed once, however many times the transcript repeats it.
"""
import json
import re
import sys

path = sys.argv[1]
show_text = "--text" in sys.argv[2:]
SCRATCH = re.compile(r"^(/private)?/tmp/|/scratchpad/")
LENS = re.compile(r"phase 2|phase2|\b(reuse|quality|efficiency|integration)\b", re.I)
READ_ONLY = re.compile(r"^\s*(git\s+(-C\s+\S+\s+)?(diff|show|log|status|rev-parse|merge-base|blame)\b"
                       r"|grep\b|rg\b|sed\s+-n\b|cat\b|head\b|tail\b|ls\b|wc\b|find\b)")
COMMIT = re.compile(r"\bgit\s+(?:-C\s+\S+\s+)*commit\b")

seen = set()
writes = {}  # file path -> content of the last Write, to read a `git commit -F <file>` message


def first_line_of_message(cmd):
    m = re.search(r"""\s-m\s+(?:"([^"]*)"|'([^']*)'|(\S+))""", cmd)
    if m:
        return (m.group(1) or m.group(2) or m.group(3) or "").splitlines()[0][:110]
    m = re.search(r"\s-F\s+(\S+)", cmd)
    if m:
        body = writes.get(m.group(1).strip("'\""))
        if body is not None:
            return (body.splitlines() or [""])[0][:110]
        return f"(-F {m.group(1)}: not written in this transcript)"
    if "--amend" in cmd and "--no-edit" in cmd:
        return "(amend, message kept)"
    return "(message not in the command)"


def emit(lineno, ts, kind, detail):
    print(f"L{lineno:<6} {ts[11:19] if len(ts) >= 19 else ts:8} {kind:6} {detail}")


with open(path, encoding="utf-8", errors="replace") as f:
    for lineno, raw in enumerate(f, 1):
        try:
            entry = json.loads(raw)
        except json.JSONDecodeError:
            continue
        if entry.get("isSidechain") or entry.get("type") != "assistant":
            continue
        ts = entry.get("timestamp", "")
        content = (entry.get("message") or {}).get("content") or []
        if not isinstance(content, list):
            continue
        for block in content:
            if not isinstance(block, dict):
                continue
            if block.get("type") == "text" and show_text:
                text = " ".join(block.get("text", "").split())
                if text:
                    emit(lineno, ts, "TEXT", text[:300])
                continue
            if block.get("type") != "tool_use":
                continue
            tid = block.get("id")
            if tid in seen:
                continue
            seen.add(tid)
            name, inp = block.get("name", ""), block.get("input") or {}
            if name == "Skill":
                if inp.get("skill") == "harden":
                    emit(lineno, ts, "HARDEN", (inp.get("args") or "")[:100])
                continue
            if name == "Write":
                writes[inp.get("file_path", "")] = inp.get("content", "")
            if name in ("Edit", "Write", "NotebookEdit", "MultiEdit"):
                fp = inp.get("file_path") or inp.get("notebook_path") or ""
                if not SCRATCH.search(fp):
                    emit(lineno, ts, "EDIT", fp)
                continue
            if name in ("Read", "Grep", "Glob"):
                emit(lineno, ts, "READ", f"{name} {inp.get('file_path') or inp.get('pattern') or ''}"[:120])
                continue
            if name == "Agent" or name == "Task":
                desc = inp.get("description", "")
                emit(lineno, ts, "P2" if LENS.search(desc) else "AGENT", desc[:100])
                continue
            if name != "Bash":
                continue
            cmd = inp.get("command", "")
            if "harden-set" in cmd:
                parts = []
                for flag in ("--cycle", "--phase1", "--phase2"):
                    vals = re.findall(re.escape(flag) + r"\s+(\S+)", cmd)
                    if vals:
                        parts.append(f"{flag[2:]}={'/'.join(vals)}")
                emit(lineno, ts, "STATE", " ".join(parts) or "harden-set (no phase flags)")
                continue
            if "harden-state" in cmd and re.search(r"phase1|converged|\"open\"", cmd):
                words = sorted(set(re.findall(r"\b(converged|open|done|pending)\b", cmd)))
                emit(lineno, ts, "STATE", "legacy write: " + ",".join(words))
                continue
            if COMMIT.search(cmd):
                emit(lineno, ts, "COMMIT", first_line_of_message(cmd))
                continue
            first = cmd.strip().splitlines()[0] if cmd.strip() else ""
            if READ_ONLY.match(first) and not re.search(r"(-i\b|>\s*[^&]|\btee\b)", cmd):
                emit(lineno, ts, "READ", first[:120])
            else:
                emit(lineno, ts, "BASH", (inp.get("description") or first)[:120])
