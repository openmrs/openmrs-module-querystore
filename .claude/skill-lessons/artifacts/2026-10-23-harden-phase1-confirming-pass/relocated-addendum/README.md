# The relocated runs, added to the confirming-pass measurement (2026-10-23)

The note's population came from `../scripts/public-transcripts.py`, which keeps a record's transcript
only at its recorded path. Five records named a path their transcript had since been moved away from
(`../../2026-10-23-retro-window/transcript-presence.out`). This folder adds them with the note's own
scripts.

## Files, in the order they were made

1. **`relocated-transcripts.tsv`** pairs each transcript, by its current path, with the one record
   that names it.
2. **`relocated-traversals.txt`** is `../scripts/phase1-traversals.py` over those five transcripts.
   - Two load no harden: #402/PR544 and the 515 driver capture.
   - #527, #562 and #564 give 8 non-terminal traversals, 3 first and 5 resumed.
3. **`relocated-digest.txt`** is `../scripts/post-edit-digest.py` over (2), giving one block per
   traversal.
4. **`build_blind_packet.py`** writes **`blind-packet.md`** and **`blind-key-private.json`**.
   - The packet holds the rubric, the 8 new blocks and 12 calibration blocks from the note's digest
     (2 per class, fixed seed), all shuffled.
   - Every header loses its record and its outcome. A pattern that hits all 127 of the original
     digest's headers hits none of the packet.
   - The key maps each block back to its source.
5. **`blind-classifier-reply.txt`** is the reply of a fresh agent that read only the packet: one class
   per block, with a reason.
6. **`score_blind.py`** writes **`score-blind.out`**, plus **`combined-digest.txt`** and
   **`combined-classes.txt`**.
   - The calibration result: 10 of 12 blocks agree with the note's reader exactly, and 11 of 12 by the
     groups the tables use. The differences are one N read as 0, and one R read as Rc.
   - The combined files are the note's 127 blocks and classes, followed by the 8 new ones.
7. **`combined-rates.out`** is `../scripts/class-rates.py` over the combined files.

To re-derive, run these from this folder. The tsv holds each transcript's path as of 2026-10-23, and a
transcript that moves again has to be found by its session id: `ls ~/.claude/projects/*/<uuid>.jsonl`.

```
cut -f1 relocated-transcripts.tsv | xargs python3 -I ../scripts/phase1-traversals.py > relocated-traversals.txt
python3 -I ../scripts/post-edit-digest.py relocated-traversals.txt relocated-transcripts.tsv ../scripts/harden-events.py > relocated-digest.txt
python3 -I build_blind_packet.py .. .
python3 -I score_blind.py .. . blind-classifier-reply.txt > score-blind.out
python3 -I ../scripts/class-rates.py combined-digest.txt combined-classes.txt
```

`build_blind_packet.py`'s seed fixes the calibration sample and the order. The classification itself
is the one agent's reply in (5), and it does not re-derive.
