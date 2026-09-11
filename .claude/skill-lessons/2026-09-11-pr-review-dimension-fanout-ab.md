# measurement · pr-review per-dimension fan-out A/B · openmrs-core PR 6551 · 2026-09-11
outcome: measurement only — one proposal derived from it, not yet through the Step 5 gate
protocol: pre-registered before either arm ran and before the orchestrator read the diff
artifacts: `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-querystore/9f0167e9-18df-4e14-bedd-eab4aa62b9c2/scratchpad/ab-6551/`
  (PROTOCOL.md with amendment, arm-A-control.json, lens-*.json ×6, arm-B-merged.json,
  adjudication-input.json + adjudication.json + the private key, oracle-analysis.md)
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-querystore/9f0167e9-18df-4e14-bedd-eab4aa62b9c2.jsonl

This is capture, not derivation. It records what one controlled comparison measured, so the next
attempt at fanning out a review starts from measured ground. **Origin: a design question** — would
`/pr-review` produce better reviews if each review dimension got its own agent? Nothing was posted to
GitHub; the PR under review was not touched.

## Design

Subject `openmrs/openmrs-core` PR 6551 (TRUNK-6669, restrict users to assigned locations), pinned at
head `b7cd909d245086fa10691496f88e5ec6971b77c2`, merge base `28e3bab9e206170df7fd43cf709495b6d45fe16a`
(from GitHub's compare API — the local clone is shallow, so `git merge-base` returns nothing and a
naive `origin/master...` diff hands every agent the wrong base). +216/-6 over 8 files. Chosen because
authorization semantics plus a liquibase changeset make every dimension load-bearing: the condition
most favourable to fan-out.

- **arm A** — one fresh agent, `pr-review` Steps 1-3 in full, single context, all six dimensions.
- **arm B** — six fresh agents, one per dimension named at `pr-review:97`, then one merger applying
  Step 4's rules, explicitly forbidden from generating findings of its own.
- Both arms blinded to the PR's existing review conversation, so the 8 human inline comments could
  serve as an independent oracle. Symmetric deviation, recorded: it costs both arms their dedup
  behaviour and neither arm's generation.
- Arm A ran alone; arm B's six contended with each other. The handicap runs **against** arm B.
- Adjudication by a fresh agent, arm labels stripped, order shuffled, evidence text scrubbed of
  within-arm cross-references (10 found and neutralised; a residual-leak check ran after).

## Measured

**Precision does not separate the arms.** The blinded adjudicator rated **20 of 20 findings REAL** —
no NOISE, no PLAUSIBLE_UNVERIFIED, no REAL_OVERSTATED, `anchor_wrong` on none.

**Defect coverage.** 20 findings collapse to 12 distinct defects by the adjudicator's own equivalence
classes: 7 found by both arms, 4 by arm B only, 1 by arm A only. Arm A's one unique defect (a
double-duty constant) *was* found by arm B's conventions reviewer and dropped by arm B's merger with
the other nits, so raw arm B covered 12 of 12, merged arm B 11 of 12, arm A 8 of 12.

**Severity accuracy, which is the sharper result.** Arm A filed 2 blocking where the adjudicator
found 3, under-calling the `getAllowedLocations` privilege hole as a suggestion. Arm B filed 3 of 3,
with no over-call. On that finding arm A reasoned from the code; the security reviewer ran the
exploit with a positive control (same caller: `APIAuthenticationException` on `getUser(1)`, location
data returned from `getAllowedLocations`) and traced it to `AOPConfig` advising `@Service` only while
`HibernateUserDAO` is `@Repository`.

**Verification depth.** Arm B returned verified answers for 3 of arm A's 5 `could_not_verify`
entries: it ran `DatabaseUpdaterDatabaseIT` (which arm A skipped as integration-profile) and
confirmed the changeset count; it proved the liquibase changeset *is* covered in CI by renaming
`location_id` and watching `ValidateHibernateMappingsDatabaseIT` fail, then killed the candidate
finding rather than raising it; and it reproduced in-session the transaction abort arm A could only
file as a question, isolating the cause by removing the native delete and seeing the exception
persist.

**Volume and the merge.** Raw arm B produced 25 findings (5 blocking, 12 suggestions, 4 questions,
4 nits) against arm A's 9. The merger cut 25 to 11 — 6 merge groups consuming 13 findings, 7 dropped,
every nit discarded — and stayed inside its authority, routing 4 of its own observations to a
did-not-add list. The predicted flood appeared in raw form and the merge step absorbed it.

**Cost.** arm A 194,690 tokens / 66 tool calls / 22.6 min. arm B 1,256,194 tokens / 388 tool calls /
35.6 min (25.2 min lens wave, a wave costing its slowest member, plus a 10.4 min merge). Ratio
6.45× tokens for 1.58× wall clock. Adjudicator, charged to neither arm: 183,588 tokens / 21.5 min.

**Per-reviewer yield.** security 2 unique defects and the corrected severity; test coverage 0 unique
but one blocker and two could-not-verify resolutions; correctness 0 unique but the question→blocking
upgrade; solution fit 0 unique, and the only approach-level alternative anyone proposed
(`onDelete="CASCADE"` on the PR's own unshipped FK, with in-tree precedent, deleting the native query
and its caveat together); performance 1 unique, non-blocking; conventions 1 unique, non-blocking,
plus 3 nits the merger dropped.

## What the human oracle measured, which was not the A/B

The 8 captured comments are 4 findings plus 4 author replies, and all four findings were marked
resolved *at* the reviewed sha (the author's fix commits are ancestors of it). Recall: arm A 3 of 4,
arm B 4 of 4. The finding arm A missed is the residue of a fix — the null-collection repair still
lets `setLocations(null)` restore null.

More useful than the recall number: **both arms reopened two threads the human review had closed.**
A maintainer asked for a regression test, the author added one, the thread closed — both arms mutated
the fix away and watched the test stay green. A maintainer described the session-cache risk as stale
data and "safe in current code paths", the author shipped a comment saying exactly that, the thread
closed on "good catch" — the behaviour is a `TransientPropertyValueException` that aborts the
transaction, reachable from the PR's own new test plus one ordinary query. Both are the case
`pr-review` Step 1 already names (*"fixed" is a claim, not evidence*), here accepted by two humans
and a review bot.

## Refuted — a claim this run's own orchestrator published and the run broke

- **"The toolchain for this repo is JDK 8."** The orchestrator ran
  `mvn help:evaluate -Dexpression=javaCompilerVersion` in the main checkout, which sits on a stale
  local branch, and briefed all seven agents accordingly. The pom **at the reviewed sha** sets
  `maven.compiler.release=21`; JDK 8 dies in spotless before any test runs. Arm A found it mid-run and
  five of six lenses reported it independently. A value proved present somewhere is not a value proved
  on the path in question — which is `pr-review:88`'s own rule, applied to a property instead of a
  method. The brief was left uncorrected for arm B on purpose, to hold the per-agent condition equal
  (PROTOCOL.md, Amendment 1).

## Limits this run cannot argue past

- **n=1**, on a PR selected because it favoured the treatment. A dimension effect and a this-PR
  effect are not separable here.
- **20 of 20 REAL is suspiciously clean.** Either both arms were disciplined — both reports do carry
  killed candidates — or the adjudicator was not adversarial enough. A second adjudicator told to
  refute rather than verify would settle it and was not run.
- **Both arms were blinded to the PR conversation**, so nothing here measures whether six reviewers
  each re-deriving the same thread state makes deduplication worse. That is the obvious place
  fan-out would cost something, and this run is silent on it.
- The handicap ran against arm B and arm B still won, so the direction is safe; but arm B's wall-clock
  advantage would shrink on an uncontended machine.
