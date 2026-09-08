# pr-harden 0.18.0 · openmrs-module-chartsearchai · PR #369 (issue #348) · 2026-09-03
outcome: converged (round 7 reported zero blocking findings; PR marked ready)
rounds: 7   cycles: n/a   verifier: ran once (prompt A/B, works at runtime); rounds 2-7 changed no production bytecode so it still covered the head
merges with main: 4 (#366, #368, #367, #371) — the ADR decision was renumbered four times, 68->69->70->71->72

NOTE: an earlier version of this record said did-not-converge after round 6. The operator asked for the
run to be taken through round 7, so a fourth merge was done (the PR had gone CONFLICTING) and round 7
ran: zero blocking, one non-blocking (a count in the tripwire javadoc), fixed in 6e39a706.
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-chartsearchai/7eac6885-815e-4652-a1f3-bfffac760478.jsonl

Entered by hand: the ticket's `resolve-ticket` run was killed by a host restart with 7 commits UNPUSHED
and no PR. Pushed the branch first, then opened the draft PR and ran the loop.

## Refuted by measurement
- "The two guard cells are unreachable in effect rather than merely untested" (round 5's decline, in a
  test javadoc AND the ADR) -> round 6 reached both end-to-end with an operator-format fixture. Every
  STRUCTURAL leg verified and the shipped-KB invariant still held, but the premise was about the SHIPPED
  data while the claim was written UNSCOPED, and the KB is operator-configurable. Round 5's tie argument
  fails because the in-play row raises no allergen-arm chip at all when its codes relate it to nothing,
  so there is no incumbent for `ContraindicationChips.add` to keep. · cost: 1 round
- "#107's arms: branch head vs the same build with only the prompt hunks reverted" (the reviewer's own
  specification of the measurement) -> invalid HERE, because the injected clause and its prompt branch
  are one change; reverting only the prompt produces a third behaviour neither arm ships. Caught by the
  agent implementing it, not the one specifying it. · cost: 0 rounds
- "assertCurrentMedicationBranch carries the mutation holding each half" (CLAUDE.md and the ADR) -> it
  held the REFUSAL half only; three permission-shaped rewrites were green. Both documents overclaimed. · 1 round
- The ADR's stated cause for a new WARN -> right count (0 -> 4), wrong mechanism: the WARNs are the
  answer reciting then misquoting DDInter MECHANISM prose, not a strength clause clearing the word floor. · 1 round
- r1-3, declined on reasoning, then run as a third A/B arm and REFUTED: adding the screening shape to the
  paragraph's scope list gives the cleanest two-order leads AND drops a finding from the eight-order cell. · 0 rounds
- My own instruction to the round-1 verifier that the clause was unobservable -> `reference_slice_chars`
  measures the injected slice to the character; it found the channel despite being told to report
  `could not determine`. · cost: 0 rounds
- Two agents disagreed on the prompt constant (8600 vs 8534); the 8534 was a source-literal count with
  escapes unresolved. A source-only measurement CANNOT work here — the prompt concatenates clause
  CONSTANTS, so ~210 chars come from referenced fields. Only a compiled measurement is valid.

## Raised by a fresh agent, missed by the author
- [r1] The ticket's own acceptance condition ("wants the #107-style A/B before it ships") had not been
  met, and the ADR's excuse (the scored gate cannot express a screening question) was TRUE but the README
  documents the substitute the project itself adopted in that situation. · blocking · cost: 1 round
- [r1] A prompt guard asserting only that a sentence names its class and contains "open" accepted a
  rewrite instructing a refusal. Whole api suite green. · blocking
- [r2] The ranking sentence, one sentence over from r1's, guarded for existence and not ORDER — inverting
  it demotes a proposal refusal below a change-of-therapy statement, green. · blocking
- [r2] Four sites in the tree still said the A/B was owed AFTER it ran, one of them naming a REFUTED arm
  as an open one — so the record actively directed the next maintainer to make a working cell worse. · blocking
- [r4] The referent is passed at two call-site PAIRS and only one was pinned; both directions green at
  the other. [r5] Four SITES, 3 of 8 cells green. [r6] Three RUNGS, one green. Same defect at three
  successively finer granularities, each found only after the previous was fixed. · blocking x3
- [r4] A stale ADR cross-reference that survived a renumbering sweep because the reference was LINE-WRAPPED
  (`ADR Decision` on one line, `70's` on the next), invisible to a phrase grep. · non-blocking

## Where a skill blocked or contradicted this run
- `pool-run --claim` RESET the worktree to main both times it was used (#353 and #348), while warning that
  a PR already existed. Harmless only because the branch was pushed first.
- Orphaned harden-lens worktrees held the branch and blocked checkout in both tickets. For #348 three of
  four were NOT byte-identical to the head: they held dead agents' mutations — a swapped prompt clause, a
  deleted clause, and a `System.err.println` probe with an added map. A killed run leaves these and
  nothing reaps them.
- The ADR decision number collided on EVERY merge (68 -> 69 -> 70 -> 71, three renumberings in one PR),
  because `main` took the next free number each time. Each sweep is a DISCRIMINATION, not a replace.
- `adr.md`'s TOC is a merge hazard: in merge 1 git silently kept only one of two lines added at the same
  insertion point, dropping main's, with nothing in the build failing.
- Two agents died on transient API errors (one 429, one 529). One died AFTER committing its merge and
  before reporting, so the merge had to be verified from outside — that is the case the snapshot rule does
  not cover, because the work was legitimately committed rather than left in the tree.

## Declined
- r1-3 (non-blocking): add the screening shape to the safety paragraph's scope list. "If we ship without
  it, the branches are reachable on the reported question only through a scope the paragraph does not
  state." Declined as a second unmeasured prompt variable that would make the owed A/B unable to attribute
  either — then RUN as a third arm and refuted, so the decline is now backed by measurement.
- 2 of 8 guard cells, round 5 (later overturned): see Refuted above.

## Assumptions review overturned
- "A prompt-facing change cannot be verified at runtime" -> `reference_slice_chars` with a controlled
  +10-char display delta gives the clause item count; three verifiers used it, one predicting 789 and 898
  and observing both exactly.
- "The fixed-dose-combination population is what this reaches" -> the stock CIEL name already satisfies
  the NAME leg, so the bridged leg only helps deployments whose locale-preferred name does not spell the
  constituents. A verifier had to RENAME two demo concepts to make the case discriminating at all.
- "Six of eight cells held" (a count in two places) -> removed, because it tracks the code. Both the ADR
  and the javadoc now record the METHOD: mutate a site or a rung and read the failures.

## Round 7, and what the fourth merge turned up
- Round 7 re-proved the referent coverage by mutation across FOUR granularities — call site, chip rung,
  the `strengthClause` consumer, and the arm literals — and found no surviving cell. It also enumerated a
  granularity no prior round had named (the consumer's three ternaries x 2 directions).
- Its one finding was a count that tracks the file ("pruned from four bullets"). Worth recording because
  the count is genuinely AMBIGUOUS rather than merely wrong: four bullets shrank keeping their opening
  words and a fifth shrank across a RENAME, so the answer depends on whether a renamed bullet is the same
  bullet. I measured 4 and the reviewer measured 5; the reviewer was right and my prefix-keyed script was
  the flaw. Dropping the number was the fix either way.
- The fourth merge found that **`main` has been missing its own Decision 70 TOC entry since #367** — an
  earlier merge dropped it and nothing caught it, because `ProjectInstructionsGuardTest` validates
  CLAUDE.md-cited decisions rather than TOC completeness. A build guard over the ADR's own TOC is owed;
  every audit that has caught this was a throwaway script.
- The merge agent proved its own comment renumbering bytecode-neutral EMPIRICALLY — compiled both
  variants against the real resolved classpath and diffed the emitted class files — rather than arguing
  that comments cannot change bytecode. That is the standard to keep for "no production change" claims.
- A figure I nearly published wrong twice: the prompt constant cannot be measured from source, because it
  concatenates clause CONSTANTS (~210 chars come from referenced fields). Only a compiled measurement is
  valid. Same trap on the clause lengths — a regex that stops at the first string segment gives 33/6 for
  a clause that is really 104/19.
