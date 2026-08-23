---
name: skill-retro
description: Turn the run records the pipeline skills leave behind into skill improvements — read the accumulated evidence, propose edits only where a lesson is corroborated, have every proposal refuted by a fresh agent, prune as much as you add, then version-bump and push. Also runs the mechanical self-contradiction linter over the skill files. Use when asked to improve the skills from what recent runs learned, or on a cadence. Trigger phrases include "improve the skills", "run the retro", "what did the last runs teach us", "skill-retro".
argument-hint: "[--since <date>] [--lint-only] [--dry-run]"
version: 0.1.0
---

# Skill retro — evidence in, governance change out

`resolve-ticket` and `pr-harden` each append a **run record** when they finish. This skill turns
those records into edits to the skills themselves. It exists because the alternative — each run
editing the skills at its own end — is worse in four measured ways, and those four are the whole
design rationale:

1. **A run cannot grade its own lessons.** Measured on run seven: the orchestrator's own
   retrospective claim about why two rounds shared a sha was false, and survived until someone asked
   for it in writing and it got checked. A step that derives and commits in one motion ships that.
2. **The best lessons are not the author's.** Run seven's most valuable change — pin the rule
   structurally instead of bending the design to be observable — came from an adversarial fresh
   reviewer. Self-retrospection systematically misses that class.
3. **One run cannot tell a pattern from noise.** The strongest rules in these skills cite several
   runs, and read "the recurrence stopped only when…". A per-run commit structurally cannot produce
   that shape.
4. **Append-only growth dilutes.** The governance surface was 1715 lines across four skills when this
   skill was written. These files are read by agents whose compliance falls as the document grows, so
   every addition has a cost and something has to push back.

So: capture is automatic and belongs to the pipeline skills. Derivation and commit are here.

## Step 1 — Read the evidence

Records live in `~/.claude/skill-lessons/*.md`, one per finished run, written by the skill that ran.
Read every record since the last retro (the last one is recorded in `~/.claude/skill-lessons/LAST`,
if present; `--since <date>` overrides). Say how many records you read and which runs they cover — a
retro over one record is a retro that cannot corroborate anything, and should say so rather than
proceed as if it could.

Each record carries, per run: what the refutation gate objected to and whether it settled; every
finding a FRESH agent raised that the run's own author had missed, with its round; every claim a
measurement refuted; every place a skill's own text blocked or contradicted the run; what was
declined and why; and the rounds or cycles each cost.

## Step 2 — Run the mechanical linter

`python3 <this dir>/skill-lint.py [roots…]` — defaults to `~/.claude/skills`. It checks only what a
script can decide: a count stated over a list of a different length, a state field documented but read
by no gate script, missing frontmatter. Exit 1 if it reports anything.

**What it does not check is the interesting half, and do not let its green fool you.** A skill
contradicting itself in SUBSTANCE — `harden` Phase 2 mandating four *parallel* agents while every
brief told each to mutate the shared worktree — is not mechanically decidable, and that one cost two
of four agent reports on run seven. Nor can any such check see a field that OUGHT to exist and does
not, which is what the same run's `awaiting` gap was. Both classes are Step 3's job.

`--lint-only` stops here.

## Step 3 — Propose, with a corroboration bar

A lesson earns a proposal when **any** of these holds. State which, per proposal:

- it appears in **two or more** run records;
- it appears once and **cost two or more rounds or cycles** in that run;
- it is a skill **contradicting itself, or contradicting its own gate script** — valid from a single
  instance, because the contradiction is a fact about the document rather than an inference about the
  world. This is the one class where one sighting is enough.

Anything below the bar goes in the report as *observed, not yet actionable*, with the count so far.
That list is the whole point of keeping records: a lesson seen once is not discarded, it is waiting.

**Write the guard, not the diagnosis, when the cause is not established.** Run seven left two
`pr-<n>-r<round>` refs sharing a sha, consistent with three different mechanisms and decidable by
none of the surviving evidence. The proposal that shipped states the check and explicitly refuses to
name a cause. A guard is worth having when the cost of the case it catches does not depend on how the
case arose — say that, rather than inventing the mechanism the reader expects.

## Step 4 — Prune as much as you add

For every proposed addition, answer in the report: **which existing rule does this subsume, retire or
render stale?** Net line growth needs a sentence justifying it. And:

- **Never delete a measured rule without recording the measurement that retires it.** These skills
  are full of rules whose whole value is the measurement behind them; a rule deleted silently is a
  measurement thrown away, and the next run re-learns it the expensive way.
- **Prefer deleting an unsupported clause to rewording it.** Measured across three cycles of run
  seven: every correction that replaced a false claim with a better-sounding one introduced a new
  false claim. The recurrence stopped at deletion.
- **Do not add a count a later reader must re-measure**, and treat *any*, *only*, *exactly*, *all*,
  *never*, *cannot* as the same defect in different grammar. Five such claims went stale on that run.

## Step 5 — Refute every proposal, with a fresh agent

One subagent, read-only, **never `subagent_type: "fork"`** — a fork inherits the reasoning that
produced the proposals, which is the one thing this step is for. Give it the proposals, the run
records they rest on, and the current skill text. Do not give it your argument for why they are right.

It answers, per proposal: does the record actually say this? Is the corroboration bar really met, or
is one sighting being counted twice because two records describe one event? Does the proposal
contradict an existing rule, or re-propose something a skill records as measured and rejected? Does it
add a claim nobody has checked? Would the edit have prevented the thing it cites?

**An objection without a citation is not an objection** — it must point at a run record, a line of a
skill, or a recorded measurement. A blocking objection whose citation settles it: drop or revise that
proposal. One that leaves the question open: park the proposal as *observed, not yet actionable*
rather than arguing. There is no deadline here, and a rule added wrongly is more expensive than a rule
added late.

## Step 6 — Apply, bump, push

Apply the surviving proposals. Then, per skill touched:

- bump `version:` in the frontmatter — minor for a new rule, patch for a correction;
- re-run the linter and leave it at zero for the files you touched;
- copy the skill into the source repo's `.claude/skills/` (this pipeline's source is
  `openmrs-module-querystore`; `git remote -v` in that checkout is the authority) and verify with
  `cmp` that the live copy and the repo copy are byte-identical, **including any `*gate*.sh`** — the
  registered hooks under `~/.claude/hooks/` are SEPARATE copies, so a skill push alone leaves the gate
  running old logic;
- one commit in the repo's own voice — `<skill> <version>[, <skill> <version>]: <lowercase summary>` —
  whose body cites the run records, and which says for each change what corroborated it;
- push, then write today's date into `~/.claude/skill-lessons/LAST`.

`--dry-run` stops before applying and prints the proposals with their corroboration and objections.

## Reporting

- how many run records were read, and which runs;
- linter findings, and what was done about each;
- per applied change: the rule, what corroborated it, and what it pruned;
- per parked lesson: the observation and its count so far;
- every proposal the refuter killed, with its citation. **This list is the evidence the bar is real** —
  a retro that applies everything it proposed did not have a bar.

## Anti-patterns

- **Don't retro a single record.** Corroboration is the only thing separating a rule from an anecdote;
  with one record, run the linter, park the observations, and stop.
- **Don't let the skills grow every time.** A retro whose net effect is +200 lines has moved the
  problem rather than solved it. Pruning is Step 4, not a nicety.
- **Don't write a lesson as a rule when the cause is unknown.** Write the guard and say the cause was
  not established.
- **Don't skip Step 5 because the proposals look obvious.** Obvious-and-wrong is the whole failure
  mode these skills document; four false premises died at exactly this gate on run seven.
- **Don't edit a skill you have not read in this session.** These files carry rules whose reasons are
  measurements; an edit made from the diff alone re-opens something that was closed deliberately.
