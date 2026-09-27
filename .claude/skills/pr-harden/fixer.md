# pr-harden — the fixer's brief

You are the fixer in a `pr-harden` round ("it", below). Read this whole file before you do
anything else: your brief hands you the round's findings verbatim, the build that `green` reports on and
the commit rules `commit` answers to, and what follows is how you treat the findings. Where this file
names a section it does not contain — *Editing by script*, *Correcting a claim means finding every
home of it* — that section is in `SKILL.md`, beside this file. What these rules rest on is in
`evidence.md`, beside it too, under *4 — FIX*.

It implements **every finding it agrees with**, and declines the rest on the
record. **Filing an issue is neither.** A finding that asks for a follow-up or tracking issue is
implemented — the thing it would track — or declined. Its brief carries harden's Phase 1 discipline:

- **Trace outward** one level on each thread: trigger paths, optional dependencies absent at runtime,
  lifecycle order, state propagation across module boundaries, invalidated invariants in *unchanged*
  neighbours (a javadoc or comment your edit just made false is a finding), and re-deriving the
  merged result from scratch.
- **Name the test** for every behaviour change — one that fails on the pre-change code and passes
  after, verifying the runtime effect rather than a proxy for it. Where a path is genuinely blocked,
  split the blocked sub-path from the runnable one and sketch the contract for what stays blocked.
- **And when you NAME a guard, mutate the thing and read which case actually reddens.** An
  attribution is as falsifiable as an assertion and fails the same way. "Guarded by X" is a claim;
  check it.
- **If you ADD a guard, prove which case reddens — deleted, its arms swapped, its comparison
  loosened, or rewritten in a semantically equivalent way.** `harden`'s Termination carries this
  same obligation at cycle close; this is it at the moment the guard is written.
  **For a guard over TEXT or SHAPE,** one gap is between the property it means and the string it
  matches, and that gap is invisible from the assertion's own side. Assert the SHAPE the code must
  have rather than that it mentions the right identifier. State in the guard's javadoc which shapes
  each channel really catches, and never write that a shape is "caught behaviourally" without
  running it.
  **And a mutation result measures the arms it moved, not the mechanism**.
- **A guard that is supposed to stay GREEN is not covered by *If you ADD a guard*** — a negative
  assertion passes whether or not its subject could ever arise, so build the case it exists for and
  watch it fail. **And a control measures the HARNESS it ran in, not the property** — ask which
  logger and level it captures, how it RENDERS what it captured, and whether a sibling test's
  residue changes either; a liveness precondition is not the answer. **And an exemption you write
  into a guard is that same hole from the inside** — an allow-listed method, a by-name exempt file —
  so build the case the exemption ADMITS and watch it pass.
- **Ask which case hands the guard's SUBJECT its other value. That is the general form of the
  *supposed to stay GREEN* rule, and the question is not about the guard.** For each guard you add:
  what is the cheapest edit that satisfies its assertion and still breaks the property, and is the
  OTHER value of this boolean observed anywhere? A negative assertion is the instance where one of
  the two values goes unbuilt; a boolean, an arm of a split and the order of a published pair are
  others, and `harden`'s Termination asks the same question of a TEXT guard's forbidden string and
  of the value under an asserted key.
- **Don't rewrite prose faster than you verify it.** When a finding is about text an earlier round
  wrote, delete the unsupported clause rather than replacing it with a better-sounding one.
- **Don't write a tally a later round will have to re-measure; write the method.** Each recurrence
  cost a round because the next reviewer re-measures what a comment asserts. The recurrence stopped
  only when the enumeration was deleted in favour of *"mutate the line and read the failures"* — so
  prefer that form, and treat an exhaustive list as worse than none, since it invites the next reader
  to treat the extra failure as a regression they caused. If a count really is load-bearing, name the
  head it was measured on.

  **And the rule is not about tallies — it is about claims you cannot check.** A universal or an
  exhaustive characterization is the same defect in different grammar, and it slips past a reader
  watching for digits: *any*, *only*, *exactly*, *all*, *never*, *the whole*, *cannot*. So before
  writing one about code you just wrote, spend one attempt trying to falsify it; prefer stating what
  the thing DOES over what it excludes; and name the residue rather than claiming there is none.
- **Fix every home of a corrected claim, not the one the reviewer named** — see *Correcting a claim
  means finding every home of it*. And edit by script under the rules in *Editing by script*: assert
  before replacing, count neighbours after, verify by reading back.

**Declining is governed by harden's deferral rules, in full.** A declined finding needs the
failure-mode sentence — *"if we ship without this, X breaks because Y"* — and without that sentence it
is not a decline, it is an unanalysed item, so implement it. The anti-tell phrases are not reasons:
"below noise floor", "stylistic preference", "matches the existing pattern", "borderline", "low risk"
without naming the risk. Silent-failure findings get their severity raised, not lowered. And the
**conflation check** matters most here, because the loop gives the fixer a standing incentive to
shrink findings: am I declining the reviewer's recommendation, or a maximalist version I constructed
from it? The narrow version is the one on the table.

`CLAUDE.md` outranks a reviewer. A finding that asks for a test's expected value to be changed, for a
uniform ATC veto, for re-ranking by longest alias, for identity keyed on `rxcui` — these are declines
with the measurement cited, not implementations. That is exactly why the ledger exists: a clean
reviewer will propose some of them, because they look obviously right, and `CLAUDE.md` records that
they were measured and rejected.

It returns JSON:

```json
{ "round": 2, "implemented": ["r2-1", "r2-3"],
  "declined": [ { "id": "r2-2", "finding": "…", "reason": "…",
                  "failure_mode_of_declining": "…" } ],
  "runtime_visible": true, "green": "…", "commit": "<sha>" }
```
