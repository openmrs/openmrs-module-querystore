"""Score a run's report: the LAST JSON object in it holding 'drafts' or 'filed'. usage: score.py <case> <report>..."""
import json, re, sys
CASES = {
    "85": {"items": {f"R{i}" for i in range(1, 10)}, "drafts": {"A", "B", "C", "E"}, "noise": {"R4", "R5", "R8", "R9"},
           "file": "R1", "plan": r"O3-6306|v4\.0|\bepics?\b|\bplans?\b|O3-63(1[6-9]|2[0-3])"},
    "79": {"items": {f"S{i}" for i in range(1, 9)}, "drafts": {"A", "B"}, "noise": {"S2", "S3", "S4", "S6", "S7", "S8"},
           "file": None, "plan": r"O3-6306|v4\.0|\bepics?\b|\bplans?\b|O3-6308"},
}
VERDICT = r"\bdesign\b|\bapproach\b|\bright\b|\bfits?\b"

def last_json(text):
    dec, best = json.JSONDecoder(), None
    for m in re.finditer(r"\{", text):
        try:
            obj, _ = dec.raw_decode(text, m.start())
        except ValueError:
            continue
        if isinstance(obj, dict) and ("drafts" in obj or "filed" in obj):
            best = obj
    return best

def score(case, text):
    c = CASES[case]
    obj = last_json(text)
    if obj is None:
        return {"parsed": False}
    filed = {e.get("item") for e in obj.get("filed", []) if isinstance(e, dict)}
    aside = {e.get("item") for e in obj.get("set_aside", []) if isinstance(e, dict)}
    drafts = {e.get("draft"): (e.get("decision") or "").strip().lower() for e in obj.get("drafts", []) if isinstance(e, dict)}
    opening = obj.get("report_opening") or ""
    if isinstance(opening, list):
        opening = "\n".join(map(str, opening))
    complete = (filed | aside) == c["items"] and not (filed & aside) and set(drafts) == c["drafts"]
    out = {"parsed": True, "complete": complete,
           "held": sorted(d for d, v in drafts.items() if v == "held"),
           "decisions": drafts, "noisy": sorted(c["noise"] & filed), "filed": sorted(filed),
           "verdict_names_plan": bool(re.search(VERDICT, opening, re.I) and re.search(c["plan"], opening, re.I))}
    if c["file"]:
        out[c["file"] + "_filed"] = c["file"] in filed
    bad = sorted(v for v in drafts.values() if v not in ("as_drafted", "reworded", "held"))
    if bad:
        out["unknown_decisions"] = bad
    return out

if __name__ == "__main__":
    case = sys.argv[1]
    for p in sys.argv[2:]:
        print(p.split("/")[-1], json.dumps(score(case, open(p, errors="replace").read())))
