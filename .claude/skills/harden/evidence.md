# harden — the evidence behind the rules

`SKILL.md` states each rule as a run needs it. This file keeps what the rules rest on: the incidents,
measurements and history that `SKILL.md` carried until 0.43.0, moved here verbatim so that a run no
longer loads them and no measurement is lost. Headings match `SKILL.md`'s. Each entry quotes the rule
it supports as `SKILL.md` states it, then the passage as it stood in 0.42.2, so an entry can repeat the
words of its rule.

A run does not need this file. Read an entry before changing or deleting the rule it supports: a
measured rule is not deleted without the measurement that retires it (`skill-retro`, Step 4).

## Phase 1: Structural review (/review style)

### Test coverage (mandatory in every Phase 1 pass)

*Cut down, H1-01 — the rule it supports:* “A premise no fixture can falsify is not covered, however many tests name it.”

**And ask what the FIXTURE can express, not only what the test asserts.** Where a rule rests on how
some external system behaves — a judge, a parser, a remote — check whether the stub standing in for it
can even produce the counterexample. Measured on the #302 run: every test of a new rule drove a stub
that always refused a conjunction, so the rule's central premise ("a correct judge says no") was
assumed and never exercised; the cell where the real system says yes was unreachable by the whole
suite, and when a reviewer finally constructed it the design reversed. Cost: four cycles of work built
on the unexamined premise. A premise no fixture can falsify is not covered, however many tests name it.

*Cut down, H1-02 — the rule it supports:* “Ask of any clean or zero result what its inputs could not have produced”

**The stand-in is not always a stub — it can be the INPUT POPULATION a measurement enumerates, and it
need not be a measurement you wrote.** Two runs, one shape: a candidate population keyed on rows'
display names, in which the tie the rule turned on cannot arise, so the result came back clean in good
faith. On #250 an adversarial sweep of a two-clause predicate "took each row's DISPLAY NAME as the
recorded order, a population where no two rows of a family can tie above rank 0"; the unguarded half of
the line was found instead by a reviewer's mutation, at a cost of one round. On #268 the sizing the
TICKET offered ("0 of 36 reachable") measured a display-name population too, while the rule turned on a
leg that ties on a name that is no row's display name — caught at the gate, before code. Ask of any
clean or zero result what its inputs could not have produced, and ask it of a measurement you rely on
as readily as one you ran. **And of a STABLE result, ask what the repeats could not have varied.** On
#315, n=3 byte-identical answers were published as stability while every repeat reused the server's
cached prefix — the engine's own javadoc says that cache makes a borderline argmax non-deterministic, so
the repeats measured the cache, and the prompt figures resting on them were weaker than they looked.
Name what is reset between repeats; where nothing is, the repeats never exercised the path a first run
takes. **And of a PASSING check, ask what it actually examined.** A check that discovers its own subject —
a source or class-file walk, a directory scan, a script reporting on output it captured itself — can
return the same clean result whether the subject was compliant or absent. Two runs of #315: a walking
architecture guard passed every rule on a wrong source root, having scanned nothing, and a capture
script wrote its done-marker without checking, so an arm that captured nothing read as a clean, empty
A/B with exit 0. Make an empty discovery FAIL, and choose the thing you assert was found so that a
sibling could not supply it — the same run found that asserting the intended root merely exists is not
equivalent, because the sibling omod module carries the same package path.

## Phase 2: Polish (/simplify style)

*Cut down, H1-03 — the rule it supports:* “Spawn four parallel review agents (reuse, quality, efficiency, integration) over the current diff, in ONE message”

1. Spawn four parallel review agents (reuse, quality, efficiency, integration) over the current diff, in ONE message, each with `run_in_background: false` where the `Agent` schema carries it — the default is background, and an unattended run's gate refuses the yield that leaves them outstanding (#433; `pr-harden`'s *Collecting in the same turn*). Where a previous Phase 2 ran, brief its agents with the applied and deferred lists from it so they don't re-surface them.

*Cut down, H1-04 — the rule it supports:* “Never pass `model` to any of them.”

- **Never pass `model` to any of them.** A per-call override beats both the agent definition's
  frontmatter and settings.json, so it is the strongest of the levers and the only one a running
  pass can pull on its own initiative — the others are set in a file or on the command line,
  outside any session. It is how a pass ends up reviewed by a weaker agent than the one that
  wrote the code — and the temptation is strongest on the lenses that keep
  returning clean, which is exactly where a missed finding is invisible. Two things measured on
  #354, stated apart because they say different things. The REUSE lens was put on a cheaper model
  and still returned real findings twice (duplicated test helpers; one measurement restated in
  three homes), so "this lens is cheap, it can take a cheaper agent" was false of it. The
  EFFICIENCY lens returned nothing on any of its four runs — but it was never run on the session
  model, so nothing there separates a quiet lens from a quiet agent, and that pairing is the one
  the run cannot speak to. Sharper: the pass whose CLEAN verdict closed Phase 2 was a retry moved
  to a cheaper model, and the next cycle's agent — same head, session model — found a sentence in
  citable production text that was false. A `PreToolUse` hook
  (`~/.claude/hooks/no-subagent-model-override.sh`) refuses the call, so this is enforced rather
  than asked. What it refuses is the per-call parameter and nothing else — an agent definition's
  `model:` frontmatter and a configured default subagent model both outrank the session model and
  produce no call for it to see, so do not read the hook as a guarantee that every subagent runs on
  the session model. That scope is stated once, in the hook's own header beside the code.

*Cut down in 0.43.1, H5-01 — the rule it supports:* “refuses the call, so this is enforced rather than asked”

- **Never pass `model` to any of them.** A per-call override beats both the agent definition's
  frontmatter and settings.json, so it is the strongest of the levers and the only one a running
  pass can pull on its own initiative — the others are set in a file or on the command line,
  outside any session. It is how a pass ends up reviewed by a weaker agent than the one that
  wrote the code — and the temptation is strongest on the lenses that keep returning clean, which
  is exactly where a missed finding is invisible. A `PreToolUse` hook
  (`~/.claude/hooks/no-subagent-model-override.sh`) refuses the call, so this is enforced rather
  than asked. What it refuses is the per-call parameter and nothing else — an agent definition's
  `model:` frontmatter and a configured default subagent model both outrank the session model and
  produce no call for it to see, so do not read the hook as a guarantee that every subagent runs
  on the session model. That scope is stated once, in the hook's own header beside the code.

*Cut down, H1-05 — the rule it supports:* “Only ONE of them may mutate the worktree, or give each its own.”

- **Only ONE of them may mutate the worktree, or give each its own.** This is the one place the skill
  contradicted itself: Phase 2 mandates four *parallel* agents and every brief tells them to
  mutate-and-restore for evidence, so the four are the same hazard to each other that the
  orchestrator is to them. Measured: four concurrent agents on one checkout produced a tree that
  went clean→dirty with "a revert of a branch order I did not make", a build that collapsed into
  842 `NoClassDefFound` errors, and an agent that opened on another's uncommitted mutation and spent
  a detour concluding the branch was red — two of four reports contaminated, and both flagged it
  themselves rather than being caught. Pick one: pass `isolation: "worktree"` so each agent gets its
  own checkout (the Agent tool supports it, and it is the only option that keeps all four able to
  run code); or license exactly one agent to mutate and tell the other three to read only; or run
  the mutating ones serially. Whichever you pick, say it in every brief — "be careful" does not
  survive contact.

*Cut down, H1-06 — the rule it supports:* “If you isolate, do not assume the worktree is on the branch under work.”

- **If you isolate, do not assume the worktree is on the branch under work.** Name the ref in every
  brief and have the agent confirm its diff is the SIZE of the change before it reviews anything —
  non-empty is fail-open, and that is the shipped wording being satisfied by the case it exists to
  catch. The cause
  differs between runs and neither is worth diagnosing here: on the #269 run every agent's worktree
  "opened at origin/main, not the PR branch, so all six had to check the branch out themselves",
  and two reported the diff they were asked for came back empty until they did; on the #250 run the
  branch existed but git refused it to a second worktree because the main one held it, so agents
  needed `git checkout --ignore-other-worktrees`. Both cost a detour per agent and no round or
  cycle, which is why this is one sentence in the brief rather than a step.
  **And name the BASE, never a local branch name** — on #336 four isolated agents all found the local
  `main` ref stale by many commits, so `git diff main...HEAD` showed ~15k lines and every brief had to
  name the base sha explicitly. That is the diff a non-empty check waves through. `pr-harden` Step 1
  carries this for its reviewer, with its own measurement; the hazard is the same here.
  **And a worktree left behind by a killed run is a dead agent's tree, not the head — the
  orchestrator is who meets them.** On #348/PR369 three of four orphans differed from it: a swapped
  prompt clause, a deleted clause, and a `System.err.println` probe with an added map. Nothing reaps
  them, because the run that leaves them is the one that died.

*Cut down, H1-07 — the rule it supports:* “An idle output or transcript file is not evidence an agent has stopped.”

- **An idle output or transcript file is not evidence an agent has stopped.** Two runs measured it
  from opposite ends: on #293 the agent output files "stay at 201 bytes until the agent finishes",
  so there was no progress signal to wait on and the run burned blind `sleep` loops; on FM2-700
  `isolation: "worktree"` agents "produced 156-byte transcripts that never grew", that was read as
  a stall, and two were killed mid-investigation — their kill notices showed both working, at a
  cost of two discarded agents and about twenty minutes. So do not infer death from a file's size
  or mtime. Inside the gate's allow a terminal outcome is one the harness reports, and it is the
  signal *Record the cycle so the gate can enforce it* has you clear the `awaiting` entry on; past
  that allow what decides is the bound, which is a clock rather than a liveness check. Where you
  need progress sooner, watch something the agent's WORK touches — FM2-700 used the worktree's own
  `target/`, which presupposes isolation, so #293's half of this is not closed.
  **And never read that file for the RESULT either** — it is the agent's transcript, not its
  report; `pr-harden`'s **State** section owns how a delegated agent is collected, and why a
  poll costs more than it returns.

*Cut down, H1-08 — the rule it supports:* “Commit before anything mutates the tree — the cycle's work before spawning them, and your own measurement probes too — and do not edit the tree while they run.”

- **Commit before anything mutates the tree — the cycle's work before spawning them, and your own
  measurement probes too — and do not edit the tree while they run.** `git checkout -- <path>` to
  undo a probe restores HEAD, so on a file carrying uncommitted intended work it discards that work
  as well: measured on the #302 run, four production edits vanished that way and the empty
  `git diff --stat` afterwards read as "restored" rather than "reverted". These agents are told to mutate-and-restore for evidence, and a restore comes from what the agent READ — so an edit that lands after it read and before it restores is silently reverted. Measured: a quality agent mutated a guard to test whether a case could discriminate it, restored the file from its remembered copy, and reverted a fix applied in the meantime. It compiled and the suite passed; it surfaced only when a later test failed inexplicably. A commit is the one thing a remembered restore cannot undo. Tell them to restore with `git checkout -- <path>`, never by rewriting remembered content. And when a restore DOES discard intended work, say where to look: the registered PreToolUse hook copies modified tracked files outside the repo before the destructive command runs, best-effort and bounded, and prints the destination and count — trust that printed message rather than assuming the file is there. Both incidents in this window were found by someone other than the agent whose command caused them, so the hook's own output had already scrolled past by the time anyone was looking.

*Cut down, H1-09 — the rule it supports:* “Phase 2 runs ONCE — not to convergence.”

**Phase 2 runs ONCE — not to convergence.** A phase that re-runs until it changes nothing is a loop
feeding on its own output, because its gate cannot tell a polish edit from a substantive one and
polish normally produces some; it was the larger half of what made this skill take hours. Apply what the agents found, build, and stop. Polish you did not reach is a
deferral, and Reporting says what a deferral owes.

## Termination: Phase 1 converges, then one Phase 2 pass

*Cut down, H2-01 — the rule it supports:* “The two gates above end a *phase*.”

The two gates above end a *phase*. Which one ends the RUN is what this skill kept losing, and the
answer used to be "one cycle that produces zero edits". This skill is 55-72% of a `resolve-ticket`
run's wall clock (196-251 min of 335-379, measured over the four runs of 2026-09-17), and the
maintainer's instruction on 2026-09-22 was that the zero-edit condition is why. Three convergence
loops were nested: a pass confirming a pass, a phase confirming a phase, a cycle confirming a cycle. Only the innermost was severity-aware. Phase 1's gate asks whether
the last pass found anything SUBSTANTIVE; Phase 2's and the cycle's asked whether anything was
EDITED, which cannot tell a polish edit from a substantive one. So the outer two re-opened the loop
on the loop's own output whenever it produced any, and a one-clause javadoc fix bought a whole fresh
Phase 1 + Phase 2 (#298, cycles 3 and 4). Not that polish ALWAYS edits — #298 converged on a cycle
whose measured zero covers its Phase 2 as well, so that universal is false, and the anti-pattern
below on *any / only / exactly / all / never* is the rule it broke.

*Cut down, H2-02 — the rule it supports:* “The run's condition is Phase 1's, and there is one loop”

So the run's condition is Phase 1's, and there is one loop:

*Cut down, H2-03 — the rule it supports:* “It changes what RE-OPENS the loop, never what counts as having finished”

**What this does not license.** It changes what RE-OPENS the loop, never what counts as having
finished, and Phase 1's own bar is untouched: a now-false comment is still a Phase 1 finding, a false
universal in a javadoc is still substantive, and "it's basically converged" is still not the
condition — on #229 every one of the last seven passes found exactly one real defect, all prose, and
six would have shipped under that stop. Read that as a bound rather than a measurement of this rule:
#229's record carries no phase attribution at all, so whether those seven were Phase 1 passes is a
classification by the rule above and not something the record settles. What is gone is the confirming CYCLE, not the
confirming PASS. Two remedies that WOULD have relaxed the bar are refused and stay refused: a cycle
cap, which at the obvious value of 4 ends #298's converged 5-cycle run as did-not-converge, and
classifying a cycle by the provenance of its edits.

*Cut down, H2-04 — the rule it supports:* “And a prose finding about BEHAVIOUR is checked by running it, not by reading it”

**And a prose finding about BEHAVIOUR is checked by running it, not by reading it.** A
documentation-only diff can carry a behavioural falsehood: on #302 the claim that `clauseScoped=true`
supersedes that issue's own treatment was written into `config.xml` and the README, and only driving
the real `verify` refuted it — the flag REMOVES the rule and reinstates the symptom. A coherence
sweep would have found that sentence coherent, and false. And where prose IS behaviour, the file it
lives in does not save it: a prompt paragraph a test asserts a substring of, a log or failure message
under assertion, a global-property default or the description shipped beside it.

*Cut down, H2-05 — the rule it supports:* “Report the edit count. It no longer gates.”

**Report the edit count. It no longer gates.** At the close of each traversal run `git status
--porcelain`, count the commits, and report both. It is a fact the report owes, and it was never the
artifact claim the old rule made of it: a zero-edit cycle licenses "this process has stopped
producing", not "complete" — which is why `pr-harden` reviews the result in a context that did not
write it.

*Cut down, H2-06 — the rule it supports:* “Before ending the run, say this out loud with the measured values filled in”

**Termination gate** (forcing function — the phase gates demand verbatim sentences and get them; this one was prose and got skipped, so it is the same shape). Before ending the run, say this out loud with the measured values filled in:

*Cut down, H2-07 — the rule it supports:* “A mutation that reddened nothing has not shown the guard is dead until you know it RAN”

- **A mutation that reddened nothing has not shown the guard is dead until you know it RAN.** It is a
  zero result, so ask of it what its inputs could not have produced — and where that zero would
  license deleting the clause, make the loop redden ON PURPOSE before believing it: run one mutation
  it must redden, and watch that fail. On #256 a mutation check reported zero red because the mutated
  method no longer took the argument the edit used, so nothing compiled and nothing ran; in a
  compiling form it reddened two cases. On #263 three configurations ran unmutated because `zsh` does
  not word-split an unquoted `$var`, and it surfaced only because a figure that should have moved did
  not. Reading the run's own output is what failed next, in both directions: two runs spelled a test
  selector `A+B`, which selects nothing (`-Dtest` takes a comma — measured 2026-09-14 on surefire
  3.5.5), and one read that void run as clean while the other read its BUILD FAILURE as a test
  failure. Residue the control does not close: a harness whose own failure is a spurious RED satisfies
  the control and the mutation alike.

*Cut down, H2-08 — the rule it supports:* “A guard expected to stay GREEN needs a positive control, and no mutation of production reaches it”

- **A guard expected to stay GREEN needs a positive control, and no mutation of production reaches
  it.** The revert-check and the add-a-guard obligation both test a guard that SHOULD redden; a
  negative assertion — "nothing was injected", "this name does not appear" — is supposed to pass, so
  a green suite says nothing about whether its subject could ever have arisen. Build the case it exists for and watch it FAIL.
  Measured on #360: a control's chart came from a helper that wires no validator, so the collection
  it asserted was empty could not have held anything, and two successive review rounds — not the
  mutation check — found it. On #355 a verifier briefed to look for a partner the shipped data does
  not carry re-drove the contract with one it does, plus a control, rather than reporting the absence
  as a result.
  **And an exemption you WRITE into a guard is the same hole from the inside** — an allow-listed
  method, a by-name exempt file. It is not coverage you are tolerating; it is where the next defect
  lands, because the carve-out was made for a reason the next change does not honour. On #448 a guard
  allow-listed a whole method and the uncharged splitter was written inside it, and the round that
  tightened that rule and its neighbour had both walked through again — a round each, both raised by a
  fresh reviewer; on #445 the endpoint class was exempted from the new dialect rule it most needed, at
  2 cycles. So build the case the exemption ADMITS, and watch it pass.

*Cut down, H2-09 — the rule it supports:* “Ask which logger and level the harness captures, how it RENDERS what it captured”

- **And a control measures the HARNESS it ran in, not the property.** This is the mirror of *Residue
  the control does not close*: there a harness's own spurious RED satisfies the control, here its
  blindness satisfies the guard, and both leave a negative assertion standing over a channel nothing
  ever drove. Ask which logger and level the harness captures, how it RENDERS what it captured, and
  whether a sibling test's residue changes either. Rounds 2, 3 and 4 of #439, one round each: a
  capture raised one class's logger to WARN, so the same details logged at `info` passed; the shared
  capture helper rendered a throwable's TYPE alone, so the patient's medication names attached to a
  diagnostic exception passed every guard; and a leftover `LoggerConfig` from a sibling capture held
  the guard under one surefire run order and not another. A liveness precondition is not the answer —
  #439's passed on unrelated events while the negative it protects was vacuous.

*Cut down, H2-10 — the rule it supports:* “name the direction it must never fail in, and build the input for that direction”

- **And where the thing you changed REPORTS — a warning, a finding, a check — name the direction it
  must never fail in, and build the input for that direction.** Two runs, each found by a fresh agent
  rather than by the author: on #337 a carve-out applied to both operands withdrew the record-side
  exit, so an answer reproducing an operator note FAITHFULLY was reported, and the carve-out had to
  become per-operand; on #276 a boundary asserted to "fail toward silence" did the opposite —
  `statesWord` matched a fragment of a larger number and produced a false report on a published key.

*Cut down, H2-11 — the rule it supports:* “And ask whether the thing you fixed has a SIBLING”

- **And ask whether the thing you fixed has a SIBLING. The revert-check above cannot answer that** —
  reverting a fix shows the suite observes it, and says nothing about a second member of the same
  family still carrying the defect. Three runs, and in each one the un-widened sibling left the suite
  green: a normalisation compiled twice from one pattern, where widening one of the two made a name
  unfindable in the record that renders it (#293); a validity rule's detail corrected on one rule and
  not on the sibling rule beside it, and a literal asserted at one of its two call sites (#266); and a
  fail-open fixed in one script of a family, left standing in the member that was actually gated
  (#315). So ask what else is the same thing — a second definition, a sibling rule, another call site,
  a sibling script — and check that too. A green suite after fixing one member is not evidence that
  the family has one member.

*Cut down, H2-12 — the rule it supports:* “And where the guard is over TEXT, mutate the SUBJECT too”

- **And where the guard is over TEXT, mutate the SUBJECT too — the four mutations above are all of the
  guard, and none of them moves the thing it forbids.** Relocate that string: into a comment, across the
  file's own line-wrap, behind a block comment, into a sibling declaration outside the slice the guard
  reads — and check it still reddens. On the #315 run one guard was defeated five ways in turn, each
  found by the next fresh agent at a cycle or a round apiece and each fix opening the next, so treat no
  list of relocations as closed. Two the run paid for: bound the window at the construct it is about
  rather than at a line count, and have both halves of a two-sided guard read text normalised the same
  way.

*Cut down, H2-13 — the rule it supports:* “what ends the loop is a change in the KIND of question, not another entry on the list”

- **Where successive fresh agents each defeat the same guard or predicate one more way, what ends the
  loop is a change in the KIND of question, not another entry on the list.** That is a rule about
  TERMINATORS, and is not *treat no list of relocations as closed*, which forbids believing a list
  complete. The family is not text guards alone: on #421 a production predicate over TYPES escaped
  three cycles running — exact type equality, then a raw type, then wildcard and type-variable bounds
  — each escape found by the next fresh agent. On
  #256 the escapes were a differently-typed field, a parenthesised initialiser, an annotation prefix, a
  method reference and a call shape, each passing with the whole suite green; it settled on asking
  `getDeclaredFields` for the field budget and on matching names rather than bodies for the resolvers.
  On #330 successive relocations — bare and qualified assignment, line wraps, a getter read, two
  extract-a-helpers, a qualified receiver — settled on stating the property positively, at class scope,
  instead of forbidding spellings. A differently typed question is not a closed one: #256's reflective
  guard still exempted `static final`, and that run left the `getDeclaredMethod`-with-a-string-literal
  shape open on the record, so name the residue.

*Cut down, H2-14 — the rule it supports:* “So mutate the VALUE under an asserted key, not only the key”

- **And the TEXT scoping above leaves out a DATA form of the same attack: a guard that asserts a key is
  PRESENT passes a placeholder under it.** On #340 a reflective guard asserted only `containsKey`, so a
  `put` of `null` beside a new public accessor satisfied it while dropping the value — the change's own
  defect, shipped green under a test that appears to cover it, and closed only by comparing the
  accessor's own reading against the published value. #263 lost two more, to a key the guard did not
  cover and to a swap that preserved the size it checked. So mutate the VALUE under an asserted key, not
  only the key.

### Record the verdict so the gate can enforce it

*Cut down, H3-01 — the rule it supports:* “And record an `awaiting` entry whenever a cycle delegates, or the gate will not let the cycle wait for its own agents.”

**And record an `awaiting` entry whenever a cycle delegates, or the gate will not let the cycle
wait for its own agents.** Phase 2 spawns subagents, and one spawned in the background leaves the
cycle nothing to do but yield — and a yield is exactly what the gate refuses, in an unattended run
even with the await recorded, which is why step 1 spawns in the foreground. Measured on the run that
added this: a Phase 2 pass blocked on a background agent tripped the gate on every yield, and the
only way to stay alive was two ten-minute in-turn wait loops, which is pure waste. `pr-harden` solved
this first and its **State** section carries the reasoning; the field and the semantics are the same.
**Stamp `owner` with `$PPID` too.** This state is keyed on the checkout, not the session, so without it
the gate cannot tell your entry from one a co-located run left in the same directory; `pr-harden`'s
**State** section carries that reasoning as well:

*Cut down, H3-02 — the rule it supports:* “`--run` is what makes this checkout's leftovers yours to ignore.”

**`--run` is what makes this checkout's leftovers yours to ignore.** Nothing clears the entry between
interactive runs, and successive reviewers found a leak per pass while the rule was *carry unless
listed* — each through the field or the caller the last fix did not name, and the worst of them an
`awaiting` from a dead run, which allowed a stop on `phase1: open`. No count is kept here; `git log`
has them, and three passes running found the count itself wrong. A write whose `--run`
differs from the entry's — or that carries no id at all — REPLACES it, so the default is *drop
unless this run wrote it*, and what a field added later inherits is nothing rather than everything
absent from a list. The first version of this exempted an entry with no id, on the reading that an
absent id means adoptable; that put the same `awaiting` back through the same door, because every
pre-0.37 entry has no id. It is not `--owner`, which answers whether a live
foreign session holds this checkout — a different question, one a resume changes and a run id does
not, and conflating them is what left the same-pid corner this replaces.

*Cut down, H3-03 — the rule it supports:* “any `--phase1` write with no `--phase2` resets it to `pending`”

**There is no `--phase2 escalated`, and the first draft's was deleted rather than repaired.** It was
sticky: `--phase1 converged` did not clear it and the hook tested it before `phase1`, so a run whose
Phase 2 escalated and whose next Phase 1 pass then CONVERGED was handed back the instruction it had
just obeyed, every turn, to the six-hour expiry — built by a fresh reviewer, in this skill's own
first run under this contract. An escalation resumes Phase 1, which is `--phase1 open`, which the
gate already blocks on. And **any `--phase1` write with no `--phase2` resets it to `pending`**, so
the Phase 2 owed after that convergence cannot be satisfied by the one that escalated — nor, in a
reused checkout, by a `done` the PREVIOUS run left behind, which was the same stickiness pointing
the other way and would have let a second `/harden` stop with its own Phase 2 never run.

*Cut down, H3-04 — the rule it supports:* “On an entry that already carries a verdict, a bare write keeps it, so do not use one to refresh the count”

**`--phase1` is what ends the run, and omitting it leaves whatever the entry already says.** On a
fresh entry that is nothing, and the gate reads a run-stamped entry with no verdict as a run in
flight that owes one, so it BLOCKS — a forgotten flag costs you a pass, not the contract. Only an
entry with neither a run id nor a verdict is legacy, and every `gate-state` command that touches the
harden entry — `harden-set`, `await`, `clear-await` — requires `--run` and stamps it, so the legacy
branch serves entries that were already on disk before ids existed. `clear-await` was the last one
without that requirement, and it created a legacy entry out of nothing while refreshing the six-hour
expiry that is the documented way out of a wedge. On an entry that
already carries a verdict, a bare write keeps it, so **do not use one to refresh the count**: say the
verdict every time. Across runs this is handled for you by `--run`, which replaces the whole entry
rather than dropping fields from it — NOT by `--owner`, which answers a different question and whose
drop is gone. `gate-state` says which it did on the line it prints. `--cycle` is a label on the traversal and nothing reads it as a number — advance it on an
escalation if you like, but the gate does not care and the edit measurement tolerates it staying
put, which is why the hook hands back whatever the entry already has.

*Cut down, H3-05 — the rule it supports:* “Do not read the fallback as the same measurement”

`--count-edits` is a REPORTED fact now, not the gate's input: uncommitted lines plus the commits made since the previous traversal
of this run closed — both halves, because a cycle that commits its work has still changed something.
That is why the helper records this cycle's `head` on every write. **Do not read the fallback as the
same measurement**: where no recorded head resolves, the commit half becomes `@{u}..HEAD`, which is
everything unpushed on the BRANCH and does not return to zero until the push, so it can read non-zero
on a cycle that changed nothing. Both directions have been paid for — on #255 and #229 an upstream-only
reading scored `edits=0` for cycles carrying 9 and 3 unpushed commits; on #357 the branch had an
upstream and cycle 8 read `edits=16` at convergence. Which one a
run met was decided by whether its branch was cut in a form that sets an upstream (`git checkout -b
<branch> origin/main` does; pulling `main` first and branching off it does not) — a choice
`resolve-ticket` Step 4 leaves open, and neither form is wrong. Neither misreading can end or extend
a run any more, which is one thing keying the gate on Phase 1's verdict buys outright; both still
corrupt the figure you report, so this stays.
Where it cannot measure the commit half — a first cycle with no earlier head, or a recorded head that
stopped resolving after a rebase or a recreated checkout — the helper prints that, instead of counting
it as zero; that line is yours to answer with your own `git log`. It is counted THERE and not here
so that the number recorded and the number you report cannot be two different numbers.

*Cut down, H3-06 — the rule it supports:* “Do not retype the mechanism; call the helper.”

**Why a helper rather than the inline `python3` this used to be.** The read, the change and the write
are one critical section, and they were not: every writer read the whole file, changed its own entry
and wrote the whole file back, with no lock. One session at a time made that merely fragile. Several
make it lossy — and lossy in the direction that kills runs, because the entry that vanishes is
somebody's `awaiting`, and their gate then sees a run that quit with agents outstanding. Measured with
20 concurrent writers to 20 different working trees: the inline form kept **3 of the 20** entries,
valid JSON throughout, nothing raised. `gate-state` holds an exclusive `flock` across both state files
and writes atomically. Do not retype the mechanism; call the helper.

*Cut down, H3-07 — the rule it supports:* “only `done` ends a run, which is decidable without knowing what the value means”

`harden-cycle-gate.sh` ships next to this file and is what reads that entry. On a Stop event it refuses to end the turn while the newest entry for this directory says Phase 1 is `open`, or that Phase 1 has converged and Phase 2 has not run. It fails open on every ambiguity (no file, malformed JSON, no jq, stale entry, an unrecognised `phase1`), so it can only ever cost you the pass you owed. An unrecognised `phase2` is deliberately not in that list: only `done` ends a run, which is decidable without knowing what the value means, and the check used to sit ahead of the `phase1` test so any unknown value disarmed an open verdict. It CAN hold a session, though — an
entry this session owns and never clears blocks every turn in that directory until the six-hour expiry,
and the way out is to finish the phase or take the labelled override, not to wait.

*Cut down, H3-08 — the rule it supports:* “write the entry regardless, and treat an uninstalled gate as a reason to be stricter with yourself rather than looser”

Until it is installed the gate is prose only, which is exactly the state that let the rule get skipped — so write the entry regardless, and treat an uninstalled gate as a reason to be stricter with yourself rather than looser.

## Anti-patterns

*Restated, H4-01 — where the rule stands:* “Do not manufacture a finding to look thorough.”

- **Don't invent concerns** to justify another pass — diminishing returns are real signals.

*Restated, H4-02 — where the rule stands:* “Stop Phase 1 when ANY are true:”

- **Don't run another pass** if the only items are below the noise floor or the agents start agreeing on "nothing actionable." That is Phase 1's convergence, and reaching it is the point; it is not licence to skip the Phase 2 pass that Termination requires after it.

*Restated, H4-03 — where the rule stands:* “a pass that itself found a substantive (non-cosmetic) issue cannot be the last pass”

- **Don't end with Phase 1 open.** "It's basically converged, and the last finding was small" is the single most common way this skill stops early. A pass that found something substantive was not the last pass, whatever its edit count — see Termination.

*Cut down, H4-04 — the rule it supports:* “The tell is the handback, not the claim.”

- **Don't hand the termination decision back to the user.** This is the disguised form of stopping early, and it is harder to catch than the honest form because it reads as deference. "You should get to decide whether to spend another cycle", "want me to keep going?", "say the word and I'll run cycle N+1" — all of these end the run with Phase 1 open while looking like good practice. Note what a naive check misses: reporting *truthfully* that the run has not converged and then handing back is still a violation, so a detector aimed at false convergence claims will not see it. The tell is the handback, not the claim. If the rule requires another cycle, run it; if you are not going to, use the labelled override in Termination, which states plainly that you overrode a rule rather than asking permission you already had instructions about.

*Cut down, H4-05 — the rule it supports:* “delete the unsupported clause instead of replacing it with a better-sounding one”

- **Don't rewrite prose faster than you verify it.** When a cycle's findings are all in text *you wrote in the previous cycle* — a comment, a failure message, a doc paragraph — stop rewriting and change tactics: delete the unsupported clause instead of replacing it with a better-sounding one. Replacing an unverified causal claim with a different unverified causal claim reads as progress and buys another mandatory Phase 1 pass, because a now-false comment is a Phase 1 finding; several passes in a row of this is the signature. Prefer stating only the mechanism you can check, naming candidates without ranking them, and saying outright that the evidence does not distinguish them. "I could not establish which" is a finished sentence. **And when deleting the clause is itself what
  keeps buying cycles, delete the CLAIM SHAPE.** On #330 four rules for one count were written in
  succession and each measured false, across ten cycles; what ended it was publishing the measurement
  with its arrangement and no rule about it. Once a second attempt at a claim of some kind has been
  refuted, the move is to stop making a claim of that kind, not to make a better one.

*Cut down, H4-06 — the rule it supports:* “spend one attempt trying to falsify it; prefer stating what the thing DOES over what it excludes”

- **Don't publish a claim a later cycle must re-measure.** **And the rule is not about tallies — it is about claims you cannot check.** A universal or an exhaustive characterization is the same defect in different grammar, and it slips past a reader watching for digits: *any*, *only*, *exactly*, *all*, *never*, *the whole*, *cannot*. Measured on the seventh run, five such claims in three consecutive cycles, each written to correct the previous cycle's false claim and each false in turn — "any looser pattern would reject" (looseness has more than one dimension), "it only re-admits `M01AE0`" (it re-admits any single trailing digit), "matched only the 5- and 7-character shapes" (the old pattern matched 6 too), "exactly the two levels the ladder is known to be handed" (nothing on the path validates a code's shape), and one that mis-numbered the very level it was excluding. So before writing one about code you just wrote, spend one attempt trying to falsify it; prefer stating what the thing DOES over what it excludes; and name the residue rather than claiming there is none. A count is the obvious case — prefer *"mutate the line and read the failures"* to any tally — but the universals are the ones that survive review, because nothing about them looks like a measurement.

*Cut down, H4-07 — the rule it supports:* “After inserting a member into an existing file, read the neighbours of the insertion point”

- **Don't trust a script's report that it edited something.** Every one of these passes edits by running a short script, and `str.replace` returns the string unchanged when it matches nothing while the script prints success anyway. One false claim survived FIVE cycles of this skill that way. Assert the target text is present before replacing; after a multi-line replacement, count what should still be there (a slice bounded by "this javadoc to the next method" once deleted a whole test method, and it compiled); and verify by reading the file back rather than by believing the script. **That clause is about a DELETION; an INSERTION has its own silent failure — it separates a javadoc from the member it documents.** After inserting a member into an existing file, read the neighbours of the insertion point and check that each doc comment still sits against the member it names. Three runs paid a review pass each for one (#234, #229, #255 — silent through compile, checkstyle and 1686 tests; on #255 three agents reported it independently). **And the diff already knows where to look, so stop relying on noticing:**

*Cut down, H4-08 — the rule it supports:* “A hit is a place to READ and not a finding”

It names every file where an insertion landed directly below a doc comment's `*/`, which is the shape of every instance above. Give it a range: the bare form reads the working tree, which *Commit before anything mutates the tree* has already emptied. A hit is a place to READ and not a finding — it fires on the commit that REPAIRS an orphan as readily as on the one that creates it. Measured 2026-09-14 on this project: it names the file on each of the two commits whose successors are titled as fixing an orphan, is silent on one of those fixes, and over 40 commits of `main` printed two lines — one of them a javadoc orphaned in `ModuleSourceRoot` that a full `/harden` and a `pr-harden` round had shipped. The remaining sweep is the one the check cannot do: read the doc comment it names and ask whether it still describes what now follows it. Another silent failure of the same family — a script that batches several edits behind one write, and reports the ones an abort never made — is in `pr-harden`'s *Editing by script*, with its two rounds.

*Cut down, H4-09 — the rule it supports:* “enumerate the claim's SUBJECT instead — every sentence in the tree about the thing the claim is about”

- **Don't stop correcting a claim at the site you noticed it.** A false statement is rarely in one place. Measured on one run of this skill: a correction reached one of seven homes, then five of six, then five of six again, and once the two halves of a single paragraph contradicted each other after one half was fixed. Search for the claim's rarest single TOKEN, over the whole tree rather than over the docs, fix every hit, then grep for the phrasing you just wrote to see where it now lives. Searching the PHRASING is what leaves the last home standing, and it fails two different ways: a phrase the file's own formatting has split — markdown emphasis inside it, a line break falling between a quantifier and its noun — does not match what you typed, while a home that is a DATA file rather than a doc is missed by scope alone. Both were paid for on the #266 run, a cycle per survivor, each hidden by a mechanism the one before it had not used; treat no list of those mechanisms as closed. **And a home that names the claim's key nowhere is reachable by no token search at all.** On #374 six cycles of sampling turned up eighteen homes of one claim and one of them was a site like that. So when a sweep keeps returning one more home, do not add a mechanism to the list; change the kind of question, as *What ends the loop is a change in the KIND of question, not another entry on the list* requires, and enumerate the claim's SUBJECT instead — every sentence in the tree about the thing the claim is about, which is a population you can finish. Two homes are easy to miss — the project's own instruction file, and anything outside the repo (a PR description, an issue comment) that no grep will reach. And a positional cross-reference ("the bullet above") is a claim about layout that any insertion falsifies: name the target instead of locating it. **And where the claim already has a home, correct that home and point the others at it rather than maintaining N corrected copies** — on #315 a measurement had reached five files and been corrected twice before it was consolidated into one, everything else pointing there. A pointer is not free: #412 paid two passes for two dangling ones, the second introduced by the fix for the first, so a pointer must name its target and not locate it; and an instruction file already at its byte budget cannot take one at all.

*Restated, H4-10 — where the rule stands:* “In every Phase 1 pass, follow each of these threads at least one level out from the slice”

- **Don't review the slice in isolation.** Integration bugs hide outside the file diff — at trigger boundaries (a sibling service mutates state without notifying you), classloader boundaries (an optional dep's absence breaks static class resolution), and lifecycle boundaries (a consumer scans before you register). Every Phase 1 pass MUST trace at least one level out on each integration thread (trigger paths, optional deps, lifecycle order, state propagation, invalidated invariants in unchanged neighbors, and re-deriving the merged result from scratch). The slice's correctness contract spans its boundaries — a fix that lives in a sibling service, or in an unchanged neighbor your edit falsified, is still a Phase 1 finding when the slice surfaces or depends on the bug. See "Trace outward" in Phase 1.
