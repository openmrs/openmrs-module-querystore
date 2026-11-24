I'm filing R2 as a non-blocking suggestion and setting aside R1, R3 and R4. Nothing was built, and the working copy is unchanged (`git status --porcelain` printed nothing).

**R2, filed as a suggestion.** On master, a read that failed for any reason other than a missing file was logged in the Text and BinaryData handlers. The empty `complexData` then hit `Assert.notNull` and threw `IllegalArgumentException`. At this head the same failure throws `APIException("Obs.error.while.trying.get.complex" / "...binary.complex")`.
- **The type change isn't up for debate.** It is what ibacher asked for in thread 3990887402.
- **No test reaches the new path.** `git grep` finds neither message key anywhere under `api/src/test`. `TextHandlerTest` and `BinaryDataHandlerTest` contain no `APIException` or `assertThrows`.
- **BinaryStream is a correction to the second reviewer.** That handler is not a change: master already wrapped every read failure there in `APIException` through its `catch (Exception e)`. So the comment covers Text and BinaryData only.

**R1, set aside.** No real save path leaves two copies of one key.
- New keys come from `newKey`: `<obsDir>/<timestamp><8 random chars>-<filename>`. Legacy files sit flat under the application data directory. Both copies exist only if someone copies files into `storage/` by hand, and the second reviewer's repro had to write both copies itself.
- In that case master is no better. `getPath` prefers the legacy copy, so after a purge master also leaves the current copy behind.
- `getPath`'s javadoc at this head says that purge deliberately prefers the legacy copy.

**R3, set aside.** It points at no actual subclass. The only class extending `LocalStorageService` or `S3StorageService` in the tree is a test's `CountingLocalStorageService`. The javadoc on `StorageService.getDataWithMetadata` invites implementations to provide their own combined read. Its default implementation still covers independent `StorageService` implementations.

**R4, set aside.** I already raised this, in thread 4512249216. The head now carries `@Deprecated(since = "3.0.0")` on the two-argument overload, which delegates to the three-argument one. That is the fix the thread asked for, and the method says a fix that's really in gets no comment.

{"filed": [{"item": "R2", "disposition": "suggestion", "first_line": "Could a test pin this `APIException`? A read that fails for a reason other than a missing file now throws it where master threw `IllegalArgumentException`, and nothing in `TextHandlerTest` or `BinaryDataHandlerTest` reaches it."}], "set_aside": [{"item": "R1", "reason": "Both copies of one key exist only if files are copied into storage/ by hand (newKey gives new keys a timestamp and random suffix, and legacy files are flat under the application data directory), and in that case master also leaves the current copy after a purge, because its getPath prefers the legacy copy too."}, {"item": "R3", "reason": "Nothing subclasses LocalStorageService or S3StorageService except a test helper, the documented extension point is overriding getDataWithMetadata, and the interface default covers independent implementations."}, {"item": "R4", "reason": "Already raised in thread 4512249216, and the head carries the @Deprecated(since = \"3.0.0\") that thread asked for, so the fix is in and the method calls for silence."}]}
