#!/bin/bash
# usage: run.sh <label> <env-assignment-or-NONE> <prompt>
label=$1; envset=$2; prompt=$3
d="$(dirname "$0")/$label"; mkdir -p "$d"; cd "$d" || exit 1
sid=$(uuidgen | tr 'A-Z' 'a-z'); echo "$sid" > sid
start=$(date +%s); echo "START=$start" > meta
if [ "$envset" != NONE ]; then export "$envset"; fi
claude -p "$prompt" --output-format stream-json --verbose --dangerously-skip-permissions --session-id "$sid" < /dev/null 2> err \
  | while IFS= read -r line; do printf '%s\t%s\n' "$(date +%s)" "$line"; done > out.tsv
echo "EXIT=${PIPESTATUS[0]} END=$(date +%s)" >> meta
