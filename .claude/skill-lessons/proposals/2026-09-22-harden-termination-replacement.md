# proposal · replace `/harden`'s zero-edit termination · drafted 2026-09-22

status: **EXECUTED 2026-09-22, repaired 2026-09-23.** `harden` 0.36.0, `pr-harden` 0.26.3,
`resolve-ticket` 0.17.0,
`hooks/harden-cycle-gate.sh`, `pipeline/gate-state`, and both test nets. The order's six sites, plus
two it did not enumerate (Phase 2's own stopping gate, which told it to run to convergence, and the
`Re-entry` section, which the escalation clause replaced — deleted rather than reworded), plus three
more sites a fresh reviewer found after the first push and that no list had: two live READERS of the
harden entry, `pipeline/pool-watch` and `pipeline/pool-run`'s leftover report, and — not a reader of
it, which the first draft of this sentence got wrong — both copies of `pr-harden-gate.sh`, whose
header drew a contrast with `/harden`'s edit count that no longer has another side. A later pass
added `hooks/README.md` and `hooks/git-restore-backup.sh`, both of which describe the retired
mechanism as current. The order's warning that a partial application is worse than the status quo was
right, and its own site list was not complete. The two
things it owed are discharged at the end of this file: the `#298` walk-forward, and the test net with
its known-bad control. Like `pr-harden` 0.26.0 before it, this was applied at the maintainer's
instruction and has NOT been through `skill-retro`'s refutation pass; a later retro should treat it
as an unrefuted proposal that happens to be live.

requested by: the maintainer, 2026-09-22, on the grounds that `/harden` has taken hours on every
run for a month and the zero-edit condition is the reason. That is a month of observation, which is
corroboration `skill-retro` would otherwise have had to ask for.

## What to change it TO

> **`/harden` is complete when Phase 1 converges.** Phase 1 keeps its existing rule — a pass that
> found a substantive (non-cosmetic) issue cannot be the last pass. Phase 2 then runs ONCE, and its
> edits do not re-open Phase 1 or start a new cycle. If Phase 2 turns up something SUBSTANTIVE
> rather than polish, it escalates on the spot and Phase 1 resumes.

The escalation clause is not optional: a polish pass does occasionally surface a real defect, and
without it that finding is swallowed. `harden` already has this idiom — *"A documentation pass that
turns up anything behavioural escalates on the spot, to a full cycle"* — so reuse the wording rather
than inventing a second one.

## Why this shape, and not the two obvious ones

Both obvious answers are already refuted in `REJECTED.md` — the cap twice, the provenance
signal at least twice — each on a walk-forward:

- **A cycle cap** (P-A, :1433). Killed twice. `#298` ran 5 cycles and `outcome: converged`, so a cap
  of 4 ends a converged run as did-not-converge and loses cycle 5's measured zero. Runs past 4
  cycles: `#302` (10), `#330` (15), `#266` (7), `#308` (6), `#293` (6), `#234` (6), FM2-700 (6),
  `#297` (5), `#250`, `#315`.
- **A prose-provenance spin signal** (P-B/P3, :32). Killed on the same walk-forward, and it also
  overrode the "prose that IS behaviour is not documentation" carve-out.

The standing reopen condition on both is **"a signal that separates this from spinning, not a cap."**
This is that signal, and the reason it is worth trying is structural: **Phase 1's gate is already
severity-aware** ("nothing substantive"), while **Phase 2's and the cycle's are edit-based** — and
polish normally edits and an edit count cannot tell that from substance, so those two are what self-feed.

**It cannot produce the failure that killed P-A and P3.** It never declares a converged run
unconverged; it changes what RE-OPENS the loop, not what counts as having finished.

It also answers `#298`:46-66, the canonical critique, which diagnosed this a month ago and named the
exact complaint: *"cycles 3 and 4, each triggered by a one-clause javadoc fix, each a full Phase 1 +
Phase 2"*. Its four defects — the gate measures the PROCESS not the artifact; it cannot distinguish a
converging slice from a self-inflicted loop; it needs a counter-rule to stay honest; harden has no cap
— are answered by keying on Phase 1's substantive predicate, and the fourth becomes unnecessary.

## The trade, stated

Phase 2 polish beyond the first pass stops being implemented. Same trade as `pr-harden` 0.26.0's
blocking-only change, same risk, same mitigation (the escalation clause).

## Six sites. A partial application is WORSE than the status quo.

The rule is enforced mechanically, not only written down. Change the prose alone and the hook keeps
forcing cycles while the skill says not to.

1. `skills/harden/SKILL.md` — the Termination section (~:187-196), the blockquote at :193.
2. `skills/harden/SKILL.md` — the `description:` frontmatter, which says "cycling until a whole cycle
   changes nothing" and is what the model reads when deciding to invoke.
3. `hooks/harden-cycle-gate.sh` (237 lines) — `edits > 0 -> block`, `edits == 0 -> allow`, the header
   contract at :4, the block messages at :229 and :235. This runs on EVERY session on this machine.
4. `pipeline/gate-state` — `harden-set --cycle N --count-edits`, and `count_edits()` at :128. The new
   condition needs a field the gate reads; `edits` stays useful as a REPORTED fact (see below).
5. `skills/resolve-ticket/SKILL.md`:437 — "changes nothing".
6. `skills/pr-harden/SKILL.md`:1123 — "drive harden to zero edits".

## What it owes before shipping

- **A test net for the hook.** There is no `gate-test.sh` in `hooks/`. `pr-harden`'s SKILL text refers
  to "both suites", so one may exist elsewhere — find it, or write the cases first. Changing
  enforcement with no net, on a hook that gates every session, is the one thing not to do.
- **The walk-forward against `#298`.** Mine was INDETERMINATE and that must not be papered over: I
  could not tell from its record whether cycles 2-4's findings were Phase-1-substantive or
  Phase-2-polish, so I could not say whether the new rule would have ended it at 3 or at 5. It does
  not have the P-A failure mode either way, but the saving is unquantified. Read the record and say.
- **Keep `edits` as a reported fact.** `#298`'s first defect is that the gate OVER-CLAIMS — "complete"
  is an artifact claim, "zero edits" licenses only "this process has stopped producing". Reporting the
  count while gating on Phase 1 convergence answers that directly, and costs nothing.

## Do NOT re-propose these; they are dead and the grounds are recorded

- A cycle cap (P-A, :1433, killed twice).
- Classifying a cycle by the provenance of its edits (P3, :32).
- `harden`'s documentation-pass classification ported into `pr-harden` as round pricing — not dead,
  but n=1 across 93 records and the benefit overlaps `pr-harden` 0.26.0. Judged not worth it
  2026-09-22.
- An established-facts carry-forward block for agent briefs — ALREADY SHIPPED as P-6,
  `pr-harden`:362. Three grammars of one remedy is the recorded failure at :2771.

## Context this replaces

Shipped 2026-09-21/22 and already in `main`: `pr-harden` 0.26.0 (rounds blocking-only from the
fourth; the orchestrator stops re-running the fixer's build; a verifier path for runtime-visible
changes the prescribed instrument cannot observe) and 0.26.1 (the contrast with `/harden` says why the
exit reads a blocking count). Estimated saving on the 9-round run behind them: ~20%, which is why the
maintainer asked for this instead.

**Separately and worth more than any of it:** measured 2026-09-22, PRs 465, 464, 461, 460 and 457 in
`openmrs-module-chartsearchai` carry ZERO reviews and ZERO inline comments. `claude-pr-review.yml`
fires only on an `@claude review` issue comment, never on push, and no GitHub App reviews there.
`pr-harden`'s FINISH is written as though one reviews every push to a non-draft PR (citing `#381`).
Either that claim needs scoping to the repos where it holds, or FINISH should post the `@claude
review` comment itself — and with a second gate that actually fires, stopping `pr-harden` earlier
becomes defensible in a way it is not today.


---

# Executed — what shipped, and the two things the order owed

## The new contract, as it now reads

`harden`:Termination, and `harden-cycle-gate.sh` enforces exactly this rather than prose about it:

> **`/harden` is complete when Phase 1 has converged and the single Phase 2 pass that follows it has
> run without escalating.**

The state entry grew two fields, written by `gate-state --owner $PPID harden-set --cycle N --phase1
open|converged [--phase2 done] --count-edits`. The hook blocks while `phase1` is `open`, and while
`phase1` is `converged` with no Phase 2 run yet; it allows on `converged` + `done`. `edits` is still
written, still printed and still owed by the report, and gates nothing.

**There are two `phase2` values and the first draft's third is deleted.** `escalated` was sticky —
`--phase1 converged` did not clear it and the hook tested it before `phase1` — so a run whose Phase 2
escalated and whose next Phase 1 pass then converged was handed back the instruction it had just
obeyed, to the six-hour expiry. An escalation resumes Phase 1, which is `--phase1 open`, which
already blocks. Any `--phase1` write with no `--phase2` now resets it to `pending`, which closes the
same stickiness pointing the other way: a `done` left in a reused checkout by the PREVIOUS run, which
let a second `/harden` stop with its own Phase 2 never run. The writer refuses `--phase2` without
`--phase1` for the last corner of it. Both were found by fresh reviewers, in this skill's own first
two passes under this contract.

**A LEGACY entry — one with no `phase1` — keeps the zero-edit rule.** A run already in flight when
this landed cannot re-report itself in the new shape, and of the two directions to be wrong in,
dropping a live run's gate is the one that costs it its outstanding findings. `gate-state` prints `[no phase1 on this
entry: it is a LEGACY entry…]`, keyed on the ENTRY and not on the arguments, so on a FRESH entry a
run that forgets the flag is held to the old contract rather than to none. Keyed on the argument, as
it first shipped, it announced a legacy entry over a `phase1` the entry already carried and the gate
was already enforcing. On an entry that already has a verdict a bare write keeps it and says
nothing — which across runs was an allow-direction hole, closed by dropping the previous run's
verdict when the `--owner` changes, and within a run is just a reason to state the verdict on every
write.

## The walk-forward against #298, which the order recorded as INDETERMINATE

**Answer: the new rule would have left #298 at 5 traversals' worth of Phase 1 passes, not 3. It
removes no Phase 1 pass from that run.** The record's cycles 2, 3 and 4 each found a false
universal in a javadoc or a comment (`:53-57`, and the five-regex-claims entry at `:18-20`), and
`harden`'s Phase 1 says in terms that *"A now-false comment is a Phase 1 finding even though it
'doesn't break runtime'"* — while the gate's cosmetic examples are *"test assertion tightening,
import ordering"*. So those findings are substantive, Phase 1 stays open on each of them, and the
prose loop runs inside Phase 1 exactly as long as it ran across cycles.

**What it removes on #298 is the other two loops.** The run was five traversals of *(Phase 1 to
convergence + Phase 2 to convergence)*, each confirmed by a further traversal. Under the new rule it
is one Phase 1 loop of the same length plus one Phase 2 pass. Gone: four repeated Phase 2 loops, and
the confirming pass each of those loops owed, and the four confirming cycles on top. The saving is
structural — three nested convergence loops collapse to one — and it is **not quantified in
minutes**, because no record times a Phase 2 loop separately.

**It does not have P-A's failure mode**, which is the thing the order needed the walk-forward for:
#298 still ends as converged. The rule changes what re-opens the loop, never what counts as having
finished.

**What this means for the prose loop — the ledger's heaviest parked class, whose last published
running count is `REJECTED.md:3084`'s ~40 plus the 2026-09-20 block's "~4 more records", which that
block did not total: it is not
what this fixes.** A false claim in prose stays a Phase 1 finding, so a run that keeps writing them
keeps buying passes. `harden`'s own *Don't rewrite prose faster than you verify it* — delete the
clause, and when deleting keeps buying passes, delete the CLAIM SHAPE — is still the whole remedy,
and the parked entry's reopen (*"a record where the existing remedy was applied and failed"*) is
untouched by this change. Anyone reading a post-0.34 run that still took hours should look there
first, and not at Termination.

## The test net, with its known-bad control

`skills/harden/gate-test.sh` and `pipeline/pool-test.py` both grew cases for the contract, and
**neither count is recorded here.** The first draft of this section wrote four of them down and every
one went stale within two commits — which is the defect `gate-test.sh`'s own header names, and which
a third reviewer then found here after the same commit had de-staled it there. Run the suites.

The known-bad control is the part worth keeping, because it is a method rather than a number: point
`gate-test.sh` at the pre-0.34 hook (`git show 60a4f2e:.claude/hooks/harden-cycle-gate.sh`;
byte-identical at `cfe8386`) and every failure is a case added for this contract. Not all of them
for the same reason — some fail on the edit count the old hook reads, others on defects found and
closed during the repairs — so read the failures rather than a one-line cause. The sharpest pair inverts in both
directions — `phase1: open` with `edits: 0` must BLOCK where the old hook allowed, and
`converged`/`done` with `edits: 99` must ALLOW where the old hook blocked. Some of the new cases pass
under both hooks on purpose: they pin that the predicate still sits behind the override, awaiting,
staleness and ownership guards, which did not change, and discriminate nothing on their own.

## Residue, named rather than left to be found

- **CLOSED — the first runs have executed.** `/harden` was run on this slice itself, 2026-09-22/23.
  Three Phase 1 passes, by three fresh reviewers, returned 12, 14 and 6 substantive findings, and
  almost all of them were in the contract rather than in anything else. Four were allow-direction
  gate defects: a sticky `escalated` that wedged an escalated run, a `done` that leaked into the next
  run in a reused checkout, an unrecognised `phase2` that disarmed an unambiguous `phase1: open` (the
  0.34.0-to-0.35.0 migration case, since 0.34.0 wrote `escalated`), and a `--phase2` write that could
  report against a verdict its run never made. The saving this change was for is still unmeasured:
  no run has yet gone from a ticket to a PR under it.
- **`--phase1` is not required by `gate-state`.** Making it required would break the `pool-test.py`
  call sites that are about `--count-edits` semantics and not about phases. The cost is that a run
  can write a legacy entry without meaning to on a FRESH entry; the mitigation is the printed
  warning and the fact that the failure direction is the old, stricter rule. On an entry that
  already carries a verdict a bare write keeps it, which across runs was the fourth allow-direction
  defect of this slice and is closed by the owner test rather than by another rule about flags.
  `--phase2`, by contrast, DOES require its pair, unconditionally.
- **CLOSED, and the decision to leave it open was wrong.** A pre-existing fail-open in the LEGACY
  branch: its block message built `"run cycle " + (($c|tonumber?) + 1 | tostring)`, and with a
  non-numeric `cycle` jq's `empty` propagates through the concatenation and suppresses the WHOLE
  object, so the hook printed nothing and the harness read the silence as allow. This file first
  recorded it as deliberately not fixed, on the grounds that the branch was short-lived — and the
  same commit's repair of the copyability half of the same defect is what a reviewer then called
  hunting a class and fixing only the milder member. The cycle is now resolved once in bash, with a
  default, and no jq arithmetic touches it on either branch.

- **Run identity: closed twice over, with one corner left.** The entry is keyed on the checkout and
  says nothing about WHICH run wrote it, and that is where four of this slice's allow-direction
  defects came from — a `done`, an `escalated`, an unrecognised value, and a whole terminal verdict
  picked up by a write that omitted `--phase1`. Two closures now: a `harden-set` whose `--owner`
  differs from the entry's drops the previous run's verdict, count and head first, and the skill
  clears the entry once before its first pass, which is what `pool-run` has always done when it sets
  a worktree up. **The corner:** a second `/harden` in the SAME session and checkout that skips the
  clear has the same pid, so the owner test cannot see it. That needs a real run id, and it is a
  redesign; the two closures above make it the only remaining shape rather than the common one.

- **Phase 2 escalation is not bounded.** Escalate → Phase 1 reconverges → Phase 2 runs again → could
  escalate again. The re-flagging clause (*a finding a previous Phase 2 already raised is not an
  escalation*) is what is supposed to stop it, and it is prose, not a gate. If a run ping-pongs, that
  is the thing to measure.
