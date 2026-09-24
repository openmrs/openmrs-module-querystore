# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #516 / PR #522 · 2026-09-24
outcome: converged
rounds: 2   cycles: 2 (harden: Phase 1 converged, Phase 2 escalated once, re-converged, Phase 2 done)   verifier: ran (works at runtime, on e72b69c4)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-516/df7f369c-a15b-4a24-ba6c-0e83246137ec.jsonl

## Refuted by measurement
- Plan: the spacing case (issue case 4) can be arranged over the SHIPPED KB with a name-only order "Isoniazid / pyrazinamide / rifampin" and "Is it safe to give Amlodipine?" -> the chip names the KB row "Rifampicin (rifampin)", not the display (the live demo used coded orders); switched to the #477 fixture's already-in-several-orders finding, which names displays · cost: one test rewrite, no round
- First dedup arrangement (#477 fixture, rifampicin question) was assumed to name one order twice -> its four findings name four distinct orders; a two-drug question over one simvastatin order did · cost: one test rewrite
- The full build after implementation: 5 failures in FindingEnumerationRepairTest / SafetyFindingSeverityFidelityTest — harnesses whose stubbed post-answer validator returned no chips had kept the completion inert; reading the records made it fire (correctly). Not foreseen by the plan or either gate pass · cost: arrangement change in two files

## Raised by a fresh agent, missed by the author
- [gate 1] root CLAUDE.md had 11 bytes of headroom; planned directive would overflow ProjectInstructionsGuardTest · blocking · cost: 0 rounds (resolved in place at 24999 bytes)
- [gate 1] validator stub must return today's chips or the new cases pass on main; answers must be built from stamped names · non-blocking · cost: 0
- [gate 2] the spacing case must be shown red with decisions 1–3 applied and 5 absent · non-blocking · cost: 0
- [harden P2] "Not shared with measure" javadoc became false once both shared citedFindings/comparable · substantive (escalated Phase 2) · cost: 1 extra Phase 1 + Phase 2
- [harden P2] canonical FindingPartnerCoverage 'stated' paragraph left stale on slash spacing while README/ADR were updated · non-blocking · cost: 0
- [harden P2b] containment residue runs BOTH ways (Lamivudine inside Lamivudine / zidovudine reads stated), contradicting "the residue runs toward reporting" · non-blocking · cost: 0
- [r1] the new source guard passes `citedFindingIndexes(null, cited, mappings)`, which reads the #409 union; no case had a non-empty structured citations array · blocking · cost: 1 round
- [verifier] an answer writing "Lopinavir / ritonavir" without strength still gets the full display appended — the spelling residue, live · note · cost: 0 (named in PR body)

## Where a skill blocked or contradicted this run
- resolve-ticket Step 3 says re-run the gate once after a settling blocking objection; the objection (byte budget) was fully settled by its citation — the second pass cost ~6 min and returned no blocking objection.
- harden: Phase 2 escalation on a one-sentence false javadoc bought a whole extra Phase 1 + a second 4-agent Phase 2; the second Phase 2 found only polish.

## Declined
- none

## Assumptions review overturned
- "Moving the two completion cases to api.impl satisfies decision 4" -> accepted by r1 and r2 reviewers (not overturned)
