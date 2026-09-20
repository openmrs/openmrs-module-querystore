# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #447 / PR 456 · 2026-09-17
outcome: converged
rounds: 1   cycles: 8   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-447/509ecd87-f685-4b22-a356-02ce44b925e3.jsonl

## Refuted by measurement
- "aboveFloorRulesAgainst has no call site in addActiveOrderPairInteractions, so the sibling arm is out of scope" -> pairKeyNames is called from BOTH arms (:5708 and :6929), so the join is reached transitively; leaving it scanning would have left an N(N-1) path inside the ticket's own arm · cost: gate pass 1 (pre-code)
- "a nameIndex(Collection) overload is the natural shape" -> ADR Decision 54 measured that exact shape reinstating a full walk as an overload RESOLUTION with the suite green; renamed to nameIndexOf · cost: gate pass 1 (pre-code)
- "the per-pair scan is the cost" (asserted) -> measured: 6,575,839 rule reads at N=95, walks 9,142 against the 8,930 the pair loop predicts · cost: 0, measured before coding
- "N=195 is the ceiling a 1000-char question can resolve" -> a marginal-coverage packer reaches 407; an independent reviewer's packer reached 375 · cost: 1 cycle
- "a cap buys nothing measurable" -> then "a cap would save most of that 92 ms" -> both false; a cap cannot touch the ~54 ms spent RESOLVING the rows it would cap · cost: 2 cycles, the second one blocking
- "the screening arm's cost profile is never worse" (asserted from the early break) -> measured strictly better at every chart: 43 orders 114 walks -> 88 · cost: 0

## Raised by a fresh agent, missed by the author
- [c1] The join returned its own mutable list where the replaced scan built a fresh one per ask; pairKeyNames takes get(0) and bestRule gets the same object · non-blocking · cost: 0
- [c2] Nothing distinguished the two-leg UNION from a name-leg-with-ATC-FALLBACK — consulting the code only where the token missed left the WHOLE build green · non-blocking · cost: 0
- [c2] candidates deduped by EQUALS while the field 60 lines above refuses to rest on that default for the same reason · non-blocking · cost: 0
- [c2] entriesCodedBy's normalisation was unexercised: every rule ATC in every fixture is already trimmed and upper-case · non-blocking · cost: 0
- [c3] ADR Decision 103 had no table-of-contents entry, in the document the javadoc designates as its one home · non-blocking · cost: 0
- [c3] "48-cell sweep over {2}x{4}x7" is 56; no exclusion reconstructs 48 · non-blocking · cost: 0
- [c4] BLOCKING: the cycle-3 correction of an overstatement overstated ~2.4x the other way · cost: 1 cycle
- [c6] The instruction rule said "neither pairwise arm" while its guard read one arm's bodies · non-blocking · cost: 0
- [c7] The commit that deleted a stale "Five shapes" count left "these three bodies" stale in the same file · non-blocking · cost: 1 cycle
- [PR r1] The source guard never asserts its needle occurs, so a rename of getInteractions makes it silently vacuous — its sibling next door has exactly that assertion · non-blocking · filed as #458

## Where a skill blocked or contradicted this run
- pr-harden:State — TaskOutput polling on agent tasks is very expensive (~25-30k tokens per timed-out poll, raw JSONL windows). Six polls across three agents cost more than every agent report combined. The skill says not to; the cost is worth restating with a number.
- harden:Termination — eight cycles, of which 5,6,7 each found only text/naming items written by the cycle before. The "delete the CLAIM SHAPE, not the claim" remedy is what ended it (deleting counts rather than correcting them), exactly as written.
- resolve-ticket Step 1 — `gh issue view` returned empty at exit 0, as documented; `gh api` worked.

## Declined
- Criterion 3 (count in-flight requests toward the rate limit) — if we ship without this, N parallel requests still multiply validator cost; but the ticket files it "Amplifier only" with its sibling rejected, it is a controller change outside the arm the title scopes to, and what a parallel request multiplies is now 2.7x-209x smaller.
- A cap on question-resolved rows (criterion 1's "e.g.") — if we ship without this, an adversarial question still costs ~92 ms rather than ~3 ms; but a cap makes PairChipExtent.getFound() a count over a population the arm chose, which a client cannot tell from a complete screen, and ~90 ms on one question is not worth a narrower safety screen.
- Rewriting isNamed as nameKeys.contains(normalizeName(token)) — if we ship without this, every isNamed caller keeps re-normalising each alias per call; but it reduces a CONSTANT whose multiplier this change removes, and touches every caller in the module. Its own ticket.

## Assumptions review overturned
- "the sibling arm is noted-not-filed, so do not touch it" -> the shared helper forced it; serving both arms is a choice of signature, not a consequence, and the change closes that arm's half too (gate pass 1)
- "the identifies confirmation makes the narrowing safe" -> it covers the too-WIDE direction only; the too-NARROW one is what drops a chip fail-closed, and only real data can hold it (cycle 1)
