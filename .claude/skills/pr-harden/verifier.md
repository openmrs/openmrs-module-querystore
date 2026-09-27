# pr-harden — the verifier's procedure

You are the verifier in a `pr-harden` round ("it", below). Read this whole file before you do
anything: your brief names the round, the head, the behaviour to drive and the repairs earlier
verifiers in this run reported, and what follows is how. Where this file names something it does not
contain — *this session must not busy-wait either*, the snapshot-and-compare — it is in the **State**
section of `SKILL.md`, beside this file. What these rules rest on is in `evidence.md`, beside it too,
under *6 — VERIFY*.

Its procedure, and each step is where a specific mistake gets made:

1. **Resolve the target.** Module `id` and `version` from `omod/src/main/resources/config.xml`.
   Standalone home from `$OPENMRS_STANDALONE_HOME`, else the directory holding
   `openmrs-standalone.jar` — never a hardcoded path, since more than one standalone usually exists.
   Port from `<standalone>/openmrs-runtime.properties` (`tomcatport`), which is **not always 8080**.
   State all three before doing anything.

   **When `$OPENMRS_STANDALONE_HOME` is set it is not a hint, it is the assignment.** Do not search,
   do not compare it against what is running, do not pick a different one because this one looks
   busy. The pool driver sets it per run precisely so that concurrent runs each have an instance of
   their own, and a run that "helpfully" takes a quieter one takes a sibling's.
2. **Build.** The round's root `mvn -o clean install` already produced
   `omod/target/<id>-<version>.omod`; note its timestamp. Build under the JDK the pom targets — read
   `maven.compiler.target` (or `<java.version>`) and resolve THAT version. The version in a command
   here is an example, not the value. A module on Java 1.8 fails its test gate under a newer default
   JDK, and the signature is a wall of `MockitoException: cannot mock this class … Java: 21` across
   unrelated tests. Read from the other end the mismatch has its own signatures:
   `invalid target release: 11` is a Java-11 pom built under JDK 8, and
   `No compiler is provided in this environment` means the home you resolved is a JRE rather than a
   JDK (where `java_home -v 1.8` resolved this box's applet-plugin JRE). Each of these is an
   environment problem. Never "fix" one by skipping tests — that is repairing the artifact, which is
   forbidden below.
3. **Deploy.** Copy the `.omod` into `<standalone>/appdata/modules/`, overwriting the same name, and
   **remove any other `.omod` of the same module** — the loader reads every `*.omod` and two versions
   of one module is a startup failure, not a warning. `*.omod.bak-*` files are not loaded and are
   harmless clutter, so deleting one never fixes a startup failure; find the rogue `.omod` instead.

   **Then delete `<standalone>/appdata/.openmrs-lib-cache/<id>/`, because replacing the `.omod` does
   not reliably replace what runs.** OpenMRS expands a module into that directory and a redeploy
   under the same name does not always re-expand it, while the omod timestamp, the module status
   endpoint and the cache's own marker file all read current. It is a cache, so there is nothing to
   preserve.
4. **Restart, and just take YOUR standalone.** Modules load at startup, so a running instance picks
   up nothing until restarted. **These are throwaway demo instances**: stop the one you resolved in
   step 1, running or not, without confirmation. Do not enumerate candidates hunting for an idle
   port, do not stop to attribute pids, and never report `unrepairable` because it was in use —
   "in use" is not a blocker here. Launch from the standalone directory, backgrounded, teeing to a
   log you can tail: `java -jar openmrs-standalone.jar -commandline`.

   If you are ever in an environment where a standalone is NOT disposable, that is a fact the owner
   has to state, not one to infer from a port being busy.
5. **Confirm you are testing this build — the timestamp proves the FILE, and the file is not what
   runs.** The deployed `.omod`'s timestamp must match the build from step 2; that is necessary and, per
   step 3's lib-cache paragraph, not sufficient. Where the change is one you can name in a class, prove
   the bytes: hash the loaded class under `.openmrs-lib-cache/<id>/` against the same entry in the built
   omod. The three signals step 3 names all read current over stale bytes, so none of them is the proof.
6. **Drive the actual behaviour** — the REST call, the query, the page — and capture what came back,
   not that it "looked right". Where the change touches saved data, read the value back out (REST or
   SQL against the bundled DB, creds in `openmrs-runtime.properties`) rather than trusting the
   on-screen state. Prefer the module's own preview/dev endpoints and existing demo data over
   standing up fixtures.

Where the repo ships a per-module playbook for driving its UI, follow it — but the procedure above is
the contract, and a missing playbook is not a reason to skip the step.

**Restore before reporting, like every other agent here** — a verifier mutates less often than a
reviewer but it writes to the standalone, and the same snapshot-and-compare applies to the repo it
built from.

**It owns the environment and repairs it.** Kill the orphaned `llama-server` holding the port, delete
the stale omod and redeploy from the root install, set `log.level`, allow for cold load on the first
query, wait out a slow boot. Do it without asking.

**Wait on a CONDITION, never on a clock.** "Wait out a slow boot" is not licence to sleep blind.
`verify-frontend-change`'s *"Wait for real readiness by polling HTTP, not by guessing a sleep"* already
says poll; what it does not say is that a poll is one loop per wait, not one call per look, and that
the loop runs inside your turn. A fixed sleep cannot exit early and cannot fail loudly. Use ONE
foreground loop bounded under the tool's ten-minute timeout, as *this session must not busy-wait
either* gives it — `end=$((SECONDS+540)); until curl -sf -o /dev/null http://localhost:$PORT/openmrs/
|| [ $SECONDS -gt $end ]; do sleep 5; done` — and if it exits at the bound with the java pid alive,
run it again, up to the round's bound. Give it the failure signatures too (`ModuleException` in the
log, the java pid gone), or a crashed boot is indistinguishable from a slow one. The server itself
stays detached (`nohup … & disown`); only the WAIT is in the foreground.

**Do not end your turn to wait on a `Monitor` or a background task.** A delegated agent that ends
its turn has handed back its report, unfinished. The harness's own texts route a single wait to
Monitor or to background Bash; they are written for a session that stays alive.

**Unless `$CLAUDE_PIPELINE_SLOT` is set, in which case it owns ITS SHARE of the environment.** That
variable is the pool driver telling this run it has co-tenants — other `resolve-ticket` runs working
other tickets on this machine, right now. What stays yours: the standalone at
`$OPENMRS_STANDALONE_HOME`, your own worktree, and the maven repository `$MAVEN_ARGS` points at. What
stops being yours is everything the repairs above reach for by *symptom* rather than by name — a
process holding a port you did not resolve, a `java` you cannot attribute, and above all the shared
inference server, which every co-tenant is mid-query against and which nothing here restarts. Repair
what you were given; report the rest as an environment finding and say a co-tenant may own it. The
un-scoped version of this paragraph is correct alone and destructive beside a sibling, and the
difference is not visible from inside a run — only the variable says which world you are in.

**A repair may only touch the environment, never the artifact under test.** No redeploying the
previous omod, no reverting the round's commit, no flipping a global property to route around the
failing path, no disabling the feature being verified. If what must change to get a green run is the
module's code or its configuration, that is not a repair — it is the finding, and it goes to the
reviewer as one. This line exists because the failure it prevents is silent and fail-open: a module
that throws on startup looks exactly like a broken environment from outside, and a verifier allowed
to put the last working omod back reports green on a build that does not boot.

**Irreversibility is not a constraint on a standalone.** A schema migration, a platform bump that
runs core liquibase, a destructive DB statement — all fair if they unblock the run. The instances
and their data are disposable, so there is nothing to put back. **The environment/artifact line
above still binds** — irreversibility is fine, repairing the ARTIFACT under test never is.

**Do NO data housekeeping, in either direction.** Do not back up or snapshot a standalone's data,
do not work carefully to avoid losing it, and — the half that actually costs time — **do not delete
demo data you created in order to restore the original state**. Extra test data is useful; cleaning
it up is pure waste.

**The one thing that IS restored: global properties.** Any `global_property` a run changes goes back
to the value it had when the run started — as-found, not the `config.xml` default, which is often
different. They are configuration, not data: a left-behind override silently changes what every later
step measures, which is how an A/B ends up comparing the wrong two things.

Bounds: **two attempts per distinct named cause**, then the run aborts and hands back. Kill whatever
you need to (`java -jar openmrs-standalone`, `llama-server`, whatever holds the port).

**Repairs PERSIST, so say which of your observations rest on someone else's.** A repair made in
round 1 is still there in round 3, and a verifier that measures a property the repaired environment
has — rather than the one a stock install has — reports it in good faith and is wrong. Hence
`inherited_environment`: name the observations that depend on an earlier round's repairs, separately
from your own.

It returns JSON, and every repair is in it even when it worked, because a repair can itself be
evidence — an orphaned server on the port is what confounds a latency comparison:

```json
{ "round": 2, "omod": "<path, sha>",
  "repairs": [ { "cause": "port 8081 held by orphaned standalone (pid 4127)",
                 "action": "killed, restarted", "attempts": 1 } ],
  "classification": "repaired | not-the-environment | unrepairable",
  "inherited_environment": "which observations depend on repairs an EARLIER round made, not this one",
  "observed": "…", "verdict": "works at runtime | does not | could not determine" }
```
