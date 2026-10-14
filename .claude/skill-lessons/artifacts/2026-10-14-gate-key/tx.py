"""Summarise a Claude Code transcript one event per output line.

    tx.py FILE [REGEX] [WIDTH] [FROM-TO]

Every JSONL line becomes one or more rows "L<n> <kind> <summary>"; with REGEX only rows whose summary
matches are printed. Line numbers are the file's own (1-based), as the ledger cites them.
"""
import json
import re
import sys


def flat(c):
    if isinstance(c, str):
        return c
    if isinstance(c, list):
        out = []
        for x in c:
            if isinstance(x, dict):
                if x.get('type') == 'text':
                    out.append(x.get('text', ''))
                elif x.get('type') == 'tool_result':
                    out.append(flat(x.get('content')))
                else:
                    out.append(json.dumps(x)[:2000])
            else:
                out.append(str(x))
        return '\n'.join(out)
    return json.dumps(c)


def rows(path):
    for n, line in enumerate(open(path, encoding='utf-8', errors='replace'), 1):
        try:
            o = json.loads(line)
        except ValueError:
            yield n, 'BADJSON', line[:200]
            continue
        t = o.get('type')
        msg = o.get('message') or {}
        content = msg.get('content') if isinstance(msg, dict) else None
        if t == 'assistant' and isinstance(content, list):
            for x in content:
                if x.get('type') == 'text':
                    yield n, 'A-text', x.get('text', '')
                elif x.get('type') == 'tool_use':
                    yield n, 'A-use:' + x.get('name', '?'), json.dumps(x.get('input'))
                elif x.get('type') == 'thinking':
                    yield n, 'A-think', x.get('thinking', '')
        elif t == 'user':
            if isinstance(content, list):
                for x in content:
                    if x.get('type') == 'tool_result':
                        yield n, 'U-result', flat(x.get('content'))
                    elif x.get('type') == 'text':
                        yield n, 'U-text', x.get('text', '')
                    else:
                        yield n, 'U-' + str(x.get('type')), json.dumps(x)[:2000]
            else:
                yield n, 'U-str', flat(content)
        elif t == 'system':
            yield n, 'S-' + str(o.get('subtype')), json.dumps({k: v for k, v in o.items() if k not in ('uuid', 'parentUuid', 'sessionId', 'cwd', 'version', 'gitBranch', 'userType', 'isSidechain')})
        elif t == 'attachment':
            yield n, 'ATT', json.dumps(o.get('attachment', o))[:4000]
        else:
            yield n, 'T-' + str(t), json.dumps(o)[:1500]


def main():
    path = sys.argv[1]
    pat = re.compile(sys.argv[2], re.S) if len(sys.argv) > 2 and sys.argv[2] else None
    width = int(sys.argv[3]) if len(sys.argv) > 3 else 300
    lo, hi = 1, 10 ** 9
    if len(sys.argv) > 4:
        a, _, b = sys.argv[4].partition('-')
        lo, hi = int(a), int(b) if b else 10 ** 9
    for n, kind, s in rows(path):
        if n < lo or n > hi:
            continue
        if pat and not pat.search(kind + ' ' + s):
            continue
        s1 = s.replace('\n', ' ⏎ ')
        print(f'L{n} {kind} {s1[:width]}')


if __name__ == '__main__':
    main()
