#!/usr/bin/env python3
"""Count assistant text blocks that state harden's gate sentences in their literal form.

Usage: gate_sentences.py <tsv, transcript path in column 1> [extra .jsonl ...]

A hit is a `text` block of an assistant message matching a gate's opening words. A block holding the
skill's bracketed template (as each skill load does) is counted apart and never as a hit, whoever
wrote it. A transcript named in the tsv but no longer on disk is listed as MISSING and left out of
the count, so a shrinking population says so instead of reading as fewer hits. Exits 2 if no
transcript could be read.

Patterns, with where each sentence entered harden/SKILL.md (`git log -S` in the source repo):
  stop    "Phase 1 stopping condition met"     Transition gate, 7812b83 (2026-05-15)
  integ   "Integration questions answered"     Transition gate, 01d1cd2 (2026-05-20)
  tests   "Behavior changes without tests"     Transition gate, c18bf29 (2026-05-20)
  p2gate  "Phase 2 ran once, over"             Phase 2 gate,    6f15b1c (2026-09-22)
  term    "Phase 1 verdict" + non-word char    Termination gate, 6f15b1c (2026-09-22)
"""
import json
import os
import re
import sys

PATS = {
    "stop": re.compile(r"stopping condition\W{0,3}met", re.I),
    "integ": re.compile(r"integration questions answered", re.I),
    "tests": re.compile(r"behaviou?r changes without tests", re.I),
    "p2gate": re.compile(r"Phase 2 ran once, over", re.I),
    "term": re.compile(r"Phase 1 verdict\W", re.I),
}
TEMPLATE = re.compile(r"\[last pass returned no further review value \|")
# The Termination gate's own template, which the skill text carries from 6f15b1c on, and with it the
# "Phase 2 ran once, over" wording the p2gate pattern matches.
TERM_TEMPLATE = re.compile(r"Phase 1 verdict: \[converged")


def texts(rec):
    c = (rec.get("message") or {}).get("content")
    if isinstance(c, str):
        yield c
    elif isinstance(c, list):
        for b in c:
            if isinstance(b, dict) and b.get("type") == "text":
                yield b.get("text", "")


def scan(path):
    hits = {k: 0 for k in PATS}
    template = 0
    term_template = False
    examples = []
    with open(path, errors="replace") as fh:
        for n, line in enumerate(fh, 1):
            try:
                rec = json.loads(line)
            except ValueError:
                continue
            role = (rec.get("message") or {}).get("role") or rec.get("type")
            for t in texts(rec):
                if TEMPLATE.search(t):
                    template += 1
                    term_template = term_template or bool(TERM_TEMPLATE.search(t))
                    continue
                if role != "assistant":
                    continue
                for k, p in PATS.items():
                    m = p.search(t)
                    if m:
                        hits[k] += 1
                        if len(examples) < 3:
                            s = max(0, m.start() - 60)
                            examples.append((n, k, t[s:m.end() + 120].replace("\n", " ")))
    return hits, template, term_template, examples


paths = []
with open(sys.argv[1]) as fh:
    for line in fh:
        if line.strip():
            paths.append(line.split("\t")[0].strip())
paths += sys.argv[2:]
tot = {k: 0 for k in PATS}
files_hit = 0
template_blocks = 0
files_template = 0
files_term_template = 0
read = 0
for p in paths:
    if not os.path.isfile(p):
        print("MISSING", p)
        continue
    read += 1
    hits, template, term_template, examples = scan(p)
    template_blocks += template
    files_template += bool(template)
    files_term_template += term_template
    for k in PATS:
        tot[k] += hits[k]
    if any(hits.values()):
        files_hit += 1
        print("HIT", p)
        for ex in examples:
            print("   L%d [%s] %s" % ex)
print("named:", len(paths), "| read:", read, "| loading the template:", files_template,
      "| of those, with the Termination gate's:", files_term_template,
      "| with an assistant hit:", files_hit, "| assistant hits:", tot,
      "| template blocks:", template_blocks)
sys.exit(0 if read else 2)
