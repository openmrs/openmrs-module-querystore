You are producing the final, decision-ready list of changes to the TEXT of a code-review skill, from all the evidence
this mining run gathered. Work read-only: read files and run read-only commands; create, edit or delete nothing, anywhere, /tmp
included. Your final reply is your output.

**Tools:** Read, Grep and Glob. The harness refuses shell commands that change anything, use variables or `cd`, or
call `gh`; read files with Read and search them with Grep instead.

The owner reviews GitHub pull requests, mostly OpenMRS, with a Claude Code skill called `pr-review` (`SKILL.md` and
`posting.md` here, version @@VERSION@@). Reviews are now the bottleneck of the owner's development cycle. They asked for
every lesson the past sessions teach, to make future reviews more accurate and faster. SKILL.md is also the system
prompt of a GitHub Action that runs `@claude review` in CI: it fetches SKILL.md only (not posting.md), with only Bash,
Read, Grep, Glob and an inline-comment tool, no subagents, no user, and it must not run the PR's code.

**Inputs:**
- `catalog-*.md`: merged lessons, refuter tallies and reader conflicts. Readers read the @@SESSIONS@@ review
  sessions of @@WINDOW@@; each catalog covers a date range.
- `auditor.md`: internal defects in the current text, with proposed edits.
- `outcomes-*.md`: what other reviewers raised after the owner's reviews, judged against what the owner had posted.
- `github.md`: thread lengths, reactions, and PRs closed unmerged after the owner's comments.
- `parked.txt`: parked lessons with their REOPEN ON. `killed.md`: proposals killed or declined earlier, with reasons. A
  shape they rejected comes back only with new evidence, which you name.
- `runrecords/`: miss records sessions wrote during the window.
- `measurements.md`: time to first report, context fill, compactions, refuter verdicts.
- `pr-review-history.txt`: every version of the skill, with its date. `pr-review-history-full.txt`: every change to
  the skill, oldest first. To date a rule, Grep that file for the exact phrase on lines starting with `+`; the nearest
  `commit <date> <sha>` line above the first hit is when it appeared.

**Produce:**
1. **A ranked list of skill-text changes** (SKILL.md and posting.md). For each, give:
   - **The change:** exact wording, old → new, short.
   - **Evidence:** total instances across the catalogs, number of sessions, date range, and the two strongest verbatim
     quotes with session ids.
   - **Coverage today:** the line that covers it. If the line covers it but sessions did not follow it, date the rule.
     An instance before the rule existed is not non-compliance; count those apart.
   - **Prior decisions:** what parked.txt or killed.md says, and whether the evidence is new.
   - **Recommendation:** apply; park, with a REOPEN ON; reject, with the reason; or owner's decision, with why it is
     theirs.
   - **Effect:** accuracy, speed or friction, and what it costs in words.
2. **Parked items whose REOPEN ON the evidence now meets.**
3. **The auditor's edits:** which to apply as written, which to change, and which to drop, with reasons.
4. **Coherence:** check that the applied changes, taken together, contradict neither each other nor any remaining
   line, and that each one behaves correctly in CI. Name every conflict you find and resolve it.
5. **Net size:** the words added and removed, and the prunes that offset the additions.

**Rules:**
- Quote verbatim. Do not invent.
- Never name a PR in `PRS-private.txt`, nor its ticket key or advisory id. Write "a private PR".
- Use they/them for anyone whose pronouns are not stated.
- Prefer few, sharp rules. Merge lessons whose fix is the same rule.
- Where readers conflicted, weigh the evidence and say which way it points.
