#!/usr/bin/env python3
"""Which models and Claude Code versions ran each list of transcripts?

Usage: models.py <label>=<list> ...   (one path per line in each list)
For each list it prints how many transcripts used each model (a transcript can count under more than
one), and each transcript's Claude Code version range as `min..max`. A pool stream log carries no
version, so a list of those prints none."""
import collections
import json
import sys

for arg in sys.argv[1:]:
    label, lst = arg.split("=", 1)
    models, versions = collections.Counter(), collections.Counter()
    for p in [l.split("\t")[0].strip() for l in open(lst) if l.strip()]:
        ms, vs = set(), set()
        for line in open(p, errors="replace"):
            try:
                r = json.loads(line)
            except ValueError:
                continue
            m = (r.get("message") or {}).get("model")
            if m and not m.startswith("<"):
                ms.add(m)
            if r.get("version"):
                vs.add(r["version"])
        for m in ms:
            models[m] += 1
        if vs:
            versions[min(vs) if len(vs) == 1 else min(vs) + ".." + max(vs)] += 1
    print(f"{label}: models {dict(models)}")
    print(f"{label}: Claude Code versions {dict(sorted(versions.items()))}")
