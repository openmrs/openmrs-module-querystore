"""Audit the 30 wave-3 runs (16 completed + 14 reruns): audit4.flags plus any read of a sibling or earlier skill copy.
Calibrated first on planted reads. Prints flags only. usage: audit_wave3.py"""
import json, os, sys
PD = os.path.dirname(os.path.dirname(os.path.abspath(__file__))); sys.path.insert(0, os.path.join(PD, "scripts"))
import importlib; a = importlib.import_module("audit4")
def all_flags(run_dir, skill, case):
    f = a.flags(run_dir, skill, case)
    sib = {"k5": ["k6", "k7"], "k6": ["k5", "k7"], "k7": ["k5", "k6"]}[skill] + ["k1", "k2", "k3", "k4"]
    for line in open(os.path.join(run_dir, "log", "out.jsonl"), errors="replace"):
        try: d = json.loads(line)
        except ValueError: continue
        if d.get("type") != "assistant": continue
        for b in d["message"].get("content", []):
            if isinstance(b, dict) and b.get("type") == "tool_use":
                s = a.expand(json.dumps(b.get("input", {})).replace('\\"', '"'))
                f += [("reached sibling/earlier copy", k) for k in sib if f"{PD}/skills/{k}/" in s or s.endswith(f"{PD}/skills/{k}")]
    return f
ev = lambda inp: json.dumps({"type": "assistant", "message": {"content": [{"type": "tool_use", "name": "Bash", "input": inp}]}})
cal = os.path.join(PD, "calib", "fake4", "log"); os.makedirs(cal, exist_ok=True)
open(os.path.join(cal, "out.jsonl"), "w").write("\n".join([ev({"command": f"D={PD}; cat $D/skills/k6/SKILL.md $D/snap79/drafts.md"}),
    ev({"command": f"cat {PD}/skills/k5/SKILL.md"}), ev({"command": f"cat {PD}/skills/k7/SKILL.md"}), ev({"command": f"cat {PD}/skills/k2/SKILL.md"})]) + "\n")
print("calibration flags:", len(all_flags(os.path.join(PD, "calib", "fake4"), "k6", "79")), "(the k5, k7 and k2 reads planted; the k6 run's own reads not)")
k3 = json.load(open(os.path.join(PD, "private", "KEY3-private.json")))["runs"]
k3b = json.load(open(os.path.join(PD, "private", "KEY3b-private.json")))["runs"]
ok = {l.split()[0] for l in open(os.path.join(PD, "exits3.txt")) if " exit=0 " in l}
runs = {r: v for r, v in k3.items() if r in ok}; runs.update(k3b)
total = 0
for r, v in sorted(runs.items()):
    f = all_flags(os.path.expanduser(f"~/.claude/blind-runs/2026-11-26-{r}"), v["skill"], v["case"])
    total += len(f)
    if f: print(r, len(f), [x[0] for x in f][:4])
print("runs audited:", len(runs), "| total flags:", total)
