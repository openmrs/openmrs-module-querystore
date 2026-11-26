I couldn't confirm whether PR #85 is ready to merge, because I didn't finish running the build or the four named test classes. Each Spring-context test class took about 15 minutes to start here, and the full head build was stopped at its 30-minute limit after one class. Reading the code, I'd ask for changes on two acceptance criteria: some bad settings are still ignored silently, and the default tests don't check the module's own code.

## What ran and what didn't

| Run | Result |
|---|---|
| Full build at head, JDK 17 | **Did not finish.** Only `StockOperationDTOValidatorTest` reported: `Tests run: 11, Failures: 0, Errors: 0` (970 s). Stopped at the 30-minute limit. |
| `PropertiesUtilTest` (head with mutations, see below) | `Tests run: 2, Failures: 0` |
| `GlobalPropertyDefaultsTest`, `ModuleConstantsTest` | **Not run.** Both were queued in the mutated run, which I stopped at the time limit. |
| `StockManagementServiceTest` | **Not run.** |
| My probe test, on master and head | **Not run.** Each was still starting the test context when I stopped it. |
| Build on JDK 8, 11 and 21 | **Not run.** |

Because of this, the findings below come from reading the head files, the deleted `GlobalProperties.java` at `origin/master`, and core's `AdministrationServiceImpl` at tag 2.8.0. None has a measured runtime result. I read every hunk with its context, plus `ModuleConstants`, `PropertiesUtil` and the three new test files in full. I did not read all of the ~3,100-line `StockManagementServiceImpl.java`, only its changed hunks.

## Findings

**1. Two yes/no settings still ignore a bad value silently, against the ticket ("No setting read swallows an exception silently").**
- **Where:** `StockManagementServiceImpl.java:1246` (`ALLOW_STOCK_ISSUE_WITHOUT_REQUISITION`) and `:1927` (`NEGATIVE_STOCK_BALANCE_ALLOWED`).
- **Why:** both read `getGlobalPropertyValue(key, false)`, which builds the value with `new Boolean(String)`. That constructor never throws, so core's error log (`AdministrationServiceImpl` 2.8.0, lines 749–751) is never reached.
- **Input:** a value of `yes`, `1`, `Y` or `ture` becomes `false` with no log line. Master also gives `false`, also with no log, so the behaviour is unchanged.
- **Inconsistency:** the PR description says "nothing is swallowed silently any more", and it does add a warning for the same mistake in the pack-size setting (`ModuleConstants.parseUomPriority`).
- **PR's own test:** `ordinaryBooleansShouldKeepBooleanParseBooleanSemantics` asserts `invalid` → `false` and never checks for a log line, so it confirms this rather than catching it.
- **Probe:** written to show it (`booleansMalformed`), not run.

**2. The consumption report now logs once per row when the days-in-month setting is bad.**
- **Where:** `StockConsumptionReport.java:59`.
- **Why:** the setting is read inside `stockInventoryResult.getData().forEach(...)`, so it is read once for every row.
- **Input:** with `reportingCalculationsNoDaysInMonth=30,5`, core logs an ERROR with a full stack trace for every row. With `0`, `ModuleConstants.positiveValue` logs a WARN for every row. A report with thousands of rows logs thousands of times.
- **Master:** reads the same setting per row but logs nothing.
- **Fix:** read it once before the loop, as `StockForecastReport` and `StockExpiryForecastReport` already do with their `daysInMonth` field.
- **Probe:** written to count the log events for 500 rows (`consumptionReportMalformedDays`, `consumptionReportZeroDays`), not run.

**3. The tests don't cover any of the defaults the PR wrote into the code.**
- **What the tests check:** `GlobalPropertyDefaultsTest` calls core's `service.getGlobalPropertyValue(key, <literal>)` with its own default values. It never touches a module call site.
- **Re-written clamping:** `numericBoundsShouldRetainClampingRatherThanSubstituteDefaults` (lines 139–142) uses `Math.max` instead of the PR's `ModuleConstants.atLeast`.
- **Mutation applied:** in the probe copy I changed three call-site defaults: excess-received threshold 10→20, expiry notice period 120→60, and the `DispensingLogsReport` page size 1000→500.
- **Mutation not tested:** the test run was stopped before reaching these tests, so I have no result. From the test source, the expected outcome is still green. The mutations are reverted.
- **Consequence:** the PR's claim "`GlobalPropertyDefaultsTest` checks each one when unset" covers core, not this module.

**4. The ticket asks for "the default stated at the call site or in one place per setting", but the PR copies defaults across many call sites.**
- **Repeated literals:** `1000` appears 12 times across 6 reports, `30.5` 6 times, `1440` twice in one expression in `StockManagementServiceImpl` (~line 3105), and `2L` twice in `StockItemImportController`.
- **Repeated expression:** the two-line pack-size read is pasted 5 times in `StockManagementServiceImpl`.
- **Risk:** together with finding 3, any one copy can drift from the others and no test will notice.
- **Better shape:** one typed read per setting (default and rules in one place). The ticket's "in one place per setting" allows this.

**5. The ticket says "No static settings utility remains", but some of one is now in `ModuleConstants`.**
- **Where:** `ModuleConstants.java:71–131`.
- **What moved:** six static parse and clamp helpers, with their own logger, now sit in what the ticket calls "a constants class". The `GlobalProperties` name clash is gone, but a static settings parser remains.
- **Possible fix:** move the helpers to their own named class (not a constants class), or into finding 4's one read per setting.

**6. Behaviour change not mentioned in the PR description: a setting the user isn't allowed to view now raises an error instead of falling back.**
- **Why:** core's `getGlobalProperty(String)` throws `APIException` when the setting has a view privilege the user lacks (2.8.0, lines 167–174). Master's `GlobalProperties` caught that and returned the default. At head, `StockItemResource` and the service throw it to the caller.
- **Assessment:** this follows the ticket's intent, but a maintainer should know about it. I did not probe it.

**7. Minor points.**
- The job switches and the search limit read text through `getGlobalPropertyValue(key, "")`, which goes through `new String(String)` by reflection. The nullable text settings use `getGlobalProperty(key, null)` instead. `getGlobalProperty(key, "")` would be the simpler, consistent call.
- Already on master: the code defaults differ from `config.xml` (rule batch size 100 vs 1000, batch job expiry 1440 vs 10080). The PR now writes the code values at each call site without reconciling them.

## Checked and found correct (by reading)

Every setting keeps master's value when it is unset, empty or malformed:
- Numbers: Integer, Long and BigDecimal all parse the same way as master.
- Yes/no and text: the job switches (`true`/`yes`/`1`), pack size (blank means true), the optional search limit (null), and the nullable text settings.
- The three lower bounds (1, 0, 1440).

`PropertiesUtil` is an unchanged move. No `GlobalProperties` references remain. The omod imports resolve.

## Left in place
- Worktrees `work/.probe-head` (head, mutations reverted; contains the untracked `api/src/test/java/org/openmrs/module/stockmanagement/ProbeSettingsLogTest.java`) and `work/.probe-master` (`origin/master`, same untracked file).
- Logs are in `/private/tmp/claude-501/…/tasks/`: `b125bwzmv.output` (head build), `bgu3tah2i.output` (master probe), `bhxtyhqyc.output` (mutated head). macOS clears `/tmp` files after three days.
- Next run should start the context tests one at a time, not in parallel. Separately, `JAVA_TOOL_OPTIONS` points the proxy at `127.0.0.1:9`, which may be why the context takes so long to start; I didn't confirm that.
