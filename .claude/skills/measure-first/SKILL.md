---
name: measure-first
description: Force a baseline + threshold gate before implementing any optimization or potentially-regressive change. Use whenever the user proposes a change motivated by performance, quality, or "this should help X" rather than fixing a known bug. Trigger phrases include "optimize", "speed up", "make X faster", "reduce latency", "add caching", "batch this", "tune", "improve", "I think this would help".
version: 0.1.0
---

# Measure First

A guardrail skill that pins the discipline of proposing a measurement gate *before* writing optimization code. When invoked, the work order is: design the gate → get sign-off → run the baseline → implement → verify against the gate. Skipping straight to implementation produces the revert pattern: hours of work, no clear "did this help?", and a difficult judgment call when the LLM's answers look slightly different but no measurement said which is better.

## The gate

Before any code is written for a change that could regress quality or whose value is "I think this will help", produce three things and stop:

1. **Benchmark harness** — the exact command, query set, or eval that will measure the effect. If the project has a locked rubric (chartsearchai's 10-query × 4-patient eval, `mvn test`, a property-based test suite), that's the harness. If no harness exists, propose one and get sign-off before building it.
2. **Baseline number** — the current state's number on the harness. Not "what we think it is" — what the harness *actually returns* when run today. Run it. Quote the actual numeric or qualitative output.
3. **Minimum threshold to justify merging** — a number, not a vibe. "P@5 must improve by ≥0.03" or "answer correctness must improve on ≥3 of 4 patients on the kidney query". The threshold answers "what would make this worth shipping?" before motivated reasoning sets in.

Three explicit asks, three explicit answers. Then wait for sign-off before implementing.

## Why this matters

Without the gate, the failure mode is well-documented:

- The exp 6 trajectory in chartsearchai (trend syntheses + abnormal flags): shipped, measured *after*, found the LLM never cited the new tokens across 28 answers, reverted. The gate would have caught it earlier — "minimum threshold: LLM cites the new token in ≥X% of answers across the rubric" forces measuring citation rates as the design-time question, not the post-shipping question.
- The persisted-slot KV cache work (per prior-session insights): hours of implementation, reverted because the gain was "too situational". The gate would have refused the implementation work until "how much speedup justifies the complexity?" had a number.
- Multiple session reverts on quality-tuning experiments where post-hoc measurement found no improvement or mixed results.

The cost of designing the gate is ~15 minutes of thinking. The cost of skipping it is hours-to-days of code that gets reverted.

## Reporting shape

When invoked, the output is **not code**. It's the gate proposal, in this shape:

```
## Measurement gate for <change>

Harness: <exact command / eval / query set>
Baseline: <actual current number from running the harness>  ← run it now
Threshold: <minimum delta to justify merging>

Out of band: <any other risks the gate doesn't cover — index size, latency, etc>

Implementation deferred until sign-off on the threshold.
```

If the baseline can't be measured (system not running, harness not built), say so and propose what to do about it before implementing.

## When NOT to use this skill

- **Bug fixes** — the gate is already "the bug is fixed". Just fix it and verify (use `/verify` for that).
- **Correctness changes** — same shape as bug fixes. The test that catches the regression is the gate.
- **Pure refactors** — the gate is "tests still pass and behavior is unchanged". Standard refactor discipline.
- **Doc / ADR changes** — no behavioral gate needed.
- **Changes the user has explicitly framed as exploratory** — "let's just see what happens if we try X". Note that the result is exploratory and not committable without re-measurement.

For everything else — optimizations, quality improvements, "I think this would help" experiments — the gate is required.

## Anti-patterns to catch

- **Motivated-reasoning thresholds.** "Threshold: P@5 improves" is not a threshold. Numbers, not vibes.
- **Hand-waving the baseline.** "Baseline is roughly what we have today" is not a baseline. Run the harness; quote what came back.
- **Implementing the harness as part of the change.** If the harness is being built for the first time alongside the change, you've already committed to the change before measuring. Build the harness first, measure baseline, *then* design the change.
- **Skipping because "this is obviously good".** The whole reason the gate exists is that "obviously good" has been wrong before — trend syntheses were obviously going to help small LLMs reason about abnormality; they didn't, the LLM ignored them. The gate is what made that finding cheap instead of expensive.
- **Post-hoc threshold setting.** Setting the "minimum improvement" *after* seeing the result is the optimization equivalent of `p < 0.05` hacking. The threshold is part of the gate; the gate runs before the experiment.

## Pairs with `/verify`

`/measure-first` is the *design-time* discipline — propose the gate before the change.
`/verify` is the *post-change* discipline — prove the change cleared the gate by running it.

Together they bound the optimization workflow on both ends: don't start without a measurable goal, don't claim done without measurable evidence.
