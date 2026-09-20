#!/usr/bin/env python3
"""Wall-clock and token decomposition of a resolve-ticket / pr-harden run, from its own transcript.

Usage:  run-timing.py ~/.claude/projects/<project-dir>/<session>.jsonl [...]

The harness for the "is the pipeline getting faster?" gate. Per run:
  * the phase timeline (skill invocations, agent waves, PR created, PR ready)
  * where the wall clock went: orchestrator generating, waiting on a subagent, tools
  * per-wave agent latency and the spread inside a wave (a wave costs its SLOWEST member)
  * context and output tokens, for the orchestrator and for the agent fleet

Two things this file got wrong before 2026-09-17, both of which read as plausible numbers:

  * **Agent latency came from an `agentId` in a notification.** Background agents return
    `agentId` from the spawn itself and complete through a task notification the old matcher did not
    see, so every latency printed `?` and the whole wave chain landed in `other idle` — the run then
    looked like an orchestrator that waits on nothing. Spans now come from
    `<session>/subagents/agent-*.meta.json` (which carries `toolUseId`, so the spawn call is the
    join key) and each agent's own transcript, which is ground truth for when it started and stopped.
  * **Output tokens were summed per transcript EVENT.** One assistant message appears once per
    content block, each copy carrying the same final `usage`, so the sum was inflated by the average
    blocks-per-message — 2.1x on the run it was first published for. Everything token-shaped is now
    deduplicated by message id.

A run is cut at the first gap > 45 min, which is the session going idle after the run ended. The cut
is inert on every run measured so far (largest intra-run gap: 19 min) and is reported when it fires.
"""
import json, sys, os, re, glob, datetime, collections

CUT = 45 * 60


def ts(s):
    return datetime.datetime.fromisoformat(s.replace('Z', '+00:00')).timestamp()


def load(path, cut=True):
    ev = []
    for line in open(path, errors='replace'):
        line = line.strip()
        if not line:
            continue
        try:
            e = json.loads(line)
        except Exception:
            continue
        if e.get('timestamp'):
            e['_t'] = ts(e['timestamp'])
            ev.append(e)
    ev.sort(key=lambda e: e['_t'])
    if not cut:
        return ev, 0
    end, gap = len(ev), 0
    for i in range(1, len(ev)):
        if ev[i]['_t'] - ev[i - 1]['_t'] > CUT:
            end, gap = i, ev[i]['_t'] - ev[i - 1]['_t']
            break
    return ev[:end], gap


def blocks(e):
    c = (e.get('message') or {}).get('content')
    return c if isinstance(c, list) else []


def tokens(ev):
    """Context and output per run, deduplicated by message id. Never sum these over events."""
    seen = {}
    for e in ev:
        if e.get('type') != 'assistant':
            continue
        m = e.get('message') or {}
        u = m.get('usage') or {}
        seen.setdefault(m.get('id'), (
            (u.get('input_tokens', 0) or 0) + (u.get('cache_read_input_tokens', 0) or 0)
            + (u.get('cache_creation_input_tokens', 0) or 0),
            u.get('output_tokens', 0) or 0))
    ctx = [v[0] for v in seen.values()]
    return dict(calls=len(seen), ctxsum=sum(ctx), peak=max(ctx) if ctx else 0,
                start=ctx[0] if ctx else 0, out=sum(v[1] for v in seen.values()))


def agent_spans(path):
    """{tool_use_id: (start, end, description)} from the subagents directory, or {} if absent."""
    sub = os.path.join(os.path.dirname(path), os.path.basename(path)[:-6], 'subagents')
    out = {}
    for mf in glob.glob(os.path.join(sub, '*.meta.json')):
        try:
            meta = json.load(open(mf))
        except Exception:
            continue
        aid = os.path.basename(mf)[len('agent-'):-len('.meta.json')]
        tr = os.path.join(sub, 'agent-%s.jsonl' % aid)
        if not os.path.exists(tr):
            continue
        ev, _ = load(tr, cut=False)
        if not ev:
            continue
        out[meta.get('toolUseId')] = (ev[0]['_t'], ev[-1]['_t'], meta.get('description') or '', tr)
    return out


def report(path):
    ev, gap = load(path)
    if not ev:
        return
    t0, t1 = ev[0]['_t'], ev[-1]['_t']
    total = t1 - t0
    spans = agent_spans(path)
    spawns, marks, opened = [], [], {}
    tool = collections.Counter()
    tooln = collections.Counter()
    mvn = 0.0
    mvnn = 0
    for e in ev:
        for b in blocks(e):
            if b.get('type') == 'tool_use':
                nm, inp = b.get('name'), (b.get('input') or {})
                opened[b['id']] = (nm, e['_t'], inp)
                if nm == 'Agent':
                    spawns.append((e['_t'], b['id'], (inp.get('description') or '')[:40]))
                    marks.append((e['_t'], 'spawn: ' + (inp.get('description') or '')[:40]))
                elif nm == 'Skill':
                    marks.append((e['_t'], 'SKILL /' + str(inp.get('skill'))))
                elif nm == 'Bash':
                    cmd = inp.get('command', '')
                    if 'gh pr create' in cmd:
                        marks.append((e['_t'], '>>> PR CREATED'))
                    if 'gh pr ready' in cmd:
                        marks.append((e['_t'], '>>> PR READY'))
            elif b.get('type') == 'tool_result':
                u = opened.pop(b.get('tool_use_id'), None)
                if u:
                    nm, st, inp = u
                    tool[nm] += e['_t'] - st
                    tooln[nm] += 1
                    if nm == 'Bash' and 'mvn' in (inp.get('command') or ''):
                        mvn += e['_t'] - st
                        mvnn += 1
    # agent intervals: the subagent transcript where there is one, else the spawn's own result
    iv = []
    for st, tid, desc in spawns:
        s = spans.get(tid)
        if s:
            iv.append((st, s[1], desc or s[2]))
        else:
            iv.append((st, None, desc))
    done = sorted((a, b) for a, b, _ in iv if b)
    union, cur = 0.0, None
    for a, b in done:
        if cur is None:
            cur = [a, b]
        elif a <= cur[1]:
            cur[1] = max(cur[1], b)
        else:
            union += cur[1] - cur[0]
            cur = [a, b]
    if cur:
        union += cur[1] - cur[0]
    gen = sum(ev[i]['_t'] - ev[i - 1]['_t'] for i in range(1, len(ev))
              if ev[i].get('type') == 'assistant' and ev[i]['_t'] > ev[i - 1]['_t'])
    tk = tokens(ev)
    fleet = [tokens(load(s[3], cut=False)[0]) for s in spans.values()]

    print('=' * 92)
    print('%s  run %.0f min (%.1f h)' % (os.path.basename(path), total / 60, total / 3600))
    print('  orchestrator generating   %6.0f min  (%.0f%%)' % (gen / 60, 100 * gen / total))
    print('  >=1 subagent outstanding  %6.0f min  (%.0f%%)  in %d agents'
          % (union / 60, 100 * union / total, len(iv)))
    print('  maven                     %6.0f min  (%.0f%%, n=%d)'
          % (mvn / 60, 100 * mvn / total, mvnn))
    if gap:
        print('  ! cut at a %.0f-min gap — the run may continue past it' % (gap / 60))
    if len(done) < len(iv):
        print('  ! %d of %d agents have no subagent transcript; their spans are missing'
              % (len(iv) - len(done), len(iv)))
    print('  tokens: %d calls, context %.0fk -> peak %.0fk, ctxSUM %.1fM, output %.0fk'
          % (tk['calls'], tk['start'] / 1000, tk['peak'] / 1000, tk['ctxsum'] / 1e6,
             tk['out'] / 1000))
    if fleet:
        fc = sum(f['ctxsum'] for f in fleet)
        print('  fleet:  %d agents, ctxSUM %.1fM (%.0f%% of the run), output %.0fk'
              % (len(fleet), fc / 1e6, 100 * fc / (fc + tk['ctxsum']),
                 sum(f['out'] for f in fleet) / 1000))
    print('  tools: ' + ', '.join('%s %.0fm/n=%d' % (k, v / 60, tooln[k])
                                  for k, v in tool.most_common()))
    print('  --- timeline (min from start)')
    for t, lab in marks:
        print('   %6.1f  %s' % ((t - t0) / 60, lab))
    print('  --- waves (a wave costs its slowest member)')
    waves = []
    for st, end, desc in sorted(iv, key=lambda x: x[0]):
        if waves and st - waves[-1][-1][0] <= 300:
            waves[-1].append((st, end, desc))
        else:
            waves.append([(st, end, desc)])
    tax = 0.0
    for i, w in enumerate(waves, 1):
        ends = [e for _, e, _ in w if e]
        span = (max(ends) - min(s for s, _, _ in w)) / 60 if ends else 0
        durs = [(e - s) / 60 for s, e, _ in w if e]
        if len(durs) > 1:
            tax += max(durs) - sorted(durs)[len(durs) // 2]
        print('   %6.1f  n=%d  span=%5.1fm  [%s]  %s'
              % ((w[0][0] - t0) / 60, len(w), span,
                 ', '.join('%.1f' % d for d in durs) or '?', w[0][2][:44]))
    print('  waves: %d | sum of wave spans %.0f min (%.0f%% of run) | slowest-member tax %.0f min'
          % (len(waves), sum((max([e for _, e, _ in w if e]) - min(s for s, _, _ in w))
                             for w in waves if any(e for _, e, _ in w)) / 60,
             100 * union / total, tax))


for p in sys.argv[1:]:
    report(p)
