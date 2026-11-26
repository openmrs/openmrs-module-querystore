I found no way for the head to miss "Action2, Pair and the module's own StringUtils are removed". I'm confident about the source tree, but I didn't confirm it with a build: the machine was too loaded for any of my Maven runs to finish.

**What I checked, and what it showed**

1. **The three files are deleted at head.** `git diff --stat origin/master...HEAD` lists `api/utils/Action2.java`, `Pair.java` and `StringUtils.java` as deleted, plus `NumberFormatUtil.java` and the old `StringUtilsTest.java`. `git ls-files api/.../api/utils/` at head lists only `DateUtil`, `FileUtil`, `GlobalProperties`, `SmtpUtil` and the `csv/` writers.
2. **Nothing refers to them by full name.** `git grep -lE 'Action2|NumberFormatUtil|api\.utils\.(Pair|StringUtils)' HEAD` returned no files. That covers all tracked files, including the Spring XML, properties, README, `bamboo-specs` and `stockmanagement/docker/dbdump/dump.sql`.
3. **No short-name use still points at the deleted classes.** I checked the imports of every Java file that uses `Pair`, `BiConsumer` or `StringUtils`:
   - `Pair` comes from `org.apache.commons.lang3.tuple.Pair` in `StockManagementServiceImpl`, `StockBatchExpiryJob` and `StockRuleEvaluationJob`.
   - The 8 processors import `java.util.function.BiConsumer`.
   - Every `StringUtils` is `org.apache.commons.lang.StringUtils`.
   - This mattered because three files use `import ...api.utils.*` (`StockManagementServiceImpl:35`, `StockBatchExpiryJob:30`, `ReportGenerator:20`). A wildcard import could have silently resolved to the module's own classes, but they each import the Commons classes explicitly or no longer use the name.
   - `SmtpUtil`, which sits in the same `utils` package, writes the full `org.apache.commons.lang.StringUtils` name every time.
4. **The PR adds no stand-ins.** The only new types are the test classes `HelperFormattingTest` and `UserEmailAddressTest` (with a private `EmailDao`). The email check is now a private `isValidEmail` method in `StockManagementServiceImpl`, not a new `StringUtils`-like class.
5. **The two new master commits don't bring anything back.** `git merge-tree --write-tree origin/master HEAD` merged without conflicts (tree `abf7673f`). That merged tree has no `Pair`, `Action2` or `StringUtils` in `api/utils/`, and none of the names from step 2 appear anywhere in it. The diff `73d9723..origin/master` adds no lines using `Pair`, `StringUtils` or `Action2`.

**A lead I couldn't test (low confidence):** a developer who rebuilds without `clean` after building master might still have the old `Pair.class`, `Action2.class` and `StringUtils.class` in `target/classes`, and so in the jar and `.omod`. This depends on whether `maven-compiler-plugin` 3.15.0 removes classes left from an earlier build. CI runs `clean install`, so CI output would not have this problem.

**What I did not run**

- **The full build and tests** (`mvn -o clean install`, JDK 8): I stopped the first attempt after 20 minutes in the background. Its log stops at `spotless:apply` in the api module, and the cause is unknown. JDK 11, 17 and 21 were not run.
- **The no-`clean` probe** (build master's base in `./stale`, switch to the PR head, package without `clean`, list the jar and `.omod` contents): I stopped it at the time limit. It hadn't printed anything after the JVM started, with about 1,750 other `mvn` processes on the machine. So the lead above is unconfirmed.
- **Checking the built jars** for the deleted classes: no build finished, so I couldn't.
- **Starting an OpenMRS server** to see whether the omod loads without those classes: not run.

I touched nothing outside my folder, and the working tree has no changes except the untracked `stale/` worktree.
