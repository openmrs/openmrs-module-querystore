"""Flag forbidden reads in a stream-json transcript's tool calls (only tool_use inputs are inspected).

usage: audit.py <R> <replay> <transcript>...      (transcript names carry wt-NN)
       audit.py --calibrate <R>
"""
import json, os, re, sys

FORBID = [
    (r"openmrs-core/(pulls?|issues|commits?|compare|blob|tree)|repos/openmrs/openmrs-core|\bgh\s+(pr|issue|api|repo|search|run|browse)\b|-R\s+openmrs/openmrs-core|api\.github\.com|raw\.githubusercontent\.com|https?://github\.com", "GitHub / gh"),
    (r"atlassian\.net|issues\.openmrs\.org", "live ticket"),
    (r"/Users/danielkayiwa/Projects(?![\w-])|~/Projects", "user's Projects"),
    (r"replay-6535/(PROTOCOL|KEY-private|briefs|scripts|logs|adj|raw|private|skill-diff)|skill-lessons", "control files / records"),
    (r"/scratchpad/(?!replay-6535(?![A-Za-z0-9_.-]))[A-Za-z0-9_.-]+", "another scratchpad folder"),
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


def audit(path, rep, own, own_skill):
    n, flags = 0, []
    for name, inp in calls(path):
        n += 1
        if name in ("Skill", "Agent", "Task", "WebFetch", "WebSearch"):
            flags.append(("disallowed tool", name, inp[:160]))
        for pat, why in FORBID:
            if re.search(pat, inp):
                flags.append((why, name, inp[:200]))
        for seg in re.findall(r"(?:/Users/danielkayiwa/|~/)\.claude[^\"\s]*", inp):
            if f"replay-6535-{rep}-{own}/" not in seg:
                flags.append(("~/.claude", name, seg[:200]))
        for r2, other in re.findall(r"replay-6535/(p2|p3)/(wt-\d\d)", inp) + re.findall(r"replay-6535/(?:m2|data)/(p2|p3)-(wt-\d\d)", inp):
            if (r2, other) != (rep, own):
                flags.append((f"other run dir {r2}/{other}", name, inp[:200]))
        for r2, other in re.findall(r"replay-6535/out/(p2|p3)/(wt-\d\d)", inp):
            if (r2, other) != (rep, own):
                flags.append((f"other run output {r2}/{other}", name, inp[:200]))
        for r2 in re.findall(r"replay-6535/snapshots/(p2|p3)", inp):
            if r2 != rep:
                flags.append((f"other replay snapshot {r2}", name, inp[:200]))
        for r2 in re.findall(r"replay-6535/frozen/(\w+)\.git", inp):
            if r2 != rep:
                flags.append((f"other frozen repo {r2}", name, inp[:200]))
        for sk in re.findall(r"replay-6535/skills/(s\d)", inp):
            if sk != own_skill:
                flags.append(("other arm skill " + sk, name, inp[:200]))
    return n, flags


def calibrate(R):
    S = "/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-openmrs-core/13a04911-9d12-40fc-baa9-b655af5ecba7/scratchpad"
    bad = [("Bash", {"command": "gh api repos/openmrs/openmrs-core/pulls/6535/comments"}),
           ("Bash", {"command": "curl -s https://api.github.com/repos/openmrs/openmrs-core/pulls/6535/reviews"}),
           ("Read", {"file_path": "/Users/danielkayiwa/.claude/projects/-Users-danielkayiwa-Projects-openmrs-openmrs-core/memory/MEMORY.md"}),
           ("Read", {"file_path": f"{R}/KEY-private.json"}),
           ("Bash", {"command": f"cat {R}/out/p2/wt-04.json"}),
           ("Read", {"file_path": f"{S}/retro/gate2/reply-all.md"}),
           ("Bash", {"command": "curl -s 'https://openmrs.atlassian.net/rest/api/2/issue/TRUNK-6682'"}),
           ("Read", {"file_path": f"{R}/skills/s3/SKILL.md"})]
    good = [("Read", {"file_path": f"{R}/skills/s1/SKILL.md"}),
            ("Read", {"file_path": f"{R}/skills/s1/posting.md"}),
            ("Read", {"file_path": f"{R}/snapshots/p2/pr6535-conversation-at-review.json"}),
            ("Bash", {"command": f"cd {R}/p2/wt-01 && git diff --stat master...HEAD"}),
            ("Bash", {"command": f"cd {R}/p2/wt-01 && mvn -o -B -pl api -am test -Dtest=TextHandlerTest -Dsurefire.failIfNoSpecifiedTests=false"}),
            ("Read", {"file_path": "/Users/danielkayiwa/.claude/projects/-private-tmp-claude-501--Users-danielkayiwa-Projects-openmrs-openmrs-core-13a04911-9d12-40fc-baa9-b655af5ecba7-scratchpad-replay-6535-p2-wt-01/abc/tool-results/x.txt"}),
            ("Bash", {"command": f"python3 -c 'import json; json.dump({{}}, open(\"{R}/out/p2/wt-01.json\", \"w\"))'"})]
    nb = ng = 0
    for i, (name, inp) in enumerate(bad + good):
        p = f"{R}/private/audit-calibration-{i:02d}.jsonl"
        with open(p, "w") as f:
            f.write(json.dumps({"type": "assistant", "message": {"content": [{"type": "tool_use", "name": name, "input": inp}]}}) + "\n")
        n, flags = audit(p, "p2", "wt-01", "s1")
        kind = "forbidden" if i < len(bad) else "allowed"
        print(f"   {kind:9} {'FLAGGED' if flags else 'clean  '} | {json.dumps(inp)[-90:]}" + (f"  <- {flags[0][0]}" if flags else ""))
        if flags and i < len(bad): nb += 1
        if flags and i >= len(bad): ng += 1
    print(f"planted forbidden flagged: {nb}/{len(bad)}; allowed flagged: {ng}/{len(good)}")
    print("CALIBRATION", "PASS" if nb == len(bad) and ng == 0 else "FAIL")


if __name__ == "__main__":
    if sys.argv[1] == "--calibrate":
        calibrate(sys.argv[2])
        sys.exit(0)
    R, rep = sys.argv[1], sys.argv[2]
    key = json.load(open(f"{R}/KEY-private.json"))
    for p in sys.argv[3:]:
        own = re.search(r"(wt-\d\d)", os.path.basename(p)).group(1)
        arm = key[rep]["slots"][own]
        n, flags = audit(p, rep, own, key["skill_dirs"][rep][arm])
        print(f"{rep}/{own}: {n} tool calls, {len(flags)} flagged")
        for f in flags:
            print("   ", f)
