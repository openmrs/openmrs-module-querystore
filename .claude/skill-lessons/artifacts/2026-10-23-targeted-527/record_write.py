#!/usr/bin/env python3
"""Where was a session's transcript when the run wrote its record's `transcript:` header?

Usage: record_write.py <transcript.jsonl>
Prints every tool call whose input carries a `transcript: ` header line, with its line, the session's
cwd at that moment and the path the header names. It then gives the cwd of the last `relocated` record
before that call (the session's first cwd when there is none), which names the project folder that
held the transcript then, and how many `relocated` records came before it."""
import collections
import json
import os
import re
import sys

home = os.path.expanduser("~")
recs = []
for n, line in enumerate(open(sys.argv[1], errors="replace"), 1):
    try:
        recs.append((n, json.loads(line)))
    except ValueError:
        pass
writes = []
for n, r in recs:
    m = r.get("message") or {}
    for b in (m.get("content") if isinstance(m.get("content"), list) else []):
        if isinstance(b, dict) and b.get("type") == "tool_use":
            s = json.dumps(b.get("input"))
            hit = re.search(r"transcript: (\S+?\.jsonl|\$\w+)", s)
            if hit:
                writes.append((n, b.get("name"), (r.get("cwd") or "").replace(home, "~"), hit.group(1).replace(home, "~")))
first_cwd = next(((r.get("cwd") or "").replace(home, "~") for n, r in recs if r.get("cwd")), None)
for n, name, cwd, path in writes:
    before = [(k, (r.get("relocatedCwd") or "").replace(home, "~"))
              for k, r in recs if r.get("type") == "relocated" and k < n]
    held = before[-1][1] if before else f"{first_cwd} (no relocated record yet)"
    print(f"L{n} {name} cwd={cwd} header names: {path}")
    print(f"     transcript then held for: {held} | relocated records before: {len(before)}")
