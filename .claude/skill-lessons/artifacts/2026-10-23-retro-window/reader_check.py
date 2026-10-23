#!/usr/bin/env python3
"""The known-bad control for gate_sentences.py on real data: does the same reader see assistant text
in these transcripts at all? A reader that saw none would report zero gate sentences too.

Usage: reader_check.py <tsv, transcript path in column 1>
Prints the assistant text blocks read, how many name "Phase 1", in how many transcripts, and up to
four that also say "converged". Missing transcripts are listed, not counted. Exits 2 if none read."""
import json
import os
import re
import sys

paths = [l.split("\t")[0].strip() for l in open(sys.argv[1]) if l.strip()]
blocks = p1 = conv = files_p1 = read = 0
examples = []
for p in paths:
    if not os.path.isfile(p):
        print("MISSING", p)
        continue
    read += 1
    seen = False
    for line in open(p, errors="replace"):
        try:
            r = json.loads(line)
        except ValueError:
            continue
        m = r.get("message") or {}
        if m.get("role") != "assistant":
            continue
        c = m.get("content")
        for b in (c if isinstance(c, list) else []):
            if isinstance(b, dict) and b.get("type") == "text":
                t = b.get("text", "")
                blocks += 1
                if re.search(r"Phase 1", t):
                    p1 += 1
                    seen = True
                    if re.search(r"converged", t, re.I):
                        conv += 1
                        if len(examples) < 4:
                            i = t.find("Phase 1")
                            examples.append((os.path.basename(p)[:8], t[max(0, i - 80):i + 200].replace("\n", " ")))
    files_p1 += seen
print("read:", read, "of", len(paths), "| assistant text blocks:", blocks, "| naming \"Phase 1\":", p1,
      "in", files_p1, "transcripts | of those also \"converged\":", conv)
for e in examples:
    print(" ", e)
sys.exit(0 if read else 2)
