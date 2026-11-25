"""Score the 2026-11-25 whole-agent time replay from the three judges' reports.

score.py <dir> [<judge-report-s1> <judge-report-s2> <judge-report-s3>]
Reads mapping-<s>.json from <dir>; judge reports default to the dlr2-judge-<s> blind-run folders.
Prints, per subject: parse problems, the calibration check (protocol's void rule), FOUND/NAMED per key and arm,
then the pre-registered decision.
"""
import json, os, re, sys

D = sys.argv[1]
home = os.path.expanduser('~')
reports = sys.argv[2:5] or [f'{home}/.claude/blind-runs/2026-11-25-dlr2-judge-{s}/log/report.md' for s in ('s1', 's2', 's3')]
KEYS = {'s1': ['K86'], 's2': ['K71a', 'K71b', 'K71c'], 's3': ['K1', 'K2', 'K3', 'K4', 'K5']}
CARRIED = {'s1': ['K86'], 's2': ['K71a', 'K71c'], 's3': ['K1', 'K4']}   # the protocol's calibration expectations
MARK = re.compile(r'^\s*[`*]*MARK\s+(R\d+)\s+(K\w+)\s+(FOUND|NAMED|ABSENT)\b')

switch, problems_any, void_any = [], False, False
for s, path in zip(('s1', 's2', 's3'), reports):
    mapping = json.load(open(f'{D}/mapping-{s}.json'))
    marks, problems = {}, []
    for line in open(path, errors='replace').read().splitlines():
        m = MARK.match(line)
        if not m:
            continue
        label, key, verdict = m.groups()
        if label not in mapping:
            problems.append(f'unknown report {label}')
        elif key not in KEYS[s]:
            problems.append(f'unknown key {key}')
        elif (label, key) in marks and marks[(label, key)] != verdict:
            problems.append(f'{label} {key} marked twice, differently')
        else:
            marks[(label, key)] = verdict
    missing = [(l, k) for l in mapping for k in KEYS[s] if (l, k) not in marks]
    if missing:
        problems.append(f'{len(missing)} marks missing: {missing[:6]}')
    src = {l: v['source'] for l, v in mapping.items()}
    by = {v: l for l, v in src.items()}
    void = []
    for k in KEYS[s]:
        g = marks.get((by['known-good'], k))
        b = marks.get((by['known-bad'], k))
        if k in CARRIED[s] and g == 'ABSENT':
            void.append(f'known-good ABSENT on {k}')
        if b not in (None, 'ABSENT'):
            void.append(f'known-bad {b} on {k}')
    print(f'== {s}: {len(marks)} marks; problems: {problems or "none"}; calibration: {"VOID: " + "; ".join(void) if void else "passes"}')
    print(f'   known-good {by["known-good"]}: ' + ' '.join(f'{k}={marks.get((by["known-good"], k))}' for k in KEYS[s]))
    print(f'   known-bad  {by["known-bad"]}: ' + ' '.join(f'{k}={marks.get((by["known-bad"], k))}' for k in KEYS[s]))
    problems_any |= bool(problems)
    void_any |= bool(void)
    for k in KEYS[s]:
        row = {}
        for arm in ('25', '75'):
            vs = [marks.get((by[f'{arm}-{w}'], k)) for w in (1, 2, 3)]
            row[arm] = vs
        f25, f75 = row['25'].count('FOUND'), row['75'].count('FOUND')
        n25, n75 = row['25'].count('NAMED'), row['75'].count('NAMED')
        print(f'   {k:5} A25 FOUND {f25}/3 NAMED {n25}/3 {row["25"]} | A75 FOUND {f75}/3 NAMED {n75}/3 {row["75"]}')
        if f75 >= 2 and f25 == 0:
            switch.append(f'{s} {k}')
verdict = f'switch to 75 ({", ".join(switch)})' if switch else 'stay at 25 (no key FOUND in >=2 of 3 A75 runs and 0 of 3 A25 runs)'
if problems_any or void_any:
    print('DECISION WITHHELD: a parse problem or a void calibration (re-judge that subject per the protocol); provisional:', verdict)
else:
    print('DECISION:', verdict)
