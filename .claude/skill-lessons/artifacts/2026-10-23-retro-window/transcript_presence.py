#!/usr/bin/env python3
"""How many of the transcripts public run records name are still on disk?

Usage: transcript_presence.py <skill-lessons dir>   (default ~/.claude/skill-lessons)
Reads the `transcript:` lines of the top-level *.md (never confidential/, never REJECTED.md), keeps
the ones ending in .jsonl, and prints how many distinct paths are on disk and how many are gone, the
record-date range of each, and every gone path whose record is under 30 days old, with whether its
project directory still exists. Two known paths are printed first as controls: one on disk on
2026-10-23 and one deleted that day."""
import datetime
import os
import pathlib
import re
import sys

home = os.path.expanduser("~")
root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else home + "/.claude/skill-lessons")
on = home + "/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-477/5092c704-ef8f-4270-9fba-4620c6ca25b1.jsonl"
off = home + "/.claude/projects/-Users-danielkayiwa-Projects-openmrs-chartsearchai/b2cc1733-01a0-4706-a6cf-518055292fb4.jsonl"
print("control on disk:", os.path.isfile(on), "| control deleted 2026-10-23:", os.path.isfile(off))
named = {}
for rec in sorted(root.glob("*.md")):
    if rec.name == "REJECTED.md":
        continue
    for line in rec.read_text(errors="replace").splitlines():
        m = re.match(r"^transcript:\s*(\S+)", line)
        if m and m.group(1).endswith(".jsonl"):
            named.setdefault(m.group(1).replace("~", home, 1), rec.name)
gone = sorted((r, p) for p, r in named.items() if not os.path.isfile(p))
have = sorted(r for p, r in named.items() if os.path.isfile(p))
print("distinct transcript paths named:", len(named), "| on disk:", len(have), "| gone:", len(gone))
if gone:
    print("gone, record dates:", gone[0][0][:10], "..", gone[-1][0][:10])
if have:
    print("on disk, record dates:", have[0][:10], "..", have[-1][:10])
cut = (datetime.date.today() - datetime.timedelta(days=30)).isoformat()
for r, p in gone:
    if r[:10] > cut:
        print("gone though its record is under 30 days old:", r, "| project dir exists:",
              os.path.isdir(os.path.dirname(p)), "|", p.replace(home, "~"))
