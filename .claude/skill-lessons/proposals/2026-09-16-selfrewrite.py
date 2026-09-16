#!/usr/bin/env python3
"""Does a commit REWRITE prose this run already wrote, or only DELETE it?

The signature harden:401 describes -- "delete the unsupported clause instead of replacing it with a
better-sounding one ... several cycles in a row of this is the signature" -- is decidable from git:
a commit whose diff is comment-only, which removes comment lines that an EARLIER commit in the same
run added, and which adds comment lines in their place.

Deletion is the sanctioned remedy, so removing own prose without adding any must NOT flag.
"""
import subprocess
import sys


def run(*args):
    return subprocess.run(args, capture_output=True, text=True, check=True).stdout


def is_comment(line):
    body = line[1:].strip()
    return body.startswith(("*", "//", "/*")) or body == ""


def diff_lines(sha):
    out = run("git", "show", sha, "--format=", "--unified=0", "--", "*.java", "*.md")
    added, removed, code = [], [], 0
    for line in out.splitlines():
        if line.startswith(("+++", "---")):
            continue
        if line.startswith(("+", "-")):
            if is_comment(line):
                (added if line[0] == "+" else removed).append(line[1:].strip())
            elif line[1:].strip():
                code += 1
    return added, removed, code


def main(base, head):
    shas = run("git", "log", "--format=%h", "--reverse", f"{base}..{head}").split()
    written = set()          # every comment line this run has added so far
    for sha in shas:
        added, removed, code = diff_lines(sha)
        subject = run("git", "log", "-1", "--format=%s", sha).strip()[:52]
        reused = [r for r in removed if r in written and r]
        if code:
            verdict = "code       "
        elif reused and added:
            verdict = "REWRITE <<<"
        elif reused:
            verdict = "delete     "
        else:
            verdict = "new prose  "
        print(f"{sha}  {verdict}  own_prose_removed={len(reused):<3} added={len(added):<3} "
              f"code={code:<4} {subject}")
        written.update(a for a in added if a)


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
