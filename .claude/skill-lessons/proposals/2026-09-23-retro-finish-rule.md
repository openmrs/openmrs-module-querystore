# Targeted retro — `pr-harden`'s FINISH rule and its round-4 extension · 2026-09-23

> **This is the draft as it went to Step 5, not what shipped.** Two refutation rounds revised or
> refuted parts of it — among them P2's resolution B (parked), P3's "cause not established" and its
> Reporting bullet, the PR426 paragraph, "no window counted it" and round 1's own "all unattended".
> What survived, and why, is `REJECTED.md`'s 2026-09-23 window.

Scope, set by the owner: the rule *"FINISH does not edit the cleared sha; the terminating round's
non-blocking findings go to a follow-up issue"* (`pr-harden` 0.20.0, `ea574f9`, 2026-09-07) and its
extension *"from round 4 on, rounds are BLOCKING-ONLY, the rest goes to the follow-up issue"*
(`pr-harden` 0.26.0, `0183209`, 2026-09-21). Both commits record that they skipped this skill's
refutation step. The ledger parks *"A FINISH rule costing something real: 1 record … REOPEN ON: a
second"* at `REJECTED.md`:2532-2534.

Live skill text is `~/.claude/skills/pr-harden/SKILL.md` 0.26.4 and `~/.claude/skills/resolve-ticket/SKILL.md`
0.17.0; line numbers below are theirs. The gate is `~/.claude/skills/pr-harden/pr-harden-gate.sh`
(byte-identical to `~/.claude/hooks/pr-harden-gate.sh`).

## Records read

Since `LAST` (2026-09-20): 7 — `2026-09-21-chartsearchai-PR465`, `2026-09-23-chartsearchai-473`,
`2026-09-23-openmrs-module-chartsearchai-{469,471,472,476}`, `2026-09-23-querystore-81`.
For the rule's whole live period (it landed 2026-09-07 17:33 +0300): every record from 2026-09-07 to
2026-09-19 whose header names `pr-harden` — 30 files. The four dated 2026-09-07 (#337/PR384,
#280/PR383, #305/PR385, #379/PR382) ran under the older FINISH or cannot be placed either side of it;
#337/PR384:27 says "applied at FINISH", the old behaviour. `2026-09-16-…-435` is a standalone `/harden`
run and bears on nothing here.

## Linter

`skill-lint.py` over `~/.claude/skills`: 10 files, 0 findings, exit 0.

## What happened to terminating-round non-blocking findings, run by run (post-0.20.0)

From each record's own words; `#N` is the chartsearchai issue number.

| outcome | runs |
|---|---|
| filed as a follow-up issue | PR393→#394, PR405→#407, PR404→#408, PR411→#412, PR414→#415 (one of its two), PR419→#421, PR424→#425, PR427→#429, PR428→#430, PR431→#432, PR440→#443, PR453→#454, PR452→#455, PR456→#458, PR457→#459, PR461→#462, PR460→#463, PR474→#479, PR478→#482 (the confirming round's notes) |
| implemented at FINISH, then ONE blocking-only round | PR414 (`2026-09-13-…-379`:23, "applied at FINISH, one blocking-only round owed anyway"), PR423 (`2026-09-14-…-421`:23, "non-blocking, implemented · cost: 1 blocking-only round"), PR478 (`2026-09-23-…-472`:3, "2 non-blocking implemented as owed at FINISH; r3 blocking-only: 0") |
| routed "to a follow-up" that was never filed | PR410 (`2026-09-12-…-294`:162-166), PR417 (`2026-09-13-…-409`:28, :53), PR426 (`2026-09-14-…-426`:25, its r5 finding — see below), PR470 (`2026-09-23-…-469`:18, "to follow-up (not filed)") |
| written into the PR body instead of an issue | PR481 (`2026-09-23-…-476`:21, citing the P1 contradiction) |

Every confirming round in the second row returned zero blocking, and each row-two edit is a commit
after the clearing review: PR414 `b711bd81` ("the measurement's head is named where Decision 80
restates it"), PR423 `cb75e9f6` ("close the order-read guard's largest residue"), PR478 `5db3ccc7` and
`33376433`.

**PR426 was first placed in row two and the commits moved it.** Its record says "twelve findings raised
across five rounds, all twelve implemented" (`…-426`:39), and r5 raised one non-blocking finding
(:25). PR426's commits carry a fix commit for each of rounds 1-4 and nothing after round 5 but
`bc0e92cb`, a merge of `origin/main` whose diff touches `CLAUDE.md`, `ChartSearchAiUtils`,
`DosingCeilingFidelityCheck`, `DrugReference`, `DosingCeilingFidelityTest` and one ADR line — `main`'s
#425 work, no guard file. Neither the PR body nor any issue since 2026-09-14 names the r5 finding. So the
record's "all twelve" is eleven, and the r5 finding was routed nowhere. A record correction is owed.

**Census of the filed issues, measured 2026-09-23** against `openmrs/openmrs-module-chartsearchai`:
three `gh api search/issues` queries restricted to `created:>=2026-09-07` — `non-blocking in:body`;
`"review round" OR "review loop" OR "reviewed clean" OR "zero blocking"`; `"blocked" OR "follow-up"
OR "follow-ups" OR "clean-context"` — every hit's body read. Excluded as not review-loop follow-ups:
#391 (proposal), #399 (scope split of #397), #413 (pre-existing defect the verifier found outside a
diff), #441 and #442 (residues of #439's scope), #450 (a ticket), #473 (feature gap), #480 (feature
scope left by #475). Result: **19 follow-ups, 17 open.** The two closed, #412 and #421, and a third,
#425 (open by design — PR428 says `Refs #425`), were each worked as a full `resolve-ticket` run
(records `2026-09-14-…-412`, `-421`, `-425-PR428`); PR427 then filed #429 and PR428 filed #430.
**Calibration of the "never filed" row:** a search for `"#414"` returns #415 (known positive); a
search for `"pull/414"` does NOT, so URL-form searches were not used as negatives. For each of PR410,
PR417, PR470 and PR481, `"#<pr>"` and `"#<ticket>"` searches since the run date return no follow-up;
the ticket's comments carry none; and PR410's, PR417's and PR470's bodies do not contain the routed
finding (PR410's says the opposite at line 75: "a `finally` that restores them. Verified to run clean
as committed"). PR481's body carries one of its two routed findings.

---

## P1 — two skills say nothing is posted to GitHub, and FINISH posts an issue

**Bar (c), a skill contradicting itself** — twice, once per skill.
- `pr-harden`:655-657 sends non-blocking findings "to a follow-up issue", and :742 sends the confirming
  round's notes there; `pr-harden`:1149 reports *"What nothing posted to GitHub means in practice: the
  PR carries N commits and no review comments."*
- `resolve-ticket`:588 reports *"Nothing was posted to GitHub but the commits."* while its Step 9
  (:547, :552) invokes `pr-harden`, which "owns everything from round 1" — FINISH included — and its
  own Step 8 opens the PR.
- Both lines date from `b06d6a8` (2026-08-20). The follow-up issue arrived with `ea574f9`, which
  edited `resolve-ticket` (0.15.1) and left :588.
- It changed behaviour once: `2026-09-23-…-476`:21 — "pr-harden FINISH says non-blocking findings go
  'to a follow-up issue'; resolve-ticket Reporting says nothing is posted but commits. Resolved by
  listing them in the report and PR body, not filing an issue."

**Edit — delete the false clause, keep the offer.**
- `pr-harden`:1149-1150 → `- The PR carries N commits and no review comments. Offer to run
  \`pr-review --post\` or \`--stage\` once, at the end, if the user wants the record public.`
- `resolve-ticket`:588-589 → `- Offer \`pr-review <n> --post\` or \`--stage\` once, at the end, if the
  user wants the review record public — offer it, do not wait for an answer.`
- `pr-harden`:133 ("Nothing is posted to GitHub.") is about the reviewer's `pr-review` run and is true;
  untouched.

**Prunes:** two false clauses. Net −1 line or less.

## P2 — FINISH forbids an edit that its own gate, and two passages of its own step, treat as normal

**Bar (c), a skill contradicting itself and its own gate script.**
- The rule, `pr-harden`:655-657: *"**FINISH does not edit it.** That round's non-blocking findings go to
  a follow-up issue, named in the report, rather than into this branch."*
- `pr-harden`:686-688, same step: *"this step's own non-blocking edits are pushed *after* the last
  verifier run in every case, so they are unverified even when a round did verify."* From `b06d6a8`
  (2026-08-20), when FINISH applied them; `ea574f9` kept it and quotes it as a concession at :661-664.
- `pr-harden`:734-737, same step, lists *"a nit's fix exposes a real defect"* among the ways a blocking
  finding turns up at FINISH. From `b06d6a8`.
- `pr-harden-gate.sh`:364-367, the block this rule's own gate prints when the head is not the reviewed
  sha: *"Something edited the branch after the last review, and FINISH applying that round's
  non-blocking findings is the usual cause. So either hand over the reviewed sha, or run one more round
  on this head: a FRESH reviewer … BLOCKING-ONLY so it terminates"*. Written BY `ea574f9`, the same
  commit as :655-657.
- `pr-harden`:1221-1222, *"If FINISH edited, one blocking-only round is owed"*, reads either way.
- **The practice split along the two readings.** Three runs took the gate's (table, second row: PR414,
  PR423, PR478), each paying one blocking-only round that returned zero blocking. The rest took
  :655-657's. None of the three records calls its choice a deviation.
- **The 2026-09-20 window read the stale clause as the rule.** `REJECTED.md`:3507-3515 parks
  "Wall-clock P1" on *"`pr-harden` §7 pushes its non-blocking edits AFTER the last verifier run in the
  documented normal case (`:630-631`)"* — :686-688 above.

**Edit — resolution B: make the rule say what its gate and its practice say.**
- `pr-harden`:655-657 →
  > The reviewer found nothing blocking, so this is the sha you are handing over — and **an edit to it
  > costs one more round.** A non-blocking finding worth that round may be taken here: implement it,
  > then run the blocking-only round below, as PR414, PR423 and PR478 did, each confirming round returning
  > zero blocking. The rest go to a follow-up issue, named in the report, rather than into
  > this branch.
- `pr-harden`:686-688: *"this step's own non-blocking edits are pushed *after* the last verifier run in
  every case"* → *"an edit this step makes is pushed *after* the last verifier run"*; and :663-664 the
  quotation of it, `("pushed *after* the last verifier run in every case")` → `("pushed *after* the
  last verifier run")`. The *in every case* is false under either resolution.
- `resolve-ticket`:563-565: delete *"`pr-harden`'s FINISH does not edit that sha, and an edit that is
  genuinely owed there costs one blocking-only round;"* — the sentence that follows, the Stop gate
  comparing the head against the last reviewed and verified sha, is the load-bearing half, and the
  paragraph's last sentence already defers to `pr-harden`'s **Termination** ("do not restate it in the
  report, cite it"). Name `pr-harden` in that last sentence, since "That skill" loses its antecedent.
- :734-737, :739-743, the gate script and :1221-1222 are consistent with B and are not edited — so
  the gate and `gate-test.sh` do not change.

**Resolution A, if B is refused:** keep :655-657; delete :686-688's clause and :663-664's quotation of
it; delete *"a nit's fix exposes a real defect, "* from :735; delete the gate's *"and FINISH applying
that round's non-blocking findings is the usual cause"* and re-run `gate-test.sh`.

**Prunes (B):** two *in every case* universals and one restated rule in `resolve-ticket`. Net ≈ +2
lines, justified by closing a contradiction with the gate without a gate edit.

## P3 — a finding routed "to a follow-up" is lost when nothing files it

**Bar (a), four records** (table, third row): PR410, PR417, PR426, PR470. Plus PR481, where the P1
contradiction sent the findings to the PR body. The cause for PR410/PR417/PR426/PR470 is not established —
the records do not say why nothing was filed — so this is a guard, and its value does not depend on
the cause: a finding nobody filed is lost whichever way it happened.

- FINISH already requires the issue to be "named in the report" (:656), and `pr-harden`'s own
  **Reporting** list (:1142-1150), which is the report's checklist, has no item for it.
  `resolve-ticket`'s Reporting (:571-589) has none either.

**Edit:** in `pr-harden` **Reporting**, add after the declined-findings bullet:
`- The follow-up issue, by number — or that the run left no non-blocking finding unimplemented.`
No `resolve-ticket` edit: its rounds bullet and Step 9 already defer to `pr-harden`.

**Prunes:** nothing. Net +1 line, justified as the report item FINISH already requires and the
report's own list omits.

## P4 — the round-4 rule's figures are half wrong

**Bar: none of the three is needed — this is a correction of a measured claim, not a new rule** (the
ledger's *APPLIED as a correction* entries are the precedent, `REJECTED.md`:2731 and :3060 — both still
went through Step 5). The rule itself is not proposed for
deletion.

`pr-harden`:118-123 states, of PR #465: 8 of the 12 blocking findings were introduced by an earlier
round, "by the findings' own attribution"; "four of the eight came from a NON-blocking prose fix: a
sentence written in round N that round N+2 then correctly faulted"; "Rounds 4 to 9 spent 13
non-blocking prose edits and four prose blockers between them while the runtime behaviour had been
settled since round 3"; and the terminating round "was blocking-only, returned zero findings, and was
its cheapest". The run's record (`2026-09-21-chartsearchai-PR465`:46-50) carries only the 8-of-12.

**Measured 2026-09-23** from the orchestrator transcript
(`~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-chartsearchai/56821ae1-6ea6-47f4-aabd-68fffc39facd.jsonl`,
reviewer JSON at L1468, L1579, L1709, L1803, L1863, L1927, L1981, L2040, L2176; fixer JSON L1524-L2073)
and `git log -S` / `git diff` in the chartsearchai clone. Calibrated: the extraction yields exactly 12
blocking findings of 38, the record's 8 IDs among them; reviewer token counts re-derived from the
subagent transcripts match the notifications. Extracts: this session's scratchpad `pr465/`
(`reviews.json`, `classification.tsv`).

| figure | verdict |
|---|---|
| 8 of 12 introduced by an earlier round | TRUE by git provenance. "By the findings' own attribution" is not: only r7-1 and r8-1 name the introducing commit. r3-2 is arguably a ninth. |
| four of the eight from a NON-blocking prose fix, round N faulted in N+2 | FALSE. Three trace to an edit that implemented a NON-blocking finding (r7-1 ← r6-5, r7-3 ← r6-6, r8-1 ← r5-4 then r6-6); one of those sources is a prose fix; none is N→N+2. The other five came from BLOCKING fixes (r1-1, r2-1, r3-1). |
| 13 non-blocking prose edits in rounds 4-9 | FALSE. 13 non-blocking findings were implemented in rounds 4-6 (r4-2..4, r5-2..5, r6-2..7); 11 are prose, r6-5 and r6-6 are code. |
| four prose blockers in rounds 4-9 | TRUE (r5-1, r7-2, r7-3, r8-1, of 7 blockers). |
| runtime behaviour settled since round 3 | FALSE. Round 6's fixer changed executable shell and reported `"runtime_visible": true`. |
| terminating round blocking-only, zero findings | TRUE — but its retry brief told the reviewer "An empty findings array is the expected, correct outcome", which `pr-harden`:1212-1213's *Don't brief the reviewer with what was fixed* is the nearest rule against. |
| and was its cheapest | TRUE for the attempt that produced the result and for the round (it had no fixer); not on duration, tools or output tokens once its dead first attempt is charged to it. |

**Edit — keep the rule; replace :118-123's figures with the measured ones, deleting what is false.**
> On a 9-round run of `openmrs-module-chartsearchai` PR #465 (2026-09-21), **8 of the 12 blocking
> findings were introduced by an earlier round of that same loop**, and three of them — r7-1, r7-3 and
> r8-1 — by an edit that implemented a NON-blocking finding, which is the edit this rule stops the fixer
> making. The other five came from blocking fixes, which it does not touch.

The "cheapest" and zero-findings clauses are deleted rather than qualified: the first is trivially
true of a round without a fixer, and the second was obtained under a brief that primed it.

**Prunes:** three false figures and one false universal. Net −2 lines.

**What the correction does to the rule's weight, stated for the refuter rather than argued:** the rule
would have prevented the three non-blocking-fix blockers of rounds 7-8 and none of the other five.

## Ledger updates owed whatever the refuter decides

- *A FINISH rule costing something real*: the reopen condition was met on 2026-09-14 and no window
  counted it. Records that say the rule cost something: `2026-09-12-…-294`:162-166 (the parked one),
  `2026-09-14-…-412`:26 ("reads as more than a nit"), `2026-09-14-…-276`:28 ("the branch ships with a
  known named residue"), `2026-09-13-…-409`:28 ("a real prose defect now owed to a follow-up"). The
  `2026-09-14` window that read #412 (`proposals/2026-09-14-retro-window-409-412-425-426-315.md`, 287
  lines) contains none of FINISH, follow-up, cleared, #429, non-blocking.
- *Wall-clock P1* (`REJECTED.md`:3507-3515): its premise is the stale :686-688 clause; its REOPEN asks
  how often §7 pushes. From the records: after the terminating review FINISH pushed on PR414, PR423
  and PR478 (a non-blocking finding taken) and on PR417, PR424, PR426 and PR475 (a merge of a
  moved `main`).
- The census above, as an observation.
