Scenarios A, C and D follow directly from the skill's text. B, F, G and H are settled too, with small gaps noted. Only E leaves its round-record entry undecided. Citations are to `SKILL.md` unless marked `blind-agents.md`.

**A. `123`**
1. Four agents: one on the whole PR plus one per criterion (3). That is under the cap of six. Line 38: "start one on the whole PR and, where the PR's ticket lists acceptance criteria, one per criterion, grouping criteria so a round starts no more than six". Each runs at `--deadline 5400`, because the 1800 deadline is only for "a whole-PR agent started without per-criterion agents" (`blind-agents.md:6-7`).
2. Yes. Line 145: "its exact text … goes to a blind agent with the PR head before you show, stage or post it, where you can start one".
3. Before reading any agent's report, append your own findings (line 38). Append again as each agent reports (line 200). An agent is owed if it is "past its deadline, that reports nothing, or whose report says the machine's load kept it from a run it planned" (line 38), or if "its log shows it never reached the model" (`blind-agents.md:10-11`).

**B. `123 --no-blind`**
1. None. Line 10: "With `--no-blind` … Step 2 starts none this round".
2. Yes. Line 10: "Step 3's blind readers, of a finding or of a fix you recommend, still run."
3. Write that Step 2 started none this round. None is owed for that. Line 10: "say so in the round record, and owe none for it".

**C. `123 --post --no-blind`**
1. None, for the same reason as B (line 10). `--post` has no effect on Step 2.
2. Yes, before posting. Line 145: "before you show, stage or post it".
3. Same as B. Separately, `--post` makes a false positive costly, which counts toward Step 3's refutation-pass criteria (line 141).

**D. `123` plus "skip the blind agents this time"**
1. None. Line 10: "or when the user asks for a review without blind agents, Step 2 starts none this round".
2. Yes (line 10).
3. Same as B: say so in the record, and none is owed.

**E. Started by pr-harden, `123`**
1. None. Line 38: "unless you are a reviewer pr-harden started".
2. Yes, as far as the text says. Line 145 applies "where you can start one" and nothing exempts pr-harden from it. Line 10's "still run" sentence only covers `--no-blind`, so it doesn't apply here.
3. **Undecided.** The "say so in the round record, and owe none" instruction (line 10) covers only `--no-blind` and the user's request. Nothing says what to write when pr-harden is the reason. They aren't owed, though: line 38 names owed agents only among those that were started, and these never were.

**F. The round after B, new head, `123`**
1. Four: whole PR plus 3 criteria (line 38). "Once per round, on the head it reviews".
2. Yes (line 145).
3. Same as A. Round B's skipped agents aren't carried over, because B owed "none for it" (line 10). Record this round's agents as they report.

**G. Earlier whole-PR agent owed, now `123 --no-blind`**
1. No re-run. Line 10: "Step 2 starts none this round; … while an agent an earlier round owes stays owed."
2. Yes (line 10).
3. The record says Step 2 started none this round because of `--no-blind`, and that the earlier whole-PR agent is still owed. It stays owed afterwards.
   - There is a slight tension with line 22: "before this round's verdict, run or apply each owed item, or say it is still owed". Line 22 alone would allow a re-run. But line 10's "Step 2 starts none" plus "stays owed" picks the "say it is still owed" branch.

**H. `123 --no-blind`, rushed pass, blocking judgment call**
1. Yes, challenge it with a separate blind reader. At least two of line 136's conditions hold ("when **two or more** of these hold"):
   - line 138: "a **judgment call, not a mechanical fact** — 'is this actually a race?'"
   - line 140: "the pass is **rushed or low-effort**"
   - Line 139 (solo, no fan-out) arguably holds too. Line 141 (high cost of a false positive) holds only if this is a `--post` round or a request-changes verdict.
2. `--no-blind` does not decide this. Line 10 keeps Step 3 readers running ("Step 3's blind readers, of a finding … still run"), and line 136's count of conditions decides whether to use one. It's still subject to "where you can start one". Outside CI, that reader is `blind-run` (line 145).
