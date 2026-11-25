**`--no-blind` and "skip the blind agents" have no basis in the skill.** It names one way out of the Step 2 blind agents: being a reviewer that pr-harden started. It also never says what a deliberately skipped agent goes down as in the round record. So scenarios B, C and D are undecidable on the points marked below, and so is part of F.

Lines the answers rely on:
- **Arguments (L10):** "a PR number or URL … plus optionally one of `--post` … or `--stage`". Argument hint is `<pr-number-or-url> [--post|--stage]`.
- **Step 2 agents (L38):** "Outside CI, have blind agents try to break the PR, unless you are a reviewer pr-harden started. Once per round … start one on the whole PR and, where the PR's ticket lists acceptance criteria, one per criterion, grouping criteria so a round starts no more than six"
- **Record (L38):** "Before reading any of these agents' reports, append to the round record what your own review has found." Also: "One past its deadline, that reports nothing, or whose report says the machine's load kept it from a run it planned, is owed (Step 5)."
- **Record (L200):** "As each agent reports … append to the PR's round record … what is still owed (a check that did not finish …)". Also: "A mid-round entry names … what each running agent checks".
- **Fixes (L145):** "its exact text … goes to a blind agent with the PR head before you show, stage or post it, where you can start one". The pr-harden exemption sits only in Step 2.

**A. `123`**
1. **4 agents:** 1 whole-PR plus 3 per-criterion (3 is under the cap of six, so no grouping). Each runs at `--deadline 5400` with "stop after about 75 minutes", because blind-agents.md uses 1800/25 only "for a whole-PR agent started without per-criterion agents".
2. **Yes**, per L145.
3. **Record:** your own findings go in before you read any report. A mid-round entry says what each agent checks. Each report is appended as it arrives. An agent is owed if it runs past its deadline, reports nothing, or cites load. Per blind-agents.md, one whose log shows it "never reached the model is owed, not clean".

**B. `123 --no-blind`**
1. **Undecidable.** L10 doesn't recognise `--no-blind`, and L38's only exemption is pr-harden. Read literally, the text still requires 4 agents. It never says whether an unknown flag overrides that.
2. **Yes**, as far as the text goes. L145 is a separate rule, and nothing ties it to a `--no-blind` flag.
3. **Undecidable.** If none start, L38's definition of "owed" (past deadline, no report, load) fits none of them. L200's "a check that did not finish" might cover a check that never started, but the text doesn't say so.

**C. `123 --post --no-blind`**
1. **Undecidable**, as in B. `--post` doesn't change L38.
2. **Yes**, per L145 ("before you show, stage or post it").
3. **Undecidable**, as in B. One extra effect: if no agents start, L38's hold ("Until each has reported or passed its deadline, post nothing but the blockers…") has nothing to wait on, and posting goes ahead.

**D. `123` plus "skip the blind agents this time"**
1. **Undecidable.** L38 has no opt-out for a user's request, only the pr-harden exemption. Read literally, the text still requires 4.
2. **Undecidable.** It depends on whether "the blind agents" includes L145's fix agent. The text doesn't say.
3. **Undecidable**, as in B.

**E. pr-harden starts you, `123`**
1. **None.** L38: "unless you are a reviewer pr-harden started."
2. **Yes.** L145 carries no pr-harden exemption, and it applies "where you can start one".
3. **Nothing is owed for them.** They were exempt; they didn't fail. The text doesn't require you to record that they were skipped. L200's end-of-round entry still has to be written.

**F. Round after B, new head, `123`**
1. **4 agents** on the new head, as in A. L38 says "Once per round, on the head it reviews."
2. **Yes**, per L145.
3. **Partly undecidable.** L22 says "run or apply each owed item, or say it is still owed". So if round B recorded its agents as owed, this round's 4 agents are what pays them off. Whether B recorded them as owed is the gap from B. For this round's own agents, record them as in A.
