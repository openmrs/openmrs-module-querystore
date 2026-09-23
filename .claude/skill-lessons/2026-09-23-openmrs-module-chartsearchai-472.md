# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #472 / PR #478 · 2026-09-23
outcome: converged
rounds: 3 (r1 full: 2 blocking; r2 full: 0 blocking, 2 non-blocking implemented as owed at FINISH; r3 blocking-only: 0)   cycles: harden 3 (Phase 2 escalated twice)   verifier: ran (works at runtime, on 26fb9c3f and on merged 5db3ccc7; head 33376433 covered by artifact identity, docs-only delta)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-chartsearchai/4e1fa208-efbe-4333-b7d3-f98a32d9ed27.jsonl

## Refuted by measurement
- plan: "the ranking sentence should name the new call" (refutation gate, Q5) -> three-arm live A/B: naming it (arm A) added a refusal on R1; declined · cost: 2 extra A/B arms (~1h)
- plan: "branch inside the safety paragraph" -> arm B put a bare "No —" on both current-medication cells; placement after the never-"Yes" token shipped · cost: 1 arm
- author: the Rifampicin chip label check "the answer already said it" works -> label is "Rifampicin (rifampin)", no model writes it; R2 double statement on the live rig · cost: 1 fix at FINISH
- author: "C is the only arm matching the baseline's bare-No count" (ADR) -> arm A matched it too · cost: harden pass
- author: an ad-hoc prompt-diff script returned 87 chars for a multi-thousand-char prompt; recalibrated against compiled class bytes with a base control

## Raised by a fresh agent, missed by the author
- [gate p1] act "not to restart" presupposes a proposal (Decision 72's cause) · non-blocking · cost: 0 (reworded pre-code)
- [gate p2] A/B must re-run Decision 72's cells, and contraindication stamp site untested · blocking · cost: 0 rounds
- [harden p2] FALSE + null-stamped record of one drug read as ended; unresolvable active order under a brand could make a live drug "ended" · substantive · cost: 1 harden cycle
- [harden p2] module path "No —" lead vs prompt branch forbidding refusal on a proposal of an ended drug -> proposal exclusion via #469 grammar · substantive · cost: 1 cycle
- [harden p2] second BridgedOrders per pass (#151 shape); ADR result claim false; Decision 47 cell misattributed · substantive · cost: 1 cycle
- [r1] question-pair arm unstamped; R1 answer still confirms the false premise -> module-appended sentence (Decision 100 pattern) · blocking · cost: 1 round
- [r2] label-substring "already said" check dead for generic-labelled drugs; ADR A/B predates Decision 109 · non-blocking (fixed as owed) · cost: 1 round

## Where a skill blocked or contradicted this run
- resolve-ticket/pr-harden assume one run per checkout: another session had uncommitted #471 work in the SAME tree; hunks had to be split by owner and moved to a new worktree (zero-context patch misplaced a class inside a javadoc on the first attempt)
- my own deploy helper's pgrep pattern matched every standalone on the box (a pool-slot rig on :8085 could have been killed); the verifier caught it and scoped it
- gate-state `await` requires --only or --run; the pr entry lost its fields after mixed --cwd writes and had to be re-set
- ProjectInstructionsGuardTest budget: reference/CLAUDE.md had 22 bytes of headroom on main; the new rule had to be folded into an existing bullet by trimming restated evidence

## Declined
- (none declined in review rounds) harden deferral: duplicated stamped-order test builder — two builders can drift, no production behaviour depends on either
- harden: judging question drugs over namingRows (pass agreement) — reverted as unpinned by any test; recorded as ADR residue

## Assumptions review overturned
- A2 "stop date not on chip/clause" -> r1 added endedOrderStopDate (pre-formatted String, so the accessor/wire guard compares like with like) and the date in the appended sentence, round 1
- A3 "question-pair arm out of scope" -> in scope (r1-1), round 1
