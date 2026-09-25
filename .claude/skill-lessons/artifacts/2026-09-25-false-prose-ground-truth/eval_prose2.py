#!/usr/bin/env python3
"""Recall of prose-candidates.py over the ground truth, with two calibration controls.

Drives the real lister (imported). Caught = the false line's (file, line) is listed under A or B for
base..prefix. Controls: (1) the base rate — listed lines / all prose lines in the prefix tree; (2) the
same file's prose lines 40 above and below each false line, which a discriminating lister should
catch far less often than the false lines themselves.
"""
import importlib.util, json, statistics, sys, collections
LISTER = "/Users/danielkayiwa/.claude/skill-lessons/artifacts/2026-09-25-false-prose-ground-truth/prose-candidates.py"
spec = importlib.util.spec_from_file_location("pc", LISTER); pc = importlib.util.module_from_spec(spec)
spec.loader.exec_module(pc)
repo, path = sys.argv[1], sys.argv[2]
rows = [json.loads(l) for l in open(path) if l.strip()]
cache, tree_prose = {}, {}
def listed(base, prefix):
    k = (base, prefix)
    if k not in cache:
        got = pc.candidates(repo, base, prefix)
        cache[k] = ({(c["path"], c["line"]) for c in got["added_universals"]},
                    {(c["path"], c["line"]) for c in got["naming"]})
        files = pc._tree(repo, prefix)
        tree_prose[k] = {(p, n) for p, t in files.items() for n in pc.prose_map(p, t)}
    return cache[k]
def report(label, sel):
    if not sel: print(label, "— none"); return
    res, ctl_hit, ctl_n = [], 0, 0
    for r in sel:
        a, b = listed(r["base"], r["prefix"])
        w = (r["file"], int(r["line"]))
        res.append((r, w in a, w in b))
        prose = tree_prose[(r["base"], r["prefix"])]
        for off in (-40, 40):
            c = (r["file"], int(r["line"]) + off)
            if c in prose:
                ctl_n += 1; ctl_hit += (c in a or c in b)
    caught = [x for x in res if x[1] or x[2]]
    by = collections.Counter((x[0]["introduced_by"], x[1] or x[2]) for x in res)
    changes = {(r["base"], r["prefix"]) for r in sel}
    sizes = [len(cache[k][0]) + len(cache[k][1]) for k in changes]
    rates = [(len(cache[k][0]) + len(cache[k][1])) / max(1, len(tree_prose[k])) for k in changes]
    print(f"{label}: instances {len(res)}, caught {len(caught)} = {len(caught)/len(res):.0%}"
          f"  (ADDED {by[('ADDED',True)]}/{by[('ADDED',True)]+by[('ADDED',False)]}, OLD {by[('OLD',True)]}/{by[('OLD',True)]+by[('OLD',False)]})")
    print(f"   changes {len(changes)}: output lines median {statistics.median(sizes)}  min {min(sizes)}  max {max(sizes)};"
          f"  base rate median {statistics.median(rates):.2%} of the tree's prose lines")
    print(f"   control, prose lines ±40 from each false line: {ctl_hit}/{ctl_n} = {ctl_hit/max(1,ctl_n):.0%} listed")
    return res
prim = report("PRIMARY (source=record)", [r for r in rows if r.get("source") == "record"])
report("ALL (record + transcript)", rows)
report("record, excluding borderline and false_before_change",
       [r for r in rows if r.get("source") == "record" and not r.get("borderline") and not r.get("false_before_change")])
for r, ina, inb in prim:
    print(json.dumps({"pr": r["pr"], "claim": r["claim"], "intro": r["introduced_by"], "A": ina, "B": inb,
                      "text": r["false_text"][:90]}))
