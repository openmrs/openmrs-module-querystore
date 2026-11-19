#!/opt/homebrew/opt/bash/bin/bash
R="/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/44d9b58a-4830-47af-b032-8ad1f2e620d4/scratchpad/replay-2r"
MODES="$R/private/modes.txt"; : > "$MODES"
while read -r p; do echo "$(stat -f '%Lp' "$p") $p" >> "$MODES"; done < "$R/lock-list.txt"
restore() { while read -r mode path; do chmod "$mode" "$path"; done < "$MODES"; echo "restored $(wc -l < "$MODES") paths $(date -u +%H:%M:%S)" > "$R/private/restored.txt"; }
trap restore EXIT
P=http://127.0.0.1:9; NP="api.anthropic.com,.anthropic.com,claude.ai,.claude.ai,localhost,127.0.0.1"
: > "$R/transcripts.txt"; : > "$R/private/exits.txt"
for i in 1 2 3 4 5 6; do wt=wt-0$i
  sid=$(python3 -c 'import uuid; print(uuid.uuid4())'); prompt=$(cat "$R/briefs/p2-$wt.md")
  echo "p2 $wt session=$sid start=$(date -u +%H:%M:%S)" >> "$R/transcripts.txt"
  ( cd "$R/p2/$wt" && env GH_TOKEN=invalid-for-replay GITHUB_TOKEN=invalid-for-replay HTTPS_PROXY=$P HTTP_PROXY=$P ALL_PROXY=$P https_proxy=$P http_proxy=$P all_proxy=$P NO_PROXY=$NP no_proxy=$NP \
      OPENMRS_APPLICATION_DATA_DIRECTORY="$R/data/p2-$wt/" \
      claude -p "$prompt" --model claude-opus-5-5 --output-format stream-json --verbose --dangerously-skip-permissions --strict-mcp-config --session-id "$sid" \
      --disallowedTools WebFetch WebSearch Skill Workflow Artifact ArtifactComments ArtifactData SendMessage PushNotification \
        RemoteTrigger CronCreate CronDelete ScheduleWakeup PublishPlugin ShareOnboardingGuide DesignSync EnterWorktree \
        ExitWorktree ReportFindings ListAgents SendToProject SendToProjectContext \
      < /dev/null > "$R/logs/p2-$wt.jsonl" 2> "$R/logs/p2-$wt.err"; echo "p2 $wt exit=$? $(date -u +%H:%M:%S)" >> "$R/private/exits.txt" ) &
done
sleep 5
while read -r p; do chmod 000 "$p"; done < "$R/lock-list.txt"
echo "locked $(wc -l < "$R/lock-list.txt") paths at $(date -u +%H:%M:%S)" > "$R/private/locked.txt"
wait
echo "all runs ended $(date -u +%H:%M:%S)"
