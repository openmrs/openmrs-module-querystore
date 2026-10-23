"""For every top-level transcript: first cwd, EnterWorktree/ExitWorktree use, relocated targets, last
worktree-state, current folder. Classify against two predictions:
 P-rel: current folder == slug(last relocated cwd) if any relocated record, else slug(first cwd)
 P-wt : sessions whose relocated targets include a folder != slug(first cwd) -- did they use EnterWorktree?
"""
import json, re, sys, pathlib, collections
slug = lambda p: re.sub(r"[^A-Za-z0-9]", "-", p)
root = pathlib.Path(sys.argv[1])
only = set(sys.argv[2].split(",")) if len(sys.argv) > 2 and sys.argv[2] else None
rows = []
for f in sorted(root.glob("*/*.jsonl")):
    sid = f.stem
    if only and sid not in only:
        continue
    first_cwd = None; rel = []; ws = None; ws_seen = False; enter = 0; exit_ = 0; n = 0
    with open(f, errors="replace") as fh:
        for line in fh:
            n += 1
            if first_cwd is None and '"cwd"' in line:
                try:
                    r = json.loads(line); first_cwd = r.get("cwd") or None
                except ValueError:
                    pass
            if "relocated" in line or "worktree-state" in line or "Worktree" in line:
                try:
                    r = json.loads(line)
                except ValueError:
                    continue
                t = r.get("type")
                if t == "relocated":
                    rel.append(r.get("relocatedCwd"))
                elif t == "worktree-state":
                    ws_seen = True; w = r.get("worktreeSession"); ws = (w or {}).get("worktreePath") if w else None
                m = r.get("message") or {}
                c = m.get("content")
                if isinstance(c, list):
                    for b in c:
                        if isinstance(b, dict) and b.get("type") == "tool_use":
                            if b.get("name") == "EnterWorktree": enter += 1
                            if b.get("name") == "ExitWorktree": exit_ += 1
    cur = f.parent.name
    pred = slug(rel[-1]) if rel else (slug(first_cwd) if first_cwd else None)
    other = sorted({x for x in rel if first_cwd and slug(x) != slug(first_cwd)})
    rows.append(dict(sid=sid, cur=cur, first=first_cwd, nrel=len(rel), last_rel=rel[-1] if rel else None,
                     ws_seen=ws_seen, ws=ws, enter=enter, exit=exit_, pred_ok=(pred == cur), other=other, lines=n))
tot = len(rows)
print("transcripts:", tot)
print("P-rel holds:", sum(r["pred_ok"] for r in rows), "| fails:", sum(not r["pred_ok"] for r in rows))
for r in rows:
    if not r["pred_ok"]:
        print("  P-rel FAIL", r["sid"], "cur", r["cur"], "first", r["first"], "last_rel", r["last_rel"], "enter", r["enter"])
moved = [r for r in rows if r["other"]]
print("sessions with relocated targets outside their first cwd:", len(moved))
for r in moved:
    print("  ", r["sid"], "enter", r["enter"], "exit", r["exit"], "| first", r["first"], "| others", r["other"][:3], "| last_rel", r["last_rel"], "| last ws", r["ws"] if r["ws_seen"] else "n/a", "| now", r["cur"])
ew = [r for r in rows if r["enter"]]
print("sessions calling EnterWorktree:", len(ew))
for r in ew:
    print("  ", r["sid"], "enter", r["enter"], "exit", r["exit"], "| first", r["first"], "| last_rel", r["last_rel"], "| last ws", r["ws"] if r["ws_seen"] else "n/a", "| now", r["cur"], "| P-rel", r["pred_ok"])
