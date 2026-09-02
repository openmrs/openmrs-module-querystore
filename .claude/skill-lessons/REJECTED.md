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
signal)" — the signal both #340 and #336 show reading green over stale bytes; the new bullet says so at
the deploy step rather than re-writing step 4.

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
- **The proposer not verifying its own citations: 13 cycles / 11 records** — previous 12/10 (:1320).
  This pass added three of its own, all cut at the gate: the superseded checkout count, the #356/0.16.0
  mis-attribution, and the wrong 2026-08-24 block for the `cp` kill.
- **A retro submitting wording that breaks the counts/universals rule it enforces: unbroken.** This
  pass's P4 shipped "no production mutation reddens it" and P2 "a retry spent before it changes
  nothing"; the gate cut both as unverified universals.
