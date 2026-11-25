"""The runs where S (sum of num_turns) != T + K: show S, T, K, each result's num_turns, and the event right after the
last result event, if any."""
import glob, json, os
rows = []
for p in sorted(glob.glob(os.path.expanduser("~/.claude/blind-runs/*/log/out.jsonl"))):
    res, T, after, seg_T, segs = [], 0, [], 0, []
    with open(p, errors="replace") as f:
        for line in f:
            try:
                d = json.loads(line)
            except ValueError:
                continue
            t = d.get("type")
            if t == "result":
                res.append(d.get("num_turns") or 0); segs.append(seg_T); seg_T = 0; after = []
            elif t == "user":
                c = sum(1 for b in (d.get("message") or {}).get("content") or [] if isinstance(b, dict) and b.get("type") == "tool_result")
                T += c; seg_T += c
            if res and t != "result":
                after.append(t + (":" + d.get("subtype") if d.get("subtype") else ""))
    if res and sum(res) != T + len(res):
        rows.append((os.path.basename(os.path.dirname(os.path.dirname(p))), sum(res), T, len(res), list(zip(res, segs))[:6], seg_T, after[:3]))
for r in rows:
    print(r)
print(len(rows))
