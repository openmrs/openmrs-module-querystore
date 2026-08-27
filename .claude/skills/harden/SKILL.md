---
name: harden
description: Run iterative /review and /simplify passes on the current slice in two phases, cycling until a whole cycle changes nothing. Use when the user wants to harden a code slice end-to-end without manually orchestrating the review/simplify dance. Trigger phrases include "harden this", "polish until done", "iterate until convergence", "harden".
version: 0.19.2
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

Run simplify passes until polish opportunities converge. Each pass:

1. Spawn four parallel review agents (reuse, quality, efficiency, integration) over the current diff. After the first pass, brief subsequent passes' agents with the applied and deferred lists from prior passes so they don't re-surface them.
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
     brief and have the agent confirm its diff is non-empty before it reviews anything. The cause
     differs between runs and neither is worth diagnosing here: on the #269 run every agent's worktree
     "opened at origin/main, not the PR branch, so all six had to check the branch out themselves",
     and two reported the diff they were asked for came back empty until they did; on the #250 run the
     branch existed but git refused it to a second worktree because the main one held it, so agents
     needed `git checkout --ignore-other-worktrees`. Both cost a detour per agent and no round or
     cycle, which is why this is one sentence in the brief rather than a step.
   - **Commit before anything mutates the tree — the cycle's work before spawning them, and your own
     measurement probes too — and do not edit the tree while they run.** `git checkout -- <path>` to
     undo a probe restores HEAD, so on a file carrying uncommitted intended work it discards that work
     as well: measured on the #302 run, four production edits vanished that way and the empty
     `git diff --stat` afterwards read as "restored" rather than "reverted". These agents are told to mutate-and-restore for evidence, and a restore comes from what the agent READ — so an edit that lands after it read and before it restores is silently reverted. Measured: a quality agent mutated a guard to test whether a case could discriminate it, restored the file from its remembered copy, and reverted a fix applied in the meantime. It compiled and the suite passed; it surfaced only when a later test failed inexplicably. A commit is the one thing a remembered restore cannot undo. Tell them to restore with `git checkout -- <path>`, never by rewriting remembered content.
   - **integration** is not intrinsic polish. It asks: does the slice degrade gracefully when neighbors are missing, misordered, or silent? Does state propagate correctly across module/service boundaries? Does the slice's runtime contract hold when an upstream service violates an implicit assumption (e.g., mutates shared state without firing the expected event)? It revisits the Phase 1 "Trace outward" threads with a polish lens — looking for the gaps Phase 1 might have missed because the failure mode was framed as "fine in the happy path."
   - In every agent's brief, require them to trace at least one level out from the slice (callers, callees, lifecycle, optional deps) before declaring "nothing new." Reviews scoped to the file diff alone miss the bugs that live at boundaries.
2. Aggregate findings across the four agents.
3. Apply genuinely actionable items; skip stylistic noise and items prior passes addressed.
4. Verify with the build.
5. Decide: another simplify pass, or stop?

**Stop Phase 2 when ANY are true:**
- Two consecutive passes return "nothing actionable" or only sub-noise-floor stylistic items.
- Agents start re-flagging items prior passes addressed (context-drift signal).
- All remaining findings are below the noise floor: micro-optimizations, naming preferences, debatable style.

**Stopping gate** (say this out loud in the final report before declaring `/harden` done):

> "Phase 2 stopping condition met: [last two consecutive passes returned nothing actionable / only sub-noise-floor items | agents re-flagging prior items | all remaining findings below the noise floor]. Edits made by that last pass: [N]."

If you cannot truthfully complete that sentence, run another Phase 2 pass. Same rule as Phase 1, and state it the same way: **a pass that itself changed something cannot be the last pass**, because applying a fix is evidence the slice was not fully explored — convergence requires a *subsequent* pass that changes nothing. Pass count is not the threshold; convergence is.

## Termination: keep cycling until a whole cycle changes nothing

The two gates above end a *phase*. They do not end the run, and this is the rule the skill most often loses. Both gates are finding-based — "no further review value", "nothing actionable" — so the obvious reading is: fix what this pass found, declare the pass returned nothing further, stop. That ends the invocation **with changes in it**, and the next `/harden` then finds more, which is how a slice gets re-hardened five times and yields something real every time.

So the run has its own condition, and it is a fact rather than a judgment:

> **`/harden` is complete when one cycle produces zero edits.** If the cycle changed anything — a line of code, a comment, a test, a doc — that cycle was not the last one. Start another. Do not hand back to the user in between. That next cycle is a full Phase 1 + Phase 2 unless the classification below makes it a documentation pass, which is a cheaper cycle and not a skipped one.

Check it, do not estimate it. At the end of a cycle run `git status --porcelain` and count the commits the cycle made; report both. "I think it has converged" is not the condition; "this cycle made 0 edits" is.

**Classify the cycle before deciding what the next one costs.** The confirming cycle is owed either
way; what can change is its price. Where a cycle's only edits are DOCUMENTATION, it may be confirmed
by a single agent rather than a full Phase 1 + Phase 2 — provided that agent does three things: the
correction method in *Don't stop correcting a claim at the site you noticed it*, in full; the build;
and, for any claim about behaviour, RUNS it rather than reads it.

That third condition is what makes this a cheaper cycle rather than a weaker one, and it is not
optional. A documentation-only diff can carry a behavioural falsehood: on the #302 run the claim that
`clauseScoped=true` supersedes that issue's own treatment was written into `config.xml` and the
README, and only driving the real `verify` refuted it — the flag REMOVES the rule and reinstates the
symptom. A coherence sweep would have found that sentence coherent, and false.

**What counts as documentation is a fact about the diff, and narrower than it reads.** One production
line, one changed assertion, one new arrangement, and it is a full cycle. And where prose IS
behaviour, the file it lives in does not save it: a prompt paragraph a test asserts a substring of, a
log or failure message under assertion, a global-property default or the description shipped beside
it — none of those are documentation for this purpose.

A documentation pass that turns up anything behavioural escalates on the spot, to a full cycle
starting at Phase 1. And the gate is unmoved: the pass still has to reach `edits: 0`, and the run
still ends only on a cycle that changed nothing.

**Cycle gate** (forcing function — the two phase gates demand verbatim sentences and get them; this one was prose and got skipped, so it is now the same shape). At the close of **every** cycle, before anything else, say this out loud with the measured values filled in:

> "Cycle N edit count, measured: `git status --porcelain` = [empty | N files], commits this cycle = N. Cycle N+1 is therefore [required | not required]."

Emit it even when the answer is obvious, and especially when the cycle's last pass reported "nothing actionable" — that is a *phase* verdict and says nothing about whether the cycle edited a file. If you cannot fill in both measured values, you have not run the check.

**If you are going to stop early anyway, label it.** Cost, elapsed time and turn length are **not** termination conditions and appear nowhere above; the rule has no cost exception. But a rule with no escape valve gets broken rather than invoked, so if you judge the remaining cycles not worth their cost, take the valve and make the deviation legible:

> "I am overriding the termination rule after cycle N, because [reason]. This run did **not** converge; cycle N+1 was required and I did not run it."

That is a permitted move. What is not permitted is ending the run without either the convergence line or the override line. And do not launder the override into a question — see the anti-pattern on handing the decision back.

This is deliberately cheap to satisfy and expensive to fake, which is the point — but it cuts both ways, so:

- **Do not manufacture a change to look thorough.** An empty cycle is the goal, not a failure. If a cycle finds nothing, say so and stop; padding it with a comment tweak just buys another mandatory cycle.
- **Do not withhold a warranted change to end sooner.** If you find something real on what you hoped was the final cycle, fix it and run another. The rule exists precisely to stop "it's basically converged" from ending a run that still had a finding in it.
- Every applied change still needs its evidence: verified by build or test, and where it fixes a behavior, checked by reverting it and confirming the failure. **Where it ADDS a guard or clause, the same check is owed on that — deleted, its arms swapped, its comparison loosened, or rewritten in a semantically equivalent way** — because a clause the suite never discriminates is one the next change can remove for free.
- **And where the guard is over TEXT, mutate the SUBJECT too — the four mutations above are all of the
  guard, and none of them moves the thing it forbids.** Relocate that string: into a comment, across the
  file's own line-wrap, behind a block comment, into a sibling declaration outside the slice the guard
  reads — and check it still reddens. On the #315 run one guard was defeated five ways in turn, each
  found by the next fresh agent at a cycle or a round apiece and each fix opening the next, so treat no
  list of relocations as closed. Two the run paid for: bound the window at the construct it is about
  rather than at a line count, and have both halves of a two-sided guard read text normalised the same
  way.

### Record the cycle so the gate can enforce it

Emitting the cycle gate is a forcing function, and forcing functions are exactly what got skipped. So also write the count where something other than you can read it. At the close of **every** cycle, alongside the gate sentence:

**And record an `awaiting` entry whenever a cycle delegates, or the gate will not let the cycle
wait for its own agents.** Phase 2 spawns subagents, so a cycle is routinely blocked on one with
nothing to do but yield — and a yield is exactly what the gate refuses. Measured on the run that
added this: a Phase 2 pass blocked on a background agent tripped the gate on every yield, and the
only way to stay alive was two ten-minute in-turn wait loops, which is pure waste. `pr-harden` solved
this first and its **State** section carries the reasoning; the field and the semantics are the same.
**Stamp `owner` with `$PPID` too.** This state is keyed on the checkout, not the session, so without it
the gate cannot tell your entry from one a co-located run left in the same directory; `pr-harden`'s
**State** section carries that reasoning as well:

```bash
# usage:  awaiting.py await "phase2 quality"   |   awaiting.py clear
import json, os, sys, time, pathlib
p = pathlib.Path.home()/".claude/harden-state.json"
s = json.loads(p.read_text()); e = s[os.getcwd()]
if sys.argv[1] == "await":
    e.setdefault("awaiting", []).append({"agent": sys.argv[2], "since": int(time.time())})
else:
    e["awaiting"] = []
e["ts"] = int(time.time()); p.write_text(json.dumps(s, indent=2))
```

Record it immediately before spawning and **clear it on ANY terminal outcome** — a result, or the
harness reporting the agent died, stalled or was killed. A stale entry lets the run stop for real,
which is what the gate exists to prevent, so the gate's allow is bounded by an hour; an agent that
has not returned inside it is treated as dead rather than outstanding.

```bash
python3 - "$PPID" <<'PY'
import json, os, subprocess, sys, time, pathlib
CYCLE, OVERRIDE = 4, False          # <- this cycle's number; True only with the labelled override
OWNER = int(sys.argv[1])            # $PPID from a tool shell IS this session's claude process
d = subprocess.run(["git","status","--porcelain"], capture_output=True, text=True).stdout.strip()
n = len(d.splitlines()) + int(subprocess.run(
    ["git","rev-list","--count","@{u}..HEAD"], capture_output=True, text=True).stdout.strip() or 0)
p = pathlib.Path.home()/".claude/harden-state.json"
s = json.loads(p.read_text()) if p.exists() else {}
e = s.get(os.getcwd(), {})
e.update({"cycle": CYCLE, "edits": n, "ts": int(time.time()), "override": OVERRIDE,
           "owner": OWNER})
e.setdefault("awaiting", [])        # written by the await one-liner above; never clobbered here
s[os.getcwd()] = e
p.parent.mkdir(parents=True, exist_ok=True); p.write_text(json.dumps(s, indent=2))
print(f"cycle {CYCLE}: {n} edit(s) recorded")
PY
```

`harden-cycle-gate.sh` ships next to this file and is what reads that entry. On a Stop event it refuses to end the turn while the newest entry for this directory says `edits > 0`. It fails open on every ambiguity (no file, malformed JSON, no jq, stale entry, non-numeric count), so it can only ever add a cycle you owed — it cannot wedge a session.

A skill cannot register its own hook, so this is a one-time install per machine:

```bash
mkdir -p ~/.claude/hooks && cp .claude/skills/harden/harden-cycle-gate.sh ~/.claude/hooks/
# then add to ~/.claude/settings.json (merge — do not replace an existing hooks block):
#   "hooks": { "Stop": [ { "hooks": [
#     { "type": "command", "command": "$HOME/.claude/hooks/harden-cycle-gate.sh", "timeout": 10 }
#   ] } ] }
```

Until it is installed the gate is prose only, which is exactly the state that let the rule get skipped — so write the entry regardless, and treat an uninstalled gate as a reason to be stricter with yourself rather than looser.

Two consequences worth internalising: **the state file lives under `$HOME`, never in the repo**, because an in-repo file would itself show up in `git status --porcelain` and corrupt the measurement it exists to record. And **when you finish** — converged, or overridden — the entry must say so (`edits: 0`, or `override: true`); leaving a stale `edits > 0` behind is what the 6-hour expiry is there to clean up after you.

If the user has also set a goal to the same effect, it is enforcing this rule from outside too; nothing changes about how you run.

## Re-entry

If Phase 2 surfaces a *structural* concern (not polish — e.g., a real correctness bug, a missing test for a critical path, a leaky abstraction), return to Phase 1 for one targeted pass before resuming Phase 2. Don't bounce back and forth more than once.

## Reporting

After stopping, summarize:
- Total cycles, and passes per phase within them.
- **The terminating cycle's edit count, as measured** — `git status --porcelain` clean and 0 commits — so the reader can see the run ended on an empty cycle rather than on a judgment that it had converged. This is the cycle gate from Termination; the report is not complete without either it or the override line, and neither may be replaced by a question to the user.
- What was changed across them (one bullet per real fix, separated by phase). If earlier cycles made changes and the last did not, say which cycle each fix landed in; that is what shows the run converged rather than ran out of patience.
- **For every deferred item, a concrete failure-mode sentence in the form "if we ship without this, X breaks because Y."** A deferral without that sentence is not a deferral — it is an unanalyzed item. Re-read and either apply or write the sentence. Group sentences by item; do not collapse multiple deferrals into a single label like "remaining items below noise floor."
- Current build / test status.
- Recommended next action (commit + push, or move on).

## Anti-patterns

- **Don't invent concerns** to justify another pass — diminishing returns are real signals.
- **Don't re-litigate** decisions from prior passes (e.g., "we deferred test fixture unification — should we revisit?" — no, ship).
- **Don't run another pass** if the only items are below the noise floor or the agents start agreeing on "nothing actionable." This governs passes *within* a phase; it is not licence to skip the confirming cycle that Termination requires after a cycle that changed something. That cycle is expected to be empty — running it is how you prove it, and a documentation pass, where the classification allows one, still is it.
- **Don't end a cycle that changed something.** "It's basically converged, and the last fix was small" is the single most common way this skill stops early, because both phase gates are about findings and neither asks whether you just edited a file. If the cycle made an edit, it was not the last cycle — see Termination.
- **Don't pause for user input between passes** unless something is genuinely ambiguous. The skill is meant to converge autonomously up to the stopping rules — including across cycles, not just across passes within a cycle.
- **Don't hand the termination decision back to the user.** This is the disguised form of stopping early, and it is harder to catch than the honest form because it reads as deference. "You should get to decide whether to spend another cycle", "want me to keep going?", "say the word and I'll run cycle N+1" — all of these end the run with edits in it while looking like good practice. Note what a naive check misses: reporting *truthfully* that the run has not converged and then handing back is still a violation, so a detector aimed at false convergence claims will not see it. The tell is the handback, not the claim. If the rule requires another cycle, run it; if you are not going to, use the labelled override in Termination, which states plainly that you overrode a rule rather than asking permission you already had instructions about.
- **Don't rewrite prose faster than you verify it.** When a cycle's findings are all in text *you wrote in the previous cycle* — a comment, a failure message, a doc paragraph — stop rewriting and change tactics: delete the unsupported clause instead of replacing it with a better-sounding one. Replacing an unverified causal claim with a different unverified causal claim reads as progress and buys another mandatory cycle; several cycles in a row of this is the signature. Prefer stating only the mechanism you can check, naming candidates without ranking them, and saying outright that the evidence does not distinguish them. "I could not establish which" is a finished sentence.
- **Don't publish a claim a later cycle must re-measure.** **And the rule is not about tallies — it is about claims you cannot check.** A universal or an exhaustive characterization is the same defect in different grammar, and it slips past a reader watching for digits: *any*, *only*, *exactly*, *all*, *never*, *the whole*, *cannot*. Measured on the seventh run, five such claims in three consecutive cycles, each written to correct the previous cycle's false claim and each false in turn — "any looser pattern would reject" (looseness has more than one dimension), "it only re-admits `M01AE0`" (it re-admits any single trailing digit), "matched only the 5- and 7-character shapes" (the old pattern matched 6 too), "exactly the two levels the ladder is known to be handed" (nothing on the path validates a code's shape), and one that mis-numbered the very level it was excluding. So before writing one about code you just wrote, spend one attempt trying to falsify it; prefer stating what the thing DOES over what it excludes; and name the residue rather than claiming there is none. A count is the obvious case — prefer *"mutate the line and read the failures"* to any tally — but the universals are the ones that survive review, because nothing about them looks like a measurement.
- **Don't trust a script's report that it edited something.** Every one of these passes edits by running a short script, and `str.replace` returns the string unchanged when it matches nothing while the script prints success anyway. One false claim survived FIVE cycles of this skill that way. Assert the target text is present before replacing; after a multi-line replacement, count what should still be there (a slice bounded by "this javadoc to the next method" once deleted a whole test method, and it compiled); and verify by reading the file back rather than by believing the script.
- **Don't stop correcting a claim at the site you noticed it.** A false statement is rarely in one place. Measured on one run of this skill: a correction reached one of seven homes, then five of six, then five of six again, and once the two halves of a single paragraph contradicted each other after one half was fixed. Grep for the claim's distinctive phrasing, fix every hit, then grep for the phrasing you just wrote to see where it now lives. Two homes are easy to miss — the project's own instruction file, and anything outside the repo (a PR description, an issue comment) that no grep will reach. And a positional cross-reference ("the bullet above") is a claim about layout that any insertion falsifies: name the target instead of locating it.
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
