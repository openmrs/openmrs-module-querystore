"""When did a blind agent first STATE a finding? Scans only the agent's own assistant text blocks (never the brief, tool
inputs or tool output), and prints minutes from the run's first event to the first text block matching every regex
of a finding, with a snippet. Usage: firstmention.py <run-name> <label> <regex>[&&<regex>...] ..."""
import json, os, re, sys, datetime
def ts(s): return datetime.datetime.fromisoformat(s.replace("Z", "+00:00"))
run = sys.argv[1]; specs = sys.argv[2:]
path = os.path.expanduser(f"~/.claude/blind-runs/{run}/log/out.jsonl")
t0 = None; texts = []; last = None
with open(path, errors="replace") as f:
    for line in f:
        try: d = json.loads(line)
        except ValueError: continue
        t = d.get("timestamp")
        if t:
            t = ts(t); t0 = t0 or t; last = t
        if d.get("type") != "assistant" or not t: continue
        for b in (d.get("message") or {}).get("content") or []:
            if isinstance(b, dict) and b.get("type") == "text" and b.get("text", "").strip():
                texts.append((t, b["text"]))
print(f"{run}: first event {t0:%H:%M:%S}, last {last:%H:%M:%S} (+{(last-t0).total_seconds()/60:.1f} min), {len(texts)} text blocks")
for i in range(0, len(specs), 2):
    label, rx = specs[i], [re.compile(r, re.I | re.S) for r in specs[i+1].split("&&")]
    hit = next(((t, x) for t, x in texts if all(r.search(x) for r in rx)), None)
    if not hit:
        print(f"  {label}: NONE"); continue
    t, x = hit; m = rx[0].search(x); s = max(0, m.start() - 80)
    print(f"  {label}: +{(t-t0).total_seconds()/60:.1f} min  ...{' '.join(x[s:s+220].split())}...")
