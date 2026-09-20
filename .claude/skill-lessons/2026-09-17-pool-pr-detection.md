# defect · ticket-pool driver — a merged PR reads as "the run opened no PR" · 2026-09-17
outcome: root cause verified; patch and tests **APPLIED 2026-09-20** in the retro of that window
         (`bca30ab`, `ticket-pool` 0.24.0). Written here as prepared-and-not-applied, because a pool
         run was executing the file at the time (pid 79582, #444 and #445 still in flight).
found by: asking what makes a run need a second attempt, which the 2026-09-17 wall-clock measurement
          handed on as its open question. Attempts are the largest latency multiplier in the ledger
          (#337 `att=3` -> 11.0 h, #409 `att=2` -> 9.4 h against 7.4 h in-session).

## The mechanism, one line of it

`open_prs()` asks `gh pr list --state open`, and `work_ticket` decides the outcome from that list
alone. **A PR merged before the outcome check is not an open PR**, so the run that opened it is
recorded `no-pr`.

## The evidence

Every `no-pr` row from 2026-09-13/14 had opened a PR that was merged before the driver looked. PR
times from `gh api repos/openmrs/openmrs-module-chartsearchai/pulls/<n>`, ledger times from
`~/.claude/pipeline/ledger.json`:

| ticket | PR | branch | merged (UTC) | ledger `last_run` |
|---|---|---|---|---|
| #413 | 419 | `fix/413-unreadable-order-drug` | 09-13 20:19:15 | 09-13 20:20 |
| #421 | 423 | `fix/421-guards-that-do-not-discriminate` | 09-14 08:48:13 | 09-14 09:34 |
| #337 | 422 | `fix/337-ascii-elision-glyph` | 09-14 08:49:40 | 09-14 09:34 |
| #276 | 424 | `feat/276-unstated-dosing-ceiling` | 09-14 09:20:13 | 09-14 09:34 |
| #412 | 427 | `fix/412-uncorroborated-chip-allergy-population` | 09-14 13:52:38 | 09-14 13:52 |
| #425 | 428 | `fix/425-dosing-ceiling-needle-residues` | 09-14 16:38:49 | 09-14 19:00 |
| #409 | 426 | `fix/409-finding-severity-marker-anchored` | 09-14 17:21:16 | 09-14 19:00 |
| #315 | 431 | `feat/315-ended-order-stop-date` | 09-14 18:44:27 | 09-14 19:01 |

Each ticket's own record reads `outcome: converged` and names that PR. And the driver had the answer
in its hand while it wrote the wrong one — `pool-20260914T093719Z.md`:9-11, #315:

> recorded in the ledger as no-pr
>     cleared leftover gate state — pr-harden-state.json: phase=reviewed blocking=0 edits=None **pr=431** round=1

## Three consequences, and one more failure mode riding on the same cause

- **The attempt budget is charged for delivered work.** `max_attempts: 2`, so two false negatives put
  a ticket past the queue's own gate at `consider`.
- **`DONE = {"ready"}`**, so a ticket whose PR is merged stays re-queueable — and the queue's
  "already has an open PR" guard reads the same open-only list, so nothing there sees the merged PR
  either. That is the path on which one issue gets a second PR.
- **Every outcome the pool reports is wrong for those rows**, including the "outcomes of earlier
  tickets" block a later run prints.
- **A `ready` verdict taken from another ticket's PR.** With the real PR invisible, matching falls
  through to `pr_for_ticket`'s prose tier, which matched a bare `#294` in PR 417's body — "`main`
  gained #416 (issue #294) after this branch was reviewed". #294's ledger row therefore says
  `status=ready pr=417`, and 417 is #409's PR (`fix/409-finding-citation-extent-prose-anchored`).
  #294's own PR, 416, had been merged 09-13 before the check, which is the first defect again.

**Not established, and it should not be assumed:** that any second attempt re-did work an earlier one
had delivered. #337 reached `att=3` past a `max_attempts` of 2, which only an operator naming the
ticket can do, so the re-runs on this record were driven by a human and not by the false status. What
is measured is the wrong record, the charged attempt, the lockout and the duplicate-PR path.

**Adjacent, same file:** `load_json_str(got.stdout, [])` turns a failed `gh` call into "this
repository has no open PRs". That is the `gh` rule in `.claude/CLAUDE.md` ("treat empty output from a
`gh` subcommand as a failure") being broken by the driver that depends on it.

## The fix, prepared and tested

`open_prs` returns `None` when the ask itself failed (non-zero exit, or exit 0 with empty stdout);
new `pr_by_number` reads one PR in any state; new `outcome_pr` takes three sources in order — the
open list, then the number the run wrote into the gate entry, then nothing — and reports whether
GitHub could be asked at all. The ladder gains `unknown` for an unanswerable ask, which joins
`NEEDS_HUMAN` so a maybe-delivered ticket waits for a human rather than risking a second PR; the
queue skips a ticket it could not ask about; `pr_for_ticket`'s prose tier now requires
`Fixes|Closes|Resolves|Refs` beside the number, in either the bare or the linked form, instead of any
mention of it.

84 changed lines over 9 hunks. **16 focused cases pass** over the real functions with `sh` — the
`gh` process boundary — substituted, and two mutants prove they discriminate: reverting the prose
tier to the bare number reddens 2, deleting the gate fallback reddens 2.

**Applied 2026-09-20**, once no pool run held the file. The driver went over
`~/.claude/pipeline/pool-run` and the repo's `.claude/pipeline/pool-run` (the same file, vendored);
the 16 cases were integrated INTO `pool-test.py` as `test_pr_detection` rather than landed beside it,
since a test file nothing runs is not a test — `pool.sh` is restored in a `finally` and the stand-in
returns a real `subprocess.CompletedProcess`. Suite: **523 passed / 0 failed** (507 before). The cases
were re-calibrated after integration: against the pre-patch driver they fail 2 and then raise
`TypeError: 'NoneType' object is not iterable`. Shipped as `ticket-pool` 0.24.0 with the `unknown` row
and the correction to "everything else is retried", which the code made false. The prepared originals
stay in `~/.claude/pipeline/.pending-2026-09-17/` as the record of what was applied; the live test is
the one in `pool-test.py`.

**Not repaired by the patch, and still true: the nine wrong rows are in the ledger.** Checked
2026-09-20 — the eight `no-pr` rows still read `pr: None` with their attempts charged (#337 at 3, past
`max_attempts`), and #294 still reads `ready pr=417`. None of the nine carries the `claude-pipeline`
label today, so none is queueable and the duplicate-PR path is not live; five (#337, #276, #409, #425,
#315) are still OPEN issues, so a re-label would make it live again. Repairing them is a hand edit of
live pipeline state and belongs to an operator.

## Also fixed, and it is why this was findable

`~/.claude/bin/run-timing.py` reported `IDLE waiting on a subagent 0 min` and `?` for every latency
on the current harness, and inflated output tokens ~2.1x by summing `usage` per transcript event.
Both are corrected in place (original kept as `.bak-20260917`), and its figures now reproduce two
independently written scripts on #446. It was not vendored in this repo; two measurement passes had
leaned on it and one published wrong numbers from it, which was the argument for vendoring it at
`.claude/bin/`. **Vendored there 2026-09-20** (`bca30ab`), with its `.bak-20260917`.
