#!/bin/bash
# Blocks any Agent (subagent) call that sets an explicit `model`, so a running
# session cannot downgrade a subagent per call.
#
# Why: an explicit `model` on the Agent tool outranks both the agent
# definition's frontmatter and settings.json, so it is the strongest of the
# levers and the only one a session can pull mid-run on its own initiative. It
# is not the only lever at all — see SCOPE. Until 2026-09-02 the pipeline skills
# named a cheaper agent as the remedy for an agent that dies on a 429; harden
# and pr-harden were edited that day to say the opposite, and this hook is what
# makes that binding rather than advisory.
#
# SCOPE, so nobody mistakes this for more than it is. It keeps a subagent on the
# model of the session that spawned it. It does NOT establish which model that
# session is on, and it does not reach three other levers:
#
#   1. An agent definition's `model:` frontmatter and a configured default
#      subagent model both outrank the session model and produce no call for
#      this to refuse. Agent definitions that pin a model DO exist here: the
#      installed marketplace plugins ship them, and some name a concrete model
#      rather than `inherit` (claude-security/agents/explore.md says
#      `model: sonnet`). They are unreachable only because no plugins are
#      enabled — settings.json has no `enabledPlugins` — so enabling one would
#      put its agents on the model its definition names, with no call for this
#      hook to refuse. Re-check rather than trusting any of that:
#        find ~/.claude/plugins ~/.claude/agents .claude/agents \
#          -path "*/agents/*.md" -exec grep -h "^model:" {} + 2>/dev/null \
#          | sort | uniq -c        # `inherit` is fine; anything else pins
#        # -path matters: without it this also sweeps plugin DOCS, which carry
#        # example `model:` lines that pin nothing.
#        jq '.enabledPlugins, .model, .agent' ~/.claude/settings.json
#      (An earlier version of this comment said none was set. That was a false
#      negative: the search used -path "*.claude/agents/*.md", which cannot
#      match a plugin's <plugin>/agents/ directory, and -maxdepth 6, which
#      cannot reach it. A clean result is only as wide as its inputs.)
#   2. The SESSION's own model — `claude --model ...`. In particular
#      `~/.claude/pipeline/pool-run` passes `--model` to every session it
#      launches when the pool config sets `claude.model`, and
#      `pool-ab-sonnet.json` sets it to claude-sonnet-5[1m] today. That is a
#      deliberate operator choice made outside any session, which is exactly the
#      case this hook's own message defers to the user on — so it is left alone.
#      pool.json, the default config, leaves it null.
#   3. Anything that spawns an agent other than through the Agent tool, such as
#      a Workflow script's own agent() calls. Untested.
#
# Matcher reach: the matcher is "Agent". Verified by sentinel 2026-09-02 that it
# does NOT also fire on `ListAgents`, so it is not a loose substring match on the
# tool name. That is one negative case, not a proof about every tool — but a
# false positive is harmless here anyway, since a tool call carrying no
# `.tool_input.model` exits 0 below.
#
# Fails OPEN on every ambiguity — no jq, unreadable stdin, malformed JSON, no
# `model` field — so it can only ever block the case it is written for.

model=$(jq -r '.tool_input.model // empty' 2>/dev/null)
[ -z "$model" ] && exit 0

reason="Agent call set model=\"$model\". Subagents must inherit the session model; \
drop the model parameter and spawn again. If a cheaper or different model is \
genuinely needed, ask the user first — this is enforced by \
~/.claude/hooks/no-subagent-model-override.sh."

jq -nc --arg r "$reason" '{
  hookSpecificOutput: {
    hookEventName: "PreToolUse",
    permissionDecision: "deny",
    permissionDecisionReason: $r
  }
}'
exit 0
