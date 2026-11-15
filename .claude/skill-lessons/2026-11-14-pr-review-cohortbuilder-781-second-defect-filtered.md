# defect note · pr-review filtered a verified second defect as a "second consequence" · openmrs-esm-cohortbuilder-app PR 781 at 4cfb4ce · 2026-11-14
outcome: partial miss. The review posted two blocking findings (main's #790 wording breaks three tests on the merge; the history-position race) and two optional ones. Dennis (denniskigen) then added three comments it should have carried: the rollback deletes by position too and needs the same change, with `composeQueries` from #782 as the fix; the "search from a tab lands in between" test asserts only descriptions; and a uuid that names nothing has its 404 swallowed while the search still runs.
written by: hand, in a later session (7d92a8d0), after the user asked why the review missed it
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-openmrs-module-chartsearchai/278e416a-5364-4d73-b9ed-64303da0e094.jsonl

## Measured (session 7d92a8d0, against 4cfb4ce)

- Rollback: four race tests through the real `CohortBuilder`. History cleared or an earlier entry deleted mid-run makes the AND send the enrollment filter twice. A clear plus a manual search, then a failure, deletes the user's search. A delete, then a failure, leaves the link's demographics entry behind. `composeQueries` alone fixes the first two and leaves both rollback races failing; adding a per-run key stored by `addToHistory` fixes all four, suite 310/310.
- Test: composing positions `startLength + i + 1` (wrong only when a tab search lands in between) leaves the whole suite green, 306/306. Dennis's assertion on the composition body reddens it and passes unmutated.
- 404: local OpenMRS, reportingrest 2.0.0, the PR's own request bodies. A missing program returns 201 with 0 rows, described "Patients enrolled in 1 program". PEP/PrEP plus a missing program returns 3100, the same as PEP/PrEP alone (control: PEP/PrEP plus Diabetes 3499). A missing encounter type is a 500 (NPE). Drugs unmeasured: neither local server has a drug order search definition.

## Why the review missed it

On record:
- 10:36:16 its refutation agent reported P3 (a delete, then a failure, leaves half the link) and P4 (a clear, a user search, then a failure: the rollback deletes the user's entry).
- 10:36:26 "My reply stays on the composition case alone, since one consequence makes the case." That applies SKILL.md's "a second consequence where one already makes the case ... stay[s] out of the comment" to a defect with its own fix, which the same paragraph's "A second ask gets its own comment if it stands as a finding" covers. The PR's own guarantee names both halves: "Only the history entries a link added are composed or rolled back". The terminal report carried P3/P4; the PR did not.
- #782 appears only in the ticket text fetched at 09:58:52; no tool call opened it, so the blocking reply named no fix although one existed a PR up the stack.
- No mutation touched `addedEntries` or `createCompositionQuery`; the coverage mutations went to the drug key and the error-once effects.
- `url-definition.resources.ts` was read once, as diff, at 09:59:03 ("Best effort: anything that fails to load is left out"). "404" never appears. No request reached an OpenMRS server, only Jira; inside the PR's mocks a failed lookup is followed by a search by design (`use-url-definition.test.tsx:144` pins "1 drug").

## Candidate lesson (one run, not corroborated)

The review saw the item, then filtered it out through one of its own scoping rules. Here: a consequence that needs its own fix is a second finding, not a second consequence; the brevity rule may drop only consequences the same fix resolves.

Corrected 2026-11-15 after the retro's gate: this paragraph first claimed a "third run with this shape", counting #53's replay. #53's run never noticed its defect (`2026-11-04-pr-review-stockmanagement-53-missed-input.md`:15), and a replay is a measurement, which REJECTED.md:5595-5596 says has never counted as corroboration.

One run each: open the rest of a PR's stack before posting a blocker without a fix; point coverage mutations at the code of the review's own blocking findings; where the PR's tests mock the server, send one real request per new server-facing input.
