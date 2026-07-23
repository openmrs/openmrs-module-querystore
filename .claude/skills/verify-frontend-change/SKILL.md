---
name: verify-frontend-change
description: Verify an OpenMRS module UI change end-to-end before declaring it done — build the .omod, deploy it into a local OpenMRS standalone, (re)start the server, then drive the actual page in a browser. Use for any backend module that renders a UI (legacy/refapp web pages, HTML Form Entry, etc.) — e.g. htmlformentry's date-widget clear button. Trigger phrases: "verify this UI change", "test the module in the browser", "does this render/work in OpenMRS", "verify frontend change".
version: 0.1.0
---

# Verifying an OpenMRS module UI change

Never report a UI change as complete based on a successful edit or a green `mvn test` alone. An OpenMRS module's UI is server-rendered (JSP/GSP + the module's own JS, e.g. `htmlFormEntry.js`) and only exists once the module is **built into an `.omod`, deployed into a running server, and the page is actually rendered**. Verify it the way a reviewer clicking through the running app would.

This skill is **module-agnostic** — it works for any module with a `omod/` package. It targets a local **OpenMRS standalone** (the self-contained jar + bundled MySQL/MariaDB + refapp/legacy UI). Steps below stay generic; where a module has its own rendering/submit quirks, capture them in a per-module playbook under `references/<moduleid>.md` and point to it (an HTML Form Entry one exists at `references/htmlformentry.md`).

## 0. Resolve the target (do this first, out loud)

- **Module**: the current repo. Confirm it has `omod/` and `omod/src/main/resources/config.xml`. Read the module `id` and `version` from `config.xml` — you need them to name the `.omod` and to find it in the loaded-modules list.
- **Standalone home**: use `$OPENMRS_STANDALONE_HOME` if set; otherwise the directory containing `openmrs-standalone.jar` that the user points you at. Do **not** hardcode a path — the user runs several standalones. If more than one candidate exists and none is configured, ask which one.
- **Port / creds**: read the port from `<standalone>/openmrs-runtime.properties` (`tomcatport=…`) — it is **not always 8080**. Admin login is `admin` / `Admin123` unless the server props say otherwise. Base URL is `http://localhost:<port>/openmrs/`.

State the resolved module, standalone path, and port before proceeding.

## 1. Build the `.omod` (gate — must pass)

```
mvn clean install
```

from the module root. This runs the tests **and** produces the deployable artifact. A test failure or compile error is a hard stop — surface the real output, fix, do not proceed. On success the artifact is `omod/target/<moduleid>-<version>.omod`. Confirm it exists and note its timestamp.

**Build under a JDK the module targets.** Read `<java.version>` (or `maven.compiler.target`) from the pom. A module targeting Java 1.8 will fail its test gate under a too-new default JDK — the classic signature is a wall of `MockitoException: cannot mock this class ... Java: 21` across otherwise-unrelated tests (the old byte-buddy can't instrument under JDK 17+). This is an environment problem, not a code defect. Find a matching JDK (`/usr/libexec/java_home -v 1.8`) and rebuild with `JAVA_HOME=… mvn clean install`. Don't "fix" it by skipping tests — a green gate under the right JDK is the contract. (The **runtime** JDK the standalone uses can differ; the Mockito issue is test-only.)

## 2. Deploy into the standalone

- Copy the freshly built `.omod` into `<standalone>/appdata/modules/`, overwriting the same-named file.
- **Remove any *other* `.omod` of the same module** (a different version) from that folder — the loader reads every file ending in `.omod`, and two versions of one module is a startup failure, not a warning. Files that don't end in `.omod` (e.g. `*.omod.bak-*` backups) are *not* loaded, so they don't cause this — they're harmless clutter, not a double-load risk. Don't delete a backup expecting it to fix a startup failure; find the rogue `.omod` instead.

## 3. (Re)start the server headless

OpenMRS loads modules at startup, so a running instance must be restarted to pick up the new `.omod`.

- **Don't clobber a server the user is using** — restarting is destructive to whoever's on it. Prefer a standalone that's idle or on a different port (this is why step 0 lets you choose); if the chosen one is already serving its port, confirm it isn't the user's active session before stopping it. To stop: find the pid with `lsof -iTCP:<port> -sTCP:LISTEN -n -P`, terminate it, then wait for the port to free.
- Launch headless from the standalone dir, backgrounded, teeing output to a log you can tail:
  ```
  java -jar openmrs-standalone.jar -commandline
  ```
- **Wait for real readiness by polling HTTP**, not by guessing a sleep. Loop `curl -s -o /dev/null -w '%{http_code}' http://localhost:<port>/openmrs/` until it returns `200`/`302` (cold start with DB upgrade can take 1–3 min). Do not open a browser before this succeeds.

## 4. Confirm the module actually loaded (the "zero console errors on load" analog)

Before touching the UI, prove the module started clean:
- **Primary (reliable positive signal):** hit the module REST endpoint (`/ws/rest/v1/module/<id>`) or the manage-modules admin page and confirm the module's state is **started**.
- **Also** scan the startup log for the module id: there must be **no** `ModuleException` / `Error while starting module` / mapping or bean failure. Absence of a clear "started" line is *not* proof of failure — a module can start while logging only benign warnings, so don't infer breakage from the log alone; rely on the state check above.

A module that silently failed to load renders nothing — catching it here saves a confusing browser session.

## 5. Drive the change in the browser

Use a real headless browser. Playwright is the default — a driver script needs both the node module and the browser binary, so install once into a scratch dir (`npm i playwright && npx playwright install chromium`) and run the driver from there with `node`; a configured chrome-devtools MCP works too. Then:

1. Log in at `http://localhost:<port>/openmrs/` as `admin` / `Admin123`.
2. Navigate to the exact page that exercises the change — the specific admin page, portlet, patient-dashboard fragment, or form the edited code renders.
3. **Interact directly with the change**, don't just assert it rendered. For a new control (button, clear-"×", toggle, input): perform the action, confirm the expected state change, and screenshot **before and after**.

**Don't build heavy fixtures you don't need.** A fresh standalone may have no domain data for the page you need. Before scripting entity creation, look for a lighter render path the module already ships — a preview/dev endpoint, a demo page, an existing demo patient. The generalizable move: reach for the module's own preview/dev endpoints before standing up domain data. Capture the one you find in the module's `references/<moduleid>.md` (e.g. HTML Form Entry's form-from-file preview is documented in `references/htmlformentry.md`).

## 6. Check the browser console

Zero **new** JS errors or warnings during load and interaction. The module's own client-side JS failures show up here and nowhere else.

## 7. Verify server-side persistence (OpenMRS-specific — do not skip)

A changed value that looks right in the DOM but doesn't persist is the classic OpenMRS UI bug. Where the change touches saved data, **save/submit and confirm the value actually landed** — check the resulting row (obs, encounter, metadata, etc.) via the REST API or a SQL query against the standalone's bundled DB (creds are in `openmrs-runtime.properties`), not just the on-screen state. Report the concrete value you read back. Dev/test standalones usually already have demo data (query the relevant table) — prefer it over faking data.

**Prove causation with a positive control.** For a change that *removes/clears/alters* a value, one save isn't enough — show the value behaves one way when you *don't* invoke the change and the other way when you *do*. Run both paths (A: don't apply the change → value persists; B: apply the change → value cleared/altered) against the same fixture, changing only the action under test, and compare the persisted rows. This distinguishes "the change works" from "this field never saved anyway".

If submitting the module's UI stalls (a JS pre-check XHR 403s, a validation hook blocks, etc.) and you are already authenticated, posting the form element directly (`document.getElementById('<formId>').submit()`) is a legitimate way to reach the server path — note module-specific submit quirks in `references/<moduleid>.md`.

## If any step fails

Fix the issue and **rerun from step 1** (rebuild → redeploy → restart). Do not hand back partially verified work, and do not substitute "the code looks right" for a step you couldn't run.

## Reporting shape

Mirror the empirical-verification discipline: name (1) what was run — the build, the deploy path, the URL/interaction; (2) what came back — the before/after screenshots, console state, and the persisted value; (3) the comparison — prior vs new behavior; (4) a one-sentence verdict (works / partially / broken) with the specific evidence. If a step genuinely couldn't run (browser unavailable, DB unreachable), say "I could not verify X because Y" explicitly rather than implying it passed.

## Anti-patterns to catch

- **"Tests pass, so the UI works."** Unit tests don't render the page; a JSP/JS error ships green.
- **Verifying against a stale `.omod`.** If you didn't rebuild after the edit, or an old version is still in `appdata/modules/`, you're testing the wrong bytes. Always confirm the deployed file's timestamp matches this build.
- **Assuming port 8080.** Read `tomcatport` from the standalone's runtime props.
- **Skipping the restart.** Editing the source and re-running `mvn install` changes nothing in a server that's already up until it's restarted (or the module is re-uploaded via web admin).
- **Reporting DOM state as done.** For data-affecting changes, confirm persistence server-side.
