#!/usr/bin/env python3
"""Cases for unchecked-items-gate.py, driven through the hook itself (stdin JSON in, decision out).

Usage: python3 unchecked-items-gate-test.py [path/to/unchecked-items-gate.py]   (default: beside this file)

MISFIRES holds the labels the gate now passes among its blocks from 2026-11-02, when it was added, to 2026-11-20 that
listed no open item of the reply's own: its own name, and a label giving its one item's reason. The other misfires
(quoted mentions, checklist categories, numbered lesson titles) still block, by decision (REJECTED.md, 2026-11-22).
OPEN_LISTS holds, once each, the
labels of the other 32, which were real lists of open items and must still block. Both were read off the transcripts by
the 2026-11-20 retro, verbatim except that PR numbers are masked.
"""
import json
import os
import subprocess
import sys

GATE = sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(os.path.abspath(__file__)), "unchecked-items-gate.py")

MISFIRES = [
    "Not checked by the auditor, and I can't check it either (needs access to the Action's workflow file):",
    'Not checked, because it needs access:',
    'The unchecked-items gate fires on quoted wording.',
]

OPEN_LISTS = [
    'Leads not verified',
    'Likely cause of the duplicates (not checked):',
    'Not checked',
    'Not checked before the commit:',
    'Not checked in the browser:',
    'Not checked on the public demo.',
    'Not checked:',
    'Not verified',
    'Not verified here:',
    'Not verified:',
    "Refuter's unverified lead:",
    'Still unchecked',
    'Unverified lead for #1234:',
    'Unverified lead from the agent:',
]


def run(reply, active=False):
    payload = json.dumps({"last_assistant_message": reply, "stop_hook_active": active})
    out = subprocess.run([sys.executable, GATE], input=payload, capture_output=True, text=True).stdout.strip()
    return bool(out) and json.loads(out).get("decision") == "block"


def replies(label):
    """The two shapes a label takes in a reply: a bold run with a list under it, and a bold run with trailing text."""
    return [f"Report.\n\n**{label}**\n- the JDK 8 build\n", f"Report.\n\n**{label}** the JDK 8 build\n"]


cases = []
for label in MISFIRES:
    cases += [(f"misfire passes: {label}", r, False, False) for r in replies(label)]
for label in OPEN_LISTS:
    cases += [(f"open list blocks: {label}", r, False, True) for r in replies(label)]
# A real list each narrowed rule must still let through to the check (the 2026-11-20 refuter's counterexamples).
cases += [
    ("own claims are not a checklist category", "**Unverified claims I made above:**\n- the JDK 8 build\n", False, True),
    ("a quote beside the label words is not a quoted label", '**Not verified ("mvn test" on JDK 8):**\n- the JDK 8 build\n', False, True),
    ("a long numbered heading that starts with the words", "## 2. Not verified on the demo server before the merge\n- the deploy\n", False, True),
    ("a short numbered label is no title", "**3. Leads not verified:**\n- the JDK 8 build\n", False, True),
    ("a reason in the label cannot excuse a list", "**Not verified (one of these needs access):**\n- the JDK 8 build\n- the demo deploy\n", False, True),
    ("a category in the first person is the reply's own list", "**Unchecked claims in my report above:**\n- the JDK 8 build passes\n", False, True),
    ("unverified suspicions are no category", "**Unverified suspicions:**\n- a race in the cache\n", False, True),
    ("a numbered sentence ending in a colon heads a list", "**1. The JDK 8 build and the demo deploy are not verified:**\n- the deploy\n", False, True),
    ("a category with no person named", "**Unchecked claims in this report:**\n- the JDK 8 build passes\n", False, True),
    ("a bare category over two items", "**Unchecked claims above:**\n- the JDK 8 build passes\n- the deploy works\n", False, True),
    ("a numbered sentence ending in a period", "**1. The JDK 8 build and the demo deploy are not verified.**\n- the deploy\n", False, True),
    ("a numbered sentence with no stop", "**2. These two items remain not verified on the demo**\n- the deploy\n- the build\n", False, True),
    ("quotes for emphasis", "**\u201cUnverified\u201d items:**\n- the JDK 8 build\n- the deploy\n", False, True),
    ("the word itself in quotes", '**Items I left "unchecked":**\n- the JDK 8 build\n', False, True),
]
cases += [
    ("heading label blocks", "## Not verified\n- the JDK 8 build\n", False, True),
    ("an item with a reason passes", "**Not verified:**\n- the JDK 8 build: needs access to a JDK 8 runner\n", False, False),
    ("the gate's file name in a code span passes", "**`unchecked-items-gate.py` changed**\n- one line\n", False, False),
    ("a second stop always passes", "**Not verified:**\n- the JDK 8 build\n", True, False),
    ("prose that only mentions the words passes", "Nothing here is not checked.\n", False, False),
]

failed = 0
for name, reply, active, want in cases:
    got = run(reply, active)
    if got != want:
        failed += 1
        print(f"FAIL {name}: expected {'block' if want else 'pass'}, got {'block' if got else 'pass'}")
broken = subprocess.run([sys.executable, GATE], input="not json", capture_output=True, text=True).stdout.strip()
if broken:
    failed += 1
    print("FAIL unreadable input must allow (fail-open)")
print(f"{len(cases) + 1 - failed} of {len(cases) + 1} cases pass")
sys.exit(1 if failed else 0)
