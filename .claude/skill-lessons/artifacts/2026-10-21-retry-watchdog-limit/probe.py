"""probe.py <poolrun copy> <stub.py copy> <reset-ahead-s> <case-dir> <port>

Calls pool-run's real `account_limit(cfg)` -- the probe `watch_hand_launched` runs for a `--work`
session -- against stub.py, with whatever environment this process was started with, which is what
`pool-run --work` hands the probe (`Session(...)` with no env= copies os.environ). Run it under
`env -i` with HOME at a scratch directory; put the two watchdog variables in that env for the case
"the two variables in front of pool-run --work".
"""
import glob
import json
import os
import socket
import subprocess
import sys
import time
from importlib.machinery import SourceFileLoader

poolrun, stubpy, ahead, case, port = (sys.argv[1], sys.argv[2], float(sys.argv[3]), sys.argv[4],
                                      int(sys.argv[5]))
os.makedirs(case, exist_ok=True)
home = os.environ["HOME"]
os.makedirs(os.path.join(home, ".claude", "pipeline", "logs"), exist_ok=True)
sys.dont_write_bytecode = True
pr = SourceFileLoader("poolrun_probe", poolrun).load_module()
assert str(pr.HOME) == home, (pr.HOME, home)

start = time.time()
stub = subprocess.Popen([sys.executable, "-I", stubpy, str(port), str(start + ahead),
                         os.path.join(case, "requests.jsonl")])
for _ in range(100):
    try:
        socket.create_connection(("127.0.0.1", port), 0.2).close()
        break
    except OSError:
        time.sleep(0.1)
else:
    sys.exit("stub never listened")
os.environ["ANTHROPIC_BASE_URL"] = f"http://127.0.0.1:{port}"
cfg = {"claude": {"model": None, "effort": None, "max_budget_usd": None, "extra_args": [],
                  "binary": "claude"}}
t0 = time.time()
answer = pr.account_limit(cfg)
took = time.time() - t0
stub.kill()
stub.wait()
print(json.dumps({"case": os.path.basename(case), "reset_ahead_s": ahead,
                  "watchdog_in_launcher_env": os.environ.get("CLAUDE_CODE_RETRY_WATCHDOG", "unset"),
                  "answer": answer, "seconds": round(took, 1)}))
for path in sorted(glob.glob(os.path.join(home, ".claude", "pipeline", "logs", "*limit-probe.jsonl"))):
    kinds = []
    for line in open(path):
        try:
            ev = json.loads(line)
        except ValueError:
            continue
        k = f"{ev.get('type')}/{ev.get('subtype') or ''}"
        if ev.get("type") == "result":
            k += f" is_error={ev.get('is_error')}"
        kinds.append(k)
    print("   probe stream:", ", ".join(kinds) or "(empty)")
    err = open(path[:-6] + ".err").read().strip()
    print("   probe stderr:", err[:300] or "(empty)")
for line in open(os.path.join(case, "requests.jsonl")):
    r = json.loads(line)
    print(f"   stub: +{r['t'] - start:6.1f}s {r['status']}")
