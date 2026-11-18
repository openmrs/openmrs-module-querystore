# measurement · pr-review with and without the parked P2 and P3 sentences · openmrs-core PR 6535 at 86b96c7d6f (P2) and a2002caeac (P3) · 2026-11-18
outcome: neither sentence changed what a review files.
- P2: TextHandler.java:118's null `mimeType` was filed by E 0 of 3 and C 0 of 3.
- P3: no run named or narrowed the broad `catch (FileSystemException e)` in the review's own draft D2 (E 0 of 3, C 0 of 3). All six runs posted it unchanged.

protocol: pre-registered before any arm ran. The pass was owner-directed: the owner asked why P2 and P3 needed a retro when they were real issues that would help catch what the maintainer found. The candidates and every P2 finding were read blind, and the verdicts were recorded before the arm map was opened.
artifacts: `.claude/skill-lessons/artifacts/2026-11-18-6535-replay-p2-p3/`: protocol with results, arm key, briefs, outputs, scripts, snapshots, blind candidates, verdicts and noise judgments, skill diffs, costs, audit, lock list, `transcripts.txt`
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-openmrs-core/13a04911-9d12-40fc-baa9-b655af5ecba7.jsonl

This is capture, not derivation. It follows `2026-11-18-pr-review-core-6535-maintainer-changes-requested.md` and REJECTED.md's 2026-11-18 section, whose REOPEN ON for both proposals names this replay.

## Design

- **Subjects.**
  - P2: 86b96c7d6f, frozen 2026-11-18 17:00 UTC, before the maintainer's review.
  - P3: a2002caeac, frozen 2026-11-04 21:30 UTC. That round's six drafted comments were handed over as the run's own unposted draft; D2 is r4473604840.
  - In both, the PR body, the conversation up to the freeze, and TRUNK-6682 with its labels came from snapshots.
- **Arms**, n=3 each per replay, all twelve runs in one wave:
  - **C:** `pr-review` 0.26.0 as live.
  - **E:** C plus gate 1's reopen wording, with P2 in Step 3's same-defect bullet and P3 on Step 4's suggestion-block bullet (`skill-diff-C-to-E-p2.diff`, `-p3.diff`).
- **Runs:** headless `claude -p --model claude-opus-5-5` at default effort.
- **Isolation:**
  - Per-run clones come from frozen bare repos with full history up to the head. The local openmrs-core checkout is shallow, so it could not serve.
  - The network went through a dead-port proxy, with only the model API exempt. A canary showed that gh, curl and git could not reach GitHub or JIRA.
  - 53 answer-bearing paths were at mode 000 for the wave.
  - The audit was calibrated first: 8 of 8 planted forbidden reads flagged, 0 of 7 allowed.

## Measured

- **P2, K-mime: E 0 of 3, C 0 of 3.** No output mentions the null `mimeType`, in findings or out of them.
- **P3, K-catch: E 0 of 3, C 0 of 3.** Two runs saw the problem and set it aside:
  - wt-02 (C) noted Greptile's "fallback masks storage failures" thread, then judged the broadening safe ("the original failure is still rethrown when no legacy file exists").
  - wt-05 (E) counted that thread among those "all resolved".
- **Recorded, not decisive:**
  - All 6 P2 runs filed `@since 3.0.0` against the `backport` label, which was in their snapshot.
  - 3 of 6 P2 runs mentioned the read-order mismatch outside their findings (2 E, 1 C), and none filed it.
  - No output mentions the MediaHandler stream.
  - E's P2 runs filed the legacy-layout I/O count against the ticket's first criterion as blocking in 2 of 3, and C's in 0 of 3.
  - All 14 P2 findings were judged valid, blind: E filed 6 and C filed 8.
- **Cost:** $36.48 in all, 11.5-29.2 min per run.

## Limits

- n=3 per arm, one PR, and one wording per proposal.
- The runs were at default effort with no subagents. The rounds that missed ran at max effort with a second reviewer.
- The P3 runs start from a draft they did not investigate.
- The answer keys were written by the session that missed them, and the maintainer's review is one maintainer's call.
- 0 of 3 against 0 of 3 cannot tell "the sentence does nothing" from "these keys are rarely reached at default effort". Either way, it gives no evidence that the sentence helps.
