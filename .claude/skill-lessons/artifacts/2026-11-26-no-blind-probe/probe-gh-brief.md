Stop after about 8 minutes of work and report what you have.

Read two files and nothing else: /private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/5db247f5-23fd-4609-a5dc-8b2de0889fe7/scratchpad/proposed-1126b/probe-proposed/SKILL.md, a skill an agent follows to review a GitHub pull
request, and /private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/5db247f5-23fd-4609-a5dc-8b2de0889fe7/scratchpad/proposed-1126b/probe-proposed/blind-agents.md beside it. Run no command but reading those two files, use no network,
and change nothing outside your own folder.

You are the agent following that skill, in a terminal session with the user (not in CI). PR 123's ticket lists three
acceptance criteria. Answer each scenario from the skill's text alone, quoting the line that decides it, and say so
where the text does not decide it rather than guessing.

G. The previous round's record says its whole-PR blind agent hit its deadline and is owed. This round the user runs
   the skill with arguments: 123 --no-blind. Do you re-run that owed agent this round? What does this round's record
   say about it, and is it still owed afterwards?
H. Arguments: 123 --no-blind. Your pass is rushed, and you hold a blocking finding that is a judgment call ("is this
   actually a race?"). Do you challenge it with a separate blind reader, as Step 3's "When an adversarial refutation
   pass is worth it" describes? Does --no-blind decide that?

Report one short block per scenario.
