#!/usr/bin/env python3
"""Scores the blind classification of the relocated-runs addendum, and writes the combined inputs for
the note's own class-rates.py.

Usage: score_blind.py <note artifacts dir> <addendum dir> <classifier reply file>
The reply holds one line per packet block, `<k> <CLASS> — <reason>`, in the packet's order.

It prints, for the 12 calibration blocks, the original reader's class beside the blind one, and how
many agree:
- exactly;
- by the groups the note's tables use: R+Rc, M+N, 0, U.

It then writes into the addendum dir:
- combined-digest.txt: the note's post-edit-digest.txt, then relocated-digest.txt;
- combined-classes.txt: the note's classes, then the blind classes of the 8 relocated blocks, numbered
  on. class-rates.py reads the two side by side.

It fails if any packet block is unclassified, or a class is not one of the rubric's six."""
import json
import pathlib
import re
import sys

note, add, reply = pathlib.Path(sys.argv[1]), pathlib.Path(sys.argv[2]), pathlib.Path(sys.argv[3])
CLASSES = {"R", "Rc", "M", "N", "0", "U"}
GROUP = {"R": "R+Rc", "Rc": "R+Rc", "M": "M+N", "N": "M+N", "0": "0", "U": "U"}
key = {int(k): v for k, v in json.loads((add / "blind-key-private.json").read_text()).items()}
blind = {}
for line in reply.read_text().splitlines():
    m = re.match(r"^\s*(\d+)\s+(Rc|R|M|N|0|U)\b", line)
    if m:
        blind[int(m.group(1))] = m.group(2)
missing = sorted(set(key) - set(blind))
if missing:
    sys.exit(f"unclassified packet blocks: {missing}")
exact = grouped = 0
print("calibration blocks: k | original | blind")
for k in sorted(key):
    if key[k]["source"] != "calibration":
        continue
    o, b = key[k]["original_class"], blind[k]
    exact += o == b
    grouped += GROUP[o] == GROUP[b]
    print(f"  {k:2} | {o:2} | {b:2}{'' if o == b else '   <- differs'}")
n = sum(1 for k in key if key[k]["source"] == "calibration")
print(f"agreement: exact {exact} of {n}, by table group {grouped} of {n}")


def blocks(path):
    res, cur = [], None
    for line in open(path, errors="replace"):
        if line.startswith("== "):
            cur = [line]
            res.append(cur)
        elif cur is not None:
            cur.append(line)
    return res


reloc = blocks(add / "relocated-digest.txt")
by_header = {key[k]["header"]: blind[k] for k in key if key[k]["source"] == "relocated"}
orig_classes = [l for l in open(note / "post-edit-classes.txt") if l.strip() and not l.startswith("#")]
start = len(orig_classes)
new_lines = []
print("relocated blocks: header | blind class")
for i, b in enumerate(reloc, 1):
    head = b[0].rstrip("\n")
    c = by_header[head]
    new_lines.append(f"{start + i} {c}\n")
    print(f"  {head} | {c}")
(add / "combined-digest.txt").write_text(open(note / "post-edit-digest.txt").read() + "".join("".join(b) for b in reloc))
(add / "combined-classes.txt").write_text("".join(orig_classes) + "".join(new_lines))
print(f"wrote combined-digest.txt ({start + len(reloc)} blocks) and combined-classes.txt")
