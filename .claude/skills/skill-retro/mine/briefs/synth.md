You are merging reader reports into one catalog. Work read-only: read files and run read-only commands; create, edit or
delete nothing, anywhere. Your final reply is your output.

**Tools:** Read, Grep and Glob. The harness refuses shell commands that change anything, use variables or `cd`, or
call `gh`; read files with Read and search them with Grep instead.

**Background.** Readers each read a different slice, by date, of the past code-review sessions in this run. Each wrote a report
of lessons for a review skill called `pr-review` (`SKILL.md` and `posting.md` here, version @@VERSION@@). Your folder holds
the reports for your share of the slices: `report-NN.md`.

**Merge them into one catalog.**
1. **Lessons.** Merge lessons that state the same rule, even when they are worded differently. For each merged lesson
   give:
   - the rule, in one sentence;
   - its kind;
   - the total instances, summed across reports;
   - every session id;
   - the two or three strongest verbatim quotes, each with its session id, copied exactly from the reports;
   - its coverage in the current skill: quote the line and say "covered", "covered, not followed" (with how many
     sessions), "partly" or "no";
   - the smallest change that would have prevented it.

   Keep single-instance lessons too, marked "1 instance". Do not merge two lessons only because they share a topic. If
   the fixes differ, they stay separate.
2. **Refuter tally.** Add up every report's refuter table into one: error type, total count, session ids. List the
   most common concrete patterns within each type.
3. **Time sinks.** Group by cause, with the summed minutes and the sessions.
4. **Practices that caught real defects.** Group by practice, with counts, sessions, and what each caught.
5. **Conflicts.** Note where two readers drew opposite lessons from similar evidence.

**Rules:**
- Do not invent. Every quote must appear verbatim in a report.
- Never name a PR listed in `PRS-private.txt`, nor its ticket key. Write "a private PR".
- Use they/them for anyone whose pronouns are not stated.
- Rank the lessons by total impact on future reviews, highest first. Be complete. Drop nothing that has evidence.
