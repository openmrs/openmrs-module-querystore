"""Flag forbidden reads in the tool calls of a replay-2r run, its subagents included.

usage: audit2.py <R> <transcript.jsonl>...   (a run's stream-json log, or a subagent transcript; names carry wt-NN)
       audit2.py --calibrate <R>
Inspects tool_use inputs only. Agent/Task are allowed in this replay; Skill, WebFetch, WebSearch and Workflow are not.
"""
import json, os, re, sys

FORBID = [
    (r"openmrs-core/(pulls?|issues|commits?|compare|blob|tree)|repos/openmrs/openmrs-core|\bgh\s+(pr|issue|api|repo|search|run|browse)\b|-R\s+openmrs/openmrs-core|api\.github\.com|raw\.githubusercontent\.com|https?://github\.com", "GitHub / gh"),
    (r"atlassian\.net|issues\.openmrs\.org", "live ticket"),
    (r"/Users/danielkayiwa/Projects(?![\w-])|~/Projects", "user's Projects"),
    (r"replay-2r/(PROTOCOL|KEY-private|briefs|scripts|logs|adj|private|skill-diff|lock-list|transcripts)|skill-lessons|replay-6535", "control files / records / the 2026-11-18 replay"),
    (r"/scratchpad/(?!replay-2r(?![A-Za-z0-9_.-]))[A-Za-z0-9_.-]+", "another scratchpad folder"),
    (r"(curl|wget|http)[^\"\n]{0,40}(localhost|127\.0\.0\.1)|openmrs-standalone|jetty:run|\b(pkill|killall|kill -9|lsof -i)\b", "server / standalone"),
    (r"\.OpenMRS(?![\w-])", "~/.OpenMRS"),
]


def calls(path):
    for line in open(path, encoding="utf-8", errors="replace"):
        try:
            m = json.loads(line)
        except Exception:
            continue
        if m.get("type") != "assistant":
            continue
        for c in (m.get("message", {}).get("content") or []):
            if c.get("type") == "tool_use":
                yield c.get("name"), json.dumps(c.get("input"), ensure_ascii=False)


def audit(path, own, own_skill):
    n, flags = 0, []
    for name, inp in calls(path):
        n += 1
        if name in ("Skill", "WebFetch", "WebSearch", "Workflow"):
            flags.append(("disallowed tool", name, inp[:160]))
        for pat, why in FORBID:
            if re.search(pat, inp):
                flags.append((why, name, inp[:200]))
        for seg in re.findall(r"(?:/Users/danielkayiwa/|~/)\.claude[^\"\s]*", inp):
            if f"replay-2r-p2-{own}/" not in seg and f"replay-2r-p2-{own}-sub/" not in seg:
                flags.append(("~/.claude", name, seg[:200]))
        for other in re.findall(r"replay-2r/p2/(wt-\d\d)", inp) + re.findall(r"replay-2r/(?:m2|data)/p2-(wt-\d\d)", inp) + re.findall(r"replay-2r/out/p2/(wt-\d\d)", inp):
            if other != own:
                flags.append((f"other run {other}", name, inp[:200]))
        for sk in re.findall(r"replay-2r/skills/([a-z]+)", inp):
            if sk != own_skill:
                flags.append(("other arm skill " + sk, name, inp[:200]))
    return n, flags


def calibrate(R):
    S = os.path.dirname(R)
    bad = [("Bash", {"command": "gh api repos/openmrs/openmrs-core/pulls/6535/comments"}),
           ("Read", {"file_path": "/Users/danielkayiwa/.claude/projects/-Users-danielkayiwa-Projects-openmrs-openmrs-core/memory/MEMORY.md"}),
           ("Read", {"file_path": f"{R}/KEY-private.json"}),
           ("Bash", {"command": f"cat {R}/out/p2/wt-04.json"}),
           ("Read", {"file_path": f"{S}/retro/proposals.md"}),
           ("Read", {"file_path": "/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-openmrs-core/13a04911-9d12-40fc-baa9-b655af5ecba7/scratchpad/replay-6535/PROTOCOL.md"}),
           ("Read", {"file_path": f"{R}/skills/u/SKILL.md"}),
           ("Bash", {"command": f"ls {R}/p2/wt-03-sub"}),
           ("Read", {"file_path": "/Users/danielkayiwa/.claude/skill-lessons/REJECTED.md"})]
    good = [("Read", {"file_path": f"{R}/skills/c/SKILL.md"}),
            ("Read", {"file_path": f"{R}/skills/c/posting.md"}),
            ("Read", {"file_path": f"{R}/snapshots/p2/pr6535-conversation-at-review.json"}),
            ("Bash", {"command": f"cd {R}/p2/wt-01 && git diff --stat master...HEAD"}),
            ("Bash", {"command": f"cd {R}/p2/wt-01-sub && mvn -o -B -pl api -am test -Dtest=TextHandlerTest -Dsurefire.failIfNoSpecifiedTests=false"}),
            ("Agent", {"description": "Independent review", "prompt": f"Review the head in {R}/p2/wt-01-sub"}),
            ("Read", {"file_path": f"/Users/danielkayiwa/.claude/projects/-private-tmp-claude-501--Users-danielkayiwa-Projects-openmrs-chartsearchai-44d9b58a-4830-47af-b032-8ad1f2e620d4-scratchpad-replay-2r-p2-wt-01/abc/tool-results/x.txt"}),
            ("Bash", {"command": f"python3 -c 'import json; json.dump({{}}, open(\"{R}/out/p2/wt-01.json\", \"w\"))'"})]
    nb = ng = 0
    os.makedirs(f"{R}/private", exist_ok=True)
    for i, (name, inp) in enumerate(bad + good):
        p = f"{R}/private/audit-calibration-{i:02d}.jsonl"
        with open(p, "w") as f:
            f.write(json.dumps({"type": "assistant", "message": {"content": [{"type": "tool_use", "name": name, "input": inp}]}}) + "\n")
        n, flags = audit(p, "wt-01", "c")
        kind = "forbidden" if i < len(bad) else "allowed"
        print(f"   {kind:9} {'FLAGGED' if flags else 'clean  '} | {json.dumps(inp)[-80:]}" + (f"  <- {flags[0][0]}" if flags else ""))
        nb += bool(flags) and i < len(bad); ng += bool(flags) and i >= len(bad)
    print(f"planted forbidden flagged: {nb}/{len(bad)}; allowed flagged: {ng}/{len(good)}")
    print("CALIBRATION", "PASS" if nb == len(bad) and ng == 0 else "FAIL")


if __name__ == "__main__":
    if sys.argv[1] == "--calibrate":
        calibrate(sys.argv[2]); sys.exit(0)
    R = sys.argv[1]
    key = json.load(open(f"{R}/KEY-private.json"))
    for p in sys.argv[2:]:
        own = re.search(r"(wt-\d\d)", p).group(1)
        arm = key["p2"]["slots"][own]
        n, flags = audit(p, own, key["skill_dirs"][arm])
        print(f"{own} {os.path.basename(p)}: {n} tool calls, {len(flags)} flagged")
        for f in flags:
            print("   ", f)
