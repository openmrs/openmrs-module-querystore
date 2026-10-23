#!/usr/bin/env python3
"""Would a wider version of skill-lint.py's `stated_counts` have caught harden's "say BOTH out loud"
over three blockquoted sentences? Three widenings, each reported with every hit, so the false ones
can be read rather than counted:

  wide   any number word (one..ten) or "both", no noun list, `>` lines counted as list items
  both   "both", any case, the same list rules
  BOTH   "BOTH" in capitals only, the same list rules (fitted to the one known instance)

As in skill-lint, a hit needs a line ending in ":" with a list starting within 3 lines whose length
differs from the word's value. Run on the skills as they stood before the 2026-10-23 retro's harden
0.44.1 edit, which removes the one true hit.

Usage: count_widening.py <skills root>   (default ~/.claude/skills; evidence.md is skipped)"""
import pathlib
import re
import sys

WORDS = {"one": 1, "two": 2, "three": 3, "four": 4, "five": 5, "six": 6, "seven": 7, "eight": 8,
         "nine": 9, "ten": 10, "both": 2}
ITEM = re.compile(r"^\s*(\d+\.|[-*]|>)\s")


def list_after(lines, i):
    j = i + 1
    while j < len(lines) and j <= i + 3 and not ITEM.match(lines[j]):
        j += 1
    if j >= len(lines) or j > i + 3:
        return 0
    kind = ITEM.match(lines[j]).group(1)
    kind = "n" if kind[0].isdigit() else kind
    indent = len(lines[j]) - len(lines[j].lstrip())
    n, k = 0, j
    while k < len(lines):
        s = lines[k]
        if not s.strip():
            k += 1
            continue
        cur = len(s) - len(s.lstrip())
        if cur < indent:
            break
        m2 = ITEM.match(s)
        if cur == indent and m2:
            k2 = m2.group(1)
            k2 = "n" if k2[0].isdigit() else k2
            if k2 != kind:
                break
            n += 1
        elif cur == indent and n:
            break
        k += 1
    return n


VARIANTS = {
    "wide": re.compile(r"\b(" + "|".join(WORDS) + r")\b", re.I),
    "both": re.compile(r"\b(both)\b", re.I),
    "BOTH": re.compile(r"\b(BOTH)\b"),
}
root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else pathlib.Path.home() / ".claude/skills")
files = sorted(p for p in root.glob("*/*.md") if p.name != "evidence.md")
for name, pat in VARIANTS.items():
    hits = []
    for p in files:
        lines = p.read_text().split("\n")
        for i, line in enumerate(lines):
            if not line.rstrip().endswith(":"):
                continue
            ms = list(pat.finditer(line))
            if not ms:
                continue
            n = list_after(lines, i)
            for m in ms:
                if n and WORDS[m.group(1).lower()] != n:
                    hits.append(f"{p.parent.name}/{p.name}:{i + 1} says '{m.group(1)}' over {n}: {line.strip()[:100]}")
    print(f"== {name}: {len(hits)} hit(s) over {len(files)} files")
    for h in hits:
        print("  ", h)
