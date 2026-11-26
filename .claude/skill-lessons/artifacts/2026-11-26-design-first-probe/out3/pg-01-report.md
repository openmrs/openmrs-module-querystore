I'd file six comments and none of them blocks merge. A, C and E go out, with B and C each gaining one link. R1 and R2 are new questions. The other seven items are set aside.

**R2 is narrower than the agent reported.** Core's default data gives "Get Global Properties" to the Authenticated role (`liquibase-core-data-2.7.x.xml`, inside openmrs-api 2.8.0). `UserContext.hasPrivilege` checks that role, so every logged-in user still passes these reads. The c3 agent got its dispense refusal because it ran with no one logged in.

The one place it does bite is the notification thread. `StockOperationNotificationTask` calls `Context.openSession()`, which starts a session with no user, and it does not add "Get Global Properties" to its proxy privileges. I wrote a throwaway test that calls the real `sendStockOperationNotification` at the head:

| Case | Result | Alert created |
|---|---|---|
| No user, the task's own privileges | `APIAuthenticationException` at `StockManagementServiceImpl.java:2906` | no |
| Same, plus "Get Global Properties" | returns normally | yes |
| Admin | returns normally | yes |
| No user, unknown operation (control) | returns normally | no |

On master, `GlobalProperties` caught that failure and returned null, which ends at the "settings not set" return. That is from reading the code, not running it: setting up a master copy needed approval I didn't have. So that thread probably sent nothing on master either, and now it logs an error. That makes R2 a question for the author, not a regression. The same finding sets R7 aside: line 2907 sits after the read that now throws, and the task is its only caller.

**R1 is a question** because the head misses "No static settings utility remains" by its words, while the ticket's purpose looks met: the name clash is gone and the reads go through core. Whether the six helpers in `ModuleConstants` are acceptable is for the ticket's author to say.

**Set aside:**
- **R3:** the ticket allows "the default stated at the call site", and draft A already covers the defaults that no test checks.
- **R4:** core's bytecode shows both calls return the same value, and `getGlobalPropertyValue` is the method the ticket asks for.
- **R5 and R6:** they match master, and changing a default or adding a bound would break the "each setting keeps its current default" criterion.
- **R8:** core logs its message together with the stack trace, which points at the module's line that read the setting.
- **R9:** one import moved inside an import block the PR has to edit anyway, and the formatter doesn't enforce import order.

**Drafts:**
- **A and E go out as drafted.** E's suggestion block has to be anchored on lines 166–168, not just 168, or it replaces only one of the three lines.
- **B and C each gain one link to core's code.** The trigger is the pre-posting rule "with what it rests on from outside the PR linked where it is named". The link points at core's `AdministrationServiceImpl.java#L730-L751`; I confirmed those line numbers from the 2.8.0 jar. Whether the git tag is literally `2.8.0` **needs access**, because the network is closed.

**Not run:**
- The full build and tests on any JDK, so the criterion "The build and all tests pass" is still undecided.
- A blind reader for R2's comment, which mentions a measured fix (adding the privilege), so it goes out only as a question. No agent could be started here.
- The PR's round record, which the constraints kept me from reading or writing.

The branch is one commit behind master (17bd512), and that commit touches no file in the PR.

I removed the probe test, the `api/target/` it created, and my `/tmp` scratch folder; `git status --porcelain` is empty.

{"filed": [{"item": "R1", "disposition": "question", "first_line": "Does this meet [O3-6352](https://openmrs.atlassian.net/browse/O3-6352)'s first criterion, \"No static settings utility remains\"?"}, {"item": "R2", "disposition": "question", "first_line": "Was this meant to start throwing in StockOperationNotificationTask?"}], "set_aside": [{"item": "R3", "reason": "The ticket explicitly allows \"the default stated at the call site\", so the copies are the shape it chose, and draft A already covers the risk that no test checks those defaults."}, {"item": "R4", "reason": "Core's bytecode shows getGlobalPropertyValue(key, \"\") and getGlobalProperty(key, \"\") return the same value for unset, empty and set values, and the typed method is the one the ticket prescribes, so there is no cost to name."}, {"item": "R5", "reason": "The mismatch is the same on master, and reconciling the code defaults with config.xml would break the criterion \"Each setting keeps its current default\"."}, {"item": "R6", "reason": "Those values parse and master uses them unchanged; the PR keeps exactly master's existing bounds, and adding new ones would change behaviour beyond the ticket."}, {"item": "R7", "reason": "Line 2907 is unchanged and same as master, and its only caller, StockOperationNotificationTask, never reaches it at the head because the read at 2906 throws first (measured), so it waits on the answer to R2."}, {"item": "R8", "reason": "The message is core's, it is logged with a stack trace that points at the module's line that read the setting, and a fix belongs in core, not this PR."}, {"item": "R9", "reason": "It is one line inside an import hunk the PR must change anyway to add ModuleConstants, no formatter enforces import order, and it costs a reviewer nothing."}], "drafts": [{"draft": "A", "decision": "as_drafted", "first_line": "Can this class go?", "why": "Two blind readers passed it, nothing since contradicts it, and it rests only on the PR's own test code."}, {"draft": "B", "decision": "reworded", "first_line": "Should this and negativeStockBalanceAllowed warn on values they cannot parse, the way parseUomPriority does?", "why": "The pre-posting gate requires what it rests on from outside the PR to be linked where it is named, so \"Core reads them\" now links core's AdministrationServiceImpl lines 730-751; nothing else changes."}, {"draft": "C", "decision": "reworded", "first_line": "Could this be read once, before the forEach?", "why": "Same gate rule: \"core log an ERROR with a stack trace\" now links core's AdministrationServiceImpl lines 730-751; the text and the formatted fix are unchanged."}, {"draft": "E", "decision": "as_drafted", "first_line": "Could this fall back to 50, the config.xml default, instead of null?", "why": "Its claims re-checked at the head (config.xml says 50, searchStockItemCommonName takes an int), but the suggestion block must be anchored on lines 166-168, not just 168."}], "report_opening": "Right fix for O3-6352, with one criterion in question: settings now go through core's AdministrationService and GlobalProperties is gone, but ModuleConstants keeps six static parse helpers against \"No static settings utility remains\".\nRight for O3-6306 (StockManagement v4.0): it clears the way for the per-domain service tickets O3-6316 to O3-6323 that it blocks, and adds nothing they would have to undo.\nNeeds action before merge: none blocking; six comments ready (A and E as drafted, B and C with a link added, plus new questions R1 and R2), nothing posted."}
