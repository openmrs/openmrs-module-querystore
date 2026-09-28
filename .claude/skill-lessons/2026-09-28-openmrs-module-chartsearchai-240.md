# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #240 / PR #549 · 2026-09-28
outcome: converged
rounds: 3   cycles: 2 (harden)   verifier: skipped (no module code changed; substitute = live GET-only runs of the harness on :8081 at r2 and r3 heads)
context: no compaction · peak not surfaced
transcript: /Users/danielkayiwa/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-240/65754a09-1a54-4b28-859b-22bdf56912fc.jsonl

## Refuted by measurement
- ticket: "Sarah Taylor, the patient it appears to intend, is dc8560c9 here" -> SQL: dc8560c9 has no malnutrition condition/diagnosis; no dev patient carries the harness's described shape; uuid not swapped · cost: 0
- ticket: "it leaves a GP flipped" -> refuter: the finally restores on an HTTPError; the harm is mutation-before-validation, best-effort restore · cost: 0

## Raised by a fresh agent, missed by the author
- [harden P2] every HTTPError reported as a missing patient (401 would send operator after a new cohort) · non-blocking · cost: 1 harden cycle
- [harden P2] selftest 404'd only 2 of 3 patients; skip-last mutation passed · substantive · cost: 1 harden cycle
- [harden P2 c2] mistyped argument fell through to the live run · non-blocking · cost: 0
- [r1] selftest never exercised the partial cohort (only the condition patient absent) — the dev box's real state; whole-cohort-only refusal passed · blocking · cost: 1 round
- [r1] empty override inconsistencies (CONDITION_PATIENT= -> 400 traceback; MEDICATION_PATIENTS=',' silently drops cases) · non-blocking · cost: 0
- [r2] restore guard blind: stub baseline equalled an arm value, hardcoded set_gp(GP,"false") passed · non-blocking · cost: 1 round (confirming)
- [r2] usage text said "the defaults" do not resolve; only the condition default does · non-blocking · cost: 0

## Where a skill blocked or contradicted this run
- harden: mutate-and-restore with Your branch is up to date with 'origin/fix/240-grounding-scope-gate-refuses-before-gp'. on a file carrying uncommitted cycle-2 edits discarded them; recovered from a /tmp copy taken moments before. The commit-before-probe rule would have prevented it.

## Declined
- (none in the loop) harden deferrals: codes_only_order_grounding.py same GP-before-validation shape — if shipped without, a nonexistent PATIENT still gets three GPs written before the first request fails, because its main() checks only that PATIENT is set; eval/latency scripts hardcode the absent uuid — they 404 on the dev standalone.

## Assumptions review overturned
- none
