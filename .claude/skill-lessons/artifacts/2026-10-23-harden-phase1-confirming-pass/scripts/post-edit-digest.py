#!/usr/bin/env python3
"""For each non-terminal Phase 1 traversal with a known outcome, prints what the orchestrator wrote and
ran between its last edit (since the previous converged write) and the converged write: its prose, and
the description of every tool call. That window holds the pass the converged write says found nothing,
so this is what a reader classifies as a re-review of the slice or not.

Usage: post-edit-digest.py <phase1-traversals output> <transcripts tsv> <harden-events.py> [<kind>]
  <kind> is first or resumed (default: both). One block per traversal, headed
  `== <record> c<cycle> next=<outcome> window L<from>-L<to> (<min> min)`.
"""
import re
import subprocess
import sys

ROW = re.compile(r"^(\S+) L(\d+)\s+c(\S+)\s+(T?)\s*span=.*? next=(\w+) .*from=L\d+:(\w+) .*post-edit: edits=(\d+) "
                 r"rd=\d+ open=\d+ min=(\S+) post-from=L(\d+)")
want = sys.argv[4] if len(sys.argv) > 4 else None
records = {}
for line in open(sys.argv[2]):
    path, rec = line.rstrip("\n").split("\t")
    records[path.rsplit("/", 1)[-1]] = (path, rec)
cache = {}
for line in open(sys.argv[1]):
    m = ROW.search(line)
    if not m or m.group(4) or m.group(5) == "UNKNOWN":
        continue
    kind = {"HARDEN": "first", "OPEN": "resumed"}.get(m.group(6))
    if kind is None or (want and kind != want):
        continue
    name, conv, cycle, outcome = m.group(1), int(m.group(2)), m.group(3), m.group(5)
    lo, mins = int(m.group(9)), m.group(8)
    path, rec = records[name]
    if path not in cache:
        out = subprocess.run([sys.executable, sys.argv[3], path, "--text"], capture_output=True, text=True)
        if out.returncode != 0:
            sys.exit(f"harden-events.py failed on {path}: {out.stderr.strip()}")
        cache[path] = out.stdout.splitlines()
    print(f"== {rec} c{cycle} next={outcome} window L{lo}-L{conv} ({mins} min) edits={m.group(7)}")
    for ev in cache[path]:
        n = int(ev.split()[0][1:])
        if lo <= n <= conv and not ev.split()[2] in ("EDIT",):
            print("   " + ev[:240])
