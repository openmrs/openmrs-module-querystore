---
name: resolve-ticket
description: Take a GitHub issue or JIRA ticket URL all the way to a pull request that is ready to merge, in one unattended run — read the ticket with its comments, plan, have the plan refuted by a fresh agent, write the failing test first, implement, prove the build green, harden with context, open a draft PR, then cycle clean-context review rounds until one reports zero blocking findings and mark it ready. Use when handed a ticket or issue URL and asked to deliver a reviewed PR. Trigger phrases include "work this issue", "resolve this ticket", "take this to a PR", "implement and harden issue N", "here's the ticket, deliver a PR".
argument-hint: <issue-url|jira-url|issue-number|jira-key> [--max-rounds N] [--no-verify] [--plan-only]
version: 0.2.3
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
3. The refutation gate's second blocking objection is about **the ticket's own meaning** rather than
   the plan's soundness — no citation can settle what the ticket did not say.
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

## Step 2 — Plan before code

`CLAUDE.md` requires it, and this is where the run is most often lost: the ticket names a symptom and
the plan is where you decide whether you have found the cause. Read the relevant code first — delegate
the *searching* to an `Explore` agent where it is broad ("every call site of X", "which of these
classes touches the wire format") so the conclusions come back instead of the file dumps, but keep the
judgement here. Then write down:

- **What the ticket actually asks for**, in your words, and what it does not. The ticket defines the
  scope; do not widen it because adjacent code looks wrong. Note the adjacent thing and leave it.
- **The root cause**, and how you know. `CLAUDE.md`: root-cause fixes over symptom patches, best
  solution before quickest, and diagnose *why* before proposing a fix.
- **The failing test** that will define the behaviour — which file, what it asserts, and why it fails
  on today's code. It must exercise the real production path with real data: no simulation, no mock,
  no reimplementation of pipeline logic in test code, no calling internal methods with hand-crafted
  inputs, and the composed method rather than a hand-chained pipeline.
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

Record the await — append to the entry's `awaiting` list — before spawning it, and clear that list
when its JSON arrives (see **State** in `pr-harden`, which owns the field and ships the snippet). The gate blocks a yield while the run is mid-flight and this agent runs in
the background, so without the await recorded the run cannot even wait for its own gate.

Spawn it as a new subagent — **never `subagent_type: "fork"`**, which would inherit the reasoning that
produced the plan and defeat the point. Give it the ticket as read (with its comments), the plan
verbatim, and the repo. Do **not** give it your argument for why the plan is right: advocacy primes it
to agree, and agreement is the one thing this agent is not for.

Five questions, and it must say which it actually checked:

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

It returns JSON:

```json
{ "checked": [1, 2, 3, 4, 5],
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

**This is a gate, not a loop, and it does not stop the run.** A blocking objection whose citation
settles it: revise the plan and re-run the gate once. `CLAUDE.md` and a recorded measurement outrank
the plan, so that is a revision, not a debate. Only when the second blocking objection is about **what
the ticket means** rather than whether the plan is sound do you abort (condition 3) — no citation can
settle what the ticket did not say. Non-blocking objections are recorded in the plan and carried into
the report; they do not hold the run.

**`--plan-only` ends here**, and ending means clearing the entry this run wrote — the whole entry for
this repo, not merely its `awaiting` list — so the next turn in this directory is ungated:

```bash
python3 -c "import json,os,pathlib; p=pathlib.Path.home()/'.claude/pr-harden-state.json'; \
s=json.loads(p.read_text()); s.pop(os.getcwd(), None); p.write_text(json.dumps(s, indent=2))"
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

The body says what the ticket asked, what the change does, and how it was verified. It does not grade
the design or tour the alternatives. Record the new PR number in the state entry.

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

## Anti-patterns

- **Don't stop between phases.** Reporting the plan and waiting, reporting the PR and waiting, asking
  whether to start the loop — each of these ends the run with work owed. The abort list has six
  entries and none of them is "a natural pause".
- **Don't ask what you can assume.** Take the defensible reading, record it in the plan, and put it in
  the report. A question is for the case where proceeding either way would be unsafe or would make the
  work useless if wrong.
- **Don't implement from the ticket title.** The body is often the first draft of the problem and a
  comment is where it was corrected.
- **Don't skip the failing test** because the fix is obvious. Never-executed code is unverified code,
  and a test written after the fix tends to assert what the code does.
- **Don't widen scope.** An adjacent defect you noticed goes in the report or a new ticket, not into
  this PR. A PR that does two things gets reviewed as neither.
- **Don't spawn a subagent to write the implementation.** Then nobody holds the writing context, the
  judgement calls get made by an agent nobody can steer, and Step 7's harden loses the one advantage
  it has over the review loop. `Explore` for searching is fine; the judgement stays here.
- **Don't let the refutation gate become a loop,** and don't argue the plan's case to the refuter — an
  agent primed with your reasoning agrees, which is the one outcome that gate cannot use.
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
