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
    role-file-check.py --selftest
"""
import argparse, contextlib, io, json, re, shlex, sys, tempfile
from pathlib import Path

PROJECTS = Path.home() / ".claude/projects"
RECORDS = Path.home() / ".claude/skill-lessons"
BASE = "Base directory for this skill: "
POINTERS = ("`fixer.md` in this skill's directory", "`verifier.md` in this skill's directory")
EDITS = {"Edit", "Write", "MultiEdit", "NotebookEdit"}
ROLE_FILE = {"fixer": "fixer.md", "verifier": "verifier.md"}
ROLE_WORD = re.compile(r"(?<!-)\b(verifier|fixer|reviewer)\b(?!-|['’]s\b|\.md\b)", re.I | re.A)
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
    if re.search(r"(?<!pr-)\bharden\b|\bcycle\b|\bphase\b", desc, re.I):
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
    read only counts if its turn comes before the act's."""
    if not agent_jsonl or not agent_jsonl.exists():
        return "no-transcript", None, None, None, None, False
    reads, total, act, act_turn, n, turn, last_msg, mentioned, scripts = [], None, None, None, 0, 0, object(), False, {}
    void = set()  # reads whose result was an error, or a preview of output too large to show
    first_other_turn = None  # the turn of the first call that is not a pure read
    for seq, e in enumerate(events(agent_jsonl)):
        for c in blocks(e):
            if c.get("type") == "tool_result":
                body = json.dumps(c.get("content"))
                if c.get("is_error") or "persisted-output" in body or "Output too large" in body:
                    void.add(c.get("tool_use_id"))
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
            if act is None and acting:
                act, act_turn = n, turn
            pure_read = name in ("Read", "Grep", "Glob", "LS") or (
                name == "Bash" and got is not None and not does_more_than_read(cmd, role_path, e.get("cwd")))
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
        if tid in void:
            continue
        so_far += got
        if covered(so_far, total):
            full_at = k
            break
    ever_at, ever_turn, so_far = None, None, []   # a full read at any point, and how early it came
    for k, t, got, tid in reads:
        if tid in void:
            continue
        so_far += got
        if covered(so_far, total):
            ever_at, ever_turn = k, t
            break
    clean = ever_turn is not None and (first_other_turn is None or ever_turn < first_other_turn)
    state = "full" if full_at is not None else "partial" if mentioned else "none"
    return state, full_at, act, total, ever_at, clean


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
        if role_path in prompt:
            path_kind = "absolute"
        elif re.search(r"~/\S*" + re.escape(ROLE_FILE[role]), prompt):
            path_kind = "tilde"
        elif ROLE_FILE[role] in prompt:
            path_kind = "bare"
        else:
            path_kind = "missing"
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
    leaks = [s for s in untreated if s["meets_a"] or s["read"] in ("full", "partial")]
    print(f"known-negative: {len(untreated)} pre-0.36 spawn(s), {len(leaks)} meeting (a) or reading a role file"
          + (" — the detector is wrong" if leaks else ""))
    odd = [d for r in treated for d in r["unclassified"]]
    print(f"treated spawns that are neither a reviewer nor recognisably a fixer or verifier: {len(odd)}"
          + (" — read these by hand: " + "; ".join(odd[:8]) if odd else ""))
    # The bar decides only what the transcript settles. FAIL: the brief did not name the file, or the
    # file was never read whole. PASS: every read was CLEAN — whole before the subagent's first call
    # that is not a pure read, so nothing can have acted first and no act detection is needed. A whole
    # read that came after other calls is LATE: it is listed with the act detector's verdict as advice,
    # and read by hand, because five reviews of that detector kept finding shell shapes it misjudged.
    failed = [(r["session"], s) for r in treated for s in r["spawns"] if not s["meets_a"] or not s["read_full_ever"]]
    late = [(r["session"], s) for r in treated for s in r["spawns"]
            if s["meets_a"] and s["read_full_ever"] and not s["read_clean"]]
    if failed:
        sess, sp = failed[0]
        why = "its brief does not name the file" if not sp["meets_a"] else "it never read the whole file"
        print(f"bar: FAIL — {len(failed)} treated spawn(s) missed; the first, {sp['description']!r} in {sess}: {why}")
    elif late:
        print(f"bar: needs a hand check — {len(late)} treated spawn(s) did not read the whole file before the "
              "message holding their first call that is not a pure read:")
        for sess, sp in late:
            verdict = "before" if sp["meets_b"] else "after"
            print(f"    {sp['description']!r}: whole read at call {sp['whole_read_at']}; the act detector says it came "
                  f"{verdict} the first act (call {sp['first_act_at']}) — read {sess} to decide")
    elif odd:
        print(f"bar: not decidable yet — {len(odd)} treated spawn(s) are unclassified and must be read by hand first")
    elif len(with_spawns) < 3:
        print(f"bar: not decidable yet — {len(with_spawns)} of the 3 treated sessions with a spawn exist, no miss so far")
    else:
        print(f"bar: PASS — {len(with_spawns)} treated sessions with a spawn, every read clean and every brief naming the file")
    return 0


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
        ("a Read that errored is not a read", [("Verify PR 9 round 1", brief_v,
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
    a = ap.parse_args()
    if a.selftest:
        return selftest(a)
    if a.baseline:
        return baseline(Path(a.records), a.before, a.after)
    return report([check_session(p) for p in sessions(a.sessions)], a.json)


if __name__ == "__main__":
    sys.exit(main())
