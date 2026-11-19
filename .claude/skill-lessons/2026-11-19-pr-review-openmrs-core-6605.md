# openmrs-core #6605 round 4 (head 3d1d28211f): a runtime A/B whose arms started differently

- **PR, round, head:** https://github.com/openmrs/openmrs-core/pull/6605 (TRUNK-6415, distributed HTTP session), the
  user's fourth review round, head 3d1d28211fe7a525cd0e2c0163b689872eb27706. Posted as review 5973579755 plus replies
  r4627624776 and r4627625369.
- **What was wrong (caught before posting):** the evidence for the legacyui `CurrentUsers` finding compared a flag-on
  arm that was the setup wizard's FIRST start with a flag-off arm that was a normal restart. On the first start,
  legacyui's `WebComponentRegistrar.setServletContext` throws in `addMappings` ("can't be modified after the context
  has been initialised"), so it never reaches `servletContext.addListener(new SessionListener())`, whatever the flag.
  The refuting agent found it in `catalina-on.log`. A clean flag-on restart then reproduced the finding (live
  `HttpSessionWrapper` 2 -> 22 after 20 legacy-UI login/logout cycles; Logged In Users kept the logged-out user;
  flag-off control 10 -> 10 `StandardSession`, user removed).
- **Seen and set aside, or never noticed:** never noticed. Each arm's log was grepped for the module-start and
  feature lines only, never for exceptions.
- **The rule that should have caught it:** none states it. Nearest: the user's "calibrate an ad-hoc measurement
  against a known-good and a known-bad case", which the flag-off arm only half satisfied. Candidate rule: both arms of
  a runtime A/B start the same way (never a setup-wizard first start against a restart), and each arm's log is
  checked for exceptions before the two are compared.
- **Second item, the refuter overreached:** it called the suggested remedy (purge on `ClassNotFoundException` /
  `ObjectStreamException`) broken because the purge is cluster-wide and "node A can still read the session". That
  held for a bare `findById` only. A real request through `SessionRepositoryFilter` on node A saves
  `lastAccessedTime`, a `compute` node B must deserialize, so it failed with B's `RemoteException` during the
  mixed-version window (same-class control passed). Candidate rule for refuter briefs: ask for the attack to be run
  on the real request path, not on the repository call alone.
- **Session:** 8820e7ac-2fe7-42a3-a25b-7a5f033b1f2b
