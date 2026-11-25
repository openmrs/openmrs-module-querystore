"""Apply one named mutation to the proposed blind-run, refuse if the anchor does not occur exactly once, run its
self-test, and print the checks that failed."""
import os, subprocess, sys
src = open(sys.argv[1]).read()
M = {
    "summary-load-unguarded": ("    with contextlib.suppress(OSError), open(load_path) as f:", "    with open(load_path) as f:"),
    "write-unguarded":        ("    with contextlib.suppress(OSError):  # a full disk must not end the loop before the deadline's stop()\n        f.write(", "    if True:\n        f.write("),
    "old-file-name":          ("dirs[\"log\"], \"blind-run-summary.txt\"), \"w\")", "dirs[\"log\"], \"summary.txt\"), \"w\")"),
    "wait-past-deadline":     ("rc = proc.wait(timeout=max(0, min(LOAD_EVERY, left)))", "rc = proc.wait(timeout=LOAD_EVERY)"),
    "loadavg-unguarded":      ("    try:\n        one, five, fifteen = os.getloadavg()\n    except OSError:\n        return\n", "    one, five, fifteen = os.getloadavg()\n"),
    "stop-old-suppression":   ("    with contextlib.suppress(ProcessLookupError, PermissionError):\n        os.killpg(proc.pid, signal.SIGTERM)\n", "    with contextlib.suppress(ProcessLookupError):\n        os.killpg(proc.pid, signal.SIGTERM)\n"),
    "kill-a-period-early":    ("                if left > LOAD_EVERY:\n", "                if left > 2 * LOAD_EVERY:\n"),
    "kill-at-first-sample":   ("                if left > LOAD_EVERY:\n                    continue\n", ""),
    "never-kill":             ("                if left > LOAD_EVERY:\n                    continue\n", "                continue\n"),
    "turns=last-result":      ("turns += d.get(\"num_turns\") or 0", "turns = d.get(\"num_turns\") or 0"),
    "minutes=duration_ms":    ("{wall / 60:.1f} min, {load}; \"\n            f\"report in", "{((result or {}).get('duration_ms') or 0) / 60000:.1f} min, {load}; \"\n            f\"report in"),
    "no-tail":                ("return (texts[-1] if texts else \"\"), result, turns + tail", "return (texts[-1] if texts else \"\"), result, turns"),
    "sample-once":            ("        while rc is None:\n            sample_load(lf)\n", "        sample_load(lf)\n        while rc is None:\n"),
    "cost=first-result":      ("            if d.get(\"type\") == \"result\":\n                result = d\n", "            if d.get(\"type\") == \"result\":\n                result = result or d\n"),
    "no-summary-file":        ("    with open(os.path.join(dirs[\"log\"], \"blind-run-summary.txt\"), \"w\") as f:\n        f.write(line + \"\\n\")\n", ""),
}
name = sys.argv[2]
old, new = M[name]
n = src.count(old)
if n != 1:
    sys.exit(f"{name}: anchor occurs {n} times")
path = os.path.join(os.path.dirname(sys.argv[1]), "mut-" + name)
open(path, "w").write(src.replace(old, new))
p = subprocess.run([sys.executable, path, "--self-test"], capture_output=True, text=True, timeout=500)
fails = [l for l in p.stdout.splitlines() if l.startswith("FAIL")]
tail = [l for l in p.stdout.splitlines() if "checks pass" in l]
err = p.stderr.strip().splitlines()[-1:] if p.returncode and not tail else []
print(f"{name}: rc={p.returncode} {tail} {fails} {err}")
