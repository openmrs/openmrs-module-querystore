# Proposals and the gate's reply · pr-review · retro window 2026-11-08 → 2026-11-15

Records: `2026-11-11-pr-review-core-6827-noop-dismissed-as-convention.md` (R-A) and `2026-11-14-pr-review-cohortbuilder-781-second-defect-filtered.md` (R-B), both hand-written defect notes. The gate's folder held copies of both, the 2026-11-04 #53 notes, `REJECTED.md`, pr-review 0.25.0 and skill-retro, a scripted extract of the #781 review transcript (owner's typed messages, assistant text, Agent prompts, task-notification results) and the PR's human comments.

## The proposals, as the gate received them

## P1a — a second consequence stays out of a comment only when the fix the comment names also resolves it

SKILL.md :163 (*Fewer, shorter comments*), two edits:
- "A second ask gets its own comment if it stands as a finding, and is dropped if not." → "A second ask, including a consequence the fix you name leaves standing, gets its own comment if it stands as a finding, and is dropped if not."
- "a second consequence where one already makes the case (Step 2's other callers aside)" → "a second consequence the fix you name also resolves (Step 2's other callers aside)"

posting.md :13, the gate line that restates it:
- "no second consequence where one made the case (Step 2's other callers aside)" → "no second consequence the fix it names also resolves (Step 2's other callers aside)"

Bar claimed: (a), two records (R-A, R-B) in which the review verified a defect, dropped it through one of the skill's own filtering rules, and a maintainer then raised it; and (c) for this site, from R-B: :163 has a "second ask" clause (own comment) and a "second consequence" clause (stays out), and a defect that is both gets opposite dispositions.

## P1b — prior instances settle a convention claim, never a claim about what a line does

SKILL.md :92 (the convention-claim bullet):
- "Prior instances kill the finding; establishing their absence is what makes it safe to post." → the same, followed by "They settle what the project accepts, never what a line does: a line verified to do nothing is still a nit, however many copies master holds."

Bar claimed: (a), as P1a; R-A is this site's instance.

## P3 — prune: delete :163's last sentence

- Delete "Verified-clean cross-cutting verification does NOT get relocated to the body (see Step 4 — the body carries none); at most it is a single inline `note (no action needed):`."
- Claimed restatement of :146 ("If such a result genuinely must be on the record, it goes in a single inline `note (no action needed):` anchored to the relevant line, never in the body.") and of posting.md :15.

## Not proposed: parked as observed, not yet actionable, each with 1 record (R-B)

- O1: the review posted a blocker with no fix, and the PR's description said later PRs build on it; one of those (#782) already held the fix. No tool call opened it.
- O2: the review's coverage mutations never touched the code its own blocking finding was about.
- O3: every check stayed inside the PR's mocks. A real request is what shows a nonexistent uuid matching nobody. 0.25.0's same-defect bullet (:104) covers unknown values only where a PR fixes a defect.

### The edits, as diffs

```diff
--- /Users/danielkayiwa/.claude/skills/pr-review/SKILL.md	2026-11-05 00:17:45
+++ proposed/SKILL.md	2026-11-15 23:37:54
@@ -89,7 +89,7 @@
 - Absence claims ("nothing registers X", "the framework never does Y") must be positively verified against the dependency's **sources** — a local checkout of the dependency (check sibling directories of the repo) or its upstream repo at the ref the project actually builds against — never inferred from jar scans or config-file greps alone: annotation-registered/component-scanned beans are invisible to XML inspection, and installed SNAPSHOT artifacts can lag or lead the sources.
 - A zero-hit search is evidence only after a positive control: before trusting "no matches", prove the same pipeline finds a string you know is present (e.g. `java/lang/Object` in a class file). Shell `grep` may be shadowed by an alias/function (ugrep shims silently skip binary-ish input, e.g. `strings` output of class files) — for evidence-grade searches use `command grep`, `git grep`, or the dedicated search tools, and never add `2>/dev/null` to an evidence-gathering command.
 - When the one experiment that could falsify a finding can't run (build stopped, environment unavailable), the finding ships as a `question:`, not a fact — and a "confirmation" that structurally cannot fail (a standalone repro that omits the very component in question) is not verification.
-- A **convention claim** — "you shouldn't do X here", "that isn't how this project works", "X blocks the release" — is a claim about this project rather than about engineering in general, and only the project's own history settles it. Before writing one, search for prior instances: `git log -S "<pattern>" -- <path>`, or walk the historical values of the property or call across the branch. Prior instances kill the finding; establishing their absence is what makes it safe to post. Verifying the *mechanism* is not a substitute, and is the trap here, because the experiment succeeds and the success feels like proof: `mvn release:prepare` really does refuse a SNAPSHOT dependency, but an OpenMRS module that carried a sibling module's SNAPSHOT on master for 58 commits before flipping it at release time was following the workflow, not breaking it. Step 2's sibling comparison is this same discipline aimed at approach rather than convention.
+- A **convention claim** — "you shouldn't do X here", "that isn't how this project works", "X blocks the release" — is a claim about this project rather than about engineering in general, and only the project's own history settles it. Before writing one, search for prior instances: `git log -S "<pattern>" -- <path>`, or walk the historical values of the property or call across the branch. Prior instances kill the finding; establishing their absence is what makes it safe to post. They settle what the project accepts, never what a line does: a line verified to do nothing is still a nit, however many copies master holds. Verifying the *mechanism* is not a substitute, and is the trap here, because the experiment succeeds and the success feels like proof: `mvn release:prepare` really does refuse a SNAPSHOT dependency, but an OpenMRS module that carried a sibling module's SNAPSHOT on master for 58 commits before flipping it at release time was following the workflow, not breaking it. Step 2's sibling comparison is this same discipline aimed at approach rather than convention.
 - Before posting, re-read every claim against the facts you already gathered this session and hunt for the one that contradicts it. The cheapest false claim to catch is the one whose refutation is already on your screen — citing an upstream precedent whose own schema makes the cited behavior unreachable, while having read that schema an hour earlier. A claim that survives only because you never connected it to something you already know has not been verified. This applies with most force to a precedent cited *in support of* a recommendation: check that the precedent actually reaches the state you are claiming it demonstrates.
 
 ### Dimensions to sweep
@@ -160,7 +160,7 @@
 - Silent-failure upgrade: when the failure mode produces wrong behavior without throwing (a swapped repo id, a dropped field, a silently replaced MockMaker), treat it one level more seriously than a crash — CI cannot catch what doesn't throw, so the cost of discovery lands on users.
 - When presenting alternatives, always state the recommended default. Never end on "either option is defensible" without picking one.
 - Do not narrate what the diff does — the author wrote it.
-- **Fewer, shorter comments beat exhaustive ones.** An inline comment is the ask (for a `note`, what it records), why it matters in one sentence (for a blocker, its failure mode), and the fix where there is one. It runs 60 words at most, not counting a suggestion or code block that holds the fix or the commands an author asked for, so a fix longer than an identifier goes in the block rather than in prose; pasted output counts, and seldom belongs. Where a finding rests on something outside the PR, link it where you name it and don't count the link: a bare name leaves the author searching. Most run two or three sentences. A second ask gets its own comment if it stands as a finding, and is dropped if not. Give what your verification found, not how you ran it: "on an instance installed with `OMRS_DB` unset, restarting with `OMRS_DB=mariadb` fails with `No suitable driver found`", not the paragraph on how you set that up and what each step printed. The account of the run, a second consequence where one already makes the case (Step 2's other callers aside), and your fix's own test results stay out of the comment unless the author asked for them, though a suggested test still says what it catches; your report to the user, where there is one, can carry them. A thread reply gets the same limit: after a bare acknowledgement ("done", "fixed") or a silent fix it is one sentence naming what survives, plus its fix, and otherwise it has no more words than the comment it answers (the latest, not the thread's first) or two sentences, whichever allows more, within the 60. Don't repeat the same cross-cutting point across comments, either. Verified-clean cross-cutting verification does NOT get relocated to the body (see Step 4 — the body carries none); at most it is a single inline `note (no action needed):`.
+- **Fewer, shorter comments beat exhaustive ones.** An inline comment is the ask (for a `note`, what it records), why it matters in one sentence (for a blocker, its failure mode), and the fix where there is one. It runs 60 words at most, not counting a suggestion or code block that holds the fix or the commands an author asked for, so a fix longer than an identifier goes in the block rather than in prose; pasted output counts, and seldom belongs. Where a finding rests on something outside the PR, link it where you name it and don't count the link: a bare name leaves the author searching. Most run two or three sentences. A second ask, including a consequence the fix you name leaves standing, gets its own comment if it stands as a finding, and is dropped if not. Give what your verification found, not how you ran it: "on an instance installed with `OMRS_DB` unset, restarting with `OMRS_DB=mariadb` fails with `No suitable driver found`", not the paragraph on how you set that up and what each step printed. The account of the run, a second consequence the fix you name also resolves (Step 2's other callers aside), and your fix's own test results stay out of the comment unless the author asked for them, though a suggested test still says what it catches; your report to the user, where there is one, can carry them. A thread reply gets the same limit: after a bare acknowledgement ("done", "fixed") or a silent fix it is one sentence naming what survives, plus its fix, and otherwise it has no more words than the comment it answers (the latest, not the thread's first) or two sentences, whichever allows more, within the 60. Don't repeat the same cross-cutting point across comments, either.
 - Use GitHub ```suggestion blocks when the fix is a concrete replacement of added lines (RIGHT side only — suggestions cannot attach to deleted lines). Where the repo's CI checks formatting, code you suggest, in a suggestion block or a fix's code block, is what its formatter makes of the change in a worktree at the PR head (openmrs-core: `mvn -pl <module> spotless:apply`), not hand-typed: an author commits a suggestion in one click, so a line one column over the limit turns the build red, and a runtime test of the fix misses that when its build skips the formatter.
 - Scale to the requested effort: by default, fewer high-confidence findings. At high effort, widen the sweep and surface uncertain findings too — explicitly as `question:`, never as unverified `issue`s.
 - Comments appear under the user's name — write them in first person, collegial tone.
--- /Users/danielkayiwa/.claude/skills/pr-review/posting.md	2026-10-27 22:47:13
+++ proposed/posting.md	2026-11-15 23:37:54
@@ -10,7 +10,7 @@
 
 > "Every finding was verified by [building / running / tracing / upstream check], not just read off the diff, and where CI checks formatting, any code I suggest is the formatter's output at the PR head, not hand-typed."
 
-> "Neither the summary verdict nor any inline comment's first line leads with a stamped label — not `issue:` / `suggestion:` / `nit:`, not the em-dash/colon variants (`Disposition: merge`, `Question — …`, `Non-blocking suggestion: …`), and not their casually-worded cousins (`Naming trivia, take or leave: …`, `Tiny style thing: …`); each says in plain language what it is and what the author must do, and either requests an action or asks a question. Every blocking comment contains its failure-mode sentence; each inline comment is the ask (for a `note`, what it records), why it matters in one sentence and the fix where there is one, a fix longer than an identifier in a code block, at most 60 words counting pasted output but not that code, commands the author asked for or a link, with what it rests on from outside the PR linked where it is named, and gives what my verification found, with no account of the run, no second consequence where one made the case (Step 2's other callers aside), and no test results for the fix beyond what a suggested test catches, unless the author asked for them; a second ask is its own comment if it stands as a finding, or gone if not; each thread reply is held to the same limit, and after a bare acknowledgement or a silent fix it is one sentence naming what survives, plus its fix, and otherwise has no more words than the comment it answers (the latest, not the thread's first) or two sentences, whichever allows more, within the 60; and no comment — fresh or threaded reply — exists merely to acknowledge a fix or credit resolved work."
+> "Neither the summary verdict nor any inline comment's first line leads with a stamped label — not `issue:` / `suggestion:` / `nit:`, not the em-dash/colon variants (`Disposition: merge`, `Question — …`, `Non-blocking suggestion: …`), and not their casually-worded cousins (`Naming trivia, take or leave: …`, `Tiny style thing: …`); each says in plain language what it is and what the author must do, and either requests an action or asks a question. Every blocking comment contains its failure-mode sentence; each inline comment is the ask (for a `note`, what it records), why it matters in one sentence and the fix where there is one, a fix longer than an identifier in a code block, at most 60 words counting pasted output but not that code, commands the author asked for or a link, with what it rests on from outside the PR linked where it is named, and gives what my verification found, with no account of the run, no second consequence the fix it names also resolves (Step 2's other callers aside), and no test results for the fix beyond what a suggested test catches, unless the author asked for them; a second ask is its own comment if it stands as a finding, or gone if not; each thread reply is held to the same limit, and after a bare acknowledgement or a silent fix it is one sentence naming what survives, plus its fix, and otherwise has no more words than the comment it answers (the latest, not the thread's first) or two sentences, whichever allows more, within the 60; and no comment — fresh or threaded reply — exists merely to acknowledge a fix or credit resolved work."
 
 > "The summary body carries only what no inline line can, and when a single inline comment already conveys the verdict it is empty. I did not add a body to announce a disposition the inline comments already imply: a lone blocking comment whose wording shows the PR is not ready needs no cover note. A body appears only for an unanchored cross-cutting concern, the author's question, or a genuinely synthetic cross-finding note (a fix-ordering dependency, or a shared root cause); each finding or answer it carries is held to the inline length limit; it never merely summarizes or tallies the blocking-ness that each inline comment already states, and even when present it never restates a finding (a pointer is a bare reference, not a one-line recap of what the comment says; a body paragraph reproducing an inline comment is the finding said twice). Zero verified-clean or cleared-concern content anywhere in it: no 'what I checked' line, bullet, section, or aside, and none folded into prose; and on a re-review, no 'prior points addressed' recap, since a resolved finding earns silence, not a tally. The verification I did shows up only as the confidence behind the verdict, never written out; if a result must be on the record it is a single inline `note`, not body text."
 
```

## The gate's reply, verbatim

One headless refuter (`refuter session=9bc75336-a64f-44c1-b75d-20c750e6890a`, `claude-opus-5-5`, started from a folder holding copies of everything above, no project memory, read-only tools, $1.17). Its first reply, then the reply it gave after its Stop hook refused the first for listing unchecked items without a reason:

### First reply

**P1a: park.** It meets no bar. If the orchestrator rules that bar (c) holds, revise it to the wording below, because the drafted wording would not have changed what R-B's run did.
**P1b: park.** One record supports it, and the premise it fixes comes from the reviewing session's own after-the-fact account, not from the transcript.
**P3: ship.** Everything the deleted sentence says is carried at SKILL.md:146 and posting.md:15.

---

## P1a: "a second consequence stays out only when the fix you name also resolves it"

**1. Does the record say it?** Yes, for R-B. R-B:16 says: "10:36:26 'My reply stays on the composition case alone, since one consequence makes the case.' That applies SKILL.md's 'a second consequence where one already makes the case … stay[s] out of the comment' to a defect with its own fix". The run's own words match (extract:110). R-A does not say it. R-A's filter was the convention rule at :92, not :163 (R-A:15: "It then applied SKILL.md's convention rule ('Prior instances kill the finding')").

**2. Bar: BLOCKING.**
- **Bar (a) treats one lesson as covering two different events.** P1a's site (:163, "second consequence") has one record, R-B. R-A is a different rule (:92) at a different site, and the proposal counts it toward both P1a and P1b. What the two records share is the class-level shape ("saw it, then filtered it through a scoping rule", R-B:23). Neither edit addresses that shape: each edit touches only the one rule its own record names.
- **The "third run" in R-B:23 is false, and so is R-A:21's "same shape".** Both cite #53 as a review that saw the item and then filtered it out. #53's own record (`2026-11-04-…-53-missed-input.md`:15) says: "An absent or null `name` has zero mentions in its text, thinking or commands". REJECTED.md:6025-6026 agrees: "The run that missed never noticed K1". The replay that did see it is a measurement, and REJECTED.md:5595-5596 says a measurement "has never counted as corroboration". proposals.md does not claim three records, but its bar (a) rests on the same class-level lesson these lines inflate.
- **Bar (c) is not a fact about the document.** R-B:16 itself reads the current text as already giving the right answer: the rollback is "a defect with its own fix, which the same paragraph's 'A second ask gets its own comment if it stands as a finding' covers". That describes a misreading, not two clauses that conflict. skill-retro:84-86 allows bar (c) only for "a fact about the document rather than an inference about the world". Whether the rollback is an ask or a consequence is a judgement about the world: it took four race tests in a later session to settle (R-B:8). With (a) at one record and (c) contested, skill-retro:134-136 says to park.

**3. Contradiction with existing rules: NON-BLOCKING.** The gate restates the rule only halfway. posting.md:13 keeps "a second ask is its own comment if it stands as a finding, or gone if not" and gets only the second edit. The run recites this gate word for word before posting (extract:152), so the first edit's "including a consequence the fix you name leaves standing" never reaches the line the run actually says. Nothing in REJECTED.md covers "second consequence": a grep for it returns no hits.

**4. Unchecked claim: BLOCKING.** "The fix you name" assumes a fix. :163 allows a comment with none ("and the fix where there is one"), and R-B's posted reply named none (pr-781-human-comments:33; R-B:17: "the blocking reply named no fix"). When no fix is named, the new test is undefined:
- If "resolves" is read as "resolves nothing", every second consequence of every blocker without a fix now has to go in. That flips the brevity rule for a whole class of comments, and nobody measured it.
- If it is read as vacuous, nothing changes for R-B's case.

**5. Would it have prevented R-B? Probably not: BLOCKING.**
- The run filed the rollback as part of one race. Its summary says "fix the history-position race in `runDefinition`" (extract:156) and lists the rollback as "It also found the rollback can leave half a link behind" under that same item (extract:134).
- Under the edited text it has to judge whether "the fix" also resolves the rollback. The only fix in sight is "stop using positions". The maintainer described it as the same change: "The rollback deletes by position too, so it needs the same change" (human-comments:36). A run reasoning that way concludes the fix resolves it and drops it again.
- The fact that would have kept the rollback in, "`composeQueries` alone fixes the first two and leaves both rollback races failing" (R-B:8), was measured only in session 7d92a8d0. The run never had it.

**Replacement text, if bar (c) is ruled met.** The test is "a change on other lines", which the run could check without the later measurement.

SKILL.md:163:
- Replace "A second ask gets its own comment if it stands as a finding, and is dropped if not." with: "A second ask, including any consequence that needs a change on other lines than the ones your comment asks about, gets its own comment if it stands as a finding, and is dropped if not."
- Replace "a second consequence where one already makes the case (Step 2's other callers aside)" with: "a second consequence of the change your comment already asks for (Step 2's other callers aside)".

posting.md:13:
- Replace "no second consequence where one made the case" with "no second consequence of the change it already asks for".
- Replace "a second ask is its own comment if it stands as a finding, or gone if not" with "a second ask, including a consequence needing a change on other lines, is its own comment if it stands as a finding, or gone if not".

## P1b: prior instances never settle what a line does

**1. Does the record say it?** Only in the session's after-the-fact account. R-A:14: "The reasoning in between is not in the transcript; this is the session's own account of it". The transcript shows a grep that found the 10 copies, and nothing more. skill-retro:15-17 ("A run cannot grade its own lessons") exists for exactly this.

**2. Bar: BLOCKING.** Only R-A involves a convention claim. R-B has no convention claim anywhere: the extract shows no prior-instances search used to drop a finding. Bar (a)'s "as P1a" is the same stretched class-level lesson as above. Bar (b) is not met (no rounds), and bar (c) is not met: :92 already defines a convention claim by its examples ("you shouldn't do X here", "X blocks the release"), and "this attribute is inert" is not one of them. The existing text does not contradict itself. One record means park (skill-retro:88, :208-209).

**3. Contradiction or re-proposal: NON-BLOCKING.** It adds "never". skill-retro:118-119 lists *never* as a defect class ("treat *any*, *only*, *exactly*, *all*, *never*, *cannot* as the same defect"). It does not re-propose anything in REJECTED.md.

**4. Unchecked claim: NON-BLOCKING.** "However many copies master holds" is a disposition rule that nobody checked against this project's maintainers. R-A:10 records that 10 copies sit on master and that #5792's "16 inline comments never mention it". That is evidence maintainers have let it pass, and the proposal does not weigh it.

**5. Would it have prevented R-A? Unlikely: BLOCKING.**
- The trigger is "a line verified to do nothing". In the review, the run only "saw both attributes as doing nothing" (R-A:15). The verification in R-A:8 was done afterwards, and R-A:17 says "The review's strongest check could not see this class of defect."
- R-A:16 names two more pressures toward silence that the edit leaves in place: "'Don't invent concerns' and 'Fewer, shorter comments' pushed further toward silence".
- So under the edited text the run most likely still reports "ready to merge, nothing posted". Its belief was never "verified", and the other two rules still argued for silence.

**Wording, for a later reopen** (it drops the "never"): "They settle what the project accepts, not whether a line has an effect: an attribute you have verified to be inert is still a take-or-leave nit, whatever master holds."

## P3: delete :163's last sentence

Nothing found wrong. The deleted sentence's two claims are both carried:
- "Does NOT get relocated to the body" is carried by :146: "verified-clean material never appears in it, not as a section, a bullet, a terse line, or folded into prose".
- "At most a single inline `note (no action needed):`" is carried by :146 ("it goes in a single inline `note (no action needed):` … never in the body") and by posting.md:15 ("if a result must be on the record it is a single inline `note`, not body text").

No evidence.md exists beside pr-review, so no measurement is attached to that sentence.

**One NON-BLOCKING issue for the bundle.** proposals.md:9 gives SKILL.md 8510 → 8516 and posting.md 2070 → 2071. That is net growth, and skill-retro:99-104 asks for a sentence justifying it and a `raises` entry. proposals.md gives neither. If P1a and P1b are parked, P3 alone is a net cut and the budget is lowered instead (skill-retro:104).

## O1–O3, and the parked entry

- **REJECTED.md:6037-6044 ("seen, then set aside"): BLOCKING for proposals.md:7.** Its REOPEN ON reads "a run record where a review left a real non-blocking defect in its notes and the maintainer then raised it."
  - R-B meets this literally. P3/P4 were in the terminal report (extract:134; R-B:16: "The terminal report carried P3/P4; the PR did not"), and Dennis raised the rollback (human-comments:36), with no claim that it blocks.
  - R-A does not meet it. Its final report "raised neither attribute" (R-A:14).
  - proposals.md names the entry but never says whether it reopened. It should record that the entry's run-record count moves 0 → 1 (R-B), and frame P1a's lesson as that entry's lesson: the review saw it, then filtered it out.
- **O1 (the PR stack was never opened):** 1 record (R-B:17). REJECTED.md has no matching parked entry: grep "stack" hits only :5430, which is unrelated. Below the bar.
- **O2 (mutations missed the blocker's code):** 1 record (R-B:18), so below the bar. NON-BLOCKING: it should cite REJECTED.md:5633-5637 ("(c) The per-condition mutation sentence", measured and not adopted). That REOPEN ON asks for a measurement, so this run record does not advance it. Without the citation, a later retro could re-propose that sentence without knowing it was measured.
- **O3 (checks stayed inside the mocks):** 1 record for the part :104 does not cover. #53's lesson (`…-53-missed-input.md`:21, "send absent, null, blank and unknown values … through the real entry point") is the precursor, and O3 should cite it. #53's PR fixed a defect, though, so :104 now covers that case, and #53 does not count toward the uncovered part. Below the bar.

### Second reply

The Stop hook flagged three items in my last reply. I'm restating only those three, each with what I checked. Trigger: "an item listed as unchecked must carry one of three reasons … Items without a reason: …". All verdicts and the rest of the reply stand.

**P1a §4: the two readings of "the fix you name" when a comment names no fix.**
- **What I checked:** the text itself. The proposed :163 offers no other wording for this case (`proposed/p-skill.diff`:17). :163 allows a comment without a fix ("and the fix where there is one"). R-B's posted reply named none (pr-781-human-comments:33). So the gap in the text is confirmed as a fact about the document, and the objection stays BLOCKING.
- **Still unchecked:** which of the two readings a run would actually take. Only a replay of the #781 review under the edited text can show that, and my brief forbids running anything. Running that replay **needs your decision**.
  - Reading 1: "resolves" means "resolves nothing", so every second consequence of a blocker without a fix has to go in.
  - Reading 2: the clause is vacuous, so nothing changes for R-B's case.
- **Applies to the third bullet under §4 too:** "If it is read as vacuous, nothing changes for R-B's case" is the same item with the same reason.

**P1b §4: "however many copies master holds" checked against maintainers.**
- **What I checked:** the only maintainer evidence in this folder. R-A:10 records 10 copies on master and #5792's "16 inline comments never mention it". R-A:2 records that Wikum asked about it on 6827 and the author removed it.
- **Still unchecked:** whether maintainers treat the copies on master as accepted or as defects nobody has raised yet. Settling that means reading openmrs-core's review history beyond these two PRs, which is not in this folder. It **needs access**.
- The objection stays NON-BLOCKING.