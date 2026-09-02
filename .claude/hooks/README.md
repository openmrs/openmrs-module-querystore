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
- `git-restore-backup.sh` — PreToolUse(Bash). Copies modified tracked files aside before a
  `git checkout -- <path>` / `git restore <path>` runs. Vendored here 2026-08-27; it had existed only on
  one machine, which is what `21b0e7e` fixed for two skills. It has no skill-side copy.

Registration lives in the user's own `~/.claude/settings.json` (`Stop` for the two gates, `PreToolUse`
matcher `Bash` for the backup, matcher `Agent` for the model guard) and is not vendored — it is user configuration, not pipeline code.

Tests: `bash .claude/skills/harden/gate-test.sh .claude/hooks/harden-cycle-gate.sh` and the pr-harden
equivalent (either path form works; the harness resolves it and exits 2 on a hook it cannot find).

`git-restore-backup.sh` has no tests, and that is a gap rather than a property of the script. It looks
like a fail-open `cp`, but it decides which commands are destructive, and review measured that decision
missing real forms: `git checkout HEAD <path>` (a path form without `--`), and anything prefixed
`git -C <dir>` or `git -c k=v`, because the prefix pattern expects every leading token to start with a
dash and a value does not. It also always backs up `$CWD`'s repo, so a `-C` target would copy the wrong
one. Under-copying is the whole failure it exists to prevent, so these are worth closing — with cases,
since a regex is exactly the thing that should not be changed on inspection alone.
