# resolve-ticket + harden + pr-harden · openmrs-module-chartsearchai · #421 / PR 423 · 2026-09-14
outcome: converged
rounds: 2 (pr-harden)   cycles: 6 (harden)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-421/62464f44-e962-4ba3-a166-14e8c9d90476.jsonl

## Refuted by measurement
- "The getActiveOrders-throws route cannot reach the both-causes branch, because the catch is outside the loop" (the TICKET's own claim, carried into the plan) -> the catch wraps the whole loop, so a drop at iteration N plus a throw at N+1 sets both flags; DrugReferenceTestSupport.partiallyReadOrdersCtx's javadoc already records a review agent measuring exactly that, and records that an earlier javadoc called it unreachable and was wrong · cost: gate pass 1 (no code)
- "Sharpening the drug.get allow-list closes the line-wrapped escape" (the ticket's suggested fix) -> closes one of two spellings; `drug` + newline + `.getConcept()` contains no `drug.get` substring at all, so the allow-list never runs on it. Both measured green on 01337385 · cost: gate pass 2 (no code)
- "No guard can express 'no verdict may be built from this accessor', because admitting the one legitimate reader means excusing all of DrugSafetyValidator" -> true of a filename-keyed source scan; false in general. The realistic harmful site is in DrugReferenceInjector, a different class, and a class-granular constant-pool question separates it cleanly · cost: 1 cycle
- "coded.dosageForm.getUuid() at a call site leaves this class green" (recorded from an agent report, never run by the author) -> unguarded it reddens 10 of 13 on the null dose form; only the null-guarded spelling is green · cost: 1 cycle
- "Since issue #421 there is one such site rather than three" -> #413 made it one; the base sha already asserted that count · cost: 1 round

## Raised by a fresh agent, missed by the author
- [harden c1] A SECOND private nested carrier holding the Drug escapes a name-keyed reflection lookup entirely · blocking-equivalent · cost: 1 cycle
- [harden c1] CodedDrug.concept/.dosageForm are default-lazy Concept proxies, so "no lazy association is reachable from this loop" was false and contradicted conceptUuid's javadoc 200 lines down · cost: 1 cycle
- [harden c1] "all three reads cannot throw once the fetch succeeded" is false — getName() IS the throwing read — and taken at face value argues for narrowing the broad catch a guard exists to forbid · cost: 1 cycle
- [harden c2] The predicate used exact type equality, so Drug[] and List<Drug> on the carrier were handed straight to the loop · cost: 1 cycle
- [harden c2] Two new javadoc sites nominated conceptUuid as "the one home" and then restated its content, with three guarded readers where there are four — the omitted one called from the very method the branch rewrites · cost: 1 cycle
- [harden c3] A parameterized type's RAW type went unread, so a generic subclass of Drug escaped · cost: 1 cycle
- [harden c4] Wildcard and type-variable bounds went unwalked; List<? extends Drug> escaped · cost: 1 cycle
- [harden c4] The "two questions" that replaced the enumeration were wrong in BOTH directions (List<Serializable> answers no to both and is reported) · cost: 1 cycle
- [pr-harden r1] The class-file guard must excuse all of DrugSafetyValidator, leaving the likeliest second reader — another beside the first — unguarded; SourceScan in the reference package closes it · non-blocking, implemented · cost: 1 blocking-only round
- [verifier] Established a LOGGER POSITIVE CONTROL before reporting absent warnings, unprompted by the brief's wording — without it an absent WARN proves nothing

## Where a skill blocked or contradicted this run
- resolve-ticket Step 9 / Stop gate — the run completed harden and reported without opening the PR; the gate caught it and named the owed phases. Correct catch, cost ~0.
- Session usage limit killed all four cycle-3 Phase 2 agents mid-flight; pool-run re-invoked after reset and the run resumed from state on disk with nothing lost. The harden dead-phase contract (clear await, retry twice, change something) covered it.
- macOS has no `timeout(1)`; a mutation loop using it silently produced no output and no error (the `||` fallback never fired because grep exited 0 on empty input). Cost: one confused re-run.

## Declined
- A reference/CLAUDE.md bullet for the accessor rule — if shipped without it a maintainer who never opens that directory could add a fourth read at a call site; but the compiler refuses the Drug case, the structural guard reddens on the carrier case, and the file measured 75,817 bytes against a 76,000-byte budget, so a bullet forces a budget raise for a rule two build-failing guards already hold.
- Extracting the constant-pool walk shared by three sites — all three sites are in ONE class, and the repo's recorded threshold counts CLASSES, not call sites.
- Moving anyHandsOutTheEntity nearer its callers — nothing is broken by the placement, and a manufactured edit buys another mandatory cycle.

## Assumptions review overturned
- "Finding 2 is a test-only change; no production change is needed" -> the text guard cannot discriminate the property at all, so the production side stopped handing back the entity; gate pass 2, before any code.
- "Finding 3 needs no new test, the behaviour is already pinned" -> the pin cited pins the opposite direction; the imperative was unpinned, and after one false start about why it could not be pinned, it got a class-granular guard plus a per-file one.
- "The enumeration of what the predicate catches just needs one more correction" -> nine successive versions were each refuted; what ended it was deleting the claim SHAPE, not improving it (harden c4).
