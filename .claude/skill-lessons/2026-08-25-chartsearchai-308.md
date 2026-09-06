# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #308 / PR 313 · 2026-08-25
outcome: converged
rounds: 6   cycles: 6 (harden)   verifier: ran (works at runtime; 9 runs / 3 builds, answers byte-identical)

## Refuted by measurement
- "Take option 1 as a THIRD STRENGTH CLASS between withhold and caution" -> ADR 42 defers PROVENANCE not strength, and ADR 37 measured a contraindication stating no withholding clause flipping "No — ibuprofen should not be taken" to "Ibuprofen can be given, with one caution" 3/3 · cost: gate pass 1, before any code
- "drug-reference-borrowed-alias-corroboration.json gives ADR 42's ketoconazole shape as a control" -> the recorded Ketoconazole fires a CORROBORATED sibling rule that wins the ledger, so the control was unconstructible as described · cost: gate pass 2, before any code
- "The corroboration answer can ride on the rank winner" -> a corroborated BLANK-NOTE rule ties at rank 0 and loses the incumbent tiebreak; the prompt then carried "Recorded for this patient" beside "could not corroborate" · cost: 1 harden cycle
- "Fold it in ContraindicationChips" -> that key is the SUBSTANCE and spans rows the injector renders one record for · cost: 1 harden cycle + 1 review round
- "A record is rendered per ROW" (ADR 44's own justification) -> matchingEntries injects ONE record per SUBSTANCE via canonicalRow; the injector's own javadoc had said so since #163 · cost: 1 review round
- "One fold over one unit makes the channels agree" -> contraindicationSections has a SECOND, clause-TEXT stage (uncorroborated.removeAll(recorded)); stopping at the key created a new divergence · cost: 1 review round
- "The trim is load-bearing and asserted in four texts" -> two texts, and the property was pinned by no case: the semantically-equivalent rewrite left the whole build green · cost: 1 review round
- "JsonDrugReferenceSource never reads substanceName" (a reviewer's grep-based claim) -> Jackson binds List<DrugReference> directly and setSubstanceName is public, so curated json reaches the multi-row shape · cost: none, caught by cross-checking two agents

## Raised by a fresh agent, missed by the author
- [c2] The two channels can contradict on one COLLAPSED KEY · blocking-equivalent · cost: 1 cycle
- [c3] The AND folded across ROWS while the record's MAX is per entry · blocking-equivalent · cost: 1 cycle
- [c3] The SubjectMatter gate skips a corroborated rule before it can carry its key · blocking-equivalent · cost: 1 cycle
- [r1] The rank can pick a warning from a row the injector renders no record for · blocking · cost: 1 round
- [r2] The record's clause-TEXT precedence stage has no counterpart in the fold · blocking · cost: 1 round
- [r3] The precedence conjunct compared the KEY's joined clause, not the note the finding prints · blocking · cost: 1 round
- [r3] One conjunct was dead and four texts named a mutation that did not redden · blocking · cost: 1 round
- [r4] The fold's own matched-rules guard was unpinned; deleting it left the whole suite green · blocking · cost: 1 round
- [r5] The trim normalisation was unpinned against a semantically-equivalent rewrite · blocking · cost: 1 round
- [r5] A CLAUDE.md bullet's second accessor was stranded mid-bullet by a 700-word insertion · non-blocking · cost: 0

## Where a skill blocked or contradicted this run
- harden:Phase 2 — `git checkout -- <path>` to undo my OWN mutation probe discarded the uncommitted ledger fix with it. The skill warns about exactly this; I hit it anyway because the probe and the intended edit were in one file. Cost: one re-implementation. Committing before probing is the only thing that prevents it.
- pr-harden:COMMIT — an agent left the worktree on `pr-313-r1` and the round's edits landed there. The pre-commit branch check caught it and a plain checkout carried the work across (both refs were the same commit). Cost: none, because the check exists.
- pr-harden:round cap — the default cap of 4 was reached with a real, one-case finding outstanding. Raised to 6 explicitly rather than ending as did-not-converge over a coverage gap; rounds 5 and 6 then converged. Every round 1-5 found a genuinely DIFFERENT defect, which is the signal that distinguished "loop working" from "loop spinning".

## Declined
- Make both corroboration folds span the SUBSTANCE unit — declined twice: measured not to close the arrangement it targets (the record's sections fold over the rendered row's rules alone), and it reddens aCorroboratedRuleOnANEIGHBOURRow..., a case it must not break. A real fix would change what a record or chip SAYS, which #308 is deliberately monotone about.

## Assumptions review overturned
- "The two injected channels cannot disagree once one predicate is shared" -> they can, on four further axes (collapsed key, row, subject-matter gate, clause text); three were closed and the row axis is declared as a two-directional residue with a case per direction.
- "This change is purely additive so it cannot create a divergence" -> adding a clause where main printed agreement IS how a disagreement is created; that reasoning was removed from both its homes.


## After-the-fact capture (added by the 2026-08-25 third retro, flagged as such)

Written from the session transcript AFTER the run finished, because the retro cited these to "the run"
and a refuter found them in no record — the defect this ledger parks at 4 cycles / 3 records. They are
true of the run; they were simply not captured. Flagged so a later reader can weigh them as amendment
rather than as contemporaneous capture, the way #250 and #269 amended theirs.

- **The `git checkout --` incident and what replaced it.** The probe that lost work was on a file
  carrying the uncommitted ledger fix. Every later probe in the run snapshotted the file to the session
  SCRATCHPAD (outside the repo) with `cp` and restored from there — `DSV.bak`, `DSV2.bak`, `DSV3.bak`,
  `DSV4.bak`, `fx.bak` — and no further incident followed. Also adopted: committing before probing.
- **Prose-correction cycles.** Harden cycles 4, 5 and 6 changed documentation only, and each corrected
  prose an earlier cycle of the same run had written; two of those corrections were themselves found
  false (the "only two things can meet on one key" universal, and the mutation instructions naming a
  `RaisedChip.origin` that no longer existed). Review rounds 2, 3 and 5 each also corrected prose from
  an earlier round.
- **A delegated agent's review target moving under it.** Three subagents reported the branch advancing
  mid-review. The cycle-4 quality agent re-verified every finding against a new HEAD and recorded that
  one of its candidate findings had been made moot by a commit that landed while it worked; the
  cycle-4 correctness and cycle-3 test-coverage agents each said the branch had moved and re-measured.
  The `pr-harden` rounds did not have this shape: each was pinned to an immutable fetched
  `pr-313-r<round>` ref.
- **The verifier's stray database process.** Killing the standalone's app and launcher left its
  `mariadbd` holding the datadir lock (`Can't lock aria control file … error: 35`), so the next launch
  failed with `Failed to initialize component [Connector["http-nio-8081"]]` and presented as a port
  conflict. Cost roughly twenty minutes across three launch attempts and one misdiagnosis before the
  DB process was identified and killed by datadir.
- **The round cap.** `pr-harden`'s default cap of 4 was reached with one real finding outstanding; it
  was raised to 6 and rounds 5-6 converged. The discriminator recorded at the time was that rounds 1-5
  each found a genuinely different defect rather than re-raising one.
