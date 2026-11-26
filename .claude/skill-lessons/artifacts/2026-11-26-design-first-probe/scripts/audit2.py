"""Audit with shell variables assigned in the same command expanded; also flag any reach into the other arm's skill
copy, the key, the candidates or the other case's snapshots. Prints flags only, never which copy a run used."""
import json, re, sys, os
PD = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SCR = os.path.dirname(PD); H = os.path.expanduser("~")
def expand(s):
    for name, val in re.findall(r'\b([A-Za-z_]\w*)=([^\s;&|]+)', s):
        s = re.sub(r'\$\{?' + name + r'\}?(?![\w])', val, s)
    return s
def flags(run_dir, skill, case):
    other_k = "k2" if skill == "k1" else "k1"; other_case = "79" if case == "85" else "85"
    own = run_dir.rstrip("/") + "/"
    allowed = [own, f"{PD}/skills/{skill}/", f"{PD}/snap{case}/"]
    out = []
    for line in open(os.path.join(run_dir, "log", "out.jsonl"), errors="replace"):
        try: d = json.loads(line)
        except ValueError: continue
        if d.get("type") != "assistant": continue
        for b in d["message"].get("content", []):
            if not (isinstance(b, dict) and b.get("type") == "tool_use"): continue
            raw = json.dumps(b.get("input", {}))
            s = expand(raw.replace('\\"', '"'))
            for marker in (f"{PD}/skills/{other_k}", f"{PD}/private", f"{PD}/cand", f"{PD}/snap{other_case}", f"{PD}/out", f"{PD}/briefs"):
                if marker in s: out.append(("reached " + marker.replace(PD, "<PD>").replace(other_k, "<other>"), ""))
            for path in re.findall(r'(?:/Users/danielkayiwa|/private/tmp/claude-501|~)[^\s"\'\\`;|)]*', s):
                p = path.replace("~", H, 1) if path.startswith("~") else path
                if "/tool-results/" in p: continue
                if any(p.startswith(a) or p + "/" == a for a in allowed): continue
                if p == PD:  # a bare assignment's value; what it reached is checked by the markers above
                    continue
                if p.startswith(SCR) or p.startswith(H + "/.claude") or p.startswith(H + "/Projects"):
                    out.append(("outside allowed", p.replace(PD, "<PD>")))
            if re.search(r'\b(curl|wget)\b|\bgh\s+(api|pr|issue|search|repo)\b|\bgit\s+(fetch|ls-remote|pull|clone)\b', s):
                out.append(("network", s[:140]))
            if b.get("name") in ("Grep", "Glob") and str(b.get("input", {}).get("path", "")).rstrip("/") in (PD, SCR, H, H + "/.claude"):
                out.append(("broad search", str(b.get("input"))[:140]))
    return out
if __name__ == "__main__":
    date = sys.argv[1]
    key = json.load(open(os.path.join(PD, "private", "KEY-private.json")))["runs"]
    total = 0
    for run, v in sorted(key.items()):
        f = flags(os.path.expanduser(f"~/.claude/blind-runs/{date}-{run}"), v["skill"], v["case"])
        total += len(f)
        if f: print(run, len(f), "flags"); [print("   ", k, x[:170]) for k, x in f]
    print("total flags:", total)
