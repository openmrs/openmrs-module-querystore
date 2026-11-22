# measurement · pr-review 0.25.1 against 0.37.0 on a private-ticket openmrs-core PR · 2026-11-22
outcome: catch-blocking C (0.25.1) 0 of 3, N (0.25.1 with one brief line pointing at the PR's earlier round notes) 3 of 3, E (0.37.0) 1 of 3, on a blocker a 0.37.0 round had posted that morning. Both E runs that filed it below blocking said the downstream reach had not been shown.
protocol: pre-registered before any arm ran; owner-directed ("run the blind replay of old vs new skill"), after the owner asked whether the posted blocker came from the recent pr-review changes. The candidates were judged blind before the key was opened.
artifacts: kept on the owner's machine only, under `confidential/artifacts/`, because the subject PR's ticket is private. They hold the protocol with results, the key, briefs, outputs, transcripts, scripts, snapshots and the blind judgments.

This is capture, not derivation.

## Subject

- An openmrs-core PR whose ticket is private (its key and number are in `confidential/PRS`), frozen at the start of the
  round that posted the blocker.
- The blocker: the PR rejects taking a voided obs out of its group, which FHIR2's current Immunization update does when it
  replaces a value. The FHIR2 change the PR depends on keeps the replaced member in the group instead. A core defect older
  than the PR (re-versioning a group copies its voided members with `previousVersion` set) then gives that member a second
  successor on the group's next re-version, such as a patient merge: a unique violation under the Hibernate-built (H2)
  schema, a forked version history under the Liquibase-built one. Master with today's FHIR2 does not hit it.
- Isolation as in the 2026-11-19 replay: a frozen GitHub mirror and frozen repos, per-run clones of core and three modules,
  no network, no memory index, default effort, no subagents. The files 0.37.0's method names under `~/.claude` (the PR's
  round record and the notes it names) were frozen as at the review moment and redirected for every arm.

## Measured

- catch-blocking: C 0/3, N 3/3, E 1/3. catch-blocking or a non-blocking catch: C 2/3, N 3/3, E 3/3.
- E's two downgrades each said the reach through FHIR2 was not shown. One ran FHIR2 updates that change the group's
  location or visit, which fail inside the test transaction on every core (master with today's FHIR2 included), and
  concluded "FHIR2's test setup cannot reach this defect on any core". The other did not run it ("Not run myself: the
  FHIR2 reach").
- The E run that blocked suspended the test transaction (`NOT_SUPPORTED`) and reached the defect through FHIR2. The N runs
  blocked on the core-level links, the removal rejected on the PR and keep-in-group then a group re-version failing on
  both cores, without an end-to-end run.
- 8 of 9 runs read the frozen round record and the notes it names, which recorded the defect from an earlier session. Two C
  runs reached them through the brief's redirect of `~/.claude` paths, which their method never names. All 8 found the
  defect; the one run that read neither did not.
- Cost $35.91; 16.6 to 46.9 min per run; the audit flagged no real violation in 544 tool calls.

## Threats

- n=3, one PR. Default effort and no subagents, where the round that posted the blocker ran at max effort with refuters and
  FHIR2 runs.
- The brief's `~/.claude` redirect leaked the notes to C (2 of 3); N's pointer is itself a framing cue.
- The key was written by the session that posted the blocker.
