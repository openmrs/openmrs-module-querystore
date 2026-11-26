
## Description of what I changed
Settings are now read through core's `AdministrationService` and `GlobalProperties` is gone.

- The property names live in `ModuleConstants`.
- Each read uses `getGlobalPropertyValue(key, default)` with the old default at the call site. On a value it can't parse, core logs it and returns the default, so nothing is swallowed silently any more.
- The few settings with their own rules keep them, in small helpers in `ModuleConstants` that log a warning with the key and the bad value: the two job switches (`true`/`yes`/`1`), the pack-size priority (blank means true), the optional search limit (blank means no limit), and the minimums (rule batch size ≥ 1, notice period ≥ 0, batch job expiry ≥ 1440, reporting page size and days in month > 0).
- The nullable text settings (health centre name, notification email/role, unknown concept, the two observation concepts) use `getGlobalProperty(key, null)`, since the typed method doesn't accept a null default.
- `toString(Properties)` / `fromString(String)` aren't settings, so they moved to `PropertiesUtil` unchanged.

Every setting keeps its old default. `GlobalPropertyDefaultsTest` checks each one when unset, and `ModuleConstantsTest` covers malformed and out-of-range values.

## Issue I worked on
see https://openmrs.atlassian.net/browse/O3-6352

## Checklist: I completed these to help reviewers :)
- [x] My IDE is configured to follow the [**code style**](https://wiki.openmrs.org/display/docs/Java+Conventions) of this project.
- [x] I have **added tests** to cover my changes.
- [x] I ran `mvn clean package` right before creating this pull request and added all formatting changes to my commit.
- [x] All new and existing **tests passed**. (227: 180 api, 47 omod)
- [x] My pull request is **based on the latest changes** of the master branch.


