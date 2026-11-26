#!/opt/homebrew/opt/bash/bin/bash
# Lock answer-bearing paths, run 10 reviewer probes and 5 reader probes in parallel, restore the locks on any exit.
P=/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/7abec84a-e793-4dfb-aa6b-79a4f7a18e1d/scratchpad/probe85
PX=http://127.0.0.1:9; NP=api.anthropic.com,.anthropic.com,claude.ai,.claude.ai,localhost,127.0.0.1
export GH_TOKEN=invalid-for-replay GITHUB_TOKEN=invalid-for-replay HTTPS_PROXY=$PX HTTP_PROXY=$PX ALL_PROXY=$PX https_proxy=$PX http_proxy=$PX all_proxy=$PX NO_PROXY=$NP no_proxy=$NP
export JAVA_TOOL_OPTIONS="-Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=9 -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=9"
MODES=$P/modes.txt; : > $MODES
while read -r p; do echo "$(stat -f '%Lp' "$p") $p" >> $MODES; done < $P/lock-list.txt
cp $MODES $P/modes-backup.txt
restore() { while read -r mode path; do chmod "$mode" "$path"; done < $MODES; echo "restored $(wc -l < $MODES | tr -d ' ') paths $(date -u +%H:%M:%S)" > $P/restored.txt; }
trap restore EXIT
PLAN=$(python3 -I -c "import json; k=json.load(open('$P/private/KEY-private.json')); print(' '.join(f'{r}:{k[\"skills\"][a]}' for r, a in sorted(k['runs'].items())))")
for i in 01 02 03 04 05; do PLAN="$PLAN p85r-$i:reader"; done
while read -r mode path; do chmod 000 "$path"; done < $MODES
echo "locked $(wc -l < $MODES | tr -d ' ') paths $(date -u +%H:%M:%S)" > $P/locked.txt
: > $P/exits.txt
for pair in $PLAN; do run=${pair%%:*}; b=${pair##*:}
  ( s=$(date -u +%H:%M:%S); /Users/danielkayiwa/.claude/bin/blind-run --deadline 1500 --repo $P/frozen.git --ref pr-85 $run $P/briefs/brief-$b.md > $P/logs-$run.stdout 2> $P/logs-$run.stderr; echo "$run exit=$? start=$s end=$(date -u +%H:%M:%S) $(tail -1 $P/logs-$run.stderr)" >> $P/exits.txt ) &
  sleep 2
done
wait
echo "wave done $(date -u +%H:%M:%S)" >> $P/exits.txt
