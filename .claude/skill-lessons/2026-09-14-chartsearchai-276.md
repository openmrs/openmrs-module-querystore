# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #276 / PR 424 · 2026-09-14
outcome: converged (pr-harden, round 1 blocking=0) · harden: did-not-converge (override after cycle 7)
rounds: 2   cycles: 7   verifier: ran twice (works at runtime, pre-merge and on the merged head)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-276/01726413-2df9-44eb-bfcd-259603ab938d.jsonl

## Refuted by measurement
- "no bundled dataset files a substance as more than one row" (published in README, ADR and javadoc as the reachability bound) -> `ddinter` sets `substanceName` on EVERY entry; what it never sets is an age band. Contradicted a figure already in the tree (129 multi-row substances). · cost: 1 harden pass
- "statesWord's boundary fails toward silence in the direction that matters" -> `statesWord("...2.5 mg/day...", "5 mg/day")` is TRUE; a decimal point is neither letter nor digit, so a ceiling matched a fragment of a larger number and produced a FALSE REPORT on a published key. · cost: 1 pass, and 4 more wordings after it
- The needle rule, four successive wordings, each refuted by a different reviewer with a real input: refuse-any-separator lost a list comma; refuse-between-two-digits lost a naked decimal; refuse-unless-a-letter-precedes lost a comma after `)`; the same for the full stop lost every other closing mark. Ended only when the question changed KIND (does the stop BEGIN a token) instead of enumerating characters. · cost: 4 passes
- "the spellings sort 200, 600, 60" -> they sort 200, 60, 600 (space 0x20 < '0' 0x30). A hand-derived sequence published in two homes. · cost: 1 cycle
- "each alternative placement reddens more than one case" -> one of the three reddens exactly one. · cost: 1 cycle

## Raised by a fresh agent, missed by the author
- [harden p1] `ArchitectureGuardTest` selects RecordMapping constructors by descriptor TAIL; a new rung or an appended parameter breaks a selector. Forced the parameter INTO the existing widest rung. · blocking at the gate · cost: caught pre-code
- [harden p1] Instruction-file byte budget: 183 bytes of headroom, and the guard's policy forbids raising it in the commit that overflows it. Forced a trim first. · blocking at the gate · cost: caught pre-code
- [harden p2] The decimal false POSITIVE (above) — the direction the check must never fail in.
- [harden p2] "saying nothing about this patient" was false: the ceilings are `bandForAge`-selected, so they narrow her age band.
- [harden p2] The canonical family enumeration (`CitationGroundingVerifier`) was stale while its README copy was fresh — the exact inversion that instruction exists to prevent.
- [harden p5] NBSP: `Character.isWhitespace` is false for U+00A0/2007/202F, the three spaces typeset copy uses to hold a number together.
- [harden c3] Every one of the 18 cases ran on a TWO-ceiling record, so "report the first ceiling the answer states" decided nothing; a mutation reporting the LAST passed the whole suite.
- [harden c6] The live class was counts OUTSIDE the diff that the code change falsified — structurally invisible to every diff-scoped sweep the run made.
- [pr-harden r1] `"4000,300 mg/day"` (comma-joined, no space) is still refused — the one false-report residue the javadoc does not name. Non-blocking; filed as #425.
- [pr-harden r1] A test's failure message asserts the opposite of what its own comment says. Non-blocking; #425.

## Where a skill blocked or contradicted this run
- harden:Termination — the cycle gate is edit-based, and five consecutive cycles found only documentation counts, each introduced by the previous cycle's own documentation edit. The rule kept the run going long after the CODE converged (cycle 1). Took the labelled override after cycle 7. What broke the loop was not another cycle but changing the fix from correcting counts to REMOVING them.
- pr-harden:FINISH — "do not edit the cleared sha" sent two real non-blocking findings to a follow-up issue rather than into the branch. Correct, and worth noting: the branch ships with a known named residue.
- Twice in one run I orphaned a javadoc by inserting a member above an existing one — the failure my own memory entry records. The second time it stranded the canonical statement of a rule three `{@link}`s point at. Anchoring on a signature rather than after a closing brace is what does it.

## Declined
- Unify the two memo wrappers in `DosingCeilingFidelityCheck.answerStates` and `SafetyFindingSeverityFidelityCheck` — if we ship without this, nothing breaks: they now memoise over two DIFFERENT scans and their key rules differ for a documented reason (an operator-authored rating folds case; a machine-generated numeric phrase must not, and folding it would hide the invariant `dailyCeiling`'s javadoc states). Unifying would reach into a sibling check this ticket does not touch.

## Assumptions review overturned
- "The subject row's ceiling" is identifiable -> it is not, on the ticket's own arrangement (both rows claim the alias equally), so the check asks the question that IS deterministic. Held from the plan through the gate and never overturned.
- The needle can be a bare number -> it must carry the record's own unit, and then also refuse number fragments. Overturned across harden passes 2-5 and cycle 5.

## Addendum — the base moved after the PR was marked ready
`main` gained #422 and #423 mid-review and the PR went CONFLICTING. Both hazards
`pr-harden` Step 1 documents fired at once, and neither is a textual accident:

- **ADR decision-number collision.** `main` took Decision 95 for #337's elision
  fix; this branch had taken 95 too. Renumbered to 96. The sweep had to be by
  NUMBER and then disambiguated one reference at a time — five surviving
  "Decision 95" citations in four files all legitimately mean main's rule.
- **Shared instruction-file budget.** main spent 139 bytes of the headroom this
  branch had already trimmed to fit inside; merged CLAUDE.md came out 126 over
  the cap. Trimmed own prose again rather than raising the number.
- **The ADR's own INDEX** had neither decision — main's shipped without one two
  commits earlier, because the guard checks headings and not the index. Exactly
  the residue pr-harden records from #348/PR369 and #280/PR383.
- **The clean merges were where the risk was.** Three files auto-merged; all of
  this branch's claims about them had to be RE-MEASURED, not re-read. All held.

## Raised by a fresh agent, missed by the author (round 2, merge)
- [r2] The trim's own justification was false — "every sibling bullet in that
  section carries the clause" is true of exactly one sibling. The rule survives
  at both call sites, so it relocated rather than vanished, but the instruction
  file lost the imperative. · non-blocking · #425
- [r2] CLAUDE.md left with 8 bytes of headroom; the next instruction edit of any
  size trips the guard. A budget raise belongs in its own change. · #425
- [r2] A pointer in my own merge commit message names the wrong javadoc for a
  trimmed rationale. Corrected in the PR body; the commit message is immutable.
- [r2] numericFragment's javadoc calls an attached '.' a sentence-ending full
  stop, which #422 made partly stale (a 3+ dot run is now a marked cut).
  Behaviour measured correct on the merged tree. · #425
