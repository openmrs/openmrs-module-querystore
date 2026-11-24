"""Score one run's report: the LAST JSON object in it with 'filed'/'set_aside'. Prints a dict per run."""
import json, re, sys

def last_json(text):
    best = None
    for m in re.finditer(r'\{', text):
        depth, j = 0, m.start()
        for k in range(m.start(), len(text)):
            if text[k] == '{': depth += 1
            elif text[k] == '}':
                depth -= 1
                if depth == 0:
                    try:
                        obj = json.loads(text[m.start():k + 1])
                        if isinstance(obj, dict) and ("filed" in obj or "set_aside" in obj):
                            best = obj
                    except ValueError:
                        pass
                    break
    return best

def score(text):
    obj = last_json(text)
    if obj is None:
        return {"parsed": False, "R1": False, "R2": False, "noisy": False, "filed": {}, "set_aside": {}}
    filed = {e.get("item"): e.get("disposition") for e in obj.get("filed", []) if isinstance(e, dict)}
    aside = {e.get("item"): e.get("reason") for e in obj.get("set_aside", []) if isinstance(e, dict)}
    return {"parsed": True, "R1": "R1" in filed, "R2": "R2" in filed, "noisy": ("R3" in filed or "R4" in filed),
            "filed": filed, "set_aside": aside}

if __name__ == "__main__":
    for p in sys.argv[1:]:
        print(p.split("/")[-3] if "/log/" in p else p, json.dumps(score(open(p, errors="replace").read())))
