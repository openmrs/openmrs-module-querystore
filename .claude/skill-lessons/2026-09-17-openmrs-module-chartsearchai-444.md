# resolve-ticket + harden + pr-harden · openmrs-module-chartsearchai · #444 / #449 / PR 460 · 2026-09-17
outcome: converged
rounds: 8 (pr-harden, cap raised 4->6->7->8)   cycles: 5 (harden)   verifier: ran (works at runtime, classification repaired)
context: compacted twice · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-444/a2a11367-3773-4e1c-8900-808d4782b6ee.jsonl

## The one structural lesson (nine rounds bought this)
A security property expressed as a SOURCE-SHAPE guard ("the fetch is written above the wiring", "the
refusal is spelled at the call site") was defeated by a fresh reviewer nine times, each time by a
different, entirely plausible restructure. What ended it was not a tighter regex; it was changing the
KIND of question twice:
  1. the refusal moved INTO the library as `fetch_or_exit`, which exits -- so there is no branch left
     at the call site for a restructure to get wrong;
  2. the ordering invariant moved into the CODE as a verified-ledger (`require_verified` answers from
     what verified in THIS shell), so source position stopped being load-bearing at all.
Only then did source guards become a cheap second channel rather than the whole defence. The general
form: if a reviewer can defeat a guard by rewriting source that behaves identically, the guard is
asking a question the property does not actually depend on.

## Refuted by measurement
- "a non-LFS file has no `x-linked-etag`" (written into the ADR's recipe for moving a pin) -> it does,
  and it is a git blob **sha1**: 40 hex, not 64. Silently taking it as a sha256 would have recorded a
  digest that can never match. Corrected with the measured value. · cost: 1 harden cycle
- "1.69 GiB/s with the ARMv8 SHA extension vs 0.364 without" -> that was two different TOOLS, not two
  ISA levels; the coreutils row had never been measured on coreutils at all. Relabelled as a per-tool
  table with what each row is a rate OF. · cost: 1 cycle
- "the fifth vocab row is a second copy worth pinning separately" -> `cmp` proved the two accounts serve
  byte-identical vocab.txt, so the row collapsed and a third third-party account left the release
  pipeline. A measurement that REMOVED surface. · cost: 0
- `if _mm_verify_file ...; then return 0; fi; return $?` -> POSIX: a false `if` with no `else` exits 0,
  so the library returned SUCCESS on every digest mismatch. Found by driving the real shell, not by
  reading. · cost: 1 cycle
- "the read-back of the global property is the safety net" -> `gp_set_if_blank` leaves an earlier
  start's row standing, so on every deployment past its first good start the property is non-blank
  whatever this start did, and the read-back could never fire. The gate had to move to the ledger.
- the verifier: "the new config.xml description is live" was FALSE on first boot -- core only writes a
  module description when the row is absent or NULL, and skips the version-change setup path entirely
  when the version has not moved. Standard core behaviour, not this PR's, but it means the shipped
  description does not reach an in-place upgrade at an unchanged SNAPSHOT version. Now in the PR body.

## Raised by a fresh agent, missed by the author
Nine distinct guard defeats, each blocking, each fixed AND mutation-proven:
- `file_sha256`'s definition matched the guard's own pattern before any `mv` existed
- the prefix test could not fail given the committed row order (vacuous)
- `contains("exit")` matched inside an `echo`
- a glob arm `[1-9])` was unrecognised by the arm parser
- an assignment `_stamp=$(date)` was misread as an arm opener
- `0|2)` folded the refusal into the success arm
- a trailing ` &` backgrounded the refusal; separately, a pipeline did the same
- a function wrap moved the fetch below the wiring with the source guard still green
- a variable-alias GP write bypassed the literal-name guard
- the headline on-every-start property was pinned by NOTHING until round 7 gave it two channels
Non-blocking residue from round 8 -> filed as issue #463 rather than edited into a cleared sha
(chiefly: `model-manifest.tsv` is not packaged in the omod, so a hand-downloader following the new
config.xml description has no manifest to check against on an ordinary module install).

## Where a skill blocked or contradicted this run
- pr-harden's round cap: raised 4->6->7->8. The licensing signal held each time -- a genuinely
  DIFFERENT defect per round, never a re-raise -- which is exactly the test the skill states, so this
  was a cap raise and not an override. Worth noting that nine rounds was the honest number here: the
  defect class (source guards vs plausible restructures) regenerates until the KIND of question changes.
- Committed `CLAUDE.md` at 25,069 bytes with `ProjectInstructionsGuardTest` red, because a helper
  script aborted before writing and I committed without reading the build output. Lesson: the guard's
  own rule (never raise a budget in the commit that overflowed it) is only enforceable if you read the
  build. Fixed by trimming seven bullets; final 24,999/25,000.
- Round 1's reviewer died on a 429 and left the worktree DETACHED because I had not passed
  `isolation: "worktree"`. Every later round used it. An agent that dies mid-`git checkout` is not a
  hypothetical.
- GitHub's `pull/<n>/head` ref LAGS a push -- round 2's first fetch returned round 1's sha, and
  `gh pr view --json headRefOid` was cached too. Every subsequent round used a delete-ref + re-fetch
  retry loop. A reviewer silently reviewing the previous round is the worst possible failure here.

## Declined
- [r1-6] container-level end state (image COPY destinations, compose stop-and-stay-stopped, querystore
  GPs still blank) is out of reach of any test in this repo. The finding requested no code change and
  asked to be carried in the report; round 6 nevertheless converted half of it into an assertion
  (`theBackendServiceDeclaresNoRestartPolicyThatWouldLoopThroughARefusal` walks this repo's
  docker-compose.yml). The residue that stays: the DEPLOY server's compose file is not this one.

## Residues named rather than claimed away
- a `fetch_or_exit` wrapped in a backgrounded function still swallows its exit; the ledger gate is what
  covers that start, not the exit
- nesting depth 0 is not reachability
- nothing in the suite checks a digest against the Hub, and nothing can without downloading gigabytes
  in CI; the first release build after a pin move is that check
