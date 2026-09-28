# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #455 / PR #550 · 2026-09-28
outcome: converged
rounds: 2   cycles: 3 (harden)   verifier: skipped (test-file-only change; no runtime surface)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-455/7b0356ed-0c41-4404-88d3-6e881f7ce20c.jsonl

## Refuted by measurement
- Ticket: "a delegating factory is caught by its own call site" (quoting a one-line delegate as measured FAIL) -> on 4cddee15 the one-line delegate was GREEN (declaration-line skip); only a multi-line delegate failed · cost: 0 (caught in Step 2 probe)
- Author (harden cycle 1-2): "a top-level member declared and bodied on one line" / "a delegating factory whose call sits on any other line is caught" -> falsified by Phase 2 agents' probes (brace-on-next-line body, one-tab static field) · cost: 2 harden cycles; ended by deleting the enumeration claim shape and stating the helper's predicate

## Raised by a fresh agent, missed by the author
- [harden P2 c1] one-line per-marker splitter calling newSentence directly also escapes · non-blocking-in-harden (escalated) · cost: 1 cycle
- [harden P2 c1] residue cited "issue #455" for a measurement the issue contradicts · polish · cost: 0
- [r1] disclosing the hole is weaker than a two-token tightening of the skip (skip only `Sentence newSentence(` lines); author had deferred it as out-of-scope "adjacent" work · non-blocking · cost: 1 round (implemented by fixer)
- [r1] 128-char javadoc line after an unreflowed append · non-blocking · cost: 0

## Where a skill blocked or contradicted this run
- gate-state: `--run` must precede the subcommand; `clear-await --run X` errored and an && chain silently skipped the commit that followed · cost: one re-issued command

## Declined
- (harden P2) move the skip predicate into enclosingMethodOf's javadoc — if the helper were narrowed, the whole residue must be re-derived whether linked or restated, so moving it protects nothing
- (harden P2) drop the "Issue #455 reports…" sentence — without it a maintainer following the issue would re-apply its over-broad wording (moot after r1 deleted it)
- (r2 notes, unimplemented) inline comment loose for a one-line `Sentence newSentence(` overload; non-Sentence newSentence overload counts its own signature (strict direction)

## Assumptions review overturned
- "Scope is the residue text, not tightening the rule" (Step 2, refuter agreed) -> r1 reviewer recommended tightening, r1 fixer judged it in scope under ADR Decision 103 and implemented it
