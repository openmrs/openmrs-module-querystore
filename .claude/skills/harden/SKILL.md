---
name: harden
description: Run iterative /review and /simplify passes on the current slice in two phases until both converge. Use when the user wants to harden a code slice end-to-end without manually orchestrating the review/simplify dance. Trigger phrases include "harden this", "polish until done", "iterate until convergence", "harden".
version: 0.7.0
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

A behavior change without a named test is a Phase 1 finding — even when the code looks "obviously correct," "matches an existing pattern," or "is trivially small." Never-executed code is unverified code.

**Blocked-path exception (compile, infrastructure, OR un-fabricatable input).** When you claim part of the slice "can't be tested" — won't compile yet, needs a real DB, or the triggering input can't be constructed (a dangling FK an FK-enforcing test DB rejects) — split the claim: name the sub-path genuinely blocked AND the adjacent one that is NOT. You usually can't fabricate the error/orphan input, but you can still execute the new code on *valid* input and assert it runs. "Can't reproduce the failure case" is not "can't run the new code at all." Sketch the test contract in writing for whatever stays genuinely blocked.

**Stop Phase 1 when ANY are true:**
- The verdict is "ready to commit" / "no further review value."
- Two consecutive passes return only cosmetic items (e.g., test assertion tightening, import ordering).
- The pass starts re-flagging items prior passes addressed.

**Transition gate** (forcing function — say BOTH out loud in the report before moving to Phase 2):

> "Phase 1 stopping condition met: [last pass returned no further review value | last two consecutive passes returned only cosmetic items | last pass started re-flagging prior items]."

> "Integration questions answered: what happens to this slice when {an upstream service mutates without notifying me / an optional dependency is absent at runtime / a consumer scans before I register / a sibling service silently changes shared state / an unchanged neighbor's documented invariant is now falsified by my change / two separately-added mechanisms compose into a race or contradiction}? Answer: [concrete behaviors observed or verified, one line each]."

> "Behavior changes without tests: [enumerated — for each, state 'test added: <name>' or 'compile-blocked, test contract sketched: <description>'] or 'none.'"

If you cannot truthfully complete ALL THREE sentences, the slice is NOT ready for Phase 2 — run another Phase 1 pass. Two passes both finding substantive issues is a signal to keep going, not stop. **Conversely, a pass that itself found a substantive (non-cosmetic) issue cannot be the last pass: finding a real gap is evidence the slice was not fully explored, so convergence requires a *subsequent* pass that finds nothing substantive.** Pass count is not the threshold; convergence is.

## Phase 2: Polish (/simplify style)

Run simplify passes until polish opportunities converge. Each pass:

1. Spawn four parallel review agents (reuse, quality, efficiency, integration) over the current diff. After the first pass, brief subsequent passes' agents with the applied and deferred lists from prior passes so they don't re-surface them.
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

> "Phase 2 stopping condition met: [last two consecutive passes returned nothing actionable / only sub-noise-floor items | agents re-flagging prior items | all remaining findings below the noise floor]."

If you cannot truthfully complete that sentence, run another Phase 2 pass. Same rule as Phase 1: pass count is not the threshold; convergence is.

## Re-entry

If Phase 2 surfaces a *structural* concern (not polish — e.g., a real correctness bug, a missing test for a critical path, a leaky abstraction), return to Phase 1 for one targeted pass before resuming Phase 2. Don't bounce back and forth more than once.

## Reporting

After stopping, summarize:
- Total passes per phase.
- What was changed across them (one bullet per real fix, separated by phase).
- **For every deferred item, a concrete failure-mode sentence in the form "if we ship without this, X breaks because Y."** A deferral without that sentence is not a deferral — it is an unanalyzed item. Re-read and either apply or write the sentence. Group sentences by item; do not collapse multiple deferrals into a single label like "remaining items below noise floor."
- Current build / test status.
- Recommended next action (commit + push, or move on).

## Anti-patterns

- **Don't invent concerns** to justify another pass — diminishing returns are real signals.
- **Don't re-litigate** decisions from prior passes (e.g., "we deferred test fixture unification — should we revisit?" — no, ship).
- **Don't run another pass** if the only items are below the noise floor or the agents start agreeing on "nothing actionable."
- **Don't pause for user input between passes** unless something is genuinely ambiguous. The skill is meant to converge autonomously up to the stopping rules.
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
