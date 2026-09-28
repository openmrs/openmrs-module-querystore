# pr-harden 0.37.0 · openmrs-module-chartsearchai · #402 / PR #544 · 2026-09-28
outcome: converged
rounds: 4   cycles: 0   verifier: ran (works at runtime, 3 runs: r1 667d1910, r2 ba929b8e, r3 ccb72940)
context: no compaction surfaced · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-chartsearchai-544/c3fd0664-f6fd-4bb0-9dbe-b81585926c1e.jsonl
entry: interactive session, not pool-run. Started from the draft #544 that resolve-ticket's 2026-09-26 run left with rounds 0, after the owner split #402 (the lead criterion moved to #548). The orchestrator merged main and renumbered the ADR decision 121 -> 123 before round 1.

## Refuted by measurement
- "the locally-applied gate (r1's fix) closes the gel/oral defect without cost" -> r2 measured the gate firing without evidence for substances the data files only under locally applied groups (117 of the 1,700 coded shipped-KB substances, e.g. salicylic acid, sulfasalazine), which brought #402's defect back · cost: 1 round
- "substancesOf(orderEntries) answers 'is this drug hers'" (the direction's item 1 formula) -> r3 measured findForActiveOrders over-resolving along three legs (a brand two rows share, a level-5 code filed under two substances, a bridged concept filed on several), which fails OPEN for the referent (Nexium 40mg + clopidogrel, "Can I give her omeprazole?" lost its refusal). Reproduced live by the r3 verifier on demo data it created · cost: 1 round
- the orchestrator's renumbering sweep ("every reference moved") -> r1 found five references it missed, because the sweep searched the phrase `Decision 121` rather than the number: "Decisions 72, 44, 110, 112, 121" and a line-wrapped one · cost: 0 rounds (fixed in r1)

## Raised by a fresh agent, missed by the author
- [r1] a locally applied order (Voltaren gel) made an oral proposal read as her current medication, and a Major finding lost its refusal · blocking · cost: 1 round
- [r2] the r1 gate vetoed without evidence (see above) · blocking · cost: 1 round
- [r3] over-resolution of her orders fails open for the referent (see above) · blocking · cost: 1 round
- [r4 notes] a surviving mutant in soleSubstanceFiledUnder's guard, which the shipped row order hides; the name leg's free-text over-reach, shared with every findForActiveOrders consumer (#293); stale wording in SafetyWarning.isAboutACurrentMedication's "one home" false-list and two test comments · non-blocking · unfixed, named in the PR description

## Where a skill blocked or contradicted this run
- pr-harden:State (keying): the Stop gate keys on the SESSION's cwd. An interactive run launched from the main checkout wrote its entry under the PR worktree's path, so the gate could not see it until EnterWorktree moved the session into that worktree. After the move, the worktree-isolation guard refused compound shell commands and `$PPID` interpolation. The orchestrator had to pass a literal pid to gate-state and split every git command, and subagents hit 12 refusals across roughly 1,000 tool calls.
- (no skill section) effort: the session ran at `/effort max`, and every subagent inherited it (`"effort":"max"` on every recorded turn of all 10 of its agents, and no other value). The pipeline's own runs record `medium`, since pool.json leaves effort unset. Measured against run #528 on the same model (claude-opus-5-5):
  - PR reviewers took 34-41 min over 96-145 turns, against 5-14 min over 24-36 turns;
  - verifiers took 23-24 min over 119 turns, against 10-12 min over 18-25;
  - each agent processed 18-64M tokens, against 0.7-4.2M.

  69% of agent wall time was model generation and about 15% tools; orchestration between agents was 1-3 min per handoff. The run record format has no effort field, so a retro cannot tell an effort-driven slow run from a pipeline fault.
- (no skill section) context: every drug-safety agent reads the 76k-char `reference/CLAUDE.md` in full, as the root CLAUDE.md requires, and carries it on every later call: 23% of all Read content. `DrugSafetyValidator.java` (13,563 lines) was read 35 times across seven agents, 26% of Read content.

## Declined
- r1-6, a tracking issue for the sibling-row residue — "If we ship without the issue, the sibling-row residue is recorded only in ADR 123 and a code comment... #402 closes with this PR, so nothing on the tracker would tell the next change to the order-driven arm that the two arms disagree."

## Assumptions review overturned
- "the direction's substancesOf formula is the referent's predicate" -> narrowed in r1 and r2 (the presentation gate) and in r3 (a substance counts only where one of her orders establishes it). The owner accepted item 1 before either narrowing existed, per r3's note; both are disclosed in ADR 123 and the PR description.
