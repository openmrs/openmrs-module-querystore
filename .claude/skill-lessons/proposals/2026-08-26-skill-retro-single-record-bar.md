# proposal · skill-retro — the single-record bar contradicts its own anti-pattern · drafted 2026-08-26
status: **KILLED** at Step 5 on 2026-08-26 — three blocking objections, each settling; see
        `REJECTED.md`, 2026-08-26. The deciding check it asked for WAS run and its answer is recorded
        there. Original text below, unaltered.
superseded-status: DRAFT. Not refuted — Step 5 has not run on it. The drafter is the finder, which is exactly the
        pairing Step 5 exists to break, so this must not be applied by whoever reads it next without
        a fresh agent trying to kill it first.
target: skill-retro 0.2.1 (one anti-pattern line, or one bar clause — the proposal picks between them)
found by: a human asking whether the retro could be run with one record banked, during the ticket-pool
          build. Not by a run. The occasion matters: the two rules give opposite answers about what a
          retro should DO in a situation that had just arisen, and neither reader had noticed until the
          question was asked out loud.

## The contradiction

Two statements in one file, about the same case:

- **Step 3, the corroboration bar.** "A lesson earns a proposal when **any** of these holds… it appears
  once and **cost two or more rounds or cycles** in that run."
- **Anti-patterns.** "**Don't retro a single record.** Corroboration is the only thing separating a rule
  from an anecdote; with one record, run the linter, park the observations, and stop."

With exactly one record in hand, the bar says a lesson that cost two or more rounds is actionable and
the anti-pattern says park everything and stop. They cannot both be followed.

This is not hypothetical. `2026-08-26-openmrs-module-chartsearchai-315.md` is a single un-retroed record
carrying **6 pr-harden rounds and 5 harden cycles**, and its own "Refuted by measurement" section names
lessons with round costs attached. Under the bar those are proposals. Under the anti-pattern the whole
pass stops after the linter.

## Which clause of the bar admits this proposal

The third: "it is a skill **contradicting itself**, or contradicting its own gate script — valid from a
single instance, because the contradiction is a fact about the document rather than an inference about
the world." That clause is the reason this can be filed at all from one sighting, and it is worth noting
that the clause licensing this proposal is in the same list as the clause the proposal is about.

## Proposed resolution, and the alternative it was chosen over

**Proposed: scope the anti-pattern to agree with the bar** rather than touching the bar. Something of
the shape — "with one record, propose only what the bar's second clause admits, a lesson that cost two
or more rounds or cycles, and park the rest."

Why this way round: the bar's second clause is a deliberate admission with a stated reason (a lesson can
be expensive enough in one run to be worth acting on), and Step 4 forbids deleting a measured rule
without recording the measurement that retires it. No such measurement exists. The anti-pattern's
absolute form reads like shorthand for the common case — a retro over one THIN record — and scoping it
costs nothing that its reason asks for, since it keeps refusing exactly what it was written against.

**The alternative: narrow the bar** to require two or more records always, and let the anti-pattern
stand as written. This is the stricter reading and may be the right one — a single run cannot tell a
pattern from noise, which is design rationale 3 of this very skill. What would decide it: whether any
rule now in these skills was in fact added on the strength of ONE costly run. If several were and they
have held up, the bar's clause is load-bearing and the anti-pattern is the loose end. If none were, the
clause is decoration and deleting it is the smaller change. **That check has not been run.** Whoever
takes this up should run it before choosing, and record what it found.

## What this proposal does NOT claim

- It does not claim the anti-pattern is wrong. It claims the two cannot both be obeyed.
- It does not claim which resolution is right. It states a preference and the evidence that would
  settle it, and that evidence has not been gathered.
- It does not propose a new rule, and adds no line to any skill's net length beyond a scope clause.

## Refutation record

Empty. Step 5 has not run. A refuter should ask in particular: is this a real contradiction or two rules
at different altitudes — a bar governing individual PROPOSALS and an anti-pattern governing whether a
PASS is worth running at all? If that reading holds, the fix may be a single clarifying clause rather
than a change to either rule, and this proposal is over-stated.
