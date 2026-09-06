# Under review: two skill additions shipped without a retro gate

Committed as 552f265 on branch skills-wait-on-condition-and-batch-probes in
/Users/danielkayiwa/Projects/openmrs/openmrs-module-querystore (UNPUSHED). No entry in ~/.claude/skill-lessons/REJECTED.md.

## Addition 1 — pr-harden 0.15.0 -> 0.16.0
```diff
--- /Users/danielkayiwa/Projects/openmrs/openmrs-module-querystore/.claude/skills/pr-harden/SKILL.md	2026-09-02 01:56:01
+++ /Users/danielkayiwa/.claude/skills/pr-harden/SKILL.md	2026-09-02 01:07:06
@@ -2,7 +2,7 @@
 name: pr-harden
 description: Harden an open pull request by cycling clean-context review rounds against it — a fresh agent reviews the pushed head, a second fresh agent implements every finding it agrees with and declines the rest on the record, the build is proved green, the change is verified on a real standalone where runtime behaviour is at stake, and the round is committed and pushed. The cycle repeats until a review round reports zero blocking findings. Use when a PR should be hardened by reviewers who have never seen it being written. Trigger phrases include "harden this PR", "review and fix the PR until it's clean", "cycle review rounds on PR N".
 argument-hint: <pr-number-or-url> [--max-rounds N] [--no-verify]
-version: 0.15.0
+version: 0.16.0
 ---
 
 # PR harden — clean-context review rounds until nothing blocks
@@ -396,6 +396,22 @@
 **It owns the environment and repairs it.** Kill the orphaned `llama-server` holding the port, delete
 the stale omod and redeploy from the root install, set `log.level`, allow for cold load on the first
 query, wait out a slow boot. Do it without asking.
+
+**Wait on a CONDITION, never on a clock.** "Wait out a slow boot" is not licence to sleep blind.
+Measured across 20 pipeline runs (2026-09-02): 429 wait episodes burned 818 turns and 64.4 h —
+**58.9% of all Bash wall time in the pipeline** — as `for i in $(seq 1 40); do sleep 15; done`, at
+~426k context-tokens per turn, because a waiting turn is re-sent the whole conversation in order to do
+nothing. The boot those waits covered is the 1–3 minutes `verify-frontend-change` documents, and that
+skill has said *poll, do not guess a sleep* since before any of those runs — so the rule is not new.
+What it was missing is that a FOREGROUND poll still costs a turn per look, which is how one wait came
+to average 1.9 of them. A fixed sleep cannot exit early and cannot fail loudly. Use ONE backgrounded
+loop that exits when the condition is true —
+`Bash(run_in_background: true)` running
+`until curl -sf -o /dev/null http://localhost:$PORT/openmrs/; do sleep 5; done` — which hands the turn
+back at once and notifies you when it exits. Give it the failure signatures too (`ModuleException` in
+the log, the java pid gone), or a crashed boot is indistinguishable from a slow one, and bound it so a
+hang cannot outlive the round. `Monitor` is for a STREAM of events — every error line in a log,
+reported as it appears — and is the wrong tool for "tell me when it is up".
 
 **Unless `$CLAUDE_PIPELINE_SLOT` is set, in which case it owns ITS SHARE of the environment.** That
 variable is the pool driver telling this run it has co-tenants — other `resolve-ticket` runs working
```

## Addition 2 — resolve-ticket 0.14.0 -> 0.15.0
```diff
--- /Users/danielkayiwa/Projects/openmrs/openmrs-module-querystore/.claude/skills/resolve-ticket/SKILL.md	2026-08-30 20:46:14
+++ /Users/danielkayiwa/.claude/skills/resolve-ticket/SKILL.md	2026-09-02 01:07:06
@@ -2,7 +2,7 @@
 name: resolve-ticket
 description: Take a GitHub issue or JIRA ticket URL all the way to a pull request that is ready to merge, in one unattended run — read the ticket with its comments, plan, have the plan refuted by a fresh agent, write the failing test first, implement, prove the build green, harden with context, open a draft PR, then cycle clean-context review rounds until one reports zero blocking findings and mark it ready. Use when handed a ticket or issue URL and asked to deliver a reviewed PR. Trigger phrases include "work this issue", "resolve this ticket", "take this to a PR", "implement and harden issue N", "here's the ticket, deliver a PR".
 argument-hint: <issue-url|jira-url|issue-number|jira-key> [--max-rounds N] [--no-verify] [--plan-only]
-version: 0.14.0
+version: 0.15.0
 ---
 
 # Resolve ticket — one URL in, a mergeable PR out
@@ -636,6 +636,15 @@
 - **Don't spawn a subagent to write the implementation.** Then nobody holds the writing context, the
   judgement calls get made by an agent nobody can steer, and Step 7's harden loses the one advantage
   it has over the review loop. `Explore` for searching is fine; the judgement stays here.
+- **Don't spend a turn per read-only probe.** This session never compacts — measured over 20 pipeline
+  runs (2026-09-02) it grows from ~100k to ~971k tokens, and a turn taken *here* costs 3.03× the same
+  turn inside a subagent (mean context 410k against 135k). So a turn costs its whole context **plus**
+  the ~1.1k it adds to every turn after it, which is why this session's own round trips, not its
+  output, are the pipeline's largest single cost. 336 stretches of back-to-back read-only Bash calls
+  with no reasoning between them cost 796 avoidable turns and ~0.56B tokens across those runs. When
+  the next several commands are reads whose answers you want together — `git log`, `git diff --stat`,
+  the file, the grep — issue them as ONE call. This is a round-trip rule and **not** a delegation
+  rule: the judgement stays here, and the asymmetry the bullet above rests on is untouched.
 - **Don't change production to create observability without ruling out a structural pin first.**
   A plan that says "this is behaviour-neutral, so I must change X to make it testable" is one move away
   from making the code worse in the name of rigour — and the move it skipped is a grep of the test tree
```

---

# Step 5 gate — fresh read-only agent, 2026-09-02

Both additions were live on one machine, committed to an unpushed branch (`552f265`,
`skills-wait-on-condition-and-batch-probes` in the `querystore` checkout), and had never been through
this gate.

## Provenance, established first

The quoted figures were checked against the **raw** published artifact, not a text extraction.
`429`, `64.4`, `58.9`, `3.03`, `410,406` and the words `busy`, `sleep`, `batch` return **zero** hits.
`971,327` is present but denotes run #339's single worst orchestrator peak, not the general endpoint
addition 2 uses it as. `~426k`, `~100k` and `~1.1k` appear in no source at all. The remainder come
from a 2026-09-02 re-measure that was never published and is in no run record — and whose only home,
a machine-local memory file, gives two unreconciled numerators for one phenomenon (61.9 h and 64.4 h).

`skill-retro` Step 1 admits run records in `~/.claude/skill-lessons/` and nothing else, so **neither
addition met any clause of the Step 3 bar**. The two records that do mention sleep loops
(`2026-08-24-pipeline-timing-measurement.md:38`, `2026-08-27-…-293.md:27`) are agent-output polling —
the case already remedied by pr-harden 0.15.0 — so addition 1 cannot claim them either.

## Addition 1 — APPLIED, REVISED (pr-harden 0.16.0)

Cleared on the merits, and the mechanism was verified against the harness rather than the claim:
`Monitor`'s own description routes this case to `Bash(run_in_background: true)` with an until-loop and
warns against an unbounded command for a single notification. Not a restatement either —
`verify-frontend-change`'s readiness bullet gives a FOREGROUND poll and no backgrounding, which is
exactly the gap.

Three objections carried, all fixed rather than argued:
1. **The measurement paragraph went.** The rule does not rest on it; Step 4's *prefer deleting an
   unsupported clause to rewording it* applies. What remains cites only sources a reader can check.
2. **Placement was wrong.** The rule sat in `### 6 — VERIFY`, a fresh subagent's brief, while the cost
   it cited is the ORCHESTRATOR's. A sibling paragraph now sits in the State section, beside the
   existing "no excuse for waiting out a timeout", which is where this session reads.
3. **The harness contradicts itself** — `Bash`'s description points at `Monitor`, `Monitor`'s points
   back at `Bash`. The rule now names the conflict instead of leaving a reader to find it and conclude
   the skill is wrong.

Recorded as a **1-record readmit justified by a mechanism gap, not as corroborated.**

## Addition 2 — DROPPED (resolve-ticket reverted to 0.14.0)

The collision it was written to avoid genuinely is not there: the wording delegates nothing and the
"round-trip rule and **not** a delegation rule" disclaimer holds against the asymmetry at
`resolve-ticket:17-19`. It dies on other grounds, each sufficient:

1. **Zero run records**, and unlike addition 1 there is not even a near-miss.
2. **The harness already mandates it** as a standing instruction ("make all of the independent calls
   in the same block"). `REJECTED.md` **P6** settled that a standing bypass-mode instruction is
   dispositive against a skill's preference. What the bullet added over the harness was nine counts.
3. **It is P0/P7 again, at the line P0/P7 cited.** It landed 17 lines below `resolve-ticket:622`,
   *"Don't publish a count you would have to re-measure every round; publish the method."*
4. **Its superlative is refused by its own source.** "the pipeline's largest single cost" — the
   analysis ranks the static floor first and says its blocks are "shares of one total, **not** a
   partition of it". The lever is 0.56B, 5.5% of 10.15B.
5. **"This session never compacts"** is promoted from the analysis's explicit hedge that the check
   "could not have detected compaction".

**REOPEN ON:** a run record where a read-only probe stretch cost a round or a cycle. Publishing the
re-measure does not reopen it — that converts unsourced counts into sourced ones in the document that
forbids counts.

## Where the gate itself was wrong

Its lead section declared `552f265` nonexistent and the rules never committed, calling it dispositive.
It searched one checkout and asserted only one exists. `git cat-file -t 552f265` returns `commit` in
`~/Projects/openmrs/querystore`. The framing was wrong; every objection resting on other evidence was
checked independently and stands. **A gate's own factual claims are claims too** — the sibling of
resolve-ticket 0.14.0's rule about a gate objection's numbers.
