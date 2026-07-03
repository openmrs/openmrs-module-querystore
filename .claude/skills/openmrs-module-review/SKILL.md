---
name: openmrs-module-review
description: >-
  Review a whole OpenMRS module against its openmrs-core contract, verifying
  every claim against real upstream sources (the core PR/API it implements,
  library docs and source) rather than the module in isolation, then post the
  findings as a GitHub issue in a plain senior-reviewer voice. Use when asked to
  review an OpenMRS module as a whole. NOT for a working diff (use code-review)
  or for inline comments on an open PR (use pr-review). Trigger phrases:
  "review this module", "review the <name> module", "code review this OpenMRS
  module", "post review comments for this module".
---

# OpenMRS module review

A whole-module review that finds real bugs by checking the module against the systems it talks to, then lands as a GitHub issue a senior engineer will actually read. The value is in the verification discipline, not the write-up — a review that only reformats findings from reading the module's own code is not this skill.

## When to use / when not to use

- Use for reviewing an OpenMRS module end-to-end, especially one built against an unreleased/new openmrs-core API.
- For a working-tree diff, use `code-review`. For inline comments on an open PR, use `pr-review`. This skill is the "no PR, review the whole module, post an issue" shape; defer to those two for their cases.

## Step 1 — Map the module and its contract

1. Read the whole module (clone to scratchpad if it's a different repo; don't edit the user's tree). Note structure, entry points, `config.xml`, `pom.xml`, README, tests.
2. Find the contract it implements or depends on. If the user names a core PR or JIRA ticket, read it. Otherwise look for the openmrs-core PR that introduced the API the module uses (`gh pr list/view`, `gh api search/code`). Read the **actual contract classes** (event types, service interfaces, annotations) from the merge commit, not from memory.
3. Note the ticket's stated goal. Relate the module back to it at the end as observations (what's implemented, what's implied but absent), not as defects.
4. Check build reality: is CI green? Does it compile against the referenced core version? State this in the review intro so nobody wonders. A stale local `.m2` is not evidence the module is broken — check CI.

## Step 2 — Review by verifying against the real upstream (the core discipline)

Reviewing the module's code against itself finds style, not bugs. The bugs live where the module meets another system. For every claim, verify against the producer/framework, not the module plus its tests.

**Verify data shapes and API semantics against the producer, not the module.** For every field the module reads out of an external payload, or every framework method it calls for its side effects, confirm the real producer emits that shape / the method does that thing. Read the library's own source or docs.
- Canonical catch: a module read the transaction id from `payload.source.txId`, but only the Postgres/Oracle Debezium connectors put `txId` in `source` — the MySQL/MariaDB connectors it targeted don't, so the field was null in production. Confirmed by reading Debezium's own `SourceInfo` classes and the transaction-metadata docs, not by trusting the module.

**Distrust tests that assert against fabricated input.** A green test is not proof of correctness.
- A mock fed hand-written JSON proves the code parses *that* JSON, not that the real producer ever emits it (the `txId` bug sat behind exactly such a mock).
- An integration test that runs the real system but never asserts the field in question leaves that field unverified.
- For each behavior you're trusting, ask: has this path run on real producer output, with the effect observed? If the only evidence is a shape assertion or a hand-built fixture, treat it as unverified.

**Ground framework/library behavior before claiming it.** If a finding depends on how a framework behaves (scheduler locking, generic event routing, connection-factory caching, classloading of optional deps), read the actual implementation or docs and either confirm or soften. Do not post speculation as a finding.
- Canonical correction: an early draft flagged "the scheduler might start two engines." Reading `JobRunrSchedulerService` showed JobRunr serializes recurring runs cluster-wide, so the finding was rewritten to the narrower, real residual risk (a hardcoded `server.id` colliding during the failover window). Verify, then state what's actually true.

**Absence claims are the highest-risk claims.** "Core never does X" must be positively verified against the dependency's sources at the ref the module builds against (look for a sibling source checkout, e.g. `~/Projects/openmrs/openmrs-core`), never inferred from jar scans, config-file greps, or any zero-hit search: component-scanned `@Configuration` beans are invisible to XML, installed SNAPSHOT jars can lag or lead the sources, and shadowed shell tools (ugrep shims with `-I` skip binary-ish input like `strings` output) return silent false negatives. Validate any zero-hit pipeline with a positive control (a string you know is present), use `command grep`/`git grep`/dedicated search tools for evidence, and if the one experiment that could falsify the claim can't run, ship it as a question, not a finding.
- Canonical correction: a review asserted nothing bridges `openmrs-runtime.properties` into Spring `@Value`; core's component-scanned `OpenmrsPropertyConfig` (now `OpenmrsApplicationContextConfig#propertySourcesPlaceholderConfigurer` on master) does exactly that, and the module author had to correct the review. The jar scan behind the claim was a silent no-op, and the "confirming" standalone Spring repro had omitted the very configurer in question — a confirmation that structurally could not fail is not verification.

**Trace outward at least one level** on each boundary: trigger paths, optional/`provided` deps absent at runtime, Spring/lifecycle registration order (does a one-shot `@PostConstruct`/`ContextRefreshedEvent` handler miss things registered later?), and documented invariants in unchanged neighbors.

## Step 3 — Adversarial re-read

After the first pass, re-read specifically hunting field-path, data-shape, and API-semantics assumptions — the class of bug that hides behind fabricated tests. Treat "already reviewed" as a reason to look harder, not to skip. A pass that finds a real bug is evidence the module wasn't fully explored; do another.

## Step 4 — Calibrate severity honestly

- **Silent failures rank high.** Wrong output with no exception (a null field, a dropped event, a mis-scoped filter that captures nothing) costs more than a crash because users find it, not CI. Lead with these.
- Separate **confirmed bugs** from **design/clarity notes** from **open questions**. For genuine uncertainty (e.g. how Spring routes a raw-generic event), ask a precise question rather than dressing a guess as a finding.
- **Retract when wrong.** If verification kills a finding, drop it and say so. Correcting your own overstatement is part of the method, not a failure.
- Before deferring anything as "minor", write the one-line failure mode. If you can't, you don't understand it yet.

## Step 5 — Write for a senior engineer

Plain reviewer voice, not a generated report:
- No emoji severity badges. No rigid "**N. Title** — Recommend:" template. Group by area (Security / Correctness / Config / Docs) with a short numbered list of prose findings.
- `file:line` references woven into the sentences. First-person ("I'd default this to loopback") is fine.
- Minimal em-dashes; cut filler ("worth noting", "leverage", "robust", "comprehensive"). Vary sentence openings.
- Don't restate what the author already told you, and don't pad with template-default nits (e.g. the activator using commons-logging is the OpenMRS module template default — noise).
- Scan the draft for AI tells before posting: `grep -nE "—|delve|leverage|robust|seamless|comprehensive|🔴|🟠|🟡"` should come back empty.
- (This voice is also captured in the `review-writeup-style` memory.)

## Step 6 — Deliver

- No open PR → post one GitHub issue titled "Code review notes". **Check for an existing one first** (`gh issue list --repo <org/repo> --state all`); if it's there, revise it (`gh issue edit <n> --repo <org/repo> --body-file <scratchpad/review.md>`) rather than opening a duplicate, otherwise create it (`gh issue create --repo <org/repo> --title "Code review notes" --body-file ...`). When re-reviewing, also check for new commits since the last pass (`gh api repos/<org/repo>/commits`) so you know whether you're revising or starting fresh.
- If the repo has Issues disabled (`gh issue create` fails, or `gh api repos/<org/repo> --jq .has_issues` is false), don't assume a target: ask whether to enable Issues and post, drop the review as a commit comment on HEAD, or hand over the markdown for JIRA. Enabling Issues (`gh api -X PATCH repos/<org/repo> -f has_issues=true`) changes a repo setting, so only do it when the user asks.
- Posting to a public repo notifies watchers. If it's the first post and the destination is ambiguous (issue vs commit comments), confirm with the user; once established, keep using the same destination.
- Open PR exists → hand off to `pr-review` for inline comments instead.

## Anti-patterns

- **Reviewing the module in isolation.** The bugs are at the boundaries. If you didn't read the upstream producer/framework, you haven't reviewed it.
- **Trusting green tests.** Especially tests built on fabricated input or missing the assertion that matters.
- **Posting ungrounded speculation.** If you haven't read the impl/docs behind a framework claim, either verify it or frame it as a question.
- **Padding.** Template-default nits and restating the author's own context dilute the signal. Cut them.
- **Inflating severity by label instead of consequence**, or **collapsing genuine uncertainty into fake-confident findings.**
