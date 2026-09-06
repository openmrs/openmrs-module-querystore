# resolve-ticket + pr-harden · openmrs-module-chartsearchai · issue #250 / PR 311 · 2026-08-25
outcome: converged
rounds: 2 (pr-harden)   cycles: 5 (harden)   verifier: ran (works at runtime)

## Refuted by measurement
- "add a third rung" (the ticket's own wording, carried into the plan as: place the new rung above namesNoRoute) -> the above-placement is the ONLY shipped family case that elects a route-qualified row over an existing unqualified one, falsifying canonicalRow's own first rung and DrugReferenceInjector.matchingEntries' monotonicity argument, and costing that family 12 rendered interaction partners against 1 gained · cost: caught at the Step 3 refutation gate, 0 rounds
- "the pre-#250 fold must be re-expressed in a harness because production no longer carries it" -> origin/main's own canonicalRow is production code, drivable from a second worktree · cost: 1 Phase 2 finding
- "the mixed-substance ATC fold gives the same row for every code" -> it is order-sensitive on 49 of 2148 codes with the rung and 50 without; the rung REMOVES one and adds none · cost: 1 cycle
- "no shipped family distinguishes floor level rank-1 from rank-2" (inferred from a mutation reddening nothing) -> true, but re-measured directly: 0 of 129 families over every alias any row publishes · cost: 1 cycle
- "restricting bestRulePerPartner to the elected row makes the new tracer case the sole failure" (fixer's attribution) -> that mutation reddens 10 cases · cost: 0 (caught before it shipped; claim never reached the repo)
- ADR 43's "1415 api tests" -> the suite size WITH the two cases the same sentence says did not yet exist · cost: 1 cycle

## Raised by a fresh agent, missed by the author
- [harden P2] The whole-name-vs-displayStem decision, argued at length in javadoc, was pinned by NOTHING: the stem mutation left all 1415 api tests green while reverting one of the three renames and handing entryForAtcCode("D08AL01") to an ophthalmic presentation · blocking-equivalent · cost: 1 cycle
- [harden P2] The rung caused a strict REGRESSION two surfaces away: chartAnchoredSubject inferred "the chart chose this row" from "the fold disagrees with the chart", and the rung made the fold reach the chart's row, so the #237 attribution clause vanished from the arrangement that needs it. Whole suite green on both sides · cost: 2 cycles
- [harden P2] assertFalse(detail.contains("Fluoroestradiol f-18")) was blind to the KB's own "fluoroestradiol F 18" inside mechanism prose · cost: 1 cycle
- [r1] The STRICTNESS of recordNamesMoreStrongly was unguarded: `>` -> `>=` makes it true in both directions, licensing the #237 clause on an equal-claim pair, suite green. The author's own sweep could not express the counterexample — it took each row's DISPLAY NAME as the recorded order, a population where no two rows of a family can tie above rank 0 · blocking · cost: 1 round
- [r1] The refusal to rename the ticket's fourth family rested on namesNoRoute() calling that row route-qualified — a MISREADING; the parenthetical is part of the row's own substanceName. Widening rung one delivers that family, moves 1 of 129 elections and 0 of 2148 folds, and dissolves the rung-order constraint · non-blocking · cost: 1 round
- [r2] Three guards whose stated job their own fixture could not express (tracer prose with no rated tracer rule in the slice; a "record has nothing to attribute" case that never reads a record and justifies silence by the retired proxy; the floor's level, unpinned while the comment naming the fixture that would pin it) · non-blocking · cost: 1 round
- [verifier] A rated-partner count (578) read as a chip count; live, the chip arm is order-scoped and emits five chips before and after · non-blocking
- [verifier] The built omod predated the merging head; the freshness check caught it before testing the wrong bytes (pr-harden's VERIFY step 5, not step 4 — step 4 is Restart; corrected during the retro that read this record, flagged as after-the-fact capture)

## Where a skill blocked or contradicted this run
- harden:Phase 2 / pr-harden:State — `git checkout -- <path>` to undo a mutation probe silently reverted UNCOMMITTED intended work, twice. First cost three javadoc paragraphs; second silently un-fixed the #237 regression and only the full build caught it. The skills both document this hazard and I walked into it anyway; the fix that worked was committing after every verified step, not remembering the rule.
- harden:bin — `hstate` and `claim-lint` existed at the start of the session and were gone from ~/.claude/bin later; fell back to writing both state files directly.
- pr-harden:FIX — the fixer's own worktree is not on the PR branch (the main worktree holds it), so both fixers reached it with `git checkout --ignore-other-worktrees` and left the diff there; the orchestrator had to extract it as a patch. Worked, but it is undocumented and the patch had whitespace warnings.
- pr-harden:FINISH — `git branch -D pr-311-r2` failed because a reviewer's worktree still had that ref checked out; had to remove the worktree first.

## Declined
- (nothing declined in either round)
- Deferred, out of scope, with its failure mode: entryForAtcCode rescans all 2283 entries per ATC code (~73k normalizedAtcCodes() allocations per validate, 9ms of 37ms). Nothing breaks and no output changes; pre-existing and unrelated to #250.

## Assumptions review overturned
- "the new rung goes third, i.e. below namesNoRoute, and that is the only defensible placement" -> still the right placement, but the REASON was wrong: r1 showed the invariant it protects rests on namesNoRoute() misreading 4 of 2283 rows, and a better route to the ticket's fourth family exists and is now recorded as a rejected alternative.
- "every behaviour change has a test proven to be the sole reddener of its own mutation" (PR body at open) -> false for two halves of one line; rounds 1 and 2 closed both.

## Under-captured at write time, added during the retro that read this record
- **The harden-side worktree detour, which the record omitted entirely.** The "Where a skill blocked
  or contradicted this run" section above records only the pr-harden FIXERS' case. The same run's
  harden Phase 2 lenses hit it too: of the four spawned with `isolation: "worktree"`, one reported
  reading the diff "from the worktree without checking the branch out (it is checked out in the main
  repo)", one ran `git checkout -B review250 fix/250-…` noting "(branch was checked out in another
  worktree)", and one checked the branch out under a local name of its own. No round or cycle was
  spent — each recovered on its own — so the cost is a detour per agent, as in #269.
- **The cause here is NOT #269's cause, and the distinction matters more than the count.** In #269
  every agent's worktree "opened at origin/main, not the PR branch". In this run the branch existed
  and git REFUSED it to a second worktree because the main worktree held it — which is why both
  pr-harden fixers needed `git checkout --ignore-other-worktrees`, a flag #269's runs never needed.
  One symptom, two mechanisms.
- **A PR-body incident the record missed.** `resolve-ticket` Step 8 says to use `Refs` rather than
  `Fixes` where a PR does not close its ticket, "and say why in the body". The sentence written to say
  why — "It does not close #250" — put GitHub's `close` keyword immediately before the reference, and
  `gh pr view 311 --json closingIssuesReferences` came back listing issue 250. Rewording it to "Issue
  #250 stays open, deliberately" emptied the field. Caught by the orchestrator's own check before the
  PR was published, so the cost was zero rounds; had it merged, it would have closed a ticket whose
  second named defect is still open, with nothing erroring.
- **Provenance: all three bullets are after-the-fact capture**, written during the retro rather than
  at run end, and flagged for the reason #269's own addendum gives — a retro citing a record for
  material that lives only in an agent's report, or in the orchestrator's memory, is the defect
  REJECTED.md records a refuter catching. The first two rest on the three lens reports of this run;
  the third on the `gh` field transition the orchestrator observed and acted on.
