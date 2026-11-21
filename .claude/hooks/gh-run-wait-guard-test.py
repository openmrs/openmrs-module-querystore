#!/usr/bin/env python3
"""Cases for gh-run-wait-guard.py, driven through the hook itself (stdin JSON in, decision out).

Usage: python3 gh-run-wait-guard-test.py [path/to/gh-run-wait-guard.py]   (default: beside this file)

The DENY cases are the shapes of the waits that never ended on 2026-11-05, 2026-11-17 and 2026-11-20 (a run id looked up
with `gh run list`, then polled by an unbounded loop with no empty check). The ALLOW cases are the guarded, bounded and
one-shot forms found among the 40 run-waiting commands of 2026-09-28 to 2026-11-20, which must keep working.
"""
import json
import os
import subprocess
import sys

HOOK = sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(os.path.abspath(__file__)), "gh-run-wait-guard.py")

FIND = "for i in $(seq 1 30); do id=$(gh run list --workflow build-docker.yml --limit 5 --json databaseId,headSha --jq '.[] | select(.headSha|startswith(\"3bfb1be67\")) | .databaseId' | head -1); [ -n \"$id\" ] && break; sleep 10; done; "
LOOKUP = "id=$(gh run list -R $R -w deploy.yml -L 1 --json databaseId -q '.[0].databaseId'); "
WAIT = 'until [ "$(gh run view $id --json status --jq .status)" = completed ]; do sleep 30; done'

DENY = [
    ("the 2026-11-20 incident: a bounded find, then an unbounded wait", FIND + "echo run=$id; " + WAIT),
    ("the 2026-11-05 shape: the status captured in the condition", LOOKUP + "until s=$(gh run view $id -R $R --json status,conclusion -q '.status'); [[ $s == completed* ]]; do sleep 30; done"),
    ("stderr swallowed in the probe", "R=$(gh run list --workflow deploy.yml --limit 1 --json databaseId --jq '.[0].databaseId'); until [ \"$(gh run view $R --json status --jq .status 2>/dev/null)\" = completed ]; do sleep 30; done"),
    ("the variable in braces", LOOKUP + 'until [ "$(gh run view ${id} --json status --jq .status)" = completed ]; do sleep 30; done'),
    ("a while loop", LOOKUP + 'while [ "$(gh run view $id --json status --jq .status)" != completed ]; do sleep 30; done'),
    ("an && that does not guard the loop", LOOKUP + '[ -n "$id" ] && echo found; ' + WAIT),
    ("gh run watch in a loop", LOOKUP + "until gh run watch $id --exit-status; do sleep 30; done"),
    ("a find loop that retries, then falls through with no id", FIND.replace('[ -n \"$id\" ] && break; sleep 10;', '[ -n \"$id\" ] || { sleep 10; continue; }; break;') + WAIT),
    ("a quoted lookup", 'id="$(gh run list -R $R -w deploy.yml -L 1 --json databaseId -q \'.[0].databaseId\')"; ' + WAIT),
    ("a timeout on the probe, not the loop", LOOKUP + 'until [ "$(timeout 20 gh run view $id --json status --jq .status)" = completed ]; do sleep 30; done'),
    ("an empty check that only sleeps", LOOKUP + '[ -n "$id" ] || sleep 5; ' + WAIT),
    ("a lookup with an environment prefix", 'id=$(GH_REPO=o/r gh run list -w deploy.yml -L 1 --json databaseId -q ".[0].databaseId"); ' + WAIT),
    ("a probe's own -gt is no deadline", LOOKUP + 'until [ "$(gh run view $id --json jobs --jq \'.jobs|length\')" -gt 0 ]; do sleep 30; done'),
    ("printing elapsed time is no deadline", LOOKUP + 'until [ "$(gh run view $id --json status --jq .status)" = completed ]; do echo "$SECONDS s"; sleep 30; done'),
    ("a probe's result in a variable is no counter", LOOKUP + 'until n=$(gh run view $id --json jobs --jq ".jobs|length"); [ "$n" -gt 0 ]; do sleep 30; done'),
    ("a test that gates another loop", LOOKUP + '[ -n "$id" ] && until true; do :; done; ' + WAIT),
    ("a check before the lookup", '[ -n "$id" ] || exit 1; ' + LOOKUP + WAIT),
    ("a waiting loop before a later lookup", 'until [ -n "$id" ]; do id=$(gh run list -w deploy.yml -L 1 --json databaseId -q ".[0].databaseId"); sleep 3; done; ' + LOOKUP.replace("deploy.yml", "build.yml") + WAIT),
    ("an exit after a semicolon inside a string", LOOKUP + '[ -n "$id" ] || { echo "no run; exit by hand"; }; ' + WAIT),
    ("an exit inside a string that names a variable", LOOKUP + '[ -n "$id" ] || { echo "no run for $GITHUB_SHA; exit by hand"; }; ' + WAIT),
    ("an if block whose string names a variable", LOOKUP + 'if [ -z "$id" ]; then echo "should exit on $sha"; fi; ' + WAIT),
    ("an if block that only says exit", LOOKUP + 'if [ -z "$id" ]; then echo "should exit"; fi; ' + WAIT),
    ("an exit that is only a word in a string", LOOKUP + '[ -n "$id" ] || { echo "no run, cannot exit cleanly"; }; ' + WAIT),
    ("a waiting loop that never looks the id up", LOOKUP + 'until [ -n "$id" ]; do sleep 5; done; ' + WAIT),
    ("a lookup repeated after the check", LOOKUP + '[ -n "$id" ] || exit 1; ' + LOOKUP.replace("deploy.yml", "build.yml") + WAIT),
]

ALLOW = [
    ("an empty check that exits", FIND + '[ -n "$id" ] || { echo "no run found"; exit 1; }; ' + WAIT),
    ("an exit block that names a variable in braces", FIND + '[ -n "$id" ] || { echo "no run for ${sha}"; exit 1; }; ' + WAIT),
    ("an empty check written with -z", FIND + '[ -z "$id" ] && exit 1; ' + WAIT),
    ("an empty check guarding the loop itself", LOOKUP + '[ -n "$id" ] && ' + WAIT),
    ("an if block", LOOKUP + 'if [ -z "$id" ]; then exit 1; fi; ' + WAIT),
    ("test -n with ||", LOOKUP + 'test -n "$id" || exit 1; ' + WAIT),
    ("test -z with &&", LOOKUP + 'test -z "$id" && exit 1; ' + WAIT),
    ("a parameter check", LOOKUP + ': "${id:?no run found}"; ' + WAIT),
    ("a deadline on SECONDS", LOOKUP + 'until [ "$(gh run view $id --json status --jq .status)" = completed ] || [ $SECONDS -gt 3600 ]; do sleep 30; done'),
    ("a deadline on date", LOOKUP + 'end=$(( $(date +%s) + 3600 )); until [ "$(gh run view $id --json status --jq .status)" = completed ] || [ "$(date +%s)" -ge "$end" ]; do sleep 30; done'),
    ("a SECONDS bound with -lt", LOOKUP + 'while [ $SECONDS -lt 3600 ] && [ "$(gh run view $id --json status --jq .status)" != completed ]; do sleep 30; done'),
    ("a date bound in arithmetic", LOOKUP + 'end=$(( $(date +%s) + 3600 )); until [ "$(gh run view $id --json status --jq .status)" = completed ] || (( $(date +%s) > end )); do sleep 30; done'),
    ("a counter bound", LOOKUP + 'n=0; until [ "$(gh run view $id --json status --jq .status)" = completed ] || [ $n -ge 120 ]; do n=$((n+1)); sleep 30; done'),
    ("an if block that runs the loop only with an id", LOOKUP + 'if [ -n "$id" ]; then ' + WAIT + '; fi'),
    ("a bare string test that exits", LOOKUP + '[ "$id" ] || exit 1; ' + WAIT),
    ("an if block that says why, then exits", LOOKUP + 'if [ -z "$id" ]; then echo "no run found"; exit 1; fi; ' + WAIT),
    ("a multi-line if that exits", LOOKUP + '\nif [ -z "$id" ]; then\n  echo "no run"\n  exit 1\nfi\n' + WAIT),
    ("then on its own line", LOOKUP + '\nif [ -z "$id" ]\nthen\n  exit 1\nfi\n' + WAIT),
    ("waiting for the run to appear, then polling it", 'until id=$(gh run list -w deploy.yml -L 1 --json databaseId -q ".[0].databaseId") && [ -n "$id" ]; do sleep 3; done; ' + WAIT),
    ("elapsed SECONDS in arithmetic", LOOKUP + 'start=$SECONDS; until [ "$(gh run view $id --json status --jq .status)" = completed ] || [ $((SECONDS - start)) -gt 3600 ]; do sleep 30; done'),
    ("waiting while the id is empty", 'while [ -z "$id" ]; do id=$(gh run list -w deploy.yml -L 1 --json databaseId -q ".[0].databaseId"); sleep 3; done; ' + WAIT),
    ("a counter compared with -lt", LOOKUP + 'n=0; while [ $n -lt 120 ] && [ "$(gh run view $id --json status --jq .status)" != completed ]; do n=$((n+1)); sleep 30; done'),
    ("an arithmetic counter", LOOKUP + 'n=0; until [ "$(gh run view $id --json status --jq .status)" = completed ] || (( n > 120 )); do n=$((n+1)); sleep 30; done'),
    ("a literal run id", 'until [ "$(gh run view 42113656207 --json status --jq .status)" = completed ]; do sleep 30; done'),
    ("a one-shot view", LOOKUP + "gh run view $id --log | tail -20"),
    ("waiting for the run to appear, then watching it once", 'until id=$(gh run list --workflow deploy.yml -L 1 --json databaseId -q ".[0].databaseId") && [ -n "$id" ]; do sleep 3; done; gh run watch $id --exit-status'),
    ("a loop unrelated to runs", 'until curl -sf http://localhost:8080/health; do sleep 5; done'),
]


def denied(cmd):
    payload = json.dumps({"tool_name": "Bash", "tool_input": {"command": cmd}})
    out = subprocess.run([sys.executable, HOOK], input=payload, capture_output=True, text=True).stdout.strip()
    return bool(out) and json.loads(out)["hookSpecificOutput"]["permissionDecision"] == "deny"


failed = 0
for name, cmd in DENY:
    if not denied(cmd):
        failed += 1
        print(f"FAIL should deny: {name}")
for name, cmd in ALLOW:
    if denied(cmd):
        failed += 1
        print(f"FAIL should allow: {name}")
if subprocess.run([sys.executable, HOOK], input="not json", capture_output=True, text=True).stdout.strip():
    failed += 1
    print("FAIL unreadable input must allow (fail-open)")
total = len(DENY) + len(ALLOW) + 1
print(f"{total - failed} of {total} cases pass")
sys.exit(1 if failed else 0)
