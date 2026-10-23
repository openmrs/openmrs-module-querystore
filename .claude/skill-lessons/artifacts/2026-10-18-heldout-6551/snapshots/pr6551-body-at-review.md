## Description of what I changed

Adds `User.locations` (many-to-many, new `user_location` join table) and `UserService.getAllowedLocations(User, LocationTag)`.

An unrestricted user (no assigned locations) gets every location carrying the tag; otherwise the result is the intersection of tagged locations and the user's assigned locations.

Follow-up fixes from review:
- `HibernateLocationDAO.deleteLocation` now removes the user's `user_location` rows before deleting the location, since the FK otherwise rejects purging a location that's ever been assigned to a user.
- `User.locations` is initialized to an empty set so `getLocations()` returns empty rather than null for a fresh/unrestricted user.
- `User.locations` is marked `@Independent` (the same annotation `Location.tags` uses) so `RequiredDataAdvice` doesn't recursively walk it on save/void/retire — without it, the collection could still be an uninitialized Hibernate proxy at that point (unlike `roles`, which privilege checks force-initialize during auth), causing `LazyInitializationException` in `AdministrationServiceTest`, `ContextTest`, and `LocationUtilityTest`.
- `DatabaseUpdaterDatabaseIT.CHANGE_SET_COUNT_FOR_GREATER_THAN_2_1_X` bumped 916 → 917: it hardcodes the total Liquibase changeset count for a fresh database, and the new `user_location` changeset adds one (per the comment on that constant, it needs updating whenever a new changeset is added). This was the `-Pintegration-test` CI job failure.
- Extracted the repeated `"locationId"` literal in `HibernateLocationDAO` into a `LOCATION_ID` constant (SonarCloud CRITICAL finding).
- Added `LocationServiceTest#purgeLocation_shouldDeleteLocationAssignedToAUser`, a regression test for the user_location FK purge fix, per review request.

Branch has since been updated with `master` and rebuilt clean on top of it.

Companion REST support (exposing `locations` on the user resource and a `forCurrentUser` location search) is in a separate PR against openmrs-module-webservices.rest ([#762](https://github.com/openmrs/openmrs-module-webservices.rest/pull/762)), which depends on this one merging first.

## Issue I worked on

see https://issues.openmrs.org/browse/TRUNK-6669

## Checklist: I completed these to help reviewers :)

- [ ] My IDE is configured to follow the [**code style**](https://wiki.openmrs.org/display/docs/Java+Conventions) of this project.
- [x] I have **added tests** to cover my changes. (If you refactored existing code that was well tested you do not have to add tests)
- [x] I ran `./mvnw clean package` right before creating this pull request and added all formatting changes to my commit. (no formatting changes produced)
- [x] All new and existing **tests passed**. (`./mvnw clean package`: BUILD SUCCESS, 5353+ tests, 0 failures; `-Pintegration-test -Dtest=DatabaseUpdaterDatabaseIT`: 1 run, 0 failures)
- [x] My pull request is **based on the latest changes** of the master branch.
