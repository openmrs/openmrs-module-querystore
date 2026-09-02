# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #354 / PR 365 · 2026-09-02
outcome: converged (round 4 reported 0 blocking findings)
rounds: 4   cycles: 7 (harden, ended on labelled override)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-354/9e497e7b-0a74-4364-8d93-39c150c0aa94.jsonl

## Refuted by measurement
- "every substance above sits under G03A*" (the ticket's own proposed mechanism) -> Ethinylestradiol is filed
  G03CA01/L02AA03, outside G03A, while G03A holds Megestrol acetate. Measured before any code was written;
  it is what chose outcome 2 over outcome 1. · cost: 0 rounds
- "no interaction screen was run for this class" (note text) -> the pairwise arm is gated on the same
  questionDrugs.isEmpty(), so a screening question fires both and the note denied a Major finding one record
  earlier. · cost: 1 harden pass
- "this response carries no reference material for it" -> that Major finding IS reference material by
  referenceGroup, and its prose names the class. · cost: 1 harden pass
- "an interaction screen runs against a named substance, not a class" -> classRelationships screens on a shared
  cross-reactivity group and names it. · cost: 1 harden pass
- "the interaction reference DATA is indexed by individual substance" -> cross-reactivity-groups.json is
  reference data this module loads, keyed by class name, and is where the printed class name came from.
  · cost: 2 harden cycles (the record, then six sites describing it)
- "a hardcode inside referenceGroup/isGroundingDemoteOnly/referenceSlice survives the whole build" (CLAUDE.md,
  pre-existing) -> this change added the THIRD reference-group type those guards were waiting for. Three
  successive attempts to write down which site reddens which test were each falsified by the next
  measurement; it now publishes no mapping. · cost: 3 harden cycles
- "a reference-group record on a question raising no chip at all is new" -> a plain dose question already
  injects a drug_reference and raises none. · cost: 1 harden cycle
- "no behavioural test can catch a hardcoded pair in groundedForWire" -> five behavioural cases redden on a
  drug_class_note citation. My own correction to this was itself false first. · cost: 1 harden cycle

## Raised by a fresh agent, missed by the author
- [r1] The record delivered only HALF the outcome #354 blesses — the ticket asks for "says it named a class AND
  asks for a specific drug"; ADR quoted that outcome verbatim and then shipped only the first half. · blocking · cost: 1 round
- [r1] The names-no-substance guard bound the term KEYS and one hardcoded rendering, never the class NAME the
  note prints. Demonstrated with a term whose value column is a substance: ships green, names the drug in
  citable evidence, and silently removes its chips through #363's echo corpus. · blocking · cost: 1 round
- [r2] The whole change was prompt-facing, and the live model did not relay it — so a /search consumer saw
  byte-identical output. The reviewer found the sibling precedent (#356/#361) that answered the same problem
  with a deterministic wire key. This is the finding that made the change actually deliver. · blocking · cost: 1 round
- [r3] The new key's client contract told clients to render two claims this very change had measured false,
  contradicting the same README 96 lines earlier. · blocking · cost: 1 round
- [r3] The note is gated on the question resolving nothing, so a question naming a class AND a drug still
  reports nothing about the class. · non-blocking
- [r3] On an inert dataset the note's first clause is a non-sequitur. · non-blocking

## Where a skill blocked or contradicted this run
- pr-harden:"Compare that sha against the last entry of reviewed_shas" — the ADR decision-number collision it
  warns about fired TWICE in one run (main took 65, then 66) and the base moved three times. Both were caught
  by the base-comparison step; without it the renumbering would have shipped as a duplicate heading.
- harden:Termination — cycles 3-7 each found only documentation stragglers of three claim families, one per
  cycle, each reached by a search token the previous sweep had not used. Ended on the labelled override after
  cycle 7. The escape valve was the right shape; what it cost was five cycles to learn the families were
  citation graphs, not wordings.
- Two agents died on an account session rate limit (429) mid-run; both completed on retry after the reset, one
  on a smaller model.

## Declined
- (none — all seven review findings across four rounds were implemented)

## Assumptions review overturned
- "the note stating the class is enough for #354's second blessed outcome" -> it is not; the outcome's own
  wording includes asking for a specific drug, and a prompt-only statement reaches no client at all. Round 1
  supplied the first half, round 2 the second.
- "prose in a doc is cheaper to get right than code" -> inverted here. Every behavioural defect was found by
  round 2; rounds 3-7 of harden and round 3 of the loop were all false claims in prose, several of them
  corrections of previous corrections.
