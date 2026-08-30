# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #238 / PR 327 · 2026-08-29
outcome: converged
rounds: 6 (cap raised 4 -> 6)   cycles: 3 (harden, Step 7)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-openmrs-module-chartsearchai/3dd8903f-6bb5-457e-a18e-2505a290be2a.jsonl

## Refuted by measurement
- Plan: "the order-vs-answer half of the defect needs no divergent alias sets, so it is the more reachable half" -> gate pass 1 cited namesNoRoute ("every variant publishes the same aliases") and findForActiveOrders' name leg; claim withdrawn, no count published in its place · cost: 0 rounds (caught at the gate)
- Plan fixture: unqualified row named `Estrone` with aliases ["estrone sulfate"] -> DrugReferenceValidity.sanitizeAliases adds an entry's own display name back on every REAL load, so the premise was true only of the setEntries seam; fixture reshaped so the repair is a strict no-op, and the test now drives the real checkEntries · cost: 0 rounds (gate pass 1, blocking)
- Plan/javadoc: "ruling is untouched" -> gate pass 2 measured addOverdose tries the SUBJECT's band first, so the quoted ceiling moves with the name (4000 mg/day: 2000 -> 3000). Repo's own javadoc settled the direction; trade stated and pinned both ways · cost: 0 rounds
- Javadoc inherited from #206: "Estrone sulfate (topical) publishes estrone and nothing spelled estrone sulfate" -> measured over the shipped KB, both rows publish `estrone` from one rxnorm_name, so that family cannot pose the divergence at all · cost: 1 Phase-2 pass
- Javadoc: "the two maps agree on the relative order of the rows they share" -> reviewer instrumented resolvedSubstanceRows: [B,A] vs [A,B] for the same two rows. The `!rows.contains(ordered)` dedup is the mechanism · cost: 1 round (r5)
- Javadoc: "neither pair arm's subjectOf ever falls through to allGroups" -> false of addInteractionWarnings: on a screening question questionDrugs is empty but inPlay still carries the answer's drugs; chip name moved with the answer, measured · cost: 1 round (r5)

## Raised by a fresh agent, missed by the author
- [r1] main had moved 3 commits; #236 deleted canonicalSubjects, moved all five arms onto the shared lookup, and its ADR entry took the number this branch used · blocking · cost: 1 round
- [r1] #236's ADR recorded as its accepted PRICE the exact residue this change removes -> reconciled, and the removal measured and pinned · blocking · cost: 1 round
- [r1] groupOf's fallback DOES fold answer rows for an answer-only substance, so "answer is no longer read by the naming decision" overclaims · non-blocking
- [r2] the comment block directly above the screening arm's subjectOf calls still described the pre-#238 mechanism — the neighbour closest to the change, missed by nine sweeps · blocking · cost: 1 round
- [r3] three more texts INHERITED from main (untouched by the diff) still asserting the old mechanism, incl. the "alternative is worse" rejection sitting three lines above its own retraction with one ground never retracted · blocking · cost: 1 round
- [r4] a paragraph whose HEADING an earlier round replaced and whose BODY still asserted the old mechanism · blocking · cost: 1 round
- [r4] my own renumber commit claimed a complete sweep; it searched the PHRASING ("issue #238 (ADR Decision 52)") and three sites wrote "Issue" with a capital I · blocking · cost: 1 round
- [r6] nothing — zero findings, loop converged

## Where a skill blocked or contradicted this run
- ENVIRONMENT: the worktree path contained `https:` (created from a ticket URL rather than a number), and javac splits its classpath on `:` — main compiled, every test failed with "cannot find symbol". A colon-free symlink did not help (maven canonicalizes). Fixed with `git worktree move` to the pipeline's own `-<n>` convention. Cost ~3 build cycles before diagnosis. resolve-ticket Step 1's pre-flight checks the standalone but not that the tree can build.
- pr-harden: round 1's fixer died instantly on a session rate limit (429). A retry on a different model succeeded — the model override is the "change something between attempts" that worked, and is not named in the skill's retry contract.
- pr-harden: round 5's fixer stalled (600s watchdog) building a temp fixture to RE-measure something the reviewer had already measured, leaving instrumentation and a temp fixture in the worktree. Retrying with "do not re-measure, edit only" succeeded in 3 minutes. Briefs that ask a fixer to reproduce evidence it was already given are a stall risk.
- pr-harden: the finish verifier yielded mid-task to wait for a server boot notification. Resuming it via SendMessage with "poll in-turn, do not yield" recovered it without losing the work.
- Both merges of origin/main landed an upstream ADR entry on the number this branch was using (49, then 52). Renumbering is now twice-repeated manual work.

## Declined
- none. Rounds 1-6 declined nothing; every finding was implemented.

## Assumptions review overturned
- A4 "the two PAIR arms' residue (canonicalSubjects, #174 site 3 / #189) stays out of scope; the ticket names neither" -> overturned in round 1: #236 had already deleted canonicalSubjects and moved both arms onto the shared lookup, so the assumption's subject no longer existed and both arms are now covered. Recorded in the PR body as overturned rather than dropped.

## Shape worth noting
Five of the six rounds found ONLY stale or self-contradicting prose; no round found a defect in the implementation, which was green and stable from before round 1. The recurring cause was a fixer REWORDING a claim and leaving a neighbour asserting the old one. The round that broke the pattern was the one briefed to DELETE rather than rewrite.
