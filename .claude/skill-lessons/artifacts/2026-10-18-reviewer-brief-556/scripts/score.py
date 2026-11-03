"""Per run and per arm: which of S1-S3 the run's findings assert, and whether filed blocking."""
import json, sys, collections
C = sys.argv[1]
key = json.load(open(f'{C}/KEY-private.json'))['slots']
amap = json.load(open(f'{C}/adjudication-map-private.json'))
adj = json.load(open(f'{C}/adj/adjudication.json'))['verdicts']
inp = {x['key']: x for x in json.load(open(f'{C}/adj/adjudication-input.json'))}
F = collections.defaultdict(set); B = collections.defaultdict(set); other = collections.Counter(); noise = collections.Counter()
for v in adj:
    wt = amap[v['key']]['wt']; ms = [m for m in (v.get('matches') or []) if m in ('S1', 'S2', 'S3')]
    for s in ms:
        F[wt].add(s)
        if inp[v['key']]['disposition'] == 'blocking': B[wt].add(s)
    if not ms:
        if v.get('class') in ('REAL', 'REAL_OVERSTATED'): other[wt] += 1
        if v.get('class') == 'NOISE': noise[wt] += 1
for w in sorted(key): print(f"{w} arm={key[w]['arm']} keys={sorted(F[w])} blocking={sorted(B[w])} other-REAL={other[w]} NOISE={noise[w]}")
print()
for arm in ('R', 'P'):
    ws = [w for w in key if key[w]['arm'] == arm]
    print(f"{arm}: S1 {sum('S1' in F[w] for w in ws)}/{len(ws)} (blocking {sum('S1' in B[w] for w in ws)})  "
          f"S2 {sum('S2' in F[w] for w in ws)}/{len(ws)}  S3 {sum('S3' in F[w] for w in ws)}/{len(ws)} (blocking {sum('S3' in B[w] for w in ws)})  "
          f"other REAL {sum(other[w] for w in ws)}  NOISE {sum(noise[w] for w in ws)}")
