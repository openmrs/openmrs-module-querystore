#!/usr/bin/env python3
"""How many of the transcripts public run records name are still on disk, at the path named or
anywhere else?

Usage: transcript_presence.py <skill-lessons dir>   (default ~/.claude/skill-lessons)
Reads the `transcript:` lines of the top-level *.md (never confidential/, never REJECTED.md), keeps
the ones ending in .jsonl, and prints how many distinct paths are on disk and how many are gone, the
record-date range of each, and every gone path whose record is under 30 days old, with whether its
project directory still exists. Three known paths are printed first as controls: one on disk on
2026-10-23, one deleted that day, and one relocated before it.

Then it looks for each gone path's file name (the session id) in every folder under
~/.claude/projects. Claude Code moves a session's transcript between project folders as the session
enters and leaves a worktree (its `relocated` records), so a path that was right when the record
was written can later point at nothing while the transcript survives. A gone path is RELOCATED when
that file exists in another folder, and ABSENT when it exists in none."""
import datetime
import os
import pathlib
import re
import sys

home = os.path.expanduser("~")
projects = pathlib.Path(home) / ".claude/projects"
root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else home + "/.claude/skill-lessons")
on = home + "/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-477/5092c704-ef8f-4270-9fba-4620c6ca25b1.jsonl"
off = home + "/.claude/projects/-Users-danielkayiwa-Projects-openmrs-chartsearchai/b2cc1733-01a0-4706-a6cf-518055292fb4.jsonl"
moved = "ebc78818-5b31-4fed-9943-24ca301a3943.jsonl"
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

print()
print("by session id, across", str(projects).replace(home, "~") + ":")
found_moved = sorted(projects.glob("*/" + moved))
print("control relocated before 2026-10-23 (named under chartsearchai-527, lives under chartsearchai):",
      [str(x.parent.name) for x in found_moved])
relocated, absent = [], []
for r, p in gone:
    hits = [x for x in projects.glob("*/" + os.path.basename(p)) if str(x) != p]
    (relocated if hits else absent).append((r, p, hits))
print("gone paths:", len(gone), "| RELOCATED (same file in another folder):", len(relocated),
      "| ABSENT (in no folder):", len(absent))
for r, p, hits in relocated:
    print("  relocated:", r, "|", os.path.basename(os.path.dirname(p)), "->",
          ", ".join(h.parent.name for h in hits))
if absent:
    print("absent, record dates:", absent[0][0][:10], "..", absent[-1][0][:10])
    for r, p, _ in absent:
        if r[:10] > cut:
            print("  absent though its record is under 30 days old:", r, "|", p.replace(home, "~"))
