"""Pool every run's findings AND could_not_verify items, strip run and arm, shuffle; write the input and a private map."""
import json, random, sys, os
C, outdir, adj = sys.argv[1], sys.argv[2], sys.argv[3]
rows = []
for f in sorted(os.listdir(outdir)):
    if not f.endswith('.json'): continue
    wt = f[:-5]; d = json.load(open(os.path.join(outdir, f)))
    for x in d.get('findings', []):
        rows.append((wt, 'finding', x.get('id'), {k: x.get(k) for k in ('file', 'line', 'where', 'disposition', 'claim', 'failure_mode', 'evidence')}))
    for i, item in enumerate(d.get('could_not_verify', []) or []):
        rows.append((wt, 'unverified-risk', f'cnv{i}', {'file': None, 'line': None, 'where': 'unverified risk',
                     'disposition': 'unverified', 'claim': item, 'failure_mode': '', 'evidence': ''}))
random.SystemRandom().shuffle(rows)
inp, priv = [], {}
for i, (wt, kind, rid, body) in enumerate(rows, 1):
    k = f'E{i:03d}'
    inp.append({'key': k, 'kind': kind, **body})
    priv[k] = {'wt': wt, 'kind': kind, 'id': rid}
os.makedirs(adj, exist_ok=True)
json.dump(inp, open(os.path.join(adj, 'adjudication-input.json'), 'w'), indent=1)
json.dump(priv, open(os.path.join(C, 'adjudication-map-private.json'), 'w'), indent=1)
kinds = {}
for v in priv.values(): kinds[v['kind']] = kinds.get(v['kind'], 0) + 1
print(f'{len(inp)} entries pooled from {len(set(v["wt"] for v in priv.values()))} runs: {kinds}')
