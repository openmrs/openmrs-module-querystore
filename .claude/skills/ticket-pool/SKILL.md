---
name: ticket-pool
description: Work a pool of tickets to reviewed pull requests unattended, one fresh session per ticket, with a skill-retro between them so later tickets are worked by improved skills. Use when asked to work a queue or pool of issues rather than a single one, to check what the pipeline has done, or to queue work for it. Trigger phrases include "work the pool", "work through these tickets", "run the pipeline", "what has the pipeline done", "queue this issue for the pipeline".
argument-hint: "[--status] [--dry-run] [--ticket N] [--retro-now]"
version: 0.1.0
---

# Ticket pool — the outer loop, and the only loop that learns

`resolve-ticket` takes one ticket to a reviewed PR. This is the loop around it, and its whole reason
for existing is the second half of the sentence: **between tickets it runs `skill-retro`, so ticket
N+1 is worked by skills that ticket N's evidence improved.** Run tickets without that and the pipeline
repeats its mistakes at full cost, run the retro by hand and it happens when someone remembers.

The driver is `~/.claude/pipeline/pool-run` (mirrored in the source repo at `.claude/pipeline/`). It
is deterministic Python and holds no judgement: everything it decides is decidable from GitHub, git
and the state files, and the work itself belongs entirely to the skills.

## Why the loop is processes and not agents

Three properties are load-bearing and all three come from *one fresh `claude -p` session per unit*:

- **A run cannot grade its own lessons** — `skill-retro`'s founding measurement. The retro therefore
  cannot share a session with the runs whose records it reads, and it gets its own.
- **Ticket N+1 must read the skills as the retro left them.** A session holds its skill text from the
  moment it starts, so a long-lived orchestrator would work the whole pool with the skills it began
  with, and the learning would be invisible until the next day.
- **`resolve-ticket` must hold the writing context itself.** It forbids delegating the implementation,
  so the pool cannot be a fan-out of subagents — nested delegation is also what killed agents on that
  skill's first run.

## Queueing work

Label an issue `claude-pipeline` on GitHub. That is the whole queue: the driver lists open issues
carrying the label, skips what the ledger records as done and anything that already has an open PR,
and works them in ascending issue order. Unlabel an issue to take it out.

An issue with an open PR is **not** this pipeline's work — it is `pr-harden`'s entry point, which is
what `resolve-ticket` says to do with one, so the driver reports it and moves on.

## Running it

Long runs belong in a terminal, not in a tool call: a ticket takes hours and the Bash tool caps out at
ten minutes. From a conversation, use the read-only forms; hand the user the command for the rest.

| command | what it does |
|---|---|
| `~/.claude/pipeline/pool-run` | works the pool until it is empty, retroing when records allow |
| `pool-run --once` / `--limit N` | one ticket / at most N |
| `pool-run --ticket 314` | one named ticket, labelled or not |
| `pool-run --dry-run` | preflight and print the queue; starts nothing |
| `pool-run --status` | the ledger, and how many records the next retro is waiting for |
| `pool-run --retro-now` | the retro alone, ignoring the record threshold |
| `pool-run --init` | create the label in each configured repo |

Config is `~/.claude/pipeline/pool.json`: the label, a repo→checkout map, the source repo the retro
pushes to, the retro's record threshold, and the per-ticket timeout and attempt cap. Logs, the ledger
and the per-session streams are under `~/.claude/pipeline/`.

## What the driver decides, and what it must not

It decides the queue, that the checkout is clean and on an up-to-date default branch before a ticket
starts, that a session which has produced nothing for the quiet window is dead, and **what the run's
outcome was** — from `gh` and the gate state file, never from the session's closing prose. A session
reporting a ready PR that does not exist is precisely the failure an outer loop is for.

It decides nothing about the work. **The prompt is `/resolve-ticket <url>` and nothing else.** A
pipeline-level instruction added there would be governance no retro can see and no run record can
cite; anything the runs need to do differently belongs in the skill text, put there by a retro.

## The retro gate

After each ticket, if at least `retro.min_records` run records are newer than
`~/.claude/skill-lessons/LAST`, the driver runs `/skill-retro` in the source repo. The threshold
defaults to 2 because `skill-retro`'s own anti-pattern says a single record cannot corroborate
anything — below it, the driver says how many it is waiting for and moves on.

Afterwards it verifies what the retro is supposed to have left behind — a commit, pushed, `LAST`
advanced, and the live skills byte-identical to the source mirror **including the gate scripts under
`~/.claude/hooks`**, which are separate copies a skill push alone does not update. It reports
divergence and never repairs it: silently fixing a retro's output would hide the defect.

## Evidence the runs cannot produce about themselves

A run that dies writes no record of its death. So when a session ends without a record, the driver
writes one marked as driver capture, stating that the four sections a retro reads for signal are
*absent rather than empty* — nobody observed them. When a run wrote its own record but the driver saw
something the run could not have (it was killed, it exited non-zero, it left the gate entry wedged),
that is appended to the run's record. When there is nothing to add, the run's own record is left
alone. It is capture, never a proposed rule — the same contract the skills have.

## Reading the outcomes

| status | what it means |
|---|---|
| `ready` | a PR exists and is marked ready for review — the only success |
| `draft` | a PR exists, still a draft: the review loop did not converge. Read the round log, then `pr-harden` it |
| `timeout` | the driver killed the session. The record says which bound it hit |
| `no-pr` / `error` | it aborted or died before opening a PR. The run's own report is in the stream |
| `dirty-skip` | the checkout had uncommitted work, so nothing was touched. Commit or stash and re-run |
| `has-open-pr` | not this pipeline's job; `pr-harden` owns it |

A ticket is retried on a later invocation until `ticket.max_attempts`; after that it waits for a
human, because a second identical failure is evidence about the skills rather than about the ticket.

## Anti-patterns

- **Don't run two pools over one checkout.** The driver takes a lock for exactly this; a second
  driver resetting the branch under a live run corrupts both.
- **Don't add instructions to the prompt** to work around something a run keeps getting wrong. That
  is a skill edit, and it goes through a retro so it is corroborated and refuted first.
- **Don't clear a gate state entry by hand to unstick a queue.** The driver clears a leftover at the
  start of the next ticket *and reports it into the record*, because whose leftover it was is
  evidence. Clearing it by hand throws that away.
- **Don't treat a `draft` as nearly done.** The loop not converging is the outcome the whole pipeline
  is built to make visible; it earns a look, not another attempt.
