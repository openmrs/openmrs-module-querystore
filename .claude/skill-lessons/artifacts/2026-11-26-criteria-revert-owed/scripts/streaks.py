"""Read criteria-rounds.tsv and report, for 0.39.0's REVERT ON (REJECTED.md :7581-7583) as written and as amended on
2026-11-26, the longest run of consecutive counting rounds and whether it is met (three or more rounds on two or more
PRs). The REVERT ON names no order for rounds that overlap, so both orders are reported: by the rounds' first agent
launch, and by their agents' last write.

  as written: a round counts when no agent's report carried a defect the review counted (defect_by "-"); otherwise
              it breaks the run.
  amended:    a round with such a defect breaks the run, owed agents or not; otherwise a round with an owed agent
              neither counts nor breaks; otherwise it counts.

Usage: streaks.py <criteria-rounds.tsv> | --self-test"""
import csv, sys


def classify(row, amended):
    if row["defect_by"] != "-":
        return "breaks"
    if amended and row["owed"] != "-":
        return "neither"
    return "counts"


def runs(rows, amended):
    """Every maximal run of consecutive counting rounds; a round that neither counts nor breaks is skipped."""
    out, run = [], []
    for r in rows:
        c = classify(r, amended)
        if c == "counts":
            run.append(r)
        elif c == "breaks":
            if run:
                out.append(run)
            run = []
    if run:
        out.append(run)
    return out


def met(run):
    # Three or more counting rounds on two or more PRs: such a run always holds three consecutive rounds on two PRs.
    return len(run) >= 3 and len({r["pr"] for r in run}) >= 2


def longest(rows, amended):
    """The first run that meets the REVERT ON if one does, else the longest run."""
    rs = runs(rows, amended)
    for run in rs:
        if met(run):
            return run
    return max(rs, key=len, default=[])


def report(rows):
    for order, key in (("first agent launch", "launch"), ("agents' last write", "last_write")):
        ordered = sorted(rows, key=lambda r: (r[key], r["round"]))
        print(f"== ordered by {order}")
        print("   " + " ".join(f"{r['round']}:{classify(r, False)[0]}{classify(r, True)[0]}" for r in ordered))
        for amended in (False, True):
            run = longest(ordered, amended)
            print(f"   {'amended   ' if amended else 'as written'}: {'met by run' if met(run) else 'longest run'} {len(run)} "
                  f"({', '.join(r['round'] for r in run) or 'none'}) on {len({r['pr'] for r in run})} PR(s) -> "
                  f"{'MET' if met(run) else 'not met'}")
    owed = [r for r in rows if r["owed"] != "-"]
    print(f"rounds: {len(rows)}; with an owed agent: {len(owed)}; of those, broken by a defect an agent reported: "
          f"{sum(1 for r in owed if r['defect_by'] != '-')}; counted as written and neither under the amendment: "
          f"{', '.join(r['round'] for r in owed if r['defect_by'] == '-')}")
    kills = sum(a.endswith(":K") for r in rows for a in r["owed"].split(","))
    print(f"owed agents: {sum(len(r['owed'].split(',')) for r in owed)} ({kills} by a deadline kill)")


def self_test():
    def R(name, pr, owed="-", defect="-", t="00"):
        return {"round": name, "pr": pr, "owed": owed, "defect_by": defect, "launch": t, "last_write": t}
    cases = [
        ("three counting rounds on two PRs", [R("a", "A"), R("b", "A"), R("c", "B")], False, 3, True),
        ("three counting rounds on one PR", [R("a", "A"), R("b", "A"), R("c", "A")], False, 3, False),
        ("a break ends the run", [R("a", "A"), R("b", "B", defect="x"), R("c", "C"), R("d", "D")], False, 2, False),
        ("as written, an owed round counts", [R("a", "A"), R("b", "B", owed="w:K"), R("c", "C")], False, 3, True),
        ("amended, an owed round neither counts nor breaks", [R("a", "A"), R("b", "B", owed="w:K"), R("c", "C"),
                                                              R("d", "D")], True, 3, True),
        ("amended, an owed round that found a defect breaks", [R("a", "A"), R("b", "B", owed="w:K", defect="w"),
                                                               R("c", "C"), R("d", "D")], True, 2, False),
        ("amended, owed rounds alone are no run", [R("a", "A", owed="w:R"), R("b", "B", owed="w:K"),
                                                   R("c", "C", owed="w:N")], True, 0, False),
        ("a met run after a longer one-PR run", [R("a", "X"), R("b", "X"), R("c", "X"), R("d", "X"),
                                                 R("e", "Y", defect="x"), R("f", "F"), R("g", "G"), R("h", "H")],
         False, 3, True),
    ]
    bad = 0
    for name, rows, amended, n, want in cases:
        run = longest(rows, amended)
        ok = len(run) == n and met(run) == want
        bad += not ok
        print(f"{'ok  ' if ok else 'FAIL'} {name}: run {len(run)}, {'met' if met(run) else 'not met'}")
    print(f"{len(cases) - bad}/{len(cases)} passed")
    return 1 if bad else 0


if __name__ == "__main__":
    if sys.argv[1:] == ["--self-test"]:
        sys.exit(self_test())
    with open(sys.argv[1]) as f:
        rows = list(csv.DictReader((line for line in f if not line.startswith("#")), delimiter="\t"))
    report(rows)
