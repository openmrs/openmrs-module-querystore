"""Five mutations of streaks.py; each must turn its --self-test red. Usage: streaks_mutate.py <streaks.py>"""
import os, subprocess, sys, tempfile
src = open(sys.argv[1]).read()
MUTATIONS = [
    ("M1 owed rounds count under the amendment", 'if amended and row["owed"] != "-":', 'if False:'),
    ("M2 a round that neither counts nor breaks breaks the run",
     '        elif c == "breaks":\n            if run:', '        else:\n            if run:'),
    ("M3 an owed round's defect does not break the run",
     '    if row["defect_by"] != "-":\n        return "breaks"',
     '    if row["defect_by"] != "-" and row["owed"] == "-":\n        return "breaks"'),
    ("M4 one PR suffices", 'len(run) >= 3 and len({r["pr"] for r in run}) >= 2', 'len(run) >= 3'),
    ("M5 only the longest run is tested", '    for run in rs:\n        if met(run):\n            return run\n', ''),
]
red = 0
for name, old, new in MUTATIONS:
    assert src.count(old) == 1, (name, src.count(old))
    with tempfile.NamedTemporaryFile("w", suffix=".py", delete=False) as f:
        f.write(src.replace(old, new))
    p = subprocess.run([sys.executable, f.name, "--self-test"], capture_output=True, text=True)
    os.unlink(f.name)
    red += p.returncode != 0
    print(f"{'RED  ' if p.returncode else 'GREEN'} {name}: {p.stdout.strip().splitlines()[-1]}")
print(f"{red}/{len(MUTATIONS)} mutations turned the self-test red")
sys.exit(0 if red == len(MUTATIONS) else 1)
