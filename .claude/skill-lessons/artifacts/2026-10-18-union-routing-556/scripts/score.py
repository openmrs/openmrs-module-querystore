"""Score the PR 556 test: C = a run's findings; U = a pair's findings unioned plus both reviewers' risks."""
import json, sys, collections
C = sys.argv[1]
key = json.load(open(f'{C}/KEY-private.json'))['slots']
amap = json.load(open(f'{C}/adjudication-map-private.json'))
adj = json.load(open(f'{C}/adj/adjudication.json'))['verdicts']
inp = {x['key']: x for x in json.load(open(f'{C}/adj/adjudication-input.json'))}
S = ['S1', 'S2', 'S3']
F = collections.defaultdict(set); Rk = collections.defaultdict(set); B = collections.defaultdict(set)
noise = collections.Counter(); real = collections.Counter(); risks = collections.Counter(); risk_useful = collections.Counter()
for v in adj:
    m = amap[v['key']]; wt = m['wt']; kind = m['kind']; ms = [x for x in (v.get('matches') or [])]
    if kind == 'finding':
        for s in ms:
            if s in S:
                F[wt].add(s)
                if (inp[v['key']].get('disposition') or '').lower() == 'blocking': B[wt].add(s)
        if not ms:
            if v.get('class') == 'NOISE': noise[wt] += 1
            if v.get('class') in ('REAL', 'REAL_OVERSTATED'): real[wt] += 1
    else:
        risks[wt] += 1
        hit = {s[:2] for s in ms if s[:2] in S}
        Rk[wt] |= hit
        if hit or v.get('class') in ('REAL', 'REAL_OVERSTATED'): risk_useful[wt] += 1
def unit(arm):
    if arm == 'C': return [[w] for w in sorted(key) if key[w]['arm'] == 'C']
    pairs = collections.defaultdict(list)
    for w in sorted(key):
        if key[w]['arm'] == 'U': pairs[key[w]['pair']].append(w)
    return [pairs[p] for p in sorted(pairs)]
print('per run:')
for w in sorted(key):
    print(f"  {w} arm={key[w]['arm']} pair={key[w]['pair']} findings-keys={sorted(F[w])} blocking={sorted(B[w])} risk-keys={sorted(Rk[w])} risks={risks[w]} useful-risks={risk_useful[w]} other-REAL={real[w]} NOISE={noise[w]}")
print()
print('unit                  S1     S2     S3   | S3 findings-only | S3 blocking')
for arm in ('C', 'U'):
    us = unit(arm); n = len(us)
    def cnt(s, use_risk): return sum(1 for u in us if any(s in F[w] or (use_risk and s in Rk[w]) for w in u))
    row = ' '.join(f'{cnt(s, arm == "U"):>3}/{n}' for s in S)
    s3f = sum(1 for u in us if any('S3' in F[w] for w in u)); s3b = sum(1 for u in us if any('S3' in B[w] for w in u))
    print(f'{arm} ({"findings" if arm=="C" else "union+risks"})   {row}   |      {s3f}/{n}        |   {s3b}/{n}')
cs = [w for w in key if key[w]['arm'] == 'C']
print(f"decomposition: C + its own risks (routing alone): S3 {sum(1 for w in cs if 'S3' in F[w] or 'S3' in Rk[w])}/{len(cs)}")
