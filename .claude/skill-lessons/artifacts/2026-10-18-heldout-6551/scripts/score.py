"""Per-run and per-arm detection of L01-L13 from the blind adjudication of the 6551 test."""
import json, sys, collections
C = sys.argv[1]
key = json.load(open(f'{C}/KEY-private.json'))['slots']
amap = json.load(open(f'{C}/adjudication-map-private.json'))
adj = json.load(open(f'{C}/adj/adjudication.json'))
inp = {x['key']: x for x in json.load(open(f'{C}/adj/adjudication-input.json'))}
L = [f'L{i:02d}' for i in range(1, 14)]
runs = collections.defaultdict(lambda: {'hit': set(), 'blk': set(), 'asserted': set(), 'n': 0, 'cls': collections.Counter()})
seen = set()
for v in adj['verdicts']:
    k = v['key']; seen.add(k); wt = amap[k]['wt']; r = runs[wt]; r['n'] += 1
    m = v.get('matches') or []
    d = (inp[k].get('disposition') or '').lower()
    for kk in m:
        r['hit'].add(kk)
        if d == 'blocking': r['blk'].add(kk)
        if d in ('blocking', 'suggestion', 'nit'): r['asserted'].add(kk)
    if not m: r['cls'][v.get('class') or 'unclassified'] += 1
missing = set(inp) - seen
if missing: print('WARNING: no verdict for', sorted(missing))
for wt in sorted(key):
    r = runs[wt]
    print(f'{wt} arm={key[wt]["arm"]} wave={key[wt]["wave"]} findings={r["n"]} keys={sorted(r["hit"])} blocking={sorted(r["blk"])} other={dict(r["cls"])}')
print()
print('arm ' + ' '.join(f'{k:>5}' for k in L) + '  L04blk L03asrt L06|L08 NOISE otherREAL')
for arm in 'CMK':
    w = [x for x in key if key[x]['arm'] == arm]
    if not w: continue
    row = [sum(1 for x in w if k in runs[x]['hit']) for k in L]
    l04b = sum(1 for x in w if 'L04' in runs[x]['blk'])
    l03a = sum(1 for x in w if 'L03' in runs[x]['asserted'])
    l68 = sum(1 for x in w if runs[x]['hit'] & {'L06', 'L08'})
    noise = sum(runs[x]['cls']['NOISE'] for x in w)
    real = sum(runs[x]['cls']['REAL'] + runs[x]['cls']['REAL_OVERSTATED'] for x in w)
    print(f'{arm}   ' + ' '.join(f'{c:>3}/3' for c in row) + f'   {l04b}/3    {l03a}/3     {l68}/3   {noise:>3}  {real:>3}')
