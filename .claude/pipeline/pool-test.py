#!/usr/bin/env python3
"""pool-test — exercises the driver's parallel machinery through its REAL entry points.

No simulations and no reimplementations: every case calls the function `pool-run` itself calls, over
a real git repository, real `git worktree` invocations, real `flock` contention between real
processes, and the real `Session`/scheduler. The one substitution is `claude.binary`, pointed at a
stub that emits stream-json — a production knob (it pins which `claude` a pool uses), not a mock of
anything this file is testing. Everything the tests assert about — isolation, leasing, locking,
scheduling, the barrier — is the shipped code path.

    python3 ~/.claude/pipeline/pool-test.py
"""

from __future__ import annotations

import json
import os
import re
import contextlib
import shutil
import time
import signal
import subprocess
import sys
import tempfile
import threading
from importlib.machinery import SourceFileLoader
from pathlib import Path

HERE = Path(__file__).resolve().parent
pool = SourceFileLoader("poolrun", str(HERE / "pool-run")).load_module()

PASS, FAIL = [], []

# The operator's own records, which no case may add to. Read here, before anything runs.
REAL_LESSONS = Path.home() / ".claude/skill-lessons"


def check(name: str, cond: bool, detail: str = "") -> None:
    (PASS if cond else FAIL).append(name)
    print(f"  {'ok  ' if cond else 'FAIL'} {name}" + (f" — {detail}" if detail and not cond else ""))


def sh(args, cwd=None, env=None):
    return subprocess.run(args, cwd=cwd and str(cwd), capture_output=True, text=True, env=env)


def git_fixture(tmp: Path) -> tuple[Path, Path]:
    """A real origin and a real clone, so worktrees and fetches are the genuine article."""
    origin = tmp / "origin.git"
    sh(["git", "init", "--bare", "-b", "main", str(origin)])
    work = tmp / "work"
    sh(["git", "clone", str(origin), str(work)])
    sh(["git", "-C", str(work), "config", "user.email", "t@t"])
    sh(["git", "-C", str(work), "config", "user.name", "t"])
    (work / "README.md").write_text("seed\n")
    sh(["git", "-C", str(work), "add", "-A"])
    sh(["git", "-C", str(work), "commit", "-m", "seed"])
    sh(["git", "-C", str(work), "push", "-u", "origin", "main"])
    return origin, work


def standalone_fixture(root: Path, name: str, tomcat: int, db: int) -> Path:
    d = root / name
    d.mkdir(parents=True)
    (d / "openmrs-standalone.jar").write_text("")
    (d / "openmrs-runtime.properties").write_text(
        f"connection.url=jdbc:mariadb://127.0.0.1:{db}/openmrs\ntomcatport={tomcat}\n")
    return d


def stub_claude(path: Path, marker: Path, sleep: float = 0.0) -> Path:
    """A `claude` that speaks just enough stream-json for Session to read it.

    It stamps the wall clock on entry and on exit, which is how the concurrency case reads whether
    two sessions OVERLAPPED. Wall-clock-of-the-whole-wave cannot answer that: the wave also does two
    `gh pr list` calls, so a serial run and a parallel one differ by less than the network noise.
    """
    path.write_text(
        "#!/bin/bash\n"
        f'start=$(python3 -c "import time;print(time.time())")\n'
        f"sleep {sleep}\n"
        f'echo "$PWD|$OPENMRS_STANDALONE_HOME|$MAVEN_ARGS|$CLAUDE_PIPELINE_SLOT|$start|'
        f'$(python3 -c \'import time;print(time.time())\')" >> {marker}\n'
        'echo \'{"type":"assistant","message":{"content":[{"type":"text","text":"hi"}]}}\'\n'
        'echo \'{"type":"result","result":"done","total_cost_usd":0.01}\'\n')
    path.chmod(0o755)
    return path


@contextlib.contextmanager
def isolated(tmp: Path):
    """Point every path the driver writes at a temp tree.

    A suite that writes the operator's real ledger, real run records and real gate state is not a
    suite, it is a second pipeline. Measured while writing this one: an earlier version left a
    driver-capture record in `~/.claude/skill-lessons`, where it counted towards the retro threshold.
    """
    # EVERY path the driver writes. A constant added to `pool-run` and forgotten here is a suite that
    # writes the operator's real state: measured — omitting `SLOTS` let a case read the real leases
    # and RELEASE two slots a hand-launched session was holding, removing their worktrees.
    names = ["LEDGER", "LEDGER_FLOCK", "LOGS", "LESSONS", "LAST", "PR_STATE", "HARDEN_STATE",
             "UNATTENDED_DIR", "WORKTREES", "SLOT_M2", "SLOTS", "LOCK", "PAUSE", "PAUSED"]
    saved = {n: getattr(pool, n) for n in names}
    root = tmp / "state"
    for n in names:
        setattr(pool, n, root / Path(saved[n]).name)
    pool.LOGS.mkdir(parents=True, exist_ok=True)
    pool.LESSONS.mkdir(parents=True, exist_ok=True)
    # The driver reaches the gate files ONLY through the `gate-state` subprocess now, which resolves
    # them from $CLAUDE_HOME or $HOME — so rebinding `pool.PR_STATE` alone stopped isolating anything
    # and a case driving `clear_gate_state` wrote the operator's real state. Point the helper at the
    # same root the module constants name, so the two cannot disagree about which file is under test.
    prior_claude_home = os.environ.get("CLAUDE_HOME")
    os.environ["CLAUDE_HOME"] = str(root)
    try:
        yield root
    finally:
        for n, v in saved.items():
            setattr(pool, n, v)
        if prior_claude_home is None:
            os.environ.pop("CLAUDE_HOME", None)
        else:
            os.environ["CLAUDE_HOME"] = prior_claude_home


# ───────────────────────────────────────────────────────────── worktrees ──


def test_worktrees(tmp: Path) -> None:
    print("\nworktree isolation")
    origin, work = git_fixture(tmp)

    # A dirty main checkout must NOT stall anything: the driver never touches it beyond fetching.
    (work / "scratch.txt").write_text("uncommitted\n")
    sh(["git", "-C", str(work), "checkout", "-b", "someones-branch"])

    say = pool.Say(tmp / "say.md")
    failure, where, base = pool.prepare_repo(work, say)
    check("a dirty checkout on a side branch is not a failure", failure is None, f"{failure}: {where}")
    # It returns the sha it resolved. Resolving it a second time in the caller is two answers waiting
    # to differ, and the second is the one every worktree would actually be cut from.
    check("it hands back the base it resolved, rather than leaving it to be resolved again",
          base == pool.remote_head(work) and base[:8] in where, f"{base} vs {where}")

    missing = tmp / "not-a-repo"
    missing.mkdir()
    failed, why, no_sha = pool.prepare_repo(missing, say)
    check("a directory that is not a checkout is refused with no base",
          failed == "checkout-blocked" and no_sha == "", f"{failed}/{no_sha}")
    a, why_a = pool.make_worktree(work, "o/r", "101", base, say)
    b, why_b = pool.make_worktree(work, "o/r", "102", base, say)
    check("two tickets get two worktrees", a is not None and b is not None and a != b, f"{why_a}/{why_b}")
    check("each worktree is a real checkout", (a / "README.md").is_file() and (b / "README.md").is_file())
    check("both start at the remote head",
          sh(["git", "-C", str(a), "rev-parse", "HEAD"]).stdout.strip() == base
          and sh(["git", "-C", str(b), "rev-parse", "HEAD"]).stdout.strip() == base)

    # The operator's own checkout is the thing that used to be reset under a run.
    check("the operator's branch survives",
          sh(["git", "-C", str(work), "rev-parse", "--abbrev-ref", "HEAD"]).stdout.strip()
          == "someones-branch")
    check("the operator's uncommitted work survives", (work / "scratch.txt").is_file())

    # Two runs must be able to hold two branches of one repo at once — impossible in one checkout.
    sh(["git", "-C", str(a), "checkout", "-b", "fix/101"])
    made = sh(["git", "-C", str(b), "checkout", "-b", "fix/102"])
    check("two ticket branches are checked out at once", made.returncode == 0, made.stderr[:120])

    # State is keyed on the cwd, so the tenancy the hooks assume now actually holds.
    check("the two runs key the gate state differently", str(a) != str(b))

    (b / "left-behind.txt").write_text("x\n")
    check("a clean worktree is removed", pool.drop_worktree(work, a, say).startswith("removed"))
    kept = pool.drop_worktree(work, b, say)
    check("a worktree with unpushed work is kept", kept.startswith("kept") and b.is_dir(), kept)
    pool.drop_worktree(work, b, say, force=True)


def test_ticket_identity(tmp: Path) -> None:
    """A ticket named as a URL must reduce to its identifier before it reaches a path or a key.

    Measured on the #238 run: the pool was handed
    `https://github.com/openmrs/openmrs-module-chartsearchai/issues/238`, `resolve_named` returned it
    verbatim as the ticket, and `worktree_path` interpolated it — producing a directory containing
    `https:` and three extra path components. javac splits its classpath on `:`, so `mvn test` could
    not compile anything in that tree: main compiled, and every test failed with "cannot find symbol"
    against classes that were sitting in `target/classes`. The run lost three build cycles to it.
    """
    print("\nticket identity")

    url = "https://github.com/o/r/issues/238"
    jira = "https://openmrs.atlassian.net/browse/TRUNK-6429"

    # The identifier itself is asserted on `ticket_id`, not through `resolve_named`: the digit branch
    # of that function verifies the issue with a real `gh issue view`, so asserting the number there
    # would be asserting that this machine can reach GitHub, which is not what is under test.
    for token, want in [(url, "238"), ("https://github.com/o/r/pull/238", "238"),
                        (jira, "TRUNK-6429"), ("238", "238"), ("#238", "238"),
                        ("O3-1234", "O3-1234"), ("  #238  ", "238"),
                        # What an operator's paste actually varies. The fragment is the likeliest of
                        # all — the address bar carries `#issuecomment-…` the moment you scroll to a
                        # comment — and the first cut of this fix recognised none of these four.
                        (url + "#issuecomment-2412", "238"), (url + "?foo=bar", "238"),
                        ("HTTPS://GitHub.com/o/r/issues/238", "238"),
                        ("github.com/o/r/issues/238", "238"),
                        (jira + "?filter=1", "TRUNK-6429"),
                        ("openmrs.atlassian.net/browse/TRUNK-6429", "TRUNK-6429")]:
        check(f"{token.strip()!r} reduces to {want!r}", pool.ticket_id(token) == want,
              pool.ticket_id(token))
    check("an unparseable token is not expanded into one",
          pool.ticket_id("not a ticket") == "not a ticket", pool.ticket_id("not a ticket"))

    # Only a URL says which repo owns it; everything else is asked of each repo in turn, as before.
    check("a github URL names its own repo", pool.ticket_repo(url) == "o/r", pool.ticket_repo(url))
    check("a bare number names no repo", pool.ticket_repo("238") is None)
    check("a URL is refused by a repo that does not own it",
          pool.resolve_named(url, "other/repo") is None,
          repr(pool.resolve_named(url, "other/repo")))

    key = pool.resolve_named(jira, "o/r")
    check("a JIRA browse URL resolves to its key",
          key is not None and key.get("key") == "TRUNK-6429", repr(key))
    check("and keeps the browsable URL it was given",
          (key or {}).get("url") == jira, repr(key))
    check("a bare JIRA key still passes through",
          (pool.resolve_named("O3-1234", "o/r") or {}).get("key") == "O3-1234")

    # Sanitising lives in `safe_component`, which the lesson record and the log stem call too, so a
    # caller passing something unsanitised cannot reach the filesystem through any of the three.
    for raw in [url, jira, "238", "#238", "TRUNK-6429", "weird/../thing", "a:b"]:
        leaf = pool.worktree_path("o/r", raw).name
        check(f"the worktree leaf for {raw!r} is filesystem-safe",
              re.fullmatch(r"[A-Za-z0-9._-]+", leaf) is not None
              and pool.worktree_path("o/r", raw).parent == pool.WORKTREES,
              leaf)

    check("a URL and its number land in the SAME worktree, so one ticket is never two trees",
          pool.worktree_path("o/r", url) == pool.worktree_path("o/r", "238"),
          f"{pool.worktree_path('o/r', url).name} vs {pool.worktree_path('o/r', '238').name}")
    check("and so does the same URL carrying a comment fragment",
          pool.worktree_path("o/r", url + "#issuecomment-2412")
          == pool.worktree_path("o/r", "238"))
    check("two different tickets still get two trees",
          pool.worktree_path("o/r", "238") != pool.worktree_path("o/r", "239"))

    # Sanitising is many-to-one, so on its own it would give two tickets one directory — and
    # `make_worktree` would release and recreate the first one's tree under the second, the defect
    # ticket-pool 0.14.4 removed. A rewritten identifier therefore carries a digest of the original.
    check("two tokens that sanitise alike do NOT share a worktree",
          pool.worktree_path("o/r", "PROJ:123") != pool.worktree_path("o/r", "PROJ-123"),
          f"{pool.worktree_path('o/r', 'PROJ:123').name} vs {pool.worktree_path('o/r', 'PROJ-123').name}")
    check("and the token that needed no rewriting keeps the path it always had",
          pool.worktree_path("o/r", "PROJ-123").name == "o-r-PROJ-123",
          pool.worktree_path("o/r", "PROJ-123").name)
    for safe in ["238", "O3-1234", "TRUNK-6429"]:
        check(f"{safe!r} is untouched by the sanitiser",
              pool.worktree_path("o/r", safe).name == f"o-r-{safe}",
              pool.worktree_path("o/r", safe).name)

    # The digest is taken from the ORIGINAL, not from the sanitised result. Hashing the result makes
    # two genuinely different unsafe tokens collide again, which the PROJ:123/PROJ-123 pair above
    # cannot see because PROJ-123 never enters the digest branch at all.
    check("two DIFFERENT unsafe tokens that sanitise alike stay apart",
          pool.worktree_path("o/r", "PROJ:123") != pool.worktree_path("o/r", "PROJ/123"),
          f"{pool.worktree_path('o/r', 'PROJ:123').name} vs {pool.worktree_path('o/r', 'PROJ/123').name}")

    # `.strip("-.")` and the `or "ticket"` fallback: without them a leaf can be empty, a bare dot, or
    # start with `-`. All three are usable-looking directory names that are not what anyone meant.
    for nasty in ["", ".", "..", "-", "????", "#"]:
        leaf = pool.worktree_path("o/r", nasty).name
        check(f"a leaf for {nasty!r} is a single ordinary component",
              re.fullmatch(r"[A-Za-z0-9._-]+", leaf) is not None
              and leaf not in (".", "..") and not leaf.startswith((".", "-")),
              leaf)

    check("a component that is already safe is returned unchanged",
          pool.safe_component("o-r-238") == "o-r-238")
    # Asserted on the helper directly: through `worktree_path` the slug prefix always survives
    # sanitising, so no ticket can drive `safe` empty and the fallback is unreachable from there.
    # It is reachable at this API, which is where a future caller would meet it.
    for allbad in ["????", "", "..", "///"]:
        comp = pool.safe_component(allbad)
        check(f"safe_component({allbad!r}) is still an ordinary component",
              re.fullmatch(r"[A-Za-z0-9._-]+", comp) is not None
              and not comp.startswith((".", "-")), comp)
    check("and one that only LOOKS safe but ends in punctuation is not",
          pool.safe_component("o-r-238-") != "o-r-238-", pool.safe_component("o-r-238-"))

    # ticket_repo strips, so a padded paste still narrows resolve_named to the owning repo. Without
    # it the ownership check silently falls through and every repo is asked instead.
    check("a whitespace-padded URL still names its repo",
          pool.ticket_repo("  " + url + "  ") == "o/r", pool.ticket_repo("  " + url + "  "))


def test_legacy_ticket_state(tmp: Path) -> None:
    """State the pre-normalisation driver wrote must still be findable.

    A lease or ledger row written before `ticket_id` existed stores the RAW token, so normalising
    only the INPUT never matches it: the slot stays held forever, and the ledger row's whole history
    goes — `attempts`, and the `aborted` status that stops an identical second attempt. No input the
    operator can type reaches those rows, because normalising what they type cannot retroactively
    normalise what was stored. Both reads therefore normalise BOTH sides.
    """
    print("\nlegacy ticket state")
    url = "https://github.com/o/r/issues/238"

    with isolated(tmp):
        pool.SLOTS.mkdir(parents=True, exist_ok=True)
        (pool.SLOTS / "slot-9.json").write_text(json.dumps({
            "slot": "slot-9", "ticket": url, "slug": "o/r",      # written by the OLD code
            "worktree": str(tmp / "gone"), "repo": str(tmp / "repo"), "standalone": None}))
        leases = pool.all_leases()
        check("precondition: the lease really stores the raw token",
              leases["slot-9"]["ticket"] == url, leases["slot-9"]["ticket"])

        say = pool.Say(tmp / "say-legacy.md")
        freed = pool.release_claim({"repos": {}}, "238", say)
        check("a lease written under a raw URL is released by the ticket's number",
              freed is not None and not (pool.SLOTS / "slot-9.json").exists(),
              "the slot would stay held with nothing able to reach it")

    # `--release <url>` with no `#` must narrow to the repo the URL names, rather than reporting the
    # number as ambiguous across every repo that happens to hold one.
    with isolated(tmp):
        pool.SLOTS.mkdir(parents=True, exist_ok=True)
        for slot, slug in [("slot-1", "o/r"), ("slot-2", "x/y")]:
            (pool.SLOTS / f"{slot}.json").write_text(json.dumps({
                "slot": slot, "ticket": "238", "slug": slug,
                "worktree": str(tmp / slot), "repo": str(tmp / "repo"), "standalone": None}))
        say = pool.Say(tmp / "say-narrow.md")
        freed = pool.release_claim({"repos": {}}, url, say)
        check("a bare URL releases the lease of the repo IT names, not an ambiguity error",
              freed is not None and not (pool.SLOTS / "slot-1.json").exists()
              and (pool.SLOTS / "slot-2.json").exists(),
              "released nothing, or released the wrong repo's slot")

    # The ledger half: a row under the raw key still carries its history to the normalised one.
    # `aborted` is the value NEEDS_HUMAN actually holds, and attempts is left at 0 deliberately: with
    # the fallback removed the row is invisible, no skip applies, and a job comes back. An earlier
    # version of this case used a status NEEDS_HUMAN does not contain and attempts at the cap, so it
    # passed through the ATTEMPTS branch and would have passed with the fallback gone too.
    ledger = {"o/r#" + url: {"status": "aborted", "attempts": 0}}
    job = pool.consider("o/r", str(tmp), {"number": 238, "title": "t", "url": url},
                        [], ledger, pool.Say(tmp / "say-ledger.md"),
                        {"ticket": {"max_attempts": 2}}, forced=False)
    check("a ledger row under the raw key still carries its aborted verdict",
          job is None, "the row was invisible, so an aborted ticket would be retried")


# ───────────────────────────────────────────────────────────────── slots ──


def test_slots(tmp: Path) -> None:
    print("\nresource slots")
    root = tmp / "standalones"
    one = standalone_fixture(root, "sa1", 8081, 3316)
    two = standalone_fixture(root, "sa2", 8083, 3318)
    clash = standalone_fixture(root, "sa3", 8081, 3399)

    cfg = pool.merge(pool.DEFAULTS, {"parallel": {"max_workers": 2,
                                                  "standalones": [str(one), str(two)]}})
    slots = pool.build_slots(cfg, 2, tmp / "m2")
    check("one slot per worker", len(slots) == 2)
    check("slots hold distinct standalones", slots[0].standalone != slots[1].standalone)
    check("slots hold distinct maven repositories", slots[0].m2 != slots[1].m2)

    env = slots[0].env()
    check("the standalone reaches the session as OPENMRS_STANDALONE_HOME",
          env["OPENMRS_STANDALONE_HOME"] == str(one))
    check("the maven repository is split, not replaced",
          f"-Dmaven.repo.local={slots[0].m2}" in env["MAVEN_ARGS"]
          and f"-Dmaven.repo.local.tail={Path.home() / '.m2/repository'}" in env["MAVEN_ARGS"])
    check("the session is told it has co-tenants", env["CLAUDE_PIPELINE_SLOT"] == slots[0].name)

    check("ports are read off the standalone", slots[0].tomcatport == 8081 and slots[0].dbport == 3316)

    # The preflight is where a misconfiguration has to stop, because the alternative is two
    # verifiers restarting one server underneath each other.
    short = pool.merge(pool.DEFAULTS, {"parallel": {"max_workers": 3,
                                                    "standalones": [str(one), str(two)]}})
    check("fewer standalones than workers is a fatal preflight problem",
          any("standalone" in p for p in pool.slot_problems(short, 3)))
    collide = pool.merge(pool.DEFAULTS, {"parallel": {"max_workers": 2,
                                                      "standalones": [str(one), str(clash)]}})
    check("two standalones sharing a port is a fatal preflight problem",
          any("port" in p for p in pool.slot_problems(collide, 2)))
    missing = pool.merge(pool.DEFAULTS, {"parallel": {"max_workers": 1,
                                                      "standalones": [str(tmp / "nope")]}})
    check("a standalone that is not on disk is a fatal preflight problem",
          any("nope" in p for p in pool.slot_problems(missing, 1)))
    check("a single worker needs no standalone configured at all",
          pool.slot_problems(pool.merge(pool.DEFAULTS, {}), 1) == [])


# ────────────────────────────────────────────────────────── gate  state ──


def test_gate_state_locking(tmp: Path) -> None:
    print("\ngate-state under real concurrency")
    helper = HERE / "gate-state"
    check("the helper is installed and executable", os.access(helper, os.X_OK))
    if not os.access(helper, os.X_OK):
        return

    home = tmp / "home"
    (home / ".claude").mkdir(parents=True)
    env = {**os.environ, "HOME": str(home)}

    # Twenty concurrent runs, twenty different worktrees, one state file. The naive
    # read-modify-write this replaces loses entries here; nothing errors when it does.
    # Resolved, because the tenant key is the PHYSICAL path — the one thing the hooks and the
    # helper must agree on, and the thing they silently did not agree on before.
    dirs = []
    for i in range(20):
        d = tmp / f"wt{i}"
        d.mkdir()
        dirs.append(d.resolve())

    def write(d: Path) -> None:
        sh([sys.executable, str(helper), "pr-set", "--pr", "9", "--round", "1",
            "--phase", "building", "--blocking", "0"], cwd=d, env=env)

    threads = [threading.Thread(target=write, args=(d,)) for d in dirs]
    for t in threads:
        t.start()
    for t in threads:
        t.join()

    state = json.loads((home / ".claude/pr-harden-state.json").read_text())
    check("no concurrent writer's entry is lost", len(state) == 20, f"kept {len(state)} of 20")

    # Interleaved writes to ONE entry must not tear it either.
    d = dirs[0]

    def churn(n: int) -> None:
        for i in range(6):
            sh([sys.executable, str(helper), "await", f"agent-{n}-{i}"], cwd=d, env=env)
            sh([sys.executable, str(helper), "clear-await"], cwd=d, env=env)

    threads = [threading.Thread(target=churn, args=(n,)) for n in range(6)]
    for t in threads:
        t.start()
    for t in threads:
        t.join()
    state = json.loads((home / ".claude/pr-harden-state.json").read_text())
    check("the file is still valid JSON after interleaved awaits", isinstance(state, dict))
    check("every other tenant's entry survived the churn", len(state) == 20, f"kept {len(state)}")

    # resolve-ticket Step 7 needs the await in BOTH files or the armed gate refuses the yield the
    # harden cycle needs. One command, so the two cannot come apart.
    sh([sys.executable, str(helper), "harden-set", "--cycle", "2", "--edits", "3"], cwd=d, env=env)
    sh([sys.executable, str(helper), "await", "harden phase 2"], cwd=d, env=env)
    pr = json.loads((home / ".claude/pr-harden-state.json").read_text())[str(d)]
    hd = json.loads((home / ".claude/harden-state.json").read_text())[str(d)]
    check("one await reaches the pr-harden gate", [a["agent"] for a in pr["awaiting"]] == ["harden phase 2"])
    check("the same await reaches the harden gate", [a["agent"] for a in hd["awaiting"]] == ["harden phase 2"])
    sh([sys.executable, str(helper), "clear-await"], cwd=d, env=env)
    pr = json.loads((home / ".claude/pr-harden-state.json").read_text())[str(d)]
    hd = json.loads((home / ".claude/harden-state.json").read_text())[str(d)]
    check("clearing it clears both", pr["awaiting"] == [] and hd["awaiting"] == [])
    check("clearing an await does not disturb the phase", pr["phase"] == "building")
    check("clearing an await does not disturb harden's counts", hd["edits"] == 3 and hd["cycle"] == 2)

    # Both gates read `owner` to tell this session's entry from a co-located session's, and an
    # UNSTAMPED entry gives that discrimination up — so the helper has to carry it through.
    sh([sys.executable, str(helper), "--owner", "4242", "pr-set", "--pr", "7", "--round", "1",
        "--phase", "init", "--blocking", "1"], cwd=d, env=env)
    pr = json.loads((home / ".claude/pr-harden-state.json").read_text())[str(d)]
    check("the owning session's pid is stamped on the entry", pr.get("owner") == 4242, str(pr))
    sh([sys.executable, str(helper), "--owner", "4242", "await", "review r1"], cwd=d, env=env)
    hd = json.loads((home / ".claude/harden-state.json").read_text())[str(d)]
    check("an await stamps the owner on the entry it creates too", hd.get("owner") == 4242, str(hd))

    # `--count-edits` is the one definition of what a harden cycle changed. A retyped count is the
    # thing that drifts from the gate's reading of it.
    repo = tmp / "repo"
    repo.mkdir()
    for args in (["git", "init", "-q", "."], ["git", "config", "user.email", "t@t"],
                 ["git", "config", "user.name", "t"]):
        sh(args, cwd=repo)
    (repo / "a").write_text("1\n")
    sh(["git", "add", "-A"], cwd=repo)
    sh(["git", "commit", "-qm", "seed"], cwd=repo)
    check("a clean tree counts zero edits",
          "edits=0" in sh([sys.executable, str(helper), "harden-set", "--cycle", "1",
                           "--count-edits"], cwd=repo, env=env).stdout)
    (repo / "a").write_text("2\n")
    (repo / "b").write_text("new\n")
    got = sh([sys.executable, str(helper), "harden-set", "--cycle", "1", "--count-edits"],
             cwd=repo, env=env).stdout
    check("an uncommitted change and an untracked file both count", "edits=2" in got, got.strip())
    snapshot = (home / ".claude/harden-state.json").read_text()
    bad = sh([sys.executable, str(helper), "harden-set", "--cycle", "1"], cwd=repo, env=env)
    check("harden-set refuses to guess an edit count", bad.returncode != 0, bad.stderr[-120:])
    check("a refused command leaves the state files exactly as they were",
          (home / ".claude/harden-state.json").read_text() == snapshot,
          "the error path wrote to the file")

    # A branch with no upstream is the pre-PR configuration, and `@{u}..HEAD` has no answer there:
    # on #255 and #229 a cycle that committed 9 and 3 commits scored edits=0, which the gate reads as
    # converged. The commit half is measured against the head the previous cycle of the same run
    # recorded instead.
    sh(["git", "add", "-A"], cwd=repo)
    sh(["git", "commit", "-qm", "cycle one's work"], cwd=repo)
    got = sh([sys.executable, str(helper), "--owner", "777", "harden-set", "--cycle", "1",
              "--count-edits"], cwd=repo, env=env).stdout
    check("with no upstream and no earlier cycle, the commit half is reported unmeasured",
          "commit half not measured" in got, got.strip())
    (repo / "c").write_text("cycle two\n")
    sh(["git", "add", "-A"], cwd=repo)
    sh(["git", "commit", "-qm", "cycle two's work"], cwd=repo)
    got = sh([sys.executable, str(helper), "--owner", "777", "harden-set", "--cycle", "2",
              "--count-edits"], cwd=repo, env=env).stdout
    check("a committed cycle on an upstreamless branch counts its commit", "edits=1" in got,
          got.strip())
    got = sh([sys.executable, str(helper), "--owner", "888", "harden-set", "--cycle", "3",
              "--count-edits"], cwd=repo, env=env).stdout
    check("another session's head is not consumed as this run's baseline",
          "commit half not measured" in got, got.strip())
    state = json.loads((home / ".claude/harden-state.json").read_text())
    # keyed by the RESOLVED working directory, which on macOS is not the string we passed as cwd
    repo_key = next(k for k in state if k.endswith("/repo"))
    state[repo_key]["head"] = "0" * 40
    (home / ".claude/harden-state.json").write_text(json.dumps(state))
    got = sh([sys.executable, str(helper), "--owner", "888", "harden-set", "--cycle", "4",
              "--count-edits"], cwd=repo, env=env).stdout
    check("a recorded head that no longer resolves is reported, not counted as zero",
          "no longer resolves" in got, got.strip())


# ─────────────────────────────────────────────────────────── scheduling ──


def test_waves(tmp: Path) -> None:
    print("\nwave scheduling and the retro barrier")
    check("a queue shorter than the width is one wave",
          pool.plan_waves(list(range(2)), 3) == [[0, 1]])
    check("the queue is split into waves of the configured width",
          pool.plan_waves(list(range(5)), 2) == [[0, 1], [2, 3], [4]])
    check("one worker is one ticket per wave, i.e. today's behaviour",
          pool.plan_waves(list(range(3)), 1) == [[0], [1], [2]])

    cfg = pool.merge(pool.DEFAULTS, {"retro": {"min_records": 2},
                                     "parallel": {"max_workers": 2}})
    marks = pool.retro_forecast(4, 0, cfg)
    waves = len(pool.plan_waves(list(range(4)), 2))
    # Four tickets two-at-a-time is TWO waves, so every mark must land on wave 1 or 2. The
    # ticket-keyed forecast this replaces marked positions 2 and 4 — position 4 being a ticket that,
    # at this width, is worked in the same wave as the one before it and cannot follow a retro.
    check("no mark falls outside the waves that exist", max(marks) <= waves, str(marks))
    check("a full wave meets the threshold, so the retro follows wave 1",
          any("retro" in m for m in marks.get(1, [])), str(marks))
    check("the wave after a retro is marked as the first to read the changed skills",
          any("changed skills" in m for m in marks.get(2, [])), str(marks))
    check("a wave that banks too few records is not marked",
          not pool.retro_forecast(1, 0, cfg), str(pool.retro_forecast(1, 0, cfg)))
    check("with one worker the marks are per ticket again, as they always were",
          pool.retro_forecast(4, 0, pool.merge(cfg, {"parallel": {"max_workers": 1}}))
          == {2: ["retro fires after this wave"], 3: ["first wave reading changed skills"],
              4: ["retro fires after this wave"]}, str(pool.retro_forecast(4, 0, pool.merge(
                  cfg, {"parallel": {"max_workers": 1}}))))


def test_parallel_run(tmp: Path) -> None:
    print("\na real parallel invocation")
    origin, work = git_fixture(tmp)
    root = tmp / "sa"
    one = standalone_fixture(root, "sa1", 8081, 3316)
    two = standalone_fixture(root, "sa2", 8083, 3318)
    real_lessons_before = {p.name for p in REAL_LESSONS.glob("*.md")}
    marker = tmp / "sessions.txt"
    stub = stub_claude(tmp / "claude-stub", marker, sleep=1.0)

    cfg = pool.merge(pool.DEFAULTS, {
        "claude": {"binary": str(stub)},
        "parallel": {"max_workers": 2, "standalones": [str(one), str(two)]},
        "ticket": {"timeout_seconds": 120, "quiet_seconds": 120},
    })
    say = pool.Say(tmp / "run.md")
    slots = pool.build_slots(cfg, 2, tmp / "m2")
    base = pool.remote_head(work)
    jobs = [{"slug": "o/r", "path": work, "ticket": str(100 + i), "url": f"u{i}",
             "title": f"t{i}", "key": f"o/r#{100 + i}"} for i in range(2)]

    with isolated(tmp):
        results = pool.run_wave(jobs, slots, cfg, {}, say, {str(work): base})

    check("both tickets ran", len(results) == 2, str(results))
    lines = [l for l in marker.read_text().splitlines() if l.strip()]
    check("both sessions actually started", len(lines) == 2, str(lines))
    cwds = {l.split("|")[0] for l in lines}
    check("each session ran in its own worktree", len(cwds) == 2, str(cwds))
    # RESOLVED, because a session's `$PWD` is physical and `work` is logical — compared as written
    # this case could not fail, and a mutation that ran every session in the operator's own checkout
    # left it green.
    check("neither ran in the operator's checkout",
          str(work.resolve()) not in cwds and str(work) not in cwds, str(cwds))
    homes = {l.split("|")[1] for l in lines}
    check("each session got its own standalone", homes == {str(one), str(two)}, str(homes))
    m2s = {l.split("|")[2] for l in lines}
    check("each session got its own maven repository", len(m2s) == 2, str(m2s))
    spans = sorted((float(l.split("|")[4]), float(l.split("|")[5])) for l in lines)
    overlap = min(spans[0][1], spans[1][1]) - max(spans[0][0], spans[1][0])
    check("the two sessions overlapped in time", overlap > 0.5,
          f"overlap {overlap:.2f}s of two 1s sessions")

    check("the driver capture landed in the suite's own tree",
          list((tmp / "state/skill-lessons").glob("*.md")) != [],
          "no record was written anywhere the suite can see")
    check("the operator's real skill-lessons gained nothing",
          real_lessons_before == {p.name for p in REAL_LESSONS.glob("*.md")},
          str({p.name for p in REAL_LESSONS.glob("*.md")} - real_lessons_before))
    check("the module's paths were restored after isolation",
          pool.LEDGER == Path.home() / ".claude/pipeline/ledger.json")


def test_say_is_thread_safe(tmp: Path) -> None:
    print("\noperator output under concurrency")
    say = pool.Say(tmp / "concurrent.md")

    def spam(n: int) -> None:
        for i in range(40):
            say.for_ticket(f"#{n}")(f"line {i}")

    threads = [threading.Thread(target=spam, args=(n,)) for n in range(4)]
    for t in threads:
        t.start()
    for t in threads:
        t.join()
    lines = [l for l in (tmp / "concurrent.md").read_text().splitlines() if l.strip()]
    check("no line is lost to an interleaved write", len(lines) == 160, f"{len(lines)} of 160")
    check("every line says which ticket produced it",
          all(l.lstrip().startswith("#") for l in lines))


# ────────────────────────────────────────────────── review-pass regressions ──


def test_record_attribution(tmp: Path) -> None:
    print("\nattributing a run record to the run that wrote it")
    with isolated(tmp):
        lessons = pool.LESSONS
        before = pool.lesson_files()
        (lessons / "2026-01-01-repo-266.md").write_text("mine\n")
        (lessons / "2026-01-01-repo-297.md").write_text("a sibling's\n")
        got = pool.record_written(before, "266", {"297"})
        check("a fresh record carrying this ticket's number is taken",
              got and got.name.endswith("-266.md"), str(got))
        got = pool.record_written(before, "297", {"266"})
        check("and the sibling's is taken by the sibling",
              got and got.name.endswith("-297.md"), str(got))

        # The case the live-set version got wrong: a sibling finishes first, so it is no longer
        # "in flight", and the last run standing inherits the record it just wrote.
        before2 = pool.lesson_files()
        (lessons / "2026-01-01-a-lesson-about-hooks.md").write_text("the sibling's, unnumbered\n")
        (lessons / "2026-01-01-repo-310.md").write_text("the sibling's, numbered\n")
        got = pool.record_written(before2, "266", {"310"})
        check("a fresh record carrying a SIBLING's number is never taken",
              got is None or "310" not in got.name, str(got))
        got = pool.record_written(before2, "266", set())
        check("with no siblings the unnumbered fallback still finds a record", got is not None)


def test_crash_does_not_clobber(tmp: Path) -> None:
    print("\na worker that raises after recording its outcome")
    # EVERYTHING here runs inside `isolated`. `crash_entry` writes LEDGER unconditionally, and these
    # three fixtures used to sit above the `with`, so running the suite as documented left `o/r#1`,
    # `o/r#2` and `o/r#3` in the operator's real ledger — found there, with `pr: 412`, making every
    # subsequent driver start query a PR that does not exist. A case that writes real state is not a
    # case, it is a second pipeline.
    with isolated(tmp):
        _crash_cases(tmp)


def _crash_cases(tmp: Path) -> None:
    done = {"o/r#1": {"status": "ready", "pr": 412, "attempts": 1, "cost_usd": 3.2,
                      "session_id": "abc"}}
    status = pool.crash_entry(done, "o/r#1", RuntimeError("boom"))
    check("the recorded outcome survives the crash", status == "ready" and done["o/r#1"]["pr"] == 412,
          str(done))
    check("the crash is still on the record",
          any("RuntimeError" in f for f in done["o/r#1"]["flags"]), str(done["o/r#1"]))
    check("a crash after an outcome does not spend a second attempt",
          done["o/r#1"]["attempts"] == 1, str(done["o/r#1"]))

    running = {"o/r#2": {"status": "running", "attempts": 1}}
    status = pool.crash_entry(running, "o/r#2", RuntimeError("boom"))
    check("a worker that died before recording anything is an error",
          status == "error", str(running))
    check("and that one does spend an attempt", running["o/r#2"]["attempts"] == 2, str(running))

    fresh: dict = {}
    check("a key the ledger has never seen is an error too",
          pool.crash_entry(fresh, "o/r#3", RuntimeError("boom")) == "error", str(fresh))

    # A driver killed outright writes no record of its own death, and the sentinel is the only trace.
    if True:
        left = {"o/r#4": {"status": "running", "attempts": 1, "last_run": "2026-01-01T00:00:00"},
                "o/r#5": {"status": "ready", "pr": 9, "attempts": 1}}
        reaped = pool.reap_running(left, pool.Say(tmp / "reap.md"))
        check("a ticket left running by a dead driver is closed out",
              left["o/r#4"]["status"] == "error" and len(reaped) == 1, str(left))
        check("and it spends the attempt it really used",
              left["o/r#4"]["attempts"] == 2, str(left["o/r#4"]))
        check("a finished ticket is left alone", left["o/r#5"]["status"] == "ready", str(left))
        check("nothing to reap reports nothing",
              pool.reap_running({"a": {"status": "ready"}}, pool.Say(tmp / "reap2.md")) == [])


def test_nothing_ran(tmp: Path) -> None:
    print("\na status that means nothing ran")
    prior = {"status": "draft", "pr": 412, "pr_url": "u", "attempts": 2, "duration_s": 8123,
             "turns": 900, "cost_usd": 41.5, "session_id": "abc", "stream": "/x.jsonl",
             "record": "/r.md", "slot": "slot-1", "flags": ["an old flag"]}
    got = pool.nothing_ran(prior, "worktree-blocked", "a previous run left it unreleased")
    check("the previous attempt's runtime is not carried onto this one",
          "duration_s" not in got and "turns" not in got and "cost_usd" not in got, str(got))
    check("nor its session, stream or record",
          not any(k in got for k in ("session_id", "stream", "record", "slot")), str(got))
    check("nor its flags", got["flags"] == [], str(got))
    # The ledger's memory of a PR is what stops a second run opening a second PR for one issue.
    check("the PR the ticket already has IS kept", got["pr"] == 412 and got["pr_url"] == "u", str(got))
    check("and the attempt count is neither spent nor lost", got["attempts"] == 2, str(got))
    check("the status and reason are this attempt's",
          got["status"] == "worktree-blocked" and got["note"].startswith("a previous run"), str(got))


def test_shared_maven_repo(tmp: Path) -> None:
    print("\nresolving the repository the slot heads read through")
    override = tmp / "elsewhere"
    cfg = pool.merge(pool.DEFAULTS, {"parallel": {"shared_m2": str(override)}})
    check("an explicit parallel.shared_m2 wins", pool.shared_maven_repo(cfg) == override)
    check("with nothing configured it is the default",
          pool.shared_maven_repo(pool.merge(pool.DEFAULTS, {})) == pool.SHARED_M2)

    # A <localRepository> in settings.xml moves it, and a tail pointing where maven is not looking
    # is a tail with nothing in it — every offline build then fails on its first dependency.
    home = tmp / "home"
    (home / ".m2").mkdir(parents=True)
    (home / ".m2/settings.xml").write_text(
        "<settings>\n  <localRepository>${user.home}/somewhere/repo</localRepository>\n</settings>\n")
    saved = pool.HOME
    try:
        pool.HOME = home
        check("a settings.xml localRepository is read",
              pool.shared_maven_repo(pool.merge(pool.DEFAULTS, {}))
              == home / "somewhere/repo",
              str(pool.shared_maven_repo(pool.merge(pool.DEFAULTS, {}))))
    finally:
        pool.HOME = saved


def test_db_port_hosts(tmp: Path) -> None:
    print("\nreading a standalone's database port")
    for host in ("127.0.0.1", "localhost", "0.0.0.0"):
        d = tmp / f"sa-{host}"
        d.mkdir()
        (d / "openmrs-standalone.jar").write_text("")
        (d / "openmrs-runtime.properties").write_text(
            f"connection.url=jdbc:mariadb://{host}:3316/openmrs?autoReconnect=true\ntomcatport=8081\n")
        ports = pool.standalone_ports(d)
        check(f"a database url on {host} publishes its port", ports["dbport"] == 3316, str(ports))

    # Two instances that agree on the database port cannot both start, whatever the host is spelled.
    a, b = tmp / "sa-localhost", tmp / "sa-0.0.0.0"
    cfg = pool.merge(pool.DEFAULTS, {"parallel": {"max_workers": 2,
                                                  "standalones": [str(a), str(b)]}})
    check("two standalones sharing a database port are refused",
          any("dbport" in p for p in pool.slot_problems(cfg, 2)),
          str(pool.slot_problems(cfg, 2)))


def test_skills_commands_run(tmp: Path) -> None:
    """Every `gate-state` invocation the skills tell a run to type, executed as written.

    A skill naming a flag the helper does not have fails at 3am inside an unattended run, and the
    only symptom is a gate entry that was never written — which is the gate's fail-OPEN case. This is
    the one thing that keeps four markdown files and one CLI in step, so it reads the invocations out
    of the skills rather than restating them here.
    """
    print("\ngate-state invocations as the skills write them")
    helper = HERE / "gate-state"
    home = tmp / "home"
    (home / ".claude").mkdir(parents=True)
    env = {**os.environ, "HOME": str(home), "PPID": str(os.getpid())}
    repo = tmp / "repo"
    repo.mkdir()
    for args in (["git", "init", "-q", "."], ["git", "config", "user.email", "t@t"],
                 ["git", "config", "user.name", "t"]):
        sh(args, cwd=repo)
    (repo / "a").write_text("1\n")
    sh(["git", "add", "-A"], cwd=repo)
    sh(["git", "commit", "-qm", "seed"], cwd=repo)

    skills = Path.home() / ".claude/skills"
    found = []
    for name in ("resolve-ticket", "pr-harden", "harden", "ticket-pool"):
        text = (skills / name / "SKILL.md").read_text()
        for m in re.finditer(r"(?:~/\.claude/pipeline/)?gate-state ([^`\n]+)", text):
            invocation = m.group(1).strip().rstrip("`").strip()
            if invocation and not invocation.startswith("("):
                found.append((name, invocation))
    check("the skills do document the helper", len(found) >= 8, f"only found {len(found)}")

    bad = []
    for name, invocation in found:
        # Run it exactly as written, through a shell, so $PPID and the quoting are the skill's own.
        got = subprocess.run(f"{helper} {invocation}", shell=True, cwd=str(repo), env=env,
                             capture_output=True, text=True)
        if got.returncode != 0:
            bad.append(f"{name}: `gate-state {invocation}` -> {got.stderr.strip()[-160:]}")
    check("every documented invocation runs", not bad, "; ".join(bad))

    # And the ones that must be understood as a pair really are one: an await written by the
    # resolve-ticket form has to be visible to BOTH gates, which is the whole of Step 7.
    sh([sys.executable, str(helper), "--owner", str(os.getpid()), "await", "x"], cwd=repo, env=env)
    both = [json.loads((home / ".claude" / f).read_text()).get(str(repo.resolve()), {}).get("awaiting")
            for f in ("pr-harden-state.json", "harden-state.json")]
    check("the default-scope await lands in both gates", all(both), str(both))



def test_pool_gate_state_via_helper(tmp: Path) -> None:
    """The driver must not read-modify-write the gate files itself.

    Those files are shared with live claude sessions, which write them through `gate-state` under an
    exclusive flock. The driver is concurrent by construction, so an unlocked read-modify-write here
    loses updates against its own threads AND against every live session — resurrecting an entry a
    finished run cleared, which blocks the next session in that path for six hours. These cases drive
    `pool-run`'s own functions, not the helper.
    """
    print("\nthe driver's gate-state access is serialised")
    helper = HERE / "gate-state"
    check("the helper is installed and executable", os.access(helper, os.X_OK))
    if not os.access(helper, os.X_OK):
        return

    home = tmp / "home"
    (home / ".claude").mkdir(parents=True)
    dirs = []
    for i in range(20):
        d = tmp / f"wt{i}"
        d.mkdir()
        dirs.append(d.resolve())

    saved_home, saved_gs = os.environ.get("HOME"), pool.GATE_STATE
    os.environ["HOME"] = str(home)
    pool.GATE_STATE = helper
    try:
        for d in dirs:
            sh([sys.executable, str(helper), "pr-set", "--pr", "9", "--round", "1",
                "--phase", "building", "--blocking", "0"], cwd=d)
        state = home / ".claude" / "pr-harden-state.json"
        check("twenty entries were written", len(json.loads(state.read_text())) == 20)

        reports: dict[Path, list[str]] = {}

        def clear(d: Path) -> None:
            reports[d] = pool.clear_gate_state(d, lambda *_a, **_k: None)

        threads = [threading.Thread(target=clear, args=(d,)) for d in dirs]
        for t in threads:
            t.start()
        for t in threads:
            t.join()

        left = json.loads(state.read_text())
        check("no entry survives twenty concurrent clears", left == {},
              f"{len(left)} left: {sorted(left)[:2]}")
        check("every clear reported the entry it removed",
              all(len(reports.get(d) or []) == 1 for d in dirs),
              f"{sum(1 for d in dirs if not reports.get(d))} reported nothing")
        check("the report names the phase it found",
              all("phase=building" in (reports.get(d) or [""])[0] for d in dirs))

        sh([sys.executable, str(helper), "pr-set", "--pr", "42", "--round", "3",
            "--phase", "reviewed", "--blocking", "0"], cwd=dirs[0])
        check("read_gate_state reads this worktree's pr entry",
              pool.read_gate_state(dirs[0]).get("pr") == 42)
        check("read_gate_state is empty for a worktree with no entry",
              pool.read_gate_state(dirs[1]) == {})

        # The helper must be the only writer: with it unavailable the driver reports and clears
        # nothing rather than falling back to racing the files itself.
        pool.GATE_STATE = tmp / "no-such-helper"
        before = state.read_text()
        check("with no helper it clears nothing", pool.clear_gate_state(dirs[0], lambda *_a: None) == [])
        check("with no helper the file is untouched", state.read_text() == before)
    finally:
        pool.GATE_STATE = saved_gs
        if saved_home is None:
            os.environ.pop("HOME", None)
        else:
            os.environ["HOME"] = saved_home


def test_save_json_temp_is_private(tmp: Path) -> None:
    """A fixed `.tmp` suffix is shared by every concurrent writer, and `write_text` truncates first."""
    print("\nsave_json's temp path is private to the writer")
    target = tmp / "x.json"
    errors: list[str] = []

    def writer(n: int) -> None:
        for _ in range(40):
            try:
                pool.save_json(target, {"who": n, "pad": "x" * 4000})
                json.loads(target.read_text())
            except Exception as exc:
                errors.append(f"{type(exc).__name__}: {exc}")

    threads = [threading.Thread(target=writer, args=(n,)) for n in range(8)]
    for t in threads:
        t.start()
    for t in threads:
        t.join()
    check("concurrent writers never publish unparseable json", not errors, str(errors[:2]))
    check("no temp file is left behind", not list(tmp.glob("x.json.tmp*")))


def test_claim_and_release(tmp: Path) -> None:
    """Hand-launched sessions: the setup the driver would otherwise hand out.

    Two `claude` sessions started by hand share a checkout, a maven repository and a standalone, and
    none of the co-tenancy scoping engages because `$CLAUDE_PIPELINE_SLOT` is unset. A claim is how an
    operator gets the same three things the driver gives a worker, without a driver.
    """
    print("\nclaiming a slot for a hand-launched session")
    origin, work = git_fixture(tmp)
    root = tmp / "sa"
    one = standalone_fixture(root, "sa1", 8081, 3316)
    two = standalone_fixture(root, "sa2", 8083, 3318)
    cfg = pool.merge(pool.DEFAULTS, {
        "repos": {"o/r": str(work)},
        "parallel": {"max_workers": 2, "standalones": [str(one), str(two)]},
    })

    with isolated(tmp):
        say = pool.Say(tmp / "claim.md")
        base = pool.remote_head(work)

        a = pool.claim_slot(cfg, "o/r", "266", work, base, say)
        b = pool.claim_slot(cfg, "o/r", "297", work, base, say)
        check("two claims get two slots", a and b and a["slot"].name != b["slot"].name,
              f"{a and a['slot'].name} / {b and b['slot'].name}")
        check("each claim gets its own worktree", a["worktree"] != b["worktree"])
        check("the worktree is real and at the remote head",
              (a["worktree"] / "README.md").is_file()
              and sh(["git", "-C", str(a["worktree"]), "rev-parse", "HEAD"]).stdout.strip() == base)
        check("each claim gets its own standalone",
              a["slot"].standalone != b["slot"].standalone)

        # The whole point: the three variables a hand-launched session does not have.
        env = a["env"]
        check("the claim hands over a standalone", env["OPENMRS_STANDALONE_HOME"] == str(one))
        check("the claim hands over a private maven head",
              f"-Dmaven.repo.local={a['slot'].m2}" in env["MAVEN_ARGS"])
        check("the claim declares co-tenancy, which is what engages the skills' scoping",
              env["CLAUDE_PIPELINE_SLOT"] == a["slot"].name)

        # A third claim has nowhere to go, and must say so rather than double-book a standalone.
        third = pool.claim_slot(cfg, "o/r", "310", work, base, say)
        check("a claim with no slot left is refused, not double-booked", third is None)

        # The driver and hand-launched sessions must not both be using the standalones.
        check("an outstanding claim is a fatal preflight problem for the driver",
              any("claim" in x for x in pool.claim_problems(cfg)), str(pool.claim_problems(cfg)))

        # resolve-ticket writes a gate entry at its Step 1, keyed on the worktree. A claim of the
        # same ticket reuses that path, so a leftover would block the NEXT session's Stop gate.
        import subprocess as _sp
        _sp.run([str(HERE / "gate-state"), "--owner", "9", "pr-set", "--ticket", "297",
                 "--round", "1", "--phase", "building", "--blocking", "0"],
                cwd=str(b["worktree"]), capture_output=True)
        key = str(b["worktree"].resolve())
        check("the session's gate entry exists before release",
              key in json.loads(pool.PR_STATE.read_text()), "nothing to clean up")

        freed = pool.release_claim(cfg, "297", say)
        check("releasing a removed worktree takes its gate entry with it",
              key not in json.loads(pool.PR_STATE.read_text()),
              "a stale `building` entry would block the next session in that path for 6h")
        check("releasing frees the slot", freed and not (pool.SLOTS / f"{b['slot'].name}.json").exists(),
              str(freed))
        check("and removes its clean worktree", not b["worktree"].is_dir())
        again = pool.claim_slot(cfg, "o/r", "310", work, base, say)
        check("the freed slot can be claimed again", again is not None)
        check("and it is the one that was freed", again["slot"].name == b["slot"].name)

        # A run that left work behind: the worktree is kept and named, but the STANDALONE is free.
        (a["worktree"] / "UNSAVED.md").write_text("mid-edit\n")
        out = pool.release_claim(cfg, "266", say)
        check("a release reports a worktree it could not remove",
              out and "kept" in out["worktree"], str(out))
        check("but the slot is freed anyway, because the standalone is idle now",
              not (pool.SLOTS / f"{a['slot'].name}.json").exists())

        # Editing parallel.standalones under a running session re-points a slot NAME at a different
        # instance, so a free name can carry an instance somebody is already using.
        swapped = pool.merge(cfg, {"parallel": {"standalones": [str(two), str(one)]}})
        before = {l["standalone"] for l in pool.active_leases().values()}
        moved = pool.claim_slot(swapped, "o/r", "555", work, base, say)
        if moved:
            check("a reordered config never hands out an instance already claimed",
                  str(moved["slot"].standalone) not in before,
                  f"{moved['slot'].standalone} was already leased")
            pool.release_claim(swapped, "555", say)
        else:
            check("a reordered config never hands out an instance already claimed", True)

        # A lease whose worktree is gone is a session that ended without releasing.
        pool.claim_slot(cfg, "o/r", "401", work, base, say)
        lease = next(pool.SLOTS.glob("*.json"))
        import shutil as _sh
        _sh.rmtree(json.loads(lease.read_text())["worktree"], ignore_errors=True)
        sh(["git", "-C", str(work), "worktree", "prune"])
        check("a lease whose worktree is gone is reclaimed, not held forever",
              pool.claim_slot(cfg, "o/r", "402", work, base, say) is not None)


def test_work_needs_a_terminal(tmp: Path) -> None:
    """`--work` launches an INTERACTIVE session, so refusing without a tty is the whole contract.

    The mistake this catches is thinking `pool-run` is a skill and running it from inside a Claude
    Code session. It is not — skills are what you type inside a session, this is a shell script that
    starts one — and without the guard the session starts with no terminal, renders none of its
    interface, and says nothing about why.
    """
    print("\n--work without a terminal")
    cfgpath = tmp / "cfg.json"
    cfgpath.write_text(json.dumps({"label": "x", "repos": {}, "retro": {"enabled": False}}))
    got = subprocess.run([str(HERE / "pool-run"), "--config", str(cfgpath), "--work", "266"],
                         capture_output=True, text=True, stdin=subprocess.DEVNULL)
    check("it refuses when there is no terminal", got.returncode != 0, got.stdout[-200:])
    check("and says so in terms of what it was about to do",
          "needs a terminal" in got.stdout, got.stdout[-300:])
    check("and points at the command that prepares one without launching",
          "--claim 266" in got.stdout, got.stdout[-300:])


def test_claim_cli(tmp: Path) -> None:
    """`--claim` twice, through the real CLI, because that is where the defect was.

    `claim_slot` never had it: the outstanding-claims refusal lived in the shared `preflight`, so the
    DRIVER's "a hand-launched session is using that standalone" check fired on the second CLAIM and
    the feature worked exactly once. A test that calls `claim_slot` directly cannot see that.
    """
    print("\nclaiming twice through the CLI")
    origin, work = git_fixture(tmp)
    root = tmp / "sa"
    one = standalone_fixture(root, "sa1", 8081, 3316)
    two = standalone_fixture(root, "sa2", 8083, 3318)
    home = tmp / "home"
    (home / ".claude").mkdir(parents=True)
    cfgpath = tmp / "cfg.json"
    cfgpath.write_text(json.dumps({
        "label": "x", "repos": {"o/r": str(work)}, "source_repo": str(work),
        # slug parity and the GitHub label are the driver's business, not this case's
        "retro": {"enabled": False},
        "parallel": {"max_workers": 2, "standalones": [str(one), str(two)]},
    }))

    with isolated(tmp):
        say = pool.Say(tmp / "cli.md")
        base = pool.remote_head(work)
        cfg = pool.merge(pool.DEFAULTS, json.loads(cfgpath.read_text()))
        first = pool.claim_slot(cfg, "o/r", "266", work, base, say)
        check("the first claim is taken", first is not None)

        # The real question: does the SHARED preflight refuse the second one?
        REFUSAL = "slot claim(s) are outstanding"
        driving = pool.preflight(cfg, say, want_label=False, driving=True)
        claiming = pool.preflight(cfg, say, want_label=False, driving=False)
        check("the driver is refused while a claim is held",
              any(REFUSAL in x for x in driving), str(driving))
        check("a second CLAIM is not refused by the first",
              not any(REFUSAL in x for x in claiming), str(claiming))
        check("and that refusal is the ONLY difference between the two",
              [x for x in driving if x not in claiming]
              and all(REFUSAL in x for x in driving if x not in claiming),
              str([x for x in driving if x not in claiming]))

        second = pool.claim_slot(cfg, "o/r", "297", work, base, say)
        check("so the second claim goes through", second is not None)
        check("on the other standalone",
              second and second["slot"].standalone != first["slot"].standalone)
        pool.release_claim(cfg, "266", say)
        pool.release_claim(cfg, "297", say)
        check("and both give their slots back", pool.active_leases() == {})


def test_work_one_command(tmp: Path) -> None:
    """`pool-run --work 266` — claim, launch the session, release when it exits.

    The claim/export/launch/release sequence is correct and nobody will remember it. This is the same
    sequence with the operator taken out of the middle: whatever they would have pasted, the driver
    sets, and whatever they would have released, it releases.
    """
    print("\nworking one ticket with one command")
    origin, work = git_fixture(tmp)
    root = tmp / "sa"
    one = standalone_fixture(root, "sa1", 8081, 3316)
    two = standalone_fixture(root, "sa2", 8083, 3318)
    seen = tmp / "launched.txt"
    stub = tmp / "claude-stub"
    stub.write_text("#!/bin/bash\n"
                    f'echo "$PWD|$OPENMRS_STANDALONE_HOME|$CLAUDE_PIPELINE_SLOT|$MAVEN_ARGS|$*" >> {seen}\n'
                    'exit ${STUB_EXIT:-0}\n')
    stub.chmod(0o755)
    cfg = pool.merge(pool.DEFAULTS, {
        "repos": {"o/r": str(work)},
        "claude": {"binary": str(stub)},
        "parallel": {"max_workers": 2, "standalones": [str(one), str(two)]},
    })

    with isolated(tmp):
        say = pool.Say(tmp / "work.md")
        rc = pool.work_in_session(cfg, "o/r", "266",
                                  {"url": "https://example/266", "number": 266}, work, say)
        check("it exits with the session's own status", rc == 0, str(rc))
        line = seen.read_text().strip().split("|")
        check("the session was launched in the ticket's worktree",
              line[0].endswith("o-r-266"), line[0])
        check("with a standalone of its own", line[1] == str(one), line[1])
        check("with co-tenancy declared", line[2] == "slot-1", line[2])
        check("with a private maven head", "m2/slot-1" in line[3], line[3])
        check("and the skill already invoked, so there is nothing left to type",
              line[4].strip() == "/resolve-ticket https://example/266", repr(line[4]))

        check("the slot is given back when the session exits", pool.active_leases() == {},
              "an unqualified release is ambiguous once two repos are configured, and refusing "
              "would leave this session's own slot held")

        # Remote Control is a flag on the LAUNCH, so a launcher that does not pass it silently costs
        # the operator phone monitoring — with nothing in the session to say why it is missing.
        seen.unlink()
        rc_cfg = pool.merge(cfg, {"claude": {"remote_control": True}})
        pool.work_in_session(rc_cfg, "o/r", "266",
                             {"url": "https://example/266"}, work, say)
        args = seen.read_text().strip().split("|")[4]
        check("remote control is passed to the session", "--remote-control" in args, args)
        check("named for the ticket, not the host, so two are tellable apart on a phone",
              "-266" in args.split("--remote-control")[1].split()[0], args)
        check("and the skill invocation survives beside it",
              "/resolve-ticket https://example/266" in args, args)

        seen.unlink()
        pool.work_in_session(pool.merge(cfg, {"claude": {"remote_control": False}}), "o/r", "266",
                             {"url": "https://example/266"}, work, say)
        check("and it is off unless asked for",
              "--remote-control" not in seen.read_text(), seen.read_text())

        # `pool.json` documents its `claude` block as reaching EVERY session, and for a while
        # `--work` read none of it — an operator's configured model silently did not apply to the
        # sessions they actually watched.
        seen.unlink()
        full = pool.merge(cfg, {"claude": {"skip_permissions": True, "model": "opus",
                                           "effort": "high", "max_budget_usd": 40,
                                           "extra_args": ["--verbose"]}})
        pool.work_in_session(full, "o/r", "266", {"url": "https://example/266"}, work, say)
        args = seen.read_text().strip().split("|")[4]
        check("permission prompts are skipped when asked for",
              "--dangerously-skip-permissions" in args, args)
        for flag, value in (("--model", "opus"), ("--effort", "high"),
                            ("--max-budget-usd", "40")):
            check(f"the configured {flag[2:]} reaches the session",
                  f"{flag} {value}" in args, args)
        check("and so do extra_args", "--verbose" in args, args)
        check("with the prompt still last, where a positional belongs",
              args.strip().endswith("/resolve-ticket https://example/266"), args)

        seen.unlink()
        pool.work_in_session(cfg, "o/r", "266", {"url": "https://example/266"}, work, say)
        check("permissions are NOT skipped unless asked for",
              "--dangerously-skip-permissions" not in seen.read_text(), seen.read_text())

        # The headless driver differs on purpose: nobody is there to ask, so a prompt is a hang.
        head = pool.Session("p", tmp, pool.merge(pool.DEFAULTS, {"claude": {"model": "sonnet"}}),
                            tmp / "s", 10, 10, print).argv()
        check("a headless session always skips permissions, settings or not",
              "--dangerously-skip-permissions" in head, str(head))
        check("and reads the same shared options",
              "--model" in head and "sonnet" in head, str(head))
        check("and the clean worktree with it",
              not (pool.WORKTREES / "o-r-266").is_dir())

        # A session that fails must still release, or the next one has nowhere to go.
        import os as _os
        _os.environ["STUB_EXIT"] = "3"
        try:
            rc = pool.work_in_session(cfg, "o/r", "297",
                                      {"url": "https://example/297", "number": 297}, work, say)
        finally:
            _os.environ.pop("STUB_EXIT", None)
        check("a session that exits non-zero reports that status", rc == 3, str(rc))
        check("and still gives its slot back", pool.active_leases() == {})

        # Uncommitted work is the one thing worth keeping, and the release says so.
        held = pool.claim_slot(cfg, "o/r", "310", work, pool.remote_head(work), say)
        (held["worktree"] / "MID-EDIT.md").write_text("x\n")
        pool.release_claim(cfg, "310", say)
        check("a worktree with unsaved work survives its release",
              held["worktree"].is_dir())


def test_ctrl_c_reaches_the_session(tmp: Path) -> None:
    """Ctrl-C must interrupt the SESSION, never the launcher waiting on it.

    Ctrl-C goes to the whole foreground process group, and the session is in the launcher's. Before
    the fix `subprocess.run` raised `KeyboardInterrupt` out of the wait while the session was still
    running, the `finally` fired, and the release deleted the worktree out from under a live
    `claude`. In Claude Code Ctrl-C is how you interrupt a tool call, so this is not an edge case; it
    is the most-pressed key in the product.

    Driven for real: a separate process group, a real SIGINT to it, and a stub that catches SIGINT
    and keeps running — exactly what `claude` does.
    """
    print("\nctrl-c while the session is running")
    origin, work = git_fixture(tmp)
    root = tmp / "sa"
    one = standalone_fixture(root, "sa1", 8081, 3316)
    log = tmp / "child.log"
    stub = tmp / "claude-stub"
    stub.write_text("#!/bin/bash\n"
                    f'trap \'echo interrupted >> {log}\' INT\n'
                    f'echo started >> {log}\n'
                    "for i in 1 2 3 4 5 6 7 8; do sleep 0.5; done\n"
                    f'echo finished >> {log}\n')
    stub.chmod(0o755)

    # The launcher runs in its OWN process group so the suite can send it a real Ctrl-C without
    # signalling itself. Its config goes in a file rather than being interpolated into source.
    (tmp / "runner-cfg.json").write_text(json.dumps({
        "root": str(tmp / "state"), "pool": str(HERE / "pool-run"),
        "repo": str(work), "stub": str(stub), "standalone": str(one),
        "say": str(tmp / "runner.md")}))
    runner = tmp / "runner.py"
    runner.write_text(
        "import importlib.machinery as m, json, os, pathlib, sys\n"
        "c = json.load(open(sys.argv[1]))\n"
        "pool = m.SourceFileLoader('p', c['pool']).load_module()\n"
        "for n in ['LEDGER','LOGS','LESSONS','LAST','PR_STATE','HARDEN_STATE','UNATTENDED_DIR',\n"
        "          'WORKTREES','SLOT_M2','SLOTS','LOCK']:\n"
        "    setattr(pool, n, pathlib.Path(c['root'])/pathlib.Path(getattr(pool, n)).name)\n"
        "pool.LOGS.mkdir(parents=True, exist_ok=True)\n"
        "os.environ['CLAUDE_HOME'] = c['root']\n"
        "cfg = pool.merge(pool.DEFAULTS, {'repos': {'o/r': c['repo']},\n"
        "                                 'claude': {'binary': c['stub']},\n"
        "                                 'parallel': {'max_workers': 1,\n"
        "                                              'standalones': [c['standalone']]}})\n"
        "rc = pool.work_in_session(cfg, 'o/r', '266', {'url': 'https://example/266'},\n"
        "                          pathlib.Path(c['repo']), pool.Say(pathlib.Path(c['say'])))\n"
        "print('RC', rc)\n")

    proc = subprocess.Popen([sys.executable, str(runner), str(tmp / "runner-cfg.json")],
                            start_new_session=True, stdout=subprocess.PIPE,
                            stderr=subprocess.STDOUT, text=True)
    deadline = time.time() + 10
    while time.time() < deadline and "started" not in (log.read_text() if log.exists() else ""):
        time.sleep(0.1)
    if not (log.exists() and "started" in log.read_text()):
        proc.kill()
        check("the session started", False, proc.communicate()[0][-400:])
        return
    check("the session started", True)

    os.killpg(os.getpgid(proc.pid), signal.SIGINT)      # the operator presses ctrl-c
    out, _ = proc.communicate(timeout=30)

    body = log.read_text() if log.exists() else ""
    check("the session received the interrupt", "interrupted" in body, body)
    check("the launcher did NOT abandon it — the session ran to its own end",
          "finished" in body, body or "the launcher returned while the session was still alive")
    check("the launcher waited for it before releasing", "RC" in out, out[-200:])

    # Closing the terminal is SIGHUP, whose DEFAULT action kills the launcher outright — so the
    # release never runs and the lease outlives the session. Measured before this was handled: the
    # lease file and the worktree were both left behind for the operator to find.
    log.unlink()
    proc = subprocess.Popen([sys.executable, str(runner), str(tmp / "runner-cfg.json")],
                            start_new_session=True, stdout=subprocess.PIPE,
                            stderr=subprocess.STDOUT, text=True)
    deadline = time.time() + 10
    while time.time() < deadline and "started" not in (log.read_text() if log.exists() else ""):
        time.sleep(0.1)
    os.killpg(os.getpgid(proc.pid), signal.SIGHUP)
    proc.communicate(timeout=30)
    slots = tmp / "state/slots"
    check("a closed terminal does not leave its slot held",
          not (list(slots.glob("*.json")) if slots.is_dir() else []),
          "the lease outlived the session")
    check("and only then gave the slot back",
          not list((tmp / "state/slots").glob("*.json")) if (tmp / "state/slots").is_dir() else True)


def test_double_claim_and_live_driver(tmp: Path) -> None:
    """The two ways a claim can collide with work that is already running.

    Both were live defects, both found by trying them rather than by reading the code, and both are
    silent — the operator sees a session start normally and finds out later.
    """
    print("\nclaims that would collide with running work")
    origin, work = git_fixture(tmp)
    root = tmp / "sa"
    one = standalone_fixture(root, "sa1", 8081, 3316)
    two = standalone_fixture(root, "sa2", 8083, 3318)
    cfg = pool.merge(pool.DEFAULTS, {
        "repos": {"o/r": str(work)},
        "parallel": {"max_workers": 2, "standalones": [str(one), str(two)]}})

    with isolated(tmp):
        say = pool.Say(tmp / "collide.md")
        base = pool.remote_head(work)

        first = pool.claim_slot(cfg, "o/r", "266", work, base, say)
        check("the first claim is taken", first is not None)
        (first["worktree"] / "session-work.txt").write_text("the live session's file\n")
        sh(["git", "-C", str(first["worktree"]), "add", "-A"])
        sh(["git", "-C", str(first["worktree"]), "-c", "user.email=t@t", "-c", "user.name=t",
            "commit", "-qm", "the live session just committed"])
        head = sh(["git", "-C", str(first["worktree"]), "rev-parse", "HEAD"]).stdout.strip()

        # Same ticket again. The worktree path is derived from the ticket, so without a guard the
        # second claim REMOVES the first session's tree — it is clean, having just committed — and
        # recreates it, wiping the running session's checkout and leaving two leases on one directory.
        second = pool.claim_slot(cfg, "o/r", "266", work, base, say)
        check("a ticket that is already claimed cannot be claimed again", second is None,
              f"got a second claim on {second['slot'].name if second else None}")
        check("the running session's worktree is untouched",
              (first["worktree"] / "session-work.txt").is_file(),
              "the live session's file was deleted")
        check("and it is still on its own commit",
              sh(["git", "-C", str(first["worktree"]), "rev-parse", "HEAD"]).stdout.strip() == head)
        check("only one lease exists for it", len(pool.active_leases()) == 1,
              str(pool.active_leases()))

        # A live DRIVER holds the machine-wide lock and is using these same standalones. The driver
        # already refuses to start while a claim is held; the reverse has to hold too or the symmetry
        # is decorative.
        # A REAL other live process. Writing our own pid proves nothing: the guard treats the
        # current process as "us" on purpose, so the lock a driver takes cannot trip its own
        # preflight — and a test that writes its own pid silently exercises that branch instead.
        other = subprocess.Popen(["sleep", "30"])
        try:
            pool.LOCK.write_text(json.dumps({"pid": other.pid, "started": "now"}))
            problems = pool.preflight(cfg, say, want_label=False, driving=False)
            check("a claim refuses to start while a driver is running",
                  any("driver" in x.lower() for x in problems), str(problems))
        finally:
            other.kill()
            other.wait()
            problems = pool.preflight(cfg, say, want_label=False, driving=False)
            check("and once that driver's process is gone, the stale lock is ignored",
                  not any("driver" in x.lower() for x in problems), str(problems))
            pool.LOCK.unlink(missing_ok=True)

        check("and does not refuse once that driver is gone",
              not any("driver" in x.lower()
                      for x in pool.preflight(cfg, say, want_label=False, driving=False)))
        pool.release_claim(cfg, "266", say)


def test_same_ticket_number_in_two_repos(tmp: Path) -> None:
    """`pool.json` maps MANY repos, and issue numbers collide across them freely.

    Leases were matched on the ticket number alone while recording the repo they belonged to, so #266
    in one repo was indistinguishable from #266 in another: the one-claim-per-ticket guard refused a
    legitimate claim, and `--release 266` freed whichever lease happened to be found first.
    """
    print("\nthe same ticket number in two repositories")
    origin_a, repo_a = git_fixture(tmp / "a")
    origin_b, repo_b = git_fixture(tmp / "b")
    root = tmp / "sa"
    one = standalone_fixture(root, "sa1", 8081, 3316)
    two = standalone_fixture(root, "sa2", 8083, 3318)
    cfg = pool.merge(pool.DEFAULTS, {
        "repos": {"o/a": str(repo_a), "o/b": str(repo_b)},
        "parallel": {"max_workers": 2, "standalones": [str(one), str(two)]}})

    with isolated(tmp):
        say = pool.Say(tmp / "two-repos.md")
        a = pool.claim_slot(cfg, "o/a", "266", repo_a, pool.remote_head(repo_a), say)
        check("the first repo's #266 is claimed", a is not None)
        b = pool.claim_slot(cfg, "o/b", "266", repo_b, pool.remote_head(repo_b), say)
        check("the OTHER repo's #266 is a different ticket and may also be claimed",
              b is not None, "refused a legitimate claim as a duplicate")
        if b:
            check("and they get different worktrees", a["worktree"] != b["worktree"],
                  str(a["worktree"]))

        # Releasing an ambiguous number must not pick one at random.
        out = pool.release_claim(cfg, "266", say)
        check("an ambiguous release is refused rather than guessed",
              out is None and len(pool.active_leases()) == 2, str(pool.active_leases()))
        out = pool.release_claim(cfg, "o/b#266", say)
        check("the qualified form releases exactly one", out is not None
              and len(pool.active_leases()) == 1, str(pool.active_leases()))
        check("and it is the one named",
              next(iter(pool.active_leases().values())).get("slug") == "o/a",
              str(pool.active_leases()))
        # And `--work`'s OWN release must be qualified too, or a session in the second repo cannot
        # give its slot back while the first repo's #266 is still running: the release would be
        # ambiguous, be refused, and silently hold the slot.
        stub = tmp / "stub"
        stub.write_text("#!/bin/bash\nexit 0\n")
        stub.chmod(0o755)
        rc_cfg = pool.merge(cfg, {"claude": {"binary": str(stub)}})
        held_before = set(pool.active_leases())
        pool.work_in_session(rc_cfg, "o/b", "266", {"url": "https://example/b266"}, repo_b, say)
        after = set(pool.active_leases())
        check("a session releases its own slot even when the number is claimed elsewhere",
              len(after) == len(held_before), f"{held_before} -> {after}")
        check("and the OTHER repo's claim on that number is untouched",
              any(l.get("slug") == "o/a" for l in pool.active_leases().values()),
              str(pool.active_leases()))
        pool.release_claim(cfg, "o/a#266", say)


def test_platform_floor(tmp: Path) -> None:
    """A standalone older than the module requires cannot run the module at all.

    `require_version` is a FLOOR, so the module refuses to start and the verifier then reports a
    failure about the INSTANCE every round until the cap. This was configured wrongly by hand — a
    slot picked by matching the reference-application version in the directory name (`…-3.7.1`),
    which is not the criterion: that instance carried openmrs-core 2.8.8 while the module needed
    2.9.0-SNAPSHOT, and a `…-3.7.0` directory carried the right core.
    """
    print("\nthe platform floor")
    check("a qualifier does not change the number",
          pool.version_tuple("2.9.0-SNAPSHOT") == (2, 9, 0), str(pool.version_tuple("2.9.0-SNAPSHOT")))
    check("a release satisfies a SNAPSHOT floor of the same number",
          not (pool.version_tuple("2.9.0") < pool.version_tuple("2.9.0-SNAPSHOT")))
    check("and an older core does not",
          pool.version_tuple("2.8.8") < pool.version_tuple("2.9.0-SNAPSHOT"))
    check("a two-part version still parses", pool.version_tuple("2.9") == (2, 9, 0))
    check("junk parses to nothing rather than to zero", pool.version_tuple("beta") is None)

    repo = tmp / "module"
    repo.mkdir()
    (repo / "pom.xml").write_text(
        "<project><properties><openmrsPlatformVersion>2.9.0-SNAPSHOT"
        "</openmrsPlatformVersion></properties></project>")
    check("the requirement is read from the pom, where the number actually lives",
          pool.required_platform(repo) == "2.9.0-SNAPSHOT", str(pool.required_platform(repo)))

    def instance(name: str, core: str | None) -> Path:
        d = standalone_fixture(tmp, name, 8081 + len(name), 3316 + len(name))
        if core:
            lib = d / "tomcat/webapps/openmrs/WEB-INF/lib"
            lib.mkdir(parents=True)
            (lib / f"openmrs-api-{core}.jar").write_text("")
        return d

    good, old, unknown = instance("ok", "2.9.0-SNAPSHOT"), instance("old", "2.8.8"), instance("na", None)
    base = {"repos": {"o/m": str(repo)}}
    check("a matching core is no problem",
          pool.platform_problems(pool.merge(pool.DEFAULTS, {**base, "parallel": {
              "standalones": [str(good)]}}), 1) == [])
    problems = pool.platform_problems(pool.merge(pool.DEFAULTS, {**base, "parallel": {
        "standalones": [str(old)]}}), 1)
    check("an older core is refused", len(problems) == 1, str(problems))
    check("and the message names both versions and which slot",
          "2.8.8" in problems[0] and "2.9.0-SNAPSHOT" in problems[0] and str(old) in problems[0],
          problems[0])
    check("a core that cannot be read is reported rather than assumed good",
          len(pool.platform_problems(pool.merge(pool.DEFAULTS, {**base, "parallel": {
              "standalones": [str(unknown)]}}), 1)) == 1)
    check("only the standalones a run will actually use are checked",
          pool.platform_problems(pool.merge(pool.DEFAULTS, {**base, "parallel": {
              "standalones": [str(good), str(old)]}}), 1) == [])
    check("a repo whose pom states no platform is skipped, not guessed at",
          pool.platform_problems(pool.merge(pool.DEFAULTS, {
              "repos": {"o/x": str(tmp)}, "parallel": {"standalones": [str(old)]}}), 1) == [])


def test_work_reaches_the_ledger(tmp: Path) -> None:
    """A hand-launched run must leave the same trace a driven one does.

    Measured the hard way: two tickets were worked to ready PRs by `--work`, and answering "is it
    done?" afterwards meant querying GitHub and parsing gate state by hand, because `--status` knew
    nothing about either. The ledger exists to answer exactly that question.
    """
    print("\na --work run in the ledger")
    origin, work = git_fixture(tmp)
    one = standalone_fixture(tmp / "sa", "sa1", 8081, 3316)
    stub = tmp / "stub"
    stub.write_text("#!/bin/bash\nexit 0\n"); stub.chmod(0o755)
    cfg = pool.merge(pool.DEFAULTS, {"repos": {"o/r": str(work)}, "claude": {"binary": str(stub)},
                                     "parallel": {"max_workers": 1, "standalones": [str(one)]}})
    with isolated(tmp):
        say = pool.Say(tmp / "l.md")
        pool.work_in_session(cfg, "o/r", "266", {"url": "https://example/266"}, work, say)
        led = pool.load_json(pool.LEDGER, {})
        e = led.get("o/r#266")
        check("the run is in the ledger at all", e is not None, str(sorted(led)))
        if not e:
            return
        check("with a terminal status, not the running sentinel",
              e.get("status") not in (None, "running"), str(e.get("status")))
        check("and it records how it was launched", e.get("launched_by") == "work", str(e))
        check("with the slot it used", e.get("slot") == "slot-1", str(e.get("slot")))
        check("and it spends an attempt like any other run", e.get("attempts") == 1, str(e))


def test_dead_owner_lease_is_reclaimable(tmp: Path) -> None:
    """The last leak path: a launcher killed with SIGKILL never runs its release.

    SIGINT, SIGHUP and SIGTERM are absorbed so the release still happens; SIGKILL cannot be. A lease
    whose recorded session is gone therefore has to be reclaimable, or one `kill -9` costs a slot
    until somebody notices and runs `--release`.
    """
    print("\na lease whose session is gone")
    origin, work = git_fixture(tmp)
    one = standalone_fixture(tmp / "sa", "sa1", 8081, 3316)
    two = standalone_fixture(tmp / "sa", "sa2", 8083, 3318)
    cfg = pool.merge(pool.DEFAULTS, {"repos": {"o/r": str(work)},
                                     "parallel": {"max_workers": 2, "standalones": [str(one), str(two)]}})
    with isolated(tmp):
        say = pool.Say(tmp / "d.md")
        base = pool.remote_head(work)
        c = pool.claim_slot(cfg, "o/r", "266", work, base, say)
        check("claimed", c is not None)

        # A --claim lease records no session, because the operator starts `claude` afterwards. Its
        # liveness rule stays the worktree, and it must NOT be reclaimed just for lacking a pid.
        check("a lease with no recorded session survives", len(pool.active_leases()) == 1)

        live = subprocess.Popen(["sleep", "30"])
        try:
            pool.record_session(c["slot"].name, live.pid)
            check("a lease whose session is alive survives", len(pool.active_leases()) == 1)
        finally:
            live.kill(); live.wait()
        check("a lease whose session is gone is reclaimed", pool.active_leases() == {},
              "one kill -9 would cost that slot until somebody ran --release")
        check("but its worktree is left alone, since it may hold unseen work",
              c["worktree"].is_dir())


def test_pool_watch_live(tmp: Path) -> None:
    """`pool-watch --live`: the survey that had to be assembled by hand, and got wrong once."""
    print("\npool-watch --live")
    watch = SourceFileLoader("poolwatch", str(HERE / "pool-watch")).load_module()

    # A stream is named `<utc-stamp>-<slug>-<ticket>.jsonl`. A bare substring match put ticket 293 on
    # a file whose TIMESTAMP contained "2937" — a real false match, seen in the first run of this view.
    saved = watch.LOGS
    try:
        watch.LOGS = tmp / "logs"
        watch.LOGS.mkdir()
        (watch.LOGS / "20260826T122937Z-repo-310.jsonl").write_text("")
        (watch.LOGS / "20260827T090000Z-repo-293.jsonl").write_text("")
        stems = [x.stem for x in watch.sessions()]
        check("a ticket matches only the stem's ticket segment",
              [x for x in stems if x.endswith("-293")] == ["20260827T090000Z-repo-293"], str(stems))
        check("and a timestamp that merely contains the digits does not match",
              not "20260826T122937Z-repo-310".endswith("-293"))
    finally:
        watch.LOGS = saved

    # Every claude on this machine is not the answer to "what is running": there are dozens, and
    # burying the ones that hold slots among them is how a live run gets read as a leftover.
    check("a session with a gate entry is pipeline work",
          watch.pipeline_relevant({"gate": {"phase": "building"}, "cmd": ""}))
    check("so is one holding a slot", watch.pipeline_relevant({"slot": "slot-1", "cmd": ""}))
    check("so is one whose argv invokes the skill",
          watch.pipeline_relevant({"cmd": "claude /resolve-ticket https://x/1"}))
    check("an unrelated session is not",
          not watch.pipeline_relevant({"cmd": "claude", "gate": {}, "harden": {}, "slot": None}))



LEDGER_CHILD = """
import sys, time
from importlib.machinery import SourceFileLoader
from pathlib import Path
pool = SourceFileLoader("poolrun", sys.argv[1]).load_module()
pool.LEDGER = Path(sys.argv[2])
pool.LEDGER_FLOCK = Path(sys.argv[3])
me = sys.argv[4]
# The shape that loses data: read the WHOLE ledger, hold it while others write, then write. A real
# driver holds this snapshot for the life of a run, which is minutes to hours.
ledger = pool.load_json(pool.LEDGER, {})
time.sleep(float(sys.argv[5]))
pool.write_ledger(ledger, me, {"status": "done", "who": me})
"""


def test_ledger_cross_process(tmp: Path) -> None:
    print("\nthe ledger survives concurrent processes")
    ledger = tmp / "ledger.json"
    flock = tmp / "ledger.lock"
    child = tmp / "child.py"
    child.write_text(LEDGER_CHILD)
    pool.save_json(ledger, {"seed": {"status": "kept"}})

    # 12 children and NO stagger. With 8 and a 0.5/0.05 stagger this case passed twice in eight runs
    # with the flock deleted — `time.sleep` decided whether it overlapped. Measured at these
    # parameters: 6 trials in 6 lose data without the flock, 0 in 6 with it.
    procs = [subprocess.Popen(
        [sys.executable, str(child), str(HERE / "pool-run"), str(ledger), str(flock), f"k{i}", "0"],
        stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        for i in range(12)]
    for pr in procs:
        pr.wait()
    bad = [pr.stderr.read().decode()[-300:] for pr in procs if pr.returncode]
    check("every child exited cleanly", not bad, str(bad[:1]))

    data = json.loads(ledger.read_text())
    missing = [f"k{i}" for i in range(12) if f"k{i}" not in data]
    check("no write is lost to another process's snapshot", not missing, f"lost {missing}")
    # Not a sensitive check — the pre-fix whole-snapshot write passes it too, since `seed` was in
    # every writer's snapshot. Kept as a statement of intent, not as a guard.
    check("a key nobody wrote is left alone", data.get("seed", {}).get("status") == "kept")
    check("each write kept its own value",
          all(data.get(f"k{i}", {}).get("who") == f"k{i}" for i in range(12)))

    saved = (pool.LEDGER, pool.LEDGER_FLOCK)
    pool.LEDGER, pool.LEDGER_FLOCK = ledger, flock
    try:
        pool.write_ledger({}, "after", {"status": "done"})
        check("the lock is released for the next writer", "after" in json.loads(ledger.read_text()))
        with contextlib.suppress(RuntimeError):
            with pool.ledger_held() as d:
                d["poison"] = {"status": "half"}
                raise RuntimeError("boom")
        check("a failed mutation writes nothing", "poison" not in json.loads(ledger.read_text()))
        pool.write_ledger({}, "post", {"status": "done"})
        check("and the lock survives that exception", "post" in json.loads(ledger.read_text()))

        # merge_ledger_changes must publish only what changed, never the whole snapshot.
        pool.save_json(ledger, {"a": {"v": 1}, "b": {"v": 1}})
        stale = {"a": {"v": 1}, "b": {"v": 1}}
        mine = {"a": {"v": 2}, "b": {"v": 1}}
        pool.save_json(ledger, {"a": {"v": 1}, "b": {"v": 99}})   # another process moved b
        changed = pool.merge_ledger_changes(stale, mine)
        end = json.loads(ledger.read_text())
        check("only the changed key is published", changed == ["a"], str(changed))
        check("and the other process's key is not reverted", end["b"]["v"] == 99, str(end))
        check("while the change itself lands", end["a"]["v"] == 2, str(end))
    finally:
        pool.LEDGER, pool.LEDGER_FLOCK = saved



def test_ledger_field_merge(tmp: Path) -> None:
    """Per KEY was not enough; the fields inside a key are the same defect one level down."""
    print("\nthe ledger merges fields, not just keys")
    with isolated(tmp):
        # Another process records an outcome for a ticket this caller is holding a stale copy of.
        pool.save_json(pool.LEDGER, {"o/r#1": {"status": "draft", "pr": 5, "attempts": 1}})
        stale = {"o/r#1": {"status": "draft", "pr": 5, "attempts": 1}}
        with pool.ledger_held() as d:
            d["o/r#1"]["outcome"] = {"pr_state": "MERGED", "reviews": 2}

        pool.write_ledger(stale, "o/r#1", {**stale["o/r#1"], "status": "ready", "attempts": 2})
        end = json.loads(pool.LEDGER.read_text())["o/r#1"]
        check("a field this caller never saw survives its write", end.get("outcome") is not None,
              str(end))
        check("and the caller's own fields win", end["status"] == "ready" and end["attempts"] == 2,
              str(end))
        check("the caller's in-memory copy agrees with the file", stale["o/r#1"] == end, str(stale))

        # nothing_ran strips RUN_FIELDS on purpose. A plain {**disk, **entry} resurrects them.
        pool.save_json(pool.LEDGER, {"o/r#2": {"status": "running", "duration_s": 99, "turns": 7,
                                               "attempts": 1}})
        held = {"o/r#2": {"status": "running", "duration_s": 99, "turns": 7, "attempts": 1}}
        pool.write_ledger(held, "o/r#2", pool.nothing_ran(held["o/r#2"], "worktree-blocked", "why"))
        end2 = json.loads(pool.LEDGER.read_text())["o/r#2"]
        check("a field the caller dropped on purpose stays dropped",
              "duration_s" not in end2 and "turns" not in end2, str(end2))


def test_reap_respects_a_newer_status(tmp: Path) -> None:
    """`reap_running`'s decision is a predicate on the STORED status, so it is re-asserted under the
    lock. `--work` writes `status: running` and does not take `pool.lock`, so it can move underneath."""
    print("\nreaping does not overwrite an outcome recorded under it")
    with isolated(tmp):
        pool.save_json(pool.LEDGER, {"o/r#7": {"status": "ready", "pr": 99, "attempts": 2}})
        stale = {"o/r#7": {"status": "running", "attempts": 1, "last_run": "2026-01-01T00:00:00"}}
        reaped = pool.reap_running(stale, pool.Say(tmp / "r.md"))
        end = json.loads(pool.LEDGER.read_text())["o/r#7"]
        check("a ticket another process finished is not reaped", end["status"] == "ready", str(end))
        check("its PR survives", end.get("pr") == 99, str(end))
        check("and it is not reported as closed out", reaped == [], str(reaped))

        # Absent from the file: nothing to conflict with, so it is still closed out.
        pool.save_json(pool.LEDGER, {})
        only = {"o/r#8": {"status": "running", "attempts": 1, "last_run": "2026-01-01T00:00:00"}}
        reaped2 = pool.reap_running(only, pool.Say(tmp / "r2.md"))
        check("a ticket the file has never seen is still closed out",
              len(reaped2) == 1 and only["o/r#8"]["status"] == "error", str(only))


def test_ledger_corruption_is_not_silent(tmp: Path) -> None:
    """An unreadable ledger must not become an empty one — per-key publishing removed the accidental
    whole-file repair, and nothing replaced it."""
    print("\nan unreadable ledger is preserved, not emptied")
    with isolated(tmp):
        pool.save_json(pool.LEDGER, {f"k{i}": {"v": i} for i in range(5)})
        pool.LEDGER.write_text("{ truncated")
        pool.write_ledger({}, "k0", {"v": 0})
        kept = list(pool.LEDGER.parent.glob(f"{pool.LEDGER.name}.corrupt.*"))
        check("the unreadable bytes are kept aside", len(kept) == 1, str(kept))
        check("and they are the bytes that were there",
              kept and kept[0].read_text() == "{ truncated")


def test_ledger_held_is_not_reentrant(tmp: Path) -> None:
    """Nesting deadlocks silently and permanently; `write_ledger` is one line from doing it."""
    print("\nnesting the ledger lock raises instead of hanging")
    with isolated(tmp):
        raised = ""
        try:
            with pool.ledger_held():
                with pool.ledger_held():
                    pass
        except RuntimeError as exc:
            raised = str(exc)
        check("a nested ledger_held raises", "not reentrant" in raised, raised or "(no exception)")
        # and the guard is cleared, so the next writer is not locked out by the failed attempt
        pool.write_ledger({}, "after", {"v": 1})
        check("the lock is usable afterwards", "after" in json.loads(pool.LEDGER.read_text()))


def test_ledger_snapshot_is_deep(tmp: Path) -> None:
    """`merge_ledger_changes` compares against `before`; a shallow copy publishes NOTHING, silently,
    because `refresh_outcomes` mutates its entries in place."""
    print("\nthe before-image is deep")
    with isolated(tmp):
        pool.save_json(pool.LEDGER, {"a": {"v": 1}})
        led = {"a": {"v": 1}}
        before = pool.ledger_snapshot(led)
        led["a"]["v"] = 2                      # in place, exactly as refresh_outcomes does
        check("an in-place change is detected", pool.merge_ledger_changes(before, led) == ["a"])
        check("and it reaches the file", json.loads(pool.LEDGER.read_text())["a"]["v"] == 2)


def test_queue_never_repeats_a_ticket(tmp: Path) -> None:
    """One ticket named twice must not become two workers in one worktree.

    The worktree path is derived from the ticket, so a queue holding `266,297,266` puts two workers
    in the SAME directory — and the second one's `make_worktree` releases and recreates the tree the
    first is working in. `claim_slot` has guarded this since the hand-launched path existed; the
    DRIVER never did, because it calls `make_worktree` directly. Found by accident: a four-worker
    rehearsal typed `--ticket 266,297,310,266` and two sessions were handed the same worktree.
    """
    print("\na ticket named twice")
    jobs = [{"slug": "o/r", "ticket": "266", "key": "o/r#266"},
            {"slug": "o/r", "ticket": "297", "key": "o/r#297"},
            {"slug": "o/r", "ticket": "266", "key": "o/r#266"},
            {"slug": "o/b", "ticket": "266", "key": "o/b#266"}]
    say = pool.Say(tmp / "q.md")
    out = pool.dedupe_queue(jobs, say)
    check("the repeat is dropped", [j["key"] for j in out] == ["o/r#266", "o/r#297", "o/b#266"],
          str([j["key"] for j in out]))
    check("the order the operator gave is kept", out[0]["ticket"] == "266" and out[1]["ticket"] == "297")
    check("the same number in ANOTHER repo is a different ticket and survives",
          any(j["slug"] == "o/b" for j in out), str([j["key"] for j in out]))
    # The guard has to be WIRED, not merely present: an earlier version called it from `main`, where
    # removing the call reddened nothing, because this case drives the function directly.
    src = (HERE / "pool-run").read_text()
    made = src[src.index("def build_queue("):]
    made = made[:made.index("\n# ", 1) if "\n# " in made else len(made)]
    check("every queue build_queue returns has been deduped",
          made.count("return dedupe_queue(") == made.count("return queue") + made.count("return dedupe_queue("),
          "a return path in build_queue skips the dedupe")
    check("and no queue is returned raw", "    return queue\n" not in made, "a raw return survives")

    check("a queue with no repeats is returned unchanged",
          [j["key"] for j in pool.dedupe_queue(jobs[:2], say)] == ["o/r#266", "o/r#297"])


# ────────────────────────────────────────────────────────────────── pause ──


def test_pause_now_suspends_and_resumes(tmp: Path) -> None:
    """An immediate pause must suspend the session, not end the ticket — and resume must re-enter it.

    The whole feature rests on one measured fact: a `claude -p` killed with SIGTERM mid-run resumes
    from `--resume <session-id>` with its transcript intact. Measured 2026-08-30 outside the suite —
    a session killed after 4 of 12 steps resumed and did steps 5-12 only, signing off with a token
    only the ORIGINAL prompt defined, so the transcript was inherited rather than re-derived from the
    files on disk. What this case pins is the driver's half of that: that a pause is told apart from
    a death everywhere the two would otherwise be confused.

    Four things separate the two, and every one of them was a way to lose the work: the attempt is
    NOT spent (a paused ticket that came back needing a human twice would be unresumable), the
    worktree is NOT released (the session resumes into it), no driver-capture record is written (the
    run is not over, and a record counts towards the retro threshold), and the session id is kept
    (without it there is nothing to resume).
    """
    print("\nan immediate pause suspends the session and resume re-enters it")
    origin, work = git_fixture(tmp)
    one = standalone_fixture(tmp / "sa", "sa1", 8081, 3316)
    argv_log = tmp / "argv.txt"
    stub = tmp / "claude-stub"
    stub.write_text(
        "#!/bin/bash\n"
        f'echo "$PWD :: $@" >> {argv_log}\n'
        "echo '{\"type\":\"assistant\",\"message\":{\"content\":[{\"type\":\"text\","
        "\"text\":\"working\"}]}}'\n"
        "sleep 40\n"
        "echo '{\"type\":\"result\",\"result\":\"done\",\"total_cost_usd\":0.01}'\n")
    stub.chmod(0o755)

    cfg = pool.merge(pool.DEFAULTS, {
        "claude": {"binary": str(stub)},
        "parallel": {"max_workers": 1, "standalones": [str(one)]},
        "ticket": {"timeout_seconds": 300, "quiet_seconds": 300},
    })
    say = pool.Say(tmp / "run.md")
    base = pool.remote_head(work)
    job = {"slug": "o/r", "path": work, "ticket": "266", "url": "https://example/266",
           "title": "t", "key": "o/r#266"}
    ledger: dict = {}

    with isolated(tmp) as root:
        slots = pool.build_slots(cfg, 1, tmp / "m2")

        def ask_when_it_starts() -> None:
            deadline = time.time() + 30
            while time.time() < deadline and not argv_log.exists():
                time.sleep(0.1)
            pool.ask_pause(immediate=True)

        threading.Thread(target=ask_when_it_starts, daemon=True).start()
        results = pool.run_wave([job], slots, cfg, ledger, say, {str(work): base})

        check("the ticket reports itself paused, not killed and not errored",
              results == [("o/r#266", "paused")], str(results))
        entry = pool.load_json(pool.LEDGER, {}).get("o/r#266", {})
        check("the ledger says paused", entry.get("status") == "paused", str(entry))
        check("the attempt is NOT spent — a pause is not a try",
              entry.get("attempts", 0) == 0, f"attempts={entry.get('attempts')}")
        sid = entry.get("session_id")
        check("the session id is kept, because it is the only thing that can be resumed",
              bool(sid), str(entry))
        wt = Path(entry.get("worktree", "/nonexistent"))
        check("the worktree is kept — the session resumes into it", wt.is_dir(), str(wt))
        check("no driver-capture record was written: the run is not over",
              not list((root / "skill-lessons").glob("*.md")),
              str([p.name for p in (root / "skill-lessons").glob("*.md")]))
        # NOT consumed here, and that is the point: with several tickets in flight the request is
        # what the OTHER watchdogs are still polling, so a worker that cleared it on its way out
        # would suspend one session and leave its siblings running unpaused — a half-paused pool,
        # reported as paused. The driver's wave loop consumes it once, after the wave.
        check("the request outlives the wave, so every session in flight still sees it",
              pool.pause_requested() is not None,
              "a worker consumed the pause request; its siblings would never see it")
        body = (HERE / "pool-run").read_text()
        worker = body[body.index("def work_ticket("):body.index("def dedupe_queue(")]
        check("no worker consumes it — only the loop that can see the whole wave does",
              "clear_pause_request(" not in worker,
              "work_ticket clears the pause request, so a sibling session can miss the pause")
        pool.clear_pause_request()      # what the driver's wave loop does, once, after the wave

        # Now resume. A sentinel proves the worktree was re-entered rather than recreated: a fresh
        # `make_worktree` would have removed and re-added the directory, taking it with it.
        (wt / "sentinel.txt").write_text("survives\n")
        stub.write_text(
            "#!/bin/bash\n"
            f'echo "$PWD :: $@" >> {argv_log}\n'
            "echo '{\"type\":\"result\",\"result\":\"done\",\"total_cost_usd\":0.01}'\n")
        stub.chmod(0o755)
        resumed = pool.resume_jobs({"queue": [], "suspended": [dict(job, path=str(work))]},
                                   pool.load_json(pool.LEDGER, {}), cfg, say)
        check("resume builds a job carrying the suspended session",
              len(resumed) == 1 and resumed[0].get("resume", {}).get("session_id") == sid,
              str(resumed))
        pool.run_wave(resumed, slots, cfg, ledger, say, {str(work): base})

    lines = [l for l in argv_log.read_text().splitlines() if l.strip()]
    check("the session was started twice in all", len(lines) == 2, str(lines))
    check("the first start opened a NEW session", "--session-id" in lines[0], lines[0])
    check("the second RESUMED that same session rather than starting another",
          f"--resume {sid}" in lines[1] and "--session-id" not in lines[1], lines[1])
    check("and it resumed in the same worktree", lines[1].split(" :: ")[0] == str(wt.resolve()),
          lines[1].split(" :: ")[0] + " != " + str(wt.resolve()))
    check("the worktree was re-entered, not recreated", (wt / "sentinel.txt").exists(),
          "the resume removed and recreated the tree, losing the paused run's work")


def test_pause_plan_round_trip(tmp: Path) -> None:
    """What a paused driver writes down, and what `--resume` reads back.

    The plan is the ONLY place the queue's remaining ORDER survives a pause. Rebuilding it from the
    label would re-sort it ascending, and an operator who typed `--ticket 310,297,266` for a reason
    would get their reason silently discarded halfway through.
    """
    print("\nthe pause plan")
    say = pool.Say(tmp / "plan.md")
    jobs = [{"slug": "o/r", "path": Path("/repo"), "ticket": t, "url": f"u{t}", "title": "t",
             "key": f"o/r#{t}"} for t in ("310", "297", "266")]
    with isolated(tmp):
        pool.write_pause_plan("/cfg.json", workers=2, no_retro=True,
                              remaining=jobs[1:], suspended=jobs[:1], say=say)
        plan = pool.read_pause_plan()
        check("the plan names the config the paused run was using",
              plan.get("config") == "/cfg.json", str(plan))
        check("and the width it was running at", plan.get("workers") == 2, str(plan))
        check("and whether the retro was off", plan.get("no_retro") is True, str(plan))
        check("the remaining queue keeps the order it was paused in",
              [j["key"] for j in plan["queue"]] == ["o/r#297", "o/r#266"], str(plan["queue"]))
        check("a Path survives the round trip as a path",
              plan["queue"][0]["path"] == "/repo", str(plan["queue"][0]))

        # A suspended ticket the ledger no longer calls paused must NOT be resumed as one: its
        # session is gone, and re-entering a session id that no longer exists is a run that starts
        # over in a worktree holding someone else's half-finished work.
        ledger = {"o/r#310": {"status": "paused", "session_id": "sid-310", "worktree": "/wt/310"}}
        built = pool.resume_jobs(plan, ledger, {"repos": {"o/r": "/repo"}}, say)
        check("the suspended ticket is worked FIRST, before the rest of the queue",
              [j["key"] for j in built] == ["o/r#310", "o/r#297", "o/r#266"],
              str([j["key"] for j in built]))
        check("it carries the session and worktree to re-enter",
              built[0]["resume"] == {"session_id": "sid-310", "worktree": "/wt/310"},
              str(built[0].get("resume")))
        check("the tickets that never started carry no session to resume",
              all("resume" not in j for j in built[1:]), str(built[1:]))
        check("a job rebuilt from the plan carries a real Path again",
              isinstance(built[1]["path"], Path), type(built[1]["path"]).__name__)

        moved_on = pool.resume_jobs(plan, {"o/r#310": {"status": "ready", "pr": 9}},
                                    {"repos": {"o/r": "/repo"}}, say)
        check("a ticket that is no longer paused is not re-entered",
              [j["key"] for j in moved_on] == ["o/r#297", "o/r#266"],
              str([j["key"] for j in moved_on]))


def test_the_ledger_is_the_durable_half_of_a_pause(tmp: Path) -> None:
    """A paused ticket the plan does not name must still be resumable.

    The two records are written at different moments: `work_ticket` marks the ticket paused as its
    session ends, and the wave loop writes the plan afterwards, once the whole wave is back. A driver
    that dies in that gap leaves a ticket that a plain run skips (it is paused) and a plan-only
    resume cannot see (it is not in the plan) — stuck, with no command that reaches it.
    """
    print("\na pause the plan never recorded")
    say = pool.Say(tmp / "fallback.md")
    cfg = {"repos": {"o/r": "/repo"}}
    ledger = {"o/r#266": {"status": "paused", "session_id": "sid", "worktree": "/wt",
                          "url": "https://example/266"}}
    built = pool.resume_jobs({"queue": [], "suspended": []}, ledger, cfg, say)
    check("the ledger's own paused ticket is resumed even with no plan naming it",
          [j["key"] for j in built] == ["o/r#266"], str(built))
    check("and it still carries the session to re-enter",
          built and built[0].get("resume", {}).get("session_id") == "sid", str(built))

    # The plan is the authority on ORDER, so a ticket it names must not be added twice by the
    # fallback — a duplicate would put two workers in one worktree, which is what `dedupe_queue`
    # exists to stop one layer down.
    plan = {"queue": [], "suspended": [{"slug": "o/r", "path": "/repo", "ticket": "266",
                                        "url": "u", "title": "t", "key": "o/r#266"}]}
    once = pool.resume_jobs(plan, ledger, cfg, say)
    check("a ticket the plan already names is not added a second time",
          [j["key"] for j in once] == ["o/r#266"], str([j["key"] for j in once]))

    other = pool.resume_jobs({"queue": [], "suspended": []},
                             {"other/repo#9": {"status": "paused", "session_id": "s",
                                               "worktree": "/wt"}}, cfg, say)
    check("a pause belonging to a repo this config does not carry is left alone",
          other == [], str(other))
    check("and said, rather than silently dropped",
          "not in this config" in (tmp / "fallback.md").read_text())


def test_a_pause_whose_worktree_is_gone_is_not_stranded(tmp: Path) -> None:
    """A resume with nowhere to resume INTO must work the ticket, not fail forever.

    `claude --resume` needs the working tree the conversation is about, so a paused ticket whose
    worktree has been removed can never be re-entered by anything. Leaving it paused is the one
    outcome that has no way out: a plain run skips a paused ticket, and every later `--resume` walks
    back into the same dead end. So the resume is dropped and the ticket is worked from the start —
    the context is lost either way, and the branch is not, because removing a worktree does not
    delete a ref.
    """
    print("\na paused ticket whose worktree has been removed")
    origin, work = git_fixture(tmp)
    one = standalone_fixture(tmp / "sa", "sa1", 8081, 3316)
    argv_log = tmp / "argv.txt"
    stub = tmp / "claude-stub"
    stub.write_text("#!/bin/bash\n"
                    f'echo "$PWD :: $@" >> {argv_log}\n'
                    "echo '{\"type\":\"result\",\"result\":\"done\"}'\n")
    stub.chmod(0o755)
    cfg = pool.merge(pool.DEFAULTS, {"claude": {"binary": str(stub)},
                                     "parallel": {"max_workers": 1, "standalones": [str(one)]},
                                     "ticket": {"timeout_seconds": 120, "quiet_seconds": 120}})
    say = pool.Say(tmp / "gone.md")
    base = pool.remote_head(work)
    job = {"slug": "o/r", "path": work, "ticket": "266", "url": "https://example/266",
           "title": "t", "key": "o/r#266",
           "resume": {"session_id": "sid-gone", "worktree": str(tmp / "never-existed")}}
    ledger: dict = {}
    with isolated(tmp):
        slots = pool.build_slots(cfg, 1, tmp / "m2")
        out = pool.run_wave([job], slots, cfg, ledger, say, {str(work): base})
    check("the ticket is worked rather than left stranded as paused",
          out and out[0][1] != "worktree-blocked", str(out))
    entry = pool.load_json(pool.LEDGER, {}).get("o/r#266", {}) if False else ledger.get("o/r#266", {})
    check("and it does not stay paused, which nothing could ever pick up",
          entry.get("status") != "paused", str(entry))
    lines = [l for l in argv_log.read_text().splitlines() if l.strip()] if argv_log.exists() else []
    check("it started a NEW session rather than trying to re-enter a lost one",
          len(lines) == 1 and "--session-id" in lines[0] and "--resume" not in lines[0], str(lines))
    check("and said why, naming the session it could not re-enter",
          "cannot be re-entered" in (tmp / "gone.md").read_text()
          and "sid-gone" in (tmp / "gone.md").read_text(),
          (tmp / "gone.md").read_text()[-200:])


def test_the_retro_is_not_suspended_by_a_pause(tmp: Path) -> None:
    """An immediate pause must reach a TICKET's session and not the retro's.

    A pause suspends a session only where something can resume it, and what carries a suspended
    session back is its ledger row — its session id and its worktree. The retro has no row. Suspended
    it would simply END, losing the work, leaving the source repo mid-checkout for the next retro to
    refuse as dirty, and reporting itself through `run_retro`'s problem list as "no commit landed",
    which is true and is not the reason.

    The window is real rather than theoretical: the loop already refuses to START a retro while a
    pause is outstanding, so what is left is a pause asked for while one is already running — and a
    retro is allowed two hours.
    """
    print("\nan immediate pause against the retro's own session")
    stub = tmp / "claude-stub"
    stub.write_text("#!/bin/bash\n"
                    "echo '{\"type\":\"assistant\",\"message\":{\"content\":[{\"type\":\"text\","
                    "\"text\":\"working\"}]}}'\n"
                    "sleep 40\n"
                    "echo '{\"type\":\"result\",\"result\":\"done\"}'\n")
    stub.chmod(0o755)
    cfg = pool.merge(pool.DEFAULTS, {"claude": {"binary": str(stub)}})
    say = pool.Say(tmp / "retro.md")

    with isolated(tmp):
        pool.ask_pause(immediate=True)
        ticket = pool.Session("t", tmp, cfg, tmp / "ticket", 300, 300, say).run()
        check("a ticket's session IS suspended by the pause",
              bool(ticket.get("paused_for")) and not ticket.get("killed_for"), str(ticket))
        retro = pool.Session("/skill-retro", tmp, cfg, tmp / "retro", 300, 300, say,
                             pausable=False).run()
        check("the retro's session is NOT — it runs to its own end",
              not retro.get("paused_for") and not retro.get("killed_for"), str(retro))
        pool.clear_pause_request()

    # Wired, not merely available: the flag defaults to pausable, so a `run_retro` that forgot to
    # pass it would suspend the retro exactly as before and nothing above would notice.
    src = (HERE / "pool-run").read_text()
    body = src[src.index("def run_retro("):src.index("def retro_forecast(")]
    check("run_retro asks for a session that cannot be suspended",
          "pausable=False" in body, "run_retro starts a pausable session")


def test_a_held_back_suspended_ticket_stays_suspended(tmp: Path) -> None:
    """`--resume --once` must not demote the tickets it did not reach.

    `resume_jobs` puts suspended tickets FIRST, so a limit smaller than their number holds one back.
    The plan is consumed as it is read, so whatever is held back has to be written straight back —
    and if a suspended one were written into the un-started queue, the next resume would work it from
    scratch, abandoning the very session the pause was taken to keep. It is a silent loss: a fresh
    session in a worktree that already holds work looks exactly like a run that got a long way.
    """
    print("\na limited resume holding back a suspended ticket")
    say = pool.Say(tmp / "held.md")
    jobs = [{"slug": "o/r", "path": "/repo", "ticket": t, "url": f"u{t}", "title": "t",
             "key": f"o/r#{t}"} for t in ("266", "297", "310")]
    with_session = dict(jobs[1], resume={"session_id": "sid-297", "worktree": "/wt/297"})
    with isolated(tmp):
        # what the driver does with the tail it will not reach: split by WHAT they are
        held = [with_session, jobs[2]]
        pool.write_pause_plan("/cfg.json", 1, False,
                              [j for j in held if not j.get("resume")],
                              [j for j in held if j.get("resume")], say, forced=True)
        plan = pool.read_pause_plan()
        check("the held-back suspended ticket is written back as SUSPENDED",
              [j["key"] for j in plan["suspended"]] == ["o/r#297"], str(plan["suspended"]))
        check("and the never-started one as an un-started ticket",
              [j["key"] for j in plan["queue"]] == ["o/r#310"], str(plan["queue"]))
        check("the plan remembers the queue was forced, so the resume screens it the same way",
              plan.get("forced") is True, str(plan))

        ledger = {"o/r#297": {"status": "paused", "session_id": "sid-297", "worktree": "/wt/297"}}
        built = pool.resume_jobs(plan, ledger, {"repos": {"o/r": "/repo"}}, say)
        check("so the next resume re-enters it rather than starting it over",
              built[0]["key"] == "o/r#297"
              and built[0].get("resume", {}).get("session_id") == "sid-297", str(built[0]))


def test_a_paused_ticket_is_not_restarted(tmp: Path) -> None:
    """A plain run must leave a paused ticket alone.

    Working it would start a SECOND session on the same branch while the first is suspended with
    hours of context in it — and nothing would report the loss, because a fresh session in a
    worktree that already holds work looks exactly like a run that got a long way.
    """
    print("\na paused ticket against a plain run")
    say = pool.Say(tmp / "consider.md")
    cfg = pool.merge(pool.DEFAULTS, {})
    issue = {"number": 266, "title": "t", "url": "https://example/266"}
    ledger = {"o/r#266": {"status": "paused", "session_id": "sid", "worktree": "/wt"}}
    check("the label path skips it",
          pool.consider("o/r", "/repo", issue, [], ledger, say, cfg, forced=False) is None)
    check("and says so, naming the one command that continues it",
          "--resume" in (tmp / "consider.md").read_text(), (tmp / "consider.md").read_text()[-200:])
    check("a ticket with no paused entry is unaffected",
          pool.consider("o/r", "/repo", issue, [], {}, say, cfg, forced=False) is not None)

    # Naming it explicitly forces past the skip, which is the escape hatch — but it abandons a
    # session holding hours of context, so it may not do that quietly.
    loud = pool.Say(tmp / "forced.md")
    got = pool.consider("o/r", "/repo", issue, [], ledger, loud, cfg, forced=True)
    check("naming it explicitly still works it, as the escape hatch", got is not None)
    said = (tmp / "forced.md").read_text() if (tmp / "forced.md").exists() else ""
    check("but says it is abandoning the suspended session, and names it",
          "abandons the suspended session" in said and "sid" in said, said[-200:] or "NOTHING SAID")


def test_a_session_reports_what_ended_it(tmp: Path) -> None:
    """What ENDED a session must reach the driver as evidence, not be inferred from the wreckage.

    The outcome status is read off what was LEFT BEHIND — a PR exists and is draft — and that cannot
    tell a loop which genuinely ran out of rounds from one whose session was killed before its
    closing steps ran. Measured on chartsearchai#349: the review loop HAD converged (gate state:
    reviewed, 0 blocking) and an API refusal ended the session before it could mark the PR ready, so
    the driver reported "the loop did not converge" — a false diagnosis, and one an operator acts on.
    """
    with isolated(tmp):
        stub = tmp / "claude"
        stub.write_text(
            "#!/bin/bash\n"
            "echo '{\"type\":\"assistant\",\"message\":{\"content\":"
            "[{\"type\":\"text\",\"text\":\"hi\"}]}}'\n"
            "echo '{\"type\":\"result\",\"subtype\":\"success\",\"is_error\":true,"
            "\"stop_reason\":\"refusal\",\"result\":\"API Error: safeguards flagged this "
            "message\",\"total_cost_usd\":0.02}'\n")
        stub.chmod(0o755)
        cfg = pool.merge(pool.DEFAULTS, {"claude": {"binary": str(stub)}})
        run = pool.Session("p", tmp, cfg, tmp / "s", 300, 300, lambda *a, **k: None).run()
        check("a session carries the stop_reason that ended it",
              run.get("stop_reason") == "refusal", repr(run.get("stop_reason")))
        check("a refusal is still reported as an error",
              run.get("is_error") is True, repr(run.get("is_error")))
        check("and the session's own closing prose is kept beside it, not instead of it",
              "safeguards" in (run.get("summary") or ""), repr(run.get("summary"))[:120])


def main() -> int:
    with tempfile.TemporaryDirectory() as td:
        tmp = Path(td)
        for name, fn in [("ticket-identity", test_ticket_identity),
                         ("legacy-ticket-state", test_legacy_ticket_state),
                         ("worktrees", test_worktrees), ("slots", test_slots),
                         ("gate-state", test_gate_state_locking), ("waves", test_waves),
                         ("parallel run", test_parallel_run), ("say", test_say_is_thread_safe),
                         ("records", test_record_attribution), ("crash", test_crash_does_not_clobber),
                         ("nothing ran", test_nothing_ran), ("maven tail", test_shared_maven_repo), ("db ports", test_db_port_hosts),
                         ("skill commands", test_skills_commands_run),
                         ("driver gate-state", test_pool_gate_state_via_helper),
                         ("save_json temp", test_save_json_temp_is_private),
                         ("ledger cross-process", test_ledger_cross_process),
                         ("ledger field merge", test_ledger_field_merge),
                         ("reap vs newer status", test_reap_respects_a_newer_status),
                         ("ledger corruption", test_ledger_corruption_is_not_silent),
                         ("ledger reentrancy", test_ledger_held_is_not_reentrant),
                         ("ledger snapshot", test_ledger_snapshot_is_deep),
                         ("claims", test_claim_and_release),
                         ("needs a tty", test_work_needs_a_terminal),
                         ("claim cli", test_claim_cli),
                         ("one command", test_work_one_command),
                         ("ctrl-c", test_ctrl_c_reaches_the_session),
                         ("collisions", test_double_claim_and_live_driver),
                         ("two repos", test_same_ticket_number_in_two_repos),
                         ("platform floor", test_platform_floor),
                         ("work ledger", test_work_reaches_the_ledger),
                         ("dead lease", test_dead_owner_lease_is_reclaimable),
                         ("watch live", test_pool_watch_live),
                         ("dupe queue", test_queue_never_repeats_a_ticket),
                         ("pause now", test_pause_now_suspends_and_resumes),
                         ("pause plan", test_pause_plan_round_trip),
                         ("pause ledger fallback", test_the_ledger_is_the_durable_half_of_a_pause),
                         ("retro not pausable", test_the_retro_is_not_suspended_by_a_pause),
                         ("lost worktree", test_a_pause_whose_worktree_is_gone_is_not_stranded),
                         ("held back", test_a_held_back_suspended_ticket_stays_suspended),
                         ("paused vs plain run", test_a_paused_ticket_is_not_restarted),
                         ("ended by", test_a_session_reports_what_ended_it)]:
            sub = tmp / name.replace(" ", "-")
            sub.mkdir()
            try:
                fn(sub)
            except Exception as exc:  # a case that cannot even run is a failure, not a crash
                FAIL.append(name)
                print(f"  FAIL {name} raised {type(exc).__name__}: {exc}")
    print(f"\npassed={len(PASS)} failed={len(FAIL)}")
    for f in FAIL:
        print(f"  failed: {f}")
    return 1 if FAIL else 0


if __name__ == "__main__":
    sys.exit(main())
