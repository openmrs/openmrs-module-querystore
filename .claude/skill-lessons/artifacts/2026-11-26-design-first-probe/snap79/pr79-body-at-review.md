## Description of what I changed
Removed the module's own helper types:

- `Action2` -> `java.util.function.BiConsumer` in the operation type processors
- `Pair` -> commons-lang3 `Pair` (provided by core, same equals/hashCode semantics, never serialized)
- module `StringUtils` -> the email check now lives in `StockManagementServiceImpl`, same regex, so nothing that validated before stops validating
- `NumberFormatUtil` -> plain `DecimalFormat` with the same patterns. This also drops the shared static `DecimalFormat` instances, which weren't thread-safe

Added tests pinning the number formatting (null/zero/negative/large/fractional) and the email check before swapping anything, so the output stays the same. No new jars in the omod.

## Issue I worked on
see https://openmrs.atlassian.net/browse/O3-6340

## Checklist: I completed these to help reviewers :)
- [x] My IDE is configured to follow the [**code style**](https://wiki.openmrs.org/display/docs/Java+Conventions) of this project.
- [x] I have **added tests** to cover my changes.
- [x] I ran `mvn clean package` right before creating this pull request and added all formatting changes to my commit.
- [x] All new and existing **tests passed**.
- [x] My pull request is **based on the latest changes** of the master branch.

