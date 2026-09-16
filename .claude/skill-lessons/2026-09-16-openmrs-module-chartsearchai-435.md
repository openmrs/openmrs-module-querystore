# harden · openmrs-module-chartsearchai · #435 / PR 436 · 2026-09-16
outcome: converged (cycle 10, measured 0 edits)
rounds: n/a (not a pr-harden run)   cycles: 10   agent passes: 14 (9 in cycle 1, then 1 per cycle)   verifier: n/a
context: no compaction · 591k/1m at the end of the run
capture: hand-written by the orchestrator. `harden` appends no run record, and this run had no
`ticket-pool` driver — the slice was a security finding handed over as a local markdown file, worked to
a PR, then hardened. Without this file a 10-cycle run would have reached the retro as nothing.

## What the run was
A CWE-93 SSE frame-injection fix: `writeSseEvent` split payloads on `\n` where the event-stream grammar
ends a line at CRLF, CR or LF, so a lone CR in model text forged a `done` event with `grounded: true`.
One expression changed in production. The fix was written before cycle 1 and **never changed again**.

## Refuted by measurement
Twenty claims, all written by the author, nearly all in prose describing the fix rather than in the fix.
- "README and ONBOARDING instruct a client where a line ends" -> neither file had ever said so; the docs
  state the server's side · cost: 1 cycle-1 pass
- "a mismatch that is only that byte" -> a CRLF collapses to ONE LF, so the streamed text is SHORTER; a
  deletion, not a substitution · cost: 1 pass
- "no client can tell a forged field from a real one" -> the decision's own frame check is a client-side
  procedure that refuses those bytes · cost: 1 pass
- "the streamed text is not byte-identical to the answer" (written to REPLACE the above) -> an answer
  whose breaks are LF streams back byte-identical, the ordinary case · cost: 1 pass
- "a second bound: text ahead of the CR makes the forged JSON unparseable" -> **two CRs in a row defeat
  it** — the pair ends the genuine frame AND its dispatch line, so the forged fields open a clean frame
  of their own. This one was a defect in the SECURITY ANALYSIS, inherited from the finding report's own
  worked example · cost: 1 cycle, and it earned it
- "the frame check catches every field a forgery needs" -> passes on the run-of-terminators shape; the
  event list is what sees it. Written as a universal, narrowed once, still a universal · cost: 1 cycle
- "dispatched() differs from String.join" -> identical on every input; what differs is the formulation it
  replaced · cost: 1 pass
- "three of the five channels" -> a denominator tracking the code, in three homes; two fresh readers took
  it for a count of wire events · cost: 1 pass
- "the first of those controls exists to prevent it" -> lands on the control indifferent to that mutation;
  introduced by the cycle-5 rewrite of a correct three-sentence version · cost: 1 cycle
- "the one thing in this class that frames a payload itself" -> false since cycle 4 added a second
  hand-framed case, in the same cycle that repaired the ADR's copy of the same claim · cost: 1 cycle
- "would leave all four green" -> the class had grown to eight; six stay green · cost: same cycle
- "This is the assertion that reddens on it" -> a stripping writer also reddens the terminator case, which
  does not go through that helper · cost: 1 cycle
- "That mutation left every other case in this class green" -> a per-mutation tally, written by the cycle
  that had just deleted one 120 lines above · cost: 1 cycle
- plus: "every real client" (the measured reference client is the counterexample), "both
  controller-streaming test classes" (seven do), "more than a dozen classes"/"joined by a dozen more"
  (13 and 12, in the javadoc explaining why counts are not kept), "contradicted the claim two members
  later" (positional, wrong on either reading), "the decoder there is this class's original one" (the
  slice replaced it and deleted the matching sentence in the same diff), "alone among these characters"
  (its antecedent contains LF), "an unguarded null would stream the word null" (it throws and the client
  gets an error event instead of the answer).

## Raised by a fresh agent, missed by the author
- [c1] A **second LF-only frame decoder** in the same test package — the keep-alive test's private
  well-formedness assertion — as blind to the finding as the code it checked · cost: 1 pass
- [c1] `assertCarriedWhole`'s predecessor: a `contains()` that a writer STRIPPING terminators satisfied
  while deleting the clinician's text — every security assertion green · cost: 1 pass
- [c1] The scope claim ("the composed events escape a CR through Jackson") was argued, never driven ·
  cost: 1 pass
- [c2] Widening the terminator set is as silent as shrinking it: `\R` passes the whole suite and turns a
  form feed in the clinician's answer into a newline · cost: 1 pass
- [c4] The run-of-terminators shape, above · cost: 1 cycle
- [c5] The `-1` split limit was unpinned: dropping it deletes a trailing line break from the answer with
  the whole suite green, because no payload in the class ended with a terminator · cost: 1 cycle
- [c5] A stale mutation tally in the ADR that had gone stale inside one cycle
- [c9] A pre-existing, unrelated defect found by the sweep and filed as #438: a non-BMP code point
  streamed across two chunks reaches the clinician as `??` because each frame is encoded on its own

## Where a skill blocked or contradicted this run
- **harden:Termination — the dominant cost of the run.** 12 of 19 harden commits changed zero
  non-comment `*.java` lines (counted over `3a4cc1af..sse-frame-injection-cr`, comment and blank lines
  excluded). Only cycles 4, 5 and 7 carried any code at all; a line figure for those three is not
  recorded here because three defensible countings disagree. Each documentation-only cycle bought a mandatory successor cycle.
  This is the ledger's parked *Prose-correction cycles* class, and the recorded reading holds: the
  shipped tactic is DELETE, and the run replaced instead of deleted for four cycles before switching.
  The recursion stopped at deletion plus species-sweeping, both already in the skill.
- **harden:199-201 — the documentation-pass route SHIPPED a false claim.** Cycle 7 was
  documentation-classified and confirmed by a single agent; it rewrote the `assertCarriedWhole`
  paragraph and shipped "This is the assertion that reddens on it", refuted by measurement at cycle 8
  (`git show 7a080eed` carries both the removed and the added wording). Deletion rather than rewording
  would have caught it. This is the REOPEN condition recorded against the killed P1-R routing clause.
- **harden:Phase 2 — the four-lens pass found nearly everything; the marginal pass found prose.** Cycle
  1's four lenses returned two structural findings, a measured cost figure and a measured cross-repo
  fact. Cycles 6-9's single agents returned 15 prose corrections between them and, each time, stated
  independently that the code had converged.
- **harden — no run record.** This file is hand-written; see `capture` above.
- **Routing.** The slice already had an open PR when `/harden` was invoked. `pr-harden` terminates on
  "reviewed with zero blocking findings" (a finding-class gate); `harden` terminates on zero edits. No
  claim is made here about which would have been faster — the counterfactual was not run.
- **Tooling, all previously recorded:** `-Dtest=A+B` selects nothing and reports BUILD FAILURE (hit once,
  silently voided one mutation measurement until re-run with a comma); `git checkout --` discarded three
  uncommitted javadoc edits during a mutation probe, recovered from `git-restore-backup.sh`'s copy —
  the hook worked and its printed destination was what made recovery possible.
- **New, not previously recorded:** a foreground `mvn -pl omod test` run concurrently with a background
  `mvn clean install` in the SAME tree invalidated the background build (it cleans the module the
  foreground run is compiling into). Cost: one full build re-run, and a moment of treating a green
  result as evidence when it was not.

## Declined
- Four verbatim copies of a `references`-from-an-event helper re-implementing `SseEvents.dataOfType` in
  sibling test classes: prototyped and measured green by a review pass, left out to keep the PR's blast
  radius at the security fix.
- Making a CRLF survive a chunk boundary, and the #438 encoder fix: both need cross-frame state in a
  per-event writer, which is a change to the writer's contract rather than a framing correction.
- A root `CLAUDE.md` bullet for the new single-writer rule: the file sits 3 bytes under its 25,000-byte
  budget and the guard reddens by name instead, so the rule lives in javadoc plus ADR Decision 101.
- A `grounded`-channel case for the Jackson scope control: the property is the mapper's and identical on
  both paths, so a second case would pin the mapper twice and the second writer not at all. Named as
  residue instead.

## After-the-fact capture (not observed during the run)
- At retro time the shared chartsearchai clone was found on `finding-partner-coverage-log-phi`, with
  another pipeline run's harden rounds committed on it, while this run's branch and its remote were
  intact at `89706112`. Nothing was lost and the analysis continued against the ref rather than by
  checking out. Flagged as after-the-fact because no part of the run observed it, and the retro's
  refuter notes this class already carries a count of its own.
