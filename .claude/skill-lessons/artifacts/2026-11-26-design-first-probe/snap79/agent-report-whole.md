I found no bug that breaks behaviour in this PR. What a maintainer would object to is the tests: they don't check what the description says they check. I couldn't confirm the build or test suite, because neither Maven run reached the test phase: the machine's load average was 167–216, with 144 Maven processes from other sessions.

## Findings, most important first

**1. The quantity-format test doesn't reach any production code.** Sure (read and grep).
- `HelperFormattingTest.shouldPreserveQuantityDisplayAndHalfEvenRounding` builds `new DecimalFormat(ModuleConstants.QUANTITY_FORMAT)` inside the test itself. It is testing the JDK's `DecimalFormat`, not the module.
- None of the 9 production call sites is exercised: `StockRuleEvaluationJob` (×6), `StockBatchExpiryJob` (×2) and the two `itembatchbalancenegativeafteroperation` errors in `StockManagementServiceImpl`. Searching `api/src/test` and `omod/src/test` for those job classes and that message key finds nothing outside the two new test files.
- So if one call site formatted with a different pattern, or the raw `BigDecimal`, every test would still pass. Only the `ReportGenerator` integer cases run real code.
- I did not run a mutation probe to show this.

**2. The description says the tests were written "before swapping anything", but they weren't.** Sure (`git show --stat 804694f`).
- Both test files were created in the same commit as the swap, and `25b913e` then edited them. Nothing in the history shows they ever ran against the old helpers.
- I started that control (master, with the PR's two tests adapted to call `NumberFormatUtil`, in `./basectl`), but it was stopped at `spotless:apply` before the test phase. It has not run.
- Reading the code: the old and new versions use the same pattern strings (`#,###,###,##0.##` and `#,###,###,##0`), the same email regex, and the same null and `endsWith(".")` guards. So I expect the pins to hold, but nothing has executed that.

**3. "Same equals/hashCode semantics" for `Pair` is not accurate.** Sure for the code; nit for impact.
- The old `Pair` used `31*h1+h2` for its hash and required the exact same class for `equals`. Commons `Pair` uses `h1^h2` and is equal to any `Map.Entry` with the same key and value.
- I ran a probe with the old `Pair` source taken from `origin/master`:

  | Pairs grouped | Distinct hashes, old | Distinct hashes, commons |
  |---|---|---|
  | 20×2000 (40k keys) | 2589 | 2016 |
  | 50×5000 (250k keys) | 6519 | 5051 |

  Both collide heavily, so the hash-keyed groupings in the jobs and the service don't get meaningfully worse.
- The timings from that probe are meaningless at that load, so I'm not quoting them.
- Nothing called the old setters (`git grep setValue1|setValue2` is empty), so `ImmutablePair` is safe. `getRight().addAll(chain)` in `StockRuleEvaluationJob` changes the `ArrayList` inside the pair, not the pair itself, so it still works.

**4. The number formatting is repeated instead of shared.** Nit.
- `new DecimalFormat(ModuleConstants.QUANTITY_FORMAT).format(x)` is written out 9 times.
- `QUANTITY_FORMAT` is a new public constant in `ModuleConstants`, which other modules can now depend on, while `INTEGER_FORMAT` is private in `ReportGenerator`.
- The ticket allowed reducing `NumberFormatUtil` rather than deleting it. A single thread-safe `formatQuantity(BigDecimal)` would have fixed the thread-safety problem the description mentions without the repetition.

**5. Test placement and coverage.** Nit.
- Both tests sit in the root package `org.openmrs.module.stockmanagement`.
- `HelperFormattingTest` is named after helpers that no longer exist.
- `UserEmailAddressTest` only tests `StockManagementServiceImpl.getUserEmailAddress`.
- The long-address stack-overflow case went from 100,000 to 50,000 repetitions compared with the deleted `StringUtilsTest`.

**6. Small style points.** Nit.
- `import java.util.function.BiConsumer;` sits in the middle of the `org.openmrs` imports in all 8 processor files.
- The anonymous `BiConsumer` classes could be lambdas.
- The build's formatter (`spotless`, using the Eclipse OpenMRS formatter) has no import-order rule, so it won't fix these.

**Checked and fine:**
- The PR merges cleanly onto the current `origin/master` (`17bd512`). `getUserEmail(Integer)`, which the test overrides, still exists there.
- No references to the removed helpers remain.
- `commons-lang3` 3.18.0 is a dependency of core 2.8.0, the module's `openmrsPlatformVersion`. Before this PR the module used no `lang3` classes.

## What I did not run
- **The full build on the merged tree (JDK 8):** started, but stopped at `spotless:apply` in the api module. No tests ran.
- **JDK 11, 17 and 21 builds:** not started.
- **The PR's new tests against master (the control for finding 2):** stopped before the test phase.
- **Mutation probes** for finding 1, and for whether `UserEmailAddressTest` fails when the regex changes: not run.
- **A server:** not started. Nothing in this change is web-facing.

The `./merged`, `./basectl` and `./probe` folders inside the working directory are mine and can be deleted.
