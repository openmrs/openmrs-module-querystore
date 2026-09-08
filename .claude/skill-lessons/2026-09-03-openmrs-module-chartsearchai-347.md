# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #347 / PR 367 · 2026-09-03
outcome: converged
rounds: 3 (pr-harden)   cycles: 8 (harden)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-347/192e3afc-0faf-4e13-8bab-2079966b394a.jsonl

## Refuted by measurement
- "a chart record of this order can carry any of [the order's recorded names]" (the pre-existing justification for the #349 bridge's silence test) -> querystore renders exactly ONE name per drug_order record, and the module records the ordered CONCEPT's name beside it, so the old test was satisfied by a name reaching no prompt text. This IS the ticket's root cause · cost: 0 (found at planning)
- "the commonest empty case is a chip about a drug the QUESTION named" -> the subject walk covers EVERY active order and consults no question; driving injectRecords over the fixture KB bridged on the subject side · cost: 1 harden cycle
- "what a chip contributes is a fact about this patient's orders and not about how the chip arose" -> the CALLER supplies the partner witness set and the two arms hand down different ones; same chart, different question, different bridge set · cost: 1 harden cycle
- "empty where nothing needed reconciling" / "empty is most chips" -> false for class-only and contraindication chips, which carry no bridges while genuinely needing the correspondence · cost: 1 harden cycle
- A published TALLY of the above refutations ("Four such rules...") -> stale one commit later, in the sentence forbidding tallies; five tallies existed simultaneously giving three different numbers · cost: 2 harden cycles
- "a PRIVATE field added to ChartOrderBridge reaches an XML client and no test sees it" -> this PR's own new guard reddens on it; two of three homes never updated when the guard was written · cost: 1 round
- 'XStream refuses the wrappers with "module java.base does not opens java.util"' -> the real message is ConversionException("No converter available"); behaviour verified, message invented · cost: 0 (caught by the verifier)

## Raised by a fresh agent, missed by the author
- [r2] REGRESSION: #347's narrowed silence test defeated #353's restsOnAnAmbiguousBridge, printing `Omeprazole from Nexium 40mg` into a citable STRENGTH_WITHHOLD finding AND onto the wire, on English deployments. #353's own tests stayed green because its fixture is the francophone shape. Whole suite green. · blocking · cost: 1 round
- [r1] restsOnAnAmbiguousBridge's premise, falsified by my own merge resolution and asserted anyway · blocking · cost: 1 round
- [r1] the ADR renumber (68->69, forced by #353 taking 68 on main) left one home standing in javadoc, where no guard resolves ADR pointers · blocking · cost: 1 round
- [r3] round 2's exclusion was pinned in the under-silence direction ONLY — widening it to every recorded name left all 1817 api tests green · non-blocking · cost: 0
- [r3] no committed case exercised #347's own shape on an order carrying a bridged concept, the arrangement production actually builds · non-blocking · cost: 0
- [harden c1] CLAUDE.md was 15 bytes under its 85,000-byte budget; the guard's own javadoc names raising it in the same commit as the overflowing prose as the anti-pattern · cost: 0
- [harden c5] the older SPELLING of a deleted gloss survived in the summary table row a client reads first, because the sweep keyed on the phrasing just deleted · cost: 1 cycle

## Where a skill blocked or contradicted this run
- resolve-ticket:Step 1 — `gh issue view` returns EMPTY in this environment (exit 0, no output). `gh api repos/O/R/issues/N` works. Cost: two wasted calls; every subsequent agent brief had to carry the workaround.
- pr-harden:State — `gate-state reviewed-sha` rejects `--only pr` with a usage error; only await/clear-await/clear accept it. The tool's own error message says so, so cost was ~0, but the skill's snippet implies otherwise.
- resolve-ticket:Step 4 — the branch was cut with `git switch -c` from a local sha, so it had no upstream until the first push; harden's `--count-edits` then could not measure the commit half and said so. The skill documents both forms as acceptable; worth knowing the fallback is the noisy one.
- A prior run of this same pipeline had left 6 unpushed commits of #347 work on a local branch and a `harden-state.json` entry with `edits: 2` and a stale await from a dead agent. Adopting the work was right; the stale gate entry would have blocked this run's first yield.

## Declined
- (none — 12 findings across 3 rounds, all actionable ones implemented; the declined ledger stayed empty for the whole run)

## Assumptions review overturned
- "The ticket asks for a measurement, so the deliverable is the measurement" -> the measurement decided direction A and the ticket's own second direction needed the wire key too; both shipped, and `Refs` not `Fixes` because the client half is another repository's · recorded at planning, unchallenged by review
- "main is where I branched from" -> main had moved by one commit (#353) into exactly this code, taking the ADR decision number I had allocated and adding a second silence my change defeated. Caught at round 1's base check, before any reviewer saw a stale base.
