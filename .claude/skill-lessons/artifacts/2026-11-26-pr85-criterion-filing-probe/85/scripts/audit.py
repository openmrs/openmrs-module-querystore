"""Flag tool calls that read outside a run's allowed files or reach the network. usage: audit.py <run-dir> <skill-dir-name>"""
import json, re, sys, os
P = "/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/7abec84a-e793-4dfb-aa6b-79a4f7a18e1d/scratchpad/probe85"; SCR = "/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/7abec84a-e793-4dfb-aa6b-79a4f7a18e1d/scratchpad"; H = "/Users/danielkayiwa"
def flags(run_dir, skill):
    own = run_dir.rstrip("/") + "/"
    allowed = [own, P + "/skills/" + skill + "/", P + "/snapshots/"]
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
    f = flags(sys.argv[1], sys.argv[2])
    print(len(f), "flags"); [print("  ", k, v[:160]) for k, v in f]
