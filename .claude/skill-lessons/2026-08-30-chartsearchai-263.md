# resolve-ticket (+ harden, pr-harden) · openmrs-module-chartsearchai · #263 / PR #331 · 2026-08-30
outcome: converged
rounds: 3 (pr-harden)   cycles: 3 (harden, 9 Phase-2 passes in cycle 1)   verifier: skipped (git diff origin/main -- api/src/main has zero non-comment lines; round 3's reviewer independently confirmed the classification)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-263/7019b3ee-2510-47d0-8e0d-fd2616d03f15.jsonl

## Refuted by measurement
- "the KB is not on this machine; the harness must fetch it and is throwaway for that reason" -> the 19 MB KB is git-tracked at api/src/main/resources/chartsearchai/ddi-knowledge-base.json, same blob as the upstream download, and the api suite already loads it via DrugReferenceTestSupport.shippedEntries() · cost: 1 gate pass (found at refutation gate 1, before code)
- "486/54/7243 is the shipped rule's split" -> it is scoped to the 30-group UNCLASSIFYING_ATC_GROUPS of #182; the list has held 36 since #184/PR #241, so the shipped split is 643/54/7086 and the neighbouring 1488 was stale at 1331 · cost: 1 gate pass (found at gate 1; became the change's second deliverable)
- "the four contributor magnitudes 135/130/99/68 reproduce under no attribution tried" -> they reproduce exactly as all-pairs counts (lost + moved) on the same attribution: 135=134+1, 130=115+15, 99=99+0, 68=33+35, moved halves summing to the paragraph's own 54 · cost: 2 harden passes (a wrong correction, then its correction)
- "vetoing every residue drops 1974 ROW / 1741 SUBSTANCE" -> that run vetoed the 98 residues UNIONED with the list's 36 members; 7 of the 34 it refuses are not residues, and the 98 alone drop 1816 / 1598 · cost: 1 pr-harden round
- "the veto-set guard reddens whenever the list moves" -> a group covering no KB-published subgroup escapes it; measured by appending Z99ZZ and staying green · cost: 1 harden pass
- "a KB refresh that changes which subgroups the dataset publishes reddens the guard" -> a size-preserving swap of two unrefused subgroups leaves both keys alike and escapes · cost: 2 harden passes + 1 pr-harden non-blocking finding (the same defect in the sibling guard)

## Raised by a fresh agent, missed by the author
- [gate 1] `sharedClass` always applies `justifiesClaim`, so the no-claim-filter counterfactual is unreachable by reflection and needs a production mutation; the plan's fallback was a re-expression CLAUDE.md forbids · blocking · cost: 1 gate pass
- [gate 1] the "9 + 12 = 21" reconciliation was a derived figure with no measurement of its own, over a corpus the repo does not carry · blocking · cost: 0 (deleted at plan time)
- [harden] the interactions table has exactly one self-row (DDInter225), so it is not a subset of the ROW-pair base · non-blocking
- [harden] `#241` is the PR; every other site in the file cites issue `#184` · non-blocking
- [harden] four inserted paragraphs sat inside the threads they annotated, and the diff had patched one seam to hide it · non-blocking · cost: 1 pass
- [r1] the 19/2 this PR was filed to supply had a tripwire on the sibling list and none on its own key; adding A06AD to LOCALLY_APPLIED_ATC_GROUPS (+ SITE_GUT) left the whole suite green while moving 46->47 and 19->20 · blocking · cost: 1 round
- [r2] the blanket-veto headline published a counterfactual that was not the one measured · blocking · cost: 1 round
- [r3] the two guards worded the same coverage class differently · non-blocking · cost: 0 (applied at FINISH)

## Where a skill blocked or contradicted this run
- resolve-ticket:Step 3 — gate pass 2's objection 1 asserted `sharedTherapyClass`/`sharedCrossReactivityClass` are package-private and callable from a same-package test. All three are `private`. Acting on it would have replaced working reflection with a non-existent call; verified before applying, cost ~0.
- pr-harden:State / harden — `git checkout -- <path>` used to restore a measurement mutation silently reverted intended javadoc edits in the same file. Hit twice: once by the round-1 fixer (it caught itself) and once by the orchestrator (caught by grep, restored from a stash recovery point). The skills warn about this; the warning is correct and the trap is still easy to walk into when the mutation and the edits are in one file.
- gate-state — `reviewed-sha` rejects `--only pr`, unlike `await`/`clear-await`. Cost one retry.
- Repeated: `zsh` does not word-split unquoted `$vars`, so a loop passing multi-flag `-D` strings to maven silently sent one malformed argument and three mutation configurations ran unmutated. Detected only because a figure that should have moved did not. Cost ~20 minutes and one re-run.

## Declined
- (none — every finding in every round was implemented)

## Assumptions review overturned
- "the measurement harness must open its own stream over a downloaded KB" -> `DrugReferenceTestSupport.shippedEntries()`, per CLAUDE.md's ReferenceDataFiles rule (gate 1)
- "this is documentation-only, so no test is possible" -> two data guards over the shipped KB are possible and one of them would have caught the drift this ticket exists to fix (gate 1 non-blocking, then r1 blocking for the second guard)
- "correcting the stale 1488 is scope creep" -> publishing a substance-pair counterpart of a wrong number delivers the ticket dishonestly; the correction is inside scope (gate 1)
