# resolve-ticket + pr-harden + harden · openmrs-module-chartsearchai · #302 / PR 303 · 2026-08-23
outcome: converged
rounds: 1 (pr-harden)   cycles: 10 (harden)   verifier: ran (works at runtime)

## Refuted by measurement
- "A compound claim unit should keep publishing its Tier-1 cosine FAIL, because sentence scope already specifies that verdict" -> measured: on {cosine FAIL, lenient judge} main published true and the draft published false, reintroducing #302's own harm on a correct citation. The rule became symmetric (publish nothing). · cost: 1 harden cycle to find, 1 to reverse, 3 to sweep the documentation debt
- "clauseScoped=true supersedes the #302 treatment" (written into config.xml and README) -> measured through the real verify: with the flag on, #302's own sentence gives [1]=true [2]=false. It REMOVES the rule and reinstates the symptom on citations 2..N. · cost: 1 cycle
- "The demotion should be mode-uniform" -> measured: with entailment off there is no Tier-2 refusal to withhold, so it only cost correct citations their verdict (~10 of 30 citations in the issue's sweep) for no defect removed. Gated on entailment. · cost: 1 cycle
- "all 8 of the issue's false cells are in this shape" -> the issue's own closing bullet folds the co-citation sub-shape into its 10/8, so the fix reaches at most 7. · cost: 2 cycles (stated wrong, then reconciled between two homes)
- "neither tier runs for a compound unit" -> false on the eagerly scored path: claim selection still embeds to choose between candidates. · cost: 2 cycles

## Raised by a fresh agent, missed by the author
- [harden c5] The judge-YES cell was unreachable by the whole suite because every compound test drove ConjunctionAwareJudge, a stub that always refuses a conjunction — so the rule's central premise ("a correct judge says no") was assumed, never exercised, for four cycles. This is the finding that reversed the design. · blocking-equivalent · cost: 4 cycles of work built on it
- [harden c3/c7] Two production sites produced the same flag and only one was pinned; and later, the Disposition precedence (UNVERIFIABLE over DEMOTE_ONLY) was the refactor's headline claim and no test held it — swapping the ternary arms left all 1362 tests green while changing published verdicts and spending embeds.
- [harden c6] The two-array split kept "decided once" for one arm and silently lost it for the other (the reference-group reason was re-derived in Pass 2) — the #110/#122 shape. The agent implemented the Disposition enum and proved the exclusive kill survived.
- [pr-harden r1] The co-citation carve-out was pinned only at TWO markers; deleting the marker-loop advance left the whole reactor green while every 3+-marker co-citation misclassified — the exact shape normalizeSlashCitations manufactures. · non-blocking · cost: the finish round
- [pr-harden r1] Read the actual frontend consumer and confirmed `false` renders a red Unsupported badge where `null` renders none — the empirical basis for the fix being user-visible, which no in-repo evidence could supply.
- [verifier] The co-citation sub-shape published `false` on a correct active order live: not a #302 miss, but the same clinician-visible harm through a different door. Sharper framing than the PR had.

## Where a skill blocked or contradicted this run
- pr-harden §6 ("never a server that was already running when the run began", "pick one with nothing listening") vs resolve-ticket §1 pre-flight (a running, attributable `java -jar openmrs-standalone` IS a usable target; the sixth run's mistake was treating that as a blocker). Both standalone ports were held by our own processes. Resolved in favour of resolve-ticket's measured correction, with the non-ours pid fenced off explicitly in the brief. The two sections should be reconciled.
- The Stop gate reads ~/.claude/pr-harden-state.json, but during Step 7 (/harden) the awaits were being written only to harden-state.json, so the gate fired mid-run with agents live. Both files need the await during the harden phase of a resolve-ticket run.

## Declined
- (none blocking) The reviewer's non-blocking finding was implemented in the finish round.
- Preferring a non-compound candidate in selectClaim, to rescue a citation whose argmax lands on a compound unit when a single-claim candidate exists — declined and recorded as a residual: it would break the documented parity between selectClaim's choice and verdictTier1's, and the cost of leaving it is a verdict withheld rather than a wrong one.

## Assumptions review overturned
- "The ticket's grounding GPs are the shipped defaults" -> config.xml ships enabled=false and entailment.enabled=false; those were that standalone's values. Recorded in the plan, and the PR body says "with grounding and entailment enabled" rather than "by default".
- "#284's shape is untouched by this fix" -> partly wrong: the inline composite safety answer the module's own few-shot teaches IS a compound claim unit, so the fix reaches it. #284's measured case (array-only, one inline marker) is genuinely untouched. Corrected at the refutation gate before any code.

## Under-captured at write time, added during the retro that read this record
- Three harden Phase 2 agents reported findings against a head the run had already moved past,
  because they were spawned before the previous cycle's commits. Fixed mid-run by telling each agent
  to confirm the sha first. pr-harden guards this via `reviewed_shas`; harden has no equivalent.
- Both agent deaths in this run (one connection-refused, one stall watchdog) happened during
  worktree-isolation setup, and both retries succeeded with isolation dropped. n=2 within one run, so
  not independent evidence.
- Provenance note: these two were in the run but not in this record when it was written; the retro's
  refuter caught the retro citing them to this file for text it did not contain. Added here as
  capture, after the fact, and flagged as such.

## Process failures worth recording
- A `git checkout -- <file>` used to undo a mutation probe reverted four UNCOMMITTED production edits to HEAD, and the empty `git diff --stat` afterwards read as "restored" rather than "reverted". Shipped a commit whose message described changes not in its diff; caught two cycles later by an agent grepping for a symbol the message named. Restore from a `cp` backup, never `git checkout`, when the file has uncommitted work.
- Guard attribution was wrong three separate times (a case named as a guard that could not observe the branch). Twice the correct attribution was already written in the test's own comment.
- A corrected claim was applied to N-1 homes four times running, twice in the same commit whose message announced the correction. The method that finally worked: grep the OLD phrasing and the REPLACEMENT, across every file type, and re-grep after.
