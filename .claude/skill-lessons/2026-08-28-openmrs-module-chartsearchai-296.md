# resolve-ticket + harden + pr-harden · openmrs-module-chartsearchai · #296 / PR 328 · 2026-08-28
outcome: converged
rounds: 2   cycles: 1 (harden, overridden)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-https--github-com-openmrs-openmrs-module-chartsearchai-issues-296/7491e12e-f0d3-4cb4-a27e-94de44597e38.jsonl

## Refuted by measurement
- "of the 72 above-floor rules, 40 are now reconciled" (simulated from candidate partners) -> the fold gets one specific labelEntry, so the simulation overcounted; replaced by an end-to-end sweep driving the real validate once per arrangement: 62 folded, 0 named once before, 28 after · cost: caught at the Step 3 gate, 0 rounds
- "a substance's claim is the MAX over its rows" -> admits a displacement the printed row does not license (`Atropine` rank 2 carrying `Atropine (ophthalmic)` rank 1 past `Hyoscyamine` rank 1); measured to buy nothing on the shipped KB · cost: caught at the Step 3 gate, 0 rounds
- "every rival passed isNamed, so every rival ranks at least NAME_IS_ANOTHER_NAME" -> false; a padded alias answers isNamed true and NAME_NO_MATCH, which the same javadoc documented 25 lines below · cost: 1 harden pass
- "with the alias trimmed, isNamed implies matchesDrugName" -> false; an alias of combining marks alone survives the trim and folds to an empty needle · cost: 1 harden pass
- "the loader drops any alias that names nothing" -> false; sanitizeAliases' repair rung re-added exactly what its drop rung removed, because the drop asks namesAnything and the repair asked only non-blank · cost: 1 harden pass
- "the blank-alias finding has already told the operator" -> false for an entry named `---` with healthy aliases: blank-alias fires on the ALIAS list, so that shape went silent · cost: 1 harden pass
- "it loads unreachable by every name-driven arm" -> false for its own motivating shape; that entry is reachable by its other aliases · cost: 1 harden pass

## Raised by a fresh agent, missed by the author
- [harden P2] `contenders.add(entry)` was inert — uniqueStrongestClaimant's identity guard skipped it on every path · non-blocking · cost: 0
- [harden P2] the change was NOT monotone: the ranking imposes a matchesDrugName floor the existence form did not, so a padded alias made the fold refuse what its gate admitted — and with the padding on a RIVAL, ADMIT a displacement (one substance's mechanism under another's name) · blocking-class · cost: 1 pass, fixed at DrugReference.setAliases
- [harden P2] nothing pinned the own-substance exclusion — removing it left the whole suite green while 187 admissions depended on it · non-blocking · cost: 1 pass
- [harden P2] the residue was mischaracterised as all ties; `gabapentin` is not a tie, it loses because canonicalRow hands the ladder the weaker claimant · non-blocking · cost: 1 pass
- [r1] **the branch did not merge**: main moved to ff2c7d48 where #297 already defines a DIFFERENT Decision 51, and three of this PR's citations would have pointed at it · blocking · cost: 1 round
- [r2] the pre-#296 causal sentence survived in #297's own test file, at the merge seam — the one home the "correct every home" sweep could not have reached before the merge existed · non-blocking · cost: 0 (applied at FINISH)

## Where a skill blocked or contradicted this run
- environment: the worktree this run was given was named from the URL argument, so its path contained a COLON (`...chartsearchai-https:/github.com/...`). javac treats `:` as a classpath separator, so test-compile could not see a single main class and the build failed on a pristine checkout. `git worktree move` to the number-based name every other pipeline worktree uses fixed it. A sibling worktree for issue 238 has the same defect.
- harden:Phase 2 — the four parallel agents each took ~15-30 min; three of the four rounds of findings were about prose the PREVIOUS pass had written, which is the signature the skill itself warns about. The loop only converged once the fixes moved from re-wording to deleting and to fixing the root cause in the loader.
- pr-harden:round 1 — the reviewer left the worktree checked out on its own `pr-328-r1` branch. Caught by the orchestrator's pre-fix branch check before any edit. Also: 8 spent harden agent worktrees still held the PR branch and had to be removed before it could be checked out.
- session rate limit killed the harden Phase 2 pass-7 agent mid-run; that pass was completed inline instead and labelled.

## Declined
- (none — every finding from both loops was implemented)

## Assumptions review overturned
- "a substance's claim is the max over its rows" -> the ladder ROW's own claim, never its substance's (Step 3 gate)
- "the ranking is a pure widening" -> it narrows in one shape and fails OPEN in another, both closed at the loader (harden Phase 2)
- "recording the padded-alias narrowing as a residue is proportionate" -> no, the mirror case admits a wrong displacement, so it had to be fixed rather than pinned (harden Phase 2)
