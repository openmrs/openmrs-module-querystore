# resolve-ticket (+ harden, pr-harden) · openmrs-module-chartsearchai · #482 / PR #487 · 2026-09-23
outcome: converged
rounds: 2   cycles: 3 (harden)   verifier: ran (works at runtime, ccd3f86a, standalone-8084, no repairs)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-482/73882034-b416-4e66-9674-4d53cf84fb88.jsonl

## Refuted by measurement
- Ticket item 2: "asking namesAnyOf of PatientClinicalContext's active-order names would close" the partial-combination hole -> over the shipped KB's 1629 combination aliases (and later 24 hand-written displays) no constituent is named-but-unresolved by either predicate; the 4 hits are cross-substance false matches; Decision 108's Bactrim names no constituent · cost: 0 (answered at plan time, no code)
- Plan v1: "clause = text back to the last , ; : – —; stated iff that clause names the drug" -> gate pass 1 cited ADR Decision 47's recorded live wording "Nevirapine was prescribed, but its order is no longer in force" (pronoun after a boundary is the TAUGHT form) · cost: one gate re-pass

## Raised by a fresh agent, missed by the author
- [gate p1] clause rule regresses Decision 47's pronoun form · blocking · cost: plan revision
- [harden c1 P2 quality] spaced ASCII hyphen " - " / "--" still reproduces the defect · substantive · cost: 1 harden cycle
- [harden c2 P2 quality] a comma/dose range/parenthesis INSIDE the other drug's clause hands the phrase back to this drug · substantive · cost: 1 harden cycle
- [harden c3 P2] README row / ADR residue lists incomplete vs the fallback · non-blocking
- [r1-1] code attributes by clause while docs say "nearest drug"; plain "and", parenthesis, "whose", U+2010/U+2212 still silent · blocking · cost: 1 round (fixer redesigned to position via namedOccurrences)
- [r2-1, r2-2] phrase-first fallback toward silence; multi-name combination walk unpinned · non-blocking -> #489

## Where a skill blocked or contradicted this run
- resolve-ticket Step 1: `gh issue view` empty at exit 0 again; gh api fallback worked.
- harden: three consecutive Phase 2 passes each found a new silent-direction shape in the same predicate (dash, intra-clause boundary, then position) — "change the KIND of question" (clause -> position) only happened in pr-harden round 1, by the fixer.
- A mutation script's `–` pattern was decoded by the shell before Python saw it (M4 did not apply); caught because the assert fired.

## Declined
- none

## Assumptions review overturned
- "Items 3 and 4 need design/measurement, so out of scope; Refs not Fixes" -> upheld by gate and both review rounds.
- "Clause boundaries decide attribution" -> replaced by nearest-drug-by-position in r1.
