# resolve-ticket + harden + pr-harden · openmrs-module-chartsearchai · #234 / PR 326 · 2026-08-28
outcome: converged
rounds: 2 (pr-harden)   cycles: 6 (harden)   verifier: ran (works at runtime)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-openmrs-module-chartsearchai/dafd75ab-3038-4325-8597-c3921a972fa5.jsonl

## Refuted by measurement
- The TICKET's own proposed remedy — "thread the route so the leg can prefer a row whose
  administration route matches" -> all 129 multi-row substances in the shipped KB publish an
  IDENTICAL ATC code list across their rows (0 differing), so choosing a row cannot change any
  classification. The narrowing had to be of the codes. · cost: caught at plan time, 0 rounds
- The ticket's unmeasured sizing question ("how many systemic chips name a topical order") -> 0 on
  the 3.7.1 demo: all 46 active drug orders record `Oral administration` (32) or nothing (14), and
  the 17-member route set names no cutaneous route. · cost: caught at plan time
- Plan revision 1's site->ATC-group table, hand-written -> dropped `S03` "Ophthalmological and
  otological preparations", which the shipped KB uses (neomycin's ear codes are {S02AA07, S03AA01}).
  Refuted by the refutation gate before any code. · cost: 1 gate pass
- Harden cycle 2: `topical` read as the skin -> ATC uses the word for the anorectum too (`C05A`
  "Antihemorrhoidals for topical use"); measured, an eye ointment recorded `Topical` lost its true
  S01BA chip. Removed as a term. · cost: 1 cycle
- Harden cycle 2: the union reading of a form word (skin u vagina u anorectal) -> keeps
  hydrocortisone's C05AA01 and turns an H02AB chip into C05AA, a haemorrhoid preparation. · cost: 0
- PR round 2's own claim that the "not systemic" reading of `topical` was unrefuted -> measured by
  the fixer: it produces `A01AC` (local ORAL treatment) on the ticket's own input, so the ADR's
  existing rejection does reach it. · cost: 0 (corrected inside the round)

## Raised by a fresh agent, missed by the author
- [gate p1] The site table was unmeasured and incomplete; CLAUDE.md requires a re-derived criterion
  and a measured KB impact for exactly these lists · blocking · cost: 1 gate pass
- [h-c1] `namedByCodesOnly`'s administration terms are unread today, and the reason must be stated
  rather than implied · non-blocking
- [h-c2] The narrowing was resolved from the FIRST unmapped order in list order; with a topical cream
  and an oral tablet of one substance, `OrderService`'s ordering decided whether a real systemic chip
  appeared · blocking-equivalent · cost: 1 cycle
- [h-c2] `ROUTES_OF_ENTRY` needed to be a refusal rather than an absence: "transdermal skin patch"
  matched the term `skin` and silenced a real chip · cost: 1 cycle
- [h-c2] `partner.codes` has three consumers, not one — the curated-group fall-through and the #88
  fold narrow too · non-blocking
- [h-c2] Inserting the SITE_* constants orphaned `LOCALLY_APPLIED_ATC_GROUPS`'s 43-line javadoc; and
  again later for `codesForThisSubstancesPresentations`. Twice, in the same slice, both silent.
- [h-c2] A compositional gap: the per-order guard asked "do the terms name a site" while the
  empty-set fallback was evaluated over the UNION, so a nasal order's fallback was consumed by a
  cutaneous sibling · cost: 1 cycle
- [h-c3] Two cases passed for a different reason than their names claimed (one caught by
  ROUTES_OF_ENTRY a step earlier; one not asserting its own premise)
- [pr-r1] **The builder read `Concept.getName()`, which returns the locale-PREFERRED name, while the
  site terms were derived from FULLY SPECIFIED names.** On the 3.7.1 dictionary that silently
  un-fixed the bilateral eye route, the bilateral ear route and the only vaginal route. Invisible to
  the entire suite because every test built concepts with a single ConceptName, where the two
  spellings coincide. · BLOCKING · cost: 1 round
- [pr-r2] Six non-blocking, including two javadocs added by the same PR contradicting each other
  about whether a guard is behavioural, and a justification stated unconditionally that is
  conditional.

## Where a skill blocked or contradicted this run
- harden:Phase2 — `git checkout -- <path>` to undo my own mutation probe also reverted uncommitted
  intended work in that file (the accessors added minutes earlier). The skill warns about this; the
  warning is about the FILE's state, not about who typed the command, and it was still easy to trip.
  Cost: one reapply, caught by a compile error only because a test referenced the reverted method.
- A python slice-replacement using an `end` anchor that had moved ABOVE the `start` anchor silently
  duplicated a block of test methods. Caught by the compiler. The skill's "count what should still be
  there" rule is aimed at deletions; this was a duplication.

## Declined
- A "does this substance publish ANY locally-applied code" short-circuit before the term scan —
  measured ~0.24 ms per /search in total, against an LLM call that dominates the request. If we ship
  without it, nothing breaks: the method does a 27-entry table scan for a substance it cannot narrow
  anyway.

## Assumptions review overturned
- "A term must name the SITE, not the form" (plan A1/A5) -> survived, but only after `topical` was
  measured multi-site and removed under the same rule that had excluded `cream`. The rule was right
  and its own first application was wrong.
- "the narrowing is decided by the order's recorded terms" -> narrowed twice: to the SUBSTANCE's
  terms across every unmapped order naming it (cycle 2), then to what the data can EXPRESS rather
  than what a term NAMES (cycle 2 review).
- "the builder reads the name of its route concept" -> false; it must read every name the concept
  publishes (PR round 1).
