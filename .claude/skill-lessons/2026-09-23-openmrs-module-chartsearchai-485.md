# resolve-ticket (+harden, pr-harden) · openmrs-module-chartsearchai · #485 / PR #493 · 2026-09-23
outcome: converged
rounds: 2   cycles: 2 (harden-485a; Phase 2 escalated once)   verifier: skipped (test/docs-only change, no round runtime-visible)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-485/6ad015df-34c7-4a56-8b50-032e518f0ca7.jsonl

## Refuted by measurement
- (none of the plan's claims; the committed join reproduced every recorded figure of the #484 sample on the first green run)
- self-review: ADR bullet "beside the two population tests" for the duplicate-row mutation -> the log showed one weights test + one population test · cost: 0 (caught before commit)

## Raised by a fresh agent, missed by the author
- [harden P2 quality] an item without `keptChains` skipped its weight check (field presence, not group, decided it); mutation proved green · substantive · cost: 1 harden cycle
- [harden P2 efficiency] substanceGroupKey()'s entry fallback pinned loaded DrugReference graphs in a static map, and my comment claiming "keeping only ids" was false · polish · cost: 0
- [harden P2 integration] unresolvedDrugs false-reds a legitimately loader-dropped drug row; NumberFormatException instead of the re-measure message on a non-numeric note id · polish · cost: 0
- [r1] cause drug never checked against recorded causeDrugs: re-pointing all rows of an adjudicated link to another drug kept every check green · blocking · cost: 1 round
- [r2] whole-population residue (unadjudicated links, the Random(480) draw) — filed #496 · non-blocking
- [r2] PR description stale after r1 (test count, cause-drug check) · non-blocking, fixed by FINISH's re-derive

## Where a skill blocked or contradicted this run
- resolve-ticket Step 1: `gh issue view` empty at exit 0 again; `gh api` fallback worked.
- gate-state: `--only` is a subcommand option, not global (`gate-state --only pr await` fails with "invalid choice: 'pr'") · cost: one call.
- CLAUDE.md user rule points at `git-restore-backup.sh` as if it were a command; it is a PreToolUse hook (~/.claude/hooks), so it cannot back up a restore inside a script — used cp-aside + cmp in the probe runner instead · cost: one lookup.
- I edited the orphan-comment nit on the branch WHILE the read-only refutation agent ran; it then reported the nit "already fixed" (non-blocking objection). Harmless here, but it is the "do not edit while a delegated agent runs" hazard.

## Declined
- [harden P2b integration] credit duplicate-key raw rows one chain each instead of failing — if we ship without it, a refresh adding two rows differing only in note id reddens the join test, which is correct: it breaks the premise the measurement was taken under, so the figure must be re-measured anyway (message now says re-measure).
- [harden P2] committed dump of the ranked enumeration — outside the ticket; filed in #496.

## Assumptions review overturned
- "controls held to kept-population membership only" -> r1 extended controls to the cause-drug check too (round 1)
