"""Flag forbidden reads in a stream-json transcript's tool calls (only tool_use inputs are inspected)."""
import json, sys, re, os
C2 = os.path.dirname(os.path.abspath(__file__))
key = json.load(open(f'{C2}/KEY-private.json'))
FORBID = [
    (r'pulls/6551\b|issues/6551\b|pull/6551\b|pr (view|diff|checks) 6551\b', 'live PR 6551'),
    (r'atlassian\.net', 'JIRA fetch'),
    (r'/Users/danielkayiwa/Projects(?![\w-])', "user's Projects"),
    (r'replay-6551-ctl|PROTOCOL\.md|KEY-private|brief-template|/briefs/|skill-lessons|ab-6551', 'control files / A/B key'),
]
def calls(path):
    for line in open(path, encoding='utf-8', errors='replace'):
        try: m = json.loads(line)
        except Exception: continue
        if m.get('type') != 'assistant': continue
        for c in (m.get('message', {}).get('content') or []):
            if c.get('type') == 'tool_use':
                yield c.get('name'), json.dumps(c.get('input'), ensure_ascii=False)
def audit(path, own):
    own_skill = key['skill_dirs'][key['slots'][own]['arm']] if own in key['slots'] else None
    n = 0; flags = []
    for name, inp in calls(path):
        n += 1
        for pat, why in FORBID:
            if re.search(pat, inp): flags.append((why, name, inp[:200]))
        for seg in re.findall(r'(?:/Users/danielkayiwa/|~/)\.claude[^"\s]*', inp):
            if f'scratchpad-replay-6551-{own}/' not in seg: flags.append(('~/.claude', name, seg[:200]))
        for other in re.findall(r'replay-6551/(wt-\d\d)', inp):
            if other != own: flags.append(('other run dir ' + other, name, inp[:200]))
        for sk in re.findall(r'replay-6551/skills/(x\d)', inp):
            if sk != own_skill: flags.append(('other arm skill ' + sk, name, inp[:200]))
    return n, flags
if __name__ == '__main__':
    for p in sys.argv[1:]:
        own = re.search(r'(wt-\d\d)', os.path.basename(p)).group(1)
        n, flags = audit(p, own)
        print(f'{own}: {n} tool calls, {len(flags)} flagged')
        for f in flags: print('   ', f)
