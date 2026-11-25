"""For every finished blind run: what the old summary line printed (the LAST result's num_turns and duration_ms) against
the summed num_turns and the wall time from file times (BRIEF.md written just before launch, out.jsonl last written at
exit). Also: is total_cost_usd non-decreasing across a run's result events (i.e. cumulative)?"""
import glob, json, os
n = multi = turns_off = min_off = cost_dec = 0
single_gap = []
for p in sorted(glob.glob(os.path.expanduser("~/.claude/blind-runs/*/log/out.jsonl"))):
    res = []
    with open(p, errors="replace") as f:
        for line in f:
            try:
                d = json.loads(line)
            except ValueError:
                continue
            if d.get("type") == "result":
                res.append(d)
    if not res:
        continue
    n += 1
    log = os.path.dirname(p)
    wall = (os.path.getmtime(p) - os.path.getmtime(os.path.join(log, "BRIEF.md"))) / 60
    old_min = (res[-1].get("duration_ms") or 0) / 60000
    if len(res) > 1:
        multi += 1
    if (res[-1].get("num_turns") or 0) != sum(r.get("num_turns") or 0 for r in res):
        turns_off += 1
    if abs(old_min - wall) > max(1, 0.1 * wall):
        min_off += 1
    costs = [r.get("total_cost_usd") or 0 for r in res]
    if any(b < a for a, b in zip(costs, costs[1:])):
        cost_dec += 1
    if len(res) == 1:
        single_gap.append(wall - old_min)
single_gap.sort()
print(f"finished runs {n}; several result events {multi}; old turns != summed turns {turns_off}; "
      f"old minutes off the file wall by >1 min and >10%: {min_off}; runs whose cost ever decreased: {cost_dec}")
print("single-result runs, file wall minus duration_ms (min): min %.2f median %.2f max %.2f" % (single_gap[0], single_gap[len(single_gap)//2], single_gap[-1]))
