# resolve-ticket (+ harden, pr-harden) · openmrs-module-chartsearchai · #337 / PR 384 · 2026-09-07
outcome: converged (pr-harden round 1, 0 blocking) · harden did-not-converge (override after cycle 9)
rounds: 1   cycles: 9   verifier: ran twice (works at runtime; pre-merge head and merged head)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-337/7bc1f2c5-a89c-4c0b-8284-a5ceae648a6a.jsonl

## Refuted by measurement
- "`unknown` is covered by the unrated arm" -> `unknown` is RATED (severityRank 0) vs unrated (-1); at `minInteractionSeverity=unknown` that is 84,830 of 590,312 shipped links accused systematically · cost: caught at the plan gate, 0 rounds
- "the record's prose states the rating, because that is where the model reads it" -> false on any `sourceFormat=curated` dataset; `renderFinding` writes no rating, only `DdiDrugReferenceSource.noteFor`'s `<Severity>. ` prefix does. A verbatim-faithful answer was reported and published · cost: 1 Phase-2 pass
- "mechanism carries its own rating word on 0.91% of links" (agent's script) -> 5,376/505,476 measured through production, after two wrong intermediate figures: 90,212 without the `statableRating` filter (Unknown sentinel), then 5,382/142 (Moderate sentinel) · cost: 2 corrections
- "0.096 us for an answer citing no finding, which is the ordinary one" -> that input was an EMPTY citation list; the ordinary answer costs ~17 us, 200x · cost: 1 pass
- "the scan is one indexOf walk per cited finding" (stated as the design) -> that IS the defect; 200 cited findings = 2.2 ms, past the family's stated outlier · cost: 1 pass
- "memoising caps the scan at three walks" -> the key was the dataset's own spelling trimmed, so `Major`/`major` gave two keys; 4 keys measured from 2 rating classes · cost: 1 cycle
- "the prompt asks for the rating either way" -> 3 of 4 cells; the current-medication CAUTION branch never asks, and a minor finding there is reachable at the shipped floor · cost: 1 cycle
- "on stock configuration" (inherited from the ticket's own comment) -> `chartMode=fullChart` and `drugReference.enabled=true` are both non-default, and with the second off the defect cannot arise · cost: found by the verifier's `inherited_environment`, 0 rounds

## Raised by a fresh agent, missed by the author
- [Phase2 p1] the check's premise false on operator datasets · blocking-class · cost: 1 pass
- [Phase2 p1] only one of four checks with no cheap gate: full index + 16kB toLowerCase before learning it cannot fire, on the shipped default · non-blocking · cost: 1 pass
- [Phase2 p1] three more homes of the "which checks read reference content" list, incl. the two `docs/adr.md:4047` records as having defeated sweeps 2 and 3 · non-blocking · cost: 1 pass
- [Phase2 p2] `renderFinding`'s 42-line javadoc orphaned by inserting a method above it — found independently by two lenses · non-blocking · cost: 1 pass
- [Phase2 p2] the "no allowance to choose" justification measurably false: at `PROSE_TRAILING_LETTERS=0`, `containsWord` reduces to the same condition (175 pairs, agree but for an accented needle) · non-blocking · cost: 1 pass
- [c2] `statableRating`'s `-1` arm unpinned — weakenable with the whole build green · non-blocking · cost: 1 cycle
- [c3] left digit half of `statesWord`'s boundary unpinned · non-blocking · cost: 1 cycle
- [c4] trailing digit half still unpinned after the case that "fixed" it glued the digit on one side only · non-blocking · cost: 1 cycle
- [c8] the "COUNT of a family that grows" cause had THREE homes; cycle 7 fixed one and declared the species swept, having searched the clause SHAPE not the claim's TOKEN · blocking-class · cost: 1 cycle
- [r1] the nested rule's closing pointer sent a maintainer to the root `CLAUDE.md` "for the check that reads it", after the root bullet was dropped for the size budget · non-blocking · cost: applied at FINISH

## Merge with main, after convergence
- #382 (#379) merged after the branch was cut. Both sides appended an ADR **Decision 77**; #379 kept it, mine became 78. Swept by the NUMBER: anchor, 2 ADR back-references, a TOC entry, 8 homes in javadoc/README/instruction file. 6 other "Decision 77" occurrences were #379's and stayed — one in the SAME `reference/CLAUDE.md` pointer run as mine, so each was classified by reading its bullet, not by pattern.
- `renderFinding(finding)` -> `renderFinding(finding, orderRecordNumbers)`: real conflict, both sides orthogonal, both taken.
- **Re-measured rather than re-read**: `ratingThisRecordStates`' claim that no clause `renderFinding` appends states a rating word, re-driven through production `statesWord` over all 15 static String constants on the merged tree -> 0 hits, claim holds.
- **The instruction-file rule had to be dropped.** base 69,772; #379 added 2,209 -> 71,981; mine added 2,198 -> 71,970; budget 72,000. Each fits alone, neither with the other, and `ProjectInstructionsGuardTest`'s own policy calls raising a budget in the same commit as the overflow "the one move the paragraph above calls illegitimate". Root file had 8 bytes. Directives survive in javadoc + ADR Decision 78; the instruction-file binding is lost. Structural remedy (trim or split) left to the maintainer.
- **Interaction nobody had run**: with #379's `citeOrderRecords` ON, the answer states ratings inline and `unstatedFindingSeverities` goes [350..354] -> []; `misattributedOrderCitations` goes [177,166,155] -> []. Both issues' reproducers are downstream of that one binary, which #379's own unmeasured A/B needs to know.

## Where a skill blocked or contradicted this run
- harden:Termination — the cycle gate's `--count-edits` fallback reported `edits=14/16/17` at convergence because the branch had an upstream, so every reading was `@{u}..HEAD`; the skill documents this, and the run had to measure its own commit count each cycle instead.
- resolve-ticket:Step 8 — `Refs #337` was not enough: the body's own sentence "This does not close #337" put #337 in `closingIssuesReferences`. Checking the FIELD rather than the wording is what caught it; rewording fixed it, as on #250.
- pr-harden:State — I spawned cycle 5's lens without recording an await and the gate caught the yield. The rule is stated; the failure is that recording it is a separate call from spawning.
- pr-harden:FINISH — "verify the merging head" met a comment-only push after the verifier ran. Byte-identity was FALSE (a split comment line shifted the LineNumberTable); `javap -c` against the exact class the verifier ran proved semantic equivalence. The skill's byte-hash advice needs this distinction.

## Declined
- index→mapping map built in four checks — if we ship without extracting it, nothing breaks: all four resolve identical keys and the duplicate-index decision they make in parallel is unreachable.
- omod wire-test skeleton clone — if we ship it, a fifth published key means a fourth 266-line clone; a positional slip in its null run fails loudly, so the cost is maintenance not correctness.
- `setOf` duplicated in two test files — if we ship it, two lines stay duplicated; the module's helper is package-private in another package and widening shared test infra costs more.
- the rule in `reference/CLAUDE.md` rather than the root file — if we ship it, a maintainer reading the root's check family sees three of four; the root is at its test-enforced 23,000-byte budget and its own header directs readers to the nested file.
- `statesWord` folding both operands — if we ship it, ~18 us per answer on a path costing seconds; the alternative adds a second public entry point to a method whose argument is that there is one rule.
- three over-report cells — if we ship them, a caution-rated current medication, a synonym rendering and a mechanism containing the rating word can each be listed on a clinician-facing key; narrowing the first needs a referent axis the record does not carry, and all three are documented in the client contract.

## Assumptions review overturned
- "uniform over every rated finding, because the prompt's instruction is uniform" -> uniform over the three ratings that say something, and the reason is that the check asks about a DATUM's survival, not a clinical call's strength; the prompt is NOT uniform (Phase 2 pass 2)
- "the rating is in the record because renderFinding puts it there" -> only `DdiDrugReferenceSource` does, so the write site needs a second condition (Phase 2 pass 1)
- "the mutation space of the new predicates is exhausted" -> falsified twice, on successive halves of one boundary (cycles 4 and 8)
