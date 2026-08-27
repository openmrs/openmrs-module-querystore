# Hooks

The three hooks this pipeline registers. They are SEPARATE copies from the ones the skills carry —
`~/.claude/hooks/` is what `settings.json` points at, and a skill push alone leaves the registered
gate running old logic, which is why `skill-retro` Step 6 requires `cmp` across both.

- `harden-cycle-gate.sh` — Stop hook. Enforces `harden`'s termination contract (a cycle that edited is
  not the last cycle) and refuses an unattended yield with an agent outstanding. Also carried in
  `.claude/skills/harden/`.
- `pr-harden-gate.sh` — Stop hook. Same, for `pr-harden`'s phase/blocking contract. Also carried in
  `.claude/skills/pr-harden/`.
- `git-restore-backup.sh` — PreToolUse(Bash). Copies modified tracked files aside before a
  `git checkout -- <path>` / `git restore <path>` runs. Vendored here 2026-08-27; it had existed only on
  one machine, which is what `21b0e7e` fixed for two skills. It has no skill-side copy.

Registration lives in the user's own `~/.claude/settings.json` (`Stop` for the two gates, `PreToolUse`
matcher `Bash` for the backup) and is not vendored — it is user configuration, not pipeline code.

Tests: `bash .claude/skills/harden/gate-test.sh .claude/hooks/harden-cycle-gate.sh` and the pr-harden
equivalent. `git-restore-backup.sh` has none; it is a fail-open `cp` with no decision to assert.
