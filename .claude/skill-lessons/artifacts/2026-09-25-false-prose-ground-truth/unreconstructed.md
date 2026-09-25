# Candidate instances not written to instances.jsonl

Scope: the 17 run records that `REJECTED.md` counts under "Prose the change (itself) made false, found by
a fresh agent" (:4019-4428). Candidates come from each record's text and, for records whose harden rounds
were committed separately, from the fresh agents' reports in the run transcript (`source: transcript` in
instances.jsonl). Every repository object named below comes from the bare clone `cs.git`
(`refs/pull/*/head`). Base commits are `git merge-base <commit> <PR baseRefOid>`.

A dash under "text at base" means the false text was written and removed between local commits that were
never pushed, so no commit on GitHub contains it.

## 1. Squashed: the fix is in the same pushed commit as the change (23 rows, 27 homes)

These runs made local commits during harden, then ran `git reset -q --soft origin/main && git commit`
before the first push. The transcripts of #433, #454, #458, #514 (first loop), #505/PR529 and #515 each
contain that command. For the #438 and #443 gate findings, the fix was part of the first commit in the
ordinary way. In both cases the fix commit's parent is the base itself, so `base..prefix` is empty, and no
pushed tree holds the change next to the false text. The pre-squash commits would have been in the local
repo `/Users/danielkayiwa/Projects/openmrs/openmrs-module-chartsearchai`, since pipeline worktrees are
worktrees of it. I did not try to recover them: the transcripts do not print their SHAs (`git commit -q`).

| # | record | PR | found by | false text / home | text at base | fix (squashed) |
|---|---|---|---|---|---|---|
| 1 | 433.md:13 | 511 | harden P2a quality lens (finding 4) | pairKeyNames javadoc, "reachable wherever one word of the question resolves several entries". This is the author's post-gate rewrite, at local HEAD 996b7482, of base's "reachable from a question naming a single drug" (DrugSafetyValidator.java:6874 @627449a7), which the plan refuter had already listed | — | 7ed0efff |
| 2 | 433.md:14 | 511 | harden P2b | the narrowed replacement ("…of one substance beside a second drug") | — | 7ed0efff |
| 3 | 433.md:15 | 511 | harden P2a | ADR Decision 115 placement bullet cited the wrong test for "still states its zero" (ADR 115 is new in this change) | — | 7ed0efff |
| 4 | 433.md:16 | 511 | harden P2a | neighbour comment in PairChipExtentContextTest ~385, "Asserted, not assumed: on TWO entries the question-pair arm runs instead" | PairChipExtentContextTest.java:384 @627449a7 (OLD) | 7ed0efff |
| 5 | 433.md:16 | 511 | harden P2a | README null list, "or it resolved one and the chart records no medication to screen it against" | README.md:543 @627449a7 (OLD) | 7ed0efff |
| 6 | 433.md:16 | 511 | harden P2a | PairChipExtent null list, same "resolved one" clause | PairChipExtent.java:150 @627449a7 (OLD) | 7ed0efff |
| 7 | 438.md:12 | 517 | plan gate | ADR Decision 101 paragraph recording #438 as open ("unchanged by this fix, and fixing it means holding a partial code point across frames") | docs/adr.md:7816-7819 @e0492bb9 (OLD) | 6be57470 (the change's first commit) |
| 8 | 443.md:13 | 538 | plan gate | SubjectRule / activeOrderEntryFor sibling javadoc naming the log label #440 deleted, "the name and log label {@link #addActiveOrderPairInteractions} gives" | DrugSafetyValidator.java:8915 @0545e844 (OLD; already false since #440) | 6025817c (the change's first commit) |
| 9 | 454.md:14 | 518 | harden P2 quality (c1) | "is what every OVERSIZED case asserts" | RemoteLlmEngineResponseSizeBoundTest.java:68-69 @e0492bb9 (OLD, made false by the fixed-body case) | 461861a9 |
| 10 | 454.md:14 | 518 | harden P2 (second Phase 2) | PEER_EXIT_SECONDS javadoc, "the oversized cases are looking for" | same file :121 @e0492bb9 (OLD) | 461861a9 |
| 11 | 454.md:14 | 518 | harden P2 (second Phase 2) | peerReachedItsSafetyLimit javadoc, "The verdict of every oversized case is this flag" | same file :147 @e0492bb9 (OLD) | 461861a9 |
| 12 | 454.md:13 | 518 | harden P2 quality | class javadoc "pins the ceilings from BELOW … so a LOWERED ceiling passes them" (the author's harden-cycle wording; base only had "What pins the ceilings from BELOW is the positive controls", :81) | — | 461861a9 |
| 13 | 458.md:11 | 520 | harden P2 #1 | method javadoc "this case passes against the pre-change code, measured" | QuestionPairRuleScanPerPassTest.java:222 @5c5b1c2c (OLD, made false by the new control) | fa6cbc25 |
| 14 | 458.md:12 | 520 | harden P2 #1 | failure message telling the reader to swap the needle (misdirects) | — | fa6cbc25 |
| 15 | 458.md:13 | 520 | harden P2 #2 | SourceScan class javadoc "Every lookup fails LOUDLY" | SourceScan.java:38 @5c5b1c2c (OLD; false before the change too) | fa6cbc25 |
| 16 | 458.md:13 | 520 | harden P2 #2 | second home, "SourceScan also fails loudly on a needle that matches nothing or twice" | StandingChartAlertsTest.java:433 @5c5b1c2c (OLD) | fa6cbc25 |
| 17 | 514.md:18 | 524 | harden P2 r1..r5 | "~15 unpinned guards/false prose". The record does not list them (see §5) | — | 6469ad6c |
| 18 | 514.md:11 | 524 | harden P2 (each round) | ADR residue list "these N legs are unpinned", found incomplete one leg per round and then deleted | — | 6469ad6c |
| 19 | 514.md:35 | 524 | harden (P2) | A4 "containment residues run toward silence", false on the partner side | — | 6469ad6c |
| 20 | 505.md:41 | 529 | harden P2 | ~30 further "verbatim" homes in javadoc, ADR and production javadoc (66 lines of 0c7c5528 that it removes mention "verbatim") | many homes @c852bb36 (OLD) | 0c7c5528 |
| 21 | 505.md:36 | 529 | harden P2 | the three ddi-fold-* fixtures' top-level `note` key, which every metadata-based sweep missed | fixtures @c852bb36 | 0c7c5528 |
| 22 | 515.md:14 | 540 | harden P2 integration | "five docs said none" (order-driven contraindications carry no subject rows): SafetyWarning.java:1099-1100 and :1112, CautionLeadOverWithholdingCheck.java:50, PatientChartSerializer.java:760, docs/adr.md:11125 (the lens's line numbers, in the pre-squash tree) | — (all written by the change) | a7c7945b |
| 23 | 515.md:14 | 540 | harden P2 integration | the stale neighbour comment, "A no-op for the order-driven arm" | DrugSafetyValidator.java:2997 @aef31076 (OLD) | a7c7945b |

This section has 23 rows. Row 22 is one record item that names five homes. Counting those homes
separately, as instances.jsonl counts homes, gives 27 candidates. Row 17 is an unenumerated "~15" (§5),
counted here as one.
Rows 4-6 are the ones the harden P2 quality lens (finding 3) and integration lens (findings 1-2) named. Their
reports are in the subagent transcripts under
`~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-433/dbfc9042-552f-4c7d-aed9-be5ea137bfe1/subagents/`
(agent-a07ca58a…, agent-af917ede…).

## 2. The false text is in the PR description, not the tree (4)

| record | PR | found by | text |
|---|---|---|---|
| 512.md:18 | 525 | pr-harden r2 reviewer | PR body said the preview was covered "on both paths" |
| 273.md:17 | 533 | pr-harden r2 reviewer | PR body's "Not done here" listed r1's work as undone |
| 432.md:18 | 536 | pr-harden r3 reviewer | PR body's "Not done here", made false by r2's fix |
| 515.md:21 | 540 | pr-harden r2 reviewer | PR body "does not close #513" made GitHub link #513. This is a closing-keyword effect, not a false sentence |

## 3. Never corrected on the PR: notes left unimplemented, or declined (5)

| record | PR | found by | text / home |
|---|---|---|---|
| 273.md:18 | 533 | pr-harden r2/r3 notes | two test javadocs that still say json is the default; PresentationMoietyAllergenTest, and the drug-reference-name-not-its-own-alias.json description ("the one shape a hand-authored `json` dataset admits"), carry the pre-narrowing item-1 claim. The harden c1 integration lens had flagged the fixture too (transcript 57517475…:421) |
| 433.md:19 | 511 | pr-harden r2 note | pairKeyNames' "33 above-floor rows" KB figure, not re-measured for the narrowed population |
| 477-PR523.md:26 | 523 | harden P2 quality lens (finding 3) | reference/CLAUDE.md:65 does not name ordersSharingASubstance as a current-medication source. This is an omission, declined because of the byte budget |
| 527.md:27 | 535 | pr-harden r2 notes | SafetyWarningFixtures "not general-purpose" wording, the javadoc's "two lists … found incomplete" note, and pre-existing README/docs/config.xml items. All are named in the PR body and left unfixed |
| 516 (transcript df7f369c…:818) | 522 | harden P2b quality lens (polish 5) | FindingPartnerCoverageCheck WARN text "its safety finding(s) name" now counts only cited findings. Not fixed |

## 4. Excluded as not a false sentence, going by the finding agent's own framing (14)

| record / transcript | PR | why excluded |
|---|---|---|
| 273.md:15 | 533 | "that method's own javadoc": an ambiguous antecedent |
| 273 c2 P2 (57517475…:485) | 533 | "What that fix leaves of it was not measured" reads as contradicting the measurement; ambiguous (fixed in e9a49c88) |
| 459.md:13 | 537 | ADR insertion orphaned the "That consumer's" referent; ambiguous |
| 459.md:15 | 537 | pointers dropped "or throws". Lens aec5bf60: "incomplete rather than false" |
| 516 P2 quality polish 7 (df7f369c…:741) | 522 | ADR "most responses are not": a population claim nobody measured, not shown false |
| 516 P2 quality polish 4, 8 | 522 | omissions (rung javadocs; the spec-change paragraph) |
| 527.md:26 | 535 | chart-alert fixture sentences not their factories' own: test data, not a claim |
| 527 c1 P2 quality polish 2-3; c2 reuse 1-4; c2 integration 1 | 535 | missing TOC entry, restatements, incomplete "not the clause text" |
| 432 P2b quality 3 (2348f3bc…:476) | 536 | ProjectInstructionsGuardTest "should consider …": an instruction withdrawn by the next sentence |
| 432 P2d quality 2 (…:608) | 536 | "the rung the injector's two note records reach" is true through the 5→7→8 chain; misleading, not false |
| 443 P2 efficiency (b4f0a7c3…:473) | 538 | "written from api.impl.LlmInferenceService's logger during this pass" is true of a planted probe; misleading |
| 505/PR529 r2-1, r2-2 (2463cfcc…:1053) | 529 | guard-coverage javadoc claims cured by extending the code (the claim became true) |
| 514 r1 (8b0eb230…) | 524 | "docs illustrated it": cured by changing the code to match the docs |
| 512 P2b finding 2 (025bd85a…:462) | 525 | LlmEngine.java:76: "not false, only stale in where it points" |

Findings the author made in its own Phase 1 passes are not candidates either: #433:8-9, #454:9, #527:12-13,
#516's 3337c7a7 "stale population claims" (transcript 663), #432's 0ea13f1c and 8a83f1e2, and #512's
744463db.

## 5. Not enumerable

#514/PR524, first loop: the record's "[harden P2 r1..r5] ~15 unpinned guards/false prose" does not name
the sentences, and all six harden cycles were squashed into 6469ad6c, so the pushed history cannot give
the list either. The source would be the transcript
`~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-514/8b0eb230-7971-41bc-afc8-73c4c763ef24.jsonl`.
Even with the list, those instances would be squashed (§1).
