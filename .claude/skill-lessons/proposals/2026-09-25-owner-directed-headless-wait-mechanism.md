# 2026-09-25 fifth pass — owner-directed: what a headless run does with background work, measured

The fourth pass recorded that "`claude -p` exits when its turn ends, so nothing re-invokes it / the yield
IS the death" is refuted by #310's own log. Its REOPEN ON asked for a measurement of the current
ceiling before every home is corrected. The owner directed it ("What next?" → "Yes").

## The measurement

`artifacts/2026-09-25-headless-background-wait/summary.md` has the table and the raw per-session
files. Seven headless sessions ran on Claude Code 2.1.282 with the driver's flags:
- a background Bash task is killed about 5 s after the turn ends, and the run exits without being
  re-invoked (60 s and 900 s tasks; `CLAUDE_CODE_PRINT_BG_WAIT_CEILING_MS=0` changes nothing);
- a background agent is waited for. It re-invokes the run if it finishes inside 600 s (10 s and 180 s
  agents), and is stopped at 600 s otherwise, when the run exits with "Background tasks still running
  after 600s; terminating";
- with the variable at 0, a 720 s agent was waited for, and it re-invoked the run.

## P1 — correct the stated mechanism in its homes (a correction; patch bumps)

Bar: a factual claim in the skills and both gates, refuted by measurement, stated as the reason for a
rule that stays. Owner-directed.

- **The one home that states the measured mechanism:** pr-harden's **State** paragraph *That last clause
  holds only for an ATTENDED session* (0.33.3). Bash is killed within seconds; an agent is stopped
  600 s after the turn ends; either way the run exits without re-invoking the orchestrator; an agent
  that finishes inside 600 s re-invokes it. It cites the artifact.
- **Every other home states only what its own rule needs** ("stops an agent still running 600 s after
  the turn ends, then exits"):
  - `resolve-ticket`:273 (0.21.3);
  - `ticket-pool`:390's `died-yielding` row (0.24.4);
  - `pr-harden-gate.sh`'s header, its unattended comment and both block strings, in both copies;
  - `harden-cycle-gate.sh`'s the same, in both copies (harden 0.42.2);
  - `pool-run`'s `died-yielding` comment and message;
  - the gate-test case label "(no next turn)".
- **The harden gate's header is also repaired:** the "THAT HOLDS ONLY FOR AN ATTENDED SESSION" block had
  been inserted mid-sentence, splitting "the harness re-invokes / the session when the agent completes".
  The sentence is now whole, with the block after it.
- **Not changed:** the RULE (an unattended run collects its agents in the turn), the gates' decisions,
  and "which is the death this marker exists to prevent". A run whose agent outlasts 600 s does die.
- **The 2026-08-26 record** gets an appended correction note, per the #409 precedent.

## Not proposed here: the driver setting the variable

`CLAUDE_CODE_PRINT_BG_WAIT_CEILING_MS=0` on driven sessions would make an agent yield survivable, since
session g was re-invoked at +742 s. It would not save a Bash yield, since session d was killed at
+11 s. The gates already refuse the unattended agent-yield, so the setting would be a second barrier,
not a fix for an open failure. It changes the pipeline's behaviour and is the owner's decision.

## Checks on the staged tree

- `gate-test.sh`: pr-harden 36/0 and harden 47/0, on the skill copy and the hook copy of each.
- `bash -n` on both gates.
- `pool-test.py`: 582/0.
- Linter: 10 files, 0 findings.
- The first staging broke both gates, 13 and 30 failures. An apostrophe in the new block strings closed
  the single-quoted `jq` program. The suites caught it before anything was installed.

## The gate's reply (fresh read-only agent, over the staged diff)

It checked every row of the summary table against the raw files, and added that #310 ran on 2.1.246:
8 `init` records under one session id, 7 re-invocations each after an agent completed, and 3 agents
stopped at the end.

- **pr-harden State paragraph → REVISE, BLOCKING.**
  - (i) It contradicted itself: "holds only for an ATTENDED session" next to "an agent that finishes
    inside 600 s does re-invoke it".
  - (ii) "Kills a background Bash task within seconds" over-reached. By a code reading of the
    2.1.282 binary, the wind-down skips the sweep while an agent or an armed `Monitor` is outstanding.
  - (iii) Nothing linked the short-agent case to the "never" rule. #310 supplies it: re-invoked after
    seven yields, dead on the eighth.
  - Its replacement text is shipped, plus its optional clause naming
    `CLAUDE_CODE_PRINT_BG_WAIT_CEILING_MS` once, as the default behind 600 s.
- **Both gates' unattended comment → REVISE, BLOCKING.** It still ended on "it is how the run dies";
  shipped as "is how the run dies whenever the agent outlasts that", in all four copies.
- **Non-blocking, all adopted:**
  - "IN FULL ONLY" in both gate headers;
  - "and nothing at the yield says which case this is" in both block messages, with no apostrophe;
  - resolve-ticket's tense ("until the 2026-08-26 fix … let the gate allow that quietly") and its #297
    wording;
  - the 2026-08-26 record's note and the artifact's `summary.md`, narrowed the same way, with the #297
    attribution now "consistent with the same cause" and two more "does NOT show" items.
- **APPLY as staged:** ticket-pool, `pool-run`, the gate-test label, harden's version.
- **Redacted before commit, at the gate's pointer:** session c's stream carried the account's connector
  list, in the `init` records and in the subagent's reply. The `init` records of all seven sessions are
  now stubs, and the reply paragraph reads "[connector list redacted]". A re-grep over every artifact
  file finds no connector, MCP or account name.
- **Recorded for later, outside this pass:** pr-harden:964 ("without it an unattended run cannot proceed
  at all") and `pr-harden-gate.sh`:33 contradict the unattended block after them, as they have since
  `207a8e3`.

## Applied

As above. Versions: pr-harden 0.33.3, resolve-ticket 0.21.3, ticket-pool 0.24.4, harden 0.42.2.
`gate-test.sh`: 36/0 and 47/0 on every copy. `bash -n` on both gates. Linter: 10 files, 0 findings.
