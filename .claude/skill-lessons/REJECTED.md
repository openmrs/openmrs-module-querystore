# Proposals a skill-retro refuter killed — do not re-propose without new evidence

Format: date · proposal · why it died · citation. A future retro that reaches the same idea should
read this first; re-running a settled objection un-cited is itself a defect (named by the 2026-08-24
refuter against P1 of that same retro).

## 2026-08-24 — all four proposals killed, nothing applied

**P1 · Relax pr-harden §6 step 4 ("never a server that was already running") to an attribution test,
deferring to a project's standing permission.**
Died: the current rule is MEASURED and stricter than attribution on purpose — the user's own server in
the cited near-miss WAS an attributable `java -jar openmrs-standalone`, which is why attribution alone
was found insufficient (pr-harden:313-315). "Never take a process you cannot attribute" already exists
separately (pr-harden:355-357), so the edit collapses two guards into one and removes one. And the
claimed contradiction with resolve-ticket does not exist — resolve-ticket:117 explicitly DEFERS
("the loop's own rule forbids a verifier taking a server it did not start"); the two sections answer
different questions (is the run blocked at pre-flight / what may an unattended verifier take).
Verified independently against both files. Also: "where the project records a standing permission,
that governs" would make an agent-written auto-memory authoritative over a safety guard.
**What remains true and is parked:** three runs (#298, #302, #268) each hand-carried an override into
the verifier brief. That is friction, not a wrong rule. Count: 3.

**P2 · Co-locate pr-harden's `git checkout -- <path>` hazard with the mutate-and-restore recipe.**
Died: the premise was false — the instruction (pr-harden:597-598) and the hazard (:600-605) are
consecutive paragraphs, verified. And both cited failures were the ORCHESTRATOR's own probe, not an
agent's, so briefing language would not have reached them; the orchestrator-facing rule already exists
and is the one that was violated (:595-597, "before your OWN measurement probe").
**What remains true and is parked:** two runs (#284, #268) lost uncommitted work to that command
AFTER the rule was written, correctly placed, in both an agent-facing and an orchestrator-facing form.
Prose did not prevent it twice. Any future remedy is probably mechanical, not textual. Count: 2.

**P3 · harden Termination: classify a cycle whose only edits correct the previous cycle's prose.**
Died on the walk-forward: applied to #298 it ends a CONVERGED run as did-not-converge at cycle 4 and
loses cycle 5's measured zero. It also restates two existing rules (harden:123-127 anti-pattern,
:132-139 documentation classification), overrides the "prose that IS behaviour is not documentation"
carve-out (:135-139), and its final clause licenses stopping with edits in the run, which :151-160
forbids without a cost exception.
**What remains true and is parked:** two runs spent cycles correcting the previous cycle's own prose;
in both, the EXISTING advice (delete rather than reword) is what converged it. Count: 2.

**P4 · Rename the run-record section to "blocked, contradicted, or under-served".**
Died: the heading was never what refused the content — "This is **capture, not derivation**: it
records what happened, never a proposed rule" (pr-harden:698-699, resolve-ticket:489-490) is, and
#298's homeless observation ended in proposed remedies. Bar not met either: only #298 raises it, as an
explicit question for skill-retro, and #268 then filed both shapes under the existing heading without
complaint. Count: 1.

## 2026-08-24 (second retro of the day) — all three proposals killed or parked, nothing applied

Records read: #298/PR301, #302/PR303, #284/PR304, #268/PR306, #269/PR307. Linter: 9 files, 0 findings.

**P1 · Change the prescribed restore idiom from `git checkout -- <path>` to a `cp` aside/back, and
subsume pr-harden's carve-out paragraph into the prescription.**
Died on four blocking objections, any one sufficient:
1. **It trades a measured silent revert for a measured silent revert.** A `cp` taken at mutation time
   is a snapshot of the file as the mutator READ it — remembered content in file form — so restoring
   from it reverts a concurrent edit exactly as the remembered copy did in the measurement the edited
   text carries (pr-harden:588-598: a reviewer "put the file back from its remembered copy, and
   reverted a guard the orchestrator had added in between… it surfaced only because a test written
   later failed"). The rationale "a file is not memory" answers the rule's wording, not its mechanism.
2. **It puts an untracked file in the repo, and `git status --porcelain` is the harden cycle gate's
   arithmetic** (harden:195). A `.premutation` an agent died before deleting scores as one edit, so a
   converged cycle reports edits>0 and the gate demands another full cycle; and `git diff | shasum`
   cannot see untracked paths at all, so it adds a residue class invisible to the one guard pr-harden
   says the rest of the skill actively needs (pr-harden:562-566, :538-540, harden:223).
3. **It is an alternative to committing, offered at the moment the reader skipped committing.** All
   five incidents are probes on files carrying uncommitted work, i.e. cases where the commit rule was
   not followed; #269's record says the fix was adopting "commit before probing", after which no third
   incident followed in that run (pr-harden:596-598, :604-605).
4. **Same reach objection that killed 2026-08-24 P2.** pr-harden:597 is agent-facing; the incidents
   are the ORCHESTRATOR's own probes. P1's prune would delete :600-605, the only clause that reaches
   the orchestrator, in order to fix an orchestrator defect.
Non-blocking, and they were my framing errors rather than the evidence's: "every one AFTER the hazard
was documented" is 4 of 5 — #302's own incident predates its documentation; harden:90-94 is ONE bullet
with the hazard FIRST, not a separate paragraph; and #268 files its incident as "Own error, not the
skill's".
**What remains true, with the running count: the HAZARD is 5 incidents across 4 records — #302, #284,
#268, #269 (twice) — 4 of them after documentation. The 4-record count corroborates the hazard, not
this remedy: only #302 proposes the cp backup, #284's own remedy is co-location (killed on a false
premise), #269's is the commit discipline. NEW EVIDENCE THAT WOULD REOPEN IT: a record showing an
incident where "commit before probing" WAS followed. That is a different proposal.**

**P2 · Tell an isolated-worktree agent which ref to review and make it confirm the diff is non-empty.**
Parked at count 1, as the proposal itself offered. Below all three limbs: one record; recorded cost is
"each agent a detour", no round or cycle; and it is not the self-contradiction limb, because nothing in
either skill asserts what ref a worktree opens on, so opening at origin/main falsifies no claim the
document makes. #302's two agent deaths "during worktree-isolation setup" are a different defect and
were correctly not counted. Also caught: the proposal quoted the #269 record for a string that is not
in it (the quote was from an agent's report) — the same defect #302's provenance note records a
refuter catching. Half the edit already exists on the pr-harden side (:116-121 mandates naming and
fetching the base explicitly); only harden's isolation bullet is bare.
**Count: 1. If a second run records the same detour, the harden-only clause is the right shape — quote
the record, and state the cost.**

**P3 · Add a "rule sound but suboptimal" section to the run-record template.**
Died: the count of 2 merged two DIFFERENT defects. #298's section is rule-sound-but-suboptimal;
#302's is "Under-captured at write time" — material that was in the run but not in the record when
written, which is late capture, not suboptimality. Real count: 1. And REJECTED P4 (same day) already
settled the underlying question: the heading is not what refuses this content, "capture, not
derivation" is (pr-harden:698, resolve-ticket:489) — and #268 filed exactly this shape under the
existing "Where a skill blocked or contradicted this run" heading without complaint. A dedicated
section also invites what that clause forbids: #298's own version ends in "Candidate remedies" and an
"Explicitly REJECTED candidate", i.e. derivation inside a record meant to be data.
**This answers the question #298 explicitly handed to skill-retro: the template does NOT grow a
section. File such an observation under "Where a skill blocked or contradicted this run", or invent a
local heading; either way capture-not-derivation governs what may go in it.**

## Running parked counts, carried so a future retro need not re-read the whole window
- `git checkout -- <path>` losing uncommitted work: **5 incidents / 4 records** (see P1 above).
- Prose-correction cycles — a cycle whose findings are all in the previous cycle's prose: **4 records**
  (#298 3 cycles, #302, #268 cycles 2-3, #269 Phase 2 passes 3-5). The classification remedy was
  killed (2026-08-24 P3) on a walk-forward that ended a converged run as did-not-converge. What
  converged all four is the advice already in both skills: delete rather than reword. No proposal.
- Verifier standing-permission friction — a run hand-carrying the override into the brief: **4
  records** (#298, #302, #268, #269). The relaxation was killed on a measured argument (2026-08-24 P1).
- Isolated-worktree agents opening at the base ref: **1 record** (#269).
- `main` moving under a run: **1 record** (#284, a 16-conflict manual merge at PR time).

## 2026-08-24 (third) — seven speed proposals, all killed, everything reverted

Not a retro over run records: a wall-clock measurement of four runs (#284, #268, #269, #298) proposed
seven edits to `resolve-ticket` 0.10.0, `harden` 0.14.0 and `pr-harden` 0.9.0, plus two new scripts.
Two refuters ran in one wave — one re-deriving every figure from the transcripts, one attacking the
edits as changes to a governing document. Six blocking objections from the text lens, six from the
evidence lens. All seven edits were reverted; both scripts were deleted; the surviving measurements
are in `2026-08-24-pipeline-timing-measurement.md`.

**P1 · Make Step 3's gate ONE wave of two parallel refuters (Q1,2,3,5 / Q4,6,7), with the re-gate
scoped to the revision.**
Died twice over. (a) Its justification was a universal — "the second wave's value in every record was
checking the REVISION, never re-reading the original plan" — and it is false in at least two of four
records: #268's pass 2 refuted the *original* plan's leg-1 exemption ("`gallium` kept 'Gallium citrate
ga-67' while renaming its two co-tied rivals"), and #298's pass 2 objected that the original "plan
MIS-CITES the rule it leans on". A re-gate scoped to the revision has no mandate to raise either.
(b) The design disabled the lens it created: the measurement lens is defined as the questions that
"ask what has actually been RUN", while the same edit forbade both agents to mutate the worktree —
where `harden`:99-101 says worktree isolation "is the only option that keeps all four able to run
code". The gate's best catches were live runs (#268 `findImpliedSubstances(…)` -> 3 substances; #269
`allergensMatching("opium")`). (c) It left abort condition 3's "second blocking objection" arithmetic
and the "no third gate pass" anti-pattern written for serial passes.
**What remains true and is parked:** the gate does cost two serial waves in every run (6+9, 11+15,
8+12, 5+10 min). A one-wave design would need worktree isolation for the measurement lens AND a
rewrite of abort condition 3. Count: 4 runs, but the obvious remedy is refuted.

**P2 · Bound every delegated agent (~10 min harden lens, ~15 min pr-harden role) and require a
`not_covered` residue list.**
Died on both lenses. Text: a bounded reviewer puts the loop's only exit condition under a clock —
nothing in `pr-harden` reads `not_covered`, `blocking == 0` exits, so a timed-out reviewer with an
empty findings array ends the run as converged, and `harden`:174 says "elapsed time … not termination
conditions … the rule has no cost exception". Its brief also says "run `pr-review` … Steps 1 through 3
in full", whose Convergence section is unbounded by construction. Evidence: the saving is 12-19% of a
run at the most generous arithmetic (16/43/46/56 min), and every finding the records CREDIT came from
an agent that ran past the proposed bound — #298's r1 (21.6 min) and its only blocking finding
(19.4 min), #269's real coverage gap (19.7 min).
**What remains true and is parked: the zero-overlap wave chain — 70-171 min idle per run with the
orchestrator using 2-12 of it. Count: 4 runs. A remedy has to keep the exit condition off the clock.
NEW EVIDENCE THAT WOULD REOPEN IT: a run where a finding's arrival time inside an agent is
recoverable, which these transcripts do not carry.**

**P3 · Let mechanical agents take a cheaper `model`; never the gate, reviewer, fixer or verifier.**
Died: the category has no instance. Of 55 spawns across four runs, 54 are refute/review/fix/verify and
the one exception fired 62 min after PR-ready inside skill-retro — mechanical agents are 0.0 of 733
minutes of agent latency. Its own canonical example is documented as judgement, not mechanics: "count
the homes of a phrase" is what #302 records failing four times running. The supporting measurement
("every spawn left `model` unset") is exactly true and distinguishes nothing.

**P4 · `hstate`, one installed command for both state files, replacing the hand-written writes.**
Died on the mechanism, not the idea. `await`/`clear` refreshed the HARDEN file's `ts` from phases where
no harden run exists, which pushes a wedged `edits > 0` entry out another six hours indefinitely and
defeats the one documented cure for that wedge (resolve-ticket:488-492, "cleared only by the 6-hour
expiry"). `drop` cleared both files, so a `--plan-only` terminus would erase an unrelated harden run's
cycle debt. No subcommand wrote `reviewed_shas` or `declined`, while the dependency table claimed it
covered "every state write". `edits_now()` counts ALL unpushed commits and silently scores 0 when the
branch has no upstream — the state at Step 7. `clear` was all-or-nothing, so with two agents live,
clearing on the first result re-opens the gate (found in use, during this pass). And the corroboration
merged two different defects — #298 had NO `awaiting` field at all, #302 had it and wrote one file —
the same merge REJECTED 2026-08-24 P3 was killed for. Real count: 1. The three runs after #302 already
wrote both files by hand (9, 10 and 12 calls naming both), and no gate has fired since.

**P5 · `claim-lint` at every harden cycle close and over the PR body.**
Died on its own target. Run against the false claims the four records quote, it catches **zero** —
including "145 containment-only pairs" -> 143, the single genuine stale tally in the corpus, missed
because the hyphenated compound eats its one-word window. Most of the corpus's false claims are
universals, which the tool deliberately does not check, so the four-record provenance it cited
(#298, #302, #268, #269) attributes to it a class it excludes. Its default ref reads uncommitted work
only — empty at cycle close precisely when `harden`'s own commit-first discipline is followed. Its
calibration did not reproduce (8+ findings per PR, not 2-3; the "1410 before scoping" is not
reproducible under any reading). Not the classification remedy killed on 2026-08-24 P3 — this one
reports and never blocks — but it re-used that entry's parked count without its caveat.

**P6 · Prefer the `Edit` tool over a hand-written replace script for a targeted replacement.**
Died: unfollowable and misattributed. The unattended runs execute under bypass-permissions mode, whose
standing instruction is "make file changes with sed, heredocs, or short scripts, rather than using the
dedicated Read, Edit, or Write tools" — dispositive against a skill's preference every time. And the
"25 minutes" was the BUILD: of #298's 92 replace-shaped calls, 35 run `mvn` in the same call and carry
23.5 of the 24.1 minutes; the 32 pure replacements execute in 4.3 seconds total. It also left
`pr-harden`'s own premise standing three lines above it ("Every role here edits files by running a
short script rather than by hand").
**What remains true and is parked:** 18.5-21.1 min and 128k-179k output tokens per run are spent
GENERATING those scripts. That is a real cost with no remedy yet proposed. Count: 4 runs.

**P0/P7 · Publishing a four-run baseline table inside `resolve-ticket`, and the set's net +235/-91
lines with no pruning.**
Died: the table is the defect these skills forbid, in the document that forbids it — skill-retro:87
("Do not add a count a later reader must re-measure"), resolve-ticket:639, harden:265 — and its
figures were wrong on day one: the build share was out by 3-5x, the turn/token/Bash ranges were
measured over a different window than the durations beside them, three of four rows did not sum to
their own totals, and one figure was mislabelled. Its escape hatch ("this command reproduces every
figure") pointed at a script that existed on one machine and was in no install block. On the set:
skill-retro Step 4 requires each addition to name what it retires, and the anti-pattern names the
threshold ("+200 lines has moved the problem"); the same timing measurement ended up in four homes,
manufacturing the multi-home-claim hazard both skills document.

## Running parked counts, carried so a future retro need not re-read the whole window
- Zero-overlap agent waves — 70-171 min idle per run, 2-12 of it used: **4 runs** (P2 above).
- Generation spent emitting edit scripts — 18.5-21.1 min, 128k-179k tokens per run: **4 runs** (P6).
- Build share of a run, 5-16%, previously believed 1-3%: **4 runs**, no remedy proposed.
- The gate's two serial waves: **4 runs**; the one-wave remedy is refuted (P1).

## 2026-08-25 — one applied (revised), one parked

Records read: #284/PR304, #268/PR306, #269/PR307, the pipeline-timing measurement, #250/PR311.
Linter: 9 files, 0 findings.

**APPLIED (revised) · harden 0.14.0 — "If you isolate, do not assume the worktree is on the branch
under work."** Proposed first as a diagnosis ("an isolated worktree does not open on the branch you
are hardening") and revised to the guard form on three blocking objections, all cited:
1. The #250 citation misstated its own record — "three agents, one needed the flag" where the record
   says two fixers and BOTH used it — and carried #269's facts (opened at origin/main, empty diff)
   onto #250. Same provenance defect this ledger records a refuter catching at 2026-08-24 (second) P2.
   Remedied by amending the #250 record with flagged after-the-fact capture, as #269's record did.
2. "Would the edit have prevented the thing it cites?" was NO for the second sighting: #250's costs
   are write-side (git refusing the checkout, a diff stranded in the agent's worktree, `git branch -D`
   failing at FINISH), and naming a ref to READ prevents none of them.
3. The two records give two different CAUSES for one symptom — #269 the tool seeding from the base
   ref, #250 git refusing a branch already checked out elsewhere — so the diagnosis was an unchecked
   universal, the grammar Step 4 forbids. The guard covers both without naming a mechanism.
Non-blocking and also applied: placement moved to after the "say it in every brief" clause rather than
wedged before it, and #269's own wording ("all six", not "six lenses") restored.
**Prune/growth (Step 4)**: +9 lines, subsuming and retiring nothing. Justified as the smallest form
that closes a detour two records now report, replacing per-agent improvisation. Declared near-duplicate:
pr-harden:116-121 already mandates naming the base ref in the reviewer's brief — a different mechanism
(a stale local `main` vs the worktree's ref), so two homes are defensible, and this is said out loud
because a previous retro was faulted for one claim reaching four homes.

**PARKED at count 1 · resolve-ticket Step 8 — a negated closing keyword still closes.** The sentence
written to explain a `Refs` ("It does not close #250") put GitHub's `close` keyword before the
reference and populated `closingIssuesReferences`; rewording emptied it. Died on two blocking
objections: (1) corroboration was ZERO, not one — a grep of all seven records for
`closingIssues|Refs #|Fixes #|does not close|stays open` returned nothing, so the proposal cited the
retro's memory of the run rather than the record; (2) the self-contradiction limb does not apply on
its own terms, since the contradiction runs through GitHub's parser and one contingent wording choice,
both facts about the world, and nothing in resolve-ticket asserts a negated keyword is inert. Recorded
cost was zero rounds — the run's own check caught it, which is itself evidence that the existing text
plus ordinary care sufficed. **The incident is now captured in the #250 record, flagged as
after-the-fact capture, so the count is 1 honestly. NEW EVIDENCE THAT WOULD REOPEN IT: a second
sighting, or one that costs a round. If admitted then, admit only the mechanical check — "after any
body edit, check `gh pr view <n> --json closingIssuesReferences`" — which is self-verifying and
cause-agnostic; not the keyword list, which is a world fact a skill would have to keep true.**

## Running parked counts, carried so a future retro need not re-read the whole window
- `git checkout -- <path>` losing uncommitted work: **7 incidents / 5 records** (#302, #284, #268,
  #269 x2, #250 x2). Both #250 incidents were probes on files carrying uncommitted work, i.e. the
  commit rule not followed, so they corroborate the HAZARD and not any remedy. The reopening
  condition set at 2026-08-24 (second) P1 is unchanged and still unmet: a record showing an incident
  where "commit before probing" WAS followed. Worth noting for whoever meets it — #250's second
  incident came roughly forty minutes after that run had itself written a commit message about the
  hazard, which is evidence about documentation's reach rather than about a new remedy.
- Prose-correction cycles: **5 records** (#298, #302, #268, #269, #250 cycles 2-4). Remedy killed at
  2026-08-24 P3; what converges them is the advice both skills already give — delete rather than reword.
- Verifier standing-permission friction: **5 records** (#298, #302, #268, #269, #250). Relaxation
  killed at 2026-08-24 P1 on a measured argument; #250 hand-carried the override into the brief at
  zero cost, as #268 did.
- Isolated-worktree agents not on the branch under work: **2 records** (#269, #250) — APPLIED above.
- `main` moving under a run: **1 record** (#284).
- A negated closing keyword still closing: **1 record** (#250) — parked above.
- A measurement whose INPUT POPULATION cannot express the counterexample: **2 records — APPLIED**
  (harden 0.15.0). Parked at 1 earlier the same day on "waits on a second sighting"; the second was
  already in the corpus and the first draft failed to cite it. #250: an adversarial sweep took each
  row's display name as the recorded order, a population where no two rows of a family can tie above
  rank 0, and reported clean (cost 1 round). #268: the sizing the TICKET offered, "0 of 36 reachable",
  measured a display-name population while the rule turned on a leg that ties on a name that is no
  row's display name (cost 0 rounds — caught at the gate). The merge is declared: the two differ in
  WHOSE measurement it was, which is why the applied clause says "need not be a measurement you wrote";
  #298's "the sweep sees the regression this arrangement exists for" was examined and NOT counted, being
  the fixture/arrangement form the existing sentence already covers. Bar limb: two-or-more-records only —
  the cost limb is not claimed. Two blocking objections shaped it: "widening is not adding" was ruled
  special pleading with no textual hook in Step 3, and the first draft's "a reviewer found it by drawing
  from the rows' shared aliases" was in NO record and spliced a method from one item onto the result of
  another whose alias sweep found nothing — the third provenance slip caught in this retro cycle.

## 2026-08-25 (second pass, after "is that the only lesson?" was asked twice) — three parked, nothing applied

**PARKED at 4 cycles / 3 records · skill-retro Step 3 — the proposer does not verify its own citations.**
Shapes seen: a record quoted for a string that is in an agent's report and not in it (#302's retro,
2026-08-24's retro against #269); a record's own content misstated (#250: "three agents, one needed the
flag" where the record says two fixers and both used it); one record's facts carried onto another
(#269's origin/main and empty diff onto #250; a method from one item onto the result of another); and a
fact in no record at all, cited to "the run" (the closing-keyword item; grep returned nothing).
Recorded at #302:39-41, #269:43-45, #250:57-61 — all as after-the-fact amendments written by the
offending retro, so the corroboration is self-reported by the process that committed the defect.
Cost so far: **zero rules shipped on a bad citation** — the refuter caught 4 of 4, and the one that
reached "applied" was revised inside its cycle, its remedy being an amended record.
Not applied because (a) Step 5:96 already owns "does the record actually say this?", and a second home
for one mechanism is the multi-home hazard this ledger names; (b) design rationale 1-2 measured that
self-retrospection misses this class, which is the argument for leaving the check adversarial; (c) it is
the `git checkout --` shape — every proposer had read the records minutes earlier, so instruction is not
the lever, and that hazard stands at 7/5 with no text applied on "probably mechanical, not textual".
The submitted wording was independently unacceptable: "caught only at Step 5 so far" uses grammar
Step 4:87-88 forbids and is false (the closing-keyword incident was caught inside the run), and "two
shapes recur" is refuted by the third shape above.
**NEW EVIDENCE THAT WOULD REOPEN IT: a rule that SHIPS on a citation the record does not carry, or one
that costs a round. If admitted then, admit only the mechanical form — a `skill-lint.py` check that
every quoted string in a proposal occurs in the file it is attributed to — and note that even that
passes the third shape, a correct quotation attached to the wrong record.**

**PARKED at 1 · pr-harden is silent on where the FIXER works.** Killed as a cross-skill inconsistency:
harden's rule is CONDITIONED on concurrency (":89-100, four PARALLEL agents on one checkout"), while
pr-harden spawns ONE fixer per round and #250's two were in different rounds, i.e. serial — so harden's
own first branch, "license exactly one agent to mutate", was already satisfied and 0.14.0/0.15.0 never
told pr-harden to isolate a lone fixer. The #250 orchestrator over-applied the rule; that is a run
decision, not a document conflict. The self-contradiction limb is also intra-skill by its own wording,
and the one previous cross-skill claim was verified against both files and killed.

**PARKED at 0 · the verifier cannot trust `omod/target`.** Killed on provenance, and the submission
committed the very defect its sibling proposal was about: a grep of all seven records for
`md5|expanded|rebuilt|wrong bytes|merging head` returns one line, #250:21, which says the OPPOSITE of
the framing — it credits the EXISTING freshness check with catching it. "The verifier rebuilt on its own
initiative", "both were the wrong bytes" and the md5-against-the-expanded-jar comparison live only in
the retro's memory of the run. Also not the self-contradiction limb: nothing in pr-harden asserts the
step-2 build equals the round's head, so it is a GAP, and the "artifact can predate the head" reading
needs an extra premise about a mutation probe rebuilding `api/target`, which is an inference about the
world. One real defect did come out of it and was fixed in the record rather than the skill: #250:21
called it "step 4's timestamp check" when pr-harden's freshness check is step 5 and step 4 is Restart.

## Running parked counts (superseding the previous block where they differ)
- `git checkout -- <path>` losing uncommitted work: **7 incidents / 5 records** — unchanged, remedies killed.
- Prose-correction cycles: **5 records** — remedy killed.
- Verifier standing-permission friction: **5 records** — relaxation killed.
- A measurement whose INPUT POPULATION cannot express the counterexample: **APPLIED**, harden 0.15.0.
- Isolated-worktree agents not on the branch under work: **APPLIED**, harden 0.14.0.
- The proposer not verifying its own citations: **4 cycles / 3 records** — parked above.
- A negated closing keyword still closing: **1 record** (#250).
- pr-harden silent on where the fixer works: **1 record** (#250) — parked above, cross-skill.
- The verifier trusting `omod/target`: **0 records** — parked above, provenance.
- `main` moving under a run: **1 record** (#284).
- `git branch -D` blocked by an agent worktree holding the ref: **1 record** (#250).


## 2026-08-25 (third pass, one-record window after #308) — one proposal killed, nothing applied

**KILLED · resolve-ticket Step 2: "a plan whose success criterion is that two computed outputs AGREE
must enumerate the UNITS each is computed over."** Five blocking objections, the first sufficient on
its own:

1. **The edit would not have prevented what it cites.** The prescribed action WAS performed and
   produced a false answer: ADR 44's own justification stated a unit for the record channel
   ("a record is rendered per ROW") and stated it wrongly, and the fold was placed on a unit the author
   had named. A bullet saying "enumerate the units" is satisfied verbatim by the statement that cost
   review round 1 and harden cycle 2. What was missing was VERIFYING the enumeration against code that
   was already there to read — a different rule, and not the one proposed.
2. **The load-bearing claim is falsified by the record's own fifth axis.** "Every one was readable in
   both call paths at plan time" is false for the trim axis, which was established by RUNNING a
   semantically-equivalent rewrite against the whole build. "Every one" is also the universal grammar
   Step 4:87-88 forbids — the second consecutive retro to submit a wording faulted for it.
3. **Four axes, not five.** The record states the number as four (collapsed key, row, subject-matter
   gate, clause text). The trim row is a test-COVERAGE gap, not a unit the channels are computed over,
   and enumerating units cannot surface "no case pins this"; the gate row is a gate, by the proposal's
   own words.
4. **The published cost figure was wrong in the direction that flattered the proposal.** "Five review
   rounds and two harden cycles" against a table naming three rounds and two cycles. The cost limb of
   the bar is still met (2 cycles + 3 rounds), so the defect is the figure, which Step 4:87 forbids
   adding.
5. **The prune justification misreads the host.** "Step 3's question 5 already provides the gate" is
   false — question 5 asks whether the SCOPE matches the ticket. No Step 3 question asks whether two
   computed outputs share their units, so the bullet would ship into a growing document with no gate
   reading it.

Non-blocking and also true: "each found by a different fresh agent" is not in the record (two findings
carry the same `[c3]` tag), and the header miscounted the corpus as eight older records where there are
seven.
**NEW EVIDENCE THAT WOULD REOPEN IT: an incident where the divergence axes were NOT visible in the
call paths at plan time, or a second record of the same shape. If readmitted, the rule to test is
"verify a stated unit against the code that computes it", NOT "enumerate the units" — the enumeration
was done and was wrong.**

**PROCESS DEFECT FOUND BY THE REFUTER, recorded because it is the fourth pass to hit it.** Four of the
five parked items in this pass cited facts present in NO run record — the `cp`-to-scratchpad idiom, the
prose-correction cycles, the moving review target, and the stray database process — while using them to
carry counts and, in one case, to partly discharge a kill condition from 2026-08-24 (second) P1. That is
the shape parked at 4 cycles / 3 records ("a fact in no record at all, cited to the run"). Remedied the
way #250 and #269 remedied theirs: the #308 record now carries a clearly-flagged **After-the-fact
capture** section, and the counts below are stated against it. The parked entry for that defect moves to
**5 cycles / 4 records**, and its own reopen condition is unchanged — still zero rules shipped on a bad
citation, because the refuter caught these before anything was applied.

## Running parked counts (superseding the previous block where they differ)
- `git checkout -- <path>` losing uncommitted work: **8 incidents / 6 records** — remedies stay killed.
  #308's incident is the same shape already analysed: a probe on a file carrying uncommitted work, i.e.
  the commit rule not followed. The scratchpad-located `cp` idiom the run adopted answers 2026-08-24
  (second) P1's objection 2 (an untracked file inside the repo corrupting `git status --porcelain`, the
  harden cycle gate's arithmetic) but leaves objections 1, 3 and 4 standing. **Reopen only on an
  incident where the commit rule WAS followed and the idiom still lost work.**
- Prose-correction cycles: **6 records** — remedy killed; deletion-over-rewording is what the run applied.
- Verifier standing-permission friction: **5 records** — relaxation killed.
- The proposer not verifying its own citations: **5 cycles / 4 records** — parked above.
- A negated closing keyword still closing: **1 record** (#250).
- pr-harden silent on where the fixer works: **1 record** (#250) — cross-skill, killed.
- The verifier trusting `omod/target`: **0 records** — provenance.
- `main` moving under a run: **1 record** (#284).
- A delegated agent's REVIEW TARGET moving under it: **1 record** (#308, amended capture). `pr-harden`
  pins each round to an immutable fetched ref; `harden` Phase 2 points its agents at the live branch the
  orchestrator commits to. Not proposed: one record, and the cross-skill limb was killed once as "a run
  decision, not a document conflict".
- The verifier's stray database process holding a datadir lock: **0 records as a SKILL gap** (#308,
  amended capture, corrected same day). The incident is real, but the trap — the orphaned
  `database/bin/mariadbd` keeping the datadir lock, its exact `Can't lock aria control file` signature
  and the `pkill -9 -f "database/bin/mariadbd"` fix — is ALREADY documented in this project's own
  memory, and the run simply did not consult it. So the remedy is not a `pr-harden` rule: the
  information existed and was ignored, which is the same "instruction is not the lever" shape as the
  `git checkout --` hazard. Recorded here so a later retro does not read it as a missing rule.
- Raising the round cap when every round finds a DIFFERENT defect: **1 record** (#308). Saved rounds
  rather than costing them, so below the bar on both limbs.
- `git branch -D` blocked by an agent worktree holding the ref: **1 record** (#250).

## 2026-08-26 (window: #315 + the #310 driver capture) — one killed, four applied, one of those promoted by the refuter

**KILLED · skill-retro: "the single-record corroboration bar contradicts its own anti-pattern."** Filed
2026-08-26 as `proposals/2026-08-26-skill-retro-single-record-bar.md`, refuted the same day. Three
blocking objections, each settling on its own:

1. **The Step 1 citation was an ellipsis that removed the deciding clause.** Full text (skill-retro
   Step 1): "a retro over one record **is a retro that cannot corroborate anything**, and should say so
   rather than proceed as if it could." The proposal quoted it as "a retro over one record… should say
   so rather than proceed as if it could" and glossed that as "say so and proceed, not stop". Read
   whole, Step 1 reads WITH the anti-pattern.
2. **The two rules have different subjects, so clause 3 was never available.** Step 3's clauses govern
   how often a LESSON appears; the anti-pattern governs whether a PASS is worth running. Obeying the
   anti-pattern violates nothing in Step 3 — its single-record clauses simply go unexercised. That is
   unstated PRECEDENCE, not a document contradicting itself, and clause 3 was the only limb claimed.
   `ticket-pool`:153-154 already reads it as a pass-level gate: "The threshold defaults to 2 because
   `skill-retro`'s own anti-pattern says a single record cannot corroborate anything."
3. **"Retires nothing" was false.** Scoping the anti-pattern renders `ticket-pool`:153-154's stated
   reason stale, and Step 4 requires naming that.

**The deciding check the proposal said had not been run WAS run, and its answer is worth keeping** so a
later pass need not re-derive it. Rules shipped from a single run's evidence: `fba95ab` ("Five lessons
from run seven"), `11f920d` ("a run that took issue #299 to a ready PR"), `89db0c4` ("each from a
failure on the run that produced #295"), `a8afea5` ("running resolve-ticket end to end on
chartsearchai#290"); and `b06d6a8`'s pre-commit branch check, shipped from one run, is what #308 records
catching — "the round's edits landed there. The pre-commit branch check caught it… Cost: none, because
the check exists." So the bar's single-record clauses are load-bearing rather than decorative. **NEW
EVIDENCE THAT WOULD REOPEN IT: a pass that stopped on the anti-pattern while holding a lesson clause 2
or 3 admits, and lost it. If readmitted, the edit to test is a PRECEDENCE clause plus the matching
correction at `ticket-pool`:153-154 — not the scope change that was filed.**

**Applied, after revision forced by the refuter** (all four in `pr-harden` 0.10.0 / `harden` 0.17.0):

- **Raising the default round cap** (2 records: #308, #315). Two blocking objections revised it rather
  than killing it: the submitted wording carried "twice it has been the right one", the tally defect
  `REJECTED.md` has now faulted three consecutive retros for, so the runs are named instead; and the
  edit licensed an UNBOUNDED raise, against `ticket-pool`:135/:187/:202-204 — a session that outruns
  `ticket.timeout_seconds` is killed, "which leaves that checkout dirty and every remaining ticket
  skipped", so a raise taken to avoid a labelled `draft` can stall a pool. The rule now raises only the
  DEFAULT cap, a round or two at a time, and never a cap the caller set.
- **Widening the ADD-a-guard mutation obligation off "text or shape."** The corroboration was REPLACED
  by the refuter. As filed it rested on #315's `[r4]/[r5]/[r6]`, and the objection settles: those are a
  missing condition, a missing companion action and a non-blocking position defect — mutation asks
  whether a guard is PINNED, and no mutation of what was written reddens a case never authored. That is
  the shape that killed 2026-08-25 (third) P1. The refuter supplied the citation that does hold: #308's
  `[r4]` "The fold's own matched-rules guard was unpinned; deleting it left the whole suite green ·
  blocking · cost: 1 round" and `[r5]` "The trim normalisation was unpinned against a
  semantically-equivalent rewrite · blocking · cost: 1 round" — both raised in pr-harden ROUNDS, both
  outside the old scope. Also cut from the submitted wording: "each arrangement passed everything",
  exhaustive-characterization grammar.
- **A repeat is evidence only where something between the repeats is reset** (#315, 2 rounds). Revised:
  "every figure from five cycles" dropped the record's word "prompt" and shipped the universal grammar —
  restored; and "n repeats are one sample" was an absolute the evidence does not reach, replaced by what
  it does reach (the repeats never exercised the path a first run takes). The refuter read
  `LocalLlmEngine`'s javadoc rather than trusting the record, and confirmed the mechanism.
- **PROMOTED BY THE REFUTER, from this pass's own parked list:** `pr-harden`:60 told a run to "report it
  and ask before clearing it" about a stale state entry, while the same file settles at :310-311 that
  "'Confirm with the user…' is not available to an unattended verifier, so the rule cannot be that."
  Intra-file, clause 3, no altitude argument needed — and the 310 driver capture is the occurrence
  ("pr-harden-state.json: phase=reviewed blocking=0 … pr=313 round=6" on a run working ticket 310). The
  pass had parked it on a FREQUENCY argument ("only reachable by a hand-launched unattended run"), which
  clause 3 does not ask for and which is itself the *only*-grammar Step 4 flags. Recorded because the
  refuter finding a proposal the proposer missed is design rationale 2 working.

**Pruned:** the eleven-ref enumeration at `pr-harden`:425. Retired by measurement — in the checkout
those runs happened in, `git branch --list 'pr-*'` now returns `pr-286` alone. The refuter killed the
proposal's second evidence claim: querystore's `pr-1/34/63/68` are NOT that class (reflogs show
`pull/<n>/head:pr-<n>`, created before the FINISH rule; `git reflog --all | grep -cE 'pr-[0-9]+-r[0-9]'`
returns 0 there), and the glob also matches `pr-harden-review-loop`. Dropped, and "so the FINISH rule
works" dropped with it as an unchecked causal claim.

**Net growth: +29 lines** across two files, against one enumeration deleted. The justifying sentence:
three of the four are clauses appended to lines that already exist rather than new homes, the fourth
closes an intra-file contradiction and so reduces the number of rules that disagree, and the retro that
proposed them is the first to add none of its own count to be re-measured.

## Running parked counts (superseding the previous block where they differ)
- `git checkout -- <path>` losing uncommitted work: **8 incidents / 6 records** — unchanged; remedies
  stay killed. Reopen only on an incident where the commit rule WAS followed and the idiom still lost work.
- **The scratchpad — the adopted remedy for that hazard — is itself an unguarded shared surface:
  1 record** (#315: "A subagent overwrote a helper script in the shared scratchpad (same filename,
  different signature), silently breaking a later measurement"), no round cost stated. #308's amended
  capture is what makes it worth watching: the `cp`-to-scratchpad idiom was adopted there precisely
  BECAUSE `git checkout --` lost work. A gap rather than a contradiction, which is the shape this ledger
  has killed before. Reopen on a second incident, or one that costs a round.
- Prose-correction cycles: **7 records** — #315 adds "three successive overstated claims in one ADR
  entry, each narrowed by the entry's own data; a FOURTH home of a false attribution after a round
  claimed all three were fixed". Remedy still killed; deletion-over-rewording is the shipped rule and
  this is the second record of it being violated after it shipped.
- Verifier standing-permission friction: **5 records** — relaxation killed.
- The proposer not verifying its own citations: **6 cycles / 5 records** — this pass added two (the Step 1
  ellipsis in the killed proposal, and the #315 corroboration that did not reach its own remedy). Still
  zero rules shipped on a bad citation; the gate caught both.
- A retro submitting wording that breaks the counts/universals rule it is codifying: **3 consecutive
  retros**. This pass shipped three such phrasings into Step 5 ("twice it has been the right one",
  "passed everything", "every figure from five cycles") and the refuter cut all three. Worth a rule only
  if one ever survives the gate; the gate is currently the mechanism.
- Raising the round cap when the loop is demonstrably converging: **APPLIED** 2026-08-26 (2 records).
- A fix applied to one member of a script family, its sibling left fail-open: **1 record** (#315 `[r6]`,
  non-blocking). `pr-harden`'s "find every home" section is about CLAIMS; this is code.
- "Don't widen scope" vs "never commit a known regression": **1 record** (#315). Second limb is a project
  `CLAUDE.md`, so clause 3 (a skill contradicting ITSELF or its own gate script) does not reach it.
- A record whose outcome is "converged, deliverable inverted" and has no template vocabulary:
  **1 record** (#315). REJECTED 2026-08-24 P4/P3 settled that the template does not grow for one sighting.
- A run that writes no record at all: **1 record** (#310). `ticket-pool` 0.6.0's driver capture is the
  shipped remedy and this is its first exercise — it worked.
- An adjacent product defect noticed and not fixed, with nowhere durable to go: **0 records.** The
  proposal is parked, not killed; both pool runs have now banked and neither names an instance.
- **New, from the refuter's own measurement and in no run record: 37 `worktree-agent-*` branches and 9
  registered worktrees in the chartsearchai checkout** — a residue class an order of magnitude larger
  than the `pr-*` refs the FINISH rule cleans, reached by no rule in these skills. **0 records /
  1 measurement.** Banked so a later pass need not re-derive it; not proposable on a measurement alone.


## 2026-08-27 (window: 1 new record, #317 / PR 318) — one applied, three parked; linter 10 files, 0 findings

**APPLIED (revised) · resolve-ticket 0.12.0 — "Check the field rather than the wording, with
`gh pr view <n> --json closingIssuesReferences`, once the body is written and again after any later
edit."** PARKED at count 1 on 2026-08-25 with the reopening condition *"a second sighting, or one that
costs a round"*; #317 is the second sighting, readmitted in exactly the form that entry
pre-constrained — the mechanical check alone, no keyword list, since a keyword list is a world fact a
skill would have to keep true.

Revised on two blocking objections, both citing the record against the proposal:
1. The draft said the fix "emptied" the field and that *only* splitting the references onto separate
   lines did it. Both false. The field was never emptied and must not be — PR 318 legitimately closes
   #317 and the field correctly names it; what was removed was 315. And #317's record says the remedy
   was splitting the lines AND dropping the keyword for 315, which the shipped body confirms. As
   drafted the rule would have taught line-splitting-alone as the remedy and an empty field as the
   success signal.
2. The prune claim ("nothing stale in that paragraph") was refuted by the proposal's own evidence:
   Step 8 stated flatly that *"the cost of `Refs` is that `closingIssuesReferences` comes back
   **empty**"*, and #317 is a body carrying `Refs #315` whose field named 315. **That universal was
   narrowed in the same edit** — "comes back empty for that ticket — unless a closing keyword elsewhere
   in the body reaches it anyway" — so the addition retires a false absolute instead of sitting beside
   one.
Non-blocking, also applied: the count and the "only" were removed per Step 4's own grammar rule, which
this ledger records three consecutive retros breaking; "the two records disagree about the cause" was
corrected to what they show (same cause — a closing keyword whose scope reached an adjacent reference —
different remedy); and the claim to prevent an observed loss was dropped, since both runs caught it
themselves at zero recorded round cost. What the rule buys is repeatability of a practice that has
worked twice, plus cover for the run where nobody looks.
**Prune/growth (Step 4)**: +9 lines, and one false universal narrowed directly above them.

**PARKED · `git checkout -- <path>` losing uncommitted work.** #317 adds one incident: the
ORCHESTRATOR's own mutation probe, undone on a file carrying the uncommitted regression fix it was
verifying, reverted the fix; the empty `git status` read as success and a commit shipped whose message
described changes absent from its diff. **Remedies stay killed** — the reopening condition (an incident
where "commit before probing" WAS followed) is still unmet, since the probe was run on a file carrying
uncommitted work. Two corrections the refuter made, kept as method: the running figure was 8 incidents
/ **6** records and the draft's enumeration silently dropped #308; and a line dating the incident
"roughly two hours after this run had read the rule" appears in no record — orchestrator memory, the
provenance defect that killed the closing-keyword proposal at count 1.

**PARKED at count 1 · the one-hour await bound is shorter than a legitimate phase.** #317's round-1
fixer ran ~85 minutes on a five-wording standalone A/B (each arm a rebuild, redeploy, restart and
interleaved capture); the gate fired at 79 minutes, a liveness ping established the agent was alive,
and it returned a complete result at zero recorded round cost. Below the bar, and the
self-contradiction limb does not reach it — pr-harden calls the bound a backstop, consistent with its
gate. **Correction kept**: the draft said following the skill literally "would have killed a live
agent"; past `AWAIT_TTL` the gate instructs nothing of the sort — it blocks the yield with "continue
the loop" — and the dead-agent contract is triggered by a terminal outcome the harness REPORTS, not by
the clock. REOPEN ON: a second record of a phase legitimately exceeding the bound, or one where the
dead-agent contract was followed and destroyed live work. Prefer then "establish liveness before
treating a past-bound agent as dead" over changing the bound.

**PARKED at count 1 · extracting the assembled prompt from llama-server's KV slot.** #317's finish
verifier obtained the prompt the model actually ingested by saving the KV slot mid-generation and
detokenizing it, where the audit table, the wire and the logs carry none. Below the bar, and a
technique rather than a rule; writing a llama.cpp detail into a skill is the world-fact objection that
killed the keyword list. **Correction kept**: the endpoint names, the header parsing and "succeeded
first try" are not in the record, which says only that the slot was saved mid-generation and
detokenized. REOPEN ON: a second run needing prompt-level evidence, or one reporting "could not
determine" for want of it.

## Running parked counts (superseding the previous block where they differ)
- `git checkout -- <path>` losing uncommitted work: **9 incidents / 7 records** (#302, #284, #268,
  #269 x2, #250 x2, #308, #317). Remedies stay killed; reopen only on an incident where the commit rule
  WAS followed and the idiom still lost work.
- A negated or adjacent closing keyword populating `closingIssuesReferences`: **2 records** (#250,
  #317) — **APPLIED** above as the field check.
- The one-hour await bound shorter than a legitimate phase: **1 record** (#317) — parked above.
- Extracting the assembled prompt from the inference server: **1 record** (#317) — parked above.
- Every other count in the 2026-08-26 block stands unchanged; this window's single record touched none
  of them.

## 2026-08-27 (second window: 1 new record, #315 / PR 321) — two applied, one applied-with-its-reason-refuted, two parked; linter 10 files, 0 findings

Proposals filed as `proposals/2026-08-27-retro-window-315-pr321.md`. The refuter revised two, refuted
the stated reason of a third while accepting its edit, and **PROMOTED one the proposer had parked** —
design rationale 2 working for the second retro running.

**APPLIED (revised) · `harden` 0.19.0 — "of a PASSING check, ask what it actually examined."** Two
records: `2026-08-26-...-315.md` `[r4]` "round 3's CAPTURE_DONE fix wrote the marker unconditionally: an
arm that captured nothing read as a clean, empty A/B, exit 0 · blocking" and `2026-08-27-...-315.md`
`[harden c3]` "`ArchitectureGuardTest` passed 5/5 on a wrong source root — it WALKS, so it scanned
nothing and reported no violations · 1 cycle" with `[c4]` "the fifth walks its own directory and returned
silently. Then: existence alone was not equivalent to the canary, because the sibling omod module
carries the same package path · 2 cycles". Appended to the paragraph that already asks the same question
of an input population and of stable repeats; what it adds is the mechanical remedy that limb lacked.
Three revisions the refuter forced, all kept: the two sightings are **two runs on one ticket**, weaker
independence than two tickets, and the text now says "Two runs of #315" rather than implying two
independent ones; "returns the same clean result" softened to "can return"; and "what **only** the
intended root holds" replaced by "so that a sibling could not supply it", since `only` is named in
`harden`:274's own list and the record shows this is the hard part, not a safe absolute. Noted for a
later pass: `[r4]`'s use by the 2026-08-26 window was killed at :470-476 as "a missing condition"; that
ruling is P1's own reading and does not block it, and `[r4]`'s lesson was unclaimed until now.

**APPLIED (promoted by the refuter, from this pass's own parked list) · `harden` 0.19.0 — "where the
guard is over TEXT, mutate the SUBJECT too."** The proposer parked this at count 1 on an "adopted
precedence" it read out of this ledger; the refuter read the ledger back and settled it: :429-459 says
the anti-pattern governs "whether a PASS is worth running" and that obeying it "violates nothing in
Step 3 — its single-record clauses simply go unexercised", and lists four commits shipped from one run's
evidence. **This pass did not stop**, so the anti-pattern was never in play, and :445-447 names this
exact loss as the reopen condition — "a pass that stopped on the anti-pattern while holding a lesson
clause 2 or 3 admits, and lost it". Clause 2 is met on the record's own cost lines whether the five
relocations are counted as five sightings or one lesson: `[c1]` blocking-equivalent, `[c2]`, `[c3]`,
`[c3]` a cycle each, `[pr r1]` blocking, a round. Homed at `harden`:185 and not in `pr-harden`, because
four of the five sightings were harden CYCLES — a pr-harden-only home would have prevented one of them —
and `pr-harden`:214 inherits that obligation by reference already. It narrows the implicit sufficiency of
that bullet's four mutations, all of which mutate the GUARD while every one of these five moved the
SUBJECT.

**APPLIED, REASON REFUTED · `pr-harden` 0.12.1 — the ranking at :220 deleted ("its weakest point is the
gap" → "one gap is").** The edit survives because nothing in the corpus ranks the failure modes of a
text guard, so the superlative is an unmeasured claim and Step 4 prefers deleting an unsupported clause.
**The submitted justification was refuted and must not be reused:** the proposal read ":22's 'this is the
only one where the assertion measured the wrong PROPERTY rather than looking in too small a WINDOW'" as
retiring the clause, but a window defect IS the gap between the property meant and the string matched —
`[c1]`'s "slice ran 125 lines past the constant, so a hardcoded mark passed as long as the constant's
NAME appeared anywhere in between" is exactly that gap. The record separates two SUB-KINDS of the gap;
six of six sat in it. Also corrected: the proposal cited the grammar rule at `pr-harden`:224 (it is :240)
and claimed those lists name superlatives (they name `any`/`only`/`exactly`/`all`/`never`/`the whole`/
`cannot`). Seventh instance of the parked "proposer not verifying its own citations" count.

**Net growth: +18 lines in `harden` against a four-word deletion in `pr-harden`, and NO prune this
window.** The submitted Step 4 sentence — "net growth is paid for by P2's deletion in the same commit" —
was false arithmetic and the refuter blocked it. The honest justification: both additions are clauses on
bullets that already exist, adding no home and no section, and each supplies the mechanical remedy for a
limb that until now asked its question without answering it — the cost of leaving them unwritten is
measured at four cycles plus a round in one run and a blocking finding in another. A scan for a genuine
prune found none: `harden` carries one tally (:60, "0 of 36 reachable") and it is quoted with its
provenance.

**PARKED at count 1 · a base measured in an EARLIER run is not a base.** `2026-08-27-...-315.md`:24
"Three runs on record against the unchanged prompt, three different bases, each stable within its own
run — so '3/3' against that cell was never safe to publish. Now recorded as unsettled." The second
citation offered does not corroborate it: `2026-08-26-...-315.md`:9 is a WITHIN-run statement
("consecutive repeats measure KV-CACHE stability"), already shipped as `harden`:63-67, and says nothing
about a base from another run. Clause 2 is not met either — that line carries no cost annotation and the
run self-corrected. Whether the two are one mechanism (borderline argmax non-determinism) or two is
OPEN, which is why this parks rather than dies. **REOPEN ON:** a second record of a base moving across
runs against an unchanged input, or one where a figure published against a foreign base cost a round.
When readmitted, drop the absolute — say what the record shows (three runs, three bases, each stable
within its run), not "a base from an earlier run is not a base".

**PARKED at count 1, and already covered · a test TOTAL summed off the wrong lines.**
`2026-08-27-...-315.md`:14 "'3052 tests' … real figure 1557, then 1559. A double count: per-class
`Tests run:` lines summed against each module's `Results:` summary · cost: caught at r2". One record, no
round cost, and `pr-harden`:230 already governs it — "Don't write a tally a later round will have to
re-measure; write the method" is exactly a total in a PR body that round 2 re-measured. The proposal
leaned on this project's `CLAUDE.md` Bash-output rule instead, which is context economy rather than
accuracy; cite :230. **REOPEN ON:** a second record, or one where a wrong total reached a merged PR body
uncorrected.

**REPORT ONLY · the restart contradiction this record flags is already resolved.** The record's "Where a
skill blocked or contradicted this run" names `pr-harden` §6 against `resolve-ticket` §1 on restarting a
running standalone and says "Worth reconciling"; both were reconciled live on 2026-08-27 under an
owner's instruction (`pr-harden`:322-327, `resolve-ticket`:112-114, `verify-frontend-change`:40). Those
three files were UNMIRRORED in the source repo, so this retro's commit carries three version bumps it
did not author — `pr-harden` 0.11.0→0.12.0, `resolve-ticket` 0.12.0→0.13.0,
`verify-frontend-change` 0.1.0→0.2.0 — plus this pass's own `pr-harden` 0.12.1 on top.

**REPORT ONLY · the field check's first post-ship exercise.** Same record: "resolve-ticket §8 says check
`closingIssuesReferences` rather than the wording — it earned its place twice here." Shipped as `3394ec3`
on 2026-08-27; `Refs #315` still produced `closes=[315]` because the body said "Please close #315 by
hand", and the remedy was removing the keyword rather than rewording around it. No edit proposed.

## Running parked counts (superseding the previous block where they differ)
- A check that examined nothing reporting clean: **2 records** (#315 ×2) — **APPLIED** above.
- A text guard defeated by relocating its SUBJECT: **1 record, 4 cycles + 1 round** (#315 / PR 321) —
  **APPLIED** above, promoted by the refuter on clause 2.
- A base measured in an earlier run: **1 record** (#315 / PR 321) — parked above.
- A test total summed off the wrong lines: **1 record** (#315 / PR 321) — parked above; covered by
  `pr-harden`:230.
- The proposer not verifying its own citations: **7 cycles / 6 records** — this pass added one (the P2
  justification, and a wrong line number with it). Still zero rules shipped on a bad citation.
- A retro submitting wording that breaks the counts/universals rule it enforces: **4 consecutive
  retros**. This pass shipped "returns the same clean result", "what only the intended root holds", "is
  not a base" and "however many repeats stood behind it"; the refuter cut all four. Worth a rule only if
  one ever survives the gate; the gate is still the mechanism.
- Every other count in the previous block stands unchanged, with ONE correction made 2026-08-27 after
  the retro, while answering which rules belong in hooks: **"verifier standing-permission friction:
  5 records — relaxation killed" is now CLOSED, not killed.** #298 identifies the permission concerned as
  standing permission to restart local standalones ("memory granting standing permission to restart
  local standalones. Followed the memory"), and the 2026-08-27 owner instruction settled it directly —
  `pr-harden`:322-327, `resolve-ticket`:112-114 and `verify-frontend-change`:40 now all say to take the
  standalone without asking. The 2026-08-24 objection that killed the relaxation ("where the project
  records a standing permission, that governs" would let an agent-written auto-memory outrank a safety
  guard) is void twice over: the permission is now the owner's own, stated, and the guard it protected
  has been reversed. Carried forward unchanged by this pass's own block, which is how a closed count
  keeps reading as an open one.

## 2026-08-27 (third window: 2 new records, #266 / PR 322 and #293 / PR 323) — two applied, both revised by the refuter, two killed, five parked; linter 10 files, 0 findings

**APPLIED (revised) · pr-harden 0.12.5 and harden 0.20.0 — "Search for the claim's rarest single TOKEN,
over the whole tree rather than over the docs."** Both homes of *Correcting a claim means finding every
home of it* said to grep the claim's *distinctive phrasing*. #266:14: "the two-format claim about the
groups file has N homes -> seven, found one per cycle, each hidden by a NEW mechanism: a data file
rather than a doc; markdown emphasis splitting the phrase; a line break between quantifier and noun
with wording matching no other home · cost: 3 harden cycles" — clause 2 on one record, and the
refuter's own correction is why it is stated that way rather than as two: **the #293 corroboration
offered for it does not hold.** #293:11 was quoted with an ellipsis removing "retired by this change",
which reframes it from a correction that failed to find its homes into a claim the change made false,
and #293:10 (asserted in four places before being measured) is harden:289's class, not this one. Two
further objections shaped the wording. The submitted "**Every** survivor #266 paid for was a home a
phrase grep **structurally cannot** hit" is false on the record's own evidence — a data file is
reachable by a phrase grep, and what failed there was SCOPE — so the shipped text names the two failure
kinds separately and attributes the third home to scope. And "name the three mechanisms" would ship a
closed list against harden:197's own "treat no list of relocations as closed", so the shipped text ends
"treat no list of those mechanisms as closed". The existing after-check ("then grep again for the
phrasing you just wrote") is kept in both homes; the plan to "replace the method sentence" had not said
it would be.

**APPLIED (revised) · harden 0.20.0 — "And ask whether the thing you fixed has a SIBLING. The
revert-check above cannot answer that."** Three records: #315:22 (a fail-open fixed in one script of a
family, left in the member that was actually gated), #266:18 and :21 (a validity rule's detail
corrected on one rule and not its sibling; a literal asserted at one of two call sites), #293:21 (a
normal form compiled twice from one pattern, widening one making a name unfindable in the record that
renders it). Readmitted from the 2026-08-26 parked entry at count 1, which is what Step 3 licenses.
Three refuter corrections are in the shipped form. The submission counted "#266 ×2" toward the bar: the
unit is RECORDS (skill-retro:63) and those two bullets are one lesson in one `[h1]` group, so the count
is 3 records and not 4 sightings. As worded it reached **one of its three sightings** — "grep for a
second definition" fits #293 and neither #266 bullet nor #315 — so the noun is widened to the family
the records share (a second definition, a sibling rule, another call site, a sibling script). And Step
4 subsumption was owed against harden:192, which already requires "checked by reverting it and
confirming the failure": on #293 that check answers directly, since the record's own words are
"reddened NOTHING" — so the rule now opens by saying what it adds, that a revert-check shows the suite
observes a fix and says nothing about a second member. Landed in ONE home rather than two; pr-harden
reaches it by the reference it already carries at :214-215.

**KILLED · pr-harden step 6.2 + verify-frontend-change:29 — "the JDK rule names one direction and
hardcodes 1.8".** Submitted on clause 3, that ":317-321 says 'Build under the JDK the pom targets' while
prescribing `/usr/libexec/java_home -v 1.8`", with `openmrs-module-chartsearchai/pom.xml:34` =
`<maven.compiler.target>11</maven.compiler.target>` as the contradiction. **Not clause 3:** read whole,
the sentence states its own antecedent — "**a module on Java 1.8** fails its test gate under a newer
default JDK" — so for the module it names the remedy agrees with the premise, and it never asserts what
chartsearchai targets. The pom is a fact about a repository, and clause 3 is available precisely
because "the contradiction is a fact about the document rather than an inference about the world"
(skill-retro:66-68); incompleteness in one direction was already ruled not-clause-3 at :434-440 and
:527-528. With clause 3 gone it is 1 record at #266:30's own "Cost: one repair attempt" — neither a
round nor a cycle, so clause 2 (skill-retro:64-65) is unmet. The submission also carried a false claim,
"Prunes the hardcoded `1.8` in both files (it is the false half)": 1.8 is a real target and is the
antecedent the `MockitoException … Java: 21` signature attaches to, so deleting it is separately blocked
by skill-retro:82-84. And the mechanism was already present at the second site — verify-frontend-change:29
*already* says to read `<java.version>` (or `maven.compiler.target`) from the pom, and the run
mis-repaired anyway. **REOPEN ON:** a second record, or one where the wrong-direction repair costs a
round or cycle. If readmitted, the edit to test is the second signature (`invalid target release: <n>`
under too OLD a JDK) added BESIDE 1.8, never a prune of it.

**KILLED · skill-retro Step 6 — "the `*gate*.sh` glob over-reaches its own obligation".** Raised by this
retro from its own Step 6 sweep: two files match `*gate*.sh` and have no hook copy by design
(`harden/gate-test.sh`, `pr-harden/gate-test.sh` — test harnesses taking `HOOK="${1:?hook path}"`,
installed nowhere). Facts verified independently by the refuter and all correct. **Killed twice over.**
The claim merged two separate bullets: the glob sits on :116-120, whose obligation is live-skill↔repo-skill
mirroring, while the hook-copy obligation is :121-126 and does not use the glob at all — it says "each
gate" and then defines it by the install line. Read whole, nothing is unsatisfiable; same reading
failure as :429-433, where a clause-3 claim died because the quotation had removed the deciding clause.
And the narrower wording would LOSE real coverage, on the record: scoping :118 to installed gates drops
both harnesses from the repo mirror, and they have drifted — `2026-08-27-retro-authored-hook-regression.md`:40
("`pr-harden/gate-test.sh`'s new header was a verbatim copy of harden's, quoting harden's hook and
harden's numbers (8/3 where its own measurement is 8/4)") and :77. Also below the bar on provenance:
0 records / 1 measurement, and :534-537 has already ruled that "not proposable on a measurement alone".
The submission's "reports two missing hook copies on every pass, forever" is itself unchecked — no retro
block on record reports that finding. **Residue, banked without an edit:** :118's glob and :121's "each
gate" use the same word for two different sets.

**No change · #266's "`gate-state pr-set` dropped `declined` and `reviewed_shas`".** Retracted by the
record's own appended correction (#266:55-58): `pr-set` uses `setdefault` for both and cannot drop them;
a foreign `gate-state --cwd <path> clear` removed them. Confirmed in `~/.claude/pipeline/gate-state`
(:209, :250-256). The skill's claim that a transition write "cannot drop them" stands. Recording it
because the run record states the defect in its own "Where a skill blocked" section, where a retro
reading only that section would have changed a correct rule.

## Running parked counts (superseding the previous block where they differ)
- **Cross-session interference on a live run's shared state: 2 records, and no single remedy at 2.**
  #266:55-64 (a foreign `gate-state --cwd <path> clear` emptied a live run's ledger, and the same
  session released its slot lease, deleting the chartsearchai worktree twice mid-verifier; the run
  recovered it with `git worktree add --detach` at the same sha and re-attached the branch) and
  `2026-08-27-retro-authored-hook-regression.md`:87-95 (a second session mirroring `~/.claude` into
  querystore, so a commit ABSORBED its `KEY="$PWD"` → `KEY="$(pwd -P)"` edit and described it in this
  session's voice; ":92 the `cmp` checks passed because they compare live against repo AFTER the copy").
  Ruled TWO events, not one described twice: different repos, different shared surface, different harm
  (destruction vs misattribution). Independence is weaker than two records usually implies — #266:51-52
  names its interferer as "a concurrent Claude Code session working on the pipeline itself", possibly
  the same actor. This entry supersedes that record's own "Count so far: **1 record**" (:99), so a third
  sighting does not restart at 1. The two candidate remedies, each at 1: refuse to clear or release
  another checkout's pipeline state without a liveness check; and, before mirroring `~/.claude` into the
  repo, check whether another live `claude` process has written those files since you read them.
- **No progress signal for parallel Phase 2 agents: 1 record** (#293 — "the agent output files stay at
  201 bytes until the agent finishes, so size-idleness is not a usable progress signal. In-turn waiting
  had to be blind `sleep` loops"). harden prescribes no size-idleness, so this is a GAP, not a
  contradiction. REOPEN ON: a second record, or one where the blind waiting costs a cycle.
- **`gate-state reviewed-sha` / `declined` reject `--only`: 1 record** (#293). ~0 cost, and the script is
  right by construction — both write only the `pr` entry (`gate-state`:250-256, :244-248). Candidate
  remedy is one line in the usage block saying they are pr-scoped. Not clause 3: the skill documents
  `--only` for `await`/`clear-await` only (the `await "review r3" --only pr` example in pr-harden's
  **State** section, and the `await "phase2 quality" --only harden` example in harden's Phase 2). Both
  line citations this entry originally carried had rotted within two days of being written, which is
  the evidence for naming a target rather than locating it.
- **A mutation surviving because the only covering case sits in a degenerate state: 1 record, 1 cycle**
  (#266:16 — hardcoding four of the crossReactivity map's five keys left the suite green, because the
  only case reading it drives the DISABLED state where all five equal the mutation). Possibly already
  reached by harden's "Ask of any clean or zero result what its inputs could not have produced".
- **harden Termination vs the claims-about-claims anti-pattern: not carried, and deliberately not
  counted as a contradiction.** #293 states it as one ("The skill names that signature and tells you to
  change tactics, but its termination rule still demands a cycle that changes nothing … took the
  labelled override"). Reading recorded instead: :288's tactic is *delete*, and a deleted clause
  generates no successor claim, so the recursion terminates — this is the shipped rule being violated,
  not two rules conflicting. Filed under Prose-correction cycles.
- **Prose-correction cycles: 9 records** — #266 adds "seven homes, found one per cycle" (:14) and #293
  adds a claim asserted in four places before measurement (:10) plus a five-text measurement whose fifth
  home surfaced a cycle later (:11). Remedy still killed; deletion-over-rewording is the shipped rule.
- **A vacuously-true negative assertion: existing rule exercised and worked, nothing proposed.** #266:17
  — `assertFalse(capture.describeAll().contains(...))` on a List was an exact-element match that can
  never be true, green while the forbidden line was being logged; caught by the run's own mutation check
  at cost 0, which is harden:192. Recorded so a later pass does not read it as a gap.
- **The proposer not verifying its own citations: 7 cycles / 6 records** — unchanged by this pass at the
  run level, but the RETRO added three of its own, all cut at the gate: an ellipsis that reframed
  #293:11, "#266 ×2" counted as two records, and P4's merge of two bullets into one quotation.
- **A retro submitting wording that breaks the counts/universals rule it enforces: 5 consecutive
  retros.** This pass shipped "Every survivor … structurally cannot", "name the three mechanisms" and
  "on every pass, forever"; the refuter cut all three. Still worth a rule only if one ever survives the
  gate — the gate is still the mechanism.
- Every other count in the previous block stands unchanged.

## 2026-08-28 — window #234 / #236 / #297 / FM2-700 (4 records; two refutation rounds)

Records: `2026-08-28-openmrs-module-chartsearchai-234.md` (PR 326),
`…-236.md` (PR 324), `…-297.md` (PR 325), `2026-08-28-openmrs-module-fhir2-FM2-700.md` (PR 629).
Linter: 10 files, 0 findings. **Applied: P1 (harden 0.21.0), P2 (`gate-state` usage block).**
Two proposals of the proposer's died in round 1; two the round-1 refuter raised itself died in
round 2, to a second fresh adversary.

**P3 · narrow Step 6's hooks `cmp` from "every file" to "every `.sh` file".** Died on three
blocking objections. Clause 3 is unavailable: `skill-retro:66-68` grants it "because the
contradiction is a fact about the document", and which files sit in two directories is a fact about
two filesystems — the ruling at `:773-775` and `:434-440`. Read whole the clause names its own
subject in the next sentence, `skill-retro:128` "That directory is what `settings.json` actually
runs", the same read-whole failure as `:792-793`. And the unchecked "reports a difference every
retro forever" is the identical claim cut at `:799`. Round 2 corrected round 1's second citation —
`:786-801` killed a narrowing of `:118`'s glob, not of `:127`'s "every file", a different clause
with a different loss profile — and confirmed the death stands on the other two. **Coverage note
kept:** narrowing would drop `hooks/README.md` from any mirror obligation, and that file has already
been wrong once (`2026-08-27-retro-authored-hook-regression.md:45`).

**P4 · add `.claude/pipeline/` to the Step 6 sync list.** Died: 0 records, and the proposal said so
itself ("no observed drift"), verified — all four pipeline files byte-identical live-vs-repo.
Clause 3 unavailable per `:775`. Round 2 corrected round 1's second objection: `pool-run:836-838`'s
"the same definition for both" means the two *invocation moments* `parity_problems` serves, not that
a skill's prose may not state a check — and `skill-retro:127-132` and `:137-143` already state two
checks `parity_problems` does not implement. P4 dies on the bar alone.

**P5 (round-1 refuter's own finding) · "Step 6 and `pool-run` disagree about who checks
`gate-state`".** Died on four blocking objections from the round-2 adversary. `pool-run:835` does
not say what was claimed once its docstring is read whole: `:831-838` names its subject first — the
skills, the registered hooks, and the repo mirror — and Step 6 carries all three (`:116-120`,
`:121-126`, `:127-132`); the `pool-run`/`pool-watch` tuple is coverage the docstring
**under**-describes, not an obligation handed off. Clause 3 does not reach it either: `pool-run` is
`ticket-pool`'s driver, skill-retro has no gate script, and `:527-528` already refused clause 3 on
this shape. Stripped of clause 3 it is round 1's P4 again. **What survived is the remedy, and it is
not prose:** `"gate-state"` was added to `pool-run:848`'s parity tuple, which both refuters
independently named. Proved live — with the live copy edited and the mirror not yet updated,
`parity_problems` reported `gate-state differs from the source repo`, which it structurally could
not do before. Governance surface unchanged.

**P6 (round-1 refuter's own finding) · "a mutation survives because every covering fixture is
degenerate".** Died on four blocking objections. The standing objection at `:836` was rebutted
against the wrong line — that rule is `harden:61-63`, not `:192`, and at its own site it asks the
proposed question directly, arriving with `harden:46-52` ("ask what the FIXTURE can express … A
premise no fixture can falsify is not covered") and `:54-56`; all three cited cases are reached.
The edit's antecedent does not fire on two of its three: `#234:46-50` records no mutation (a reviewer
found it by reading `Concept.getName()`'s semantics) and `FM2-700:28-30` is tagged "cost: 0
(explanatory)". The one genuine surviving-mutation case, `#266:12`, is the existing rule working and
diagnosing its own cause unprompted — the shape already ruled at `:846-849`. And the proposed text
broke the grammar rule it was submitted under ("all three", "every concept"), the sixth consecutive
retro to do so (`:853-856`). **Better witness recorded for a future pass:** `FM2-700:35-37`, a real
surviving mutation on a degenerate fixture at a cycle's cost — objections 1 and 3 still bite on it.

**P1 · applied, harden 0.21.0 — an idle output or transcript file is not evidence an agent has
stopped.** Clause 1, two records ruled independent (two repos, two dates, two harms), meeting
`:828`'s reopen condition verbatim. Round 1 cut two blocking wording defects: "both runs … until the
agent finishes" misattributed `#293:27`'s observation to FM2-700, whose agents were "killed
mid-investigation" and never finished; and "the harness reports completion, so wait for that" is
what `#293:27` provably could not do ("In-turn waiting had to be blind `sleep` loops"). Round 2 cut
two more: the terminal-outcome sentence had to be scoped to the gate's allow, because past it
`harden:236-237` and `harden-cycle-gate.sh:27`/`:45` hand the decision to a clock — and `:237` must
NOT be deleted, since it mirrors its own gate script and `skill-retro:121-126` exists so those do not
diverge; and the bullet closes only the false-death half, since FM2-700's `target/` remedy
presupposes isolation while `#293`'s does not. **The claimed closure of the `:588-590` park was
withdrawn**: that preference is about a *past-bound* agent, a clock trigger, and FM2-700 killed on
file idleness rather than at `AWAIT_TTL`, so that entry's own reopen condition is still unmet.
+11 lines, retiring nothing; the recorded cost is two destroyed live agents.

**P2 · applied, `gate-state` usage block.** Clause 1, `#293:28` and `#297:32`. Comment text verified
against the script by both refuters (`gate-state:244`, `:251` both write `held.entry("pr")`;
`:172-179` define no `--only`) and then empirically — `reviewed-sha` on a scratch tenant wrote
`{"pr": {"reviewed_shas": [...]}}` and no harden entry. Applied to the live copy and the repo mirror
in one commit. **Parked, round-2 non-blocking:** four subcommands take no `--only`, not two
(`pr-set` and `harden-set` at `:32-33`), so annotating two invites the reading that the others do —
untested, and the records name only `reviewed-sha`. Also parked: whether `pr-harden`'s State section
should say it too, since `pr-harden:757-759` already shows the correct invocation fifteen lines
above the `--only` examples it was evidently generalized from.

## Running parked counts (superseding the previous block where they differ)
- **`git checkout -- <path>` losing uncommitted work: 11 incidents / 9 records** (#302, #284, #268,
  #269 x2, #250 x2, #308, #317, +#234, +FM2-700). Blocking remedies stay killed and neither new
  incident meets the reopen condition — both are probes on files carrying uncommitted work
  (`#234:56-58`, `FM2-700:48-50`). **Correction to this entry's own wording: "remedies stay killed"
  is now incomplete.** A remedy SHIPPED — `~/.claude/hooks/git-restore-backup.sh`, registered in
  `settings.json` under `PreToolUse`/`Bash` and populating `~/.claude/restore-backups/` — and neither
  incident used it: `#234:56-58` records "one reapply" and `FM2-700:48-50` "Restoring from a `cp`
  copy", the author's own copy rather than the hook's. **A shipped net not reaching the operator is
  a different lesson from the killed remedies, at 2 records**, and is the shape to propose next time
  rather than re-proposing the `cp` aside (killed at `:52-100`, objection 1: a `cp` taken at mutation
  time reverts a concurrent edit exactly as a remembered copy does). No count is recorded for the
  backup directory: a draft carried one and it was stale within the same session.
- **Prose-correction cycles: 12 records** — previous figure 9 at `:843`, plus FM2-700 ("SIX separate
  false sentences … five of them written while correcting the previous one · cost: 5 harden cycles"),
  #297:30 (cycles 2-4, escaped by the shipped delete-rather-than-reword rule, "cycle 4's only edit
  was a deletion") and #236 (a round-3 correction that "made a vague-but-true sentence sharper and
  false"). Remedy killed on the walk-forward at `:32-40`; the shipped rule is what converged #297.
- **Cross-session interference on a live run's shared state: 3 records** — `:811-818` at 2, plus
  `#297:43` (a round-1 verifier "killed a co-tenant's standalone (pool-slots/standalone-8082) by
  misattributing its PIDs before checking their cwd… One co-tenant request lost"). Third distinct
  harm and a third surface. The brief already required the cwd check and the agent did it only after
  the first kill, so the candidate remedy is mechanical rather than more brief text.
- **A published count that does not reproduce from its own stated predicate: 1 record, 3 sightings**
  (#236:9, :10, :13, one Phase 2 pass each). `:776-777` settles that a pass is neither a round nor a
  cycle, so clause 2 is unmet. Plausibly already reached by `harden:299`.
- **No progress signal for parallel Phase 2 agents: CLOSED for the false-death half** by P1 above.
  The progress half — #293's blind `sleep` loops — is open at 1 record; `pr-harden:669` makes in-turn
  waiting the shipped rule there, so it may not be a defect at all.
- **`gate-state reviewed-sha`/`declined` reject `--only`: CLOSED** by P2 above (2 records).
- **New at 1 record each, verified against the records:** `gh issue view` returning EMPTY where
  `gh api repos/<o>/<r>/issues/<n>` works (#236:26 — and `:596` has ruled against writing a machine
  fact into a skill); a stale jar in `.openmrs-lib-cache/<module>/lib/` shadowing a deployed fix
  (FM2-700:18-20, 1 restart); `mvn install` re-dirtying the tree between a spotless revert and the
  commit so `git add -A` takes 27 unrelated files (FM2-700:45-47, 1 amend); a python slice
  replacement whose `end` anchor had moved above `start`, silently duplicating a block (#234:60-62,
  cost 0, and the record notes `harden:300`'s "count what should still be there" is aimed at
  deletions); inserting a constant between a javadoc and the member it documents (#234:39-40, two
  incidents in one slice, both silent); harden's confirming-cycle cost against where the blocking
  findings come from (#236:27 — 5 Phase 2 passes over a 15-line change found no blocking item, while
  both guard bypasses came from pr-harden's clean-context rounds).
- **Existing rules exercised and working, recorded so a later pass does not read them as gaps:**
  #297:29, the refutation gate returning TWO blocking objections that both settled and converged on
  one design, handled correctly by the three-outcome rule ("the naive reading is 'two blockers =
  deadlock = abort'"); #297:31, pr-harden Step 1's sha comparison firing usefully as a no-op; and
  #234:44-45 with FM2-700:35-37, tests passing for a different reason than their names claim, found
  by the mutation check that `harden:192` requires.
- **Ledger citation corrections** (round 2, verified): `:834`'s "#266:16" is `#266:12`, and `:846`'s
  "#266:17" is `#266:13` — two individual errors, not an offset, since `:843`'s "#266 … (:14)" is
  correct.
- **The proposer not verifying its own citations: 8 cycles / 7 records** — this pass added one at the
  retro level (a `skill-retro:126-127` line cite that is `:130-132`), cut at the gate.
- **A retro submitting wording that breaks the counts/universals rule it enforces: 6 consecutive
  retros.** This pass shipped "all three recorded cases", "every concept" and "every covering
  fixture"; the round-2 gate cut all three with P6. The gate is still the mechanism.
- Every other count in the previous block stands unchanged.

## 2026-08-30 — window #296 / #238 / #256 / #263 / #330 (5 records; two refutation rounds)

Records: `2026-08-28-openmrs-module-chartsearchai-296.md` (PR 328),
`2026-08-29-…-238.md` (PR 327), `2026-08-29-…-256.md` (PR 329),
`2026-08-30-chartsearchai-263.md` (PR 331), `2026-08-30-…-330.md` (PR 332).
Linter: 10 files, 0 findings. **Applied: harden 0.22.0, pr-harden 0.13.0, `gate-state` error override.**
One proposal died in round 1; one died in round 2; one the round-1 refuter raised itself parked in
round 2 — the same 0-of-2 base rate for refuter-raised proposals this ledger recorded last window.

**KILLED · P6 — resolve-ticket Step 1: pre-flight that the tree BUILDS.** Three blocking objections.
Clause 1 fails because #296 and #238 are ONE `pool-run` defect seen from two sides — #296's own record
closes "A sibling worktree for issue 238 has the same defect", and `pool-run`'s `ticket_id()` docstring
names the single mis-parameterised invocation behind both. Clause 2 is unavailable because "~3 build
cycles" is not a harden cycle (the unit ruling already recorded in this ledger). And the cause is closed
in `ticket_id()`, leaving the residual class — an unrecognised token returned verbatim — with no
observed member, against a per-run cost of a full test-compile on every ticket forever.
**REOPEN ON:** a second, independent cause of an unbuildable pristine checkout.

**KILLED · P7 — harden Phase 2: tell the four isolated agents not to `mvn install`.** Raised by the
round-1 refuter on clause 3; round 2 ruled clause 3 unavailable and the ruling is the reusable part.
skill-retro grants the single-instance exception for "a skill **contradicting itself**, or contradicting
**its own** gate script … because the contradiction is a fact about **the document**" — reflexive,
possessive, singular. A `harden`/`resolve-ticket` disagreement is two documents, and settling it needs
to know what maven does with a shared repository head, which is the inference about the world the clause
excludes. Two earlier rulings in this ledger turned on the same word ("a fact about two filesystems",
"a fact about a repository"). Three further defects, recorded so a reopen does not resubmit the text:
the edit mis-attributes what isolation was chosen to remove (harden's own bullet names a dirty tree, 842
`NoClassDefFound` errors and two contaminated reports; the record says "the stale-api-jar trap **by
another route**"); its reason inverts where it matters most, since resolve-ticket says an UNSET
`$MAVEN_ARGS` means no per-run head at all, so a standalone `/harden`'s four installs land in the shared
`~/.m2`; and it forbids `mvn install` while harden's Phase 1 prescribes `mvn -pl api install` as the
verification, naming no substitute — which would MANUFACTURE a genuine self-contradiction.
**REOPEN ON:** a second record, submitted with a substitute for the Phase 1 build command.

**Revised rather than killed, with the revision each citation forced.** P1 dropped its prose edit
entirely for a mechanical one after round 2 tested the option round 1 proposed and round 1's revision
had declined: overriding `ArgumentParser.error` reaches the unrecognized-argument path that an epilog
never does, is not fail-open the way accept-and-ignore is, and costs zero skill lines at either of the
two sites that document `--only`. P2 moved from FINISH to the base fetch after round 1 walked all the
collisions forward and found every one caught at r1 or mid-run; round 2 then struck its count and its
"each found by a reviewer who was not looking for it", which the records do not support. P3 was killed
in the submitted form — its premise that harden's next bullet "already names the exit" is false, that
bullet names two different exits — and applied in the form round 2 prescribed instead: the increment
the record actually carries, in the bullet's own voice, with no cross-reference. P4 lost the sentence
that restated "each fix opening the next" three lines above it. P5 lost its line-number pointer.

**A cross-cutting ruling worth more than any single proposal: do not write a `:NNN` cross-reference into
skill prose.** harden already forbids the weaker form — "a positional cross-reference … is a claim about
layout that any insertion falsifies: name the target instead of locating it" — and a line number is
strictly more brittle. Round 2 measured it on this ledger's own last window: of three skill line
citations written 2026-08-28, two had rotted within two days. Both are repaired above, by naming. Four
of this window's six proposals carried one and all four were revised.

**Running parked counts (superseding the previous block where they differ)**
- `main` moving under a run: **4 records** — previous figure 1 (#284), plus #296, #238 and #256. The
  ADR-number half is **APPLIED** above; the merge-conflict half #284 recorded is still parked at 1.
- Prose-correction cycles: **14 records** — previous figure 12, plus #330 (cycles 5-15, one ADR section)
  and #263 (a wrong correction and then its correction). The delete-the-clause remedy stays shipped; the
  delete-the-claim-SHAPE increment is **APPLIED** above on #330's ten cycles.
- `git checkout -- <path>` losing uncommitted work: **~14 incidents / 11 records** — previous figure
  11/9, plus #256 (five production edits, found by a later agent diffing the commit against its claim)
  and #263 (hit twice). The killed remedies stay killed; the shape this ledger itself named — a shipped
  net not reaching the operator — is **APPLIED** above at both sites, at 4 records.
- A text guard defeated by relocating its SUBJECT: **3 records** — previous 1 (#315), plus #256 and
  #330. The termination half is **APPLIED** above; the applied #315 text said only that no list is
  closed.
- `gate-state` write subcommands rejecting `--only`: **5 records** (#293, #256, #263, #330, and the
  ledger's own earlier count of 2). **CLOSED mechanically** above. Two prose remedies preceded it and
  neither reached a caller: the correct invocation already sits fifteen lines above the examples agents
  generalised from, and the usage block applied last window never reaches `--help`, because the parser
  passes only the docstring's first line as its description.
- A mutation that ran but did not take effect: **2 records** (#256 did not compile, #263 was not
  word-split by `zsh`) — **APPLIED** above. Round 2 noted the second record is a measurement sweep
  rather than a revert check, so the placement leans on the first.
- A fixer brief that asks an agent to re-measure evidence it was already given: **1 record** (#238,
  round 5's 600s stall; a retry saying "do not re-measure, edit only" finished in 3 minutes).
- A model override as the "change something between attempts" after a 429: **1 record** for the lever
  (#238); the rate-limit death itself is at 2 (#238, #296).
- A refutation gate emitting a factually wrong objection: **1 record** (#263 — three methods asserted
  package-private that are all `private`; cost ~0 because the run verified before applying).
- A DATA guard escaped by an uncovered key or a size-preserving swap: **2 records** (#263 ×2) — the same
  family as the text-guard relocations but over data. Not folded into the applied text, which is scoped
  to guards over TEXT.
- The proposer not verifying its own citations: **9 cycles / 8 records** — this retro added four of its
  own, all cut at the gates: a superseded ledger range presented as live, a phrase attributed to the
  wrong line, an off-by-one bullet, and a precedent citation that cut against the point it was cited for.
- A retro submitting wording that breaks the counts/universals rule it enforces: **7 consecutive
  retros.** This pass submitted "alone", "any identifier", "no build or test observes", "every
  replacement", "seven relocations", "two readings", "every modified tracked file" and three invented
  thresholds; the two gates cut all of them. Still worth a rule only if one ever survives the gate.

## 2026-08-30 (second window: 3 new records, #255 / #229 / #250) — one parked, three applied, every one of the three revised by the refuter; linter 10 files, 0 findings

Records: `2026-08-30-chartsearchai-255.md` (PR 335), `2026-08-30-…-229.md` (PR 334),
`2026-08-30-…-250.md` (PR 333). **Applied: harden 0.23.0, pr-harden 0.13.1,
verify-frontend-change 0.2.1, and `gate-state`'s `count_edits` with four new `pool-test.py` checks.**
No proposal survived in its submitted form. The refuter also found the pass had UNDER-cited its own
strongest support, which is worth as much as any kill: this ledger's 2026-08-24 (third) block already
killed a proposal whose reasoning named the defect P1 rediscovered — "`edits_now()` counts ALL unpushed
commits and silently scores 0 when the branch has no upstream — the state at Step 7" — five days before
either record was written. A retro that does not read its own ledger re-derives what the ledger holds.

**PARKED · P3 — the mirror of "a mutation that did not take effect": a revert that did not reach the
artifact.** #250: "`api/target/classes` held a mutated class from a revert-check, and two probe runs
silently measured the mutation. Caught only by an impossible answer." Three settled objections. The bar
is unmet: one record, and the cost is two probe runs — this ledger has twice ruled that a pass is
neither a round nor a cycle, and clause 3 is unavailable because a gap is not a document contradiction.
The family framing double-counts: the applied bullet's two records are a mutation that never RAN (#256
did not compile, #263 was not word-split), while #250 is a RESTORE that never reached the compiled
class — a third mechanism, not a second direction. And, banked as a correction to the record itself:
the rule #250 quotes, `pr-harden:"After any mutation rebuild with clean"`, **does not exist in any
skill** (`grep -ri "after any mutation" ~/.claude/skills` is empty); pr-harden's only clean-rebuild
instruction is the round's root `mvn -o clean install`, and harden's commit-before-you-probe bullet does
not reach a stale `target/`. So the record's premise "The rule is in the skill for agents" is unverified.
**REOPEN ON:** a second record, submitted without that rule title.

**The revisions each citation forced, since the applied text is the refuter's and not the submission's.**
P1 shipped an ownership test it did not have: the harden entry survives a run — nothing clears it, and
the skill requires it to say `edits: 0` when the run finishes — so a recorded head is consumed only
when the same `owner` wrote it at an earlier `cycle`, or cycle 1 of a second run in a reused checkout
(which is #229's own configuration, a shared checkout rather than a pool worktree) would count an
arbitrary range. Its named residue covered one of three silent-zero cases: the refuter measured that
`git rev-list --count <unreachable-sha>..HEAD` prints nothing and lands on the same fail-open, so the
helper now REPORTS an unmeasured commit half — no earlier head, or a head that stopped resolving —
instead of returning it as zero. And the fix falsifies the skill sentence the proposal cited as its
clause-3 evidence, which the submission had not proposed to repair; it now carries the no-upstream
reading. P2 lost its ground entirely: "an orphaned javadoc is still true, so a truth check passes it"
is unmeasured and cuts against its own records — read against the member it now sits on the javadoc is
FALSE, which is what harden's neighbour rule already asks, and all three sightings were in fact found
by a review pass. It shipped re-grounded as cheaper prevention at edit time, and answering the
subsumption it owed: the existing clause in that bullet is deletion-scoped, the addition
insertion-scoped. P4 proposed the one edit the earlier kill forbade — "if readmitted, the edit to test
is the second signature added BESIDE 1.8, never a prune of it" — so 1.8 stays with its own antecedent
and both recorded signatures joined it; and it was applied at BOTH homes, because
`verify-frontend-change` already carried the general instruction paired with the same hardcoded
parenthetical, which is the sibling-home rule harden states.

**Running parked counts (superseding the previous block where they differ)**
- **`count_edits` scoring the commit half 0 on a branch with no upstream: APPLIED** at 2 records (#255
  with 9 unpushed commits, #229 with 3), plus this ledger's own 2026-08-24 naming of it. Mechanical,
  like the `--only` closure last window; the two prose remedies that preceded that one are why.
- **An insertion that orphans a javadoc from its member: APPLIED** at 3 records — previous figure 1
  (#234, two incidents in one slice), plus #255 (three agents independently) and #229 (two orphans,
  silent through compile, checkstyle and 1686 tests). Readmitted from the parked entry, as the SIBLING
  rule was.
- **The JDK example hardcoding 1.8: APPLIED** at 2 records (#266's `invalid target release: 11`, #255's
  `No compiler is provided in this environment`), meeting the 2026-08-27 kill's own reopen condition
  ("a second record"). Applied at both homes.
- A revert that did not reach the measured artifact: **1 record** (#250), parked above.
- `gh issue view` returning empty (exit 0) where `gh api repos/<o>/<r>/issues/<n>` works: **2 records**
  — previous figure 1 (#236), plus #255. Still not submitted: the standing ruling against writing a
  machine fact into a skill covers the two forms considered so far. The pointer a third record should
  use instead, because it is a fact about the DOCUMENT: the skills prescribe the failing invocation
  themselves — `resolve-ticket`'s URL table and `pr-harden`'s "A GitHub issue via `gh issue view <m>
  --comments`".
- Prose-correction cycles: **16 records** — previous 14, plus #255 (a false claim replaced by another
  false claim across three cycles, settled by deleting) and #250 ("Second attribution claim of that
  shape to be wrong; the claim was deleted rather than corrected again"). Both runs record DELETION as
  the terminating move, which is the shipped remedy behaving as intended; no increment proposed.
- `git checkout -- <path>` losing uncommitted work: **~15 incidents / 12 records** — previous ~14/11,
  plus #255, where the orchestrator's own revert of a measurement mutation destroyed three uncommitted
  javadoc fixes and three later agents each flagged them as defects. NOT a skill gap: harden already
  says "and your own measurement probes too" and pr-harden "The axis is the FILE's state, not who typed
  the command", so #255's own diagnosis that the hazard is written only for agents is false. Another
  instance of instruction-is-not-the-lever, recorded so a later retro does not read it as missing text.
- The confirming-cycle rule, as POSITIVE evidence: **1 record** (#229 — "every one of the last seven
  passes found exactly one real defect, all prose, each in a file the change had not edited. Six of them
  would have shipped under any 'it's basically converged' stop"). Any future proposal to weaken
  Termination has to pass this.
- The proposer not verifying its own citations: **10 cycles / 9 records** — this pass carried a rule
  title from a run record without checking it exists, and under-cited its own ledger.
- A retro submitting wording that breaks the counts/universals rule it enforces: **8 consecutive
  retros.** This pass submitted "never returns to zero", "block every stop", "an orphaned javadoc is
  still true" and "no form of the remedy avoids that"; the gate cut all four. Eight for eight caught at
  the gate is still an argument for the gate rather than for a rule.

### Addendum — second pass of the same window, after "anything else" was asked

The ledger has this shape on record twice before (2026-08-25's "second pass, after 'is that the only
lesson?' was asked twice"). It produced one applied rule and three parked entries the first pass had
missed, which is the third time that question has paid.

**APPLIED after revision · P5 — resolve-ticket Step 3: an objection's own numbers are claims too**
(resolve-ticket 0.14.0). Two records, met on the count route: this ledger's parked "A refutation gate
emitting a factually wrong objection: 1 record (#263)", plus #255 — "'Widening the existing 4-arg
validate in place breaks 3 test call sites' (refutation gate pass 2's own estimate, from a grep of one
file) -> the compiler says 33, across three files … · cost: 1 implementation attempt". Independent
events: different tickets, different PRs, one a visibility claim and one a call-site count. Clause 2 is
NOT claimed — an implementation attempt is neither a round nor a cycle, the unit ruling this ledger has
now made four times, and #255's own header says `rounds: 1`.

Four grounds of the submission died at the gate and the applied text uses none of them:
- **Its central ground cut against itself.** It cited "`CLAUDE.md` and a recorded measurement outrank
  the plan, so that is a revision, not a debate" as text that pushes the run to adopt an objection
  unchecked. That sentence's two antecedents are `CLAUDE.md` and *a recorded measurement*; a gate's own
  grep of one file is neither, so it grants no such licence. Another instance of the proposer not
  verifying its own citation.
- **"Neither objection LACKED a citation … passes both"** — an unverified two-member universal. #263's
  record shows an objection that ASSERTED a visibility fact and records no citation. Cut.
- **"Telling the gate to be careful is instruction-is-not-the-lever"** — unsupported. Every instance of
  that shape in this ledger turns on an EXISTING instruction that was ignored; none measures a new rule
  failing on one side and working on the other. Cut, per Step 4's prefer-deleting rule.
- **"call sites, callers, visibility"** — "callers" appears in neither record. Cut.

The grounds that replaced them, all citations the refuter supplied: outcome 2 keys on the citation's
authority and offers no instrument for testing it; outcome 2 forbids a third gate pass, so the final
pass's objections have no adversarial check but the run's own; `harden` and `pr-harden` both carry
"Check it, do not estimate it" on the count that decides a control-flow decision and `resolve-ticket`
carried it nowhere, which is the sibling-home shape this window already applied to P4 at two homes; and
the rule can only bind the RUN, because Step 3's own "the refutation gate is read-only by instruction …
Tell it to restore anything it changed" forbids the gate from compiling anything. Step 4 answered:
subsumes nothing, and the growth is paid for by pruning Step 3's discriminator, which was restated four
lines below itself — net ~0 on a 661-line document.

**Correction to this ledger's own text, from the record rather than from the summary.** The parked
entry read "#263 — three methods asserted package-private"; #263's record says gate pass 2 "asserted
`sharedTherapyClass`/`sharedCrossReactivityClass` are package-private … All three are `private`". Two
named, three private. The applied text follows the record.

**Running parked counts (superseding the previous block where they differ)**
- **A refutation gate emitting a factually wrong objection: CLOSED/APPLIED** at 2 records (#263, #255)
  — previous figure 1. This is the first parked entry in this ledger to cross the bar and ship, which
  is the argument for keeping counts on lessons that are below it.
- **The verifier can green a change whose schema half never deployed: 1 record** (#229 — "core runs a
  module's changelog only on a version change, so a same-version SNAPSHOT redeploy skips it, module
  started=true, null error, ten minutes serving requests against a table lacking the columns · cost: 1
  doc commit"). Gap confirmed against the text: `pr-harden`'s VERIFY deploys by "overwriting the same
  name", nothing in its six steps changes the module version or makes a fresh database, and the only
  liquibase sentence in the whole skill set is a PERMISSION inside the repairs paragraph ("a platform
  bump that runs core liquibase … all fair if they unblock the run"). Below the bar at 1 record and a
  doc commit. A readmit lands at TWO homes — `verify-frontend-change` also says "overwriting the
  same-named file" — and should grep for `liquibase`/`changeset` rather than "changelog", which also
  hits `pr-review`'s append-only-files bullet. **REOPEN ON:** a second record.
- An efficiency lens skipped as a labelled reduction (#250): **not parked as a lesson.** It is the
  labelled-deviation discipline working, and it saved a pass rather than costing one — the ruling this
  ledger applied to raising the round cap. Banked instead: **the phrase "labelled reduction" appears in
  no skill**, so a later retro must not cite it as existing text — the same trap as #250's
  `pr-harden:"After any mutation rebuild with clean"`, which also exists nowhere.
- #229's "search the rarest TOKEN, and the files NOT in the diff" and #250's ADR-51/`CLAUDE.md` sweep:
  **not a gap.** `harden` already says "Search for the claim's rarest single TOKEN, over the whole tree
  rather than over the docs" and names the project's own instruction file as an easy-to-miss home. Both
  runs were the shipped rule working.
- A retro submitting wording that breaks the counts/universals rule it enforces: **8 consecutive
  retros**, unchanged from the block above — this second pass submitted three more (two universals and
  an invented list member) and the gate cut all three. The figure counts retros, not proposals.
- The proposer not verifying its own citations: **11 cycles / 9 records** — previous 10/9, plus this
  pass's precedent citation that cut against the point it was cited for.

## 2026-08-31 (5 new records, #340 / #336 / #337 / #338 / #339) — 1 killed, 2 parked, 1 half-parked, 4 applied after revision; linter 10 files, 0 findings

Records: `2026-08-31-chartsearchai-340.md` (PR 344), `…-336.md` (PR 341), `…-337.md` (PR 345),
`…-338.md` (PR 343), `…-339.md` (PR 342). **Applied: harden 0.24.0, pr-harden 0.14.0,
verify-frontend-change 0.3.0.** Every surviving proposal was revised by the refuter; none shipped as
submitted. Net +47 lines across three files, which is above the estimate the gate gave (+14) and is
recorded here rather than rounded down — the overshoot is in P4's and P5a's structure, not in new
claims.

**KILLED · P2b — import `pr-harden`'s dead-phase contract into harden's Phase 2.** Its own second
citation refutes the remedy: #339 says the contract "**does not fit** a limit that will refuse every
retry for hours". So the edit would have prevented neither incident it cited — #338 recovered without
it, #339 would have burned two further refused retries — and the "clear the `awaiting` entry" half is
already shipped three sections away ("clear it on ANY terminal outcome — a result, or the harness
reporting the agent died, stalled or was killed"). **PARKED as:** harden Phase 2 delegates four agents
and carries no retry contract of its own — **2 records** (#338, #339). **REOPEN ON:** a remedy that
answers a limit refusing every retry for hours, which #339 says was a cheap capacity probe before
re-spawning (1 record for that lever).

**PARKED · P1 — "what makes two refuted claims the same kind is their subject and property, not their
wording".** Three settled objections, and the first is a false claim of the proposal's own: it wrote
that #340's nine Phase-2 passes "**all** went to the rendered text of one chip", which #340's own
findings list refutes — a stale javadoc class list, ADR 59 missing from the TOC, a guard failing OPEN
under `mvn -pl omod test`, and a broken "that last class" referent are four other subjects. Second, the
premise "a run never counts a second attempt" is contradicted by the line it quotes: #340 says the rule
was "applied after the second refutation of the same shape". Third, #336 records DELETION as the
terminating move, which this ledger has three times classified as the shipped remedy working (#297,
#255, #250) rather than an increment. Clause 2 is unavailable: #340's cost is 9 **Phase-2 passes**, and
a pass is neither a round nor a cycle. **Parked at 1 record. REOPEN ON:** a run where differently-worded
claims about ONE subject each took their own pass, and with the rule stated on the subject alone —
the submission's own definition split its five examples across four different properties.

**PARKED · P5b — the same-version redeploy that runs no liquibase.** #229 and #336 are **one event**,
not two records: #336's own line says "Inherited from #229's round, not this PR. · non-blocking,
environment" — one un-run changeset on one standalone, seen by a second run's verifier. This ledger has
killed exactly that shape before (#296/#238 as "ONE `pool-run` defect seen from two sides"). Cost fails
clause 2 as well: #229 is 1 doc commit, #336 non-blocking. The entry's earlier **REOPEN ON: a second
record** therefore stands unmet, and is sharpened: **a run whose OWN schema-bearing changeset failed to
deploy on a same-version redeploy.**

**APPLIED · P2a — a 429 is a capacity condition and the lever is a cheaper agent** (pr-harden, the
dead-phase contract). 2 records, both pr-harden deaths: #238 ("round 1's fixer died instantly on a
session rate limit (429). A retry on a different model succeeded — the model override … is not named in
the skill's retry contract") and #336 ("retry 1 with a leaner brief and a smaller model completed"). This
closes the parked entry at 1 record for the lever. Three revisions the citations forced: #338's death is
a **harden cycle-4** agent, so it is not cited in a document that counts rounds; the model NAMES are
left in the run records rather than written into a skill, per this ledger's world-fact ruling; and the
residue is named — #339's limit refused every retry for hours, where nothing here is known to help.
**Correction to the arithmetic the submission asserted:** the rate-limit death is at **6** records
(#238, #296, #336, #338, #339, #340), not 7 — #337 matched a grep for `quota` inside the word
"quotation" and records no rate-limit death.

**APPLIED · P3 — the DATA form of the guard-subject attack** (harden, Termination). 2 records: #340
(a reflective guard asserting only `containsKey`, so a `put` of `null` beside a new accessor satisfied
it while dropping the value — "#340's own defect, shipped green under a test that appears to cover it")
and #263 ×1 record (an uncovered key; a size-preserving swap). **Closes** the parked "A DATA guard
escaped by an uncovered key or a size-preserving swap", which was parked precisely on the applied text
being TEXT-scoped. **Revision forced:** the whole-FILE clause was cut. #336's both-keys guard, "read the
whole FILE, so splitting `putSafetyChips` into two writers passed the guard whose own message forbids
exactly that", is a TEXT guard defeated by relocating its subject — the bullet's existing territory,
whose shipped fix already reads "bound the window at the construct it is about rather than at a line
count". That is the shipped rule unapplied, not a DATA gap. Placement left harden-only and the
sibling-home question left open on the record: #340's instance was caught by pr-harden's reviewer, whose
Step-1 brief is narrow "on purpose".

**APPLIED · P4 — a moved base falsifies CLAIMS, not only identifiers** (pr-harden Step 1). 2 records:
#340 ("A merge can be textually clean and semantically falsifying … Nothing in the merge flagged them")
and #337 ("both blocking findings were counts the main merge falsified, in four homes … cost: 1 round").
Four revisions forced, three of them cuts:
- **the clause-2 claim was invalid** — the submission summed 1 round across #337, #339 and a post-hoc
  #340 to reach "≥2 rounds"; clause 2 requires two rounds **in one run**;
- **#339 was dropped from this class's count** — its round-costing findings are the identifier class and
  a mutation tally the record does not attribute to a merge;
- **the ledger-closure claim was a citation error** — #284's parked "merge-conflict half" is a 16-conflict
  MANUAL merge with its remedy in resolve-ticket Step 8, a different mechanism in a different document.
  It stays parked at 1. This ticks *the proposer not verifying its own citations* to **12 cycles / 10
  records**;
- **the identifier-spelling clause (b) was dropped entirely.** #339's sweep searched "Decision 61" — a
  phrasing — while the shipped sentence already says "search for the number itself rather than for a
  phrasing you wrote", and #340's post-hoc confirms that shipped rule working and load-bearing. One
  record of an existing instruction not followed is the *instruction-is-not-the-lever* shape.
Also repaired while in the passage: the 13,602-line measurement had drifted onto the identifier
paragraph and belongs to the stale-local-`main` rule above it; it was moved back to its antecedent.

**APPLIED · P5a — the deploy freshness check proves the FILE, not the bytes that run** (pr-harden
verifier step 3 + step 5, and verify-frontend-change step 2 + its anti-pattern). 2 records: FM2-700
("`.openmrs-lib-cache/fhir2/lib/` held BOTH … the stale one shadowed the fix. Wiping the module cache
dir is required, not just replacing the .omod") and #340 ("the first boot ran week-old controller bytes
while the deployed .omod timestamp, the module status endpoint and the lib-cache marker all read
current"), meeting the parked entry's reopen. **Deleted:** "Verifying against a stale `.omod` is the
single most common way this step reports on the wrong bytes" — an unmeasured superlative in a skill that
forbids them, and #340 cuts against it. **Revision forced:** the submission wrote "delete that directory
before boot" into step **5**, which is AFTER step 4's restart — the deletion now sits in step 3 (Deploy)
and only the byte proof is in step 5. "Every freshness signal" became the three #340 names. The
timestamp check STAYS, framed as necessary-not-sufficient, because this ledger's PARKED-at-0 entry on
`omod/target` carries #250's counterweight crediting the existing check with a catch. Residue recorded:
verify-frontend-change step 4 still calls the `started` state check the "Primary (reliable positive
signal)" — the signal #340 shows reading green over stale bytes; the new bullet says so at
the deploy step rather than re-writing step 4. **Corrected 2026-09-09, and the residue is CLOSED:**
this entry read "both #340 and #336", and #336 carries no such sighting — neither of its records
contains the token `started` at all, and FM2-700, P5a's other record, never mentions the status
endpoint either. The second record is #305 (`2026-09-07-…-305.md`:83-85, the status endpoint reading
current over a stale expanded-module cache), found by the round-1 gate of the 2026-09-09 pass, which
rewrote step 4.

**APPLIED · P6 — prune pr-harden's copy of harden's five-universal paragraph.** Two claims in the
submitted justification were false and the gate measured them: the paragraphs are **not** byte-identical
(harden 1226 chars / 207 words; pr-harden 1026 / 171 — pr-harden's is a prefix, missing harden's closing
sentence), and "~190 words" was wrong in both directions. The prune shipped on the stronger ground the
gate supplied instead: **provenance.** pr-harden's copy said "Measured on the seventh run" of a
measurement that is #298's **harden cycles**, in a document that counts rounds, 88 lines below a
neighbouring "Measured on **this loop's** seventh run" — the same harm shape as `gate-test.sh`'s header
quoting harden's hook and harden's numbers. It now names `/harden` and #298, keeps two of the five
examples, and points at harden for the rest. Verified before pruning: the fixer has no other route to
harden's text — line 208 enumerates the bullets in pr-harden itself, and naming a section is not routing.

**APPLIED (refuter-raised) · harden Phase 2's diff base.** #336: "four parallel agents in isolated
worktrees all reported the local `main` ref was stale by many commits, so `git diff main...HEAD` showed
~15k lines. Every brief had to name `09717dc7...HEAD` explicitly. The skill warns about this for
pr-harden's reviewer; the same hazard bites harden's Phase 2 agents and is not stated there." 1 record,
and the cost is a detour per agent rather than a round — so it ships on the DOCUMENT half: harden's
shipped check is "confirm its diff is non-empty", which a 15k-line diff satisfies, i.e. the check is
fail-open against the case it exists to catch. That is readable from the file without the record, and it
is now "confirm its diff is the SIZE of the change", with the base named. The sibling home carries its
own measurement (pr-harden's 13,602 lines) and this ledger has ruled two homes defensible for this pair.
Recorded plainly: this is a 1-record readmit justified by a fail-open in the shipped wording, not by
corroboration.

**Running parked counts (superseding the previous block where they differ)**
- Prose-correction cycles: **~20 records** — previous 16, plus #340 (9 Phase-2 passes in one cycle),
  #336 (3), #337 (2) and #339 (rounds 8-11). The delete-the-clause and delete-the-claim-SHAPE remedies
  stay shipped; P1's attempt to operationalise "the same kind" is parked above.
- `git checkout -- <path>` losing uncommitted work: **~17 incidents / 13 records** — previous ~15/12,
  plus #339 ×2 ("once for the orchestrator … and once for a fixer, which replayed its edits"). The
  shipped net is in place and was verified this pass: `git-restore-backup.sh` IS registered as a
  PreToolUse hook in `~/.claude/settings.json`. #339's own reading — "the rule is in both skills; it
  still happened, because the destructive call looks identical to the safe one" — proposes no new
  remedy and none is applied.
- The rate-limit death: **6 records** (see P2a's arithmetic correction). The model-override lever:
  **APPLIED** at 2 pr-harden records.
- harden's confirming-cycle cost against where the blocking findings come from: **3 records** —
  previous 1 (#236), plus #337 (5 Phase 2 passes, then pr-harden r1's two blocking findings) and #340
  (9 passes, then pr-harden r1's `containsKey` defect). Still parked, and the standing counterweight is
  #229's "six of them would have shipped under any 'it's basically converged' stop". Recorded so the
  next retro does not re-derive the count.
- A mutation that ran but did not take effect: **APPLIED, and a third mechanism observed.** #337's
  "a perl escaping slip left the line unchanged and the check reported green" is harden's shipped
  "assert the target text is present before replacing" unapplied — *instruction-is-not-the-lever*, not
  a pending increment.
- The shared `~/.m2` api jar: **2 records** for the KILLED-P7 family — previous 1 (harden's own 842
  `NoClassDefFound` errors), plus #340's "the reflective guard fails OPEN under `mvn -pl omod test`
  (stale ~/.m2 api jar)", a new harm rather than a repeat. The kill's reopen asked for "a second record,
  submitted with a substitute for the Phase 1 build command"; the record half is met, the substitute is
  not, so it stays killed.
- Agent worktrees accumulating until the disk fills: **1 record** (#340 — "162 agent worktrees totalling
  22G had accumulated under `.claude/worktrees/` across runs"; Bash failed with ENOSPC and a worktree
  could not be created). Cost is a hard block rather than rounds or cycles. **REOPEN ON:** a second
  record, or a run that loses work to it.
- pr-harden's cap raised nine times on a change where each fix legitimately opens the next defect in the
  same area: **1 record** (#339, 13 rounds over ~16h). Parked, and the record argues AGAINST a stop
  rule: rounds 8-11 found no behavioural defect and round 12 then found a real one (366 duplicate chips
  over 610 products), so any rule stopping after four quiet rounds would have shipped it. **REOPEN ON:**
  a signal that separates this from spinning, not a cap.
- A brief's own factual claims going unchecked: **1 record** (#338 — "my own verifier brief asserted the
  check emits a DEBUG line … It does not; that gate is a bare `return`. The verifier caught the brief
  rather than the code"). Sibling of resolve-ticket 0.14.0's "a gate objection's own numbers are claims
  too". **REOPEN ON:** a second record.
- `mode` still has no writer, and the defect pr-harden's State section already names is now verified
  mechanically: `gate-state` has no `--mode`, no skill writes it, and `pr-harden-gate.sh` reads `.mode`
  for a distinct `--plan-only` message. No record in this window cites it, so nothing was applied — but
  a later retro need not re-check the mechanism.
- A refutation gate emitting a factually wrong objection: **APPLIED**, and #338 adds a positive datum
  ("gate pass 2's blocking objection settled the question rather than opening one, and no third pass was
  run") — the three-outcome rule working.
- The proposer not verifying its own citations: **12 cycles / 10 records** — plus this pass's #284
  closure claim, cut at the gate.
- A retro submitting wording that breaks the counts/universals rule it enforces: **9 consecutive
  retros.** This pass submitted "all went to the rendered text", "never counts a second attempt", "every
  freshness signal", "byte-identical" and "re-check every claim"; the gate cut or bounded all five. Still
  worth a rule only if one ever survives the gate.

## 2026-09-02 (targeted pass, not a window: one question — should harden's cycle gate stop on "no blocking findings" instead of "no edits"?) — 3 proposals, 0 applied; refuted by a fresh read-only agent

Not a window retro: no new records were read for corroboration beyond the two since `LAST`
(2026-08-31), and nothing was applied, so the running parked counts in the 2026-08-31 block stand
except where this block says otherwise. Recorded because two of the three proposals had already been
derived once before, and re-deriving them cost this pass its whole budget.

**P-A · Give `harden` a cycle cap, for symmetry with `pr-harden`'s round cap.** Re-derived
independently of #298's gap (4), and killed a second time.
- The parked entry *"pr-harden's cap raised nine times…"* already rules on caps and its reopen
  condition is **"a signal that separates this from spinning, not a cap."**
- #298's own ground is *symmetry*, a claim about two documents. It listed the cap as one of three
  candidate remedies with no cost attached, so the single-instance route in skill-retro Step 3 was
  never available to it.
- **The walk-forward that kills it is #298, not #229**, and the proposer got this wrong: #298 ran 5
  cycles and `outcome: converged`, so a cap of 4 ends a converged run as did-not-converge and loses
  cycle 5's measured zero — verbatim the first ground on which **P3** died. Other runs past 4 cycles:
  #302 (10), #330 (15), #266 (7), #308 (6), #293 (6), #234 (6), FM2-700 (6), #297 (5), #250, #315.
- **REOPEN ON:** unchanged from the existing entry — a spin signal, not a cap.

**P-B · A cycle-level spin signal (a cycle whose only edits are in the previous cycle's prose).**
Parked, unchanged; the remedy stays killed as **P3**, and the claims-about-claims ruling stays as
written (the shipped tactic is *delete*, so the recursion terminates — a violated rule, not two rules
conflicting). The proposer cited the **2026-08-27** block's count of 9; the running count supersedes it
at ~20. `2026-08-28-…-296.md:26` is a further direct statement of the signature, and names the same
terminating move (fixes moving from re-wording to deleting and to the root cause).

**P-C · KILLED AT THE GATE · "`pr-harden` is diff-scoped, so 'pr-harden reviews next and is the
stronger gate' is a false reason to take harden's override."** Two records state that reason for the
override (#268:59, #337:27) and the observation is real; the *diagnosis* is false, three ways, any one
sufficient:
1. `pr-harden`'s reviewer runs `pr-review` **Steps 1-3 in full**, and `pr-review:99-103` is a section
   headed *"Trace outward — the bugs live outside the diff"* carrying the same unchanged-neighbours
   sentence as `harden`'s Phase 1. It fetches a branch ref, so it holds the head tree, not a patch.
2. The fixer's brief carries harden's Phase 1 discipline explicitly (`pr-harden:220-223`), including
   the whole-tree rarest-token sweep for every home of a claim.
3. Empirically refuted: `2026-08-28-…-297.md:25` — a pr-harden round-2 finding on
   "DdiDrugReferenceSource's self-pair guard javadoc, **a home no sweep reached because that file is
   not in the diff**". The claim was structural, so one instance settles it.
Two further blocking objections stand on their own: the counterfactual "pr-harden could not have
caught #229's six" is in no record, and the clause would contradict `pr-harden`'s shipped statement
that harden "supplies the *weaker* review for this purpose". The surviving true content already has a
home at `resolve-ticket` Step 7 ("The two are not substitutes") — the restatement objection that
killed P3.
- **Parked instead, with no rule attached:** *harden's override taken on the ground that pr-harden
  covers it — 2 records (#268:59, #337:27).* The ground is already answered by `resolve-ticket` Step 7
  and `pr-harden`'s *"Where /harden sits"*; the diff-scope justification was refuted at this gate, so
  no future retro need re-derive it. **REOPEN ON:** a record where the override demonstrably lost a
  defect pr-harden then failed to catch.

**Clarification to the standing counterweight, which reads as a cycle-level fact and is not one.**
The entry *"The confirming-cycle rule, as POSITIVE evidence"* quotes #229's "last seven **passes**".
`2026-08-30-…-229.md:3` records `cycles: 2` and `outcome: converged`, so those passes sit inside a
non-terminating cycle. A **pass**-level stop is what #229 forbids; a **cycle** cap is forbidden by
#298 instead. The pass-to-cycle distribution is not recorded, so anything resting on it stays open.

**New observation, parked.** #339's harden override was **failure recovery, not a cost judgment**
(`2026-08-31-…-339.md:29` — "cycle 1 was overridden (Phase 2 round 3's four agents all died on the
rate limit)"), a use of the valve harden's override sentence does not model. 1 record; adjacent to the
parked rate-limit-death entry. **REOPEN ON:** a second record.

**The proposer not verifying its own citations** — this pass added three, all cut at the gate: #229
read as seven cycles when it ran two, a parked count taken from a superseded block, and an override
tally of 6 where the records show at least 8 (#296, #339 also record an overridden cycle).

## 2026-09-02 (second targeted pass: two rules that had shipped to the live skills without ever passing this gate) — 1 applied after revision, 1 dropped; linter 10 files, 0 findings

Found by the Step 6 `cmp` battery during the pass above: `~/.claude/skills/` was ahead of the repo by
`pr-harden 0.16.0` and `resolve-ticket 0.15.0`, committed to an unpushed branch and recorded in no
ledger entry. Full evidence in `proposals/2026-09-02-retro-gate-shipped-wait-and-batch-rules.md`.

**Provenance, and it is the finding that reaches both.** The figures both rules quote were checked
against the RAW published artifact: `429`, `64.4`, `58.9`, `3.03`, `410,406` and the words `busy`,
`sleep`, `batch` return zero hits; `971,327` is there but means one run's worst peak; `~426k`,
`~100k` and `~1.1k` are in no source at all. The rest came from an unpublished 2026-09-02 re-measure
whose only home is a machine-local memory file that gives two numerators for one phenomenon (61.9 h
and 64.4 h). Step 1 admits run records and nothing else, so **neither rule ever met the Step 3 bar**.

**APPLIED after revision · pr-harden 0.16.0 — "Wait on a CONDITION, never on a clock".** The rule is
sound and the mechanism was verified against the harness rather than against the claim: `Monitor`'s
own description routes this case to `Bash(run_in_background: true)` with an until-loop. Not a
restatement — `verify-frontend-change`'s readiness bullet gives a FOREGROUND poll and no backgrounding.
Three fixes: the measurement paragraph deleted (Step 4's prefer-deletion), a sibling paragraph added in
the State section because the rule sat in the VERIFIER's brief while the cost it cited is the
ORCHESTRATOR's, and the `Bash`/`Monitor` contradiction named rather than left for a reader to trip on.
Recorded as a 1-record readmit justified by a mechanism gap, **not as corroborated**.

**DROPPED · resolve-ticket back to 0.14.0 — "Don't spend a turn per read-only probe".** The collision
it was written to avoid is genuinely absent; it dies on five other grounds, any one sufficient: zero
run records; the harness already mandates the behaviour as a standing instruction, which **P6** settled
is dispositive against a skill's preference; it landed 17 lines below `resolve-ticket:622`'s own "don't
publish a count you would have to re-measure", which is **P0/P7** at the line P0/P7 cited; its
"largest single cost" superlative is refused by the analysis it cites, which declines to partition its
blocks; and "this session never compacts" promotes a hedge the source states as undetectable.
**REOPEN ON:** a run record where a probe stretch cost a round or a cycle — not the publication of the
re-measure, which would only convert unsourced counts into sourced ones in the document that forbids
counts.

**A skill edit that never reached the repo: 1 incident (2 rules, ~29 lines, live-only for ~1 day).**
Step 6's mirror obligation is what surfaced it, a pass later and by accident. **REOPEN ON:** a second
incident — a mechanical check at session start would be the remedy, not more prose.

**A gate's own factual claims are claims too — second instance.** This gate opened by declaring the
branch commit nonexistent and the rules never committed, calling it dispositive; it had searched one
checkout and asserted only one existed. `git cat-file -t` in the other returns `commit`. Every
objection resting on other evidence was verified independently and stands. Sibling of resolve-ticket
0.14.0's rule about a gate objection's numbers, and of #338's brief-checks-out finding.

## 2026-09-02 (window: 7 records — #346 / #349-driver / #354 / #355 / #356 / #357 / #360) — 3 applied, 1 deferred, 1 parked entry found reopened; linter 10 files, 0 findings

Records: `2026-09-01-…-346.md` (PR 351), `2026-09-01-…-349-driver.md` (driver capture, outcome
draft), `2026-09-02-…-354.md` (PR 365), `2026-09-02-chartsearchai-355.md` (PR 362),
`2026-09-02-…-356.md` (PR 361), `2026-09-02-…-357.md` (PR 364), `2026-09-02-…-360.md` (PR 363).
Submission: `proposals/2026-09-02-retro-window-346-354-355-356-357-360.md`.

**A concurrent session was rewriting the governance surface while this pass ran, and none of those
edits are this pass's.** `~/.claude/skills/pr-harden/SKILL.md` and `harden/SKILL.md` were modified at
20:58, 21:04, 21:08 and 21:13 by pid 45786 (`claude --dangerously-skip-permissions`, started 20:58:10,
cwd `openmrs-contrib-gha-workflows`), which also created and registered
`~/.claude/hooks/no-subagent-model-override.sh` at 21:12. At 21:08 live was +20 lines on `pr-harden`
and +19 on `harden` against the repo with neither version bumped; by 21:19 that session had bumped
both, committed `e362818` and left live byte-identical with the repo again. **Nothing was applied to
those two files until it had** — editing them mid-flight is the `git checkout --` silent-loss class in
a different costume, and a diff-only edit re-opens what their measurements closed. The uncontested
half (`gate-state`) went first and was committed before any skill edit began, which is P1's own rule
applied to this pass. Not counted against the parked "a skill edit that never reached the repo" entry
(:1519): those edits reached it.

**APPLIED · `gate-state` `count_edits` — the helper implemented the per-run reading its own docstring
rejects.** Limb: clause 3, a script contradicting itself, plus one record measuring the cost. The
docstring says "Per-cycle rather than per-run on purpose. The run's own total … does not return to
zero while nothing has been pushed, so it would block the stop at the close of a pre-PR cycle that
changed nothing" — and the first branch was `@{u}..HEAD`, which is exactly that reading whenever an
upstream exists. `2026-09-02-…-357.md:28` measured the consequence: `edits=16` at convergence,
0 only after the push. The record's own attribution ("on a branch with no upstream") is inverted and
the fix rests on the code, not on it. The recorded per-cycle head now wins wherever it resolves;
`@{u}..HEAD` is the fallback and says what it counted. Two new cases in `pool-test.py`, both verified
to FAIL on the pre-fix helper (`edits=1` on a cycle that committed nothing) and pass after — 343
passed / 0 failed.
- **The prune was re-aimed by the gate and this is the part worth keeping.** The submission proposed
  deleting "because a cycle that commits its work has still changed something". That clause is TRUE
  and measured — it is #255/#229's justification for counting the commit half at all (:1135) — so
  deleting it would have been Step 4's "never delete a measured rule". What was actually false is
  "pre-PR `/harden` … is the configuration that has no upstream", and the gate proved it by reading
  the branch-cut commands: #357 and #354 used `git checkout -b <b> origin/main` (upstream set, and
  both pre-PR), #355/#356/#360 passed no start point (none). **No skill prescribes either form**, so
  which reading a cycle got was decided by an incidental phrasing. That clause is deleted and replaced
  by the measurement.
- Residue: `harden`:284-288 repeats the superseded framing and still needs the same correction. It is
  in the contested file and is owed with the reconciliation above.

**APPLIED once those files settled — `harden` 0.26.0, `pr-harden` 0.18.0.** Both shipped in the
gate's revised form, never as submitted:
- **P1 · commit-first outranks "one commit per round"** (clause 3; `2026-09-02-chartsearchai-355.md:31`
  verbatim). Survives. Revisions the gate forced: one clause appended at `pr-harden`:509, not two
  sentences, because :510-511 already gives the rationale (amend/force-push is the named harm); drop
  the "a round in which a probe is run cannot satisfy both" half, which **no record witnesses** — all
  four probe incidents lost work because the commit was SKIPPED, none records a second commit; and the
  `cp`-aside kill is the SECOND 2026-08-24 block (:52), not the first (:9). Shipped at the COMMIT
  step as one clause, on the residue case only.
- **DEFERRED · P2 · a 429 names a clock.** Survives only in part, and its home was wrong. Both cited deaths are
  **harden** agents, not pr-harden phases (#356's sits between the cycle-1 and cycle-2 writes; #354's
  both precede `gh pr create`), so :1289-1290's standing precedent — "#338's death is a harden cycle-4
  agent, so it is not cited in a document that counts rounds" — refuses them in the pr-harden
  paragraph. Its real home is the parked harden Phase 2 entry at :1258-1260, whose **REOPEN ON: a
  remedy that answers a limit refusing every retry for hours** this window meets (2 records → 4).
  **The submitted wording is also measurably false**: "a retry spent before the reset changes nothing"
  is refuted by `2026-09-02-…-357.md:27` ("The lever that worked both times was a cheaper model") —
  only an UNCHANGED pre-reset retry is idle. And the live 21:13 text already carries a WAITING remedy
  with its confound stated ("that retry also changed model, so the reset and the model are not
  separated by it"), which removes #354 as a witness and leaves #356 alone. **Not applied**: revising
  text another session wrote minutes earlier, without its reasoning, is what `skill-retro`'s
  edit-from-the-diff anti-pattern forbids. Carried to the next pass, against :1258-1260.
- **P4 · a control whose subject the data cannot produce.** Survives at 2 records, but not the two
  submitted. #355's [r6] matrix is a quantifier over a CONSTRUCTED set, not an empty input — :1104's
  ruling against merging mechanisms into a family applies and it is dropped. The admissible second
  sighting is #355's own verifier bullet (a briefed check "could not have witnessed what it was for,
  and it constructed one that could, plus a positive control") beside #360's typo control. Must be
  keyed on the POSITIVE CONTROL, not on "show the input is non-empty", and must state its trigger, or
  it restates `harden`:231 ("ask of it what its inputs could not have produced") — which never fires
  here, because a negative control is EXPECTED to stay green. The #266 precedent at :849 does not kill
  it: there the mutation check caught it at cost 0, here it did not and two pr-harden reads did.
  Shipped in `harden`'s Termination, with a pointer from `pr-harden`'s guard list.

**Also applied · the doc half of the `gate-state` fix** (`harden`'s `--count-edits` paragraph).
"Pre-PR that second reading is the live one" is deleted, replaced by both measured directions and by
what decides which one a run meets. Leaving the code fixed and its account stale is the
two-resolutions-that-disagree shape the pipeline skills forbid elsewhere.

**Net +22 lines across the two skills, and the pruning is thin — say so rather than dress it up.**
One false clause deleted with its replacement measured; the other three additions subsume nothing.
The growth is justified per addition rather than in aggregate: each is a rule with a measurement, and
P4's is the only class in this window the existing evidence rules provably cannot reach. **Two
defects in this pass's own additions, both caught by its own review rounds:** a positional
cross-reference ("the bullet above") written into `harden`, whose own :384 forbids exactly that; and
a branch-cut claim scoped to "here" when the branch is cut in `resolve-ticket` Step 4.

**A parked entry this window REOPENS, which the submission missed entirely.** :1403-1406 — "A brief's
own factual claims going unchecked: **1 record** (#338). REOPEN ON: a second record."
`2026-09-02-chartsearchai-355.md:17,28` is it: the VERIFIER's brief asserted "warfarin is in
ibuprofen's compact tail on the shipped KB", measured over the bundled 16-drug excerpt rather than the
shipped KB, and the verifier caught the brief rather than the code. **Count: 2, reopen met, no
proposal drafted** — its home is a brief in the contested files, so it goes to the next pass with the
rest.

### Running parked counts
- `git checkout -- <path>` losing uncommitted work: **~21 incidents / 17 records** — previous ~17/13
  (:1372), plus four this window (#346, #356, #357, #360), every one the ORCHESTRATOR's own probe.
  **The submission published ~19/16, taken from the SUPERSEDED 2026-08-30 block (~15/12)** — the exact
  defect :1483 recorded one pass ago. Remedies stay killed and :81's reopen ("an incident where
  'commit before probing' WAS followed") is **still unmet**: all four say the commit was skipped.
  One new datum, cause unestablished: #346 reports "No PreToolUse backup was found at the paths the
  skill names" while #357 records recovering FROM that backup on the same class of incident.
- Rate-limit agent death: **11 records** — previous 6 (:1378), plus all five runs of this window
  (#354, #355, #356, #357, #360).
- Prose-correction cycles: **~24 records** — previous ~20, plus #354 (cycles 3-7), #355 (5 cycles,
  "nine of ~twenty corrections were themselves wrong"), #357 (c5/c8), #360 (four consecutive).
  Remedy still killed; deletion-over-rewording is the shipped rule, and #354 converged by publishing
  no mapping at all.
- pr-harden's round cap raised past its default: **2 records** — previous 1 (#339, :1398), plus #355
  (4→9, one at a time, each with the signal stated, converged). Banked as the shipped rule working.
- Dead-agent residue disposition: **3 records, bar met, no rule derivable** — #357 discarded it, #360
  completed it in-session ("cheaper than the two retries the contract allows"), #355 committed it
  before retrying. Three dispositions, no discriminator the records settle. #360's is stated AGAINST
  the shipped contract, which is its own observation. **REOPEN ON:** a record where completing a dead
  agent's residue in-session shipped something a retry would have caught.
- The refutation gate APPLYING the plan and running the suite: **1 record** (#357:8, 1 of 1713 red,
  cost 0 rounds). In tension with `resolve-ticket`:232-233's "read-only by instruction" — though that
  same passage already anticipates mutation ("Tell it to restore anything it changed before it
  reports"), so the tension is with silence about applying, not with an unguarded claim. **REOPEN ON:**
  a second record.
- A `pr-<n>-r<round>` fetch ref ending up checked out: **1 record** (#355), remedy stated there.
- A repo size/budget guard overflowed by MERGING the base: **1 record** (#355).
- An agent stalled with no positive liveness signal: **1 record** (#346). The submission's second
  sighting was withdrawn at the gate: it claimed #356's run "predates `pr-harden` 0.16.0, which now
  blesses the bash-task half", but the blessing is **0.15.0**'s (`dd99bb5`, 2026-09-01 22:28) and
  #356's session begins 2026-09-01T23:39 — after it.
- Runs ending without writing a run record: #349 (`claude -p` exited 1, 4h11m, 2011 assistant turns,
  outcome draft). Nothing corroborated from it, by instruction.
- **The proposer not verifying its own citations: 14 cycles / 12 records** — previous 13/11 (:1320).
  This pass added three of its own, all cut at the gate: the superseded checkout count, the #356/0.16.0
  mis-attribution, and the wrong 2026-08-24 block for the `cp` kill.
- **A retro submitting wording that breaks the counts/universals rule it enforces: unbroken.** This
  pass's P4 shipped "no production mutation reddens it" and P2 "a retry spent before it changes
  nothing"; the gate cut both as unverified universals.
- **NEW, parked with no proposal — a measured lever retired by a same-day hook rather than by a
  measurement.** `pr-harden` 0.17.0 (`e362818`, another session, mid-window) rewrote the 429 paragraph
  to "the lever that used to work is no longer available", retiring the cheaper-agent lever that
  :1285-1287 APPLIED at 2 records (#238, #336), on the strength of
  `~/.claude/hooks/no-subagent-model-override.sh`, created and registered the same hour. Two things to
  watch, neither of which this pass is entitled to fix — the rule is another session's, shipped with
  its own bump and commit:
  1. It writes a MACHINE fact (a hook path on this machine) into a skill, which :1148 rules against.
     The admissible route was available and unused: it is ALSO a fact about the DOCUMENT, since the
     skill itself prescribes the lever the hook now refuses. A hook is revertable and a skill is
     copied to other machines, so a reader elsewhere may meet a skill asserting a lever is gone where
     it is not.
  2. What is left for a 429 is "a leaner brief" and WAITING — and the WAITING half carries its own
     stated confound (#354's retry "also changed model, so the reset and the model are not separated
     by it"), so the paragraph retires its only twice-measured lever in favour of one remedy that is
     unmeasured alone and one whose witness it disclaims. #339's residue ("nothing here is known to
     help") is kept beside all of it.
  Verified by this pass: the hook exists, is registered under `PreToolUse`/`Agent`, and postdates every
  run in this window — so no record in it was collected under the machine state the paragraph
  describes. **REOPEN ON:** a record of a 429 met under the hook, which is the first evidence about
  what the contract actually has left.

**KILLED · a verifier brief's own factual claims go unchecked** (`proposals/2026-09-02-briefs-own-claims.md`,
drafted and gated the same evening on the operator's instruction, after this pass reported the entry
at :1403-1406 reopened). Two blocking objections, either sufficient:

1. **The negative-coverage claim was false, against text this very pass had shipped 9 minutes
   earlier.** The proposal asserted "Nothing in `pr-harden`, `harden` or `resolve-ticket` addresses a
   brief's own premises". `harden`:244-246 NARRATES the proposal's own second record — "On #355 a
   verifier briefed to look for a partner the shipped data does not carry re-drove the contract with
   one it does, plus a control" — and `pr-harden`:260-262 cites #355 by name. Both are P4, applied
   above at 0.26.0 / 0.18.0. The four tokens searched (`premise`, `the brief's own`, `brief asserted`,
   `unverified claim`) structurally could not reach either; `brief`, the word in the proposal's own
   title, finds `harden`:244 at once. **Verified independently before accepting the objection.**
2. **The one operative clause contradicted that shipped text, inverted.** The proposal would have had
   the verifier "treat 'the briefed witness cannot exist' as a result to report"; `harden`:245-246
   ends "**rather than reporting the absence as a result**", and `pr-harden`:261-262 ships the remedy
   the proposal omits (construct a witness that can exist, plus a control). Shipping it would have
   manufactured a fresh instance of the self-contradiction class this skill exists to catch.

Also cut: a comment-provenance clause resting on #355 alone (#338 carries none — its false witness is
a DEBUG line at a bare `return`); `:492` for a blank line (the bullet is 493-498); a "#339" attribution
for an example no record carries, which is :1232's own trap; and four universals.

**Two rulings from this gate worth keeping.** The proposal conceded its own weakness as
*instruction-is-not-the-lever*; **that concession is invalid** — :1196-1198 already cut that shape,
because "every instance of it in this ledger turns on an EXISTING instruction that was ignored", and
no such instruction existed during #338 or #355 (`harden`:244-246 landed the same evening). The
correct shape is :849's *existing mechanism exercised and worked*. And the gate's O7, on text this
pass applied rather than on the proposal: `harden`:244-246 narrates a **pr-harden verifier** incident
inside `harden`, which by :1289-1290's own logic is questionable placement. **Not corrected here** —
it is one observation about freshly-shipped text, and re-editing it tonight is the rewrite-faster-
than-you-verify shape. **REOPEN ON:** a reader who has to follow the pointer the wrong way.

**Re-parked, with the spent reopen replaced — "a second record" is now used up and re-parking on it
loops.** A brief's own factual claims going unchecked: **2 records (#338, #355), both caught unprompted
by the verifier at cost 0.** The catching is `pr-harden`:493-498 working one level up, and #355's
remedy already shipped at `harden`:238-246 / `pr-harden`:260-262. **REOPEN ON:** a record where a false
brief claim was NOT caught — a verification reported on the strength of a witness that could not exist
— or one that cost a round or a cycle. A second zero-cost catch is evidence the verifier step works,
not that it needs text.

**NEW, banked with a remedy applied — the source repo has TWO checkouts, and Step 6's battery proves
nothing about the one the pipeline writes in.** `skill-retro` Step 6 says to copy the skills into "the
source repo's `.claude/skills/`" and offers one test of which repo that is: "this pipeline's source is
`openmrs-module-querystore`; `git remote -v` in that checkout is the authority". Two checkouts on this
machine answer that test identically — `~/Projects/openmrs/openmrs-module-querystore` and
`~/Projects/openmrs/querystore`, both `openmrs/openmrs-module-querystore.git`. This pass used the
first; `~/.claude/pipeline/pool.json`'s `source_repo` names the **second**, so an automated retro run by
the pool writes there. Measured at the close of this pass: the pool's checkout was clean and **5
commits behind** origin/main, missing both this window's four commits and the other session's
`e362818`. Nothing was lost — both are working copies of one remote and every commit was pushed — but
the `cmp` battery ran against a checkout the pipeline does not use, so its "all four copies identical"
says nothing about the copy that matters for the next pool run. **Remedy applied: the pool's checkout
was fast-forwarded to origin/main.** `pool.json` was NOT repointed — it is the pool driver's file and
naming which checkout is authoritative is that file's job, not a retro's. **REOPEN ON:** a pool-run
retro that commits on the stale base, or a third checkout.

## 2026-09-07 (window: 13 records — #336/PR368, #347/PR367, #354, #366, #348/PR369, #370/PR371, #337/PR375, #338/PR376, #377/PR381, #378, #379/PR382, #337/PR384, #280/PR383) — 9 applied after revision, 1 killed, 2 limbs killed, 8 parked; linter 10 files, 0 findings; `pool-test.py` 347 passed / 0 failed

Submission: `proposals/2026-09-07-retro-window-336-347-354-366-369-370-337-338-377-378-379-280.md`.
Two refutation rounds, one read-only agent, never a fork. **Every proposal it left standing was
revised at the gate; not one shipped as submitted.** Two ticket numbers appear twice in this window
(#336, #337), which is the source of two of this pass's own mis-citations — key them by PR.

**The live skills had drifted from the repo before this pass began, and the drift is not this pass's.**
`pr-harden/SKILL.md` and `resolve-ticket/SKILL.md` carried the *mark ready only after the last push*
rule (measured on #381, 2026-09-06, and an owner instruction of 2026-09-07), edited live at 00:56/00:57
on 2026-09-07 with **neither file's `version:` bumped** and nothing pushed — so the version no longer
identified the content, which is the parked "a skill edit that never reached the repo" entry (:1519) in
its other direction. Those hunks are mirrored to the repo in this pass's commit and subsumed by its
bumps; they are named here so the next reader does not attribute them to this window's records.

**APPLIED · `gate-state` `pr-set` — a reused checkout inherited the previous PR's ledger.** Limb 3, a
gate script contradicting the contract its skill states, plus `2026-09-04-…-337.md:30` measuring it
(three `reviewed_shas` from PR 345 surviving into that ticket's next run in the same worktree).
Mechanical: `pr-set` `setdefault`s `reviewed_shas` and `declined`, so both survive a change of `--pr`,
while `harden-set` guards the analogous reuse of `head` on same-owner-and-monotonic-cycle. Step 0:71-80
adopts an entry only on a matching or null `pr`; Step 1:122 reads `reviewed_shas`' last entry. Three new
`pool-test.py` cases, **verified to fail before the fix** (the handoff case passed before and after, which
is the half that must not regress). `awaiting` untouched — it is nobody's ledger.
- **The gate cut the skill clause that came with it.** The submission had the helper's print satisfying
  Step 0's "say which PR's entry you cleared and what it said"; it cannot — "what it said" is the
  `round`/`phase`/`blocking` that `pr-set` overwrites in the same call, the obligation attaches to a
  decision taken *before* the write, and `gate-state clear --json` already exists for exactly this
  ("reading it first and clearing it second is two operations, and the gap between them is the race
  this lock removes"). Shipped naming the print a backstop and pointing at `clear --json`.

**APPLIED · `pool-run --claim` printed the command for the skill its own warning had just ruled out.**
Limb 3 again, at 2 records / 2 invocations — `2026-09-03-…-366.md:56-58` and `2026-09-03-…-369.md:56-57`
(#353 and #348, the first double-reported). **Cost was zero both times and by luck, not by judgment**
("Harmless only because the branch was pushed first"), which is why this is a guard rather than a fix
for a paid failure. Revisions the gate forced: the worktree's reset to the base is `make_worktree`
BY DESIGN (git refuses one branch in two worktrees), so the edit must not be sold as fixing that — only
the printed next command was wrong; the branch form is printed first because Step 0 wants the head
branch *tracking*, with the `pull/<n>/head` form named as the fallback for a branch another worktree
holds and its lost tracking stated; and `cmd_work`'s identical warning is **not** included, because
changing what skill that session is launched with is a behaviour change with three existing assertions
describing only the no-PR path. `open_prs` fetches `headRefName` but not `isCrossRepository`, so no
claim is made about a fork's branch.

**APPLIED to the skills — `pr-harden` 0.19.0, `harden` 0.27.0, `resolve-ticket` 0.15.0.** Each in the
gate's revised form:
- **The 429 contract, now measured under the hook** (`pr-harden`). The parked reopen at :1685-1686 — "a
  record of a 429 met under the hook" — is met six times (#336/PR368, #366, #348/PR369, #370, #377/PR381,
  #379/PR382; the hook was born 2026-09-02 20:56 and every one of them postdates it). Three of the
  gate's objections were fatal to the submission as written and all three are in the shipped text:
  "four convergences" was false — **#379/PR382 spent no retry at all**, and its own record says the
  fresh-context read "did not happen and was reported as such"; **#354 predates the hook** (record
  19:05, hook 20:56), so it cannot be cited as measured under it; and the skill's existing sentence
  disclaiming #354's wait witness was itself an over-reading — `2026-09-02-…-354.md:53` says both agents
  completed after the reset with only **one** on a smaller model, so the clean witness the paragraph
  said it did not have was there all along. #366 and #377 applied brief and wait together and are stated
  as unable to separate them. The new rule is #370's: a stated reset is not always inside the run's
  horizon (a WEEKLY cap resetting next day, where a plain leaner-brief retry succeeded anyway), so both
  retries must not be spent waiting. The retired cheaper-model lever's two measurements are KEPT and its
  record disambiguated to #336/PR341, because the paragraph now cites two different #336 runs.
- **A shared BUDGET the base consumed** (`pr-harden` Step 1, a third base-drift class). Submitted at
  seven records, shipped at **four** — the gate found 369 has no size-budget event at all (the phrase is
  336:28, already counted separately) and that 347's and 377's are notes with no merge and no overflow.
  Two further cuts: the paragraph's own "Two classes, and git flags neither" had to become "Three
  classes, and git flags none of them", and the submitted closing clause forbade what #280/PR383 in fact
  did (trim its own prose first, then raise with the reasoning written into the guard). **This pass then
  found a fifth defect in the gate's own replacement wording** — "three of them on `main`'s last hundred
  bytes of headroom" is four of four (39, 79, 19 and 8 bytes), so the clause was deleted rather than
  re-counted.
- **The sequence's own INDEX** (`pr-harden` Step 1, the identifier paragraph). Two failure sightings, not
  four: 378's TOC was self-caught and #337/PR384's sweep reached the index, so both are the rule working.
  The submitted "each after a sweep had declared itself done" was supported for neither survivor and is
  gone — #280/PR383's was a round-3 reviewer finding, and #348/PR369's was `main`'s Decision 70, missing
  since #367 and surfaced by a fourth merge. The one-record merge fact (two entries at one insertion
  point resolving to one line) is scoped to #348.
- **`gh issue view` returning empty at exit 0** (`resolve-ticket` Step 1, `pr-harden` Step 1). Third
  record (#347) reached the count, and the route was the one :1146-1151 prescribed for it — a fact about
  the DOCUMENT, since the skills prescribe the failing invocation themselves. The gate cut the `/comments`
  endpoint (**no record measures it**; prescribing it would re-import the machine fact :1148 rules
  against) and the claim that all three runs paid wasted calls (only #347 records any).
- **A brief names the SHA** (`pr-harden` Step 1). Ships as one clause, and only because :83-93's parked
  reopen prescribed this exact shape ("If a second run records the same detour, the harden-only clause is
  the right shape — quote the record, and state the cost"). The gate cut "a detour each": #370 records the
  mechanism and the remedy and states **no** cost, so the clause says so.
- **A killed run's leftover worktree is a dead agent's tree** (`harden` Phase 2). The submitted REAP half
  is KILLED and stays killed: both records say the orphans come from **killed** runs ("nothing reaps
  them"), so a cycle-close reap is executed by the only party that cannot need it, and `ticket-pool`
  already owns worktree removal. The adoption half survives on #348/PR369's three non-identical orphans,
  with #377/PR381's dead-agent mutation in the shared tree as the second sighting the submission missed.
- **A batched write reports edits an abort never made** (`pr-harden` *Editing by script*, `harden` a
  pointer). Two records at a round for one (#336/PR368 round 1's blocking finding; #280/PR383 states no
  cost, so the submitted "two rounds paid for it" was cut). The section's own "Three failure modes … all
  measured on this loop's second run" had to change with it. **This pass then hit the rule while applying
  it**: the batch adding it died on a bad anchor two edits from the end, the five prints that had already
  succeeded read as the whole batch, and the two missing edits were caught only because a `head -80` on
  the diff review hid them and a later grep did not. That instance is in the shipped bullet.
- **A "no production change" claim is proved from the compiled artifact** (`pr-harden` FINISH). Submitted
  at four sightings, shipped at **two** — the gate found 369 supplies one sighting twice (its other is
  about deriving a FIGURE) and 378's is deployment identity, which is the verifier's hash step working.
  Home moved to FINISH, where #337/PR384's incident actually landed, and the bullet states the scope
  explicitly, because the same tool has opposite verdicts on the two questions: a hash answers "is the
  loaded class the one I built", and cannot answer "did this push change behaviour".

**KILLED · the PRUNE of `resolve-ticket` Step 1's "or no LLM endpoint for a module that needs one".**
Three blocking objections, any sufficient. `2026-09-03-…-354.md:61-62` records the same clause WORKING on
the same module ("the standalone, llama-server and models were all confirmed present at Step 1 and never
became the blocker"), so #337/PR375's finding is one record against another. The skill never prescribes
the check 337 blames — :137-151 says "confirm a standalone exists" and explicitly "**Do not check whether
the port is free**"; the listening-port method was that run's own. And a clause whose antecedent ("a
module that NEEDS one") is not satisfied has been mis-applied, not refuted, so Step 4's "never delete a
measured rule without recording the measurement that retires it" refuses it. **Nothing was changed here**
— the gate's offered scoping half-clause was not taken either, because taking it would add text to close
a one-record mis-application.

**Net +59 lines across three skills, of which ~24 are the other session's mirrored mark-ready rule, so
this pass is about +35, and the pruning is thin — say so rather than dress it up.** Deleted: the WAIT
paragraph's false generalisation ("a session limit states its reset time, so a bounded background wait
until that time is a condition rather than a clock") with its over-read confound, and this pass's own
headroom count. Everything else is addition. The justification per addition: two are limb-3
contradictions between a script and its skill (both closed in CODE, not prose, which is the only kind of
addition here that cannot go stale), one closes a parked reopen the window met six times, and the rest
carry a measurement each. What subsumes nothing is stated as such.

### Running parked counts (superseding the previous block where they differ)
- **Prose-correction cycles: ~28 records** — previous ~24 (:1636), plus #338 (14 cycles; "applying it at
  cycle 6 rather than cycle 11 would have saved roughly five cycles", and the shipped
  deletion-over-rewording remedy is what ended it), #378 (5), #337/PR384 (9), #377. Remedy still killed.
- **`git checkout -- <path>` losing uncommitted work: ~22 incidents / 18 records** — previous ~21/17
  (:1628), plus #378, again the orchestrator's own probe, and again with the commit SKIPPED ("The skill
  documents exactly this and I did it anyway"). :81's reopen — an incident where "commit before probing"
  WAS followed — is still unmet.
- **Rate-limit agent death: 17 records** — previous 11 (:1635), plus #336/PR368, #366, #348/PR369, #370,
  #377/PR381, #379/PR382. The lever question is no longer parked: see the 429 entry above.
- **`pr-harden`'s round cap raised past its default: 3 records** — previous 2 (:1640), plus #379/PR382
  (4 → 5, converged at 5).
- **An insertion orphaning a javadoc: 3 records this window** (#337/PR384 "found independently by two
  lenses", #366, #338 "the known recurring defect, recurring"), against a rule already shipped at
  `harden`:389. No proposal — the rule exists and is being skipped, and more text is not the remedy.
  **REOPEN ON:** a mechanical check that could catch it, or a record where the rule was followed and
  missed one anyway.
- **Step 3's fourth outcome — the gate settles NEGATIVELY and no in-scope code change survives:**
  **2 records** (#354 aborted under condition 3; #338 became a measurement deliverable and converged).
  Both cost 0 rounds and both records say the rule HELD. :1725-1726's precedent treats a zero-cost catch
  as the step working. **REOPEN ON:** a record where the missing outcome cost a round, or produced an
  abort the skill should have handled.
- **`--count-edits` answered by hand at every cycle: 3 records** (#347, #370, #337/PR384) — and the
  window carries BOTH directions, which the previous entry flattened: #347/#370 are the no-upstream
  fallback, #337/PR384 is the opposite ("the branch **had** an upstream, so every reading was
  `@{u}..HEAD`", `edits=14/16/17` at convergence across nine cycles, i.e. the recorded-head path never
  engaged for that run). All three are the fallback printing its own note and the run answering it.
  **REOPEN ON:** a record where the note was absent, or the wrong number reached the gate — or a
  measurement of why the recorded head did not resolve across nine cycles of one run.
- **Recording an await is a separate call from spawning: 1 record** (#337/PR384; the gate caught the
  yield, cost 0).
- **A run record with a raw build log interpolated into a bullet: 1 record** (#377/PR381, 638 KB; read
  here by section extraction, and mirrored to the repo at that size). **REOPEN ON:** a second, or a retro
  that cannot read a record whole.
- **The disk filling from concurrent isolated agent worktrees: 2 records, possibly ONE event**
  (#377/PR381 and #379/PR382, consecutive runs of the same day; #377 names six worktrees each running a
  full maven build). Both had to hand back, and #379 could not write its own gate state, because every
  recovery path needs to create a file first. Carried inside the worktree entry rather than counted, per
  the submission's own flag. **REOPEN ON:** a third record, or one where the two runs are provably
  independent.
- **The proposer not verifying its own citations: 20 cycles / 14 records** — previous 14/12 (:1642). This
  pass added six of its own, every one cut at a gate: 369 credited with a budget event it does not
  contain; "four convergences under the hook" contradicted by its own fourth citation; #354 cited as
  post-hook when it predates the hook by two hours; #337/PR375's incident attributed to #337/PR384; "a
  detour each" and "two rounds paid for it" invented; and, after the gate, a headroom count of three
  where the records say four.
- **A retro submitting wording that breaks the counts/universals rule it enforces: unbroken.** This pass
  submitted "four runs, four convergences", "three of them on `main`'s last hundred bytes", "each paying
  wasted calls", "a detour each" and "two rounds paid for it". Two were cut by the gate, three by this
  pass's own diff review — the last of them in text the gate had written.

## 2026-09-09 (window: 4 records — #305/PR385, #379/PR386, PR393/#388, #397/PR398) — 4 applied edits from 3 surviving proposals, 4 proposals killed, 1 limb killed, and the parked list restated below; linter 10 files, 0 findings

Submissions: `proposals/2026-09-09-retro-window-305-379-393-397.md`, then
`proposals/2026-09-09-retro-window-round2.md`. **Two refutation rounds, two read-only agents, never a
fork. Not one proposal shipped as submitted, and the strongest lesson in the window was raised by the
round-1 gate rather than by the proposer.**

**APPLIED · `pr-harden` 0.21.0 — ask which case hands the guard's SUBJECT its other value.** 3 records
and 4 blocking rounds: `2026-09-09-…-397.md`:159-162 (a context test asserting `Boolean.TRUE` at both
legs, `= true;` at both call sites green) and :205-214 (the flag's last hop, the flag-less overload
green while hardcoding `true` reddens one case — covered in one direction only);
`2026-09-08-…-379.md`:21 (every wire fixture `ActiveOrderClaims(n, n)`, the two `map.put` arms
transposable and green); `2026-09-08-chartsearchai-PR393.md`:24-28 (a contract published over two arms,
tests driving one). #397:330 and :334-335 name the shape: "the mirror of the mutation you just proved".
- **Placement forced by the gate.** The submission put the rule and all three round measurements in
  `harden`'s Termination; :1289-1290 forbids that (a document that counts cycles), and #397:335-337
  prescribes the other home in terms — "worth carrying into the fixer brief as a standing question". It
  ships in `pr-harden`'s fixer brief and `harden` is untouched, which also withdrew the submitted
  merge-and-delete of `harden`:242-250 and so kept "no mutation of production reaches it", the phrase
  "positive control", and `pr-harden`'s "not covered by *If you ADD a guard*".
- **Four things the gates cut from the text.** "the suite is green either way", refuted by #397:212-213
  ("hardcoding `true` there DOES redden one case"); "mutation-verified by its author", false of #379:21
  ("rested on review alone") and unsourced for #393; four universals; and "the mutations above", a
  positional cross-reference into the file that forbids them at `harden`:390. The round-2 gate also cut
  "satisfied by a constant" — it mis-describes a transposition and a hoist — replaced by #397:336-337's
  own wording, and split the bullet so the negative-assertion rule keeps its own headline.
- Bar (b) as submitted said "four rounds in one run"; #397:232 records round 4 at "ZERO blocking", so it
  is three. Bar (a) at three records is what this rests on.

**APPLIED · `pr-harden` 0.21.0 — a mutation result measures the arms it moved, not the mechanism.**
`2026-09-08-…-379.md`:12, at 1 harden cycle + 1 pr-harden round. Split out of the bullet above at the
round-2 gate's objection that it is a different lesson.

**APPLIED · `verify-frontend-change` 0.4.0 — `started` is necessary and not sufficient.** Bar (c), on
the round-2 gate's own upgrade: :40-41 said step 4's check "cannot see this" while :57 called it the
"Primary (reliable positive signal)" and :58 said to "rely on the state check above". Closes the
residue recorded at :1338-1343. Records: #340; #305:83-85; PR393:74-76 (`started=true`, null
`startupErrorMessage`, 404 on every REST path that module serves — the check is not evidence the
controllers are mapped). **#336 and FM2-700 both cut as citations** — see the correction above.
**#397:80-81 is the MIRROR** (`started: false` for a module that had not started) and stays out of the
skill text; it is why the frame is necessary-not-sufficient rather than deletion. The gate also
narrowed the added probe to "one path this module MAPS and read the status code", which is PR393's
actual observable and does not restate step 5, and cut a third copy of the lib-cache pointer.

**APPLIED · `pr-harden` 0.21.0 — deleted "the `Agent` call RETURNS the report".** Bar (c): :875 asserted
it and :879-881 described the opposite path four lines later. `2026-09-08-chartsearchai-PR393.md`:52-55
measured the background launch. The round-2 gate corrected the framing — :886-887 carries a measurement
of the synchronous behaviour, so the record shows VARIANCE, not falsity, which is the argument for
deleting the mechanism claim in either direction rather than asserting the other one.

**KILLED · P2, pr-harden's identical-head guard omits a cause the skill itself licenses.** Bar (c)
fails: the record's own words are "the enumerated causes **did not cover** this one" and the two
sentences "**do not reference each other**" (PR393:41,:45-46) — both gaps, and :331-333 and :1103 have
already ruled a gap is not a document contradiction. The submission's bridge (that #393's case "lands
in the first enumerated cause") is false: that cause's parenthetical requires the fixer to have
declined everything and PR393:58 records nothing declined in any round. The guard also fired and the
run established why at zero cost, which is :849-852's *existing mechanism exercised and worked*.
**PARKED at 1 record / 0 cost. REOPEN ON:** a run that mis-classified an identical head as a *did not
converge* and spent or skipped a round on it.

**KILLED · P3, FINISH tells you to rewrite a body a round just reviewed.** Bar (c) fails on a passage
the submission never cited: `pr-harden`:579-580 already reconciles the exact pair the record names —
"Re-deriving the PR description is still owed and is not an exception, because the body is not in the
tree and does not move the head." The substitute contradiction (:1128-1130's sha anti-pattern carried
across to the body) is the widening :1017-1021 refused — clause (c) is "reflexive, possessive,
singular". And the carve-out was fail-open: nothing in Step 1's reviewer brief asks a reviewer to
review the description, so its antecedent would normally be an orchestrator's belief, while the
consequent skips a step whose measurement is at :592-594. **PARKED at 1 record / 0 cost. REOPEN ON:** a
record where a FINISH rewrite put a false sentence into a body a round had cleared.

**KILLED · P6, the every-home search runs in both directions (deleting prose needs the search
correcting it needs).** Bar (a) fails on this ledger's own precedent that a record where the discipline
WORKED is not a sighting — :1812-1814, :1226-1229, :1725-1726. `2026-09-07-…-305.md`:68-71 is a clean
instance of the run doing it right ("the falsifiable half **WAS** done"), leaving one failure record.
Bar (b) fails too: the claimed cost is #397's cycle 4, which `harden`:193 owed regardless after cycle
3's Phase 2 made 197 insertions / 239 deletions. The operative clause is genuinely absent from
`harden`:383-387, :390, `pr-harden`:186-189 and `skill-retro`:84, which is why this parks rather than
dies. **PARKED at 1 failure record. REOPEN ON:** a second record where a deletion cut the only home of
a fact, or one where finding it cost a round or a cycle that was not already owed.

**KILLED · P7, a claim-correction sweep needs a terminator that is not another grep** — the round-1
gate's own proposal, killed by the round-2 gate, which is the clearest argument in this ledger for
gating a gate's findings. Five objections, three sufficient:
1. **The nearest text is uncited and is the proposal's operative instruction.** `harden`:268 — "**What
   ends the loop is a change in the KIND of question, not another entry on the list**" — sits in a
   bullet with the same open-ended-widening shape (:265, "treat no list of relocations as closed") and
   its worked terminators at :271-273 are structural checks. P7 nominated only the *delete* half
   (`harden`:383-387) and missed this.
2. **P7's imperative excludes its own second record's terminator.** #397:123-124's was "re-running the
   mutation and reading which cases actually redden" — behavioural, not "an identifier, a number, a
   shape" — and that form is already shipped at `pr-harden`:270-273. So #397 is not evidence for the
   imperative as written, and it carried no round cost either (both its incidents were caught inside
   rounds already running).
3. **"Widening does not converge" is refuted by P7's own third record**: on #379:13 the survivor was
   missed because the sweep was doc-scoped, so widening past the docs is exactly what would have found
   it — which is also what `harden`:390 already requires, making the `harden` half
   *instruction-not-followed* (:1196-1198). That half was separately false: #379:13 records three
   survivors and attributes only `config.xml` to doc-scoping, where P7 wrote "the survivor".
4. Its text also stated "four successively wider greps" over a list of three, tightened a trigger the
   record measures at the third sweep to the second, and carried two universals.
- **What survives, and why it parks rather than ships:** #305 alone, at three rounds (:42, :43-45,
  :46-47), which is bar (b); the round-2 gate ruled those three rounds ONE sighting, correctly, per
  :1277-1280. And :1264-1275 already parks a prior attempt to operationalise this family — "what makes
  two refuted claims the same kind is their subject and property, not their wording", **REOPEN ON:** a
  run where differently-worded claims about ONE subject each took their own pass, stated on the subject
  alone. #305:74-75 ("three consecutive rounds were homes of one property. The sweep, not the edit, was
  the failing artifact") is arguably that reopen in rounds rather than passes, and P7 neither cited nor
  argued it. **PARKED at 1 sighting / 3 rounds, and the reopen it must answer is :1264-1275's, not a
  second record.** Whoever takes it next: cite `harden`:268, state the rule on the subject, and give an
  imperative that reaches a behavioural check as well as a structural one.

**KILLED · P4's second limb, appending the unattended-collection uncertainty to `pr-harden`'s State.**
Three objections, each sufficient. It is a diagnosis where `skill-retro`:73-77 asks for a guard, and
its operative sentence ("treat the gate as the mechanism and same-turn collection as the intent") has a
reader do nothing :869-871 does not already have them do. It DEMOTES the highest-cost rule in the
document — :868's "an unattended run never ends a turn with an agent outstanding", behind :865-867's
two dead runs (#297 at 51 turns with no PR, #310 at 1365 turns and $76.72) — in an append placed
immediately after it, while PR393's own incident cost nothing (:54). And it is growth without action in
a file whose compliance falls with length. **PARKED: an unattended run may be unable to comply with
:868 if the harness backgrounds the call — 1 record, 0 cost (PR393:52-55), against :886-887's contrary
measurement. REOPEN ON:** an unattended run that died with an agent outstanding *after* launching it in
the same turn, i.e. where the gate was the only thing between the run and #297's signature.

**Running parked counts (superseding earlier blocks where they differ)**
- **A brief's own factual claims going unchecked: 3 records (#338, #355, #397), all three caught by the
  agent unprompted at zero round cost.** #397's is two invalid instruments in one verifier brief —
  byte-identity as the test, and reading finding subjects off `safetyWarnings[]` — both refused by the
  verifier, which substituted better ones. :1721-1726's condition is explicit that a further zero-cost
  catch is "evidence the verifier step works, not that it needs text", so **the reopen is NOT met** and
  the condition stands unchanged. Narrower and NOT a skill matter: #397 frames the class as "a
  plausible re-expression of a production predicate produced a confident wrong number … `CLAUDE.md`
  already forbids it in scripts, and a subagent BRIEF turns out to be the same hazard with no rule
  covering it" — that is an edit to the chartsearchai instruction file, whose root is at its size
  budget, and it is reported to the owner rather than proposed here.
- **`pr-harden` is silent on where the FIXER works: 2 records** (#250 at :317-323, and
  `2026-09-08-…-379.md`:27). Still not shipped on :318-322's ground — #379's fixer was isolated "per the
  brief", an orchestrator decision rather than a document conflict. **The round-2 gate established the
  two are different halves**, so two reopens, not one: **REOPEN ON** a run where `pr-harden` ITSELF
  tells the fixer to isolate (the *whether*); and separately **REOPEN ON** a run where collecting from
  an isolated tree cost a round or a cycle (#379:27's *how*, at 1 step so far).
- **The measurement's INPUT moving between arms with nothing reporting it: 1 record (#397), 2
  incidents.** A restart emptied the retrieval index — "`chartMode` still read `fullChart` while the
  chart dropped from ~348 records to ~9 … the first shipping-artifact arm was measured against a
  different prompt and was not comparable" — and the index rebuild then "shifted the chart text by a
  uniform 12 tokens, which re-worded 11 of 14 greedy answers". The record names the instrument it
  should have used: "a cross-build differential on a quantity the change is known to move, with the
  cells the change cannot reach as the drift control". `harden`:63-68 has the sibling rule for REPEATS
  and nothing covers ARMS. Cost was one discarded arm, and the project's own eval README already
  carried the rule its author then ignored (:1196-1198). **REOPEN ON:** a second record, or a round or
  cycle spent.
- **Editing the tree while a build you backgrounded is running: 1 record (#397), 2 invalidated builds.**
  "Running `mvn` in the background and then editing files raced the build twice, producing two invalid
  red results that cost a re-run each." `harden`:164-168 and `pr-harden`:924-934 forbid exactly this
  for a delegated AGENT reading the tree; a backgrounded build is the same reader, so this is one
  clause away. No ledger entry stands against it. **REOPEN ON:** a second record.
- **A line-scoped grep cannot see a wrapped SENTENCE, and reports absence: 1 record (#397), twice in one
  run** — "a line-scoped grep over wrapped prose is a fail-open check … which is the direction that
  invites writing a 'correction' over something already correct." The hazard is already named
  (`pr-harden`:696-700, `harden`:390) and the shipped method — the rarest single TOKEN — is immune by
  construction. **This retro reproduced it live**: two of eleven proposal quotations read as MISSING
  under a line-scoped grep and verified once whitespace was flattened. That is a third sighting of the
  hazard and still not a sighting of the shipped method failing. **REOPEN ON:** a record where the
  single-TOKEN method itself returned a false absence.
- **`resolve-ticket` has no step for "the gate you are told to work against is not on main yet": 1
  record** (#397 — "Merging main into that branch first, renumbering to 83 and fixing its five pointer
  sites was prerequisite, not scope"). **REOPEN ON:** a second record.
- **The verifier can green a change whose schema half never deployed: still 1 record (#229).** Checked
  against PR393 this pass and NOT met — #393's mechanism is a stale omod serving 404, not a changeset
  skipped on a same-version redeploy. The operative reopen is the sharpened form at :1281-1283, "a run
  whose OWN schema-bearing changeset failed to deploy on a same-version redeploy", not :1229's.
- **Counts published in prose going stale: 4 records this window, carried and NOT proposable.**
  `2026-09-07-…-305.md`:9-15 (three counts still standing inside the commit titled "the withholding
  kinds are counted nowhere", 1 round), `PR393`:18 ("api 1995 tests" → 1997), `2026-09-09-…-397.md`:
  232-240 ("nineteen" against 20, "a dozen" against 16, and a third count of one population),
  `2026-09-08-…-379.md`:23 (Decision 81's "one mutation, both cases" stale in the round that wrote it).
  The rule exists at `harden`:388 and `pr-harden`:294-304, so this is :1196-1198's shape; the count is
  carried because the previous submission dropped it and `skill-retro`:70-71 requires it.
- **The proposer not verifying its own citations: 21 cycles** — previous 20 (:1907). This pass added
  nine, every one cut at a gate: bar (b) stated as four rounds where #397:232 records the fourth at
  zero blocking; "#336" inherited from this ledger's own mis-citation and never checked; "the guard was
  mutation-verified by its author" false of #379:21; "satisfied by a constant" describing a
  transposition and a hoist; P3 omitting `pr-harden`:579-580, the passage that settles it; P7's "the
  survivor" over #379's three; P7's "four successively wider greps" over a list of three; "observed
  both ways" with no record for the inline direction; and "`…-379.md` throughout" left uncited. **The
  records half of this counter is NOT re-derived here** — this pass's errors touch #397, #379 and #336,
  and whether those were already inside the previous 14 cannot be established from the entry, so the
  figure is left at its last stated value rather than guessed forward.
- **A retro submitting wording that breaks the counts/universals rule it enforces: unbroken.** This
  pass submitted "observes only the values", "all of the guard", "every fixture agrees", "never of the
  mechanism", "green either way", "exactly this", "the first one able to", "every line naming", "no
  phrasing in it" and "widening does not converge". The gates cut all of them; none reached a skill.
- **Two source checkouts, one authoritative: remedy re-applied in the other direction.**
  :1727-1740 recorded `~/Projects/openmrs/querystore` (the `pool.json` `source_repo`) five behind and
  fast-forwarded it. This pass found the reverse — that checkout at `origin/main` and
  `~/Projects/openmrs/openmrs-module-querystore`, which the previous pass committed in, five behind —
  and fast-forwarded the latter. **This pass commits in the `source_repo` one.** Both were at
  `origin/main` before the commit. **REOPEN ON:** a third checkout, or a pass that commits in neither.

## 2026-09-11 (window: 4 records — #247/PR406, #310/PR405, #387/PR404, and a `pr-review` fan-out measurement) — 2 ledger entries applied from 3 proposals, 1 parked with 6 limbs killed, 3 derived candidates killed at this ledger before reaching the gate; linter 10 files, 0 findings; **no skill file changed**

Origin was a design question rather than a window sweep: would `/pr-review` be better if each review
dimension got its own agent? A pre-registered A/B answered it
(`2026-09-11-pr-review-dimension-fanout-ab.md`), and the proposals drafted from it went to a fresh
refuter alongside three candidates derived from the three pipeline records.

**Derived from the records and killed HERE, before the gate — recorded so a fourth derivation costs nobody a walk:**

1. **`harden` Phase 2 lens tapering is unsanctioned** (#310: "I ran 4, then 3, then 2, then 1 as
   findings shrank. Nothing in the skill sanctions tapering"). Already ruled at **:1230** on #250:
   *"not parked as a lesson. It is the labelled-deviation discipline working, and it saved a pass
   rather than costing one."* #310 is the third instance of the discipline working. **REOPEN ON:**
   unchanged — a record where tapering LOST a defect, not one where it saved waste.
2. **`git checkout -- <path>` discarding the ORCHESTRATOR's own uncommitted work** (#247, the
   warnUnreadable helper). Increment only; remedies stay killed, and **:81's reopen — an incident
   where "commit before probing" WAS followed — is still unmet**, since #247 records the commit as
   skipped ("Committing before probing is the fix and it worked thereafter").
3. **Delete the claim SHAPE early and wholesale rather than one claim per cycle** (#310 "applying it
   earlier and wholesale would have saved two cycles"; #387 "FOUR successive attempts … what ended it
   was deleting the claim SHAPE", 5 cycles cumulative). #338 already recorded this exact sharpening;
   the remedy stays killed and the count is carried below.

**P-A · PARKED as observed-not-yet-actionable · "Give `pr-review` per-dimension fan-out as a selectable mode in Step 3."**

**Zero Step 3 criteria met.** The dispositive citation is **:1502** — *"Step 1 admits run records and
nothing else, so neither rule ever met the Step 3 bar"*: the A/B's own header says
`outcome: measurement only`, it was written by neither a pipeline skill nor the `ticket-pool` driver
(`skill-retro`:32-35), and it cost zero rounds or cycles. A measurement is admissible as argument
(**:119-126** heard a four-run wall-clock measurement on the merits and reverted all seven of its
edits) and has never counted as corroboration.

The two records cited for the mechanism are two events, not one — different tickets, dates, lenses and
defects, and both quoted accurately (`2026-09-09-…-397.md`:303 quality lens;
`2026-09-03-…-336.md`:18 reuse lens, "blocking-equivalent" as claimed). **They are nonetheless
counted for something they do not support:** `harden`:100 spawns four parallel agents, so Phase 2 *is*
a fan-out by construction and neither record contains an unscoped single-context arm. They measure
lens-versus-lens coverage INSIDE a fan-out, and are silent on fan-out versus one context, which is the
whole claim. Sharper still — `harden`'s lenses are reuse/quality/efficiency/integration, not
`pr-review`:97's six, and #397's quality lens maps to conventions/quality, **the dimension P-A's own
recommended scope drops and P-B records as not worth its cost**. Read whole, the corroboration argues
against the configuration it was cited to support.

**Parked rather than killed** because the mode question is genuinely open: n=1, on a PR selected
because it favoured the treatment, judged by an adjudicator the record itself flags as possibly
non-adversarial, and whose 20-of-20 precision result never saw raw fan-out at all (only the merged
arm). **REOPEN ON:** a second measurement on a PR chosen for a different dominant dimension, with the
duplication/dedup cost measured — and note `2026-09-11-…-387.md`:39-40 already cuts the other way
("Raised independently by two lenses, the second told nothing about the first · cost: 1 cycle + a
rename"), which is the duplication side the A/B admits it cannot speak to.

**Six limbs KILLED, so a later readmit cannot carry them back:**

1. **The heading rename** (`### When an adversarial refutation pass is worth it` → `### When one
   context is not enough`). A grep of `pr-review` for "adversarial" returns exactly one line, 116 —
   the heading — while `pr-harden`:111 and :119 both name "`pr-review` Step 3's adversarial refutation
   pass". The rename strands both, and P-A proposed no `pr-harden` edit.
2. **"It did not buy discovery"** — false on its own record: `…-ab.md`:38-41 gives 4 arm-B-only
   defects, arm A 8 of 12. What it did not buy was *blocker* discovery.
3. **The 6.5× / 1.6× figures in skill text** — `skill-retro`:90 and **:257-259**. They belong in the
   record.
4. **The four-dimension scope in skill text** — contradicts `pr-review`:97 ("Cover **each** of these …
   an unnamed dimension is one you'll silently skip"), is a one-PR world fact of the class **:257-259**
   admits only as a mechanical check, and duplicates P-B, which is its correct home.
5. **"because a narrow scope leaves budget to run the expensive one"** — the record says the generalist
   "skipped [it] as integration-profile" (`…-ab.md`:51), a profile decision, not budget exhaustion.
   `skill-retro`:73-77: write the guard, not the diagnosis.
6. **The "merge pass may not add" rule** — the record parks whether the merge should be a separate
   agent at all, and the no-add merger is exactly where merged arm B lost a real defect to raw arm B
   (11 of 12 against 12 of 12).

**P-B · APPLIED after revision · Below the bar (not a run record; zero rounds or cycles) — parked with a reopen condition.**

> **Per-dimension `pr-review` fan-out at the full six-dimension list.** Measured once
> (`2026-09-11-pr-review-dimension-fanout-ab.md`): of the six, **performance and conventions returned
> no blocking finding** — the five raw blocking findings are security 1, correctness 1, test
> coverage 2, solution fit 1, artifact-verified in `lens-*.json`. All four raw nits were discarded by
> the merge pass, three from conventions and one from **test coverage**, which is a dimension worth
> keeping, so nit production does not discriminate. The whole arm cost 1.26M tokens; no per-lens cost
> was measured. That the four remaining dimensions would have covered every blocker and both severity
> corrections is **inference from the per-reviewer yield, not a measured arm** — no four-dimension arm
> was run. **REOPEN ON:** a PR where a performance or conventions reviewer returns a blocking finding
> the other four missed.

Revised at the gate on four counts: the proposal's "~210k tokens apiece" was an invented per-lens
figure (the one measured total covers six lenses *and* a 10.4-minute merge, and the record contradicts
uniformity); its nit claim was false for performance, which returned four suggestions and no nits; its
two `harden` citations re-proposed what **:1230** declined, and that same passage warns that the phrase
"labelled reduction" appears in no skill and must not re-enter as if it were skill text; and its bar
line misread `skill-retro`:64-65.

**P-C · APPLIED · Standing counterweight.**

> **`pr-review` Step 1's "Verify claimed fixes against the head — 'fixed' is a claim, not evidence"
> (`pr-review`:20), as POSITIVE evidence.** From a **measurement, not a run record**
> (`2026-09-11-pr-review-dimension-fanout-ab.md`), so it is not criterion-1 corroboration for anything.
> On one PR (`openmrs-core` 6551) two review threads had been closed as fixed. Both repairs were
> defective, and both arms of that one experiment caught it independently: the regression test a
> reviewer asked for and accepted stays green with the fix it guards reverted, and the comment that
> closed the session-cache thread describes stale data where the behaviour is a
> `TransientPropertyValueException` that aborts the transaction. The record attributes the acceptance
> to two humans and a review bot; that attribution is the record's, not independently checkable here.
> **Any future proposal to soften the re-verification of claimed fixes has to pass this.**

Mirrors the shape of **:1162**, and written in units that cannot drift, because that entry itself had
to be re-litigated at **:1477** for stating "passes" where the record meant cycles.

**Parked below the bar, with counts:**

- **Fail-open verification — a pipe swallows the exit status.** #310: `mvn … | grep …; echo "EXIT=$?"`
  reports grep's status, so a compile failure read green, "caught only because a later mutation
  reported zero failures suspiciously." **1 record**, ~0 rounds. No skill carries this shape; the
  nearest is `pr-review`:90 (no `2>/dev/null` on an evidence command, positive control before trusting
  a zero hit). **REOPEN ON:** a second record, or one where it cost a round.
- **`pr-harden`:REVIEW's "check out detached" leaves the worktree detached AND holding the branch**, so
  a later `git checkout <branch>` fails — twice on #247, compounded by a killed agent's orphan worktree
  holding the same branch. The record's own fix is to brief "return the worktree to `<branch>`", which
  later rounds did. **1 record**, a cleanup detour. **REOPEN ON:** a second record.
- **`pr-harden`:Step 0's `maintainerCanModify: false` reads as a refusal condition but only gates
  CROSS-repo PRs**; `isCrossRepository` is what settles it (#387). **1 record, cost nothing**, but the
  guard list invites the misread. **REOPEN ON:** a record where it cost a run.
- **`gate-state:harden-set` misreports `edits` for a cycle whose label was never recorded** (#387:
  recording cycle 4 then cycle 6 left the commit-half window spanning cycle 5). The gate's own contract
  already says it writes an entry "at the end of every cycle" (`harden-cycle-gate.sh`:9), so this is
  :1196-1198's shape — the rule exists and was skipped. **1 record.** **REOPEN ON:** a mechanical check
  that could catch a missing cycle label.

### Running parked counts (superseding the previous block where they differ)

- **Prose-correction cycles: ~30 records** — previous ~28 (:2094), plus #310 (cycles 4-6, one
  over-reaching claim each, converged at 7 by deleting the shape) and #387 (four successive summaries
  refuted, 5 cycles cumulative). Remedy still killed.
- **`git checkout -- <path>` losing uncommitted work: ~23 incidents / 19 records** — previous ~22/18
  (:2096), plus #247, again the orchestrator's own probe and again with the commit skipped. :81's
  reopen still unmet.
- **Rate-limit / session-limit agent death: 19 records** — previous 17 (:2098), plus #310 (cycle-4
  confirming agent; account-wide, so the retry was pointless and the three obligations were done
  in-context and labelled) and #387 (documentation-cycle confirming agent, same shape).
- **The proposer not verifying its own citations: 22 cycles** — previous 21 (:2099). This pass added
  two, both in the brief handed to the gate rather than in the proposals: the count above was quoted as
  "11 cycles / 9 records" from the superseded 2026-08-30 block (:1242), which is :1488's exact failure;
  and the brief credited P-A with an offsetting cut (compressing `pr-review`:120-123) that appears
  nowhere in the proposal file, whose entire Step 4 answer deletes no text. The gate caught both.
  Also carried: the brief mis-cited those conditions as :119-123 when 119 is blank.
- **A retro applying no skill change at all: 1 record** (this pass). `skill-retro`:141-143 anticipates
  it — "A retro that applies NOTHING still commits here, because the parked counts and the new records
  are its whole output." **REOPEN ON:** a second, which would say something about the bar rather than
  about one window.

## 2026-09-13 (targeted pass: the reopen condition the 2026-09-11 block attached to P-A, answered by a second measurement) — 0 applied of 5 proposals as drafted; 2 revised into entries, 2 killed, 1 reduced to an observation; linter 10 files, 0 findings; **no skill file changed**

Window swept: `2026-09-12-openmrs-module-chartsearchai-294.md` (a run record, `outcome: in-progress
at time of writing`) and `2026-09-12-pr-review-dimension-fanout-ab2.md` (a measurement). The second
answers the condition at **:2166**.

**The pass's own procedural claim was false and the gate caught it.** The proposals opened "the window
since `LAST` contains exactly one file"; it contained two, and the run record had been on disk an hour
before the proposals were written, unread. Sweeping it afterwards is what this entry records. A
targeted pass is still the right shape — testing a reopen condition the ledger itself wrote is not a
rule derived from one record — but the exemption was argued from a false premise.

**From #294, swept late:** a third consecutive-Phase-2-prose instance ("three consecutive Phase-2
passes each found false claims in prose the previous pass had just written … I reached for it late,
after the fourth refutation, not the second"), and two shipped rules recorded working — `resolve-ticket`
Step 3's three-outcome gate ("saved a third pass") and the `no-subagent-model-override` hook refusing a
`model` on a rate-limit retry ("correct refusal; the retry's changed variable should have been the
brief only"). **#294 is in-progress; its completion is NOT covered by this pass** — a future retro
reading by date will skip it, so re-sweep it when it finishes.

**Q-A · APPLIED after revision · P-B, strengthened but not as drafted.**
The draft said P-B's reopen condition was "directly tested and NOT met". It was **tested with no
discriminating power**: `adjudication.json` assigns no `blocking` disposition to any of the 17 findings
from any arm (nit ×7, suggestion ×5, question ×4, drop ×1), so a condition phrased on a blocking
finding could not have been met by any reviewer on that subject. The record concedes exactly this for
P2 and P1 is owed the same sentence. Appended to P-B:

> **Second measurement, 2026-09-12** (`2026-09-12-…-ab2.md`), on a performance-dominant PR chosen to
> falsify this entry. Per-reviewer yield: **performance** 1 finding, a duplicate of the control's, 0
> unique; **conventions** 2 findings, of which **1 survives as a lens-unique adjudicated-REAL
> suggestion** (F14, in no duplicate group). So this entry is not strengthened cleanly — conventions
> produced one of the three surviving lens-unique defects, and in run 1 it also returned one unique.
> The reopen condition could not be met on this subject at all, since no arm filed a blocking finding.
> **REOPEN ON:** unchanged.

Citing only "the conventions reviewer returned 0 blocking" would have been the same selective-citation
defect this ledger charged P-A with at **:2181**.

**Q-B · APPLIED after revision · P-A stays parked; reason upgraded, condition rewritten.**
Every figure verified against the artifacts. Applied to P-A:

> **Reopen condition met and answered, 2026-09-12.** A second measurement on a performance-dominant PR
> with the duplication cost measured. The answer does not reopen it: fan-out found **no blocker the
> single context missed in either run** (run 1: 3 blocking defects, each in both arms; run 2: none
> anywhere — so that claim rests on three defects); unique yield was 4 defects each time, none
> blocking, and in run 2 one of the four was destroyed at adjudication as an impossible consequence.
> Cost ~6.3× tokens, and that figure **excludes an entire lost six-lens wave**, so the true cost is
> worse. Duplication, the clause run 1 could not measure: 3 redundant of 10 lens findings (30%) by
> independent adjudication, one defect derived five times, and one trivial dedup decision derived
> seven times — every agent re-fetching the same conversation and re-verifying the same claimed fix.
> Against all that, the strongest single pro-fan-out datum in either run is also from run 2: the
> adjudicator's own pick for most serious defect in that PR was found by a lens alone.
> **Reason for parking upgraded** from *insufficient evidence* to *measured twice, no blocker yield
> either time*. **REOPEN ON (replacing the met condition): a measurement where fan-out surfaces a
> BLOCKING defect the single context missed — the yield neither run produced.**

The draft's second limb ("or a run record from a pipeline skill that used it") is **struck as
circular**: no pipeline skill offers the mode, P-A is the proposal to add it, and the one existing
pipeline fan-out was ruled non-probative for this question at **:2181**.

**Q-C · KILLED. The distinguishing claim is false four times over.**
The draft asserted that every one of the 19 rate-limit records is a single-agent death and that a
wave dying at once is a new shape. Refuted from inside that count:
`2026-08-31-…-339.md`:28 ("all four Phase 2 agents simultaneously"), `2026-09-02-…-357.md`:27 ("4
harden agents at once"), `2026-09-08-…-379.md`:28 ("Both pass-4 harden agents died simultaneously"),
`2026-09-02-…-354.md`:53 ("Two agents died"). And **:1259-1262** already parks the shape at 2 records
with a sharper open condition. The draft's own reopen ("a run record showing the same wave-level
loss") was met four times before it was written. Appended to **:1259-1262** instead:

> A seventh instance, from a measurement (`2026-09-12-…-ab2.md`): six lenses lost at once mid-work
> with results unwritten, four worktrees left dirty by agents that died mid-probe, wave re-run from
> zero. **Supplies no remedy** — the run waited for a fresh window rather than probing capacity, so
> :1262's condition stays unmet.

**Q-D · REDUCED to an observation; the causal claim is KILLED.** "A verification agent's briefed
stance determines its result" is not supported by one paired comparison. The four `anchor_wrong` are a
single duplicate group created by run 2's ticket-half sitting at an untouched line; run 1's PR held 3
blocking defects where run 2's held none, which mechanically yields more overstatement; one of the
four overstatements came from a second pass triggered by an **in-brief warning about the expected
distribution**, an outcome prime rather than a stance; and run 2's adjudicator brief was not archived,
so a claim wholly about brief wording rests on a brief nobody can read. It also proposes nothing new —
`pr-review`:116-118 already prescribes a reader "told to *refute* it". Recorded as: *two adjudications,
0/20 and 4/17 overstated, cause not established; 1 measurement.* Run 1's record has been corrected to
match, having briefly carried the stronger claim.

**Q-E · causal clause KILLED, arithmetic PARKED.** The over-merge is real and reproducible — merger 10
findings → 5 distinct, independent adjudication → 7 over the same 10. But "folded a claim together
with its own refutation" is **misattributed**: the finding asserting placement is not a problem is
**AM2**, an arm-A-merged *control* finding the merger never saw, and the note it was drawn from was the
adjudicator describing its own cross-arm decision. The sharper and unmentioned over-merge is the
merger's B3, folding the defect the adjudicator named most serious in the PR together with a seed-value
nit, a grouping the adjudicator explicitly rejected. Parked as: *the merge pass returned 5 distinct
where independent adjudication found 7 over the same 10 findings; two groupings rejected, one absorbing
the adjudicator's most-serious pick. 1 measurement.* **REOPEN ON:** a second instance.

**P4, recorded correctly rather than as the pass first reported it.** The pre-registered wording was
"inter-lens duplication on raw output **exceeds the merger's self-reported ~28%**", with ~28% fixed in
the same document as run 1's figure. Measured 30% — **the pre-registered numeric test passed.** What is
refuted is P4's *mechanism* (that a merger under-reports because it has a stake in its grouping); run 2's
merger over-reported. The pass initially called P4 "refuted" by substituting run 2's comparator after
the fact. The whole difference is one finding: 3 of 10 against 7 of 25.

### Running parked counts (superseding the previous block where they differ)

- **Prose-correction cycles: ~31 records** — previous ~30 (2026-09-11 block), plus #294. Remedy still
  killed; #294 adds the same sharpening #338 recorded (applied late, not early).
- **`git checkout -- <path>` losing uncommitted work: ~23 incidents / 19 records** — unchanged.
- **Rate-limit / session-limit agent death: 20 records + 1 measurement**, the measurement carried
  separately per **:2237**'s convention. The wave-level sub-shape is at :1259-1262, not a new entry.
- **A retro applying no skill change at all: 2 records.** **:2264-2267 wrote "REOPEN ON: a second,
  which would say something about the bar rather than about one window" — this is that second, and the
  pass did not notice it; the gate did.** What two consecutive no-change passes say about the bar is
  now the open question, and it is not answered here.
- **The proposer not verifying its own citations: 25 cycles** — previous 22 (:2258). This pass added
  three, each cut at the gate: the "exactly one file" window claim, with an unread run record on disk;
  "every one is a single-agent death", false in four records inside the count it cited; and the Q-E
  misattribution of a control finding to the merger. A fourth is defensible — substituting run 2's
  merger for the pre-registered P4 comparator — and is recorded above rather than counted.

## 2026-09-13 (second window of the day: 3 records — #374/PR411, #379/PR414, and #294/PR410's CONTINUATION) — 1 applied after revision, 1 applied as a correction, 1 killed, 1 parked with its clause-(c) limb killed; linter 10 files, 0 findings

Submission: `proposals/2026-09-13-retro-window-374-379-294cont.md`. One refutation round, one
read-only agent, never a fork. **Not one of the three proposals survived as drafted**, and each of
the two that shipped is roughly a quarter of what was submitted. Keyed by PR throughout: a
`#379/PR382` already sits in this ledger at **:1749** and **:1881**, and this window's is PR414.

**Before anything else, a false alarm this pass raised against itself.** Step 6's battery was first
run against `~/Projects/openmrs/openmrs-module-querystore` and reported four drifts — `ticket-pool`
0.21.0 against a live 0.22.0, `pipeline/pool-run`, `pipeline/pool-test.py`, and an unmirrored
`skill-lessons/artifacts/` tree that two run records assert is committed. **All four were false.**
`~/.claude/pipeline/pool.json`'s `source_repo` names `~/Projects/openmrs/querystore`, which is
current with origin/main; `pool-run`'s own `parity_problems(cfg)`, driven directly rather than
re-expressed, returns 0. The other checkout was 3 commits behind (`e4df73f`, `bbd68fb`, `34845fd`).
This is the **:1733-1746** hazard in its other direction — that entry is about what a retro on the
stale checkout WRITES; this is about what its battery REPORTS — and a false drift report is the
more expensive of the two, because a retro acts on it.

**APPLIED after revision · P1 — one sentence in `harden`:390, not the block submitted.** What
shipped is the third mechanism plus the terminator `harden`:268 already prescribes: a home that
names the claim's key nowhere is reachable by no token search, so when a sweep keeps returning one
more home, change the kind of question and enumerate the claim's SUBJECT. Six blocking objections
cut everything else:

1. **Two of the three claimed sightings are not sightings of this lesson.** `#379`:18-22 is four
   different claims in four classes, not one claim in four homes — its own summary at `:28` says
   "progressively narrower **classes** (stale claim -> stale count -> stale ordinal -> positional
   pointer)" — and the submission reached that reading by omitting `:20` (c6). `#294`:137-138 is one
   missed home found in one round with no sweep loop at all, so P1's own trigger never fires on it.
   What is left is **#374 alone at 6 cycles**, which is bar (b) and enough.
2. **The submission moved a cost onto its own evidence.** It quoted `#294`:137 as ending
   "· blocking · **cost: 1 round**". The record ends "· blocking"; the cost annotation belongs to
   `:136`, the finding above it.
3. **"A token search enumerates HOMES and is unbounded" would have manufactured a
   self-contradiction** four lines from `harden`:390's own "**Search for the claim's rarest single
   TOKEN, over the whole tree**", the same defect **:1026-1028** killed a proposal for. It is also
   false on its evidence: `#374`:18 and `:24` blame **sampling**, and a rarest-token grep returns a
   finite hit list.
4. **The prune's stated ground was false.** "Treat no list of those mechanisms as closed" was said
   to be the shape `harden`:268 forbids. `harden`:265 ("treat no list of relocations as closed") and
   `:268` ("not another entry on the list") sit **in one bullet, three lines apart**: one forbids
   believing a list complete, the other forbids list-extension as a *terminator*. And `skill-retro`
   :84-86 forbids deleting a measured rule without the measurement that retires it — #374's third
   mechanism **confirms** the list is open, so it cannot be that measurement. Nothing was pruned.
5. **Growth was misreported as "roughly flat"** for +185 words across two files against 8 removed.
   The shipped sentence is **+110 words in `harden` alone**; the `pr-harden` half was dropped with
   the #379 evidence that justified it.
6. **It is partly instruction-not-followed**, the class at **:1196-1198**: `#374`:24 says outright
   "What ended it was **the skill's own** 'change the KIND of question' rule". What shipped is
   therefore a ROUTING fix — that rule lives in the guard-mutation bullet and is now named from the
   claim-correction bullet — not a new rule.

This answers **:1264-1275**'s reopen and **:2029-2032**'s three instructions on #374 alone: it cites
`harden`:268 by naming its sentence rather than its position, states the rule on the subject, and
reaches the behavioural half by routing — `harden`:199-201 already sends the documentation-pass
agent through this bullet "in full" and requires it to RUN any claim about behaviour.

**KILLED · P3 as a rule; APPLIED as a one-clause factual correction.** `skill-retro`:116-117 said
"this pipeline's source is `openmrs-module-querystore`; `git remote -v` in that checkout is the
authority", and `pool.json` names `querystore`. Clause (c) does not reach the rule half: "two
checkouts exist on this machine" is a fact about a filesystem, refused at **:871-873**, **:884** and
**:1020-1021**, and **:895** settles that `skill-retro` has no gate script for it to contradict. The
banked **:1745-1746** reopen — "a pool-run retro that commits on the stale base, or a third
checkout" — is unmet, and same-cause-different-consequence does not open an entry. The draft also
wrote a transient state into a skill ("one of them named for the repo **and behind**"), which
`skill-retro`:90-91 forbids. What shipped is the swap alone, net zero: the parenthetical now reads
"(which checkout that is comes from `~/.claude/pipeline/pool.json`'s `source_repo`)". Deleting a
false clause needs no bar — `skill-retro`:87-89 prefers it — so this is a patch, not a rule.

**SUPERSEDED the same day, by the owner, and the ledger supplies the refutation this pass missed.** Asked why the REMOTE was not the authority, the answer is that it should have been: `pool.json`'s `source_repo` cannot answer currency either, and **:1740-1741** records that on 2026-09-07 the stale checkout was **the pool's own** — 5 commits behind — while on 2026-09-13 it was the other. Which checkout is stale does not hold still, so naming a checkout at all is the wrong axis; **:1742** already said the identity does not matter ("both are working copies of one remote and every commit was pushed"). `skill-retro` 0.2.5 therefore compares against `git show origin/main:<path>` after a fetch, AFTER the push, and lets any clean checkout be the place you write. This retires the whole class rather than renaming it: a stale working tree can no longer be mistaken for parity, because the side you compare against is the one you just pushed to. Recorded as a proposer miss, not a gate miss — the gate was asked whether the rule half met the bar, and correctly said no; nobody asked whether the identifier was the right kind of thing.

**KILLED · P2's clause-(c) limb. PARKED at 1 record / 0 cost.** `#379`:29 records that GitHub's
`pull/<n>/head` lagged the pushed branch by one commit while `gh pr view --json headRefOid` and
`git ls-remote` had the new sha. Clause (c) is refused by **:1016-1021**, whose ruling transfers
word for word with GitHub in place of maven: the exception is "reflexive, possessive, singular", and
`pr-harden`:125-126 and `pr-harden-gate.sh`:344-377 contradict each other **only** once you import
the world-fact that the ref can lag. A gate catching such a sha is the gate working, not disagreeing
with the skill. The draft also carried an unchecked "so the round is lost either way" — #379 lost no
round, converged in 2, and `:29` is counterfactual ("**would have** reviewed the wrong sha").
**PARKED:** *`pull/<n>/head` can lag the pushed branch, and `pr-harden`:133-141's guard only fires
when the lag lands exactly on the previous round's sha — 1 record (#379/PR414), cost 0.*
**REOPEN ON:** a record where a lagging pull ref cost a round; or a lag of two or more commits,
which `:133`'s equality check cannot see; or a first round, where `reviewed_shas` is empty and the
guard has no antecedent.

**PARKED (refuter-raised) · `#379`:28's own remedy, which is not P1's and would not have been
covered by it.** "The cheaper move is to sweep a whole CLASS tree-wide the moment one instance is
found, which is what cycles 7 and 8 finally did." That is *class*-wide — all counts, all ordinals,
all positional pointers — the opposite direction of generality from P1's *subject*-wide rule, and
P1 would have prevented none of #379's cycles: c4's subject (a LAST-wins index claim) has no overlap
with c6's (a refusals enumeration), c7's ("is not a fourth case") or c8's (a readings count).
**PARKED at 1 record / 5 cycles (c4-c8).** It belongs to **:1264-1275**'s family and "one class" is
not "one subject". **REOPEN ON:** a second record where sweeping the CLASS on first sighting would
have collapsed several cycles, stated on the class rather than on the claim.

**PARKED (refuter-raised) · a reviewer's own evidence being false — 1 record, uncounted until now.**
`#374`:10 ("one of its two witnesses (GI bleeding) IS a free-text condition", cost 1 round) and
`#374`:11 ("Came from the REVIEWER's own evidence and would have been propagated", cost 0). The
nearest entry, **:1725-1731**, is about a **brief's** claims and is not reopened by this: the
subject there is a brief, here a reviewer's finding, and `:10`'s round was owed regardless until
zero blocking — the same discount **:1996-1997** applied. **REOPEN ON:** a record where a false
reviewer finding reached the branch, or cost a round that was not otherwise owed.

**Corrections to this pass's own parked arithmetic, all caught at the gate.**
- The submission proposed prose-correction "~33, plus #374 **and #294's continuation**". #294's
  continuation at `:167-171` and its first half at **:2281-2283** are **one event** — same ADR
  section, same terminating move, same "after the fourth refutation, not the second" — already
  counted in the 2026-09-13 block. Only #374 is new.
- Rate-limit: `#379`:4 says "one session rate-limit **interruption**", not an agent death, and the
  count is keyed on death. Only #374's Phase-2 agent counts. The entry is at **:2376-2377**; the
  submission cited **:2375**, which is the `git checkout` line.

**Shipped rules recorded working, no text proposed:** `pr-harden`'s cap raise on a shrinking-findings
signal (#294:111 — 4/5/4/1, zero re-raises, round 4's finding caused by round 3's fix, round 5
returned zero, "the signal test is what made it decidable rather than a judgement call");
`resolve-ticket`'s refutation gate withdrawing #379's plan v1 entirely before any code; `pr-harden`
FINISH not editing the cleared sha, in all three records; and `harden`'s deletion-over-rewording
rule, which ended both #374's and #294's loops — late in both.

**Two consecutive no-change retros ended here.** **:2379-2381** left open what that streak says about
the bar; this pass applied two changes, so the streak stands at 2 and the question is neither
advanced nor answered.

### Running parked counts (superseding the previous block where they differ)

- **Prose-correction cycles: ~32 records** — previous ~31 (**:2373**), plus #374 only ("Ended by
  deleting the CLAIM SHAPE, not by a fourth attempt", cost gate pass 2 + cycles 6 and 9). #294's
  continuation is the same event as its first half and is **not** counted twice. Remedy still killed;
  #374 is the shipped rule applied late, as #338, #310 and #294 were.
- **`git checkout -- <path>` losing uncommitted work: ~23 incidents / 19 records** — unchanged; no
  incident in this window. **:81**'s reopen still unmet.
- **Rate-limit / session-limit agent death: 21 records + 1 measurement** — previous 20 + 1
  (**:2376-2377**), plus #374's Phase-2 agent, which "retried with a leaner brief after the reset and
  it converged, matching the recorded pattern" — the 2026-08-31 P2a remedy working. #379's
  interruption is not an agent death and is not counted.
- **`pr-harden`'s round cap raised past its default: 4 records** — previous 3 (**:1881**), plus
  #294/PR410 (4 -> 5, converged at 5).
- **A retro applying no skill change at all: 2 records** — unchanged; this pass applied two.
- **`pull/<n>/head` lagging the pushed branch: 1 record / 0 cost** — new, above.
- **Sweeping a CLASS tree-wide on first sighting: 1 record / 5 cycles** — new, above.
- **A reviewer's own evidence being false: 1 record** — new, above.
- **resolve-ticket Step 3's "revise and re-run ONCE" undefined for a plan WITHDRAWN and replaced:
  1 record** (#379:27, cost 0, read as allowed). An ambiguity, not a contradiction. **REOPEN ON:** a
  second record, or one where the reading cost a gate pass.
- **A FINISH rule costing something real: 1 record** (#294:162-166 — a non-blocking 3-line
  robustness gap the verifier found goes to a follow-up rather than into the cleared sha). Distinct
  from the FINISH entry killed at **:1982-1990**. **REOPEN ON:** a second.
- **In a measurement or documentation PR the blocking findings are about CLAIMS, not code: 1 record**
  (#294:177 — all 15 findings and all 6 blocking ones, against a comment-only production diff).
  **REOPEN ON:** a second, with a remedy that is not "review the claims too".
- **The proposer not verifying its own citations: 28 cycles** — previous 25 (**:2384**). This pass
  added three, each cut at the gate: the `#294`:137 `cost: 1 round` that belongs to `:136`; the
  `#379`:18-22 "one claim" reading, reached by omitting `:20`; and the **:2375** line slip. A fourth
  is defensible — the #294 double-count in the submitted parked list — and is recorded above rather
  than counted, per **:2386**'s convention.
- **And the GATE mis-cited twice, and this pass propagated both before checking them: **:1024-1025** for the manufactured-self-contradiction ruling (it is **:1026-1028**) and **:1789** for `#379/PR382` (it is **:1749** and **:1881**). Caught before the commit, by re-resolving every pointer written into this block. A refuter's citation is owed the same check as a proposer's — **:2382**'s counter is keyed on the proposer and does not see this, which is why it is recorded here in prose rather than added to it.

## 2026-09-14 (3 records — #294/PR416, #409/PR417, #413/PR419) — 3 applied after revision, 1 killed, 2 parked; linter 10 files, 0 findings

Submission: `proposals/2026-09-14-retro-window-294-409-413.md`. One refutation round, one read-only
agent, never a fork. Six proposals, **none surviving as drafted**; the three that shipped are a clause
or a command apiece. Keyed by PR: `#294` here is **PR416**, a different run from the PR410 pair the
2026-09-13 blocks cover — different transcript (`8adeb630` vs `171b90dd`), and its own assumptions
section records PR410's measurement as already merged.

**KILLED · P-1 — `harden`:383, widen the prose-correction trigger from a CYCLE to a PASS.** Three
blocking objections, each re-resolved against the sources before this entry was written:
1. **The walk-forward.** The widened antecedent is *the findings are ALL in text an earlier pass
   wrote*, and it does not hold at the passes the proposal claimed a saving for. `#409`:16, :17 and
   :21 are code findings at P2/1, P2/2 and P2/3; `#294`:20 (a descriptor-TAIL guard disarmed by a rung
   below the widest) and `:21` (a vacuity precondition over the complement) are code at p2 and p3;
   `#413`:18 is eight mutations at p4/p5. The most its own evidence supports is ONE pass saved on
   #409 and none on the other two. This is the walk-forward that killed P3 at **:33-34**.
2. **It re-proposes a killed remedy.** **:2138-2141** — *"Delete the claim SHAPE early and wholesale
   rather than one claim per cycle … #338 already recorded this exact sharpening; the remedy stays
   killed."* A trigger UNIT is that remedy in different grammar.
3. **The pass-level form is already recorded and already counted.** **:2281-2283**, from #294/PR410:
   *"three consecutive Phase-2 **passes** each found false claims in prose the previous pass had just
   written … I reached for it late, after the fourth refutation, not the second."*

Worth keeping for whoever reaches this again: `#409`:27's own summary — *"five Phase-2 passes in a row
each found only prose defects"* — is falsified by that same record's findings list at `:16`, `:17` and
`:21`. **A run record's narrative line is not evidence against its own findings list**, and the
proposal took the narrative.

**APPLIED after revision · P-2 — `harden` 0.29.0, a mechanical trigger for the orphan sweep.** The
entry at **:1883-1887** wrote its own reopen: *"No proposal — the rule exists and is being skipped, and
more text is not the remedy. **REOPEN ON:** a mechanical check that could catch it, or a record where
the rule was followed and missed one anyway."* Both clauses are met — `#294`:19 is *"ORPHANED JAVADOC
created by the pass that swept for orphans"*, and a check exists. What ships is one `git diff -U1 |
awk` line that names every file where an insertion landed directly below a doc comment's `*/`, plus
the clause that a hit is a place to READ. Two blocking revisions at the gate: the drafted command
carried no revision range and so reads the WORKING TREE, which `harden`'s own *Commit before anything
mutates the tree* has already emptied — run bare on a clean checkout it printed nothing, which is
silence exactly when a pass would use it; and the submitted +4 lines of prose went into what is already
one of the largest bullets in the file, so it was cut to the command plus one clause, paid for by
compressing the three-run enumeration (#234/#229/#255 keep their runs and the silent-through-the-build
fact; the per-run insertion SHAPES go, because keying on position is what the check replaces them with).
Measured 2026-09-14 against `openmrs-module-chartsearchai`: names the file on `4099f6d9` and `9e9df1e8`
— the two commits whose successors are titled as fixing an orphan — silent on `278f474f`, and two lines
over 40 commits of `main`. The gate supplied the fact the submission lacked: one of those two is a LIVE
orphan, `ModuleSourceRoot.java`:41-54, where `apiRoot()`'s javadoc was stranded above `repoRoot()` by
`f2602c83` and shipped through a full `/harden` and a `pr-harden` round. The other, `9aca5790`, is a
false positive (a `//` comment between a javadoc and its member), which is why the shipped clause says a
hit is read and not a finding. No standing ruling bars an executable line here — `pr-harden`:476, :1022,
:126 and `harden`:195 all ship one — and **:1031-1034** and **:1063-1067** both prefer a mechanical
closure to a third prose remedy.

**APPLIED after revision · P-5 — `resolve-ticket` 0.16.0, do not spell `$MAVEN_ARGS` onto a command
line.** Two records of the mechanism (`#263`:29, `#409`:62) and one of the prescription. The
machine-fact ruling the submission worried about does **not** bite: **:1147-1151** and **:1826** are
about a hook path and a filesystem layout, while **:1068-1069** admitted this very fact — *"#263 was not
word-split by `zsh`" — **APPLIED***. The live objection was the other one, restatement (**:1468**), so
what ships is the imperative and a pointer to the measurement's existing home in `harden`'s
*A mutation that reddened nothing* bullet, not a second copy. Re-measured: `zsh -c 'V="-Dmaven.repo.local=/tmp/head -o"; set -- $V; echo $#'` → 1, `bash` → 2.

**APPLIED after revision · P-6 — `pr-harden` 0.22.0, do not ask an agent to re-derive evidence its
brief already carries.** **:1071-1072** parked this at 1 record (#238); `#294`:28 is the second, and it
closes at 2. The gate cut the placement and the claim: the submission put it in the 429 paragraph, but
`pr-harden`:994-996 already says a 429 *"is neither of those"* — neither a volume stall nor a nesting
stall — so a brief-leanness rule argued there is arguing the confound. And it does **not** prevent a
429: `#294`:28 applied the leaner brief and the reset window together, which the dead-phase contract
already discloses for #366 and #354, and nothing establishes the reviewer's own spend caused an
account-level limit. It ships at the brief-composition site instead, worded to bind Step 1's reviewer
brief as well as Step 4's fixer brief, with the non-claim stated.

**PARKED · P-3 — `pr-harden` is silent on where the FIXER works. Now 4 records, and the entry at
:2054-2061 says 2.** The submission claimed clause (c) and that limb is settled against it twice —
**:437-440** (*"That is unstated PRECEDENCE, not a document contradicting itself"*) and **:1018-1021**
(clause (c) is *"reflexive, possessive, singular"*). It also passed through a quotation that is not in
the skill: `#413`:24 attributes *"leave your work uncommitted in the worktree; the orchestrator
commits"* to `pr-harden`, and `#379`:27 attributes *"leave your edits in the working tree"* — neither
string exists in the file (the substance does, at Step 4's snapshot and COMMIT's one-commit-per-round).
The gate's own contribution is the count: **`#338`:29 was never assessed** and is the fourth record,
and it names the cause the other three do not — the orchestrator isolated the fixer *"to satisfy the
skill's own 'do not edit the worktree while an agent runs'"*. I read `pr-harden`:951-961 whole before
acting on the gate's suggested fix and did **not** take it: that paragraph scopes itself three times
over (*"Commit first, or wait"*, *"your own concurrent edits"*, *"before your OWN measurement probe"*),
so re-scoping it would narrow a sentence that is not actually ambiguous — the **:789-794** shape. What
remains is the gap **:2054-2056** already names, with both of its reopens still unmet after four
records: no run has `pr-harden` ITSELF telling the fixer to isolate, and none has cost a round or a
cycle (#250 a patch extraction, #338 *"worked every round"*, #379 1 step, #413 a patch). **Count
corrected 2 → 4** (`#250`:26, `#338`:29, `#379`:27, `#413`:24). **A third REOPEN, which the evidence now
suggests is the one that matters:** a record where the patch-across was NOT noticed, or failed — all
four recovered, which is why this has never cost a round and why prose has not been earned.

**PARKED at 1 · P-4 — the standalone is a process TREE.** `#409`:63 is real and the gap is real:
`pr-harden`:476 and `verify-frontend-change`:52 both poll `curl` for readiness, which a server the run
failed to kill answers, and step 5's omod timestamp and lib-cache hash measure the DISK, not the JVM.
But the second record does not exist. **:415-421** already ruled `#308`'s `mariadbd` *"**0 records as a
SKILL gap** … Recorded here so a later retro does not read it as a missing rule"* — which is exactly
what this proposal did — and the two are different halves anyway: #308 is the DB surviving after the
launcher and app were killed, #409 the child Tomcat JVM surviving the launcher. A sweep of all 70 run
records for the child-JVM half returns `#409`:63 alone. **REOPEN ON:** a second record of the
**child-JVM** half — a verifier whose readiness probe was answered by a server it believed it had
killed — or one that cost a round. If readmitted, ship only *confirm the port answers nothing before
relaunching*, at both homes.

**PARKED at 1 (gate-raised) · `harden`:158-160's open residue, and `#409`:58 is a candidate close.**
The skill says FM2-700's `target/` remedy *"presupposes isolation, so #293's half of this is not
closed"*, scoped deliberately per **:924-925**. `#409`:58 supplies a work-touched signal that
presupposes no isolation — *"watching something the agent's WORK touches (the standalone …) rather than
the transcript's size"* — in a run where the rule was RIGHT and the orchestrator doubted it, at a cost
of *"a duplicated measurement and a false line in a user-facing report"* and no round. **REOPEN ON:** a
second non-isolated work-touched signal, or a false-stall doubt that cost a round.

**PARKED at 1 (gate-raised) · a FIFTH failure mode for `pr-harden`'s *Editing by script*.** `#409`:59
— an assert correctly stopped the write, *"but `gh pr edit` was chained after it with `;` and pushed the
unchanged file anyway. Chain edits with `&&`, not `;`."* The four modes at **`pr-harden`:689-708** are
all the WRITE failing silently; this is the guard SUCCEEDING and the next command running regardless.
Never assessed here — `chained` and `&&` return nothing in this ledger. **REOPEN ON:** a second, or one
where the stale artifact reached a reviewer.

**Shipped rules recorded working, no text proposed:** `harden`'s deletion-over-rewording rule, which
ended #409's and #413's Phase-2 loops (both late, and #413 by the *KIND of question* half rather than
the deletion half); `harden`'s positive-control rule, which caught `#413`'s catch-breadth guard matching
any of nine sibling catches at cost 0; `resolve-ticket` Step 1's `gh issue view` warning, which `#409`:26
met at cost 0 because the skill named it; `pr-harden` Step 1's base-moved check, which `#409`:56 calls
*"the FIRST class it names"* against an ADR-number collision; `pr-harden` FINISH not editing the cleared
sha (#409); `gate-state --count-edits` printing its own no-recorded-head caveat and `#294`:27 answering
it; and the instruction-file byte budget at `pr-harden`:182-189, where `#409`:29 trimmed to make room and
`#413`:29 records an earlier decline on budget grounds as **wrong** and corrected inside the round —
**:1812-1813**'s sanctioned trim-then-raise path, working.

**A trap, flagged by the gate so a later pass does not mis-score it.** `#294`:24 — *"[pr-harden r1] The
only PR-round finding, and it was a false claim about what an assertion covers"* — reads like a second
record for **:2483-2489** (a reviewer's own evidence being false). It is not: the line sits under *Raised
by a fresh agent, missed by the author*, so the reviewer correctly found a false claim in the CODE. That
entry stays at 1.

### Running parked counts (superseding the previous block where they differ)

- **Prose-correction cycles: ~33 records** — previous ~32 (**:2513**), plus #409 and #413. **#294/PR416
  is NOT counted**: its record carries no prose-correction friction in *Where a skill blocked or
  contradicted this run*, and the PR410 instance is already inside the ~32 at **:2281-2283**. Remedy
  still killed at **:2138-2141**; P-1 above is that remedy in a new grammar and died the same way.
- **An insertion orphaning a javadoc: APPLIED mechanically** — the entry at **:1883-1887** is closed by
  P-2, on both limbs of its own reopen (`#294`:19 the rule followed and missing one, `#409`:18 a fourth
  instance, and a check that catches them). The residue the check cannot reach, named in the shipped
  text: whether the doc comment it points at still describes what now follows it.
- **`pr-harden` is silent on where the FIXER works: 4 records** — previous 2 (**:2056**), plus
  `#338`:29, never assessed, and `#413`:24. Three reopens now, above. 0 rounds and 0 cycles across all
  four.
- **The verifier's standalone surviving an incomplete kill: 1 record** (`#409`:63, child-JVM half) —
  new. `#308`'s `mariadbd` stays at **:415-421**'s 0 and is not the same half.
- **A brief asking an agent to re-derive evidence it already carries: 2 records — APPLIED** (`#238`:28,
  `#294`:28), closing **:1071-1072**.
- **`$MAVEN_ARGS` spelled onto a command line: 1 record — APPLIED** (`#409`:62) on the mechanism's 2
  (**:1068-1069**), as the imperative only.
- **Rate-limit / session-limit agent death: 22 records + 1 measurement** — previous 21 + 1 (**:2519**), plus `#294`:28's
  round-1 reviewer. The hook refusing the model lever is recorded working for the third time.
- **`--count-edits` answered by hand at every cycle: 4 records** — previous 3 (**:1893**), plus
  `#294`:27, the no-recorded-head fallback printing its caveat and the run answering it.
  **:1898-1899**'s reopens still unmet: the note was present and the wrong number never reached the gate.
- **A guard whose SUBJECT can be relocated: unchanged as a count**, but `#413`:13 is worth the pointer —
  a catch-breadth guard first written to match `catch (RuntimeException e)` anywhere in the FILE, so
  nine sibling catches satisfied it and narrowing the real handler stayed green, caught at cost 0 by the
  run's own positive control. The shipped rule finding its own case.
- **The proposer not verifying its own citations: 31 cycles** — previous 28 (**:2538**). This pass added
  three: citing **:32-39**'s *"Count: 2"* when the running block supersedes it at **:2513** (the
  **:1449** defect, again); citing **:1072-1073** for the fixer-brief entry, which is **:1071-1072**
  (:1073 is the model-override entry); and passing `#413`:24's quotation of `pr-harden` through to the
  gate as the skill's own words after having already grepped and found it absent. A fourth is recorded
  in prose rather than counted, per **:2386**: reading `#308` as P-4's second record when **:415-421**
  exists to prevent exactly that — a bar failure from not sweeping this ledger for my own citation,
  which is a different defect from mis-resolving a pointer.
- **And a mechanism for stale citations that nobody here had named: a skill's line numbers are
  invalidated by the edit shipped in the same commit that cites them.** Every `pr-harden` pointer in
  this block was written from the gate's reading of the PRE-edit file, and each below :326 was stale by
  ten lines the moment P-6 inserted a paragraph there — `:466` for the readiness poll is now `:476`,
  the *Editing by script* modes `:689-708`, the dead-phase contract `:994-996`, the do-not-edit
  paragraph `:951-961`. Caught by re-resolving every pointer against the POST-edit files, along with
  five slips that had nothing to do with the shift (`:2519`, `:1893`, `:1898-1899`, `#413`:13,
  `#409`:56). None reached the commit. The cheap rule, for
  whoever writes the next block: resolve skill-file pointers LAST, after every edit in the commit has
  landed — a record's line numbers are stable, a skill's are not.

## 2026-09-14 (second window of the day: 3 records — #421/PR423, #337/PR422, #276/PR424) — 2 applied after revision, 1 applied as a correction, 4 killed, 6 parked; two refutation rounds; linter 10 files, 0 findings

Records postdate the **:2545** block's own commit by hours, so the windows do not overlap. Three
records cannot corroborate much, and most of what they carry turned out to be existing rules
recurring rather than gaps.

**APPLIED as a correction · P0' — `pr-harden` 0.22.1, the marker-decides-ownership paragraph deleted.**
The one class Step 3 admits from a single sighting: a document contradicting itself AND its own gate
script. The deleted paragraph opened *"that marker now decides OWNERSHIP as well as attendedness"*,
while `pr-harden`:861 says ownership *"is not the unattended marker's job"* and :872 pins the measured
cost of the reading it re-proposed (*"a live foreign marker allowed EVERY block path"*). The gate
settles it: ownership is read off the ENTRY's `.owner` at `pr-harden-gate.sh`:203-217, whereas the
marker block writes only `UNATTENDED`, consumed at :268 **inside** `if [ "$AWAITING" -gt 0 ]` (:264) —
so the paragraph's own cited incident, a `phase: building` block, is decided before the marker is
consulted at all. Both suites pin the entry-based answer (`pr-harden/gate-test.sh`:80,
`harden/gate-test.sh`:65). It carried a SECOND stale claim nobody had flagged: *"still keyed on `$PWD`
alone"* against State's own opening, *"the WORKING TREE's physical path — `pwd -P`"*, and gate :90's
`KEY="$(pwd -P)"`. Two clauses were folded into the marker paragraph at `pr-harden`:902 rather than
lost — the ancestry walk (a live non-ancestor marker leaves this session ATTENDED, pinned at
`pr-harden/gate-test.sh`:88 and `harden/gate-test.sh`:75) and the indeterminate walk keeping the block,
whose ONLY home in either skill was the deleted text. Net −7 lines.

**APPLIED after revision · P2' — `harden` 0.30.0, the KIND-of-question rule promoted out of the
text-guard bullet** (now `harden`:275, pointer at :408 widened). Clause 2: `#421`:18/:20/:21 are three
cycles of one production predicate over TYPES escaping in turn (exact type equality, a raw type,
wildcard and type-variable bounds), each found by the next fresh agent. The rule's sentence sat inside
a bullet headed *"where the guard is over TEXT"* and :408 cited it as something it *"requires of a
guard"*, so on the shipped wording it reached neither a non-text predicate nor a production one.
Promotion rather than a third entry beside the TEXT and DATA bullets, because a third entry is what the
rule itself forbids. Two gate cuts: the standalone bullet lost its antecedent ("the loop",
"successive relocations"), so it now names its own population; and it carries a clause keeping it
distinct from *treat no list of relocations as closed*, per **:2426-2431**, by NAMING that rule rather
than locating it.

**APPLIED after revision · P4' — `harden` 0.30.0, name the direction a REPORTING check must never fail
in** (`harden`:251). Clause 1 only, two records, and the proposal says so: `#337`:15 (a carve-out
applied to both operands withdrew the record-side exit, so a FAITHFUL reproduction was reported —
blocking, found before the PR) and `#276`:17/:9 (`statesWord`, asserted to *"fail toward silence"*,
matched a fragment of a larger number and produced a false report on a published key). Not subsumed by
`pr-harden`:293-306 or `harden`:242-250, both of which are about a TEST guard's boolean — their
question ("is the OTHER value observed anywhere?") answers yes for both records and finds neither. Two
clauses cut by the gates: *"on input the suite already had"*, which was in neither record and was
carrying the non-subsumption, and the causal headline *"a fix that closes a miss moves the other one
too"*, which describes `#337` only. What ships is `#276`:17's own wording.

**KILLED · P1, a trigger for reaching the claim-shape deletion sooner.** **:2561-2563**, from the
block 11 hours earlier: *"It re-proposes a killed remedy. **:2138-2141** … A trigger UNIT is that
remedy in different grammar."* Third grammar, same remedy. **:2564-2566** shows the pass-level form
already counted at **:2281-2283** in the record's own words (*"I reached for it late, after the fourth
refutation, not the second"*), and `harden`:398-399 already ships the threshold. The proposal also took
`#276`'s narrative over its own findings list — the **:2568-2571** defect — since `#276`:21 (c3, a test
arrangement) and `#276`:22 (c6, counts outside the diff) are non-documentation findings inside the span
it called five documentation cycles, and `harden`:209-210 refuses to classify those as documentation.

**KILLED · P1-R, the surviving routing clause naming *Don't rewrite prose faster than you verify it* in
`harden`'s documentation-pass conditions.** Derived from round 1's own suggestion, killed by round 2 on
three grounds, any one sufficient. It stated no bar clause. The site (`harden`:199-201) governs only a
cycle confirmed by a single agent, and the ONLY record naming such a cycle is `#337`:22, where that
route is recorded ENDING the run — so the clause would not have been read by either run it was
justified from; `#276` never used the classification. And the routing precedent at **:2435-2438** points
the other way here: `#337`:24 says what finished its hardest sweep was *"enumerating the claim's
SUBJECT"*, which `harden`:199-201 already reaches through the rule it names "in full". Its fold also
broke its own sentence — four obligations under a label of "three things", invisible to
`skill-lint.py`, whose docstring names that exact class but whose check only fires on a markdown list.
**REOPEN ON:** a record of a documentation-classified single-agent cycle that shipped a false claim the
deletion discipline would have caught.

**KILLED · P3', the write-side anchor for an orphaned javadoc** ("anchor after the preceding member's
closing brace, never on the following member's signature"). **:1883-1885** is a standing refusal aimed
at this family and this bullet — *"the rule exists and is being skipped, and more text is not the
remedy"* — and `#276`:29 is another instance of the rule being skipped (*"the failure my own memory
entry records"*), not of it being followed and missing one. Its reopen was consumed by the mechanical
check at **:2684-2687**. **:2579-2585** records that the same bullet was trimmed to "the command plus
one clause" eleven hours earlier, paid for by compressing its three-run enumeration; P3' put +3 lines
of prose back into it and named no compression. And the prescription over-generalises its one record:
"the preceding member's closing brace" has no referent for a field, an enum constant, or the first
member of a type, each of which orphans a javadoc the same way. **REOPEN ON:** a record of a run that
ran the awk check, or followed *read the neighbours*, and still shipped an orphan — and if it ships it
must state the invariant (never insert between a doc comment and the member it documents) rather than
an anchor, and name the lines it pays for.

**KILLED · P5, an INBOUND-premise bullet in `harden` Phase 1** ("is the standing claim you are BUILDING
on true?"), the class round 1 certified as missed. Four grounds. Its premise — *"nothing asks the
opposite question"* — is false in three places, and this is the **:1693-1709** defect recurring:
`harden`:59-62 asks it and its worked case is *the TICKET's* own offered sizing ("0 of 36 reachable")
caught at the gate before code, the same artifact class and outcome as `#421`:8; `resolve-ticket`:166-169
(*"a measurement in a comment outranks a claim in the body"*, with a second home at :640); `resolve-ticket`:287-297 Q6 (*"Does the
plan rest on a claim about the DATA that nobody has measured? … ask for the count"*), which is exactly
`#276`:8. Two of its six cited lines are the OPPOSITE direction — `#421`:16 is the author's own claim
refuted by a standing javadoc (`harden`:32's population), and `#337`:12's control band was published by
this very run, its resolution being `harden`:395-399 working — and `#337`:12 was the sole citation
carrying clause-2 cost, so clause 2 collapsed. By the records' own fields the genuine inbound instances
cost nothing (`#421`:8 "gate pass 1 (no code)", `#337`:8 and :11 "cost: 0"), leaving `#276`:8 at one
harden pass: one record, already reached by two existing instructions. Placement was wrong too —
`harden` contains **zero** occurrences of "plan", having no planning phase, and a ticket premise is not
in the population *Trace outward* enumerates. **REOPEN ON:** a record where a standing IN-TREE claim —
not a ticket premise, not the run's own publication, and not a data claim `harden`:59-62 or Step 3 Q6
already reach — cost a harden cycle or more.

### Running parked counts (superseding the previous block where they differ)

- **Prose-correction cycles: ~35 records** — previous ~33 (**:2680**), plus `#276` (five cycles of
  documentation counts by its own narrative, four by its findings list) and `#337`:18 (six passes, cost
  4). Remedy killed at **:2138-2141**; trigger units killed at **:2561-2563** and again above.
- **Rate-limit / session-limit agent death: 24 records + 1 measurement** — previous 22 + 1
  (**:2697**), plus `#421` (all four cycle-3 Phase 2 agents killed mid-flight) and `#337`. Both resumed
  from disk with nothing lost under the dead-phase contract; no proposal, and they are evidence the
  contract works rather than of a gap.
- **A test pinned from one side only, defeated by a TIGHTENING: 1 record** — new. `harden`:235's
  mutation list is exhaustive-shaped (*"deleted, its arms swapped, its comparison **loosened**, or
  rewritten"*) and `#337`:16 is `>=`→`==`, blocking, 1 round, with 2140 tests green. Round 1 called
  this the sharpest of the parked items. Below both clauses. **REOPEN ON:** a second record, or one
  costing two rounds.
- **A mutation loop that silently ran nothing: 3 records for the family, 1 for the mechanism** —
  `#421`'s macOS `timeout(1)` absence, where the `||` fallback never fired because grep exits 0 on
  empty input, joins #256 and #263 at `harden`:236-241. Deliberately NOT added: the neighbouring rule
  says what ends such a loop is a change in the KIND of question, not another entry on the list.
- **A positive control before reporting an ABSENT signal at runtime: 1 record, cost 0** — `#421`'s
  verifier established a logger positive control before reporting absent WARNs, *"unprompted by the
  brief's wording"*. The brief does not ask for it. `harden`:242-250 carries the test-side rule.
- **A measurement taken from an agent's report and never re-run by the author: 1 record** — `#421`:11
  (*"recorded from an agent report, never run by the author … unguarded it reddens 10 of 13"*), 1
  cycle. In tension with `pr-harden`:326-334 (do not ask an agent to re-derive evidence its brief
  carries), which is a rule-versus-record tension and not a document self-contradiction, so clause 3
  does not reach it. Raised by round 1.
- **The proposer not verifying its own citations: 45** — previous 31 (**:2716**); this pass added 14,
  which is what two refutation rounds over six proposals cost. Three wrong figures (`#421`'s cascade
  called four cycles when its own lines say three; "46 lines above" for 57; the enumeration and the
  "two questions" inverted). A citation filed in the wrong family (`#421`:22 belongs to the
  claim-shape record, not the widening one). An overstated corroboration claim ("both runs name
  Termination as the section they were fighting", false of `#337`:22, which names it favourably). A
  narrative taken over a findings list. An unsourced clause carrying a proposal's whole
  non-subsumption. A causal headline stated over two records and supported by one. A proposal with no
  bar clause at all. A negative-coverage premise asserted without grepping for it — the **:1693-1709**
  defect. Two records mis-cast as the proposal's own class. A round-2 revision silently dropping a
  clause round 1 had itself named as unhomed, which is the one a reader should find most alarming: the
  gate had to catch the same proposal twice. And a budget figure left describing a superseded set.
- **And a note for whoever writes the next block, since **:2716-2723** asked for one:** its rule
  (resolve skill-file pointers LAST) worked. Every `harden` and `pr-harden` pointer above was resolved
  against the POST-edit files, after the P0'/P2'/P4' edits had landed — which is why they read :251,
  :275 and :408 rather than the :235, :268 and :396 the proposals were written against.

## 2026-09-14 (third window of the day: 5 records / **4 runs** — #409+#426 share one transcript, #412/PR427, #425/PR428, #315/PR431) — 3 applied after revision, 2 killed, 1 parked; two refutation rounds; linter 10 files, 0 findings

**The window's counting trap, stated first:** `2026-09-14-…-409.md` and `2026-09-14-…-426.md` both
name transcript `…-chartsearchai-409/0102617e-5075-4baa-9afe-3a29fc459eab.jsonl`. They are ONE run —
the condition-3 abort and the PR the same session went on to open. Everything they share (the
"ABSENCE of prose" refutation, "3 of 7", `ClassCodeFidelityCheck is already marker-anchored`, the
empty `gh issue view --comments`, the abort-delivers-nothing complaint) is one sighting. Both gates
confirmed it independently.

### Applied

**P1 · `harden`:236 — a mutation loop's clean result is believed only after the loop has produced a
RED.** Bar (a): `#425`:34 and `#426`:36, two runs, one mechanism. The revisions are the interesting
part:
- The original warrant — "a positive control is blind to which mechanism broke the loop" — is FALSE
  of one of its own two records. `#426`:36's selector "makes surefire run nothing and report BUILD
  FAILURE", and a control confirmed by a red is satisfied by that very failure. The shipped text
  names the false-RED direction as residue instead of claiming the control closes it.
- It does NOT claim `#425`'s round. `#425`:29 assigns that round elsewhere in the run's own words.
  Both selector incidents cost 0.
- The comma was over-sourced to two runs; only `#426`:36 mentions it. **Re-measured by this retro**
  — 2026-09-14, surefire 3.5.5, chartsearchai `api`: `-Dtest='A+B'` → BUILD FAILURE, *"No tests
  matching pattern … were executed!"*, 0 tests; the same pair comma-separated → `Tests run: 20`,
  BUILD SUCCESS. The shipped figure is that measurement, not a record's.
- **:1604-1606** requires a positive-control rule to state its trigger; the shipped clause triggers
  on a zero that would license deleting the clause, and keeps *"ask of it what its inputs could not
  have produced"* beside it. (That entry's own `harden`:231 pointer no longer resolves — the line is
  now the cycle-gate's "cheap to satisfy and expensive to fake" — which is **:2716-2723**'s hazard
  seen from the other end: a skill's line numbers are invalidated by every later commit, not only by
  the one that cites them. Name the sentence.)
- **Deliberately NOT shipped:** `#425`:34's `-DfailIfNoTests=false` vs
  `-Dsurefire.failIfNoSpecifiedTests=false`. Verified true — and surefire's own error text names the
  right flag, so the tool tells the run and the skill need not.
- **Prunes** *"read the build output rather than the test count"*, retired by the two records: both
  read the output, in opposite directions, and neither read it as "the mutation never ran".
- This closes **:2838-2841** on the terms that entry set. It asked for a change in the KIND of
  question rather than a sixth mechanism; a control needs no correct reading of the output, and the
  mechanism list (`#256`, `#263`, `#421`, and this window's selector) is not extended.
- Checked for collateral staleness: `harden`:273's *"the four mutations above"* still counts :235's
  list, which this edit does not touch. Found only by grepping a phrase that does NOT straddle the
  line-wrap — the **:2080-2086** hazard, reproduced live again.

**P4a · `pr-harden`'s *Correcting a claim* section gains a POINTER to `harden`'s subject-enumeration
remedy.** The two copies had drifted: `harden`:408 carries *enumerate the claim's SUBJECT* (from
#374), `pr-harden`'s section ended at "treat no list of those mechanisms as closed". Bar (b) inside
one run — `#426`:20 ("a home in `LlmInferenceService` the author's six-file sweep missed · blocking ·
cost: 1 round") and `#426`:23 ("A SEVENTH home … cost: 1 round") — and bar (a) with `#315`:27.
Shipped as a pointer, not a paraphrase, per **:764-765**; a paraphrase would be the second home P4b
prohibits. **Correction to the proposal's own bookkeeping:** it billed `#426`:24 in both directions,
once as evidence the remedy was missing from `pr-harden` and once as a shipped rule working. One
reading survives: the fixer reached a remedy `pr-harden` does not name, three rounds after the sweep
first cost a round.

**P4b · `harden`:408 — where a claim already has a home, correct that home and point the others at
it.** Bar (a) for the DEFECT (`#315`:37, `#412`:20). Gate 1 established that the REMEDY has one
record and that this same window measures its cost, so the shipped text carries both:
- `#412`:19 — "a dangling cross-file pointer; then a SECOND one, self-referential … introduced by the
  fix for the first · cost: 2 passes". The most expensive single item in that record is the remedy
  being proposed, and the proposal had not cited it.
- `#412`:12 and `#425`:39 — an instruction file at its byte budget cannot take a pointer at all.
- The universal ("the fix is ONE home … not N corrected copies") was cut; `harden`:400's grammar rule.

### Killed

**P2 · `pr-harden`'s 429 paragraph: "when the attempt is the WAIT, re-dispatch the briefs unchanged."**
Killed twice, both citations settling.
1. **Wrong document — and this submission was refused here once already, for this reason.**
   **:1586-1591**: *"Both cited deaths are **harden** agents, not pr-harden phases … so :1289-1290's
   standing precedent … refuses them in the pr-harden paragraph. Its real home is the parked harden
   Phase 2 entry at :1258-1260."* `#412`:25 opens "harden:Phase 2 —"; `#425`:33 says "cycle-2 Phase-2
   lenses". Same shape, newer deaths.
2. **:2606-2608** — *"the submission put it in the 429 paragraph, but `pr-harden`:994-996 already
   says a 429 'is neither of those' … so a brief-leanness rule argued there is arguing the confound."*
   A do-NOT-lean rule is a brief-leanness rule.
Residue: `#425`:33 is not a second witness for the proposed rule. It records another confound
("cannot be separated from it"), not a second observation of re-dispatch-unchanged succeeding. On the
rule as worded `#412`:25 stands alone, at 0 rounds.

**P3 · name the `gate-state … --override --reason` invocation in `resolve-ticket`'s abort paragraph.**
`#409`:30 is real and the script check is exact (`gate-state`:217 `action="store_true"`, :218
`--reason` separately). Killed on clause (c) and on the remedy:
- `grep -- "--override" resolve-ticket/SKILL.md` returns nothing, so the skill states no shape and
  cannot contradict the script — it UNDER-SPECIFIES. **:2615-2617** settles that limb twice over
  (**:437-440** "unstated PRECEDENCE, not a document contradicting itself", and **:1018-1021**).
- `resolve-ticket`:77 already says *"see **State** in `pr-harden`, which owns the format"*, five lines
  above the abort obligation, and `pr-harden`:1020 carries the shape. The correct call was one named
  pointer away: the rule exists and was skipped, and **:1885-1887** / **:1196-1198** both rule that
  more text is not the remedy for that. Writing the shape in would also be the second home P4b
  prohibits.
- One record, cost ≈ 0 (an errored first invocation, corrected). **REOPEN ON:** a second record, or
  one where a wrong shape reached the gate rather than erroring.

### Parked

**P5 · a numeric BOUND defeated by MOVING it — the owed-mutation list names one direction.** Proposed
after gate 1 was dispatched, and gate 2 (fresh) parked it. `#425`:24 and `#337`:16 are distinct runs
(different transcripts, tickets and PRs) and both are blocking at one round, so **:2833-2837**'s
reopen looked met. It is not, and the reason is an ellipsis in the proposal itself:
- `#425`:29 whole reads *"harden Termination lists 'its comparison loosened' among the owed mutations;
  I ran only deletions/disablings. **That exact omission is what round 1 caught, one round later.**
  The list was in front of me."* The proposal quoted it with that middle sentence elided. It says the
  round-1 defeat (`#425`:24) and the skipped list entry are ONE sighting — of the rule being SKIPPED.
  **:2792-2796** killed a proposal on exactly that distinction, and **:432-436** is the standing
  ruling on an ellipsis that removes the deciding clause.
- A second ellipsis, in the proposal's quotation of `#425`:26, removed *"(tightening to `< 5`)"* —
  the words that contradict the proposal's own sentence calling `< 4` → `< 3` a tightening.
- The nearest text is uncited and is in the very bullet the edit would extend: `pr-harden`:287-289,
  *"a mutation result measures the arms it moved, not the mechanism — one run published a
  'byte-identical' result for removing a scan bound, which held for the one arm it ran and failed for
  the other, at a cycle and a round."* A bound, one arm exercised, at a cycle AND a round. Either
  that is the family's home (so P5 opens a second one, against P4b shipped in the same commit) or it
  is an uncounted record.
- "Move it one step in EACH direction" also under-covers its senior record: one step on `>= 3` is
  `>= 2` or `>= 4`, never `#337`'s `==`.
**REOPEN ON (sharpened, replacing :2836's):** a second record where the owed mutations WERE run and a
moved bound still went unnoticed, or one costing two rounds. Without that sharpening the next window
re-submits this on the same evidence.

### Running parked counts (superseding the previous block where they differ)

- **Prose-correction cycles: ~39 records** — previous ~35 (**:2826**), plus all four runs of this
  window (`#425` cycles 2-5, `#409`/`#426` rounds 3-5, `#412`'s dangling pointer whose fix introduced
  a second, `#315`'s four cycles over two claim families). Remedy still killed at **:2138-2141**.
- **Rate-limit / session-limit agent death: 26 records + 1 measurement** — previous 24 + 1
  (**:2829**), plus `#412` and `#425`. Both resumed; still evidence the contract works.
- **`harden` Phase 2 carries no retry contract of its own: 6 records** — **:1258-1262**'s 2, raised to
  4 at **:1591**, plus `#412` and `#425`. Its **REOPEN ON** (a remedy answering a limit that refuses
  every retry for hours) is still unmet by both: neither offers a remedy, only a witness. This is
  where P2's evidence belongs, and it is banked here rather than argued in `pr-harden`.
- **A mutation loop that silently ran nothing: CLOSED by P1** (**:2838-2841**), on the KIND-change
  terms that entry set. The mechanism list was not extended.
- **A test pinned from one side only, defeated by a moved bound: still 1 record** (`#337`:16).
  `#425`:24 is NOT a second — see P5 above; it and `#425`:29 are one sighting of a skip.
- **A ticket that GAINS a material comment mid-run: 1 record, cost 1 round.** `#426`:19 — the reporter
  posted the raw SSE two hours before the PR opened and the author's Step 1 read predated it.
  `resolve-ticket` Step 1 says to read the comments; nothing says the read expires. **REOPEN ON:** a
  second record, or one costing two rounds.
- **A sentence in the repo's own MERGED prose taken as true: 4 runs, not proposable as drafted.**
  `#409`:10-11 is the sharp one (a false sentence in merged ADR Decision 94 "propagated straight into
  a new plan", and its "two sibling keys" was three, "stale within one day of being written") — but
  cost 0, caught at plan time. The other three (`#412`:9, `#425`:15, `#315`:13) are the parked
  prose-correction family in a new coat. **REOPEN ON:** a record where a claim inherited from a merged
  artifact reached code, or cost a round.
- **Abort condition 3 delivers nothing, discarding what the stopped run established: 3rd record**
  (`#409`:26-28), joining **:1888-1892**'s two. Reopen ("a record where the missing outcome cost a
  round, or produced an abort the skill should have handled") **still unmet**: the same session
  resumed, PR 426 merged, nothing was re-derived. Both records say the rule fired correctly.
- **The instruction file at its size budget blocking a rule: 2 more records** (`#412`:12, cost 1
  build; `#315`:28, "the fourth raise, and the third in eight days"). `pr-harden`'s third class owns
  the merge-consumed form. The residue both name — the guard's javadoc advises a SPLIT and the advice
  is recorded rather than taken — is a decision in the target repo, not in a skill.
- **`gate-state declined` is append-only: 1 record** (`#425`:32), asking for `--clear-declined` after
  writing a placeholder row the skill never asked for.
- **`git checkout -- <path>` discarding the ORCHESTRATOR's own uncommitted work: increments only**
  (`#425`:35). **:2134-2137** keeps the remedies killed; its reopen (an incident where "commit before
  probing" WAS followed) is still unmet — `#425` did not commit first.
- **The proposer not verifying its own citations: 56** — previous 45 (**:2850**); this pass added 11
  across two gates. P1's central warrant false of one of its own two records; the comma over-sourced
  to two runs when one names it; P2's two harden deaths cited into a pr-harden paragraph this ledger
  had already refused for that exact reason; P4a's one citation billed in both directions; P4b's
  remedy stated over a record that measures its cost, uncited; two off-by-one ledger spans
  (:2137-2141 for :2138-2141, :2134-2136 for :2134-2137 — and note BOTH were "corrections" I made to
  citations that had been right the first time); P5's `#425`:31 for :29; and P5's three: the ellipsis
  that removed the deciding clause, the ellipsis that removed the contradicting parenthesis, and the
  uncited nearest text. Not counted, because they never reached a gate: seven wrong record line
  numbers in the first draft, swept before dispatch by grepping every quoted phrase — which is the
  cheap check, and it found half the citations wrong.
- **A note for the next block:** **:2716-2723**'s rule (resolve skill-file pointers LAST) was followed
  again — every `harden` and `pr-harden` pointer above was resolved against the POST-edit files. And
  add one: a phrase that straddles a line-wrap does not grep, which cost a false "that tally is gone"
  here before the flattened search found `harden`:273. **:2080-2086** predicted exactly this.

## 2026-09-16 (1 record — #435/PR436, the store's **first standalone-`harden` run**) — 0 applied as drafted, 1 applied as a correction, 1 killed, 6 parked; linter 10 files, 0 findings

**KILLED · P1, stating the standalone-`harden` capture gap in `skill-retro` Step 1.** Bar (c) claimed
and not met, on three grounds any one sufficient. `skill-retro`:10 already enumerates the capturing
skills — *"`resolve-ticket` and `pr-harden` each append a run record when they finish"* — so Step 1's
sentence is incomplete, not false, and its coverage claim is bounded two paragraphs earlier.
**:825-828** has already ruled this shape: an absence in `harden` cited against another skill's
sentence is *"a **GAP, not a contradiction**"*. And deciding whether the omission matters needs a fact
about the world — whether `/harden` is ever invoked outside the two — which is what **:66-68** excludes
from clause (c). At 1 record and 0 cycles lost, clauses (a) and (b) fail too.
**Its second sentence was worse than unchecked and the refuter measured it.** P1 asserted the corpus
under-samples standalone harden runs *"which is where the prose-correction class is heaviest"*. Every
record behind the prose-correction count names `resolve-ticket` or `pr-harden` with harden nested, so
the standalone sample size is **1** and the comparative is unavailable from the corpus — while the
corpus's actual weight sits on the nested path. It also carried *"only"* and a superlative into a
proposal, which is **:856-859**'s five-retro streak of submitting wording that breaks the
counts/universals rule being enforced.
**And it would not have prevented what it cites:** it tells a later retro how to read the corpus; it
cannot make a standalone run write a record. **REOPEN ON:** a second standalone-`harden` record, or one
where the missing capture costs a retro a re-derivation.

**APPLIED as a correction · `skill-retro` 0.2.7, Step 1.** *"one per finished run"* has been loosely
false since 2026-08-24: six files in the store are measurement or defect notes rather than run records
(`2026-08-24-pipeline-timing-measurement`, `2026-08-26-pwd-keyed-gate-false-positive`,
`2026-08-26-unattended-yield-kills-the-run`, `2026-08-27-retro-authored-hook-regression`, and two
`pr-review-dimension-fanout` measurements), and this window adds a seventh kind, a hand-written
capture. One clause: a file count is not a run count, read each file's header, and a standalone
`/harden` writes none. Precedent for applying a correction against a killed proposal: **:2388**,
**:2725**.

**The reopen at :2789-2790 FIRED and is ANSWERED here, so the next window does not re-derive it.**
Its condition — *"a documentation-classified single-agent cycle that shipped a false claim the deletion
discipline would have caught"* — is met word for word by this run: cycle 7 rewrote the
`assertCarriedWhole` paragraph and shipped *"This is the assertion that reddens on it"*, refuted by
measurement at cycle 8 (`git show 7a080eed` carries both wordings). The routing clause stays killed
anyway, on a ground at the site rather than the one first drafted: `harden`:199-201's third obligation
already requires the confirming agent to RUN any claim about behaviour, and running the mutation is
exactly what refuted this one — so it is non-compliance with a shipped condition, not a missing one,
and **:2786** already ruled the site *"already reaches"* the rule P1-R named. The first draft of this
entry suppressed the reopen on **:25-27**'s logic instead (the failure was the orchestrator's, not an
agent's), which the refuter killed: the record files the failure at the route and says nothing about
who typed the edit, so that premise was not in evidence. **REOPEN ON:** a documentation-classified
cycle that shipped a false claim *after* its behavioural claims were run.

**Parked.**
- **Prose-correction cycles: ~40 records** — previous ~39 (**:2987**), plus this run. First
  standalone-`harden` instance in the store, and the sharpest single case: **12 of 19 harden commits
  changed zero non-comment `*.java` lines**, and the fix itself was written before cycle 1 and never
  changed again. Both rules that would have ended it early were already shipped — `harden`:401
  (delete, don't reword) and :406 (the universals list) — and both were violated until cycle 7, when
  deletion plus enumerating the claim's SUBJECT stopped it. Remedy still killed; the shipped rules are
  the remedy. The draft of this entry took its figure from **:1152**'s superseded 16, which is
  **:1488**'s named defect (*"a parked count taken from a superseded block"*), and a "33 lines of code"
  figure that did not reproduce under three countings was dropped rather than published.
- **A mechanical detector for the self-rewrite signature: attempted, not calibrated.** Written and run
  over this run's own history (`proposals/2026-09-16-selfrewrite.py`): comment-only diff, removing
  comment lines an earlier commit of the same run added, adding comment lines in their place. Measured
  **4 true positives, 2 false positives, 1 false negative** — it flags cycles 8 and 9, whose edits were
  pure deletions, because cutting a clause out of a wrapped comment reflows its neighbours and that is
  indistinguishable from a replacement at line granularity; and it exempts cycle 7, which carried one
  code line and is the cycle that shipped a false claim. A third failure the first draft missed: its
  `is_comment` treats every line of a `.md` file as code, so an ADR-only prose cycle is silently
  exempt, and this run's prose had ADR homes. A word-granularity version might separate a reflow from a
  replacement. Recorded so the next attempt starts here. The standing *"probably mechanical, not
  textual"* note at **:30** is the `git checkout --` entry's, not this class's — the first draft
  borrowed it, which is **:853-855**'s citation defect; this class's own note is **:2989**.
- **N consecutive single-agent cycles each independently reporting code convergence while the cycle's
  only edits are prose: 1 record** — cycles 6-9 here, four of them. Parked against **:1444**'s standing
  reopen, *"a spin signal, not a cap"*, as the first candidate signal any record has supplied, and
  distinct from another cap or trigger (killed at **:2770-2777** as *"third grammar, same remedy"*). No
  remedy proposed. **REOPEN ON:** a second record of the shape, or one where the signal is available to
  a gate rather than only to a reader.
- **A test pinned from one side only, defeated by a moved bound: 3 records** — previous 1 (**:2998**,
  `#337`:16), plus two from this run: widening the terminator set to `\R` passes the whole suite while
  turning a form feed in a clinician's answer into a newline, and dropping the `-1` from the framing
  split deletes a trailing line break with the whole suite green, because no payload in the class ended
  with a terminator. Whether **:2981**'s sharpened reopen (*"the owed mutations WERE run and a moved
  bound still went unnoticed"*) is met is **undecidable from this record**: it says a fresh agent found
  both, not whether the author had run the owed mutations. Recorded as undecidable rather than counted
  either way.
- **Cross-session interference on a live run's shared state: 4 records** — *corrected by the second
  window of 2026-09-16, which measured it: this entry was written as "3 records — previous 2
  (**:811-824**)", re-deriving "previous" from the base entry and dropping **:958**'s increment to 3
  (`#297`:43, "Third distinct harm and a third surface"). That is **:1488**'s named defect, the one
  this same block caught itself committing for the prose-correction count at **:3090-3092**.* At
  retro time the shared chartsearchai
  clone was on another pipeline run's branch with that run's commits on it, while this run's branch and
  remote were intact. **Harm realised: none** — worth recording beside the two destructive sightings,
  since it is the first of the class that cost nothing. After-the-fact capture: no part of the run
  observed it, and the record labels it so, per **:387-395**.
- **Concurrent maven in one checkout: 1 record.** A foreground `mvn -pl omod test` run while a
  background `mvn clean install` was in flight in the SAME tree invalidated the background build, which
  cleans the module the foreground run compiles into. Cost: one build re-run, and a moment of treating
  its green as evidence. No prior sighting in this ledger.
- **`-Dtest=A+B` selects nothing and reports BUILD FAILURE:** this run is another sighting, adding to
  **:2887-2890**. It silently voided one mutation measurement until re-run with a comma.

**A note for the next block:** every ledger line number quoted above was resolved against the file
before writing, after the refuter found the draft's `:30` citation pointing at a neighbouring entry —
the same cheap check **:3036** prescribes, and it found the one wrong citation in this draft.

## 2026-09-16 (second window of the day: 1 record — #439/PR440, mirrored at 9a08df8 and explicitly not read by the 22:45 block) — 1 applied after revision, 2 killed, 2 corrections applied to the store, 5 parked; linter 10 files, 0 findings

**APPLIED after revision · `harden` 0.32.0, `pr-harden` 0.24.0 — a control measures the HARNESS it
ran in, not the property.** Bar **(b)**: rounds 2, 3 and 4 of `#439`, one round each, every one a
positive control that EXISTED and was green while the thing it forbids reached the log. The parent
rule — *"Build the case it exists for and watch it FAIL"* — was obeyed: `efccb372`'s own message says
*"Its control is what makes that a defect rather than a doubt — restoring the pre-fix label does
redden the case"*. Three axes the control did not move along: a capture raised one class's logger to
WARN (`info` passed), the shared helper's `describeAll` rendered a throwable's TYPE alone (a
diagnostic exception passed), and a sibling capture's leftover `LoggerConfig` held the guard under
one surefire run order. All three fixes verified in the tree, not just in the record.
- **Written against `harden`'s *Residue the control does not close*, which the draft claimed did not
  exist** (*"Nothing states it for a control"*). It does, two lines above the bullet edited, for the
  opposite direction — a harness's spurious RED. The shipped text names it as the mirror.
- **The bar-(a) leg was DROPPED.** The draft cited `#435`:`[c1]`, a second LF-only frame decoder in
  the test package, as corroboration. It is not a positive control and the record never calls it one;
  it is *a second definition*, which `harden`'s *ask whether the thing you fixed has a SIBLING*
  already reaches. Same ground as **:3077**. The RE-IMPLEMENTATION clause built on it went with it.
- **Dropped: *"One probe is evidence for ONE site"*** — the draft's own universal, sourced to
  nothing, over an incident the record costs at 0 rounds. The axis it names is already carried by
  `#360` in the parent bullet.
- **Added, from the refuter's sweep of the record the draft had already mined:** `[r4]`'s liveness
  precondition, *"passes while the negative it protects is vacuous"* — a sixth instance of the
  proposal's own class, in the proposal's own record, missing from its evidence list.
- **Reachability, decided after the draft:** `pr-harden`:261 says the fixer's brief *"carries
  harden's Phase 1 discipline:"* followed by the bullets, so a rule written only in `harden`'s
  Termination is not in a `pr-harden` fixer's brief — and `#439` is a `pr-harden` run whose guards a
  fixer wrote. The operative sentence went into `pr-harden`'s *supposed to stay GREEN* bullet too,
  which is that skill's existing pattern for this rule.
- **Prune: none of comparable size, stated plainly rather than manufactured.** The one real cut is
  `pr-harden`'s *"carries the measurements (#360, #355)"* enumeration, ten characters, which this
  edit makes wrong and which is the rotting kind. Net **+13 lines**, justified: the parent rule was
  in force, was obeyed, and cost three rounds in one run anyway.

**KILLED · P2, a monotonic parked-count check for `skill-lint.py`.** The proposal reported the check
*"calibrated in both directions on the live ledger"* — 39 classes, 7 repeated, one flag, and that
flag the genuine `:3119` defect. Every one of those figures reproduces. **The calibration was still
false, because the sample was half the corpus.** This ledger writes a parked count in TWO formats:
`- **Class: N records**` (60 entries) and `- Class: **N records**` (66 entries, the earlier blocks'
form). Over both — 82 classes, 16 repeated — the same rule fires **13 times, 12 of them legitimate
restatements** of standing counts in the early blocks. `skill-lint.py`'s own removed positional-
cross-reference check is the precedent and the bar: *"A guard whose output is mostly noise gets
learned-around rather than obeyed, which is worse than not having it"* — that one was 3 noise in 4,
this is 12 in 13. And the defect it MISSES is the one it was written for: `:2255` cites *"previous 17
(:2098)"* where the 17 is at `:1879`, and `:1872` cites *"previous ~24 (:1636)"* where the ~24 is at
`:1641` — both **:1488**'s class, both invisible because N rose. The proposal recorded that coverage
hole against the variant it REJECTED and never applied it to the one it recommended. Two figures in
its own prose do not reproduce either: the sequence *"17→19→20→21→22"* spans a class RENAME (`Rate-
limit agent death` → `Rate-limit / session-limit agent death`) and continues 24 and 26.
**REOPEN ON:** a check calibrated over both formats, with the `unchanged` convention retro-fitted to
the twelve first, that detects the wrong-`previous`-pointer defect rather than only the flat-N one.

**KILLED · C2, renaming the record's `-DfailIfNoTests=false` to `-Dsurefire.failIfNoSpecifiedTests=
false`.** The measurement behind it is sound and reproduced twice — six arms, maven 3.9.10 / surefire
3.5.5, calibrated known-bad (plus selector → exit 1) and known-good (comma → `Tests run: 20`, exit 0);
`-DfailIfNoTests=false` exits **1** on `surefire:test`, on the `test` lifecycle, in both modules and
in the reactor, while `-Dsurefire.failIfNoSpecifiedTests=false` exits 0 with no `Tests run:` line.
**But the correction it licensed was false.** The run's transcript carries `failIfNoTests` throughout
and `failIfNoSpecifiedTests` only inside surefire's error text: the flag name in the record is RIGHT.
What is wrong is *"exits 0"* — the command was `mvn … | grep -E … | head -20`, whose status is
`head`'s, and BUILD FAILURE was in the captured output, unread. Renaming the flag would have put a
command the run never issued into the record while preserving the half that is false. **:2897-2899**'s
ground for not shipping a flag clause therefore stands re-measured, for the reason already recorded.
Corrected per Step 4's *"Prefer deleting an unsupported clause to rewording it"*: the outcome, not the
flag. Precedent for correcting a record from a later window: **:2350-2352**.

**Corrections applied to the store.**
- **`:3119`'s count, 3 → 4.** It read *"3 records — previous 2 (:811-824)"*, re-deriving from the base
  entry and dropping **:958**'s increment (`#297`:43, *"Third distinct harm and a third surface"*).
  **:1488**'s class, and the same block caught itself committing it for the prose-correction count at
  **:3090-3092**. The fourth sighting is the one `:3119` records, captured in `#435`; `#439` is the
  OTHER SIDE of that same event and is not a fifth — the draft said it was, contradicting its own O5.
- **`#439`'s Environment bullet**, per the C2 entry above.

**Parked.**
- **The proposer not verifying its own citations: 34 cycles** — previous 31 (**:2706**). This pass
  added three: `harden`:410 for *Don't stop correcting a claim*, which is **:414** (caught by the
  proposer before submission); `harden`:248-255 for a bullet that ends at **:256**; and **:3084-3086**
  for the self-catch sentence, which is **:3090-3092**. A fourth in prose rather than counted: the
  draft cited `9a08df8` without saying it is a querystore mirror commit, which resolves in no
  chartsearchai ref.
- **A completeness sweep whose criterion is narrower than the defect class it claims: 1 record.**
  `#439`:`[r1]`, blocking, 1 round, and the run's only genuinely missed disclosure — the sweep
  filtered log-call ARGUMENTS by identifier name and the missed site passed `withheld`/`pairs`;
  `[r3]` then found a fourth class the same criterion reaches. The transposed remedy already exists
  for the neighbouring problem, `harden`'s *enumerate the claim's SUBJECT instead … a population you
  can finish*. **REOPEN ON:** a second record of a defect-site sweep narrower than the class it reports.
- **A measurement recorded in javadoc, falsified by a later commit on the SAME branch: 1 record.**
  `#439`, *"Widening the capture to the module root was tried and reverted"*, whose cause round 3's
  own `close()` fix removed. Cost: part of 1 round. **:2714-2723** and **:2895** are the same hazard
  for POINTERS; this is the measured claim itself. **REOPEN ON:** a second.
- **`pr-harden` has no contract for a background BUILD as an await: 1 record, 0 cost.** `#439` records
  inventing one — *"recording a background BUILD as an await (not just an agent) is what lets an
  orchestrator yield while a build runs"* — against a schema whose field is `{"agent": …}` and a rule
  that reads *"Don't spawn a subagent without recording the await."* Raised by the refuter against the
  draft, which had filed it as a skill holding rather than a gap. **REOPEN ON:** a second, or one
  where the missing contract costs a yield.
- **A run reusing a prior run's DECLINE as precedent, overturned: 1 record.** `#439` round 5 —
  *"A security-scan fix needs no instruction-file rule, per #435's precedent"* — where `#435`'s rule
  bound one directory and this one binds two packages, which the root instruction file's own criterion
  routes differently. Absent from the draft entirely. **REOPEN ON:** a second.
- **`-Dtest=A+B` selects nothing and reports BUILD FAILURE: 3 records** — previous 2
  (**:2887-2890**, **:3133-3134**), plus `#439`. Re-measured today at both ends; see the C2 entry. No
  clause proposed, three windows running. Label kept character-for-character from **:3133** so the
  two group, which is one of the grounds P2 died on.
- **A ledger citation invalidated by an edit the SAME commit made ABOVE it: 1 incident.** The draft
  of this block cited the entry above as **:3129-3130**, correct when read and four lines stale by
  the time it was written, because this block's own `:3119` correction inserted four lines above it.
  **:2895** states this hazard for a SKILL's line numbers and **:2721-2723**'s remedy — *"resolve
  skill-file pointers LAST … a record's line numbers are stable, a skill's are not"* — does not
  reach it: the ledger's own numbers are not stable either, in exactly the commit that corrects an
  entry. Caught by re-resolving every citation in this block after writing it. Not proposed as a
  rule at 1 incident; the existing remedy needs one word, not a new entry.

## 2026-09-16 (addendum to the window above, operator-directed) — 1 applied as a correction; **Step 5 not run**

**APPLIED as a correction · `skill-retro` 0.2.8, Step 6's push-verification clause.** The clause
already said to read the pushed copy with `git show origin/main:<path>` "after a `git fetch`"; it did
not say WHEN. Measured this window: a concurrent session pushed its own retro between this window's
fetch and its all-clear, so a full four-family `cmp` reported everything identical against a
superseded `origin/main`, and only a re-fetch showed the remote had moved. The added clause says to
fetch immediately before the comparison you report. It is a sharpening of an existing instruction
rather than a new obligation, hence the patch bump.

**Deviation, recorded because it is one:** Step 5 was not run on this change. The operator directed
the edit after the incident, and no fresh agent refuted it. The cheap checks were done instead — the
clause was read in full to confirm it does not restate what is already there, and `REJECTED.md` was
searched for a prior kill of the same idea (none). **REOPEN ON:** any evidence that "immediately
before" is the wrong remedy — for instance a window where the re-fetch itself raced and a lock or a
recorded sha would have been the answer.

**And the incident itself is the fourth sighting of cross-session interference on a live run's shared
state** (previous 3, this block above). The first three were about work; this one is about
VERIFICATION, which is the more dangerous shape: a stale check reports a clean result it has not
earned, and nothing about the output distinguishes it from one that has.

## 2026-09-20 (window: 6 run records — #444/PR460, #445/PR461, #446/PR453, #447/PR456, #448/PR452, #450/PR457 — plus 2 notes and one carried draft) — 5 edits applied from 8 proposals, 2 killed, 2 parked, 1 driver patch shipped, 1 lesson the proposals missed; linter 10 files, 0 findings; `pool-test.py` 523 passed / 0 failed

The window's records were written by six runs; the `#450` file carries two, its `resolve-ticket`
record and a separate `pr-harden` one for PR 457. Three further 2026-09-17 files were on `origin/main`
and **not on this machine** — the wall-clock measurement note, the pool-PR-detection defect note, and
`proposals/2026-09-17-wall-clock-two-levers.md`, whose own header says "Step 5 has NOT run". They were
pulled down before reading, and that omission is what P0 below is about.

**APPLIED · `pr-harden` 0.25.0 — "check it out detached" binds only an agent with its OWN checkout
(P1).** Bar **(a)**, three records, and **:2234-2237**'s reopen (*"1 record, a cleanup detour. REOPEN
ON: a second record"*) met twice: `#446` (the brief left the ORCHESTRATOR's tree detached, the next
phase's fixer edited in detached HEAD, reattaching needed `--ignore-other-worktrees` past a leftover
agent worktree; rounds 2-3 briefed "the worktree is already at the head" and it did not recur) and
`#444` (a reviewer that died on a 429 left it detached, every later round passed
`isolation: "worktree"`). It is also the shared-worktree hazard `harden`:120-131 documents and
`pr-harden`:954 forbids, instructed by `pr-harden`'s own Step 1.
- **The gate cut `#247` to a clause** rather than the paragraph it had in the draft, on document-growth
  grounds (`skill-retro`:24-26). Nothing measured is lost: `#247`'s own remedy, *"return the worktree to
  `<branch>`"*, is a cleanup after the fact and both shipped options are preventive.
- **Scoped to the REVIEWER on purpose.** `#445` records that an isolated FIXER "cannot push to the
  pipeline worktree (the harness refuses git operations against another worktree)", so this must never
  read as "isolate everything"; parked below.
- Net **+6 lines**.

**APPLIED · `pr-harden` 0.25.0 — the index line is owed by the commit that ADDS an entry (P5).**
**:1817-1822** parks the INDEX at two failure sightings (`#280/PR383`, `#348/PR369`), both found after
a merge; this window is a third, found with no merge anywhere near it.
- **The draft's own characterisation was FALSE and the gate measured it.** The draft said one branch
  wrote Decision 103 and three others detected it missing — "three detections of ONE authoring
  failure". It is **four branches, four different decisions, four missing index lines**, each writing
  its own: `6cd160a5` (#448), `93a14bbe` (#447), `818b23cd` (#444), `0c9eeb21` (#445, still only on its
  own branch). `#444`'s instance is in the git history and in **no record**. The draft understated its
  own case, which is the direction this ledger sees least often and should not reward.
- The same fact strengthens the paragraph's OTHER half, so both were edited: four concurrent branches
  taking one number from a shared sequence is the collision `pr-harden`:159-161 already records at
  "three consecutive runs, twice within a single run".
- **OPEN, carried rather than argued: placement.** The gate's objection stands — the clause now sits
  inside a block conditioned on *"where the base MOVED"*, and the reader who owes the index line is the
  agent WRITING the entry, not the round that merges. No skill has a step where an ADR decision is
  written, and the root `CLAUDE.md` is 24,999 of 25,000 bytes (`#444`), so it cannot take the rule.
  **REOPEN ON:** a home for an authoring obligation, or a record where the merge-scoped placement is
  what let one through.
- Net **+5 lines**.

**APPLIED · `harden` 0.33.0 and `pr-harden` 0.25.0 — an exemption is where the next defect gets
written (P4).** Bar **(a)**, two records, each costing a round or cycle and each raised by a fresh
agent: `#448` pr r1 (*"allow-listed a whole METHOD, so an uncharged per-marker splitter written inside
it passed"*, non-blocking, 1 round) and pr r2 (*"BOTH tightened rules walked through again"*,
blocking, 1 round); `#445` harden p2 pass 10 (*"the endpoint class was then exempted from the new
dialect rule it most needed"*, 2 cycles). Nothing in this ledger occupies the territory — the gate
grepped `exempt|allow-list|allowlist|carve-out` over all 3,274 lines and found only unrelated hits.
- **The `api/src` limb was CUT.** The gate cited `harden`:283-285, which already says to relocate a
  guard's subject "outside the slice the guard reads": a client written in the omod controller against
  a rule reading `api/src` IS that mutation, so it is **:1196-1198**'s instruction-not-followed class.
  What survives is the half about a carve-out the author WROTE, which no rule reaches.
- **Placed on the positive-control rule, not the relocation bullet**, because it is that rule's mirror
  — build the case the exemption ADMITS and watch it PASS, against build the case a negative assertion
  exists for and watch it FAIL. In BOTH skills, per **:3xxx**'s reachability finding restated by the
  gate: `pr-harden`:261 means a rule living only in `harden`'s Termination is not in a `pr-harden`
  fixer's brief, and both of `#448`'s instances were `pr-harden` rounds.
- Net **+8 in `harden`, +3 in `pr-harden`**.

**APPLIED as a PRUNE, from the gate rather than from a proposal · `pr-harden` 0.25.0 — the polling
paragraph argued a lever the harness no longer has.** The paragraph at `pr-harden`:917-931 named
`TaskOutput` and a `run_in_background` flag and argued a CHOICE between a background spawn and a
foreground one. **The retiring measurement, recorded here as Step 4 requires:** in the harness of
2026-09-20 the `Agent` tool's schema carries `description, isolation, model, prompt, subagent_type`
and no `run_in_background`; `ToolSearch "select:TaskOutput"` returns no matching tool; and the pool
launches the same `claude` binary (`pool.json` `binary: null` → `/opt/homebrew/bin/claude` 2.1.278)
that this session runs. The spawn result now carries the warning itself. What outlived the tool is the
transcript file, and the paragraph now says that and nothing about a tool name. The 2026-09-01
953,119-byte measurement is kept, compressed, because it still supports the surviving rule. Net
**−1 line**, and a dead instruction removed.

**KILLED · P2, "the polling decision is made at the SPAWN".** Bar (a) was met — `#445`, `#447` and
`#448` each record polling raw JSONL windows — but the proposal's structural premise was false, and the
gate measured it rather than arguing. Parsing all six orchestrator transcripts and mapping every
`TaskOutput` call's `task_id` back to the `Agent` spawn that issued that `agentId`: **106 polls, 54 of
them against agents spawned with NO flag** — #444 11/11 flagged, #445 11 of 40, #446 0 of 23, #447
22/22, #448 7/7, #450 0 of 3. #445, one of the proposal's own three records, is 29 of its 40 polls
against unflagged spawns. So the choice was never at the spawn even on the harness those runs used, and
the drafted rule would not have prevented what it cites. It is separately unfollowable, per the prune
above. **The records' three quoted figures are undercounts** (#447's "six polls" against 22 for that
run; #448's "three" against 7) — recorded here, attributed to the gate's method, and NOT written back
into the records, because this pass did not reproduce the parse and the records' figures are scoped to
subsets the gate's totals are not. **REOPEN ON:** nothing, unless a harness reintroduces the lever.

**KILLED · P3, a TELL for when a guard is asking the wrong question.** Three objections, two
sufficient. **The headline evidence was paid where the edit cannot reach**: `#444`'s nine defeats were
`pr-harden` ROUNDS (its header, `rounds: 8 (pr-harden…)`; its heading, "nine rounds bought this"), and
the edit was drafted into `harden`'s Termination, which `pr-harden`:261 does not route to a fixer —
leaving `#446`'s ~4 cycles, one record, which is not the case the proposal argued from. **And the tell
contradicts its own bullet**: `harden`:291-304 already works `#421`, whose escapes were exact type
equality, then a raw type, then wildcard bounds — defeats that do not behave identically — so
"the tell is that the defeat BEHAVES identically" narrows the bullet's own example out of its
antecedent. Also a restatement of `pr-harden`:210-219 and `harden`:235. **:2002-2012** killed a
proposal in this territory before, partly as instruction-not-followed, and `#448` records the existing
rule working ("the operative rule of this whole run"), which **:1993-1995** rules is not a sighting.

**PARKED · P6, collapsing Step 6's enumerated `cmp` bullets into the tracked list — and P0's full
form with it.** The document facts all check out (184 paths tracked under `.claude/` on `origin/main`;
`.claude/pipeline/` holds five, including `gate-state`, the only writer of either state file; no Step 6
clause names it), and the sweep was calibrated both ways this session — it reported the three records
this machine was missing, and a one-byte append to a live skill fired `DRIFT` and cleared on restore.
It is parked anyway, on three blocking objections:
- **The pipeline limb is a re-proposal at the same bar.** **:882-887** killed exactly it (*"0 records,
  and the proposal said so itself ('no observed drift')"*). The drift count is still zero: the sweep
  found no `DRIFT` on any of the 184 paths.
- **Bar (c) is unavailable for it.** **:870-873**, with **:773-775** and **:434-440**, rules that
  "which files sit in two directories is a fact about two filesystems", not a contradiction in a
  document. The proposal claimed (c) on that ground.
- **The sweep cannot replace the mirror clause, which is what the collapse would have retired.** It
  iterates `git ls-tree origin/main`, so a live file absent from the repo is never visited — and that
  is the exact direction the mirror paragraph exists for (**:1524-1526**). Measured while parked: seven
  live files under `~/.claude/skill-lessons/` were absent from `origin/main`.
- Two further OPEN objections worth carrying: the draft's coverage arithmetic omitted
  `skill-lessons/artifacts/` (43 tracked files), in a proposal whose whole claim was that it had read
  the document; and `pool-run`'s own `parity_problems` already compares live skills, hooks and the
  pipeline scripts against the source repo and walks the LIVE tree, so it already catches the direction
  the sweep misses — its docstring's *"two copies of a consistency check drift exactly like the thing
  they are checking"* is the argument against a third.
**REOPEN ON:** a drift actually observed in a vendored family no clause names, or an extension of
`parity_problems` rather than more prose — which **:1524-1526** already prescribes for this class.

**APPLIED in reduced form · `skill-retro` 0.2.9 — Step 6's comparison is owed BEFORE Step 1's read
(what remained of P0).** The gate's two blocking objections were about the bar claimed and the cause
named, not about the guard, so this is the revision those objections license rather than the proposal
they refused. It states no cause: the draft said "the store is written by more than one machine", and
the gate showed one committer and this machine's own querystore checkout in the harness path, which is
`skill-retro`:80-84's *write the guard, not the diagnosis*. What ships is an ordering fact about the
document — Step 1 depends on an invariant only Step 6 checks, and Step 6 runs last — plus the measured
instance. **Deviation, recorded:** the reduced form did not go back through Step 5. Net **+5 lines**.

**SHIPPED · `ticket-pool` 0.24.0 — a merged PR is not an open PR (P7).** Not a lesson but a prepared,
tested patch from `2026-09-17-pool-pr-detection.md` whose stated apply condition ("once no pool run is
executing the file") held. `open_prs()` asked `gh pr list --state open` and `work_ticket` decided the
outcome from that list alone, so a PR merged before the check was recorded `no-pr`: eight tickets over
two days (#413, #421, #337, #276, #412, #409, #425, #315), each charged an attempt against
`max_attempts: 2`, and the queue's "already has an open PR" guard read the same open-only list, which
is the path on which one issue gets two PRs. #294's row still reads `ready pr=417`, and 417 is #409's.
- **Controls, both directions.** 16 cases pass against the patched driver; against the pre-patch driver
  the same cases fail 2 and then raise `TypeError: 'NoneType' object is not iterable`. Re-calibrated
  after integration into `pool-test.py`, where they behave identically. Full suite **523 passed /
  0 failed** (previous total 507).
- **The cases were integrated rather than left beside the suite**, because a test file nothing runs is
  not a test; `pool.sh` is restored in a `finally` so later cases keep the shipped one, and the
  stand-in returns a real `subprocess.CompletedProcess` rather than a `SimpleNamespace`.
- **The documentation change is forced by the code, not cosmetic.** `ticket-pool`:401-402 read
  *"Everything else is retried on a later invocation until `ticket.max_attempts`"*, which `unknown`
  joining `NEEDS_HUMAN` makes false.
- **DECLINED, with the citation:** the gate called the patch's new comment "Tier 3 reads PROSE" an
  inversion against the code's `tier = 1`. It is not. `pr_for_ticket`'s docstring numbers "three
  descending tiers of how sure the link is" — Tier 1 `closingIssuesReferences`, Tier 3 the body — while
  the code's `tier` variable is a sort key where higher wins. The patch matches the docstring four
  lines above it; "fixing" it would have made the two disagree.
- **OPEN, not fixed, because it is untested code in an unattended driver:** nothing retries the `gh`
  ask before `open_prs` returns `None`, and `prs_for` caches that `None` per slug, so one transient
  failure both strands a delivered-or-not ticket for a human AND makes the queue builder skip every
  ticket for that repository in that invocation. Both directions fail SAFE, which is the whole point of
  the change, but the availability cost is real. **REOPEN ON:** a window where a transient `gh` failure
  actually strands a queue.

**Corrections applied to the store.**
- **`~/.claude/bin/run-timing.py` vendored at `.claude/bin/`**, with its `.bak-20260917`. The
  2026-09-17 note argues for it and nothing objected: it is the harness the "is the pipeline getting
  faster?" question runs on, two measurement passes have leaned on it, one published wrong numbers from
  it, and it was fixed in place with no copy anywhere else. Evidence preservation, not a rule.
- **Six run records and this window's proposals file mirrored**, per Step 6.

**Parked.**
- **`harden-set` written only when a cycle spawns an agent, not at every cycle CLOSE: 2 records** —
  previous 1 (**:2241-2245**, `#387`, a missing cycle label). `#445` is the stale-label sibling: the
  entry "went stale at cycle 7 / head 5ec91f5e and was never updated through cycles 8-12 (the context
  compaction sits in that gap), so the Stop gate refused the handover quoting 'cycle 7 made 16 edits'
  about a head 20 commits old", and the run closed on a labelled override rather than a cycle 13.
  **The proposals missed this entirely and the gate found it.** No clause proposed: `harden`:315 already
  says "At the close of **every** cycle", so prose would be **:1196-1198**'s class — though that
  sentence is separated from its own `harden-set` snippet by some twenty lines of `awaiting` material,
  which is a layout defect nobody has measured a cost for. **REOPEN ON:** the mechanical check
  **:2241-2245** asks for — `harden-set --count-edits` already prints when it cannot measure the commit
  half, so a missing or stale cycle label may be within reach of the helper itself.
- **`pull/<n>/head` LAGS a push, and `gh pr view --json headRefOid` is cached too: 1 record.** `#444`
  — round 2's first fetch returned round 1's sha; every later round used a delete-ref + re-fetch retry
  loop. `pr-harden`:133-145 already names "the push had not landed when the fetch ran" as one of the
  causes of two rounds sharing a sha and prescribes **no remedy**, and explicitly refuses to name a
  cause for the two observed pairs. This is the first record to establish one. **REOPEN ON:** a second,
  or one where a reviewer actually reviewed the previous round's sha.
- **Orphaned `agent-*` worktrees: 2 records.** `#446` — "35 orphaned `agent-*` worktrees accumulated
  from isolated /harden agents; nothing reaped them, and one held the PR branch", which is the
  `--ignore-other-worktrees` detour P1's shipped text quotes. `harden`:146-149 names the class on
  `#348/PR369`. Nothing reaps them because the run that leaves one is the run that died. **REOPEN ON:**
  a third, or a reaper.
- **An isolated fixer cannot write to the pipeline worktree: 1 record, no round cost.** `#445` — its
  edits were extracted as a patch and applied by the orchestrator, which then "discarded one of the
  seven files with `git checkout --` while restoring its OWN mutation probe", caught by `git diff
  --stat` reading 6 files instead of 7. Constrains P1's shipped text rather than standing alone.
  **REOPEN ON:** a second, or one where the hand-off cost a round.
- **A script that aborts mid-batch, and the recovery re-applying only the edit that FAILED: 1 record.**
  `#446` — three corrections silently never landed and "the follow-up only re-applied the one that had
  failed", caught a cycle later by a fresh agent. The per-edit-write rule exists (`pr-harden`, *Editing
  by script*, fourth bullet) and the record says it "was not followed"; what is new is the recovery
  asymmetry. `#444`'s "a helper script aborted before writing and I committed without reading the build
  output" is the same family from the other end. **REOPEN ON:** a second recovery that re-applied only
  the failing edit.
- **A correction that states a NUMBER, announcing itself as re-derived while repeating the original's
  arithmetic: 2 sightings, 1 record.** `#445`, blocking at round 1 — "the budget entry is now derived
  rather than asserted — read the file's size and add a tenth" against 3,884 + a tenth = 4,272 and an
  entry saying 3,900, where "the second draft repeated the first draft's arithmetic while announcing
  the correction"; and, same record, "a correction falsified by its own commit". `#447` c4 is the
  adjacent shape, blocking at 1 cycle: "the cycle-3 correction of an overstatement overstated ~2.4x the
  other way". Held back because all three skills carry *prefer deleting to rewording*, and the residue
  is the case where a number MUST be stated. **REOPEN ON:** a second record where deletion was not
  available.
- **A fresh lens's prediction logged as a risk instead of acted on: 1 record.** `#446` — a `/harden`
  lens predicted "a macOS number doing a cross-platform job" and "the orchestrator logged it as a risk
  rather than acting", costing 1 `pr-harden` round and a red CI run. **REOPEN ON:** a second.
- **A threshold calibrated on one platform, and CI never green: 1 record.** `#446`'s
  `ERROR_BODY_BUDGET`, 2 MiB from ~0.7 MB of macOS loopback slack, red on Linux at 2.67-2.75 MB on all
  three Java jobs and red at the previous head too. No skill mentions the PR's own CI checks at all;
  `pr-harden` step 5 proves the build locally and stops. Counted as ONE round: the record states it
  twice, as the measurement and as r2's blocking finding, and that is one event. **REOPEN ON:** a
  second, or a window where a round went to a check the loop never looked at.
- **The prose loop: ~4 more records, remedy unchanged.** `#447` (cycles 5-7 each finding only text the
  cycle before wrote; ended by "delete the CLAIM SHAPE", "exactly as written"), `#448` (nine false
  sentences across four rounds, 3 cycles; ended by publishing two measured points and no rule), `#450`
  (from cycle 3 on every finding in prose the previous cycle wrote, cycle 4's own cut introducing 3 of
  its 7; ended on the labelled override, after which "pr-harden's separate fixer then produced two
  blocking findings the author had had four cycles to find"), `#444`. Two of the three ended INSIDE
  `harden` with the existing remedy, so nothing licenses a new one — and `#450`'s implied remedy, hand
  the prose to the fresh-context loop, would license ending a run early, which Termination refuses.
  **REOPEN ON:** a record where the existing remedy was applied and failed.
- **Wall-clock P1 — the verifier that covers the merging head running in the reviewer's wave.**
  Carried from `proposals/2026-09-17-wall-clock-two-levers.md`, which its author marked "Step 5 has NOT
  run". This pass was that gate and the answer is PARK, on the draft's own first objection: `pr-harden`
  §7 pushes its non-blocking edits AFTER the last verifier run in the documented normal case
  (`:630-631`), so the saving exists only where §7 edits nothing, and three runs in which nothing was
  pushed is the whole evidence base. It buys 8-19 min in the clean case against a discarded agent, a
  standalone deploy and rate-limit exposure in the non-clean one, and the rate-limit-death entries are
  the cost side nobody has priced. **REOPEN ON:** a count of how often §7 pushes, over a window large
  enough to price the discard.
- **Wall-clock P2 — the cheaper confirming cycle as a two-agent wave.** Its own open question was
  "read those four records once they land and classify every single-agent wave". They landed and do
  **not** close it: from the findings lists `#450` c4 and `#447` c3 look documentation-only while
  `#448` c4 (a test in the causal path) and `#450` c3 (a cache-hit disconnect window) do not — but that
  is inferred from findings, not measured off the waves, and the draft's own third objection stands
  ("it spends an agent to save wall clock on the cheapest cycle in the run, which is the opposite of
  where the measurement says the mass is"). **REOPEN ON:** a wave-level classification taken from the
  transcripts.
- **`mode` has a reader and no writer: verified, still 0 cost.** `pr-harden`'s **State** section says
  so itself ("Either something writes `mode` or that branch goes"). Verified 2026-09-20: `gate-state`
  contains no occurrence of `mode` in 341 lines, while `pr-harden-gate.sh`:295-306 reads `.mode` and
  has a distinct `--plan-only` block behind it, so a plan-only run that stops mid-plan gets the generic
  `building` message telling it to implement — work its own mode excludes. No record has met it.
  **REOPEN ON:** a `--plan-only` run that was told to implement, or take the skill's own second option
  and delete the branch.
- **Two pool slots sharing `chartsearchai.llm.serverPort` 18085 and evicting each other's
  llama-server: 1 record.** `#450` `[verify]`, recorded as `environment`. A pool configuration fact.
  **REOPEN ON:** a record where it cost a round.
- **The proposer not verifying its own citations: 34 cycles, unchanged** — previous 34 (**:3227**).
  The gate checked every line number and ledger position this window's proposals cite and reported
  "None" did not resolve. What failed instead was three CHARACTERISATIONS built on resolved citations
  (P2's premise about the flag, P5's "one branch … three others", P6's coverage arithmetic), which is
  a different defect and is counted with each proposal above rather than here.
- **A measurement command self-matching its own shell: 1 incident.** `ps aux | grep -c '[p]ool-run'`
  returned **3** inside a compound command whose own text contains `pool-run` many times, and **0**
  when run alone — the bracket trick protects the grep's own line, not a parent shell whose argv
  carries the literal. Caught only because the two readings disagreed. Nothing was harmed: the true
  count was 0 both times and the patch's apply condition held. The user-level `CLAUDE.md` already
  requires calibrating an ad-hoc measurement both ways; this is that rule paying for itself.

## 2026-09-23 (targeted, owner-directed: `pr-harden`'s FINISH rule and its round-4 extension — 8 records since `LAST` plus the 30 `pr-harden` records of the rule's live period, 2026-09-07 to 2026-09-19) — 5 applied (P1, P3 and P2 after revision, P4 as a correction, P5 from the gate) plus 2 homes the gates found, 1 killed (P3's Reporting bullet), 1 parked (P2's resolution B); net −4 lines over five files; two refutation rounds; linter 10 files, 0 findings; `gate-test.sh` 35 passed / 0 failed

The window is `proposals/2026-09-23-retro-finish-rule.md`, which carries the run-by-run table of what
FINISH did with a terminating round's non-blocking findings, the follow-up-issue census and its
calibration. Both rules under review skipped Step 5 when they shipped (`ea574f9`'s and `0183209`'s own
messages), so this pass was theirs as much as the proposals'. The eighth record,
`2026-09-23-openmrs-module-chartsearchai-477`, was written at 16:58 while this pass ran, and was read
before it committed; it bears on no proposal here.

**The census, as an observation (measured 2026-09-23, `openmrs/openmrs-module-chartsearchai`).** 19
review-loop follow-up issues filed since 2026-09-07, 17 open. Three were worked as full `resolve-ticket`
runs — #412, #421 and #425 (`Refs`, so still open) — and two of those three PRs filed a new follow-up
(PR427 → #429, PR428 → #430). Four more runs routed findings "to a follow-up" that nothing ever filed
(PR410, PR417, PR426, PR470), and one wrote them into its PR body instead (PR481).

**APPLIED after revision · `pr-harden` 0.27.0, `resolve-ticket` 0.18.0 — two skills said nothing is
posted to GitHub (P1).** Bar **(c)** in each skill (`pr-harden`:1149 against its FINISH's follow-up
issue; `resolve-ticket`:588 against its own Step 8 PR and its adjacent-defect "new ticket"), and bar
**(a)** as well, which the proposal understated: the gate read the final reports of the four lost runs
and each repeats `resolve-ticket`:588's line next to a follow-up list that was never filed (PR410
*"Nothing was posted to GitHub but the commits and the description. Want me to file the two follow-up
issues"*; PR417; PR426 *"**Nothing was posted to GitHub but the commits.**"*; PR470), plus #476:21,
which cites the contradiction as its reason for not filing.
- **The gate cut the proposal's rewording.** *"The PR carries N commits and no review comments"* would
  have turned a gloss into a false standalone claim — a PR that was already ready keeps receiving the
  GitHub App's reviews (`pr-harden`:68-70, :718-723). Both bullets now keep only the offer.
- **A third home, found by the gate:** `ticket-pool`:325 listed what an invocation does "in your name"
  without the follow-up issue. `ticket-pool` 0.24.1 adds it — a disclosure, since FINISH files it
  unattended on a public repo.
- Net **±0** in both skills, **+1** in `ticket-pool`.

**APPLIED after revision · `pr-harden` 0.27.0 — FINISH files the follow-up itself (P3).** Bar **(a)**,
four records (PR410 `…-294`:162-166, PR417 `…-409`:28/:53, PR426 `…-426`, PR470 `…-469`:18). PR417 and
PR426 are both ticket #409 but separate runs with separate transcripts, so no double count.
- **The draft said the cause was not established. It was, and the gates established it** from each
  record's `transcript:` line: every report NAMED the items, and three then offered to file them or
  declined without a go-ahead — PR410 L2101, PR426 L1729/L1777, PR470 L2250 (*"I'm not creating a
  GitHub issue without your go-ahead"*); PR417 L1413 only named them as owed.
- **Round 1 said all four were unattended. Round 2 measured the opposite**: all four were interactive
  `--work` sessions with the owner present — entrypoint `cli` against the driven runs' `sdk-cli`,
  `ledger.json`'s `launched_by: work`, and owner messages minutes from each offer (PR426's owner typed
  a request 2m16s after its offer; PR410 was merged by the owner 3.5 min after its). The offers went
  untaken anyway, and filing unasked is established practice (PR478's attended run filed #482).
- **So the Reporting bullet the draft proposed was KILLED**, on **:1196-1198**'s
  instruction-is-not-the-lever: the report was reached in all four runs, and :656 already required the
  issue to be "named in the report". Its alternative clause (*"or that the run left no non-blocking
  finding unimplemented"*) was also a universal, false whenever a fixer declines on the record.
- What shipped is an instruction at the step that failed: *"file it yourself before you report, rather
  than offering to, and name it by number in the report"*, in the same commit as P1, which removes the
  line all four repeated. The staged diff also carried a provenance sentence naming the four as
  having offered "unattended"; round 2 cut it as false twice over, and the corrected provenance went to
  the commit body. Net **±0** (three lines for three).

**APPLIED after revision — resolution A · `pr-harden` 0.27.0 and `pr-harden-gate.sh` — FINISH forbade
an edit three passages still assumed (P2).** Bar **(c)**: `pr-harden`:655-657 says FINISH does not edit
the cleared sha, while :686-688 (from `b06d6a8`) said *"this step's own non-blocking edits are pushed
*after* the last verifier run in every case"*, :661-664 quoted that as a concession, :735 listed *"a
nit's fix exposes a real defect"* as a FINISH event, and the gate's own block message (written by
`ea574f9`, the same commit as the rule) called *"FINISH applying that round's non-blocking findings …
the usual cause"*. All four deleted. The gate's clause went on a measurement, not only on consistency:
across every transcript since 2026-09-07 its only real firing was PR478's, on a checkout sitting on
PR465's head.
- **The 2026-09-20 window read the stale clause as the rule** — *Wall-clock P1* (**:3507-3515**) is
  parked on *"§7 pushes its non-blocking edits AFTER the last verifier run in the documented normal
  case"*. That premise no longer has a home.
- Net **−4 lines** in the skill, one clause out of the gate. `gate-test.sh` asserts allow/block only:
  35 / 0 on the edited gate and on the original, 12 failures and exit 1 on an always-allow copy
  (known-bad control); both edited block messages were rendered through the real hook under a temp
  `HOME`, before and after.

**PARKED · P2 resolution B — let FINISH take a non-blocking finding for one blocking-only round.** Five
blocking objections, each cited:
- **Its premise was false.** The draft said three runs (PR414, PR423, PR478) "took the gate's reading".
  The gate's text appears 0 times in the PR414 and PR423 transcripts, and in PR478's only after its run
  record, about another PR's head. What all three quoted was the skill (`pr-harden`:1221-1222 and
  :734-737).
- **Unscoped**, it applied at every FINISH, including after a blocking-only round, where :742-743 and
  :112-113 route non-blocking findings to the issue — so it reopened the loop :739-741 closes.
- **"An edit to it costs one more round" is a universal the same window refutes**: PR #465's r6-6 fix
  introduced blocker r7-3 and a further round.
- **Its precedent would normalise the ORCHESTRATOR editing a cleared sha**: PR414 (L1942-L1962) and
  PR423 (L1688-L1717) wrote and committed the fixes themselves, bypassing the fresh fixer the Roles
  table and `resolve-ticket`:554-556 require; only PR478 spawned one (L2272).
- **Bar (c) licenses resolving the contradiction, not B's behaviour change**, and the ledger already
  holds that question.
**REOPEN ON:** a proposal that reopens **:2532-2534** on its records (#294:162-166, #409:28, #412:26,
#276:28), limits the permission to a full round's findings (rounds 1-3), taken once per run by a fresh
fixer, names the confirming round by its heading, states the cost as at least one round with PR465's
r6-6 → r7-3 as the counter-case, and survives its own Step 5.

**APPLIED as a correction · `pr-harden` 0.27.0 — the round-4 rule's PR #465 figures (P4).** Measured
from the orchestrator transcript and `git log -S` in the chartsearchai clone, calibrated to exactly 12
blocking findings of 38, with the record's 8 IDs among them: the 8-of-12 holds, by git rather than "by the
findings' own attribution" (only r7-1 and r8-1 name their source). *"Four of the eight came from a
NON-blocking prose fix … round N … N+2"* is FALSE: three trace to an edit implementing a non-blocking
finding (r7-1 ← r6-5, r7-3 ← r6-6, r8-1 ← r5-4 then r6-6), one of those a prose fix, none N→N+2. *"13
non-blocking prose edits"* is 13 non-blocking findings implemented, 11 of them prose. *"Runtime
behaviour settled since round 3"* is FALSE — round 6's fixer changed executable shell and reported
`runtime_visible: true`. The zero-findings terminating round was briefed *"An empty findings array is
the expected, correct outcome"*, so the clause is deleted rather than kept.
- **Bar, stated honestly:** the no-bar precedent (**:2454-2455**) covers DELETION, and this is
  replacement text; it ships on the gate's `apply` verdict and a calibrated measurement. (The draft
  cited **:2731**, which was applied under bar (c).)
- **At the gate's suggestion, the other side of the same measurement is now in the text**: r6-6, a
  non-blocking finding in the PR's own defect class whose fixer verified the hole was real, would have
  gone to the follow-up issue unfixed under this rule.
- Net **±0 lines**.

**APPLIED from the gate · `pr-harden` 0.27.0 and `pr-harden-gate.sh` — "blocking and non-blocking alike"
had no round scope (P5).** Bar **(c)**, found by the round-1 gate and missed by the proposals: from
round 4 the skill routes non-blocking findings to the issue (`pr-harden`:110-113, `0183209`), while the
FIX step (:291) and the gate's blocking>0 message (:413-416, unchanged since `b06d6a8`) still said
"blocking and non-blocking alike" for every round. Deleted from both; rounds 1-3's non-blocking
implementation stays stated at :110-112 and :807. Net **±0 lines**.

**Round 2 — a fresh agent over the staged diff, before anything went live: ship with fixes.**
- **P3's provenance sentence cut**, on the two blocking objections above.
- **A residual home the proposals and round 1 both missed:** `~/.claude/pipeline/gate-state`:348-350's
  comment said *"FINISH pushes after the last verifier run in every case"* — P2's deleted claim, in the
  script that writes both state files. Fixed in the same commit; `py_compile` clean.
- **The 160-character line P2(1)'s deletion left**, reflowed.
- **`resolve-ticket`'s bump:** only `babbda7` added a rule without one; `44b382c` rewrote two commands,
  `f31dbea` and `9f4d0da` deleted an empty fence and a blank line.
- Every other hunk: ship, re-checked against its citations; both edited block messages rendered through
  the real hook.

**Corrections to the store and this ledger.**
- **:2504** records *"`pr-harden` FINISH not editing the cleared sha, in all three records"*; false for
  #379/PR414 in that same window, whose record says "applied at FINISH" and whose orchestrator committed
  `b711bd81` after round 1 cleared `392f6c13`.
- The windows DID count the FINISH records — as the rule working (#409 at **:2666-2667**, #276 in the
  2026-09-14 window's *Corroborated as WORKING*, #374:25 crediting it). What none counted was the cost
  the same records name, so **:2532-2534** stayed at 1.
- Dated correction notes appended to `2026-09-14-openmrs-module-chartsearchai-426.md` (the "all twelve
  implemented" line), `2026-09-21-chartsearchai-PR465.md` (*"by the findings' own attribution"*) and
  `2026-09-13-openmrs-module-chartsearchai-409.md` (its `transcript:` path does not exist).

**Parked counts (superseding earlier blocks where they differ).**
- **A FINISH rule costing something real: 4 records** — previous 1 (**:2532-2534**): #294:162-166,
  #276:28, #409:28, #412:26. The reopen condition was met on 2026-09-14; the proposal it produced here
  (resolution B) is parked above with its own reopen.
- **The round-4 rule owes its own Step 5.** It shipped without one on one record; this pass corrected
  its figures and did not refute the rule. Its founding record carries its counter-case (r6-6), and
  `0183209`'s *"rounds 1-3 … where every code defect in that run was found"* is false (r6-5, r6-6).
  **REOPEN ON:** a second record where a round-4+ non-blocking finding was a real defect routed
  unfixed, or one where the rule stopped a fixer-introduced blocker.
- **`:734-737`'s "the description was false" read as licence to implement a non-blocking finding: 1
  record** (PR478 L2268), against :666-667's *the body does not move the head*. **REOPEN ON:** a second.
- **A confirming reviewer briefed that zero findings is the expected outcome: 1 record** (PR465 r9's
  retry brief). The nearest rule is :1212-1213's *Don't brief the reviewer with what was fixed*.
  **REOPEN ON:** a second.
- **Wall-clock P1** (**:3507-3515**): its premise's home is deleted (P2 above). Its REOPEN asked how
  often §7 pushes: FINISH pushed an edit to a cleared sha BEFORE ready on PR414, PR423 and PR478 — each
  a non-blocking finding the corrected rule reads as the issue's. PR417's, PR424's and PR426's merges
  came AFTER ready and do not count.
- **An adjacent product defect noticed and not fixed, with nowhere durable to go:** **:532-533** reads 0
  records; #294's report names one it never filed (*"it deserves its own issue and I have not filed
  one"*, `171b90dd` L2101) and #374:30 another ("reported, not filed"). Recorded, not reopened here —
  outside this window's scope.
- **Unpushed skill commits that changed a skill without a version bump: 1 incident** — `babbda7`, one of
  the 2026-09-23 overnight commits, added a rule to `resolve-ticket` and left it at 0.17.0. This pass's
  bump to 0.18.0 covers it.
- **A retro gate's own evidence being false: 1 incident** — this window's round 1 said the four
  lost-follow-up runs were unattended; round 2 measured them interactive, before anything shipped. Not
  **:2483-2489**'s class (that is a run reviewer's finding, and its reopen needs one that reached the
  branch). It is the case for running a second round over the staged diff rather than applying round
  1's revisions directly.
- **A fixer implementing a blocking finding's DIAGNOSIS a different way than its recommendation, which
  Termination cannot tell from a decline: 1 record** (#477:26, `did-not-converge`). **REOPEN ON:** a second.
- **A round-4+ fixer handed a non-blocking finding anyway: 0 records, document analysis (round 2).** The
  orchestrator's downgrade (:269-271) can produce one in any round, :372-374 (*"without that sentence
  it is not a decline … so implement it"*) then pushes the fixer to implement it, against :112-113's
  *"unfixed in this branch"*. Predates this diff. **REOPEN ON:** a record where it happened.
- **The proposer not verifying its own citations: 35 cycles** — previous 34 (**:3534**): one citation cut
  at the gate (P4's **:2731**). Six line-number slips were caught before the gate by re-reading. Refuted
  characterisations, counted with their proposals above rather than here: P2-B's "the gate's reading",
  P3's "cause not established", "no window counted it", and the PR426 paragraph (its round 5 returned
  `findings: []` plus six notes, not one finding "routed nowhere"; and r2-2 was neither implemented nor
  declined).

## 2026-09-23 (second window of the day: 7 run records — #479/PR486, #480/PR484, #482/PR487, #485/PR493, #488/PR490, #489/PR492, #491/PR495) — 3 applied after revision, 0 killed, 9 parked; one refutation round; linter 10 files, 0 findings

Proposals and the gate's objections: `proposals/2026-09-23-window-479-491.md`. The gate killed nothing
and revised all three, so the bar this window was exercised by REVISION, not by a kill.

- **P1 APPLIED (resolve-ticket 0.18.1, pr-harden 0.28.0, pr-review 0.17.1): read a GitHub ticket with
  `gh issue view <n> --json title,body,comments`, never `--comments`.** Bar (a) 4 records (#482:20,
  #485:20, #488:20, #491:20) plus the 3 the retired warning cited; and (c) a document fact, the route
  :1146-1151 and :1823-1826 named. Measured 2026-09-23 on gh 2.87.3, off a TTY: `--comments` prints the
  comments only, so a 0-comment issue is 0 bytes (#491) and #480's 1-comment read is 655 bytes with the
  body absent; the gate re-ran each, confirmed under `script -q`/`GH_FORCE_TTY=1` that a TTY prints the
  body, and found every "empty" run's issue had 0 comments at run time (#347's two comments post-date
  its record). **The gate found the silent case LIVE in #480's transcript** (2026-09-23T14:06:57Z, only
  the comment returned, recovered by `gh api`), which the #480 record never mentions. The gate also
  measured `gh api …/comments` truncating at 30 (cli/cli#13840: 148 via `--json`, 30 via the bare
  endpoint), so the retired paragraph's "fetch the comments separately" was itself lossy past 30. The
  paragraph "three runs met it … confirm you got them" is DELETED, its diagnosis ("that failure") being
  what the measurement retires. Revised per the gate: "never prints" became the version and date.
  **Not edited, reported to the owner:** `~/.claude/CLAUDE.md`'s `gh` bullet carries the same diagnosis.
- **P2 APPLIED (pipeline/gate-state + pool-test.py): `--only` ahead of the subcommand now gets
  guidance, and the `--run` refusal on await/clear-await names the `--only pr` form.** Bar (a) 2
  records (#485:21, #488:18) plus a transcript-only third the gate found (#480:
  `gate-state --owner $PPID --only pr await …`), and (c) `_Parser`'s docstring claiming the guidance
  reaches "the one text the caller is certain to read" — false for `invalid choice: 'pr'`. The gate's
  missed half: #488 was steered into the misplacement by `_require_run`'s "--run goes BEFORE the
  subcommand", so that message now names the pr-only form. The old `--only` branch was narrowed to
  `unrecognized arguments`, because the new refusal text contains `--only` and otherwise drew the
  contradictory "Re-run without it". Accepting `--only` before the subcommand (the gate's alternative)
  was not taken: the guidance approach is the shipped design.
- **P3 APPLIED (pr-harden 0.28.0): where this run pushed the head, compare the fetched sha against it,
  and re-fetch in a bounded loop on a mismatch.** Bar (a) at **3** records, not the 2 drafted — the gate
  found #379 (`2026-09-13-…-379.md:29`, parked :2459-2470) beside #444 (:3457-3461, reopen "a second",
  met) and #491:21. #379's own reopen condition (a round lost, a ≥2-commit lag, a first round) is NOT
  met; the edit's value is that it covers the first-round and multi-commit holes that park named, which
  the `reviewed_shas` equality check cannot see, and against a reference `headRefOid` is not (#444:70:
  cached too). Revised per the gate: the forced `+` refspec was unverified and is gone; the loop is bounded.
  Scoped to runs that pushed the head themselves, since a standalone pr-harden's first round has no
  pushed sha of its own.

**Parked.**
- **Stop hook refusing a yield while a background build/capture runs: 2 records** (#479:20, #489:18,
  one turn). The draft parked it on `resolve-ticket`:448, which the gate showed covers Phase-2 agent
  awaits, not command waits; `pr-harden-gate.sh` refuses a yield in phase `building`, and pr-harden's
  "a background loop hands the turn back at once" sits in the verifier's section. Bar (a) met, but
  whether this is (c) skill-vs-gate depends on who that sentence binds, which is open. **REOPEN ON:**
  establishing that, or a record where it cost more than a turn.
- **harden Phase 2 run as 1-2 agents over the four lenses: 3 records** (#479:21, #488:19, #491:22)
  against harden:100's "Spawn four parallel". Bar met, no cost measured either way, so any text is an
  unchecked claim. **REOPEN ON:** a merged-lens pass missing what a later fresh agent finds in that lens.
- **A shipped KIND-of-question rule (harden:313) not applied across three Phase-2 passes: 1 record,
  ≥2 cycles** (#482). More prose is :1196-1198's class.
- 1 record each: measurement deliverable vs Step 5's test-first framing (#480); `clear-await` has no
  per-agent clear (#480, confirmed gate-state:334); base moved by a clean non-required merge after
  round 1 (#489); editing while a read-only refuter runs (#485); user CLAUDE.md naming
  `git-restore-backup.sh` as a command when it is a PreToolUse hook (#485); shell decoding `–` before
  Python in a mutation script (#482).

**Gate-side effect, undone:** the gate's probe `gate-state --cwd /nonexistent-x await --only pr x`
wrote an `awaiting` entry into `~/.claude/pr-harden-state.json`; it cleared it with `clear --only pr`,
and this pass re-checked that no key containing `nonexistent` remains.

**Not in this window:** `…-494.md` (21:33) and `…-496.md` (21:42) were written by the pool after this
window was read, and are mirrored unread. They open the next window; `LAST` holds a date, so a pass
starting from it re-reads this day and must skip the seven records above.

## 2026-09-23 (third pass of the day, targeted, owner-directed: the review loop files no follow-up issue) — 2 applied after revision (P1, P2) plus 5 homes and 1 test case the gates found, 0 killed, 4 parked; two refutation rounds, the second over the staged diff; linter 10 files, 0 findings; `gate-test.sh` 36 passed / 0 failed on the edited gate and on the original, 13 failed / exit 1 on an always-allow copy

The owner's instruction, verbatim (session `11df53f2`, 2026-09-23): *"i think i am getting tired of the
skill retro skill creating new issue after issue when working on any issue. Can't it resolve it all in
the same pull request without creating a chain of issues? I work on a created issue, and then it also
creates another one, and the cycle continues"*. The proposal as it went to Step 5 is
`proposals/2026-09-23-owner-no-follow-up-issues.md`; round 1's revisions and how each was taken are
below. No window of records was read: `…-494.md` and `…-496.md` (mirrored unread by `846a225`) were
read ONLY for this question, and remain the next window's to derive from.

**The census, measured 2026-09-23 on `openmrs/openmrs-module-chartsearchai`** (`gh api …/issues?since=`,
every body's first line read). Of the 18 issues opened that day, 13 were review-loop follow-ups: #479,
#480, #482, #485, #488, #489, #491, #494, #496, #498, #500, #503, #504. Three chains: #471 → #479 → #488
→ {#491 via PR490, #503 via PR501}; #472 → #482 → #489 → #494 → #498 → #504 (08:50Z to 19:06Z); #473 →
#480 → #485 → #496 → #500. **#480 was filed by FINISH** — `ebdba95d` L1792-1793, *"Meanwhile I'm filing
the follow-up issue, which FINISH calls for"*, then `gh issue create` — so the first window's census
excluding it as "feature scope left by #475" (`proposals/2026-09-23-retro-finish-rule.md`:62-63) was a
misclassification. Every worked link was an owner-launched `--work` session (`ledger.json`
`launched_by: work`) and none of the issues was labelled, so each link needed both the filing and the
owner's choice to work it; this change removes the filing, which is the part the owner named.

**APPLIED after revision · `pr-harden` 0.29.0 — a clean FULL round's non-blocking findings go to a
fresh fixer in this PR, under one blocking-only round; FINISH files no issue (P1).** Bar: owner-directed,
and **:3616-3634**'s REOPEN ON for resolution B, point by point: a full round's findings only; once per
run (every round after it is blocking-only); a fresh fixer at step 4, not FINISH or the orchestrator;
the confirming round named by its heading; the cost stated as at least one round with PR465's r6-6 →
r7-3; the four records of **:3684-3686** (#294:162-166, #409:28, #412:26, #276:28). What goes beyond
resolution B — deleting the issue for everything else — rests on the owner's instruction, not on the
REOPEN ON. The mechanism reaches #409, #412 and #276; #294's class (a FINISH verifier's non-blocking
observation on the merging head) is not fixed, and goes into the PR description.
- **Round 1 revised seven of the ten `pr-harden` edits and killed none.** The settled blocking
  objections: the decline-all path (a fixer that declines everything commits nothing, and step 1's
  guard then reads the next round as *the fixer declined everything — did not converge*) — so step 3
  now routes it, and a finding naming only the PR description, straight to FINISH with the count
  re-recorded; the cap path's decline had no failure-mode sentence, against :377-379; a misattached
  "Otherwise go to step 7"; "Rounds 1 to 3 are full rounds" and "Through round 3 the fixer implements…"
  (:808, gate :7-8) left as universals the exception falsifies; and "nothing else it notices is
  implemented or filed" would have sent a blocking-only reviewer's other observations NOWHERE, making
  **:3690-3691**'s reopen condition impossible to meet — so they return as `notes`, copied into the
  run record unfixed, never to a fixer or an issue, and a finding the orchestrator downgrades in such a
  round is a note too (the parked path at **:3715-3718** now reaches rounds 2-3). Round 2 sent the
  notes to the PR description as well; see below.
- **The embedded measurement was already false when drafted** — "ten … eight … seven of those eight"
  missed #480, and #503 and #504 were filed while it was being written. The text now states the one
  chain with its times instead of a count a later reader would re-measure.
- **Three homes the gate found:** Step 0's takeover terminus (:77, now `phase: reviewed` with
  `blocking: 0`, since an exception's live fixer runs at `phase: fixing, blocking: 0`); the gate
  header's "Through round 3" (:7-8, four copies); and `resolve-ticket` Step 8's body sentence (:521),
  now pointing at FINISH's one-line-each list.
- **Round 1's OPEN scope objection** (a "follow-up suggestion" reaching the exception's fixer is
  never reviewed for scope, since only blocking-only rounds follow) is answered in step 3: a finding
  beyond the PR's own scope is declined there, not implemented.
- **Retired, with the measurement that retires it:** 0.27.0's *"file it yourself before you report,
  rather than offering to"* (the first window's P3, bar (a) on PR410/PR417/PR426/PR470). Its concern —
  a finding routed "to a follow-up" that nothing files is lost — is kept: a full round's finding is now
  implemented or declined in the PR, a decline and a FINISH verifier observation are named in the PR
  description, and a blocking-only round's notes reach the run record and the PR description.
- Gate: the unreviewed-head block message no longer routes non-blocking findings to a follow-up issue.
  Rendered through the real hook under a temp `HOME`, before and after.

**APPLIED after revision · `resolve-ticket` 0.19.0, `ticket-pool` 0.24.2 — the rest of the pipeline
files no issue either (P2).** `resolve-ticket`:663 "or a new ticket" becomes the report and the PR
description, extended at the gate's suggestion to items `/harden` deferred as outside the ticket (the
riders: #485:27 into #496, #494:11 into #498). Round 1: its bar-(c) claim for :663 FAILS — "or a new
ticket" is `resolve-ticket`'s own licence, not an issue the loop files — so it rests on the owner's
instruction alone. `ticket-pool`:326-327's in-your-name disclosure loses the follow-up issue, a true
(c) correction once P1 lands. This also gives **:3701-3704**'s *adjacent product defect … with nowhere
durable to go* a home.

**Round 2 — a fresh agent over the staged diff, before anything went live: ship with fixes.** It ran
the real staged hook under a temp `HOME` through every exception path — mid-fix, decline-all,
description-only, the cap, a blocker in the confirming round, a FINISH edit after the exception — and
each got the right verdict. Five fixes, each taken as given:
- **Notes had two destinations.** FINISH said what the loop did not implement goes in the PR
  description, while E3 and the confirming round sent a blocking-only round's notes to the run record
  alone. Taken on the side of the diff's own stated reason (*"with no issue filed, the PR is where it
  stays visible"*): notes reach both, and every home now says so. The case it decides is #482's — its
  item 1, a real defect later fixed by #487, was PR478's blocking-only r3 note (`…-472.md`:3).
- **The gate's `init|fixing` message said "no review round has yet reported zero blocking findings"**,
  false on the exception's normal path and pointing a run that lost context at a second full round.
  Now *"the head it will hand over has not been reviewed yet"* in the header comment, the reason and
  the systemMessage of both copies. `gate-test.sh` gains the case it lacked, `fixing, blocking 0 ->
  block`: passes on the edited and the original gate, fails on the always-allow copy.
- The scope sentence's passive "is declined" left the orchestrator as a possible decliner, against
  the Roles table; now the fixer declines, told so by its brief.
- State's `fixing` span, "until the next reviewer reports", was false on the decline-all path; now
  "until a reviewer's count is recorded again".
- A second home of E13's terminus, at *When the run finishes*.
Also from round 2, recorded here rather than in the text: the chain sentence departs from round 1's
wording — "each issue after the first", since #472 is the original ticket — and was re-verified on
GitHub; the cap path's decline is the orchestrator's, not a fixer's, which round 1 accepted as
procedural; a blocking-only round's notes have no state-file home and live in context until the run
record is written, as the follow-up material did before FINISH filed it.

**Parked.**
- **The step-3 exception is enforced by prose only.** The gate reads `phase: reviewed, blocking: 0` on
  the reviewed head as converged (`pr-harden-gate.sh`:62, :348-407), so a run that goes from RECORD
  straight to FINISH drops the findings with every check green; the FINISH rule it replaces, prose only
  as well, went unfollowed on 4 runs (**:3558-3559**). Cheapest lever named by the gate: record `--phase fixing` at step 2 when
  the exception fires. **REOPEN ON:** a record where a clean full round's non-blocking findings reached
  FINISH neither implemented nor declined.
- **Caps.** Under a caller cap N, an exception at round N-1 whose fix brings a blocker ends as
  did-not-converge where it converged before; and after an exception at round 3, a FINISH-owed round is
  round 5 — whether FINISH's owed round counts against the cap was never stated. The pool passes no cap
  (`pool-run`:1644). **REOPEN ON:** a record where either cost a convergence.
- **"Taken at most once per run" is recorded by no state field** (`gate-state`:380-381); after a
  compaction a run could fire it again at round 2 or 3, bounded by the full-round limit and the cap.
  **REOPEN ON:** a record where it fired twice.
- **Cost: at least one more round on most runs** — the gate header calls a terminating round with a
  non-blocking finding "the normal case" (:18); the three precedents (PR414, PR423, PR478) each paid
  one confirming round that returned zero blocking. **REOPEN ON:** a window of 0.29.0 records, read
  for rounds per run and for any 8h kill.

**Corrections to the record.** #500 did not come from a fixer decline, as the draft said: PR499's loop
ran one round and spawned no fixer (`0e6a9f09`), the decline at `…-496.md`:22 is the plan-time
refuter's, and FINISH filed round 1's r1-1. Resolution B's REOPEN ON runs to **:3634**, not :3633.

**Mirrored in the same commit:** `…-498.md` (22:06), unread for derivation like `…-494.md` and
`…-496.md`. **And a record the store lost:** the second run on #488 (PR #501) wrote its record over the
first run's (PR #490) under the same name, where both skills say *append*. The first run's record is
restored from `846a225` at `…-488.md`, and the second's is kept beside it as `…-488-PR501.md`, in the
store and the repo alike.

## 2026-09-24 (window: 10 run records — #494/PR497, #496/PR499, #498/PR502, #488/PR501, the four mirrored unread by `846a225`/`278d42f`; and #433/PR511, #451/PR510, #469/PR507, #476/PR506, #477/PR509, #505/PR508, the first six under `pr-harden` 0.29.0) — 4 applied after revision, 1 as drafted, 0 killed, parked below; one refutation round; linter 10 files, 0 findings; `pool-test.py` 575 passed / 0 failed, and the two new guards each fail it when removed (571/1 and 574/1)

Proposals and the gate's objections: `proposals/2026-09-24-window-494-505.md`. The gate killed nothing,
revised P1, P2, P3 and P5, and re-aimed P5 at a root cause it traced. So once again this bar was
exercised by revision. Launch mode, from `ledger.json`: #433 and #451 were pool-launched (unattended);
the rest were `launched_by: work`. The draft's transcript line numbers ran one below the JSONL's
1-indexed lines. The citations below use the gate's corrected numbers.

- **P1 APPLIED after revision (pr-harden 0.30.0, harden 0.42.0, resolve-ticket 0.20.0): spawn
  delegated agents with `run_in_background: false` where the schema carries it.** Bar (c):
  `pr-harden`'s *Collecting in the same turn* said the flag did not exist (*"as of 2026-09-20"*).
  Followed as written, a spawn takes the default, which is BACKGROUND, and the skill's own unattended
  gate refuses the yield. Measured 2026-09-24: #451 passed `False` on all 9 `Agent` calls. Its four
  Phase 2 lenses (transcript 294-297) returned at 300-303, 342.5s of `totalDurationMs` in 165s of wall
  clock, with no Stop block. #433's wave 1 (391-398) omitted the flag, got "Async agent launched" four
  times, and was blocked at 403/406 and 476/479. Its later waves passed `False` and came back in-turn.
  The flag by date, over pipeline transcripts: present 09-16/17, absent through 09-14, the string
  `'true'` on 09-23, `False` on 09-24. So **:3338-3348's prune measured a real absence on 09-20,
  and the lever came back afterwards.** P1 concerns yields, not polls, so it does not reopen :3350-3361's
  killed polling proposal. Revised per the gate: conditioned on the schema carrying the flag, with
  #433's fallback (a bounded foreground wait, after which its notices were delivered). Also scoped, per
  the gate: `pr-harden`:986's "Every phase here delegates to a background subagent", and harden's
  await paragraph, which steered #433 into background spawns plus an await. Deleted: "the report
  arrives by itself in the completion notification's `<result>`", the 2026-09-20 italic sentence, and
  "What outlived the tool…".
- **P2 APPLIED after revision (pr-harden 0.30.0): wait on a build, boot or lock with a foreground loop
  bounded under the tool timeout, with a failure signature. Do not background it and yield.** Bar (a):
  #479:20, #489:18, #498:20, #505:20, each an attended run blocked mid-flight and each recovered by
  such a loop. The gate found the same block, unrecorded, in #469, #476 and #477's transcripts. This
  closes two parks: :3766-3770's (the gate showed the pre-edit `pr-harden`:1084-1087, the ORCHESTRATOR's *"background
  the wait and let it notify you"*, settles "who the sentence binds", and that sentence is the one
  rewritten); and :3231-3236's *no contract for a background BUILD as an await* (REOPEN ON "one where the
  missing contract costs a yield", met by #505:20). It closes the other way: a foreground loop works
  attended and unattended, while an await only licenses an attended yield. Revised per the gate: the
  draft's example (#505's loop) could not tell done from timed out, against :568-570. The shipped loop
  exits on a failure signature and a bound, and was run here against a file that gained `BUILD FAILURE`
  after 3s (exit at 3s) and one that stayed empty (exit at the bound). **Not edited, reported to the
  owner:** `~/.claude/CLAUDE.md`'s *Wait on a condition* says "let the completion notification wake
  you", the yield the gate refuses mid-run.
- **P3 APPLIED after revision (resolve-ticket 0.20.0): Step 3 carries the refuter's await as `await …
  --only pr`.** Bar (a): after 0.28.0, #451:19 and #505:21, one failed call each. Before it, #496:18 and
  #488-PR501:21. Measured in a temp HOME: bare `await` → exit 2 (needs `--run`), `--only pr` → exit 0.
  Revised per the gate: "No harden entry exists yet" was unchecked, and a stale one can exist, so the
  text now says "`/harden` has not started yet". Full `~/.claude/pipeline/gate-state --owner $PPID`
  prefix.
- **P4 APPLIED as drafted (pr-harden 0.30.0): `clear-await` takes no label.** Bar (a): #480:24, #494:18,
  distinct runs. Measured: `clear-await refuter --only pr` → exit 2. It replaces the second copy of
  "Kept apart from the transition write above…".
- **P5 APPLIED after revision (pool-run, gate-state, pool-test.py): a claim clears the leftover gate
  entry at its worktree, and `pr-set --phase building` warns on an entry that still names a PR.**
  Bar (c): `resolve-ticket`:170 says "no `pr` yet", and the script kept #483's `pr` and ledgers into
  #477's building entry (#477:26; the transcript shows it at 1059 and the clear at 1087). What it would
  have cost uncleared: `pr-harden` Step 0 refuses a fresh entry claiming another PR, and `pool-run`:2004
  credits a PR-less death with the entry's `pr`. **The gate traced the cause the draft said was not
  established.** `logs/pool-20260923T173457Z.md`:6 shows #477's first `--work` session reaped with no
  release. `claim_slot` recreated the same worktree path without the `clear_gate_state` that the driven
  path (:3233) and `release_claim` (:1200) both call. The primary fix mirrors that call. The drafted
  gate-state drop was **not taken**: the gate showed it would null a run's own PR and ledger if Step 1
  were re-run after Step 8 (compaction, a resume ignoring `RESUME_PROMPT`). It also named fields,
  which the `adopt()` docstring records as a leak per pass, and `override_reason` was already missed.
  So the gate-state half only warns and changes nothing. Tests: the reaped-claim case, the warning,
  its keep and its silence on a `--pr` write. Removing the `claim_slot` call → 571/1. Disabling the
  warning → 574/1.

**Net:** pr-harden +10, resolve-ticket +7, harden +1. The growth is P2's loop, which replaces a
one-line instruction that the gate refused on seven runs, and P3's snippet, which replaces a pointer
that cost a call on four. Deleted: three sentences of P1's paragraph and P4's duplicate.

**0.29.0's REOPEN ON conditions (:3885-3902), read against its first six records.** Rounds: #433 2,
#451 1, #469 2, #476 1, #477 2, #505 2. Four of six paid the confirming blocking-only round, each
returning 0 blocking. #451's fixer committed nothing, so no round was owed. The longest run was #477
at 8594s, and nothing was killed. That cost is what 0.29.0 stated, so it does not reopen. The
prose-only exception, the cap and fired-twice conditions: no record shows any of them. No pipeline run
filed an issue: `gh issue create` appears 0 times in each of the six transcripts. The positive controls
are `f79f9941…` (3; the non-pipeline session that opened #512-#516) and `11df53f2…` (the owner-directed
pass that opened #505 as a consolidation).

**Parked.**
- **harden Phase 2 run with fewer than four agents: 4 records** (#479, #488, #491, #476:18). REOPEN ON
  unchanged: #476's r1 raised nothing.
- **The orchestrator editing while a delegated agent runs: 2 records** (#485:23, with a read-only
  refuter; #477:23/:28, where the refuter probed production files). The gate corrected the draft's
  framing: what the two share is the orchestrator's edit, not a mutating refuter. The rule exists
  (`pr-harden`:1052). **REOPEN ON:** a record where it cost a commit or a round.
- **Phase 2 escalating repeatedly on the run's own fixes: bar (b) met, no remedy identified** — #477
  (5 cycles, labelled override), #433 (3 cycles), #482 (parked :3775). **REOPEN ON:** a proposal that
  names what would have stopped the chain earlier without losing a real defect each pass found.
- **chartsearchai's nested `CLAUDE.md` byte budget: bar (a) met, target-repo property** — #433:10,
  #469:23, #477:29, 1-2 builds each. **REOPEN ON:** a skill-side lever, e.g. a budget check the skills
  could run before the build.
- **Usage-limit interruption: 2 records** (#476:16, #505 transcript 413), no convergence lost.
- `gh issue view` empty at exit 0: 4 pre-0.18.1 records (#494:17, #496:17, #498:18, #488-PR501:20),
  already addressed by `99594f1` and `669b906`. None of the six 0.29.0 records mention it.
- 1 record each: an unfilled PLAN_PLACEHOLDER in a refuter brief (#469:21); a refuter's own doc-sweep
  list incomplete (#433:23); main took the ADR decision number mid-run (#477:30).

## 2026-09-24 (second window of the day: 2 run records — #438/PR517, #454/PR518, both pool-launched 02:55:52Z, 52s after `dcafb6c`) — 0 proposed, 0 applied, 0 killed, parked below; no refutation round (nothing to refute); linter 10 files, 0 findings

Both transcripts carry the 0.30.0 / 0.42.0 / 0.20.0 text (`run_in_background: false` 3 times each, the
2026-09-20 *"neither exists in the harness"* sentence 0 times), so these are the first two records under
`dcafb6c`. **Its P1/P2, read against them:** every `Agent` tool_use passes `"run_in_background":false`
(22 and 24 occurrences over 11 and 13 `Agent` calls), and the gate's refusal strings (`ended your turn`,
`is mid-run and has not`) occur 0 times in either. Two records, both unattended; not yet a window.

**Parked.**
- **Prose the change itself made false, found by a fresh agent rather than the author's sweep: 3 records**
  — #433:23 (parked above as a refuter's incomplete doc-sweep list), #438 (gate: ADR 101 paragraph; harden
  P2: ADR "one delta becomes one frame" x2; r1: test javadoc, cost one blocking-only round), #454 (harden
  P2: "is what every OVERSIZED case asserts", two further homes found only by the second Phase 2). Bar (a)
  is met but no remedy is: `harden`:497 and `pr-harden` *Correcting a claim means finding every home of
  it* already prescribe the subject enumeration, and the fresh agents caught every instance before merge.
  More prose on a rule that exists is the class the previous entry declined. **REOPEN ON:** a mechanical
  lever (a check a script can run), or a record where such prose reached a merged head.
- **main moving under a run: 2 records** — #477:30 (took the ADR decision number), #438 (moved between
  the first read and branching from `origin/main`; a script's anchor assert caught it, cost one re-read).
  Different shapes, both cheap, both caught by an existing assert. **REOPEN ON:** one that cost a round.
- **Verifier "could not determine" on the local instrument, settled by a substitute instrument: 1 record**
  (#438: llama-server emits whole UTF-8 per delta; a fake OpenAI-compatible server behind the remote engine,
  with a pre-fix positive control, reproduced the split). `pr-harden` FINISH's "could not determine stops
  as converged-but-unverified" was avoided, not contradicted.
- **Plan going past the ticket's recommended fix when measurement shows it misses the ticket's own
  regression: 1 record** (#454: the ticket's 16 MiB literal left its own mutation 11/0 green; handled as an
  assumption, the gate accepted it).
- #438's r2 note (token/preliminary separation unpinned) went unfixed, which is what `pr-harden` :115-118
  prescribes for a blocking-only round's notes — not a defect.
