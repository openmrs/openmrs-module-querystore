Undecidable on "the build and all tests pass": no build finished. The machine was at load average ~200 and nearly out of memory, so neither of my Maven runs got past startup. On "no static settings utility remains", the head misses the letter of it: `ModuleConstants` is now a small static settings utility. It does fix most of what the ticket's Why section objects to.

## 1. "No static settings utility remains"

**Missed by the letter.** `api/src/main/java/org/openmrs/module/stockmanagement/api/ModuleConstants.java:68-131` (I read the file at the head) now has a logger and six `public static` methods. Each one takes a setting's key and value and returns the value to use:
- `parseOptionalInteger`
- `parseJobEnabled`
- `parseUomPriority`
- `atLeast`
- two `positiveValue` overloads

Nine call sites go through them, for example `StockRuleEvaluationJob.java:69-79`, `StockBatchExpiryJob.java:74-87` and six report classes. So the static utility was shrunk and moved into what the ticket calls "a constants class". Master has `GlobalProperties` with 22 static methods; the head has six static helpers in `ModuleConstants`.

Against the Why section, one objection at a time:
- **Name clash:** gone.
- **Duplicating core:** reads now go through `AdministrationService`. The helpers keep rules core doesn't have (yes/1 switches, blank means true, lower bounds).
- **Failing silently:** the helpers log. But one silent path is left, through core rather than the helper:
  - `negativeStockBalanceAllowed` is read at `StockManagementServiceImpl.java:1927` and `allowStockIssueWithoutRequisition` at `:1246`, both with `getGlobalPropertyValue(key, false)`.
  - Core 2.8.0 (`AdministrationServiceImpl.java:740`) turns the value into a Boolean with `new Boolean(propVal)`, which never throws. So `yes`, `1` or `invalid` silently become `false` and nothing is logged.
  - The PR's own test locks this in (`GlobalPropertyDefaultsTest.java:126-133`, which expects `Boolean.parseBoolean` behaviour for those values).
  - Master does the same thing (`Boolean.parseBoolean` inside a swallowing try/catch). So the behaviour isn't new, but these lines are rewritten by the PR and the ticket's Why section names exactly this case.
  - This comes from reading core's source and the test. I did not run it.

## 2. "The build and all tests pass": not run

- **JDK 8:** I ran the CI build (`JAVA_HOME=…temurin-8.jdk… CI=true mvn -o --batch-mode clean install -Dmaven.deploy.skip`) in `scratch/wt8` twice. The first attempt was killed when the shell's 10-minute limit ran out. The second ran about 9 minutes and stayed at the root pom (13 log lines). A thread dump showed `main` RUNNABLE in `PluginDescriptorBuilder.buildComponentDescriptor` at 3–5% CPU. So it wasn't hung, just starved of CPU by other sessions' builds. I stopped it at the time limit.
- **JDK 11, 17 and 21:** not run.
- **Test order, shared state and logging configuration:** I reasoned about these but ran nothing.
  - Core's test `log4j2.xml` sets `org.openmrs.api` and the root logger to `OFF`.
  - Both new log-capture tests rely on `core.Logger.addAppender` plus `setLevel` in `@BeforeEach`. That level holds only until the next `LoggerContext.updateLoggers()`.
  - In 2.8.0 only the `log.level`, `log.layout` and `log.location` setting listeners trigger that (`LoggingConfigurationGlobalPropertyListener.java`), and no module test saves those settings.
  - So I found no ordering that breaks them. This is a lead, not a result.
- **Core version:** `require_version` is 2.8.0, and `getGlobalPropertyValue` is the same in core 2.6.0, 2.7.0 and 2.8.0. That rules out a dependence on the core version.

## Tests that pass whatever the defaults are

No test runs any rewritten call site. A search of `api/src/test` and `omod/src/test` for the setting constants finds them only in `GlobalPropertyDefaultsTest` and `ModuleConstantsTest`:
- `GlobalPropertyDefaultsTest.java:86-91` passes the defaults as literals straight to core.
- `:136-142` re-implements the lower bounds with `Math.max` instead of calling `atLeast`.

So a wrong default at a call site would not turn any test red. That leaves "each setting keeps its current default" unguarded. To show it, I changed four call sites in `scratch/wt11`:
- the reporting page size in `DispensingLogsReport` from 1000 to 999
- the rule job batch size from 100 to 50 and its minimum from 1 to 0
- the excess threshold from 10 to 11
- the batch job expiry default from 1440 to 60

I started the api tests on JDK 17 and then stopped them, so that run is **not run**. My expectation that it would stay green rests on the search above, not on test output.

## Left in place
- Worktrees `work/scratch/wt8` (clean head), `work/scratch/wt21` (clean head, unused) and `work/scratch/wt11` (head plus the uncommitted mutations above).
- Logs `work/scratch/build8.log` and `work/scratch/mut17.log`, both partial.
- None of my Maven processes are still running.

To settle criterion 2, the next step is to rerun the CI command in `wt8` and `wt21` (and then JDK 11 and 17) once the machine is quiet, plus the api tests in `wt11` for the mutation check.
