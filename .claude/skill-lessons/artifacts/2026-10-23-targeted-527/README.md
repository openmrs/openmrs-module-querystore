# 2026-10-23 targeted retro: the #527 example

These are the instruments behind the targeted `skill-retro` pass on the example "#527 recorded the folder
of the worktree it had moved into". Each script reads only and runs with `python3 -I`, except
`calibrate.sh`, which copies two transcripts into a temporary directory and removes it afterwards. The
`.out` files are each run's stdout and stderr, and the command they came from heads each one.

## `relocation_mechanism.py`: does each transcript sit where its own records say Claude Code put it?

The pass's refutation agent wrote this script, and the pass re-ran it. For every top-level transcript
under `~/.claude/projects`, it predicts the transcript's folder:
- the slug of its last `relocated` record's cwd;
- or, where it has none, the slug of its first cwd.

It then compares that prediction with the folder the transcript is actually in.

- **`relocation-mechanism.out`** (re-run at 14:46 +0300, after the move below):
  - 767 of 767 transcripts sit in the predicted folder.
  - 8 sessions have `relocated` targets outside their first cwd.
  - 10 sessions call `EnterWorktree`.
  - 2 sessions' last `worktree-state` still names a worktree.
- **A move during this pass.** At 2026-10-23T11:24:58Z, from L1652's `cost-state`, the TRUNK-6826
  session (`74a2fd99…`) exited its worktree session, and its transcript left `core-6826`'s folder for
  `openmrs-core`'s (L1650-L1653).
  - An earlier run of the same script, from before the move, is
    **`relocation-mechanism-before-move.out`**. It still shows TRUNK-6826 naming its worktree, and
    counts 765 of 765, with 3 sessions still naming a worktree.
- **`calibrate.sh` / `calibrate.out`** are the known-bad and known-good cases:
  - a copy placed in a folder named for no cwd it ever had FAILS;
  - a copy placed where its last `relocated` record points holds.

## `record_write.py`: where was the transcript when the run wrote its header?

For each tool call that writes a `transcript:` header, the script prints the session's cwd and the path
the header names. It also prints the folder that held the transcript at that moment: the last
`relocated` record before the call.

**`record-write.out`:**
- **#592 (`9356b230…`).** At L2582, from inside `chartsearchai-592`, the run's header names the main
  checkout's folder while the transcript was held for the worktree. It was wrong when written, and reads
  right today only because the worktree session later exited.
- **#527 (`ebc78818…`), the control.** At L3125 its header names the worktree folder, which then held
  the transcript. It was right when written.
