# measurement · pr-harden's reviewer brief vs one ported sentence · openmrs-module-chartsearchai PR 556 at round-1 head · 2026-10-18
outcome: measurement only — neither the pipeline's own reviewer brief (0 of 3) nor the ported sentence (0 of 3) found S1; the 2026-09-29 catch does not reproduce; no proposal
protocol: pre-registered before any arm ran; the planned blind adjudication was not run because the primary outcome is decided mechanically (recorded with the results before interpretation)
artifacts: `.claude/skill-lessons/artifacts/2026-10-18-reviewer-brief-556/` — protocol with results, arm key, the orchestrator's original round-1 brief, all six briefs, outputs, scripts, the S1 demonstration, `transcripts.txt`
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-queue/5743c726-b776-450f-a755-a07953eaf17e.jsonl

This is capture, not derivation. Fourth measurement this session; follows
`2026-10-18-pr-review-union-routing-556.md`, which recorded that the pipeline's round-1 reviewer raised
S1 (the fix skips condition-mediated findings) where 9 plain `pr-review` runs did not, and pointed at the
reviewer brief's "never against the PR description" sentence.

## Design

- Same subject and isolation as that test (PR 556 at `d77204ba`, frozen description and issue, per-run
  clones, JDK 11 offline, private Maven repositories, headless runs from clone directories).
- **R** (n=3): the orchestrator's original round-1 reviewer brief (agent #13 in the #555 transcript),
  changed only where a replay must (snapshots for `gh issue view`, clone path, JDK note, no-server and
  forbidden-read rules), loading today's `pr-harden/reviewer.md` and `pr-review` 0.19.0. **P** (n=3):
  plain `pr-review` 0.19.0 plus `reviewer.md`'s sentence ("Judge it against the ticket's own words and
  its comments, never against the PR description, which was written by the same person who wrote the
  code"), with the brief every earlier 556 run had. All six in one wave.
- Isolation tightened after a pre-launch finding (below): the whole scratchpad outside the run folder
  forbidden, audited, and earlier experiments' folders plus this test's key and briefs made unreadable
  for the duration of the runs.

## Measured

- **S1: R 0 of 3, P 0 of 3.** No finding in any output mentions condition-mediated findings. Two runs
  saw it and set it aside outside their findings: wt-02 (R) in its notes, "it is residue in the
  reporting direction", citing the ADR amendment; wt-05 (P) in its sweep, "the issue's symptom does not
  reach that arm in that arrangement".
- **S1 is real, behind a default-off tier.** With the head's production code and round 1's fix-commit
  test files, 2 of 3 cases of `ConditionMediatedFindingPartnerCompletionContextTest` fail with the
  issue's own symptom ("not named above: active order Lactic acid (lactate)" after an answer naming "her
  Lactic acid"). The finding requires `chartsearchai.drugSafety.derivedFindings`, `off` by default in
  `config.xml`. So the runs that set it aside were not simply wrong; none of them said why in a finding.
- What R found instead: in all three runs, a blocking test gap (the new crediting check unpinned in
  the over-crediting direction, or partner rows not pinned as the partner's), and in two of them the
  ticket's dolutegravir case left without a test. P: the cross-finding row union, combination-order
  crediting, the same test gap.
- Cost: R $5.47, P $8.07, $13.54 in all; 15.7-34.6 min per run.

## Isolation finding, applying to all four measurements

Before launch: the earlier briefs forbade `~/Projects`, `~/.claude` and their run folder's siblings but
not the scratchpad's other folders, which held earlier adjudicator briefs carrying answer keys, and the
audit's other-run rule missed `out/wt-NN.json`. A scan of all 30 earlier transcripts found no read of
either (one `ls ../out | head -3` in queue wt-05 listed file names only); a planted read is caught by the
same scan. The earlier records' results stand.

## Refuted — claims this run's own orchestrator made and the run broke

- **"pr-harden's reviewer brief already caught S1 at round 1"** as evidence of what the brief does: the
  catch does not reproduce, 0 of 3 with today's `reviewer.md`. The contrast the 556 union record drew
  between that reviewer and plain `pr-review` rests on one run.
- **"Port that sentence into pr-review"**: it moved nothing here, 0 of 3.

## Limits

- n=3 per arm, one PR, one defect. `reviewer.md` is today's copy, not the 2026-09-29 one.
- No blind adjudication: the outcome needed none, so the six runs' other findings are recorded but not
  classified.
