#!/usr/bin/env python3
"""Build instances.jsonl from the candidate list below, each located and verified by gtlib.locate."""
import json
import sys

from gtlib import LocateError, locate

OUT = "/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/db22ea60-d89d-4e0d-808b-b04c38a332ec/scratchpad/gt/instances.jsonl"

A = "api/src/main/java/org/openmrs/module/chartsearchai/"
AT = "api/src/test/java/org/openmrs/module/chartsearchai/"
O = "omod/src/main/java/org/openmrs/module/chartsearchai/"
OT = "omod/src/test/java/org/openmrs/module/chartsearchai/"
RES = "api/src/test/resources/chartsearchai-test/"
ADR = "docs/adr.md"
RM = "README.md"
R = "2026-09-24-openmrs-module-chartsearchai-"

# transcript files (~/.claude/projects/...)
TX = {
    433: "-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-433/dbfc9042-552f-4c7d-aed9-be5ea137bfe1.jsonl",
    438: "-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-438/f3ad137e-07c2-4842-8334-41f4b6a67a99.jsonl",
    516: "-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-516/df7f369c-a15b-4a24-ba6c-0e83246137ec.jsonl",
    477: "-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-477/608ffbd8-3a07-431b-b0e8-fc2713a49842.jsonl",
    512: "-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-512/025bd85a-7f0e-4151-acda-9a4b0578c836.jsonl",
    407: "-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-407/1244e489-9b33-4380-8c35-9b7a7ef2ef8b.jsonl",
    505: "-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-505/2463cfcc-5579-4630-9cef-6790a536dcf1.jsonl",
    273: "-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-273/57517475-94c2-455f-9557-9174614051d6.jsonl",
    272: "-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-272/c69f3eac-b1ff-4fd4-b43e-ddd965659462.jsonl",
    432: "-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-432/2348f3bc-6c67-4129-b67d-f313a5913b10.jsonl",
    443: "-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-443/b4f0a7c3-2e1d-44e6-875d-d7e9f0e1cde7.jsonl",
    459: "-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-459/74c32ea2-72d8-47fa-860f-8459b95a9225.jsonl",
    527: "-Users-danielkayiwa-Projects-openmrs-chartsearchai/ebc78818-5b31-4fed-9943-24ca301a3943.jsonl",
    515: "-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-515/1d4126e7-e46f-4406-a584-25ab451be1d8.jsonl",
}


def tx(ticket, line):
    return "~/.claude/projects/%s:%d" % (TX[ticket], line)


# (ticket, pr, fix, file, phrase, mode, claim, found_by, record, source, transcript_ref, notes)
C = []


def c(ticket, pr, fix, path, phrase, claim, found_by, record, source, tref=None, notes=None, mode="removed",
      borderline=None):
    C.append(dict(ticket=ticket, pr=pr, fix=fix, path=path, phrase=phrase, mode=mode, claim=claim,
                  found_by=found_by, record=record, source=source, transcript_ref=tref, notes=notes,
                  borderline=borderline))


NEC = "borderline: still true as a NECESSARY condition; stale as a statement of the gate (the harden c1 integration lens had judged the validate comment 'still true as a necessary condition'; r1 called both 'now inaccurate')"

# ---------------- #433 / PR 511: round 1 (the gate stated as entries, not substances)
c(433, 511, "30905bd7", A + "reference/DrugSafetyValidator.java", "That arm needs questionDrugs.size() >= 2",
  "C433-gate-entry-count", "pr-harden r1 reviewer (r1-1)", R + "433.md:17", "record", tx(433, 701), NEC, borderline=True)
c(433, 511, "30905bd7", A + "reference/DrugSafetyValidator.java",
  "arm needs the question to resolve two or more reference drugs", "C433-gate-entry-count",
  "pr-harden r1 fixer (a home it swept while fixing r1-1; the reviewer named the validate comment and the examples doc)",
  R + "433.md:17", "record", tx(433, 718), NEC, borderline=True)
c(433, 511, "30905bd7", "docs/ddi-interaction-question-examples.md", "the question resolves **≥2** reference entries",
  "C433-gate-entry-count", "pr-harden r1 reviewer (r1-1: 'examples doc table + prose')", R + "433.md:17", "record",
  tx(433, 701), "the 'Fires when' column states a sufficient condition, which the change made false", borderline=False)
c(433, 511, "30905bd7", "docs/ddi-interaction-question-examples.md",
  "the question-pair arm needs two or more resolved drugs", "C433-gate-entry-count", "pr-harden r1 reviewer (r1-1)",
  R + "433.md:17", "record", tx(433, 701), NEC, borderline=True)

# ---------------- #438 / PR 517: harden Phase 2 and round 1
c(438, 517, "1c1e6b0c", ADR, "one upstream delta chunk becomes one frame", "C438-one-frame-per-delta",
  "harden c1 Phase 2 integration lens", R + "438.md:13", "record", tx(438, 266))
c(438, 517, "1c1e6b0c", ADR, "each upstream delta becomes one frame", "C438-one-frame-per-delta",
  "harden c1 Phase 2 integration lens", R + "438.md:13", "record", tx(438, 266))
c(438, 517, "1c1e6b0c", ADR, "needs state in a per-event writer", "C438-per-event-writer-state",
  "harden c1 Phase 2 quality lens", R + "438.md:13", "record", tx(438, 265))
c(438, 517, "1c1e6b0c", O + "web/rest/ChartSearchAiRestController.java", 'must not now read as "held back"',
  "C438-throws-hazard", "harden c1 Phase 2 quality lens", R + "438.md:13", "record", tx(438, 265),
  "the record's '@throws reason named the wrong hazard'")
c(438, 517, "800da6dd", OT + "web/rest/ChartSearchAiSseSurrogatePairTest.java", "Each chunk becomes one frame",
  "C438-one-frame-per-delta", "pr-harden r1 reviewer (r1-1)", R + "438.md:14", "record", tx(438, 348))

# ---------------- #516 / PR 522: harden Phase 2 (first pass -> b6562008) and Phase 2b (-> 1b747d71)
c(516, 522, "b6562008", A + "api/impl/FindingPartnerCoverageCheck.java", "<p><b>Not shared with {@link #measure}</b>",
  "C516-not-shared-with-measure", "harden c1 Phase 2 quality lens (substantive; escalated)", R + "516.md:16", "record",
  tx(516, 741))
c(516, 522, "b6562008", A + "api/ChartSearchService.java",
  "differently reads as unstated, so the residue runs toward reporting a shortfall", "C516-slash-spacing-unstated",
  "harden c1 Phase 2 quality and reuse lenses", R + "516.md:17", "record", tx(516, 741),
  "sentence starts on the previous line ('A name the model spells')")
c(516, 522, "b6562008", A + "api/ChartSearchService.java", "one count per finding naming an order",
  "C516-count-unit", "harden c1 Phase 2 quality lens (polish 5)", R + "516.md (not listed)", "transcript", tx(516, 741))
c(516, 522, "b6562008", AT + "api/impl/CitedFindingPartnerCompletionTest.java",
  "hands back the REAL chips the same arrangement raises — every chip", "C516-every-case-real-chips",
  "harden c1 Phase 2 quality lens (polish 3)", R + "516.md (not listed)", "transcript", tx(516, 741))
c(516, 522, "1b747d71", RM, "and the residue runs toward reporting a shortfall rather than toward silence",
  "C516-residue-one-direction", "harden c2 Phase 2 (2b) quality lens", R + "516.md:18", "record", tx(516, 818),
  "the lens: 'a gap in a sentence the diff rewrote; the gap itself predates the diff'")
c(516, 522, "1b747d71", A + "api/ChartSearchService.java", "reads as unstated, so the residue runs toward reporting a shortfall",
  "C516-residue-one-direction", "harden c2 Phase 2 (2b) quality lens", R + "516.md:18", "record", tx(516, 818))
c(516, 522, "1b747d71", A + "api/impl/FindingPartnerCoverageCheck.java",
  "the residue therefore runs toward REPORTING a shortfall", "C516-residue-one-direction",
  "harden c2 Phase 2 (2b) quality lens", R + "516.md:18", "record", tx(516, 818),
  "corrected by a qualifying sentence added in the same paragraph; the line itself is unchanged in the fix",
  mode="unchanged")
c(516, 522, "1b747d71", AT + "api/impl/CitedFindingPartnerCompletionTest.java",
  "Every order name an answer carries is read off the chips rather than spelled here.", "C516-names-read-off-chips",
  "harden c2 Phase 2 (2b) quality lens (polish 1)", R + "516.md (not listed)", "transcript", tx(516, 818))
c(516, 522, "1b747d71", AT + "reference/SharedMechanismChipCollapseTest.java",
  "it must read those names off the chip rather than recover them by", "C516-names-off-the-chip",
  "harden c2 Phase 2 (2b) quality lens (polish 2: 'unchanged neighbour, now stale')", R + "516.md (not listed)",
  "transcript", tx(516, 818))
c(516, 522, "1b747d71", A + "api/impl/FindingPartnerCoverageCheck.java",
  "@param answer the model's answer, read for the markers it anchors and for the orders it names",
  "C516-param-answer-models", "harden c2 Phase 2 (2b) quality lens (polish 4)", R + "516.md (not listed)",
  "transcript", tx(516, 818), "borderline: false only for answerFromTheModule's composed answer")

# ---------------- #477 / PR 523: harden c1 Phase 2 quality lens (-> 22635e60)
c(477, 523, "22635e60", RM,
  "On a question that asks to be screened and names no drug, two or more of her orders carrying the same substances raise one chip per set of orders",
  "C477-shared-substance-screen-only", "harden c1 Phase 2 quality lens (finding 1)", R + "477-PR523.md:16", "record",
  tx(477, 555), "whole warnOnInteractions table row is one line")
c(477, 523, "22635e60", A + "api/ChartSearchService.java",
  "and so is a screen's finding that several of her orders share a substance", "C477-a-screens-finding",
  "harden c1 Phase 2 quality lens (finding 2)", R + "477-PR523.md:16", "record", tx(477, 555))
c(477, 523, "22635e60", A + "api/impl/FindingPartnerCoverageCheck.java",
  "orders and a screen's finding that several of her orders share a substance", "C477-a-screens-finding",
  "harden c1 Phase 2 quality lens (finding 2)", R + "477-PR523.md:16", "record", tx(477, 555))
c(477, 523, "22635e60", A + "reference/PairChipExtent.java",
  "nor a screen's finding that several of her orders share one", "C477-a-screens-finding",
  "harden c1 Phase 2 (the third javadoc home the fix touched)", R + "477-PR523.md:16", "record", tx(477, 555),
  "the record's 'three javadocs'; the lens named two, the fix touched this third")
c(477, 523, "22635e60", ADR, "Moving the composer to the chips' order is the change that makes them agree",
  "C477-composer-chips-agree", "harden c1 Phase 2 quality lens (finding 4)", R + "477-PR523.md:16", "record",
  tx(477, 555))
c(477, 523, "22635e60", ADR,
  "said nothing about her two tuberculosis combinations sharing isoniazid, pyrazinamide and rifampicin",
  "C477-context-rifampicin-said-nothing", "harden c1 Phase 2 quality lens (finding 5)", R + "477-PR523.md (not listed)",
  "transcript", tx(477, 555), "the false subject ('the Rifampicin and Metformin questions') is on the previous line")
c(477, 523, "22635e60", ADR,
  "which relates her own medications when no drug is in play, has no class leg. Decisions 114 and 116",
  "C477-decisions-114-and-116", "harden c1 Phase 2 quality lens (finding 6)", R + "477-PR523.md (not listed)",
  "transcript", tx(477, 555))
c(477, 523, "22635e60", AT + "reference/DrugReferenceTestSupport.java",
  "her orders share a substance ({@link #ordersSharingASubstance}, issue #477) — which over a",
  "C477-classchipdetails-garbled", "harden c1 Phase 2 quality lens (finding 7a)", R + "477-PR523.md (not listed)",
  "transcript", tx(477, 555), "borderline: a misattached clause ('garbled'), false on its literal reading")
c(477, 523, "22635e60", AT + "reference/OrdersSharingASubstanceTest.java",
  "her medications and on a question about another drug", "C477-another-drug",
  "harden c1 Phase 2 quality lens (finding 7c)", R + "477-PR523.md (not listed)", "transcript", tx(477, 555))

# ---------------- #512 / PR 525: harden Phase 2 (-> 32d3047c) and Phase 2b (-> 981de3f2); ledger counted only the PR body
c(512, 525, "32d3047c", A + "api/impl/LlmEngine.java", "and keeps the penalty, with every test",
  "C512-every-test-double", "harden c1 Phase 2 quality lens (finding 2)", R + "512.md (not listed)", "transcript",
  tx(512, 418), "the universal continues on the next line ('double recording the value it was handed')")
c(512, 525, "32d3047c", A + "api/impl/LlmProvider.java", "which decides the engine's repetition penalty and nothing in the prompt",
  "C512-engines-repetition-penalty", "harden c1 Phase 2 quality lens (cosmetic)", R + "512.md (not listed)",
  "transcript", tx(512, 418), "borderline: true of the local engine, false of RemoteLlmEngine")
c(512, 525, "981de3f2", A + "ChartSearchAiUtils.java", "Four behaviours now hang off this one classification",
  "C512-four-behaviours", "harden c2 Phase 2 (2b) quality+integration lens (finding 1)", R + "512.md (not listed)",
  "transcript", tx(512, 462))

# ---------------- #407 / PR 526
c(407, 526, "827e4d5f", AT + "reference/InjectedContraindicationClauseTest.java", "where every other line",
  "C407-every-other-line", "harden c1 Phase 2 quality lens", R + "407.md:12", "record", tx(407, 227),
  "the claim continues on the next line ('about the sections lives')")
c(407, 526, "1dbde07e", AT + "reference/InjectedContraindicationClauseTest.java", "nothing else in this class renders",
  "C407-nothing-else-renders", "pr-harden r2 reviewer (note; fixed at FINISH)", R + "407.md:14", "record", tx(407, 372),
  "true when written (d55c5076); made false by the r1 fix's sibling case (ba31686b) — a seam between commits")
c(407, 526, "1dbde07e", A + "reference/DrugReferenceInjector.java", "switches are that cause's third and fourth",
  "C407-third-and-fourth", "pr-harden r2 reviewer (note)", R + "407.md (not listed)", "transcript", tx(407, 372),
  "borderline: the reviewer called it a count disagreement with the test comment's 'three ways'")

# ---------------- #505 / PR 529: round 2 (r2-3)
for fx in ("ddi-fold-outranked-token.json", "ddi-fold-own-substance-sibling.json", "ddi-fold-tied-token.json"):
    c(505, 529, "d2dd2c3e", RES + fx, 'which load as aliases, are not."', "C505-brand-names-are-not",
      "pr-harden r2 reviewer (r2-3, non-blocking, fixed under step 3's exception)", R + "505.md:43", "record",
      tx(505, 1053), "borderline: false on its literal reading ('are not' the dataset's own) — the brand names are absent")

# ---------------- #273 / PR 533
c(273, 533, "a17defd7", A + "reference/DrugReferenceValidity.java",
  "The same alias also failed CLOSED, through the dose arm, until issue #271.", "C273-pr-271-called-an-issue",
  "harden c1 Phase 2 reuse and integration lenses", "2026-09-24-openmrs-module-chartsearchai-273.md:13", "record",
  tx(273, 417))
c(273, 533, "a17defd7", A + "reference/DrugReference.java", "for it the display name is not a match at all",
  "C273-gate-excludes-every-such-entry", "harden c1 Phase 2 quality lens (false universal)",
  "2026-09-24-openmrs-module-chartsearchai-273.md:14", "record", tx(273, 418),
  "the clause is older than the change (the change rewrapped its line)")
c(273, 533, "a17defd7", A + "reference/DrugReference.java", "so for such an entry it answers false where this answers true",
  "C273-gate-excludes-every-such-entry", "harden c1 Phase 2 quality lens ('the same overgeneralisation is repeated at :525-528')",
  "2026-09-24-openmrs-module-chartsearchai-273.md:15", "record", tx(273, 418))
c(273, 533, "a17defd7", A + "reference/DrugReferenceService.java", "javadoc already records its gate as excluding",
  "C273-rowsof-stale-caveat", "harden c1 Phase 2 reuse lens ('rowsOf's javadoc ... now contradicts the new text')",
  "2026-09-24-openmrs-module-chartsearchai-273.md:15", "record", tx(273, 417))
for (path, phrase, who) in (
        (A + "reference/DrugReferenceValidity.java", "file need not do that, and {@code json} is the DEFAULT format",
         "harden c2 Phase 2 truth+reuse lens (deferred), then pr-harden r1 reviewer (r1-1)"),
        (A + "reference/JsonDrugReferenceSource.java", "The curated schema is the DEFAULT format, so the document",
         "harden c2 Phase 2 truth+reuse lens (deferred), then pr-harden r1 reviewer (r1-1)"),
        (A + "reference/PatientClinicalContext.java", "On the SHIPPED DEFAULT source format",
         "pr-harden r1 fixer (sweep of r1-1's claim)"),
        (AT + "reference/ActiveOrderContraindicationTest.java", "(the production default",
         "pr-harden r1 fixer (sweep of r1-1's claim)"),
        (AT + "reference/DrugReferenceValidityContextTest.java", "this is the default format, so an operator",
         "pr-harden r1 fixer (sweep of r1-1's claim)"),
        (AT + "reference/JsonDrugReferenceSourceTest.java", "— the production default — so this runs the real load path",
         "pr-harden r1 fixer (sweep of r1-1's claim)"),
        (AT + "reference/JsonDrugReferenceSourceTest.java", "which is the likelier of the two directions: this is the DEFAULT",
         "pr-harden r1 fixer (sweep of r1-1's claim)"),
        (AT + "reference/SelfNamedAllergyRuleFoldTest.java", "on the shipped default sourceFormat=json",
         "pr-harden r1 fixer (sweep of r1-1's claim)"),
        (AT + "reference/SubstanceCandidateSetTest.java", "is the DEFAULT sourceFormat",
         "pr-harden r1 fixer (sweep of r1-1's claim)")):
    c(273, 533, "c0146b75", path, phrase, "C273-json-is-the-default", who,
      "2026-09-24-openmrs-module-chartsearchai-273.md:16", "record", tx(273, 557),
      "false before the change (since ADR Decision 36), not made false by it; the ledger counts it")

# ---------------- #272 / PR 534
c(272, 534, "364db61e", AT + "reference/DrugSafetyValidatorTest.java",
  "the daily total it states is 1300 x the doses-per-day parsed", "C272-1300-x-formula",
  "harden c1 Phase 2 quality lens", "2026-09-24-openmrs-module-chartsearchai-272.md:12", "record", tx(272, 180))

# ---------------- #527 / PR 535 (rebased before push; SHAs are the pushed ones)
R527 = "2026-09-24-openmrs-module-chartsearchai-527.md"
c(527, 535, "3f0d7fd9", A + "reference/SafetyWarning.java", "client only inside a record the model cites",
  "C527-referent-only-via-cited-record", "harden c1 Phase 2 quality lens (finding 1, substantive)", R527 + ":20",
  "record", tx(527, 1601), "the claim starts on the previous line ('and the referent reached a')")
c(527, 535, "3f0d7fd9", ADR, "and stated it only in the", "C527-referent-only-via-cited-record",
  "harden c1 Phase 2 quality lens (finding 1, substantive)", R527 + ":20", "record", tx(527, 1601))
c(527, 535, "3f0d7fd9", OT + "web/rest/ChartSearchAiCurrentMedicationReferentTest.java",
  "and stated it only in the injected record, which", "C527-referent-only-via-cited-record",
  "harden c1 Phase 2 quality lens (finding 1, substantive)", R527 + ":20", "record", tx(527, 1601))
c(527, 535, "3f0d7fd9", RM, "two are exceptions", "C527-two-wire-exceptions",
  "harden c1 Phase 2 quality lens (polish 4, 'stale no wire counterpart enumeration')", R527 + " (not listed)",
  "transcript", tx(527, 1601))
c(527, 535, "3f0d7fd9", A + "api/ChartSearchService.java", "The prefix and the strength call carry no wire counterpart; TWO clauses",
  "C527-two-wire-exceptions", "harden c1 Phase 2 quality lens (polish 4)", R527 + " (not listed)", "transcript",
  tx(527, 1601))
c(527, 535, "3f0d7fd9", OT + "web/rest/ChartSearchAiCurrentMedicationReferentTest.java", "as a rule chip of either arm does",
  "C527-rule-chip-names-one-order", "harden c1 Phase 2 quality lens (polish 5, false universal)", R527 + " (not listed)",
  "transcript", tx(527, 1601))
c(527, 535, "c4ae1be4", A + "reference/SafetyWarning.java",
  "answer is attributable to names ({@code DrugSafetyValidator.isEchoOfAttributableRecord}) is in play",
  "C527-uncited-answer-drug-in-play", "harden c2 Phase 2 (2b) quality lens (finding 2, measured)", R527 + ":8",
  "record", tx(527, 1989), "the sentence spans the previous and next lines")
c(527, 535, "c4ae1be4", AT + "api/impl/LlmInferenceServiceCurrentMedicationReferentContextTest.java",
  "so what the first case pins is that an answer reciting", "C527-first-case-pins-recitation",
  "harden c2 Phase 2 (2b) quality lens (polish 4, measured)", R527 + " (not listed)", "transcript", tx(527, 1989))
c(527, 535, "c4ae1be4", OT + "reference/SafetyWarningFixtures.java",
  "{@code false} as the drug-in-play loop passes it. {@code chartRecords} is empty, being on no wire.",
  "C527-chartrecords-on-no-wire", "harden c2 Phase 2 (2b) integration lens (polish 2)", R527 + " (not listed)",
  "transcript", tx(527, 1981))
c(527, 535, "c4ae1be4", OT + "reference/SafetyWarningFixtures.java", "	 * {@code chartRecords} is empty, being on no wire.",
  "C527-chartrecords-on-no-wire", "harden c2 Phase 2 (2b) integration lens (polish 2)", R527 + " (not listed)",
  "transcript", tx(527, 1981))
c(527, 535, "c4ae1be4", OT + "reference/SafetyWarningFixtures.java",
  "No fold, no reconciled name and no bridge, which is what an unfolded chip", "C527-fixture-bridge-emptiness-rule",
  "harden c2 Phase 2 (2b) quality lens (finding 3, measured)", R527 + ":22", "record", tx(527, 1989))
c(527, 535, "87d0057e", A + "reference/SafetyWarning.java", "What can move it: a sibling ROW",
  "C527-what-can-move-it-routes", "harden c3 Phase 2 (2c) integration lens (substantive: a route missing)",
  R527 + ":9", "record", tx(527, 2389))
c(527, 535, "87d0057e", A + "reference/SafetyWarning.java", "and the order-driven arm then answers false for every row of it",
  "C527-order-driven-arm-answers-false", "harden c3 Phase 2 (2c) quality lens (polish 2)", R527 + ":9", "record",
  tx(527, 2406), "the record's 'the sibling-row consequence only fits the contraindication arm'")
c(527, 535, "87d0057e", ADR, "the prose is asked", "C527-asked-to-summarise",
  "harden c3 Phase 2 (2c) quality lens (finding 1, substantive)", R527 + ":10", "record", tx(527, 2406),
  "the claim continues on the next line ('to summarise the findings')")
c(527, 535, "87d0057e", RM, "(or the answer) named", "C527-or-the-answer-false",
  "harden c3 Phase 2 (2c) integration lens (polish 2)", R527 + " (not listed)", "transcript", tx(527, 2389),
  "the aboutACurrentMedication table row is one line; phrase_in_base is a coincidental match on base README:504 "
  "(the aboutAnEndedOrder row), not this claim")
c(527, 535, "87d0057e", OT + "web/rest/ChartSearchAiChartAlertsTest.java", "whose three-argument constructor",
  "C527-three-argument-constructor", "harden c3 Phase 2 (2c) efficiency lens (polish, 'outside my lens'), also reuse",
  R527 + " (not listed)", "transcript", tx(527, 2365))
c(527, 535, "2453c5d1", RM, "unless a record it cited or one the module injected already names it",
  "C527-echo-exception-wider", "harden c4 Phase 2 (2d) quality lens (substantive, measured; also reuse and integration)",
  R527 + ":11", "record", tx(527, 2583), "the aboutACurrentMedication table row is one line")
c(527, 535, "2453c5d1", A + "reference/SafetyWarning.java", "	 * uncited. {@code answerFromTheModule} hands it the EMPTY answer",
  "C527-modules-own-uncited", "harden c4 Phase 2 (2d) quality lens (polish 2)", R527 + " (not listed)", "transcript",
  tx(527, 2583), "the phrase is 'the attributable records include the module's own / uncited', split across this and the previous line")
c(527, 535, "2453c5d1", OT + "reference/SafetyWarningFixtures.java",
  "through a factory {@code SafetyWarning} keeps package-private, for", "C527-fixtures-lead-factory",
  "harden c4 Phase 2 (2d) integration lens (polish 2)", R527 + " (not listed)", "transcript", tx(527, 2568),
  "pre-existing: false at base already (endedOrderInteraction was built through a public constructor there)")

# ---------------- #432 / PR 536 (ledger counted only the PR body; these are the lens findings)
R432 = "2026-09-24-openmrs-module-chartsearchai-432.md"
c(432, 536, "c999a3e8", AT + "api/impl/ArchitectureGuardTest.java",
  "forwards the PARAMETER through {@code this(...)}, which is typed as a date and is not a write.",
  "C432-ladder-forwards-parameter", "harden c1 Phase 2 quality lens (finding 1)", R432 + " (not listed)", "transcript",
  tx(432, 326))
c(432, 536, "c999a3e8", AT + "api/impl/ArchitectureGuardTest.java", "The shared body of both constructor cases in this file",
  "C432-both-constructor-cases", "harden c1 Phase 2 quality lens (finding 2: 'an unchanged comment is now false')",
  R432 + " (not listed)", "transcript", tx(432, 326))
c(432, 536, "c999a3e8", ADR,
  "lets no class but `PatientChartSerializer` pass `RecordMapping` anything but a null stop date",
  "C432-no-class-but-serializer", "harden c1 Phase 2 integration and quality lenses", R432 + " (not listed)",
  "transcript", tx(432, 325))
c(432, 536, "c999a3e8", RM, "on any server not on UTC it is routinely a day off", "C432-any-server-not-on-utc",
  "harden c1 Phase 2 quality lens (finding 3)", R432 + " (not listed)", "transcript", tx(432, 326))
c(432, 536, "c999a3e8", ADR, "so on a server not on UTC it is a day off the local one", "C432-any-server-not-on-utc",
  "harden c1 Phase 2 quality lens (finding 3)", R432 + " (not listed)", "transcript", tx(432, 326))
c(432, 536, "c999a3e8", ADR, "`discontinueOrder` stops an order at `new Date()`", "C432-discontinue-at-new-date",
  "harden c1 Phase 2 quality lens (finding 3, 'one detail is slightly off')", R432 + " (not listed)", "transcript",
  tx(432, 326), "same line as the C432-any-server-not-on-utc ADR instance; the lens's own companion claim was refuted (record :9)")
c(432, 536, "570d4674", A + "serializer/PatientChartSerializer.java",
  "what keeps any other class from passing a date into THIS field is", "C432-keeps-any-other-class",
  "harden c2 Phase 2 (2b) quality lens (finding 1, overclaim)", R432 + " (not listed)", "transcript", tx(432, 476))
c(432, 536, "570d4674", AT + "api/impl/ArchitectureGuardTest.java", "they confine the widest rung to the injector",
  "C432-siblings-confine-widest-rung", "harden c2 Phase 2 (2b) quality lens (finding 2)", R432 + " (not listed)",
  "transcript", tx(432, 476))
c(432, 536, "d8156d62", AT + "api/impl/ArchitectureGuardTest.java",
  "only {@code PatientChartSerializer}'s may pass anything but the null", "C432-only-serializer-passes-non-null",
  "harden c3 Phase 2 (2c) quality lens (finding 1: javadoc lead)", R432 + " (not listed)", "transcript", tx(432, 552),
  "corrected by an exemption inserted on the next line; the line itself is unchanged in the fix", mode="unchanged")
c(432, 536, "d8156d62", AT + "api/impl/ArchitectureGuardTest.java",
  "every other call site of a date-carrying rung passes null", "C432-only-serializer-passes-non-null",
  "harden c3 Phase 2 (2c) quality lens (finding 1: assertion message)", R432 + " (not listed)", "transcript",
  tx(432, 552))
c(432, 536, "d8156d62", RM, "it is routinely a day off the local one", "C432-routinely-a-day-off",
  "harden c3 Phase 2 (2c) quality lens (finding 2)", R432 + " (not listed)", "transcript", tx(432, 552))
c(432, 536, "87866779", AT + "api/impl/ArchitectureGuardTest.java",
  "The one class that may pass a {@code RecordMapping} a stop date (issue #432).", "C432-the-one-class",
  "harden c4 Phase 2 (2d) quality lens (finding 1, overclaim)", R432 + " (not listed)", "transcript", tx(432, 608),
  "borderline: the lens called it an overclaim while reporting 'no false universals'")

# ---------------- #459 / PR 537
for (path, phrase, who) in (
        (A + "api/ChartSearchService.java", "Every consumer a {@code searchStreaming} overload is handed is invoked synchronously",
         "harden c1 Phase 2 quality lens (substantive; escalated)"),
        (A + "api/ChartSearchService.java", "It is invoked on the calling thread, before this call returns or throws, as every",
         "harden c1 Phase 2 quality lens (a home it named)"),
        (ADR, "Every implementation is bound to invoke each consumer synchronously",
         "harden c1 Phase 2 quality lens (a home it named)"),
        (O + "web/rest/ChartSearchAiRestController.java", "that every consumer is invoked on the calling",
         "harden c1 Phase 2 (a home the fix touched; named by the efficiency lens as a restatement)"),
        (O + "web/rest/ChartSearchAiRestController.java", "requires every consumer to be invoked synchronously",
         "harden c1 Phase 2 quality lens (a home it named)")):
    c(459, 537, "45256c0e", path, phrase, "C459-every-consumer-invoked", who,
      "2026-09-24-openmrs-module-chartsearchai-459.md:12", "record", tx(459, 330))

# ---------------- #443 / PR 538
R443 = "2026-09-24-openmrs-module-chartsearchai-443.md"
c(443, 538, "92c28b4b", AT + "api/impl/FindingPartnerLogDisclosureTest.java", "in {@link #newService}: the model",
  "C443-stubbed-in-newservice", "harden c1 Phase 2 quality lens (finding 1, substantive)", R443 + ":14", "record",
  tx(443, 477), "the author's own correction of issue item 4; also false on the strategy (next lines)")
c(443, 538, "92c28b4b", AT + "LogCapture.java", "The three disclosure negatives of ADR Decision 102",
  "C443-three-negatives", "harden c1 Phase 2 quality lens (finding 2)", R443 + ":16", "record", tx(443, 477))
c(443, 538, "92c28b4b", ADR, "all three negatives", "C443-three-negatives",
  "harden c1 Phase 2 quality lens (finding 2)", R443 + ":16", "record", tx(443, 477))
for (path, phrase) in (
        (A + "reference/DrugSafetyValidator.java", "from those two counts on the served response, so what the WARN holds alone"),
        (A + "reference/DrugSafetyValidator.java", "the extent returned below), from which how many went follows"),
        (A + "reference/DrugSafetyValidator.java", "which also states how many went"),
        (AT + "reference/PairChipCapContextTest.java", "so how many went is on the served response too"),
        ("omod/src/main/resources/config.xml", "how many went follows from interactionPairs")):
    c(443, 538, "92c28b4b", path, phrase, "C443-how-many-went-on-the-wire",
      "harden c1 Phase 2 quality lens (finding 3, an unverified lead the author acted on)", R443 + " (not listed)",
      "transcript", tx(443, 477),
      "borderline: the lens flagged it as 'lead, not verified' (the pre-answer pass's WARN has no response behind it)")
c(443, 538, "157dacaa", AT + "LogCapture.java", "captured level and assert it first", "C443-names-a-line-at-captured-level",
  "harden c2 Phase 2 (2b) quality lens (polish 1: unchanged sentence the change made false)", R443 + " (not listed)",
  "transcript", tx(443, 540))
c(443, 538, "157dacaa", ADR, "The negative at each of the three sites now captures from DEBUG up",
  "C443-adr-now-debug", "harden c2 Phase 2 (2b) quality lens (polish 2)", R443 + " (not listed)", "transcript",
  tx(443, 540))
c(443, 538, "157dacaa", ADR, "the two reference-package negatives now assert", "C443-adr-reference-package-negatives",
  "harden c2 Phase 2 (2b) quality lens (polish 2)", R443 + " (not listed)", "transcript", tx(443, 540))

# ---------------- #515 / PR 540: round 2 (r2-3) — made false by round 1's own fix
R515 = "2026-09-25-openmrs-module-chartsearchai-515.md"
c(515, 540, "8984b713", RM, "where each finding states a reason to withhold that drug", "C515-withhold-that-drug",
  "pr-harden r2 reviewer (r2-3)", R515 + ":22", "record", tx(515, 1112),
  "the r1 fix (590925b0) made it false by reporting a question-pair finding from either drug")
c(515, 540, "8984b713", A + "api/ChartSearchService.java",
  "the prompt carried this finding about X with a clause stating a reason to withhold it (or to change",
  "C515-withhold-that-drug", "pr-harden r2 reviewer (r2-3, names this line)", R515 + ":22", "record", tx(515, 1112),
  "corrected by an exception sentence inserted in the same paragraph; the line itself is unchanged in the fix",
  mode="unchanged")
c(515, 540, "8984b713", A + "api/impl/CautionLeadOverWithholdingCheck.java",
  "Reports an answer whose CAUTION LEAD says a drug can be given beside a finding about that drug stating a",
  "C515-withhold-that-drug", "pr-harden r2 fixer (a home it swept while fixing r2-3)", R515 + ":22", "record",
  tx(515, 1135))
c(515, 540, "8984b713", A + "api/impl/LlmInferenceService.java", "stating a reason to withhold it. Resolved here, after the repair",
  "C515-withhold-that-drug", "pr-harden r2 fixer (a home it swept while fixing r2-3)", R515 + ":22", "record",
  tx(515, 1135))


LEDGER = {  # ~/.claude/skill-lessons/REJECTED.md lines whose "Parked counts" entry adds the record
    433: "REJECTED.md:4019-4020", 438: "REJECTED.md:4020-4021", 454: "REJECTED.md:4021-4022",
    458: "REJECTED.md:4056-4057", 516: "REJECTED.md:4080-4082", 514: "REJECTED.md:4140",
    477: "REJECTED.md:4140", 512: "REJECTED.md:4166-4167", 407: "REJECTED.md:4220-4221",
    505: "REJECTED.md:4221-4222", 273: "REJECTED.md:4288-4290", 272: "REJECTED.md:4329-4330",
    432: "REJECTED.md:4330-4331", 443: "REJECTED.md:4359-4360", 459: "REJECTED.md:4360",
    527: "REJECTED.md:4360-4361", 515: "REJECTED.md:4427-4428",
}
BORDERLINE = {"C516-param-answer-models", "C477-classchipdetails-garbled", "C512-engines-repetition-penalty",
              "C407-third-and-fourth", "C505-brand-names-are-not", "C443-how-many-went-on-the-wire",
              "C432-the-one-class"}
# claims that were already false at the base (the change did not make them false; it rewrote or neighboured them)
FALSE_BEFORE = {"C273-json-is-the-default", "C273-rowsof-stale-caveat", "C273-gate-excludes-every-such-entry",
                "C516-residue-one-direction", "C527-fixtures-lead-factory"}


def main():
    out = []
    bad = []
    for e in C:
        try:
            o = locate(e["pr"], e["fix"], e["path"], e["phrase"], e["mode"])
        except LocateError as err:
            bad.append((e, str(err)))
            continue
        rec = {
            "ticket": e["ticket"],
            "pr": o["pr"],
            "fix": o["fix"],
            "prefix": o["prefix"],
            "base": o["base"],
            "base_from": o["base_from"],
            "file": o["file"],
            "line": o["line"],
            "false_text": o["false_text"],
            "phrase": o["phrase"],
            "introduced_by": o["introduced_by"],
            "found_by": e["found_by"],
            "record": e["record"],
            "claim": e["claim"],
            "source": e["source"],
            "transcript_ref": e["transcript_ref"],
            "fix_removes_line": o["fix_removes_line"],
            "phrase_in_base": o["_check"]["phrase_in_base"],
            "borderline": e["borderline"] if e["borderline"] is not None else e["claim"] in BORDERLINE,
            "false_before_change": e["claim"] in FALSE_BEFORE,
            "ledger": LEDGER[e["ticket"]],
            "notes": e["notes"],
        }
        out.append(rec)
    for e, err in bad:
        sys.stderr.write("UNLOCATED %s %s %s %r: %s\n" % (e["ticket"], e["fix"], e["path"], e["phrase"], err))
    with open(OUT, "w") as f:
        for r in out:
            f.write(json.dumps(r, ensure_ascii=False) + "\n")
    print("written", len(out), "unlocated", len(bad))
    return 1 if bad else 0


if __name__ == "__main__":
    sys.exit(main())
