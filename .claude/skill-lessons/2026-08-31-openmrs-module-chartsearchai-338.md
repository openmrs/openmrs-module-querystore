# resolve-ticket + harden + pr-harden · openmrs-module-chartsearchai · #338 / PR 343 · 2026-08-31
outcome: converged
rounds: 1 (pr-harden)   cycles: 4 (harden)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-338/7af57184-007f-45b9-82a8-c0b3035f49ad.jsonl

## Refuted by measurement
- "A drug-name fidelity rule is the analogue of the code check: report `A (B)` where the head is a truncation of the gloss." -> Over the shipped KB, 2 of 2283 `displayLabel()`s ARE that shape (`Ethinylestradiol (ethinyl estradiol)`, `Flax seed (flaxseed extract)`), and 36 of the 1840 single-token names stand in a proper-prefix relation, mixing spelling variants of one substance, biosimilar suffixes and genuinely different drugs. The rule fires on the module's own correct labels. · cost: 0 rounds (caught at the plan gate, before code)
- "The gate says two named methods are package-private and callable from a same-package test." (gate pass 2, on #263's pattern) -> Not applicable here; but the same shape recurred: gate pass 2's blocking objection settled the question rather than opening one, and no third pass was run. · cost: 0
- "Every renderer in this module writes one code per parenthetical and no record states a list." -> False in the same javadoc paragraph that then says the reference record renders a list. Replaced by a measurement over the data (1,187,473 free-text fields, 0 stating an ATC token). · cost: 1 harden pass (self-caught on diff review)
- "Reading every balanced group fixes the stray-`(` fail-open." -> It also puts a child's codes into its parent: `(levofloxacin (J01MA) and moxifloxacin (J01MA))` reported a repetition and `(ATC class (J01MA) [3])` a misplaced marker, both correct prose; and it was quadratic (13.9 s on 32 KB, 3.5 MB WARN line). · cost: 1 cycle
- "Reading each parenthetical at its own level fixes it." -> Deleting the nested span WELDS the text either side: `(J01M(sic)A, J01MA)` manufactured a repetition, `(J01MA [0(sic)3])` a marker with a real index. A space, not nothing. · cost: 1 cycle
- "The `retainAll(cited)` intersection keeps bracketed clinical values out." -> `extractCitedReferences` promotes every IN-RANGE bracket, so it only excludes numbers the chart has no record for. The test passed because 97 was out of range. · cost: 1 cycle
- "The architecture guard pins the marker contract." -> Three of four ordinary relocations walked through it green (nested class by concatenation, hand-rolled `charAt` walk, `INLINE_CITATION.matcher` direct). Only a positive assertion — the decode step must be CALLED — closes them. · cost: 1 cycle

## Raised by a fresh agent, missed by the author
- [harden c1] The architecture rule fired on COMMENT lines, so documenting the rejected alternative inside the class it is about broke the build — the very text ADR 59 spells. · non-blocking · cost: 1 cycle
- [harden c1] The entry point kept the name `reportUnsupportedClassCodes` after growing a second responsibility; its javadoc described only the first. · non-blocking · cost: 1 cycle
- [harden c2] The whole-text walk's false positives and quadratic cost (above). · cost: 1 cycle
- [harden c3] The weld; the `retainAll` over-claim; the `597,161` figure not matching the field list it was stated over (one field per interaction counted, two enumerated); CLAUDE.md wrong on three counts (comment lines skipped, `stripCitationMarkers` legitimately reads the pattern, the guard does not catch a dialect beside a retained call). · cost: 1 cycle
- [pr r1] Rule 2 reports a legitimate aside that states a code and carries the marker attributing its own clause; ADR 59's residue list read as exhaustive and omitted it. · non-blocking · cost: 0 rounds (applied at FINISH)

## Where a skill blocked or contradicted this run
- harden:Termination — four cycles were owed and run; cycle 4 was the first to change nothing. Each of cycles 1-3 found a real correctness defect, so the rule paid for itself three times.
- Agent tool — the opus session quota was exhausted mid-run and a cycle-4 agent died with HTTP 429. A retry on `model: sonnet` succeeded and returned a clean, thorough report. That is the "change something between attempts" the retry contract asks for, and it is worth recording as a concrete remedy.
- pr-harden:Step 6 — my own verifier brief asserted the check emits a DEBUG line when the answer states no ATC token. It does not; that gate is a bare `return`. The verifier caught the brief rather than the code.

## Declined
- (none — the single non-blocking finding was implemented)

## Assumptions overturned
- "The ticket's preferred direction (findings as a list the model does not restate) is the root-cause fix." -> It removes the verdict lead #107/#283 require, and is gated only by a live eval this repo records as regression-prone. Direction (b) taken, with the refusal stated and cited. (plan stage)
- "Detection alone is a thin deliverable." -> The live verifier reproduced #338's captured answer byte-for-byte on the reported patient and both new reports fired on it, which is the strongest evidence the run produced and is only obtainable because detection landed first.
