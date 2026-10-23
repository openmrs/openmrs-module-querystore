#!/bin/bash
# Known-bad and known-good cases for relocation_mechanism.py. It copies one transcript into a folder named
# for no cwd it ever had, and that copy must FAIL; it copies another into the folder its last
# `relocated` record names, and that copy must hold. Without the first, "every transcript holds" could
# be a check that never fails.
# Usage: calibrate.sh   (reads two transcripts under ~/.claude/projects; writes only to a temp directory)
set -u
here="$(cd "$(dirname "$0")" && pwd)"
p="$HOME/.claude/projects/-Users-danielkayiwa-Projects-openmrs-chartsearchai"
t="$(mktemp -d)"
trap 'rm -rf "$t"' EXIT
mkdir -p "$t/-wrong-folder" "$t/-Users-danielkayiwa-Projects-openmrs-chartsearchai"
cp "$p/ebc78818-5b31-4fed-9943-24ca301a3943.jsonl" "$t/-wrong-folder/" || exit 1
cp "$p/111381c1-d104-4508-9063-81d4c5f687f1.jsonl" "$t/-Users-danielkayiwa-Projects-openmrs-chartsearchai/" || exit 1
python3 -I "$here/relocation_mechanism.py" "$t" | head -3
exit "${PIPESTATUS[0]}"
