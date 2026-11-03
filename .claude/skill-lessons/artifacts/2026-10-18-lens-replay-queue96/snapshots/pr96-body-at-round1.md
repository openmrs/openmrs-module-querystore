## Summary

When a visit is deleted (voided or purged), associated queue entries were not being removed, leaving patients visible in the service queue despite their visit being gone.

**JIRA**: [O3-5459](https://openmrs.atlassian.net/browse/O3-5459)

## Root Cause

The existing `VisitWithQueueEntriesSaveHandler` implements `VoidHandler<Visit>`, but this handler does not reliably fire when `visitService.voidVisit()` is called. The handler only works through the `saveVisit()` path. Additionally, there was no handling for `purgeVisit()` at all.

## Changes

- **Added `VisitWithQueueEntriesDeleteAdvice`** — an AOP `MethodBeforeAdvice` registered on `VisitService` that intercepts:
  - `voidVisit()`: voids all associated queue entries with the same void reason
  - `purgeVisit()`: purges all associated queue entries before the visit is permanently deleted (preventing FK constraint violations)
- **Registered the advice** in `config.xml` via the `<advice>` element
- **Added tests** for both void and purge scenarios

## Testing

All 98 integration tests pass.

[O3-5459]: https://openmrs.atlassian.net/browse/O3-5459?atlOrigin=eyJpIjoiNWRkNTljNzYxNjVmNDY3MDlhMDU5Y2ZhYzA5YTRkZjUiLCJwIjoiZ2l0aHViLWNvbS1KU1cifQ