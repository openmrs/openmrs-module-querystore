Decisions are in. R1 and R2 get filed, R3 and R4 get set aside.

**R1: filed as a question on `LocalStorageService.read()`.**
- I read the code at the head. `read()` tries the current location first. `purgeData()` still goes through `getPath()`, which tries the legacy location first, and the PR's own Javadoc on `getPath()` says "the legacy location must win" for deletes.
- On master, reads and purges both went through `getPath()` and picked the same copy. So this split is new.
- The effect is silent: when both copies exist, a purge deletes the legacy file, returns true, and the current copy keeps being served.
- It's a question, not a blocker, because nothing shipped creates both copies. The only route I can name is an admin copying (not moving) `complex_obs/` into `storage/complex_obs/`.

**R2: filed as a suggestion, cut down to the missing test.**
- The switch to `APIException` is what ibacher asked for in thread r3990887402, so the exception type itself isn't a finding.
- `BinaryStreamHandler` isn't a change at all. On master its `catch (Exception e)` already threw `APIException`.
- What's left is that the new wrapped-error path in `TextHandler` and `BinaryDataHandler` has no test. A grep of those two handlers' test files finds no `APIException` or `assertThrows`.

**R3: set aside.**
- Nothing in `src/main` subclasses `LocalStorageService` or `S3StorageService`. The only subclass in the tree is the counting helper in `LocalStorageServiceTest`.
- A module that wraps storage by implementing `StorageService` and delegating gets the interface's default `getDataWithMetadata`, which still calls its own `getData`.
- Only a subclass that overrides `getData` through inheritance is bypassed. I can't name one, and since the network is closed I couldn't check downstream modules.

**R4: set aside.**
- This is the point raised in thread 4512249216. The head already has the deprecation suggested there: `@Deprecated(since = "3.0.0")` with a pointer to the three-argument overload. Under the method, a fixed thread gets no reply.

I didn't build or change anything; the decisions rest on reading the code. The JSON below has the opening line each filed comment would start with.

{"filed": [{"item": "R1", "disposition": "question", "first_line": "Should a key stored in both places be read and purged from the same one? `read()` now prefers the current copy while `purgeData()` (via `getPath()`) still deletes the legacy one, so the purge returns true and the current copy keeps being served."}, {"item": "R2", "disposition": "suggestion", "first_line": "Could `TextHandlerTest` and `BinaryDataHandlerTest` each get a test that a read error other than a missing file comes back as an `APIException` with the `IOException` as its cause? Nothing covers that path yet, so going back to swallowing the error would leave every test green."}], "set_aside": [{"item": "R3", "reason": "No subclass of LocalStorageService or S3StorageService exists outside tests, and a module that delegates by implementing StorageService gets the default getDataWithMetadata, which still calls its own getData, so I can't name any caller this breaks."}, {"item": "R4", "reason": "This is thread 4512249216, and the head already carries the deprecation suggested there, so the fixed thread gets silence under the method."}]}
