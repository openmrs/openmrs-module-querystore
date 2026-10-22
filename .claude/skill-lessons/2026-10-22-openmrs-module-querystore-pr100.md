# harden 0.43.2 · openmrs-module-querystore · PR 100 · 2026-10-22
outcome: converged (cycle 7: its Phase 1 pass found nothing substantive and its one Phase 2 did not escalate)
rounds: n/a (not a pr-harden run)   cycles: 7   agent passes: 37 agents (9 Phase 1 reviewers, 7 four-agent Phase 2 passes); the Phase 1 passes of cycles 1 and 2 were the orchestrator's   verifier: n/a (`/verify` drove the real `pool-run` CLI and gate hooks before each code commit)
context: compacted once, at cycle 4's Phase 2 dispatch · peak not surfaced
capture: hand-written by the orchestrator. `harden` appends no run record, and this was a standalone
`/harden` on an open PR, asked for by the user after the slice was written and the PR opened.
transcript: /root/.claude/projects/-home-user-openmrs-module-querystore/826e4e5b-d6e7-538c-9e4c-91ea103fac93.jsonl (the 19:25-20:31 UTC part, before the repository was attached, is also in /root/.claude/projects/-home-user/826e4e5b-d6e7-538c-9e4c-91ea103fac93.jsonl)

## What the run was
resolve-ticket 0.29.0's Step 1 pre-flight for `CLAUDE_CODE_RETRY_WATCHDOG`, under which a request at a
claude.ai usage limit waits for the reset inside the turn instead of ending it. When `/harden` was
asked for, the slice was 3 files, +85/-5 (commit 87697a6). Hardening found that as written it would
break `pool-run`'s driver (a headless session waiting under the watchdog neither ends nor goes quiet,
so it is killed at its ticket timeout instead of suspended), that the gates' one-hour await bound
would read a waiting agent as dead, and that `--work`'s own carry would nudge a session the watchdog
had already resumed. At the end: 19 files, +810/-92, plus a 116-file measurement artifact, in 29
commits. Wall clock 21:04 to 07:40 UTC (10 h 35 min), taken from the transcript's event timestamps
and agreeing with the commit times (first harden commit 21:16, last 07:33).

## Cycle by cycle (UTC; what reopened each)
- c1, 21:04-22:47. Phase 1: the orchestrator. Phase 2 (21:22): 3 of 4 agents SUBSTANTIVE — the
  remedy sentence never said why an alias and not an `export`, and an export turns off `pool-run`'s
  usage-limit carry; an evidence paragraph contradicted itself on the dropped keepalive. Escalated.
- c2, 22:47-23:32. Phase 1: the orchestrator. Phase 2 (22:47): 1 SUBSTANTIVE — past the bound, the
  gates' block text told the run to re-dispatch the phase, beside an agent still waiting. Escalated.
- c3, 23:32-01:00. Phase 1 agent: 4 SUBSTANTIVE (note tests checked a substring, so an appended note
  passed; no harden case for an await with no `since`; "listed twice" misread a wave recorded under
  one label; a false comment and docstring). Phase 2 (00:01): 3 SUBSTANTIVE (a wording one; the
  `--work` carry nudging a watchdog-resumed session; a watchdog in `settings.json`'s `env` reaching
  every session `pool-run` starts). Escalated.
- c4, 01:00-02:28. Phase 1 agent: 5 SUBSTANTIVE (the carry check ignored `limit_wait_max_seconds: 0`;
  the settings refusal's scope wrong both ways; two unchanged passages falsified; Step 1's only remedy
  unusable for a carry-off `--work`; the suite read the operator's real `settings.json`). Phase 2
  (01:35): 3 SUBSTANTIVE — `--work` still passed `crossSessionInbound: accept` at a wait cap of 0
  (pre-existing on main, contradicting the slice's new `work_carry_on` docstring); an evidence
  sentence said every gate-test case was calibrated against the gate without the note, six passed
  without it; `cmd_work`'s `carried=` argument could be deleted with every test green. Escalated.
- c5, 02:28-03:21. Phase 1 agent: 0 substantive, 5 cosmetic. Phase 2 (02:56): 1 SUBSTANTIVE — an
  evidence sentence ("every message the agent's query yields re-arms that timer") had dropped the
  binary's four bookkeeping exceptions, which the artifact's summary kept; written in cycle 4
  (d7cfefa) and read by cycle 4's Phase 2. Escalated.
- c6, 03:21-06:30. Phase 1, four agent passes plus one lost to a container restart (03:44): p1, 2
  SUBSTANTIVE (the `--work` env case inherited the very marker value a carried launch sets, so it
  could not see a launch that set none — a blind spot the orchestrator's own cycle-5 polish
  consolidation introduced; two docs still called the carry-off environment untouched); p2, 1
  (`test_the_carry_can_be_turned_off`, from main, could not fail: its stub never served, so no nudge
  was possible whatever the switch said); p3, 1 (nothing pinned that an await recorded again is named
  twice, which State's "once" reads); p4, 0 (cosmetic: `yes`/`on`). Phase 2 (06:00): 1 SUBSTANTIVE —
  the check named "every other value Claude Code reads as on" never wrote `"1"`. Escalated.
- c7, 06:30-07:40. Phase 1 agent: 0 substantive, 1 cosmetic. Phase 2 (07:04): 0 substantive.
Six of seven Phase 2 passes escalated. An escalation cost one or more fresh Phase 1 agent passes
(15-45 min each) and another four-agent Phase 2 over the whole slice (25-35 min wall each).

## Refuted by measurement
- "a headless `pool-run` session under the watchdog meets the driver's limit handling" -> it streams a
  retry line every 30 s, never goes quiet and sends no `rate_limit_event`; killed at a 60 s ticket
  timeout (artifact case pool-timeout) · cost: the `Session` drop, cycle 2-3
- "the pair in front of `pool-run --work` reaches only the interactive session" -> it reached the
  launcher's limit probe, which waited out its 300 s timeout (probe-before-vars) · cost: cycle 3
- "a watchdog in `settings.json`'s `env` is harmless once `pool-run` drops the pair" -> it still waits
  (settings-env-watchdog) · cost: the preflight refusal, cycle 3-4
- "a `0` in `settings.json`'s `env` is a safe way to turn it off" -> it overrode an alias's `=1`; the
  session ended at the first 429 (settings-env-zero-alias against settings-env-none-alias, 2.1.307)
  · cost: the refusal's remedy text, cycle 4 Phase 2
- "every gate-test case was calibrated against the gate without the note" -> six that read the reason
  passed without it (`gate-calibrate.py`) · cost: part of cycle 4's escalation
- "`await_agents()` runs only on block paths" (the orchestrator's brief) -> it runs on an allow path
  past the bound with a stale await (a jq-call count) · cost: none, deferred
- "`test_the_carry_can_be_turned_off` pins the switch" -> it passed against a watcher that ignored the
  switch · cost: one cycle-6 Phase 1 pass
- "a wait at a limit holds a live agent past the hour" -> the longest wait measured was 11 minutes;
  now "can hold" · cost: polish, cycle 7

## Raised by a fresh agent, missed by the author
All of the SUBSTANTIVE items listed per cycle above were raised by fresh agents. After cycle 2 the
orchestrator found two gaps itself, both in its own cycle-4 Phase 2 fixes (438c54a: no case read the
unattended block's agent names, and nothing asserted the refusal's remedy). Of the escalating Phase 2
items from cycle 4 on: one
changed runtime behaviour (the inbound flag at a cap of 0, pre-existing on main), two were sentences
in `evidence.md`, which no run loads, and two were test-coverage gaps.

## Where a skill blocked or contradicted this run
- harden:Phase 2 — "If Phase 2 turns up something SUBSTANTIVE … it escalates on the spot: Phase 1
  resumes and runs to its own convergence, and Phase 2 then runs once against what that produced."
  Cycles 5, 6 and 7 were each opened by one Phase 2 item whose fix was a clause or a line (a
  qualifier restored in `evidence.md`; `"1"` added to a test's loop), and each cost about an hour.
- harden:Phase 2 — "four parallel review agents … over the current diff". Each pass reviewed the whole
  slice: 34 files at cycle 1, 135 at cycle 7 (116 of them artifact files) and ~200 lines of evidence
  prose. The cycle-5 escalation was in a sentence cycle 4's Phase 2 had already read.
- harden:What this does not license — "a false universal in a javadoc is still substantive" was
  applied to `evidence.md`, which "a run does not need to read", so evidence wording reopened cycles.
- harden:Test coverage — "ask what the FIXTURE can express". In cycle 6 four Phase 1 passes each found
  one test that could not fail or did not pin its behaviour, one gap per 30-60 min pass.
- harden:Termination — "Cost, elapsed time and turn length are not termination conditions." The
  labelled override was not taken; the run went 10 h 35 min.
- harden:Phase 2 — "each with `run_in_background: false` where the `Agent` schema carries it". Every
  Agent call made that way returned "Async agent launched successfully"; the recorded awaits and the
  gate's attended allow carried the yields.
- External: a claude.ai usage limit at 00:11 killed the four cycle-3 Phase 2 agents (this session ran
  without `CLAUDE_CODE_RETRY_WATCHDOG`, the slice's own subject); the user resumed it at 00:21. A
  container restart at 03:44 killed a Phase 1 reviewer launched at 03:39; it was re-run.
- pool-test: `live_session_fixture` leaves an orphan `sleep 300` per case, and a harness that captured
  the suite's stdout through a pipe waited for them (~10 min lost); writing to a file avoids it.

## Declined
- the gates reading the variable, or the Stop hook's `background_tasks`, themselves — a stale block in
  a session without the watchdog carries a note whose condition is false, which the run reads past,
  and liveness stays the one-hour clock plus the model's judgment (measured: 2.1.307 passes
  `background_tasks` to the hook; 2.1.301-2.1.305 not measured)
- preflight does not read a project-level `.claude/settings.json`, managed settings or
  `claude.extra_args` — the variable set there leaves the driver's sessions waiting until a ticket
  timeout, as before the slice
- `await_agents()`'s one jq call (~2 ms) on an allow path past the bound with a stale await
- running `test_the_carry_can_be_turned_off`'s cases concurrently (~20 s of suite time; no pipeline
  run pays it)
- a helper for four watcher tests whose fixture was already duplicated on main
- wording an earlier pass had settled: harden's await pointer, `by-hand.md`'s opening sentence, Step
  1's "in any case"

## Assumptions review overturned
- "the slice is a small skill-text change" -> it would have broken `pool-run`'s driver and the gates'
  await bound; cycles 1-4 grew it from 3 files (+85) to 19 (+810) and a 116-file artifact
