# resolve-ticket (+ harden, pr-harden) · openmrs-module-chartsearchai · #514 / PR #524 · 2026-09-24
outcome: did-not-converge (pr-harden round cap of 4 reached with r4-1 blocking; override recorded; PR left draft)
rounds: 4   cycles: 6 (harden, run harden-514a)   verifier: ran 3x (works at runtime each; repairs environmental only)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-514/8b0eb230-7971-41bc-afc8-73c4c763ef24.jsonl

## Refuted by measurement
- Plan: "a run-less claim takes finding markers up to the next claim" -> refuted at gate pass 2 (reference/CLAUDE.md run-unit rule + cry-wolf example) · cost: 0 (plan time)
- Plan: finding names = subject+partners only -> gate pass 1 showed chart-order bridges give a second name; false alarm on brand-named orders · cost: 0
- Harden r4 fix "one side must be the finding's SUBJECT" -> harden r5 lens measured a false report on #477's several-orders finding (orders related to each other); reverted · cost: 1 harden cycle
- ADR residue list "these N legs are unpinned" -> each Phase 2 round found one more; deleted the enumeration · cost: 2 harden cycles
- ADR Decision 116 number -> taken by #523 merged meanwhile; renumbered to 117 at rebase · cost: 0

## Raised by a fresh agent, missed by the author
- [gate1] comma-placed markers give an empty run on ticket cases 2 and 4 · blocking · cost: 0
- [gate1] bridge names missing from N(F) · blocking · cost: 0
- [gate2] drug_reference citation makes a claim unfounded falsely · blocking · cost: 0
- [harden P2 r1..r5] ~15 unpinned guards/false prose; flat-name-set order-order miss; split's false report on #477 · mixed · cost: 5 Phase-2 escalations
- [r1] swapped-subject citation after a comma never named in misattributedCitations while docs illustrated it · blocking · cost: 1 round
- [r2] invented partner beside a real one passes; lead clause 'but'/parenthesis hides swap · blocking x2 · cost: 1 round
- [r3] pronoun subject in later clause read as other drug (false report); 'but not with X' counted as partner (false report) — both introduced by r2's widening · blocking x2 · cost: 1 round
- [r4] run-less claim's partner span swallows next 'and'-joined clause (false unfounded) — introduced by r3's list rule · blocking · unresolved

## Where a skill blocked or contradicted this run
- harden:Phase 2 "runs once" + escalation rule — six Phase 2 rounds ran (five escalated), each quality lens finding one more unpinned leg or seam; most expensive part of the run.
- pr-harden:round cap — each round's fix of a prose-position heuristic introduced the next round's false-report shape (r2->r3->r4); the loop exposed that a free-prose recogniser widened per finding does not converge within 4 rounds.
- orchestrator error: ran `gate-state declined` with placeholder values; removed under the gate-state lock. No harm, but the helper has no remove subcommand.

## Declined
- none (no finding declined in any round)

## Assumptions review overturned
- A2 "relating is orientation-free over one name set" -> briefly replaced by subject/order split (harden r4), restored (harden r5) with the merged-finding miss as stated residue
- A6 "only departure from the sibling's run is this check's own" -> round 1 added gated trailing-run attribution after a comma
- A4 "containment residues run toward silence" -> false for the partner side (can report); corrected in harden

## Driver capture (pool-run)
outcome as the driver measured it: draft
session: 8b0eb230-7971-41bc-afc8-73c4c763ef24 · 3h52m · 1352 assistant turns · stream: /Users/danielkayiwa/.claude/pipeline/logs/20260924T061420Z-openmrs_openmrs-module-chartsearchai-514.jsonl
- the run left its gate entry unfinished: phase=reviewed blocking=1 round=4 override=True
