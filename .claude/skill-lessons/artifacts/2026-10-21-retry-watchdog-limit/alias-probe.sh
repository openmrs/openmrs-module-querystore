#!/bin/bash
# alias-probe.sh — defines resolve-ticket's alias in an interactive bash with a probe `claude` first on
# the PATH that prints the two variables it was started with, runs `claude --version` through it, and
# prints whether the variables are left in the shell itself. The control runs the probe with no alias.
set -u
d="$(mktemp -d)"; mkdir -p "$d/bin"
cat > "$d/bin/claude" <<'EOF'
#!/bin/sh
echo "probe claude ran: WATCHDOG=${CLAUDE_CODE_RETRY_WATCHDOG:-unset} CAP=${CLAUDE_CODE_RETRY_WATCHDOG_MAX_USAGE_LIMIT_WAIT_MS:-unset} args=$*"
EOF
chmod +x "$d/bin/claude"
printf '%s\n' "alias claude='CLAUDE_CODE_RETRY_WATCHDOG=1 CLAUDE_CODE_RETRY_WATCHDOG_MAX_USAGE_LIMIT_WAIT_MS=18000000 claude'" \
  'claude --version' 'echo "the shell after: WATCHDOG=${CLAUDE_CODE_RETRY_WATCHDOG:-unset} CAP=${CLAUDE_CODE_RETRY_WATCHDOG_MAX_USAGE_LIMIT_WAIT_MS:-unset}"' > "$d/rc"
echo "== bash $BASH_VERSION, interactive, with the alias"
# stderr kept, less the two lines an interactive bash prints with no terminal to control
env -i PATH="$d/bin:/usr/bin:/bin" HOME="$d" bash --norc -i "$d/rc" 2>&1 |
  grep -v -e 'cannot set terminal process group' -e 'no job control in this shell'
echo "== the control: no alias"
env -i PATH="$d/bin:/usr/bin:/bin" HOME="$d" bash --norc -c 'claude --version'
rm -rf "$d"
