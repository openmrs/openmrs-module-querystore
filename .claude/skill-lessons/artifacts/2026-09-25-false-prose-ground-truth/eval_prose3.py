#!/usr/bin/env python3
"""EXPLORATORY variants for the record only — the verdict rests on eval_prose2.py's strict metric."""
import importlib.util, json, re, statistics, sys, collections
LISTER = "/Users/danielkayiwa/.claude/skill-lessons/artifacts/2026-09-25-false-prose-ground-truth/prose-candidates.py"
def load():
    spec = importlib.util.spec_from_file_location("pc", LISTER); m = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(m); return m
repo, path = sys.argv[1], sys.argv[2]
rows = [json.loads(l) for l in open(path) if l.strip() and json.loads(l).get("source") == "record"]
def run(pc, label, tol):
    cache, res = {}, collections.Counter()
    for r in rows:
        k = (r["base"], r["prefix"])
        if k not in cache:
            g = pc.candidates(repo, *k)
            cache[k] = {(c["path"], c["line"]) for c in g["added_universals"] + g["naming"]}
        hit = any((r["file"], int(r["line"]) + d) in cache[k] for d in range(-tol, tol + 1))
        res[(r["introduced_by"], hit)] += 1
    sizes = [len(v) for v in cache.values()]
    tot = sum(res[(i, True)] for i in ("ADDED", "OLD"))
    print(f"{label}: caught {tot}/{len(rows)} = {tot/len(rows):.0%}  (ADDED {res[('ADDED',True)]}/35, OLD {res[('OLD',True)]}/24);  output median {statistics.median(sizes)} max {max(sizes)}")
pc = load(); run(pc, "as designed, ±2 lines", 2)
pc2 = load()
def broad(code):
    ids = set()
    for lit in pc2.STRING.findall(code):
        ids.update(pc2.DOTTED.findall(lit)); ids.update(t for t in pc2.TOKEN.findall(lit) if pc2._interesting(t))
    ids.update(t for t in pc2.TOKEN.findall(pc2.STRING.sub('""', code)) if pc2._interesting(t))
    return ids | pc.__dict__["_subjects"](code)
pc2._subjects = broad
run(pc2, "broad B (every identifier on a changed line), strict", 0)
