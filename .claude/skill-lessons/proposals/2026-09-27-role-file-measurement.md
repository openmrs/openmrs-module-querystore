# 2026-09-27 — owner-directed: does pr-harden 0.36's role-file move work? Bar set BEFORE any run

pr-harden 0.36.0 (`afd4dcc`) moved the verifier's procedure and the fixer's brief out of SKILL.md into
`verifier.md` and `fixer.md`, which the subagent is told to read first. The brief-to-file moves still
queued — pr-harden's reviewer brief, resolve-ticket's Step 3 refuter — use the same mechanism, so they
wait on this result. At the time of writing, no session has run on pr-harden ≥ 0.36.0: the newest pool
log is `20260926T194811Z-retro.jsonl`.

## The question

When the orchestrator spawns a fixer or a verifier, does its brief give the role file's absolute path,
and does the subagent then read that whole file before it acts?

## The instrument

`.claude/bin/role-file-check.py`, over the session transcripts under `~/.claude/projects/`, joining each
orchestrator `Agent` call to its subagent through `subagents/agent-*.meta.json`'s `toolUseId`.

- **Treated sessions** are those whose loaded pr-harden text carries the 0.36 pointer (`` `fixer.md` in
  this skill's directory ``). Only they count.
- A **fixer** or **verifier** spawn is recognised by its description or brief. The check does not use
  the role file's own mention to decide this, because a brief that fails to name the file is the case
  being looked for.
- **(a) path:** the brief contains the absolute path of the role file under the base directory the
  session printed for pr-harden.
- **(b) read in full, before acting:** the subagent's reads of that file together cover every line
  of it, in messages sent before the message holding its first act.
  - **What counts as a read:** a `Read` (its range, with the file's length taken from the transcript,
    as the session saw it) or a Bash segment that shows the file — `cat`, `sed -n 'A,Bp'`, `head -N`,
    `tail -n +K`, `awk 'NR>=A && NR<=B'`. The path may be spelled absolute, with `~` or `$HOME`, or
    relative after a `cd`.
  - **A fixer acts** at its first edit: an `Edit`/`Write`, or a Bash command that writes outside the
    temp directory, including running a scratch script that writes. Its brief says to read the file
    "before it does anything else", as the verifier's does (pr-harden 0.36.2; "before it edits anything"
    until then, see the sixth revision). Since the fifth revision, this act only feeds the hand-check
    advice.
  - **A verifier acts** at its first Bash command that is not a read of the file, or at its first
    edit. Its brief says "before it does anything".
  - **Calls sent together in one message** run before any of their results is seen. So a read that
    shares a message with the act does not count as before it.
- **Items**, secondary and reported rather than gating. The pointer lists them:
  - for a fixer: a finding id or the findings, the build, the commit rules;
  - for a verifier: the round, the head sha, and, where an earlier verifier ran in the session, its
    repairs.

## The bar, fixed now

The bar decides only what a transcript settles; see the fifth revision below for why.

- **FAIL:** a treated spawn whose brief does not name the file's absolute path, or which never reads
  the whole file. Fix the pointer, or the mechanism, before any further brief-to-file move. One miss
  is enough, because the move exists so that a rule is read by the agent that must follow it.
- **PASS:** there are at least 3 treated sessions that spawn a fixer or verifier (ordered by their
  first timestamp), and every treated spawn's read is CLEAN. Clean means the whole file was read
  before the subagent's first call that is not a pure read, so nothing can have acted before it.
- **HAND CHECK:** a treated spawn that read the whole file only after other calls. The tool lists it
  with the act detector's verdict as advice, and its transcript is read by hand before the bar is
  called. A brief that says "read this first" is followed by a clean read, so for a subagent doing
  as told this should be the rare case.
- **Items:** an item missing in 2 or more of the 3 sessions is a finding to fix. It is not a FAIL of
  the mechanism.

## Calibration, before any treated session is read

- **Known-negative:** on the pre-0.36 sessions already on disk, no fixer or verifier spawn may meet (a)
  or (b), because none had a role file to name.
- **Planted cases:** fixtures must classify correctly — a full read, a `limit`-truncated read, no read,
  a read after the first edit, a brief without the path, and a round-2 verifier brief without the
  earlier repairs.
- **Why (b) is checked rather than assumed:** a partial read has already happened in this pipeline. On
  #229 a subagent read `~/.claude/skills/pr-review/SKILL.md` with `limit: 120`.

## The slower comparison, reported and not gating

A baseline taken now over the run records dated before 0.36.0: the share of records whose *Where a
skill blocked or contradicted this run* section names pr-harden's VERIFY or FIX, the verifier, or the
fixer. The same count over the records after it is the before/after. It is a proxy for friction with
those rules, and it is read beside the records rather than instead of them.

## Calibrated and measured 2026-09-27, before any treated session

- **`--selftest`: 23 planted cases, all PASS.**
  - The planted transcripts cover: a full read, a `limit` read, no read, a read after the first edit,
    a brief without the path, a verifier `cat` before deploy, `head -50`, and the round-2 repairs item.
    They also cover an untreated session, four reviewer shapes, a silent description falling back to
    a stated role, and an unplaceable spawn being listed rather than dropped.
  - Eight record lines pin the friction pattern.
  - Mutating the full-read test (every `Read` counts as full) fails the truncated-read case.
  - Restoring the first classifier fails all four reviewer cases. That classifier had counted
    reviewers whose briefs name the fixer, the verifier and the standalone as fixers or verifiers.
    Seen on the real sessions as "pr-harden round N review" and "blocking-only confirm of merged head".
- **Known-negative, on every real session that loaded pr-harden:**
  - 112 sessions, 0 treated.
  - 312 fixer or verifier spawns: 202 fixers and 110 verifiers, sampled by their rarest description
    shapes.
  - 0 of those spawns meet (a) or read a role file.
  - 21 unclassified spawns — harden cycle agents, hook proofs, merges — are listed, not counted.
- **Slower comparison, baseline:** 39 of the 130 records before 2026-09-27 (30%) name pr-harden's
  VERIFY or FIX, the verifier, or the fixer in *Where a skill blocked or contradicted this run*.
  It is counted per record; see the revision below.

Run it with `.claude/bin/role-file-check.py` for the bar, and add `--baseline --after 2026-09-27` for
the comparison.

## Revised 2026-09-27, before any treated session, after a fresh-agent review

The review raised 4 blocking findings and 5 others. Each change below quotes its trigger. The (b)
definition, the bar and the baseline above are the revised forms.

- **"146 of 202 fixers edit through Bash … and never call either tool."** Measured here as 152 of 202.
  So a fixer acts at its first edit of any kind, not at its first `Edit`/`Write`.
  - The write detector is shell-aware:
    - a `>` inside quotes, an awk program or a data heredoc is not a redirection;
    - Python writes count only in code that runs;
    - `git merge-base` is not `git merge`, `git stash list` is not `git stash`, and `perl -Mstrict`
      is not `perl -i`;
    - a scratch write under the temp directory is not an edit (`$VAR` targets are expanded);
    - running a scratch script that writes is one.
  - Calibration, as re-measured in the second revision below: 195 of 196 real fixers have a detected
    first edit. The 196th made six read-only calls, then hit a session limit, and never edited. A
    random 14 detected edits were each a real edit once the false positives were fixed. There were four:
    an awk `>=`, a sed pattern's `<…>`, a scratch `fix.py`, and `$SP` backups.
- **"a real verifier's first counted 'act' is usually a read-only `ls …/appdata/modules` … 33 of 108
  verifiers ran `mvn` before it."** A verifier now acts at its first command that is not a read of
  the file, which is what "before it does anything" says.
- **"The first 3 sessions are taken in path order, not in time order … the tool never checks spawns
  outside those three."** Sessions are ordered by their first timestamp, and a miss in any treated
  session is a FAIL, so the bar above no longer has an unchecked tail.
- **"Of the 32 reviewers that opened that file, 9 come back `none`."** This was a positive control
  on real reads of `~/.claude/skills/pr-review/SKILL.md`. Read detection now accepts every spelling
  of the path, judges a Bash command per segment, adds up coverage across reads, and takes the
  file's length from the transcript. An unknown length makes a limited read partial, never full.
  - Re-run: of 76 agents whose own calls touch that file, all 49 with a whole-file read came back
    `full` (0 misses), and 6 more were full through ranged reads that add up to the whole file.
    - This overstated it: 17 of those 49 saw only a truncated preview. The third revision below
      corrects it.
  - The other 21 are real partial reads, greps, or reads of a different copy of the file (the repo's
    `.claude/skills/…`, not `~/.claude/skills/…`).
- **"Proposal `:21-23` is false."** The fallback that classified a spawn by the role file's mention
  is removed, so that sentence is now true. A description-silent spawn falls back only to a role
  its brief states; otherwise it is listed as unclassified.
- **"Proposal `:79` is false … counted per record, it is 40 of 131."** The baseline now splits files
  into records at their headers. The tool counts 39 of 130.
- **"The known-negative check cannot catch this … its 0 was guaranteed."** Conceded. The
  known-negative still reads 0 of 312, and the pr-review positive control above is the calibration
  of the read detector that could have failed.
- **The treated-session marker** is matched with its whitespace collapsed, so re-wrapping the
  pointer line cannot un-treat a session.

`--selftest` is now 57 planted cases, and all pass.

## Revised again 2026-09-27, before any treated session, after the confirming review

The confirming review raised 4 more blocking findings. Each change quotes its trigger.

- **"Tool calls sent together count as 'read before acting' … 67 of 110 real verifiers start with
  several calls in one message."** Calls are now grouped into turns by `message.id`. A read counts
  only in an earlier turn than the act.
- **"`cat X | head`, `| sed -n`, `| wc -l` and `cat X > file` count as whole-file reads."** A
  pipeline is judged whole. `cat X | head -50` shows 50 lines, `cat -n X | sed -n 95,160p` shows a
  window, and `cat X > f` shows nothing.
- **"Harden's agents that run after pr-harden loaded are counted as pr-harden's."** A description
  naming a harden cycle or phase ("Harden cycle 2 fixer", "Cycle 4 verification pass") is harden's
  own agent, and is neither counted nor listed.
- **"Proposal `:108` says '200 of the 202 real fixers have a detected first edit'. The tool as written
  gives 199."** The count is corrected above to the tool's own number after these changes: 195 of 196.
  Two of the misses that finding named edited through an imported scratch helper. That now counts as
  an edit, as does running a scratch script that writes. Writing such a script under the temp
  directory is not an edit by itself, and nor is any other Write or Edit there.
- **"`:103` says the brief is read 'for a stated role or the role file itself'. The code never uses
  the role file."** The docstring now says "only for a role it states".
- **Notes, also fixed:**
  - a spawn a hook refused (an error `tool_result` on the Agent call) is skipped rather than
    scored as a MISS;
  - `--against ""` exits 2;
  - skill-retro Step 4 says a new file's budget rises from 0.
- **Re-measured:**
  - the known-negative is 0 of 305 pre-0.36 spawns;
  - the pr-review positive control has all 49 whole-file reads full, with 0 misses;
  - `--selftest` has 70 planted cases, and all pass.

## Revised a third time 2026-09-27, before any treated session, after the second confirming review

It raised 2 blocking findings and some notes. Each change quotes its trigger.

- **"`bash_reads` splits the command on `&&` before it looks at quotes … so the awk pattern … can never
  match."** Commands and pipelines are now split outside quotes only. On the real transcripts, all 71
  plain `awk 'NR>=A && NR<=B' FILE` reads are credited with their exact range. The 249 piped ones and
  the 3 redirected ones are judged by what the pipe or the redirection leaves shown.
- **"proposal `:156-157` says a Write of a scratch script that writes 'counts as an edit'."**
  Corrected above to what the code does.
- **"When a Bash `cat` prints more than 30,000 characters, Claude Code saves the output to a file and
  shows the agent about 2KB … 17 of the tool's 52 'full' verdicts rest on such a preview."**
  - A read whose result was persisted as a preview, or was an error, is now void.
  - The positive control, re-run on `~/.claude/skills/pr-review/SKILL.md`:
    - all 31 agents whose whole-file read was actually shown come back `full`;
    - the 17 whose only whole-file read was a preview come back `partial`;
    - 1 of the preview-only agents comes back `full` anyway, through ranged reads shown in full.
  - The role files are well under 30,000 characters (verifier.md 10.7KB, fixer.md 6.7KB), so this did
    not bear on them. It did bear on this calibration.
- **Notes, also fixed:**
  - a verifier command that shows the file and runs something else in the same call is an act in the
    same turn as the read;
  - the verifier's items now include the behaviour to drive.
- **Known limit, kept:** a Bash ranged read with no `Read` result is judged against the file's
  length on disk when the tool runs, not at session time. The tool's docstring says so.
- **Re-measured:**
  - the known-negative is 0 of 305;
  - `--selftest` has 76 planted cases, and all pass.

## Revised a fourth time 2026-09-27, before any treated session, after the third confirming review

It raised 1 blocking finding and some notes. Each change quotes its trigger.

- **"A temp-dir scratch write counts as the fixer's act … In 14–16 of the 195 real fixers, the
  detected first edit is really a scratch write."**
  - Every write target is now resolved before it is judged:
    - against the command's own `cd`s and `VAR=` assignments, with quotes honoured;
    - `sed -i` and `perl -i` by their file arguments;
    - a copy's destination with redirection words set aside;
    - Python writes by a literal target, or by a variable the code assigns a literal.
  - A scratch script counts as writing only if its own targets leave the temp directory.
  - Calibration:
    - 194 of 196 real fixers have a detected first edit. The other two made none: one only probed
      under `/tmp`, and the other read six times and hit a session limit.
    - A random 10 of the 52 first edits whose command mentions a temp path were each a repo edit, or a
      run of a scratch script whose targets are repo files.
- **Notes, also fixed:**
  - `cat "$F"` with `F` assigned in the same command is a read;
  - a comment, `set`, an assignment, `export` or `echo` stage is not an act;
  - awk's strict bounds are exact (`NR>0 && NR<143` is 1..142);
  - a spawn whose Agent call ended in an error is skipped only when it has no transcript, so an
    interrupted agent that ran is still scored;
  - PASS waits until no treated spawn is unclassified.
- **Expected, and stated so a FAIL is read correctly:** all 109 pre-0.36 verifiers acted at their
  very first call, an orientation command such as `git rev-parse` or a `grep` of `config.xml`.
  - The pointer says to read `verifier.md` "before it does anything". So a 0.36 verifier that
    orients first reads late. Under the fifth revision's bar that makes it a hand check, not a
    FAIL, and it is the likeliest reason for one.
  - The owner's decision on the wording is recorded in the sixth revision.
- **Kept as known limits:**
  - an awk program with an action (`NR>=A && NR<=B {printf …}`);
  - `NR` across several files;
  - an apostrophe inside a shell comment (38 of 113,888 real commands);
  - a role file that itself quotes "Output too large".
- **Re-measured:**
  - the known-negative is 0 of 305;
  - the positive control has all 31 shown whole reads full and all 17 previews partial;
  - `--selftest` has 91 planted cases, and all pass.

## Revised a fifth time 2026-09-27, before any treated session, after the fourth confirming review

**"Wrong FAIL: scratch-only steps are still scored as a fixer's first edit … Wrong PASS: real repo
edits go undetected."** The fourth confirming review found both by inserting a read into real fixer
transcripts and running the real `report()`.

These were the latest shapes of one class: deciding from shell text whether an agent had already
edited something. Reviews 1, 2, 4 and 5 each found new shapes of it, among them Bash-only edits,
scratch writes, quoted and `cd`-relative targets, `bash -n`, `python3 - "$F" <<` and
`git checkout <ref> --`. That is not converging. So the bar no longer rests on it:

- **Mechanical:**
  - FAIL, when the brief lacks the path or the file is never read whole;
  - PASS, when every read is clean.

  Neither uses act detection.
- **Hand check:** a whole read that came after other calls is listed with the act detector's verdict
  as advice, and read by hand.
- **The act detector is kept for that advice.** The four simple misses the review named are fixed:
  - `bash -n` runs nothing;
  - `python3 - "$F" <<` is an interpreter heredoc;
  - `git checkout <ref> -- <paths>` is an edit;
  - a scratch script run by its relative name after a `cd` is found by name.

  A write whose target is a variable not assigned in the same code, such as `open(sys.argv[2], 'w')`
  or `open(SP + "/x", 'w')`, still counts as an edit. Under this bar that can only send a spawn to a
  hand check, never FAIL it.
- **The review's own counterfactual, re-run against this code:**
  - the helper-edit case and the `python3 - "$F"` case are now detected as edits, so the inserted
    read after them fails (b);
  - the `bash -n` case is now read-before-act;
  - the `argv` scratch case is still scored after-act. Under this bar it goes to a hand check, not a
    FAIL.
- **`--selftest`: 98 cases, all pass.** They include the three bar outcomes:
  - three sessions with clean reads PASS;
  - a whole read after a `git status` goes to a hand check and does not PASS;
  - a read cut to 20 lines FAILs.
- **The known-negative is still 0 of 305.**

## Revised a sixth time 2026-09-27, before any treated session: the owner's decision on the wording

The owner kept the verifier's "before it does anything", and aligned the fixer's pointer and the
`fixer.md` header to "before it does anything else" in pr-harden 0.36.2.

Under the fifth revision's bar, only a read that comes before every other call is decided
mechanically. So a fixer following the old "before it edits anything" could run `git status`, read
its file, and still land in a hand check although it did what it was told. With the change, a
compliant fixer's read is clean, as a compliant verifier's is, and "read this first" is also the
simplest instruction for an agent to follow. Both files grew by one word, and the two rises are
recorded in `skill-budgets.json`'s `raises`, the ratchet's first use on pr-harden.

## Result, wave 1 (#246, #463, #528), read 2026-09-28: PASS

- **The bar is met.** Wave 1 was pr-harden 0.36.2's first pool run. Three treated sessions spawned a
  fixer or a verifier: 4 fixers and 2 verifiers, both verifiers in #528. All 6 briefs named the role
  file's absolute path. All 6 spawns read the whole file in their first call: three fixers with
  `cat`, and the fourth fixer and both verifiers with an unranged Read. No output was a preview.
- **One hand check.** Five reads were clean mechanically. The tool sent 'PR 545 round 1 fixer' to a
  hand check, because its first call was `cat fixer.md; grep -n "^## \|^### " SKILL.md`, and a Bash
  call that also reads another file is not a pure read. Read by hand it is clean: the second segment
  reads SKILL.md's headings, nothing in the call writes, and no call came before it.
- **One unclassified spawn.** 'PR 546 blocking-only round 3' was round 3's reviewer: its brief opens
  "You are the round-3 reviewer of pull request #546", and its first call loads `pr-review`. The
  seventh revision below records why the tool still holds it for a hand check.
- **Items.** `round` was missing from both #528 verifier briefs. Verifiers ran in one session only,
  so the 2-of-3 rule cannot be applied to that item yet.
- **The likeliest cause of a hand check did not occur.** Neither verifier oriented before reading.
- **The known-negative is 0 of 295, not the 305 above, because four pre-0.36 sessions left the
  disk.** A report written at 2026-09-27T14:11Z, which read 305, lists #234, #236, #296 and #297,
  which ran on 27–28 August, with 10 spawns between them. Their transcripts and project directories are
  gone. None of the 108 sessions in both reports changed its spawn count, so 305 − 10 = 295.
  - What deleted them is a lead. `cleanupPeriodDays` is unset, so Claude Code's default 30-day
    transcript retention applies, which goes by when a transcript was last written. The oldest
    pipeline transcript left was last written on 2026-08-30 local time; sessions that began on 28
    August and were written later remain. The count first read 295 after the pool's sessions
    started.
  - If retention is the cause, it deletes wave 1's transcripts from about 2026-10-28.
- **What the PASS released:** pr-harden 0.37.0's `reviewer.md` and resolve-ticket 0.23.0's
  `refuter.md`. This tool does not measure those two files yet.

## Revised a seventh time 2026-09-28, after the verdict: wave 1's verdict is unchanged, later ones can differ

**"treated spawns that are neither a reviewer nor recognisably a fixer or verifier: 1 — read these by
hand: PR 546 blocking-only round 3"** The tool recognised a reviewer by its description alone,
`review|refut|confirm`, and this description names none of them. The owner agreed to let the tool
recognise a reviewer from its brief as well.

- **It does not, and that is this revision's result.** Fresh-agent reviews of four versions each
  found brief wording by which a fixer or a verifier would leave the count as a reviewer, which is
  the direction the bar exists to block.
  - The first version took the first role word: *"'You are the reviewer's fixer …' … the spawn is
    classified as a reviewer and dropped without being listed anywhere."*
  - The second excluded possessives: *"leaves the count just because its brief mentions the reviewer:
    'implementing the reviewer findings', 'what the reviewer found', 'the reviewer ran first'."*
  - The third also required the subagent to have run `pr-review`: *"It does this even when the same
    clause says the spawn is the fixer or the verifier."*
  - The fourth required the clause to name the reviewer alone: *"A spawn whose description names no
    role can say later in its brief that it is the fixer or the verifier … If the first window names
    only the reviewer and the transcript has a pr-review Skill call, the spawn silently leaves the
    count as a reviewer."*

  Like the act detector in the fifth revision, this did not converge, and a spawn's own `pr-review`
  call does not settle it either. So a brief never makes a spawn a reviewer. PR 546 round 3 stays a
  hand check, as at 83008d9.
- **What the revision keeps.** `role_of` now says what a spawn is:
  - `fixer` or `verifier`, which the bar measures;
  - `reviewer` or `harden`, which it does not;
  - None, which is unclassified.

  The rules are these:
  - **A description decides first.** Its reviewer words now match only at the start of a word, so
    "preview", "unconfirmed" and "irrefutable" no longer make a spawn a reviewer.
  - **A silent description falls back to the brief.** The brief's first "you are the", "you are a"
    or "you are an" clause that names a role decides, where the role starts within 40 characters and
    before a full stop. These are not roles: a possessive, a role joined to a hyphen on either side,
    and a role file's name (`fixer.md`). Case is ignored for ASCII letters only, so a dotless-i
    "fıxer" is not a role either, and is held rather than dropped. The clause places a fixer or a
    verifier only when that is the one role it names. A clause naming the reviewer, or naming two
    roles, is unclassified.
  - **Nothing it cannot place is dropped unlisted.** `check_session` no longer repeats the
    description regex. So a description that only contains "review" and states no role is listed,
    where 83008d9 dropped it.
  - **The hand-check message.** It now says "did not read the whole file before the message holding
    their first call that is not a pure read", which is the CLEAN definition. The bar's HAND wording,
    "only after other calls", is read as "not CLEAN", as the tool always decided it. The old message
    missed a read made in the same call or message as something else, as PR 545's was.
- **What it changes for later runs, against 83008d9.** For a spawn whose description names no role:
  - "you are an" now states a role, where "You are an independent fixer" was unplaced;
  - the first clause that names a role decides. 83008d9 took a verifier from any clause before a
    fixer, so "You are the fixer for round 2. You are the one the verifier waits on." read as a
    verifier;
  - a clause naming two roles, or the reviewer, is held. 83008d9 read "You are the fixer; the
    verifier ran in round 1" as a verifier, and "You are the fixer for round 2 of PR 546; the
    reviewer found two blockers" as a fixer;
  - a possessive no longer counts, where "You are the verifier's fixer" read as a verifier;
  - a role joined to a hyphen no longer counts, where "the fixer-reviewer" read as a fixer;
  - a role file's name no longer counts, where "you are the agent for fixer.md" read as a fixer.

  For any spawn, a description's reviewer words match only at the start of a word. So "Verify the
  preview endpoint" is a verifier where it was a reviewer, and "Rereview PR 9 after the fix" a fixer.
  Items follow the roles: a spawn now held is no longer an earlier verifier for the next one's
  `repairs` item. Items never gate.

  Within classification, a spawn leaves the count only as a `reviewer` or as `harden`, and both now
  come from the description alone. `harden`'s words are unchanged, and the reviewer's match a subset
  of what they matched at 83008d9. So no change takes a spawn out of the count. Each change does one
  of three things:
  - it holds a spawn;
  - it counts one that 83008d9 held or skipped;
  - it changes which role file a counted spawn is checked against. That can turn a FAIL into a PASS
    where 83008d9 checked the wrong file.
- **Kept as a known limit:** a description is still decided by its words alone, as at 83008d9. So a
  fixer described "Fix review findings round 2" is a reviewer's, and it leaves the count.
  - Among the spawns the tool classifies, those made after pr-harden loaded, on the sessions on disk
    on 2026-09-28, no description classified as a reviewer names a fixer or a verifier.
  - Seven classified as harden do. All seven are named by harden's cycles or phases, such as
    "Harden cycle 2 fixer" and "Cycle 4 verification pass".
  - The shape does occur outside that population: "Fix PR 392 review threads" is a fixer, briefed to
    "Address the four outstanding review threads on PR #392 … push the fixes", in a session that
    never loaded pr-harden.
- **Re-run on every session against 83008d9:** the `--json` report is byte-identical, and the text
  report differs only in that message.
- **`--selftest`: 128 cases, all pass.** The new cases cover:
  - wave 1's reviewer held although it ran `pr-review`;
  - briefs that name the reviewer, in the first role-naming clause or beside a fixer;
  - "an … fixer";
  - the possessive, with either apostrophe and either role first, and a role in single quotes,
    straight or curly, which still counts;
  - hyphens on either side;
  - a capitalised role, and a dotless-i "fıxer", which is not one;
  - two-role clauses;
  - the 40-character window at its boundary, and the full stop;
  - the first clause against a later one, and a role-less clause passing the decision on;
  - the 400-character head;
  - a role file's name;
  - description words that only contain "review", "refut" or "confirm", including one that states
    no role.

  A mutation of each rule, made on a scratch copy, reddens at least one of them. A control copy stays
  green.

## Revised 2026-09-28, after both verdicts: an errored result is no longer void

The third revision above voided a read whose result "was persisted as a preview, or was an error".
The second move's wave-2 run showed that an error result can carry the whole file. Two reviewers'
`cat reviewer.md; echo ======` printed the file before zsh failed on the separator. The detector
this move shares now treats such a result as unsettled:
- it keeps a spawn from FAIL and from PASS, and sends it to a hand check;
- a preview is still no read;
- a result saying "No such file or directory" counts as errored too.

On this move's sessions the report is byte-identical before and after. The change is recorded in
full in `2026-09-28-reviewer-refuter-measurement.md`.
