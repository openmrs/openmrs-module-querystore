# resolve-ticket · openmrs-module-chartsearchai · #491 / PR #495 · 2026-09-23
outcome: converged
rounds: 2   cycles: 1   verifier: skipped (ADR prose only; no runtime-visible change)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-491/56c6d6b1-e091-410b-843c-4e5a03eb4bfd.jsonl

## Refuted by measurement
- ticket: "names-only 0" reproduced -> the #488 harness never PRINTED a names-only arm line (res map entry created only on a fold), so "0" was an absent line; extended harness shows 1,281/1,300 Minor and 26,331/26,606 Moderate admitted rows carry a chip without the fold · cost: one extra 8-min harness run (first run killed)
- ticket's sha 93786563 is not on main (squash merge); 903e5047 is tree-identical and was cited instead · cost: 0

## Raised by a fresh agent, missed by the author
- [gate] harness prints no names-only arm line; a missing line "matches" trivially · non-blocking (adopted)
- [gate] known-bad control was the figure under measurement, not independent · non-blocking (adopted: chip counts)
- [harden P2] #483's finding needs two orders; single-order harness cannot exercise it, so "unchanged" is partly by construction · polish (adopted)
- [harden P2] placement cut "The arm's rows are not a subset" off from 101/3,015 · polish (adopted)
- [r1] "Every figure above" covered the 3,054/60 subgroup/group split and Minor curated-group count the harness never computes · blocking · cost: 1 round
- [r1] "at most one order" overstated coverage; "the #488 test" named nothing findable · non-blocking (implemented)

## Where a skill blocked or contradicted this run
- resolve-ticket Step 1: `gh issue view` exit 0 empty output again; gh api fallback worked
- pr-harden step 1: fetch of pull/495/head right after push returned the old sha (ref not yet updated); caught by the reviewed_shas comparison, re-fetched with '+' · cost: 0
- harden Phase 2: one agent covering four lenses for a one-paragraph diff (deviation stated)

## Declined
- none (harden P2 item 3, "rule chip" vs any interaction chip at the rating, deferred: consistent with the unchanged #479 sentence; changing one alone makes them disagree)

## Assumptions review overturned
- none
