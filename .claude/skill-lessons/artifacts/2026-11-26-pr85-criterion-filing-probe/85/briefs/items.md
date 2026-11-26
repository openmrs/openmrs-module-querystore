- R1: The criterion agent and the whole-PR agent each report that the head misses the ticket's first acceptance
  criterion, "No static settings utility remains", by its letter: `ModuleConstants` now has a logger and six public
  static helpers (`parseOptionalInteger`, `parseJobEnabled`, `parseUomPriority`, `atLeast`, and two `positiveValue`
  overloads), each taking a setting's key and value and returning the value to use (c14, section 1; whole, finding 5).
- R2: A caller without core's "Get Global Properties" privilege now gets an `APIAuthenticationException` where
  master's getters returned the default (c3; whole, finding 6).
- R3: The defaults are copied across call sites: `1000` twelve times in six reports, `30.5` six times, and the
  pack-size read five times in the service (whole, finding 4).
- R4: The job switches and the search limit read text with `getGlobalPropertyValue(key, "")`, while the nullable text
  settings use `getGlobalProperty(key, null)`; `getGlobalProperty(key, "")` would be simpler and consistent (whole,
  finding 7).
- R5: The code defaults differ from `config.xml`'s (rule batch size 100 against 1000, batch job expiry 1440 against
  10080), as on master (whole, finding 7).
- R6: Three numeric settings take out-of-range values without a log, as on master: a negative or overflowing upload
  size, a negative excess threshold, and a search limit of 0 or -5 (c2, findings 2, 4 and 5).
- R7: An invalid notification email is set to null without a log at `StockManagementServiceImpl.java:2907-2908`, a
  line the PR does not change, as on master (c2, finding 3).
- R8: Core's parse error names the value but not the setting's key (c2, finding 6).
- R9: One `Context` import moved within the import block the PR already edits in `StockManagementServiceImpl` (your own
  pass).
