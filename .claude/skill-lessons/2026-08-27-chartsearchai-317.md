# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #317 / PR 318 · 2026-08-27
outcome: converged (pr-harden round 2, blocking 0) — resolve-ticket's own /harden phase ended on a labelled override
rounds: 2   cycles: 4 (harden)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa-Downloads-referenceapplication-standalone-3-7-1-openmrs-module-chartsearchai/81a52e12-8b3d-47c6-a236-8b30f3f5ac71.jsonl

## Refuted by measurement
- "the wording's failing cell is the model's phrasing sensitivity rather than the mark being wrong" (shipped in the PR body as a disclosed residue) -> false. Five wordings A/B'd, n=3 interleaved: four whose negative half contains the word "active" all answer "No active medications are recorded" for a patient with two live prescriptions; ". Order status: not in force" answers correctly. The last two differ only in the value token, so it is the mark's own word being recited into the prompt's absent-data sentence — #110's failure class reaching a FIELD, which the constant's javadoc had argued a field was safe from. · cost: 1 round
- "Order.isActive() is the same predicate getActiveOrders applies in SQL" (written to justify dropping a service call) -> agrees on every leg checked EXCEPT that isDiscontinued/isExpired THROW where the SQL answers, when dateStopped > autoExpireDate. Inside one chart-wide try that removed the mark from every record on every chart for the patient. · cost: 1 harden cycle
- "the reused prefix ends at the first drug-order record" -> ends at the record whose mark MOVED; and the population claim under it ("all 8 drug orders newer than all 197 observations, so the first is record [1]") ignored 9 allergies that sort above them. · cost: 2 harden cycles
- ChartSearchAiReferenceGroupTest's sweep "would object to promoting drug_order to a constant" -> the sweep is a forcing function to RECORD a group decision, not a prohibition; promoting it was 1 constant + 1 row, 6/6 green. · cost: 0 (caught in harden)

## Raised by a fresh agent, missed by the author
- [harden] The branch was RED and a commit's message described a fix its diff did not contain — my own mutation probe, undone with `git checkout --` while the file carried the uncommitted fix, reverted the fix with the probe. Empty `git status` read as success. · blocking · cost: 1 cycle
- [harden] Two guards the suite could not discriminate: a case that charted only a test order (so the read-skip guard fired and the resource-type scope was never exercised), and the statement order inside the per-order try (the unevaluable order was never charted). · blocking · cost: 1 cycle
- [r1] The wording regression must not ship on a disclosure when the constant's own javadoc demands an A/B first. · blocking · cost: 1 round
- [r1] Chart bytes are now PRIVILEGE-dependent: daemon threads always assemble with the mark, so a role lacking Get Orders can never match the warmed fullChart prefix and the durable KV corpus buys it nothing. · non-blocking
- [r2] Two independent answers to "is this order in force" (chart uses Order.isActive(), chips use getActiveOrders) with nothing pinning their agreement. · non-blocking
- [r2] `aFailedOrderReadIsReportedAtWarn`'s comment claimed the WARN covered a swallow inside resolveAllOrders; the test overrides that method, so its production body never runs and a real swallow left the whole suite green. · non-blocking
- [verifier] Extracted the assembled prompt by saving llama-server's KV slot mid-generation and detokenizing it — direct evidence the mark is in the prompt with correct polarity, where the audit table, the wire and the logs all carry no prompt.

## Where a skill blocked or contradicted this run
- resolve-ticket:Step 8 — it prescribes `Fixes #N` for the ticket and `Refs #M` for the related one. Written on ONE line ("Fixes #317. Refs #315.") GitHub applies the keyword across the period and closingIssuesReferences came back [315, 317]. Rewording "does not close #315" did nothing; only splitting them onto separate lines and dropping the keyword for 315 fixed it. A merge would have silently closed an open defect.
- harden:State — the `git checkout --` hazard is documented with a measured precedent, and I hit it anyway. The rule that would have saved it (commit before anything mutates the tree) is stated one paragraph above the one I followed.
- pr-harden gate fired at 79 minutes on a live fixer that was mid-standalone-A/B. The one-hour await bound is shorter than a legitimate deploy-and-measure phase; a liveness ping established it was alive and it returned a complete result.

## Declined
- (none — round 1 and round 2 fixers implemented every finding)

## Assumptions review overturned
- A2 "one-sided marking, as the ticket words it" -> two-sided, because absence would otherwise mean three things at once and a #315 rule keying on it would assert currency for every stopped drug whenever the read failed. Overturned at the refutation gate, before code.
- A5 "Context.logout() reaches the order-read failure path" -> false; build() resolves a GP outside every try and throws first. Overturned at the refutation gate, by measurement.
- "the wording residue is phrasing sensitivity" -> the word "active"; overturned in round 1.
