# resolve-ticket + harden + pr-harden · openmrs-module-chartsearchai · #340 / PR #344 · 2026-08-31
outcome: converged
rounds: 1 (pr-harden)   cycles: 2 (harden; 9 Phase-2 passes in cycle 1)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced · one session rate-limit hit mid-run (agent died, retried)
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-openmrs-module-chartsearchai/51da7d8f-a2ff-421e-951d-3ca805989c7a.jsonl

## Refuted by measurement
- "a rated rule with no mechanism text names its rating nowhere in its own sentence" (the change's own stated reason, in 6 homes) -> false for sourceFormat=ddinter: noteFor writes a note for EVERY row and always leads with the rating; 0 of 590,312 links are an exception. Reachable only on json. · cost: 1 Phase-2 pass
- "detail is one complete standalone sentence" -> a rated interaction's is two or more · cost: 1 pass
- the replacement rule "one for a contraindication, two for a rated interaction, three when folded" -> 61.4% of shipped links produce 3+, bundled seed's folded chip is 2 · cost: 1 pass
- "detail opens on the drug's own name" -> an overdose detail is "The stated <drug> dose ~..."; 6 existing cases assert it with startsWith · cost: 1 pass
- "the rating is the first word after the em dash" -> the chip embeds the order display UNQUOTED, so free text can carry that delimiter first (NonCodedDrugOrderNameTest pins it) · cost: 1 pass
- "the module reads the unrated chip as the weaker of the two" -> unrated ranks MAX_VALUE vs Minor's 1 · cost: 1 pass
- gate pass 1 (plan): "null means the finding carries no rating" implies non-null means it HAS one -> severityRank maps unrecognised non-null identically to null; three classes on the wire, not two · cost: 0 (caught pre-code)
- gate pass 2 (plan): "the field says exactly what the prose quotes" -> false for a rated rule with no note, which is the plan's own T3 arrangement · cost: 0 (caught pre-code)

## Raised by a fresh agent, missed by the author
- [harden P2r1] the change's stated REASON was measurably false in 6 homes · non-blocking · cost: 1 pass
- [harden P2r1] RestControllerContext's javadoc list of contextless classes was stale by one · non-blocking
- [harden P2r2] the folded-chip paragraph added the pass before had the ordering backwards · non-blocking
- [harden P2r2] ADR Decision 59 missing from the ADR table of contents · non-blocking
- [harden P2r2] the reflective guard fails OPEN under `mvn -pl omod test` (stale ~/.m2 api jar) · non-blocking
- [harden P2r7] appending a class name to a list broke the next sentence's "that last class" referent · non-blocking
- [harden P2r8] the eval scorer still carried the retracted positional claim, and this branch had ADDED a sentence offering the note census as proof of it · non-blocking
- [pr-harden r1] STRONG mutation on the reflective guard: it asserted only containsKey, so `map.put("partnerMoiety", null)` beside a new public accessor satisfies it while dropping the value — #340's own defect, shipped green under a test that appears to cover it. Demonstrated green by the reviewer. · non-blocking · cost: 0 rounds (fixed at FINISH)
- [pr-harden r1] the chip section never stated where the four recognised ratings rank; Unknown is RECOGNISED and the LOWEST (rank 0, below Minor), so "unrated sorts above Major" invites inverting the order for 84,830 of 590,312 links · non-blocking
- [verifier] OpenMRS did NOT re-expand the module's loose classes on restart: the first boot ran week-old controller bytes while the deployed .omod timestamp, the module status endpoint and the lib-cache marker all read current. Caught by hashing the loaded class against the omod entry; fixed by rm -rf appdata/.openmrs-lib-cache/<module>.

## Where a skill blocked or contradicted this run
- harden:Termination — 9 Phase-2 passes in one cycle, each finding real prose defects, because each fix introduced the next false claim. What ended it was the skill's own "delete the CLAIM SHAPE" rule, applied after the second refutation of the same shape rather than after the fourth. The rule works; it was applied later than it should have been.
- pr-harden:step 6 — the verifier's step 5 ("confirm the deployed .omod timestamp matches the build") is NOT sufficient on OpenMRS: the lib-cache keeps expanded loose classes that the timestamp check cannot see. The sufficient check is hashing the loaded class in appdata/.openmrs-lib-cache/<module>/ against the same entry in the built omod.
- environment — the machine hit 100% disk mid-run (873Gi/926Gi); Bash failed with ENOSPC and an agent worktree could not be created. 162 agent worktrees totalling 22G had accumulated under .claude/worktrees/ across runs. Removing this run's own ten freed 2.3Gi and unblocked it. Agent worktrees are not being cleaned up across runs.

## Declined
- publish a derived discriminator (a `rated` boolean or canonical rank) beside the verbatim value — if we ship without this, no runtime break; a client that skips the documented trim/case-fold or floors an unrecognised value mis-ranks a chip, bounded to operator-authored datasets since the shipped one emits only the four canonical spellings. It is a second published field with its own contract and its own #283 question, so it is a decision on its own evidence rather than a rider on this one. Recorded as an open alternative in ADR Decision 59.

## Assumptions review overturned
- "the field says exactly what the prose quotes, so publishing it asserts nothing new" -> false in both directions; replaced by a statement about the KIND of assertion (the source's rating, not the module's judgment) — gate pass 2 and harden P2r8
- "the guard makes a rule the class already states enforced" -> the class stated the OPPOSITE for this field ("Not serialized onto the REST response"); it is a NEW contract and its javadoc now says so — gate pass 2
- "asserting the key is present is enough for the guard" -> a placeholder put satisfies it; it now compares the accessor's own reading against the published value — pr-harden r1

## Post-hoc: merge conflicts (user-requested, after the run converged)
- `main` gained two PRs while this branch was open and the PR went CONFLICTING. Four files overlapped; two conflicted textually.
- **ADR NUMBER COLLISION, exactly the case pr-harden warns about.** main took Decision 59 AND 60; this branch's 59 was renumbered to 61. It had SIX homes — the heading, the TOC, two inline supersession markers (on Decisions 23 and 51), README's census pointer, and a javadoc pointer in SafetyWarning. Searching for the NUMBER rather than for a phrasing is what found them all; the skill's rule is correct and was load-bearing.
- **A merge can be textually clean and semantically falsifying.** main's #341 refactored the three emission sites onto a shared `putSafetyChips` (which also writes a new `interactionPairs` key) and added a guard failing the build if `serializeSafetyWarnings` is named outside it. Git auto-merged the controller correctly, but three of this branch's CLAIMS became false — the ADR's per-site mutation recipe, the test class javadoc, and getSeverity()'s wire paragraph, all of which described the three sites as naming the serializer directly. Nothing in the merge flagged them; they were found by grepping this branch's own claims about the structure main had changed.
- The rewritten mutation recipe was RE-MEASURED on the merged tree rather than assumed: deleting the severity put reddens 7/7; making the blocking site's putSafetyChips write an empty list reddens the 5 cases that call searchChips().
- Re-verified at runtime on the merged head: severity and interactionPairs coexist at all three sites, no repairs needed. The lib-cache staleness trap was avoided this time by purging appdata/.openmrs-lib-cache/<module> before boot and proving the loaded class's sha256 against the omod entry.
