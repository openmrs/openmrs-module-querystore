"""What os.killpg raises on macOS for a process group whose only member is an unreaped zombie, and after the reap.
blind-run's stop() suppressed only ProcessLookupError until 2026-11-25."""
import os, signal, subprocess, sys, time


def attempt(label, fn):
    try:
        fn()
        print(f"{label}: no error")
    except Exception as e:
        print(f"{label}: {type(e).__name__} {e}")


p = subprocess.Popen([sys.executable, "-c", "import time; time.sleep(30)"], start_new_session=True)
time.sleep(0.5)
os.killpg(p.pid, signal.SIGTERM)
time.sleep(0.5)
attempt("zombie leader, unreaped", lambda: os.killpg(p.pid, signal.SIGKILL))
p.wait()
attempt("leader reaped, group empty", lambda: os.killpg(p.pid, signal.SIGKILL))
