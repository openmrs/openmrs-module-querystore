#!/bin/bash
# Re-derives every table in the 2026-10-23 harden Phase 1 confirming-pass note from the transcripts on
# this machine, into the directory given (default: a new temporary one), then compares each output with
# the copy kept beside this script and prints the table. A difference means the inputs changed (a record
# or transcript added or removed since the note), not necessarily that the note is wrong: read the diff.
# Usage: rederive.sh [<out-dir>]
set -u
here="$(cd "$(dirname "$0")" && pwd)"
art="$(dirname "$here")"
out="${1:-$(mktemp -d)}"
mkdir -p "$out"
python3 "$here/public-transcripts.py" "$HOME/.claude/skill-lessons" > "$out/public-transcripts.tsv" || { echo "public-transcripts.py failed"; exit 1; }
cut -f1 "$out/public-transcripts.tsv" | xargs python3 "$here/phase1-traversals.py" > "$out/public-traversals.txt" || { echo "phase1-traversals.py failed"; exit 1; }
python3 "$here/post-edit-digest.py" "$out/public-traversals.txt" "$out/public-transcripts.tsv" "$here/harden-events.py" > "$out/post-edit-digest.txt" || { echo "post-edit-digest.py failed"; exit 1; }
for f in public-transcripts.tsv public-traversals.txt post-edit-digest.txt; do
  if cmp -s "$out/$f" "$art/$f"; then echo "$f: identical to the note's copy"; else echo "$f: DIFFERS from the note's copy (diff $out/$f $art/$f)"; fi
done
python3 "$here/class-rates.py" "$out/post-edit-digest.txt" "$art/post-edit-classes.txt"
