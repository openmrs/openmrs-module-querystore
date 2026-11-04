"""K1 candidate scan. Mode `calibrate`: run the pattern over fixed known-hit / known-miss texts.
Mode `scan <R>`: pool every run's findings, flag candidates, shuffle with run and arm stripped, write the
blind list to <R>/adj/candidates.json and the private map to <R>/adj/map-private.json; also list
mentions outside findings per run (recorded, not decisive)."""
import json, re, sys, os, random
NAME = r"(getName\s*\(\s*\)|\bname\b|\baction\b)"
ABSENT = r"(\bnull\b|missing|absent|omit|without|no\s+name|unset|not\s+(?:set|provided|supplied|sent|given))"
PAT = re.compile(rf"({NAME}[^.;\n]{{0,80}}{ABSENT}|{ABSENT}[^.;\n]{{0,80}}{NAME}|switch\s*\(\s*(?:delegate\.)?getName|switch\s*\(\s*null)", re.I)
def hit(text): return bool(PAT.search(text or ""))
def load(p):
    t = open(p).read()
    try: return json.loads(t)
    except Exception: return json.loads(re.search(r"\{[\s\S]*\}", t).group(0))
if sys.argv[1] == "calibrate":
    known_hits = ["I think we should also add a null name guard here. Currently a `POST` with a valid UUID and no name passes the new guard, runs the lookup, then hits `switch (null)`.",
                  "Reject stock actions without an action name"]
    known_misses = ["The omod's openmrs-test dependency resolves mockito-core 5.6.0 and ByteBuddy 1.14.8 on JDK 21.",
                    "A blank UUID is rejected with 400 before the lookup, and an unknown UUID returns 404.",
                    "Wiring up the module's unused StockOperationActionValidator would also start requiring a reason for REJECT, RETURN and CANCEL."]
    for t in known_hits: print("HIT " if hit(t) else "MISS", "| known hit  |", t[:70])
    for t in known_misses: print("HIT " if hit(t) else "MISS", "| known miss |", t[:70])
    sys.exit(0)
R = sys.argv[2]; os.makedirs(f"{R}/adj", exist_ok=True)
rows, outside = [], {}
for f in sorted(os.listdir(f"{R}/out")):
    if not f.endswith(".json"): continue
    wt = f[:-5]; d = load(f"{R}/out/{f}")
    for x in d.get("findings") or []:
        text = " | ".join(str(x.get(k) or "") for k in ("claim", "failure_mode", "evidence"))
        if hit(text): rows.append((wt, x.get("id"), {"disposition": x.get("disposition"), "file": x.get("file"), "line": x.get("line"), "claim": x.get("claim"), "failure_mode": x.get("failure_mode"), "evidence": x.get("evidence")}))
    outside[wt] = [k for k in ("notes", "what_i_swept", "could_not_verify", "verdict") if hit(json.dumps(d.get(k)))]
random.SystemRandom().shuffle(rows)
blind = [{"key": f"K{i:02d}", **body} for i, (wt, rid, body) in enumerate(rows, 1)]
priv = {f"K{i:02d}": {"wt": wt, "id": rid} for i, (wt, rid, body) in enumerate(rows, 1)}
json.dump(blind, open(f"{R}/adj/candidates.json", "w"), indent=1)
json.dump({"map": priv, "outside_findings": outside}, open(f"{R}/adj/map-private.json", "w"), indent=1)
print(f"{len(blind)} candidate findings written to adj/candidates.json (blind)")
