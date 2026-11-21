# patientqueueing #41 round 11 (head 24ee29f): the README kiosk row a round-9 suggestion wrote was still incomplete

- **PR:** https://github.com/openmrs/openmrs-module-patientqueueing/pull/41, round 11, head 24ee29fa81b43241cf70656453fbd7dba61ee547.
- **What was wrong:** round 9 (2026-11-18, review 5956525317, comment 4613885928) suggested the README "Kiosk / display device account" row, and the author applied it verbatim. Two privileges are still missing.
  - `Get Visit Attribute Types`: a kiosk checking in a patient with no open visit gets 403. `saveVisit` runs `VisitValidator.validate`, and line 109 there calls `getAllVisitAttributeTypes()`, which needs that privilege.
  - `Get Concepts`: a visitor check-in sending `queueTypeUuid` gets 403 at `getConceptByUuid`.
- **Seen or not:**
  - `Get Concepts` was seen and set aside. Round 9's probes Q1/Q2 measured it, but left it out because no documented kiosk call sent `queueTypeUuid`. Round 11 docs now list that parameter.
  - `Get Visit Attribute Types` was never noticed. Round 9's probe patient (patient 2, ID 101-6) and round 11's own first probe (patient 7) both have open visits in core's standardTestDataset. `ensurePatientVisit` reused those visits, so the new-visit branch never ran.
  - A fresh-context refuter found it by choosing patient 8, who has no visits.
- **Rule that should have caught it:** none states it. The memory recipe `openmrs-privilege-table-drive-as-non-superuser` says to drive the real resource as a non-superuser, but not to vary the data state that picks a branch: open visit or none, identifier or phone, queueType given or not. Lesson: a privilege-table probe must enumerate the branches of the code path and pick test data that reaches each one. The default dataset's state silently selects one branch.
- **Session:** f7c8e2e1-8294-487e-9f64-1bc3c222a893.

# patientqueueing #41 round 12 (head 29f101b): the /checkin privilege line's conditions were never tested as conditions

- **PR:** https://github.com/openmrs/openmrs-module-patientqueueing/pull/41, round 12, head 29f101b8b79bafd7c500ad166992d6630940f9ea.
- **What was wrong:** round 11 (review 5977290287, comment 4630441759) asked to add four privileges to API_DOCUMENTATION.md line 24 and said "listing all four keeps a role built from this line working". The line's own conditions were false too, and round 11 left them standing.
  - "creating the visit additionally needs `Get Visits`, `Get Visit Types`, …" is wrong: both are needed on every check-in. See `CheckInResource:137` (visit type lookup) and `:224` (`getActiveVisitsByPatient`).
  - An explicit `provider` needs `Get Providers` (`:130`), though the line ties it to auto-assignment.
- **Seen or not:** round 11 never noticed. Its refuter granted every privilege the line names at once, so it proved the union and never withheld a conditional privilege where the condition is false. Round 12's first refuter found both defects that way: patient 2 with an open visit and auto-assign off, then a `provider` in the request with auto-assign off.
- **Rule that should have caught it:** the recipe "Privilege-table probe: list the branches…" covers branches, not conditions. Lesson: for each privilege a line makes conditional, probe the state where the condition is false without that privilege and expect success. Also probe each unconditional privilege removed one at a time.
- **Session:** c0de4167-d24e-4fc8-93b6-1f916b64c600.

# patientqueueing #41 round 13 (head efdc5d9): three earlier-round suggestions caused or missed defects an unanchored reviewer found

- **PR:** https://github.com/openmrs/openmrs-module-patientqueueing/pull/41, round 13, head efdc5d9204820acbb7d641d2cf583806e27722c2, 2026-11-20.
- **Round 12's prose premise was false, and the author's fix for it created a new docs defect.**
  - Round 12's r4633893217 said "This should name `queueType`, the property in the request body above." At 29f101b the request example had no `queueType`; only the response did.
  - The author then added `"queueType"` to the request example (docs line 176). Line 167's Requires then lacked `Get Concepts` (a checkIn-only user gets 400), and line 189's response still showed `null`.
  - Never noticed: round 12's three refuters were given the suggestion lines (s1/s2/s3) only, not the comment prose.
  - **Lesson:** a refuter gets the full comment body, prose included, and checks every factual phrase in it ("the request body above") against the file.
- **Round 8's reorder suggestion opened a commit-then-403 path.**
  - Round 8's r4551262360 had the dashboard update run the status transition before `savePatientQue`. That stopped refused transitions committing other fields.
  - But the transition (manageQueue) commits on its own. A user without sendPatientToQueue then gets 403 for a change already saved. The same shape exists in FORWARD with a comment, and in non-patient completion without manageQueue.
  - Found by this round's unanchored reviewer; confirmed with NOT_SUPPORTED probes and a second JDBC connection.
  - Never noticed: rounds 9-12 probed with the default transactional harness, which cannot show a commit, and built roles from documented rows.
  - **Lesson:** for any write path that makes more than one service call, probe each refusal with production transaction semantics (NOT_SUPPORTED, a bound session, a second connection that sees only commits). Check what was committed, not only the status code.
- **Round 9's README kiosk row over-grants.**
  - The row I suggested gives the unattended kiosk account `viewQueue`. That privilege also returns `GET /patientqueue?v=full`: every patient ever queued, with full name and birthdate, which the kiosk and display endpoints mask.
  - Never noticed: rounds 9-12 checked the row only for privileges it lacked (403s).
  - **Lesson:** for a privilege row meant for an unattended or public account, also probe what the row unlocks beyond its own endpoints.
- **Rule that should have caught these:** none in the skill. The recipes cover branch and condition probes for privilege lines, not refusal commits or over-grant.
- **Session:** 38a4e348-b2c7-4393-9e9e-70f57bfe44fc.
