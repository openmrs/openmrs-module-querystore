#!/opt/homebrew/opt/bash/bin/bash
# usage: launch.sh <s1|s2> <25|75> <rep>
set -u
s=$1 a=$2 n=$3
Z=$(cd "$(dirname "$0")" && pwd)
case $s in s1) ref=ad69ce66acffe6a34f1e63ec3579f521861675e1 ;; s2) ref=d7a1eefa50e2aac52b34202c1d3fbeb29ec8f5e8 ;; *) echo "bad subject $s" >&2; exit 2 ;; esac
case $a in 25) dl=1800 ;; 75) dl=5400 ;; *) echo "bad arm $a" >&2; exit 2 ;; esac
export HTTP_PROXY=http://127.0.0.1:9 HTTPS_PROXY=http://127.0.0.1:9 http_proxy=http://127.0.0.1:9 https_proxy=http://127.0.0.1:9
export NO_PROXY=api.anthropic.com,.anthropic.com,claude.ai,.claude.ai,statsig.anthropic.com,localhost,127.0.0.1
export no_proxy=$NO_PROXY
export JAVA_TOOL_OPTIONS="-Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=9 -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=9 -Dhttp.nonProxyHosts=localhost|127.0.0.1"
echo "$(date '+%H:%M:%S') start blindz-$s-a$a-$n load=$(sysctl -n vm.loadavg)" >> "$Z/orchestrator.log"
~/.claude/bin/blind-run --deadline $dl --repo /Users/danielkayiwa/Projects/openmrs/stockmanagement --ref $ref "blindz-$s-a$a-$n" "$Z/briefs/$s-a$a.md"
rc=$?
echo "$(date '+%H:%M:%S') end blindz-$s-a$a-$n rc=$rc" >> "$Z/orchestrator.log"
exit $rc
