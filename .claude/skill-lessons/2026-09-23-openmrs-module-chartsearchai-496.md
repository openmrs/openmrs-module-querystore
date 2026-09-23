# resolve-ticket (+ harden, pr-harden) · openmrs-module-chartsearchai · #496 / PR #499 · 2026-09-23
outcome: converged
rounds: 1   cycles: 1   verifier: skipped (test/data/ADR-only change; no runtime code touched)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-496/0e6a9f09-b57c-4a02-873f-9f6ac37fbdf7.jsonl

## Refuted by measurement
- (none refuted) The ticket's claim that the hash alone closes the "still what Random(480) would draw" residue was measured: it pins the rest order but not the sample file's own items, so a recorded-positions check was added. Measured that the filed sample == random.Random(480).sample(rest,100) on the test's emitted ranking (seed 481 negative control), and that the recorded hash reproduces at measuredAt 27e9cf40 via a throwaway worktree running the new test there.

## Raised by a fresh agent, missed by the author
- [refuter] the ADR mutation list would name an incomplete set of tests after the new tests joined the class · non-blocking · cost: 0 (dated qualifier added)
- [harden P2, 3 of 4 agents] read-back assertArrayEquals was tautological; replaced by hashing the file as read back · polish · cost: 0
- [harden P2] "every run writes" false universal (a run failing before ranking writes nothing); "Not checked" understated residue for adjudicated links · polish · cost: 0
- [r1] swap of rows between two equal-weight unadjudicated links (940/262 × Hypotension) stays green · non-blocking · cost: 0 (follow-up issue #500)

## Where a skill blocked or contradicted this run
- resolve-ticket:Step 1 — `gh issue view` returned empty at exit 0 again; the gh api fallback worked.
- gate-state `await` without `--only` requires `--run`; the resolve-ticket snippet at Step 3 omits `--only pr`, so it failed with a usage error first. Cost: one call.
- harden Phase 1 insertion-orphan sweep: clean.

## Declined
- refuter Q3 / r1-1: carry cause drugs in the hash — declined as beyond the ticket's named line shape (link, chains, rated substances); if we ship without it, a swap of rows between equal-weight unadjudicated links stays green, because the line carries neither which rows nor which cause drugs. Filed as #500.

## Assumptions review overturned
- none
