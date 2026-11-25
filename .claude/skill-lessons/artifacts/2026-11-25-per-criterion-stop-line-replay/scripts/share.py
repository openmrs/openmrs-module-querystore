"""Per reviewed PR: the review session's own cost (main transcript + its subagents) vs its blind runs' cost (round agents,
and all blind runs incl. fix refuters), all from transcripts via tcost.cost_of (calibrated exact on 7 blind runs)."""
import glob, os, re, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tcost import cost_of
H = os.path.expanduser("~/.claude/projects")
pr = {"#57": (["3344d931"], r"pr57-"), "#68": (["ae230002"], r"sm68-"), "#69": (["5fe9b0ef"], r"pr69-"),
      "#77": (["62381ef8"], r"sm77-"), "#78": (["9b8bb37b"], r"sm78-"), "#79": (["b852290a"], r"sm79-"),
      "#80": (["6facc525", "66ae8637"], r"sm80-"), "#83": (["7a2981c8"], r"sm83-"), "#85": (["04a649fa"], r"pr85-"),
      "#71 r9-10": (["2877c081", "7268afe1"], r"pr71-r(9|10)-")}
agent_re = re.compile(r"(whole|crit|criteria|ac\d|c\d+|behaviour|privileges|priv|structure|balances|lifecycle|permissions|unchanged|fresh|existing|restart)(-work)?$")
tot = [0, 0, 0]
for label, (sids, pfx) in pr.items():
    sess = []
    for sid in sids:
        for f in glob.glob(f"{H}/*/{sid}*.jsonl") + glob.glob(f"{H}/*/{sid}*/subagents/*.jsonl"):
            sess.append(f)
    s_usd = cost_of(sess)[0]
    runs = [d for d in glob.glob(f"{H}/-Users-danielkayiwa--claude-blind-runs-2026-11-2?-*-work") if re.search(pfx, d.split("blind-runs-")[1][11:])]
    a_all = a_round = 0.0
    for d in runs:
        c = cost_of(glob.glob(f"{d}/**/*.jsonl", recursive=True))[0]; a_all += c
        name = d.split("blind-runs-")[1][11:-5]
        if agent_re.search(name) and "refute" not in name: a_round += c
    tot[0] += s_usd; tot[1] += a_round; tot[2] += a_all
    print(f"{label:10} session ${s_usd:7.2f} ({len(sess)} files) | round agents ${a_round:6.2f} | all blind runs ${a_all:6.2f} ({len(runs)}) | agents share {a_round/(s_usd+a_all):5.1%}")
print(f"TOTAL      session ${tot[0]:7.2f} | round agents ${tot[1]:6.2f} | all blind runs ${tot[2]:6.2f} | round-agent share of all {tot[1]/(tot[0]+tot[2]):5.1%}; all blind share {tot[2]/(tot[0]+tot[2]):5.1%}")
