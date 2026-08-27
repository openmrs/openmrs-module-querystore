# defect · a retro's own hook change shipped a regression its refuter never saw · pipeline · 2026-08-27
outcome: found by a fresh adversarial review 27 minutes after push (`f7cf3a7` 14:05:14 -> `3bf4073`
         14:32:34); fixed in `3bf4073`, which corrected THREE skills (harden, pr-harden, skill-retro)
         and three hook files plus the hooks README. Nothing reverted. A second round on `3bf4073`
         itself found more — see the addendum at the end.
evidence: durable — `f7cf3a7` (the defect), `3bf4073` (the fix), both in openmrs-module-querystore.

## What happened

While answering "which parts of the skills should be hooks?", the session fixed a live-observed defect
in the two Stop gates (`2026-08-26-pwd-keyed-gate-false-positive.md`): state keyed on `$PWD`, so an
interactive session in a pool-worked checkout is blocked by another live session's entry.

The fix established who owned the unattended MARKER and allowed the stop when that owner was not this
session. But the ambiguous thing is the ENTRY, and the marker is a different file answering a different
question. Before the change a live marker affected ONE branch (it turned the attended `awaiting` allow
into a block); after it, a live foreign marker allowed every block path — `edits: 7`, `phase: fixing`,
`phase: building`, `reviewed+blocking>0`. An interactive `/harden` in a checkout the pool was working
therefore lost its own termination contract, silently, for as long as the pool run lived.

The two tests shipped with it asserted that as correct behaviour.

The lesson file being acted on had already named the right remedy — *"writing the owning pid and session
id into it [the entry], and having the gate allow when the reader is not the owner"*. It was not
followed, and no reason was recorded for declining it.

## Why the pipeline did not catch it

`skill-retro` Step 5 refutes **proposals**, and it did its job: it killed one, revised two and promoted
one. But this change was not a proposal — it was code the retro wrote afterwards, in a hook, in the same
session, and Step 5 had already run. Nothing in the skill asks for a fresh review of a code change the
retro itself makes, so a hook edit reaches `settings.json` on the author's own say-so. Every other code
path in this pipeline is reviewed by an agent that did not write it; this one is not.

The four tests written with the change all passed and all measured the wrong property — the same shape
`harden` 0.19.0 had added a rule about earlier that day, one commit back.

## Also found in the same review, all in the retro's own output

- `pr-harden/gate-test.sh`'s new header was a verbatim copy of harden's, quoting harden's hook and
  harden's numbers (8/3 where its own measurement is 8/4).
- Both gate headers glossed "ownership is established positively" with a definition of foreignness.
- `git-restore-backup.sh`'s refreshed count asserted the ledger said 5/4 while the ledger said 9/7 —
  the stale-count defect reproduced one line below its own correction.
- `hooks/README.md` claimed the backup hook has "no decision to assert"; it decides destructiveness, and
  that decision misses `git checkout HEAD <path>`, `git -C <dir>` and `git -c k=v`.
- Vendoring the hooks created a fourth copy of each gate with no sync obligation in Step 6, and
  `git-restore-backup.sh` had none at all.

## Candidate rules, NOT proposed here

The author of a defect is the wrong person to write its rule, and this record exists so a later retro can
weigh these against other evidence:

1. A retro that changes a **hook or gate script** has that change reviewed by a fresh agent before it is
   pushed, the way `pr-harden` reviews a round. Cost: one agent per hook change.
2. A guard added by a retro states which property it pins and is mutated against the property, not the
   mechanism. Four tests here passed while pinning the wrong thing.
3. When a lesson file names a candidate remedy and the fix takes a different one, the reason is recorded.
   Both defects above follow from declining the entry-stamp silently.

Count so far: **1 record.**

## Addendum, same day — round 2 on the fix, and a concurrent session

A second fresh review of `3bf4073` was not approvable either. What it found, all reproduced:

- The MARKER name was still built from logical `$PWD` while its writer
  (`pool-run.unattended_marker_path` -> `tenant_key` -> `realpath`) builds it from the physical path. The
  round-1 fix had resolved the state KEY three lines above and left this one — under a comment in the
  same file saying "resolve on both sides or neither". Demonstrated block->allow through a symlinked
  cwd: entry found, marker missed, unattended awaiting-yield allowed. Both suites were structurally
  blind to it, because the fixture builds its temp dir with `pwd -P`.
- `owns_this_session` compared pids as STRINGS, and `kill -0 0` succeeds — so `"owner": 0` read as a
  live non-ancestor and disarmed every block path, while a zero-padded own pid read as somebody else's.
- Both harness headers carried counts that the same commit invalidated by adding two cases each: the
  defect that commit had just corrected one file away.
- Four cases pinned something other than their name; the two `kill -0` liveness rungs were unpinned in
  both suites, and pr-harden had no case for the marker refinement its own commit message headlined.
- The dead-owner allow was justified by two claims that are both false on that branch: `claude --resume`
  is a new pid on the same conversation and CAN advance the run, and the "two damaging remedies" both
  need a LIVE foreign owner.
- An exhaustive-by-contrast field list omitted three fields the gate reads, one of which (`mode`) has no
  writer at all, so a `--plan-only` run gets the generic message telling it to do the work its mode
  excludes.

**And the run was racing another session in the same checkout.** A second interactive session (pid
64395, started 14:13) was doing overlapping work on the same files and mirroring `~/.claude` into the
same repo. `3bf4073` therefore ABSORBED that session's `KEY="$PWD"` -> `KEY="$(pwd -P)"` edit, picked up
by a `cp ~/.claude/hooks/*.sh`, and described it in this session's own voice; the same commit's sentence
"the state is still keyed on `$PWD`" was false the moment it was written, because the line had changed
underneath. The `cmp` checks passed because they compare live against repo AFTER the copy, which cannot
see that the live file moved. That is the "commit before anything mutates the tree" rule one level up —
it binds AGENTS, and nothing in these skills covers two SESSIONS on one checkout. The other session's
work landed independently as `ddf84d4`.

Candidate rule 4, not proposed: before mirroring `~/.claude` into the repo, check whether another live
`claude` process has written those files since you read them — the same question `owns_this_session`
asks, one directory up. Count so far: **1 record.**
