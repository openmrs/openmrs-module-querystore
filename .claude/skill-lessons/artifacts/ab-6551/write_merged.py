import json, os

base = "/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-querystore/9f0167e9-18df-4e14-bedd-eab4aa62b9c2/scratchpad/ab-6551"

findings = [
{
 "id": "B1",
 "dimension": "solution fit + correctness",
 "merged_from": ["solution-fit-1", "correctness-1"],
 "file": "api/src/main/java/org/openmrs/api/db/hibernate/HibernateLocationDAO.java",
 "line": 133,
 "disposition": "blocking",
 "claim": "The new user_location cleanup covers only the location passed in, and only its committed rows, so purgeLocation still aborts the transaction on two reachable paths: purging a parent whose descendant is assigned to a user, and purging while a User.locations collection holding that location is loaded in the same session, which is the case the shipped comment declares safe.",
 "failure_mode": "If merged as-is, LocationService.purgeLocation still fails in two of the cases this hunk exists to cover. (a) Purging a parent department throws org.hibernate.exception.ConstraintViolationException on USER_LOCATION FOREIGN KEY(LOCATION_ID) whenever any descendant of it is in user_location, because Location.childLocations is @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true) (Location.java:140) so session.remove(parent) issues a delete for every descendant location row while the native delete above it only clears user_location for location.getLocationId() - and TRUNK-6669's own usage model is that the assigned rows are leaves while the purgeable object is the parent department. (b) If the transaction has initialised User.locations for a user assigned to the purged location, the next flush (an ordinary query in the same transaction, or the commit) throws TransientPropertyValueException 'Persistent instance of org.openmrs.User references an unsaved transient instance of org.openmrs.Location', rolling back the purge and everything else in that unit of work. Both are invisible to CI because the single new test cannot reach either (see B3), and the comment at lines 129-132 tells the next maintainer that (b) is only a stale read.",
 "evidence": "Both cases reproduced at b7cd909d in prepared worktrees, each with a positive control, and reverted (git status --porcelain clean). (a) A fresh parent+child pair with the child assigned to user 1: purging the parent threw 'ConstraintViolationException ... PUBLIC.USER_LOCATION FOREIGN KEY(LOCATION_ID) REFERENCES PUBLIC.LOCATION(LOCATION_ID) (11)' where id 11 is the child, not the purged parent; the same purge with no user assignment passed, so the cascade itself is clean. (Locations 1 and 3 from the standard dataset cannot be used for this - they fail on patient_identifier first.) (b) Reproduced three ways: explicit Context.flushSession() after purge; an ordinary getAllLocations() triggering auto-flush; and the PR's own new test body verbatim plus one trailing ls.getAllLocations(), where the PR assertion passes and the trailing query throws. Isolated to the right cause: with the native delete temporarily commented out the same exception still fires, so it comes from session.remove(location) with a managed User.locations still referencing it, not from the native query; and the analogue against Location.childLocations (load parent, initialise children, purge a child, flush) completes cleanly, so this is new, not pre-existing. I re-read Location.java:136-144 and HibernateLocationDAO.deleteLocation at the head sha to confirm the cascade attributes and that the native delete precedes session.remove. Recommended default, verified end to end by one of the lenses: replace the native delete with an HQL load of the users holding the location and remove it from each managed collection ('select u from User u join u.locations l where l.locationId = :locationId', then u.getLocations().remove(location)) before session.remove - that made (a-style) committed-row purges, all three (b) repros and all four LocationServiceTest#purgeLocation* tests pass with one mechanism and no raw SQL - and run it over the location's descendants, for which this class already has the recursive-CTE getDescendantIds query (~line 470). Putting onDelete=\"CASCADE\" on the user_location_to_location FK in this PR's own unshipped changeset (precedented in liquibase-update-to-latest-2.4.x.xml and 2.5.x) also covers (a) and lets the native delete and its caveat go, but it does not cover (b), which is a Hibernate-level check independent of the DB FK.",
 "confidence": "high"
},
{
 "id": "B2",
 "dimension": "security",
 "merged_from": ["security-1"],
 "file": "api/src/main/java/org/openmrs/api/impl/UserServiceImpl.java",
 "line": 856,
 "disposition": "blocking",
 "claim": "getAllowedLocations is gated only by GET_LOCATIONS, which core's own shipped data grants to the Authenticated role, and it resolves the target user through dao.getUser(user.getUserId()), an unadvised @Repository call, so the GET_USERS privilege that gates every other User read on UserService never fires.",
 "failure_mode": "If merged as-is, any authenticated user can read any other user's stored location assignments by passing a bare new User() with a chosen userId, and iterate userIds to enumerate the lot - in the deployments this feature targets that is the staff-by-department/ward roster. AuthorizationAdvice only checks the declared GET_LOCATIONS, which core-data grants to role 'Authenticated', and the User row is then fetched through HibernateUserDAO, which AOPConfig.createAdvisor does not intercept, so UserService.getUser's GET_USERS gate is bypassed. Nothing throws and nothing is logged, so the disclosure is invisible; and once webservices.rest#762 and esm-login-app depend on this signature, tightening the privilege becomes a breaking API change rather than a one-line edit.",
 "evidence": "Ran in the worktree at b7cd909d: a throwaway probe created a fresh non-superuser with no privilege-bearing role and printed 'authenticated=probecaller isSuperUser=false hasGetUsers=false hasGetLocations=true'. Positive control in that same context: userService.getUser(1) threw APIAuthenticationException, so the GET_USERS gate genuinely is unsatisfied for this caller. Then userService.getAllowedLocations(u, tag) with u = new User(); u.setUserId(1); returned user 1's persisted assignment, [Xanadu]. I re-verified all four mechanism facts at the head sha: UserServiceImpl.java:856 @Authorized(PrivilegeConstants.GET_LOCATIONS) and :860 dao.getUser(user.getUserId()); UserService.java:86 @Authorized({ PrivilegeConstants.GET_USERS }) User getUser(Integer); HibernateUserDAO.java:59 @Repository(\"userDAO\"); AOPConfig.java:117 targetClass.isAnnotationPresent(Service.class) - so DAO calls carry no authorization advice. Blast radius taken from core's shipped data rather than assumed: the single role_privilege row for 'Get Locations' in liquibase-core-data-2.9.x.xml grants it to role 'Authenticated'. Sibling check: every other UserService method that resolves a User from the datastore requires GET_USERS; the two that are only @Authorized() (getLastLoginTime, getDefaultLocaleForUser) read fields off the object the caller already holds rather than re-reading the row, so they are not precedent. Fix, mirroring the in-tree guard at UserServiceImpl.java:431 (setUserProperty): allow the call for the authenticated user's own record and require GET_USERS otherwise. If done by annotation it must be @Authorized(value = {GET_USERS, GET_LOCATIONS}, requireAll = true) - Authorized.requireAll() defaults to false, so simply adding GET_USERS to the array would weaken the check rather than strengthen it. One caveat on the probe: GET_LOCATIONS reached the caller via Context.addProxyPrivilege because a role created in the same test transaction did not populate the privilege cache; Context.hasPrivilege treats proxy and role privileges identically, and the load-bearing half - GET_USERS absent from the caller's role graph - was proved directly by the positive control.",
 "confidence": "high"
},
{
 "id": "B3",
 "dimension": "test coverage + correctness",
 "merged_from": ["test-coverage-1", "correctness-2"],
 "file": "api/src/test/java/org/openmrs/api/LocationServiceTest.java",
 "line": 559,
 "disposition": "blocking",
 "claim": "purgeLocation_shouldDeleteLocationAssignedToAUser is the only test for the new user_location cleanup and it passes verbatim when the cleanup is deleted: the assertion reads through session.get, which returns null for a session-removed entity with no DELETE issued, and the transaction is rolled back before any flush, so the foreign key is never evaluated.",
 "failure_mode": "If merged as-is the join-row cleanup ships unguarded, and so does whatever replaces it: two reviewers independently deleted the two added lines in HibernateLocationDAO.deleteLocation and this test stayed green, so any later edit that drops the cleanup or moves it after session.remove ships green too, and purgeLocation then fails at commit with a referential-integrity violation on user_location(location_id) in any deployment with populated user_location - a break CI cannot see, so the cost of discovery lands on whoever purges a location. It also means the repair B1 asks for cannot be demonstrated by the suite.",
 "evidence": "Mutation run twice, independently: with the native 'delete from user_location where location_id = :locationId' and its setParameter removed, mvn -o -pl api test -Dtest='LocationServiceTest#purgeLocation*' is 4/4 green, BUILD SUCCESS. Mechanism confirmed with a probe in the same class: purgeLocation -> deleteLocation -> session.remove(location), and the assertion at line 559 goes through HibernateLocationDAO.getLocation(Integer) = session.get, which returns null for a removed entity with no SQL DELETE; BaseContextSensitiveTest then rolls back. A discriminating shape exists and was run: save the assignment, Context.flushSession(), Context.clearSession(), reload the location, purgeLocation, Context.flushSession() - succeeds with the fix present and throws 'org.h2.jdbc.JdbcBatchUpdateException: Referential integrity constraint violation: PUBLIC.USER_LOCATION FOREIGN KEY(LOCATION_ID) REFERENCES PUBLIC.LOCATION(LOCATION_ID) (4)' without it; asserting the join row directly (select count(*) from user_location) also discriminates, 1 -> 0. For whoever writes the repair: the clearSession/reload step is not cosmetic - adding a bare Context.flushSession() to the test as written throws TransientPropertyValueException even WITH the fix present, because the test still holds the session-cached User referencing the removed Location, which is case (b) of B1.",
 "confidence": "high"
},
{
 "id": "B4",
 "dimension": "solution fit + performance",
 "merged_from": ["solution-fit-2", "performance-1", "performance-4"],
 "file": "api/src/main/java/org/openmrs/api/impl/UserServiceImpl.java",
 "line": 865,
 "disposition": "suggestion",
 "claim": "The intersection is computed in Java on top of getLocationsByTag - the one tag query in this codebase that loads every unretired location and filters in memory - and on top of an unconditional re-read of the whole User/Person graph, on what TRUNK-6669 makes a per-login path; the DAO this PR edits already answers the tag half in one statement, and a single query joining l.tags with the optional user_location restriction, ordered by name, would answer both halves and remove the re-read.",
 "failure_mode": "",
 "evidence": "Measured by two lenses independently, not inferred. With 253 non-retired locations and the User detached exactly as Context.getAuthenticatedUser() hands it to a request: getAllowedLocations for a restricted user = 9 statements for a result of size 1 (users+person join, person_name, person_attribute, person_address, 'select ... from location' with all 253 rows hydrated, 3x location_tag_map at 100 binds each, then user_location), 12 for the unrestricted user; getLocationsByTag alone = 4; getLocationsHavingAllTags([tag]) = 1 statement for the same tagged locations. On the 6-location LocationServiceTest dataset the same comparison with Hibernate statistics is stmts=2/entities=11 versus stmts=1/entities=1. The cost grows as 1+ceil(N/100) statements and N hydrated entities in the total location count rather than the result count, because LocationServiceImpl.getLocationsByTag (lines 213-223) loops dao.getAllLocations(false) and filters on l.getTags().contains(tag) in Java, and Location.tags carries @BatchSize(100). The re-read at line 860 is a plain session.get that pulls users+person plus the three Person collections (User.person is EAGER) to reach only persisted.getLocations(). Sibling and direction evidence: the endpoint TRUNK-6669 says the frontend will replace, GET /ws/rest/v1/location?tag=Login+Location, calls getLocationsHavingAllTags; the two commits immediately preceding this PR on this same DAO are the ones that built and then tuned LocationSearchCriteria (TRUNK-6635, TRUNK-6681), which does the tag filter in SQL with join/groupBy/having, name ordering and paging; a dormancy check with a positive control found getLocationsByTag had zero production callers at the merge base. Consumer: webservices.rest#762's LocationForCurrentUserSearchHandler3_0 calls this with Context.getAuthenticatedUser() and wraps the result in NeedsPaging, i.e. it pages in memory after the whole table has been hydrated, on every login. One caveat for whoever fixes it: getAllLocations(false) orders by name and getLocationsHavingAllTags does not, so a naive swap silently drops the picker's ordering - which is why the recommendation is one DAO query rather than a substitution. Not blocking: the fetch-all shape is inherited from the endpoint this replaces rather than introduced here, and the public signature does not need to change to fix it later.",
 "confidence": "high"
},
{
 "id": "B5",
 "dimension": "test coverage + correctness",
 "merged_from": ["test-coverage-2", "correctness-5"],
 "file": "api/src/main/java/org/openmrs/api/impl/UserServiceImpl.java",
 "line": 860,
 "disposition": "suggestion",
 "claim": "If the re-read stays, nothing pins it and its comment misdescribes it: all five new tests pass with dao.getUser(user.getUserId()) replaced by the caller's own object, and 'whatever the caller passed in' is not what happens - for a session-attached User the caller's in-memory set is what gets intersected, so attached and detached callers get different answers for the same logical input.",
 "failure_mode": "",
 "evidence": "Mutation: with dao.getUser(user.getUserId()) replaced by user, mvn -o -pl api test -Dtest='UserServiceTest#getAllowedLocations*' is 5/5 green, so the only thing asserting the re-read is a code comment. What it actually buys, measured with two probes (tag locations 1 and 2, assign 2, flush+clear): getAllowedLocations(new User(1), tag) returns [2] with the re-read and [1, 2] without it - a synthetic User silently receives every tagged location for a user restricted to one, the exact over-permission the feature exists to prevent, with nothing thrown for CI to catch - and getAllowedLocations(Context.getAuthenticatedUser(), tag) returns [2] with it and throws LazyInitializationException without it. Either probe, added as a test, is the missing guard. Attachment sensitivity, two probes in separate transactions with user 1 holding no committed assignments: attached (u = us.getUser(1); u.setLocations({location 2})) returns 1 result, because dao.getUser hands back the same managed instance so the caller's unsaved set is what gets intersected, and getLocationsByTag's query auto-flushes it to user_location on the way; detached (evict before setLocations) returns 2, all tagged. No consumer in this PR is affected - the REST path loads the user and does not modify it - so this is about the comment being load-bearing documentation for the method's most unusual choice. Note the dependency on B4: if the restriction folds into one userId-keyed DAO query the re-read and its comment disappear and this dissolves; if the re-read stays, it needs the guard test and a comment that says what it does.",
 "confidence": "high"
},
{
 "id": "B6",
 "dimension": "correctness + test coverage",
 "merged_from": ["correctness-3", "test-coverage-5"],
 "file": "api/src/main/java/org/openmrs/User.java",
 "line": 372,
 "disposition": "suggestion",
 "claim": "getLocations(), the getter the new field javadoc points readers at and which is documented as 'empty when the user is unrestricted', throws LazyInitializationException on a detached User - including the Context.getAuthenticatedUser() instance a login-location consumer is most likely to call it on - and it has no test; roles, the sibling this collection is modelled on, is EAGER and behaves differently.",
 "failure_mode": "",
 "evidence": "Probe: load user 1, assign a location, flush+clear, reload, read getRoles().size() (works, EAGER), Context.evictFromSession(user), then getLocations() -> 'org.hibernate.LazyInitializationException: Cannot lazily initialize collection of role org.openmrs.User.locations with key 1 (no session)'. UserContext.getAuthenticatedUser() (UserContext.java:211-213) returns the stored field verbatim with no reload and refreshAuthenticatedUser() is only called explicitly, so the authenticated User is detached from the current request's session. UserServiceImpl.getAllowedLocations sidesteps this by re-reading the user by id, so the service API is safe and the new public getter is not; coverage of the collection today runs only through that method, so the getter's documented contract on a detached instance is never exercised. Recommended default: say in the javadoc (User.java:101-107 and :368-371) that the getter is valid only inside a session and point callers at getAllowedLocations, rather than switching to EAGER - EAGER would load the collection for every User everywhere and cuts against the batching direction in B9. If detached access is meant to be in contract instead, that needs EAGER (or an explicit fetch) plus a test.",
 "confidence": "high"
},
{
 "id": "B7",
 "dimension": "test coverage",
 "merged_from": ["test-coverage-3"],
 "file": "api/src/main/java/org/openmrs/User.java",
 "line": 110,
 "disposition": "suggestion",
 "claim": "@Independent on the new collection is load-bearing and has no test, while the repo already ships exactly this test for both of the other two @Independent fields.",
 "failure_mode": "",
 "evidence": "Measured: a probe that assigns locations 1 and 2 to user 1 then calls userService.retireUser(user, reason) prints 'loc1 retired=false loc2 retired=false' as shipped and 'loc1 retired=true loc2 retired=true' with @Independent removed - i.e. without the annotation, retiring a user retires every location that user is assigned to (User implements Retireable, RequiredDataAdvice.before:181 calls recursivelyHandle(RetireHandler.class, ...), and the @Independent skip is at RequiredDataAdvice.java:306). With it removed, mvn -o -pl api test -Dtest='UserServiceTest,LocationServiceTest,UserTest,HibernateUserDAOTest' still reports 223/223 green, so nothing in the User/Location area detects the removal. The project's own precedent is two tests of exactly this shape: LocationServiceTest:1218 retireLocation_shouldNotRetireIndependentField (Location.tags) and OrderServiceTest:3316 retireOrderType_shouldNotRetireIndependentField (OrderType.conceptClasses); a retireUser_shouldNotRetireIndependentField mirroring them is four lines. Scope caveat: the removal was checked against those four classes, not the full api suite.",
 "confidence": "high"
},
{
 "id": "B8",
 "dimension": "project conventions",
 "merged_from": ["conventions-1"],
 "file": "api/src/main/java/org/openmrs/User.java",
 "line": 380,
 "disposition": "suggestion",
 "claim": "The new collection is the only @ManyToMany on a core domain class with no add/remove mutators, and it follows neither of the two shapes its siblings use to guarantee non-nullness, so the field javadoc's 'an empty set means the user is unrestricted' contract is not actually enforced - setLocations(null) puts the field back to null.",
 "failure_mode": "",
 "evidence": "Enumerated every @ManyToMany in the domain package at the head sha: exactly five (ConceptName:100, Location:145, OrderType:64, User:95 roles, User:108 locations). All four pre-existing ones carry mutators - User.addRole/removeRole (:343, :361, both null-tolerant), Location.addTag/removeTag (:582, :597), ConceptName.addTag/removeTag (:406, :424), OrderType.addConceptClass (:196). The closest structural sibling, OrderType.conceptClasses (also @Independent + @ManyToMany + @JoinTable with no cascade), does both: it lazy-initialises inside the getter (OrderType.java:159-163) and exposes addConceptClass; the same lazy-init-getter shape is in Person.getNames and Concept.getNames. The new accessors instead return and assign the field directly (User.java:372-374 and :380-382), which is why UserServiceImpl.getAllowedLocations still has to defend with 'assigned == null' (line 868) despite the javadoc promise. Cost visible inside this PR: LocationServiceTest:554 has to write user.setLocations(new HashSet<>(Collections.singletonList(locationToDelete))) for want of an addLocation, and the companion REST subresource described in the PR body (POST/DELETE one location at a time) repeats that copy-mutate-set on every call.",
 "confidence": "high"
},
{
 "id": "B9",
 "dimension": "performance",
 "merged_from": ["performance-2"],
 "file": "api/src/main/java/org/openmrs/User.java",
 "line": 108,
 "disposition": "suggestion",
 "claim": "User.locations is a lazy @ManyToMany with no @BatchSize, so initialising it across n Users costs n separate 'select ... from user_location where user_id = ?' queries; both collections on Location already carry @BatchSize(size = 100).",
 "failure_mode": "",
 "evidence": "Measured both ways with a positive control: loading UserService.getAllUsers() (3 users) after flush+clear and touching getLocations() on each issues 3 user_location statements with 1 bind param each; with @BatchSize(size = 100) temporarily added to the field, exactly 1 statement with 100 bind params (User.java restored afterwards, grep -c BatchSize back to 0). Sibling precedent in the file this mapping was modelled against: Location.tags and Location.childLocations both carry @BatchSize(size = 100) (Location.java:141, :147). Named consumer: webservices.rest#762 adds locations to UserResource3_0's FullRepresentation, so GET /ws/rest/v1/user?v=full over n users pays n extra queries.",
 "confidence": "high"
},
{
 "id": "B10",
 "dimension": "security",
 "merged_from": ["security-2"],
 "file": "api/src/main/java/org/openmrs/User.java",
 "line": 102,
 "disposition": "suggestion",
 "claim": "Because an empty set means unrestricted, any path that empties a user's assignment set silently converts that user from restricted to unrestricted - purging a location does exactly that, and it leaves no trace: the executeUpdate() row count is discarded, user_location has no audit columns, and nothing is logged. Neither this javadoc nor the service javadoc says so.",
 "failure_mode": "",
 "evidence": "Ran: a probe tagged locations 1 and 2 as 'Login Location', created and tagged a third, assigned user 1 to that third location alone and printed 'before=[Probe Ward]'; after locationService.purgeLocation on it, a re-read printed 'after=[Unknown Location, Xanadu]' - one allowed location became every tagged location in the system, through the fail-open branch at UserServiceImpl.java:868-869. HibernateLocationDAO.java:133-134 discards the executeUpdate() count; the new changeset creates user_location with no audit columns; git grep user_location over api/src/main and omod/src shows nothing logs or records the deletion. retireLocation (LocationServiceImpl.java:257) does not touch user_location, so only purge does this. Not blocking: fail-open on an empty set is the ticket's explicit design ('the caller always receives a usable list'), purgeLocation requires PURGE_LOCATIONS, and denying instead would lock the user out. Concrete ask: state the consequence where an implementer reads the contract - this javadoc says an empty set means unrestricted but not that any path which empties the set therefore grants every tagged location - and keep the count from executeUpdate() to log at WARN when it is non-zero, naming the location, so an admin purge that un-restricts users is traceable. If B1 is resolved by putting onDelete=\"CASCADE\" on the FK instead, the count is no longer available in Java and the javadoc half is what remains.",
 "confidence": "high"
},
{
 "id": "B11",
 "dimension": "solution fit + security",
 "merged_from": ["solution-fit-3", "security-3"],
 "file": "api/src/main/java/org/openmrs/api/UserService.java",
 "line": 669,
 "disposition": "question",
 "claim": "Nothing in core enforces the restriction - this is a read filter for a picker - but the PR title ('restricting users to assigned locations') and the javadoc ('the locations the given user is allowed to use') both read as enforcement. Is advisory-only the intent, and if so is it worth one javadoc sentence saying this scopes a picker rather than being an access check, so module authors do not build authorization on it?",
 "failure_mode": "",
 "evidence": "Verified by positive search rather than a zero-hit, by two lenses. git grep getAllowedLocations over the tree returns only the declaration (UserService.java:669), the impl, the User javadoc reference and the new tests - no in-core consumer; git grep 'getLocations()' over api/src/main/java returns 8 hits, so the pipeline finds real matches, and UserServiceImpl.java:867 is the only reader of User.locations among them. UserContext.setLocation(Location) and setLocationId(Integer) are public and unguarded (UserContext.java:578, :597) and consult nothing; LocationService.getLocationsByTag, the call the current esm-login-app reaches through /ws/rest/v1/location?tag=Login+Location, is unchanged and still returns every tagged location to every GET_LOCATIONS holder; no encounter, visit or login path consults User.locations. So a restricted user can still obtain and submit any location. TRUNK-6669's acceptance criteria are all about what the GET returns, so the PR matches the ticket and neither lens is asking for enforcement here - the ask is the one javadoc sentence. Worth answering in the same breath: the PR adds no privilege for managing assignments, so writing User.locations rides EDIT_USERS through saveUser with no self-assignment guard, where role assignment on that same method is additionally constrained by checkPrivileges (UserServiceImpl.java:407-416); that asymmetry is defensible exactly as long as locations are not enforced, which is why the answer to this question also settles how heavily B10 should be read.",
 "confidence": "high"
}
]

dropped = [
 {"id": "performance-3",
  "why": "The @Cache asymmetry with User.roles is real, but the remedy is unverified by its own lens ('I did NOT measure that adding @Cache actually produces an Infinispan hit') and the correctness lens found the absence load-bearing in the opposite direction: because the collection is not second-level cached, the new native delete in deleteLocation leaves no stale cache entry. Recommending @Cache would add a stale-cache failure mode to the very hunk B1 already asks to rework."},
 {"id": "correctness-4",
  "why": "Medium confidence and the closest thing in the correctness set to taste (throw IllegalArgumentException versus return empty). No caller anyone reaches passes a null user or tag - the REST consumer always supplies both - so the ask changes no reachable behaviour, and the underlying question of what this new contract promises is already put to the author in B11."},
 {"id": "test-coverage-4",
  "why": "The behaviour it would pin already works with no production change (Hibernate deletes the owning side's join rows with the User, probed green), so it guards a hypothetical future mapping change. With three test asks already on the author (B3, B5, B7) this is the one that buys least."},
 {"id": "test-coverage-6",
  "why": "Its own lens proved the intersection is genuinely covered (replacing the filter with 'return tagged' fails 2 of the 5 new tests), leaving only a request that one test be more self-describing. Below the attention bar for a review that already carries three blockers."},
 {"id": "conventions-2",
  "why": "Three lenses noticed the ':' + LOCATION_ID splice (one as a nit, two as style asides) and the loss of a greppable query string is real, but it is cosmetic next to B1 and B3 on the same five lines, and the author is already rewriting that statement."},
 {"id": "conventions-3",
  "why": "The misleading comment above CHANGE_SET_COUNT_FOR_GREATER_THAN_2_1_X is pre-existing and about a different thing than what the PR changes on that line (the count itself), and the lens could not check the constant's history (shallow clone). Not worth a comment."},
 {"id": "conventions-4",
  "why": "Self-refuting as a convention claim: the lens found a counter-instance in the same sibling file it cites (HibernateConceptDAO.java:238 uses createNativeQuery for an INSERT), so there is no project rule to appeal to."}
]

downgraded = [
 {"id": "test-coverage-2", "from": "blocking", "to": "suggestion",
  "why": "The code as shipped is correct - the re-read is present and does its job. The failure mode the lens wrote out (silent over-permission for a synthetic User, LazyInitializationException for the authenticated User) only materialises if a later edit removes the line, and if B4's single-query fix is taken the line disappears by design. 'If merged as-is, X breaks' cannot be written for it, so it ships as a strong suggestion carrying its own mutation evidence (now B5)."}
]

merged_pairs = [
 ["solution-fit-1", "correctness-1"],
 ["test-coverage-1", "correctness-2"],
 ["solution-fit-2", "performance-1", "performance-4"],
 ["test-coverage-2", "correctness-5"],
 ["correctness-3", "test-coverage-5"],
 ["solution-fit-3", "security-3"]
]

noticed = [
 "The new changeset is the only one in liquibase-update-to-latest-3.0.x.xml with a preConditions block, and MARK_RAN on tableExists means a distro that already has a user_location table silently skips it and can end up with a schema that does not match the mapping (solution-fit out_of_scope; no lens developed it).",
 "purgeRole has the identical latent FK problem for user_role and does nothing about it (HibernateUserDAO.deleteRole is a bare session.remove), so this PR's location-side handling is a new pattern rather than a mirrored sibling - which shape core standardises on is a decision no lens raised as a finding (correctness out_of_scope).",
 "deleteLocation fixes one location FK of several: a probe's purgeLocation(3) failed on PATIENT_IDENTIFIER FOREIGN KEY(LOCATION_ID), so purging a referenced location is still generally unsupported (performance out_of_scope). Context for B1's scope rather than a finding.",
 "user_location has no standalone index on location_id (the composite PK leads with user_id), which this PR's own 'delete from user_location where location_id = ?' scans (solution-fit out_of_scope).",
 "The changeset ships a composite (user_id, location_id) primary key where TRUNK-6669's proposed DDL specifies a user_location_id surrogate key. Three lenses noticed the divergence and each judged the composite equal or better (it matches the user_role sibling), so none raised it.",
 "getAllowedLocations returns the live list from getLocationsByTag in the unrestricted branch and a freshly collected list in the restricted branch, so callers get differently-owned lists from the same method (security out_of_scope).",
 "saveUser accepts a Set<Location> containing a null element without complaint and silently persists nothing for it (security out_of_scope, observed incidentally).",
 "getAllowedLocations was added to the UserService interface with no default method, breaking any out-of-tree implementor, and arguably belongs on LocationService rather than UserService (correctness out_of_scope).",
 "The new changeset's DDL is proven on H2 only - the composite addPrimaryKey and the two RESTRICT FKs were not exercised on MySQL or PostgreSQL by any lens (three could_not_verify entries agree on this); the CI matrix is where that gets covered.",
 "No lens read the PR's review conversation, by instruction, so any of the above - and any finding in this review - may already have been raised and answered in a prior round."
]

out = {
 "findings": findings,
 "dropped": dropped,
 "downgraded": downgraded,
 "merged_pairs": merged_pairs,
 "merger_noticed_but_did_not_add": noticed,
 "verdict": "Needs work: the user_location cleanup still aborts purgeLocation on two reachable paths and its only test provably cannot fail, and getAllowedLocations lets any authenticated user read another user's location assignments.",
 "notes": "Five of the six lenses converged on the five-line deleteLocation hunk, and the strongest signal in the set is that four of them independently reproduced the TransientPropertyValueException that the hunk's own comment declares impossible ('Safe today since no caller holds a loaded User across this call') - correctness as a finding, security, test-coverage and performance as out-of-scope notes. That is why B1 carries both failure cases rather than only solution-fit's descendant-cascade one: the two lenses attacked the same hunk from different ends (does the fix cover the ticket's usage model; does it introduce a new failure) and their fixes converge on one mechanism, mutating the managed collections over the location and its descendants. Three lenses independently mutation-tested the new purge test and got the same result, green with the production fix deleted, so B3 is as well-established as a coverage finding gets; correctness labelled it a suggestion and test-coverage blocking, and I kept it blocking, because B1's repair cannot be demonstrated by a test that cannot fail. Solution-fit and performance reached the same conclusion about getLocationsByTag from opposite directions - sibling/approach comparison versus measured statement counts - and performance's own re-read finding folded into the same comment because one DAO query fixes all three, so B4 merges three lens findings. One genuine contradiction needed adjudication: performance wanted @Cache on the new collection while correctness had positively established that the absence is what keeps the native delete from leaving a stale cache entry, and performance had not measured the remedy - dropped. One genuine tension was resolved rather than dropped: performance-4 wants the re-read gone while test-coverage-2 proves it load-bearing for the detached authenticated User; a userId-keyed DAO query satisfies both, which is what B4 recommends and what B5 records the dependency on. Solution-fit and security arrived independently at the same question and asked for the same javadoc sentence (B11). Two upgrades, since the schema has no field for them: correctness-2 suggestion -> blocking (inside B3) and test-coverage-5 question -> suggestion (inside B6, where correctness-3 already carried the concrete ask). Security was the narrowest lens - two passes, three findings - but produced the single highest-value finding, and it was the only one to trace the privilege gate end to end through AOPConfig; I re-verified its four mechanism facts in the worktree before keeping it blocking. Conventions was the quietest in substance: one suggestion and three nits, with its own report noting that most of its effort went into killing about twenty candidate findings as precedented, and it was the only lens that ran no build (it says so, and its surviving finding is a source read that does not need one). All six lenses independently reported that the brief's JDK 8 instruction is wrong for this repo (maven.compiler.release=21); five ran real tests on JDK 21 with narrow -Dtest selections and all reported clean worktrees afterwards, which I confirmed."
}

path = os.path.join(base, "arm-B-merged.json")
with open(path, "w") as f:
    json.dump(out, f, indent=2)
print("wrote", path)
print("findings:", len(out["findings"]))
print("dropped:", len(out["dropped"]))
print("merge groups:", len(out["merged_pairs"]), "consuming", sum(len(g) for g in out["merged_pairs"]), "lens findings")
by_disp = {}
for f_ in findings:
    by_disp[f_["disposition"]] = by_disp.get(f_["disposition"], 0) + 1
print("by disposition:", by_disp)
