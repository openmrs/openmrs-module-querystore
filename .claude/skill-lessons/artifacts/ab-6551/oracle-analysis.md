# Recall vs the human oracle — 8 inline comments on core#6551

The 8 captured comments are **4 findings plus 4 author replies**. All four findings were filed
before the reviewed sha and all four are marked resolved *at* it (`commit_id` on every comment is
the head `b7cd909d`; the author's replies name fix commits `92c8b17c`, `18ba3637`, `8a7e6fcdc`, all
of which are ancestors of the reviewed head).

| # | author | finding | author's response | A reached it | B reached it |
|---|---|---|---|---|---|
| 1 | greptile bot (P1) | assigned locations cannot be purged — RESTRICT FK rejects the delete | fixed in `92c8b17c` (the native delete this PR now carries) | **yes** — and went past it: the cascade to `Location.childLocations` still hits the FK | **yes**, same, with a proposed `onDelete="CASCADE"` alternative |
| 2 | greptile bot (P2) | `getLocations()` returns null for a fresh User, violating its own empty-set contract | fixed in `92c8b17c` (field initialised) | no | **yes** — found the residue: `setLocations(null)` restores null, so the contract is still defeatable and `UserServiceImpl` still null-checks |
| 3 | jwnasambu | "kindly could you add a regression test for the fix?" | added in `18ba3637` | **yes** — proved that test passes with the fix reverted | **yes**, same, independently in three lenses, plus the discriminating shape that does fail |
| 4 | jwnasambu | native SQL bypasses the session cache, in-memory collection would be stale; "safe in current code paths, so non-blocking" | comment added in `8a7e6fcdc` recording the caveat | **yes**, as a `question` — could not reproduce in a committing transaction | **yes**, as **blocking** — mutation-isolated the cause, showed it is not staleness but a `TransientPropertyValueException` that aborts the transaction |

Recall: **A 3/4, B 4/4.**

## The oracle's real result, which is not about the A/B

Because every human finding was already addressed at the reviewed sha, this measures "did the arm
reach the same ground and go past it" rather than "did the arm find what humans found." On that
reading both arms beat the human+bot review on this PR, and they beat it in the same two places:

- **The test the human review asked for, and accepted, provably cannot fail.** `jwnasambu` asked for
  a regression test; the author added one; the thread closed. Both arms independently mutated the
  fix away and watched the test stay green. Nobody on the PR checked.
- **The comment the human review accepted as a resolution states something false.** `jwnasambu`
  described the session-cache risk as stale data and "safe in current code paths"; the author shipped
  a comment saying exactly that, and the thread closed on "good catch". The behaviour is not
  staleness — it is an exception that aborts the transaction, and it is reachable from the PR's own
  new test plus one ordinary query.

Both are instances of the same thing `pr-review` Step 1 already names — *"fixed" is a claim, not
evidence* — and here the claim was accepted by two humans and a review bot. That is the strongest
argument in this whole run for the skill's verification discipline, and it is independent of whether
the review is fanned out or not.

## Limitation

n=1, and a PR whose human review had already run to resolution. The oracle cannot separate "arm
found it" from "arm found it because the fix commit made it findable". Both arms were blinded to the
conversation, so neither was led to it — but neither was this a cold-start comparison against a
human review of the same sha.
