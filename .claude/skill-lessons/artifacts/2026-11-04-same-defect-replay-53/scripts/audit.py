"""Flag forbidden reads in a stream-json transcript's tool calls (only tool_use inputs are inspected)."""
import json, sys, re, os
R = sys.argv[1]
key = json.load(open(f"{R}/KEY-private.json"))
FORBID = [
    (r"openmrs-module-stockmanagement/(pulls?|issues|commits?|compare|blob|tree)|repos/openmrs/openmrs-module-stockmanagement|\bgh (pr|issue|api|repo|search)\b|-R openmrs/openmrs-module-stockmanagement", "this repo on GitHub / gh"),
    (r"atlassian\.net|issues\.openmrs\.org", "live ticket"),
    (r"/Users/danielkayiwa/Projects(?![\w-])|~/Projects", "user's Projects"),
    (r"replay-53/(PROTOCOL|KEY-private|briefs|scripts|logs|adj)|skill-lessons", "control files / records"),
    (r"/scratchpad/(?!replay-53(?![A-Za-z0-9_.-]))[A-Za-z0-9_.-]+", "another scratchpad folder"),
    (r"(curl|wget|http)[^\"\n]{0,40}(localhost|127\.0\.0\.1)|openmrs-standalone|\b(pkill|killall|kill -9|lsof -i)\b", "server / standalone"),
]
def calls(path):
    for line in open(path, encoding="utf-8", errors="replace"):
        try: m = json.loads(line)
        except Exception: continue
        if m.get("type") != "assistant": continue
        for c in (m.get("message", {}).get("content") or []):
            if c.get("type") == "tool_use":
                yield c.get("name"), json.dumps(c.get("input"), ensure_ascii=False)
def audit(path, own):
    own_skill = key["skill_dirs"][key["slots"][own]["arm"]]
    n = 0; flags = []
    for name, inp in calls(path):
        n += 1
        if name in ("Skill", "Agent", "Task", "WebFetch", "WebSearch"): flags.append(("disallowed tool", name, inp[:160]))
        for pat, why in FORBID:
            if re.search(pat, inp): flags.append((why, name, inp[:200]))
        for seg in re.findall(r"(?:/Users/danielkayiwa/|~/)\.claude[^\"\s]*", inp):
            if f"scratchpad-replay-53-{own}/" not in seg: flags.append(("~/.claude", name, seg[:200]))
        for other in re.findall(r"replay-53/(?:out/|m2/)?(wt-\d\d)", inp):
            if other != own: flags.append(("other run dir " + other, name, inp[:200]))
        for sk in re.findall(r"replay-53/skills/(s\d)", inp):
            if sk != own_skill: flags.append(("other arm skill " + sk, name, inp[:200]))
    return n, flags
if __name__ == "__main__":
    for p in sys.argv[2:]:
        own = re.search(r"(wt-\d\d)", os.path.basename(p)).group(1)
        n, flags = audit(p, own)
        print(f"{own}: {n} tool calls, {len(flags)} flagged")
        for f in flags: print("   ", f)
