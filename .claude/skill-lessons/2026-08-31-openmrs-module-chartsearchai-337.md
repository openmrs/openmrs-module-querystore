# resolve-ticket + harden + pr-harden · openmrs-module-chartsearchai · #337 / PR 345 · 2026-08-31
outcome: converged (pr-harden round 2, 0 blocking) · harden overridden after cycle 1
rounds: 2   cycles: 1 (harden: Phase 1 x4, Phase 2 x5)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-337/4b68d0d7-2144-40a0-9cdc-1353ce4f6ca9.jsonl

## Refuted by measurement
- "logging the record's own continuation is safe, a reference record carries no patient data" -> a safety_finding detail embeds the patient's prescription display and a drug_reference record carries the "Recorded for this patient:" sections; the WARN now logs offsets only · cost: caught at the refutation gate, 0 rounds
- "a public reproducibleText accessor is needed to strip renderFinding's appended clauses" -> endSentence already guarantees the clause opens a new record sentence, so the exit covers it; accessor deleted · cost: caught at the gate, 0 rounds
- "the gap question can be SENTENCE_BOUNDARY, and a misread boundary can only cost recall" -> false in the other direction: a quotation closed with `."` before a new sentence was reported as a substitution it never made · cost: 1 harden Phase 2 pass
- "SENTENCE_TERMINATORS narrowed to '.' would redden the case that pins it" -> the case built its expected set FROM that constant, so it iterated one ending and passed · cost: 1 harden Phase 2 pass
- "two ClassCodeFidelityTest cases fail on a floor of nine" -> five, after the branch merged main and #338 brought seven more package-scoped silence cases into that file; stated in four homes · cost: 1 pr-harden round
- "the pooling keeps the shipped arrangement quiet" -> after the gap-predicate fix the reference record explains its own continuation; the case pinned the record-sentence exit instead · cost: 1 harden Phase 2 pass
- unquoted interpolation of the terminator set "fails silently with a green build" -> it reddens fifteen-plus tests; figure then went stale in the commit that wrote it · cost: 2 harden Phase 2 passes

## Raised by a fresh agent, missed by the author
- [harden p2] the check reported a faithful quotation closed with `."` — a real false positive · blocking · cost: 1 pass
- [harden p2] six silencing legs unpinned; mutating each left the whole suite green · blocking · cost: 1 pass
- [harden p2] a javadoc asserting the two new checks cover the chart-citation class, contradicting README and ADR 41 in the same change · blocking · cost: 1 pass
- [harden p2] a NINTH home of the false chips claim, in a line comment; the sweep had scoped to javadoc because the enumeration said "in its javadoc" · blocking · cost: 1 pass
- [harden p2] three more unpinned behaviours: the case fold, the floor from below, the gap rule's line-break arm · blocking · cost: 1 pass
- [pr-harden r1] both blocking findings were counts the main merge falsified, in four homes (reviewer named three) · blocking · cost: 1 round
- [verifier] the probe's word counts are phrasing-sensitive and byte-reproducible per phrasing, so the ADR table is one capture rather than an invariant · non-blocking

## Where a skill blocked or contradicted this run
- pr-harden:step 1 — "compare the base you just fetched against the one the previous round saw, and re-check any identifier allocated from a sequence main also appends to" caught an ADR-number collision (upstream had taken 59) BEFORE round 1 spawned. Cost 0; without it the number collides silently in six homes.
- harden:Termination — five Phase 2 passes each found something real, so the gate never released; the run took the labelled override after cycle 1 rather than spending a full confirming cycle ahead of pr-harden, which is a stronger gate.
- A mutation that fails to APPLY reads as a pass: a perl escaping slip left the line unchanged and the check reported green. Verify the mutation landed (grep the line) before believing a zero result. Cost: one wasted verification round.

## Declined
- Widen `isReferenceMaterial` (ChartSearchAiUtils' private boolean) rather than asking `referenceGroup` directly — CLAUDE.md prescribes the direct comparison for any question that is not about grading; borrowing a named view couples the caller to what that view is for.
- A per-call vocabulary filter skipping the DP where a record shares no 12-word run — measured at ~0.4 ms on a realistic chart and 1.2 ms at a synthetic 20-record tail; a filter that is wrong SILENCES the check, which is the fail-closed direction, and 0.4 ms does not buy that risk. Recorded in ADR 61 as available later on evidence this check produces.

## Assumptions review overturned
- "the fidelity check should be scoped to safety_finding" -> scoped to the reference GROUP via `referenceGroup`, per CLAUDE.md, so a later reference type is covered without this class changing (gate pass 1)
- "'log/flag divergence' might mean surfacing it" -> log only; #201 withholds every reference-group verdict at the wire and #142 left surfacing deliberately open (gate pass 1)
