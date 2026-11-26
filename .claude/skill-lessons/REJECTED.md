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

## 2026-09-24 (third window of the day: 2 run records — #408/PR519, #458/PR520) — 1 proposed, 0 applied, 1 parked by the refuter; linter 10 files, 0 findings

**Parked by the refuter.**
- **resolve-ticket Step 5 assumes a production change, on a non-fix deliverable: 2 records, bar (a) met,
  0 cost** — #480:23 (measurement; guard red first for the missing file) and #458:19 (test-only; red only
  under a needle-rename mutation, 4/4 green pre-change per #458:8). Draft P1 added a sentence to Step 5.
  Refuter: neither record reports a cost (#480:23 "Worked but not described by the skill"), so :3772-3774's
  "any text is an unchecked claim" applies; the draft's "changing production code reads as producing that
  deliverable" collides with Step 5's "Never by changing the test… or the test data" (:411), leaves Step 3
  q4 (:288-290, "would pass on the pre-change code proves nothing") able to block a test-only plan, and
  "failing against the pre-change base" is false of #458. **REOPEN ON:** a record where Step 5 or Step 3
  q4 cost something on a non-fix deliverable (a gate blocking a test-only plan on :290, a run bending
  test/data to reach green, or a run skipping red for "no production change"). If applied, word the green
  as "the guard passing once the mutation is reverted or the recorded data lands".

**Parked counts advanced.**
- **Prose the change made false, found by a fresh agent: 4 records** (+#458: harden P2 found the method
  javadoc "passes against the pre-change code" and SourceScan's "Every lookup fails LOUDLY" false, one
  Phase 1 re-convergence each). Still no mechanical lever; none reached a merged head. REOPEN unchanged.
- **Phase 2 escalating repeatedly on the run's own fixes: 4 records** (+#458: Phase 2 three times on a
  ~15-line test diff, each pass one real falsehood). Record says the rule worked as written; no remedy
  named. REOPEN unchanged.
- 1 record each: a ticket's stated mutation not reaching the row it names, refuted by measurement at 0
  cost (#408); harden's full cycle as a fixed cost on a 2-line change, lenses under a minute each (#408).

## 2026-09-24 (fourth window of the day: 2 run records — #513 (aborted, no PR), #516/PR522) — 1 derived, killed at this ledger before the gate, 0 applied; no refutation round (nothing survived to refute); linter 10 files, 0 findings

**Killed before the gate.**
- **Draft: harden Phase 2 corrects a sentence the change made false in place, without escalating.**
  Rests on #516 ("Phase 2 escalation on a one-sentence false javadoc bought a whole extra Phase 1 + a
  second 4-agent Phase 2; the second Phase 2 found only polish") and the escalation park, now 5
  records. Killed on `harden`:209-213 — "a false universal in a javadoc is still substantive", with
  #229's seven one-prose-defect passes behind it — and on two records where an escalation on prose
  caught a second real defect: #433:14 (the narrowed javadoc was false the other way) and #458:13
  (SourceScan's "Every lookup fails LOUDLY", the root claim behind a vacuous guard). The draft would
  have lost both. It fails the escalation park's REOPEN ON by that condition's own words.

**Parked counts advanced.**
- **Phase 2 escalating repeatedly on the run's own fixes: 5 records** (+#516: one escalation, on a
  false javadoc; the second Phase 2 found only polish). REOPEN unchanged.
- **Prose the change made false, found by a fresh agent: 5 records** (+#516: harden P2 javadoc "Not
  shared with measure", the canonical FindingPartnerCoverage paragraph, the containment-residue
  direction). None reached a merged head. REOPEN unchanged.
- **chartsearchai's nested `CLAUDE.md` byte budget: 5 records** (+#513: 7 bytes of headroom, +#516:
  11 bytes). Both were caught by the Step 3 gate as blocking, at 0 rounds, which is a skill-side catch
  already working. REOPEN unchanged.

**Parked, 1 record each.**
- resolve-ticket Step 3's one re-run after a SETTLING pass-1 objection cost ~6 min and found nothing
  (#516). Not a contradiction: outcome 2 (:356-360) governs the pass AFTER the re-run, and the re-run
  exists for the measured case where pass 2 caught a claim the pass-1 revision introduced
  (:342-350). **REOPEN ON:** a second record of a re-run finding nothing after a settling objection, read
  together with the rate at which re-runs catch revision-introduced claims.
- resolve-ticket Step 3's "before any code" was overridden: #513 implemented the plan before
  re-gating so the full suite could measure what reddened, and that measurement decided the abort
  (condition 3), at 0 rounds. Consistent with :363-375's "check it, do not estimate it", which binds
  the run and involves building. **REOPEN ON:** a record where code before the re-gate cost something.
- #513's driver capture ("left its gate entry unfinished: phase=building … override=True") is the
  abort having recorded its override as :82-83 requires, not a defect.

## 2026-09-24 (fifth window of the day: 2 run records — #514/PR524, #477/PR523) — 1 applied after revision (three edits, one driver change), 0 killed, parked below; one refutation round; linter 10 files, 0 findings

Proposals and the gate's objections: `proposals/2026-09-24-window-514-477PR523.md`.

**A record the store lost, again.** The #477/PR523 run did `cat > …-477.md` (`608ffbd8…`:785) over the
#477/PR509 record pushed at `dcafb6c`. Found at Step 6's pre-read comparison. PR509's record restored
from `origin/main` at `…-477.md`, and PR523's kept beside it as `…-477-PR523.md`, as with #488 (:3909).

- **P1 APPLIED after revision (resolve-ticket 0.21.0, pr-harden 0.31.0, skill-retro 0.2.10, pool-run):
  a run record is appended with `>>`, and the driver reads the LAST record's outcome.** Bar (a): three
  separate overwrites, all `cat >` on a date+ticket name — #305 (2026-09-07, one run's pr-harden record
  over its own resolve-ticket record, `28d5f7ec`:2166 then :3523; the resolve-ticket record is lost for
  good, `5871bcf` already had only the pr-harden header; found by the gate), #488 (`39ed9650`:769,
  `d52a1da1`:531) and #477. The name is kept because `pool-run` `record_written` attributes by
  `[-_]<num>(-driver)?$` at the stem end. Revised per the gate:
  - edit 1's reason names the same-run case (#305) alongside the second-run case;
  - edit 2, BLOCKING as drafted: an append bumps the older file's mtime, so `un_retroed()` (mtime vs
    `LAST`) and Step 1's "since the last retro" re-read runs an earlier window counted — measured by the
    gate running the real `un_retroed()` on this store, which returned the restored `…-477.md`. Step 1
    now says to count only records whose header's PR the ledger does not already name;
  - edit 3: the anchor is the last `# ` line immediately followed by `outcome:`, not the last `# `
    chunk, because a fenced shell comment is also a `# ` line. Falls back to the old first-12-lines read.
    Over the 121 store records the new reader agrees with the old one (0 differences, 3 aborted). The
    new `pool-test.py` cases against `origin/main`'s reader: 2 of 5 FAIL (converged-then-aborted, and
    aborted-then-converged), 5 of 5 pass with the new one. The converged fixture is padded past 12
    lines, since the short one passed the old reader by accident.
  Prunes nothing: it makes "Append" concrete where three runs read it as "write". Net: +5 lines each
  in resolve-ticket and pr-harden, +3 in skill-retro, +10 in pool-run, +20 in pool-test.py.

**Gate's observation, not proposable (0 records):** `capture_record` writes `-driver.md` with
`write_text` under a date+ticket name, so two record-less deaths on one ticket and UTC date would
overwrite. The three `-driver.md` files each hold one capture. **REOPEN ON:** a lost driver capture.

**Parked counts advanced.**
- **Phase 2 escalating repeatedly on the run's own fixes: 7 records** (+#514: six Phase 2 rounds, five
  escalated; +#477-PR523: one). No remedy named that keeps each pass's real defect. REOPEN unchanged.
- **pr-harden rounds whose blocking finding the previous round's fix introduced: 1 record, bar (b) met,
  no remedy** — #514:21-22, r2->r3->r4 on a free-prose recogniser widened per finding, ending at the
  round cap with the PR left draft, which is the cap working. Same family as the escalation park.
  **REOPEN ON:** a second record, or a proposal naming what the fixer could have done differently.
- **Prose the change made false, found by a fresh agent: 7 records** (+#514, +#477-PR523:16). REOPEN
  unchanged.
- **chartsearchai's nested `CLAUDE.md` byte budget: 6 records** (+#477-PR523:26, 7 bytes left, a
  pointer declined). REOPEN unchanged.
- **main moving under a run: 3 records** (+#514:12, ADR 116 taken by #523, renumbered at rebase, 0 cost).
  REOPEN unchanged.
- #514:11's "these N legs are unpinned" residue list (2 harden cycles, stopped at deletion) is
  `harden`:489 followed late, not a new lesson.
- 1 record: `gate-state` has no remove subcommand; an entry written with placeholder values was removed
  by hand under the lock (#514:27, no harm).

## 2026-09-24 (sixth window of the day: 2 run records — #512/PR525, #515 driver capture) — 0 proposed, 0 applied, 0 killed, parked below; no refutation round (nothing to refute); linter 10 files, 0 findings

Step 6's pre-read comparison: `origin/main` and this store agreed on every file except these two,
local-only. No other file changed since `c37a8e7`, so no earlier window's record is re-counted.

**#515 is not skill evidence.** The driver capture says `claude -p` exited 1 with an empty `.err`. The
stream's last lines (`logs/20260924T102400Z-…-515.jsonl`) are an `api_retry` and then *"API Error: No
response from API (waited 4m, then 10m on the retry)"* (`is_error: true`, 95 turns, 2666s). That was
mid-Step 5 (the last tool call edits the REST controller after *"New test is green"*). The gate entry
it left (`phase=building`) is what `claim_slot`'s clear from the fifth window (P5) exists for.

**Parked counts advanced.**
- **Phase 2 escalating repeatedly on the run's own fixes: 8 records** (+#512: one escalation, a preview
  test that could not tell a read of the focused chart from a literal ABSENT, one Phase 1 pass). That
  escalation caught a real defect, which is the park's REOPEN ON condition read the other way. Unchanged.
- **Prose the change made false, found by a fresh agent: 8 records** (+#512 r2: the PR body's "on both
  paths", description-only, non-blocking). A second false PR-body claim ("each new test failed before
  the production change") was the author's own self-reread, so it does not count here. None reached
  a merged head. REOPEN unchanged.
- **main moving under a run: 4 records** (+#512: ADR 116 taken by #523 between branch cut and ADR
  write, renumbered to 117 before the PR, 0 cost). #514 hit the same collision with the same PR. It
  still has not cost a round. REOPEN unchanged.

**Parked, 1 record each.**
- A verification the ticket's comment mandated (standalone + loopback) was missing from the PR, and
  was blocking at r1 (#512:16). Cost: one verifier run, no code round. `resolve-ticket`:165 already
  makes the comments mandatory reading. What the run missed was carrying the comment's verification
  into the PR, not reading it. **REOPEN ON:** a second record, or one that cost a code round.
- No documented recipe for loopback capture of the LOCAL engine (#512:21). The verifier improvised a
  wrapper at `appdata/chartsearchai/bin/llama-server` and a raw-TCP proxy, which worked first try.
  This belongs with #438's substitute-instrument park: a verifier building its own instrument, and
  twice now it did not cost a round. **REOPEN ON:** an improvised instrument that fails or costs a round.
- A run killed by an API no-response timeout (#515). It is infrastructure, and so is the
  usage-limit park (2 records), but the mechanism differs. #369:66's per-agent 429/529 deaths are a
  third shape. **REOPEN ON:** a skill-side lever, e.g. a resume that the driver could offer.

## 2026-09-24 (seventh window of the day: 2 run records — #407/PR526, #505/PR529, the latter appended under #505/PR508's record, which the first window counted) — 1 applied after revision, 0 killed, 1 parked by the refuter; one refutation round; linter 10 files, 0 findings

Step 6's pre-read comparison: `origin/main` and this store agreed on every file except
`…-407.md` (local-only) and `…-505.md` (PR529's record appended). Proposals and the gate's objections:
`proposals/2026-09-24-window-407-505PR529.md`.

- **P1 APPLIED after revision (pr-harden 0.32.0): a delegated agent waits inside its turn.** Bar (c):
  the verifier brief's *Wait on a CONDITION* prescribed `Bash(run_in_background: true)` "which hands
  the turn back at once and notifies you", while *And this session must not busy-wait either* (0.30.0's
  P2, :3946-3952, which rewrote only the orchestrator's sentence) refuses exactly that. Bar (a): #238:29
  (verifier armed a Monitor, returned "Standing by…", subagent `a36255013146dae6f` :28/:50) and
  #407:17 (reviewer, `a19d1965d75bd2978`, two background tasks, returned at :94). **The gate found what
  neither record states, re-measured here:** #407's `bkizeigcv.output` reads `[killed]` with mtime
  14:58:21+0300, the second the report returned, so the resumed reviewer waited ~9m42s on a dead build.
  Revised per the gate: the "a FOREGROUND poll still costs a turn per look" sentence reworded rather
  than left arguing against the fix (BLOCKING); *The harness disagrees with itself here* replaced, not
  just deleted, by a paragraph naming Monitor and background tasks, since every harness text steers
  there (BLOCKING); `[killed]` stated as #407's measurement; re-run at the bound for a long boot; the
  server launch stays detached; #407's "~20 min" not carried (transcript ~12.5 min); the brief clause
  is its own sentence, not under the restore paragraph's eleven-agent evidence. The loop was run here
  against :8081 (exit 0s) and a dead :8099 (exit at the 6s bound). Net +6 lines.

**Parked by the refuter.**
- **harden's KIND-of-question rule triggered at the second escape: 2 records** (#482:21, #505/PR529:37
  and :48 — one event, the same constant parser, not two). Killed as prose on :3775-3776's reason (rule
  exists, not applied; #505:48 says it "fired late") and on :2029, where tightening a trigger measured
  at the third sweep to the second was killed; neither record measures that a change at the second
  escape closes the loop cheaper. **REOPEN ON:** a mechanical lever, e.g. harden state counting
  successive escapes against one guard.

**Parked counts advanced.**
- **Phase 2 escalating repeatedly on the run's own fixes: 9 records** (+#505/PR529: five escalations,
  labelled override). REOPEN unchanged.
- **Prose the change made false, found by a fresh agent: 10 records** (+#407 harden P2 and r2, a seam
  between commits fixed at FINISH; +#505/PR529 ~30 "verbatim" homes, and the top-level `note` key every
  metadata sweep missed). None reached a merged head. REOPEN unchanged.
- **pr-harden rounds whose blocking finding the previous round's fix introduced: 1 record** — #505/PR529's
  r1/r3 were scan gaps the fixes did not introduce, so they do not count. REOPEN unchanged.

**Parked, 1 record each.**
- A Phase 2 lens's mutation probe overwrote the surefire report the fixer's green rested on (#407:19,
  one root re-build). `harden` Phase 2's *Only ONE of them may mutate the worktree* covers the shape.
  **REOPEN ON:** a second record, or one that cost a cycle.
- Refuter's scope narrowing ("one case pins it") overturned by a reviewer's per-conjunct measurement
  (#407:8/:18, 1 fixer pass inside r1's non-blocking exception). **REOPEN ON:** a second record.
- `maintainerCanModify=false` on a same-repo PR (#505/PR529:49), no cost.

## 2026-09-24 (eighth window of the day: 2 run records — #394/PR531, #429/PR530, both 1 round, converged) — 1 proposed, 0 applied, 1 parked by the refuter; one refutation round; linter 10 files, 0 findings

Proposals and the gate's reply: `proposals/2026-09-24-window-394-429.md`.

**Parked by the refuter.**
- **resolve-ticket Step 1: a ticket's measurement taken on another sha must be re-measured on HEAD:
  1 record** (#394:11, gate-blocking, one extra api-suite run). Drafted as bar (a) with #408, extending
  Step 1's "a measurement in a comment outranks a claim in the body" (:165-167). Refuter: #408:9 is a
  different class. That ticket's mutation missed its row "on today's selftest, and on the base", so it
  was never stale, just wrong. This ledger already logs it as its own 1-record item (:4062-4063). The
  draft's "no longer reached" was unsupported. Both measurements sat in the ticket BODY, with no
  comments, so the anchor was the wrong sentence. The costs (one suite run, and 0) repeat :3772-3774's
  "no cost measured, so any text is an unchecked claim", and Step 3 q6 plus #408's author measurement
  already caught both. **REOPEN ON:** a record where a ticket or comment measurement from an older sha
  was carried into a plan or PR and cost at least one round, or reached a merged head. Anchor on the
  ticket as a whole, and cite only #394 for staleness.

**Parked counts advanced.**
- **resolve-ticket Step 5 assumes a production change, on a non-fix deliverable: 3 records, 0 cost**
  (+#429:17, a doc-only ticket, "Mutation probes stood in as the evidence"). None of the three reports
  a cost. REOPEN condition at :4049-4052 unchanged, and not met.
- **Orchestrator yield on a background build/Monitor refused by the Stop gate, AFTER pr-harden 0.30.0's
  foreground-loop rule: 1 record** (#429:18, one refused yield, recovered with a foreground until-loop,
  as the rule prescribes). The rule exists and was not applied. The owner-level `~/.claude/CLAUDE.md`
  *Wait on a condition* still says "start long work with `run_in_background` … and let the completion
  notification wake you", the steer :3951-3952 reported and left unedited. **REOPEN ON:** a second
  post-0.30.0 record, or one that cost more than a turn.

**Parked, 1 record each.**
- A harden P2 lens finding a new parenthetical restated what moved rather than why (#394:12, one polish
  commit). No cost beyond polish.

## 2026-09-24 (ninth window of the day: 2 run records — #273/PR533, #430/PR532, both converged) — 2 proposed, 1 applied after revision, 1 parked by the refuter; one refutation round; linter 10 files, 0 findings

Step 6's pre-read comparison: `origin/main` and this store agreed on every file except
`…-273.md` and `…-430.md` (local-only). Proposals and the gate's reply:
`proposals/2026-09-24-window-273-430.md`.

- **P2 APPLIED after revision (resolve-ticket 0.21.1): the autonomy contract points at `pr-harden`'s
  bounded foreground wait.** Closes the eighth window's park *Orchestrator yield on a background
  build/Monitor refused by the Stop gate, AFTER pr-harden 0.30.0* on its own REOPEN ON ("a second
  post-0.30.0 record"): #429:18 and #273:21 (resolve-ticket Step 6, under 0.21.0). resolve-ticket had no
  waiting guidance, and the rule lived only in `pr-harden`. Revised per the gate: #429 names no step, so
  the sentence sits in *The Stop gate covers the whole run*, not Step 6. Net +2 lines, a pointer rather
  than a restatement. **Still not edited, reported to the owner:** `~/.claude/CLAUDE.md`'s *Wait on a
  condition* still steers to `run_in_background` + the completion notification.

**Parked by the refuter.**
- **pr-harden step 1's identical-head guard omits the blocking PR-description fix: 2 records / 0 cost**
  (PR393:40-46, #273:23). Re-proposed as bar (c). Killed back to this park on :1972-1980's ruling (a gap,
  not a contradiction). The guard fired and was resolved at no cost. REOPEN ON unchanged. Readmission
  wording is in the proposals file.

**Parked counts advanced.**
- **Prose the change made false, found by a fresh agent: 11 records** (+#273 harden P2 c1: "PR #271"
  called an issue, "the display name is not a match at all", "json is the DEFAULT format" at 9 homes, and
  r2's PR body "Not done here" listing r1's work as undone, blocking, 1 round). REOPEN unchanged.
- **Phase 2 escalating repeatedly on the run's own fixes: 11 records** (+#273 one escalation, +#430 one
  escalation, each on a real substantive finding). REOPEN unchanged.

**Parked, 1 record each.**
- An author's control measured on `main` confounded by an unrelated later change (#296's trim), and
  re-measured on the historical sha (#273:8). Caught at the gate pre-code, one throwaway run. It is
  related to the eighth window's other-sha park (#394) but is the author's own control, not a ticket's
  measurement. **REOPEN ON:** a second record.
- A background root build started before the last edits landed, killed and re-run (#273:21, one stale
  build). **REOPEN ON:** a second record.
- The author's own mutation probe blind to a case it claimed to cover (#430:9, :14 — the spelling sort
  cannot flip 0.5/5), found by harden P2, 1 cycle. **REOPEN ON:** a second record.

## 2026-09-24 (tenth window of the day: 2 run records — #272/PR534, 1 round, and #432/PR536, 4 rounds / 4 cycles, both converged) — 1 proposed, 0 applied, 1 killed; one refutation round; linter 10 files, 0 findings; **no skill file changed**

Step 6's pre-read comparison: `origin/main` and this store agreed on every file except `…-272.md` and
`…-432.md` (local-only). Proposal and the gate's reply: `proposals/2026-09-24-window-272-432.md`.

**KILLED · P1, resolve-ticket Step 3: a gate objection that narrows coverage is settled by the mutation
it exempts.** Drafted as bar (a) on #407:8/:18 plus #432:8/:13, and bar (b) on #432 alone. Subsumed by
`harden`:272-278 (*an exemption you WRITE into a guard … build the case the exemption ADMITS*), which
is what caught #432's exclusion in harden c1 P2. Bar (b) was misapplied: #432:8 records ONE cycle, and
`skill-retro`:75 needs two. The draft cited #432:9/:14 for :8/:13. The two records are different
classes: #407 is a reasoned sufficiency cut, while #432's gate objection was correct and only the
remedy the run adopted was too coarse (transcript jsonl :132, :135). A whole-class exclusion also
names no single mutation to run. The #407 park above stays open with its REOPEN ON unchanged, and #432
is logged below as the exemption rule working in its home.

**Parked counts advanced.**
- **harden's KIND-of-question rule triggered at the second escape: 3 records** (+#432:21). Four
  successive escapes on one guard (direct pass, method handle, setter, putfield), 3 harden cycles + 1
  round. The record itself says the rule was "applied one cycle later than it could have been". The
  positive property it produced (field final) was then escaped once more, at r1, which is the rule's
  own *a differently typed question is not a closed one*. **REOPEN ON** unchanged (a mechanical lever),
  and not met.
- **pr-harden step 1's identical-head guard omits the blocking PR-description fix: 3 records / 0 cost**
  (+#432:22, r3's body-only fix). The guard fired and the cause was known. :1979's REOPEN ON (an identical
  head mis-classified, costing or skipping a round) is not met.
- **Prose the change made false, found by a fresh agent: 13 records** (+#272 harden P2, a test comment
  false for the as-needed case; +#432 r3, the PR body's "Not done here" made false by r2's fix, blocking,
  1 round). REOPEN unchanged.
- **Phase 2 escalating repeatedly on the run's own fixes: 12 records** (+#432, three escalations, each
  on a substantive escape). REOPEN unchanged.
- **`git checkout -- <path>` discarding the ORCHESTRATOR's own uncommitted work: increments only**
  (+#432:10, a production edit lost after a mutation loop, ~3 builds). #432 did not commit first, so
  the reopen (an incident where "commit before probing" WAS followed) is still unmet.

**Observed, no remedy owed.**
- `harden-set … --only harden` rejected (#272:15, one retry). `gate-state`'s argparse message, added
  after #293/#256/#263/#330, fired and named where `--only` is accepted. The shipped remedy worked.
- `harden`:272-278's exemption rule caught a by-name class exclusion in its home (#432:8/:13, 1 cycle).
- #272's gate refuted a plan claim that a rounding mutation would redden tests (6 and 8 both divide 24)
  before any code was written, at 0 cost. The gate working as designed.

## 2026-09-24 (eleventh window of the day: 3 run records — #443/PR538, 2 rounds / 2 cycles; #459/PR537, 1 round / 2 cycles; #527/PR535, 2 rounds / 5 cycles plus its pr-harden record; all converged) — 0 proposed, 0 applied, 0 killed; no refutation round (nothing to refute); linter 10 files, 0 findings; **no skill file changed**

Step 6's pre-read comparison: `origin/main` and this store agreed on every file except `…-443.md`,
`…-459.md` and `…-527.md` (local-only).

**Parked counts advanced.**
- **Phase 2 escalating repeatedly on the run's own fixes: 15 records** (+#443:14, one escalation;
  +#459:12, one; +#527:30, four, on route enumerations and restatements of the echo rule). #527 names
  what ended its chain: deleting the claim shape (c3) and pointing at the rule's existing home (c4).
  Both are already rules, `harden`'s *Don't rewrite prose faster than you verify it* ("Once a second
  attempt at a claim of some kind has been refuted … stop making a claim of that kind") and *Don't stop
  correcting a claim at the site you noticed it* ("correct that home and point the others at it").
  The record says they were applied "about three cycles" late. That is the rule-exists-applied-late
  shape :4210-4214 killed as prose, so it does not meet the REOPEN ON. Unchanged.
- **Prose the change made false, found by a fresh agent: 16 records** (+#443:14, the author's
  corrected "stubbed" javadoc; +#459:12, "Every consumer … is invoked"; +#527:8-11, four false claims
  in javadoc, ADR and README). None reached a merged head. REOPEN unchanged.

**Parked, 1 record each.**
- A `run_in_background: false` spawn still answered "Async agent launched" (#527:32, attended, cost
  ~0; waits recorded as yields). This contradicts the measurement behind the P1 applied at
  :3922-3935 (#451 in-turn on every `False` call). The prescribed fallback (a bounded foreground wait)
  covers it. **REOPEN ON:** a second post-0.30.0 record, or one that cost a turn or a Stop refusal.
- The Stop gate read the shared checkout's entry after the shell cwd reset, needing `EnterWorktree`
  (#527:31, attended, minutes). **REOPEN ON:** a second record, or a pool-launched one.
- A set-aside/restore loop not word-split by `zsh` let `git show origin/main:f > f` overwrite
  uncommitted edits (#443:20, ~2 tool calls, recovered by re-running the edit script). A sibling of
  the applied mutation-loop zsh rule (:1068-1070) and of the orchestrator-discard park (:4334). **REOPEN
  ON:** a second record, or one where work was lost.

**Observed, no remedy owed.**
- pr-harden's base-moved check done as written (#527:54): `main` gained #536, overlap none, not BEHIND,
  so no merge, push or round.

## 2026-09-25 (window: 2 run records — #514/PR524's second `pr-harden` loop, appended under the record the fifth window counted, and #515/PR540, converged) — 1 proposed, 0 applied, 1 killed (both options); one refutation round; linter 10 files, 0 findings; **no skill file changed**

Step 6's pre-read comparison: `origin/main` and this store agreed on every file except `…-515.md`
(local-only) and `…-514.md` (the PR524 second-loop record appended at its line 42). Proposal and the
gate's reply: `proposals/2026-09-25-window-514PR524-515.md`.

**KILLED · P1, pr-harden: a declined BLOCKING finding ends the run, made mechanical.** Drafted as bar
(b) on #514-2:57/:65, where r1-3 was declined in round 1 and five more rounds ran. (a) was prose at
:436, and (b) had `gate-state pr-set` refuse a later round after a blocking decline. The record's
"Nothing in the skill says whether to continue" is false against `pr-harden`:889-898 and :1258. This
is a rule that exists and was skipped, killed as prose on :1885 and :4211-4214. The record never cites
:436. (b) is harmful: `pr-set` rewrites `override` on every call (`gate-state`:388-389), and
`override: true` allows any stop (`pr-harden-gate.sh`:99-100). So it forces either dropping r4-1 and
r5-1 unfixed or disarming the Stop gate for every later round. The bar is not met either. Round 2
reviewed round 1's fixes with 0 blocking, and r4-1 and r5-1 were real blockers fixed with the verifier
reporting works. **Parked, 1 record:** continuing past a declined blocker. **REOPEN ON:** a record where
the rounds after the decline delivered no fix, or where a declined-blocker run exited as converged or
ready. The open question it leaves: whether Termination should let a run review its own fixes after a
blocking decline. Untested. The run's override line (transcript :453) named only the round cap, not
r1-3.

**Parked, 1 record each.**
- **`closingIssuesReferences` read green right after the edit, red later** (#515:21, 1 round). The
  check `resolve-ticket`:520 prescribes ran 5s after `gh pr edit` and returned `[515]` (transcript
  `1d4126e7-…` :1109, 01:18:44Z). The r2 reviewer's GraphQL read 11 minutes later returned `[513, 515]`
  (:1113) from a body whose only #513 keyword was "does not close #513". Cause not established. PR540
  merged with `[515]`, and #513 is open (both read 2026-09-25). **REOPEN ON:** a second record of the
  field changing after a green post-edit read. Candidate guard, cause-independent: re-read the field at
  FINISH.
- **A fixer ended its turn waiting on a build under pr-harden 0.32.0, its brief carrying the in-turn
  rule** (#514-2:66, one SendMessage resume). The orchestrator then had no in-turn wait on the resumed
  agent except polling for `mvn`. Sibling of the #527 `run_in_background: false` park. **REOPEN ON:** a
  second post-0.32.0 record, or one that cost a round or a Stop refusal.

**Parked counts advanced.**
- **pr-harden rounds whose blocking finding the previous round's fix introduced: 2 records** (+#514-2:59,
  r2-2's non-blocking widening → r3-1). The draft's r5 → r6 does not count: r5-1 was pre-existing
  (transcript :375) and r6-1 is its incomplete fix, the exclusion :4223-4224 applied to #505. The
  second loop is a separate run with separate round events, so the REOPEN ON's first limb is met.
  Nothing is proposable, though. r3-1 is the cost `pr-harden`:123-127 already documents from #465 for
  rounds 1-3's full-round rule, and neither record names what the fixer could have done differently.
  **REOPEN ON:** such a proposal.
- **Phase 2 escalating repeatedly on the run's own fixes: 16 records** (+#515:26, two escalations).
  REOPEN unchanged.
- **harden Phase 2 run as 1-2 agents over the four lenses: 4 records** (+#515:26, one agent, deviation
  stated). The REOPEN ON (:3774) is a merged-lens pass missing what a later fresh agent found in that
  lens. Not established here: r3's blocker (the duplicate-therapy finding bypassing `EndedOrders.stamp`)
  was not attributed to a lens. Unchanged.
- **Prose the change made false, found by a fresh agent: 17 records** (+#515:14, "five docs said
  none"). REOPEN unchanged.
- **chartsearchai's nested `CLAUDE.md` byte budget: 7 records** (+#515:10/:32, bullet dropped, one
  build). REOPEN unchanged.
- **main moving under a run: 5 records** (+#514-2:48: Decision 117 → 119 in 6 homes plus a line-broken
  citation, and #525's `LlmProvider` signature change reddened a stub, found only by the root build,
  0 rounds). REOPEN unchanged.

**Observed, no remedy owed.**
- `pr-harden`'s cap raise read its signal and stopped. Round 6 re-raised round 5's defect, and the run
  took the override (:904-913 as written, #514-2:43).
- #515's driver capture: the run began on a gate entry the aborted #515 run left (`phase=building`),
  the case the fifth window's `claim_slot` clear exists for. No cost recorded.

## 2026-09-25 (second window of the day: 1 run record — #514/PR524's THIRD `pr-harden` loop, `…09-25-…-514.md`:1, converged at round 5) — 3 applied after revision (P1 as a correction, P2(a), and a `ticket-pool` correction the gate found), 0 killed, P2(b) parked; two refutation rounds, the second over the staged diff; linter 10 files, 0 findings; `pool-test.py` 580 passed / 0 failed on the final tree

Step 6's pre-read comparison: `origin/main` (`2423c91`) and this store agreed on every file except
`…09-25-…-514.md` (local-only). One record since `LAST`, so this is a one-record window. #514-1
(`…09-24-…-514.md`:1) and #514-2 (:42), counted at :4100 and :4379, were read beside it for the adoption
question only. Proposals and both gates' replies: `proposals/2026-09-25-window-514PR524-loop3.md`.
Transcripts below: T2 = `31e8eac3-…` (#514-2), T3 = `7b912efd-…` (#514-3), times UTC.

**A record defect, corrected in the store.** #514-3's `transcript:` names T2, the SECOND loop's session.
The third loop ran in T3 (02:25Z to 07:30Z), whose :285 appends the record. The lookup was `ls
<dir>/*.jsonl -t | head -1` (T3:281-282). BSD `ls` read `-t` as a file operand and listed
alphabetically, so the earlier uuid came first. The note is appended to the record, per :3679-3681's #409
precedent.

- **P1 APPLIED as a correction (skill-retro 0.2.11): Step 1 counts the records `REJECTED.md` does not
  already name.** The "header's PR" key (added at :4116-4119, measured on a different-PR append) drops a
  later run on the same PR. :4379 counted #514-2 despite it, and this window's only record is the third
  loop on PR524. Round 1 killed the bar-(c) claim: :41 governs reading and :34-35 governs counting, so
  this is a gap (:330-333, :1103, :1972-1975). It also killed the drafted replacement, "name each by its
  file and header line rather than its PR". That would re-count #514-1, which :4100 names only as
  "#514/PR524", and replacement text needs a calibrated measurement (:3646-3648). What shipped is a
  deletion (:2454-2455), plus round 2's "`REJECTED.md`" in place of an undefined "the ledger", a word
  `ticket-pool` uses for `ledger.json`. Net 0. **So this block names its record as the THIRD loop.**
- **P2(a) APPLIED after revision (pr-harden 0.33.0): a PR worked again in a worktree `pool-run` has just
  made counts its earlier rounds toward round 4.** Bar (a): two records on ONE PR, #514-2 and #514-3,
  separate runs per :4416-4417.
  - The mechanism: Step 0 carries a same-PR entry's `round` forward (:71-73), but `pool-run` clears the
    entry when it makes a worktree (`claim_slot` :1146, the driven path :3243). Both later loops were
    launched by an operator chain after `pool-run --claim 514` (`d2089475…`:1701, :1761, :2106). Both
    read `null` (T2:22-23, T3:22-23) and wrote round 1 (T2:32, T3:32).
  - #514-2 then ran full rounds 1 and 2 (T2:134, :195) as the PR's rounds 5 and 6. Round 2's findings
    were all non-blocking (T2:196), and they went to a fixer under step 3's exception (T2:202-208). One
    of them, r2-2, introduced blocking r3-1 (#514-2:51, :59).
  - #514-3 stated the gap (#514-3:24) and ran blocking-only from round 1 by judgment.
  - This answers :4414-4420's REOPEN ON ("such a proposal"). What the run could have done differently
    is count the PR's rounds.
  - Round 1's revisions:
    - The trigger ("a PR the pool hands back", "routed here by `ticket-pool`") was false. The driver
      skips an open-PR ticket as `has-open-pr` (`pool-run`:2117-2125), and only `--claim` prints
      `/pr-harden` (:4055).
    - :112-113's "Otherwise rounds 1 to 3 are full rounds" and :96's diagram would have stood as
      universals the rule falsifies (:3821-3827). Both now say "the PR's".
  - Round 2's revisions:
    - The counter-case, per :3649-3651. Counted, round 2 was blocking-only, so r2-1 and r2-3 become
      notes. They were real holes, implemented in part (T2:208). On round 2's count of 0 (T2:203), the
      run ends before rounds 4 and 5 found r4-1 and r5-1 (T2:314, :369), false reports already in round
      2's head (:4392-4393, :4415-4416).
    - The universals narrowed to "when it makes a worktree".
    - Step 0's next paragraph wrote `round: 1` even over an adopted entry; it now keeps that entry's
      `round`.
  - The cap stays this run's budget (`pr-harden`:911-913). No gate reads `round` against a cap
    (`pr-harden-gate.sh`:293).
  - **It widens the round-4 rule, which :3687-3691 records as owing its own Step 5.** That REOPEN ON is
    not met, since #514-2 offers counterfactuals only.
  - Net +11: a Step 0 bullet on the only path where its carry-forward could fail.
- **APPLIED as a correction, found by the gate (ticket-pool 0.24.3): a draft PR is skipped as
  `has-open-pr`, not "handed to `pr-harden`".** Bar (c): :51 against :46, :394 and the driver. Round 2
  drove the real `consider` (`pool-run`:2117-2123). P2's draft trigger rested on this sentence. Round 2
  also cut round 1's suggested `--claim` clause. `cmd_claim` calls `pr_for_ticket` without `since` (:4019)
  and reads no ledger, so a PR on a numberless branch with `Refs` only gets `/resolve-ticket` (:4057).
  Net 0.

**Parked by the gate · P2(b), the records' `## Declined` sections as the declined ledger: 1 record, 0
cost.** #514-1 declined nothing (#514-1:30), so #514-2 had nothing to carry. T3's fifth carried item came
from the PR body (T3:33), not from a `## Declined` section. #514-2's record omits the round-5 note it
came from (T2:369, :437). So the description, which FINISH makes the home of everything unimplemented
(`pr-harden`:715-717), is the fuller source. **REOPEN ON:** a run adopting an entry-less PR that
re-raises, or is re-given, an earlier decline or note at a round's cost; or a second hand rebuild. If
readmitted, source it from the PR description, cross-checked against the records.

**Parked by the gate, no edit.** These are the widened rule's per-run neighbours: step 3's exception, "at
most once per run" (pr-harden:302); "every round after a clean full round" (:121); and "From round 2 on
… re-derive" (:256). Also the new bullet's sources missing or disagreeing: a driver capture's `rounds:
unknown` (`…-349-driver.md`:3), and T3:33's description, which names only the second loop's six rounds.
Only three or more earlier rounds change behaviour, so the larger count is the safe read. **REOPEN ON:**
a record where either cost a round.

**Parked, 1 record each.**
- **An adopted PR's round 1 ran before the ticket's newer comments were read** (#514-3).
  - The owner's decision on #514 (2026-09-24T23:58:35Z) predates the loop (02:25Z). The orchestrator
    first read it at T3:96, after round 1 (T3:90, :101).
  - It briefed declined item 1 ("PR says Refs, not Closes", T3:87) as settled, although the decision's
    point 5 said `Fixes #514`.
  - r1-1 was #514-2's open r6-1, which round 1 would have raised anyway.
  - The sixth window's #512 park (:4175) is a different mechanism with a different home.
  - **REOPEN ON:** a second record where an adopted PR's round is spent on a head that a ticket comment
    had already decided, or where a carried decline that a comment had reversed suppressed a finding.
- **A `transcript:` line naming another real session** (#514-3, corrected above).
  - Measured over the store by `artifacts/2026-09-25-transcript-check.py`. It tests whether a record's
    transcript MENTIONS the record's own file. The gate re-ran a test for an actual write; it also flags
    #377 and #471, both real writes by hand.
  - Of 124 headers carrying a transcript line, #514-3's is the only wrong uuid.
  - 11 headers (10 records, #527 twice) name the right uuid under the wrong project folder; #409's was
    corrected at :3679-3681. 4 driver captures carry a literal `<cwd-slug>` (`pool-run`:3179, beside
    `project_dir_name` at :1208), the latest being 09-24's `…-515-driver.md`. 3 headers (#337 and #338
    of 09-04) name a uuid found nowhere.
  - Each of those fails loudly. #514-3's alone resolves to a plausible wrong run.
  - **REOPEN ON:** a second wrong-uuid record, or a retro or gate conclusion drawn from one.

**Parked counts advanced.**
- **main moving under a run: 6 records** (+#514-3:10: #540 with 8 conflicting files and an ADR 119
  collision, merged by a fresh agent before round 1, 0 rounds). Two of the six are before-round-1 merges
  on an adopted PR (#514-2:48, #514-3:10). #514-3:25's "no phase for merging before round 1" is half
  right: Step 1's base check (:193-230) compares against "the one the previous round saw", so round 1
  has nothing to compare against. REOPEN unchanged (:4029).
- Checked, not advanced: the fix-introduced blocker stays at 2, since T3's reviewer reports attribute no
  blocker to a previous fix. Prose made false stays at 17, since the r5 note (#514-3:21) is an omission
  that the PR body names (T3:273).

**Observed, no remedy owed.**
- The cap was raised on #308's signal (#514-3:9, 4→6 after four different defects) and converged at round
  5. That is `pr-harden`:904-913 as written, the counterpart of #514-2's raise-then-override (:4436).
- The owner narrowing what blocks ("only a false report blocks") ended a non-convergence that the
  did-not-converge → draft path had handed back (#514-3:7).
- Both of this pass's gate spawns passed `run_in_background: false` and answered "Async agent launched"
  in this attended session. Every T2 and T3 spawn returned in-turn: each file's only occurrence of the
  string is the skill text at its line 4. That is #527:32's attended shape. This session is not a run
  record.

## 2026-09-25 (third pass of the day, owner-directed: run records name their transcript by a real path — no new run record) — 2 applied after revision (P3 in pr-harden and resolve-ticket, P4 in `pool-run` with a `pool-test.py` case), 0 killed; one refutation round over the staged diff, whose one precondition (a headless check) was run and passed; linter 10 files, 0 findings; `pool-test.py` 582 passed / 0 failed

The owner took two items from the second window's list: the record sections' `<cwd-slug>` and the driver
capture's placeholder. Proposals and the gate's reply: `proposals/2026-09-25-owner-directed-transcript-paths.md`.

- **P3 APPLIED after revision (pr-harden 0.33.1, resolve-ticket 0.21.2): the uuid is
  `$CLAUDE_CODE_SESSION_ID`, and the transcript is the one file `ls ~/.claude/projects/*/"$CLAUDE_CODE_SESSION_ID".jsonl`
  prints.** Bar (a), both halves.
  - The folder half: 11 headers across 10 records name the right uuid under the wrong folder. All ten
    writers were interactive: #266, #234, #297, #238, #229, #340, #355, #357 and #409 named the main
    checkout from a pool worktree, and #527 named the worktree it had moved into.
  - The uuid half: 2 records, both headless and both from a directory listing: #514-3 (T3:281-282) and
    #505/PR529 (`2463cfcc`:1129-1138).
  - The draft said "take both from the scratchpad path", and the gate blocked it twice.
    - A headless `claude -p` run has no scratchpad: 0 of 46 headless temp roots have one, and 0 of
      688 `sdk-cli` transcripts mention one. So the draft could not have prevented either wrong uuid it
      cited, and it forbade the cwd that headless runs had used correctly (27 writes, 0 wrong folders).
    - A session resumed from another directory keeps its transcript's folder but moves its scratchpad:
      #305, resumed from `~`, has its scratchpad there 193 times. The draft reproduces #305's own wrong
      L2166 header.
    - The draft's "T3's scratchpad (36 mentions)" was false: they were `tasks/` paths, and T3's temp
      root has only `tasks/`.
  - Shipped: the gate's Replacement A, verbatim, on the condition it set. Run here: a headless session
    with the driver's flags (`pool-run`:2668-2673) and a chosen `--session-id` printed that id from
    `$CLAUDE_CODE_SESSION_ID`, and the glob printed exactly its transcript. This interactive session's
    shell has `db22ea60-…`, which resolves the same way.
  - Net +4 lines per skill, one clause per measured failure. It replaces the uuid sentence in place.
- **P4 APPLIED (`pool-run`, `pool-test.py`): a driver capture's `transcript:` is
  `~/.claude/projects/{project_dir_name(worktree)}/{session}.jsonl`.** Bar (a), all 4 captures carried
  the literal `<cwd-slug>` (`pool-run`:3179), while the driver computes the folder for `silence_since`
  (:1208, :1243).
  - `capture_record` takes a keyword-only `worktree`, and its one caller passes `wt`, which is the launch
    cwd on the fresh and the resumed path.
  - The gate measured the folder rule on real transcripts: `project_dir_name` of each transcript's first
    `cwd` reproduces its folder for 1170 of 1170.
  - The test was written first, in `test_parallel_run`, over the real `run_wave`. It is red on the
    pre-fix driver (580 / 2), green after (582 / 0), and red again on a wrong folder and on a wrong
    session id (580 / 2 each).
  - The gate's two wording fixes were applied. The check pins "the folder `silence_since` reads", not
    "the folder claude uses". The comment's "the only pointer" is gone.
  - The gate also noted `pool-run`:98 pointing at a `session_transcript` that does not exist; it is now
    `project_dir_name`.

**Corrections to this ledger.**
- :4532 "#514-3's is the only wrong uuid" is false. #505/PR529's header names `e3da4188`, while its
  writer was `2463cfcc`. The mention-based detector passed it because `e3da4188` wrote that file's first
  record. A note is appended to `…-505.md`, per the #409 precedent.
- :4538's REOPEN ON ("a second wrong-uuid record") is therefore met, and P3's uuid half closes that park.
- :4535-4536's "#337 and #338 … name a uuid found nowhere" is false. Their session directories exist
  (`subagents/`, `tool-results/`); only the parent `.jsonl` files are gone, so those headers were right.

**Observed, no remedy owed.** Resumed sessions move their scratchpad (#305): one session in the store,
and the filesystem check found one. The uuid glob resolves it correctly.

## 2026-09-25 (fourth pass of the day, owner-directed: items 3-5 of the third pass's list — no new run record) — 3 applied (the owner's wait rule after revision; a 25-deletion pr-harden prune; two gate comments and one `pr-set` correction), 2 parked by bars registered before measuring (a stale-prose lister, a size-budget pre-check), 0 killed; one refutation round over the staged diff; linter 10 files, 0 findings; `gate-test.sh` pr-harden 36/0 and harden 47/0 on both copies

Proposals, measurements and the gate's reply: `proposals/2026-09-25-owner-directed-prose-prune-wait.md`.
The prune's candidate table: `artifacts/2026-09-25-pr-harden-prune-candidates.json`. The stale-prose
lister, its ground truth and the harness: `artifacts/2026-09-25-false-prose-ground-truth/`.

- **APPLIED after revision · the owner's `~/.claude/CLAUDE.md`: *Wait on a condition* gets a pipeline
  exception.** Bar (a): the Stop gate refused a mid-run yield on a background build or a `Monitor` on
  #479, #489, #498 and #505, and on #429 and #273 after pr-harden 0.30.0. It had been reported to the
  owner unedited at :3951 and :4278; the owner directed it now.
  - The gate killed the draft's second half, "an unattended `claude -p` run ends with its turn".
    #310's single `-p` process has 8 `system init` records, each re-init following a notification after
    a turn end, and its `.err` reads "Background tasks still running after 600s; terminating". Both
    re-measured here.
  - Shipped: the gate's text, which keeps the gate half and points at `pr-harden`:1071 (*this session
    must not busy-wait either*).
- **APPLIED · pr-harden 0.33.2: a prune, 1315 → 1250 lines.** Drafted by a fresh agent, 25 deletions
  with verbatim surviving-home quotes (its checker calibrated on a one-word-changed and a one-line-off
  quote, 29 passages rejected with reasons). Applied by script, with every in-file surviving quote
  re-checked afterwards.
  - The gate verified every `preserved` quote in its target, found no sibling pointing at a deleted
    passage by name, and applied all 25.
  - Five of the deletions removed claims already false: the fail-open universal "it cannot wedge a
    session", "nothing has ever compared them", "skips every remaining ticket in the pool", "since no
    session can advance a run whose writer is gone", and the Anti-patterns bullet P16's "Only the
    declined ledger crosses rounds".
  - Eleven are Anti-patterns bullets restating body rules, three of which had drifted from the body.
    This ledger holds no ruling on Anti-patterns sections.
  - Measured rules moved nowhere; their measurements survive at the quoted homes. The one pointer
    names `resolve-ticket`'s *Step 5 — Test first, then the fix*.
- **APPLIED · two gate comments, and `pr-set`'s drop list.** "Nobody here can advance its run", which
  both gates' headers retract, is deleted from `pr-harden-gate.sh`:218 and `harden-cycle-gate.sh`:187,
  in all copies (harden 0.42.1). pr-harden:82-83 now lists `verified_shas` among what `pr-set` drops
  (`gate-state`:400-409), stale since `ea574f9`.
- **Re-pointed, per the gate:** :3695's park ("A confirming reviewer briefed that zero findings is the
  expected outcome") named *Don't brief the reviewer with what was fixed* as its nearest rule. That
  bullet is deleted; the rule is at pr-harden:251-255, the declined-ledger bullet: "do **not**
  reassure it that any area is closed".

**PARKED by its own bar · a stale-prose lister (item 4a): 31% recall against 50%.**
- The bar and the metric were fixed before anything was measured: recall ≥ 50% and median output ≤ 60
  lines, over the park's reconstructable in-tree instances.
- The lister was built test first. Its list B was narrowed to a change's SUBJECTS on output size
  (#524: 1119 → 138) before any ground-truth run.
- The ground truth came from a fresh agent: 102 instances, 59 of them the park's own (`source:
  record`), over 14 PRs. 46 candidates were unreconstructable, 23 because they had been squashed into
  the change's first commit.
- **Primary:** 18/59 caught. That is ADDED 18/35 (A, universals in added prose) and OLD 0/24 (B, unchanged
  prose naming a subject). Median output was 49 lines.
- **Calibration:** 0.06% of prose lines are listed, and the lines ±40 from each false line are listed
  2 times in 58.
- **Secondary:** all 102 instances, 32%; excluding borderline and already-false instances, 42%.
- **Exploratory:** at ±2 lines, 23/59 (ADDED 22/35); the broad B, every identifier on a changed line,
  catches OLD 0/24 at a median of 87 lines.
- So the park "Prose the change made false" (17 records) has now had its mechanical lever tried.
  Universals catch about half of what a change ADDED. What a change made false in OLD prose is not
  reachable by identifier mention, which is `harden`:497's *names the claim's key nowhere*, measured.
- **REOPEN ON:** a universals-only lister under a fresh bar, registered before it is measured on new
  records' ADDED instances.

**PARKED by its own bar · a size-budget pre-check (item 4b): the expected saving is negative.**
- The guard alone took 39 s cold and 20 s warm, against ~105 s for a root build. 80 of 134 PRs merged
  from 08-20 to 09-25 touched a `CLAUDE.md`.
- A pre-check on each would cost ≥ 27-52 min, while the park's 7 records × 1-2 builds are ≤ 12-25 min.
- **REOPEN ON:** a guard that runs without compiling the module, or an overflow that cost a round.

**Recorded for a later pass: the stated mechanism of the unattended-yield rule is wrong in thirteen
homes.**
- "`claude -p` exits when its turn ends, so nothing re-invokes it / the yield IS the death" appears in:
  - `pr-harden`:973-974 and `resolve-ticket`:273;
  - `pr-harden-gate.sh`:42-43, :229, :277, :285 and `harden-cycle-gate.sh`:36-37, :196, :238, :245,
    two copies each;
  - `ticket-pool`:390 and `pool-run`:3328, :3397.
- #310's own log refutes it. A `-p` run waits on background work up to a ceiling, 600 s in #310, and
  is re-invoked. It dies when the work outlasts that ceiling, as a subagent often does and a ~105 s
  build does not.
- The gates' blocking behaviour stays right; only the reason they state is wrong.
- **REOPEN ON:** a pass that measures the current harness's ceiling (`CLAUDE_CODE_PRINT_BG_WAIT_CEILING_MS`)
  and then corrects every home.

**Observed, no remedy owed.**
- The owner's "a foreground `sleep` is refused" is not universal: the gate ran `sleep 3`, `sleep 12`
  and until-loops. It is the owner's sentence, so it is noted, not edited.
- Three of this session's five read-only agents wrote something, each disclosed and each scratch: two
  helper scripts, a candidates file and verifier, and one `FETCH_HEAD`.

## 2026-09-25 (fifth pass of the day, owner-directed: what a headless run does with background work, measured — no new run record) — 1 applied after revision (the stated mechanism, corrected in its homes), 0 killed; one refutation round over the staged diff; linter 10 files, 0 findings; `gate-test.sh` pr-harden 36/0 and harden 47/0 on every copy; `pool-test.py` 582/0

This closes the fourth pass's REOPEN ON ("a pass that measures the current harness's ceiling and then
corrects every home"). Proposal and the gate's reply:
`proposals/2026-09-25-owner-directed-headless-wait-mechanism.md`. Measurement, with its streams
redacted of the account's connector list: `artifacts/2026-09-25-headless-background-wait/`.

**The measurement.** Seven headless sessions ran on Claude Code 2.1.282 with the driver's flags.
- A `run_in_background` Bash command was killed ~5 s after the turn ended, when nothing else was
  outstanding. The run exited without being re-invoked; true of 60 s and 900 s tasks, and with
  `CLAUDE_CODE_PRINT_BG_WAIT_CEILING_MS=0`.
- A background agent was waited for. It re-invoked the run when it finished inside 600 s (10 s and
  180 s agents), and was stopped at 600 s otherwise ("Background tasks still running after 600s;
  terminating").
- With the variable at 0, a 720 s agent was waited for, and it re-invoked the run.
- #310 (2.1.246) matches: one process, 7 re-invocations, 3 agents stopped at the ceiling.

**APPLIED after revision · pr-harden 0.33.3, resolve-ticket 0.21.3, ticket-pool 0.24.4, harden 0.42.2,
both gates, `pool-run`: the mechanism behind the unattended-yield rule, corrected.**
- The one full statement of it is pr-harden's **State** paragraph. Every other home says only that the
  process stops an agent still running 600 s after the turn ends, and then exits.
- The gate killed the first draft's paragraph for contradicting its own bold lead ("holds only for an
  ATTENDED session") and for an over-broad Bash claim. It also found that nothing linked the
  short-agent case to the "never" rule. The shipped text gives the link: a yield cannot tell which case
  it is in, since #310 was re-invoked after seven yields and died on the eighth.
- Both gates' unattended comment still ended on "it is how the run dies"; it now says "whenever the
  agent outlasts that".
- The rule, the gates' decisions and "the death this marker exists to prevent" are unchanged.
- The harden gate's header had its attended-session block inserted mid-sentence since `207a8e3`. It is
  now whole.
- The first staging broke both gates, 13 and 30 failures: an apostrophe inside the single-quoted `jq`
  program. Their own suites caught it before install.

**Corrections to the store.** The 2026-08-26 record gets an appended note, per the #409 precedent.
#297's cause is stated as "consistent with the same cause", because its stream does not survive.

**Not proposed, the owner's decision:** set `CLAUDE_CODE_PRINT_BG_WAIT_CEILING_MS=0` on driven sessions.
It makes an agent yield survivable and not a Bash yield. The gates already refuse the agent yield, so
it would be a second barrier rather than a fix.

**Recorded for a later pass:** pr-harden:964 ("without it an unattended run cannot proceed at all") and
`pr-harden-gate.sh`:33 contradict the unattended block after them, since `207a8e3`.
**REOPEN ON:** a pass over the attended/unattended framing of `awaiting` as a whole.

## 2026-09-26 (window: 4 records over 2 tickets — #402's resumed resolve-ticket run, `…09-26-…-402.md`, aborted-as-draft; #542/PR543's pr-harden run, converged in 2 rounds; and the two 2026-09-25 driver captures for #542 and #402, found appended to `2026-08-26-unattended-yield-kills-the-run.md` and relocated below) — 2 applied after revision, 0 killed, 2 parked; two refutation rounds, the second over the staged diff; linter 10 files, 0 findings; `pool-test.py` 587 passed / 0 failed

Proposals and the gate's reply: `proposals/2026-09-26-window-402-542.md`.

**APPLIED after revision · pr-harden 0.34.0: under `ticket-pool`, `[Request interrupted by user for
tool use]` on an agent's result is not the operator.**
- Evidence: #542's stream `20260925T152447Z-…-542.jsonl`. The round-1 reviewer's `task_updated` reads
  `Agent stalled: no progress for 600s (stream watchdog did not recover)`, the parent's tool result
  reads the interrupted text, and the orchestrator stopped on "you interrupted the round-1 reviewer",
  took the override and wrote no record. Another session re-ran the loop on 2026-09-26 (2 rounds).
- Bar, stated as the gate corrected it: one event; the whole loop was forfeited in-run, and the rerun
  in another session cost 2 rounds. The stop's reason was neither of pr-harden's two early ends.
- Measured over `~/.claude/pipeline/logs/*.jsonl`: that exact string occurs in one top-level tool
  result, this one. The broader `[Request interrupted by user]` also appears where an orchestrator's own
  `TaskStop` killed subagents (#346's stream), which is why the text claims no cause.
- Gate's blocking objections, both fixed: (1) the first draft said "in an unattended run" and a
  session had no way to test that. The shipped text names `CLAUDE_PIPELINE_SESSION=1`, which
  `pool-run`'s `Session` stamps into the sessions it starts headless (since e09a039, 2026-09-14, so
  #542 had it). `--work` sessions do not get it. (2) The draft named the harness wording as a
  watchdog's text; the shipped text says the cause was not established.
- An attempt to back the guard with "a real Esc ends the turn" found no calibration case in any
  transcript, so that claim was not made.
- Prunes nothing. Net +7 lines, because the existing contract was bypassed by a misclassification it
  does not name.

**APPLIED after revision · `pool-run` (no skill version; ticket-pool's prose is unchanged): the
`record_written` fallback takes a file only if the run's own stream names it.**
- Evidence: ledger `#542.record` was `2026-08-26-unattended-yield-kills-the-run.md`, and the ledger
  backup before #402's resume shows #402 pointing there too. Both runs started 2026-09-25T15:24:47Z;
  commit 47b8431 edited that note at 15:47Z. Neither stream names it. The gate reproduced the
  misattribution by calling the real `record_written`.
- Bar: data corruption in driver code, measured. The gate refused the first draft's "self-
  contradiction" label because the docstring names siblings as the threat.
- Gate's blocking objections, both fixed: (1) the draft's "created during the run" restriction would
  drop a legitimate PRE-EXISTING record that does not end in the ticket's number (`…-PR543.md`, or a
  same-day second attempt appending to one). Dropping it also blinds `record_says_aborted`, so an
  aborted run gets retried. (2) The draft's "either numbered or someone else's edit" was false for
  those names.
- The shipped test is the gate's stronger shape: one of the run's own TOOL CALLS must name the file.
  With no stream to read, only a created file counts. The second gate narrowed "the stream" to tool
  calls, because #444's stream ran `ls -t ~/.claude/skill-lessons` and its output named an unnumbered
  record another run wrote. Calibrated over the ledger: 30 of 30 run-written records are named in a
  tool call of their own stream. The two misses are a driver-written `-driver.md`, which never reaches
  the fallback, and this misattribution. Both 09-25 streams do not name the note, and #444's `ls` no
  longer counts.
- Five `pool-test.py` cases. A mutation removing the filter reddens four of them. A mutation that reads
  the whole stream instead of tool calls reddens the `ls`-output case.
- Store repair: the two captures moved verbatim into `2026-09-25-openmrs-module-chartsearchai-
  {542,402}-driver.md`. Their mtimes are set to when the driver took each capture, 2026-09-25 21:25
  and 2026-09-26 01:56 local. The first attempt's `touch -r` used a reference that `cp` had already
  restamped; the second gate caught it. The 08-26 note is restored to its committed
  content, and ledger `#542.record` is repointed (backup `ledger.json.bak-pre-542-record-repair-20260926`).

**PARKED · #402: a ticket's direction ("end as draft when a gate criterion cannot be fixed by
rendering") against resolve-ticket Step 9 / pr-harden FINISH and harden's required second Phase 2
after escalation.** A labelled override was taken. Count: 1. **REOPEN ON:** a second record where a
ticket's direction and a skill's terminus disagree.

**PARKED · session-level network loss.** #402 session abb941f5 died after 10 API retries on ENOTFOUND
(exit 1, 7h31m) and was resumed by the operator the next day. #542's session 138dde08 shows an 18-minute
heartbeat gap and "unknown" `api_retry` lines in the same window, but that was not verified to be the
same outage. Count: 1 verified. **REOPEN ON:** a second verified session death to network loss.

**Not a lesson:** the rest of #402's record is chartsearchai domain evidence (A/B arms, referent
wording), and so is PR543's r1/r2.

## 2026-09-28 (window: 3 run records — #463/PR545, converged 2 rounds / 2 cycles; #246/PR547, converged 3 rounds / 2 cycles, Refs; #528/PR546, converged 3 rounds / 4 cycles, verifier ran) — 1 proposed, 0 applied, 1 killed; one refutation round; linter 15 files, 0 findings; **no skill file changed**

Step 6's pre-read comparison: `origin/main` and this store agreed on every file except the three
records, which were local-only. Proposals and the gate's reply are in `proposals/2026-09-28-window-463-246-528.md`.

**KILLED · resolve-ticket: before the Step 8 push, check the ADR decision number against every open
PR's diff.**
- Why it was proposed: the park *main moving under a run* reached 7 (+#528:21). Its REOPEN ON ("one that
  cost a round", :4029) was met, because merging #547's Decision 121 cost #528 a blocking-only round
  and a second verifier run.
- Measured this pass: three pipeline PRs allocated Decision 121. They were PR544 (#402, draft, opened
  2026-09-26T19:47Z, **still open and still claiming 121**), PR546 (#528, written 09-27T19:02Z) and
  PR547 (#246, opened 19:57Z, merged 22:23Z). `for m in $(gh pr list --state open --json number --jq
  '.[].number'); do gh pr diff $m | grep -E '^\+## Decision [0-9]+'; done` was calibrated: merged
  PR547 gives `Decision 121`, and PR545, which has no entry, gives 0 lines.
- Killed on the gate's citation, re-read: #528 transcript :778/:788. The conflict was textual, with
  the TOC line after Decision 120 and the body at end of file being the same insertion points on both
  branches. The merge, the confirming round and the verifier run were owed whatever the number. The
  lever saves a renumber, which is the 0-cost shape.
- **REOPEN ON:** a lever that avoids two branches meeting at the ADR's shared insertion points. That is
  a target-repo layout property, like the nested-CLAUDE.md budget park. Or a record where a collision
  that git merged SILENTLY (no textual conflict) reached a merged head.

**Parked counts advanced.**
- **main moving under a run: 7 records** (+#528:21, one blocking-only round and one verifier run,
  which is FINISH's *costs one more round* as written). The cost-bearer is the shared insertion point,
  not the number (above).
- **Phase 2 escalating repeatedly on the run's own fixes: 19 records** (+#463:13, one; +#246:3/:16, one;
  +#528:14-15/:20, three, ending on the labelled override). No remedy named. REOPEN unchanged (:3998).
- **Prose the change made false, found by a fresh agent: 20 records** (+#463:15 stale code-table
  paragraph; +#528:15 a javadoc crediting #528 with #74's failure; +#246:18 "every refusal" true only
  on ABSTAIN cells). REOPEN unchanged.

**Observed, no remedy owed.**
- pr-harden 0.34.0's interrupted-agent rule fired as written (#246:21). The round-2 reviewer returned
  `[Request interrupted by user for tool use]` under `CLAUDE_PIPELINE_SESSION=1`, was treated as agent
  death, and a single retry succeeded. That is the second sighting of the string and the first under
  the rule.
- #528:9, an uncalibrated bytecode comparison (177/189 classes differed), was discarded by the run
  itself in favour of a verifier re-run. That is the CLAUDE.md calibration rule applied in-run.
- #528's refutation gate caught two plan omissions of prior measured art: #66's inline-date baseline,
  and the committed `temporal_probe_rc2.py`. #463's gate refuted the ticket's 3b premise by
  measurement. Both are the gate working, at 0 rounds.

**Not a lesson:** #246's A/B arms and greedy-decode noise-floor finding are chartsearchai domain
evidence.

## 2026-09-28 (second window of the day: 2 run records — #240/PR549, converged 3 rounds / 2 cycles; #455/PR550, converged 2 rounds / 3 cycles) — 0 proposed, 0 applied, 0 killed; no refutation round (nothing to refute); linter 17 files, 0 findings; **no skill file changed**

Step 6's pre-read comparison: `origin/main` and this store agreed on every file except `…-240.md` and
`…-455.md` (local-only) and a `.DS_Store` (not mirrored).

**Parked, new, 1 record.**
- **A run record written through an UNQUOTED heredoc executes the record's own backticks.** #240's
  record line 21 reads "mutate-and-restore with Your branch is up to date with 'origin/fix/240-…'." The
  transcript (`65754a09-…jsonl`, line 402) shows `cat >> …-240.md <<EOF`, left unquoted so that `$T`
  (the transcript path) would expand, around the text `` `git checkout --` ``. The shell RAN that
  substitution. With no path it only printed status; a record naming `` `git checkout -- <real
  path>` `` would have discarded work at the run's last step, silently. Measured over
  `~/.claude/projects/*/*.jsonl`: 67 record-writing heredocs, 62 quoted, and 1 unquoted body with a
  backtick or `$(`, which is this one. The detector was calibrated: it finds #240's known-bad call, and
  the 62 quoted ones are the known-good class. Cost 0. **REOPEN ON:** a second record, or one where a
  substitution changed a file or ran a command other than a status print.
- **`--run` placed after the subcommand, in an `&&` chain.** In #455:18, `clear-await --run X` exited
  non-zero and the chain skipped the commit that followed. Every skill example puts `--run` first
  (harden:265, resolve-ticket:367), and the refusal names the right order, so the text is not at
  fault. Cost: one re-issued command. **REOPEN ON:** a second record, or one where a skipped step went
  unnoticed.
- **A narrowly scoped Step 2 plan, with the refuter agreeing, widened by a round-1 reviewer** (#455:26,
  the fixer implemented a two-token tightening under the target repo's ADR). Cost: 1 round. **REOPEN
  ON:** a second record.

**Parked counts advanced.**
- **A mutation surviving because the only covering case sits in a degenerate state: 2 records**
  (:836, #266; +#240:17, a stub baseline equal to an arm value let a hardcoded `set_gp(GP,"false")`
  pass, 1 confirming round). The bar is met. But `harden`:52-53 ("check whether the stub standing in
  for it can even produce the counterexample") already states the rule, so this is the
  rule-exists-applied-late shape that :4210-4214 killed as prose. No proposal.
- **Phase 2 escalating repeatedly on the run's own fixes: 21 records** (+#240, one substantive P2
  escape; +#455, escalations over 3 cycles, which the run ended by deleting the enumeration claim shape
  and stating the helper's predicate. That is `harden`'s *Don't rewrite prose faster than you verify
  it*, applied). REOPEN unchanged.
- **Prose the change made false, found by a fresh agent: 22 records** (+#240:18, the usage text's "the
  defaults"; +#455:13, a residue citing issue #455 for a measurement the issue contradicts; +#455:9,
  two falsified enumeration claims). REOPEN unchanged.
- **`git checkout -- <path>` discarding the ORCHESTRATOR's own uncommitted work: increments only**
  (+#240:21, cycle-2 edits recovered from a /tmp copy). The record itself says commit-before-probe was
  not followed, so the reopen is still unmet.

**Observed, no remedy owed.**
- #240's refutation gate refuted two ticket premises by SQL and by reading the code, at 0 rounds. #455's
  Step 2 probe caught the ticket's quoted "measured FAIL" as green on the named sha, also at 0 rounds.
- Both runs skipped the verifier because there was no runtime surface. #240 substituted live GET-only
  harness runs on :8081.

**Not a lesson:** #240's cohort and GP-restore details and #455's `newSentence` skip predicate are
chartsearchai domain evidence.

## 2026-09-28 (third window of the day: 2 run records — #402/PR544, attended pr-harden 0.37.0, converged 4 rounds / 0 cycles; #462/PR551, resolve-ticket + pr-harden, converged 1 round / 5 harden cycles) — 1 proposed (4 parts), 3 parts applied revised, 1 part killed; one refutation round; linter 17 files, 0 findings; **pr-harden 0.38.0, resolve-ticket 0.24.0**

Step 6's pre-read comparison: `origin/main` (`7be82d8`) and this store agreed on every file except
`2026-09-28-…-402.md` and `…-462.md` (local-only). Each holds one record. The 2026-09-26 #402 record
was counted by an earlier window, and this is a new run on the same ticket.

**Applied (revised by the refuter): the Stop gate keys on the session's cwd.** Bar (a): two records,
#527:31 (2026-09-24, resolve-ticket Step 1) and #402:20 (pr-harden Step 0). Two sessions and two tickets,
so not one event counted twice. That meets the 2026-09-24 park's *REOPEN ON: a second record*. What
the refuter established from the transcripts: in NEITHER run did the gate fail open. #527
(`ebc78818…:86-107`) wrote its entry from a `cd`, read the hook, and moved before any Stop. #402
(`c3fd0664…:1343-1356`) anticipated it and moved first. #402:20's "the gate could not see it until
EnterWorktree" is therefore the record's inference, not an observation. The rule says so ("anticipated
from `KEY="$(pwd -P)"` and not yet observed"), per Step 3's *write the guard, not the diagnosis*. The
guard (start in the worktree, or enter it before the first write) is right whatever the harness's hook
cwd turns out to be. Narrowed on the refuter's citations: "refuses compound commands" became "a
compound command that runs git" (`:1372`), and `$PPID` refused on a `gate-state` line (`:1376-1377`) now
comes with the remedy `echo $PPID` alone, which ran (`:1385`). Also added: a pointer in resolve-ticket
Step 1, because #527 hit it there, ~480 lines before pr-harden's **State**. Evidence entry:
pr-harden/evidence.md *State*, "Added 0.38.0". Pruned: nothing. Budgets raised
(pr-harden 9403→9503, resolve-ticket 6749→6778) with the `why` in `raises`.

**Killed: prune (b), pr-harden's "`--owner $PPID` is this session's own claude process, which is how
both gates tell your entry from one a co-located session left in the same directory."** Offered as a
restatement of the `owner` paragraph earlier in **State**. Died: that paragraph says "the gate". This
sentence says "**both** gates", and `harden-cycle-gate.sh` also reads `owner`. The prune would drop that
fact. **REOPEN ON:** the earlier paragraph gaining "both gates", at which point the sentence is a
genuine duplicate.

**Parked, new, 1 record each.**
- **Effort inheritance is invisible to a retro** (#402:21-26). The session ran at `/effort max`, and
  all 10 agents inherited it: reviewers 34-41 min against 5-14 on #528, and 18-64M tokens per agent
  against 0.7-4.2M. The record format has no effort field. Cost: wall time and tokens, not rounds.
  **REOPEN ON:** a second record where effort explains an anomaly a retro would otherwise have
  attributed to the pipeline.
- **Target-repo context load** (#402:27). The 76k-char `reference/CLAUDE.md` accounts for 23% of Read
  content, and one 13,563-line file for 26%. This is a target-repo property, a sibling of the nested
  `CLAUDE.md` byte-budget park. **REOPEN ON:** a skill-side lever.
- **A renumbering sweep searched the phrase, not the number** (#402:11). "Decisions 72, 44, 110, 112,
  121" and a line-wrapped reference were missed, found in r1, at 0 rounds.

**Parked counts advanced.**
- **main moving under a run: 8 records** (+#402:6/:11, main merged and 121 → 123 before round 1, five
  missed references fixed in r1). No silent merge reached a merged head, so the :4817 REOPEN ON is
  unmet. Unchanged.
- **Phase 2 escalating repeatedly on the run's own fixes: 22 records** (+#462:21, four successive
  defeats of a source-text guard, 5 cycles for a ~150-line test change). It ended when the defeats were
  of kinds already named as residue, which is `harden`'s *name the residue* (harden:245, :399) applied.
  That is not a lever that stops the chain earlier. REOPEN unchanged.
- **Prose the change made false, found by a fresh agent: 24 records** (+#462:9/:10/:14, the guard
  javadoc and the stamp comment, each measured false at 1 cycle; +#402:17, stale wording, non-blocking
  and named in the PR). REOPEN unchanged.

**Observed, no remedy owed.**
- #462's refuter caught a call regex copied from the #512 sibling that could not match today's call,
  before any code was written, at 0 rounds.
- #462 skipped the verifier on a CALIBRATED bytecode comparison: `javap -c -p` identical between base
  and head, with a positive control that differs. #528:9's uncalibrated one was discarded. This is the
  CLAUDE.md calibration rule followed.
- #402's declined r1-6 (a tracking issue) matches the no-follow-up-issue rule. #402's r2 and r3
  measurements refuting the direction's formula are the review loop working.

**Not a lesson:** #402's substance-resolution legs and #462's readiness-stamp design are chartsearchai
domain evidence.

## 2026-09-29 (window: 5 run records — #467/PR558, converged 4 rounds / 3 cycles; #548/PR554, did-not-converge at the round-4 cap, draft, plus its driver capture; #552/PR557, converged 3 rounds / 3 cycles; #553/PR559, converged 5 rounds (cap raised) / 3 cycles; #555/PR556, converged 4 rounds / 3 cycles) — 0 proposed, 0 applied, 0 killed; no refutation round (nothing to refute); linter 17 files, 0 findings; **no skill file changed**

Step 6's pre-read comparison: `origin/main` (`b09c13d`) and this store agreed on every file except the
five `2026-09-29-…` records (local-only), one record each. `proposals/2026-09-2{7,8}-*.md` are newer
than `LAST` and byte-identical to `origin/main`.

**No lesson cleared the bar with a lever.** Two parks met their count long ago and gained records
here. Neither REOPEN ON condition is met, so the rest below is counts.

**Parked, new, 1 record each.**
- **A BLOCKING finding naming only the PR description, at the cap** (#548: r4's one blocker was
  `Fixes #548` → `Refs`, applied to the body, head unmoved, no round left; ended on override as draft).
  pr-harden step 3 routes a NON-blocking body-only finding to FINISH with no round owed ("they named
  only the PR description — no round is owed"). For a blocking one no text names a path, so it is a
  gap and not a contradiction. Cost: the outcome, not a round. **REOPEN ON:** a second record, or one
  where a body-only blocker before the cap spent a confirming round on an unmoved sha.
- **Plan-refuter passes contradicting each other** (#553: pass 1's obj1 and pass 2's obj2 were
  opposite; the run treated pass 2 as settling, and harden Phase 2 measured pass 1 right. Cost: 1
  escalation cycle). This is a sibling of the :4088 park, a re-run after a settling objection.
  **REOPEN ON:** a second record where a re-run's objection reverses a settled pass-1 objection.
- **Verifier repairing a stale omod on :8081 before every run** (#548, ×3, 0 rounds). The deploying
  party was not recorded. **REOPEN ON:** a run where the stale omod produced a wrong verdict.

**Parked counts advanced.**
- **Phase 2 escalating repeatedly on the run's own fixes: 27 records** (+#467, two escalations;
  +#548, one; +#552, two; +#553, two; +#555, Phase 2 three times for doc-only findings). Every record
  says each escalation found a real defect, so no proposal names what would have stopped the chain
  earlier without losing one (:3999-4000). Unchanged.
- **Prose the change made false, found by a fresh agent: 28 records** (+#548 r3, ADR said the opposite;
  +#552 harden P2, namedPartners javadoc/README/controller; +#553 harden P2b, README contract; +#555
  harden P2 ×2 cycles and r2 comparable() javadoc). #467's stale class javadocs were author-found and
  are not counted. None reached a merged head, and no mechanical lever is proposed (:4025-4026).
  Unchanged.
- **chartsearchai's nested `CLAUDE.md` byte budget: 10 records** (+#548, 6 bytes, raised in its own
  commit; +#552, caught by the plan refuter before code, 1 gate re-run; +#553, words trimmed to fit).
  #552 is the "budget check before the build" (:4002) happening unprompted at the refutation gate.
  It is not a skill-side lever, because no rule told it to. Unchanged.
- **harden Phase 2 run as 1-2 agents over the four lenses: 5 records** (+#553 passes 2b/2c, two agents,
  deviation stated; transcript :1254-1255, :1287-1288). The REOPEN ON (:3774) is unmet. The merged
  passes reviewed the incremental diffs. r1-r4's blockers were not attributed to a lens the merged
  pass covered, and the four-lens pass 1 (:1091-1094) also missed r1's. Unchanged.
- **`pr-harden`'s round cap raised past its default: 5 records** (+#553, 4 → 5, converged at 5). No
  count was advanced after :2523, so instances between may be uncounted.
- **Orphaned javadoc from an insertion** (killed P3', :2792-2804): +#548 (harden P2, cycle 1) and +#552
  (harden P2, "6th time"). Neither transcript runs harden's awk check before Phase 2 found the orphan.
  #548 runs it first at transcript :900, after the fix at :850. #552 sweeps at :521, after the fix at
  :480. Neither shipped one. The REOPEN ON (ran the check or read the neighbours and STILL shipped) is
  unmet. These are instances of the rule being skipped.

**Observed, no remedy owed.**
- `gate-state harden-set --only` rejected (#467, one retry). It is the second record after #272:15
  (:4339). The argparse message named the accepted subcommands. harden's `harden-set` examples
  (harden:285-291) carry no `--only`, so this is no skill-vs-gate contradiction.
- #555 left a co-tenant run's `pr-559-r*` refs alone. That is FINISH's "Delete the run's own"
  scoping (pr-harden:357) holding.
- #467 calibrated its race harness with a positive control after the first one read 0/3000 on the
  known-bad version. That is the CLAUDE.md calibration rule followed.
- #552 and #555 re-ran the plan refuter after a settling objection, and the re-run caught a real
  second blocker or a redesign. That is the re-run doing its job (:4089-4091, read the other way).

**Not a lesson:** #467's dash-vs-bash `: >` defect and the publisher race are target-repo domain.
Referent and wording findings in #548/#552/#553/#555 are chartsearchai domain evidence.

## 2026-10-02 (window: 4 records over 3 tickets — #560/PR561, pool-launched resolve-ticket and its appended pr-harden record, converged 2 rounds / 5 harden cycles; #562/PR563, attended, converged 3 rounds / 2 cycles; #564/PR565, attended, converged 2 rounds / 1 cycle) — 4 proposed, 3 applied (P2 and P3 after revision, P4 as a correction the gate found), 1 parked by the gate (P1); two refutation rounds, the second over the staged diff; linter 17 files, 0 findings; **pr-harden 0.38.1, resolve-ticket 0.24.1**

Step 6's pre-read comparison: `origin/main` (`5943ef9`) and this store agreed on all 284 shared files,
and on all 43 governance files. The three records were local-only. Proposals and both gate replies are
in `proposals/2026-10-02-window-560-562-564.md`.

**APPLIED after revision · pr-harden 0.38.1 *State* and resolve-ticket 0.24.1 Step 1: the isolation
guard refuses what it cannot verify, git or not.**
- Bar (a): #562:23 and #564:19. Two tickets and two sessions, both after 0.38.0 told an attended run to
  `EnterWorktree`.
- Measured over the five `EnterWorktree` windows in the store, in four sessions (#562, #564's session
  with two, #402, #527).
  Compound commands that ran git: 118 passed, 7 refused. Those that ran none: 305 passed, 25 refused.
  Scripts run by absolute path: 32, none refused. An unchained `$PPID` gate-state line was refused
  twice. All 37 refusals use the guard's own verifiability wording: 30 say "split", 5 "the plain
  command", and 2 "git with literal arguments".
- The gate's blocking objection, settled by re-measurement. In *State* alone the text could not have
  reached 19 of the two records' 23 refusals, because those came before Step 9 loaded `pr-harden`.
  #527 had 7 of 7 before. So resolve-ticket Step 1 now carries the same sentences, not only the 0.24.0
  pointer.
- Also applied from the gate: its wording ("do as its message says"), the script's git being its
  author's to keep in the worktree, and its fourth window (#527).
- Pruned: "a compound command that runs git" and "split the git commands".
- Round 2, over the staged diff, settled one blocking objection. The evidence entry had said why
  scripts pass ("the guard does not read inside it"), which the 32/0 figure cannot show, because none
  of the 32 scripts ran git. The entry now says only that. Round 2 also made three wording fixes: five
  windows, not four; both `eval` greps, not one; and "`EnterWorktree`'s" for resolve-ticket's
  antecedent.

**APPLIED · pr-harden 0.38.1 *State*: prune (b) of 2026-09-28, on its own REOPEN ON (:4934).**
- The `owner` paragraph now says each gate allows the stop on the pid in its own entry.
- The two gates' owner logic is the same code: `pr-harden-gate.sh`:208-222 and
  `harden-cycle-gate.sh`:177-191.
- So the "`--owner $PPID` … both gates" sentence was a restatement and is gone (−26 words).

**APPLIED as a correction · pr-harden 0.38.1 *6 — VERIFY*: "Inside a round, neither is recorded as a
blocking finding by the orchestrator".**
- Bar (c), found by the round-1 gate and by no record.
- :325 said "Neither is ever recorded as a blocking finding by the orchestrator". FINISH records "a
  `not-the-environment` finding on the merging head" as blocking.
- R3-07 added FINISH's rule and left :325 unscoped.

**PARKED by the gate · P1: `verifier.md`'s `classification` has no value for a clean run.**
- Died as a contradiction. FINISH and the gate key on a `not-the-environment` FINDING, not on the
  label.
  - #562's own orchestrator called its label a mislabel (`111381c1…` L2546), and #527's read one the
    same way (`ebc78818…` L3073).
  - A gap, as :4986-4987 ruled on a similar case.
- **The proposer's own measurement was wrong, and that is the part worth keeping.**
  - Its extractor looked for `verdict` only within 4000 characters of `classification`, and dropped
    the rest silently.
  - Calibrated on short synthetic reports only, it found 36 reports where there are 85.
- Re-measured with round 1's JSON-parsing `scan_cls2.py` (re-run, exit 0, stderr empty): 85 reports.
  - 41 carry `not-the-environment`: 40 of the 84 that say `works at runtime`, plus one that could not
    determine.
  - Of the 84 that say `works at runtime`, 43 list no repairs: 32 say `not-the-environment`, 10
    `repaired`, and 1 has a free-text label.
  - Of the 41, 36 were followed by a `verified-sha` write. In 14 of those the write sat in the same
    command as a phase move carrying the previous reviewer's count, as *State* says to leave it.
  - The other 5 show neither within 15 Bash calls. None was recorded as blocking.
- A revival must not define the label as any defect of the artifact. FINISH sends non-blocking
  verifier observations to the PR body, and #560's round-1 verifier set the label beside one
  (`5d25fc45…` L1283; 560.md:21). A new schema value is arguably a minor bump.
- **REOPEN ON:** an orchestrator recording a blocking finding, or running a round, because of a clean
  run's label; or a second record.

**Parked counts advanced.**
- **Phase 2 escalating repeatedly on the run's own fixes: 29 records** (+#560, four in a row; +#562,
  one). #560's chain ended at cycle 5 on `harden`:240-245's *a change in the KIND of question*, so this
  is the rule applied late. REOPEN unchanged.
- **Prose the change made false, found by a fresh agent: 30 records.**
  - +#562: four in harden P1 and P2, and r2's Decisions 118/119 "ships off", which cost 1 round.
  - +#564: gate r2's comment, and pr r1's javadoc, which cost 1 round.
  - None reached a merged head. REOPEN unchanged.
- **main moving under a run: 9 records** (+#560, ADR 126 → 127 before round 1, by pr-harden's base
  check as written). REOPEN unchanged.
- **chartsearchai's nested `CLAUDE.md` byte budget: 11 records** (+#560: a refuter's instruction-file
  bullet declined at 6 and 23 bytes of headroom). This is the first record where the cost is a rule
  never written there at all. Target-repo property. REOPEN unchanged.
- **Verifier repairing a stale omod on :8081: 2 records** (+#560). The operator asked for each verified
  build to stay deployed on :8081, which is why the next run finds one. REOPEN unchanged: no wrong
  verdict.
- **Two rigs sharing llama-server port 18085: 2 records** (+#562). The operator's :8081 rig returned
  500s until another session moved it to 18081. The cost was an outage, not a round, so the REOPEN ON
  at :3531 is unmet.
- **`closingIssuesReferences` changing after a post-edit read: 2 records.**
  - #515 went green, then red. +#564 went red, then green: `[]` from create to ready, under bodies that
    both opened `Fixes #564`, then `[564]` at merge, which closed #564.
  - The REOPEN ON at :4405-4406 names a green read. #564's read was red.
  - Its candidate guard, a re-read at FINISH, would have read `[]` again 19 s after the re-save. Cause
    not established.

**Parked, new, 1 record each.**
- **A pre-registered gate the control arm already failed** (#562: "exit 0" could not be met, because
  the OFF arm exits 3, and the refuter caught the relaxation). The CLAUDE.md calibration rule covers
  it. **REOPEN ON:** a second record.
- **A plan undercounting the pins a change reddens** (#562: a "two-line change" turned 13 classes red;
  #564: "two pins" turned nine red). That is two records at 0 rounds and one gate re-run, with no lever.
  **REOPEN ON:** one that cost a round.
- **`verified-sha` recorded off the orchestrator's own live check, with no verifier subagent spawned**
  (#564, `12e2feb4…` L1499-1523; the record says "author-run").
  - The check was of a pre-polish build, carried to the head by a calibrated `javap` comparison.
  - pr-harden's verifier is "a fresh subagent".
  - **REOPEN ON:** a second record, or one where the carried check was wrong.
- **A default flip moved existing tests off the path they pinned** (#562 r1, 1 round). **REOPEN ON:** a
  second record.

**Observed, no remedy owed.**
- **Transcript paths that moved after the record named them.**
  - #562, #564 and #402's 2026-09-28 record each name a worktree project folder. At write time the uuid
    glob printed exactly that folder (#562 L2575, #564 L1575).
  - The file now sits under the session's starting folder, and the worktree folder is gone.
  - The uuid glob resolves it, as with #305's scratchpad (:4611).
  - :4570's "#527 named the worktree it had moved into" fits the same move, and was not re-checked.
- #564's Step 8 check read `[]`, and the run reported that as unresolved rather than rewording a body
  that was already correct.
- #560's and #564's refuters each refuted a ticket or plan premise, at 0 rounds.

**Not a lesson:** #560's exemption design, #562's A/B arms and Decision 131, and #564's tail-partner
arrangement are chartsearchai domain evidence.

## 2026-10-07 (window: 3 records over 2 tickets, all from one attended session — #591, aborted at Step 1/2 as "condition 1/2", 0 rounds; #592's first run, aborted at Step 3 under condition 3, 0 rounds; #592/PR593, converged 2 rounds / 4 harden cycles, verifier ran, merged at the reviewed head `48abcf01`) — 0 proposed, 0 applied, 0 killed; no refutation round (nothing to refute); linter 17 files, 0 findings; **no skill file changed**

Step 6's pre-read comparison: `origin/main` (`e12a1de`) and this store agreed on all 288 shared files,
and on all 43 governance files (27 under `skills/`, 7 `hooks/`, `CLAUDE.md`, 3 `bin/`, 5 `pipeline/`).
Each gate's skill copy matched its hook copy. The three records were local-only.

All three records name session `9356b230…`. Its transcript is
`~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-chartsearchai-592/9356b230-6691-4281-ad32-2e72bb32ee1e.jsonl`
(2026-10-02T09:02Z to 2026-10-05T11:10Z), with 19 subagent transcripts beside it. Line numbers
below are that file's.

**This is the first window under `resolve-ticket` 0.24.1 and `pr-harden` 0.38.1.**
- The session loaded `resolve-ticket` at 2026-10-02T10:52Z (L358), after `e12a1de` was committed at
  2026-10-01T22:52Z. That body carries the 0.24.1 sentence.
- A loaded body carries no frontmatter (L358, L1496, L2412). So the version was dated against the
  commit, and could not be read off the record.

**No lesson cleared the bar with a lever.**

**The isolation guard, measured after the 2026-10-02 fix: the rule held, and nothing is owed.**
- Method: `artifacts/2026-10-07-isolation-guard/guard_refusals.py`, the 0.38.1 evidence entry's own
  method.
  - Its `--calibrate` reads the seven cases that entry names as the entry does.
  - Over the entry's five windows it counts 37 refusals, the entry's figure. Exit 0.
- From `EnterWorktree` (L656), the main loop made 189 Bash calls, of which 13 were refused, all 13
  of them compound. The 19 subagents made 498 calls, of which 27 were refused.
- Each of the 13 was followed by a call that passed, 7 of them a script run by absolute path.
- Plain script-by-absolute-path calls: 25, none refused.
  - Three of them (`mut2.sh`, `mut3.sh`, `mut5.sh`) ran `git checkout --` and `git status` inside the
    worktree, which none of 0.38.1's 32 did.
  - Their git targeted the worktree, so this still does not show whether the guard reads inside a
    script.
- `echo $PPID` ran alone (L640, L924), and a literal `--owner` followed it. No `$PPID` refusal, so
  the 0.24.1 sentence held.
- The largest class is `sed` handed a value computed at runtime: 14 of the 40, 12 of them in
  subagents. It is not new: it was 6 of the five windows' 37 (#564's first window 4, #527 2).
- The main-loop rate was 13 in 189 here, against 37 in 948 over the five windows. This is one
  session, and no cause is claimed. Every refusal cost one retry. Splitting every compound call in
  advance would cost more, since 155 of the 189 calls were compound. The rule's second clause, a
  script, avoids the refusal outright, and was used.
- 27 of the 40 refusals came from subagents, whose briefs carry no guard sentence.
  - **Parked, new: guard refusals in the subagents of an `EnterWorktree` session.** Two sessions: #402
    (12 refusals across about 1,000 tool calls, `pr-harden` evidence 0.38.0) and #592 (27 in 498
    Bash calls).
  - **REOPEN ON:** a refusal that cost a round or produced a wrong report.
- #592 wrote its opening entry with `cd <worktree> && gate-state` (L643), before `EnterWorktree`
  (L656), the reverse of Step 1's "before this write".
  - Both calls fell in one turn, so no Stop could fire between them. #527 did the same before the
    rule. Observed.

**Parked counts advanced.**
- **Phase 2 escalating repeatedly on the run's own fixes: 30 records.** +#592, three escalations,
  each on a real defect (`…10-05-…-592.md`:15-17). REOPEN unchanged (:3998-4000).
- **Prose the change made false, found by a fresh agent: 31 records.** +#592, three harden quality
  passes (`…10-05-…-592.md`:18). PR 593 merged at the reviewed head, and none of these is known to
  have reached it. REOPEN unchanged (:4019-4026, :4671).
- **`git checkout -- <path>` discarding the ORCHESTRATOR's own uncommitted work: increments only.**
  +#592 (`…10-05-…-592.md`:24).
  - The restore was a `cp` from the run's own set-aside copy, taken at L1196 and run at the end of
    L1547. A javadoc edit had landed in between.
  - A grep found the edit missing (L1553), and it was re-applied (L1555) before the first commit
    (L1561).
  - That is the mechanism that killed 2026-08-24's P1 (`cp` aside/back, :52-55), now observed. The
    shipped `git-restore-backup.sh` does not reach a `cp`.
  - L1547 opened with a `cmp` against that copy, which reported the difference. Its `&&` stopped
    only the mutation, and the restore on a later line still ran.
  - `harden` was loaded at L1496 and nothing was committed before the probe. So the reopen, an
    incident where commit-before-probe WAS followed, is unmet.
  - Afterwards its mutation scripts ran on a committed tree (L1892 before L1896, L2202 before L2204). From
    `mut3.sh` (L1918) on, its mutation scripts also opened with
    `[ -z "$(git status --porcelain)" ] || { echo "TREE NOT CLEAN"; exit 1; }`. That covers
    `mut4.sh`, derived from it at L1925, and `mut5.sh` (L2203). The check never fired.
    - That is commit-before-probe as a check the probe runs itself, a shape none of the killed
      remedies has. It is a 1-record observation of a remedy, not a proposal.
    - A rule asking for it would still be text the orchestrator must choose to follow. It would not
      reach an inline probe like L1547's, and the loss came from one of those.
- **harden's override taken on the ground that pr-harden covers it** (:1470, parked at 2 and never
  advanced): **5 records**, #268:59, #337:27, #426:34, #469:21 and `…10-05-…-592.md`:23.
  - In #592, pr-harden's r1 then raised two non-blocking findings, fixed before r2.
  - No record shows the override losing a defect that pr-harden then missed. REOPEN unchanged.
- **Abort condition 3 delivers nothing** (:3010): +#592's first run.
  - The same session carried the second refuter's finding (`LlmProvider.java`:267-282) into the
    maintainer's decision comment (2026-10-05T08:55Z) and re-ran. The re-run converged.
  - #513's condition-3 abort (2026-09-24) was not counted by its window, and is not re-read here.
    REOPEN unchanged.
- **Transcript paths that moved after the record named them** (:5144), the other way round this time.
  - #591:5 came from the `ls` the skill prescribes (L413), while the session sat in its starting
    folder.
  - After `EnterWorktree`, the file sits under the worktree's project folder, which outlived the
    worktree.
  - Neither of #592's records ran that `ls`. The first had it inside the compound command the guard
    refused (L846). The retry dropped it, so `…10-02-…-592.md`:5 names only the session.
  - The second (`…10-05-…-592.md`:6) names the starting folder.
  - The uuid glob resolves all three.

**Parked, new, 1 record each.**
- **A ticket in this repo whose remedy lies in a repo this session can only read** (#591:2, :11).
  - Condition 2's "cannot be pushed" covers it, so the abort list has no gap.
  - Step 1's reading of #353's comment and ADR Decision 68 found it at 0 rounds.
  - **REOPEN ON:** a second record, or one that spent code or a round first.
- **Record headers without the skill's version**: 3 of 3 here, and 138 of the store's 150 skill-run
  headers.
  - The count is a regex over the first field of each `# ` line. Its 12 hits are all three-part
    versions, and no two-part form occurs.
  - A loaded body carries no frontmatter, so a run has its version only from the file on disk.
  - **REOPEN ON:** a retro that attributes a record to the wrong version.

**Observed, no remedy owed.**
- #592's first-run refuters found two pass rules that a known-bad answer would pass
  (`…10-02-…-592.md`:11, :14), at 0 rounds.
  - That is the CLAUDE.md calibration rule, applied by the gate.
  - It sits beside :5129, a gate the control arm already failed (too strict, where this was too lax),
    and is not counted there.
- #592's v2 refuter found the plan's "deleting each guard reddens a case" overstated
  (`…10-05-…-592.md`:14). That is the opposite direction from :5132, and is not counted there.
- The v2 refuter's count objection (substances against prescriptions) was recorded in the decision,
  as it asked (L1392). r1 then changed the count (`…10-05-…-592.md`:19, :30). That is a design call
  overturned in review, not a dropped objection.
- `gate-state pr-set` without `--override` writes `override: false` (`gate-state`:389). So #592's
  re-run (L925) re-armed the gate that its abort (L849) had released.

**Not a lesson:** the KB's international drug names (#591, handed upstream), #592's composed
sentence, its count of orders and the DDInter test excerpt are chartsearchai domain evidence.

## 2026-10-14 (window: 3 run records — TRUNK-6826/PR6680, openmrs-core, resolve-ticket 0.24.1, converged 2 rounds / 2 harden cycles; chartsearchai PR #607, attended pr-harden 0.38.1, converged 3 rounds; esm-patient-chart PR 3787, attended pr-harden 0.38.1, converged 1 round) — 2 proposed, both applied after revision, 4 drafted limbs killed at the gates; two refutation rounds, the second over the staged diff; linter 17 files, 0 findings; **pr-harden 0.39.0, resolve-ticket 0.25.0**

Step 6's pre-read comparison: `origin/main` (`53b7446`) and this store agreed on all 292 shared files, and
on all 43 governance files (`skills/`, `hooks/`, `CLAUDE.md`, `bin/`, `pipeline/`). Each gate's skill
copy matched its hook copy. The three records were local-only, and this ledger named none of them.
Proposals, both gate replies and the revisions are in `proposals/2026-10-14-window-6826-607-3787.md`.

**APPLIED after revision · pr-harden 0.39.0 *State*: where `EnterWorktree` refuses the checkout, the gate,
run in the session's directory, reads no entry keyed on the checkout. Key each write `--cwd` on the
checkout anyway, and say so in the report. Step 0's gate sentence is scoped to where the gate reads the
entry.**
- Bar (a): #607:36 and 3787:14. Two attended sessions (auditlogweb, querystore), two PRs, two target
  repositories, one refusal text: "is not a registered worktree of <session repository>"
  (`f1b92832…` L995, `a1472a0f…` L81).
- *State* told a session already running elsewhere to `EnterWorktree` into the checkout, and named no
  refusal. Both runs keyed `--cwd` on the checkout unprompted and said nothing enforced the run.
- **Killed at the first gate: "`EnterWorktree` enters only a registered worktree of the session's own
  repository".** The tool's own description, loaded in both runs (`f1b92832…` L991, `a1472a0f…` L74,
  harness 2.1.296), is broader on a first entry (a repository "nested inside it" in a multi-repo
  workspace) and narrower on a further switch ("a worktree under `.claude/worktrees/` of the same
  repository").
- **Killed at the second gate: "the gate cannot see the run".** An entry keyed on the session's own
  directory is read. Measured on the live gate with a scratch `HOME`
  (`artifacts/2026-10-14-gate-key/gate_key_probe.sh`): such an in-flight entry with no shas blocked, a
  terminal one with no shas allowed, and one carrying the checkout's HEAD as its reviewed sha blocked
  on the session directory's HEAD. So the rule states only what was measured: the gate, run in the
  session's directory, printed nothing over an entry keyed on the checkout. Keying on the session's
  directory instead would wedge the handover once shas are recorded, or give up the sha lists that
  Step 1 and FINISH read.
- **Step 0's "From this point the Stop gate will not let the turn end"**, now "where it reads this
  entry". Round 2 found the State sentence made it contradict the document.
- The paragraph's "anticipated from `KEY="$(pwd -P)"` and not yet observed" moved to evidence.md. The
  measurement is of the script, not a run. No stop in either run was one the gate should have refused:
  each #607 stop after its opening entry came after an `await` write or the terminal entry, and the
  checkout's HEAD never left `3b462015`.
- The `raises` why says the edit restores no enforcement, per round 1's non-blocking objection. Net +29
  words.
- **Not proposed, parked:** a gate that also finds this session's own entry by `owner` when none is keyed
  on its cwd. **REOPEN ON:** a stop the gate should have refused, in a session whose entry was keyed
  elsewhere, or a pool-launched session meeting the refusal.
- **resolve-ticket Step 1** carries the parallel sentence. It is unedited for want of a record, not
  because of abort condition 1, which compares GitHub repositories. This machine holds more than one
  clone each of openmrs-core and chartsearchai (two of each under `~/Projects/openmrs`), so a
  resolve-ticket session can meet the refusal. **REOPEN ON:** a resolve-ticket run that `EnterWorktree`
  refuses.

**APPLIED after revision · pr-harden 0.39.0 `fixer.md`: run every mutation probe before the build `green`
reports on. resolve-ticket 0.25.0 Step 5: after a mutation probe, rebuild the module it compiled before
anything runs the restored code from `target/` or cites its reports.**
- Two parks reopened, each on "a second record". The 2026-09-24 seventh window's #407 park (:4227-4229)
  is met by #607. The 2026-08-30 P3, #250 (:1099-1111), is met by TRUNK-6826. Its "submitted without
  that rule title" clause is honoured: neither rule cites `pr-harden:"After any mutation rebuild with
  clean"`.
- **The fixer's class: #407 and #607, one mechanism.** Each round-1 fixer ran its final root
  `mvn -o clean install`, then probes, and the orchestrator's step-5 spot-check read a probe's
  `failures="1"`.
  - #407's build finished at 12:22:10Z and its probes began at 12:22:42Z (`agent-a1336009…` L88-L111,
    main L358-L363).
  - #607's build (re)started at 13:46:49Z and its probe ran at 13:50:17Z (`agent-ad1f3257…` L241, L264;
    main L1090-L1107).
  - Only #607's fixer read `fixer.md` (L13). #407 predates the file (pr-harden 0.36.0, 2026-09-27), and
    its fixer was briefed in the orchestrator's own words.
- **The restored-mutant class: #250 and TRUNK-6826.** In each, a restored probe's mutant still compiled
  in `target/classes` was run. In #250 two probe runs measured it. In TRUNK-6826 a Jetty start ran it
  (`74a2fd99…` L742-L820), at L794 under Step 5, with `resolve-ticket` the only skill loaded (`harden`
  came at L938). #250's transcript is gone, so who ran its revert-check, and at which step, cannot be
  checked. It was a resolve-ticket run with no compaction (#250:1, :4), so that skill's text was in its
  context throughout.
- **Correction to a record, found by the first gate in #407's transcript:** #407:19 put its residue on a
  `harden` Phase 2 quality lens. It was the round-1 fixer (main L342-L358). The lens's probes ran
  11:47-11:49Z, before the orchestrator's own root build at 11:51-11:54Z (main L246). **So the
  2026-09-24 ruling at :4227-4229, that *Only ONE of them may mutate the worktree* covers the shape,
  rested on the misattribution and is withdrawn.**
- **Killed at the first gate: the `harden` home** (a sentence in Phase 2's *Commit before anything
  mutates the tree*). With #407 re-attributed, its only candidate is #250's unverifiable step.
  **Parked:** a `harden`-run probe leaving `target/` residue, 1 unverifiable record (#250). **REOPEN ON:**
  a record whose residue a `harden` pass or lens left.
- **Killed at the first gate: resolve-ticket's drafted "Run it after your last mutation probe, and before
  anything runs from `target/`", at Step 6.** Step 7's `harden` probes after Step 6 by design, so "last"
  cannot mean the run's last. The universal is false of TRUNK-6826's deliberate runtime mutant
  (L843-L858). Step 6 is also a step TRUNK-6826 had not reached at L794. The rule is now per probe,
  scoped to the RESTORED code, and placed at Step 5.
- Corrections to TRUNK-6826:23: the class's mtime caught the mutant (L797, L809), and `javap` only
  confirmed the recompile (L810-L820). `wait-ready.sh`'s readiness requests did reach the mutant before
  L809 (`session=200`, L797). The probes ran under resolve-ticket Step 5, not `harden`. Corrections to
  this pass's own proposal: `mutate.sh` ran four probes, not three, and the `if (false)` mutant was the
  fourth. Also #607:19's "At FINISH" was round 1's step-5 spot-check (L1090).
- Prunes nothing in skill text. Net +32 (fixer.md) and +28 (resolve-ticket).

**Parked counts advanced.**
- **Prose the change made false, found by a fresh agent: 33 records.**
  - +TRUNK-6826:15: harden P2 found a javadoc and test comment naming the wrong filter as the URL-rule
    enforcer, at 1 cycle.
  - +#607:13-15, :29 and :33. r1, blocking, at 1 round: "runs on Linux CI" in the test javadoc, ADR 174
    and the PR body. Also the README's counters, and r2's `recordLaunch` javadoc.
  - #607 merged at its reviewed head `3b462015`. PR 6680 merged at `e2fa05ef`, whose one commit after
    the handed-over `6522f363` is "Merge master into TRUNK-6826". REOPEN unchanged.
- **Phase 2 escalating repeatedly on the run's own fixes: 31 records** (+TRUNK-6826:15, one escalation,
  counted as :4078 and :4351 count one). REOPEN unchanged.
- **Guard refusals in the subagents of an `EnterWorktree` session** (:5198-5201): +TRUNK-6826.
  - Its `guard_refusals.py` calibration passed (37/37).
  - From L292, the main loop refused 10 of 151, all compound, and subagents refused 18 of 238. Scripts by
    absolute path were refused 0 of 42. Each main-loop refusal's next call passed.
  - The record's "a URL containing github" is two compound commands (main L462, `agent-a77863bc…` L37),
    inside 0.38.1's "git or not". A script retry passed both.
  - The cost was tool calls, so the REOPEN ON (a round, or a wrong report) is unmet.

**Parked, new, 1 record each.**
- **A test that skipped in all three Java CI jobs while three documents said it ran on Linux**
  (#607:13-15, :25; r1 blocking, 1 round). An `assumeFalse` turned GNU `yes` rejecting `-t` into a skip.
  **REOPEN ON:** a second record.
- **A Phase 2 helper merge that introduced an overload ambiguity, caught only by re-running the ordering
  mutation** (TRUNK-6826:24). The ledger holds no earlier Phase 2 edit that introduced a defect.
  **REOPEN ON:** a second record.
- **A runtime-visible frontend change that no verifier run covered, recorded as `converged`**
  (3787:2-3).
  - The run cited the author's manual run and an external bot, and ran no substitute. Step 6's third
    case says "say which one can, use it".
  - FINISH would have it read converged-but-unverified.
  - **REOPEN ON:** a second record, or one whose change was wrong at runtime.

**Observed, no remedy owed.**
- #607's mutation-probe claim about a helper, read as covering its call sites (#607:16-18), is the shape
  `fixer.md`'s *a mutation result measures the arms it moved, not the mechanism* names (:1951, pr-harden
  0.21.0). The PR broke it before the loop, and fresh reviewers caught it as designed. The record's
  "cost: 2 rounds" is discounted as :1996-1997 and :2487-2490 discount: r2 was owed by r1's blocking
  finding, and r3 by r2's three non-blocking findings together.
- #607:37, an `Agent` schema without `run_in_background`, is the condition *State*'s *Collecting in the
  same turn* already names (measured at :3340-3343), recurring at no cost in an attended session. This
  retro's own session's schema lacks the flag too.
- TRUNK-6826's refuter caught a pattern inert under a context path at 0 rounds (TRUNK-6826:8, :14).
- TRUNK-6826 substituted `jetty:run` for the omod standalone (TRUNK-6826:3), which is step 6's third case
  followed.
- TRUNK-6826 deleted an unverifiable javadoc clause rather than rewording it (TRUNK-6826:10).
- #607:43 found a project memory ("Docker is off on this machine") contradicted by its own fixer. That is
  the operator's memory file, not a skill.

**Not a lesson:** TRUNK-6826's Spring 7 `UrlHandlerFilter` behaviour, #607's llama-server thread and
cgroup readings, and 3787's `routes.json` ranges are target-repo domain evidence.

## 2026-10-22 (window: 1 run record — querystore PR 100, a standalone `/harden` 0.43.2 on an open PR, converged at cycle 7 after 10 h 35 min, captured by hand) — 2 proposed, both applied after revision, 0 killed; two refutation rounds, the second over the staged diff; linter 17 files, 0 findings against `1ed1eaa`; **harden 0.45.0, skill-retro 0.7.1**

Step 6's pre-read comparison had no live side in this cloud container. It holds no
`~/.claude/skill-lessons`, `~/.claude/hooks` or `~/.claude/CLAUDE.md`, and its `~/.claude/skills` holds
none of these skills. The store read was this checkout's `.claude/skill-lessons/` at `1ed1eaa`, PR 100's
head. Its merge base with `origin/main` is `origin/main` itself (`a829dcd`, re-fetched in Step 6), so
the read missed nothing pushed. The cmp steps are owed where a live copy exists: on the operator's
machine, once this lands.

This branch is stacked on PR 100. `--against origin/main` reports harden's rise as 7403→7538, because
this raise replaces PR 100's 7403→7429 entry, as `budget_ratchet`'s docstring says a second rise does.
Against `1ed1eaa`, the head this retro started from, it reports nothing. Step 6's zero against
`origin/main` is still owed: once PR 100 merges at harden's 7429, re-run `skill-lint.py --against
origin/main`.

The one record corroborates nothing by itself. Each proposal rests on it together with a record from an
earlier window, and each answers a REOPEN ON this ledger held. The proposals, the gate's replies and the
revisions are in `proposals/2026-10-22-window-pr100.md`.

**APPLIED after revision · harden 0.45.0 *Reporting*: a `/harden` invoked on its own appends its run
record, in `pr-harden`'s *Write the run record* format and file name, with the branch (any `/` replaced by `-`) where the slice
has no ticket or PR. skill-retro 0.7.1: its opening names that run beside the two skills, and Step 1's
"writes none" is dated to the versions before.**
- It reopens the 2026-09-16 KILLED P1 (:3041-3058) on that kill's REOPEN ON, "a second
  standalone-`harden` record". #435 was the first standalone record, and PR 100 is the second. The gate
  found no other standalone record between them. Bar (a): #435:5-7 and :84, and pr100:5-6.
- The kill's prevention objection (:3056-3057) is answered by where the rule lives: the record is written
  in the run, not in how a retro reads the store.
- The first gate raised five objections, all non-blocking, and each is settled:
  - the kill's grounds are described as bar (c), with the prevention objection as its own paragraph;
  - the file name falls back to the branch;
  - skill-retro:10 names the run;
  - the version pin was re-checked: harden is 0.43.2 on `origin/main` and 0.44.0 at `1ed1eaa`, and PR
    100 is open at `1ed1eaa` (REST API, 2026-10-22), so 0.45.0 holds if PR 100 merges at 0.44.0;
  - the budget raises are made.
- The second gate added two non-blocking objections, both applied:
  - the branch's `/` becomes `-`, since a `/` makes `>>` fail or files the record where the store's
    `*.md` does not look;
  - evidence.md's "often before either exists" becomes "can be", since both standalone records had an
    open PR (#435:85, pr100:5-6).
- It prunes nothing. Net +61 words in harden and +12 in skill-retro.

**APPLIED after revision · harden 0.45.0 *Termination*: an edit that rewrites a test owes the
revert-check on the mutations that test caught before the edit.**
- It reopens the 2026-10-14 park (:5392-5394) on that park's REOPEN ON, "a second record". Bar (a):
  - TRUNK-6826:24: a Phase 2 helper merge introduced an overload ambiguity, "caught only by re-running
    the ordering mutation".
  - PR 100: cycle 5's Phase 2 commit `a7cb1dc` folded a launch into the `--work` environment case, and
    had the case inherit the marker a carried launch sets.
- It would have caught PR 100's defect a cycle earlier:
  - the case caught `nomark` before the edit (L3869-3870), and L4353 still lists that mutant;
  - the run after the edit ran four mutants, two of them new and none of them `nomark` (L5447-5448);
  - cycle 6's first Phase 1 pass found the gap with that mutant (L5525).
- **Blocking at the first gate, settled by revision:** the drafted evidence said the run "re-ran four
  mutants", but two of the four were new.
- Its non-blocking objections were also applied:
  - "owes that check" now names its target;
  - the Step 4 note's "committed with the build green" is in no text, since TRUNK-6826:24 does not
    say it.
- The second gate's one objection here was to the `raises` why: "only a re-run mutation showed it"
  overstated PR 100, whose reviewer named the gap by reading the fixture and then confirmed it with the
  mutant (L5525). It now reads "and a re-run mutation showed it".
- It subsumes nothing. The bullet before it covers an edit that FIXES a behaviour or ADDS a guard, and a
  rewritten test does neither. Net +48 words.

**Corrections the first gate found, now made in the record and listed in the proposals file's
revisions.**
- "Cycles 5-7, each opened by a one-line fix" is false for cycle 5, which three findings opened, one of
  them a runtime change. Cycles 1-4 took 5 h 24 min, and cycles 5-7 took 5 h 12 min.
- `d7cfefa` changed no words in pr-harden's evidence.md, because it was a reflow. `git log -S` names it
  anyway, which is the failure :3036-3037 records.
- Both evidence sentences whose escalations opened cycles 5 and 6 were written in `db9fe91`, under cycle
  3's state write (L3348).
- Both of cycle 6's first-pass findings came from `a7cb1dc`, not one.
- The record's context line counts two compactions, not one.

**Corrections the second gate found, made in the record, this block and the proposals file.**
- **Blocking: no harden Stop gate was registered in this container.**
  - `~/.claude/launcher-settings.json` registers one Stop hook, `stop-hook-git-check.sh`, and there is
    no user or project `settings.json`.
  - Each of the transcript's 56 `stop_hook_summary` events (L696-L7054) counts one hook and names it.
  - The record's "the gate's attended allow carried the yields" was false, and so was this block's
    first draft of it.
- **Blocking: the delta-only premise was false.** Each escalating evidence sentence was inside the diff
  of the Phase 2 that found it:
  - `d7cfefa`'s reflow put both in `c5111fe..6e8b512`, cycle 4's Phase 2;
  - `bd1b5b2`'s edit put the second in `6e8b512..d5dcea8`, cycle 5's.

  The first gate had endorsed the premise.
- **Blocking: the record's "each cost about an hour"** was false for cycle 6, which took 3 h 09 min.
- The record now states its cycle convention: each range runs from the cycle's first agent dispatch, and
  the run's `--cycle` writes open each cycle earlier. Its corrections anchor on those writes.

**REOPEN ON met, no text owed · a `run_in_background: false` spawn answered "Async agent launched"
(:4364-4367): 2 records.**
- PR 100 is the new record. It was attended and cost 0.
  - Inside the run, each of 37 `Agent` calls passed `false`, and each was answered "Async agent launched
    successfully" (transcript L1457-L6026; the session spanned harness 2.1.306 to 2.1.308).
  - No harden Stop gate was registered in this container, so no yield on outstanding agents could be
    refused (the second gate's corrections, above).
- The clause met is "a second post-0.30.0 record". No text is owed:
  - `pr-harden`'s *Collecting in the same turn* (:679-683) is headed "where the harness allows it". It
    already names this answer and the unattended fallback: "where a spawn still answers "Async agent
    launched", an unattended run keeps the turn alive with a bounded foreground wait".
  - harden's Phase 2 points there.
- The first gate's count, "34 of 36", did not reproduce. The condition is met either way.
- **REOPEN ON:** one that cost a turn or a Stop refusal, or one from a session with a harden or
  pr-harden Stop gate registered, which PR 100's was not.

**Parked counts advanced.**
- **Phase 2 escalating repeatedly on the run's own fixes: 32 records.**
  - +PR 100: six of seven Phase 2 passes escalated, and this was the record's main cost.
  - Three remedies were weighed against its REOPEN ON and not proposed. **Correcting a Phase 2 finding
    in place** was killed before the gate on 2026-09-24 (:4068-4076), and PR 100 repeats the kill's
    reason. Cycle 5's Phase 2 found one evidence sentence. The Phase 1 it reopened found four
    substantive items in cycle 6, two of them introduced by `a7cb1dc`.
  - **Exempting `evidence.md` from escalation** is the same remedy narrowed to one file, and loses the
    same four.
  - **A delta-only Phase 2 after an escalation** is not refuted by this record. `d7cfefa`'s reflow and
    `bd1b5b2`'s edit put both sentences' lines in the diff of the Phase 2 that found each. A proposal
    would have to show, finding by finding across the records, what each escalating Phase 2 found
    inside its delta and what it reached only by tracing outward from it.
  - REOPEN unchanged.
- **Prose the change made false, found by a fresh agent: 34 records.**
  - +PR 100: cycle 4's Phase 1 found two unchanged passages the slice falsified.
  - The Phase 2 passes of cycles 4 and 5 each found a false `evidence.md` sentence that the slice
    wrote.
  - Cycle 6's first Phase 1 pass found two documents that `a7cb1dc` made false.
  - PR 100 is open, so none reached a merged head. REOPEN unchanged.
- **Usage-limit interruption: 3 records** (:4004).
  - +PR 100: a claude.ai usage limit at 00:11Z killed the four cycle-3 Phase 2 agents, and the user
    resumed at 00:21Z. No convergence was lost.
  - The session ran without `CLAUDE_CODE_RETRY_WATCHDOG`, which is PR 100's own subject.
- **Not advanced, on the first gate's objection:** *harden's full cycle as a fixed cost on a small fix*
  (:4063) stays at #408 alone. That class is a 2-line change with lenses under a minute each. PR 100
  was 19 files, +810, with Phase 2 passes of 25-35 min, and its cost belongs to the escalation park.

**Parked, new, 1 record each.**
- **A `/harden` run in a cloud container had no harden Stop gate registered** (PR 100).
  - The container's one Stop hook was `stop-hook-git-check.sh`. The run recorded its state with the
    repo's vendored `gate-state` (L1037), which no hook read, so nothing enforced its termination or
    its yields.
  - Cost: none observed, since the run was attended and converged.
  - **REOPEN ON:** a second record, or a cloud run that ended with Phase 1 open or agents outstanding.
- **A container restart killed an in-flight Phase 1 reviewer** (PR 100). The reviewer was launched at
  03:39Z, killed at 03:44Z and re-run at 03:45Z, a cost of minutes. **REOPEN ON:** a second record, or
  one that cost a cycle.
- **pool-test's `live_session_fixture` leaves an orphan `sleep 300` per case** (PR 100).
  - The orphans held a harness's captured stdout pipe for about 10 min. Writing to a file avoids it.
  - This is a test defect, not a skill rule.
  - **REOPEN ON:** a second record, or one in a pipeline run.
- **The budgets' unit, `LC_ALL=C wc -w`, is not the linter's count on GNU coreutils 9.4.**
  - `skill-budgets.json`'s `unit` and `skill-lint.py`'s `words()` docstring say the two agree.
  - In this container they differ: `LC_ALL=C wc -w` counts harden at `1ed1eaa` as 7322, and the
    linter counts it as 7429.
  - GNU `wc` skips a token made only of non-ASCII bytes. `printf 'a — b'` counts as 2 words there, and
    as 3 under Python's `bytes.split()`.
  - This window used the linter's count, at no cost. The tool on the operator's machine was not
    measured.
  - **REOPEN ON:** a second record, or a budget set from `wc` that the linter then failed.

**Not a lesson:** PR 100's measurements of Claude Code's limit handling. They are the slice's own domain
evidence, in its artifact: the retry line every 30 s, a `0` in `settings.json` overriding an alias's
`=1`, and 2.1.307 passing `background_tasks` to the Stop hook.
## 2026-10-23 (window: no run record — five measurement notes, four `pr-review` replays dated 2026-10-18 and one `harden` measurement dated 2026-10-23) — 1 applied (bar (c)), 0 killed, every ledger entry revised at the first gate, parked below; two refutation rounds, the second over the staged diff; linter 17 files, 0 findings; **harden 0.44.1**

**Step 6's pre-read comparison.**

- `origin/main` (`72df9d9`) and this store agreed on all 415 shared files.
- They agreed on all 43 governance files (`skills/`, `hooks/`, `CLAUDE.md`, `bin/`, `pipeline/`).
- Each gate's skill copy matched its hook copy.
- The five notes were local-only, and this ledger named none of them.
- Nothing was drawn from `confidential/`. Only its file names and `PRS` were read, as Step 1 allows.

No run record is newer than `LAST`, so nothing here corroborates a lesson. A measurement "is admissible
as argument … and has never counted as corroboration" (:2148-2150). The one skill edit is a bar-(c)
correction.

- The proposals, both gates' verbatim replies and the revisions are in
  `proposals/2026-10-23-window-five-measurements.md`.
- This pass's instruments and their outputs are in `artifacts/2026-10-23-retro-window/`. Its README
  says which output backs which figure below.

**Re-derived rather than taken from the notes.**

- The 6551, queue96 and union-556 notes' own `scripts/score.py` print their tables.
- The reviewer-brief-556 note has no adjudication. Its "S1: R 0 of 3, P 0 of 3" holds under a search
  whose pattern hits that note's protocol (`notes-checks.out` 1).
- The harden note's `rederive.sh` reproduced all three of its stages before the deletion recorded
  below. After it, the rates still re-derive.

**APPLIED · harden 0.44.1, Transition gate: "say BOTH out loud" now reads "say these out loud".**

- Bar (c). :77's "BOTH" stood over three blockquoted sentences, against :85's "ALL THREE sentences".
  - `01d1cd2` (2026-05-20) wrote "BOTH" when the gate had two sentences.
  - `c18bf29` (2026-05-20) added the third sentence and changed :85, but not :77.
- "these" replaces the draft's "them", at the gate's suggestion: "them" reads back to the stop
  conditions at :72-75.
- The edit is word-neutral: 7429 words, the budget.
- `evidence.md` has no Transition gate entry, so nothing moved.
- `skill-lint.py` did not flag the stale count; see the linter entry below.

**Measured and not adopted: skill-text candidates the 2026-10-18 notes tested.** Each one is recorded in
its own note. REOPEN ON for each: a measurement on another PR where the candidate moves a
pre-registered outcome.

- **(a) A verification-techniques file beside `pr-review`, with a Step 3 pointer** (6551, arm K).
  - The file was read in 3 of 3 runs, the non-admin probe run in 1, `NOT_SUPPORTED` in 0.
  - L04 filed blocking: 0 of 3, against the control's 0 of 3.
- **(b) A *Trace outward* thread naming transaction, flush, handler order and required privilege**
  (queue96, arm T). Its runs asked the transaction question in 3 of 3 runs and answered it wrongly in
  3 of 3 (K7 0 of 3), on the subject the text was written from.
- **(c) The per-condition mutation sentence** (queue96 T, 6551 M).
  - T's unique REAL defect, the unpinned `purgeVisit` guard (2 of 3 runs), was not a pre-registered
    target.
  - On 6551, M found 0 of its targets (L06/L08), and the mutation-shaped finding there came from C and
    K.
- **(d) "A concern is cleared by the same bar a finding has to meet"** (queue96, arm V,
  `…-queue96.md`:28-30, added after waves 1-2). K7 0 of 3, and wt-12 cleared it "by reading the proxy
  chain only, so not raised" (:65-66).
- **(e) "A claim in the review conversation about runtime or framework behaviour is a lead to verify,
  whoever made it"** (queue96 `PROTOCOL.md`:115-117). Tested by withholding the conversation (arm B):
  K7 0 of 3.
- **(f) `pr-harden/reviewer.md`'s "never against the PR description" sentence, ported into
  `pr-review`** (556, arm P, with "agent" read as "person"). S1 0 of 3.
  - The pipeline's own round-1 brief with today's `reviewer.md` (arm R) also found S1 in 0 of 3.
  - So its 2026-09-29 catch rests on one run.

**Parked, measurements only, 0 run records.**

- **A demonstrated privilege bypass filed below blocking.**
  - On 6551, all 6 L04 findings were filed as questions (`notes-checks.out` 3). One of them is
    wt-02's, filed after it ran the exploit with a positive control.
  - The 2026-09-11 A/B's control filed L04 as a suggestion (`…-ab.md`:46-47).
  - `pr-review`:152 sends "uncertainty about an observed side effect" to a question and names no
    exposure case. :158 makes a written failure mode the blocking test.
  - The candidate rule, "a privilege bypass you have demonstrated is blocking", rode untested in 556's
    arm U, because nothing in 556 is an exposure.
  - **REOPEN ON:** a run record where a demonstrated exposure was filed below blocking, or a
    measurement of the rule on a PR that holds one.
- **Routing `could_not_verify` to the author.**
  - 556: 25 risk items over all nine runs, 19 of them NOISE. Arm U's pairs held 18, 13 of them NOISE
    (`notes-checks.out` 4). Routing alone reached neither S1 nor S3.
  - queue96: 6 of 12 runs put K7's unobserved rollback in that field and raised nothing (C 1, T 2,
    B 0, V 3).
  - `could_not_verify` is a field of the measurement briefs only. `pr-harden/reviewer.md`:26-33
    returns `findings` and `notes`, and no live skill names the field.
  - **REOPEN ON:** a pre-registered measurement where routing reaches a blocker the findings missed,
    or a run record where a reviewer's `notes` held a blocker the round did not raise.
- **A union of two independent single-context reviewers** (556, arm U).
  - S3 surfaced in 1 of 3 pairs, against 0 of 3 controls, below the pre-registered bar.
  - U bundled the union with routing and the disposition rule, and the one S3 came from a single
    reviewer (wt-07).
  - It is not per-dimension fan-out, so it does not meet P-A's condition.
  - **REOPEN ON:** a pre-registered measurement where a union clears its bar.
- **A limitation the PR declares, accepted as scope** (556).
  - S1, and S3 in its narrow form, sit inside limitations the description declared, and all nine
    `pr-review` replays accepted them.
  - The run whose round-1 head they replayed did not. #555's r1 raised S1 regardless
    (`2026-09-29-…-555.md`:17, :31), a record the 2026-09-29 window counted. r1 and r2 missed S3, and
    the record names no cause (:19).
  - The closest tested remedy is (f) above, at S1 0 of 3.
  - **REOPEN ON:** a run record where a declared limitation hid a blocker.
- **Agent-tool subagents load the project's memory index.** That goes beyond what `pr-harden`:42-44
  says a non-fork subagent reads ("Any other subagent type starts clean. It still reads `CLAUDE.md`
  and the repo's skills").
  - Two observations: queue96's canary (`PROTOCOL.md`:69-72), and this pass's own refuter, whose
    transcript holds the `MEMORY.md` index as an `AutoMem` record.
  - **REOPEN ON:** a run record where a reviewer's or refuter's loaded memory carried content about
    the change under review.

**P-A (:2143, :2307-2327): parking reason qualified, condition unchanged.**

- In the 2026-09-11 A/B's run 1, fan-out found "no blocker the single context missed", partly because
  its control found L02 (`artifacts/ab-6551/arm-A-control.json` A1, filed blocking).
- At `pr-review` 0.19.0, L02 was found by 0 of 3 controls and 3 of 9 runs. So that half of "measured
  twice, no blocker yield either time" rests on a control draw that later measured 0 of 3.
- That later measurement also used a later skill version and a different harness. The A/B ran
  Agent-tool subagents (`…-queue96.md`:82-83); 2026-10-18 ran headless clones without project memory.
- queue96 at `ae7f8fa2` is a subject the condition can use: single contexts missed K7 12 times in 12.
- No fan-out arm ran in any of the four measurements.

**Phase 2 escalating repeatedly on the run's own fixes (:5378, 31 records; REOPEN at :3998-4000).**

- The remedy "enforce Phase 1's confirming pass" was measured observationally, over 57 public runs and
  127 classified traversals.
- After a re-review, 71% of first traversals and 45% of resumed ones escalated. After a mechanical
  check or nothing, 67% and 54% did.
- The count and REOPEN are unchanged, since a measurement adds no record. A proposal of this remedy has
  to show an effect this comparison did not.
- Limits (`…-confirming-pass.md`:99-101, :107):
  - the comparison is observational;
  - one classifier, neither blinded nor independent, sorted every traversal;
  - 55 of the 57 runs are chartsearchai, from about one week.

**Observed: harden's gate sentences are not said in their literal form** (`gate-sentences-after-deletion.out`,
with `controls.out`).

- 58 public-record transcripts load harden, all of them on a version carrying every gate. Of those:
  - 0 state the Transition gate's three sentences or the Termination gate's;
  - 1 states the Phase 2 gate's (`2026-09-23-…-477.md`'s transcript).
- Runs announce convergence in their own words ("Harden Phase 1 has converged.").
- For this population, that contradicts `harden/evidence.md`:214, "the phase gates demand verbatim
  sentences and get them", which is the Termination gate's stated rationale.
- 1 measurement.
- **REOPEN ON:** a proposal to delete or replace those sentences. This count is evidence that they are
  not said, not that they do nothing.
- **Corrected the same day (addendum below):** 61 transcripts load harden, not 58; 0 and 2 of them, not
  0 and 1.

**Linter: `stated_counts` did not flag harden:77.** Three things missed it (`skill-lint.py`:16-18, :27,
:36): no noun from `NOUNS` followed "BOTH", "both" is not in `WORDS`, and blockquote items are not
counted.

- A widening run in scratch (`count-widening-before-harden-0.44.1.out`), counting the number words one
  to ten and "both" with `>` items, gave 15 hits on 13 lines, 1 of them true.
- "both" alone gave 3 hits, 1 true.
- A capitals-only "BOTH" gave 1 hit, 1 true, but it is fitted to the one instance.
- **REOPEN ON:** a second stale count the linter missed.

**Observed, operator action recommended (not a skill change): the transcripts records cite are
disappearing.**

- During this pass, two transcripts named by public records were deleted: `b2cc1733…` (the 2026-09-16
  PR440 record) and `56821ae1…` (the 2026-09-21 PR465 record).
- Claude Code 2.1.309's settings schema describes `cleanupPeriodDays` as "Number of days to retain chat
  transcripts before automatic cleanup (default: 30)". `~/.claude/settings.json` does not set it.
- 77 of the 141 distinct paths on public records' `transcript:` lines are gone
  (`transcript-presence.out`). Five of those belong to records under 30 days old:
  - one is a `<cwd-slug>` placeholder;
  - four have lost their whole project directory too, and what removed them was not established.
- Costs so far:
  - #250's step attribution can no longer be checked (:5346);
  - queue96's 2026-09-16 handler-order miss cannot be explained (`…-queue96.md`:80-81);
  - the harden note's first two stages no longer re-derive.
- The action is the operator's: a long `cleanupPeriodDays` in `~/.claude/settings.json`.
- **Corrected the same day (addendum below):** none of those five is lost. Four were relocated by
  Claude Code, and the fifth's transcript was never moved. 72 of the 77 are gone.

**Observed, no remedy owed.**

- reviewer-brief-556's isolation finding (`…-reviewer-brief-556.md`:43-49): earlier briefs left the
  scratchpad's other folders readable. A scan of 30 earlier transcripts found no read. This is
  measurement hygiene, not a skill.
- queue96's round-1 handler-order miss does not reproduce: 12 of 12 runs found K6
  (`…-queue96.md`:50).

**Running parked counts:** unchanged. The window holds no run record.

## 2026-10-23 (addendum to the window above, operator-directed: the lost transcripts re-measured by session id) — 2 entries corrected, 1 parked; no skill file changed; **Step 5 not run**

The owner asked, after the window, to "dig into the lost project folders". The re-measure is in
`artifacts/2026-10-23-retro-window/`, and its README says what each output shows:
- `transcript_presence.py`, which now also searches every folder by session id, with its output in
  `transcript-presence.out`;
- `relocations.py`, with its output in `relocations.out`;
- `gate-sentences-relocated.out`.

**CORRECTED · the transcript-loss entry.** The block above says four records under 30 days old had lost
their whole project directory, by a cause not established. The transcripts of all five records under
30 days old are on disk.

- **Four, Claude Code relocated itself:** #527, #402/PR544, #562 and #564 (`relocations.out`).
  - Each run entered its worktree with `EnterWorktree`.
  - Each then looked its transcript up by session id from inside the worktree: #527 at L3120, #402 at
    L2157, #562 at L2575, #564 at L1575 of its transcript.
  - Each record names the worktree-folder path that lookup printed.
  - In each transcript, the last `worktree-state` record shows the worktree session exited, and the
    last `relocated` record points at the main `chartsearchai` checkout. All four now sit in that
    checkout's folder.
  - So each record was right when it was written, and went stale afterwards.
- **The fifth is the 515 driver capture.** It names an unfilled `<cwd-slug>` placeholder. Its transcript
  sits under the pipeline worktree's folder and was never relocated.
- **What is actually gone.** 72 of the 141 paths name a transcript that is missing from every folder.
  All 72 come from records dated 2026-08-26 to 2026-09-21, more than 30 days old
  (`transcript-presence.out`).
  - No mechanism beyond the 30-day cleanup is in evidence.
  - `cleanupPeriodDays` was set to 3650 on 2026-10-23, at the operator's direction.

**CORRECTED · the gate-sentences entry.**

- Three of the five relocated transcripts load harden, on a version carrying every gate. #527's states
  the Phase 2 gate's sentence (`gate-sentences-relocated.out`).
- So the population is 61, not 58:
  - 0 state the Transition gate's sentences or the Termination gate's;
  - 2 state the Phase 2 gate's (#477, #527).
- The entry's reading and its REOPEN stand.

**Affected, not re-run: the harden note's population** (`…-confirming-pass.md`). That population came
from `public-transcripts.py`, which keeps a transcript only at its recorded path, so the relocated runs
were left out. Its escalation rates were not re-derived with them.

**Parked for the next retro: the example both evidence files give of a name taken "from anywhere
else".**

- `pr-harden/evidence.md`:1090 and `resolve-ticket/evidence.md`:591 say "#527 recorded the folder of
  the worktree it had moved into".
- #527 found that folder by a `find` on its own session id (L3120), which is the kind of lookup the rule
  prescribes.
- #402/PR544, #562 and #564 recorded the same kind of path after the rule shipped (`2794d2c`,
  2026-09-25), each from a lookup by session id.
- The rule gives the path as it stands at write time, and Claude Code can move the file afterwards.
  Read against this, the example looks like a relocation rather than a misnamed path.
- Not edited here: it is skill text, and this addendum ran no refutation.
- **REOPEN ON:** the next retro, which owes it a fresh agent's check.
- Until then, a reader of any record's `transcript:` line should resolve it by session id:
  `ls ~/.claude/projects/*/<uuid>.jsonl`.

**Deviation, recorded because it is one:** Step 5 was not run on this addendum. Its figures come from
the outputs it names, and its commits and evidence lines from `git log` and the files cited.

## 2026-10-23 (targeted pass: the #527 example the addendum above parked) — 1 applied after revision, 1 correction found through the second gate's report, 1 parked by the gate; three refutation rounds, the second and third over the staged diff; linter 17 files, 0 findings; **pr-harden 0.41.1, resolve-ticket 0.29.1**

**Step 6's pre-read comparison.** `origin/main` (`92781d1`) and this store agreed on all 590 shared files,
and on all 43 governance files.

**The window.** No record is newer than `LAST`, so the window is the addendum's parked item and the four
records it rests on: #527, #402/PR544, #562 and #564.
- The proposals, each gate's verbatim reply and the revisions are in
  `proposals/2026-10-23-targeted-527-example.md`.
- The instruments are in `artifacts/2026-10-23-targeted-527/`, beside `…-retro-window/` from the
  addendum. Each folder's README maps its figures to their outputs.

**This is not new ground.**
- The ledger has carried this shape since 2026-10-02: "Transcript paths that moved after the record
  named them" (:5144-5149), filed under "Observed, no remedy owed" (:5143).
- On 2026-10-07 :5242-5250 advanced it under "Parked counts advanced" (:5207), with no reopen condition.
- :5149 left the #527 example "not re-checked".
- The window block above read these moves as losses (:5751). Its correction (:5757-5758) and the
  addendum (:5783-5791) re-derived the move without citing either entry.

**APPLIED after revision · pr-harden 0.41.1 and resolve-ticket 0.29.1, `evidence.md`: the #527 example
is deleted from the list that supports "Take neither name from anywhere else", and a note records why.**

- **Bar: none needed.** Deleting a false clause is a patch (:2454-2455). The note is the measurement
  that retires the example, which Step 4 keeps in `evidence.md`.
- **What #527 did** (transcript `ebc78818…`). It answers :5149: the example fits the move.
  - It started in the main checkout (L5) and entered its worktree with `EnterWorktree` (L119).
  - From inside the worktree it looked its own session id up (L3120) and wrote that path into its
    record (L3125). `record-write.out` shows the header naming the folder that then held the transcript.
  - Claude Code moved the transcript back to the main checkout's folder when the worktree session
    exited (L3161, L3165).
- **Revised at the first gate.** The note's lead follows the sibling "*Pruned 0.38.1:*"
  (`pr-harden/evidence.md`:786), and the note cites the transcript's own lines beside `relocations.out`.
- **The rule's own count changes.** The 2026-09-25 folder half (:4568-4570) falls from 10 records to 9,
  which is still bar (a). The rule stands.
- **Size.** Neither SKILL.md changes beyond its version line, and `evidence.md` has no budget.

**APPLIED as a correction, found through the second gate's report · resolve-ticket 0.29.1
`evidence.md`:115-116.**

- The entry said TRUNK-6826's transcript "lives under" `core-6826`'s project folder. It now says it
  lived there while the session worked in that worktree, with a "*Corrected 0.29.1:*" note after the
  paragraph.
- During this pass, that session's worktree session exited and Claude Code moved the transcript to
  `openmrs-core`'s folder (transcript `74a2fd99…`, L1650-L1653). L1652's `cost-state` puts that at
  2026-10-23T11:24:58Z.
- `2026-10-11-openmrs-core-TRUNK-6826.md`:5's path is stale for the same reason. The uuid glob resolves
  it (:5148).
- Deleting the clause would have broken the entry's reasoning ("which is why a private ticket's slug
  never carries its key"), so this is replacement text.
  - It ships on the third gate's verdict: CLEAN.
  - The note's wording and citations follow that gate's non-blocking points.

**PARKED by the gate · skill-retro Step 1: a reader-side remedy for :5144's shape (look a record's
transcript up by its session id before reading it as gone).**

- **Bar (a) is not met.**
  - The four records carry the stale paths, not the lesson.
  - Both sightings of the lesson are reader passes: the 2026-09-25 owner-directed pass and the
    2026-10-23 window's retro (:5748-5751). That retro's misreading of all four was one script run.
  - Step 1 admits run records only (:1502, :2148-2150).
  - :5144 owed the shape no remedy, and :5242 advanced it with no reopen condition.
- **The draft's first sentence is false of the store.** It read: "A record's `transcript:` line is
  where its transcript was when the run wrote it."
  - #592's second header was wrong when written (below).
  - The 515 driver capture names a placeholder.
  - `2026-10-02-chartsearchai-592.md`:5 names a session, not a path.
  - #505's header names another session (:4604-4606).
- **The draft would not have reached the 2026-09-25 misreading.** That pass searched by uuid, and read
  #527 as misnamed, not gone.
- **Count: 2 reader sightings, 0 run records. REOPEN ON:** a run record where reading a moved
  transcript's path as lost cost a round, or a second retro that does.
- **The mechanism, as the gate measured it and this pass re-ran it** (`relocation-mechanism.out`,
  calibrated in `calibrate.out`):
  - Re-run after the TRUNK-6826 move: 767 of 767 top-level transcripts sit where their last `relocated`
    record, or their first cwd when there is none, puts them.
  - The 8 sessions whose `relocated` records reach outside their first cwd all called `EnterWorktree`.
    The other 2 callers, #607 and 3787, were refused and moved nothing.
  - 2 sessions' last `worktree-state` still names a worktree, and both sit in that worktree's folder.

**Correction to :5242-5250, not a new count.**

- :5249, "(`…10-05-…-592.md`:6) names the starting folder": that header was wrong when it was written.
  - At L2582, from inside `chartsearchai-592`, it named the starting folder.
  - The transcript was then held for the worktree, after 82 `relocated` records (`record-write.out`).
  - The worktree session's later exit moved the file to the starting folder, so the header reads right
    today. The exit is at L2604-L2608; L2606's `cost-state` puts it at 2026-10-08T14:01:02Z.
- :5245-5246, "the file sits under the worktree's project folder", no longer holds, for the same reason.
- Neither does :5163-5164, which names that transcript under `-chartsearchai-592/`.

**Running parked counts:**
- The folder half (:4568-4570) falls from 10 to 9.
- :5144 stays "no remedy owed", and :5242 keeps no reopen condition.
- :5149 is now answered, and :5249, :5245-5246 and :5163-5164 are corrected.

## 2026-10-23 (owner-directed: harden's gate sentences, and the confirming-pass measurement re-run with the moved runs) — 1 proposal parked by the gate, 1 measurement re-run; no skill file changed; one refutation round

The owner directed both. The direction on the gate sentences rested on this session's statement that
runs never state them, and the gate found that statement holds only after harden 0.34.0.
- The proposal, the gate's reply and what changed are in
  `proposals/2026-10-23-owner-directed-harden-gate-sentences.md`. One line of the reply is redacted
  there: it cited a session that belongs to a ticket whose records are confidential.
- The evidence is in `artifacts/2026-10-23-gate-sentences-periods/` and
  `artifacts/2026-10-23-harden-phase1-confirming-pass/relocated-addendum/`, each with a README.

**PARKED by the gate · a harden 0.45.0 proposal: each gate states its required content instead of a
fill-in template.** :5726's REOPEN ("a proposal to delete or replace those sentences") was met, and
the proposal does not survive the facts.

- **Before 0.34.0** (`6f15b1c`, 2026-09-22). The pool's stream logs, 2026-08-26 to 2026-09-17, cover 13
  runs that load harden.
  - 8 of them state the Transition gate's sentences in their literal form.
  - 6 answer at least 4 of its 6 integration questions in one block (`gate-sentences-pre034.out`,
    `integ-pre034.out`).
- **After 0.34.0.** The 61 transcripts behind :5716-5729 do neither: 0 and 0. The most any reaches is
  3 topics (`integ-post034.out`). So the Transition gate's content went with its wording.
- **Three changes coincide.** harden 0.34.0 moved the verdict into `gate-state harden-set`. All 13
  earlier runs ran on Opus 5 or Sonnet 5, and all 61 later ones on Opus 5.5. The 61 ran on Claude Code
  2.1.280 or later, and the pool logs record no version (`models.out`). These counts cannot separate the
  three, so the cause is not established.
- **The gate's other blocking points lapse with the proposal:**
  - the Phase 2 gate lost "ran once" and its edit count as a number;
  - that gate went beyond what the owner directed;
  - the measurement note belonged under each gate's own heading.
- **REOPEN ON:** the owner deciding again on these facts, or a measurement that separates harden
  0.34.0 from the model change.

**Corrections to :5716-5729.**
- Its title and counts hold for its population, which is the runs after 0.34.0, and not before.
- `harden/evidence.md`:214's "the phase gates demand verbatim sentences and get them" held for the
  period it was written in (`5bc2d71`, 2026-08-17). The pool logs bear it out.
- :5723's "for this population" was the right limit.

**Re-run · the harden confirming-pass measurement, with the moved runs** (:5809's "Affected, not
re-run"):

- Three moved runs, #527, #562 and #564, add 8 traversals to the note's 127.
- A fresh agent classified them blind, with record and outcome removed, among 12 calibration blocks.
  It agreed with the note's reader on 10 of 12 exactly, and on 11 of 12 by table group
  (`score-blind.out`).
- Over 135 traversals (`combined-rates.out`):
  - first traversals escalated after a re-review in 72%, and after a mechanical check or nothing in 65%;
  - resumed traversals, 47% and 54%.
- The note's reading stands, and its addendum records this. :5703's park and REOPEN are unchanged.

**Running parked counts:** unchanged.

## 2026-10-26 (window: no run record since 2026-10-23) — 0 proposed, 0 applied, 0 killed; no refutation round (nothing to refute); linter 17 files, 0 findings; **no skill file changed**

The store matched `origin/main` at `eeb9648` before the read. Its two files dated 2026-10-22 or later
were both already counted: the PR 100 record is the whole 2026-10-22 window above, and the harden
confirming-pass note was read in the 2026-10-23 passes. No remote branch and no pool slot held a newer
record. `confidential/` was read for names only.

**Correction: citations shifted by the 2026-10-22 window's cherry-pick.** That window was written
on a branch and landed (`eeb9648`) after the four 2026-10-23 sections, ahead of them in date order. Its
164 lines moved every line those sections cite inside themselves, so ten citations there were shifted
by +164 — :5539→:5703, :5552-5565→:5716-5729, :5559→:5723, :5562→:5726, :5584-5587→:5748-5751,
:5587→:5751, :5593-5594→:5757-5758, :5619-5627→:5783-5791, :5645→:5809. Each new target was checked
equal to the old one at `7eb600c`. Citations below :5421 did not move.

**Running parked counts:** unchanged.

## 2026-11-05 (owner-directed: pr-review's same-defect bullet, from the stockmanagement PR 53 miss) — 1 proposed and applied unrevised (P1), 1 parked by the gate (P1′); one pre-registered replay; one refutation round; linter 17 files, 0 findings; **pr-review 0.25.0**

The store matched `origin/main` at `670ee0d7` before the read. One record since 2026-10-26:
`2026-11-04-pr-review-stockmanagement-53-missed-input.md`, a hand-written defect note from an attended
`pr-review` 0.24.0 run. A retro over one record cannot corroborate anything. This pass ran because the
owner directed the change, as `proposals/2026-09-25-owner-directed-prose-prune-wait.md` did, with a
pre-registered measurement as its evidence:
- the measurement note, `2026-11-04-pr-review-same-defect-replay-53.md`;
- the proposal with the gate's reply, `proposals/2026-11-04-owner-directed-pr-review-same-defect.md`;
- the artifacts, `artifacts/2026-11-04-same-defect-replay-53/`.
`confidential/` was read for names only.

**Applied: P1, pr-review 0.25.0.** A bullet in Step 3's *Trace outward*, after *Unchanged neighbors*:
where a PR fixes a kind of defect, look for the same kind on the same path in the code it left alone. An
instance that predates the PR blocks only if it defeats the PR's own guarantee; otherwise it is a
suggestion.
- **Corroborated by:** the owner's direction and the replay, which filed K1 in E 2 of 3 against C 0 of 3.
  It was scored blind and met the reading the protocol committed to. Not by a count: there is one record.
- **Pruned:** nothing, since it subsumes no rule (the gate checked Step 2's "class of input" and "next
  instance" bullets). Net +80 words, with the `pr-review` budget raised 8430 → 8510 and its `why` recorded.
- **The gate's non-blocking objections**, each with what would reopen it:
  - **Thin signal.** With 2 hits in 6 runs, both land in E 3 times in 15. **REOPEN ON:** a held-out
    replay where E does not file more same-kind pre-existing defects than C.
  - **"Blocks only if" is untested on a severe pre-existing instance.** It is in tension with *Don't let
    severity labels substitute for analysis* and with the parked "A demonstrated privilege bypass filed
    below blocking" (2026-10-23). **REOPEN ON:** a run record or measurement where the bullet filed a
    pre-existing exposure on the same path below blocking.
  - **Noise is unmeasured.** E filed 6 findings and C filed 3, on one PR; the gate found none of E's extra
    findings false. **REOPEN ON:** a held-out PR where E's extra findings are noise.
  - **Disposition.** Both E filings were `nit`, where the bullet says suggestion. Recorded; no action.
  - **Mechanism.** The replay changed what reviewers FILE (E 2/3, C 0/3), not what they NOTICE (E 3/3,
    C 2/3). The run that missed never noticed K1, and its conditions were not replayed: max effort, the PR
    conversation, and a second-reviewer subagent with a closed brief.

**Parked by the gate: P1′.** An unmeasured rewording of P1's last sentence ("raised all the same"),
parked on two blocking objections:
- Its premise is not shown. The premise was that wt-03 read the blocking sentence as a reason not to
  raise K1, but wt-03's reason is the one C run wt-02 gave with no bullet.
- An unmeasured rewording is what skill-retro Step 4 and :39 say not to ship.
**REOPEN ON:** a measurement where E runs see a same-kind pre-existing instance and leave it unfiled at a
rate no lower than C's.

**Parked, measurement only, 0 run records: seen, then set aside as outside the ticket.**
- In the replay, 3 of 6 runs found K1 and left it in their notes as pre-existing or out of scope: C wt-02,
  C wt-06 and E wt-03.
- In 556, wt-02 and wt-05 set S1 aside in the same way (`2026-10-18-pr-review-reviewer-brief-556.md`).
- K1 is not a blocker, so this does not meet the REOPEN of the parked "Routing `could_not_verify` to the
  author", which asks for a blocker held in `notes`.
- **REOPEN ON:** a run record where a review left a real non-blocking defect in its notes and the
  maintainer then raised it.

**Isolation.**
- The replay ran headless from per-run clones, with project memory empty (canary checked).
- `gh` was unauthenticated, MCP was off, and the web, subagent, skill and publishing tools were
  disallowed. Nine answer-bearing paths were at mode 000 for the wave.
- Audit: 0 forbidden reads. It was calibrated first: 7 of 7 planted reads flagged, 0 of 7 allowed reads.
- The refuter ran headless from a folder of copies, for the reason the parked "Agent-tool subagents load
  the project's memory index" gives. It read this session's transcript, but only the owner's five typed
  messages, to check the origin claim: no assistant text and no tool output.

**Running parked counts:** unchanged. The one record adds to none of them.

## 2026-11-08 (window: no run record since 2026-11-05) — 0 proposed, 0 applied, 0 killed; no refutation round (nothing to refute); linter 17 files, 0 findings; **no skill file changed**

The store matched `origin/main` at `4ef8b460` before the read, apart from one `__pycache__` bytecode file
under `artifacts/2026-10-07-isolation-guard/`, which is build output and was never mirrored. No file in
the store is newer than `LAST`. No other remote branch carries a `.claude/` commit since 2026-11-04 that
`main` lacks, and the pool's ledger was last written 2026-09-29. `confidential/` was read for its `PRS`
and file names only.

**What ran instead**, read from the transcripts' `Skill` calls since `LAST` was written (2026-11-04
21:18Z). The extractor found this session's own call, and found nothing in a session whose only `Skill`
strings are its system prompt.
- 22 `pr-review` runs, one per session, and 3 `verify` runs.
- No `resolve-ticket`, `pr-harden` or `harden` run.
- `pr-review` has no capture step, and no run was captured by hand. So the 22 runs since the 0.25.0
  bullet shipped left nothing the 2026-11-05 section's REOPEN conditions can be read from.

**Step 6, whether or not a skill changed.**
- Equal, live against pushed: all 27 skill files with `skill-budgets.json`, `CLAUDE.md` and
  `proposing-fixes.md`, the 8 pushed hook files, and the vendored `pipeline/` and `bin/` scripts.
- Equal, live skill against live hook: both gates.
- **Red: two hook files are live only.** One is `kill-session-scratchpad-procs.sh`, a SessionEnd hook
  dated 2026-11-01 and registered in `settings.json`. The other is its `.log`, which
  `.gitignore:22:*.log` keeps out of the repo, so the comparison cannot pass while the hook writes it
  there.
- This was already red at the 2026-11-05 pass, and that section does not record it. That session put the
  hook to the owner, and its two refuters found:
  - it would not have stopped the incident its header cites, because that session never ended;
  - it stops the tasks `/background` carries over to a new session. This was traced in the 2.1.318
    binary, not run.
- Its answer was "No, not as it stands", with two options:
  - push a fixed draft, if sessions are never sent to the background;
  - otherwise, remove the hook.
- No choice is recorded since. Nothing was pushed, and the hook, its log and `settings.json` are
  unchanged.

**Running parked counts:** unchanged.

## 2026-11-15 (window: 2 hand-written `pr-review` defect notes since 2026-11-08, owner-invoked with a `pr-review` focus) — 3 proposed: 1 applied (P3, a prune), 2 parked by the gate (P1a, P1b), 0 killed; one refutation round; linter 17 files, 0 findings; **pr-review 0.25.1**

The store matched `origin/main` at `ea5ee4b2` before the read, apart from the two notes below, a
`.DS_Store`, and the `__pycache__` file the 2026-11-08 section names. `confidential/` was read for its
`PRS` and file names only. The window holds no pipeline run record, only two hand-written defect notes
of attended `pr-review` 0.25.0 runs, each written after the owner asked why the review missed something:
- R-A: `2026-11-11-pr-review-core-6827-noop-dismissed-as-convention.md`
- R-B: `2026-11-14-pr-review-cohortbuilder-781-second-defect-filtered.md`

The proposals and the gate's reply, verbatim, are in `proposals/2026-11-15-pr-review-seen-then-filtered.md`.

**Applied: P3, pr-review 0.25.1.** Deleted :163's last sentence, "Verified-clean cross-cutting
verification does NOT get relocated to the body (see Step 4 — the body carries none); at most it is a
single inline `note (no action needed):`."
- **Corroborated by:** nothing, since it is a Step 4 prune and not a lesson. The gate found both of its
  claims carried at :146 and posting.md:15, and no evidence.md beside `pr-review` to hold a measurement.
- **Budget:** `pr-review` 8510 → 8481, lowered with the prune.

**Parked by the gate: P1a**, "a second consequence stays out of a comment only when the fix the comment
names also resolves it" (:163 and posting.md:13). Blocking objections, each settled by its citation:
- **Bar (a) counted one lesson over two events.** P1a's site has one record, R-B. R-A's filter was the
  convention rule at :92 (R-A:15), and neither edit reaches the class-level shape the two share.
- **Bar (c) is not a fact about the document.** R-B:16 reads the current :163 as already covering the
  rollback through "A second ask gets its own comment if it stands as a finding". That is a misreading,
  not two clauses in conflict.
- **"The fix you name" is undefined when a comment names none.** :163 allows that ("and the fix where
  there is one"), and R-B's posted reply named none.
- **It would probably not have changed R-B's run.** Its final report lists the rollback under the one
  item "fix the history-position race in `runDefinition`" (the transcript R-B names, 10:39:06Z), so "the
  fix" would have read as resolving both. That
  `composeQueries` leaves the rollback standing was measured only later, in session 7d92a8d0 (R-B:8).
- The gate's alternative test needs no later measurement: "a consequence that needs a change on other
  lines than the ones your comment asks about". Its exact wording is in the proposals file.
- **REOPEN ON:** a second run record where a review filtered a verified defect out through :163; or a
  pre-registered replay of the #781 review in which the "other lines" wording files the rollback and the
  current text does not.

**Parked by the gate: P1b**, "prior instances settle a convention claim, never a claim about what a line
does" (:92). Blocking objections:
- One record, R-A, and its premise is the reviewing session's own after-the-fact account (R-A:14, "The
  reasoning in between is not in the transcript").
- It would probably not have changed R-A's run. Its trigger is "a line verified to do nothing", but that
  verification came after the review (R-A:8, :17), and R-A:16's two other pressures toward silence stay.
- Non-blocking: "never" is one of the words skill-retro's Step 4 treats as a defect, and R-A:10's ten
  copies on master, untouched by #5792's 16 inline comments, were not weighed. A wording for a reopen
  drops it: "They settle what the project accepts, not whether a line has an effect: an attribute you
  have verified to be inert is still a take-or-leave nit, whatever master holds."
- **REOPEN ON:** a second run record where prior instances dropped a verified claim about what a line
  does.

**Corrected.** R-B:23 claimed a "third run with this shape", counting #53's replay. #53's run never
noticed its defect (`2026-11-04-pr-review-stockmanagement-53-missed-input.md`:15; this ledger :6025-6026),
and a measurement "has never counted as corroboration" (:5595-5596). The record now says so. R-A:21
makes the same "same shape" claim about #53. It is left as written, and flagged here.

**Parked counts advanced.**
- **"Seen, then set aside" (2026-11-05, :6037-6044): REOPEN met, 0 → 1 run record.** R-B meets it:
  P3/P4 were in the terminal report and not on the PR (R-B:16), and the maintainer then raised the
  rollback. R-A does not, since its final report raised neither attribute (R-A:14). The reopened lesson
  is P1a's, and it stays parked with P1a's REOPEN ON.

**Parked, new, 1 record each (R-B).**
- **O1:** a blocker posted without a fix, when the PR said later PRs build on it and one of them (#782)
  already held the fix. No tool call opened it (R-B:17). No ledger entry matches. **REOPEN ON:** a second
  record.
- **O2:** the coverage mutations never touched the code of the review's own blocking finding (R-B:18).
  Read it beside "(c) The per-condition mutation sentence" (:5633-5637), measured and not adopted; a run
  record does not meet that entry's REOPEN. **REOPEN ON:** a second record.
- **O3:** every check stayed inside the PR's mocks, where a failed lookup followed by a search is the
  specified behaviour (R-B:19). Precursor: #53's lesson, "send absent, null, blank and unknown values …
  through the real entry point" (`…-53-missed-input.md`:21), now :104, which covers only a PR that fixes
  a defect. **REOPEN ON:** a second record on a PR that does not.

**Step 6, before the commit.**
- Equal, live against pushed: all 27 skill files, `skill-budgets.json`, `proposing-fixes.md`, the 8
  pushed hook files, and the vendored `pipeline/` and `bin/` scripts.
- **Red: `CLAUDE.md`.** The live copy carries the owner's 2026-11-13 18:07 edit, which replaces the
  zsh `=`-separator bullet with a bash 5 one. It was made outside this retro and is not this retro's to
  publish, so it is reported here and not committed.
- **Red, as on 2026-11-08:** `kill-session-scratchpad-procs.sh` and its `.log` are live only.

**Running parked counts:** "seen, then set aside" 0 → 1 run record. The others are unchanged.

## 2026-11-18 (owner-directed: pr-review, from the openmrs-core PR 6535 miss; window: 1 hand-written `pr-review` defect note since 2026-11-15) — 3 proposed: 1 applied after revision (P1), 2 parked by the gate (P2, P3), 0 killed; 2 prunes applied (A, B); two refutation rounds, the second over the staged diff; linter 17 files, 0 findings; **pr-review 0.26.0**

The store matched `origin/main` at `cf5b82b9` before the read, apart from a `.DS_Store`. `confidential/` was read for its `PRS` and file names only. There is one record since 2026-11-15: `2026-11-18-pr-review-core-6535-maintainer-changes-requested.md`. It is a hand-written defect note on six attended `pr-review` rounds of openmrs-core PR 6535, written after the owner asked how the review missed a maintainer's changes-requested review. R-B (`2026-11-14-…-781-…`) was counted on 2026-11-15 and was read again only for the REOPEN below.

The owner's instruction, the proposals, both gate replies verbatim, and the checks made after the gate are in `proposals/2026-11-18-owner-directed-pr-review-6535.md`.

**Applied after revision: P1, pr-review 0.26.0.** Step 1's JIRA recipe fetches `labels,fixVersions`, and the MCP route takes "the same `fields`". A `backport` label means an API the change adds lists each maintenance line's version in `@since`, "as openmrs-core's `StorageService` does".
- **Corroborated by:** the owner's direction, on a measured premise (the record's REST read, and 81 multi-version `@since` tags in 48 files at openmrs-core master `a93b6293c9`), and a mechanical edit: the fields print the label. Not by a count. Gate 1 ruled bar (b) not met, because the 2026-11-09 suggestion and the 2026-11-18 miss are one defect seen in two rounds.
- **Revised at gate 1:**
  - The MCP clause no longer claims that getJiraIssue omits `labels`, and it now carries `fixVersions`.
  - The example `3.0.0, 2.9.0, 2.8.9` went: 2.8.9 is released (r4070873955), and an example gets copied verbatim.
  - "`StorageService`'s own `@since`" became "openmrs-core's `StorageService`". Gate 2 found that accurate (`StorageService.java:29`, `@since 2.8.0, 2.7.5, 2.6.16, 2.5.15`) and acceptable.
- **Checked after gate 2, which lacked the access:** `getJiraIssue` with those six `fields` returned TRUNK-6682's labels and an empty fixVersions. Its schema's default field list leaves out `comment`.
- **What it does not fix (gate 1):** the backport intent was already on the PR (r3990860896, r4070873955), and the record's main failure is :20's headline-only check. P1 adds a louder second signal; it does not repair a compliance failure.

**Applied: prunes A and B (Step 4).**
- **A** deleted the old :142, "A "ready to merge" verdict with nothing left to act on is **not** something you post — see Step 5…". It is carried by :174, :184, :134 and posting.md:17.
- **B** deleted the anti-pattern "Don't acknowledge fixes (especially on re-review)". It is carried by :20, :144, and posting.md:13 and :15.
  - It came in with `8fb4b4bd` (pr-review 0.8.0, 2026-06-17). That commit's message names it as the fourth home of a rule it also put in Step 1, the body rule and both gate lines.
  - No measurement stood behind it, so none is retired. The one thing lost is the salience line "This is the gap authors notice most".
- **Budget:** `pr-review` 8481 → 8371 (P1 +28 words, A −39, B −99), lowered in this commit.

**Parked by the gate: P2**, "lines the PR moves or rewrites are its own code; a defect found there is filed even when master has it too" (:104). Blocking objections, each settled by its citation:
- **Bar (a) fails at the site edited:** one record. The class-level count of two, with R-B, is the move the 2026-11-15 gate rejected ("Bar (a) counted one lesson over two events").
- **A false universal.** "Comparing the PR with its base cannot show a defect both carry" is false: the same run's comparison surfaced master's MediaHandler leak (record :18). What hid the null `mimeType` was a brief that asked only for differences (record :23).
- **An uncited exemption.** "What a repaired line was designed to do is not such a defect" cites nothing in the store. It could also exempt the stream, since master's MediaHandler hands an open stream on by design.
- **A mismatched item.** The stream the reviewer saw is not shown to be the one r4617890396 raised (record, corrected).
- **Non-blocking:**
  - P2 widens :104's "blocks only if it defeats the PR's own guarantee" over unclosed resources, against :182. That tension was parked on 2026-11-05.
  - Its noise is unmeasured.
- **REOPEN ON** either of these:
  - A pre-registered, blind-scored replay of the 2026-11-18 round on 6535 head `86b96c7d6f`, in which the bullet files `TextHandler.java:118`'s null `mimeType` and the current text does not, with E's extra findings counted for noise.
  - A second run record where a defect on lines the PR moved, also on base, was left out of the review and then raised by a maintainer.

  A reopen uses gate 1's wording (proposals file, P2), and drops the "repaired line" sentence unless the boundary is cited.

**Parked by the gate: P3**, "a fix the review posts gets the checks the review gave the PR" (:162). Blocking objections:
- **One run record.** The owner's memory notes on earlier rounds (PRs 380, 6801, 6157, REST 778, cohortbuilder 783) are not in the store.
- **No measurement.** The owner-directed precedent ships a rule on a measurement (2026-11-05), and P3 has none.
- **Non-blocking:** "name every input and exception type it changes" doesn't say where. Naming them in the comment would collide with :161's word limit and posting.md:13. It also overlaps :18 and :82.
- **REOPEN ON** either of these:
  - A second run record in the store where a fix the review posted or drafted was broken by a maintainer or a refuter. Each owner-memory round above qualifies if written up from its transcript.
  - A replay where a reviewer given the r4473604840 block plus P3 names `AccessDeniedException`, and one without P3 doesn't.

  A reopen uses gate 1's wording (proposals file, P3).

**Parked counts advanced.**
- **"Seen, then set aside" (2026-11-05): 1 → 2 run records.** Gate 1 reads 6535's order mismatch as meeting the REOPEN: set aside twice as "not realistic" (record :17), then raised by the maintainer (r4617856131). The two records' filters differ (R-B's was :161, the old :163; 6535's was a realism judgment), and no proposed edit reaches both. P1a's REOPEN stays unmet.

**Parked, new, 1 record each (6535).**
- **O1:** `resolve-ticket`:163 and `pr-harden`:262 carry the field list that P1 widened in pr-review. **REOPEN ON:** a run record of either skill where a ticket label the fetch did not print mattered.
- **O2:** the unanchored reviewer's brief asked for "defects this PR introduces relative to master" (record :23). pr-review prescribes no such agent. **REOPEN ON:** a second record.

**Corrected.** The record first claimed two things, and both were corrected after gate 1:
- that a defect present in both trees is "invisible" to a base comparison;
- that the MediaHandler stream is the item meeting the REOPEN.

**Step 6, before the commit.**
- **Equal, live against pushed (`origin/main` `cf5b82b9`):** the 25 other skill files, the pipeline and bin scripts, `proposing-fixes.md`, and the 8 pushed hook files. Live skill against live hook: both gates.
- **Red, as on 2026-11-15:**
  - `CLAUDE.md` carries the owner's 2026-11-13 bash 5 edit. It is not this retro's to publish.
  - `kill-session-scratchpad-procs.sh` and its `.log` are live only.

**Gate sessions.** Both ran `claude-opus-5-5`, headless, from folders of copies, with no project memory and read-only tools.
- Gate 1: `efc908b8-b7bb-41a8-98de-214dee88d48f`, $1.93. Its first reply was refused once by the Stop hook for unreasoned unchecked items.
- Gate 2: `8b5a2a53-dc93-430d-9332-9bec119ba688`, $0.65.

**Running parked counts:** "seen, then set aside" 1 → 2 run records. The others are unchanged.

## 2026-11-18 (addendum, owner-directed: the replay both REOPEN ONs above name) — 2 parked proposals measured, both stay parked (E 0 of 3, C 0 of 3 each); no skill file changed; **Step 5 not run** (nothing to apply)

The owner asked why P2 and P3 needed a retro "when they were real issues that would help us catch what Ian identified". Both REOPEN ONs above named a replay, so this pass ran it, pre-registered.
- **Measurement note:** `2026-11-18-pr-review-6535-replay-p2-p3.md`.
- **Artifacts:** `artifacts/2026-11-18-6535-replay-p2-p3/`.
- **Arms:** C was `pr-review` 0.26.0 as live, and E was C plus gate 1's reopen wording, n=3 per arm per replay.
- **Run conditions:**
  - Headless, at default effort, from per-run clones of frozen full-history repos.
  - The network was blocked through a dead-port proxy, with only the model API exempt (canary checked).
  - 53 answer-bearing paths were at mode 000 for the wave.
  - Audit: 0 forbidden reads. Every flag was read in context.
- **Cost:** $36.48.

**P2 stays parked: E 0 of 3, C 0 of 3.**
- No run filed `TextHandler.java:118`'s null `mimeType`, and no output mentions it anywhere. The first REOPEN ON ran and failed for gate 1's wording.
- **REOPEN ON** either of these:
  - the second-run-record route above, unchanged;
  - a different mechanism with its own replay, in which E files the null `mimeType`.

  A sentence asking reviewers to read moved lines "as new code, such as for a null that throws" made no run notice it.
- **Recorded:** E's runs filed the legacy-layout I/O count as blocking in 2 of 3, against C's 0 of 3. That is a disposition shift the sentence may cause, and it is unmeasured as noise or as value.

**P3 stays parked: E 0 of 3, C 0 of 3.**
- All six runs posted D2 (r4473604840) with `catch (FileSystemException e)` unchanged. The replay REOPEN ran and failed for gate 1's wording.
- Two runs saw the concern and set it aside:
  - wt-02 (C) judged the broadening safe against Greptile's masking thread.
  - wt-05 (E) read that thread, as P3 asks, and counted it "resolved".

  So in this replay the gap was judgment, not a missing check.
- **REOPEN ON** both of these:
  - the second-run-record route above;
  - a wording or mechanism that a replay shows changes the posted block.

**Parked counts advanced (measurement only, not run records).**
- **"Seen, then set aside":** 5 more sightings in this replay.
  - The read-order mismatch appears in the notes of 3 of 6 P2 runs, 2 E and 1 C. None filed it, and the maintainer raised it in r4617856131.
  - The catch's breadth appears outside the findings of 2 of 6 P3 runs.

  The count stays at 2 run records.
- **"Never noticed":** the null `mimeType` and the MediaHandler stream, 0 of 6 P2 runs each.

**Consistent with P1 (0.26.0), not a test of it.** With TRUNK-6682's `backport` label in the snapshot, all 6 P2 runs filed the `@since 3.0.0` question. No arm lacked the label.

**Running parked counts:** unchanged in run records. Measurement sightings for "seen, then set aside" are 3 + 2 + 3 = 8 across the 2026-11-04 and 2026-11-18 replays.

## 2026-11-19 (owner-directed: pr-review lessons read from 722 session transcripts and the project memories; no run-record window) — 13 proposed: P1, P6(a) and prunes A and B applied after revision, P2 turned into this record and the replay, 5 parked, 3 killed; four gate rounds, the last three over the staged diff; linter 17 files, 0 findings; **pr-review 0.28.0**; one measurement started (addendum to follow)

**Evidence read.**
- Every main-session transcript on this machine that launched pr-review: 722, from 2026-09-08 (the oldest
  surviving transcript) to 2026-11-19, in 21 project folders. 187 of them hold messages the owner typed after the
  launch, 425 in all once task notifications and compaction summaries are dropped.
- Calibrated regex counts over those messages, by session: the owner asked why a review missed something in 9,
  for a reply that does not read as AI-generated in 7 (4 after seeing a draft), for a real-server check in 6, and
  whether the PR was the best fix in 7.
- About 20 review lessons that sessions saved to project memories in 8 folders, none of them in this store.
- This ledger. The proposals and gate 1's reply were kept only in session 44d9b58a's scratchpad.

**Applied, pr-review 0.28.0.**
- **P1, Step 5:** "**When the review turns out wrong, record it.** In a session the user is part of, not in CI,
  when another reviewer raises what this or an earlier round should have found, or something a round posted
  proves wrong, append, under its own `# ` header, to `~/.claude/skill-lessons/<date>-pr-review-<repo>-<n>.md` in
  that turn: …; and this session's id. A private PR's record goes under `confidential/`."
  - **Corroborated by:** the owner's direction, and the read above. Misses reached this store only when the owner
    asked for a write-up: #53, #6827, #781, #6535 and #6790, each after 2026-11-04. Not written: esm-core #1946
    (2026-10-19, the session ended on "Want me to draft the two proposals?"), core #6757 (2026-10-26, it ended on
    "Shall I write the capture and run the retro?"), stockmanagement #66 and cohortbuilder #783. The 2026-11-08
    section had already recorded that `pr-review` has no capture step.
  - **Revised at the gates:** "attended" became "a session the user is part of, not in CI", because SKILL.md is
    also the `@claude review` Action's prompt; a transcript path became the session id; a sentence of rationale
    about memory and a "draft sent back" trigger were dropped; "this review" became "this or an earlier round";
    "if any" became "or that none did"; "write" became "append, under its own `# ` header", so a second record on
    the same PR and day does not overwrite the first.
- **P6(a), Step 3:** "run it before posting" became "run it before you show, stage or post the reply", matching
  posting.md:7. Incidents: cohortbuilder #783 (2026-11-17, "I skipped it here because this was a draft for you")
  and appointments #221 (three wrong statements in the evidence behind a draft). Gate 1 asked for records first;
  it was applied on the owner's direction, as the 2026-11-18 P1 was.
- **Prunes:**
  - A: :185's restatement of the label rule, which keeps its homes at :139, :149 and posting.md:13. "Three
    habits" became "Two habits".
  - B: :143's first sentence, which keeps its homes at :129, :140 and :145.
  - No measurement stood behind either.
- **Budget:** `pr-review` 8513 → 8548, with its `why`.

**Parked by gate 1.**
- **P3, "code you post always gets the refutation pass".**
  - It contradicts :127 ("never a default step").
  - In the memory's incidents a refuter was already running before the post.
  - CI's `--allowedTools` has no Agent tool.
  - **REOPEN ON:** the 2026-11-18 P3 conditions, or a record under the new Step 5 paragraph of a posted fix that
    broke with no refuter run.
- **P4, a real-server run before a clean verdict on runtime behaviour.**
  - The four public real-server runs found nothing in the PR under review; one found a master bug instead.
  - The one catch is on a private PR, and the one timed run took 52 minutes.
  - **REOPEN ON:** a public record where a real-server run found what the review's tests missed.
- **P5, running consumers' own tests when a change narrows what the base accepted.**
  - Every incident is on a private PR, and :88 and :106 cover it.
  - **REOPEN ON:** a public record of a consumer break the review read past.
- **P7, any ask on a PR judged ready carries the false-positive bar.**
  - One event, core #6757, was counted twice; #6701's ask was never posted.
  - **REOPEN ON:** a second record.
- **P10, Step 2 names "no change here" and other layers as shapes.**
  - 1 of the 7 "best fix?" challenges found that shape (fhir2 #635, the locale pinned in the pom). Four of the
    seven found something, but the others were a simpler implementation, a coverage gap and a private PR.
  - **REOPEN ON:** a second record where the better fix the review missed was one of those shapes.

**Killed by gate 1.**
- **P6(b), a voice rule:** :165 and :185 already require it, and 3 of the 7 requests were instructions before any
  draft existed.
- **P8, a preference-versus-blocker line in the gate:** wrong place. On webservices.rest #767 the gate already said
  "none"; the preference came in a later answer that no gate line governs.
- **P9, shortening the recited gate:** its premise came from a miscalibrated script, which counted only quoted gate
  lines. All six sampled reports carried all five. No owner message complains of report length, and the edit
  contradicts :169-171.

**Observed, measurement started.** A second reviewer briefed without the review's findings:
- It ran in 54 of 59 openmrs-core-folder sessions from 2026-11-05, against 3 of 136 before, and in 8 of 71
  sessions elsewhere, against 6 of 456 before. It follows a project memory written 2026-11-05, and no owner message
  asked for it. The classifier was a regex over Agent briefs, checked on 5 known second-reviewer briefs and 3
  refuter briefs.
- By the sessions' own statements it found something the review missed in 12 of 71: core #6535 twice, #6597,
  #6605, #6157, #6171 and #6801; webservices.rest #778 and #779; stockmanagement #52; esm-medication-side-effects
  #1; and one private PR. 26 say it found nothing new and 33 don't say.
- The closest ledger shape is the parked union of two reviewers (2026-10-23, 556 arm U). Its pre-registered replay
  runs now: `artifacts/2026-11-19-6535-second-reviewer/`.

**Outside the skill, for the owner.**
- "Address the review comments" means commit and push to the PR's branch: 6 sessions on 2026-10-08/09, and the
  rule lives only in querystore's project memory.
- `event`'s project memory `pr-review-comment-style.md` (June) prescribes the label openers and footer the skill
  now forbids.

**Gate sessions.**
- Gate 1 was an Agent-tool subagent of session 44d9b58a.
- Gates 2 to 4 ran `claude-opus-5-5` headless from folders of copies, with no project memory and write tools
  disallowed: `f317a1a6-6cfa-43c0-8794-fdd700a0a80e` ($0.46), `d4f80da8-1d90-4918-a8a1-7bcadd4fcef0` ($0.36) and
  `2e251b46-86e2-46f8-a7ed-f1ab2bfda231` ($0.34). The fourth found nothing still wrong.

**Running parked counts:** unchanged in run records.

## 2026-11-19 (addendum, owner-directed: the second-reviewer replay the section above started) — 1 parked candidate measured, stays parked (U 0 of 3, C 0 of 3); no skill file changed; **Step 5 not run** (nothing to apply)

- **Measurement note:** `2026-11-19-pr-review-6535-second-reviewer-replay.md`.
- **Artifacts:** `artifacts/2026-11-19-6535-second-reviewer/`.
- **Arms:** C was `pr-review` 0.27.0 as live. U was C plus 92 words asking the review to start one fresh agent given
  none of its findings. Agent was allowed in both arms; n=3 each on the 2026-11-18 P2 subject. Cost $17.58 for the
  six runs.

**The second reviewer stays parked: no maintainer key filed by U 0 of 3 or C 0 of 3.**
- **Compliance:** U 3 of 3 started the reviewer, and no C run started a subagent.
- **What it changed:** K-order was noticed by U 3 of 3 and C 0 of 3, two of the three through the second reviewer
  (one with a probe), and set aside in all three as unrealistic.
- **Its two extra filed findings,** judged blind: one valid (a missing test for the new `APIException` paths) and one
  noise.
- **Cost:** about 76% more per review, and 47% more wall time.
- **REOPEN ON:** a pre-registered measurement where U files a key, or a known defect, that C does not, at 2 of 3
  against 0-1. Or a run record under 0.28.0's Step 5 paragraph where a second reviewer's finding was filed and was
  what the maintainer raised.

**Parked counts advanced (measurement only).**
- **"Seen, then set aside":** 3 more sightings, the K-order dismissals above, so 11 measurement sightings across the
  2026-11-04, 2026-11-18 and 2026-11-19 replays. Run records stay at 2.
- Unlike the 2026-11-04 and 2026-11-18 replays, which disallowed subagents, the dropped item here came from the
  review's own second agent. Detection improved and filing did not, so the next candidate is the filing judgment (the
  parked "seen, then set aside" entry of 2026-11-05, and P1a of 2026-11-15), not more reviewers.

**Running parked counts:** unchanged in run records.

## 2026-11-19 (second owner-directed pass: what other reviewers raised after these reviews, and the per-PR memory notes) — 8 applied (6 rules, a wording repair, 3 anti-pattern prunes as one), weaker lessons left out on purpose; seven gate rounds over the staged diff, the last finding nothing; linter 17 files, 0 findings; **pr-review 0.29.0**

The owner asked "shouldn't we just directly add these improvements to the skill?". So this pass applied directly,
gated by fresh agents on the exact diff, and did not write the run records first.

**Evidence read.**
- **Outcomes on GitHub.** For the 250 PRs the 722 sessions reviewed, 36 got new top-level inline comments or
  changes-requested reviews from other humans after this skill's first review: 105 items, fetched read-only.
  - Two fresh agents judged them against everything posted under the owner's account before each item: 31 missed
    defects, 57 missed quality points, 6 already raised, 7 declined, 3 not findings, 1 unclear.
  - 13 of the 88 misses sat in code or text the review had suggested, on 11 PRs. Two were re-checked by hand
    against the posted suggestion blocks (billing PR 891, authentication PR 59).
- **Per-PR memory notes.** Six readers went through 172 of them across the project folders. 15 lessons rest on two or
  more PRs, and none was a rule. The largest, "check what you suggest", rests on 36 PRs: 29 posted then found broken
  or untested, 7 caught by a refuter before posting.
- **Whether a refuter saw the suggestion.** Transcripts of 26 posting rounds on 10 of those 11 PRs: a refuter ran
  before the post in 11, and at most one was given the suggested code. That one, on core PR 6535, is recorded in its
  note as refuting the findings, not the code.
- **The 2026-11-18 replay** that left P3 parked had disallowed subagents, so it could not test a refuter step.

**Applied, pr-review 0.29.0:**
1. **Step 1.** When the MCP reads a ticket whose REST URL 404s and PRS has no key for the PR, the review appends
   `<url> <key>` to PRS. Three private PRs were missing from it. They were added to the local list on 2026-11-19, and
   none is named in the public repo, which was searched with a positive control.
2. **Step 1.** Before replying in a thread, check `isResolved` and `isOutdated`. A resolved or outdated thread gets a
   fresh comment on the head lines, or the review body where those lines are gone, linking that thread. :21's
   surviving-sub-point reply follows the same rule. Six PRs.
3. **posting.md.** The pre-post re-read takes `headRefOid`, `state` and title, and each replied-to thread's state.
   - Merged or closed: stop.
   - Head moved: re-check and re-anchor.
   - Thread now resolved or outdated: fresh comment.
   - Title changed: re-read the ticket.
   Posting waits for the last check started for the round. Seven PRs' notes; four sessions edited a posted comment
   once their own still-running refuter or suites reported.
4. **Step 3.** Code posted for the author to commit (a suggestion block, or a fix in a code block) goes to a fresh agent
   with that exact code and the PR head before it is shown, staged or posted, where one can be started. The old
   "never a default step" is scoped to findings.
5. **Step 3.** For the test the review names or suggests, undo the guarded line while keeping the code compiling,
   confirm the test fails on its assertion, then restore it. Nine test-design misses.
6. **Step 3.** Read each added line for what it does. A no-op is a one-line nit; master's copies settle what the
   project accepts, not whether the line does anything. Nine misses. This carries the substance of the parked P1b.
7. **Step 3.** Build and test each ref in its own worktree, with a scratch Maven repository holding no local installs
   (`-U`, and `-am` beside `-pl`) and a scratch data store. Eight PRs.
   - A first wording chained `~/.m2` as a tail. Gate round 3 read Maven's resolver source: local metadata in the tail
     still lets a newer local install win.
   - That claim was not run, so the rule avoids the tail rather than asserting how it behaves.
8. **Prunes.** Three anti-patterns that restated other rules were cut: "Don't invent concerns to look thorough"
   (:118), "Don't post unverified suspicion as an `issue`" (:94, :122, :166) and "Don't mistake a verified diff for a
   solved problem…" (:41, :47, :60-66). None carried a measurement.
- **Budgets:** `pr-review` 8548 → 8725 and `pr-review/posting.md` 2072 → 2185, with their whys.

**Left out, with the reason.** Each needs a sharper mechanism or more than its evidence shows:
- concurrent sessions reviewing one PR or stack (4 PRs);
- running tests in CI's order (3);
- recording each round's sha and what was weighed (6; rule 2's capture covers misses only);
- privilege probes on a running server (5; related techniques were measured and not adopted, 2026-10-23);
- claims about CI, deploys or the merge (5);
- the safety classifier stopping security probes (3);
- error-handling breadth (8 outcome misses; the existing silent-failure rule and rule 4 cover the suggested-code cases);
- missed call sites (4; 0.25.0's same-defect bullet exists, and the gap is compliance);
- docs PRs' factual errors (2 PRs).

**Gate sessions**, each `claude-opus-5-5` headless from a folder of copies, with no project memory and write tools
disallowed: f81ad2ab ($0.86), 675435ab ($0.72), df65e824 ($0.93), d9d1678c ($0.83), 1be63c89 ($0.81), da77eb8b ($0.75)
and 857b5b65 ($0.67). Round seven found nothing still wrong.

**Running parked counts:** the 2026-11-18 P3 and the first 2026-11-19 pass's P3 are superseded by rule 4. P1b's
substance is in rule 6.

## 2026-11-19 (third owner-directed pass: the axes the first two did not read — what happened to the comments this skill posted, bot overlap, self-corrections, cost) — 2 applied (a correction to 0.29.0, one new rule), 1 left to the owner, 1 measurement of mine retracted; four gate rounds over the staged diff, the last finding nothing; linter 17 files, 0 findings; **pr-review 0.29.1**

**What the comments posted under the owner's account became** (1,222 top-level comments on other people's PRs among
the 250):
- **Mechanical first pass:** 43% agreed or applied in a reply, 26% no reply but the code under them changed, 1.1%
  declined in a reply.
- **Retracted:** that pass also said 5.5% got no reply with their line unchanged at merge, and the rate fell from 17% to
  10% after 2026-10-17. That signal overstated ignoring about fourfold. A fresh judge read the final diffs and found
  52 of the 67 "unchanged at merge" acted on elsewhere in the PR. Of all 80 flagged comments (those 67 and the 13
  "declined"), 18 really went unheeded.
- **What landed:** authors acted on 42 of 48 test, defect and docs findings.
- **Least effective kinds:**
  - scope-split, unrelated-hunk and "nothing to change" notes: 8 of the 18, and 5 of those 8 not worth the author's
    time;
  - scope/follow-up comments: unheeded 5 times in 12;
  - comments posted after a merge: 4 of 5 went nowhere. 0.29.0's posting re-read already stops a post to a merged PR.

**Bots.** On 132 of the 250 PRs Greptile also reviewed. Of our comments within 10 lines of an earlier Greptile comment
on the same file, 34 of 43 made a different point and 7 built on it. 2 repeated it, both while its thread was still
open. No change.

**Self-corrections in the transcripts:** 19 of 722 sessions, by a calibrated phrase search (3 of 3 known cases found).
- Most were claims about framework or code behaviour, several of them caught by a refuter or second agent before
  posting.
- One was an answer to the owner from a fetch 19 hours old (core PR 6535). The openmrs-core memory note on it says
  posting's re-fetch rule "applies to every answer about a review's state too".

**Applied, pr-review 0.29.1:**
1. **Step 3, a correction to 0.29.0's isolation bullet.** Where `~/.m2/repository` and the scratch repository share an
   APFS volume, the scratch is seeded from a clone of it with each directory holding a `maven-metadata-local.xml`
   deleted; elsewhere it starts empty.
   - The memory note on CI-faithful builds records an empty scratch repository's first build at 25-30 minutes.
   - Measured 2026-11-19: the clone of a 29 GB `~/.m2` took 17-18 s, deleting its 992 local-install directories 2 s,
     and free disk did not change. `~/.m2` kept its 992 local installs. An openmrs-core `-pl api -am -U compile` on
     the seed took 23 s.
   - The gates removed an unchecked claim about GNU `cp -c` and added the same-volume condition: `cp -c` falls back to
     a full copy when cloning is not possible.
2. **Step 5.** Before answering the user about a PR's state, re-run Step 1's `gh pr view` and conversation fetches in
   that turn.
   - The second incident in that memory note, a status answer that left out decisions another session had recorded
     in a local note, is not reached by this rule, and the budget `why` does not claim it.
- **Budget:** `pr-review` 8725 → 8806, with its why.

**Left to the owner.** Scope-split and unrelated-hunk notes are the least effective comment kind here (5 of 12 unheeded;
one author: "This is a major nitpick :)"). The rule that asks for them came from the owner's own feedback
(0.15.0). Whether to keep, soften or move them, for example into one PR-level note, is the owner's call.

**Gate sessions**, each `claude-opus-5-5` headless from a folder of copies, write tools disallowed: ce3f8347 ($0.42),
d036eee8 ($0.26), 4e7d06f2 ($0.26) and c48c8c30 ($0.36). The fourth found nothing still wrong.

**Running parked counts:** unchanged in run records.

## 2026-11-19 (fourth owner-directed pass: questions not yet asked of data already read) — 1 applied (a per-PR round record), 3 checks that found nothing to change; three gate rounds over the staged diff, the last finding nothing; linter 17 files, 0 findings; **pr-review 0.30.0, skill-retro 0.7.3**

The owner asked the same question a third time. So this pass named what would show the history exhausted and checked
it.

**Checks that found nothing to change:**
- **Our own later rounds on an unchanged head.**
  - A first count found 106 top-level comments posted in a later round of ours on a head an earlier round of ours had
    reviewed. A sample showed most were June–August rounds, or single rounds posted as many one-comment reviews within
    a minute.
  - Counting gaps of an hour or more since 2026-09-08: 7 of 1,122 comments, on 5 PRs. One was a real first-pass miss,
    a packaging blocker on esm-medication-side-effects PR 1 found the next day. No rule follows from 7.
- **PR size.** Median lines changed was 462 where others raised points after us and 412 elsewhere. Of PRs with 1,000+
  lines, 17% had such points, against 14% overall. No size rule.
- **The `@claude review` Action's own comments:** 27 top-level comments on 9 PRs, July–August, classified only
  mechanically by reply wording. The previous pass showed that kind of signal is not trustworthy, so nothing is drawn
  from them.

**Applied, pr-review 0.30.0 and skill-retro 0.7.3: one round record per PR.** It lives at
`~/.claude/pr-review-rounds/<owner>__<repo>__<n>.md`, is local, and stays out of the repo.
- **Rules:**
  - Step 1 reads it.
  - A status answer re-reads it.
  - Each round, a clean one included, appends to it outside CI with `cat >>` rather than Edit or Write. The entry
    goes under a `# <date> <head sha>` header and holds what was posted and what was weighed and set aside and why.
  - The user's decisions are appended as their own `decision` entries in the turn they decide.
- **Evidence.** Of 62 PRs reviewed from two or more project folders (170 were reviewed in 2+ sessions), 33 kept
  separate memory notes in each folder and 25 had a note some rounds could not see. A project memory loads in the folder
  that wrote it. The matcher was checked on a PR known to have notes in two folders (stockmanagement PR 51).
- **Cost.** Per-PR notes record 6 PRs where re-deriving a clean round cost 1-4 hours. One status answer omitted
  decisions another session had recorded in such a note.
- **skill-retro Step 6** now names the directory as kept out of the repo. The governance rule says a new directory
  under `~/.claude/` no clause covers is an unsynced copy.
- **Revised at the gates:**
  - the status answer re-reads the record;
  - `cat >>` replaced Edit or Write, because two sessions can write one PR's record;
  - decisions are recorded in the turn they are made, with their own header, and an unsubmitted staged review is
    "pending";
  - a clean round writes its entry too.
- **Budgets:** `pr-review` 8806 → 8987 and `skill-retro` 2831 → 2848, with their whys.

**Gate sessions**, each `claude-opus-5-5` headless from a folder of copies, write tools disallowed: 08816a35 ($0.33),
1ee7b77a ($0.32) and 9172ce1a ($0.35). The third found nothing still wrong.

**Running parked counts:** unchanged in run records.

## 2026-11-19 (fifth owner-directed pass: the prompt history older than the transcripts) — 1 applied (the owner's measured voice, read by every session), 1 pending decision settled by the owner's own words; three gate rounds over the staged diff, the last finding nothing; linter 17 files, 0 findings; **pr-review 0.31.0, skill-retro 0.7.4**

The fourth pass called the history exhausted, and that was wrong. `~/.claude/history.jsonl` keeps the owner's typed
prompts from 2026-03-09, and 553 sessions with a `/pr-review` prompt started before the oldest surviving transcript
(2026-09-08). After each launch they hold 539 non-routine typed messages. Read in full on 2026-11-19.

- **Most early corrections became rules at the time.** The owner asked "update the skill" after nearly each one, and the
  June–August commits match: the body rules, the label and em-dash bans, absence claims, `--stage`, the comment-commit
  check, verified thread replies, best fix, unrelated hunks, and SNAPSHOT conventions.
- **The request for replies that do not read as AI-generated spans the whole history.** It was made on 2026-07-20, 07-21
  and 07-29, and seven times in 2026-11: ten in all, for replies under the owner's name.
  - Inline comments, held by concrete rules, comply in 99% of cases. Replies had only the general tone rules.
  - The measured profile of the owner's writing (716 comments, 2026-11-18) loaded in one project folder. The
    openmrs-core memory contradicted it on contractions: a rater's "use contractions" against "do not" 15 to "don't" 1
    measured.
  - The two drafts written from the profile were posted unchanged.
- **Applied, pr-review 0.31.0.** Step 4: where `~/.claude/pr-review-voice.md` exists, each comment and reply posted under
  the owner's name is drafted in that voice from the first draft, "within the rules above". The file is local and was
  created from the measured profile.
- **Applied, skill-retro 0.7.4.** Step 6 names the file, beside the round records, as kept out of the repo.
- **Revised at the gates:** the skill's own rules win where the voice file differs (a colon opener that diagnoses, the
  two-sentence reply allowance); the file's tells were reworded to agree; and skill-retro says pr-review reads the file
  rather than keeps it.
- **Settled.** The scope-split rule stays. On 2026-08-17 the owner wrote "I do not want any changes that are not related
  to the ticket", which outweighs the low rate at which authors heed those notes.
- **Gate sessions**, each `claude-opus-5-5` headless from a folder of copies, write tools disallowed: f09274b9 ($0.39),
  ccec55e5 ($0.39) and 849f615a ($0.35). The third found nothing still wrong.
- **Budgets:** `pr-review` 8987 → 9022 and `skill-retro` 2848 → 2856, with their whys.

## 2026-11-19 (sixth owner-directed pass: two items earlier passes set aside without opening) — 1 applied, 1 recorded as a watch item; two gate rounds over the staged diff, the second finding nothing; linter 17 files, 0 findings; **pr-review 0.32.0**

- **Applied, pr-review 0.32.0.** A Step 3 bullet: a claim about what CI, a deploy or the merge does (which checks ran,
  what is published and when, what the squash commit message holds) is read off the runs (`gh pr checks <n>`,
  `gh run list`), the published artifact or the repository's merge settings, not inferred from a workflow file's triggers
  or from how another repository behaves.
  - **Evidence:** four wrong claims of that kind in per-PR memory notes, read in context on 2026-11-19.
    - core PR 6590: a posted comment said SNAPSHOTs publish on every master push. They publish after green builds.
    - fhir2 PR 606: a posted review body said a stale PR description becomes the squash commit. That repository squashes
      from commit messages.
    - A merge assumed a build plan deploys a SNAPSHOT. It did not.
    - A subagent read a `pull_request: branches` filter and reported no CI had run on stacked PRs. CI ran on every push.
  - **Coverage:** the rule reaches the two posted claims and the stacked-PR case. The merge-time assumption is outside a
    review.
  - **Revised at the gate:** `gh pr checks`, and the note that the squash-setting query returns null with exit 0
    without push access. The gate ran it: `COMMIT_MESSAGES` on fhir2 and core with admin, `null` on `cli/cli` read-only.
- **Watch item, not acted on.** The `@claude review` Action posted 5 suggestion blocks among 27 top-level comments in
  2026-07/08. Authors took 2 and none is recorded as broken. CI cannot start the refuter that 0.29.0 requires for posted
  code, and 0.28.0's capture skips CI, so a broken CI suggestion would leave no record. **REOPEN ON:** an Action-posted
  suggestion found broken, or a harvest of the Action's outcomes from GitHub showing one.
- **Gate sessions**, `claude-opus-5-5` headless from folders of copies, write tools disallowed: ebfa555d ($0.27) and
  e438931b ($0.25).
- **Budget:** `pr-review` 9022 → 9090, with its why.

## 2026-11-19 (seventh owner-directed pass: the rest of what earlier passes set aside, read at its source) — 3 applied, 10 checked and left with their reasons; two gate rounds over the staged diff, the second finding nothing; linter 17 files, 0 findings; **pr-review 0.33.0**

The second pass set nine items aside under one shared reason ("needs a sharper mechanism or more than its evidence
shows"), not one reason and REOPEN ON per item. The sixth pass opened two of them and found a rule. This pass read every
remaining item at its anchor in the per-PR notes or the outcome judges' reports, together with the harvest's lessons
resting on two PRs.

**Applied, pr-review 0.33.0:**
1. **Owed items, Steps 1 and 5.** The round record also holds what is still owed: a check that did not finish (a refuter
   stopped by a usage limit, for example), a change not yet applied, or a question the user has not answered. Before its
   verdict, Step 1 runs or applies each owed item, or says it is still owed.
   - **Evidence.** A search found owed items in 18 per-PR note files; the esm-stock PR 733 note was the positive control.
     - Refuters were stopped by the weekly limit on esm-stock PR 733 and on a private PR.
     - On another private PR, a clean verdict was challenged after an owed item listed in its note had never been applied.
     - 0.30.0's round record had no place for any of these.
   - **Revised at the gate:** added "a change not yet applied" and "run or apply", because the unapplied item was a
     change, not a check.
2. **Failure paths, Step 3's dimension list.** For each catch, throw or fallback the PR adds or moves, ask three questions:
   what else it catches, whether an earlier step throws before it is reached, and what the caller or user then sees.
   - **Evidence.** After our review, other reviewers raised failure-path defects in code the PR added:
     - core PR 6744, what else it catches;
     - core PR 6605, whether it is reached;
     - core PR 6544 and cohortbuilder PR 781, what the caller sees.
     - The gate confirmed each with `gh pr diff`.
     - Two more were in catches the review itself suggested (core PR 6535 and a private PR). Those fall under 0.29.0's
       refuter for posted code.
   - **Counter-measure.** 422 of 1,122 top-level inline comments since 2026-09-08 (38%) mention a catch, throw, exception
     or fallback. The regex was checked on 2 known hits and 2 known misses. Reviews already look at failure paths; what
     they missed were these three questions.
3. **Trade-offs, Step 4's recommended-default bullet.** A fix that takes away something people using the product had is
   a trade-off. Say what it loses, and put it to the user with the pick before posting. In CI, post it as a question to
   the author.
   - **Evidence.** On esm-stock PR 734, a posted fix hid requesters' own stock figure. Its note says the trade-off "was
     decided by me, not put to the user as a choice". The author applied the fix verbatim, then changed it a round later
     when the user chose to ask the author.
     - proposing-fixes.md's "undecidable because X" was in force (in the repo since 2026-11-02) and did not fire.
   - **Not reached:** cohortbuilder PR 783. There the pick rested on one column of data and took nothing away; a refuter
     caught it. The `why` does not cite it.

**Checked, no change:**
- **Privilege claims on a running server (5 PRs).** The verification already required caught each wrong premise before
  posting. The premises were a roles CSV read instead of runtime grants, and a probe user missing a prerequisite
  privilege.
- **Concurrent sessions.**
  - 5 of 257 PRs had a second pr-review launch within 3 hours, before the first session ended. Core PR 6590, the known
    case, is among them.
  - Its duplicate stopped at the pre-post re-fetch, so the cost was compute.
  - One duplicate question landed on a sibling PR of a stack (chartsearchai PR 621).
  - A stale shared install and a shared dev database are now covered by 0.29.0's scratch stores.
- **Run order (3 PRs).**
  - One blocker the review itself found in a Linux container.
  - One suggested test the author made order-safe.
  - One note.
  - Since 0.32.0, a claim about CI's result is read off the runs.
- **The safety classifier stopping security probes (3 PRs).** Each session said what stayed unverified. No harm is on
  record.
- **Refuter briefs.**
  - The incidents:
    - On a private PR, a brief's "known and accepted" list kept five refuters off a regression a maintainer later raised.
    - On medicationsideeffects PR 1, a brief paraphrased an earlier comment wrongly.
  - A regex for a known-or-skip list matches 138 of 569 briefs. It matches all 7 briefs holding "known and accepted" and
    misses a billing refuter brief that has no such list.
  - None of 10 sampled matches showed the failure: they re-ran base or listed only what the user had decided.
  - **REOPEN ON:** a second record of a brief's label suppressing a finding.
- **Usage limits stopping refuters (2 PRs).** Folded into item 1.
- **Tests that pin file contents (2 PRs).** Step 3's "runtime effect, not a proxy" and 0.29.0's refuter for posted code
  reach both.
- **Residue on shared rigs (3 PRs).** Probe users, roles and stock rows were left on local replicas, each recorded in its
  note. No harm is on record. **REOPEN ON:** a measurement skewed by such residue.
- **Missed call sites (4).** Three predate 0.25.0's same-defect bullet (2026-11-05). One, webservices.rest PR 779, came
  after it.
- **Docs factual errors (2 PRs).**
  - One was in text the review suggested, which 0.29.0's refuter now covers.
  - The other is not a clean miss: our review started two minutes before the maintainer's nine comments and posted an
    hour after them.

**Gate sessions.** Both ran `claude-opus-5-5` headless from folders of copies, with write tools disallowed: 9fd23d02
($0.40) and 99f66069 ($0.22). The second found nothing still wrong. It wrote one scratch file in /tmp and deleted it.

**Budget:** `pr-review` 9090 → 9226, with its why.

**Running parked counts:** unchanged in run records.

## 2026-11-19 (eighth owner-directed pass: the owner's typed messages after each launch, read in full, and the session signals no pass had read) — 2 applied, 1 refuted by its own re-measurement, 9 checked; two gate rounds over the staged diff, the second finding nothing; linter 17 files, 0 findings; **pr-review 0.34.0, skill-retro 0.8.0**

The first pass regex-counted the 425 messages the owner typed after a launch, across the 722 transcripts, and never read
them. This pass read the 303 non-routine ones in full. It also read the session signals no pass had looked at.

**Applied:**
1. **pr-review 0.34.0, Step 5.** Outside CI, "address the review comments" means:
   - fixing every open review comment, a bot's included, or telling the user why one stays;
   - building and testing;
   - committing and pushing to the PR's head branch.

   Replies, resolving threads and description edits still need their own ask.
   - **Evidence:**
     - On o3forms PR 12 a session stopped at local changes: "you'd asked me to address the comments, not to commit or
       push". The owner replied: "by address i mean commit and push to the pull request whose comments you were
       addressing".
     - Initializer PR 338 took more asks before it pushed.
     - On fhir2 PR 635 a session left Greptile's finding out, as not "the review comment".
     - The rule sat in querystore's project memory, written 2026-10-09. 98 address or commit-and-push requests ran
       across 14 folders, and most of the later ones ran where that memory does not load. From 2026-10-13 the owner
       spelled out "commit and push" in nearly every request.
     - The gate confirmed pr-review was loaded in all three incident sessions before the correction.
   - **The pending owner decision is withdrawn.** The rule goes in pr-review, not `~/.claude/CLAUDE.md`. The memory
     stays, because it also carries "keep unrelated churn out of the commit" and a single-use scope link.
   - **Revised at the gate:**
     - "Outside CI": otherwise a commenter's ask would let the Action push to a contributor's fork.
     - "telling the user why one stays": otherwise "saying why" could be read as a thread reply.
2. **skill-retro 0.8.0, Step 3.** The owner can direct a change in place of the corroboration bar. Step 4's prune
   question and Step 5's refutation still apply, and the report marks the change owner-directed.
   - Made to agree with it: the description, the single-sighting sentence, the single-record anti-pattern and Step 5's
     refuter question. "add this to the skill" is now a trigger.
   - **Evidence:** sessions answered the owner's "add this to the skill" three ways:
     - core PR 6535: a replay first, after the owner asked "Why do these need a retro when they were real issues...?";
     - core PR 6790: a direct edit, followed by the owner asking "why is it that this time you did not advise first
       doing a skill retro?";
     - this session: retro first, until the owner challenged it.
   - **The ledger's own count:** it holds 14 owner-directed passes that applied a change. Four of them state that no
     run record stood behind the change (the gate's count).

**Refuted by its own re-measurement: a merge-readiness line in the report.**
- The owner asked "is it ready to merge?" in 14 sessions.
- A first script said 8 of 14 reports did not state readiness. It had read the message just before the question, and
  in five sessions that message answered "bug fix or new feature?".
- Re-measured on the review report itself (the last assistant message before the first typed message): 14 of 14 open
  with readiness ("#615 is ready to merge…").

**Checked, no change:**
- **Rejected tool calls (7) and interrupts (12)** across the 722 sessions: one-off redirects. What followed each is in
  the typed messages.
- **Hook messages (35):**
  - `/goal` conditions and replay guards;
  - the unchecked-items gate, in 6 sessions, each time making the session run an item or give its reason, as intended;
  - a model-override block in 3 sessions on 2026-11-10. pr-review names no model.
- **Reviews requested without the skill:** none since the first `/pr-review` on 2026-06-12, apart from follow-ups and
  three `/goal` review-and-fix loops on the owner's own PRs, 2026-10-12 to 10-14.
- **Reactions on 1,804 of our comments:** 119 +1 and 29 eyes; no -1 and no confused.
- **29 reviewed PRs closed unmerged:**
  - 19 were closed in one stack recut, and 2 were the owner's own.
  - Core PRs 6355 and 6790 are covered by 0.23.0 and 0.24.0, and by Step 1's replacing-PR bullet.
  - Billing PR 189 closed in favour of the PR our review showed already covered it.
  - **Observed, 1 record:** core PR 6729 was reviewed 18 hours after a direct commit had put its change on 2.8.x. Step
    1's bullet looks only for open PRs. **REOPEN ON:** a second review of a change already on its base.
- **pr-harden's reviewer** loads pr-review and runs its Steps 1 to 3. Of its two extras, one is the merged-result rule
  and the other is a guard attack specific to chartsearchai.
- **"Seen, then set aside", checked in the field:** of the 75 items other reviewers caught after us, the per-PR notes
  record as set aside only the two already known: core PR 6535's streams and a private PR's diagnostics. Run records
  stay at 2.
- **Our judgment of other reviewers' comments:**
  - core PR 6757: the judgment held, with evidence;
  - stockmanagement PR 66: the answer was right;
  - core PR 6535: the answer was about an old review, which is 0.29.1's stale-fetch case.

**Gate sessions:**
- 2fd2a353 ($0.87). Its own Stop hook made it run its unchecked items.
- 75ed29ef ($0.41). It found nothing still wrong.

**Budgets:** `pr-review` 9226 → 9300 and `skill-retro` 2856 → 2911, with their whys.

**Running parked counts:** unchanged in run records.

## 2026-11-19 (ninth owner-directed pass: every review session read in full, every review memory, and a fresh audit of the skill) — 20 changes applied after two gate rounds; machinery outside the repo updated; 7 shapes rejected or parked with reasons; linter 17 files, 0 findings; **pr-review 0.35.0, skill-retro 0.8.1**

The owner asked for "one more final round of very careful checking … even if that means using more tokens". So this pass
read the sources that earlier passes had only searched, and measured where review time goes.

**Evidence read:**
- **Sessions.** The text of all 722 pr-review sessions (2026-09-08 to 2026-11-19), with the owner's typed messages
  and each subagent's brief and report.
  - The first corpus missed background agents' reports. The control, 0 of 401 refuter results matched, exposed it, and
    the builder then read task notifications.
  - 25 headless readers took one date-ordered chunk each, and each reported all its sessions read.
  - Three synthesizers merged them into catalogs: A (303 sessions), B (227) and C (172).
  - Two mergers produced the skill-text list (D1) and the machinery list (D2), with every rule dated against
    querystore's history.
- **Memories.** All 117 review-related project memories, read against the skill.
- **Audit.** A fresh audit of SKILL.md and posting.md found 18 internal defects.
- **Measurements, each calibrated:**
  - Time to first report: median 39 minutes, p90 102.
  - Model time 38%, background waits 16%, and a further 30% mostly waits ended by compaction; foreground tools 16%.
  - 239 of 722 sessions compacted.
  - Context fill: 224M characters, 67% of it code reading.
  - Refuters: 199 of 337 results carry a refuted, weakened or wrong signal.
  - Threads: 46 of 1,122 reached four messages, mostly productive.
  - Reactions: no thumbs-down.
  - The Action: 17 repos, fetching SKILL.md only, about 15 runs since August.
- **Today's run records:** patientqueueing 41 (a probe's data picked its branch) and core 6605 (an A/B whose arms started
  differently).
- **D1's dating.** Almost every "covered, not followed" in the catalogs predates its rule: eleven of the thirteen rules
  involved shipped on 11-19.

**Applied, pr-review 0.35.0:**
1. A blocker checks the base and names the default, version or caller that reaches it. Severity is the refuter
   tallies' most common error.
2. Every fix recommended goes to a refuter: code, prose, or a named alternative. It is checked against sibling inputs and
   supported versions. 139 instances.
3. Agents:
   - they start as soon as what they check exists, with a budget written in their brief;
   - they stop at 30 minutes, after which the finding ships as a question or is held, and the agent is recorded as owed;
   - posting waits for an agent only up to its budget.

   82 waits; one unbounded refuter ran 16 hours.
4. The private check runs first in Step 1.
   - Outside CI, a missing PRS file is a failed check.
   - An authentication, privilege or secrets PR with an unreadable ticket stays private.
   - In CI, such a review states code facts only.

   Of 23 lookup instances, 20 broke the 10-21 rule.
5. Where it runs: dependency, Liquibase and consumed-API changes boot base and head on a scratch server, and run the
   consumers' tests. P4's and P5's REOPEN ON are met:
   - core PR 6462: base 200, PR 500;
   - core PR 6694: legacyui went red after a "can merge as is".
6. A test run or probe needs a nonzero count, production entry, a matched control and its own exit status. 88
   instances. The parked "pipe swallows the exit status" is met.
7. Privilege probes use restricted users and data that takes every branch. 11 instances, plus today's record.
8. Agents get scratch isolation. The parked "residue on shared rigs" is met.
9. CI that never ran a head is said, and its command is run. 18 sessions.
10. Owed items gain applied suggestions, re-tested at the head, and sibling-PR leads, written into that PR's record.
11. The round record is written mid-round, with the scratchpad, rig ports, data directory and running agents.
12. Decision records span repos and closed threads.
13. The pre-post re-fetch covers each open PR in a stack.
14. The formatter check is made real; build-config PRs build on CI's JDK matrix; mutations run one per run.
15. The gate's brackets are filled each round, its line 5 is made truthful, and it names stacked merge order. 47 recitals
    had been left unfilled; 15 said "not posted" after a post.
16. An exploitable defect the base already has goes to the user privately. In CI, the review posts only a neutral
    request for a security review. 4 sessions did this on their own judgment.
17. Addressing the review comments now replies in the threads it addressed and, on the owner's own PR, corrects the
    description lines the push made false. Resolving a thread and other writes still need their own ask.
    - The new fact, which D1 and gate 1 lacked: 0.34.0's "own ask" clause came from a session's project memory. The
      owner's words were "commit and push", and they asked for replies on legacyui PR 277 and fhir2 PR 635.
    - The source memory was aligned.
18. On the owner's own PR, the report offers to apply the findings and push. Findings held back privately are excluded.
    57 loops of the owner asking for the fixes next.
19. A recipes pointer. `~/.claude/pr-review-recipes.md` was written from D2's grounded draft: 9 sections, four facts
    spot-checked against their sources.
20. The auditor's consolidation edits:
    - answering an author's question versus the clean verdict;
    - label tags removed from the skill's own prose;
    - em-dashes removed from examples;
    - the body rules and the clean-verdict rule deduplicated;
    - the two build rules merged;
    - Step 1's garbled sentence fixed;
    - CI scoping;
    - the numbering ban kept.

**Revised at the gates:**
- The exploit rule is narrowed to defects already on the base.
- The own-PR offer excludes findings held back privately.
- Resolving a thread keeps its ask, and description fixes are limited to the owner's own PR.
- The `why` is corrected.
- The numbering ban is restored.
- "within the 60" is kept.

**skill-retro 0.8.1:** Step 6 names `~/.claude/pr-review-recipes.md` as local.

**Machinery outside the repo:**
- **Round records.** 49 open PRs have round records seeded with pointers to their notes: 41 by strict match, and 8 by a
  looser match I verified. A first count read a trailing tab as a note.
- **Memory corrections:**
  - no-stamped-labels: the triage body and the stale override;
  - resolved-thread-followups: a fresh comment instead of reopening;
  - metadataexport merge-fast: stop when the PR has merged;
  - both canonical-source notes: check the version;
  - a private PR's note and its index line;
  - the querystore address-rule note.
- **Local checkouts.** Both local querystore checkouts were fast-forwarded. They had been at 0.24.0 and 0.26.0.

**Rejected or parked:**
- Refuter or second reviewer on by default: parked. The 2026-07-23 revert and the 11-19 replay stand, and :127 with :129
  already covers a solo posted blocker.
- Usage and concurrency caps: belong in the launcher, not the text.
- A per-PR claim: the re-fetch caught every duplicate.
- The unchanged-head short-circuit: re-audits found defects.
- Hunk-rule exemptions: the owner settled this on 11-19.
- A merge-readiness line: refuted, 14 of 14.
- Repeated owner questions: parked. **REOPEN ON:** a run record of an item asked three times.

**REOPEN ON not met:**
- the second reviewer;
- brief labels;
- Action suggestions;
- a change already on its base: d4c4bdb7 was the PR itself, merged;
- "seen, then set aside": run records stay at 2;
- O2;
- the 10-23 bypass;
- P7 and P10.

**Left to the owner:**
- whether to stop posting reviews on their own PRs;
- the CI half of the exploit rule;
- three hooks D2 proposed: an `~/.m2` install guard, a per-PR claim note and a refuter time box;
- the Action prompt's "post nothing" when an author has asked a question.

**Gate sessions:** cbe41864 ($1.30) and e3426fed ($0.60). The second found nothing in the diff, and one memory outside
it, which was corrected.

**Cost of this pass:** about $95. Readers about $68, synthesizers and mergers about $18, the memory reader and auditor
$5, the pilot $3 and the gates $2.

**Budgets:** `pr-review` 9300 → 9756, posting.md 2185 → 2236, `skill-retro` 2911 → 2916.

**Running parked counts:** unchanged in run records.

## 2026-11-19 (owner-directed: a mining mode, so the ninth pass repeats as one command) — 1 applied after nine gate rounds and a live verification run (skill-retro 0.9.0 and `mine/`); 5 shapes rejected with reasons; linter 17 files, 0 findings; **skill-retro 0.9.0**

The owner asked "how can we easily do this again, after running new sessions for like a month?", and answered "ok" to
packaging the ninth pass's mining: the scripts with their checks, the briefs, a driver, a skill-retro section and a
watermark.

**Applied, skill-retro 0.9.0:**
- Step 1, a mining mode. `mine/mine.py` scans the pr-review sessions active since `~/.claude/pr-review-mining/MINED`.
  It builds the corpus, measures, fetches the PRs' outcomes and reads the review memories. Then it runs read-only
  headless agents: readers, outcomes judges, a memory reader, an auditor, synthesizers and two mergers. Their two lists
  go to the owner, and each change the owner directs takes the owner-directed path.
- Step 6 names `~/.claude/pr-review-mining/` as local.
- Seven briefs in `mine/briefs/`.

**Calibration:** a dry run over the ninth pass's window (2026-09-01 to 2026-11-19T09:16:23Z).
- Equal to the hand run: sessions 722, chunks 25, private sessions 30, refuter pairs 337 with 199 signals, context fill
  224M, PRs 250, closed unmerged 29.
- Compactions 266 against 268: two fell after the window's end, and go to the next run.
- Median minutes to the first typed reply 41 against 39: the hand method stopped at 44 sessions' meta notices.

**Self-test:** synthetic transcripts with known answers, a fake `gh`, a local git origin, and stubbed agents driving
`main()` end to end, and `run_agent` against a fake `claude` binary. Every mutation tried in the nine rounds turned
it red: each at its own check, but for round 1's task-notification mutation, caught by the corpus control's abort.

**Gate rounds**, each a fresh read-only agent given the code:
1. 69e6cfcc ($0.85). The agents ran with `--dangerously-skip-permissions`, reading untrusted PR comments while the
   owner's `gh` token was present; they now get Read, Grep and Glob only. Also fixed: the window and resume, the
   window's end, the launch-time cut-off, the login check, a ratio corpus control, private flagging, the rewrite rule,
   `is_error`, timeouts, mergers run over failed inputs, briefs naming absent files, and a committed `__pycache__`.
2. d7f1e19b ($1.49). A real private number in the self-test; dry runs auto-resumed; `#n` unmatched; a 404 stopping
   the run; outcomes unbounded by the window; a budget `why` claiming more than the calibration showed.
3. 97226cb0 ($1.54). An empty window left as an unfinished run, resumed forever; carried PRs losing their earlier
   review time; launches after the window; ledger rejections missed by case and in lists; a `git` rule that can write
   (`--output`); `--no-github` moving the watermark; memories and run records unbounded by the window's end.
4. af87c52a ($1.15). The seed not yet installed; an empty window deleting a whole `--out` folder; "rerun the same
   command" false for re-mines and dry runs; the watermark able to move backwards. Its two nits were fixed too:
   owner-directed sections about other skills reached the mergers, and a PR merged just after the window's end was
   never carried.
5. 268eb5fd ($1.03). Ship, with two minor items, both fixed. A first attempt mined to the microsecond, while the
   window and the watermark store whole seconds, so up to a second was mined twice. Three stops gave no resume hint,
   and one of them, a `gh api user` timeout, ended in a traceback.
6. 46aec020 ($0.60). Ship, with one nit, fixed: a re-mine given fractional times was refused as "another window".
   One helper now cuts a given time to the whole second, where a window is made and where it is compared.
7. 0e21e9b3 ($0.29). Ship, with one lead it could not run: a time with no zone. Run here, it stopped with a
   `TypeError` after writing a window shifted by the machine's zone; a time with no zone is now read as UTC.
8. d8a6d1a1 ($0.26). Ship; nothing new.
9. d052c883 ($0.37). Ship; nothing new.

**Live verification** of the round-8 code, over the sessions since the watermark, given as `--since` so the
watermark stayed: 10 sessions, 7 agents, $8.25, no failures; `MINED` and `carried.json` byte-identical before and
after. The skill-text merger made no shell call and dated rules with 16 Greps of the skill's history; both mergers
read the ledger extract; no report names a private PR. It showed two defects, fixed before round 9:
- both mergers and the auditor read their own stream, brief or error log, written in their working folder. Each now
  goes to `<folder>.log/`, and a real probe agent's Glob of its folder returned only its input;
- "github batches 2" counted PRs, not batch files; the line says "PRs for the judges".

**Rejected:**
- Agents with `--dangerously-skip-permissions`: gate round 1. They read untrusted text while the owner's token is
  present.
- A Bash `git` rule for the skill-text merger: gate round 3, since `git log --output=<file>` writes. The merger greps
  the skill's history, written before it runs.
- Resuming any unfinished run: gate round 2. A dry run or an explicit re-mine would be resumed as the next real run.
- A coverage note instead of carrying open PRs: the first monthly run would not judge maintainers' later comments on
  the 71 PRs open at the ninth pass's window end.
- Dropping the owner-directed clause from the ledger rule: it loses the two pr-review replay addenda's six REOPEN ONs
  (parked 71 → 45). The rule now asks that the section name pr-review somewhere (parked 51).

**Machinery outside the repo:** `~/.claude/pr-review-mining/carried.json`, 71 PRs open at the ninth pass's window end.

**Cost:** gates about $7.58 in all; smoke runs $10.06 and $10.85; the live verification $8.25 and its probe $0.06.

**Budgets:** `skill-retro` 2916 → 3044.

**Running parked counts:** unchanged.

## 2026-11-20 (owner-directed: pr-review lessons from the reviews since the 2026-11-19 mining, and a fresh audit of 0.35.0; no run-record window) — 20 edits applied: 3 new rules, 13 text fixes from 12 of the auditor's 14 findings, and a 4-part privacy fix for a PR with several tickets; thirteen gate rounds over the staged diff, the last finding nothing; 5 shapes rejected and 4 parked with reasons; linter 17 files, 0 findings; **pr-review 0.36.0**

The owner asked "Could there be any other lessons that we should apply to the pr-review skill?" and, shown the
proposal below, answered "apply".

**Evidence read:** the live verification run of `skill-retro mine` (the 2026-11-19 section above), over the 10 sessions
since the watermark: its skill-text merger's ranked list, its auditor's 14 findings on pr-review 0.35.0, and the
2026-11-19 run records (two in `…-patientqueueing-41.md`, one in `…-openmrs-core-6605.md`). That run was given
`--since`, so it left the watermark at 2026-11-19T09:16:23Z; on the owner's ask it was then moved by hand to the
run's end, 2026-11-19T20:55:21Z (addendum below).

**Applied, new rules (Step 3):**
- **Privilege probes one at a time.** A privilege or scope claim, the PR's or one the review suggests: where it names
  several privileges, withhold each in turn with the rest granted; where it says "only when" or "unless", also run with
  that condition false and the privilege it names withheld. Corroboration: the two run records in
  `2026-11-19-pr-review-patientqueueing-41.md`. In round 11, round 9's posted README row still missed a privilege
  that only the no-open-visit branch needs; 0.35.0's "(an open visit and none)" came from that one. In round 12, round
  11's refuter had granted every privilege on a line at once, so the line's conditions were never tested. Both rows were
  posted, so the seventh pass's "the verification already required caught each wrong premise before posting" (:6673)
  no longer holds. The records also meet the 2026-11-18 P3's first REOPEN ON (:6217). P3 itself stays parked: the
  addendum (:6276) also asks for a replay showing that a wording changes the posted block, and none was run.
- **A fix's refuter gets its callers.** Put the `git grep` list of every caller of each method or query the fix
  changes in the brief; outside CI, while it runs, drive each caller a request reaches. Corroboration: three sessions
  (8fda42a9, 9145b9ab, 8820e7ac) each spent a refuter round finding a caller the suggested fix broke, all caught before
  posting. The merger also counts about 207 minutes of refuter rounds across three sessions.
- **Mutate in your own worktree, one you deploy nothing from.** Session 8fda42a9 nearly deployed the jar it had
  mutated (clean head `ffd3199d…`, mutated `e478cbf5…`). The text caused the hazard: the mutation probe and the
  build-and-deploy line both said "your worktree". That is a defect in the document, valid from one instance.

**Applied, the auditor's text fixes** (its numbers):
- **#2:** the rule that every fix you recommend goes to a refuter gets its own paragraph and the bold lead "A fix you
  recommend is different.", out from under the paragraph that calls the pass a conditional escalation. The heading
  `### When an adversarial refutation pass is worth it` is unchanged, since `pr-harden` cites it (:2171).
- **#3:** "say nothing" names its exceptions, so it no longer contradicts "say plainly what you confirmed is safe".
- **#5:** the pending-review check runs outside CI, where there is a user to ask.
- **#6:** the open-PR rule's three conditions read "when all three hold", and the list ends before "A shared hunk
  alone".
- **#7:** the re-review closure rule gets its own paragraph and bold lead.
- **#8, #9, #12:** three rules stated twice are stated once: the no-label rule (−31), no body when only blockers would
  be in it, keeping "one blocker or several" (−13), and the blocking rule in the anti-pattern, which keeps only the
  under-labeling warning (−7).
- **#10:** a result that must be on the record goes in a note doing one of the note rule's two jobs. The old "(no
  action needed)" note was one the note rule forbids.
- **#11:** the sample question loses its em-dash, which the skill's own em-dash rule bans.
- **#13, half:** line 24's "and confirm it silently, by not commenting" goes (−8). Line 48's half stays, since only it
  reconciles the hunk rule with the anti-pattern.
- **#14:** posting.md's thread-reply line points at "Step 1's reply-in-thread rule"; the "Needs action before merge:"
  example says it never goes in the review, and that outside CI it belongs in the report.

**Applied, a privacy fix for a PR with several tickets**, found by gate round 4. The private list holds one PR under two
advisory keys, and the text assumed one ticket:
- nothing posted names any of its tickets, quotes one or links one; posting.md's gate line says the same;
- the append to `PRS` fires when the private check does not list that key for the PR's `url`, so a second key goes in;
- an auth PR "whose ticket, or any one of its tickets, is blank or unreadable stays private". The old condition survives
  word for word, so the edit can only add PRs to the private set.

**Gate rounds**, each a fresh read-only headless agent given the base and new files, the edit list, the Action's
workflow file and the private list, never the argument for the edits:
1. d5b0ec33 ($0.75). 7 of 18 edits needed changes:
   - the privilege rule named no list, and "run each clause false without its privilege" read two ways;
   - the caller rule delayed the refuter, against the same paragraph's "start each agent as soon as…". It assumed an
     HTTP request, and in CI it would have run the PR's code;
   - "a worktree you deploy nothing from" dropped "your own", so the user's checkout qualified;
   - #2's lead said "not conditional" two clauses before "where you can start one", and CI has no agent tool;
   - #4's "its ticket" narrowed "a ticket"; #6's list ran into the next clause; #14's half pointed CI at posting.md,
     which CI never loads; #9 lost the several-blockers case.
2. c5b8d1cb ($0.71). #4's "it" had two antecedents; #9's "none" read as "no blockers" and lost "several"; #14's
   "outside CI" scoped the whole ban; #3 named one of three exceptions.
3. c5ca4132 ($0.88). #4 moved the append to `PRS` two bullets ahead of the MCP read that triggers it. Dropped.
4. c3f782fa ($0.76). Nothing wrong with #4 dropped, but #4's "any of its tickets" had been the set's only fix for a PR
   with several tickets: the privacy fix's first three parts.
5. 67e42cc2 ($0.42). CI's only privacy guard still said "whose ticket": an auth PR citing a public and a private ticket
   reads as public there. The fourth part.
6. 63309d1e ($0.46). The fourth part's first text dropped "blank" and narrowed "ticket" to "key".
7. 2ec36fa2 ($0.40). A key-shaped string that is no ticket (`SHA-512`) answers 404 and made an auth PR private.
8. cd6e69bf ($0.43). "Cites" said nowhere where a citation can be, so a key only in the branch name stayed public in CI.
9. 6826573c ($0.39). A non-closing reference ("Part of #N") counted as no ticket.
10. 18bbb275 ($0.56). An advisory ID was not clearly a ticket.
11. 29aafa83 ($0.47). "Issue reference" counted a reference to a PR, and `gh issue view` reads a PR with exit 0. So
    "Backport of #N" of a private PR became a readable ticket and went public, a regression on the base text.
12. 519b9ed2 ($0.39). Nothing the skill runs shows that a `#N` is a PR. The fourth part went back to the base wording
    plus one "or" case.
13. 2965c0f4 ($0.40). "Nothing is still wrong." It found no PR, locally or in CI, that the new text leaves
    public and the base text makes private.

**Rejected:**
- **Auditor #1, the "run it" rules limited to outside CI.** The Action's prompt (`claude-pr-review.yml`,
  openmrs-contrib-gha-workflows) says "this OVERRIDES the skill wherever they differ" and "never execute the
  repository's own code", and turns a check that cannot run into a question.
- **The merger's open item, whether `--post` leaves the Action posting nothing:** read. The Action's prompt says to
  post "the way the skill's `--post` does".
- **Auditor #4, the private-PR definition moved into the private-check bullet:** gate round 3.
- **Defining what counts as a ticket**, in the wordings of rounds 5 to 11. Each traded a leak for false privacy or
  the reverse, and round 11's let a backport of a private PR go public.
- **Counting only keys of projects the tracker has**, round 7's fix. Measured 2026-11-20 against the tracker's REST API
  with no login: `/project/TRUNK` and `/project/O3` 200, a private project 404, `/project/SHA` 404, `/issue/TRUNK-1`
  200 and `/issue/SHA-512` 404. A private project and a made-up one answer alike, so the rule reopens the leak.

**Parked:**
- **A fix's refuters run in parallel, one per angle.** The largest speed lever in view, unmeasured. REOPEN ON a timed
  review round of both shapes.
- **A private key cited only in commit messages** (round 9's lead). Older than these edits, and no case is known.
  REOPEN ON such a PR.
- **A backport of a private fix that is not an auth change stays public** (round 12). Older than these edits; the
  narrower fix would add to the ticket bullet that a PR referencing a pull request carries that pull request's tickets.
  REOPEN ON such a backport.
- **Key-shaped strings make an auth PR private** (`SHA-512`, `AES-256`, `RFC-6749`), round 13's cost, in the safe
  direction. REOPEN ON a public auth PR a CI review limited to code facts for that reason.

**From the merger's list, parked or left:**
- Parked, one instance each, REOPEN ON a second: testing a suggested test against cross-wiring mutants (3 instances in
  session 9145b9ab, all caught by its refuter); "here only" on a sibling-match suggestion; saying that an expected red
  build is expected; a measured pick for a fix outside the PR (near parked O1 and P10, which need a run record); the
  log-check half of "both arms start the same way"; two outcome misses, openmrs-esm-stock-management-app PR 734's
  batch picker loading 10 batches and openmrs-module-stockmanagement PR 65's README placeholder.
- Left, already covered: a time limit on refuters and the control arm started the same way (0.35.0, after these
  instances); quoting acceptance criteria; verifying a draft reply's claims; probe tests writing to `~/.OpenMRS`; `-am`
  beside `-pl`. One comment carrying three asks breaks a rule in force since 0.19.0, in one session.
- Not skill text: tested snippets for recurring parsers, which the merger points at the recipes file. None was written
  this pass.
- **Needs the owner's decision:** whether "push the fix" also replies in that comment's thread. It would widen Step 5's
  "any other write needs its own ask", which governs replies posted under the owner's name; on
  openmrs-module-webservices.rest PR 780 the owner asked for the reply separately.

**Machinery outside the repo:** `~/.claude/pr-review-recipes.md` gains 8 recipes, and its copy line now names
`posting.md` beside SKILL.md.

**Cost:** gates $7.00.

**Budgets:** `pr-review` 9756 → 9837; `pr-review/posting.md` 2236 → 2226.

**Running parked counts:** unchanged.

## 2026-11-20 (addendum, owner-directed: the live run's machinery list, and its watermark) — 8 memory corrections and 3 recipes applied, all outside the repo; 3 hooks and 3 recipes declined with reasons; the watermark moved by hand to the run's end; no skill file changed; **Step 5 not run** (nothing to apply)

The owner asked "can you move the mining bookmark?" and then "anything else?". Shown the machinery list, they
answered "apply", and then "Push the REJECTED.md commit".

**Watermark.** The live run (2026-11-19T09:16:23Z to 20:55:21Z) finished with both reports, no failures and its GitHub
stage. It left the watermark only because it was given `--since`. `MINED` was set to 2026-11-19T20:55:21Z, and the
run's `carried-next.json` (70 PRs) was copied to `carried.json`: the two writes `mine.py` makes when a complete run
from the watermark ends. A dry run with no agents and no GitHub then started at 20:55:21Z on its own, found 3
sessions, and left both files unchanged. The run's folder is kept at `~/.claude/pr-review-mining/2026-11-19/`, which a
bare `mine` does not resume: it is finished, and was not started from the watermark.

**Applied from the machinery list, outside the repo**, each checked at its source first:
- 8 memory corrections:
  - a note that recommended posting a bare "Looks good to me." now says a clean verdict is never posted (Step 5);
  - a resync command that piped a fetch straight into the installed SKILL.md now fetches both files to scratch and
    copies only on success, tested against `main` and against a missing ref;
  - a third note grounds a review in `posting.md` as well as SKILL.md;
  - a private PR's two notes and its round record point at its latest round, checked on GitHub;
  - the cohortbuilder PR 783 note names the posted draft, v10, which is identical to the posted comment;
  - the restws PR 779 note says core WAR 485 still has joda.
- 3 recipes in `~/.claude/pr-review-recipes.md`: the interdiff after a force-push; a PostgreSQL changeset IT failure
  that is a harness artifact; the head checked before composing, and `commit_id` pinned.

**Declined:**
- **A hook refusing a refuter brief without the 25-minute stop line**, which the ninth pass also left to the owner as
  its refuter time box. Its evidence predates the rule. Session 9145b9ab loaded a skill without the 30-minute limit,
  and its refuters started between 13:53 and 15:36 UTC on 2026-11-19, before 0.35.0 was committed at 17:01 UTC; the
  16-hour refuter (:6836) is older still. The ten reviews measured since 0.35.0 all kept the limit. REOPEN ON a
  refuter brief without the stop line, or a round past 30 minutes, under 0.35.0 or later.
- **A hook refusing a deploy from a worktree with changes under `src/`.** One near miss (8fda42a9), which 0.36.0's
  worktree rule and the checksum recipe answer. REOPEN ON a second, under 0.36.0.
- **A hook checking a review payload's `commit_id` and `state` against the PR.** Its two incidents are from 2026-09-25
  and 2026-10-06. `posting.md` now re-reads the head sha and state just before posting, and the recipe above pins
  `commit_id`. REOPEN ON a review posted against a head it did not verify, under 0.36.0.
- **Three recipes:**
  - "another session's hollow Tomcat" appears in neither source session (8820e7ac, 8f72a176), and the recipes already
    cover another session's server on a port;
  - a test class whose shared `setup()` grants a privilege contradicts its source session (d9ae5118), where the new
    test caught the mutation;
  - the resolved-thread follow-up is already in Step 1.

**Still the owner's:**
- whether "push the fix" also replies in that comment's thread;
- the ninth pass's posting on their own PRs, the CI half of the exploit rule, its `~/.m2` install guard and per-PR
  claim note, and the Action's "post nothing" when an author has asked a question.

**Running parked counts:** unchanged.

## 2026-11-20 (second addendum, owner-directed: the parked parallel refuters, checked against their own evidence) — 1 parked item rejected without its replay; 1 lesson parked; 1 memory note corrected outside the repo; no skill file changed; **Step 5 not run** (nothing to apply)

The owner asked "why Later, no action now?". Offered a replay of session 9145b9ab's three serial refuter rounds as
parallel ones, they answered "yes"; shown why the replay could not measure the lever, they answered "push it".

**Rejected: a fix's refuters run in parallel, one per angle** (parked in the 2026-11-20 section above). Its saving, in
the live run's machinery report, "assumes about one serial round saved per affected session". Neither review it cites
had a round to save:
- fhir2 PR 644 (session 9145b9ab). Each later round ran to check text revised after the round before, so none ran only
  to find what an earlier refuter had missed.
  - Round 2's first finding came from its own revision. It replaced round 1's three no-match tests with four
    match-only tests, and its refuter measured that the no-match tests catch the gap it found: a filter that is never
    applied.
  - Round 3 checked the tests that put them back, with a new comment. Its refuter found a sentence in that comment
    ambiguous, and measured a case-sensitivity dependence round 2's refuter had already reported unmeasured.
  - One smaller miss in round 1's text, an `identifier=<user system>|` search its fix also broke, surfaced in round 2,
    a round the revision needed anyway.
- patientqueueing PR 41 (sessions f7c8e2e1 and c0de4167, and their two run records). One miss was a probe whose data
  took one branch, the other a refuter that granted every privilege at once. The privilege-probe rule now names both:
  data that takes each branch (0.35.0), and each privilege withheld in turn, with each condition false (0.36.0).

The replay was not run and cost nothing: round 1's text did not hold round 2's gap, so parallel refuters on it could
not have saved a round. The three original refuters cost about $33 together ($13.14, $13.68 and $6.47), from their
transcripts' token counts at the per-token prices that reproduce 20 runs' billed costs exactly. REOPEN ON a review that
needed a round only because a refuter missed a defect already in the text it was given, of a kind no content rule
covers.

**Parked, one instance, REOPEN ON a second:** revise a refuted test by adding the missing case, not by swapping its
style. In fhir2 PR 644, round 2 replaced round 1's no-match tests instead of adding match cases beside them, so its
refuter found a gap the replaced tests had closed. For a filter test, the recipes file's "a match and a no-match case
per field" already rules the swap out; the general form waits for a second instance.

**Outside the repo:** the fhir2 review's project memory, whose summary of its three rounds the machinery report quoted
as this item's evidence, now says why it is not: each later round checked revised text, and the swap opened round 2's
gap.

**Running parked counts:** the parallel-refuter item leaves the parked list; "add the missing case" enters at 1.

## 2026-11-22 (owner-directed: the 2026-11-20 usage report and an audit of the sessions behind it, through ten blind refutation rounds) — 11 changes applied; 1 killed; 1 dropped; 3 parked; 1 un-parked on a refuter's evidence; linter 17 files, 0 findings; **pr-review 0.37.0, pr-harden 0.41.2, verify 0.1.1, skill-retro 0.10.0**

Of the usage report `/insights` wrote on 2026-11-20, the owner asked "So you are absolutely sure that there is nothing
in the report that we can use to improve this machinery?", then "It can lead to the improvement of that machinery or
adding more to it", and answered "go" to a list of fourteen changes this skill would take. The first answer, "nothing to
implement", had counted from the report's per-session summaries, which record no challenges, and had skipped
proposing-fixes step 5. Seven audit rounds followed (three Agent-tool subagents, four `claude -p` runs from an empty
folder), each item re-checked against files, transcripts or a rerun. Evidence, local:
`~/.claude/pr-review-mining/2026-11-20-insights-audit/`. Ten refutation rounds ran through `bin/blind-run`, none loading
a memory index: round 1 (71 turns, $4.27) revised ten changes and killed one; round 2 (53 turns, $2.74) found three new
problems in the run guard and three unresolved; round 3 (21 turns, $1.09) found real lists the gate's wording rules let
through, four more run-guard holes, and that verify now pointed at nothing runnable; round 4 (blind like the rest) found
the quote rule passing a real list, guards counted before the lookup they vouch for, and a verify line that gave a green
build evaluating nothing; round 5 (30 turns, $0.94) found the round-4 points resolved, one guard that refused a correct
exit block, and wording in this record. Round 6 (20 turns, $0.59) found a bytecode file in the diff, an exit word inside
an echoed string counted as a guard, and that a loop waiting for a run to appear stays outside the guard. Round 7 (7
turns, $0.35) and round 8 (9 turns, $0.34) found an exit word inside a quoted string still counted, a stale .gitignore
comment, and a figure no saved script reproduced; each is fixed or recorded here. Round 9 (12 turns, $0.45) found the
evidence script's own docstring naming the wrong window, a stale field in its dataset, and a find-loop count of 7 where
the script gives 8. Round 10 (22 turns, $0.54) found nothing new in the changes or this record, only the commit message
naming the linter without its root.

**Applied:**
- `hooks/unchecked-items-gate.py`: a label must name the reply's own open items. Of the gate's 57 reproducible blocks
  from 2026-11-02, when it was added, to 2026-11-20, re-run through its own `labels_with_items`, 25 were not open-item
  lists; it now passes the 2 that name the gate itself or give their one item's reason, and still blocks the 32 real
  lists. New `hooks/unchecked-items-gate-test.py` holds both sets and every refuter counterexample; dropping any rule
  reddens it.
- `hooks/gh-run-wait-guard.py` (new, PreToolUse Bash): refuses a loop polling a run id `gh run list` looked up with no
  empty check after its last lookup and no deadline (a clock compared, or a counter it steps). Three stalls had that
  shape (5eda5cb4 2026-11-05, 98d101a8 2026-11-17, 2723129b 2026-11-20, two hours); of the 40 run-waiting commands from
  2026-09-28 to 2026-11-20 15:00 UTC (`run_waiting_commands.py`) it refuses 21, all three among them. Test beside it.
- `bin/blind-run` (new): a headless agent from an empty folder with its own Maven repository, OpenMRS data directory,
  TMPDIR and deadline. An Agent-tool subagent loads the calling folder's memory index; the audit's three Agent rounds
  each carried chartsearchai's. `bin/owed` (new): the owed and parked items five stores hold, with ages.
- `skills/skill-retro/mine/mine.py`: `memories()` also reads build, probe and tooling memories (REVIEWISH alone skipped
  the `~/.OpenMRS`, `-Dtest` and `git grep -E \b` lessons, each later broken in another folder) and takes
  `--memories-since`; a `challenges` stage gives the machinery merger every owner challenge with the reply's opening.
- pr-review 0.37.0: the literal OpenMRS data-directory flags (two reviews run outside the core folder rolled the live
  `openmrs.log`, 2026-11-18 and 2026-11-20); a Step 1 watch on the PR's state, head and review decision (stockmanagement
  #73 and #72 merged mid-review, this window's records); patientqueueing #41 round 13's refusal-commit and over-grant
  probes and the comment's prose for a fix's refuter (one record, the misses spanning rounds 8 to 13); a mutation
  worktree nothing else reads.
- pr-harden 0.41.2: "starts clean" corrected. The park at "Agent-tool subagents load the project's memory index"
  reopened on its own condition: this audit's refuters carried notes on what they judged.
- verify 0.1.1: the rubric clause named two `/tmp` scripts that exist nowhere; it now gives querystore's
  `RetrievalQualityEvalTest` with the `-Dquerystore.eval.modelDir` it skips without, the `integration` profile its
  siblings need, and that a skip is not a run; it keeps reading the answers on a prompt change.
- skill-retro 0.10.0: Step 5 runs through `bin/blind-run`; the backfill and its cost; the report ends with `bin/owed`
  and restates every decision still the owner's below, which `bin/owed` reads (a decision recorded in prose on
  2026-11-08 appeared in neither of the next two such lists).
- `CLAUDE.md` (four Environment lines: never-started waits, tmp_cleaner, `git grep -E \b`, `-Dtest` wildcards) and
  `proposing-fixes.md` step 5 (a blind agent, through `bin/blind-run`).
- `pipeline/pool-run`: each slot's own OpenMRS data directory. Un-parked by round 1: the module's tests write into
  `<data dir>/chartsearchai` (`DrugReferenceTestSupport.java:187`), so pipeline runs wrote into the owner's live
  `~/.OpenMRS`; the model-reading tests are opt-in and link their model from a `-D` path.

**Killed:** `hooks/sweep-gate.py`, refusing an offer once the owner had said not to stop to ask. Round 1 replayed it
over the 17 sessions whose prompts set its marker: it blocked 56 replies in 10 sessions, 3 of them the offers it was
built from; it would have pushed 29946b14 (2026-10-14) past an ask before a push to the public demo; its marker never
expired. REOPEN ON a second session where an explicit "without asking" was followed by offers, with a design measured to
stay silent before an outward-facing action.

**Dropped:** widening `kill-session-scratchpad-procs.sh` to match by working directory. Its fix-or-remove decision (the
2026-11-05 entry, "No choice is recorded since") is open, the widening grows the defect behind it, and it would stop an
owner's shell `cd`'d into a scratchpad. The live hook was restored from this session's read of it.

**Parked:**
- A Stop gate refusing a reply that names a pick when no refuter ran. Since 2026-11-02, 443 of 705 owner turns that
  ended in a reply ran no Agent and no headless refuter (`refuter_free_turns.py`, 2026-11-22); with no detector for a
  reply that names a pick, it fires on all of them to reach the 20 picks the owner challenged. REOPEN ON a detector that
  separates those 20 from the rest at usable precision; the `challenges` stage reports the rate to every retro.
- Exempting the gate's other 23 misfires: quoted mentions ('The "Not checked" line is right.'), review-checklist
  categories ("Unchecked claims", "4. Unchecked claim: BLOCKING.") and numbered lesson titles. Every wording rule tried
  let a real list through in the same words (rounds 1 to 4: "Unverified claims I made above:", "Unchecked claims
  above:", "1. The JDK 8 build and the demo deploy are not verified.", 'Items I left "unchecked":'). REOPEN ON a rule
  that passes them and blocks every counterexample in `unchecked-items-gate-test.py`, or on the prompts that produce the
  categories ("Does it add a claim nobody has checked?") being reworded.
- Extending the run guard to a loop that waits for `gh run list` to return a run with no deadline: 8 of the 40
  run-waiting commands had one (`run_waiting_commands.py --find-loops`), none measured stalling, and the 2026-11-05
  stall was the unguarded poll after one. REOPEN ON a stall inside such a loop.
- A PreToolUse hook refusing any background wait loop without a deadline: of the 818 background wait loops in all
  transcripts, counted on 2026-11-20, 541 carry no deadline construct, and beb370df's loops all had one and stalled on a
  setup redirect. REOPEN ON a stall the run guard does not cover.

**Counter-objection recorded:** round 1 asked to point verify's clause at `docs/adr.md:1475` rather than delete it; that
is a rubric definition, not something runnable, so the clause names querystore's eval test instead (round 3).

**Process:** the live `CLAUDE.md` and `proposing-fixes.md` were edited before Step 5, which "go" did not authorize
(round 1). Their text survived three rounds unchanged, and the tools they name ship in this commit.

**Observed, not yet actionable:** replays assuming Docker unavailable (6 sessions; the gate caught 5); Maven downloads
without `curl -L` (57; none of the cases read led to a wrong conclusion); answering from a stale primary checkout (1,
0fc77d8a); probes counting HTTP errors as data (2, both caught in-session).

**Still the owner's:**
- `kill-session-scratchpad-procs.sh`: push a fixed draft or remove it (2026-11-05 entry), and with it the
  working-directory match this pass dropped;
- publishing the 2026-11-13 bash 5 paragraph of the user-level `CLAUDE.md`;
- openmrs-core's project memory runs an unanchored second reviewer this file parked (2026-11-19): adopt it in pr-review,
  or drop the memory's line;
- whether "push the fix" also replies in that comment's thread;
- the ninth pass's posting on their own PRs, the CI half of the exploit rule, its `~/.m2` install guard and per-PR claim
  note, and the Action's "post nothing" when an author has asked a question.

**Running parked counts:** the reply-side pick gate enters at 1 (owner-directed, measured); the deadline-on-every-loop
hook enters at 1; the run guard's find-loop extension enters at 1; the gate's category and title exemptions enter at 1;
"add the missing case" stays at 1.

## 2026-11-22 (owner-directed: "run the skill-retro on this replay", the blind replay of pr-review 0.25.1 against 0.37.0 on a private-ticket core PR) — 0 applied; 1 parked; 1 not re-proposed; no skill file changed; linter 17 files, 0 findings; **Step 5 not run** (nothing to apply)

**Evidence read.**
- The store matched `origin/main` both ways before the read: no file missing on either side, none different.
- No top-level record had been written since `LAST` (2026-11-22). The replay's own records are under `confidential/`
  because the subject's ticket is private, and this pass drew nothing from them. Its window is the one capture it wrote,
  `2026-11-22-pr-review-replay-reach-clause.md`, read against the records it bears on: `2026-10-18-pr-review-lens-replay-queue96.md`,
  `2026-10-18-pr-review-heldout-6551.md`, `2026-11-19-pr-review-patientqueueing-41.md`, and this file's 2026-10-23
  (*Measured and not adopted*) and 2026-11-19 (pr-review 0.35.0) sections.

**Parked, 1 measurement record and 0 run records: a real regression filed below blocking because a test harness hid its
reach.**
- 0.37.0 filed the blocker in 1 of 3 runs; 0.25.1 pointed at the same notes filed it in 3 of 3; 0.25.1 alone in 0 of 3.
- Both E downgrades trace to pr-review :172's "a reach you cannot name makes it a question" (0.35.0), reached through
  :101's "can't run ... ships as a question": the FHIR2 path failed inside the test transaction on every core, base
  included, or was not run. The E run that suspended the transaction blocked.
- Candidate wording, untested and not proposed: at :172, a reach counts as named when each link is measured on base and
  head, and a harness whose base control fails the same way decides nothing.
- Why parked: one record; and 0.35.0's clause rests on severity being the refuter tallies' most common error across 722
  sessions (2026-11-19 section, change 1), which one replay cannot outweigh.
- **REOPEN ON:** a run record where a regression was filed below blocking because its reach failed only inside a test
  harness, or a pre-registered measurement of a candidate wording on another PR.

**Not re-proposed: a transaction-semantics remedy (`NOT_SUPPORTED`).**
- The replay is another sighting of the harness theme: only the one E run that suspended the test transaction reached the
  defect through FHIR2.
- Sightings so far: queue96 (0 of 12 runs ran anything able to observe a transaction), 6551 (`NOT_SUPPORTED` in 0 of 3
  runs that had read it), patientqueueing 41 (rounds 9-12 probed inside the transaction and missed a commit), this replay
  (1 of 3 E runs).
- The candidate texts are recorded as measured and not adopted (2026-10-23, (a) and (b)). Their REOPEN ON asks for a
  measurement in which a candidate moves a pre-registered outcome. This replay tested skill versions, not a candidate, so
  it does not meet it.

**Observed, for the next replay's protocol rather than a skill:** a brief that redirects `~/.claude` paths for every arm
hands the treatment arm's inputs to a control whose method never names them; 2 of 3 C runs read the notes that way. Give
the redirect only to the arms whose method names those paths, and count it as a threat otherwise.

**Still the owner's:**
- whether to direct a change to pr-review :172's reach clause now, on this one measurement, instead of waiting for its
  REOPEN ON;
- `kill-session-scratchpad-procs.sh`: push a fixed draft or remove it (2026-11-05 entry), and with it the
  working-directory match a 2026-11-22 pass dropped;
- publishing the 2026-11-13 bash 5 paragraph of the user-level `CLAUDE.md`;
- openmrs-core's project memory runs an unanchored second reviewer this file parked (2026-11-19): adopt it in pr-review,
  or drop the memory's line;
- whether "push the fix" also replies in that comment's thread;
- the ninth pass's posting on their own PRs, the CI half of the exploit rule, its `~/.m2` install guard and per-PR claim
  note, and the Action's "post nothing" when an author has asked a question.

**Running parked counts:** "a regression filed below blocking because a harness hid its reach" enters at 1. The counts
the previous section listed are unchanged, since no run record arrived in this window.

## 2026-11-23 (owner-directed: pr-review does not name `bin/blind-run`, and a replay that measured the parked second reviewer had no public record; the owner answered "yes" to "close gaps 1 and 2 in one `/skill-retro` pass, with a blind refuter, and push it") — 1 applied; 1 parked candidate measured, stays parked; 1 capture killed; linter 17 files, 0 findings; **pr-review 0.38.0**

**Evidence read.**
- The store matched `origin/main` both ways before the read: 751 files, none different; only a `.DS_Store` and a
  `.pyc` cache are local-only, and neither is mirrored.
- No top-level run record had been written since `LAST` (2026-11-22) beyond
  `2026-11-22-pr-review-replay-reach-clause.md`, which the previous section counted.
- The replay this pass reports is summarized below. The session that ran it also corrected one count in its records
  (a key noticed without being filed, or filed only in part, in 5 runs, not 6).

**Applied, owner-directed: pr-review 0.38.0.**
- :132's "a separate reader per finding" and :141's "a fresh agent" now say blind, and :141 ends with what a blind
  agent is outside CI: `~/.claude/bin/blind-run <name> <brief-file> --repo <checkout> --ref pr-<n> --deadline 1800` run
  in the background, not an Agent-tool subagent, which loads the project's memory index where it has one, with any
  notes on this PR. pr-harden :45, skill-retro Step 5 and `proposing-fixes.md` step 5 already say the same.
  `--deadline 1800` carries out the paragraph's own "give it 30 minutes; past that, stop it". The background is
  measured: with the self-test's fake `claude`, killing the wrapper's process group, as the Bash tool's 600 s
  foreground limit does, left the agent running in its own session and wrote no report; left to finish, it wrote one.
- A guard on the owner's direction (Step 3): no pr-review run is on record that it would have changed. Rounds that used
  `blind-run` already started their agents through it; what kept those agents from being blind was the tool's own
  defect, which 98c45ffa fixed. It prunes nothing, since it makes "a fresh agent" precise rather than replacing a rule.
  Budget 10026 to 10072, with its `raises` entry.

**Parked, measured again: the second reviewer (2026-11-19).**
- A pre-registered replay with a blind judge measured the parked 92-word paragraph again: U filed a key in 1 of 3 runs
  and C, the skill without it, in 0 of 3. REOPEN ON asks for 2 of 3 against 0-1, so it stays parked, REOPEN ON
  unchanged.
- The same replay ran a round's second-reviewer brief on its own. On one of its keys it filed in 3 of 3 runs blind and
  0 of 3 with a project memory index loaded, which its pre-registered reading takes as the index suppressing that key;
  on another, the memory-loaded arm did better (1 of 3 against 0 of 3 and one partial). Taking 0.33.0's failure-path
  clause out of the brief gave no evidence either way.

**Fixed outside this pass, recorded here:** `bin/blind-run` at 98c45ffa (owner-directed). `--repo` made a worktree, and
Claude Code loads a worktree's memory index from its main checkout, so those agents were not blind. Each run's first
stream event records the memory folder it was given (`memory_paths`). Read across the 46 runs under
`~/.claude/blind-runs` before this pass's own: 21 round agents, all `--repo` worktrees, were given a folder holding an
index (18 in openmrs-core rounds, 3 in stockmanagement rounds), as was one canary of the old script; none of the 21
opened a note. `bin/blind-run`'s docstring said every `--repo` run had loaded an index; this pass corrects it to runs
on a checkout whose project had one. The ten refutation rounds of the first 2026-11-22 section each recorded their
own empty folder, and the replay of the second ran its arms in per-run clones, so neither is affected. Since the fix the
event carries no memory folder.

**Gate.** Refuters through `bin/blind-run` on the staged diff, a draft capture and drafts of this section:
`2026-11-23-retro-1123-refute1` (50 turns, $2.10), `-refute2` (44 turns, $2.36), `-refute3` (28 turns, $1.76) and
`-refute4` (24 turns, $1.52).
- **Killed: a capture of the replay,** on rounds 1 and 2's question 6; the results above stand in for it.
- Taken from round 1: two false sentences of the draft section (every `--repo` run loading the index; this pass drawing
  nothing from the replay's records), and P1's citation of a measurement of the second reviewer, a role P1 does not
  touch, so P1 is now the guard above.
- Taken from round 2: `<brief-file>` and the background in P1's command; the counts above (21 round agents and one
  canary; 46 runs with a stream).
- Round 2 also judged P1's owner bar weak, the gaps being named in the session: the owner was shown "The pr-review
  skill never names blind-run" and that no public record of this replay existed, then answered "yes".
- Taken from round 3: question 6 again, by dropping four phrases of the draft; the background claim, measured above
  rather than read from the code; and "where it has one" in P1. Round 3 also found that the reason the owner was given
  for gap 1 overstated the replay: it measured a reviewer brief with and without the memory index, not an Agent-tool
  subagent, and a finder, not a refuter. The owner is told so in this pass's report, and P1 stands as the
  guard above on their direction.
- Taken from round 4: the second-reviewer sentence above, which read as if the index suppressed every key; the count
  correction's wording; `bin/blind-run`'s docstring; "with any notes on this PR"; and two more phrases on question 6.
  Round 4 also noted that the owner hears of the overstated reason only after the push; P1 is a text change a revert
  undoes, and the report offers one.
- Suspected by rounds 3 and 4 and not run (it needs a `claude` run): a reviewer pr-harden starts, told "not to spawn
  subagents of its own" (pr-harden :202), might take P1's command as allowed.

**Observed, not yet actionable.**
- "Seen, then set aside": one more measurement sighting in a full review (a C run of the replay above noticed a key and
  did not file it), so 12. Two standalone reviewer-brief runs also set it aside and two filed it only in part, which
  this count leaves out. Run records stay at 2.

**Still the owner's:**
- openmrs-core's project memory runs an unanchored second reviewer this file parked (2026-11-19): adopt it in pr-review,
  or drop the memory's line;
- whether to direct a change to pr-review :172's reach clause now, on one measurement, instead of waiting for its
  REOPEN ON;
- `kill-session-scratchpad-procs.sh`: push a fixed draft or remove it (2026-11-05 entry), and with it the
  working-directory match a 2026-11-22 pass dropped;
- publishing the 2026-11-13 bash 5 paragraph of the user-level `CLAUDE.md`;
- whether "push the fix" also replies in that comment's thread;
- the ninth pass's posting on their own PRs, the CI half of the exploit rule, its `~/.m2` install guard and per-PR claim
  note, and the Action's "post nothing" when an author has asked a question.

**Running parked counts:** "seen, then set aside" measurement sightings 11 to 12; the counts in run records are
unchanged, since no run record arrived in this window.

## 2026-11-23 (second owner-directed pass: the parked second reviewer into pr-review for `--post` rounds, then left parked on the owner's choice) — 0 applied; 1 owner-directed proposal killed; linter 17 files, 0 findings; no skill file changed

**Evidence read.**
- The store matched `origin/main` both ways before the read: 751 files, none different; only a `.DS_Store` and a
  `.pyc` cache are local-only, and neither is mirrored.
- No run record has been written since `LAST` (2026-11-23). The pass rested on the owner's direction and on the
  measurements this file holds: the 2026-11-19 sections and the previous section's replay.

**Killed: the second reviewer in pr-review for `--post` rounds.**
- The owner asked "isn't a a finding in 1 of 3 runs better than no blind reviewer at all?" and answered "put it into
  pr-review for --post rounds". The reply they answered had credited the second reviewer with catching, in 1 of 3 runs
  of the previous section's replay, a defect the review alone missed.
- The refuter (`2026-11-23-retro2-1123-refute1`, 25 turns, $1.43) showed that credit false, and the run's own log
  confirms it. In the one U run that filed a key, the main review found it by reading the code before it started its
  second reviewer, whose report found no defect it could prove; the other two U runs' second reviewers reported neither
  key. Across that replay and 2026-11-19's, the second reviewer's own reports carried a key in 2 of the 6 runs, both
  K-order on 2026-11-19, which the review set aside each time, and in none of the previous section's three. On
  2026-11-19 it also added one valid finding (a missing test) and one noise, at 76% more cost. In neither replay did it
  change what the review filed.
- The refuter put its own reports at 0 keys in 6 runs. That held for one replay only; the session passed it to the
  owner unchecked, then checked it and gave the owner the 2 of 6. The owner chose "Leave it parked" both times.
- The refuter's other objections fell with the proposal: the draft
  clashed with :26's "post the blockers now" on an approval mid-round; it moved the measured paragraph from "before you
  settle your findings" to Step 1 without saying so; it named the 2026-11-19 REOPEN ON and not the 556 union's; it left
  out `--stage` and a post the user asks for without `--post`; and its REVERT ON read ambiguously.

**Correction to the previous section.** Its "U filed a key in 1 of 3 runs" holds for the arm, and that key was the main
review's own, not its second reviewer's. Read the second reviewer's REOPEN ON (2026-11-19, unchanged) with that in
mind: a U catch is the second reviewer's only where its own report carries the key.

**Decided by the owner:** the second reviewer stays parked in the skill, and keeps running from openmrs-core's project
memory on `--post` rounds, now through the fixed `bin/blind-run`.

**Still the owner's:**
- whether to direct a change to pr-review :172's reach clause now, on one measurement, instead of waiting for its
  REOPEN ON;
- `kill-session-scratchpad-procs.sh`: push a fixed draft or remove it (2026-11-05 entry), and with it the
  working-directory match a 2026-11-22 pass dropped;
- publishing the 2026-11-13 bash 5 paragraph of the user-level `CLAUDE.md`;
- whether "push the fix" also replies in that comment's thread;
- the ninth pass's posting on their own PRs, the CI half of the exploit rule, its `~/.m2` install guard and per-PR claim
  note, and the Action's "post nothing" when an author has asked a question.

**Running parked counts:** unchanged; no run record arrived in this window.

## 2026-11-24 (owner-directed: blind agents on each acceptance criterion in pr-review, reopening the parked second reviewer for those PRs on the owner's choice; and the four files since `LAST`) — 2 applied; 0 killed; linter 18 files, 0 findings; **pr-review 0.39.0**

**Evidence read.**
- The store matched `origin/main` before the read: every one of its 751 files is here and identical. 52 files are
  here only; this commit mirrors them, except a `.DS_Store` and a `.pyc` cache.
- Since `LAST` (2026-11-23), four files that no section names:
  - `2026-11-23-pr-review-openmrs-module-stockmanagement-71.md`, three run records (rounds 5 to 7);
  - `2026-11-23-pr-review-setaside-filing-probe-6535.md`, a measurement note, not a run record;
  - `2026-11-24-pr-review-openmrs-core-6801.md` and `2026-11-24-pr-review-openmrs-module-stockmanagement-86.md`, one
    run record each.
  That is five run records over three PRs, and one measurement.
- P1 rests on the owner's direction and on what the owner was shown from a private-ticket PR, whose record is not in
  this repository.

**Applied, owner-directed: P1, pr-review 0.39.0, Step 2, with a new role file `blind-agents.md`.**
- **The direction.** The owner asked "If the blind review runs found things that other reviews did not catch,
  shouldn't we incorporate it in the pr-review skill by default to increase the comprehensiveness of the review?". The
  answer they were given opened "Yes, as the default for pull requests whose ticket lists acceptance criteria, not for
  every review". They then said "yes, run the skill-retro pass".
- **The rule, in SKILL.md.**
  - Outside CI, where the PR's ticket lists acceptance criteria, each round starts, on the head it reviews, one blind
    agent per criterion and one on the whole PR. Criteria are grouped so a round starts no more than six.
  - A reviewer pr-harden started is exempt.
  - The review records its own findings before it reads the agents' reports, then reproduces or refutes each defect
    they report before the verdict.
  - An agent still running holds back no verified blocker. One past its deadline, or with no report, is owed.
- **The launch and brief, in `blind-agents.md`,** which is read only where the step applies. This is the setup of the
  rounds the evidence comes from:
  - a 90-minute deadline and a 75-minute stop line;
  - proxies at a closed local port, except the Anthropic and claude.ai hosts, localhost and 127.0.0.1;
  - Maven offline, and each server with its own data directory and a free port between 18000 and 19000, touching no
    server the agent did not start;
  - a brief with the ticket in full and the decisions already made, but none of the review's own findings;
  - "use no network", plus "post nothing, push nothing, change nothing outside its folder", and the PR's base named.
- **What the owner was shown.** One private-ticket PR, two rounds with one agent per criterion and one on the whole PR.
  They ran with the deadline, stop line, proxies, offline Maven, no-network brief and server rules above, and no memory
  index.
  - Seven defects, all reproduced and then fixed in that PR.
  - The whole-PR agents' own reports carried 4 of them, 2 that no other agent carried.
  - The agent for the one criterion the head missed carried 4, 3 that no other agent carried.
  - The other criteria's agents carried one of the seven, which a whole-PR agent also carried. Two of them carried
    three older defects outside the PR that no other agent found.
  - $17.00 and $18.65 a round.
  - An earlier round, run before `bin/blind-run`'s fix, missed five of them.
- **It reopens the second reviewer (parked 2026-11-19, kept parked on the owner's choice 2026-11-23) for these PRs.**
  - Neither of its REOPEN ONs is met (2026-11-19's, and the 556 union's at :5675): one PR, no control arm, no
    pre-registered bar, and no run record in which a second reviewer's filed finding was what the maintainer raised.
  - The first refuter named this. The owner was then asked whether to keep the whole-PR agent, and was told the 2 of 7
    only it found, that there was no control run, and the confound below. They chose "Keep whole-PR agent
    (Recommended)".
  - By the 2026-11-23 correction, these catches are the agents' own, since their reports carry the defects.
- **Confound.** The round that missed five of them ran its agents before `bin/blind-run`'s fix at 98c45ffa, which
  removes the memory index they had loaded. So part of the gain may be blindness, or the 90-minute deadline and the
  break-it target, rather than the split by criterion.
- **It is not P-A** (per-dimension fan-out, parked :2143, :2307-2327).
  - It splits by the ticket's criteria, not by review dimension, and it is no selectable mode.
  - Its evidence is uncontrolled, so P-A stays parked.
- **REVERT ON:** three consecutive rounds under it, on at least two PRs whose tickets list acceptance criteria, where
  the agents report no defect in the PR's change that is missing from the findings the review recorded before it read
  their reports.

**Applied: P2, pr-review 0.39.0, Step 3's test coverage.**
- **Bar:** skill-retro Step 3, two run records.
  - Predicate enumeration rests on rounds 5 and 6 of stockmanagement #71.
  - The fixture-input check rests on round 7 (role inheritance; one item and only dated batches) and round 5 (every
    actor but one held every privilege at both ends).
- **Distinct from (c)**, the per-condition mutation sentence that was measured and not adopted (:5633-5637).
  - (c) targeted the conditions a PR adds ("For each one the PR adds, delete or invert it alone"). P2 targets a PR that
    adds only tests, for the code that serves what its ticket describes.
  - The ruling that a run record does not meet (c)'s REOPEN (:6160) concerned a PR that adds conditions. A tests-only
    PR adds none, so (c) cannot apply to one, and the fixture-input check is not in (c).
- **What the ticket describes.** #71's ticket names queries and checks rather than methods, so P2 lists the methods that
  serve what the ticket describes.
- **Where the ticket points at no code,** the review asks the user, or in CI the author. Under another skill, it returns
  the question.
- **Survivors.** A survivor counts as a gap only where a caller can see its change.
- **Examples.** The examples are #71's own answers, so they stay illustrations.

**Budget.**
- pr-review/SKILL.md goes from 10072 to 10381 words.
- The new role file pr-review/blind-agents.md goes from 0 to 288.
- Both carry `raises` entries. Neither proposal retires a rule.
- Putting the launch detail in a role file keeps what every review reads down to the rule itself.

**Gate.** Refuters through `bin/blind-run`:
- **`2026-11-24-retro-1124-refute1`:** P1 REVISE, P2 REVISE. Taken:
  - P1: it names the parked second reviewer it reopens, which went to the owner;
  - P1: an exemption for pr-harden's reviewer, since pr-harden:202 tells it to start no agents;
  - P1: the citation of *A fix you recommend is different*;
  - P1: the brief's stop line, the agent owed past its deadline, verified blockers not held back while agents run, and
    "ticket" only;
  - P2: round 7's attribution;
  - P2: the PR's test classes before the whole suite;
  - P2: tickets that name no methods.
- **Corrected after round 1.**
  - The owner and the first refuter had been told "up to 45 minutes". The rounds ran with `--deadline 5400` and a
    75-minute stop line.
  - The first refuter had also been told that the other criteria's agents carried none of the seven. One carried one,
    which a whole-PR agent also carried.
- **`2026-11-24-retro-1124-refute2`:** P1 REVISE, P2 REVISE, eight sentences of this section to fix. Taken:
  - P1: the proxy variables named, with localhost among the exceptions;
  - P1: an agent with no report counted as owed;
  - P1: the review's findings recorded first, so the REVERT ON can be measured;
  - P2: "reverse the check";
  - P2: asking where the ticket names no method;
  - this section's REOPEN ONs, the 98c45ffa fix, (c)'s ruling, the counts below, the open question on the memory's
    second reviewer, and wording that narrowed down which PR this is.
- **`2026-11-24-retro-1124-refute3`:** P1 REVISE, P2 REVISE, seven sentences to fix.
  - **Measured by the refuter:** Maven 3.9.10 downloaded with `JAVA_TOOL_OPTIONS` and `HTTPS_PROXY` at a dead port, so
    the agents build offline. Offline, an artifact in `~/.m2` resolved under `blind-run`'s repository settings, and one
    missing from it failed. A finished background run's notice shows no output, so the review can record its findings
    before it reads a report.
  - Taken: offline Maven; each agent's own data directory and port, touching no server it did not start; the
    exemption narrowed to pr-harden's reviewer; "invert or remove ... keeping the code compiling", since reverting to
    base changes nothing on a tests-only PR; asking the author in CI; the REVERT ON's rounds and PRs; and this
    section's bar, confound, P-A and #86 sentences.
- **After round 3:** the launch and brief moved to `blind-agents.md`.
- **`2026-11-24-retro-1124-refute4`:** P1 REVISE, P2 REVISE, eight sentences to fix.
  - Taken for P1:
    - one set of agents per round, on the head it reviews (pr-review:26);
    - no more than six a round, grouping criteria;
    - "post nothing, push nothing" and the base in the brief (blind-run:48-49, pr-harden:228);
    - the port range the evidence used.
  - Taken for P2:
    - keyed to what the ticket describes, since #71's ticket names no method;
    - the question returned under another skill (pr-harden:195-197);
    - a survivor counted as a gap only where a caller can see its change (#6801's record);
    - the fixture-input bar resting on round 5 as well.
  - Taken for this section: :7123.
  - **Rejected, with the evidence's briefs as citation:** that offline Maven departs from the evidence. Those briefs
    said "Keep it, and Maven offline (`-o`)" and "Use no network", and their launch excepted localhost from the proxy.
    So "use no network" goes into the brief too.
- **`2026-11-24-retro-1124-refute5`:** P1 REVISE (role file only), P2 STANDS, three sentences to fix.
  - **Measured by the refuter:**
    - curl reached the agent's own server at 127.0.0.1 only once 127.0.0.1 joined `NO_PROXY` (exit 7 before).
    - Java skips the proxy for loopback either way.
    - Maven 3.9.10 downloaded behind the dead proxy with `MAVEN_ARGS` cleared, and the offline control with an empty
      repository failed.
  - Taken:
    - 127.0.0.1 and `no_proxy` in the exceptions;
    - the evidence's "Keep `MAVEN_ARGS`" line, which carries `blind-run`'s fallback to `~/.m2`;
    - the server rules moved inside the brief, since an agent knows only its brief;
    - this section's #86, setup and #71 sentences.
- **`2026-11-24-retro-1124-refute6`:** P1 REVISE, P2 STANDS, five sentences of this section to fix.
  - Taken for P1: the proxy exceptions named as the evidence's launch named them, and "any of these agents'
    reports", so the rule does not catch Step 3's agents.
  - Taken for this section: the REOPEN ON reason, :6043-6044, round 5's set-aside, the proxy exceptions and this
    entry.
  - Those replacements restore the evidence's own wording or narrow a scope, and were applied as the refuter wrote
    them, with no seventh round.

**Observed, not yet actionable.**
- **stockmanagement #71, rounds 6 and 7:** two set-asides of another shape were re-raised by the blind reviewer and
  found valid. One was set aside as unverified, the other for a false reason. Neither is a sighting of "seen, then set
  aside as outside the ticket".
  - In round 5 the review re-weighed and posted its own rounds 1-4 set-aside (`urs.enabled = 1`, set aside as
    "notifications only"), since the ticket names that query.
  - That is a set-aside of the parked shape, but the review raised it, not a maintainer. So it does not meet the
    REOPEN ON (:6043-6044), and the run-record count stays at 2.
- **stockmanagement #86, round 2:** a suggested test was checked against its trigger but not mutated on the predicate it
  pins (a location carrying another tag). The blind unanchored reviewer found it.
  - It is near the parked "testing a suggested test against cross-wiring mutants" (:7123, REOPEN ON a second), but it
    is not that second instance: its mutant weakens the predicate, and does not cross-wire it.
  - Its record names a line of the local recipes file that asked for a no-match case. It also says no skill sentence
    asks a suggested test to be mutated on the predicate it pins as well as on its trigger. 1 record.
- **openmrs-core #6801:** Step 3's "Read each added line" rule existed, and six rounds did not apply it to a file they
  probed in depth. 1 record of a rule not applied, and no new rule.
- **The set-aside filing measurement on 6535 (2026-11-23):** W2 filed K-order in 4 of 5 runs against C's 1 of 5, with
  noise in 1 of 5 against 0 of 5. This is development evidence. A held-out validation is what would make it
  proposable.
- **stockmanagement #71's ticket** lists four acceptance criteria, so P1 would apply to that PR too.
- **Suspected, not run:**
  - whether parallel agents that each start servers clash on ports. In the evidence's rounds, each brief gave its agent
    its own data directory and a free port between 18000 and 19000, and none clashed;
  - whether an offline build finds every artifact a head needs in `~/.m2`. One missing there leaves that agent unable
    to build, which its report then marks as not run.

**Still the owner's:**
- whether openmrs-core's project-memory second reviewer still runs on rounds where P1 starts a whole-PR agent;
- whether to direct a change to pr-review :172's reach clause now, on one measurement, instead of waiting for its
  REOPEN ON;
- `kill-session-scratchpad-procs.sh`: push a fixed draft or remove it (2026-11-05 entry), and with it the
  working-directory match a 2026-11-22 pass dropped;
- publishing the 2026-11-13 bash 5 paragraph of the user-level `CLAUDE.md`;
- whether "push the fix" also replies in that comment's thread;
- the ninth pass's posting on their own PRs, the CI half of the exploit rule, its `~/.m2` install guard and per-PR
  claim note, and the Action's "post nothing" when an author has asked a question.

**Running parked counts:**
- "Seen, then set aside" is unchanged: run records 2, measurement sightings 12.
- The second reviewer is reopened for PRs whose tickets list acceptance criteria, and stays parked for the rest.
- P-A is unchanged.
- The cross-wiring item at :7123 stays at one instance.

## 2026-11-24 (second owner-directed pass: the second reviewer in pr-review for every PR outside CI) — 1 applied; 0 killed; linter 18 files, 0 findings; **pr-review 0.40.0**

**Evidence read.**
- The store matched `origin/main` before the read: 801 files both ways, none different.
- No record has been written since `LAST`, which this morning's pass set to 2026-11-24. This pass rests on the owner's
  direction and on the records that pass read.

**Applied, owner-directed: P3, pr-review 0.40.0.**
- **The direction.**
  - The owner asked "why does the blind reviewer run for only openmrs-core?", and was told it lives in that folder's
    project memory.
  - They then asked "Can we have it for every pull request?". The answer opened "Yes, outside CI. The simplest way is
    to start the whole-PR blind agent from pr-review 0.39.0 on every review round, instead of only on PRs whose ticket
    lists acceptance criteria". They said "ok".
  - The first refuter showed that one reason in that answer was false (below). The owner was asked again, with the
    correction and the measured cost, and chose "Apply it to every PR (Recommended)", whose text read "Whole-PR blind
    agent every review round outside CI, at $0.69–$3.43 a round".
- **The rule.**
  - Step 2's blind-agent paragraph starts the whole-PR agent every round outside CI, and per-criterion agents only where
    the ticket lists acceptance criteria.
  - Where no per-criterion agents run (no ticket, an unreadable one, or one that lists no acceptance criteria), the
    whole-PR agent runs at `--deadline 1800` with a 25-minute stop line, the setup of 10 of the 12 public agents
    measured below. Criteria rounds keep 5400 and 75 minutes.
  - Until each agent has reported or passed its deadline, the review posts nothing but the blockers Step 1's watch
    bullet posts on an approval, counting one the PR already had when the round started.
  - Step 3's "solo in one pass, without a fan-out of independent finders" says these agents do not count as that
    fan-out, since they never weigh the review's findings.
  - `blind-agents.md` briefs the whole-PR agent with the PR's description where there is no ticket, or the ticket could
    not be read.
- **It adopts the second reviewer for every PR outside CI.** That reviewer was parked 2026-11-19, kept parked on the
  owner's choice 2026-11-23, and reopened for PRs with acceptance criteria by this morning's pass. Neither of its
  REOPEN ONs is met (2026-11-19's, and the 556 union's at :5675). There is no new evidence since this morning's pass,
  so the change rests on the owner's direction alone.
- **What the owner was shown.**
  - **Against:** the two replays on 6535. In neither did it change what the review filed, and on 2026-11-19 it cost
    about 76% more and took 47% longer per review.
  - **For, uncontrolled:**
    - stockmanagement #86 round 2, where its own report found the case the review's suggested test missed;
    - stockmanagement #71, where it re-raised an item the review had set aside for a false reason;
    - a private-ticket PR, where the whole-PR agents' reports carried 4 of 7 defects, 2 that nothing else carried.
  - **The false reason.** The first answer said the replays' failure, the review setting the reviewer's finding aside,
    is what 0.39.0's "reproduce or refute each defect they report" targets. It is not.
    - In the 2026-11-19 replay the review noticed K-order in all three U runs, two through the second reviewer, one of
      those with a probe, and set it aside in all three (:6395-6396). A reproduced defect can still be set aside.
    - The 2026-11-19 addendum put the next candidate at the filing judgment, not at more reviewers (:6408-6409).
    - The owner was given this correction before their second answer. That correction itself said the review
      "reproduced the defect", where the record shows a probe once.
- **Also on the record, not shown to the owner:** by the sessions' own statements it found something the review missed
  in 12 of the 71 sessions that ran it, and 26 say it found nothing new (:6365-6367).
- **Cost.** The 12 public whole-PR and unanchored agents of 2026-11-23 and 24 cost $0.69 to $3.43 a round and ran 3.1 to
  22.5 minutes. 10 had a 25-minute stop line, one had 60, and one, at $3.43, had the 75-minute line.
  - The 25-minute agents cost $0.69 to $1.88, so the top of the range is the 75-minute one.
  - The owner was told 5 to 22 minutes.
- **Posting.** Posting only after the agents report or pass their deadline is what `posting.md`:23 already required
  ("a second reviewer").
  - This reverses 0.39.0's "One still running holds back no blocker you have verified", which this morning's first
    refuter had asked for.
  - This pass's first refuter said `posting.md` has no wait for agents. Its search missed :23.
- **Memory, outside this repo.**
  - openmrs-core's project memory starts no unanchored reviewer of its own any more, so it does not run twice there.
  - Its lines that briefed or timed that reviewer now brief and time pr-review's whole-PR agent: post only after it
    reports, a narrower brief on auth PRs, and packaging and release in the brief.
  - Its refuter instruction stays.
- **REVERT ON:**
  - **When:** three consecutive such rounds whose whole-PR agent reported, on at least two PRs where no per-criterion
    agents ran, where that agent reports no defect in the PR's change that the review then reproduced and that is
    missing from the findings it recorded before it read the report.
  - **What it restores:** 0.39.0's Step 2 paragraph and `blind-agents.md`, and openmrs-core's memory reviewer.
  - **How to check it:** read those rounds' records in `~/.claude/pr-review-rounds/`, which skill-retro's Step 1 does not
    read.

**Budget.**
- pr-review/SKILL.md goes from 10381 to 10413 words, with a `raises` entry.
- pr-review/blind-agents.md goes from 288 to 332, with a `raises` entry. It is now read on every round outside CI.
- Nothing is retired.

**Gate.** Refuters through `bin/blind-run`:
- **`2026-11-24-retro-1124b-refute1`:** P3 REVISE.
  - **Taken:**
    - the false reason, put to the owner, who answered again;
    - the measured cost;
    - posting only after the agents report;
    - Step 3's fan-out clause;
    - the REVERT ON's PRs with no ticket, and where its evidence is kept;
    - the full memory edit;
    - the 12-of-71 sentence moved out of what the owner was shown.
  - **Left as is:** giving every agent the PR's description as well as the ticket, which would depart from the briefs
    the evidence rests on.
- **`2026-11-24-retro-1124b-refute2`:** P3 REVISE.
  - **Measured by the refuter:** the 12 public agents' stop lines, costs and run times, which showed the owner's price
    came from 25-minute runs.
  - **Taken:**
    - the 25-minute setup for whole-PR agents without criteria;
    - an approval already present when a round starts;
    - "never weigh", not "never see";
    - unreadable tickets;
    - the posting reversal named;
    - the REVERT ON counting reproduced defects and naming skill-retro's Step 1;
    - the correction's "reproduced" noted.
  - **Not run, and the owner's:** whether a 25-minute agent finds what a 75-minute one does. That needs the replay the
    owner declined.
- **`2026-11-24-retro-1124b-refute3`:** P3 REVISE. It found no contradiction with pr-review, its role files or
  pr-harden.
  - **Measured by the refuter:** the 12 agents' times, 3.1 to 22.5 minutes, and that the 25-minute ones cost $0.69 to
    $1.88.
  - **Taken:**
    - the deadline keyed to whether per-criterion agents run, so an unreadable ticket gets 1800;
    - the posting wait ending at the deadline as well as at a report (`posting.md`:23);
    - the REVERT ON counting only rounds whose agent reported, and naming what a revert restores;
    - the cost and replay sentences.
  - **Applied as written,** with no fourth round, since they narrow wording the earlier rounds had settled.

**Observed, not yet actionable.** None new since this morning's section.

**Still the owner's:**
- whether to direct a change to pr-review :172's reach clause now, on one measurement, instead of waiting for its
  REOPEN ON;
- `kill-session-scratchpad-procs.sh`: push a fixed draft or remove it (2026-11-05 entry), and with it the
  working-directory match a 2026-11-22 pass dropped;
- publishing the 2026-11-13 bash 5 paragraph of the user-level `CLAUDE.md`;
- whether "push the fix" also replies in that comment's thread;
- the ninth pass's posting on their own PRs, the CI half of the exploit rule, its `~/.m2` install guard and per-PR
  claim note, and the Action's "post nothing" when an author has asked a question.

**Running parked counts:**
- The second reviewer is adopted for every PR outside CI (pr-review 0.40.0).
- This morning's question about openmrs-core's memory reviewer is settled by the memory edit above.
- "Seen, then set aside" is unchanged: run records 2, measurement sightings 12. It stays the candidate the 2026-11-19
  addendum named.
- P-A is unchanged.

## 2026-11-25 (owner-directed pass: what pr-review's blind agents cost, and what a starved one leaves on record) — 3 applied (Q1-Q3), 1 part put to the owner; seven refutation rounds; linter 18 files, 0 findings; **pr-review 0.41.0**, **bin/blind-run**

**Evidence read.**
- Before the read, every file the repo holds under `.claude/` matched its live copy except the user-level `CLAUDE.md`
  (still the owner's, below). Live only: this window's records and artifact folders, and
  `kill-session-scratchpad-procs.sh` with its log (still the owner's).
- Records since `LAST` (2026-11-24) that no section names: three, all 2026-11-25.
  - `2026-11-25-blind-agent-cost-review.md`, a hand-written capture with a 19:00 addendum. This pass rests on it.
  - `2026-11-25-blind-agent-concurrency-load.md`, a hand-written capture.
  - `2026-11-25-pr-review-openmrs-module-stockmanagement-71.md`, the run record of #71 round 9.
- One measurement note, `artifacts/2026-11-25-whole-agent-time-replay/protocol.md`. Its result, "stay at 25", changes
  nothing.
- `confidential/` was read for names only. The cost review's `runs.tsv` named runs of two PRs `PRS` lists, in 22 rows,
  and the capture named one of them. The full table moved to `confidential/artifacts/`, the public one lost those rows,
  and the capture now says "the private-ticket subject".

**The direction.** The owner asked "Is it still worth it? Or should we remove it from the default such that we make it
optional. For instance only do when a given parameter is passed? Or any other better strategy that you can think of?",
then "what do you recommend?". The answer was to keep every agent, the whole-PR one included, to build no concurrency
cap until the load is on record, and to make three fixes either way. They said "yes go ahead".
- Q3 first skipped starved rounds in the count. After the fifth refutation round, asked how a starved round that still
  found something should count, the owner answered "why shouldn't a starved round just run again?". The answer: it
  should, by making a starved agent owed, so the next round runs it, with the count left to the owed rule and a
  reproduced defect still breaking a run. They said "ok".
- That answer gave pr71-r10-whole as an agent that named the load but lost nothing. Its report lists its REST class and
  JDK 21 runs as not run under the load, so it lost runs too, and the owed rule takes it.

**Applied, owner-directed: Q1 and Q2, `bin/blind-run`.**
- **Q1:** `log/blind-run-load.tsv`, the 1-, 5- and 15-minute load averages at launch, every 60 seconds and at exit. A
  sample that cannot be written is skipped, so the deadline loop still reaches its kill.
- **Q2:** the summary line, now also in `log/blind-run-summary.txt`, and printed on a deadline kill too. Both files
  carry `blind-run` in their names because agents write into `log/` too: a replay agent's own `summary.txt` is there.
  - Turns are Claude Code's `num_turns` summed over every result event, plus the tool results after the last one.
  - Cost is the last result event's. Minutes are measured by `blind-run`, launch to exit.
  - The line ends with the load's median and maximum.
- **Also in Q2, beyond the owner's words, because fix 2 needs them:**
  - The kill moved into `stop()`, which suppresses `PermissionError` as well as `ProcessLookupError`. macOS raises it
    from `killpg` on a group whose only member is an unreaped zombie, the state of an agent that exits just as its
    deadline passes. `blind-run` then stopped with a traceback before writing its report or summary. It was found
    while checking a mutation (`artifacts/2026-11-25-blind-run-load-summary/killpg-zombie-output.txt`).
  - A load sample that cannot be written, or a load file the agent removed, no longer ends the run before its
    summary.
- **Measured** over the 273 runs with a result event in `~/.claude/blind-runs` at 20:26 on 2026-11-25:
  - 55 had several result events, from a Stop hook's or a background task's continuation. The old line printed the
    last segment's turns in all 55, and its minutes were off the file times by more than a minute and 10% in 57.
    sm80-r2-whole read "2 turns, 0.5 min" for 58 turns and 73 minutes.
  - Summed `num_turns` equalled tool results plus result events in 241. In the other 32 the gap is text-only turns,
    such as a Stop-hook amendment, which a count of the stream misses.
  - Cost never fell across a run's result events (0 of 55), so the last one is the run's.
- **Self-test:** 38 checks to 55. Each new one goes red under a mutation of the code it guards: fifteen mutations,
  five of them from the refuters' findings (`mutations-output.txt` in that artifact folder, with the calibration
  scripts and their outputs, re-run at about 21:25 when the counts had moved on).
- **Real agents:**
  - With the final file, `brfix2-real-ok` finished and printed Claude Code's own count, 3 turns. `brfix2-real-kill`
    was killed at 45 s inside a foreground `ping` and printed the kill's summary.
  - With earlier drafts, `brfix-real-kill3` was killed at 20 s while the model was writing.
  - `brfix-real-kill` never reached its deadline: the owner's sleep hook refused its `sleep 200`, so hooks reach
    blind agents too.

**Applied, owner-directed: Q3, pr-review 0.41.0.**
- **The rule:** Step 2's owed sentence reads "One past its deadline, that reports nothing, or whose report says the
  machine's load kept it from a run it planned, is owed (Step 5)." Step 1 already has each round run or apply every
  owed item before its verdict, or say it is still owed (`SKILL.md:22`). A re-run costs one more agent run; the #67
  round re-ran its killed whole-PR agent within the round (`pr67-r1-whole2`).
- **Budget:** pr-review/SKILL.md goes from 10413 to 10426 words, with a `raises` entry. Nothing is retired; the rule
  amends 0.40.0's REVERT ON, below.

**0.40.0's REVERT ON (:7781-7787), amended.**
- **When:** as at :7782-7784, counting only rounds whose whole-PR agent reported and is not owed. A round counts by
  that agent's last run in the round that reported and is not owed, a re-run after a kill included (`pr67-r1-whole2`
  in the #67 round record). A round whose agent is still owed when the round ends neither counts toward the three nor
  breaks a run of them; the next round's own agent counts for that next round. A round in which that agent reported a
  defect that :7783-7784 counts (the review reproduced it, and had not recorded it before reading the report) breaks
  the run, owed or not.
- **Owed,** from pr-review 0.41.0 (Step 2's blind-agent paragraph): past its deadline, reporting nothing, or with a
  report that says the machine's load kept it from a run it planned, or, per `blind-agents.md`:11, whose log shows it
  never reached the model. The next round runs it or says it is still owed, as with any owed item (`SKILL.md:22`).
- **How to check it:** the rounds' records in `~/.claude/pr-review-rounds/`, as before, which name owed agents, and each
  whole-PR agent's `~/.claude/blind-runs/<date>-<name>/log/`.
  - **A deadline kill:** `log/blind-run-summary.txt` opens "blind-run: deadline of". Where that file is missing or
    opens otherwise, as for a run from before it existed, the agent's `log/out.jsonl` ends with an event other than a
    result event and was last written 0 to 30 seconds after its deadline, counted from when `log/BRIEF.md` was
    written. The deadline is the round record's, else `blind-agents.md`'s 1800 s for a whole-PR agent without
    per-criterion agents. An agent the kill found waiting on the model with nothing streamed may have written nothing;
    no run on record shows that.
  - **The load:** read the report in full, since a pattern finds only the wordings it lists. This one finds the
    wordings the record held on 2026-11-25:

    ```
    load average|machine (was|is) (at load|(very )?(slow|loaded|busy|saturated)|shared)|machine was shared|loading the machine|machine load|slowed|under load|heavily loaded|very slow
    ```
- **Evidence:**
  - **Report words,** as of 21:25: of 293 `blind-run` reports written from 2026-11-20, 33 match that pattern, 25 of
    them round agents', and each of those 25 hits is about the machine
    (`artifacts/2026-11-25-blind-run-load-summary/starved-grep-output.txt`).
  - **Kills,** as of 21:15: of the 20 streams that ended with an event other than a result event, the 10 deadline
    kills, 8 of round or replay agents and 2 staged with a draft of the new copy, are the ones last written 0 to 30
    seconds after their deadline: 5400.5 to 5402.2 s, 1800.1 and 1803.1 s, 45.0 s at 45 and 20.4 s at 20. The others
    were stopped another way or were still running. Of the 273 streams that ended with a result event, the five last
    written within 30 seconds of a 30-, 45- or 90-minute mark each ended on its own with a `success` result.
  - **A re-run recovers the work:** in the cost review's replay, #80 r1's behaviour brief, killed at its 90-minute
    deadline at a load of about 170, finished in 29.7 to 38.4 minutes, and #71 r8's lifecycle brief, 47.4 minutes at
    about 86, took 26.9 to 42.3.
- **Not in the test, and the owner's:** a load threshold read from `blind-run-load.tsv`. The owed clause misses agents
  that describe what the load did without naming it, or blame a time limit for a run the load slowed: pr69-whole's
  MariaDB "was stopped by the 10-minute background limit during the install", pr85-r1-whole's test classes each "took
  about 15 minutes to start", and at least seven more round agents of 2026-11-25 (pr71-r9-whole, pr71-r10-permissions,
  pr69-ac1-fresh, pr69-ac2-existing, pr69-ac3-restart, sm80-r1-privileges, sm80-r2-structure). None ran in a round this
  REVERT ON counts, since all those rounds had per-criterion agents. At loads of 55 to 85, by pr71-r10-lifecycle's own
  estimate, every agent of four overlapping reviews reported, but three said the load cut their work, so the data
  neither sets nor rules out a threshold.

**Gate.** Refuters through `bin/blind-run`:
- **`2026-11-25-retro-1125-refute1`:** Q1 REVISE, Q2 STANDS, Q3 REVISE.
  - **Measured by the refuter:**
    - a kill one sampling period early passed all 48 checks;
    - Q3's first pattern hit 21 of 279 reports and missed sm80-r1-whole, sm80-r2-whole and pr71-r9-unchanged;
    - its own `load.tsv` read 49 to 97 while it ran.
  - **Taken:**
    - a check that the kill comes no earlier than the deadline;
    - the wider pattern, re-measured;
    - the load threshold out of the test and put to the owner, since the owner agreed to two halves;
    - a fallback for runs from before `summary.txt`, since a run folder records no deadline.
- **`2026-11-25-retro-1125-refute2`:** Q1 STANDS, Q2 STANDS, Q3 REVISE.
  - **Measured by the refuter:** a loop that waits a full sampling period past the deadline passed all 50 checks.
  - **Taken:**
    - a check that the kill comes on time when the period is longer than the deadline;
    - "any of the round's blind agents", the owner's "an agent", where Q3 had said the whole-PR agent;
    - the pattern on a line of its own;
    - "runs with a result event", not "finished runs";
    - the overlap that reported at 55 to 85 stated as the record states it.
  - **Its suspicion, measured instead of noted:** a kill while the model is writing. `brfix-real-kill3` shows it ends
    with an event other than a result, which made that the fallback test, checked over every stream on record.
  - **Also added:** a check for a machine where `os.getloadavg()` fails, which neither refuter could run.
- **`2026-11-25-retro-1125-refute3`:** Q1 and Q2 REVISE, Q3 REVISE.
  - **Measured by the refuter:**
    - two of six `summary.txt` files on record were written by agents;
    - Q3's fallback took runs stopped another way for kills, where 0 to 30 seconds after a deadline separates the 8
      kills from every other run;
    - six round agents' reports say the machine was saturated, shared and busy, loaded by builds, or under load, in
      words the pattern missed.
  - **Taken:**
    - the files renamed `blind-run-summary.txt` and `blind-run-load.tsv`;
    - the timing condition, re-measured;
    - the agents named as Step 2's, which excludes a fix's refuter;
    - the wider pattern, with each report read in full;
    - the threshold's reason restated, since three agents that reported at 55 to 85 said the load cut their work;
    - the counts dated;
    - a load sample that cannot be written is skipped, so the loop still reaches `stop()`.
- **`2026-11-25-retro-1125-refute4`:** Q1, Q2 and Q3 REVISE.
  - **Measured by the refuter:** the real runs then cited came from earlier drafts, whose files had the old names.
  - **Taken:**
    - two real runs of the final file;
    - a file for the EPERM measurement;
    - "by pr71-r10-lifecycle's own estimate" for 55 to 85;
    - the two agents the report test misses named as outside this REVERT ON's rounds;
    - a kill while the agent waits on the model with nothing streamed named as a gap of the fallback;
    - the turn in flight at a kill named as uncounted.
  - **Also taken:** a load file the agent removed. The refuter listed it as not run, and it crashed `summary()` before
    the summary was written.
- **`2026-11-25-retro-1125-refute5`:** Q1 and Q2 STAND, Q3 REVISE.
  - **Taken:** 293 reports at 21:25, the artifact's count; ten deadline kills counting the two staged ones.
  - **Put to the owner:** whether a starved round that found something breaks a run. The owner answered with the re-run
    design above, which replaced the skip.
- **`2026-11-25-retro-1125-refute6`:** Q1 and Q2 STAND, and Q3's skill change and budget hold; its amendment REVISE.
  - **Measured by the refuter:**
    - the amendment's defect sentence had dropped :7784's "missing from the findings it recorded before it read the
      report";
    - the #67 round re-ran its killed agent within the round, as `pr67-r1-whole2`, which a test reading only the first
      run's folder would have dropped;
    - at least seven more round agents blame a time limit for a run the load slowed.
  - **Taken:**
    - the defect sentence restored to :7783-7784's test;
    - a round counted by its agent's last run in that round, rather than "in that round or a later one" as the refuter
      wrote, so one run cannot count for two rounds;
    - the seven agents named in what the test misses;
    - what the change amends, for Step 4.
  - **Answered, no change:** whether owed re-runs push a round past six agents. The answer first given was struck in
    round 7.
- **`2026-11-25-retro-1125-refute7`,** on the changed text only: REVISE.
  - **Taken as written:**
    - a sentence deleted, which said the next round's own agents cover an owed agent at no extra start; no skill text
      says so, and the owner was told a re-run costs one more agent run;
    - `blind-agents.md`:11's never-reached-the-model case added to **Owed**.
  - **No eighth round,** since one change deletes a sentence and the other cites existing skill text.

**Observed, not yet actionable.**
- **#71 round 9:** a maintainer raised two nits that rounds 4 to 8 had carried as set aside while each posted 3 to 5
  other comments. It meets the wording of the "seen, then set aside" REOPEN ON (:6043-6044). But the set-aside reason
  was "nit", not "outside the ticket", and W2 (2026-11-23) is a wording for defects judged unlikely, not this. The
  record's own lesson, posting verified one-line nits in a round that posts anyway, has 1 record.
- **A run that ends on the account's usage limit** reads like any other in the summary line: 4 of 276 runs end with
  `is_error` and the limit's message, and their reports are that message. The reviews' own "reports nothing, is owed"
  covers them (esm-core #2022's round record has its agent owed). 1 window.
- **`blind-run` with no report:** a run whose agent backgrounds its work and ends its turn can end with none
  (blindz-s1-a75-1, in the cost review). 1 sighting.

**Killed before this pass,** by the cost review's refuters (`~/.claude/blind-runs/2026-11-25-blindcost-refute1` to
`refute6`), each with what would reopen it:
- **Making the agents opt-in.** Round agents are about 28% of the spend on 10 reviewed PRs, and 8 posted comments
  rest on their findings. **REOPEN ON:** a measured share well above that, or posted findings that stop coming.
- **Dropping the whole-PR agent beside per-criterion agents.** It is about 9% of that spend, and the only source of
  #71 round 10's SUB1, which survives the whole suite. **REOPEN ON:** a run of rounds, none starved, in which it is the
  only source of nothing the review reproduced.
- **A cap of five agents queued in `blind-run`** (refute5): five has no support in the data, is below `SKILL.md:38`'s
  six-agent round, and posting would wait on a queued agent with no bound; agents are not the only load.
- **A start-of-round wait, two rounds at a time** (refute6): no round-in-progress state exists to count, a bounded
  wait ends in no cap or owed agents, and it would hold up pr-harden's reviewers. Two rounds were already starved at a
  load of about 60, while the 13 round agents of four overlapping reviews all reported.
- **Admission with deferral:** #83's D and #85's E came from rounds started 7th and 8th in the night burst.
  `queue_sim.py` with six slots delays those rounds 121 and 142 minutes on the recorded wall times, and finishes every
  round 5 to 60 minutes sooner on a third of them; without a load record, nothing says which.
- **25-minute per-criterion agents:** the pre-registered replay backed 15 of 18 key-runs at 75 minutes and 7 at 25.
- **REOPEN ON, for any cap:** load records from Q1 showing agents starved at a load the machine reaches routinely.

**Still the owner's:**
- whether to direct a change to pr-review :172's reach clause now, on one measurement, instead of waiting for its
  REOPEN ON;
- `kill-session-scratchpad-procs.sh`: push a fixed draft or remove it (2026-11-05 entry), and with it the
  working-directory match a 2026-11-22 pass dropped;
- publishing the 2026-11-13 bash 5 paragraph of the user-level `CLAUDE.md`;
- whether "push the fix" also replies in that comment's thread;
- the ninth pass's posting on their own PRs, the CI half of the exploit rule, its `~/.m2` install guard and per-PR
  claim note, and the Action's "post nothing" when an author has asked a question;
- whether a load threshold read from `blind-run-load.tsv` makes an agent owed, once Q1's records show the load at which
  agents lose their runs;
- whether 0.39.0's REVERT ON (:7581-7583) leaves out rounds whose agents are owed, as 0.40.0's now does;
- whether a round that posts other comments anyway also posts the verified nits it would set aside (#71 round 9).

**Running parked counts:**
- "Seen, then set aside" as outside the ticket is unchanged: run records 2, measurement sightings 12.
- Every blind agent stays, the whole-PR one included; 0.41.0 changes only what makes one owed.
- P-A is unchanged.

## 2026-11-26 (owner-directed pass: 0.39.0's REVERT ON takes the owed rule) — 1 applied, its reading of "a defect" put to the owner; seven refutation rounds, the first run twice; linter 18 files, 0 findings; no skill file changed

**Evidence read.**
- Before the read, every file the repo holds under `.claude/` matched its live copy except the user-level `CLAUDE.md`
  (still the owner's) and `2026-11-25-pr-review-openmrs-module-stockmanagement-71.md`, which gained a record after the
  last push. Live only, outside the pipeline's run state: `kill-session-scratchpad-procs.sh` with its log (still the
  owner's), and the Anthropic skills Claude Code syncs into `skills/synced/` and `skills/.trash/`, which are not this
  repo's.
- Records since `LAST` (2026-11-25) that no section names: one, #71 round 12, appended at 23:43 to that file.
- `confidential/` was read for names only.
- The measurement below read the round records in `~/.claude/pr-review-rounds/` and the agents' folders in
  `~/.claude/blind-runs/`, which skill-retro's Step 1 does not read.

**The direction.** The 2026-11-25 pass left with the owner "whether 0.39.0's REVERT ON (:7581-7583) leaves out rounds
whose agents are owed, as 0.40.0's now does" (:8075), put to them as "whether 0.39.0's revert condition for the
per-criterion agents gets the same owed rule". They answered "yes, apply the owed rule to 0.39.0 too".

**Applied, owner-directed: 0.39.0's REVERT ON (:7581-7583), amended.** No skill file changes: pr-review 0.41.0's owed
sentence (`SKILL.md:38`) already covers every Step 2 agent, per-criterion ones included.
- **When:** as at :7581-7583, counting only rounds in which each of the round's Step 2 agents reported and none is
  owed. An agent counts by its last run in the round that reported and is not owed, a re-run after a kill included, as
  0.40.0's amendment counts `pr67-r1-whole2` (:7917-7919); no round with per-criterion agents has re-run one yet. A
  round with an agent still owed when the round ends neither counts toward the three nor breaks a run of them; the
  next round's own agents count for that next round. A round in which any of its agents, owed or not, reported a
  defect in the PR's change that is missing from the findings the review recorded before it read their reports
  (:7582-7583), and that the review reproduced, as the owed rule the owner was shown reads, breaks the run.
- **How this pass's measurement read "a defect", for the owner to decide:** "reproduced" is not in :7582-7583; it is in
  the owed rule the owner was shown ("which the review reproduced and hadn't already recorded") and in `SKILL.md:38`
  ("Reproduce or refute each defect they report"). The rest is this pass's reading of :7582-7583's "defect", which the
  evidence below applies and the owner has not agreed to, so it is not part of the amended test:
  - "the review" is this round's (`SKILL.md:38`, "what your own review has found"), so an earlier round's set-aside
    item that this round's agents re-raise counts unless this round's review recorded it first; no round's class turns
    on it;
  - a reproduced item set aside for another reason, such as inputs no caller sends, counts (:7761, "A reproduced
    defect can still be set aside");
  - a nit, or what the ticket requires, does not, which #69's first round relies on (its round record :14, :16) and
    #78's in part (:18);
  - one the base shares counts when the PR rewrote the code that carries it, as #85's E (its round record :29);
  - where the ticket asks for tests, a survivor whose change a caller can see counts: #71 only adds tests, for
    coverage criteria (`SKILL.md:128`; its round record :2), and #83's criteria include "behaviour unchanged and
    covered by tests" (its round record :2). #71's rounds 8 and 10 to 12 rest on it (its round record :333, :435,
    :509, :565), and so does #83's first round (:41, :47).
- **Owed, and how to check it:** as in 0.40.0's amendment (:7923-7939), for each of the round's agents and not its
  whole-PR agent alone. The deadline the fallback reads is the round record's, else `blind-agents.md`'s 5400 s for a
  round with per-criterion agents. `artifacts/2026-11-26-criteria-revert-owed/scripts/criteria_rounds_owed.py
  [--deadline SECS] <date>:<round>` lists each run's kill, missing report and load words, a re-run included. Read
  every agent's report in full, the pattern's hits being leads only (:7934), and count an agent by its last run in the
  round.
- **Evidence:** the 17 rounds with per-criterion agents that ended between 0.39.0's push (2026-11-24 20:35) and
  2026-11-26, #80's second when its PR closed, with the lines of each round record the reading rests on
  (`artifacts/2026-11-26-criteria-revert-owed/criteria-rounds.tsv`, `streaks-output.txt`, `sensitivity-output.txt`).
  One more, started 2026-11-26 on a private-ticket PR, was still running. A round counts here where its review
  started per-criterion agents. #57's ticket (O3-6248) and #68's (O3-6261) never say "acceptance", and their only
  criteria are an "Expected" section, which their reviews took as the criteria (round records :9, :6;
  `tickets-expected-sections.txt`); read strictly, :7581 leaves both out, which only shortens runs.
  - 9 rounds had an agent owed under 0.41.0's sentence, 27 agents in all: 6 killed at the deadline, 1 that wrote no
    report before the usage limit stopped it, and 20 whose reports name the load and say a run they planned did not
    run or finish because it slowed them, directly or by using up their time. The reports of the agents not marked
    owed were scanned with a wider net than the pattern and each sentence it found read (`scripts/unowed_scan.py`);
    that found sm79-r1-ac1's "too loaded", which the pattern misses.
  - 7 of those 9 break a run anyway, by a defect an agent's report carried; in 6 of them an owed agent carried it.
  - Neither test is met on the record: as written and as amended, the longest run is #57's and #69's first rounds, in
    the order the rounds' agents launched and in the order they last wrote, to the second. The REVERT ON names no order
    for rounds that overlap. The amendment takes #79's and #68's first rounds out of the count. "As written" applies
    the reading above too; read literally, :7582-7583 would also let a report the review did not reproduce break a
    run, which only adds breaks.
  - **Where a reading decides it** (`sensitivity-output.txt`), each reading above taken the other way:
    - Each of three readings read the other way, so that a survivor, a set-aside item or a defect the base shares on
      code the PR rewrote is no defect, meets the REVERT ON as written in at least one order, whether #57's first
      round counts or not. The nit-and-ticket reading read the other way only adds breaks.
    - Two of them, the survivor and base-shared readings, meet the amended test, and only with #57's first round
      counted; left out, as a strict reading of :7581 leaves it, none does.
    - Round by round, five rounds read as finding no defect would each meet it as written, three of them rounds whose
      every agent was owed (#77's, #80's and #83's first); as amended, two would (#71's round 8 and #85's first).
  - **Checks:** `criteria_rounds_owed.py --calibrate` gives the state each round record gives nine control agents
    (three kills, two clean finishes, two usage-limit stops, a load hit, a clean report), and the expected state for
    four synthetic folders that test the summary file; with `--deadline 1800` it finds pr67-r1-whole's kill, which
    the default 5400 does not (`criteria-rounds-owed-deadline-control.txt`). `streaks.py --self-test` passes 8 of 8,
    and each of five mutations of `streaks.py` turns it red (`streaks-mutations-output.txt`).
- **Not in the test, and the owner's:** as at :7952-7959, and here it reaches a counted round. #69's first round counts
  under the amendment though its four agents blame a time limit for runs the load slowed (:7953-7956). It is in the
  longest run, and in both runs of three that a single reading makes under the amendment. #78's first round, in one
  of those, counts with sm78-r1-whole read as not owed: its report names the load as slowing a test class and a build
  its time limit stopped, which it then ran again. With that agent owed, the run #85's E would make falls to two.

**Budget.** No SKILL.md or role file changes, so no budget moves and nothing is retired.

**Gate.** Refuters through `bin/blind-run`:
- **`2026-11-26-retro-1126-refute1`:** dropped 2.5 minutes in, with no report, when the background task that started it
  ended with exit 144. No hook fired, and the cause was not established; #71's round-9 record logs the same exit as
  "cause unknown". Rerun in a session of its own.
- **`2026-11-26-retro-1126-refute1b`:** REVISE.
  - **Measured by the refuter:** the first draft counted a reproduced item that was set aside and told to the user as a
    defect in #80's second round and #6835's third, and not in #77's first, where ac3 reported it on its own criterion.
    Counted alike, neither test is met in either order; the first draft said the record met the REVERT ON as written.
  - **Taken:**
    - one reading of "a defect" in every round, a reproduced one set aside for another reason counting (:7761), which
      moves #77's first round and deletes the first draft's claim;
    - that reading written into **When**, with "reproduced", and named as going beyond the owner's words;
    - #71 round 12's OF1 and E1, and the round that set F1 aside;
    - the set-aside-reason lesson moved out of **Observed**, and the first draft's claim that the owner scoped this
      pass deleted;
    - `criteria_rounds_owed.py` taking rounds as arguments, and its fallback running on a summary that opens otherwise
      (:7929), with four synthetic folders that test it.
  - **Not taken:** marking pr71-r9-lifecycle, pr71-r10-unchanged and sm78-r1-whole owed. Their reports blame a budget
    or a time limit, the gap :7952-7959 names, and no verdict turns on them (`sensitivity-output.txt`).
  - **Added by the proposer after it:** the single-reading check, `scripts/sensitivity.py`.
- **`2026-11-26-retro-1126-refute2`:** REVISE, with every figure re-derived.
  - **Measured by the refuter:** `streaks.py` tested only the longest run, so a met run after a longer run on one PR
    read as not met (four rounds on one PR, a break, three rounds on three PRs).
  - **Taken:**
    - `streaks.py` testing every run, with that case in its self-test and a fifth mutation it catches;
    - :7582-7583's "findings the review recorded" restored in **When**, where the first revision had "what the round
      record held", so an earlier round's set-aside item that this round's agents re-raise counts; a round-12 item
      under **Observed** that rested on the replaced wording, deleted;
    - the nit and ticket-required exclusions named as beyond the owner's words, with the rounds that rely on them, and
      "reproduced" credited to the owed rule the owner was shown;
    - one owed reading for every load report, which marks pr71-r9-lifecycle and pr71-r10-unchanged owed as well;
    - `criteria_rounds_owed.py --deadline`, and a round named once per date.
  - **Its finding on At the bar:** holding the proposal for the next pass breaks no rule, with :1598 and :1628 as
    precedent (withdrawn in round 4: those items were blocked).
- **`2026-11-26-retro-1126-refute3`:** REVISE. It re-ran the calibration, the deadline control and the five mutations,
  and read each of the 19 load sentences marked owed.
  - **Taken:**
    - the reading #85's first round rests on, a defect on code the PR rewrote counting though the base has it too
      (#85's round record :29), written into **When** as the refuter asked, with "code" for its "line" so that a
      reformatted line does not count, and listed beyond the owner's words with "this round's review";
    - #6502's and #83's reproduction lines in the TSV.
  - **Answered, no change:** whether #57's "user-scope half untested" was reproduced and set aside. The review judged it
    no defect, the per-user cache being gone and its removal pinned by M1 (#57's round record :14, :20); the TSV row
    now says so.
- **`2026-11-26-retro-1126-refute4`:** REVISE; the verdict survived its re-run.
  - **Measured by the refuter:** sm79-r1-ac1's report says the machine was too loaded for any of its Maven runs to
    finish, which the pattern misses; the revision's "read each load hit in full" was narrower than 0.40.0's "read the
    report in full" (:7934).
  - **Taken:**
    - sm79-r1-ac1 owed (27 agents, 20 by report), and a wider scan of every unowed agent's report, which found no other;
    - 0.40.0's "read every report in full" restored in **Owed, and how to check it**;
    - the test-gap reading named beyond the owner's words, and the readings put to the owner, with whether they govern
      0.40.0's REVERT ON too;
    - sm78-r1-whole named under **Not in the test**;
    - the rounds counted as under 0.39.0's rule defined, with #57's and #68's expected-behaviour criteria;
    - the **Observed** line on round 12, which said none of its set-asides was outside the ticket, corrected;
    - a clause under **At the bar** citing :1598 and :1628 as precedent, deleted: those items were blocked, not held.
- **`2026-11-26-retro-1126-refute5`:** REVISE, with no verdict changed.
  - **Measured by the refuter:** both amended single-reading runs depend on #57's first round; #83's first round rests
    on the test-gap reading too, so the round-by-round list counted that reading twice.
  - **Taken:** the readings' effect stated reading by reading (`sensitivity.py`'s second part) as well as round by
    round, with #57's dependence; #83 under the test-gap reading, with `SKILL.md:128`'s definition; the **Observed**
    wording; "ended" for #80's second round; refute2's withdrawn precedent marked.
  - **Checked by the proposer after it:** #57's and #68's tickets on the public tracker, which have an "Expected"
    section and none named acceptance criteria, put to the owner.
- **`2026-11-26-retro-1126-refute6`:** REVISE, with no verdict changed.
  - **Measured by the refuter:** #83's and #68's first rounds last wrote in the same minute, and to the second #83's
    comes first, which drops one last-write line from the round-by-round sensitivity.
  - **Taken:**
    - the TSV's times to the second (`scripts/round_times.py`), and both scripts re-run;
    - #83's untested outputs credited to `SKILL.md:126`, so the survivors reading covers #71's tests-only rounds
      only;
    - the readings beyond the owner's words moved out of **When**, which now holds only what the owner was shown, as
      0.40.0's amendment did (:7920-7922), into a bullet the owner decides;
    - "as written" said to apply the same reading;
    - the reason for holding the set-aside-reason proposal stated as the proposer's choice, and its timing put to the
      owner.
- **`2026-11-26-retro-1126-refute7`:** REVISE, two small objections, no figure wrong and no verdict changed.
  - **Taken:** its wording for **When**, which credits "reproduced" to the owed rule the owner was shown and not to
    :7582-7583; and #83's first round back under the survivor reading, its ticket asking for "covered by tests", where
    round 6 had credited `SKILL.md:126`, which covers a behaviour change the PR introduces. Re-run, the survivor reading
    meets the test as written without #57's first round too.
  - **No eighth round:** both changes are the refuter's, the first its own words and the second one of the two options
    it gave, re-run through `sensitivity.py`; the 2026-11-24 and 2026-11-25 passes stopped on the same ground
    (:7828, :8033).

**At the bar, not proposed in this pass:** a set-aside reason that names a test which pins something else, found when a
blind reviewer re-raised the item: #71 round 7's B4 ("the two-role test pins the APPROVE-gated ones"; that test uses a
receipt, which cannot be dispatched), and round 12's F1 (PL2 pins `getCompleteLocationTree`, not the method's own child
mapping and tag filter) and record filters (RP1 pins `if (!hasPrivilege)` only). 2 run records, two separate events, so
Step 3 says it earns a proposal; round 7's half was logged as observed at :7676-7677, and pr-review has no sentence on
checking a set-aside reason. Not proposed in this pass, by the proposer's choice: this pass applies one owner
direction, and a new pr-review rule needs its own design, budget and refutation rounds. The proposal is owed to the
next pass.

**Observed, not yet actionable.** None new. Round 12 set O3-6311's three report filters aside as a follow-up (#71's
round record :531, :547). No maintainer has raised them, which the REOPEN ON of "seen, then set aside as outside the
ticket" needs (:6043-6044), so that count is unchanged.

**Still the owner's:**
- whether to direct a change to pr-review :172's reach clause now, on one measurement, instead of waiting for its
  REOPEN ON;
- `kill-session-scratchpad-procs.sh`: push a fixed draft or remove it (2026-11-05 entry), and with it the
  working-directory match a 2026-11-22 pass dropped;
- publishing the 2026-11-13 bash 5 paragraph of the user-level `CLAUDE.md`;
- whether "push the fix" also replies in that comment's thread;
- the ninth pass's posting on their own PRs, the CI half of the exploit rule, its `~/.m2` install guard and per-PR
  claim note, and the Action's "post nothing" when an author has asked a question;
- whether a load threshold read from `blind-run-load.tsv` makes an agent owed, once Q1's records show the load at which
  agents lose their runs; #69's first round, above, is a round it would decide;
- whether a round that posts other comments anyway also posts the verified nits it would set aside (#71 round 9);
- whether the reading of "a defect" this pass's measurement used becomes part of 0.39.0's amended REVERT ON, the
  survivor and base-shared readings each deciding its verdict on the record when #57's first round counts, and
  whether it also governs 0.40.0's (:7782-7784), which uses the same words;
- whether a ticket whose only criteria are an "Expected" section lists acceptance criteria, for pr-review's
  per-criterion agents and for this REVERT ON: #57's and #68's reviews started agents on one (O3-6248, O3-6261);
- whether to run the pass the set-aside-reason lesson above earns now, rather than at the next retro.

**Running parked counts:**
- "Seen, then set aside" as outside the ticket is unchanged: run records 2, measurement sightings 12.
- A set-aside reason naming a test that pins something else: 2 run records (#71 rounds 7 and 12), at the bar.
- Every blind agent stays; 0.39.0's and 0.40.0's REVERT ONs now both leave out rounds with an owed agent.
- P-A is unchanged.

## 2026-11-26 (second owner-directed pass: `--no-blind` turns pr-review's blind agents off for a round) — 1 applied; two refutation rounds, six blind probes of the text; linter 18 files, 0 findings; **pr-review 0.42.0**

**Evidence read.** No record has been written since this morning's pass, whose push left every file the repo holds
under `.claude/` matching its live copy but the user-level `CLAUDE.md` (still the owner's).

**The direction.** The owner asked: "Just for the sake of testing, can we make blind runs the default but include an
optional parameter for turning them off when one wants to?" Step 2's blind agents have been the default outside CI
since pr-review 0.40.0 (:7736-7737), so the change is the parameter.

**Applied, owner-directed: pr-review 0.42.0, `--no-blind`.**
- **The rule.** The arguments line: "With `--no-blind`, or when the user asks for a review without blind agents, Step 2
  starts none this round; say so in the round record, and owe none for it, while an agent an earlier round owes is not
  run and stays owed. Step 3's blind readers, of a finding or of a fix you recommend, still run." Step 2's blind-agent
  paragraph names it beside pr-harden's exemption, and `blind-agents.md`'s opening sentence, which said the whole-PR
  agent starts every round, adds "unless the user turned them off". The argument hint lists it.
- **Beyond the owner's words, by the proposer:** a plain request for a review without blind agents works as the flag
  does. The owner asked for "an optional parameter"; on 0.41.0 such a request was undecidable, read literally still
  four agents (`probe-current-report.md`, scenario D).
- **What it covers.** Step 2's agents, the whole-PR one and the per-criterion ones: the agents the owner's 2026-11-25
  question called "blind runs" ("we recently added blind runs to the pr-review skill", in this session's transcript)
  and the cost review measured. Step 3's blind readers, of a finding (:136) and of a fix the review recommends (:145),
  keep running, and the arguments line says so, since drafts without that left readers unsure (below); whether the
  flag should stop them too is put to the owner.
- **For one round.** The next round starts its agents unless the flag is passed again, and owes none for the round
  that went without. An agent an earlier round owes is not run in a round under the flag and stays owed, the branch
  of Step 1's owed items (`SKILL.md:22`, "or say it is still owed") the arguments line now names.
- **The REVERT ONs.** A round that started no Step 2 agent, by `--no-blind` as by the CI and pr-harden exemptions, is
  not a round under 0.39.0's REVERT ON (:7581-7583, as amended at :8102-8108) or 0.40.0's (:7781-7787, as amended at
  :7916-7922): it neither counts toward the three nor breaks a run. Nor is it a round under the 2026-11-25 REOPEN ONs
  for opt-in agents and for dropping the whole-PR agent (:8048-8052): it neither extends nor breaks their runs, and its
  cost is not in a share measured for them. 0.40.0's amended text already counts only rounds whose whole-PR agent
  reported, so for it this adds only "nor breaks a run". 0.39.0's needs it: this morning's amendment, "counting only
  rounds in which each of the round's Step 2 agents reported and none is owed", would hold of a round with no agent at
  all, a CI or pr-harden round's included.
- **Not the killed opt-in.** REJECTED.md's 2026-11-25 section killed making the agents opt-in (:8048-8049), on their
  share of the spend and the posted comments resting on them. The default stays on here; the flag turns them off where
  the owner asks.
- **Measured** (`artifacts/2026-11-26-no-blind-probe/`): six blind agents that read only the skill text answered
  scenarios. On 0.41.0, the control, `--no-blind` and a plain "skip the blind agents" were undecidable, read literally
  still four agents. On the first draft, both started none, recorded it and owed none, and the next round started its
  agents again; the plain request left open whether Step 3's fix refuter stops too. On the second, two scenarios the
  first refuter raised were undecided: an earlier round's owed agent under "owe none", and Step 3's reader of a
  finding. The third settled both, with a tension the second refuter also found: `SKILL.md:22` still let an owed item
  be run. On the final text, all eight are decided but one 0.41.0 leaves silent as well: what a pr-harden reviewer's
  round record says about the agents it does not start. The README gives the sentence each probe read.

**Budget.** pr-review/SKILL.md goes from 10426 to 10493 words and `blind-agents.md` from 332 to 339, each with a
`raises` entry. Nothing is retired: the flag adds a case to an exemption the paragraph already has.

**Gate.** Refuters through `bin/blind-run`:
- **`2026-11-26-retro-1126c-refute1`:** the skill change STANDS, on the 10473-word draft, before the arguments line
  gained its owed and Step 3 sentences; the section REVISE.
  - **Measured by the refuter:** the linter, 0 findings with the proposed files and budgets and 2 with the proposed
    files and the old budgets, its control; the word counts; the citations.
  - **Taken:** the 2026-11-25 REOPEN ONs that count rounds (:8048-8052); 0.40.0's REVERT ON cited as amended, with the
    reason this morning's 0.39.0 amendment needs the sentence and 0.40.0's does not.
  - **Probed, then taken:** its two suspected gaps, an earlier round's owed agent under the flag and Step 3's reader of
    a finding (`probe-gh-report.md`), which the arguments line now decides.
  - **Kept, with its reason:** the CI and pr-harden rounds in the REVERT ON sentence, which the refuter called beyond
    the ask; this morning's amendment would otherwise count them.
- **`2026-11-26-retro-1126c-refute2`:** the skill change STANDS; the section REVISE, with every count, citation and the
  linter re-run.
  - **Taken:** "is not run and" before "stays owed", its alternative to a section fix, re-probed
    (`probe-final2-report.md`); refute1's verdict qualified by the draft it judged; the plain-request trigger named as
    the proposer's, with its reason; "final text" where the section said "as committed"; each probe's sentence in the
    README.
  - **No third round:** the skill change is the refuter's own words, re-probed on all eight scenarios, and the rest
    corrects the section's account of the gate; the earlier passes stopped on the same ground (:7828, :8033).

**Observed, not yet actionable.** None new.

**Still the owner's:**
- whether `--no-blind` should also stop Step 3's blind readers, of a finding and of a fix the review recommends;
- whether to direct a change to pr-review :172's reach clause now, on one measurement, instead of waiting for its
  REOPEN ON;
- `kill-session-scratchpad-procs.sh`: push a fixed draft or remove it (2026-11-05 entry), and with it the
  working-directory match a 2026-11-22 pass dropped;
- publishing the 2026-11-13 bash 5 paragraph of the user-level `CLAUDE.md`;
- whether "push the fix" also replies in that comment's thread;
- the ninth pass's posting on their own PRs, the CI half of the exploit rule, its `~/.m2` install guard and per-PR
  claim note, and the Action's "post nothing" when an author has asked a question;
- whether a load threshold read from `blind-run-load.tsv` makes an agent owed, once Q1's records show the load at which
  agents lose their runs; #69's first round in this morning's section is a round it would decide;
- whether a round that posts other comments anyway also posts the verified nits it would set aside (#71 round 9);
- whether the reading of "a defect" this morning's measurement used becomes part of 0.39.0's amended REVERT ON, the
  survivor and base-shared readings each deciding its verdict on the record when #57's first round counts, and
  whether it also governs 0.40.0's (:7782-7784), which uses the same words;
- whether a ticket whose only criteria are an "Expected" section lists acceptance criteria, for pr-review's
  per-criterion agents and for 0.39.0's REVERT ON: #57's and #68's reviews started agents on one (O3-6248, O3-6261);
- whether to run the pass the set-aside-reason lesson in this morning's section earns now, rather than at the next
  retro.

**Running parked counts:** unchanged from this morning's section.

## 2026-11-26 (third owner-directed pass: a listed acceptance criterion the head misses by its words is raised, not set aside) — 1 applied; 1 candidate measured and not proposed; three refutation rounds; linter 18 files, 0 findings; **pr-review 0.43.0**

**Evidence read.** Before the read, the store matched `origin/main` at `3efa90fc` but for one new record and the
`__pycache__` file earlier sections name. `confidential/` was read for its `PRS` only; neither PR this pass measures is
listed. The one record since `LAST`:
- `2026-11-26-pr-review-openmrs-module-stockmanagement-85.md`, the record of stockmanagement #85 round 1 (head
  8f7bb30, review session 04a649fa). It was written in session 7abec84a after the owner asked why that round missed what
  the ticket's author raised on the PR (issuecomment-7185869450).

**The direction.** The owner asked "fix the pr-review skill so this doesn't happen again" (session 7abec84a,
2026-11-26T09:49Z), after the session's answer to their question about #85.

**Applied, owner-directed: pr-review 0.43.0.**
- **The rule.** The end of Step 2's "The ticket is evidence, not a spec" paragraph gains one sentence: "Either way, a
  listed acceptance criterion the head misses by its words is raised, unless the thread has since settled it: where
  you read the ticket's purpose as met, ask, naming the criterion and what the head does instead, since what the ticket
  meant is for its author to say."
- **A related parked entry, not counted.** "Seen, then set aside as outside the ticket" (:6037-6044) does not take #85
  round 1. A missed acceptance criterion is blocking (`SKILL.md:47`), and its REOPEN ON asks for a non-blocking defect.
  The round set the point aside as met by the ticket, not as outside it; #71 round 9 was left uncounted for its reason
  (:8037-8039). Two of the round's four blind agents reported that `ModuleConstants`' six static helpers miss O3-6352's
  first criterion, "No static settings utility remains". The round's own pass had not raised it. It set their point
  aside on its own reading ("they parse values and log, do not read settings, so the ticket's Why objections do not
  apply"), and the ticket's author then raised it. Detection was not the miss, so this edit is a filing-judgment change
  of the kind :6408-6409 and :7845 name, narrowed to criteria.
- **Measured at #85 round 1's decision point** (`artifacts/2026-11-26-pr85-criterion-filing-probe/85/`; pre-registered,
  blinded, scored before unblinding, n=5 per arm). The probe gives each run the frozen head, the thread and ticket as
  they stood at the round's start, the four agents' reports and the round's four drafted comments, then asks it to
  decide nine undecided items. pr-review 0.42.0 filed the criterion miss in 0 of 5 runs; with the sentence, 5 of 5,
  each as a question naming the criterion and what the head does. Neither arm filed any of the four noise items
  (0 of 5 each). The 0.42.0 runs set it aside with the real round's reasons: the Why-section reading of :83, and :79's
  "Cost missing, it's taste", which p85-04 put as "taste with no cost to name".
- **Held out on openmrs-core #6502** (`…/6502/`; the probe's "round 1" is the 11-25 round's decision point, the sixth
  review of that PR), where an agent reported TRUNK-6666's fifth criterion missed
  and the round set it aside. Both texts filed it in 5 of 5, noisy in 0 of 5. With the round's own earlier-round notes on
  it added verbatim (wave 2), 5 of 5 again for both. So the sentence changed nothing where 0.42.0 already files the miss.
  The held-out case hit the ceiling and measured nothing about the sentence's effect.
- **Budget:** `pr-review` 10493 -> 10543 words, raised. It retires no rule. It narrows two for listed acceptance
  criteria: :83's "a PR that solves it differently from what the ticket proposed is fine", and :79's "Cost missing,
  it's taste; keep it to yourself". Those are the two the 0.42.0 runs cited.
- **REVERT ON:** two run records in which the ticket's author dismisses a criterion question posted under this sentence
  as not what the ticket meant; or a run record of a round under it that sets aside a criterion miss an agent reported,
  which a maintainer then raises.

**What the measurement does not show.**
- #85 is the case the sentence was written on, and n=5. The probe's item text says "by its letter", which the
  sentence's "by its words" echoes.
- The probe starts after the agents have reported. The sentence acts only on a miss someone has noticed, and #85's own
  pass had not.
- Two branches were never exercised. One is "unless the thread has since settled it". The other is a criterion missed
  by its words that a maintainer would not want raised, which is the sentence's own way of filing too much. No round on
  record has either case: all eight round records that mention criteria were searched. Each **needs the
  implementation**, the sentence running in real rounds, and the REVERT ON above watches for it.
- "What the ticket meant is for its author to say" holds on #85, where the author is the maintainer reviewing the PR.
  It is not shown in general.

**Measured and not proposed: a blind reader of the round's set-aside reasons** (`…/85/`, runs p85r-01 to p85r-05, given
round 1's real reasons). It upheld the reason for setting the criterion miss aside in 5 of 5. The pre-registered bar
was rejecting it in at least 4 of 5. So it would not have prevented #85.

**Rejected shapes, with their evidence.**
- **Leaving it to the report to the user.** #85's point reached the owner, framed "a maintainer's call", and was not
  posted before the ticket's author raised it 32 hours later. #6502's criterion reached the owner in six `pr-review` rounds
  launched on 10-22, 10-23, 11-10, 11-11, 11-12 and 11-25 (sessions 5ccf7c6c, 97874f38, d68355a0, 03d30d8a, 93793db1,
  792919b5), none of which raised it ("Earlier rounds chose not to raise it", 11-25), and was never posted. The probe's
  PROTOCOL.md:5 lists five: it omits 11-10 and dates 10-23's round by its 10-26 report.
- **More reviewers.** #85's miss was not detection: two of four agents found it.
- **:79 alone.** The 0.42.0 runs also cite :83's reading.
- **Reframing the per-criterion brief.** #85's criterion agent reported the miss under a brief that told it to judge
  the criterion against the ticket's Why section.

**Gaps left open (round 1's refuter, not blocking).**
- **The round's own drafts.** #85 round 1 also posted comment B, asking the booleans to warn "the way parseUomPriority
  does". The author answered with another static helper, `ModuleConstants.parseBoolean` (bc98c50). Under the sentence,
  the same round would ask whether the helpers meet the criterion while asking for one more. No clause checks the
  review's own drafts against a criterion. One would be unmeasured wording (:6033).
- **The REVERT ONs.** How #85 round 1 counts under 0.39.0's and 0.40.0's REVERT ONs turns on the reading of "a defect"
  that is still the owner's (:8109-8124). The round confirmed the six helpers exist and judged the criterion met, and
  that reading does not say whether such a point counts as reproduced (:8116-8117). Its "what the ticket requires"
  (:8118) is not this case: it covers an agent reporting, as a defect, behaviour the ticket prescribes (#69's round record
  :14, :16; #78's :18), not a miss of a criterion. Both refutation rounds read :8118 the other way.

**Observed, not yet actionable.**
- **#6502's criterion** was set aside in those six real rounds. Yet a fresh reviewer at that round's decision point files it
  in 10 of 10 under either text, also with the round's earlier notes inserted. What carried the real set-asides is not
  reproduced. On the record are the round's own decision before it read the agents ("prior rounds chose not to raise;
  no new evidence"), and a brief that told an agent "No metric/counter is required (ticket AC 5)" under "Already decided
  by the maintainers". No thread decided that; a refuter found so on 11-10. 1 PR.
- **#85 round 1's other set-aside reasons, as the five readers judged them.** The reason for the import move (R9, "part
  of the import edit the PR has to make") is false: master already imported `Context`, and the PR needed only the
  `ModuleConstants` import (checked against the diff). The readers also rejected the reasons for R3 (defaults copied to
  many call sites) and R7 (an invalid email dropped silently) in 5 of 5. No maintainer has raised either. This is a
  measurement sighting for the at-the-bar set-aside-reason lesson (#71 rounds 7 and 12), not a run record.
- **The probe's scorer** first counted braces without JSON's string rules, and two #6502 reports holding "a `{`
  prefix" did not parse. It now uses `json`'s `raw_decode`, calibrated on that case, and every #85 score was re-derived
  identically. The 2026-11-23 #6535 probe's scorer had the same shape; all 20 of its reports re-score identically
  (`…/6535-rescore.txt`).

**Refutation.**
- **Round 1** (`retro-1126d-pr85-refute1`, 3.1 min): apply as is. It found no blocking objection and asked this entry
  to record the parked count, what the measurement does not show, the two gaps and a REVERT ON, all above.
- **Round 2** (`retro-1126d-pr85-refute2`, 2.9 min): apply with corrections, all in this section and the commit
  message. Taken: a quote of :79 that no file holds; "six" and "five" rounds in one section; the parked count, which
  this section first moved to 3 run records. Not taken: dropping the 11-10 round, which it could not check (session
  d68355a0 launched `/pr-review` on #6502 at 2026-11-10T09:23Z and reported "AC 5's metric. … I still didn't raise it"); and its reading of :8118 (above).
- **Round 3** (`retro-1126d-pr85-refute3`, 2.2 min): commit with corrections. It accepted both departures from round 2
  on their evidence (the script's output for d68355a0; #69's and #78's round records). Taken in its words: a quote of
  d68355a0 that its evidence did not show; "each listing it as not raised or not posted", which two sessions' lines do
  not say; this section's label for #6502's round; "refuters" in the commit message. It wrote two scratch files outside
  its folder, extracts of its inputs, and deleted them (both checked gone).
  - **No fourth round,** since the corrections are its own words narrowing text the earlier rounds settled, as at :7828.

**Still the owner's:**
- whether `--no-blind` should also stop Step 3's blind readers, of a finding and of a fix the review recommends;
- whether to direct a change to pr-review :172's reach clause now, on one measurement, instead of waiting for its
  REOPEN ON;
- `kill-session-scratchpad-procs.sh`: push a fixed draft or remove it (2026-11-05 entry), and with it the
  working-directory match a 2026-11-22 pass dropped;
- publishing the 2026-11-13 bash 5 paragraph of the user-level `CLAUDE.md`;
- whether "push the fix" also replies in that comment's thread;
- the ninth pass's posting on their own PRs, the CI half of the exploit rule, its `~/.m2` install guard and per-PR
  claim note, and the Action's "post nothing" when an author has asked a question;
- whether a load threshold read from `blind-run-load.tsv` makes an agent owed, once Q1's records show the load at which
  agents lose their runs; #69's first round in this morning's section is a round it would decide;
- whether a round that posts other comments anyway also posts the verified nits it would set aside (#71 round 9);
- whether the reading of "a defect" this morning's measurement used becomes part of 0.39.0's amended REVERT ON, the
  survivor and base-shared readings each deciding its verdict on the record when #57's first round counts, and
  whether it also governs 0.40.0's (:7782-7784), which uses the same words; and whether, under 0.43.0, a criterion
  miss an agent reported and the round posted counts as a defect for both;
- whether a ticket whose only criteria are an "Expected" section lists acceptance criteria, for pr-review's
  per-criterion agents, for 0.43.0's criterion sentence and for 0.39.0's REVERT ON: #57's and #68's reviews started agents on one (O3-6248, O3-6261);
- whether to run the pass the set-aside-reason lesson in this morning's section earns now, rather than at the next
  retro.

**Running parked counts:**
- "Seen, then set aside" as outside the ticket is unchanged: run records 2, measurement sightings 12. #85 round 1 and
  the five 0.42.0 runs at its decision point set a criterion miss aside as met, not as outside the ticket (above).
- A set-aside reason naming a test that pins something else: 2 run records (#71 rounds 7 and 12), unchanged. One
  measurement sighting of a false set-aside reason is added (#85's R9).
- The others are unchanged from this morning's section.

**Added after the push of 04435e8e, not refuted:** a record written during this pass, after its read, is not one this
pass read for its proposals or counted: `2026-11-26-pr-review-openmrs-module-stockmanagement-86.md` (stockmanagement
#86 round 1: a consumer read of a removed field, set aside as an owed check for a future frontend PR, then raised by a
maintainer). It is the next retro's, which should count it under "count only the records REJECTED.md does not already
name" as not yet counted.

## 2026-11-26 (fourth owner-directed pass: pr-review settles the design first, for the ticket and the plan it belongs to, and holds back a drafted comment that would add more of what is in question) — 1 applied; 2 wordings measured and not proposed; three refutation rounds; linter 18 files, 0 findings; **pr-review 0.44.0**

**Evidence read.** Before the read, the store matched `origin/main` at `39631bd4`: 971 files identical, none missing, none
local-only but `confidential/`, a `.DS_Store` and the `__pycache__` file earlier sections name. `confidential/` was read for its `PRS`
only; neither PR this pass measures is listed. One record had not been counted:
- `2026-11-26-pr-review-openmrs-module-stockmanagement-86.md`, stockmanagement #86 round 1 (head eb4a0e8), which the
  third 2026-11-26 section left for this retro (:8529-8533). It is counted under the parked counts below.

**The direction.**
- The owner relayed advice from Wikum, who reported O3-6352, after #85: that the review "take a holistic look at the PR,
  not just the code". The owner's message adds: "It's important that we confirm people are making the right design
  choices" (session 7abec84a, 2026-11-26T13:19Z).
- The owner then directed: "yes close the pr-review gap by first state whether the design is right for the ticket and the
  plan it belongs to, and hold back any drafted comment that adds more of what's in question" (13:40Z).
- Both are in `artifacts/2026-11-26-design-first-probe/owner-words.txt`, extracted from the transcript.

**Applied, owner-directed: pr-review 0.44.0.** Three edits:
- **Step 1 (:17)** asks the ticket's `parent` and `issuelinks` with the rest, and adds, after its private-ticket rules:
  "Read the plan the ticket belongs
  to as well: its `parent` ticket, read the same way, with its description and comments, and the summary of each ticket
  in its `issuelinks`, or on GitHub the issue's `parent` (`gh api graphql`) and the issues it says it blocks or waits
  on." Both Jira fields were checked on O3-6352 (parent O3-6306, 8
  links), and GraphQL's `Issue.parent` on cli/cli; a made-up field name errors.
- **Step 2's opening (:30)** replaces "settle the approach before verifying the details" with: "settle the design first:
  before any line-level finding, decide whether it is right for the ticket, and whether it is right for the plan the
  ticket belongs to, naming that plan or saying Step 1 found none, and, outside CI, write both verdicts in the round
  record and open your report to the user with them."
- **:85** gains "It is in question once a finding or a question you post says so." It also gains "including every
  drafted comment that would add more of what is in question, however sound its own point", and the case: "On
  stockmanagement #85, a request that two settings warn "the way parseUomPriority does" was answered with one more static
  helper, while the ticket asked that no static settings utility remain."
- **What it rests on.**
  - #85 round 1 (8f7bb30) found the design points and reported them to the owner only. It posted four line-level
    comments.
  - Comment B asked two settings to warn "the way parseUomPriority does". The author answered by adding `parseBoolean`
    to the `ModuleConstants` helpers (bc98c50), which the ticket's author then called "worse than what we're replacing"
    (issuecomment-7185869450).
  - The third 2026-11-26 section left this open as "The round's own drafts" (:8460-8463).
  - The author's current head (ae609b2) has no helpers left and reads every setting through core.
- **Budget:** `pr-review` 10543 -> 10703 words, raised. It retires no rule. Step 2's opening sentence is replaced, not
  added to, and :85's "hold the rest" now names what it holds.
- **REVERT ON:** two run records in which a maintainer says a comment held back under :85's new clause should have gone
  out that round. Or one in which a round's verdict on the plan led it to post an objection the ticket's author calls
  outside the ticket.
- **REOPEN ON** (for a clause that holds without its #85 sentence): one run record in which a round under 0.44.0 posts a
  drafted comment that adds more of a design point its own verdict or posted question has put in question, and the
  author answers with more of it.

**Measured** (`artifacts/2026-11-26-design-first-probe/`; PROTOCOL.md, each wave pre-registered before it ran and its
result appended before the next; scored before unblinding; audited, 0 flags after a calibrated re-audit; n = 5 per arm
per case per wave).
- **The probe.** Each run gets the frozen head, the snapshots as they stood when the round began, the agents' reports
  and the round's drafted comments, and decides:
  - what to file;
  - what to post, reword or hold;
  - its report's first three lines.
- **The cases.** #85 round 1 (21:31Z) is the target. #79 round 1 (21:25Z) is the control: a maintainer had approved its
  design, and both of its drafts are sound.
- **The arms.**
  - C: 0.43.0.
  - T: the first wording, a single verdict "for the ticket and for the plan".
  - T2: the applied text.
  - T3: T2 without the #85 sentence.
- **Holding #85's comment B.** C 0 of 15. T3, the clause without the sentence naming #85, 2 of 5. T and T2, with that
  sentence, 15 of 15. All 15 name that sentence in their report, 12 of them in their stated reason (pd-01, pe-13
  and pf-20 in their text). pf-16, for one: "the request the method records this PR answering with
  one more static helper". No arm held A, C or E (0 of 35). Every run filed the criterion question R1 (35 of 35).
- **The plan in the opening.** C 0 of 30; T 0 of 10, though all 20 runs of that wave opened the epic file; T2 20 of 20;
  T3 10 of 10.
- **Noise at #85.** R9, the one-line import nit, was the only noise item filed: C 5 of 15; T and T2 2 of 15; T3 0 of
  5.
- **Over-holding at #79.** Waves 1 and 2 gave the drafts without the round's blind reads. There the new text held B in 3
  of 10, and C in 0 of 10. All three cited the blind read Step 3 (:145) asks of a fix before posting, which the probe
  forbade; the real round had run it (sm79-r1-refute-B). Wave 3 told the runs which blind reads each draft had passed,
  as both round records show. Then C held A in 1 of 5, and T2 and T3 held nothing (0 of 10). No arm filed a #79 noise
  item (0 of 35); among them was S2, the shared `formatQuantity` helper, a helper the ticket removes.
- **The usage limit.** Wave 3 ran into the account's five-hour usage limit at 14:26Z, and 14 of its 30 runs ended
  mid-review. They were rerun under new names in the same cells after the limit reset (PROTOCOL.md says so before the
  reruns).

**What the measurement does not show.**
- **The hold's reach beyond #85.** The hold at #85 comes from the sentence that names #85. Without it (T3) the clause
  held B in 2 of 5. So on a PR the sentence does not describe, the clause alone may hold little. **Needs the
  implementation:** the records hold no other round whose own design question met its own drafted comment, and the
  REOPEN ON above watches for one.
- **The decision point.** The probe starts with the drafts written. The verdict-first order of Step 2 also acts earlier,
  on what a round drafts at all, and that is not measured.
- **The verdict's correctness.** The plan verdict was scored for being there, not for being right. Nothing shows it
  changed a filing at #79. **Needs the implementation.**
- **What a held draft loses.**
  - B asked for a ticket requirement: O3-6352's Details say "A setting that cannot be parsed should be logged, not
    silently ignored". It is still unmet at ae609b2 (round record :59).
  - All 15 T and T2 holds deferred B whole. None asked for the logging without the helper, and the probe's labels
    counted such a reword as posting, so they could not tell the two apart.
- **A re-review.** :87 reopens an approach the thread settled only on a changed head or new evidence. A round's plan
  verdict could be offered as that evidence. Neither case was a re-review. **Needs the implementation.**
- **Sample size.** n = 5 per arm per case per wave, on two cases, both stockmanagement PRs in one epic.

**Measured and not proposed.**
- **T, one verdict "for the ticket and for the plan".** It named the plan in 0 of 10 openings: the runs gave a ticket
  verdict and dropped the plan.
- **T3, T2 without the #85 sentence.** It held B in 2 of 5, failing the pre-registered at-least-4.

**Rejected shapes, with their evidence.**
- **Posting the design verdict on the PR.** :85 says an approach you agree with earns no comment, and :157 bans
  architecture grading in the body. Whether an agreeing verdict should be posted is put to the owner below.
- **A line in `posting.md`'s gate.** It was not needed. The applied text alone put the verdict, the plan named, in 20 of
  20 openings.
- **More blind agents, or a blind design reader.** #85's design point was found by two of four agents (:8408); detection
  was not the miss.
- **Doing nothing after 0.43.0.** 0.43.0 files the criterion question, 15 of 15 here, and still posts B, 15 of 15.

**Observed, not yet actionable.**
- **A record written during this pass, after its read:** `2026-11-26-pr-review-openmrs-module-stockmanagement-79.md`.
  #79 round 1 set aside an agent's "testing the JDK's `DecimalFormat`, not the module", and the maintainer then asked to
  drop that test class. It is the next retro's, under "count only the records REJECTED.md does not already name".
- **The probe's own lesson.** A run told it cannot start agents may hold a sound fix whose blind read is owed (:145), and
  that shows as over-holding. A probe's drafts say which blind reads each draft has passed. 1 probe.
- **The probe's lock lists** took every `~/.claude/blind-runs/2026-11-26-*` folder, other sessions' included. Among them
  were four of another session's refuter folders for a private PR, locked during waves 1 and 2 and restored after each.
  The copies of those lists in this pass's artifacts leave those lines out. A probe locks the folders it names, not a
  date's. 1 probe.

**Refutation.**
- **Round 1** (`retro-1126e-design-refute1`, 3.4 min): apply with corrections, nothing blocking. Taken:
  - "A 404 there" could read as the GitHub call the new sentence ended on. The plan sentence now follows the
    private-ticket rules instead, so "there" is the REST URL again and the word count stays 10693.
  - The hold figure now leads with T3's 2 of 5, and says how many T and T2 holds cite the #85 sentence.
  - A REOPEN ON for the under-hold, which the REVERT ON could not see.
  - Wikum's role and words, as the files show them.
  - The noise split by arm.
  - PROTOCOL.md's wave 3 result.
  - The script behind `audit4.txt`, saved and re-run (0 flags).
- **Not taken from round 1:**
  - "2 of 20 means T and T2 only": the 20 are T, T2 and T3, now split above.
  - Its note that a minor criterion question could hold every draft: no arm held A, C or E (0 of 35), so no change.
- **Round 2** (`retro-1126e-design-refute2`, 3.7 min): apply with corrections, nothing blocking. Taken, in its words
  where it gave them:
  - Step 1 reads the parent ticket "the same way, with its description and comments", which is what the probe's runs
    were given. It also puts a parent only the MCP can read under :17's private-ticket chain. This adds 10 words, so
    10693 -> 10703.
  - All 15 T and T2 holds of B name the #85 sentence (three in their report's text).
  - What a held draft loses, and the re-review case, recorded above.
  - "Still the owner's" now cites :176 for the reach clause. The live :172 is the note bullet, so the old reference,
    carried from :7833 on, was stale.
- **Round 3** (`retro-1126e-design-refute3`, 4.0 min): apply, nothing blocking.
  - It ran the checks left owed: the linter on the final text (18 files, 0 findings), the store against the local
    `origin/main` ref, the wave-3 audit (0 flags) and every figure above.
  - Taken in its words:
    - :85's trigger is now "a finding or a question you post", not "your verdict". A verdict :79 keeps off the PR as
      taste would otherwise hold drafts behind a direction the author was never told about. In the probe, R1 was
      posted as a question in 35 of 35 #85 runs, so the narrower trigger is the measured one. Word-neutral.
    - The store's `.DS_Store`.
    - The precedents for counting #86, and what the count rests on.
  - Not taken: "checked against the local `origin/main` ref without a fetch". That describes its own re-check; this
    pass's check fetched first.
  - **No fourth round,** since the corrections are round 3's own words narrowing text the earlier rounds settled, as at
    :7828.

**Still the owner's:**
- whether `--no-blind` should also stop Step 3's blind readers, of a finding and of a fix the review recommends;
- whether to direct a change to pr-review :176's reach clause now, on one measurement, instead of waiting for its
  REOPEN ON;
- `kill-session-scratchpad-procs.sh`: push a fixed draft or remove it (2026-11-05 entry), and with it the
  working-directory match a 2026-11-22 pass dropped;
- publishing the 2026-11-13 bash 5 paragraph of the user-level `CLAUDE.md`;
- whether "push the fix" also replies in that comment's thread;
- the ninth pass's posting on their own PRs, the CI half of the exploit rule, its `~/.m2` install guard and per-PR
  claim note, and the Action's "post nothing" when an author has asked a question;
- whether a load threshold read from `blind-run-load.tsv` makes an agent owed, once Q1's records show the load at which
  agents lose their runs; #69's first round in this morning's section is a round it would decide;
- whether a round that posts other comments anyway also posts the verified nits it would set aside (#71 round 9);
- whether the reading of "a defect" this morning's measurement used becomes part of 0.39.0's amended REVERT ON, the
  survivor and base-shared readings each deciding its verdict on the record when #57's first round counts, and
  whether it also governs 0.40.0's (:7782-7784), which uses the same words; and whether, under 0.43.0, a criterion
  miss an agent reported and the round posted counts as a defect for both;
- whether a ticket whose only criteria are an "Expected" section lists acceptance criteria, for pr-review's
  per-criterion agents, for 0.43.0's criterion sentence and for 0.39.0's REVERT ON: #57's and #68's reviews started agents on one (O3-6248, O3-6261);
- whether to run the pass the set-aside-reason lesson in this morning's section earns now, rather than at the next
  retro;
- whether a design verdict that agrees with the PR is posted on it: 0.44.0 keeps it to the round record and the report
  to the user (:85, "an approach you agree with earns no comment"), where the owner's message relaying Wikum's advice asks that
  reviews "confirm people are making the right design choices".

**Running parked counts:**
- "Seen, then set aside" as outside the ticket: run records 2 -> 3, with #86 round 1.
  - #86 round 1 noted that the current frontend's type filter matches `undefined === undefined` on the head. It filed
    that as "an owed check for a future frontend PR", a set-aside to outside the PR, and a maintainer then raised it
    (r4707232941). That meets the REOPEN ON's wording (:6043-6044).
  - Its reason, a deferral to a later PR, is neither the "nit" of #71 round 9 nor the "met" of #85, which left both
    uncounted (:8037-8039, :8405-8408). It is counted as 6535's "not realistic" was (:6224).
  - Its non-blocking status rests on the 2026-11-24 decision. The round record says that decision was made on the
    understated list and was told to the user to reconsider (:109). If the user now calls it blocking, the count
    returns to 2.
  - The three records' reasons differ: R-B's line filter, 6535's realism judgment, and #86's deferral to another PR. No
    proposed edit reaches all three.
  - Measurement sightings stay 12.
- The others are unchanged from the third 2026-11-26 section.
