# resolve-ticket · openmrs-module-chartsearchai · #397 · 2026-09-09
outcome: (pending — filled at the end of the run)
rounds: (pending)   cycles: (pending)   verifier: ran (reproducer + 14-cell corpus on the shipping artifact)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-chartsearchai/ba56d577-144b-4390-b852-2971c4b4a2b0.jsonl

## Refuted by measurement
- "the drop always costs the LAST injected finding" (written into production code, a test javadoc and
  PROVENANCE.md) -> 7 of the 8 drops, not 8: the Methotrexate cell dropped record [355] with [356]
  cited. Caught by re-reading my own capture set, not by a reviewer. · cost: 4 correction sites
- "the count drives it" / "the output budget drives it" / "the record is materially different" —
  the ticket's own three hypotheses, all three refuted by the 14-cell sweep before any code · cost: 0
- "maxPairChips can vary the finding count" -> a 7->2 cap sweep left `carried` at 7 throughout; the
  drug-in-play arm is uncapped · cost: 1 sweep (~2 min)
- "the clause belongs in DEFAULT_SYSTEM_PROMPT" (the plan's own arm, chosen as the LOWER-risk
  position) -> made completeness WORSE by a cell (8/12 -> 9/12) and cost 72% more output. The SAME
  126 characters after the question took it to 6/12 and made answers 40% shorter. POSITION was the
  variable and nothing in the plan predicted that · cost: 1 full A/B (~25 min) + one revert
- "the ordinal-in-the-record lever is lower risk" (the plan's v1 primary, and the ticket's own
  first-ranked lever) -> the refutation gate showed it reddens two assertions of a passing test;
  three further reasons followed. Dropped before it was built · cost: 0 code

- "the wording measured by a question-suffix probe can be shipped in the system prompt" -> POSITION was
  the variable the plan never considered. Same 126 characters: ahead of the records it made
  completeness WORSE (8/12 -> 9/12 short) and cost 72% more output; after the question it went to
  6/12 and made answers 40% shorter · cost: 1 full A/B (~25 min) + a revert
- "the separator is incidental" — written into the production comment as "the one difference from the
  arm is the separator" and shipped as a newline. It cost a VERDICT LEAD: verdict-led 12/12 -> 11/12,
  one answer opening "Ciprofloxacin interactions with active orders are:" with no call. The comment
  named the risk and the gate found it one round before it would have shipped · cost: 1 build +
  deploy + 14-cell corpus (~20 min)
- "arm C's aggregate is what the shipping artifact will do" -> the artifact had to be measured
  separately, and the first build did NOT reproduce it (7/12 vs 6/12) for exactly the separator
  reason above. The corrected build reproduced it to the character (6/12, 0 ratings, 12/12
  verdict-led, 564 mean chars) · cost: the arm above
- "the clause makes the model write a list" -> six of the twelve shipped answers contain no newline at
  all and completeness improves anyway; the arm that DID produce nine newlined answers is the one that
  made completeness worse. The obvious follow-up (enforce the lines) has a measurement against it
  · cost: 0, caught in Phase 1 pass 2

## Raised by a fresh agent, missed by the author
- [gate] The `total <= 1` guard would NOT keep every FINDING_PREFIX assertion green:
  `UncorroboratedFindingProvenanceTest.twoRulesOfOneEntryAreEachAnsweredOnTheirOwnMatch` raises two
  findings about ONE subject. The plan asserted the opposite, and estimated "about 25" sites where
  the gate counted 21 (12 of them `assertEquals`). · blocking · cost: 0 rounds (pre-code)
- [gate] An unmeasured-extent `problems` entry would fail the scorer's own `--selftest` on
  `shipped-clean` and four other arms, and the escape route (re-capturing the fixtures) is barred by
  PROVENANCE.md. Plan v2 had independently taken the census-line design; what the gate added was
  that `_blank_cell` must gain the keys or an unreadable cell raises KeyError. · blocking · cost: 0
- [gate] The named Java exemplars were wrong: `InteractionFindingChartOrderBridgeTest` routes every
  case through `onlyFinding` (one finding). The several-findings-one-subject shape is
  `SafetyFindingCitationExtentTest.setUp` -> `DrugReferenceTestSupport.injectedFindingsOver`.
  · non-blocking · cost: 0
- [gate] §3's diagnosis was taken pre-merge and main's Decision 82 moves the `safety_finding`
  numbering for this very patient — so arm A had to be re-captured post-change rather than carried
  across. · non-blocking · cost: 0

- [phase1-p1] A javadoc `{@link #search(String, List, String)}` left dangling by removing that
  overload — the build does not catch it (the guard for that is PR #372, unmerged) · non-blocking
  · cost: 0
- [phase1-p2] Only 6 of 12 shipped answers use a newline, so "the clause makes it write a list" is a
  false inference a reader would draw from the clause's own wording · non-blocking · cost: 0

## Where a skill blocked or contradicted this run
- resolve-ticket Step 4 ("cut from an up-to-date default branch") could not be followed: the ticket's
  entire gate (`findingCitations`, #395) was on an unmerged branch whose PR was CONFLICTING with main,
  both sides having added a "Decision 82". Merging main into that branch first, renumbering to 83 and
  fixing its five pointer sites was prerequisite, not scope — and it also left the reference
  instruction file 300 bytes over its budget with no prose added, which is the state
  ProjectInstructionsGuardTest's own javadoc records of #379/#280. The skill has no step for "the
  gate you are told to work against is not on main yet".
- resolve-ticket Step 2's delegation guidance ("delegate the searching only when broad") held up; the
  refutation gate was worth far more than any search agent would have been.
- resolve-ticket Step 9's verifier assumption: the run had to restart the standalone THREE times, and
  the first restart silently emptied the querystore index — `chartMode` still read `fullChart` while
  the chart dropped from ~348 records to ~9, so the first shipping-artifact arm was measured against a
  different prompt and was not comparable. Nothing in the response says the chart was short. The tell
  was the citation indexes ([10]-[15] where the pre-restart arm cited [349]-[355]); the fix was
  `querystore.bootstrap.autostart=true`. `POST /moduleaction {"action":"restart"}` also fails on this
  module with a JobRunr teardown error, and after a restart the module comes up `started: false` with
  no error message and needs an explicit start. None of that is in any skill.
- Running `mvn` in the background and then editing files raced the build twice, producing two invalid
  red results that cost a re-run each. The skill's Step 6 says to build from the root but says nothing
  about not editing while it runs.

## pr-harden rounds
- **Round 1** (PR #398 @ 6c90572a, reviewer fresh): **3 blocking**, 3 non-blocking. Every blocking
  finding was mutation-verified by the reviewer, and all three are about the CHANGE'S OWN GUARDS
  rather than its measured result:
  - r1-1 the clause is appended on the interaction-SCREENING population too (#113), where the
    question names no drug, so its subject "it" has no antecedent — and the corpus is 14 cells all
    phrased `should i give {drug}?`, so no arm contains such a cell. My stated evidence for
    "self-gating" (the two absent-data cells were unmoved) is explained by the GATE — those cells
    carry the flag FALSE — and says nothing about flag-true-with-no-drug. This is the
    "unmeasured change" the ticket rules out, found by reading the population and not the diff.
  - r1-2 the flag is evaluated INLINE at both call sites with nothing pinning it is the POST-inject
    chart; hoisting it above `inject()` makes it unconditionally false and the whole build stays
    green. Every sibling chart-derived local (#178, #229, #354) is hoisted with an explicit
    "after inject() deliberately" comment. My own `theFlagTheGateComputesIsWhatTheProviderIsHanded`
    structurally cannot see it — its stub injector returns the chart unchanged.
  - r1-3 the measured clause is asserted by substrings, so an ADDED imperative ships green
    (` State your verdict first.` → 2189 green) — and an added imperative in this exact position is
    the measured hazard, ADR 84's `, and nothing else` arm having lost the verdict lead. The repo's
    idiom for measured prompt text is an exact-bytes pin, in three named classes.
  The lesson the round teaches is one shape three times: **a guard written by the author of the
  change is tested against the mutation the author already had in mind.** All three survive the
  suite, and the reviewer found each by asking what the guard's assertion permits rather than what
  it forbids.

### Round 1's fix — all six implemented, none declined
The fixer refused the reviewer's own *narrow* remedy for r1-1 and was right to: it measured all
three of the remedies the finding offered and the phrasing gate
(`QueryScopeRouter.isInteractionScreening` negated) is **worse in both directions** — a question
carrying the screening cue whose findings name the one drug it names would lose the clause for no
reason, and a two-drug question carrying no cue would be sent it. What it shipped instead is the
clause's own precondition: several findings **and all of them naming one drug**. That is the
distinction the reviewer's finding pointed at without naming, and it took a measurement to see.

Also worth keeping: the fixer found a **third home** of a claim two earlier rounds had already
corrected in two — the same falsified KV-cache sentence, this time in a test class's javadoc. A
sweep for a corrected claim has now missed a home on three consecutive rounds of one change, each
time in a file the correcting round had open. Grepping the claim's rarest token over the whole tree
is not enough when the claim is a SENTENCE that can be rephrased; what caught it was re-running the
mutation and reading which cases actually redden.

### Round 1's runtime verification — and two instruments the ORCHESTRATOR got wrong
The verifier answered the question the fixer could not (the narrowing withholds the clause from
**none** of the fourteen cells, and does withhold it from the screening cell, at a measured 28-token
deficit) — and it did so by refusing both instruments I handed it.

- **I prescribed byte-identity as the test** ("greedy decode with `--cache-reuse 0`, so an unchanged
  prompt gives an unchanged answer"). The premise is true and the instrument is still invalid,
  because it cannot separate the change from the corpus: rebuilding the querystore index shifted the
  chart text by a uniform 12 tokens, which re-worded 11 of 14 greedy answers and moved the
  completeness cell by one in EACH direction. Had the verifier obeyed the brief it would have
  reported 3-of-14 identical and a 6→7 regression, and both readings would have been about the index
  rebuild. What it substituted is strictly better and is the shape to reach for next time: a
  **cross-build differential on a quantity the change is known to move (input tokens), with the
  cells the change cannot reach as the drift control** — here the two zero-finding cells, where the
  clause is absent in both builds by construction.
- **I told it to read finding subjects off `safetyWarnings[]`.** That is unsafe and it caught it by
  doing what the observability memory says — validate the model on a known cell first. The chips are
  a larger population than the carried findings (one cell: 17 chips against `carried` 8, nine of them
  contraindications naming five drugs never injected), so the naive read gives 6 subjects for a cell
  with exactly 1 and would have reported the clause withheld from a cell that received it. **The
  wire-safe read is `references[]` where `resourceType == 'safety_finding'`** — literally the key
  the production accessor splits. This is the third time on this ticket that a plausible
  re-expression of a production predicate produced a confident wrong number; `CLAUDE.md` already
  forbids it in scripts, and a subagent BRIEF turns out to be the same hazard with no rule covering it.
- **A memory of mine was falsified within the day.** `project_querystore_index_lost_on_restart`
  recorded `querystore.bootstrap.autostart=true` as the fix; the restart wipes the Lucene directory
  but the bootstrap tracking rows survive reading COMPLETED, so autostart declines to rebuild and the
  status endpoint reports `complete: true` over an empty index. Corrected in place: the fix is an
  explicit per-patient `reindex`, and the only thing that confirms it is the citation range.

- **Round 2** (PR #398 @ 9e0d88a7, fresh reviewer in an isolated worktree): **3 blocking**, 2
  non-blocking. Every blocking finding is one class: **a guard that holds only the direction its
  author was worried about.**
  - r2-1 the context test round 1 added asserts `Boolean.TRUE` at both legs, so nothing observes a
    call site handing FALSE — `= true;` at both sites leaves 2192 green. Round 1 closed the
    literal-`false` revert and left its mirror open, which is the direction with the safety
    consequence (the clause reaching the absent-data prompt and the several-drug screen).
  - r2-2 `findingSubjects`' split at the `:` is a no-op in every arrangement the suite builds,
    because they all inject interaction findings only — `subjects.add(key)` leaves 2192 green, and
    the population it silently withholds the clause from ships today (an allergy contraindication
    beside interactions about one drug, #383's own feature).
  - r2-3 the rating key's A/B measurability refusal has **no fixture** — deleting it leaves the
    selftest at "OK (30 arms)". Its extent-key twin is guarded. The symmetric refusal was itself a
    round-0 blocking finding, added because the asymmetry was the fail-open; it was added without
    the fixture that would hold it.
  - **Two false claims in the PR BODY fell out of establishing those**: "an ungated clause reddened
    AbsentDataEvalTest" is true only of the provider-internal ungate and never of a call site, and
    "removing either A/B refusal reddens --selftest" is false of the rating half. Both were mine.
  The pattern across rounds is now legible and worth stating: **each round's fix ADDS a guard, and
  the next round's finding is that the new guard is one-directional or unpinned.** Round 0 pinned
  the flag's journey; round 1 pinned its bytes and hoisted it; round 2 finds the journey pinned in
  one direction and the split it now depends on pinned in none. A guard added under time pressure
  gets tested against the mutation its author already had in mind, and nothing else.

### Round 2's fix — all five implemented, and the fixer corrected its own reviewer
Two things from this round are worth more than the fixes.

**The fixer refuted a premise inside the finding it was implementing.** r2-3's evidence said "every
committed fixture carries `unstatedFindingSeverities`"; enumerated across all 22 fixture
directories, the pre-#397 fixtures carry **neither** key. The refusal could not fire because no
committed PAIR disagreed — a different fact with a different remedy — and the fixer had already
written the reviewer's version into four places before catching it, then corrected all four plus two
adjacent pre-existing claims that were loose the same way. A finding's EVIDENCE is not privileged
over the tree; the loop's rule that a fixer must not soften a finding is not a rule that its
premises are true.

**It deleted an unattributable figure rather than re-attributing it.** `directness=1/1
expected_lead_match=1/1 safety_violations=0` could not be traced to a capture, so a plausible
provenance would have been the exact error `CLAUDE.md` forbids. What replaced it is measured: the
directness scorer prints "no scoreable cells found" over this capture shape because it reads the
yes/no harness's cell ids. **Deleting a figure you cannot source is a fix, not a loss** — and it is
the option a round under pressure to look thorough will not reach for.

One sweep found NOTHING and said so ("removing either A/B refusal reddens --selftest" has no in-repo
home — it lives only in the PR body). A sweep that reports zero homes is worth as much as one that
reports five, and only one of the two is ever written down.

- **Round 3** (PR #398 @ 71f47f92, fresh reviewer, isolated worktree): **2 blocking**, 2
  non-blocking.
  - r3-1 **the flag's LAST hop was the one with nothing on it.** Every other link in the chain is
    pinned — the gate's read position, both call sites, the append condition, the blank-question
    guard — and `LlmProvider.search`/`searchStreaming` forwarding the flag into `buildUserMessage`
    was not. Swapping both to the retained flag-less 3-arg overload leaves 2194 green. It is the
    same shape as the mistake this change shipped in its first hour ("an overload production called
    instead would be silently bypassed by every test double"), whose own javadoc explains why the
    flag is a parameter of `search` — the rationale simply was not applied one level in. The
    reviewer also established the asymmetry: hardcoding `true` there DOES redden one case, so the
    hop was uncovered in one direction only, which is the third instance this run of a guard that
    holds the direction its author feared and not its mirror.
  - r3-2 ADR Decision 84's table lost a row in the copy and kept the sentence written for it: four
    rows partitioned as "the first three … the last two". The dropped row is the wording-selection
    arm, so the `\n`-separator row — the one carrying the change's only measured safety cost, and
    the sole evidence for the space separator being load-bearing — reads as attributed to both a
    prompt-override simulation and the deployed build. The eval README's copy is correct. **A table
    and its prose partition drifted apart inside one document while both halves stayed
    individually plausible**, which no guard in this repo can see.

### Round 3's fix, and an error inside the reviewer's own premise (again)
All three tree findings implemented. Checking r3-4's premise that "the committed artefacts are all
current" turned up a fourth error nobody had asked about: **the #384/#395 window was stated backwards
in four places.** `unstatedFindingSeverities` shipped two days BEFORE `findingCitations`, so a real
capture taken between them carries the rating key without the extent key — one fixture's shape — and
two of the four places had attributed that to the OTHER fixture, describing "the shape a build between
#384 and #395 produced" for a shape no build ever produced. It also found a THIRD home of r3-3's
claim, which the finding's own sweep had missed because the claim was rephrased there.

- **Round 4** (@ f5fe0b1d): **ZERO blocking.** Three non-blocking, two of them false claims in shipped
  javadoc: two published test-double counts ("nineteen" against 20 sites, "a dozen" against 16), and
  "null-tolerant in both arities" for a method with one arity. **The nineteen was right until this very
  change added a double of its own** — the figure went stale inside the commit that published it, which
  is the root `CLAUDE.md` rule landing one file outside what `ProjectInstructionsGuardTest` can police.
- **Blocking-only round** (@ 1ee14fcd, the price of an edit after the clearing round): **zero
  findings**, every surviving sentence verified true of the code beside it. It also counted the same
  population a third time and got a third number (17 sites / 15 files against the reviewer's 16 and the
  javadoc's 12) — which is the case for DELETING a count rather than refreshing it, made by accident.

## /harden cycles 2 and 3 — the part the run originally skipped
Cycle 1 applied nine findings and cycle 2 was owed and not taken; the override was on the record. It
was then taken, and it was not a formality.

**Cycle 2 (the three lenses that never ran during the ticket: reuse, efficiency, integration): 12
substantive findings.** The two that mattered most were sentences in the CLIENT document, one of them
fail-open: `README.md` told a client to read the carried findings off `references[]`, which carries only
the CITED subset — on the documented reproducer that shows six of seven, so a client counting hazards
from it under-reports a dropped one. And the whole ledger had been captured on `chartMode=fullChart`
while `config.xml` ships `queryScoped`.

**Cycle 3 Phase 1, four passes: 6, then 2, then 4, then 2 substantive.** Pass 4 was the first whose main
result was confirmation — 16 of 18 disclosures rechecked and held, the production edit no pass had
reviewed found correct, the scorer surviving a 15-mutation battery.

### The queryScoped arm, and a refusal worth keeping
The measurement cycle 2 declared owed came back **inconclusive on the clause's sign and decisive on the
harms**: under the shipped default, verdict lead is at CEILING (12/12, so it cannot have been lost),
ratings at FLOOR (0), abstention held — absolute readings that need no second arm. Completeness was 2 of
12 short, the best figure on record, and the agent **refused to claim it** as the clause's doing, since
a 4-cell mode gap measured once is equally consistent with the mode change.

**The second arm was unobtainable and it refused to substitute.** The omod kept for it contained the
clause — its "PRE" meant pre-review-round-1. Every genuinely pre-clause build on disk is also pre-#395,
and `score_probe_safety.py` REFUSES that A/B by design. **The measurability refusal added in the
original change blocked the run's own attempt to measure it** — the gate working against its author,
which is the strongest evidence it was worth adding.

It also corrected cycle 2's caveat: the ~8.6KB the ledger blames is `DEFAULT_SYSTEM_PROMPT`'s own
length, a compile-time constant `chartMode` does not shrink, so the causal mechanism transfers to the
default unchanged. What shrinks is the user message.

### THE ORCHESTRATOR'S OWN WORST ERROR, and how it was caught
I argued from byte-identical control answers that the wording-selection arm carried no clause on the two
abstention cells. **Wrong, and wrong by ignoring a warning I had caused to be written**: the same README
says byte-identity is not that instrument and designates the zero-finding cells as the drift CONTROL
*because they do not move*. A reviewer caught it and named the recorded instrument; per-cell
`input_tokens` settled it the other way — 11,381 against 11,353 and 11,380 against 11,352, exactly the
clause's 28-token cost. **The lesson is not "check your inferences" but something narrower: when a
document warns that an instrument is invalid for a population, that warning binds the person who wrote
it.** Sharper still, those cells' `output_tokens` differ (99/118, 162/115) while the published `answer`
does not — byte-identity of a published field is weaker than byte-identity of a completion.

### The disclosure treadmill, named and broken
Passes 2, 3 and 4 each found the previous pass's DISCLOSURE wrong while its CODE was right. Pass 3
diagnosed it: each pass rewrote a residue paragraph without mutating the new sentence first — this
project's own rule, applied to code and never to the prose about code. Pass 3's fixer then logged
**sixteen** falsifiable sentences each with the mutation run BEFORE writing it, and pass 4 found 16 of 18
held. **That is the fix: a disclosure is a claim, and it gets the same mutation a guard does.** The two
that still failed shared one root — the mutations chosen (`HashSet`, `TreeSet`) both normalise to
ascending over the test chart's index values, so neither could falsify the sentence being written.
**Choosing a mutation that CANNOT falsify the claim is the failure mode one level up.**

### Refusals by fixers, which is what made the cycles work
Five separate agents refused a premise handed to them: a finding's evidence about every fixture; a
finding's suggested wording that was arithmetically wrong; a reviewer's remedy that measurement showed
worse in both directions; a lens's suggested test placement that could not observe the edit it named;
and two premises of mine. **A brief is not privileged over the tree, and the good agents on this run
treated mine as a claim.**

### Cycle 3 Phase 2 and cycle 4 — the first work that made the tree smaller
**The quality lens found eleven substantive items in a class three other lenses and five correctness
passes never touched: what nine review passes LEAVE BEHIND.** Over the four production Java files the
slice had added 402 comment lines against 65 of code, 6.2 to 1. The sharpest instance is worth keeping
as a shape: **the 82-line comment whose first paragraph forbids a third copy of the ledger contained
one in its last paragraph.** Also a case name that named the one property its case had come to refuse
to pin, with two javadocs citing that name as the pin; and a client-contract paragraph grown from 1,086
to 3,413 characters, five directives deep, in a section whose every other paragraph is one directive.

Phase 2's fix was 197 insertions against 239 deletions — **the first pass on this change to leave the
tree smaller** — and deletion carried a failure mode nothing before it had: cutting the only home of a
fact. Requiring a per-deletion ledger (what was cut, where the fact lives now, the grep that FOUND it
there) is what made it safe; the two facts with no other home were MOVED, not dropped.

**Cycle 4 verified that independently and found one that had been missed** — a fact living nowhere —
plus a false premise that predated Phase 2 and had been made more prominent by it: the client contract
told clients the #397 request "says nothing about the answer's LAYOUT" when the shipped clause's
dominant verb is a layout instruction. ADR 84 held the correct form all along; the two homes disagreed
about the prompt's own bytes and the client-facing one was wrong.

### The grep that cannot see a wrapped phrase, twice in one run
Checking my own replacement against ADR 84, a line-scoped grep reported two of five claims MISSING when
they were merely line-wrapped. An earlier pass hit the identical false negative and said so, and I
walked into it anyway. **A sweep for a SENTENCE must flatten whitespace; a line-scoped grep over
wrapped prose is a fail-open check** — it reports absence, which is the direction that invites writing
a "correction" over something already correct.

## What this run says about the loop itself
**Every round's fix added a guard, and the next round's blocking finding was that the new guard was
one-directional or unpinned.** Round 0 pinned the flag's journey; round 1 pinned its bytes and hoisted
the read; round 2 found the journey pinned in one direction and the split it now rested on pinned in
none; round 3 found the chain's last hop unpinned in both. Nobody was careless — each guard was
mutation-verified by the agent that wrote it, against the mutation that agent had in mind. The class of
defect that survives is **the mirror of the mutation you just proved.** Worth carrying into the fixer
brief as a standing question: for each guard you add, what is the cheapest edit that satisfies its
assertion and still breaks the property, and is the OTHER value of this boolean observed anywhere?

Second, smaller: **three separate agents produced three different counts of one population.** No count
of code that a build cannot check should be published, and the argument a count is supporting usually
does not need it.

## Declined
- (none — no round of this run declined a finding, blocking or otherwise, across four review rounds
  plus a blocking-only verification round)
- Consciously NOT fixed, on the terminating round's own framing ("states nothing false under the
  reading its own context forces ... offered for the author's judgement, not as a gate"): three prose
  observations in `LlmProvider`'s javadoc — a displaced antecedent for "that mistake", "the same seam"
  asserting a set identity that holds only loosely, and an under-explaining participle. Fixing them
  would have owed a further blocking-only round for three words of comment, which is the
  false-convergence treadmill the loop exists to avoid.

## Assumptions overturned
- A2 ("widen the corpus from the ticket's one cell to 14") turned out to be load-bearing rather than
  thorough: the single cell cannot separate any of the ticket's four hypotheses, and the 8-of-12
  baseline is what made the system-prompt arm's one-cell regression legible as a regression.
- A4 (add an `unstatedFindingSeverities` cell beside the completeness one) was written as a judgement
  call and was then confirmed by measurement within the hour: a bare list instruction states all
  seven findings and drops all seven ratings, and the completeness cell alone scores that a win.
