# measurement · pr-review per-dimension fan-out A/B #2 · openmrs-core PR 6555 · 2026-09-12
outcome: measurement only — built to answer the reopen condition the 2026-09-11 gate attached to P-A
protocol: pre-registered before either arm ran and before the orchestrator read the diff; two amendments, both recorded before the results they bear on
artifacts: `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-querystore/9f0167e9-18df-4e14-bedd-eab4aa62b9c2/scratchpad/ab-6555/`
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-querystore/9f0167e9-18df-4e14-bedd-eab4aa62b9c2.jsonl

Answers the condition `REJECTED.md` (2026-09-11) attached to the parked P-A: *"a second measurement on
a PR chosen for a different dominant dimension, with the duplication/dedup cost measured."* Both
clauses satisfied. **Still a measurement, not a run record**, so it cannot meet the Step 3 bar either
(`REJECTED.md`:1502); what it can do is say whether run 1 was a this-PR effect.

## Design

Subject `openmrs/openmrs-core` PR **6555** (TRUNK-6701, "Avoid redundant per-request session attribute
writes in OpenmrsFilter"), head `d381957e84386f5902bec7886b8f2ba391dc5101`, merge base
`eda229b12a28a41ba35ecd75b910803da93631a8`, +279/-12 over 3 files. **Dominant dimension: performance**
— one of the two dimensions P-B recorded as not worth their cost, so the subject was chosen to
falsify P-B rather than to confirm P-A. Small and same-repo: small biases against fan-out, same-repo
controls repo effects across the two runs.

Arms as in run 1 (control alone, then six lenses at `pr-review`:97, then a merger forbidden to
generate). Four changes, each fixing something run 1 or its gate flagged: **no blinding** (both arms
run Step 1's conversation read, which clause 2 requires); **the toolchain verified before briefing**
(`mvn -o -pl web test -Dtest=OpenmrsFilterTest`, 5 green, 44s, offline — run 1 briefed seven agents a
JDK that cannot build the project); **an adversarial adjudicator**, told to refute rather than verify;
**raw arm B adjudicated** rather than merged, fixing run 1's stated gap.

## Measured — the four pre-committed predictions

**P1 CONFIRMED. P-B survives falsification.** On a PR wholly about performance, the performance lens
returned 0 blocking and 0 suggestions; its single question duplicated the control's own finding. The
dimension ranking is not a one-PR artifact.

**P2 INDETERMINATE.** Zero blocking findings in any arm, so no severity existed to under-call. Run 1's
headline (the control filing a privilege escalation as a suggestion) cannot replicate on a clean PR.
The subject choice biased against fan-out and against measuring run 1's main effect together.

**P3 CONFIRMED, and it damages run 1.** The adversarial adjudicator returned **4 of 17
REAL_OVERSTATED and 4 `anchor_wrong`** (four findings anchored at `OpenmrsFilter.java:64`, outside
both diff hunks) where run 1's confirmation-stance adjudicator returned 20 of 20 REAL with zero of
each. **Run 1's precision result was an artifact of how its instrument was briefed**, exactly as run
1's own record suspected. Both runs still returned **0 NOISE**: these arms observe real things and
misjudge severity and anchoring, rather than inventing.

**P4 REFUTED, in the opposite direction.** The prediction was that a merger under-reports duplication
because it has a stake in its own grouping. The independent adjudicator found **less** duplication
than the merger claimed: 10 lens findings over **7** distinct defects (3 redundant, 30%) against the
merger's 5 (5 redundant, 50%). The merger **over**-merged, and in one case folded a claim together
with its own refutation — it grouped "the fixture is unnecessary" with "the fixture is in the wrong
place" although one of those findings explicitly asserts the other is not a problem.

## Outcome

17 findings over **8 distinct defects** by the adjudicator's equivalence classes.

| | defects |
|---|---|
| both arms | 3 |
| lenses only | **4** — 3 actionable, 1 dropped |
| control only | 1, adjudicated REAL_OVERSTATED → nit |

One defect was found **five** times (the dead `@AfterEach logout()`: both controls plus three lenses,
by three different methods), one four times, one three times.

**The strongest result for fan-out in either run:** the adjudicator's own pick for most serious defect
in the PR was found by a lens alone — *nothing pins the two guards as independent*. It reproduced the
mutation itself: replacing both `setSessionAttributeIfChanged` calls with one keyed only on the
username leaves all five tests green, so a later refactor could reintroduce the very redundant write
the ticket was filed to remove, with no test noticing.

**The strongest-looking lens-only finding was destroyed.** The security lens's clustering question —
that dropping the per-request writes stops replicating in-place `UserContext` mutations — died on a
probe: `UserContext` holds a non-transient, non-`Serializable` `authenticationScheme`
(`NotSerializableException` reproduced), so that state was never replicated before this PR either.
Observation true, consequence impossible, dropped.

## Clause 2 — the duplication and dedup cost, which run 1 could not measure

- **Inter-lens finding duplication: 3 redundant of 10 (30%)** by independent adjudication.
- **Redundant dedup work: seven-fold.** All 13 findings from both arms were delivered `fresh`; none
  was routed into the PR's one existing thread. That is correct — the thread was a naming nit
  genuinely fixed at head, which earns silence. The cost is that **all seven agents independently
  fetched that conversation, independently verified the same claimed fix against the head, and
  independently reached the same silence**. One trivial dedup decision, derived seven times.
- Thread-duplication in final output: 0 for both arms, on 1 already-resolved thread. Coarse, as
  Amendment 1 said in advance.

## Cost

| | tokens | tool calls | wall clock |
|---|---|---|---|
| control (merge-blind) | 154,668 | 40 | 14.7 min |
| control (merge-aware) | 157,369 | 46 | 15.4 min |
| 6 lenses | 851,000 | 240 | 15.4 min (wave = slowest) |
| merger | 126,796 | 18 | 7.6 min |
| **arm B total** | **977,796** | **258** | **23.0 min** |
| ratio B:control | **6.32×** | 6.5× | 1.6× |

Run 1's ratio was 6.45×, so the multiple is stable across two subjects. Adversarial adjudicator,
charged to neither arm: 147,967 tokens, 51 calls, 23.4 min. **Not counted anywhere above: a first
six-lens wave that died entirely (below), whose tokens were spent and whose output was lost.**

## A cost neither run anticipated — fan-out fails all-or-nothing

**All six lenses died simultaneously on one session-wide 429.** Not one flaky agent: the whole wave,
mid-work, with results already established and unwritten ("Both probes passed. Now restore and run the
falsification experiment"). Nothing was written; the wave was re-run from zero on a fresh limit
window. Four of eight worktrees were left dirty by agents that died mid-probe — two with mutated
tracked files, three with probe classes — and each was read before being restored.

The ledger's 19 rate-limit records are all **single-agent** deaths, where a run loses one agent and
retries it. This is a different shape: **a coordinated wave sharing one account limit loses everything
at once, and a single-context review structurally cannot**. The retry cost a full second wave.

## Refuted — claims this run's own orchestrator made and the run broke

- **"The PR is open."** Pinned as `state: OPEN, mergeable: MERGEABLE` at setup (~15:30 UTC); it merged
  at 17:35 UTC mid-experiment (merge commit `645814dc`). The code was unaffected because every arm
  names the pinned sha — that is what pinning is for — but the arms saw different PR *state*, which
  reaches them because this run is unblinded. Recorded as Amendment 2 before the remaining results
  were seen, with the remedy of measuring it rather than assuming it.
- **"Knowing a PR has merged will soften dispositions."** The remedy measured it: a second control run
  solo under post-merge conditions found the **same three defects** as the merge-blind control plus
  one extra nit, with an identical severity profile (0 blocking both). The confound was real and its
  effect was not detectable here.
- **P4**, above: the merger over-merged rather than under-merged.

## Limits

- Two measurements are not two run records; the Step 3 bar is unmet either way.
- n=2, one repo, both subjects chosen by the same orchestrator, one of them explicitly to favour
  fan-out and one explicitly to disfavour it.
- Run 2's subject was clean enough (0 blocking anywhere) that P2 could not be tested at all.
- The per-arm overstatement rates are close and the samples tiny: lenses 2 of 10, controls 2 of 7.
  Nothing here supports a claim that either arm is more disciplined.
