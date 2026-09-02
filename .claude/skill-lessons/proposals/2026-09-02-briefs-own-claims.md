# Proposal · a verifier brief's own factual claims go unchecked

Drafted 2026-09-02, against the parked entry `REJECTED.md`:1403-1406 — "A brief's own factual claims
going unchecked: **1 record** (#338). Sibling of resolve-ticket 0.14.0's 'a gate objection's own
numbers are claims too'. **REOPEN ON:** a second record." The second record has arrived.

## The claim

The orchestrator writes the verifier's brief, and that brief carries factual assertions about the
system — what a code path emits, what the shipped data contains. Nothing tells the verifier those are
the orchestrator's own reading rather than established fact, and nothing tells it what to do when one
is false. Twice the briefed witness did not exist, so the step produced no observation at all until
the verifier noticed and repaired the brief itself.

## Corroboration — limb (a), two run records

- **#338** (`2026-08-31-openmrs-module-chartsearchai-338.md:26`, verbatim): "pr-harden:Step 6 — my own
  verifier brief asserted the check emits a DEBUG line when the answer states no ATC token. It does
  not; that gate is a bare `return`. The verifier caught the brief rather than the code."
- **#355** (`2026-09-02-chartsearchai-355.md:17`, verbatim): "'warfarin is in ibuprofen's compact tail
  on the shipped KB' (verifier's brief, from a comment measured over the bundled 16-drug excerpt) ->
  it is not; the verifier re-drove the same contract with ketoconazole plus a positive control.
  cost: 0 rounds" — and :28, the same event from the other side: "[verifier] its own briefed check
  could not have witnessed what it was for, and it constructed one that could, plus a positive
  control · non-blocking · cost: 0".

**Stated honestly: both cost 0 rounds, and both were caught.** The limb is (a), two records; it is NOT
limb (b). What the two paid was verification work spent repairing the brief — in #355 the verifier had
to construct a different witness and a positive control before it could verify anything. The
counterfactual (an uncaught false premise producing a false verification) is **not** measured and this
proposal does not assert it.

**Adjacent and deliberately NOT counted:** #357's "the excerpt cannot yield a visible order-driven
filtered segment -> a confirming agent CONSTRUCTED the arrangement · cost: 2 cycles". Same shape — an
orchestrator claim an agent refuted by construction — but the record does not place the claim in a
brief, and counting it would be the over-count this ledger catches repeatedly.

## Why it is not a restatement

`pr-harden`:492-497 already says "**A verifier's observations can falsify a claim the PR makes in
prose, and that is a finding rather than a footnote.**" That is the opposite direction: a false claim
in the PR's prose is something the verifier's observations *catch downstream*, and it is a finding. A
false claim in the BRIEF is upstream of any observation — it decides what the verifier goes to look
at, so the failure is that there is no observation to report.

Nothing in `pr-harden`, `harden` or `resolve-ticket` addresses a brief's own premises (checked:
`premise`, `the brief's own`, `brief asserted`, `unverified claim` return only harden:49-52 on a
FIXTURE's premise and resolve-ticket:280 on a PLAN's dataset premise).

## Scope — verifier brief only, deliberately

Both records are `pr-harden` **verifier** briefs. The ledger's own precedent forbids widening past
where the evidence sits — :1289-1290, "#338's death is a harden cycle-4 agent, **so it is not cited in
a document that counts rounds**", which is the objection that deferred this pass's P2. So: the VERIFY
step, not a general rule about briefs, and not `harden`'s Phase 2 briefs.

## Proposed edit

Fold into the existing `pr-harden`:492 bullet rather than adding a neighbour, so the two directions
are stated together:

> **A verifier's observations can falsify a claim the PR makes in prose — or one the BRIEF makes, and
> the second is the worse failure.** [existing PR-prose sentences and the #339 chip example stay]
> **A false claim in the brief is upstream of every observation**: it decides what the verifier goes
> to look at, so instead of a finding it yields nothing until the verifier repairs the brief itself.
> Twice the briefed witness could not exist — on #338 a DEBUG line at a gate that is a bare `return`,
> on #355 a partner named from a comment measured over the bundled excerpt rather than the KB the run
> ships against, which the verifier replaced with one that is there plus a positive control. So say
> in the brief which factual claims are your own reading, have it check those before driving
> anything, and treat "the briefed witness cannot exist" as a result to report. Distrust a figure
> lifted from a comment first: the comment records what was true of the data when it was written.

## Prune answer

**Subsumes nothing; retires nothing. Net ≈ +7 lines, folded rather than added.** No change to the
verifier's JSON schema is needed and none is proposed — `verdict: "could not determine"` and the
`observed` field already carry the outcome, which is the reason this is prose and not a new field.
Growth is justified per-addition rather than in aggregate: this is the one class in the window that
the existing verifier rules provably do not reach, being about the brief rather than the code.

## Known weaknesses, for the gate to weigh

1. Both sightings cost 0 rounds. A rule whose measured cost is zero twice may not be worth 7 lines.
2. Both were caught by the agent unprompted, which is evidence the current briefs already produce the
   behaviour the rule would mandate — the *instruction-is-not-the-lever* shape the ledger names.
3. "Say which claims are your own reading" is an instruction to the orchestrator about prose it writes
   ad hoc; nothing checks it, so it may be unenforceable in the way :307 describes.
