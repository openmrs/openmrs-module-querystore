"""gate-calibrate.py <repo> <base> -- run each gate's gate-test.sh against mutants of that gate (copies
in a temp dir; the repo is only read) and, for every case added since <base> (named on an added
`run_*case` line of the suite's diff), print the mutants it fails against. The control, no mutant,
must fail nothing; a case that no mutant fails is marked NONE."""
import os, re, shutil, subprocess, sys, tempfile
repo, base = sys.argv[1], sys.argv[2]
# (name, anchor, replacement[, the one gate it applies to])
MUTANTS = [
    ("control", None, None),
    ("no-note", 'STALE_NOTE="The newest await', 'STALE_NOTE_OFF="The newest await'),
    ("appended", ".reason = $n + .reason", ".reason = .reason + $n"),
    ("replaces", ".reason = $n + .reason", ".reason = $n"),
    ("no-since-guard", 'if [ "$NEWEST" -gt 0 ]; then', "if true; then"),
    ("no-await-needed", 'STALE_NOTE=""', 'STALE_NOTE="The newest await recorded here is 0 min old, past the one-hour bound (awaiting: ?). Otherwise: "'),
    ("min-not-max", "| (.since // 0)] | max'", "| (.since // 0)] | min'"),
    ("ttl-infinite", "AWAIT_TTL=3600 ", "AWAIT_TTL=999999999 "),
    ("agents-unnamed", "await_agents() { jq", "await_agents() { echo \"?\"; return; jq"),
    ("names-deduped", '(.agent // "?")] | join(", ")',
     '(.agent // "?")] | reduce .[] as $a ([]; if any(.[]; . == $a) then . else . + [$a] end)'
     ' | join(", ")'),
    ("note-makes-block", "  allow\nfi\n\n# Present, fresh, and the last review found blocking findings",
     "  [ -n \"$STALE_NOTE\" ] && { jq -n '{decision:\"block\",reason:\"x\"}' | with_note; exit 0; }\n"
     "  allow\nfi\n\n# Present, fresh, and the last review found blocking findings", "pr-harden-gate.sh"),
]
SUITES = [("pr-harden", "pr-harden-gate.sh"), ("harden", "harden-cycle-gate.sh")]
for suite, gate in SUITES:
    test = os.path.join(repo, ".claude/skills", suite, "gate-test.sh")
    diff = subprocess.run(["git", "-C", repo, "diff", base, "--", test], capture_output=True, text=True, check=True).stdout
    added = [m.group(1) for m in re.finditer(r'^\+\s*run_\w*case "([^"]+)"', diff, re.M)]
    fails = {}
    with tempfile.TemporaryDirectory() as d:
        for name, old, new, *only in MUTANTS:
            if only and only[0] != gate:
                continue
            src = open(os.path.join(repo, ".claude/hooks", gate)).read()
            if old is not None:
                if src.count(old) != 1:
                    sys.exit(f"{gate}/{name}: anchor occurs {src.count(old)} times")
                src = src.replace(old, new)
            hook = os.path.join(d, f"{name}-{gate}")
            open(hook, "w").write(src)
            got = subprocess.run(["bash", test, hook], capture_output=True, text=True)
            failed = re.findall(r"^  FAIL (.+?)(?::| \(| expected|$)", got.stdout, re.M)
            total = re.search(r"passed=(\d+) failed=(\d+)", got.stdout)
            print(f"{suite:9} {name:16} {total.group(0) if total else 'NO TOTAL rc=%d' % got.returncode}")
            fails[name] = got.stdout
    print(f"{suite}: {len(added)} cases added by the slice")
    for case in added:
        hit = [n for n, out in fails.items() if re.search(r"^  FAIL " + re.escape(case) + r"(:| \(| expected|$)", out, re.M)]
        print(f"   {'NONE ' if not hit else '     '}{case}  <-  {', '.join(hit) or '-'}")
