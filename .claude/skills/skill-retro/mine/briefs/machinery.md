You are proposing changes to the MACHINERY around a code-review skill: scripts, hooks, a local recipes file, and
corrections to project memories. Work read-only: read files and run read-only commands; create, edit or delete
nothing, anywhere, /tmp included. Your final reply is your output.

**Tools:** Read, Grep and Glob. The harness refuses shell commands that change anything, use variables or `cd`, or
call `gh`; read files with Read and search them with Grep instead.

The owner reviews GitHub pull requests, mostly OpenMRS, with a Claude Code skill called `pr-review` (`SKILL.md` and
`posting.md` here, version @@VERSION@@). Reviews are the bottleneck of the owner's development cycle; `measurements.md` says
where review time goes in this run. They want reviews faster and more accurate.

**Inputs:**
- `catalog-*-timesinks-practices.md`: time sinks and practices that caught real defects, merged from the readers of the
  @@SESSIONS@@ review sessions of @@WINDOW@@.
- `memreader-*.md`: lessons, contradictions and recipes found in the @@N_MEM@@ review-related project-memory files
  changed during the window. A
  project memory loads only in the folder that wrote it, so its recipe is relearned elsewhere.
- `parked.txt`, `killed.md`: earlier decisions. A shape they rejected returns only with new evidence, which you name.
- `measurements.md`, `runrecords/`, `SKILL.md`, `posting.md`.

**Produce:**
1. **Speed changes, ranked by minutes saved per review.** For each:
   - the time sink, with its summed minutes and sessions;
   - the change: a script, a hook, a recipe, or one sentence of skill text (exact wording);
   - why it is safe for accuracy;
   - the evidence.

   Consider at least: refuter waits and timing, environment setup (Maven, Docker, servers, databases), rebuilding state
   lost between rounds, duplicated sessions, usage-limit stalls, unchanged-head rounds, and compactions.
2. **The local recipes file**, `~/.claude/pr-review-recipes.md`, copied here as `recipes.md`, which every session
   reads when a step needs it. Read it, then give additions and corrections as exact text:
   - build, server, database, CI, JIRA, probe and environment how-tos, each short and with its gotchas;
   - each naming its source memory files or sessions;
   - only what the evidence measured, not a guess.
3. **Hooks or scripts worth adding:** for example a guard on `~/.m2` installs, a refuter time box. For
   each: what it enforces, the evidence that prose alone failed, and a sketch of the mechanism.
4. **Memory corrections.** For each memory that contradicts an evidence-backed rule or carries stale facts: the file,
   the exact text to change, and the new text.

**Rules:**
- Quote verbatim. Do not invent.
- Never name a PR in `PRS-private.txt`, nor its ticket key or advisory id. Write "a private PR".
- Use they/them for anyone whose pronouns are not stated.
