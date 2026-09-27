# ticket-pool — pausing, and picking it up later

This continues `SKILL.md`'s *Pausing, and picking it up later*, which keeps the commands and the
Ctrl-C rule. Read the whole file before you pause or resume a pool, abandon a paused ticket, or act
on a ticket a usage limit paused. Where it names a section or a rule it does not contain, that is in
`SKILL.md`, beside this file.

**A claude.ai usage limit pauses the pool by itself, and un-pauses it by itself.** The CLI's own
wait-for-the-reset is gated on an INTERACTIVE session, so it can never arm under the `claude -p` the
driver runs: a headless session simply ends when the window closes, and for as long as that was all
the driver saw it read as a crash — the attempt spent, the worktree dropped, a driver-capture record
written. So the driver now reads the `rate_limit_event` records the stream already carries and
treats a rejection as `--pause --now`: the same suspension, keeping everything the paragraph below
lists, and then it sleeps until the reset the CLI reported and re-enters the sessions. Nothing is
typed and nothing is owed. `pool-run --status` shows such a row as paused with the reset time on it,
and the wave that comes back is worked before the retro, which must not run while a ticket is
mid-attempt. It hands back to you instead in three cases, each named in the log by
`wait_for_limit_reset`: a suspended ticket with no reset time on it (an operator's own pause, in the
same wave), a reset further out than `ticket.limit_wait_max_seconds` — 6h by default, so a five-hour
window is waited out and a weekly one is not — and a ticket suspended this way before that did
nothing with the last reset, which is a session spinning rather than a big ticket.

**A `--work` session is carried past it too, by a third mechanism.** Neither of the other two
reaches a hand-launched session: Claude Code's own wait belongs to the SESSION and can be gated off
for an account (measured 2026-09-11 — three `--work` sessions sat idle 4h14m past a 07:10 reset and
the continuation prompt is in none of their transcripts), and the driver's wait belongs to sessions
it can suspend and re-enter, which a `--work` session is not: it has no ledger row carrying its id
and worktree back. So `watch_hand_launched`, which otherwise only ever reports, does the one thing
it acts on: when the window reopens it tells the session to continue, over the local peer socket the
session publishes for itself. Three guards stand in front of that, because a wrong nudge is a turn
injected into a session that was working — the run must be quiet, the ACCOUNT must have refused a
probe rather than merely be suspected, and the account must then SERVE one again. That last is the
signal and a reset time is not: the rejected form of the `rate_limit_event` that would carry one
has never been captured here (all 304 records in the kept streams say `allowed_warning`, because a
rejection ends the run that would log one), so a named window only says when to stop asking early.
`procStart` and the session id keep a recycled pid from being handed somebody else's resume, and
both are compared as INSTANTS rather than as strings, because `claude` records its start in UTC and
`ps` prints local — compared as text they disagree by the machine's offset on any box that is not
on UTC, which silently refused every nudge the carry ever sent until 2026-09-13.

**It only works on sessions this launcher started**, and that is not an accident of packaging. A
session running `--dangerously-skip-permissions` HOLDS an inbound peer message unless it can
identify the sender as another session in its own permission class, and `pool-run` is a python
script — unidentifiable whatever it puts in the envelope. So `--work` passes
`crossSessionInbound: accept` on the launch, scoped to that session: it lets any local process put
a turn into a pool worktree that is already bypassing prompts, and deliberately does not enrol the
operator's other sessions, which are not inside that boundary. A session started by hand will have
its nudge held, and will say so in its own transcript.

**The account is asked per quiet SPELL, not per watcher.** A session that has been writing and goes
quiet again is looked at at once; only repeats within one spell back off, at
`ticket.limit_probe_seconds` (120s). Pacing it per watcher is what let #315 sit idle for an hour on
2026-09-14: a probe spent during an earlier silence pushed its next look ten minutes out, it stopped
six minutes before its window reopened, and by the time it looked the account was serving — which
says nothing without a refusal to pair it with. That interval is a SETTING and deliberately not
derived from `quiet_seconds`: deriving it left a pool with a small quiet window unable to exercise a
long interval, so the case written for that bug passed against the broken code. `ticket.limit_continue_work: false`
turns it off, and so does `limit_wait_max_seconds: 0`, which is one instruction — "do not wait for
usage limits" — answered the same way on both paths.

**What a suspended ticket keeps.** SIGTERM to the session's process group, and then four things that
together are the whole feature: its transcript (kept under its own session id, which `--resume`
re-enters with `claude --resume`), its worktree, its gate state, and its ATTEMPT — a pause is the
middle of a try, not a failed one, and spending the attempt would let two pauses exhaust
`ticket.max_attempts` and leave the ticket needing a human. No run record is written either: a record
counts towards the retro threshold, and this run has not finished to be learned from.

**Resuming continues the conversation; it does not start a second one.** Measured 2026-08-30 outside
the suite, because the whole feature rests on it: a headless session killed with SIGTERM after 4 of
12 steps resumed from `--resume <session-id>` and did steps 5–12 only, signing off with a token that
only the ORIGINAL prompt defined — so the transcript was inherited, not re-derived from the files on
disk. The driver's half of that is what `pool-test.py`'s `test_pause_now_suspends_and_resumes` pins:
that the worktree is re-entered rather than recreated (a fresh `make_worktree` would remove the tree
and cut a new checkout under a session whose whole context is the old one), and that the second start
carries `--resume <id>` and not `--session-id`.

**A resume re-screens what never started.** A pause is an invitation to come back much later, and
the un-started tail of the queue was screened when the pause was taken, not now. So `--resume` puts
it back through `consider` — the same predicate a fresh run uses — which is what stops it opening a
SECOND PR for a ticket somebody took to one by hand in the meantime. It screens as the PAUSED run
would have, forced or not, because the operator's `--ticket` decision belongs to the queue and it is
the same queue. A SUSPENDED ticket is deliberately not re-screened: it is mid-attempt with its
worktree and session open, and the ledger row it would be judged against is its own.

**The plan is the only place the remaining ORDER survives.** Everything else could be re-derived —
the label query would find the unworked tickets again — but an operator who typed
`--ticket 310,297,266` chose that sequence, and which tickets run first decides what evidence the
retro reads. `--resume` therefore replays the saved queue rather than rebuilding one, and reuses the
width and retro setting the paused run was using unless this invocation names its own.

**A paused ticket is skipped by a plain `pool-run`**, and told to use `--resume`. Working it would
open a second session on the same branch while the first is suspended with hours of context in it,
and nothing would report the loss — a fresh session in a worktree that already holds work looks
exactly like a run that got a long way. `pool-run --status` names what is paused; `--dry-run
--resume` prints what would be picked up without consuming the plan; `pool-watch <ticket>` shows the
newest stream for a ticket, which after a resume is the resumed half — the first half is still on
disk beside it, and the SESSION's own transcript spans both. To abandon a paused ticket instead, name
it with `--ticket`: that is forced past the skip and says so, naming the session it is abandoning.
Whether the restart then begins is the worktree's answer, not the pause's — `make_worktree` refuses
while uncommitted work is in it, printing the `git worktree remove --force` that releases it, and
removes and recreates it where it is clean. Either way the branch survives, because removing a
worktree does not delete a ref, and the abandoned transcript stays on disk under its session id.

**The retro is not interruptible, and that is deliberate.** A pause suspends a session only where
something can resume it, and what carries a suspended session back is its ledger row. The retro has
no row, so suspending it would end it — losing the work, leaving the source repo mid-checkout for the
next retro to refuse as dirty, and reporting itself as "no commit landed", which is true and is not
the reason. The loop will not START one while a pause is outstanding, so what an immediate pause
meets is a retro already running: it waits for it, then stops at the next wave. A pause the run
outruns entirely is cleared and said, not left on disk for the next driver.
