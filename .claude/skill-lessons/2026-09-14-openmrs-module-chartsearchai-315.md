# resolve-ticket + harden + pr-harden · openmrs-module-chartsearchai · #315 / PR 431 · 2026-09-14
outcome: converged (pr-harden round 1, 0 blocking, verified at runtime) — harden itself ended on a labelled override after cycle 4
rounds: 1 (pr-harden)   cycles: 4 (harden)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced · two session rate-limit interruptions, both resumed by the pool driver
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-315/471c131f-68ac-4cac-9fd9-688e908a3fca.jsonl

## Refuted by measurement
- "orderActive == FALSE means the order ended, so publish its date" -> `Order.isActive()` returns false for voided/DISCONTINUE before consulting any date, and `cloneForDiscontinuing()` sets neither; refuted at the refutation gate, before code · cost: 0 rounds
- "getDateStopped() else getAutoExpireDate()" hand-rolled -> that IS core's `Order.getEffectiveStopDate()`, bytecode-identical; gate objection, verified by javap · cost: 0
- "the gate alone reddens the test-order and unevaluable cases" -> the gate alone reddens ONE case; the unevaluable order is refused by `forRecord` answering null, not by the try scope; needed a three-part mutation to leak · cost: 1 cycle
- "a discontinuation carries no end date, so the commonest ended shape produces no entry" -> `stopOrder` stamps `dateStopped` on the PRESCRIPTION; then a second attempt said the stub carries none, and `saveOrderInternal` stamps `autoExpireDate` on it too. BOTH records of a real discontinuation are dated · cost: 2 cycles, refuted twice
- "querystore renders no auto_expire_date at all" -> it renders none into the record TEXT and does carry it in document METADATA, which this module's own javadoc already said · cost: 1 cycle
- "the 1464-test api suite ... and nothing else" -> the suite runs 2196, and deleting the figure left the exclusivity half, which this change's own new tests falsified (5 cases redden, 4 of them added by #315) · cost: 1 cycle

## Raised by a fresh agent, missed by the author
- [harden c1] nulling all three `LlmInferenceService` wirings left the entire 2386-test suite green — the statement was resolved by production code no test executed · blocking-equivalent · cost: 1 pass
- [harden c1] the stop-date stamp was unpinned across the real `DrugReferenceInjector` while the SIBLING stamp on the same record was pinned; the same mutation on `orderActive` reddens · cost: 1 pass
- [harden c2] the `non-null implies FALSE` contract had no witness — the live case used an order carrying NEITHER end date, so it asserted nothing its inputs could produce. The witness is a live duration-based prescription (future auto-expire), the commonest live shape · cost: 1 pass
- [harden c2] the ordering rule was unpinned: with distinct record dates the citation sort already leaves the resolution ascending, so TreeSet -> insertion order stayed green. A same-date pair discriminates · cost: 1 pass
- [harden c1] `ArchitectureGuardTest`'s sole-caller walk had been copied a third time and the copy had already drifted (path-separator normalisation) — the helper was already parameterized for exactly this · cost: 1 pass
- [harden c1] an orphaned serializer javadoc: the new method landed between a javadoc and its method. The skill's own awk detector found it · cost: 1 pass
- [pr-harden r1] the single-writer guard covers `SerializedRecord` but the PUBLISHED value is read off `RecordMapping`, whose stop-date rung is guarded by nothing; writing a date at the injector leaves every guard green · non-blocking · deferred to #432
- [pr-harden r1] the UTC caveat understates its own direction: west of UTC the shift runs the other way at ordinary clinic hours, measured on three zones · non-blocking · deferred to #432

## Where a skill blocked or contradicted this run
- pr-harden:Step 1 "compare the base you just fetched against the one the previous round saw" — caught `main` merging its OWN Decision 97 while this branch held 97. The skill records this as recurring on three consecutive runs; it recurred again. Cost: a merge, a full renumbering sweep, and a re-measurement of two claims the merge could have falsified (one held, one did not).
- harden:Termination — four cycles each yielded one-line prose residues in two claim families; what finally closed them was enumerating the FAMILIES across the diff rather than fixing instances, which is the skill's own "change the KIND of question" remedy. The run still ended on the labelled override rather than a clean cycle.
- ProjectInstructionsGuardTest's root budget was 8 bytes under its cap, so a one-rule change could not land without a raise — the fourth, and the third in eight days. The guard's javadoc predicted exactly this and advised a split; the advice was recorded, not taken.

## Declined
- The `cited == null || mappings == null` disjuncts and the element null-guards — "if we ship without distinguishing them, nothing breaks, because neither operand can be null in production (`extractCitedReferences` never returns null, `getMappings()` never null); they cost two comparisons and prevent an NPE for a future caller."
- Changing `formatDate`'s UTC conversion — "if we ship without it, a server east of UTC publishes the previous calendar day for an order stopped in the small hours; fixing it moves every date this module publishes, which is a separate decision with its own compatibility question."
- Cell H's fabricated stop date (ADR Decision 47) — "if we ship without it, a same-drug renewal history can still produce a fabricated date in the prose; five wordings were measured and all five fabricate, so it is a property of the clause and the remedy is in querystore."

## Assumptions review overturned
- "the PR can say `Fixes #315`" -> the refutation gate showed it under-delivers on three counts (inert on the two naming-nothing shapes, the base on that cell is unsettled, and the title's clause stays true of the prose because rendering is an ESM change). Switched to `Refs`, and #315 named by hand in every reviewer brief since `closingIssuesReferences` is then empty.
- "the measurement belongs in the ADR" -> it had reached five files and needed correcting twice; consolidated into one home (`SerializedRecord.orderStopDate`) with everything else pointing there. Overturned in harden cycle 2.
