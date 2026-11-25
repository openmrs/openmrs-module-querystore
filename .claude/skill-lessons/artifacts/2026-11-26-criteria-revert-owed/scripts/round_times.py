"""Rewrite criteria-rounds.tsv's launch and last_write columns, to the second, from each round's Step 2 agents' folders:
the earliest log/BRIEF.md mtime and the latest log/out.jsonl mtime (local time). Usage:
round_times.py <criteria-rounds.tsv> <criteria_rounds_owed.py>"""
import datetime, importlib.util, os, sys
spec = importlib.util.spec_from_file_location("owed", sys.argv[2])
ow = importlib.util.module_from_spec(spec)
spec.loader.exec_module(ow)
fmt = lambda t: datetime.datetime.fromtimestamp(t).isoformat(timespec="seconds")
lines = open(sys.argv[1]).read().splitlines(keepends=True)
out, header = [], None
for line in lines:
    if line.startswith("#"):
        out.append(line); continue
    cols = line.rstrip("\n").split("\t")
    if header is None:
        header = cols; out.append(line); continue
    row = dict(zip(header, cols))
    folders = [d for _, d in ow.agents(row["round"], dict(ow.ROUNDS)[row["round"]])]
    launch = min(os.path.getmtime(os.path.join(d, "log", "BRIEF.md")) for d in folders)
    last = max(os.path.getmtime(os.path.join(d, "log", "out.jsonl")) for d in folders)
    cols[header.index("launch")], cols[header.index("last_write")] = fmt(launch), fmt(last)
    out.append("\t".join(cols) + "\n")
open(sys.argv[1], "w").write("".join(out))
