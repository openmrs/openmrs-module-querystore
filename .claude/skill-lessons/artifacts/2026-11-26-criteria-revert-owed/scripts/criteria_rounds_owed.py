"""For every pr-review round that started per-criterion blind agents between pr-review 0.39.0's push (2026-11-24 20:35
+03:00) and 2026-11-26, print each Step 2 agent's state from its own log folder, as pr-review 0.41.0's owed sentence
(SKILL.md:38) and blind-agents.md:10-11 read it:

  KILL      killed at its deadline: log/blind-run-summary.txt opens "blind-run: deadline of", or, where that file is
            missing or is not blind-run's (it opens otherwise), log/out.jsonl ends with an event other than a result
            event and was last written 0 to 30 s after the deadline, counted from log/BRIEF.md's mtime (REJECTED.md's
            2026-11-25 amendment)
  NOREPORT  no log/report.md, an empty one, or a short one carrying the account's usage-limit message
  NOMODEL   no system init event in log/out.jsonl (never reached the model)
  LOADHIT   the report matches the 2026-11-25 load pattern; a hit is a lead: whether the report says the load kept
            the agent from a run it planned is read in full, and criteria-rounds.tsv records that reading
  RUNNING   no result event yet and the deadline has not passed

A round still running when this ran is left out. Every round here launched its agents at --deadline 5400.

Usage: criteria_rounds_owed.py [--calibrate] [--deadline SECS] [<date>:<round-prefix> ...]
With no rounds named, it reads the 17 it was written for. A later pass names its own, as 2026-11-26:pr71-r13 for the
agents in ~/.claude/blind-runs/2026-11-26-pr71-r13-*, and names the prefix again with the next date for an agent
started after midnight. --deadline is the round record's (default 5400). Each run is listed, a re-run included: the
REVERT ON counts an agent by its last run in the round that reported and is not owed, which the reader applies.
--calibrate prints only the control agents, each beside the state its round record gives it."""
import glob, json, os, re, sys, time

BR = os.path.expanduser("~/.claude/blind-runs")
LOAD = re.compile(r"load average|machine (was|is) (at load|(very )?(slow|loaded|busy|saturated)|shared)|machine was shared|"
                  r"loading the machine|machine load|slowed|under load|heavily loaded|very slow", re.I)
LIMIT = re.compile(r"hit your (weekly |5-hour |session )?limit|usage limit|resets ", re.I)

# Rounds that started per-criterion agents, from their round records' launch lines.
ROUNDS = {
    "pr71-r8": "2026-11-24", "pr71-r9": "2026-11-25", "pr71-r10": "2026-11-25", "pr71-r11": "2026-11-25",
    "pr71-r12": "2026-11-25", "pr69": "2026-11-25", "pr57-r1": "2026-11-25", "pr6502-r1": "2026-11-25",
    "pr85-r1": "2026-11-25", "sm68": "2026-11-25", "sm77-r1": "2026-11-25", "sm78-r1": "2026-11-25",
    "sm79-r1": "2026-11-25", "sm80-r1": "2026-11-25", "sm80-r2": "2026-11-25", "sm83-r1": "2026-11-25",
    "core6835-r3": "2026-11-25",
}
NOT_STEP2 = re.compile(r"refute|fixref|revertref|fixes|comment", re.I)  # a fix's, payload's or comment's refuter
DEADLINE = 5400


def events(path):
    out = []
    try:
        with open(path, errors="replace") as f:
            for line in f:
                line = line.strip()
                if not line:
                    continue
                try:
                    out.append(json.loads(line))
                except ValueError:
                    out.append({"type": "<unparsed>"})
    except OSError as e:
        out.append({"type": f"<{e.__class__.__name__}>"})
    return out


def state(folder):
    log = os.path.join(folder, "log")
    ev = events(os.path.join(log, "out.jsonl"))
    tags = []
    try:
        launch = os.path.getmtime(os.path.join(log, "BRIEF.md"))
        last_write = os.path.getmtime(os.path.join(log, "out.jsonl"))
    except OSError as e:
        return [f"<{e.__class__.__name__}>"], 0, ""
    after = last_write - launch - DEADLINE
    summary = ""
    try:
        with open(os.path.join(log, "blind-run-summary.txt"), errors="replace") as f:
            summary = f.readline().strip()
    except FileNotFoundError:
        pass
    last_type = ev[-1].get("type") if ev else None
    if summary.startswith("blind-run: deadline of") or (
            not summary.startswith("blind-run:") and last_type != "result" and 0 <= after <= 30):
        tags.append("KILL")
    elif last_type != "result" and time.time() - launch < DEADLINE + 60:
        tags.append("RUNNING")
    if not any(e.get("type") == "system" and e.get("subtype") == "init" for e in ev):
        tags.append("NOMODEL")
    report = ""
    try:
        with open(os.path.join(log, "report.md"), errors="replace") as f:
            report = f.read()
    except FileNotFoundError:
        pass
    if "RUNNING" not in tags and (not report.strip() or (len(report) < 400 and LIMIT.search(report))):
        tags.append("NOREPORT")
    m = LOAD.search(report)
    ctx = ""
    if m:
        tags.append("LOADHIT")
        ctx = report[max(0, m.start() - 70):m.end() + 50].replace("\n", " ")
    return tags, (last_write - launch) / 60, ctx


def agents(prefix, date):
    found = []
    for d in sorted(glob.glob(os.path.join(BR, f"{date}-{prefix}-*"))):
        name = os.path.basename(d)[len(date) + 1:]
        rest = name[len(prefix) + 1:]
        if NOT_STEP2.search(rest) or re.match(r"r\d+-", rest):  # not a Step 2 agent, or another round's folder
            continue
        found.append((name, d))
    return found


CONTROLS = {  # agent -> the state its round record gives it
    "sm80-r1-whole": "KILL (#80 record :20, 'hit the 5400 s deadline (exit 3)')",
    "sm80-r1-privileges": "KILL (#80 record :20)",
    "sm80-r1-behaviour": "KILL (#80 record :20)",
    "pr71-r10-balances": "not killed (#71 record :433, '48.5 min')",
    "pr57-r1-crit": "not killed (#57 record :27, '25.5 min')",
    "pr71-r9-lifecycle": "LOADHIT (its report: builds took ~10 min each under machine load)",
    "pr6502-r1-ac145": "reported, no load words (#6502 record :21)",
}

if __name__ == "__main__":
    args = sys.argv[1:]
    calibrate = "--calibrate" in args
    if "--deadline" in args:
        i = args.index("--deadline")
        DEADLINE = int(args[i + 1])
        del args[i:i + 2]
    named = [a for a in args if a != "--calibrate"]
    rounds = [(a.split(":", 1)[1], a.split(":", 1)[0]) for a in named] if named else list(ROUNDS.items())
    for prefix, date in rounds:
        rows = agents(prefix, date)
        if not calibrate:
            print(f"== {prefix} ({len(rows)} agents)" + (f" on {date}" if named else ""))
        for name, d in rows:
            tags, minutes, ctx = state(d)
            if calibrate and name not in CONTROLS:
                continue
            line = f"  {name:28s} {minutes:6.1f} min  {' '.join(tags) or '-'}"
            if calibrate:
                line += f"   <- expected {CONTROLS[name]}"
            print(line + (f"\n      load: ...{ctx}..." if ctx and not calibrate else ""))
    if calibrate:
        for name in ("2026-11-25-sm80-r1-refute", "2026-11-25-esm2022r2-wholepr"):  # not criteria rounds' agents
            tags, minutes, ctx = state(os.path.join(BR, name))
            print(f"  {name:28s} {minutes:6.1f} min  {' '.join(tags) or '-'}   <- expected NOREPORT (usage limit, per its round record)")
        # Synthetic folders for the summary-file branch, which no criteria round's agent has: each stream ends on an
        # assistant event 10 s past the deadline, the shape of a kill.
        import tempfile
        cases = [("no summary file", None, True), ("summary the agent wrote", "my notes so far\n", True),
                 ("blind-run's own line, finished", "blind-run: 9 turns, $0.50, 20.0 min\n", False),
                 ("blind-run's own line, a kill", "blind-run: deadline of 5400s reached after 9 turns\n", True)]
        with tempfile.TemporaryDirectory() as tmp:
            for label, text, want in cases:
                log = os.path.join(tmp, label.replace(" ", "_").replace("'", ""), "log")
                os.makedirs(log)
                for f, body in (("BRIEF.md", "brief"), ("report.md", "partial report"),
                                ("out.jsonl", '{"type":"system","subtype":"init"}\n{"type":"assistant"}\n')):
                    open(os.path.join(log, f), "w").write(body)
                if text is not None:
                    open(os.path.join(log, "blind-run-summary.txt"), "w").write(text)
                t0 = time.time() - 2 * DEADLINE
                os.utime(os.path.join(log, "BRIEF.md"), (t0, t0))
                os.utime(os.path.join(log, "out.jsonl"), (t0 + DEADLINE + 10, t0 + DEADLINE + 10))
                got = "KILL" in state(os.path.dirname(log))[0]
                print(f"  synthetic: {label:32s} KILL={got}   <- expected KILL={want}  {'ok' if got == want else 'WRONG'}")
