# proposal · replace `/harden`'s zero-edit termination · drafted 2026-09-22

status: WORK ORDER for a fresh session. Nothing applied, no version bumped. The maintainer has
instructed the change directly; this is not a proposal awaiting approval, it is a specification
awaiting execution. What it still owes is the walk-forward and the hook's test net.

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

Both obvious answers are already refuted in `REJECTED.md`, each twice, each on a walk-forward:

- **A cycle cap** (P-A, :1433). Killed twice. `#298` ran 5 cycles and `outcome: converged`, so a cap
  of 4 ends a converged run as did-not-converge and loses cycle 5's measured zero. Runs past 4
  cycles: `#302` (10), `#330` (15), `#266` (7), `#308` (6), `#293` (6), `#234` (6), FM2-700 (6),
  `#297` (5), `#250`, `#315`.
- **A prose-provenance spin signal** (P-B/P3, :32). Killed on the same walk-forward, and it also
  overrode the "prose that IS behaviour is not documentation" carve-out.

The standing reopen condition on both is **"a signal that separates this from spinning, not a cap."**
This is that signal, and the reason it is worth trying is structural: **Phase 1's gate is already
severity-aware** ("nothing substantive"), while **Phase 2's and the cycle's are edit-based** — and
polish always edits, so those two are what self-feed.

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
