# resolve-ticket 0.24.1 · openmrs-core · TRUNK-6826 / PR 6680 · 2026-10-11
outcome: converged
rounds: 2   cycles: 2 (harden)   verifier: ran (works at runtime; instrument substituted: jetty:run + 31 O3 modules, scratch MySQL — the standalone/omod procedure cannot reach a core web.xml/web change)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa-Projects-openmrs-core-6826/74a2fd99-d448-40eb-888b-5436dcddd9c4.jsonl

## Refuted by measurement
- Plan: UrlHandlerFilter.trailingSlashHandler("/ws/**") scopes the trim to /ws -> refuter probe: patterns match the full path incl. context path, so "/ws/**" never fires under /openmrs; became "/**" behind the filter's own /ws guard · cost: 0 (caught at Step 3)
- Plan: a pathInfo.length() > 1 guard is needed to keep /openmrs/ws/ (legacyui index) alive -> runtime mutant on jetty: /ws/ still 200; UrlHandlerFilter excludes the servlet root itself; guard deleted · cost: one jetty restart
- Javadoc "Spring 6 stopped matching ... by default" -> own probe harness failed on classloading; clause deleted; Spring 7 claim kept after javap control (6.2.12 has setMatchOptionalTrailingSeparator, 7.0.9 has none) · cost: harden pass
- Assumed the RefApp 500 was the missing handler itself -> it is webservices.rest's /rest/**=unknownResourceController SimpleUrlHandlerMapping with no adapter (also /ws/rest/nosuch without a slash); REST-only setup gave 404 · cost: 0

## Raised by a fresh agent, missed by the author
- [refuter] "/ws/**" pattern inert under a context path · blocking · cost: 0 rounds
- [harden P2 quality] javadoc/test comment said springSecurityFilterChain enforces URL rules; it is permitAll, openmrsAuthorizationFilter enforces them · substantive (escalated) · cost: 1 harden cycle
- [harden P2 integration] composition with #6673: RepeatedSlashFilter must precede TrailingSlashFilter · non-blocking · PR body
- [harden P2 integration] multipart POST 500 from legacyui XSSFilter ClassCastException (pre-existing) · non-blocking · PR body
- [harden P2b integration] anonymous /ws/fhir2/R4/metadata/ 401 -> 200 via fhir2 endsWith("/metadata") · non-blocking · PR body
- [r1] URI ending in // trimmed to a single / before the firewall: 400 -> 404/500 on Tomcat without #6673 · non-blocking · fixed in 6522f363f2 · cost: 1 round

## Where a skill blocked or contradicted this run
- EnterWorktree isolation guard refused many compound Bash commands (cd, runtime vars, a URL containing "github"); multi-step actions had to become scratchpad scripts · cost: many extra tool calls
- harden mutation: the restore put the source back but left the last mutant compiled in web/target/classes; a jetty restart ran the mutant until a javap check caught it
- harden Phase 2 helper merge introduced an overload ambiguity (mockMvc(new Filter[]{..}) resolved to the varargs between-slot), caught only by re-running the ordering mutation

## Declined
- none

## Assumptions review overturned
- none (A1 "restore 2.x acceptance" confirmed on a 2.x standalone: /ws/rest/v1/session/ 200)
