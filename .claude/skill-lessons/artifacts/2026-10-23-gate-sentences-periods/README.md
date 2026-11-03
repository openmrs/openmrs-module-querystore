# harden's gate sentences, in two periods (2026-10-23)

The 2026-10-23 retro counted harden's gate sentences over 61 transcripts from public run records, all
from 2026-09-23 on (`../2026-10-23-retro-window/`). An owner-directed proposal built on that count went
to a refutation gate, which found that earlier runs survive too. Their transcripts are gone from
`~/.claude/projects`, but the pool's stream logs keep them in `~/.claude/pipeline/logs/`, which is not
in this repository. This folder keeps the counts from both periods.

Each output's command heads it, and the scripts read only.

## The populations

- **`pre034-logs.tsv`:** the 26 per-ticket pool logs dated 2026-08-26 to 2026-09-17, before harden 0.34.0
  (`6f15b1c`, 2026-09-22). None matches a private key.
- **`pre034-harden.tsv`:** the 13 of those logs that load harden's template.
- **`post034-harden.tsv`:** the 61 public-record transcripts that load harden, each found by its
  session id. The paths are as of 2026-10-23.

## The outputs

- **`gate-sentences-pre034.out`** is `../2026-10-23-retro-window/gate_sentences.py` over the 26:
  - 13 load the template;
  - 8 state at least one of the Transition gate's sentences in their literal form;
  - by block, that is 11 stopping condition, 7 integration questions and 7 tests;
  - 7 runs state "Integration questions answered".
  - The Termination and Phase 2 sentences did not exist in this wording before 0.34.0.
- **`integ_content.py`** was written by the refutation gate. It counts the transcripts holding an
  assistant block that names at least K of the six integration-question topics, in any words.
  - At K=4 it catches 6 of the 7 runs that state the integration answers literally. It misses #310,
    whose best block names 3. It flags none of the 6 pre-0.34 runs that do not.
  - **`integ-pre034.out`:** 6 of 13.
  - **`integ-post034.out`:** 0 of 61, and the most any transcript reaches is 3 topics.
- **`models.py`** writes **`models.out`**:
  - before 0.34: `claude-opus-5` in 12 and `claude-sonnet-5` in 1;
  - after 0.34: `claude-opus-5-5` in all 61, on Claude Code 2.1.280 and later;
  - the pool's stream logs record no version.

## What it shows

Before harden 0.34.0, runs stated the Transition gate and answered its integration questions. Since
0.34.0, the 61 runs do neither, in the gate's words or in any others.

Three changes coincide with the drop:
- harden 0.34.0, which moved the verdict into `gate-state harden-set`;
- the model;
- the Claude Code version.

These counts cannot separate them. One session the gate also cited belongs to a ticket whose records are
confidential, and it is left out here.
