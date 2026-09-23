# resolve-ticket (+harden, pr-harden) · openmrs-module-chartsearchai · #469 / PR #470 · 2026-09-23
outcome: converged (pr-harden round 2: 0 blocking); harden took the labelled override after its Phase 2 escalated ten times
rounds: 2   cycles: 11 (harden)   verifier: ran (works at runtime, at 9d1fb03d, loaded-class hash matched)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-469/de9bcc8a-a30b-443b-900d-9267fcbec25c.jsonl

## Refuted by measurement
- "a withholding finding makes 'No' true whatever else the module did" -> false for contraindications (opium/Tiotropium flagged; egg/Eggplant unflagged; gentamicin "dose adjustment required" note) and for unrated rules / class folds onto Minor rows · cost: 3 harden cycles
- "a closed word LIST recognises a proposal/screen" (twice: refusing list, then admitting list) -> defeated each time by questions built of admitted words in another order ("…, and is she allergic?", "Does this drug interact with her medications?"); ended only when replaced by a GRAMMAR of question shapes · cost: 3 harden cycles
- "removing every word of every alias removes the drug's name" -> brand/combination aliases (Aleve Arthritis Pain, Ibuprofen / levomenthol) strip purpose clauses and second drugs; fixed with DrugReference.namedOccurrences spans · cost: 1 cycle
- "every active order resolved" guarantees "not already taking" -> a combination filed under one constituent (Bactrim → trimethoprim) passes; left as a named residue · cost: 1 cycle
- "the strength sort within a group is load-bearing" -> arm order already yields it; kept and labelled unobservable
- the pass-1 refutation gate's revised plan (screen-note answer, caution "can be given" lead) -> both clearances later found false by review (unread allergy list, class/brand allergen, switched-off arm) · cost: 2 cycles

## Raised by a fresh agent, missed by the author
- [harden P2, ×10 escalations] each Phase 2 found one substantive new gate hole (unknown-drug screens, wh-questions, stronger current-med finding leading, composing with GP off, allergy-only screens, arms off, history questions as screens, alias stripping, "which drugs" screens, clearance beside unreadable allergies, uncorroborated/egg/dose-adjustment contraindications, the "should not be given" directive on deliberate combinations, unrated rules, unresolved brand orders, partial combination resolution) · blocking-equivalent · cost: ~10 cycles
- [r1] the rating conjunct was pinned by a test that was refused earlier by the order-resolution gate (curated service resolved none of her orders) · blocking · cost: 1 round
- [r2] the first reason under the "No" is a contraindication, not the licensing interaction; an order-driven allergy line loses "she is prescribed it" · non-blocking · to follow-up (not filed)

## Where a skill blocked or contradicted this run
- harden:Termination — the convergence rule has no bound for a design whose failure space is open-ended (question semantics × dataset semantics); ten consecutive escalations each found one real hole; the run took the labelled override and let pr-harden's fresh rounds continue
- harden:Phase 2 — orphan-javadoc insertions happened three more times; the awk sweep caught each before commit

## Declined
- r1-3 (order-record numbers independent of citeOrderRecords) — if we ship without it, a composed interaction answer cites the finding and not her order record; measured that neither proposed remedy produces that citation (orderRecordNumbers only numbers bridge items); real fix is new provenance for interaction findings

## Assumptions review overturned
- A3 (answer caution-only proposals with a "can be given" lead; answer empty screens with the note) -> removed: the module states only positive findings (harden cycle 4)
- A4 lead "No — X should not be given: …" -> "No — this module's drug-safety check found a reason to withhold X." (harden cycle 8)
- contraindications license the "No" -> only a RATED (Moderate+) interaction does (harden cycles 7, 9)
