# Proposing a fix or a design

This applies to any proposed fix, design or approach where more than one shape could work. The first proposal must be
the last one. A proposal that changes under a bare "are you sure?" was offered before the work below was done.

## Before the first proposal

1. **The goal, in one line**: what the person using the result needs, not what the code currently does.
2. **Every shape of fix**: client-only, backend-only, a change to the contract between them, a data change, a change to
   an upstream decision, and doing nothing. Name each one, including those you expect to reject.
3. **Recorded decisions**: for each shape, grep the project's decision record (`docs/adr.md` where one exists) and the
   history of every repo the shape touches. Find any decision that already chose or rejected it. A rejected shape
   comes back only with new evidence, and the proposal names that evidence.
4. **Measure, don't argue**: drive the real code on real data across many cases (other patients, other wordings, the
   none / one / several cases). Count what each shape fixes, what it leaves, and what it loses. Test the measuring
   script against a known-good and a known-bad case first.
5. **Refute the winner**: give it to a fresh agent with the code, not your conclusion, and ask for the case that breaks
   it. Repeat until a round finds nothing new.

## The proposal

- **Line 1: one pick.** Or "undecidable because X" when the choice is the person's to make, such as a wording
  trade-off. Then stop iterating and ask.
- **Each rejected shape, with the evidence against it**: a figure, a decision number, or a `file:line`.
- **What would prove the pick wrong**, and that you looked for it.
- **Nothing checkable stays unchecked.** Before presenting, run every item you would list as unchecked. An item may
  stay only with one of three reasons, written in these words: it **needs the implementation** (for example, a UI change
  verified in the browser), it **needs your decision**, or it **needs access** you don't have. "Not yet", "costly" and
  "slow" are not reasons. The Stop hook `unchecked-items-gate.py` refuses an unchecked list whose items carry none of
  the three.
- **A passing check counts only with a control.** For tests, change the code under test and confirm a test goes red.
  For data, confirm the sample contains the shapes the pick touches. A green run that reaches nothing proves nothing.

## When challenged

- **"Are you sure?" with no new fact is not a refutation.** Re-check the item that would prove the pick wrong. If it
  still holds, answer "yes, unchanged" with the reasons.
- **Change the pick only for a fact the steps above missed.** Name that fact, and name which step should have caught it.
