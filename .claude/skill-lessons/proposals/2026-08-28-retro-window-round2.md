# Retro window 2026-08-28 — round 2 (post-refutation)

Round 1 outcome: **P3 dropped** (3 blocking objections: clause 3 unavailable for a fact about two
filesystems, `REJECTED.md:773-775`; the same Step 6 narrowing killed at `REJECTED.md:786-801` at
0 records / 1 measurement; and read whole the clause names its own subject at `skill-retro:128`).
**P4 dropped as submitted** (0 records, self-declared "no observed drift"; and its wording would
create a second definition of a check `pool-run:830-838` says must be single-sourced).

Below: P1 and P2 revised against their blocking objections, plus two proposals the refuter itself
raised (its MISSED D and MISSED E). D and E have NOT been refuted — they are the refuter's own
findings, so they need a fresh adversary.

---

## P1 (revised) · harden Phase 2 — an idle output file is not evidence an agent has stopped

**Bar: clause 1, two records** (#293, FM2-700), ruled independent by round 1: two repos, two dates,
two harms. Reopen condition met verbatim at `REJECTED.md:828`.

Blocking objections being answered:
1. The draft said "**Both runs** … until the agent finishes". FM2-700 describes no finish — its
   agents were "killed mid-investigation" (`FM2-700:42`). Fixed by attributing each observation to
   its own record.
2. The draft said "the harness reports completion, **so wait for that**", which is exactly what
   #293 could not do: "In-turn waiting had to be blind `sleep` loops" (`#293:27`). `harden:216-229`
   is the mechanism that makes waiting possible. Fixed by naming that section and stating the
   dependency.
3. (non-blocking) `harden:234-237` already carries the harness's terminal-outcome contract. Fixed
   by cross-referencing rather than restating.

**Edit** — new sub-bullet in Phase 2 step 1, after the "If you isolate, do not assume the worktree
is on the branch under work" bullet:

> - **An idle output or transcript file is not evidence an agent has stopped.** Two runs measured
>   it from opposite ends: on #293 the agent output files "stay at 201 bytes until the agent
>   finishes", so there was no progress signal to wait on and the run burned blind `sleep` loops;
>   on FM2-700 `isolation: "worktree"` agents "produced 156-byte transcripts that never grew", that
>   was read as a stall, and two were killed mid-investigation — their kill notices showed both
>   working, at a cost of two discarded agents and about twenty minutes. So do not infer death from
>   a file's size or mtime. A terminal outcome is one the harness reports, which is the signal
>   *Record the cycle so the gate can enforce it* already tells you to clear the `awaiting` entry
>   on — and recording that entry is what buys a blocked cycle the ability to wait for it at all.
>   Where you need progress sooner, watch something the agent's WORK touches; FM2-700 used the
>   worktree's own `target/`.

**Prune (Step 4).** It retires no rule. It does settle, in generalised form, the preference parked
at `REJECTED.md:588-590` ("Prefer then 'establish liveness before treating a past-bound agent as
dead'"), so that parked entry closes rather than a skill line being deleted. Net +11 lines against
a recorded cost of destroyed live work; nothing else in this pass grows a skill.

---

## P2 (revised) · `gate-state` usage block — mark `reviewed-sha` and `declined` as pr-scoped

**Bar: clause 1, two records** (`#293:28`, `#297:32`). Round 1 verified the comment text against the
script: `gate-state:244` and `:251` both write `held.entry("pr")`, and neither subparser at `:172-179`
defines `--only`.

Blocking objection being answered: editing the live copy alone diverges it from the repo mirror with
every automated check green — Step 6 does not cover `.claude/pipeline/` and `pool-run:848` checks
only `("pool-run", "pool-watch")`.

**Edit** — usage block lines 36-37, comments appended to two existing lines, applied to
`~/.claude/pipeline/gate-state` **and** `openmrs-module-querystore/.claude/pipeline/gate-state` in
the same commit, `cmp`-verified:

>       gate-state declined --round 1 --id r1-2 --finding "…" --reason "…"   # writes the pr entry; no --only
>       gate-state reviewed-sha <sha>                                        # writes the pr entry; no --only

**Not adopted** (round 1 non-blocking, leaves the question open → parked): also stating it in
`pr-harden`'s State section. `pr-harden:757-759` already shows the correct invocation fifteen lines
above the `--only` examples at `:774-775`, so whether a second prose home would help is untested.

**Prune (Step 4):** nothing retired. Net 0 lines.

---

## P5 (new — the refuter's MISSED D) · Step 6 and `pool-run` disagree about who checks `gate-state`

**Bar: clause 3 candidate** — a skill's driver script asserting an obligation the skill text does not
carry, leaving one concrete file covered by neither. Not the round-1 P4 argument ("no observed
drift"), which was correctly dropped.

Citations:
- `pool-run:835` — "`skill-retro` Step 6 is what keeps them in step; this reports when they are not".
- `pool-run:848` — the parity set is hard-coded `("pool-run", "pool-watch")`.
- `skill-retro:116-143` — Step 6 mandates `cmp` for the skills, the live-skill↔live-hook gate pairs,
  `~/.claude/hooks/` and `~/.claude/skill-lessons/`, and never mentions `.claude/pipeline/`.
- `harden:227` and `pr-harden:752` instruct every run to call `~/.claude/pipeline/gate-state`.

So `gate-state` — the helper every run invokes, and the file P2 edits — is checked by nothing.

**Edit** — one clause appended to Step 6's existing hooks bullet, naming one file rather than
defining a second general set (round 1's objection 2 to P4):

> …and `cmp` `~/.claude/pipeline/gate-state` against the repo's `.claude/pipeline/gate-state` —
> `pool-run`'s own parity check covers `pool-run` and `pool-watch` and attributes the remainder to
> this step, which did not carry it.

**Prune (Step 4):** nothing retired. Net +2 lines.

---

## P6 (new — the refuter's MISSED E) · a mutation survives because every covering fixture is degenerate on the dimension it tests

**Bar: clause 1, 2-3 records.** Prior park: `REJECTED.md:833-836`, "**1 record, 1 cycle** (#266:16 —
hardcoding four of the crossReactivity map's five keys left the suite green, because the only case
reading it drives the DISABLED state where all five equal the mutation)".

New citations:
- `#234:46-50` — the builder read `Concept.getName()` (locale-preferred) while the site terms came
  from fully specified names; "Invisible to the entire suite because every test built concepts with
  a single ConceptName, where the two spellings coincide." · BLOCKING · cost 1 round.
- `FM2-700:28-30` — "a2bc7a9a added `target_uuid=\"<bare reference>\"` to every fhir_reference row in
  both shared fixtures in the same commit that made the predicate read that column. That, not the
  focus test's shape, is why CI stayed green through the regression."

The standing objection to test: `REJECTED.md:836` — "Possibly already reached by harden's 'Ask of
any clean or zero result what its inputs could not have produced'." The distinction being claimed is
that `harden:192`'s mutation check reports THAT a clause is undiscriminated and not WHY; in all three
cases the why is that every covering fixture collapses the two values into one.

**Edit** — one sentence appended to the mutation-check bullet at `harden:192`:

> When a mutation survives, ask what the fixtures could not have distinguished: in all three recorded
> cases the covering data collapsed the two values into one — every concept built with a single
> `ConceptName`, so the preferred and fully specified spellings coincided (#234); both shared
> fixtures carrying the same string in the column the predicate had just been moved onto (FM2-700);
> and the only case reading a map driving the state where all its keys equal the mutation (#266).

**Prune (Step 4):** if it ships it should subsume the `REJECTED.md:833-836` parked entry. Net +5
lines. If the refuter finds `harden:192` or `harden:193-202` already reaches all three, drop it.

---

## Ledger corrections round 1 established (to be recorded, not proposed as edits)

- Prose-correction cycles is **12 records**, not 11 (`REJECTED.md:843` says 9; three added).
- The `git checkout -- <path>` parked entry's "Remedies stay killed" is now incomplete: a remedy
  SHIPPED — `~/.claude/hooks/git-restore-backup.sh`, registered in `settings.json` PreToolUse/Bash,
  with 452 dated directories under `~/.claude/restore-backups/` — and neither incident this window
  used it (`#234:56-58` "one reapply"; `FM2-700:48-50` "Restoring from a `cp` copy"). The shipped
  net not reaching the operator is a different lesson from the killed one, at 2 records.
- `#297:43`'s co-tenant standalone kill is plausibly a third record of "Cross-session interference
  on a live run's shared state" (`REJECTED.md:811-818`, at 2), not a fresh entry at 1.
