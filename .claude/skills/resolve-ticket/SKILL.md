---
name: resolve-ticket
description: Take a GitHub issue or JIRA ticket URL all the way to a pull request that is ready to merge, in one unattended run — read the ticket with its comments, plan, have the plan refuted by a fresh agent, write the failing test first, implement, prove the build green, harden with context, open a draft PR, then cycle clean-context review rounds until one reports zero blocking findings and mark it ready. Use when handed a ticket or issue URL and asked to deliver a reviewed PR. Trigger phrases include "work this issue", "resolve this ticket", "take this to a PR", "implement and harden issue N", "here's the ticket, deliver a PR".
argument-hint: <issue-url|jira-url|issue-number|jira-key> [--max-rounds N] [--no-verify] [--plan-only]
version: 0.13.0
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
gate that was never going to release. The distinction matters because the override is the record of a
deviation: one taken on every partial run means nothing, and the time it records a real deviation
nobody will notice.

**The Stop gate covers the whole run, not just the loop.** Write the state entry at Step 1, before any
work — see **State** in `pr-harden`, which owns the format. From that moment `pr-harden-gate.sh`
refuses to let the turn end until a review round has reported zero blocking findings, or an override
is recorded. That is what makes the run unattended rather than merely intended to be.

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
strip it, do not add `-Dmaven.repo.local` of your own, and do not be surprised that
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
| `github.com/<owner>/<repo>/issues/123` | GitHub issue | `gh issue view 123 --repo <owner>/<repo> --comments` |
| `123`, `#123` | GitHub issue, this repo | `gh issue view 123 --comments` |
| `openmrs.atlassian.net/browse/KEY`, `O3-1234`, `TRUNK-6429` | JIRA | the REST call below |

```bash
curl -s "https://openmrs.atlassian.net/rest/api/2/issue/<KEY>?fields=summary,description,status,comment"
```

That endpoint serves **unauthenticated** (verified: `TRUNK-6429` → 200). The `issues.openmrs.org`
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
always 8080**. **Do not check whether the port is free** — these are throwaway demo instances
(owner's instruction, 2026-08-27), a busy port is the normal state, and the verifier simply takes and
restarts the one it resolved. What would
actually block the run is having no standalone on disk at all, or no LLM endpoint for a module that
needs one. Say so NOW if either is missing, so the user can fix it while the work proceeds. Measured on this skill's fourth run: both standalones were held by
pre-existing processes, discovered at round 2, and the run reached its final round unable to mark the
PR ready for a reason that had nothing to do with the code. Measured on the sixth, the opposite
mistake — both ports busy at pre-flight, both by our own standalones, and neither a blocker.

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

`gate-state` is the only writer of either state file — it holds a lock across both and writes
atomically, which an inline read-modify-write cannot, and under a parallel pool cannot safely be
retyped: measured with 20 concurrent writers, the inline form kept 3 of 20 entries and raised nothing.
`pr-harden`'s **State** section has the rest of the subcommands.

## Step 2 — Plan before code

`CLAUDE.md` requires it, and this is where the run is most often lost: the ticket names a symptom and
the plan is where you decide whether you have found the cause. Read the relevant code first.

Delegate the *searching* only when the question is **broad and of unknown shape** — "where does X
live, across conventions I cannot guess", "every call site of Y". When it is narrow and the target is
named — what does this class expose, where is this global property read — grep it yourself. Measured
on this skill's first run: four targeted greps answered every planning question while a dispatched
`Explore` agent was still working, so the delegation duplicated the work rather than saving context.
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
  This is where a plan quietly gets worse while looking more rigorous. Measured on this skill's
  seventh run: the fix was behaviour-neutral and therefore unobservable, so the plan changed a
  *second* production decision — the order of two branches — purely to make the first one testable.
  Both refutation passes accepted it. Round 1 of the review loop then refuted it in one move: the
  reordering was not required by the ticket, and it ADDED exposure to the very defect the ticket
  exists to remove, because in the old order an inconsistent state was harmless and in the new one it
  reached a clinician-facing chip. State the trade explicitly, and rule out "test it differently"
  before taking it — question 7 below is that check.
- **Which API-surface rules in `CLAUDE.md` this touches.** That file is a list of entry points that
  must not be bypassed and of changes that were measured and rejected. If the plan reinvents one of
  them, the plan is wrong.
- **Every assumption you took** on an ambiguous reading of the ticket. Take the defensible reading and
  record it here; do not stop to ask. This list goes into the final report verbatim, which is what
  makes an unattended run auditable rather than merely finished.

`--plan-only` runs on through Step 3 and stops there — its point is a plan that has survived
refutation, not a first draft.

## Step 3 — Refute the plan, before any code

One fresh subagent, one pass, read-only. Its **only** job is to try to break the plan. This is the
cheapest gate in the pipeline and it guards the failure this module is most prone to: `CLAUDE.md` is
largely a catalogue of changes that looked obviously right and measured wrong — a uniform ATC veto,
re-ranking by longest alias, identity keyed on `rxcui`, tightening `hasAllergyToken`. Catching one at
plan time costs one agent and no code. Catching it in round 3 costs three rounds of implementation
plus the rounds spent polishing the wrong fix.

Snapshot the worktree hash before spawning and compare it after — the refutation gate is read-only by
instruction, but "read-only by instruction" is not a guarantee, and `pr-harden`'s **State** section
carries the measurement of what an agent that dies mid-mutation leaves behind. Tell it to restore
anything it changed **before** it reports.

**Snapshot `git branch --show-current` beside the hash, at every delegation in this skill.** A diff
hash cannot see a `git checkout`: both trees are clean, so the hash matches and the switch is
invisible. Measured on the run that added this line — a review agent left the worktree on `main`, and
only the NEXT agent's own branch check caught it before edits landed there. A wrong-tree edit is
recoverable exactly until something commits on top of it.

Record the await — append to the entry's `awaiting` list — before spawning it, and clear that list
on ANY terminal outcome: a result, or the harness reporting the agent failed, stalled or was killed.
A death leaves a fresh await that the gate honours for the full hour, which is a licence to stop the
run with nothing running. Tell the refuter **not to spawn subagents of its own** — nested delegation
killed an agent on this skill's first run — and if it dies, retry twice with something changed between
attempts before taking the labelled deviation (`pr-harden`'s **State** section carries the contract).

The field and its snippet live in `pr-harden`'s **State** section. The gate blocks a yield while the
run is mid-flight and this agent runs in the background, so without the await recorded the run cannot
even wait for its own gate.

**When the run is unattended, do not yield at all — collect the agent inside the same turn.** A
`claude -p` process exits when its turn ends, so there is no next turn to be re-invoked into, and the
recorded await then licenses the gate to let the run die quietly. Measured 2026-08-26: that is exactly
how #297 ended at this step, having dispatched this very refuter, and how #310 ended in `/harden` —
both with committed work and no PR. `pr-harden`'s **State** section carries the measurement.

Spawn it as a new subagent — **never `subagent_type: "fork"`**, which would inherit the reasoning that
produced the plan and defeat the point. Give it the ticket as read (with its comments), the plan
verbatim, and the repo. Do **not** give it your argument for why the plan is right: advocacy primes it
to agree, and agreement is the one thing this agent is not for.

Seven questions, and it must say which it actually checked:

1. **Does the plan bypass or reimplement a documented entry point?** `CLAUDE.md`'s API-surface rules
   name the only correct callers for their operations. Name the method and the rule.
2. **Does it re-propose something recorded as measured and rejected?** Quote the measurement.
3. **Does the root-cause claim hold,** or is this a symptom patch with the real cause one layer down?
   Is there a cheaper or deeper locus for the same fix?
4. **Does the planned test pin the behaviour?** Real production path, real data, composed method
   rather than hand-chained steps — and would it fail *today* for the predicted reason? A test that
   would pass on the pre-change code proves nothing.
5. **Does the scope match the ticket** — neither wider (an adjacent defect smuggled in) nor narrower
   (part of the ask quietly dropped)?
6. **Does the plan rest on a claim about the DATA that nobody has measured?** Name the claim, and name
   what would measure it. This is the question the others cannot reach: they test the plan against
   the repo's recorded decisions, and a premise about the *dataset* can be unrecorded and still false.
   Measured on this skill's fourth run, against #292: a plan whose whole gate was a name-identity test
   (`DrugReference.isNamed`, the accessor `CLAUDE.md` itself names for that question) survived TWO gate
   passes and a full `/harden` cycle before a review agent measured it — the `ddinter` parser writes
   each entry's aliases from its name AND its `rxnorm_name`, and the shipped KB has a row named
   `Omeprazole` carrying `rxnorm_name: esomeprazole`, so the test was true of exactly the pair the gate
   was written to refuse. Two cycles of implementing, documenting and testing the wrong predicate. The
   tell is a plan that says "X names Y" or "X and Y are the same substance" and cites a method rather
   than a count: ask for the count.

7. **If the plan says something CANNOT be tested, has this repo pinned an untestable rule before, and
   how?** Ask it whenever the plan reaches for a production change to create observability, or says a
   behaviour is unobservable, or calls a rule "conventional" / "enforced by javadoc only". The answer
   is very often yes and the plan has not looked: a repo that has met this problem already has a
   *structural* pin somewhere — a test that reads its own source or compiled class files, an
   architecture guard, a build-time assertion — and finding it is strictly better than bending the
   design to become behaviourally observable. Measured on this skill's seventh run: the plan concluded
   "the write path alone is unobservable, so the branch order must change to give it coverage", and
   both gate passes accepted that. The repo already pinned a behaviour-neutral rule structurally, in a
   test `CLAUDE.md` itself cites approvingly for exactly that reason. Round 1 of the review loop found
   it, and the redesign that followed was better on every axis — the trade the plan had accepted
   disappeared, and a residue the plan had recorded as unclosable was closed. Two rounds spent
   implementing, documenting and then reverting the wrong design. The tell is a plan whose
   justification for touching production is "otherwise we cannot test it": grep the test tree for a
   guard that reads source or `.class` files before believing it.

It returns JSON:

```json
{ "checked": [1, 2, 3, 4, 5, 6, 7],
  "objections": [
    { "question": 2, "blocking": true, "objection": "…",
      "citation": "CLAUDE.md, the ATC-subgroup bullet: a uniform veto loses real signal 2.4x faster than it removes false claims" } ] }
```

**An objection without a citation is not an objection.** It must point at a `CLAUDE.md` rule, a
specific line of code, or a recorded measurement — same discipline as the reviewer's failure-mode
sentence, and for the same reason: an agent told to find problems will manufacture them, and a
manufactured objection at plan time sends the run down a worse path than the one it replaced. That
risk is sharper here than in the review loop, because there is no code yet to check the objection
against. A plan it cannot fault gets an explicit empty `objections` list, and the `checked` array is
what stops silence being mistaken for coverage.

**This is a gate, not a loop.** A blocking objection whose citation settles it: revise the plan and
re-run the gate **once**. `CLAUDE.md` and a recorded measurement outrank the plan, so that is a
revision, not a debate. Non-blocking objections are recorded in the plan and carried into the report;
they do not hold the run.

**Revise by deleting, not by re-wording.** The revision is itself unverified prose written fast under
the pressure of an objection, and it is a live source of the next false claim: measured, gate pass 2's
blocking objection was against a claim the pass-1 REVISION had introduced, and the `/harden` run later
in that same session caught four more of the shape, twice in a correction from the round before. So
when an objection lands on a claim, cut the unsupported clause rather than replacing it with a
better-sounding one, and re-derive any figure you carry across rather than restating it. `harden` and
`pr-harden` both carry this rule; it belongs here too, because Step 3 is where the first rewrite
happens.

After that one re-run there are **three** outcomes, and the discriminator is not how many objections
have been raised but **whether the objection's citation determines the answer**:

1. **No blocking objection.** The plan stands. Proceed.
2. **A blocking objection that SETTLES the question** — its citation names the answer, including when
   the answer is "the previous revision was right". Apply it and proceed. This is convergence, not
   iteration, so there is **no third gate pass**: the objection did not open a question, it closed
   one, and re-gating a plan the gate has just told you the shape of is the loop this section forbids.
3. **A blocking objection that leaves the question OPEN** — two defensible readings and no citation
   deciding between them. That is abort condition 3: hand back with both readings, do not pick one.

The distinction is the same one that governs objections in the first place. "An objection without a
citation is not an objection"; by the same rule, an objection whose citation *determines* the answer
is a resolution, and one that merely disputes the plan without deciding it is a deadlock. Count
citations that decide, not objections raised.

Measured on the first real run of this skill, against issue #285: pass 1 refuted the plan's stated
reason and left its conclusion intact; the revision reversed the conclusion; pass 2 refuted **that**,
citing three existing tests, and in doing so named the answer — the original conclusion, on new
grounds. Two blocking objections, no deadlock, and a third pass would have re-gated a settled
question. Four false justifications died before any code existed, one of them on its way into a PR
body.

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

**Check `git branch --show-current` before you edit, not only before you commit.** Every phase of this
pipeline delegates, and an agent that runs `git checkout` silently redirects everything after it. On
this skill's fourth run an agent left the worktree on `main` and four edits landed there; it surfaced
only because the test count dropped by exactly the size of the new test file, and had those been code
edits with a commit after them they would have gone to `main`. `pr-harden` states this rule for
committing; committing is too late, because by then the edit is already in the wrong tree.

**Edits made by script need three guards, because all three failures are silent.** You will edit by
running short scripts rather than by hand; measured on this skill's third run, each of these cost a
cycle or a review round. `str.replace` returns the string unchanged when it matches nothing and the
script prints success anyway — so **assert the target text is present before replacing**, and let the
assert stop the script rather than falling through to the next edit. A replacement bounded by
"from here to the next method" can span further than you meant — so after any multi-line edit, **count
what should still be there** (test methods, symbols) against what you expected; one such slice deleted a
whole test method and everything still compiled. And **verify by reading the file back**, because the
script's own report is not evidence: the other two both announced success.

## Step 6 — Green

`mvn -o clean install` from the **repository root**. Not `-pl api`, not `-pl omod`: the omod unpacks
the *resolved* api artifact over `omod/target/classes` at generate-resources, so a `~/.m2` jar from
another branch shadows the reactor's classes and reddens tests on a drift that is not in the source.

## Step 7 — Harden with context, once

Run `/harden` here, before the PR exists. This is the one place its passes are the right tool: they
run in the context that wrote the code, which makes them good at polish and at the boundaries you were
just thinking about — trace outward, the invariants in unchanged neighbours your edit may have
falsified, the test named for each behaviour change. Let it converge on its own terms (a cycle that
changes nothing) and let its own Stop gate do its job.

Do not skip it on the grounds that the loop will review anyway. The two are not substitutes: polish
with context first, adversarial review without it second. Skipping this hands the first clean reviewer
a pile of nits and spends a whole round on them.

**While harden runs, write its awaits to BOTH state files.** The gate armed at Step 1 is
`pr-harden-gate.sh`, which reads `~/.claude/pr-harden-state.json`; harden's own awaits are written to
`~/.claude/harden-state.json`. So a harden cycle blocked on its Phase 2 agents is invisible to the
armed gate, which then refuses the yield the cycle needs in order to wait — the same shape #298
measured in the un-nested case at "two ten-minute in-turn wait loops", and it fired again on the #302
run with four agents live. That is what `gate-state`'s default scope is for — **omit `--only` and one
command writes both**, so the pair cannot come apart the way two commands could:

```bash
~/.claude/pipeline/gate-state --owner $PPID await "harden phase 2"
~/.claude/pipeline/gate-state --owner $PPID clear-await
```

Clear it in both **at the end of this step, and when a harden cycle dies or takes its labelled
override** — a fresh await left in `pr-harden-state.json` licenses a real quit for up to the gate's
hour-long TTL while Step 8 runs.

**Then confirm `harden` left its own state entry finished**, because two Stop gates are now live in
this run and both must allow the turn to end. `~/.claude/harden-state.json` must say `edits: 0` for
this repo, or `override: true` if it took the labelled override. A `harden` run that was interrupted
leaves `edits > 0` there, and that entry then blocks the end of *this* run even after the review loop
has converged — a wedge with nothing wrong with the PR, cleared only by the 6-hour expiry. Check it
here, where it is one line, rather than discovering it after the loop.

## Step 8 — Draft PR

Commit and push, then open the PR **as a draft** — it is about to take several rounds of commits, and
a draft says that honestly to anyone watching the repo.

Match the repo's title voice, which is distinctive here: `type(scope): ` followed by a lowercase
sentence stating **the behaviour after the fix**, not the task performed — *"a long answer is no longer
cut off by a proxy that has read nothing yet"*, not *"add SSE keep-alive"*. Read
`gh pr list --state all --limit 12 --json title` and match what you see.

Link the ticket so it is machine-readable, because every round's reviewer resolves it:

- **GitHub issue** — `Fixes #123` in the body, which populates `closingIssuesReferences`.
- **JIRA** — no auto-close exists, so put the key in the **title** and the browse URL in the body.
  `pr-review` and `pr-harden` both look for a key in the title or branch name.

**`Fixes` only if the PR actually closes the ticket. Otherwise `Refs`, and say why in the body.**
GitHub acts on the keyword, so a PR that delivers something SHORT of the ticket — an instrument for a
defect it does not fix, one part of a multi-part ask, a diagnosis the ticket asked for before a
remedy — silently closes an open defect on merge. Measured: a run whose PR body said `Fixes #299` in
its first line and, four paragraphs down and in bold, "this PR should not be read as closing #299".
That contradiction was round 1's blocking finding, and it fails closed and quietly — nothing errors,
no check reddens, and the next person looking for open defects does not see it.

The cost of `Refs` is that `closingIssuesReferences` comes back **empty** for that ticket — unless a
closing keyword elsewhere in the body reaches it anyway — and `pr-review` Step 1 resolves the ticket
from exactly that field. So when you use it, **name the issue number explicitly
in every reviewer brief** rather than leaving the reviewer to find it — otherwise the round that is
supposed to ask "does this resolve the ticket?" never reads the ticket at all.

**Check the field rather than the wording, with `gh pr view <n> --json closingIssuesReferences`, once
the body is written and again after any later edit to it.** That field has named an issue the PR does
not close on two runs. The cause was the same both times — a closing keyword whose scope reached an
adjacent reference — but the remedy was not, which is the argument for checking the field instead of
learning a rule about the prose: on #250 rewording the offending sentence was enough, and on #317
rewording changed nothing while separating the two references onto their own lines and naming the
non-closing one without a `#` did. Both runs caught it themselves, so this makes a practice that has
already worked twice repeatable; what it guards against is the run where nobody looks, because merging
then closes an open defect and nothing reddens.

The body says what the ticket asked, what the change does, and how it was verified. It does not grade
the design or tour the alternatives.

**Write it once here, and RE-DERIVE IT WHOLE before the PR is marked ready — never patch it across
rounds.** The body describes code the review loop is about to change under it, so an incremental edit
is how it comes to assert something false. Measured on this skill's fourth run: a round-1 edit made the
body say the ticket's second named shape was not fixed, round 2's fix made it fixed, round 3's only
blocking finding was that sentence — and rounds 4, 5 and 6 each caught another stale claim in the same
paragraph set ("nothing outside a folded chip is touched", two mutation counts, "the three sites that
quoted it"). Four consecutive rounds whose top finding was the description. So: patch it mid-loop ONLY
to satisfy a blocking finding, and at the end rewrite it against the final head, re-measuring every
figure in it at that point rather than carrying one forward.

**Treat the body as part of the change, not as a summary of it.** It is the durable public rationale
attached to the closing of the ticket, no test can fail on a false sentence in it, and a repo-wide grep
for a claim you later correct will never reach it. Two consequences, both measured on this skill's third
run. Every figure in it carries the dataset it was measured over — a chip count taken against a
four-entry fixture is not a claim about the shipped knowledge base, and stating it without its base is
how the same sentence became a blocking finding twice. And when a later round corrects a claim anywhere
in the repo, **re-read the body for the same claim**: round 2 of that run found its only blocking
finding here, the sixth home of something already fixed in five files, still standing because the
orchestrator had edited the body for an unrelated reason without re-reading the paragraph above. Record the new PR number in the state entry.

## Step 9 — Run the loop, here, now

Invoke `pr-harden <the new PR number>` with this run's `--max-rounds` and `--no-verify`, and let it run
to its own termination. **Do not report the PR and stop.** The user asked for a pull request with no
blocking comment; a draft PR nobody has reviewed is not that, and handing back here is the disguised
early stop the autonomy contract forbids.

`pr-harden` owns everything from round 1: fresh reviewer, fresh fixer, the declined ledger, the
verifier, the round cap, and marking the PR ready when a round reports zero blocking findings. Do not
review the PR yourself while it runs, and do not pre-empt round 1 by fixing what you suspect it will
find — you hold the writing context, which is exactly the disqualification the loop is built around.
Anything you can already see belongs in Step 7, before the PR existed.

When it converges, `pr-harden` verifies the merging head if no round already did — a runtime-visible
change is not ready until something has run it, and the loop's per-round verifier sits on the fix
path, which the exit path skips. Then the PR is marked ready (`gh pr ready`) and the run is done. A
head that cannot be verified ends the run as converged-but-unverified, not as ready.

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
- Nothing was posted to GitHub but the commits. Offer `pr-review <n> --post` or `--stage` once, at the
  end, if the user wants the review record public — offer it, do not wait for an answer.

## Write the run record — always, before you finish

Append a record to `~/.claude/skill-lessons/<UTC-date>-<repo>-<ticket-or-pr>.md` (create the
directory if needed). This is **capture, not derivation**: it records what happened, never a proposed
rule. `skill-retro` turns accumulated records into skill edits, because a lesson needs corroboration
across runs and an adversarial pass before it changes how every future run behaves — neither of which
this run can supply about itself.

It costs no agent and nothing you do not already hold. Write it even when the run was clean; a record
saying "the gate objected to nothing and no fresh agent found anything the author had missed" is
evidence about the skill working, and its absence would bias every retro toward runs that went badly.

```markdown
# <skill> <version> · <repo> · <ticket/PR> · <UTC date>
outcome: converged | did-not-converge (<reason>) | aborted (<condition>)
rounds: <n>   cycles: <n>   verifier: ran (<verdict>) | skipped (<why>)
context: no compaction | compacted at <step> · peak <n>% at <step> | peak not surfaced
transcript: ~/.claude/projects/<cwd-slug>/<session-uuid>.jsonl

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
only what actually surfaced — a compaction, a context warning, and the step it happened at — and name
the transcript, which carries the ground truth a retro can measure instead. The path needs no
bookkeeping: the session uuid is the directory name in this run's scratchpad path, and the transcript
is `~/.claude/projects/<cwd-slug>/<uuid>.jsonl`. `no compaction · peak not surfaced` is the expected
reading and is worth writing for the same reason a clean run still gets a record — a field filled in
only when something went wrong biases every retro that reads it, in the direction of the runs that
went badly. Derive nothing from it here: whether context pressure costs quality is a claim about many
runs, and no run can settle it about itself.

## Anti-patterns

- **Don't stop between phases.** Reporting the plan and waiting, reporting the PR and waiting, asking
  whether to start the loop — each of these ends the run with work owed. The abort list has six
  entries and none of them is "a natural pause".
- **Don't ask what you can assume.** Take the defensible reading, record it in the plan, and put it in
  the report. A question is for the case where proceeding either way would be unsafe or would make the
  work useless if wrong.
- **Don't implement from the ticket title.** The body is often the first draft of the problem and a
  comment is where it was corrected.
- **Don't publish a count you would have to re-measure every round; publish the method.** Measured on
  this skill's fourth run, every figure that named a tally went stale, several of them twice —
  "1342 tests", "negating it reddens exactly two", "the three sites that quoted it", "eleven cases" —
  and each recurrence cost a round, because a review agent re-measures what a comment asserts. Two of
  them went stale in the very round that added the thing they had miscounted. The recurrence stopped
  only when the enumeration was deleted and replaced with *"mutate the line and read the failures"*.
  Prefer that form. An exhaustive list that is wrong is worse than no list, because it invites the next
  reader to treat the extra failure as a regression they caused.

  **And the rule is not about tallies — it is about claims you cannot check.** A universal or an exhaustive characterization is the same defect in different grammar, and it slips past a reader watching for digits: *any*, *only*, *exactly*, *all*, *never*, *the whole*, *cannot*. Measured on the seventh run, five such claims in three consecutive cycles, each written to correct the previous cycle's false claim and each false in turn — "any looser pattern would reject" (looseness has more than one dimension), "it only re-admits `M01AE0`" (it re-admits any single trailing digit), "matched only the 5- and 7-character shapes" (the old pattern matched 6 too), "exactly the two levels the ladder is known to be handed" (nothing on the path validates a code's shape), and one that mis-numbered the very level it was excluding. So before writing one about code you just wrote, spend one attempt trying to falsify it; prefer stating what the thing DOES over what it excludes; and name the residue rather than claiming there is none.
- **Don't skip the failing test** because the fix is obvious. Never-executed code is unverified code,
  and a test written after the fix tends to assert what the code does.
- **Don't widen scope.** An adjacent defect you noticed goes in the report or a new ticket, not into
  this PR. A PR that does two things gets reviewed as neither.
- **Don't spawn a subagent to write the implementation.** Then nobody holds the writing context, the
  judgement calls get made by an agent nobody can steer, and Step 7's harden loses the one advantage
  it has over the review loop. `Explore` for searching is fine; the judgement stays here.
- **Don't change production to create observability without ruling out a structural pin first.**
  A plan that says "this is behaviour-neutral, so I must change X to make it testable" is one move away
  from making the code worse in the name of rigour — and the move it skipped is a grep of the test tree
  for a guard that reads source or compiled class files. Measured on the seventh run: a second
  production decision was changed purely for coverage, both gate passes accepted it, and round 1 of the
  loop showed it added exposure to the defect the ticket existed to remove while buying coverage that
  was available another way. Step 3's question 7 exists for this; if you take the trade anyway, label
  it as one in the plan and in the PR body.
- **Don't let the refutation gate become a loop.** The loop is a *third gate pass*, not a second
  revision: two blocking objections are fine when the second one settles the question, and a third
  pass re-gates something already decided. Step 3's three outcomes are the rule. And don't argue the
  plan's case to the refuter — an agent primed with your reasoning agrees, which is the one outcome
  that gate cannot use.
- **Don't write `Fixes` on a PR that does not fix the ticket.** It is the default this skill hands
  you in Step 8 and it is wrong whenever the delivery falls short of the ask, which is exactly the
  case a careful run produces — an instrument, a diagnosis, one part of several. The merge then closes
  an open defect and nothing anywhere says so. See Step 8; and having switched to `Refs`, hand every
  reviewer the issue number by hand, because the field they resolve it from is now empty.
- **Don't review your own PR after Step 8,** and don't pre-empt round 1.
- **Don't spawn a subagent without recording the await.** Every phase of this skill and of the loop
  delegates, and the gate cannot tell a run waiting on an agent from a run that quit unless the
  entry says so.
- **Don't leave the state entry behind on an abort.** Record the override and its reason, or the next
  turn in this repo is blocked until the 6-hour expiry.
- **Don't reinvent a `CLAUDE.md` entry point** — `buildPrefixedText`, `cosineSimilarity`,
  `substanceKey`, `findImpliedByDrugName`, `groundedForWire` and the rest exist because a second
  implementation of each has already gone wrong once. Steps 2–3 are where to catch that.

## When NOT to use this skill

- When the ticket needs discussion rather than code. Answer it on the ticket.
- When a PR for the ticket already exists — run `pr-harden` on it.
- When the ticket belongs to another repository. Open that repo and run it there.
- For exploratory work you intend to throw away. The whole pipeline assumes the change is meant to
  land.
