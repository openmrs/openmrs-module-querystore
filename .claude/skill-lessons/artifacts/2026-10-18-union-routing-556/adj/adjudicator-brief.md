You are verifying code-review output against real code. Several independent reviews of the same pull
request produced the entries in the input file. You do not know how any review was produced, and it
does not matter: judge each entry only against the code.

## Inputs

    /private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-queue/5743c726-b776-450f-a755-a07953eaf17e/scratchpad/adj-556-blind/adjudication-input.json

Each entry has an opaque `key` (e.g. `E007`) and a `kind`:
- `finding`: something a reviewer asserted or asked, with `file`, `line`, `disposition`, `claim`,
  `failure_mode`, `evidence`;
- `unverified-risk`: something a reviewer said it could not verify; only `claim` is filled in.

The entries are shuffled; their order carries no information, and any grouping you might infer from it
is not real.

## The code

`openmrs/openmrs-module-chartsearchai` PR 556 at head `d77204bae7091579a7131f486b613d03e365609d`; base
`main` = `e089267e`. The PR diff is exactly `git diff main...HEAD` (15 files, +493/-69). Your own clone,
at the head, with full history:

    /private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-queue/5743c726-b776-450f-a755-a07953eaf17e/scratchpad/adj-556-blind/wt-adj

- Its `.mvn/maven.config` points Maven at a private repository; leave `.mvn/` alone. Build with JDK 11
  (`JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-11.jdk/Contents/Home`), offline (`-o`). A narrow
  run: `mvn -o -B -pl api -am test -Dtest='CitedFindingPartnerCompletionTest' -Dsurefire.failIfNoSpecifiedTests=false`
  (14 tests, about a minute). The repository's CLAUDE.md files describe its rules.
- Do not start, stop, deploy to or query any OpenMRS server, standalone or llama-server on this machine.
- Do not read this repository on GitHub, anything under `/Users/danielkayiwa/Projects` or
  `/Users/danielkayiwa/.claude` (except a file the harness says it saved your own tool output to), or
  anything under
  `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-queue/5743c726-b776-450f-a755-a07953eaf17e/scratchpad`
  outside `.../scratchpad/adj-556-blind`. Each would show you what others concluded or how the reviews
  were run; your job is to reach your own conclusion from the code.
- Do not spawn subagents. Restore any mutation with `git checkout -- <path>` and leave the clone clean.

## Defects already known to exist at this head

Each was established by an earlier review of this sha. Treat these as given.

| key | defect | severity |
|---|---|---|
| S1 | condition-mediated findings never get partner rows (`SafetyWarning.conditionMediated(...)` is never given `withPartnerRows`, so `getFindingPartnerRows()` is empty for them), so the #555 credit does not apply to them and they still print the partner by the knowledge base's parenthetical label | blocking |
| S2 | the `comparable` javadoc in `FindingPartnerCoverageCheck` claims `InteractionClaimPairFidelityCheck` takes the same form "so the two checks cannot read one name as stated to one and not to the other"; since #555, `FindingPartnerCoverageCheck` also counts a partner as stated through `DrugSafetyValidator.namesThePartner`, which the other check does not, so the claim is false | non-blocking |
| S3 | `namesThePartner` credits a partner when ANY alias of its rows appears ANYWHERE in the answer (through `namesAnyOf`/`matchesText`); the knowledge base gives substances aliases that are everyday lab or analyte words (the `Lactic acid` row carries `lactate`), so an answer that mentions only the lab marks the partner as stated and silently drops the "not named above" note | blocking |

Match rules, fixed in advance:
- A `finding` matches a key only if it **asserts that defect** (or asks it as a question that names the
  mechanism), not merely mentions the code involved.
- S3 needs over-crediting: a partner counted as stated because of an alias, or a name that is an
  ordinary word, which the answer did not use to name that drug. A finding about the opposite direction
  (a partner the answer did name that is still reported unstated) does not match S3.
- An `unverified-risk` entry matches `S1-r`, `S2-r` or `S3-r` if it names that possibility without
  establishing it.
- An entry can match more than one key, or none.

## What to decide, per entry

1. `matches`: the list of keys it asserts (with the `-r` suffix for an unverified-risk entry), or `[]`.
2. For a matched entry: `mechanism_correct` (true/false).
3. For an unmatched entry, verify it. Run things. Where a claim says a test is vacuous, mutate the code
   it guards and see whether the test fails. Where a claim says something breaks, reproduce it. Where a
   claim asserts an absence, prove your search finds a string you know is present before trusting a
   zero hit. For an `unverified-risk` entry, ask whether there is a real defect behind the risk it
   names. Then classify:
   - **REAL**: you reproduced it, or traced a mechanism you are confident in.
   - **REAL_OVERSTATED**: the observation is true but its stated consequence or disposition is wrong.
   - **PLAUSIBLE_UNVERIFIED**: could be true; the experiment that would settle it did not run.
   - **NOISE**: false, already handled, outside the PR, or so trivial that acting on it changes nothing.
     An unverified risk about the reviewer's environment (a database it did not run, a server it could
     not reach) with no defect behind it is NOISE.
   Mark `anchor_wrong: true` where a finding is anchored to code the PR does not touch or to a line
   that does not say what it claims.
4. `correct_disposition` for every entry: blocking, suggestion, question, nit, or drop.

Finally, list `duplicate_groups` among the unmatched entries: sets of keys that are the same underlying
point stated differently. Say explicitly that you looked.

## Output

Strict JSON, built with `python3` and `json.dump`, to

    /private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-queue/5743c726-b776-450f-a755-a07953eaf17e/scratchpad/adj-556-blind/adjudication.json

    {
      "verdicts": [
        {"key": "E007", "matches": ["S3"], "mechanism_correct": true,
         "class": "REAL|REAL_OVERSTATED|PLAUSIBLE_UNVERIFIED|NOISE|null for matched",
         "anchor_wrong": false, "what_i_ran": "...", "why": "one or two sentences",
         "correct_disposition": "blocking|suggestion|question|nit|drop"}
      ],
      "duplicate_groups": [["E003", "E011"]],
      "notes": "..."
    }

Then report back in plain text: how many entries matched each key, the class counts for the unmatched
ones split by kind, the duplicate groups, and the JSON path.
