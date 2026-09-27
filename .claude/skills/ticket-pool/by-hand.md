# ticket-pool — two sessions by hand

This is `SKILL.md`'s *Two sessions by hand*. Read the whole file before you start a `--work` or
`--claim` session, release a slot, or act on a hand-launched run's ledger row. Where it names a
section or a rule it does not contain, that is in `SKILL.md`, beside this file. What a `--work`
session opens to any local process so that a usage limit cannot strand it, and why a session started
any other way is not carried past one, is in `pausing.md`, beside it too.

**What a hand-launched run does NOT get, and what it now does.** `ticket.timeout_seconds` and
`ticket.quiet_seconds` are enforced by `Session`, which only the driven path and the retro use; a
`--work` session is an ordinary interactive `claude` under `Popen`. So it is not killed on a clock,
and — because it inherits your terminal rather than emitting `stream-json` — it records no
`cost_usd` and no `turns` either. That mattered more than it sounds: measured on this machine on
2026-09-08, 32 of 34 ledger rows were `launched_by: work`, the five longest runs were all `--work`
at 47.7h down to 19.7h and every one of them ended `error`, and only the two driver-launched runs
recorded a cost at all.

Both bounds are now WATCHED rather than enforced: passing the timeout, and going quiet for
`quiet_seconds`, each report once — to the log and to your channel — and neither ends the session.
Killing is left to you on purpose: the driven path may kill on a clock because it is headless and
the bound is the only thing that can end it, while this session has somebody who can answer it.

Quiet is read from the session's own transcripts under `~/.claude/projects/`, and only writes made
**since this run started** count. That directory is keyed on the worktree path, which is
deterministic per ticket, so it outlives the worktree — #266's still held transcripts from
2026-08-27 with no worktree at all, and without that floor the next run of that ticket is "quiet for
twelve days" on its first tick. The whole subtree is read, not just the top level: a session's
subagent transcripts sit under `<session-id>/`, 16 of #266's 17 files were below it, and the largest
gap in a parent jsonl of a real `--work` run is hours. Fail-open throughout — nothing written yet is
absence of evidence, not a stall, and the bound still catches a session that wedges before its first
write.

**And a hand-launched START closes out what an earlier one left behind** — `--work` and `--claim`
both. A row still saying `running` is otherwise reaped only when a DRIVER starts, which on this
machine happened twice against 32 hand-launched runs, leaving seven rows reporting a live session
days later.

**What it takes as proof is a recorded pid that no longer exists, and nothing weaker.** Not "no
lease holds it": `release_claim` unlinks a lease with no liveness check at all, and `active_leases`
prunes one whose worktree was removed, so an operator typing `--release` in a second terminal would
have the next session publish `error` over a run that is still working — and `write_ledger` keeps
the flag saying so as a field it never saw, so the lie outlives the session that disproves it. A row
with no pid, which is every row written before this existed, is left alone and marked by `--status`
rather than rewritten. It also cannot be `reap_running`: that one's soundness IS the machine lock,
which a hand-launched session never takes.

If you would rather drive two `claude`
sessions yourself — to watch them, or to interrupt one — the isolation still has to come from
somewhere, because **a session started by hand has none of it**. It inherits no
`OPENMRS_STANDALONE_HOME`, no `MAVEN_ARGS` and no `CLAUDE_PIPELINE_SLOT`, and if you start both in
your checkout they share one working tree. Measured: two sessions in one directory share ONE gate
entry, and the first ticket's is silently gone — the later writer wins. Give them a worktree each and
both entries survive.

**`pool-run` is a shell script, not a skill.** Skills — `/resolve-ticket`, `/ticket-pool` — are what
you type INSIDE a Claude Code session. `~/.claude/pipeline/pool-run` is a program you run in a
terminal, and `--work` is a program that STARTS a session. Mistaking one for the other is easy and
the failure was silent, so `--work` now refuses when it has no terminal and says which it wanted.

So: **one command per terminal.**

```bash
pool-run --work 266      # terminal 1
pool-run --work 297      # terminal 2
```

That is the whole procedure. Each starts an ORDINARY interactive `claude` — your terminal, your
session, watch and interrupt it as always — already in the ticket's worktree, already holding a
standalone and a maven repository of its own, with `/resolve-ticket <url>` already invoked. When you
finish, the slot is given back automatically: on a clean exit, on a failure, and on an interrupt,
because the release is the half a person forgets and it has to happen on the paths they forget it on.

`--work` refuses to run from inside a session that already holds a slot, since that is the mistake
that puts two runs in one worktree.

**Permission prompts.** `claude.skip_permissions` starts `--work` sessions with
`--dangerously-skip-permissions`, so a run never stops to ask; `--skip-permissions` /
`--no-skip-permissions` override one invocation. It is opt-IN here and unconditional in the headless
driver, and the difference is not an oversight: a headless run has nobody to ask, so a prompt is not
a pause but a hang until the quiet watchdog kills the run hours later, while an interactive session
has somebody in front of it who could answer. What the bypass is scoped to is worth knowing — the
session's own worktree, its own maven repository and its own standalone, none of which is the
operator's checkout.

**The whole `claude` block reaches `--work` too**, which for a while it did not: the headless driver
read `model`, `effort`, `max_budget_usd` and `extra_args` and `--work` read none of them, so a
configured model silently did not apply to the sessions an operator actually watched. One definition
now, `common_claude_args`, read by both launchers.

**Remote Control travels with it.** It is a flag on the LAUNCH (`--remote-control [name]`), so a
launcher that does not pass it silently costs you phone monitoring, with nothing in the session to
say why it is missing. `claude.remote_control` in `pool.json` turns it on for every `--work` session,
`--remote-control` / `--no-remote-control` override one invocation, and the session is named for its
TICKET rather than the host — `chartsearchai-266` — because the point of monitoring two sessions from
a phone is being able to tell which is which. It applies to `--work` only: the headless driver's
sessions are not interactive and the flag means nothing to them.

Nothing else about the environment is touched. The session inherits `os.environ` whole, plus the
three variables the slot adds, so credentials, config and proxy settings reach it exactly as they
would if you had typed `claude` yourself.

**Ctrl-C goes to the session, not to the launcher.** It has to be said because it very nearly did not
work: Ctrl-C reaches the whole foreground process group, so without care `subprocess.run` raises
`KeyboardInterrupt` out of the wait while the session is still running and the release then deletes
the worktree out from under a live `claude`. Measured before the fix, against a child that caught
SIGINT and ran on for four more seconds: the launcher returned in **0.4s**. In Claude Code Ctrl-C is
how you interrupt a tool call, so that is the most-pressed key in the product, not an edge case. The
launcher now absorbs SIGINT with a no-op HANDLER — not `SIG_IGN`, which `exec` would leave inherited
and so disable Ctrl-C inside the session too.

Closing the terminal is handled too. SIGHUP's default action kills the launcher outright, so the
release never ran and the lease outlived the session — measured, the lease file and the worktree were
both left behind. SIGHUP and SIGTERM are now absorbed for that reason, exactly as SIGINT is for its
own: the launcher outlives the signal by the moment it takes the session to die, then releases.
`--claims` and `--release` remain for anything that gets past all of it.

**One claim per ticket, and it is a safety check rather than tidiness.** The worktree path is derived
from the ticket, so a second claim for the same one resolves to the SAME directory — and creating a
worktree releases whatever it finds there first. A running session that has just committed has a
clean tree, so that release succeeds: measured, the second claim deleted the live session's committed
file and left two leases pointing at one directory, silently.

**A lease is per REPOSITORY and ticket, not per number.** Issue numbers collide across repos freely,
so `o/b#266` is a different ticket from `o/a#266` and both may be claimed at once. `--release 266`
works while the number is unambiguous and is REFUSED, naming the candidates, once it is not —
`--release o/b#266` picks one. Guessing there would delete a worktree belonging to a live session.

**A lease also records the INSTANCE it reserves, not just the slot name.** Editing
`parallel.standalones` under a running session re-points a name at a different standalone, and a free
name could then carry an instance somebody was already using.

**And a claim refuses to start while a driver is running**, which is the mirror of the driver
refusing while a claim is held. Without both halves the symmetry is decorative — a claim would take a
standalone the driver was already using. Only a LIVE holder counts; a lock left behind by a killed
driver blocks nobody.

The pieces are still there if you want to drive them yourself — `--claim 266` prints the `cd` and the
three exports instead of launching anything, `--claims` lists what is held, `--release 266` gives one
back by hand. Reach for those if you start `claude` some other way; otherwise `--work` is the whole
of it.

A claim is a worktree cut from `origin/<default>` plus a leased standalone and maven head — the same
three things the driver hands a worker. The lease is taken with an exclusive create, so two claims
racing cannot pick one standalone; that is the part that is easy to get wrong by hand and the reason
this is a command rather than a paragraph of instructions.

A lease is released when `--work`'s session exits, by `--release`, or reclaimed automatically once
its worktree is gone — it cannot
be pid-owned, because you claim first and start `claude` afterwards, so there is no process to point
at when the lease is written. `--release` also clears that worktree's gate entry, because a re-claim
of the same ticket reuses the same path and would otherwise inherit a stale `phase: building` and
block the new session's Stop gate for six hours.

**The driver refuses to start while any claim is held**, naming them. Both would be using the same
standalones and the driver cannot see what a session it did not start is doing with one.
