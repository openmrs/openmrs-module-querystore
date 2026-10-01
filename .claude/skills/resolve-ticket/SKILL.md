---
name: resolve-ticket
description: Take a GitHub issue or JIRA ticket URL all the way to a pull request that is ready to merge, in one unattended run — read the ticket with its comments, plan, have the plan refuted by a fresh agent, write the failing test first, implement, prove the build green, harden with context, open a draft PR, then cycle clean-context review rounds until the sha it hands over is reviewed clean, and mark it ready. Use when handed a ticket or issue URL and asked to deliver a reviewed PR. Trigger phrases include "work this issue", "resolve this ticket", "take this to a PR", "implement and harden issue N", "here's the ticket, deliver a PR".
argument-hint: <issue-url|jira-url|issue-number|jira-key> [--max-rounds N] [--no-verify] [--plan-only]
version: 0.24.1
---

# Resolve ticket — one URL in, a mergeable PR out

Arguments: `$ARGUMENTS` — a GitHub issue URL or number, or a JIRA ticket URL or key (`O3-1234`,
`TRUNK-6429`). `--plan-only` stops after Step 3 with a refuted plan and no code. `--max-rounds N`
caps the review rounds (default 4). `--no-verify` skips the standalone verifier for the whole run.

You are handed a ticket and you deliver a pull request that a fresh reviewer found nothing blocking
in, marked ready for review. **This runs to the end without checking in.** Every phase is in this one
skill: the loop is `pr-harden`, and Step 9 *invokes* it rather than handing it back to the user.

The one asymmetry the whole design rests on: **implementation happens in this session, where the user
can steer it if they choose to watch — and every review happens in a fresh agent that has never seen
it.** So never spawn a subagent to write the implementation, and never review your own work here.

The incidents and measurements these rules rest on are in [evidence.md](evidence.md), under the same
headings, so where another skill or a gate says a section here "carries the measurement", that section
of evidence.md does. A run does not need to read it. Before changing or deleting a rule, read its entry
there.

## What this depends on

Two skills, one transitively, and a hook. Nothing else — the verifier carries its own deploy
procedure and delegates to no skill.

| dependency | where | required |
|---|---|---|
| `pr-harden` | Step 9 invokes it — the entire review loop | yes |
| `harden` | Step 7 runs it once, with context | yes |
| `pr-review` | inside `pr-harden`'s reviewer, every round | yes, transitively |
| `pr-harden-gate.sh` | Stop hook, one-time install per machine | yes — without it the termination rules are prose |
| `Explore` agent type | Step 2, delegating broad searches | no; convenience |

A missing dependency is an abort, not a degradation: without `pr-harden` there is no loop and the run
would end at a draft PR nobody reviewed, which is the outcome the whole pipeline exists to prevent.

## The autonomy contract

Read this before Step 1, because most of the ways this run fails are ways it stops.

**It decides these itself, records them, and keeps going:**

- every ambiguity in the ticket that has a defensible reading — take it, state the assumption in the
  plan, and carry it into the final report
- which findings to implement and which to decline (the fixer's job, per round, on the record)
- how to repair a broken standalone (the verifier's job, within its bounds)
- a disagreement between the plan and the refutation gate where a citation settles it: **`CLAUDE.md`
  and a recorded measurement outrank the plan**, always, so revise the plan and continue

**It aborts and hands back on exactly these — no others:**

1. The ticket names a repository other than the one this session is in.
2. The change cannot be pushed (no rights, or a cross-repository PR with `maintainerCanModify` false).
3. The refutation gate's second blocking objection **leaves the question open** — two defensible
   readings with no citation deciding between them. Usually that is an objection about the ticket's
   own meaning, since no citation can settle what the ticket did not say; but a soundness objection
   can deadlock the same way when the repo genuinely does not decide between two designs. What does
   NOT abort is a second blocking objection that **settles** the question — see Step 3.
4. The verifier reports `unrepairable` after its bounded attempts: a broken environment is not
   something more rounds fix.
5. The round cap is reached without convergence.
6. A round declines a **blocking** finding. `pr-harden` ends that run as *did not converge* rather
   than as success — the loop may refuse a wrong finding, but it may not call the result clean.

Everything else is a decision, not a question. In particular: cost, elapsed time and turn length are
not abort conditions and appear nowhere on that list.

**A partial mode is a TERMINUS, not an abort.** `--plan-only` is defined to stop at the end of Step 3,
and no gate condition can ever be satisfied by a run that opens no PR and runs no round — so such a
run **clears its own state entry** at its terminus, and must not reach for the override to escape a
gate that was never going to release.

**The Stop gate covers the whole run, not just the loop.** Write the state entry at Step 1, before any
work — see **State** in `pr-harden`, which owns the format. From that moment `pr-harden-gate.sh`
refuses to let the turn end until the head being handed over has been reviewed with zero blocking
findings — and verified, where any verifier ran — or an override is recorded. That is what makes the
run unattended rather than merely intended to be. It refuses a yield on a backgrounded build or Monitor
too, so wait inside the turn with `pr-harden`'s bounded foreground loop (*And this session must not
busy-wait either*).

Two obligations come with it. On any abort above, **write the override into the state entry with its
reason** — an abort that leaves `blocking > 0` behind wedges the next turn in this repo until the
6-hour expiry. And never hand the termination decision back: "want me to continue?" ends the run with
work owed while reading as deference. If a phase is owed, run it.

## You may not be the only run on this machine

`$CLAUDE_PIPELINE_SLOT` is set when this run has co-tenants — either the pool driver is working
several tickets at once, or an operator claimed a slot for this session with `pool-run --claim <n>`.
Either way other `resolve-ticket` runs are in flight right now, and three things are yours alone while
everything else is shared:

| yours | given as | shared, and not yours to reclaim |
|---|---|---|
| the working tree | the cwd — a `git worktree`, not the operator's checkout | the repository's object store: fetch through it, never reset it |
| the OpenMRS standalone | `$OPENMRS_STANDALONE_HOME` | every other standalone, and every port you were not given |
| the maven repository | `$MAVEN_ARGS` — a per-run head over the shared repository behind it | that shared repository, which is read-only to you: your installs go to the head |

`$MAVEN_ARGS` is read by `mvn` itself, so a plain `mvn -o clean install` already picks it up: do not
strip it, do not spell it onto a command line — interpolated it arrives as ONE argument, not several,
and the build installs into a directory named after the whole string — do not add
`-Dmaven.repo.local` of your own, and do not be surprised that
`chartsearchai-api-1.0.0-SNAPSHOT.jar` installs somewhere under `~/.claude/pipeline/m2/`. That is the
point — it is the jar `omod` unpacks over `omod/target/classes`, so two runs sharing it means one
run's classes silently under the other's tests.

If the variable is UNSET you are the only run, and nothing here applies — but note what that means
for an operator starting a second session by hand: without a claim it would share your checkout, your
maven repository and your standalone, and the two of you would share one gate entry. `pool-run
--claim` is how a hand-launched session gets what the driver would have given it.

The rule that follows from all of it: **repair what you were given, and report the rest.** A process
holding a port you did not resolve, a `java` you cannot attribute, the shared inference server — a
co-tenant may be mid-query against any of them. Killing by symptom is correct alone and destructive
beside a sibling, and only this variable tells you which you are.

## Step 1 — Resolve the ticket, and read it

Parse the argument:

| shape | it is | read it with |
|---|---|---|
| `github.com/<owner>/<repo>/issues/123` | GitHub issue | `gh issue view 123 --repo <owner>/<repo> --json title,body,comments` |
| `123`, `#123` | GitHub issue, this repo | `gh issue view 123 --json title,body,comments` |
| `openmrs.atlassian.net/browse/KEY`, `O3-1234`, `TRUNK-6429` | JIRA | the REST call below |

```bash
curl -s "https://openmrs.atlassian.net/rest/api/2/issue/<KEY>?fields=summary,description,status,comment"
```

**Never `--comments` for the ticket itself.** Off a terminal, gh 2.87.3 printed the comments and not
the body. `--json` carries both and paginates comments.

That endpoint serves **unauthenticated**. The `issues.openmrs.org`
link people paste redirects to a dashboard and will not serve REST, so never reach for it.

**Guard, before anything else:** a GitHub issue URL names its repository. If that is not the repo this
session is in, abort (condition 1) — say which repo the ticket belongs to and stop. A JIRA URL names
no repo, so it is assumed to be this one; if the ticket's text plainly describes another module, that
is also condition 1.

**Pre-flight the verifier here, not at the end.** This pipeline's terminal state is a PR marked ready,
and `pr-harden` will not mark one ready that no verifier could run — so an unavailable standalone blocks
the whole run's finish line, and finding that out in the last round wastes the chance to fix it. One
command: confirm a standalone exists (`$OPENMRS_STANDALONE_HOME` if it is set — the pool driver sets it
whenever it has an instance to assign, and then it is an assignment rather than a hint — else a
directory holding
`openmrs-standalone.jar`) and read its `tomcatport` from `openmrs-runtime.properties`, which is **not
always 8080**. **Do not check whether the port is free** — these are throwaway demo instances,
a busy port is the normal state, and the verifier simply takes and restarts the one it resolved.
What would actually block the run is having no standalone on disk at all, or no LLM endpoint for a
module that needs one. Say so NOW if either is missing, so the user can fix it while the work
proceeds.

Then check nobody is already on it: `gh pr list --state open --search "<number-or-key>"` and a look at
open branch names. If a PR exists, this is the wrong entry point — run `pr-harden <that PR>` on it
instead, and say so.

Read the **comments**, always. On this module the ticket body is frequently the first draft of the
problem and a comment is where it was corrected, narrowed, or measured — and a measurement in a
comment outranks a claim in the body. Note anything the ticket says was already tried and rejected;
re-proposing it is the most expensive mistake available at this stage.

Now write the opening state entry: `phase: "building"`, `round: 1`, no `pr` yet. `building` is the
gate's pre-PR phase — it blocks the turn from ending and says so in the language of *this* skill's
remaining steps, rather than telling you to spawn a reviewer for code that does not exist yet.
`pr-harden` moves it to `init` when it takes over at Step 9. The run is now gated.

```bash
~/.claude/pipeline/gate-state --owner $PPID pr-set --ticket 315 --round 1 --phase building --blocking 0
```

Attended from another checkout, start in the working tree or `EnterWorktree` into it before this
write — the cwd is the working tree (table above); `pr-harden`'s **State** says why.
`EnterWorktree`'s isolation guard then refuses what it cannot verify, git or not: do as its message
says, or run the steps as a script by absolute path, whose git must still target this worktree. It
refuses `$PPID` on a `gate-state` line even unchained, so run `echo $PPID` alone and pass that pid
to `--owner`.

`gate-state` is the only writer of either state file — it holds a lock across both and writes
atomically, which an inline read-modify-write cannot, and under a parallel pool cannot safely be
retyped. `pr-harden`'s **State** section has the rest of the subcommands.

## Step 2 — Plan before code

`CLAUDE.md` requires it, and this is where the run is most often lost: the ticket names a symptom and
the plan is where you decide whether you have found the cause. Read the relevant code first.

Delegate the *searching* only when the question is **broad and of unknown shape** — "where does X
live, across conventions I cannot guess", "every call site of Y". When it is narrow and the target is
named — what does this class expose, where is this global property read — grep it yourself.
Either way the judgement stays here. Then write down:

- **What the ticket actually asks for**, in your words, and what it does not. The ticket defines the
  scope; do not widen it because adjacent code looks wrong. Note the adjacent thing and leave it.
- **Whether what it asks for is a FIX at all.** The rest of this skill assumes a defect and a
  production change that closes it, and that assumption is wrong often enough to state: a ticket can
  ask for a measurement before a remedy ("establish which of these two it is before proposing a
  fix"), for an instrument, or for a diagnosis. When it does, that IS the deliverable — running the
  discriminator and reporting what it decided is the work, not a preliminary to it. Two consequences
  follow and both are easy to get wrong. The honest outcome may be *inconclusive*: "the measurement
  refutes both branches as worded, and here is what is left" is a finding, not a failure, and it is
  the finding the next change needs. And the PR then does not close the ticket, so Step 8's `Refs`
  rule binds — check it now rather than at PR time.
- **The root cause**, and how you know. `CLAUDE.md`: root-cause fixes over symptom patches, best
  solution before quickest, and diagnose *why* before proposing a fix.
- **The failing test** that will define the behaviour — which file, what it asserts, and why it fails
  on today's code. It must exercise the real production path with real data: no simulation, no mock,
  no reimplementation of pipeline logic in test code, no calling internal methods with hand-crafted
  inputs, and the composed method rather than a hand-chained pipeline.
- **If any part of the plan exists ONLY so the change can be tested, say so and label it a TRADE.**
  This is where a plan quietly gets worse while looking more rigorous. State the trade explicitly,
  and rule out "test it differently" before taking it — question 7, in `refuter.md`, is that check.
- **Which API-surface rules in `CLAUDE.md` this touches.** That file is a list of entry points that
  must not be bypassed and of changes that were measured and rejected. If the plan reinvents one of
  them, the plan is wrong.
- **Every assumption you took** on an ambiguous reading of the ticket. Take the defensible reading and
  record it here; do not stop to ask. This list goes into the final report verbatim.

`--plan-only` runs on through Step 3 and stops there — its point is a plan that has survived
refutation, not a first draft.

## Step 3 — Refute the plan, before any code

One fresh subagent, one pass, read-only. Its **only** job is to try to break the plan. This is the
cheapest gate in the pipeline and it guards the failure this module is most prone to: `CLAUDE.md` is
largely a catalogue of changes that looked obviously right and measured wrong.

Snapshot the worktree hash before spawning and compare it after — the refutation gate is read-only by
instruction, but "read-only by instruction" is not a guarantee. Tell it to restore anything it
changed **before** it reports.

**Snapshot `git branch --show-current` beside the hash, at every delegation in this skill.** A diff
hash cannot see a `git checkout`: both trees are clean, so the hash matches and the switch is
invisible. A wrong-tree edit is recoverable exactly until something commits on top of it.

Record the await — append to the entry's `awaiting` list — before spawning it, and clear that list
on ANY terminal outcome: a result, or the harness reporting the agent failed, stalled or was killed.
A death leaves a fresh await that the gate honours for the full hour, which is a licence to stop the
run with nothing running. Tell the refuter **not to spawn subagents of its own** — and if it dies,
retry twice with something changed between attempts before taking the labelled deviation
(`pr-harden`'s **State** section carries the contract).

The field lives in `pr-harden`'s **State** section. The gate blocks a yield while the run is
mid-flight and this agent runs in the background, so without the await recorded the run cannot even
wait for its own gate. `/harden` has not started yet, so this await is the pr gate's alone —
`--only pr`, after the subcommand; without it the await also reaches harden's entry and needs `--run`:

```bash
~/.claude/pipeline/gate-state --owner $PPID await "refute plan" --only pr
~/.claude/pipeline/gate-state --owner $PPID clear-await --only pr
```

**When the run is unattended, do not yield at all — collect the agent inside the same turn**, by
spawning it with `run_in_background: false` (`pr-harden`'s *Collecting in the same turn*). A
`claude -p` process stops a background agent still running 600 s after its turn ends and then exits.

Spawn it as a new subagent — **never `subagent_type: "fork"`**, which would inherit the reasoning that
produced the plan and defeat the point. Give it the ticket as read (with its comments), the plan
verbatim, and the repo. Do **not** give it your argument for why the plan is right: advocacy primes it
to agree, and agreement is the one thing this agent is not for.

Its seven questions, the JSON it returns and the rule every objection must meet are in `refuter.md`
in this skill's directory. Brief it with that file's absolute path and tell it to read the whole file
before it does anything else; the brief itself hands it the ticket, the plan and the repo, as above.
The gate below acts only on objections that meet that file's *An objection without a citation is not
an objection*.

**This is a gate, not a loop.** A blocking objection whose citation settles it: revise the plan and
re-run the gate **once**. `CLAUDE.md` and a recorded measurement outrank the plan, so that is a
revision, not a debate. Non-blocking objections are recorded in the plan and carried into the report;
they do not hold the run.

**Revise by deleting, not by re-wording.** The revision is itself unverified prose written fast under
the pressure of an objection, and it is a live source of the next false claim. So when an objection
lands on a claim, cut the unsupported clause rather than replacing it with a better-sounding one, and
re-derive any figure you carry across rather than restating it.

After that one re-run there are **three** outcomes, and the discriminator is not how many objections
have been raised but **whether the objection's citation determines the answer**:

1. **No blocking objection.** The plan stands. Proceed.
2. **A blocking objection that SETTLES the question** — its citation names the answer, including when
   the answer is "the previous revision was right". Apply it and proceed. This is convergence, not
   iteration, so there is **no third gate pass**: the objection did not open a question, it closed
   one, and re-gating a plan the gate has just told you the shape of is the loop this section forbids.
3. **A blocking objection that leaves the question OPEN** — two defensible readings and no citation
   deciding between them. That is abort condition 3: hand back with both readings, do not pick one.

**Check it, do not estimate it — the objection's own numbers included.** Outcome 2 turns on the
citation's *authority* and hands you no instrument for testing it, and by then there is no third gate
pass to catch a citation that merely looks like it decides. So where the citation is a count or a
declaration that a build settles — call sites, a modifier — take it from the compiler or a whole-tree
search before adopting the design it implies, rather than from a grep of the file in front of you.
It binds the RUN and not the gate, because the gate is read-only by instruction above and a
call-site count from the compiler means changing a signature and building.

**`--plan-only` ends here**, and ending means clearing the entry this run wrote — the whole entry for
this repo, not merely its `awaiting` list — so the next turn in this directory is ungated:

```bash
~/.claude/pipeline/gate-state clear --only pr
```

Then report: the plan, every assumption taken, and what the gate checked and objected to. A
`--plan-only` report is not a partial version of the full one — it is complete for what it covers,
and it says plainly that no code was written.

## Step 4 — Branch

Cut from an up-to-date default branch, and match the repo's own naming rather than inventing one —
`git log --oneline -20` and `gh pr list --state all --limit 15 --json headRefName,title` show it. In
this module the shape is `fix/<issue>-<slug>`, `fix/<slug>` or `feat/<slug>`.

## Step 5 — Test first, then the fix

Write the failing test. Run it. **Watch it fail for the reason you predicted** — a test that fails for
a different reason is not yet the test for this ticket, and one that passes immediately means either
the bug is elsewhere or the assertion is too loose. `CLAUDE.md`: write the strictest assertion, and if
it doesn't fail, tighten it until it does.

Then make it pass by changing production code. Never by changing the test, the expected values, or the
test data — that is changing the specification, and on a failing test the pipeline is what is wrong.

**Check `git branch --show-current` before you edit, not only before you commit.** Every phase of
this pipeline delegates, and an agent that runs `git checkout` silently redirects everything after
it. `pr-harden` states this rule for committing; committing is too late, because by then the edit is
already in the wrong tree.

**Edits made by script need three guards, because all three failures are silent.** You will edit by
running short scripts rather than by hand. `str.replace` returns the string unchanged when it
matches nothing and the script prints success anyway — so **assert the target text is present before
replacing**, and let the assert stop the script rather than falling through to the next edit. A
replacement bounded by "from here to the next method" can span further than you meant — so after any
multi-line edit, **count what should still be there** (test methods, symbols) against what you
expected; one such slice deleted a whole test method and everything still compiled. And **verify by
reading the file back**, because the script's own report is not evidence: the other two both
announced success.

## Step 6 — Green

`mvn -o clean install` from the **repository root**. Not `-pl api`, not `-pl omod`: the omod unpacks
the *resolved* api artifact over `omod/target/classes` at generate-resources, so a `~/.m2` jar from
another branch shadows the reactor's classes and reddens tests on a drift that is not in the source.

## Step 7 — Harden with context, once

Run `/harden` here, before the PR exists. This is the one place its passes are the right tool: they
run in the context that wrote the code, which makes them good at polish and at the boundaries you were
just thinking about — trace outward, the invariants in unchanged neighbours your edit may have
falsified, the test named for each behaviour change. Let it converge on its own terms (Phase 1
passes until one finds nothing substantive, then a single Phase 2 pass) and let its own Stop gate do
its job.

Do not skip it on the grounds that the loop will review anyway. The two are not substitutes: polish
with context first, adversarial review without it second. Skipping this hands the first clean reviewer
a pile of nits and spends a whole round on them.

**While harden runs, write its awaits to BOTH state files.** The gate armed at Step 1 is
`pr-harden-gate.sh`, which reads `~/.claude/pr-harden-state.json`; harden's own awaits are written
to `~/.claude/harden-state.json`. So a harden cycle blocked on its Phase 2 agents is invisible to
the armed gate, which then refuses the yield the cycle needs in order to wait. That is what
`gate-state`'s default scope is for — **omit `--only` and one command writes both**, so the pair
cannot come apart the way two commands could:

```bash
~/.claude/pipeline/gate-state --owner $PPID --run harden-1758600000 await "harden phase 2"
~/.claude/pipeline/gate-state --owner $PPID --run harden-1758600000 clear-await
```

**`--run` here must be the id the nested `/harden` minted, not one of your own.** A write whose id
differs from the entry's REPLACES it, so a second id does not coexist with the first — it deletes
it. Typed in the order above it destroys the harden run's verdict, its cycle and the head its edit
count measures against; typed the other way round it leaves the await in `pr-harden-state.json`
alone, which is the stale-await quit this step exists to prevent. `harden-1758600000` is the
example the harden skill uses; read the id out of that run's own report and use it.

Clear it in both **at the end of this step, and when a harden cycle dies or takes its labelled
override** — a fresh await left in `pr-harden-state.json` licenses a real quit for up to the gate's
hour-long TTL while Step 8 runs.

**Then confirm `harden` left its own state entry finished**, because two Stop gates are now live in
this run and both must allow the turn to end. `~/.claude/harden-state.json` must say `phase1: converged` with
`phase2: done` for this repo, or `override: true` if it took the labelled override. An entry with neither a
`run` id nor a `phase1` is one written by a `/harden` older than that contract, and there the
finished state is still `edits: 0`. A `harden` run that was interrupted
leaves the entry saying a phase is owed (or, on a legacy entry, `edits > 0`), and that then blocks the
end of *this* run even after the review loop has converged — a wedge with nothing wrong with the PR, cleared only by the 6-hour expiry. Check it
here, where it is one line, rather than discovering it after the loop.

## Step 8 — Draft PR

Commit and push, then open the PR **as a draft** — it is about to take several rounds of commits, and
a draft says that honestly to anyone watching the repo.

**Draft is also what keeps the automatic reviewer off the rounds.** The Claude Code GitHub App
reviews every push to a NON-draft PR and skips drafts entirely, so opening ready — or marking ready
early — buys one automatic review per round. `pr-harden`'s FINISH owns the rule that follows from
that: ready is the LAST action, after the last push. Do not pre-empt it here.

Match the repo's title voice, which is distinctive here: `type(scope): ` followed by a lowercase
sentence stating **the behaviour after the fix**, not the task performed — *"a long answer is no longer
cut off by a proxy that has read nothing yet"*, not *"add SSE keep-alive"*. Read
`gh pr list --state all --limit 12 --json title` and match what you see.

Link the ticket so it is machine-readable, because every round's reviewer resolves it:

- **GitHub issue** — `Fixes #123` in the body, which populates `closingIssuesReferences`.
- **JIRA** — no auto-close exists, so put the key in the **title** and the browse URL in the body.
  `pr-review` and `pr-harden` both look for a key in the title or branch name.

**`Fixes` only if the PR actually closes the ticket. Otherwise `Refs`, and say why in the body.**
GitHub acts on the keyword, so a PR that delivers something SHORT of the ticket — an instrument for
a defect it does not fix, one part of a multi-part ask, a diagnosis the ticket asked for before a
remedy — silently closes an open defect on merge.

The cost of `Refs` is that `closingIssuesReferences` comes back **empty** for that ticket — unless a
closing keyword elsewhere in the body reaches it anyway — and `pr-review` Step 1 resolves the ticket
from exactly that field. So when you use it, **name the issue number explicitly
in every reviewer brief** rather than leaving the reviewer to find it — otherwise the round that is
supposed to ask "does this resolve the ticket?" never reads the ticket at all.

**Check the field rather than the wording, with `gh pr view <n> --json closingIssuesReferences`, once
the body is written and again after any later edit to it.** That field has named an issue the PR does
not close on two runs. The cause was the same both times — a closing keyword whose scope reached an
adjacent reference — but the remedy was not: on #250 rewording the offending sentence was enough, and
on #317 rewording changed nothing while separating the two references onto their own lines and naming
the non-closing one without a `#` did.

The body says what the ticket asked, what the change does, and how it was verified — and, once
`pr-harden`'s FINISH re-derives it, what the loop left unimplemented, one line each. It does not grade
the design or tour the alternatives.

**Write it once here, and RE-DERIVE IT WHOLE before the PR is marked ready — never patch it across
rounds.** The body describes code the review loop is about to change under it, so an incremental
edit is how it comes to assert something false. So: patch it mid-loop ONLY to satisfy a blocking
finding, and at the end rewrite it against the final head, re-measuring every figure in it at that
point rather than carrying one forward.

**Treat the body as part of the change, not as a summary of it.** It is the durable public rationale
attached to the closing of the ticket, no test can fail on a false sentence in it, and a repo-wide
grep for a claim you later correct will never reach it. Two consequences. Every figure in it carries
the dataset it was measured over — a chip count taken against a four-entry fixture is not a claim
about the shipped knowledge base. And when a later round corrects a claim anywhere in the repo,
**re-read the body for the same claim**. Record the new PR number in the state entry.

## Step 9 — Run the loop, here, now

Invoke `pr-harden <the new PR number>` with this run's `--max-rounds` and `--no-verify`, and let it run
to its own termination. **Do not report the PR and stop.** The user asked for a pull request with no
blocking comment; a draft PR nobody has reviewed is not that, and handing back here is the disguised
early stop the autonomy contract forbids.

`pr-harden` owns everything from round 1: fresh reviewer, fresh fixer, the declined ledger, the
verifier, the round cap, and marking the PR ready once the sha it hands over is reviewed clean. Do not
review the PR yourself while it runs, and do not pre-empt round 1 by fixing what you suspect it will
find — you hold the writing context, which is exactly the disqualification the loop is built around.
Anything you can already see belongs in Step 7, before the PR existed.

When it converges, `pr-harden` verifies the merging head if no round already did — a runtime-visible
change is not ready until something has run it, and the loop's per-round verifier sits on the fix
path, which the exit path skips. Then the PR is marked ready (`gh pr ready`) and the run is done. A
head that cannot be verified ends the run as converged-but-unverified, not as ready.

**What it hands over is the sha a reviewer cleared, not merely a branch that once had a clean
round.** `pr-harden`'s FINISH does not edit that sha, and an edit that is genuinely owed there costs
one blocking-only round; the Stop gate compares the head against the last reviewed and verified sha
and refuses the handover otherwise. That skill's **Termination** section owns the rule — do not
restate it in the report, cite it.

## Reporting — once, at the end

One report for the whole run, in this order:

- **The ticket as you read it** — the sentence you are claiming the PR satisfies. If a comment
  corrected the body, say which.
- **Every assumption you took** on an ambiguous reading, from the Step 2 list, verbatim. This is the
  part an unattended run cannot omit: it is the only place the user learns which of the readings they
  got.
- **The root cause and the test that pins it.**
- **The refutation gate** — what it checked, what it objected to, and what the plan became. Including
  when it objected to nothing.
- **The rounds** — for each: the sha reviewed, findings raised (blocking / non-blocking), implemented,
  declined with their failure-mode sentences, whether the verifier ran and every repair it made.
- **The terminating round's blocking count, as measured**, quoted from the reviewer's JSON. The report
  is not complete without that line or an abort line, and neither may be replaced by a question.
- **The PR**, and that it is marked ready.
- **Anything the ticket asked for that you did not do,** and why. Scope left on the table belongs in
  the report, not in a silence.
- Offer `pr-review <n> --post` or `--stage` once, at the end, if the user wants the review record
  public — offer it, do not wait for an answer.

## Write the run record — always, before you finish

Append a record to `~/.claude/skill-lessons/<UTC-date>-<repo>-<ticket-or-pr>.md` (create the
directory if needed). This is **capture, not derivation**: it records what happened, never a proposed
rule. `skill-retro` turns accumulated records into skill edits, because a lesson needs corroboration
across runs and an adversarial pass before it changes how every future run behaves — neither of which
this run can supply about itself.

**Append means `>>`, never `>` or the Write tool.** The name is keyed on date and ticket, so a
second record for one ticket and day (a second run, or this run's pr-harden record after its
resolve-ticket one) lands on the first. Keep the name — it is how `pool-run` finds your record.

It costs no agent and nothing you do not already hold. Write it even when the run was clean; a record
saying "the gate objected to nothing and no fresh agent found anything the author had missed" is
evidence about the skill working, and its absence would bias every retro toward runs that went badly.

```markdown
# <skill> <version> · <repo> · <ticket/PR> · <UTC date>
outcome: converged | did-not-converge (<reason>) | aborted (<condition>)
rounds: <n>   cycles: <n>   verifier: ran (<verdict>) | skipped (<why>)
context: no compaction | compacted at <step> · peak <n>% at <step> | peak not surfaced
transcript: ~/.claude/projects/<folder>/<uuid>.jsonl

## Refuted by measurement
- <the claim, as it was stated> -> <what the measurement showed> · cost: <rounds/cycles>

## Raised by a fresh agent, missed by the author
- [r<n>] <finding> · blocking|non-blocking · cost: <rounds>

## Where a skill blocked or contradicted this run
- <skill>:<section> — <what happened, and what it cost>

## Declined
- <finding> — <the failure-mode sentence>

## Assumptions review overturned
- <assumption as recorded> -> <what replaced it, and which round>
```

The four middle sections are the ones with signal, and the reason is worth stating: **"refuted by
measurement" and "raised by a fresh agent" are the two categories the run's own author provably
cannot generate**, which is why they are recorded separately from everything else rather than folded
into a summary.

**`context:` and `transcript:` are capture, and the second is what makes the first checkable.** A
run's own sense of how full its window was is a guess made by the thing being measured, so record
only what actually surfaced — a compaction, a context warning, and the step it happened at — and
name the transcript, which carries the ground truth a retro can measure instead. The path needs no
bookkeeping: the uuid is `$CLAUDE_CODE_SESSION_ID`, and the transcript is the one file
`ls ~/.claude/projects/*/"$CLAUDE_CODE_SESSION_ID".jsonl` prints. Take neither name from anywhere
else. `no compaction · peak not surfaced` is the expected reading and is worth writing. Derive
nothing from it here: whether context pressure costs quality is a claim about many runs, and no run
can settle it about itself.

## Anti-patterns

- **Don't stop between phases.** Reporting the plan and waiting, reporting the PR and waiting, asking
  whether to start the loop — each of these ends the run with work owed. The abort list has six
  entries and none of them is "a natural pause".
- **Don't ask what you can assume.** Take the defensible reading, record it in the plan, and put it in
  the report. A question is for the case where proceeding either way would be unsafe or would make the
  work useless if wrong.
- **Don't implement from the ticket title.** The body is often the first draft of the problem and a
  comment is where it was corrected.
- **Don't publish a count you would have to re-measure every round; publish the method.** Each
  recurrence cost a round, because a review agent re-measures what a comment asserts. The recurrence
  stopped only when the enumeration was deleted and replaced with
  *"mutate the line and read the failures"*. Prefer that form. An exhaustive list that is wrong is
  worse than no list, because it invites the next reader to treat the extra failure as a regression
  they caused.

  **And the rule is not about tallies — it is about claims you cannot check.** A universal or an
  exhaustive characterization is the same defect in different grammar, and it slips past a reader
  watching for digits: *any*, *only*, *exactly*, *all*, *never*, *the whole*, *cannot*. So before
  writing one about code you just wrote, spend one attempt trying to falsify it; prefer stating what
  the thing DOES over what it excludes; and name the residue rather than claiming there is none.
- **Don't skip the failing test** because the fix is obvious. Never-executed code is unverified code,
  and a test written after the fix tends to assert what the code does.
- **Don't widen scope.** An adjacent defect you noticed, or an item `/harden` deferred as outside the
  ticket, goes in the report and the PR description rather than into this PR's diff — and not into a
  new issue: file none, as `pr-harden` files none. A PR that does two things gets reviewed as neither.
- **Don't change production to create observability without ruling out a structural pin first.** A
  plan that says "this is behaviour-neutral, so I must change X to make it testable" is one move
  away from making the code worse in the name of rigour — and the move it skipped is a grep of the
  test tree for a guard that reads source or compiled class files. Step 3's question 7, in `refuter.md`, exists for
  this; if you take the trade anyway, label it as one in the plan and in the PR body.
- **Don't spawn a subagent without recording the await.** Every phase of this skill and of the loop
  delegates, and the gate cannot tell a run waiting on an agent from a run that quit unless the
  entry says so.
- **Don't reinvent a `CLAUDE.md` entry point** — `buildPrefixedText`, `cosineSimilarity`,
  `substanceKey`, `findImpliedByDrugName`, `groundedForWire` and the rest. Steps 2–3 are where to
  catch that.

## When NOT to use this skill

- When the ticket needs discussion rather than code. Answer it on the ticket.
- When a PR for the ticket already exists — run `pr-harden` on it.
- When the ticket belongs to another repository. Open that repo and run it there.
- For exploratory work you intend to throw away. The whole pipeline assumes the change is meant to
  land.
