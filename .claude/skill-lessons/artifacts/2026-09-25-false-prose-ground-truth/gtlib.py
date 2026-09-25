#!/usr/bin/env python3
"""Shared locator for the ground-truth dataset: finds one false line and verifies it mechanically.

locate(pr, fix, path, phrase, mode="removed") returns a dict or raises LocateError.

- prefix = fix^1; base = git merge-base prefix <PR baseRefOid>
- mode "removed": exactly one line REMOVED by `git diff -U0 prefix fix -- path` contains `phrase`;
  its 1-indexed old-file number is the line.
- mode "unchanged": the fix corrected the sentence by editing an adjacent line (or adding a qualifying
  sentence) and left the line carrying the false predicate as it was; `phrase` must occur on exactly one
  line of prefix:path, that line must be byte-identical in fix:path, and the fix's diff for the file must
  have a hunk inside the paragraph that line sits in.
- always: `git show prefix:path` line N equals the text; grep -nF of the trimmed line hits N; the fix is
  an ancestor of refs/pull/<pr>/head.
- introduced_by: OLD if the trimmed line occurs (grep -F) in base:path, else ADDED.
"""
import json
import re
import subprocess

REPO = "/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/db22ea60-d89d-4e0d-808b-b04c38a332ec/scratchpad/gt/cs.git"
NEAR = 4
_base_refs = {}


class LocateError(Exception):
    pass


def _run(args, check=True):
    p = subprocess.run(args, capture_output=True, text=True)
    if check and p.returncode != 0:
        raise LocateError("FAILED: %s\n%s" % (" ".join(args), p.stderr))
    return p


def git(*args, check=True):
    return _run(["git", "-C", REPO] + list(args), check=check)


def base_ref_of(pr):
    if pr not in _base_refs:
        _base_refs[pr] = _run(["gh", "pr", "view", str(pr), "--repo", "openmrs/openmrs-module-chartsearchai",
                               "--json", "baseRefOid", "--jq", ".baseRefOid"]).stdout.strip()
    return _base_refs[pr]


def _hunks(prefix, fix, path):
    """[(old_start, old_len, removed[(ln, text)])] of git diff -U0."""
    diff = git("diff", "-U0", prefix, fix, "--", path).stdout
    out = []
    cur = None
    old_ln = None
    for line in diff.splitlines():
        m = re.match(r"^@@ -(\d+)(?:,(\d+))? \+\d+(?:,\d+)? @@", line)
        if m:
            cur = (int(m.group(1)), int(m.group(2)) if m.group(2) is not None else 1, [])
            out.append(cur)
            old_ln = cur[0]
            continue
        if cur is None or line.startswith("---") or line.startswith("+++"):
            continue
        if line.startswith("-"):
            cur[2].append((old_ln, line[1:]))
            old_ln += 1
    return out


def locate(pr, fix, path, phrase, mode="removed"):
    fix = git("rev-parse", fix + "^{commit}").stdout.strip()
    prefix = git("rev-parse", fix + "^1").stdout.strip()
    bref = base_ref_of(pr)
    base = git("merge-base", prefix, bref).stdout.strip()
    if git("merge-base", "--is-ancestor", fix, "refs/pull/%s/head" % pr, check=False).returncode != 0:
        raise LocateError("fix %s is not on refs/pull/%s/head" % (fix[:10], pr))
    pre = git("show", "%s:%s" % (prefix, path)).stdout.split("\n")
    hunks = _hunks(prefix, fix, path)
    if not hunks:
        raise LocateError("fix %s does not touch %s" % (fix[:10], path))
    if mode == "removed":
        hits = [(ln, t) for (_, _, rem) in hunks for (ln, t) in rem if phrase in t]
        if len(hits) != 1:
            raise LocateError("expected one removed line with %r, got %d" % (phrase, len(hits)))
        ln, text = hits[0]
        fix_removes_line = True
    elif mode == "unchanged":
        cand = [i + 1 for i, l in enumerate(pre) if phrase in l]
        if len(cand) != 1:
            raise LocateError("expected one prefix line with %r, got %s" % (phrase, cand))
        ln = cand[0]
        text = pre[ln - 1]
        post = git("show", "%s:%s" % (fix, path)).stdout.split("\n")
        if text not in post:
            raise LocateError("unchanged-mode line is not byte-identical in the fix")
        # The paragraph the line sits in: bounded by a blank line, a bare " *" javadoc line, a bare "//",
        # or a comment opener/closer. The fix must edit inside it (or at its closing boundary).
        brk = re.compile(r"^\s*(\*|//|/\*\*?|\*/)?\s*$")
        start = ln
        while start > 1 and not brk.match(pre[start - 2]):
            start -= 1
        end = ln
        while end < len(pre) and not brk.match(pre[end]):
            end += 1
        near = [h for h in hunks if h[0] <= end + 1 and h[0] + max(h[1], 1) - 1 >= start]
        if not near:
            raise LocateError("no hunk of the fix inside the paragraph %d-%d around %d" % (start, end, ln))
        fix_removes_line = False
    else:
        raise LocateError("bad mode")
    if pre[ln - 1] != text:
        raise LocateError("prefix line %d mismatch" % ln)
    trimmed = text.strip()
    grep_hits = [i + 1 for i, l in enumerate(pre) if trimmed in l]
    if ln not in grep_hits:
        raise LocateError("grep -nF of the trimmed line misses %d" % ln)
    b = git("show", "%s:%s" % (base, path), check=False)
    file_in_base = b.returncode == 0
    in_base_line = file_in_base and trimmed in b.stdout
    in_base_phrase = file_in_base and phrase in b.stdout
    return {
        "pr": int(pr),
        "fix": fix,
        "prefix": prefix,
        "base": base,
        "base_from": "git merge-base <prefix> <PR baseRefOid %s>" % bref[:10],
        "file": path,
        "line": ln,
        "false_text": trimmed,
        "phrase": phrase,
        "introduced_by": "OLD" if in_base_line else "ADDED",
        "fix_removes_line": fix_removes_line,
        "_check": {
            "grep_hits_in_prefix": grep_hits,
            "file_in_base": file_in_base,
            "trimmed_line_in_base": in_base_line,
            "phrase_in_base": in_base_phrase,
        },
    }


if __name__ == "__main__":
    import sys
    a = sys.argv[1:]
    mode = "removed"
    if "--unchanged" in a:
        a.remove("--unchanged")
        mode = "unchanged"
    print(json.dumps(locate(a[0], a[1], a[2], a[3], mode)))
