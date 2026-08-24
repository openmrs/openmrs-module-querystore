# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #268 / PR 306 · 2026-08-24
outcome: converged
rounds: 1 (pr-harden)   cycles: 3 (harden, ended on labelled override)   verifier: ran (works at runtime)

## Refuted by measurement
- "The ticket's comment closes the sizing: 0 of 36 reachable, so this is a hazard, fixture-only." -> the
  comment measures DrugReferenceValidity's population (alias == another entry's DISPLAY name), where
  nameMatchStrength never ties. The chip turns on findImpliedSubstances' equal-claimant leg, which ties on
  a name that is NO row's display name. Live on the shipped KB: findImpliedSubstances("ado-trastuzumab
  emtansine") -> 3 substances. · cost: gate pass 1 (caught before any code)
- "Gate the sentence on the row's display name occurring in the recorded string, with the principal
  resolution exempt." -> renames 342 rows over the shipped KB, 179 of them from legs the plan had just
  certified correct, and the entire suite stays green on all of it. · cost: gate pass 1
- "The leg-1 (principal resolution) exemption is safe." -> lookupByToken breaks a tie by earliest dataset
  entry, so on the very tie the fix targets it blesses whichever row dataset order put first: `gallium`
  kept "Gallium citrate ga-67" while renaming its two co-tied rivals in one payload. · cost: gate pass 2
- "The derivation clause mirrors legs 3/4." -> it asked "does this ROW claim a constituent" where the leg
  asks "what does the constituent RESOLVE to". 53 more shipped-KB pairs were falsehoods it left standing
  (hydrocortisone / neomycin -> Hydrocortisone butyrate). · cost: harden cycle 1 pass 2
- "The unique clause is sound." -> at containment rank the equal-claimant leg never runs, so an
  uncontested survivor is uncontested by ARTEFACT. `gallium — hives` went on naming a radiodiagnostic —
  four characters of reaction text defeated the whole fix. · cost: harden cycle 2
- "145 containment-only pairs, every one named by its own label." -> 143; the other 2 by the derivation
  clause. · cost: harden cycle 2
- "The three gallium rows are what the appendsGenericName guard keeps out." -> they carry no generic at
  all (the ddinter parser sets one only where the display name does not contain the rxnorm_name), so they
  exercise the other half entirely. · cost: harden cycle 2
- "15 (name, row) pairs turn on the appended-generic half." -> relayed from a review report and matching
  neither thing it could mean: the half ADMITS 512, the guard REFUSES 7. · cost: harden cycle 3

## Raised by a fresh agent, missed by the author
- [harden c1] The appended-generic clause, the derivation clause and the substance-keyed mirror were each
  pinned by NOTHING — disabling the derivation clause outright (153 rows it alone decides) left all 1384
  tests green. · non-blocking · cost: 1 pass each
- [harden c2] The class sentences still asserted the allergy the identity chip had just declined to, in
  ONE payload on shipped data. The fix was half a fix and its own javadoc correction over-claimed. · cost: 1 pass
- [harden c2] The first sentence form broke a documented wire contract (README + SafetyWarning.getDetail):
  a quoted detail names its drug NOWHERE by construction, and 32 recorded names gave byte-identical
  details on different subjects — #238's collapse from the other side. · cost: 1 pass
- [harden c2] Two order-dependencies the sentence split introduced: the dedup discarded a naming record's
  evidence, and the ledger kept whichever chip arrived first. · cost: 1 pass
- [pr-harden r1] The equal-rank tiebreak covered only the identity chip; the class sentences' allergen
  half has two forms too, so it was order-dependent in exactly the shape the tiebreak was added to
  remove. · non-blocking · cost: finish round
- [pr-harden r1] `{@link #alreadyResolved}` named a method renamed earlier in the same branch; CLAUDE.md
  had no entry for the new accessor, whose misuse as a resolver is fail-CLOSED and silent. · non-blocking

## Where a skill blocked or contradicted this run
- pr-harden:"VERIFY" — "never a server that was already running when the run began" would have aborted as
  `unrepairable`: one standalone on disk, running. The user's standing preference (standalones are
  disposable; only limits are unattributable processes and repairing the artifact) resolves it, but the
  skill states the conservative rule as if it were the only one. Cost: none, because the brief carried the
  override explicitly.
- pr-harden:"COMMIT" — the round-1 reviewer left the worktree on its own `pr-306-r1` branch and the finish
  commit landed there. The skill predicts this exactly and the pre-commit branch check caught it; fixed by
  an append-only fast-forward. Cost: none, but it fired on the FIRST round of the run.
- harden:"Termination" — cycles 2 and 3 were dominated by correcting prose the previous cycle had written,
  which is the anti-pattern the skill names. Deleting rather than rewording converged it; the override was
  taken at cycle 3 because pr-harden supplies the same adversarial pass with a fixer and verifier attached.
- Own error, not the skill's: `git checkout -- <path>` to undo a mutation probe discarded three uncommitted
  production edits on that file. The skill warns about this in exactly these words. Cost: one re-apply.

## Declined
- (none — every finding from every round was implemented)

## Assumptions review overturned
- A1 "the defect is a hazard, fixture-only on shipped data" -> live on the shipped default dataset;
  the PR states it as such. (gate pass 1)
- A2 "name the recorded allergen where the row is unsupported" -> that sentence names its drug nowhere and
  breaks the wire contract; it states the RELATIONSHIP instead, in the curated-rule arm's shape. (harden c2)
- A4 "only the identity chip's DETAIL changes" -> all three of the arm's sentences change, and the ledger's
  tiebreak with them. (harden c2 / pr-harden r1)
