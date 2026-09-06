# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #315 / PR 316 · 2026-08-26
outcome: converged (round 6 reported 0 blocking) — but the DELIVERABLE inverted: the ticket's proposed fix was measured unworkable and reverted, so the PR refs #315 rather than closing it
rounds: 6 (pr-harden) · cycles: 5 (harden, converged empty at cycle 5) · verifier: ran r1 (works at runtime); not owed at finish (production diff is javadoc only)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa-Downloads-referenceapplication-standalone-3-7-1-openmrs-module-chartsearchai/f352fd83-388b-419e-8798-db190857be55.jsonl

## Refuted by measurement
- "the prompt clause fixes #315" -> fixed the ticket's own cell 4/4 but made an auto-expired order read as current; both are the same clinical error, so net benefit unclear · cost: 3 rounds
- "n=3 byte-identical repeats establish stability" -> consecutive repeats measure KV-CACHE stability, not answer stability (LocalLlmEngine's own javadoc: cache_prompt makes a borderline argmax non-deterministic). Every prompt figure in the first 5 harden cycles was weaker than it looked · cost: 2 rounds
- "the auto-expiry fold is caused by the currency sentence" -> deleting the sentence entirely folds identically; base (no clause) does not fold. The fold is the clause AS A WHOLE · cost: settled the direction, prevented more wording roulette
- "removing the currency sentence is safe" (round 1's fix) -> reinstated the ticket's defect WORSE than base ("is currently taking" vs base's "was ordered"), 4/4 interleaved · cost: 1 round
- "its text begins" is loosely worded, correct it to "carries" -> one word, everything else byte-identical, reinstated the original defect n=3 both directions · cost: caught in-cycle
- "the eval golds can gate this" -> 0 of 26 gold patients exist on this standalone; capture_eval_standalone.sh returns 32/32 HTTP 404 · cost: caught at plan gate

## Raised by a fresh agent, missed by the author
- [r1] auto-expired order beside live ones: clause asserts the lapsed drug is current AND drops both live prescriptions, while the payload's own chips name the two it dropped · blocking · cost: 1 round
- [r1] all-active chart loses its "Yes" verdict lead (#107 directness); no committed probe cell was a medications list, so no gate in the repo could see it · blocking · cost: 1 round
- [r2] the fix does not resolve the ticket on the ticket's own arrangement, 12/13 interleaved — and the n=3 methodology is structurally blind to it · blocking · cost: 2 rounds
- [r4] round 3's CAPTURE_DONE fix wrote the marker unconditionally: an arm that captured nothing read as a clean, empty A/B, exit 0 · blocking
- [r5] round 4's refusal did not CLEAR a stale marker, so the documented resume path left the old arm's answers + marker standing · blocking
- [r6] round 5's clear sat above both refusals, so a caller error destroyed a complete arm's marker (~190 LLM cells to recover) · non-blocking
- [r6] the sibling capture_probe_safety.sh still had the same fail-open, in the family that is actually gated, while the ADR claimed the guard was "in the WRITER" · non-blocking
- [r4/r5/r6] three successive overstated claims in one ADR entry, each narrowed by the entry's own data; a FOURTH home of a false attribution after a round claimed all three were fixed

## Where a skill blocked or contradicted this run
- pr-harden: the round cap (4) was reached mid-convergence with one-line findings outstanding. Extended twice (to 5, then 6) rather than declaring did-not-converge; findings shrank monotonically, so the extensions were right, but the skill offers no guidance on when extending beats overriding.
- resolve-ticket Step 2 "don't widen scope" vs CLAUDE.md "never commit a known regression" pulled opposite ways once the fix was measured to trade one defect for another. Resolved by reverting and reporting, but the skills do not name this fork.
- A subagent overwrote a helper script in the shared scratchpad (same filename, different signature), silently breaking a later measurement. Scratchpad is not covered by the worktree-hash discipline.

## Declined
- Nothing was declined across six rounds. Every finding raised was implemented.

## Assumptions review overturned
- "auto-expiry is out of scope, a pre-existing gap" -> it is a REGRESSION the clause introduces (base separates the lapsed drug correctly) -> round 2
- "prompt-only is the right locus, per the ticket's own root-cause guess" -> the information is not in the record text; the fix is structural (mark records whose orders are not in getActiveOrders), and cannot live in DrugReferenceInjector because drugReference.enabled defaults false -> round 3
- "a prompt cannot tell an ended order from a live one" (my own conclusion) -> too strong; it cannot classify an UNMARKED one. The prompt tells them apart 4/4 where date_stopped is set -> round 4
