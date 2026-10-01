# pr-harden — the evidence behind the rules

`SKILL.md` states each rule as a run needs it. This file keeps what the rules rest on: the incidents,
measurements and history that `SKILL.md` carried until 0.35.0, moved here verbatim so that a run no
longer loads them and no measurement is lost. Headings match `SKILL.md`'s. Each entry quotes the rule
it supports as `SKILL.md` states it, then the passage as it stood in 0.34.0, so an entry can repeat the
words of its rule. From 0.36.0 the rules R2-01 to R2-08 quote are in `fixer.md`, and those R2-14 to
R2-20 and R2-22 quote are in `verifier.md`; their entries stay under *4 — FIX* and *6 — VERIFY*
here. From 0.37.0 the rules R1-12 and R1-13 quote are in `reviewer.md`, and their entries stay
under *1 — REVIEW* here.

A run does not need this file. Read an entry before changing or deleting the rule it supports: a
measured rule is not deleted without the measurement that retires it (`skill-retro`, Step 4).

## Roles — and the one rule that makes them roles

*Cut down in 0.36.1, R5-01 — the rule it supports:* “refuses such a call, so this is enforced rather than asked”

> **And never pass `model`.** A per-call override beats both the agent definition's frontmatter and
> settings.json, so it is the strongest of the levers and the only one a running round can pull on
> its own initiative — the others are set outside any session. It is how one of these agents ends up
> weaker than the run that spawned it — and the round whose verdict it returns is the round that
> decides whether the loop exits. A
> `PreToolUse` hook (`~/.claude/hooks/no-subagent-model-override.sh`) refuses such a call, so this is
> enforced rather than asked; if a different model is genuinely wanted, that is the user's call, not a
> lever to reach for mid-round.

## Step 0 — Guards, before any round

*Cut down, R1-01 — the rule it supports:* “A reused worktree inherits the previous PR's ledger”

- An entry for this repo in `~/.claude/pr-harden-state.json` is **this run's own** when its `pr`
  matches the PR being hardened, or when it has no `pr` yet — that is the handoff `resolve-ticket`
  writes, and you adopt it and carry its `round`, `declined` and `reviewed_shas` forward. An entry
  naming a *different* PR is a stale run: report it and ask before clearing it — **but an unattended
  run has nobody to ask**, which is settled for the verifier at step 6 and settles the same way here.
  The gate already draws the line the ask stood in for: past `STALE_AFTER` (6h) it treats a run as
  abandoned rather than in flight. So take over an entry past that bound, or one whose run recorded a
  terminus (`phase: reviewed` with `blocking: 0`, or `override: true`), and say in the report which
  PR's entry you cleared and what it said; refuse a fresher entry claiming a live round on another PR
  rather than adopting it, since two runs in one checkout is what the ask was preventing.
  A reused worktree inherits the previous PR's ledger: on #337/PR375 the entry carried three
  `reviewed_shas` from that ticket's earlier run on PR #345 in the same worktree. `pr-set` now drops
  `reviewed_shas`, `verified_shas` and `declined` when the PR number changes and prints the prior
  number and what it dropped. That print is a backstop, not this step's report — when you take over
  or clear an entry
  naming another PR, read it with `gate-state clear --only pr --json`, which prints the whole entry it removed,
  and say in the report which PR's it was and what it said.

*Cut down, R1-02 — the rule it supports:* “Count its earlier rounds, from its description and the run records whose header names this PR, toward *1 — REVIEW*'s round 4”

- **A PR with rounds from an earlier run can arrive with no entry to adopt.** `pool-run` clears the
  entry when it makes a worktree, so a PR worked again in a worktree it has just made starts with
  none, as #514/PR524's second and third runs did under `pool-run --claim`. Count its earlier rounds,
  from its description and the run records whose header names this PR, toward *1 — REVIEW*'s round
  4; the cap stays this run's budget. PR524's second run restarted at a full round 1, and its round 2,
  the PR's sixth, sent non-blocking r2-2 to a fixer whose fix introduced blocking r3-1. Counted, that
  round was blocking-only: r2-1 and r2-3, real holes that fixer implemented in part, would have been
  notes, and on round 2's recorded count of 0 the run would have ended there, before its rounds 4 and
  5 found r4-1 and r5-1, false reports round 2's reviewer had not raised.

## The round

### 1 — REVIEW

*Cut down, R1-03 — the rule it supports:* “From the PR's round 4 on, counting rounds earlier runs made (Step 0), the round is BLOCKING-ONLY”

This is FINISH's own rule — *"a round that implements nits and then re-reviews can never
converge, because a review is expected to produce nits"* — applied before FINISH rather than
only at it, and the reason it has to start earlier is measured. On a 9-round run of
`openmrs-module-chartsearchai` PR #465 (2026-09-21), 8 of the 12 blocking findings were
introduced by an earlier round of that same loop, and **three of them — r7-1, r7-3 and r8-1 — by
an edit that implemented a NON-blocking finding**, which is the edit this rule stops the fixer
making; the other five came from blocking fixes, which it does not touch. The same run carries the
cost: r6-6, a non-blocking finding in the PR's own defect class whose fixer verified the hole was
real, would have stayed unfixed, named in the PR description and the run record.

*Cut down, R1-04 — the rule it supports:* “Tell it not to spawn subagents of its own”

**Tell it not to spawn subagents of its own.** `pr-review` Step 3 asks for an adversarial refutation
pass, which reads as an instruction to delegate; nested delegation is what killed the first reviewer
on this loop's first run, mid-refutation. The independence is already supplied one level up — the
reviewer IS the independent agent — so a second layer buys a failure mode and nothing else. Have it
argue both sides in its own reasoning, or mark the finding non-blocking.

*Cut down, R1-05 — the rule it supports:* “A brief names that SHA, and only an agent with its OWN checkout is told to check it out”

**A brief names that SHA, and only an agent with its OWN checkout is told to check it out.** The
round's ref name is a shared resource: on #370 two reviewers in isolated worktrees could not `git
fetch origin pull/371/head:pr-371` because a sibling agent's worktree already held that name, and
briefing the SHA fixed it at no round or cycle cost. But a `git checkout` by an agent that has no
checkout of its own lands on the tree this run commits from: on #446 the next phase's fixer then
edited in detached HEAD, and reattaching had to get past a leftover agent worktree holding the branch
(`--ignore-other-worktrees`); on #444 a reviewer that died on a 429 left it detached, and every later
round passed `isolation: "worktree"`; #247 paid the same detour twice. So either isolate it, or brief
that the worktree is already at the head and nothing is to be checked out — #446's rounds 2 and 3 took
the second and the problem did not recur.

*Moved, R1-06 — the rule it supports:* “Compare that sha against the last entry of `reviewed_shas` before you spawn anything”

Two pairs of refs across four earlier runs each shared a sha (`pr-288-r1`/`r2`, `pr-291-r2`/`r3`).
**Which cause produced them was never established**, so this is a guard, not a diagnosis — worth
having because the cost of the case it catches does not depend on how the case arose.

*Cut down, R1-07 — the rule it supports:* “And where this run pushed the head, compare it against the sha it pushed”

**And where this run pushed the head, compare it against the sha it pushed** (`git rev-parse HEAD`),
which also covers a first round and a lag of more than one commit. The ref lagged the push on #379, #444 and #491, and
`headRefOid` was stale beside it on #444. Where they differ, re-fetch in a bounded until-loop, and
spawn nothing on the lagging sha.

*Cut down, R1-08 — the rule it supports:* “Tell the reviewer what to diff against, and never let it be a local branch name”

**Tell the reviewer what to diff against, and never let it be a local branch name.** Fetch the base
too and name it explicitly: `git fetch origin main` then `git diff origin/main...pr-<n>-r<round>` — or
better, the PR's own base from `gh pr view <n> --json baseRefName`. A local `main` is stale on any
machine that has not pulled, and the merge base then reaches back to whenever it last did. Measured on
this loop's first real run: `main...` produced **13,602 lines against an 864-line change**, most of it
other people's commits. That is the worst failure this design has produced, because it is silent — the
reviewer returns well-formed JSON with a legitimate-looking blocking count, about code the PR never
touched, and a fixer then acts on it.

*Cut down, R1-09 — the rule it supports:* “The first is an identifier this branch allocated from a sequence `main` also appends to”

The first is **an identifier this branch allocated from a sequence `main` also appends to.** An ADR
decision number is the observed instance: the branch takes the next free one when it writes the entry, and an upstream PR
merged since can have taken the same one. Observed on three consecutive runs, twice within a single
run, and on 2026-09-17 four concurrent branches each took Decision 103 for a different decision.
When it has moved, correct every home of the old value and not just the one you noticed — they sit in
javadoc and test names, not only in the ADR file — and search for the number itself rather than for a
phrasing you wrote, which is how a renumbering sweep left three sites standing on #238.
**And the sequence's own INDEX is a home no guard resolves** — an ADR's table of contents. On
#280/PR383 a decision with eight citations had no entry in it, found by a round-3 reviewer, because the
guard checks the heading and not the index; on #348/PR369 `main`'s own Decision 70 had been missing
since #367 and surfaced only on a fourth merge, for the same reason. On #348 a merge also kept only one
of two entries added at the same insertion point, with nothing in the build failing — so after a merge,
check the index for BOTH numbers. **And the index line is owed by the commit that ADDS an entry, not
only by the merge that meets one**: each of those four branches wrote its decision and none wrote its
index line, every one found later by a fresh agent (#447 c3 and #448 r1 non-blocking; #450 c4 blocking,
a cycle; #444's is in the git history and in no record).

*Cut down, R1-10 — the rule it supports:* “The second is a count or a structural claim that git merges cleanly and silently falsifies”

The second is **a count or a structural claim that git merges cleanly and silently falsifies.** On #340
`main` refactored three emission sites onto a shared writer; the controller auto-merged correctly and
three of this branch's claims became false — the ADR's per-site mutation recipe, a test class's javadoc
and a wire paragraph, all of which described the three sites as naming the serializer directly, and
"nothing in the merge flagged them". On #337 "two cases fail on a floor of nine" became five when the
merge brought seven more cases into that file, in four homes, and both of round 1's blocking findings
were counts the merge had falsified — a round. So grep this branch's own claims about the structures
`main` changed, and RE-MEASURE each on the merged tree rather than re-reading it for coherence; a
coherent sentence about a structure that moved is the failure mode, not the check.

*Cut down, R1-11 — the rule it supports:* “The third is a shared BUDGET the base consumed”

The third is **a shared BUDGET the base consumed.** A repo size guard is scored on the merged file, so
a change that fitted before the merge does not after — four runs met it (#336/PR368, #379/PR382,
#337/PR384, #280/PR383). The overflow is
loud and the loss is not: on #337/PR384 the remedy left was to drop the rule the branch came to add,
and nothing in the build says a directive went missing. Trim your OWN added prose, or move the rule to
the code it binds. Where a guard's javadoc forbids raising the budget in the commit that overflowed it,
raising it is not the move on its own — #280/PR383 raised it only after trimming its own prose first,
with the reasoning written into the guard.

*Cut down, R1-12 — the rule it supports:* “from round 2 on, harden's Phase 1 rule re-derive the merged result from scratch”

- from round 2 on, harden's Phase 1 rule **re-derive the merged result from scratch**. By round 3 the
  code is an accretion of rounds of individually-approved fixes, each judged against the state at the
  time it landed; the bug lives in the seam between two separately-correct mechanisms. This rule was
  written for exactly this shape.

*Cut down, R1-13 — the rule it supports:* “how to attack a guard whose subject is TEXT or SHAPE”

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

### 3 — Exit test

*Cut down, R1-14 — the rule it supports:* “A commit here costs at least that round, and more when a fix brings a blocker of its own”

`blocking == 0` ends the loop, with one exception, taken at most once per run: **a FULL round that
finds nothing blocking but raises non-blocking findings.** Those go to a fixer in this PR rather than
to an issue. Run step 4 on them with a fresh fixer, then steps 5 and 6 and COMMIT as usual, and
review the head that produces under *That confirming round is BLOCKING-ONLY* in FINISH — as is every
round after it, which is what stops the loop implementing nits forever. A commit here costs at least
that round, and more when a fix brings a blocker of its own: on PR #465 the fix for non-blocking r6-6
introduced blocking r7-3. A finding beyond the PR's own scope — a redesign, an adjacent defect,
behaviour the ticket did not ask for — is one that fixer declines rather than implements, and its
brief says so, since only blocking-only rounds follow it. Where nothing moved the head — the fixer
declined them all, or they named only the PR description — no round is owed, and it is not step 1's
*the fixer declined everything*: record the same count again (`phase: reviewed`) and go to step 7.
Where the round cap leaves no round for that review, decline them on the record instead, each with
its failure-mode sentence and the cap as the reason, and go to step 7. A clean round with no
non-blocking findings goes to step 7.

*Moved, R1-15 — the rule it supports:* “Those go to a fixer in this PR rather than to an issue”

**Filing them, or a blocking-only round's notes, as an issue is what this replaced.** On
`openmrs-module-chartsearchai` on 2026-09-23 the chain #472 → #482 → #489 → #494 → #498 → #504 ran
from 08:50Z to 19:06Z, each issue after the first filed by the PR that worked the one before it, and
the owner's instruction that day was to resolve it in the same pull request, without a chain of
issues.

### 4 — FIX

*Cut down, R2-01 — the rule it supports:* “A finding that asks for a follow-up or tracking issue is implemented”

Spawn a fresh fixer. It implements **every finding it agrees with**, and declines the rest on the
record. **Filing an issue is neither.** A finding that asks for a follow-up or tracking issue is
implemented — the thing it would track — or declined; on #494/PR497 round 1 asked for one, and
filing it began #498. Its brief carries harden's Phase 1 discipline:

*Cut down, R2-02 — the rule it supports:* “And when you NAME a guard, mutate the thing and read which case actually reddens”

- **And when you NAME a guard, mutate the thing and read which case actually reddens.** An attribution
  is as falsifiable as an assertion and fails the same way: on the #302 run three sites named a test as
  the guard for a branch it could not observe — it took a different code path — and twice the correct
  attribution was already written in that test's own comment. "Guarded by X" is a claim; check it.

*Cut down, R2-03 — the rule it supports:* “If you ADD a guard, prove which case reddens”

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
  **And a mutation result measures the arms it moved, not the mechanism** — one run published a
  "byte-identical" result for removing a scan bound, which held for the one arm it ran and failed for
  the other, at a cycle and a round.

*Cut down, R2-04 — the rule it supports:* “so build the case the exemption ADMITS and watch it pass”

- **A guard that is supposed to stay GREEN is not covered by *If you ADD a guard*** — a negative
  assertion passes whether or not its subject could ever arise, so build the case it exists for and
  watch it fail. **And a control measures the HARNESS it ran in, not the property** — ask which
  logger and level it captures, how it RENDERS what it captured, and whether a sibling test's residue
  changes either; a liveness precondition is not the answer. **And an exemption you write into a
  guard is that same hole from the inside** — an allow-listed method, a by-name exempt file — so build
  the case the exemption ADMITS and watch it pass; both of #448's rounds here were that. `harden`'s
  Termination carries the measurements.

*Cut down, R2-05 — the rule it supports:* “Ask which case hands the guard's SUBJECT its other value”

- **Ask which case hands the guard's SUBJECT its other value. That is the general form of the
  *supposed to stay GREEN* rule, and the question is not about the guard.** For each guard you
  add: what is the cheapest edit that satisfies its assertion and still breaks the property, and
  is the OTHER value of this boolean observed anywhere? A negative assertion is the instance where
  one of the two values goes unbuilt; a boolean, an arm of a split and the order of a published
  pair are others, and `harden`'s Termination asks the same question of a TEXT guard's forbidden
  string and of the value under an asserted key. Four rounds across three runs, each blocking and
  each raised by a fresh reviewer rather than by the guard's author: a context test asserting
  `Boolean.TRUE` at both legs, where `= true;` at both call sites stayed green; that same flag's
  last hop, where substituting the retained flag-less overload stayed green while hardcoding
  `true` reddened a case, so the hop was covered in one direction and not its mirror; a pair of
  published numbers whose wire fixtures were `ActiveOrderClaims(n, n)`, leaving the two `map.put`
  arms transposable and green; and an ordering contract published over two arms whose tests drove
  one, so hoisting the untested arm restored the defect it was written for with the suite passing.

*Cut down, R2-06 — the rule it supports:* “delete the unsupported clause rather than replacing it with a better-sounding one”

- **Don't rewrite prose faster than you verify it.** When a finding is about text an earlier round
  wrote, delete the unsupported clause rather than replacing it with a better-sounding one. That is not
  a counsel of caution — measured on this loop's second run, a correction of a false claim introduced a
  DIFFERENT false claim, which the next round then had to catch.

*Cut down, R2-07 — the rule it supports:* “Don't write a tally a later round will have to re-measure; write the method”

- **Don't write a tally a later round will have to re-measure; write the method.** Every count published
  on the fourth run went stale, several twice, and each recurrence cost a round because the next reviewer
  re-measures what a comment asserts: "negating it reddens exactly two" became three in the very round
  that added the third observer, and "the three sites that quoted it" became four in the round that found
  the fourth. Both had a sentence beside them claiming they had just been re-measured. The recurrence
  stopped only when the enumeration was deleted in favour of *"mutate the line and read the failures"* —
  so prefer that form, and treat an exhaustive list as worse than none, since it invites the next reader
  to treat the extra failure as a regression they caused. If a count really is load-bearing, name the
  head it was measured on.

*Cut down, R2-08 — the rule it supports:* “And the rule is not about tallies — it is about claims you cannot check”

**And the rule is not about tallies — it is about claims you cannot check.** A universal or an exhaustive characterization is the same defect in different grammar, and it slips past a reader watching for digits: *any*, *only*, *exactly*, *all*, *never*, *the whole*, *cannot*. Measured on a `/harden` run of #298, five such claims in three consecutive cycles, each written to correct the previous cycle's false claim and each false in turn — "it only re-admits `M01AE0`" (it re-admits any single trailing digit), "exactly the two levels the ladder is known to be handed" (nothing on the path validates a code's shape), and three more of the same shape; `harden`'s own anti-pattern carries all five. So before writing one about code you just wrote, spend one attempt trying to falsify it; prefer stating what the thing DOES over what it excludes; and name the residue rather than claiming there is none.

*Cut down, R2-09 — the rule it supports:* “And do not ask either agent to re-derive evidence its brief already carries”

**And do not ask either agent to re-derive evidence its brief already carries** — hand it the
measurement and point its budget at the claims it DOUBTS. Two runs paid an attempt for the
opposite: on #238 round 5's fixer stalled at the 600s watchdog building a temp fixture to
re-measure something the reviewer had already measured, and the retry saying *"do not re-measure,
edit only"* finished in three minutes; on #294 the round-1 reviewer's first attempt "exhausted
itself re-running" mutations its brief had already documented, and died. This binds Step 1's
brief as much as this one. It is **not** a 429 remedy, whatever the shape of those two records:
#294 applied the leaner brief and the reset window together, which is the confound the dead-phase
contract in **State** already discloses for #366 and #354.

*Cut down, R2-10 — the rule it supports:* “A finding may name the PR DESCRIPTION rather than a file, and it can be blocking”

**A finding may name the PR DESCRIPTION rather than a file, and it can be blocking.** The fixer cannot
edit the description, so the orchestrator applies that one and says which it applied; it still counts as
the round's fix and the round proceeds normally. Do not wave it through as cosmetic: the description is
the durable public rationale attached to the closing of the ticket, no test can fail on a false sentence
in it, and a repo-wide grep for a corrected claim will never reach it. On this loop's second run, round
2's ONLY blocking finding was exactly this — the sixth home of a claim that had just been corrected in
five files, left standing in the body because the fixer had no access and the orchestrator had edited
that same body for something else without re-reading it.

### 5 — GREEN

*Cut down, R2-11 — the rule it supports:* “It is the fixer's build and the orchestrator does not re-run it”

**It is the fixer's build and the orchestrator does not re-run it.** Read the `green` field and
spot-check what it claims: the surefire reports are on disk, and what actually needs verifying
is `git status`, the branch, and the pushed sha. A second full root install per round buys
nothing those do not already carry — eight duplicates on PR #465. Where the report is missing,
vague, or names a command other than a root `mvn -o clean install`, run it yourself; the rule is
against the reflex, not the check.

### 6 — VERIFY, when the round touched runtime behaviour

*Cut down, R2-12 — the rule it supports:* “A third case: runtime-visible, but not observable by THIS instrument”

**A third case had no path: runtime-visible, but not observable by THIS instrument.** The
procedure below deploys an `.omod` and restarts `openmrs-standalone.jar`. A change to the
published Docker image's ENTRYPOINT — `backend-init.sh`, the model-fetch library it sources, the
container's own startup wiring — is runtime behaviour a standalone never executes, so deploying
and restarting cannot see it however carefully it is done.

*Cut down, R2-13 — the rule it supports:* “If the prescribed one cannot reach the change, say which one can, use it”

So name the instrument before deciding. If the prescribed one cannot reach the change, say which
one can, use it, and record BOTH the skip and the substitute in the report — this is not
`--no-verify`, which asserts no round can touch runtime behaviour. On #465 the substitute was
the repo's own harness, which pastes the entrypoint's wiring functions out of the file verbatim
and sources the real library against a database stand-in: stronger than a proxy, because it is
the production function. `verified_shas` stays empty there, which the gate already treats as a
legitimate no-verifier-ran state, so the report is the only place the substitute lands.

*Cut down, R2-14 — the rule it supports:* “Read from the other end the mismatch has its own signatures”

*0.35.0 also deleted "for that one `/usr/libexec/java_home -v 1.8` is the fix", as false on this
machine. Measured 2026-09-27: `/usr/libexec/java_home -v 1.8` returns
`/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home`, which has no `bin/javac`, whatever
the pom targets; `/usr/libexec/java_home -v 1.8.0` returns the OpenLogic JDK 8, which has one.
`REJECTED.md`'s 2026-08-27 kill of a prune of 1.8 as a TARGET still stands: 1.8 stays as the
antecedent of the Mockito signature.*

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

*Cut down, R2-15 — the rule it supports:* “because replacing the `.omod` does not reliably replace what runs”

**Then delete `<standalone>/appdata/.openmrs-lib-cache/<id>/`, because replacing the `.omod` does not
reliably replace what runs.** OpenMRS expands a module into that directory and a redeploy under the
same name does not always re-expand it: on FM2-700 it held both the released api jar and the new
snapshot, and the stale one shadowed the fix; on #340 the first boot ran week-old controller classes
while the omod timestamp, the module status endpoint and the cache's own marker file all read
current. It is a cache, so there is nothing to preserve.

*Cut down, R2-16 — the rule it supports:* “These are throwaway demo instances: stop the one you resolved in step 1”

4. **Restart, and just take YOUR standalone.** Modules load at startup, so a running instance picks
   up nothing until restarted. **These are throwaway demo instances** (owner's instruction,
   2026-08-27): stop the one you resolved in step 1, running or not, without confirmation. Do not
   enumerate candidates hunting for an idle port, do not stop to attribute pids, and never report
   `unrepairable` because it was in use — "in use" is not a blocker here. Launch from the standalone
   directory, backgrounded, teeing to a log you can tail:
   `java -jar openmrs-standalone.jar -commandline`.

*Cut down, R2-17 — the rule it supports:* “that is a fact the owner has to state, not one to infer from a port being busy”

*This rule used to say the opposite* — never restart a server that was already running — written
after a run nearly killed what it took to be the user's own session. That caution was wrong about
this environment and cost a later run real time: it hunted for a free port and prepared an
`unrepairable` abort on the only standalone there is. Kept as history so nobody reinstates it from
the same reasoning; if you are ever in an environment where a standalone is NOT disposable, that
is a fact the owner has to state, not one to infer from a port being busy.

*Cut down, R2-22 — the rule it supports:* “Wait on a CONDITION, never on a clock”

**Wait on a CONDITION, never on a clock.** "Wait out a slow boot" is not licence to sleep blind.
`verify-frontend-change`'s *"Wait for real readiness by polling HTTP, not by guessing a sleep"* already
says poll; what it does not say is that a poll is one loop per wait, not one call per look, and that
the loop runs inside your turn. A fixed sleep cannot exit early and cannot fail loudly. Use ONE
foreground loop bounded under the tool's ten-minute timeout, as *this session must not busy-wait
either* gives it — `end=$((SECONDS+540)); until curl -sf -o /dev/null http://localhost:$PORT/openmrs/
|| [ $SECONDS -gt $end ]; do sleep 5; done` — and if it exits at the bound with the java pid alive,
run it again, up to the round's bound. Give it the failure signatures too (`ModuleException` in the
log, the java pid gone), or a crashed boot is indistinguishable from a slow one. The server itself
stays detached (`nohup … & disown`, as #238 launched it); only the WAIT is in the foreground.

*Cut down, R2-18 — the rule it supports:* “Do not end your turn to wait on a `Monitor` or a background task”

**Do not end your turn to wait on a `Monitor` or a background task.** A delegated agent that ends its
turn has handed back its report, unfinished. #238's verifier armed a Monitor and returned *"Standing by
for the startup monitor notification"*. #407's reviewer started two background tasks and returned
mid-mutation; both tasks' output files read `[killed]` from the second its report came back, so its
resume waited about ten minutes on a build that was already dead. The harness's own texts route a
single wait to Monitor or to background Bash; they are written for a session that stays alive.

*Cut down, R2-19 — the rule it supports:* “Irreversibility is not a constraint on a standalone”

**Irreversibility is not a constraint on a standalone.** A schema migration, a platform bump that
runs core liquibase, a destructive DB statement — all fair if they unblock the run. The instances and
their data are disposable, so there is nothing to put back. *This paragraph used to require the
opposite,* after a verifier raised a standalone's platform and ran liquibase against its database;
that was recorded as a hazard and is not one here. **The environment/artifact line above still
binds** — irreversibility is fine, repairing the ARTIFACT under test never is.

*Cut down, R2-20 — the rule it supports:* “Repairs PERSIST, so say which of your observations rest on someone else's”

**Repairs PERSIST, so say which of your observations rest on someone else's.** A repair made in
round 1 is still there in round 3, and a verifier that measures a property the repaired environment
has — rather than the one a stock install has — reports it in good faith and is wrong. On this loop's
first run, round 1 added a `log4j2.xml` logger entry to see an INFO line at all; two commits of that
same PR exist because the line is invisible at stock levels, so a later round reporting "present in
the log" would have reintroduced the very claim those commits removed. Hence `inherited_environment`:
name the observations that depend on an earlier round's repairs, separately from your own.

*Cut down, R2-21 — the rule it supports:* “A verifier's observations can falsify a claim the PR makes in prose, and that is a finding rather than a footnote”

**A verifier's observations can falsify a claim the PR makes in prose, and that is a finding rather
than a footnote.** It is running the code, so it sees the units the documentation guessed at. On this
loop's second run the final verifier's live output corrected the ADR's own benefit bullet: "one chip per
prescription" is really one per `orderCarrying` pick, because two orders sharing an unnameable code
collapse onto one partner — visible in a live chip and in no test. Read the `observed` field for what it
contradicts as well as for what it confirms.

*Added 0.38.1 — the rule it supports:* “Inside a round, neither is recorded as a blocking finding by the orchestrator”

Until 0.38.1 this read "Neither is ever recorded as a blocking finding by the orchestrator", while
FINISH, since R3-07, records "a `not-the-environment` finding on the merging head" as a blocking
finding. R3-07 added the FINISH rule and left this sentence unscoped. The 2026-10-02 retro's refuter
found the contradiction in the text; no run record cites it.

### COMMIT

*Cut down, R3-01 — the rule it supports:* “Check the branch before you EDIT, and again before you commit.”

**Check the branch before you EDIT, and again before you commit.** The commit-time check below is
necessary and not sufficient: by then a wrong-tree edit has already happened, and the only reason it is
recoverable is that nothing was committed yet. `resolve-ticket`'s *Step 5 — Test first, then the fix*
carries the measurement.

*Cut down, R3-02 — the rule it supports:* “Re-check the branch immediately before committing.”

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

*Cut down, R3-03 — the rule it supports:* “A protective commit taken mid-round does not violate this, and the commit rule in State outranks it”

One commit per round, in the repo's existing voice (see `git log`), pushed to the PR head branch.
**Append only — never amend, never force-push.** A reviewer must be able to see the chain of rounds,
and rewriting history under one that is mid-flight is how a round reviews a sha that no longer exists.
**A protective commit taken mid-round does not violate this, and the commit rule in *State* outranks
it** — on #355 round 2's fixer died on a 429 with partial work in the tree, and committing that residue
before retrying was correct and made two commits for the round. The convention guards the chain of
rounds, which such a commit only ever appends to.

### 7 — FINISH

*Cut down, R3-04 — the rule it supports:* “this is the sha you are handing over — and FINISH does not edit it”

A version of this step applied those findings here, and the argument it rested on was *"those edits
carry no blocking finding by construction, so no further round is owed"*. It graded the FINDINGS,
which the reviewer saw, and not the FIXES, which did not exist when it looked. The run records name
what actually landed here: a whitespace normal form defined twice, a citation carve-out pinned at
only two markers, an assertion that pinned nothing. That is why step 3, not this step, implements
them: there the fixes get a review.
Re-deriving the PR description is still owed and is not an exception, because the body is not in the
tree and does not move the head.

*Cut down, R3-05 — the rule it supports:* “Delete the run's own `pr-<n>-r<round>` refs.”

**Delete the run's own `pr-<n>-r<round>` refs.** They are local fetch copies of the PR head with no
upstream, worth nothing once the round is over and re-fetchable from `pull/<n>/head` while GitHub
retains it. The loop creates one per round and went four completed runs without removing any, leaving
refs on merged PRs that clutter every `git branch` a human or an agent runs afterwards. The check is
`git branch --list 'pr-*'` in a repo this loop has worked, read for the `pr-<n>-r<round>` shape. Delete
them here rather than at the top of the next run, because the next run may be in a different repo or
may never happen.

*Cut down, R3-06 — the rule it supports:* “Re-derive the PR description against the merging head before marking ready.”

**Re-derive the PR description against the merging head before marking ready.** Across rounds the body
describes code that later rounds change under it, so the patches this loop applies to it accumulate into
something false: on the fourth run, four consecutive rounds had their top finding in the description, one
of them a sentence an earlier round had itself added. Patching mid-loop is right when a finding names the
body; leaving those patches as the final text is not. Rewrite it whole here, re-measuring every figure in
it rather than carrying one forward. Name in it, one line each, every finding the loop did not
implement — each decline with its failure-mode sentence, each note a blocking-only reviewer returned,
and any non-blocking observation the verifier made on the merging head — since with no issue filed,
the PR is where it stays visible.

*Cut down, R3-07 — the rule it supports:* “a `not-the-environment` finding on the merging head is a blocking finding”

**And a verifier that ran fine and found something substantive was, until this was written, the one
outcome with no instruction at all.** `unrepairable` aborts and "could not determine" stops as
converged-but-unverified, but a `not-the-environment` finding on the merging head had no round left
to reach, because the loop had already exited — so what happened next was whatever the orchestrator
improvised, including marking ready over it, with the gate satisfied and nothing objecting. It is a
**blocking finding**: record it and continue from step 4, under *That confirming round is
BLOCKING-ONLY* in this step. An
ENVIRONMENTAL failure is still not one and must never re-enter the loop — that is what grinds rounds
against a broken standalone until the cap.

*Cut down, R3-08 — the rule it supports:* “Whether a verifier run still covers the head is a question about the compiled artifact”

**Whether a verifier run still covers the head is a question about the compiled artifact, not about the
source, the timestamp or a file hash.** On #337/PR384 a comment-only push after the verifier ran made
byte-identity FALSE — a split comment line shifted a `LineNumberTable` — while `javap -c` against the
exact class the verifier ran proved equivalence; on #348/PR369 a comment renumbering was proved neutral
by compiling both variants against the resolved classpath and diffing the emitted class files, rather
than by arguing that comments cannot change bytecode. This does not touch the verifier's own
deploy-identity hash, in *Confirm you are testing this build*: that one asks whether the class that
LOADED is the one you built, which is an identity question a hash answers and a disassembly does not.

*Cut down, R3-09 — the rule it supports:* “Marking ready is the LAST action of the run, after the last push — never before one.”

**Marking ready is the LAST action of the run, after the last push — never before one.** It is not a
status update, it is a trigger: the Claude Code GitHub App reviews every push to a NON-draft PR and
skips drafts entirely, so a run that marks ready and then keeps pushing buys one automatic review per
push. Measured on #381 (2026-09-06): `ready_for_review` at 19:30Z, first app review 12 minutes later,
and four more pushes — a merge with `main` and two further rounds — produced four reviews, three of
which reported no issues. The three pushes made while it was still a draft produced none.

*Cut down, R3-10 — the rule it supports:* “Do the merge, the rounds and the description first; mark ready once, last.”

So if anything after the ready mark requires another push — `main` moved and the branch needs
merging, a late round finds something, the description is re-derived — **the run was not finished and
should not have marked it.** Do the merge, the rounds and the description first; mark ready once,
last. The owner's instruction, 2026-09-07: *mark PRs ready only after the last push.*

## Editing by script, which is how edits get silently lost

*Cut down, R3-11 — the rule it supports:* “Four failure modes follow, all silent and all cheap to close”

Every role here edits files by running a short script rather than by hand, because the edits are
precise and the files are large. Four failure modes follow, all silent and all cheap to close; the
first three were measured on this loop's second run:

*Cut down, R3-12 — the rule it supports:* “assert the target is present before replacing”

- **A replacement that matches nothing reports success.** `str.replace` returns the string unchanged
  and the script prints whatever you told it to. One claim survived five hardening cycles that way —
  the script said it had fixed it, and it had not, because the target text wrapped differently than the
  script assumed. So: **assert the target is present before replacing**, and let the assert kill the
  script rather than continuing to the next edit.

*Cut down, R3-13 — the rule it supports:* “after any multi-line replacement, count what should still be there”

- **A slice can span further than you meant and take a neighbour with it.** A replacement bounded by
  "from this javadoc to the next method" deleted a whole test method that sat between them; it compiled
  and the remaining tests passed. So: after any multi-line replacement, **count what should still be
  there** — test methods, symbols, bullet points — and compare against what you expected.

*Cut down, R3-14 — the rule it supports:* “A script's own report is not evidence.”

- **A script's own report is not evidence.** Verify by reading the file back, with a grep for the text
  you believe you wrote. The three defects above all announced success.

*Cut down, R3-15 — the rule it supports:* “A batched write reports edits an abort never made.”

- **A batched write reports edits an abort never made.** Write each replacement as you make it: a
  script that prints per-edit success and opens the file once after its loop loses every edit that
  write would have carried when a later assert throws, and the prints stand. #336/PR368 paid a round
  for it — two `Decision 68` → `69` replacements printed `ok` and never reached disk, round 1's
  blocking finding — and on #280/PR383 three edits vanished the same way, with the commit message
  announcing edits the diff did not contain, found by a later review pass. **And read the report for
  WHICH edits it names**: this rule was itself applied by a script whose assert stopped it two edits
  from the end, and the five prints that had already scrolled past read as the whole batch.

*Cut down, R3-16 — the rule it supports:* “None of this is optional politeness”

None of this is optional politeness. Each of the three cost a round or a cycle on the run that found
them, and the third is what caught the other two.

## Correcting a claim means finding every home of it

*Cut down, R3-17 — the rule it supports:* “When a finding is that some statement is false, the statement is rarely in one place.”

When a finding is that some statement is false, the statement is rarely in one place. Measured on this
loop's second run: a correction reached one of seven homes, then five of six, then five of six again —
and once, both halves of a single paragraph disagreed with each other after one half was fixed.

*Cut down, R3-18 — the rule it supports:* “Searching the PHRASING is what leaves the last home standing, and it fails two different ways”

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

*Cut down, R3-19 — the rule it supports:* “Name the target instead of locating it.”

And a positional cross-reference — "the bullet above", "the section below" — is a claim about layout
that any insertion falsifies. On that same run, inserting a bullet silently re-pointed a neighbouring
bullet's "see the bullet above" at the new text. **Name the target instead of locating it.**

*Cut down, R3-20 — the rule it supports:* “enumerate the claim's SUBJECT rather than a phrasing”

`harden`'s *Don't stop correcting a claim at the site you noticed it* carries one remedy this section
does not, and it is the one for a sweep that keeps returning one more home: enumerate the claim's
SUBJECT rather than a phrasing. A round-4 fixer here found nine homes of one claim that way, five
more than the findings had named — after the same claim had already cost a round in round 1 and
another in round 4.

## Termination

*Cut down, R3-21 — the rule it supports:* “a terminal entry is indistinguishable from a mid-flight one”

**What the check costs, said rather than left to be discovered:** a terminal entry is indistinguishable
from a mid-flight one — both are `phase: reviewed, blocking: 0` — so a FINISHED run's leftover entry
in a worktree that has since moved on now blocks, where before it allowed, until the six-hour expiry.
`gate-state clear --only pr` is the remedy and the block message says so. Under these rules a run that
handed over correctly leaves the head ON the reviewed sha, so this is the tail of an entry written
under the older ones, or of later work in the same worktree — never of a run that finished cleanly.

*Cut down, R3-22 — the rule it supports:* “Raising the DEFAULT cap is a third move, and the runs that took it read a signal first.”

**Raising the DEFAULT cap is a third move, and the runs that took it read a signal first.** #308 raised
it with one real finding outstanding, on the ground that rounds 1-5 had each found a genuinely different
defect; #315 extended it twice, on the ground that the findings were shrinking round on round. Both then
converged. So what licenses a raise is evidence the loop is still WORKING rather than spinning — a
different defect each round, or findings shrinking — and a round that re-raises what an earlier one
raised is spinning: take the override instead. Raise it a round or two at a time, re-read the signal each
time, and say what you raised it to, because a raise nobody states turns a *did not converge* into a
*converged* silently. **Do not raise a cap the caller set:** `--max-rounds N` is their budget, and under
`ticket-pool` a session that outruns `ticket.timeout_seconds` is killed. A labelled `draft` is by far
the cheaper outcome.

## State

*Cut down, R4-01 — the rule it supports:* “Resolved on both sides or the two disagree wherever a path component is a symlink”

`~/.claude/pr-harden-state.json`, keyed by the WORKING TREE's physical path — `pwd -P`, symlinks
resolved, which is what both hooks key on and what `gate-state` writes. Resolved on both sides or the
two disagree wherever a path component is a symlink (`/tmp` on macOS, a symlinked home), and
"no entry" is the gate's fail-OPEN case: a run with findings outstanding stops and nothing says why.
Measured — the hook suites reported 4 of 12 and 3 of 11 cases silently inverted before both sides
resolved.

*Added 0.38.0 — the rule it supports:* “And the gate runs in the SESSION's cwd, which a shell `cd` does not move”

Two attended sessions launched from the shared checkout worked a PR in a worktree, and each moved
into it with `EnterWorktree` before any Stop fired, so neither observed the fail-OPEN — it is read
off the gate's `KEY="$(pwd -P)"`, which ignores the hook input's `cwd`. #527 (2026-09-24, resolve-ticket
Step 1, transcript `ebc78818…:86-107`) wrote its entry via `cd <worktree> && gate-state …` while the
session's cwd was the shared checkout, read the hook, then moved. #402 (2026-09-28, pr-harden Step 0,
`c3fd0664…:1343-1356`) found no entry, reasoned the gate was keyed to the main checkout, and moved
first. Measured after the move, in #402: a compound command naming git was refused (`:1372`), a plain
`gate-state --owner $PPID pr-set …` was refused (`:1376-1377`), and `echo $PPID` alone ran (`:1385`).
Its subagents hit 12 refusals across about 1,000 tool calls, which starting in the worktree avoids.
Parked at one record 2026-09-24 (`REJECTED.md`, REOPEN ON a second record); reopened by #402.

*Added 0.38.1 — the rule it supports:* “Its isolation guard then refuses what it cannot verify, git or not”

0.38.0 said the guard "refuses a compound command that runs git", and that was wrong both ways.
Measured 2026-10-02 over every Bash call inside the five `EnterWorktree` windows in the store: #562
(`111381c1…` from L944), #564's session (`12e2feb4…` L295-1682, its L1657 exit having failed, and
L3255-4152, a later #567 run), #402
(`c3fd0664…` from L1356) and #527 (`ebc78818…` from L119). Compound commands that ran git passed 118
times and were refused 7. Compound commands that ran none were refused 25 times and passed 305: #562's
L1183, a heredoc into python editing docs, and L1411, an `lsof`/`kill` chain, are two. All 37
refusals carry the guard's own predicate — "too complex to verify", "cannot be shown not to be git" or
"can't be verified" — and 30 say "Split it into plain, separate commands"; 5 say "Run the plain
command" and 2 "Run git directly with literal arguments", both of them over a `grep` with a
directory argument named `eval`, which the guard read as an `eval` call (#527 L421, L425). A plain
`python3` or `bash` script run by absolute path ran 32 times and was refused 0. None of the 32 ran
git, so this does not show whether the guard reads inside a script; keeping a script's git in the
worktree is its author's job either way. An unchained `gate-state --owner $PPID …` was refused
twice (#402 L1376, #527 L667). Method: a refusal is a result carrying "This session is isolated in the
worktree", and "runs git" is a `git` command word outside heredoc bodies. Calibrated on #402 L1371 (git,
refused), #562 L1075 (git, passed) and #562 L1153 (a heredoc followed by `git diff`, passed), after a
first draft also counted a heredoc that only mentioned git. The two records carry the cost, #562:23 as "repeated
re-issues" and #564:19 as "several retries". 19 of their 23 refusals came before Step 9 loaded `pr-harden`, so
`resolve-ticket` Step 1 carries the same sentences.

*Pruned 0.38.1:* “`--owner $PPID` is this session's own claude process, which is how both gates tell
your entry from one a co-located session left in the same directory.” The `owner` paragraph now says
each gate allows the stop on the pid in its own entry, and the two gates' owner logic is the same code
(the `OWNER_PID` block in `pr-harden-gate.sh` and in `harden-cycle-gate.sh`), so the sentence restated it. The
2026-09-28 retro killed this prune while that paragraph said "the gate" (`REJECTED.md`).

*Cut down, R4-02 — the rule it supports:* “A missing `reviewed-sha` or `verified-sha` call is the difference between a checked handover”

`phase` is `"init"` before the first review, `"reviewed"` once a reviewer's count is recorded,
`"fixing"` from the moment the fixer is spawned until a reviewer's count is recorded again. On `init` and
`fixing` the gate blocks regardless of `blocking`, so leave the last measured value there for the
record. The gate reads `pr`, `round`, `blocking`, `phase`, `ts`, `override`, `owner`, `awaiting`,
`unattended`, `mode`, `reviewed_shas` and `verified_shas`; `declined` is the orchestrator's own
ledger, carried in the same entry so one write keeps it in step with the rest. The two sha lists were
the orchestrator's ledger too until the gate began reading their last entries against the head — so a
missing `reviewed-sha` or `verified-sha` call is no longer merely an incomplete record, it is the
difference between a checked handover and an unchecked one.

*Moved, R4-03 — the rule it supports:* “The gate reads `pr`, `round`, `blocking`, `phase`, `ts`, `override`, `owner`, `awaiting`, `unattended`, `mode`, `reviewed_shas` and `verified_shas`”

**`mode` has no writer today**, which is a defect and not a spare field: the gate has a distinct
`--plan-only` message behind it, so a plan-only run instead gets the generic `building` one, telling
it to "continue the phases" through implementation and a draft PR — exactly the work its own mode
excludes. Either something writes `mode` or that branch goes.

*Cut down, R4-04 — the rule it supports:* “`owner` is what tells your entry from somebody else's, and it is not the unattended marker's job”

**`owner` is what tells your entry from somebody else's, and it is not the unattended marker's job.**
This file is keyed on the CHECKOUT, so a pool run and an interactive session in the same directory read
one entry. Measured live 2026-08-26: an interactive session was stopped with "resolve-ticket is mid-run
and has not opened its pull request yet" over an entry belonging to a live `claude -p /resolve-ticket`
run, and both remedies the block offers damage that run — `override: true` disarms its gate for the rest
of its life, and "continue the phases" puts a second session in one worktree. So stamp `owner` with
`$PPID`, which from a tool shell is this session's own `claude` process; the gate allows the stop when
that pid is alive and is not an ancestor of the stopping session, and when it is DEAD. An UNSTAMPED
entry is held to the contract exactly as before, so nothing is relaxed on a missing field.

*Restated, R4-05 — where the rule stands:* “Whose ENTRY it is is a different question about a different file, answered by `owner` above and never by this marker”

**Do not answer this question with the unattended marker.** The first version of that check inferred
entry ownership from marker ownership, and review measured the cost within the hour: a live foreign
marker allowed EVERY block path, so an interactive `/harden` or `/pr-harden` in a pool-worked checkout
silently lost its own termination contract — `edits: 7` allowed, `phase: fixing` allowed. The marker
answers whether THIS session is unattended; the two questions coincide only in the incident above.
`gate-test.sh`'s "foreign marker but the entry is OURS -> block" is that regression, pinned in both
suites.

*Cut down, R4-06 — the rule it supports:* “an unattended run never ends a turn with an agent outstanding — collect it in the same turn”

**That last clause holds in full only for an ATTENDED session, and taking it as universal killed
two unattended runs.** When a `claude -p` turn ends, the process kills a `run_in_background` Bash
command within seconds if nothing else is outstanding, and stops a background agent still running
600 s later (the default of `CLAUDE_CODE_PRINT_BG_WAIT_CEILING_MS`); either way it then exits
without re-invoking the orchestrator, and the gate's own allow made that silent (allowing is
`exit 0`). An agent that finishes inside the 600 s is waited for and does re-invoke it — measured
2026-09-25 on Claude Code 2.1.282 (`~/.claude/skill-lessons/artifacts/2026-09-25-headless-background-wait/`).
Which case a yield is in is not known when the turn ends: #310's one process (2.1.246) was
re-invoked after seven such yields and died on the eighth, its three pass-3 reviewers stopped at
that ceiling. Measured 2026-08-26 — #297 recorded `awaiting=[{agent: "refute plan #297 pass 1"}]`,
narrated *"dispatched the refutation gate. Here is where things stand"*, and ended at 51 turns with
no PR and its plan and reproduction discarded; #310 died with the same signature in `/harden` pass 3,
at 1365 turns and $76.72. So **an unattended run never ends a turn with an agent outstanding —
collect it in the same
turn.** The gate enforces it now, scoping the allow to attended sessions off a pid-stamped marker the
driver holds for the life of the run; the rule is stated here as well because a gate can only refuse
a stop after the decision to stop has been made, and that decision is what costs the run. And do not
read a stream with no gate text in it as evidence the gate never ran: hooks DO reach `-p` sessions,
probed the same day, feedback delivered and captured.

*Cut down, R4-07 — the rule it supports:* “Collecting in the same turn means spawning in the foreground, where the harness allows it”

**Collecting in the same turn means spawning in the foreground, where the harness allows it.** When
the `Agent` schema carries `run_in_background`, pass `false` on each call: several such calls in ONE
message still run concurrently, and each agent's report returns as that call's own result, in this
turn. Measured 2026-09-24 on two unattended runs: #451's four Phase 2 lenses, spawned so in one
message, took 343s of agent time in 165s of wall clock, all four reports back before its next tool
call; #433's first wave took the default, which is BACKGROUND, ended the turn with four agents
outstanding, and was refused by the unattended gate twice. The flag has come and gone between
harness builds — present 2026-09-16, absent 2026-09-20 (when this paragraph said it did not exist),
back 2026-09-23 — so where a spawn still answers "Async agent launched", an unattended run keeps the
turn alive with a bounded foreground wait (#433: a 420-second loop, after which its completion
notices were delivered). **A delegated agent's output file is its whole JSONL transcript, never its
report**: each read injects a window of raw agent chatter, the next a different window rather than
the rest of the first, and the orchestrator re-sends all of it on every later turn — measured
2026-09-01 across three tickets of twenty, 49 reads carrying 953,119 bytes no round ever used, against
whole reports of 9,352 and 9,956 bytes from two agents collected in-turn. Where you need to block on
something that is NOT an agent — a build, a server coming up — that is a background Bash task, whose
output file is its stdout and is safe to read; wait on it as *this session must not busy-wait
either* says.

*Cut down, R4-08 — the rule it supports:* “Snapshot the worktree before every delegation and compare it after — on ANY terminal outcome”

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

*Cut down, R4-09 — the rule it supports:* “DO NOT EDIT THE WORKTREE WHILE A DELEGATED AGENT IS RUNNING”

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

*Cut down, R4-10 — the rule it supports:* “`git checkout -- <path>` is the right restore only where the file carries nothing but the mutation”

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

*Cut down, R4-11 — the rule it supports:* “Tell every agent to restore BEFORE it reports, not after”

**Tell every agent to restore BEFORE it reports, not after** — a mutation restored late is a mutation
that ships if the agent dies mid-sentence. On the second run, the eleven agents briefed that way all
restored cleanly, verified by hash rather than trusted.

*Cut down, R4-12 — the rule it supports:* “And tell every agent to wait on its own builds inside its turn”

**And tell every agent to wait on its own builds inside its turn**, per the verifier's *Do not end your
turn to wait*: #407's round-1 reviewer, briefed only to restore first, returned mid-build; later briefs
spelled the in-turn wait out and it did not recur (#407:17).

*Cut down, R4-13 — the rule it supports:* “Clear the await on ANY terminal outcome — completed, failed, stalled, killed — not on a result arriving”

**Clear the await on ANY terminal outcome — completed, failed, stalled, killed — not on a result arriving.**
"The moment the result arrives" says nothing about a result that never will, and agents die: on this
loop's first run six did, to a network drop, two stall watchdogs and a nested-spawn timeout. Four dead
agents left four fresh awaits, and the gate honoured them — measured, it would have licensed a yield
for another 36 minutes with nothing whatsoever running. The harness reports the death, so there is no
excuse for waiting out a timeout. The one-hour bound and the no-`since`-reads-as-dead rule are
backstops, not the mechanism.

*Cut down, R4-14 — the rule it supports:* “And this session must not busy-wait either”

**And this session must not busy-wait either.** *Wait on a CONDITION, never on a clock* is written into
the verifier's brief, but the orchestrator is where a blind `sleep` loop costs the most, because its
context is the largest thing being re-sent per turn. Whatever you are waiting on — a boot, a build, an
agent, a lock — wait on the CONDITION inside the turn: an agent by spawning it in the foreground
(*Collecting in the same turn*), anything else by a foreground loop that exits on the condition, on a
failure signature, or at a bound under the tool's ten-minute timeout — `end=$((SECONDS+540)); until
grep -qE 'BUILD (SUCCESS|FAILURE)' "$F" || [ $SECONDS -gt $end ]; do sleep 5; done; tail -3 "$F"`.
Backgrounding the wait and ending the turn for its notification is what the gate refused mid-run on
#479, #489, #498 and #505, each of which recovered with such a loop, and on #469, #476 and #477 by
their transcripts.

*Cut down, R4-15 — the rule it supports:* “clear the await, retry the phase twice, and change something between attempts”

**And a dead delegated phase needs a contract, because it is neither an abort condition nor a
finding.** Left undefined, an unattended run ends on the first agent death. The contract: clear the
await, retry the phase **twice**, and change something between attempts — an agent that stalled on
volume gets a leaner brief, one that stalled on nesting is told not to delegate. **A session or quota
429 is neither of those, and the lever that used to work is no longer available.** It was a cheaper
agent — on #238 round 1's fixer "died instantly on a session rate limit (429)" and a retry on a
different model succeeded; on #336 the round-1 reviewer died the same way and completed on a smaller
model with a leaner brief (that record is #336/PR341; the #336 cited below is PR 368, a different
run of the same ticket number). **Do not reach for it: a per-call `model` on the Agent tool is refused by a
PreToolUse hook (`~/.claude/hooks/no-subagent-model-override.sh`), so a retry cannot downgrade an
agent from inside the round.** What is left is a leaner brief and WAITING. The leaner-brief retry
converged three times under the hook — #336/PR368, #366 and #370 — though #366 applied the wait in the
same attempt and cannot separate them. The wait's one witness free of the model confound predates the
hook: on #354 two agents completed after the reset, only ONE of them on a smaller model. #377/PR381's
retry succeeded after the stated reset under the hook, also on a leaner brief. But **a stated reset is
not always inside this run's horizon**: #370's was a WEEKLY cap resetting the next day, and a plain
leaner-brief retry succeeded anyway, so the error text does not separate a burst from an exhausted cap
and both attempts must not be spent waiting. The residue: #339 met a limit that
"will refuse every retry for hours", where nothing here is known to help and the two attempts are
spent on a condition that has not changed; and on #379/PR382 the run spent no retry at all — the
orchestrator verified that cycle's four changed sentences itself and reported that the fresh-context
read did not happen, which is the deviation to name rather than a gap to leave silent. After the second
retry, stop with the labelled deviation naming the phase and the failure mode, exactly as the round
cap does. A retry is not free of consequence either: on the first run, retrying a reviewer twice is
what exposed the stale-diff-base defect above, because the third brief had to state the base
explicitly.

*Cut down, R4-16 — the rule it supports:* “where that is set, treat the result as the agent's death and apply this contract”

**And under `ticket-pool`, `[Request interrupted by user for tool use]` on an agent's result is not the
operator.** The driver stamps `CLAUDE_PIPELINE_SESSION=1` into the headless sessions it starts; where
that is set, treat the result as the agent's death and apply this contract. On #542 the round-1
reviewer hit `Agent stalled: no progress for 600s`, its result read as above, and the orchestrator
stopped on "you interrupted the round-1 reviewer" — neither of the two early ends — forfeiting the
loop, which another session then re-ran (#542/PR543, 2 rounds). Why the harness words it so was not
established.

## Write the run record — always, before you finish

*Cut down, R4-17 — the rule it supports:* “Append means `>>`, never `>` or the Write tool”

**Append means `>>`, never `>` or the Write tool.** The name is keyed on date and ticket, so a second
record for one ticket and day (a second run, or this run's pr-harden record after its resolve-ticket
one) lands on the first: `cat >` replaced it on #305 (2026-09-07, same run), #488 (2026-09-23) and
#477 (2026-09-24). Keep the name — it is how `pool-run` finds your record.

*Cut down, R4-18 — the rule it supports:* “The path needs no bookkeeping: the uuid is `$CLAUDE_CODE_SESSION_ID`”

**`context:` and `transcript:` are capture, and the second is what makes the first checkable.** A
run's own sense of how full its window was is a guess made by the thing being measured, so record
only what actually surfaced — a compaction, a context warning, and the step it happened at — and name
the transcript, which carries the ground truth a retro can measure instead. The path needs no
bookkeeping: the uuid is `$CLAUDE_CODE_SESSION_ID`, and the transcript is the one file
`ls ~/.claude/projects/*/"$CLAUDE_CODE_SESSION_ID".jsonl` prints. Take neither name from anywhere else:
the pool's headless `claude -p` runs have had no scratchpad path; #305, resumed from `~`, had its
scratchpad under that folder and its transcript under its worktree's; #527 recorded the folder of the
worktree it had moved into; and #514/PR524's third loop and #505/PR529 took their uuid from a directory
listing, which named another session. `no compaction · peak not surfaced` is the expected
reading and is worth writing for the same reason a clean run still gets a record — a field filled in
only when something went wrong biases every retro that reads it, in the direction of the runs that
went badly. Derive nothing from it here: whether context pressure costs quality is a claim about many
runs, and no run can settle it about itself.
