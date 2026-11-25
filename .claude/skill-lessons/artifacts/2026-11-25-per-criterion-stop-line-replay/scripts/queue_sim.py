"""Model, not measurement: the 2026-11-25 burst's per-criterion agents under a gate that starts at most K at once,
FIFO by their real start time, never skipping. Durations: (a) the recorded wall time (inflated by that night's load,
an upper bound), (b) recorded wall / 3 (the ratio of the burst's median 75-minute whole-PR wall, 64 min, to the quiet
replay's, 21 min). Prints, per round, how much later its last agent would have finished than it really did."""
import csv, sys, heapq, datetime, collections
rows = {r["name"][11:]: r for r in csv.DictReader(open(sys.argv[1]), delimiter="\t") if r["name"].startswith("2026-11-25")}
crit = {"sm#57": ["pr57-r1-crit"], "sm#69": ["pr69-ac1-fresh","pr69-ac2-existing","pr69-ac3-restart"],
        "sm#77": ["sm77-r1-ac1","sm77-r1-ac2","sm77-r1-ac3"], "sm#80": ["sm80-r1-behaviour","sm80-r1-privileges","sm80-r1-structure"],
        "sm#79": ["sm79-r1-ac1","sm79-r1-ac2","sm79-r1-ac3"], "sm#78": ["sm78-r1-criteria"], "sm#83": ["sm83-r1-behaviour","sm83-r1-priv"],
        "sm#85": ["pr85-r1-c14","pr85-r1-c2","pr85-r1-c3"], "sm#68": ["sm68-crit"]}
def t(s): return datetime.datetime.strptime("2026-" + s, "%Y-%m-%d %H:%M")
K = int(sys.argv[2]) if len(sys.argv) > 2 else 3
for label, div in (("(a) recorded walls", 1.0), ("(b) walls / 3", 3.0)):
    jobs = sorted((t(rows[n]["start"]), n, r, float(rows[n]["wall_min"]) / div) for r, ns in crit.items() for n in ns)
    slots = [datetime.datetime.min] * K; heapq.heapify(slots); done = {}
    for start, n, r, dur in jobs:
        free = heapq.heappop(slots); begin = max(start, free); end = begin + datetime.timedelta(minutes=dur)
        heapq.heappush(slots, end); done[n] = (begin, end)
    actual_end = {r: max(t(rows[n]["start"]) + datetime.timedelta(minutes=float(rows[n]["wall_min"])) for n in ns) for r, ns in crit.items()}
    model_end = {r: max(done[n][1] for n in ns) for r, ns in crit.items()}
    print(f"K={K} {label}: {len(jobs)} agents; last finishes {max(model_end.values()):%H:%M} (really {max(actual_end.values()):%H:%M})")
    print("   " + ", ".join(f"{r} {((model_end[r]-actual_end[r]).total_seconds()/60):+.0f}m" for r in crit))
