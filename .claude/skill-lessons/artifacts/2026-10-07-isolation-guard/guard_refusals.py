"""EnterWorktree isolation-guard refusals in one session, by the pr-harden 0.38.1 evidence's method.

A refusal is a Bash result carrying "This session is isolated in the worktree". A command "runs git"
when `git` is a command word outside heredoc bodies. A plain script call is one command that runs a
.sh/.py file named by a literal absolute path, with no chaining, substitution or variable.

    guard_refusals.py MAIN.jsonl FROM[-TO] [SUBAGENT.jsonl ...]
    guard_refusals.py --calibrate PROJECTS_DIR

FROM is the main transcript's EnterWorktree line; subagent transcripts are read whole.
--calibrate re-reads the seven cases the 0.38.1 entry names, and its five windows, whose refusals
that entry counts as 37; it exits 1 if any case is misread or the total differs.
"""
import collections
import json
import re
import sys

MARK = 'This session is isolated in the worktree'
PLAIN_SCRIPT = re.compile(
    r'^\s*(?:(?:bash|sh|zsh|python3?)\s+(?:-I\s+)?)?(/[^\s;&|$`]+\.(?:sh|py))(?:\s+[^;&|$`\n<>]*)?\s*$')


def strip_heredocs(cmd):
    out, lines, i = [], cmd.split('\n'), 0
    while i < len(lines):
        out.append(lines[i])
        tags = re.findall(r"<<-?\s*\\?['\"]?([A-Za-z_][A-Za-z0-9_]*)['\"]?", lines[i])
        if tags:
            i += 1
            while i < len(lines) and lines[i].strip() != tags[-1]:
                i += 1
            if i < len(lines):
                out.append(lines[i])
        i += 1
    return '\n'.join(out)


def runs_git(cmd):
    return re.search(r'(^|[\s;&|(`$])git(\s|$)', strip_heredocs(cmd), re.M) is not None


def compound(cmd):
    return bool(re.search(r'&&|\|\||;|\||\$\(|`|\n', strip_heredocs(cmd).strip()))


def category(result):
    m = re.search(r'but this command (.*?)(?:\. Refusing|$)', result, re.S)
    w = m.group(1) if m else result
    m = re.match(r'runs (\S+) with a (?:value|program) computed at runtime', w)
    if m:
        return f'{m.group(1)} handed a value computed at runtime'
    if re.match(r'runs a string through (eval|source)', w):
        return 'a word read as eval or source'
    if 'names git in a form too complex' in w:
        return 'names git, too complex to verify'
    if w.startswith('is too complex to verify'):
        return 'too complex to verify, no git named'
    if w.startswith('runs bash'):
        return 'runs bash'
    return 'other: ' + w[:60]


def bash_calls(path, start=1, end=None):
    """Yield (line, command, result) for every Bash call from `start` to `end`, in order."""
    uses = {}
    for i, line in enumerate(open(path)):
        o = json.loads(line)
        content = o.get('message', {}).get('content')
        if not isinstance(content, list):
            continue
        for x in content:
            if x.get('type') == 'tool_use' and x.get('name') == 'Bash':
                uses[x['id']] = (i + 1, x['input'].get('command', ''))
            elif x.get('type') == 'tool_result' and x.get('tool_use_id') in uses:
                ln, cmd = uses.pop(x['tool_use_id'])
                c = x.get('content')
                if ln >= start and (end is None or ln <= end):
                    yield ln, cmd, c if isinstance(c, str) else json.dumps(c)


def calibrate(projects):
    p = projects.rstrip('/') + '/'
    s402, s562 = p + 'c3fd0664-f6fd-4bb0-9dbe-b81585926c1e.jsonl', p + '111381c1-d104-4508-9063-81d4c5f687f1.jsonl'
    s564, s527 = p + '12e2feb4-5c6c-4205-9910-5e4d194feff4.jsonl', p + 'ebc78818-5b31-4fed-9943-24ca301a3943.jsonl'
    cases = [(s402, 1371, True, True), (s402, 1376, True, False), (s402, 1385, False, False),
             (s562, 1075, False, True), (s562, 1153, False, True), (s562, 1183, True, False),
             (s562, 1411, True, False)]
    bad = 0
    for path, ln, want_r, want_g in cases:
        got = {l: (MARK in r, runs_git(c)) for l, c, r in bash_calls(path)}
        ok = got.get(ln) == (want_r, want_g)
        bad += not ok
        print(f"{'ok ' if ok else 'BAD'} {path[-42:-34]} L{ln}: expected refused={want_r} git={want_g},"
              f" read {got.get(ln)}")
    windows = [(s562, 944, None), (s564, 295, 1682), (s564, 3255, 4152), (s402, 1356, None), (s527, 119, None)]
    total = sum(MARK in r for path, a, b in windows for _, _, r in bash_calls(path, a, b))
    print(f"{'ok ' if total == 37 else 'BAD'} the five 0.38.1 windows: {total} refusals, the entry counts 37")
    return bad + (total != 37)


def main(argv):
    if argv[1] == '--calibrate':
        sys.exit(1 if calibrate(argv[2]) else 0)
    a, _, b = argv[2].partition('-')
    groups = {'main loop': list(bash_calls(argv[1], int(a), int(b) if b else None)),
              'subagents': [c for path in argv[3:] for c in bash_calls(path)]}
    cats, plain = collections.Counter(), [0, 0]
    for name, calls in groups.items():
        refused = [c for c in calls if MARK in c[2]]
        print(f'{name}: {len(calls)} Bash calls, {len(refused)} refused;'
              f' {sum(compound(c) for _, c, _ in calls)} compound, of which {sum(compound(c) for _, c, _ in refused)} refused')
        for _, cmd, res in calls:
            if MARK in res:
                cats[category(res)] += 1
            if PLAIN_SCRIPT.match(cmd):
                plain[0] += 1
                plain[1] += MARK in res
    for c, n in cats.most_common():
        print(f'  {n:3d}  {c}')
    print(f'plain script-by-absolute-path calls: {plain[0]}, refused {plain[1]}')
    calls = groups['main loop']
    retried = [calls[k + 1] for k, c in enumerate(calls[:-1]) if MARK in c[2]]
    print(f'main-loop refusals whose next Bash call passed: {sum(MARK not in r for _, _, r in retried)}'
          f' of {len(retried)}, {sum(bool(PLAIN_SCRIPT.match(c)) for _, c, _ in retried)} of them a plain script')


if __name__ == '__main__':
    main(sys.argv)
