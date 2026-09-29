# resolve-ticket · openmrs-module-chartsearchai · #553 / PR #559 · 2026-09-29
outcome: converged
rounds: 5 (default cap 4 raised to 5, stated)   cycles: 3 (harden)   verifier: ran (works at runtime, r1 f3e575f9 and finish d33a13d6)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-553/1840b2b4-1192-4c87-b45f-231f7d7739fb.jsonl

## Refuted by measurement
- Plan rev 2 (refuter round 2): "the current-medication referent requires a started order at EVERY arm; Decision 72's defect does not transfer" -> harden Phase 2 integration agent showed a module-composed screening answer then opens "No — … withhold Rifampicin" (Decision 72's defect); order-driven arms reverted to current referent + started-side visiting order · cost: 1 harden escalation
- "the stand-in / collapse / subject-clause / carriers branches are covered" -> mutation probes stayed green; 7 tests added in harden pass 1 · cost: 1 harden pass

## Raised by a fresh agent, missed by the author
- [refute1] class sentence "as active order X", FindingPartnerCoverageCheck append, ConflictingOrderStatement, pair-arm referent flip · blocking · cost: plan revision
- [harden-p2] "No —" withhold lead on screening after referent flip · substantive · cost: 1 escalation
- [harden-p2b] contraindication chip carries no date; README aboutACurrentMedication contract · substantive · cost: 1 escalation
- [r1] contraindication detail states no date / scheduled wording · blocking · 1 round
- [r1] listed (not proposed) scheduled drug flipped to proposal referent · non-blocking
- [r2] module-composed answer adds "already taking" after "has not started" · blocking · 1 round
- [r3] duplicate-therapy sentence ("active orders A and B") names scheduled carrier active · blocking · 1 round
- [r4] coverage append words duplicate-therapy partners from one per-finding date → "active order" for the scheduled one · blocking · 1 round (the seam between r1 and r3 fixes)

## Where a skill blocked or contradicted this run
- resolve-ticket Step 3 refutation: round-1 refuter's obj1 ("proposal referent on screening = #348 defect") and round-2 refuter's obj2 ("require started at every arm") contradicted each other; I adopted round 2 as "settling", and harden Phase 2 then measured round 1 right. Cost: one escalation cycle.
- Instruction files at byte budgets (CLAUDE.md 24,977/25,000, reference 76,494/76,500): no room for a rule bullet; trimmed neighbouring filler words to fit a qualifier. Fixers later did the same.
- harden Phase 2 rounds 2–3 used two agents covering the four lenses in pairs, not four (deviation, stated).
- pr-harden cap: default 4 raised to 5 on "different defect each round" signal; round 5 clean.

## Declined
- r2-2 UTC date formatting — if shipped, a local-midnight scheduledDate on a server east of UTC states the start one day early, as every other published date already does.

## Assumptions review overturned
- "Referent withdrawn at every arm for a scheduled subject" -> order-driven arms keep current referent (harden cycle 2); drug-in-play arm only for a PROPOSED drug, not a LISTED one (round 1)
- "Contraindication about a scheduled order is a residue" -> detail states "Her order for X has not started" (round 1)
- "Duplicate-therapy sentence unchanged (residue)" -> names scheduled carriers as scheduled (round 3)
