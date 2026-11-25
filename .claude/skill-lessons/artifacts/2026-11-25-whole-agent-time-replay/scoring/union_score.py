"""Score the exploratory union judges. union_score.py <dir> [<report-s1> <report-s2> <report-s3>]"""
import json, os, re, sys
D = sys.argv[1]
home = os.path.expanduser('~')
reports = sys.argv[2:5] or [f'{home}/.claude/blind-runs/2026-11-25-dlr2-union-{s}/log/report.md' for s in ('s1', 's2', 's3')]
DEF = re.compile(r'^\s*[`*]*DEFECT\s+(D\d+)\s+(PRE-EXISTING|INTRODUCED|UNCLEAR)\s+(.*)$')
MARK = re.compile(r'^\s*[`*]*MARK\s+(U\d+)\s+(D\d+)\s+(FOUND|NAMED|ABSENT)\b')
tot = {'25': 0, '75': 0}
for s, path in zip(('s1', 's2', 's3'), reports):
    mapping = json.load(open(f'{D}/union-mapping-{s}.json'))
    arm = {l: v['source'].split('-')[0] for l, v in mapping.items()}
    defects, marks, problems = {}, {}, []
    for line in open(path, errors='replace').read().splitlines():
        m = DEF.match(line)
        if m:
            defects[m.group(1)] = (m.group(2), m.group(3).strip())
            continue
        m = MARK.match(line)
        if m:
            l, d, v = m.groups()
            if l not in mapping:
                problems.append(f'unknown report {l}')
            elif (l, d) in marks and marks[(l, d)] != v:
                problems.append(f'{l} {d} marked twice, differently')
            else:
                marks[(l, d)] = v
    for d in defects:
        miss = [l for l in mapping if (l, d) not in marks]
        if miss:
            problems.append(f'{d} lacks marks for {miss}')
    extra = sorted({d for (_, d) in marks} - set(defects))
    if extra:
        problems.append(f'marks for undeclared defects {extra}')
    print(f'== {s}: {len(defects)} defects; problems: {problems or "none"}')
    cnt = {'25': {'found_any': 0, 'claimed_any': 0, 'found_marks': 0}, '75': {'found_any': 0, 'claimed_any': 0, 'found_marks': 0}}
    only, pattern = {'25': [], '75': []}, {'25': [], '75': []}
    for d, (kind, desc) in sorted(defects.items(), key=lambda x: int(x[0][1:])):
        f = {a: sum(1 for l in mapping if arm[l] == a and marks.get((l, d)) == 'FOUND') for a in ('25', '75')}
        c = {a: sum(1 for l in mapping if arm[l] == a and marks.get((l, d)) in ('FOUND', 'NAMED')) for a in ('25', '75')}
        for a in ('25', '75'):
            cnt[a]['found_marks'] += f[a]
            cnt[a]['found_any'] += f[a] > 0
            cnt[a]['claimed_any'] += c[a] > 0
        o = {'25': '75', '75': '25'}
        for a in ('25', '75'):
            if f[a] > 0 and c[o[a]] == 0:
                only[a].append(d)
            if f[a] >= 2 and f[o[a]] == 0:
                pattern[a].append(d)
        print(f'   {d:4} {kind:12} A25 F{f["25"]} C{c["25"]} | A75 F{f["75"]} C{c["75"]} | {desc[:110]}')
    for a in ('25', '75'):
        tot[a] += cnt[a]['found_marks']
        print(f'   A{a}: defects FOUND by >=1 run {cnt[a]["found_any"]}, claimed by >=1 run {cnt[a]["claimed_any"]}, FOUND marks {cnt[a]["found_marks"]}; '
              f'FOUND here and claimed by no run of the other arm: {only[a]}; FOUND in >=2 of 3 here and 0 there: {pattern[a]}')
print('FOUND marks in all:', tot)
