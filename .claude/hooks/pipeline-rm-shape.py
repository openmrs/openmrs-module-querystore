#!/usr/bin/env python3
"""PreToolUse(Bash) guard for the one `rm` shape that can stall an unattended pool run.

WHY A HOOK, AND WHY IT SAYS `deny`
Claude Code carries a registry of permission "circuit breakers", and `dangerousRemoval` is declared
`bypassImmune: true` (claude 2.1.270, function `CJ`). Measured 2026-09-13 on an isolated rig, under
`--dangerously-skip-permissions`: the breaker still raises a prompt; a permission rule cannot clear
it ("cannot be auto-allowed by permission rules"); and a PreToolUse hook answering `allow` does NOT
clear it either -- the command stayed blocked. Only a human keystroke moves it, which an unattended
run has none of; ticket 409's session sat on that prompt. A hook `deny` IS honoured ahead of the
breaker -- the breaker's message never renders and this reason reaches the model instead. So the
only way to keep a pool run moving is to stop the shape being proposed and hand back the rewrite.

WHAT IT MATCHES, AND WHY THAT IS A COMPLETE COVER
Three of `CJ`'s four ask-branches are gated on one predicate: the argument survives stripping a
trailing `/*` run -- i.e. it ENDS in a glob. So "an rm argument ending in `*`" covers all three
without reimplementing their internals, which matters because those internals turn on tilde
expansion, `..` collapsing and command-substitution sentinels; a 98%-right copy would miss in
exactly the tail that prompts. The fourth branch needs no glob but fires only on the working
directory, an ancestor, or a critical system path -- matched separately and literally.

DELIBERATELY WIDER THAN THE BREAKER
`rm -rf sub/*` inside a worktree passes the breaker today (measured) and is refused here anyway. The
idiom is rare, the rewrite is mechanical and named in the message, and the alternative is a rule
that tracks four minified predicates across CLI upgrades.

SCOPED TO UNATTENDED SESSIONS
Judged when the cwd is inside a pipeline or agent worktree, OR when pool-run's stamp is in the
environment (hooks inherit the session's env -- measured). The stamp is what covers the driver's
sessions that do NOT run in a worktree: `/skill-retro` runs in the skills checkout and the health
probe in the pipeline directory, and a prompt raised in either is a hang until the quiet watchdog
kills the run, not a pause. An operator's own interactive session carries neither and is untouched:
there a prompt reaches somebody who can answer it, and this would only be friction.

FAIL OPEN, ALWAYS
Any unexpected input -- no payload, bad JSON, a command the tokenizer refuses -- allows the command
unchanged. The worst this may do is fail to prevent a prompt.
"""

import json
import os
import re
import shlex
import sys

OPS = {"&&", "||", "|", ";", "&", "(", ")", "{", "}", "\n"}
HEREDOC = re.compile(r"<<-?[ \t]*(?:'([^']+)'|\"([^\"]+)\"|([A-Za-z_][A-Za-z0-9_]*))")
RM_WORD = re.compile(r"(?:^|[^A-Za-z0-9_.\-])(rm|rmdir)(?:\s|$)")
TRAILING_GLOB = re.compile(r"\*+/*$")
BARE_GLOB_ARG = re.compile(r"(?:^|\s)([^\s;&|()]*\*+/*)(?=\s|$)")

WORKTREE_MARKERS = ("/.claude/pipeline/worktrees/", "/.claude/worktrees/")


def allow():
    sys.exit(0)


def strip_heredocs(command):
    """Drop heredoc BODIES: they are data. A commit message saying rm beside a star is not an rm."""
    lines = command.split("\n")
    out, i = [], 0
    while i < len(lines):
        out.append(lines[i])
        tags = HEREDOC.findall(lines[i])
        i += 1
        for a, b, c in tags:
            tag = a or b or c
            while i < len(lines) and lines[i].strip() != tag:
                i += 1
            i += 1
    return "\n".join(out)


def neutralize(command):
    """Collapse substitutions to one opaque word so a substituted path stays ONE argument."""
    out, i, n = [], 0, len(command)
    while i < n:
        if command.startswith("$(", i):
            depth, j = 1, i + 2
            while j < n and depth:
                if command[j] == "(":
                    depth += 1
                elif command[j] == ")":
                    depth -= 1
                j += 1
            out.append("SUBST")
            i = j
        elif command[i] == "\x60":
            j = command.find("\x60", i + 1)
            out.append("SUBST")
            i = n if j == -1 else j + 1
        else:
            out.append(command[i])
            i += 1
    return "".join(out)


def rm_targets(command):
    """Non-flag arguments of every rm/rmdir, or None when the command cannot be tokenized."""
    try:
        lex = shlex.shlex(command, posix=True, punctuation_chars=True)
        lex.whitespace_split = True
        tokens = list(lex)
    except ValueError:
        return None
    found, active, end_of_flags = [], False, False
    for tok in tokens:
        if tok in OPS or set(tok) <= {";", "&", "|"}:
            active = end_of_flags = False
            continue
        if tok.rsplit("/", 1)[-1] in ("rm", "rmdir"):
            active, end_of_flags = True, False
            continue
        if not active:
            continue
        if tok == "--":
            end_of_flags = True
            continue
        if not end_of_flags and tok.startswith("-"):
            continue
        found.append(tok)
    return found


def resolve(arg, cwd, home):
    path = arg
    if path.startswith("~"):
        path = home + path[1:]
    path = path.replace("${HOME}", home).replace("$HOME", home)
    if not path.startswith("/"):
        path = os.path.join(cwd, path)
    return os.path.normpath(path)


def worktree_root(path):
    """True when path is the ROOT of a pipeline or agent worktree, not something inside one."""
    for marker in WORKTREE_MARKERS:
        i = path.find(marker)
        if i != -1:
            rest = path[i + len(marker):]
            return bool(rest) and "/" not in rest
    return False


GLOB_REASON = """`{arg}` ends in a glob, and an rm whose target ends in a glob is what Claude Code's \
`dangerousRemoval` circuit breaker asks a human about. That breaker is bypass-immune: \
`--dangerously-skip-permissions` does not clear it, no permission rule can auto-allow it, and this \
run has nobody to answer it -- it would stall here until a person did. Rewrite it:
  - to empty a directory:  rm -rf <dir> && mkdir -p <dir>
  - or:                    find <dir> -mindepth 1 -delete
  - to discard a worktree: do not. pool-run's own release() reclaims it. If you genuinely must:
                           git -C <parent repo> worktree remove --force <worktree>
                           run from OUTSIDE the worktree. A bare rm leaves .git behind and the
                           worktree stays registered in the parent repository."""

ROOT_REASON = """`{arg}` resolves to this session's working directory, one of its ancestors, a \
critical system path, or the root of a pipeline worktree. Claude Code's `dangerousRemoval` circuit \
breaker asks a human about that, and it is bypass-immune: `--dangerously-skip-permissions` does not \
clear it and no permission rule can auto-allow it, so this run would stall here until a person \
answered. If you are discarding the worktree, do not -- pool-run's release() owns that. If you \
genuinely must:
  git -C <parent repo> worktree remove --force <worktree>
run from OUTSIDE the worktree. A bare rm leaves .git behind and the worktree stays registered."""


def main():
    payload = json.load(sys.stdin)
    if payload.get("tool_name") != "Bash":
        allow()

    command = (payload.get("tool_input") or {}).get("command") or ""
    cwd = payload.get("cwd") or ""
    if not command or not cwd:
        allow()

    in_worktree = any(m in cwd + "/" for m in WORKTREE_MARKERS)
    unattended = bool(os.environ.get("CLAUDE_PIPELINE_SESSION")
                      or os.environ.get("CLAUDE_PIPELINE_SLOT"))
    if not (in_worktree or unattended):
        allow()
    if not RM_WORD.search(command):
        allow()

    home = os.path.expanduser("~")
    scan = neutralize(strip_heredocs(command))

    targets = rm_targets(scan)
    if targets is None:
        targets = BARE_GLOB_ARG.findall(scan)

    critical = {"/", "/usr", "/etc", "/var", "/bin", "/sbin", "/lib", "/opt",
                "/System", "/Library", home}

    for raw in targets:
        arg = raw.strip("'\"")
        if not arg:
            continue
        if TRAILING_GLOB.search(arg):
            reason = GLOB_REASON.format(arg=arg)
        else:
            path = resolve(arg, cwd, home)
            hits_cwd = path == cwd or cwd.startswith(path.rstrip("/") + "/")
            if not (path in critical or hits_cwd or worktree_root(path)):
                continue
            reason = ROOT_REASON.format(arg=arg)
        json.dump({"hookSpecificOutput": {
            "hookEventName": "PreToolUse",
            "permissionDecision": "deny",
            "permissionDecisionReason": reason,
        }}, sys.stdout)
        sys.exit(0)


if __name__ == "__main__":
    try:
        main()
    except Exception:
        allow()
