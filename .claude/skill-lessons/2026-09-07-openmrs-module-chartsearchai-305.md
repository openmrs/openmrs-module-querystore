# pr-harden (via resolve-ticket) · openmrs-module-chartsearchai · #305 / PR 385 · 2026-09-07
outcome: converged (round 6 reported zero blocking on the merging head)
rounds: 6 (default cap 4, raised to 6 on the stated signal)   cycles: 2 harden cycles pre-PR
verifier: ran twice — mid-run (verdict "works at runtime") and on the merging head (verdict "works at runtime")
context: compacted once, at the handoff from resolve-ticket step 8 into pr-harden step 9 · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-305/28d5f7ec-f1f4-4e33-b908-890f2d3ad289.jsonl

## Refuted by measurement
- "Every count of the withholding kinds is now deleted rather than maintained" (round 4's fixer, relayed
  by me into the commit subject, the PR body and my report to the user) -> THREE counts still stood in
  CitationGroundingVerifier (419, 614, 806); this branch had ADDED two and BUMPED the third from "Two
  reasons" to "Three reasons", inside the very commit titled "the withholding kinds are counted nowhere".
  One was also substantively false: it denied the three share a treatment, while #302 and #305 both map
  to UNVERIFIABLE. My own three termination greps missed them because they searched the phrasings I
  expected ("kinds of citation") rather than the concept. · cost: 1 round
- "the join is exact end to end" (mine, round 3) -> the standalone drove ALLERGY rows only; the condition
  leg was unmeasured, and moving the bullet out of "What is NOT measured" deleted the record that it was
  owed. · cost: 1 round
- "a model-cited chart citation is graded, a module-attached one never is" (mine, round 3, written while
  RETRACTING a false claim) -> falsified by the measurement three sentences above it in the same bullet;
  the disposition ternary makes a model-emitted citation UNVERIFIABLE whenever the claim unit is
  compound, and a green test had asserted exactly that all along. Stated symmetrically it licensed the
  contrapositive attachedByTheModule exists to prevent. · cost: 1 round
- "OpenMRS installs config.xml's descriptions into global_property, so they are the text an implementer
  reads in the admin settings UI" (mine, round 5 commit) -> measured false for the audience it names: the
  description is written only on row CREATION. Three-step measurement, including forcing the
  version-change path and deleting one row: the deleted row got the new text md5-exact, the two existing
  rows kept the old wording despite the version change. The module's OWN pre-existing activator javadoc
  already said this. · cost: caught by the final verifier, no round
- ADR Decision 80's "the published verdict still moves with the question's capitalisation" -> did not
  reproduce; badge read null on BOTH wordings on both patients, because the model cited the record inline
  and #302 withholds a compound claim unit either way. A control run (same record cited alone) published
  grounded true, proving grounding was live rather than inert. · cost: caught by the mid-run verifier

## Raised by a fresh agent, missed by the author
- [r1] The attach walk's LOOP SUBJECT was unpinned — only the check inside it was. `seen` ->
  `indexMap.keySet()` is one token, is exactly ADR Decision 80's refused alternative (attach
  unconditionally), and passed all 2142 tests. Reviewer proved non-equivalence with a throwaway probe.
  · blocking · cost: 1 round
- [r2] A comment reading "The two sibling checks need no such filter and their javadoc says why" — both
  halves false after the merge added a fourth post-answer check. · blocking · cost: 1 round
- [r3] Three false-claim homes, incl. README's client contract. · blocking · cost: 1 round
- [r4] A fourth home in this PR's own wire-contract TEST javadoc (a docs-only sweep misses test javadoc),
  and the javadoc of CitationGroundingVerifier.verify — the SOLE production entry point, untouched by
  this branch, still describing the whole-answer argmax Decision 80 refuses. · blocking · cost: 1 round
- [r5] The module descriptor's operator-facing property descriptions: XML, a file type four rounds of
  grepping java/md/py/sh never read. · blocking · cost: 1 round
- [r4 fixer, unprompted] RecordReference.getGrounded() — the PUBLIC accessor for the field — and the
  shared wire serializer both enumerated the null-causes with the module-attached case absent, never
  extended for #305 by any round.

## Where a skill blocked or contradicted this run
- pr-harden:"Compare the base you just fetched against the one the previous round saw" — earned its place
  before round 1 existed. `main` had moved two commits; the PR was CONFLICTING across 16 files; the ADR
  decision number collided (main took 78 and 79, ours became 80, 11 homes); and the merged root CLAUDE.md
  went 19 bytes over its 23,000 budget. All three named hazard classes fired in one merge.
- pr-harden:Termination — the Stop gate blocked me twice, both times correctly: I had left phase="fixing"
  in the state while narrating progress. Both times the fix was to finish the round, not to override.
- pr-harden:Reporting vs this repo's CLAUDE.md Bash-scoping rule — my `grep -E "^\[INFO\] Tests run:"`
  matched every per-class line and dumped ~150 lines of build output into context. The repo's own rule
  ("report a build as its FAILURES plus a computed total") is the one that was right; `grep -v ' -- in '`
  is the fix.

## Declined
- r1-2, second clause ("revert the ~14 compressed unrelated CLAUDE.md bullets") — arithmetically
  unavailable: ProjectInstructionsGuardTest caps that file at 23,000 bytes and origin/main alone is
  22,943, so restoring ~1.1 KB either drops the rule the PR exists to record or compresses a DIFFERENT
  set of unrelated bullets, which is the thing the clause asks to avoid. Failure mode of declining: a
  later reviewer cannot tell from the file alone whether a trimmed reason was moved or lost — which is
  why the falsifiable half WAS done (every dropped clause checked for another home; the one orphan is in
  docs/adr.md Appendix A with its date and provenance commit).

## Assumptions review overturned
- "The loop's exit condition is about code" -> five of six blocking findings after round 1 were PROSE, and
  three consecutive rounds were homes of one property. The sweep, not the edit, was the failing artifact.
- "A wider grep terminates a claim-correction sweep" -> four successively wider greps each missed a
  surface (test javadoc, then the sole production entry point, then XML). What terminated it was a
  STRUCTURAL check needing no phrasing (any line naming #302 and #284 but not #305) plus deleting every
  count outright, and even that first shipped with three counts still standing.
- "A verifier confirms" -> both verifier runs FALSIFIED prose the change asserted, while returning "works
  at runtime" on the behaviour. Reading `observed` for what it contradicts was worth more than reading it
  for what it confirms.
- "The environment is stable" -> the mid-run verifier found the standalone serving pre-#305 bytes from a
  stale expanded-module cache, with the omod timestamp, module-status endpoint and cache marker all
  reading current. Only hashing the loaded class against the built omod caught it.
