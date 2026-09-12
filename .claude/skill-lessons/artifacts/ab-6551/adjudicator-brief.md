You are verifying review findings against real code. Two reviewers independently reviewed the same
pull request and produced findings. You do not know anything about how either review was produced,
and it does not matter: judge each finding only against the code.

## Inputs

    .../ab-6551/adjudication-input.json

Each entry has an opaque `key` (e.g. `F07`), plus `file`, `line`, `disposition`, `claim`,
`failure_mode`, `evidence`. The entries are shuffled; the order carries no information, and any
grouping you might infer from it is not real.

(full base path: /private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-querystore/9f0167e9-18df-4e14-bedd-eab4aa62b9c2/scratchpad/ab-6551/)

## The code

`openmrs/openmrs-core` PR #6551 at sha `b7cd909d245086fa10691496f88e5ec6971b77c2`; merge base
`28e3bab9e206170df7fd43cf709495b6d45fe16a`. The PR diff is exactly
`git diff 28e3bab9e206170df7fd43cf709495b6d45fe16a b7cd909d245086fa10691496f88e5ec6971b77c2`
(8 files, +216/-6). Ticket TRUNK-6669 is at
`https://openmrs.atlassian.net/rest/api/2/issue/TRUNK-6669?fields=summary,description,status,comment`

Your own worktree, already detached at the head sha:

    .../ab-6551/wt-adjudicator

Constraints: never run `mvn install`; the pom at this sha sets `maven.compiler.release=21`, so use
JDK 21 (the machine default) and prefer the narrowest test selection, e.g.
`mvn -o -pl api test -Dtest=UserServiceTest#someMethod`. Do not spawn subagents. Restore any mutation
with `git checkout -- <path>` and leave the worktree clean. Do not read the PR's GitHub review
conversation (`gh api .../pulls/6551/comments`, `--json reviews,comments`) — it would tell you what
other people concluded, and your job is to reach your own conclusion from the code.

## What to decide, per finding

Verify it. Run things. Where a claim says a test is vacuous, mutate the code it guards and see
whether the test fails. Where a claim says something breaks, reproduce the break. Where a claim
asserts an absence, prove the search pipeline finds a string you know is present before trusting a
zero hit.

Then classify:

- **REAL** — you reproduced it, or traced a mechanism you are confident in, and the stated failure
  mode holds as written.
- **REAL_OVERSTATED** — the underlying observation is true but the stated failure mode or
  disposition is wrong (e.g. called blocking when nothing breaks).
- **PLAUSIBLE_UNVERIFIED** — could be true; the experiment that would settle it did not run, or
  could not.
- **NOISE** — false, already handled in the code, or so trivial that acting on it would not change
  the author's work.

Also mark, for each finding, whether **the diff even contains what it describes** — a finding
anchored to code the PR does not touch, or to a line that does not say what the finding claims, is
`anchor_wrong: true` regardless of its class.

Finally, list **duplicate groups**: sets of keys that are the same underlying defect stated
differently. This matters and is easy to under-call — two findings about one root cause are one
defect. Be explicit that you looked.

## Output

Strict JSON (via `python3` + `json.dump`) to `.../ab-6551/adjudication.json`:

    {
      "verdicts": [
        {"key": "F07", "class": "REAL|REAL_OVERSTATED|PLAUSIBLE_UNVERIFIED|NOISE",
         "anchor_wrong": false,
         "what_i_ran": "the commands/mutations that settled it",
         "why": "one or two sentences",
         "correct_disposition": "blocking|suggestion|question|nit|drop"}
      ],
      "duplicate_groups": [["F03","F11"]],
      "most_serious_defect_in_this_pr": "in your own words, whether or not any finding names it",
      "notes": "..."
    }

Then report back in plain text: the class counts, the duplicate groups, and the JSON path.
