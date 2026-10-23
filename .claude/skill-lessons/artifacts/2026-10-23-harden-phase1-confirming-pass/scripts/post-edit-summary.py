#!/usr/bin/env python3
"""Escalation rate of the Phase 2 that follows each Phase 1 traversal, by how much reading of source
the orchestrator did between its last edit (since the previous converged write) and the converged
write. Reads phase1-traversals.py output.

Usage: post-edit-summary.py <phase1-traversals output> [--list <first|resumed> <0|1-2|3+>]
"""
import sys
import re

ROW = re.compile(r"^(\S+) L(\d+)\s+c(\S+)\s+(T?)\s*span=\s*([\d.]+) .*? next=(\w+) .*passes=(\d+) from=L(\d+):(\w+) "
                 r".*post-edit: edits=(\d+) rd=(\d+) open=(\d+) min=(\S+)")
rows, bad = [], 0
for line in open(sys.argv[1]):
    if "span=" not in line:
        continue
    m = ROW.search(line)
    if not m:
        bad += 1
        continue
    rows.append(dict(name=m.group(1), line=int(m.group(2)), cycle=m.group(3), terminal=bool(m.group(4)),
                     outcome=m.group(6), start=int(m.group(8)), frm=m.group(9), edits=int(m.group(10)),
                     rd=int(m.group(11)), opn=int(m.group(12)),
                     mins=None if m.group(13) == "-" else float(m.group(13))))
print(f"traversal rows: {len(rows)}, unparsed: {bad}")
known = [r for r in rows if not r["terminal"] and r["outcome"] != "UNKNOWN"]
BUCKETS = (("0", lambda r: r["rd"] == 0), ("1-2", lambda r: 1 <= r["rd"] <= 2), ("3+", lambda r: r["rd"] >= 3))
KINDS = (("first", "HARDEN"), ("resumed", "OPEN"))
if "--list" in sys.argv:
    k, b = sys.argv[sys.argv.index("--list") + 1:sys.argv.index("--list") + 3]
    frm = dict(KINDS)[k]
    f = dict(BUCKETS)[b]
    for r in known:
        if r["frm"] == frm and r["edits"] and f(r):
            print(f"  {r['name']} conv L{r['line']} c{r['cycle']} rd={r['rd']} min={r['mins']} next={r['outcome']}")
    sys.exit(0)
for label, frm in KINDS:
    sel = [r for r in known if r["frm"] == frm]
    print(f"{label}: {len(sel)} traversals over {len({r['name'] for r in sel})} runs; "
          f"no edit detected since the previous converged write: {sum(1 for r in sel if not r['edits'])}")
    for b, f in BUCKETS:
        rs = [r for r in sel if r["edits"] and f(r)]
        if not rs:
            print(f"  reads after last edit {b:3}: none")
            continue
        esc = sum(r["outcome"] == "ESCALATED" for r in rs)
        mins = sorted(r["mins"] for r in rs)
        print(f"  reads after last edit {b:3}: traversals={len(rs):3} escalated={esc:3} ({esc / len(rs) * 100:3.0f}%) "
              f"runs={len({r['name'] for r in rs}):3} median minutes after last edit={mins[len(mins) // 2]:.1f}")
