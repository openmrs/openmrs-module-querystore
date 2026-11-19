# measurement · pr-review with and without an unanchored second reviewer · openmrs-core PR 6535 at 86b96c7d6f · 2026-11-19
outcome: the second reviewer made the review notice more, not file more.
- None of the four points the maintainer raised was filed, by U 0 of 3 or C 0 of 3. The pre-committed reading
  keeps the lesson parked.
- K-order (reads try the current location first while `getPath()`, which purge uses, tries legacy first) was
  noticed by U 3 of 3 and C 0 of 3, and set aside every time. Two U runs heard it from their second reviewer, one
  with a probe. wt-01's main run wrote "Not filed: it needs a file at both <appdata>/K and
  <appdata>/storage/encode(K), which the obs key formats don't produce".

protocol: pre-registered before any arm ran. Owner-directed: on 2026-11-19 the owner approved "a replay on the
frozen #6535 head, with and without a second reviewer" before the second-reviewer lesson goes into the skill.
artifacts: `.claude/skill-lessons/artifacts/2026-11-19-6535-second-reviewer/`. It holds the protocol with results,
the arm key, briefs, outputs, scripts, blind pools and maps, the judge's verdicts, the audit, costs, the launcher and
the deviations.
transcript: session 44d9b58a-4830-47af-b032-8ad1f2e620d4 (project folder `-Users-danielkayiwa-Projects-openmrs-chartsearchai`)

This is capture, not derivation. It follows REJECTED.md's 2026-11-19 section, which recorded the field evidence.
That evidence is a second reviewer credited by its session with a finding the review missed in 12 of 71 sessions,
run in 54 of 59 openmrs-core-folder sessions from 2026-11-05 against 8 of 71 elsewhere. The closest parked shape
is the 2026-10-23 "union of two independent single-context reviewers" (556 arm U), to reopen on a pre-registered
measurement where a union clears its bar.

## Design

- **Subject:** the 2026-11-18 replay's P2 subject, unchanged. PR 6535 at 86b96c7d6f, base `a93b6293c9`, frozen
  2026-11-18 17:00 UTC before ibacher's review, with the same snapshots and per-run clones from a bare copy of that
  replay's frozen repository. Each slot had a second clone for its subagent.
- **Arms**, n=3 each, one wave:
  - **C:** `pr-review` 0.27.0 as live.
  - **U:** C plus 92 words in Step 3, between *Convergence* and the refutation section (`skill-diff-C-to-U.diff`).
    Before settling its findings, the review starts one fresh agent that gets only the PR, its problem, the
    repository and how to build and test it, and none of the review's findings. It reproduces what that agent
    reports before filing any of it.
- **Runs:** headless `claude -p --model claude-opus-5-5` at default effort.
  - Agent and Task were allowed in both arms, so C is the live skill as it runs, refutation pass included.
  - WebFetch, WebSearch, Skill and Workflow were disallowed.
  - The network went through a dead-port proxy, with only the model API exempt. A canary showed that curl, gh and
    git could not reach GitHub or JIRA, that a subagent starts, and that no project memory loads.
- **Locks:** 41 answer-bearing paths at mode 000 for the wave. Not the openmrs-core and webservices project memories
  or REJECTED.md, which other live sessions were reading; reads of those were left to the audit.
- **Keys:** K-mime, K-stream, K-order and K-catch, from ibacher's review 5961392681, as defined in the 2026-11-18
  protocol. K-since was not scored, since 6 of 6 runs filed it on 2026-11-18.
- **Decisive:** per run, whether a `findings` entry states at least one key. Candidates come from a calibrated scan
  (4 keys, 2 known hits and 2 known misses each, all passed). Pools are judged blind before the map is opened.

## Results

- **Compliance:** U 3 of 3 started one unanchored reviewer, at main tool calls 12, 19 and 15. No brief carried the
  run's own findings. No C run started a subagent.
- **Decisive:** 0 candidates over all 12 findings. The wider vocabulary check over every output field and each
  subagent's final report found K-order as above. K-mime, K-stream and K-catch appear in none; wt-05's reviewer
  reported "no resource leaks".
- **Two U findings came from the second reviewer.** The fresh blind judge (session e0be79e8, $0.54) rated them:
  - wt-03 F2 (no test for the new `APIException` paths): valid.
  - wt-01 F3 (a storage subclass overriding `getData` is bypassed): noise, because "no subclass exists outside the
    test's own CountingLocalStorageService".
  - All 12 findings: 11 valid, 0 invalid, 1 noise.
- **Cost and time:** U $11.21 (mean 11.3 min) against C $6.36 (mean 7.7 min), so the second reviewer added about
  76% to cost and 47% to wall time per review.
- **Audit:** calibrated on 9 planted forbidden calls and 8 allowed ones (9 of 9 flagged, 0 of 8). Every flag is a
  hard constraint the review passed to its subagent in the Agent call's prompt, read in context. The subagent
  transcripts had 0 flags.
- **Deviations:** wt-05 wrote its output to a mistyped path (recovered unchanged), and copied a war out of a local
  docker image without starting it to check the 2.8.x Java level.

## Reading

- Against the pre-committed reading, U is 0-1, so the second reviewer does not carry the catch on this subject.
- What it changed is noticing. K-order surfaced in every U run and in no C run, and the same filter then dropped it
  in each. The filter was a realism judgment, the one that set aside 6535's read-order mismatch twice in the live
  rounds (REJECTED.md, the 2026-11-18 section).
- So here the bottleneck is the filing judgment, not detection. That is the parked "seen, then set aside" lesson,
  whose measurement sightings this replay takes from 8 to 11. Its run-record count stays at 2.

## Threats

- n=3 per arm, one PR, one wording, default effort.
- The keys were written by the session that missed them, and ibacher's review is one maintainer's call.
- The field's second reviewers loaded openmrs-core's project memory; these loaded none.
