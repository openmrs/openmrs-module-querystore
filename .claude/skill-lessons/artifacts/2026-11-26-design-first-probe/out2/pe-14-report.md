Post two non-blocking comments: draft A reworded to cover the expiry alerts, and draft B as drafted. Nothing blocks merge, and Dennis's approval at this head stands.

The PR does what O3-6340 asks. `Action2`, `Pair`, the module's `StringUtils` and `NumberFormatUtil` are all gone. Each of master's 10 `qtyDisplayFormat` and 5 `integerDisplayFormat` calls now uses the same pattern string. The email regex and its guards are unchanged. The one behaviour change is the `Pair` hash order.

It also fits its plan, epic O3-6306 (StockManagement v4.0), under the "Foundation" cleanup. Of the blind agents' eight items, only S5 goes out, merged into draft A. I set the other seven aside.

**Evidence I ran**
- **`Pair` order:** I ran master's own `Pair.java` against commons-lang3 3.18.0 on the PR's grouping expression, with stock items at party 3.
  - With 1 item (the control), the order is the same.
  - With 5 items, master gives `[3,4,5,1,2]` and the head gives `[3,2,1,5,4]`.
  - With 10 items, the order is as draft A says.
- **Why that matters (S5):** `StockBatchExpiryJob` stops after `maxAlerts = 4` while walking that order. It then marks the alerted batches notified (`StockBatchExpiryJob.java:141`), and the DAO never selects them again (`sb.expiryNotificationDate is null`, `StockManagementDao.java:1751`). So a user with more than 4 expiring batches can be alerted about a different 4. This is a second caller of the same cause, so it goes into A and is not a separate comment.
- **Email regex (B and S3):** I compared the head pattern with the pattern from before #76.
  - The old pattern overflows the stack from 5,000 repetitions upward. The head pattern handles 50,000 and 100,000.
  - No address ending in `.` matches the head pattern.
  - So the `endsWith(".")` guard does nothing. B's suggestion also keeps the 50,000-repetition trailing-dot test case returning false without an overflow.
  - The halved count still guards the possessive fix, which settles S3's coverage point.
- **Precedent (S2 and S8):** #76 (`73d9723`, on master) changed `DateUtil` to "create a formatter per call". So per-call `DecimalFormat` follows what the project already did. Nothing in the module's main sources calls `Locale.setDefault`.
- **Anchors:** reworded A goes on `StockBatchExpiryJob.java:130`, the line that builds the grouped map. B stays on `StockManagementServiceImpl.java:3228`.
- **Not run:** I did not run Maven or the formatter on B's suggestion line.

My probe folder is deleted, and `git status --porcelain` is empty.

{"filed": [{"item": "S5", "disposition": "question", "first_line": "Was this order change intended? Commons-lang3 `Pair` hashes as `left ^ right`, not `31 * left + right`, so this map iterates differently, and with more than 4 expiring batches a user can be alerted about a different 4, which are then marked notified."}],
 "set_aside": [{"item": "S1", "reason": "Greptile raised it and the author answered by moving the pattern into the shared constant; the part left over, a call site using the wrong pattern, does not happen at head, where all 10 quantity call sites use QUANTITY_FORMAT one-for-one with master's qtyDisplayFormat, and pinning them would mean driving the scheduled jobs, which is beyond the scope of this PR."},
 {"item": "S2", "reason": "The ticket asks for NumberFormatUtil to go, and a formatQuantity helper would bring it back; #76 on master chose a formatter per call for dates, and since the pattern is one shared constant, the 10 copies cannot drift apart."},
 {"item": "S3", "reason": "50,000 repetitions still overflow the regex from before #76 (it overflows from 5,000 up in my probe) and the domain case kept 100,000, so the test still guards the fix; the package and class name are a matter of taste."},
 {"item": "S4", "reason": "Commit order inside a PR that gets squashed changes nothing the author has to do, and the claim that matters, that the output stays the same, holds by construction: same patterns, call sites one-for-one, same regex and guards."},
 {"item": "S6", "reason": "Neither spotless nor the project enforces an import order, and keeping the anonymous classes limits the diff to a type swap where lambdas would be a drive-by rewrite."},
 {"item": "S7", "reason": "Every class deletion behaves this way in an incremental build without clean, so it is not a defect of this PR; CI and the PR's checklist build with clean, and the lead was never confirmed."},
 {"item": "S8", "reason": "A formatter per call follows #76's precedent on master, nothing in the module changes the JVM default locale, and the static instance it replaces only fixed whichever locale was current when the class first loaded."}],
 "drafts": [{"draft": "A", "decision": "reworded", "first_line": "Was this order change intended? Commons-lang3 `Pair` hashes as `left ^ right`, not `31 * left + right`, so this map iterates differently, and with more than 4 expiring batches a user can be alerted about a different 4, which are then marked notified.", "why": "The same hash change decides which expiry alerts go out, and those batches are permanently marked notified, so the question goes on StockBatchExpiryJob.java:130 and names the REST totals as the other caller rather than becoming a second comment on the same cause."},
 {"draft": "B", "decision": "as_drafted", "first_line": "This `!email.endsWith(\".\")` never changes the result, because the pattern has to end in a letter and so never matches an address ending in a dot. If you want to drop it:", "why": "My probe confirms no address ending in a dot matches the pattern, and the suggested line keeps the long trailing-dot test case returning false without an overflow, so it stands as an optional nit on a guard nothing reaches."}],
 "report_opening": "Post two non-blocking comments: draft A reworded to cover the expiry alerts, and draft B as drafted. Nothing blocks merge, and Dennis's approval at this head stands.\nThe PR does what O3-6340 asks. `Action2`, `Pair`, the module's `StringUtils` and `NumberFormatUtil` are all gone. Each of master's 10 `qtyDisplayFormat` and 5 `integerDisplayFormat` calls now uses the same pattern string. The email regex and its guards are unchanged. The one behaviour change is the `Pair` hash order.\nIt also fits its plan, epic O3-6306 (StockManagement v4.0), under the \"Foundation\" cleanup. Of the blind agents' eight items, only S5 goes out, merged into draft A. I set the other seven aside."}
