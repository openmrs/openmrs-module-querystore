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
