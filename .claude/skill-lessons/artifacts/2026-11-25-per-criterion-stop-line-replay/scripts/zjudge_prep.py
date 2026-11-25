"""One blind judge brief per subject: the 6 replay reports plus a known-good and a known-bad, relabelled R1-R8 at random.
Adapted from the 2026-11-25 whole-agent replay's judge_prep.py (same marks, same masking). The label -> source mapping
goes to mapping-<s>.json, which the judge never sees. Keys are those of PREREGISTERED.md, verbatim."""
import json, os, random, re, sys
B = os.path.expanduser('~/.claude/blind-runs')
out = sys.argv[1]
day = sys.argv[2] if len(sys.argv) > 2 else '2026-11-25'
KEYS = {
 's1': {'G': "The tests put only receipt and issue types through the lifecycle; making transfer-out completion credit the source instead of the destination (TransferOutOperationTypeProcessor) passes the suite.",
        'I': "Cancelling an operation in NEW or RETURNED: requiring APPROVE instead of MUTATE (StockManagementServiceImpl, about :1602) passes the suite; no test has a mutate-only user cancel a NEW operation.",
        'H': "Completing a stock issue straight from NEW: dropping `&& !requiresDispatchAcknowledgement()` from the completion check, so an issue can skip dispatch and receipt, passes the suite.",
        'J': "The insufficient-stock check on a direct dispatch from NEW: removing `|| DISPATCH` from the validation condition passes the suite; no test dispatches more than the balance straight from NEW."},
 's2': {'T': "The moved stock-source text search now trims the search string (HibernateStockSourceDAO), so a query with leading or trailing spaces matches differently from master.",
        'D2': "Nothing pins that voided stock sources are excluded; the include-voided step of master's removed findStockSources_shouldFilterOnAllCriteria was dropped (removing the voided filter passes the suite)."},
}
CAL = {'s1': (f'{B}/2026-11-24-pr71-r8-lifecycle/log/report.md', f'{B}/2026-11-24-pr71-r8-unchanged/log/report.md'),
       's2': (f'{B}/2026-11-25-sm80-r1-behaviour/log/report.md', f'{B}/2026-11-25-pr57-r1-crit/log/report.md')}
MASKS = [(re.compile(r'\b20\d\d-\d\d-\d\d-[a-z0-9]+(?:-[a-z0-9]+)+'), '<run folder>'),
         (re.compile(r'\b(?:blindz|blind-runs?)[-/][A-Za-z0-9/._-]+'), '<folder>'),
         (re.compile(r'\b(?:25|75)(?=[ -]min)'), '<N>')]
rng = random.SystemRandom()
for s in ('s1', 's2'):
    items = [(f'{a}-{w}', f'{B}/{day}-blindz-{s}-a{a}-{w}/log/report.md') for a in (25, 75) for w in (1, 2, 3)]
    # PREREGISTERED.md deviation: s1's A75 run 1 is blindz-s1-a75-4 (blindz-s1-a75-1 backgrounded its chain and ended)
    items = [(src, path.replace('blindz-s1-a75-1/', 'blindz-s1-a75-4/')) for src, path in items]
    items += [('known-good', CAL[s][0]), ('known-bad', CAL[s][1])]
    rng.shuffle(items)
    mapping, parts = {}, []
    for i, (src, path) in enumerate(items, 1):
        label = f'R{i}'
        mapping[label] = {'source': src, 'path': path}
        text = open(path).read() if os.path.exists(path) and os.path.getsize(path) else '(no report: this run wrote none)'
        for rx, rep in MASKS:
            text, n = rx.subn(rep, text)
            if n: print(f'  {s} {label} ({src}): {n} x {rep}')
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
