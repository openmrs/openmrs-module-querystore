"""Solve per-token list prices per model from blind-run result events' modelUsage (costUSD is the recorded truth)."""
import glob, json, sys, collections
import numpy as np
K = ["inputTokens", "outputTokens", "cacheReadInputTokens", "cacheCreationEphemeral5mInputTokens", "cacheCreationEphemeral1hInputTokens"]
rows = collections.defaultdict(list)
for f in glob.glob(sys.argv[1]):
    last = None
    for line in open(f, errors="replace"):
        if '"type":"result"' in line:
            try: last = json.loads(line)
            except Exception: pass
    if not last: continue
    for m, u in (last.get("modelUsage") or {}).items():
        rows[m].append(([u.get(k, 0) for k in K], u.get("costUSD", 0)))
for m, rs in rows.items():
    A = np.array([r[0] for r in rs], float); b = np.array([r[1] for r in rs], float)
    x, res, rank, _ = np.linalg.lstsq(A, b, rcond=None)
    pred = A @ x; err = np.max(np.abs(pred - b) / np.maximum(b, 1e-9)) if len(b) else 0
    print(m, len(rs), "rank", rank, "per-M:", [round(v * 1e6, 4) for v in x], "max rel err", round(float(err), 5))
