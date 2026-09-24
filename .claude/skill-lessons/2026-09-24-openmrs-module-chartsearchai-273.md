# resolve-ticket 0.21.0 (+ harden, pr-harden) · openmrs-module-chartsearchai · #273 / PR #533 · 2026-09-24
outcome: converged
rounds: 3   cycles: 2 (harden)   verifier: skipped (javadoc/comment/message-string only; nothing runtime-visible)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-273/57517475-94c2-455f-9557-9174614051d6.jsonl

## Refuted by measurement
- "moot for the dose arm since #271" supported by a control on main -> gate showed main's #296 alias trim turns " " into "", so main's control could not isolate #271; re-measured at c39cc524 itself (no trim): warning survives -> claim held on new grounds · cost: one throwaway run, pre-code
- ticket item 1's "cannot survive a load" -> false for an entry whose display name names nothing (#296 reports rather than repairs) and for the setEntries seam · cost: 0 (caught in planning)

## Raised by a fresh agent, missed by the author
- [gate] main's control confounded by the #296 trim · blocking · cost: 1 re-measure
- [harden P2 c1] new paragraph called PR #271 an "issue" (the item-3 mistake repeated; issue is #260) · substantive · cost: 1 harden cycle
- [harden P2 c1] "for it the display name is not a match at all" false when an alias occurs inside the name (Warfarin sodium / warfarin) · substantive · cost: same cycle
- [harden P2 c1] rowsOf and namesItsSubstance homes of the item-1 claim; antecedent "that method's own javadoc" ambiguous · substantive · cost: same cycle
- [harden P2 c2 / r1] "json is the DEFAULT format" false since ADR Decision 36, in the javadoc the PR edits · non-blocking · fixed in r1 at 9 homes
- [r2] PR body's "Not done here" listed as undone what r1 had done · blocking · cost: 1 round
- [r2/r3 notes] two test javadocs still say json default; PresentationMoietyAllergenTest and a fixture description carry the pre-narrowing item-1 claim · non-blocking · unfixed, named in PR body

## Where a skill blocked or contradicted this run
- resolve-ticket Step 6 — a background root build started before the last comment edits had to be killed and re-run; the Stop hook refused the yield while waiting on it (monitor-based wait), recovered with a foreground until-loop · cost: one stale build
- pr-harden step 1 — PR ref lagged the push after round 1 (fetched sha = previous round's); the bounded re-fetch loop caught it before spawning
- pr-harden — a blocking finding on the PR DESCRIPTION leaves the head unmoved, so the confirming round reviews the same sha; step 1's "identical sha" guard reads as a stop, resolved by noting the body changed

## Declined
- (none declined) — two r2/r3 notes left unimplemented because only blocking-only rounds followed; named in PR body

## Assumptions review overturned
- "item 3 is the only #148 mis-credit worth touching" -> held; other "issue #148" sites credit PR 148's own work, named in body
