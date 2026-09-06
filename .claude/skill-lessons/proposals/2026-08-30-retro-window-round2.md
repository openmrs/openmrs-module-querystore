# skill-retro — REVISED proposals after refutation round 1

Round 1's refuter KILLED P6 and forced revisions on P1-P5. Every revision it required has been applied
below; where it offered a choice, the choice taken is stated. P7 and P8 are proposals the ROUND-1
REFUTER raised itself, so they have not yet faced an adversary — precedent for a second round on
refuter-raised proposals is REJECTED.md:863.

---

## P1 · pr-harden :779 (patch) — which `gate-state` subcommands accept `--only`

**Revised edit** (append after :779):

> `--only` is a flag of the await/clear subcommands — `await`, `clear-await`, `clear`. The subcommands
> that write an entry (`pr-set`, `harden-set`, `declined`, `reviewed-sha`) each write the one entry they
> are named for and reject it.

**Round-1 revisions applied.** `harden-set` added (its omission was blocking objection 1); the false
rationale "write the `pr` entry by construction" removed, since `harden-set` writes the harden entry;
the word "alone" removed (Step 4 universal).

**Mechanical remedy considered and declined, with the reason.** Round 1 preferred a `gate-state` change:
accept-and-ignore `--only`, or an argparse `epilog` so the 2026-08-28 usage block reaches `--help`.
Accept-and-ignore is a fail-open — `reviewed-sha --only harden` would silently write the pr entry. The
epilog does not reach the failure either: argparse prints `usage:` plus the error on an unrecognized
argument and never the epilog, so it would only help an agent who ran `--help` first, which none of the
three did. Declining a mechanical fix leaves prose, which round 1 showed has failed twice here.

**Corroboration.** Clause 1, ~5 records: REJECTED.md:966 records the count as 2 and CLOSED by the
2026-08-28 remedy; 256, 263 and 330 are three sightings after that remedy shipped.

**Disclosure round 1 requires in the report.** Two prior remedies at this site did not prevent
recurrence: pr-harden:757-759 already prints the correct `reviewed-sha` invocation fifteen lines above
the `--only` examples, and the `gate-state` usage block applied on 2026-08-28 is unreachable because
`ArgumentParser(description=__doc__.splitlines()[0])` (gate-state:136) prints only the first docstring
line. REJECTED.md:28-30 records the precedent that a twice-failed prose remedy points at a mechanical
one. **If round 2 judges a third prose attempt unsupported, park it.**

---

## P2 · pr-harden (minor) — re-derive an identifier `main` may have taken, at the fetch site

**Revised edit.** Not at FINISH. Append to Step 1's base-fetch passage (pr-harden:122-128), which is
where `git fetch origin main` already happens:

> **And when the base has moved, re-check any identifier this branch allocated from a sequence `main`
> also appends to.** An ADR decision number is the observed instance: the branch takes the next free one
> when it writes the entry, and an upstream PR merged since can have taken the same one. Four collisions
> across three runs, each found by a reviewer who was not looking for it. When it moves, sweep the
> citations of the old value under harden:312's rule — they sit in javadoc and test names, not only in
> the ADR file.

**Round-1 revisions applied.** Moved from §7 FINISH to the fetch site — round 1's blocking objection 2
walked all four collisions forward and showed every one was found at r1 or earlier, before FINISH is
ever read. "Search for the number, not for a phrasing you used" deleted and replaced by a pointer to
harden:312, which already carries it. "which no build or test observes" deleted (unmeasured `cannot`).
"any identifier" kept only as the object of a conditional, not as a universal imperative. The clause-2
claim is struck: REJECTED.md:776-777 rules a *pass* is neither a round nor a cycle, so #256 alone is one
round and this rests on clause 1 only.

**Corroboration.** Clause 1, three records, four distinct collisions: 296:21 (Decision 51 vs #297,
blocking at r1), 238:30 and 238:16 (49 then 52, vs #236), 256:18 and 256:22 (52 then 53, vs #296 then
#238). Different branches, different numbers, different upstream PRs — not one event.

**Step 4.** The PR-description half of the original proposal is dropped as already covered by
pr-harden:476 ("re-measuring every figure in it rather than carrying one forward"), which round 1 showed
covers 256:23 exactly.

---

## P3 · harden :309 (patch) — a pointer to the exit that is already shipped at :310

**Revised edit** (append to :309):

> Where the clause is a count or a universal, :310 already names the exit — publish the measurement and
> its arrangement rather than a rule about it. On #330 four successive rules for one count were each
> measured false before that exit was taken, across ten cycles.

**Round-1 revisions applied.** Reduced from a new rule to a cross-reference: round 1's blocking
objection 1 showed the terminating move is already shipped at harden:310 ("prefer 'mutate the line and
read the failures' to any tally … prefer stating what the thing DOES over what it excludes"), and that
REJECTED.md:840-842 has already ruled this class as "the shipped rule being violated, not two rules
conflicting". "every replacement written under this rule" deleted — the record says only "I applied it
repeatedly" (330:29). The unmeasured "two attempts" threshold deleted. 263 dropped from the
corroboration: round 1 showed its loop ended in a correct correction, not in deleting a claim shape.

**Corroboration.** Clause 2, #330 alone: ten cycles (330:29), four successive count-rules each measured
false (330:13).

**Step 4.** Prunes nothing but is now one sentence rather than five, and it makes an adjacent shipped
rule reachable from the bullet whose failure mode sends readers to it.

---

## P4 · harden (minor) — a relocation loop ends by changing the KIND of question

**Revised edit.** Appended at the END of the bullet, after :221:

> **What ends the loop is a change in the KIND of question, not another entry on the list.** On #256 the
> escapes were a differently-typed field, a parenthesised initialiser, an annotation prefix, a method
> reference and a call shape, each passing with the whole suite green; it settled on asking
> `getDeclaredFields` for the field budget and on matching names rather than bodies for the resolvers.
> On #330 successive relocations — bare and qualified assignment, line wraps, a getter read, two
> extract-a-helpers, a qualified receiver — settled on stating the property positively, at class scope,
> instead of forbidding spellings. The signature to act on is each fix opening the next. A differently
> typed question is not a closed one: #256's reflective guard still exempted `static final`, and that run
> left the `getDeclaredMethod`-with-a-string-literal shape open on the record, so name the residue.

**Round-1 revisions applied.** "seven relocations" deleted — 330:23's list contains "two line wraps", so
the count is not seven and the record states none. "ask what question admits no relocation" deleted and
replaced with the record's own honest version: round 1's blocking objection 2 showed #256's reflective
guard was itself evaded twice more (256:20 `static final`; 256:34 `getDeclaredMethod` left open with
that run's ruling "no textual guard closes the family"). "name-over-bodies for the resolvers" restored
beside `getDeclaredFields` (blocking objection 1: the text→reflection gloss dropped half of 256:17, and
name-over-bodies is still a text guard). The "second evasion" threshold replaced by the signature all
three records share. Placement moved from mid-bullet to after :221, which round 1 showed would otherwise
split the sentence pair at :219-221.

**Corroboration.** Clause 1 (256, 330; #315 already applied at REJECTED.md:706) and clause 2 via #330's
harden c2-c3. The "5 passes" clause-2 claim for #256 is struck per REJECTED.md:776-777.

**Step 4.** Round 1 confirms REJECTED.md:706's applied text warns that no list of relocations is closed
and says nothing about what terminates the loop, so this adds the missing half rather than restating it.

---

## P5 · harden :203 (patch) — a mutation that reddened nothing may not have run

**Revised edit** (append to the evidence bullet at :203):

> A mutation that reddens nothing is a zero result, so :61's question applies to it — ask what its inputs
> could not have produced, and read the build output rather than the test count. On #256 a mutation check
> reported zero red because the mutated method no longer took the argument the edit used, so nothing
> compiled and nothing ran; in a compiling form it reddened two cases. On #263 three configurations ran
> unmutated because `zsh` does not word-split an unquoted `$var`, and it surfaced only because a figure
> that should have moved did not.

**Round-1 revisions applied.** Reframed as an application of the shipped rule at harden:61-73 with an
explicit pointer, rather than a new independent rule (blocking objection 1). "has two readings" deleted
— round 1 showed 263:12-13 supplies at least two more (a guard escaped by an uncovered key, and by a
size-preserving swap). Host corrected from :202-203 to :203; :202 is the withhold-a-change bullet.

**Corroboration.** Clause 1, two records, two independent mechanisms: 256:36 and 263:29.

**Step 4.** Justification for placing it at :203 when the general rule is at :61: :61-73 is Phase 1
guidance and :203 is where the mutation check is actually performed at cycle close.

---

## P7 · harden :107-111 (patch) — RAISED BY THE ROUND-1 REFUTER, not yet adversarially tested

**Edit.** In the "Only ONE of them may mutate the worktree" bullet, correct the parenthetical at :108-110
and add the missing constraint:

> ~~(the Agent tool supports it, and it is the only option that keeps all four able to run code)~~
> (the Agent tool supports it, and it is the option that keeps all four able to run code) — **and tell
> them not to `mvn install`, because the maven repository head is per RUN and shared across its agents
> (resolve-ticket:97), so four isolated checkouts installing into it reinstate the stale-jar hazard the
> isolation was chosen to remove.**

**Bar — stated honestly, because it is contestable.** One record (256:27: "`isolation: 'worktree'`
resolved it, but the briefs had to carry a NEVER-`mvn install` rule too, since four agents installing
into one shared slot-1 repo is the stale-api-jar trap by another route"). The round-1 refuter argued
clause 3, on the ground that harden:109's "the only option that keeps all four able to run code" is
falsified by resolve-ticket:97/:103 ("a per-run head over the shared repository", "two runs sharing it
means one run's classes silently under the other's tests"). **Round 2 should rule on whether clause 3
reaches a contradiction between two skills rather than inside one** — skill-retro:66-68 grants the
single-instance exception "because the contradiction is a fact about the document", singular. If it does
not reach, this is 1 record and parks.

---

## P8 · harden :136 (patch) — RAISED BY THE ROUND-1 REFUTER, not yet adversarially tested

**Edit.** Append to the bullet at :136:

> When a restore does discard intended work, it is recoverable: the registered PreToolUse hook copies
> every modified tracked file outside the repo before the destructive command runs and prints where.
> Look there before re-deriving anything.

**Bar.** REJECTED.md:945-946 names this shape and instructs that it is "the shape to propose next time
rather than re-proposing the `cp` aside", at 2 records. This window adds two: 256:16 (five uncommitted
production edits reverted; "caught only because a later agent diffed the commit against the claim") and
263:27 (hit twice; recovered "from a stash recovery point", not from the hook). Both post-date the hook,
whose file is dated 2026-08-27 and which is registered at settings.json:29.

**The objection round 2 must weigh.** The hook already prints the backup location at the moment of the
command, so the information reached the agent and was not used. That makes this a THIRD prose remedy in
a class where REJECTED.md:28-30 records "Prose did not prevent it twice. Any future remedy is probably
mechanical, not textual." If that reasoning binds, park it.

---

## KILLED in round 1

- **P6 · resolve-ticket Step 1, pre-flight that the tree builds.** Three blocking objections: 296 and 238
  are one `pool-run` defect seen from two sides (296:25 says so outright), so clause 1 fails; "~3 build
  cycles" is not a harden cycle so clause 2 is unavailable (REJECTED.md:776-777); and the cause is closed
  at pool-run:1212-1237, leaving the residual class with no observed member. Parked at 1 event. Reopen
  on a second, independent cause of an unbuildable pristine checkout.
