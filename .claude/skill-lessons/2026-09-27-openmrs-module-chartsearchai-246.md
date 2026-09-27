# resolve-ticket · openmrs-module-chartsearchai · #246 / PR #547 · 2026-09-27
outcome: converged (PR delivers a measurement, Refs #246; defect still open)
rounds: 3   cycles: 2 (harden-246a; Phase 2 escalated once)   verifier: skipped (no round changed production bytes; runtime claims were measured live by the author/fixer on the slot standalone)
context: no compaction · peak not surfaced
transcript: /Users/danielkayiwa/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-246/77631f7e-dfde-4a09-94c0-93402a865946.jsonl

## Refuted by measurement
- The ticket's diagnosis "the blanket prompt sentence causes the misreading; narrow it" -> two narrowings (B, C) fixed Betty's cell but each refused two other ABSTAIN cells; deleting the phrase (D) refused four; null-perturbation arms moved the misreading by up to one cell, so inconclusive · cost: the A/B (~1.5h standalone) plus r1 fixer's null arms
- "The issue comment's phrasing reproduces it" -> "Is amoxicillin safe…" abstained 3/3 at this head; only the probe phrasing "Can this patient take…" refused · caught by the refutation gate (q6) before any arm ran
- "The short fixture will reproduce" -> the 153-record test chart abstained 3/3 under both phrasings, with Betty's allergies, and with the production request body · cost: ~5 probe runs
- "Noise floor zero from A/A" as a gate -> zero by construction under greedy decode; the relevant floor is a meaning-free prompt edit · cost: 1 round (r1-1 blocking)

## Raised by a fresh agent, missed by the author
- [gate] first wording asserted silence-as-denial ("only Recorded clauses are recorded") · blocking · cost: 0 rounds (plan revision)
- [gate] n too small / phrasing not shown to reproduce / oracle is a negation blocklist · blocking · cost: 0 rounds
- [harden p2] same-chart decoy does not reset the KV prefix; oracle fails open on markdown-bold "No" · substantive · cost: 1 extra harden cycle
- [r1] zero A/A floor is by construction under temperature 0; "net-protective" unsupported · blocking · cost: 1 round
- [r2] "every refusal" claims true only on ABSTAIN cells; Betty chips-only labelling undisclosed · non-blocking · cost: 1 round

## Where a skill blocked or contradicted this run
- pr-harden:round 2 — first reviewer spawn returned "[Request interrupted by user for tool use]" under CLAUDE_PIPELINE_SESSION=1; treated as agent death per State, retried once with a leaner brief, which succeeded

## Declined
- r1-4 (part) follow-up issue for the note-wording experiment — if #246 is closed without reading Decision 121 the pointer to that experiment is lost and the curated-source misreading stays unexamined

## Assumptions review overturned
- A2 "A B B A with an A/A control satisfies interleaved" -> A/A is uninformative under greedy decode; null arms added (r1)
- A1 "if no candidate passes, the prompt stays and PR says Refs" -> held, but the verdict changed from "rejected" to "inconclusive" (r1)
