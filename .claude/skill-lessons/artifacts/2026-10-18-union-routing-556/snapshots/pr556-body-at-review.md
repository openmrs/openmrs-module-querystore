Fixes #555

**What the ticket asked.** When a cited finding names an active order by the knowledge base's label, e.g. *Rifampicin (rifampin)* for her *Rifampicin* order, an answer that names the order *"Rifampicin"* should count as having named it. Today such an answer gets *"Also covered by those findings and not named above: active order Rifampicin (rifampin)."* appended, and `findingPartners.stated` is one short.

**What the change does.**
- A partner is now stated where the answer contains its printed name, as before. It is also stated where the drug-name **prose rule** (`DrugReference.matchesText`, through the new `DrugSafetyValidator.namesThePartner`) reads the answer as naming the rows the finding's chip resolved that partner to.
- `measure` and `withUnstatedPartnersNamed` both read one decision, `FindingPartnerCoverageCheck.statedPartners`. So the count and the appended sentence cannot disagree.
- The rows travel with the finding and are never re-resolved from the printed label:
  - `SafetyWarning.rowsOfPartner` is set at the two active-order arms' rule-chip sites and unioned by `collapseSharedMechanisms`.
  - `DrugReferenceInjector` writes it as row ids onto the new `RecordMapping.getFindingPartnerRows()`, index-aligned with `getFindingPartners()`.
  - The new constructor parameter sits after `findingBridgeNames` in both rungs, #514's placement, so `ArchitectureGuardTest`'s descriptor tails are unchanged.
- **Stripping the label's trailing parenthetical was proposed and rejected at plan review.** A substring of a stem would be a fourth answer to "does this prose name this drug", which reference `CLAUDE.md` (*Matching a drug name*) forbids. PR #478 round 2 had already replaced a substring test of such a label with the prose rule.
- **Residue, and which way it errs:**
  - Issue #477's two same-substance findings and the condition-mediated finding carry no rows, so their orders are stated only by their printed names. That errs toward reporting a missing order, pinned by `ordersOfOneSubstanceAreNotStatedByThatSubstancesName`.
  - The prose rule's own residue comes with it: an alias the data files under two substances names both.
- Docs updated to match: ADR Decision 100 (new amendment), the README `findingPartners` paragraph, `ChartSearchService.FindingPartnerCoverage`'s canonical javadoc, and one sentence in `InteractionClaimPairFidelityCheck`.

**How it was verified.** New cases in `CitedFindingPartnerCompletionTest` drive the real `search`/`searchStreaming` over findings the real injector wrote from the shipped knowledge base. Only the model is stubbed.
- The ticket's alprazolam answer (on both paths) and its medication-screen question are returned byte for byte, with `findingPartners` `{2,2}` and `{1,1}`. Before the fix, the alprazolam case appended exactly the ticket's sentence.
- A merged ivosidenib finding naming Phenytoin and *Rifampicin (rifampin)* reads `{2,2}` for an answer naming "phenytoin and rifampicin".
- The #477 residue case described above.

Each new write site was mutated in turn, and a named case reddened for each:
- the drug-in-play arm's rows: the two alprazolam cases and the merged case
- the screening arm's rows: the screening case
- the merge: the merged case
- the prose-rule arm: all of the above

`mvn -o clean install` from the root: 0 failures.

🤖 Generated with [Claude Code](https://claude.com/claude-code)
