# measurement · a disposition sentence for defects a review judges unlikely, at the filing decision · openmrs-core PR 6535 at 86b96c7d6f · 2026-11-23
outcome: round 1, W ("a defect you can trigger but judge unlikely in practice is still a finding ... file it as a question") filed K-order in 5 of 5 runs against C's 2 of 5, and the noise item in 4 of 5 against 0 of 5: files more and noisier, no claim. Round 2, W2 (the same, for a condition that lives in data a real installation can hold) filed K-order in 4 of 5 against C's 1 of 5, and noise in 1 of 5 against 0 of 5: the signal reading, on development evidence, since W2 was written after round 1.
protocol: pre-registered before each round, scored mechanically before each round's key was opened. Owner-directed: the owner answered "yes" to "The real gap is the review setting aside what its second reviewer finds; that's the parked 'seen, then set aside' item, at 12 sightings. Want me to look at it next?".
artifacts: `artifacts/2026-11-23-6535-setaside-probe/` (protocol with results, keys, briefs, the two skill diffs, scripts, scores, run reports; full transcripts listed in `transcripts.txt`, kept locally).

This is capture, not derivation. It follows the parked "seen, then set aside" entry (REJECTED.md, 2026-11-05) and the
2026-11-18 section's route, "a wording or mechanism that a replay shows changes the posted block".

## Design

- **Subject:** PR 6535 at `86b96c7d6f`, base `a93b6293c9`, frozen at 2026-11-18 17:00 UTC with the 2026-11-18 replay's
  snapshots, in a repository holding only those two branches' history.
- **The decision point** both 2026-11-19 U runs reached: a review with its own findings (F1, F2) and its second
  reviewer's report, with the runs' verdicts removed. R1 is K-order (file: the maintainer raised it in r4617856131), R2
  the read-error `APIException` paths with no test (file: judged valid on 2026-11-19), R3 a storage subclass's
  `getData` bypassed (noise: judged so on 2026-11-19, no subclass outside a test), R4 the two-argument
  `injectMissingMetadata` overrides (do not re-file: closed in thread r4512249216).
- **Arms, n=5, one wave each:** C is pr-review 0.38.0; W and W2 are C plus one bullet after Step 3's "ships as a
  question" bullet (`skill-diff-round1.diff`, `skill-diff-round2.diff`).
- **Runs:** `bin/blind-run` (own repository copy, no memory index), a dead proxy except Anthropic hosts, invalid GitHub
  tokens, Agent disallowed; 11 answer-bearing paths at mode 000 for each wave.
- **Scoring:** R1 filed at any disposition; a run is noisy if it files R3 or R4.

## Measured

- Round 1: C R1 2 of 5, noisy 0 of 5; W R1 5 of 5, noisy 4 of 5, each of those filing R3 as a question.
- Round 2: C R1 1 of 5, noisy 0 of 5; W2 R1 4 of 5, noisy 1 of 5 (the run that left R1 aside).
- Every run filed R2; no run filed R4.
- C's reasons for leaving R1 aside: "Nothing in core writes the same key to both the current and the legacy
  location" (twice) and "Master's purgeData also deleted only the legacy copy ... the orphan predates the PR". C left R3
  aside after a search: "No subclass of LocalStorageService or S3StorageService exists outside tests".
- W's runs turned both doubts into questions ("Can a key ever have a copy in both ...?", "Does any module subclass
  `LocalStorageService` ...?").
- Cost $17.67 for 20 runs of 1.5 to 2.5 minutes. Audit: no read outside the allowed files, no network.

## Reading

- Pre-committed: round 1, files more and noisier, no claim; round 2, the signal reading.
- W2 draws its line where R1 and R3 differ: R1's condition lives in files an installation can hold, which no search of
  the code settles; R3's lives in code, which a search did settle.

## Threats

- The decision point is handed over as a list; a full review may weigh these items differently.
- One PR, n=5 per arm, and W2 was written after round 1 on the same items: development, not validation.
- R1's ground truth is one maintainer's call; R3's "noise" is the 2026-11-19 blind judge's.
- The operator wrote the candidates and the items' neutral wording.
