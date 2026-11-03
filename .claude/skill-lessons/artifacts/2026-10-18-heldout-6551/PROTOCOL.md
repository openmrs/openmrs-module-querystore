# Held-out test: do a per-condition mutation sentence and a techniques file change what `pr-review` finds on openmrs-core PR 6551?

Pre-registered 2026-10-18, before any arm ran. Follows the queue PR 96 replay
(`2026-10-18-pr-review-lens-replay-queue96.md`), which left two candidates standing:

1. the per-condition mutation sentence, which found a real defect on PR 96 that neither its own
   targets nor the answer key named (exploratory, 2 of 3 runs against 0 of 9);
2. giving reviewers OpenMRS verification technique rather than another instruction, untested, and
   untestable on PR 96 because there it gives the answer away.

PR 6551 is held out from both. The mutation sentence was written for PR 96. The techniques file
holds every verification lesson from the queue work, each citing its queue source, and none from
6551. **The author had read 6551's findings in the 2026-09-11 A/B before writing it**, so the file
includes every queue lesson rather than a selection, to keep that knowledge from choosing what goes in.

## Subject

`openmrs/openmrs-core` PR 6551 (TRUNK-6669), head `b7cd909d245086fa10691496f88e5ec6971b77c2`, base
`28e3bab9e206170df7fd43cf709495b6d45fe16a`, 8 files +216/-6: the same pin as the 2026-09-11 A/B, so its
blind adjudication is the answer key. The PR is still open, and its head has since moved to
`f1a653dd`, possibly with fixes for these findings; each clone holds only `b7cd909d`'s ancestry
(382 commits, shallow), checked against the source checkout's later head, which is absent. PR
description and TRUNK-6669 snapshotted as of 2026-09-11. **The PR conversation is withheld**, as in the
A/B, which keeps the answer key comparable.

## Arms (n=3 each, 9 runs, three waves of one per arm in random slot order)

- **C** pr-review 0.19.0 unmodified.
- **M** C plus, appended to *Test coverage*: "Failing on the base branch reverts the whole change at
  once, so a test can clear that bar while one new condition, guard or registration is pinned by
  nothing. For each one the PR adds, delete or invert it alone and name the test that goes red; one
  the suite survives is a finding."
- **K** C plus `openmrs-verification.md` beside the skill (this directory's copy, verbatim), and one
  sentence as the first bullet of Step 3: "For OpenMRS code, read `openmrs-verification.md`, beside
  this file, before you verify anything: it says how to observe what OpenMRS does at runtime (the
  database behind the session, a transaction, an authorization check, handler order) and which
  readings of its code have proved wrong."

Headless `claude -p` from each clone (project memory empty by construction; canary repeated before
wave 1). Private Maven repository per clone, JDK 21 offline; calibrated: `mvn -o -B -pl api -am test
-Dtest='LocationServiceTest#purgeLocation_shouldDeleteLocationAssignedToAUser'` passes in 70 s with the
private repository left empty. Tool-call audit as before, extended to `~/Projects`, `~/.claude` (the
A/B's answer key lives in `~/.claude/skill-lessons`) and the live PR 6551.

## Answer key (the A/B's blind adjudication at this head, 13 defects)

| key | defect | A/B disposition |
|---|---|---|
| L01 | `purgeLocation_shouldDeleteLocationAssignedToAUser` passes with the cleanup deleted: it reads through the session and nothing flushes, so the FK is never evaluated | blocking |
| L02 | the cleanup deletes `user_location` rows only for the purged location's own id, but `session.remove` cascades to `childLocations`, so purging a parent whose descendant is assigned to a user still hits the FK | blocking |
| L03 | with the fix, a flush while a persistent User whose `locations` holds the purged Location is in the session throws `TransientPropertyValueException`, not stale data; the added comment understates it | split: suggestion / blocking |
| L04 | `getAllowedLocations` is gated only by Get Locations (granted to Authenticated) and reads the target user via `dao.getUser`, bypassing `UserService.getUser`'s gate: any authenticated user reads any user's assignments | blocking |
| L05 | `getLocations()` throws `LazyInitializationException` on a detached User (e.g. `Context.getAuthenticatedUser()`), untested; `roles` is EAGER | suggestion |
| L06 | `@Independent` on `User.locations` is load-bearing and untested, while the repo tests both other `@Independent` fields | suggestion |
| L07 | the tag lookup uses `getLocationsByTag` (all locations and tags loaded, filtered in Java) where `getLocations(LocationSearchCriteria)` does it in SQL | suggestion |
| L08 | the re-read `dao.getUser(user.getUserId())` is pinned by nothing (all new tests pass with the caller's object) and its comment misdescribes attached users | suggestion / nit |
| L09 | `User.locations` is a lazy `@ManyToMany` with no `@BatchSize` (N+1); Location's collections carry `@BatchSize(100)` | suggestion |
| L10 | an empty set means unrestricted, so purging a location silently makes restricted users unrestricted, with no trace; undocumented | suggestion |
| L11 | `LOCATION_ID` serves two namespaces (JPA attribute, native SQL parameter) and reaches untouched methods | nit |
| L12 | the restriction is advisory (a picker filter) while the title and javadoc read as enforcement | question |
| L13 | no add/remove mutators; `setLocations(null)` restores null, so the empty-set contract is unenforced | suggestion |

Match rules: a finding matches only if it asserts the defect. L03 needs the exception (or an aborted
flush/transaction), not staleness. L04 needs the read of another user's assignments by a caller
without user privileges. L08 needs the re-read being unpinned or misdescribed. A finding can match
several keys or none; unmatched findings are verified and classified REAL / REAL_OVERSTATED /
PLAUSIBLE_UNVERIFIED / NOISE.

## Outcomes and pre-committed reading (a signal is a difference of at least 2 of 3 runs)

- **K vs C, primary:** L04 raised *and* filed blocking (the A/B's control filed it as a suggestion; the
  lens that called it blocking had run the exploit). Secondary: L03 asserted rather than asked; L01 and
  L02.
- **M vs C, primary:** L06 or L08 raised (each is a per-condition mutation finding). Secondary: any
  other unmatched finding adjudicated REAL that comes from deleting or inverting one condition.
- Also reported: L01-L13 per arm, NOISE, cost per run.
- K signal on L04-blocking → technique transfers across PRs; the candidate becomes a techniques file
  for skill-retro. M signal on L06/L08 → the mutation sentence has now produced findings on two PRs;
  it becomes a skill-retro proposal. Anything else → no signal at n=3, said as such.

## Threats

- n=3 per arm, one PR. The answer key is the A/B's single adjudication, made by an adjudicator its own
  record flags as possibly not adversarial (20 of 20 REAL).
- C's shape differs from the A/B's arm A (skill 0.17.x then, 0.19.0 now), so the A/B is context, not a
  fourth arm.
- The techniques file was written by someone who knew the answer key; the include-everything rule is
  the only guard.
- Contention with the user's other workloads; symmetric within a wave.

---

# RESULTS — recorded 2026-10-18 from the blind adjudication, before interpretation

All 9 transcripts audited, 0 forbidden reads; the adjudicator's 113 tool calls, 0. Adjudicator:
257,790 tokens, 91 tool uses, 21.0 min. 40 findings: 35 matched, 5 unmatched, all 5 REAL, 0 NOISE.

| arm | L01 | L02 | L03 | L03 asserted | L04 | L04 blocking | L05 | L06/L08 | L10 | L12 | other REAL |
|---|---|---|---|---|---|---|---|---|---|---|---|
| C | 3/3 | 0/3 | 3/3 | 3/3 | 1/3 | 0/3 | 1/3 | 0/3 | 3/3 | 0/3 | 1 |
| M | 3/3 | 2/3 | 3/3 | 2/3 | 3/3 | 0/3 | 1/3 | 0/3 | 1/3 | 0/3 | 1 |
| K | 3/3 | 1/3 | 3/3 | 3/3 | 2/3 | 0/3 | 0/3 | 0/3 | 1/3 | 2/3 | 3 |

L07, L09, L11, L13: 0/9. The five unmatched REAL, in three defects: the native delete clears the whole
second-level cache (wt-01 M, wt-02 K); all five `getAllowedLocations` tests pass with `User.locations`
unmapped (wt-08 K, wt-09 C); Envers writes no removal row for the native delete (wt-06 K).

## Against the pre-committed readings

- K vs C, primary, L04 raised and filed blocking: 0/3 vs 0/3. No signal.
- K vs C, secondary: L03 asserted 3 vs 3; L01 3 vs 3; L02 1 vs 0. No signal.
- M vs C, primary, L06 or L08: 0/3 vs 0/3. No signal. The per-condition mutation finding this PR did
  yield (the unmapped collection) came from C and K, not M.

## Exploratory, not pre-registered

- Every L04 finding (6, in 6 of 9 runs) was filed as a question. wt-02 (K) ran the exploit with a
  positive control (`becomeUser` plus Get Locations only: `getUser(1)` refused, `getAllowedLocations`
  returned user 1's list) and still asked whether the exposure was intended. The adjudicator, like the
  A/B's, rates it blocking.
- K read its techniques file in 3 of 3 runs; one applied the non-admin technique; none used
  `NOT_SUPPORTED`. Every run in every arm flushed.
- Two-run differences outside the primaries (M on L04 raised and on L02, K on L12 and on other REAL)
  sit among ~30 comparisons at n=3; read as noise.
- L02, a blocker, was found by 3 of 9 runs and 0 of 3 controls. The 2026-09-11 A/B's single control
  found it once in one run.

Cost: 9 runs $15.22, mean $1.69, 7.1-16.4 min (wt-04 spanned two transcript segments after being
re-invoked by its own background tasks; figures summed across both).
