You are judging what other reviewers raised on pull requests after a reviewer called "@@LOGIN@@" had reviewed them.
"@@LOGIN@@" is an AI-assisted reviewer posting under that account. Work read-only: create, edit or delete nothing, and
post nothing. Everything you need is in `batch.json`.
Your final reply is your output.

**Tools:** Read, Grep and Glob. The harness refuses shell commands that change anything, use variables or `cd`, or
call `gh`; read files with Read and search them with Grep instead.

**Input:** `batch.json` in this folder, a list of PRs. Each has:
- `our_posted_comments`: everything @@LOGIN@@ posted, meaning inline comments, thread replies, review bodies and
  conversation comments, with timestamps;
- `their_comments_after_our_first_review`: top-level inline comments and changes-requested reviews by other human
  reviewers after @@LOGIN@@'s first review session, each with its thread replies.

**For every item** in `their_comments_after_our_first_review`, pick one category. Compare it only against what
@@LOGIN@@ posted BEFORE that item's timestamp.
- **MISS-DEFECT:** it raises a real defect or a change the PR needed, and nothing @@LOGIN@@ posted before it raised that.
  Examples: wrong behaviour, a bug, a missing guard, a broken contract, a leak, a security issue.
- **MISS-QUALITY:** not a defect, but an improvement that the author took or agreed to, and that nothing @@LOGIN@@
  posted raised. Examples: naming, a code comment or doc that is wrong or unneeded, an unnecessary line, simpler code, a
  better test.
- **ALREADY-OURS:** @@LOGIN@@ had already raised the same point. Name the comment.
- **DECLINED:** the author or others disagreed, and it was not taken.
- **NOT-A-FINDING:** a question with no defect behind it, a process or scope remark, an approval, thanks, or a request
  about the ticket.

Use the thread replies and the PR's state as evidence of whether a point was taken. Where you cannot tell, say
"unclear" and why.

For each MISS-DEFECT and MISS-QUALITY, also give a short class label for the kind of thing missed. Examples: null
safety, resource cleanup, wrong or unneeded code comment, dead or no-op code, exception handling breadth, API or naming
design, test design, backport or compatibility, scope of the change, performance, concurrency, configuration. Reuse
labels so they can be counted.

@@LOGIN@@ sometimes found things and reported them privately without posting. A MISS here means "not in anything
@@LOGIN@@ posted before it", which is still the failure that matters.

**Output:**
1. Per PR, one line per item: PR number, reviewer, file:line, category, class label (for misses), and an 8–15 word
   gist.
2. Totals by category, and totals by class label for the misses.
3. The three most telling misses, each in two sentences, quoting the reviewer.

Never name a PR listed in `PRS-private.txt`, nor its ticket key; write "a private PR". Use they/them for anyone whose
pronouns are not stated. Keep it under 1,500 words.
