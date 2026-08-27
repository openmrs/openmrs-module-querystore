---
name: ticket-pool
description: Work a pool of tickets to reviewed pull requests unattended, one fresh session per ticket, with a skill-retro between them so later tickets are worked by improved skills. Use when asked to work a queue or pool of issues rather than a single one, to check what the pipeline has done, or to queue work for it. Trigger phrases include "work the pool", "work through these tickets", "run the pipeline", "what has the pipeline done", "queue this issue for the pipeline".
argument-hint: "[--once] [--limit N] [--workers N] [--work N] [--claim N] [--release N] [--claims] [--ticket N[,N,…]] [--dry-run] [--status] [--retro-now] [--no-retro] [--init]"
version: 0.12.1
---

# Ticket pool — the loop that learns

`resolve-ticket` takes one ticket to a reviewed PR. This is the loop around it, and its whole reason
for existing is the second half of the sentence: **between tickets it runs `skill-retro`, so ticket
N+1 is worked by skills that ticket N's evidence improved.** Run tickets without that and the pipeline
repeats its mistakes at full cost; run the retro by hand and it happens when someone remembers.

The driver is `~/.claude/pipeline/pool-run` (mirrored in the source repo at `.claude/pipeline/`). It
decides nothing about the work — that belongs entirely to the skills — and every call it makes for
itself is read off GitHub, git or the state files. **Two are not readings but declared bounds, and it
is worth knowing which**: a session that has printed nothing for the quiet window is *called* dead,
because nothing here can tell a wedged run from a slow one; and a PR found only by a mention in its
body is accepted only if it was created after the run started, because a body is prose.

## Why the loop is processes and not agents

- **A run cannot grade its own lessons** — `skill-retro`'s founding measurement. The retro therefore
  cannot share a session with the runs whose records it reads, and it gets its own.
- **A retro changes the skills mid-pool, and only a process started after it is guaranteed to read
  them.** That is a guarantee, not a claim about caching, and the distinction is load-bearing: a
  session's own view of the skills is refreshed on some cycle of the harness's choosing and is not
  reliably in step with disk — measured while writing this, a skill created mid-session was not
  invocable, and its listing was still being advertised after the file was deleted. An earlier version
  of this section asserted instead that a session holds its skill text from the moment it starts. That
  was never checked, and the guarantee above does not depend on the answer.
- **`resolve-ticket` must hold the writing context itself.** It forbids delegating the implementation,
  so the pool cannot be a fan-out of subagents — and nested delegation is what killed an agent on that
  skill's first run.
- **Context.** A `resolve-ticket` run is long enough that its own run record has a field for what the
  context cost. Several in one session compact, and after that ticket N's material is either bleeding
  into ticket N+1's judgement or gone.

## Queueing work

Label an issue `claude-pipeline` on GitHub. That is the whole queue: the driver lists open issues
carrying the label — reading each issue's own `labels` field rather than asking GitHub to filter, because
that filter is search-indexed and lags behind a label you just added, which would drop the ticket from
this invocation's queue silently — works them in ascending issue order, and skips what it should not start: a ticket
the ledger records as `ready`, one whose run aborted, one that already has an open PR, and one that has
spent its attempt budget. Unlabel an issue to remove it.

An issue with an open PR is **not** this pipeline's work — it is `pr-harden`'s entry point, which is
what `resolve-ticket` says to do with one. That is also what happens to a ticket whose run ended as a
`draft`: the next invocation finds the draft PR and hands it to `pr-harden` rather than starting again.
The PR is recognised from the **ledger's memory of the number** before any matching on titles or
branches is attempted, because a run that wrote `Refs` on a branch carrying no number leaves a PR that
no matching can see — and a second run on that ticket would open a second PR for one issue.

### Order

The label path works tickets in **ascending issue number**, and a named list is worked in **the order
given**. The split is deliberate. Order has consequences the driver cannot weigh: the retro fires once
the record threshold is met, so which tickets run first decide both what evidence it reads and which
ticket is worked by the skills it changed — and weighing that means judging the tickets, which is the
one thing this driver does not do. So there is no ordering policy here and should not be: an operator
who wants a particular sequence names it, and gets it unattended in one invocation rather than having
to launch each ticket by hand. Ascending stays the default for the reason a default should be boring —
the same labels give the same order every time.

So the queue is printed with a **forecast** instead: which position a retro will follow, and which run
is the first to read what it changed. It is a forecast and says so — it assumes one record per run and
that each retro advances `LAST` — and its point is that the order stops being an invisible choice while
it is still free to change. Read the un-retroed count printed under it: records banked before the pool
started shift every mark, and a pool begun with one record already in hand retros after its FIRST
ticket, not its second.

## Running it

Long runs belong in a terminal. A foreground tool call cannot exceed ten minutes and a ticket takes
hours; a backgrounded one ties a multi-hour pipeline to the lifetime of a chat session. From a
conversation, use the read-only forms below and hand over the command for the rest.

| command | what it does |
|---|---|
| `~/.claude/pipeline/pool-run` | works the pool until it is empty, retroing when records allow |
| `pool-run --once` / `--limit N` | one ticket / at most N |
| `pool-run --workers 2` | work two tickets AT ONCE; see **Working several at once** |
| `pool-run --work 266` | work ONE ticket in this terminal, in an ordinary interactive session. One per terminal |
| `pool-run --claim 266` | the same setup, printed to paste yourself, if you want to start `claude` your own way |
| `pool-run --release 266` / `--claims` | give that slot back / list the ones held |
| `pool-run --ticket 310,297,266` | those tickets, **in that order**, labelled or not, past every skip |
| `pool-run --dry-run` | preflight and print the queue; starts nothing |
| `pool-run --status` | the ledger, and how many records the next retro is waiting for |
| `pool-run --retro-now` | the retro alone, past the record threshold and the stop below |
| `pool-run --no-retro` | tickets only, no retro at all |
| `pool-run --init` | create the label in each configured repo |
| `pool-run --config <path>` | a config other than the default |
| `pool-run --outcomes` | refresh what became of every PR the ledger knows about, and stop |
| `pool-watch` | render the newest session's stream the way an interactive session reads |
| `pool-watch 310 --results` | that ticket's session, tool results included |

`~/.claude/pipeline/pool.json` holds the label, a repo→checkout map, the source repo the retro pushes
to, the retro's record threshold and timeout, the per-ticket timeout, quiet window and attempt cap, a
`parallel` block (`max_workers`, the `standalones` that bound it, and `shared_m2` if your local maven
repository is not `~/.m2/repository`), and a `claude` block — `model`, `effort`, `max_budget_usd`,
`binary`, and `extra_args` passed through to every
session. Logs, the ledger and the per-session streams are under `~/.claude/pipeline/`.

### Watching a session

Each ticket runs as `claude -p --output-format stream-json`, so the whole session is on disk as it
happens: every assistant message, every tool call, every subagent spawn and its progress. `pool-watch`
renders it — live by default, `--replay` to print and stop, `--results` for tool output, `--thinking`
for thinking blocks, `--tail N` to join near the end.

**Read the stream; do not attach to the session.** `claude --resume <session-id>` puts a second writer
on a conversation, so it is for a session that has FINISHED — `pool-watch`'s header prints the full id
for exactly that. Reading the `.jsonl` is safe at any time and cannot perturb a run.

### What became of the work

Everything else the ledger holds measures the pipeline's own activity — rounds, turns, whether a PR was
marked ready — and none of it says whether the work was any good. That evidence is downstream and
arrives days later, so every invocation refreshes it first, and `--outcomes` does only that: per PR,
whether it MERGED, was closed unmerged or is still open, its size, its reviews, and
`commits_after_pipeline`.

That last field is named for exactly what it counts — commits whose date falls after this ticket's run
finished — and **not** "human rework", which it is not: a second attempt on the same ticket lands there
too, and moves the boundary as it does. Read it beside `attempts`. A count of something checkable is
worth more here than an inference about who changed what and why.

Until several tickets have been through, this is the only column that can answer the questions this
pipeline defers to evidence: whether ordering deserves a policy, whether the retro's threshold is right,
whether a ticket shape predicts a `draft`.

## What one invocation can do

Unattended and in your name: per ticket, a branch, commits, and a **pull request on a public repo**,
plus whatever its review rounds push on top; per retro, an edit to the skills every future run obeys,
committed and pushed to the source repo. It runs for hours per ticket and spends real tokens doing it.

Three caps, in different states: `claude.max_budget_usd` bounds one session's spend and is **unset**
by default; `ticket.timeout_seconds` bounds its wall clock and ships set to eight hours; `--limit N`
bounds how many tickets an invocation takes and applies only when you pass it. The unset one is the
one to decide about before the first long run rather than after it.

## What the driver decides, and what it must not

It decides the queue, that each repository can be fetched and its default branch resolved before any
ticket starts, that each ticket gets a worktree and a slot, when a silent session has passed the bound
above, and **what the run's outcome was** — from
`gh` and the gate state file, never from the session's closing prose. A session reporting a ready PR
that does not exist is precisely the failure an outer loop is for.

It decides nothing about the work. **The prompt is `/resolve-ticket <url>` and nothing else.** A
pipeline-level instruction added there would be governance no retro can see and no run record can
cite; anything the runs need to do differently belongs in the skill text, put there by a retro.

## The retro gate

After each wave — every ticket in it finished, nothing in flight — if at least `retro.min_records` run
records are newer than
`~/.claude/skill-lessons/LAST`, the driver runs `/skill-retro` in the source repo. The threshold
defaults to 2 because `skill-retro`'s own anti-pattern says a single record cannot corroborate
anything — below it, the driver says how many it is waiting for and moves on.

Afterwards it verifies what the retro is supposed to have left behind: a commit, pushed, `LAST`
advanced, and the live copies byte-identical to the source mirror — the skills, the driver itself, and
the gate scripts under `~/.claude/hooks`, which are separate copies a skill push alone does not update.
It reports divergence and never repairs it, because silently fixing a retro's output would hide it.

**If a retro leaves `LAST` where it was, retros are OFF for the rest of that invocation**, and the
driver says so each time it skips one. The threshold is computed from `LAST`, so without that stop
every remaining ticket would trigger another retro over the same records. Read the line when you see
it: it means the rest of the pool is being worked without learning, which is the failure this skill
exists to prevent, and it wants fixing before the next batch rather than after it.

## Evidence the runs cannot produce about themselves

A run that dies writes no record of its death. So when a session ends without one, the driver writes a
record marked as driver capture, stating that the four sections a retro reads for signal are *absent
rather than empty* — nobody observed them. When a run wrote its own record but the driver saw something
the run could not have (it was killed, it exited non-zero, it left its gate entry unfinished), that is
appended to the run's record. A clean run's record is left alone. It is capture, never a proposed rule.

There is exactly one place the driver reads the run's own account of itself, and it is bounded twice:
whether the record reports an abort. It is consulted only where the driver's own evidence cannot tell
a deliberate hand-back from a crash, since both leave no PR, and it can only make the driver do less.

## Reading the outcomes

| status | what it means |
|---|---|
| `ready` | a PR exists and is marked ready for review — the only success |
| `draft` | a PR exists, still a draft: the review loop did not converge. `pr-harden` owns it now |
| `aborted` | the run hit one of `resolve-ticket`'s six abort conditions and handed back. Not retried |
| `timeout` | the driver killed the session. The record says which bound it hit |
| `no-pr` / `error` | it died before opening a PR, and its record did not report an abort |
| `died-yielding` | it ended with a background agent still outstanding. Not its judgement: it yielded, and an unattended run has no next turn to yield into. Both gates refuse this now, so a fresh sighting means EITHER the marker never reached the gate (check `pipeline/unattended/` for a file whose pid was live) or the run stopped despite being told not to — a block is persuasion, not a lock, and the two have different fixes |
| `worktree-blocked` | a previous run left uncommitted work in this ticket's worktree; nothing was touched. Read it, then `git worktree remove --force` that path |
| `checkout-blocked` | the repository could not be fetched, or its default branch does not resolve on origin, so nothing was touched. Fix the remote or the clone |
| `dirty-skip` | only in ledger entries written before worktrees: the shared checkout had uncommitted work. It can no longer happen — the driver does not touch your checkout |
| `has-open-pr` | not this pipeline's job; `pr-harden` owns it |

An abort is the skills working, not failing, and the conditions that reach this bucket are ones only a
human can settle: the ticket names another repository, the change cannot be pushed, the environment is
unrepairable, or the refutation gate found two defensible readings and no citation deciding between
them. A second identical run meets the same wall, or is asked to pick a reading `resolve-ticket`
forbids it to pick — so the ticket waits, and keeps its attempt budget. (The two remaining conditions,
the round cap and a declined blocking finding, normally leave a draft PR and land as `draft`.)
Everything else is retried on a later invocation until `ticket.max_attempts`, after which a second
identical failure is evidence about the skills rather than about the ticket.

**A `checkout-blocked` stalls every ticket for that repository**, because it means the driver could not
fetch it or could not resolve its default branch on origin — there is no base to cut a worktree from.
It costs no attempt: nothing ran.

**A `worktree-blocked` costs one ticket and no others.** It means a previous run left uncommitted work
in that ticket's worktree, and the driver will not delete a killed run's evidence to get past it. Read
the directory it names, then `git worktree remove --force` it.

*This section used to say that a dirty checkout stalled the whole pool*, and that a single uncommitted
file in the operator's own tree skipped every remaining ticket. It is no longer true and the change is
the point: the driver never checks out, resets or branches your checkout. It **fetches**, and cuts each
ticket a `git worktree` under `~/.claude/pipeline/worktrees/` detached at `origin/<default>`. Your
branch and your uncommitted work survive a pool run untouched, several tickets of one repository can be
in flight at once, and the "ahead of origin" case this section used to have to detect cannot arise —
no ticket branch is ever cut from local HEAD.

A worktree is removed when its run leaves it clean, and kept when it does not. Nothing committed is
ever at risk either way: removing a worktree does not delete the branch it was on.

## Working several at once

`parallel.max_workers` (or `--workers N`) is how many tickets run concurrently. It defaults to **1**,
which is the behaviour that predates it, and it is bounded by hardware you have to supply rather than
by cores:

```json
"parallel": {
  "max_workers": 2,
  "standalones": ["/path/to/standalone-a", "/path/to/standalone-b"]
}
```

**One OpenMRS standalone per worker, each on its own tomcat AND database port.** The verifier restarts
a real instance and drives a real query against it, so two runs sharing one would restart it underneath
each other and each would grade the other's deploy. The preflight refuses a width it cannot resource:
fewer standalones than workers, a directory with no `openmrs-standalone.jar`, or two that bind the same
port. Check the ports before configuring — on this machine five of the eight standalones on disk share
8081/3316, so "there are several standalones" is not the same claim as "several can run at once".

Each worker also gets its own maven repository: installs go to a per-slot head under
`~/.claude/pipeline/m2/`, reads fall through to the shared `~/.m2/repository` behind it, so builds stay
offline and fast while the module's own jar stops being one file two runs overwrite. That needs Maven
3.9 or newer and the preflight checks it. The tail is read from `parallel.shared_m2`, else from a
`<localRepository>` in `~/.m2/settings.xml`, else the default — a tail pointing where maven is not
looking is a tail with nothing in it, and every offline build then fails on its first dependency.

Two at once is comfortable on a 10-core / 32GB machine: each slot costs a JVM, an embedded MariaDB and
a maven build. Raise it only as far as you have standalones on distinct ports.

**Tickets are worked in waves, not as a rolling pool**, and the retro is why. `skill-retro` rewrites the
skills, and this pipeline's whole reason for existing is that a run started after it reads what it
changed — which needs a moment when nothing is in flight. A rolling pool never has one; a wave boundary
is one. It costs the width's slowest ticket, and it keeps the queue forecast meaningful: the marks say
which WAVE a retro follows, not which ticket.

Each run is told it has co-tenants through `$CLAUDE_PIPELINE_SLOT`, and the skills read it: with it set,
a run repairs the standalone, worktree and maven repository it was given and reports everything else
rather than killing by symptom. Without that scoping a verifier's ordinary "kill whatever holds the
port" is a licence to stop a sibling's server mid-query.

## Two sessions by hand

Everything above is the driver working tickets unattended. If you would rather drive two `claude`
sessions yourself — to watch them, or to interrupt one — the isolation still has to come from
somewhere, because **a session started by hand has none of it**. It inherits no
`OPENMRS_STANDALONE_HOME`, no `MAVEN_ARGS` and no `CLAUDE_PIPELINE_SLOT`, and if you start both in
your checkout they share one working tree. Measured: two sessions in one directory share ONE gate
entry, and the first ticket's is silently gone — the later writer wins. Give them a worktree each and
both entries survive.

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

If a terminal is closed outright rather than exited, the lease survives its session. `--claims` shows
it and `--release <ticket>` gives it back.

**One claim per ticket, and it is a safety check rather than tidiness.** The worktree path is derived
from the ticket, so a second claim for the same one resolves to the SAME directory — and creating a
worktree releases whatever it finds there first. A running session that has just committed has a
clean tree, so that release succeeds: measured, the second claim deleted the live session's committed
file and left two leases pointing at one directory, silently.

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

## Anti-patterns

- **Don't run two pool DRIVERS.** The lock is one file for the whole machine, not one per checkout, and
  that is deliberate: two drivers would each retro over the other's records and race the one ledger.
  Several tickets at once is what `--workers` is for, inside one driver that can hold the wave barrier.
- **Don't raise `--workers` past the standalones you actually have.** The preflight refuses it, and the
  refusal is the feature: the alternative is two verifiers restarting one server and both reporting on
  the other's deploy.
- **Don't add instructions to the prompt** to work around something a run keeps getting wrong. That
  is a skill edit, and it goes through a retro so it is corroborated and refuted first.
- **Don't clear a gate state entry by hand to unstick a queue.** The driver clears a leftover at the
  start of the next ticket *and reports it into the record*, because whose leftover it was is
  evidence. Clearing it by hand throws that away — and by hand means `jq`, an editor, or any other
  read-modify-write, because those files are shared with every live session and only
  `gate-state` serialises them. Use `gate-state clear`, which reports what it removed from inside its
  own lock. Measured on the driver's pre-fix code, which did the read-modify-write itself: 20 threads
  clearing 20 worktrees left 16-17 of the 20 entries standing and killed 56 threads on a shared temp
  path, silently. Each survivor blocks the next session in that worktree for six hours.
- **Don't treat a `draft` as nearly done.** The loop not converging is the outcome the whole pipeline
  is built to make visible; it earns a look, not another attempt.
- **Don't answer a stalled pool by widening a skip.** A ticket the driver refuses is refused for a
  stated reason; `--ticket N` runs exactly one past every skip, which is the honest way to override.
