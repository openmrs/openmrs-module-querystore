# resolve-ticket + harden + pr-harden · openmrs-module-chartsearchai · #387 / PR 404 · 2026-09-11
outcome: converged
rounds: 1 (pr-harden)   cycles: 6 (harden)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-387/043c28ee-345f-4d92-9fab-6aea245283b6.jsonl

## Refuted by measurement
- "its extent is a strict subset of the chips'" -> falsified by its own preceding sentence (this key
  publishes "Major" where a chip publishes "  Major  "); the two also come from different passes over
  different populations · cost: 1 phase-2 pass
- "ActiveOrderClaims and FindingCitationExtent never appear inside a list" -> two in-repo tests hold
  exactly `List<FindingCitationExtent>` / `List<ActiveOrderClaims>` · cost: same pass
- "equals, hashCode AND toString dereference severity" -> toString concatenates, which is null-safe; it
  returns `[350] null`. ChartOrderBridge's javadoc names only equals/hashCode · cost: 1 cycle
- "before #387 this returned the map's KEY SET" -> it returned the OFFENDING set (`offending.addAll(seen)`);
  the issue's own loose prose, repeated as a claim about this repo · cost: 1 cycle
- "a severity read off a chip reddens this file" -> measured: mutating ratingThisRecordStates to the
  chip's raw value left every case in that file green; caught by SafetyFindingSeverityCarriedContextTest
  instead, via padding · cost: 1 cycle
- "the two agree on every dataset but a padded one" -> false on the SHIPPED dataset: a chip rates a
  finding `Unknown` where this key has no entry at all (84,830 such links in the bundled KB), and the
  next sentence in the same paragraph said so · cost: 1 cycle, in three homes
- "a future reader that indexed an element would pass silently" -> it raises loudly on the old-shape
  fixture; the new selftest rows hold the OPPOSITE direction (a reader narrowed to the old shape) · cost: 1 cycle
- the ADR's claim that this change does not let the ESM reconstruction be deleted -> the client gates on
  this very key and its own header names #387 as the change that allows the deletion; the PR's README
  paragraph said the opposite in the same diff · cost: 1 cycle
- FOUR successive attempts to summarise how this value and a chip's compare were each refuted. What ended
  it was deleting the claim SHAPE (any distributional summary) and stating only mechanisms · cost: 5 cycles cumulative

## Raised by a fresh agent, missed by the author
- [harden c1] `equals`/`hashCode` entirely unobserved — making `equals` return `true` unconditionally left
  all 2251 tests green · blocking-equivalent · cost: 1 cycle
- [harden c1] the WARN's rating half unpinned: every case asserted the citation in the log, none the rating · cost: 1 cycle
- [harden c1] `Dockerfile.frontend` clones the ESM's default branch UNPINNED, so the in-repo demo stack
  silently degrades on this wire change — an in-repo consequence, not just another repo's problem · cost: 1 cycle
- [harden c3] README still instructed clients to "read the rating from the log or from the record" —
  the exact reconstruction #387 removes, in the client contract, eight lines below the new shape · cost: 1 cycle
- [harden c3/c4] the field name: `severity` invites a join that is "right almost always and wrong exactly
  in the tail". Raised independently by two lenses, the second told nothing about the first · cost: 1 cycle + a rename
- [harden c4] the rename's own prose still said `severity` in the two sentences whose job is to say it is
  not called that; one of a matched pair had been fixed and the other missed · cost: 1 cycle
- [pr-harden r1] selftest crashes on the wrong row when a reader drops the new shape, so the failure reads
  as a broken harness · non-blocking · filed as #408

## Where a skill blocked or contradicted this run
- harden:Termination — the confirming cycle for a documentation-only cycle is permitted a single agent; that
  agent died on a session rate limit (429) having only reached the build. Retrying was pointless (the limit
  was the session's, not the agent's), so the three obligations were done in-context and the deviation named.
  The limit cleared later and pr-harden's own agents ran normally.
- gate-state:harden-set — recording cycle 4 then cycle 6 left the commit-half window spanning cycle 5, so it
  reported edits=1 for a cycle that made none. Recording every cycle label fixed it; the helper's
  "commit half not measured" line is the tell.
- pr-harden:Step 0 — `maintainerCanModify: false` reads as a refusal condition but only gates CROSS-repo PRs;
  `isCrossRepository: false` is what settles it. Cost nothing, but the guard list invites the misread.

## Declined
- No `CLAUDE.md` bullet for this key — both instruction files sit within ~10 bytes of the size budget
  `ProjectInstructionsGuardTest` enforces, and that guard's javadoc calls raising it in the overflowing
  commit illegitimate. *If we ship without this, the two directives live only in javadoc and the ADR, so a
  future change may re-derive the pairing at a consumer or join `rating` to a chip's `severity` untripped.*
- Publishing the rating on `RecordReference` for EVERY cited finding — wider than the ticket, unmeasured.
  *If we ship without this, a client wanting a rating beside every citation still has nowhere to read one.*
- Pinning `hashCode`'s use of the rating. *If we ship without this, a degraded hashCode only slows a map
  keyed on these; it cannot change an answer.*

## Assumptions review overturned
- "the wire field should be `severity`, matching the chips' vocabulary" -> `rating`, because the shared
  spelling invites the one join four paragraphs forbid (harden cycles 3-4, two independent lenses)
- "the eval fixtures and scorer need no attention" -> the scorer's shape-tolerance is what the ADR's
  no-shim argument rests on, and it was unpinned (harden cycle 3)
