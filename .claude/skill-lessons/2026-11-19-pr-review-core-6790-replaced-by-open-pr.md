# defect note · eight pr-review rounds ended clean on a PR an open master PR was about to replace · openmrs-core PR 6790 at ae55e0e280 · 2026-11-19
outcome: miss. Attended `pr-review` rounds on openmrs-core PR 6790 (2.9.x, TRUNK-6844, a login back-off kept in user properties) ran from 2026-10-29 to 2026-11-18. Rounds 7 and 8 ran on 0.25.1. Round 8 ended "ready to merge", told to the owner and not posted. At 2026-11-18T17:10Z ibacher closed it: "While the mechanism here is probably fine, this will need to be reworked with the new storage for user security properties, so I'm closing this so it doesn't accidentally get merged. Tbc, this will need to be rewritten with those changes."
written by: hand, in the 2026-11-18 session (c9736b83), after the owner asked why the review hadn't recommended closing it as the maintainer did
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-openmrs-core/c9736b83-622f-4f85-a88e-570319ce5419.jsonl; round 7 is 9cc96893-3707-4e2a-a538-2d05450c3363.jsonl in the same folder

## Measured (2026-11-18 and 2026-11-19)

- **What replaced it.** An open master PR, opened 2026-11-10, whose thread has the maintainer asking on 2026-11-10 for a 2.9 backport. It moves the lockout counters PR 6790 reads out of user properties, and deletes `getUsersLoginAttempts`, which PR 6790's new branch calls (`HibernateContextDAO.java:211` at ae55e0e280). The maintainer approved it eight minutes after closing PR 6790.
- **Nobody looked.** Round 7's transcript never names that PR. In round 8 the owner's PR index line for it was read at 13:37Z and not followed. The same reviewer reviewed that PR for four rounds in other sessions; those checked a second open 2.9.x lockout PR for the backport, and not PR 6790.
- **Nothing asked.** Step 1 reads only the PR's own threads. Step 2's sibling comparison is `git log` and `git grep` in the tree, which holds no unmerged PR.
- **The listing.** openmrs-core had 352 open PRs. `gh pr list --state open --limit 1000 --json number,baseRefName,changedFiles,files` took 12 to 20 s, and GitHub answered 4 of 7 attempts with HTTP 502. gh lists at most 100 files a PR: PR 4620 changed 102 and gh listed 100, while `gh api repos/openmrs/openmrs-core/pulls/4620/files --paginate` returned all 102. Filtered on `HibernateContextDAO.java` it gives 11 open PRs, and a made-up path gives none.
- **The trigger.** The replacing PR deletes `getUsersLoginAttempts`, which PR 6790's added lines use, and stops using the two lockout user-property keys PR 6790 uses. The second 2.9.x lockout PR deletes four symbols PR 6790 doesn't use and moves no key, so it is only a rebase for whichever lands second.
- **The maintainer's call.** The maintainer closed PR 6790 and left the second 2.9.x lockout PR open, so a review's part is to ask, not to rule.

## The draft and its refuter (2026-11-19)

A fresh refuter checked the first draft of the bullet. It fired both ways between PR 6790 and the second 2.9.x lockout PR, through "rewrites what this PR changes". Its `--search` step had gh's silent default limit of 30. It handed down "waits and is redone on top" where the maintainer made a different call for each of the two PRs. And its privacy clause sat next to the example. The applied wording answers each: the trigger is deleting, renaming or moving what this PR calls, reads or writes; the search is gone; the disposition is a question to the maintainer on the line it breaks; and Step 1's private-ticket rule covers a post about such a PR.

## Candidate lesson

1. Step 1 looks, every round, for an open PR that a maintainer has reviewed, that targets this PR's line or is due for a backport to it, and that deletes, renames or moves what this PR calls, reads or writes, and asks the maintainer whether this PR waits. One record. Applied in pr-review 0.27.0 at the owner's direction.
