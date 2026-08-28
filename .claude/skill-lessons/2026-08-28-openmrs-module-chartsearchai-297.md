# resolve-ticket (with harden + pr-harden) · openmrs-module-chartsearchai · #297 / PR 325 · 2026-08-28
outcome: converged
rounds: 2   cycles: 5 (harden)   verifier: ran (gate passed — pure-prompt A/B on the 3.7.1 standalone)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-openmrs-module-chartsearchai/d4fb8076-1759-4d22-97c2-d1d4e8081d10.jsonl

## Refuted by measurement
- Plan: "the injector asks DrugSafetyValidator for the fold's answer per record (a second classRelationships walk)" -> refutation gate cited CLAUDE.md #151's "the remedy is not two resolutions that agree"; the name now travels on the SafetyWarning the fold produced. · cost: 0 rounds (caught at the gate)
- Plan: "the prompt's name union cannot grow, because the folded chip is in the prompt" -> true for question-driven records, FALSE as stated for order-driven ones, where no interaction chip stands behind the record at all. Same gate. · cost: 0 rounds
- Plan: "close the class arm's key-asymmetry so the record's name is key-independent" -> implemented twice and abandoned: a name rung over the flattened set reddens DuplicateInteractionChipTest.aRuleOnlyPairIsWordedExactlyAsBefore, and sourcing it from findForActiveOrders reddens DuplicateTherapySelfChipTest too. Replaced by gating the RECORD on per-order structure. · cost: ~1 implementation cycle
- Harden c1: "the coalesce is not needed / getName() cannot be blank" -> the ddinter parser refuses isEmpty() and admits whitespace; the record rendered `Interactions: (Major. ...)`. · cost: 1 Phase-2 pass
- Harden c2-c3: the coalesce's stated reason ("the isBlank guard does not fire") -> false for a rule with NO mechanism note, where it DOES fire and the partner leaves the record entirely — worse than stated. · cost: 2 cycles
- PR r1: "nothing pins the rule-identity conjunct, because no fixture reaches the sibling-row shape" -> only its SECOND job. Its first is scoping a name within one record, reachable on the ticket's own reproducer: weakened, lisinopril's Moderate interaction printed under aspirin's name, suite green. · cost: 1 round
- PR r1: the ORDER rung's justification ("the dataset has no name there") -> false; that rung is reached after soleSubstanceOf resolved an entry, so labelEntry is a real entry named Naproxen. The true reason is that the name there is UNVALIDATED. · cost: 1 round
- PR r1 verifier: "the eval gate's default 20-cell matrix would gate this change" -> structurally blind to it; no cell raises a folded interaction chip, so both arms are the same build in effect. Needed PROBE_PATIENTS/PROBE_DRUGS. · cost: 0 rounds (found inside the verify)

## Raised by a fresh agent, missed by the author
- [harden c1 P2] A blank `labelEntry.getName()` is reachable on operator data and the note then names no partner · would-be-blocking · cost: 1 pass
- [harden c1 P2] The 5-arg SafetyWarning constructor left unreachable with a javadoc still naming a caller · non-blocking · cost: 1 pass
- [harden c1 P2] ADR 49's budget bullet was unmeasured; measured at 14/600 cells flipping full->compact, withheldInteractions provably invariant · non-blocking
- [harden c2 P2] "strictly one-directional" was a property of the 600-cell sample, not the change: 142 of 365 renaming tokens SHORTEN · non-blocking
- [harden c4] A fourth copy of one argument in CLAUDE.md, against that file's own twice-stated rule · non-blocking, resolved by DELETION
- [PR r1] the rule-identity conjunct's real job, and its reachability · BLOCKING · cost: 1 round
- [PR r1] the eval gate unrun with no reason given for the runnable arm · BLOCKING · cost: 1 round
- [PR r2] DdiDrugReferenceSource's self-pair guard javadoc, a home no sweep reached because that file is not in the diff · non-blocking
- [PR r2] a retained parenthetical inside the very paragraph this PR rewrote, contradicting the two sentences bracketing it · non-blocking

## Where a skill blocked or contradicted this run
- resolve-ticket Step 3: the refutation gate returned TWO blocking objections that both SETTLED the question and converged on one design. The skill's three-outcome rule handled it correctly (apply, no third pass) — worth noting because the naive reading is "two blockers = deadlock = abort".
- harden Termination: cycles 2-4 were all corrections to prose written the cycle before — the churn signature the skill warns about. The escape was the skill's own "delete rather than reword" counsel, and cycle 4's only edit was a deletion.
- pr-harden Step 1's sha comparison fired usefully as a no-op (r1 f6a19e9d -> r2 81c99081), confirming the round pushed.
- `gate-state ... reviewed-sha <sha> --only pr` rejects `--only pr` ("unrecognized arguments"); the subcommand works without it. Minor CLI inconsistency with `await`/`clear-await`, which do accept it.

## Declined
- (none by the fixer — every finding in both rounds was implemented)
- Orchestrator deferrals, with failure modes: FoldedClassSentence still carries a parallel partnerName/partnerNoteName pair rather than a ReconciledPartner — if we ship without this, a future third rendering has to be added to two types instead of one. And orderedInteractionNotes reads configuredSeverityFloor() per record rather than once per injection — if we ship without this, a GP flipped mid-injection leaves record [7] on a different floor from record [8]; pre-existing on main, not this ticket's.

## Assumptions review overturned
- A2 (recorded in the plan): "the ORDER rung moves the record's name too, one rule for all rungs" -> reversed before implementation on the refuter's non-blocking objection; the record keeps the token there, and the REASON for it was then corrected twice more (harden, then PR round 1).
- "The eval gate cannot be run on this host" -> it can, via the pure-prompt A/B the README prescribes for this cohort; only the F1/drift gold is unrunnable, and that is confirmed by score_directness rather than assumed.

## Environment note
- The round-1 verifier killed a co-tenant's standalone (pool-slots/standalone-8082) by misattributing its PIDs before checking their cwd, then restarted it and verified 200. One co-tenant request lost. $CLAUDE_PIPELINE_SLOT was set; the brief said to check cwd, and the agent did so only after the first kill.
