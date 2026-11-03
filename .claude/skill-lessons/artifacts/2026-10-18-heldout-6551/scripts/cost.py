"""Per-run model, turns, duration, cost and tokens from a stream-json transcript's init and result messages."""
import json, sys, re
for p in sys.argv[1:]:
    init = res = None; tools = 0
    for line in open(p, encoding='utf-8', errors='replace'):
        try: m = json.loads(line)
        except Exception: continue
        if m.get('type') == 'system' and m.get('subtype') == 'init': init = m
        if m.get('type') == 'result':
            if res is None: res = dict(m, usage=dict(m.get('usage', {})))
            else:  # a run re-invoked by a background-task notification writes one result per segment
                res['num_turns'] = (res.get('num_turns') or 0) + (m.get('num_turns') or 0)
                res['duration_ms'] = (res.get('duration_ms') or 0) + (m.get('duration_ms') or 0)
                res['total_cost_usd'] = max(res.get('total_cost_usd') or 0, m.get('total_cost_usd') or 0)
                for k, v in (m.get('usage') or {}).items():
                    if isinstance(v, (int, float)): res['usage'][k] = res['usage'].get(k, 0) + v
                res['segments'] = res.get('segments', 1) + 1
        if m.get('type') == 'assistant':
            tools += sum(1 for c in (m.get('message', {}).get('content') or []) if c.get('type') == 'tool_use')
    wt = re.search(r'(wt-\d\d)', p).group(1)
    if not res:
        print(f'{wt}: NO RESULT MESSAGE (run unfinished or died); tool calls so far {tools}'); continue
    u = res.get('usage', {})
    tok_in = u.get('input_tokens', 0) + u.get('cache_creation_input_tokens', 0) + u.get('cache_read_input_tokens', 0)
    print(f"{wt}: model={init.get('model') if init else '?'} subtype={res.get('subtype')} is_error={res.get('is_error')} "
          f"segments={res.get('segments', 1)} turns={res.get('num_turns')} tools={tools} min={res.get('duration_ms', 0)/60000:.1f} cost=${res.get('total_cost_usd', 0):.2f} "
          f"in(all)={tok_in:,} out={u.get('output_tokens', 0):,}")
