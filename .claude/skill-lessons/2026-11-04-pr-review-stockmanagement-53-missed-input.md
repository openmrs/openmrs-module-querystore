# defect note · pr-review missed a 500 in the method the PR changed · openmrs-module-stockmanagement PR 53 at d4a9bba · 2026-11-04
outcome: miss. The review reported "ready to merge, nothing for the author to act on". A maintainer (Wikum) then found that a valid uuid with no `name` passes the new guard, runs the lookup and fails at `switch (delegate.getName())`. Non-blocking (he approved with it), and pre-existing on master.
written by: hand, in the reviewing session, after the user asked how the review missed it
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-openmrs-module-chartsearchai/5bb1becb-751a-4a76-b06d-08682713e488.jsonl

## Measured

Through REST 3.0.0's real `create()` → `save()` → `BaseRestController.handleException`, with `Context` stubbed. The control `name=SUBMIT` reaches `submitStockOperation`.
- PR head and master alike: no `name` key → 500 `NullPointerException` after `findStockOperations`. `"name": null` → the same. `""` or `FOO` → 400 `ConversionException` before any lookup. `QUANTITY_RECEIVED` → 400 from `default:`.
- `if (delegate.getName() == null) throw new IllegalRequestException(...)` after the uuid guard turns both into a 400 with no lookup. A test (valid uuid, null name, `verifyNoInteractions`) goes red without the guard and green with it. `addRequiredProperty("name")` still lets `"name": null` through to the 500.

## Why the review missed it (read from the transcript, not reconstructed)

- Every check was scoped to the ticket's harm, an action landing on the wrong operation, plus regressions against master. A null name acts on nothing, and master fails the same way, so it fails both filters by construction.
- The review generalized the HARM (whitespace uuids, `%20` paths on two sibling resources), never the INPUT. It varied only `uuid`. An absent or null `name` has zero mentions in its text, thinking or commands, and `reason` came up only as the dead validator's rule, never as an input to vary.
- The pointer was read and passed over. `StockOperationActionValidator.java:34` checks `getName() == null` right above the uuid check the PR replaces. The review read that validator only to reject wiring it, and grepped the `namerequired` key while tracing the uuid message.
- The independent reviewer's brief asked four closed questions: a wrong-operation path, a regression, a better approach, and test validity. None of them asks what else fails in the changed method, so the second reader inherited the same scope.

## Candidate lesson (one run, not corroborated)

When a PR adds input validation to a method, send absent, null, blank and unknown values for every request field that method reads through the real entry point. Also give the independent reviewer an open item, "what input still produces a 5xx or an unintended effect in the code this PR changes?", beside the ticket's failure mode.
Related: `2026-10-18-pr-review-reviewer-brief-556.md`, where runs saw a real defect and set it aside because "the issue's symptom does not reach that arm".
