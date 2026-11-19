You are judging review findings on an open-source pull request, blind: you do not know who wrote them. Do not edit,
create or delete anything outside a scratch test file you remove again, and do not use the network.

The pull request: openmrs-core PR 6535, head 86b96c7d6, base master a93b6293c9, in `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/44d9b58a-4830-47af-b032-8ad1f2e620d4/scratchpad/replay-2r/judge/repo` (a clone at the head with
a local `master` at the base; `git diff master...HEAD` is the PR). Its description, ticket and review conversation as of
the review moment are `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/44d9b58a-4830-47af-b032-8ad1f2e620d4/scratchpad/replay-2r/judge/pr6535-body-at-review.md`, `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/44d9b58a-4830-47af-b032-8ad1f2e620d4/scratchpad/replay-2r/judge/trunk-6682-at-review.json` and
`/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/44d9b58a-4830-47af-b032-8ad1f2e620d4/scratchpad/replay-2r/judge/pr6535-conversation-at-review.json`. Maven runs offline (`-o`) with the repo's `.mvn/maven.config`; for example
`mvn -o -B -q -pl api -am test -Dtest=TextHandlerTest -Dsurefire.failIfNoSpecifiedTests=false`.

Judge each of the 12 findings in `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/44d9b58a-4830-47af-b032-8ad1f2e620d4/scratchpad/replay-2r/judge/p2-all-findings.json` as exactly one of:
- **valid:** true of the head, and a defect or a fair question the author can act on in this PR;
- **invalid:** false of the head, or a failure that cannot happen;
- **noise:** true, but not worth the author's time in this PR (style no CI check enforces, speculation with no failure
  mode, a pre-existing issue on lines the PR does not touch, or a restatement of a thread already settled in the
  conversation file).
Check each claim against the code (and run what you need to); do not take a finding's own evidence as given.

Output: one line per finding, `F<nn>: valid|invalid|noise — one sentence on why, citing file:line`, then a count per
verdict. Under 600 words.
