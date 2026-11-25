The text decides scenarios A, B, C, D, F, G and H. In E it decides only item 1; items 2 and 3 are not stated. The rule the most scenarios turn on is SKILL.md:10: *"With `--no-blind`, or when the user asks for a review without blind agents, Step 2 starts none this round; say so in the round record, and owe none for it, while an agent an earlier round owes is not run and stays owed. Step 3's blind readers, of a finding or of a fix you recommend, still run."*

**A. `123`**
1. I start four agents: one on the whole PR and one for each of the three criteria. SKILL.md:38: *"start one on the whole PR and, where the PR's ticket lists acceptance criteria, one per criterion, grouping criteria so a round starts no more than six"*. Since 4 is no more than 6, no grouping is needed. Each runs at `--deadline 5400` (blind-agents.md:7).
2. Yes. SKILL.md:145: the fix *"goes to a blind agent with the PR head before you show, stage or post it, where you can start one"*.
3. Before reading the agents' reports, I append what my own review has found (SKILL.md:38). A mid-round entry names *"what each running agent checks"* (SKILL.md:200). An agent is owed only if it *"past its deadline, that reports nothing, or whose report says the machine's load kept it from a run"* (SKILL.md:38).

**B. `123 --no-blind`**
1. None: *"Step 2 starts none this round"* (SKILL.md:10).
2. Yes: *"Step 3's blind readers, of a finding or of a fix you recommend, still run."*
3. The record says Step 2 started none, and none is owed for it: *"say so in the round record, and owe none for it"*.

**C. `123 --post --no-blind`**
1–3. Same as B. `--post` only changes publishing (SKILL.md:192). It does not change Step 2. It does make a false positive costly (SKILL.md:141), but that only matters for H's refutation pass.

**D. `123` plus "skip the blind agents this time"**
1–3. Same as B. SKILL.md:10 covers this case: *"or when the user asks for a review without blind agents"*.

**E. Started by pr-harden, `123`**
1. None: SKILL.md:38 says *"unless you are a reviewer pr-harden started"*.
2. Not decided. The "still run" sentence at SKILL.md:10 is attached to the `--no-blind` case. SKILL.md:145 applies *"where you can start one"*, and the text never says whether a reviewer started by pr-harden can.
3. Not decided. The "say so in the round record, owe none" rule at SKILL.md:10 is written for `--no-blind` or the user's request, not for pr-harden. The owed rule at SKILL.md:38 only applies to agents that were started. So nothing becomes owed, but the text gives no wording for the record.

**F. The round after B, new head, `123`**
1. Four agents, as in A. B's opt-out was *"this round"* only.
2. Yes, as in A.
3. As in A. Nothing carries over from B, because B *"owe[d] none"*.

**G. An earlier whole-PR agent is owed; this round is `123 --no-blind`**
- I do not re-run it: *"an agent an earlier round owes is not run and stays owed"* (SKILL.md:10).
- The record says Step 2 started none this round, and that the earlier agent is still owed. SKILL.md:22 backs this: *"run or apply each owed item, or say it is still owed"*.
- It is still owed afterwards.

**H. `123 --no-blind`, rushed pass, blocking finding that is a judgment call**
- Yes, I challenge it, as long as I can start a reader. At least two of the conditions hold: *"the finding is a judgment call, not a mechanical fact — 'is this actually a race?'"* (SKILL.md:138) and *"the pass is rushed or low-effort"* (SKILL.md:140). The threshold is *"when two or more of these hold"* (SKILL.md:136). "Solo in one pass" (SKILL.md:139) probably holds too, since Step 2's agents don't count either way.
- `--no-blind` does not decide it. SKILL.md:10 says Step 3's blind readers *"of a finding ... still run"*.
