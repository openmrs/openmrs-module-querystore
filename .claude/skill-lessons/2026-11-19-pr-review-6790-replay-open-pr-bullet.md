# measurement · pr-review 0.26.0 against 0.27.0's open-PR bullet · openmrs-core PR 6790 at ff42ef88d4 · 2026-11-19
outcome: the bullet carries the catch, by one run. The open master PR that replaced PR 6790 was filed as a finding by E 2 of 3 and C 1 of 3, with no cross-PR noise in either arm. Under the pre-committed reading (E 2-3 and C 0-1, noise check passed), 0.27.0's bullet stays.

protocol: pre-registered before any arm ran. Owner-directed: the owner asked why no skill-retro was advised before 0.27.0 was pushed. A retro's gate parks a one-record judgment rule until a replay shows it changes what a review files, as it did 0.26.0's P2 and P3, and this is that replay. The candidates were read blind, and the verdicts were recorded before the arm map was opened.
artifacts: kept on the owner's machine only, under `~/.claude/skill-lessons/confidential/artifacts/2026-11-19-6790-replay/`, because they name the replacing PR. They hold the protocol, arm key, briefs, outputs, scripts, snapshots, blind candidates and verdicts, skill diff, audit and lock list.
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-openmrs-core/c9736b83-622f-4f85-a88e-570319ce5419.jsonl

This is capture, not derivation. It follows `2026-11-19-pr-review-core-6790-replaced-by-open-pr.md`.

## Design

- **Subject.** PR 6790 at ff42ef88d4, base 2.9.x at cc20f662d6. Frozen at 2026-11-18 11:00 UTC, 21 minutes before round 7's review was posted. The PR body, the conversation up to the freeze and TRUNK-6844 came from snapshots.
- **A GitHub mirror as of the freeze stood in for gh.** It held the open-PR list (353 PRs) and, for each PR, its slim `pulls/<n>` response, its conversation before the freeze and its diff. A leak scan found no item from after the freeze and no answer-bearing string in it.
- **Arms**, n=3 each, one wave:
  - **C:** `pr-review` 0.26.0.
  - **E:** 0.27.0 with the bullet's last sentence removed, because that sentence states this PR's outcome.
- **Runs:** headless `claude -p --model claude-opus-5-5` at default effort. Each ran Steps 1-3 and wrote its findings as JSON.
- **Isolation, as in the 6535 replay.**
  - Per-run clones came from a frozen bare repo.
  - The network went through a dead-port proxy, with only the model API exempt. The canary showed that gh, curl and git could not reach GitHub or JIRA, that no memory was loaded, and that WebFetch, Skill and Agent were absent.
  - 17 answer-bearing paths were at mode 000 for the wave, and a trap restored them.
  - The audit was calibrated first: 9 of 9 planted forbidden calls flagged, and 0 of 7 allowed ones. It flagged nothing in the six runs' 213 tool calls.

## Measured

- **K-replace (decisive): E 2 of 3, C 1 of 3.** Each catch is a question asking whether PR 6790 waits for the replacing PR, or keeps the back-off in its new store. Each catching run also said so in its verdict. No run mentioned it only outside its findings.
- **Mechanism.** Every run read the mirror's INDEX.
  - Every run that opened the open-PR list found the replacing PR (3 of 3). Every run that didn't open it missed (3 of 3).
  - E opened the list in 2 of 3 runs, and C in 1 of 3.
  - The E miss never opened the list and filed no findings. It was the shortest run, at 3.8 min.
- **Noise.** No finding in either arm raised another open PR. One E run checked the second 2.9.x lockout PR and recorded it in its evidence as a rebase, not a replacement.
- **Recorded, not decisive.**
  - Findings per run: C 1, 1, 1; E 0, 2, 3.
  - The six runs cost $11.93 and took 3.8 to 12.4 min each.

## Reading

- By the pre-committed reading for E 2-3 and C 0-1 with the noise check passed, the bullet stays.
- The signal is weak: one run apart at n=3.
  - C's one catch came after the mirror's INDEX showed it the list. The real round had no index: round 7 ran with live gh and never listed open PRs. If anything, the control is inflated.
  - One E run skipped the bullet entirely.

## Threats

- n=3, one PR, and E without the example sentence.
- The runs used default effort with no subagents.
- Mirror fidelity: bodies and file lists are as read on 2026-11-19.
- The key was written by the session that wrote the bullet.
