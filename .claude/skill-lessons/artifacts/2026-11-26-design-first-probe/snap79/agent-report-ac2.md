I found no way for the head to miss this criterion: `NumberFormatUtil` is deleted, nothing uses it, and nothing replaces it. I'm highly confident in the removal itself. The build check is only partial, because the tests never ran (see the last section).

**Evidence (run):**
- **Head (25b913e):** `git grep -i "numberformatutil\|qtyDisplayFormat\|integerDisplayFormat" HEAD` finds nothing anywhere in the repo, in Java or other files. `api/utils/` now holds only `DateUtil`, `FileUtil`, `GlobalProperties`, `SmtpUtil` and `csv`.
- **No replacement helper:** the head calls `new DecimalFormat(pattern)` directly at each place it formats a number. The patterns are kept as strings:
  - `ModuleConstants.QUANTITY_FORMAT = "#,###,###,##0.##"` at `api/.../api/ModuleConstants.java:30`
  - a private `INTEGER_FORMAT = "#,###,###,##0"` at `api/.../reporting/ReportGenerator.java:37`
  
  These are the same patterns master's `NumberFormatUtil` used.
- **Master has moved since the PR was opened:** its two new commits (#82, #84) add no new uses of `NumberFormatUtil`. I merged `origin/master` into the head in `./merged`. Git merged everything automatically, including `ModuleConstants.java`, where master removed three constants next to the head's added `QUANTITY_FORMAT`; the merged file still has `QUANTITY_FORMAT` at line 24. The grep above also finds nothing in the merged tree.
- **The merged tree compiles on JDK 8:** `mvn -o --batch-mode clean install` compiled all 155 main and 6 test sources of the api module with zero `ERROR` lines in `work/build-merged-jdk8.log`. So no leftover import or call survives the merge.

**Not run:**
- **The test suite:** the JDK 8 build was stopped twice by the background time limit, the second time while surefire was still running the api tests. No surefire reports were written, so I have no pass/fail result for any test, including the PR's `HelperFormattingTest`. The machine's load average was about 225 at the time.
- **The omod module** never got its turn in the build.
- **JDK 11, 17 and 21:** not run.
- **Building the head without the merge:** not run separately. The merged build covers its sources plus master's.
- **No server was started.** None of the timing or concurrency scenarios applies to a class that no longer exists.

Outside this criterion's scope, I did not check whether the output matches master's. The head builds a fresh `DecimalFormat` on every call, so it reads the current default locale each time, where master's static instances read it once at class load.

I left behind only the `./merged` worktree and `work/build-merged-jdk8.log`. No processes of mine are still running.
