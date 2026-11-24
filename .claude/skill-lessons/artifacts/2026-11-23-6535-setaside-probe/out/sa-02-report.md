I'm filing one of the four, R2, as a non-blocking suggestion. R1, R3 and R4 are set aside.

**R2, filed as a suggestion.** The change itself is fine: ibacher asked in-thread for an `APIException` wrapping the `IOException` (review comment 3990887402). In `TextHandler` and `BinaryDataHandler`, a read error other than `NoSuchFileException` used to be logged and then fail with the unhelpful `Complex data must not be null` error. It now throws `APIException` with the cause attached. Nothing tests that branch: a grep of the handler tests finds no `APIException`, no `assertThrows` and no stub that throws `IOException`. Under the method's test-coverage rule, an untested behaviour change is a finding. Two narrowings:
- `BinaryStreamHandler` behaves as it did on master, whose `catch (Exception e)` already threw `APIException`. So the comment names only Text and BinaryData.
- ibacher's thread is outdated, so the comment goes on the head lines and links it.
- I didn't run a test that removes the branch and goes red, so the comment asks for the tests without claiming one would fail.

**R1, set aside.** The state it needs doesn't arise on its own:
- Current copies live under `<appdata>/storage/<encoded key>` and legacy copies under `<appdata>/<key>`.
- New keys are date-structured. Nothing in core writes a key at both locations, and no migration code exists (I grepped `api/src/main/java` for "legacy").
- `purgeData()` still goes through `getPath()`, which prefers the legacy copy exactly as master did. So the leftover copy after a purge happens on master too.
- Reads preferring the current copy is a deliberate choice, explained in the head's `getPath` Javadoc.

**R3, set aside.**
- Nothing in the tree subclasses `LocalStorageService` or `S3StorageService` outside a test.
- A module plugs in its own storage by implementing `StorageService` under its own qualifier (chosen by `storage.type`). The new default `getDataWithMetadata` calls `getData` and `getMetadata`, so those implementations keep their behaviour.
- For S3, routing `getData` through the combined read is what your review comment 4473604884 suggested.

**R4, set aside.** Your review comment 4512249216 raised it on this head, and the author applied your suggested `@Deprecated(since = "3.0.0")` with its Javadoc pointer word for word (`AbstractHandler.java`, just above the two-argument overload). Under the method, a real fix gets no reply.

No files were changed; I only read files and ran greps.

{"filed": [{"item": "R2", "disposition": "suggestion", "first_line": "Could `TextHandlerTest` and `BinaryDataHandlerTest` each get a test for this new `APIException` path, where the read fails with an `IOException` other than `NoSuchFileException`?"}], "set_aside": [{"item": "R1", "reason": "Nothing in core writes the same key to both the current and the legacy location, and `purgeData` still prefers the legacy copy just as master does, so the mismatch needs a duplicate made by hand and the leftover copy after a purge happens on master too."}, {"item": "R3", "reason": "Nothing in the tree subclasses the concrete storage services, modules plug in storage by implementing `StorageService`, whose new default method still calls `getData`/`getMetadata`, and having S3 `getData` share the combined read was the reviewer's own suggestion."}, {"item": "R4", "reason": "Already raised in review comment 4512249216 on this head, and the author applied the suggested `@Deprecated` pointer word for word, so the method calls for silence."}]}
