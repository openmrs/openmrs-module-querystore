"""Score the per-criterion stop-line replay from the judges' reports, by PREREGISTERED.md's rules.
zscore.py <dir> [<judge-report-s1> <judge-report-s2>]  (defaults: the zjudge-<s> blind-run folders of <day>)"""
import json, os, re, sys
D = sys.argv[1]; B = os.path.expanduser('~/.claude/blind-runs')
reports = sys.argv[2:4] or [f'{B}/2026-11-25-zjudge-{s}/log/report.md' for s in ('s1', 's2')]
KEYS = {'s1': ['G', 'I', 'H', 'J'], 's2': ['T', 'D2']}
MARK = re.compile(r'^\s*[`*]*MARK\s+(R\d+)\s+(\w+)\s+(FOUND|NAMED|ABSENT)\b')
switch, bad = [], False
tot = {'25': [0, 0], '75': [0, 0]}
for s, path in zip(('s1', 's2'), reports):
    mapping = json.load(open(f'{D}/mapping-{s}.json')); marks, problems = {}, []
    for line in open(path, errors='replace').read().splitlines():
        m = MARK.match(line)
        if not m: continue
        lab, key, v = m.groups()
        if lab not in mapping: problems.append(f'unknown report {lab}')
        elif key not in KEYS[s]: problems.append(f'unknown key {key}')
        elif (lab, key) in marks and marks[(lab, key)] != v: problems.append(f'{lab} {key} marked twice, differently')
        else: marks[(lab, key)] = v
    missing = [(l, k) for l in mapping for k in KEYS[s] if (l, k) not in marks]
    if missing: problems.append(f'{len(missing)} marks missing: {missing[:6]}')
    by = {v['source']: l for l, v in mapping.items()}
    void = [f'known-good ABSENT on {k}' for k in KEYS[s] if marks.get((by['known-good'], k)) == 'ABSENT']
    void += [f'known-bad {marks.get((by["known-bad"], k))} on {k}' for k in KEYS[s] if marks.get((by['known-bad'], k)) not in (None, 'ABSENT')]
    print(f'== {s}: {len(marks)} marks; problems: {problems or "none"}; calibration: {"VOID: " + "; ".join(void) if void else "passes"}')
    print('   known-good: ' + ' '.join(f'{k}={marks.get((by["known-good"], k))}' for k in KEYS[s]) + ' | known-bad: ' + ' '.join(f'{k}={marks.get((by["known-bad"], k))}' for k in KEYS[s]))
    bad |= bool(problems) or bool(void)
    for k in KEYS[s]:
        row = {a: [marks.get((by[f'{a}-{w}'], k)) for w in (1, 2, 3)] for a in ('25', '75')}
        f25, f75 = row['25'].count('FOUND'), row['75'].count('FOUND')
        n25, n75 = row['25'].count('NAMED'), row['75'].count('NAMED')
        tot['25'][0] += f25; tot['25'][1] += n25; tot['75'][0] += f75; tot['75'][1] += n75
        print(f'   {k:3} A25 FOUND {f25}/3 NAMED {n25}/3 {row["25"]} | A75 FOUND {f75}/3 NAMED {n75}/3 {row["75"]}')
        if f75 >= 2 and f25 == 0: switch.append(f'{s} {k}')
print(f'TOTAL key-runs (of 18 per arm): A25 FOUND {tot["25"][0]} NAMED {tot["25"][1]} | A75 FOUND {tot["75"][0]} NAMED {tot["75"][1]}')
verdict = f'keep 75 ({", ".join(switch)})' if switch else 'move per-criterion agents to 25 (no key FOUND in >=2 of 3 A75 runs and 0 of 3 A25 runs)'
print(('DECISION WITHHELD (parse problem or void calibration); provisional: ' if bad else 'DECISION: ') + verdict)
