# Design-first probe: pr-review 0.43.0 against 0.43.0 plus the owner-directed text (pre-registered 2026-11-26, before any run)

Owner-directed: "yes close the pr-review gap by first state whether the design is right for the ticket and the plan it
belongs to, and hold back any drafted comment that adds more of what's in question." After stockmanagement #85, where
round 1's drafted comment B ("warn ... the way parseUomPriority does") was answered with one more static helper
(bc98c50), and the ticket's author then called the helpers "worse than what we're replacing" (issuecomment-7185869450).

## Arms (skill copies k1 and k2; which is which is in private/KEY-private.json, locked during the wave)
- C: pr-review 0.43.0 as live.
- T: C plus cand/skill-diff-T.diff (Step 1 reads the ticket's parent and links; Step 2 opens with a design verdict for the
  ticket and its plan; :85 holds back a drafted comment that would add more of what is in question).
n = 5 per arm per case. Both arms get identical inputs, the plan files included.

## Case 85 (target): stockmanagement #85 round 1, head 8f7bb30, base 17bd512, frozen at 2026-11-24 21:31 UTC
Snapshots: the PR description, the ticket with its parent and links, the epic O3-6306 (description and comments before
T), the conversation before T, the four agents' reports verbatim, the round's four drafted comments verbatim (A, B, C, E),
and items R1 to R9 as in the 2026-11-26 filing probe.
Labels (fixed now):
- B: HOLD. Its fix adds one more static helper to the helpers R1 questions; the author did exactly that (bc98c50).
- A, E: GO (neither adds a helper; E removes one). Holding either is over-holding.
- C: REPORTED, not scored (its diff block moves an existing helper call; it adds no helper).
- R1: FILE. R4, R5, R8, R9: NOISE if filed. R2, R3, R6, R7: REPORTED.

## Case 79 (noise): stockmanagement #79 round 1, head 25b913e8, base 17bd512, frozen at 2026-11-24 21:25 UTC
Snapshots: the same kinds. The design was approved by a maintainer before the round (denniskigen, 20:09 UTC).
Drafts A (question: the order of `total` after the Pair swap) and B (nit + suggestion: drop a guard that never changes
the result), both verbatim as the round posted them. Items S1 to S8 are the agents' findings the round set aside.
Labels (fixed now):
- A, B: GO. Holding either is over-holding.
- S2 (a shared formatQuantity helper), S3 (test names, package, 50000), S4 (the "tests before swapping" process claim),
  S6 (import order, lambdas), S7 (stale classes after a build without `clean`), S8 (the locale read per call rather than
  at class load): NOISE if filed. S1 (the quantity test drives no production call site; a maintainer later
  asked to drop that test class) and S5 (the Pair order's effect on the expiry alerts): REPORTED.

## Predictions and reading rules (fixed now)
1. Case 85, C posts B (as drafted or reworded) in at least 3 of 5. If C holds B in 3 or more, 0.43.0 already holds it at
   this decision point and T's hold-back gain is not measured here.
2. Case 85, T holds B in at least 4 of 5, and holds A or E in no more runs than C.
3. Case 85, both arms file R1 in at least 4 of 5, and T is noisy (files any of R4, R5, R8, R9) in no more runs than C.
4. Case 79, T holds A or B in no more runs than C, and in at most 1 of 5; T files a NOISE item in no more runs than C.
5. Both cases, T's report opening states a design verdict that names the plan in at least 4 of 5. Scored mechanically:
   the opening has "design" or "approach", and names the plan (the epic key O3-6306, "v4.0", "epic", "plan", or a linked
   ticket's key: O3-6316 to O3-6323 for 85, O3-6308 for 79). C's rate is reported.
T is a candidate only if 2, 4 and 5 hold, and 3 holds for T. Scored by scripts/score.py before the key is opened; every
report is also read, and a mechanical score a reading contradicts is reported beside it.

## Wave 1 result (scored before unblinding; read; audited 0 flags after a calibrated re-audit)
- 85: C posted B 5/5 (3 reworded, keeping "the way parseUomPriority does"; 2 as drafted); T held B 5/5; A, C, E held 0 in
  both; R1 filed 5/5 in both; noisy: C 0, T 1 (pd-12 filed R9 as a nit).
- 79: C held A or B 0/5; T 1/5 (pd-07 held B, citing a change the ticket doesn't need and a suggestion block no blind
  reader had checked, rules both arms have); noisy 0 in both.
- Openings naming the plan: 0/10 C, 0/10 T, though all 20 runs opened the epic file. T's openings state a verdict on the
  ticket ("the direction needs agreeing first" at 85; "the right fix for O3-6340" at 79), C's state their posting plan.
- So T failed predictions 3 and 4 by one run each, and 5 outright: not a candidate as worded.

## Wave 2 (pre-registered after wave 1, before any wave-2 run)
- Arms: C (0.43.0, again) and T2 = T with Step 2's verdict sentence revised so the plan's verdict is stated and the plan
  named (cand/skill-diff-T2.diff). The hold-back text is unchanged from T. New skill folders k3 and k4, a new key; same
  briefs, snapshots and locks. n = 5 per arm per case.
- Opening scored with the wave-1 plan pattern and a widened verdict pattern ("design", "approach", "right", "fit"); wave
  1 is re-scored with it too.
- W2-1: T2's opening names the plan beside a verdict in at least 4 of 5 in each case.
- W2-2: at 85, T2 holds B in at least 4 of 5, and holds A or E in no more runs than C.
- W2-3: at 85, R1 filed in at least 4 of 5 by both arms and T2 noisy in no more runs than C; at 79, T2 holds A or B, and
  files a NOISE item, in no more runs than C.
- Pooled over both waves (the hold-back text is the same in T and T2), the counts of held A/E at 85, held A/B at 79,
  and NOISE filings are reported for the new text against C.
T2 is a candidate only if W2-1, W2-2 and W2-3 hold.

## Wave 2 result (scored before unblinding; read; audited 0 flags with a calibrated check)
- 85: C posted B 5/5, noisy 2 (R9 twice), plan 0/5, R1 5/5; T2 held B 5/5, A/C/E held 0, noisy 0, plan 5/5, R1 5/5.
- 79: C held 0, noisy 0, plan 0/5; T2 held B 2/5 (pe-10, pe-12), noisy 0, plan 5/5.
- W2-1 and W2-2 hold; W2-3 fails at 79 (B held 2 vs 0). Every new-text hold of 79's B in both waves (pd-07, pe-10,
  pe-12) cites Step 3's blind read of a fix the method asks before posting, which the brief made impossible; the real
  round had that read done (sm79-r1-refute-B). The probe's drafts did not say so.

## Wave 3 (pre-registered after wave 2, before any wave-3 run)
- Input correction for both cases: each drafts.md now says what blind readers the real round ran on each draft and that
  their changes are applied (85: pr85-r1-refute and refute2; 79: sm79-r1-refute-B), per the round records. The wave-1
  and wave-2 drafts files are kept in raw/drafts-v1/.
- Arms, n = 5 per arm per case, three blinded folders k5, k6, k7: C (0.43.0); T2 (as in wave 2); T3 = T2 without the
  sentence naming the #85 case (cand/skill-diff-T3.diff), to see whether the hold-back clause works without it.
- W3-1: T2 and T3 each name the plan beside a verdict in at least 4 of 5 in each case (score2.py).
- W3-2: at 85, each of T2 and T3 holds B in at least 4 of 5, and holds A or E in no more runs than C.
- W3-3: at 85, every arm files R1 in at least 4 of 5, and T2 and T3 are each noisy in no more runs than C; at 79, T2
  and T3 each hold A or B, and file a NOISE item, in no more runs than C.
- Reading rule: an arm that passes W3-1 to W3-3 is a candidate; if both pass, T3 is preferred (shorter, names no case).
  If T3 fails W3-2 while T2 passes it, the example sentence carries the hold at 85, which is reported as such.

## Wave 3, interrupted (recorded before any rerun)
- At 14:26-14:30 UTC the account's five-hour usage window reached 100% (rate_limit_event "rejected", five_hour, overage
  refused) and 14 of the 30 runs ended mid-review with exit 1; their reports are the limit's message, not reviews.
- The 16 completed runs were scored before unblinding (scores3-before-unblinding.txt). The 14 are rerun under new names
  (pg-01..pg-14, same case and folder each, private/KEY3b-private.json) after the window resets at 15:40 UTC, with the
  same briefs, snapshots and locks, plus the completed and cut wave-3 folders locked. W3-1 to W3-3 are read on the 16
  plus the reruns, n = 5 per arm per case, unchanged.

## Wave 3 result (16 completed runs and 14 reruns; scored before unblinding; read; audited 0 flags with a calibrated check)
- Arms: k6 = C, k5 = T2, k7 = T3.
- 85: C held B 0/5 (4 as drafted, 1 reworded), noisy 3/5 (R9), plan 0/5, R1 5/5. T2 held B 5/5, A/C/E 0, noisy 1/5
  (pg-13, R9), plan 5/5, R1 5/5. T3 held B 2/5, A/C/E 0, noisy 0, plan 5/5, R1 5/5.
- 79, with the drafts naming the blind reads the round ran: C held A 1/5 (pg-09), B 0; T2 held nothing (0/5); T3 held
  nothing (0/5); no arm filed a NOISE item; plan C 0/5, T2 5/5, T3 5/5.
- W3-1 holds for T2 and T3. W3-2 holds for T2 and fails for T3 (2 of 5). W3-3 holds for both. So T2 is the candidate,
  and, by the reading rule above, the hold at 85 depends on the sentence naming #85.
