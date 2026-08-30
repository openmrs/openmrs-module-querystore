# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #229 / PR 334 · 2026-08-30
outcome: converged
rounds: 1 (pr-harden)   cycles: 2 (harden, pre-PR)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-openmrs-module-chartsearchai/b5d47443-3315-49a8-9f78-e3cf171c608e.jsonl

## Refuted by measurement
- "The DEBUG line's char total and the durable row must state the same number, so delete the injector's
  private sum" -> `ReferenceRecordSubstanceCollapseTest` is the SPEC for that fragment and requires the
  drug-reference ENTRIES' chars and explicitly not the findings'. The full build refuted it. Both numbers
  are now printed, labelled. · cost: 1 build
- "Zero has three causes" (published in 6 files) -> it has more: injectFromQuery / injectFromOrders /
  drugSafety.validateAnswers each produce it, as does an injection that added only active-order records.
  Replaced with the property + an explicit "not offered as complete". · cost: 1 cycle
- "The sweep fails on the commit that adds a fifth reference type" -> a THIRD reference-GROUP type. Two
  types are classified today, so a hardcoded pair diverges on the third. Demonstrated by adding one. · cost: 1 cycle
- "CLAUDE.md: a re-hardcode only fails once a fourth injected type exists" (pre-existing, cited by my new
  bullet as its authority) -> falsified by experiment: hardcode the predicate, add a fourth CHART-group
  type, sweep stays green 7/7. · cost: 1 cycle
- "every assertion below fails" if the slice is resolved pre-inject -> two of three; the third builds a
  ChartAnswer directly and no ordering moves it. · cost: 1 cycle
- "Reusing a liquibase id is silently skipped" -> liquibase stores a checksum beside id+author+filename
  and raises ValidationFailedException; the module fails to start. Loud, and worse. · cost: 1 cycle
- PR/ADR claim that the silent-audit-death hazard follows from EDITING changeset 002 -> the verifier
  measured that APPENDING 009 reproduces it anyway: core runs a module's changelog only on a version
  change, so a same-version SNAPSHOT redeploy skips it, module started=true, null error, ten minutes
  serving requests against a table lacking the columns. · cost: 1 doc commit

## Raised by a fresh agent, missed by the author
- [gate p1] The liquibase strategy rested on an unmeasured claim about deployed instances · blocking · cost: 1 gate pass
- [gate p1] No test covered the CARRYING, which the plan named as its own root cause · blocking · cost: 1 gate pass
- [gate p1] The "counts the group not the type" pin was one-directional — a wrong impl counting everything
  the injector added would pass it · non-blocking
- [gate p2] Only a DAO round-trip can see a property missing from the .hbm.xml; controller tests capture
  the entity and never persist · blocking · cost: 1 gate pass
- [harden] The fourth consumer of the provenance classification had no enumeration sweep, and the javadoc
  claimed kinship with the three that do · cost: 1 pass
- [harden] Two orphaned javadocs from inserted methods — silent through compile, checkstyle and 1686 tests · cost: 1 pass
- [harden] "floor on the bytes spent" does not hold for a serializer-minted mapping (date + group label
  the chart line dedups away) · cost: 1 pass
- [harden] Streaming test claimed "by construction" while asserting int equality; a second derivation
  satisfies it. assertSame · cost: 1 pass
- [harden] liquibase.xml's HEADER invited the edit this change measured to be dangerous. ADR 56 named the
  invitation; naming it there did not remove it from the file an author opens · cost: 1 pass
- [r1] The schema half was pinned by nothing: deleting changeset 009 entirely left the suite green · non-blocking · cost: 0 rounds

## Where a skill blocked or contradicted this run
- pr-harden:"State" — `gate-state --count-edits` reported edits=0 with 3 unpushed commits on a branch that
  had no upstream yet. Did not affect the outcome (I measured commits myself) but the field was not
  reporting what its description says.
- harden:Termination — the confirming-cycle rule worked exactly as designed here: every one of the last
  seven passes found exactly one real defect, all prose, each in a file the change had not edited. Six of
  them would have shipped under any "it's basically converged" stop.

## Declined
- Sharing `createAuditLog` in HibernateChartSearchAiDAOTest — if we ship without it, a future NOT NULL
  column needs updating in four places instead of three; and the new case cannot reuse the helper without
  parameterising it, which is worse than the duplication.
- A structural guard against re-hardcoding the type list inside `referenceSlice` — if we ship without it,
  such a hardcode is invisible until a third reference-group type arrives, which is exactly the limit
  CLAUDE.md already records for the sibling method, and the new sweep fails on the commit that adds it.
- Unused `MediaType` import in the controller — pre-existing at a5d533a0, not this change's.

## Assumptions review overturned
- "A new changeset guarantees the columns exist on an upgraded instance" -> only on a version CHANGE;
  a same-version redeploy runs no liquibase at all (verifier, round 1 FINISH).
- "Searching the corrected phrasing finds every home of a claim" -> it does not. The last four homes were
  found by wording variants ("the injected slice"), by files the change never edited (LogCapture), and by
  a four-word slogan in a migration plan. Search the rarest TOKEN, and the files NOT in the diff.
