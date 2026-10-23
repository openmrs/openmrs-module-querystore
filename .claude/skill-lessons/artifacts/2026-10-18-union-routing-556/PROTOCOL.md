# Held-out test: two reviewers combined, their unverifiable checks surfaced, and a disposition rule, on chartsearchai PR 556

Pre-registered 2026-10-18, before any arm ran. Third measurement after
`2026-10-18-pr-review-lens-replay-queue96.md` and `2026-10-18-pr-review-heldout-6551.md`, which pointed at
three changes none of them tested:

1. **Union** of two independent single-context reviews. Re-scoring the first two measurements' 21 runs
   gave two-run recall of 91% for a blocker one run finds 67% of the time, and 58% for one found 33%.
   No effect on a blocker no run finds.
2. **Routing** each reviewer's `could_not_verify` list to the human as unverified risks. On queue PR
   96 it carried the transaction blocker in 6 of 12 runs, the only change that reached it at all.
3. **A disposition rule**: an authorization or data-exposure defect the reviewer has reproduced is
   blocking even when intent is unclear (6551's privilege hole was filed as a question in 6 of 6
   findings, once after a working exploit).

## Subject, and why it is held out

`openmrs/openmrs-module-chartsearchai` PR 556 (issue #555), the pipeline's own round-1 head
`d77204bae7091579a7131f486b613d03e365609d`, base `e089267e`, 15 files +493/-69. Neither earlier subject
is this repository, and none of the three changes names anything in it. Each clone holds only that
head's ancestry (1,096 commits); the later round commits `8e9bc158`, `0c8900e5`, `1e7c80f6` are absent.
PR description (as first posted, 02:51:31Z; the next edit is after round 1) and issue #555 (no edits,
no comments) frozen at 2026-09-29T02:55Z. PR conversation withheld, as on 6551.

## Answer key (the pipeline's own rounds at this PR, from its transcript)

| key | defect | first raised | severity |
|---|---|---|---|
| S1 | condition-mediated findings never get partner rows (`SafetyWarning.conditionMediated` is never given `withPartnerRows`), so the #555 credit skips them and they still print the partner by its parenthetical label | round 1 | blocking |
| S2 | the `comparable` javadoc's claim that `InteractionClaimPairFidelityCheck` and this check cannot read one name differently is false since the #555 credit applies to one of them | round 2 | non-blocking |
| S3 | `namesThePartner` credits a partner when ANY alias of its rows appears ANYWHERE in the answer; the knowledge base's aliases include lab/analyte words (`Lactic acid` carries `lactate`), so an answer that only mentions the lab marks the partner stated and silently drops the "not named above" note | round 3 (missed by rounds 1 and 2; `namesThePartner` is byte-identical at both heads, checked) | blocking |

Match rules: a finding matches only if it asserts the defect. S3 needs the over-crediting through
aliases (or through names that are ordinary words) of a partner that the answer did not name; a
finding about missed crediting (the other direction) does not match. An unverified-risk item matches
`S3-r` if it names that possibility without establishing it.

## Arms (9 reviewer runs, three waves of one C and one U pair each, random slot order)

- **C** pr-review 0.19.0 unmodified, one reviewer per run; its `findings` are the review. n=3.
- **U** two independent reviewers per run, each with pr-review 0.19.0 plus, after the *Silent-failure
  upgrade* bullet:
  > - Demonstrated-exposure rule: an authorization or data-exposure defect you have reproduced, by
  >   running the call as the affected caller and watching it happen, is blocking even when you cannot
  >   tell whether it was intended. Say what you ran, and put the intent question in the same comment;
  >   do not downgrade it to a question.

  The review is the union of both reviewers' `findings`, plus both `could_not_verify` lists surfaced as
  unverified risks. n=3 pairs.

Same brief for both arms apart from the skill path and run directory. Headless `claude -p` per clone;
JDK 11 offline with a private Maven repository per clone (calibrated: `CitedFindingPartnerCompletionTest`,
14 tests, 58 s). Tool-call audit as on 6551, also covering the live PR 556, issue #555 and the
`skill-lessons` record of this ticket (which states S3).

## Adjudication

One blind adjudicator gets every `findings` entry AND every `could_not_verify` item from all nine runs,
each entry marked `finding` or `unverified-risk`, run and arm stripped, shuffled; it matches each
against S1-S3 and classifies the rest. The adjudicator cannot tell the arms apart: both kinds come from
every run. Scoring then reads C as its findings only and U as the union of a pair's findings and risks.

## Outcomes and pre-committed reading (a signal is a difference of at least 2 of 3)

- **Primary: S3 surfaced** (U: a finding or a risk in the pair; C: a finding) — U vs C.
- Decomposition, from the same data: U findings-only (the union alone) vs C; and C findings plus its
  own risks (routing alone, one reviewer) vs C.
- Secondary: S1 (round 1 found it, so it is the control positive) and S2; NOISE among findings; risk
  items per run and the share matching a key or adjudicated REAL; cost (U is two runs per run).
- U − C signal on S3 → the bundle moved a blocker two fresh rounds missed, on a PR none of it was built
  from; the decomposition says which part. Otherwise no signal at n=3, said as such.
- The disposition rule can only be read if some run demonstrates an exposure; this subject is not
  expected to contain one. If none appears, the rule stays untested.

## Threats

- n=3, one PR, answer key from the pipeline's own rounds (S3 confirmed against the code; S1 and S2
  taken as given).
- `~/.m2` now holds a later `openmrs-api 2.9.0-SNAPSHOT` than the one round 1 built against.
- The repository's CLAUDE.md files load into every run, as they did for the original rounds.
- Contention with the user's other workloads; symmetric within a wave.

## Amendment 1 — 2026-10-18, wave 3 running, before adjudication

- **Audit rule narrowed.** The server/standalone rule matched the bare word `llama-server`, which flagged
  wt-02's output-writing call because its `could_not_verify` text says no llama-server was available.
  The rule now matches access (curl/wget/http to localhost, the standalone directory or jar,
  pkill/killall/kill -9/lsof -i); recalibrated on three planted accesses (all flagged) and two allowed
  calls including that exact shape (neither flagged). With it, waves 1-2 show no forbidden access.
- **Harness limitation, symmetric across arms.** `.mvn/maven.config` is untracked, so a git worktree a
  reviewer creates from its clone (e.g. to build the base) has no private-repository config, and the
  brief forbids passing a repository location. At least one run (wt-06) reported this as why it did not
  run a probe on the base. Reviewers can still check out the base in their own clone.

---

# RESULTS — recorded 2026-10-18 from the blind adjudication, before interpretation

All 9 transcripts audited (amended rule), 0 forbidden accesses; the adjudicator's 113 tool calls, 0
(its extra clone was made inside its own directory). Adjudicator: 383,295 tokens, 105 tool uses,
46.8 min, including a full `mvn -o clean install` at the head (passes; 2,676 api tests).

37 entries: 12 findings, 25 unverified risks. Matched: S3 once (E027, wt-07, blocking), S3-r once
(E026, same run). S1, S2: none. Unmatched findings 10 REAL, 1 REAL_OVERSTATED, 0 NOISE; unmatched risks
5 REAL, 19 NOISE.

| unit | S1 | S2 | S3 surfaced | S3 as a finding | S3 blocking |
|---|---|---|---|---|---|
| C (3 single runs, findings) | 0/3 | 0/3 | 0/3 | 0/3 | 0/3 |
| U (3 pairs, union of findings + both risk lists) | 0/3 | 0/3 | 1/3 | 1/3 | 1/3 |

Decomposition: the union alone 1/3 (pair P3); routing alone (C plus its own risks) 0/3.

## Against the pre-committed reading

U − C on S3: 1 vs 0. **No signal.** The disposition rule stays untested: no run demonstrated an exposure.

## Sensitivity the adjudicator flagged, not the result

Two U risks (E024 wt-03 in P1, E029 wt-04 in P2) say the PR's documented residue ("an alias the data
files under two substances names both") was not measured. Matched strictly they are NOISE; read as
S3-r they would make U 3/3 against C 0/3. The blind call stands; the reading is reported because the
adjudicator raised it.

## Exploratory, not pre-registered

- **What routing added**: of the 5 REAL risks, 3 restate a defect their own unit already raised as a
  finding; routing added a new real defect in 2 of 6 units (wt-05 C: the chip-collapse asymmetry it had
  "kept out of findings"; P1: the over-crediting test gap in `namesThePartner`), at 19 noise items of 24.
- **S1 0/9.** The PR description at the review moment declares it: "the condition-mediated finding carry
  no rows, so their orders are stated only by their printed names". The ticket's problem still occurs
  for those findings. The pipeline's round-1 reviewer, whose brief says to judge against the ticket's
  own words "never against the PR description", raised it; all nine pr-review runs accepted the
  declaration. S3's narrow form is declared the same way ("an alias the data files under two
  substances names both"); its real extent (any alias, lab words included, anywhere in the answer) was
  measured only at round 3.
- Findings precision: 11 unmatched findings, 0 NOISE.

Cost: C $8.66 (3 runs), U $16.17 (6 runs), $24.83 in all; 8.9-16.6 min per run.
