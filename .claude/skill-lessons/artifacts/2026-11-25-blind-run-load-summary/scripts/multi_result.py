"""Runs whose stream holds several result events: print each result's num_turns, cost and duration, the stream's
distinct assistant message ids and tool_result count, and the run's wall time from file times."""
import glob, json, os
n_multi = 0
for p in sorted(glob.glob(os.path.expanduser("~/.claude/blind-runs/*/log/out.jsonl"))):
    res, ids, trs = [], set(), 0
    with open(p, errors="replace") as f:
        for line in f:
            try:
                d = json.loads(line)
            except ValueError:
                continue
            if d.get("type") == "result":
                res.append((d.get("num_turns"), round(d.get("total_cost_usd") or 0, 2), round((d.get("duration_ms") or 0) / 60000, 1)))
            elif d.get("type") == "assistant":
                ids.add((d.get("message") or {}).get("id"))
            elif d.get("type") == "user":
                trs += sum(1 for b in (d.get("message") or {}).get("content") or [] if isinstance(b, dict) and b.get("type") == "tool_result")
    if len(res) > 1:
        n_multi += 1
        log = os.path.dirname(p)
        wall = (os.path.getmtime(p) - os.path.getmtime(os.path.join(log, "BRIEF.md"))) / 60
        print(os.path.basename(os.path.dirname(log)), "results(turns,$,min):", res, "| ids", len(ids - {None}), "tool_results", trs, f"| file wall {wall:.1f} min")
print("runs with several results:", n_multi)
