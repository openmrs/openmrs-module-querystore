Every setting keeps its default for every stored value I tried. The one exception: when the caller lacks core's "Get Global Properties" privilege, master falls back to the default but head throws `APIAuthenticationException`.

**How I compared.** I ran the same test on both refs (JDK 17, `-pl api`). It covers all 19 settings the module reads and 51 stored values per setting, 969 rows in all. Master's side calls the old `GlobalProperties` getters. Head's side runs the call-site expressions copied verbatim from the head code. The stored values were:
- unset, `""`, `" "`, a tab
- `abc`, `" 42 "`, `42 `
- `0`, `-0`, `-1`, `-5`, `1`
- `1439`, `1440`, `1441`
- `2147483647`, `2147483648`, `-2147483649`, a 20-digit number
- `1e3`, `1E+2`, `10.5`, `0.0001`, `-0.5`, `+7`, `0x10`, `007`, `NaN`, `Infinity`, Arabic-Indic digits
- `true`, `TRUE`, `TrUe`, `" true"`, `yes`, `1`, `no`, `false`, `0 `, `y`, `on`
- `"  first,second  "`

Values were written directly onto the stored property, so core's whitespace trimming on save doesn't hide anything. The harness does reach the stored value: `42` gives 42 while `" 42 "` gives the default. Every row matched on value and type.

## Defect: a missing privilege now raises an exception instead of the default

- **Where:** every head call site that reads a setting, for example `api/src/main/java/org/openmrs/module/stockmanagement/api/impl/StockManagementServiceImpl.java:2105` (pack-size priority in `dispenseStockItems`), `:1246` (allow issue without requisition) and `:1514` (excess threshold).
- **Cause:** all three core methods the PR now uses are marked `@Authorized(GET_GLOBAL_PROPERTIES)` in `AdministrationService.java` (lines 98, 115, 328 at 2.8.0). So is the plain `getGlobalProperty(key)` that master used. Master's getters caught the resulting `APIAuthenticationException` and returned the default; head lets it through. The same happens for a property with its own view privilege (`AdministrationServiceImpl.java:172`).
- **Same input on both refs:** the "LOGGEDOUT" part of the comparison test (`SettingCompareTest`) logs out and reads all 19 settings.
  - Master returned every default (`searchMax null`, `excessThreshold BigDecimal:10`, `uomBigToSmall Boolean:true`, …).
  - Head returned `EXC:APIAuthenticationException` for all 19.
  - `diff master.tsv head.tsv` differs only in those 19 lines.
- **Through a real service method:** `DispenseNoGpPrivilegeTest` logs out, adds only the privileges "Task: stockmanagement.stockItems.dispense" and "Get Locations", then calls `dispenseStockItems` with one empty request.
  - Master printed `StockManagementException: The location where your dispensing drugs is required` (it got past the setting read with the default).
  - Head printed `org.openmrs.api.APIAuthenticationException: error.privilegesRequired`.
  - So a user who holds the stock privileges but not "Get Global Properties" can dispense, submit operations and search stock items on master, and is refused on head.
- **Commands:** `JAVA_HOME=…temurin-17.jdk… CI=true mvn -o --batch-mode install -pl api -Dtest=<Class> -Dsurefire.failIfNoSpecifiedTests=false -DargLine="-DcmpOut=<file>"`. Each test reported `Tests run: 1, Failures: 0`. Master's build then failed only on the license-header check of my scratch test files, after the tests had run.

**Not a difference:** if the search-limit setting is malformed, `StockItemResource.java:173` unboxes a null and fails. Master does the same and the PR doesn't rewrite that line.

## Not run
- Running a server and calling the REST endpoints (`StockItemResource`, `StockItemImportController`, which reads the upload-size limit) end to end. I didn't start one; the omod call sites were checked only through their copied expressions.
- Whether real deployments' stock roles lack "Get Global Properties". This decides how often the defect bites, and **needs access** to a deployment's role setup.
- JDK 8, 11 and 21, and the full CI build. Only JDK 17 with `-pl api` was run.
- Running the jobs and reports themselves. Their reads were covered only by the copied expressions, and scheduled jobs normally run as the daemon user, which has every privilege.

## Files left in place
- `.scratch/master/`: a git worktree of `origin/master`, holding my two test files, untracked.
- `.scratch/gen/`: the test sources, `master.tsv`, `head.tsv`, `master-dispense.txt`, `head-dispense.txt` and the Maven logs.
- `api/src/test/java/org/openmrs/module/stockmanagement/api/SettingCompareTest.java` and `DispenseNoGpPrivilegeTest.java` in the head checkout, untracked. No tracked file changed.
