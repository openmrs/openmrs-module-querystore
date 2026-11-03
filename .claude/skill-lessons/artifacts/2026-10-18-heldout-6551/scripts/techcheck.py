"""Exploratory: which verification techniques each run actually executed (tool-call inputs only)."""
import json, sys, re
T = {'non-admin user': r'becomeUser|addProxyPrivilege|removeProxyPrivilege|Context\.logout|authenticate\(',
     'flush': r'flushSession|\.flush\(\)',
     'NOT_SUPPORTED/tx': r'NOT_SUPPORTED|isActualTransactionActive|TestTransaction',
     'read techniques file': r'openmrs-verification\.md'}
C2 = sys.argv[1]
for wt in sys.argv[2:]:
    hits = {k: 0 for k in T}
    for line in open(f'{C2}/logs/{wt}.jsonl', encoding='utf-8', errors='replace'):
        try: m = json.loads(line)
        except Exception: continue
        if m.get('type') != 'assistant': continue
        for c in (m.get('message', {}).get('content') or []):
            if c.get('type') == 'tool_use':
                s = json.dumps(c.get('input'))
                for k, p in T.items():
                    if re.search(p, s): hits[k] += 1
    print(wt, hits)
