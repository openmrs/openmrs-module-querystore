#!/usr/bin/env python3
"""Where did a session's transcript live while the run wrote its record, and where does it live now?

Usage: relocations.py <transcript.jsonl> ...
For each transcript it prints:
- its current folder;
- the targets of its `relocated` records, with counts, and the last one;
- whether its last `worktree-state` record still names a worktree or shows the worktree session as
  exited;
- every tool call that looked the transcript up (a `$CLAUDE_CODE_SESSION_ID` lookup, or a find or ls by
  the session id), with the line it ran on and the path it printed;
- every `EnterWorktree` and `ExitWorktree` call, with its line and the first words of its result.

Claude Code writes those records itself. A `relocated` record carries the cwd whose project folder the
transcript moved to."""
import collections
import json
import os
import re
import sys

home = os.path.expanduser("~")


def short(s):
    return (s or "").replace(home, "~")


for path in sys.argv[1:]:
    sid = os.path.basename(path)[:-len(".jsonl")]
    rel, ws, uses = [], [], {}
    lookups, wt_uses, wt_calls = [], {}, []
    for n, line in enumerate(open(path, errors="replace"), 1):
        try:
            r = json.loads(line)
        except ValueError:
            continue
        if r.get("type") == "relocated":
            rel.append((n, r.get("relocatedCwd")))
        if r.get("type") == "worktree-state":
            w = r.get("worktreeSession")
            ws.append((n, (w or {}).get("worktreePath") if w else None))
        m = r.get("message") or {}
        for b in (m.get("content") if isinstance(m.get("content"), list) else []):
            if not isinstance(b, dict):
                continue
            if b.get("type") == "tool_use":
                s = json.dumps(b.get("input"))
                if "CLAUDE_CODE_SESSION_ID" in s or sid in s:
                    uses[b.get("id")] = n
                if b.get("name") in ("EnterWorktree", "ExitWorktree"):
                    wt_uses[b.get("id")] = (n, b.get("name"))
            if b.get("type") == "tool_result" and b.get("tool_use_id") in wt_uses:
                out = b.get("content")
                out = out if isinstance(out, str) else json.dumps(out)
                wn, name = wt_uses[b["tool_use_id"]]
                wt_calls.append((wn, name, short(" ".join(out.split()[:9]))))
            if b.get("type") == "tool_result" and b.get("tool_use_id") in uses:
                out = b.get("content")
                out = out if isinstance(out, str) else json.dumps(out)
                hits = [l for l in out.splitlines() if l.strip().endswith(sid + ".jsonl")]
                if hits:
                    lookups.append((uses[b["tool_use_id"]], short(hits[0].strip())))
    targets = collections.Counter(short(x) for _, x in rel)
    print(f"== {sid}")
    print(f"   now under: {os.path.basename(os.path.dirname(path))}")
    print(f"   relocated records: {len(rel)}, targets: {dict(targets)}")
    if rel:
        print(f"   last relocated: L{rel[-1][0]} -> {short(rel[-1][1])}")
    if ws:
        print(f"   last worktree-state: L{ws[-1][0]} -> {short(ws[-1][1]) if ws[-1][1] else 'none (worktree session exited)'}")
    for n, name, out in wt_calls:
        print(f"   {name} at L{n}: {out}")
    for n, p in lookups:
        print(f"   lookup at L{n} printed: {p}")
