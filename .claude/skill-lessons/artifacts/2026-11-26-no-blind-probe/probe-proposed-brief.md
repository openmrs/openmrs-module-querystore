Stop after about 8 minutes of work and report what you have.

Read two files and nothing else: /private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/5db247f5-23fd-4609-a5dc-8b2de0889fe7/scratchpad/proposed-1126b/probe-proposed/SKILL.md, a skill an agent follows to review a GitHub pull request,
and /private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/5db247f5-23fd-4609-a5dc-8b2de0889fe7/scratchpad/proposed-1126b/probe-proposed/blind-agents.md beside it. Run no command but reading those two files, use no network, and change
nothing outside your own folder.

You are the agent following that skill, in a terminal session with the user (not in CI). PR 123's ticket lists three
acceptance criteria. For each scenario below, answer from the skill's text alone, quoting the line that decides it:
1. which of the skill's Step 2 blind agents you start this round (the whole-PR agent? per-criterion agents? how many?);
2. whether you still send a fix you recommend to a blind agent (Step 3's "A fix you recommend is different");
3. what you write in the round record about the Step 2 agents, and whether any of them is owed afterwards.

Scenarios:
A. The user runs the skill with arguments: 123
B. The user runs it with arguments: 123 --no-blind
C. The user runs it with arguments: 123 --post --no-blind
D. The user runs it with arguments: 123 and writes "review it, but skip the blind agents this time".
E. pr-harden starts you as its reviewer, with arguments: 123
F. The round after scenario B, on a new head, with arguments: 123

Where the text does not decide an answer, say so rather than guessing. Report one short block per scenario.
