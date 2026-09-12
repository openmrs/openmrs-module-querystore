Six reviewers have each reviewed one dimension of the same open-source pull request and written a
JSON report. Your job is to merge those six reports into the single review a careful human reviewer
would actually post. Your output goes to a machine — NOT to GitHub. Post nothing, stage nothing.

## Inputs

Read all six, and nothing else about the PR's review history:

    .../ab-6551/lens-solution-fit.json
    .../ab-6551/lens-correctness.json
    .../ab-6551/lens-test-coverage.json
    .../ab-6551/lens-security.json
    .../ab-6551/lens-performance.json
    .../ab-6551/lens-conventions.json

(full base path: /private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-querystore/9f0167e9-18df-4e14-bedd-eab4aa62b9c2/scratchpad/ab-6551/)

The PR is `openmrs/openmrs-core` #6551 at sha `b7cd909d245086fa10691496f88e5ec6971b77c2`, merge base
`28e3bab9e206170df7fd43cf709495b6d45fe16a`. A worktree at that sha is at `.../ab-6551/wt-control`
— read-only for you. Do **not** call `gh api .../pulls/6551/comments` and do not fetch
`--json reviews,comments`.

## The rule that defines your job

Read `/Users/danielkayiwa/.claude/skills/pr-review/SKILL.md` and apply **Step 4's** discipline to the
merged set. The parts that bind you hardest:

- Every finding must either request an action or ask a question. A blocking finding must carry a
  concrete failure-mode sentence ("if merged as-is, X breaks because Y"); if that sentence cannot be
  written, downgrade or drop it.
- Unverified suspicion is a `question`, never a blocking `issue`.
- **Each finding lives in exactly one place.** Where two or more lenses found the same thing from
  different angles, merge them into one finding and say so in `merged_from`; do not emit both.
- **Fewer, sharper beats exhaustive.** Reviewer and author attention is the scarce resource. A
  finding that would not change what the author does is pure cost — drop it.
- Don't let severity labels substitute for analysis: re-derive each disposition from the written
  failure mode, not from the lens's own label. Lenses routinely under-label correctness defects as
  minor and over-label style as blocking.
- A cleared concern earns silence. Verified-clean material is not a finding.

## The hard limit on your authority

You may **drop, merge, downgrade, upgrade, re-anchor (file/line), and rewrite the wording** of what
the lenses reported. You may read the worktree to adjudicate a disagreement between two lenses, or
to check an anchor.

You may **NOT generate a finding of your own.** Every finding you emit must trace to at least one
lens finding, named in `merged_from`. If you notice something no lens reported, put it in
`merger_noticed_but_did_not_add` — it is recorded there and excluded from the review. This limit is
absolute: a merger that adds findings is a seventh reviewer, which would make this merge
uninterpretable.

## Output

Write strict JSON (via `python3` + `json.dump`) to
`.../ab-6551/arm-B-merged.json`:

    {
      "findings": [
        {"id": "B1",
         "dimension": "which dimension it came from (or several)",
         "merged_from": ["solution-fit-2", "correctness-1"],
         "file": "...", "line": 123,
         "disposition": "blocking|suggestion|question|nit",
         "claim": "...", "failure_mode": "...", "evidence": "...",
         "confidence": "high|medium|low"}
      ],
      "dropped": [{"id": "conventions-3", "why": "..."}],
      "downgraded": [{"id": "security-1", "from": "blocking", "to": "question", "why": "..."}],
      "merged_pairs": [["solution-fit-2", "correctness-1"]],
      "merger_noticed_but_did_not_add": ["..."],
      "verdict": "one sentence: merge | merge after X | needs work | direction needs agreeing first",
      "notes": "how the six reports related to each other — overlaps, contradictions, and which lenses were quiet"
    }

Then report back in plain text: input findings total, output findings total, how many you dropped and
merged, your one-sentence verdict, and the JSON path. Do not paste the whole JSON.
