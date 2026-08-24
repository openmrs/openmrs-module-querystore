# resolve-ticket (+pr-harden 0.8.0, harden) · openmrs-module-chartsearchai · #284 / PR 304 · 2026-08-24
outcome: converged
rounds: 1 (zero blocking)   cycles: 1 harden cycle (overridden after it, labelled)   verifier: ran (works at runtime)

## Refuted by measurement
- "the cosine floor can carry the fix" (revision 2's design: withhold the Tier-2 negative, fall back to Tier-1) -> config.xml:222 says the shipped minCosine=0.40 "is far too low for e5 — set it to ~0.82", so at the advised floor the fix stops delivering. Gate pass 2's only blocking objection; settled the design rather than deadlocking it. · cost: 1 gate pass
- "applying the suppression in Tier-1-only mode" -> rested on an unmeasured claim that a composite statement dilutes cosine; every #284 measurement was taken with entailment on. · cost: 1 gate pass
- "#284 measured one of each [unanchored side]" -> the ticket measured the chart side unanchored and never the reference side. My own prose, caught in harden Phase 2 round 2. · cost: 0 rounds (pre-PR)
- "lexical containment guarantees the yes" -> refuted by the measurement it cites (4 role-swapped entailed AND one faithful recitation judged not). "Uninformative", not "guaranteed". · cost: 0 rounds (pre-PR)

## Raised by a fresh agent, missed by the author
- [harden P2 r1, twice independently] claimRestsOn read inline markers only, so a finding cited array-only left the same defect standing · would have been blocking · cost: 0 rounds (caught pre-PR)
- [harden P2 r1] no test distinguished "the claim rests on it" from "the answer contains it": replacing the whole mechanism with `!demoteOnlyIndexes.isEmpty()` passed all 1360 tests · cost: 0 rounds
- [harden P2 r2] the withheld negative was recorded nowhere — not the wire, not the log, not the audit · cost: 0 rounds
- [harden P2 r2] the new bookkeeping could index the sentence list on an unset best under a non-finite cosine, turning a FALSE into a null blaming the embedder · cost: 0 rounds
- [PR r1] the reason given for keeping the judge's "yes" contradicted the decision's own premise two sentences earlier · non-blocking · cost: 0 rounds
- [PR r1] the two grounding global properties were the last operator-facing surface left stale · non-blocking · cost: 0 rounds

## Where a skill blocked or contradicted this run
- pr-harden:State — "restore with `git checkout -- <path>`" was followed on a file carrying uncommitted intended work and silently reverted a whole phase of production edits. The skill states this hazard (added after the #302 run) and I hit it anyway, from muscle memory, immediately after a successful mutation probe. The section is right; what is missing is that the mutation-probe recipe and the restore hazard sit far apart in the text.
- resolve-ticket:Step 8 — the pre-flight in Step 1 checks the standalone but nothing checks whether `main` has moved. A sibling PR touching the SAME class merged mid-run, and the rebase surfaced only at PR time, costing a 16-conflict manual merge. A `git fetch` + ahead/behind check at Step 4 (branch) would have caught it when it was cheap, and re-checking at Step 8 would have caught it before the conflict.

## Declined
- (none — round 1 raised no blocking findings, and every non-blocking one was implemented)

## Assumptions review overturned
- A2 "implement the ticket's amendment as: publish null unconditionally" -> gate pass 1: that discards the mis-attribution signal #122 deliberately kept; -> gate pass 2: but the Tier-1 fallback that keeps it is GP-conditional. Final: withhold the Tier-2 negative only, where a judge actually spoke.
- A4 "whether the reference record supplies the relationship is not decidable from the text" -> overstated: it IS decidable in-module for an INJECTED record (activeOrderEntryFor / onePerPartner). The proxy is preferred because the verifier sees only RecordMapping, and because the ticket's own [4] is a RETRIEVED drug_order no injector resolved.
- "the pronoun-compound case is a #284 test" -> after the rebase onto #302 it is that rule's shape, not this one's; rewritten as an interaction pin asserting zero judge calls.
