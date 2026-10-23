# measurement · pr-review union + risk routing + disposition rule · openmrs-module-chartsearchai PR 556 at round-1 head · 2026-10-18
outcome: measurement only — the bundle surfaced the blocker two fresh rounds missed in 1 of 3 runs against 0 of 3, below the pre-registered bar; no proposal drafted
protocol: pre-registered before any arm ran; one amendment (audit rule narrowed, a harness limitation recorded) before adjudication
artifacts: `.claude/skill-lessons/artifacts/2026-10-18-union-routing-556/` — protocol with results, both private keys, all briefs, snapshots, every run's findings and risks, the blind adjudication, the scripts; `python3 scripts/score.py <that dir>` re-derives the table below; `transcripts.txt` maps each run to its persisted transcript
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-queue/5743c726-b776-450f-a755-a07953eaf17e.jsonl

This is capture, not derivation. Third of three measurements this session, after
`2026-10-18-pr-review-lens-replay-queue96.md` and `2026-10-18-pr-review-heldout-6551.md`.

## Design

- Subject: PR 556 (#555) at the pipeline's own round-1 head `d77204ba` (15 files +493/-69), base
  `e089267e`. Answer key from that run's per-round reviewer outputs: **S1** condition-mediated findings
  carry no partner rows, so the fix skips them (raised round 1, blocking); **S2** a `comparable` javadoc
  made false (round 2); **S3** `namesThePartner` credits any alias of the partner's rows anywhere in the
  answer, and the knowledge base's aliases include lab words (raised round 3, blocking; the method is
  byte-identical at the round-1 and round-3 heads, checked). Rounds 1 and 2 missed S3.
- Arms, three waves of one C run and one U pair each: **C** pr-review 0.19.0, one reviewer; **U** two
  independent reviewers with pr-review 0.19.0 plus a demonstrated-exposure disposition rule, scored as
  the union of both reviewers' findings plus both `could_not_verify` lists surfaced as risks.
- The adjudicator saw every finding and every risk item from all nine runs, marked by kind only, so the
  arms stayed indistinguishable; C is scored on findings, U on its pair's findings and risks.
- Headless runs from per-run clones holding only the round-1 head's ancestry; JDK 11 offline, private
  Maven repositories; reviewers told no server on the machine is theirs to touch. Audit: 0 forbidden
  accesses across 9 runs and the adjudicator.

## Measured

| unit | S1 | S2 | S3 surfaced | S3 as a finding |
|---|---|---|---|---|
| C, 3 runs | 0/3 | 0/3 | 0/3 | 0/3 |
| U, 3 pairs | 0/3 | 0/3 | 1/3 | 1/3 |

- **No pre-registered signal.** The one S3 (wt-07, blocking, reproduced with a stubbed answer about
  magnesium salicylate crediting a Salicylic acid order through the alias "salicylate") came from the
  union; routing alone surfaced S3 in 0 of 3.
- **Sensitivity, not the result:** two U risks say the PR's documented alias residue was not measured.
  The blind adjudicator rated them NOISE; read as naming S3, U would be 3 of 3.
- **Routing's yield:** 25 risk items, 19 NOISE (mostly "full build not run", "no live server"); of the 5
  REAL, 3 restate a defect their unit already raised as a finding. It added a new real defect in 2 of 6
  units, neither of them S1-S3.
- Findings precision: 12 findings, 0 NOISE. Disposition rule untested: nothing in this PR is an exposure.
- Cost: C $8.66, U $16.17 (two runs per unit), $24.83 in all; adjudicator 383,295 tokens, 46.8 min.

## What the transcripts and the PR show (exploratory)

**Both blockers sit inside limitations the PR declares.** The description at the review moment says
"the condition-mediated finding carry no rows, so their orders are stated only by their printed names"
(S1) and "an alias the data files under two substances names both" (S3's narrow form). Issue #555's
problem still occurs for condition-mediated findings, and S3's real extent is any alias anywhere in the
answer. All nine pr-review runs accepted the declarations; two listed the alias residue's frequency as
unmeasured. The pipeline's round-1 reviewer raised S1, and its brief (`pr-harden/reviewer.md`) says to
judge "against the ticket's own words and its comments, never against the PR description, which was
written by the same agent that wrote the code"; plain `pr-review` has no such instruction.

## Refuted — claims this run's own orchestrator made and the run broke

- **"Routing `could_not_verify` is the lever that reaches what single runs miss."** On this PR it reached
  neither blocker, and 19 of its 24 items were noise.
- Not refuted, but not tested either: **"a union of two runs fixes the misses that are luck."** S3 was
  found by 1 of 9 runs, closer to a systematic miss than to luck, so this subject could not test the
  claim; the union surfaced it once in three pairs.

## Limits

- One PR, n=3; the key's S1 and S2 come from the pipeline's own reviewers and were taken as given
  (their code claims were checked at the head).
- The replays ran plain pr-review Steps 1-3, not the pipeline's reviewer brief; the S1 contrast above
  is between those two, on one PR.
- `~/.m2` holds a later `openmrs-api 2.9.0-SNAPSHOT` than round 1 built against; a base worktree has no
  private-repository config (Amendment 1), which at least one run cited for skipping a base probe.
