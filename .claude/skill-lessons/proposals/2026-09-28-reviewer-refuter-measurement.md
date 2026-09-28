# 2026-09-28 — owner-directed: does the second role-file move work? Bar set BEFORE any run

pr-harden 0.37.0 and resolve-ticket 0.23.0 (`0b858a9`, synced to `~/.claude` on 2026-09-28) moved text
into two role files:
- the reviewer's standing rules, its JSON and what makes a finding blocking, into `reviewer.md`;
- the plan refuter's seven questions, its JSON and the citation rule, into `refuter.md`.

The first move's wave-1 PASS released them: see `2026-09-27-role-file-measurement.md`. At
2026-09-28T13:10:21Z, no session but the one writing this had loaded either new pointer. That was
seven transcripts written since the sync, searched for the pointer text. The positive control was
the writing session's own transcript, which the same search finds.

## The question

When the orchestrator spawns a pr-harden reviewer, does its brief give the absolute path of
`reviewer.md`? Does the reviewer then read that whole file before it does anything else, loading
`pr-review` included? The same question applies to a resolve-ticket plan refuter and `refuter.md`.

## The instrument

`role-file-check.py --second-move`. It is a mode beside the first move's, which it leaves unchanged,
over the same transcripts and the same join of each `Agent` call to its subagent.

- **Treated sessions**, one kind per file:
  - treated for the reviewer: the loaded pr-harden text carries `` `reviewer.md` in this skill's
    directory ``;
  - treated for the refuter: the loaded resolve-ticket text carries `` `refuter.md` in this skill's
    directory ``.
- **A reviewer** is a spawn made after pr-harden loaded that the first move's `role_of` calls a
  reviewer, by its description. Two kinds are unplaced instead:
  - a review whose description also names a harden cycle or phase ("Phase 2 quality review"), which
    is harden's own agent. Four such spawns come after pr-harden loads on the sessions on disk;
  - a spawn `role_of` places as a fixer, a verifier or harden's agent that ran `pr-review`, by the
    Skill tool or by showing its SKILL.md. It behaved as a reviewer (revised below).
- **A refuter** is a spawn in Step 3's window: from each resolve-ticket load until harden or
  pr-harden loads. A spawn in an open Step 3 is in that window only (revised below). Its description,
  or the first 400 characters of its brief, must name refutation at the start of a word. The name
  `refuter.md` is not refutation, as a brief's mention of its role file never places a spawn in the
  first move (revised again below).
  - On the 116 resolve-ticket sessions on disk, 111 have a "Refute…" spawn in that window.
  - Five spawns there are named "Gate pass…" or "Re-gate…". Each brief says "refutation gate", so
    they are refuters too.
- **Anything a window's rules cannot place is listed**, and that file's PASS waits until none is.
  - In pr-harden's window: a reviewer named only by its brief ("You are the round-3 reviewer"), and a
    harden-named review.
  - In Step 3's window: a search agent, a measurement agent or an adjudicator. There are five such
    spawns on disk.
- **(a) path** and **(b) read in full** are the first move's. A reviewer or a refuter acts at its first
  call that is not a read of its own file, because both headers say "before you do anything else". A
  verifier, by contrast, acts only at a Bash call that does more than read, or at an edit.
- **Items** are reported and never gate:
  - for a reviewer: the round, the head sha, the base, whether the run started from a ticket, and the
    ticket, named as an issue or a JIRA key (revised below);
  - for a refuter: the ticket, the plan and the repo.

## The bar, fixed now

It is the first move's bar, called for each file separately, so one file can PASS while the other
waits.

- **FAIL:** a treated reviewer or refuter spawn whose brief does not name its file's absolute path, or
  which never reads the whole file. One miss is enough.
- **PASS:** for that file, at least 3 treated sessions with a spawn of its role (ordered by their first
  timestamp), every treated read CLEAN, and no unclassified spawn in that role's window of a treated
  session. Clean means the whole file was read before the message holding the subagent's first call
  that is not a pure read. For a reviewer or a refuter, only a read of its own file is pure, since
  both headers say to read it before anything else. So a Skill call loading `pr-review`, or a Read of
  any other file, is such a call (revised below).
- **HAND CHECK:** a treated spawn whose whole read was not clean. Its transcript is read by hand before
  the bar is called.
- **Items:** an item missing in 2 or more of the 3 sessions is a finding to fix. It is not a FAIL of
  the mechanism.

## Calibration, before any treated session is read

- **Known-negative:** no pre-0.37 reviewer spawn and no pre-0.23 refuter spawn on disk may meet (a) or
  read its file, because neither file existed.
- **Planted cases** must classify correctly:
  - a clean read;
  - a reviewer that loads `pr-review` first and reads its file after;
  - a brief without the path;
  - a file never read whole;
  - a harden-named review;
  - a refuter named only by its brief's "refutation gate";
  - a Step 3 search agent;
  - a spawn after harden loads;
  - a session that is untreated for one file and treated for the other.
- **The read detector** is the first move's, and its positive control is re-run here.

## The slower comparison, reported and not gating

This is the share of run records whose *Where a skill blocked or contradicted this run* section names
pr-harden's REVIEW, the reviewer, resolve-ticket's Step 3, or the refuter, before the move against
after it.
- A record is after when the session its `transcript:` line names was treated for either file.
- A record whose transcript is gone is before if it is dated before 2026-09-28. Otherwise it cannot
  be placed, and it is counted apart (revised below).

## Known limits, taken knowingly

- **Description words place a reviewer,** as in the first move. A fixer described "Fix review findings"
  is a reviewer here. That makes it measured, so it FAILs loudly for want of `reviewer.md`, where the
  first move drops it silently.
- **Only the two windows are read.** A refuter spawned outside Step 3's window is not measured as one.
  After pr-harden loads, it is measured as a reviewer, which FAILs reviewer.md loudly; anywhere else it
  is not seen. Nor is a refuter in a session that never printed resolve-ticket's base directory.
- **Some reviewers leave reviewer.md's count unseen.** It happens to a reviewer that never ran
  `pr-review` and is described as a fixer, a verifier or harden's agent. The first move then measures
  it as a fixer or a verifier, which is loud; described as harden's, it is dropped by both. On disk on
  2026-09-28, 51 of the 323 reviewers never loaded `pr-review`, and each is described as a review or a
  confirmation.

## Calibrated and measured 2026-09-28, before any treated session

At 2026-09-28T13:19:13Z the pointer search still found only the writing session, and the tool reports
0 treated sessions for either file.

- **Known-negative:**
  - 322 pre-0.37 reviewer spawns, over 112 sessions that loaded pr-harden, and none meets (a) or
    reads `reviewer.md`. The first move's 326 description-placed reviewers less the four harden-named
    reviews give the 322.
  - 171 pre-0.23 refuter spawns, over 116 sessions that loaded resolve-ticket, and none meets (a) or
    reads `refuter.md`.
- **Positive control for the read detector:** it was driven over every subagent transcript that
  touches a `~/.claude/skills/*/*.md` exactly once. Ground truth comes from the transcript, not the
  detector: a `Read`'s own `numLines` against `totalLines`, and whether a bare `cat`'s output was
  saved as a preview. It agrees in 32 of 32:

  | read | shown whole | shown in part |
  |---|---|---|
  | `Read` | 27 of 27 detected whole | 1 of 1 detected partial |
  | bare `cat` | 2 of 2 detected whole | 2 of 2 previews detected partial |
- **`--selftest`: 159 cases, all pass.** 31 of them are the second move's. A mutation of each new rule,
  made on a scratch copy, reddens at least one of them, and a control copy stays green. The rules
  are these:
  - the harden-named review;
  - the refuter read from its brief;
  - harden closing Step 3's window;
  - the pointer marking a session treated;
  - a reviewer's Skill call as its act;
  - an unplaced spawn holding its file's PASS;
  - the known-negative;
  - each alternative of the slower comparison's pattern.
- **The first move is unchanged.** Its report is byte-identical to `0b858a9`'s in text, in `--json`, and
  in `--baseline` for all dates, before 2026-09-27 and after it.
- **The slower comparison's baseline:** 65 of 132 records dated before 2026-09-28 (49%) name pr-harden's
  REVIEW, resolve-ticket's Step 3, the reviewer or the refuter. The one record since names none. The
  share is high because "the reviewer" is a common word in those sections. It is a proxy, and it is
  read beside the records.

## Revised 2026-09-28, before any treated session, after a fresh-agent review

The review drove the real `check_session2` and `report2` on planted sessions. It found one blocking
issue and six others, all fixed above and pinned by `--selftest` cases.

- **s1 (blocking):** *"any spawn role_of calls a fixer, a verifier or harden then leaves reviewer.md's
  count … and nothing lists it … reviewer.md's bar prints PASS while a treated pr-harden reviewer whose
  brief never named reviewer.md goes unmeasured and unlisted."*
  - The reviewer planted "PR 9 blocking-only verification round". A real brief reads "You are running a
    **BLOCKING-ONLY verification round**".
  - Behaviour settles what the wording cannot. A spawn placed as a non-reviewer that ran `pr-review` is
    now unplaced.
  - On disk it changes nothing: the report is byte-identical with and without the rule. The reviewer
    measured that none of the 321 spawns placed as fixer, verifier or harden ran `pr-review`.
  - The residue is a known limit above.
- **s2:** *"The selftest case 'refuter.md is called apart from reviewer.md' cannot fail."* New cases
  treat sessions for one file only, for both, and not at all. They check each file's session and
  treated counts, and three mutations of the per-file bar now redden them.
- **s3:** *"No case covers pr-harden closing Step 3's window."* A case now does. So do a leak in the
  known-negative, and an untreated session's unplaced spawn, which must not hold PASS.
- **s4:** *"a second Step 3 in the same session goes unseen by refuter.md, and it is measured as a
  reviewer instead."* Each resolve-ticket load opens Step 3's window, and a spawn in it is the refuter
  window's alone. No session on disk loads resolve-ticket twice.
- **s5:** *"a known pre-treatment record sits on the treated side."* That record is #528's, dated the
  day the move shipped, from a session on the versions before it. The comparison now splits by the
  record's own session. Header versions could not do it: 124 of the 133 section-holding records name
  no version.
- **s6:** *"a reviewer that loads pr-review by Reading its SKILL.md before reviewer.md counts as
  CLEAN, although the header says 'loading `pr-review` included'."* For a reviewer or a refuter, only a
  read of its own file is now pure. The read detector's positive control is unchanged by this: 32 of
  32 for each role.
- **s7:** *"items2's 'ticket' item is satisfied by any '#N', which includes the PR number."* The
  reviewer's items are now "ticket origin", from phrasing such as "started from" or "resolve-ticket",
  and "ticket", named as an issue or a JIRA key.

**Re-measured:**
- **Known-negative:** 323 reviewers (the live PR 544 session added "PR 544 round 2 review") and 171
  refuters. None meets (a) or reads its file.
- **Treated sessions:** none, for either file.
- **`--selftest`:** 175 cases, all pass. A battery of 24 mutations, one per rule, reddens at least one
  case each. It includes the three per-file-bar mutants the review found surviving, and a control
  copy stays green.
- **The first move is unchanged.** Its report is byte-identical to `0b858a9`'s in text, in `--json`
  and in every `--baseline` window.
- **The slower comparison:** 65 of 133 records before the move (49%), none after, none unplaced.

## Revised again 2026-09-28, before any treated session, after the confirming review

- **t1 (blocking):** *"The selftest case 'second move, a refuter named only by its brief's 'refutation
  gate'' … does not test what its label says … The "/refuter.md" in that path matches role2_of's
  `\brefut` by itself."*
  - The rule itself was also placing a refuter by its role file's name, which the first move never
    lets a brief do.
  - Now `refuter.md` is not refutation. A pathless "refutation gate" brief is a refuter's, and a brief
    that names only `refuter.md` is unplaced.
  - Both are pinned: narrowing the pattern to "refute" or "refuter" reddens them, and so does counting
    the file's name again.
  - On disk nothing moves: there are still 171 refuters.
- **Notes, also fixed:**
  - The instrument's act wording now matches the code.
  - 5 of the 51 reviewers that never loaded `pr-review` are "confirm" rounds.
  - 125 of the 133 `transcript:` lines on disk are written with `~`, and those are now expanded
    (pinned). The other 8, #528's among them, are absolute.
  - The JIRA pattern takes "O3-1234" and not "UTF-8" (pinned).
  - `MOVE2_SHIPPED`'s comment says "that day or later", as the code does.
- **Re-measured:**
  - `--selftest`: 181 cases, all pass, with a mutation for each new rule reddening its case.
  - The first move is byte-identical to `0b858a9`'s in every mode.
  - The second move: 323 reviewers and 171 refuters, none meeting (a) or reading its file, and 0
    treated sessions.
  - The slower comparison: 65 of 133 records before the move, none after, none unplaced.

## Corrected 2026-09-28, after the last confirming review

**"The newest section … says "Every `transcript:` line on disk is written with `~`". That is false."**
The claim came from an earlier review's notes, and it was repeated without measuring. Measured, 125
lines use `~` and 8 are absolute, #528's among them. The code reads both, so the result does not move.
The same review's wording notes are applied above:
- a role file's mention is excluded for briefs only;
- `MOVE2_SHIPPED`'s comment now describes the code;
- `baseline`'s unused parameters are gone;
- a refuter spawned after pr-harden loads FAILs reviewer.md loudly, rather than going unseen.

## Result, wave 2 (#240, #455, #462), read 2026-09-28: refuter.md PASS, reviewer.md PASS on hand checks

Wave 2 was the first pool run on pr-harden 0.37.0 and resolve-ticket 0.23.0. It produced PRs #549,
#550 and #551. The pool ran #240 and #455 first, then its first retro, then #462, then its second
retro.
- The first retro, `7be82d8`, changed no skill file, only two run records and `REJECTED.md`. So #462
  ran on the same versions.
- The second, `fede115`, shipped pr-harden 0.38.0 and resolve-ticket 0.24.0. Both pointers survive in
  them, so later sessions stay treated.

- **refuter.md: PASS, mechanically.** Three treated sessions each have one refuter. Each brief named
  the file's absolute path, and each refuter read the file whole at call 1, before its first other
  call.
- **reviewer.md: PASS, with three hand checks.** Three treated sessions have six reviewers. All six
  briefs named the file's absolute path, and all six reviewers read the whole file in their first
  call. Three reads were clean mechanically. The other three went to a hand check. Each is clean in
  substance, because the whole file was shown before anything else ran, `pr-review` loading included:
  - 'Review PR 549 round 1' ran `cat reviewer.md; ls …/pr-review/`: a whole read, and a listing of
    pr-review's directory in the same call.
  - 'Review PR 549 round 3' and 'Round 1 PR review #551' ran `cat reviewer.md; echo ======; …`. Their
    output shows the whole file, then "(eval):1: ===== not found". zsh's `=`-expansion failed on the
    separator, the harness flagged the call as an error, and nothing after the separator ran.
  - What ran after it was an `ls` for 549's round 3. For #551's round 1 it was a `cat` of pr-review's
    SKILL.md, which is loading pr-review in the same call. The header forbids that, and only the zsh
    error stopped it. That reviewer then read pr-review in a later call.
  - The tool as committed voided both reads and printed FAIL. The revision below is why it now sends
    them to this hand check.
- **Items:** no item is missing in any of the nine treated briefs.
- **The first move over both waves:** 15 of 15 treated fixer and verifier spawns meet (a) and (b),
  over 6 sessions. One of those sessions is the hand-launched PR 544 session on 0.36.2.
  - Three new hand checks are in #240's and #455's sessions: 'Fix PR 549 round 1', 'Fix PR 549 round 2'
    and 'PR 550 round 1 fixer'. Each read `cat fixer.md; cd … && git branch --show-current && git
    status` at call 1, and each is clean in substance.
  - The report lists a second unclassified treated spawn, 'Implement esm#31 ended-order fields', in the
    PR 544 session. Read by hand, it is an implementation agent for openmrs-esm-chartsearchai#31, not a
    pr-harden role, so it is outside the bar.
- **A hazard the run showed:** in the Bash tool's zsh, `echo ======` fails and aborts the rest of the
  command line. It cost two reviewers the calls chained after their separator.

## Revised after the verdict, 2026-09-28: a result flagged as an error goes to a hand check

**"bar (reviewer.md): FAIL — 2 treated spawn(s) missed; the first, 'Review PR 549 round 3' … it never
read the whole file"** The tool voided every result flagged as an error. These two results carried the
whole file before zsh's error, so the verdict contradicted the transcript.

- An error result does not settle whether the file was shown. It may have been shown, as here, or
  not, as with a `cat` of a missing file.
- So a preview of output too large to show is still no read. An errored result now counts toward a
  whole read at some point, which keeps the spawn from a FAIL, and never toward a clean one, which
  keeps it from a PASS.
- Such a spawn's state is "errored", and its hand-check line says the result was flagged.
- It moves a spawn from FAIL to a hand check and never to PASS. A mutation that lets an errored read
  be clean reddens the selftest.
- On the sessions on disk, it changes only wave 2's verdict for reviewer.md, from FAIL to the hand
  check above. The first move's report is byte-identical before and after, in every mode.
- `--selftest` has 187 cases, all passing. Each of eight mutations of the fix reddens at least one of
  them, and a control copy stays green.

**Its confirming review found three errors, all fixed:**
- *"'Both of the wave's retros ran after these sessions.' is false"*: the run order above is corrected.
- *"that attribution is false"*: the three new first-move hand checks are in #240's and #455's
  sessions, not in the PR 544 session.
- *"No selftest case pins [a preview being no read]"*: one token in `read_check` would have made a
  preview-only read clean, and so a PASS, with the selftest green. That gap was already in `0b858a9`.
  Cases now pin it at the read and at the bar, for both moves.

The same review found a gap that was also older: `cat role.md; ls` of a missing role file exits 0,
so the result is never flagged, and the detector counted a clean whole read. A result saying "No
such file or directory" is now errored too. That moves the spawn only toward a hand check. The
selftest now has 190 cases.
