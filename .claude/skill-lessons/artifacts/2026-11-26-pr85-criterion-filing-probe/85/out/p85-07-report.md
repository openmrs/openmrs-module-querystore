I'm filing one of the nine: R7, as a non-blocking suggestion. The other eight are set aside. I changed no files and `git status --porcelain` is clean.

**Filed**
- **R7** goes on `StockManagementServiceImpl.java:2907`, which sits inside the PR's hunk at 2902–2914. `isValidEmail` returns false and the address is set to null without a log. If the role setting is also blank, the only log line is an INFO saying the email and role settings are "not set", which is wrong. This is the ticket's "should be logged, not silently ignored" case, on a value the code already rejects. Master behaves the same, so under the same-defect rule it is a suggestion, not a blocker. The fix needs a `StringUtils.isNotBlank` guard ahead of the warning. Without it, an install that never sets this property (its `config.xml` entry has no default) would warn on every notification. The method wants a blind check of this fix before it is shown, but this brief rules out starting agents, so that check is still owed.

**Set aside**
- **R2:** no shipped setup reaches it. Core 2.8.0's data (`liquibase-core-data-2.7.x.xml`, around line 5472) gives "Get Global Properties" to the Authenticated role. No later update in the 2.8.0 jar touches that privilege; the grep found it ten times in the 2.0.x update, so the search works. `UserContext.getAllRoles` adds the Authenticated role for every logged-in user, and no stockmanagement setting declares its own view privilege. On master, a user stripped of that privilege silently got the default instead of the configured value. That is the kind of silent fallback the ticket's second criterion removes.
- **R9:** the formatter is the Eclipse `OpenMRSFormatter.xml` through spotless (parent POM 2.3.0), and it doesn't sort imports, so the move was done by hand. But it is one line inside an import hunk the PR has to edit anyway, so it adds no real review cost.
- **R1:** the six helpers read no setting themselves (`ModuleConstants.java:71-131` has no `Context` call). They hold rules core doesn't have, and the PR description says where they live. The only alternative is moving them to another class, which has no cost anyone can name.
- **R3:** the ticket explicitly allows "the default stated at the call site".
- **R4:** the ticket prescribes `getGlobalPropertyValue(key, defaultValue)`, and `getGlobalProperty(key, "")` returns the same result.
- **R5, R6, R8:** pre-existing behaviour or core's own message; each reason is in the JSON below.

```json
{"filed": [{"item": "R7", "disposition": "suggestion", "first_line": "Could this warn with the key and the value before dropping the address? With stockOperationNotificationEmail=not-an-email the notification email is silently skipped, and if the role is blank the only log line says the settings are not set."}],
 "set_aside": [{"item": "R1", "reason": "The six helpers read no setting and only hold value rules core lacks (yes/1 switches, blank-means-true, master's existing minimums), the PR description discloses them, and the only alternative, moving them to another class, has no cost to name, so the GlobalProperties utility the criterion targets is gone."},
 {"item": "R2", "reason": "Core 2.8.0's data grants Get Global Properties to the Authenticated role, UserContext.getAllRoles adds that role for every logged-in user and no module setting declares a view privilege, so no shipped setup reaches it, and master's fallback there was itself a silent swallow the ticket's second criterion removes."},
 {"item": "R3", "reason": "The ticket explicitly allows the default to be stated at the call site, so the repeated literals follow a recorded decision, and finding A already covers the missing test that would catch drift."},
 {"item": "R4", "reason": "The ticket prescribes getGlobalPropertyValue(key, defaultValue), and getGlobalProperty(key, \"\") gives the same result, so the difference is taste."},
 {"item": "R5", "reason": "The mismatch predates the PR, and reconciling it would change a code default, against the ticket's criterion that each setting keeps its current default."},
 {"item": "R6", "reason": "These values parse, so no exception is swallowed, master has no bound for them, and adding new bounds would be new behaviour beyond the ticket rather than logging an existing rejection."},
 {"item": "R8", "reason": "The message is core's, the ticket prescribes core's method, and the logged stack trace names the module call site, so a fix belongs in core or would need a wrapper like the utility the ticket removes."},
 {"item": "R9", "reason": "The one-line move sits inside an import hunk the PR must edit anyway and leaves the block sorted, so it adds no review cost the extra-hunk rule exists to prevent."}]}
```
