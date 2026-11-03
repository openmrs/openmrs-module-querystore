You are verifying code-review findings against real code. Several independent reviews of the same
pull request produced the findings in the input file. You do not know how any review was produced,
and it does not matter: judge each finding only against the code.

## Inputs

    /private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-queue/5743c726-b776-450f-a755-a07953eaf17e/scratchpad/adj-blind/adjudication-input.json

Each entry has an opaque `key` (e.g. `F007`), plus `file`, `line`, `where`, `disposition`, `claim`,
`failure_mode`, `evidence`. The entries are shuffled; their order carries no information, and any
grouping you might infer from it is not real.

## The code

`openmrs/openmrs-module-queue` PR 96 at head `ae7f8fa20945014019da201547383286aab99c94`; base
`main` = `bdfaa72846d87ac123c89791c6cce1c04a888065`. The PR diff is exactly `git diff main...HEAD`
(3 files, +170/-0). Your own clone, at the head:

    /private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-queue/5743c726-b776-450f-a755-a07953eaf17e/scratchpad/adj-blind/wt-adj

- Its `.mvn/maven.config` points Maven at a private repository; leave `.mvn/` alone. Build with JDK 8
  (`JAVA_HOME=/Library/Java/JavaVirtualMachines/openlogic-openjdk-8.jdk/Contents/Home`), offline
  (`-o`). For integration tests use `-pl integration-tests -am -Dtest=X
  -Dsurefire.failIfNoSpecifiedTests=false`; `-pl integration-tests` alone tests a stale jar.
- openmrs-core 2.7.4 sources: `git -C /Users/danielkayiwa/Projects/openmrs/openmrs-core show
  2.7.4:<path>`. REST module sources: `/Users/danielkayiwa/Projects/openmrs/webservices`. Read only.
- Do not read the live PR or its tickets on GitHub or JIRA, and do not read
  `/Users/danielkayiwa/Projects/openmrs/queue` or the directory
  `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-queue/5743c726-b776-450f-a755-a07953eaf17e/scratchpad/replay`, or `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-queue/5743c726-b776-450f-a755-a07953eaf17e/scratchpad/replay-ctl`, or anything else under `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-queue/5743c726-b776-450f-a755-a07953eaf17e/scratchpad` outside
  `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-queue/5743c726-b776-450f-a755-a07953eaf17e/scratchpad/adj-blind`.
  Either would show you what other people concluded or how the reviews were run, and your job is to
  reach your own conclusion from the code.
- Never run `mvn install` outside your clone's private repository. Do not spawn subagents. Restore any
  mutation with `git checkout -- <path>` and leave the clone clean.

## Defects already known to exist at this head

Each was established by an earlier review and verified against this sha. Treat these as given.

| key | defect | severity |
|---|---|---|
| K1 | the advice proxies GET_QUEUE_ENTRIES and MANAGE_QUEUE_ENTRIES, but `purgeQueueEntry` is `@Authorized(PURGE_QUEUE_ENTRIES)`, so a caller holding Purge Visits but not Purge Queue Entries cannot purge the visit; MANAGE does nothing on this path | blocking |
| K2 | the PR description says the advice intercepts `voidVisit` and voids entries with the same reason, and gives a root cause the code now contradicts | non-blocking |
| K3 | unused `QueueEntry` import in `VisitWithQueueEntriesDeleteAdviceTest` | nit |
| K4 | the purge tests never flush, so they assert through the Hibernate session and never reach the `queue_entry_visit_id_fk` failure the PR exists to fix | non-blocking |
| K5 | the comment "the visit or its patient may already be voided, which would hide the entries from the default search" is wrong about the visit: the entry search does not filter on the visit's voided flag | non-blocking |
| K6 | voiding a visit (the ticket's reported symptom: the chart's delete is a void) is left to `VisitWithQueueEntriesSaveHandler`, whose void branch is guarded by `visit.getVoided()` and so works only when core's `BaseVoidHandler` runs first; both take the default `@Handler` order and core breaks the tie in HashMap iteration order | blocking |
| K7 | the entries are purged in a `MethodBeforeAdvice`, outside the visit's transaction, so when `purgeVisit` then refuses (`APIException("Visit.purge.inUse")` when the visit has encounters) the queue entries are already gone and the visit survives | blocking |
| K8 | the `config.xml` `<advice>` block is read by no test: delete it and the build stays green, and a wrong class name would fail quietly at module start | non-blocking |
| K9 | the class Javadoc says the visit's void stamp lets "an unvoid ... tell them apart", but no `UnvoidHandler<Visit>` exists in the module | non-blocking |

Match rules, fixed in advance:
- A finding matches a key only if it **asserts that defect**, not merely mentions the code involved.
- K6 needs the ordering or guard mechanism, or the claim that the reported void half of the ticket is
  not reliably fixed by this head. A finding that only says the description and code disagree is K2.
- K7 needs queue entries being deleted while the visit purge fails or rolls back (non-atomic with the
  visit delete). A generic worry about partial failure or atomicity that does not establish that is
  `K7-partial`.
- A finding phrased as a question that names K7's scenario (queue entries deleted while the visit
  purge is refused or rolled back) without asserting it is `K7-q`.
- A finding can match more than one key, or none.

## What to decide, per finding

1. `matches`: the list of keys it asserts (use `K7-partial` or `K7-q` where the rules say so), or `[]`.
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

    /private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-queue/5743c726-b776-450f-a755-a07953eaf17e/scratchpad/adj-blind/adjudication.json

    {
      "verdicts": [
        {"key": "F007", "matches": ["K7"], "mechanism_correct": true,
         "class": "REAL|REAL_OVERSTATED|PLAUSIBLE_UNVERIFIED|NOISE|null for matched",
         "anchor_wrong": false, "what_i_ran": "...", "why": "one or two sentences",
         "correct_disposition": "blocking|suggestion|question|nit|drop"}
      ],
      "duplicate_groups": [["F003", "F011"]],
      "notes": "..."
    }

Then report back in plain text: how many findings matched each key, the class counts for the
unmatched ones, the duplicate groups, and the JSON path.
