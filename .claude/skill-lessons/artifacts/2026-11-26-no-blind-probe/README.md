# Does a fresh reader apply pr-review's `--no-blind`?

Six blind agents (`~/.claude/bin/blind-run`, 2026-11-26) each read a copy of pr-review's SKILL.md and
`blind-agents.md` and nothing else, and answered scenarios from the text alone. The first three answered six:
plain `123`, `123 --no-blind`, `123 --post --no-blind`, `123` with "skip the blind agents this time", a reviewer
pr-harden started, and the round after `--no-blind`. Those briefs are identical but for the folder they read
(`probe-current-brief.md`, `probe-proposed-brief.md`); the third probe used the second's brief, after that folder's
text gained one sentence.

- `probe-current-report.md`: pr-review 0.41.0 as pushed, the control. It finds `--no-blind` and the plain request
  undecidable, read literally still four agents, and what a skipped agent goes down as undecided.
- `probe-proposed-report.md`: the first draft of 0.42.0. It starts none on `--no-blind` and on the plain request,
  records it and owes none, and restarts the agents the next round; it finds open whether the plain request also stops
  Step 3's refuter of a recommended fix.
- `probe-proposed2-report.md`: the second draft, with "Step 3's refuter of a fix you recommend still runs." It decides
  all six scenarios but what a pr-harden reviewer's round record says about the agents it does not start, which 0.41.0
  leaves silent too.
- `probe-gh-report.md`: that text, on two scenarios the first refuter of the change raised (`probe-gh-brief.md`): a
  round with `--no-blind` after one whose whole-PR agent is owed, and a refutation pass of a finding under
  `--no-blind`. It found both undecided: whether "owe none" cancels an earlier round's owed agent, and whether the flag
  stops Step 3's blind reader of a finding.
- `probe-final-report.md`: the third draft, on all eight scenarios (`probe-final-brief.md`), after the arguments line
  gained "for it, while an agent an earlier round owes stays owed" and "Step 3's blind readers, of a finding or of a
  fix you recommend, still run". It decides every scenario but the pr-harden record above, and notes a tension in
  G: `SKILL.md:22` still lets an owed item be run.
- `probe-final2-report.md`: the text as committed, with "is not run and" before "stays owed", on the same brief. It
  decides every scenario but the pr-harden record, G with no tension.

The arguments line's sentence on the flag, as each probe read it (`probe-proposed/` changed under the same brief):
1. `probe-proposed-report.md`: "With `--no-blind`, or when the user asks for a review without blind agents, Step 2
   starts none this round; say so in the round record, and owe none."
2. `probe-proposed2-report.md`, `probe-gh-report.md`: the same, then "Step 3's refuter of a fix you recommend still
   runs."
3. `probe-final-report.md`: "... and owe none for it, while an agent an earlier round owes stays owed. Step 3's blind
   readers, of a finding or of a fix you recommend, still run."
4. `probe-final2-report.md`: the third, with "is not run and stays owed", as committed.
The rest of the change (the argument hint, Step 2's exemption and `blind-agents.md`'s opening sentence) was the same
in every draft.
