---
name: harden
description: Run iterative /review and /simplify passes on the current slice in two phases until both converge. Use when the user wants to harden a code slice end-to-end without manually orchestrating the review/simplify dance. Trigger phrases include "harden this", "polish until done", "iterate until convergence", "harden".
version: 0.1.0
---

# Harden

Iteratively review and polish the current slice in two phases. Phase order matters: review catches structural issues (correctness, missing tests, design concerns); simplify catches polish (duplication, naming, micro-efficiency). Doing simplify on structurally-incomplete code wastes work — review-first surfaces the real fixes before polish happens.

## Phase 1: Structural review (/review style)

Run review passes until structural concerns converge. Each pass:

1. One comprehensive review covering correctness, conventions, performance, tests, security.
2. Apply genuinely actionable findings.
3. Verify with the build (`mvn -pl api install` for Maven, project-equivalent otherwise).
4. Decide: another review pass, or move to Phase 2?

**Stop Phase 1 when ANY are true:**
- The verdict is "ready to commit" / "no further review value."
- Two consecutive passes return only cosmetic items (e.g., test assertion tightening, import ordering).
- The pass starts re-flagging items prior passes addressed.

## Phase 2: Polish (/simplify style)

Run simplify passes until polish opportunities converge. Each pass:

1. Spawn three parallel review agents (reuse, quality, efficiency) over the current diff.
2. Aggregate findings across the three agents.
3. Apply genuinely actionable items; skip stylistic noise and items prior passes addressed.
4. Verify with the build.
5. Decide: another simplify pass, or stop?

**Stop Phase 2 when ANY are true:**
- Two consecutive passes return "nothing actionable" or only sub-noise-floor stylistic items.
- Agents start re-flagging items prior passes addressed (context-drift signal).
- All remaining findings are below the noise floor: micro-optimizations, naming preferences, debatable style.

## Re-entry

If Phase 2 surfaces a *structural* concern (not polish — e.g., a real correctness bug, a missing test for a critical path, a leaky abstraction), return to Phase 1 for one targeted pass before resuming Phase 2. Don't bounce back and forth more than once.

## Reporting

After stopping, summarize:
- Total passes per phase.
- What was changed across them (one bullet per real fix, separated by phase).
- Why any remaining flagged items were skipped.
- Current build / test status.
- Recommended next action (commit + push, or move on).

## Anti-patterns

- **Don't invent concerns** to justify another pass — diminishing returns are real signals.
- **Don't re-litigate** decisions from prior passes (e.g., "we deferred test fixture unification — should we revisit?" — no, ship).
- **Don't run another pass** if the only items are below the noise floor or the agents start agreeing on "nothing actionable."
- **Don't pause for user input between passes** unless something is genuinely ambiguous. The skill is meant to converge autonomously up to the stopping rules.
- **Don't promote architectural concerns** into in-pass fixes. Items like "this Hibernate proxy hits the DB at backfill scale" are real but belong in the indexer/sync layer, not in the slice being polished — flag and defer.

## When NOT to use this skill

- For a brand-new slice that hasn't been reviewed once. Run /review first; promote to /harden only if the slice would benefit from iterative polishing (typical for code that's structurally non-trivial — serializers, parsers, multi-step pipelines).
- When the user wants a single-pass sanity check. Use /review or /simplify for that.
- For changes the user flagged as exploratory or about-to-be-reverted.
