import sys
p = sys.argv[1]
s = open(p).read()
edits = [
 ("`https://openmrs.atlassian.net/rest/api/2/issue/<KEY>?fields=summary,description,status,comment,labels,fixVersions` (the `issues.openmrs.org` link people paste redirects to a dashboard and won't serve REST).",
  "`https://openmrs.atlassian.net/rest/api/2/issue/<KEY>?fields=summary,description,status,comment,labels,fixVersions,parent,issuelinks` (the `issues.openmrs.org` link people paste redirects to a dashboard and won't serve REST). Read the plan the ticket belongs to as well: its `parent` and the summary of each ticket in its `issuelinks`, or on GitHub the issue's `parent` (`gh api graphql`) and the issues it says it blocks or waits on."),
 ("Line-level review of code that shouldn't exist wastes both sides' time, so settle the approach before verifying the details. Two questions, in order:",
  "Line-level review of code that shouldn't exist wastes both sides' time, so settle the design first: before any line-level finding, decide whether it is right for the ticket and for the plan the ticket belongs to (Step 1), and, outside CI, write that verdict in the round record and open your report to the user with it. Two questions, in order:"),
 ("**When the approach really is in question, lead with that and stop stacking line-level comments.** Twelve nits on code that may not survive a rewrite are noise, and they bury the one comment that decides the PR. Post the approach finding plus whatever would survive the rewrite, hold the rest, and say you'll do the detailed pass once the direction is settled.",
  "**When the approach really is in question, lead with that and stop stacking line-level comments.** It is in question once your verdict, or a question you post, says so. Twelve nits on code that may not survive a rewrite are noise, and they bury the one comment that decides the PR. Post the approach finding plus whatever would survive the rewrite, hold the rest, including every drafted comment that would add more of what is in question, however sound its own point, and say you'll do the detailed pass once the direction is settled. On stockmanagement #85, a request that two settings warn \"the way parseUomPriority does\" was answered with one more static helper, while the ticket asked that no static settings utility remain."),
]
for old, new in edits:
    assert s.count(old) == 1, ("anchor not unique or missing", old[:80])
    s = s.replace(old, new)
open(p, "w").write(s)
print("applied", len(edits), "edits")
