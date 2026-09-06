# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #346 / PR 351 · 2026-09-01
outcome: converged
rounds: 2   cycles: 3 (harden)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-346/e5b7d764-c9c7-483a-8e2d-018e58be862d.jsonl

## Refuted by measurement
- "ddi-route-variants.json cannot express a group replaced after a tied partner opened" (written into a
  new fixture's metadata AND into a DrugReferenceTestSupport javadoc) -> a Phase 2 agent drove the real
  validator with SUBJECT Sirolimus and got three tied Majors that DO discriminate the re-put mutation.
  The 153-line fixture was deleted and the guard rewritten over the existing slice. · cost: 1 cycle
- "all three interaction arms order most-severe-first" (config.xml, README, CLAUDE.md — written by the
  falsified-claim sweep itself) -> false twice: unrated class-only chips trail, and the sort is per
  in-play SUBSTANCE so a two-subject response is not one ranked list. · cost: 1 cycle
- severityPriority's javadoc was EXTENDED to list FINDING_STRENGTH_DESCENDING among orderings that share
  it, immediately above a clause saying two copies "could drift into ranking the same pair oppositely"
  -> a probe showed they now DO: chips lead with the folded Atorvastatin finding while the same prompt's
  drug_reference note list leads with Metformin. · cost: 1 cycle
- "a folded Minor sorts LAST among the withholding findings" -> false; a folded Unknown withholds on the
  same OR and ranks below minor, reachable by lowering minInteractionSeverity. · cost: 1 cycle
- PR body: "three new cases, each failing on the pre-change code" -> only two do; the stability case
  passes pre-change, because without a sort the chips arrive in the order it asserts. · cost: 0

## Raised by a fresh agent, missed by the author
- [harden P1] The comparator's licensesWithholding BRANCH was undiscriminated — deleting it left the
  whole api suite green. That branch was the entire point of gate pass 2's blocking objection. · cost: 0
  (found in-cycle)
- [r1] docs/ddi-interaction-question-examples.md still stated the defect in the PRESENT tense as standing
  guidance, and is the file the ticket's own comment points testers at; merging closes #346, so a tester
  would have kept applying a workaround against a closed issue. The sweep missed it because every token
  searched described the arm's ORDERING and this home described its ABSENCE ("neither severity-sorted nor
  capped"). · blocking · cost: 1 round
- [r1] "rated findings" is the wrong dichotomy: an unrated RULE chip (hand-authored json/curated) is
  neither rated nor class-only, is sorted by the comparator, and leads ahead of every Major. · non-blocking
- [r2] The comparator's KEY ORDER was unpinned — swapping to severityPriority-first left all 1697 tests
  green. Every case exercised an arrangement where the two RATINGS tie, which either key order satisfies.
  The only discriminating shape is a folded Unknown vs a plain Minor. · non-blocking · cost: 0 rounds
- [r2] The old collapse guard was still NAMED after the property #346 retired, and its assertion message
  still explained iron's lead by dataset position. · non-blocking
- [r2] The new fixture's note called its ratings the one thing it stated clinically, when the ratings are
  the one thing invented (shipped KB rates those pairs Unknown/Moderate), and its Atorvastatin id was
  invented too. · non-blocking

## Where a skill blocked or contradicted this run
- pr-harden:"State" — I ran `git checkout -- <path>` to revert my own key-swap probe on a file that ALSO
  carried the fixer's javadoc edit, and silently destroyed that edit. This is the exact hazard the skill
  documents; I had not committed first. No PreToolUse backup was found at the paths the skill names. Cost:
  one reconstruction. The skill's own remedy (commit before any mutation, including your own probe) is
  correct and I applied it for the second attempt.
- harden:Phase 2 — two of four cycle-2 agents stalled: idle >10 min with TaskOutput still reporting
  "running" and no completion notification. Stopped them and retried with ONE combined agent and a tighter
  ~25-tool-call budget, which completed in 8 min with an empty findings list. The skill's "do not infer
  death from file mtime" is right in general, but it offers no positive liveness signal to use instead.
- resolve-ticket:Step 3 — gate pass 1's blocking objection cited CLAUDE.md to argue there was NO licence
  to re-capture a golden test list. Checking rather than estimating refuted it: merged PR #342 rewrote all
  ten strings of that exact constant. The skill's "check it, do not estimate it — the objection's own
  numbers included" is what saved the run from taking a worse design.

## Declined
- (none — every finding in both rounds was implemented)

## Assumptions review overturned
- "Sort the rule chips on severityPriority, as the ticket suggests" -> gate pass 2 refuted it from
  CLAUDE.md: this is the only arm that FOLDS, so the key must be the FINDING (licensesWithholding) and
  only then the rating. Overturned before any code was written.
- "The fixture I built is necessary because the existing one cannot express the arrangement" -> overturned
  in harden cycle 1 Phase 2 by driving the real pipeline under a different subject.
