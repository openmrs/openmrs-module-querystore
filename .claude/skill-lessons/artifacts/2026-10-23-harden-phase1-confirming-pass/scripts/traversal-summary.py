#!/usr/bin/env python3
"""Summarizes phase1-traversals.py output: escalation rate of the Phase 2 that follows a Phase 1
traversal, by how many passes the orchestrator recorded in it, for the first traversal of a run and
for one resumed after an escalation. Counts traversals and, separately, runs (a run counts once per
bucket, escalated if any of its traversals in that bucket escalated), so one long run cannot carry
a bucket alone.

Usage: traversal-summary.py <phase1-traversals output> [--list <bucket>]
  --list prints the traversals of one bucket, e.g. `resumed:2`.
"""
import collections
import re
import sys

ROW = re.compile(r"^(\S+) L(\d+)\s+c(\S+)\s+(T?)\s*span=\s*([\d.]+) .*? next=(\w+) .*passes=(\d+) from=L(\d+):(\w+)")
rows, unparsed = [], 0
for line in open(sys.argv[1]):
    if " L" not in line or "span=" not in line:
        continue
    m = ROW.search(line)
    if not m:
        unparsed += 1
        continue
    rows.append(dict(name=m.group(1), line=int(m.group(2)), cycle=m.group(3), terminal=bool(m.group(4)),
                     span=float(m.group(5)), outcome=m.group(6), passes=int(m.group(7)),
                     start=int(m.group(8)), frm=m.group(9)))
if unparsed:
    print(f"UNPARSED traversal lines: {unparsed}")


def kind(r):
    return {"HARDEN": "first", "OPEN": "resumed"}.get(r["frm"], "other:" + r["frm"])


def bucket(r):
    return "1" if r["passes"] == 1 else "2" if r["passes"] == 2 else "3+"


known = [r for r in rows if not r["terminal"] and r["outcome"] != "UNKNOWN"]
print(f"traversals: {len(rows)}, terminal: {sum(r['terminal'] for r in rows)}, "
      f"non-terminal unknown outcome: {sum(1 for r in rows if not r['terminal'] and r['outcome'] == 'UNKNOWN')}, "
      f"known: {len(known)}")
if "--list" in sys.argv:
    want = sys.argv[sys.argv.index("--list") + 1]
    for r in known:
        if f"{kind(r)}:{bucket(r)}" == want:
            print(f"  {r['name']} start L{r['start']} conv L{r['line']} c{r['cycle']} span={r['span']} next={r['outcome']}")
    sys.exit(0)
for k in sorted({kind(r) for r in known}):
    sel = [r for r in known if kind(r) == k]
    print(f"{k}: {len(sel)} traversals over {len({r['name'] for r in sel})} runs")
    for b in ("1", "2", "3+"):
        rs = [r for r in sel if bucket(r) == b]
        if not rs:
            continue
        esc = sum(r["outcome"] == "ESCALATED" for r in rs)
        runs = collections.defaultdict(bool)
        for r in rs:
            runs[r["name"]] |= r["outcome"] == "ESCALATED"
        spans = sorted(r["span"] for r in rs)
        print(f"  passes={b:3} traversals={len(rs):3} escalated={esc:3} ({esc / len(rs) * 100:3.0f}%)   "
              f"runs={len(runs):3} escalated={sum(runs.values()):3} ({sum(runs.values()) / len(runs) * 100:3.0f}%)   "
              f"median span={spans[len(spans) // 2]:.1f} min")
