# skill-retro draft — pr-review: per-dimension fan-out as a selectable mode

Scope: a targeted proposal, not a window retro. Origin: a design question — would `/pr-review`
produce better reviews if each review dimension got its own agent? Corroboration is one purpose-built
controlled experiment (`skill-lessons/2026-09-11-pr-review-dimension-fanout-ab.md`) plus two prior run
records that independently state the underlying mechanism.

Current versions: pr-review 0.17.0, pr-harden 0.21.0, harden 0.27.0, resolve-ticket 0.15.1,
skill-retro 0.2.3, ticket-pool 0.21.0.

**Nothing here has been applied.** No skill file was edited, nothing was committed or pushed. This
stops at the Step 5 gate by design.

---

## P-A · Give `pr-review` the per-dimension fan-out shape, as an opt-in mode distinct from the refutation pass

**Origin.** The A/B record above. The question behind it: `pr-review:121` already treats "a fan-out of
independent finders" as a *circumstance* that makes the refutation escalation unnecessary, but the
skill never says fan-out is a thing you can choose, or what it buys, or how to configure it. A fresh
session reading Step 3 cannot tell that the two shapes have different payoffs.

**Corroboration bar: criterion 1 — two or more run records**, for the mechanism that a
dimension-scoped reviewer surfaces what an unscoped sweep walks past:

- `2026-09-09-openmrs-module-chartsearchai-397.md:303` — "**The quality lens found eleven substantive
  items in a class three other lenses and five correctness passes never touched**: what nine review
  passes LEAVE BEHIND."
- `2026-09-03-openmrs-module-chartsearchai-336.md:18` — "My own ADR paragraph had reasoned only about
  `StatedInteractionChips` and concluded the sibling was decided differently — **the reuse lens
  measured the one I had missed**." Recorded as blocking-equivalent.

Both are `harden` Phase 2 lenses, not `pr-review`, so they corroborate the mechanism rather than this
skill's version of it. The A/B is the direct evidence for the `pr-review` shape and for the
configuration below.

**The edit.** In `pr-review` Step 3, rename `### When an adversarial refutation pass is worth it` to
`### When one context is not enough`, leave the refutation paragraph and its four conditions
untouched, extend the second condition by one sentence, and add two paragraphs after the existing
closing paragraph.

Second condition, appended:

> Fanning out is a choice, not only a circumstance: the paragraph below says when to make it.

Added:

> **A different heavier shape: generating findings from separately-scoped contexts** — one reviewer
> per dimension, then a merge pass under Step 4's rules. Measured on a pre-registered A/B with
> blinded adjudication (`skill-lessons/2026-09-11-pr-review-dimension-fanout-ab.md`). It did not buy
> discovery: the three blockers on that PR were found both ways. It bought severity accuracy, where
> the single context under-called a privilege escalation as a suggestion, and it bought checks the
> single context had to skip, because a narrow scope leaves budget to run the expensive one. Cost was
> roughly 6.5× the tokens for 1.6× the wall clock, a parallel wave costing its slowest member. Reach
> for it when the dimensions are separately load-bearing and the checks a finding needs are
> expensive, and scope it to solution fit, correctness, test coverage and security — on that PR
> performance and conventions each returned one non-blocking finding plus nits the merge discarded.
> The merge pass may drop, merge, downgrade and re-anchor, but **not add**: a merger that generates is
> one more reviewer, and the review stops being attributable to what produced it.
>
> Neither shape is reachable when `pr-review` is itself the delegated agent — `pr-harden`'s reviewer,
> and `resolve-ticket` through it — where spawning subagents is forbidden. There, argue the dimensions
> in one context, which is what the escalation conditions above already assume.

**Step 4 — what this prunes.** It subsumes the standalone reading of `pr-review:121`, which described
fan-out only as a circumstance and left the reader no way to select it. Net growth is about twelve
lines and the justification is conflation: the section currently presents one escalation shape, and
the run shows there are two with different payoffs — refutation re-verifies candidates the same
context generated and was measured to add nothing (955d961), while scoped generation changes what
gets generated and what can be verified. A skill that conflates two instruments makes a fresh session
reach for the wrong one, and that is the failure this pays twelve lines to prevent.

**Where this proposal is weakest**, stated so the gate does not have to find it:

1. **The corroboration class is new.** The bar in Step 3 is written for run records; this rests
   primarily on a purpose-built experiment. The two run records above corroborate the mechanism but
   neither is a `pr-review` run. If the gate holds that an experiment is not a run record, this drops
   to *observed, not yet actionable* with the A/B attached, and that is a defensible outcome.
2. **n=1, on a PR chosen because it favoured the treatment.** A dimension effect and a this-PR effect
   are not separable from one comparison.
3. **The adjudicator rated 20 of 20 findings REAL.** That is clean enough to suspect the instrument.
   No second adjudicator was told to refute rather than verify.
4. **The cited cost ratio is a single measurement** and the kind of number Step 4 warns about
   ("do not add a count a later reader must re-measure"). It is in the proposed text as an
   order-of-magnitude signal; if the gate objects, cut it to "several times the tokens for
   comparable wall clock" and leave the figures in the record.
5. Nothing measured whether fan-out degrades the Step 1 deduplication work, because both arms were
   blinded to the PR conversation. Six reviewers each re-deriving the same thread state is the
   obvious place this costs something.

---

## P-B · Record the six-dimension configuration as measured-and-not-recommended, so it is not re-proposed

**Origin.** The A/B ran the literal proposal — one agent per dimension named at `pr-review:97`, all
six. Two of the six paid for a sixth of the cost apiece: performance returned one non-blocking unique
defect, conventions one non-blocking unique defect plus three nits the merger discarded.

**Corroboration bar: criterion 2 — one run, and the cost is the point.** The full configuration cost
1.26M tokens where the four-lens subset would have covered every blocker and both severity
corrections. There is also a prior sighting of the same shape being trimmed in practice:
`2026-09-03-…-336.md:29` — "on cycle 2 I ran two lenses rather than four … Labelled as a deviation in
the report", and `2026-08-30-…-250.md:28` for an efficiency lens skipped as a labelled reduction.

**The edit.** A `REJECTED.md` entry, no skill text:

> **Per-dimension fan-out at the full six-dimension list.** Measured once
> (`2026-09-11-pr-review-dimension-fanout-ab.md`): performance and conventions each returned one
> non-blocking unique defect, and every nit either produced was dropped by the merge pass, at
> ~210k tokens apiece. The shape that paid is solution fit + correctness + test coverage + security.
> **REOPEN ON:** a PR where a performance or conventions reviewer returns a blocking finding the
> other four missed.

---

## P-C · Standing counterweight: the A/B is positive evidence for Step 1's verify-claimed-fixes rule

**Origin.** Not a change. The run produced unusually direct evidence for a rule already in the skill,
and the ledger's convention is to record that so a later pass cannot weaken it cheaply — the shape of
`REJECTED.md`'s existing *"The confirming-cycle rule, as POSITIVE evidence"* entry.

**The edit.** A `REJECTED.md` entry, no skill text:

> **`pr-review` Step 1's "verify claimed fixes against the head — 'fixed' is a claim, not evidence",
> as POSITIVE evidence.** On core#6551 two review threads had been closed as fixed by a maintainer and
> the author. Both repairs were defective and both arms of the fan-out A/B caught it independently:
> the regression test a maintainer asked for and accepted passes with the fix it guards reverted, and
> the comment that closed the session-cache thread describes stale data where the real behaviour is a
> `TransientPropertyValueException` that aborts the transaction, reachable from the PR's own new test
> plus one ordinary query. Two humans and a review bot accepted both. **Any future proposal to soften
> the re-verification of claimed fixes has to pass this.**

**Step 4 — what this prunes.** Nothing, and it adds nothing to a skill. It is corroboration parked
where the next retro will read it.

---

## Not proposed, recorded as observed

- **The orchestrator's own toolchain error** (briefing JDK 8 from a property evaluated in a stale
  checkout, while the reviewed sha sets `maven.compiler.release=21`) is an instance of
  `pr-review:88`'s existing rule about a hit proving existence but not the path. One sighting, in a
  brief rather than a review. No edit; it is in the record's *Refuted* section.
- **Whether the merge pass should be a separate agent at all.** The A/B's merger dropped a real
  nit-level defect that arm A kept, which is defensible under "fewer, sharper" but is also the only
  place merged arm B lost to raw arm B. One sighting, and the alternative (the orchestrator merging)
  was not run.
