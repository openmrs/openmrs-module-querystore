# 2026-11-18 · owner-directed · pr-review 0.25.1 → 0.26.0 proposals from the openmrs-core PR 6535 miss

## The owner's instruction, verbatim (session 13a04911, 2026-11-18)

After the session listed three gaps, the owner asked: *"Doesn't this require updating this skill? https://github.com/openmrs/openmrs-module-querystore/blob/main/.claude/skills/pr-review/SKILL.md"*. The session's answer ended *"Should I run these three through skill-retro and push the result? That means drafting them, having a fresh agent try to break them, pruning as much as I add, and bumping to 0.26.0."* The owner replied *"yes"*.

## Evidence

- New record: `2026-11-18-pr-review-core-6535-maintainer-changes-requested.md`.
- Earlier record, already counted on 2026-11-15: `2026-11-14-pr-review-cohortbuilder-781-second-defect-filtered.md` (R-B).
- Ledger: REJECTED.md, the 2026-11-05 section ("Parked, measurement only, 0 run records: seen, then set aside as outside the ticket", with its REOPEN ON) and the 2026-11-15 section ("Seen, then set aside … 0 → 1 run record", P1a's REOPEN ON).
- Not a run record: the owner's auto-memory `~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-openmrs-core/memory/pr-review-clean-verdict-survives-proposing-fixes.md`. It records earlier rounds where a refuter given the posted fix found its error before posting (PRs 380, 6801, 6157, REST 778, cohortbuilder 783).

Current text: `~/.claude/skills/pr-review/SKILL.md` (0.25.1, 8481 words, budget 8481) and `posting.md`. pr-review has no evidence.md. The edits applied to a scratch copy, as a unified diff: `proposed.diff`, beside the after-copy `SKILL.after.md` (8487 words).

## P1 · Step 1 (:16): fetch labels and fixVersions, and read a `backport` label

Before: `…/issue/<KEY>?fields=summary,description,status,comment` · `and \`comment\` among its \`fields\``
After: `…?fields=summary,description,status,comment,labels,fixVersions` · `and \`comment\` and \`labels\` among its \`fields\``, plus before "Step 2 is what this feeds.":
> A `backport` label means the change must also be right on the maintenance lines: an API it adds lists their versions in `@since`, as the tree's `@since 3.0.0, 2.9.0, 2.8.9` tags do.

- **Bar claimed:** owner-directed. Bar (b) as well, tentatively: one record, in which the missing label cost two rounds. The 2026-11-09 round suggested `@Deprecated(since = "3.0.0")` (r4512249216), and the 2026-11-18 changes-requested round flagged `@since 3.0.0` (r4617894513).
- **Convention evidence (record):** 81 multi-version `@since` tags in 48 files at openmrs-core master a93b6293c9.

## P2 · Step 3 Trace outward (:104): lines the PR moves or rewrites are its own code

The bullet's title becomes **"The same defect, where the PR didn't look, and any defect in lines it moved."** Inserted before "An instance that predates the PR":
> Lines the PR moves or rewrites are its own code, so read them for any defect, such as a null that throws or a stream an error path leaves open: comparing the PR with its base cannot show a defect both carry. What a repaired line was designed to do is not such a defect.

The existing disposition sentence (blocks only if it defeats the PR's guarantee, otherwise a suggestion) then covers these too.

- **Bar claimed:** owner-directed. The record meets the parked "seen, then set aside" REOPEN ON ("a run record where a review left a real non-blocking defect in its notes and the maintainer then raised it"): the MediaHandler stream was seen and set aside as "no regression", then raised (r4617890396). That makes 2 run records at the class level, with R-B.
- **Stated plainly:** R-B's filter was :163, and this edit does not reach it, so the site-level count is 1. The null mimeType (r4617880105) was never noticed; the edit's first clause is aimed at noticing it.
- The last sentence keeps the owner's recorded boundary: a PR that repairs never-run code isn't held to question its original design.

## P3 · Step 4 (:164): a fix the review posts gets the PR's checks

Appended to the suggestion-block bullet:
> A fix you post is a change you propose, so it gets the checks you gave the PR: name every input and exception type it changes, read any older thread on the same concern, and where a refutation pass runs, give it the code, not only the claim.

- **Bar claimed:** owner-directed. One run record (6535: the 2026-11-04 code block r4473604840 with `catch (FileSystemException e)`, flagged by r4617836000 and r4617840695). Greptile's 2026-09-02 thread 3915136388 had already named the concern ("Fallback masks storage failures … permission error").
- **Supporting, not a run record:** the owner-memory incidents listed above.

## Prunes (Step 4)

- **A · delete :142**, 39 words: "A "ready to merge" verdict with nothing left to act on is **not** something you post — see Step 5. It goes to the person who asked for the review, in conversation, not as a comment on the PR." Claimed carried by :176 (Step 5's rule, in full) and :146's last sentence.
- **B · delete the anti-pattern "Don't acknowledge fixes (especially on re-review)"** (:186), 99 words. Claimed carried by :20 (leave a fixed thread closed and post nothing; silence is the confirmation; the most common re-review slop), :146 (no resolved-finding recap on a re-review), and posting.md's third gate line (no comment exists merely to acknowledge a fix).

Net: 8481 → 8487 words, +6. To be trimmed to ≤ 0 after the gate, or raised with a `why`.

## The gate's reply, verbatim (round 1)

One headless refuter (`refuter session=efc908b8-b7bb-41a8-98de-214dee88d48f`, `claude-opus-5-5`, started from a folder holding copies of everything above, no project memory, read-only tools, $1.93). Its first reply, then the reply it gave after its Stop hook refused the first for listing unchecked items without a reason:

### First reply

**Verdicts: P1 revise · P2 park · P3 park · Prune A ship · Prune B ship.**

Ground rule from the ledger. An owner-directed pass has shipped a rule only when it also had a pre-registered measurement. ledger-11-05:5-10 says the pass ran "because the owner directed the change … with a pre-registered measurement as its evidence", and :17 credits "the owner's direction and the replay … Not by a count". An unmeasured rewording in that same owner-directed pass was parked (P1′, :35-41). The 11-15 pass was also owner-invoked, and it parked both of its rules and shipped only a prune (ledger-11-15:1, :19, :38). This pass adds one new record (6535). R-B was already counted on 11-15.

---

## P1 · labels, fixVersions and backport `@since` → **revise**

**1. Does the record say it?** Yes.
- record-6535:13: "Step 1's JIRA recipe asks for `fields=summary,description,status,comment` … Neither prints labels. The REST read shows TRUNK-6682 with `labels: ['backport', …]`."
- The maintainer used exactly that signal. r4617894513 (ibacher, StorageService.java:197): "The ticket is marked for backport, so this is incorrect."
- The review's own wrong suggestion is r4512249216 (2026-11-09): `@Deprecated(since = "3.0.0")`. I confirmed it in the JSON.

**2. Bar.**
- Bar (b) is not met (non-blocking, since the proposal says "tentatively"). The 11-09 suggestion and the 11-18 miss are one defect seen in two rounds. The record gives no round to the label: record-6535:2 lists six rounds, held for other reasons.
- Bar (a) and bar (c) don't apply.
- The owner-directed precedent does cover it. Its premise is measured (record:13–14), and the edit works mechanically: adding `labels` to the field list prints the labels. No replay is needed to show that.

**3. Contradiction or re-proposal:** none found in SKILL.current.md, posting.current.md or either ledger section.

**4. Unchecked claims.**
- **Convention: verified.** `since-tags-multi-version.txt` has 81 lines in 48 distinct files. `3.0.0, 2.9.0, 2.8.9` appears 11 times and `2.8.0, 2.7.5, 2.6.16, 2.5.15` 7 times. The file's sibling, StorageService.java:29, carries a multi-version tag.
  - The file holds only multi-version tags. It cannot show that every backported API uses one. The proposed sentence presents it as an example ("as the tree's … tags do"), not as a universal, so this is non-blocking.
- **Non-blocking:** the diff changes the MCP clause to "`comment` and `labels` among its `fields`, which it leaves out by default". That says the MCP's getJiraIssue omits `labels` by default, and nothing here checks it. Verifying it **needs access** to the Atlassian MCP.
- **Non-blocking:** the MCP route also leaves out `fixVersions`, which the REST URL now fetches. A private ticket would lose the field that names the versions.
- **Non-blocking:** the example `3.0.0, 2.9.0, 2.8.9` will be copied verbatim. 2.8.9 is already released (r4070873955: "2.8.9 is out"), so it is wrong for a new API.

**5. Would it have prevented the miss?** Partly (non-blocking).
- The backport intent was already on the PR on 2026-09-11. r3990860896 (ibacher): "We can't backport this implementation".
- The review's own r4070873955 (2026-09-22) asked for "whatever maintenance lines it gets backported to".
- record-6535:15 puts the failure on "checked by its headline only". SKILL.current.md:20 already forbids that ("Authors routinely fix the headline … while leaving a sub-point untouched").
- record-6535:13 says the run fetched `fixVersions` and still missed. The record doesn't say what fixVersions held.
- So P1 adds a louder second signal. It doesn't fix the :20 compliance failure.

**Replacement wording.**
- REST URL: keep `…?fields=summary,description,status,comment,labels,fixVersions`.
- MCP clause: "(cloudId `openmrs.atlassian.net`, with the same `fields`, since it leaves `comment` out by default)".
- Inserted sentence: "A `backport` label means the change must also be right on the maintenance lines: an API it adds lists each line's version in `@since`, as `StorageService`'s own `@since` does."

---

## P2 · lines the PR moves or rewrites → **park**

**1. Does the record say it?** Partly.
- **Null mimeType.** record-6535:19: "`mimeType.equals("application/octet-stream")` is master's `TextHandler.java:91-92`, moved into `setContentMetadata`." Confirmed: TextHandler.master.java:91-92 → TextHandler.head.java:117-118, with no guard on `mimeType`.
- **Stream (blocking, for the bar claim).** proposals.md:32 says the stream was "seen and set aside as 'no regression', then raised". But record-6535:18 says the reviewer found that "master leaks … when a module's `getMetadata` throws, and that the PR doesn't". The maintainer raised a different thing. r4617890396 asks for streams to be "guaranteed to be closed (current code doesn't …)". The record doesn't show that the defect seen and the defect raised are the same one.
- The 6535 item that clearly meets the 11-05 REOPEN is the order mismatch. record:17 has it set aside twice as "not realistic", and r4617856131 then raised it. P2 doesn't touch that filter.

**2. Bar: not met (blocking).**
- (a) fails at the site P2 edits. proposals.md:33 admits "the site-level count is 1".
- The 11-15 gate rejected this exact move. ledger-11-15:21-22: "Bar (a) counted one lesson over two events … neither edit reaches the class-level shape the two share."
- ledger-11-15:57-60 sent the reopened class lesson to P1a: "it stays parked with P1a's REOPEN ON". 6535 doesn't meet that REOPEN (":163"; proposals.md:33).
- (b) and (c) don't apply.
- Owner-directed: there is no measurement, and the precedent shipped nothing unmeasured (ledger-11-05:35-41).

**3. Contradictions and re-proposals.**
- **Non-blocking:** P2 sends more pre-existing defects, unclosed streams included, through ":104 … blocks only if it defeats the PR's own guarantee". SKILL.current.md:184 names "an unclosed resource" as routinely under-labelled. The 11-05 ledger already parked this tension (ledger-11-05:23-27), and P2 widens it.
- **Non-blocking:** the extra noise from "any defect in lines it moved" is unmeasured. Its REOPEN, "a held-out PR where E's extra findings are noise", is not met (ledger-11-05:28-29).

**4. Unchecked claims.**
- **"Comparing the PR with its base cannot show a defect both carry" (blocking).** It is false as a universal. The 11-18 base comparison did show a master defect (record:18). What hid the null mimeType was a brief that reported only differences: record:23 quotes "defects this PR introduces relative to master". skill-retro.md:118-119 treats "cannot" as a defect.
- **"Read them for any defect" (non-blocking):** "any" is the same defect class under skill-retro.md:118.
- **"What a repaired line was designed to do is not such a defect" (blocking).** proposals.md:34 says it keeps "the owner's recorded boundary", but nothing in this folder records that boundary. The sentence could also exempt the stream: handing an open stream to `ComplexData` is what master's line was designed to do (MediaHandler.master.java:64-65).

**5. Would it have prevented the miss?**
- Plausibly for the null mimeType. Its example, "a null that throws", is fitted to that one case.
- Not shown for the stream, for the reason under 1.
- Untested in any replay.

**REOPEN ON** either of these:
- A pre-registered, blind-scored replay of the 2026-11-18 round on 6535 head 86b96c7d6f. The bullet files the TextHandler:118 null `mimeType` and the current text does not, and E's extra findings are counted for noise.
- A second run record where a defect on lines the PR moved, also present on base, was left out of the review and then raised by a maintainer.

A reopen should drop the "repaired line" sentence unless the boundary is cited. It should use this wording: "Lines the PR moves or rewrites are its own code, so read them as new code, such as for a null that throws or a stream an error path leaves open: a probe that reports only differences from the base misses a defect both carry."

---

## P3 · a posted fix gets the PR's checks → **park**

**1. Does the record say it?** Yes.
- record-6535:16: "`catch (FileSystemException e)` came from r4473604840 (2026-11-04) … `AccessDeniedException` extends `FileSystemException`, so a permission error at the current location serves the legacy copy whenever one exists."
- Confirmed in the JSON. r4473604840 is by dkayiwa and contains `} catch (FileSystemException e) {`. It is still at LocalStorageService.head.java:102.
- The maintainer flagged it in r4617836000 and r4617840695 ("This overreaches and also bypasses `AccessDeniedException`s and others.").
- Greptile's 3915136388 (2026-09-02) does read "Fallback masks storage failures … permission error". It sits on AbstractHandler.java, not LocalStorageService.

**2. Bar: not met (blocking).**
- One run record. (a), (b) and (c) don't apply.
- The owner-memory incidents (owner-memory:46, :50, :54, :59, :68, :90, :96) are not in the store. skill-retro.md:39-42 says hand-written captures don't count as runs, and proposals.md:42 concedes the point.
- The owner-directed precedent requires a measurement (ledger-11-05:5-10), and P3 has none.

**3. Contradictions.**
- **Non-blocking:** "name every input and exception type it changes" doesn't say where to name them. If it means in the comment, it conflicts with :163 ("your fix's own test results stay out of the comment", 60-word limit) and posting.current.md:13 ("no account of the run").
- **Overlap, non-blocking:** :18 already asks for older threads to be checked, and :82 already extends the verification bar to "thread replies and design answers".

**4. Unchecked claims (non-blocking):** "every" is the defect class under skill-retro.md:118.

**5. Would it have prevented the miss?** Plausibly. The code block's own comment says it widened past a missing file on purpose ("Not only a missing file …"). Listing what `FileSystemException` catches would have turned up `AccessDeniedException`, and so would reading 3915136388. No replay tests this.

**REOPEN ON** either of these:
- A second run record in the store where a fix the review posted or drafted was broken by a maintainer or a refuter. The memory's PR 380, 6801, REST 778, 6157 and 6790 rounds would each qualify if written up from their transcripts as run records.
- A replay where a reviewer given the r4473604840 block plus P3 names `AccessDeniedException` and one without P3 doesn't.

A reopen should use this wording: "A fix you post is a change you propose: before posting, list for yourself each input and exception type it newly accepts or catches, and read earlier threads on the same lines; where a refutation pass runs, give it the code, not only the claim."

---

## Prune A · delete :142 → **ship**

Every claim is carried elsewhere:
- **"Not something you post":** :176 ("A clean 'ready to merge' verdict is reported to the user here, never posted to the PR"), :187 ("never post a standalone 'ready to merge / LGTM' comment"), and posting.current.md:17.
- **"Goes to the person who asked, in conversation":** :176 ("tell the user it's ready to merge in the conversation and stop") and :187 ("that verdict goes to the person who asked, in conversation").
- **"Nothing left to act on":** :176 ("nothing to act on — fresh PR or re-review").
- **"See Step 5":** :134 already points there ("keep the 'if it would just be LGTM, post nothing' rule from Step 5").

No objection.

## Prune B · delete the :186 anti-pattern → **ship**

Every claim is carried elsewhere:
- **Comment only on what's still wrong:** :20 ("Only when a flagged sub-point *survives* do you reply in-thread").
- **No "thanks, that's resolved":** :20 ("no reply, no 'thanks, that's fixed', no resolved-thread acknowledgment") and posting.current.md:13 ("no comment — fresh or threaded reply — exists merely to acknowledge a fix").
- **No body tallying what got addressed:** :146 ("do not open with 'all N points from last round are addressed' or otherwise tally") and posting.current.md:15.
- **Fixed means no comment, and the verdict is the acknowledgment:** :20 ("Silence *is* the confirmation") and :146.
- **Reads as slop:** :20 ("the single most common re-review slop").

Lost: only the rationale "This is the gap authors notice most". It is a salience claim, not a rule, and it competes with :176's "the thing PR authors most dislike".

**Non-blocking:** pr-review has no evidence.md (proposals.md:14). Whether :186 was added with a measurement behind it needs the querystore history, which **needs access** I don't have here. skill-retro.md:106-108 requires recording any measurement that retires a rule.

---

**Budget:** P1 as revised plus prunes A and B comes to about +30 −138 words. Lower the `pr-review` budget in the same commit (skill-retro.md:104).

### Second reply

**The verdicts are unchanged: P1 revise · P2 park · P3 park · Prune A ship · Prune B ship.**

The Stop hook flagged eight items with no reason given ("an item listed as unchecked must carry one of three reasons…"). They were all sitting under my "4. Unchecked claims" headings, which made them look unchecked. Six of them I had already checked against the files in this folder. One more was half-checked: what it rests on doesn't depend on any check. That leaves two that genuinely need access. Corrected answers to question 4:

**P1: claims and what I found**
- **Checked:** the counts hold. `since-tags-multi-version.txt` has 81 tags in 48 distinct files. `3.0.0, 2.9.0, 2.8.9` appears 11 times and `2.8.0, 2.7.5, 2.6.16, 2.5.15` 7 times.
- **Checked:** the file is a grep for multi-version tags only. So it cannot show that every backported API carries one. The proposed sentence uses the tags as an example ("as the tree's … tags do"), not as a rule for all of them. Non-blocking.
  - To count backported APIs whose tag lists only one version, I'd need a grep over openmrs-core with JIRA labels. That **needs access** I don't have here.
- **Checked:** proposed.diff:5 adds `fixVersions` to the REST URL but leaves it out of the MCP clause ("`comment` and `labels` among its `fields`"). Non-blocking.
- **Checked:** r4070873955 says "2.8.9 is out". So the example `3.0.0, 2.9.0, 2.8.9` is wrong if a new API copies it. Non-blocking.
- **Unchecked:** whether getJiraIssue leaves `labels` out by default. This **needs access** to the Atlassian MCP. The wording I proposed for P1 avoids the claim.

**P2: claims and what I found**
- **Checked:** P2 says "comparing the PR with its base cannot show a defect both carry". record-6535:18 contradicts that: the 11-18 comparison did find a master defect. What hid the null `mimeType` was the brief's "relative to master" wording (record-6535:23). Blocking.
- **Checked:** P2's "any" is one of the words skill-retro.md:118 treats as a defect. Non-blocking.
- **Half-checked:** P2's last sentence ("What a repaired line was designed to do is not such a defect") rests on what proposals.md:34 calls "the owner's recorded boundary". No file in this folder records that boundary, and the folder holds everything the brief says I need. Whether it is recorded anywhere else **needs access**. Either way, the objection stands on two checked facts:
  - the sentence cites nothing here;
  - it could exempt the stream: master's MediaHandler.master.java:64-65 hands an open stream on by design.

  Blocking.

**P3: claims and what I found**
- **Checked:** P3's "every" is one of the words skill-retro.md:118 treats as a defect. Non-blocking.

**Prune B**
- **Unchecked:** whether the :186 anti-pattern was added with a recorded measurement behind it. This **needs access** to the querystore git history.

## The gate's reply, verbatim (round 2, over the staged diff)

One headless refuter (`refuter session=8b5a2a53-dc93-430d-9332-9bec119ba688`, `claude-opus-5-5`, started from a folder holding the current and staged SKILL.md, the staged diff, round 1's replies, the record and the budget statement; no project memory, read-only tools, $0.65):

**Commit as staged.** I found no blocking objection.

**1. Does the diff apply exactly the verdicts?** Yes.
- **REST URL:** `staged.diff:8` reads `…?fields=summary,description,status,comment,labels,fixVersions`. That matches `gate1-reply-first.md:36` word for word.
- **MCP clause:** `staged.diff:8` reads "(cloudId `openmrs.atlassian.net`, with the same `fields`, since it leaves `comment` out by default)". That matches `gate1-reply-first.md:37` word for word.
- **Inserted sentence:** it matches `gate1-reply-first.md:38` except for the agreed change. It is placed just before "Step 2 is what this feeds."
- **The agreed change is accurate and acceptable.** `StorageService.master.java:29` is `@since 2.8.0, 2.7.5, 2.6.16, 2.5.15`, which lists one version per line, and it is the only `@since` in the file. The staged wording also fixes the round-one wording, which said "@since" twice ("lists … in `@since`, as `StorageService`'s own `@since` does").
- **Prune A:** `staged.diff:10-11` deletes `SKILL.current.md:142` and the blank line after it, and nothing else. `SKILL.staged.md:140-142` keeps a single blank line.
- **Prune B:** `staged.diff:13` deletes `SKILL.current.md:186` only. The neighbouring bullets are intact at `SKILL.staged.md:183-185`.
- **Version:** 0.25.1 → 0.26.0 is a minor bump for a new rule, as `skill-retro.md:143` asks.
- **Non-blocking:** the diff header (`staged.diff:2`) names `SKILL.final.md`, not `SKILL.staged.md`. I checked that `SKILL.staged.md:5`, `:16`, `:142` and `:176-185` match the diff. The section headings move by exactly the deleted lines (Step 5: 168→166; Anti-patterns: 178→176). I did not run a full byte diff of the two files: that **needs access** to a shell, which this session doesn't have. Before committing, the orchestrator should run `diff SKILL.current.md SKILL.staged.md` and confirm it prints the same hunks.

**2. Does the staged text add a new defect?** No.
- **No dangling references.** The two remaining "anti-pattern" mentions (`SKILL.staged.md:44`, `:72`) both mean the "inflated version" bullet, which is still at `:180`. `posting.current.md` matches none of "acknowledge fixes", "anti-pattern", "ready to merge" or "backport".
- **Prune A's pointer is still covered.** The "see Step 5" pointer is carried by `SKILL.staged.md:134` and `:174`.
- **No contradictions.** The new sentence doesn't conflict with `:20`, `:174`, `:184` or `posting.current.md`. It only adds something to read in Step 1.
- **Universal words:** the new text has none of the words `skill-retro.md:118` lists. The "only" in `:16` ("A ticket only that reads") was already there.
- **Non-blocking:** "since it leaves `comment` out by default" assumes `getJiraIssue` accepts `labels,fixVersions` in `fields`. Round one already left that open (`gate1-reply-second.md:11`) because checking it **needs access** to the Atlassian MCP.
- **Non-blocking:** the example names a file by name, so it goes stale if `StorageService` is renamed or moved. It is not a count, so `skill-retro.md:118` doesn't apply.

**3. Is the budget right?** Yes, and lowering it with no raise is what Step 4 asks for.
- **Arithmetic:** I counted the changed text by hand, splitting on whitespace the way `wc -w` does.
  - The URL is one word before and after.
  - The MCP clause is 11 words before and after.
  - The new sentence adds 28 words.
  - Prune A removes 39 words and prune B removes 99.
  - Net change: −110, and 8481 − 110 = 8371, which matches `budget.txt:1`. The 138 words removed (39 + 99) also match round one's estimate of −138 (`gate1-reply-first.md:132`).
  - I did not measure the 8481 starting count myself: that **needs access** to a shell.
- **Step 4:** the file shrinks, so `skill-retro.md:100-103` (pay for growth, or add a `raises` entry) doesn't apply. `:104` ("a file you prune has its budget lowered in the same commit") does, and the linter in `:63-65` flags any file whose word count differs from its budget. So the budget must be set to exactly 8371, with no `raises` entry.
- **Non-blocking, for the report rather than the diff:** `skill-retro.md:106` says a measured rule is never deleted without recording the measurement that retires it. Whether prune B's rule had a measurement behind it is still unchecked; that **needs access** to the querystore history (`gate1-reply-second.md:26`). The commit body should say so.

## Checked after round 2

- Round 2's full-diff item: `diff` of the live 0.25.1 file against the staged file prints the hunks `5c5`, `16c16`, `142,143d141`, `186d183`, and the staged copy round 2 read is byte-identical to the committed one (`cmp`).
- Its MCP item: `getJiraIssue` (cloudId `openmrs.atlassian.net`, TRUNK-6682, `fields` summary, description, status, comment, labels, fixVersions) returned `labels: [backport, community-priority, performance]`, `fixVersions: []` and the comment; its schema's default field list leaves out `comment`.
- Its prune-B item: `8fb4b4bd` (pr-review 0.8.0, 2026-06-17) added the anti-pattern as the fourth home of the rule it also put in Step 1, the body rule and both gate lines. No measurement stands behind it.
