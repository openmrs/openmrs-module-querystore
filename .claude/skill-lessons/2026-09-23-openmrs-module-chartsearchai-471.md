# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #471 / PR #474 · 2026-09-23
outcome: converged
rounds: 4   cycles: 4 (harden: Phase 1 converged, Phase 2 escalated three times, all on prose)   verifier: ran (works at runtime, rounds 1-3; 3 runs, loaded-class hash matched each time)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-chartsearchai/0b1a474f-cd01-47ee-b8b5-c58b11ffdbc3.jsonl

## Refuted by measurement
- The plan's claim that "the prompt keys on the clause, so no prompt change" covered the whole ticket -> #471's "stated with its rating" failed on the current-medication caution branch (r1-2). The first remedy, adding "carry the finding's severity", then produced the proposal's permission lead ("Enalapril can be given") on a drug the patient already takes. Measured live on Kenneth b65f951f via a calibrated chartsearchai.llm.systemPrompt override. A third wording met all criteria at n=4 · cost: 2 rounds
- ADR text "these Moderate rows are no longer answered by the module at all" -> the branch's own ciprofloxacin test composes a Moderate line under a Major "No". The first rewording was then false for the screen shape too, so the claim was deleted · cost: 2 harden Phase-2 escalations
- "Default 20-cell capture_probe_safety matrix exercises the change" -> it holds no Moderate cell; a PROBE_PATIENTS=kamwara arm was needed to see any flip

## Raised by a fresh agent, missed by the author
- [gate p1] #470 landed mid-run with ratedAReasonToWithhold, a second `>= "moderate"` boundary. Missed: it would have module-composed caution-only answers · blocking at gate, cost 0 rounds
- [gate p2] aSecondDrugInsideTheFirstsCombinationNameStillAsksTheModel would stay green for the wrong reason after the boundary move · blocking at gate
- [harden p2] README/statableRating over-report residue still said minor-only · substantive
- [harden p2] the antiplatelet examples cited as "Moderate withheld" are folded pairs that still withhold · substantive
- [r1] folded Moderate still withholds (implemented, then reverted in r2 as out of scope per Decision 86) · blocking · cost: 2 rounds of oscillation
- [r1] the current-medication caution branch asks for no rating · blocking
- [r3] the severity clause caused the permission lead (live-measured by the reviewer) · blocking · cost: 1 round

## Where a skill blocked or contradicted this run
- resolve-ticket Step 1: gate-state keys on cwd. The run began in the operator's shared checkout, then moved to a worktree when another session (#472) switched that checkout's branch with my uncommitted work in it. The pr entry armed at Step 1 stayed on the shared checkout and carried a finished run's (PR #465) declined ledger; clearing it destroyed that ledger. It was stale (PR merged 09-21), so no live loss, but nothing warned.
- Parallel sessions: another session ran `git checkout -b` in the shared tree carrying my uncommitted changes, and its edits interleaved with mine in two files. It was resolved by hunk-selecting into a separate worktree and a cross-session message. No skill says "start in a dedicated worktree when $CLAUDE_PIPELINE_SLOT is unset and other sessions are active in this repo".
- pr-harden round 1/2: a reviewer's scope-widening finding (r1-1) was implemented by the fixer, and the next reviewer then correctly called it out of scope. The ledger's "settled" mechanism only engages after a decline, so an implemented-then-reverted finding cost two rounds.

## Declined
- r1-1 (fold leg removal) — if we ship without it, a Moderate pair that also shares a level-4 subgroup (efavirenz+nevirapine, zidovudine+stavudine) still leads "No". Recorded in Decision 109 and follow-up #479; Decision 86 kept the fold and #471 left joins alone.

## Assumptions review overturned
- "folded Moderate keeps withholding (out of scope)" -> overturned in r1, restored in r2 on Decision 86's own text
