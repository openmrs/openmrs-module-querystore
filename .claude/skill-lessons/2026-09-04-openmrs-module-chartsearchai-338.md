# resolve-ticket · openmrs-module-chartsearchai · #338 / PR 376 · 2026-09-04
outcome: converged
rounds: 4   cycles: 14   verifier: skipped (no production code changed; no round could touch runtime behaviour)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-338/0bcdfdef-5615-4248-aa18-ba0b31183932.jsonl

## Refuted by measurement
- "Publish a count of injected safety findings vs how many the answer cites, as the remedy for #338's defect 4" -> ADR Decision 59 (written FOR this issue, shipped as PR #343) records exactly that as Rejected, on Decision 35's ground that "the chips are what the answer was expected to report, not what it was licensed to state". The plan never mentioned Decision 59, 35 or the shipped PR. · cost: gate pass 1, before any code
- "#337's ReferenceProseFidelityCheck closes defect 2, so record a pointer" -> measured through the production predicate: it is SILENT on #338's own captured answer (defect 1 cuts every run below the floor) and it REPORTS answers whose drug name is correct. Wrong in both directions. · cost: gate pass 2 + one measurement, before any code
- "The false-positive measurement an aligned prefix rule needs cannot be taken in this repository" -> the drift-metric captures carry each answer's safetyWarnings, whose drug and detail are the operands renderFinding composes a safety_finding record from, so the record sentence is reconstructible; 16 of 42 captured answers reproduce one to the floor. · cost: round 1
- "The corpus is unlabelled — the drift metric's own perturbed answers" -> its own PROVENANCE.md says everything not marked CONSTRUCTED/COUNTERFACTUAL is a verbatim live capture, and two of the nine reported answers are live ones, including the shipped-clean control whose contract is that nothing in it may be flagged. Characterised from the shape of the tree instead of reading the file. · cost: round 2
- "Which gate the check declines at under which treatment of the capture's markers" -> five successive harden cycles each refuted the sentence the cycle before had rewritten. Ended by deleting the claim shape, not by rewording it again. · cost: 5 cycles

## Raised by a fresh agent, missed by the author
- [c2] the measurement was taken over a ONE-record arrangement while the capture cites four findings stating the same code; the check pools across cited records · blocking · cost: 1 cycle
- [c3] gutting findingSentence to return the whole record left both cases green — the operand they slice was unpinned · blocking · cost: 1 cycle
- [c3] editing an ATC code in the new fixture left both cases green; they build their answers out of the record, so they are self-relative · blocking · cost: 1 cycle
- [c5] the whole six-clause capture reaches a DIFFERENT gate than its opening clause; the published mechanism was the excerpt's · blocking · cost: 1 cycle
- [c5] the transcribed capture constant could have its allergen spelled correctly and its code stated twice with every case still green · blocking · cost: 1 cycle
- [c8] the previous cycle's own correction stated the two defects the wrong way round · blocking · cost: 1 cycle
- [c12] an inserted helper took over its neighbour's javadoc — the known recurring defect, recurring · blocking · cost: 1 cycle
- [r1] "cannot be measured in this repository" refuted with a 16-of-42 measurement · blocking · cost: 1 round
- [r2] the corpus's PROVENANCE.md refutes the characterisation; the shipped-clean control is itself flagged on a real `plateet`/`platelet` divergence · blocking · cost: 1 round
- [r3] "unlike every other case in this file" — three other cases are hand-written literals, one cited against a null-text record · blocking · cost: 1 round

## Where a skill blocked or contradicted this run
- resolve-ticket:Step 3 — the refutation gate refuted TWO successive plans on citations that settled them, and the second left no replacement within scope. The skill's three outcomes cover "settles" and "leaves open" but not "settles negatively, and the ticket then has no accepted code change" — which is what happened, and what made this a measurement deliverable.
- harden:Termination — ran to 14 cycles. The prescribed remedy for the recurring pattern ("delete the CLAIM SHAPE" once a second attempt at a claim of some kind is refuted) was the thing that worked, and applying it at cycle 6 rather than cycle 11 would have saved roughly five cycles. The signal to apply it earlier is available: two refuted attempts at the same KIND of claim.
- pr-harden:Step 4 — the fixer was spawned with `isolation: "worktree"` (to satisfy the skill's own "do not edit the worktree while an agent runs"), which puts its edits in a worktree the orchestrator must then patch across. The skill's FIX step assumes the shared worktree and says nothing about this; `git -C <agent worktree> diff | git apply` worked every round but is undocumented.

## Declined
- (none — every finding raised by every round was implemented)

## Assumptions review overturned
- "The ticket's remaining scope is item 4, and a deterministic statement of dropped findings is the fix" -> refuted at the gate by Decision 59; the deliverable became a measurement of whether the check that landed since closes item 2 · gate pass 1
- "#337's check covers item 2's shape, so the record is a one-line pointer" -> refuted by measurement; the record became a nine-row table and three readings · gate pass 2

# pr-harden · openmrs-module-chartsearchai · PR 376 · 2026-09-04
outcome: converged
rounds: 4   verifier: skipped (no production code in the diff)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-338/0bcdfdef-5615-4248-aa18-ba0b31183932.jsonl

Rounds 1-3 each raised exactly one blocking finding and each was a DIFFERENT defect, shrinking round
on round: a false claim about what a corpus contains, a false claim about how that corpus is labelled,
a false exhaustive claim in test javadoc. Round 4 returned `"findings": []` having independently
re-derived every published figure. Nothing was declined in any round. Every round's blocking finding
was a claim in prose that no test could fail on — which is what the fourteen preceding harden cycles
had also mostly been finding, and is the signal that this change's risk surface was its record rather
than its code.
