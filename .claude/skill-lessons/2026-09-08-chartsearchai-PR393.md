# pr-harden · openmrs-module-chartsearchai · PR 393 (issue #388) · 2026-09-08
outcome: converged
rounds: 3   cycles: 0   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-393/49d154db-534c-419b-8f6f-dea64e4ab2a8.jsonl

Entered via `/resolve-ticket` with a PR URL rather than a ticket URL. That skill's own
"when NOT to use" rule settles it — a PR already exists, so the entry point is pr-harden —
and the handoff cost nothing: guards, base check and state entry were re-derived here.

## Refuted by measurement
- (orchestrator, r1 body patch) "swapping the pass order ... ; widening the second pass's input
  to every allergen reddens those and further cases besides" -> widening leaves all three named
  classes GREEN; it is caught by CombinationAllergenResolutionTest (#193) and
  RecordedAllergenChipNameTest (#268). Mechanism: for a single-implied allergen the widened pass
  raises its chip on the IDENTITY key at SAME_CLASS rank and ContraindicationChips.add drops it
  against the incumbent, so every #388 case is structurally blind to that mutation · cost: 1 round
- (PR body, pre-existing) "api 1995 tests" -> 1997 after r1 added two cases · cost: 0 (caught in-round)
- (r1 reviewer, cited to orchestrator) declining-side attribution "#193/#195" -> #193 and #268 are
  what the two test classes actually cite; #195 is a different fixture's issue · cost: 0 (checked
  before writing)

## Raised by a fresh agent, missed by the author
- [r1] The ordering contract is published over BOTH cross-reactivity arms (README, nested CLAUDE.md,
  ADR 82) but every test drove only the ATC-class arm. Proved by mutation rather than asserted:
  hoisting ONLY CrossReactivityGroup.sharedGroup into pass one leaves chip text byte-identical,
  moves emission order, restores #388's defect for group-related allergens, and passes the whole
  suite (api 1995 / omod 167, 0 failures) · blocking · cost: 1 round
- [r2] The orchestrator's own corrected mutation ledger in the PR DESCRIPTION mis-attributed the
  guard for pass two's input width — the body's closing line is "mutate the pass split and read the
  failures", so a maintainer simplifying notThisDrug away (the cheapest edit on the diff) would have
  watched the named tests, read them green, and shipped the loss of per-allergen
  identity-over-class precedence (measured: Lamivudine gets both its identity chip and a spurious
  same-J05AF-class chip from one combination record) · blocking · cost: 1 round
- [r3] DirectAllergyContraindicationTest.anEarlierUnrelatedAllergenDoesNotHideTheDirectOne keeps
  "the token order is therefore load-bearing" while #388 made its stated reason unconditional;
  flipping the tokens on HEAD reddens nothing · non-blocking · filed as issue #394

## Where a skill blocked or contradicted this run
- pr-harden:"The round" step 1 — the guard "if the new head EQUALS the previous round's, stop and
  establish why" fired at r3 and the enumerated causes did not cover this one: r2's ONLY finding
  named the PR description, which is not in the tree, so no commit was made and the sha legitimately
  did not move. Handled by briefing r3 that the tree was byte-identical and the change was to the
  body. The skill already says a description finding "still counts as the round's fix and the round
  proceeds normally" — but that sentence and the identical-head guard are in different sections and
  do not reference each other.
- pr-harden:FINISH — "re-derive the PR description against the merging head" is in tension with
  "FINISH does not edit the cleared sha" when the body has just been reviewed clean by r3 and every
  figure in it has been re-measured on that head. Resolved by re-checking each figure rather than
  rewriting the prose, on the ground that a wholesale rewrite would substitute unreviewed text for
  reviewed-and-verified text. Worth stating explicitly in the skill either way.
- pr-harden:State — every Agent call in this run launched ASYNC despite the skill's "collect it in
  the same turn / the Agent call RETURNS the report". Attended session (remote-control, no unattended
  marker), so the completion notification re-invoked the orchestrator each time and nothing was lost;
  an unattended run under the same harness behaviour would have depended entirely on the gate.

## Declined
- (none — no finding was declined in any round)

## Assumptions review overturned
- "chips.subjectOf(ref) moving below the new early return may change behaviour" (orchestrator's own
  reading of the diff, deliberately not acted on) -> r1 and r3 both established it is a
  side-effect-free memo over inputs fixed for the pass, so the deferral is cost-only. Not
  pre-empting round 1 with it was the right call.
- "the live sixteen-chip / one-transposition measurement in the body is unverifiable from here"
  (r1 and r3 both reported they could not reach it) -> the FINISH verifier reproduced it exactly on
  the assigned standalone, both sides with separately hashed loaded class bytes.

## Environment, reported not repaired
- <standalone-8082>/restarted-by-slot4.log was being written during this run: a co-tenant (slot-4)
  has been restarting the instance assigned to slot-2 via $OPENMRS_STANDALONE_HOME. Nothing of this
  run's measurements was disturbed, but a concurrent redeploy by that slot could invalidate a
  reading, and the per-slot standalone assignment is what is supposed to prevent exactly that.
- The inherited instance was serving 404 on every chartsearchai REST path from a Sep-7 omod while
  /ws/rest/v1/module/chartsearchai reported started=true, startupErrorMessage=null. So the module
  status endpoint is not evidence that a module's controllers are mapped.
