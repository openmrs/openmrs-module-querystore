# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #476 / PR #481 · 2026-09-23
outcome: converged
rounds: 1   cycles: 1 (harden: Phase 1 4 passes, Phase 2 once, 4 agents)   verifier: ran (works at runtime, at FINISH on c1b2b7de)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-476/718330b6-8c9a-455e-a88d-6d00323b5a6e.jsonl

## Refuted by measurement
- Plan: condition 1 alone "is #196's measured 276 divergent / 23 synonyms population" -> misquote; 276 = genericName rows, the containsWord population is ~221 (refuter, exploratory; r1 reviewer measured 221 through real code) · cost: 0 (removed before code)
- Plan: Omeprazole/Hyoscyamine rxnorm_names are rows "the module deliberately relies on" -> the repo records them as defects already reported by alias-names-another-substance · cost: 0
- Fixture design: a Rifampicin control would isolate the not-filed-on-this-row clause (refuter's suggestion) -> it could not; only Rifampicin carries "Rifampicin", so it fails the other-row half anyway; used the Azelaic acid pair instead · cost: 0
- Assumed every conjunct was pinned by the verbatim slice -> the stem-carries-substance conjunct changed nothing on the shipped KB (measured); needed a hand-authored edge fixture · cost: 0 (caught by own mutation pass)

## Raised by a fresh agent, missed by the author
- [refuter] two stale statements the rule falsified (derivative javadoc "undetectable from inside the file", redistribution test "two content rules on 19 rows") · non-blocking · cost: 0
- [harden p2 quality] "#476 records the remedy" claimed for rows #476 never names · non-blocking · cost: 0
- [r1] rule silently misses Monopotassium phosphate (#196's other wrong rxnorm_name) and Iodide I-131 — different spelling, not a qualifier; javadoc understated the residue · non-blocking · cost: 0 (to follow-up, named in PR body)
- [r1] the display-STEM choice on the row side is unpinned (normalizeName substitution stays green) · non-blocking · cost: 0 (follow-up)

## Where a skill blocked or contradicted this run
- gate-state: `await` without --only needs --run even in resolve-ticket's pre-harden phase; `--only pr` must go after the subcommand — two failed calls
- pr-harden FINISH says non-blocking findings go "to a follow-up issue"; resolve-ticket Reporting says nothing is posted but commits. Resolved by listing them in the report and PR body, not filing an issue.
- The "19 rows / two rules" count lived in 7 homes (README x2, ADR x2, 3 javadocs/test javadocs); phrase-grep found 5, a subject sweep of the README findings bullet found the 6th/7th in harden pass 2.

## Declined
- (none in pr-harden) harden Phase 2 optional: swap `own` set for DrugReference.bridgedConceptName — if we ship without it nothing breaks, behaviour identical since setBridgedConcepts drops null uuids.

## Assumptions review overturned
- none
