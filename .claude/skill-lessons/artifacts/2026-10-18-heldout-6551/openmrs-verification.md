# Verifying OpenMRS behaviour: what to run, and which readings have proved wrong

Read this before you verify anything in OpenMRS code, core or module. It does not replace running
the thing; it says how to run it. Each item was learned on a real review; the bracket says where.

## The database behind the session

- Inside a test, service getters read the Hibernate session. A delete or a constraint violation does
  not reach the database until the session flushes, so an assertion that "the entity is gone" can
  pass while the statement that would fail was never issued. Call `Context.flushSession()` after the
  call under test whenever the property is about the database (a foreign key, a unique constraint, a
  cascade), and check that the test then fails without the fix. [queue PR 96]
- A flush can also hide a failure: an exception thrown by one save can surface at a later flush, or
  not at all. Assert on the exception where one is expected, not only on the end state. [queue,
  flushsession-hides-a-thrown-save]

## A transaction

- A test normally runs inside one transaction that is rolled back at the end, so commit and rollback
  are invisible by default, but they can be observed. Annotate the test method
  `@Transactional(propagation = Propagation.NOT_SUPPORTED)` so no transaction wraps the call, then check
  the rows with a fresh query afterwards. `TransactionSynchronizationManager.isActualTransactionActive()`
  called from inside the code under test says whether a transaction is really active at that point.
  [queue PR 96]
- Reading the transaction configuration, or printing a service's interceptor chain, shows the order of
  interceptors, not whether a transaction is active. On core 2.7.x, module advice added with
  `Context.addAdvice` and the save/void handlers run by `RequiredDataAdvice` run outside the service's
  transaction, although the configuration reads as if they were inside it. [queue PR 96,
  addadvice-lands-outside-the-transaction]

## An authorization check

- The default test user is a superuser and passes every check. To test a privilege, run as a user who
  holds only what a real caller has: `Context.becomeUser(<a non-admin system id>)`, add exactly the
  caller's privileges with `Context.addProxyPrivilege`, and confirm the call fails without the
  privilege under test and succeeds with it. [queue PR 96]
- `@Authorized`, validation and save/void handlers all run as advice on the service proxy. A path that
  reaches a DAO directly skips every one of them; core does this itself, e.g.
  `VisitServiceImpl.stopVisits` saves through `dao.saveVisit`, so no handler or validator fires. [queue,
  stopvisits-bypasses-save-handlers]

## Ordering

- Handlers tied on `@Handler(order)` (the default is `Integer.MAX_VALUE`) run in `HashMap` iteration
  order over bean names, so adding or removing any handler bean can reorder them. To see both orders,
  change the set of registered beans. [queue, handler-order-is-hashmap-order]
- `PatientServiceImpl.mergePatients` moves visits and encounters to the preferred patient before it
  voids the other, so a void handler for that patient sees children already reassigned. [queue,
  mergepatients-moves-visits-before-voiding]

## Builds that test something other than you think

- After a mutation probe, rebuild before trusting any result: restoring a source file does not restore
  the compiled class. In a module, `-pl integration-tests` without `-am` tests the api jar installed in
  `~/.m2`, not your source. [queue, mutation-probes-need-a-reinstall]
- Module tests build the H2 schema from the mappings, not from `liquibase.xml`, so a broken changeset
  builds green; validate changesets explicitly. [queue, ci-never-parses-liquibase-xml]
- `date_voided` and similar columns are `datetime(0)`: two timestamps taken in the same second compare
  equal after a round trip. [queue, void-stamp-matching-collides-in-one-second]
