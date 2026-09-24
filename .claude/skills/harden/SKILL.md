---
name: harden
description: Run /review passes on the current slice until they stop finding substantive issues, then one /simplify polish pass. Use when the user wants to harden a code slice end-to-end without manually orchestrating the review/simplify dance. Trigger phrases include "harden this", "polish until done", "iterate until convergence", "harden".
version: 0.42.0
---

# Harden

Iteratively review and polish the current slice in two phases. Phase order matters: review catches structural issues (correctness, missing tests, design concerns); simplify catches polish (duplication, naming, micro-efficiency). Doing simplify on structurally-incomplete code wastes work — review-first surfaces the real fixes before polish happens.

## Phase 1: Structural review (/review style)

Run review passes until structural concerns converge. Each pass:

1. One comprehensive review covering correctness, conventions, performance, tests, security, **and integration** (see "Trace outward" below).
2. Apply genuinely actionable findings.
3. Verify with the build (`mvn -pl api install` for Maven, project-equivalent otherwise).
4. Decide: another review pass, or move to Phase 2?

### Trace outward (mandatory in every Phase 1 pass)

The slice is a piece of a bigger machine. Reviewing it in isolation hides the bugs that live at its boundaries — the slice's intrinsic code is correct but it desyncs with the rest of the system at runtime. In every Phase 1 pass, follow each of these threads at least one level out from the slice and write down what you found:

- **Trigger paths.** For each output the slice produces (an event, a document, a computed value, a denormalized field), identify every upstream state that affects it AND every code path that should cause re-production. Verify each mutation path actually fires the trigger. Trigger gaps are the most common Phase 1 miss — the slice is correct but a sibling service mutates shared state without notifying it.

- **Optional dependencies absent at runtime.** For each `provided`-scope dependency, `aware_of_module`-style soft declaration, or any other "may not be installed" relationship, walk through what happens when the dep is absent. For static class references, follow the JVM classloading chain (supertypes, generic bounds, annotations) — does the slice's class still resolve? For Spring-managed code, would eager singleton init force a load that fails? Soft-dependency declarations do **not** shield JVM-level class resolution.

- **Lifecycle order.** For lifecycle-sensitive code (Spring beans, event listeners, schedulers, SPI contributors), verify the registration timing matches consumer expectations. Will the slice be registered before consumers scan for it? Will a scheduled job start before its inputs are ready? Will a listener subscribe before the events it cares about start firing?

- **State propagation across module/service boundaries.** For any derived/computed/denormalized value the slice exposes, enumerate every upstream service that can mutate that value. Does each such service trigger the re-computation contract (event, dirty-flag, save, callback)? A serializer that correctly computes `getX()` is still broken if half the code paths that mutate the inputs to `getX()` don't fire the event the indexer listens to.

- **Invalidated invariants in unchanged neighbors.** Re-read the *unchanged* code adjacent to and depending on the change — sibling methods in the same class, callers, teardown/shutdown paths, and any comment, Javadoc, or ADR note that states an assumption. Ask of each: *does my change make this statement false?* The most-missed defects are not in the lines you edited but in unchanged code whose documented invariant your edit silently invalidated — a `stopped()`/teardown comment that says "only X starts this" after you added a second starter, a Javadoc that enumerates "the only callers," a "this is the only path that…" remark. Diff-scoped review structurally cannot catch these: the stale code never appears in the diff, so it is never read. You must re-read the neighbors *with the change in mind*. A now-false comment is a Phase 1 finding even though it "doesn't break runtime" — it breaks the next maintainer's mental model, which is how the real bug ships later.

- **Re-derive the merged result from scratch.** When the slice was built across multiple edits or prior passes (a guard added in one pass, an error path added in another), do not trust the incremental reviews that approved each piece against the state *at the time it was added*. Re-derive the correctness of the FINAL combined code from zero — especially for concurrency, state machines, and lifecycle flags. Adversarially interleave the code paths you added separately: the bug lives in the seam between two individually-correct mechanisms (e.g., a CAS guard from one pass composing with a catch-and-reset from another to open a window where a concurrent caller observes a false success). This is also where "already settled" briefings betray you — telling a reviewer (or yourself) that an area is closed suppresses exactly the re-examination that finds composition bugs, so re-derive it firsthand instead of declaring it done.

If any thread surfaces a concrete failure mode ("if we ship, X breaks because Y"), it is a Phase 1 finding even if the fix lives outside the file you're hardening. The slice's correctness contract spans its boundaries.

### Test coverage (mandatory in every Phase 1 pass)

For each behavior change the slice introduces (a new method, a changed signature, a new code path, a new contract, a new invariant), name the test that exercises it. The test must:

- Fail on the pre-change code (or would have, retroactively applied).
- Pass on the post-change code.
- **Verify the runtime effect — not a proxy for it, not an analogy.** Asserting the *artifact* of the change (the generated SQL/HQL string, the config key, the serialized shape) proves it was produced, not that it runs; "mirrors a pattern already in production" proves the sibling, not the variant you added. Both are necessary-not-sufficient. Ask plainly: **has this path ever executed on real input, with the effect observed?** If the only coverage is a shape assertion or an analogy, that's a partial-coverage gap — name it.

**And ask what the FIXTURE can express, not only what the test asserts.** Where a rule rests on how
some external system behaves — a judge, a parser, a remote — check whether the stub standing in for it
can even produce the counterexample. Measured on the #302 run: every test of a new rule drove a stub
that always refused a conjunction, so the rule's central premise ("a correct judge says no") was
assumed and never exercised; the cell where the real system says yes was unreachable by the whole
suite, and when a reviewer finally constructed it the design reversed. Cost: four cycles of work built
on the unexamined premise. A premise no fixture can falsify is not covered, however many tests name it.

**The stand-in is not always a stub — it can be the INPUT POPULATION a measurement enumerates, and it
need not be a measurement you wrote.** Two runs, one shape: a candidate population keyed on rows'
display names, in which the tie the rule turned on cannot arise, so the result came back clean in good
faith. On #250 an adversarial sweep of a two-clause predicate "took each row's DISPLAY NAME as the
recorded order, a population where no two rows of a family can tie above rank 0"; the unguarded half of
the line was found instead by a reviewer's mutation, at a cost of one round. On #268 the sizing the
TICKET offered ("0 of 36 reachable") measured a display-name population too, while the rule turned on a
leg that ties on a name that is no row's display name — caught at the gate, before code. Ask of any
clean or zero result what its inputs could not have produced, and ask it of a measurement you rely on
as readily as one you ran. **And of a STABLE result, ask what the repeats could not have varied.** On
#315, n=3 byte-identical answers were published as stability while every repeat reused the server's
cached prefix — the engine's own javadoc says that cache makes a borderline argmax non-deterministic, so
the repeats measured the cache, and the prompt figures resting on them were weaker than they looked.
Name what is reset between repeats; where nothing is, the repeats never exercised the path a first run
takes. **And of a PASSING check, ask what it actually examined.** A check that discovers its own subject —
a source or class-file walk, a directory scan, a script reporting on output it captured itself — can
return the same clean result whether the subject was compliant or absent. Two runs of #315: a walking
architecture guard passed every rule on a wrong source root, having scanned nothing, and a capture
script wrote its done-marker without checking, so an arm that captured nothing read as a clean, empty
A/B with exit 0. Make an empty discovery FAIL, and choose the thing you assert was found so that a
sibling could not supply it — the same run found that asserting the intended root merely exists is not
equivalent, because the sibling omod module carries the same package path.

A behavior change without a named test is a Phase 1 finding — even when the code looks "obviously correct," "matches an existing pattern," or "is trivially small." Never-executed code is unverified code.

**Blocked-path exception (compile, infrastructure, OR un-fabricatable input).** When you claim part of the slice "can't be tested" — won't compile yet, needs a real DB, or the triggering input can't be constructed (a dangling FK an FK-enforcing test DB rejects) — split the claim: name the sub-path genuinely blocked AND the adjacent one that is NOT. You usually can't fabricate the error/orphan input, but you can still execute the new code on *valid* input and assert it runs. "Can't reproduce the failure case" is not "can't run the new code at all." Sketch the test contract in writing for whatever stays genuinely blocked.

**Stop Phase 1 when ANY are true:**
- The verdict is "ready to commit" / "no further review value."
- Two consecutive passes return only cosmetic items (e.g., test assertion tightening, import ordering).
- The pass starts re-flagging items prior passes addressed.

**Transition gate** (forcing function — say BOTH out loud in the report before moving to Phase 2):

> "Phase 1 stopping condition met: [last pass returned no further review value | last two consecutive passes returned only cosmetic items | last pass started re-flagging prior items]. Edits made by that last pass: [N]."

> "Integration questions answered: what happens to this slice when {an upstream service mutates without notifying me / an optional dependency is absent at runtime / a consumer scans before I register / a sibling service silently changes shared state / an unchanged neighbor's documented invariant is now falsified by my change / two separately-added mechanisms compose into a race or contradiction}? Answer: [concrete behaviors observed or verified, one line each]."

> "Behavior changes without tests: [enumerated — for each, state 'test added: <name>' or 'compile-blocked, test contract sketched: <description>'] or 'none.'"

If you cannot truthfully complete ALL THREE sentences, the slice is NOT ready for Phase 2 — run another Phase 1 pass. Two passes both finding substantive issues is a signal to keep going, not stop. **Conversely, a pass that itself found a substantive (non-cosmetic) issue cannot be the last pass: finding a real gap is evidence the slice was not fully explored, so convergence requires a *subsequent* pass that finds nothing substantive.** Pass count is not the threshold; convergence is.

## Phase 2: Polish (/simplify style)

One polish pass, run after Phase 1 has converged. A second one happens only where the first escalated and Phase 1 re-converged; the gate below says so. The pass:

1. Spawn four parallel review agents (reuse, quality, efficiency, integration) over the current diff, in ONE message, each with `run_in_background: false` where the `Agent` schema carries it — the default is background, and an unattended run's gate refuses the yield that leaves them outstanding (#433; `pr-harden`'s *Collecting in the same turn*). Where a previous Phase 2 ran, brief its agents with the applied and deferred lists from it so they don't re-surface them.
   - **Never pass `model` to any of them.** A per-call override beats both the agent definition's
     frontmatter and settings.json, so it is the strongest of the levers and the only one a running
     pass can pull on its own initiative — the others are set in a file or on the command line,
     outside any session. It is how a pass ends up reviewed by a weaker agent than the one that
     wrote the code — and the temptation is strongest on the lenses that keep
     returning clean, which is exactly where a missed finding is invisible. Two things measured on
     #354, stated apart because they say different things. The REUSE lens was put on a cheaper model
     and still returned real findings twice (duplicated test helpers; one measurement restated in
     three homes), so "this lens is cheap, it can take a cheaper agent" was false of it. The
     EFFICIENCY lens returned nothing on any of its four runs — but it was never run on the session
     model, so nothing there separates a quiet lens from a quiet agent, and that pairing is the one
     the run cannot speak to. Sharper: the pass whose CLEAN verdict closed Phase 2 was a retry moved
     to a cheaper model, and the next cycle's agent — same head, session model — found a sentence in
     citable production text that was false. A `PreToolUse` hook
     (`~/.claude/hooks/no-subagent-model-override.sh`) refuses the call, so this is enforced rather
     than asked. What it refuses is the per-call parameter and nothing else — an agent definition's
     `model:` frontmatter and a configured default subagent model both outrank the session model and
     produce no call for it to see, so do not read the hook as a guarantee that every subagent runs on
     the session model. That scope is stated once, in the hook's own header beside the code.
   - **Only ONE of them may mutate the worktree, or give each its own.** This is the one place the skill
     contradicted itself: Phase 2 mandates four *parallel* agents and every brief tells them to
     mutate-and-restore for evidence, so the four are the same hazard to each other that the
     orchestrator is to them. Measured: four concurrent agents on one checkout produced a tree that
     went clean→dirty with "a revert of a branch order I did not make", a build that collapsed into
     842 `NoClassDefFound` errors, and an agent that opened on another's uncommitted mutation and spent
     a detour concluding the branch was red — two of four reports contaminated, and both flagged it
     themselves rather than being caught. Pick one: pass `isolation: "worktree"` so each agent gets its
     own checkout (the Agent tool supports it, and it is the only option that keeps all four able to
     run code); or license exactly one agent to mutate and tell the other three to read only; or run
     the mutating ones serially. Whichever you pick, say it in every brief — "be careful" does not
     survive contact.
   - **If you isolate, do not assume the worktree is on the branch under work.** Name the ref in every
     brief and have the agent confirm its diff is the SIZE of the change before it reviews anything —
     non-empty is fail-open, and that is the shipped wording being satisfied by the case it exists to
     catch. The cause
     differs between runs and neither is worth diagnosing here: on the #269 run every agent's worktree
     "opened at origin/main, not the PR branch, so all six had to check the branch out themselves",
     and two reported the diff they were asked for came back empty until they did; on the #250 run the
     branch existed but git refused it to a second worktree because the main one held it, so agents
     needed `git checkout --ignore-other-worktrees`. Both cost a detour per agent and no round or
     cycle, which is why this is one sentence in the brief rather than a step.
     **And name the BASE, never a local branch name** — on #336 four isolated agents all found the local
     `main` ref stale by many commits, so `git diff main...HEAD` showed ~15k lines and every brief had to
     name the base sha explicitly. That is the diff a non-empty check waves through. `pr-harden` Step 1
     carries this for its reviewer, with its own measurement; the hazard is the same here.
     **And a worktree left behind by a killed run is a dead agent's tree, not the head — the
     orchestrator is who meets them.** On #348/PR369 three of four orphans differed from it: a swapped
     prompt clause, a deleted clause, and a `System.err.println` probe with an added map. Nothing reaps
     them, because the run that leaves them is the one that died.
   - **An idle output or transcript file is not evidence an agent has stopped.** Two runs measured it
     from opposite ends: on #293 the agent output files "stay at 201 bytes until the agent finishes",
     so there was no progress signal to wait on and the run burned blind `sleep` loops; on FM2-700
     `isolation: "worktree"` agents "produced 156-byte transcripts that never grew", that was read as
     a stall, and two were killed mid-investigation — their kill notices showed both working, at a
     cost of two discarded agents and about twenty minutes. So do not infer death from a file's size
     or mtime. Inside the gate's allow a terminal outcome is one the harness reports, and it is the
     signal *Record the cycle so the gate can enforce it* has you clear the `awaiting` entry on; past
     that allow what decides is the bound, which is a clock rather than a liveness check. Where you
     need progress sooner, watch something the agent's WORK touches — FM2-700 used the worktree's own
     `target/`, which presupposes isolation, so #293's half of this is not closed.
     **And never read that file for the RESULT either** — it is the agent's transcript, not its
     report; `pr-harden`'s **State** section owns how a delegated agent is collected, and why a
     poll costs more than it returns.
   - **Commit before anything mutates the tree — the cycle's work before spawning them, and your own
     measurement probes too — and do not edit the tree while they run.** `git checkout -- <path>` to
     undo a probe restores HEAD, so on a file carrying uncommitted intended work it discards that work
     as well: measured on the #302 run, four production edits vanished that way and the empty
     `git diff --stat` afterwards read as "restored" rather than "reverted". These agents are told to mutate-and-restore for evidence, and a restore comes from what the agent READ — so an edit that lands after it read and before it restores is silently reverted. Measured: a quality agent mutated a guard to test whether a case could discriminate it, restored the file from its remembered copy, and reverted a fix applied in the meantime. It compiled and the suite passed; it surfaced only when a later test failed inexplicably. A commit is the one thing a remembered restore cannot undo. Tell them to restore with `git checkout -- <path>`, never by rewriting remembered content. And when a restore DOES discard intended work, say where to look: the registered PreToolUse hook copies modified tracked files outside the repo before the destructive command runs, best-effort and bounded, and prints the destination and count — trust that printed message rather than assuming the file is there. Both incidents in this window were found by someone other than the agent whose command caused them, so the hook's own output had already scrolled past by the time anyone was looking.
   - **integration** is not intrinsic polish. It asks: does the slice degrade gracefully when neighbors are missing, misordered, or silent? Does state propagate correctly across module/service boundaries? Does the slice's runtime contract hold when an upstream service violates an implicit assumption (e.g., mutates shared state without firing the expected event)? It revisits the Phase 1 "Trace outward" threads with a polish lens — looking for the gaps Phase 1 might have missed because the failure mode was framed as "fine in the happy path."
   - In every agent's brief, require them to trace at least one level out from the slice (callers, callees, lifecycle, optional deps) before declaring "nothing new." Reviews scoped to the file diff alone miss the bugs that live at boundaries.
2. Aggregate findings across the four agents.
3. Apply genuinely actionable items; skip stylistic noise and items prior passes addressed.
4. Verify with the build.
5. Report what it found, and whether any of it was substantive.

**Phase 2 runs ONCE — not to convergence.** A phase that re-runs until it changes nothing is a loop
feeding on its own output, because its gate cannot tell a polish edit from a substantive one and
polish normally produces some; it was the larger half of what made this skill take hours. Apply what the agents found, build, and stop. Polish you did not reach is a
deferral, and Reporting says what a deferral owes.

**The exception is severity, not volume.** If Phase 2 turns up something SUBSTANTIVE rather than
polish — a real correctness bug, a missing test for a critical path, a leaky abstraction — it
escalates on the spot: Phase 1 resumes and runs to its own convergence, and Phase 2 then runs once
against what that produced. A finding a previous Phase 2 already raised is not an escalation; it is
the re-flagging that signals context drift, and it ends the phase rather than reopening the run.

**Phase 2 gate** (say this out loud before declaring the phase done):

> "Phase 2 ran once, over [N] agents. Substantive findings: [none | enumerated, so Phase 1 resumes]. Edits made: [N]. Deferred: [enumerated | none]."

## Termination: Phase 1 converges, then one Phase 2 pass

The two gates above end a *phase*. Which one ends the RUN is what this skill kept losing, and the
answer used to be "one cycle that produces zero edits". This skill is 55-72% of a `resolve-ticket`
run's wall clock (196-251 min of 335-379, measured over the four runs of 2026-09-17), and the
maintainer's instruction on 2026-09-22 was that the zero-edit condition is why. Three convergence
loops were nested: a pass confirming a pass, a phase confirming a phase, a cycle confirming a cycle. Only the innermost was severity-aware. Phase 1's gate asks whether
the last pass found anything SUBSTANTIVE; Phase 2's and the cycle's asked whether anything was
EDITED, which cannot tell a polish edit from a substantive one. So the outer two re-opened the loop
on the loop's own output whenever it produced any, and a one-clause javadoc fix bought a whole fresh
Phase 1 + Phase 2 (#298, cycles 3 and 4). Not that polish ALWAYS edits — #298 converged on a cycle
whose measured zero covers its Phase 2 as well, so that universal is false, and the anti-pattern
below on *any / only / exactly / all / never* is the rule it broke.

So the run's condition is Phase 1's, and there is one loop:

> **`/harden` is complete when Phase 1 has converged and the single Phase 2 pass that follows it has run without escalating.** Phase 1 keeps its existing rule — a pass that found a substantive (non-cosmetic) issue cannot be the last pass. Phase 2's edits do not re-open Phase 1 and do not buy another traversal. Do not hand back to the user in between.

**What this does not license.** It changes what RE-OPENS the loop, never what counts as having
finished, and Phase 1's own bar is untouched: a now-false comment is still a Phase 1 finding, a false
universal in a javadoc is still substantive, and "it's basically converged" is still not the
condition — on #229 every one of the last seven passes found exactly one real defect, all prose, and
six would have shipped under that stop. Read that as a bound rather than a measurement of this rule:
#229's record carries no phase attribution at all, so whether those seven were Phase 1 passes is a
classification by the rule above and not something the record settles. What is gone is the confirming CYCLE, not the
confirming PASS. Two remedies that WOULD have relaxed the bar are refused and stay refused: a cycle
cap, which at the obvious value of 4 ends #298's converged 5-cycle run as did-not-converge, and
classifying a cycle by the provenance of its edits.

**And a prose finding about BEHAVIOUR is checked by running it, not by reading it.** A
documentation-only diff can carry a behavioural falsehood: on #302 the claim that `clauseScoped=true`
supersedes that issue's own treatment was written into `config.xml` and the README, and only driving
the real `verify` refuted it — the flag REMOVES the rule and reinstates the symptom. A coherence
sweep would have found that sentence coherent, and false. And where prose IS behaviour, the file it
lives in does not save it: a prompt paragraph a test asserts a substring of, a log or failure message
under assertion, a global-property default or the description shipped beside it.

**Report the edit count. It no longer gates.** At the close of each traversal run `git status
--porcelain`, count the commits, and report both. It is a fact the report owes, and it was never the
artifact claim the old rule made of it: a zero-edit cycle licenses "this process has stopped
producing", not "complete" — which is why `pr-harden` reviews the result in a context that did not
write it.

**Termination gate** (forcing function — the phase gates demand verbatim sentences and get them; this one was prose and got skipped, so it is the same shape). Before ending the run, say this out loud with the measured values filled in:

> "Phase 1 verdict: [converged — the last pass found nothing substantive | open]. Phase 2: [ran once, nothing substantive | escalated]. Edits this traversal, measured: `git status --porcelain` = [empty | N files], commits = N."

Emit it even when the answer is obvious. If you cannot fill in the Phase 1 verdict from a pass that actually ran, you have not run the check.

**If you are going to stop early anyway, label it.** Cost, elapsed time and turn length are **not** termination conditions and appear nowhere above; the rule has no cost exception. But a rule with no escape valve gets broken rather than invoked, so if you judge the remaining passes not worth their cost, take the valve and make the deviation legible:

> "I am overriding the termination rule, because [reason]. This run did **not** converge; Phase 1 was still open [or: Phase 2 escalated] and I did not run the pass that was required."

That is a permitted move. What is not permitted is ending the run without either the convergence line or the override line. And do not launder the override into a question — see the anti-pattern on handing the decision back.

This is deliberately cheap to satisfy and expensive to fake, which is the point — but it cuts both ways, so:

- **Do not manufacture a finding to look thorough.** A Phase 1 pass that finds nothing substantive is the goal, not a failure. If it finds nothing, say so and move to Phase 2; inventing a cosmetic item to justify the pass just buys another mandatory one.
- **Do not withhold a warranted finding to end sooner.** If you find something substantive on what you hoped was the last Phase 1 pass, fix it and run another. The rule exists precisely to stop "it's basically converged" from ending a run that still had a finding in it.
- Every applied change still needs its evidence: verified by build or test, and where it fixes a behavior, checked by reverting it and confirming the failure. **Where it ADDS a guard or clause, the same check is owed on that — deleted, its arms swapped, its comparison loosened, or rewritten in a semantically equivalent way** — because a clause the suite never discriminates is one the next change can remove for free.
- **A mutation that reddened nothing has not shown the guard is dead until you know it RAN.** It is a
  zero result, so ask of it what its inputs could not have produced — and where that zero would
  license deleting the clause, make the loop redden ON PURPOSE before believing it: run one mutation
  it must redden, and watch that fail. On #256 a mutation check reported zero red because the mutated
  method no longer took the argument the edit used, so nothing compiled and nothing ran; in a
  compiling form it reddened two cases. On #263 three configurations ran unmutated because `zsh` does
  not word-split an unquoted `$var`, and it surfaced only because a figure that should have moved did
  not. Reading the run's own output is what failed next, in both directions: two runs spelled a test
  selector `A+B`, which selects nothing (`-Dtest` takes a comma — measured 2026-09-14 on surefire
  3.5.5), and one read that void run as clean while the other read its BUILD FAILURE as a test
  failure. Residue the control does not close: a harness whose own failure is a spurious RED satisfies
  the control and the mutation alike.
- **A guard expected to stay GREEN needs a positive control, and no mutation of production reaches
  it.** The revert-check and the add-a-guard obligation both test a guard that SHOULD redden; a
  negative assertion — "nothing was injected", "this name does not appear" — is supposed to pass, so
  a green suite says nothing about whether its subject could ever have arisen. Build the case it exists for and watch it FAIL.
  Measured on #360: a control's chart came from a helper that wires no validator, so the collection
  it asserted was empty could not have held anything, and two successive review rounds — not the
  mutation check — found it. On #355 a verifier briefed to look for a partner the shipped data does
  not carry re-drove the contract with one it does, plus a control, rather than reporting the absence
  as a result.
  **And an exemption you WRITE into a guard is the same hole from the inside** — an allow-listed
  method, a by-name exempt file. It is not coverage you are tolerating; it is where the next defect
  lands, because the carve-out was made for a reason the next change does not honour. On #448 a guard
  allow-listed a whole method and the uncharged splitter was written inside it, and the round that
  tightened that rule and its neighbour had both walked through again — a round each, both raised by a
  fresh reviewer; on #445 the endpoint class was exempted from the new dialect rule it most needed, at
  2 cycles. So build the case the exemption ADMITS, and watch it pass.
- **And a control measures the HARNESS it ran in, not the property.** This is the mirror of *Residue
  the control does not close*: there a harness's own spurious RED satisfies the control, here its
  blindness satisfies the guard, and both leave a negative assertion standing over a channel nothing
  ever drove. Ask which logger and level the harness captures, how it RENDERS what it captured, and
  whether a sibling test's residue changes either. Rounds 2, 3 and 4 of #439, one round each: a
  capture raised one class's logger to WARN, so the same details logged at `info` passed; the shared
  capture helper rendered a throwable's TYPE alone, so the patient's medication names attached to a
  diagnostic exception passed every guard; and a leftover `LoggerConfig` from a sibling capture held
  the guard under one surefire run order and not another. A liveness precondition is not the answer —
  #439's passed on unrelated events while the negative it protects was vacuous.
- **And where the thing you changed REPORTS — a warning, a finding, a check — name the direction it
  must never fail in, and build the input for that direction.** Two runs, each found by a fresh agent
  rather than by the author: on #337 a carve-out applied to both operands withdrew the record-side
  exit, so an answer reproducing an operator note FAITHFULLY was reported, and the carve-out had to
  become per-operand; on #276 a boundary asserted to "fail toward silence" did the opposite —
  `statesWord` matched a fragment of a larger number and produced a false report on a published key.
- **And ask whether the thing you fixed has a SIBLING. The revert-check above cannot answer that** —
  reverting a fix shows the suite observes it, and says nothing about a second member of the same
  family still carrying the defect. Three runs, and in each one the un-widened sibling left the suite
  green: a normalisation compiled twice from one pattern, where widening one of the two made a name
  unfindable in the record that renders it (#293); a validity rule's detail corrected on one rule and
  not on the sibling rule beside it, and a literal asserted at one of its two call sites (#266); and a
  fail-open fixed in one script of a family, left standing in the member that was actually gated
  (#315). So ask what else is the same thing — a second definition, a sibling rule, another call site,
  a sibling script — and check that too. A green suite after fixing one member is not evidence that
  the family has one member.
- **And where the guard is over TEXT, mutate the SUBJECT too — the four mutations above are all of the
  guard, and none of them moves the thing it forbids.** Relocate that string: into a comment, across the
  file's own line-wrap, behind a block comment, into a sibling declaration outside the slice the guard
  reads — and check it still reddens. On the #315 run one guard was defeated five ways in turn, each
  found by the next fresh agent at a cycle or a round apiece and each fix opening the next, so treat no
  list of relocations as closed. Two the run paid for: bound the window at the construct it is about
  rather than at a line count, and have both halves of a two-sided guard read text normalised the same
  way.
- **Where successive fresh agents each defeat the same guard or predicate one more way, what ends the
  loop is a change in the KIND of question, not another entry on the list.** That is a rule about
  TERMINATORS, and is not *treat no list of relocations as closed*, which forbids believing a list
  complete. The family is not text guards alone: on #421 a production predicate over TYPES escaped
  three cycles running — exact type equality, then a raw type, then wildcard and type-variable bounds
  — each escape found by the next fresh agent. On
  #256 the escapes were a differently-typed field, a parenthesised initialiser, an annotation prefix, a
  method reference and a call shape, each passing with the whole suite green; it settled on asking
  `getDeclaredFields` for the field budget and on matching names rather than bodies for the resolvers.
  On #330 successive relocations — bare and qualified assignment, line wraps, a getter read, two
  extract-a-helpers, a qualified receiver — settled on stating the property positively, at class scope,
  instead of forbidding spellings. A differently typed question is not a closed one: #256's reflective
  guard still exempted `static final`, and that run left the `getDeclaredMethod`-with-a-string-literal
  shape open on the record, so name the residue.
- **And the TEXT scoping above leaves out a DATA form of the same attack: a guard that asserts a key is
  PRESENT passes a placeholder under it.** On #340 a reflective guard asserted only `containsKey`, so a
  `put` of `null` beside a new public accessor satisfied it while dropping the value — the change's own
  defect, shipped green under a test that appears to cover it, and closed only by comparing the
  accessor's own reading against the published value. #263 lost two more, to a key the guard did not
  cover and to a swap that preserved the size it checked. So mutate the VALUE under an asserted key, not
  only the key.

### Record the verdict so the gate can enforce it

Emitting the termination gate is a forcing function, and forcing functions are exactly what got skipped. So also write the verdict where something other than you can read it. At the close of **every** Phase 1 pass, and again when Phase 2 finishes:

**And record an `awaiting` entry whenever a cycle delegates, or the gate will not let the cycle
wait for its own agents.** Phase 2 spawns subagents, and one spawned in the background leaves the
cycle nothing to do but yield — and a yield is exactly what the gate refuses, in an unattended run
even with the await recorded, which is why step 1 spawns in the foreground. Measured on the run that
added this: a Phase 2 pass blocked on a background agent tripped the gate on every yield, and the
only way to stay alive was two ten-minute in-turn wait loops, which is pure waste. `pr-harden` solved
this first and its **State** section carries the reasoning; the field and the semantics are the same.
**Stamp `owner` with `$PPID` too.** This state is keyed on the checkout, not the session, so without it
the gate cannot tell your entry from one a co-located run left in the same directory; `pr-harden`'s
**State** section carries that reasoning as well:

```bash
~/.claude/pipeline/gate-state --owner $PPID --run harden-1758600000 await "phase2 quality" --only harden
~/.claude/pipeline/gate-state --owner $PPID --run harden-1758600000 clear-await --only harden
```

Drop `--only harden` and it writes BOTH gates' entries, which is what a `/harden` cycle nested inside
a `resolve-ticket` run needs — see that skill's Step 7.

Record it immediately before spawning and **clear it on ANY terminal outcome** — a result, or the
harness reporting the agent died, stalled or was killed. A stale entry lets the run stop for real,
which is what the gate exists to prevent, so the gate's allow is bounded by an hour; an agent that
has not returned inside it is treated as dead rather than outstanding.

```bash
# ONCE, at the top of the run: pick an id for it — any string unique to this run — and use the same
# LITERAL on every write below. NOT a shell variable: shell state does not survive between tool
# calls, so it would be unset at the point of use, and unquoted an unset one makes `--run` swallow
# the next argument and the write fail — which is the gate's fail-OPEN case, so the contract would
# simply never engage. State the id in your report, so you retype it rather than re-derive it.
# `harden-1758600000` here is an example; use your own.

# after a Phase 1 pass that found something substantive:
~/.claude/pipeline/gate-state --owner $PPID --run harden-1758600000 harden-set --cycle 1 --phase1 open --count-edits
# after the Phase 1 pass that found nothing substantive:
~/.claude/pipeline/gate-state --owner $PPID --run harden-1758600000 harden-set --cycle 1 --phase1 converged --count-edits
# after a Phase 2 that found only polish — this is what ends the run:
~/.claude/pipeline/gate-state --owner $PPID --run harden-1758600000 harden-set --cycle 1 --phase1 converged --phase2 done --count-edits
# after a Phase 2 that ESCALATED — that resumes Phase 1, so it is a Phase 1 write and nothing else:
~/.claude/pipeline/gate-state --owner $PPID --run harden-1758600000 harden-set --cycle 2 --phase1 open --count-edits
```

**`--run` is what makes this checkout's leftovers yours to ignore.** Nothing clears the entry between
interactive runs, and successive reviewers found a leak per pass while the rule was *carry unless
listed* — each through the field or the caller the last fix did not name, and the worst of them an
`awaiting` from a dead run, which allowed a stop on `phase1: open`. No count is kept here; `git log`
has them, and three passes running found the count itself wrong. A write whose `--run`
differs from the entry's — or that carries no id at all — REPLACES it, so the default is *drop
unless this run wrote it*, and what a field added later inherits is nothing rather than everything
absent from a list. The first version of this exempted an entry with no id, on the reading that an
absent id means adoptable; that put the same `awaiting` back through the same door, because every
pre-0.37 entry has no id. It is not `--owner`, which answers whether a live
foreign session holds this checkout — a different question, one a resume changes and a run id does
not, and conflating them is what left the same-pid corner this replaces.

**There is no `--phase2 escalated`, and the first draft's was deleted rather than repaired.** It was
sticky: `--phase1 converged` did not clear it and the hook tested it before `phase1`, so a run whose
Phase 2 escalated and whose next Phase 1 pass then CONVERGED was handed back the instruction it had
just obeyed, every turn, to the six-hour expiry — built by a fresh reviewer, in this skill's own
first run under this contract. An escalation resumes Phase 1, which is `--phase1 open`, which the
gate already blocks on. And **any `--phase1` write with no `--phase2` resets it to `pending`**, so
the Phase 2 owed after that convergence cannot be satisfied by the one that escalated — nor, in a
reused checkout, by a `done` the PREVIOUS run left behind, which was the same stickiness pointing
the other way and would have let a second `/harden` stop with its own Phase 2 never run.

**`--phase1` is what ends the run, and omitting it leaves whatever the entry already says.** On a
fresh entry that is nothing, and the gate reads a run-stamped entry with no verdict as a run in
flight that owes one, so it BLOCKS — a forgotten flag costs you a pass, not the contract. Only an
entry with neither a run id nor a verdict is legacy, and every `gate-state` command that touches the
harden entry — `harden-set`, `await`, `clear-await` — requires `--run` and stamps it, so the legacy
branch serves entries that were already on disk before ids existed. `clear-await` was the last one
without that requirement, and it created a legacy entry out of nothing while refreshing the six-hour
expiry that is the documented way out of a wedge. On an entry that
already carries a verdict, a bare write keeps it, so **do not use one to refresh the count**: say the
verdict every time. Across runs this is handled for you by `--run`, which replaces the whole entry
rather than dropping fields from it — NOT by `--owner`, which answers a different question and whose
drop is gone. `gate-state` says which it did on the line it prints. `--cycle` is a label on the traversal and nothing reads it as a number — advance it on an
escalation if you like, but the gate does not care and the edit measurement tolerates it staying
put, which is why the hook hands back whatever the entry already has.

`--count-edits` is a REPORTED fact now, not the gate's input: uncommitted lines plus the commits made since the previous traversal
of this run closed — both halves, because a cycle that commits its work has still changed something.
That is why the helper records this cycle's `head` on every write. **Do not read the fallback as the
same measurement**: where no recorded head resolves, the commit half becomes `@{u}..HEAD`, which is
everything unpushed on the BRANCH and does not return to zero until the push, so it can read non-zero
on a cycle that changed nothing. Both directions have been paid for — on #255 and #229 an upstream-only
reading scored `edits=0` for cycles carrying 9 and 3 unpushed commits; on #357 the branch had an
upstream and cycle 8 read `edits=16` at convergence. Which one a
run met was decided by whether its branch was cut in a form that sets an upstream (`git checkout -b
<branch> origin/main` does; pulling `main` first and branching off it does not) — a choice
`resolve-ticket` Step 4 leaves open, and neither form is wrong. Neither misreading can end or extend
a run any more, which is one thing keying the gate on Phase 1's verdict buys outright; both still
corrupt the figure you report, so this stays.
Where it cannot measure the commit half — a first cycle with no earlier head, or a recorded head that
stopped resolving after a rebase or a recreated checkout — the helper prints that, instead of counting
it as zero; that line is yours to answer with your own `git log`. It is counted THERE and not here
so that the number recorded and the number you report cannot be two different numbers.

**Why a helper rather than the inline `python3` this used to be.** The read, the change and the write
are one critical section, and they were not: every writer read the whole file, changed its own entry
and wrote the whole file back, with no lock. One session at a time made that merely fragile. Several
make it lossy — and lossy in the direction that kills runs, because the entry that vanishes is
somebody's `awaiting`, and their gate then sees a run that quit with agents outstanding. Measured with
20 concurrent writers to 20 different working trees: the inline form kept **3 of the 20** entries,
valid JSON throughout, nothing raised. `gate-state` holds an exclusive `flock` across both state files
and writes atomically. Do not retype the mechanism; call the helper.

`harden-cycle-gate.sh` ships next to this file and is what reads that entry. On a Stop event it refuses to end the turn while the newest entry for this directory says Phase 1 is `open`, or that Phase 1 has converged and Phase 2 has not run. It fails open on every ambiguity (no file, malformed JSON, no jq, stale entry, an unrecognised `phase1`), so it can only ever cost you the pass you owed. An unrecognised `phase2` is deliberately not in that list: only `done` ends a run, which is decidable without knowing what the value means, and the check used to sit ahead of the `phase1` test so any unknown value disarmed an open verdict. It CAN hold a session, though — an
entry this session owns and never clears blocks every turn in that directory until the six-hour expiry,
and the way out is to finish the phase or take the labelled override, not to wait.

A skill cannot register its own hook, so this is a one-time install per machine:

```bash
mkdir -p ~/.claude/hooks && cp .claude/skills/harden/harden-cycle-gate.sh ~/.claude/hooks/
# then add to ~/.claude/settings.json (merge — do not replace an existing hooks block):
#   "hooks": { "Stop": [ { "hooks": [
#     { "type": "command", "command": "$HOME/.claude/hooks/harden-cycle-gate.sh", "timeout": 10 }
#   ] } ] }
```

Until it is installed the gate is prose only, which is exactly the state that let the rule get skipped — so write the entry regardless, and treat an uninstalled gate as a reason to be stricter with yourself rather than looser.

Two consequences worth internalising: **the state file lives under `$HOME`, never in the repo**, because an in-repo file would itself show up in `git status --porcelain` and corrupt the measurement it exists to record. And **when you finish** — converged, or overridden — the entry must say so (`phase1: converged` with `phase2: done`, or `override: true`); leaving a stale entry that still owes a phase behind is what the 6-hour expiry is there to clean up after you.

If the user has also set a goal to the same effect, it is enforcing this rule from outside too; nothing changes about how you run.

## Reporting

After stopping, summarize:
- Phase 1 passes run, and whether Phase 2 ran once or escalated.
- **Phase 1's verdict on its last pass, and the traversal's edit count as measured** — the termination gate from Termination, filled in. The report is not complete without either it or the override line, and neither may be replaced by a question to the user. The edit count is reported, not a claim of completeness: what a run of this skill licenses is "the review passes stopped finding substantive issues", which is why the fresh-context loop reviews the result afterwards.
- What was changed (one bullet per real fix, separated by phase), and which pass each fix landed in; that is what shows the run converged rather than ran out of patience.
- **For every deferred item, a concrete failure-mode sentence in the form "if we ship without this, X breaks because Y."** A deferral without that sentence is not a deferral — it is an unanalyzed item. Re-read and either apply or write the sentence. Group sentences by item; do not collapse multiple deferrals into a single label like "remaining items below noise floor."
- Current build / test status.
- Recommended next action (commit + push, or move on).

## Anti-patterns

- **Don't invent concerns** to justify another pass — diminishing returns are real signals.
- **Don't re-litigate** decisions from prior passes (e.g., "we deferred test fixture unification — should we revisit?" — no, ship).
- **Don't run another pass** if the only items are below the noise floor or the agents start agreeing on "nothing actionable." That is Phase 1's convergence, and reaching it is the point; it is not licence to skip the Phase 2 pass that Termination requires after it.
- **Don't end with Phase 1 open.** "It's basically converged, and the last finding was small" is the single most common way this skill stops early. A pass that found something substantive was not the last pass, whatever its edit count — see Termination.
- **Don't pause for user input between passes** unless something is genuinely ambiguous. The skill is meant to converge autonomously up to the stopping rules.
- **Don't hand the termination decision back to the user.** This is the disguised form of stopping early, and it is harder to catch than the honest form because it reads as deference. "You should get to decide whether to spend another cycle", "want me to keep going?", "say the word and I'll run cycle N+1" — all of these end the run with Phase 1 open while looking like good practice. Note what a naive check misses: reporting *truthfully* that the run has not converged and then handing back is still a violation, so a detector aimed at false convergence claims will not see it. The tell is the handback, not the claim. If the rule requires another cycle, run it; if you are not going to, use the labelled override in Termination, which states plainly that you overrode a rule rather than asking permission you already had instructions about.
- **Don't rewrite prose faster than you verify it.** When a cycle's findings are all in text *you wrote in the previous cycle* — a comment, a failure message, a doc paragraph — stop rewriting and change tactics: delete the unsupported clause instead of replacing it with a better-sounding one. Replacing an unverified causal claim with a different unverified causal claim reads as progress and buys another mandatory Phase 1 pass, because a now-false comment is a Phase 1 finding; several passes in a row of this is the signature. Prefer stating only the mechanism you can check, naming candidates without ranking them, and saying outright that the evidence does not distinguish them. "I could not establish which" is a finished sentence. **And when deleting the clause is itself what
  keeps buying cycles, delete the CLAIM SHAPE.** On #330 four rules for one count were written in
  succession and each measured false, across ten cycles; what ended it was publishing the measurement
  with its arrangement and no rule about it. Once a second attempt at a claim of some kind has been
  refuted, the move is to stop making a claim of that kind, not to make a better one.
- **Don't publish a claim a later cycle must re-measure.** **And the rule is not about tallies — it is about claims you cannot check.** A universal or an exhaustive characterization is the same defect in different grammar, and it slips past a reader watching for digits: *any*, *only*, *exactly*, *all*, *never*, *the whole*, *cannot*. Measured on the seventh run, five such claims in three consecutive cycles, each written to correct the previous cycle's false claim and each false in turn — "any looser pattern would reject" (looseness has more than one dimension), "it only re-admits `M01AE0`" (it re-admits any single trailing digit), "matched only the 5- and 7-character shapes" (the old pattern matched 6 too), "exactly the two levels the ladder is known to be handed" (nothing on the path validates a code's shape), and one that mis-numbered the very level it was excluding. So before writing one about code you just wrote, spend one attempt trying to falsify it; prefer stating what the thing DOES over what it excludes; and name the residue rather than claiming there is none. A count is the obvious case — prefer *"mutate the line and read the failures"* to any tally — but the universals are the ones that survive review, because nothing about them looks like a measurement.
- **Don't trust a script's report that it edited something.** Every one of these passes edits by running a short script, and `str.replace` returns the string unchanged when it matches nothing while the script prints success anyway. One false claim survived FIVE cycles of this skill that way. Assert the target text is present before replacing; after a multi-line replacement, count what should still be there (a slice bounded by "this javadoc to the next method" once deleted a whole test method, and it compiled); and verify by reading the file back rather than by believing the script. **That clause is about a DELETION; an INSERTION has its own silent failure — it separates a javadoc from the member it documents.** After inserting a member into an existing file, read the neighbours of the insertion point and check that each doc comment still sits against the member it names. Three runs paid a review pass each for one (#234, #229, #255 — silent through compile, checkstyle and 1686 tests; on #255 three agents reported it independently). **And the diff already knows where to look, so stop relying on noticing:**

  ```sh
  git diff -U1 <base>..HEAD -- '*.java' | awk '/^\+\+\+ /{f=substr($0,7); p=""; next} /^ /{p=$0; next} /^\+/{if (p ~ /\*\/[ \t]*$/) print f; p=""; next} {p=""}'
  ```

  It names every file where an insertion landed directly below a doc comment's `*/`, which is the shape of every instance above. Give it a range: the bare form reads the working tree, which *Commit before anything mutates the tree* has already emptied. A hit is a place to READ and not a finding — it fires on the commit that REPAIRS an orphan as readily as on the one that creates it. Measured 2026-09-14 on this project: it names the file on each of the two commits whose successors are titled as fixing an orphan, is silent on one of those fixes, and over 40 commits of `main` printed two lines — one of them a javadoc orphaned in `ModuleSourceRoot` that a full `/harden` and a `pr-harden` round had shipped. The remaining sweep is the one the check cannot do: read the doc comment it names and ask whether it still describes what now follows it. Another silent failure of the same family — a script that batches several edits behind one write, and reports the ones an abort never made — is in `pr-harden`'s *Editing by script*, with its two rounds.
- **Don't stop correcting a claim at the site you noticed it.** A false statement is rarely in one place. Measured on one run of this skill: a correction reached one of seven homes, then five of six, then five of six again, and once the two halves of a single paragraph contradicted each other after one half was fixed. Search for the claim's rarest single TOKEN, over the whole tree rather than over the docs, fix every hit, then grep for the phrasing you just wrote to see where it now lives. Searching the PHRASING is what leaves the last home standing, and it fails two different ways: a phrase the file's own formatting has split — markdown emphasis inside it, a line break falling between a quantifier and its noun — does not match what you typed, while a home that is a DATA file rather than a doc is missed by scope alone. Both were paid for on the #266 run, a cycle per survivor, each hidden by a mechanism the one before it had not used; treat no list of those mechanisms as closed. **And a home that names the claim's key nowhere is reachable by no token search at all.** On #374 six cycles of sampling turned up eighteen homes of one claim and one of them was a site like that. So when a sweep keeps returning one more home, do not add a mechanism to the list; change the kind of question, as *What ends the loop is a change in the KIND of question, not another entry on the list* requires, and enumerate the claim's SUBJECT instead — every sentence in the tree about the thing the claim is about, which is a population you can finish. Two homes are easy to miss — the project's own instruction file, and anything outside the repo (a PR description, an issue comment) that no grep will reach. And a positional cross-reference ("the bullet above") is a claim about layout that any insertion falsifies: name the target instead of locating it. **And where the claim already has a home, correct that home and point the others at it rather than maintaining N corrected copies** — on #315 a measurement had reached five files and been corrected twice before it was consolidated into one, everything else pointing there. A pointer is not free: #412 paid two passes for two dangling ones, the second introduced by the fix for the first, so a pointer must name its target and not locate it; and an instruction file already at its byte budget cannot take one at all.
- **Don't promote architectural concerns** into in-pass fixes. Items like "this Hibernate proxy hits the DB at backfill scale" are real but belong in the indexer/sync layer, not in the slice being polished — flag and defer.
- **Don't review the slice in isolation.** Integration bugs hide outside the file diff — at trigger boundaries (a sibling service mutates state without notifying you), classloader boundaries (an optional dep's absence breaks static class resolution), and lifecycle boundaries (a consumer scans before you register). Every Phase 1 pass MUST trace at least one level out on each integration thread (trigger paths, optional deps, lifecycle order, state propagation, invalidated invariants in unchanged neighbors, and re-deriving the merged result from scratch). The slice's correctness contract spans its boundaries — a fix that lives in a sibling service, or in an unchanged neighbor your edit falsified, is still a Phase 1 finding when the slice surfaces or depends on the bug. See "Trace outward" in Phase 1.
- **Don't batch-defer "Minor" items by severity label.** Severity labels are an agent's guess, not a verdict. Before deferring any finding, write the concrete failure mode out loud: "if we ship without this, X breaks because Y." If you can't complete that sentence, you don't yet understand the severity — re-read the finding, trace its consequence, and either apply the fix or write down what you'd need to know to defer it. This rule is load-bearing: agents routinely under-label correctness fixes as Minor (e.g. unclosed `AutoCloseable`s, leaked test state) because the code-pattern looks small.

  **Sub-rules to keep the failure-mode sentence honest:**

  - **Anti-tell phrases.** These are smoke that hides the failure-mode question. If you reach for one, stop and write the failure mode instead — none of these are failure modes:
    - "matches the existing pattern" / "matches the ADR's example" — illustrative code is not a constraint; the agent's specific recommendation for your slice overrides general convention.
    - "below noise floor" / "sub-noise-floor" — a label, not a consequence.
    - "stylistic preference" / "debatable style" — restate as a failure mode and recheck.
    - "borderline" — pick a side and write the sentence for that side.
    - "low risk" without naming the risk — name what could go wrong, who would notice, and how.
    - "environment-blocked" / "can't unit-test this" / "proven in production" / "mirrors a shipped pattern" — split the genuinely-blocked input from the runnable path (you can usually still execute the new code on valid input), and remember analogy verifies the sibling, not the variant you added. See the Blocked-path exception in Phase 1.

  - **Silent-failure upgrade.** When the failure mode is "the system produces wrong output without throwing," upgrade the severity one level. Silent corruption is harder to detect than a crash, and the cost to discover it is paid by users, not CI. A typo'd metadata key, a dropped field, a stale denormalized value — these don't crash; they leak.

  - **Conflation check.** If you're rejecting a finding because of scope inflation ("extracting 30+ constants would be too much"), re-read the agent's exact wording. Are you rejecting the agent's recommendation, or an inflated version you constructed? Agents often recommend a *targeted* fix; rejecting the *maximalist* fix is rejecting a strawman. The agent's narrow scope is the deferral candidate, not your expansion of it.

  - **Conditional-recommendation check.** If the agent's recommendation contains a conditional ("skip — but if X, reconsider"), evaluate the conditional explicitly in the report. Don't treat a conditional skip as a flat skip. If you don't know whether the conditional applies, find out before deferring.

## When NOT to use this skill

- For a brand-new slice that hasn't been reviewed once. Run /review first; promote to /harden only if the slice would benefit from iterative polishing (typical for code that's structurally non-trivial — serializers, parsers, multi-step pipelines).
- When the user wants a single-pass sanity check. Use /review or /simplify for that.
- For changes the user flagged as exploratory or about-to-be-reverted.
