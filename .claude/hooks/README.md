# Hooks

The hooks this pipeline registers. They are SEPARATE copies from the ones the skills carry —
`~/.claude/hooks/` is what `settings.json` points at, and a skill push alone leaves the registered
gate running old logic, which is why `skill-retro` Step 6 requires `cmp` across both.

- `harden-cycle-gate.sh` — Stop hook. Enforces `harden`'s termination contract (a cycle that edited is
  not the last cycle) and refuses an unattended yield with an agent outstanding. Also carried in
  `.claude/skills/harden/`.
- `pr-harden-gate.sh` — Stop hook. Same, for `pr-harden`'s phase/blocking contract. Also carried in
  `.claude/skills/pr-harden/`.
- `no-subagent-model-override.sh` — PreToolUse(Agent). Refuses an `Agent` call that sets an explicit
  `model`, so a running session cannot downgrade a subagent per call — `harden` and `pr-harden` both
  carry the matching rule, and until 2026-09-02 `pr-harden` named a cheaper agent as the remedy for a
  429. Like the backup hook it has no skill-side copy. Read its header before trusting it: it guards
  the per-call parameter only, and an agent definition's `model:` frontmatter, a configured default
  subagent model, and the session's own `--model` all outrank the session model without producing a
  call for it to refuse.
- `pipeline-rm-shape.py` — PreToolUse(Bash). Refuses, in an unattended session only, the `rm`
  shapes that reach Claude Code's `dangerousRemoval` circuit breaker, and hands back the rewrite. That
  breaker is declared `bypassImmune`, so it raises a prompt an unattended run cannot answer:
  `--dangerously-skip-permissions` does not clear it, a permission rule may not auto-allow it, and a
  hook answering `allow` does not clear it either — all three measured 2026-09-13, after ticket 409's
  session stalled on one. A hook `deny` IS honoured ahead of it, which is why this refuses rather than
  permits. It matches an rm argument ending in a glob (the predicate three of the breaker's four
  branches share) plus the working directory, its ancestors, critical paths and worktree roots (the
  fourth); it deliberately does NOT reimplement those branches' internals. "Unattended" is a worktree
  cwd OR pool-run's `CLAUDE_PIPELINE_SESSION` / `CLAUDE_PIPELINE_SLOT` in the environment, which hooks
  inherit — the path alone missed `/skill-retro` and the health probe, which the driver runs outside
  any worktree. `pool-run` sets the stamp in `Session.__init__`, so the two move together. Read its
  header before widening it. No skill-side copy.
- `git-restore-backup.sh` — PreToolUse(Bash). Copies modified tracked files aside before a
  `git checkout -- <path>` / `git restore <path>` runs. Vendored here 2026-08-27; it had existed only on
  one machine, which is what `21b0e7e` fixed for two skills. It has no skill-side copy.

Registration lives in the user's own `~/.claude/settings.json` (`Stop` for the two gates, `PreToolUse`
matcher `Bash` for the rm-shape guard and the backup, matcher `Agent` for the model guard) and is not vendored — it is user configuration, not pipeline code.

Tests: `bash .claude/skills/harden/gate-test.sh .claude/hooks/harden-cycle-gate.sh` and the pr-harden
equivalent (either path form works; the harness resolves it and exits 2 on a hook it cannot find).
`bash ~/.claude/hooks/pipeline-rm-shape-test.sh` covers the rm-shape guard; its cases are shapes
measured to prompt and real commands lifted from pool-run transcripts, and dropping any one of the
guard's rules reddens it.

`git-restore-backup.sh` has no tests, and that is a gap rather than a property of the script. It looks
like a fail-open `cp`, but it decides which commands are destructive, and review measured that decision
missing real forms: `git checkout HEAD <path>` (a path form without `--`), and anything prefixed
`git -C <dir>` or `git -c k=v`, because the prefix pattern expects every leading token to start with a
dash and a value does not. It also always backs up `$CWD`'s repo, so a `-C` target would copy the wrong
one. Under-copying is the whole failure it exists to prevent, so these are worth closing — with cases,
since a regex is exactly the thing that should not be changed on inspection alone.
