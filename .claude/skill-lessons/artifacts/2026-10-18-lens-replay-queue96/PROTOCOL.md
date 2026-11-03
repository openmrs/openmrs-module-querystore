# Replay: does `pr-review` find, in one pass, the blockers its first round missed on queue PR 96?

Pre-registered 2026-10-18, before any arm ran.

## Why this subject

On 2026-09-16 five review rounds were posted on `openmrs/openmrs-module-queue#96` under the user's
account, each on a new head after the author's fix commit. Two blockers present at round 1's head were
first raised later:

- the void path left relying on handler order (raised round 3, on 95ed7457);
- the purge running from a before-advice outside the visit's transaction (raised round 4, on 77ff35b0).

Both are present at round 1's head `ae7f8fa2` (verified by reading the code at that sha). The
handler-order mechanism is spelled out in the ticket's own thread (Dennis Kigen, O3-5459, 2026-07-03),
and the approving review on the PR answers the transaction question wrongly ("inside the
`TransactionInterceptor`, so the queue-entry writes and the visit void commit or roll back together").

Round 1 is a single-context review that is known to have missed two blockers. That makes this head
the subject `REJECTED.md` P-A's reopen condition asks for, and the subject for testing whether named
checks move such blockers into round 1.

## Questions

1. **C** Does the current skill (pr-review 0.19.0), run once, raise K6 and K7?
2. **T − C** Do two added sentences (a framework-semantics thread in *Trace outward*; per-condition
   mutation in *Test coverage*) raise them more often?
3. **B − C** Does the wrong transaction answer in the conversation suppress K7? Arm B is the current
   skill with the PR conversation withheld.

## Subject

- repo `openmrs/openmrs-module-queue`, PR 96, head `ae7f8fa20945014019da201547383286aab99c94` (a merge
  of main into the branch), base `bdfaa72846d87ac123c89791c6cce1c04a888065`. Diff: 3 files, +170/-0.
- Snapshots at 2026-09-16T11:16:58Z, the moment round 1 posted: PR description
  (`pr96-body-at-round1.md`, the 2026-03-03 edit), PR conversation (`pr96-conversation-at-round1.json`:
  one approving review, one comment, no inline threads), tickets O3-5459 and O3-5461 with comments
  before the cutoff.

## Arms (n=3 each, 9 runs)

- **C, control.** Unmodified copy of `pr-review` 0.19.0, conversation snapshot provided.
- **T, treatment.** Same copy plus the two sentences below, conversation snapshot provided.
- **B, blinded.** Unmodified copy, conversation withheld (told the PR has no prior review). Same
  description and tickets.

Each run is a headless `claude -p` started from its own clone directory, runs Steps 1-3 and writes a
findings JSON. Nothing is posted. Three waves of three, each wave one run per arm in random slot
order, so contention is symmetric. Default model (`opus`) and default effort for every run.

Treatment text, inserted verbatim:

> after the *Optional dependencies and lifecycle* bullet:
> - **What the framework does on this path.** For each framework or core call the change relies on
>   for a side effect (a save, void, purge or delete, an advice or handler, a scheduled task, a REST
>   conversion), read its implementation at the versions the module supports and state what
>   transaction it runs in and what stays committed if it fails partway, when the session flushes,
>   which handlers and advice fire and in what order, and what privilege the called method demands of
>   this caller. These are the assumptions a diff carries least visibly, and the ones its tests
>   exercise least.
>
> appended to the *Test coverage* paragraph:
> Failing on the base branch reverts the whole change at once, so a test can clear that bar while one
> new condition, guard or registration is pinned by nothing. For each one the PR adds, delete or
> invert it alone and name the test that goes red; one the suite survives is a finding.

## Isolation, and how it is checked

- A subagent of the orchestrating session sees that project's memory index, which names both answers
  ("Handler order is HashMap order", "Context.addAdvice lands outside the transaction"); measured
  before this protocol. So no Agent-tool subagents. Headless runs from clone directories get an empty
  project memory; a canary run confirms that before wave 1.
- Each clone holds history only up to `ae7f8fa2` (the fix commits 3c43c4f3 and 57e8eec6 are absent;
  checked against the source repo, which has them).
- Each clone's `.mvn/maven.config` points Maven at a private local repository with `~/.m2` as a
  read-only tail. Calibrated: offline JDK 8 `clean verify` passes in 155 s, and the integration tests
  run against `api/target/queue-api-3.1.0-SNAPSHOT.jar`.
- Forbidden reads: the live PR 96 and O3-5459/O3-5461, `~/Projects/openmrs/queue`,
  `~/.claude/projects`, other runs' directories. Audited after the runs by grepping every transcript.

## Answer key (present at ae7f8fa2; origin = the round that first raised it)

| key | defect | origin | severity |
|---|---|---|---|
| K1 | advice proxies GET and MANAGE_QUEUE_ENTRIES but `purgeQueueEntry` requires PURGE_QUEUE_ENTRIES, so a caller with Purge Visits but not Purge Queue Entries cannot purge; MANAGE does nothing here | r1 | blocking |
| K2 | PR description says the advice intercepts `voidVisit` and gives a contradictory root cause | r1 | non-blocking |
| K3 | unused `QueueEntry` import in the test | r1 | nit |
| K4 | purge tests never flush, so they do not reach the FK failure the PR fixes | r2 | non-blocking |
| K5 | comment "the visit ... may already be voided, which would hide the entries" is false: the search does not filter on the visit's voided flag | r2 | non-blocking |
| K6 | the void path (the ticket's reported symptom) now rests on `VisitWithQueueEntriesSaveHandler`, whose `getVoided()` guard holds only when core's `BaseVoidHandler` runs first; tie broken by HashMap order | r3 | **blocking** |
| K7 | entries are purged in a before-advice outside the visit's transaction, so when `purgeVisit` then refuses (`Visit.purge.inUse`, visit has encounters) the entries are gone and the visit survives | r4 | **blocking** |
| K8 | the `config.xml` `<advice>` block is read by no test; delete it and the build stays green, and a wrong class name fails quietly | r4 | non-blocking |
| K9 | Javadoc says the void stamp lets "an unvoid tell them apart", but no `UnvoidHandler<Visit>` exists | r5 (moved form) | non-blocking |

Match rules: K6 needs the ordering/guard mechanism or the claim that the reported void half is not
reliably fixed; a description-only mismatch is K2. K7 needs entries deleted while the visit purge
fails or rolls back; a generic "partial failure" worry without that is recorded as K7-partial.

## Outcomes

- Primary: per arm, runs raising K6 and K7 (any disposition), and runs calling them blocking.
- Secondary: K1 (control positive, since round 1 found it), K8 and K4 (the mutation sentence's
  targets), K2, K3, K5, K9; other findings classified REAL / REAL_OVERSTATED / PLAUSIBLE_UNVERIFIED /
  NOISE; tokens, turns, wall clock per run.
- Adjudication by one fresh agent: findings from all nine runs pooled, labels stripped, order
  shuffled, given the answer key, told to match and to verify anything new by running it.

## Pre-committed reading (a "signal" is a difference of at least 2 of 3 runs)

- C raises K6 and K7 in at least 2 of 3 runs: round 1's miss does not reproduce on the current skill
  without project memory, and this subject supports no lens change.
- T − C signal on K6 or K7: the framework sentence is supported on this subject only. Not proposable
  until a held-out subject agrees, because it was written by someone who knew the answers.
- T − C signal on K8 or K4: the same for the mutation sentence.
- B − C signal on K7: the wrong answer in the thread suppresses re-verification. Candidate rule: a
  claim in the review conversation about runtime or framework behaviour is a lead to verify, whoever
  made it.
- Anything else: no signal at n=3, said as such.

## Deviations from the original round 1, recorded

- Skill version: 0.19.0 now; unknown on 2026-09-16 (likely 0.17.x). The lens list is identical
  (unchanged since 6e7c147).
- No project memory. The original round, if run from the queue project, had the handler-order index
  line in context (written 2026-09-09).
- Headless, default effort; conversation given as a file instead of fetched; nothing composed or posted.

## Threats

- n=3 per arm, one PR.
- The treatment names transactions and handler order, the mechanisms of K7 and K6. A treatment win
  here is a ceiling, not evidence it generalises.
- Matching a finding to a key entry is a judgement; the match rules above are fixed before any run.
- The machine is running other workloads (a chartsearchai sweep, a standalone with llama-server).
  Symmetric across arms within each wave, not across waves.

## Amendment 1 — 2026-10-18T10:33:38Z, wave 1 running, no result seen

This protocol, the arm key, the brief template, the raw PR history (including the later PR
descriptions) and the logs sat in the run directory, beside the snapshot files the briefs point at,
for the first ~35 s of wave 1. All were moved to a sibling directory the briefs never name. An audit
of the three wave-1 transcripts at that moment found no read of any of them (a string every brief
contains was found, as the positive control). The end-of-run audit repeats the check over every
tool call in all nine transcripts. Later waves' logs are written outside the run directory, and each
wave's outputs are moved out before the next wave starts.

## Amendment 2 — 2026-10-18, after waves 1-2 finished and wave 3 launched, before any arm-V run

**Motivated by what waves 1-2 showed, so this arm tests a hypothesis the same subject produced.**
Recorded as such; it can show that a rule changes behaviour on this PR, not that it generalises.

Observed (exploratory, from the transcripts, not from adjudication): no run raised K7. Five of six
asked where the purge sits relative to the transaction, and all five cleared it by reading core's
`TransactionProxyFactoryBean` configuration, two of them saying in so many words that they did not
run it because "module tests run inside one test transaction". The skill turns a concern cleared that
way into silence ("Checked it and it's fine → say nothing").

**Arm V, n=3 (wt-10..12), one wave after wave 3.** The control skill plus one sentence, inserted after
Step 3's "When the one experiment that could falsify a finding can't run" bullet, written to name no
mechanism of this PR:

> - A concern is cleared by the same bar a finding has to meet. If the experiment that would show the
>   defect did not run, the concern is not cleared, however convincing the reading of the code or
>   configuration that clears it: ask it as a question that names what you could not observe. "The
>   test harness can't show it" is itself a claim to check before you stop.

Briefs identical to arm C's (conversation provided) apart from the skill path and run directory.
V's findings are pooled into the same blind adjudication as the nine.

Added outcome **K7-q**: a question (not an assertion) that names the scenario of K7, entries deleted
while the visit purge is refused or rolled back. The scorer reports K7, K7-q and K7-partial
separately.

Pre-committed reading: V raises K7 or K7-q in at least 2 of 3 runs while C raises neither in more
than 1 of 3 → the clearance rule changes behaviour on this subject, and it is the candidate to test
on a held-out PR. Otherwise no signal.

---

# RESULTS — recorded 2026-10-18 from the blind adjudication, before interpretation

Adjudicator: one fresh agent, 39 pooled findings with run and arm stripped and order shuffled; its
77 tool calls audited, none touched the run directories, the key or the live PR. 247,732 tokens,
70 tool uses, 25.1 min. All 12 run transcripts audited: 0 forbidden reads.

| arm | K1 | K2 | K4 | K6 | K6 blk | K7 | K7-q | K7-partial | K8 | K9 | other REAL | NOISE |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| C | 2/3 | 3/3 | 1/3 | 3/3 | 3/3 | 0/3 | 0/3 | 0/3 | 0/3 | 0/3 | 0 | 1 |
| T | 3/3 | 3/3 | 0/3 | 3/3 | 3/3 | 0/3 | 0/3 | 0/3 | 0/3 | 0/3 | 2 (one defect, twice) | 0 |
| B | 2/3 | 3/3 | 1/3 | 3/3 | 3/3 | 0/3 | 0/3 | 0/3 | 0/3 | 1/3 | 0 | 0 |
| V | 1/3 | 3/3 | 0/3 | 3/3 | 3/3 | 0/3 | 0/3 | 0/3 | 0/3 | 0/3 | 0 | 0 |

K3 and K5: 0/12. Arm C filed K1 as a suggestion once (adjudicated blocking). The other REAL defect,
raised only by T (wt-02, wt-05): no test pins the advice's `purgeVisit` method-name guard; removing it
leaves api 59/59 and integration 119/119 green, and without it `voidVisit` purges the entries. The one
NOISE: a legacy `visit_queue_entries` table that changeset `drop_visit_queue_entries_20231025` drops.

## Against the pre-committed readings

- C raised K6 3/3 and K7 0/3: round 1's K6 miss does not reproduce on the current skill; its K7 miss does.
- T − C on K6: 3 vs 3; on K7: 0 vs 0. No signal. On K8: 0 vs 0; on K4: 0 vs 1. No signal.
- B − C on K7: 0 vs 0. No signal.
- V on K7 or K7-q: 0/3. No signal.

## Exploratory, not pre-registered (from transcripts; detectors calibrated on planted cases)

- 11 of 12 runs asked where the purge sits relative to the transaction (all but wt-03). All 11
  concluded it rolls back: ten from reading core's `TransactionProxyFactoryBean` configuration, one
  (wt-10) from a printed interceptor chain, which shows interceptor order, not an active transaction.
- 0 of 12 ran anything able to observe a transaction (`NOT_SUPPORTED`, `isActualTransactionActive`,
  `TestTransaction`). Several stated the module test harness cannot show a rollback; round 4 showed it
  with a `NOT_SUPPORTED` test.
- 6 of 12 runs wrote the unobserved rollback into `could_not_verify` (C 1, T 2, B 0, V 3) and raised
  nothing. Counterfactual from the same outputs: had `could_not_verify` been posted as questions, K7
  would have reached the author as a question in 6 of 12 runs.
- T's unique REAL defect is not one the treatment text targeted (K8 and K4 were); the answer key did
  not contain it.

Cost: 12 runs, $21.93 total, $1.83 mean; 6.1-46.5 min; mean 2.62M input tokens including cache
reads, 25.8k output. The "~2M tokens for ~12 runs" estimate given beforehand used the 6551 A/B's
subagent token counts, which exclude cache reads, and is not comparable.
