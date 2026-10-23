# 2026-10-23 owner-directed — harden's gate sentences

The proposal as submitted to the gate, the gate's reply (verbatim, but for one redacted line), then what
the proposer checked and changed. Paths under `/private/tmp/…/scratchpad/` were this session's scratchpad; the
drafts there were never applied.

# Owner-directed change, 2026-10-23: harden's gate sentences — proposal for refutation

**Origin.** On 2026-10-23 the owner was told that harden's Transition and Termination gate sentences are
never stated in their literal form, and that deleting or replacing them was the owner's call. The owner
directed the change.

The ledger parked the observation with this REOPEN (`~/.claude/skill-lessons/REJECTED.md`, the 2026-10-23
window block's entry "Observed: harden's gate sentences are not said in their literal form"): "a
proposal to delete or replace those sentences. This count is evidence that they are not said, not that
they do nothing." Do not read `~/.claude/skill-lessons/confidential/`.

**The measurement**, in `~/.claude/skill-lessons/artifacts/2026-10-23-retro-window/` (see its README):

- 61 public-record transcripts load harden, all on a version carrying every gate. 0 state the
  Transition gate's sentences or the Termination gate's, and 2 state the Phase 2 gate's.
  - The first 58 are in `gate-sentences-after-deletion.out`, the other 3 in
    `gate-sentences-relocated.out`.
  - The detector is `gate_sentences.py`, which matches each sentence's opening words, with the
    controls in `controls.out`.
- The reader check (`reader-check-after-deletion.out`) shows runs announcing convergence in their own
  words.
- The enforced half is unaffected. `harden-cycle-gate.sh` and `gate-state` read the verdict a run
  records with `gate-state harden-set`, never the report's text. A grep of both gate copies,
  `gate-state`, `pool-run` and `pool-test.py` for the template wording finds only the gate's own
  messages about "Phase 1 verdict" in its state.

**The choice: replace the templates with their required content, rather than delete the gates.** The
count shows the literal form is not used, but it does not show the checks do nothing (the REOPEN above).

## P1 — harden 0.45.0: each gate states its required content instead of a fill-in template

The edit is in `draft.diff` beside this file. `a/` holds the live files and `b/` the proposal, over
`harden/SKILL.md`, `harden/evidence.md` and `skill-retro/skill-budgets.json` (the last unchanged).

**SKILL.md.**
- **Transition gate (:77-85).** The three bracketed templates become three bulleted items:
  - the stopping condition and the last pass's edit count;
  - each integration question, now listed;
  - each behavior change without a test.
  "If you cannot truthfully complete ALL THREE sentences" becomes "If you cannot truthfully state all
  three". The rest of :85 is unchanged.
- **Phase 2 gate (:156-158).** The template becomes a four-item list: the agents, the substantive
  findings, the edits and the deferrals.
- **Termination gate (:185-190).** The template becomes a three-item list: Phase 1's verdict, Phase 2,
  and the measured edits. "Emit it even when the answer is obvious. If you cannot fill in the Phase 1
  verdict…" becomes "State it even when the answer is obvious. If you cannot give the Phase 1
  verdict…".
- **References.** :196's "the convergence line" becomes "the Termination gate's statement". :252's
  "Emitting the termination gate is a forcing function" becomes "The Termination gate's statement is a
  forcing function". :367's "the termination gate from Termination, filled in" becomes "the Termination
  gate's statement".
- **Not changed:** the override line (:194). No measurement covers it.
- **Size and version.** Words stay at 7429, the budget. The version goes 0.44.1 → 0.45.0, a minor bump
  because the rule's form changes.

**evidence.md, H2-06 (:212-214).**
- Its quote of the rule becomes the new opening, "Before ending the run, the report states, with the
  measured values".
- A "*Measured 2026-10-23, 0.45.0:*" note records that the passage's "the phase gates demand verbatim
  sentences and get them" did not hold, with the counts and their files.
- The passage itself stays, as the record of what it said.

**Step 4.** The edit prunes the three templates and adds their content as lists, for net 0 words. It
subsumes nothing else.

---

# Gate 1 — the reply, verbatim but for one redacted line

**PARK.** The owner approved this change after being told the gate sentences are "never stated in their literal form". The transcripts and logs on disk show that is false. Until 2026-09-22, runs stated this same template text routinely. Since then they state neither the wording nor the integration answers it asks for. So the edit fixes a wording problem the data don't show, and why the drop happened is still unknown.

**B1 · BLOCKING (the question stays open): the measurement only covers runs after the drop, and the draft reads it as the gates never being said.**
- **The 61 count reproduces.** The controls re-run byte-identical. All 61 transcripts use `claude-opus-5-5` and a harden of 0.34 or later. All 61 ran `harden-set … --phase1 converged`, so every one reached the Transition gate (`/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-querystore/80e823df-8753-4cb7-8aa5-6aead0d9104a/scratchpad/refuter6/public61-converged.out`; the controls gave 1 and 0).
- **Earlier runs did state the sentences.** `~/.claude/pipeline/logs/` holds the pool's stream logs of 13 runs from before 0.34 (2026-08-26 to 09-17; 12 on `claude-opus-5`, 1 on `claude-sonnet-5`). Their transcripts are gone from `~/.claude/projects`, and the 2026-10-23 scripts only searched `~/.claude/projects`.
  - The unmodified `gate_sentences.py` finds the Transition sentences in 8 of the 13 (stop 11, integ 7, tests 7), e.g. `20260917T093644Z-…-445.jsonl` L1680 and `20260902T192225Z-…-347.jsonl` L1339 (`/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-querystore/80e823df-8753-4cb7-8aa5-6aead0d9104a/scratchpad/refuter6/pipelogs.out`).
  - I added the opening words of the old Stopping and Cycle gates and checked them against a good and a bad control. With those, 12 of 13 runs state at least one gate literally, including 22 Cycle-gate hits (`/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-querystore/80e823df-8753-4cb7-8aa5-6aead0d9104a/scratchpad/refuter6/pre034-old-patterns.out`).
- **The template text did not change.** The Transition gate at `6f15b1c^` and the live one differ only in "BOTH" → "these".
- **Attended runs use the literal form too** (`/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-querystore/80e823df-8753-4cb7-8aa5-6aead0d9104a/scratchpad/refuter6/all-template.out`):
  - `0fa317a7…` L2842 (Opus 5, before 0.34) states all three Transition sentences.
  - [Redacted from this record: one session the gate cited here belongs to a ticket whose records are confidential.]
- **So two statements in the draft are false.** proposals.md's "never stated" is false. b/evidence.md:216's "They did not." is false for the period the passage described: written 5bc2d71 (2026-08-17), reworded 6f15b1c (2026-09-22). The ledger limited its version to "For this population" (REJECTED.md:5559), and the draft drops that limit.
- **The content is gone too, not just the wording.**
  - My check counts an assistant text block that names at least 4 of the 6 integration-question topics. I checked it against the 13 old logs: it found 6 of the 7 runs with a literal answer and none of the 6 without.
  - On the 61 it finds 0 (the most any transcript reaches is 3 topics, in one). Tool inputs give 0 as well (`/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-querystore/80e823df-8753-4cb7-8aa5-6aead0d9104a/scratchpad/refuter6/public61-integ.out`).
  - Sampled runs go straight from recording the verdict to "Four Phase 2 review agents are running".
  - So b/evidence.md:219-220 ("Runs state convergence in their own words. So 0.45.0 asks … content") does not follow. Runs state the verdict, not the integration answers. Nothing shows the edit would have prevented what it cites (skill-retro:131).
- **Three changes landed together, and the data cannot separate them:** harden 0.34.0 (the verdict moved into `gate-state harden-set`), the switch from Opus 5 to Opus 5.5, and Claude Code 2.1.273 → 2.1.280+.
- **Fix:**
  - Give the owner these facts and park the proposal. It reopens when the owner decides again on them, or when a measurement separates 0.34.0 from the model change.
  - If the owner re-directs, delete "They did not" and :219-220 rather than rewording them (skill-retro:115-117).
  - State both periods with their counts and say the cause is not established.
  - Archive the pipeline-log counts under `artifacts/`, because `~/.claude/pipeline/logs/` is not in the repo.

**B2 · BLOCKING: the Phase 2 gate loses content.**
- a:158's "Phase 2 ran once" is the statement that enforces :145's measured "runs ONCE" rule. It is gone from b:160-164.
- "Edits made: [N]" became "the edits it made", without the "as a number" the Transition item got at b:79.
- The proposal's "four-item list" summary does not mention either loss.
- Fix: "- that it ran once, over how many agents;" and "- the edits it made, as a number;".

**B3 · BLOCKING: the Phase 2 gate is outside what the owner directed.** The Origin names only the Transition and Termination gates. The Phase 2 change has no owner direction and meets no Step 3 bar, and it is the one gate whose literal form runs actually use (2 of 61). Fix: drop that part of the edit, or record that the owner's direction covers it.

**B4 · BLOCKING: the evidence goes under the wrong heading.** skill-retro:109-112 says the measurement that retires a rule goes under that rule's heading, quoting it. The note sits only under Termination's H2-06. evidence.md has no Transition gate entry (REJECTED.md:5456), so following harden SKILL.md:13 ("read its entry there") finds nothing for the Transition or Phase 2 gates. Fix: add entries under evidence.md's Phase 1 and Phase 2 headings that quote b:77 and b:160 and name H2-06.

**NON-BLOCKING**
- b:83 "a consumer scans before it registers": "it" reads as the consumer, but :33 means the slice. Fix: "before the slice registers".
- b:80 drops "concrete" from a:81's "concrete behaviors observed or verified".
- b/evidence.md:216 "the passage above" is the kind of positional reference harden:438-439 says to replace with a named target, e.g. "H2-06's passage".
- `gate_sentences.py` matches on the template wording. After 0.45.0 there is no fixed text left to match, so this measurement cannot be repeated. The Step 4 answer should say so.
- Re-running on `public-transcripts.tsv` today gives 57 transcripts loading harden, not 58. The TRUNK-6826 transcript `74a2fd99…` moved to `-Users-danielkayiwa-Projects-openmrs-openmrs-core/`; looking it up by session id restores 58.
- The detector's `stop` pattern also matches the old "Phase 2 stopping condition met". It doesn't change the zero.
- Cosmetic: "ALL THREE" lost its capitals; "that last pass" (b:79) has nothing to refer back to; the gate spells it both "behaviour" and "behavior" (b:80, b:87).

**The five questions**
1. **Does the measurement say what the proposal says?** Only for runs on Opus 5.5 and harden 0.34 or later. The 0 / 0 / 2 of 61 reproduces, but earlier runs contradict the reading (B1).
2. **Fields and consistency.**
   - Termination keeps every field and both decision rules.
   - Transition keeps them all, apart from "concrete" and the pronoun.
   - Phase 2 loses "ran once" and the number of edits (B2).
   - The rest reads consistently: b:204, :260, :375, the override line, and the gate's messages (harden-cycle-gate.sh:323, :334, :375). pr-harden:598's "convergence line" is pr-harden's own, and resolve-ticket:568 refers to pr-harden's Termination.
   - All 43 rule quotes in evidence.md match b/SKILL.md. As a check, the old H2-06 quote fails against b.
3. **Contradictions with the ledger.** REJECTED.md has no ruling on forcing-function sentences (0 hits for "forcing"). The proposal does reverse the 5bc2d71 reasoning recorded at evidence.md:214, using only data from after that reasoning stopped holding (B1), and its Phase 2 change goes beyond the direction (B3).
4. **Unchecked claims.** "They did not" is false, :219-220 does not follow, and the Origin's "never" is false. The SKILL.md text adds no factual claim.
5. **Budget and linter.** `LC_ALL=C wc -w` gives 7429 for both a and b, against a budget of 7429, and the budgets file is identical to the live one. The linter on b reports 0 findings (rc 0); the same copy plus one word reports "7430 words, over its budget of 7429" (rc 1). The B2 and non-blocking fixes add about 9 words, so they need a prune (e.g. "with their specifics" at b:77) or a `raises` entry.

I wrote nothing outside `/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-querystore/80e823df-8753-4cb7-8aa5-6aead0d9104a/scratchpad/refuter6/`. One exception: a `git fetch` in the querystore checkout updated its remote-tracking refs.

---

# Gate 1 — what the proposer checked, and what changed

**One redaction.** One line of the reply above has been redacted. It cited a session that belongs to a
ticket whose records are confidential: the transcript matches the private keys 183 times. Nothing
from that session is drawn on here.

**Re-checked before acting.** Each instrument is archived in
`artifacts/2026-10-23-gate-sentences-periods/`, with a README.

- The 26 per-ticket pool logs dated 2026-08-26 to 2026-09-17 match no private key, and 13 of them load
  harden.
- `gate_sentences.py` finds the Transition gate's sentences, in their literal form, in 8 of the 13.
  7 of the 13 state "Integration questions answered".
- `integ_content.py` at K=4 finds 6 of the 13 and 0 of the 61. It misses one of the 7 literal-answer
  runs, #310 (best 3), and flags none of the other 6.
- `models.out`: Opus 5 and Sonnet 5 before 0.34. Opus 5.5 in all 61, on Claude Code 2.1.280 and
  later. The pool logs record no Claude Code version, so the gate's "2.1.273" is not repeated.

**Decision: parked, as the gate ruled.** Nothing live changed, and the drafts stay in this session's
scratchpad.
- The owner's direction rested on "never stated", which holds only for the 61 runs from after 0.34.
- Those 61 runs also leave out the Transition gate's content. So asking for content rather than
  wording would not address what the data show.
- Blocking points B2-B4 lapse with the proposal, and are recorded in the ledger.

**For the owner.**
- harden 0.34.0, the model and Claude Code all changed together.
- Since then, the Transition gate's integration answers and test enumeration appear in none of the 61
  runs' reports.
- Which change caused it is not established.
