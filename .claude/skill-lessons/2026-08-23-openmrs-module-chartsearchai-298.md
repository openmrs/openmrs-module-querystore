# resolve-ticket 0.6.0 (pr-harden 0.4.0) · openmrs-module-chartsearchai · #298 / PR 301 · 2026-08-23
outcome: converged
rounds: 3   cycles: 5 (harden)   verifier: ran (works at runtime, merging head 02553c7d)

## Refuted by measurement
- "A constructor gate makes it a class invariant for every caller that exists and every caller added
  later" -> false: `namingOrder` is non-final and `nameByOrder` already assigns it, so a constructor
  gate binds constructor callers only · cost: gate pass 2 (pre-code)
- "The write path alone is unobservable, so `foldedPartnerLabel`'s branch order must change to give
  the change coverage" -> false: the repo already pins a behaviour-neutral rule structurally
  (`ChartSearchAiReferenceGroundingWithholdingTest`), which CLAUDE.md itself cites · cost: 2 rounds
- "The sweep sees the regression this arrangement exists for" -> false: substituting the code leaves
  exactly ONE name, so the one-name assertion still passed; needed a second assertion · cost: 1 round
- "The guard checks the gate reads the flag" -> false: it checked the RHS *contained* the flag's name,
  so `order != null || namesADrug ? order : null` passed it with all 1350 tests green · cost: 1 round
- "each constructor derives all three facts from one source" -> false of the order rung (it takes the
  order AND the code) · cost: 1 round
- Five universal/exhaustive claims about a regex, each written to fix the previous cycle's false one,
  each false ("any looser pattern", "only re-admits M01AE0", "only the 5- and 7-character shapes",
  "exactly the two levels", one mis-levelled) · cost: 3 cycles
- Post-run: "two rounds sharing a sha means the fixer implemented nothing, or the commit landed after
  the fetch" -> not established; the surviving evidence fits three mechanisms · cost: 0 (caught before
  it was written into a skill)

## Raised by a fresh agent, missed by the author
- [r1] the branch reversal is not required by the ticket and RETIRES a working guard; a structural pin
  gives coverage while keeping both · non-blocking · cost: 1 round (and produced the better design)
- [r2] the guard's gate assertion is defeated by a semantically-equivalent rewrite · blocking · cost: 1
- [r3] this change removes the only behavioural coverage of the FIRST guard, unrecorded · non-blocking
- [r3] Decision 39's "18 folded chips, 6 on this branch" falsified by this PR's own new arrangement
- [harden p2] four concurrent agents corrupted two of four reports on one checkout

## Where a skill blocked or contradicted this run
- harden:Phase 2 — mandates four PARALLEL agents while every brief tells each to mutate-and-restore
  the shared worktree. Cost: two of four agent reports contaminated, one build collapsed into 842
  NoClassDefFound errors, one agent spent a detour on another's uncommitted mutation.
- harden:State — no `awaiting` field, so a cycle blocked on its own subagent could not yield; the Stop
  gate fired on every attempt. Cost: two ten-minute in-turn wait loops. pr-harden had solved this.
- pr-harden:FINISH — never deletes its own `pr-<n>-r<round>` refs; 11 left behind by 4 runs.
- pr-harden:step 1 — records `reviewed_shas` and never compares them; two refs per PR shared a sha on
  two earlier runs, meaning a round may review bytes already reviewed.
- pr-harden:verifier — "never take a running server, report unrepairable" contradicts the project
  memory granting standing permission to restart local standalones. Followed the memory.

## Design observations — rule sound but suboptimal
- harden's termination condition ("one full Phase 1 + Phase 2 cycle produces zero edits") is the best
  ENFORCEABLE condition, not the best condition, and four gaps showed on this run.
  (1) It measures the PROCESS, not the artifact: this run converged at cycle 5 with a measured zero,
  and pr-harden round 1 then found a better design and round 2 found a BLOCKING defect — so at the
  moment harden declared convergence, two real findings were outstanding. pr-harden itself says why
  ("its passes run in the context that wrote the code… the *weaker* review"), so the word "complete"
  over-claims; "this process has stopped producing" is what a zero-edit cycle licenses.
  (2) It cannot distinguish a converging slice from a self-inflicted loop: cycles 2, 3 and 4 each
  found real defects, and all three were in prose written by the immediately preceding cycle. The
  skill has an anti-pattern naming that signature but the termination rule cannot see it · cost: 2
  cycles (3 and 4, each triggered by a one-clause javadoc fix, each a full Phase 1 + Phase 2 with an
  agent).
  (3) Perverse incentive at the margin, patched by a counter-rule: a late small fix mandates a whole
  extra cycle, and the skill has to forbid withholding it. A rule needing a counter-rule to stay
  honest is a smell.
  (4) harden has NO cycle cap (0 matches for one), while pr-harden caps rounds at 4 and treats the cap
  as a labelled terminus. The asymmetry looks unintentional, and it leaves the override — framed as a
  deviation — as harden's only non-convergence exit.
  Candidate remedies: classify the edits, so a cycle whose only changes are in the previous cycle's
  prose triggers a tactic change rather than a fresh full cycle; rename what the gate asserts from
  "complete" to "stopped producing"; add a cycle cap for symmetry. Explicitly REJECTED candidate:
  folding a fresh-context review into harden — that gives two termination contracts arguing every
  turn, which the skill rejects with a stated reason.

## Evidence about the run-record format itself
- This observation had no section to go in. The template's four middle sections capture FAILURES (a
  refuted claim, a missed finding, a skill blocking the run, a decline); a rule that worked as written
  but is suboptimal, with measured cost, has no home. Added as a section here; whether the template in
  resolve-ticket/pr-harden should grow one is skill-retro's call, not this record's — a record is data,
  the template is governance. First real exercise of a format that had never been used when written.

## Declined
- Derive `namesADrug` from `(namingOrder != null || labelEntry != null)` — suite-green, but it changes
  what the flag MEANS and its benefit is illusory: a direct field write still yields a derived `true`.
- An immutable `OrderPartner` with a copy constructor, so javac enforces the write path — wider than
  the ticket; it turns an accumulating object immutable and the accumulation is load-bearing.
- Rename the `<ol>` outcome ordinals to match code order — renaming five cited identifiers is itself
  the drift it would prevent.

## Assumptions review overturned
- A2 "reordering `foldedPartnerLabel`'s branches is in scope, because the ticket's defect is that the
  safety lives in that statement order" -> overturned in round 1: the reordering adds exposure to the
  very defect the ticket removes, and its coverage justification was false. Branch order restored.
