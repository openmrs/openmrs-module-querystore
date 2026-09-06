# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #355 / PR 362 · 2026-09-02
outcome: converged
rounds: 9   cycles: 5   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-openmrs-module-chartsearchai/8194686b-07b0-4bdb-bccb-b593d0471c16.jsonl

## Refuted by measurement
- "the plan's cap in dataset order is fine" -> capping the tail at 5 in dataset order evicts the entry's only Major partner (spironolactone at index 5, behind two Moderate and three Unknown). Gate pass 1 · cost: 0 rounds (caught pre-code)
- "digoxin renders today, so its absence discriminates the cap" -> the tail loop breaks on the budget at index 7; digoxin never rendered, and under the severity order it DOES render. Gate pass 1 · cost: 0 rounds
- "the naming flag is pinned" -> refuted SEVEN times, each round finding a different semantically-equivalent re-derivation that left the whole build green: !compact.equals(rendered); firstNonBlank(getToken())!=null; severity!=null; getToken()!=null||getAtc()!=null; label!=null&&severity!=null; getAtc()!=null||firstNonBlank(getToken())!=null; and the first-present-then-blank-check inlining. cost: ~6 rounds
- "moving the read into the constructor ended the family" -> it removed the CALL-SITE re-derivations only; each is still writable on the constructor's own line. cost: 1 round
- "the shape matrix bounds the family" -> it filed 7 of the 9 cells of the 3x3 product partnerLabel distinguishes, and its anti-vacuity check structurally could not see an unfiled cell (it asked whether every FILED cell is asserted). cost: 1 round
- "#355's live reproduction witnesses this PR" -> #357 landed mid-run and moved that arrangement into a new chart-named segment; the ticket's three-ARV records are byte-identical on both trees (215/662). The witness is a chart with no DDInter-covered order. cost: 0 rounds (found during the re-base)
- "the suite bounds the cap somewhere under 3" -> an inference from the one value never run; at 2 it reddens, so the bound is exactly 3. cost: 1 round
- "573 withheld out of the elected row's 592 rules" -> withheldInteractions counts PARTNERS (578) after onePerPartner, which the field's own javadoc insists on; 592-573=19 against the 5 partners the same sentence quoted. cost: 1 round
- "the constant's javadoc carries the trimmed evidence" -> none of it is there; all of it is in InteractionNote.namesItsPartner's field javadoc. ProjectInstructionsGuardTest cannot catch this: the symbol resolves, only the claim about its contents was false. cost: 1 round
- "warfarin is in ibuprofen's compact tail on the shipped KB" (verifier's brief, from a comment measured over the bundled 16-drug excerpt) -> it is not; the verifier re-drove the same contract with ketoconazole plus a positive control. cost: 0 rounds

## Raised by a fresh agent, missed by the author
- [gate] the plan changed a second production decision purely for testability -> not applicable this run; the gate instead caught the dataset-order regression before code. blocking · cost: 0
- [r1] the naming flag was undiscriminated against its own javadoc's anticipated simplification · blocking · cost: 1 round
- [r1] docs/adr.md Decision 43's quoted record and named truncation mechanism were falsified; the sweep that fixed Decision 63 missed it · blocking · cost: 1 round
- [r2] verify-row (d) holds for the Metformin instance and NOT for the class: on Lisinopril the same collapsed row is rated Major, survives the top five, and prints `ketoconazole (Major)` with the witness removed · blocking · cost: 1 round
- [r3] the PR had become UNMERGEABLE against a base that moved to #360's own fix, which renamed the method this branch edited; GitHub still said mergeable=UNKNOWN · blocking · cost: 1 round
- [r4] a safety-chip silencing mis-attributed to #360; measured on the base that already contains #360's fix, both answer shapes raise the chip there and neither does here, so this change silences it · blocking · cost: 1 round
- [r6] the matrix's own anti-vacuity check could not see an unfiled cell · blocking · cost: 1 round
- [r8] the CLAUDE.md trim's pointer named a javadoc that held none of the evidence · blocking · cost: 1 round
- [verifier] its own briefed check could not have witnessed what it was for, and it constructed one that could, plus a positive control · non-blocking · cost: 0

## Where a skill blocked or contradicted this run
- pr-harden:"one commit per round" vs "commit before anything mutates the worktree" — round 2's fixer died mid-phase on a 429 with partial work in the tree; committing it before retrying (correct) produced two commits for that round. The commit-first rule should say it outranks the one-commit convention.
- pr-harden:Step 1 fetch refs — a reviewer checked out `pr-362-r9` IN the orchestrator's worktree because the PR branch was held by a leftover agent worktree, and a later orchestrator commit landed on the review ref. The branch check caught it; recovery was a fast-forward push of the commit to the PR branch, touching no checkout. Worth saying that the fetch-ref may end up checked out, and that the remedy is a direct fast-forward push rather than a local checkout when the branch is held elsewhere.
- pr-harden:Termination — the default cap of 4 was reached with the loop demonstrably working (no re-raises, a different defect each round). Raised 4->5->6->7->8->9, one at a time, each with the signal stated. Nine rounds to converge on a change whose production diff is ~15 lines.
- harden:Termination — 5 cycles; every cycle's findings were in prose the PREVIOUS cycle wrote. Nine of ~twenty corrections were themselves wrong.
- The 85,000-byte CLAUDE.md budget was overflowed by MERGING main (#354), not by this branch's own prose. The guard's javadoc forbids raising the budget in the overflowing commit, which is right, but the overflow arriving via a merge is a case it does not name.

## Declined
- Merging render's two tail branches into one loop with a tailCap variable — if we ship without it, a future change to the tail's budget rule must be made in two places and making it once is silent. Declined because the merged form states the one-representative guarantee arithmetically where the current form states it syntactically, and a later reviewer found a second argument against it: with tailCap=1 the merged loop's budget test would apply to the promoted representative, which renders unconditionally.
- Hoisting one Interactions:-section slicing helper into DrugReferenceTestSupport — if we ship without it, four pre-existing unguarded call sites throw StringIndexOutOfBoundsException with an opaque message the day a render change stops emitting the section. Declined as pre-existing sprawl across three unrelated test files; the sites this PR touches are guarded.
- Repairing the Levoketoconazole/ketoconazole naming collision — if we ship without it, a citable record prints a Major rating under a name whose own row DDInter rates Unknown, with the mechanism prose that betrayed it now removed. Declined as #196's: repairing it changes what partnerLabel calls a partner, which CLAUDE.md pins as the grouping key and printed name for every rule chip. Recorded in the constant's javadoc and the PR body instead.

## Assumptions review overturned
- "the ride-along naming defect is out of scope and row (d) is unmet" -> row (d) is met on the Metformin instance incidentally (the Moderate row loses a severity-ordered top five) and NOT met on Lisinopril, where the same row is Major. Round 2.
- "Fixes #355 may be dishonest since the reproduction is closed by #357" -> four independent rounds judged it honest: the defect the ticket NAMES is live on the base and this closes it; only the reproduction moved. Rounds 6-9.
