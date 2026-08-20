---
name: pr-harden
description: Harden an open pull request by cycling clean-context review rounds against it — a fresh agent reviews the pushed head, a second fresh agent implements every finding it agrees with and declines the rest on the record, the build is proved green, the change is verified on a real standalone where runtime behaviour is at stake, and the round is committed and pushed. The cycle repeats until a review round reports zero blocking findings. Use when a PR should be hardened by reviewers who have never seen it being written. Trigger phrases include "harden this PR", "review and fix the PR until it's clean", "cycle review rounds on PR N".
argument-hint: <pr-number-or-url> [--max-rounds N] [--no-verify]
version: 0.1.0
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

Fetch and review the **pushed** head, not the local worktree:
`git fetch origin 'pull/<n>/head:pr-<n>-r<round>'`. Record the sha.

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
  wrote, delete the unsupported clause rather than replacing it with a better-sounding one.

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
4. **Restart.** Modules load at startup, so a running instance picks up nothing until restarted.
   Find the listener with `lsof -iTCP:<port> -sTCP:LISTEN -n -P` and confirm it is not a server the
   user is actively on before stopping it; then launch from the standalone directory, backgrounded,
   teeing to a log you can tail: `java -jar openmrs-standalone.jar -commandline`.
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

Bounds: **two attempts per distinct named cause**, then the run aborts and hands back. Kill only
processes it can attribute (`java -jar openmrs-standalone`, `llama-server`) — never a blind kill on
whatever holds a port; the user's own work may be there.

It returns JSON, and every repair is in it even when it worked, because a repair can itself be
evidence — an orphaned server on the port is what confounds a latency comparison:

```json
{ "round": 2, "omod": "<path, sha>",
  "repairs": [ { "cause": "port 8081 held by orphaned standalone (pid 4127)",
                 "action": "killed, restarted", "attempts": 1 } ],
  "classification": "repaired | not-the-environment | unrepairable",
  "observed": "…", "verdict": "works at runtime | does not | could not determine" }
```

`classification: "not-the-environment"` is a verification result and a candidate finding for the next
reviewer. `"unrepairable"` aborts the run. Neither is ever recorded as a blocking finding by the
orchestrator: **an environmental failure is not a review finding**, and if the loop is allowed to
treat one as blocking it will grind rounds against a broken standalone until the cap.

### COMMIT

One commit per round, in the repo's existing voice (see `git log`), pushed to the PR head branch.
**Append only — never amend, never force-push.** A reviewer must be able to see the chain of rounds,
and rewriting history under one that is mid-flight is how a round reviews a sha that no longer exists.

### 7 — FINISH

The reviewer found nothing blocking. Apply that round's non-blocking findings under the same fixer
rules, prove green, commit and push. The run ends here: those edits carry no blocking finding by
construction, so no further round is owed.

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
    "reviewed_shas": ["<r1 sha>", "<r2 sha>"],
    "declined": [ { "round": 1, "id": "r1-2", "finding": "…", "reason": "…" } ] } }
```

`phase` is `"init"` before the first review, `"reviewed"` once a reviewer's count is recorded,
`"fixing"` from the moment the fixer is spawned until the next reviewer reports. On `init` and
`fixing` the gate blocks regardless of `blocking`, so leave the last measured value there for the
record. The gate reads `pr`, `round`, `blocking`, `phase`, `ts` and `override`; `declined` and
`reviewed_shas` are the orchestrator's own ledger, carried in the same entry so one write keeps both
in step.

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
