ultrathink

You are finishing a code review of a real open-source pull request. Your output goes to a machine, not to GitHub:
post nothing, stage nothing, comment nowhere.

## The method

Read `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/7abec84a-e793-4dfb-aa6b-79a4f7a18e1d/scratchpad/probe-design/skills/k5/SKILL.md` in full and follow it; `posting.md` and `blind-agents.md` beside it are part of it. You have
already done its Steps 1 to 3 on this PR, except the decisions this brief asks you for. Do not redo the review.

## The PR, frozen at 2026-11-24 21:25 UTC

- `openmrs/openmrs-module-stockmanagement` PR 79, "O3-6340: Remove hand-written helper types", head
  `25b913e8b8c5a453a1c129a7583cfc1b48a40f5a`, base `master` at `17bd512`.
- Your working copy is your current folder, a copy at the head. `origin/master` is the base, so the PR diff is
  `git diff origin/master...HEAD` (20 files).
- At that moment: the PR description `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/7abec84a-e793-4dfb-aa6b-79a4f7a18e1d/scratchpad/probe-design/snap79/pr79-body-at-review.md`; the ticket, with its parent and links,
  `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/7abec84a-e793-4dfb-aa6b-79a4f7a18e1d/scratchpad/probe-design/snap79/o3-6340-at-review.json`; its parent `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/7abec84a-e793-4dfb-aa6b-79a4f7a18e1d/scratchpad/probe-design/snap79/o3-6306-at-review.json`; the review conversation
  `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/7abec84a-e793-4dfb-aa6b-79a4f7a18e1d/scratchpad/probe-design/snap79/pr79-conversation-at-review.json`.
- The blind agents' reports, as they wrote them: `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/7abec84a-e793-4dfb-aa6b-79a4f7a18e1d/scratchpad/probe-design/snap79/agent-report-whole.md` (the whole PR), `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/7abec84a-e793-4dfb-aa6b-79a4f7a18e1d/scratchpad/probe-design/snap79/agent-report-ac1.md`
  ("Action2, Pair and the module's own StringUtils are removed"), `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/7abec84a-e793-4dfb-aa6b-79a4f7a18e1d/scratchpad/probe-design/snap79/agent-report-ac2.md` ("NumberFormatUtil is
  removed or reduced to what the standard library does not cover") and `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/7abec84a-e793-4dfb-aa6b-79a4f7a18e1d/scratchpad/probe-design/snap79/agent-report-ac3.md` ("No behaviour
  changes; the build and all tests pass").

## Where the review stands

Your own findings, each verified as written, drafted as comments and not yet posted: `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/7abec84a-e793-4dfb-aa6b-79a4f7a18e1d/scratchpad/probe-design/snap79/drafts.md` (A and B).

Besides what those drafts cover, the blind agents raised the items below. You have not decided on any of them yet.

- S1: The quantity-format test, `HelperFormattingTest.shouldPreserveQuantityDisplayAndHalfEvenRounding`, builds its own
  `new DecimalFormat(ModuleConstants.QUANTITY_FORMAT)`, so none of the 9 production call sites is driven (whole,
  finding 1).
- S2: `new DecimalFormat(ModuleConstants.QUANTITY_FORMAT).format(x)` is written out 9 times; a single thread-safe
  `formatQuantity(BigDecimal)` would avoid the repetition (whole, finding 4).
- S3: Both new tests sit in the root package, `HelperFormattingTest` is named after helpers that no longer exist, and the
  long-address case went from 100,000 to 50,000 repetitions (whole, finding 5).
- S4: The description says the tests were added "before swapping anything", but both test files were created in the
  swap's own commit (whole, finding 2).
- S5: The same `Pair` order change decides which 4 expiry alerts each user gets in `StockBatchExpiryJob`, persistently,
  and above 100 items which items the expiry email lists (ac3, finding 1).
- S6: `import java.util.function.BiConsumer;` sits among the `org.openmrs` imports in the 8 processor files, and the
  anonymous `BiConsumer` classes could be lambdas (whole, finding 6).
- S7: A build without `clean` after building master might keep the old `Pair`, `Action2` and `StringUtils` classes in
  `target/classes`, and so in the jar and `.omod` (ac1, lead).
- S8: The head builds a fresh `DecimalFormat` on every call, so it reads the default locale each time, where master's
  static instances read it once at class load (ac2, last paragraph).

## What to decide

Under the method, decide:
1. for each of S1 to S8, whether it is filed, and how, or set aside, and why;
2. for each of your drafts A and B, whether it goes out as drafted, goes out reworded, or is held back, and why;
3. the first three lines of your report to the user.

You may check anything in your working copy; a build is not required. If you build, run Maven offline (`-o`) with the
narrowest selection that can settle the question.

## Hard constraints

1. Do not read this repository on GitHub or the web, nor the live ticket. `gh` is not authenticated here and the
   network is closed.
2. Do not read anything under `/Users/danielkayiwa/Projects` or `/Users/danielkayiwa/.claude` other than your working
   copy, and nothing under `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/7abec84a-e793-4dfb-aa6b-79a4f7a18e1d/scratchpad` other than the method folder and the snapshot files named above.
3. Start no subagent and no other agent.
4. Restore any file you change, delete any file you add, and leave `git status --porcelain` clean.

## Output

End your reply with exactly one JSON object and nothing after it. Every one of S1 to S8 appears in "filed" or
"set_aside" exactly once, and every draft appears in "drafts" exactly once:
{"filed": [{"item": "S1", "disposition": "blocking|suggestion|question|nit", "first_line": "the comment's first line"}],
 "set_aside": [{"item": "S3", "reason": "one sentence"}],
 "drafts": [{"draft": "A", "decision": "as_drafted|reworded|held", "first_line": "its first line as it goes out, or empty when held", "why": "one sentence"}],
 "report_opening": "the first three lines of your report to the user"}
