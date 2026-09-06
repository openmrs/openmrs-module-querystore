# resolve-ticket + harden + pr-harden · openmrs-module-chartsearchai · #315 / PR 321 · 2026-08-27
outcome: converged
rounds: 2 (pr-harden)   cycles: 5 (harden)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa-Downloads-referenceapplication-standalone-3-7-1-openmrs-module-chartsearchai/392275d7-a5bd-4d59-9d5a-99f4a8c9d208.jsonl

## Refuted by measurement
- "the verb is the discriminator: 'add to the sentence naming it' does not fire" (written into 3 texts) -> mutating the verb ALONE leaves both answer cases green; the completeness half was doing the work. The two arms that established it changed two clauses at once. · cost: 1 cycle
- "give the date it was stopped only when that record states one" would deliver the ticket's title -> left the ticket's own cell UNFIXED and dropped the activation date it used to carry, 3/3. · cost: 1 arm
- "together with the date it was stopped where that record carries one" fixes the cell with the date -> it does, AND replaces both live drug names with a lab measurement on ADR 45's cell-B chart, 4/4. · cost: 1 arm
- Five wordings tried to remove the fabricated stop date (prohibition on dating; prohibition on reusing other dates; stating what the field does/does not say; dropping "ended"; status-only). All five fabricate. It is the clause, not the phrasing. · cost: 5 arms
- "every attributable drug_order record carries the mark" -> false; an attributable but unevaluable order gets no mark. Contradicted two other places in the same diff. · cost: 1 cycle
- "a site that has SET this property already has the row" (in 3 places) -> false; the row is created by the module's own first startup from config.xml. The real scope is BROADER: every upgraded install. · cost: 1 cycle
- "3052 tests" (published in the PR body and several commit messages) -> real figure 1557, then 1559. A double count: per-class `Tests run:` lines summed against each module's `Results:` summary. · cost: caught at r2

## Raised by a fresh agent, missed by the author
- [harden c1] The structural guard was FAIL-OPEN: its slice ran 125 lines past the constant, so a hardcoded mark passed as long as the constant's NAME appeared anywhere in between. · blocking-equivalent · cost: 1 cycle
- [harden c2] Still open one scope in: the name can sit in a comment INSIDE the initializer while the prompt hardcodes the text. · cost: 1 cycle
- [harden c3] Open a third way: the copy split across the file's own line-wrap. · cost: 1 cycle
- [harden c3] Open a fourth way: the split held apart by a BLOCK comment (line-comment stripping alone misses it). · cost: 1 cycle
- [pr r1] Open a FIFTH way: the positive assertion read RAW text while the negative read normalised text, so the name could sit in a comment while the copy sat in a sibling constant declared OUTSIDE the slice. · blocking · cost: 1 round
- [pr r2] `assertFalse(prompt.contains(ACTIVE_ORDER_LABEL))` claimed to pin the ONE-BRANCH design and pinned nothing of the sort — a positive half worded without quoting the constant passes the whole suite. Six distinct guard defects in one PR; this is the only one where the assertion measured the wrong PROPERTY rather than looking in too small a WINDOW. · non-blocking · cost: 0 (finish round)
- [pr r1] The PR body claimed a cell "states the ending, 3/3" when the author's OWN capture showed it names nothing and cites nothing. Written from the ticket's framing rather than the measurement. · blocking · cost: 1 round
- [pr r1] That reviewer measured a DIFFERENT base for the same cell than the author did. Three runs on record against the unchanged prompt, three different bases, each stable within its own run — so "3/3" against that cell was never safe to publish. Now recorded as unsettled.
- [harden c1, integration] A chart shape no cell reached: the #118 injected `Active drug order:` stand-in sitting beside an ended record for the same drug. Measured; the feared false statement does not occur. · cost: 1 cycle
- [harden c3] `ArchitectureGuardTest` passed 5/5 on a wrong source root — it WALKS, so it scanned nothing and reported no violations. · cost: 1 cycle
- [harden c4] That fix covered 4 of its 5 rules; the fifth walks its own directory and returned silently. Then: existence alone was not equivalent to the canary, because the sibling omod module carries the same package path. · cost: 2 cycles
- [pr finish, verifier] The standalone had a STALE omod deployed (an 03:19 build) — caught before driving any cell, exactly the trap the skill names.
- [pr finish, verifier] New evidence nobody had produced: the clause discriminates BOTH ways within one answer (live drugs plain, lapsed one qualified), proving the cell passes because the mark discriminates rather than because the lapsed order was absent from retrieval.

## Where a skill blocked or contradicted this run
- pr-harden §6 says never restart a server that was already running when the run began; resolve-ticket §1's pre-flight (added on its 6th run) says an attributable running standalone IS a usable target. Only one standalone exists on this machine and the whole run drove it. Followed resolve-ticket's rule, stated the deviation in the verifier brief. Worth reconciling.
- resolve-ticket §8 says check `closingIssuesReferences` rather than the wording — it earned its place twice here. `Refs #315` still produced closes=[315] because the body said "Please close #315 by hand"; and the remedy that worked was removing the keyword, not rewording around it.

## Declined
- [pr r1-3] "~130 lines (ModuleSourceRoot + 3 call sites, assumeOptedIn + 3 call sites, ArchitectureGuardTest hardening) are unrelated to #315; split them out." — If we ship without splitting, nothing breaks: each is exercised by this PR's own tests and is a strict hardening of an existing helper. What splitting would break is this PR — its structural guard does not compile without ModuleSourceRoot and its answer cases do not skip correctly without assumeOptedIn — so the split is a re-ordering with a window in which the walking guard is silently fail-open.

## Assumptions review overturned
- "Rows 2 and 3 of the re-measured table are the deliverable" -> row 3 is not fixed; it is a COST (the answer stops naming the drug). Overturned at pr round 1, and the ledger gained an A3 column.
- "The clause's costs are the dose loss and the A3 name loss" (written as a closed set of TWO) -> the worst cost, the fabricated stop date, was in neither list. Overturned at pr round 2.
- "The two new converse cases guard the live-order direction" -> four mutations leave both green; they are canaries over a property that currently holds, not guards with demonstrated sensitivity. Overturned by the fixer itself, and recorded rather than glossed.
