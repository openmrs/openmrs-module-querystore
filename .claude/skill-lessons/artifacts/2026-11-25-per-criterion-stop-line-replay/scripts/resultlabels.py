"""For each mutation label an agent defined (M<n>_<word>...), the first tool_result that names the label next to an
outcome word. Minutes from the run's first event. Usage: resultlabels.py <run-name> [label-regex]"""
import json, os, re, sys, datetime
def ts(s): return datetime.datetime.fromisoformat(s.replace("Z", "+00:00"))
run = sys.argv[1]; lab = re.compile(sys.argv[2] if len(sys.argv) > 2 else r"\bM\d+[a-z]?_[A-Za-z0-9_]+")
OUT = re.compile(r"(exit [0-9]|SURVIV|KILLED|green|GREEN|red\b|RED\b|FAIL|PASS|BUILD SUCCESS|BUILD FAILURE|Tests run)")
path = os.path.expanduser(f"~/.claude/blind-runs/{run}/log/out.jsonl")
t0, first = None, {}
with open(path, errors="replace") as f:
    for line in f:
        try: d = json.loads(line)
        except ValueError: continue
        t = d.get("timestamp")
        if t: t = ts(t); t0 = t0 or t
        if d.get("type") != "user" or not t: continue
        msg = d.get("message"); content = msg.get("content") if isinstance(msg, dict) else None
        for b in content if isinstance(content, list) else []:
            if not (isinstance(b, dict) and b.get("type") == "tool_result"): continue
            c = b.get("content"); text = c if isinstance(c, str) else " ".join(x.get("text", "") for x in c or [] if isinstance(x, dict))
            for ln in text.splitlines():
                for m in lab.finditer(ln):
                    if m.group(0) not in first and OUT.search(ln):
                        first[m.group(0)] = ((t - t0).total_seconds() / 60, ln.strip()[:140])
for k, (mins, ln) in sorted(first.items(), key=lambda kv: kv[1][0]):
    print(f"  +{mins:5.1f} min  {k:40} {ln}")
print(f"{run}: {len(first)} labels with an outcome line")
