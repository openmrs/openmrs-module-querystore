I'm filing R1 as a question and leading the review with it. A, C and E go out as drafted, B is held, and R2–R9 are set aside.

**Is it right for the ticket?** Mostly. Settings are now read through `AdministrationService`, and a value core can't parse is logged. But `ModuleConstants.java:68-131` now has a logger and six public static helpers, and the ticket's first criterion is "No static settings utility remains". I read the ticket's purpose as met (the name clash is gone, reads go through core, and the helpers log), so the method says to ask rather than block.

**Is it right for the plan?** Yes. O3-6306 (StockManagement v4.0) is a cleanup pass. This ticket blocks the per-domain service tickets O3-6316 to O3-6323, and nothing in the PR works against that.

**What decided each item**
- **The method names this exact case.** Its Step 2 says that on stockmanagement #85, a request that two settings warn "the way parseUomPriority does" was answered with one more static helper. That request is draft B. So once R1 is asked, B is held, along with R3 and R4, which also add to the part in question. A, C and E stay because each still applies however R1 is settled. C and E don't add helpers, and E would remove `parseOptionalInteger`.
- **R2 can't reach a normal user.** Core's shipped data gives "Get Global Properties" to the Authenticated role: `liquibase-core-data-2.7.x.xml:5472-5474` inside `openmrs-api-2.8.0.jar`, and the 2.0.x update script copies it to every role that has "View Global Properties". c3's failing case needed a logged-out session with proxy privileges, and I can't name a real caller that hits it.
- **E is confirmed.** `StockItemResource.java:166-170` passes a nullable `Integer` into `searchStockItemCommonName(..., int maxItems)`, which crashes on null (`StockManagementService.java:148`). `config.xml` defaults this setting to 50.
- **C is confirmed.** The read is inside the `forEach` at `StockConsumptionReport.java:59-62`, and `preWriteBuffer` runs once per buffer (`bufferRecordCount = 100` at `StockItemInventoryReport.java:197`).
- **R5 is confirmed and set aside.** `config.xml` has 1000 for the rule batch size and 10080 for the batch job expiry.
- **The comments sit on changed lines.** C's anchor is inside hunk `+57,8` and E's inside hunk `+166,3`.
- I changed no files and didn't build anything. `git status --porcelain` is empty.

**Still owed:** B, and R3 and R4 as follow-ups, once R1's direction is settled.

{"filed": [{"item": "R1", "disposition": "question", "first_line": "Does this still meet the ticket's first acceptance criterion, \"No static settings utility remains\"? ModuleConstants now has a logger and six public static helpers that each take a setting's key and value and return the value to use, so settling whether those rules stay decides how every read in this PR is written."}],
 "set_aside": [{"item": "R2", "reason": "Core's shipped data gives Get Global Properties to the Authenticated role (liquibase-core-data-2.7.x.xml:5472-5474 in openmrs-api 2.8.0), so no logged-in user hits it, and showing the error instead of swallowing it is what the ticket asks for."},
  {"item": "R3", "reason": "The ticket allows the default at the call site, and the fix (one read per setting) is the shape R1's question asks the ticket to decide, so it is held until that is settled."},
  {"item": "R4", "reason": "Both calls behave the same, so it is a nit with no failure mode, held while R1 has the approach in question; E would also remove one of the three reads."},
  {"item": "R5", "reason": "It is the same on master, and reconciling config.xml with the code would change a default, against the ticket's criterion that each setting keeps its current default."},
  {"item": "R6", "reason": "It is the same on master, an in-range parse throws nothing to swallow, and asking for more range warnings is the request the method records as getting one more static helper."},
  {"item": "R7", "reason": "It is on a line the PR does not change, behaves the same on master, and is an email check rather than a setting that failed to parse."},
  {"item": "R8", "reason": "The message is core's, and naming the key from the module would mean wrapping every read in a helper, the shape R1 questions."},
  {"item": "R9", "reason": "It is a one-line sort inside the import block the PR has to edit anyway to add ModuleConstants, so it costs a reviewer nothing, and line-level nits are held while the approach is in question."}],
 "drafts": [{"draft": "A", "decision": "as_drafted", "first_line": "Can this class go?", "why": "It is verified, its point holds however R1 is answered, and it shows the defaults criterion has no guard at all."},
  {"draft": "B", "decision": "held", "first_line": "", "why": "It asks for more parse-and-warn helpers, the request the method says was answered with one more static helper against the ticket's first criterion, so it waits for R1's answer."},
  {"draft": "C", "decision": "as_drafted", "first_line": "Could this be read once, before the forEach?", "why": "It is a regression this PR adds (an ERROR per row where master logged none), and reading once before the loop is right whatever form the read takes after R1."},
  {"draft": "E", "decision": "as_drafted", "first_line": "Could this fall back to 50, the config.xml default, instead of null?", "why": "A blank or malformed value crashes the search on a line this PR rewrites, and the fix removes a static helper instead of adding one."}],
 "report_opening": "I'm filing R1 as a question and leading the review with it. A, C and E go out as drafted, B is held, and R2–R9 are set aside.\nIs it right for the ticket? Mostly. Settings are now read through AdministrationService and a value core can't parse is logged, but ModuleConstants keeps six static settings helpers against the ticket's \"No static settings utility remains\", and I ask about that rather than block on it.\nIs it right for the plan? Yes. O3-6306 (StockManagement v4.0) is a cleanup pass, this ticket blocks the per-domain service tickets O3-6316 to O3-6323, and nothing in the PR works against that."}
