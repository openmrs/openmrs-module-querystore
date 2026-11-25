"""Build one blind judge brief per subject: six run reports plus two calibration reports, relabelled R1-R8 at random.
The label -> source mapping goes to mapping-<subject>.json, which the judge never sees."""
import json, os, random, re, sys
home = os.path.expanduser('~')
B = f'{home}/.claude/blind-runs'
L = f'{home}/.claude/skill-lessons'
C = f'{L}/confidential/2026-11-25-whole-agent-time-replay'
out = sys.argv[1]
KEYS = {
 's1': {'K86': "validate_shouldRejectASourceWithoutARequiredLocationTag tests only a location with no tags, so a check weakened to accept any tagged location (hasAnyTag changed to !location.getTags().isEmpty()) passes the suite; the test needs a location carrying a different tag."},
 's2': {'K71a': "Role inheritance in the access path is untested: getPrivilegeScopes and getRecordPrivilegeFilters use getAllRoles()/getAllParentRoles(), and every fixture role holds its privileges directly, so dropping inheritance passes the suite.",
        'K71b': "The balance query's per-item match and its `sb.expiration is null` half are untested: the fixture has one item and only dated batches (no batch without an expiry date).",
        'K71c': "Dispatching a SUBMITTED operation requires the APPROVE privilege, and no test pins it (the two-role test uses a receipt, which cannot be dispatched), so changing that privilege check passes the suite."},
 's3': None,
}
s3 = open(f'{C}/s3-keys.md').read()
KEYS['s3'] = {}
for line in s3.splitlines():
    if re.match(r'- \*\*K\d', line) and ':**' in line:
        k = line.split('**')[1].rstrip(':')
        KEYS['s3'][k] = line.split(':**', 1)[1].strip()
# continuation lines of each S3 key
cur = None
for line in s3.splitlines():
    if re.match(r'- \*\*K\d', line):
        cur = line.split('**')[1].rstrip(':')
    elif cur and line.startswith('  ') and line.strip():
        KEYS['s3'][cur] += ' ' + line.strip()
    elif not line.strip():
        cur = None
CAL = {
 's1': (f'{B}/2026-11-24-pr86-r2-unanchored/log/report.md', f'{B}/2026-11-23-pr71-r5-review/log/report.md'),
 's2': (f'{B}/2026-11-24-pr71-r7-review/log/report.md', f'{B}/2026-11-24-pr86-r1-unanchored/log/report.md'),
 's3': (f'{C}/s3-known-good-report.md', f'{B}/2026-11-24-pr86-r1-unanchored/log/report.md'),
}
# Reports name their own run folder (2026-11-25-dlr2-s1-a75-2), a folder they chose (dlr2-a25-store), or their stop line
# ("about 75 minutes", "the 25-minute limit"), each of which tells the judge the arm or the calibration. Mask them.
MASKS = [(re.compile(r'\b20\d\d-\d\d-\d\d-[a-z0-9]+(?:-[a-z0-9]+)+'), '<run folder>'),
         (re.compile(r'\bdlr2?-[A-Za-z0-9-]+'), '<folder>'),
         (re.compile(r'\b(?:25|75)(?=[ -]min)'), '<N>')]
def mask(text, label, src):
    for rx, rep in MASKS:
        text, n = rx.subn(rep, text)
        if n:
            print(f'  {s} {label} ({src}): {n} x {rep}')
    return text
rng = random.SystemRandom()
for s in ('s1', 's2', 's3'):
    items = [(f'{a}-{w}', f'{B}/2026-11-25-dlr2-{s}-a{a}-{w}/log/report.md') for a in (25, 75) for w in (1, 2, 3)]
    items += [('known-good', CAL[s][0]), ('known-bad', CAL[s][1])]
    rng.shuffle(items)
    mapping, parts = {}, []
    for i, (src, path) in enumerate(items, 1):
        label = f'R{i}'
        mapping[label] = {'source': src, 'path': path}
        text = open(path).read() if os.path.exists(path) else '(no report: this run wrote none)'
        text = mask(text, label, src)
        parts.append(f'===== BEGIN {label} =====\n{text.strip()}\n===== END {label} =====\n')
    keys = '\n'.join(f'- {k}: {v}' for k, v in KEYS[s].items())
    brief = f"""Stop after about 20 minutes of work and report what you have.

You are judging code-review reports against a list of known defects. Change nothing, write nothing outside your own
folder, use no network, post nothing, push nothing. You need no repository: everything is below.

## Known defects

{keys}

## What to mark

For each report and each defect, mark exactly one:
- FOUND: the report names this defect and backs it with a run, a mutation result, or a file:line it read.
- NAMED: the report names this defect without such backing.
- ABSENT: the report does not name it. A different gap in the same area is ABSENT.
A report names a defect when it describes the same gap, in any words.

## Output

First, one line per report and defect, exactly in this form, and nothing else on those lines:
MARK <report> <defect> <FOUND|NAMED|ABSENT>
Then, for each FOUND or NAMED mark, one line quoted from that report that shows it.

## The reports

{''.join(parts)}"""
    open(f'{out}/judge-{s}.md', 'w').write(brief)
    json.dump(mapping, open(f'{out}/mapping-{s}.json', 'w'), indent=1)
    print(s, 'keys', list(KEYS[s]), 'brief words', len(brief.split()))
