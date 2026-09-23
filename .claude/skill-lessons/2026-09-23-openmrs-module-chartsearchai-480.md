# resolve-ticket (+ harden, pr-harden) · openmrs-module-chartsearchai · #480 / PR #484 · 2026-09-23
outcome: converged
rounds: 1   cycles: 2 (harden: Phase 1 ×2 passes, Phase 2 ran once, escalated once, re-ran once)   verifier: skipped (no runtime behaviour change; only the derivedFindings GP description text in config.xml, which the build's GlobalPropertyDefaultsTest parses)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-480/edc70c72-4e84-4ac9-8155-e80fe007caa1.jsonl

Deliverable: a MEASUREMENT, not a fix (the skill's "measurement is the deliverable" case). #480 is a checklist; delivered item 1 (precision of the derived tier) with Refs, not Fixes. Model-adjudicated (2 blind agents + tie-break, 6 unmarked controls all correct, 204/206 agreement): strict 0.742 of kept chains, 0.826 of links.

## Refuted by measurement
- plan: "look up each chain's cause note via disease_interactions on (cause id, causeCondition)" -> 158 keys map to >1 note; must use the derived row's own cause_note_id · cost: 0 (gate pass 1)
- plan: "join loader chains back to raw rows on (cause id, ...)" -> DrugReference.getId() is the rxcui when unique, not the DDInter id · cost: 0 (gate pass 2; the join script already translated through the drugs table)
- ADR draft: "the chain-weighted figure sits below per-link BECAUSE the census holds the false links" -> the sample stratum shows the same drop (0.83 links vs 0.74 chains) · cost: 0 (caught in harden Phase 1 by the author)
- PR body: "It does not close #480" -> GitHub parsed "close #480" as a closing keyword; closingIssuesReferences named #480 despite `Refs` · cost: 0 (caught by the Step 8 field check)

## Raised by a fresh agent, missed by the author
- [harden P2 integration] README.md:498, a second README home still said "no precision figure measured" · substantive (escalated Phase 2) · cost: 1 Phase 1 resumption
- [harden P2 reuse] the figure restated in 4 homes outside the ADR, which the guard cannot keep fresh · non-blocking · cost: 0
- [harden P2 quality] substance row mislabelled (link×substance pairs), 0.774 should be 0.775, tautological assert · non-blocking
- [harden P2b reuse] ADR rubric was an abridgement missing two of the agents' rules · non-blocking
- [r1] guard can be greened by editing one JSON number; link check accepts any severity; third mutation not in ADR · non-blocking · filed as #485

## Where a skill blocked or contradicted this run
- resolve-ticket Step 5 (test first) assumes a production change; for a measurement the "failing test" was a guard over the recorded data, red first for the missing file. Worked but not described by the skill.
- gate-state `await` has no per-agent clear; clearing one of four phase-2 awaits clears all.

## Declined
- none

## Assumptions review overturned
- none
