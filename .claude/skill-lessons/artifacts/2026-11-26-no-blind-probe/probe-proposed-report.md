# What the skill text decides, scenario by scenario

The text decides most of the answers. It leaves three things open: what a pr-harden round (E) records, whether the user's wording in D also turns off Step 3's fix agent, and whether a fix agent counts as owed after it misses its deadline.

**Lines that decide most of the scenarios:**
- **SKILL.md:10:** "With `--no-blind`, or when the user asks for a review without blind agents, Step 2 starts none this round; say so in the round record, and owe none."
- **SKILL.md:38:** "Outside CI, have blind agents try to break the PR, unless you are a reviewer pr-harden started or the user turned them off (`--no-blind`, above). Once per round … start one on the whole PR and, where the PR's ticket lists acceptance criteria, one per criterion, grouping criteria so a round starts no more than six"
- **SKILL.md:145:** "A fix you recommend is different … goes to a blind agent with the PR head before you show, stage or post it, where you can start one". This line has no `--no-blind` or pr-harden exception. Line 10 scopes `--no-blind` to "Step 2" only.

---

**A. `123`**
1. **Four agents:** one on the whole PR and one for each of the three criteria. Per line 38, three is under the cap of six, so no grouping is needed. All four run at `--deadline 5400` with "about 75 minutes" in the brief. blind-agents.md:7 says "`--deadline 5400`, or `--deadline 1800` for a whole-PR agent started without per-criterion agents". This round does start per-criterion agents.
2. **Yes,** per line 145.
3. Before reading any agent's report, I append my own findings to the round record (line 38). Each mid-round entry names "what each running agent checks" (line 200). An agent is owed only if it is "past its deadline, that reports nothing, or whose report says the machine's load kept it from a run it planned" (line 38).

**B. `123 --no-blind`**
1. **None.** Line 10: "Step 2 starts none this round."
2. **Yes, still.** Line 10 limits the flag to Step 2, and line 145 has no flag exception. The argument hint `[--no-blind]` reads broader than that, but the text only defines it for Step 2.
3. I write that Step 2 started no agents because of `--no-blind`. None are owed (line 10: "say so in the round record, and owe none"). A Step 3 fix agent that misses its 30 minutes is still owed (line 145: "record the agent as owed"), because "owe none" covers only Step 2.

**C. `123 --post --no-blind`**
1. **None,** per line 10, same as B.
2. **Yes.** The fix goes to a blind agent "before you show, stage or post it" (line 145). `--post` doesn't change that.
3. Same as B. Line 38 holds posts back until the Step 2 agents report, but no Step 2 agents are running, so that hold doesn't apply here. Posting still waits on any fix agent until it reports or times out.

**D. `123` plus "review it, but skip the blind agents this time"**
1. **None.** Line 10: "or when the user asks for a review without blind agents."
2. **The text doesn't decide this.** Line 10 maps that request onto Step 2 only. The user's words ("skip the blind agents") are broader, and line 145's "where you can start one" may or may not cover a user telling me not to. I would ask the user rather than guess.
3. Same as B: say so in the record, and none are owed.

**E. pr-harden starts me with `123`**
1. **None.** Line 38: "unless you are a reviewer pr-harden started."
2. **Yes, as far as the text says.** Line 145 has no pr-harden exception. Whether I can launch an agent from inside pr-harden is not stated in these two files.
3. **The text doesn't decide this.** Line 10's "say so in the round record, and owe none" is triggered only by `--no-blind` or a user request, not by pr-harden. Line 38 lists pr-harden as an exception but says nothing about what to record. Nothing makes these agents owed, since the owed rule only covers agents that were started. But nothing states "owe none" either, so a later round can't tell from the text whether they're owed.

**F. The next round after B, on a new head, with `123`**
1. **Four agents** on the new head, at 5400 each, same as A. Line 10's "this round" means B's `--no-blind` doesn't carry over.
2. **Yes,** per line 145.
3. From B, Step 1 finds nothing owed for Step 2 (line 22: "run or apply each owed item"), because B recorded "owe none". The only exception is a B fix agent that missed its deadline. After that, recording works as in A.
