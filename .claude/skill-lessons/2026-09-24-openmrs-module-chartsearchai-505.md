# resolve-ticket · openmrs-module-chartsearchai · #505 / PR #508 · 2026-09-24
outcome: converged
rounds: 2   cycles: 2 (harden: Phase 1 converged, Phase 2 escalated once, re-converged, Phase 2 ran once)   verifier: skipped (only production change is behaviour-neutral nearestIsOwn, re-derived case by case by two fresh reviewers; context tests drive the real LlmInferenceService)
context: no compaction · peak not surfaced (one account re-login mid-run)
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-505/e3da4188-ba15-4f24-b3f0-1ddfca5f75b0.jsonl

## Refuted by measurement
- Plan: "compute the later end with Math.max over the non-null ends" leaves no equivalent mutant -> probe M6 (mineBefore==null arm set to -1) stayed green: once no own name precedes, the old walk can only end false, so that arm was dead; replaced by an early `return false` · cost: 0 (caught by own probe, pre-PR)
- Plan: TAB check = "every line has >=5 fields" -> refuter: cannot fail (every link has >=1 cause drug); replaced by asserting no loaded name contains TAB/LF, with a known-bad control · cost: 0

## Raised by a fresh agent, missed by the author
- [harden P2] ADR "The last two were run with…" misdated after appending the #500 bullet to the dated list · substantive (escalated Phase 2) · cost: 1 harden cycle
- [harden P2] slice-guard message "the loader attaches each such row" overclaims (Major gate, self-pair drop) · non-blocking
- [harden P2] derivedWithin/interactionsWithin duplicate body · non-blocking
- [r1] Math.max arm had no killing case (gap predates PR) · non-blocking · fixed r1 (1 round)
- [r1] mirror-case residue absent from PR body's still-open list · non-blocking · applied in FINISH
- [r2 note] SLICES order test does not pin which three come first · note, in PR body

## Where a skill blocked or contradicted this run
- resolve-ticket Step 3 — collected refuter via notification; the Stop hook fired twice while waiting on background probes (no await recorded for a Bash background task); resolved by a foreground bounded wait loop.
- gate-state `await` without --only requires --run before the harden entry exists; needed `--only pr` at Step 3.

## Declined
- none

## Assumptions review overturned
- none (Refs #505 and the conditional-equality reading of #503 item 1 both confirmed by r1/r2)
# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #505 / PR #529 · 2026-09-24
outcome: converged (pr-harden round 4: 0 blocking); harden ended by labelled override (Phase 2 not re-run after last Phase 1 convergence)
rounds: 4   cycles: 7 (harden)   verifier: skipped (test code, fixture prose and javadoc only — no runtime behaviour)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-505/e3da4188-ba15-4f24-b3f0-1ddfca5f75b0.jsonl

## Refuted by measurement
- Plan: a literal/constant-name source scan finds what a derived-on test loads -> refuted: helper methods (135 uses) were the commonest load path (refutation gate, blocking) · cost: 0 (plan revision)
- "The mirror-case notes need only metadata.note scans" -> three ddi-fold-* fixtures keep a TOP-LEVEL `note` key; missed by every metadata-based sweep until a Phase 2 agent found it · cost: 1 harden cycle
- Harden's regex constant parser -> defeated successively by line-wraps, aliasing, nested classes, modifier order, interface constants, multi-declarators; ended by changing the KIND of question (class-file ConstantValue + mention rule) · cost: ~3 harden cycles

## Raised by a fresh agent, missed by the author
- [gate] helper-method loads invisible to the scan · blocking · cost: 0 rounds
- [harden P2] method refs / nested qualifiers / witnesses / annotated params / FQN; nested-name false RED; base-class @BeforeEach; nested base + interface default; wildcard-bound false RED; ~30 further "verbatim" homes in javadoc/ADR/production javadoc · cost: harden cycles
- [r1] subclass of a derived-on test inherits the tier without naming the key · blocking · cost: 1 round
- [r2] imported nested supertype; alias constant for the GP key; "are not" wording in fold notes · non-blocking (fixed via step-3 exception) · cost: 1 round
- [r3] fixture name inside a larger literal (@CsvSource row) not counted · blocking · cost: 1 round
- [r4 notes] helper that sets the GP for callers (documented residue); refusal predicate unpinned; split-literal in method arg — named in PR body, unfixed

## Where a skill blocked or contradicted this run
- harden:Phase 2 "runs once… escalation re-opens Phase 1" — on a text/shape guard every Phase 2 found one more shape, so the loop escalated 5 times; ended with the labelled override. The KIND-change rule fired late (after ~6 constant shapes) — cost several cycles.
- pr-harden:Step 0 — maintainerCanModify=false on a same-repo PR; not a blocker since isCrossRepository=false.

## Declined
- none

## Assumptions review overturned
- "may only load a listed slice that carries derived_interactions" read strictly (unlisted fixtures refused too) — not overturned.

**Corrected by the 2026-09-25 retro (third pass):** the #505/PR #529 record's `transcript:` line (the
second header in this file) names `e3da4188-…`, the session that wrote the FIRST record (PR #508), and
that transcript never mentions PR #529. The PR #529 record was written by
`~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-505/2463cfcc-5579-4630-9cef-6790a536dcf1.jsonl`
(its :1138), a headless run. It had taken the uuid from `ls <dir>/*.jsonl 2>/dev/null | tail -1` at its
:1129, which lists alphabetically.
