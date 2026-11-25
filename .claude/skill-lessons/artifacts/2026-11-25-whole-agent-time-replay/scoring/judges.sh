#!/opt/homebrew/opt/bash/bin/bash
# Three blind judges for the 2026-11-25 whole-agent time replay, one per subject, in parallel.
set -u
D=${D:?set D to the folder holding the judge briefs}
judge() {
  local s=$1
  ( cd /private/tmp && \
    JAVA_TOOL_OPTIONS="-Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=9 -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=9" \
    HTTPS_PROXY=http://127.0.0.1:9 HTTP_PROXY=http://127.0.0.1:9 https_proxy=http://127.0.0.1:9 http_proxy=http://127.0.0.1:9 \
    ALL_PROXY=http://127.0.0.1:9 NO_PROXY=api.anthropic.com,.anthropic.com,claude.ai,.claude.ai,localhost,127.0.0.1 \
    no_proxy=api.anthropic.com,.anthropic.com,claude.ai,.claude.ai,localhost,127.0.0.1 GH_TOKEN=invalid GITHUB_TOKEN=invalid \
    "$HOME/.claude/bin/blind-run" --deadline 1800 "dlr2-judge-$s" "$D/judge-$s.md" > "$D/judge-$s.out" 2>&1
    echo "exit=$?" >> "$D/judge-$s.out" )
  echo "$(date '+%H:%M:%S') judge $s ended ($(tail -1 "$D/judge-$s.out"))" >> "$D/orchestrator2.log"
}
echo "$(date '+%H:%M:%S') judges start" >> "$D/orchestrator2.log"
judge s1 & a=$!
judge s2 & b=$!
judge s3 & c=$!
wait $a $b $c
echo "$(date '+%H:%M:%S') judges done" >> "$D/orchestrator2.log"
