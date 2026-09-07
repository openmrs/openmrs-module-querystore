# pr-harden 0.18.0 · openmrs-module-chartsearchai · PR #366 (issue #353) · 2026-09-03
outcome: converged
rounds: 4   cycles: n/a   verifier: ran x3 (works at runtime each round)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-chartsearchai/7eac6885-815e-4652-a1f3-bfffac760478.jsonl

Entered as a RESUME: a `pool-run --work 353` driver was killed by a host restart mid-run, after
`resolve-ticket` had pushed 3 commits and opened draft PR #366 but before any review round. Recovered
from the pushed branch; no work was lost.

## Refuted by measurement
- "The round-1 clause is prompt-facing with no observability channel, so a standalone cannot see it"
  (orchestrator's own brief to the round-1 verifier, which told it to report `could not determine`)
  -> `reference_slice_chars` on the audit row measures the injected slice to the character, and the
  clause has computable length. Round 1's verifier used a predicted +100 and observed exactly +100;
  round 2 refined it into a controlled +10-char display instrument where delta/10 IS the clause item
  count; round 3 predicted 789 and 898 and observed both exactly. · cost: 0 rounds (the verifier
  disregarded the wrong steer and measured anyway)
- Round 1's fix ("refuse the clause where the bridged answer spans >1 substance") -> over-refused
  every genuine fixed-dose combination; 1112 of 4251 bridged concepts trip it, 990 of them real
  combinations whose recorded name names every substance. · cost: 1 round
- Round 2's fix ("asked per substance") -> held only on the QUESTION's subject side; on the partner
  side the row was elected by first-match over `findForActiveOrders`, so the answer depended on
  knowledge-base FILE ORDER. Proved by swapping two fixture rows: clause present one way, absent the
  other. · cost: 1 round
- "46 of the 4251 bridged concepts answer with more than one substance", published in six homes ->
  a FILTERED measurement (population minus combination-shaped names) attached to a predicate applying
  no filter; the real reach is 1112. Could only be reproduced by also excluding " and ", which no
  stated rule said. · cost: 1 round (non-blocking)
- "990 of the 1112 are fixed-dose combinations" -> the predicate says "recorded name names every
  substance it resolves", which is not the same class; 967 carry RxNorm's `/`, the remainder are
  salt/ester/derivative names (`Mometasone furoate`, `Hydrocortisone butyrate`). #243's shape one step
  along: a measured figure relabelled as a clinical class. · cost: 0 rounds (found in the converging round)
- "The test diff is additions only" (round-3 fixer) -> 7 deletions, all javadoc lines the change made
  false. Checked rather than accepted; no assertion weakened. · cost: 0 rounds

## Raised by a fresh agent, missed by the author
- [r1] The bridged leg feeds `addChartOrderBridge`, not only the candidate set, so for a multi-substance
  bridged concept it printed a FALSE substance-to-prescription attribution (`Omeprazole from Inexium
  40mg` for a patient on esomeprazole) into a citable `safety_finding` carrying STRENGTH_WITHHOLD.
  The PR's "subset of what an `en` session already gets" argument is about the candidate SET and does
  not transfer to that consumer, because the clause's silence test is `recordsANameOfAny` and the leg's
  whole premise is that the order's names do NOT reach the substance. · blocking · cost: 1 round
- [r2] The above fix could not tell an ambiguous bridge from a genuine combination. · blocking · 1 round
- [r3] The per-substance property held only on the subject side; partner side rested on file order. · blocking · 1 round
- [r4] The election's TIE-BREAK was asserted in three homes and witnessed nowhere: `claim > strongest`
  -> `claim >= strongest` left all 1806 api tests green while moving a clinical statement back onto
  dataset row order. · non-blocking
- [r1 fixer, unprompted] The PR had ORPHANED `resolvesFrom`'s entire javadoc — a new class was inserted
  between the comment and the method, so `resolvesFrom` shipped undocumented. No reviewer flagged it.
- [r3 verifier] Round 2 left its own +10 measurement instrument in the standalone data
  (`Inexium 40mgZZZZZZZZZZ`), so a naive comparison against round 2's recorded 671 would have been
  invalid. Caught and reset before measuring.

## Where a skill blocked or contradicted this run
- `pool-run --claim` WARNED that #366 was already open and that this is `pr-harden`'s entry point, then
  reset the worktree to origin/main anyway, dropping the branch checkout. Nothing was lost (all commits
  were pushed) but the warning and the action disagree.
- The branch was held by FOUR orphaned lens worktrees from the killed `resolve-ticket` harden phase, so
  `git checkout` of the PR branch was refused. All four were byte-identical to a pushed commit. A killed
  run leaves these behind and nothing reaps them.
- `gate-state` had a live-looking entry from the dead session (`round 1, phase building, owner 81993`).
  The skill says to ADOPT an entry whose `pr` is null as the `resolve-ticket` handoff; the orchestrator
  cleared it instead before reading that passage. Cost nothing here (empty ledger, round 1) but it was
  luck, not judgment — two rounds later it would have discarded a real declined ledger.
- The round-4 reviewer died instantly on a session 429. The skill's documented lever (retry on a cheaper
  model) is now refused by a PreToolUse hook, leaving "leaner brief" and "wait for the stated reset".
  Both applied; the retry converged. The 429 arrived with no partial work, so nothing needed reconciling.

## Declined
- r1-4 (non-blocking): add a CIEL reference-map-code rung beside the bridged-uuid key. "If we ship
  without this, a deployment that files CIEL concepts under local uuids and reaches CIEL only through
  `concept_reference_map` gets nothing from the bridged leg — its francophone orders stay unjoined,
  exactly as on main — because the uuid key cannot see a cross-walked concept." It is a second KEY, not
  a second spelling: needs CIEL-source maps carried on `ActiveDrugOrder` (`addAtcCodes` reads maps only
  for ATC-named sources), a rule for which `ConceptSource` names count as CIEL, and a false-join
  measurement against a cross-walking dictionary, none available in this environment. Recorded on the
  ADR as considered-and-not-taken with its residue.

## Assumptions review overturned
- "A prompt-facing change cannot be verified at runtime" -> it can, via `reference_slice_chars` with a
  controlled display-length delta. This is now the established instrument for this module and was used
  by three successive verifiers.
- "The two suppression sites cannot cheaply be pinned individually" (round-2 fixer, stated honestly as
  a residue) -> round 3's reviewer said the cases were cheap and sketched them; round 3's fixer built
  both, and while doing so found the THIRD bridged argument was already pinned. The "a case per site is
  owed" admission is retired.
- "The fixed-dose-combination population is what a francophone ARV list is mostly made of" -> the stock
  CIEL name `Abacavir / lamivudine` itself satisfies the NAME leg by bounded-token matching, so the
  bridged leg's premise never holds for an unmodified CIEL 103166 order. The change reaches only
  deployments whose locale-preferred concept name does not spell the constituents. Round 2's verifier
  had to RENAME two demo concept FSNs to make the case discriminating at all.
