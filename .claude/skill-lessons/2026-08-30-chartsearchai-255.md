# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #255 / PR #335 · 2026-08-30
outcome: converged
rounds: 1   cycles: 3 (harden)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-255/96268822-9c6b-4522-89a4-283526e55145.jsonl

## Refuted by measurement
- "Widening the existing 4-arg validate in place breaks 3 test call sites" (refutation gate pass 2's own estimate, from a grep of one file) -> the compiler says 33, across three files. Flipped the design back to an added overload + re-targeted needles. · cost: 1 implementation attempt
- "A question naming a drug can observe a wrong threaded list" -> it cannot: its findings come from the arm reading the context's reference names, which the injector attaches before calling in. Threading `Collections.emptyList()` left all three cases green. Moved the guard to a screening question, which reddens with 4 Major findings against none. · cost: 1 Phase-1 pass
- "The two structural needles' second line alone is enough" -> a sibling method carrying the same parameter tail hijacks it (with a rename); needles re-anchored to two-line literals. · cost: 1 Phase-2 pass
- "A sibling with the same tail alone silently steals a tail-only needle" (written as the justification for the fix above) -> measured false: a sibling alone makes TWO matches and fails loudly. Only sibling PLUS rename/re-wrap gets through. · cost: 1 Phase-2 pass
- "the pass-total delta exceeds the resolution time removed" (from un-alternated runs) -> a proper A/B shows it is essentially all the removed resolution. Retracted. · cost: 0 (caught by a reviewer's own A/B)
- "`lowerCaseNames` distinguishes what the chart records" -> inert: `PatientClinicalContext`'s constructor lower-cases either way. Parameter dropped. · cost: 1 Phase-2 pass
- ADR "11 of the 33 pass null mappings" -> three different parsers gave 11/22, 11/23 and 9/24. The split is now unpublished; the total is the compiler's own count. · cost: 1 Phase-2 pass
- The javadoc claim "no test in the module sees a mutation of this list" (present tense) -> its own remedy had made it false. · cost: 1 cycle

## Raised by a fresh agent, missed by the author
- [harden P1] Four unchanged neighbours whose documented invariants the change falsified (injector comment, validate's "ONE dataset sweep", ADR 54 and 55 trade-offs, CLAUDE.md's Decision-55 pointer) · non-blocking · cost: 0
- [harden P2] The read-only invariant the change made load-bearing was unpinned — `Collections.reverse` after validate's reads left all 1585 tests green · major · cost: 1 pass
- [harden P2] The `unmodifiableList` remedy was ITSELF unpinned — removing it left the suite green · minor · cost: 1 pass
- [harden P2] `findingTexts` inserted between `injectedFindings`' javadoc and the method, orphaning it — found independently by three agents, and it is the exact lesson already in my own memory file · minor · cost: 1 pass
- [harden P2] "every other list parameter in this class is read-only" — false; `List<SafetyWarning> warnings` accumulators are written · major · cost: 1 pass
- [pr-harden r1] The unmodifiable contract was stated only in an inline comment and CLAUDE.md, not in the javadoc a public caller reads · non-blocking · cost: 0 (applied at FINISH)

## Where a skill blocked or contradicted this run
- pr-harden:"### 6 — VERIFY" step 2 — `/usr/libexec/java_home -v 1.8` is named as the fix for a JDK mismatch, but this repo's pom targets Java 11 and that command resolves to the applet-plugin JRE on this box. The verifier lost an attempt to "No compiler is provided in this environment" before finding temurin-11. The advice should be "the JDK the pom targets", with 1.8 as an example rather than the value.
- pr-harden/harden:"State"/"Record the cycle" — `gate-state --count-edits` reported `edits=0` for a cycle with 9 unpushed commits, because the branch had no upstream at that point. The cycle gate's own measurement was therefore not what the skill's prose says it is ("uncommitted lines plus commits not yet pushed"); I measured with `git status` + `git log` instead, as the gate sentence requires.
- harden:"Phase 2" — the mutate-and-restore instruction destroyed three of my own uncommitted javadoc fixes when I ran `git checkout --` to revert a measurement mutation on the same file. Three separate agents then flagged the missing fixes as defects. The skill names this hazard for AGENTS' edits; it bit the orchestrator's own.
- General environment — `gh issue view <n>` without `--json` printed nothing at all (exit 0, empty). Every brief had to say so.

## Declined
- Extracting the two-line needle literal into a shared constant — if we ship without it, a later edit to `validate`'s declaration re-targets one guard and leaves the other pointing at absent text, but that fails the build LOUDLY with a message naming the needle, so it cannot fail silently.
- Retiring the ten hand-written finding-text loops in `UncorroboratedFindingProvenanceTest` — if we ship without it, a change to what `renderFinding` appends is enforced against the shared helper's callers only; pre-existing, out of this ticket's scope, and the helper's javadoc now says so instead of claiming sole ownership.
- Closing the `Supplier`-lambda / `for`-loop evasion of `ChipSubjectOneResolutionTest`'s construction guard — if we ship without it, a future per-arm split written that way stays green; pre-existing, identical under the old needle, and recorded in ADR 56.

## Assumptions review overturned
- "Widening `validate` in place is the cheaper design" (adopted after gate pass 2 preferred it) -> reverted to an added overload once the compiler showed 33 broken call sites rather than 3; round of implementation, before any review round.
- "The premise case pins the trap DrugSafetyValidator's comment recorded" -> it does not, on this excerpt; the javadoc now says what it cannot see. Phase 2 pass 4.
