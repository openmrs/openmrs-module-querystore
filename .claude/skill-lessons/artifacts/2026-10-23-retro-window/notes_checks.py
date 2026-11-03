#!/usr/bin/env python3
"""The checks the 2026-10-23 retro ran on the four 2026-10-18 pr-review measurement notes, beyond
each note's own scripts/score.py. Each search prints its positive control first.

Usage: notes_checks.py <skill-lessons artifacts dir>   (default ~/.claude/skill-lessons/artifacts)"""
import collections
import glob
import json
import pathlib
import re
import sys

A = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else pathlib.Path.home() / ".claude/skill-lessons/artifacts")


def load(d):
    key = json.load(open(d / "KEY-private.json"))["slots"]
    amap = json.load(open(d / "adjudication-map-private.json"))
    adj = json.load(open(d / "adj/adjudication.json"))
    adj = adj["verdicts"] if isinstance(adj, dict) else adj
    inp = {x["key"]: x for x in json.load(open(d / "adj/adjudication-input.json"))}
    return key, amap, adj, inp


# 1. reviewer-brief-556 has no adjudication: does any finding assert S1 (condition-mediated findings)?
d = A / "2026-10-18-reviewer-brief-556"
pat = re.compile(r"condition[- ]?mediated|conditionmediated", re.I)
key = json.load(open(d / "KEY-private.json"))["slots"]
print("1. S1 search; control, PROTOCOL.md hits:", len(pat.findall((d / "PROTOCOL.md").read_text())))
for f in sorted(glob.glob(str(d / "out/wt-*.json"))):
    doc = json.load(open(f))
    wt = pathlib.Path(f).stem
    fs = doc.get("findings", [])
    print(f"   {wt} arm={key[wt]['arm']} findings={len(fs)} finding-hits={[i for i, x in enumerate(fs) if pat.search(json.dumps(x))]} whole-file-hits={len(pat.findall(json.dumps(doc)))}")

# 2. queue96: which runs parked the unobserved rollback in could_not_verify?
d = A / "2026-10-18-lens-replay-queue96"
pat = re.compile(r"transaction|roll ?back|rolled back|TransactionInterceptor", re.I)
key = json.load(open(d / "KEY-private.json"))["slots"]
print("2. could_not_verify rollback items; control, PROTOCOL.md hits:", len(pat.findall((d / "PROTOCOL.md").read_text())))
by_arm = collections.Counter()
for f in sorted(glob.glob(str(d / "out/wt-*.json"))):
    doc = json.load(open(f))
    wt = pathlib.Path(f).stem
    hit = any(pat.search(json.dumps(x)) for x in doc.get("could_not_verify") or [])
    by_arm[key[wt]["arm"]] += hit
    print(f"   {wt} arm={key[wt]['arm']} items={len(doc.get('could_not_verify') or [])} rollback-item={hit}")
print("   runs with one, by arm:", dict(by_arm), "total:", sum(by_arm.values()))

# 3. Filed dispositions of the privilege defects: 6551 L04, queue96 K1.
for name, target in (("2026-10-18-heldout-6551", "L04"), ("2026-10-18-lens-replay-queue96", "K1")):
    key, amap, adj, inp = load(A / name)
    rows = sorted((amap[v["key"]]["wt"], key[amap[v["key"]]["wt"]]["arm"], inp[v["key"]].get("disposition"))
                  for v in adj if target in (v.get("matches") or []))
    print(f"3. {name} {target}:", rows, dict(collections.Counter(r[2] for r in rows)))

# 4. union-routing-556: risk items and their adjudicated class, by arm.
key, amap, adj, inp = load(A / "2026-10-18-union-routing-556")
c = collections.Counter()
for v in adj:
    m = amap[v["key"]]
    if m["kind"] != "finding":
        c[(key[m["wt"]]["arm"], str(v.get("class")))] += 1
print("4. union-routing-556 risks by (arm, class):", sorted(c.items()))
