# proposal · resolve-ticket — an adjacent defect noticed unattended has nowhere durable to go · drafted 2026-08-26
status: DRAFT. Not refuted — Step 5 has not run. It also leaves one design choice OPEN on purpose (see
        **Where the capture lives**), which a refuter should settle rather than inherit.
target: resolve-ticket (the run-record template, and one anti-pattern line), possibly one clause in
        skill-retro depending on how the open choice lands
found by: a question during the ticket-pool build — should the pipeline file issues for bugs it finds?
          Not by a run. No pool run had completed when this was drafted.

## The problem

`resolve-ticket` says an adjacent defect "goes in the report or a new ticket, not into this PR", and
scope discipline is right. But under UNATTENDED operation the report is a session stream nobody opens,
so "goes in the report" resolves to lost. The pipeline does not gain a gap here — it INTRODUCES one,
because the flow this instruction describes has been working only while a human was reading the output.

## What says the class is real, and the honest limit of it

Measured 2026-08-26 over the 40 open issues of `openmrs-module-chartsearchai`, matching provenance
phrases in the issue bodies (`found by`, `found independently`, `#N's fix pass`, `split out of`,
`carried forward from`, `residue`, `review agents`): **23 of 40 name a run or a review pass as their
provenance.** Several name the mechanism exactly — `#208's fix pass`, `found independently by two of
its review agents`.

**What that does NOT establish**, and a refuter should press here: the phrases were matched, not each
issue read, so the count includes shapes like "residue" and "found while" that may be a different thing.
And every one of those issues was written by a human who was present. Nothing in the measurement shows
a run WOULD have captured them, or that the observation happened inside a `resolve-ticket` run rather
than in an ad-hoc session. The number establishes that the class is large and valuable here. It does not
establish that automatic capture would reproduce it.

## Proposed: capture in the run record. NOT automatic issue filing

Add one section to the run-record template, written on every run including when it is empty:

```markdown
## Adjacent defects noticed, not fixed
- <what, in one sentence> · <file/method where it sits> · <how it was seen> · <verified? how, or not at all>
```

Written even when there is nothing — "none noticed" — for the reason the template already gives about
`context:`: a field filled in only when something happened biases every retro that reads it.

And retarget the anti-pattern's "goes in the report or a new ticket" at that section, so the instruction
names a place that survives the session.

**Filing issues automatically is deliberately not proposed**, on this skill's own logic rather than on
general caution. `skill-retro` exists because a run cannot grade its own lessons, the best ones come
from an adversarial pass, one run cannot tell a pattern from noise, and append-only growth dilutes. Each
transfers to product defects unchanged. Two more are specific to a tracker: the issues in this repo carry
measured counts through the real predicates, pinned test names and honest scoping, and a passing
observation filed automatically would not meet that bar while diluting a tracker that currently does;
and a run that files will re-file the same adjacent defect every time it touches that code, because
deduplication means reading the backlog first — which is why the existing issues say "split out of #292"
instead of repeating it.

## Where the capture lives — OPEN, and Step 5 should decide it

The run record is the one artefact a run reliably writes, which argues for putting the section there.
But run records live in `~/.claude/skill-lessons/` and `skill-retro` reads them for SKILL lessons; a
product defect in that file is off-topic for every retro that reads it, and would need a clause telling
the retro to route or ignore it. The alternative is a separate capture path, which is a second mechanism
to write and to remember. This proposal prefers the record section and flags the cost; it does not claim
the cost is small.

## What is NOT proposed

- No triage skill, no promotion pass, no issue creation. Those become designable once captures exist to
  design against, and zero exist today. Building the gate before the evidence is the shape this
  pipeline's own governance keeps rejecting.
- No change to what a run may put IN a PR. Scope discipline is unaffected.

## Pruning

One template section and one retargeted clause. Net growth is a few lines, and it retires nothing —
which is itself worth a refuter's attention, since Step 4 asks what an addition subsumes and the honest
answer here is "nothing".

## Refutation record

Empty. Step 5 has not run. Attacks worth trying, beyond the limits already stated above:

1. **Does a home already exist?** `pr-harden`'s declined ledger is durable and per-round; the four
   middle sections of the record carry fresh-agent findings. Is an adjacent product defect already
   covered by one of those, making this a fifth home for something that has four?
2. **Would the section actually get filled?** A section runs skip is worse than no section, because its
   emptiness reads as "nothing adjacent was seen" to every later reader.
3. **Is the problem the record or the REPORT?** If the report were written to a durable file per run,
   the instruction would already work and no template change would be needed.
