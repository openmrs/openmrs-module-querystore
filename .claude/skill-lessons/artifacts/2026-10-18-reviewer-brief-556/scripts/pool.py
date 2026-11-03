"""Pool both arms' findings into one schema (blocking / non-blocking), strip run and arm, shuffle."""
import json, random, sys, os, re
C, outdir, adj = sys.argv[1], sys.argv[2], sys.argv[3]
def load(path):
    txt = open(path).read()
    try: return json.loads(txt)
    except Exception:
        m = re.search(r'\{[\s\S]*\}', txt); return json.loads(m.group(0))
rows = []
for f in sorted(os.listdir(outdir)):
    if not f.endswith('.json'): continue
    wt = f[:-5]; d = load(os.path.join(outdir, f))
    for x in d.get('findings', []) or []:
        if 'blocking' in x and 'disposition' not in x:      # pr-harden reviewer schema
            disp = 'blocking' if x.get('blocking') else 'non-blocking'
            claim = x.get('finding') or x.get('claim') or ''
        else:                                               # pr-review replay schema
            disp = 'blocking' if (x.get('disposition') or '').lower() == 'blocking' else 'non-blocking'
            claim = x.get('claim') or x.get('finding') or ''
        rows.append((wt, x.get('id'), {'file': x.get('file'), 'line': x.get('line'), 'disposition': disp,
                     'claim': claim, 'failure_mode': x.get('failure_mode') or '', 'evidence': x.get('evidence') or ''}))
random.SystemRandom().shuffle(rows)
inp, priv = [], {}
for i, (wt, rid, body) in enumerate(rows, 1):
    k = f'E{i:03d}'; inp.append({'key': k, 'kind': 'finding', **body}); priv[k] = {'wt': wt, 'kind': 'finding', 'id': rid}
os.makedirs(adj, exist_ok=True)
json.dump(inp, open(os.path.join(adj, 'adjudication-input.json'), 'w'), indent=1)
json.dump(priv, open(os.path.join(C, 'adjudication-map-private.json'), 'w'), indent=1)
print(f'{len(inp)} findings pooled from {len(set(v["wt"] for v in priv.values()))} runs')
