"""S = sum of num_turns over every result event. Which stream count equals it?
I = distinct assistant message ids; T = tool_result blocks; K = result events; U = user events carrying a tool_result."""
import glob, json, os, collections
tally = collections.Counter(); n = 0; worst = []
for p in sorted(glob.glob(os.path.expanduser("~/.claude/blind-runs/*/log/out.jsonl"))):
    res, ids, T, U = [], [], 0, 0
    with open(p, errors="replace") as f:
        for line in f:
            try:
                d = json.loads(line)
            except ValueError:
                continue
            t = d.get("type")
            if t == "result":
                res.append(d.get("num_turns") or 0)
            elif t == "assistant":
                mid = (d.get("message") or {}).get("id")
                if mid and mid not in ids:
                    ids.append(mid)
            elif t == "user":
                c = [b for b in (d.get("message") or {}).get("content") or [] if isinstance(b, dict) and b.get("type") == "tool_result"]
                T += len(c); U += 1 if c else 0
    if not res:
        continue
    n += 1; S = sum(res); I = len(ids); K = len(res)
    for name, v in (("I", I), ("T+K", T + K), ("U+K", U + K), ("T+1", T + 1)):
        if v == S:
            tally[name] += 1
    worst.append((abs(I - S), S, I, T, K, os.path.basename(os.path.dirname(os.path.dirname(p)))))
print("runs:", n, dict(tally))
worst.sort(reverse=True)
print("largest |I - S|:", worst[:6])
print("share of runs within 10% of S, by I:", sum(1 for w in worst if w[0] <= 0.1 * max(w[1], 1)), "of", n)
