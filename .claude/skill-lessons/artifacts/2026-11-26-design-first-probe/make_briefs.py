import os, sys
PD = os.path.dirname(os.path.abspath(__file__))
SCR = os.path.dirname(PD)
HEAD = """ultrathink

You are finishing a code review of a real open-source pull request. Your output goes to a machine, not to GitHub:
post nothing, stage nothing, comment nowhere.

## The method

Read `{PD}/skills/{K}/SKILL.md` in full and follow it; `posting.md` and `blind-agents.md` beside it are part of it. You have
already done its Steps 1 to 3 on this PR, except the decisions this brief asks you for. Do not redo the review.
"""
PR85 = """
## The PR, frozen at 2026-11-24 21:31 UTC

- `openmrs/openmrs-module-stockmanagement` PR 85, "O3-6352: Read settings through the administration service and
  remove GlobalProperties", head `8f7bb30dd038e4d5c36b0f78cbc46af1c9d07812`, base `master` at `17bd512`.
- Your working copy is your current folder, a copy at the head. `origin/master` is the base, so the PR diff is
  `git diff origin/master...HEAD` (24 files).
- At that moment: the PR description `{S}/pr85-body-at-review.md`; the ticket, with its parent and links,
  `{S}/o3-6352-at-review.json`; its parent `{S}/o3-6306-at-review.json`; the review conversation
  `{S}/pr85-conversation-at-review.json`.
- The blind agents' reports, as they wrote them: `{S}/agent-report-whole.md` (the whole PR), `{S}/agent-report-c14.md`
  (the criteria "No static settings utility remains" and "The build and all tests pass"), `{S}/agent-report-c2.md`
  ("No setting read swallows an exception silently") and `{S}/agent-report-c3.md` ("Each setting keeps its current
  default").

## Where the review stands

Your own findings, each verified as written, drafted as comments and not yet posted: `{S}/drafts.md` (A, B, C and E).

Besides what those drafts cover, the blind agents raised the items below, and your own pass left R9. You have not
decided on any of them yet.

- R1: The criterion agent and the whole-PR agent each report that the head misses the ticket's first acceptance
  criterion, "No static settings utility remains", by its letter: `ModuleConstants` now has a logger and six public
  static helpers (`parseOptionalInteger`, `parseJobEnabled`, `parseUomPriority`, `atLeast`, and two `positiveValue`
  overloads), each taking a setting's key and value and returning the value to use (c14, section 1; whole, finding 5).
- R2: A caller without core's "Get Global Properties" privilege now gets an `APIAuthenticationException` where
  master's getters returned the default (c3; whole, finding 6).
- R3: The defaults are copied across call sites: `1000` twelve times in six reports, `30.5` six times, and the
  pack-size read five times in the service (whole, finding 4).
- R4: The job switches and the search limit read text with `getGlobalPropertyValue(key, "")`, while the nullable text
  settings use `getGlobalProperty(key, null)`; `getGlobalProperty(key, "")` would be simpler and consistent (whole,
  finding 7).
- R5: The code defaults differ from `config.xml`'s (rule batch size 100 against 1000, batch job expiry 1440 against
  10080), as on master (whole, finding 7).
- R6: Three numeric settings take out-of-range values without a log, as on master: a negative or overflowing upload
  size, a negative excess threshold, and a search limit of 0 or -5 (c2, findings 2, 4 and 5).
- R7: An invalid notification email is set to null without a log at `StockManagementServiceImpl.java:2907-2908`, a
  line the PR does not change, as on master (c2, finding 3).
- R8: Core's parse error names the value but not the setting's key (c2, finding 6).
- R9: One `Context` import moved within the import block the PR already edits in `StockManagementServiceImpl` (your own
  pass).
"""
PR79 = """
## The PR, frozen at 2026-11-24 21:25 UTC

- `openmrs/openmrs-module-stockmanagement` PR 79, "O3-6340: Remove hand-written helper types", head
  `25b913e8b8c5a453a1c129a7583cfc1b48a40f5a`, base `master` at `17bd512`.
- Your working copy is your current folder, a copy at the head. `origin/master` is the base, so the PR diff is
  `git diff origin/master...HEAD` (20 files).
- At that moment: the PR description `{S}/pr79-body-at-review.md`; the ticket, with its parent and links,
  `{S}/o3-6340-at-review.json`; its parent `{S}/o3-6306-at-review.json`; the review conversation
  `{S}/pr79-conversation-at-review.json`.
- The blind agents' reports, as they wrote them: `{S}/agent-report-whole.md` (the whole PR), `{S}/agent-report-ac1.md`
  ("Action2, Pair and the module's own StringUtils are removed"), `{S}/agent-report-ac2.md` ("NumberFormatUtil is
  removed or reduced to what the standard library does not cover") and `{S}/agent-report-ac3.md` ("No behaviour
  changes; the build and all tests pass").

## Where the review stands

Your own findings, each verified as written, drafted as comments and not yet posted: `{S}/drafts.md` (A and B).

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
"""
DECIDE = """
## What to decide

Under the method, decide:
1. for each of {ITEMS}, whether it is filed, and how, or set aside, and why;
2. for each of your drafts {DRAFTS}, whether it goes out as drafted, goes out reworded, or is held back, and why;
3. the first three lines of your report to the user.

You may check anything in your working copy; a build is not required. If you build, run Maven offline (`-o`) with the
narrowest selection that can settle the question.

## Hard constraints

1. Do not read this repository on GitHub or the web, nor the live ticket. `gh` is not authenticated here and the
   network is closed.
2. Do not read anything under `/Users/danielkayiwa/Projects` or `/Users/danielkayiwa/.claude` other than your working
   copy, and nothing under `{SCR}` other than the method folder and the snapshot files named above.
3. Start no subagent and no other agent.
4. Restore any file you change, delete any file you add, and leave `git status --porcelain` clean.

## Output

End your reply with exactly one JSON object and nothing after it. Every one of {ITEMS} appears in "filed" or
"set_aside" exactly once, and every draft appears in "drafts" exactly once:
{{"filed": [{{"item": "{FIRST}", "disposition": "blocking|suggestion|question|nit", "first_line": "the comment's first line"}}],
 "set_aside": [{{"item": "{SECOND}", "reason": "one sentence"}}],
 "drafts": [{{"draft": "A", "decision": "as_drafted|reworded|held", "first_line": "its first line as it goes out, or empty when held", "why": "one sentence"}}],
 "report_opening": "the first three lines of your report to the user"}}
"""
for case, body, items, drafts, first, second in (("85", PR85, "R1 to R9", "A, B, C and E", "R1", "R3"),
                                                   ("79", PR79, "S1 to S8", "A and B", "S1", "S3")):
    S = os.path.join(PD, "snap" + case)
    for k in ("k1", "k2"):
        text = HEAD.format(PD=PD, K=k) + body.format(S=S) + DECIDE.format(ITEMS=items, DRAFTS=drafts, SCR=SCR, FIRST=first, SECOND=second)
        open(os.path.join(PD, "briefs", f"brief-{case}-{k}.md"), "w").write(text)
print("briefs:", sorted(os.listdir(os.path.join(PD, "briefs"))))
