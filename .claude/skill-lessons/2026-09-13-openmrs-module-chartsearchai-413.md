# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #413 / PR 419 · 2026-09-13
outcome: converged
rounds: 3   cycles: 1 (harden, overridden)   verifier: ran twice (works at runtime, works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-413/2ed04c6b-f25a-47cd-9dd9-4237c125f3f9.jsonl

## Refuted by measurement
- "Guarding the line the ticket names closes the defect" -> the concept election three lines later throws identically; mutation showed the fix-one-site variant still reddens the case · cost: 0 (caught in planning)
- "The fixture's writes roll back with the test" -> H2's SET REFERENTIAL_INTEGRITY, which turnOffDBConstraints issues, COMMITS the open transaction; the concept rename leaked and reddened NonCodedDrugOrderNameTest under -Dsurefire.runOrder=reversealphabetical · cost: 1 harden pass
- "Initialisation is atomic, so a fourth read on the returned Drug needs no fourth guard" -> Drug's lazy ingredients/drugReferenceMaps sets and getFullName/getDisplayName can still throw · cost: 1 harden pass
- "One unreadable drug abandoned the entire read / an empty list" -> it cost the failing order and every order AFTER it; the list is empty only when the failing order was first · cost: 2 harden passes (the claim was re-introduced after being fixed once)
- "Stamping the pass unread whenever an order is dropped is the honest fix" -> measured through the real standingChartAlerts, one never-named order then withholds every chip on the chart, a regression against base · cost: 1 harden pass
- "The catch-breadth guard is satisfied" -> first attempt matched `catch (RuntimeException e)` anywhere in the FILE, so nine sibling catches satisfied it; narrowing the real handler stayed green · cost: 0 (caught by own positive control)

## Raised by a fresh agent, missed by the author
- [harden p2] The test fixture leaked committed state and reddened a sibling class under a reversed run order · blocking-equivalent · cost: 1 pass
- [harden p3] Stamping on the drop alone is a regression against base, measured through the real surface · blocking-equivalent · cost: 1 pass
- [harden p4/p5] Five then three mutations that reddened nothing (a WARN level, e.toString(), activeOrders.clear(), the null short-circuit, the gate, the catch breadth, a fourth Drug read) · cost: 2 passes
- [r1] The standing surface still told operators to check Get Orders for a cause with no privilege to grant — measured with a LogCapture probe on the suite's own fixture · blocking · cost: 1 round
- [r2] The combined-cause WARN arm has neither arm observed; a line-wrapped chained read escapes the allow-list; a javadoc sentence asserts the flag is verdict-neutral when it is a term of the verdict · non-blocking · filed as #421
- [verify r3] The ticket's own LazyInitializationException on Drug#79 observed live, handled per-order by the new accessor

## Where a skill blocked or contradicted this run
- pr-harden:FIX — the fixer was spawned with isolation:"worktree", so its edits landed in its own checkout rather than the loop's; the skill says "leave your work uncommitted in the worktree; the orchestrator commits", which assumes a shared tree. Recovered by generating a patch from the agent's worktree and applying it. Worth stating in the skill which of the two shapes the fixer runs in.
- harden:Phase 2 — the four-lens pass ran five times and the last two found only prose and text-guard evasion; the skill's own "change the KIND of question rather than adding another entry to the list" is what ended it, via a labelled override.

## Declined
- (none in the loop; the round-1 fixer implemented all six findings and declined none)
- Orchestrator declined, on the record: adding a `reference/CLAUDE.md` bullet for the getDrug() rule was initially declined on budget grounds (10 and 54 bytes of headroom), then IMPLEMENTED in round 1 after the fixer trimmed a restated argument to make room. The earlier decline was wrong and the round corrected it.

## Assumptions review overturned
- "The drop case should stamp the pass unread" -> gated on the FAILED drug read only, after a measurement showed the wider rule costs the whole alert list (harden pass 3)
- "The hbm quote is verbatim" -> it was missing not-null="false"; corrected (harden pass 2)
