# defect note · pr-review rounds missed what a maintainer's changes-requested review raised · openmrs-core PR 6535 at 86b96c7d6f · 2026-11-18
outcome: miss. Attended `pr-review` rounds on 2026-09-22, 2026-11-04, 2026-11-09, 2026-11-10, 2026-11-17 and 2026-11-18 (0.25.1, with an unanchored reviewer agent). The last one reported "needs action: none" and posted nothing. ibacher then requested changes (review 5961392681, 2026-11-18T17:08Z, head 86b96c7d6f) with six comments on five issues:
- r4617894513, `StorageService.java:197`: `@since 3.0.0` is wrong because the ticket is marked for backport.
- r4617836000 and r4617840695, `LocalStorageService.java:102`: `catch (FileSystemException e)` catches more than missing files, including `AccessDeniedException`.
- r4617856131, `:97`: reads try the locations in a different order from `getPath()`. "Why that change?"
- r4617880105, `TextHandler.java:118`: a null `mimeType` throws.
- r4617890396, `MediaHandler.java:65`: streams are not guaranteed closed. Master has the same gap; he wants it fixed here.
written by: hand, in the 2026-11-18 session (13a04911), after the owner asked how the review missed them
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-openmrs-core/13a04911-9d12-40fc-baa9-b655af5ecba7.jsonl; the 2026-11-04 round is e1f2fd6c-39df-41e2-84cf-ca93fa4a8ab1.jsonl in the same folder

## Measured (2026-11-18)

- **The label was never printed.** Step 1's JIRA recipe asks for `fields=summary,description,status,comment`. This run's own fetch added `resolution,fixVersions,updated`. Neither prints labels. The REST read shows TRUNK-6682 with `labels: ['backport', 'community-priority', 'performance']`, and its `updated` is 2026-09-15.
- **Multi-version `@since` is the project's convention.** At master a93b6293c9, `api/src/main/java` has 81 `@since` tags, in 48 files, that list more than one version (`@since 3.0.0, 2.9.0, 2.8.9` 11 times; `@since 2.8.0, 2.7.5, 2.6.16, 2.5.15` 7 times).
- **The `@since` thread was checked by its headline only.** The 2026-09-22 comment r4070873955 asked for "the version this actually lands in, plus whatever maintenance lines it gets backported to". The author's `@since 3.0.0` answered the first half, and later rounds treated the thread as fixed. The 2026-11-09 round suggested `@Deprecated(since = "3.0.0")` itself (r4512249216).
- **The catch was the review's own code.** `catch (FileSystemException e)` came from r4473604840 (2026-11-04), written so that long non-ASCII legacy names fall back to the legacy location. `AccessDeniedException` extends `FileSystemException`, so a permission error at the current location serves the legacy copy whenever one exists. The owner's memory for that round records a refuter of its three findings and an unanchored reviewer. Neither is recorded as testing what else the code block catches.
- **The order mismatch was seen and set aside twice.** The 2026-11-05 round recorded "reads take the new one and purge removes the legacy one. Not realistic", and didn't post it. The 2026-11-18 unanchored reviewer reported it again (its F5), and the report dropped it.
- **The stream leak was seen and set aside.** The 2026-11-18 reviewer reported that master leaks a descriptor in MediaHandler when a module's `getMetadata` throws, and that the PR doesn't. The report filed this as "no regression".
- **The null `mimeType` was never noticed.** `mimeType.equals("application/octet-stream")` is master's `TextHandler.java:91-92`, moved into `setContentMetadata`.

## Why the review missed it

- **Noticing.** Every defect probe compared the PR with base: jetty:run, file-operation counts, the subclass probe, and the reviewer brief's "defects this PR introduces relative to master". A comparison that reports only differences misses a defect present in both trees, and it missed the null `mimeType`. (Corrected 2026-11-18 after the retro's gate: this first said such a defect is "invisible" to the comparison, but the same run's comparison did surface master's MediaHandler leak.)
- **Filing.** Three items were seen and then dropped, each through a different filter: "not realistic" (the order), "no worse than master" (the stream), and a fix checked by its headline (the `@since`). The catch was never refuted because the review wrote it. The label was never fetched.

## Candidate lessons

1. Step 1's JIRA fields leave out `labels` and `fixVersions`, and a `backport` label decides `@since` and whether public behavior may change (r3990860896, r4617894513). One record.
2. Lines a PR moves or rewrites are its code, and a defect found there is filed even when master has it too. This is the second run record for the parked "seen, then set aside" lesson (after R-B, `2026-11-14-…-781-…`). Its REOPEN wording is met: "a run record where a review left a real non-blocking defect in its notes and the maintainer then raised it". The filters differ from R-B's (:163). (Corrected 2026-11-18 after the retro's gate: the item that meets it is the order mismatch, set aside twice and then raised in r4617856131. The stream the reviewer saw, master's leak when `getMetadata` throws, is not shown to be what r4617890396 raises: that streams are not guaranteed to be closed.)
3. A code block the review posts is a proposed change, and needs the same checks as the PR. One record here. The owner's auto-memory (`pr-review-clean-verdict-survives-proposing-fixes.md`) lists earlier rounds where refuting the fix caught its error before posting: PRs 380, 6801 and 6157, REST 778, and cohortbuilder 783. Those are not run records in this store.
