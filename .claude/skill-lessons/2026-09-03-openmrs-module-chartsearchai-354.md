# resolve-ticket · openmrs-module-chartsearchai · issue #354 · 2026-09-03
outcome: aborted (condition 3 — gate pass 2's second blocking objection leaves the design open)
rounds: 0   cycles: 0   verifier: skipped (aborted at Step 3, before any code)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-354/3f5c4bef-0066-43e6-8d6a-dd5b5374b1f9.jsonl

## Refuted by measurement
- "#362 (#355's fix) bounded the per-record cost, so ADR 67's 'N members cost N x
  MAX_INTERACTION_RENDER_CHARS' no longer holds" -> false. #362 bounds a record only where
  OrderedInteractions.nothingPatientSpecific() holds; a PROMOTED note deliberately overrides the
  budget. Measured through the real injector/validator over the shipped KB: NSAID class expansion on
  an 8-order chart = 28 records, 39140 reference chars, 163 chips (largest single record 1694
  chars). On the issue's own 3-order regimen: 27 records / 5285 chars / 11 chips. Oral contraceptive:
  11 / 3807 / 10 and 11 / 10453 / 14. · cost: 1 gate pass + the measurement
- "findImpliedByDrugName is the ranked accessor, so it is the right way to resolve a curated member
  name" -> false. `ethinyl estradiol` resolves to Fluoroestradiol f-18, Estradiol, Estradiol
  (topical), Ethinylestradiol. Identity (DrugReference.isNamed, via nameIndex()/entriesNamedBy) is
  the right accessor and measured 0 ambiguous names across 3 lists (37 + 16 + 21). · cost: 0
- "A hand-curated membership list derived by enumerating the KB's ATC subtrees is complete enough to
  guard" -> false in two ways. 444 of 2283 shipped entries carry an EMPTY atc array (Mefenamic acid,
  Norgestimate, Norgestrel, Mestranol, Norelgestromin, Segesterone among them), so an ATC guard
  cannot reach them; and the list I derived was itself measurably incomplete (Etynodiol, Dienogest,
  Estetrol dropped; "Capsaicin" written where the KB name is "Capsaicin (topical)"; one Salicylic
  acid exclusion where the KB carries three names). That is ADR 67's cited #161/#263 failure mode
  realised on the attempt. · cost: 1 gate pass

## Raised by a fresh agent, missed by the author
- [gate1] CLAUDE.md's namedDrugClass bullet forbids the CALLER from reading its answer as putting
  substances in play; the plan's rebuttal answered the wrong half (the return type, not the caller).
  · blocking · cost: plan rewrite
- [gate1] Five DrugClassQuestionNoteTest cases break because the 16-drug excerpt contains Ibuprofen
  and Acetylsalicylic acid, so an NSAID question resolves there. · blocking
- [gate1] Widening the screening-arm gate silently disables the whole-chart pairwise screen and
  swaps a truncatable extent for a non-truncatable one. · blocking
- [gate1] resolvedSubstanceRows(questionDrugs, ...) at DrugSafetyValidator:498 was omitted from the
  plan's site list; left narrow it reopens #238. · blocking
- [gate1] Monotonicity fails: a partial membership list plus a suppressed note and
  unresolvedDrugClass:null is WORSE than today's silence, because nothing states the screen was over
  a member list. · blocking
- [gate2] SubstanceSubjects/interactionSubject cannot be reused by an arm whose subjects are not in
  questionDrugs or inPlay — subjectOf falls to the positional branch (#206's defect), and
  ChipSubjectOneResolutionTest structurally pins both workarounds shut. · blocking
- [gate2] The three pairwise arms are NOT mutually exclusive as claimed: the screening arm's
  pairExtent assignment at :697 is unconditional, and its gate overlaps a class gate. The suite
  already drives the overlapping question. · blocking
- [gate2] The injector cannot know which members' chips survived the cap: SafetyWarning carries no
  subject ENTRY, only a display string, so re-resolving it is #151's shape. · blocking
- [gate2] Injecting a member record puts the member in the recitable corpus, so an answer proposing
  that member is echo-suppressed and loses its contraindication/allergy chips — and subtracting the
  type from the corpus is already measured wrong (#360). · blocking, OPEN
- [gate2] The plan's own test-impact remedy was wrong for
  everyClassNameTheTableCanReportNamesNoSubstanceOfTheShippedKnowledgeBase — moving it to an inert
  service destroys the guard's purpose. · non-blocking

## Where a skill blocked or contradicted this run
- resolve-ticket Step 3 allows exactly one revision and one re-gate. Gate pass 2's open objection had
  a third remedy neither gate considered (run the contraindication checks inside the new arm), but
  proposing it would have needed a third pass, which the section forbids. The rule held; recording it
  because the shape recurs: an "open" objection can be open only relative to the two branches the
  refuter enumerated.
- resolve-ticket's Step 1 pre-flight and the autonomy contract worked as intended: the standalone,
  llama-server and models were all confirmed present at Step 1 and never became the blocker.
- Waiting for a background subagent inside one turn is awkward: foreground `sleep` is blocked, the
  Monitor until-loop fired early on an lsof check, and file-size stability fired early on a buffered
  transcript. Four wasted wait cycles across two gate passes.

## Declined
- (no review rounds ran)

## Assumptions review overturned
- "The remaining deliverable is the issue's outcome (1), and it is one PR" -> outcome (1) is right,
  but after two gate passes its requirements are: a new capped/ranked/extent-stating arm; explicit
  precedence coding that breaks two pinned arrangements; extending SubstanceSubjects inside validate;
  a new channel carrying surviving member entries from validate to injectRecords; contraindication
  coverage inside the arm to avoid an echo-scoping regression; exhaustively re-derived membership
  lists; a fourth reference-group type with ~7 named structural guards plus a README client contract;
  and five superseded test cases. Not one change.
- "A curated membership list is a module deliverable because the module already ships
  cross-reactivity-groups.json" -> the precedent is real but does not answer ADR 67's ground, which
  is about a hand-picked list being incomplete. Measured incomplete on the first attempt.
