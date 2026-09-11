# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #247 / PR 406 · 2026-09-11
outcome: converged
rounds: 4   cycles: 2 (harden, pre-PR)   verifier: ran twice (works at runtime; final run on the merging head)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-247/f7d9b5da-9c38-40be-adf6-937e8ce5644a.jsonl

## Refuted by measurement
- "a records-only verdict (contraindicationRecordsRead alone) is the right published key" -> refused at the refutation gate: it reads TRUE where the ORDER read failed, which is ADR Decision 79's own defect one surface over. Verdict became the whole pass. · cost: 1 gate pass, 0 rounds (caught before code)
- "every chart read behind the drug-safety layer" (in the accessor javadoc + README) -> false; age and weight are also read and were excluded. · cost: 1 harden pass
- "weight feeds only the dose arm, which raises nothing today" -> false; addOverdose adds a SafetyWarning from validate, proven by an existing green test. · cost: 1 harden pass
- "the response is byte-identical to a healthy patient's" -> unmeasured; the injected record differs. Replaced with the issue's own measured counts. · cost: 1 harden pass
- "any permissions problem, database error or querystore fault" -> these reads go through core's service layer, not querystore. · cost: 1 harden pass
- "Seventeen test doubles overrode it" -> round 1 "corrected" it to 14+3; round 2 measured 17+3=20 by compiling the pre-widening tree against the widened signature. The grep both corrections used cannot see a double written with fully-qualified types. Tally deleted entirely in favour of the method. · cost: 2 rounds
- "This key IS non-null on the early done event" -> false on the shipped default (drugReference.enabled=false -> null). · cost: 1 round

## Raised by a fresh agent, missed by the author
- [harden p2] Each new WARN carried a full stack trace (~13 KB measured) and /chartalerts is a documented unrate-limited poll · non-blocking · cost: 0 rounds (fixed in harden)
- [harden p2] A read that throws PART-WAY leaves a PARTIAL set, so `false` beside a NON-EMPTY safetyWarnings is reachable and README only described the empty case · non-blocking · cost: 0 rounds. Later observed LIVE by the final verifier (v406_nocond: false with 3 chips).
- [harden p2] The record-before-injectRecords ordering was asserted in javadoc and pinned by nothing — moving it left the build green · non-blocking · cost: 0 rounds
- [r1] Nothing pinned that each warnUnreadable call names ITS OWN read's privilege; swapping GET_CONDITIONS for GET_ORDERS shipped green, and the deliberate trace-drop rests on that message being right · blocking · cost: 1 round
- [r2] Round 1's own correction of the widening tally was itself wrong · blocking · cost: 1 round
- [r3] The PR description still carried the refuted tally after the code prose had been corrected — the body is a home no repo-wide grep reaches, and the orchestrator had applied round 1's body fix but not round 2's · blocking · cost: 1 round
- [r3] warnUnreadable's javadoc claimed it was shared by "every read this builder degrades to an empty value"; four per-concept helper reads are still DEBUG · non-blocking · cost: 0 rounds

## Where a skill blocked or contradicted this run
- pr-harden:REVIEW — briefing the reviewer to "check out detached" left the worktree detached AND holding the branch, and a previously killed agent's orphan worktree also held it, so `git checkout <branch>` failed twice. Cost: a cleanup detour. The brief should say "return the worktree to <branch>" (later rounds did, and it worked).
- harden:Termination / pr-harden:State — `git checkout -- <path>` to undo my OWN mutation probe discarded an uncommitted production fix (the warnUnreadable helper). The skill warns about this for agents; the orchestrator is equally exposed. Committing before probing is the fix and it worked thereafter. Cost: one re-apply, caught by a test failing for the right reason.
- Rate limit killed one harden agent and one pr-harden-era agent; the leaner-brief retry converged both times.

## Declined
- (none — every finding across 4 rounds was implemented)

## Assumptions review overturned
- "the ticket's mention of age/weight as neighbours that 'bound the change' means leave them alone" -> round 2's reviewer showed the RECORDED REASON answered the verdict question rather than the log question; round 2's fixer raised both to WARN with tests. Scope widened beyond item 1's literal text, deliberately and on the record.
