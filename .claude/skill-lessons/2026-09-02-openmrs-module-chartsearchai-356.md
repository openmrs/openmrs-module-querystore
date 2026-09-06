# resolve-ticket + harden + pr-harden · openmrs-module-chartsearchai · #356 / PR 361 · 2026-09-02
outcome: converged
rounds: 1   cycles: 4   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-356/7ab3cb83-d2eb-4ac1-811a-bc2c7d5f0c53.jsonl

## Refuted by measurement
- "`found`/`reported` count the interaction chips this arm emitted" (plan v1) -> on a flattened #118 context one prescription is several partners, and neither pairwise arm has a class leg, so the key would mean two things by question shape; refuted at gate pass 1 and again at pass 2, which named the remedy (count RULE chips only) · cost: 0 rounds, 2 gate passes
- "The extent is answer-independent because it is scoped to the question's substances" -> substance-level only; `resolvedRows` is built over `inPlay`, so an answer-named ROW can widen the group the arm rules over (#175's shape) and change which rule survives · cost: 0 rounds (caught at gate pass 2, recorded as a stated limit)
- "The canonical prescribing question names one drug and so triggers neither pairwise arm" -> a name a clinician reads as one drug resolves to several reference entries on the shipped KB (`dexamethasone` -> 4 rows), which opens the question-pair arm; it owns the field while the drug-in-play chips stay outside its count · cost: 1 harden cycle. Independently reproduced live by the verifier: the SMX/TMP control returns `{0,0}` beside a real Minor chip.
- "The population is rated pairs" (written into 4 homes during a doc sweep) -> `clearsSeverityFloor` exempts an unrated rule rather than demoting it, so unrated curated rules count too · cost: 1 harden cycle

## Raised by a fresh agent, missed by the author
- [harden P2r1] `docs/ddi-interaction-question-examples.md` stated the defect as the contract in five places, including a worked-example cell whose recorded `pairs: null` a tester would now read as a regression · blocking-equivalent · cost: 1 cycle
- [harden P2r1] `CLAUDE.md`'s rewritten bullet contradicted itself in one sentence ("stated by THREE arms" ... "from a local both arms assign") · cost: 1 cycle
- [harden P2r2] The null-causes list lost its "and did not ask to be screened" qualifier in the rewrite — a screening question over an empty chart states `of(0,0)`, not null · cost: 1 cycle
- [harden c3] A mid-paragraph insertion in README put three sentences between "It" and the drug-in-play check it referred to, so the uncapped-arm claim read as being about the capped one · cost: 1 cycle
- [pr r1] The count's placement AFTER the `StatedInteractionChips` collapse was pinned by nothing: `return rules.size()` publishes `found=2` beside one chip with all 1720 api tests green · non-blocking · cost: 0 rounds
- [pr r1] The ticket's own acceptance case could pass without the new arm running — on a two-entry resolution the question-pair arm answers `of(0,0)` and every assertion still holds · non-blocking · cost: 0 rounds

## Where a skill blocked or contradicted this run
- pr-harden:"Editing by script" / the `git checkout --` restore rule — I ran `git checkout -- DrugSafetyValidator.java` to undo a mutation probe while an UNCOMMITTED javadoc fix from the same round sat in that file, and lost it. The skill documents this exactly ("the right restore only where the file carries nothing but the mutation") and I read it before doing it anyway. Caught by grepping for the text I had written. Cost: one re-apply. The rule that would have prevented it is the one immediately above: commit before your own measurement probe.
- resolve-ticket Step 3 / the "collect in the same turn" rule vs. the harness — every delegation had to be kept alive with a chain of backgrounded `sleep` + `TaskOutput` on the *bash* task, reading the agent's report from its completion notification. That works, but the pattern is not written down anywhere; the skill says only "do not yield".
- A phase-2 agent died on a session rate limit (429, opus) at ~04:00; the limit reset at 07:00 and the retry succeeded unchanged. The skill's "retry twice with something changed" has no branch for "the condition is a clock".

## Declined
- Build `questionSubstances` as `substanceRows(questionDrugs).keySet()` instead of the explicit loop — if we ship without this, nothing breaks: `substanceGroupKey()` is the sanctioned identity accessor and the neighbouring line calls it the same way, so the two cannot key differently, while `substanceRows` allocates a map of row lists to discard all but its keys.
- A structural guard over `recordPairExtent`'s single call site — if we ship without this, a future per-arm sink write is still caught by `PairChipExtentContextTest.aPassThatThrewStatesNothingRatherThanACompleteScreen`, whose javadoc records that writing `(0,0)` inside the fail-safe left api and omod green before it existed; the guard would add a source-text case to a context-sensitive class for a mutation already reddened.
- Closing the "medications the KB cannot resolve" hole in `hasActiveMedicationRecords` — if we ship without this, a patient whose only order is a name the dataset does not carry gets `{0,0}` read as a complete screen; declined because the screening arm answers identically on that chart (measured), so it is pre-existing and spans both arms, and it is recorded in ADR Decision 65's trade-offs rather than left to be rediscovered.

## Assumptions overturned
- A1 "found/reported count this arm's interaction chips" -> rule chips only, class-only sentences excluded (gate pass 2)
- A3 "a chart recording no active medication states nothing" -> kept, but the asymmetry with the screening arm's `of(0,0)` on the same chart had to be recorded in the ADR rather than smoothed over (gate pass 2, non-blocking objection)
