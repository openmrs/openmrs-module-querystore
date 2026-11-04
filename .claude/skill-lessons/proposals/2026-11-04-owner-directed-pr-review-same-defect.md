# 2026-11-04 owner-directed — pr-review: the same defect, where the PR didn't look

**Origin.** On 2026-11-04 the owner asked whether the miss in
`2026-11-04-pr-review-stockmanagement-53-missed-input.md` required a `pr-review` edit. They were told it
would go through skill-retro with a replay, and directed the change.

**Bar.** One record: below bar (a), not bar (b) (no rounds were spent), and not bar (c). It proceeds as
an owner-directed change, as `2026-09-25-owner-directed-prose-prune-wait.md` did ("No new run record"),
and rests on the pre-registered replay `2026-11-04-pr-review-same-defect-replay-53.md`.

## P1 — the measured text

`pr-review/SKILL.md`, Step 3, *Trace outward*: a new bullet directly after **Unchanged neighbors**.

> - **The same defect, where the PR didn't look.** When the change fixes a kind of defect (an unguarded
> input, a missing null check), look for the same kind on the same path in the code it left alone, such
> as every other input that reaches the line the PR now protects. An instance that predates the PR blocks
> only if it defeats the PR's own guarantee; otherwise it is a suggestion, which the author may take as a
> follow-up.

- Measured: K1 filed by E 2 of 3, C 0 of 3. In their notes, two C runs and the third E run (wt-03) saw K1
  and did not file it (artifacts `PROTOCOL.md`, RESULTS).
- Version: `pr-review` 0.24.0 → 0.25.0 (minor, a new rule).
- Step 4: the bullet subsumes, retires or makes stale no existing rule. Net +80 words (`LC_ALL=C wc -w`,
  8430 → 8510). Paid for by a raise of the `pr-review` budget, with its `why` in `raises`.

## Open question for the gate — P1′, unmeasured

wt-03 (E) wrote: "No operation is mutated, so the PR's guarantee holds and I did not raise it." That
reads P1's last sentence as deciding WHETHER to raise the instance, not how to file it. A candidate
rewording of that sentence, NOT measured:

> An instance that predates the PR is raised all the same: it blocks only if it defeats the PR's own
> guarantee, and is otherwise a suggestion the author may take as a follow-up.

## Lines the gate should check P1 against

`pr-review/SKILL.md`:42 (*Does it do only that?*), :103 (*Unchanged neighbors*), :179 (*Don't invent
concerns to look thorough*), :181 (*Don't critique an inflated version of the PR*), and
`skill-lessons/REJECTED.md`'s parked `pr-review` remedies from the 2026-10-23 window (:5585 onward).

## The gate's reply, verbatim

One headless refuter (`refuter session=4c7f70ad-decf-423e-93e7-9baf200e104c`, `claude-opus-5-5`, started from a folder holding copies of everything
above, no project memory, read-only, $1.10). Its first reply, then the reply it gave
after its Stop hook refused the first for listing unchecked items without a reason:

### First reply

**P1: ship. P1′: park.**

**1. Does the record say what the proposal says?** Yes, with one small change to a quote.
- I read all six `out/wt-0*.json` files against `KEY-private.json` and `adj/map-private.json`. K01 is wt-04 and K03 is wt-01, both in arm E and both filed `nit`. The C runs filed no K1 finding. wt-02 and wt-06 (C) and wt-03 (E) mention K1 only in their notes. wt-05 never mentions it. All six say "Ready to merge".
- NON-BLOCKING: the proposal quotes wt-03 as "...did not raise it". wt-03's note actually ends "did not raise it **as a finding**" (`out/wt-03.json`). The shortened quote makes the misreading P1′ rests on look stronger than it is.
- The origin claim holds. The session transcript has "Doesn't this require updating the pr-review skill?" (20:39:32Z) and then "yes" (20:45:39Z).
- What the record says happened matches the PR. Wikum flagged it as optional: "I'm fine with doing it here, but a follow-up PR works too." The fix `a9c2a39` adds `getName() == null` → `IllegalRequestException`.

**2. Is the bar met?** Yes, and nothing is counted twice. The proposal counts one record, gets no corroboration from the measurement (consistent with REJECTED.md:2148-2150), and rests on the owner's direction, as the precedent did. The result is E 2, C 0, which matches the reading the protocol committed to in advance ("E 2-3 and C 0-1 … goes to Step 5's refuter").
- NON-BLOCKING: with 2 hits among 6 runs, the chance that both land in E is 3 in 15, about 0.2. The bar was met, but the signal is thin. PROTOCOL.md lists "n=3; one PR; one defect" under Threats.

**3. Does it contradict an existing rule or a recorded rejection?** No re-proposal. One tension:
- NON-BLOCKING: "blocks **only** if it defeats the PR's own guarantee" pulls against SKILL.md:183, "A finding is blocking because of its written failure mode". Read literally, a pre-existing security exposure on the same path would be filed as a suggestion. REJECTED.md's parked "A demonstrated privilege bypass filed below blocking" (2026-10-23) worries about exactly that direction. It is also an "only" of the kind skill-retro Step 4 lists. It is not blocking, because :181 draws the same scope line ("respond to the author's actual scope; larger redesigns are follow-up `suggestion`s").
- P1 is none of the parked 2026-10-23 remedies. It is not (b), a thread naming transaction and handler order. It is not "A limitation the PR declares": K1 was neither a declared limitation nor a blocker.

**4. Does it add an unchecked claim?** Two.
- NON-BLOCKING: the "blocks only if" clause was never tested on a pre-existing instance that has a serious failure mode, because K1 has none.
- NON-BLOCKING: the bullet's noise was not measured. E filed 6 findings and C filed 3, on one PR.
- "Subsumes nothing" holds. :37 is about other values of the same input, not other inputs. :57 is about the shape of the fix. 8430 words is correct (`LC_ALL=C wc -w`), and the bullet is 80 words. I could not check `skill-budgets.json`: it **needs access**, since it is not in this folder.

**5. Would it have prevented the cited run's miss? Does it change what gets filed?**
- NON-BLOCKING: the replay did not reproduce how the original run missed it. The record says the original run never raised the question: "An absent or null `name` has zero mentions". In the replay, C runs saw K1 2 of 3 times. So the measurement shows a change in **what reviewers file** (E 2/3 against C 0/3), not in **what they notice** (E 3/3 against C 2/3).
- The bullet's "every other input that reaches the line the PR now protects" does name the original gap ("It varied only `uuid`").
- The original run's conditions were not replayed: max effort, the PR conversation, and a second-reviewer subagent with a closed brief.
- Filed as a suggestion, K1 would have matched what the maintainer actually did.
- NON-BLOCKING: no run followed the disposition the bullet sets. Both E filings are `nit` where the bullet says "suggestion" (`adj/candidates.json` K01, K03), and the third E run did not file at all. Two E runs did use the bullet's wording on a different finding: wt-01 "doesn't break the PR's own guarantee", wt-04 "does not weaken the action-endpoint guarantee". So they read it, and the change in filing is plausibly the "may take as a follow-up" licence at work.

**6. P1′: is it needed, and is shipping it unmeasured allowed?** It is not established as needed, and shipping it would break the store's rule.
- BLOCKING: the case for it is an inference from one note. wt-03's transcript shows no other trace of how it reasoned. Its reason, "No operation is mutated, so the PR's guarantee holds", is the same reason C run wt-02 gave with no bullet at all: "pre-existing, out of scope, no wrong-operation effect". So the claim that the bullet caused wt-03 not to file is not shown.
- BLOCKING: an unmeasured rewording to fix a reading goes against skill-retro Step 4: "Prefer deleting an unsupported clause to rewording it … every correction that replaced a false claim with a better-sounding one introduced a new false claim". REJECTED.md:39 repeats it: "the EXISTING advice (delete rather than reword) is what converged it".
- NON-BLOCKING: "raised all the same" turns the licence into a requirement to file every pre-existing instance. That pulls against :114 ("don't invent findings to fill a quota") and :179.
- It also keeps the same "blocks only if" clause, so it fixes nothing from point 3.

P1: ship. The pre-registered bar was met on blind, verified scoring, and every objection is non-blocking.
P1′: park. Its premise is one run whose reason matches the control arm's, and an unmeasured rewording is what Step 4 and REJECTED.md:39 tell the store not to ship.

### After the Stop hook

**P1: ship. P1′: park.** My verdicts don't change. Below are the two items the gate flagged, rewritten with what I could still check from the data here.

The trigger, quoted: "Items without a reason: NON-BLOCKING: the "blocks only if" clause was never tested … | NON-BLOCKING: the bullet's noise was not measured."

**Point 4, item 1 (NON-BLOCKING): the "blocks only if" clause.**
- I checked the six outputs again. No finding in any run is a pre-existing instance with a severe failure mode. K1 itself records "No operation is changed" (`adj/candidates.json` K03), and the PR 53 sample has nothing that would test this clause.
- Testing it needs a pre-registered replay on a PR that holds such an instance. That **needs access**: this brief makes me read-only and bars subagents, so I can't launch headless runs or write their outputs.

**Point 4, item 2 (NON-BLOCKING): noise from the bullet.**
- **What I could check here:** I sorted every finding by run. Findings that report a pre-existing defect of the same kind:
  - E has 4: wt-01 F2 and F3, wt-04 F1 and F2.
  - C has 1: wt-02 F1, the sibling-resource finding.
- None of the 5 was filed blocking.
- Each is backed by its own evidence. wt-04 F1 confirmed the sibling-resource defect with a throwaway test ("I confirmed this with a throwaway test"). K1 matches what the maintainer found and `a9c2a39` fixed.
- E's other extra finding, wt-01 F1 (a pom ByteBuddy nit), is not shaped like the bullet.
- So on PR 53 the bullet added no finding I can show is false.
- **What is still open:** whether it adds noise on other PRs. Measuring that also **needs access**, for the same reason: new headless runs on another PR, which my read-only, no-subagent brief rules out.

P1: ship. It met the pre-registered bar under blind, verified scoring, and every objection, including these two, is non-blocking.
P1′: park. Its premise is one run whose stated reason matches the control arm's (wt-02), and skill-retro Step 4 and REJECTED.md:39 both say not to ship an unmeasured rewording.

## What the proposer checked and changed

- **wt-03's quote** (point 1) is confirmed: `out/wt-03.json` `notes` reads "…and I did not raise it as a
  finding." It was corrected in the measurement note and the protocol's results. The sentence in the
  measurement note that took wt-03's note as a misreading of the bullet was deleted (point 6). This file's
  submitted text above is left as submitted.
- **P1 ships as measured**, with no word changed. Every objection against it was non-blocking. The
  non-blocking ones (points 2, 3, 4 and 5) are recorded with REOPEN conditions in REJECTED.md's 2026-11-05
  section.
- **P1′ is parked** on its two blocking objections (point 6).
