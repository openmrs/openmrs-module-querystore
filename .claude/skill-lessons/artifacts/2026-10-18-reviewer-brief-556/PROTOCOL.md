# Does the pipeline's own reviewer find S1 on PR 556, and is it the "judge against the ticket" sentence?

Pre-registered 2026-10-18, before any arm ran. Follows `2026-10-18-pr-review-union-routing-556.md`:
S1 (the fix skips condition-mediated findings, which the PR description declares as residue) was
found by the pipeline's round-1 reviewer on 2026-09-29 and by 0 of 9 plain `pr-review` runs since.

## Subject

Unchanged from that test: PR 556 at `d77204bae7091579a7131f486b613d03e365609d`, base `e089267e`, the
same snapshots (PR description as first posted, issue #555 with no comments), PR conversation withheld,
per-run clones holding only that head's ancestry, JDK 11 offline with private Maven repositories.

## Arms (n=3 each, all six runs in one wave, so contention is identical)

- **R, the pipeline's reviewer.** The orchestrator's original round-1 brief (agent #13 in the #555
  transcript), changed only where the replay requires: snapshot files instead of `gh issue view`, the
  clone's path and branch, `git diff main...HEAD`, the JDK/private-repository note, and the no-server and
  forbidden-read rules every replay carries. It loads copies of the current `pr-harden/reviewer.md`
  (with `SKILL.md` and `evidence.md` beside it) and plain `pr-review` 0.19.0. None of those files
  mentions #555 or its defects (checked).
- **P, plain pr-review plus one sentence.** The brief every earlier 556 run had, with `pr-review` 0.19.0
  plus, after "a PR can pass every line-level check while failing it.":
  > Judge it against the ticket's own words and its comments, never against the PR description, which
  > was written by the same person who wrote the code.

  (`reviewer.md`'s sentence, with "agent" made "person".)

The 9 earlier plain runs on this head (0 of 9 on S1) are the reference for both.

## Scoring

One blind adjudicator, the same S1-S3 key and match rules as before, over the six runs' findings
normalised to one schema (disposition blocking / non-blocking), run and arm stripped, shuffled.

## Pre-committed reading (a signal is at least 2 of 3)

- R finds S1 in 2 or 3 runs: the pipeline reviewer's catch was not luck.
- P finds S1 in 2 or 3 runs: the sentence carries it, against 0 of 9 without it. It becomes the port
  candidate, to be confirmed on a held-out PR before skill-retro.
- R 2-3 and P 0-1: something else in the pipeline brief carries it (its "does not resolve the ticket is
  the first one to look for", the round framing, the JSON's blocking rule); report which, as a lead.
- R 0-1: the original catch was luck; stop adding text.

## Threats

n=3; one PR; `reviewer.md` is today's copy, not the 2026-09-29 one; R's original brief told it to read
the live ticket and conversation (the conversation was empty at that minute; the ticket is identical).

---

# RESULTS — recorded 2026-10-18, before interpretation

**Isolation, amended during the run.** A gap found before launch and fixed for this test: earlier
briefs forbade `~/Projects`, `~/.claude` and their own run directory's siblings, but not the scratchpad's
other folders (which hold earlier adjudicator briefs with answer keys), and the audit's other-run rule
did not cover `out/wt-NN.json`. A scan of all 30 earlier transcripts found no read of any of them (one
`ls ../out | head -3` listing file names in queue wt-05, nothing opened); a planted read is caught. For
this test the briefs forbid the whole scratchpad outside the run folder, the audit flags it, and the
earlier experiments' folders plus this test's key, protocol and briefs were made unreadable (mode 000)
for the duration of the runs and restored after (16 paths, modes recorded). Audit of the six runs: 0
forbidden reads (an over-strict first version of the new rule flagged each run's own `.../replay-556b;`
paths; corrected and recalibrated).

**S1 found by: R 0 of 3, P 0 of 3.** Decided mechanically, so the planned blind adjudication was not
run: no `findings` entry in any of the six outputs mentions condition-mediated findings (the scan sees
finding text: `namesThePartner` appears in all six). Two runs mention S1 outside their findings:
- wt-02 (R), `notes`: "The condition-mediated finding's orders carry no rows and no case measures them.
  The ADR amendment states this; it is residue in the reporting direction."
- wt-05 (P), `what_i_swept`: "with real active orders the ladder prints the order display 'Lactic acid',
  not the KB label, so the issue's symptom does not reach that arm in that arrangement."

**S1 is real at the head, behind a default-off tier.** Round 1's fix commit added
`ConditionMediatedFindingPartnerCompletionContextTest`; with the head's production code and that
commit's test files, 2 of its 3 cases fail with the issue's own symptom ("Also covered by those findings
and not named above: active order Lactic acid (lactate)" after an answer naming "her Lactic acid"). The
finding needs `chartsearchai.drugSafety.derivedFindings`, whose `config.xml` default is `off`.

**Against the pre-committed reading:** R 0-1, so the pipeline reviewer's 2026-09-29 catch does not
reproduce (0 of 3 with today's `reviewer.md`); P 0 of 3, so the sentence does not carry it either.
