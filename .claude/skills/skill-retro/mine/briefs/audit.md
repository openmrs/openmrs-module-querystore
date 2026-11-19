You are auditing a code-review skill for internal defects. Work read-only: read files and run read-only commands;
create, edit or delete nothing, anywhere. Your final reply is your output.

**Tools:** Read, Grep and Glob. The harness refuses shell commands that change anything, use variables or `cd`, or
call `gh`; read files with Read and search them with Grep instead.

The skill is `pr-review`: `SKILL.md` (version @@VERSION@@), @@SKILL_WORDS@@ words, and its companion `posting.md`, @@POSTING_WORDS@@ words. The
standard for pruning is `skill-retro.SKILL.md`, Step 4. SKILL.md is also the system prompt of a GitHub Action that runs
in CI with only Bash, Read, Grep, Glob and an inline-comment tool. That Action fetches SKILL.md alone, not posting.md,
and must not run the PR's code.

**Find, with line numbers and verbatim quotes:**
1. Contradictions: two rules that tell a reviewer to do incompatible things in the same situation.
2. Duplication: the same rule stated in two or more places. List each place and say which one to keep.
3. Stale text: examples, cross-references or line references that no longer match the current rules or file layout.
4. Rules a reviewer cannot act on: vague, or so long that the instruction is buried.
5. Rules that misfire in CI, given the Action's constraints above.

**For each finding**, propose the exact edit (old text, then new text) and the words it saves or costs. Do not propose
new rules or new checks. Consolidating and clarifying only. Rank the findings by how much each would improve a
reviewer's compliance.

Never name a PR listed in `PRS-private.txt`.
Use they/them for anyone whose pronouns are not stated.
