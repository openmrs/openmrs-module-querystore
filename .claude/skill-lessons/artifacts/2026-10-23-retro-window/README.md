# 2026-10-23 retro: the instruments behind its ledger entries

The 2026-10-23 `skill-retro` pass read five measurement notes and no run record. This folder keeps what
it ran beyond each note's own `scripts/`, so that its entries in `REJECTED.md` can be re-checked
without this session's scratchpad. The refutation gate made that a condition: the transcripts these
scripts read are being deleted (below), so a figure whose instrument is not kept here cannot be
checked again. They are also moved between project folders, so `transcript_presence.py` looks each
one up by its session id as well as by the path its record names.

Every script reads only and runs with `python3 -I <script> <args>`. The outputs beside them are
their stdout and stderr.

## `gate_sentences.py` and `controls/`: harden's gate sentences in assistant text

The script counts assistant `text` blocks that state each of harden's gate sentences in their
literal opening words. A block holding the skill's bracketed template counts apart and is never a
hit. Transcripts that are no longer on disk are listed as `MISSING`. The script's docstring names
the commit that added each sentence.

- **`controls.out`** has four control runs:
  - the two good controls, counted (all five patterns between them);
  - the bad control, the template in a user message plus a user message holding the phrase: one
    template block, no hit;
  - the Termination-template control: counted as loading that template, no hit;
  - an empty list: exit 2.
- **`gate-sentences-after-deletion.out`** covers the 66 transcripts in
  `../2026-10-23-harden-phase1-confirming-pass/public-transcripts.tsv`:
  - 64 read, 2 missing;
  - 58 load the template, and all 58 loaded a harden that already had the Termination gate;
  - 0 hits for the three Transition gate sentences and for the Termination gate's;
  - 1 hit for the Phase 2 gate's (chartsearchai-477's transcript `5092c704…`).
- **Before the deletion**, this pass ran two earlier versions of these patterns. Neither had the
  `MISSING` handling or the Termination-template count, and the first had only the Transition gate's
  three patterns. Over all 66 they printed, verbatim:

  ```
  transcripts: 66 | with an assistant hit: 0 | assistant hits: {'stop': 0, 'integ': 0, 'tests': 0} | template blocks: 58
  transcripts: 66 | with an assistant hit: 1 | assistant hits: {'stop': 0, 'integ': 0, 'tests': 0, 'p2gate': 1, 'term': 0} | template blocks: 58
  ```

  The two deleted transcripts held no template block: the 58 blocks are the same before and after.
- **`gate-sentences-relocated.out`**, added later the same day, covers the five transcripts
  `transcript-presence.out` lists as RELOCATED. The tsv above left them out, because it keeps a record's
  transcript only at its recorded path (see *The deletion*).
  - 3 of the 5 load the template, all on a harden with the Termination gate.
  - 1 states the Phase 2 gate's sentence: #527's transcript `ebc78818…`.
  - With the 58 above, 61 transcripts load harden. 0 of the 61 state the Transition gate's three
    sentences or the Termination gate's, and 2 state the Phase 2 gate's.

## `reader_check.py`: the reader sees assistant text

This is the known-bad case for the zero above. A reader that saw no assistant text would also report
no gate sentence.

- **`reader-check-after-deletion.out`** (64 of 66 transcripts): 2965 assistant text blocks, 179 of
  them naming "Phase 1", in 55 transcripts. Its examples show runs announcing convergence in their own
  words.
- **Before the deletion** (66 transcripts): 3193 blocks, 184 naming "Phase 1", in 56 transcripts.

## `count_widening.py`: could `skill-lint.py`'s count check have caught harden's "BOTH"?

This file ran on the skills as they stood before harden 0.44.1, which removes the one true hit. Its
output is **`count-widening-before-harden-0.44.1.out`**.

- **wide** (the number words one to ten or "both", no noun list, `>` lines counted as items): 15 hits
  on 13 lines, 1 true.
- **both** (any case): 3 hits, 1 true.
- **BOTH** (capitals only): 1 hit, 1 true. This variant is fitted to the one known instance.

## `notes_checks.py`: four checks on the 2026-10-18 notes, beyond their own `score.py`

Its output is **`notes-checks.out`**.

1. **S1 in the reviewer-brief-556 findings: none.** The pattern hits `PROTOCOL.md` 4 times as its
   control.
2. **Queue96 runs with a transaction/rollback `could_not_verify` item: 6 of 12.** By arm that is C 1,
   T 2, B 0, V 3. The pattern hits `PROTOCOL.md` 21 times as its control.
3. **Filed dispositions.** 6551 L04: 6 of 6 `question`. Queue96 K1: 7 `blocking` and 1 `suggestion`.
4. **556 risk items by arm and adjudicated class.**
   - C: 7 items, 6 NOISE.
   - U: 18 items, 13 NOISE, 4 REAL, and 1 whose class the adjudicator left empty (`None`), the item
     that matched S3.

## The deletion

At about 12:40 on 2026-10-23, during this pass, two transcripts named by public records disappeared:
`b2cc1733…` (the 2026-09-16 PR440 record) and `56821ae1…` (the 2026-09-21 PR465 record).

- Claude Code 2.1.309's settings schema reads: `cleanupPeriodDays`, "Number of days to retain chat
  transcripts before automatic cleanup (default: 30)". `~/.claude/settings.json` does not set it. The
  two deletions fit that schedule.
- `transcript_presence.py` counts this over the `transcript:` lines of all public records (output in
  `transcript-presence.out`). It does not read paths that appear only in record bodies.
  - 77 of the 141 distinct paths point at no file.
  - Searched by session id across every folder under `~/.claude/projects`, 72 of those 77 are absent
    everywhere. All 72 belong to records dated 2026-08-26 to 2026-09-21, more than 30 days old, which
    fits the cleanup.
  - The other 5 still exist in another folder.

**Corrected later on 2026-10-23.** This section first said that five of the 77 belonged to records under
30 days old, that four of them had lost their whole project directory, and that the cause was not
established. All five transcripts exist:

- **Four were relocated by Claude Code itself** (`relocations.py`, output in `relocations.out`).
  - On `EnterWorktree`, Claude Code moves a session's transcript into the worktree's project folder.
    Each of the four runs looked its transcript up by session id from inside the worktree, and each
    lookup printed the worktree-folder path its record names.
  - When the worktree session ends, Claude Code moves the transcript back to the folder of the directory
    the session started in. Each of the four transcripts ends with a `worktree-state` record showing the
    worktree session exited, then a `relocated` record pointing at the main `chartsearchai` checkout.
  - So each record was right when written and went stale afterwards.
- **The fifth is the 515 driver capture**, which wrote an unfilled `<cwd-slug>` placeholder. Its
  transcript, never relocated, sits under the pipeline worktree's folder.

## `relocations.py`: where a transcript lived when its record was written, and where it lives now

For each transcript given, the script prints:
- its current folder;
- the targets of its `relocated` records;
- its last `worktree-state`;
- its `EnterWorktree` and `ExitWorktree` calls;
- every lookup the run made of its own transcript, with the path that lookup printed.

The output is **`relocations.out`**, over the five RELOCATED transcripts.
