#!/usr/bin/env python3
"""Mechanical self-contradiction checks over skill files.

Only the classes a script can decide. A skill contradicting itself in SUBSTANCE — "spawn four
parallel agents" beside "each agent must mutate the worktree" — is not one of them, and pretending
otherwise would report a coverage this does not have. That class is what skill-retro's refutation
pass is for; this catches the ones that are facts about the document.

Exit 1 if anything is reported, so a hook or a CI step can act on it.
"""
import json, pathlib, re, sys

WORDS = {"one":1,"two":2,"three":3,"four":4,"five":5,"six":6,"seven":7,"eight":8,"nine":9,"ten":10}
NOUNS = ("questions","things","rules","legs","conditions","shapes","items","steps","reasons",
         "kinds","guards","checks","phases","passes","outcomes","branches","sites","homes")

def stated_counts(text):
    """A sentence that says how many, immediately above a list that says otherwise.

    This is the defect this session actually shipped: "Six questions" over a list of seven, created
    by adding the seventh. Only fires when a list starts within 3 lines, so prose that merely counts
    something in passing is not flagged."""
    out, lines = [], text.split("\n")
    pat = re.compile(r"\b(" + "|".join(WORDS) + r")\s+(" + "|".join(NOUNS) + r")\b", re.I)
    for i, line in enumerate(lines):
        m = pat.search(line)
        # Only a sentence that INTRODUCES a list, i.e. ends with a colon. Without this the check
        # fires on prose that merely mentions a number ("a PR that does two things gets reviewed as
        # neither") whenever the next anti-pattern bullet happens to follow it.
        if not m or not line.rstrip().endswith(":"):
            continue
        j = i + 1
        while j < len(lines) and j <= i + 3 and not re.match(r"^\s*(\d+\.|[-*])\s", lines[j]):
            j += 1
        if j >= len(lines) or j > i + 3:
            continue
        numbered = bool(re.match(r"^\s*\d+\.\s", lines[j]))
        indent = len(lines[j]) - len(lines[j].lstrip())
        n, k = 0, j
        while k < len(lines):
            s = lines[k]
            if not s.strip():
                k += 1; continue
            cur = len(s) - len(s.lstrip())
            if cur < indent:
                break
            if cur == indent:
                if numbered and re.match(r"^\s*\d+\.\s", s):
                    n += 1
                elif not numbered and re.match(r"^\s*[-*]\s", s):
                    n += 1
                elif not re.match(r"^\s*(\d+\.|[-*])\s", s) and n:
                    break
            k += 1
        said = WORDS[m.group(1).lower()]
        if n and said != n:
            out.append((i + 1, f'says "{m.group(0)}" over a list of {n}'))
    return out

# There WAS a positional-cross-reference check here ("the bullet above"), and it is deliberately
# gone. Measured over these skills: 4 hits, and 3 were the skills QUOTING the rule against such
# references as a bad example, the fourth a past-tense narrative about one. A guard whose output is
# mostly noise gets learned-around rather than obeyed, which is worse than not having it, so this
# stays a rule for a reader and not a check for a script.

def state_fields_vs_gate(skill_dir, text):
    """A skill documenting a state field its own gate script never reads.

    Scope, stated precisely because the first version of this docstring overstated it: this catches a
    field DOCUMENTED and UNREAD. It would NOT have caught the `awaiting` gap that motivated it —
    there the field did not exist at all, so there was nothing to compare. A field that ought to
    exist and does not is invisible to any check of this kind, and is what skill-retro's refutation
    pass is for."""
    gates = list(skill_dir.glob("*gate*.sh"))
    if not gates:
        return []
    blob = "\n".join(g.read_text() for g in gates)
    # ONLY the state-file example — the block keyed by a filesystem path. Without this scoping the
    # check reads every agent-report schema in the file and reports a dozen fields no gate should
    # ever read (findings, repairs, observed, ...), which is how a check becomes noise.
    fields = set()
    for blk in re.findall(r"```json\n(.*?)```", text, re.S):
        if not re.search(r'"/[^"]*path[^"]*"\s*:', blk):
            continue
        fields |= set(re.findall(r'"([a-z_]+)"\s*:', blk))
    ignore = {"agent", "since", "id", "finding", "reason", "round"}
    # A field the skill EXPLICITLY declares as the orchestrator's own is not a gap: pr-harden says
    # "`declined` and `reviewed_shas` are the orchestrator's own ledger" and means it.
    for sent in re.split(r"(?<=[.])\s", text):
        if "ledger" in sent or "orchestrator's own" in sent or "no gate" in sent:
            ignore |= set(re.findall(r"`([a-z_]+)`", sent))
    return [(0, f'state field "{f}" is documented but no gate script reads it')
            for f in sorted(fields - ignore) if f and f'.{f}' not in blob]

def frontmatter(text, path):
    out = []
    if not text.startswith("---"):
        return [(1, "no YAML frontmatter")]
    head = text.split("---")[1]
    if not re.search(r"^version:\s*\S+", head, re.M):
        out.append((1, "no `version:` in frontmatter — the skill cannot be version-tracked"))
    if not re.search(r"^name:\s*\S+", head, re.M):
        out.append((1, "no `name:` in frontmatter"))
    return out

def main(argv):
    roots = [pathlib.Path(a) for a in argv[1:]] or [pathlib.Path.home() / ".claude/skills"]
    findings, checked = {}, 0
    for root in roots:
        for md in sorted(root.glob("*/SKILL.md")) or ([root] if root.name == "SKILL.md" else []):
            checked += 1
            text = md.read_text()
            got = (frontmatter(text, md) + stated_counts(text)
                   + state_fields_vs_gate(md.parent, text))
            if got:
                findings[str(md)] = got
    for f, items in findings.items():
        print(f"\n{f}")
        for line, msg in sorted(items):
            print(f"  {('line ' + str(line)) if line else 'file '}: {msg}")
    total = sum(len(v) for v in findings.values())
    print(f"\n{checked} skill file(s) checked, {total} finding(s).")
    print("Substantive self-contradiction is NOT checked here — see skill-retro.")
    return 1 if total else 0

if __name__ == "__main__":
    sys.exit(main(sys.argv))
