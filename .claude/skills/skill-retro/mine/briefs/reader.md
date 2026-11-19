You are one of @@N_READERS@@ readers mining past code-review sessions for lessons. Work read-only: read files and run read-only
commands; create, edit or delete nothing, anywhere, /tmp included. Your final reply is your output.

**Tools:** Read, Grep and Glob. The harness refuses shell commands that change anything, use variables or `cd`, or
call `gh`; read files with Read and search them with Grep instead.

**Context.** The account owner reviews GitHub pull requests, mostly OpenMRS, with a Claude Code skill called `pr-review`.
Its current text is `SKILL.md` and `posting.md` in this folder (version @@VERSION@@). Reviews are now the
bottleneck of the owner's development cycle. We want every lesson that would make future reviews:
- more accurate: they catch what maintainers later catch, and make no false claims;
- faster: less wall-clock time and fewer rounds;
- less work for the owner and for PR authors.

**Your input** is `chunk.txt`, the record of @@SESSIONS_N@@ review sessions from @@DATE_RANGE@@, in date order.
- Each session starts with `=== SESSION <id> | folder | PR | start -> end | public/PRIVATE`.
- Lines are tagged:
  - `[HH:MM USER]`: what the owner typed;
  - `[HH:MM A]`: what the session said;
  - `[HH:MM AGENT-BRIEF]`: a subagent it started, usually a refuter or a second reviewer;
  - `[HH:MM AGENT-REPORT]`: that subagent's report, when it ran in the foreground, cut at 4,000 characters;
  - `[HH:MM TASK-DONE]`: a background agent or command finished. For an agent, this carries its report, cut at
    4,000 characters.
- Tool calls and their raw output are not included.
- Read the whole file, every session, not a sample. Use the Read tool with offset and limit until you reach the end.
  At the end, say how many sessions you read.

**What to extract: every instance of the following.**
1. An error a session made that was caught later. It may have been caught by:
   - a refuter or second agent (REFUTED, WEAKENED, "does not hold");
   - the session itself ("I was wrong", "correction");
   - the owner;
   - a maintainer quoted in the session.

   Say what kind of error it was, and what at draft time would have prevented it.
2. A defect or important point found by a practice worth making standard: a probe, a real-server run, a downstream
   build, a mutation, a second reviewer, or reading a specific source. Name the practice, and say whether the skill asks
   for it.
3. Wasted time or effort. Look for:
   - long gaps between timestamps;
   - repeated failing attempts;
   - fights with the environment (builds, ports, Maven, servers, permissions);
   - work redone because state was lost;
   - waiting;
   - over-verifying trivial things.

   Estimate the minutes from the timestamps, and say what change to the machinery would have avoided it.
4. Something the owner had to ask for, correct or repeat (their `[USER]` lines), and what the session should have done
   without being asked.
5. A rule in SKILL.md or posting.md that the session broke, or that it followed and that backfired. Cite the line.
6. Friction with PR authors or maintainers visible in the session: quoted pushback, confusion, or a comment that had to
   be explained or withdrawn.

**For each lesson, write:**
- **Lesson:** one sentence, as a rule a reviewer could follow.
- **Kind:** accuracy, speed, owner-friction, author-friction or process.
- **Evidence:** for each instance, the session id, date and PR (or "a private PR"), and a verbatim quote of at most two
  lines from the chunk.
- **Instances in this chunk:** a count.
- **Covered by the current skill?** Quote the SKILL.md or posting.md line that covers it, or write "no". If the line
  covers it but the session did not follow it, write "covered, not followed".
- **What would change:** the smallest rule or tool change that would have prevented it.

**Rules:**
- Report only what the chunk shows, and quote it exactly. Do not invent. Do not turn one instance into a claim about
  many.
- Never name a PR listed in `PRS-private.txt`, nor its ticket key or repository-plus-number. Write "a private PR"
  instead. Every session marked PRIVATE is such a PR, and other sessions may mention them too.
- Use they/them for anyone whose pronouns are not stated.
- Sort the lessons by how much they would improve future reviews, highest first.
- Give one entry per distinct lesson, with its instances merged. There is no length cap, but do not pad.

**Refuter tally.** For every AGENT-BRIEF that asks an agent to refute, break or challenge a draft, together with its
result, record:
- one line saying what the agent refuted or weakened (or "held");
- the error type, one of:
  - a wrong claim about framework or library behaviour;
  - a wrong claim about the PR's code path or reachability;
  - overstated or understated severity;
  - a suggested fix or test that was broken or untested;
  - a wrong line, anchor or citation;
  - a stale view of the PR (head, threads, state);
  - a misread ticket or description;
  - a probe that did not test what it claimed;
  - other, named.

Give the tally as a table: error type, count, session ids.

**Finish with two lists:**
- **Time sinks:** session, minutes, cause.
- **Practices that caught real defects:** session, practice, what it caught.
