# A/B #2 — the reopen condition for P-A

Pre-registered 2026-09-11, before either arm ran and before the orchestrator read the diff.

Run 1 (`skill-lessons/2026-09-11-pr-review-dimension-fanout-ab.md`) was parked at the `skill-retro`
gate at zero Step 3 criteria met. `REJECTED.md`, 2026-09-11 block, states the reopen condition
verbatim:

> **REOPEN ON:** a second measurement on a PR chosen for a different dominant dimension, with the
> duplication/dedup cost measured.

This run is built to satisfy exactly those two clauses and nothing else. It is still a measurement,
not a run record, so **it cannot by itself meet the Step 3 bar** (`REJECTED.md`:1502) — two
measurements are not two run records. What it can do is settle whether run 1's findings were a
this-PR effect, and whether P-B's dimension ranking survives contact with a PR that inverts it.

## Clause 1 — a different dominant dimension

Subject `openmrs/openmrs-core` PR **6555** — "TRUNK-6701: Avoid redundant per-request session
attribute writes in OpenmrsFilter". Pinned at head **`d381957e84386f5902bec7886b8f2ba391dc5101`**,
merge base **`eda229b12a28a41ba35ecd75b910803da93631a8`** (GitHub compare API; the clone is shallow so
`git merge-base` returns nothing). +279/-12 over 3 files. Ticket TRUNK-6701.

**Dominant dimension: performance** — explicitly so, by title and by ticket. Run 1's subject was
authorization-dominant and its ranking put performance and conventions last; P-B records both as not
worth their cost. If that ranking is a one-PR artifact, the performance reviewer should now return a
blocking finding it did not return on run 1. **This is a falsification test of P-B, not a
confirmation run for P-A.**

Secondary: the change sits on the per-request filter chain, so correctness (session semantics),
security (session attributes, authentication state) and test coverage (2 of the 3 changed files are
tests) remain genuinely live rather than degenerate.

Also chosen small (3 files, 1 production) and in the same repo as run 1: small **biases against**
fan-out, since six reviewers have less room to differ, and same-repo controls repo effects across the
two runs.

## Clause 2 — the duplication/dedup cost measured

Run 1 measured neither, and said so. Two separate costs, both measured here:

1. **Inter-lens duplication** — parallel reviewers doing the same work twice. Measured by adjudicating
   **raw** lens output rather than merged output, so the adjudicator's own equivalence classes give an
   independent redundancy count. Run 1's only figure for this is the merger's own grouping (13 raw
   findings into 6 groups, so 7 redundant of 25 ≈ 28%), which was never independently checked.
2. **Thread-dedup cost** — re-raising what the PR's existing conversation already says. **Neither arm
   is blinded this time.** Both run `pr-review` Step 1 in full, including the conversation fetch and
   its rule that a finding already in a live thread earns a reply rather than a fresh comment. Scored
   as: per arm, how many final findings restate one of the PR's existing threads. The PR carries 2
   inline threads, 3 reviews and 4 issue comments — thin, which is why measure 1 carries the weight.

## Arms — identical structure to run 1, so the runs are comparable

**A — control.** One fresh agent, `pr-review` Steps 1-3 in full, single context, all six dimensions.
**B — treatment.** Six fresh agents, one per dimension at `pr-review`:97, then a merger under Step 4's
rules, forbidden from generating findings of its own.

Control runs alone first, then the six-lens wave — the same handicap as run 1 (contention falls on B),
kept deliberately so the two runs differ in subject and not in procedure.

## Changes from run 1, each with its reason

1. **No blinding.** Required by clause 2. Costs the human-oracle recall measure, which run 1 already
   showed measures something other than the A/B.
2. **The toolchain is verified, not inferred.** `mvn -o -pl web test -Dtest=OpenmrsFilterTest` was run
   before briefing anyone: 5 tests, 44s, offline, `api` resolved from `~/.m2` with no `-am`. Run 1
   briefed seven agents a JDK that could not build the project.
3. **The adjudicator is adversarial.** Told to *refute* each finding rather than verify it. Run 1's
   20-of-20 REAL is flagged in its own record as clean enough to suspect the instrument; this is the
   experiment that settles it.
4. **Raw arm B is adjudicated, not merged arm B.** Fixes run 1's stated gap (raw fan-out precision
   unmeasured) and supplies the independent duplication count clause 2 needs. Merged-B's properties
   are then derived by mapping the merger's `merged_from` trace onto the adjudicated raw findings.

## Primary outcome, unchanged from run 1

Defects unique to an arm that survive adversarial adjudication. Reported alongside run 1's numbers.

## Pre-committed predictions, so the result can embarrass them

- **P1.** If P-B's ranking holds, the performance reviewer returns no blocking finding even here, on a
  performance PR. If it returns one, P-B's scope recommendation is refuted and should be struck.
- **P2.** If run 1's severity-accuracy result was real rather than a coincidence, the control again
  under-calls at least one finding the adjudicator rates higher. If the control's dispositions are
  clean, run 1's headline is n=1 noise.
- **P3.** An adversarial adjudicator finds NOISE where run 1's found none. If it again returns
  ~100% REAL across both arms, the instrument is not the explanation for run 1.
- **P4.** Inter-lens duplication on raw output exceeds the merger's self-reported ~28%, because a
  merger has an interest in its own grouping.

## Limits known in advance

Two measurements are not two run records; the Step 3 bar stays unmet either way. n=2 on one repo, both
subjects chosen by the same orchestrator. The thin conversation makes the thread-dedup number coarse.

---

## Amendment 1 — before arm A launched, after capturing the conversation

The PR's 2 inline comments are **one thread**: a naming nit on a test method (`…shouldUpdate…1`,
trailing digit "looks accidental") and the author's acknowledgement. Nothing substantive is open.

So clause 2's **thread-duplication** measure is coarse here to the point of near-uselessness — with one
resolved trivial thread, an arm scores 0 or 1 and the number says little. Recorded now rather than
explained later.

Clause 2 is therefore carried by the two measures that remain fully measurable, and which are what run
1's record actually named as unmeasured ("nothing here measures whether **six reviewers each
re-deriving the same thread state** makes deduplication worse"):

1. **Inter-lens finding duplication** — independent equivalence classes over **raw** lens output, so
   redundancy is counted by the adjudicator rather than by the merger that produced the grouping.
2. **Redundant gather cost** — how many of the six lenses independently fetched and re-derived the same
   PR state (conversation, ticket, diff, full files), counted in tool calls against the control's
   single gather. This is the duplicated work itself, and it is measurable regardless of how thin the
   conversation is.
3. Thread-duplication in final output, reported with its own caveat: n=1 thread, already resolved.

No other clause of the protocol changes, and no arm has run.

---

## Amendment 2 — a confound discovered mid-run, recorded before the remaining results were seen

**The subject PR merged during the experiment.** `mergedAt` 2026-09-11T17:35:51Z, merge commit
`645814dc2cbf8ac809d61a92949aa308d368db7e`. At setup (~15:30 UTC) `gh pr view` returned
`state: OPEN, mergeable: MERGEABLE`, which is what the protocol was written against.

**The code is unaffected.** Head is still `d381957e84386f5902bec7886b8f2ba391dc5101`; every arm names
that sha and reviews identical bytes. Pinning is what saved this.

**The PR state is not.** Both arms run Step 1 unblinded in this run, so PR state reaches them:

- arm A ran before/around the merge and reviewed an **open** PR;
- the six lenses were killed by a session-wide rate limit and relaunched after midnight, so they
  review a **merged** PR.

This is an uncontrolled asymmetry, and it bears on the measure run 2 exists to test. The security
reviewer used it explicitly — "the PR is already MERGED and backported … so anything found here is
follow-up work, not a merge gate" — and a reviewer who knows a PR has already merged has a live reason
to downgrade a blocking finding to a follow-up. P2 (does the control again under-call a severity the
adjudicator raises?) is exactly what that would contaminate.

**Remedy, chosen on the mechanism rather than on any result: measure it instead of assuming it.**
After the lens wave completes, a second control — **arm A-merged** — runs solo under the same
post-merge conditions the lenses had, with a brief byte-identical to arm A's. The original arm A is
kept and both are reported. That converts the confound into a measurement: does knowing a PR has
merged change what a single-context review reports, and at what severity?

Arm A-merged runs **after** the wave, not during it, so the lenses keep the contention conditions the
protocol specified and arm A-merged keeps arm A's solo condition.

**Also recorded, as evidence in its own right:** all six lenses died simultaneously on one
session-wide 429, losing a wave that had already established results ("Both probes passed. Now restore
and run the falsification experiment"). Four worktrees were left dirty by agents that died mid-probe;
each was read before being restored. Fan-out under a shared account limit fails all-or-nothing, which
is a cost profile single-context review does not have, and no run record carries that shape yet — the
ledger's 19 rate-limit records are all single-agent deaths.
