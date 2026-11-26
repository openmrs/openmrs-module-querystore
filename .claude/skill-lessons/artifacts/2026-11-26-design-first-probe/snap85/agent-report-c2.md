Yes. At 8f7bb30, two settings still ignore values they can't parse without logging anything, and three numeric settings take out-of-range values silently. Every other setting read now logs.

I ran my own probe test, `SettingsLoggingProbeTest`, through the real `AdministrationService` on the test database. For each setting it stored a value, ran the same read expression the head uses at that call site, and caught every WARN-or-higher log event from any logger. A twin probe on `origin/master`, `MasterSettingsProbeTest`, called the old `GlobalProperties` getters. Commands:
- head: `mvn -o --batch-mode test -pl api -Dtest=SettingsLoggingProbeTest -Dsurefire.failIfNoSpecifiedTests=false` → `Tests run: 1 … BUILD SUCCESS`
- master: the same command in `_wt-master` with `-Dtest=MasterSettingsProbeTest` and the spotless, license and jacoco plugins skipped.

Checking the probe against known cases first: page size `'invalid'` logs `ERROR AdministrationServiceImpl: Unable to turn value 'invalid' into type java.lang.Integer`, and page size `'1000'` logs nothing. The probe's appender is attached to several loggers, so the event counts below repeat; read them only as zero or not zero.

## Defects

**1. `negativeStockBalanceAllowed` and `allowStockIssueWithoutRequisition` drop unparseable values silently.** This is on lines the PR rewrote.
- **Where:** `StockManagementServiceImpl.java:1245-1246` and `:1926-1927`.
- **Why:** core turns the value into a Boolean with `new Boolean(String)`, which never throws, so core's error log is never reached (`AdministrationServiceImpl.java:740` at 2.8.0).
- **Head:** `'yes'`, `'1'`, `' true'`, `'true '`, `'Y'`, `'on'`, `'invalid'` and `' '` all read as `false | warn+events=0`, for both keys.
- **Effect:** an admin who sets `allowStockIssueWithoutRequisition=yes` gets every requisition-less issue refused, with no log saying why.
- **Master:** the same inputs give the same result (`'yes' -> false | warn+events=0`).
- **Inconsistent with the PR's own fix:** the PR added warning helpers for the two job switches and the pack-size priority. For those, `' yes'` and `'maybe'` produce `WARN … Invalid value`, but these two booleans got no helper. The PR's own test `ordinaryBooleansShouldKeepBooleanParseBooleanSemantics` asserts `"yes"`, `"1"`, `" true "` and `"invalid"` → false, and never checks for a log.

**2. `stockItemsMaxUploadFileSize` accepts negative and overflowing values silently.** This is on lines the PR rewrote.
- **Where:** `StockItemImportController.java:71-72` and `:79-80`, in the expression `getGlobalPropertyValue(…, 2L) * 1024 * 1024`.
- **Head:**
  - `'-1'` gives `-1048576`
  - `'0'` gives `0`
  - `'8796093022208'` gives `-9223372036854775808`
  - `'9223372036854775807'` gives `-1048576`
  - all four with `warn+events=0`. Any limit of zero or below rejects every upload.
- **Master:** the same results, with no log.
- **Inconsistent with the PR's own fix:** the PR added logged bounds for five other numeric settings but not this one.

**3. `stockOperationNotificationEmail` drops an invalid address silently.** The PR rewrote the read at `:2905-2906` but left the drop at `:2907-2908`.
- **Where:** `StockManagementServiceImpl.java:2907-2908` sets the value to `null` when `isValidEmail` fails.
- **Head:** `'not-an-email'`, `'a@b.'` and `' ops@example.org'` all give `null | warn+events=0`.
- **Effect:** if the role setting is also blank, the only log line is the INFO message "Email and role notification settings not set", which is false: the email setting is set.
- **Master:** `'not-an-email' -> null | warn+events=0`.

## Weaker findings

**4. `excessReceivedItemThreshold` accepts a negative value without a log.**
- **Where:** `StockManagementServiceImpl.java:1513-1514`.
- **Head:** `'-5'` and `'0'` are accepted with `warn+events=0`. At `:1541`, a threshold of `-5` means receiving exactly the sent quantity is refused.
- **Master:** the same, but malformed `'abc'` was silent on master and now logs at ERROR on the head.

**5. `stockItemSearchMaxIntermediateResult` passes `0` and `-5` through without a log.**
- **Head:** `'0' -> 0`, `'-5' -> -5`, both `warn+events=0`. Master gives the same.
- **Unset or bad values lead to a crash (not run):** when the setting is unset, blank or malformed, the value is null. `StockItemResource.java:166-170` passes that `Integer` into `searchStockItemCommonName(…, int maxItems)` (`StockManagementServiceImpl.java:358`). Unboxing a null into an `int` throws a NullPointerException, so the PR's "blank means no limit" is not what happens. That surfaces as a REST error rather than a silent swallow, and master returns null too. I did not run the omod resource, so this rests on the method signatures only.

**6. Core's parse error never names the setting.** It reads `Unable to turn value 'abc' into type java.lang.Integer`, with no key. If several numeric settings are malformed, an admin can only tell them apart from the stack trace.

## Settings that now log correctly (all with WARN or ERROR events on the head)
- **Job switches:** `'maybe'`, `' '` and `' yes'` log.
- **Pack-size priority:** `'yes'` and `'TRUE '` log; blank means true and stays silent, as intended.
- **Search limit:** `' '`, `' 5'`, `'abc'` and `'99999999999'` log.
- **Batch size, notice period, job expiry:** `-1` and `0` log, except notice period `0`, which is legal. `'abc'` logs.
- **Days in month:** `0`, `-1`, `Infinity` and `NaN` log.
- **Page size, threshold, upload size:** malformed values log.

The probe values `2147483647` (batch size, notice period, job expiry) are accepted silently; they are valid ints.

## Checked by reading the code, not run
- **Settings that require a view privilege:** core throws `APIException` (`AdministrationServiceImpl.java:172`). Every job's `catch` logs it with `log.error`, so nothing is swallowed.
- **The notification thread:** `StockOperationNotificationTask.java:53-55` also logs.
- **`SmtpUtil`'s mail settings:** they are read as plain strings, with nothing to parse.
- **`unknownConceptId`, observation concepts:** a value that doesn't resolve shows up as an import row error or a failed batch job. Core's `getConcept(String)` swallows the NumberFormatException for an all-digit value that overflows an int (2.8.0 `ConceptServiceImpl.java:491-496`), but the row error still appears.

## Not run
- The full CI build and the PR's own test suite, on any JDK. A whole-project `install` hung and I stopped it. My single-class runs took 8–14 minutes each.
- Finding 3 through the real `sendStockOperationNotification` method: I ran its read expression and `isValidEmail` only.
- The null crash in finding 5.

## Files left in place
- `work/api/src/test/java/org/openmrs/module/stockmanagement/probe/SettingsLoggingProbeTest.java` (untracked)
- the worktree `work/_wt-master` at `origin/master` 17bd512, containing `api/src/test/java/org/openmrs/module/stockmanagement/MasterSettingsProbeTest.java`
- logs in `../log/`: `probe1.txt` (head), `probe-master.txt`, and `install.txt` (the stopped build)

No PR files were changed: `git status` shows only these untracked paths.
