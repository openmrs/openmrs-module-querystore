**Two comments go out, and neither blocks: A is reworded into a question on the expiry job and now covers S5; B goes out as drafted.**
I'm setting aside S1–S4 and S6–S8. Nothing blocks merge, and the approval at `25b913e` stands.
Nothing was posted. The working tree is clean (`git status --porcelain` is empty) and my probe folder is deleted.

**What the decisions rest on, all run this session:**

- **S5 / A: confirmed, but "persistently" overstates it.** I used master's own `Pair` and commons-lang3 3.18.0 on JDK 8. With one store and items 1–5, master's map order is 1,2,3,4,5 and the head's is 1,3,2,5,4. A one-item map gives the same order on both, which is the control.
  - The job sends each user at most 4 alerts (`StockBatchExpiryJob.java:173`), so item 4's alert now goes to item 5.
  - A's totals example (party 3, items 1–10) also reproduces exactly.
  - The overstatement: a batch that gets no alert is not marked (only alerted or emailed batches are, line 141, and the DAO filter at `StockManagementDao.java:1751` picks unmarked ones up again), so it comes back on the next run. The only lasting change is which batches get an alert.
  - It stays a question, not a blocker: master's order was an equally arbitrary hash order, and nothing breaks. It still misses the ticket's "No behaviour changes" as worded, and it contradicts the PR description's "same equals/hashCode semantics".
  - I moved A's anchor to `StockBatchExpiryJob.java:130`, where the order matters, and it names the totals as the other affected place.
- **B:** line 3228 matches the draft, and the pattern ends in `[a-zA-Z]{2,7}$`.
- **S3:** a 50,000-repeat local part still overflows the stack with the pre-#76 pattern, and also with only the local part reverted, on JDK 8, 11, 17 and 21. The head's pattern matches it. So the shortened test still guards the fix.
- **S1 and S2:** all 9 quantity call sites and 5 integer call sites map one-for-one onto master's calls, with the same argument and the same pattern.
- **S8:** no main code in the module calls `Locale.setDefault`.
- **S6:** `BiConsumer` takes the old `Action2` import's place in each file.

```json
{"filed": [{"item": "S5", "disposition": "question", "first_line": "Is it OK that this changes which expiry alerts users get? Commons `Pair` hashes as `left ^ right`, not `31 * left + right`, so this map iterates in a different order and the 4-alert cap picks other batches: with one store and items 1 to 5, item 4's alert now goes to item 5."}],
 "set_aside": [{"item": "S1", "reason": "Greptile raised this gap in thread 4646348610, the author answered and a maintainer approved over it, all 9 call sites swap one-for-one with master's (same argument, same pattern), and testing the untested jobs would grow a refactor PR beyond its ticket."},
  {"item": "S2", "reason": "The pattern already lives in one constant, so changing the format is one edit, and a formatQuantity helper would bring back the very class the ticket asks to remove, with no concrete cost named against the repetition."},
  {"item": "S3", "reason": "50,000 repetitions still overflow the pre-#76 pattern (and one with only the local part reverted) on JDK 8, 11, 17 and 21 while the head's pattern passes, so the test still guards the fix, and the package and class name are taste."},
  {"item": "S4", "reason": "The branch was rewritten on rebase (Greptile reviewed ee683b4, the author replied on 122879b), so its commit order can't show when the tests were written, and the claim changes nothing in the code, whose equivalence the one-for-one call-site mapping already settles."},
  {"item": "S6", "reason": "BiConsumer takes the removed Action2 import's place in each file and the formatter enforces no import order, and turning the anonymous classes into lambdas would be churn the ticket doesn't ask for."},
  {"item": "S7", "reason": "Old classes left in target/ by a build without clean affect every Maven project that deletes a class, CI and releases build with clean, and the author can change nothing in this PR about it."},
  {"item": "S8", "reason": "No main code in the module calls Locale.setDefault, and master's static formatters were themselves built at their first use, not at startup, so no caller can see a difference."}],
 "drafts": [{"draft": "A", "decision": "reworded", "first_line": "Is it OK that this changes which expiry alerts users get? Commons `Pair` hashes as `left ^ right`, not `31 * left + right`, so this map iterates in a different order and the 4-alert cap picks other batches: with one store and items 1 to 5, item 4's alert now goes to item 5.", "why": "The probe showed the swap changes which batches fill each user's 4 expiry alerts, a bigger effect than the order of the totals list, so A moves to StockBatchExpiryJob.java:130, takes in S5 and names the totals as the other affected place."},
  {"draft": "B", "decision": "as_drafted", "first_line": "This `!email.endsWith(\".\")` never changes the result, because the pattern has to end in a letter and so never matches an address ending in a dot.", "why": "Line 3228 and the pattern ending in [a-zA-Z]{2,7}$ were re-checked at the head, a blind reader already cleared the suggestion, and it reads as the optional one-line nit it is."}],
 "report_opening": "Two comments go out, and neither blocks: A is reworded into a question on the expiry job and now covers S5; B goes out as drafted.\nI'm setting aside S1–S4 and S6–S8. Nothing blocks merge, and the approval at 25b913e stands.\nNothing was posted. The working tree is clean (git status --porcelain is empty) and my probe folder is deleted."}
```
