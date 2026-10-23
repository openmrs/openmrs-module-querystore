# measurement · pr-review lens replay · openmrs-module-queue PR 96 at round-1 head · 2026-10-18
outcome: measurement only — no proposal drafted. Two candidate skill texts and one withheld-context condition tested against the control on the head where a posted review round missed two blockers; none moved the blocker that still escapes
protocol: pre-registered before any arm ran; two amendments, each recorded before the results it bears on (the second adds an arm motivated by waves 1-2, and says so)
artifacts: `.claude/skill-lessons/artifacts/2026-10-18-lens-replay-queue96/` — protocol with results, both private keys, all briefs, the frozen PR/ticket snapshots, every run's findings, the blind adjudication, and the scripts. `python3 scripts/score.py <that dir>` re-derives the table below. Run logs were left out (6.8 MB); `transcripts.txt` there maps each run to its persisted session transcript
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-queue/5743c726-b776-450f-a755-a07953eaf17e.jsonl

This is capture, not derivation. **Origin: a question** — are `pr-review`'s lenses enough, or should
more be added? Nothing was posted; the PR was not touched.

## Why this subject

On 2026-09-16 five review rounds were posted on queue PR 96 under the user's account, each on a new
head after a fix commit. Two blockers present at round 1's head `ae7f8fa2` were first raised later:
the void path left resting on handler order (round 3) and the purge running from a before-advice
outside the visit's transaction (round 4; the memory `addadvice-lands-outside-the-transaction`). A
single-context review is known to have missed both there.

## Design

- Head `ae7f8fa2` (a merge of main into the branch), base `bdfaa728`, +170/-0 over 3 files. PR
  description, PR conversation and tickets O3-5459/O3-5461 frozen at 2026-09-16T11:16:58Z, the moment
  round 1 posted. The conversation then held one approving review, which answers a commenter's
  partial-failure question wrongly ("inside the TransactionInterceptor, so ... commit or roll back
  together"). The ticket thread spells out the handler-order mechanism.
- Arms, n=3, one wave per three runs with arms in random slot order, so contention is symmetric:
  **C** pr-review 0.19.0 unmodified; **T** C plus a *Trace outward* thread on what the framework does
  on the path (transaction, flush, handler order, required privilege) and a per-condition mutation
  sentence in *Test coverage*; **B** C with the PR conversation withheld; **V** (Amendment 2) C plus
  "a concern is cleared by the same bar a finding has to meet ... 'The test harness can't show it' is
  itself a claim to check".
- **Agent-tool subagents could not be used.** A canary subagent quoted the project's memory index,
  which names both answers; a headless `claude -p` started in a scratch clone answered NONE. All runs
  were headless, each in its own clone holding history only to `ae7f8fa2`, with a private Maven repo
  backed read-only by `~/.m2`. Tool calls of all 12 transcripts audited for reads of the live PR,
  JIRA, the user's checkout, project memory, other runs and the control files: 0 flagged (the audit
  flagged all five planted forbidden calls in a calibration transcript).
- Blind adjudication by one fresh agent: 39 findings pooled, run and arm stripped, shuffled, matched
  against a nine-defect answer key fixed in the protocol; anything unmatched verified by running it.
  Its own tool calls audited the same way: 0 flagged.

## Measured

| arm | K6 handler order | K7 transaction | K1 privilege | K4 flush | K8 config.xml | other REAL | NOISE |
|---|---|---|---|---|---|---|---|
| C | 3/3, all blocking | 0/3 | 2/3 (one filed as suggestion) | 1/3 | 0/3 | 0 | 1 |
| T | 3/3, all blocking | 0/3 | 3/3, all blocking | 0/3 | 0/3 | 2 (one defect, twice) | 0 |
| B | 3/3, all blocking | 0/3 | 2/3 | 1/3 | 0/3 | 0 | 0 |
| V | 3/3, all blocking | 0/3 | 1/3 | 0/3 | 0/3 | 0 | 0 |

- **Round 1's handler-order miss does not reproduce:** 12 of 12. Its transaction miss does: 0 of 12.
- No pre-registered signal for T, B or V on any outcome.
- T's unique REAL defect is one neither its text nor the answer key targeted: no test pins the
  advice's `purgeVisit` method-name guard; removing it leaves api 59/59 and integration 119/119 green,
  and `voidVisit` then purges the entries. 2 of 3 T runs, 0 of 9 others. Exploratory, since the
  pre-registered mutation outcomes were K4 and K8.
- The one NOISE: a legacy `visit_queue_entries` table that changeset `drop_visit_queue_entries_20231025`
  already drops.

**How K7 escapes (exploratory, from transcripts; both detectors calibrated on planted cases).** 11 of
12 runs asked whether the purge sits inside the transaction and all 11 concluded it rolls back: ten
from core's `TransactionProxyFactoryBean` XML, one (V) from a printed interceptor chain, which shows
interceptor order, not an active transaction. **0 of 12 ran anything able to observe a transaction**
(`NOT_SUPPORTED`, `isActualTransactionActive`, `TestTransaction`); several said the module harness
cannot show a rollback, which round 4 disproved with a `NOT_SUPPORTED` test. 6 of 12 wrote the
unobserved rollback into `could_not_verify` (C 1, T 2, B 0, V 3) and raised nothing; V's wt-12:
"cleared by reading the proxy chain only, so not raised", against the sentence its skill copy had just
added. Counterfactual on the same outputs: posting `could_not_verify` as questions would have put K7
before the author in 6 of 12 runs.

**Cost:** 12 runs $21.93, mean $1.83, 6.1-46.5 min, mean 2.62M input tokens counting cache reads and
25.8k output. Adjudicator 247,732 tokens, 25.1 min. The machine carried other workloads throughout.

## Refuted — claims this run's own orchestrator made and the run broke

- **"Name the missing check: transaction and framework semantics."** Recommended before the run; on
  its own motivating subject T asked the question 3/3 and answered it wrongly 3/3. A ceiling test,
  since the text was written by someone who knew the answer, and it still scored 0.
- **"The approving review's wrong answer, and the rule that a settled thread stays closed, kept rounds
  1-3 from rechecking."** B never saw the answer and cleared K7 the same way, 0/3.
- **"The handler-order miss is a depth failure inside a named lens."** It does not reproduce, so its
  cause on 2026-09-16 is unknown; the transcripts from that day are not on this machine.
- **"~2M tokens for ~12 runs."** Taken from the 6551 A/B's subagent token counts, which exclude cache
  reads; not comparable to headless transcripts.
- The protocol, arm key and raw later PR history first sat beside the snapshot files the briefs name,
  for ~35 s of wave 1 (Amendment 1); moved, and no transcript touched them.

## Limits

- One PR, n=3 per arm. T's and V's texts were written after the answers were known. V was added after
  waves 1-2 had shown the mechanism it targets.
- The K7 mechanism tallies come from transcript detectors, not from the adjudicator.
- Default effort, headless, no project memory. The original round, if run from the queue project, had
  the handler-order memory line (written 2026-09-09) in context.
- Bearing on `REJECTED.md` P-A: no fan-out arm ran here. This head is a subject its reopen condition
  can use: a single context misses a blocker 12 of 12 times.
