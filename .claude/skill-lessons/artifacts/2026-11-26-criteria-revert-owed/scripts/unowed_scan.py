"""For every Step 2 agent of the rounds in criteria-rounds.tsv that the TSV does not mark owed, print each sentence of
its report that speaks of the machine, the load, time or a run that did not finish, wider than the 2026-11-25 load
pattern, so each can be read. Usage: unowed_scan.py <criteria-rounds.tsv> <criteria_rounds_owed.py>"""
import csv, importlib.util, os, re, sys
spec = importlib.util.spec_from_file_location("owed", sys.argv[2])
ow = importlib.util.module_from_spec(spec)
spec.loader.exec_module(ow)
WIDE = re.compile(r"load|machine|slow|busy|other (session|build|process|mvn|maven)|processes|saturat|contention|"
                  r"memory|time limit|budget|minute|did not (finish|run|complete)|didn.t (finish|run|complete)|"
                  r"never (finished|completed|reached)|not (finish|run|complete)|unfinished|stopped|killed|timed? ?out", re.I)
with open(sys.argv[1]) as f:
    rows = list(csv.DictReader((l for l in f if not l.startswith("#")), delimiter="\t"))
dates = dict(ow.ROUNDS)
scanned = flagged = 0
for r in rows:
    owed = {a.split(":")[0] for a in r["owed"].split(",")} if r["owed"] != "-" else set()
    for name, d in ow.agents(r["round"], dates[r["round"]]):
        short = name[len(r["round"]) + 1:]
        if short in owed:
            continue
        scanned += 1
        try:
            text = open(os.path.join(d, "log", "report.md"), errors="replace").read()
        except FileNotFoundError:
            print(f"== {name}: NO REPORT")
            continue
        sents = [s.strip() for s in re.split(r"(?<=[.!?])\s+|\n+", text) if WIDE.search(s)]
        if sents:
            flagged += 1
            print(f"== {name} ({len(sents)} sentences)")
            for s in sents[:8]:
                print("   - " + s[:260])
print(f"agents not marked owed: {scanned}; with a sentence to read: {flagged}")
