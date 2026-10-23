"""Pool every run's findings, strip run and arm, shuffle, write the adjudicator's input and a private map."""
import json, random, sys, os
C = sys.argv[1]; outdir = sys.argv[2]; adj = sys.argv[3]
rows = []
for f in sorted(os.listdir(outdir)):
    if not f.endswith('.json'): continue
    d = json.load(open(os.path.join(outdir, f)))
    for x in d.get('findings', []):
        rows.append((f[:-5], x))
random.SystemRandom().shuffle(rows)
inp, priv = [], {}
for i, (wt, x) in enumerate(rows, 1):
    k = f'F{i:03d}'
    inp.append({'key': k, **{f: x.get(f) for f in ('file', 'line', 'where', 'disposition', 'claim', 'failure_mode', 'evidence')}})
    priv[k] = {'wt': wt, 'id': x.get('id')}
os.makedirs(adj, exist_ok=True)
json.dump(inp, open(os.path.join(adj, 'adjudication-input.json'), 'w'), indent=1)
json.dump(priv, open(os.path.join(C, 'adjudication-map-private.json'), 'w'), indent=1)
print(f'{len(inp)} findings pooled from {len(set(v["wt"] for v in priv.values()))} runs')
