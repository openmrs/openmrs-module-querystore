# resolve-ticket (+ harden, pr-harden) · openmrs-module-chartsearchai · #250 / PR 333 · 2026-08-30
outcome: converged
rounds: 2   cycles: 5   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-250/1c475c8b-934b-4960-9f97-dea71b43a26d.jsonl

## Refuted by measurement
- "`substanceKey` includes the substance name, so a family holding plain `Foo` beside a self-naming `Foo (ophthalmic)` is not ONE family" -> false; all rows of a family share a substanceName, so the pathological pair IS constructible and the shipped A/Vietnam family is exactly that shape. · cost: 1 gate pass
- "Elected rows carrying a route-qualified row: 10 -> 9" quoted without its unit -> the syntactic reading goes 10 -> 11 and the predicate reading 10 -> 9; the correction is what makes the two diverge. · cost: 1 cycle
- "The family that moves the 119/10/7 triple is A/Vietnam" -> tick-borne. Second attribution claim of that shape to be wrong; the claim was deleted rather than corrected again. · cost: 1 cycle
- "Ozanimod is the one partner whose two rows carry different note text" (4 homes) -> 199 of 227 shared partners differ; ozanimod is the one where the CORRECT row's note is fuller. Surfaced an unrecorded cost: 194 partners lose a corticosteroid dose-threshold clause from the injected record. · cost: 1 cycle
- "#297's ceiling is 365 renaming tokens" -> this change moves it to 364; the A/Vietnam rule token IS the newly elected row's name. Reproduced on the published base. · cost: 1 cycle
- "The two KB-wide cases form a ratchet — the property assertion guards the golden list" -> false structurally: a parenthesised row reaches that assertion only by having been elected over a plain sibling, which requires the exact equality it asserts. · cost: 1 round

## Raised by a fresh agent, missed by the author
- [gate 1] The fix leaks an identity test through an ungated rung; the same-substance gate is a measured decision · blocking · cost: 1 gate pass
- [gate 2] The `outranks` sweep re-expressed the grouping, and had no column for the substance swap the winner decides · blocking · cost: 1 gate pass
- [c1] The question-pair arm's substance swap was a behaviour change with no test · substantive · cost: 1 cycle
- [c1 quality] The change creates a residue class (a route-qualified substanceName beside a plain row) that nothing named or pinned · substantive · cost: 1 cycle
- [c2 reuse] `canonicalRow`'s `@return` carried the other reading's count, 190 lines from the javadoc that owns it · cost: 1 cycle
- [c3 sweep] The change moves a pre-existing figure in ADR 51 and CLAUDE.md, two files the diff never touched · cost: 1 cycle
- [c4] "one 119-character clause" — the clause is 117; the residue is a collapsed double space · cost: 1 cycle
- [r1] The "ratchet" claim, measured false with a synthetic family through the real parser · blocking · cost: 1 round
- [r2] A "stated once" uniqueness claim falsified by a second attribution twelve lines away in the same file · non-blocking · cost: 0

## Where a skill blocked or contradicted this run
- pr-harden:"After any mutation rebuild with clean" — the orchestrator hit exactly this: `api/target/classes` held a mutated class from a revert-check, and two probe runs silently measured the mutation. Caught only by an impossible answer. The rule is in the skill for agents; the orchestrator needs it too.
- harden:Phase 2 — the efficiency lens was not re-run in cycle 2 because zero executable lines had moved since it measured; stated as a labelled reduction rather than a silent skip.

## Declined
- (none — every finding in both rounds was implemented)

## Assumptions review overturned
- "The fix is behaviourally invisible outside `canonicalRow`" -> `outranks` is a second SITE of the same defect, not a risk to clear; the tick-borne chip already rendered a paediatric row's prose under the substance row's name (gate 2 / cycle 1)
- "The residue is unreachable because the pathological family cannot exist" -> it can; it is named by test and adjudicated by a person (gate 2 / cycle 1)
