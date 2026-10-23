# 2026-10-23 window — five measurement notes, no run record

The proposals as submitted to the first gate, then each gate's verbatim reply followed by what the proposer
checked and changed. Paths under `/private/tmp/…/scratchpad/` were this session's scratchpad. The instruments
they name are kept, some in later versions, in `artifacts/2026-10-23-retro-window/`, whose README maps each
figure to its output.

# skill-retro 2026-10-23 — proposals for refutation

Window: every file in `~/.claude/skill-lessons/` newer than `LAST` (2026-10-14). `confidential/` is out
of scope: do not read it. None of the five files below is named in `~/.claude/skill-lessons/REJECTED.md`.

| file | what its own header says it is |
|---|---|
| `2026-10-18-pr-review-lens-replay-queue96.md` | measurement, `pr-review`, openmrs-module-queue PR 96 at `ae7f8fa2` |
| `2026-10-18-pr-review-heldout-6551.md` | measurement, `pr-review`, openmrs-core PR 6551 at `b7cd909d` |
| `2026-10-18-pr-review-union-routing-556.md` | measurement, `pr-review`, chartsearchai PR 556 at `d77204ba` |
| `2026-10-18-pr-review-reviewer-brief-556.md` | measurement, `pr-harden/reviewer.md` vs `pr-review`, same subject |
| `2026-10-23-harden-phase1-confirming-pass.md` | measurement, `harden`, 57 public runs' transcripts |

**No run record.** Each note's artifacts are under `~/.claude/skill-lessons/artifacts/<the note's dir>/`.

**The bar (skill-retro Step 3), one of:** (a) the lesson appears in two or more run records; (b) it
appears once and cost two or more rounds or cycles in that run; (c) a skill contradicts itself or its
own gate script, valid from one instance. On measurements, `REJECTED.md` :2145-2150: "A measurement is
admissible as argument ... and has never counted as corroboration."

**Re-derivations run for this pass.** They are listed so you can re-run them, not so you take them on
trust. `S` below is `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-querystore/80e823df-8753-4cb7-8aa5-6aead0d9104a/scratchpad`.

1. `python3 -I <dir>/scripts/score.py <dir>` in each of `2026-10-18-heldout-6551`,
   `2026-10-18-lens-replay-queue96` and `2026-10-18-union-routing-556` prints each note's table.
2. `reviewer-brief-556` has no adjudication. Over its `out/wt-*.json`, `/condition[- ]?mediated/i` matches no
   element of any `findings` array. The same pattern hits `PROTOCOL.md` 4 times, and outside the findings it
   hits wt-02 (R) once and wt-05 (P) three times.
3. `bash artifacts/2026-10-23-harden-phase1-confirming-pass/scripts/rederive.sh $S/rederive-harden`
   reports all three stages identical to the kept copies, and prints the note's rates.
4. Harden's gate sentences: `python3 -I $S/scripts/gate_sentences2.py <tsv> [jsonl…]` counts assistant
   text blocks matching each gate's opening words, and reports the skill's bracketed template apart.
   - Controls: `$S/control-good.jsonl` and `$S/control-good2.jsonl` are counted (all five patterns between
     them). `$S/control-bad.jsonl` (the template inside a user message, and a user message holding the
     phrase) is not.
   - Over `artifacts/2026-10-23-harden-phase1-confirming-pass/public-transcripts.tsv` (66 transcripts): 0
     blocks for the Transition gate's three sentences and for the Termination gate's, 1 for the Phase 2
     gate's, 58 template blocks.
   - Reader check over the same files: 3193 assistant text blocks, 184 naming "Phase 1", in 56
     transcripts.
5. `lens-replay-queue96` `out/*.json`: 6 of 12 runs carry a `could_not_verify` item matching
   `/transaction|roll ?back|rolled back|TransactionInterceptor/i` (C 1, T 2, B 0, V 3). The pattern hits
   `PROTOCOL.md` 21 times.
6. Filed dispositions, read from `adj/adjudication-input.json` through each private map:
   - 6551 L04: 6 of 6 `question`.
   - queue96 K1: 7 `blocking`, 1 `suggestion` (wt-03, arm C).
7. `artifacts/ab-6551/arm-A-control.json` finding A1 (the 2026-09-11 A/B's control) is the defect the
   heldout key calls L02, and was filed `blocking`.

---

## P1 — harden *Transition gate*: "say BOTH out loud" becomes "say them out loud"

**Claimed bar: (c).**

- `~/.claude/skills/harden/SKILL.md`:77 reads: `**Transition gate** (forcing function — say BOTH out loud in the
  report before moving to Phase 2):`. Three blockquoted sentences follow, at :79, :81 and :83.
- :85 reads: "If you cannot truthfully complete ALL THREE sentences, the slice is NOT ready for Phase 2".
- In the repository (`/Users/danielkayiwa/Projects/openmrs/querystore`),
  `git log -S 'say BOTH out loud' -- .claude/skills/harden/SKILL.md` names `01d1cd2` (2026-05-20), when
  the gate held two sentences.
- `git show c18bf29 -- .claude/skills/harden/SKILL.md` (2026-05-20) adds the third sentence and turns the
  closing line's "BOTH sentences" into "ALL THREE sentences". It leaves the header's "BOTH".
- How it surfaced: the 2026-10-23 note's "Harden's three Phase 1 sentences never appear filled in" sent
  this pass to the gate.

**Edit.** In :77, `say BOTH out loud` becomes `say them out loud`. That is word-neutral:
`LC_ALL=C wc -w` gives 7429 before and after, the budget in `skill-retro/skill-budgets.json` is 7429, and
no `raises` entry is needed. :85's "ALL THREE" stays, since it is accurate. `harden` goes from 0.44.0 to
0.44.1 (patch, a correction).

**Step 4.** It prunes nothing and adds no count. `evidence.md` has no entry for the Transition gate (its
`## Phase 1` heading holds only `### Test coverage`), so nothing moves there.

**Not proposed:** deleting the Transition gate. See L5.

---

## Ledger entries proposed (no skill text): check each against the notes, the artifacts and `REJECTED.md`

**L1 · Measured and not adopted.** Skill-text candidates the notes tested, each recorded in that note's
own *Refuted* section. REOPEN ON for each: a measurement on another PR where the candidate moves a
pre-registered outcome.

- **(a) A verification-techniques file beside `pr-review`, with a Step 3 pointer** (6551, arm K).
  - Usage: read in 3 of 3 runs, the non-admin probe run in 1, `NOT_SUPPORTED` in 0.
  - Pre-registered outcome, L04 filed blocking: 0 of 3, against the control's 0 of 3.
- **(b) A *Trace outward* thread naming transaction, flush, handler order and required privilege** (queue96,
  arm T). Its runs asked the transaction question 3 of 3 times and answered it wrongly 3 of 3 times (K7 0
  of 3). That was on the subject the text was written from, by an author who knew the answer.
- **(c) The per-condition mutation sentence** (queue96 T, 6551 M).
  - On queue96, T's unique REAL defect (the unpinned `purgeVisit` guard, 2 of 3) was not a pre-registered
    target.
  - On 6551, M hit 0 of its targets (L06/L08). The mutation-shaped finding there came from C and K.
- **(d) "A concern is cleared by the same bar a finding has to meet"** (queue96, arm V, added after waves
  1-2): K7 0 of 3. wt-12 wrote "cleared by reading the proxy chain only, so not raised", against that
  very sentence.
- **(e) Withholding the PR conversation** (queue96, arm B): K7 0 of 3. That refutes "the approving
  review's wrong answer kept rounds 1-3 from rechecking".
- **(f) Porting `pr-harden/reviewer.md`'s "never against the PR description" sentence into `pr-review`**
  (556, arm P; ported with "agent" read as "person"): S1 0 of 3. The pipeline's own round-1 brief with
  today's `reviewer.md` (arm R) also found S1 in 0 of 3. The 2026-09-29 round-1 catch therefore rests on
  one run.

**L2 · Parked, measurements only (0 run records).**

- **(a) A demonstrated privilege bypass filed below blocking.**
  - 6551 L04: all 6 findings filed as questions (2026-10-18). Among them is wt-02, which ran the exploit
    with a positive control. The 2026-09-11 A/B's control filed it as a suggestion
    (`2026-09-11-…-ab.md`:46-47).
  - queue96 K1: 7 of 8 filed blocking.
  - `pr-review`:152 sends "uncertainty about an observed side effect" to a question and names no exposure
    case. :158 makes a written failure mode the blocking test.
  - Candidate: a disposition rule, "a privilege bypass you have demonstrated is blocking". 556's U arm
    carried it, untested, because nothing in 556 is an exposure.
  - REOPEN ON: a run record where a demonstrated exposure was filed below blocking, or a measurement of
    the rule on a PR that holds one.
- **(b) Routing `could_not_verify` to the author.**
  - 556 U: 25 risk items, 19 NOISE. Routing alone reached neither S1 nor S3.
  - queue96: 6 of 12 runs held K7's unobserved rollback there (C 1, T 2, B 0, V 3) and raised nothing.
    On that counterfactual, routing would have put K7 before the author 6 times.
  - REOPEN ON: a run record where a `could_not_verify` item was a blocker the round did not raise.
- **(c) A union of two independent single-context reviewers** (556 U): S3 surfaced in 1 of 3 pairs,
  against 0 of 3 controls, below the pre-registered bar. It is not per-dimension fan-out, so it is not
  P-A's condition. REOPEN ON: a pre-registered measurement where a union clears its bar.
- **(d) Limitations a PR declares, accepted as scope.**
  - On 556, S1 and S3's narrow form both sit inside limitations the description declared, and all nine
    `pr-review` runs accepted them.
  - The closest tested remedy is L1(f), at 0 of 3.
  - REOPEN ON: a run record where a declared limitation hid a blocker.

**L3 · P-A (`REJECTED.md` :2143, :2307-2327): parking reason qualified, condition unchanged.**

- The 2026-09-11 A/B's run 1 found "no blocker the single context missed", with its control finding L02
  (A1, filed blocking). At `pr-review` 0.19.0, 0 of 3 controls and 3 of 9 runs found L02 (2026-10-18). So
  the run-1 half of "measured twice, no blocker yield either time" rests on a control draw that later
  measured 0 of 3, under a later skill version.
- queue96 at `ae7f8fa2` is a subject the reopen condition can use, since single contexts missed K7 12
  times in 12.
- No fan-out arm ran in any of the four measurements.

**L4 · Harden's parked "Phase 2 escalating repeatedly on the run's own fixes" (:5378, 31 records;
REOPEN at :3998-4000).**

- The remedy "enforce Phase 1's confirming pass" was measured observationally (57 public runs, 127
  classified traversals).
- After a re-review, 71% of first traversals and 45% of resumed ones escalated. After a mechanical check
  or nothing, the rates were 67% and 54%.
- The count is unchanged, because a measurement adds no record, and so is REOPEN. A proposal of this
  remedy has to beat these rates.
- Limits: observational, and one classifier who was neither blinded nor independent.

**L5 · Observed: harden's literal gate sentences are not emitted.**

- Over the 66 transcripts of public records:
  - 0 assistant blocks state the Transition gate's three sentences or the Termination gate's;
  - 1 states the Phase 2 gate's.
- Runs announce convergence in their own words ("Harden Phase 1 has converged.").
- 1 measurement.
- REOPEN ON: a proposal to delete or replace those sentences. This count is evidence that they are not
  said, not that they do nothing.

**L6 · Linter blind spot.** `skill-lint.py`'s `stated_counts` does not flag `harden`:77, for three
reasons: no noun from `NOUNS` follows "BOTH", "both" is not in `WORDS`, and blockquote items are not
counted.

- A scratch widening (number words without the noun list, plus "both", plus `>` items) gave 15 hits
  over 17 files, 1 of them true.
- "both" alone gave 3 hits, 1 true. The false ones are `harden`:198 ("it cuts both ways") and
  `verify-frontend-change`:64 ("needs both the node module and the browser binary").
- So there is no clean extension.
- REOPEN ON: a second stale count the linter missed.

**L7 · Observed, no remedy owed.**

- In the queue96 session, an Agent-tool canary subagent quoted the project's memory index, so those
  measurements ran headless `claude -p` from scratch clones instead. A grep of the pipeline skills for
  "memory" finds no claim about what a subagent loads; `skill-retro`:172 concerns this repo's
  `.claude/CLAUDE.md`.
- reviewer-brief-556's isolation finding: earlier briefs left the scratchpad's other folders readable.
  A scan of 30 earlier transcripts found no read. This is measurement hygiene, not a skill.
- queue96's round-1 handler-order miss does not reproduce: 12 of 12 runs found K6. Its 2026-09-16 cause
  is unknown, because those transcripts are not on this machine.

---

# Gate 1 — the reply, verbatim

P1 stands. L2, L5 and L7 need revising: there are six blocking objections and every one is settled by a citation, so nothing has to be parked. L1, L3, L4 and L6 stand with non-blocking fixes. Nothing should be killed.

**Something happened during this pass that affects L4 and L5.** At 12:40, two of the 66 transcripts were gone:
- `b2cc1733…` (PR440, 2026-09-16) and `56821ae1…` (PR465, 2026-09-21).
- My first run of `rederive.sh` and my detector runs earlier in this pass read both files. My scans pulled hits out of PR465.
- Running `rederive.sh` again now gives "DIFFERS" for `public-transcripts.tsv` and for `public-traversals.txt`. `post-edit-digest.txt` is still identical, so the escalation rates still re-derive.
- The oldest transcript left was last modified 2026-09-23 14:43. That fits a 30-day retention cutoff, but I have not established the cause.
- The note's population is built only from transcripts still on disk (`public-transcripts.py`:20), so it will keep shrinking.

## P1 — STANDS
- **Quotes and history check out.**
  - `harden/SKILL.md`:77 says "say BOTH out loud". Three blockquotes follow at :79, :81 and :83, and :85 says "ALL THREE sentences".
  - `01d1cd2` introduced "say BOTH out loud" (one sentence became two). `c18bf29` changed :85 to "ALL THREE" and left :77 alone.
- **Bar (c) is met** by a single instance. Nothing is counted twice.
- **The edit removes the stale count and breaks nothing.**
  - I applied it to a copy: `wc -w` gives 7429, matching the budget, and `skill-lint.py` reports 0 findings.
  - :77 is the only place the header text exists, in the live skills, hooks, ledger and `origin/main`.
  - The other uses of "both" or "two" refer to other things: :162 "two gates", :181 "report both", :198 "both ways", :268 "BOTH gates' entries".
  - `evidence.md` has no entry quoting :77, so the Step 4 sentence holds.
- NON-BLOCKING: "them" normally points back, and the nearest plural before :77 is the stop-condition list at :72-75. "say these out loud" is also word-neutral and points forward.

## L1 — STANDS (non-blocking fixes)
Every figure re-derived: `score.py` on all three notes, a technique scan over the 6551 transcripts, the `could_not_verify` items in queue96 `out/*.json`, and the reviewer-brief findings arrays.

- **(a), (b), (c), (f): stand.**
- **(d): stands, but the L1 header is wrong about it.** The header says each limb is "recorded in that note's own Refuted section". V is not in queue96's Refuted section (:73-85). It is recorded at :48 and :65-66. NON-BLOCKING.
- **(e): stands, but it names the wrong thing.** The note calls B "one withheld-context condition" (:2), not skill text. The candidate rule B actually tested is `PROTOCOL.md`:115-117: "a claim in the review conversation about runtime or framework behaviour is a lead to verify, whoever made it." Name that rule. NON-BLOCKING.

## L2 — REVISE
**(a) REVISE.** BLOCKING, settled by citation.
- The figures are right (L04 6/6 `question`; K1 7 `blocking` + 1 `suggestion`).
- The problem is K1. It is "a caller with Purge Visits but not Purge Queue Entries cannot purge" (queue96 `PROTOCOL.md`:85). That is a privilege check that refuses an allowed caller, not an exposure.
- Listed under "A demonstrated privilege bypass filed below blocking", it reads like a counter-instance.
- Fix: label K1 as fail-closed and not an exposure, or drop the bullet.

**(b) REVISE.** Two BLOCKING objections, both settled.
1. **Wrong attribution.** The 25 risk items and 19 NOISE are counts over all nine runs, not U alone (union `PROTOCOL.md`:103-105). Through the private map, U holds 18 items (13 NOISE, 4 REAL, 1 S3-match) and C holds 7 (6 NOISE, 1 REAL). Fix: "25 risk items over all nine runs, 19 NOISE; U's pairs 18, 13 NOISE."
2. **The REOPEN can never be met.** `could_not_verify` exists only in the measurement briefs (queue96 `brief-template.md`:73 and :96; 556 `brief-template.md`:66 and :89).
   - `pr-harden/reviewer.md`:26-33 returns `findings` and `notes`. A grep of `pr-review`, `pr-harden`, `resolve-ticket` and `harden` finds no `could_not_verify`.
   - So "a run record where a `could_not_verify` item was a blocker" cannot occur. The ledger struck a circular reopen of the same shape at `REJECTED.md`:2325-2327.
   - Fix: phrase the REOPEN on a reviewer's `notes`, or on a pre-registered measurement.

**(c) STANDS.** NON-BLOCKING: U was a bundle of union, routing and the disposition rule (`PROTOCOL.md`:41-49, `control-to-U.diff`). The one S3 came from a single reviewer (wt-07).

**(d) STANDS.** NON-BLOCKING: cite the run whose round-1 head these replays used, and say why it is not counted.
- `2026-09-29-…-555.md`:17 shows r1 raised S1 despite the declared limitation, and :31 shows the author's assumption overturned.
- r1 and r2 missed S3, but :19 attributes that miss to nothing.
- Also, "0 of 3" for L1(f) was measured on S1 only.

## L3 — STANDS
- Checked: `REJECTED.md` :2143, :2311-2313 and :2321-2323.
- A1 in `ab-6551/arm-A-control.json` is the cascade defect, filed `blocking`. L02 at 0.19.0 was found by 0 of 3 controls and 3 of 9 runs.
- No fan-out arm ran in any of the four measurements.
- NON-BLOCKING: the harness also changed. The A/B used Agent-tool subagents (queue96 note :82-83), while 2026-10-18 used headless clones without project memory. Add that next to "a later skill version".

## L4 — STANDS
- `rederive.sh` reproduced 71/45 and 67/54. The `REJECTED.md` cites (:5378, :3998-4000) are correct.
- NON-BLOCKING: "has to beat these rates" treats confounded observational rates as the benchmark a proposal must beat (note :99-101). Reword it as "show an effect this comparison did not".
- NON-BLOCKING: add the note's limit that 55 of the 57 runs are chartsearchai, from about one week (:107).
- NON-BLOCKING: add that the population stage no longer re-derives (see the top section).

## L5 — REVISE
- **Counts reproduce.**
  - `gate_sentences2.py` gives 0/0/0/1/0 and 58 template blocks. The controls behave as stated, and the reader check gives 3193 / 184 / 56.
  - My own scan of every JSON field (tool inputs, `Write` contents, PR bodies) found no filled-in gate sentence anywhere.
- **BLOCKING, settled: L5 contradicts harden's own evidence file and does not say so.** `harden/evidence.md`:214 reads "the phase gates demand verbatim sentences and get them". That claim dates from `5bc2d71` (2026-08-17). L5 must cite it and say this window contradicts it.
- **BLOCKING, settled: L5's evidence is not stored anywhere it will survive.**
  - The note's `scripts/` has no gate-sentence search.
  - `gate_sentences2.py` and its controls exist only in the session scratchpad.
  - Transcripts are already disappearing (see the top section).
  - Fix: archive the detector, its three controls and its output in the note's `artifacts/` directory.
- NON-BLOCKING: 66 is the wrong denominator.
  - Only 58 transcripts load harden.
  - Two (PR440, PR465) predate the Termination gate, which arrived in `6f15b1c` on 2026-09-22.
  - One is the 2026-10-18 measurement session.
- NON-BLOCKING: the title says the sentences "are not emitted", but the Phase 2 gate's sentence was emitted once.

## L6 — STANDS
- The three reasons are correct (`skill-lint.py`:16-18, :27, :36).
- My own version of the widening reproduces "both" alone at 3 hits, 1 true (harden:77, :198, verify-frontend-change:64). It reproduces 15 hits, 1 true, only when every number word on a line counts separately; counting lines gives 13.
- NON-BLOCKING: "no clean extension" claims too much. A case-sensitive `BOTH` check that also counts `>` lines gives 1 hit, 1 true across today's 17 files, though it is fitted to the one instance.
- NON-BLOCKING: the widening script is not archived.

## L7 — REVISE
- **BLOCKING, settled: the reason given for bullet 1 is false.**
  - `pr-harden/SKILL.md`:42-44 is a claim about what a subagent loads: "Any other subagent type starts clean. It still reads `CLAUDE.md` and the repo's skills". The grep missed it because the sentence never says "memory".
  - The canary contradicts that list: queue96 `PROTOCOL.md`:69-72, "A subagent of the orchestrating session sees that project's memory index". My own context as this Agent-tool subagent also carries the querystore `MEMORY.md` index.
  - Pipeline runs can load non-empty memory. #607's transcript is in the auditlogweb project and 3787's is in querystore, and both projects have memory directories.
  - Fix: park this as 1 measurement against pr-harden:42-44, with a REOPEN such as "a run record where a reviewer's or refuter's loaded memory carried content about the change under review".
- Bullets 2 and 3 stand (reviewer-brief note :43-49; queue96 note :50 and :80-81).

My scripts and outputs are in `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-querystore/80e823df-8753-4cb7-8aa5-6aead0d9104a/scratchpad/refuter/`. I wrote nothing outside that folder.

---

# Gate 1 — what the proposer checked, and what changed

Every citation the gate's blocking objections rest on was re-read before acting on it.

**Re-read and confirmed.**

- Both transcripts were gone (`ls`: no such file).
- `harden/evidence.md`:214 reads as quoted.
- `pr-harden/SKILL.md`:42-44 reads as quoted.
- The refuter's own transcript holds an `AutoMem` record carrying the `MEMORY.md` index (`grep -c
  adr_draft_mode.md` on it: 2).
- queue96 `PROTOCOL.md`:85 (K1) and :115-117 (B's candidate rule) read as quoted, as does its
  canary at :69-72.
- union `PROTOCOL.md`:103-105 reads "37 entries: 12 findings, 25 unverified risks … unmatched risks
  5 REAL, 19 NOISE". By arm, that is C 7 (6 NOISE) and U 18 (13 NOISE).
- `could_not_verify` appears in no live skill (`grep -rln` exit 1), and `reviewer.md`:26-33 returns
  `findings` and `notes`.
- `2026-09-29-…-555.md`:17, :19 and :31 read as cited.
- `git log -S` confirmed the commits that introduced each gate sentence. The Termination and Phase 2
  gate sentences came in with `6f15b1c` (2026-09-22).

**One gate claim corrected by measurement.** The gate wrote that "two (PR440, PR465) predate the
Termination gate". Those two transcripts held no harden template at all: 58 template blocks over 66
transcripts before the deletion, and 58 over the 64 left after it. A Termination-template count added
to the detector shows that every one of the 58 harden-loading transcripts loaded a version carrying
that gate. So the denominator for all five sentences is 58.

**Changes.**

- **P1.** "them" became "these", at the gate's non-blocking suggestion. Applied to the live skill as
  harden 0.44.1: two lines changed, 7429 words, linter 0.
- **L1.**
  - The header no longer says "Refuted section", and (d) cites the note's lines.
  - (e) now names the rule B tested.
- **L2.**
  - (a) K1 is dropped: it is a fail-closed check, not an exposure.
  - (b) carries the counts by arm, and its REOPEN is rephrased onto a pre-registered measurement or a
    reviewer's `notes`.
  - (c) records the bundle and the single reviewer.
  - (d) cites #555's record and says why that record is not counted again.
- **L3.** Adds the harness change.
- **L4.** "has to beat these rates" became "has to show an effect this comparison did not", and the
  limits were added.
- **L5.**
  - Cites `evidence.md`:214.
  - Uses the 58 denominator.
  - Its title no longer says "not emitted" outright.
  - Its instruments are archived in `artifacts/2026-10-23-retro-window/`: the detector, four controls
    and their outputs, and the pre-deletion results quoted verbatim.
- **L6.** "No clean extension" became the three widenings with their hits, and the widening script is
  archived.
- **L7.** The first bullet became a park against `pr-harden`:42-44, with two observations. The second
  and third bullets stand.
- **New, from the gate's report: transcript loss.**
  - `cleanupPeriodDays` is in Claude Code 2.1.309's settings schema with "default: 30", and is unset
    here.
  - 77 of 141 named transcript paths are gone (`transcript_presence.py`).
  - Five of them are under 30 days old: one placeholder, and four whose project directories are gone,
    cause not established.
  - This is recorded as an operator action, not a skill change.
- **One of this pass's own claims was corrected by its own instrument.** The README's first draft said
  "three of those 77 belong to records under 30 days old". `transcript-presence.out` lists five.

---

# Gate 2 (over the staged diff) — the reply, verbatim

**FIX: 4 blocking objections (2 in the ledger, 1 in the README, 1 in the commit message). The harden edit and the proposals file are CLEAN.**

I re-ran every archived script with `python3 -I`. All six `.out` files come back byte-identical and every run exits 0. `count_widening.py` reproduces only on a pre-edit tree, for which I used `git archive HEAD .claude/skills`. `rederive.sh` reproduces the rates 71/45 and 67/54, and the first two stages still differ. `skill-lint` reports 17 files and 0 findings, with and without `--against origin/main`, and also 0 on the pre-edit HEAD. My scratch outputs are in `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-querystore/80e823df-8753-4cb7-8aa5-6aead0d9104a/scratchpad/refuter2/`.

## 1. `harden/SKILL.md`: CLEAN
- It is 7429 words, which equals `words.harden` in `skill-budgets.json`. 0.44.0 → 0.44.1 is a patch for a correction, as Step 6 asks.
- The staged copy is identical to the live copy.
- The history checks out: `01d1cd2` wrote "say BOTH out loud" when the gate had two sentences, and `c18bf29` added the third sentence and changed :85 but not :77.
- `evidence.md` has no Transition gate entry.
- The file now reads consistently: :77 "these" introduces the three quoted sentences at :79, :81 and :83, and :85 says "ALL THREE". The "two gates" at :162 means the Transition gate and the Phase 2 gate. The old wording appears in no skill, hook or `CLAUDE.md`.

## 2. `REJECTED.md` :5421-5600: FIX
- **BLOCKING · :5429 "`confidential/` was not read, as Step 1 requires."** This is false. In the main transcript `~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-querystore/80e823df-8753-4cb7-8aa5-6aead0d9104a.jsonl`, line 34 (09:13:17Z) ran `ls ~/.claude/skill-lessons/confidential/` and `cat …/confidential/PRS`. Lines 559-571 and 1021 then ran `awk` over `PRS`. Step 1 allows exactly that ("Read only its `PRS` and its file names", `skill-retro` SKILL.md:35-37). I printed only the commands, with digits blanked, and none of their output.
  - Fix: "Nothing was drawn from `confidential/`; only its file names and `PRS` were read, as Step 1 allows."
- **BLOCKING · :5442 "Each 2026-10-18 note's own `scripts/score.py` prints its table."** This is false for reviewer-brief-556. Its `score.py` stops with `FileNotFoundError: …/adjudication-map-private.json` and exits 1, because that note was never adjudicated. The submitted proposals named only three notes (proposals file :30-31).
  - Fix: "The 6551, queue96 and union-556 notes' own `scripts/score.py` print their tables."
- NON-BLOCKING · :5559-5560. The quoted wording "the phase gates demand verbatim sentences…" first appears in `6f15b1c` (2026-09-22) and moved to `evidence.md` in `96fad8d`. In `5bc2d71` (2026-08-17) it read "the two phase gates…" and was the Cycle gate's reason. The date of the claim holds, but `git log -S` on the quote does not return `5bc2d71`.
- NON-BLOCKING · :5569 "every number word". The script counts one through ten only (`count_widening.py`:19-20).
- NON-BLOCKING · :5582 "Across public records, 77 of 141". `transcript_presence.py`:26 reads only `transcript:` lines. Three more paths are named in record bodies:
  - `…-409.md` names `d588c8a7…`, which is gone.
  - `…-505.md` names `2463cfcc…` and `…-514.md` names `7b912efd…`, both on disk.
  - Counting every mention gives 78 of 144. Suggested wording: "77 of the 141 paths on records' `transcript:` lines".
- For your information, not an objection: the bare :72-75, :77 and :85 in the APPLIED entry point at harden's lines, not this file's. That follows earlier practice (:5074-5076 uses a bare :325 for pr-harden).
- Everything else checked out against its source:
  - the citations :2148-2150, :2143, :2307-2327, :3998-4000, :5346 and :5378;
  - the four notes, the A/B note :46-47 and #555 :17, :19, :31;
  - `pr-review`:152/:158, `pr-harden`:42-44, `reviewer.md`:26-33 and `skill-lint.py`:16-18/:27/:36;
  - all six rollback items, the six L04 dispositions, and U's 18/13 split;
  - the pre-read comparison: 415 shared files with only `REJECTED.md` changed since, 43 governance files with only harden changed, the five notes local-only and unnamed;
  - the schema text, which is in the 2.1.309 binary, and the absence of `cleanupPeriodDays` from settings.

## 3. Proposals file: CLEAN
- The gate's reply matches its final message in `subagents/agent-a9eeeadbef2d5c8a6.jsonl`, ignoring blank lines. The submitted section matches `scratchpad/proposals-draft.md`.
- All six blocking objections were handled as the "what changed" section says. The PR440/PR465 correction is backed by a second, independent detector: `public-traversals.txt`:1-2 reports "no harden load" for both, and both detectors flag the same 6 non-loaders on disk.
- NON-BLOCKING · :297 "came in with `5bc2d71`". Same issue as the :5559-5560 item in section 2.
- NON-BLOCKING · :4-5 "the instruments they name are kept". `gate_sentences2.py` was replaced by a later version rather than kept, which README:31-32 does say.

## 4. README, scripts and outputs: FIX
- **BLOCKING · README:10 "each run's exit status is noted".** Only `controls.out` records exit statuses; the other five outputs have none. Fix: delete the clause. I re-ran all five and each exits 0.
- NON-BLOCKING:
  - README:56 "any number word": it is one through ten.
  - README:83 "across all public records": same scope issue as :5582.
  - README:31 "an earlier version of the same patterns": two earlier versions produced those lines, and the first (`scratchpad/scripts/gate_sentences.py`) had only the three Transition patterns.
  - `gate_sentences.py`:32-33 says a harden from before `6f15b1c` had no Phase 2 gate sentence. That is false: `6f15b1c^` had the Phase 2 "Stopping gate" sentence (:181-183) and the Cycle gate (:219). No figure changes, since all 58 transcripts carry the Termination template.

## 5. Commit message: FIX
- **BLOCKING · :25 "demonstrated privilege bypasses filed as questions (6551 L04, 6 of 6)".** Only wt-02 ran the exploit. The other five findings' evidence fields (F003, F008, F020, F029, F032 in `adj/adjudication-input.json`) say the probe was not run. The ledger itself gets this right at :5488-5489.
  - Fix: "privilege bypasses filed as questions (6551 L04, 6 of 6; one after wt-02 ran the exploit)".
- NON-BLOCKING:
  - :11 "two lines below": :85 is eight lines below :77, and two only if counted from :83.
  - :12-13 "changed only the closing line": `c18bf29` also changed the frontmatter and the test-coverage section. The ledger's "changed :85, but not :77" is accurate.
  - :39-40 "77 of 141 named paths": same scope issue as :5582.

Step 6's `PRS` check last ran at 09:56:05Z on the current message (main transcript line 1021). It has to run again after any of these fixes, as Step 6 requires.

---

# Gate 2 — what the proposer checked, and what changed

The gate's citations were re-read before acting on them.

**Confirmed.**

- `git log -S` on the exact quote "the phase gates demand verbatim sentences and get them" returns
  `6f15b1c` (2026-09-22) and `96fad8d`. The "two phase gates" wording returns `5bc2d71` (2026-08-17)
  and `6f15b1c`.
- `6f15b1c^` carries a "Stopping gate" sentence at :181 and a "Cycle gate" at :219.
- Of the six L04 findings in `adj/adjudication-input.json`, only F030 (wt-02) cites a probe. The other
  five cite reading the code.

**Changes, each named by the gate.**

- **Ledger, blocking.** "`confidential/` was not read" became "Nothing was drawn from `confidential/`.
  Only its file names and `PRS` were read, as Step 1 allows."
- **Ledger, blocking.** "Each 2026-10-18 note's own `scripts/score.py` prints its table" became "The
  6551, queue96 and union-556 notes' …".
- **Ledger, non-blocking.**
  - The `5bc2d71` attribution was deleted. Here and in Gate 1's section above, the quoted line is
    cited only by its place in `evidence.md`.
  - "every number word" became "the number words one to ten".
  - "77 of 141 distinct transcript paths" became "77 of the 141 distinct paths on public records'
    `transcript:` lines".
  - "the gate's verbatim reply" became "both gates' verbatim replies".
- **README, blocking.** "and each run's exit status is noted" was deleted.
- **README, non-blocking.** "an earlier version" became "two earlier versions", the first with only
  the Transition patterns. "any number word" became "one to ten". The 77-of-141 count is scoped to
  `transcript:` lines.
- **`gate_sentences.py`.** The comment claiming that a pre-`6f15b1c` harden had no Phase 2 gate
  sentence was replaced by what the template marks. The change is to a comment only; a re-run
  reproduced both outputs byte for byte.
- **Commit message, blocking.** "demonstrated privilege bypasses filed as questions" became "privilege
  bypasses filed as questions (6551 L04, 6 of 6; one after wt-02 ran the exploit)".
- **Commit message, non-blocking.** "two lines below" and "changed only the closing line" were
  reworded, and the 77-of-141 count is scoped.
- **This file's header** now says the instruments are kept "some in later versions".

**Not changed:** the bare :72-75, :77 and :85 in the APPLIED entry. The gate notes that they follow the
ledger's practice (:5074-5076).
