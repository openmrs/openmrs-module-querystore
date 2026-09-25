#!/usr/bin/env python3
"""List the prose a change should be re-read against, so a sweep starts from a list.

usage: prose-candidates.py [-C DIR] [--json] BASE [HEAD]
       prose-candidates.py --selftest

For the change merge-base(BASE, HEAD)..HEAD (HEAD defaults to HEAD; the committed state, never the
working tree):
  A  lines of prose the change ADDED that carry a universal, the words harden's anti-pattern
     *Don't publish a claim a later cycle must re-measure* names, plus every/none/nothing/always;
  B  UNCHANGED prose lines in HEAD's tree that name a SUBJECT of the change: the method and class
     enclosing a changed code hunk, a name a changed line declares, or a dotted or camelCase key in a
     string literal on a changed line. A subject named by more than TOO_COMMON prose lines is
     reported with its count instead of listed.
Prose is a comment or docstring in code, and markdown or text outside a fence. A listed line is a
place to READ with the change in mind, not a finding.
"""
import json
import os
import re
import subprocess
import sys
import tempfile

TOO_COMMON = 10
DOC_EXT = {".md", ".markdown", ".txt", ".adoc", ".rst"}
CLIKE_EXT = {".java", ".kt", ".kts", ".groovy", ".scala", ".js", ".jsx", ".ts", ".tsx", ".c", ".h",
             ".cc", ".cpp", ".hpp", ".go", ".rs", ".swift", ".css", ".scss", ".less"}
HASH_EXT = {".sh", ".bash", ".zsh", ".rb", ".pl", ".yml", ".yaml", ".properties", ".toml", ".cfg",
            ".ini", ".conf"}
XML_EXT = {".xml", ".html", ".htm", ".xsd", ".vm", ".jsp"}
UNIVERSAL = re.compile(r"\b(every|all|only|never|none|nothing|always|any|exactly|cannot|the whole)\b",
                       re.I)
TOKEN = re.compile(r"[A-Za-z_][A-Za-z0-9_]*")
STRING = re.compile(r'"((?:[^"\\]|\\.)*)"')
DOTTED = re.compile(r"[A-Za-z_][A-Za-z0-9_]*(?:\.[A-Za-z_][A-Za-z0-9_]*)+")
CLASS_DECL = re.compile(r"\b(?:class|interface|enum|record)\s+([A-Z][\w$]*)")
JAVA_DECL = re.compile(r"^\s*(?:@[\w.]+(?:\([^)]*\))?\s+)*(?:(?:public|protected|private|static|final|"
                       r"abstract|synchronized|default|native)\s+)*(?:<[^>]*>\s*)?(?:[\w.$]+(?:<[^()]*>)?"
                       r"(?:\[\])*\s+)?([A-Za-z_$][\w$]*)\s*\(")
PY_DECL = re.compile(r"^\s*(?:async\s+)?(?:def|class)\s+([A-Za-z_]\w*)")
FIELD_DECL = re.compile(r"^\s*(?:(?:public|protected|private|static|final|volatile|transient)\s+)+"
                        r"[\w.$<>\[\], ?]+?\s+([A-Za-z_$][\w$]*)\s*(?:=|;)")
ASSIGN_DECL = re.compile(r"^([A-Z][A-Z0-9_]+)\s*=")
SH_DECL = re.compile(r"^\s*(?:function\s+)?([A-Za-z_][\w-]*)\s*\(\)\s*\{?")
KEYWORDS = {"if", "for", "while", "switch", "catch", "return", "new", "else", "throw", "try",
            "synchronized", "super", "this", "assert", "do", "case", "yield"}
STOP = {"String", "Integer", "Boolean", "Object", "Override", "Deprecated", "Exception",
        "RuntimeException", "IllegalStateException", "IllegalArgumentException", "StringBuilder",
        "ArrayList", "HashMap", "HashSet", "LinkedHashMap", "LinkedHashSet", "Collections",
        "Arrays", "Objects", "Optional", "assertEquals", "assertTrue", "assertFalse", "assertNull",
        "assertNotNull", "assertThat", "hashCode", "toString", "equals", "isEmpty", "valueOf",
        "getClass", "forEach", "toList", "Collectors", "LoggerFactory", "getLogger", "println",
        "self", "None", "True", "False", "print", "range", "return", "import", "static", "final",
        "public", "private", "protected", "class", "void", "null", "true", "false", "else", "elif",
        "with", "from", "lambda", "yield", "async", "await", "while", "break", "continue"}


def _kind(path: str, first_line: str) -> str:
    base = os.path.basename(path)
    ext = os.path.splitext(base)[1].lower()
    if ext in DOC_EXT:
        return "doc"
    if ext in CLIKE_EXT:
        return "clike"
    if ext == ".py" or (not ext and first_line.startswith("#!") and "python" in first_line):
        return "py"
    if ext in HASH_EXT or base in ("Makefile", "Dockerfile") or (
            not ext and first_line.startswith("#!")):
        return "hash"
    if ext in XML_EXT:
        return "xml"
    return ""


def prose_map(path: str, text: str) -> dict:
    """{line number: the prose on that line}, for every line that carries some."""
    lines = text.splitlines()
    kind = _kind(path, lines[0] if lines else "")
    out = {}
    if kind == "doc":
        fence = None
        for i, line in enumerate(lines, 1):
            s = line.strip()
            marker = s[:3]
            if marker in ("```", "~~~"):
                fence = None if fence == marker else (fence or marker)
                continue
            if fence is None and s:
                out[i] = s
        return out
    if not kind:
        return out
    block = None
    for i, line in enumerate(lines, 1):
        if i == 1 and line.startswith("#!"):
            continue
        seg, j, n, quote = [], 0, len(line), None
        while j < n:
            if block:
                end = line.find(block, j)
                if end == -1:
                    seg.append(line[j:])
                    j = n
                else:
                    seg.append(line[j:end])
                    j, block = end + len(block), None
                continue
            c = line[j]
            if quote:
                if c == "\\":
                    j += 2
                    continue
                if c == quote:
                    quote = None
                j += 1
                continue
            if kind == "py" and line.startswith(('"""', "'''"), j):
                block = line[j:j + 3]
                j += 3
                continue
            if kind == "clike" and line.startswith("/*", j):
                block, j = "*/", j + 2
                continue
            if kind == "xml" and line.startswith("<!--", j):
                block, j = "-->", j + 4
                continue
            if kind == "clike" and line.startswith("//", j):
                seg.append(line[j + 2:])
                break
            if kind in ("py", "hash") and c == "#":
                seg.append(line[j + 1:])
                break
            if c in "\"'`" and kind != "xml":
                quote = c
            j += 1
        text_ = " ".join(x.strip() for x in seg).strip()
        if kind == "clike":
            text_ = re.sub(r"^\*+\s?", "", text_).strip()
        if text_ and text_ not in ("*", "/"):
            out[i] = text_
    return out


def _git(repo: str, *args: str) -> str:
    return subprocess.run(["git", "-C", repo, *args], check=True, capture_output=True,
                          text=True).stdout


def _blob(repo: str, rev: str, path: str) -> str:
    r = subprocess.run(["git", "-C", repo, "show", f"{rev}:{path}"], capture_output=True)
    return r.stdout.decode("utf-8", "replace") if r.returncode == 0 else ""


def _tree(repo: str, rev: str) -> dict:
    """{path: text} for every tracked file at `rev` whose kind carries prose."""
    paths = [p for p in _git(repo, "ls-tree", "-r", "-z", "--name-only", rev).split("\0") if p]
    want = [p for p in paths if _kind(p, "#!python") or not os.path.splitext(p)[1]]
    proc = subprocess.Popen(["git", "-C", repo, "cat-file", "--batch"], stdin=subprocess.PIPE,
                            stdout=subprocess.PIPE)
    out = {}
    for p in want:
        proc.stdin.write(f"{rev}:{p}\n".encode())
        proc.stdin.flush()
        header = proc.stdout.readline().decode().split()
        if len(header) < 3 or header[1] != "blob":
            continue
        data = proc.stdout.read(int(header[2]) + 1)[:-1]
        if b"\0" in data[:8000] or len(data) > 4_000_000:
            continue
        text = data.decode("utf-8", "replace")
        if _kind(p, text.split("\n", 1)[0]):
            out[p] = text
    proc.stdin.close()
    proc.wait()
    return out


def _hunks(repo: str, base: str, head: str):
    """Yield (old path, new path, removed {old line: text}, added {new line: text}) per file."""
    diff = _git(repo, "diff", "-U0", "--no-color", "-M", base, head)
    old = new = None
    removed, added = {}, {}
    o = n = 0
    for line in diff.splitlines():
        if line.startswith("diff --git "):
            if old is not None or new is not None:
                yield old, new, removed, added
            old = new = None
            removed, added = {}, {}
        elif line.startswith("--- "):
            old = None if line[4:] == "/dev/null" else line[6:]
        elif line.startswith("+++ "):
            new = None if line[4:] == "/dev/null" else line[6:]
        elif line.startswith("@@"):
            m = re.match(r"@@ -(\d+)(?:,\d+)? \+(\d+)(?:,\d+)? @@", line)
            o, n = int(m.group(1)), int(m.group(2))
        elif line.startswith("-"):
            removed[o] = line[1:]
            o += 1
        elif line.startswith("+"):
            added[n] = line[1:]
            n += 1
    if old is not None or new is not None:
        yield old, new, removed, added


def _enclosing(text: str, line_no: int) -> list:
    """The nearest declaration names above `line_no`: a method or function, and a class."""
    lines = text.splitlines()
    found, have_member = [], False
    for i in range(min(line_no, len(lines)) - 1, -1, -1):
        s = lines[i]
        m = CLASS_DECL.search(s)
        if m:
            found.append(m.group(1))
            break
        if not have_member:
            for rx in (PY_DECL, JAVA_DECL, SH_DECL):
                m = rx.match(s)
                if m and m.group(1) not in KEYWORDS and not s.rstrip().endswith(";"):
                    found.append(m.group(1))
                    have_member = True
                    break
    return found


def _subjects(code: str) -> set:
    """The names a changed code line DECLARES, and the keys its string literals carry."""
    ids = set()
    for lit in STRING.findall(code):
        ids.update(k for k in DOTTED.findall(lit) if len(k) >= 4)
        ids.update(t for t in TOKEN.findall(lit) if _interesting(t))
    for rx in (CLASS_DECL, FIELD_DECL, ASSIGN_DECL, PY_DECL):
        m = rx.search(code) if rx is CLASS_DECL else rx.match(code)
        if m and _named(m.group(1)):
            ids.add(m.group(1))
    m = JAVA_DECL.match(code)
    if m and _named(m.group(1)) and not code.rstrip().endswith(";") and not re.match(
            r"^\s*(?:return|new|throw|else|if|for|while|switch|catch)\b", code):
        ids.add(m.group(1))
    return ids


def _named(name: str) -> bool:
    return len(name) >= 4 and name not in STOP and name not in KEYWORDS


def _interesting(tok: str) -> bool:
    if len(tok) < 4 or tok in STOP or tok in KEYWORDS:
        return False
    return bool(re.search(r"[a-z][A-Z]", tok) or ("_" in tok.strip("_") and re.search(r"[A-Za-z]", tok)))


def candidates(repo: str, base: str, head: str) -> dict:
    base = _git(repo, "merge-base", base, head).strip()
    touched, added_prose = set(), {}
    for old, new, removed, added in _hunks(repo, base, head):
        for rev, path, lines in ((base, old, removed), (head, new, added)):
            if not path or not lines:
                continue
            text = _blob(repo, rev, path)
            kind = _kind(path, text.split("\n", 1)[0])
            prose = prose_map(path, text)
            if kind != "doc":
                for no, line in lines.items():
                    code = line.replace(prose.get(no, "\0"), "") if no in prose else line
                    touched |= _subjects(code)
                for no in (min(lines), max(lines)):
                    touched.update(n for n in _enclosing(text, no) if _named(n))
            if rev == head:
                for no in lines:
                    if no in prose:
                        added_prose[(path, no)] = prose[no]
    a = []
    for (path, no), text in sorted(added_prose.items()):
        words = sorted({m.group(1).lower() for m in UNIVERSAL.finditer(text)})
        if words:
            a.append({"path": path, "line": no, "text": text, "why": words})
    listed = {(c["path"], c["line"]) for c in a}
    hits = {}
    if touched:
        rx = re.compile(r"(?<![A-Za-z0-9_])(" + "|".join(
            sorted(map(re.escape, touched), key=len, reverse=True)) + r")(?![A-Za-z0-9_])")
        for path, text in _tree(repo, head).items():
            for no, prose in prose_map(path, text).items():
                for m in set(rx.findall(prose)):
                    hits.setdefault(m, []).append((path, no, prose))
    common = {k: len(v) for k, v in hits.items() if len(v) > TOO_COMMON}
    naming = {}
    for ident, where in hits.items():
        if ident in common:
            continue
        for path, no, prose in where:
            if (path, no) in listed or (path, no) in added_prose:
                continue
            entry = naming.setdefault((path, no), {"path": path, "line": no, "text": prose,
                                                   "why": [], "added": (path, no) in added_prose})
            entry["why"].append(ident)
    b = [dict(v, why=sorted(v["why"])) for _, v in sorted(naming.items())]
    return {"base": base, "added_universals": a, "naming": b, "too_common": common}


def _git(repo: str, *args: str) -> str:
    return subprocess.run(["git", "-C", repo, *args], check=True, capture_output=True,
                          text=True).stdout


def selftest() -> int:
    failures = []

    def check(name, ok, detail=""):
        print(("  ok   " if ok else "  FAIL ") + name + ("" if ok else f" — {detail}"))
        if not ok:
            failures.append(name)

    with tempfile.TemporaryDirectory() as tmp:
        def write(rel, text):
            path = os.path.join(tmp, rel)
            os.makedirs(os.path.dirname(path), exist_ok=True)
            with open(path, "w") as f:
                f.write(text)

        _git(tmp, "init", "-q")
        _git(tmp, "config", "user.email", "t@example.org")
        _git(tmp, "config", "user.name", "t")
        write("src/Foo.java",
              "/** Every caller of {@link #fooBar} passes a non-null name. */\n"
              "public class Foo {\n"
              "    // counts things\n"
              "    int fooBar(String name) { return name.length(); }\n"
              "}\n")
        write("README.md",
              "Call `fooBar` to measure a name.\n"
              "Unrelated sentence about the weather, which is never the same.\n"
              "```\n"
              "fooBar(x)  // only code in a fence\n"
              "```\n")
        _git(tmp, "add", "-A")
        _git(tmp, "commit", "-qm", "base")
        base = _git(tmp, "rev-parse", "HEAD").strip()
        write("src/Foo.java",
              "/** Every caller of {@link #fooBar} passes a non-null name. */\n"
              "public class Foo {\n"
              "    // counts things\n"
              "    // This is the only path that measures names.\n"
              "    int fooBar(String name) { return name == null ? 0 : name.length(); }\n"
              "}\n")
        write("docs/notes.md", "Nothing else reads fooBar.\nA plain added line.\n")
        _git(tmp, "add", "-A")
        _git(tmp, "commit", "-qm", "change")
        head = _git(tmp, "rev-parse", "HEAD").strip()

        got = candidates(tmp, base, head)
        a = {(c["path"], c["line"]) for c in got["added_universals"]}
        b = {(c["path"], c["line"]) for c in got["naming"]}
        check("an added comment carrying 'only' is listed under A", ("src/Foo.java", 4) in a, str(a))
        check("an added doc line carrying 'Nothing' is listed under A", ("docs/notes.md", 1) in a,
              str(a))
        check("an added line with no universal is not under A", ("docs/notes.md", 2) not in a, str(a))
        check("unchanged prose carrying a universal is not under A", ("src/Foo.java", 1) not in a,
              str(a))
        check("unchanged javadoc naming the changed method is listed under B",
              ("src/Foo.java", 1) in b, str(b))
        check("an unchanged README line naming it is listed under B", ("README.md", 1) in b, str(b))
        check("an unrelated README line is listed nowhere",
              ("README.md", 2) not in a | b, str(a | b))
        check("a code line inside a markdown fence is not prose", ("README.md", 4) not in a | b,
              str(a | b))
        check("the changed code line itself is not prose", ("src/Foo.java", 5) not in a | b,
              str(a | b))
    print(f"\npassed={9 - len(failures)} failed={len(failures)}")
    return 1 if failures else 0


def main(argv) -> int:
    if argv[1:] == ["--selftest"]:
        return selftest()
    args = argv[1:]
    repo, as_json = ".", False
    if args[:1] == ["-C"]:
        repo, args = args[1], args[2:]
    if args[:1] == ["--json"]:
        as_json, args = True, args[1:]
    if len(args) not in (1, 2):
        print(__doc__, file=sys.stderr)
        return 2
    got = candidates(repo, args[0], args[1] if len(args) == 2 else "HEAD")
    if as_json:
        print(json.dumps(got, indent=1))
        return 0
    print(f"A · universals in prose this change added ({len(got['added_universals'])})")
    for c in got["added_universals"]:
        print(f"  {c['path']}:{c['line']}: {c['text'][:160]}   [{', '.join(c['why'])}]")
    print(f"B · prose naming an identifier this change touched ({len(got['naming'])})")
    for c in got["naming"]:
        mark = " (added)" if c["added"] else ""
        print(f"  {c['path']}:{c['line']}{mark}: {c['text'][:160]}   [{', '.join(c['why'])}]")
    if got["too_common"]:
        print(f"too common to list (named by more than {TOO_COMMON} prose lines): " +
              ", ".join(f"{k} ({v})" for k, v in sorted(got["too_common"].items())))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
