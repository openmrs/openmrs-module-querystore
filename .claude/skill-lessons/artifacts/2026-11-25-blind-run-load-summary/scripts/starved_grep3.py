"""Refuter 1's pattern for 'the report says the machine was loaded or slow', over the reports written 2026-11-20 to
2026-11-25. Prints each hit with context, and says which are round agents' (named <pr>-r<n>-<agent> or <pr>-<agent>
by a review), not refuters', replays', judges' or retro agents'."""
import glob, os, re, sys
pat = re.compile(r"load average|machine (was|is) (at load|(very )?(slow|loaded|busy|saturated)|shared)|machine was shared|loading the machine|machine load|slowed|"
                 r"under load|heavily loaded|very slow", re.I)
NOT_ROUND = re.compile(r"refute|retro|dlr|blindz|blindcost|zjudge|judge|replay|probe|brfix|desc-|fixes|refuter", re.I)
reports = sorted(glob.glob(os.path.expanduser("~/.claude/blind-runs/2026-11-2[0-5]-*/log/report.md")))
hits = []
for p in reports:
    name = os.path.basename(os.path.dirname(os.path.dirname(p)))
    text = open(p, errors="replace").read()
    m = pat.search(text)
    if m:
        hits.append((name, not NOT_ROUND.search(name), text[max(0, m.start() - 80):m.end() + 60].replace("\n", " ")))
print(f"reports: {len(reports)}; hits: {len(hits)}; round agents among them: {sum(1 for h in hits if h[1])}")
for n, r, ctx in hits:
    print(("ROUND " if r else "other ") + n + ": " + ctx[:200])
