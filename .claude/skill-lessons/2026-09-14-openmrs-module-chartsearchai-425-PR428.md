# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #425 / PR 428 · 2026-09-14
outcome: converged
rounds: 2   cycles: 6 (harden, pre-PR)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-425/f41742d4-2973-4737-8802-1334f1e9895b.jsonl

## Refuted by measurement
- "narrowing the comma arm only ever ADMITS more, so it can only turn a false REPORT into silence" -> DosingCeilingFidelityCheck's walk returns early only on published.get(0); an admitted LAXER ceiling CONSTRUCTS the report. The v1 design's whole safety argument. · cost: 1 gate pass
- "no structural narrowing of the comma arm can be sound, because a comma between two digit runs is ambiguous three ways" -> gate pass 2 produced the conjunction (run >= 4 AND tail == 3) meeting all seven cases; each half had been killed separately in pass 1, the conjunction never measured. · cost: 1 gate pass
- "the group length alone reads 0,5 mg/day as a list" -> 0,5 is refused under EITHER half alone; the sentence was gate pass 1's measurement of the v1 POLARITY, carried across the redesign. The run-length half's real pin is aThousandsSeparator..., in the false-SILENCE direction. · cost: 1 cycle
- "no grouped number can be spelled that way" -> 20000,500 mg/day (partially grouped) IS admitted; correction reached 1 of 5 homes on the first pass. · cost: 1 cycle
- "every group after the head is exactly three digits, so any other length says the digits after the comma are a number of their own" -> argues for digits != 3 beside code admitting on digits == 3; the comment justified the negation of its own line, in 3 homes. · cost: 1 cycle
- "the one- and two-digit ceilings are the reachable direction" -> the four-digit direction is reachable on a shipped fixture pair (Ceftriaxone 4000/2000) and fires in the severe direction. · cost: 1 cycle
- "the tail bound's TWO residues ... the short-ceiling list" -> contradicted the list one sentence above it, which had just been corrected to say both directions. · cost: 1 cycle
- "every other residue it carries silences a report, and this one INVENTS one" -> pre-existing and already false (the check's own list names the respelling residue as reported); #425 made it more so. · cost: 1 cycle

## Raised by a fresh agent, missed by the author
- [gate 1] the monotonicity claim above · blocking · cost: 1 gate pass
- [gate 2] the conjunction exists, and Refs-not-Fixes is owed · blocking · cost: 1 gate pass
- [cycle 1 P2] eight false claims in one javadoc, incl. the digits!=3 inversion and "both unpinned" · cost: 1 cycle
- [cycle 2 P2] the clause's own test was NAMED for an operand the rule never examines (MIDDLE vs LAST number of a three-item list) · cost: 1 cycle
- [cycle 3] the "only residue that invents" claim · cost: 1 cycle
- [cycle 5] the two-residues/short-ceiling contradiction · cost: 1 cycle
- [r1] **both numeric bounds pinned against DELETION but not against MOVEMENT** — `comma - start < 4` -> `< 3` left the whole build green while flipping ordinary grouped numbers (300,500; 1,234,500; 24,000,500) to admitted, which on this check produces a false report on a published key. The only case exercising that arm used a ONE-digit head group. · blocking · cost: 1 round
- [r2] the three-item-list case rests on an unasserted fixture order; the residue list states no direction for its admitted shapes · non-blocking · filed as #430
- [r2, beyond brief] attacked the bounds in the direction the javadoc does NOT name (tightening to `< 5`) and proved both pinned in both directions; ran a shape mutation (widening the left walk to cross commas) proving the "1,234,500" element of the new test is load-bearing rather than decoration

## Where a skill blocked or contradicted this run
- harden Termination lists "its comparison loosened" among the owed mutations; I ran only deletions/disablings. That exact omission is what round 1 caught, one round later. The list was in front of me.
- harden Termination: cycles 2-5 each found their findings in prose the previous cycle had written. What ended it was deleting the claim SHAPE (four accounts of what the tail bound MEANS were each refuted), not writing a better account. The skill says this; it took four cycles to apply.
- resolve-ticket Step 3's three-outcome rule worked as designed: two blocking objections, neither a deadlock, because each SETTLED rather than opened. No third gate pass.
- pr-harden State: `gate-state declined` is append-only, so a placeholder row I wrote ("round 1: nothing declined") cannot be removed without retyping the locking mechanism the skill forbids. A `--clear-declined` or an idempotent no-op would help.
- Both cycle-2 Phase-2 lenses died on a session 429 immediately after confirming their trees. The reset was the fix; the leaner-brief retry was not needed and cannot be separated from it.
- `-DfailIfNoTests=false` is the wrong flag for this surefire and fails the omod stage with "No tests matching pattern"; `-Dsurefire.failIfNoSpecifiedTests=false` works. Also `-Dtest='A+B'` silently matches nothing — it produced one falsely-clean mutation check, which read as "all three clauses are dead".
- `git checkout -- <path>` inside my own mutation loop discarded an uncommitted javadoc fix. Caught only by grepping for the claim afterwards.

## Declined
- The efficiency lens's ~1us gain from running the needle-bounded walk first — if we ship without this, a pathological all-digit answer costs about 1 microsecond more per call; the lens that measured it advised against touching a method whose wording had already been revised twice for that.
- Adding ", never re-derived at a consumer" to the restored CLAUDE.md clause — if we ship without this, the directive is still carried by the sibling bullet two lines above and by both LlmInferenceService call sites; adding it costs ~30 bytes against 28 of headroom and would trip the very budget item 3 is about.
- Tightening DrugReference's "refuses a leading '.' or ',' that marks the tail of a longer NUMBER", which as a universal is falsified by the two admitted residues — if we ship without this, a reader could take the sentence as exhaustive; the clause immediately after it disclaims exactly that and names numericFragment as canonical, so the reader is told where the rule lives before they can be misled.

## Assumptions review overturned
- "Item 1 takes the ticket's remedy 1 (name it), because remedy 2 cannot be made sound" -> overturned by gate pass 2; remedy 2 shipped in the conjunction form, and the ticket's own sketch shares the residue.
- "This repo's structural guards pin WHERE a predicate may live, never a phrase's SPELLING" (the ground for declining a source-text canary) -> false; ArchitectureGuardTest.noHardcodedEmbeddingPrefixes pins ten literal prose spellings. The decline survived on two other grounds (no reader sees javadoc; the paragraph is expected to move).
- "the run's LENGTH is all that is asked" / "the comma is asked about the LENGTHS rather than about any character" -> the arm asks Character.isDigit(before) first; corrected.
