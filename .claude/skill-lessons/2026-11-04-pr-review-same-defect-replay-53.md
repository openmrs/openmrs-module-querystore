# measurement · pr-review with and without a "same defect, where the PR didn't look" bullet · openmrs-module-stockmanagement PR 53 at d4a9bba · 2026-11-04
outcome: K1 (a valid uuid with no `name` reaches `switch (delegate.getName())` and returns 500) filed by E 2 of 3, C 0 of 3. Two C runs and the third E run saw it and left it in their notes as out of scope.
protocol: pre-registered before any arm ran (owner-directed skill-retro pass). The candidates were read blind and the verdicts recorded before the arm map was opened.
artifacts: `.claude/skill-lessons/artifacts/2026-11-04-same-defect-replay-53/`: protocol with results, arm key, briefs, outputs, scripts, blind candidates and verdicts, skill diff, `transcripts.txt`
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-openmrs-module-chartsearchai/5bb1becb-751a-4a76-b06d-08682713e488.jsonl

This is capture, not derivation. Follows `2026-11-04-pr-review-stockmanagement-53-missed-input.md`.

## Design

- Subject: PR 53 at `d4a9bba`, frozen at 2026-11-04 12:00 UTC. The maintainer's comment is 12:36:17Z, and the fix `a9c2a39` merged at 20:33Z. PR conversation withheld. Per-run clones, private Maven repositories with `~/.m2` as a read-only tail, JDK 21 offline.
- **C** (n=3): `pr-review` 0.24.0 exactly as live. **E** (n=3): the same plus one bullet after "Unchanged neighbors" (`skill-diff-C-to-E.diff`). All six in one wave.
- Headless `claude -p --model claude-opus-5-5` at default effort. Isolation:
  - project memory empty (canary);
  - `gh` unauthenticated, no MCP servers, and the web, subagent, skill and publishing tools disallowed;
  - nine answer-bearing paths at mode 000 for the wave.

## Measured

- **K1 filed: E 2 of 3** (wt-01 and wt-04, both `nit`), **C 0 of 3**. None filed blocking.
- **K1 seen and not filed: 3 runs.**
  - wt-02 (C): "pre-existing, out of scope, no wrong-operation effect".
  - wt-06 (C): "pre-existing and outside this ticket, so I did not file it".
  - wt-03 (E): "No operation is mutated, so the PR's guarantee holds and I did not raise it as a finding". wt-02 gave the same reason with no bullet.
- So 5 of 6 runs SAW K1, and what separates the arms is whether it was filed. That is not the mechanism the missed-input record gives for its own run, which never raised the question at all.
- All six verdicts were "ready to merge". Cost $7.74 in all, 4.3-10.2 min per run.
- Audit: 0 forbidden reads in the six transcripts. It was calibrated first on 7 planted forbidden reads (all flagged) and 7 allowed ones (none flagged).

## Limits

- n=3 per arm, one PR, one defect, one wording.
- Default effort. The run that missed K1 was at max effort, with the PR conversation and a second-reviewer subagent.
- The answer key was written by the session that missed K1.
- If the two hits were spread at random over the six runs, both would land in E 3 times in 15 (0.2). The
  pre-registered bar was met, but the signal is thin (the gate's point 2).

**Corrected after the gate, 2026-11-05.** wt-03's quote had dropped "as a finding". A sentence saying wt-03
read the bullet's blocking sentence as a reason not to raise K1 was deleted: the gate showed its reason is
the one C run wt-02 gave.
