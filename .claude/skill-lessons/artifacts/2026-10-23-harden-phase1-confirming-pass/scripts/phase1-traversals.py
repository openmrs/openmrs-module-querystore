#!/usr/bin/env python3
"""Splits each harden run in a session transcript into its Phase 1 traversals and measures, for each,
what the orchestrator did between its last fix and the write that recorded Phase 1 converged — and
whether the Phase 2 pass that followed escalated.

Usage: phase1-traversals.py <transcript.jsonl>...

A traversal runs from a `phase1 open` gate write (or the harden load) to the next `phase1 converged`
write. Its outcome is ESCALATED when a later `phase1 open` write for a higher cycle comes before any
`phase2 done`, DONE when the converged write itself, or the next gate write, carries `phase2 done`,
and UNKNOWN otherwise (the transcript ends, or the next write says neither).

The last fix is the latest of: an Edit/Write/MultiEdit/NotebookEdit outside the scratchpad and /tmp,
a Bash call whose description says it applies, edits, replaces, rewrites, patches, inserts, renames
or fixes something, and a git commit. After it, the row counts:
  rd  reads of source: Read on a non-scratch path, or a read-only Bash call (git diff/show, grep,
      sed -n, cat, head, tail) naming a path under src/ or a source extension and no probe log
  ag  Agent spawns that are not Phase 2 lenses (a Phase 1 review delegated to an agent)
  bash  other Bash calls (builds, probes, tests)
  min   minutes from the last fix to the converged write
Only the main thread (isSidechain entries are a subagent's) and each tool_use id once. Gate writes
are gate-state `harden-set` invocations; a grep or a script that merely names `harden-set` is not one.
Older runs that wrote harden-state.json by hand are reported as such and not split.
"""
import datetime
import json
import re
import sys

SCRATCH = re.compile(r"^(/private)?/tmp/|/scratchpad/|/\.claude/(skills|pipeline|hooks|skill-lessons)/")
LENS = re.compile(r"phase 2|phase2|\b(reuse|quality|efficiency|integration)\b", re.I)
COMMIT = re.compile(r"\bgit\s+(?:-C\s+\S+\s+)*commit\b")
GATE = re.compile(r"gate-state\b[^|;&\n]*?\bharden-set\b([^|;&\n]*)")
FIXWORD = re.compile(r"\b(apply|applies|edit|edits|replace|replaces|rewrite|rewrites|patch|patches|insert|"
                     r"inserts|rename|renames|fix|fixes)\b", re.I)
# A Bash call that changes a tracked file: an in-place sed/perl, a Python write, `git apply`, or a
# redirect into a source-like path outside /tmp, the scratchpad and target/.
WRITES_TREE = re.compile(r"\b(sed|perl)\s+-[a-zA-Z]*i|\.write_text\(|open\([^)]*['\"][wa]['\"]|\bgit\s+apply\b|"
                         r"\bpatch\s+-p|(?<![2&])>>?\s*(?!/dev/|/tmp/|/private/tmp/|\S*scratchpad|\S*target/)"
                         r"\S+\.(java|py|md|xml|ts|tsx|js|properties|json|ya?ml|sh)\b")
READ_CMD = re.compile(r"^\s*(git\s+(-C\s+\S+\s+)?(diff|show)\b|grep\b|rg\b|sed\s+-n\b|cat\b|head\b|tail\b|awk\b)")
SOURCE = re.compile(r"(src/|\.(java|py|ts|tsx|js|xml|md|sh|properties|json)\b)")
PROBE_LOG = re.compile(r"\.log\b|/probe|probe-|surefire-reports|target/")


def parse_ts(ts):
    try:
        return datetime.datetime.fromisoformat(ts.replace("Z", "+00:00"))
    except ValueError:
        return None


def events(path):
    seen = set()
    with open(path, encoding="utf-8", errors="replace") as f:
        for lineno, raw in enumerate(f, 1):
            try:
                entry = json.loads(raw)
            except json.JSONDecodeError:
                continue
            if entry.get("isSidechain") or entry.get("type") != "assistant":
                continue
            ts = parse_ts(entry.get("timestamp", ""))
            content = (entry.get("message") or {}).get("content") or []
            if not isinstance(content, list) or ts is None:
                continue
            for block in content:
                if not isinstance(block, dict) or block.get("type") != "tool_use":
                    continue
                tid = block.get("id")
                if tid in seen:
                    continue
                seen.add(tid)
                name, inp = block.get("name", ""), block.get("input") or {}
                if name == "Skill":
                    if inp.get("skill") == "harden":
                        yield lineno, ts, "HARDEN", {}
                    continue
                if name in ("Edit", "Write", "NotebookEdit", "MultiEdit"):
                    fp = inp.get("file_path") or inp.get("notebook_path") or ""
                    if not SCRATCH.search(fp):
                        yield lineno, ts, "FIX", {"how": "tool", "what": fp}
                    continue
                if name == "Read":
                    fp = inp.get("file_path", "")
                    if not SCRATCH.search(fp):
                        yield lineno, ts, "READ", {"what": fp}
                    continue
                if name in ("Grep", "Glob"):
                    yield lineno, ts, "READ", {"what": inp.get("pattern", "")}
                    continue
                if name in ("Agent", "Task"):
                    desc = inp.get("description", "")
                    yield lineno, ts, "P2" if LENS.search(desc) else "AGENT", {"what": desc}
                    continue
                if name != "Bash":
                    continue
                cmd, desc = inp.get("command", ""), inp.get("description", "") or ""
                gates = GATE.findall(cmd)
                if gates:
                    args = " ".join(gates)
                    st = {}
                    for flag in ("cycle", "phase1", "phase2"):
                        vals = re.findall(r"--" + flag + r"\s+(\S+)", args)
                        if vals:
                            st[flag] = vals[-1]
                    yield lineno, ts, "GATE", st
                    if COMMIT.search(cmd):
                        yield lineno, ts, "FIX", {"how": "commit", "what": desc}
                    continue
                if "harden-state.json" in cmd and re.search(r"converged", cmd) and not READ_CMD.match(cmd):
                    yield lineno, ts, "LEGACY", {"what": desc}
                    continue
                if COMMIT.search(cmd):
                    yield lineno, ts, "FIX", {"how": "commit", "what": desc}
                    continue
                probe = re.search(r"\b(probe|probes|mutat\w*|restore\w*)\b", desc, re.I) or \
                    re.search(r"\bgit\s+(?:-C\s+\S+\s+)*(checkout\s+--|restore)\b", cmd)
                if not probe and (FIXWORD.search(desc) or WRITES_TREE.search(cmd)):
                    yield lineno, ts, "FIX", {"how": "script", "what": desc}
                    continue
                first = cmd.strip().splitlines()[0] if cmd.strip() else ""
                if READ_CMD.match(first) and SOURCE.search(cmd) and not PROBE_LOG.search(cmd):
                    yield lineno, ts, "READ", {"what": first[:80]}
                else:
                    yield lineno, ts, "BASH", {"what": desc or first[:80]}


def traversals(path):
    evs = list(events(path))
    rows, legacy = [], sum(1 for e in evs if e[2] == "LEGACY")
    harden_at = next((i for i, e in enumerate(evs) if e[2] == "HARDEN"), None)
    if harden_at is None:
        return rows, legacy, False
    # A traversal opens at the harden load or at the first `phase1 open` write after a converged
    # write; a repeated open write inside one traversal (cycle 1 of a run writes several) keeps it.
    start, in_traversal = harden_at, True
    gates = [i for i, e in enumerate(evs) if e[2] == "GATE" and i > harden_at]
    for gi in gates:
        st = evs[gi][3]
        if st.get("phase1") == "open":
            if not in_traversal:
                start, in_traversal = gi, True
            continue
        if st.get("phase1") != "converged":
            continue
        window = evs[start:gi]
        # Passes the orchestrator recorded: per harden's write protocol, a `phase1 open` after a pass
        # that found something and `converged` after the pass that found nothing. An open write that
        # starts the traversal (an escalation) is not a pass, and an open with no other event since
        # the previous gate write is a duplicate write, not a new pass.
        opens_inside, prev_gate, last_open = 0, start, start
        for k in range(start + 1, gi):
            if evs[k][2] != "GATE":
                continue
            if evs[k][3].get("phase1") == "open" and any(evs[x][2] != "GATE" for x in range(prev_gate + 1, k)):
                opens_inside += 1
            if evs[k][3].get("phase1") == "open":
                last_open = k
            prev_gate = k
        # The final pass: from the last open write (or the traversal's start) to the converged write.
        # It is the pass the converged write says found nothing.
        final = evs[last_open + 1:gi]
        # A commit changes no file, and a run often chains it onto the open write, so only edits count.
        final_fix = sum(1 for e in final if e[2] == "FIX" and e[3]["how"] != "commit")
        # After the last edit since the previous converged write (or the harden load): a resumed
        # cycle's fixes can land before its open write, while Phase 2's findings are being applied.
        prev_conv = max([g for g in gates if g < gi and evs[g][3].get("phase1") == "converged"] + [harden_at])
        edits_since = [i for i in range(prev_conv + 1, gi) if evs[i][2] == "FIX" and evs[i][3]["how"] != "commit"]
        if edits_since:
            le = edits_since[-1]
            post = evs[le + 1:gi]
            post_rd = sum(1 for e in post if e[2] == "READ")
            post_open = sum(1 for e in post if e[2] == "GATE" and e[3].get("phase1") == "open")
            post_min = (evs[gi][1] - evs[le][1]).total_seconds() / 60
        else:
            post_rd = post_open = 0
            post_min = None
        final_rd = sum(1 for e in final if e[2] == "READ")
        final_min = (evs[gi][1] - evs[last_open][1]).total_seconds() / 60
        fixes = [i for i in range(start, gi) if evs[i][2] == "FIX"]
        last_fix = fixes[-1] if fixes else None
        after = evs[last_fix + 1:gi] if last_fix is not None else []
        rd = sum(1 for e in after if e[2] == "READ")
        ag = sum(1 for e in after if e[2] == "AGENT")
        bash = sum(1 for e in after if e[2] == "BASH")
        mins = (evs[gi][1] - evs[last_fix][1]).total_seconds() / 60 if last_fix is not None else None
        # The same, from the last EDIT (a tool or scripted change, never a commit): harden commits a
        # cycle's work before Phase 2, so a confirming pass sits between the last edit and the commit.
        edits = [i for i in fixes if evs[i][3]["how"] != "commit"]
        last_edit = edits[-1] if edits else None
        after_e = evs[last_edit + 1:gi] if last_edit is not None else []
        rd_e = sum(1 for e in after_e if e[2] == "READ")
        ag_e = sum(1 for e in after_e if e[2] == "AGENT")
        mins_e = (evs[gi][1] - evs[last_edit][1]).total_seconds() / 60 if last_edit is not None else None
        # outcome: this write or the next gate write with phase2 done -> DONE; a later open of a
        # higher cycle before any done -> ESCALATED
        outcome = "DONE" if st.get("phase2") == "done" else "UNKNOWN"
        if outcome != "DONE":
            cyc = int(st["cycle"]) if st.get("cycle", "").isdigit() else None
            for gj in gates:
                if gj <= gi:
                    continue
                s2 = evs[gj][3]
                if s2.get("phase2") == "done":
                    outcome = "DONE"
                    break
                if s2.get("phase1") == "open" and cyc is not None and s2.get("cycle", "").isdigit() \
                        and int(s2["cycle"]) > cyc:
                    outcome = "ESCALATED"
                    break
        terminal = st.get("phase2") == "done"
        rows.append({
            "line": evs[gi][0], "cycle": st.get("cycle", "?"), "terminal": terminal,
            "fixes": len(fixes), "commits": sum(1 for i in fixes if evs[i][3]["how"] == "commit"),
            "rd": rd, "ag": ag, "bash": bash, "min": mins, "outcome": outcome,
            "rd_e": rd_e, "ag_e": ag_e, "min_e": mins_e, "edit_line": evs[last_edit][0] if last_edit is not None else None,
            "passes": opens_inside + 1, "start_line": evs[start][0],
            "final_fix": final_fix, "final_rd": final_rd, "final_min": final_min,
            "n_edits": len(edits_since), "post_rd": post_rd, "post_open": post_open, "post_min": post_min,
            "post_from": evs[edits_since[-1]][0] if edits_since else evs[prev_conv][0],
            "start_kind": evs[start][2] if evs[start][2] != "GATE" else "OPEN" if evs[start][3].get("phase1") == "open"
            else "CONVERGED" if evs[start][3].get("phase1") == "converged" else "GATE",
            "span": (evs[gi][1] - evs[start][1]).total_seconds() / 60,
        })
        start, in_traversal = gi, False
    return rows, legacy, True


for path in sys.argv[1:]:
    rows, legacy, has_harden = traversals(path)
    name = path.rsplit("/", 1)[-1]
    if not has_harden:
        print(f"{name}: no harden load")
        continue
    if legacy and not rows:
        print(f"{name}: {legacy} hand-written harden-state writes, no gate-state traversals")
        continue
    for r in rows:
        m = "  -  " if r["min"] is None else f"{r['min']:5.1f}"
        me = "  -  " if r["min_e"] is None else f"{r['min_e']:5.1f}"
        print(f"{name} L{r['line']:<6} c{r['cycle']:<3} {'T' if r['terminal'] else ' '} span={r['span']:5.1f} "
              f"fixes={r['fixes']:<3} commits={r['commits']:<2} after-last-fix: rd={r['rd']:<3} ag={r['ag']} "
              f"bash={r['bash']:<3} min={m} next={r['outcome']} "
              f"after-last-edit(L{r['edit_line']}): rd={r['rd_e']:<3} ag={r['ag_e']} min={me} "
              f"passes={r['passes']} from=L{r['start_line']}:{r['start_kind']} "
              f"final: fix={r['final_fix']} rd={r['final_rd']} min={r['final_min']:.1f} "
              f"post-edit: edits={r['n_edits']} rd={r['post_rd']} open={r['post_open']} "
              f"min={'-' if r['post_min'] is None else format(r['post_min'], '.1f')} post-from=L{r['post_from']}")
