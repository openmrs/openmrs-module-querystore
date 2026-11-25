"""Cost of Claude Code transcripts from per-message usage, deduped by (message.id, requestId).
Prices: claude-opus-5-5 list, solved exactly from 176 blind-run modelUsage records (prices.py): $/M input 4,
output 20, cache read 0.2, 1h write 8; 5m write assumed 5 (1.25x input; never seen in blind runs, counted separately).
Usage: tcost.py [--nodedupe] [--from ISO] [--to ISO] files..."""
import json, sys, collections
P = {"in": 4e-6, "out": 20e-6, "cr": 0.2e-6, "w5": 5e-6, "w1": 8e-6}
args = sys.argv[1:]; dedupe = True; t0 = ""; t1 = "9999"
while args and args[0].startswith("--"):
    a = args.pop(0)
    if a == "--nodedupe": dedupe = False
    elif a == "--from": t0 = args.pop(0)
    elif a == "--to": t1 = args.pop(0)
def cost_of(files):
    seen = {}; other = collections.Counter(); n = 0
    for f in files:
        for line in open(f, errors="replace"):
            if '"assistant"' not in line or '"usage"' not in line: continue
            try: e = json.loads(line)
            except Exception: continue
            if e.get("type") != "assistant": continue
            ts = e.get("timestamp", "")
            if not (t0 <= ts < t1): continue
            m = e.get("message") or {}; u = m.get("usage") or {}
            key = (m.get("id"), e.get("requestId")) if dedupe else n
            n += 1
            cc = u.get("cache_creation") or {}
            w5 = cc.get("ephemeral_5m_input_tokens"); w1 = cc.get("ephemeral_1h_input_tokens")
            if w5 is None and w1 is None: w5, w1 = 0, u.get("cache_creation_input_tokens", 0)
            rec = (m.get("model"), u.get("input_tokens", 0), u.get("output_tokens", 0), u.get("cache_read_input_tokens", 0), w5 or 0, w1 or 0)
            if key in seen and seen[key][2] >= rec[2]: continue
            seen[key] = rec
    usd = 0.0; tok = collections.Counter()
    for model, i, o, cr, w5, w1 in seen.values():
        if model and "opus-5-5" not in model:
            other[model] += 1; continue
        usd += i * P["in"] + o * P["out"] + cr * P["cr"] + w5 * P["w5"] + w1 * P["w1"]; tok["w5"] += w5
    return usd, len(seen), other, tok
if __name__ == "__main__":
    usd, msgs, other, tok = cost_of(args)
    print(f"{usd:.2f} USD over {msgs} messages; 5m-write tokens {tok['w5']}; other models {dict(other)}")
