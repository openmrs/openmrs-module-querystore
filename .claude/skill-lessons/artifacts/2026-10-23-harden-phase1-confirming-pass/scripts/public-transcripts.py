#!/usr/bin/env python3
"""Prints, one per line, every transcript path named by a public run record (a top-level *.md of the
skill-lessons store; confidential/ is never read) that is still on disk, each once.

Usage: public-transcripts.py <skill-lessons dir>
Exits 2 when no record names a transcript on disk, so an empty list cannot pass for a clean one.
"""
import os
import pathlib
import re
import sys

home = os.path.expanduser("~")
seen = {}
for rec in sorted(pathlib.Path(sys.argv[1]).glob("*.md")):
    for line in rec.read_text(errors="replace").splitlines():
        m = re.match(r"^transcript:\s*(\S+)", line)
        if m:
            p = m.group(1).replace("~", home, 1)
            if os.path.isfile(p) and p not in seen:
                seen[p] = rec.name
for p, rec in seen.items():
    print(f"{p}\t{rec}")
sys.exit(0 if seen else 2)
