# resolve-ticket · openmrs-module-chartsearchai · #378 · 2026-09-06
outcome: converged (pr-harden round 1 reported 0 blocking); harden itself did-not-converge (labelled override after cycle 5)
rounds: 1   cycles: 5 (harden, ended on a labelled override)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-378/31566720-10ff-4784-bcb8-8b60bd046093.jsonl

## Refuted by measurement
- "Gating conditionRuleCoverage() on reportsContraindications() would make Coverage.UNLOADED
  unreachable on this surface" -> false; the gate reads two GPs defaulting TRUE while the load is
  gated on one defaulting FALSE, so a stock install answers UNLOADED either way. Two Phase-2 agents
  independently implemented the rejected gate and watched the suite stay green. The RULE was right
  and its recorded REASON was refutable; replaced with a test. · cost: 1 cycle
- "nothing in api/src/main or omod/src/main reads a Diagnosis" -> ChartSearchEventListener:127 does,
  and RESOURCE_TYPE_DIAGNOSIS is an indexed, citable chart record type. The true claim is narrower:
  no encounter diagnosis reaches a contraindication RULE. · cost: 1 cycle, 5 homes to correct
- "there is exactly one arrangement that separates the two designs" -> two. · cost: 0 (self-caught)
- "the one arrangement where `absent` understates the damage is an inert load" -> the drugSafety
  toggles being off is a second, and the change's own new test builds it. · cost: 0 (agent-caught)
- "the limits list has exactly two homes" -> the ADR's residue section is a third, partial copy,
  and the same commit corrected both copies independently. · cost: 0 (two agents, same round)

## Raised by a fresh agent, missed by the author
- [c2] ArchitectureGuardTest's literal `new ChartAnswer(` needle is defeated by
  `new ChartSearchService.ChartAnswer(...)` · blocking · cost: 0 rounds (fixed in-cycle)
- [c2] ...and by a COMMENT naming that construction: the unbalanced bracket made the raw-text depth
  walk swallow the two real constructions after it, count 3 -> 1 · blocking
- [c2] ...and, after the first widening, by a line wrap inside the qualifier
  (`new ChartSearchService.\n\t\tChartAnswer(`) · blocking
- [c2] The SIBLING guard CoMedicationResolutionPerPassTest had the same escape ALREADY OPEN:
  `new\n\t\tCoMedications(context)` is the second per-pass resolution #256 forbids and left that
  class 6/6 green. Found by asking the harden skill's "does the family have another member" question.
- [c2] The new gate test asserted its premise off a SECOND, separately parsed DrugReferenceService
  (measured: 3022 ms + 1490 ms vs 989 ms shared) — the repo's own documented anti-pattern
- [c2] Its second case set no toggles, so it asserted a value identical under both designs while its
  javadoc claimed it was the half the gate leaves untouched
- [c1] Arm.HAND_AUTHORED_RULES' javadoc had no pointer to its new condition leg
- [c1] putSafetyChips' javadoc enumerated a composition that had fallen behind
- [c2] Decision 75 said the verdict existed since #285 while arguing two paragraphs later the arm is new
- [c2] ADR TOC (75 entries) — self-caught, but only because I looked for one

## Where a skill blocked or contradicted this run
- harden's Phase 2 "commit before anything mutates the tree": I mutated for a guard check and then
  `git checkout -- <file>`, which discarded TWO uncommitted javadoc corrections in that file. The
  skill documents exactly this and I did it anyway. Cost: re-applying both from memory.
- ProjectInstructionsGuardTest's per-bullet prose budget fired when the instruction bullet grew a
  rationale. That is the guard working — the evidence moved to the ADR and the directive stayed.

## Declined
- Efficiency agent: point the gate test at a 12-entry fixture instead of the shipped 18MB dataset
  (33 ms vs 989 ms). Declined: the shipped dataset is what makes "a dataset really did load" a
  statement about the install an operator actually has; a fixture would make the premise about a
  file no install runs. Cost of declining: ~1 s of suite time.
- Two more literal-needle guards named but unmeasured (`"new Thread("`,`"putSafetyChips("`). Left to
  their own issues rather than widened into #378.

## Assumptions review overturned
- A2 "null where the contraindication arms are toggled off, per PairChipExtent's precedent" ->
  overturned at the refutation gate (pass 2, blocking): a per-response measurement genuinely cannot
  be stated when its arm did not run; a load-time capability can, and gating it made the ticket's
  explicitly requested `unloaded` value structurally unreachable.

## Harden outcome
5 cycles, 15 fresh agents, ended on the labelled override rather than on an empty cycle.
Every finding from cycle 2 on was in the test-only ChartAnswer construction guard added during
cycle 1, or in the prose describing it — each correction generating the next. The guard was re-typed
twice (source scan -> class-file constant pool -> class-file METHOD TABLE) and is now measured
against seven escapes: qualified type name, a comment with an unbalanced bracket, a line wrap in the
qualifier, a unicode escape, a generic type witness, a construction in another class entirely, and a
new constructor arity with different leading parameter types. The production change (63 non-comment
lines) drew no finding from any agent after cycle 1.

## The loop this run got stuck in, and what broke it
Widening a lexical needle bought exactly one more escape each time, five times. What ended it was a
reviewer's argument to change the KIND of question — ask javac's output rather than the author's
typing — which closed the whole family at once AND closed the residue the source form had conceded
(an answer built in another file). The skill already says "a differently-typed question is not a
closed one"; this run is a clean instance of it being the only thing that worked.

## pr-harden round 1
Zero blocking findings on the first round, two non-blocking, both applied at FINISH:
- The class-file construction guard proves every answer is built through the WIDEST constructor, not
  that every answer carries a value — a literal null in that argument satisfies it. The reviewer
  measured it with a fourth answer site and named it the likeliest escape of all, since ChartAnswer's
  own telescoping constructors pass null that way. Named as residue rather than closed; closing it
  needs an instruction walk for aconst_null, not a constant-pool read. It also falsified a README
  sentence about what `null` means, which nothing else held.
- The DDI test guide said any ddinter cell reading other than `absent` means the wrong dataset was
  loaded. `unloaded` is a fourth reading and is what a tester sees having forgotten the master switch
  — the commonest setup mistake, and the one that document exists to diagnose.

## Verifier
Proved the RUNNING bytes rather than the omod timestamp: DrugReferenceLoad.class and the new arm's
DrugReferenceLoad$Arm$5.class hashed identical in the built omod, the lib-cache jar and the exploded
tree. The standalone carried a Sep-3 omod and a stale lib-cache expansion; both replaced. It caught
the design's own distinction live — the contraindication screen fired on its ALLERGY leg (three chips,
an NSAID cross-reactivity refusal) on the same response where conditionRuleCoverage said the CONDITION
leg had no rule to ask with. That is the reading README tells a client not to conflate, observed.
