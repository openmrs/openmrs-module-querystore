# pr-review stockmanagement #79, round 1 (head 25b913e8), raised by wikumChamith 2026-11-25 (r4692999416, review 6055207630)
- PR: https://github.com/openmrs/openmrs-module-stockmanagement/pull/79 (O3-6340, public). Missed in round 1 at 25b913e8.
- What was missed: `HelperFormattingTest.shouldPreserveQuantityDisplayAndHalfEvenRounding` builds its own `DecimalFormat`,
  so it tests the JDK, not the module. Wikum asked "Do we need to write tests for others' code?" and to drop the whole
  test class, and said the PR, "a like-for-like swap of helper types", needs no new tests. The author dropped
  `HelperFormattingTest` (ff9e91d), then the `InventoryTotalsTest` added after round 1's comment A: "so the PR is just
  the type swap now".
- Seen and set aside: the whole-PR agent's finding 1 ("It is testing the JDK's `DecimalFormat`, not the module") and
  Greptile's surviving sub-point. Round 1 set it aside because the quantity call sites keep their arguments and the
  pattern is pinned through the constant.
- Also: round 1's comment A (the order of `total` after the `Pair` swap) was a question; the author answered it with a
  `LinkedHashMap` grouping, a new `totalByLocationStockItem` and `InventoryTotalsTest` (bf636ca), and Wikum's "no new
  tests" took that test back out.
- Rule nearest to it: Step 2's "Does it do only that?" (account for each hunk as required by the ticket or not). The round
  read the added test only for coverage, whether it drives production code, and judged the gap harmless.
- Sessions: noticed in 7abec84a-e793-4dfb-aa6b-79a4f7a18e1d on 2026-11-26 while building a probe from #79's thread; the
  round-1 session was b852290a-cb88-4be1-9def-44a08b2df86a.
