# Does a disposition sentence make a review file a defect it judged unlikely? A filing probe on PR 6535 · 2026-11-23

Owner-directed: the owner answered "yes" to "The real gap is the review setting aside what its second reviewer
finds; that's the parked 'seen, then set aside' item, at 12 sightings. Want me to look at it next?". The parked item's
reopen route (REJECTED.md, 2026-11-18 section) is "a wording or mechanism that a replay shows changes the posted
block". Pre-registered before any run.

## Subject

- openmrs-core PR 6535 at `86b96c7d6f`, base `a93b6293c9`, frozen at 2026-11-18 17:00 UTC (snapshots from
  `artifacts/2026-11-18-6535-replay-p2-p3/snapshots/p2/`, latest entry 2026-11-17T20:46:57Z). A frozen repository
  holding only those two branches' history (359 commits; 21 files, +967/-68).
- The decision point both 2026-11-19 U runs reached: a review that has its own findings and its second reviewer's
  report, deciding what to file. wt-01 and wt-03 set K-order aside there ("which the obs key formats don't produce";
  "which no current write path produces, so I judged it not worth filing"). The maintainer raised it (r4617856131).

## Items handed to every run (the second reviewer's report, verdicts removed)

- R1 = K-order: a key in both layouts; reads return the current copy while purgeData deletes the legacy copy.
  Ground truth: file (the maintainer raised it).
- R2 = the read-error APIException paths with no test. Ground truth: file (judged valid on 2026-11-19).
- R3 = a storage subclass overriding getData is bypassed. Ground truth: noise (judged noise on 2026-11-19: no
  subclass exists outside a test).
- R4 = two-argument injectMissingMetadata overrides no longer called. Ground truth: do not re-file (raised in thread
  r4512249216 and closed with the deprecation the reviewer asked for, which the snapshot holds).

## Arms (n=5 each, one wave, names assigned at random in KEY-private.json)

- C: pr-review 0.38.0 as installed (SKILL.md, posting.md).
- W: C plus one bullet after Step 3's "When the one experiment that could falsify a finding can't run" bullet:
  "A defect you can trigger but judge unlikely in practice is still a finding. File it as a question that names the
  condition it needs, so the author can say whether that condition arises, and set it aside only once you have shown
  the condition cannot occur, as an absence claim is shown." (54 words)
- Each run: `~/.claude/bin/blind-run` (own repository copy, no memory index), `--repo` the frozen clone, `--ref
  pr-6535`, deadline 1500 s, default effort, network through a dead proxy except Anthropic hosts, invalid GitHub
  tokens, Agent disallowed (blind-run's list). Answer-bearing paths locked at mode 000 for the wave (lock-list.txt).

## Scoring (decided before any output exists)

- Each run ends with one JSON object listing every item as filed (with a disposition) or set aside.
- Decisive: R1 filed, at any disposition. A missing or unparsable JSON counts as not filed and is reported.
- Noise: a run counts as noisy if it files R3 or files R4.
- Also recorded: R2 filed; dispositions; what each set-aside reason says.

## Pre-committed reading

- Signal: W files R1 in at least 3 more runs than C, and W's noisy runs are at most C's plus 1. Then the sentence
  changes the posted block at this decision point.
- W files R1 at least 3 more runs than C, but noise rises by 2 or more: it files more and noisier; observed, no claim.
- Otherwise: no evidence the sentence changes filing here.

## Threats

- The decision point is handed over, not reached: the items arrive as a list, which may make filing likelier in both
  arms than in a full review.
- One PR; R1's ground truth is one maintainer's call.
- n=5 per arm.
- The operator wrote the candidate and the items' neutral wording.
- Budget: stop and report if the cost passes $40.

---

# RESULTS, round 1 (scored 00:26Z before the key was opened at the unblinding time in unblinded.txt)

- C: R1 filed 2 of 5, R2 5 of 5, noisy 0 of 5. W: R1 filed 5 of 5, R2 5 of 5, noisy 4 of 5 (each filed R3 as a
  question). Pre-committed reading: W files R1 3 more, but noise rose by 4: files more and noisier; observed, no claim.
- Audit: 41 flags, all reads of the allowed snapshots or own skill folder written without a trailing slash; none
  outside them, no network. Locks restored (11 of 11).

# ROUND 2, registered before any round-2 run

- Written after seeing round 1 on this subject, so a pass is development, not validation.
- W2 replaces W's bullet, same place:
  "A defect you can trigger with data a real installation can hold, such as files or rows left by an upgrade, a
  migration or another module, is still a finding when you judge that data unlikely: you cannot rule it out from the
  code, so file it as a question that names the condition, for the author to answer."
- Arms C (0.38.0, fresh runs) and W2, n=5 each, one wave, names sb-01..sb-10 at random; same items, brief, isolation,
  scoring and pre-committed reading as round 1. Budget: stop and report past $25 for the round.

# RESULTS, round 2 (scored before the round-2 key was opened; times in unblinded.txt)

- C: R1 filed 1 of 5, R2 5 of 5, noisy 0 of 5. W2: R1 filed 4 of 5, R2 5 of 5, noisy 1 of 5 (sb-03, which also left R1
  aside). Pre-committed reading: W2 files R1 3 more and noise rose by 1: the signal reading, on development evidence.
- Both rounds' C: R1 3 of 10, noisy 0 of 10.
- Audit: no flag outside the allowed snapshots or own skill folder, no network. Locks applied and restored (11 of 11).
