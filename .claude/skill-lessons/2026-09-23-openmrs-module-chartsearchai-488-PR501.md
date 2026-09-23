# resolve-ticket (+ harden, pr-harden) · openmrs-module-chartsearchai · #488 / PR #501 · 2026-09-23
outcome: converged
rounds: 1   cycles: 2 (harden: Phase 2 escalated once)   verifier: skipped (test-only change: fixtures + one test class; no runtime-visible surface)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-488/39ed9650-0c88-46d7-8efe-c265ee75a1aa.jsonl

## Refuted by measurement
- (plan) "re-copying brand_names into 21 fixtures is data-only" -> validity findings identical and no substanceKey changed, but 106 of 132 parsed entries gained aliases; a green build alone would not have shown the alias change (the refuter's point that these cases are self-relative) · cost: 0 (measured before code, via a before/after probe)
- "my javadoc rewrap is clean" -> an Edit that joined two lines, then two rewrap attempts each leaving the tail line long, cascading down the paragraph · cost: 0 rounds (caught by a width check and by a harden agent)

## Raised by a fresh agent, missed by the author
- [refuter] four re-copied fixtures declared schema_version 1.0 while now carrying a 1.3 field · non-blocking · cost: 0
- [harden P2 quality] ddi-substance-name-row.json's note, "value-identical" to another fixture's estradiol rows, was made false by the re-copy · substantive, escalated Phase 2 · cost: 1 harden cycle
- [harden P2 integration] BridgedConceptOrderResolutionTest's NEXIUM_ORDER javadoc, "the fixture carries nowhere", was falsified for the fixture the class javadoc names · polish, treated as a Phase 1 finding · cost: included in the same cycle
- [harden P2b quality] the first corrective clause misattached "which carries rated estradiol rules" · polish · cost: 0
- [r1] the provenance guard does not compare derived_interactions (a probe with an invented Major row passed) · non-blocking -> follow-up #503
- [r1] the PR body omitted four fixtures from its "left off" list, two with provenance claims still untrue · non-blocking -> body re-derived at FINISH, and #503

## Where a skill blocked or contradicted this run
- resolve-ticket:Step 1 — `gh issue view` empty at exit 0 again; the API fallback worked first time.
- gate-state — the `await` syntax cost two attempts: `--only` goes AFTER the subcommand, and a both-gates await needs --run. Cost ~0.
- harden:Phase 2 — "four parallel agents" plus the "only one may mutate" rule: isolated one via worktree and made the others read-only; no contamination.

## Declined
- none (no fixer ran; r1 had 0 blocking findings, and FINISH sent the non-blocking ones to #503)

## Assumptions review overturned
- none
