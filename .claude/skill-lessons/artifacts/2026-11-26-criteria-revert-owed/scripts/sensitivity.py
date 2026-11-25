"""Which single change of reading in criteria-rounds.tsv moves the longest run to three or more on two or more PRs?
For each round: flip its defect column (a breaking defect read as none, or none read as one) and, for each round
without an owed agent, read one more agent as owed; recompute both orders' longest runs, as written and amended,
with streaks.py's own functions. Usage: sensitivity.py <streaks.py> <criteria-rounds.tsv>"""
import csv, importlib.util, sys

spec = importlib.util.spec_from_file_location("streaks", sys.argv[1])
st = importlib.util.module_from_spec(spec)
spec.loader.exec_module(st)
with open(sys.argv[2]) as f:
    rows = list(csv.DictReader((line for line in f if not line.startswith("#")), delimiter="\t"))


def results(rs):
    out = {}
    for key in ("launch", "last_write"):
        ordered = sorted(rs, key=lambda r: (r[key], r["round"]))
        for amended in (False, True):
            run = st.longest(ordered, amended)
            out[(key, amended)] = (len(run), st.met(run), [r["round"] for r in run])
    return out


base = results(rows)
print("base: " + "; ".join(f"{k[0]}/{'amended' if k[1] else 'as written'} {v[0]}{' MET' if v[1] else ''}"
                          for k, v in base.items()))
for i, r in enumerate(rows):
    variants = [("defect " + ("none" if r["defect_by"] != "-" else "one"),
                 dict(r, defect_by="-" if r["defect_by"] != "-" else "x"))]
    if r["owed"] == "-":
        variants.append(("an agent owed", dict(r, owed="x:R")))
    for label, alt in variants:
        res = results(rows[:i] + [alt] + rows[i + 1:])
        changed = [(k, v) for k, v in res.items() if v[1] != base[k][1]]
        if changed:
            for k, v in changed:
                print(f"{r['round']:12s} {label:14s} -> {k[0]}/{'amended' if k[1] else 'as written'}: "
                      f"{'MET' if v[1] else 'not met'} ({', '.join(v[2])})")
print("rounds whose single flip changes no verdict are not listed")

# Each reading of "a defect" this pass's measurement used, taken the other way, flips every round whose counted defects
# all rest on it (from criteria-rounds.tsv's record lines): where the ticket asks for tests, a survivor whose change a
# caller can see (#71 only adds tests for coverage criteria, pr-review SKILL.md:128; #83's criteria include "covered by
# tests", its round record :2); :7761's reproduced-then-set-aside items; a defect the base shares on code the PR
# rewrote. Rounds with another kind of defect keep it: #71 round 9's locale failure, #80 round 1's trim change, #6502's
# OutOfMemoryError.
READINGS = {
    "a survivor is no defect": ["pr71-r8", "pr71-r10", "pr71-r11", "pr71-r12", "sm83-r1"],
    "a set-aside item is no defect": ["sm77-r1", "sm80-r2", "core6835-r3"],
    "a defect the base shares is no defect": ["pr85-r1"],
}
for without in ([], ["pr57-r1"]):
    kept = [r for r in rows if r["round"] not in without]
    print(f"== readings taken the other way{' without ' + ', '.join(without) if without else ''}")
    for label, flip in READINGS.items():
        res = results([dict(r, defect_by="-") if r["round"] in flip else r for r in kept])
        print(f"   {label:40s} " + "; ".join(f"{k[0]}/{'amended' if k[1] else 'as written'} "
                                             f"{'MET' if v[1] else 'not met'}" for k, v in res.items()))
