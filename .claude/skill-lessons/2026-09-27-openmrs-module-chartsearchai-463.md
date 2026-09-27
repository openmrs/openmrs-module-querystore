# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #463 / PR #545 · 2026-09-27
outcome: converged
rounds: 2   cycles: 2 (harden: phase 2 escalated once)   verifier: skipped (the change lives in the Docker entrypoint/fetch library, which a standalone never runs, and in a config.xml description an existing standalone row keeps; substitute = ModelDownloadIntegrityTest driving the real library under /bin/sh against a loopback origin)
context: no compaction · peak not surfaced
transcript: /Users/danielkayiwa/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-463/3830a57e-9f6b-49c7-b1ea-3d9636e55ebc.jsonl

## Refuted by measurement
- Ticket 3b: "the code-3 arm promises curl -C - will resume a partial that is already complete, so the resume takes a 416 and recovery costs an extra restart" -> curl 8.7.1: a complete partial resumed against a 416 exits 0 and leaves the bytes intact (libcurl ignores -f for a 416 on a resume). The plan's library change for 3b was dropped. Refuter measured it first; I re-measured. · cost: 0 rounds (caught at plan gate)
- Ticket 4: "the bullet now points only at ADR Decision 80 while that reasoning lives in Decision 79" -> docs/adr.md:6227 "The refusals, and why each is stricter than #118's" is inside Decision 80; no change needed · cost: 0

## Raised by a fresh agent, missed by the author
- [plan gate] 3b premise false (see above) · blocking · cost: 0
- [harden p2] new guard regex unbounded: ran into the next GP's description when modelFilePath lost its own · substantive · cost: 1 harden cycle
- [harden p2] guard passed with digest inside an XML comment · polish · cost: 0
- [harden p2] stale code-table paragraph "pinned revision could not then be reached"; refusalRecord helper not reused · polish
- [r1] guard picked row by hardcoded id, not by the setting's defaultValue · non-blocking · implemented
- [r1] 5->6 remap also covers an unmeasurable (stat-failing) replacement; messages said only "hashed" · non-blocking · implemented
- [r1] upgraded installs keep old description; suggest activator log line · non-blocking · declined

## Where a skill blocked or contradicted this run
- none

## Declined
- r1-3 activator log line for upgraded installs — if we ship without this, an admin on an install that started the module before this change still reads the old description and must take the digest from README/model-manifest.tsv.

## Assumptions review overturned
- (A2) item 3b fixed in the library by placing a complete partial without a transfer -> dropped at plan gate by measurement
