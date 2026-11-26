I'd file two of the eight, R2 and R1, and set the other six aside. Nothing was built and the working copy is unchanged (`git status --porcelain` is empty).

**Filed**
- **R2, as a suggestion** on `ContextDAOTest.java:502` (`authenticate_shouldStillLogInWhenTheUpgradeFails`). The criterion says "an error is logged", and my own mutation pass showed that no test checks it. There is already a way to check it in core: `AuthorizationAdviceHierarchyTest` attaches a `MemoryAppender` to a logger and asserts on what it captures. Because the comment names that way to fix it, the method wants a blind agent to read it before it is posted. This brief allows no agents, so that check is **still owed**.
- **R1, as a question** on `HibernateContextDAO.java:382`, the `log.info("Successfully upgraded …")` line.
  - I checked the agent's claim. Nothing in the change counts upgrades, and no pom has a metrics library: searching for micrometer, dropwizard, simpleclient and prometheus found nothing, while a control search on the same poms did find matches.
  - The shipped `webapp/src/main/resources/log4j2.xml:56` sets `org.openmrs.api` to INFO, so that log line is printed and the upgrades can be counted from the log.
  - The PR only says "see TRUNK-6666", and the ticket's own wording is "can be exposed". Whether the log line is enough, or the counter moves to another ticket, is for the ticket owner to decide. So this is a question: I can't name anything that breaks, which the method needs for a blocking comment.

**Set aside**
- **R3:** this case needs two things at once:
  - an UPDATE that fails, which the thread ruled out: argon2 is the only encoder that can be configured (`applicationContext-service.xml:55`), and its hashes are at most 126 characters against a 128-character column;
  - a caller that runs `Context.authenticate` inside its own transaction. The only core caller is `InitializationFilter.java:1839`.

  So it adds nothing real to the trade-off you accepted when you chose the inline upgrade.
- **R4:** the only thing such a test could pin is the accepted loss of the login's writes. A test that locks in a known downside guards nothing.
- **R5:** the guard is not shown to be dead code.
  - `authenticate` runs with `READ_COMMITTED`, but that setting is ignored when the login joins a caller's transaction.
  - Under MySQL's default `REPEATABLE READ`, the plain `select password, salt` that follows the `for update` lock can then return an older snapshot.
  - In that case `AND password = :oldHash` is the only thing stopping the old password's hash from overwriting a newer one.

  This is reasoned, not run, because it needs MySQL access, so asking for the guard's removal isn't justified.
- **R6:** Spring's `upgradeEncoding` behaves this way on purpose, and ibacher's thread comment told the author to rely on it. The encoder settings belong to TRUNK-6663.
- **R7:** the ticket only asks for upgrades to Argon2id. With the shipped default (`idForEncode = ""`) there is nothing to upgrade to, and that opt-in design was decided before this PR.
- **R8:** `User` doesn't map the `password` column (`User.java` has no field for it), so a later save can't put the old hash back. On a later save, `AuditableInterceptor` (lines 152–153 and 193–194) sets `changedBy` and `dateChanged` afresh, so the out-of-date values are never written. They are only out of date in memory.

The method's round-record append under `~/.claude` was not done, because the hard constraints rule out touching that folder.

{"filed": [{"item": "R2", "disposition": "suggestion", "first_line": "Could this test also check that the failure is logged? That's the last part of the ticket's third criterion, and removing the `log.error` call in `upgradePasswordHash` leaves the suite green; `AuthorizationAdviceHierarchyTest` already captures a logger with `MemoryAppender`."}, {"item": "R1", "disposition": "question", "first_line": "Is the ticket's last criterion, a counter of upgraded accounts, meant for another ticket? This INFO line is the only trace an upgrade leaves and nothing counts them, so if the log line is enough, could the ticket say so before it closes?"}], "set_aside": [{"item": "R3", "reason": "It needs both an UPDATE failure the thread already ruled out for core's only configurable encoder (argon2, at most 126 characters into 128) and a caller that authenticates inside its own transaction, which no core code does (the only caller, InitializationFilter.java:1839, is not transactional), so it adds nothing real to the trade-off the inline decision accepted."}, {"item": "R4", "reason": "The only thing such a test could pin is the accepted loss of the login's writes, and a test that locks in a known downside guards no guarantee."}, {"item": "R5", "reason": "The guard is not shown dead: when authenticate joins a caller's transaction under MySQL's default REPEATABLE READ, the plain password select after the for-update lock can read an older snapshot, and the guard is then the only thing stopping the old password's hash from overwriting a newer one."}, {"item": "R6", "reason": "This is Spring's Argon2PasswordEncoder.upgradeEncoding working as designed, which the thread told the author to rely on, and the encoder settings belong to TRUNK-6663, not this ticket."}, {"item": "R7", "reason": "The ticket only asks for upgrades to Argon2id, and with the shipped default (idForEncode empty) there is nothing to upgrade to, an opt-in design decided before this PR."}, {"item": "R8", "reason": "User does not map the password column, and AuditableInterceptor sets changedBy and dateChanged afresh on any later save, so the out-of-date values are only in memory and are never written."}]}
