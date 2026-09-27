#!/usr/bin/env python3
"""Mechanical checks over skill files: the self-contradictions a script can decide, and a size
budget. With `--against <ref>`, run inside the source repo, also that no budget rose since <ref>
without a matching entry in the budget file's `raises`.

Only the classes a script can decide. A skill contradicting itself in SUBSTANCE — "spawn four
parallel agents" beside "each agent must mutate the worktree" — is not one of them, and pretending
otherwise would report a coverage this does not have. That class is what skill-retro's refutation
pass is for; this catches the ones that are facts about the document.

Exit 1 if anything is reported, so a hook or a CI step can act on it, and 2 if no skill file was
found to check, because a check that ran on nothing must not read as green.
"""
import json, pathlib, re, subprocess, sys

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

# Beside this script, not beside the skills it checks, so a run over any root reads one table.
BUDGETS = pathlib.Path(__file__).resolve().parent / "skill-budgets.json"

def words(text):
    """What a size budget counts: words of the whole file, frontmatter included, split on ASCII
    whitespace only, which is exactly what `LC_ALL=C wc -w` counts, so a finding can be checked
    without this script. `str.split()` would also split on Unicode spaces, and a bare `wc -w` under
    a UTF-8 locale splits some emoji into extra words. Words and not lines, because some of these
    files hold a paragraph per line, and a line count says nothing about their length."""
    return len(text.encode().split())

def load_budgets():
    """The table, or why there is none. A missing or unreadable table becomes a finding on every
    skill instead of a traceback, so the other checks still run."""
    try:
        table = json.loads(BUDGETS.read_text())["words"]
        if not all(type(v) is int for v in table.values()):
            raise TypeError("every budget must be an integer word count")
        return table, None
    except FileNotFoundError:
        return None, f"no {BUDGETS.name} beside this script"
    except (OSError, ValueError, KeyError, TypeError, AttributeError) as e:
        return None, f"unreadable {BUDGETS.name} ({type(e).__name__}: {e})"

def size_budget(skill, text, budgets, why_none, named="its directory name"):
    """A SKILL.md, or a file beside it, whose size differs from its budget, or that has no budget at all.

    Its body is loaded into every run of its skill and its description into every session, and
    skill-retro Step 4 asks what every addition prunes, and a sentence for net growth; this is the
    half of that a script can decide. A budget is the skill's size, written down. Growth needs a
    raise, made in the commit that needs it with Step 4's sentence recorded in `raises` (checked by
    `budget_ratchet` under `--against`), and a prune lowers it in the same commit, so no room is left
    over for a later pass to grow into without writing anything down."""
    n = words(text)
    if budgets is None:
        return [(0, f"{why_none}, so no size is checked ({n} words)")]
    if skill not in budgets:
        return [(0, f"no size budget for {skill!r} ({named}) in {BUDGETS.name}: "
                    f"add one at its {n} words")]
    cap = budgets[skill]
    if n > cap:
        return [(0, f"{n} words, over its budget of {cap} in {BUDGETS.name}: prune {n - cap}, "
                    f"or raise the budget and say why (skill-retro Step 4)")]
    if n < cap:
        return [(0, f"{n} words, under its budget of {cap} in {BUDGETS.name}: lower it to {n} "
                    f"in the same commit")]
    return []

def role_files(skill_dir):
    """The other instruction files beside SKILL.md, a subagent's role file or a reference a session
    reads when a step sends it there: every top-level `*.md` but `evidence.md`, which no run loads. Budgeted like SKILL.md, keyed `<skill>/<file>`, because a
    rule moved into one leaves SKILL.md's budget without leaving the skill, and an unbudgeted file is
    where the next addition would go unmeasured. A file in a subdirectory is not one of these."""
    return sorted(p for p in skill_dir.glob("*.md") if p.name not in ("SKILL.md", "evidence.md"))

def budget_ratchet(ref):
    """A budget that rose since `ref` without a matching `raises` entry.

    skill-retro Step 4 lets a budget rise only with a sentence saying why, and a sentence in a report
    is what a later reader never sees; the entry puts it beside the number it justifies. It must name
    the exact rise — `{"from": <budget at ref>, "to": <budget now>, "why": "…"}` — so it covers that
    rise and no other. What it cannot see: a budget pruned and later restored to the same two numbers
    is covered by the same entry again, and a second rise of one key replaces the first entry, whose
    reason then lives only in git history. A key new since `ref` rises from 0; entries that match no
    rise are ignored. The table at `ref` comes from git — see `budgets_at` — and a ref it cannot read
    is a finding, never a pass."""
    try:
        cur = json.loads(BUDGETS.read_text())
    except (OSError, ValueError) as e:
        return [(0, f"unreadable {BUDGETS.name} ({type(e).__name__}), so no rise since {ref} is checked")]
    committed, why = budgets_at(ref)
    if committed is None:
        return [(0, f"cannot read {BUDGETS.name} at {ref}: {why}")]
    try:
        was_table = json.loads(committed)["words"]
    except (ValueError, KeyError, TypeError):
        return [(0, f"{BUDGETS.name} at {ref} has no readable `words` table")]
    raises = cur.get("raises") or {}
    out = []
    for key, n in sorted((cur.get("words") or {}).items()):
        was = was_table.get(key, 0)
        if type(n) is not int or n <= was:
            continue
        e = raises.get(key) if isinstance(raises.get(key), dict) else {}
        if e.get("from") == was and e.get("to") == n and str(e.get("why", "")).strip():
            continue
        out.append((0, f"{key}: budget rose from {was} to {n} since {ref} with no matching entry in "
                       f'`raises` ({{"from": {was}, "to": {n}, "why": "…"}}), which skill-retro Step 4 asks for'))
    return out

def budgets_at(ref):
    """The table as committed at `ref`: through the checkout holding this script when it is one (the
    source repo's own copy), else through the checkout the command runs in, at the table's path there.
    The second is the retro's case — it runs the live copy under ~/.claude, which is in no checkout,
    from the source repo — and without it `--against` could only ever report that git failed."""
    tries = [["git", "-C", str(BUDGETS.parent), "show", f"{ref}:./{BUDGETS.name}"],
             ["git", "show", f"{ref}:.claude/skills/skill-retro/{BUDGETS.name}"]]
    errs = []
    for cmd in tries:
        r = subprocess.run(cmd, capture_output=True, text=True)
        if r.returncode == 0:
            return r.stdout, None
        errs.append(r.stderr.strip())
    return None, "; ".join(e for e in errs if e) or "git failed"

def main(argv):
    args, against, it = [], None, iter(argv[1:])
    for a in it:
        if a == "--against":
            against = next(it, None)
            if not against:
                print("--against needs a git ref")
                return 2
        elif a.startswith("--against="):
            against = a.split("=", 1)[1]
            if not against:
                print("--against needs a git ref")
                return 2
        elif a.startswith("-"):
            print(f"unknown option {a}: the arguments are skill roots and `--against <ref>`")
            return 2
        else:
            args.append(a)
    roots = [pathlib.Path(a) for a in args] or [pathlib.Path.home() / ".claude/skills"]
    findings, checked = {}, 0
    budgets, why_none = load_budgets()
    for root in roots:
        single = [root] if root.name == "SKILL.md" and root.is_file() else []
        for md in sorted(root.glob("*/SKILL.md")) or single:
            checked += 1
            text = md.read_text()
            got = (frontmatter(text, md) + stated_counts(text)
                   + state_fields_vs_gate(md.parent, text)
                   + size_budget(md.resolve().parent.name, text, budgets, why_none))
            if got:
                findings[str(md)] = got
            for role in role_files(md.parent):
                checked += 1
                text = role.read_text()
                got = stated_counts(text) + size_budget(
                    f"{md.resolve().parent.name}/{role.name}", text, budgets, why_none,
                    named="its skill's directory name, a slash, and its file name")
                if got:
                    findings[str(role)] = got
    if not checked:
        print(f"no SKILL.md found under {', '.join(map(str, roots))}: nothing was checked")
        return 2
    if against:
        got = budget_ratchet(against)
        if got:
            findings[str(BUDGETS)] = got
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
