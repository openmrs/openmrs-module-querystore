#!/usr/bin/env python3
"""Escalation rate of the Phase 2 that follows each Phase 1 traversal, by the hand classification of
what the orchestrator did after its last edit (post-edit-classes.txt), for first traversals (cycle 1)
and for traversals resumed after an escalation. Counts traversals and runs.

Usage: class-rates.py <post-edit-digest.txt> <post-edit-classes.txt>
Fails if the two files do not have the same number of blocks, so a misaligned classification cannot
produce a table.
"""
import collections
import re
import sys

heads = [l for l in open(sys.argv[1]) if l.startswith("== ")]
classes = [l.split() for l in open(sys.argv[2]) if l.strip() and not l.startswith("#")]
if len(heads) != len(classes) or any(int(c[0]) != i + 1 for i, c in enumerate(classes)):
    sys.exit(f"misaligned: {len(heads)} blocks, {len(classes)} classes")
rows = []
for h, (_, cls) in zip(heads, classes):
    m = re.match(r"== (\S+) c(\d+) next=(\w+)", h)
    rows.append(dict(rec=m.group(1), cycle=int(m.group(2)), outcome=m.group(3), cls=cls,
                     kind="first" if m.group(2) == "1" else "resumed"))
print("all blocks by class:", dict(collections.Counter(r["cls"] for r in rows)))
groups = (("R", {"R"}), ("Rc", {"Rc"}), ("R+Rc", {"R", "Rc"}), ("M", {"M"}), ("N", {"N"}), ("M+N", {"M", "N"}),
          ("0", {"0"}), ("U", {"U"}))
for kind in ("first", "resumed"):
    sel = [r for r in rows if r["kind"] == kind]
    print(f"\n{kind}: {len(sel)} traversals over {len({r['rec'] for r in sel})} runs")
    for name, cs in groups:
        rs = [r for r in sel if r["cls"] in cs]
        if not rs:
            print(f"  {name:5} none")
            continue
        esc = sum(r["outcome"] == "ESCALATED" for r in rs)
        print(f"  {name:5} traversals={len(rs):3} escalated={esc:3} ({esc / len(rs) * 100:3.0f}%)  "
              f"runs={len({r['rec'] for r in rs})}")
