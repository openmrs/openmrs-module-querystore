- R1: The agent on criteria 1, 4 and 5 reports that the head misses the ticket's fifth acceptance criterion, "A
  metric/counter can be exposed to track the number of upgraded accounts": there is no counter, field, bean or query
  to read a count from, and the only trace of an upgrade is an INFO log line per upgrade (ac145, "Miss — AC5", which
  it calls blocking; whole, finding 3).
- R2: Your own mutation that removes the `log.error` call in `upgradePasswordHash` leaves the suite green: no test
  asserts the "an error is logged" half of the third acceptance criterion (your own pass).
- R3: A failed UPDATE also silently discards the writes of a caller that called `Context.authenticate` inside its own
  open transaction; no core caller does that (ac23, miss 2; whole, finding 1).
- R4: `authenticate_shouldStillLogInWhenTheUpgradeFails` runs inside the test's own transaction, so it cannot see the
  discarded writes (whole, finding 6).
- R5: The `AND password = :oldHash` guard and its test cover a case the row lock already prevents (whole, finding 4;
  your own pass).
- R6: Raising only `security.argon2.parallelism` never re-encodes existing hashes, because Spring's `upgradeEncoding`
  compares only memory and iterations (ac145, finding 1).
- R7: With the shipped default (no opt-in), SHA-1 rows are never upgraded to SHA-512 (ac145, finding 2).
- R8: After the native UPDATE, the `User` that `authenticate` returns still holds the old `changedBy` and
  `dateChanged` (whole, reasoned, not run).
