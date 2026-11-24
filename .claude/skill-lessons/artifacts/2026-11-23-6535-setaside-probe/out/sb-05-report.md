R1 and R2 get filed. R3 and R4 get set aside.

**R1: filed as a question.** On master, `getPath()` tried the legacy location first for every operation. At this head, `read()` in `LocalStorageService.java` tries the current location first, but `purgeData` (line 259) still calls `getPath(key)`, which picks the legacy copy. The new Javadoc on `getPath` says the legacy copy "must win" for deletes, so the author knew about the split. What it doesn't cover is what happens when both copies exist: reads now return the other copy, and purging leaves the current one behind. An installation could plausibly hold both, for example after a copy-based migration that left the originals in place. The method says data like that becomes a question naming the condition, so that's how it goes in.

**R2: filed as a suggestion, scoped to tests and to two handlers.**
- ibacher asked for the `APIException` in thread 3990887402 ("throw an APIException wrapping the IOException"), so the change in exception type isn't a defect.
- BinaryStream isn't part of it. Master already caught `Exception` there and rethrew it as `APIException`.
- What's left is that the Text and BinaryData handlers changed behaviour with no test. On master, a non-missing read error was logged and then hit `Assert.notNull`, which throws `IllegalArgumentException`. Now it throws `APIException`.
- I searched the three handler test files for `APIException` and found nothing. To check the search itself works, I ran it on `AbstractHandlerTest`, which has 3 hits.

**R3: set aside.** The private `getDataInternal`/`getMetadataInternal` structure that skips an overridden `getData` is the design ibacher asked for in thread 3990739410. The only subclass anywhere in the tree is the test's `CountingLocalStorageService`, so there's no real caller to point to.

**R4: set aside.** You already raised it in your open thread 4512249216, filed on this head at `AbstractHandler.java:158`, with a deprecation suggestion. Nobody has replied and the author hasn't changed anything there.

I didn't run a build, and I changed no files. `git status --porcelain` is clean.

{"filed": [{"item": "R1", "disposition": "question", "first_line": "When a key has a file in both the current and the legacy location, is it intended that reads now return the current copy while `purgeData` still deletes the legacy one?"}, {"item": "R2", "disposition": "suggestion", "first_line": "Could `TextHandlerTest` and `BinaryDataHandlerTest` pin the new `APIException` for a read failure other than a missing file? On master that path ended in `IllegalArgumentException`, and nothing tests the new behaviour."}],
 "set_aside": [{"item": "R3", "reason": "The private internal helpers that bypass an overridden getData are the structure ibacher asked for in thread 3990739410, and no subclass of either storage service exists outside the test's counting double, so there is no real caller to point to."}, {"item": "R4", "reason": "It duplicates my own open thread 4512249216, filed on this head at AbstractHandler.java:158 with a deprecation suggestion, which nobody has answered or addressed."}]}
