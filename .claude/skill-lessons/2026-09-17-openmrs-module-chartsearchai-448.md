# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #448 / PR 452 · 2026-09-17
outcome: converged
rounds: 4 (pr-harden)   cycles: 6 (harden, step 7)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-448/1904f6d9-fa6d-41d3-8169-e2f5fb83083e.jsonl

## Refuted by measurement
- The TICKET's own claim, "the sentence-scoped default splitter (splitIntoCitedSentences / splitEnumeration) is linear per sentence and does not have this property" -> false; splitEnumeration hands every item the whole preamble, so the sink is reachable with clauseScoped at its default false, which the ticket's stated conditions exclude · cost: 0 (caught at plan time, proven by a test)
- Plan v1 test 1 used ONE REPEATED marker index, which ChartSearchAiUtils.citedIndexes collapses to a one-element set -> the case passed before the change · cost: 0 (refutation gate pass 1)
- Plan v1 test 3 was sized on the CHARGED cost while asserting ACTUAL characters, which differ ~2x for the clause sink -> passed before the change · cost: 0 (gate pass 1)
- Plan v1's "resulting memory O(L + B)" -> false; AnswerCitations.restsOn retains a per-reference index set · cost: 0 (gate pass 1)
- Plan v2's proposed ArchitectureGuardTest pin -> the repo's two "written in ONE place" guards are CONSTANT-POOL guards at class-file granularity, so they cannot express a two-methods-in-one-class rule · cost: 0 (gate pass 2)
- Plan v2's proposed CLAUDE.md bullet -> the file was 24,997 bytes against a 25,000 budget, and the guard's javadoc forbids raising it in the same commit · cost: 0 (gate pass 2)
- Plan v2's corpus calibration -> the probe-safety corpus maxes three orders of magnitude below the constant and cannot calibrate it · cost: 0 (gate pass 2)
- "bounding the splitter bounds grounding memory" -> the quadratic survived in the CONSUMER: AnswerCitations unioned each claim unit's set once per FRAGMENT, 254 / 879 / 3,557 ms at 5k/10k/20k markers · cost: 1 harden cycle
- "restsOn's per-reference copy is linear and therefore within the criterion" -> OOM reproduced with the copy restored, between 96 MB and 80 MB on a 910 KB answer at ten cited records · cost: 1 harden cycle
- "1e6 characters is about 2 MB of heap" -> the fragments' own objects are the larger half · cost: 1 cycle
- "the largest corpus total is 648 characters on a 654-character answer" -> that is the answer's own length on an answer nothing split; the largest CHARGED is 474 on 346 · cost: 1 cycle
- Five successive drafts of a rule about WHICH ANSWERS reach the allowance, each refuted by the next measurement; ended only by publishing the two measured points and no rule · cost: 3 cycles

## Raised by a fresh agent, missed by the author
- [harden r1] The per-fragment copy of sourceCitedIndexes makes a character-counted allowance report itself satisfied while the exhaustion stays reachable · blocking · cost: 1 cycle
- [harden r2] The WARN double-counted a sentence both splitters refuse, so the line stated up to twice the truth · non-blocking · cost: 1 cycle
- [harden r2/r3] Nine false or overstated documentation sentences across four rounds, every one written by a previous round's own correction · non-blocking · cost: 3 cycles
- [harden r4] A test whose name and ADR citation promised a clause-scoped measurement drove the sentence-scoped overload, so the refusal was not in the causal path · blocking · cost: 1 cycle
- [pr r1] aClaimFragmentIsBuiltOnlyThroughTheBudgetChargedFactory allow-listed a whole METHOD, so an uncharged per-marker splitter written inside it passed · non-blocking · cost: 1 round
- [pr r1] theCitationsAClaimRestsOnAreAViewAndNotACopy scanned only restsOn's body, so the union could be rebuilt one constructor deeper in ClaimSupport · non-blocking · cost: 1 round
- [pr r1] Decision 103 was missing from docs/adr.md's own table of contents, which all 102 before it are in · non-blocking · cost: 0
- [pr r2] BOTH tightened rules walked through again: a per-marker splitter calling the allow-listed whole-sentence factory; one creating its own FragmentBudget; and a copy spelled `new java.util.HashSet<Integer>(own)`, invisible to a five-spelling denylist · blocking · cost: 1 round
- [pr r4] A residue paragraph under-stated its rule's reach — a delegating factory IS caught, since newSentence is private static · non-blocking · deferred to issue #455

## Where a skill blocked or contradicted this run
- pr-harden:"Editing by script" / harden:"Don't trust a script's report" — a slice-based replacement in the test file duplicated a 60-line block because `str.index` found the END anchor BEFORE the start anchor (two methods inserted at the same anchor land in reverse order). The duplicate-method compile error caught it; the neighbour count the rule asks for would have caught it first.
- pr-harden:"Collecting in the same turn means never polling afterwards" — followed late. Three TaskOutput calls on background agents each injected a truncated window of raw JSONL (~20k tokens apiece) before the rule's reasoning was applied; after that, agents were collected by a foreground poll on a terminal-state predicate over the transcript, which costs nothing and reads no content.
- harden:"What ends the loop is a change in the KIND of question" — the operative rule of this whole run, hit twice: five drafts of a claim about the allowance's reach (ended by publishing measurements and no rule), and three rounds of text guards defeated one more way each (ended by splitting one denylist into three list-equality questions plus one positive shape constraint).

## Declined
- (none) — every finding in every round was implemented. The one deferral, pr r4-1, was routed to issue #455 under FINISH's rule against editing a cleared sha, not declined: the reviewer itself rated it non-blocking and its error is in the safe direction.

## Assumptions review overturned
- "Recommended-fix item 2 would duplicate #446's change" -> false; #446 binds the TRANSPORT and item 2 binds the extracted answer on both engines and both search paths. The deferral stood, on corrected grounds: it is a behaviour-visible decision of its own (gate pass 2)
- "A refusal is a remote-endpoint event, the allowance being above what the local engine can produce" -> a 3,457-character one-line answer with 594 markers is refused, well inside the local cap; corrected in four homes (harden cycle 1)
- "A refused clause split costs the isolation" -> shape-dependent, and the ordinary prose case is larger: the whole sentence is a compound claim, so under entailment every citation of it publishes no verdict (harden cycle 2)
- "AnswerCitations.restsOn is residue this change does not close" -> closed in the change, after two reviewers measured that linear was not the property that mattered (harden cycle 1)
