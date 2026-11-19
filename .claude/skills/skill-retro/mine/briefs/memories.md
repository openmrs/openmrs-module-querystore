You are checking project-memory files that past code-review sessions wrote, for lessons about reviewing pull requests
that never reached the review skill. Work read-only: read files and run read-only commands; create, edit or delete
nothing, anywhere. Your final reply is your output.

**Tools:** Read, Grep and Glob. The harness refuses shell commands that change anything, use variables or `cd`, or
call `gh`; read files with Read and search them with Grep instead.

The skill is `pr-review` (`SKILL.md` and `posting.md` here, version @@VERSION@@). A project memory loads only in the
project folder that wrote it, so a lesson kept there reaches sessions in that folder alone. `memories.txt` holds the
@@N_FILES@@ such files changed since the last run, each under a `##### FILE <folder>/<name>` header. Read every file, not a sample.

**For each file**, decide whether it carries a lesson about HOW to review or run the review machinery: a practice, a
check, a trap, a recipe, or a wording rule. Facts about one PR's code do not count.

**Report:**
1. Lessons not in the skill, grouped by theme. For each: the files that carry it, a verbatim quote of at most two lines,
   and how many files carry it. Mark each "not in the skill" or "partly". For "partly", quote the skill line it extends.
2. Memories that contradict the current skill. They keep steering sessions in their folder the wrong way. Quote both
   the memory and the skill line.
3. Recipes that recur across folders and belong in one shared file beside the skill: build, run, probe or environment
   how-tos. For each: the recipe in a sentence, and the files that carry it.
4. A one-line verdict per file: covered, partly, not in the skill, contradicts, or no review lesson.

**Rules:**
- Quote exactly.
- Never name a PR listed in `PRS-private.txt`, nor its ticket key. Write "a private PR".
- Use they/them for anyone whose pronouns are not stated.
