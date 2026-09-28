#!/usr/bin/env python3
"""role-file-check — did pr-harden 0.36's role-file move work?

pr-harden 0.36.0 moved the verifier's procedure and the fixer's brief out of SKILL.md into
`verifier.md` and `fixer.md`, which the orchestrator tells each subagent to read first. This reads
session transcripts and reports, for every fixer and verifier spawned after pr-harden was loaded:

  path   the brief names the role file's absolute path under the base directory the session printed
         for pr-harden
  read   the subagent's reads of that file covered every line of it before it acted. A read is a
         Read (its range, and the file's length as the transcript recorded it — else, for a Bash-only
         read, the length on disk now), or a Bash segment
         that shows the file — `cat`, `sed -n 'A,Bp'`, `head -N`, `tail -n +K`, `awk 'NR>=A && NR<=B'`
         — under any spelling of the path (absolute, `~`, `$HOME`, or relative after a `cd`).
         A fixer acts at its first edit: an Edit/Write outside the temp directory, a Bash command
         that writes one, or running or importing a scratch script that does. A verifier acts at its
         first Bash command that is not a read of the file, or its first edit, since its brief says to
         read the file "before it does anything". A read counts only if it was sent in an EARLIER
         message than the act: calls sent together in one message run before any result is seen.
  items  what the pointer says the brief carries; reported, never gating

Each orchestrator `Agent` call is joined to its subagent's transcript through
`<session>/subagents/agent-*.meta.json`'s `toolUseId`. A spawn is a fixer, a verifier, a reviewer or
harden's own agent by its description, and failing that a fixer or a verifier by the one role its
brief states — never a reviewer by its brief, and never by the role file's mention, because a brief
that fails to name the file is the case being looked for. A spawn it cannot place is listed, among
them a brief naming the reviewer or naming two roles.

Only TREATED sessions — whose loaded pr-harden text carries the 0.36 pointer — count toward the bar,
which `.claude/skill-lessons/proposals/2026-09-27-role-file-measurement.md` fixed before any treated
session existed. The bar decides only what a transcript settles:
  FAIL   a treated brief does not name the file, or the file is never read whole
  PASS   3 treated sessions with a spawn, every read CLEAN — whole before the subagent's first call
         that is not a pure read, so nothing can have acted first
  HAND   a whole read that was not CLEAN — not before the message holding the first call that is not
         a pure read, as in `cat fixer.md; grep … SKILL.md`, whose second segment reads another
         file; the act detector's verdict is shown as advice, because deciding from shell text
         whether an agent had already edited proved open-ended over five reviews
Sessions are ordered by their first timestamp, never by path.

    role-file-check.py [SESSION.jsonl ...]     # default: every session under ~/.claude/projects
    role-file-check.py --json [SESSION.jsonl ...]
    role-file-check.py --baseline [--before YYYY-MM-DD | --after YYYY-MM-DD] [--records DIR]
    role-file-check.py --second-move [--json] [SESSION.jsonl ...]
    role-file-check.py --second-move --baseline [--before YYYY-MM-DD | --after YYYY-MM-DD]
    role-file-check.py --selftest

`--second-move` applies the same checks to the second move — pr-harden 0.37.0's `reviewer.md` and
resolve-ticket 0.23.0's `refuter.md` — against its own bar, pre-registered in
`.claude/skill-lessons/proposals/2026-09-28-reviewer-refuter-measurement.md`, and leaves the first
move's report as it was. A reviewer is looked for after pr-harden loads, a refuter in Step 3's window:
after resolve-ticket loads and before harden or pr-harden does.
"""
import argparse, contextlib, io, json, os, re, shlex, sys, tempfile
from pathlib import Path

PROJECTS = Path.home() / ".claude/projects"
RECORDS = Path.home() / ".claude/skill-lessons"
BASE = "Base directory for this skill: "
POINTERS = ("`fixer.md` in this skill's directory", "`verifier.md` in this skill's directory")
EDITS = {"Edit", "Write", "MultiEdit", "NotebookEdit"}
ROLE_FILE = {"fixer": "fixer.md", "verifier": "verifier.md"}
ROLE_WORD = re.compile(r"(?<!-)\b(verifier|fixer|reviewer)\b(?!-|['’]s\b|\.md\b)", re.I | re.A)
HARDEN_WORDS = r"(?<!pr-)\bharden\b|\bcycle\b|\bphase\b"
REFUT = r"\brefut(?!er\.md\b)"
# A JIRA key, "O3-1234" or "TRUNK-6429", and not "UTF-8".
JIRA = r"\b(?!UTF-)[A-Z][A-Z0-9]+-\d+\b"
# The second move, one entry per role: the skill that loads its file, the file, and the pointer text
# whose presence in that skill's loaded text makes a session treated for the role.
MOVE2 = {"reviewer": ("pr-harden", "reviewer.md", "`reviewer.md` in this skill's directory"),
         "refuter": ("resolve-ticket", "refuter.md", "`refuter.md` in this skill's directory")}
HOME = str(Path.home())
TMP = re.compile(r"^(?:/tmp/|/private/tmp/|/private/var/folders/|/var/folders/|\$TMPDIR|\$\{TMPDIR|/dev/null)")
# A Bash command writes a file when it does one of these. Redirections are judged by their target, so
# a scratch file under the temp directory is not an edit; the verbs below always are.
# A git verb must end at whitespace, so `git merge-base` is not `git merge` (a `\b` there counted a
# reviewer's read-only rev-parse chain as an edit, measured on the real transcripts).
SHELL_WRITES = re.compile(r"(?:^|[\s;&|(])(?:sed\s+(?:-\w+\s+)*-i|perl\s+(?:-\w+\s+)*-p?i(?:e|\b)|"
                          r"git\s+(?:commit|apply(?!\s+--check)|am|checkout\s+--|restore|stash(?!\s+(?:list|show))|"
                          r"merge|rebase|cherry-pick|reset|rm|mv)(?=\s|$)|patch\s)")
PY_WRITES = re.compile(r"\.write_text\(|\.write_bytes\(|\bopen\([^)]*['\"][wax]\+?['\"]")
INTERPRETER = re.compile(r"(?:^|[\s;&|(])(?:python3?|bash|sh|zsh|perl|ruby|node)\b[^\n<]*<<")
RUNS = re.compile(r"(?:^|[\s;&|(])(?:python3?|bash|sh|zsh|perl|ruby|node)\s+(?:-\w+\s+)*([^\s;&|<>-][^\s;&|<>]*)")
HEREDOC_TO = re.compile(r"(?:cat|tee)\s*>?\s*([^\s;&|<>]+)\s*<<-?\s*['\"]?(\w+)['\"]?")
REDIRECT = re.compile(r"(?<![0-9&<>=])>>?\s*(?!&)([^\s;&|]+)")
TOUCHES = re.compile(r"(?:^|[\s;&|(])(?:tee|mv|cp|rm|truncate|ln)\s+(?:-\S+\s+)*((?:[^\s;&|]+\s+)*[^\s;&|]+)")


def events(path):
    with open(path, errors="replace") as fh:
        for line in fh:
            try:
                yield json.loads(line)
            except ValueError:
                continue


def blocks(e):
    """The content blocks of one transcript event, whatever shape its message takes."""
    msg = e.get("message") or {}
    cont = msg.get("content")
    if isinstance(cont, str):
        return [{"type": "text", "text": cont}]
    return [c for c in cont if isinstance(c, dict)] if isinstance(cont, list) else []


def scan_session(path):
    """(pr-harden base directory or None, treated, [(index, Agent tool_use)] after pr-harden loaded,
    the session's first timestamp)."""
    base, treated, loaded_at, spawns, started, refused = None, False, None, [], None, set()
    for i, e in enumerate(events(path)):
        if started is None and e.get("timestamp"):
            started = str(e["timestamp"])
        for c in blocks(e):
            text = c.get("text") if c.get("type") == "text" else None
            if isinstance(text, str) and BASE in text:
                d = text.split(BASE, 1)[1].split()[0]
                if d.rstrip("/").endswith("/pr-harden"):
                    base = d.rstrip("/")
                    loaded_at = i if loaded_at is None else loaded_at
                    flat = " ".join(text.split())
                    treated = treated or any(p in flat for p in POINTERS)
            if c.get("type") == "tool_use" and c.get("name") in ("Agent", "Task") and loaded_at is not None:
                spawns.append((i, c))
            if c.get("type") == "tool_result" and c.get("is_error"):
                refused.add(c.get("tool_use_id"))
    ran = set(subagent_files(path))
    spawns = [(i, c) for i, c in spawns if c.get("id") not in refused or c.get("id") in ran]
    return base, treated, spawns, started


def role_of(desc, prompt):
    """What a spawn is: "fixer" or "verifier", which the bar measures; "reviewer" or "harden", which it
    does not; None for one it cannot place. A description naming review, refutation or confirmation
    at the start of a word, so not "preview" or "unconfirmed", is a reviewer's, whatever its brief
    says: a reviewer's brief names the fixer, the verifier and the standalone too, which is how a
    looser version counted reviewers as fixers. One naming a harden cycle or phase is harden's own
    agent, which pr-harden never briefed ("Harden cycle 2 fixer", "Cycle 4 verification pass"). Both
    are decided by the description's words alone, so "Fix review findings round 2" is a reviewer's
    too: a known limit, measured in the proposal's seventh revision.

    The brief is read only when the description is silent, and then only for the role it states: the
    first "you are the/a/an" clause naming a role within 40 characters, before a full stop, decides.
    A possessive, a role joined to a hyphen on either side, or a role file's name is not a role. The
    clause places a fixer or a verifier only when that is the one role it names, so "You are the
    fixer; the verifier ran in round 1" is unplaced, for a hand check. It never places a reviewer: a
    reviewer takes a spawn out of the count, and four reviews each found brief wording that would take
    a fixer out with it, the last of them even with the subagent's own `pr-review` call required. So
    a clause naming the reviewer is unplaced too, as wave 1's "PR 546 blocking-only round 3" was."""
    if re.search(r"\breview|\brefut|\bconfirm", desc, re.I):
        return "reviewer"
    if re.search(HARDEN_WORDS, desc, re.I):
        return "harden"
    if re.search(r"verif", desc, re.I):
        return "verifier"
    if re.search(r"\bfix", desc, re.I):
        return "fixer"
    head = prompt[:400]
    for stated in re.finditer(r"\byou are (?:the|an?)\b", head, re.I):
        start = stated.end()
        stop = head.find(".", start)
        stop = len(head) if stop < 0 else stop
        roles = {m.group(1).lower() for m in ROLE_WORD.finditer(head, start)
                 if m.start() < stop and m.start() - start <= 40}
        if roles:
            return roles.pop() if len(roles) == 1 and "reviewer" not in roles else None
    return None


def subagent_files(session_path):
    d = Path(str(session_path)[:-len(".jsonl")]) / "subagents"
    out = {}
    for meta in d.glob("agent-*.meta.json"):
        try:
            tid = json.loads(meta.read_text()).get("toolUseId")
        except (OSError, ValueError):
            continue
        if tid:
            out[tid] = meta.with_name(meta.name[:-len(".meta.json")] + ".jsonl")
    return out


def spellings(path, cwd=None):
    """The ways a command can name one file: absolute, under `~` or `$HOME`, or relative to `cwd`."""
    p = str(path)
    out = {p}
    if p.startswith(HOME + "/"):
        rest = p[len(HOME) + 1:]
        out |= {"~/" + rest, "$HOME/" + rest, "${HOME}/" + rest}
    if cwd:
        c = str(cwd).rstrip("/")
        if c.startswith("~"):
            c = HOME + c[1:]
        if p.startswith(c + "/"):
            out |= {p[len(c) + 1:], "./" + p[len(c) + 1:]}
    return out


def shown_by(stage):
    """The lines one pipeline stage prints of the file it names: (a, b or None for the end) or None."""
    if re.match(r"(?:\w+=\S+\s+)*(?:command\s+)?(?:cat|nl|less|more|bat)\b", stage):
        return (1, None)
    if m := re.match(r"sed\s+-n\s+['\"]?(\d+),(\d+|\$)p", stage):
        return (int(m.group(1)), None if m.group(2) == "$" else int(m.group(2)))
    if m := re.match(r"head\s+(?:-n\s*)?-?(\d+)\b", stage):
        return (1, int(m.group(1)))
    if re.match(r"head\b", stage):
        return (1, 10)
    if m := re.match(r"tail\s+-n\s*\+(\d+)", stage):
        return (int(m.group(1)), None)
    if m := re.match(r"awk\s+['\"]NR\s*(>=?)\s*(\d+)\s*&&\s*NR\s*(<=?)\s*(\d+)['\"]", stage):
        return (int(m.group(2)) + (m.group(1) == ">"), int(m.group(4)) - (m.group(3) == "<"))
    return None


def narrowed(rng, stage):
    """What a downstream stage leaves shown of lines (a, b) it is fed, or None when it shows no whole
    line range of them (grep, wc, cut, sort, a redirection into a file …)."""
    a, b = rng
    if REDIRECT.search(unquoted(stage)):
        return None
    if re.match(r"(?:cat|nl)\b", stage):
        return rng
    sub = shown_by(stage)
    if sub is None:
        return None
    lo, hi = a + sub[0] - 1, None if sub[1] is None else a + sub[1] - 1
    if b is not None:
        hi = b if hi is None else min(hi, b)
    return (lo, hi)


def split_unquoted(text, pipes=False):
    """Split shell text on `&&`, `||`, `;` and newlines — or, with `pipes`, on single `|` — outside
    quotes only, so the `&&` inside `awk 'NR>=1 && NR<=200'` does not cut the program in half."""
    out, cur, quote, i = [], [], None, 0
    while i < len(text):
        ch = text[i]
        if quote:
            cur.append(ch)
            if ch == "\\" and quote == '"' and i + 1 < len(text):
                cur.append(text[i + 1])
                i += 1
            elif ch == quote:
                quote = None
        elif ch in "'\"":
            quote = ch
            cur.append(ch)
        elif pipes and ch == "|" and text[i + 1:i + 2] != "|" and (not cur or cur[-1] != "|"):
            out.append("".join(cur))
            cur = []
        elif not pipes and (text[i:i + 2] in ("&&", "||") or ch in ";\n"):
            out.append("".join(cur))
            cur = []
            i += 1 if text[i:i + 2] in ("&&", "||") else 0
        else:
            cur.append(ch)
        i += 1
    out.append("".join(cur))
    return [x for x in out if x.strip()]


def bash_reads(cmd, role_path, cwd=None):
    """The line ranges a Bash command shows of the file — [(a, b or None for the end)] — or None when
    it never names it. A pipeline is judged whole: `cat X | head -50` shows 50 lines, and `cat X > f`
    shows none. A command that names the file without showing it (grep, wc) adds nothing."""
    found, here, shell = None, cwd, heredocs(cmd)[0]
    env = assignments(shell)
    for command in split_unquoted(shell):
        command = with_vars(command, env)
        stages = [st.strip() for st in split_unquoted(command, pipes=True)]
        m = re.match(r"cd\s+(\S+)$", stages[0]) if stages else None
        if m:
            here = m.group(1).strip("'\"")
            continue
        forms = spellings(role_path, here)
        for k, st in enumerate(stages):
            if not any(f in st for f in forms):
                continue
            found = found if found is not None else []
            rng = None if REDIRECT.search(unquoted(st)) else shown_by(st)
            for down in stages[k + 1:]:
                if rng is None:
                    break
                rng = narrowed(rng, down)
            if rng is not None:
                found.append(rng)
            break
    return found


def heredocs(cmd):
    """Split a command into its shell text and its heredoc bodies: [(opening line, body)]."""
    lines, shell, bodies, i = cmd.split("\n"), [], [], 0
    while i < len(lines):
        line = lines[i]
        shell.append(line)
        m = re.search(r"<<-?\s*['\"]?(\w+)['\"]?", line)
        i += 1
        if m:
            body = []
            while i < len(lines) and lines[i].strip() != m.group(1):
                body.append(lines[i])
                i += 1
            i += 1
            bodies.append((line, "\n".join(body)))
    return "\n".join(shell), bodies


def unquoted(text):
    return re.sub(r'"(?:\\.|[^"\\])*"', '""', re.sub(r"'[^']*'", "''", text))


def expanded(target, env):
    """A target with the command's own `VAR=value` assignments substituted and its quotes removed."""
    return re.sub(r"\$\{?(\w+)\}?", lambda m: env.get(m.group(1), m.group(0)), target.strip("'\""))


def assignments(shell):
    return {m.group(1): m.group(2).strip("'\"") for m in re.finditer(r"(?:^|[\s;&|(])(\w+)=(\S+)", shell)}


def with_vars(command, env):
    return re.sub(r"\$\{?(\w+)\}?", lambda m: env.get(m.group(1), m.group(0)), command)


def resolved(target, env, here):
    """Where a write lands: the command's variables expanded, `~` expanded, and a relative path joined
    to the directory its own `cd`s left it in (or the session's)."""
    t = expanded(target, env)
    if t.startswith("~"):
        t = HOME + t[1:]
    if t and not t.startswith(("/", "$")) and here:
        t = str(here).rstrip("/") + "/" + t
    return t


def words(segment):
    try:
        return shlex.split(segment, comments=True)
    except ValueError:
        return segment.split()


def redirect_targets(segment):
    """The files a segment's unquoted `>`/`>>` write stdout to, read with quotes honoured. A `2>` or a
    `>&` is not a file write of stdout, and a `>` inside quotes is not a redirection at all."""
    out, quote, i = [], None, 0
    while i < len(segment):
        ch = segment[i]
        if quote:
            quote = None if ch == quote else quote
        elif ch in "'\"":
            quote = ch
        elif ch == ">" and (i == 0 or segment[i - 1] not in "<>=-"):
            fd = re.search(r"(?:^|\s)(\d)$", segment[:i])
            j = i + 1 + (segment[i + 1:i + 2] == ">")
            if segment[j:j + 1] == "&" or (fd and fd.group(1) != "1"):
                i = j + 1
                continue
            while j < len(segment) and segment[j] == " ":
                j += 1
            k, q = j, None
            while k < len(segment) and (q or segment[k] not in " ;&|<>"):
                q = (None if segment[k] == q else q) if q else (segment[k] if segment[k] in "'\"" else None)
                k += 1
            out.append(segment[j:k])
            i = k
            continue
        i += 1
    return out


def noop(stage):
    """A stage that neither reads the role file nor does anything: a comment, `set`, an assignment,
    `export`, `echo`, `printf`, `true`, `:`."""
    st = stage.strip()
    return not st or st.startswith("#") or bool(re.match(r"(?:echo|printf|true|:|set|export)\b|\w+=\S*$", st))


def does_more_than_read(cmd, role_path, cwd=None):
    """Whether a command runs anything besides showing the role file. What else it runs was written
    before the file's contents were seen, so for a verifier it is an act in the same turn as the read."""
    here, shell = cwd, heredocs(cmd)[0]
    env = assignments(shell)
    for command in split_unquoted(shell):
        command = with_vars(command, env)
        first = split_unquoted(command, pipes=True)[0].strip() if split_unquoted(command, pipes=True) else ""
        m = re.match(r"cd\s+(\S+)$", first)
        if m:
            here = m.group(1).strip("'\"")
            continue
        if noop(first):
            continue
        if not any(f in command for f in spellings(role_path, here)):
            return True
    return False


VERB_TARGETS = {"tee": "all", "rm": "all", "truncate": "all", "mv": "last", "cp": "last", "ln": "last"}
GIT_WRITES = re.compile(r"(?:^|[\s;&|(])git\s+(?:commit|apply(?!\s+--check)|am|checkout(?:\s+[^\s-]\S*)?\s+--(?=\s)|restore|"
                        r"stash(?!\s+(?:list|show))|merge|rebase|cherry-pick|reset|rm|mv)(?=\s|$)")


def writes(cmd, cwd=None):
    """Whether a Bash command writes a file outside the temp directory. Every target is resolved before
    it is judged — against the command's own `cd`s and `VAR=` assignments, quotes honoured — so
    `cd /tmp && cat > x`, `> "$P/log"` with `P` a temp path, and `sed -i … /tmp/x` are scratch. Git
    verbs always edit the repository. Python writes in code that runs are judged by their literal
    targets, and one whose target is not a literal counts as an edit."""
    shell, bodies = heredocs(cmd)
    env, here = assignments(shell), cwd
    if GIT_WRITES.search(unquoted(shell)):
        return True
    for command in split_unquoted(shell):
        for seg in split_unquoted(command, pipes=True):
            w = words(seg)
            while w and re.match(r"\w+=", w[0]):
                w = w[1:]
            if w and w[0] == "cd" and len(w) > 1:
                here = resolved(w[1], env, here)
                continue
            for t in redirect_targets(seg):
                if not TMP.match(resolved(t, env, here)):
                    return True
            if not w:
                continue
            verb, args = w[0], [a for a in w[1:] if not a.startswith("-") and not re.match(r"\d*>|&>|<", a)]
            if verb in VERB_TARGETS and args:
                targets = args if VERB_TARGETS[verb] == "all" else args[-1:]
                if any(not TMP.match(resolved(t, env, here)) for t in targets):
                    return True
            in_place = r"-[Ernsuz]*i" if verb == "sed" else r"-[aclnpsw0-9]*i"  # not perl's -M, -I or -e
            if verb in ("sed", "perl") and any(re.match(in_place, a) for a in w[1:]) and args:
                if any(not TMP.match(resolved(t, env, here)) for t in (args[1:] or args[-1:])):
                    return True
    ran = [b for opener, b in bodies if INTERPRETER.search(opener)]
    if re.search(r"\bpython3?\s+-c\b", unquoted(shell)):
        ran.append(shell)
    return any(python_writes(body, env, here) for body in ran)


def python_writes(body, env=None, here=None):
    """Whether Python code writes a file outside the temp directory. A write's target is read from a
    literal, or from a variable the code assigns a literal (`p = 'api/…'; Path(p).write_text(…)`);
    one it cannot resolve counts as an edit."""
    env = env or {}
    names = {m.group(1): m.group(2) for m in re.finditer(
        r"""^\s*(\w+)\s*=\s*(?:Path\()?\s*(?:r|f)?['"]([^'"]+)['"]""", body, re.M)}
    for m in PY_WRITES.finditer(body):
        near = body[max(0, m.start() - 200):m.end() + 80]
        lit = re.search(r"""(?:Path|open)\(\s*(?:r|f)?['"]([^'"]+)['"]""", near)
        var = re.search(r"(?:\b(\w+)\.write_(?:text|bytes)\(|Path\(\s*(\w+)\s*\)\.write_|open\(\s*(\w+)\s*,)", body[max(0, m.start() - 40):m.end() + 40])
        target = lit.group(1) if lit else None
        if var and not lit:
            name = next(g for g in var.groups() if g)
            target = names.get(name)
        if target is None or not TMP.match(resolved(target, env, here)):
            return True
    return False


def scripts_written(cmd):
    """Scratch scripts a command writes through a heredoc, and whether each would write a file."""
    shell, bodies = heredocs(cmd)
    out = {}
    for opener, body in bodies:
        m = HEREDOC_TO.search(opener)
        if m:
            out[m.group(1).strip("'\"")] = python_writes(body) or writes(body)
    return out


def scripts_run(cmd):
    """Scripts a command runs; `bash -n x` only checks x's syntax, so it runs nothing."""
    return [m.group(1).strip("'\"") for m in RUNS.finditer(unquoted(heredocs(cmd)[0]))
            if not re.match(r"(?:ba|z)?sh\s+(?:-\w+\s+)*-\w*n", m.group(0).strip(" ;&|("))]


def modules_imported(cmd):
    """Module names the interpreter heredocs of a command import."""
    names = set()
    for opener, body in heredocs(cmd)[1]:
        if INTERPRETER.search(opener):
            names |= {a or b for a, b in re.findall(r"^\s*(?:from\s+(\w+)\s+import|import\s+(\w+))", body, re.M)}
    return names


def covered(ranges, total):
    """Whether the ranges show every line. A range open to the end from line 1 does, whatever the
    length; anything else needs the length, and an unknown length is not a full read."""
    if any(a <= 1 and b is None for a, b in ranges):
        return True
    if not ranges or not total:
        return False
    seen = set()
    for a, b in ranges:
        seen.update(range(max(a, 1), (total if b is None else min(b, total)) + 1))
    return seen >= set(range(1, total + 1))


def read_check(agent_jsonl, role_path, role):
    """How the subagent read its role file: (state, index of the call that completed a full read,
    index of its first act, the file's length as the transcript recorded it). A command that only
    shows the role file is reading it, not acting. Calls sent in one message share a turn, and a
    read only counts if its turn comes before the act's.

    A preview of output too large to show is no read: the file was not shown. A result flagged as an
    error is not settled either way. It may have shown the file, as `cat reviewer.md; echo ======`
    did in wave 2 before zsh's `=`-expansion failed on the separator, or not, as a `cat` of a missing
    file does. So an errored result counts toward a whole read at some point, which keeps the spawn
    from a FAIL, and never toward a CLEAN one, which keeps it from a PASS. So does a result saying "No
    such file or directory" that is not flagged, because `cat role.md; ls` exits 0. Such a spawn's state
    is "errored", and it goes to a hand check, if its brief names the file; if not, it FAILs on that."""
    if not agent_jsonl or not agent_jsonl.exists():
        return "no-transcript", None, None, None, None, False
    reads, total, act, act_turn, n, turn, last_msg, mentioned, scripts = [], None, None, None, 0, 0, object(), False, {}
    void = set()     # previews of output too large to show: the file was not shown
    errored = set()  # results flagged as errors: whether the file was shown is for a hand check
    first_other_turn = None  # the turn of the first call that is not a pure read
    for seq, e in enumerate(events(agent_jsonl)):
        for c in blocks(e):
            if c.get("type") == "tool_result":
                body = json.dumps(c.get("content"))
                if "persisted-output" in body or "Output too large" in body:
                    void.add(c.get("tool_use_id"))
                elif c.get("is_error") or "No such file or directory" in body:
                    # `cat role.md; ls` of a missing role file exits 0, so the flag alone misses it
                    errored.add(c.get("tool_use_id"))
        tur = e.get("toolUseResult")
        if isinstance(tur, dict) and isinstance(tur.get("file"), dict) \
                and str(tur["file"].get("filePath", "")) == role_path and tur["file"].get("totalLines"):
            total = int(tur["file"]["totalLines"])
        msg_id = (e.get("message") or {}).get("id") or e.get("uuid") or f"event-{seq}"
        for c in blocks(e):
            if c.get("type") != "tool_use":
                continue
            n += 1
            if msg_id != last_msg:
                turn, last_msg = turn + 1, msg_id
            name, inp = c.get("name"), c.get("input") or {}
            got, acting = None, False
            if name == "Read" and str(inp.get("file_path", "")).rstrip() in spellings(role_path, e.get("cwd")):
                off = int(inp.get("offset") or 1)
                lim = inp.get("limit")
                got = [(off, None if lim is None else off + int(lim) - 1)]
            elif name == "Bash":
                cmd = str(inp.get("command", ""))
                got = bash_reads(cmd, role_path, e.get("cwd"))
                scripts.update(scripts_written(cmd))
                helpers = {Path(k).stem for k, v in scripts.items() if v and k.endswith(".py")}
                by_name = {Path(k).name: v for k, v in scripts.items()}
                acting = writes(cmd, e.get("cwd")) or any(scripts.get(x) or by_name.get(Path(x).name) for x in scripts_run(cmd)) \
                    or bool(modules_imported(cmd) & helpers) \
                    or (role == "verifier" and does_more_than_read(cmd, role_path, e.get("cwd")))
            elif name in EDITS:
                target = str(inp.get("file_path") or inp.get("notebook_path") or "")
                if name == "Write" and target.endswith(".py"):
                    scripts[target] = bool(PY_WRITES.search(str(inp.get("content", ""))))
                acting = not TMP.match(target)
            pure_read = name in ("Read", "Grep", "Glob", "LS") or (
                name == "Bash" and got is not None and not does_more_than_read(cmd, role_path, e.get("cwd")))
            if role in ("reviewer", "refuter"):
                # Both headers say to read the file before anything else, and the reviewer's names loading
                # `pr-review`: so for them only a read of that file is pure, and any other call, a Read of
                # another file included, is the act.
                pure_read = got is not None and (name != "Bash" or not does_more_than_read(cmd, role_path, e.get("cwd")))
                acting = acting or not pure_read
            if act is None and acting:
                act, act_turn = n, turn
            if first_other_turn is None and not pure_read:
                first_other_turn = turn
            if got is not None:
                mentioned = True
                reads.append((n, turn, got, c.get("id")))
    if total is None:
        try:
            total = len(Path(role_path).read_text().splitlines()) or None
        except OSError:
            total = None
    full_at, so_far = None, []
    for k, t, got, tid in reads:
        if act_turn is not None and t >= act_turn:
            break
        if tid in void or tid in errored:
            continue
        so_far += got
        if covered(so_far, total):
            full_at = k
            break

    def first_whole(skip):
        """The call, and turn, at which reads not in `skip` first cover the file."""
        so_far = []
        for k, t, got, tid in reads:
            if tid in skip:
                continue
            so_far += got
            if covered(so_far, total):
                return k, t
        return None, None
    ever_at, ever_turn = first_whole(void | errored)   # a whole read, every result of it shown
    loose_at, _ = first_whole(void)                     # or one that needs a result flagged as an error
    clean = ever_turn is not None and (first_other_turn is None or ever_turn < first_other_turn)
    state = "full" if full_at is not None else "errored" if ever_at is None and loose_at is not None \
        else "partial" if mentioned else "none"
    return state, full_at, act, total, ever_at if ever_at is not None else loose_at, clean


def items(role, prompt, earlier_verifier):
    if role == "fixer":
        return {"findings": bool(re.search(r"\br\d+-\d+\b|\bfinding", prompt, re.I)),
                "build": bool(re.search(r"\bmvn\b|clean install", prompt)),
                "commit": bool(re.search(r"\bcommit", prompt, re.I))}
    got = {"round": bool(re.search(r"\bround\b|\br\d+\b", prompt, re.I)),
           "head": bool(re.search(r"\b[0-9a-f]{7,40}\b", prompt)),
           "drive": bool(re.search(r"\bdrive\b|\bREST\b|endpoint|\bcurl\b|\bquery\b|/ws/rest", prompt, re.I))}
    if earlier_verifier:
        got["repairs"] = bool(re.search(r"\brepair", prompt, re.I))
    return got


def named_as(prompt, role_path, name):
    """How a brief names its role file: by the absolute path the session printed, by a `~` path, by its
    bare name, or not at all."""
    if role_path in prompt:
        return "absolute"
    if re.search(r"~/\S*" + re.escape(name), prompt):
        return "tilde"
    return "bare" if name in prompt else "missing"


def check_session(path):
    base, treated, spawns, started = scan_session(path)
    if base is None:
        return None
    subs, rows, verifiers_seen, unclassified = subagent_files(path), [], 0, []
    for _, c in spawns:
        inp = c.get("input") or {}
        desc, prompt = str(inp.get("description", "")), str(inp.get("prompt", ""))
        role = role_of(desc, prompt)
        if role is None:
            unclassified.append(desc)
            continue
        if role not in ROLE_FILE:
            continue
        role_path = f"{base}/{ROLE_FILE[role]}"
        path_kind = named_as(prompt, role_path, ROLE_FILE[role])
        how, at, act, _, ever_at, clean = read_check(subs.get(c.get("id")), role_path, role)
        in_time = how == "full"
        rows.append({"role": role, "description": desc, "path": path_kind, "read": how,
                     "read_at": at, "first_act_at": act, "read_before_acting": in_time,
                     "meets_a": path_kind == "absolute", "meets_b": in_time,
                     "read_full_ever": ever_at is not None, "whole_read_at": ever_at, "read_clean": clean,
                     "items": items(role, prompt, verifiers_seen > 0)})
        verifiers_seen += role == "verifier"
    return {"session": str(path), "treated": treated, "started": started, "spawns": rows,
            "unclassified": unclassified}


def sessions(args):
    if args:
        return [Path(a) for a in args]
    out = []
    for p in sorted(PROJECTS.glob("*/*.jsonl")):
        try:
            blob = p.read_bytes()
        except OSError:
            continue
        if BASE.encode() in blob and b"/pr-harden" in blob:
            out.append(p)
    return out


def report(results, as_json):
    results = sorted((r for r in results if r), key=lambda r: r.get("started") or "")
    if as_json:
        print(json.dumps(results, indent=1))
        return 0
    treated = [r for r in results if r["treated"]]
    for r in results:
        tag = "TREATED" if r["treated"] else "pre-0.36"
        print(f"\n{tag}  {r['session']}")
        for s in r["spawns"]:
            ok = "ok  " if s["meets_a"] and s["meets_b"] else "MISS"
            miss_items = [k for k, v in s["items"].items() if not v]
            print(f"  {ok} {s['role']:8} path={s['path']:8} read={s['read']:13} "
                  f"read@{s['read_at']} act@{s['first_act_at']}  {s['description'][:50]}"
                  + (f"  items missing: {', '.join(miss_items)}" if miss_items else ""))
    spawns = [s for r in treated for s in r["spawns"]]
    with_spawns = [r for r in treated if r["spawns"]]
    print(f"\n{len(results)} session(s) that loaded pr-harden; {len(treated)} treated, "
          f"{len(with_spawns)} of them with a fixer or verifier spawn; "
          f"{sum(s['meets_a'] and s['meets_b'] for s in spawns)} of {len(spawns)} treated spawns meet (a) and (b).")
    untreated = [s for r in results if not r["treated"] for s in r["spawns"]]
    leaks = [s for s in untreated if s["meets_a"] or s["read"] in ("full", "partial", "errored")]
    print(f"known-negative: {len(untreated)} pre-0.36 spawn(s), {len(leaks)} meeting (a) or reading a role file"
          + (" — the detector is wrong" if leaks else ""))
    odd = [d for r in treated for d in r["unclassified"]]
    print(f"treated spawns that are neither a reviewer nor recognisably a fixer or verifier: {len(odd)}"
          + (" — read these by hand: " + "; ".join(odd[:8]) if odd else ""))
    call_bar(treated, odd)
    return 0


def call_bar(treated, odd, prefix="bar"):
    """The pre-registered bar over the treated sessions' spawn rows, printed under `prefix`.

    It decides only what the transcript settles. FAIL: the brief did not name the file, or the file was
    never read whole. PASS: every read was CLEAN — whole before the subagent's first call that is not a
    pure read, so nothing can have acted first and no act detection is needed. A whole read that came
    after other calls is LATE: it is listed with the act detector's verdict as advice, and read by
    hand, because five reviews of that detector kept finding shell shapes it misjudged."""
    with_spawns = [r for r in treated if r["spawns"]]
    failed = [(r["session"], s) for r in treated for s in r["spawns"] if not s["meets_a"] or not s["read_full_ever"]]
    late = [(r["session"], s) for r in treated for s in r["spawns"]
            if s["meets_a"] and s["read_full_ever"] and not s["read_clean"]]
    if failed:
        sess, sp = failed[0]
        why = "its brief does not name the file" if not sp["meets_a"] else "it never read the whole file"
        print(f"{prefix}: FAIL — {len(failed)} treated spawn(s) missed; the first, {sp['description']!r} in {sess}: {why}")
    elif late:
        print(f"{prefix}: needs a hand check — {len(late)} treated spawn(s) did not read the whole file before the "
              "message holding their first call that is not a pure read:")
        for sess, sp in late:
            verdict = "before" if sp["meets_b"] else "after"
            flagged = "; its result was flagged as an error, so read whether the file was shown" \
                if sp["read"] == "errored" else ""
            print(f"    {sp['description']!r}: whole read at call {sp['whole_read_at']}; the act detector says it came "
                  f"{verdict} the first act (call {sp['first_act_at']}){flagged} — read {sess} to decide")
    elif odd:
        print(f"{prefix}: not decidable yet — {len(odd)} treated spawn(s) are unclassified and must be read by hand first")
    elif len(with_spawns) < 3:
        print(f"{prefix}: not decidable yet — {len(with_spawns)} of the 3 treated sessions with a spawn exist, no miss so far")
    else:
        print(f"{prefix}: PASS — {len(with_spawns)} treated sessions with a spawn, every read clean and every brief naming the file")


# Records name the sections as `pr-harden:VERIFY`, `pr-harden:"### 6 — VERIFY"`, `pr-harden §6` or
# `pr-harden:step 6` (and 4 for FIX), and the agents by role. Calibrated in --selftest on record lines.
FRICTION = re.compile(r"pr-harden\s*[:§]?\s*[\"'(]*(?:#+\s*)?(?:(?:6|step\s*6)\s*—?\s*)?VERIFY"
                      r"|pr-harden\s*[:§]?\s*[\"'(]*(?:#+\s*)?(?:(?:4|step\s*4)\s*—?\s*)?FIX\b"
                      r"|pr-harden\s*(?:§\s*|:\s*step\s*|\s+step\s+)[46]\b"
                      r"|\bverifier\b|\bfixer\b", re.I)
SECTION = "## Where a skill blocked or contradicted this run"


def baseline(records, before, after):
    """The share of run RECORDS, not files, whose skill-blocked section names the VERIFY or FIX rules
    or either agent. A file can hold several records, appended by later runs on the same ticket and
    day, and each opens with a level-1 header carrying ` · `; reading only a file's first section
    undercounted (125 files, 131 records, before this split)."""
    n = hit = files = 0
    for f in sorted(records.glob("20??-??-??-*.md")):
        day = f.name[:10]
        if (before and day >= before) or (after and day < after):
            continue
        files += 1
        text = f.read_text(errors="replace")
        for rec in re.split(r"(?m)^(?=# [^\n]*·)", text):
            if SECTION not in rec:
                continue
            sec = rec.split(SECTION, 1)[1].split("\n## ", 1)[0].split("\n# ", 1)[0]
            n += 1
            hit += bool(FRICTION.search(sec))
    window = f"before {before}" if before else f"from {after}" if after else "all dates"
    print(f"records {window} with the section: {n} (in {files} files); naming pr-harden VERIFY/FIX, the verifier or "
          f"the fixer: {hit} ({(100 * hit / n) if n else 0:.0f}%)")
    return 0


# The second move's slower comparison: records naming pr-harden's REVIEW, resolve-ticket's Step 3, the
# reviewer or the refuter, in the spellings FRICTION takes for VERIFY and FIX. Calibrated in --selftest.
FRICTION2 = re.compile(r"pr-harden\s*[:§]?\s*[\"'(]*(?:#+\s*)?(?:(?:1|step\s*1)\s*—?\s*)?REVIEW\b"
                       r"|pr-harden\s*(?:§\s*|:\s*step\s*|\s+step\s+)1\b"
                       r"|resolve-ticket\s*[:§]?\s*[\"'(]*(?:#+\s*)?(?:§\s*3|step\s*3)\b"
                       r"|\breviewer\b|\brefuter\b|\brefutation\b", re.I)


# The date pr-harden 0.37.0 and resolve-ticket 0.23.0 shipped. A record whose session transcript is gone
# counts as before the move if it is dated earlier, since every session then ran the versions before it,
# and cannot be placed if it is dated that day or later.
MOVE2_SHIPPED = "2026-09-28"
TRANSCRIPT = re.compile(r"transcript:\s*`?([^\s`]+\.jsonl)")
UUID = re.compile(r"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")


def baseline2(records, before, after):
    """The second move's slower comparison. A record is after the move when the session it names was
    treated for either file, which `scan2` decides. A date cannot: #528's record is dated the day the
    move shipped and its session ran the versions before it. A record whose session transcript is gone
    is before the move if it is dated before MOVE2_SHIPPED, and unknown otherwise."""
    groups = {"before": [0, 0], "after": [0, 0], "unknown": [0, 0]}
    for f in sorted(records.glob("20??-??-??-*.md")):
        day = f.name[:10]
        if (before and day >= before) or (after and day < after):
            continue
        for rec in re.split(r"(?m)^(?=# [^\n]*·)", f.read_text(errors="replace")):
            if SECTION not in rec:
                continue
            sec = rec.split(SECTION, 1)[1].split("\n## ", 1)[0].split("\n# ", 1)[0]
            named = TRANSCRIPT.search(rec)
            named = Path(named.group(1)).expanduser() if named else None
            paths = [named] if named and named.is_file() else \
                [p for u in UUID.findall(rec) for p in PROJECTS.glob(f"*/{u}.jsonl")]
            if paths:
                treated = scan2(paths[0])[1]
                group = "after" if treated["reviewer"] or treated["refuter"] else "before"
            else:
                group = "before" if day < MOVE2_SHIPPED else "unknown"
            groups[group][0] += 1
            groups[group][1] += bool(FRICTION2.search(sec))
    label = {"before": "before the second move", "after": "after the second move", "unknown": "that cannot be placed"}
    for group, (n, hit) in groups.items():
        print(f"records {label[group]} with the section: {n}; naming pr-harden REVIEW, resolve-ticket Step 3, "
              f"the reviewer or the refuter: {hit} ({(100 * hit / n) if n else 0:.0f}%)")
    return 0


def scan2(path):
    """What --second-move needs from one session: each skill's base directory, whether each role is
    treated (its skill's loaded text carries the role's pointer), the spawns in each role's window, and
    the first timestamp. A reviewer's window opens when pr-harden loads. A refuter's is Step 3's: it
    opens at each resolve-ticket load, so a second ticket in one session has one too, and closes when
    harden or pr-harden loads. A spawn in an open Step 3 is in that window only."""
    base = {"pr-harden": None, "resolve-ticket": None}
    treated = {"reviewer": False, "refuter": False}
    opened = {"reviewer": False, "refuter": False}
    closed, windows, started, refused = False, {"reviewer": [], "refuter": []}, None, set()
    for e in events(path):
        if started is None and e.get("timestamp"):
            started = str(e["timestamp"])
        for c in blocks(e):
            text = c.get("text") if c.get("type") == "text" else None
            if isinstance(text, str) and BASE in text:
                d = text.split(BASE, 1)[1].split()[0].rstrip("/")
                flat = " ".join(text.split())
                for role, (skill, _, pointer) in MOVE2.items():
                    if d.endswith("/" + skill):
                        base[skill] = d
                        opened[role] = True
                        treated[role] = treated[role] or pointer in flat
                if d.endswith("/resolve-ticket"):
                    closed = False
                elif opened["refuter"] and (d.endswith("/harden") or d.endswith("/pr-harden")):
                    closed = True
            if c.get("type") == "tool_use" and c.get("name") in ("Agent", "Task"):
                if opened["refuter"] and not closed:
                    windows["refuter"].append(c)
                elif opened["reviewer"]:
                    windows["reviewer"].append(c)
            if c.get("type") == "tool_result" and c.get("is_error"):
                refused.add(c.get("tool_use_id"))
    ran = set(subagent_files(path))
    for role in windows:
        windows[role] = [c for c in windows[role] if c.get("id") not in refused or c.get("id") in ran]
    return base, treated, windows, started


def ran_review(agent_jsonl):
    """Whether the subagent loaded `pr-review`, by the Skill tool or by showing its SKILL.md."""
    if not agent_jsonl or not agent_jsonl.exists():
        return False
    for e in events(agent_jsonl):
        for c in blocks(e):
            if c.get("type") != "tool_use":
                continue
            inp = c.get("input") or {}
            if c.get("name") == "Skill" and re.fullmatch(r"(?:[\w-]+:)?pr-review", str(inp.get("skill", ""))):
                return True
            if c.get("name") in ("Read", "Bash") and "skills/pr-review/SKILL.md" in json.dumps(inp):
                return True
    return False


def role2_of(window, desc, prompt, ran=lambda: False):
    """The second move's roles: "reviewer" or "refuter", which it measures; another role, which it leaves
    to the first move; None for a spawn it cannot place, which holds that role's PASS.

    In pr-harden's window the first move's `role_of` decides, with two exceptions, both unplaced. A
    review whose description also names a harden cycle or phase is harden's own agent: "Phase 2
    quality review", four of which follow a pr-harden load on disk. And a spawn placed as a fixer, a
    verifier or harden's agent that ran `pr-review` (`ran()`) behaved as a reviewer: a reviewer
    described as a "blocking-only verification round" would otherwise leave reviewer.md's count
    unseen. In Step 3's window a refuter names refutation, at a word's start, in its description or
    its brief's first 400 characters — "Refute plan for issue 528", or "Gate pass 2 on revised plan
    309", whose brief says "refutation gate" — and anything else there is unplaced. The name
    `refuter.md` is not refutation: as in the first move, a brief never places a spawn by naming its
    role file, since a brief that fails to name the file is the case being looked for."""
    if window == "reviewer":
        role = role_of(desc, prompt)
        if role == "reviewer":
            return None if re.search(HARDEN_WORDS, desc, re.I) else role
        return None if role in ("fixer", "verifier", "harden") and ran() else role
    return "refuter" if re.search(REFUT, desc, re.I) or re.search(REFUT, prompt[:400], re.I) else None


def items2(role, prompt):
    """What the pointer says the brief carries, reported and never gating. For a reviewer: the round, the
    head, the base, whether the run started from a ticket, and the ticket, named as an issue or a JIRA
    key, since a bare "#N" is as often the PR. For a refuter: the ticket, the plan and the repo."""
    if role == "reviewer":
        return {"round": bool(re.search(r"\bround\b|\br\d+\b", prompt, re.I)),
                "head": bool(re.search(r"\b[0-9a-f]{7,40}\b", prompt)),
                "base": bool(re.search(r"origin/|\bbase\b|baseRefName", prompt, re.I)),
                "ticket origin": bool(re.search(r"\bstarted from\b|\bfrom (?:a |the )?ticket\b|\bresolve-ticket\b"
                                                r"|\bexisting PR\b", prompt, re.I)),
                "ticket": bool(re.search(r"gh issue view|\bissue\s*#?\d+|/issues/\d+", prompt, re.I)
                               or re.search(JIRA, prompt))}
    ticket = bool(re.search(r"#\d+", prompt) or re.search(r"\b(?:issue|ticket)\b", prompt, re.I)
                  or re.search(JIRA, prompt))
    return {"ticket": ticket, "plan": bool(re.search(r"\bplan\b", prompt, re.I)),
            "repo": bool(re.search(r"/worktrees/|\brepo(?:sitory)?\b", prompt, re.I))}


def check_session2(path):
    base, treated, windows, started = scan2(path)
    if base["pr-harden"] is None and base["resolve-ticket"] is None:
        return None
    subs, rows, unclassified = subagent_files(path), [], {"reviewer": [], "refuter": []}
    for window, spawns in windows.items():
        for c in spawns:
            inp = c.get("input") or {}
            desc, prompt = str(inp.get("description", "")), str(inp.get("prompt", ""))
            role = role2_of(window, desc, prompt, lambda: ran_review(subs.get(c.get("id"))))
            if role is None:
                unclassified[window].append(desc)
                continue
            if role != window:
                continue
            skill, name, _ = MOVE2[role]
            role_path = f"{base[skill]}/{name}"
            path_kind = named_as(prompt, role_path, name)
            how, at, act, _, ever_at, clean = read_check(subs.get(c.get("id")), role_path, role)
            rows.append({"role": role, "treated": treated[role], "description": desc, "path": path_kind,
                         "read": how, "read_at": at, "first_act_at": act, "read_before_acting": how == "full",
                         "meets_a": path_kind == "absolute", "meets_b": how == "full",
                         "read_full_ever": ever_at is not None, "whole_read_at": ever_at, "read_clean": clean,
                         "items": items2(role, prompt)})
    return {"session": str(path), "started": started, "base": base, "treated": treated, "spawns": rows,
            "unclassified": unclassified}


def sessions2(args):
    if args:
        return [Path(a) for a in args]
    out = []
    for p in sorted(PROJECTS.glob("*/*.jsonl")):
        try:
            blob = p.read_bytes()
        except OSError:
            continue
        if BASE.encode() in blob and (b"/pr-harden" in blob or b"/resolve-ticket" in blob):
            out.append(p)
    return out


def report2(results, as_json):
    results = sorted((r for r in results if r), key=lambda r: r.get("started") or "")
    if as_json:
        print(json.dumps(results, indent=1))
        return 0
    for r in results:
        if not r["spawns"]:
            continue
        tag = " ".join(f"TREATED:{MOVE2[k][1]}" for k in MOVE2 if r["treated"][k]) or "untreated"
        print(f"\n{tag}  {r['session']}")
        for s in r["spawns"]:
            ok = "ok  " if s["meets_a"] and s["meets_b"] else "MISS"
            miss_items = [k for k, v in s["items"].items() if not v]
            print(f"  {ok} {s['role']:8} {'treated' if s['treated'] else 'pre    '} path={s['path']:8} "
                  f"read={s['read']:13} read@{s['read_at']} act@{s['first_act_at']}  {s['description'][:50]}"
                  + (f"  items missing: {', '.join(miss_items)}" if miss_items else ""))
    for role, (skill, name, _) in MOVE2.items():
        loaded = [r for r in results if r["base"][skill]]
        treated = [{"session": r["session"], "spawns": [s for s in r["spawns"] if s["role"] == role]}
                   for r in loaded if r["treated"][role]]
        spawns = [s for r in treated for s in r["spawns"]]
        untreated = [s for r in loaded if not r["treated"][role] for s in r["spawns"] if s["role"] == role]
        leaks = [s for s in untreated if s["meets_a"] or s["read"] in ("full", "partial", "errored")]
        odd = [d for r in loaded if r["treated"][role] for d in r["unclassified"][role]]
        print(f"\n{name}: {len(loaded)} session(s) that loaded {skill}; {len(treated)} treated, "
              f"{sum(1 for r in treated if r['spawns'])} of them with a {role} spawn; "
              f"{sum(s['meets_a'] and s['meets_b'] for s in spawns)} of {len(spawns)} treated spawns meet (a) and (b).")
        print(f"known-negative: {len(untreated)} untreated {role} spawn(s), {len(leaks)} meeting (a) or reading {name}"
              + (" — the detector is wrong" if leaks else ""))
        print(f"treated spawns in the {role}'s window that it cannot place: {len(odd)}"
              + (" — read these by hand: " + "; ".join(odd[:8]) if odd else ""))
        call_bar(treated, odd, prefix=f"bar ({name})")
    return 0


def write_agent(path, calls):
    with open(path, "w") as fh:
        for name_, inp in calls:
            fh.write(json.dumps({"type": "assistant", "message": {"content": [
                {"type": "tool_use", "id": "x", "name": name_, "input": inp}]}}) + "\n")
    return path


def selftest(_):
    """Planted transcripts, one per case the instrument must tell apart."""
    global HOME
    tmp = Path(tempfile.mkdtemp())
    HOME = str(tmp)  # so the fixtures' `~/…` spellings resolve, as a real home's would
    base = tmp / "skills/pr-harden"
    base.mkdir(parents=True)
    (base / "fixer.md").write_text("x\n" * 87)
    (base / "verifier.md").write_text("x\n" * 142)
    fails = 0

    def session(name, treated, spawns, started="2026-09-28T00:00:00Z", pointer=None, where=tmp):
        s = where / f"{name}.jsonl"
        sub = where / name / "subagents"
        sub.mkdir(parents=True)
        pointer = pointer or (POINTERS[0] if treated else "the fixer's brief carries harden's Phase 1 discipline")
        lines = [{"type": "user", "timestamp": started, "message": {"content": [{"type": "text", "text":
                  f"{BASE}{base}\n\n# PR harden ... {pointer} ..."}]}}]
        for k, (desc, prompt, calls) in enumerate(spawns):
            tid = f"toolu_{name}_{k}"
            lines.append({"type": "assistant", "message": {"content": [
                {"type": "tool_use", "id": tid, "name": "Agent", "input": {"description": desc, "prompt": prompt}}]}})
            (sub / f"agent-{k}.meta.json").write_text(json.dumps({"toolUseId": tid, "description": desc}))
            with open(sub / f"agent-{k}.jsonl", "w") as fh:
                for q, (name_, inp) in enumerate(calls):
                    if name_ == "__parallel__":  # several calls sent in ONE message
                        fh.write(json.dumps({"type": "assistant", "message": {"id": f"m{q}", "content": [
                            {"type": "tool_use", "id": f"x{q}{w}", "name": nm, "input": ip}
                            for w, (nm, ip) in enumerate(inp)]}}) + "\n")
                        continue
                    if name_ == "__result__":  # a Read's result, carrying the length at session time
                        fh.write(json.dumps({"type": "user", "toolUseResult": {"file": inp},
                                             "message": {"content": [{"type": "tool_result", "tool_use_id": f"c{q - 1}"}]}}) + "\n")
                        continue
                    if name_ == "__raw_result__":  # a raw result for the previous call: an error, or a preview
                        fh.write(json.dumps({"type": "user", "message": {"content": [dict(
                            {"type": "tool_result", "tool_use_id": f"c{q - 1}"}, **inp)]}}) + "\n")
                        continue
                    fh.write(json.dumps({"type": "assistant", "message": {"content": [
                        {"type": "tool_use", "id": f"c{q}", "name": name_, "input": inp}]}}) + "\n")
        with open(s, "w") as fh:
            for l in lines:
                fh.write(json.dumps(l) + "\n")
        return check_session(s)

    fx, vf = str(base / "fixer.md"), str(base / "verifier.md")
    brief_f = f"Read {fx} in full first. Findings r2-1 verbatim. Build: mvn -o clean install. Commit rules."
    brief_v = f"Read {vf} first. Round 2, head 3085ff02, drive the REST call."
    cases = [
        ("full read before editing", True, [("PR 9 fix round 2", brief_f,
            [("Read", {"file_path": fx}), ("Edit", {"file_path": "a.java"})])], (True, True)),
        ("truncated read", True, [("PR 9 fix round 2", brief_f,
            [("Read", {"file_path": fx, "limit": 40}), ("Edit", {"file_path": "a.java"})])], (True, False)),
        ("no read", True, [("PR 9 fix round 2", brief_f, [("Edit", {"file_path": "a.java"})])], (True, False)),
        ("read after the first edit", True, [("PR 9 fix round 2", brief_f,
            [("Edit", {"file_path": "a.java"}), ("Read", {"file_path": fx})])], (True, False)),
        ("brief without the path", True, [("PR 9 fix round 2", "Findings r2-1. mvn. commit.",
            [("Read", {"file_path": fx})])], (False, True)),
        ("verifier cat before deploy", True, [("Verify PR 9 round 1", brief_v,
            [("Bash", {"command": f"cat {vf}"}), ("Bash", {"command": "cp x.omod $S/appdata/modules/"})])], (True, True)),
        ("verifier head -50", True, [("Verify PR 9 round 1", brief_v,
            [("Bash", {"command": f"head -50 {vf}"})])], (True, False)),
    ]
    for label, treated, spawns, (want_a, want_b) in cases:
        r = session(label.replace(" ", "-"), treated, spawns)
        s = r["spawns"][0]
        ok = (s["meets_a"], s["meets_b"]) == (want_a, want_b)
        print(("PASS" if ok else "FAIL"), f"{label}: meets (a)={s['meets_a']} (b)={s['meets_b']}, read={s['read']}")
        fails += not ok
    tilde_v = "~/skills/pr-harden/verifier.md"
    more = [
        ("fixer edits through a Bash heredoc before reading", [("PR 9 fix round 2", brief_f,
            [("Bash", {"command": "python3 - <<'EOF'\nfrom pathlib import Path\nPath('a.java').write_text('x')\nEOF"}),
             ("Read", {"file_path": fx})])], (True, False)),
        ("a scratch write under /tmp is not an edit", [("PR 9 fix round 2", brief_f,
            [("Bash", {"command": "git diff > /tmp/before.diff"}), ("Read", {"file_path": fx}),
             ("Bash", {"command": "sed -i 's/a/b/' a.java"})])], (True, True)),
        ("verifier builds before reading", [("Verify PR 9 round 1", brief_v,
            [("Bash", {"command": "mvn -o clean install"}), ("Read", {"file_path": vf})])], (True, False)),
        ("verifier cats the ~ path", [("Verify PR 9 round 1", brief_v,
            [("Bash", {"command": f"cat {tilde_v}"}), ("Bash", {"command": "ls x"})])], (True, True)),
        ("cat of it then a grep of it, in one command", [("Verify PR 9 round 1", brief_v,
            [("Bash", {"command": f"cat {vf} && grep -n deploy {vf}"})])], (True, True)),
        ("head -400 of a 142-line file", [("Verify PR 9 round 1", brief_v,
            [("Bash", {"command": f"head -400 {vf}"})])], (True, True)),
        ("sed -n 1,200p of a 142-line file", [("Verify PR 9 round 1", brief_v,
            [("Bash", {"command": f"sed -n '1,200p' {vf}"})])], (True, True)),
        ("sed -n 1,50p is partial", [("Verify PR 9 round 1", brief_v,
            [("Bash", {"command": f"sed -n '1,50p' {vf}"})])], (True, False)),
        ("two Reads that together cover the file", [("Verify PR 9 round 1", brief_v,
            [("Read", {"file_path": vf, "limit": 80}), ("Read", {"file_path": vf, "offset": 81, "limit": 80})])], (True, True)),
        ("a limit past the length the transcript recorded", [("Verify PR 9 round 1", brief_v,
            [("Read", {"file_path": vf, "limit": 100}),
             ("__result__", {"filePath": vf, "startLine": 1, "numLines": 90, "totalLines": 90})])], (True, True)),
        ("cd into the skill, then cat the bare name", [("Verify PR 9 round 1", brief_v,
            [("Bash", {"command": f"cd {base} && cat verifier.md"})])], (True, True)),
    ]
    for label, spawns, (want_a, want_b) in more:
        r = session(re.sub(r"\W+", "-", label), True, spawns)
        s_ = r["spawns"][0]
        ok = (s_["meets_a"], s_["meets_b"]) == (want_a, want_b)
        print(("PASS" if ok else "FAIL"), f"{label}: meets (a)={s_['meets_a']} (b)={s_['meets_b']}, read={s_['read']}")
        fails += not ok
    r = session("rewrapped-pointer", True, [], pointer="`fixer.md` in this\n   skill's directory")
    ok = r["treated"] is True
    print(("PASS" if ok else "FAIL"), "a pointer re-wrapped across lines still marks the session treated")
    fails += not ok
    r = session("mention-is-not-a-role", True, [("round 2 agent", f"Read {fx} and then do the task.", [])])
    ok = r["spawns"] == [] and r["unclassified"] == ["round 2 agent"]
    print(("PASS" if ok else "FAIL"), "naming the role file does not by itself make a spawn a fixer")
    fails += not ok
    later = tmp / "a-sorts-first"
    later.mkdir()
    early_miss = session("zz-early-miss", True, [("PR 9 fix round 1", "Findings r1-1. mvn. commit.", [("Edit", {"file_path": "a"})])],
                         started="2026-09-28T01:00:00Z")
    goods = [session(f"a-good-{k}", True, [("PR 9 fix round 1", brief_f, [("Read", {"file_path": fx})])],
                     started=f"2026-09-28T0{k + 2}:00:00Z", where=later) for k in range(3)]
    buf = io.StringIO()
    with contextlib.redirect_stdout(buf):
        report(goods + [early_miss], False)
    ok = "bar: FAIL" in buf.getvalue()
    print(("PASS" if ok else "FAIL"), "the earliest treated session's miss fails the bar however its path sorts")
    fails += not ok
    buf = io.StringIO()
    with contextlib.redirect_stdout(buf):
        report(goods, False)
    ok = "bar: PASS" in buf.getvalue()
    print(("PASS" if ok else "FAIL"), "three treated sessions whose every read is clean pass")
    fails += not ok
    late = session("a-late", True, [("PR 9 fix round 2", brief_f, [("Bash", {"command": "git status --short"}),
                   ("Read", {"file_path": fx}), ("Edit", {"file_path": "a.java"})])], started="2026-09-28T09:00:00Z", where=later)
    buf = io.StringIO()
    with contextlib.redirect_stdout(buf):
        report(goods + [late], False)
    ok = "bar: needs a hand check" in buf.getvalue() and "bar: PASS" not in buf.getvalue()
    print(("PASS" if ok else "FAIL"), "a whole read that came after another call goes to a hand check, not to PASS")
    fails += not ok
    never = session("a-never", True, [("PR 9 fix round 2", brief_f, [("Read", {"file_path": fx, "limit": 20})])],
                    started="2026-09-28T10:00:00Z", where=later)
    buf = io.StringIO()
    with contextlib.redirect_stdout(buf):
        report(goods + [never], False)
    ok = "bar: FAIL" in buf.getvalue() and "never read the whole file" in buf.getvalue()
    print(("PASS" if ok else "FAIL"), "a role file never read whole fails the bar without the act detector")
    fails += not ok
    # A result flagged as an error settles nothing: wave 2's `cat reviewer.md; echo ======` showed the whole
    # file before zsh failed on the separator. It keeps a spawn from FAIL and from PASS.
    flagged = session("a-flagged", True, [("PR 9 fix round 2", brief_f, [("Bash", {"command": f"cat {fx}; echo ======"}),
        ("__raw_result__", {"is_error": True, "content": "Exit code 1\n" + "x\n" * 87 + "(eval):1: ===== not found"}),
        ("Edit", {"file_path": "a.java"})])], started="2026-09-28T09:30:00Z", where=later)
    s_ = flagged["spawns"][0]
    ok = (s_["read"], s_["read_full_ever"], s_["read_clean"], s_["meets_b"]) == ("errored", True, False, False)
    print(("PASS" if ok else "FAIL"), f"a whole read whose result was flagged as an error is 'errored' and not clean: "
          f"{(s_['read'], s_['read_full_ever'], s_['read_clean'], s_['meets_b'])}")
    fails += not ok
    buf = io.StringIO()
    with contextlib.redirect_stdout(buf):
        report(goods + [flagged], False)
    out = buf.getvalue()
    ok = "bar: needs a hand check" in out and "flagged as an error" in out and "bar: FAIL" not in out
    print(("PASS" if ok else "FAIL"), "an errored whole read sends the bar to a hand check, not to FAIL")
    fails += not ok
    recovered = session("a-recovered", True, [("Verify PR 9 round 1", brief_v, [("Bash", {"command": f"cat {vf}"}),
        ("__raw_result__", {"is_error": True, "content": "cat: no such file"}), ("Read", {"file_path": vf}),
        ("Bash", {"command": "mvn -q"})])], where=later)
    s_ = recovered["spawns"][0]
    ok = (s_["read"], s_["read_clean"]) == ("full", True)
    print(("PASS" if ok else "FAIL"), f"an errored read followed by a clean one before any act is clean: "
          f"{(s_['read'], s_['read_clean'])}")
    fails += not ok
    pre_err = session("a-pre-errored", False, [("PR 9 fix round 1", "Findings r1-1. mvn. commit.",
        [("Bash", {"command": f"cat {fx}"}), ("__raw_result__", {"is_error": True, "content": "x"})])], where=later)
    buf = io.StringIO()
    with contextlib.redirect_stdout(buf):
        report(goods + [pre_err], False)
    ok = "known-negative: 1 pre-0.36 spawn(s), 1 meeting (a) or reading a role file" in buf.getvalue()
    print(("PASS" if ok else "FAIL"), "an untreated spawn's errored read of a role file is still a leak")
    fails += not ok
    # A preview of output too large to show is no read, at any point: three sessions reading only a preview FAIL.
    preview = ("__raw_result__", {"content": "<persisted-output>\nOutput too large (57.6KB). Full output saved to: /x"
                                            "\n\nPreview (first 2KB):\n…"})
    previewed = [session(f"a-preview-{k}", True, [("PR 9 fix round 1", brief_f, [("Bash", {"command": f"cat {fx}"}),
                 preview])], started=f"2026-09-28T1{k}:00:00Z", where=later) for k in range(3)]
    s_ = previewed[0]["spawns"][0]
    ok = (s_["read_full_ever"], s_["read_clean"]) == (False, False)
    buf = io.StringIO()
    with contextlib.redirect_stdout(buf):
        report(previewed, False)
    ok = ok and "bar: FAIL" in buf.getvalue() and "never read the whole file" in buf.getvalue()
    print(("PASS" if ok else "FAIL"), "a role file shown only as a preview is never read whole, so three such sessions FAIL")
    fails += not ok
    missing = session("a-missing", True, [("PR 9 fix round 1", brief_f, [("Bash", {"command": f"cat {fx}; true"}),
        ("__raw_result__", {"content": f"cat: {fx}: No such file or directory"})])], where=later)
    s_ = missing["spawns"][0]
    ok = (s_["read"], s_["read_clean"]) == ("errored", False)
    print(("PASS" if ok else "FAIL"), f"a cat of a missing role file that a chained no-op hid from the flag is errored, "
          f"not clean: {(s_['read'], s_['read_clean'])}")
    fails += not ok
    r = session("round-2-verifier-without-repairs", True,
                [("Verify PR 9 round 1", brief_v + " Repairs: none.", [("Read", {"file_path": vf})]),
                 ("Verify PR 9 round 2", brief_v, [("Read", {"file_path": vf})])])
    ok = r["spawns"][0]["items"].get("repairs") is None and r["spawns"][1]["items"].get("repairs") is False
    print(("PASS" if ok else "FAIL"), "a round-2 verifier brief without the earlier repairs is an item miss")
    fails += not ok
    r = session("pre-0.36", False, [("PR 9 fix round 1", "Findings r1-1. mvn. commit.", [("Edit", {"file_path": "a"})])])
    ok = r["treated"] is False and not r["spawns"][0]["meets_a"] and r["spawns"][0]["read"] == "none"
    print(("PASS" if ok else "FAIL"), "an untreated session is reported as pre-0.36 and reads no role file")
    fails += not ok
    reviewer_brief = ("Run pr-review on the pushed head. The last verifier report says the standalone passed; "
                      "the fixer will implement what you find.")
    for desc in ("PR 9 review round 2", "pr-harden round 3 review", "blocking-only confirm of merged head",
                 "PR 9 confirming round"):
        r = session("rev-" + re.sub(r"\W+", "-", desc), True, [(desc, reviewer_brief, [])])
        ok = r["spawns"] == [] and r["unclassified"] == []
        print(("PASS" if ok else "FAIL"), f"a reviewer ({desc!r}) is neither a fixer nor a verifier")
        fails += not ok
    r = session("silent-description", True, [("round 2 agent", "You are the fixer for round 2. " + brief_f, [])])
    ok = [s["role"] for s in r["spawns"]] == ["fixer"]
    print(("PASS" if ok else "FAIL"), "a silent description falls back to the brief's stated role")
    fails += not ok
    held = ["round 2 agent"]
    ran_review = [("Read", {"file_path": "/x/pr-harden/reviewer.md"}), ("Skill", {"skill": "pr-review", "args": "9"})]
    for desc, brief, calls, want, want_held, label in [
            ("PR 546 blocking-only round 3",
             "You are the round-3 reviewer of pull request #546 in openmrs/openmrs-module-chartsearchai. "
             "This round is BLOCKING-ONLY. " + reviewer_brief, ran_review, [], ["PR 546 blocking-only round 3"],
             "wave 1's reviewer, named only in its brief, is held although it ran pr-review"),
            ("round 2 agent", "You are a fresh agent acting on what the reviewer found in round 2. You are the "
             "fixer: implement r2-1.", ran_review, [], held,
             "a brief whose first role-naming clause names the reviewer is held, whatever comes after"),
            ("round 2 agent", "You are the reviewer for round 2 — a fresh fixer will implement what you find. "
             + reviewer_brief, ran_review, [], held, "a clause naming the reviewer and the fixer is held"),
            ("round 2 agent", "You are the fixer for round 2 of PR 546; the reviewer found two blockers. " + brief_f,
             ran_review, [], held, "a fixer's clause naming the reviewer is held"),
            ("round 2 agent", "You are a fresh agent implementing the reviewer findings for PR 9, round 2. " + brief_f,
             [], [], held, "a fixer whose brief names the reviewer is held"),
            ("round 2 agent", "You are the second agent in round 2; the reviewer ran first. " + brief_f, [], [], held,
             "'the reviewer ran first' does not make the spawn a reviewer"),
            ("round 2 agent", "You are an independent fixer for round 2. " + brief_f, [], ["fixer"], [],
             "'an ... fixer' is a fixer"),
            ("round 2 agent", "You are the reviewer's fixer for round 2. " + brief_f, [], ["fixer"], [],
             "'the reviewer's fixer' is a fixer"),
            ("round 2 agent", "You are the verifier's fixer for round 2. " + brief_f, [], ["fixer"], [],
             "'the verifier's fixer' is a fixer, where 83008d9 read a verifier"),
            ("round 2 agent", "You are the reviewer’s fixer for round 2. " + brief_f, [], ["fixer"], [],
             "a curly apostrophe is a possessive too"),
            ("round 2 agent", "You are the 'fixer' for round 2. " + brief_f, [], ["fixer"], [],
             "a role in straight single quotes is still a role"),
            ("round 2 agent", "You are the ‘fixer’ for round 2. " + brief_f, [], ["fixer"], [],
             "a role in curly single quotes is still a role"),
            ("round 2 agent", "You are the reviewer-appointed fixer for round 2. " + brief_f, [], ["fixer"], [],
             "a role before a hyphen is not a role"),
            ("round 2 agent", "You are the fixer-reviewer for round 2. " + brief_f, [], [], held,
             "'the fixer-reviewer' is held, where 83008d9 counted it as a fixer"),
            ("round 2 agent", "You are the verifier-fixer for round 2. " + brief_f, [], [], held,
             "a role after a hyphen is not a role either"),
            ("round 2 agent", "You are the FIXER for round 2. " + brief_f, [], ["fixer"], [],
             "a capitalised role is the same role"),
            ("round 2 agent", "You are the fıxer for round 2. " + brief_f, [], [], held,
             "a dotless-i 'fıxer' is not a role, so it is held rather than dropped"),
            ("round 2 agent", "You are the " + "a" * 38 + " fixer for round 2. " + brief_f, [], ["fixer"], [],
             "a role starting 40 characters in is stated"),
            ("round 2 agent", "You are the " + "a" * 39 + " fixer for round 2. " + brief_f, [], [], held,
             "a role starting 41 characters in is not"),
            ("round 2 agent", "You are the fixer for round 2; the verifier ran in round 1. " + brief_f, [], [], held,
             "a clause naming the fixer and the verifier is held for a hand check"),
            ("round 2 agent", "You are a fresh agent implementing the reviewer's findings as the fixer. " + brief_f,
             [], [], held, "a role past 40 characters is not stated, so the spawn is held"),
            ("round 2 agent", "You are the agent for fixer.md in round 2. Findings r2-1.", [], [], held,
             "a role file's name is not a role"),
            ("round 2 agent", "You are the agent for round 2. The fixer part: findings r2-1.", [], [], held,
             "a role after the clause's full stop is not stated"),
            ("round 2 agent", "You are the agent for round 2. You are the fixer. " + brief_f, [], ["fixer"], [],
             "a clause naming no role passes the decision to the next"),
            ("round 2 agent", "You are the fixer for round 2. You are the one the verifier waits on. " + brief_f,
             [], ["fixer"], [], "the first clause naming a role decides, not every clause"),
            ("round 2 agent", "x" * 400 + " You are the fixer for round 2.", [], [], held,
             "a role stated past the first 400 characters is not read")]:
        r = session("stated-" + re.sub(r"\W+", "-", label), True, [(desc, brief, calls)])
        ok = [s["role"] for s in r["spawns"]] == want and r["unclassified"] == want_held
        print(("PASS" if ok else "FAIL"), f"a brief's stated role: {label}")
        fails += not ok
    for desc in ("Verify the preview endpoint for PR 9", "Fix unconfirmed dose parsing", "Irrefutable fix for PR 9"):
        r = session("desc-" + re.sub(r"\W+", "-", desc), True, [(desc, brief_f, [])])
        ok = len(r["spawns"]) == 1 and r["unclassified"] == []
        print(("PASS" if ok else "FAIL"), f"a word that only contains 'review', 'refut' or 'confirm' is not one: {desc!r}")
        fails += not ok
    r = session("desc-preview-unplaced", True, [("Preview endpoint round 2", "Check the endpoint and report.", [])])
    ok = r["spawns"] == [] and r["unclassified"] == ["Preview endpoint round 2"]
    print(("PASS" if ok else "FAIL"), "a description that only contains 'review' and states no role is listed, not dropped")
    fails += not ok
    r = session("unrecognised", True, [("rebase PR 9 onto main", "Rebase the branch and push.", [])])
    ok = r["spawns"] == [] and r["unclassified"] == ["rebase PR 9 onto main"]
    print(("PASS" if ok else "FAIL"), "a spawn it cannot place is listed as unclassified, not dropped")
    fails += not ok
    for line, want in [
            ('- pr-harden §6 ("never a server that was already running when the run began") vs resolve-ticket', True),
            ("- pr-harden:FIX — the fixer's own worktree is not on the PR branch", True),
            ('- pr-harden:"VERIFY" — "never a server that was already running"', True),
            ("- pr-harden:step 6 — the verifier's procedure anticipates a mismatch", True),
            ("- pr-harden:FINISH — never deletes its own `pr-<n>-r<round>` refs", False),
            ("- pr-harden:COMMIT — an agent left the worktree on `pr-313-r1`", False),
            ('- pr-harden:State — "restore with `git checkout -- <path>`" was followed', False),
            ("- pr-harden:step 1 — records `reviewed_shas` and never compares them", False)]:
        ok = bool(FRICTION.search(line)) == want
        print(("PASS" if ok else "FAIL"), f"friction {'matches' if want else 'does not match'}: {line[:60]}")
        fails += not ok
    for cmd, want in [("git status --porcelain && git merge-base origin/main HEAD && git rev-parse HEAD", False),
                      ("git stash list && perl -Mstrict -wle 'print 1'", False),
                      ("git apply --check fix.patch", False),
                      ("git merge origin/main", True), ("git stash", True), ("perl -pi -e 's/a/b/' x.java", True),
                      ("sed -E -i 's/a/b/' x.java", True), ("cat > src/A.java <<'EOF'\nx\nEOF", True),
                      ("grep -c x a > /tmp/out.txt", False), ("mvn -q test 2>&1 | tail -3", False),
                      ("awk 'NR>=8697 && NR<=8760' docs/adr.md", False),
                      ("sed -n '/<pluginManagement>/,/<\\/pluginManagement>/p' pom.xml", False),
                      ("cat > /private/tmp/s/fix.py <<'PY'\nPath('a.java').write_text('x')\nPY", False),
                      ("python3 - <<'EOF'\nopen('a.java', 'w').write('x')\nEOF", True),
                      ("python3 -c \"from pathlib import Path; Path('a').write_text('x')\"", True),
                      ("SP=/private/tmp/s; mkdir -p $SP/orig; cp pom.xml $SP/orig/pom.xml", False),
                      ("D=src/main; cp /tmp/x.java $D/A.java", True)]:
        ok = writes(cmd) == want
        print(("PASS" if ok else "FAIL"), f"writes({cmd[:48]!r}) is {want}")
        fails += not ok
    r = session("scratch-script-run-before-reading", True, [("PR 9 fix round 2", brief_f,
        [("Bash", {"command": "cat > /private/tmp/s/fix.py <<'PY'\nPath('a.java').write_text('x')\nPY"}),
         ("Bash", {"command": "python3 /private/tmp/s/fix.py"}), ("Read", {"file_path": fx})])])
    ok = r["spawns"][0]["meets_b"] is False and r["spawns"][0]["first_act_at"] == 2
    print(("PASS" if ok else "FAIL"), "running a scratch script that writes is the fixer's first edit")
    fails += not ok
    round2 = [
        ("a read sent in the same message as the first command", [("Verify PR 9 round 1", brief_v,
            [("__parallel__", [("Read", {"file_path": vf}), ("Bash", {"command": "mvn -o clean install"})])])], (True, False)),
        ("the same read in its own earlier message", [("Verify PR 9 round 1", brief_v,
            [("Read", {"file_path": vf}), ("Bash", {"command": "mvn -o clean install"})])], (True, True)),
        ("cat | head -50 shows 50 lines", [("Verify PR 9 round 1", brief_v,
            [("Bash", {"command": f"cat {vf} | head -50"})])], (True, False)),
        ("cat -n | sed -n 95,160p shows a window", [("Verify PR 9 round 1", brief_v,
            [("Bash", {"command": f"cat -n {vf} | sed -n 95,160p"})])], (True, False)),
        ("cat into a file shows nothing", [("Verify PR 9 round 1", brief_v,
            [("Bash", {"command": f"cat {vf} > /tmp/copy.md"})])], (True, False)),
        ("cat | cat -n shows it all", [("Verify PR 9 round 1", brief_v,
            [("Bash", {"command": f"cat {vf} | cat -n"})])], (True, True)),
        ("cat with stderr silenced shows it all", [("Verify PR 9 round 1", brief_v,
            [("Bash", {"command": f"cat {vf} 2>/dev/null"})])], (True, True)),
        ("importing a scratch helper that writes is an edit", [("PR 9 fix round 2", brief_f,
            [("Bash", {"command": "cat > /tmp/s/rep.py <<'PY'\ndef rep(p):\n    Path(p).write_text('x')\nPY"}),
             ("Bash", {"command": "python3 - <<'PY'\nimport sys; sys.path.insert(0, '/tmp/s')\nfrom rep import rep\nrep('a.java')\nPY"}),
             ("Read", {"file_path": fx})])], (True, False)),
        ("a Write under /tmp is not an edit", [("PR 9 fix round 2", brief_f,
            [("Write", {"file_path": "/tmp/s/notes.md", "content": "x"}), ("Read", {"file_path": fx}),
             ("Edit", {"file_path": "a.java"})])], (True, True)),
    ]
    for label, spawns, (want_a, want_b) in round2:
        r = session(re.sub(r"\W+", "-", label), True, spawns)
        s_ = r["spawns"][0]
        ok = (s_["meets_a"], s_["meets_b"]) == (want_a, want_b)
        print(("PASS" if ok else "FAIL"), f"{label}: meets (a)={s_['meets_a']} (b)={s_['meets_b']}, read={s_['read']}")
        fails += not ok
    round3 = [
        ("awk NR>=1 && NR<=200 of a 142-line file", [("Verify PR 9 round 1", brief_v,
            [("Bash", {"command": f"awk 'NR>=1 && NR<=200' {vf}"}), ("Bash", {"command": "mvn -o clean install"})])], (True, True)),
        ("awk NR>=1 && NR<=50 is partial", [("Verify PR 9 round 1", brief_v,
            [("Bash", {"command": f"awk 'NR>=1 && NR<=50' {vf}"})])], (True, False)),
        ("a verifier's cat and build in one command", [("Verify PR 9 round 1", brief_v,
            [("Bash", {"command": f"cat {vf} && mvn -o clean install"})])], (True, False)),
        ("cd then cat, nothing else, is still only a read", [("Verify PR 9 round 1", brief_v,
            [("Bash", {"command": f"cd {base} && cat verifier.md"}), ("Bash", {"command": "mvn -q"})])], (True, True)),
        ("output too large to show is a preview, not a read", [("Verify PR 9 round 1", brief_v,
            [("Bash", {"command": f"cat {vf}"}),
             ("__raw_result__", {"content": "<persisted-output>\nOutput too large (57.6KB). Full output saved to: /x\n\nPreview (first 2KB):\n…"}),
             ("Bash", {"command": "mvn -q"})])], (True, False)),
        ("a Read that errored is not a read before acting", [("Verify PR 9 round 1", brief_v,
            [("Read", {"file_path": vf}), ("__raw_result__", {"is_error": True, "content": "File does not exist."}),
             ("Bash", {"command": "mvn -q"})])], (True, False)),
    ]
    for label, spawns, (want_a, want_b) in round3:
        r = session(re.sub(r"\W+", "-", label), True, spawns)
        s_ = r["spawns"][0]
        ok = (s_["meets_a"], s_["meets_b"]) == (want_a, want_b)
        print(("PASS" if ok else "FAIL"), f"{label}: meets (a)={s_['meets_a']} (b)={s_['meets_b']}, read={s_['read']}")
        fails += not ok
    for cmd, want in [("cd /tmp && rm -rf conceptsrc && unzip -q x.zip", False),
                      ("cd /private/tmp/s && cat > DeadReference.java <<'EOF'\nclass A {}\nEOF", False),
                      ("S=/private/tmp/s; cd $S && sed -i '' 's/a/b/' measure.py", False),
                      ('P=/private/tmp/s; mysqld --datadir=x > "$P/mariadb" 2>&1', False),
                      ('git diff > "/private/tmp/x/before.diff"', False),
                      ('SP=/private/tmp/s; mvn -q test | tee "$SP/build.log"', False),
                      ("cat > \"api/A.java\" <<'EOF'\nclass A {}\nEOF", True),
                      ("sed -i '' 's/a/b/' api/A.java", True),
                      ("cp api/A.java /private/tmp/s/A.orig 2>/dev/null", False),
                      ("python3 - <<'EOF'\np = 'api/src/A.java'\nPath(p).write_text('x')\nEOF", True),
                      ("python3 - <<'EOF'\nout = '/private/tmp/s/r.json'\nopen(out, 'w').write('x')\nEOF", False),
                      ("git checkout e4953cac -- api/A.java api/B.java", True),
                      ("python3 - \"$F\" <<'PY'\np = 'api/A.java'\nPath(p).write_text('x')\nPY", True)]:
        ok = writes(cmd, "/repo") == want
        print(("PASS" if ok else "FAIL"), f"writes({cmd[:46]!r}, cwd=/repo) is {want}")
        fails += not ok
    round4 = [
        ("a read through a variable", [("Verify PR 9 round 1", brief_v,
            [("Bash", {"command": f'F={vf}; cat "$F"'}), ("Bash", {"command": "mvn -q"})])], (True, True)),
        ("a comment-only command is not a verifier's act", [("Verify PR 9 round 1", brief_v,
            [("Bash", {"command": "# the brief first"}), ("Read", {"file_path": vf}), ("Bash", {"command": "mvn -q"})])], (True, True)),
        ("awk with strict bounds around the file", [("Verify PR 9 round 1", brief_v,
            [("Bash", {"command": f"awk 'NR>0 && NR<143' {vf}"}), ("Bash", {"command": "mvn -q"})])], (True, True)),
        ("a scratch unzip under /tmp before reading is not a fixer's edit", [("PR 9 fix round 2", brief_f,
            [("Bash", {"command": "cd /tmp && rm -rf conceptsrc && unzip -q x.zip"}), ("Read", {"file_path": fx}),
             ("Edit", {"file_path": "a.java"})])], (True, True)),
    ]
    for label, spawns, (want_a, want_b) in round4:
        r = session(re.sub(r"\W+", "-", label), True, spawns)
        s_ = r["spawns"][0]
        ok = (s_["meets_a"], s_["meets_b"]) == (want_a, want_b)
        print(("PASS" if ok else "FAIL"), f"{label}: meets (a)={s_['meets_a']} (b)={s_['meets_b']}, read={s_['read']}")
        fails += not ok
    for desc in ("Harden cycle 2 fixer", "Cycle 3 Phase 1 fixer", "Cycle 4 verification pass"):
        r = session("h-" + re.sub(r"\W+", "-", desc), True, [(desc, brief_f, [("Edit", {"file_path": "a"})])])
        ok = r["spawns"] == [] and r["unclassified"] == []
        print(("PASS" if ok else "FAIL"), f"harden's own agent ({desc!r}) is not pr-harden's fixer or verifier")
        fails += not ok
    ref = tmp / "refused.jsonl"
    with open(ref, "w") as fh:
        for ev in ({"type": "user", "message": {"content": [{"type": "text", "text": f"{BASE}{base}\n{POINTERS[0]}"}]}},
                   {"type": "assistant", "message": {"content": [{"type": "tool_use", "id": "toolu_r", "name": "Agent",
                    "input": {"description": "PR 9 fix round 1", "prompt": brief_f, "model": "haiku"}}]}},
                   {"type": "user", "message": {"content": [{"type": "tool_result", "tool_use_id": "toolu_r",
                    "is_error": True, "content": "blocked by hook"}]}}):
            fh.write(json.dumps(ev) + "\n")
    r = check_session(ref)
    ok = r["spawns"] == []
    print(("PASS" if ok else "FAIL"), "a spawn a hook refused is not a fixer that failed to read its file")
    fails += not ok
    for cmd, want in [("bash -n /tmp/s/runblock.sh", []), ("python3 /tmp/s/x.py a", ["/tmp/s/x.py"])]:
        ok = scripts_run(cmd) == want
        print(("PASS" if ok else "FAIL"), f"scripts_run({cmd!r}) is {want}")
        fails += not ok
    r = session("relative-script-run", True, [("PR 9 fix round 2", brief_f,
        [("Bash", {"command": "cat > /private/tmp/s/edit.py <<'PY'\nPath('api/A.java').write_text('x')\nPY"}),
         ("Bash", {"command": "cd /private/tmp/s && python3 edit.py"}), ("Read", {"file_path": fx})])])
    ok = r["spawns"][0]["first_act_at"] == 2
    print(("PASS" if ok else "FAIL"), "a scratch script run by its relative name after a cd is the fixer's first edit")
    fails += not ok
    missing = str(base / "missing.md")
    st, _, _, total, *_ = read_check(write_agent(tmp / "unknown-length.jsonl", [("Read", {"file_path": missing, "limit": 50})]),
                                 missing, "verifier")
    ok = st == "partial" and total is None
    print(("PASS" if ok else "FAIL"), f"a limited read of a file whose length is unknown is not full (read={st})")
    fails += not ok
    # ---- the second move: reviewer.md under pr-harden, refuter.md under resolve-ticket --------------
    rt_base, hd_base = tmp / "skills/resolve-ticket", tmp / "skills/harden"
    rt_base.mkdir(parents=True)
    hd_base.mkdir(parents=True)
    (base / "reviewer.md").write_text("x\n" * 44)
    (rt_base / "refuter.md").write_text("x\n" * 52)
    rv, rf = str(base / "reviewer.md"), str(rt_base / "refuter.md")
    rv_pointer = "are in `reviewer.md`\nin this skill's directory"   # re-wrapped, as SKILL.md carries it
    rf_pointer = "are in `refuter.md`\nin this skill's directory"

    def write_calls(path, calls):
        """As write_agent, with a distinct id per call, and ("__raw_result__", {...}) planting a raw result for
        the call before it."""
        with open(path, "w") as fh:
            for q, (name_, inp) in enumerate(calls):
                if name_ == "__raw_result__":
                    fh.write(json.dumps({"type": "user", "message": {"content": [dict(
                        {"type": "tool_result", "tool_use_id": f"c{q - 1}"}, **inp)]}}) + "\n")
                    continue
                fh.write(json.dumps({"type": "assistant", "message": {"content": [
                    {"type": "tool_use", "id": f"c{q}", "name": name_, "input": inp}]}}) + "\n")

    def session2(name, steps, started="2026-09-28T12:00:00Z"):
        """A planted session: ("load", skill, pointer or None) prints that skill's base directory, and
        ("spawn", description, brief, calls) spawns a subagent that makes those calls, in order."""
        path, sub, lines, k = tmp / f"m2-{name}.jsonl", tmp / f"m2-{name}" / "subagents", [], 0
        sub.mkdir(parents=True)
        for st in steps:
            if st[0] == "load":
                d = {"pr-harden": base, "resolve-ticket": rt_base, "harden": hd_base}[st[1]]
                lines.append({"type": "user", "timestamp": started, "message": {"content": [{"type": "text",
                              "text": f"{BASE}{d}\n\n# {st[1]} ... {st[2] or 'no pointer in this version'} ..."}]}})
                continue
            _, desc, prompt, calls = st
            tid = f"toolu_m2_{name}_{k}"
            lines.append({"type": "assistant", "message": {"content": [
                {"type": "tool_use", "id": tid, "name": "Agent", "input": {"description": desc, "prompt": prompt}}]}})
            (sub / f"agent-{k}.meta.json").write_text(json.dumps({"toolUseId": tid, "description": desc}))
            write_calls(sub / f"agent-{k}.jsonl", calls)
            k += 1
        with open(path, "w") as fh:
            for l in lines:
                fh.write(json.dumps(l) + "\n")
        return path

    rv_brief = f"Read {rv} first, then run pr-review. Round 1, head 3085ff02, base origin/main, ticket #9."
    rf_brief = f"Read {rf} first. Ticket #9 with its comments, the plan below, repo /x/worktrees/openmrs-9."
    t_rt, u_rt = ("load", "resolve-ticket", rf_pointer), ("load", "resolve-ticket", None)
    t_ph, u_ph = ("load", "pr-harden", POINTERS[0] + " " + rv_pointer), ("load", "pr-harden", POINTERS[0])
    hd = ("load", "harden", None)
    grep, review = ("Bash", {"command": "grep -n x api"}), ("Skill", {"skill": "pr-review", "args": "9"})
    for label, steps, want in [
            ("a refuter's clean read", [t_rt, ("spawn", "Refute plan for issue 9", rf_brief,
             [("Read", {"file_path": rf}), grep]), hd], ("refuter", True, True, True, True)),
            ("a refuter whose brief lacks the path", [t_rt, ("spawn", "Refute plan for issue 9",
             "Ticket #9, the plan, the repo.", [("Read", {"file_path": rf})]), hd], ("refuter", True, False, True, True)),
            ("a refuter that greps before it reads", [t_rt, ("spawn", "Refute plan for issue 9", rf_brief,
             [grep, ("Read", {"file_path": rf})]), hd], ("refuter", True, True, False, False)),
            ("a refuter's truncated read", [t_rt, ("spawn", "Refute plan for issue 9", rf_brief,
             [("Read", {"file_path": rf, "limit": 20})]), hd], ("refuter", True, True, False, False)),
            ("a refuter named only by its brief's 'refutation gate'", [t_rt, ("spawn", "Gate pass 2 on revised plan 9",
             "You are a REFUTATION GATE, pass 2. " + rf_brief, [("Read", {"file_path": rf})]), hd],
             ("refuter", True, True, True, True)),
            ("a pathless 'refutation gate' brief is still a refuter's", [t_rt, ("spawn", "Gate pass 2 on revised plan 9",
             "You are a REFUTATION GATE, pass 2. Ticket #9, the plan.", [("Read", {"file_path": rf})]), hd],
             ("refuter", True, False, True, True)),
            ("a reviewer's clean read before pr-review", [u_rt, hd, t_ph, ("spawn", "PR 9 review round 1", rv_brief,
             [("Read", {"file_path": rv}), review])], ("reviewer", True, True, True, True)),
            ("a reviewer that loads pr-review before it reads", [t_ph, ("spawn", "PR 9 review round 1", rv_brief,
             [review, ("Read", {"file_path": rv})])], ("reviewer", True, True, False, False)),
            ("a pre-0.37 reviewer", [u_ph, ("spawn", "PR 9 review round 1", "Run pr-review on PR 9.", [review])],
             ("reviewer", False, False, False, False))]:
        r = check_session2(session2(re.sub(r"\W+", "-", label), steps))
        got = [(s["role"], s["treated"], s["meets_a"], s["meets_b"], s["read_clean"]) for s in r["spawns"]]
        ok = got == [want]
        print(("PASS" if ok else "FAIL"), f"second move, {label}: {got}")
        fails += not ok
    for label, steps, window, want_odd in [
            ("a harden-named review after pr-harden loads is unplaced", [t_ph, ("spawn", "Phase 2 quality review",
             "Review the diff for quality.", [])], "reviewer", ["Phase 2 quality review"]),
            ("a brief-stated reviewer is unplaced", [t_ph, ("spawn", "PR 9 blocking-only round 3",
             "You are the round-3 reviewer of PR 9. " + rv_brief, [])], "reviewer", ["PR 9 blocking-only round 3"]),
            ("a brief that names refuter.md and no refutation is unplaced", [t_rt, ("spawn", "Plan gate pass 2",
             f"Read {rf} first. Ticket #9, the plan.", [("Read", {"file_path": rf})]), hd], "refuter", ["Plan gate pass 2"]),
            ("a Step 3 search agent is unplaced", [t_rt, ("spawn", "Find composed answer path",
             "Grep the repo for where ChartAnswer is built.", []), hd], "refuter", ["Find composed answer path"]),
            ("a refuter spawned after harden loads is outside Step 3's window", [t_rt, hd, ("spawn",
             "Refute plan for issue 9", rf_brief, [("Read", {"file_path": rf})])], "refuter", []),
            ("a fixer in pr-harden's window is the first move's", [t_ph, ("spawn", "PR 9 fix round 1", brief_f,
             [])], "reviewer", [])]:
        r = check_session2(session2(re.sub(r"\W+", "-", label), steps))
        ok = r["spawns"] == [] and r["unclassified"][window] == want_odd
        print(("PASS" if ok else "FAIL"), f"second move, {label}")
        fails += not ok
    mixed = check_session2(session2("mixed", [t_rt, ("spawn", "Refute plan for issue 9", rf_brief,
        [("Read", {"file_path": rf})]), hd, u_ph, ("spawn", "PR 9 review round 1", "Run pr-review on PR 9.", [review])]))
    ok = [(s["role"], s["treated"]) for s in mixed["spawns"]] == [("reviewer", False), ("refuter", True)] \
        or [(s["role"], s["treated"]) for s in mixed["spawns"]] == [("refuter", True), ("reviewer", False)]
    print(("PASS" if ok else "FAIL"), "second move, a session treated for the refuter and not the reviewer is both")
    fails += not ok
    it = check_session2(session2("items", [t_ph, ("spawn", "PR 9 review round 1", f"Read {rv} first.",
        [("Read", {"file_path": rv})])]))["spawns"][0]["items"]
    ok = it == {"round": False, "head": False, "base": False, "ticket origin": False, "ticket": False}
    print(("PASS" if ok else "FAIL"), f"second move, a reviewer brief carrying only the path misses every item: {it}")
    fails += not ok
    it = check_session2(session2("items-full", [t_ph, ("spawn", "PR 9 review round 1", rv_brief +
        " The run started from ticket #9: gh issue view 9 --json title,body,comments.",
        [("Read", {"file_path": rv})])]))["spawns"][0]["items"]
    ok = it == {"round": True, "head": True, "base": True, "ticket origin": True, "ticket": True}
    print(("PASS" if ok else "FAIL"), f"second move, a reviewer brief carrying every item has them all: {it}")
    fails += not ok
    it = check_session2(session2("items-pr-number", [t_ph, ("spawn", "PR 9 review round 1",
        f"Read {rv} first. Review PR #9, round 1, head 3085ff02, base origin/main.", [("Read", {"file_path": rv})])]))
    it = it["spawns"][0]["items"]
    ok = not it["ticket"] and not it["ticket origin"]
    print(("PASS" if ok else "FAIL"), f"second move, a PR number is not the ticket, nor its origin: {it}")
    fails += not ok
    for text, want in [("Resolves O3-1234.", True), ("Resolves TRUNK-6429.", True), ("Write it as UTF-8.", False)]:
        got = items2("reviewer", text)["ticket"]
        ok = got == want
        print(("PASS" if ok else "FAIL"), f"second move, {text!r} names a JIRA ticket: {got}")
        fails += not ok
    # s1: a spawn the first move places as a fixer, a verifier or harden's that ran pr-review is held.
    cat_review = ("Bash", {"command": "cat ~/.claude/skills/pr-review/SKILL.md"})
    for label, desc, brief, calls, want_odd in [
            ("a 'verification round' that ran pr-review is held", "PR 9 blocking-only verification round",
             "You are running a BLOCKING-ONLY verification round. Run pr-review on PR 9.", [review], True),
            ("a 'cycle' pass that ran pr-review is held", "PR 9 cycle 3 blocking-only pass",
             "Run pr-review on PR 9.", [review], True),
            ("a 'fix' check that showed pr-review's SKILL.md is held", "Check the fix for PR 9 round 2",
             "Review what the fixer did.", [cat_review], True),
            ("a brief-placed fixer that ran pr-review is held", "PR 9 round 3",
             "You are the second agent after the fixer; run pr-review on PR 9.", [review], True),
            ("a verifier that never ran pr-review is the first move's", "Verify PR 9 round 1", brief_v,
             [("Bash", {"command": "curl -s localhost:8081"})], False)]:
        r = check_session2(session2("s1-" + re.sub(r"\W+", "-", label), [t_ph, ("spawn", desc, brief, calls)]))
        ok = r["spawns"] == [] and r["unclassified"]["reviewer"] == ([desc] if want_odd else [])
        print(("PASS" if ok else "FAIL"), f"second move, {label}")
        fails += not ok
    # s6: for a reviewer, a Read of another file first is an act, so the read of its own file is not clean.
    r = check_session2(session2("s6-read-other-first", [t_ph, ("spawn", "PR 9 review round 1", rv_brief,
        [("Read", {"file_path": "/x/.claude/skills/pr-review/SKILL.md"}), ("Read", {"file_path": rv})])]))
    got = [(s["meets_b"], s["read_clean"], s["read_full_ever"]) for s in r["spawns"]]
    ok = got == [(False, False, True)]
    print(("PASS" if ok else "FAIL"), f"second move, a reviewer that reads another file first is not clean: {got}")
    fails += not ok
    # s4: a second resolve-ticket load in one session opens a second Step 3, which is the refuter's alone.
    r = check_session2(session2("s4-two-tickets", [t_rt, ("spawn", "Refute plan for issue 9", rf_brief,
        [("Read", {"file_path": rf})]), hd, t_ph, ("spawn", "PR 9 review round 1", rv_brief,
        [("Read", {"file_path": rv}), review]), t_rt, ("spawn", "Refute plan for issue 10", "Ticket #10, the plan.",
        [("Read", {"file_path": rf})])]))
    got = sorted((s["role"], s["description"], s["meets_a"]) for s in r["spawns"])
    ok = got == [("refuter", "Refute plan for issue 10", False), ("refuter", "Refute plan for issue 9", True),
                 ("reviewer", "PR 9 review round 1", True)]
    print(("PASS" if ok else "FAIL"), f"second move, a second ticket's refuter is measured as a refuter: {got}")
    fails += not ok
    # s3: pr-harden closes Step 3's window too, when harden never loads.
    r = check_session2(session2("s3-pr-harden-closes", [t_rt, t_ph, ("spawn", "Refute plan for issue 9", rf_brief,
        [("Read", {"file_path": rf})])]))
    ok = [s["role"] for s in r["spawns"] if s["role"] == "refuter"] == [] and r["unclassified"]["refuter"] == []
    print(("PASS" if ok else "FAIL"), "second move, pr-harden loading closes Step 3's window as harden does")
    fails += not ok

    def bar2(paths, want):
        buf = io.StringIO()
        with contextlib.redirect_stdout(buf):
            report2([check_session2(p) for p in paths], False)
        return want in buf.getvalue(), buf.getvalue()

    def rt_run(name, calls, brief=rf_brief, desc="Refute plan for issue 9", started="2026-09-28T12:00:00Z"):
        return session2(name, [t_rt, ("spawn", desc, brief, calls), hd], started)

    clean_rt = [rt_run(f"rt-clean-{i}", [("Read", {"file_path": rf}), grep], started=f"2026-09-28T12:0{i}:00Z")
                for i in range(3)]
    pre_rt = session2("rt-pre", [u_rt, ("spawn", "Refute plan for issue 8", "Break the plan.", [grep]), hd])
    for label, extra, want in [
            ("three clean refuter sessions PASS refuter.md", [], "bar (refuter.md): PASS"),
            ("a refuter that greps first sends refuter.md to a hand check",
             [rt_run("rt-late", [grep, ("Read", {"file_path": rf})])], "bar (refuter.md): needs a hand check"),
            ("a refuter brief without the path FAILs refuter.md",
             [rt_run("rt-nopath", [("Read", {"file_path": rf})], brief="Ticket #9, the plan, the repo.")],
             "bar (refuter.md): FAIL"),
            ("an unplaced spawn in a treated Step 3 holds refuter.md",
             [session2("rt-odd", [t_rt, ("spawn", "Blind adjudicator 1", "Adjudicate the items.", []), hd])],
             "bar (refuter.md): not decidable yet — 1 treated spawn(s) are unclassified"),
            ("refuter.md is called apart from reviewer.md", [], "bar (reviewer.md): not decidable yet — 0 of the 3"),
            ("the pre-0.23 refuter is the known-negative, and reads nothing", [pre_rt],
             "known-negative: 1 untreated refuter spawn(s), 0 meeting (a) or reading refuter.md")]:
        ok, out = bar2(clean_rt + extra, want)
        print(("PASS" if ok else "FAIL"), f"second move, {label}")
        fails += not ok
    clean_rv = [session2(f"rv-clean-{i}", [t_ph, ("spawn", "PR 9 review round 1", rv_brief,
                [("Read", {"file_path": rv}), review])], started=f"2026-09-28T13:0{i}:00Z") for i in range(3)]
    ok, out = bar2(clean_rv, "bar (reviewer.md): PASS")
    print(("PASS" if ok else "FAIL"), "second move, three clean reviewer sessions PASS reviewer.md")
    fails += not ok
    ok, out = bar2(clean_rv + [session2("rv-late", [t_ph, ("spawn", "PR 9 review round 2", rv_brief,
                   [review, ("Read", {"file_path": rv})])])], "bar (reviewer.md): needs a hand check")
    print(("PASS" if ok else "FAIL"), "second move, a reviewer that loads pr-review first sends reviewer.md to a hand check")
    fails += not ok
    rv_flagged = session2("rv-flagged", [t_ph, ("spawn", "PR 9 review round 2", rv_brief, [
        ("Bash", {"command": f"cat {rv}; echo ======; ls /x/pr-review/"}),
        ("__raw_result__", {"is_error": True, "content": "Exit code 1\n" + "x\n" * 44 + "(eval):1: ===== not found"}),
        review])], started="2026-09-28T13:30:00Z")
    s_ = check_session2(rv_flagged)["spawns"][0]
    ok, out = bar2(clean_rv + [rv_flagged], "bar (reviewer.md): needs a hand check")
    ok = ok and (s_["read"], s_["read_full_ever"], s_["read_clean"]) == ("errored", True, False) \
        and "bar (reviewer.md): FAIL" not in out and "flagged as an error" in out
    print(("PASS" if ok else "FAIL"), "second move, wave 2's zsh-flagged whole read sends reviewer.md to a hand check, "
          "not to FAIL")
    fails += not ok
    pre_rt_err = session2("rt-pre-errored", [u_rt, ("spawn", "Refute plan for issue 8", "Break the plan.",
        [("Bash", {"command": f"cat {rf}"}), ("__raw_result__", {"is_error": True, "content": "x"})]), hd])
    ok, out = bar2(clean_rt + [pre_rt_err], "known-negative: 1 untreated refuter spawn(s), 1 meeting (a) or reading "
                                            "refuter.md — the detector is wrong")
    print(("PASS" if ok else "FAIL"), "second move, an untreated refuter's errored read of refuter.md is still a leak")
    fails += not ok
    rv_previewed = [session2(f"rv-preview-{k}", [t_ph, ("spawn", "PR 9 review round 1", rv_brief, [("Bash", {"command": f"cat {rv}"}),
                    ("__raw_result__", {"content": "<persisted-output>\nOutput too large (57.6KB). Full output saved to: /x"}),
                    review])], started=f"2026-09-28T17:0{k}:00Z") for k in range(3)]
    ok, out = bar2(rv_previewed, "bar (reviewer.md): FAIL")
    print(("PASS" if ok else "FAIL"), "second move, three reviewers shown reviewer.md only as a preview FAIL it")
    fails += not ok
    for line, want in [
            ("- pr-harden:REVIEW — the brief named no base", True),
            ('- pr-harden §1 ("re-derive the merged result") vs harden', True),
            ("- resolve-ticket Step 3 — question 6 asked for no count", True),
            ("- the reviewer re-raised a settled finding", True),
            ("- the refuter cited no line of code", True),
            ("- the refutation gate spawned a subagent of its own", True),
            ("- pr-harden:FINISH — never deletes its own `pr-<n>-r<round>` refs", False),
            ("- resolve-ticket:Step 8 — the draft PR body named the wrong ticket", False)]:
        ok = bool(FRICTION2.search(line)) == want
        print(("PASS" if ok else "FAIL"), f"FRICTION2 on {line[:60]!r} is {want}")
        fails += not ok
    # s2: the two files' bars are called apart, over the right sessions.
    both = [session2(f"s2-both-{i}", [t_rt, ("spawn", "Refute plan for issue 9", rf_brief, [("Read", {"file_path": rf}),
            grep]), hd, t_ph], started=f"2026-09-28T14:0{i}:00Z") for i in range(3)]
    ok, out = bar2(both, "bar (refuter.md): PASS")
    ok = ok and "bar (reviewer.md): not decidable yet — 0 of the 3" in out \
        and "reviewer.md: 3 session(s) that loaded pr-harden; 3 treated, 0 of them with a reviewer spawn" in out
    print(("PASS" if ok else "FAIL"), "second move, three refuter sessions that load pr-harden do not count for reviewer.md")
    fails += not ok
    pre_rv = [session2(f"s2-pre-rv-{i}", [t_rt, ("spawn", "Refute plan for issue 9", rf_brief,
              [("Read", {"file_path": rf})]), hd, u_ph, ("spawn", "PR 9 review round 1", "Run pr-review on PR 9.",
              [review])], started=f"2026-09-28T15:0{i}:00Z") for i in range(3)]
    ok, out = bar2(pre_rv, "reviewer.md: 3 session(s) that loaded pr-harden; 0 treated")
    ok = ok and "bar (reviewer.md): not decidable yet — 0 of the 3" in out and "bar (refuter.md): PASS" in out
    print(("PASS" if ok else "FAIL"), "second move, a session treated for refuter.md is not treated for reviewer.md")
    fails += not ok
    only_rt = [session2(f"s2-only-rt-{i}", [t_rt, ("spawn", "Refute plan for issue 9", rf_brief,
               [("Read", {"file_path": rf})]), hd], started=f"2026-09-28T16:0{i}:00Z") for i in range(2)]
    ok, out = bar2(only_rt + clean_rv, "reviewer.md: 3 session(s) that loaded pr-harden; 3 treated")
    print(("PASS" if ok else "FAIL"), "second move, a session that never loads pr-harden is not one of reviewer.md's")
    fails += not ok
    # s3's other gaps: a leak is reported, and an untreated session's unplaced spawn does not hold PASS.
    leak = session2("s3-leak", [u_rt, ("spawn", "Refute plan for issue 8", rf_brief, [("Read", {"file_path": rf})]), hd])
    ok, out = bar2(clean_rt + [leak], "known-negative: 1 untreated refuter spawn(s), 1 meeting (a) or reading "
                                     "refuter.md — the detector is wrong")
    print(("PASS" if ok else "FAIL"), "second move, an untreated refuter that names and reads refuter.md is a leak")
    fails += not ok
    odd_pre = session2("s3-odd-pre", [u_rt, ("spawn", "Blind adjudicator 1", "Adjudicate the items.", []), hd])
    ok, out = bar2(clean_rt + [odd_pre], "bar (refuter.md): PASS")
    print(("PASS" if ok else "FAIL"), "second move, an untreated session's unplaced spawn does not hold refuter.md")
    fails += not ok
    # s5: the slower comparison splits each record by its own session, not by its date.
    recs = tmp / "m2-records"
    recs.mkdir()
    t_sess, u_sess = clean_rt[0], pre_rt
    (recs / "2026-09-28-a.md").write_text(f"# resolve-ticket · o/r · #1 · 2026-09-28\n\ntranscript: {t_sess}\n\n"
                                          f"{SECTION}\n- the refuter cited no line of code\n")
    (recs / "2026-09-28-b.md").write_text(f"# resolve-ticket · o/r · #2 · 2026-09-28\n\ntranscript: {u_sess}\n\n"
                                          f"{SECTION}\n- none\n")
    (recs / "2026-09-20-c.md").write_text(f"# resolve-ticket · o/r · #3 · 2026-09-20\n\n{SECTION}\n- none\n")
    (recs / "2026-09-29-d.md").write_text(f"# resolve-ticket · o/r · #4 · 2026-09-29\n\n{SECTION}\n- none\n")
    buf = io.StringIO()
    with contextlib.redirect_stdout(buf):
        baseline2(recs, None, None)
    out = buf.getvalue()
    ok = ("records before the second move with the section: 2; naming" in out
          and "records after the second move with the section: 1; naming pr-harden REVIEW, resolve-ticket Step 3, "
              "the reviewer or the refuter: 1 (100%)" in out
          and "records that cannot be placed with the section: 1;" in out)
    print(("PASS" if ok else "FAIL"), "second move, a record is before or after by its own session, and dated "
          "after the move with no transcript it is unknown")
    fails += not ok
    recs_tilde = tmp / "m2-records-tilde"
    recs_tilde.mkdir()
    (recs_tilde / "2026-09-28-e.md").write_text(f"# resolve-ticket · o/r · #5 · 2026-09-28\n\n"
                                                f"transcript: ~/{clean_rt[1].name}\n\n{SECTION}\n- none\n")
    real_home, buf = os.environ.get("HOME"), io.StringIO()
    os.environ["HOME"] = str(tmp)   # the record spells its transcript with ~, as 125 of the 133 on disk do
    try:
        with contextlib.redirect_stdout(buf):
            baseline2(recs_tilde, None, None)
    finally:
        if real_home is None:
            del os.environ["HOME"]
        else:
            os.environ["HOME"] = real_home
    ok = "records after the second move with the section: 1;" in buf.getvalue()
    print(("PASS" if ok else "FAIL"), "second move, a record's ~-spelled transcript is found")
    fails += not ok
    print("selftest:", "OK" if not fails else f"{fails} FAILURE(S)")
    return 1 if fails else 0


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("sessions", nargs="*")
    ap.add_argument("--json", action="store_true")
    ap.add_argument("--selftest", action="store_true")
    ap.add_argument("--baseline", action="store_true")
    ap.add_argument("--before")
    ap.add_argument("--after")
    ap.add_argument("--records", default=str(RECORDS))
    ap.add_argument("--second-move", action="store_true",
                    help="measure pr-harden 0.37.0's reviewer.md and resolve-ticket 0.23.0's refuter.md instead")
    a = ap.parse_args()
    if a.selftest:
        return selftest(a)
    if a.baseline:
        if a.second_move:
            return baseline2(Path(a.records), a.before, a.after)
        return baseline(Path(a.records), a.before, a.after)
    if a.second_move:
        return report2([check_session2(p) for p in sessions2(a.sessions)], a.json)
    return report([check_session(p) for p in sessions(a.sessions)], a.json)


if __name__ == "__main__":
    sys.exit(main())
