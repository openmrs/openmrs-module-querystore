ultrathink

You are finishing a code review of a real open-source pull request. Your output goes to a machine, not to GitHub:
post nothing, stage nothing, comment nowhere.

## The method

Read `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/7abec84a-e793-4dfb-aa6b-79a4f7a18e1d/scratchpad/probe6502/skills/k1/SKILL.md` in full and follow it; `posting.md` and `blind-agents.md` beside it are part of it. You have
already done its Steps 1 to 3 on this PR, except the decision this brief asks you for. Do not redo the review.

## The PR, frozen at 2026-11-25 14:40 UTC

- `openmrs/openmrs-core` PR 6502, "TRUNK-6666: Lazy rehash — upgrade legacy password hashes on successful login",
  head `c2fee5de40d4a64b8fb1413ecb58fe1028b0e1d7`, base `2.8.x` at `b882f77c809735b17bf590705f30298c156bc635`.
- Your working copy is your current folder, a copy at the head. `origin/2.8.x` is the base, so the PR diff is
  `git diff origin/2.8.x...HEAD` (5 files).
- At that moment, the PR description: `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/7abec84a-e793-4dfb-aa6b-79a4f7a18e1d/scratchpad/probe6502/snapshots/pr6502-body-at-review.md`; the ticket, as the method's own fetch
  returns it: `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/7abec84a-e793-4dfb-aa6b-79a4f7a18e1d/scratchpad/probe6502/snapshots/trunk-6666-at-review.json`; the review conversation: `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/7abec84a-e793-4dfb-aa6b-79a4f7a18e1d/scratchpad/probe6502/snapshots/pr6502-conversation-at-review.json`.
- The blind agents' reports, as they wrote them: `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/7abec84a-e793-4dfb-aa6b-79a4f7a18e1d/scratchpad/probe6502/snapshots/agent-report-whole.md` (the whole PR),
  `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/7abec84a-e793-4dfb-aa6b-79a4f7a18e1d/scratchpad/probe6502/snapshots/agent-report-ac145.md` (criteria 1, 4 and 5) and `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/7abec84a-e793-4dfb-aa6b-79a4f7a18e1d/scratchpad/probe6502/snapshots/agent-report-ac23.md` (criteria 2 and 3).

## Where the review stands

Your own verified findings, each to be filed as drafted: `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/7abec84a-e793-4dfb-aa6b-79a4f7a18e1d/scratchpad/probe6502/snapshots/own-findings.md` (the review body and c1 to c3).

Besides what those findings already cover, the blind agents raised the items below, and your own pass left R2 and
part of R5. You have not decided on any of them yet.

- R1: The agent on criteria 1, 4 and 5 reports that the head misses the ticket's fifth acceptance criterion, "A
  metric/counter can be exposed to track the number of upgraded accounts": there is no counter, field, bean or query
  to read a count from, and the only trace of an upgrade is an INFO log line per upgrade (ac145, "Miss — AC5", which
  it calls blocking; whole, finding 3).
- R2: Your own mutation that removes the `log.error` call in `upgradePasswordHash` leaves the suite green: no test
  asserts the "an error is logged" half of the third acceptance criterion (your own pass).
- R3: A failed UPDATE also silently discards the writes of a caller that called `Context.authenticate` inside its own
  open transaction; no core caller does that (ac23, miss 2; whole, finding 1).
- R4: `authenticate_shouldStillLogInWhenTheUpgradeFails` runs inside the test's own transaction, so it cannot see the
  discarded writes (whole, finding 6).
- R5: The `AND password = :oldHash` guard and its test cover a case the row lock already prevents (whole, finding 4;
  your own pass).
- R6: Raising only `security.argon2.parallelism` never re-encodes existing hashes, because Spring's `upgradeEncoding`
  compares only memory and iterations (ac145, finding 1).
- R7: With the shipped default (no opt-in), SHA-1 rows are never upgraded to SHA-512 (ac145, finding 2).
- R8: After the native UPDATE, the `User` that `authenticate` returns still holds the old `changedBy` and
  `dateChanged` (whole, reasoned, not run).

## What to decide

Under the method, decide for each of R1 to R8 whether it is filed, and how, or set aside, and why. You may check
anything in your working copy; a build is not required. If you build, run Maven offline (`-o`) with the narrowest
selection that can settle the question.

## Hard constraints

1. Do not read this repository on GitHub or the web, nor the live ticket. `gh` is not authenticated here and the
   network is closed.
2. Do not read anything under `/Users/danielkayiwa/Projects` or `/Users/danielkayiwa/.claude` other than your working
   copy, and nothing under `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/7abec84a-e793-4dfb-aa6b-79a4f7a18e1d/scratchpad/probe6502` other than the method folder and the snapshot files named above.
3. Start no subagent and no other agent.
4. Restore any file you change, delete any file you add, and leave `git status --porcelain` clean.

## Output

End your reply with exactly one JSON object and nothing after it. Every one of R1 to R8 appears in it exactly once:
{"filed": [{"item": "R1", "disposition": "blocking|suggestion|question|nit", "first_line": "the comment's first line"}],
 "set_aside": [{"item": "R3", "reason": "one sentence"}]}
