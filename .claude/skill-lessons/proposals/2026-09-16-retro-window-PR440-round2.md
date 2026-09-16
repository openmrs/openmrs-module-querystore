# Retro proposals · 2026-09-16 (second window of the day) · from `2026-09-16-chartsearchai-PR440.md`

New evidence this window: ONE record — `#439/PR440`, the pr-harden run that was mirrored at 9a08df8
but explicitly not read by the 22:45 block. Corroboration for P1 is drawn from
`2026-09-16-openmrs-module-chartsearchai-435.md`, which the previous window read but did not examine
for this class.

## P1 · `harden`, the *supposed to stay GREEN* bullet — a control measures the HARNESS, not the property

**Verdict: APPLIED AFTER REVISION** (`harden` 0.32.0, `pr-harden` 0.24.0). The bar-(a) leg below,
the RE-IMPLEMENTATION clause and *"One probe is evidence for ONE site"* were all dropped, *"Nothing
states it for a control"* is FALSE, and the shipped text also went into `pr-harden`'s fixer brief.
Read the shipped reasoning in `REJECTED.md`'s 2026-09-16 second-window block, not this draft.

**Bar: (b), and also (a).** (b): three blocking rounds of `#439/PR440`, one each, all of them a
positive control that existed and was green while the thing it forbids reached the log. (a): the same
class in `#435` at a pass each.

The rule already shipped — *"Build the case it exists for and watch it FAIL"* (`harden`:248-255) — was
obeyed on this run. A control was built at every one of the three sites, and `efccb372`'s own message
says so: *"Its control is what makes that a defect rather than a doubt — restoring the pre-fix label
does redden the case"*. The guard was still blind, because what the control proves is bounded by what
the harness can observe:

- **LEVEL/SCOPE** · `[r2]` the capture raised one class's logger, and only to WARN; the same details
  logged at `info` passed the guard. Fix `efccb372` widened it to the package at `Level.DEBUG` —
  verified in the diff. · 1 round
- **CHANNEL** · `[r4]` the shared helper's `describeAll()` rendered a throwable's TYPE alone, so the
  patient's medication names attached to a diagnostic exception on the cap WARN passed every guard.
  Verified: `LogCapture.describeAll` now appends `describeThrown`, which walks causes and suppressed
  and renders each type AND message. · 1 round
- **ORDER** · `[r3]` `LogCapture.close()` restored a level but left the `LoggerConfig` its constructor
  had installed, so a sibling test's class-named capture outranked a later package capture and the
  guard held under one surefire run order only. Verified: `close()` now calls
  `configuration.removeLogger(loggerName)` where the capture caused the config. · 1 round
- **SITE** · `[r4]` a probe at the cap WARN was read as evidence for all three negatives; the other
  two stub the validator and the injector out, *"so their staying green measured nothing rather than
  measuring a hole"* (`a1d19569`). Each site was probed separately instead. · 0 rounds, caught in-round
- **RE-IMPLEMENTATION** · `#435`:`[c1]` a second LF-only frame decoder in the test package — the
  keep-alive test's private well-formedness assertion — *"as blind to the finding as the code it
  checked"*. · 1 pass

`pr-harden`:287-289 already states the same principle for the other side of the loop — *"a mutation
result measures the arms it moved, not the mechanism"*. Nothing states it for a control.

**Proposed**, appended to `harden`'s *supposed to stay GREEN* bullet after *"watch it FAIL."*:

> **And a control measures the HARNESS it ran in, not the property — the harness is part of the
> guard, and a negative assertion is only as wide as what its harness can SEE.** `pr-harden`'s *a
> mutation result measures the arms it moved, not the mechanism* is this rule on the other side. Ask
> what the harness captures (which logger, at which level), how it RENDERS what it captured, and
> whether a sibling test's residue changes either: three blocking rounds on #439, one each, where a
> capture raised one class's logger to WARN and the same details at `info` passed; where the shared
> helper rendered a throwable's TYPE alone, so the patient's medication names on a diagnostic
> exception passed every guard; and where a leftover `LoggerConfig` from a sibling capture made the
> guard hold under one surefire run order and not another. One probe is evidence for ONE site — on
> #439 the other sites' harnesses stub the subject out, so their staying green measured nothing — and
> a harness that RE-IMPLEMENTS the subject inherits its bug, which on #435 was a second LF-only frame
> decoder in the test package, as blind to the finding as the code it checked.

**Prune.** `pr-harden`:292's *"`harden`'s Termination carries the measurements (#360, #355)"* — the
enumeration is made wrong by this edit and is the kind that rots; cut the parenthetical, keep the
pointer. Net growth ~+9 lines, justified: the parent rule was in force, was obeyed, and cost three
blocking rounds in one run anyway, which is the definition of a rule that does not reach its cases.

## KILLED BY MEASUREMENT before proposing · the `-DfailIfNoTests=false` entry

The record's Environment section states: *"`-Dtest='A+B'` with `-DfailIfNoTests=false` runs NOTHING
and exits 0."* The mirror commit `9a08df8` flagged it for this window as *"a sharper form of the
-Dtest=A+B entry"*. **It does not reproduce.** Measured today, maven 3.9.10 / surefire 3.5.5, in this
repo, with a real two-class selector:

| arm | command | result |
| --- | --- | --- |
| A | `-pl api surefire:test -Dtest='X+Y' -DfailIfNoTests=false` | **EXIT=1**, BUILD FAILURE, *No tests matching pattern* |
| B | `-pl api surefire:test -Dtest='X+Y'` (known-bad) | EXIT=1, BUILD FAILURE |
| C | `-pl api surefire:test -Dtest='X,Y'` (known-good) | EXIT=0, `Tests run: 20` |
| D | `-pl api test -Dtest='X+Y' -DfailIfNoTests=false` | **EXIT=1**, BUILD FAILURE |
| E | `-pl api surefire:test -Dtest='X+Y' -Dsurefire.failIfNoSpecifiedTests=false` | EXIT=0, BUILD SUCCESS, no `Tests run:` line |
| F | `-pl omod test -Dtest='X+Y' -DfailIfNoTests=false` | **EXIT=1**, BUILD FAILURE |

Calibrated in both directions (B known-bad, C known-good). The behaviour the record describes is real
but belongs to a DIFFERENT flag: `-Dsurefire.failIfNoSpecifiedTests=false`, arm E, which is the flag
surefire's own error text tells the run to set. So `REJECTED`:2897-2899's ground for not shipping this
— *"surefire's own error text names the right flag, so the tool tells the run and the skill need not"*
— **stands, re-measured**, and no clause is proposed. The record is corrected in place.

## Observed, not yet actionable

**O1 · A completeness sweep whose criterion is narrower than the defect class it claims: 1 record.**
`#439`:`[r1]`, blocking, 1 round, and the run's only genuinely missed disclosure. The sweep filtered
log-call ARGUMENTS by identifier name; the missed site passed `withheld`/`pairs`. `[r3]` then found a
fourth class the same criterion reaches. The remedy is already written for the neighbouring problem —
`harden`:414's *"enumerate the claim's SUBJECT instead … a population you can finish"* — and the
transposition here is the log calls in the two packages, read. Not proposed: bar (b) wants two rounds
and this is one; `#435`'s test-package decoder is the same shape but its record does not attribute it
to a sweep, so counting it would be inventing the citation. **REOPEN ON:** a second record where a
defect-site sweep's criterion is narrower than the class it reports on.

**O2 · A measurement recorded in javadoc, falsified by a later commit on the SAME branch: 1 record.**
`#439`, *"Widening the capture to the module root was tried and reverted"* — round 2's measurement,
whose cause round 3's own `close()` fix removed, leaving a narrow scope resting on a measurement the
branch had already falsified (`a1d19569`'s message states it). Cost: part of 1 round. The ledger's
existing staleness entries (**:2714-2723**, **:2895**) are about POINTERS going stale; this is the
measured claim itself. **REOPEN ON:** a second record of a within-run measurement invalidated by a
later commit of the same run.

**O3 · A claim of coverage wider than the thing covers: this run adds four.** `[r2]` a residue
paragraph covering a site its reasoning does not reach; `[r5]` two guard-scope comments citing an
argument wider than their scope; and the round-2 assertion *"over the whole line and every level"*.
`harden`:406's universals rule is the shipped remedy and was not followed. Folds into the ledger's
prose-correction class rather than earning a rule of its own.

**O4 · A corrected claim's second home two rows above the corrected one** (`[r2]`, README,
non-blocking). `harden`:414 is the rule; the interesting part is that proximity did not help. Count: 1.

**O5 · Cross-session interference — NOT incremented.** This record is the other side of the event
already counted as the third sighting at **:3119-3124** (`#435`'s after-the-fact capture found the
shared clone on `finding-partner-coverage-log-phi`, which is THIS run's branch). One event, two
records. `#439`'s own environment note is a different class: a subagent dying on a 429 mid-edit, whose
residue the worktree-hash snapshot caught and whose surviving half was committed protectively — both
mechanisms already shipped (`pr-harden`:256, :588) and both worked.

## Where a skill was exercised and held (no proposal)

- The Stop gate fired once, correctly: the orchestrator cleared the await and ended the turn with
  `phase=fixing`. `pr-harden`:890-891 and :1163-1164 are the rules; the gate caught the violation.
- `pr-harden`:588's protective mid-round commit and :256's worktree-hash snapshot both did their job
  on the 429 death. No change owed.

## P2 · `skill-lint.py` — a parked count that does not increase across ledger blocks

**Verdict: KILLED.** Every figure below reproduces and the calibration is still false: it measured
ONE of the ledger's two count formats. Over both, the check fires 13 times, 12 of them legitimate,
and it misses the wrong-`previous`-pointer defect it was written for. `REJECTED.md`, same block.

**Bar: not the Step 3 bar, which is over run records.** This is a Step 2 item: a class a script can
decide, of the same kind the linter already carries (*"a count stated over a list of a different
length"*). Its warrant is a defect **this retro found shipped in the ledger**, by hand, and the
ledger's own standing note that a remedy for a twice-violated accounting rule *"is probably
mechanical, not textual"* (**:30**, borrowed here knowingly as a general note, not as this class's).

**The defect.** `REJECTED`:3119 — written by the 22:45 block today — states *"Cross-session
interference on a live run's shared state: **3 records** — previous 2 (**:811-824**…)"*. But
**:958** had already taken that class to 3, adding `#297`:43. The block re-derived "previous" from
the base entry at :811 and missed the intervening increment, so the class is at **4**, not 3, and
`#439`'s own after-the-fact sighting is the fourth. This is **:1488**'s named defect (*"a parked
count taken from a superseded block"*) — and the same block caught itself committing it for the
prose-correction count at **:3084-3086** while missing it here.

**The check.** Group `- **<class>: N records**` entries by class, and flag where a later block's N is
not greater than the previous block's, unless the line says `unchanged`.

**Calibrated in both directions, on the live ledger** (3134 lines, 39 classes carrying a count, 7 of
them repeated):
- **known-good, not flagged:** `Rate-limit / session-limit agent death` 17→19→20→21→22,
  `Prose-correction cycles` 9→12→…, `--count-edits answered by hand` 3→4, `pr-harden is silent on
  where the FIXER works` 2→4, the round cap 3→4.
- **known-bad, flagged:** the cross-session class above — the one defect, found by hand first.
- **suppressed correctly:** **:2525** *"2 records — unchanged; this pass applied two"*, a standing
  count restated without a new sighting. Without the `unchanged` clause this is a false positive.

**A sharper variant was tried and rejected**: parse the block's own *"previous N"* and compare it to
the last recorded count. It reads the block's own claim rather than inferring from monotonicity, but
measured on the same ledger it returns 3 flags of which 2 are artefacts — a context window catching
*"correcting the previous one"* as `previous 1`, and a coverage hole where an indented entry is not
matched at all, which makes it compare across a gap. Recorded so the next attempt does not redo it.

**The honest cost.** A future block that legitimately restates a count without writing `unchanged`
is flagged, and the linter exits 1 on anything. Exactly one of the 7 repeats uses that word today, so
the convention is thin. The flag costs one word to clear, and the alternative is what :3119 shipped.

**Prune offered:** none in the skills; the change is +1 check in the script and one clause in Step 2
naming the ledger as a thing it now reads.

## Corrections to apply to the store (not proposals)

**C1 APPLIED** (with its own error fixed: `#439` is the other side of the fourth sighting, not a
fifth). **C2 KILLED as worded** — the record's FLAG NAME is right and its *"exits 0"* is wrong; the
run piped maven into `grep … | head -20`. Corrected the outcome instead. `REJECTED.md`, same block.

**C1 · `REJECTED`:3119 — the count is 4, not 3, and "previous 2" is wrong.** Verified: :811 rules two
distinct records (`#266`:55-64 destruction, `2026-08-27-retro-authored-hook-regression`:87-95
misattribution, *"Ruled TWO events, not one described twice"*), :958 adds `#297`:43 as the third.
Corrected in place with the re-derivation named, per the precedent at :2350-2352 (a record corrected
by a later window that measured it).

**C2 · `2026-09-16-chartsearchai-PR440.md`, the Environment bullet naming `-DfailIfNoTests=false`.**
Refuted by the six-arm measurement above. Corrected in place to name
`-Dsurefire.failIfNoSpecifiedTests=false`, with the measurement and its date, so the next window does
not re-derive it — and so the mirror commit's hand-off (*"a sharper form of the -Dtest=A+B entry"*)
is answered where it was made.
