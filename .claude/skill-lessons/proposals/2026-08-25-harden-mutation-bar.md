# proposal · harden — the unpinned guard · drafted 2026-08-25, revised after refutation
status: REVISED after a Step 5 refutation pass. One proposal, ready for Step 6.
target: harden (one existing bullet, at cycle close)
history: the first draft proposed two new bullets, one per phase. Refuted — see **The refutation
         record** below. It rested on a false premise about harden's own text and would have opened
         two new homes for an obligation that already exists.

## The lesson

A guard or clause the change ADDS is not pinned by anything: deleting it, swapping its arms, loosening
its comparison or rewriting it equivalently leaves the whole suite green. The author does not find
this; a fresh agent does, by running the mutation.

## Corroboration (Step 3)

Bar met: **appears in two or more run records** — six of the seven run records in the corpus as it
stood on 2026-08-25.

Method, so the next reader re-derives rather than trusts a tally: extract the
`## Raised by a fresh agent, missed by the author` section of each record and grep it for
green-under-mutation language (`green`, `unpinned`, `pinned by NOTHING`, `unguarded`, `passed all`,
`no test held`, `left all`). Then split the surviving bullets by their leading tag: `[harden …]`
versus `[r<n>]` / `[pr-harden r<n>]`. Re-run it; do not carry these words forward as a measurement.

Two shapes re-derived independently at Step 5 and held: the findings split about evenly between ones
harden caught before the PR and ones that leaked to a review round at about a round each, and every
harden-side catch that names its phase names **Phase 2** — none names Phase 1. `#298` returns nothing
under this method; its nearest bullet is an equivalent-rewrite finding and its green-under-mutation
language sits in a different section.

## What the pipeline obliges today

Four homes, none of which reaches the lesson:

- `harden:180`, at cycle close — "Every applied change still needs its evidence: verified by build or
  test, and where it fixes a behavior, checked by reverting it and confirming the failure." A
  deletion-mutation obligation already, and *"confirming the failure"* already makes a green suite a
  finding. What it lacks is reach: a guard the change ADDS is not always a behaviour fix, and the
  equivalent-rewrite mutation class is absent.
- `harden:42`, Phase 1 — name the test; it must fail on the pre-change code. Not a mutation.
- `pr-harden:203` — mutate when you NAME a guard in prose. Triggered by an attribution claim.
- `pr-harden:207` — prove an equivalent rewrite reddens, scoped to a guard whose subject is text or
  shape, which that skill defines as a source scan, a class-file scan, an architecture guard or a
  build-time assertion.

`harden:269` mentions mutation too ("prefer *mutate the line and read the failures* to any tally"),
but in service of the unverifiable-claims rule. Untouched by this proposal.

## The proposed edit

One bullet replaced, at `harden:180`. Before:

> - Every applied change still needs its evidence: verified by build or test, and where it fixes a
>   behavior, checked by reverting it and confirming the failure.

After:

> - Every applied change still needs its evidence: verified by build or test, and where it fixes a
>   behavior, checked by reverting it and confirming the failure. **Where it ADDS a guard or clause,
>   the same check is owed on that — deleted, its arms swapped, its comparison loosened, or rewritten
>   in a semantically equivalent way** — because a clause the suite never discriminates is one the next
>   change can remove for free.

Nothing else moves. No new bullet, no phase obligation, no brief change.

## Step 4 — what it subsumes, retires or renders stale

It **extends** `harden:180` rather than adding a home, which is the whole revision: the obligation
already lives in four places and a fifth and sixth would be free to drift from them.

It retires nothing. Net growth is under three lines. The justifying sentence: the two gaps it closes
are reach and mutation class, both on a line that already exists, and the corpus shows the uncovered
half being discharged by whichever fresh agent happens to think of it.

Three things it deliberately does not do, each because a citation settled it at Step 5:

- **It does not retire `harden:42`.** The subsumption argued in the first draft is unsound: mutating
  post-change code proves the new test discriminates the new clause, while failing on pre-change code
  proves it discriminates the DEFECT, and for a relocation or a re-ranking the two come apart. Step 4
  also forbids deleting a measured rule on an argument rather than a measurement.
- **It does not scope the equivalent-rewrite class to text or shape.** That qualifier, imported from
  `pr-harden:207`, would scope out the newest finding this proposal rests on, whose mutation replaces
  an ordinary production call with an untrimmed one.
- **It leaves `pr-harden:207` narrower than the new clause.** The asymmetry is legible and deliberate
  — different actor, different moment — but it is now a place two homes can drift. Named here rather
  than fixed, because widening a second skill is a second proposal.

## What it does not reach — named rather than denied

- **Of the leaked findings, it reaches two or three, not half.** It would not have caught a mutation
  that deletes a loop advance (neither a guard nor a clause, and the real cause was a fixture that
  could not express the case, which `harden:46` already owns); one whose property is section ORDER and
  whose mutation is a reorder; or one that is the attribution defect `pr-harden:203` owns.
- The mutations an author runs are the ones that author imagined. The ones that leak may be exactly
  the ones they could not.
- It says nothing about guards in code the change did not touch.
- Placement resolves repetition rather than a rule doing it: `harden:180` is a cycle-close obligation
  over the changes that cycle applied, so it does not multiply by pass count the way a Phase 1 or
  Phase 2 bullet would.

## The refutation record

Seven blocking objections, five settling. What changed: the corroboration claim ("every record" was
false, and is the universal grammar Step 4 forbids); the reach claim; the text-or-shape qualifier; the
retirement of `harden:42`, dropped; and the whole two-bullet structure, folded into `harden:180`.

Deleted with it: the cost argument that mutations belong in Phase 2 because Phase 1 repeats most. No
record carries a phase-level build attribution, and the one measurement that could is explicit that
its phase boundaries do not sum. The measurement also lists build share as live headroom, so an edit
that adds builds owes a cost estimate rather than a placement argument. Cycle-close placement makes
the question moot; it is not answered.

Two objections were left OPEN and are not resolved by this revision: whether two or three leaked
rounds across the corpus justify the clause at all, and whether the phase argument was a cost argument
dressed as a design one. If Step 6 is unwilling to accept the clause on the first, the correct outcome
is to park this as *observed, not yet actionable* — the count is recorded and the lesson is not lost.

**For the record's own sake:** the first draft's false premise was about a line in the file it edits,
and its author had read that file. That is the class `REJECTED.md` already parks — a proposer not
verifying its own citations. It was caught at Step 5 again, so the reopen condition there is still
unmet, but the count moves.
