# resolve-ticket · openmrs-module-chartsearchai · #409 (round two) · 2026-09-14
outcome: aborted (condition 3 — gate pass 2's second blocking objection left the question open)
rounds: 0   cycles: 0   verifier: skipped (no code written; standalone was pre-flighted and available at :8081)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-409/0102617e-5075-4baa-9afe-3a29fc459eab.jsonl

## Refuted by measurement
- "A check that accuses on the ABSENCE of prose must anchor its citation; one that accuses on the PRESENCE of prose already holds its evidence" (v1's family principle, load-bearing for the scope decision) -> false of `DosingCeilingFidelityCheck`: its "presence" test is `answerStates` → `ChartSearchAiUtils.statesMeasurement(answer, ceiling)` over the WHOLE answer (`DosingCeilingFidelityCheck.java:308-315`), not against the record, and its own javadoc records "a ceiling stated for a DIFFERENT drug … Measured through the real answer path". · cost: 1 gate pass
- "3 of 7 runs" as the production rate of the array-only shape -> the recorded figure for that shape is 2 of 7 (Mode A alone); 3 of 7 is both modes combined (`docs/adr.md:7323`). The inflated figure was the headline of the warrant it was inflating. · cost: 0 (caught at plan time)
- "`ClassCodeFidelityCheck` is already marker-anchored" — inherited verbatim from ADR Decision 94:7341 -> false: its #142 leg walks the resolution's union with only the #305 and blank-text filters (`ClassCodeFidelityCheck.java:239-271`), and it POOLS cited records' codes as support, so an unanchored citation can only SILENCE a report; narrowing it would gain accusations, not lose signal. A false sentence in a merged ADR propagated straight into a new plan. · cost: 0 (caught at plan time)
- "Decision 94's 'Two sibling keys select off the union'" -> three, since #276 (`unstatedDosingCeilings`) merged after #417. Stale within one day of being written. · cost: 0

## Raised by a fresh agent, missed by the author
- [gate 1] The plan's whole warrant was a self-contradiction between README:561 and README:573, and :561 was WRITTEN BY the decision being overturned — so the contradiction was in view when the change was declined, and a doc defect was being used to license a behaviour change. · blocking · cost: 1 gate pass
- [gate 2] The revised warrant still does not clear Decision 94's stated bar, by the plan's own admission ("what the ticket does NOT show is the false accusation itself"). A live occurrence of a precondition the decision had already enumerated is not evidence of harm. · blocking · cost: the run
- [gate 2] `README.md:561` does NOT tell a client to accept the join uninspected — it discloses, in the same paragraph as the join instruction, both that the number may be absent from `answer` and that `findingCitations` will disagree. That killed the plan's point 2 ("the only per-index signal a client receives …") as a statement about the contract. · non-blocking
- [gate 2] `README.md:581` is a rendering rule for `findingCitations`' gap alone; the plan cited it as a family prohibition being breached. · non-blocking
- [gate 2] A fifth per-item publication on the same union that the plan's "five checks" inventory left unnamed: `RecordReference.getGrounded()` (`ChartSearchService.java:1491`) — a per-citation verdict on a citation the prose may never have anchored. Out of scope (Decision 94 refused narrowing the union because it moves grounding) but the inventory read as closed. · non-blocking
- [gate 2] Reusing `ArchitectureGuardTest.assertMarkersReachedOnlyThroughTheSharedDecodeStep` with a parameterised needle IS the drift its own javadoc exists to prevent ("in one place so their needle set cannot drift apart"), and its name would be false of a caller that reaches no markers. · non-blocking
- [gate 2] `reportUnstatedFindingSeverities`' method-level `@param cited` javadoc (`:158-167`) argues at length for an absence the fix removes; the plan's documentation list named only the class javadoc. · non-blocking
- [gate 2] The plan's paraphrase of README:571 ("no bundled dataset gives one record two ceilings") is not what README:571 says (it is about rows *carrying age bands*, per source). The conclusion follows; the recorded claim does not read that way. · non-blocking
- [gate 1 and 2, twice] The ticket's own reported harm — `[370]` cited twice, once for a sentence about a different drug pair — is untouched by BOTH rounds, and it is a module defect, not model noise to be disposed of. The plan had filed it as out of scope.

## Declined
- (none — no fixer round ran)

## Where a skill blocked or contradicted this run
- resolve-ticket:Step 3 — the three-outcome rule worked as designed: pass 1 settled, pass 2 did not, and the run stopped rather than picking a side. What the skill does NOT say is what to deliver when the open question is small and the run has verified real defects on the way to it. Four factual errors in merged artifacts (three in `docs/adr.md` Decision 94, one in `README.md:573`) were established with citations and are now in a report rather than in the repo, because the abort path opens no PR. The next attempt on #409 will re-derive them.
- resolve-ticket:Step 1 — `gh issue view 409 --comments` returned empty at exit 0 again (fourth run to meet it: #236, #255, #347, this one). `gh api repos/<owner>/<repo>/issues/<n>` worked first try.
- resolve-ticket:Step 3 — `gate-state pr-set --override` takes `--override` as a FLAG plus `--reason`, not `--override "<text>"`. The skill's abort paragraph says "write the override into the state entry with its reason" without the shape, and the first invocation errored.

## Assumptions review overturned
- "The remaining #409 ask that is a module defect is the per-item accusation keyed on an unanchored citation" (plan assumption 2) -> overturned by both gate passes: the reporter's Mode A misattribution is also a module defect, just a harder one needing a structural partner field at the injector's write site and a check of what a sentence is about. The plan chose the residue the ADR invited because it was smaller, and the ADR's invitation turned out to be gated on evidence the run did not have.
