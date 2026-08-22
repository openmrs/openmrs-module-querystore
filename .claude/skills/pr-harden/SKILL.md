---
name: pr-harden
description: Harden an open pull request by cycling clean-context review rounds against it — a fresh agent reviews the pushed head, a second fresh agent implements every finding it agrees with and declines the rest on the record, the build is proved green, the change is verified on a real standalone where runtime behaviour is at stake, and the round is committed and pushed. The cycle repeats until a review round reports zero blocking findings. Use when a PR should be hardened by reviewers who have never seen it being written. Trigger phrases include "harden this PR", "review and fix the PR until it's clean", "cycle review rounds on PR N".
argument-hint: <pr-number-or-url> [--max-rounds N] [--no-verify]
version: 0.4.0
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
  naming a *different* PR is a stale run: report it and ask before clearing it.

Then write the opening state entry (`phase: "init"`, `round: 1`) — see **State**. From this point the
Stop gate will not let the turn end until a review round reports zero blocking findings, or the
override is taken.

## The round

```
1  REVIEW    fresh subagent · pushed head · declined ledger · last verifier report
2  RECORD    the reviewer's blocking count → state          {phase: "reviewed"}
3  exit?     blocking == 0 → step 7
4  FIX       fresh subagent · implements what it agrees with, declines the rest
             on the record                                   {phase: "fixing"}
5  GREEN     mvn -o clean install, from the ROOT
6  VERIFY?   runtime-visible change → fresh verifier on the standalone
   COMMIT    one commit, push · round++ → step 1
7  FINISH    apply the final round's non-blocking findings, green, commit, push,
             VERIFY the merging head if nothing has, then mark ready
```

### 1 — REVIEW

Spawn a fresh reviewer and have it run the repo's `pr-review` skill on the PR — Steps 1 through 3 in
full: read the issue the PR claims to close and not only the PR, ask whether this is the right fix
and the best one available, verify rather than read, and run its adversarial refutation pass where it
is worth it. Nothing is posted to GitHub. `pr-review`'s default is already consent-gated, so pass
neither `--post` nor `--stage`, and tell the reviewer explicitly that its output goes to a machine,
not to the PR.

Record the await before spawning the reviewer, and clear it when its JSON arrives — see **State**.
Snapshot the worktree hash first, and tell the reviewer to restore any mutation **before** it reports.

**Tell it not to spawn subagents of its own.** `pr-review` Step 3 asks for an adversarial refutation
pass, which reads as an instruction to delegate; nested delegation is what killed the first reviewer
on this loop's first run, mid-refutation. The independence is already supplied one level up — the
reviewer IS the independent agent — so a second layer buys a failure mode and nothing else. Have it
argue both sides in its own reasoning, or mark the finding non-blocking.

Fetch and review the **pushed** head, not the local worktree:
`git fetch origin 'pull/<n>/head:pr-<n>-r<round>'`. Record the sha.

**Tell the reviewer what to diff against, and never let it be a local branch name.** Fetch the base
too and name it explicitly: `git fetch origin main` then `git diff origin/main...pr-<n>-r<round>` — or
better, the PR's own base from `gh pr view <n> --json baseRefName`. A local `main` is stale on any
machine that has not pulled, and the merge base then reaches back to whenever it last did. Measured
on this loop's first real run: `main...` produced **13,602 lines against an 864-line change**, most of
it other people's commits. That is the worst failure this design has produced, because it is silent —
the reviewer returns well-formed JSON with a legitimate-looking blocking count, about code the PR
never touched, and a fixer then acts on it.

What the reviewer is given, and nothing more:

- the PR, its diff, and **the ticket it claims to resolve** — read with its comments, not just its
  title. A GitHub issue via `gh issue view <m> --comments`; a JIRA key (`O3-1234`, `TRUNK-6429`,
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
- from round 2 on, harden's Phase 1 rule **re-derive the merged result from scratch**. By round 3 the
  code is an accretion of rounds of individually-approved fixes, each judged against the state at the
  time it landed; the bug lives in the seam between two separately-correct mechanisms. This rule was
  written for exactly this shape.

It returns JSON as its final text, and nothing else:

```json
{ "pr": 93, "round": 2, "head": "<sha reviewed>",
  "findings": [
    { "id": "r2-1", "blocking": true,
      "file": "api/src/main/java/.../DrugSafetyValidator.java", "line": 412,
      "finding": "…", "failure_mode": "…", "evidence": "…" } ] }
```

**"Does not resolve the ticket" is a blocking finding, and it is the first one to look for.**
When the run started from a ticket rather than from an existing PR, `pr-review` Step 2 stops being a
preliminary and becomes the primary axis: a PR that is internally clean but does not resolve the
thing it claims to is exactly what a polish loop will happily converge on. Judge it against the
ticket's own words and its comments, never against the PR description, which was written by the same
agent that wrote the code.

**`blocking: true` requires a non-empty `failure_mode` and `evidence`.** A finding with either
missing is non-blocking by construction — the orchestrator downgrades it and records that it did.
This is what stops "this feels hacky" from holding the loop open forever, and it is also what gives
the fixer something specific enough to decline honestly.

### 2 — RECORD

The orchestrator writes the blocking count to state **from the reviewer's JSON**. Not from the
fixer's reading of it, not from your own judgement of which findings are serious. The exit condition
belongs to the agent whose work is not being judged.

### 3 — Exit test

`blocking == 0` ends the loop. Non-blocking findings do not extend it — that is the whole point of
separating the fixer's scope from the exit condition. Go to step 7.

### 4 — FIX

Record the await before spawning, clear it on the result — see **State**. Snapshot the worktree hash
first, and tell the fixer to restore any measurement mutation **before** it reports; a fixer's intended
edits stay, its measurement scaffolding does not.

Spawn a fresh fixer. It implements **every finding it agrees with, blocking and non-blocking alike**,
and declines the rest on the record. Its brief carries harden's Phase 1 discipline:

- **Trace outward** one level on each thread: trigger paths, optional dependencies absent at runtime,
  lifecycle order, state propagation across module boundaries, invalidated invariants in *unchanged*
  neighbours (a javadoc or comment your edit just made false is a finding), and re-deriving the
  merged result from scratch.
- **Name the test** for every behaviour change — one that fails on the pre-change code and passes
  after, verifying the runtime effect rather than a proxy for it. Where a path is genuinely blocked,
  split the blocked sub-path from the runnable one and sketch the contract for what stays blocked.
- **Don't rewrite prose faster than you verify it.** When a finding is about text an earlier round
  wrote, delete the unsupported clause rather than replacing it with a better-sounding one. That is not
  a counsel of caution — measured on this loop's second run, a correction of a false claim introduced a
  DIFFERENT false claim, which the next round then had to catch.
- **Don't write a tally a later round will have to re-measure; write the method.** Every count published
  on the fourth run went stale, several twice, and each recurrence cost a round because the next reviewer
  re-measures what a comment asserts: "negating it reddens exactly two" became three in the very round
  that added the third observer, and "the three sites that quoted it" became four in the round that found
  the fourth. Both had a sentence beside them claiming they had just been re-measured. The recurrence
  stopped only when the enumeration was deleted in favour of *"mutate the line and read the failures"* —
  so prefer that form, and treat an exhaustive list as worse than none, since it invites the next reader
  to treat the extra failure as a regression they caused. If a count really is load-bearing, name the
  head it was measured on.
- **Fix every home of a corrected claim, not the one the reviewer named** — see *Correcting a claim
  means finding every home of it*. And edit by script under the rules in *Editing by script*: assert
  before replacing, count neighbours after, verify by reading back.

**Declining is governed by harden's deferral rules, in full.** A declined finding needs the
failure-mode sentence — *"if we ship without this, X breaks because Y"* — and without that sentence it
is not a decline, it is an unanalysed item, so implement it. The anti-tell phrases are not reasons:
"below noise floor", "stylistic preference", "matches the existing pattern", "borderline", "low risk"
without naming the risk. Silent-failure findings get their severity raised, not lowered. And the
**conflation check** matters most here, because the loop gives the fixer a standing incentive to
shrink findings: am I declining the reviewer's recommendation, or a maximalist version I constructed
from it? The narrow version is the one on the table.

`CLAUDE.md` outranks a reviewer. A finding that asks for a test's expected value to be changed, for a
uniform ATC veto, for re-ranking by longest alias, for identity keyed on `rxcui` — these are declines
with the measurement cited, not implementations. That is exactly why the ledger exists: a clean
reviewer will propose some of them, because they look obviously right, and `CLAUDE.md` records that
they were measured and rejected.

It returns JSON:

```json
{ "round": 2, "implemented": ["r2-1", "r2-3"],
  "declined": [ { "id": "r2-2", "finding": "…", "reason": "…",
                  "failure_mode_of_declining": "…" } ],
  "runtime_visible": true, "green": "…", "commit": "<sha>" }
```

**A finding may name the PR DESCRIPTION rather than a file, and it can be blocking.** The fixer cannot
edit the description, so the orchestrator applies that one and says which it applied; it still counts as
the round's fix and the round proceeds normally. Do not wave it through as cosmetic: the description is
the durable public rationale attached to the closing of the ticket, no test can fail on a false sentence
in it, and a repo-wide grep for a corrected claim will never reach it. On this loop's second run, round
2's ONLY blocking finding was exactly this — the sixth home of a claim that had just been corrected in
five files, left standing in the body because the fixer had no access and the orchestrator had edited
that same body for something else without re-reading it.

A declined **blocking** finding does not end the loop quietly — see **Termination**.

### 5 — GREEN

`mvn -o clean install` from the **repository root**. Not `-pl api`, not `-pl omod`: the omod unpacks
the *resolved* api artifact over `omod/target/classes` at generate-resources, so a `~/.m2` jar from
another branch shadows the reactor's classes and reddens tests on a drift that is not in the source.
A root install is also what produces the omod the verifier deploys.

A red build is the fixer's problem, inside the round — never a finding for the next reviewer. A round
that pushes red code makes the next round a review of a broken build.

### 6 — VERIFY, when the round touched runtime behaviour

Gate this on what the round actually changed, at most once per round. A round that moved only
javadoc, comments or tests needs no standalone restart. A round that changed behaviour only
observable at runtime does — and where tests structurally cannot answer the question (streaming,
SSE timing, wire serialisation, prompt or latency behaviour) the verifier is not optional: skip it
there and the loop converges on code nobody ran.

The verifier is a fresh subagent that **does the work itself** — it does not delegate to another
skill, and nothing about it depends on one being installed. It is **not** the reviewer, for a specific
reason: a reviewer that deploys is grading its own deploy, so when it hits the stale-omod trap or an
orphaned server, that surfaces as a *finding about the code* — and a wrong blocking finding is what
the loop cannot escape.

Its procedure, and each step is where a specific mistake gets made:

1. **Resolve the target.** Module `id` and `version` from `omod/src/main/resources/config.xml`.
   Standalone home from `$OPENMRS_STANDALONE_HOME`, else the directory holding
   `openmrs-standalone.jar` — never a hardcoded path, since more than one standalone usually exists.
   Port from `<standalone>/openmrs-runtime.properties` (`tomcatport`), which is **not always 8080**.
   State all three before doing anything.
2. **Build.** The round's root `mvn -o clean install` already produced
   `omod/target/<id>-<version>.omod`; note its timestamp. Build under the JDK the pom targets — a
   module on Java 1.8 fails its test gate under a newer default JDK, and the signature is a wall of
   `MockitoException: cannot mock this class … Java: 21` across unrelated tests. That is an
   environment problem: find a matching JDK (`/usr/libexec/java_home -v 1.8`) and rebuild. Never
   "fix" it by skipping tests — that is repairing the artifact, which is forbidden below.
3. **Deploy.** Copy the `.omod` into `<standalone>/appdata/modules/`, overwriting the same name, and
   **remove any other `.omod` of the same module** — the loader reads every `*.omod` and two versions
   of one module is a startup failure, not a warning. `*.omod.bak-*` files are not loaded and are
   harmless clutter, so deleting one never fixes a startup failure; find the rogue `.omod` instead.
4. **Restart — and never a server that was already running when the run began.** Modules load at
   startup, so a running instance picks up nothing until restarted. **"Confirm with the user that it
   is not their active session" is not available to an unattended verifier**, so the rule cannot be
   that: enumerate the standalones on disk, pick one with nothing listening on its port, and if every
   candidate is in use, report `unrepairable` rather than taking one. On this loop's first run the
   only listener was the user's own server with a module deployed into it that morning; left to the
   procedure as previously written, the verifier would have attributed the process and killed it.
   Then launch from the standalone directory, backgrounded, teeing to a log you can tail:
   `java -jar openmrs-standalone.jar -commandline`.
5. **Confirm you are testing this build.** The deployed file's timestamp must match the build from
   step 2. Verifying against a stale `.omod` is the single most common way this step reports on the
   wrong bytes.
6. **Drive the actual behaviour** — the REST call, the query, the page — and capture what came back,
   not that it "looked right". Where the change touches saved data, read the value back out (REST or
   SQL against the bundled DB, creds in `openmrs-runtime.properties`) rather than trusting the
   on-screen state. Prefer the module's own preview/dev endpoints and existing demo data over
   standing up fixtures.

Where the repo ships a per-module playbook for driving its UI, follow it — but the procedure above is
the contract, and a missing playbook is not a reason to skip the step.

**Restore before reporting, like every other agent here** — a verifier mutates less often than a
reviewer but it writes to the standalone, and the same snapshot-and-compare applies to the repo it
built from.

**It owns the environment and repairs it.** Kill the orphaned `llama-server` holding the port, delete
the stale omod and redeploy from the root install, set `log.level`, allow for cold load on the first
query, wait out a slow boot. Do it without asking.

**A repair may only touch the environment, never the artifact under test.** No redeploying the
previous omod, no reverting the round's commit, no flipping a global property to route around the
failing path, no disabling the feature being verified. If what must change to get a green run is the
module's code or its configuration, that is not a repair — it is the finding, and it goes to the
reviewer as one. This line exists because the failure it prevents is silent and fail-open: a module
that throws on startup looks exactly like a broken environment from outside, and a verifier allowed
to put the last working omod back reports green on a build that does not boot.

**Prefer a repair you can undo, and report rather than perform an irreversible one.** The
environment/artifact line says what a repair may touch; it says nothing about whether it can be taken
back. On this loop's first run a verifier raised a standalone's platform from 2.8.7 to 2.9.0-SNAPSHOT
to satisfy the module's require-version — which ran core liquibase against that install's database.
The webapp was backed up; a schema migration is not undone by moving a directory back. So: a
filesystem swap with a kept backup, a process restart, a log level, a copied dependency are all fair.
A schema migration, a destructive DB statement, or anything else you cannot put back is reported as
the reason the environment is unusable, not performed to get a green run.

Bounds: **two attempts per distinct named cause**, then the run aborts and hands back. Kill only
processes it can attribute (`java -jar openmrs-standalone`, `llama-server`) — never a blind kill on
whatever holds a port; the user's own work may be there.

**Repairs PERSIST, so say which of your observations rest on someone else's.** A repair made in
round 1 is still there in round 3, and a verifier that measures a property the repaired environment
has — rather than the one a stock install has — reports it in good faith and is wrong. On this loop's
first run, round 1 added a `log4j2.xml` logger entry to see an INFO line at all; two commits of that
same PR exist because the line is invisible at stock levels, so a later round reporting "present in
the log" would have reintroduced the very claim those commits removed. Hence `inherited_environment`:
name the observations that depend on an earlier round's repairs, separately from your own.

It returns JSON, and every repair is in it even when it worked, because a repair can itself be
evidence — an orphaned server on the port is what confounds a latency comparison:

```json
{ "round": 2, "omod": "<path, sha>",
  "repairs": [ { "cause": "port 8081 held by orphaned standalone (pid 4127)",
                 "action": "killed, restarted", "attempts": 1 } ],
  "classification": "repaired | not-the-environment | unrepairable",
  "inherited_environment": "which observations depend on repairs an EARLIER round made, not this one",
  "observed": "…", "verdict": "works at runtime | does not | could not determine" }
```

**A verifier's observations can falsify a claim the PR makes in prose, and that is a finding rather
than a footnote.** It is running the code, so it sees the units the documentation guessed at. On this
loop's second run the final verifier's live output corrected the ADR's own benefit bullet: "one chip per
prescription" is really one per `orderCarrying` pick, because two orders sharing an unnameable code
collapse onto one partner — visible in a live chip and in no test. Read the `observed` field for what it
contradicts as well as for what it confirms.

`classification: "not-the-environment"` is a verification result and a candidate finding for the next
reviewer. `"unrepairable"` aborts the run. Neither is ever recorded as a blocking finding by the
orchestrator: **an environmental failure is not a review finding**, and if the loop is allowed to
treat one as blocking it will grind rounds against a broken standalone until the cap.

### COMMIT

**Check the branch before you EDIT, and again before you commit.** The commit-time check below is
necessary and not sufficient: by then a wrong-tree edit has already happened, and the only reason it is
recoverable is that nothing was committed yet. Measured on the fourth run of the pipeline that calls this
skill: an agent left the worktree on `main`, four orchestrator edits landed there, and it surfaced only
because the test count dropped by exactly the size of the PR's new test file — a `git branch
--show-current` before the first edit would have caught it immediately, and a commit in between would
have put the work on `main`.

**Re-check the branch immediately before committing.** Step 0's check happens once; agents share
this worktree and one of them running `git checkout` silently redirects everything after it. That
happened on this loop's first run: a reviewer checked out the review branch, the fixer edited files
there, and the round's commit landed on it. Every local signal was green — build passed, tests passed,
the commit existed — and it surfaced only because the push had no upstream. With
`push.autoSetupRemote` enabled it would have pushed a stray branch, the PR would never have received
the fix, and the next round would have reviewed the un-fixed head and re-raised the same blocking
finding until the cap. So: `git branch --show-current` must equal the PR's head ref before `git
commit`, and if it does not, fast-forward the head ref onto the work (append only — never reset,
never force) rather than committing where you stand.

One commit per round, in the repo's existing voice (see `git log`), pushed to the PR head branch.
**Append only — never amend, never force-push.** A reviewer must be able to see the chain of rounds,
and rewriting history under one that is mid-flight is how a round reviews a sha that no longer exists.

### 7 — FINISH

The reviewer found nothing blocking. Apply that round's non-blocking findings under the same fixer
rules, prove green, commit and push. The run ends here: those edits carry no blocking finding by
construction, so no further round is owed.

**Re-derive the PR description against the merging head before marking ready.** Across rounds the body
describes code that later rounds change under it, so the patches this loop applies to it accumulate into
something false: on the fourth run, four consecutive rounds had their top finding in the description, one
of them a sentence an earlier round had itself added. Patching mid-loop is right when a finding names the
body; leaving those patches as the final text is not. Rewrite it whole here, re-measuring every figure in
it rather than carrying one forward.

**A runtime-visible change is not ready until a verifier has run against the head that will merge.**
Step 6 sits on the fix path, so without this a PR whose round 1 found nothing blocking would reach
`gh pr ready` with the standalone never started — and this step's own non-blocking edits are pushed
*after* the last verifier run in every case, so they are unverified even when a round did verify. So
before marking ready: if the change is runtime-visible and no verifier run covers the current head,
run one now. It is the same verifier under the same rules — it repairs the environment, never the
artifact — and `unrepairable` aborts the run here exactly as it does inside a round. **A PR that
could not be verified is not marked ready**; report it as converged-but-unverified and stop.

Then mark the PR ready for review if this run opened it as a draft (`gh pr ready <n>`), and say in the
report that it is now ready, naming the sha the verifier covered.

If applying them turns up something blocking — it happens; a nit's fix exposes a real defect — that
is a new blocking finding: record it, and the loop continues from step 4.

## Editing by script, which is how edits get silently lost

Every role here edits files by running a short script rather than by hand, because the edits are
precise and the files are large. Three failure modes follow, all silent, all measured on this loop's
second run, and all cheap to close:

- **A replacement that matches nothing reports success.** `str.replace` returns the string unchanged
  and the script prints whatever you told it to. One claim survived five hardening cycles that way —
  the script said it had fixed it, and it had not, because the target text wrapped differently than the
  script assumed. So: **assert the target is present before replacing**, and let the assert kill the
  script rather than continuing to the next edit.
- **A slice can span further than you meant and take a neighbour with it.** A replacement bounded by
  "from this javadoc to the next method" deleted a whole test method that sat between them; it compiled
  and the remaining tests passed. So: after any multi-line replacement, **count what should still be
  there** — test methods, symbols, bullet points — and compare against what you expected.
- **A script's own report is not evidence.** Verify by reading the file back, with a grep for the text
  you believe you wrote. The three defects above all announced success.

None of this is optional politeness. Each of the three cost a round or a cycle on the run that found
them, and the third is what caught the other two.

## Correcting a claim means finding every home of it

When a finding is that some statement is false, the statement is rarely in one place. Measured on this
loop's second run: a correction reached one of seven homes, then five of six, then five of six again —
and once, both halves of a single paragraph disagreed with each other after one half was fixed.

So a correction is not finished when the named site is fixed. **Grep the repository for the claim's
distinctive phrasing and fix every hit**, including the ones a reviewer did not name; then grep again
for the phrasing you just wrote, to see how many places now say it. Two homes are easy to forget: the
project's own instruction file, which outranks the code and is the worst place for a half-true rule,
and the **pull request description**, which no repo-wide grep will ever reach.

And a positional cross-reference — "the bullet above", "the section below" — is a claim about layout
that any insertion falsifies. On that same run, inserting a bullet silently re-pointed a neighbouring
bullet's "see the bullet above" at the new text. **Name the target instead of locating it.**

## Termination

> **A `/pr-harden` run is complete when a REVIEW ROUND reports zero blocking findings.**

Not when a round makes no edits. Unlike `/harden`, every round here is *expected* to edit — the fixer
implements the non-blocking findings too — so an edit count can never be the condition, and this is
the one place the two skills' contracts genuinely differ.

Check it, do not estimate it. The count comes from the reviewer's JSON, and it goes in the state file
where something other than you can read it.

`pr-harden-gate.sh` ships next to this file and runs on Stop. It refuses to end the turn while the
newest entry for this directory says `blocking > 0`, and also while it says a run is in flight that
has not yet recorded a review — so a run cannot end by never having reviewed at all. It fails open on
every ambiguity (no file, malformed JSON, no `jq`, unrecognised phase, stale entry, non-numeric
count), so it can only ever add a round you owed; it cannot wedge a session.

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

What is not permitted is ending the run without either the convergence line or an override line, and
**handing the decision back to the user is the disguised form of it**. "Want me to run another
round?" ends the run with blocking findings in it while reading as deference. If a round is owed,
run it.

## State

`~/.claude/pr-harden-state.json`, keyed by the repo directory. **Under `$HOME`, never in the repo** —
an in-repo file would show up in the `git status --porcelain` the round measures, and would be swept
into the round's own commit.

```json
{ "/abs/path/to/repo": {
    "pr": 93, "round": 2, "blocking": 1, "phase": "reviewed",
    "ts": 1755400000, "override": false,
    "awaiting": [ { "agent": "fix r2", "since": 1755400000 } ],
    "reviewed_shas": ["<r1 sha>", "<r2 sha>"],
    "declined": [ { "round": 1, "id": "r1-2", "finding": "…", "reason": "…" } ] } }
```

`phase` is `"init"` before the first review, `"reviewed"` once a reviewer's count is recorded,
`"fixing"` from the moment the fixer is spawned until the next reviewer reports. On `init` and
`fixing` the gate blocks regardless of `blocking`, so leave the last measured value there for the
record. The gate reads `pr`, `round`, `blocking`, `phase`, `ts` and `override`; `declined` and
`reviewed_shas` are the orchestrator's own ledger, carried in the same entry so one write keeps both
in step.

**`awaiting` is not optional bookkeeping — without it an unattended run cannot proceed at all.**
Every phase here delegates to a background subagent, and while one is outstanding the orchestrator
has nothing to do but yield. The gate blocks yields, so a run waiting correctly looks exactly like a
run that quit. So: **record the await immediately before spawning, and clear it the moment the
result arrives.** A non-empty, fresh `awaiting` lets the gate allow the yield — not a loophole,
because the harness re-invokes the orchestrator when the agent completes, so yielding mid-await is
how the run proceeds rather than how it ends.

**Snapshot the worktree before every delegation and compare it after — on ANY terminal outcome.**
`git diff | shasum` before you spawn; the same after the agent returns, fails, stalls or is killed. On a
mismatch, restore from the snapshot and treat it as the agent's residue, never as a finding.

This is not defensive habit, it is the one guard the rest of this skill actively needs. Every reviewer,
fixer and verifier brief here tells the agent to **mutate the production code, run it, restore it**,
because that is the strongest evidence available and it has produced most of the real findings in both
runs of this loop. So the risk is created by the instruction. Measured on the second run: a confirming
agent died mid-response having changed `DrugReference.strictlyContains` from
`start <= other.start && end >= other.end` to `start <= other.start`, and the mutation was still in the
worktree when the notification arrived. It compiled. It read plausibly. Most tests passed. The agent
never got to report, so nothing said it had happened, and it would have gone into the round's commit —
where it silences overdose warnings. It was caught on a hunch about how that agent died, not by any
rule.

Death is not the only path: an agent that simply forgets to restore looks identical from here. And a
hash comparison costs nothing, which is the whole argument for doing it every time rather than when
something feels wrong.

**And the snapshot cannot see the third path, so a rule has to: DO NOT EDIT THE WORKTREE WHILE A
DELEGATED AGENT IS RUNNING.** Commit first, or wait. An agent told to mutate-and-restore restores from
what it READ, so an edit that lands after it read and before it restores is silently reverted — and the
hash comparison is blind to it, because your own concurrent edits make the hash differ legitimately.
Measured on the second run of this loop: a reviewer mutated `orderPartners` to test a hypothesis, put
the file back from its remembered copy, and reverted a guard the orchestrator had added in between. It
compiled, the whole suite passed, and it surfaced only because a test written later failed for a reason
that made no sense. Two consequences: commit before you delegate — a commit is the only thing a
remembered restore cannot undo — and tell agents to restore with `git checkout -- <path>`, never by
rewriting content they remember.

**Tell every agent to restore BEFORE it reports, not after** — a mutation restored late is a mutation
that ships if the agent dies mid-sentence. On the second run, the eleven agents briefed that way all
restored cleanly, verified by hash rather than trusted.

**Clear the await on ANY terminal outcome — completed, failed, stalled, killed — not on a result arriving.**
"The moment the result arrives" says nothing about a result that never will, and agents die: on this
loop's first run six did, to a network drop, two stall watchdogs and a nested-spawn timeout. Four dead
agents left four fresh awaits, and the gate honoured them — measured, it would have licensed a yield
for another 36 minutes with nothing whatsoever running. The harness reports the death, so there is no
excuse for waiting out a timeout. The one-hour bound and the no-`since`-reads-as-dead rule are
backstops, not the mechanism.

**And a dead delegated phase needs a contract, because it is neither an abort condition nor a
finding.** Left undefined, an unattended run ends on the first agent death. The contract: clear the
await, retry the phase **twice**, and change something between attempts — an agent that stalled on
volume gets a leaner brief, one that stalled on nesting is told not to delegate. After the second
retry, stop with the labelled deviation naming the phase and the failure mode, exactly as the round
cap does. A retry is not free of consequence either: on the first run, retrying a reviewer twice is
what exposed the stale-diff-base defect above, because the third brief had to state the base
explicitly.

Write it at every transition:

```bash
python3 - <<'PY'
import json, os, time, pathlib
PR, ROUND, PHASE, BLOCKING, OVERRIDE = 93, 2, "reviewed", 1, False
p = pathlib.Path.home()/".claude/pr-harden-state.json"
s = json.loads(p.read_text()) if p.exists() else {}
e = s.get(os.getcwd(), {})
e.update({"pr": PR, "round": ROUND, "phase": PHASE, "blocking": BLOCKING,
          "ts": int(time.time()), "override": OVERRIDE})
e.setdefault("declined", []); e.setdefault("reviewed_shas", [])
s[os.getcwd()] = e
p.parent.mkdir(parents=True, exist_ok=True); p.write_text(json.dumps(s, indent=2))
print(f"round {ROUND}: phase={PHASE} blocking={BLOCKING}")
PY
```

Recording and clearing an await is its own one-liner, kept apart from the transition write above so
that a spawn never has to restate the phase:

```bash
# usage:  awaiting.py await "review r3"   |   awaiting.py clear
import json, os, sys, time, pathlib
p = pathlib.Path.home()/".claude/pr-harden-state.json"
s = json.loads(p.read_text()); e = s[os.getcwd()]
if sys.argv[1] == "await":
    e.setdefault("awaiting", []).append({"agent": sys.argv[2], "since": int(time.time())})
else:
    e["awaiting"] = []
e["ts"] = int(time.time()); p.write_text(json.dumps(s, indent=2))
```

When the run finishes — converged or overridden — the entry must say so (`blocking: 0`, or
`override: true`). A stale `blocking > 0` left behind is what the 6-hour expiry exists to clean up
after you.

## Where `/harden` sits, and why it is not inside the round

`/harden` is itself a convergence loop with its own Stop gate. Nesting it here would give two
termination contracts arguing on every turn, and each round would have to drive harden to zero edits
before the next reviewer even looked. It also supplies the *weaker* review for this purpose: its
passes run in the context that wrote the code, which is what this loop is built to avoid. And its
Phase 2 polish rewrites lines the next fresh reviewer then reads for the first time — new unreviewed
surface, so it can *raise* the round count.

So harden's Phase 1 discipline is borrowed as instructions (steps 1 and 4 above) and the skill runs
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
- What nothing posted to GitHub means in practice: the PR carries N commits and no review comments.
  Offer to run `pr-review --post` or `--stage` once, at the end, if the user wants the record public.

## Anti-patterns

- **Don't let the fixer own the blocking count.** It is the exit condition; the agent being reviewed
  does not get to set it. A disagreement is a decline, and a declined blocker ends the run as *did
  not converge*.
- **Don't paraphrase the reviewer to the fixer.** The findings go across verbatim, with their
  failure-mode sentences intact. When the run started from a ticket the orchestrator implemented, it
  holds the writing context and is the least neutral participant in the loop — softening a finding on
  the way past is the one way its contamination reaches a round.
- **Don't brief the reviewer with what was fixed.** Only the declined ledger crosses rounds. Telling
  it an area is settled suppresses exactly the re-examination that finds accretion bugs.
- **Don't record an environmental failure as a blocking finding.** The loop will grind rounds against
  a broken standalone until the cap and call it review.
- **Don't repair the artifact to get a green verifier run.** Reverting the round, redeploying the last
  working omod, or flipping a GP to route around the failure is a green report on a broken build.
- **Don't mark a PR ready on an unverified head.** The exit path skips step 6 and the FINISH commit
  lands after it, so "no blocking findings" is not "somebody ran it".
- **Don't skip the verifier on a streaming or timing change** because the tests are green. Those are
  the changes tests structurally cannot answer.
- **Don't spawn a subagent without recording the await,** and don't leave one recorded after its
  result arrives. The first blocks the run's own next yield; the second holds the gate open for a
  run that has actually stopped.
- **Don't amend or force-push a round.** The chain of rounds is the artifact; a rewritten sha is a
  round reviewing code that no longer exists.
- **Don't implement a finding `CLAUDE.md` has measured and rejected.** A clean reviewer will propose
  some of them. Decline with the measurement cited.
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
