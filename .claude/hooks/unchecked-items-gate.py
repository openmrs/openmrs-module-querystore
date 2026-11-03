#!/usr/bin/env python3
"""Stop hook: refuse a reply whose "unchecked" list carries an item without an admissible reason.

The procedure in ~/.claude/proposing-fixes.md ("Nothing checkable stays unchecked") allows an item to stay unchecked
only for one of three reasons, written in these words: it needs the implementation, it needs your decision, or it needs
access. Naming an item was being used instead of doing it (2026-11-02: two checkable items were listed and not run),
so this hook enforces the wording at the moment of yielding.

What it reads: the final reply of the turn, the text blocks of the last assistant entries after the last real user
prompt in the transcript. What it matches: a LABEL, a markdown heading or a bold run whose words say unchecked,
unverified or not (yet) checked/verified. Prose that merely contains those words (a quoted answer's "Not checked:
this patient's conditions") is not a label. Every list item under the label, or the label's own trailing text where
there is no list, must contain one of the REASONS below.

It cannot judge whether a reason is TRUE, only that one was given. It blocks once per stop: with stop_hook_active set
it allows, so a revised reply is never trapped in a loop. Any failure to read its input allows (fail-open), because a
broken Stop hook that blocks would trap every session.
"""
import json
import re
import sys

LABEL = re.compile(
    r"\b(still\s+unchecked|unchecked|unverified|not\s+(?:yet\s+)?(?:checked|verified)|left\s+to\s+check)\b",
    re.IGNORECASE)
REASONS = re.compile(
    r"needs\s+the\s+implementation|needs\s+(?:your|the\s+person's|the\s+user's)\s+decision|needs\s+access",
    re.IGNORECASE)
HEADING = re.compile(r"^\s{0,3}#{1,6}\s+(.*)$")
BOLD = re.compile(r"^\s*(?:[-*]\s+|\d+[.)]\s+)?\*\*(.+?)\*\*(.*)$")
ITEM = re.compile(r"^\s*(?:[-*+]|\d+[.)])\s+(.*)$")
# A code span names a file or a symbol, never a label: `unchecked-items-gate.py` in a bold run is not one (2026-11-02).
CODE = re.compile(r"`[^`]*`")


def final_reply(transcript_path):
    entries = []
    with open(transcript_path, encoding="utf-8") as f:
        for line in f:
            try:
                entries.append(json.loads(line))
            except ValueError:
                continue
    texts = []
    for entry in reversed(entries):
        message = entry.get("message") or {}
        content = message.get("content")
        if entry.get("type") == "user":
            blocks = content if isinstance(content, list) else [{"type": "text"}]
            if any(b.get("type") != "tool_result" for b in blocks if isinstance(b, dict)):
                break
            continue
        if entry.get("type") == "assistant" and isinstance(content, list):
            parts = [b.get("text", "") for b in content if isinstance(b, dict) and b.get("type") == "text"]
            if parts:
                texts.append("\n".join(parts))
                # Only the last assistant text before any tool call is the reply the user reads.
                break
    return "\n".join(reversed(texts))


def labels_with_items(text):
    """Yield (label, [items]) for every unchecked-style label outside code fences and quotes."""
    lines = text.split("\n")
    in_fence = False
    i = 0
    while i < len(lines):
        line = lines[i]
        if line.strip().startswith("```"):
            in_fence = not in_fence
            i += 1
            continue
        if in_fence or line.lstrip().startswith(">"):
            i += 1
            continue
        label, trailing = None, ""
        m = HEADING.match(line)
        if m and LABEL.search(CODE.sub("", m.group(1))):
            label = m.group(1)
        else:
            m = BOLD.match(line)
            if m and LABEL.search(CODE.sub("", m.group(1))):
                label, trailing = m.group(1), m.group(2).strip(" :.-—")
        if label is None:
            i += 1
            continue
        items, j = [], i + 1
        while j < len(lines) and not lines[j].strip():
            j += 1
        while j < len(lines):
            item = ITEM.match(lines[j])
            if item:
                items.append(item.group(1))
            elif lines[j].strip() and items and lines[j].startswith((" ", "\t")):
                items[-1] += " " + lines[j].strip()
            else:
                break
            j += 1
        if not items and trailing:
            items = [trailing]
        yield label, items
        i = j if j > i else i + 1


def main():
    try:
        payload = json.load(sys.stdin)
        if payload.get("stop_hook_active"):
            return
        text = payload.get("last_assistant_message") or final_reply(payload["transcript_path"])
    except Exception:  # fail-open: a broken read must never trap the session
        return
    missing = []
    for label, items in labels_with_items(text):
        for item in items:
            if not REASONS.search(item):
                missing.append(item[:120])
    if missing:
        print(json.dumps({
            "decision": "block",
            "reason": "proposing-fixes.md: an item listed as unchecked must carry one of three reasons, in these "
                      "words: \"needs the implementation\", \"needs your decision\" or \"needs access\". Run every "
                      "other item now, then reply again. Items without a reason: " + " | ".join(missing)
        }))


if __name__ == "__main__":
    main()
