I found one way the head misses "No behaviour changes": replacing the module's `Pair` with commons-lang3 `Pair` changes the iteration order of every map keyed on it. That changes which 4 expiry alerts each user gets, and the order of `totals` in the inventory REST response. I could not check "the build and all tests pass": the JDK 8 build was still compiling the API module when I stopped (load average about 200).

## Finding 1: grouping order changes with the `Pair` swap

**How sure: certain that the order changes (probe run). Highly likely that the alerts change (I read the code but did not drive the job).**

- **Why.** The old `Pair.hashCode` was `31*h(left) + h(right)`. Commons-lang3 `Pair.hashCode` is `h(left) ^ h(right)`. Equality is the same, so the groups hold the same entries, but `HashMap` iteration order follows `hashCode`.
- **The probe.** I compiled master's own `Pair.java` and commons-lang3 3.14.0, and ran the PR's exact grouping expression on (partyId, stockItemId) grids. Code is in `../probe/src/Order.java`.
  - Control, 1×1 grid: same order.
  - 1 location × 5 items: old order `[1:1, 1:2, 1:3, 1:4, 1:5]`, new order `[1:1, 1:3, 1:2, 1:5, 1:4]`.
  - 2×2 and 3×4 grids: the order differs too.
  - 10 locations × 40 items: of the first 100 groups, 70 differ.
  - In every case the set of groups is the same; only the order changes.
- **Where it matters: expiry alerts.**
  - `StockBatchExpiryJob.java:183`, `:213` (`maxAlerts = 4`) walks `partyStockItemMap` in that order. So with more than 4 expiring (location, item) pairs, users get a different set of 4 alerts.
  - For example, with one store and 5 items, item 4's batch used to get an alert and now item 5's does.
  - Those batches are added to `batchNumbersNotified`, which sets `expiryNotificationDate` (`:141`). The DAO then never selects them again (`StockManagementDao.java:1751`), so the change is persistent.
- **Where it matters: expiry email.** The email has a cap of 100 items (`:321`). Above that cap, a different subset of items is listed.
- **Where it matters: REST totals.** `StockManagementServiceImpl.java:2466` (`totalBy=LocationStockItem`) builds `totals` from the same kind of map. That list goes into the REST response through `StockInventoryPageableResult.java:40`, so clients see a different order.
- **Why the tests don't catch it.** No test touches `getTotals` or `TotalBy`, and none covers the expiry job.
- **Possible fix.** Sort explicitly before the caps and before building `totals`, or keep a key with the old hash. Since the old order was arbitrary anyway, the reviewers might also just accept the change and say so in the PR.

## Checked and found no change

- **Merge with current master.** It merges cleanly (`merged/` worktree, commit ff87440). After the merge there are no references to `NumberFormatUtil`, `utils.Pair`, `utils.StringUtils` or `Action2`, and `ModuleConstants.QUANTITY_FORMAT` survives the dead-code removal in #82. This was a grep, not a build.
- **Email check.** It uses the same regex as master, with the same null and trailing-dot checks (`StockManagementServiceImpl.java:3227`).
- **Number formatting.** The patterns are identical and `new DecimalFormat` uses the same default locale and rounding as before (read only, not run).
- **Mutability.** `Pair.of` returns an immutable pair. No call site calls a setter; the only mutation is `getRight().addAll(...)` on a mutable `ArrayList` (`StockRuleEvaluationJob.java:483`), which still works.
- **Formatting.** The build's `spotless:apply` and `license:format` steps left the head's tree unchanged (`git status` was clean apart from my worktree).

## Not run

- **Head build and tests on JDK 8:** started, still compiling the API module when I stopped. `../build-head-j8.log` has no result. I left the build running (Maven pid 53558), and stopping it is up to you.
- **Build of head merged with master:** never started, because the waiting script was killed before it reached that step.
- **JDK 11, 17 and 21 builds:** not started.
- **The expiry job and the REST endpoint themselves:** not run. The ordering evidence comes from the probe of the grouping expression plus reading the code, not from a server. I started no server.
- **Blind-agent refutation:** not run.

The JDK 8 build of the head hit its background time limit and was stopped before giving any result. The build of head merged with master never started, so neither has a result. The Maven process I mentioned in my report is gone, so there is nothing left to stop.

So "the build and all tests pass" is still unchecked on every JDK (8, 11, 17, 21), for both the head and the head merged with master. I'm not restarting it because the 75-minute budget is used up. On this machine (load average about 200), the head build spent over 18 minutes compiling the API module without finishing. A rerun needs a less loaded machine, or a much longer wait with the merged build in its own job.

The `Pair` ordering finding stands as reported. It rests on the probe and on reading the code, not on the build.
