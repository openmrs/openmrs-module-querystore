# Blind agents on the PR and its acceptance criteria

pr-review's Step 2 starts these, outside CI: one agent on the whole PR every round, and, where the PR's ticket
lists acceptance criteria, one per criterion.

- **Launch.** Use the command that *A fix you recommend is different* (pr-review's Step 3) gives, at
  `--deadline 5400`, or `--deadline 1800` for a whole-PR agent started without per-criterion agents, in the
  background. `blind-run` passes on your environment, so point `HTTP_PROXY`, `HTTPS_PROXY`, their lower-case forms and
  the `http(s).proxyHost` and `proxyPort` of `JAVA_TOOL_OPTIONS` at a closed local port, and set `NO_PROXY` and
  `no_proxy` to the Anthropic and claude.ai hosts, `localhost` and `127.0.0.1`. An agent whose log shows it never
  reached the model is owed, not clean.
- **Brief.** Open it with "stop after about N minutes of work and report what you have, marking what you did not run",
  N being 75, or 25 for a whole-PR agent at `--deadline 1800`. Give the ticket in full, or the PR's description where
  the PR has no ticket or its ticket could not be read, and the decisions its thread or the user already made, but
  none of the review's own findings. Name the PR's base as `origin/<baseRefName>`, and tell the agent to use no
  network, post nothing, push nothing and change nothing outside its own folder. Then give its target:
  - for a criterion, any configuration, request sequence, failure or timing under which the head misses it;
  - for the whole PR, what a maintainer who must live with it would find wrong.
- **Also in the brief, since the agent knows only what it is told:** "`MAVEN_ARGS` already gives every `mvn` command
  your own Maven repository and OpenMRS data directory. Keep it, and Maven offline (`-o`)." (Maven ignores the proxy
  settings, and offline it resolves from what `~/.m2` already holds.) And: start your own servers, each with its own
  data directory and a free port between 18000 and 19000, and touch no server you did not start.
