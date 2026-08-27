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
    names = ["LEDGER", "LOGS", "LESSONS", "LAST", "PR_STATE", "HARDEN_STATE", "UNATTENDED_DIR",
             "WORKTREES", "SLOT_M2", "SLOTS", "LOCK"]
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
    with isolated(tmp):
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


def main() -> int:
    with tempfile.TemporaryDirectory() as td:
        tmp = Path(td)
        for name, fn in [("worktrees", test_worktrees), ("slots", test_slots),
                         ("gate-state", test_gate_state_locking), ("waves", test_waves),
                         ("parallel run", test_parallel_run), ("say", test_say_is_thread_safe),
                         ("records", test_record_attribution), ("crash", test_crash_does_not_clobber),
                         ("nothing ran", test_nothing_ran), ("maven tail", test_shared_maven_repo), ("db ports", test_db_port_hosts),
                         ("skill commands", test_skills_commands_run),
                         ("driver gate-state", test_pool_gate_state_via_helper),
                         ("save_json temp", test_save_json_temp_is_private),
                         ("claims", test_claim_and_release),
                         ("needs a tty", test_work_needs_a_terminal),
                         ("claim cli", test_claim_cli),
                         ("one command", test_work_one_command),
                         ("ctrl-c", test_ctrl_c_reaches_the_session),
                         ("collisions", test_double_claim_and_live_driver),
                         ("two repos", test_same_ticket_number_in_two_repos),
                         ("platform floor", test_platform_floor),
                         ("work ledger", test_work_reaches_the_ledger),
                         ("dead lease", test_dead_owner_lease_is_reclaimable)]:
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
