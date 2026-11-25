**Answers:** A and F start 4 blind agents (1 whole-PR + 3 per-criterion). B, C, D and E start none. A fix you recommend still goes to a blind agent in every scenario, though for E that only follows because nothing exempts it. Only B, C and D say what the record should say about the skipped agents; E's text is silent.

**A. `123`**
1. You start 4 agents: one on the whole PR and one for each of the 3 criteria. Line 38: "Once per round … start one on the whole PR and, where the PR's ticket lists acceptance criteria, one per criterion, grouping criteria so a round starts no more than six". Each gets `--deadline 5400`, because blind-agents.md says "`--deadline 1800` for a whole-PR agent started without per-criterion agents", and here there are per-criterion agents.
2. Yes. Line 145: "its exact text … goes to a blind agent with the PR head before you show, stage or post it, where you can start one".
3. Line 38: "Before reading any of these agents' reports, append to the round record what your own review has found". Line 200 adds that a mid-round entry names "what each running agent checks", with another append "as each agent reports". An agent is owed only on line 38's terms: "One past its deadline, that reports nothing, or whose report says the machine's load kept it from a run it planned, is owed".

**B. `123 --no-blind`**
1. None. Line 10: "With `--no-blind` … Step 2 starts none this round".
2. Yes. Line 10: "Step 3's refuter of a fix you recommend still runs."
3. Line 10: "say so in the round record, and owe none." Nothing is owed.

**C. `123 --post --no-blind`**
1. None, same as B, decided by line 10. `--post` only changes whether the review gets published (Step 5).
2. Yes, line 10. Line 145 also requires it before you "post".
3. Same as B: "say so in the round record, and owe none." Line 38's hold ("Until each has reported … post nothing but the blockers") never applies, because no agents were started.

**D. `123` plus "skip the blind agents this time"**
1. None. Line 10: "or when the user asks for a review without blind agents, Step 2 starts none this round". Line 38's "or the user turned them off" points back to that line.
2. Yes. Line 10: "Step 3's refuter of a fix you recommend still runs."
3. Same as B: "say so in the round record, and owe none."

**E. pr-harden starts you with `123`**
1. None. Line 38: "unless you are a reviewer pr-harden started".
2. Yes, but only by silence. Line 145 has no pr-harden exemption and applies "where you can start one". Line 10's "still runs" covers only the `--no-blind` case. The text doesn't say whether a reviewer under pr-harden can start one.
3. **Undecidable from the text.** Line 10's "say so in the round record, and owe none" is tied to `--no-blind` or the user's request, not to pr-harden. Line 38 makes owed only an agent that was started, so nothing here becomes owed, but no line says to record that the agents were skipped. Line 200's general round-record append still applies.

**F. Next round, new head, `123`**
1. 4 agents again, as in A, at `--deadline 5400`. Line 10's exemption is scoped to "this round", and line 38 says "Once per round, on the head it reviews".
2. Yes, line 145.
3. Same as A. B's skipped agents are not carried forward: line 10 says "owe none", so when line 22 reads the record ("run or apply each owed item") there is nothing from B to run.
