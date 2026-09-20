# resolve-ticket + pr-harden · openmrs-module-chartsearchai · #445 / PR 461 · 2026-09-17
outcome: converged
rounds: 2 (pr-harden)   cycles: 12 (harden phase 2)   verifier: ran (works at runtime)
context: compacted once, mid-harden-phase-2 · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-445/058432d2-0f52-4893-8673-4b46ce20200b.jsonl

## Refuted by measurement
- "The 0.138 s bind window is one in which the START FAILS, not one in which a foreign listener is served"
  (written into the PR body, the ADR, two javadocs and the nested instruction file) -> the liveness re-check
  ran at the FIRST healthy /health reply, ~3 ms after pb.start(), i.e. inside the child's PRE-BIND window,
  ~195 ms before the child could fail its bind. An impostor serving {"status":"ok"}, 401 unauthenticated and
  200 to any bearer passed all three legs and was handed the prompt and the chart. The ADR's own weaker
  version ("unless the re-check happens to run inside those 60 ms") put the two windows in the wrong order.
  Reproduced live afterwards on the real standalone. · cost: 1 pr-harden round
- "the guard's `\s*` tolerates whatever a formatter does to the whitespace" -> scanForPattern fed the pattern
  ONE LINE AT A TIME, so the one whitespace character that matters, the newline, was the one it could not see.
  A second HttpClient with its receiver on its own line, used for the production /health request, passed the
  full api suite. · cost: 2 harden cycles
- "the three #445 source rules are unconditional" -> they read api/src only; a default-proxy client in the
  omod REST controller, where the chart already passes through, left the full root build green. · cost: same
- "the `openConnection` alternative alone carries no lookbehind" -> written in the same commit that added
  `openStream`, which has the identical property; `p.openStream()` on a non-URL was reported. A correction
  falsified by its own commit. · cost: 1 harden cycle
- "the budget entry is now derived rather than asserted — read the file's size and add a tenth" -> 3,884
  bytes plus a tenth is 4,272 and the entry said 3,900. The second draft repeated the first draft's
  arithmetic while announcing the correction. · cost: 1 pr-harden round
- ADR row 12b's "needs no property at all, ProxySelector.setDefault installed in-process" -> true but
  order-dependent: a client built with no .proxy() captures the default selector at CONSTRUCTION (measured
  1/0/0 across install-before-build, install-after-build, no-proxy control, JDK 21.0.6). · cost: 1 cycle
- README's "a listener that accepts nothing is not detected" -> those are the words the CAUGHT case owns;
  and the shape it was reaching for (a socket bound but never listened) is ALSO caught on macOS 14, the SYN
  being dropped rather than reset. · cost: 0 (same round)

## Raised by a fresh agent, missed by the author
- [pr-harden r1] the pre-bind race, above · blocking · cost: 1 round
- [pr-harden r1] nothing asserted the bearer on the WIRE is the secret handed to the CHILD — the two halves
  were each verified against production, so `"Bearer " + API_KEY_ENV` left 2,460 tests green while the
  shipped module would present a credential the child does not hold, refusing every start on the default
  engine · blocking · cost: same round
- [pr-harden r1] the budget-comment arithmetic · blocking · cost: same round
- [harden p2 pass 9] scanForPattern's newline blindness and its api-only scope · cost: 1 cycle each
- [harden p2 pass 10] a static import and the legacy HttpURLConnection stack defeat all three receiver-
  anchored rules; the endpoint class was then exempted from the new dialect rule it most needed · cost: 2
- [pr-harden r2, non-blocking] the `launchedAtNanos` argument production passes is observed by no test in
  either direction; the fail-open direction is a refactor the comment argues against and nothing enforces
  -> filed as issue #462 rather than edited into a reviewed head
- [verifier] the module's /health request carries the bearer, so a race-winner receives that start's key
  before the fourth leg refuses it — consistent with the design, stated nowhere. Round 2's reviewer judged
  it not a finding (two existing ADR statements already carry it, and the key authenticates to nothing);
  recorded in the PR body's residue anyway.

## Where a skill blocked or contradicted this run
- harden: its state entry went stale at cycle 7 / head 5ec91f5e and was never updated through cycles
  8-12 (the context compaction sits in that gap), so the Stop gate refused the handover quoting
  "cycle 7 made 16 edits" about a head 20 commits old. The gate was right that the contract was left
  open and wrong about where the run had got to. Closed with the labelled override rather than a
  cycle 13, because by then pr-harden had cleared the head and its FINISH forbids editing a cleared
  sha. Lesson for a retro: harden-set should be written at the CLOSE of every cycle, not only when a
  cycle happens to be the one that spawns an agent — a run that compacts mid-phase loses the record
  otherwise, and the gate then argues from a stale head.
- pr-harden: "collecting in the same turn means never polling afterwards" vs the Stop gate's refusal to end
  a turn with an agent outstanding. An agent launched with run_in_background:true can only be collected by
  polling TaskOutput, and each poll injects a 32 KB window of raw JSONL. Fixed mid-run by switching to
  run_in_background:false for the fixer, reviewer 2 and the verifier — their reports came back whole, in
  one result each. The first two agents cost ~10 polls between them.
- pr-harden: an isolated fixer cannot push to the pipeline worktree (the harness refuses git operations
  against another worktree), so its edits had to be extracted as a patch and applied by the orchestrator.
  Worked, but the orchestrator then discarded one of the seven files with `git checkout --` while restoring
  its OWN mutation probe — the exact hazard the skill's own paragraph names. Caught by `git diff --stat`
  reading 6 files instead of 7, and re-applied from the patch with `git apply --include=`.

## Declined
- Nothing was declined, in either skill. Two things were deliberately NOT done and named as residue instead:
  an unpredictable ephemeral port for the subprocess (ADR Decision 103 — chartsearchai.llm.serverPort is a
  documented operator contract), and any attempt to widen the dialect rule past the two shortest ways to
  write the defect (a hand-written Socket cannot be banned where the port probe legitimately opens one).

## Assumptions review overturned
- "the readiness probes tie the listener to the child" -> only the port-free check and a liveness question
  asked LATE do; the key probes establish that some credential is demanded and ours was not refused, and
  the first three legs all passed against a live impostor.
- "a text guard that names the right identifier pins the property" -> three times it did not: a wrapped
  receiver, a static import, and a semantically equivalent rewrite.
