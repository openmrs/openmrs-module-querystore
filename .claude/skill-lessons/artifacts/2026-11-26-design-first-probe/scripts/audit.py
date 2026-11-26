"""Flag tool calls that read outside a run's allowed paths or reach the network. Reads the key itself and prints only flags.
usage: audit.py <date>   (audits every run in runs-case.json)"""
import json, re, sys, os
PD = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SCR = os.path.dirname(PD); H = os.path.expanduser("~")
def flags(run_dir, skill, case):
    own = run_dir.rstrip("/") + "/"
    allowed = [own, f"{PD}/skills/{skill}/", f"{PD}/snap{case}/"]
    out = []
    for line in open(os.path.join(run_dir, "log", "out.jsonl"), errors="replace"):
        try: d = json.loads(line)
        except ValueError: continue
        if d.get("type") != "assistant": continue
        for b in d["message"].get("content", []):
            if not (isinstance(b, dict) and b.get("type") == "tool_use"): continue
            s = json.dumps(b.get("input", {}))
            for path in re.findall(r'(?:/Users/danielkayiwa|/private/tmp/claude-501|~)[^\s"\'\\`;|)]*', s):
                p = path.replace("~", H, 1) if path.startswith("~") else path
                if "/tool-results/" in p: continue
                if any(p.startswith(a) or p + "/" == a for a in allowed): continue
                if p.startswith(SCR) or p.startswith(H + "/.claude") or p.startswith(H + "/Projects"):
                    out.append(("outside allowed", p))
            if re.search(r'\b(curl|wget)\b|\bgh\s+(api|pr|issue|search|repo)\b|\bgit\s+(fetch|ls-remote|pull|clone)\b', s):
                out.append(("network", s[:140]))
    return out
if __name__ == "__main__":
    date = sys.argv[1]
    key = json.load(open(os.path.join(PD, "private", "KEY-private.json")))["runs"]
    total = 0
    for run, v in sorted(key.items()):
        f = flags(os.path.expanduser(f"~/.claude/blind-runs/{date}-{run}"), v["skill"], v["case"])
        total += len(f)
        print(run, len(f), "flags"); [print("   ", k, x[:170]) for k, x in f]
    print("total flags:", total)
