# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #280 / PR 383 · 2026-09-07
outcome: converged
rounds: 3 (pr-harden)   cycles: 3 (harden)   verifier: ran twice (works at runtime, both)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-280/d2d99cb9-3080-471b-bcb6-1216f8c0ac69.jsonl

## Refuted by measurement
- "the order stamp's carry across the enrichment is unreachable, do not write a case for it" (written into javadoc as an instruction) -> a review agent ran the real builder against an order list throwing partway through iteration and got activeDrugOrdersRead=false WITH a populated order list; the carry was reachable and unpinned · cost: 1 harden pass
- "screened == reportsStandingChartAlerts()", stated in five homes -> the published flag is strictly narrower (gate AND both chart reads AND the pass completing); they diverge in exactly the cases the key exists for · cost: 2 harden passes, two sweeps missing survivors that said "publishes it" rather than "the predicate it publishes"
- "every global property in this module is read live" -> false; the dataset load is lazy and cached for the module's life, which is why /drugreferencestatus exists · cost: 1 harden pass
- A plan-stage claim that publishing conditionRuleCoverage on the new endpoint was free -> the gate found ChartSearchAiConditionRuleCoverageTest pins that key's write at exactly one site; cut at plan time, then re-derived and taken properly in pr-harden round 2 through a shared writer · cost: 0 (caught at the gate)

## Raised by a fresh agent, missed by the author
- [harden p1] The entry-gate source guard used a hand-rolled reader that did not blank comments, so a commented-out gate satisfied it while the gate was gone; SourceScan in the same package already solved this · blocking · cost: 1 pass
- [harden p2] A failed ACTIVE-ORDER read did not clear any stamp, so a role without core Get Orders got {"screened": true, "alerts": []} for a patient whose prescriptions nobody could read · blocking · cost: 1 pass
- [harden p2] Nothing pinned the endpoint's Context.requirePrivilege; deleting it left the whole omod suite green · blocking · cost: 1 pass
- [harden p3] standingChartAlerts(null) certified a chart nobody read as screened; getAlerts() leaked the internal list; screened(null) threw · blocking · cost: 1 pass
- [harden p3] The interactionPairs guard was defeated by writing the key as "interaction" + "Pairs"; the UNBOUNDED guard by reaching the seam, which is package-private, from a sibling class · blocking · cost: 1 pass
- [pr-harden r1] The wire suite never exercised screened:true beside an empty alerts array - the payload that dominates production - so deriving the flag from the list left the build green · blocking · cost: 1 round
- [pr-harden r2] The ORDER stamp's production write was pinned but its RECORDS sibling was not; deleting both assignments left the build green · blocking · cost: 1 round
- [pr-harden r3] ADR Decision 78 had eight citations and no entry in the ADR's own table of contents; the guard checks the heading, not the index · non-blocking

## Where a skill blocked or contradicted this run
- pr-harden:"Compare the base you just fetched against the one the previous round saw" — main merged #382 mid-run and took ADR Decision 77 while this branch held it. The rule caught it; the renumbering touched nine homes and was done by searching for the NUMBER, not a phrasing. Third consecutive run to hit this.
- ProjectInstructionsGuardTest's budget javadoc — "raising a budget in the same commit as the prose that overflowed it is illegitimate" had no case for two branches each adding one rule. main alone left 19 bytes of headroom, so the second branch could not record a rule of ANY size. Resolved by trimming this branch's own prose first (folding a duplicate sub-bullet away) and raising to 75,000 with the reasoning written into the guard.
- A python edit script whose write followed a failing assert silently lost three edits (a method rename and two claim corrections) while its earlier statements had already mutated the string. The commit message announced edits the diff did not contain; a later review pass found it. The skill's "assert before replacing, verify by reading back" covers the first half but not "one script, several edits, one write at the end".

## Declined
- (none — no finding was declined in any harden pass or pr-harden round)

## Assumptions review overturned
- "the endpoint publishes {screened, alerts} and conditionRuleCoverage belongs only on /drugreferencestatus" -> a client cannot reach that endpoint under the same privilege, and the key was already on every /search answer; the standing surface now carries it too, written through one shared method · pr-harden round 2
- "the answer path's SubjectMatter bound was introduced by this branch" (implied by README and config.xml wording) -> it predates the base commit and is unchanged here · harden cycle 2
