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
