#!/bin/bash
# Independent re-check of every instance with plain git + grep -nF, as the task specifies.
G="git -C /private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/db22ea60-d89d-4e0d-808b-b04c38a332ec/scratchpad/gt/cs.git"
D=/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/db22ea60-d89d-4e0d-808b-b04c38a332ec/scratchpad/gt/vpat
[ -s "${INDEX:-$D/index.tsv}" ] || { echo "no index"; exit 2; }
fail=0; n=0
while IFS=$'\t' read -r i pr fix prefix base file line intro removes; do
  n=$((n+1)); p=$(printf '%s/%03d.txt' "$D" "$i")
  errs=""
  # prefix is fix's first parent, fix is on the PR head
  [ "$($G rev-parse "$fix^1")" = "$prefix" ] || errs="$errs prefix!=fix^;"
  $G merge-base --is-ancestor "$fix" "refs/pull/$pr/head" || errs="$errs fix-not-on-pr-head;"
  # base = merge-base(prefix, baseRefOid)
  bro=$(gh pr view "$pr" --repo openmrs/openmrs-module-chartsearchai --json baseRefOid --jq .baseRefOid)
  [ "$($G merge-base "$prefix" "$bro")" = "$base" ] || errs="$errs base-mismatch;"
  # the false text at the stated line of prefix
  hits=$($G show "$prefix:$file" | grep -nF -f "$p" | cut -d: -f1 | tr '\n' ',')
  case ",$hits" in *",$line,"*) ;; *) errs="$errs grep-prefix-miss(hits=$hits);";; esac
  # introduced_by against base
  if $G show "$base:$file" >/dev/null 2>&1 && $G show "$base:$file" | grep -qF -f "$p"; then inb=OLD; else inb=ADDED; fi
  [ "$inb" = "$intro" ] || errs="$errs introduced_by($inb!=$intro);"
  # the fix removed that line (removed-mode) or kept it (unchanged-mode)
  # exact (trimmed) line counts, so a line the fix EXTENDED is not mistaken for the old one
  fixhits=$($G show "$fix:$file" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//' | grep -cxF -f "$p")
  prehits=$($G show "$prefix:$file" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//' | grep -cxF -f "$p")
  if [ "$removes" = "True" ]; then [ "$fixhits" -lt "$prehits" ] || errs="$errs fix-did-not-remove(pre=$prehits,fix=$fixhits);"
  else [ "$fixhits" -ge 1 ] || errs="$errs unchanged-line-missing-in-fix;"; fi
  if [ -n "$errs" ]; then fail=$((fail+1)); echo "FAIL $i pr$pr $file:$line $errs"; fi
done < "${INDEX:-$D/index.tsv}"
echo "checked=$n failed=$fail"
[ "$n" -gt 0 ] && [ "$fail" -eq 0 ]
