I'd post one comment: a non-blocking question built from draft A, reworded to cover S5 and moved to `StockBatchExpiryJob.java:130`. Hold draft B and set aside the other seven items. Nothing blocks merge.

**Draft A.** S5 is the reason for the rewording: it found that the `Pair` change also decides "which 4 expiry alerts each user gets". I ran master's `Pair` source and commons-lang3 3.18.0's `Pair` through the PR's exact `groupingBy` code:
- **Expiry alerts:** with one store and items 1 to 5, master orders them 1,2,3,4,5 and the head orders them 1,3,2,5,4. The 4-alert loop (`StockBatchExpiryJob.java:183-213`) takes the first four, so item 5 now gets an alert in place of item 4.
- **Inventory totals:** with party 3 and items 1 to 10, A's original example reproduces: master gives 3,4,…,10,1,2 and the head gives 3,2,1,7,6,5,4,10,9,8.
- **Control:** a single pair comes out the same on both.

The ticket's purpose is arguably met, because master's order was arbitrary too. So under the method this stays a question that names the "No behaviour changes" criterion, not a blocker. It carries no fix, so it doesn't need a blind read.

**Draft B is held.** I confirmed the guard never runs: every string ending in "." fails the pattern. But the suggestion block is a fix I'm recommending, and the method requires a blind read of it before it's shown. This round can't start agents, so that read is still owed. Posting it as a question would just be a nit dressed up as one.

**The S-items:**
- **S1:** Greptile already raised it. The author answered by moving the pattern into a shared constant, and Dennis Kigen approved after that exchange. All 10 call sites read that one constant. The blind agent counted 9, but the grep finds 6 + 2 + 2.
- **S2:** a shared `formatQuantity` helper would bring back the helper the ticket removes.
- **S3:**
  - I ran the old backtracking pattern from before #76 at 50,000 repetitions and it still overflows the stack, so the shorter test still catches that regression.
  - `StockManagementServiceTest` already sits in the root package, so the placement matches.
- **S4:** I ran every expectation in both new tests against master's `NumberFormatUtil` and `StringUtils`: no mismatches. As a control, switching the locale to German produces 9.
- **S6:** the `BiConsumer` import sits on the same line the `Action2` import held.
- **S7:** any PR that deletes a class has this; it isn't specific to this change.
- **S8:** neither this module's main code nor core 2.8.0's API calls `Locale.setDefault`. I scanned 67 classes and the control check found `getDefault`. I didn't check `openmrs-web` because its jar isn't available locally.

**Build:** I ran `mvn -o package` at the head on JDK 21 in a scratch worktree. It passed: 94 api tests and 47 omod tests. Still not run: JDK 8, 11 and 17, and a build of the head merged with current master. The working copy is clean and the scratch files are deleted.

{"filed": [{"item": "S5", "disposition": "question", "first_line": "Is this order change intended? Commons `Pair` hashes as `left ^ right`, not `31 * left + right`, so with one store and one expiring batch each for items 1 to 5, the 4-alert cap now picks items 1, 3, 2, 5 instead of 1 to 4. Inventory `totals` reorder too, and the ticket asks for no behaviour changes."}],
 "set_aside": [{"item": "S1", "reason": "Greptile's thread already raised it, the author answered by sharing the pattern in ModuleConstants, the maintainer approved after that exchange, and all 10 call sites read that one constant, so the remaining gap guards nothing this PR changes and master never tested those call sites either."},
  {"item": "S2", "reason": "The ticket asks for NumberFormatUtil to be replaced by standard formatting where it adds nothing, so a formatQuantity helper would bring it back, and with the pattern in one constant the repetition can't drift."},
  {"item": "S3", "reason": "The old backtracking pattern from before #76 still overflows the stack at 50,000 repetitions, so the shorter case still catches the regression, and existing tests such as StockManagementServiceTest already sit in the root package, so placement and naming are taste."},
  {"item": "S4", "reason": "Every expectation in both new tests passes against master's NumberFormatUtil and StringUtils (no mismatches; a German-locale control gives 9), so the tests do hold on the old helpers and the commit order changes nothing the approver relied on."},
  {"item": "S6", "reason": "The BiConsumer import replaces the Action2 import on the same line, the formatter enforces no import order, and turning the anonymous classes into lambdas would add hunks the ticket doesn't ask for."},
  {"item": "S7", "reason": "Old class files surviving a build without clean follow from deleting any class in any PR, not from this change, and the agent left it as an untested lead with nothing the author could change here."},
  {"item": "S8", "reason": "The difference needs the JVM default locale to change after first use, and neither this module's main code nor core 2.8.0's API calls Locale.setDefault (bytecode scan of 67 classes with a getDefault control)."}],
 "drafts": [{"draft": "A", "decision": "reworded", "first_line": "Is this order change intended? Commons `Pair` hashes as `left ^ right`, not `31 * left + right`, so with one store and one expiring batch each for items 1 to 5, the 4-alert cap now picks items 1, 3, 2, 5 instead of 1 to 4. Inventory `totals` reorder too, and the ticket asks for no behaviour changes.", "why": "S5 found the same hash-order change decides which batches get the 4 alerts, a user-facing effect stronger than the REST totals order, so A leads with it on StockBatchExpiryJob.java:130 and stays a question because the old order was arbitrary too."},
  {"draft": "B", "decision": "held", "first_line": "", "why": "The guard really never runs, but the suggestion block is a fix I'm recommending and the method requires a blind read of it, which this round can't start, and as a question it would be a nit in disguise."}],
 "report_opening": "Post one comment, a non-blocking question on StockBatchExpiryJob.java:130 (draft A reworded to cover S5); hold draft B; set aside S1-S4 and S6-S8; nothing blocks merge.\nRight for the ticket (O3-6340): Action2, Pair, StringUtils and NumberFormatUtil are gone with nothing referring to them, the new tests also pass on master's old helpers, and the head builds offline on JDK 21 with 94 api and 47 omod tests passing; the one behaviour change is the Pair hash order A asks about.\nRight for the plan (O3-6306, StockManagement v4.0, Foundation area): a self-contained cleanup the later service split and REST rework build on; still owed: B's blind read, and test runs on JDK 8/11/17 and on the head merged with current master."}
