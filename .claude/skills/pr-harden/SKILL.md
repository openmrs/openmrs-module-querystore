---
name: pr-harden
description: Harden an open pull request by cycling clean-context review rounds against it — a fresh agent reviews the pushed head, a second fresh agent implements every finding it agrees with and declines the rest on the record, the build is proved green, the change is verified on a real standalone where runtime behaviour is at stake, and the round is committed and pushed. The cycle repeats until the sha being handed over has been reviewed with zero blocking findings. Use when a PR should be hardened by reviewers who have never seen it being written. Trigger phrases include "harden this PR", "review and fix the PR until it's clean", "cycle review rounds on PR N".
argument-hint: <pr-number-or-url> [--max-rounds N] [--no-verify]
version: 0.38.1
---

# PR harden — clean-context review rounds until nothing blocks

Arguments: `$ARGUMENTS` — a PR number or URL (if omitted, run `gh pr list` and ask which one).
`--max-rounds N` overrides the default cap of 4. `--no-verify` skips the standalone verifier for the
whole run; use it only when you already know no round can touch runtime behaviour, and say so in the
report.

This skill is the **loop**. It runs against an open PR, whether you opened it by hand or
`resolve-ticket` opened it from a ticket — in the second case that skill hands off here and this one
owns everything from round 1 on.

The problem this solves is not that PRs go unreviewed. It is that the agent that wrote the code is
the worst possible reviewer of it, and knows too much to be surprised by it. So every review in this
loop is done by an agent that has never seen the code being written, and the loop's exit condition
is owned by that agent and nothing else.

The incidents and measurements these rules rest on are in [evidence.md](evidence.md), under the same
headings, so where another skill or a gate says a section here "carries the measurement", that section
of evidence.md does. A run does not need to read it. Before changing or deleting a rule, read its entry
there.

## Roles — and the one rule that makes them roles

Four participants. Every one of them except the orchestrator is a **new subagent, spawned fresh for
that round**.

| role | context | owns |
|---|---|---|
| orchestrator | this session | the loop, the state file, the ledger, the report |
| reviewer | fresh, per round | the findings, and whether each one **blocks** |
| fixer | fresh, per round | what gets implemented, and what gets declined and why |
| verifier | fresh, when needed | what the running server actually does, and the environment |

> **Never spawn any of them with `subagent_type: "fork"`.** A fork inherits this conversation, which
> is the one thing the loop exists to prevent. Any other subagent type starts clean. It still reads
> `CLAUDE.md` and the repo's skills — that is intended; what it must not have is the transcript of
> the code being argued for.

> **And never pass `model`.** A `PreToolUse` hook (`~/.claude/hooks/no-subagent-model-override.sh`)
> refuses such a call, so this is enforced rather than asked; if a different model is genuinely
> wanted, that is the user's call, not a lever to reach for mid-round.
>
> **What that hook does NOT establish is that every subagent runs on the session model.** The scope
> lives once, in the hook's own header beside the code — read it there before trusting the property,
> rather than trusting this sentence.

Reviewer and fixer are always **different agents in the same round**. One agent doing both grades its
own homework, which is the failure this whole design removes.

## Step 0 — Guards, before any round

Refuse the run, with the reason, if any of these fails:

- `gh pr view <n> --json state,headRefName,headRepositoryOwner,maintainerCanModify,isCrossRepository`
  — the loop **commits and pushes**, so a cross-repository PR is only viable when
  `maintainerCanModify` is true. Otherwise stop: no amount of rounds helps if the fix cannot land.
- The local worktree is clean (`git status --porcelain` empty) and checked out on the PR's head
  branch, tracking the remote. Uncommitted work of yours would be swept into a round's commit.
- `gh auth status` succeeds.
- Note whether the PR is a **draft**. A run entered from `resolve-ticket` opens it as one, because it
  is about to take N rounds of commits; on convergence, mark it ready (`gh pr ready <n>`). A PR that
  was already ready stays ready — never move it back to draft.
- An entry for this repo in `~/.claude/pr-harden-state.json` is **this run's own** when its `pr`
  matches the PR being hardened, or when it has no `pr` yet — that is the handoff `resolve-ticket`
  writes, and you adopt it and carry its `round`, `declined` and `reviewed_shas` forward. An entry
  naming a *different* PR is a stale run: report it and ask before clearing it — **but an unattended
  run has nobody to ask**, which is settled for the verifier at step 6, in `verifier.md`, and settles the same way here.
  The gate already draws the line the ask stood in for: past `STALE_AFTER` (6h) it treats a run as
  abandoned rather than in flight. So take over an entry past that bound, or one whose run recorded a
  terminus (`phase: reviewed` with `blocking: 0`, or `override: true`), and say in the report which
  PR's entry you cleared and what it said; refuse a fresher entry claiming a live round on another PR
  rather than adopting it, since two runs in one checkout is what the ask was preventing.
  A reused worktree inherits the previous PR's ledger. `pr-set` drops `reviewed_shas`,
  `verified_shas` and `declined` when the PR number changes and prints the prior number and what it
  dropped. That print is a backstop, not this step's report — when you take over or clear an entry
  naming another PR, read it with `gate-state clear --only pr --json`, which prints the whole entry
  it removed, and say in the report which PR's it was and what it said.
- **A PR with rounds from an earlier run can arrive with no entry to adopt.** `pool-run` clears the
  entry when it makes a worktree, so a PR worked again in a worktree it has just made starts with
  none. Count its earlier rounds, from its description and the run records whose header names this
  PR, toward *1 — REVIEW*'s round 4; the cap stays this run's budget.

Then write the opening state entry (`phase: "init"`, and `round: 1` unless you adopted an entry, whose
`round` you keep) — see **State**. From this point the
Stop gate will not let the turn end until the head being handed over has been reviewed with zero
blocking findings — and verified, where any verifier ran — or the override is taken.

## The round

```
1  REVIEW    fresh subagent · pushed head · declined ledger · last verifier report
             BLOCKING-ONLY from the PR's round 4, and after a clean full round
2  RECORD    the reviewer's blocking count → state          {phase: "reviewed"}
3  exit?     blocking == 0 → step 7 · a clean FULL round's non-blocking
             findings go to step 4 first, and every round after is BLOCKING-ONLY
4  FIX       fresh subagent · implements what it agrees with, declines the rest
             on the record                                   {phase: "fixing"}
5  GREEN     mvn -o clean install, from the ROOT
6  VERIFY?   runtime-visible change → fresh verifier on the standalone
   COMMIT    one commit, push · round++ → step 1
7  FINISH    do NOT edit the cleared sha · file NO issue · re-derive the body ·
             VERIFY the merging head if nothing has · mark ready.
             An edit here owes a BLOCKING-ONLY round
```

### 1 — REVIEW

**From the PR's round 4 on, counting rounds earlier runs made (Step 0), the round is BLOCKING-ONLY**
— and so is every round after a full round that found nothing blocking (step 3). Otherwise the PR's
rounds 1 to 3 are full rounds: the reviewer reports everything and the fixer implements the
non-blocking findings too, which is how polish happens.
A blocking-only reviewer's findings are its blockers; anything else it notices it returns as `notes`,
which the orchestrator copies into the run record's *Raised by a fresh agent*, marked unfixed, and
FINISH names in the PR description — never to a fixer, never to an issue. A finding the
orchestrator downgrades in such a round is a note too.

This is FINISH's own rule — *"a round that implements nits and then re-reviews can never
converge, because a review is expected to produce nits"* — applied before FINISH rather than
only at it.

It does not grade the findings. A blocking finding is still whatever the reviewer says it is and
the bar is unchanged; what this bounds is the surface the FIXER is asked to touch, which is what
generates the next round's review. **Not licence to raise the cap instead** — a blocking-only
round is cheaper, not free.

Spawn a fresh reviewer and have it run the repo's `pr-review` skill on the PR — Steps 1 through 3 in
full: read the issue the PR claims to close and not only the PR, ask whether this is the right fix
and the best one available, verify rather than read, and run its adversarial refutation pass where it
is worth it. Nothing is posted to GitHub. `pr-review`'s default is already consent-gated, so pass
neither `--post` nor `--stage`, and tell the reviewer explicitly that its output goes to a machine,
not to the PR.

Record the await before spawning the reviewer, and clear it when its JSON arrives — see **State**.
Snapshot the worktree hash first, and tell the reviewer to restore any mutation **before** it reports.

**Tell it not to spawn subagents of its own.** `pr-review` Step 3 asks for an adversarial refutation
pass, which reads as an instruction to delegate. The independence is already supplied one level up —
the reviewer IS the independent agent — so a second layer buys a failure mode and nothing else. Have
it argue both sides in its own reasoning, or mark the finding non-blocking.

Fetch and review the **pushed** head, not the local worktree:
`git fetch origin 'pull/<n>/head:pr-<n>-r<round>'`. Record the sha.

**A brief names that SHA, and only an agent with its OWN checkout is told to check it out.** The
round's ref name is a shared resource, and a `git checkout` by an agent that has no checkout of its
own lands on the tree this run commits from. So either isolate it, or brief that the worktree is
already at the head and nothing is to be checked out.

**Compare that sha against the last entry of `reviewed_shas` before you spawn anything.** If the new
head EQUALS the previous round's, the loop is about to spend a round re-reviewing bytes it has
already reviewed — and a reviewer given identical
input will either repeat its findings, which reads as an unfixed defect, or find nothing, which reads
as convergence. Both are wrong and neither looks wrong. So stop and establish why before spawning:
the round pushed no commit (the fixer declined everything — that is a *did not converge*, see
**Termination**, not a free round), or the push had not landed when the fetch ran, or the ref was
created outside a round at all.

**And where this run pushed the head, compare it against the sha it pushed** (`git rev-parse HEAD`),
which also covers a first round and a lag of more than one commit. Where they differ, re-fetch in a
bounded until-loop, and spawn nothing on the lagging sha.

**Tell the reviewer what to diff against, and never let it be a local branch name.** Fetch the base
too and name it explicitly: `git fetch origin main` then `git diff origin/main...pr-<n>-r<round>` — or
better, the PR's own base from `gh pr view <n> --json baseRefName`. A local `main` is stale on any
machine that has not pulled, and the merge base then reaches back to whenever it last did.

**Compare the base you just fetched against the one the previous round saw, and where it moved, re-check
what this branch says about the code the move touched.** Three classes, and git flags none of them.

The first is **an identifier this branch allocated from a sequence `main` also appends to.** An ADR
decision number is the observed instance: the branch takes the next free one when it writes the
entry, and an upstream PR merged since can have taken the same one. When it has moved, correct every
home of the old value and not just the one you noticed — they sit in javadoc and test names, not
only in the ADR file — and search for the number itself rather than for a phrasing you wrote. **And
the sequence's own INDEX is a home no guard resolves** — an ADR's table of contents. A merge kept
only one of two entries added at the same insertion point, with nothing in the build failing — so
after a merge, check the index for BOTH numbers. **And the index line is owed by the commit that
ADDS an entry, not only by the merge that meets one**.

The second is **a count or a structural claim that git merges cleanly and silently falsifies.** So
grep this branch's own claims about the structures `main` changed, and RE-MEASURE each on the merged
tree rather than re-reading it for coherence; a coherent sentence about a structure that moved is
the failure mode, not the check.

The third is **a shared BUDGET the base consumed.** A repo size guard is scored on the merged file, so
a change that fitted before the merge does not after. The overflow is loud and the loss is not:
nothing in the build says a directive went missing. Trim your OWN added prose, or move the rule to
the code it binds. Where a guard's javadoc forbids raising the budget in the commit that overflowed
it, raising it is not the move on its own.

What the reviewer is given, and nothing more:

- the PR, its diff, and **the ticket it claims to resolve** — read with its comments, not just its
  title. A GitHub issue via `gh issue view <m> --json title,body,comments` — never
  `--comments`, which drops the body off a terminal, see `resolve-ticket` Step 1; a JIRA key (`O3-1234`, `TRUNK-6429`,
  carried in the PR title or branch name) via
  `https://openmrs.atlassian.net/rest/api/2/issue/<KEY>?fields=summary,description,status,comment`,
  which serves unauthenticated. The `issues.openmrs.org` link people paste redirects to a dashboard
  and will not serve REST.
- **the declined ledger** — every finding earlier rounds did not implement, with the reason. Frame it
  exactly as `pr-review` Step 1 frames prior review threads: do not re-raise what is settled. Do
  **not** tell it what was implemented, and do **not** reassure it that any area is closed — that
  suppression is the thing harden's "re-derive the merged result from scratch" warns about, and an
  insufficient fix must be free to be re-raised on the reviewer's own initiative.
- **the last verifier report**, if one exists, as fact it may use and need not own

Its two standing rules — re-deriving the merged result from round 2 on, and attacking a guard whose
subject is TEXT or SHAPE — the JSON it returns and what makes a finding blocking are in `reviewer.md`
in this skill's directory. Brief it with that file's absolute path and tell it to read the whole file
before it does anything else; the brief itself names the round, the head and the base to diff
against, says whether the round is BLOCKING-ONLY and whether the run started from a ticket, and
hands it what the list above names. Step 2 downgrades a finding that fails that file's
*`blocking: true` requires a non-empty `failure_mode` and `evidence`*, and records that it did.

### 2 — RECORD

The orchestrator writes the blocking count to state **from the reviewer's JSON**. Not from the
fixer's reading of it, not from your own judgement of which findings are serious. The exit condition
belongs to the agent whose work is not being judged.

### 3 — Exit test

`blocking == 0` ends the loop, with one exception, taken at most once per run: **a FULL round that
finds nothing blocking but raises non-blocking findings.** Those go to a fixer in this PR rather than
to an issue. Run step 4 on them with a fresh fixer, then steps 5 and 6 and COMMIT as usual, and
review the head that produces under *That confirming round is BLOCKING-ONLY* in FINISH — as is every
round after it, which is what stops the loop implementing nits forever. A commit here costs at least
that round, and more when a fix brings a blocker of its own. A finding beyond the PR's own scope — a
redesign, an adjacent defect, behaviour the ticket did not ask for — is one that fixer declines
rather than implements, and its brief says so, since only blocking-only rounds follow it. Where
nothing moved the head — the fixer declined them all, or they named only the PR description — no
round is owed, and it is not step 1's *the fixer declined everything*: record the same count again
(`phase: reviewed`) and go to step 7. Where the round cap leaves no round for that review, decline
them on the record instead, each with its failure-mode sentence and the cap as the reason, and go to
step 7. A clean round with no non-blocking findings goes to step 7.

### 4 — FIX

Record the await before spawning, clear it on the result — see **State**. Snapshot the worktree hash
first, and tell the fixer to restore any measurement mutation **before** it reports; a fixer's intended
edits stay, its measurement scaffolding does not.

Spawn a fresh fixer. What it implements and what it declines, harden's Phase 1 discipline and the JSON it
returns are in `fixer.md` in this skill's directory. Brief it with that file's absolute path and tell
it to read the whole file before it does anything else; the brief itself hands it the findings verbatim,
the build step 5 names and the commit rules in COMMIT. The rules its declines must meet are that
file's *Declining is governed by harden's deferral rules*.

**And do not ask either agent to re-derive evidence its brief already carries** — hand it the
measurement and point its budget at the claims it DOUBTS. This binds Step 1's brief as much as this
one.

**A finding may name the PR DESCRIPTION rather than a file, and it can be blocking.** The fixer
cannot edit the description, so the orchestrator applies that one and says which it applied; it
still counts as the round's fix and the round proceeds normally. Do not wave it through as cosmetic:
the description is the durable public rationale attached to the closing of the ticket, no test can
fail on a false sentence in it, and a repo-wide grep for a corrected claim will never reach it.

A declined **blocking** finding does not end the loop quietly — see **Termination**.

### 5 — GREEN

`mvn -o clean install` from the **repository root**. Not `-pl api`, not `-pl omod`: the omod unpacks
the *resolved* api artifact over `omod/target/classes` at generate-resources, so a `~/.m2` jar from
another branch shadows the reactor's classes and reddens tests on a drift that is not in the source.
A root install is also what produces the omod the verifier deploys.

A red build is the fixer's problem, inside the round — never a finding for the next reviewer. A round
that pushes red code makes the next round a review of a broken build.

**It is the fixer's build and the orchestrator does not re-run it.** Read the `green` field and
spot-check what it claims: the surefire reports are on disk, and what actually needs verifying is
`git status`, the branch, and the pushed sha. A second full root install per round buys nothing
those do not already carry. Where the report is missing, vague, or names a command other than a root
`mvn -o clean install`, run it yourself; the rule is against the reflex, not the check.

### 6 — VERIFY, when the round touched runtime behaviour

Gate this on what the round actually changed, at most once per round. A round that moved only
javadoc, comments or tests needs no standalone restart. A round that changed behaviour only
observable at runtime does — and where tests structurally cannot answer the question (streaming,
SSE timing, wire serialisation, prompt or latency behaviour) the verifier is not optional: skip it
there and the loop converges on code nobody ran.

**A third case: runtime-visible, but not observable by THIS instrument.** The procedure in `verifier.md`
deploys an `.omod` and restarts `openmrs-standalone.jar`. A change to the published Docker image's
ENTRYPOINT — `backend-init.sh`, the model-fetch library it sources, the container's own startup
wiring — is runtime behaviour a standalone never executes, so deploying and restarting cannot see it
however carefully it is done.

So name the instrument before deciding. If the prescribed one cannot reach the change, say which one
can, use it, and record BOTH the skip and the substitute in the report — this is not `--no-verify`,
which asserts no round can touch runtime behaviour. `verified_shas` stays empty there, which the
gate already treats as a legitimate no-verifier-ran state, so the report is the only place the
substitute lands.

The verifier is a fresh subagent that **does the work itself** — it does not delegate to another
skill, and nothing about it depends on one being installed. It is **not** the reviewer, for a specific
reason: a reviewer that deploys is grading its own deploy, so when it hits the stale-omod trap or an
orphaned server, that surfaces as a *finding about the code* — and a wrong blocking finding is what
the loop cannot escape.

Its procedure, and the JSON it returns, are in `verifier.md` in this skill's directory. Brief it
with that file's absolute path and tell it to read the whole file before it does anything; the
brief itself names the round, the head it covers, the behaviour to drive and the repairs earlier
verifiers in this run reported.

**A verifier's observations can falsify a claim the PR makes in prose, and that is a finding rather
than a footnote.** It is running the code, so it sees the units the documentation guessed at. Read
the `observed` field for what it contradicts as well as for what it confirms.

**Record the sha it covered** — `gate-state verified-sha 3085ff02` — for the same reason a round records
the sha it reviewed: a runtime verdict is a statement about one commit and does not survive the next
one. FINISH's own check and the Stop gate both read the last entry of that list against the head being
handed over, so a verifier run that goes unrecorded reads as no verifier run at all.

`classification: "not-the-environment"` is a verification result and a candidate finding for the next
reviewer. `"unrepairable"` aborts the run. Inside a round, neither is recorded as a blocking finding
by the orchestrator: **an environmental failure is not a review finding**, and if the loop is allowed
to treat one as blocking it will grind rounds against a broken standalone until the cap.

### COMMIT

**Check the branch before you EDIT, and again before you commit.** The commit-time check below is
necessary and not sufficient: by then a wrong-tree edit has already happened, and the only reason it
is recoverable is that nothing was committed yet.

**Re-check the branch immediately before committing.** Step 0's check happens once; agents share
this worktree and one of them running `git checkout` silently redirects everything after it. So:
`git branch --show-current` must equal the PR's head ref before `git commit`, and if it does not,
fast-forward the head ref onto the work (append only — never reset, never force) rather than
committing where you stand.

One commit per round, in the repo's existing voice (see `git log`), pushed to the PR head branch.
**Append only — never amend, never force-push.** A reviewer must be able to see the chain of rounds,
and rewriting history under one that is mid-flight is how a round reviews a sha that no longer
exists. **A protective commit taken mid-round does not violate this, and the commit rule in *State*
outranks it.** The convention guards the chain of rounds, which such a commit only ever appends to.

### 7 — FINISH

The reviewer found nothing blocking, so this is the sha you are handing over — and **FINISH does not
edit it.** Nor does it file an issue, or offer to: step 3 has already had a clean full round's
non-blocking findings fixed or declined, a blocking-only round hands a fixer nothing but blockers,
and what the loop did not implement goes into the PR description this step re-derives.

Re-deriving the PR description is still owed and is not an exception, because the body is not in the
tree and does not move the head.

**Delete the run's own `pr-<n>-r<round>` refs.** They are local fetch copies of the PR head with no
upstream, worth nothing once the round is over and re-fetchable from `pull/<n>/head` while GitHub
retains it. The check is `git branch --list 'pr-*'` in a repo this loop has worked, read for the
`pr-<n>-r<round>` shape. Delete them here rather than at the top of the next run, because the next
run may be in a different repo or may never happen.

**Re-derive the PR description against the merging head before marking ready.** Across rounds the
body describes code that later rounds change under it, so the patches this loop applies to it
accumulate into something false. Patching mid-loop is right when a finding names the body; leaving
those patches as the final text is not. Rewrite it whole here, re-measuring every figure in it
rather than carrying one forward. Name in it, one line each, every finding the loop did not
implement — each decline with its failure-mode sentence, each note a blocking-only reviewer
returned, and any non-blocking observation the verifier made on the merging head — since with no
issue filed, the PR is where it stays visible.

**A runtime-visible change is not ready until a verifier has run against the head that will merge.**
Step 6 sits on the fix path, so without this a PR whose round 1 found nothing blocking would reach
`gh pr ready` with the standalone never started. So before marking ready: if the change is
runtime-visible and no verifier run covers the current head, run one now. It is the same verifier
under the same rules — it repairs the environment, never the artifact — and `unrepairable` aborts
the run here exactly as it does inside a round.

**A PR that could not be verified is not marked ready**; report it as
converged-but-unverified and stop.

`unrepairable` aborts and "could not determine" stops as converged-but-unverified, but a
`not-the-environment` finding on the merging head is a **blocking finding**: record it and continue
from step 4, under *That confirming round is BLOCKING-ONLY* in this step. An ENVIRONMENTAL failure
is still not one and must never re-enter the loop — that is what grinds rounds against a broken
standalone until the cap.

**Whether a verifier run still covers the head is a question about the compiled artifact, not about
the source, the timestamp or a file hash.** A comment-only push after the verifier ran made
byte-identity FALSE — a split comment line shifted a `LineNumberTable` — while `javap -c` against
the exact class the verifier ran proved equivalence; a comment renumbering was proved neutral by
compiling both variants against the resolved classpath and diffing the emitted class files, rather
than by arguing that comments cannot change bytecode. This does not touch the verifier's own
deploy-identity hash, in `verifier.md`'s *Confirm you are testing this build*: that one asks whether the class that
LOADED is the one you built, which is an identity question a hash answers and a disassembly does
not.

Then mark the PR ready for review if this run opened it as a draft (`gh pr ready <n>`), and say in the
report that it is now ready, naming the sha the verifier covered.

**Marking ready is the LAST action of the run, after the last push — never before one.** It is not a
status update, it is a trigger: the Claude Code GitHub App reviews every push to a NON-draft PR and
skips drafts entirely, so a run that marks ready and then keeps pushing buys one automatic review
per push.

So if anything after the ready mark requires another push — `main` moved and the branch needs
merging, a late round finds something, the description is re-derived — **the run was not finished
and should not have marked it.** Do the merge, the rounds and the description first; mark ready
once, last.

This does not license moving a PR back to draft to dodge the trigger — that rule stands. It licenses
ordering the run so the question never arises: check `git log origin/main..HEAD` and the PR's
mergeable state BEFORE marking ready, not after.

**Where an edit here really is owed, it costs one more round rather than none.** A blocking finding
turns up while you are handing over — `main` moved, the description was false — or the verifier on
the merging head reports one. Implement it, and run one more round.

**That confirming round is BLOCKING-ONLY, and this is what makes the rule terminate.** A round that
implements nits and then re-reviews can never converge, because a review is expected to produce nits;
a round that may only *report* blockers converges as soon as there are none. So brief the confirming
reviewer to return blocking findings alone, and anything else it notices as `notes`, which reach
the run record and the PR description; nothing in them is implemented or filed.

## Editing by script, which is how edits get silently lost

Every role here edits files by running a short script rather than by hand, because the edits are
precise and the files are large. Four failure modes follow, all silent and all cheap to close:

- **A replacement that matches nothing reports success.** `str.replace` returns the string unchanged
  and the script prints whatever you told it to. So: **assert the target is present before
  replacing**, and let the assert kill the script rather than continuing to the next edit.
- **A slice can span further than you meant and take a neighbour with it.** So: after any multi-line
  replacement, **count what should still be there** — test methods, symbols, bullet points — and
  compare against what you expected.
- **A script's own report is not evidence.** Verify by reading the file back, with a grep for the
  text you believe you wrote.
- **A batched write reports edits an abort never made.** Write each replacement as you make it: a
  script that prints per-edit success and opens the file once after its loop loses every edit that
  write would have carried when a later assert throws, and the prints stand. **And read the report
  for WHICH edits it names.**

None of this is optional politeness.

## Correcting a claim means finding every home of it

When a finding is that some statement is false, the statement is rarely in one place.

So a correction is not finished when the named site is fixed. **Search for the claim's rarest single
TOKEN, over the whole tree rather than over the docs**, and fix every hit, including the ones a
reviewer did not name; then grep again for the phrasing you just wrote, to see how many places now
say it. Searching the PHRASING is what leaves the last home standing, and it fails two different
ways: a phrase the file's own formatting has split — markdown emphasis inside it, a line break
falling between a quantifier and its noun — does not match what you typed, while a home that is a
DATA file rather than a doc is missed by scope alone. Treat no list of those mechanisms as closed.
Two homes are easy to forget: the project's own instruction file, which outranks the code and is the
worst place for a half-true rule, and the **pull request description**, which no repo-wide grep will
ever reach.

And a positional cross-reference — "the bullet above", "the section below" — is a claim about layout
that any insertion falsifies. **Name the target instead of locating it.**

`harden`'s *Don't stop correcting a claim at the site you noticed it* carries one remedy this
section does not, and it is the one for a sweep that keeps returning one more home: enumerate the
claim's SUBJECT rather than a phrasing.

## Termination

> **A `/pr-harden` run is complete when the SHA IT IS HANDING OVER has been reviewed with zero
> blocking findings — and, where any verifier ran, verified on that same sha.**

Not when a round makes no edits. In a full round the fixer implements the non-blocking findings too,
so a round that edits has not thereby found anything that blocks, and an edit count would answer the
wrong question. In a blocking-only round the two coincide — the fixer edits for blockers
alone — and the condition still reads the blocking count there, for the reason that outlives the
coincidence: **the count belongs to the reviewer, whose work is not being judged, and an edit count
hands the exit to the fixer, whose work is.**

Check it, do not estimate it. The count comes from the reviewer's JSON and the shas from
`gate-state`, and all of it goes in the state file where something other than you can read it:
`pr-harden-gate.sh` refuses the stop when the head is not the last reviewed sha, or not the last
verified one. Both comparisons fail OPEN — no git, no worktree, no recorded sha, or a recorded value
that is not sha-shaped (hex, 7 characters or more) — so neither can wedge a run that owes nothing,
and `verified_shas` being empty means no verifier ran for this PR at all, which is a legitimate state
for a change no round could make runtime-visible.

**What the check costs, said rather than left to be discovered:** a terminal entry is
indistinguishable from a mid-flight one — both are `phase: reviewed, blocking: 0` — so a FINISHED
run's leftover entry in a worktree that has since moved on now blocks until the six-hour expiry.
`gate-state clear --only pr` is the remedy and the block message says so. Under these rules a run
that handed over correctly leaves the head ON the reviewed sha, so this is the tail of an entry
written under the older ones, or of later work in the same worktree — never of a run that finished
cleanly.

`pr-harden-gate.sh` ships next to this file and runs on Stop. It refuses to end the turn while the
newest entry for this directory says `blocking > 0`, and also while it says a run is in flight that
has not yet recorded a review — so a run cannot end by never having reviewed at all.

A skill cannot register its own hook, so this is a one-time install per machine:

```bash
mkdir -p ~/.claude/hooks && cp .claude/skills/pr-harden/pr-harden-gate.sh ~/.claude/hooks/
# then add to ~/.claude/settings.json (merge — do not replace an existing hooks block):
#   "hooks": { "Stop": [ { "hooks": [
#     { "type": "command", "command": "$HOME/.claude/hooks/pr-harden-gate.sh", "timeout": 10 }
#   ] } ] }
```

**Two things end a run early, and both are labelled.** Say the line out loud in the report and set
`override: true` in the state file, so the deviation is on the record rather than in a commit message:

> "I am ending this run after round N without convergence, because [a blocking finding was declined:
> \<finding\> — \<reason\> | the round cap of N was reached | the verifier reported `unrepairable`:
> \<cause\>]. Round N+1 was required and I did not run it."

A **declined blocking finding** is one of them. Declining a non-blocking finding costs nothing; the
loop exits normally. Declining a blocking one means the exit condition was never met, so the run ends
visibly as *did not converge* — never as success. That asymmetry is deliberate: it keeps the exit
condition out of the hands of the agent whose work is being judged, while still leaving it able to
refuse a wrong finding.

The **round cap** (default 4) is the other. A cap is not convergence; reaching it is an override.

**Raising the DEFAULT cap is a third move, and the runs that took it read a signal first.** What
licenses a raise is evidence the loop is still WORKING rather than spinning — a different defect
each round, or findings shrinking — and a round that re-raises what an earlier one raised is
spinning: take the override instead. Raise it a round or two at a time, re-read the signal each
time, and say what you raised it to, because a raise nobody states turns a *did not converge* into a
*converged* silently. **Do not raise a cap the caller set:** `--max-rounds N` is their budget, and
under `ticket-pool` a session that outruns `ticket.timeout_seconds` is killed. A labelled `draft` is
by far the cheaper outcome.

What is not permitted is ending the run without either the convergence line or an override line, and
**handing the decision back to the user is the disguised form of it**. "Want me to run another
round?" ends the run with blocking findings in it while reading as deference. If a round is owed,
run it.

## State

`~/.claude/pr-harden-state.json`, keyed by the WORKING TREE's physical path — `pwd -P`, symlinks
resolved, which is what both hooks key on and what `gate-state` writes. Resolved on both sides or the
two disagree wherever a path component is a symlink (`/tmp` on macOS, a symlinked home), and
"no entry" is the gate's fail-OPEN case: a run with findings outstanding stops and nothing says why.

**And the gate runs in the SESSION's cwd, which a shell `cd` does not move.** An entry written from
another checkout lands under the worktree's path, and the gate looks under the session's, where there
is none — the same fail-OPEN, anticipated from `KEY="$(pwd -P)"` and not yet observed. So start the
session in the worktree; if it is already running elsewhere, `EnterWorktree` into it before the
opening entry. Its isolation guard then refuses what it cannot verify, git or not: do as its message
says, or run the steps as a script by absolute path, whose git must still target this worktree. It
refuses `$PPID` on a `gate-state` line even unchained, so run `echo $PPID` alone and pass that pid to
`--owner`.

**Under `$HOME`, never in the repo** — an in-repo file would show up in the `git status --porcelain`
the round measures, and would be swept into the round's own commit.

Keying on the working tree is also what makes this multi-tenant. Under the pool driver each ticket is
worked in its own `git worktree`, so two runs on one repository have two keys and two entries; only
two sessions sharing ONE directory still share one entry, and `owner` is what tells those apart.

```json
{ "/abs/path/to/repo": {
    "pr": 93, "round": 2, "blocking": 1, "phase": "reviewed",
    "ts": 1755400000, "override": false, "owner": 51039,
    "awaiting": [ { "agent": "fix r2", "since": 1755400000 } ],
    "reviewed_shas": ["<r1 sha>", "<r2 sha>"],
    "verified_shas": ["<sha a verifier run covered>"],
    "declined": [ { "round": 1, "id": "r1-2", "finding": "…", "reason": "…" } ] } }
```

`phase` is `"init"` before the first review, `"reviewed"` once a reviewer's count is recorded,
`"fixing"` from the moment the fixer is spawned until a reviewer's count is recorded again. On `init` and
`fixing` the gate blocks regardless of `blocking`, so leave the last measured value there for the
record. The gate reads `pr`, `round`, `blocking`, `phase`, `ts`, `override`, `owner`, `awaiting`,
`unattended`, `mode`, `reviewed_shas` and `verified_shas`; `declined` is the orchestrator's own
ledger, carried in the same entry so one write keeps it in step with the rest. A missing
`reviewed-sha` or `verified-sha` call is the difference between a checked handover and an unchecked
one.

**`owner` is what tells your entry from somebody else's, and it is not the unattended marker's job.**
This file is keyed on the CHECKOUT, so a pool run and an interactive session in the same directory read
one entry. So stamp `owner` with `$PPID`, which from a tool shell is this session's own `claude`
process; each gate allows the stop when the pid in its own entry is alive and is not an ancestor of
the stopping session, and when it is DEAD. An UNSTAMPED entry is held to the contract, so nothing is
relaxed on a missing field.

**`awaiting` is not optional bookkeeping — without it an unattended run cannot proceed at all.**
Every phase here delegates to a subagent, and while a background one is outstanding the orchestrator
has nothing to do but yield. The gate blocks yields, so a run waiting correctly looks exactly like a
run that quit. So: **record the await immediately before spawning, and clear it the moment the
result arrives.** A non-empty, fresh `awaiting` lets the gate allow the yield — not a loophole,
because the harness re-invokes the orchestrator when the agent completes, so yielding mid-await is
how the run proceeds rather than how it ends.

**That last clause holds in full only for an ATTENDED session.** When a `claude -p` turn ends, the
process kills a `run_in_background` Bash command within seconds if nothing else is outstanding, and
stops a background agent still running 600 s later (the default of
`CLAUDE_CODE_PRINT_BG_WAIT_CEILING_MS`); either way it then exits without re-invoking the
orchestrator. An agent that finishes inside the 600 s is waited for and does re-invoke it. Which
case a yield is in is not known when the turn ends. So **an unattended run never ends a turn with an
agent outstanding — collect it in the same turn.** The gate enforces it now, scoping the allow to
attended sessions off a pid-stamped marker the driver holds for the life of the run; the rule is
stated here as well because a gate can only refuse a stop after the decision to stop has been made,
and that decision is what costs the run. And do not read a stream with no gate text in it as
evidence the gate never ran: hooks DO reach `-p` sessions.

**That marker answers attendedness and nothing else, and its own test is an ancestry walk.** A live
marker whose pid is NOT an ancestor of this session belongs to a co-located run, and a co-located run
does not make this session unattended — the yield then allows. An INDETERMINATE walk keeps the block
instead, because losing the unattended guard back is the more expensive direction. Whose ENTRY it is
is a different question about a different file, answered by `owner` above and never by this marker.

**Collecting in the same turn means spawning in the foreground, where the harness allows it.** When
the `Agent` schema carries `run_in_background`, pass `false` on each call: several such calls in ONE
message still run concurrently, and each agent's report returns as that call's own result, in this
turn. The flag has come and gone between harness builds — so where a spawn still answers
"Async agent launched", an unattended run keeps the turn alive with a bounded foreground wait. **A
delegated agent's output file is its whole JSONL transcript, never its report**: each read injects a
window of raw agent chatter, the next a different window rather than the rest of the first, and the
orchestrator re-sends all of it on every later turn. Where you need to block on something that is
NOT an agent — a build, a server coming up — that is a background Bash task, whose output file is
its stdout and is safe to read; wait on it as *this session must not busy-wait either* says.

**Snapshot the worktree before every delegation and compare it after — on ANY terminal outcome.**
`git diff | shasum` before you spawn; the same after the agent returns, fails, stalls or is killed. On a
mismatch, treat it as the agent's residue, never as a finding. **The hash DETECTS; it cannot restore —
a shasum is not an artifact you can apply.** What makes the residue recoverable is the commit rule
below, which is why that rule binds anything that mutates the worktree and not only delegation.

Every reviewer, fixer and verifier brief here tells the agent to **mutate the production code, run
it, restore it**, because that is the strongest evidence available. So the risk is created by the
instruction. A confirming agent died mid-response, and the mutation was still in the worktree when
the notification arrived.

Death is not the only path: an agent that simply forgets to restore looks identical from here. And a
hash comparison costs nothing, which is the whole argument for doing it every time rather than when
something feels wrong.

**And the snapshot cannot see the third path, so a rule has to: DO NOT EDIT THE WORKTREE WHILE A
DELEGATED AGENT IS RUNNING.** Commit first, or wait. An agent told to mutate-and-restore restores from
what it READ, so an edit that lands after it read and before it restores is silently reverted — and the
hash comparison is blind to it, because your own concurrent edits make the hash differ legitimately.
Two consequences: commit before anything mutates the worktree — before you delegate, and before your
OWN measurement probe, because a commit is the only thing a remembered restore cannot undo — and
tell agents to restore with `git checkout -- <path>`, never by rewriting content they remember.

**`git checkout -- <path>` is the right restore only where the file carries nothing but the mutation.**
It restores HEAD, so in a worktree holding uncommitted intended work it silently discards that too.
The axis is the FILE's state, not who typed the command — which is the whole reason the commit rule
above comes first. When it does happen, say where to look: the registered PreToolUse hook copies
modified tracked files outside the repo before the destructive command runs, best-effort and
bounded, and prints the destination and count — trust that printed message rather than assuming the
file is there. It reaches only the agent whose call triggered it.

**Tell every agent to restore BEFORE it reports, not after** — a mutation restored late is a mutation
that ships if the agent dies mid-sentence.

**And tell every agent to wait on its own builds inside its turn**, per the verifier's *Do not end your
turn to wait*, in `verifier.md`.

**Clear the await on ANY terminal outcome — completed, failed, stalled, killed — not on a result arriving.**
"The moment the result arrives" says nothing about a result that never will, and agents die. The
harness reports the death, so there is no excuse for waiting out a timeout. The one-hour bound and
the no-`since`-reads-as-dead rule are backstops, not the mechanism.

**And this session must not busy-wait either.** *Wait on a CONDITION, never on a clock* is written into
`verifier.md`, but the orchestrator is where a blind `sleep` loop costs the most, because its
context is the largest thing being re-sent per turn. Whatever you are waiting on — a boot, a build, an
agent, a lock — wait on the CONDITION inside the turn: an agent by spawning it in the foreground
(*Collecting in the same turn*), anything else by a foreground loop that exits on the condition, on a
failure signature, or at a bound under the tool's ten-minute timeout — `end=$((SECONDS+540)); until
grep -qE 'BUILD (SUCCESS|FAILURE)' "$F" || [ $SECONDS -gt $end ]; do sleep 5; done; tail -3 "$F"`.
Backgrounding the wait and ending the turn for its notification is what the gate refused mid-run.

**And a dead delegated phase needs a contract, because it is neither an abort condition nor a
finding.** Left undefined, an unattended run ends on the first agent death. The contract: clear the
await, retry the phase **twice**, and change something between attempts — an agent that stalled on
volume gets a leaner brief, one that stalled on nesting is told not to delegate. **A session or quota
429 is neither of those, and the lever that used to work is no longer available.** It was a cheaper
agent. **Do not reach for it: a per-call `model` on the Agent tool is refused by a PreToolUse hook
(`~/.claude/hooks/no-subagent-model-override.sh`), so a retry cannot downgrade an agent from inside
the round.** What is left is a leaner brief and WAITING. But **a stated reset is not always inside
this run's horizon**: the error text does not separate a burst from an exhausted cap and both
attempts must not be spent waiting. After the second retry, stop with the labelled deviation naming
the phase and the failure mode, exactly as the round cap does.

**And under `ticket-pool`, `[Request interrupted by user for tool use]` on an agent's result is not the
operator.** The driver stamps `CLAUDE_PIPELINE_SESSION=1` into the headless sessions it starts; where
that is set, treat the result as the agent's death and apply this contract.

Write it at every transition:

```bash
~/.claude/pipeline/gate-state --owner $PPID pr-set --pr 93 --round 2 --phase reviewed --blocking 1
```

Add `--override --reason "…"` only when taking the labelled override. `declined`, `reviewed_shas`
and `verified_shas` have their own subcommands — `gate-state declined --round 1 --id r1-2 --finding
"…" --reason "…"`, `gate-state reviewed-sha 3085ff02` and `gate-state verified-sha 3085ff02` — so a
transition write never has to restate them and cannot drop them.

`gate-state` holds an exclusive `flock` across both state files and
writes atomically. Do not retype the mechanism.

Recording and clearing an await is its own one-liner, kept apart from the transition write above so
that a spawn never has to restate the phase:

```bash
~/.claude/pipeline/gate-state --owner $PPID await "review r3" --only pr
~/.claude/pipeline/gate-state --owner $PPID clear-await --only pr
```

`clear-await` takes no label and empties the whole list: clear once, after the last agent of the wave
has returned or died. Drop `--only pr` and it writes both gates at once, which is what a nested `/harden` cycle needs.

When the run finishes — converged or overridden — the entry must say so (`phase: reviewed` with
`blocking: 0`, or `override: true`). A stale `blocking > 0` left behind is what the 6-hour expiry exists to clean up
after you.

## Where `/harden` sits, and why it is not inside the round

`/harden` is itself a convergence loop with its own Stop gate. Nesting it here would give two
termination contracts arguing on every turn, and each round would have to drive harden's Phase 1 to
convergence before the next reviewer even looked. It also supplies the *weaker* review for this purpose: its
passes run in the context that wrote the code, which is what this loop is built to avoid. And its
Phase 2 polish rewrites lines the next fresh reviewer then reads for the first time — new unreviewed
surface, so it can *raise* the round count.

So harden's Phase 1 discipline is borrowed as instructions (steps 1 and 4 above; step 1's is in `reviewer.md` and step 4's in `fixer.md`) and the skill runs
at the two ends instead:

- **before the loop** — harden the slice while you still have the writing context, then open or
  refresh the PR. Polish with context; adversarial review without it.
- **after the loop converges** — the exit condition is "nothing blocks", which says nothing about
  polish. If that harden run edits anything, one more review round is owed, because no clean agent
  has seen those lines.

## Reporting

- Rounds run, and for each: the sha reviewed, findings raised (blocking / non-blocking), implemented,
  declined, whether the verifier ran, and the commit.
- **The terminating round's blocking count, as measured** — from the reviewer's JSON, quoted. The
  report is not complete without that line or an override line, and neither may be replaced by a
  question to the user.
- Every declined finding with its failure-mode sentence. A decline without one is not a decline.
- Every verifier repair, with its cause — even the ones that worked.
- Offer to run `pr-review --post` or `--stage` once, at the end, if the user wants the review record
  public.

## Write the run record — always, before you finish

Append a record to `~/.claude/skill-lessons/<UTC-date>-<repo>-<ticket-or-pr>.md` (create the
directory if needed). This is **capture, not derivation**: it records what happened, never a proposed
rule. `skill-retro` turns accumulated records into skill edits, because a lesson needs corroboration
across runs and an adversarial pass before it changes how every future run behaves — neither of which
this run can supply about itself.

**Append means `>>`, never `>` or the Write tool.** The name is keyed on date and ticket, so a second
record for one ticket and day (a second run, or this run's pr-harden record after its resolve-ticket
one) lands on the first. Keep the name — it is how `pool-run` finds your record.

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
only what actually surfaced — a compaction, a context warning, and the step it happened at — and name
the transcript, which carries the ground truth a retro can measure instead. The path needs no
bookkeeping: the uuid is `$CLAUDE_CODE_SESSION_ID`, and the transcript is the one file
`ls ~/.claude/projects/*/"$CLAUDE_CODE_SESSION_ID".jsonl` prints. Take neither name from anywhere
else. `no compaction · peak not surfaced` is the expected reading and is worth writing for the same
reason a clean run still gets a record. Derive nothing from it here: whether context pressure costs
quality is a claim about many runs, and no run can settle it about itself.

## Anti-patterns

- **Don't paraphrase the reviewer to the fixer.** The findings go across verbatim, with their
  failure-mode sentences intact. When the run started from a ticket the orchestrator implemented, it
  holds the writing context and is the least neutral participant in the loop — softening a finding on
  the way past is the one way its contamination reaches a round.
- **Don't hand the termination decision back to the user.** If a round is owed, run it. Reporting
  truthfully that the run has not converged and *then* handing back is still the violation — the tell
  is the handback, not the claim.
- **Don't post intermediate rounds to GitHub.** Five rounds of comments you then fix is noise on the
  PR, and it makes `pr-review`'s own prior-conversation rules fight the ledger.

## When NOT to use this skill

- On a PR whose direction is not agreed. Rounds harden an approach; they do not choose one. Settle
  `pr-review` Step 2 first.
- On a cross-repository PR you cannot push to — Step 0 refuses it, and rightly.
- For a single review. Use `pr-review`; this is for when you want the review repeated by agents that
  cannot be talked round.
- On work flagged exploratory or about to be reverted.
