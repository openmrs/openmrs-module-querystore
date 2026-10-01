# resolve-ticket — the evidence behind the rules

`SKILL.md` states each rule as a run needs it. This file keeps what the rules rest on: the incidents,
measurements and history that `SKILL.md` carried until 0.22.0, moved here verbatim so that a run no
longer loads them and no measurement is lost. Headings match `SKILL.md`'s. Each entry quotes the rule
it supports as `SKILL.md` states it, then the passage as it stood in 0.21.3, so an entry can repeat the
words of its rule. From 0.23.0 the rules B-09 to B-11 quote are in `refuter.md`; their entries stay
under *Step 3 — Refute the plan, before any code* here.

A run does not need this file. Read an entry before changing or deleting the rule it supports: a
measured rule is not deleted without the measurement that retires it (`skill-retro`, Step 4).

## The autonomy contract

*Cut down, A-01 — the rule it supports:* “such a run clears its own state entry at its terminus, and must not reach for the override to escape a gate that was never going to release”

**A partial mode is a TERMINUS, not an abort.** `--plan-only` is defined to stop at the end of Step 3,
and no gate condition can ever be satisfied by a run that opens no PR and runs no round — so such a
run **clears its own state entry** at its terminus, and must not reach for the override to escape a
gate that was never going to release. The distinction matters because the override is the record of a
deviation: one taken on every partial run means nothing, and the time it records a real deviation
nobody will notice.

*Cut down, A-02 — the rule it supports:* “It refuses a yield on a backgrounded build or Monitor too, so wait inside the turn with `pr-harden`'s bounded foreground loop”

**The Stop gate covers the whole run, not just the loop.** Write the state entry at Step 1, before any
work — see **State** in `pr-harden`, which owns the format. From that moment `pr-harden-gate.sh`
refuses to let the turn end until the head being handed over has been reviewed with zero blocking
findings — and verified, where any verifier ran — or an override is recorded. That is what makes the
run unattended rather than merely intended to be. It refuses a yield on a backgrounded build or Monitor
too, so wait inside the turn with `pr-harden`'s bounded foreground loop (*And this session must not
busy-wait either*) — #273, #429.

## You may not be the only run on this machine

*Cut down, A-03 — the rule it supports:* “do not spell it onto a command line — interpolated it arrives as ONE argument, not several, and the build installs into a directory named after the whole string”

`$MAVEN_ARGS` is read by `mvn` itself, so a plain `mvn -o clean install` already picks it up: do not
strip it, do not spell it onto a command line — interpolated it arrives as ONE argument, not several,
and the build installs into a directory named after the whole string (#409; the measurement is in
`harden`'s *A mutation that reddened nothing has not shown the guard is dead until you know it RAN*,
which lost three configurations to it) — do not add `-Dmaven.repo.local` of your own, and do not be surprised that
`chartsearchai-api-1.0.0-SNAPSHOT.jar` installs somewhere under `~/.claude/pipeline/m2/`. That is the
point — it is the jar `omod` unpacks over `omod/target/classes`, so two runs sharing it means one
run's classes silently under the other's tests.

## Step 1 — Resolve the ticket, and read it

*Cut down, A-04 — the rule it supports:* “Never `--comments` for the ticket itself.”

**Never `--comments` for the ticket itself.** Off a terminal, gh 2.87.3 printed the comments and not
the body (measured 2026-09-23), so an issue without comments read as empty — seven runs, #236 to #491 —
and #480's with one comment read as that comment alone. `--json` carries both and paginates comments.

*Cut down, A-05 — the rule it supports:* “That endpoint serves unauthenticated. The `issues.openmrs.org` link people paste redirects to a dashboard and will not serve REST, so never reach for it.”

That endpoint serves **unauthenticated** (verified: `TRUNK-6429` → 200). The `issues.openmrs.org`
link people paste redirects to a dashboard and will not serve REST, so never reach for it.

*Cut down, A-06 — the rule it supports:* “Pre-flight the verifier here, not at the end.”

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

*Cut down, A-07 — the rule it supports:* “which an inline read-modify-write cannot, and under a parallel pool cannot safely be retyped”

`gate-state` is the only writer of either state file — it holds a lock across both and writes
atomically, which an inline read-modify-write cannot, and under a parallel pool cannot safely be
retyped: measured with 20 concurrent writers, the inline form kept 3 of 20 entries and raised nothing.
`pr-harden`'s **State** section has the rest of the subcommands.

*Added 0.24.1 — the rule it supports:* “`EnterWorktree`'s isolation guard then refuses what it cannot verify, git or not”

0.24.0 put only a pointer here, to `pr-harden`'s **State**. In #562 and #564 (2026-09-29 and -30) the
first refusal came seconds after `EnterWorktree`, at this step's first `gate-state` write, and 19 of
the two runs' 23 refusals came before Step 9 loaded `pr-harden`, so the text the pointer named was not
in context when the guard refused. The measurement is in `pr-harden`'s `evidence.md`, *State*, 0.38.1.

## Step 2 — Plan before code

*Cut down, B-01 — the rule it supports:* “Delegate the searching only when the question is broad and of unknown shape”

Delegate the *searching* only when the question is **broad and of unknown shape** — "where does X
live, across conventions I cannot guess", "every call site of Y". When it is narrow and the target is
named — what does this class expose, where is this global property read — grep it yourself. Measured
on this skill's first run: four targeted greps answered every planning question while a dispatched
`Explore` agent was still working, so the delegation duplicated the work rather than saving context.
Either way the judgement stays here. Then write down:

*Cut down, B-02 — the rule it supports:* “State the trade explicitly, and rule out "test it differently" before taking it”

- **If any part of the plan exists ONLY so the change can be tested, say so and label it a TRADE.**
  This is where a plan quietly gets worse while looking more rigorous. Measured on this skill's
  seventh run: the fix was behaviour-neutral and therefore unobservable, so the plan changed a
  *second* production decision — the order of two branches — purely to make the first one testable.
  Both refutation passes accepted it. Round 1 of the review loop then refuted it in one move: the
  reordering was not required by the ticket, and it ADDED exposure to the very defect the ticket
  exists to remove, because in the old order an inconsistent state was harmless and in the new one it
  reached a clinician-facing chip. State the trade explicitly, and rule out "test it differently"
  before taking it — question 7 below is that check.

*Cut down, B-03 — the rule it supports:* “Take the defensible reading and record it here; do not stop to ask.”

- **Every assumption you took** on an ambiguous reading of the ticket. Take the defensible reading and
  record it here; do not stop to ask. This list goes into the final report verbatim, which is what
  makes an unattended run auditable rather than merely finished.

## Step 3 — Refute the plan, before any code

*Cut down, B-04 — the rule it supports:* “One fresh subagent, one pass, read-only. Its only job is to try to break the plan.”

One fresh subagent, one pass, read-only. Its **only** job is to try to break the plan. This is the
cheapest gate in the pipeline and it guards the failure this module is most prone to: `CLAUDE.md` is
largely a catalogue of changes that looked obviously right and measured wrong — a uniform ATC veto,
re-ranking by longest alias, identity keyed on `rxcui`, tightening `hasAllergyToken`. Catching one at
plan time costs one agent and no code. Catching it in round 3 costs three rounds of implementation
plus the rounds spent polishing the wrong fix.

*Cut down, B-05 — the rule it supports:* “Snapshot the worktree hash before spawning and compare it after”

Snapshot the worktree hash before spawning and compare it after — the refutation gate is read-only by
instruction, but "read-only by instruction" is not a guarantee, and `pr-harden`'s **State** section
carries the measurement of what an agent that dies mid-mutation leaves behind. Tell it to restore
anything it changed **before** it reports.

*Cut down, B-06 — the rule it supports:* “Snapshot `git branch --show-current` beside the hash, at every delegation in this skill.”

**Snapshot `git branch --show-current` beside the hash, at every delegation in this skill.** A diff
hash cannot see a `git checkout`: both trees are clean, so the hash matches and the switch is
invisible. Measured on the run that added this line — a review agent left the worktree on `main`, and
only the NEXT agent's own branch check caught it before edits landed there. A wrong-tree edit is
recoverable exactly until something commits on top of it.

*Cut down, B-07 — the rule it supports:* “Tell the refuter not to spawn subagents of its own”

Record the await — append to the entry's `awaiting` list — before spawning it, and clear that list
on ANY terminal outcome: a result, or the harness reporting the agent failed, stalled or was killed.
A death leaves a fresh await that the gate honours for the full hour, which is a licence to stop the
run with nothing running. Tell the refuter **not to spawn subagents of its own** — nested delegation
killed an agent on this skill's first run — and if it dies, retry twice with something changed between
attempts before taking the labelled deviation (`pr-harden`'s **State** section carries the contract).

*Cut down, B-08 — the rule it supports:* “When the run is unattended, do not yield at all — collect the agent inside the same turn”

**When the run is unattended, do not yield at all — collect the agent inside the same turn**, by
spawning it with `run_in_background: false` (`pr-harden`'s *Collecting in the same turn*). A
`claude -p` process stops a background agent still running 600 s after its turn ends and then exits,
and until the 2026-08-26 fix the recorded await let the gate allow that quietly. That day #297 ended
at this step with this very refuter outstanding, and #310 ended in `/harden` with its reviewers
stopped at that ceiling — both with committed work and no PR. `pr-harden`'s **State** section
carries the measurement.

*Cut down, B-09 — the rule it supports:* “Name the claim, and name what would measure it.”

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

*Cut down, B-10 — the rule it supports:* “grep the test tree for a guard that reads source or `.class` files before believing it”

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

*Cut down, B-11 — the rule it supports:* “An objection without a citation is not an objection.”

**An objection without a citation is not an objection.** It must point at a `CLAUDE.md` rule, a
specific line of code, or a recorded measurement — same discipline as the reviewer's failure-mode
sentence, and for the same reason: an agent told to find problems will manufacture them, and a
manufactured objection at plan time sends the run down a worse path than the one it replaced. That
risk is sharper here than in the review loop, because there is no code yet to check the objection
against. A plan it cannot fault gets an explicit empty `objections` list, and the `checked` array is
what stops silence being mistaken for coverage.

*Cut down, B-12 — the rule it supports:* “So when an objection lands on a claim, cut the unsupported clause rather than replacing it with a better-sounding one”

**Revise by deleting, not by re-wording.** The revision is itself unverified prose written fast under
the pressure of an objection, and it is a live source of the next false claim: measured, gate pass 2's
blocking objection was against a claim the pass-1 REVISION had introduced, and the `/harden` run later
in that same session caught four more of the shape, twice in a correction from the round before. So
when an objection lands on a claim, cut the unsupported clause rather than replacing it with a
better-sounding one, and re-derive any figure you carry across rather than restating it. `harden` and
`pr-harden` both carry this rule; it belongs here too, because Step 3 is where the first rewrite
happens.

*Cut down, B-13 — the rule it supports:* “take it from the compiler or a whole-tree search before adopting the design it implies”

**Check it, do not estimate it — the objection's own numbers included.** Outcome 2 turns on the
citation's *authority* and hands you no instrument for testing it, and by then there is no third gate
pass to catch a citation that merely looks like it decides. So where the citation is a count or a
declaration that a build settles — call sites, a modifier — take it from the compiler or a whole-tree
search before adopting the design it implies, rather than from a grep of the file in front of you. Two
runs, and the two costs differ by exactly that check: on #263 gate pass 2 asserted two named methods
are package-private and callable from a same-package test, where the answer is that all three are
`private` — verified before applying, cost ~0. On #255 the gate's own estimate, from a grep of one
file, was 3 test call sites; the run adopted the in-place widening that made cheaper, the compiler
then reported 33 across three files, and the design went back to an added overload for one
implementation attempt — neither a round nor a cycle, and spent before any review round. `harden` and
`pr-harden` both bind this rule on the count that decides a control-flow decision; here the count
decides a design. It binds the RUN and not the gate, because the gate is read-only by instruction
above and a call-site count from the compiler means changing a signature and building.

*Restated, B-14 — where the rule stands:* “the discriminator is not how many objections have been raised but whether the objection's citation determines the answer”

Count citations that decide, not objections raised.

*Moved, B-15 — the rule it supports:* “This is convergence, not iteration, so there is no third gate pass”

Measured on the first real run of this skill, against issue #285: pass 1 refuted the plan's stated
reason and left its conclusion intact; the revision reversed the conclusion; pass 2 refuted **that**,
citing three existing tests, and in doing so named the answer — the original conclusion, on new
grounds. Two blocking objections, no deadlock, and a third pass would have re-gated a settled
question. Four false justifications died before any code existed, one of them on its way into a PR
body.

## Step 5 — Test first, then the fix

*Cut down, C-01 — the rule it supports:* “Check `git branch --show-current` before you edit, not only before you commit.”

**Check `git branch --show-current` before you edit, not only before you commit.** Every phase of this
pipeline delegates, and an agent that runs `git checkout` silently redirects everything after it. On
this skill's fourth run an agent left the worktree on `main` and four edits landed there; it surfaced
only because the test count dropped by exactly the size of the new test file, and had those been code
edits with a commit after them they would have gone to `main`. `pr-harden` states this rule for
committing; committing is too late, because by then the edit is already in the wrong tree.

*Cut down, C-02 — the rule it supports:* “Edits made by script need three guards, because all three failures are silent.”

**Edits made by script need three guards, because all three failures are silent.** You will edit by
running short scripts rather than by hand; measured on this skill's third run, each of these cost a
cycle or a review round. `str.replace` returns the string unchanged when it matches nothing and the
script prints success anyway — so **assert the target text is present before replacing**, and let the
assert stop the script rather than falling through to the next edit. A replacement bounded by
"from here to the next method" can span further than you meant — so after any multi-line edit, **count
what should still be there** (test methods, symbols) against what you expected; one such slice deleted a
whole test method and everything still compiled. And **verify by reading the file back**, because the
script's own report is not evidence: the other two both announced success.

## Step 7 — Harden with context, once

*Cut down, C-03 — the rule it supports:* “While harden runs, write its awaits to BOTH state files.”

**While harden runs, write its awaits to BOTH state files.** The gate armed at Step 1 is
`pr-harden-gate.sh`, which reads `~/.claude/pr-harden-state.json`; harden's own awaits are written to
`~/.claude/harden-state.json`. So a harden cycle blocked on its Phase 2 agents is invisible to the
armed gate, which then refuses the yield the cycle needs in order to wait — the same shape #298
measured in the un-nested case at "two ten-minute in-turn wait loops", and it fired again on the #302
run with four agents live. That is what `gate-state`'s default scope is for — **omit `--only` and one
command writes both**, so the pair cannot come apart the way two commands could:

## Step 8 — Draft PR

*Cut down, C-04 — the rule it supports:* “`Fixes` only if the PR actually closes the ticket. Otherwise `Refs`, and say why in the body.”

**`Fixes` only if the PR actually closes the ticket. Otherwise `Refs`, and say why in the body.**
GitHub acts on the keyword, so a PR that delivers something SHORT of the ticket — an instrument for a
defect it does not fix, one part of a multi-part ask, a diagnosis the ticket asked for before a
remedy — silently closes an open defect on merge. Measured: a run whose PR body said `Fixes #299` in
its first line and, four paragraphs down and in bold, "this PR should not be read as closing #299".
That contradiction was round 1's blocking finding, and it fails closed and quietly — nothing errors,
no check reddens, and the next person looking for open defects does not see it.

*Cut down, C-05 — the rule it supports:* “Check the field rather than the wording, with `gh pr view <n> --json closingIssuesReferences`, once the body is written and again after any later edit to it.”

**Check the field rather than the wording, with `gh pr view <n> --json closingIssuesReferences`, once
the body is written and again after any later edit to it.** That field has named an issue the PR does
not close on two runs. The cause was the same both times — a closing keyword whose scope reached an
adjacent reference — but the remedy was not, which is the argument for checking the field instead of
learning a rule about the prose: on #250 rewording the offending sentence was enough, and on #317
rewording changed nothing while separating the two references onto their own lines and naming the
non-closing one without a `#` did. Both runs caught it themselves, so this makes a practice that has
already worked twice repeatable; what it guards against is the run where nobody looks, because merging
then closes an open defect and nothing reddens.

*Cut down, C-06 — the rule it supports:* “patch it mid-loop ONLY to satisfy a blocking finding, and at the end rewrite it against the final head”

**Write it once here, and RE-DERIVE IT WHOLE before the PR is marked ready — never patch it across
rounds.** The body describes code the review loop is about to change under it, so an incremental edit
is how it comes to assert something false. Measured on this skill's fourth run: a round-1 edit made the
body say the ticket's second named shape was not fixed, round 2's fix made it fixed, round 3's only
blocking finding was that sentence — and rounds 4, 5 and 6 each caught another stale claim in the same
paragraph set ("nothing outside a folded chip is touched", two mutation counts, "the three sites that
quoted it"). Four consecutive rounds whose top finding was the description. So: patch it mid-loop ONLY
to satisfy a blocking finding, and at the end rewrite it against the final head, re-measuring every
figure in it at that point rather than carrying one forward.

*Cut down, C-07 — the rule it supports:* “when a later round corrects a claim anywhere in the repo, re-read the body for the same claim”

**Treat the body as part of the change, not as a summary of it.** It is the durable public rationale
attached to the closing of the ticket, no test can fail on a false sentence in it, and a repo-wide grep
for a claim you later correct will never reach it. Two consequences, both measured on this skill's third
run. Every figure in it carries the dataset it was measured over — a chip count taken against a
four-entry fixture is not a claim about the shipped knowledge base, and stating it without its base is
how the same sentence became a blocking finding twice. And when a later round corrects a claim anywhere
in the repo, **re-read the body for the same claim**: round 2 of that run found its only blocking
finding here, the sixth home of something already fixed in five files, still standing because the
orchestrator had edited the body for an unrelated reason without re-reading the paragraph above. Record the new PR number in the state entry.

## Write the run record — always, before you finish

*Cut down, C-08 — the rule it supports:* “Append means `>>`, never `>` or the Write tool.”

**Append means `>>`, never `>` or the Write tool.** The name is keyed on date and ticket, so a second
record for one ticket and day (a second run, or this run's pr-harden record after its resolve-ticket
one) lands on the first: `cat >` replaced it on #305 (2026-09-07, same run), #488 (2026-09-23) and
#477 (2026-09-24). Keep the name — it is how `pool-run` finds your record.

*Cut down, C-09 — the rule it supports:* “Take neither name from anywhere else.”

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

## Anti-patterns

*Cut down, C-10 — the rule it supports:* “Don't publish a count you would have to re-measure every round; publish the method.”

- **Don't publish a count you would have to re-measure every round; publish the method.** Measured on
  this skill's fourth run, every figure that named a tally went stale, several of them twice —
  "1342 tests", "negating it reddens exactly two", "the three sites that quoted it", "eleven cases" —
  and each recurrence cost a round, because a review agent re-measures what a comment asserts. Two of
  them went stale in the very round that added the thing they had miscounted. The recurrence stopped
  only when the enumeration was deleted and replaced with *"mutate the line and read the failures"*.
  Prefer that form. An exhaustive list that is wrong is worse than no list, because it invites the next
  reader to treat the extra failure as a regression they caused.

*Cut down, C-11 — the rule it supports:* “spend one attempt trying to falsify it; prefer stating what the thing DOES over what it excludes”

**And the rule is not about tallies — it is about claims you cannot check.** A universal or an exhaustive characterization is the same defect in different grammar, and it slips past a reader watching for digits: *any*, *only*, *exactly*, *all*, *never*, *the whole*, *cannot*. Measured on the seventh run, five such claims in three consecutive cycles, each written to correct the previous cycle's false claim and each false in turn — "any looser pattern would reject" (looseness has more than one dimension), "it only re-admits `M01AE0`" (it re-admits any single trailing digit), "matched only the 5- and 7-character shapes" (the old pattern matched 6 too), "exactly the two levels the ladder is known to be handed" (nothing on the path validates a code's shape), and one that mis-numbered the very level it was excluding. So before writing one about code you just wrote, spend one attempt trying to falsify it; prefer stating what the thing DOES over what it excludes; and name the residue rather than claiming there is none.

*Restated, C-12 — where the rule stands:* “So never spawn a subagent to write the implementation, and never review your own work here.”

- **Don't spawn a subagent to write the implementation.** Then nobody holds the writing context, the
  judgement calls get made by an agent nobody can steer, and Step 7's harden loses the one advantage
  it has over the review loop. `Explore` for searching is fine; the judgement stays here.

*Cut down, C-13 — the rule it supports:* “Don't change production to create observability without ruling out a structural pin first.”

- **Don't change production to create observability without ruling out a structural pin first.**
  A plan that says "this is behaviour-neutral, so I must change X to make it testable" is one move away
  from making the code worse in the name of rigour — and the move it skipped is a grep of the test tree
  for a guard that reads source or compiled class files. Measured on the seventh run: a second
  production decision was changed purely for coverage, both gate passes accepted it, and round 1 of the
  loop showed it added exposure to the defect the ticket existed to remove while buying coverage that
  was available another way. Step 3's question 7 exists for this; if you take the trade anyway, label
  it as one in the plan and in the PR body.

*Restated, C-14 — where the rule stands:* “This is convergence, not iteration, so there is no third gate pass”

- **Don't let the refutation gate become a loop.** The loop is a *third gate pass*, not a second
  revision: two blocking objections are fine when the second one settles the question, and a third
  pass re-gates something already decided. Step 3's three outcomes are the rule. And don't argue the
  plan's case to the refuter — an agent primed with your reasoning agrees, which is the one outcome
  that gate cannot use.

*Restated, C-15 — where the rule stands:* “`Fixes` only if the PR actually closes the ticket. Otherwise `Refs`, and say why in the body.”

- **Don't write `Fixes` on a PR that does not fix the ticket.** It is the default this skill hands
  you in Step 8 and it is wrong whenever the delivery falls short of the ask, which is exactly the
  case a careful run produces — an instrument, a diagnosis, one part of several. The merge then closes
  an open defect and nothing anywhere says so. See Step 8; and having switched to `Refs`, hand every
  reviewer the issue number by hand, because the field they resolve it from is now empty.

*Restated, C-16 — where the rule stands:* “Do not review the PR yourself while it runs, and do not pre-empt round 1”

- **Don't review your own PR after Step 8,** and don't pre-empt round 1.

*Restated, C-17 — where the rule stands:* “On any abort above, write the override into the state entry with its reason”

- **Don't leave the state entry behind on an abort.** Record the override and its reason, or the next
  turn in this repo is blocked until the 6-hour expiry.

*Cut down, C-18 — the rule it supports:* “Don't reinvent a `CLAUDE.md` entry point”

- **Don't reinvent a `CLAUDE.md` entry point** — `buildPrefixedText`, `cosineSimilarity`,
  `substanceKey`, `findImpliedByDrugName`, `groundedForWire` and the rest exist because a second
  implementation of each has already gone wrong once. Steps 2–3 are where to catch that.
