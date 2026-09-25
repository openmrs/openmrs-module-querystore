# Answering

- Line 1 is the answer — the name, the number, the yes/no, the choice. Evidence after it, with file
  paths or command output. No preamble, no hedging, no survey of alternatives. Where line 1 is
  genuinely undecidable it says "undecidable because X".
- "Which X should I use" and "will this fix Y" get the concrete pick on the first line, not a
  comparison of the candidates with the pick buried in it.
- A reply to a forum post, an issue comment or a PR thread matches or undercuts the length of what it
  is answering.

# Verification before claiming

- A grep hit, a plausible hypothesis, or nearby code that looks like it settles the question is a
  LEAD. A finding, a root cause or a doc claim becomes a statement of fact only on executed code,
  test output, or file contents just re-read.
- Re-check every citation against the file before writing or posting it — method name, `file:line`,
  anchor, issue number, ledger entry. A method that does not exist reads exactly like one that does.
- Before defending existing output as correct, name the observation that would prove it wrong and go
  get it. A right answer reached from wrong records is no evidence the records are right.
- **Calibrate an ad-hoc measurement script against a known-good AND a known-bad case before quoting
  its numbers.** The positive control for a zero-hit search is a different gate and does not cover
  this one: what it misses is a script returning plausible NON-zero numbers — the wrong uuid, a
  swallowed HTTP error, a detector matching a diff header. Never `2>/dev/null` in a measurement
  command, and never let a non-zero exit pass unread.
- Prefer driving the real code to re-expressing its predicate in a script at all. A reimplementation
  that is 98% right is wrong in exactly the tail being investigated, and says so in plausible numbers.

# Editing what is already written

- Prose you wrote earlier in the same session is frozen. Revise it only when a failing test, a CI gate
  or a reviewer finding names it, and quote that trigger before editing. Rewriting your own paragraph
  because the second phrasing reads better is not work.
- Anchor an edit on content, never on a line number or a byte offset, so a stale anchor fails loudly
  instead of silently editing the wrong line.
- A destructive discard (`git checkout -- <path>`, `git restore <path>`) is legitimate for undoing a
  mutation probe, and at that moment the file is ALWAYS modified — so "is it modified?" is not the
  question and must not become the rule. The question is whether the path also carries work the probe
  did not put there. `git-restore-backup.sh` copies each modified tracked file aside first and says
  where; read that output, because the loss it exists for is silent and found later.

# Environment

- **Wait on a condition, never on a clock.** A foreground `sleep` is refused by the harness: it costs
  a turn, and takes any real work in the same call down with it. Poll with `Monitor` and an
  until-loop, start long work with `run_in_background`, or do adjacent work and let the completion
  notification wake you. **Except inside a pipeline skill's run** (`resolve-ticket`, `pr-harden`,
  `harden`): its Stop gate refuses a mid-run yield on a background build or a `Monitor`. There, wait
  inside the turn with one foreground until-loop bounded under the tool's timeout, as `pr-harden`'s
  *this session must not busy-wait either* gives it.
- Read an issue with `gh issue view <n> --json title,body,comments`, never `--comments`: off a
  terminal that prints the comments and not the body, so an issue without comments reads as empty and
  one with comments reads as its comments alone (gh 2.87.3, measured 2026-09-23).
