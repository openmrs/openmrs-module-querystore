"""One row per blind run. A run's out.jsonl can hold several result events (a Stop hook continues the session):
total_cost_usd is cumulative across them, duration_ms and num_turns are per segment. So: cost = max total_cost_usd,
turns = sum num_turns, model+tool time = sum duration_ms, wall = out.jsonl mtime - BRIEF.md mtime (finished runs)."""
import json, os, sys, datetime
root = os.path.expanduser("~/.claude/blind-runs")
print("name\tstart\tcost\twall_min\tsum_dur_min\tapi_min\tturns\tsegments\tlast_seg_min\tlast_seg_turns\treport\tbash_calls")
for name in sorted(os.listdir(root)):
    log = os.path.join(root, name, "log"); out = os.path.join(log, "out.jsonl"); brief = os.path.join(log, "BRIEF.md")
    if not (os.path.exists(out) and os.path.exists(brief)):
        print(f"{name}\t\t\t\t\t\t\t0\t\t\tmissing"); continue
    t0, t1 = os.path.getmtime(brief), os.path.getmtime(out)
    res, bash = [], 0
    with open(out, errors="replace") as f:
        for line in f:
            try: d = json.loads(line)
            except ValueError: continue
            if d.get("type") == "result": res.append(d)
            elif d.get("type") == "assistant":
                for b in (d.get("message") or {}).get("content") or []:
                    if isinstance(b, dict) and b.get("type") == "tool_use" and b.get("name") == "Bash": bash += 1
    rep = os.path.exists(os.path.join(log, "report.md"))
    if not res:
        print(f"{name}\t{datetime.datetime.fromtimestamp(t0):%m-%d %H:%M}\t\t{(t1-t0)/60:.1f}\t\t\t\t0\t\t\t{rep}\t{bash}"); continue
    cost = max(r.get("total_cost_usd") or 0 for r in res)
    turns = sum(r.get("num_turns") or 0 for r in res)
    dur = sum(r.get("duration_ms") or 0 for r in res) / 60000
    api = max(r.get("duration_api_ms") or 0 for r in res) / 60000
    print(f"{name}\t{datetime.datetime.fromtimestamp(t0):%m-%d %H:%M}\t{cost:.2f}\t{(t1-t0)/60:.1f}\t{dur:.1f}\t{api:.1f}\t{turns}\t{len(res)}\t{(res[-1].get('duration_ms') or 0)/60000:.1f}\t{res[-1].get('num_turns')}\t{rep}\t{bash}")
