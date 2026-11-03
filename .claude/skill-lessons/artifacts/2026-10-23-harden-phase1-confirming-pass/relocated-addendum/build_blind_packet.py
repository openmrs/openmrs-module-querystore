#!/usr/bin/env python3
"""Builds the blind classification packet for the relocated-runs addendum.

Usage: build_blind_packet.py <note artifacts dir> <out dir>
Takes the 8 digest blocks of the relocated runs (relocated-digest.txt), and from the note's original
digest (post-edit-digest.txt) 2 calibration blocks of each class the original reader assigned
(post-edit-classes.txt), chosen with a fixed seed. Every header loses its record name and its
`next=<outcome>`, so the classifier sees neither the run nor what its Phase 2 did. Writes:
  blind-packet.md         the rubric, then the shuffled blocks as `== block <k>`
  blind-key-private.json  for each k: its source (relocated|calibration), its original header, and
                          for a calibration block the original reader's class
"""
import json
import pathlib
import random
import re
import sys

note = pathlib.Path(sys.argv[1])
out = pathlib.Path(sys.argv[2])


def blocks(path):
    cur = None
    res = []
    for line in open(path, errors="replace"):
        if line.startswith("== "):
            cur = [line.rstrip("\n")]
            res.append(cur)
        elif cur is not None:
            cur.append(line.rstrip("\n"))
    return res


orig = blocks(note / "post-edit-digest.txt")
classes = [l.split() for l in open(note / "post-edit-classes.txt") if l.strip() and not l.startswith("#")]
assert len(orig) == len(classes), (len(orig), len(classes))
rubric = [l[2:].rstrip("\n") for l in open(note / "post-edit-classes.txt") if l.startswith("# ")]
new = blocks(out / "relocated-digest.txt")
rng = random.Random(20261023)
by_class = {}
for i, (_, c) in enumerate(classes):
    by_class.setdefault(c, []).append(i)
calib = []
for c in sorted(by_class):
    calib += [(i, c) for i in rng.sample(by_class[c], 2)]
items = [("relocated", b, None) for b in new] + [("calibration", orig[i], c) for i, c in calib]
rng.shuffle(items)
strip = re.compile(r"^== \S+ (c\S+) next=\w+ (.*)$")
key, lines = {}, []
lines.append("# Classify each block below with exactly one class from this rubric.")
lines.append("")
lines += rubric
lines.append("")
for k, (src, b, c) in enumerate(items, 1):
    m = strip.match(b[0])
    lines.append(f"== block {k} {m.group(1)} {m.group(2)}")
    lines += b[1:]
    lines.append("")
    key[k] = {"source": src, "header": b[0], "original_class": c}
(out / "blind-packet.md").write_text("\n".join(lines) + "\n")
(out / "blind-key-private.json").write_text(json.dumps(key, indent=1) + "\n")
print(f"blocks: {len(items)} ({len(new)} relocated, {len(calib)} calibration)")
