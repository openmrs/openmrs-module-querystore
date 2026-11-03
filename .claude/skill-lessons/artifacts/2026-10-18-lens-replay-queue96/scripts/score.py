"""Join the adjudication with the private maps and print per-run and per-arm detection of each key."""
import json, sys, collections, os
C = sys.argv[1]
key = json.load(open(f'{C}/KEY-private.json'))['slots']
amap = json.load(open(f'{C}/adjudication-map-private.json'))
adj = json.load(open(f'{C}/adj/adjudication.json'))
inp = {x['key']: x for x in json.load(open(f'{C}/adj/adjudication-input.json'))}
K = ['K1', 'K2', 'K3', 'K4', 'K5', 'K6', 'K7', 'K8', 'K9']
runs = collections.defaultdict(lambda: {'hit': set(), 'blk': set(), 'partial7': False, 'q7': False, 'n': 0, 'cls': collections.Counter()})
seen = set()
for v in adj['verdicts']:
    k = v['key']; seen.add(k)
    wt = amap[k]['wt']; r = runs[wt]; r['n'] += 1
    m = v.get('matches') or []
    for kk in m:
        if kk == 'K7-partial': r['partial7'] = True; continue
        if kk == 'K7-q': r['q7'] = True; continue
        r['hit'].add(kk)
        if (inp[k].get('disposition') or '').lower() == 'blocking': r['blk'].add(kk)
    if not m or set(m) <= {'K7-partial', 'K7-q'}:
        r['cls'][v.get('class') or 'unclassified'] += 1
missing = set(inp) - seen
if missing: print('WARNING: adjudication has no verdict for', sorted(missing))
for wt in sorted(key):
    if wt not in runs: print(f'{wt} ({key[wt]["arm"]}): no findings adjudicated'); continue
    r = runs[wt]
    print(f'{wt} arm={key[wt]["arm"]} wave={key[wt]["wave"]} findings={r["n"]} keys={sorted(r["hit"])} blocking-filed={sorted(r["blk"])} K7-partial={r["partial7"]} K7-q={r["q7"]} other={dict(r["cls"])}')
print()
print('arm  ' + ' '.join(f'{k:>5}' for k in K) + '  K6blk K7blk K7q  K7part NOISE other-REAL')
for arm in 'CTBV':
    wts = [w for w in key if key[w]['arm'] == arm]
    row = [sum(1 for w in wts if k in runs[w]['hit']) for k in K]
    k6b = sum(1 for w in wts if 'K6' in runs[w]['blk']); k7b = sum(1 for w in wts if 'K7' in runs[w]['blk'])
    k7p = sum(1 for w in wts if runs[w]['partial7'] and 'K7' not in runs[w]['hit'])
    k7q = sum(1 for w in wts if runs[w]['q7'] and 'K7' not in runs[w]['hit'])
    if not wts: continue
    noise = sum(runs[w]['cls']['NOISE'] for w in wts)
    real = sum(runs[w]['cls']['REAL'] + runs[w]['cls']['REAL_OVERSTATED'] for w in wts)
    print(f'{arm}    ' + ' '.join(f'{x:>3}/3' for x in row) + f'  {k6b}/3   {k7b}/3   {k7q}/3  {k7p}/3    {noise:>3}  {real:>3}')
