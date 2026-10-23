"""List every tool call in a stream-json transcript and flag forbidden reads.
Only tool_use inputs are inspected: the brief itself names the forbidden paths."""
import json, sys, re
RUN = '/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-queue/5743c726-b776-450f-a755-a07953eaf17e/scratchpad/replay'
FORBID = [
    (r'pulls/96\b|issues/96\b|pull/96\b|pr (view|diff|checks) 96\b|--repo openmrs/openmrs-module-queue', 'live PR 96'),
    (r'atlassian\.net', 'JIRA fetch (review by hand)'),
    (r'/Users/danielkayiwa/Projects/openmrs/queue(?![\w-])', 'user queue checkout'),
    (r'\.claude/projects', 'project memory/transcripts'),
    (r'replay-ctl|PROTOCOL\.md|KEY-private|brief-template|/briefs/', 'control files'),
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
    n = 0; flags = []
    for name, inp in calls(path):
        n += 1
        own_dir = 'scratchpad-replay-' + own + '/'
        for pat, why in FORBID:
            if re.search(pat, inp):
                if why == 'project memory/transcripts' and all(own_dir in seg for seg in re.findall(r'\.claude/projects/[^"\s]*', inp)):
                    continue
                flags.append((why, name, inp[:220]))
        for other in re.findall(r'replay/(wt-\d\d)', inp):
            if other != own: flags.append(('other run dir ' + other, name, inp[:220]))
    return n, flags
if __name__ == '__main__':
    for p in sys.argv[1:]:
        own = re.search(r'(wt-\d\d)', p).group(1)
        n, flags = audit(p, own)
        print(f'{own}: {n} tool calls, {len(flags)} flagged')
        for f in flags: print('   ', f)
