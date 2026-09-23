# resolve-ticket (+ harden, pr-harden) · openmrs-module-chartsearchai · #409 / PR 417 · 2026-09-13
outcome: converged
rounds: 1 (pr-harden)   cycles: 2 (harden, 6 Phase-2 passes in cycle 1)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced · one claude.ai usage-limit pause, resumed by pool-run
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-openmrs-module-chartsearchai/d588c8a7-f979-4583-9898-b145c0a9506e.jsonl

## Refuted by measurement
- "cited is taken from extractCitedReferences and never re-derived from the markers" (ADR Decision 83, the decision that created the key) -> the resolution is the UNION of the model's array and its inline markers, so the gate for "did the prose state every finding" failed open on exactly its own shape · cost: the whole ticket
- "Mode B's missing references mean the model cited them neither inline nor in the array" (plan assumption A2) -> refuted by the gate: normalizeSlashCitations leaves an uncorroborated compact group unrewritten and the single-index pattern then sees no marker in it, which reproduces Mode B exactly · cost: 1 gate pass
- "Both halves decide, and neither alone is this question" (javadoc I wrote) -> for a non-blank answer the resolution test is INERT: replacing the branch with `anchored ∩ carried` leaves the whole api suite green. `carried` membership is what excludes a bracketed clinical value · cost: 1 Phase-2 pass
- "the repair now fires on at least as many cells, so the cost figures are a lower bound" (config.xml + ADR) -> the same commit added the blank-original refusal, which SHRINKS the firing set; stated three clauses later in the same paragraph · cost: 1 Phase-2 pass
- "findingCitations 82/86 -> 81/86 and cells stating every finding 8 of 12 -> 12 of 12, measured for Decisions 84 and 85" -> a splice of two different arms: 82/86 is Decision 77's second citeOrderRecords arm (with 8 of 12 -> 7 of 12), Decision 85's is 6 of 12 -> 12 of 12, and "8 of 12 -> 12 of 12" was never measured · cost: 1 Phase-2 pass
- "8000 input tokens on a kept continuation against 1000 on a discarded one, measured on this branch" -> nothing in the tree produces those numbers; the stub's two-arg LlmResponse zeroes all three counts · cost: 1 Phase-2 pass

## Raised by a fresh agent, missed by the author
- [harden P2/1] The repair could LOWER findingCitations.cited: cited reads a blank answer's resolution and a real answer's markers, so a continuation appended to a blank original flips the reading. Measured 3 -> 1 through the real search() path by two lenses independently · blocking · cost: 1 pass
- [harden P2/2] The STREAMING call site of findingsOwedARepair had zero coverage — handing it a literal instead of response.getAnswer() left the whole api suite green, defeating both halves of the gate on the path users hit · blocking · cost: 1 pass
- [harden P2/1] The inserted ArchitectureGuard method orphaned its neighbour's javadoc — reported independently by three of four lenses, and the exact defect this repo's own memory records · non-blocking · cost: 0
- [harden P2/2] ChartSearchService.FindingCitationExtent's CLASS javadoc still said "never a re-derivation from the markers" while its own getCited(), rewritten in the same commit, said the opposite — and it is the type every other surface calls canonical · blocking · cost: 1 pass
- [harden P2/2] The shipped config.xml description of the repair GP stated the old keep-gate and omitted the blank refusal · blocking · cost: 1 pass
- [harden P2/3] The composed overload re-spelled carriedFindingIndexes instead of calling it — which is the production caller that method's own javadoc asked for · non-blocking · cost: 0
- [pr-harden r1] Decision 93's own "sites that quote such a figure point at this rule" describes the tree rather than stating a policy, and three sites in Decision 77 carry no pointer · non-blocking · cost: 0
- [verifier] On the real standalone the fix is visible: carried 7 / cited 6 with reference [355] anchored nowhere in the prose — the old reading would have published 7 · confirmation, not a finding

## Where a skill blocked or contradicted this run
- resolve-ticket Step 1 — `gh issue view` returned empty at exit 0 exactly as the skill warns; `gh api` worked first try. Cost 0 because the skill named it.
- harden Termination vs the phase gates — five Phase-2 passes in a row each found only prose defects written by the previous pass. What ended it was harden's own "delete the CLAIM SHAPE" rule: a closed enumeration and a superlative were replaced by a dated rule, and the next pass was empty. The rule works; it took four passes to reach for it.
- pr-harden FINISH — "do NOT edit the cleared sha" meant round 1's two non-blocking findings could not be applied. Correct, and worth noting that one of them (the ledger sentence) is a real prose defect now owed to a follow-up.
- ProjectInstructionsGuardTest budget — the reference instruction file had 54 bytes free, so the new directive was paid for by trimming the same bullet rather than raising the number. It now has 5 bytes free; the next rule added there faces the raise the guard warns about.

## Declined
- Narrowing extractCitedReferences so references[] matches the prose — what #409's Expected-behavior sentence literally asks for. If we ship without this, a client still renders a citation chip for a record the answer never referenced, because the union is the specification (LlmInferenceServiceTest.extractCitedReferences_shouldKeepTheArrayWhenTheOnlyInlineMarkerIsUnmapped, "This locks that decision") and the same resolution feeds grounding and the chips.
- Widening unstatedFindingSeverities and unfaithfullyRenderedCitations to the same reading. If we ship without this, a finding findingCitations says the answer never cited can still appear in unstatedFindingSeverities as a cited finding whose rating went unstated — a per-item accusation changed on no evidence of harm, where this key is a base.
- Splitting an uncorroborated compact group. If we ship without this, a finding anchored only inside `[363, 367]` is counted uncited — and the alternative re-opens the defect normalizeSlashCitations exists for, an uncorroborated numeric bracket being a clinical value.
- Fixing config.xml's "Default: false" against a `true` defaultValue, and three broken ADR anchors. If we ship without these, an operator reads the wrong default for a sibling GP and three cross-references do not resolve — both pre-existing at the base sha and outside this ticket's scope.

## Assumptions review overturned
- "the plan's composition is licensed by CLAUDE.md's inline-citation rule" -> that rule governs a MARKER-FIRST consumer; ActiveOrderCitationFidelityCheck is marker-first and neither licenses narrowing a resolution-derived population. The change was argued on #409's own evidence and superseded on the record instead · gate pass 2
- "Mode B can be declined because the model cited them nowhere" -> declined instead because BOTH readings end without a code change here, one of them on a CLAUDE.md rule · gate pass 2

---

## Addendum — merge round (user-requested, after the run had converged)

`main` gained f4a4db27 (#416 / issue #294) minutes after PR 417 was marked ready, and the PR went
CONFLICTING. rounds: 2 · verifier: ran twice (both "works at runtime")

### Refuted by measurement
- "the base has not moved since the branch was cut" (true when round 1 fetched it, ~40 min earlier) -> main landed one commit that allocated the SAME ADR decision number, 93, from the sequence this branch had taken it from · cost: 1 round + 1 verifier run

### Raised by a fresh agent, missed by the author
- [r2] Nothing blocking. The reviewer proved the resolution mechanically rather than by reading it: `git diff f4a4db27 HEAD` with the renumber normalized away is byte-for-byte identical to `git diff 972299d9 54b512e3` on every file — a check the author had not thought to construct, and strictly stronger than the per-home sweep.
- [r2 note] Root CLAUDE.md's inline-citation bullet cites only the older of the two marker guards; the pointer resolves and no count is stated, so nothing is stale, but a maintainer will not learn the sibling guard exists · non-blocking · owed to follow-up

### Where a skill blocked or contradicted this run
- pr-harden Step 1's "compare the base you just fetched against the one the previous round saw" — the ADR-number collision is the FIRST class it names, and it happened exactly as described, twice-observed-per-run in the skill's own record. Naming it is what made the resolution a checklist rather than a diagnosis.
- The phrase search missed a home: `reference/CLAUDE.md` writes "ADR Decisions 83, 93", which no search for "Decision 93" reaches. The skill's "search the rarest TOKEN, not the phrasing" rule is what caught it; searching the number in a Decision-list regex is what actually found it.
- pr-harden's "an idle output file is not evidence an agent has stopped" was RIGHT and the orchestrator was wrong to doubt it: the round-2 verifier's transcript sat byte-identical for ~15 minutes and it had NOT stalled — it ran 8,743s (~2.4h) and reported cleanly. The orchestrator meanwhile drove the runtime check itself and reported that the fresh-context read had not happened, which was false. Cost: a duplicated measurement and a false line in a user-facing report. The lesson is the skill's own — a terminal outcome is one the harness reports — and what would have made the doubt cheap is watching something the agent's WORK touches (the standalone, which the orchestrator did check and which showed the verifier progressing) rather than the transcript's size. The module's `local` engine had idled out its llama-server (idleTimeoutMinutes=30) and relaunched it on the next query; the first request failed only on a wrong field name (`patientUuid` vs `patient`), which cost one round-trip.
- "Editing by script" fired twice in one command: an assert caught a THIRD occurrence of "Decision 93" in the PR body that the two-form assert had not counted, and the write correctly never ran — but `gh pr edit` was chained after it with `;` and pushed the unchanged file anyway. Chain edits with `&&`, not `;`.

### Raised by a fresh agent, missed by the author (verifier, round 2)
- `$MAVEN_ARGS` passed unquoted on a `mvn` command line is NOT word-split by zsh, so the build installed into a junk repo directory named after the whole string. The orchestrator never hit this because plain `mvn` reads the variable from the environment itself; an agent that echoes it onto the command line does. Same shape as the #263 record.
- `java -jar openmrs-standalone.jar -commandline` forks a CHILD JVM that is the actual Tomcat, so killing the launcher pid leaves the old server holding the port and a readiness probe answers from it. The verifier caught that its first measurements came from the pre-existing server and re-took them all after killing the whole tree.
- main's #294 shape is not reachable in this demo DB (every drug order names a drug), so the verifier BUILT it as demo data — a concept with its only name voided, mapped SAME-AS to WHOATC B01AB01, plus an active order with a null drug — which is exactly the `namedByCodesOnly` path, and confirmed `grounded: null` on the citation while chart-group `drug_order` citations on the same run published `false`. That is the carve-out working, proved against a live control rather than asserted.

### Assumptions review overturned
- "the merged head is covered by the round-1 verifier run" -> a runtime verdict is a statement about one commit; re-taken on c40d95c3 and identical ({"carried": 7, "cited": 6}, [355] unanchored, all three invariants) · merge round

**Corrected by the 2026-09-23 retro:** the `transcript:` path above does not exist. This run's
transcript is `~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-409/d588c8a7-f979-4583-9898-b145c0a9506e.jsonl`.
