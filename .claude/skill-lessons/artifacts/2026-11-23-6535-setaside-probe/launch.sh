#!/opt/homebrew/opt/bash/bin/bash
# Lock answer-bearing paths, run the 10 filing probes in parallel, restore the locks on any exit.
SP=/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-openmrs-core/4af4e956-314b-4f19-90e1-df1f26e4f888/scratchpad/probe-setaside
P=http://127.0.0.1:9; NP=api.anthropic.com,.anthropic.com,claude.ai,.claude.ai,localhost,127.0.0.1
export GH_TOKEN=invalid-for-replay GITHUB_TOKEN=invalid-for-replay HTTPS_PROXY=$P HTTP_PROXY=$P ALL_PROXY=$P https_proxy=$P http_proxy=$P all_proxy=$P NO_PROXY=$NP no_proxy=$NP
MODES=$SP/modes.txt; : > $MODES
while read -r p; do echo "$(stat -f '%Lp' "$p") $p" >> $MODES; done < $SP/lock-list.txt
cp $MODES $SP/modes-backup.txt
restore() { while read -r mode path; do chmod "$mode" "$path"; done < $MODES; echo "restored $(wc -l < $MODES | tr -d ' ') paths $(date -u +%H:%M:%S)" > $SP/restored.txt; }
trap restore EXIT
# Read the run-to-skill mapping before the key's own folder is locked.
PLAN=$(python3 -I -c "import json; k=json.load(open('$SP/private/KEY-private.json')); print(' '.join(f'{r}:{k[\"skills\"][a]}' for r, a in sorted(k['runs'].items())))")
while read -r mode path; do chmod 000 "$path"; done < $MODES
echo "locked $(wc -l < $MODES | tr -d ' ') paths $(date -u +%H:%M:%S)" > $SP/locked.txt
: > $SP/exits.txt
for pair in $PLAN; do run=${pair%%:*}; skill=${pair##*:}
  ( s=$(date -u +%H:%M:%S); /Users/danielkayiwa/.claude/bin/blind-run --deadline 1500 --repo $SP/core-probe --ref pr-6535 probe-$run $SP/brief-$skill.md > $SP/logs-$run.stdout 2> $SP/logs-$run.stderr; echo "$run exit=$? start=$s end=$(date -u +%H:%M:%S) $(tail -1 $SP/logs-$run.stderr)" >> $SP/exits.txt ) &
  sleep 2
done
wait
echo "wave done $(date -u +%H:%M:%S)" >> $SP/exits.txt
