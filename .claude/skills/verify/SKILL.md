---
name: verify
description: Verify a claim or behavior by running the real system rather than reasoning from code. Use when the user asks "does X work?", "did that change behavior on the demo?", "is this actually fixed?", or whenever a claim about runtime state needs grounding. Trigger phrases: "verify", "test on the demo", "check this works", "prove it", "is this actually fixed".
version: 0.1.0
---

# Verify

A guardrail skill that pins the empirical-verification discipline. When invoked, the goal is to **produce concrete evidence from the running system**, not to reason from code or ask the user to check things in their browser.

## The discipline

If a claim is about runtime behavior — a demo, a deployed service, a database, an external API — the answer must come from that running thing, not from reading code that *should* behave that way.

**Do this:**
- `curl` the REST endpoint, capture the response body, paste the relevant bytes
- Playwright/headless browser for UI behavior, capture a screenshot
- SQL the live database for the actual state, paste the row counts or values
- Run the project's eval/test harness, paste the score
- Tail the log while triggering the behavior, paste the relevant lines

**Don't do this:**
- "Looking at the code, this should..." — code inference is fine for design questions, not for runtime claims
- "Can you verify this in your browser?" — only if Playwright genuinely can't reach the UI (rare)
- "The test passes, so X works" — test passing ≠ production behavior; check the actual production path
- "It probably does Y" — if uncertain, run the thing

## Per-project gates

Each project may have specific evaluation gates that block "is this fixed?" claims:
- `chartsearchai` / `querystore`: run the 10-query × 4-patient rubric (`/tmp/qs_fire_queries.sh` + `/tmp/qs_multi_patient_eval.py`) when retrieval/embedding/LLM-prompt behavior changes; eyeball the LLM answers, not just the rubric P@5
- General Java projects: `mvn test` must pass before reporting completion
- Demo-server changes: smoke-test the actual deployed surface, not just the build artifact

If the project's CLAUDE.md or ADRs name a gate, run that gate before declaring done. If a gate exists and you're tempted to skip it, the skip is itself a finding — say so out loud.

## Reporting shape

When verification completes, the report names:
1. **What was run** (the exact command, endpoint, query, or harness)
2. **What came back** (the concrete output, not "it looks right")
3. **What the comparison was against** (baseline, prior behavior, expected output)
4. **The verdict in one sentence** (works / partially works / broken, with the specific evidence)

If verification *didn't* happen — because the system isn't reachable, the eval can't be run, the test fixture is missing — say that explicitly: "I could not verify because X." Don't substitute code reasoning for missing verification.

## When NOT to use this skill

- Pure design questions ("should we use X or Y pattern?") — those are reasoning, not verification.
- Hypotheticals ("if we did X, would Y work?") — answer with reasoning, optionally with a small empirical probe.
- The change is so trivial verification is overhead (a typo fix, a doc-only commit).

For everything else, when the user is asking a "does it actually work?" question, the burden of proof is on the running system, not on the code or on the user's browser.

## Anti-patterns to catch

- **Claiming "pushed" when only edited locally.** Always distinguish: edited locally / committed / pushed. State the repo and branch.
- **Reporting a model name or API endpoint you didn't verify exists.** If unsure, say so and offer to check rather than guessing a plausible-sounding identifier.
- **"All tests pass" without listing the actual count or running the gate the project requires.** Quote the test count from the actual run output.
- **Skipping the eval because "the code change looks safe".** The whole reason the gate exists is that "looks safe" has been wrong before.
