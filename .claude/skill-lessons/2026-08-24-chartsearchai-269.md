# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #269 / PR 307 · 2026-08-24
outcome: converged
rounds: 1 (pr-harden)   cycles: 1 harden cycle, overridden (7 Phase 1 passes, 5 Phase 2 passes)
verifier: ran (works at runtime)

## Refuted by measurement
- "Reuse the predicate the chip now uses" (the TICKET's own proposed fix) -> understates: allergensMatching("opium") is [tiotropium] even when Papaveretum is on record, so a genuine allergy the allergen arm chips would be hedged · cost: caught at plan gate pass 1, 0 rounds
- "Use the allergen arm's set instead" (plan revision 1) -> overstates: findImpliedSubstances admits equal claimants only at the strongest claimant's rank, so a self-named rule on an entry merely aliasing a recorded `Ketoconazole` would be hedged while its chip stands at full SELF_NAMED_RULE · cost: caught at plan gate pass 2, 0 rounds
- Lead "…but not by a recorded allergy to this drug" -> a categorical the chart can contradict; a reviewer CONSTRUCTED the counterexample (entry aliasing `ketoconazole`, ruling on another of its own self-names, beside an allergy recorded as `Ketoconazole` that matchesDrugName accepts) · cost: 1 Phase 2 pass
- "A constant read from production is what makes a reword visible" -> backwards; rewording it left all 1405 tests green because every assertion compared the constant to itself · cost: 1 Phase 2 pass
- "ContraindicationReading makes the disagreeing pair unconstructible" -> false twice: the SERVICE was a constructor arg (v1), then the FLAG was (v2). `new ContraindicationReading(true, null)` rendered a denial about a chart nobody read · cost: 2 Phase 2 passes
- ADR/injector "each shortfall was found by a reviewer constructing the pair" -> only the flag half was; the service half was read off the signature · cost: 1 Phase 2 pass

## Raised by a fresh agent, missed by the author
- [harden P2r1] render carried the chart twice (parameter + the reading built from it) — the pair the value object exists to remove, one level up · non-blocking · cost: 1 pass
- [harden P2r1] the new file restated the two pre-existing leads as literals with assertNull guards; a reword reddened 5 cases in OTHER files and 0 here · non-blocking · cost: 1 pass
- [harden P2r2] render still took `age`, a second chart-derived fact · non-blocking · cost: 1 pass
- [harden P2r4] the imply-vs-name leg of the union was pinned by NOTHING; mutating allergicSubstanceKeys to findNamedSubstances left 1406 green · non-blocking, real coverage gap · cost: 1 pass
- [pr-harden r1] no case rendered the third reading section beside the other two, so the section ORDER was unpinned; moving its append ahead of the two that make a claim left 1407 green · non-blocking · cost: 1 round
- [pr-harden verify] the residue is real in practice: the model answered from the safety_finding and never surfaced the new hedge, so the record is now truthful but that patient's ANSWER is unchanged

## Where a skill blocked or contradicted this run
- pr-harden/harden State — `git checkout -- <path>` to undo a mutation probe silently took uncommitted edits with it TWICE. One regression (allergicSubstanceKeys' @return reverting from IMPLY to name) survived three Phase 2 passes before an agent caught it. The skill documents this; the discipline (commit before probing) was adopted only after the second incident.
- harden isolation:"worktree" — every agent's worktree opened at origin/main, not the PR branch, so all six had to check the branch out themselves. Cost each agent a detour; two flagged it.
- resolve-ticket Step 3 — the "no third gate pass" rule worked exactly as written: pass 2's non-blocking objection 3 SETTLED the question (take the union of the two disjuncts), which is convergence rather than deadlock.

## Declined
- Hoisting sectionAfter/record/fixtureService into DrugReferenceTestSupport — if we ship without this, a fourth section or a lead that becomes a substring of another makes two copies read the wrong sentence; mitigated here by the pairwise non-containment guard, and the sibling's guards fail loudly rather than vacuously.
- Threading recordedAllergens through from validate — if a future dataset ships DDInter-scale entries WITH contraindication rules, the uncorroborated branch pays ~2ms per allergen twice instead of once; measured 22us on the only shipped dataset that reaches it.
- The pre-existing test files' own lead literals — if we ship without this, a reword reddens their loud positive assertions, which is the safe direction; #269's own guards read production constants and cannot go vacuous.

## Assumptions review overturned
- "The ticket's suggested fix is the fix" -> the union of two predicates, neither sufficient (plan gate, both passes)
- "CLAUDE.md forbids touching an existing test" -> its rule is about WEAKENING a test; a mechanical helper extraction is not that (pr-harden reuse reviewer, corrected on the record)

## Under-captured at write time, added during the retro that read this record
- The worktree bullet above was one sentence and paraphrased an agent. What the six isolated agents
  actually reported: each opened on a branch at `origin/main` rather than at the PR branch, and two
  said in their own reports that the diff they were asked for came back empty until they checked the
  branch out themselves ("`git diff origin/main...HEAD` in this worktree is *empty*", "whoever wires
  the worktree should push the branch into it"). No round or cycle was spent — each agent recovered
  on its own — so the cost is a detour per agent and nothing more.
- Provenance: flagged as after-the-fact capture, because the 2026-08-24 retro's refuter caught this
  record being cited for the quoted string, which was in an agent's report and not here. Same defect
  #302's own provenance note records.
- Two incidents of `git checkout -- <path>` reverting uncommitted work, not one, and the second is the
  one worth the record: it reverted a javadoc correction (`allergicSubstanceKeys`' `@return` from
  IMPLY back to name) which then survived THREE Phase 2 passes before an agent re-found it. Both were
  the orchestrator's own probes on files carrying uncommitted work; adopting "commit before probing"
  after the second is what stopped it, and no third occurred.
