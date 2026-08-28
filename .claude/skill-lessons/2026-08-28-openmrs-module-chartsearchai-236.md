# resolve-ticket (+ harden, pr-harden) · openmrs-module-chartsearchai · #236 / PR #324 · 2026-08-28
outcome: converged
rounds: 2 (pr-harden)   cycles: 2 (harden)   verifier: ran (works at runtime; main-vs-PR byte-identical, A/A control byte-identical)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-236/46cd10dc-09df-4537-9749-f64a5f9b796f.jsonl

## Refuted by measurement
- "Put one active order naming a non-canonical row of the probed family on the chart" as the measurement probe -> on the shipped symmetric KB that makes the chart arm OWN the pair, so the question-pair arm never fires: 12 chart-lever probes, 0 chips, before and after. The reachable lever is the ANSWER. Caught at refutation gate pass 2, before any measurement was published. · cost: 1 gate pass
- "13 of 129 multi-row families publish a subset-resolving name" -> 22. The stated predicate did not produce 13; 13 silently required a second, unstated condition. The sharper figure the argument wanted is 10. · cost: 1 Phase 2 pass
- "10 of 22 families elect a different row" as a property of the KB -> it is a property of interactionSubject's SECOND operand: 10 with empty recorded names, 0 recorded on the subset name, 22 recorded on another row. · cost: 1 Phase 2 pass
- "The memo is what makes the one-name guarantee structural" -> the memo is behaviour-neutral; deleting the put leaves the whole api suite green. The shared groups map is what makes it structural. Found independently by two agents. · cost: 1 Phase 2 pass
- "Before #236 this arm's chip was byte-identical across the two validate passes" -> the two passes each call PatientClinicalContextBuilder.build, so the recorded names could already move. True claim is answer-invariance. · cost: 1 Phase 2 pass
- "12 probes reached the arm, name moved on 3" -> does not reproduce from its own stated construction (a looser partner filter gives 16/4). The count is a property of the harness, not the KB. · cost: 1 Phase 2 pass
- "only preAnswerFindings calls renderFinding" -> injectRecords calls it. A round-3 correction that made a vague-but-true sentence sharper and false. · cost: 1 Phase 2 pass

## Raised by a fresh agent, missed by the author
- [harden P2r1] The paired "the injected record carries the chip's detail verbatim" invariant is falsified cross-pass, in THREE homes (reviewer found two) · blocking-grade · cost: 1 pass
- [harden P2r1] "substanceRows over their own list" said of an arm that never calls substanceRows · non-blocking · cost: 1 pass
- [harden P2r4] The guard was credited with enforcing "a caller cannot supply a different group", which it does not check · non-blocking · cost: 1 pass
- [harden P2r5] The documentation had outgrown a 15-line change; four paragraphs deleted, nothing lost · non-blocking · cost: 1 pass
- [pr-harden r1] **Bypass of the new structural guard**: re-constructing SubstanceSubjects over an arm's own row group is the deleted canonicalSubjects, needs no interactionSubject call, and passed the guard AND the whole suite. On the screening arm nothing caught it. · BLOCKING · cost: 1 round
- [pr-harden r2] Second bypass: a method reference (DrugSafetyValidator::interactionSubject) walks past a paren-anchored needle · non-blocking · cost: 1 round
- [pr-harden r2] "the group differs by exactly one input" kept a conclusion whose premise an earlier round had deleted · non-blocking · cost: 1 round

## Where a skill blocked or contradicted this run
- resolve-ticket Step 1: `gh issue view` returns EMPTY (exit 0) on this machine; `gh api repos/<o>/<r>/issues/<n>` works. Every agent brief had to carry the workaround. Worth putting in the skill.
- harden Termination vs resolve-ticket Step 9: harden's confirming-cycle rule ran 5 Phase 2 passes over a 15-line logic change, then pr-harden reviewed the same slice twice more. The two blocking-grade guard bypasses were found by pr-harden, not by harden — evidence the clean-context loop is where the value is, and that harden's prose-correction spiral (4 rounds of correcting its own previous round's prose) has a cost the skill names but does not bound.

## Declined
- (none — every finding in both loops was implemented)

## Assumptions review overturned
- "The screening arm's divergence is unreachable, so the change there is behaviour-neutral" -> upheld for chips, but the WARN withheld-pair label can move on a one-directional curated dataset; and the arm-handoff residue (pre-answer finding from the screening arm, post-answer chip from addInteractionWarnings) is pre-existing, not this change's. Round: harden P2r1/r2.
- "The question-pair arm keeps its pass-stability" -> it does not; it loses answer-invariance, measured live. That became ADR Decision 49's "What it costs". Round: harden P1r1.
