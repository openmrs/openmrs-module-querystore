"""Score a run's report: the LAST JSON object in it with 'filed'/'set_aside' (reviewer) or 'upheld'/'rejected' (reader)."""
import json, re, sys
NOISE = {"R6", "R7", "R8"}
ITEMS = {"R%d" % i for i in range(1, 9)}

def last_json(text, keys):
    """The last JSON object in text holding any of keys, decoded with json's own parser (braces inside strings are safe)."""
    dec, best = json.JSONDecoder(), None
    for m in re.finditer(r"\{", text):
        try:
            obj, _ = dec.raw_decode(text, m.start())
        except ValueError:
            continue
        if isinstance(obj, dict) and any(key in obj for key in keys):
            best = obj
    return best

def score(text):
    obj = last_json(text, ("filed", "set_aside", "upheld", "rejected"))
    if obj is None:
        return {"parsed": False}
    reader = "upheld" in obj or "rejected" in obj
    yes_key, no_key = ("rejected", "upheld") if reader else ("filed", "set_aside")
    yes = {e.get("item"): e.get("disposition", e.get("why")) for e in obj.get(yes_key, []) if isinstance(e, dict)}
    no = {e.get("item"): e.get("reason", e.get("why")) for e in obj.get(no_key, []) if isinstance(e, dict)}
    complete = set(yes) | set(no) == ITEMS and not (set(yes) & set(no))
    return {"parsed": True, "kind": "reader" if reader else "reviewer", "complete": complete,
            "R1": "R1" in yes, "noisy": bool(NOISE & set(yes)), "yes": sorted(yes), "dispositions": yes}

if __name__ == "__main__":
    for p in sys.argv[1:]:
        print(p, json.dumps(score(open(p, errors="replace").read())))
