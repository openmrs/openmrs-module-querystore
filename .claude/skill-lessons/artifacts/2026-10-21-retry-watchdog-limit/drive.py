"""drive.py <label> <reset-ahead-s> <quiet-s> <timeout-s> <on|off> <path to pool-run>

Runs pool-run's real `Session`, the driver's headless session, against stub.py, to see what the
driver makes of a session whose request waits at a usage limit under the retry watchdog. Writes
<label>/ beside this script: result.json (the run's verdict, launch and reset as epoch seconds, the
quiet window and timeout), session.jsonl and session.err (the Session's own stream and stderr) and
requests.jsonl (the stub's log). pool-run is loaded read-only, with HOME at a throwaway directory.
"""
import json
import os
import shutil
import socket
import subprocess
import sys
import tempfile
import time
from importlib.machinery import SourceFileLoader
from pathlib import Path

here = Path(__file__).resolve().parent
label, ahead, quiet, timeout, watchdog, poolrun = (sys.argv[1], float(sys.argv[2]), int(sys.argv[3]),
                                                   int(sys.argv[4]), sys.argv[5] == "on", sys.argv[6])
if not label or "/" in label or label.startswith("."):
    sys.exit("label must be a plain name")
out = here / label
shutil.rmtree(out, ignore_errors=True)
out.mkdir()
home = Path(tempfile.mkdtemp())
os.environ["HOME"] = str(home)  # before the import, so pool-run's HOME-derived paths land here
sys.dont_write_bytecode = True
pr = SourceFileLoader("poolrun_driven", poolrun).load_module()

port = 20000 + (os.getpid() % 20000)
start = time.time()
stub = subprocess.Popen([sys.executable, "-I", str(here / "stub.py"), str(port), str(start + ahead),
                         str(out / "requests.jsonl")])
for _ in range(100):
    try:
        socket.create_connection(("127.0.0.1", port), 0.2).close()
        break
    except OSError:
        time.sleep(0.1)
env = {"PATH": os.environ["PATH"], "HOME": str(home), "TERM": "dumb", "LANG": "C.UTF-8",
       "ANTHROPIC_API_KEY": "stub-not-a-key", "ANTHROPIC_BASE_URL": f"http://127.0.0.1:{port}",
       "DISABLE_TELEMETRY": "1", "CLAUDE_CODE_DISABLE_NONESSENTIAL_TRAFFIC": "1",
       "DISABLE_AUTOUPDATER": "1", "IS_SANDBOX": "1"}
if watchdog:
    env.update({"CLAUDE_CODE_RETRY_WATCHDOG": "1",
                "CLAUDE_CODE_RETRY_WATCHDOG_MAX_USAGE_LIMIT_WAIT_MS": "18000000"})
cfg = {"claude": {"model": None, "effort": None, "max_budget_usd": None, "extra_args": [],
                  "binary": "claude"}}
session = pr.Session("Reply with one word.", home, cfg, out / "session", timeout, quiet,
                     lambda *a, **k: None, env=env)
run = session.run()
stub.kill()
stub.wait()
shutil.rmtree(home, ignore_errors=True)
keep = {k: run.get(k) for k in ("exit", "seconds", "killed_for", "paused_for", "rate_limit")}
keep.update({"start": start, "reset": start + ahead, "quiet_s": quiet, "timeout_s": timeout,
             "watchdog": watchdog})
(out / "result.json").write_text(json.dumps(keep, indent=2) + "\n")
print(f"== {label}: watchdog={'on' if watchdog else 'off'} reset=+{ahead:.0f}s quiet={quiet}s "
      f"timeout={timeout}s -> " + json.dumps({k: run.get(k) for k in
                                              ("exit", "seconds", "killed_for", "paused_for", "rate_limit")}))
for line in open(out / "session.jsonl"):
    ev = json.loads(line)
    if ev.get("type") == "system" and ev.get("subtype") == "init":
        continue
    print("   stream:", ev.get("type"), ev.get("subtype") or "", ev.get("retry_delay_ms") or "")
for line in open(out / "requests.jsonl"):
    r = json.loads(line)
    print(f"   stub: +{r['t'] - start:6.1f}s {r['status']}")
