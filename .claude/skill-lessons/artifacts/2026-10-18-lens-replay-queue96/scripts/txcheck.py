"""Exploratory, not pre-registered: what each run said about the purge's transaction."""
import json, sys, re
C = sys.argv[1]
for wt in sys.argv[2:]:
    hits = []
    for line in open(f'{C}/logs/{wt}.jsonl', encoding='utf-8', errors='replace'):
        try: m = json.loads(line)
        except Exception: continue
        if m.get('type') != 'assistant': continue
        for c in (m.get('message', {}).get('content') or []):
            t = c.get('text') or c.get('thinking') or (json.dumps(c.get('input')) if c.get('type') == 'tool_use' else '')
            for mm in re.finditer(r'[^.\n]{0,160}(transaction|TransactionInterceptor|roll ?back|rolled back|isActualTransactionActive|NOT_SUPPORTED|purge\.inUse|atomic)[^.\n]{0,160}', t or '', re.I):
                hits.append((c.get('type'), mm.group(0).strip()))
    print(f'== {wt}: {len(hits)} mentions')
    seen = set()
    for typ, h in hits:
        k = h[:90]
        if k in seen: continue
        seen.add(k); print(f'  [{typ}] {h[:300]}')
