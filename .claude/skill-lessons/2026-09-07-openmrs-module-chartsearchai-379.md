# resolve-ticket (+ harden, pr-harden) · openmrs-module-chartsearchai · #379 / PR 382 · 2026-09-07
outcome: converged
rounds: 5 (pr-harden; cap raised 4 -> 5)   cycles: 5 (harden)   verifier: ran (works at runtime) x2
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-379/80da25e7-4647-4c1f-bfc5-8d1bee5c46a8.jsonl

## Refuted by measurement
- "Putting [N] in a record is additive and safe" -> ReferenceProseFidelityCheck strips markers from the ANSWER only, so a record's `[14]` tokenises to the word `14` the stripped answer can never reproduce; an answer copying the attribution VERBATIM was reported as a substitution, at WARN and on the published unfaithfullyRenderedCitations. Caught by the refutation gate, before code. · cost: 0 rounds
- "It adds DATA, not an instruction, so the three reverted prompt variants do not bind" -> eval/drift-metric/README.md records a data-adding change in this same render layer that "worked, and it did not help ... Reverted on the same beat-or-match standard". Argument withdrawn at gate pass 2. · cost: 0 rounds
- "Neither ambiguity rule above could see it" (ADR) -> the displayByNumber rule DOES see that arrangement; what the claimedByUuid refusal actually buys is the FIRST order's correct number, not the second's silence. · cost: 1 cycle
- "This map is only read by an ORDER's uuid, which no injected record's key can equal" -> an injected active_drug_order record IS keyed on an order's uuid, and that lookup is the whole reason numbers are resolved after the reconciliation. Third successive attempt to describe that field; all three measured false, so the description was deleted in favour of "instrument the constructor and read the types". · cost: 1 cycle
- "The record-side marker stripping is a no-op on every record shipped before this decision" -> an order display carrying a bracketed number already put one in a record's prose; `Zolvimix [9]` renders `... from Zolvimix [9] [1]` and citedIndexes returns [9,1,2]. · cost: 1 cycle
- "The model got it wrong for 3 of 5 attributions" (shipped GP description + README) -> the issue records 5 interaction SENTENCES, 2 cited right, and the clause was emitted for exactly 2 of the 5, both mis-cited. The third miss raises no clause at all. · cost: 1 round

## Raised by a fresh agent, missed by the author
- [harden c1] A record that is ANOTHER active order's own (by uuid) was cited for this order — the aspirin 81mg/325mg shape, reproduced through the real injector · blocking-equivalent · cost: 1 cycle
- [harden c1] The one-record-one-prescription refusal was keyed on DISPLAY, so two prescriptions sharing a display and reaching one record kept the number · blocking-equivalent · cost: 1 cycle
- [r1] The "one record two ORDERS reach" refusal was written over what orders RESOLVED to, not over CANDIDACY; a record two orders name was still handed to one whenever the other's resolution came back null · blocking · cost: 1 round
- [r1] Shipped an unmeasured prompt-render change against the ticket's own stated precondition; remedied with a GP gate defaulting off, the repo's own established pattern · blocking · cost: 1 round
- [r2] One half of the display refusal reddened NO case — deleting it left the whole api suite green · blocking · cost: 1 round
- [r3] That half's case pinned only ONE permutation of getActiveDrugOrders(); substituting byDisplay.remove for the strike left the suite green. Fixing it turned up the same weakness on the sibling branch at higher cardinality, and in the claimedByUuid pre-pass · blocking · cost: 1 round
- [r4] The shipped global-property description — the text an operator reads at the moment they flip the flag — carried a denominator the issue does not contain · blocking · cost: 1 round
- [final verify] The reworded GP description never reaches an install already carrying the module at 1.0.0-SNAPSHOT: OpenMRS gates setup on module.<id>.version, and setup only CREATES a missing row. Core behaviour, established by forcing setup and then deleting the row · non-blocking · cost: 0

## Where a skill blocked or contradicted this run
- ENVIRONMENT (not a skill): the disk filled mid-run and every recovery path needs to write a file first — Bash creates a per-call .output, Write stages through a .tmp sibling. The session could not free its own space, could not record its own gate state (gate-state is a shell command), and had to hand back. Cost: one handback with a blocking finding open and a stale state entry. Feedback drafted.
- pr-harden:Step 1 "compare the base you just fetched against the one the previous round saw" — earned its keep three times: main took ADR Decision 75 (#380), then Decision 76 (#377), while this branch had allocated each in turn. Renumbered 75->76->77. Cost without the check would have been two colliding decisions in one file.
- pr-harden / ProjectInstructionsGuardTest interaction: the third merge from main put BOTH instruction files over their size budgets, main having left the root file 79 bytes of headroom. Resolved by moving this change's rule to the javadoc beside its code, which is what "Documenting a decision" prescribes for a rule binding one class — but the budget is now the binding constraint on any new root-file rule, by anyone.
- harden Termination vs a 429: cycle 5's confirming agent died on a session rate limit and the per-call model override is hook-refused, so the documented retry lever was unavailable. The four changed sentences were verified by the orchestrator instead; the fresh-context read of that cycle's edits did not happen and was reported as such.

## Declined
- Nothing was declined in any of the five rounds.

## Assumptions review overturned
- "The deliverable is the fix, and the owed live measurement is stated rather than run" -> still true, but round 1 established that stating it is not sufficient: the rendering had to ship behind a global property defaulting off, so a merge switches nothing on and the owed probe becomes a one-flip A/B. (round 1)
- "A wrong citation NUMBER is #377's question, open elsewhere" -> #377's own fix landed on main mid-run (#381, ActiveOrderCitationFidelityCheck). Round 5 examined the seam and found the numbered attribution neither makes that check fire nor go silent wrongly. (round 5)
