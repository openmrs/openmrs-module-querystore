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
