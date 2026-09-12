# A/B: does per-dimension agent fan-out improve `pr-review` quality?

Pre-registered 2026-09-11, before either arm ran and before the orchestrator read the diff.

## Hypothesis

H1 (treatment wins): scoping each of `pr-review`'s six named review dimensions to its own agent
generates **real, verified findings that a single-context sweep misses**, without inflating the
unverified/noise rate.

H0: the two arms return substantially the same finding set, and fan-out is recurring cost for no
quality gain — the outcome that killed the always-on adversarial gate at 955d961.

## Subject

- `openmrs/openmrs-core` PR **6551** — "TRUNK-6669: Add support for restricting users to assigned locations"
- head **`b7cd909d245086fa10691496f88e5ec6971b77c2`** (pinned; both arms name this sha, so a new push cannot void the run)
- base `origin/master`; +216/-6 over 8 files; ticket TRUNK-6669 (no GitHub `closingIssuesReferences`, so Step 2 must reach JIRA)
- chosen because authorization semantics + a liquibase changeset make all six dimensions genuinely
  load-bearing: the condition most favourable to the treatment. If fan-out does not win here it
  does not win.

## Arms

**A — control (incumbent).** One fresh agent, `pr-review` Steps 1-3 in full, single context,
including the Convergence rule (sweep until a pass finds nothing substantive). Covers all six
dimensions itself.

**B — treatment.** Six fresh agents in parallel, one per dimension named at `pr-review/SKILL.md:97`
(solution fit, correctness, test coverage, security, performance, project conventions), each with
the identical gather/verification/hazard block. Then one fresh **merger** applies Step 4's precision
rules. The merger may drop, merge, downgrade or re-anchor; it may **not** generate findings of its
own, or it becomes a seventh finder and confounds the arm.

Both arms emit the same strict JSON schema. Nothing is posted to GitHub.

## Symmetric deviations (identical in both arms, recorded)

1. **Neither arm reads the PR's existing review conversation.** `pr-review` Step 1 mandates it; here
   the 8 existing human inline comments are the independent recall oracle, captured unread at
   `oracle-human-review-comments.json` before the run. Suppressing the read costs both arms their
   dedup behaviour and neither arm's generation, which is what is being measured.
2. Nothing is posted or staged (`pr-review`'s default is already consent-gated).
3. No agent may `mvn install` (shared `~/.m2`), spawn subagents (nested delegation killed a reviewer
   on `pr-harden`'s first run), or pass a `model` override.

## Handicap, stated up front

The control runs **alone**, then the six lenses run as one wave. Six concurrent maven test runs
contend with each other; the control contends with nothing. This biases **against** the treatment.
So: a treatment win under the handicap is strong evidence; a treatment loss is inconclusive on
verification depth and must be reported as such, not as a refutation.

## Primary outcome

For each arm, the count of findings that are **unique to that arm** and survive independent
verification by a blinded adjudicator. H1 survives only if arm B's unique-and-real count exceeds
arm A's by more than one finding.

## Secondary outcomes

- **Recall vs the human oracle**: of the 8 maintainer inline comments, how many did each arm
  independently reach?
- **Precision**: per arm, REAL / PLAUSIBLE-UNVERIFIED / NOISE split.
- **Volume**: total findings per arm, and whether B's merger actually dropped material (a merger
  that forwards everything is the predicted failure mode — `pr-review:165`, "fewer, sharper").
- **Blocking-call agreement** between arms.
- Cost: agents spawned, wall clock per wave.

## Bias controls

- The orchestrator wrote both briefs **without reading the diff**, so no orchestrator-held finding
  could leak into a lens brief. Lens briefs name a dimension and nothing PR-specific.
- The human oracle was captured before the run and stays unread until scoring.
- Adjudication is by a fresh agent given both finding sets **with arm labels stripped and order
  shuffled**, told only to verify each finding against the sha. It is not told what the experiment
  tests, nor which set is which.
- n=1 PR. Same bar as the 955d961 precedent, and the same limit: one PR cannot separate a dimension
  effect from a this-PR effect. Recorded as such.

## Pre-committed reading of the result

- B wins by >1 unique-real finding → propose fan-out to `skill-retro`, as an opt-in top layer for
  interactive `/pr-review` only, because `pr-harden:119` forbids the nested case.
- Arms substantially tied → H0; record in `REJECTED.md` so it is not re-proposed without new evidence.
- B floods (volume up, unique-real flat) → the precision objection is confirmed; record that too.

---

## Amendment 1 — 2026-09-11, after arm A returned, before arm B launched

**The toolchain hint in both briefs is wrong.** Both briefs say to export
`JAVA_HOME=.../openlogic-openjdk-8...` on the ground that `javaCompilerVersion` is `1.8`. The pom
**at the PR's sha** sets `maven.compiler.release=21` (`pom.xml:813`); JDK 21 is correct and is the
machine default. Arm A found this itself, mid-run, and reported it.

Cause, recorded because it is the same defect this project's skills warn about: the orchestrator ran
`mvn help:evaluate -Dexpression=javaCompilerVersion` in the **main checkout**, which sits on a stale
local branch, rather than in the worktree at the reviewed sha. The property exists there and
evaluates to 1.8; it does not govern the build at this sha. A value proved present somewhere is not
a value proved on the path in question.

**Decision: the brief is NOT corrected for arm B.** Keeping it byte-identical preserves the
pre-registered symmetry and holds the per-agent condition exactly equal to what arm A faced.
Correcting it would give B's six agents a smoother path than A had, which cancels part of the stated
handicap in an unmeasured direction — a known bias direction is worth more than a smaller unknown
one. Arm A recovered from it at the cost of some in-run time.

**Scoring consequence:** if a lens fails to recover and therefore cannot verify, that shows up as
`could_not_verify` and its findings degrade to questions. That is a per-agent recovery difference to
report, not a design flaw to hide — and if it materially depresses arm B, the run is inconclusive on
verification depth, exactly as the handicap clause already says.

## Arm A result, recorded before arm B launched (so it cannot be revised afterwards)

9 findings over 6 passes in 22.6 min, 194.7k agent tokens, 66 tool calls: 2 blocking, 1 question,
4 suggestions, 2 nits. By dimension: correctness 3, test coverage 2, project conventions 2,
security 1, performance 1, **solution fit 0**. 5 entries in `could_not_verify`.
Worktree verified clean at the pinned sha afterwards.

---

# RESULTS

Adjudicator: 20/20 findings **REAL**. Zero NOISE, zero PLAUSIBLE_UNVERIFIED, zero REAL_OVERSTATED,
`anchor_wrong` on 0 of 20. The precision axis is flat — it does not separate the arms.

## Primary outcome — defects, using the adjudicator's own equivalence classes

20 findings collapse to **12 distinct defects**.

| | defects |
|---|---|
| found by both arms | 7 |
| arm A only | **1** |
| arm B only | **4** |

Pre-registered threshold was B − A > 1. Observed **4 vs 1**. **H1 survives.**

Caveat that sharpens it: arm A's one unique defect (the `LOCATION_ID` double-duty constant) *was*
found by arm B's conventions lens and **dropped by arm B's merger** with the other three nits. So the
raw lens set covered 12/12 defects, merged arm B covered 11/12, arm A covered 8/12. The drop was of a
nit the adjudicator also rated `nit`, so it is defensible under "fewer, sharper" rather than a loss.

## The result that matters more than the count — disposition accuracy

| arm | filed blocking | adjudicated blocking | under-called | over-called |
|---|---|---|---|---|
| A | 2 | 3 | **1** | 0 |
| B | 3 | 3 | 0 | 0 |

Arm A filed the `getAllowedLocations` privilege hole as a **suggestion**; the blinded adjudicator
rated it **blocking**. Arm B filed it blocking, having proved it with a runtime probe plus positive
control (same caller: `APIAuthenticationException` on `getUser(1)`, location data returned from
`getAllowedLocations`) and traced the mechanism to `AOPConfig` advising `@Service` only while
`HibernateUserDAO` is `@Repository`.

**All three real blockers were found by both arms.** Fan-out discovered no blocker the control missed.
Its four unique defects are all non-blocking (one performance suggestion, one security suggestion, one
question, one conventions suggestion).

## Verification depth — the mechanism that actually differed

Arm B resolved **3 of arm A's 5 `could_not_verify` entries**:
- arm A could not run `DatabaseUpdaterDatabaseIT` (integration profile, real DB) and checked the
  916→917 bump arithmetically; the test-coverage lens ran it and confirmed 917.
- arm A recorded the liquibase changeset as "not exercised by anything I ran"; the test-coverage lens
  proved it *is* — renaming its `location_id` makes `ValidateHibernateMappingsDatabaseIT` fail, and
  that IT runs in CI — and killed the candidate finding instead of raising it.
- arm A could not reproduce the in-session abort in a committing transaction and shipped it as a
  `question`; the correctness lens isolated the cause by mutation (removed the native delete, the
  exception still fired) and upgraded it to blocking.

The mechanism is not "more eyes see more". It is that a narrower scope leaves enough budget to run
the expensive verification a generalist must skip.

## Cost

| | tokens | tool calls | wall clock |
|---|---|---|---|
| arm A | 194,690 | 66 | 22.6 min |
| arm B — 6 lenses | 1,108,186 | 371 | 25.2 min (wave = slowest member) |
| arm B — merger | 148,008 | 17 | 10.4 min |
| **arm B total** | **1,256,194** | **388** | **35.6 min** |
| ratio B:A | **6.45×** | 5.9× | 1.58× |

Per defect: A 24.3k tokens, B 114.2k. Marginal cost of arm B's additional defects ≈ 354k tokens each.

Adjudicator (instrument, charged to neither arm): 183,588 tokens, 64 tools, 21.5 min.

## Per-lens yield, for sizing a real configuration

| lens | unique defects | blocking contribution | notes |
|---|---|---|---|
| security | 2 | got the under-called privilege hole right | runtime probe with positive control |
| test coverage | 0 | 1 (untested re-read) | resolved 2 of A's could-not-verify |
| correctness | 0 | upgraded A's question to blocking | mutation-isolated the cause |
| solution fit | 0 | duplicated the FK blocker | but proposed the better fix (`onDelete="CASCADE"`, in-tree precedent) |
| performance | 1 | 0 | 4 suggestions, all measured, none blocking |
| conventions | 1 | 0 | 1 suggestion + 3 nits; merger dropped every nit |

## Reading, against the pre-committed options

B wins by >1 unique-real defect, so the pre-committed action applies: **propose to `skill-retro` as an
opt-in top layer for interactive `/pr-review` only**, since `pr-harden:119` forbids the nested case.

But the shape of the win argues for a smaller configuration than the one tested. The value sat in
security, test coverage and correctness (disposition accuracy and verification depth), plus
solution fit for the approach-level alternative. Performance and conventions contributed one
non-blocking defect each and four nits the merger discarded — at a sixth of the cost apiece.

## Threats to this result

- **n=1 PR**, and one chosen to favour the treatment. It cannot separate a dimension effect from a
  this-PR effect.
- **The adjudicator rated 20/20 REAL**, which is suspiciously clean. Either both arms were genuinely
  disciplined (the verification rules are strict, and both arms' own reports are full of killed
  candidates), or the adjudicator was insufficiently adversarial. A second adjudicator told to refute
  rather than verify would settle it; that was not run.
- **The handicap ran against B** (control alone, lenses contended) and B still won, so the direction
  is safe — but B's cost advantage in wall clock would shrink on a quiet machine.
- **Both arms were blinded to the PR conversation**, so neither did the dedup work a real round does.
  Nothing here measures whether fan-out makes dedup worse, and six lenses each re-deriving the same
  thread state is the obvious place it would.
