# resolve-ticket · openmrs-module-chartsearchai · #294 · 2026-09-12 (started 2026-09-11)
outcome: in-progress at time of writing (harden converging; PR not yet opened)
rounds: 0 (pr-harden not yet entered)   cycles: 1 (Phase 1 ×8 passes, Phase 2 ×3 passes + 1 retry)
verifier: n/a yet — but the LIVE measurement was itself run on the pool standalone
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-294/171b90dd-a1c8-490e-b56c-918ab703d129.jsonl

## Refuted by measurement

- "The wire verdict for this record is unobservable / nobody can read it" (the ticket's own
  framing) -> the wire DOES carry it; #291's install simply had grounding OFF. · cost: 0, caught
  at plan time
- "The unmeasured gap is one unknown: does the module attach `false`?" -> ADR Decision 41's
  residue already recorded it as arrangement-dependent. · cost: 1 gate pass
- "Nothing pins Decision 41's residue in either cell" -> cell A2 IS pinned, over a real-chain
  `active_drug_order` record, and the #284 branch reads no record text so a codes-only display
  cannot move it. · cost: 1 gate pass (settled, no deadlock)
- "The model writes a sentence naming no drug, which the record entails, so `true`" -> measured
  against a CONFOUNDED arrangement: changing an order's uuid left its own stale index record in
  the chart as a NAMED TWIN for the same prescription. · cost: 1 Phase-1 pass + a standalone
  rebuild + restart
- "The answers omit a medication" (twinned arrangement) -> they were declining to list one
  prescription twice; record [1] carried the same order's original uuid. · cost: folded into
  the above
- "The antecedent does not arise; no `false` observed in 18 cells" -> ALL 18 cells asked about
  the MEDICATION. Asked about the RECORD ("any active drug order whose drug the chart does not
  name?"), the model cites it and the verdict IS `false`. Two of five probes still named the
  SUPERSEDED arrangement's drug. · cost: 1 Phase-2 pass + a re-run; inverted the headline
- "The gate might be the cosine floor" -> it is ENTAILMENT. Tier-1-only publishes `true` at both
  0.40 and 0.82; entailment publishes `false` at both. · cost: 0 (fell out of the regime grid)
- Population "0 of 53 codes-only, and an undercount is the safe direction for a zero" -> the bias
  direction was INVERTED; an undercount is exactly what turns a real instance into a reported
  zero. · cost: 1 Phase-2 pass

## Raised by a fresh agent, missed by the author

- [gate] ADR Decision 41's residue is the governing recorded statement about #294 · blocking
- [gate] `AnswerCitations.restsOn` unions UNANCHORED citations into every claim, so the ANSWER
  SHAPE, not the judge, decides the outcome · blocking
- [gate] `eval/grounding-scope/grounding_scope_ab.py` already had the correct per-index wire
  reader; the repo has published a comparable rate before · blocking
- [p2.1] a FOURTH home of the nameless-order SQL (`ActiveOrderAdministrationTermsTest`), after
  the change claimed to have unified three · blocking
- [p2.1] the chart the new test hand-built IS `oneRecordChart()` byte-for-byte
- [p2.1] the denominator: 14 of 18 cells published NO verdict, so 18 was not what "not `false`"
  was a share of · blocking
- [p2.1] README told a client to "expect" a shape no serialized field expresses
- [p2.1] three of four homes I claimed "point at the decision" pointed nowhere — including the
  comment ON the gate a remedy would mutate
- [p2.1] `FixedJudge` discarded its inputs, so "a judge that refuses a claim ABOUT THIS RECORD"
  was untested
- [p2.2] a DROPPED NEGATION made the quotable trade-off bullet assert the opposite of the
  measurement · blocking
- [p2.2] the committed instrument's docstring said "do not hand-roll a second reader" while
  hand-rolling one, with dead residue where the call had been
- [p2.2] the instrument left the SHARED standalone off-stock with no `finally`
- [p2.3] Decision 41's residue is CONTRADICTED for the shape it names, not confirmed
- [p2.3] the regime table's provenance: the committed grid held fixed a question that leaves the
  record uncited
- [p2.3] Decision 25 mentions neither this type nor #118, so the cost was credited to an argument
  it does not make
- [p2.3] "No frequency is stated" falsified by the 0-of-53 in its own paragraph
- [p2.3] the mutation enumeration named 5 of 14 and read as closed
- [efficiency] falsified the brief's own premise with numbers: the Spring context is SHARED, so
  the new suite is 0.47% of the api test phase and hoisting setUp would break rollback

## Where a skill blocked or contradicted this run

- `resolve-ticket` Step 3 says the gate is not a loop and two blocking objections are fine when
  the second SETTLES. Both passes settled; the three-outcome rule worked exactly as written and
  saved a third pass.
- `harden` Termination vs. the prose anti-pattern: three consecutive Phase-2 passes each found
  false claims in prose the previous pass had just written. The skill's own prescription —
  "delete the CLAIM SHAPE" once a second attempt has been refuted — was the thing that ended it,
  but I reached for it late (after the fourth refutation, not the second).
- `harden` forbids `model` on subagents and a hook enforces it. I tried it on a rate-limit retry
  and was refused. Correct refusal; the retry's changed variable should have been the brief only.

## Declined

- Extracting a shared `TestableVerifier`/`FixedJudge` — "if we ship without it, a rename of
  `resolveEmbedder` or `entailsBatch` costs three test edits instead of one, and the next case
  needing a real cosine must write a fourth subclass." Not applied: extraction means restructuring
  a large, heavily-documented sibling test for a maintenance saving, and this copy differs
  deliberately (fixed null; a RECORDING judge the sibling's stub is not).
- Overriding `searchStreaming` in the stub — "if we ship without it, pointing this arrangement at
  the streaming path later means rewriting the stub rather than adding a case." Not applied: the
  measurement is about `search`, and an untested override is code nothing exercises.
- Extracting the drug-side-only SQL clear — "if a fourth name source is added, the helper's
  javadoc promise does not hold for that partial clear." Not applied: it would cover 2 of 3
  variants (the third writes a VALUE), and a partial extraction that looks complete is the trap.
- Running the builder's own WARN sweep for the population figure — "if we ship, 0-of-53 stands on
  a SQL re-expression of `addDrugName`'s predicate, so a reader cannot distinguish 'no such order'
  from 'my predicate missed one'." Not applied; labelled as a bound with its bias direction
  instead.

## Assumptions review overturned

- A1 "the remedy is out of scope because one candidate is #292's" -> #292 is IMPLEMENTED
  (Decision 39); the basis was wrong though the conclusion stood. Replaced with the ticket's own
  reservation sentence. [gate pass 1]
- A3 "how often is not answerable to a frequency by this run" -> a per-cell rate IS available and
  the repo publishes one; what is scarce is the POPULATION. [gate pass 1]
- "No production code changes at all" -> the canonical rationale home is a production javadoc, so
  the diff touches `api/src/main`. Comment-only, but the claim needed correcting. [Phase 1]
