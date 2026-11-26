import json, sys
names = {4687844105: "A", 4687844119: "B", 4687844127: "C", 4687844140: "E"}
rows = [json.loads(l) for l in sys.stdin if l.strip()]
assert sorted(r["id"] for r in rows) == sorted(names), [r["id"] for r in rows]
out = ["# Your own verified findings, as drafted for this round (to be filed as they stand)", ""]
for r in sorted(rows, key=lambda r: names[r["id"]]):
    out += ["## %s, on `%s` line %s" % (names[r["id"]], r["path"], r["line"]), "", r["body"], ""]
open(sys.argv[1], "w").write("\n".join(out))
print("own findings:", len(rows))
