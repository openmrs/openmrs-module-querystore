"""Drive pool-run's real watch_hand_launched (scratch copy) through pool-test's own fixtures.

usage: SCRATCH=<dir> drive_carry.py <scratch pool-test.py> <case>   case: control-no-limit | control-limit | resumed-by-itself
A --work session's transcript is stale (quiet), the account refuses probes `refusals` times, then serves.
In `resumed-by-itself` the session's subagent transcript is written every 0.5 s from the first refusal on --
more than a background agent waiting under CLAUDE_CODE_RETRY_WATCHDOG writes (a reviewer saw none written
during a stub wait), so the case shows the carry nudging even a session whose agent is visibly at work.
Prints how many nudges the session's socket received.
"""
import os, sys, tempfile, threading, time, signal
from importlib.machinery import SourceFileLoader
from pathlib import Path

T = SourceFileLoader("pooltest", sys.argv[1]).load_module()
pool = T.pool
case = sys.argv[2]
refusals = 0 if case == "control-no-limit" else 2
tmp = Path(tempfile.mkdtemp(dir=os.environ["SCRATCH"]))
with T.isolated(tmp) as root:
    pid, _sock, received = T.live_session_fixture(tmp, root, name=case[:8])
    wt = tmp / "wt"; wt.mkdir()
    started = pool.now() - 1000
    proj = pool.PROJECTS / pool.project_dir_name(wt); proj.mkdir(parents=True, exist_ok=True)
    main = proj / "s.jsonl"; main.write_text("{}\n")
    sub = proj / "s" / "subagents" / "agent-x.jsonl"; sub.parent.mkdir(parents=True); sub.write_text("{}\n")
    for f in (main, sub):
        os.utime(f, (pool.now() - 400, pool.now() - 400))
    probes = tmp / "probes.txt"
    stub = T.refusing_then_serving_stub(tmp / "claude-stub", probes, refusals, "true")
    cfg = pool.merge(pool.DEFAULTS, {"claude": {"binary": str(stub)},
                                     "ticket": {"timeout_seconds": 4, "quiet_seconds": 4,
                                                "limit_wait_jitter_seconds": 0, "limit_probe_seconds": 3}})
    stop = threading.Event()
    said = []
    threading.Thread(target=pool.watch_hand_launched, daemon=True,
                     args=(cfg, "o/r#1", wt, started, stop, lambda *a, **k: said.append(a[0] if a else ""), pid)).start()
    t0 = time.time(); writes = 0; first_write = None
    while time.time() < t0 + 26 and not received:
        n = int(probes.read_text().strip() or 0) if probes.exists() else 0
        if case == "resumed-by-itself" and n >= 1:
            os.utime(sub, None); writes += 1
            first_write = first_write or round(time.time() - t0, 1)
        time.sleep(0.5)
    time.sleep(1)
    stop.set()
    n = int(probes.read_text().strip() or 0) if probes.exists() else 0
    print(f"{case}: probes={n} subagent_writes={writes} (first at +{first_write}s) "
          f"silence_at_end={pool.silence_since(wt, started)}s nudges={len(received)} at +{round(time.time()-t0,1)}s")
    for s in said:
        print("   said:", str(s)[:150])
    for r in received:
        print("   nudge:", str(r)[:150])
    os.kill(pid, signal.SIGTERM)
