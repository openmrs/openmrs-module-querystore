# resolve-ticket (+ harden, pr-harden) · openmrs-module-chartsearchai · #266 / PR 322 · 2026-08-27
outcome: converged
rounds: 1 (pr-harden)   cycles: 7 (harden)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-openmrs-module-chartsearchai/d1c97ff6-1e59-447e-acef-8c5d5509727d.jsonl

## Refuted by measurement
- "the 7 existing AtcDrugReferenceSourceTest call sites" (plan, on keeping the single-arg parse) -> there are 2 in that file and 4 repo-wide; the remedy was unaffected but the figure was invented · cost: 0 (caught at the refutation gate, before code)
- "without it a rule that always fires passes 1-4" (plan's justification for the healthy-export control case) -> case 4 already reddens on an always-firing rule; the control's real value is narrower · cost: 0 (gate)
- "the readOperatorFile extraction is behaviour-identical" -> it silently dropped loadWithClasspathFallback's blank-path skip, so a BLANK path logged `file '' not available` per dataset · cost: 1 harden cycle
- "every untouched install logged that spurious line" (my own justification for restoring the skip) -> an INSTALLED module reads the non-blank config.xml default, so what it logs is the line main already logged; only a blank/absent-row path is noisy. The guard is real on the narrower claim · cost: 1 pr-harden round
- "the crossReactivity map's wire values are pinned" -> hardcoding four of its five keys to the disabled-state values left the whole suite green, because the only case reading that map drives the DISABLED state where all five equal the mutation · cost: 1 harden cycle
- "assertFalse(capture.describeAll().contains(...))" pins the log-noise regression -> describeAll() returns a List, so contains() was an exact-element match that can never be true; the assertion was green while the forbidden line was being logged · cost: 0 (my own mutation check caught it in the same cycle)
- "the two-format claim about the groups file has N homes" -> seven, found one per cycle, each hidden by a NEW mechanism: a data file rather than a doc; markdown emphasis splitting the phrase; a line break between quantifier and noun with wording matching no other home · cost: 3 harden cycles

## Raised by a fresh agent, missed by the author
- [gate] The plan's §8 doc list was incomplete: four more texts stated ATC's exclusion or the entries-only vocabulary (DrugReferenceSource's javadoc, the corpus sweep's javadoc, loadStatusNamesTheAtcExportItRead's javadoc AND its assertion message, datasetMissingARequiredTable's @param) · non-blocking · cost: 0
- [h1] `configuredDataFileNotRead`'s detail said "The entry count …" for the GROUPS dataset, whose section publishes a groupCount and no entryCount — the exact defect the items parameter was added to prevent, applied to one rule and not its sibling · non-blocking · cost: 1 cycle
- [h1] The structural guard's discovery was a literal `"implements DrugReferenceSource"` substring match; a probe class declaring `implements Serializable, DrugReferenceSource` was never enumerated and the guard stayed green · non-blocking · cost: 1 cycle
- [h1] `configuredDataFileNotRead` is raised BEFORE the classpath read, so on a missing bundled resource the detail asserted a read that never happened · non-blocking · cost: 1 cycle
- [h1] The item-noun assertion covered the curated call site only; crossing the DDInter literal survived the whole suite · non-blocking · cost: 1 cycle
- [h2] The groups LOG channel was unpinned — deleting `logTo(log)` from the loader left the suite green, i.e. the channel this ticket exists to add a SECOND of could silently drop back to one · non-blocking · cost: 1 cycle
- [h2] The "already-loaded whatever the switch says now" clause, stated in the accessor's javadoc as the entry dataset's contract, rested on nothing: deleting the early return was green · non-blocking · cost: 1 cycle
- [h3] A NESTED type is a class in the package that a file-name scan hides; the guard's own javadoc claimed file names were "a question no syntax can hide" · non-blocking · cost: 1 pr-harden round
- [r1] The PR body said `docs/drug-knowledge-base-comparison.md` "is untouched here" while the branch edits it — the body contradicting the diff · non-blocking · cost: 0 (applied at FINISH)
- [verifier] Independently counted the staged file's content lines (87) and got the exact figure the finding's detail reports, and confirmed BOTH branches of configuredDataFileNotRead on the wire, including the "reached for" wording the r1 commit claims to have introduced

## Where a skill blocked or contradicted this run
- pr-harden:State — `gate-state pr-set` dropped `declined` and `reviewed_shas`. Both were written successfully (the helper echoed "declined r1-1 recorded" / "reviewed c4648faeacee") and were absent from the entry after two later `pr-set` transition writes. The skill states these subcommands exist "so a transition write never has to restate them and cannot drop them" — it did. No cost here (the loop converged in round 1, so no round 2 reviewer needed the ledger) but on a multi-round run the ledger would silently empty and a reviewer would re-raise settled findings.
- pr-harden:step 6 — the verifier's procedure anticipates a Java-21-vs-1.8 mismatch and suggests `java_home -v 1.8`; the actual failure was the OPPOSITE (`invalid target release: 11` under JDK 8, machine default 21). Cost: one repair attempt.
- environment — the assigned worktree was deleted by something outside the session TWICE during the verifier's run. It recreated it at the exact sha with `git worktree add --detach`, so nothing was lost, but the checkout came back DETACHED and the orchestrator had to re-attach it to the PR head branch. Nothing in either skill covers a checkout disappearing mid-run.
- pr-harden:FINISH — one round ref (`pr-322-review`, created by the reviewer inside its own isolated worktree) cannot be deleted because that agent worktree is still registered. Not the orchestrator's to clean.

## Declined
- [r1-1] The groups parser drops a group with a blank name or no usable atcPrefixes with a bare log.warn, not a finding — if we ship without this, an operator's own curated family is silently out of force with `findings: []` beside a plausible `groupCount`, diagnosable only by whoever was reading the WARN log at module start. Declined because JsonDrugReferenceSource carries the identical bare warn for dropped ENTRIES and is untouched by this branch, so closing it properly means a new rule with its remedy and DATA_RULES classification plus cases on both parsers — the shape #242 -> #264 -> #266 has already taken twice. Reported as a follow-up in the PR body.
- [r1-6] The log line for a cross-reactivity finding carries the same "Drug-reference data validity —" prefix as the entries dataset — if we ship without this, a reader scanning log prefixes alone cannot tell which of the two datasets a line is about. Declined: `logTo` is one shared renderer and each detail names its own global property, so the line is unambiguous in substance; threading a dataset label through would give the two channels two vocabularies for one rule.
- Collapsing each source's two `lastLoad*` volatile fields into one — if we ship without this, three sources keep two volatile fields written in sequence; nothing breaks, because both are written inside `ensureLoaded`'s monitor and read by the same thread immediately after, unlike `LoadedGroups` whose holder IS read via a lock-free fast path.
- The ATC parser's per-line `split("\\s+", 2)` Pattern compile — ~6,500 Matcher allocations once per module lifetime; pre-existing and untouched.
- A production seam so the groups double-checked lock could be load-counted — a production change purely for test reachability, which the plan's own question 7 refuses.
- `DrugReferenceValidity.toMaps`' documented serialization ORDER — a reversed findings list misleads nobody; the order is informational.
- `docs/drug-knowledge-base-comparison.md`'s two-of-three ADAPTERS enumeration — a different pre-existing claim from the one this branch corrected in seven homes; its own change.

## Assumptions review overturned
- "the extraction of readOperatorFile is behaviour-neutral, so the blank-path guard belongs at the call site" -> the guard belongs INSIDE the helper so no caller can forget it (harden cycle 2)
- "a source's validity channel can be guarded by scanning source text for `implements DrugReferenceSource`" -> membership must be asked of the loaded class, and discovery must walk nested types too (two review probes, one per round)
- "the crossReactivity section's wire shape is covered by the omod test" -> that test drives the DISABLED state only, where every value coincides with the mutation (harden cycles 2 and 3)

## Correction appended by a separate session (not by this run)

This run recorded two observations about its environment. One of them names the wrong cause, and both
were caused by a concurrent Claude Code session working on the pipeline itself. Correcting them here
because a retro reads this file as evidence and would otherwise change a skill to fix a defect that
does not exist.

**"`gate-state pr-set` dropped `declined` and `reviewed_shas`" — not what happened.** `pr-set` uses
`setdefault` for both fields and cannot drop them. What removed them was `gate-state --cwd <path>
clear`, run by the other session against this run's worktree while it was sweeping what it had
misjudged as dead entries. The run then wrote a fresh entry, correctly, with both lists empty. The
skill's claim that a transition write "cannot drop them" is accurate and needs no change; the entry
as it stands now carries both fields, repopulated by this run's later writes.

**"the assigned worktree was deleted by something outside the session TWICE" — accurate, and the
cause was that same session** releasing this run's slot lease, having read it as a leftover probe. The
recovery this run performed — recreating the worktree at the same sha and re-attaching the branch —
was correct and is the reason nothing was lost. That a run survived its checkout being deleted twice
mid-verifier is a genuine and unplanned robustness result; it is NOT evidence of a defect in either
skill, and the observation that "nothing in either skill covers a checkout disappearing mid-run"
stands on its own as a gap worth noting.

**Independently re-verified afterwards**, in a throwaway worktree with its own maven repository, at
this PR's head `b0311944`: `BUILD SUCCESS`, tests=1579 failures=0 errors=0 skipped=57. And at runtime
on a standalone no session was using, with `sourceFormat=atc`: `loaded: True, inert: True,
entryCount: 0, findings: 1` — rule `no-line-yielded-an-entry`, remedy `reported`, naming the
document's 100 content lines. The `crossReactivity` section carries its own `findings` key, empty on
the shipped file with `groupCount: 1`. The run's `verifier: ran (works at runtime)` is confirmed.
