> **This is the draft as it went to Step 5, not what shipped.** Two refutation rounds revised it —
> among them E4's decline-all and cap paths, E2's `notes`, the gate's `init|fixing` message, and the
> census (#480, #503 and #504 were missed or not yet filed). What survived, and why, is `REJECTED.md`'s
> third 2026-09-23 entry.

# Owner-directed — the review loop files no follow-up issue · 2026-09-23

Owner's instruction, 2026-09-23 (session 11df53f2, cwd `openmrs/querystore`), verbatim:
*"i think i am getting tired of the skill retro skill creating new issue after issue when working on
any issue. Can't it resolve it all in the same pull request without creating a chain of issues? I work
on a created issue, and then it also creates another one, and the cycle continues"*

Line numbers are the LIVE copies as of 2026-09-23 ~19:00Z: `~/.claude/skills/pr-harden/SKILL.md`
0.28.0, `resolve-ticket/SKILL.md` 0.18.1, `ticket-pool/SKILL.md` 0.24.1,
`pr-harden/pr-harden-gate.sh` (byte-identical to `~/.claude/hooks/pr-harden-gate.sh`), and
`~/.claude/skill-lessons/REJECTED.md`.

## Evidence

### The chain (measured 2026-09-23, `openmrs/openmrs-module-chartsearchai`)

`gh api repos/openmrs/openmrs-module-chartsearchai/issues/<n>` (first line of body, created_at) and
`…/pulls/<n>` (merged):

| follow-up | filed (UTC) | body names it as from | worked as | that PR filed |
|---|---|---|---|---|
| #479 | 11:23 | PR #474 (issue #471) | PR486, merged | #488 |
| #482 | 12:29 | #478 (#472) | PR487, merged | #489 |
| #485 | 14:55 | #484 (part of #480) | PR493, merged | #496 |
| #488 | 15:28 | PR #486 (issue #479) | PR490, merged | #491 |
| #489 | 16:00 | #487 (issue 482 item 1) | PR492, merged | #494 |
| #491 | 17:29 | PR #490 (issue #488) | PR495, merged | — |
| #494 | 17:47 | #492 (issue #489) | PR497, merged | #498 |
| #496 | 18:07 | #493 (issue #485) | PR499, merged | #500 |
| #498 | 18:32 | PR #497 (issue #494) | a `resolve-ticket` session on it is running now | |
| #500 | 18:41 | PR #499 (issue 496) | open | |

Ten follow-up issues filed in one day; eight worked to a merged PR the same day; seven of those eight
PRs filed a follow-up of their own. Three chains: #471 → #479 → #488 → #491; #472 → #482 → #489 →
#494 → #498; #480 → #485 → #496 → #500. Every link of the second is about the same ended-order phrase
reading (titles: #482 "residues of the ended-order referent", #489 "the ended-order phrase ahead of
its drug…", #494 "two unpinned mutations in the ended-order phrase reading", #498 "two unpinned tie
mutations in the ended-order phrase reading").

### Where the filing comes from

- `pr-harden`:660-662 (FINISH, 0.27.0 `8a9fc93`): *"That round's non-blocking findings go to a
  follow-up issue rather than into this branch — file it yourself before you report, rather than
  offering to, and name it by number in the report."*
- `pr-harden`:110-113 (round 4+): *"the rest goes to the follow-up issue, unfixed in this branch"*;
  :123 (the r6-6 counter-case, phrased as "would have gone to the follow-up issue unfixed");
  :740-744 (confirming round): *"send anything else it notices to the follow-up issue with the rest"*.
- `pr-harden-gate.sh`:367-368, the unreviewed-head block message: *"BLOCKING-ONLY so it terminates --
  any non-blocking finding it raises goes to a follow-up issue rather than into this branch"*.
- `resolve-ticket`:663: *"An adjacent defect you noticed goes in the report or a new ticket, not into
  this PR."*
- `ticket-pool`:325-327 discloses the follow-up issue as something an invocation does in your name.
- **Riders.** Once FINISH files an issue, runs put other deferred material in it:
  `2026-09-23-openmrs-module-chartsearchai-494.md`:11-13 — harden Phase 2's two deferred mutations went
  into #498 after round 1 raised *"deferred mutations had no tracking issue"* as a non-blocking finding,
  "resolved by FINISH filing #498"; `…-496.md`:22 — a fixer DECLINE was "Filed as #500";
  `…-485.md`:27 — a harden P2 item "outside the ticket; filed in #496".

### What the rule cost before the chain (REJECTED.md :3684-3686, "4 records")

#294:162-166 (a 3-line robustness gap the verifier found, sent to a follow-up), #409:28 ("a real prose
defect now owed to a follow-up"), #412:26 ("the finding's own failure mode is the ticket's defect
restored for the other population, which reads as more than a nit"), #276:28 ("the branch ships with a
known named residue").

### Precedent and counter-case

- `proposals/2026-09-23-retro-finish-rule.md`, table row two: PR414, PR423 and PR478 implemented a
  clearing round's non-blocking findings and ran one blocking-only round; each confirming round returned
  zero blocking. REJECTED.md :3626-3629: PR414 and PR423 had the ORCHESTRATOR write those fixes; only
  PR478 spawned a fresh fixer.
- `pr-harden`:117-121: on PR #465, three blockers (r7-1, r7-3, r8-1) were introduced by an edit that
  implemented a NON-blocking finding; REJECTED.md's first 2026-09-23 window names r6-6 → r7-3.

### The parked design this reopens

REJECTED.md :3616-3633, first 2026-09-23 window, **PARKED · P2 resolution B — let FINISH take a
non-blocking finding for one blocking-only round**, with: *"REOPEN ON: a proposal that reopens :2532-2534 on its
records (#294:162-166, #409:28, #412:26, #276:28), limits the permission to a full round's findings
(rounds 1-3), taken once per run by a fresh fixer, names the confirming round by its heading, states
the cost as at least one round with PR465's r6-6 → r7-3 as the counter-case, and survives its own
Step 5."*

---

## P1 — a clean FULL round's non-blocking findings are fixed in this PR, under one blocking-only round; nothing is filed

Bar: owner-directed; also (a) — the chain above, and the 4 records of REJECTED.md :3684-3686. It is
resolution B re-scoped to that REOPEN ON, point by point:
- full round's findings only — the exception fires only on a round 1-3 (full) review;
- once per run — every round after it is BLOCKING-ONLY, so it cannot fire twice;
- by a fresh fixer — it runs step 4, not FINISH, and not the orchestrator;
- the confirming round named by its heading — *That confirming round is BLOCKING-ONLY*;
- cost stated as at least one round, with r6-6 → r7-3 — in E4's text;
- :2532-2534's records — above; Step 5 — this pass.

What it changes beyond resolution B: resolution B left the follow-up issue in place for the rest;
this deletes it. A blocking-only round's reviewer reports blockers alone and nothing else it notices
is implemented or filed. That makes the r6-6 counter-case sharper — a real hole a round-4+ reviewer
grades non-blocking is now unreported rather than routed to an issue — and E3 says so.

### E1 — `pr-harden`:94-106, the round diagram

Before:
```
1  REVIEW    fresh subagent · pushed head · declined ledger · last verifier report
             from round 4 on: BLOCKING-ONLY
2  RECORD    the reviewer's blocking count → state          {phase: "reviewed"}
3  exit?     blocking == 0 → step 7
…
7  FINISH    do NOT edit the cleared sha · re-derive the body · VERIFY the merging
             head if nothing has · mark ready. An edit here owes a BLOCKING-ONLY round
```
After:
```
1  REVIEW    fresh subagent · pushed head · declined ledger · last verifier report
             BLOCKING-ONLY from round 4, and after a clean full round
2  RECORD    the reviewer's blocking count → state          {phase: "reviewed"}
3  exit?     blocking == 0 → step 7 · a clean FULL round's non-blocking
             findings go to step 4 first, and every round after is BLOCKING-ONLY
…
7  FINISH    do NOT edit the cleared sha · file NO issue · re-derive the body ·
             VERIFY the merging head if nothing has · mark ready.
             An edit here owes a BLOCKING-ONLY round
```

### E2 — `pr-harden`:110-113, step 1's opening

Before:
> **From round 4 on, the round is BLOCKING-ONLY.** Rounds 1 to 3 are full rounds: the reviewer
> reports everything and the fixer implements the non-blocking findings too, which is how polish
> happens. From round 4 the reviewer reports blockers alone and the rest goes to the follow-up
> issue, unfixed in this branch.

After:
> **From round 4 on, the round is BLOCKING-ONLY** — and so is every round after a full round that
> found nothing blocking (step 3). Rounds 1 to 3 are full rounds: the reviewer reports everything and
> the fixer implements the non-blocking findings too, which is how polish happens. A blocking-only
> reviewer reports blockers alone, and nothing else it notices is implemented or filed.

### E3 — `pr-harden`:122-123, the r6-6 counter-case

Before: *"…whose fixer verified the hole was real, would have gone to the follow-up issue unfixed."*
After: *"…whose fixer verified the hole was real, would have gone unreported, and so unfixed."*

### E4 — `pr-harden`:285-288, step 3

Before:
> `blocking == 0` ends the loop. Non-blocking findings do not extend it — that is the whole point of
> separating the fixer's scope from the exit condition. Go to step 7.

After:
> `blocking == 0` ends the loop, with one exception, taken at most once per run: **a FULL round that
> finds nothing blocking but raises non-blocking findings.** Those are fixed in this PR, not filed.
> Run step 4 on them with a fresh fixer, then steps 5 and 6 and COMMIT as usual, and review the head
> that produces under *That confirming round is BLOCKING-ONLY* in FINISH — as is every round after
> it, which is what stops the loop implementing nits forever. It costs at least that round, and more
> when a fix brings a blocker of its own: on PR #465 the fix for non-blocking r6-6 introduced blocking
> r7-3. Where the round cap leaves no round for that review, decline those findings on the record
> instead, citing the cap. Otherwise go to step 7.
>
> **Filing those findings as an issue instead is what this replaced, and the issues chained.**
> Measured 2026-09-23 on `openmrs-module-chartsearchai`: ten follow-up issues filed that day, eight
> worked to a merged PR the same day, and seven of those eight PRs filed one of their own — #472 →
> #482 → #489 → #494 → #498 the longest, every link about the same ended-order phrase reading. The
> owner's instruction, the same day: resolve it in the same pull request, without a chain of issues.

### E5 — `pr-harden`:296-297, step 4, after "declines the rest on the record."

Add:
> **Filing an issue is neither.** A finding that asks for a follow-up or tracking issue is
> implemented — the thing it would track — or declined; on #494/PR497 round 1 asked for one, and
> filing it began #498.

### E6 — `pr-harden`:660-670, FINISH's opening

Before:
> The reviewer found nothing blocking, so this is the sha you are handing over — and **FINISH does not
> edit it.** That round's non-blocking findings go to a follow-up issue rather than into this branch —
> file it yourself before you report, rather than offering to, and name it by number in the report.
>
> That is the whole change from the version of this step that applied them, and the argument it
> replaces was *"those edits carry no blocking finding by construction, so no further round is owed"*.
> It graded the FINDINGS, …pinned nothing.
> Re-deriving the PR description is still owed and is not an exception, …

After:
> The reviewer found nothing blocking, so this is the sha you are handing over — and **FINISH does not
> edit it.** Nor does it file an issue, or offer to: a clean full round's non-blocking findings were
> fixed and reviewed before this (step 3), and a blocking-only round reports nothing else.
>
> A version of this step applied those findings here, and the argument it rested on was *"those edits
> carry no blocking finding by construction, so no further round is owed"*. It graded the FINDINGS,
> …pinned nothing. That is why step 3, not this step, fixes them: there the fixes get a review.
> Re-deriving the PR description is still owed and is not an exception, …

(The elided text is unchanged.)

### E7 — `pr-harden`:685-686, FINISH's re-derive-the-body paragraph, after "…rather than carrying one forward."

Add:
> Name in it every finding the loop declined, with its reason: with no issue filed, the PR is where a
> decline stays visible.

### E8 — `pr-harden`:742-744, *That confirming round is BLOCKING-ONLY*

Before: *"So brief the confirming reviewer to return blocking findings alone, and send anything else it
notices to the follow-up issue with the rest."*
After: *"So brief the confirming reviewer to return blocking findings alone; nothing else it notices is
implemented or filed."*

### E9 — `pr-harden`:810-811, Termination

Before: *"From round 4 the two coincide — blocking-only means the fixer edits for blockers alone —"*
After: *"In a blocking-only round the two coincide — the fixer edits for blockers alone —"*

### E10 — `pr-harden-gate.sh`, all four copies (live skill, live hook, repo skill, repo hook)

:9 comment — before *"that, from round 4 on where blocking-only makes the two nearly coincide, is whose
number it is."* → after *"that, in a blocking-only round where the two nearly coincide, is whose number
it is."*
:367-368 message — before *"BLOCKING-ONLY so it terminates -- any non-blocking finding it raises goes
to a follow-up issue rather than into this branch -- then record it…"* → after *"BLOCKING-ONLY so it
terminates, reporting blockers alone and filing nothing -- then record it…"*
`gate-test.sh` asserts allow/block only, so it is re-run for that and the message rendered once.

## P2 — the rest of the pipeline files no issue either

Bar: owner-directed; (c) once P1 lands — `resolve-ticket`:663 and `ticket-pool`:326 would license or
disclose an issue the loop no longer files. Also REJECTED.md :3701-3704 (*"An adjacent product defect
noticed and not fixed, with nowhere durable to go"*): this gives it the PR description.

- **E11 — `resolve-ticket`:663-664.** Before: *"An adjacent defect you noticed goes in the report or a
  new ticket, not into this PR."* After: *"An adjacent defect you noticed goes in the report and the PR
  description, not into this PR — and not into a new issue: file none, as `pr-harden` files none."*
- **E12 — `ticket-pool`:326-327.** Before: *"plus whatever its review rounds push on top and, where
  review left findings unfixed, a follow-up **issue** on that repo holding them; per retro,"* After:
  *"plus whatever its review rounds push on top; per retro,"*

## Versions

`pr-harden` 0.28.0 → 0.29.0; `resolve-ticket` 0.18.1 → 0.19.0; `ticket-pool` 0.24.1 → 0.24.2.

## Pruned, and net

Deleted: FINISH's "file it yourself… name it by number" instruction; the round-4 rule's "the rest goes
to the follow-up issue"; the confirming round's "send anything else… to the follow-up issue"; step 3's
"Non-blocking findings do not extend it" (false under P1); the gate's follow-up clause;
`resolve-ticket`'s "or a new ticket"; `ticket-pool`'s follow-up disclosure. Added: step 3's exception
and its measurement (E4), step 4's one rule (E5), the body line (E7). Net about +12 lines in
`pr-harden`, justified as the mechanism that replaces the issue plus the measurement that retires it;
−1 in `ticket-pool`, ±0 in `resolve-ticket`.
