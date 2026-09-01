---
name: pr-harden
description: Harden an open pull request by cycling clean-context review rounds against it — a fresh agent reviews the pushed head, a second fresh agent implements every finding it agrees with and declines the rest on the record, the build is proved green, the change is verified on a real standalone where runtime behaviour is at stake, and the round is committed and pushed. The cycle repeats until a review round reports zero blocking findings. Use when a PR should be hardened by reviewers who have never seen it being written. Trigger phrases include "harden this PR", "review and fix the PR until it's clean", "cycle review rounds on PR N".
argument-hint: <pr-number-or-url> [--max-rounds N] [--no-verify]
version: 0.15.0
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
  naming a *different* PR is a stale run: report it and ask before clearing it — **but an unattended
  run has nobody to ask**, which is settled for the verifier at step 6 and settles the same way here.
  The gate already draws the line the ask stood in for: past `STALE_AFTER` (6h) it treats a run as
  abandoned rather than in flight. So take over an entry past that bound, or one whose run recorded a
  terminus (`blocking: 0` or `override: true`), and say in the report which PR's entry you cleared and
  what it said; refuse a fresher entry claiming a live round on another PR rather than adopting it,
  since two runs in one checkout is what the ask was preventing.

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

**Compare that sha against the last entry of `reviewed_shas` before you spawn anything.** The state
file has recorded these since the skill was written and nothing has ever compared them, so the
cheapest check in the loop was sitting unused. If the new head EQUALS the previous round's, the loop
is about to spend a round re-reviewing bytes it has already reviewed — and a reviewer given identical
input will either repeat its findings, which reads as an unfixed defect, or find nothing, which reads
as convergence. Both are wrong and neither looks wrong. So stop and establish why before spawning:
the round pushed no commit (the fixer declined everything — that is a *did not converge*, see
**Termination**, not a free round), or the push had not landed when the fetch ran, or the ref was
created outside a round at all.

Two pairs of refs across four earlier runs each shared a sha (`pr-288-r1`/`r2`, `pr-291-r2`/`r3`).
**Which cause produced them was never established**, so this is a guard, not a diagnosis — worth
having because the cost of the case it catches does not depend on how the case arose.

**Tell the reviewer what to diff against, and never let it be a local branch name.** Fetch the base
too and name it explicitly: `git fetch origin main` then `git diff origin/main...pr-<n>-r<round>` — or
better, the PR's own base from `gh pr view <n> --json baseRefName`. A local `main` is stale on any
machine that has not pulled, and the merge base then reaches back to whenever it last did. Measured on
this loop's first real run: `main...` produced **13,602 lines against an 864-line change**, most of it
other people's commits. That is the worst failure this design has produced, because it is silent — the
reviewer returns well-formed JSON with a legitimate-looking blocking count, about code the PR never
touched, and a fixer then acts on it.

**Compare the base you just fetched against the one the previous round saw, and where it moved, re-check
what this branch says about the code the move touched.** Two classes, and git flags neither.

The first is **an identifier this branch allocated from a sequence `main` also appends to.** An ADR
decision number is the observed instance: the branch takes the next free one when it writes the entry, and an upstream PR
merged since can have taken the same one. Observed on three consecutive runs, twice within a single run.
When it has moved, correct every home of the old value and not just the one you noticed — they sit in
javadoc and test names, not only in the ADR file — and search for the number itself rather than for a
phrasing you wrote, which is how a renumbering sweep left three sites standing on #238.

The second is **a count or a structural claim that git merges cleanly and silently falsifies.** On #340
`main` refactored three emission sites onto a shared writer; the controller auto-merged correctly and
three of this branch's claims became false — the ADR's per-site mutation recipe, a test class's javadoc
and a wire paragraph, all of which described the three sites as naming the serializer directly, and
"nothing in the merge flagged them". On #337 "two cases fail on a floor of nine" became five when the
merge brought seven more cases into that file, in four homes, and both of round 1's blocking findings
were counts the merge had falsified — a round. So grep this branch's own claims about the structures
`main` changed, and RE-MEASURE each on the merged tree rather than re-reading it for coherence; a
coherent sentence about a structure that moved is the failure mode, not the check.

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
- **how to attack a guard whose subject is TEXT or SHAPE** — a source scan, a class-file scan, an
  architecture guard, a build-time assertion. Deleting the thing it guards is the weak mutation and the
  one its author already tried; the strong one is a form that is **semantically the defect but textually
  not the obvious edit**. Measured on this loop's seventh run, and it produced the run's only blocking
  finding: a guard asserted that the gate's right-hand side *contained* the flag's name, so
  `order != null || namesADrug ? order : null` passed it — that names the flag and means
  `namingOrder = order` for every non-null order, i.e. the pre-fix state restored, with all 1350 tests
  green. Deleting the gate was caught; the equivalent rewrite was not. Ask of any such guard: what is the
  cheapest edit that satisfies its assertion and still breaks the property? A plausible slip is worth more
  than a contrived one — that one is an `&&`/`||` typo in a defensive null check.

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
- **And when you NAME a guard, mutate the thing and read which case actually reddens.** An attribution
  is as falsifiable as an assertion and fails the same way: on the #302 run three sites named a test as
  the guard for a branch it could not observe — it took a different code path — and twice the correct
  attribution was already written in that test's own comment. "Guarded by X" is a claim; check it.
- **If you ADD a guard, prove which case reddens — deleted, its arms swapped, its comparison loosened,
  or rewritten in a semantically equivalent way.** `harden`'s Termination carries this same obligation at
  cycle close; this is it at the moment the guard is written, and it was scoped to text and shape until
  two blocking findings on the #308 run fell outside that scope at a round each: a guard the change
  added, unpinned — deleting it left the whole suite green — and a trim normalisation unpinned against an
  equivalent rewrite. Step 1's reviewer brief stays narrower on purpose, because what it teaches is the
  string-versus-property attack and that is specific to text and shape; this obligation is not.
  **For a guard over TEXT or SHAPE,** one gap is between the property it means and
  the string it matches, and that gap is invisible from the assertion's own side. Assert the SHAPE the
  code must have rather than that it mentions the right identifier — measured, a guard requiring only that a
  right-hand side *contained* the flag's name accepted `order != null || namesADrug ? order : null`,
  which restores the defect with the whole suite green. State in the guard's javadoc which shapes each
  channel really catches, and never write that a shape is "caught behaviourally" without running it.
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

  **And the rule is not about tallies — it is about claims you cannot check.** A universal or an exhaustive characterization is the same defect in different grammar, and it slips past a reader watching for digits: *any*, *only*, *exactly*, *all*, *never*, *the whole*, *cannot*. Measured on a `/harden` run of #298, five such claims in three consecutive cycles, each written to correct the previous cycle's false claim and each false in turn — "it only re-admits `M01AE0`" (it re-admits any single trailing digit), "exactly the two levels the ladder is known to be handed" (nothing on the path validates a code's shape), and three more of the same shape; `harden`'s own anti-pattern carries all five. So before writing one about code you just wrote, spend one attempt trying to falsify it; prefer stating what the thing DOES over what it excludes; and name the residue rather than claiming there is none.
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

   **When `$OPENMRS_STANDALONE_HOME` is set it is not a hint, it is the assignment.** Do not search,
   do not compare it against what is running, do not pick a different one because this one looks
   busy. The pool driver sets it per run precisely so that concurrent runs each have an instance of
   their own, and a run that "helpfully" takes a quieter one takes a sibling's.
2. **Build.** The round's root `mvn -o clean install` already produced
   `omod/target/<id>-<version>.omod`; note its timestamp. Build under the JDK the pom targets — read
   `maven.compiler.target` (or `<java.version>`) and resolve THAT version. The version in a command
   here is an example, not the value. A module on Java 1.8 fails its test gate under a newer default
   JDK, and the signature is a wall of `MockitoException: cannot mock this class … Java: 21` across
   unrelated tests; for that one `/usr/libexec/java_home -v 1.8` is the fix. Read from the other end
   the mismatch has its own signatures: `invalid target release: 11` is a Java-11 pom built under JDK
   8 (#266, one repair attempt spent reaching for 1.8 because this step named it), and `No compiler is
   provided in this environment` means the home you resolved is a JRE rather than a JDK (#255, where
   `java_home -v 1.8` resolved this box's applet-plugin JRE for a pom targeting 11). Each of these is
   an environment problem. Never "fix" one by skipping tests — that is repairing the artifact, which
   is forbidden below.
3. **Deploy.** Copy the `.omod` into `<standalone>/appdata/modules/`, overwriting the same name, and
   **remove any other `.omod` of the same module** — the loader reads every `*.omod` and two versions
   of one module is a startup failure, not a warning. `*.omod.bak-*` files are not loaded and are
   harmless clutter, so deleting one never fixes a startup failure; find the rogue `.omod` instead.

   **Then delete `<standalone>/appdata/.openmrs-lib-cache/<id>/`, because replacing the `.omod` does not
   reliably replace what runs.** OpenMRS expands a module into that directory and a redeploy under the
   same name does not always re-expand it: on FM2-700 it held both the released api jar and the new
   snapshot, and the stale one shadowed the fix; on #340 the first boot ran week-old controller classes
   while the omod timestamp, the module status endpoint and the cache's own marker file all read
   current. It is a cache, so there is nothing to preserve.
4. **Restart, and just take YOUR standalone.** Modules load at startup, so a running instance picks
   up nothing until restarted. **These are throwaway demo instances** (owner's instruction,
   2026-08-27): stop the one you resolved in step 1, running or not, without confirmation. Do not
   enumerate candidates hunting for an idle port, do not stop to attribute pids, and never report
   `unrepairable` because it was in use — "in use" is not a blocker here. Launch from the standalone
   directory, backgrounded, teeing to a log you can tail:
   `java -jar openmrs-standalone.jar -commandline`.

   **"Whichever one you need" means the one you were given.** That phrase used to be unqualified, and
   unqualified it is a licence to stop a server another run is mid-query against — which became
   reachable the moment the pool could work two tickets at once. The instance is yours; every other
   one on the machine is somebody's.

   *This rule used to say the opposite* — never restart a server that was already running — written
   after a run nearly killed what it took to be the user's own session. That caution was wrong about
   this environment and cost a later run real time: it hunted for a free port and prepared an
   `unrepairable` abort on the only standalone there is. Kept as history so nobody reinstates it from
   the same reasoning; if you are ever in an environment where a standalone is NOT disposable, that
   is a fact the owner has to state, not one to infer from a port being busy.
5. **Confirm you are testing this build — the timestamp proves the FILE, and the file is not what
   runs.** The deployed `.omod`'s timestamp must match the build from step 2; that is necessary and, per
   step 3's lib-cache paragraph, not sufficient. Where the change is one you can name in a class, prove
   the bytes: hash the loaded class under `.openmrs-lib-cache/<id>/` against the same entry in the built
   omod. The three signals step 3 names all read current over stale bytes, so none of them is the proof.
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

**Unless `$CLAUDE_PIPELINE_SLOT` is set, in which case it owns ITS SHARE of the environment.** That
variable is the pool driver telling this run it has co-tenants — other `resolve-ticket` runs working
other tickets on this machine, right now. What stays yours: the standalone at
`$OPENMRS_STANDALONE_HOME`, your own worktree, and the maven repository `$MAVEN_ARGS` points at. What
stops being yours is everything the repairs above reach for by *symptom* rather than by name — a
process holding a port you did not resolve, a `java` you cannot attribute, and above all the shared
inference server, which every co-tenant is mid-query against and which nothing here restarts. Repair
what you were given; report the rest as an environment finding and say a co-tenant may own it. The
un-scoped version of this paragraph is correct alone and destructive beside a sibling, and the
difference is not visible from inside a run — only the variable says which world you are in.

**A repair may only touch the environment, never the artifact under test.** No redeploying the
previous omod, no reverting the round's commit, no flipping a global property to route around the
failing path, no disabling the feature being verified. If what must change to get a green run is the
module's code or its configuration, that is not a repair — it is the finding, and it goes to the
reviewer as one. This line exists because the failure it prevents is silent and fail-open: a module
that throws on startup looks exactly like a broken environment from outside, and a verifier allowed
to put the last working omod back reports green on a build that does not boot.

**Irreversibility is not a constraint on a standalone.** A schema migration, a platform bump that
runs core liquibase, a destructive DB statement — all fair if they unblock the run. The instances and
their data are disposable, so there is nothing to put back. *This paragraph used to require the
opposite,* after a verifier raised a standalone's platform and ran liquibase against its database;
that was recorded as a hazard and is not one here. **The environment/artifact line above still
binds** — irreversibility is fine, repairing the ARTIFACT under test never is.

**Do NO data housekeeping, in either direction.** Do not back up or snapshot a standalone's data,
do not work carefully to avoid losing it, and — the half that actually costs time — **do not delete
demo data you created in order to restore the original state**. Extra test data is useful; cleaning
it up is pure waste.

**The one thing that IS restored: global properties.** Any `global_property` a run changes goes back
to the value it had when the run started — as-found, not the `config.xml` default, which is often
different. They are configuration, not data: a left-behind override silently changes what every later
step measures, which is how an A/B ends up comparing the wrong two things.

Bounds: **two attempts per distinct named cause**, then the run aborts and hands back. Kill whatever
you need to (`java -jar openmrs-standalone`, `llama-server`, whatever holds the port).

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

**Delete the run's own `pr-<n>-r<round>` refs.** They are local fetch copies of the PR head with no
upstream, worth nothing once the round is over and re-fetchable from `pull/<n>/head` while GitHub
retains it. The loop creates one per round and went four completed runs without removing any, leaving
refs on merged PRs that clutter every `git branch` a human or an agent runs afterwards. The check is
`git branch --list 'pr-*'` in a repo this loop has worked, read for the `pr-<n>-r<round>` shape. This
passage used to enumerate the refs instead; they are gone from the checkout it measured, which is why it
names the method now. Delete them here rather than at the top of the next run, because the next run may
be in a different repo or may never happen.

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

So a correction is not finished when the named site is fixed. **Search for the claim's rarest single
TOKEN, over the whole tree rather than over the docs**, and fix every hit, including the ones a
reviewer did not name; then grep again for the phrasing you just wrote, to see how many places now say
it. Searching the PHRASING is what leaves the last home standing, and it fails two different ways: a
phrase the file's own formatting has split — markdown emphasis inside it, a line break falling between
a quantifier and its noun — does not match what you typed, while a home that is a DATA file rather
than a doc is missed by scope alone. Both were paid for on the #266 run, where the survivors were
found a cycle apiece and each was hidden by a mechanism the one before it had not used; treat no list
of those mechanisms as closed. Two homes are easy to forget: the project's own instruction file, which
outranks the code and is the worst place for a half-true rule, and the **pull request description**,
which no repo-wide grep will ever reach.

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

**Raising the DEFAULT cap is a third move, and the runs that took it read a signal first.** #308 raised
it with one real finding outstanding, on the ground that rounds 1-5 had each found a genuinely different
defect; #315 extended it twice, on the ground that the findings were shrinking round on round. Both then
converged. So what licenses a raise is evidence the loop is still WORKING rather than spinning — a
different defect each round, or findings shrinking — and a round that re-raises what an earlier one
raised is spinning: take the override instead. Raise it a round or two at a time, re-read the signal each
time, and say what you raised it to, because a raise nobody states turns a *did not converge* into a
*converged* silently. **Do not raise a cap the caller set:** `--max-rounds N` is their budget, and under
`ticket-pool` a session that outruns `ticket.timeout_seconds` is killed, which leaves the checkout dirty
and skips every remaining ticket in the pool. A labelled `draft` is by far the cheaper outcome.

What is not permitted is ending the run without either the convergence line or an override line, and
**handing the decision back to the user is the disguised form of it**. "Want me to run another
round?" ends the run with blocking findings in it while reading as deference. If a round is owed,
run it.

## State

`~/.claude/pr-harden-state.json`, keyed by the WORKING TREE's physical path — `pwd -P`, symlinks
resolved, which is what both hooks key on and what `gate-state` writes. Resolved on both sides or the
two disagree wherever a path component is a symlink (`/tmp` on macOS, a symlinked home), and
"no entry" is the gate's fail-OPEN case: a run with findings outstanding stops and nothing says why.
Measured — the hook suites reported 4 of 12 and 3 of 11 cases silently inverted before both sides
resolved.

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
    "declined": [ { "round": 1, "id": "r1-2", "finding": "…", "reason": "…" } ] } }
```

`phase` is `"init"` before the first review, `"reviewed"` once a reviewer's count is recorded,
`"fixing"` from the moment the fixer is spawned until the next reviewer reports. On `init` and
`fixing` the gate blocks regardless of `blocking`, so leave the last measured value there for the
record. The gate reads `pr`, `round`, `blocking`, `phase`, `ts`, `override`, `owner`, `awaiting`, `unattended`
and `mode` — all ten; `declined` and `reviewed_shas` are the orchestrator's own ledger, carried in the
same entry so one write keeps both in step. **`mode` has no writer today**, which is a defect and not a
spare field: the gate has a distinct `--plan-only` message behind it, so a plan-only run instead gets
the generic `building` one, telling it to "continue the phases" through implementation and a draft PR —
exactly the work its own mode excludes. Either something writes `mode` or that branch goes. `reviewed_shas` is not only a record: step 1 compares the incoming head against its last
entry, because two rounds reviewing one sha is a round spent on bytes already reviewed.

**`owner` is what tells your entry from somebody else's, and it is not the unattended marker's job.**
This file is keyed on the CHECKOUT, so a pool run and an interactive session in the same directory read
one entry. Measured live 2026-08-26: an interactive session was stopped with "resolve-ticket is mid-run
and has not opened its pull request yet" over an entry belonging to a live `claude -p /resolve-ticket`
run, and both remedies the block offers damage that run — `override: true` disarms its gate for the rest
of its life, and "continue the phases" puts a second session in one worktree. So stamp `owner` with
`$PPID`, which from a tool shell is this session's own `claude` process; the gate allows the stop when
that pid is alive and is not an ancestor of the stopping session, and when it is DEAD, since no session
can advance a run whose writer is gone. An UNSTAMPED entry is held to the contract exactly as before,
so nothing is relaxed on a missing field.

**Do not answer this question with the unattended marker.** The first version of that check inferred
entry ownership from marker ownership, and review measured the cost within the hour: a live foreign
marker allowed EVERY block path, so an interactive `/harden` or `/pr-harden` in a pool-worked checkout
silently lost its own termination contract — `edits: 7` allowed, `phase: fixing` allowed. The marker
answers whether THIS session is unattended; the two questions coincide only in the incident above.
`gate-test.sh`'s "foreign marker but the entry is OURS -> block" is that regression, pinned in both
suites. What none of it fixes: two runs in one checkout still share one entry and the later writer
wins — this tells one session's entry from another's, it does not give them one each.

**`awaiting` is not optional bookkeeping — without it an unattended run cannot proceed at all.**
Every phase here delegates to a background subagent, and while one is outstanding the orchestrator
has nothing to do but yield. The gate blocks yields, so a run waiting correctly looks exactly like a
run that quit. So: **record the await immediately before spawning, and clear it the moment the
result arrives.** A non-empty, fresh `awaiting` lets the gate allow the yield — not a loophole,
because the harness re-invokes the orchestrator when the agent completes, so yielding mid-await is
how the run proceeds rather than how it ends.

**That last clause holds only for an ATTENDED session, and taking it as universal killed two
unattended runs.** A `claude -p` process exits when its turn ends, so nothing re-invokes it: there
the yield IS the death, and the gate's own allow made it silent (allowing is `exit 0`). Measured
2026-08-26 — #297 recorded `awaiting=[{agent: "refute plan #297 pass 1"}]`, narrated *"dispatched the
refutation gate. Here is where things stand"*, and ended at 51 turns with no PR and its plan and
reproduction discarded; #310 died with the same signature in `/harden` pass 3, at 1365 turns and
$76.72. So **an unattended run never ends a turn with an agent outstanding — collect it in the same
turn.** The gate enforces it now, scoping the allow to attended sessions off a pid-stamped marker the
driver holds for the life of the run; the rule is stated here as well because a gate can only refuse
a stop after the decision to stop has been made, and that decision is what costs the run. And do not
read a stream with no gate text in it as evidence the gate never ran: hooks DO reach `-p` sessions,
probed the same day, feedback delivered and captured.

**Collecting in the same turn means the `Agent` call RETURNS the report — never a poll
afterwards.** Several `Agent` calls in ONE message run concurrently, so a wave keeps its
parallelism while each result is that agent's own report; Step 3's refutation gate already
collects this way and its agents run ten to twenty minutes, so length is not what forces a
background spawn. Launching async and then blocking on `TaskOutput` collects nothing extra — the
report arrives by itself in the completion notification's `<result>` — while `TaskOutput` is
DEPRECATED for an agent task precisely because its output file is a symlink to the agent's whole
JSONL transcript: each poll injects a truncated window of raw agent chatter, the next poll injects
a different window rather than the rest of the first, and the orchestrator re-sends all of it on
every later turn. Measured 2026-09-01 over the three tickets of twenty that reached for it: 49
polls carried 953,119 bytes no round ever used, 23 of them at the 32 KB truncation cap; on one of
those runs the two agents that WERE collected synchronously returned their whole reports in 9,352 and
9,956 bytes, so one report is a third of a single poll's window and a polled agent costs several
windows. Where you need to block on something that is NOT an agent — a build, a server coming up —
that is a background Bash task, whose output file is its stdout and is safe to read.

**And that marker now decides OWNERSHIP as well as attendedness, because the gate state is keyed on the
checkout and not on the session.** Measured live 2026-08-26: an interactive session in a checkout the
pool was working was stopped by a `phase: building` entry belonging to a different live
`claude -p /resolve-ticket` run, and both remedies the block offered damaged that run — `override: true`
disarms its gate for the rest of its life, and "continue the phases" puts a second session in one
worktree. The gate now allows the stop where the marker's pid is alive and is **not** an ancestor of the
stopping session. Two consequences to keep. Ownership is only ever established POSITIVELY, so an
indeterminate answer keeps the block — losing the unattended guard back is the more expensive
direction. And the state file is still keyed on `$PWD` alone, so two runs in one checkout share one
entry and the later writer wins: this narrows a false positive and does not make the state
multi-tenant.

**Snapshot the worktree before every delegation and compare it after — on ANY terminal outcome.**
`git diff | shasum` before you spawn; the same after the agent returns, fails, stalls or is killed. On a
mismatch, treat it as the agent's residue, never as a finding. **The hash DETECTS; it cannot restore —
a shasum is not an artifact you can apply.** What makes the residue recoverable is the commit rule
below, which is why that rule binds anything that mutates the worktree and not only delegation.

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
that made no sense. Two consequences: commit before anything mutates the worktree — before you
delegate, and before your OWN measurement probe, because a commit is the only thing a remembered
restore cannot undo — and tell agents to restore with `git checkout -- <path>`, never by
rewriting content they remember.

**`git checkout -- <path>` is the right restore only where the file carries nothing but the mutation.**
It restores HEAD, so in a worktree holding uncommitted intended work it silently discards that too:
measured on the #302 run, an orchestrator's own mutation probe undone that way took four production
edits with it, and the empty `git diff --stat` afterwards read as "restored" rather than "reverted", so
a commit shipped whose message described changes absent from its diff. The axis is the FILE's state,
not who typed the command — which is the whole reason the commit rule above comes first. When it does
happen, say where to look: the registered PreToolUse hook copies modified tracked files outside the repo
before the destructive command runs, best-effort and bounded, and prints the destination and count —
trust that printed message rather than assuming the file is there. It reaches only the agent whose call
triggered it, and in both incidents of this window the loss was found by somebody else: on #256 by a
later agent diffing the commit against its claim, on #263 by the orchestrator grepping.

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
volume gets a leaner brief, one that stalled on nesting is told not to delegate. **A session or quota
429 is neither of those, and the lever that has worked is a cheaper agent rather than only a shorter
one** — on #238 round 1's fixer "died instantly on a session rate limit (429)" and a retry on a
different model succeeded; on #336 the round-1 reviewer died the same way and completed on a smaller
model with a leaner brief. Either may be what is available. The residue: #339 met a limit that "will
refuse every retry for hours", where nothing here is known to help and the two attempts are spent on a
condition that has not changed. After the second
retry, stop with the labelled deviation naming the phase and the failure mode, exactly as the round
cap does. A retry is not free of consequence either: on the first run, retrying a reviewer twice is
what exposed the stale-diff-base defect above, because the third brief had to state the base
explicitly.

Write it at every transition:

```bash
~/.claude/pipeline/gate-state --owner $PPID pr-set --pr 93 --round 2 --phase reviewed --blocking 1
```

`--owner $PPID` is this session's own claude process, which is how both gates tell your entry from
one a co-located session left in the same directory. Add `--override --reason "…"` only when taking
the labelled override. `declined` and `reviewed_shas` have their own subcommands — `gate-state
declined --round 1 --id r1-2 --finding "…" --reason "…"` and `gate-state reviewed-sha 3085ff02` — so a
transition write never has to restate them and cannot drop them.

**Why a helper rather than the inline `python3` this used to be.** The read, the change and the write
are one critical section, and they were not: every writer read the whole file, changed its own entry
and wrote the whole file back, unlocked. With one run on the machine that is fragile; with several it
is lossy, and lossy in the direction that kills a run — the entry that disappears is somebody's
`awaiting`, and their gate then sees a run that quit with agents outstanding. Measured with 20
concurrent writers to 20 different working trees: the inline form kept **3 of the 20**, valid JSON
throughout, nothing raised. `gate-state` holds an exclusive `flock` across both state files and
writes atomically. Do not retype the mechanism.

Recording and clearing an await is its own one-liner, kept apart from the transition write above so
that a spawn never has to restate the phase:

```bash
~/.claude/pipeline/gate-state --owner $PPID await "review r3" --only pr
~/.claude/pipeline/gate-state --owner $PPID clear-await --only pr
```

Kept apart from the transition write above so that a spawn never has to restate the phase. Drop
`--only pr` and it writes both gates at once, which is what a nested `/harden` cycle needs.

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
