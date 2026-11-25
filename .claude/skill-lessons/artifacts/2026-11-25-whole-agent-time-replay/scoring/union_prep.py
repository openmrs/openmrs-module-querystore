"""Exploratory, after the pre-registered result: one blind judge per subject lists every distinct defect the six run
reports claim, and marks which reports carry it. Labels U1-U6 at random; mapping to union-mapping-<s>.json."""
import json, os, random, re, sys
home = os.path.expanduser('~')
B = f'{home}/.claude/blind-runs'
out = sys.argv[1]
MASKS = [(re.compile(r'\b20\d\d-\d\d-\d\d-[a-z0-9]+(?:-[a-z0-9]+)+'), '<run folder>'),
         (re.compile(r'\bdlr2?-[A-Za-z0-9-]+'), '<folder>'),
         (re.compile(r'\b(?:25|75)(?=[ -]min)'), '<N>')]
rng = random.SystemRandom()
for s in ('s1', 's2', 's3'):
    items = [(f'{a}-{w}', f'{B}/2026-11-25-dlr2-{s}-a{a}-{w}/log/report.md') for a in (25, 75) for w in (1, 2, 3)]
    rng.shuffle(items)
    mapping, parts = {}, []
    for i, (src, path) in enumerate(items, 1):
        label = f'U{i}'
        mapping[label] = {'source': src, 'path': path}
        text = open(path).read()
        for rx, rep in MASKS:
            text, n = rx.subn(rep, text)
            if n:
                print(f'  {s} {label} ({src}): {n} x {rep}')
        parts.append(f'===== BEGIN {label} =====\n{text.strip()}\n===== END {label} =====\n')
    brief = f"""Stop after about 20 minutes of work and report what you have.

Below are six code-review reports, U1 to U6, each written by a different reviewer on the same change. Change nothing,
write nothing outside your own folder, use no network, post nothing, push nothing. You need no repository: everything
is below.

## Your task

List every distinct defect that any of the six reports claims. A defect is something wrong in the code or the tests
under review: a bug, a regression, a security or data-loss risk, a test that passes when the code it should guard is
broken, a missing test for a behaviour the change adds, or a breaking change for existing users.

Leave out:
- style, naming, unused imports, formatting and comment wording;
- anything a report says it checked and found fine;
- anything a report lists only as not run or not checked, without claiming it is wrong;
- questions to the author that claim nothing is wrong.

Merge claims that describe the same gap in different words, or reach it through a different mutation or example. Keep
two claims apart when they are different gaps in the same code (for example two different conditions of one query).

Then, for each defect and each report, mark exactly one:
- FOUND: the report claims this defect and backs it with a run, a mutation result, or a file:line it read.
- NAMED: the report claims it without such backing.
- ABSENT: the report does not claim it.

## Output

First, one line per defect, numbered D1, D2, ..., exactly in this form:
DEFECT <Dn> <PRE-EXISTING|INTRODUCED|UNCLEAR> <one-line description>
PRE-EXISTING means a report says the base branch behaves the same; INTRODUCED means a report says the change under
review causes it; UNCLEAR otherwise.
Then one line per defect and report, exactly in this form, and nothing else on those lines:
MARK <report> <Dn> <FOUND|NAMED|ABSENT>

## The reports

{''.join(parts)}"""
    open(f'{out}/union-{s}.md', 'w').write(brief)
    json.dump(mapping, open(f'{out}/union-mapping-{s}.json', 'w'), indent=1)
    print(s, 'brief words', len(brief.split()))
