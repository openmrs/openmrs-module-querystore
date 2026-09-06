# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #360 / PR 363 · 2026-09-02
outcome: converged
rounds: 2 (pr-harden)   cycles: 7 (harden)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-360/e54df04b-425a-48a2-8920-11fc53b58eea.jsonl

## Refuted by measurement
- "Subtract `safety_finding` from the uncited attribution corpus, because a finding is this validator's
  own conclusion and admitting it lets one pass silence the next" -> the circularity cannot bite for a
  finding's own subject (the pre-answer pass runs with an empty answer, so every subject is
  question-named or actively ordered), AND the subtraction made the fix a no-op for every question
  naming no drug: on the six-order screening chart over the shipped KB, 22 chips uncited vs 10 with one
  `[2]` added, six of the difference naming lovastatin. Reverted. · cost: 2 harden cycles
- "The gate's proposed alternative — attribute per-occurrence inside a span the answer reproduces from
  the record" -> refuted by the operator's own recorded measurement that the live model paraphrases and
  misspells the prose it copies, so a verbatim-run test is green in the suite and dead on the rig; also
  would have reddened `allergyEchoedOffTheChartDoesNotChipItself`. · cost: 0 (caught at the gate)
- "The withheld residue is the question drug's KB partners" -> wider: the record renders each partner's
  mechanism paragraph verbatim, so `naproxen` (named only inside the aspirin mechanism sentence) is
  withheld on the shipped KB. · cost: 1 harden cycle

## Raised by a fresh agent, missed by the author
- [harden P2] The `safety_finding` exclusion created a measured circularity — and then, one pass later,
  the SAME exclusion was measured to reopen #360 for drug-less questions. Two agents pulling opposite
  ways on one fact (mechanism paragraphs name third-party substances) is what located the right design.
  · blocking · cost: 2 cycles
- [harden P2] "What stays withheld is an INTERACTION finding" was false — the exemption removes the drug
  from `inPlay`, which the contraindication and overdose arms also iterate. Measured with a base-sha A/B.
  · blocking · cost: 1 cycle
- [harden] Four consecutive cycles each found another home of a corrected claim that the previous
  sweep had missed, including the CANONICAL public-entry javadoc. Sweeps that scoped themselves to the
  two files just edited missed a third. · cost: 4 cycles
- [r1] The `safety_finding` half of `isRecitableReferenceMaterial` was pinned by no test: narrowing it to
  `drug_reference` — the exact alternative its own javadoc advertises — left the whole build green.
  · blocking · cost: 1 round
- [r1] The typo control's chart was built with `injector(...)`, which wires no validator, so
  `preAnswerFindings` returned empty unconditionally: its "nothing injected" was structural, not
  measured, and its precondition used a `drug_reference`-only accessor blind to `safety_finding`.
  · non-blocking · cost: 0 (same round)
- [r2] The same typo control could not see a widening at all — its corpus is empty by construction — while
  its comment claimed it would. · non-blocking · cost: 0 (finish step)

## Where a skill blocked or contradicted this run
- resolve-ticket Step 3: gate pass 1 said to borrow `ChartSearchAiUtils.isReferenceMaterial`; gate pass 2
  cited `ReferenceProseFidelityCheck.isModuleSuppliedReferenceProse`, which deliberately does NOT borrow
  it and records why. Pass 2's citation settled it. Two passes disagreeing, with the second carrying the
  repo precedent, is the gate working — but only because pass 2 was asked to attack pass 1's fix.
- harden Phase 2 / this orchestrator: I ran `git checkout -- <file>` after a mutation probe on a file
  carrying UNCOMMITTED intended work, and it reverted ~9 edits (the predicate rename, the exclusion, and
  every prose correction). The skill warns about exactly this; I hit it anyway because the commit-first
  rule reads as being about AGENTS. Cost: one rebuild + full reapply from my own scripts.
- pr-harden FINISH: the fixer died on a session 429 mid-phase, having made its edits but not its mutation
  check. Completing it in-session (build + the mutation it never ran) was cheaper than the two retries
  the contract allows, and the residue was coherent enough to verify rather than discard.

## Declined
- (none — every finding from both review rounds was implemented)

## Assumptions review overturned
- A2 "verification row 4 is satisfied structurally, since a question naming no drug injects nothing" ->
  false: `preAnswerFindings` can inject reference-group `safety_finding` records on a drug-less question.
  Replaced by a real test. (gate pass 1, before code)
- "The containment with SubjectMatter is preserved by handing both consumers the same list" -> replaced
  by an argument that does not depend on the corpus at all (`findImpliedByQuery` returns a subset of
  `findByQuery`, so the answer names every exemptible entry), which is what allowed the two corpora to be
  separated. (gate pass 2)
