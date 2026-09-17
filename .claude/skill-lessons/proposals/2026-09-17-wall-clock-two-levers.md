# proposals · pr-harden + harden — two overlaps the wall clock pays for · drafted 2026-09-17
status: **DRAFT.** Step 5 has NOT run: no refuter has seen either edit. Nothing is applied and no
        version is bumped. The measurement behind them is
        `../2026-09-17-pipeline-wall-clock-measurement.md`.
target: `pr-harden` 0.24.0 (one paragraph in §6) and `harden` 0.32.0 (one paragraph at :197-201).
        P3 proposes no edit and is parked with the experiment that would settle it.
found by: a wall-clock measurement of the four runs of 2026-09-17, asked for by the user ("takes 7hrs
          and more — anything we can do without reducing quality of the generated pull request?").
          Not by a run record. The runs' own records were not yet in this repo when this was drafted,
          which is a stated limit under P2.

## The shape the measurement found

A run is a **strictly serial chain of 10-15 agent waves** — the sum of the wave spans equals their
union, so nothing ever overlaps anything — with the orchestrator's own generation and its builds
slotted into the gaps between them. 63-70% of the run has an agent outstanding; only the first one or
two waves carry four lenses; the tail is one agent at a time.

So the only quality-free saving available is **overlapping work that does not depend on a previous
result.** Neither proposal below removes a pass, a round, a cycle, a lens or a check; neither puts an
exit condition under a clock (2026-08-24 P2's kill); neither touches `--max-rounds` or the
termination rule (#229's counterweight stands).

---

## P1 · `pr-harden` §6 — the verifier that covers the merging head can run in the reviewer's wave

**The lesson.** `pr-harden`:629-630 says it itself: "Step 6 sits on the fix path, so without this a PR
whose round 1 found nothing blocking would reach `gh pr ready` with the standalone never started."
In a clean round, therefore, the verifier run that actually covers the merging head is step 7's — and
it reads *the same sha the reviewer just read*. Today it starts after that reviewer returns, so the
last thing every converged run does is wait for a verifier it could have launched alongside the review
that cleared the PR.

**Corroboration (measured, three of four runs; the fourth ran no verifier).**
Method, so the next reader re-derives rather than trusts it: for each run take the verifier's spawn
time and the previous agent's own last event, then list every `git commit`/`git push` between them.

| run | clean review returned | verifier spawned | gap | pushes in between | verifier span |
|---|---|---|---|---|---|
| #446 | t=346.5 (r3) | t=348.2 | 1.8 min | none | 8.6 min |
| #447 | t=320.6 (r1) | t=322.0 | 1.4 min | none | 10.1 min |
| #450 | t=354.0 (r2) | t=355.6 | 1.6 min | none | 18.9 min |

**The edit** — one new paragraph in §6, after "Gate this on what the round actually changed, at most
once per round":

> **A verifier owed on the head the reviewer is reading can run in the reviewer's wave.** Step 6 sits
> on the fix path, so in a round that returns nothing blocking, the run that covers the merging head is
> step 7's — and it goes out after the reviewer has finished, against the same sha, with nothing pushed
> in between. Spawn it alongside that reviewer instead: one wave, two agents, one sha. The coverage
> question is unchanged — *Whether a verifier run still covers the head is a question about the compiled
> artifact* — so if the round turns out non-clean, the speculative report is **discarded rather than
> carried onto the fixed head**, and step 6 verifies as it does today. Still never the reviewer, and
> never two verifiers against one slot's standalone at once.

**What a refuter should press.**
1. §7 pushes its own non-blocking edits *after* the last verifier run (:630-631). A speculative report
   does not cover those, so the saving is real only where §7 edits nothing or where the equivalence
   rule at :649-654 proves the push neutral. In all three runs above nothing was pushed — but three
   runs is the whole evidence base, and a run that edits at §7 loses the saving and spends an extra
   verifier.
2. It adds a discardable agent to a non-clean round. That is tokens, a standalone deploy and a
   rate-limit exposure, against 8-19 min of wall clock in the clean case. The ledger's rate-limit-death
   entry (6 records) is the cost side nobody has priced here.
3. "One wave, two agents" must not read as licence for the reviewer to deploy — :394's reason
   (a reviewer that deploys grades its own deploy) is exactly what the wave puts next to each other.

---

## P2 · `harden` :197-206 — the cheaper confirming cycle can be a wave instead of one agent

**The lesson.** The rule already prices the confirming cycle down to a single agent for a
documentation-only diff, and holds it to three obligations: the correction method in full, the build,
and RUNNING any claim about behaviour. One agent doing three obligations in series is the whole
cycle's wall clock, and unbounded — the longest single-agent confirming wave in the window ran 73.8
min while nine cores and the orchestrator waited.

**Corroboration, with its limit stated.** Two single-agent waves named as confirming passes cost 29.2
min (#448 cycle 4) and 73.8 min (#450 cycle 4). Three further single-agent cycle-level waves have the
same shape — 35.5 (#448 "Phase 2 round 3 claim verification"), 40.8 (#447 "Cycle 3 whole-change
review"), 31.6 (#450 "Cycle 3 combined verification lens") — but their descriptions are all this pass
read, and **whether those three were documentation-only cycles is not established**: the four runs'
own records were not in this repo when this was drafted. So the corroboration for the edit as written
is 2 waves / 2 runs. **What would close it:** read those four records once they land and classify every
single-agent wave.

**The edit** — appended to the paragraph that ends "and, for any claim about behaviour, RUNS it rather
than reads it":

> The three obligations may go to two agents in one wave rather than one agent in series, and the cycle
> then costs its slowest obligation instead of their sum: the correction sweep reads and needs no
> install, so it can take an isolated worktree; the behavioural run needs the build, so it stays where
> the run's `~/.m2` head is. Nothing about the bar moves — all three are owed, on the same head, and
> the orchestrator merges the two reports. What may **not** be split is the escalation: an agent that
> turns up a behavioural falsehood has not discharged its obligation by reporting it, and the cycle
> escalates on the spot exactly as it does today.

**What a refuter should press.**
1. The obligations are coupled — a claim the runner falsifies changes what the sweep must correct — so
   two agents may produce two half-answers where one produced a whole one. The escalation clause is
   the answer offered; it may not be enough.
2. Two agents on one checkout is the hazard Phase 2 already documents (:120-131, four concurrent agents
   producing a contaminated tree), and "one isolated, one not" is a new asymmetry that the shared-`~/.m2`
   ruling (KILLED P7, 2026-08-30, `REJECTED.md`:1015) says needs to know what maven does with a shared
   repository head —
   an inference about the world, not about the document.
3. It spends an agent to save wall clock on the cheapest cycle in the run, which is the opposite of
   where the measurement says the mass is.

---

## P3 · PARKED, no edit — the build count

17-37 full `mvn -o clean install` per run, 26-51 min, ~105 s each, every one of them on the serial
path. No edit is proposed and none should be, for three reasons found while drafting one:

- `harden`:17 already prescribes the scoped build (`mvn -pl api install`), and `pr-harden`:376 requires
  the root `clean install` *and says why* ("Not `-pl api`, not `-pl omod`: the omod unpacks…"). The
  instruction exists; the runs diverge from it. This ledger's own repeated finding is that
  instruction-is-not-the-lever.
- Telling agents not to `mvn install` is KILLED (P7) and its reopen wants "a substitute for the Phase 1
  build command", which this has not got.
- Whether dropping `clean` is safe here is an inference about maven's incremental correctness, which is
  exactly what a skill edit may not assert.

**The experiment that would settle it**, and it is cheap: on a quiet machine, with the pool stopped,
time `mvn -o clean install` against `mvn -o install` on this repo, n=3 each, after a touch of one
`api/` source file — and separately time a root build against `-pl api` for an api-only diff. If the
incremental build is both materially faster and green, the saving is a *habit* change with a measured
substitute; if not, the 26-51 min is the price of the discipline and this entry closes. `/measure-first`
is the skill for it.
