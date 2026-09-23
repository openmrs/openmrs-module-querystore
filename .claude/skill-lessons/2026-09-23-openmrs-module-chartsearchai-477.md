# resolve-ticket + harden + pr-harden · openmrs-module-chartsearchai · #477 / PR #483 · 2026-09-23
outcome: did-not-converge (round 1 blocking finding r1-1 declined by the fixer, on #402's recorded reversion and CLAUDE.md's referent rule)
rounds: 1   cycles: 5 (harden: Phase 1 converged, then Phase 2 escalated four times before a clean one)   verifier: ran on 154225d6 (works at runtime); not re-run on final head 1ff81089
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-477/5092c704-ef8f-4270-9fba-4620c6ca25b1.jsonl

## Refuted by measurement
- "Grade the new unrated duplicate finding a caution, like #400" -> gate pass 1: a caution's prompt branch opens "the drug can be given"; Decision 86 graded down only classification · cost: 1 gate pass
- "Attribute a substance to an order by a chart ATC code the dataset names" -> gate pass 1: Omeprazole/Esomeprazole share A02BC05, a positive claim would name the wrong order · cost: 1 gate pass
- "Attribute by any recorded name of the order" -> gate pass 2: #293, drugNonCoded names a different drug than the display · cost: 1 gate pass
- "Use findImpliedSubstances, findNamedSubstances' contracted input" -> measured: resolves neither TB combination display; reverted to findImpliedByDrugName · cost: 0 (in-pass)
- "Current-medication referent for the new finding (settled at gate pass 2 by the #348 rule)" -> round 1 reviewer + live verifier: the finding became the arm's one current-medication site beside proposal-withhold rule chips (#402's reverted shape) and still ranked below the Major; live answer led "No — Rifampicin should not be given" · cost: 1 round, and the run's convergence

## Raised by a fresh agent, missed by the author
- [harden P2a reuse] findNamedSubstances handed the unfolded implied set; 678 shipped aliases name nothing unfolded (Acticlate) · substantive · cost: 1 escalation
- [harden P2a efficiency] per-order fresh name cache: 58->82 dataset sweeps on the six-order case (64 after sharing) · substantive · cost: same escalation
- [harden P2b quality+integration] count label "(2 active orders)" put the ACTIVE_ORDER noun in twice -> citation check read two claims, first uncited · substantive · cost: 1 escalation
- [harden P2c reuse] my noun-once assertion re-expressed the citation check's counting rule in test code (CLAUDE.md forbids) · substantive · cost: 1 escalation
- [harden P2d reuse+quality] inserted test orphaned sentenceFragment's javadoc — the memory's recurring pattern, again, with the sweep command in hand and not run · substantive · cost: 1 escalation
- [r1] r1-1 above · blocking · cost: run did not converge
- [r1] PR body's reason for deferring the one-order case recast Direction bullet 1 as a duplicate-therapy ask · non-blocking · fixed

## Where a skill blocked or contradicted this run
- resolve-ticket Step 3 outcome 2 vs pr-harden round 1 — gate pass 2's objection was read as SETTLING the referent (current-medication, #348's counterpart-clause rule); round 1 showed the question was actually OPEN (#402's one-site reversion cuts the other way) — i.e. abort condition 3 arguably applied at plan time. Cost: the whole implementation was built on a referent the first review undid.
- harden Phase 2 "runs once unless substantive" — every escalation was a defect introduced by the previous escalation's own fix (label, test, insertion); five Phase 2 traversals of 4 agents each.
- pr-harden FINISH/Termination — a declined blocking finding ends the run; the fixer implemented the finding's DIAGNOSIS a different way than its recommendation, which the contract does not distinguish from a plain decline.

## Declined
- r1-1 re-label the drug's rule chips to the current-medication referent when two orders carry it — if we ship without it, the reproduction's answer still opens by refusing rifampicin as a proposal (as on main, #402 still open); taking it is #402's reversal, owing every site and Decision 72's 14-cell A/B, and a two-order trigger has no referent reason.
- harden deferrals: #477 arrangement constants duplicated across two test classes; SubstanceInSeveralActiveOrdersTest not routed through chipsOverOrders; rawContextNaming fold; optional name->rows memo (last 6 of 64 sweeps); pre-existing phrase-vs-noun javadoc drift and an orphaned javadoc at ChartSearchService:1291 (both pre-existing).

## Assumptions review overturned
- "The new finding's referent is a current medication (#348)" -> proposal referent, like its arm (round 1)
- "The one-order case is deferred because it is not a duplicate-therapy finding" -> deferred because it is #402's referent change (round 1, r1-2)
