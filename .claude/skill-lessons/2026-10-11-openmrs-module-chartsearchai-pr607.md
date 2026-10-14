# pr-harden 0.38.1 · openmrs-module-chartsearchai · PR #607 · 2026-10-11
outcome: converged
rounds: 3   cycles: 0   verifier: ran (works at runtime, r1 on 9f3ed3e and r2 on 3b46201; 0 repairs)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-openmrs-module-auditlogweb/f1b92832-ce99-44a3-9d13-63eae5af9564.jsonl

Rounds: r1 1c1ade52 full, 1 blocking + 5 non-blocking, all implemented in 9f3ed3e, verifier ran.
r2 9f3ed3e full, 0 blocking + 3 non-blocking. Step 3's once-per-run exception applied: all implemented
in 3b46201, verifier ran. r3 3b46201 blocking-only, 0 blocking. Terminating count from the r3 JSON:
"findings": [].

## Refuted by measurement
- "The live-child HostDiagnosticsTest case runs on Linux CI (only macOS skips it)", stated in the test
  javadoc, ADR 174 and the PR body -> GNU yes rejects `-t` with exit 1, so the case had skipped in all 3
  CI JDK jobs. The assumeFalse turned any launch failure into a skip. · cost: 1 round
- The PR's first mutation-probe claim implied the threads wiring was test-pinned -> only the static
  helper was. Dropping the argument at the startServer / ensureServerRunning call sites passed every
  test (r1), and so did reverting startServer to the old buildServerCommand call (r2). · cost: 2 rounds
- At FINISH an omod surefire report showed 1 failure after the r1 fixer reported green -> it was the
  report from the fixer's mutation probe, which ran after the final build. Re-running on the
  committed tree gave 0. The r2 fixer brief said to run probes before the final build, and r2 had no
  such residue. · cost: 0

## Raised by a fresh agent, missed by the author
- [r1] Live-child test silently skipped on Linux CI · blocking · cost: 1 round
- [r1] llama-server matched by executable name among descendants (could pick an abandoned child); use the engine's own handle · non-blocking
- [r1] cgroupCpu read the mount top, not the JVM's own cgroup (a zero standing in for "not this cgroup") · non-blocking
- [r1] JVM's own CPU time missing, so "another process" could not be attributed · non-blocking
- [r1] README called every field a cumulative counter; several are gauges · non-blocking
- [r1] No test through the call sites where the setting takes effect · non-blocking
- [r2] startServer not tied to currentServerCommand by any test · non-blocking
- [r2] threadsArgument says "auto" when LLAMA_ARG_THREADS is inherited through the environment · non-blocking
- [r2] recordLaunch javadoc claimed a read-back rule that it did not apply to -c · non-blocking

## Where a skill blocked or contradicted this run
- pr-harden:State — the session cwd was a different repo (auditlogweb), and EnterWorktree refused the PR's worktree because it is not a registered worktree of the session's repo. The entries were keyed on the worktree path with --cwd, so the Stop gate (keyed on the session cwd) could not enforce this run. I held to the contract by hand. Cost: no enforcement for the whole run.
- pr-harden:1 REVIEW — the Agent tool here has no run_in_background flag; every spawn came back "Async agent launched". This session was attended, so I yielded on a recorded await and the notification resumed it. No cost.

## Declined
- (none)

## Assumptions review overturned
- "Docker is off on this machine" (memory file embedded-postgres-without-docker) -> the r1 fixer ran Ubuntu containers in Docker successfully. The memory may be stale.
