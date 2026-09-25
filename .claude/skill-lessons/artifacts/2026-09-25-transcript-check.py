import os, re, sys, glob
store = os.path.expanduser('~/.claude/skill-lessons')
rows = []
for f in sorted(glob.glob(store + '/*.md')):
    base = os.path.basename(f)
    if base in ('REJECTED.md',): continue
    lines = open(f, encoding='utf-8', errors='replace').read().splitlines()
    for i, l in enumerate(lines):
        if not l.startswith('# '): continue
        # a record header is followed within 2 lines by outcome:
        nxt = lines[i+1:i+3]
        if not any(x.startswith('outcome:') for x in nxt): continue
        tline = None
        for x in lines[i+1:i+8]:
            if x.startswith('transcript:'):
                tline = x; break
        if tline is None:
            rows.append((base, i+1, 'NO-TRANSCRIPT-LINE', '')); continue
        m = re.search(r'(~?/[^\s`]+\.jsonl)', tline)
        if not m:
            rows.append((base, i+1, 'UNPARSEABLE', tline[:100])); continue
        p = os.path.expanduser(m.group(1))
        if not os.path.exists(p):
            rows.append((base, i+1, 'MISSING-FILE', p)); continue
        data = open(p, encoding='utf-8', errors='replace').read()
        hit = ('skill-lessons/' + base) in data
        rows.append((base, i+1, 'WRITE-FOUND' if hit else 'NO-WRITE-OF-THIS-FILE', os.path.basename(p)))
from collections import Counter
print(Counter(r[2] for r in rows))
for r in rows:
    if r[2] != 'WRITE-FOUND':
        print(*r, sep=' | ')
