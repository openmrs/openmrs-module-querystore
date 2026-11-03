You are verifying code-review findings against real code. Several independent reviews of the same
pull request produced the findings in the input file. You do not know how any review was produced,
and it does not matter: judge each finding only against the code.

## Inputs

    /private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-queue/5743c726-b776-450f-a755-a07953eaf17e/scratchpad/adj-6551-blind/adjudication-input.json

Each entry has an opaque `key` (e.g. `F007`), plus `file`, `line`, `where`, `disposition`, `claim`,
`failure_mode`, `evidence`. The entries are shuffled; their order carries no information, and any
grouping you might infer from it is not real.

## The code

`openmrs/openmrs-core` PR 6551 at head `b7cd909d245086fa10691496f88e5ec6971b77c2`; base `master` =
`28e3bab9e206170df7fd43cf709495b6d45fe16a`. The PR diff is exactly `git diff master...HEAD` (8 files,
+216/-6). Your own clone, at the head (shallow, 382 commits):

    /private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-queue/5743c726-b776-450f-a755-a07953eaf17e/scratchpad/adj-6551-blind/wt-adj

- Its `.mvn/maven.config` points Maven at a private repository; leave it alone. JDK 21 (the machine
  default), offline (`-o`). A narrow run:
  `mvn -o -B -pl api -am test -Dtest='LocationServiceTest#purgeLocation_shouldDeleteLocationAssignedToAUser' -Dsurefire.failIfNoSpecifiedTests=false`
  takes about 70 s; the log4j `MEMORY_APPENDER` errors at test start are harmless.
- Do not read the live PR 6551 or ticket TRUNK-6669, anything under `/Users/danielkayiwa/Projects` or
  `/Users/danielkayiwa/.claude` (except a file the harness says it saved your own tool output to), or
  anything under
  `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-queue/5743c726-b776-450f-a755-a07953eaf17e/scratchpad`
  outside `.../scratchpad/adj-6551-blind`. Each would show you what others concluded or how the reviews
  were run; your job is to reach your own conclusion from the code.
- Do not spawn subagents. Restore any mutation with `git checkout -- <path>` and leave the clone clean.

## Defects already known to exist at this head

Each was established by an earlier, independent adjudication at this sha. Treat these as given.

| key | defect | severity |
|---|---|---|
| L01 | `purgeLocation_shouldDeleteLocationAssignedToAUser` passes with the `user_location` cleanup deleted: it reads through the session and nothing flushes, so the foreign key is never evaluated | blocking |
| L02 | the cleanup deletes `user_location` rows only for the purged location's own id, but `session.remove(location)` cascades to `Location.childLocations`, so purging a parent whose descendant is assigned to a user still hits the `user_location` foreign key | blocking |
| L03 | with the fix in place, a flush while a persistent User whose `locations` contains the purged Location is in the session throws `TransientPropertyValueException` (the session becomes unflushable), not merely stale data; the added comment understates this | suggestion or blocking |
| L04 | `getAllowedLocations` is gated only by Get Locations (granted to Authenticated by default) and reads the target user with `dao.getUser(...)`, bypassing `UserService.getUser`'s privilege, so any authenticated user can read any other user's location assignments | blocking |
| L05 | `User.getLocations()` throws `LazyInitializationException` on a detached User (including `Context.getAuthenticatedUser()`), with no test; `roles`, the sibling it is modelled on, is EAGER | suggestion |
| L06 | `@Independent` on `User.locations` is load-bearing and untested, while the repo already tests both other `@Independent` fields | suggestion |
| L07 | the tag lookup uses `getLocationsByTag` (loads every non-retired location and its tags, filters in Java) where `LocationService.getLocations(LocationSearchCriteria)` does tag matching in SQL | suggestion |
| L08 | the re-read `dao.getUser(user.getUserId())` is pinned by nothing (all new tests pass with the caller's object instead) and its comment misdescribes what happens for a session-attached user | suggestion or nit |
| L09 | `User.locations` is a lazy `@ManyToMany` with no `@BatchSize`, so initialising it across n users costs n queries; Location's collections carry `@BatchSize(size = 100)` | suggestion |
| L10 | an empty set means unrestricted, so any path that empties a user's set (purging a location does) silently makes a restricted user unrestricted, with no trace, and nothing documents it | suggestion |
| L11 | `LOCATION_ID` serves two namespaces (a JPA attribute name and a native-SQL parameter name) and the extraction reaches methods the ticket does not touch | nit |
| L12 | nothing enforces the restriction (it filters a picker), while the PR title and javadoc read as enforcement | question |
| L13 | the collection has no add/remove mutators, and `setLocations(null)` restores null, so the "empty set means unrestricted" contract is not enforced | suggestion |

Match rules, fixed in advance:
- A finding matches a key only if it **asserts that defect**, not merely mentions the code involved.
- L03 needs the exception, or an aborted flush or transaction; a finding that predicts only stale data
  does not match.
- L04 needs a caller without user privileges reading another user's assignments.
- L08 needs the re-read being unpinned by any test, or its comment being wrong.
- A finding can match more than one key, or none.

## What to decide, per finding

1. `matches`: the list of keys it asserts, or `[]`.
2. For a matched finding: `mechanism_correct` (true/false), whether the reason it gives is right.
3. For an unmatched finding: verify it. Run things. Where a claim says a test is vacuous, mutate the
   code it guards and see whether the test fails. Where a claim says something breaks, reproduce the
   break. Where a claim asserts an absence, prove your search finds a string you know is present
   before trusting a zero hit. Then classify:
   - **REAL**: you reproduced it, or traced a mechanism you are confident in, and the stated failure
     mode holds as written.
   - **REAL_OVERSTATED**: the observation is true but the stated failure mode or disposition is wrong.
   - **PLAUSIBLE_UNVERIFIED**: could be true; the experiment that would settle it did not run.
   - **NOISE**: false, already handled, or so trivial that acting on it changes nothing.
   Also mark `anchor_wrong: true` where the finding is anchored to code the PR does not touch or to a
   line that does not say what the finding claims.
4. `correct_disposition` for every finding: blocking, suggestion, question, nit, or drop.

Finally, list `duplicate_groups` among the unmatched findings: sets of keys that are the same
underlying defect stated differently. This is easy to under-call; say explicitly that you looked.

## Output

Strict JSON, built with `python3` and `json.dump`, to

    /private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-queue/5743c726-b776-450f-a755-a07953eaf17e/scratchpad/adj-6551-blind/adjudication.json

    {
      "verdicts": [
        {"key": "F007", "matches": ["L04"], "mechanism_correct": true,
         "class": "REAL|REAL_OVERSTATED|PLAUSIBLE_UNVERIFIED|NOISE|null for matched",
         "anchor_wrong": false, "what_i_ran": "...", "why": "one or two sentences",
         "correct_disposition": "blocking|suggestion|question|nit|drop"}
      ],
      "duplicate_groups": [["F003", "F011"]],
      "notes": "..."
    }

Then report back in plain text: how many findings matched each key, the class counts for the
unmatched ones, the duplicate groups, and the JSON path.
