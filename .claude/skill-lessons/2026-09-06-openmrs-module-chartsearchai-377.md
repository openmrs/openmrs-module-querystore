# resolve-ticket · openmrs-module-chartsearchai · #377 / PR 381 · 2026-09-06
outcome: converged
rounds: 4   cycles: 3 (harden)   verifier: ran twice (works at runtime; the second covered the merged head)
context: no compaction · peak not surfaced
transcript: ~/.claude/projects/-Users-danielkayiwa--claude-pipeline-worktrees-openmrs-openmrs-module-chartsearchai-377/44995b38-77b8-4722-8b85-e76775b396f3.jsonl

## Refuted by measurement
- "the next-phrase-occurrence bound is what keeps the check quiet" -> byte-identical over 66,429 generated arrangements without it; it is only a scan bound, and survives as the Matcher region that keeps the scan linear · cost: 1 harden pass
- "the phrase gate is the rule; without it this becomes a rule about which records an answer may cite" -> deleting it was byte-identical; examine's per-sentence indexOf is the rule, the gate is a short-circuit · cost: 1 harden pass
- "re-inlining the literal leaves the suite green while the check silently stops matching" -> re-inlined, all cases stayed green; what it removes is the coupling, not the match · cost: 1 harden pass
- "TWO callers match INLINE_CITATION directly, each needing an offset" -> six sites across three classes, in two categories · cost: 1 harden pass
- "RUN_SEPARATORS carries \r\n because that is how the model writes them" -> SENTENCE_BOUNDARY's line-break arm makes them unreachable; dropped, and the hard-wrap residue they were hiding is now recorded · cost: 1 harden pass
- "horizontal whitespace" as the separator category -> NBSP is horizontal whitespace and is not in the set; the three characters are now named instead · cost: 1 harden pass
- "two independently generated corpora" -> corroborated nowhere in the tree; the code records one corpus of 66,429 · cost: 1 harden pass
- ADR anchors invented from decision CONTENT rather than headings -> three of four resolved nowhere · cost: 0 (caught in the same pass)

## Raised by a fresh agent, missed by the author
- [harden p1] `referenceGroup`'s fail-safe calls an unread type chart evidence, so an untyped record was reported as "null record" — an accusation about metadata nobody read · non-blocking · cost: 0
- [harden p2] a measured Matcher.find quadratic: 320 markerless claims cost 10.7 ms, 103 us with a region bound · non-blocking · cost: 0
- [harden p2] the early-done case rested on one negative assertion over a log snapshot; with the check moved above the handoff AND the capture pointed at a dead logger, all cases stayed green · non-blocking · cost: 0
- [harden p2] README told a frontend author the finding is correct and the citation wrong, and named no shape where the opposite holds; two are reachable, and the `grounded: true` one was constructed against the real pipeline · non-blocking · cost: 0
- [r1] the attribution unit was unbounded on the NEAR side, so a claim with no citation of its own annexed the next clause's — into a key a client renders. The prior argument refuted a LENGTH budget and never reached a CLAUSE bound · non-blocking · cost: 0
- [r1] the phrase source guard defeated by splitting the render site into two adjacent literals: same string, constant read nowhere, test green · non-blocking · cost: 0
- [r2] the comma member of RUN_SEPARATORS was the one mechanism no case pinned; deleting it left the whole build green, and `LlmAnswerExtractor.rewriteShorthand` emits ", " between rewritten markers, so a normalised `[finding, chart]` group would have dropped this ticket's own defect · non-blocking · cost: 0
- [r2] `CitationGroundingVerifier`'s "has no counterpart here" was the fourth and weakest copy of one argument — and the exact line the issue body points a maintainer at · non-blocking · cost: 0
- [verifier] the live shape is BROADER than the ticket's three: two obs records were misattributed in the same five-claim answer · cost: 0

## Raised by a fresh agent, missed by the author (continued, post-merge)
- [r3] the merge deleted the twelve-argument ChartAnswer constructor and thereby falsified the only written rationale for putModuleStatements' null guard ("built from a shorter constructor"); two homes, both true pre-merge · BLOCKING · cost: 1 round
- [r4] round 3's own correction carried a third clause its own fix had falsified — "moving the check ABOVE the handoff left every case here green" reddens now, because this PR added the handoff-time log snapshot · non-blocking · cost: 0
- [r4] Decision 41's bold LEAD still read "has no counterpart here" while its body and the javadoc it mirrors had both been qualified — a third home, half-corrected · non-blocking · cost: 0

## Where a skill blocked or contradicted this run
- pr-harden:round — the r2 reviewer died on a session 429 mid-mutation, having removed the per-sentence split, and left it in the worktree. The snapshot-and-compare rule caught it; without it that mutation would have shipped in the next commit. The retry succeeded after the stated reset with a leaner brief. Cost: one retry.
- pr-harden:Step 1 — the base-drift guard earned its place twice in one merge. main took ADR Decision 75 from the same sequence this branch had allocated from (the identifier hazard), AND main's ArchitectureGuardTest caught the first conflict resolution (the silent-structural-claim hazard): two constructors taking the coverage where the guard admits one. Both are exactly what that step warns about; neither was visible from the conflict markers.
- ENVIRONMENT — the disk filled mid-run from six isolated agent worktrees each running a full [INFO] Scanning for projects...
[WARNING] 
[WARNING] Some problems were encountered while building the effective model for org.openmrs.module:chartsearchai-omod:jar:1.0.0-SNAPSHOT
[WARNING] 'build.plugins.plugin.version' for org.apache.maven.plugins:maven-clean-plugin is missing. @ line 122, column 12
[WARNING] 
[WARNING] It is highly recommended to fix these problems because they threaten the stability of your build.
[WARNING] 
[WARNING] For this reason, future Maven versions might no longer support building such malformed projects.
[WARNING] 
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO] 
[INFO] Chart Search AI Module                                             [pom]
[INFO] Chart Search AI Module - API                                       [jar]
[INFO] Chart Search AI Module - OMOD                                      [jar]
[INFO] 
[INFO] ------------------< org.openmrs.module:chartsearchai >------------------
[INFO] Building Chart Search AI Module 1.0.0-SNAPSHOT                     [1/3]
[INFO]   from pom.xml
[INFO] --------------------------------[ pom ]---------------------------------
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-compiler-plugin/maven-metadata.xml
Downloading from openmrs-repo: https://mavenrepo.openmrs.org/public/org/apache/maven/plugins/maven-compiler-plugin/maven-metadata.xml
Progress (1): 1.6 kB
                    
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-compiler-plugin/maven-metadata.xml (1.6 kB at 2.2 kB/s)
Progress (1): 0.5/1.6 kB
Progress (1): 1.6 kB    
                    
Downloaded from openmrs-repo: https://mavenrepo.openmrs.org/public/org/apache/maven/plugins/maven-compiler-plugin/maven-metadata.xml (1.6 kB at 736 B/s)
[WARNING] Ignoring incompatible plugin version 4.0.0-beta-5: The plugin org.apache.maven.plugins:maven-compiler-plugin:4.0.0-beta-5 requires Maven version 4.0.0-rc-6
[INFO] Latest version of plugin org.apache.maven.plugins:maven-compiler-plugin failed compatibility check
[INFO] Looking for compatible RELEASE version of plugin org.apache.maven.plugins:maven-compiler-plugin
[WARNING] Ignoring incompatible plugin version 4.0.0-beta-4: The plugin org.apache.maven.plugins:maven-compiler-plugin:4.0.0-beta-4 requires Maven version 4.0.0-rc-4
[WARNING] Ignoring incompatible plugin version 4.0.0-beta-3: The plugin org.apache.maven.plugins:maven-compiler-plugin:4.0.0-beta-3 requires Maven version 4.0.0-rc-4
[WARNING] Ignoring incompatible plugin version 4.0.0-beta-2: The plugin org.apache.maven.plugins:maven-compiler-plugin:4.0.0-beta-2 requires Maven version 4.0.0-rc-2
[WARNING] Ignoring incompatible plugin version 4.0.0-beta-1: The plugin org.apache.maven.plugins:maven-compiler-plugin:4.0.0-beta-1 requires Maven version 4.0.0-beta-3
[INFO] Selected plugin org.apache.maven.plugins:maven-compiler-plugin:3.16.0
[INFO] 
[INFO] --- clean:3.2.0:clean (default-clean) @ chartsearchai ---
[INFO] 
[INFO] --- install:3.1.2:install (default-install) @ chartsearchai ---
[INFO] Installing /Users/danielkayiwa/.claude/pipeline/worktrees/openmrs-openmrs-module-chartsearchai-377/pom.xml to /Users/danielkayiwa/.claude/pipeline/m2/slot-4/org/openmrs/module/chartsearchai/1.0.0-SNAPSHOT/chartsearchai-1.0.0-SNAPSHOT.pom
[INFO] 
[INFO] ----------------< org.openmrs.module:chartsearchai-api >----------------
[INFO] Building Chart Search AI Module - API 1.0.0-SNAPSHOT               [2/3]
[INFO]   from api/pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
Downloading from openmrs-repo-thirdparty: https://mavenrepo.openmrs.org/thirdparty/org/openmrs/api/openmrs-api/2.9.0-SNAPSHOT/maven-metadata.xml
Downloading from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/api/openmrs-api/2.9.0-SNAPSHOT/maven-metadata.xml
Downloading from archetype: https://mavenrepo.openmrs.org/public/org/openmrs/api/openmrs-api/2.9.0-SNAPSHOT/maven-metadata.xml
Downloading from mks-nexus-public: https://nexus.mekomsolutions.net/repository/maven-public/org/openmrs/api/openmrs-api/2.9.0-SNAPSHOT/maven-metadata.xml
Progress (1): 545/992 B
Progress (1): 992 B    
                   
Downloaded from archetype: https://mavenrepo.openmrs.org/public/org/openmrs/api/openmrs-api/2.9.0-SNAPSHOT/maven-metadata.xml (992 B at 2.3 kB/s)
Downloading from sonatype-central-snapshots: https://central.sonatype.com/repository/maven-snapshots/org/openmrs/api/openmrs-api/2.9.0-SNAPSHOT/maven-metadata.xml
Downloading from sonatype-nexus: https://oss.sonatype.org/content/repositories/releases/org/openmrs/api/openmrs-api/2.9.0-SNAPSHOT/maven-metadata.xml
Progress (1): 545/992 B
Progress (1): 992 B    
                   
Downloaded from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/api/openmrs-api/2.9.0-SNAPSHOT/maven-metadata.xml (992 B at 670 B/s)
Downloading from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/api/openmrs-api/2.9.0-SNAPSHOT/openmrs-api-2.9.0-20260904.122951-82.pom
Progress (1): 0.4/12 kB
Progress (1): 1.8/12 kB
Progress (1): 3.2/12 kB
Progress (1): 4.6/12 kB
Progress (1): 6.0/12 kB
Progress (1): 7.4/12 kB
Progress (1): 8.8/12 kB
Progress (1): 10/12 kB 
Progress (1): 12/12 kB
Progress (1): 12 kB   
                   
Downloaded from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/api/openmrs-api/2.9.0-SNAPSHOT/openmrs-api-2.9.0-20260904.122951-82.pom (12 kB at 20 kB/s)
Downloading from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/openmrs/2.9.0-SNAPSHOT/maven-metadata.xml
Downloading from archetype: https://mavenrepo.openmrs.org/public/org/openmrs/openmrs/2.9.0-SNAPSHOT/maven-metadata.xml
Downloading from openmrs-repo-thirdparty: https://mavenrepo.openmrs.org/thirdparty/org/openmrs/openmrs/2.9.0-SNAPSHOT/maven-metadata.xml
Downloading from mks-nexus-public: https://nexus.mekomsolutions.net/repository/maven-public/org/openmrs/openmrs/2.9.0-SNAPSHOT/maven-metadata.xml
Downloading from sonatype-central-snapshots: https://central.sonatype.com/repository/maven-snapshots/org/openmrs/openmrs/2.9.0-SNAPSHOT/maven-metadata.xml
Progress (1): 545/810 B
Progress (1): 810 B    
Progress (2): 810 B | 545/810 B
                               
Downloaded from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/openmrs/2.9.0-SNAPSHOT/maven-metadata.xml (810 B at 1.8 kB/s)
Progress (1): 810 B
                   
Downloading from sonatype-nexus: https://oss.sonatype.org/content/repositories/releases/org/openmrs/openmrs/2.9.0-SNAPSHOT/maven-metadata.xml
Downloaded from archetype: https://mavenrepo.openmrs.org/public/org/openmrs/openmrs/2.9.0-SNAPSHOT/maven-metadata.xml (810 B at 1.8 kB/s)
Downloading from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/openmrs/2.9.0-SNAPSHOT/openmrs-2.9.0-20260904.122951-82.pom
Progress (1): 0.5/45 kB
Progress (1): 1.9/45 kB
Progress (1): 3.3/45 kB
Progress (1): 4.7/45 kB
Progress (1): 6.1/45 kB
Progress (1): 7.5/45 kB
Progress (1): 8.9/45 kB
Progress (1): 10/45 kB 
Progress (1): 12/45 kB
Progress (1): 13/45 kB
Progress (1): 15/45 kB
Progress (1): 16/45 kB
Progress (1): 16/45 kB
Progress (1): 17/45 kB
Progress (1): 19/45 kB
Progress (1): 20/45 kB
Progress (1): 22/45 kB
Progress (1): 23/45 kB
Progress (1): 24/45 kB
Progress (1): 26/45 kB
Progress (1): 27/45 kB
Progress (1): 29/45 kB
Progress (1): 30/45 kB
Progress (1): 31/45 kB
Progress (1): 33/45 kB
Progress (1): 34/45 kB
Progress (1): 36/45 kB
Progress (1): 37/45 kB
Progress (1): 38/45 kB
Progress (1): 40/45 kB
Progress (1): 41/45 kB
Progress (1): 43/45 kB
Progress (1): 44/45 kB
Progress (1): 45 kB   
                   
Downloaded from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/openmrs/2.9.0-SNAPSHOT/openmrs-2.9.0-20260904.122951-82.pom (45 kB at 55 kB/s)
Downloading from openmrs-repo-thirdparty: https://mavenrepo.openmrs.org/thirdparty/org/openmrs/module/llama-server-natives/1.0.0-SNAPSHOT/maven-metadata.xml
Downloading from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/module/llama-server-natives/1.0.0-SNAPSHOT/maven-metadata.xml
Downloading from archetype: https://mavenrepo.openmrs.org/public/org/openmrs/module/llama-server-natives/1.0.0-SNAPSHOT/maven-metadata.xml
Downloading from mks-nexus-public: https://nexus.mekomsolutions.net/repository/maven-public/org/openmrs/module/llama-server-natives/1.0.0-SNAPSHOT/maven-metadata.xml
Downloading from sonatype-central-snapshots: https://central.sonatype.com/repository/maven-snapshots/org/openmrs/module/llama-server-natives/1.0.0-SNAPSHOT/maven-metadata.xml
Progress (1): 0.5/2.1 kB
Progress (1): 1.9/2.1 kB
Progress (2): 1.9/2.1 kB | 0.5/2.1 kB
                                     
Downloading from sonatype-nexus: https://oss.sonatype.org/content/repositories/releases/org/openmrs/module/llama-server-natives/1.0.0-SNAPSHOT/maven-metadata.xml
Progress (2): 2.1 kB | 0.5/2.1 kB
Progress (2): 2.1 kB | 1.9/2.1 kB
Progress (2): 2.1 kB | 2.1 kB    
                             
Downloaded from archetype: https://mavenrepo.openmrs.org/public/org/openmrs/module/llama-server-natives/1.0.0-SNAPSHOT/maven-metadata.xml (2.1 kB at 4.8 kB/s)
Downloaded from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/module/llama-server-natives/1.0.0-SNAPSHOT/maven-metadata.xml (2.1 kB at 4.8 kB/s)
Downloading from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/module/querystore-api/1.0.0-SNAPSHOT/maven-metadata.xml
Downloading from archetype: https://mavenrepo.openmrs.org/public/org/openmrs/module/querystore-api/1.0.0-SNAPSHOT/maven-metadata.xml
Downloading from mks-nexus-public: https://nexus.mekomsolutions.net/repository/maven-public/org/openmrs/module/querystore-api/1.0.0-SNAPSHOT/maven-metadata.xml
Downloading from openmrs-repo-thirdparty: https://mavenrepo.openmrs.org/thirdparty/org/openmrs/module/querystore-api/1.0.0-SNAPSHOT/maven-metadata.xml
Downloading from sonatype-central-snapshots: https://central.sonatype.com/repository/maven-snapshots/org/openmrs/module/querystore-api/1.0.0-SNAPSHOT/maven-metadata.xml
Downloading from sonatype-nexus: https://oss.sonatype.org/content/repositories/releases/org/openmrs/module/querystore-api/1.0.0-SNAPSHOT/maven-metadata.xml
Progress (1): 545/788 B
Progress (1): 788 B    
                   
Downloaded from archetype: https://mavenrepo.openmrs.org/public/org/openmrs/module/querystore-api/1.0.0-SNAPSHOT/maven-metadata.xml (788 B at 404 B/s)
Progress (1): 545/788 B
Progress (1): 788 B    
                   
Downloaded from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/module/querystore-api/1.0.0-SNAPSHOT/maven-metadata.xml (788 B at 401 B/s)
Downloading from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/module/querystore/1.0.0-SNAPSHOT/maven-metadata.xml
Downloading from archetype: https://mavenrepo.openmrs.org/public/org/openmrs/module/querystore/1.0.0-SNAPSHOT/maven-metadata.xml
Downloading from openmrs-repo-thirdparty: https://mavenrepo.openmrs.org/thirdparty/org/openmrs/module/querystore/1.0.0-SNAPSHOT/maven-metadata.xml
Downloading from mks-nexus-public: https://nexus.mekomsolutions.net/repository/maven-public/org/openmrs/module/querystore/1.0.0-SNAPSHOT/maven-metadata.xml
Downloading from sonatype-central-snapshots: https://central.sonatype.com/repository/maven-snapshots/org/openmrs/module/querystore/1.0.0-SNAPSHOT/maven-metadata.xml
Downloading from sonatype-nexus: https://oss.sonatype.org/content/repositories/releases/org/openmrs/module/querystore/1.0.0-SNAPSHOT/maven-metadata.xml
Progress (1): 545/609 B
Progress (1): 609 B    
                   
Downloaded from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/module/querystore/1.0.0-SNAPSHOT/maven-metadata.xml (609 B at 523 B/s)
Progress (1): 545/609 B
Progress (1): 609 B    
                   
Downloaded from archetype: https://mavenrepo.openmrs.org/public/org/openmrs/module/querystore/1.0.0-SNAPSHOT/maven-metadata.xml (609 B at 521 B/s)
Downloading from archetype: https://mavenrepo.openmrs.org/public/org/openmrs/test/openmrs-test/2.9.0-SNAPSHOT/maven-metadata.xml
Downloading from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/test/openmrs-test/2.9.0-SNAPSHOT/maven-metadata.xml
Downloading from openmrs-repo-thirdparty: https://mavenrepo.openmrs.org/thirdparty/org/openmrs/test/openmrs-test/2.9.0-SNAPSHOT/maven-metadata.xml
Downloading from mks-nexus-public: https://nexus.mekomsolutions.net/repository/maven-public/org/openmrs/test/openmrs-test/2.9.0-SNAPSHOT/maven-metadata.xml
Downloading from sonatype-central-snapshots: https://central.sonatype.com/repository/maven-snapshots/org/openmrs/test/openmrs-test/2.9.0-SNAPSHOT/maven-metadata.xml
Progress (1): 545/820 B
Progress (2): 545/820 B | 545/820 B
Progress (2): 545/820 B | 820 B    
Progress (2): 820 B | 820 B    
                           
Downloaded from archetype: https://mavenrepo.openmrs.org/public/org/openmrs/test/openmrs-test/2.9.0-SNAPSHOT/maven-metadata.xml (820 B at 1.9 kB/s)
Downloaded from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/test/openmrs-test/2.9.0-SNAPSHOT/maven-metadata.xml (820 B at 1.9 kB/s)
Downloading from sonatype-nexus: https://oss.sonatype.org/content/repositories/releases/org/openmrs/test/openmrs-test/2.9.0-SNAPSHOT/maven-metadata.xml
Downloading from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/test/openmrs-test/2.9.0-SNAPSHOT/openmrs-test-2.9.0-20260904.122951-82.pom
Progress (1): 0.4/4.1 kB
Progress (1): 1.8/4.1 kB
Progress (1): 3.2/4.1 kB
Progress (1): 4.1 kB    
                    
Downloaded from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/test/openmrs-test/2.9.0-SNAPSHOT/openmrs-test-2.9.0-20260904.122951-82.pom (4.1 kB at 9.3 kB/s)
Downloading from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/api/openmrs-api/2.9.0-SNAPSHOT/openmrs-api-2.9.0-20260904.122951-82.jar
Progress (1): 0/3.4 MB
Progress (1): 0/3.4 MB
Progress (1): 0/3.4 MB
Progress (1): 0/3.4 MB
Progress (1): 0.1/3.4 MB
Progress (1): 0.1/3.4 MB
Progress (1): 0.1/3.4 MB
Progress (1): 0.1/3.4 MB
Progress (1): 0.1/3.4 MB
Progress (1): 0.1/3.4 MB
Progress (1): 0.1/3.4 MB
Progress (1): 0.1/3.4 MB
Progress (1): 0.1/3.4 MB
Progress (1): 0.1/3.4 MB
Progress (1): 0.1/3.4 MB
Progress (1): 0.1/3.4 MB
Progress (1): 0.1/3.4 MB
Progress (1): 0.2/3.4 MB
Progress (1): 0.2/3.4 MB
Progress (1): 0.2/3.4 MB
Progress (1): 0.2/3.4 MB
Progress (1): 0.2/3.4 MB
Progress (1): 0.2/3.4 MB
Progress (1): 0.2/3.4 MB
Progress (1): 0.2/3.4 MB
Progress (1): 0.2/3.4 MB
Progress (1): 0.2/3.4 MB
Progress (1): 0.2/3.4 MB
Progress (1): 0.2/3.4 MB
Progress (1): 0.3/3.4 MB
Progress (1): 0.3/3.4 MB
Progress (1): 0.3/3.4 MB
Progress (1): 0.3/3.4 MB
Progress (1): 0.3/3.4 MB
Progress (1): 0.3/3.4 MB
Progress (1): 0.3/3.4 MB
Progress (1): 0.3/3.4 MB
Progress (1): 0.3/3.4 MB
Progress (1): 0.3/3.4 MB
Progress (1): 0.3/3.4 MB
Progress (1): 0.3/3.4 MB
Progress (1): 0.4/3.4 MB
Progress (1): 0.4/3.4 MB
Progress (1): 0.4/3.4 MB
Progress (1): 0.4/3.4 MB
Progress (1): 0.4/3.4 MB
Progress (1): 0.4/3.4 MB
Progress (1): 0.4/3.4 MB
Progress (1): 0.4/3.4 MB
Progress (1): 0.4/3.4 MB
Progress (1): 0.4/3.4 MB
Progress (1): 0.5/3.4 MB
Progress (1): 0.5/3.4 MB
Progress (1): 0.5/3.4 MB
Progress (1): 0.5/3.4 MB
Progress (1): 0.5/3.4 MB
Progress (1): 0.5/3.4 MB
Progress (1): 0.5/3.4 MB
Progress (1): 0.5/3.4 MB
Progress (1): 0.5/3.4 MB
Progress (1): 0.5/3.4 MB
Progress (1): 0.5/3.4 MB
Progress (1): 0.5/3.4 MB
Progress (1): 0.6/3.4 MB
Progress (1): 0.6/3.4 MB
Progress (1): 0.6/3.4 MB
Progress (1): 0.6/3.4 MB
Progress (1): 0.6/3.4 MB
Progress (1): 0.6/3.4 MB
Progress (1): 0.6/3.4 MB
Progress (1): 0.6/3.4 MB
Progress (1): 0.6/3.4 MB
Progress (1): 0.6/3.4 MB
Progress (1): 0.6/3.4 MB
Progress (1): 0.6/3.4 MB
Progress (1): 0.7/3.4 MB
Progress (1): 0.7/3.4 MB
Progress (1): 0.7/3.4 MB
Progress (1): 0.7/3.4 MB
Progress (1): 0.7/3.4 MB
Progress (1): 0.7/3.4 MB
Progress (1): 0.7/3.4 MB
Progress (1): 0.7/3.4 MB
Progress (1): 0.7/3.4 MB
Progress (1): 0.7/3.4 MB
Progress (1): 0.7/3.4 MB
Progress (1): 0.7/3.4 MB
Progress (1): 0.8/3.4 MB
Progress (1): 0.8/3.4 MB
Progress (1): 0.8/3.4 MB
Progress (1): 0.8/3.4 MB
Progress (1): 0.8/3.4 MB
Progress (1): 0.8/3.4 MB
Progress (1): 0.8/3.4 MB
Progress (1): 0.8/3.4 MB
Progress (1): 0.8/3.4 MB
Progress (1): 0.8/3.4 MB
Progress (1): 0.9/3.4 MB
Progress (1): 0.9/3.4 MB
Progress (1): 0.9/3.4 MB
Progress (1): 0.9/3.4 MB
Progress (1): 0.9/3.4 MB
Progress (1): 0.9/3.4 MB
Progress (1): 0.9/3.4 MB
Progress (1): 0.9/3.4 MB
Progress (1): 0.9/3.4 MB
Progress (1): 0.9/3.4 MB
Progress (1): 0.9/3.4 MB
Progress (1): 0.9/3.4 MB
Progress (1): 1.0/3.4 MB
Progress (1): 1.0/3.4 MB
Progress (1): 1.0/3.4 MB
Progress (1): 1.0/3.4 MB
Progress (1): 1.0/3.4 MB
Progress (1): 1.0/3.4 MB
Progress (1): 1.0/3.4 MB
Progress (1): 1.0/3.4 MB
Progress (1): 1.0/3.4 MB
Progress (1): 1.0/3.4 MB
Progress (1): 1.0/3.4 MB
Progress (1): 1.0/3.4 MB
Progress (1): 1.1/3.4 MB
Progress (1): 1.1/3.4 MB
Progress (1): 1.1/3.4 MB
Progress (1): 1.1/3.4 MB
Progress (1): 1.1/3.4 MB
Progress (1): 1.1/3.4 MB
Progress (1): 1.1/3.4 MB
Progress (1): 1.1/3.4 MB
Progress (1): 1.1/3.4 MB
Progress (1): 1.1/3.4 MB
Progress (1): 1.1/3.4 MB
Progress (1): 1.2/3.4 MB
Progress (1): 1.2/3.4 MB
Progress (1): 1.2/3.4 MB
Progress (1): 1.2/3.4 MB
Progress (1): 1.2/3.4 MB
Progress (1): 1.2/3.4 MB
Progress (1): 1.2/3.4 MB
Progress (1): 1.2/3.4 MB
Progress (1): 1.2/3.4 MB
Progress (1): 1.2/3.4 MB
Progress (1): 1.2/3.4 MB
Progress (1): 1.3/3.4 MB
Progress (1): 1.3/3.4 MB
Progress (1): 1.3/3.4 MB
Progress (1): 1.3/3.4 MB
Progress (1): 1.3/3.4 MB
Progress (1): 1.3/3.4 MB
Progress (1): 1.3/3.4 MB
Progress (1): 1.3/3.4 MB
Progress (1): 1.3/3.4 MB
Progress (1): 1.3/3.4 MB
Progress (1): 1.3/3.4 MB
Progress (1): 1.3/3.4 MB
Progress (1): 1.4/3.4 MB
Progress (1): 1.4/3.4 MB
Progress (1): 1.4/3.4 MB
Progress (1): 1.4/3.4 MB
Progress (1): 1.4/3.4 MB
Progress (1): 1.4/3.4 MB
Progress (1): 1.4/3.4 MB
Progress (1): 1.4/3.4 MB
Progress (1): 1.4/3.4 MB
Progress (1): 1.4/3.4 MB
Progress (1): 1.4/3.4 MB
Progress (1): 1.4/3.4 MB
Progress (1): 1.5/3.4 MB
Progress (1): 1.5/3.4 MB
Progress (1): 1.5/3.4 MB
Progress (1): 1.5/3.4 MB
Progress (1): 1.5/3.4 MB
Progress (1): 1.5/3.4 MB
Progress (1): 1.5/3.4 MB
Progress (1): 1.5/3.4 MB
Progress (1): 1.5/3.4 MB
Progress (1): 1.5/3.4 MB
Progress (1): 1.5/3.4 MB
Progress (1): 1.6/3.4 MB
Progress (1): 1.6/3.4 MB
Progress (1): 1.6/3.4 MB
Progress (1): 1.6/3.4 MB
Progress (1): 1.6/3.4 MB
Progress (1): 1.6/3.4 MB
Progress (1): 1.6/3.4 MB
Progress (1): 1.6/3.4 MB
Progress (1): 1.6/3.4 MB
Progress (1): 1.6/3.4 MB
Progress (1): 1.6/3.4 MB
Progress (1): 1.7/3.4 MB
Progress (1): 1.7/3.4 MB
Progress (1): 1.7/3.4 MB
Progress (1): 1.7/3.4 MB
Progress (1): 1.7/3.4 MB
Progress (1): 1.7/3.4 MB
Progress (1): 1.7/3.4 MB
Progress (1): 1.7/3.4 MB
Progress (1): 1.7/3.4 MB
Progress (1): 1.7/3.4 MB
Progress (1): 1.7/3.4 MB
Progress (1): 1.7/3.4 MB
Progress (1): 1.8/3.4 MB
Progress (1): 1.8/3.4 MB
Progress (1): 1.8/3.4 MB
Progress (1): 1.8/3.4 MB
Progress (1): 1.8/3.4 MB
Progress (1): 1.8/3.4 MB
Progress (1): 1.8/3.4 MB
Progress (1): 1.8/3.4 MB
Progress (1): 1.8/3.4 MB
Progress (1): 1.8/3.4 MB
Progress (1): 1.8/3.4 MB
Progress (1): 1.8/3.4 MB
Progress (1): 1.9/3.4 MB
Progress (1): 1.9/3.4 MB
Progress (1): 1.9/3.4 MB
Progress (1): 1.9/3.4 MB
Progress (1): 1.9/3.4 MB
Progress (1): 1.9/3.4 MB
Progress (1): 1.9/3.4 MB
Progress (1): 1.9/3.4 MB
Progress (1): 1.9/3.4 MB
Progress (1): 1.9/3.4 MB
Progress (1): 1.9/3.4 MB
Progress (1): 2.0/3.4 MB
Progress (1): 2.0/3.4 MB
Progress (1): 2.0/3.4 MB
Progress (1): 2.0/3.4 MB
Progress (1): 2.0/3.4 MB
Progress (1): 2.0/3.4 MB
Progress (1): 2.0/3.4 MB
Progress (1): 2.0/3.4 MB
Progress (1): 2.0/3.4 MB
Progress (1): 2.0/3.4 MB
Progress (1): 2.0/3.4 MB
Progress (1): 2.1/3.4 MB
Progress (1): 2.1/3.4 MB
Progress (1): 2.1/3.4 MB
Progress (1): 2.1/3.4 MB
Progress (1): 2.1/3.4 MB
Progress (1): 2.1/3.4 MB
Progress (1): 2.1/3.4 MB
Progress (1): 2.1/3.4 MB
Progress (1): 2.1/3.4 MB
Progress (1): 2.1/3.4 MB
Progress (1): 2.1/3.4 MB
Progress (1): 2.1/3.4 MB
Progress (1): 2.2/3.4 MB
Progress (1): 2.2/3.4 MB
Progress (1): 2.2/3.4 MB
Progress (1): 2.2/3.4 MB
Progress (1): 2.2/3.4 MB
Progress (1): 2.2/3.4 MB
Progress (1): 2.2/3.4 MB
Progress (1): 2.2/3.4 MB
Progress (1): 2.2/3.4 MB
Progress (1): 2.2/3.4 MB
Progress (1): 2.2/3.4 MB
Progress (1): 2.2/3.4 MB
Progress (1): 2.3/3.4 MB
Progress (1): 2.3/3.4 MB
Progress (1): 2.3/3.4 MB
Progress (1): 2.3/3.4 MB
Progress (1): 2.3/3.4 MB
Progress (1): 2.3/3.4 MB
Progress (1): 2.3/3.4 MB
Progress (1): 2.3/3.4 MB
Progress (1): 2.3/3.4 MB
Progress (1): 2.3/3.4 MB
Progress (1): 2.4/3.4 MB
Progress (1): 2.4/3.4 MB
Progress (1): 2.4/3.4 MB
Progress (1): 2.4/3.4 MB
Progress (1): 2.4/3.4 MB
Progress (1): 2.4/3.4 MB
Progress (1): 2.4/3.4 MB
Progress (1): 2.4/3.4 MB
Progress (1): 2.4/3.4 MB
Progress (1): 2.4/3.4 MB
Progress (1): 2.4/3.4 MB
Progress (1): 2.4/3.4 MB
Progress (1): 2.5/3.4 MB
Progress (1): 2.5/3.4 MB
Progress (1): 2.5/3.4 MB
Progress (1): 2.5/3.4 MB
Progress (1): 2.5/3.4 MB
Progress (1): 2.5/3.4 MB
Progress (1): 2.5/3.4 MB
Progress (1): 2.5/3.4 MB
Progress (1): 2.5/3.4 MB
Progress (1): 2.5/3.4 MB
Progress (1): 2.5/3.4 MB
Progress (1): 2.5/3.4 MB
Progress (1): 2.6/3.4 MB
Progress (1): 2.6/3.4 MB
Progress (1): 2.6/3.4 MB
Progress (1): 2.6/3.4 MB
Progress (1): 2.6/3.4 MB
Progress (1): 2.6/3.4 MB
Progress (1): 2.6/3.4 MB
Progress (1): 2.6/3.4 MB
Progress (1): 2.6/3.4 MB
Progress (1): 2.6/3.4 MB
Progress (1): 2.6/3.4 MB
Progress (1): 2.6/3.4 MB
Progress (1): 2.7/3.4 MB
Progress (1): 2.7/3.4 MB
Progress (1): 2.7/3.4 MB
Progress (1): 2.7/3.4 MB
Progress (1): 2.7/3.4 MB
Progress (1): 2.7/3.4 MB
Progress (1): 2.7/3.4 MB
Progress (1): 2.7/3.4 MB
Progress (1): 2.7/3.4 MB
Progress (1): 2.7/3.4 MB
Progress (1): 2.8/3.4 MB
Progress (1): 2.8/3.4 MB
Progress (1): 2.8/3.4 MB
Progress (1): 2.8/3.4 MB
Progress (1): 2.8/3.4 MB
Progress (1): 2.8/3.4 MB
Progress (1): 2.9/3.4 MB
Progress (1): 2.9/3.4 MB
Progress (1): 2.9/3.4 MB
Progress (1): 2.9/3.4 MB
Progress (1): 2.9/3.4 MB
Progress (1): 2.9/3.4 MB
Progress (1): 3.0/3.4 MB
Progress (1): 3.0/3.4 MB
Progress (1): 3.0/3.4 MB
Progress (1): 3.0/3.4 MB
Progress (1): 3.0/3.4 MB
Progress (1): 3.0/3.4 MB
Progress (1): 3.1/3.4 MB
Progress (1): 3.1/3.4 MB
Progress (1): 3.1/3.4 MB
Progress (1): 3.1/3.4 MB
Progress (1): 3.1/3.4 MB
Progress (1): 3.1/3.4 MB
Progress (1): 3.2/3.4 MB
Progress (1): 3.2/3.4 MB
Progress (1): 3.2/3.4 MB
Progress (1): 3.2/3.4 MB
Progress (1): 3.2/3.4 MB
Progress (1): 3.2/3.4 MB
Progress (1): 3.3/3.4 MB
Progress (1): 3.3/3.4 MB
Progress (1): 3.3/3.4 MB
Progress (1): 3.3/3.4 MB
Progress (1): 3.3/3.4 MB
Progress (1): 3.3/3.4 MB
Progress (1): 3.4/3.4 MB
Progress (1): 3.4 MB    
                    
Downloaded from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/api/openmrs-api/2.9.0-SNAPSHOT/openmrs-api-2.9.0-20260904.122951-82.jar (3.4 MB at 830 kB/s)
Downloading from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/api/openmrs-api/2.9.0-SNAPSHOT/openmrs-api-2.9.0-20260904.122951-82-tests.jar
Progress (1): 0/2.5 MB
Progress (1): 0/2.5 MB
Progress (1): 0/2.5 MB
Progress (1): 0/2.5 MB
Progress (1): 0.1/2.5 MB
Progress (1): 0.1/2.5 MB
Progress (1): 0.1/2.5 MB
Progress (1): 0.1/2.5 MB
Progress (1): 0.1/2.5 MB
Progress (1): 0.1/2.5 MB
Progress (1): 0.1/2.5 MB
Progress (1): 0.1/2.5 MB
Progress (1): 0.1/2.5 MB
Progress (1): 0.1/2.5 MB
Progress (1): 0.1/2.5 MB
Progress (1): 0.2/2.5 MB
Progress (1): 0.2/2.5 MB
Progress (1): 0.2/2.5 MB
Progress (1): 0.2/2.5 MB
Progress (1): 0.2/2.5 MB
Progress (1): 0.2/2.5 MB
Progress (1): 0.2/2.5 MB
Progress (1): 0.2/2.5 MB
Progress (1): 0.2/2.5 MB
Progress (1): 0.2/2.5 MB
Progress (1): 0.2/2.5 MB
Progress (1): 0.2/2.5 MB
Progress (1): 0.2/2.5 MB
Progress (1): 0.3/2.5 MB
Progress (1): 0.3/2.5 MB
Progress (1): 0.3/2.5 MB
Progress (1): 0.3/2.5 MB
Progress (1): 0.3/2.5 MB
Progress (1): 0.3/2.5 MB
Progress (1): 0.3/2.5 MB
Progress (1): 0.3/2.5 MB
Progress (1): 0.3/2.5 MB
Progress (1): 0.3/2.5 MB
Progress (1): 0.3/2.5 MB
Progress (1): 0.3/2.5 MB
Progress (1): 0.4/2.5 MB
Progress (1): 0.4/2.5 MB
Progress (1): 0.4/2.5 MB
Progress (1): 0.4/2.5 MB
Progress (1): 0.4/2.5 MB
Progress (1): 0.4/2.5 MB
Progress (1): 0.4/2.5 MB
Progress (1): 0.4/2.5 MB
Progress (1): 0.4/2.5 MB
Progress (1): 0.4/2.5 MB
Progress (1): 0.4/2.5 MB
Progress (1): 0.5/2.5 MB
Progress (1): 0.5/2.5 MB
Progress (1): 0.5/2.5 MB
Progress (1): 0.5/2.5 MB
Progress (1): 0.5/2.5 MB
Progress (1): 0.5/2.5 MB
Progress (1): 0.5/2.5 MB
Progress (1): 0.5/2.5 MB
Progress (1): 0.5/2.5 MB
Progress (1): 0.5/2.5 MB
Progress (1): 0.6/2.5 MB
Progress (1): 0.6/2.5 MB
Progress (1): 0.6/2.5 MB
Progress (1): 0.6/2.5 MB
Progress (1): 0.6/2.5 MB
Progress (1): 0.6/2.5 MB
Progress (1): 0.6/2.5 MB
Progress (1): 0.6/2.5 MB
Progress (1): 0.6/2.5 MB
Progress (1): 0.6/2.5 MB
Progress (1): 0.6/2.5 MB
Progress (1): 0.6/2.5 MB
Progress (1): 0.7/2.5 MB
Progress (1): 0.7/2.5 MB
Progress (1): 0.7/2.5 MB
Progress (1): 0.7/2.5 MB
Progress (1): 0.7/2.5 MB
Progress (1): 0.7/2.5 MB
Progress (1): 0.7/2.5 MB
Progress (1): 0.7/2.5 MB
Progress (1): 0.7/2.5 MB
Progress (1): 0.7/2.5 MB
Progress (1): 0.7/2.5 MB
Progress (1): 0.7/2.5 MB
Progress (1): 0.8/2.5 MB
Progress (1): 0.8/2.5 MB
Progress (1): 0.8/2.5 MB
Progress (1): 0.8/2.5 MB
Progress (1): 0.8/2.5 MB
Progress (1): 0.8/2.5 MB
Progress (1): 0.8/2.5 MB
Progress (1): 0.8/2.5 MB
Progress (1): 0.8/2.5 MB
Progress (1): 0.8/2.5 MB
Progress (1): 0.8/2.5 MB
Progress (1): 0.9/2.5 MB
Progress (1): 0.9/2.5 MB
Progress (1): 0.9/2.5 MB
Progress (1): 0.9/2.5 MB
Progress (1): 0.9/2.5 MB
Progress (1): 0.9/2.5 MB
Progress (1): 0.9/2.5 MB
Progress (1): 0.9/2.5 MB
Progress (1): 0.9/2.5 MB
Progress (1): 0.9/2.5 MB
Progress (1): 0.9/2.5 MB
Progress (1): 0.9/2.5 MB
Progress (1): 1.0/2.5 MB
Progress (1): 1.0/2.5 MB
Progress (1): 1.0/2.5 MB
Progress (1): 1.0/2.5 MB
Progress (1): 1.0/2.5 MB
Progress (1): 1.0/2.5 MB
Progress (1): 1.0/2.5 MB
Progress (1): 1.0/2.5 MB
Progress (1): 1.0/2.5 MB
Progress (1): 1.0/2.5 MB
Progress (1): 1.1/2.5 MB
Progress (1): 1.1/2.5 MB
Progress (1): 1.1/2.5 MB
Progress (1): 1.1/2.5 MB
Progress (1): 1.1/2.5 MB
Progress (1): 1.1/2.5 MB
Progress (1): 1.1/2.5 MB
Progress (1): 1.1/2.5 MB
Progress (1): 1.1/2.5 MB
Progress (1): 1.1/2.5 MB
Progress (1): 1.1/2.5 MB
Progress (1): 1.1/2.5 MB
Progress (1): 1.1/2.5 MB
Progress (1): 1.2/2.5 MB
Progress (1): 1.2/2.5 MB
Progress (1): 1.2/2.5 MB
Progress (1): 1.2/2.5 MB
Progress (1): 1.2/2.5 MB
Progress (1): 1.2/2.5 MB
Progress (1): 1.2/2.5 MB
Progress (1): 1.2/2.5 MB
Progress (1): 1.2/2.5 MB
Progress (1): 1.2/2.5 MB
Progress (1): 1.2/2.5 MB
Progress (1): 1.2/2.5 MB
Progress (1): 1.3/2.5 MB
Progress (1): 1.3/2.5 MB
Progress (1): 1.3/2.5 MB
Progress (1): 1.3/2.5 MB
Progress (1): 1.3/2.5 MB
Progress (1): 1.3/2.5 MB
Progress (1): 1.3/2.5 MB
Progress (1): 1.3/2.5 MB
Progress (1): 1.3/2.5 MB
Progress (1): 1.3/2.5 MB
Progress (1): 1.3/2.5 MB
Progress (1): 1.4/2.5 MB
Progress (1): 1.4/2.5 MB
Progress (1): 1.4/2.5 MB
Progress (1): 1.4/2.5 MB
Progress (1): 1.4/2.5 MB
Progress (1): 1.4/2.5 MB
Progress (1): 1.4/2.5 MB
Progress (1): 1.4/2.5 MB
Progress (1): 1.4/2.5 MB
Progress (1): 1.4/2.5 MB
Progress (1): 1.4/2.5 MB
Progress (1): 1.4/2.5 MB
Progress (1): 1.5/2.5 MB
Progress (1): 1.5/2.5 MB
Progress (1): 1.5/2.5 MB
Progress (1): 1.5/2.5 MB
Progress (1): 1.5/2.5 MB
Progress (1): 1.5/2.5 MB
Progress (1): 1.5/2.5 MB
Progress (1): 1.5/2.5 MB
Progress (1): 1.5/2.5 MB
Progress (1): 1.5/2.5 MB
Progress (1): 1.5/2.5 MB
Progress (1): 1.6/2.5 MB
Progress (1): 1.6/2.5 MB
Progress (1): 1.6/2.5 MB
Progress (1): 1.6/2.5 MB
Progress (1): 1.6/2.5 MB
Progress (1): 1.6/2.5 MB
Progress (1): 1.6/2.5 MB
Progress (1): 1.6/2.5 MB
Progress (1): 1.6/2.5 MB
Progress (1): 1.6/2.5 MB
Progress (1): 1.6/2.5 MB
Progress (1): 1.7/2.5 MB
Progress (1): 1.7/2.5 MB
Progress (1): 1.7/2.5 MB
Progress (1): 1.7/2.5 MB
Progress (1): 1.7/2.5 MB
Progress (1): 1.7/2.5 MB
Progress (1): 1.7/2.5 MB
Progress (1): 1.7/2.5 MB
Progress (1): 1.7/2.5 MB
Progress (1): 1.7/2.5 MB
Progress (1): 1.7/2.5 MB
Progress (1): 1.8/2.5 MB
Progress (1): 1.8/2.5 MB
Progress (1): 1.8/2.5 MB
Progress (1): 1.8/2.5 MB
Progress (1): 1.8/2.5 MB
Progress (1): 1.8/2.5 MB
Progress (1): 1.8/2.5 MB
Progress (1): 1.8/2.5 MB
Progress (1): 1.8/2.5 MB
Progress (1): 1.8/2.5 MB
Progress (1): 1.8/2.5 MB
Progress (1): 1.9/2.5 MB
Progress (1): 1.9/2.5 MB
Progress (1): 1.9/2.5 MB
Progress (1): 1.9/2.5 MB
Progress (1): 1.9/2.5 MB
Progress (1): 1.9/2.5 MB
Progress (1): 1.9/2.5 MB
Progress (1): 1.9/2.5 MB
Progress (1): 1.9/2.5 MB
Progress (1): 1.9/2.5 MB
Progress (1): 1.9/2.5 MB
Progress (1): 1.9/2.5 MB
Progress (1): 2.0/2.5 MB
Progress (1): 2.0/2.5 MB
Progress (1): 2.0/2.5 MB
Progress (1): 2.0/2.5 MB
Progress (1): 2.0/2.5 MB
Progress (1): 2.0/2.5 MB
Progress (1): 2.0/2.5 MB
Progress (1): 2.0/2.5 MB
Progress (1): 2.0/2.5 MB
Progress (1): 2.0/2.5 MB
Progress (1): 2.0/2.5 MB
Progress (1): 2.1/2.5 MB
Progress (1): 2.1/2.5 MB
Progress (1): 2.1/2.5 MB
Progress (1): 2.1/2.5 MB
Progress (1): 2.1/2.5 MB
Progress (1): 2.1/2.5 MB
Progress (1): 2.1/2.5 MB
Progress (1): 2.1/2.5 MB
Progress (1): 2.1/2.5 MB
Progress (1): 2.1/2.5 MB
Progress (1): 2.1/2.5 MB
Progress (1): 2.1/2.5 MB
Progress (1): 2.2/2.5 MB
Progress (1): 2.2/2.5 MB
Progress (1): 2.2/2.5 MB
Progress (1): 2.2/2.5 MB
Progress (1): 2.2/2.5 MB
Progress (1): 2.2/2.5 MB
Progress (1): 2.2/2.5 MB
Progress (1): 2.2/2.5 MB
Progress (1): 2.2/2.5 MB
Progress (1): 2.2/2.5 MB
Progress (1): 2.2/2.5 MB
Progress (1): 2.2/2.5 MB
Progress (1): 2.3/2.5 MB
Progress (1): 2.3/2.5 MB
Progress (1): 2.3/2.5 MB
Progress (1): 2.3/2.5 MB
Progress (1): 2.3/2.5 MB
Progress (1): 2.3/2.5 MB
Progress (1): 2.3/2.5 MB
Progress (1): 2.3/2.5 MB
Progress (1): 2.3/2.5 MB
Progress (1): 2.3/2.5 MB
Progress (1): 2.3/2.5 MB
Progress (1): 2.3/2.5 MB
Progress (1): 2.4/2.5 MB
Progress (1): 2.4/2.5 MB
Progress (1): 2.4/2.5 MB
Progress (1): 2.4/2.5 MB
Progress (1): 2.4/2.5 MB
Progress (1): 2.4/2.5 MB
Progress (1): 2.4/2.5 MB
Progress (1): 2.4/2.5 MB
Progress (1): 2.4/2.5 MB
Progress (1): 2.4/2.5 MB
Progress (1): 2.4/2.5 MB
Progress (1): 2.5/2.5 MB
Progress (1): 2.5/2.5 MB
Progress (1): 2.5/2.5 MB
Progress (1): 2.5/2.5 MB
Progress (1): 2.5/2.5 MB
Progress (1): 2.5/2.5 MB
Progress (1): 2.5/2.5 MB
Progress (1): 2.5/2.5 MB
Progress (1): 2.5 MB    
                    
Downloaded from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/api/openmrs-api/2.9.0-SNAPSHOT/openmrs-api-2.9.0-20260904.122951-82-tests.jar (2.5 MB at 989 kB/s)
[INFO] 
[INFO] --- clean:3.2.0:clean (default-clean) @ chartsearchai-api ---
[INFO] Deleting /Users/danielkayiwa/.claude/pipeline/worktrees/openmrs-openmrs-module-chartsearchai-377/api/target
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ chartsearchai-api ---
[INFO] Copying 8 resources from src/main/resources to target/classes
[INFO] The encoding used to copy filtered properties files have not been set. This means that the same encoding will be used to copy filtered properties files as when copying other filtered resources. This might not be what you want! Run your build with --debug to see which files might be affected. Read more at https://maven.apache.org/plugins/maven-resources-plugin/examples/filtering-properties-files.html
[INFO] 
[INFO] --- compiler:3.13.0:compile (default-compile) @ chartsearchai-api ---
[INFO] Recompiling the module because of changed source code.
[INFO] Compiling 66 source files with javac [debug target 11] to target/classes
[WARNING] system modules path not set in conjunction with -source 11
[INFO] Annotation processing is enabled because one or more processors were found
  on the class path. A future release of javac may disable annotation processing
  unless at least one processor is specified by name (-processor), or a search
  path is specified (--processor-path, --processor-module-path), or annotation
  processing is enabled explicitly (-proc:only, -proc:full).
  Use -Xlint:-options to suppress this message.
  Use -proc:none to disable annotation processing.
[INFO] /Users/danielkayiwa/.claude/pipeline/worktrees/openmrs-openmrs-module-chartsearchai-377/api/src/main/java/org/openmrs/module/chartsearchai/ChartSearchAiModuleActivator.java: Some input files use or override a deprecated API.
[INFO] /Users/danielkayiwa/.claude/pipeline/worktrees/openmrs-openmrs-module-chartsearchai-377/api/src/main/java/org/openmrs/module/chartsearchai/ChartSearchAiModuleActivator.java: Recompile with -Xlint:deprecation for details.
[INFO] 
[INFO] --- resources:3.3.1:testResources (default-testResources) @ chartsearchai-api ---
[INFO] Copying 130 resources from src/test/resources to target/test-classes
[INFO] 
[INFO] --- compiler:3.13.0:testCompile (default-testCompile) @ chartsearchai-api ---
[INFO] Recompiling the module because of changed dependency.
[INFO] Compiling 197 source files with javac [debug target 11] to target/test-classes
[WARNING] system modules path not set in conjunction with -source 11
[INFO] Annotation processing is enabled because one or more processors were found
  on the class path. A future release of javac may disable annotation processing
  unless at least one processor is specified by name (-processor), or a search
  path is specified (--processor-path, --processor-module-path), or annotation
  processing is enabled explicitly (-proc:only, -proc:full).
  Use -Xlint:-options to suppress this message.
  Use -proc:none to disable annotation processing.
[INFO] /Users/danielkayiwa/.claude/pipeline/worktrees/openmrs-openmrs-module-chartsearchai-377/api/src/test/java/org/openmrs/module/chartsearchai/ChartSearchAiModuleActivatorTest.java: Some input files use or override a deprecated API.
[INFO] /Users/danielkayiwa/.claude/pipeline/worktrees/openmrs-openmrs-module-chartsearchai-377/api/src/test/java/org/openmrs/module/chartsearchai/ChartSearchAiModuleActivatorTest.java: Recompile with -Xlint:deprecation for details.
[INFO] 
[INFO] --- surefire:3.5.5:test (default-test) @ chartsearchai-api ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running org.openmrs.module.chartsearchai.ChartSearchAiReferenceGroupTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 5.352 s -- in org.openmrs.module.chartsearchai.ChartSearchAiReferenceGroupTest
[INFO] Running org.openmrs.module.chartsearchai.MedicationOrderRecordTypeTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 s -- in org.openmrs.module.chartsearchai.MedicationOrderRecordTypeTest
[INFO] Running org.openmrs.module.chartsearchai.ProjectInstructionsGuardTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.916 s -- in org.openmrs.module.chartsearchai.ProjectInstructionsGuardTest
[INFO] Running org.openmrs.module.chartsearchai.serializer.PatientChartSerializerTest
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.012 s -- in org.openmrs.module.chartsearchai.serializer.PatientChartSerializerTest
[INFO] Running org.openmrs.module.chartsearchai.util.ConceptNameUtilTest
INFO - StorageServiceCondition.matches(38) |2026-09-07T00:25:58,534| Selected storage type: local
INFO - StorageServiceCondition.matches(38) |2026-09-07T00:25:58,856| Selected storage type: local
INFO - StorageServiceCondition.matches(38) |2026-09-07T00:25:58,865| Selected storage type: local
WARN - JbossMarshallingModule.cacheManagerStarting(30) |2026-09-07T00:26:00,733| ISPN000554: jboss-marshalling is deprecated and planned for removal
WARN - JbossMarshallingModule.cacheManagerStarting(30) |2026-09-07T00:26:02,517| ISPN000554: jboss-marshalling is deprecated and planned for removal
WARN - AuthorizationAdvice.warnMethodIsUnguarded(177) |2026-09-07T00:26:10,403| No @Authorized annotation applies to org.openmrs.api.AdministrationService.getAllowedLocales(), directly or through any method it overrides, so calls to it are not privilege checked
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 12.38 s -- in org.openmrs.module.chartsearchai.util.ConceptNameUtilTest
[INFO] Running org.openmrs.module.chartsearchai.util.DateFormatUtilTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 s -- in org.openmrs.module.chartsearchai.util.DateFormatUtilTest
[INFO] Running org.openmrs.module.chartsearchai.ChartSearchAiUtilsTest
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.071 s -- in org.openmrs.module.chartsearchai.ChartSearchAiUtilsTest
[INFO] Running org.openmrs.module.chartsearchai.model.AuditLogSchemaMappingTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.006 s -- in org.openmrs.module.chartsearchai.model.AuditLogSchemaMappingTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.PromptInjectionEvalTest
[WARNING] Tests run: 33, Failures: 0, Errors: 0, Skipped: 33, Time elapsed: 0.055 s -- in org.openmrs.module.chartsearchai.api.impl.PromptInjectionEvalTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.LlmProviderEntailmentTest
WARN - LlmAnswerExtractor.extractResponse(149) |2026-09-07T00:26:10,605| LLM response did not parse as JSON (possibly truncated), attempting regex extraction
WARN - LlmAnswerExtractor.extractResponse(149) |2026-09-07T00:26:10,605| LLM response did not parse as JSON (possibly truncated), attempting regex extraction
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 s -- in org.openmrs.module.chartsearchai.api.impl.LlmProviderEntailmentTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.CountingQueryStoreStubTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 s -- in org.openmrs.module.chartsearchai.api.impl.CountingQueryStoreStubTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilderScopedTest
WARN - QueryStoreChartBuilder.buildScoped(321) |2026-09-07T00:26:10,616| getPatientChart returned 10000 docs for patient [uuid=uuid-1] — at querystore's ES tier cap; typed slices may silently omit records older than the cutoff.
WARN - QueryStoreChartBuilder.readOrderCurrency(760) |2026-09-07T00:26:10,621| Could not read orders for patient [uuid=uuid-1] — this chart's drug-order records will not say whether each prescription is still in force, so the answer may infer it from the record's dates. Chart assembly is otherwise unaffected.
org.openmrs.api.APIException: A user context must first be passed to setUserContext()...use Context.openSession() (and closeSession() to prevent memory leaks!) before using the API
	at org.openmrs.api.context.Context.getUserContext(Context.java:260) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.openmrs.api.context.Context.removeProxyPrivilege(Context.java:860) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.openmrs.aop.AuthorizationAdvice.before(AuthorizationAdvice.java:122) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor.invoke(MethodBeforeAdviceInterceptor.java:57) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:241) ~[spring-aop-5.3.30.jar:5.3.30]
	at jdk.proxy3/jdk.proxy3.$Proxy221.getAllOrdersByPatient(Unknown Source) ~[?:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:344) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:198) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke(ExposeInvocationInterceptor.java:97) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:241) ~[spring-aop-5.3.30.jar:5.3.30]
	at jdk.proxy3/jdk.proxy3.$Proxy221.getAllOrdersByPatient(Unknown Source) ~[?:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.resolveAllOrders(QueryStoreChartBuilder.java:874) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.readOrderCurrency(QueryStoreChartBuilder.java:757) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.toSerializedRecords(QueryStoreChartBuilder.java:629) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.buildScoped(QueryStoreChartBuilder.java:367) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilderScopedTest.buildScoped_shouldStillDeclareCompleteness_atTheQuerystoreChartCap(QueryStoreChartBuilderScopedTest.java:321) ~[test-classes/:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:767) ~[junit-platform-commons-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$8(TestMethodTestDescriptor.java:217) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:213) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:138) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:156) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.executeWithoutCancellationToken(LauncherAdapter.java:60) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.execute(LauncherAdapter.java:52) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:203) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:168) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:136) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) [surefire-booter-3.5.5.jar:3.5.5]
WARN - QueryStoreChartBuilder.readOrderCurrency(760) |2026-09-07T00:26:10,628| Could not read orders for patient [uuid=uuid-1] — this chart's drug-order records will not say whether each prescription is still in force, so the answer may infer it from the record's dates. Chart assembly is otherwise unaffected.
org.openmrs.api.APIException: A user context must first be passed to setUserContext()...use Context.openSession() (and closeSession() to prevent memory leaks!) before using the API
	at org.openmrs.api.context.Context.getUserContext(Context.java:260) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.openmrs.api.context.Context.removeProxyPrivilege(Context.java:860) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.openmrs.aop.AuthorizationAdvice.before(AuthorizationAdvice.java:122) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor.invoke(MethodBeforeAdviceInterceptor.java:57) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:241) ~[spring-aop-5.3.30.jar:5.3.30]
	at jdk.proxy3/jdk.proxy3.$Proxy221.getAllOrdersByPatient(Unknown Source) ~[?:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:344) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:198) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke(ExposeInvocationInterceptor.java:97) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:241) ~[spring-aop-5.3.30.jar:5.3.30]
	at jdk.proxy3/jdk.proxy3.$Proxy221.getAllOrdersByPatient(Unknown Source) ~[?:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.resolveAllOrders(QueryStoreChartBuilder.java:874) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.readOrderCurrency(QueryStoreChartBuilder.java:757) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.toSerializedRecords(QueryStoreChartBuilder.java:629) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.buildScoped(QueryStoreChartBuilder.java:367) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilderScopedTest.buildScoped_shouldKeepAllergiesTypedComplete_forAdverseReactionPhrasings(QueryStoreChartBuilderScopedTest.java:140) ~[test-classes/:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:767) ~[junit-platform-commons-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$8(TestMethodTestDescriptor.java:217) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:213) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:138) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:156) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.executeWithoutCancellationToken(LauncherAdapter.java:60) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.execute(LauncherAdapter.java:52) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:203) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:168) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:136) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) [surefire-booter-3.5.5.jar:3.5.5]
WARN - QueryStoreChartBuilder.contributedResourceTypes(933) |2026-09-07T00:26:10,630| QueryScopeContributor [org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilderScopedTest$2] failed; ignoring its scope claim for this query
java.lang.RuntimeException: contributor boom
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilderScopedTest$2.scopedResourceTypes(QueryStoreChartBuilderScopedTest.java:526) ~[test-classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.contributedResourceTypes(QueryStoreChartBuilder.java:922) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.buildScoped(QueryStoreChartBuilder.java:292) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilderScopedTest.buildScoped_shouldSurviveThrowingContributor(QueryStoreChartBuilderScopedTest.java:535) ~[test-classes/:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:767) ~[junit-platform-commons-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$8(TestMethodTestDescriptor.java:217) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:213) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:138) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:156) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.executeWithoutCancellationToken(LauncherAdapter.java:60) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.execute(LauncherAdapter.java:52) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:203) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:168) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:136) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) [surefire-booter-3.5.5.jar:3.5.5]
WARN - QueryStoreChartBuilder.readOrderCurrency(760) |2026-09-07T00:26:10,634| Could not read orders for patient [uuid=uuid-1] — this chart's drug-order records will not say whether each prescription is still in force, so the answer may infer it from the record's dates. Chart assembly is otherwise unaffected.
org.openmrs.api.APIException: A user context must first be passed to setUserContext()...use Context.openSession() (and closeSession() to prevent memory leaks!) before using the API
	at org.openmrs.api.context.Context.getUserContext(Context.java:260) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.openmrs.api.context.Context.removeProxyPrivilege(Context.java:860) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.openmrs.aop.AuthorizationAdvice.before(AuthorizationAdvice.java:122) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor.invoke(MethodBeforeAdviceInterceptor.java:57) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:241) ~[spring-aop-5.3.30.jar:5.3.30]
	at jdk.proxy3/jdk.proxy3.$Proxy221.getAllOrdersByPatient(Unknown Source) ~[?:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:344) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:198) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke(ExposeInvocationInterceptor.java:97) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:241) ~[spring-aop-5.3.30.jar:5.3.30]
	at jdk.proxy3/jdk.proxy3.$Proxy221.getAllOrdersByPatient(Unknown Source) ~[?:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.resolveAllOrders(QueryStoreChartBuilder.java:874) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.readOrderCurrency(QueryStoreChartBuilder.java:757) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.toSerializedRecords(QueryStoreChartBuilder.java:629) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.buildScoped(QueryStoreChartBuilder.java:367) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilderScopedTest.buildScoped_shouldIncludeTheRecencyAnchor_forTemporalQuestions(QueryStoreChartBuilderScopedTest.java:182) ~[test-classes/:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:767) ~[junit-platform-commons-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$8(TestMethodTestDescriptor.java:217) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:213) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:138) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:156) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.executeWithoutCancellationToken(LauncherAdapter.java:60) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.execute(LauncherAdapter.java:52) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:203) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:168) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:136) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) [surefire-booter-3.5.5.jar:3.5.5]
WARN - QueryStoreChartBuilder.readOrderCurrency(760) |2026-09-07T00:26:10,635| Could not read orders for patient [uuid=uuid-1] — this chart's drug-order records will not say whether each prescription is still in force, so the answer may infer it from the record's dates. Chart assembly is otherwise unaffected.
org.openmrs.api.APIException: A user context must first be passed to setUserContext()...use Context.openSession() (and closeSession() to prevent memory leaks!) before using the API
	at org.openmrs.api.context.Context.getUserContext(Context.java:260) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.openmrs.api.context.Context.removeProxyPrivilege(Context.java:860) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.openmrs.aop.AuthorizationAdvice.before(AuthorizationAdvice.java:122) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor.invoke(MethodBeforeAdviceInterceptor.java:57) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:241) ~[spring-aop-5.3.30.jar:5.3.30]
	at jdk.proxy3/jdk.proxy3.$Proxy221.getAllOrdersByPatient(Unknown Source) ~[?:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:344) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:198) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke(ExposeInvocationInterceptor.java:97) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:241) ~[spring-aop-5.3.30.jar:5.3.30]
	at jdk.proxy3/jdk.proxy3.$Proxy221.getAllOrdersByPatient(Unknown Source) ~[?:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.resolveAllOrders(QueryStoreChartBuilder.java:874) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.readOrderCurrency(QueryStoreChartBuilder.java:757) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.toSerializedRecords(QueryStoreChartBuilder.java:629) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.buildScoped(QueryStoreChartBuilder.java:367) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilderScopedTest.buildScoped_shouldIncludeAllTypedRecordsPlusSimilarityHitsPlusPatient(QueryStoreChartBuilderScopedTest.java:101) ~[test-classes/:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:767) ~[junit-platform-commons-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$8(TestMethodTestDescriptor.java:217) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:213) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:138) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:156) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.executeWithoutCancellationToken(LauncherAdapter.java:60) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.execute(LauncherAdapter.java:52) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:203) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:168) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:136) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) [surefire-booter-3.5.5.jar:3.5.5]
WARN - QueryStoreChartBuilder.contributedResourceTypes(910) |2026-09-07T00:26:10,637| Resolving QueryScopeContributor beans failed; proceeding with the built-in scope only
java.lang.RuntimeException: simulated getRegisteredComponents failure
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilderScopedTest$TestableScopedBuilder.resolveScopeContributors(QueryStoreChartBuilderScopedTest.java:577) ~[test-classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.contributedResourceTypes(QueryStoreChartBuilder.java:907) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.buildScoped(QueryStoreChartBuilder.java:292) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilderScopedTest.buildScoped_shouldSurvive_whenContributorResolutionItselfFails(QueryStoreChartBuilderScopedTest.java:553) ~[test-classes/:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:767) ~[junit-platform-commons-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$8(TestMethodTestDescriptor.java:217) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:213) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:138) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:156) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.executeWithoutCancellationToken(LauncherAdapter.java:60) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.execute(LauncherAdapter.java:52) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:203) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:168) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:136) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) [surefire-booter-3.5.5.jar:3.5.5]
WARN - QueryStoreChartBuilder.readOrderCurrency(760) |2026-09-07T00:26:10,637| Could not read orders for patient [uuid=uuid-1] — this chart's drug-order records will not say whether each prescription is still in force, so the answer may infer it from the record's dates. Chart assembly is otherwise unaffected.
org.openmrs.api.APIException: A user context must first be passed to setUserContext()...use Context.openSession() (and closeSession() to prevent memory leaks!) before using the API
	at org.openmrs.api.context.Context.getUserContext(Context.java:260) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.openmrs.api.context.Context.removeProxyPrivilege(Context.java:860) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.openmrs.aop.AuthorizationAdvice.before(AuthorizationAdvice.java:122) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor.invoke(MethodBeforeAdviceInterceptor.java:57) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:241) ~[spring-aop-5.3.30.jar:5.3.30]
	at jdk.proxy3/jdk.proxy3.$Proxy221.getAllOrdersByPatient(Unknown Source) ~[?:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:344) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:198) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke(ExposeInvocationInterceptor.java:97) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:241) ~[spring-aop-5.3.30.jar:5.3.30]
	at jdk.proxy3/jdk.proxy3.$Proxy221.getAllOrdersByPatient(Unknown Source) ~[?:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.resolveAllOrders(QueryStoreChartBuilder.java:874) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.readOrderCurrency(QueryStoreChartBuilder.java:757) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.toSerializedRecords(QueryStoreChartBuilder.java:629) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.buildScoped(QueryStoreChartBuilder.java:367) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilderScopedTest.buildScoped_shouldSurvive_whenContributorResolutionItselfFails(QueryStoreChartBuilderScopedTest.java:553) ~[test-classes/:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:767) ~[junit-platform-commons-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$8(TestMethodTestDescriptor.java:217) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:213) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:138) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:156) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.executeWithoutCancellationToken(LauncherAdapter.java:60) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.execute(LauncherAdapter.java:52) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:203) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:168) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:136) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) [surefire-booter-3.5.5.jar:3.5.5]
WARN - QueryStoreChartBuilder.readOrderCurrency(760) |2026-09-07T00:26:10,638| Could not read orders for patient [uuid=uuid-1] — this chart's drug-order records will not say whether each prescription is still in force, so the answer may infer it from the record's dates. Chart assembly is otherwise unaffected.
org.openmrs.api.APIException: A user context must first be passed to setUserContext()...use Context.openSession() (and closeSession() to prevent memory leaks!) before using the API
	at org.openmrs.api.context.Context.getUserContext(Context.java:260) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.openmrs.api.context.Context.removeProxyPrivilege(Context.java:860) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.openmrs.aop.AuthorizationAdvice.before(AuthorizationAdvice.java:122) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor.invoke(MethodBeforeAdviceInterceptor.java:57) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:241) ~[spring-aop-5.3.30.jar:5.3.30]
	at jdk.proxy3/jdk.proxy3.$Proxy221.getAllOrdersByPatient(Unknown Source) ~[?:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:344) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:198) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke(ExposeInvocationInterceptor.java:97) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:241) ~[spring-aop-5.3.30.jar:5.3.30]
	at jdk.proxy3/jdk.proxy3.$Proxy221.getAllOrdersByPatient(Unknown Source) ~[?:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.resolveAllOrders(QueryStoreChartBuilder.java:874) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.readOrderCurrency(QueryStoreChartBuilder.java:757) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.toSerializedRecords(QueryStoreChartBuilder.java:629) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.buildScoped(QueryStoreChartBuilder.java:367) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilderScopedTest.buildScoped_shouldKeepAllergiesTypedComplete_whenMedicationCuesAlsoMatch(QueryStoreChartBuilderScopedTest.java:121) ~[test-classes/:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:767) ~[junit-platform-commons-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$8(TestMethodTestDescriptor.java:217) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:213) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:138) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:156) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.executeWithoutCancellationToken(LauncherAdapter.java:60) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.execute(LauncherAdapter.java:52) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:203) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:168) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:136) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) [surefire-booter-3.5.5.jar:3.5.5]
WARN - QueryStoreChartBuilder.searchSimilarityUuids(578) |2026-09-07T00:26:10,640| QueryStore.searchByPatient failed for scoped build [uuid=uuid-1] — proceeding with the typed slice only
java.lang.RuntimeException: simulated similarity RPC failure
	at org.openmrs.module.chartsearchai.api.impl.CountingQueryStoreStub.searchByPatient(CountingQueryStoreStub.java:124) ~[test-classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.CountingQueryStoreStub.answer(CountingQueryStoreStub.java:104) ~[test-classes/:?]
	at jdk.proxy2/jdk.proxy2.$Proxy291.searchByPatient(Unknown Source) ~[?:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.searchSimilarityUuids(QueryStoreChartBuilder.java:573) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.buildScoped(QueryStoreChartBuilder.java:332) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilderScopedTest.buildScoped_shouldDegradeToTypedSlice_whenSimilaritySearchFails(QueryStoreChartBuilderScopedTest.java:166) ~[test-classes/:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:767) ~[junit-platform-commons-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$8(TestMethodTestDescriptor.java:217) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:213) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:138) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:156) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.executeWithoutCancellationToken(LauncherAdapter.java:60) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.execute(LauncherAdapter.java:52) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:203) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:168) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:136) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) [surefire-booter-3.5.5.jar:3.5.5]
WARN - QueryStoreChartBuilder.readOrderCurrency(760) |2026-09-07T00:26:10,641| Could not read orders for patient [uuid=uuid-1] — this chart's drug-order records will not say whether each prescription is still in force, so the answer may infer it from the record's dates. Chart assembly is otherwise unaffected.
org.openmrs.api.APIException: A user context must first be passed to setUserContext()...use Context.openSession() (and closeSession() to prevent memory leaks!) before using the API
	at org.openmrs.api.context.Context.getUserContext(Context.java:260) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.openmrs.api.context.Context.removeProxyPrivilege(Context.java:860) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.openmrs.aop.AuthorizationAdvice.before(AuthorizationAdvice.java:122) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor.invoke(MethodBeforeAdviceInterceptor.java:57) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:241) ~[spring-aop-5.3.30.jar:5.3.30]
	at jdk.proxy3/jdk.proxy3.$Proxy221.getAllOrdersByPatient(Unknown Source) ~[?:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:344) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:198) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke(ExposeInvocationInterceptor.java:97) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:241) ~[spring-aop-5.3.30.jar:5.3.30]
	at jdk.proxy3/jdk.proxy3.$Proxy221.getAllOrdersByPatient(Unknown Source) ~[?:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.resolveAllOrders(QueryStoreChartBuilder.java:874) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.readOrderCurrency(QueryStoreChartBuilder.java:757) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.toSerializedRecords(QueryStoreChartBuilder.java:629) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.buildScoped(QueryStoreChartBuilder.java:367) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilderScopedTest.buildScoped_shouldDegradeToTypedSlice_whenSimilaritySearchFails(QueryStoreChartBuilderScopedTest.java:166) ~[test-classes/:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:767) ~[junit-platform-commons-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$8(TestMethodTestDescriptor.java:217) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:213) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:138) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:156) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.executeWithoutCancellationToken(LauncherAdapter.java:60) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.execute(LauncherAdapter.java:52) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:203) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:168) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:136) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) [surefire-booter-3.5.5.jar:3.5.5]
WARN - QueryStoreChartBuilder.readOrderCurrency(760) |2026-09-07T00:26:10,642| Could not read orders for patient [uuid=uuid-1] — this chart's drug-order records will not say whether each prescription is still in force, so the answer may infer it from the record's dates. Chart assembly is otherwise unaffected.
org.openmrs.api.APIException: A user context must first be passed to setUserContext()...use Context.openSession() (and closeSession() to prevent memory leaks!) before using the API
	at org.openmrs.api.context.Context.getUserContext(Context.java:260) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.openmrs.api.context.Context.removeProxyPrivilege(Context.java:860) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.openmrs.aop.AuthorizationAdvice.before(AuthorizationAdvice.java:122) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor.invoke(MethodBeforeAdviceInterceptor.java:57) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:241) ~[spring-aop-5.3.30.jar:5.3.30]
	at jdk.proxy3/jdk.proxy3.$Proxy221.getAllOrdersByPatient(Unknown Source) ~[?:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:344) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:198) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke(ExposeInvocationInterceptor.java:97) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:241) ~[spring-aop-5.3.30.jar:5.3.30]
	at jdk.proxy3/jdk.proxy3.$Proxy221.getAllOrdersByPatient(Unknown Source) ~[?:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.resolveAllOrders(QueryStoreChartBuilder.java:874) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.readOrderCurrency(QueryStoreChartBuilder.java:757) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.toSerializedRecords(QueryStoreChartBuilder.java:629) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.buildScoped(QueryStoreChartBuilder.java:367) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilderScopedTest.buildScoped_shouldNeverRenderFocusIndices(QueryStoreChartBuilderScopedTest.java:338) ~[test-classes/:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:767) ~[junit-platform-commons-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$8(TestMethodTestDescriptor.java:217) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:213) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:138) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:156) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.executeWithoutCancellationToken(LauncherAdapter.java:60) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.execute(LauncherAdapter.java:52) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:203) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:168) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:136) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) [surefire-booter-3.5.5.jar:3.5.5]
WARN - QueryStoreChartBuilder.readOrderCurrency(760) |2026-09-07T00:26:10,644| Could not read orders for patient [uuid=uuid-1] — this chart's drug-order records will not say whether each prescription is still in force, so the answer may infer it from the record's dates. Chart assembly is otherwise unaffected.
org.openmrs.api.APIException: A user context must first be passed to setUserContext()...use Context.openSession() (and closeSession() to prevent memory leaks!) before using the API
	at org.openmrs.api.context.Context.getUserContext(Context.java:260) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.openmrs.api.context.Context.removeProxyPrivilege(Context.java:860) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.openmrs.aop.AuthorizationAdvice.before(AuthorizationAdvice.java:122) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor.invoke(MethodBeforeAdviceInterceptor.java:57) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:241) ~[spring-aop-5.3.30.jar:5.3.30]
	at jdk.proxy3/jdk.proxy3.$Proxy221.getAllOrdersByPatient(Unknown Source) ~[?:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:344) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:198) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke(ExposeInvocationInterceptor.java:97) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:241) ~[spring-aop-5.3.30.jar:5.3.30]
	at jdk.proxy3/jdk.proxy3.$Proxy221.getAllOrdersByPatient(Unknown Source) ~[?:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.resolveAllOrders(QueryStoreChartBuilder.java:874) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.readOrderCurrency(QueryStoreChartBuilder.java:757) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.toSerializedRecords(QueryStoreChartBuilder.java:629) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.buildScoped(QueryStoreChartBuilder.java:367) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilderScopedTest.buildScoped_shouldUnionContributorScope_withBuiltInScope(QueryStoreChartBuilderScopedTest.java:495) ~[test-classes/:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:767) ~[junit-platform-commons-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$8(TestMethodTestDescriptor.java:217) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:213) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:138) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:156) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.executeWithoutCancellationToken(LauncherAdapter.java:60) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.execute(LauncherAdapter.java:52) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:203) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:168) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:136) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) [surefire-booter-3.5.5.jar:3.5.5]
WARN - QueryStoreChartBuilder.readOrderCurrency(760) |2026-09-07T00:26:10,646| Could not read orders for patient [uuid=uuid-1] — this chart's drug-order records will not say whether each prescription is still in force, so the answer may infer it from the record's dates. Chart assembly is otherwise unaffected.
org.openmrs.api.APIException: A user context must first be passed to setUserContext()...use Context.openSession() (and closeSession() to prevent memory leaks!) before using the API
	at org.openmrs.api.context.Context.getUserContext(Context.java:260) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.openmrs.api.context.Context.removeProxyPrivilege(Context.java:860) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.openmrs.aop.AuthorizationAdvice.before(AuthorizationAdvice.java:122) ~[openmrs-api-2.9.0-SNAPSHOT.jar:?]
	at org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor.invoke(MethodBeforeAdviceInterceptor.java:57) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:241) ~[spring-aop-5.3.30.jar:5.3.30]
	at jdk.proxy3/jdk.proxy3.$Proxy221.getAllOrdersByPatient(Unknown Source) ~[?:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:344) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:198) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke(ExposeInvocationInterceptor.java:97) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[spring-aop-5.3.30.jar:5.3.30]
	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:241) ~[spring-aop-5.3.30.jar:5.3.30]
	at jdk.proxy3/jdk.proxy3.$Proxy221.getAllOrdersByPatient(Unknown Source) ~[?:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.resolveAllOrders(QueryStoreChartBuilder.java:874) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.readOrderCurrency(QueryStoreChartBuilder.java:757) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.toSerializedRecords(QueryStoreChartBuilder.java:629) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.buildScoped(QueryStoreChartBuilder.java:367) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilderScopedTest.buildScoped_shouldDeclareTheTypedScopeComplete_soAbsenceIsReadableAsDrift(QueryStoreChartBuilderScopedTest.java:262) ~[test-classes/:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:767) ~[junit-platform-commons-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$8(TestMethodTestDescriptor.java:217) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:213) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:138) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:156) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.executeWithoutCancellationToken(LauncherAdapter.java:60) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.execute(LauncherAdapter.java:52) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:203) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:168) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:136) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) [surefire-booter-3.5.5.jar:3.5.5]
[INFO] Tests run: 25, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.035 s -- in org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilderScopedTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.PrewarmRefreshExecutorTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.934 s -- in org.openmrs.module.chartsearchai.api.impl.PrewarmRefreshExecutorTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.ChartBuildingStrategyDispatchTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 s -- in org.openmrs.module.chartsearchai.api.impl.ChartBuildingStrategyDispatchTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.AbsentDataEvalTest
[WARNING] Tests run: 22, Failures: 0, Errors: 0, Skipped: 19, Time elapsed: 0.022 s -- in org.openmrs.module.chartsearchai.api.impl.AbsentDataEvalTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.LlmInferenceServiceTest
WARN - LlmInferenceService.extractCitedReferences(638) |2026-09-07T00:26:11,611| LLM cited record [99] which does not exist in the provided records
WARN - LlmInferenceService.maybeEmitPreliminaryReasoning(362) |2026-09-07T00:26:11,616| Preliminary reasoning skipped for patient [id=null]: A user context must first be passed to setUserContext()...use Context.openSession() (and closeSession() to prevent memory leaks!) before using the API
WARN - LlmInferenceService.extractCitedReferences(638) |2026-09-07T00:26:11,633| LLM cited record [8] which does not exist in the provided records
WARN - LlmInferenceService.extractCitedReferences(638) |2026-09-07T00:26:11,634| LLM cited record [99] which does not exist in the provided records
[INFO] Tests run: 41, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.026 s -- in org.openmrs.module.chartsearchai.api.impl.LlmInferenceServiceTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.ArchitectureGuardTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.220 s -- in org.openmrs.module.chartsearchai.api.impl.ArchitectureGuardTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.LlmInferenceServiceConditionRuleCoverageContextTest
WARN - AuthorizationAdvice.warnMethodIsUnguarded(177) |2026-09-07T00:26:11,881| No @Authorized annotation applies to org.openmrs.api.AdministrationService.validate(), directly or through any method it overrides, so calls to it are not privilege checked
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.573 s -- in org.openmrs.module.chartsearchai.api.impl.LlmInferenceServiceConditionRuleCoverageContextTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.LlmInferenceServiceAsyncGroundingTest
WARN - LlmInferenceService.maybeEmitPreliminaryReasoning(362) |2026-09-07T00:26:13,430| Preliminary reasoning skipped for patient [id=1]: A user context must first be passed to setUserContext()...use Context.openSession() (and closeSession() to prevent memory leaks!) before using the API
WARN - LlmInferenceService.maybeEmitPreliminaryReasoning(362) |2026-09-07T00:26:13,431| Preliminary reasoning skipped for patient [id=1]: A user context must first be passed to setUserContext()...use Context.openSession() (and closeSession() to prevent memory leaks!) before using the API
WARN - LlmInferenceService.maybeEmitPreliminaryReasoning(362) |2026-09-07T00:26:13,432| Preliminary reasoning skipped for patient [id=1]: A user context must first be passed to setUserContext()...use Context.openSession() (and closeSession() to prevent memory leaks!) before using the API
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in org.openmrs.module.chartsearchai.api.impl.LlmInferenceServiceAsyncGroundingTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.QueryPreprocessorLabExpansionTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.001 s -- in org.openmrs.module.chartsearchai.api.impl.QueryPreprocessorLabExpansionTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.CitationGroundingVerifierTest
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:13,445| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Simvastatin 20mg [order-uuid-simvastatin]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:13,451| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Simvastatin 20mg [order-uuid-simvastatin]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:13,455| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Simvastatin 20mg [order-uuid-simvastatin]]
INFO - CitationGroundingVerifier.verify(793) |2026-09-07T00:26:13,457| Citation grounding: withheld 1 not-entailed verdict(s) whose claim also rested on module-supplied reference material; those citations render unverified (issue #284)
WARN - CitationGroundingVerifier.safeEntailsBatch(1091) |2026-09-07T00:26:13,462| Tier-2 batch entailment failed for 1 citation(s); keeping Tier-1 verdicts
java.lang.RuntimeException: llama-server timed out
	at org.openmrs.module.chartsearchai.api.impl.CitationGroundingVerifierTest$2.entailsBatch(CitationGroundingVerifierTest.java:711) ~[test-classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.CitationGroundingVerifier.safeEntailsBatch(CitationGroundingVerifier.java:1088) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.CitationGroundingVerifier.verify(CitationGroundingVerifier.java:701) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.CitationGroundingVerifierTest.clauseScoped_isolateCitations_fallBackToLazyTier1OnEngineFailure(CitationGroundingVerifierTest.java:720) ~[test-classes/:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:767) ~[junit-platform-commons-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$8(TestMethodTestDescriptor.java:217) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:213) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:138) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:156) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.executeWithoutCancellationToken(LauncherAdapter.java:60) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.execute(LauncherAdapter.java:52) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:203) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:168) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:136) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) [surefire-booter-3.5.5.jar:3.5.5]
WARN - CitationGroundingVerifier.safeEntailsBatch(1091) |2026-09-07T00:26:13,463| Tier-2 batch entailment failed for 1 citation(s); keeping Tier-1 verdicts
java.lang.RuntimeException: llama-server timed out
	at org.openmrs.module.chartsearchai.api.impl.CitationGroundingVerifierTest$2.entailsBatch(CitationGroundingVerifierTest.java:711) ~[test-classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.CitationGroundingVerifier.safeEntailsBatch(CitationGroundingVerifier.java:1088) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.CitationGroundingVerifier.verify(CitationGroundingVerifier.java:701) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.CitationGroundingVerifierTest.clauseScoped_isolateCitations_fallBackToLazyTier1OnEngineFailure(CitationGroundingVerifierTest.java:720) ~[test-classes/:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:767) ~[junit-platform-commons-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$8(TestMethodTestDescriptor.java:217) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:213) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:138) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:156) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.executeWithoutCancellationToken(LauncherAdapter.java:60) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.execute(LauncherAdapter.java:52) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:203) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:168) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:136) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) [surefire-booter-3.5.5.jar:3.5.5]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:13,471| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Simvastatin Co 20mg [order-uuid-7]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:13,474| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Simvastatin 20mg [order-uuid-simvastatin]]
WARN - CitationGroundingVerifier.verify(801) |2026-09-07T00:26:13,477| Citation grounding: could not verify 1 of 1 citation(s) — querystore's embedding provider failed (RuntimeException: ONNX session unavailable); those citations are left unverified (Tier-2 entailment still applies wherever it was asked). Ensure querystore's embedding model is configured.
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:13,487| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Simvastatin 20mg [order-uuid-simvastatin]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:13,491| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Simvastatin 20mg [order-uuid-simvastatin]]
WARN - CitationGroundingVerifier.safeEntailsBatch(1091) |2026-09-07T00:26:13,492| Tier-2 batch entailment failed for 2 citation(s); keeping Tier-1 verdicts
java.lang.RuntimeException: llama-server timed out
	at org.openmrs.module.chartsearchai.api.impl.CitationGroundingVerifierTest$3.entailsBatch(CitationGroundingVerifierTest.java:761) ~[test-classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.CitationGroundingVerifier.safeEntailsBatch(CitationGroundingVerifier.java:1088) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.CitationGroundingVerifier.verify(CitationGroundingVerifier.java:690) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.CitationGroundingVerifier.verify(CitationGroundingVerifier.java:470) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.CitationGroundingVerifierTest.tier2_batchFailure_lazyTier1VerdictMatchesEagerCosine(CitationGroundingVerifierTest.java:770) ~[test-classes/:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:767) ~[junit-platform-commons-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$8(TestMethodTestDescriptor.java:217) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:213) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:138) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:156) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.executeWithoutCancellationToken(LauncherAdapter.java:60) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.execute(LauncherAdapter.java:52) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:203) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:168) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:136) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) [surefire-booter-3.5.5.jar:3.5.5]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:13,494| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Simvastatin 20mg [order-uuid-simvastatin]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:13,496| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Simvastatin Co 20mg [order-uuid-7]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:13,498| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Simvastatin 20mg [order-uuid-simvastatin]]
WARN - CitationGroundingVerifier.safeEntailsBatch(1091) |2026-09-07T00:26:13,499| Tier-2 batch entailment failed for 1 citation(s); keeping Tier-1 verdicts
java.lang.RuntimeException: llama-server timed out
	at org.openmrs.module.chartsearchai.api.impl.CitationGroundingVerifierTest$1.entailsBatch(CitationGroundingVerifierTest.java:332) ~[test-classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.CitationGroundingVerifier.safeEntailsBatch(CitationGroundingVerifier.java:1088) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.CitationGroundingVerifier.verify(CitationGroundingVerifier.java:690) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.CitationGroundingVerifier.verify(CitationGroundingVerifier.java:470) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.CitationGroundingVerifierTest.tier2_llmFailureDegradesToTier1(CitationGroundingVerifierTest.java:341) ~[test-classes/:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:767) ~[junit-platform-commons-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$8(TestMethodTestDescriptor.java:217) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:213) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:138) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:156) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.executeWithoutCancellationToken(LauncherAdapter.java:60) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.execute(LauncherAdapter.java:52) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:203) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:168) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:136) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) [surefire-booter-3.5.5.jar:3.5.5]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:13,503| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Simvastatin 20mg [order-uuid-simvastatin]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:13,506| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Simvastatin 20mg [order-uuid-simvastatin]]
[INFO] Tests run: 98, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.072 s -- in org.openmrs.module.chartsearchai.api.impl.CitationGroundingVerifierTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.SafetyVerdictSeverityGradationTest
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in org.openmrs.module.chartsearchai.api.impl.SafetyVerdictSeverityGradationTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.ClassCodeFidelityTest
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:13,515| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:13,519| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
DEBUG - ClassCodeFidelityCheck.reportClassCodeDefects(245) |2026-09-07T00:26:13,524| Class-code check skipped for patient=1: cited record [99] carries no text
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:13,528| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
WARN - ClassCodeFidelityCheck.reportMalformedParentheticals(411) |2026-09-07T00:26:13,533| Answer for patient=1 states ATC class code(s) [J01MA] more than once inside one parenthetical; nothing this module renders states one code twice inside one. The answer prose is left unchanged (issue #338).
WARN - ClassCodeFidelityCheck.reportClassCodeDefects(279) |2026-09-07T00:26:13,536| Answer for patient=1 states ATC class code(s) [J01M] that no cited record contains; cited record(s) [3] state [J01MA, J01MA12]. The answer prose is left unchanged (issue #142).
DEBUG - ClassCodeFidelityCheck.reportClassCodeDefects(258) |2026-09-07T00:26:13,540| Class-code check skipped for patient=1: no cited record states a class code
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:13,542| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
WARN - LlmInferenceService.maybeEmitPreliminaryReasoning(362) |2026-09-07T00:26:13,545| Preliminary reasoning skipped for patient [id=1]: A user context must first be passed to setUserContext()...use Context.openSession() (and closeSession() to prevent memory leaks!) before using the API
WARN - ClassCodeFidelityCheck.reportClassCodeDefects(279) |2026-09-07T00:26:13,546| Answer for patient=1 states ATC class code(s) [J01CA] that no cited record contains; cited record(s) [3] state [J01MA, J01MA12]. The answer prose is left unchanged (issue #142).
WARN - LlmInferenceService.maybeEmitPreliminaryReasoning(362) |2026-09-07T00:26:13,549| Preliminary reasoning skipped for patient [id=1]: A user context must first be passed to setUserContext()...use Context.openSession() (and closeSession() to prevent memory leaks!) before using the API
WARN - ClassCodeFidelityCheck.reportMalformedParentheticals(411) |2026-09-07T00:26:13,549| Answer for patient=1 states ATC class code(s) [J01MA] more than once inside one parenthetical; nothing this module renders states one code twice inside one. The answer prose is left unchanged (issue #338).
WARN - ClassCodeFidelityCheck.reportMalformedParentheticals(411) |2026-09-07T00:26:13,552| Answer for patient=1 states ATC class code(s) [J01MA] more than once inside one parenthetical; nothing this module renders states one code twice inside one. The answer prose is left unchanged (issue #338).
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:13,554| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
WARN - ClassCodeFidelityCheck.reportClassCodeDefects(279) |2026-09-07T00:26:13,555| Answer for patient=1 states ATC class code(s) [J01CA] that no cited record contains; cited record(s) [1] state [J01MA]. The answer prose is left unchanged (issue #142).
WARN - ClassCodeFidelityCheck.reportMalformedParentheticals(416) |2026-09-07T00:26:13,558| Answer for patient=1 places citation marker(s) inside a parenthetical stating ATC class code(s), one entry per distinct code-and-marker pairing: [[J01MA] with [3]]; a marker attributes the clause and belongs after it. The answer prose is left unchanged (issue #338).
WARN - ClassCodeFidelityCheck.reportMalformedParentheticals(411) |2026-09-07T00:26:13,560| Answer for patient=1 states ATC class code(s) [J01MA] more than once inside one parenthetical; nothing this module renders states one code twice inside one. The answer prose is left unchanged (issue #338).
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:13,565| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
WARN - ClassCodeFidelityCheck.reportMalformedParentheticals(416) |2026-09-07T00:26:13,571| Answer for patient=1 places citation marker(s) inside a parenthetical stating ATC class code(s), one entry per distinct code-and-marker pairing: [[J01MA] with [3]]; a marker attributes the clause and belongs after it. The answer prose is left unchanged (issue #338).
DEBUG - ClassCodeFidelityCheck.reportClassCodeDefects(258) |2026-09-07T00:26:13,573| Class-code check skipped for patient=1: no cited record states a class code
DEBUG - ClassCodeFidelityCheck.reportClassCodeDefects(258) |2026-09-07T00:26:13,576| Class-code check skipped for patient=1: no cited record states a class code
WARN - ClassCodeFidelityCheck.reportMalformedParentheticals(411) |2026-09-07T00:26:13,578| Answer for patient=1 states ATC class code(s) [J01MA] more than once inside one parenthetical; nothing this module renders states one code twice inside one. The answer prose is left unchanged (issue #338).
WARN - ClassCodeFidelityCheck.reportMalformedParentheticals(411) |2026-09-07T00:26:13,579| Answer for patient=1 states ATC class code(s) [J01MA] more than once inside one parenthetical; nothing this module renders states one code twice inside one. The answer prose is left unchanged (issue #338).
WARN - ClassCodeFidelityCheck.reportClassCodeDefects(279) |2026-09-07T00:26:13,584| Answer for patient=1 states ATC class code(s) [J01M] that no cited record contains; cited record(s) [3] state [J01MA, J01MA12]. The answer prose is left unchanged (issue #142).
WARN - ClassCodeFidelityCheck.reportClassCodeDefects(290) |2026-09-07T00:26:13,590| Class-code check failed for patient=1; the answer is unaffected: java.lang.IllegalStateException: record text unavailable
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:13,593| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:13,595| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
WARN - ClassCodeFidelityCheck.reportClassCodeDefects(279) |2026-09-07T00:26:13,597| Answer for patient=1 states ATC class code(s) [S01BA02] that no cited record contains; cited record(s) [3] state [J01MA, J01MA12]. The answer prose is left unchanged (issue #142).
WARN - ClassCodeFidelityCheck.reportMalformedParentheticals(416) |2026-09-07T00:26:13,599| Answer for patient=1 places citation marker(s) inside a parenthetical stating ATC class code(s), one entry per distinct code-and-marker pairing: [[J01MA] with [3]]; a marker attributes the clause and belongs after it. The answer prose is left unchanged (issue #338).
WARN - ClassCodeFidelityCheck.reportMalformedParentheticals(416) |2026-09-07T00:26:13,603| Answer for patient=1 places citation marker(s) inside a parenthetical stating ATC class code(s), one entry per distinct code-and-marker pairing: [[J01MA02] with [2], [S01AE03] with [3]]; a marker attributes the clause and belongs after it. The answer prose is left unchanged (issue #338).
WARN - ClassCodeFidelityCheck.reportClassCodeDefects(279) |2026-09-07T00:26:13,606| Answer for patient=1 states ATC class code(s) [J01MA] that no cited record contains; cited record(s) [2] state [J01MA02, S01AE03, S02AA15, S03AA07]. The answer prose is left unchanged (issue #142).
WARN - ClassCodeFidelityCheck.reportClassCodeDefects(279) |2026-09-07T00:26:13,608| Answer for patient=1 states ATC class code(s) [J01CA] that no cited record contains; cited record(s) [3] state [J01MA, J01MA12]. The answer prose is left unchanged (issue #142).
WARN - LlmInferenceService.extractCitedReferences(638) |2026-09-07T00:26:13,612| LLM cited record [97] which does not exist in the provided records
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:13,612| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:13,614| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:13,616| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
[INFO] Tests run: 33, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.106 s -- in org.openmrs.module.chartsearchai.api.impl.ClassCodeFidelityTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.LlmInferenceServicePairChipExtentTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.001 s -- in org.openmrs.module.chartsearchai.api.impl.LlmInferenceServicePairChipExtentTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.ChartAnswerResponseFormatTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.001 s -- in org.openmrs.module.chartsearchai.api.impl.ChartAnswerResponseFormatTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.DefaultPatientAccessCheckTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0 s -- in org.openmrs.module.chartsearchai.api.impl.DefaultPatientAccessCheckTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.EndedOrderAnswerRuleTest
[WARNING] Tests run: 7, Failures: 0, Errors: 0, Skipped: 4, Time elapsed: 0.175 s -- in org.openmrs.module.chartsearchai.api.impl.EndedOrderAnswerRuleTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.CitationEvalTest
WARN - LlmInferenceService.extractCitedReferences(638) |2026-09-07T00:26:13,803| LLM cited record [99] which does not exist in the provided records
WARN - LlmAnswerExtractor.extractResponse(149) |2026-09-07T00:26:13,804| LLM response did not parse as JSON (possibly truncated), attempting regex extraction
WARN - LlmInferenceService.extractCitedReferences(638) |2026-09-07T00:26:13,805| LLM cited record [0] which does not exist in the provided records
WARN - LlmInferenceService.extractCitedReferences(638) |2026-09-07T00:26:13,805| LLM cited record [-1] which does not exist in the provided records
WARN - LlmInferenceService.extractCitedReferences(638) |2026-09-07T00:26:13,805| LLM cited record [999] which does not exist in the provided records
WARN - LlmInferenceService.extractCitedReferences(638) |2026-09-07T00:26:13,805| LLM cited record [11] which does not exist in the provided records
WARN - LlmInferenceService.extractCitedReferences(638) |2026-09-07T00:26:13,805| LLM cited record [15] which does not exist in the provided records
WARN - LlmInferenceService.extractCitedReferences(638) |2026-09-07T00:26:13,805| LLM cited record [20] which does not exist in the provided records
WARN - LlmInferenceService.extractCitedReferences(638) |2026-09-07T00:26:13,805| LLM cited record [12] which does not exist in the provided records
WARN - LlmAnswerExtractor.reportNonConformantCitations(352) |2026-09-07T00:26:13,806| The LLM's citations array did not honour the integer schema the request asked for: 2 index(es) recovered from a numeric string, 0 entry(ies) named no index and were dropped []
WARN - LlmAnswerExtractor.reportNonConformantCitations(352) |2026-09-07T00:26:13,806| The LLM's citations array did not honour the integer schema the request asked for: 2 index(es) recovered from a numeric string, 0 entry(ies) named no index and were dropped []
WARN - LlmAnswerExtractor.extractResponse(149) |2026-09-07T00:26:13,807| LLM response did not parse as JSON (possibly truncated), attempting regex extraction
WARN - LlmInferenceService.extractCitedReferences(638) |2026-09-07T00:26:13,811| LLM cited record [99] which does not exist in the provided records
WARN - LlmAnswerExtractor.extractResponse(149) |2026-09-07T00:26:13,812| LLM response did not parse as JSON (possibly truncated), attempting regex extraction
WARN - LlmInferenceService.extractCitedReferences(638) |2026-09-07T00:26:13,814| LLM cited record [0] which does not exist in the provided records
WARN - LlmInferenceService.extractCitedReferences(638) |2026-09-07T00:26:13,814| LLM cited record [-1] which does not exist in the provided records
WARN - LlmInferenceService.extractCitedReferences(638) |2026-09-07T00:26:13,815| LLM cited record [999] which does not exist in the provided records
WARN - LlmInferenceService.extractCitedReferences(638) |2026-09-07T00:26:13,815| LLM cited record [11] which does not exist in the provided records
WARN - LlmInferenceService.extractCitedReferences(638) |2026-09-07T00:26:13,815| LLM cited record [15] which does not exist in the provided records
WARN - LlmInferenceService.extractCitedReferences(638) |2026-09-07T00:26:13,815| LLM cited record [20] which does not exist in the provided records
WARN - LlmInferenceService.extractCitedReferences(638) |2026-09-07T00:26:13,816| LLM cited record [12] which does not exist in the provided records
WARN - LlmAnswerExtractor.reportNonConformantCitations(352) |2026-09-07T00:26:13,817| The LLM's citations array did not honour the integer schema the request asked for: 2 index(es) recovered from a numeric string, 0 entry(ies) named no index and were dropped []
WARN - LlmAnswerExtractor.reportNonConformantCitations(352) |2026-09-07T00:26:13,817| The LLM's citations array did not honour the integer schema the request asked for: 2 index(es) recovered from a numeric string, 0 entry(ies) named no index and were dropped []
WARN - LlmAnswerExtractor.extractResponse(149) |2026-09-07T00:26:13,819| LLM response did not parse as JSON (possibly truncated), attempting regex extraction
[INFO] Tests run: 29, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.022 s -- in org.openmrs.module.chartsearchai.api.impl.CitationEvalTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.StreamingContextPropagationTest
WARN - AuthorizationAdvice.warnMethodIsUnguarded(177) |2026-09-07T00:26:13,840| No @Authorized annotation applies to org.springframework.context.MessageSource.getMessage(), directly or through any method it overrides, so calls to it are not privilege checked
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.029 s -- in org.openmrs.module.chartsearchai.api.impl.StreamingContextPropagationTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.LlmInferenceServiceReferenceSliceTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 s -- in org.openmrs.module.chartsearchai.api.impl.LlmInferenceServiceReferenceSliceTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.LocalLlmEngineTest
[INFO] Tests run: 52, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.054 s -- in org.openmrs.module.chartsearchai.api.impl.LocalLlmEngineTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.LlmInferenceServiceAbstentionCitationTest
WARN - LlmInferenceService.maybeEmitPreliminaryReasoning(362) |2026-09-07T00:26:13,913| Preliminary reasoning skipped for patient [id=1]: A user context must first be passed to setUserContext()...use Context.openSession() (and closeSession() to prevent memory leaks!) before using the API
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in org.openmrs.module.chartsearchai.api.impl.LlmInferenceServiceAbstentionCitationTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.DrugOrderCurrencyMarkTest
WARN - AuthorizationAdvice.warnMethodIsUnguarded(177) |2026-09-07T00:26:13,946| No @Authorized annotation applies to org.openmrs.api.AdministrationService.addGlobalPropertyListener(), directly or through any method it overrides, so calls to it are not privilege checked
WARN - QueryStoreChartBuilder.readingOf(847) |2026-09-07T00:26:13,959| Could not decide whether 1 of this patient's order(s) are in force, so a chart record for any of them will not say either way; every other order is unaffected. The usual cause is an order whose stop date is after its auto-expire date, which core neither validates nor refuses to save. Orders: [9319cccc-0000-4000-8000-00000000319c]
WARN - QueryStoreChartBuilder.readingOf(847) |2026-09-07T00:26:13,992| Could not decide whether 1 of this patient's order(s) are in force, so a chart record for any of them will not say either way; every other order is unaffected. The usual cause is an order whose stop date is after its auto-expire date, which core neither validates nor refuses to save. Orders: [9319cccc-0000-4000-8000-00000000319c]
WARN - QueryStoreChartBuilder.readOrderCurrency(760) |2026-09-07T00:26:14,026| Could not read orders for patient [uuid=da7f524f-27ce-4bb2-86d6-6d1d05312bd5] — this chart's drug-order records will not say whether each prescription is still in force, so the answer may infer it from the record's dates. Chart assembly is otherwise unaffected.
java.lang.IllegalStateException: simulated OrderService failure
	at org.openmrs.module.chartsearchai.api.impl.DrugOrderCurrencyMarkTest$TestableBuilder.resolveAllOrders(DrugOrderCurrencyMarkTest.java:720) ~[test-classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.readOrderCurrency(QueryStoreChartBuilder.java:757) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.toSerializedRecords(QueryStoreChartBuilder.java:629) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.build(QueryStoreChartBuilder.java:225) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.DrugOrderCurrencyMarkTest.aFailedOrderReadIsReportedAtWarn(DrugOrderCurrencyMarkTest.java:294) ~[test-classes/:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:767) ~[junit-platform-commons-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$8(TestMethodTestDescriptor.java:217) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:213) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:138) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:156) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.executeWithoutCancellationToken(LauncherAdapter.java:60) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.execute(LauncherAdapter.java:52) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:203) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:168) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:136) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) [surefire-booter-3.5.5.jar:3.5.5]
INFO - QueryStoreChartBuilder.build(230) |2026-09-07T00:26:14,026| [timing] querystoreBuild patient=2 mode=fullChart hits=2 focusHits=0 rpcMs=0 serializeMs=0 totalMs=0 outcome=ok
WARN - QueryStoreChartBuilder.readingOf(847) |2026-09-07T00:26:14,060| Could not decide whether 1 of this patient's order(s) are in force, so a chart record for any of them will not say either way; every other order is unaffected. The usual cause is an order whose stop date is after its auto-expire date, which core neither validates nor refuses to save. Orders: [9319cccc-0000-4000-8000-00000000319c]
WARN - QueryStoreChartBuilder.readingOf(847) |2026-09-07T00:26:14,090| Could not decide whether 1 of this patient's order(s) are in force, so a chart record for any of them will not say either way; every other order is unaffected. The usual cause is an order whose stop date is after its auto-expire date, which core neither validates nor refuses to save. Orders: [9319cccc-0000-4000-8000-00000000319c]
WARN - QueryStoreChartBuilder.readingOf(847) |2026-09-07T00:26:14,121| Could not decide whether 1 of this patient's order(s) are in force, so a chart record for any of them will not say either way; every other order is unaffected. The usual cause is an order whose stop date is after its auto-expire date, which core neither validates nor refuses to save. Orders: [9319cccc-0000-4000-8000-00000000319c]
WARN - QueryStoreChartBuilder.readingOf(847) |2026-09-07T00:26:14,154| Could not decide whether 1 of this patient's order(s) are in force, so a chart record for any of them will not say either way; every other order is unaffected. The usual cause is an order whose stop date is after its auto-expire date, which core neither validates nor refuses to save. Orders: [9319cccc-0000-4000-8000-00000000319c]
WARN - QueryStoreChartBuilder.readingOf(847) |2026-09-07T00:26:14,226| Could not decide whether 1 of this patient's order(s) are in force, so a chart record for any of them will not say either way; every other order is unaffected. The usual cause is an order whose stop date is after its auto-expire date, which core neither validates nor refuses to save. Orders: [9319cccc-0000-4000-8000-00000000319c]
WARN - QueryStoreChartBuilder.readingOf(847) |2026-09-07T00:26:14,260| Could not decide whether 1 of this patient's order(s) are in force, so a chart record for any of them will not say either way; every other order is unaffected. The usual cause is an order whose stop date is after its auto-expire date, which core neither validates nor refuses to save. Orders: [9319cccc-0000-4000-8000-00000000319c]
WARN - QueryStoreChartBuilder.readOrderCurrency(760) |2026-09-07T00:26:14,327| Could not read orders for patient [uuid=da7f524f-27ce-4bb2-86d6-6d1d05312bd5] — this chart's drug-order records will not say whether each prescription is still in force, so the answer may infer it from the record's dates. Chart assembly is otherwise unaffected.
java.lang.IllegalStateException: simulated OrderService failure
	at org.openmrs.module.chartsearchai.api.impl.DrugOrderCurrencyMarkTest$TestableBuilder.resolveAllOrders(DrugOrderCurrencyMarkTest.java:720) ~[test-classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.readOrderCurrency(QueryStoreChartBuilder.java:757) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.toSerializedRecords(QueryStoreChartBuilder.java:629) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.build(QueryStoreChartBuilder.java:225) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.DrugOrderCurrencyMarkTest.neitherMarkIsRenderedWhenTheActiveOrderReadFails(DrugOrderCurrencyMarkTest.java:246) ~[test-classes/:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:767) ~[junit-platform-commons-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$8(TestMethodTestDescriptor.java:217) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:213) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:138) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:156) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.executeWithoutCancellationToken(LauncherAdapter.java:60) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.execute(LauncherAdapter.java:52) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:203) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:168) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:136) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) [surefire-booter-3.5.5.jar:3.5.5]
WARN - QueryStoreChartBuilder.readingOf(847) |2026-09-07T00:26:14,356| Could not decide whether 1 of this patient's order(s) are in force, so a chart record for any of them will not say either way; every other order is unaffected. The usual cause is an order whose stop date is after its auto-expire date, which core neither validates nor refuses to save. Orders: [9319cccc-0000-4000-8000-00000000319c]
WARN - QueryStoreChartBuilder.readingOf(847) |2026-09-07T00:26:14,411| Could not decide whether 1 of this patient's order(s) are in force, so a chart record for any of them will not say either way; every other order is unaffected. The usual cause is an order whose stop date is after its auto-expire date, which core neither validates nor refuses to save. Orders: [9319cccc-0000-4000-8000-00000000319c]
WARN - QueryStoreChartBuilder.readingOf(847) |2026-09-07T00:26:14,463| Could not decide whether 1 of this patient's order(s) are in force, so a chart record for any of them will not say either way; every other order is unaffected. The usual cause is an order whose stop date is after its auto-expire date, which core neither validates nor refuses to save. Orders: [9319cccc-0000-4000-8000-00000000319c]
[INFO] Tests run: 17, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.549 s -- in org.openmrs.module.chartsearchai.api.impl.DrugOrderCurrencyMarkTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.LlmProviderTest
WARN - LlmAnswerExtractor.readNonArrayCitations(332) |2026-09-07T00:26:14,473| The LLM's citations field was not the array the request's schema asked for: read as the one index its single value names: 8
WARN - LlmAnswerExtractor.readNonArrayCitations(332) |2026-09-07T00:26:14,473| The LLM's citations field was not the array the request's schema asked for: read as the one index its single value names: 0
WARN - LlmAnswerExtractor.readNonArrayCitations(332) |2026-09-07T00:26:14,473| The LLM's citations field was not the array the request's schema asked for: read as the one index its single value names: -1
WARN - LlmAnswerExtractor.reportNonConformantCitations(352) |2026-09-07T00:26:14,473| The LLM's citations array did not honour the integer schema the request asked for: 1 index(es) recovered from a numeric string, 0 entry(ies) named no index and were dropped []
WARN - LlmAnswerExtractor.readNonArrayCitations(332) |2026-09-07T00:26:14,473| The LLM's citations field was not the array the request's schema asked for: read as the one index its single value names: "8"
WARN - LlmAnswerExtractor.reportNonConformantCitations(352) |2026-09-07T00:26:14,474| The LLM's citations array did not honour the integer schema the request asked for: 1 index(es) recovered from a numeric string, 0 entry(ies) named no index and were dropped []
WARN - LlmAnswerExtractor.readNonArrayCitations(332) |2026-09-07T00:26:14,474| The LLM's citations field was not the array the request's schema asked for: read as the one index its single value names: "-1"
WARN - LlmAnswerExtractor.readNonArrayCitations(332) |2026-09-07T00:26:14,475| The LLM's citations field was not the array the request's schema asked for: read as the one index its single value names: 8
WARN - LlmAnswerExtractor.extractResponse(149) |2026-09-07T00:26:14,480| LLM response did not parse as JSON (possibly truncated), attempting regex extraction
WARN - LlmAnswerExtractor.readNonArrayCitations(327) |2026-09-07T00:26:14,490| The LLM's citations field was not the array the request's schema asked for, and names no index — it was dropped: {"index":9}
WARN - LlmAnswerExtractor.readNonArrayCitations(327) |2026-09-07T00:26:14,491| The LLM's citations field was not the array the request's schema asked for, and names no index — it was dropped: "eight"
WARN - LlmAnswerExtractor.readNonArrayCitations(327) |2026-09-07T00:26:14,492| The LLM's citations field was not the array the request's schema asked for, and names no index — it was dropped: 9.7
WARN - LlmAnswerExtractor.readNonArrayCitations(327) |2026-09-07T00:26:14,492| The LLM's citations field was not the array the request's schema asked for, and names no index — it was dropped: true
WARN - LlmAnswerExtractor.extractResponse(149) |2026-09-07T00:26:14,493| LLM response did not parse as JSON (possibly truncated), attempting regex extraction
WARN - LlmProvider.parseBatchVerdicts(879) |2026-09-07T00:26:14,493| Could not parse batch entailment verdicts; falling back to Tier-1 (Unrecognized token 'not': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')
 at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1, column: 4])
WARN - LlmAnswerExtractor.readNonArrayCitations(332) |2026-09-07T00:26:14,495| The LLM's citations field was not the array the request's schema asked for: read as the one index its single value names: 8
WARN - LlmAnswerExtractor.readNonArrayCitations(332) |2026-09-07T00:26:14,495| The LLM's citations field was not the array the request's schema asked for: read as the one index its single value names: "8"
WARN - LlmAnswerExtractor.reportNonConformantCitations(352) |2026-09-07T00:26:14,498| The LLM's citations array did not honour the integer schema the request asked for: 1 index(es) recovered from a numeric string, 0 entry(ies) named no index and were dropped []
WARN - LlmAnswerExtractor.reportNonConformantCitations(352) |2026-09-07T00:26:14,504| The LLM's citations array did not honour the integer schema the request asked for: 3 index(es) recovered from a numeric string, 0 entry(ies) named no index and were dropped []
WARN - LlmAnswerExtractor.reportNonConformantCitations(352) |2026-09-07T00:26:14,504| The LLM's citations array did not honour the integer schema the request asked for: 3 index(es) recovered from a numeric string, 0 entry(ies) named no index and were dropped []
WARN - LlmAnswerExtractor.reportNonConformantCitations(352) |2026-09-07T00:26:14,505| The LLM's citations array did not honour the integer schema the request asked for: 2 index(es) recovered from a numeric string, 0 entry(ies) named no index and were dropped []
WARN - LlmAnswerExtractor.extractResponse(149) |2026-09-07T00:26:14,505| LLM response did not parse as JSON (possibly truncated), attempting regex extraction
WARN - LlmAnswerExtractor.extractResponse(149) |2026-09-07T00:26:14,507| LLM response did not parse as JSON (possibly truncated), attempting regex extraction
WARN - LlmAnswerExtractor.extractResponse(183) |2026-09-07T00:26:14,507| Skipping unparseable citation number in truncated response: 22222222222…
WARN - LlmAnswerExtractor.reportNonConformantCitations(352) |2026-09-07T00:26:14,507| The LLM's citations array did not honour the integer schema the request asked for: 0 index(es) recovered from a numeric string, 6 entry(ies) named no index and were dropped ["eight", "9x", "", null, {"index":9}]
WARN - LlmAnswerExtractor.extractResponse(149) |2026-09-07T00:26:14,509| LLM response did not parse as JSON (possibly truncated), attempting regex extraction
[INFO] Tests run: 63, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.045 s -- in org.openmrs.module.chartsearchai.api.impl.LlmProviderTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilderTest
WARN - QueryStoreChartBuilder.buildFocused(507) |2026-09-07T00:26:14,512| QueryStore.searchByPatient failed for focused build [uuid=uuid-1] — returning empty focused chart
java.lang.RuntimeException: simulated similarity RPC failure
	at org.openmrs.module.chartsearchai.api.impl.CountingQueryStoreStub.searchByPatient(CountingQueryStoreStub.java:124) ~[test-classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.CountingQueryStoreStub.answer(CountingQueryStoreStub.java:104) ~[test-classes/:?]
	at jdk.proxy2/jdk.proxy2.$Proxy291.searchByPatient(Unknown Source) ~[?:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.buildFocused(QueryStoreChartBuilder.java:503) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilderTest.buildFocused_shouldReturnEmptyChart_whenSearchByPatientThrows(QueryStoreChartBuilderTest.java:633) ~[test-classes/:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:767) ~[junit-platform-commons-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$8(TestMethodTestDescriptor.java:217) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:213) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:138) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:156) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.executeWithoutCancellationToken(LauncherAdapter.java:60) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.execute(LauncherAdapter.java:52) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:203) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:168) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:136) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) [surefire-booter-3.5.5.jar:3.5.5]
WARN - QueryStoreChartBuilder.build(188) |2026-09-07T00:26:14,513| QueryStoreService is unavailable — querystore is a required module, so this indicates a querystore startup failure; check the querystore module. Returning empty chart.
WARN - QueryStoreChartBuilder.toSerializedRecords(633) |2026-09-07T00:26:14,519| Skipping null QueryDocument
WARN - QueryStoreChartBuilder.toSerializedRecords(637) |2026-09-07T00:26:14,519| Skipping malformed QueryDocument: type=null uuid=dropped-no-type
WARN - QueryStoreChartBuilder.toSerializedRecords(637) |2026-09-07T00:26:14,519| Skipping malformed QueryDocument: type=Condition uuid=null
WARN - QueryStoreChartBuilder.searchSimilarityUuids(578) |2026-09-07T00:26:14,521| QueryStore.searchByPatient failed for patient [uuid=uuid-1] — proceeding without focus hint
java.lang.RuntimeException: simulated similarity RPC failure
	at org.openmrs.module.chartsearchai.api.impl.CountingQueryStoreStub.searchByPatient(CountingQueryStoreStub.java:124) ~[test-classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.CountingQueryStoreStub.answer(CountingQueryStoreStub.java:104) ~[test-classes/:?]
	at jdk.proxy2/jdk.proxy2.$Proxy291.searchByPatient(Unknown Source) ~[?:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.searchSimilarityUuids(QueryStoreChartBuilder.java:573) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilder.build(QueryStoreChartBuilder.java:218) ~[classes/:?]
	at org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilderTest.build_shouldStillReturnFullChart_whenSearchByPatientThrows(QueryStoreChartBuilderTest.java:332) ~[test-classes/:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:767) ~[junit-platform-commons-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$8(TestMethodTestDescriptor.java:217) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:213) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:138) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:156) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.executeWithoutCancellationToken(LauncherAdapter.java:60) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.execute(LauncherAdapter.java:52) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:203) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:168) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:136) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) [surefire-booter-3.5.5.jar:3.5.5]
[INFO] Tests run: 29, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.011 s -- in org.openmrs.module.chartsearchai.api.impl.QueryStoreChartBuilderTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.EntailmentBatchResponseFormatTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0 s -- in org.openmrs.module.chartsearchai.api.impl.EntailmentBatchResponseFormatTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.WarmupExecutorTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.001 s -- in org.openmrs.module.chartsearchai.api.impl.WarmupExecutorTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.PrewarmBootstrapServiceTest
WARN - PrewarmBootstrapService.runSweep(209) |2026-09-07T00:26:14,530| Prewarm sweep COMPLETED: done=5 failed=0 cursor=5
WARN - PrewarmBootstrapService.runSweep(155) |2026-09-07T00:26:14,531| Prewarm sweep skipped: chartsearchai.chartMode=queryScoped — scoped answers use per-question slice prompts, so there is no full-chart prefix to pre-fill or pin.
WARN - PrewarmBootstrapService.runSweep(185) |2026-09-07T00:26:14,531| Prewarm reached maxPinnedEntries=2; stopping (no further pinning)
WARN - PrewarmBootstrapService.runSweep(209) |2026-09-07T00:26:14,532| Prewarm sweep STOPPED: done=0 failed=0 cursor=0
WARN - PrewarmBootstrapService.runSweep(209) |2026-09-07T00:26:14,533| Prewarm sweep COMPLETED: done=5 failed=0 cursor=5
WARN - PrewarmBootstrapService.runSweep(179) |2026-09-07T00:26:14,536| Prewarm sweep stopping: chartsearchai.chartMode flipped to queryScoped mid-sweep — remaining patients would silently not be pinned.
WARN - PrewarmBootstrapService.runSweep(209) |2026-09-07T00:26:14,536| Prewarm sweep STOPPED: done=2 failed=0 cursor=2
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.012 s -- in org.openmrs.module.chartsearchai.api.impl.PrewarmBootstrapServiceTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.LlmInferenceServiceProgressiveReasoningTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 s -- in org.openmrs.module.chartsearchai.api.impl.LlmInferenceServiceProgressiveReasoningTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.LlmInferenceServiceWarmupIntegrationTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 s -- in org.openmrs.module.chartsearchai.api.impl.LlmInferenceServiceWarmupIntegrationTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.RemoteLlmEngineTest
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.006 s -- in org.openmrs.module.chartsearchai.api.impl.RemoteLlmEngineTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.QueryScopeRouterTest
[INFO] Tests run: 20, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in org.openmrs.module.chartsearchai.api.impl.QueryScopeRouterTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.LlmInferenceServiceUnresolvedDrugClassTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.011 s -- in org.openmrs.module.chartsearchai.api.impl.LlmInferenceServiceUnresolvedDrugClassTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.LlmAnswerQualityTest
[WARNING] Tests run: 2, Failures: 0, Errors: 0, Skipped: 1, Time elapsed: 0.003 s -- in org.openmrs.module.chartsearchai.api.impl.LlmAnswerQualityTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.ChartSearchServiceRouterTest
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.006 s -- in org.openmrs.module.chartsearchai.api.impl.ChartSearchServiceRouterTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.LlmInferenceServiceWarmupTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 s -- in org.openmrs.module.chartsearchai.api.impl.LlmInferenceServiceWarmupTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.ActiveOrderCitationFidelityTest
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:14,579| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:14,582| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:14,585| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
WARN - ActiveOrderCitationFidelityCheck.reportMisattributedOrderCitations(240) |2026-09-07T00:26:14,589| Answer for patient=1 cites [[3] condition record] as evidence of an active drug order it cannot be. The answer prose is left unchanged (issue #377).
WARN - LlmInferenceService.maybeEmitPreliminaryReasoning(362) |2026-09-07T00:26:14,592| Preliminary reasoning skipped for patient [id=1]: A user context must first be passed to setUserContext()...use Context.openSession() (and closeSession() to prevent memory leaks!) before using the API
WARN - ActiveOrderCitationFidelityCheck.reportMisattributedOrderCitations(240) |2026-09-07T00:26:14,592| Answer for patient=1 cites [[3] condition record] as evidence of an active drug order it cannot be. The answer prose is left unchanged (issue #377).
WARN - ActiveOrderCitationFidelityCheck.reportMisattributedOrderCitations(240) |2026-09-07T00:26:14,597| Answer for patient=1 cites [[1] an order the chart marks as no longer in force] as evidence of an active drug order it cannot be. The answer prose is left unchanged (issue #377).
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:14,600| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
WARN - ActiveOrderCitationFidelityCheck.reportMisattributedOrderCitations(240) |2026-09-07T00:26:14,603| Answer for patient=1 cites [[3] condition record, [4] visit record, [5] encounter record] as evidence of an active drug order it cannot be. The answer prose is left unchanged (issue #377).
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:14,606| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
WARN - LlmInferenceService.maybeEmitPreliminaryReasoning(362) |2026-09-07T00:26:14,609| Preliminary reasoning skipped for patient [id=1]: A user context must first be passed to setUserContext()...use Context.openSession() (and closeSession() to prevent memory leaks!) before using the API
WARN - ActiveOrderCitationFidelityCheck.reportMisattributedOrderCitations(240) |2026-09-07T00:26:14,609| Answer for patient=1 cites [[3] condition record] as evidence of an active drug order it cannot be. The answer prose is left unchanged (issue #377).
WARN - ActiveOrderCitationFidelityCheck.reportMisattributedOrderCitations(240) |2026-09-07T00:26:14,611| Answer for patient=1 cites [[3] condition record] as evidence of an active drug order it cannot be. The answer prose is left unchanged (issue #377).
WARN - ActiveOrderCitationFidelityCheck.reportMisattributedOrderCitations(251) |2026-09-07T00:26:14,614| Active-order citation check failed for patient=1; the answer is unaffected: java.lang.IllegalStateException: order currency unavailable
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:14,622| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Simvastatin Co 20mg [order-uuid-unsubstantiated]]
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:14,623| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
WARN - ActiveOrderCitationFidelityCheck.reportMisattributedOrderCitations(240) |2026-09-07T00:26:14,625| Answer for patient=1 cites [[3] condition record] as evidence of an active drug order it cannot be. The answer prose is left unchanged (issue #377).
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:14,627| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.053 s -- in org.openmrs.module.chartsearchai.api.impl.ActiveOrderCitationFidelityTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.LlmInferenceServiceCitationWiringTest
WARN - LlmInferenceService.maybeEmitPreliminaryReasoning(362) |2026-09-07T00:26:14,629| Preliminary reasoning skipped for patient [id=1]: A user context must first be passed to setUserContext()...use Context.openSession() (and closeSession() to prevent memory leaks!) before using the API
WARN - LlmInferenceService.maybeEmitPreliminaryReasoning(362) |2026-09-07T00:26:14,629| Preliminary reasoning skipped for patient [id=1]: A user context must first be passed to setUserContext()...use Context.openSession() (and closeSession() to prevent memory leaks!) before using the API
WARN - LlmInferenceService.maybeEmitPreliminaryReasoning(362) |2026-09-07T00:26:14,630| Preliminary reasoning skipped for patient [id=1]: A user context must first be passed to setUserContext()...use Context.openSession() (and closeSession() to prevent memory leaks!) before using the API
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in org.openmrs.module.chartsearchai.api.impl.LlmInferenceServiceCitationWiringTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.LlmInferenceServiceQueryScopedTest
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in org.openmrs.module.chartsearchai.api.impl.LlmInferenceServiceQueryScopedTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.ReferenceRecordPromptLeadTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.001 s -- in org.openmrs.module.chartsearchai.api.impl.ReferenceRecordPromptLeadTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.PipelineSettingsDefaultTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.001 s -- in org.openmrs.module.chartsearchai.api.impl.PipelineSettingsDefaultTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.ReferenceProseFidelityTest
DEBUG - ReferenceProseFidelityCheck.reportUnfaithfulReferenceProse(284) |2026-09-07T00:26:14,641| Reference-prose check found no divergence for patient=1: every reproduction of a cited reference record is faithful
WARN - ReferenceProseFidelityCheck.reportUnfaithfulReferenceProse(298) |2026-09-07T00:26:14,644| Answer for patient=1 reproduces 25 words of cited record [3] and then states different words inside the sentence it was copying (the record's own text continues at its word 26, counting from one). The answer prose is left unchanged (issue #337).
WARN - ReferenceProseFidelityCheck.reportUnfaithfulReferenceProse(298) |2026-09-07T00:26:14,649| Answer for patient=1 reproduces 15 words of cited record [3] and then states different words inside the sentence it was copying (the record's own text continues at its word 26, counting from one). The answer prose is left unchanged (issue #337).
WARN - ReferenceProseFidelityCheck.reportUnfaithfulReferenceProse(298) |2026-09-07T00:26:14,654| Answer for patient=1 reproduces 12 words of cited record [3] and then states different words inside the sentence it was copying (the record's own text continues at its word 28, counting from one). The answer prose is left unchanged (issue #337).
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:14,656| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
WARN - LlmInferenceService.maybeEmitPreliminaryReasoning(362) |2026-09-07T00:26:14,659| Preliminary reasoning skipped for patient [id=1]: A user context must first be passed to setUserContext()...use Context.openSession() (and closeSession() to prevent memory leaks!) before using the API
WARN - ReferenceProseFidelityCheck.reportUnfaithfulReferenceProse(298) |2026-09-07T00:26:14,659| Answer for patient=1 reproduces 25 words of cited record [3] and then states different words inside the sentence it was copying (the record's own text continues at its word 26, counting from one). The answer prose is left unchanged (issue #337).
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:14,663| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
WARN - ReferenceProseFidelityCheck.reportUnfaithfulReferenceProse(298) |2026-09-07T00:26:14,664| Answer for patient=1 reproduces 14 words of cited record [3] and then states different words inside the sentence it was copying (the record's own text continues at its word 18, counting from one). The answer prose is left unchanged (issue #337).
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:14,669| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:14,671| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
WARN - ReferenceProseFidelityCheck.reportUnfaithfulReferenceProse(298) |2026-09-07T00:26:14,673| Answer for patient=1 reproduces 14 words of cited record [2] and then states different words inside the sentence it was copying (the record's own text continues at its word 15, counting from one). The answer prose is left unchanged (issue #337).
WARN - ReferenceProseFidelityCheck.reportUnfaithfulReferenceProse(298) |2026-09-07T00:26:14,673| Answer for patient=1 reproduces 12 words of cited record [3] and then states different words inside the sentence it was copying (the record's own text continues at its word 13, counting from one). The answer prose is left unchanged (issue #337).
WARN - ReferenceProseFidelityCheck.reportUnfaithfulReferenceProse(298) |2026-09-07T00:26:14,673| Answer for patient=1 reproduces 14 words of cited record [1] and then states different words inside the sentence it was copying (the record's own text continues at its word 15, counting from one). The answer prose is left unchanged (issue #337).
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:14,676| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:14,679| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
WARN - ReferenceProseFidelityCheck.reportUnfaithfulReferenceProse(298) |2026-09-07T00:26:14,686| Answer for patient=1 reproduces 25 words of cited record [3] and then states different words inside the sentence it was copying (the record's own text continues at its word 26, counting from one). The answer prose is left unchanged (issue #337).
WARN - ReferenceProseFidelityCheck.reportUnfaithfulReferenceProse(298) |2026-09-07T00:26:14,689| Answer for patient=1 reproduces 12 words of cited record [3] and then states different words inside the sentence it was copying (the record's own text continues at its word 66, counting from one). The answer prose is left unchanged (issue #337).
WARN - ReferenceProseFidelityCheck.reportUnfaithfulReferenceProse(298) |2026-09-07T00:26:14,689| Answer for patient=1 reproduces 25 words of cited record [3] and then states different words inside the sentence it was copying (the record's own text continues at its word 26, counting from one). The answer prose is left unchanged (issue #337).
WARN - LlmInferenceService.maybeEmitPreliminaryReasoning(362) |2026-09-07T00:26:14,692| Preliminary reasoning skipped for patient [id=1]: A user context must first be passed to setUserContext()...use Context.openSession() (and closeSession() to prevent memory leaks!) before using the API
WARN - ReferenceProseFidelityCheck.reportUnfaithfulReferenceProse(298) |2026-09-07T00:26:14,693| Answer for patient=1 reproduces 25 words of cited record [3] and then states different words inside the sentence it was copying (the record's own text continues at its word 26, counting from one). The answer prose is left unchanged (issue #337).
DEBUG - ReferenceProseFidelityCheck.reportUnfaithfulReferenceProse(275) |2026-09-07T00:26:14,695| Reference-prose check skipped for patient=1: the answer reproduces no cited reference record
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:14,697| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:14,699| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:14,701| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
WARN - ClassCodeFidelityCheck.reportMalformedParentheticals(411) |2026-09-07T00:26:14,704| Answer for patient=1 states ATC class code(s) [H02AB] more than once inside one parenthetical; nothing this module renders states one code twice inside one. The answer prose is left unchanged (issue #338).
DEBUG - ReferenceProseFidelityCheck.reportUnfaithfulReferenceProse(275) |2026-09-07T00:26:14,704| Reference-prose check skipped for patient=1: the answer reproduces no cited reference record
WARN - ReferenceProseFidelityCheck.reportUnfaithfulReferenceProse(298) |2026-09-07T00:26:14,705| Answer for patient=1 reproduces 14 words of cited record [3] and then states different words inside the sentence it was copying (the record's own text continues at its word 18, counting from one). The answer prose is left unchanged (issue #337).
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:14,708| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:14,710| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
WARN - ReferenceProseFidelityCheck.reportUnfaithfulReferenceProse(298) |2026-09-07T00:26:14,712| Answer for patient=1 reproduces 12 words of cited record [3] and then states different words inside the sentence it was copying (the record's own text continues at its word 23, counting from one). The answer prose is left unchanged (issue #337).
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:14,715| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:14,716| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:14,717| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:14,717| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
DEBUG - ReferenceProseFidelityCheck.reportUnfaithfulReferenceProse(258) |2026-09-07T00:26:14,719| Reference-prose check skipped for patient=1: the answer cites no readable reference record
INFO - LlmInferenceService.search(190) |2026-09-07T00:26:14,721| [timing] search patient=1 chartBuildMs=0 llmMs=0 totalMs=0 inputTokens=0 cachedTokens=0 outcome=ok
[INFO] Tests run: 26, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.084 s -- in org.openmrs.module.chartsearchai.api.impl.ReferenceProseFidelityTest
[INFO] Running org.openmrs.module.chartsearchai.api.impl.LlmProviderUserMessageTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 s -- in org.openmrs.module.chartsearchai.api.impl.LlmProviderUserMessageTest
[INFO] Running org.openmrs.module.chartsearchai.api.AuditLogPurgeTaskTest
WARN - AuditLogPurgeTask.parseRetentionDays(72) |2026-09-07T00:26:14,726| Invalid audit log retention value 'not-a-number', using default
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.001 s -- in org.openmrs.module.chartsearchai.api.AuditLogPurgeTaskTest
[INFO] Running org.openmrs.module.chartsearchai.api.ChartSearchEventListenerTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 s -- in org.openmrs.module.chartsearchai.api.ChartSearchEventListenerTest
[INFO] Running org.openmrs.module.chartsearchai.api.db.hibernate.HibernateChartSearchAiDAOTest
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.371 s -- in org.openmrs.module.chartsearchai.api.db.hibernate.HibernateChartSearchAiDAOTest
[INFO] Running org.openmrs.module.chartsearchai.ChartSearchAiModuleActivatorTest
WARN - HibernateSchedulerDAO.getTaskByName(105) |2026-09-07T00:26:15,116| Task 'Chart Search AI - Embedding Backfill' not found
WARN - JobRunrSchedulerService.getTaskByName(245) |2026-09-07T00:26:15,116| getTaskByName(Chart Search AI - Embedding Backfill) failed, because: org.springframework.orm.ObjectRetrievalFailureException: Object of class [org.openmrs.scheduler.TaskDefinition] with identifier [Chart Search AI - Embedding Backfill]: not found
WARN - HibernateSchedulerDAO.getTaskByName(105) |2026-09-07T00:26:15,117| Task 'Chart Search AI - Embedding Backfill' not found
WARN - JobRunrSchedulerService.getTaskByName(245) |2026-09-07T00:26:15,117| getTaskByName(Chart Search AI - Embedding Backfill) failed, because: org.springframework.orm.ObjectRetrievalFailureException: Object of class [org.openmrs.scheduler.TaskDefinition] with identifier [Chart Search AI - Embedding Backfill]: not found
WARN - HibernateSchedulerDAO.getTaskByName(105) |2026-09-07T00:26:15,117| Task 'Chart Search AI - Embedding Backfill' not found
WARN - JobRunrSchedulerService.getTaskByName(245) |2026-09-07T00:26:15,117| getTaskByName(Chart Search AI - Embedding Backfill) failed, because: org.springframework.orm.ObjectRetrievalFailureException: Object of class [org.openmrs.scheduler.TaskDefinition] with identifier [Chart Search AI - Embedding Backfill]: not found
INFO - ChartSearchAiModuleActivator.started(95) |2026-09-07T00:26:15,124| Chart Search AI Module started
WARN - ChartSearchAiModuleActivator.validateModelFile(184) |2026-09-07T00:26:15,124| Chart Search AI: LLM model path not configured. Set 'chartsearchai.llm.modelFilePath' before using the module.
WARN - HibernateSchedulerDAO.getTaskByName(105) |2026-09-07T00:26:15,125| Task 'Chart Search AI - Embedding Backfill' not found
WARN - JobRunrSchedulerService.getTaskByName(245) |2026-09-07T00:26:15,125| getTaskByName(Chart Search AI - Embedding Backfill) failed, because: org.springframework.orm.ObjectRetrievalFailureException: Object of class [org.openmrs.scheduler.TaskDefinition] with identifier [Chart Search AI - Embedding Backfill]: not found
WARN - HibernateSchedulerDAO.getTaskByName(105) |2026-09-07T00:26:15,125| Task 'Chart Search AI - Audit Log Purge' not found
WARN - JobRunrSchedulerService.getTaskByName(245) |2026-09-07T00:26:15,125| getTaskByName(Chart Search AI - Audit Log Purge) failed, because: org.springframework.orm.ObjectRetrievalFailureException: Object of class [org.openmrs.scheduler.TaskDefinition] with identifier [Chart Search AI - Audit Log Purge]: not found
WARN - AuthorizationAdvice.warnMethodIsUnguarded(177) |2026-09-07T00:26:15,131| No @Authorized annotation applies to org.openmrs.api.AdministrationService.getMaximumPropertyLength(), directly or through any method it overrides, so calls to it are not privilege checked
INFO - ChartSearchAiModuleActivator.registerAuditLogPurgeTask(262) |2026-09-07T00:26:15,133| Registered audit log purge task
INFO - ChartSearchAiModuleActivator.started(95) |2026-09-07T00:26:15,140| Chart Search AI Module started
WARN - ChartSearchAiModuleActivator.validateModelFile(184) |2026-09-07T00:26:15,141| Chart Search AI: LLM model path not configured. Set 'chartsearchai.llm.modelFilePath' before using the module.
WARN - ChartSearchAiModuleActivator.warnIfCustomSystemPrompt(166) |2026-09-07T00:26:15,141| Chart Search AI: 'chartsearchai.llm.systemPrompt' is set, so this deployment uses a custom system prompt and NOT the module's built-in one. A custom prompt replaces the built-in prompt wholesale rather than adding to it, so it does not carry rules added since it was written — including the rule that an answer naming a drug from an ended drug order must say the order is no longer in force (issue #315). Re-derive the custom prompt from the built-in one, or clear the property to use the built-in prompt.
WARN - HibernateSchedulerDAO.getTaskByName(105) |2026-09-07T00:26:15,142| Task 'Chart Search AI - Embedding Backfill' not found
WARN - JobRunrSchedulerService.getTaskByName(245) |2026-09-07T00:26:15,142| getTaskByName(Chart Search AI - Embedding Backfill) failed, because: org.springframework.orm.ObjectRetrievalFailureException: Object of class [org.openmrs.scheduler.TaskDefinition] with identifier [Chart Search AI - Embedding Backfill]: not found
WARN - HibernateSchedulerDAO.getTaskByName(105) |2026-09-07T00:26:15,142| Task 'Chart Search AI - Audit Log Purge' not found
WARN - JobRunrSchedulerService.getTaskByName(245) |2026-09-07T00:26:15,142| getTaskByName(Chart Search AI - Audit Log Purge) failed, because: org.springframework.orm.ObjectRetrievalFailureException: Object of class [org.openmrs.scheduler.TaskDefinition] with identifier [Chart Search AI - Audit Log Purge]: not found
INFO - ChartSearchAiModuleActivator.registerAuditLogPurgeTask(262) |2026-09-07T00:26:15,144| Registered audit log purge task
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.044 s -- in org.openmrs.module.chartsearchai.ChartSearchAiModuleActivatorTest
[INFO] Running org.openmrs.module.chartsearchai.reference.ActiveOrderContraindicationTest
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.020 s -- in org.openmrs.module.chartsearchai.reference.ActiveOrderContraindicationTest
[INFO] Running org.openmrs.module.chartsearchai.reference.FoldedFindingStrengthTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in org.openmrs.module.chartsearchai.reference.FoldedFindingStrengthTest
[INFO] Running org.openmrs.module.chartsearchai.reference.BridgedConceptLegBoundsTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 6.863 s -- in org.openmrs.module.chartsearchai.reference.BridgedConceptLegBoundsTest
[INFO] Running org.openmrs.module.chartsearchai.reference.InjectedInteractionRelevanceOrderTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.670 s -- in org.openmrs.module.chartsearchai.reference.InjectedInteractionRelevanceOrderTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DrugSafetyValidatorEchoScopingTest
WARN - DrugSafetyValidator.addActiveOrderPairInteractions(6366) |2026-09-07T00:26:23,068| Interaction screening across 6 active-order reference entries found 15 pair(s) above the severity floor; reporting the 10 most severe and WITHHOLDING 5: Simvastatin x Ciprofloxacin (Moderate); Ciprofloxacin x Clarithromycin (Moderate); Ciprofloxacin x Fluconazole (Moderate); Simvastatin x Warfarin (Minor); Clarithromycin x Fluconazole (Minor)
WARN - DrugSafetyValidator.addActiveOrderPairInteractions(6366) |2026-09-07T00:26:23,080| Interaction screening across 6 active-order reference entries found 15 pair(s) above the severity floor; reporting the 10 most severe and WITHHOLDING 5: Simvastatin x Ciprofloxacin (Moderate); Ciprofloxacin x Clarithromycin (Moderate); Ciprofloxacin x Fluconazole (Moderate); Simvastatin x Warfarin (Minor); Clarithromycin x Fluconazole (Minor)
ERROR - ServerZooKeeper.signalBackgroundJobServerAliveAndDoZooKeeping(88) |2026-09-07T00:26:23,258| SEVERE ERROR - Server timed out while it's still alive. Are all servers using NTP and in the same timezone? Are you having long GC cycles? Restart attempt 1 out of 3
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.685 s -- in org.openmrs.module.chartsearchai.reference.DrugSafetyValidatorEchoScopingTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DrugSafetyInteractionSeverityFloorTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 s -- in org.openmrs.module.chartsearchai.reference.DrugSafetyInteractionSeverityFloorTest
[INFO] Running org.openmrs.module.chartsearchai.reference.InjectedReferenceSliceTest
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:23,398| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Simvastatin Co 20mg [11111111-2222-3333-4444-555555555555]]
INFO - DdiDrugReferenceSource.parse(318) |2026-09-07T00:26:23,399| Parsed 16 DDInter drug-reference entries
INFO - ReferenceDataFiles.loadWithClasspathFallback(228) |2026-09-07T00:26:23,400| Loaded 1 cross-reactivity groups from bundled default /chartsearchai/cross-reactivity-groups.json
INFO - DrugSafetyValidator.validate(778) |2026-09-07T00:26:23,400| Drug-safety validator raised 1 warning(s)
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:23,400| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Simvastatin Co 20mg [11111111-2222-3333-4444-555555555555]]
DEBUG - DrugReferenceInjector.injectRecords(631) |2026-09-07T00:26:23,400| Injected 1 active-order, 1 drug-reference (333 chars), 1 safety-finding and 0 drug-class-note record(s) — reference slice 2 record(s), 699 chars — into chart for question 'is it safe to give clarithromycin?'
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:23,402| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Simvastatin Co 20mg [11111111-2222-3333-4444-555555555555]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:23,403| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Simvastatin Co 20mg [11111111-2222-3333-4444-555555555555]]
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.007 s -- in org.openmrs.module.chartsearchai.reference.InjectedReferenceSliceTest
[INFO] Running org.openmrs.module.chartsearchai.reference.SubstanceCandidateSetTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.011 s -- in org.openmrs.module.chartsearchai.reference.SubstanceCandidateSetTest
[INFO] Running org.openmrs.module.chartsearchai.reference.ActiveOrderAdministrationTermsTest
WARN - AuthorizationAdvice.warnMethodIsUnguarded(177) |2026-09-07T00:26:23,435| No @Authorized annotation applies to org.openmrs.api.AdministrationService.isDatabaseStringComparisonCaseSensitive(), directly or through any method it overrides, so calls to it are not privilege checked
WARN - PatientClinicalContextBuilder.build(174) |2026-09-07T00:26:23,581| Active drug order e1f95924-697a-11e3-bd76-0800271c1b75 has no readable name; it will be identified by its ATC codes as [ATC N02BA01]. A safety chip for it is labelled that way unless the reference data can name one of those codes, and the order cannot be matched against chart text at all.
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.169 s -- in org.openmrs.module.chartsearchai.reference.ActiveOrderAdministrationTermsTest
[INFO] Running org.openmrs.module.chartsearchai.reference.ReferenceRecordSubstanceCeilingsTest
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:23,586| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Amoxicillin (suspension) [order-Amoxicillin (suspension)]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:23,592| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Amoxicillin (suspension) [order-Amoxicillin (suspension)]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:23,594| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Ibuprofen [order-Ibuprofen]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:23,596| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [tmp-paediatric [order-tmp-paediatric]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:23,597| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Amoxicillin (suspension) [order-Amoxicillin (suspension)]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:23,598| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Amoxicillin (suspension) [order-Amoxicillin (suspension)]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:23,599| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Mupirocin (nasal) [order-Mupirocin (nasal)]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:23,600| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [zantac [order-zantac]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:23,601| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Cefadroxil [order-Cefadroxil]]
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.017 s -- in org.openmrs.module.chartsearchai.reference.ReferenceRecordSubstanceCeilingsTest
[INFO] Running org.openmrs.module.chartsearchai.reference.InjectedContraindicationCorroborationTest
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.009 s -- in org.openmrs.module.chartsearchai.reference.InjectedContraindicationCorroborationTest
[INFO] Running org.openmrs.module.chartsearchai.reference.InjectedInteractionNoteCollapseTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.007 s -- in org.openmrs.module.chartsearchai.reference.InjectedInteractionNoteCollapseTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DrugClassQuestionNoteTest
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:24,702| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Warfarin 5mg [order-warfarin]]
[INFO] Tests run: 20, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.083 s -- in org.openmrs.module.chartsearchai.reference.DrugClassQuestionNoteTest
[INFO] Running org.openmrs.module.chartsearchai.reference.ContraindicationRouteVariantTest
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.012 s -- in org.openmrs.module.chartsearchai.reference.ContraindicationRouteVariantTest
[INFO] Running org.openmrs.module.chartsearchai.reference.ReferenceRecordSubstanceCollapseTest
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:24,719| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:24,723| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:24,728| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
INFO - DrugSafetyValidator.validate(778) |2026-09-07T00:26:24,729| Drug-safety validator raised 1 warning(s)
DEBUG - DrugReferenceInjector.injectRecords(631) |2026-09-07T00:26:24,734| Injected 0 active-order, 1 drug-reference (398 chars), 1 safety-finding and 0 drug-class-note record(s) — reference slice 2 record(s), 757 chars — into chart for question 'Is it safe to give hydrocortisone?'
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.019 s -- in org.openmrs.module.chartsearchai.reference.ReferenceRecordSubstanceCollapseTest
[INFO] Running org.openmrs.module.chartsearchai.reference.BridgedOrderSelfWitnessContextTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.017 s -- in org.openmrs.module.chartsearchai.reference.BridgedOrderSelfWitnessContextTest
[INFO] Running org.openmrs.module.chartsearchai.reference.UnmappedOrderAdministrationSiteTest
[INFO] Tests run: 23, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.995 s -- in org.openmrs.module.chartsearchai.reference.UnmappedOrderAdministrationSiteTest
[INFO] Running org.openmrs.module.chartsearchai.reference.CrossReactivityGroupsTest
WARN - CrossReactivityGroupsLoader.parse(174) |2026-09-07T00:26:27,756| Dropped 3 unusable cross-reactivity groups (blank name or no usable atcPrefixes)
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:27,757| Drug-reference data validity — dataset-missing-a-required-table (reported, 1): a 'cross-reactivity groups' document must declare [groups]; this one does not, so it parsed to no groups at all. The data is left as loaded — the fix is in the file: either it is not a document of the format its reader expects, or, where a document carrying none of them is intended, the missing table has to be declared empty. Reading it as empty here would load an export truncated before it wrote that table as a complete one.
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.010 s -- in org.openmrs.module.chartsearchai.reference.CrossReactivityGroupsTest
[INFO] Running org.openmrs.module.chartsearchai.reference.PartialOrderCoveragePartnerTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.601 s -- in org.openmrs.module.chartsearchai.reference.PartialOrderCoveragePartnerTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DrugInPlayFindingStrengthKeyOrderContextTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.016 s -- in org.openmrs.module.chartsearchai.reference.DrugInPlayFindingStrengthKeyOrderContextTest
[INFO] Running org.openmrs.module.chartsearchai.reference.RecordedOrderNameBeyondItsDisplayTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.022 s -- in org.openmrs.module.chartsearchai.reference.RecordedOrderNameBeyondItsDisplayTest
[INFO] Running org.openmrs.module.chartsearchai.reference.InjectedContraindicationPatientReadingTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.008 s -- in org.openmrs.module.chartsearchai.reference.InjectedContraindicationPatientReadingTest
[INFO] Running org.openmrs.module.chartsearchai.reference.RecordedAllergenMemoScopeTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in org.openmrs.module.chartsearchai.reference.RecordedAllergenMemoScopeTest
[INFO] Running org.openmrs.module.chartsearchai.reference.PairChipCapContextTest
WARN - DrugSafetyValidator.addActiveOrderPairInteractions(6366) |2026-09-07T00:26:28,428| Interaction screening across 6 active-order reference entries found 15 pair(s) above the severity floor; reporting the 3 most severe and WITHHOLDING 12: Warfarin x Ciprofloxacin (Major); Warfarin x Clarithromycin (Major); Warfarin x Fluconazole (Major); Warfarin x Amiodarone (Major); Ciprofloxacin x Amiodarone (Major); Clarithromycin x Amiodarone (Major); Fluconazole x Amiodarone (Major); Simvastatin x Ciprofloxacin (Moderate); Ciprofloxacin x Clarithromycin (Moderate); Ciprofloxacin x Fluconazole (Moderate); Simvastatin x Warfarin (Minor); Clarithromycin x Fluconazole (Minor)
WARN - DrugSafetyValidator.addQuestionPairInteractions(5014) |2026-09-07T00:26:28,436| Question-pair safety: 10 of 72 question-named drug pairs shown (cap 10); the question resolved 16 reference drugs, and pairs grow as N^2/2. Withheld, least severe last: [Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor]
WARN - DrugSafetyValidator.addQuestionPairInteractions(5014) |2026-09-07T00:26:28,441| Question-pair safety: 25 of 72 question-named drug pairs shown (cap 25); the question resolved 16 reference drugs, and pairs grow as N^2/2. Withheld, least severe last: [Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor]
WARN - DrugSafetyValidator.addQuestionPairInteractions(5014) |2026-09-07T00:26:28,450| Question-pair safety: 10 of 72 question-named drug pairs shown (cap 10); the question resolved 16 reference drugs, and pairs grow as N^2/2. Withheld, least severe last: [Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor]
WARN - DrugSafetyValidator.addQuestionPairInteractions(5014) |2026-09-07T00:26:28,461| Question-pair safety: 10 of 72 question-named drug pairs shown (cap 10); the question resolved 16 reference drugs, and pairs grow as N^2/2. Withheld, least severe last: [Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor]
WARN - DrugSafetyValidator.addQuestionPairInteractions(5014) |2026-09-07T00:26:28,470| Question-pair safety: 10 of 72 question-named drug pairs shown (cap 10); the question resolved 16 reference drugs, and pairs grow as N^2/2. Withheld, least severe last: [Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor]
WARN - DrugSafetyValidator.addQuestionPairInteractions(5014) |2026-09-07T00:26:28,474| Question-pair safety: 10 of 72 question-named drug pairs shown (cap 10); the question resolved 16 reference drugs, and pairs grow as N^2/2. Withheld, least severe last: [Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor]
WARN - DrugSafetyValidator.addQuestionPairInteractions(5014) |2026-09-07T00:26:28,485| Question-pair safety: 3 of 72 question-named drug pairs shown (cap 3); the question resolved 16 reference drugs, and pairs grow as N^2/2. Withheld, least severe last: [Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor]
WARN - DrugSafetyValidator.addQuestionPairInteractions(5014) |2026-09-07T00:26:28,493| Question-pair safety: 3 of 72 question-named drug pairs shown (cap 3); the question resolved 16 reference drugs, and pairs grow as N^2/2. Withheld, least severe last: [Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor]
WARN - DrugSafetyValidator.addQuestionPairInteractions(5014) |2026-09-07T00:26:28,505| Question-pair safety: 1 of 72 question-named drug pairs shown (cap 1); the question resolved 16 reference drugs, and pairs grow as N^2/2. Withheld, least severe last: [Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor]
WARN - DrugSafetyValidator.addQuestionPairInteractions(5014) |2026-09-07T00:26:28,508| Question-pair safety: 3 of 72 question-named drug pairs shown (cap 3); the question resolved 16 reference drugs, and pairs grow as N^2/2. Withheld, least severe last: [Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor]
WARN - DrugSafetyValidator.addQuestionPairInteractions(5014) |2026-09-07T00:26:28,511| Question-pair safety: 10 of 72 question-named drug pairs shown (cap 10); the question resolved 16 reference drugs, and pairs grow as N^2/2. Withheld, least severe last: [Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor]
WARN - DrugSafetyValidator.addQuestionPairInteractions(5014) |2026-09-07T00:26:28,514| Question-pair safety: 25 of 72 question-named drug pairs shown (cap 25); the question resolved 16 reference drugs, and pairs grow as N^2/2. Withheld, least severe last: [Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor]
WARN - DrugSafetyValidator.addQuestionPairInteractions(5014) |2026-09-07T00:26:28,517| Question-pair safety: 71 of 72 question-named drug pairs shown (cap 71); the question resolved 16 reference drugs, and pairs grow as N^2/2. Withheld, least severe last: [Minor]
WARN - DrugSafetyValidator.addActiveOrderPairInteractions(6366) |2026-09-07T00:26:28,524| Interaction screening across 6 active-order reference entries found 15 pair(s) above the severity floor; reporting the 3 most severe and WITHHOLDING 12: Warfarin x Ciprofloxacin (Major); Warfarin x Clarithromycin (Major); Warfarin x Fluconazole (Major); Warfarin x Amiodarone (Major); Ciprofloxacin x Amiodarone (Major); Clarithromycin x Amiodarone (Major); Fluconazole x Amiodarone (Major); Simvastatin x Ciprofloxacin (Moderate); Ciprofloxacin x Clarithromycin (Moderate); Ciprofloxacin x Fluconazole (Moderate); Simvastatin x Warfarin (Minor); Clarithromycin x Fluconazole (Minor)
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.112 s -- in org.openmrs.module.chartsearchai.reference.PairChipCapContextTest
[INFO] Running org.openmrs.module.chartsearchai.reference.AllergenNameResolutionTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.006 s -- in org.openmrs.module.chartsearchai.reference.AllergenNameResolutionTest
[INFO] Running org.openmrs.module.chartsearchai.reference.ActiveOrderConceptIdentityTest
WARN - PatientClinicalContextBuilder.build(174) |2026-09-07T00:26:28,589| Active drug order e1f95924-697a-11e3-bd76-0800271c1b75 has no readable name; it will be identified by its ATC codes as [ATC N02BA01]. A safety chip for it is labelled that way unless the reference data can name one of those codes, and the order cannot be matched against chart text at all.
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.068 s -- in org.openmrs.module.chartsearchai.reference.ActiveOrderConceptIdentityTest
[INFO] Running org.openmrs.module.chartsearchai.reference.PresentationMoietyAllergenTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in org.openmrs.module.chartsearchai.reference.PresentationMoietyAllergenTest
[INFO] Running org.openmrs.module.chartsearchai.reference.ProseWarningsTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.294 s -- in org.openmrs.module.chartsearchai.reference.ProseWarningsTest
[INFO] Running org.openmrs.module.chartsearchai.reference.PairChipExtentContextTest
WARN - DrugSafetyValidator.validate(287) |2026-09-07T00:26:29,218| Drug-safety validation failed; returning no warnings — the answer path is never broken
java.lang.IllegalStateException: the reference dataset is unreadable
	at org.openmrs.module.chartsearchai.reference.PairChipExtentContextTest$2.withReferenceNames(PairChipExtentContextTest.java:341) ~[test-classes/:?]
	at org.openmrs.module.chartsearchai.reference.DrugSafetyValidator.validate(DrugSafetyValidator.java:428) ~[classes/:?]
	at org.openmrs.module.chartsearchai.reference.DrugSafetyValidator.validate(DrugSafetyValidator.java:284) ~[classes/:?]
	at org.openmrs.module.chartsearchai.reference.PairChipExtentContextTest.aPassThatThrewStatesNothingRatherThanACompleteScreen(PairChipExtentContextTest.java:346) ~[test-classes/:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:767) ~[junit-platform-commons-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$8(TestMethodTestDescriptor.java:217) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:213) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:138) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:156) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.executeWithoutCancellationToken(LauncherAdapter.java:60) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.execute(LauncherAdapter.java:52) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:203) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:168) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:136) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) [surefire-booter-3.5.5.jar:3.5.5]
WARN - DrugSafetyValidator.addQuestionPairInteractions(5014) |2026-09-07T00:26:29,243| Question-pair safety: 3 of 72 question-named drug pairs shown (cap 3); the question resolved 16 reference drugs, and pairs grow as N^2/2. Withheld, least severe last: [Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor]
WARN - DrugSafetyValidator.addQuestionPairInteractions(5014) |2026-09-07T00:26:29,263| Question-pair safety: 10 of 63 question-named drug pairs shown (cap 10); the question resolved 16 reference drugs, and pairs grow as N^2/2. Withheld, least severe last: [Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Minor, Minor, Minor, Minor, Minor, Minor, Minor]
WARN - DrugSafetyValidator.addActiveOrderPairInteractions(6366) |2026-09-07T00:26:29,285| Interaction screening across 6 active-order reference entries found 15 pair(s) above the severity floor; reporting the 1 most severe and WITHHOLDING 14: Simvastatin x Fluconazole (Major); Simvastatin x Amiodarone (Major); Warfarin x Ciprofloxacin (Major); Warfarin x Clarithromycin (Major); Warfarin x Fluconazole (Major); Warfarin x Amiodarone (Major); Ciprofloxacin x Amiodarone (Major); Clarithromycin x Amiodarone (Major); Fluconazole x Amiodarone (Major); Simvastatin x Ciprofloxacin (Moderate); Ciprofloxacin x Clarithromycin (Moderate); Ciprofloxacin x Fluconazole (Moderate); Simvastatin x Warfarin (Minor); Clarithromycin x Fluconazole (Minor)
WARN - DrugSafetyValidator.addQuestionPairInteractions(5014) |2026-09-07T00:26:29,288| Question-pair safety: 1 of 72 question-named drug pairs shown (cap 1); the question resolved 16 reference drugs, and pairs grow as N^2/2. Withheld, least severe last: [Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor]
WARN - DrugSafetyValidator.addActiveOrderPairInteractions(6366) |2026-09-07T00:26:29,289| Interaction screening across 6 active-order reference entries found 15 pair(s) above the severity floor; reporting the 3 most severe and WITHHOLDING 12: Warfarin x Ciprofloxacin (Major); Warfarin x Clarithromycin (Major); Warfarin x Fluconazole (Major); Warfarin x Amiodarone (Major); Ciprofloxacin x Amiodarone (Major); Clarithromycin x Amiodarone (Major); Fluconazole x Amiodarone (Major); Simvastatin x Ciprofloxacin (Moderate); Ciprofloxacin x Clarithromycin (Moderate); Ciprofloxacin x Fluconazole (Moderate); Simvastatin x Warfarin (Minor); Clarithromycin x Fluconazole (Minor)
WARN - DrugSafetyValidator.addQuestionPairInteractions(5014) |2026-09-07T00:26:29,291| Question-pair safety: 3 of 72 question-named drug pairs shown (cap 3); the question resolved 16 reference drugs, and pairs grow as N^2/2. Withheld, least severe last: [Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor]
WARN - DrugSafetyValidator.addActiveOrderPairInteractions(6366) |2026-09-07T00:26:29,293| Interaction screening across 6 active-order reference entries found 15 pair(s) above the severity floor; reporting the 10 most severe and WITHHOLDING 5: Simvastatin x Ciprofloxacin (Moderate); Ciprofloxacin x Clarithromycin (Moderate); Ciprofloxacin x Fluconazole (Moderate); Simvastatin x Warfarin (Minor); Clarithromycin x Fluconazole (Minor)
WARN - DrugSafetyValidator.addQuestionPairInteractions(5014) |2026-09-07T00:26:29,294| Question-pair safety: 10 of 72 question-named drug pairs shown (cap 10); the question resolved 16 reference drugs, and pairs grow as N^2/2. Withheld, least severe last: [Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor]
WARN - DrugSafetyValidator.addQuestionPairInteractions(5014) |2026-09-07T00:26:29,298| Question-pair safety: 15 of 72 question-named drug pairs shown (cap 15); the question resolved 16 reference drugs, and pairs grow as N^2/2. Withheld, least severe last: [Major, Major, Major, Major, Major, Major, Major, Major, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor]
WARN - DrugSafetyValidator.addActiveOrderPairInteractions(6366) |2026-09-07T00:26:29,339| Interaction screening across 6 active-order reference entries found 15 pair(s) above the severity floor; reporting the 3 most severe and WITHHOLDING 12: Warfarin x Ciprofloxacin (Major); Warfarin x Clarithromycin (Major); Warfarin x Fluconazole (Major); Warfarin x Amiodarone (Major); Ciprofloxacin x Amiodarone (Major); Clarithromycin x Amiodarone (Major); Fluconazole x Amiodarone (Major); Simvastatin x Ciprofloxacin (Moderate); Ciprofloxacin x Clarithromycin (Moderate); Ciprofloxacin x Fluconazole (Moderate); Simvastatin x Warfarin (Minor); Clarithromycin x Fluconazole (Minor)
[INFO] Tests run: 22, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.437 s -- in org.openmrs.module.chartsearchai.reference.PairChipExtentContextTest
[INFO] Running org.openmrs.module.chartsearchai.reference.NamelessActiveOrderPartnerTest
WARN - PatientClinicalContextBuilder.build(174) |2026-09-07T00:26:29,371| Active drug order e1f95924-697a-11e3-bd76-0800271c1b75 has no readable name; it will be identified by its ATC codes as [ATC M01AE02, M01AE04]. A safety chip for it is labelled that way unless the reference data can name one of those codes, and the order cannot be matched against chart text at all.
WARN - PatientClinicalContextBuilder.build(174) |2026-09-07T00:26:29,400| Active drug order e1f95924-697a-11e3-bd76-0800271c1b75 has no readable name; it will be identified by its ATC codes as [ATC M01AE02, M01AE04]. A safety chip for it is labelled that way unless the reference data can name one of those codes, and the order cannot be matched against chart text at all.
WARN - PatientClinicalContextBuilder.build(174) |2026-09-07T00:26:29,428| Active drug order e1f95924-697a-11e3-bd76-0800271c1b75 has no readable name; it will be identified by its ATC codes as [ATC M01AE02, M01AE04]. A safety chip for it is labelled that way unless the reference data can name one of those codes, and the order cannot be matched against chart text at all.
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:29,433| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [[ATC M01AE02, M01AE04] [e1f95924-697a-11e3-bd76-0800271c1b75]]
WARN - PatientClinicalContextBuilder.build(174) |2026-09-07T00:26:29,454| Active drug order e1f95924-697a-11e3-bd76-0800271c1b75 has no readable name; it will be identified by its ATC codes as [ATC C10AA01, J01FA09]. A safety chip for it is labelled that way unless the reference data can name one of those codes, and the order cannot be matched against chart text at all.
WARN - PatientClinicalContextBuilder.build(174) |2026-09-07T00:26:29,486| Active drug order e1f95924-697a-11e3-bd76-0800271c1b75 has no readable name; it will be identified by its ATC codes as [ATC N02BA01, N02BA99]. A safety chip for it is labelled that way unless the reference data can name one of those codes, and the order cannot be matched against chart text at all.
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.151 s -- in org.openmrs.module.chartsearchai.reference.NamelessActiveOrderPartnerTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DrugSafetyQuestionPairInteractionTest
WARN - DrugSafetyValidator.addQuestionPairInteractions(5014) |2026-09-07T00:26:29,495| Question-pair safety: 10 of 72 question-named drug pairs shown (cap 10); the question resolved 16 reference drugs, and pairs grow as N^2/2. Withheld, least severe last: [Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor]
WARN - DrugSafetyValidator.addQuestionPairInteractions(5014) |2026-09-07T00:26:29,508| Question-pair safety: 10 of 72 question-named drug pairs shown (cap 10); the question resolved 16 reference drugs, and pairs grow as N^2/2. Withheld, least severe last: [Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Major, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Moderate, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor, Minor]
[INFO] Tests run: 22, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.025 s -- in org.openmrs.module.chartsearchai.reference.DrugSafetyQuestionPairInteractionTest
[INFO] Running org.openmrs.module.chartsearchai.reference.MultiCodeClassChipTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in org.openmrs.module.chartsearchai.reference.MultiCodeClassChipTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DrugSafetyWeightContextTest
WARN - DrugSafetyValidator.validate(287) |2026-09-07T00:26:29,815| Drug-safety validation failed; returning no warnings — the answer path is never broken
java.lang.RuntimeException: boom
	at org.openmrs.module.chartsearchai.reference.DrugSafetyWeightContextTest.lambda$validatorFailureNeverBreaksTheAnswerPath$0(DrugSafetyWeightContextTest.java:211) ~[test-classes/:?]
	at org.openmrs.module.chartsearchai.reference.DrugReferenceService.ensureLoaded(DrugReferenceService.java:1570) ~[classes/:?]
	at org.openmrs.module.chartsearchai.reference.DrugReferenceService.getAll(DrugReferenceService.java:182) ~[classes/:?]
	at org.openmrs.module.chartsearchai.reference.DrugReferenceService.findByDrugName(DrugReferenceService.java:1283) ~[classes/:?]
	at org.openmrs.module.chartsearchai.reference.DrugReferenceService.findImpliedByDrugName(DrugReferenceService.java:409) ~[classes/:?]
	at org.openmrs.module.chartsearchai.reference.DrugReferenceService.findForActiveOrders(DrugReferenceService.java:1368) ~[classes/:?]
	at org.openmrs.module.chartsearchai.reference.DrugSafetyValidator.validate(DrugSafetyValidator.java:427) ~[classes/:?]
	at org.openmrs.module.chartsearchai.reference.DrugSafetyValidator.validate(DrugSafetyValidator.java:284) ~[classes/:?]
	at org.openmrs.module.chartsearchai.reference.DrugSafetyValidator.validate(DrugSafetyValidator.java:259) ~[classes/:?]
	at org.openmrs.module.chartsearchai.reference.DrugSafetyValidator.validate(DrugSafetyValidator.java:232) ~[classes/:?]
	at org.openmrs.module.chartsearchai.reference.DrugSafetyWeightContextTest.validatorFailureNeverBreaksTheAnswerPath(DrugSafetyWeightContextTest.java:216) ~[test-classes/:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:767) ~[junit-platform-commons-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$8(TestMethodTestDescriptor.java:217) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:213) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:138) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:156) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.executeWithoutCancellationToken(LauncherAdapter.java:60) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.execute(LauncherAdapter.java:52) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:203) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:168) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:136) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) [surefire-booter-3.5.5.jar:3.5.5]
WARN - DrugReferenceInjector.inject(472) |2026-09-07T00:26:29,827| Drug-reference injection failed; leaving the chart unmodified — the answer path is never broken
java.lang.RuntimeException: boom
	at org.openmrs.module.chartsearchai.reference.DrugSafetyWeightContextTest.lambda$injectorFailureNeverBreaksTheAnswerPath$1(DrugSafetyWeightContextTest.java:224) ~[test-classes/:?]
	at org.openmrs.module.chartsearchai.reference.DrugReferenceService.ensureLoaded(DrugReferenceService.java:1570) ~[classes/:?]
	at org.openmrs.module.chartsearchai.reference.DrugReferenceService.getAll(DrugReferenceService.java:182) ~[classes/:?]
	at org.openmrs.module.chartsearchai.reference.DrugReferenceService.findByDrugName(DrugReferenceService.java:1283) ~[classes/:?]
	at org.openmrs.module.chartsearchai.reference.DrugReferenceService.findImpliedByDrugName(DrugReferenceService.java:409) ~[classes/:?]
	at org.openmrs.module.chartsearchai.reference.DrugReferenceService.findForActiveOrders(DrugReferenceService.java:1368) ~[classes/:?]
	at org.openmrs.module.chartsearchai.reference.DrugReferenceInjector.injectRecords(DrugReferenceInjector.java:497) ~[classes/:?]
	at org.openmrs.module.chartsearchai.reference.DrugReferenceInjector.inject(DrugReferenceInjector.java:469) ~[classes/:?]
	at org.openmrs.module.chartsearchai.reference.DrugSafetyWeightContextTest.injectorFailureNeverBreaksTheAnswerPath(DrugSafetyWeightContextTest.java:229) ~[test-classes/:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:767) ~[junit-platform-commons-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$8(TestMethodTestDescriptor.java:217) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:213) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:138) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:156) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.executeWithoutCancellationToken(LauncherAdapter.java:60) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.execute(LauncherAdapter.java:52) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:203) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:168) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:136) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) [surefire-booter-3.5.5.jar:3.5.5]
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.308 s -- in org.openmrs.module.chartsearchai.reference.DrugSafetyWeightContextTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DrugReferenceRenderRobustnessTest
WARN - JsonDrugReferenceSource.parse(127) |2026-09-07T00:26:29,830| Dropped 2 unusable drug-reference entries (blank id or name)
WARN - JsonDrugReferenceSource.parse(127) |2026-09-07T00:26:29,831| Dropped 2 unusable drug-reference entries (blank id or name)
WARN - JsonDrugReferenceSource.parse(127) |2026-09-07T00:26:29,831| Dropped 2 unusable drug-reference entries (blank id or name)
WARN - JsonDrugReferenceSource.parse(127) |2026-09-07T00:26:29,832| Dropped 2 unusable drug-reference entries (blank id or name)
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in org.openmrs.module.chartsearchai.reference.DrugReferenceRenderRobustnessTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DrugSafetyValidatorTest
[INFO] Tests run: 25, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.011 s -- in org.openmrs.module.chartsearchai.reference.DrugSafetyValidatorTest
[INFO] Running org.openmrs.module.chartsearchai.reference.ConditionRuleCoverageGateContextTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.298 s -- in org.openmrs.module.chartsearchai.reference.ConditionRuleCoverageGateContextTest
[INFO] Running org.openmrs.module.chartsearchai.reference.ResidualAtcClassClaimTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in org.openmrs.module.chartsearchai.reference.ResidualAtcClassClaimTest
[INFO] Running org.openmrs.module.chartsearchai.reference.SubstanceIdentityTest
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:30,147| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:30,147| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:30,148| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:30,149| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:30,150| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:30,150| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:30,151| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:30,152| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:30,152| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.007 s -- in org.openmrs.module.chartsearchai.reference.SubstanceIdentityTest
[INFO] Running org.openmrs.module.chartsearchai.reference.OverdoseSubstanceCollapseTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 s -- in org.openmrs.module.chartsearchai.reference.OverdoseSubstanceCollapseTest
[INFO] Running org.openmrs.module.chartsearchai.reference.UnknownSeverityFindingStrengthContextTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.011 s -- in org.openmrs.module.chartsearchai.reference.UnknownSeverityFindingStrengthContextTest
[INFO] Running org.openmrs.module.chartsearchai.reference.AllergenExactNameResolutionTest
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:30,169| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:30,169| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:30,170| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:30,170| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:30,170| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:30,171| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.006 s -- in org.openmrs.module.chartsearchai.reference.AllergenExactNameResolutionTest
[INFO] Running org.openmrs.module.chartsearchai.reference.BridgedConceptOrderResolutionTest
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.011 s -- in org.openmrs.module.chartsearchai.reference.BridgedConceptOrderResolutionTest
[INFO] Running org.openmrs.module.chartsearchai.reference.OneSubstanceOneRuleTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in org.openmrs.module.chartsearchai.reference.OneSubstanceOneRuleTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DdiDrugReferenceSourceTest
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:30,718| Drug-reference data validity — dataset-missing-a-required-table (reported, 1): a 'ddinter' document must declare [interactions]; this one does not, so it parsed to no entries at all, discarding the 3 row(s) it does carry. The data is left as loaded — the fix is in the file: either it is not a document of the format its reader expects, or, where a document carrying none of them is intended, the missing table has to be declared empty. Reading it as empty here would load an export truncated before it wrote that table as a complete one.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:30,719| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
[INFO] Tests run: 25, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.879 s -- in org.openmrs.module.chartsearchai.reference.DdiDrugReferenceSourceTest
[INFO] Running org.openmrs.module.chartsearchai.reference.OrderPartnerNameSourceWritePathTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.578 s -- in org.openmrs.module.chartsearchai.reference.OrderPartnerNameSourceWritePathTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DrugSafetyEvalTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.010 s -- in org.openmrs.module.chartsearchai.reference.DrugSafetyEvalTest
[INFO] Running org.openmrs.module.chartsearchai.reference.WeightAwareOverdoseTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in org.openmrs.module.chartsearchai.reference.WeightAwareOverdoseTest
[INFO] Running org.openmrs.module.chartsearchai.reference.UncorroboratedFindingProvenanceTest
[INFO] Tests run: 20, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.011 s -- in org.openmrs.module.chartsearchai.reference.UncorroboratedFindingProvenanceTest
[INFO] Running org.openmrs.module.chartsearchai.reference.ReferenceRecordRowAttributionTest
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:32,673| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Clobetasol 0.05% [order-Clobetasol 0.05%]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:32,674| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Amoxicillin (suspension) [order-Amoxicillin (suspension)]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:32,675| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Dexamethasone (ophthalmic) [order-Dexamethasone (ophthalmic)]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:32,676| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Mupirocin (nasal) [order-Mupirocin (nasal)]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:32,677| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Dexamethasone (ophthalmic) [order-Dexamethasone (ophthalmic)]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:32,678| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Acetylsalicylic acid (enteric-coated) [order-Acetylsalicylic acid (enteric-coated)]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:32,679| Active-order reconciliation: 2 of 2 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Dexamethasone (ophthalmic) [order-Dexamethasone (ophthalmic)], Phenytoin [order-Phenytoin]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:32,680| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [zantac [order-zantac]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:32,681| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Ibuprofen [order-Ibuprofen]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:32,682| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Dexamethasone (ophthalmic) [order-Dexamethasone (ophthalmic)]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:32,683| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [tmp-paediatric [order-tmp-paediatric]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:32,684| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Cefadroxil [order-Cefadroxil]]
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.012 s -- in org.openmrs.module.chartsearchai.reference.ReferenceRecordRowAttributionTest
[INFO] Running org.openmrs.module.chartsearchai.reference.UnmappedOrderClassPartnerTest
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.008 s -- in org.openmrs.module.chartsearchai.reference.UnmappedOrderClassPartnerTest
[INFO] Running org.openmrs.module.chartsearchai.reference.ActiveOrderReconciliationContextTest
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:32,708| Active-order reconciliation: 4 of 4 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Triomune-30 [e3d621f0-a4d5-47d1-a4e1-5ace3f66d43a], NYQUIL [0c96f25c-4949-4f72-9931-d808fbc226db], Triomune-30 [2662e6c2-697b-11e3-bd76-0800271c1b75], ASPIRIN [9c21e407-697b-11e3-bd76-0800271c1b75]]
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.028 s -- in org.openmrs.module.chartsearchai.reference.ActiveOrderReconciliationContextTest
[INFO] Running org.openmrs.module.chartsearchai.reference.QuerystoreOrderTextMarkerTest
WARN - PersonName.getFullName(417) |2026-09-07T00:26:32,731| No name layout format set
WARN - PersonName.getFullName(417) |2026-09-07T00:26:32,731| No name layout format set
WARN - PersonName.getFullName(417) |2026-09-07T00:26:32,732| No name layout format set
WARN - PersonName.getFullName(417) |2026-09-07T00:26:32,732| No name layout format set
WARN - PersonName.getFullName(417) |2026-09-07T00:26:32,740| No name layout format set
WARN - PersonName.getFullName(417) |2026-09-07T00:26:32,740| No name layout format set
WARN - PersonName.getFullName(417) |2026-09-07T00:26:32,741| No name layout format set
WARN - PersonName.getFullName(417) |2026-09-07T00:26:32,741| No name layout format set
INFO - OrderServiceImpl.getOrderNumberGenerator(398) |2026-09-07T00:26:32,764| Setting default order number generator
WARN - PersonName.getFullName(417) |2026-09-07T00:26:32,780| No name layout format set
WARN - PersonName.getFullName(417) |2026-09-07T00:26:32,780| No name layout format set
WARN - PersonName.getFullName(417) |2026-09-07T00:26:32,781| No name layout format set
WARN - PersonName.getFullName(417) |2026-09-07T00:26:32,781| No name layout format set
WARN - PersonName.getFullName(417) |2026-09-07T00:26:32,788| No name layout format set
WARN - PersonName.getFullName(417) |2026-09-07T00:26:32,789| No name layout format set
WARN - PersonName.getFullName(417) |2026-09-07T00:26:32,789| No name layout format set
WARN - PersonName.getFullName(417) |2026-09-07T00:26:32,789| No name layout format set
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.068 s -- in org.openmrs.module.chartsearchai.reference.QuerystoreOrderTextMarkerTest
[INFO] Running org.openmrs.module.chartsearchai.reference.ContraindicationTokenDiacriticFoldTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.006 s -- in org.openmrs.module.chartsearchai.reference.ContraindicationTokenDiacriticFoldTest
[INFO] Running org.openmrs.module.chartsearchai.reference.ActiveOrderAtcContextTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.047 s -- in org.openmrs.module.chartsearchai.reference.ActiveOrderAtcContextTest
[INFO] Running org.openmrs.module.chartsearchai.reference.SubstanceNameRowTest
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:34,220| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:34,220| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:34,745| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:34,746| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:34,748| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:34,748| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
[INFO] Tests run: 19, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.904 s -- in org.openmrs.module.chartsearchai.reference.SubstanceNameRowTest
[INFO] Running org.openmrs.module.chartsearchai.reference.ShippedDrugReferenceDefaultTest
INFO - DdiDrugReferenceSource.parse(318) |2026-09-07T00:26:35,027| Parsed 2283 DDInter drug-reference entries
INFO - ReferenceDataFiles.loadWithClasspathFallback(228) |2026-09-07T00:26:35,028| Loaded 2283 DDInter drug-reference entries from bundled default /chartsearchai/ddi-knowledge-base.json
INFO - DrugReferenceValidity.logTo(382) |2026-09-07T00:26:35,038| Drug-reference data validity — self-paired-interaction-rows (dropped, 28): 28 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible. (in the dataset the module ships, so the remedy is a data fix upstream rather than a change to this deployment)
INFO - DrugReferenceValidity.logTo(382) |2026-09-07T00:26:35,038| Drug-reference data validity — alias-names-another-substance (reported, 18): 18 published name(s) denote a DIFFERENT substance in this dataset, so a question or a chart string carrying one resolves the wrong drug. The data is left as loaded — the fix is in the dataset. [Omeprazole publishes 'esomeprazole', which is Esomeprazole's own name, Pfizer-BioNTech Covid-19 Vaccine publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Trastuzumab emtansine publishes 'trastuzumab deruxtecan', which is Trastuzumab deruxtecan's own name, Levoketoconazole publishes 'ketoconazole', which is Ketoconazole's own name, Tozinameran (12y+) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Tozinameran (5y-11y) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Tozinameran (6m-4y) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Gallium citrate ga-67 publishes 'gallium nitrate', which is Gallium nitrate's own name] and 10 more (in the dataset the module ships, so the remedy is a data fix upstream rather than a change to this deployment)
INFO - DrugReferenceValidity.logTo(382) |2026-09-07T00:26:35,038| Drug-reference data validity — derivative-merged-with-its-parent-substance (reported, 1): 1 row(s) are keyed as ONE substance with rows their own name says they only DERIVE from, so a chip, a record or a citation about the substance can be named after the derivative and carry its rules. The data is left as loaded — the fix is in the dataset, which has to stop filing the derivative under the parent's substance: in a DDInter-shaped file by giving it its own drugbank_id, which is what keeps every derivative this module already separates apart, and in a curated file by giving it its own substanceName. [Fluoroestradiol f-18 is filed as 'estradiol' beside [Estradiol, Estradiol (topical)]] (in the dataset the module ships, so the remedy is a data fix upstream rather than a change to this deployment)
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:35,039| Drug-reference safety arms over the 2283 entr(ies) read from classpath:/chartsearchai/ddi-knowledge-base.json: doseCeilings=absent (0), handAuthoredRules=absent (0), atcCodes=published (1839), interactions=published (2283), conditionRules=absent (0)
INFO - DdiDrugReferenceSource.parse(318) |2026-09-07T00:26:35,865| Parsed 2283 DDInter drug-reference entries
INFO - ReferenceDataFiles.loadWithClasspathFallback(228) |2026-09-07T00:26:35,865| Loaded 2283 DDInter drug-reference entries from bundled default /chartsearchai/ddi-knowledge-base.json
INFO - DrugReferenceValidity.logTo(382) |2026-09-07T00:26:35,875| Drug-reference data validity — self-paired-interaction-rows (dropped, 28): 28 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible. (in the dataset the module ships, so the remedy is a data fix upstream rather than a change to this deployment)
INFO - DrugReferenceValidity.logTo(382) |2026-09-07T00:26:35,875| Drug-reference data validity — alias-names-another-substance (reported, 18): 18 published name(s) denote a DIFFERENT substance in this dataset, so a question or a chart string carrying one resolves the wrong drug. The data is left as loaded — the fix is in the dataset. [Omeprazole publishes 'esomeprazole', which is Esomeprazole's own name, Pfizer-BioNTech Covid-19 Vaccine publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Trastuzumab emtansine publishes 'trastuzumab deruxtecan', which is Trastuzumab deruxtecan's own name, Levoketoconazole publishes 'ketoconazole', which is Ketoconazole's own name, Tozinameran (12y+) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Tozinameran (5y-11y) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Tozinameran (6m-4y) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Gallium citrate ga-67 publishes 'gallium nitrate', which is Gallium nitrate's own name] and 10 more (in the dataset the module ships, so the remedy is a data fix upstream rather than a change to this deployment)
INFO - DrugReferenceValidity.logTo(382) |2026-09-07T00:26:35,875| Drug-reference data validity — derivative-merged-with-its-parent-substance (reported, 1): 1 row(s) are keyed as ONE substance with rows their own name says they only DERIVE from, so a chip, a record or a citation about the substance can be named after the derivative and carry its rules. The data is left as loaded — the fix is in the dataset, which has to stop filing the derivative under the parent's substance: in a DDInter-shaped file by giving it its own drugbank_id, which is what keeps every derivative this module already separates apart, and in a curated file by giving it its own substanceName. [Fluoroestradiol f-18 is filed as 'estradiol' beside [Estradiol, Estradiol (topical)]] (in the dataset the module ships, so the remedy is a data fix upstream rather than a change to this deployment)
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:35,876| Drug-reference safety arms over the 2283 entr(ies) read from classpath:/chartsearchai/ddi-knowledge-base.json: doseCeilings=absent (0), handAuthoredRules=absent (0), atcCodes=published (1839), interactions=published (2283), conditionRules=absent (0)
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.413 s -- in org.openmrs.module.chartsearchai.reference.ShippedDrugReferenceDefaultTest
[INFO] Running org.openmrs.module.chartsearchai.reference.CrossReactivityStatusContextTest
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:36,181| Drug-reference data validity — dataset-missing-a-required-table (reported, 1): a 'cross-reactivity groups' document must declare [groups]; this one does not, so it parsed to no groups at all. The data is left as loaded — the fix is in the file: either it is not a document of the format its reader expects, or, where a document carrying none of them is intended, the missing table has to be declared empty. Reading it as empty here would load an export truncated before it wrote that table as a complete one.
INFO - ReferenceDataFiles.readOperatorFile(351) |2026-09-07T00:26:36,202| cross-reactivity groups file 'chartsearchai/h266-no-such-groups-for-the-log.json' not available (Model file not found: /var/folders/38/70vr92w528b6f4zr0wxvd1600000gn/T/appdir-for-unit-tests-4728846297741994847/chartsearchai/h266-no-such-groups-for-the-log.json. Set the correct relative path in chartsearchai.drugReference.crossReactivityGroupsFilePath); using bundled default
INFO - ReferenceDataFiles.loadWithClasspathFallback(228) |2026-09-07T00:26:36,202| Loaded 1 cross-reactivity groups from bundled default /chartsearchai/cross-reactivity-groups.json
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:36,202| Drug-reference data validity — configured-data-file-not-read (reported, 1): chartsearchai.drugReference.crossReactivityGroupsFilePath names 'chartsearchai/h266-no-such-groups-for-the-log.json', which could not be read, so 'classpath:/chartsearchai/cross-reactivity-groups.json' was reached for in its place. Whatever count you see is therefore a count of a dataset nobody configured rather than of your file, however healthy it looks.
INFO - ReferenceDataFiles.loadWithClasspathFallback(228) |2026-09-07T00:26:36,234| Loaded 1 cross-reactivity groups from bundled default /chartsearchai/cross-reactivity-groups.json
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:36,241| Drug-reference data validity — configured-data-file-not-read (reported, 1): chartsearchai.drugReference.crossReactivityGroupsFilePath names 'chartsearchai/h266-no-such-groups.json', which could not be read, so 'classpath:/chartsearchai/cross-reactivity-groups.json' was reached for in its place. Whatever count you see is therefore a count of a dataset nobody configured rather than of your file, however healthy it looks.
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.090 s -- in org.openmrs.module.chartsearchai.reference.CrossReactivityStatusContextTest
[INFO] Running org.openmrs.module.chartsearchai.reference.CombinationAllergenResolutionTest
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.009 s -- in org.openmrs.module.chartsearchai.reference.CombinationAllergenResolutionTest
[INFO] Running org.openmrs.module.chartsearchai.reference.ClassChipPartnerLabelTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in org.openmrs.module.chartsearchai.reference.ClassChipPartnerLabelTest
[INFO] Running org.openmrs.module.chartsearchai.reference.ShippedAliasVocabularyTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.301 s -- in org.openmrs.module.chartsearchai.reference.ShippedAliasVocabularyTest
[INFO] Running org.openmrs.module.chartsearchai.reference.ChipSubjectOneResolutionTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.034 s -- in org.openmrs.module.chartsearchai.reference.ChipSubjectOneResolutionTest
[INFO] Running org.openmrs.module.chartsearchai.reference.CoMedicationResolutionPerPassTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.088 s -- in org.openmrs.module.chartsearchai.reference.CoMedicationResolutionPerPassTest
[INFO] Running org.openmrs.module.chartsearchai.reference.LocallyAppliedAtcGroupKeyTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.283 s -- in org.openmrs.module.chartsearchai.reference.LocallyAppliedAtcGroupKeyTest
[INFO] Running org.openmrs.module.chartsearchai.reference.ContraindicationSubjectLabelTest
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:36,977| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:36,978| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:36,978| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:36,979| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:36,980| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:36,980| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.006 s -- in org.openmrs.module.chartsearchai.reference.ContraindicationSubjectLabelTest
[INFO] Running org.openmrs.module.chartsearchai.reference.ContraindicationToggleContextTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.013 s -- in org.openmrs.module.chartsearchai.reference.ContraindicationToggleContextTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DirectAllergyContraindicationTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in org.openmrs.module.chartsearchai.reference.DirectAllergyContraindicationTest
[INFO] Running org.openmrs.module.chartsearchai.reference.ScreeningSubjectLabelTest
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:37,001| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:37,002| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:37,003| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in org.openmrs.module.chartsearchai.reference.ScreeningSubjectLabelTest
[INFO] Running org.openmrs.module.chartsearchai.reference.InjectedContraindicationCorroborationToggleContextTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.016 s -- in org.openmrs.module.chartsearchai.reference.InjectedContraindicationCorroborationToggleContextTest
[INFO] Running org.openmrs.module.chartsearchai.reference.SlicedReferenceRowProvenanceTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.073 s -- in org.openmrs.module.chartsearchai.reference.SlicedReferenceRowProvenanceTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DuplicateTherapySelfChipTest
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.007 s -- in org.openmrs.module.chartsearchai.reference.DuplicateTherapySelfChipTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DrugReferenceSourceValidityChannelTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.001 s -- in org.openmrs.module.chartsearchai.reference.DrugReferenceSourceValidityChannelTest
[INFO] Running org.openmrs.module.chartsearchai.reference.SafetyFindingSeverityStrengthTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.006 s -- in org.openmrs.module.chartsearchai.reference.SafetyFindingSeverityStrengthTest
[INFO] Running org.openmrs.module.chartsearchai.reference.ActiveOrderResolutionPerPassTest
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:37,113| Active-order reconciliation: 4 of 4 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Warfarin [order-Warfarin], Amiodarone [order-Amiodarone], Ciprofloxacin [order-Ciprofloxacin], Digoxin [order-Digoxin]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:37,115| Active-order reconciliation: 4 of 4 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Warfarin [order-Warfarin], Amiodarone [order-Amiodarone], Ciprofloxacin [order-Ciprofloxacin], Digoxin [order-Digoxin]]
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.007 s -- in org.openmrs.module.chartsearchai.reference.ActiveOrderResolutionPerPassTest
[INFO] Running org.openmrs.module.chartsearchai.reference.InjectedContraindicationReadingToggleContextTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.018 s -- in org.openmrs.module.chartsearchai.reference.InjectedContraindicationReadingToggleContextTest
[INFO] Running org.openmrs.module.chartsearchai.reference.OrderDrivenInjectionResolutionTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.007 s -- in org.openmrs.module.chartsearchai.reference.OrderDrivenInjectionResolutionTest
[INFO] Running org.openmrs.module.chartsearchai.reference.SelfNamedAllergyRuleRankTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in org.openmrs.module.chartsearchai.reference.SelfNamedAllergyRuleRankTest
[INFO] Running org.openmrs.module.chartsearchai.reference.OneOrderNameAcrossOneResponseTest
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:38,095| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Emtricitabine / Tenofovir disoproxil [order-truvada]]
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:38,097| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:38,097| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
[INFO] Tests run: 23, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.221 s -- in org.openmrs.module.chartsearchai.reference.OneOrderNameAcrossOneResponseTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DrugReferenceLoadContextTest
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:38,379| Drug-reference data validity — rules-without-a-substance-identity (reported, 1): 1 rule-bearing entr(ies) share a published name with another entry while declaring no substanceName, so each is its own substance and only the strongest claimant on that name is put in play — the others' rules are silently dropped. Fix: declare substanceName on the rows that are one substance AND name a presentation with a trailing parenthesized qualifier ('Ibuprofen (tablets)', not 'Ibuprofen tablets'), which is what the shipped datasets do; the claim alone is vetoed by the display stem. [Levoketoconazole shares 'ketoconazole' with [Ketoconazole (oral), Levoketoconazole]]
INFO - DdiDrugReferenceSource.parse(318) |2026-09-07T00:26:38,404| Parsed 16 DDInter drug-reference entries
INFO - ReferenceDataFiles.readOperatorFile(344) |2026-09-07T00:26:38,405| Loaded 16 DDInter drug-reference entries from /var/folders/38/70vr92w528b6f4zr0wxvd1600000gn/T/appdir-for-unit-tests-4728846297741994847/chartsearchai/healthy-ddi.json
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:38,405| Drug-reference safety arms over the 16 entr(ies) read from appdata:chartsearchai/healthy-ddi.json: doseCeilings=absent (0), handAuthoredRules=absent (0), atcCodes=published (16), interactions=published (16), conditionRules=absent (0)
INFO - ReferenceDataFiles.readOperatorFile(344) |2026-09-07T00:26:38,417| Loaded 0 DDInter drug-reference entries from /var/folders/38/70vr92w528b6f4zr0wxvd1600000gn/T/appdir-for-unit-tests-4728846297741994847/chartsearchai/mismatch-curated.json
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:38,417| Drug-reference data validity — dataset-missing-a-required-table (reported, 2): a 'ddinter' document must declare [drugs, interactions]; this one does not, so it parsed to no entries at all. The data is left as loaded — the fix is in the file: either it is not a document of the format its reader expects, or, where a document carrying none of them is intended, the missing table has to be declared empty. Reading it as empty here would load an export truncated before it wrote that table as a complete one.
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:38,417| Drug-reference safety arms over the 0 entr(ies) read from appdata:chartsearchai/mismatch-curated.json: doseCeilings=absent (0), handAuthoredRules=absent (0), atcCodes=absent (0), interactions=absent (0), conditionRules=absent (0)
WARN - DrugReferenceService.ensureLoaded(1611) |2026-09-07T00:26:38,417| Loaded 0 drug-reference entries — drug-safety checking is INERT: no interaction, allergy or contraindication warning can be raised, and every safety question will answer as though there were nothing to find. chartsearchai.drugReference.sourceFormat=ddinter (parser in use: ddinter), chartsearchai.drugReference.dataFilePath=chartsearchai/mismatch-curated.json, read from appdata:chartsearchai/mismatch-curated.json. The usual cause is a format/path mismatch: each source format parses only its own shape and returns nothing — without failing — for another's.
INFO - ReferenceDataFiles.loadOperatorFile(275) |2026-09-07T00:26:38,425| No dataset file is configured for ATC drug-reference entries and there is no bundled fallback; running empty
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:38,425| Drug-reference safety arms over the 0 entr(ies) read from none: doseCeilings=absent (0), handAuthoredRules=absent (0), atcCodes=absent (0), interactions=absent (0), conditionRules=absent (0)
WARN - DrugReferenceService.ensureLoaded(1611) |2026-09-07T00:26:38,425| Loaded 0 drug-reference entries — drug-safety checking is INERT: no interaction, allergy or contraindication warning can be raised, and every safety question will answer as though there were nothing to find. chartsearchai.drugReference.sourceFormat=atc (parser in use: atc), chartsearchai.drugReference.dataFilePath=, read from none. The usual cause is a format/path mismatch: each source format parses only its own shape and returns nothing — without failing — for another's.
INFO - DdiDrugReferenceSource.parse(318) |2026-09-07T00:26:38,742| Parsed 2283 DDInter drug-reference entries
INFO - ReferenceDataFiles.readOperatorFile(344) |2026-09-07T00:26:38,742| Loaded 2283 DDInter drug-reference entries from /var/folders/38/70vr92w528b6f4zr0wxvd1600000gn/T/appdir-for-unit-tests-4728846297741994847/chartsearchai/relative-ddi.json
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:38,751| Drug-reference data validity — self-paired-interaction-rows (dropped, 28): 28 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:38,752| Drug-reference data validity — alias-names-another-substance (reported, 18): 18 published name(s) denote a DIFFERENT substance in this dataset, so a question or a chart string carrying one resolves the wrong drug. The data is left as loaded — the fix is in the dataset. [Omeprazole publishes 'esomeprazole', which is Esomeprazole's own name, Pfizer-BioNTech Covid-19 Vaccine publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Trastuzumab emtansine publishes 'trastuzumab deruxtecan', which is Trastuzumab deruxtecan's own name, Levoketoconazole publishes 'ketoconazole', which is Ketoconazole's own name, Tozinameran (12y+) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Tozinameran (5y-11y) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Tozinameran (6m-4y) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Gallium citrate ga-67 publishes 'gallium nitrate', which is Gallium nitrate's own name] and 10 more
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:38,752| Drug-reference data validity — derivative-merged-with-its-parent-substance (reported, 1): 1 row(s) are keyed as ONE substance with rows their own name says they only DERIVE from, so a chip, a record or a citation about the substance can be named after the derivative and carry its rules. The data is left as loaded — the fix is in the dataset, which has to stop filing the derivative under the parent's substance: in a DDInter-shaped file by giving it its own drugbank_id, which is what keeps every derivative this module already separates apart, and in a curated file by giving it its own substanceName. [Fluoroestradiol f-18 is filed as 'estradiol' beside [Estradiol, Estradiol (topical)]]
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:38,752| Drug-reference safety arms over the 2283 entr(ies) read from appdata:chartsearchai/relative-ddi.json: doseCeilings=absent (0), handAuthoredRules=absent (0), atcCodes=published (1839), interactions=published (2283), conditionRules=absent (0)
INFO - ReferenceDataFiles.readOperatorFile(344) |2026-09-07T00:26:38,823| Loaded 0 drug-reference entries from /var/folders/38/70vr92w528b6f4zr0wxvd1600000gn/T/appdir-for-unit-tests-4728846297741994847/chartsearchai/mismatch-ddi.json
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:38,823| Drug-reference data validity — dataset-missing-a-required-table (reported, 1): a 'json' document must declare [entries]; this one does not, so it parsed to no entries at all. The data is left as loaded — the fix is in the file: either it is not a document of the format its reader expects, or, where a document carrying none of them is intended, the missing table has to be declared empty. Reading it as empty here would load an export truncated before it wrote that table as a complete one.
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:38,824| Drug-reference safety arms over the 0 entr(ies) read from appdata:chartsearchai/mismatch-ddi.json: doseCeilings=absent (0), handAuthoredRules=absent (0), atcCodes=absent (0), interactions=absent (0), conditionRules=absent (0)
WARN - DrugReferenceService.ensureLoaded(1611) |2026-09-07T00:26:38,824| Loaded 0 drug-reference entries — drug-safety checking is INERT: no interaction, allergy or contraindication warning can be raised, and every safety question will answer as though there were nothing to find. chartsearchai.drugReference.sourceFormat=json (parser in use: json), chartsearchai.drugReference.dataFilePath=chartsearchai/mismatch-ddi.json, read from appdata:chartsearchai/mismatch-ddi.json. The usual cause is a format/path mismatch: each source format parses only its own shape and returns nothing — without failing — for another's.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:39,115| Drug-reference data validity — self-paired-interaction-rows (dropped, 28): 28 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:39,116| Drug-reference data validity — alias-names-another-substance (reported, 18): 18 published name(s) denote a DIFFERENT substance in this dataset, so a question or a chart string carrying one resolves the wrong drug. The data is left as loaded — the fix is in the dataset. [Omeprazole publishes 'esomeprazole', which is Esomeprazole's own name, Pfizer-BioNTech Covid-19 Vaccine publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Trastuzumab emtansine publishes 'trastuzumab deruxtecan', which is Trastuzumab deruxtecan's own name, Levoketoconazole publishes 'ketoconazole', which is Ketoconazole's own name, Tozinameran (12y+) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Tozinameran (5y-11y) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Tozinameran (6m-4y) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Gallium citrate ga-67 publishes 'gallium nitrate', which is Gallium nitrate's own name] and 10 more
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:39,116| Drug-reference data validity — derivative-merged-with-its-parent-substance (reported, 1): 1 row(s) are keyed as ONE substance with rows their own name says they only DERIVE from, so a chip, a record or a citation about the substance can be named after the derivative and carry its rules. The data is left as loaded — the fix is in the dataset, which has to stop filing the derivative under the parent's substance: in a DDInter-shaped file by giving it its own drugbank_id, which is what keeps every derivative this module already separates apart, and in a curated file by giving it its own substanceName. [Fluoroestradiol f-18 is filed as 'estradiol' beside [Estradiol, Estradiol (topical)]]
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:39,418| Drug-reference data validity — self-paired-interaction-rows (dropped, 28): 28 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:39,418| Drug-reference data validity — alias-names-another-substance (reported, 18): 18 published name(s) denote a DIFFERENT substance in this dataset, so a question or a chart string carrying one resolves the wrong drug. The data is left as loaded — the fix is in the dataset. [Omeprazole publishes 'esomeprazole', which is Esomeprazole's own name, Pfizer-BioNTech Covid-19 Vaccine publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Trastuzumab emtansine publishes 'trastuzumab deruxtecan', which is Trastuzumab deruxtecan's own name, Levoketoconazole publishes 'ketoconazole', which is Ketoconazole's own name, Tozinameran (12y+) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Tozinameran (5y-11y) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Tozinameran (6m-4y) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Gallium citrate ga-67 publishes 'gallium nitrate', which is Gallium nitrate's own name] and 10 more
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:39,418| Drug-reference data validity — derivative-merged-with-its-parent-substance (reported, 1): 1 row(s) are keyed as ONE substance with rows their own name says they only DERIVE from, so a chip, a record or a citation about the substance can be named after the derivative and carry its rules. The data is left as loaded — the fix is in the dataset, which has to stop filing the derivative under the parent's substance: in a DDInter-shaped file by giving it its own drugbank_id, which is what keeps every derivative this module already separates apart, and in a curated file by giving it its own substanceName. [Fluoroestradiol f-18 is filed as 'estradiol' beside [Estradiol, Estradiol (topical)]]
INFO - DdiDrugReferenceSource.parse(318) |2026-09-07T00:26:39,690| Parsed 2283 DDInter drug-reference entries
INFO - ReferenceDataFiles.loadWithClasspathFallback(228) |2026-09-07T00:26:39,690| Loaded 2283 DDInter drug-reference entries from bundled default /chartsearchai/ddi-knowledge-base.json
INFO - DrugReferenceValidity.logTo(382) |2026-09-07T00:26:39,706| Drug-reference data validity — self-paired-interaction-rows (dropped, 28): 28 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible. (in the dataset the module ships, so the remedy is a data fix upstream rather than a change to this deployment)
INFO - DrugReferenceValidity.logTo(382) |2026-09-07T00:26:39,706| Drug-reference data validity — alias-names-another-substance (reported, 18): 18 published name(s) denote a DIFFERENT substance in this dataset, so a question or a chart string carrying one resolves the wrong drug. The data is left as loaded — the fix is in the dataset. [Omeprazole publishes 'esomeprazole', which is Esomeprazole's own name, Pfizer-BioNTech Covid-19 Vaccine publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Trastuzumab emtansine publishes 'trastuzumab deruxtecan', which is Trastuzumab deruxtecan's own name, Levoketoconazole publishes 'ketoconazole', which is Ketoconazole's own name, Tozinameran (12y+) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Tozinameran (5y-11y) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Tozinameran (6m-4y) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Gallium citrate ga-67 publishes 'gallium nitrate', which is Gallium nitrate's own name] and 10 more (in the dataset the module ships, so the remedy is a data fix upstream rather than a change to this deployment)
INFO - DrugReferenceValidity.logTo(382) |2026-09-07T00:26:39,707| Drug-reference data validity — derivative-merged-with-its-parent-substance (reported, 1): 1 row(s) are keyed as ONE substance with rows their own name says they only DERIVE from, so a chip, a record or a citation about the substance can be named after the derivative and carry its rules. The data is left as loaded — the fix is in the dataset, which has to stop filing the derivative under the parent's substance: in a DDInter-shaped file by giving it its own drugbank_id, which is what keeps every derivative this module already separates apart, and in a curated file by giving it its own substanceName. [Fluoroestradiol f-18 is filed as 'estradiol' beside [Estradiol, Estradiol (topical)]] (in the dataset the module ships, so the remedy is a data fix upstream rather than a change to this deployment)
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:39,708| Drug-reference safety arms over the 2283 entr(ies) read from classpath:/chartsearchai/ddi-knowledge-base.json: doseCeilings=absent (0), handAuthoredRules=absent (0), atcCodes=published (1839), interactions=published (2283), conditionRules=absent (0)
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:39,718| Drug-reference data validity — dataset-missing-a-required-table (reported, 2): a 'ddinter' document must declare [drugs, interactions]; this one does not, so it parsed to no entries at all. The data is left as loaded — the fix is in the file: either it is not a document of the format its reader expects, or, where a document carrying none of them is intended, the missing table has to be declared empty. Reading it as empty here would load an export truncated before it wrote that table as a complete one.
WARN - DrugReferenceService.ensureLoaded(1611) |2026-09-07T00:26:39,719| Loaded 0 drug-reference entries — drug-safety checking is INERT: no interaction, allergy or contraindication warning can be raised, and every safety question will answer as though there were nothing to find. chartsearchai.drugReference.sourceFormat=ddinter (parser in use: ddinter), chartsearchai.drugReference.dataFilePath=chartsearchai/inert-curated.json, read from appdata:chartsearchai/inert-curated.json. The usual cause is a format/path mismatch: each source format parses only its own shape and returns nothing — without failing — for another's.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:39,724| Drug-reference data validity — configured-data-file-not-read (reported, 1): chartsearchai.drugReference.dataFilePath names 'chartsearchai/no-such-drug-reference.json', which could not be read, so 'classpath:/chartsearchai/drug-reference.json' was reached for in its place. Whatever count you see is therefore a count of a dataset nobody configured rather than of your file, however healthy it looks.
INFO - ReferenceDataFiles.readOperatorFile(344) |2026-09-07T00:26:39,734| Loaded 4 drug-reference entries from /var/folders/38/70vr92w528b6f4zr0wxvd1600000gn/T/appdir-for-unit-tests-4728846297741994847/chartsearchai/typo-curated.json
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:39,734| Drug-reference data validity — configured-source-format-not-used (reported, 1): chartsearchai.drugReference.sourceFormat is 'ddintr', which matches no adapter, so the 'json' parser is in force instead.
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:39,734| Drug-reference safety arms over the 4 entr(ies) read from appdata:chartsearchai/typo-curated.json: doseCeilings=published (4), handAuthoredRules=published (4), atcCodes=published (4), interactions=published (4), conditionRules=published (3)
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:39,743| Drug-reference data validity — null-list-element (dropped, 2): 2 null element(s) inside an entry's own lists were dropped: a null is not a value, and the consumers of an alias, a code, an age band or a rule dereference it — the safety arms then raise no warning at all for the request, rather than one fewer. The fix is in the file. Entries: [Ibuprofen]
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:40,091| Drug-reference data validity — self-paired-interaction-rows (dropped, 28): 28 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:40,091| Drug-reference data validity — alias-names-another-substance (reported, 18): 18 published name(s) denote a DIFFERENT substance in this dataset, so a question or a chart string carrying one resolves the wrong drug. The data is left as loaded — the fix is in the dataset. [Omeprazole publishes 'esomeprazole', which is Esomeprazole's own name, Pfizer-BioNTech Covid-19 Vaccine publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Trastuzumab emtansine publishes 'trastuzumab deruxtecan', which is Trastuzumab deruxtecan's own name, Levoketoconazole publishes 'ketoconazole', which is Ketoconazole's own name, Tozinameran (12y+) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Tozinameran (5y-11y) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Tozinameran (6m-4y) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Gallium citrate ga-67 publishes 'gallium nitrate', which is Gallium nitrate's own name] and 10 more
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:40,091| Drug-reference data validity — derivative-merged-with-its-parent-substance (reported, 1): 1 row(s) are keyed as ONE substance with rows their own name says they only DERIVE from, so a chip, a record or a citation about the substance can be named after the derivative and carry its rules. The data is left as loaded — the fix is in the dataset, which has to stop filing the derivative under the parent's substance: in a DDInter-shaped file by giving it its own drugbank_id, which is what keeps every derivative this module already separates apart, and in a curated file by giving it its own substanceName. [Fluoroestradiol f-18 is filed as 'estradiol' beside [Estradiol, Estradiol (topical)]]
[INFO] Tests run: 19, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.728 s -- in org.openmrs.module.chartsearchai.reference.DrugReferenceLoadContextTest
[INFO] Running org.openmrs.module.chartsearchai.reference.InjectedContraindicationClauseTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in org.openmrs.module.chartsearchai.reference.InjectedContraindicationClauseTest
[INFO] Running org.openmrs.module.chartsearchai.reference.BridgedConceptResolutionPerPassTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in org.openmrs.module.chartsearchai.reference.BridgedConceptResolutionPerPassTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DrugSafetyOrderNameMatchingTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in org.openmrs.module.chartsearchai.reference.DrugSafetyOrderNameMatchingTest
[INFO] Running org.openmrs.module.chartsearchai.reference.SelfInteractionTest
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:40,107| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:40,109| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:40,109| Drug-reference data validity — self-paired-interaction-rows (dropped, 3): 3 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:40,110| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:40,110| Drug-reference data validity — self-paired-interaction-rows (dropped, 3): 3 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:40,111| Drug-reference data validity — self-paired-interaction-rows (dropped, 3): 3 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:40,111| Drug-reference data validity — self-paired-interaction-rows (dropped, 3): 3 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:40,111| Drug-reference data validity — self-paired-interaction-rows (dropped, 3): 3 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:40,112| Drug-reference data validity — self-paired-interaction-rows (dropped, 3): 3 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:40,112| Drug-reference data validity — self-paired-interaction-rows (dropped, 3): 3 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 s -- in org.openmrs.module.chartsearchai.reference.SelfInteractionTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DrugReferenceFindingLoudnessTest
INFO - DdiDrugReferenceSource.parse(318) |2026-09-07T00:26:40,128| Parsed 9 DDInter drug-reference entries
INFO - ReferenceDataFiles.readOperatorFile(344) |2026-09-07T00:26:40,128| Loaded 9 DDInter drug-reference entries from /var/folders/38/70vr92w528b6f4zr0wxvd1600000gn/T/appdir-for-unit-tests-4728846297741994847/chartsearchai/loudness-operator.json
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:40,128| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:40,128| Drug-reference data validity — alias-names-another-substance (reported, 2): 2 published name(s) denote a DIFFERENT substance in this dataset, so a question or a chart string carrying one resolves the wrong drug. The data is left as loaded — the fix is in the dataset. [Pfizer-BioNTech Covid-19 Vaccine publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Trastuzumab emtansine publishes 'trastuzumab deruxtecan', which is Trastuzumab deruxtecan's own name]
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:40,128| Drug-reference safety arms over the 9 entr(ies) read from appdata:chartsearchai/loudness-operator.json: doseCeilings=absent (0), handAuthoredRules=absent (0), atcCodes=published (6), interactions=published (5), conditionRules=absent (0)
INFO - DdiDrugReferenceSource.parse(318) |2026-09-07T00:26:40,415| Parsed 2283 DDInter drug-reference entries
INFO - ReferenceDataFiles.loadWithClasspathFallback(228) |2026-09-07T00:26:40,415| Loaded 2283 DDInter drug-reference entries from bundled default /chartsearchai/ddi-knowledge-base.json
INFO - DrugReferenceValidity.logTo(382) |2026-09-07T00:26:40,424| Drug-reference data validity — self-paired-interaction-rows (dropped, 28): 28 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible. (in the dataset the module ships, so the remedy is a data fix upstream rather than a change to this deployment)
INFO - DrugReferenceValidity.logTo(382) |2026-09-07T00:26:40,424| Drug-reference data validity — alias-names-another-substance (reported, 18): 18 published name(s) denote a DIFFERENT substance in this dataset, so a question or a chart string carrying one resolves the wrong drug. The data is left as loaded — the fix is in the dataset. [Omeprazole publishes 'esomeprazole', which is Esomeprazole's own name, Pfizer-BioNTech Covid-19 Vaccine publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Trastuzumab emtansine publishes 'trastuzumab deruxtecan', which is Trastuzumab deruxtecan's own name, Levoketoconazole publishes 'ketoconazole', which is Ketoconazole's own name, Tozinameran (12y+) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Tozinameran (5y-11y) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Tozinameran (6m-4y) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Gallium citrate ga-67 publishes 'gallium nitrate', which is Gallium nitrate's own name] and 10 more (in the dataset the module ships, so the remedy is a data fix upstream rather than a change to this deployment)
INFO - DrugReferenceValidity.logTo(382) |2026-09-07T00:26:40,424| Drug-reference data validity — derivative-merged-with-its-parent-substance (reported, 1): 1 row(s) are keyed as ONE substance with rows their own name says they only DERIVE from, so a chip, a record or a citation about the substance can be named after the derivative and carry its rules. The data is left as loaded — the fix is in the dataset, which has to stop filing the derivative under the parent's substance: in a DDInter-shaped file by giving it its own drugbank_id, which is what keeps every derivative this module already separates apart, and in a curated file by giving it its own substanceName. [Fluoroestradiol f-18 is filed as 'estradiol' beside [Estradiol, Estradiol (topical)]] (in the dataset the module ships, so the remedy is a data fix upstream rather than a change to this deployment)
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:40,425| Drug-reference safety arms over the 2283 entr(ies) read from classpath:/chartsearchai/ddi-knowledge-base.json: doseCeilings=absent (0), handAuthoredRules=absent (0), atcCodes=published (1839), interactions=published (2283), conditionRules=absent (0)
INFO - ReferenceDataFiles.readOperatorFile(351) |2026-09-07T00:26:40,432| DDInter drug-reference entries file 'chartsearchai/loudness-no-such-file.json' not available (Model file not found: /var/folders/38/70vr92w528b6f4zr0wxvd1600000gn/T/appdir-for-unit-tests-4728846297741994847/chartsearchai/loudness-no-such-file.json. Set the correct relative path in chartsearchai.drugReference.dataFilePath); using bundled default
INFO - DdiDrugReferenceSource.parse(318) |2026-09-07T00:26:40,787| Parsed 2283 DDInter drug-reference entries
INFO - ReferenceDataFiles.loadWithClasspathFallback(228) |2026-09-07T00:26:40,788| Loaded 2283 DDInter drug-reference entries from bundled default /chartsearchai/ddi-knowledge-base.json
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:40,797| Drug-reference data validity — configured-data-file-not-read (reported, 1): chartsearchai.drugReference.dataFilePath names 'chartsearchai/loudness-no-such-file.json', which could not be read, so 'classpath:/chartsearchai/ddi-knowledge-base.json' was reached for in its place. Whatever count you see is therefore a count of a dataset nobody configured rather than of your file, however healthy it looks.
INFO - DrugReferenceValidity.logTo(382) |2026-09-07T00:26:40,797| Drug-reference data validity — self-paired-interaction-rows (dropped, 28): 28 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible. (in the dataset the module ships, so the remedy is a data fix upstream rather than a change to this deployment)
INFO - DrugReferenceValidity.logTo(382) |2026-09-07T00:26:40,797| Drug-reference data validity — alias-names-another-substance (reported, 18): 18 published name(s) denote a DIFFERENT substance in this dataset, so a question or a chart string carrying one resolves the wrong drug. The data is left as loaded — the fix is in the dataset. [Omeprazole publishes 'esomeprazole', which is Esomeprazole's own name, Pfizer-BioNTech Covid-19 Vaccine publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Trastuzumab emtansine publishes 'trastuzumab deruxtecan', which is Trastuzumab deruxtecan's own name, Levoketoconazole publishes 'ketoconazole', which is Ketoconazole's own name, Tozinameran (12y+) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Tozinameran (5y-11y) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Tozinameran (6m-4y) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Gallium citrate ga-67 publishes 'gallium nitrate', which is Gallium nitrate's own name] and 10 more (in the dataset the module ships, so the remedy is a data fix upstream rather than a change to this deployment)
INFO - DrugReferenceValidity.logTo(382) |2026-09-07T00:26:40,797| Drug-reference data validity — derivative-merged-with-its-parent-substance (reported, 1): 1 row(s) are keyed as ONE substance with rows their own name says they only DERIVE from, so a chip, a record or a citation about the substance can be named after the derivative and carry its rules. The data is left as loaded — the fix is in the dataset, which has to stop filing the derivative under the parent's substance: in a DDInter-shaped file by giving it its own drugbank_id, which is what keeps every derivative this module already separates apart, and in a curated file by giving it its own substanceName. [Fluoroestradiol f-18 is filed as 'estradiol' beside [Estradiol, Estradiol (topical)]] (in the dataset the module ships, so the remedy is a data fix upstream rather than a change to this deployment)
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:40,797| Drug-reference safety arms over the 2283 entr(ies) read from classpath:/chartsearchai/ddi-knowledge-base.json: doseCeilings=absent (0), handAuthoredRules=absent (0), atcCodes=published (1839), interactions=published (2283), conditionRules=absent (0)
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:41,408| Drug-reference data validity — self-paired-interaction-rows (dropped, 28): 28 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:41,408| Drug-reference data validity — alias-names-another-substance (reported, 18): 18 published name(s) denote a DIFFERENT substance in this dataset, so a question or a chart string carrying one resolves the wrong drug. The data is left as loaded — the fix is in the dataset. [Omeprazole publishes 'esomeprazole', which is Esomeprazole's own name, Pfizer-BioNTech Covid-19 Vaccine publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Trastuzumab emtansine publishes 'trastuzumab deruxtecan', which is Trastuzumab deruxtecan's own name, Levoketoconazole publishes 'ketoconazole', which is Ketoconazole's own name, Tozinameran (12y+) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Tozinameran (5y-11y) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Tozinameran (6m-4y) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Gallium citrate ga-67 publishes 'gallium nitrate', which is Gallium nitrate's own name] and 10 more
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:41,408| Drug-reference data validity — derivative-merged-with-its-parent-substance (reported, 1): 1 row(s) are keyed as ONE substance with rows their own name says they only DERIVE from, so a chip, a record or a citation about the substance can be named after the derivative and carry its rules. The data is left as loaded — the fix is in the dataset, which has to stop filing the derivative under the parent's substance: in a DDInter-shaped file by giving it its own drugbank_id, which is what keeps every derivative this module already separates apart, and in a curated file by giving it its own substanceName. [Fluoroestradiol f-18 is filed as 'estradiol' beside [Estradiol, Estradiol (topical)]]
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.295 s -- in org.openmrs.module.chartsearchai.reference.DrugReferenceFindingLoudnessTest
[INFO] Running org.openmrs.module.chartsearchai.reference.InjectedInteractionRelevanceOrderContextTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.018 s -- in org.openmrs.module.chartsearchai.reference.InjectedInteractionRelevanceOrderContextTest
[INFO] Running org.openmrs.module.chartsearchai.reference.OneOrderNameAcrossAnswerAndChipTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in org.openmrs.module.chartsearchai.reference.OneOrderNameAcrossAnswerAndChipTest
[INFO] Running org.openmrs.module.chartsearchai.reference.HasActiveDrugWholeWordTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 s -- in org.openmrs.module.chartsearchai.reference.HasActiveDrugWholeWordTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DuplicateInteractionChipTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in org.openmrs.module.chartsearchai.reference.DuplicateInteractionChipTest
[INFO] Running org.openmrs.module.chartsearchai.reference.NestedNameDoseTieTest
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.007 s -- in org.openmrs.module.chartsearchai.reference.NestedNameDoseTieTest
[INFO] Running org.openmrs.module.chartsearchai.reference.ActiveOrderReconciliationTest
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:41,449| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Simvastatin Co 20mg [11111111-2222-3333-4444-555555555555]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:41,450| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Simvastatin Co 20mg [11111111-2222-3333-4444-555555555555]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:41,451| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Simvastatin Co 20mg [11111111-2222-3333-4444-555555555555]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:41,454| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [ASA [asa-order-uuid]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:41,455| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Simvastatin Co 20mg [11111111-2222-3333-4444-555555555555]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:41,455| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Simvastatin Co 20mg [11111111-2222-3333-4444-555555555555]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:41,456| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Simvastatin Co 20mg [11111111-2222-3333-4444-555555555555]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:41,458| Active-order reconciliation: 1 of 2 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Simvastatin Co 20mg [11111111-2222-3333-4444-555555555555]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:41,459| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Simvastatin Co 20mg [11111111-2222-3333-4444-555555555555]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:41,461| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Simvastatin Co 20mg [11111111-2222-3333-4444-555555555555]]
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.013 s -- in org.openmrs.module.chartsearchai.reference.ActiveOrderReconciliationTest
[INFO] Running org.openmrs.module.chartsearchai.reference.CurrentMedicationFindingStrengthTest
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.011 s -- in org.openmrs.module.chartsearchai.reference.CurrentMedicationFindingStrengthTest
[INFO] Running org.openmrs.module.chartsearchai.reference.RuleTokenAliasOrderMatchingTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 s -- in org.openmrs.module.chartsearchai.reference.RuleTokenAliasOrderMatchingTest
[INFO] Running org.openmrs.module.chartsearchai.reference.InteractionFindingChartOrderBridgeTest
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:41,479| Active-order reconciliation: 1 of 2 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [[ATC C10AA01] [order-coded]]
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.010 s -- in org.openmrs.module.chartsearchai.reference.InteractionFindingChartOrderBridgeTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DrugReferenceServiceTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in org.openmrs.module.chartsearchai.reference.DrugReferenceServiceTest
[INFO] Running org.openmrs.module.chartsearchai.reference.NameIndexAgreesWithIsNamedTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 6.679 s -- in org.openmrs.module.chartsearchai.reference.NameIndexAgreesWithIsNamedTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DoseCeilingBySubstanceTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in org.openmrs.module.chartsearchai.reference.DoseCeilingBySubstanceTest
[INFO] Running org.openmrs.module.chartsearchai.reference.RecordedAllergenChipNameTest
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:48,176| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:48,177| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:48,178| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:48,180| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:48,182| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.009 s -- in org.openmrs.module.chartsearchai.reference.RecordedAllergenChipNameTest
[INFO] Running org.openmrs.module.chartsearchai.reference.AuthoritativeEndedOrderSubstantiationTest
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:48,185| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Simvastatin Co 20mg [11111111-2222-3333-4444-555555555555]]
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 s -- in org.openmrs.module.chartsearchai.reference.AuthoritativeEndedOrderSubstantiationTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DoseAliasBoundaryTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 s -- in org.openmrs.module.chartsearchai.reference.DoseAliasBoundaryTest
[INFO] Running org.openmrs.module.chartsearchai.reference.ReferenceRecordRowAttributionToggleContextTest
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:48,196| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Ceftriaxone (intramuscular) [o1]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:48,197| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Ceftriaxone (intramuscular) [o1]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:48,202| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Ceftriaxone (intramuscular) [o1]]
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.014 s -- in org.openmrs.module.chartsearchai.reference.ReferenceRecordRowAttributionToggleContextTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DrugReferenceInjectorTest
[INFO] Tests run: 36, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.024 s -- in org.openmrs.module.chartsearchai.reference.DrugReferenceInjectorTest
[INFO] Running org.openmrs.module.chartsearchai.reference.NonCodedDrugOrderNameTest
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:48,281| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [ASPIRIN [e1f95924-697a-11e3-bd76-0800271c1b75]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:48,341| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Warfarin 5mg [e1f95924-697a-11e3-bd76-0800271c1b75]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:48,411| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Warfarin 5mg [99] Allergy: none recorded [e1f95924-697a-11e3-bd76-0800271c1b75]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:48,422| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Warfarin 5mg [e1f95924-697a-11e3-bd76-0800271c1b75]]
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.209 s -- in org.openmrs.module.chartsearchai.reference.NonCodedDrugOrderNameTest
[INFO] Running org.openmrs.module.chartsearchai.reference.CrossReactivityGroupsContextTest
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:48,442| Drug-reference data validity — configured-data-file-not-read (reported, 1): chartsearchai.drugReference.crossReactivityGroupsFilePath names 'chartsearchai/nonexistent-groups.json', which could not be read, so 'classpath:/chartsearchai/cross-reactivity-groups.json' was reached for in its place. Whatever count you see is therefore a count of a dataset nobody configured rather than of your file, however healthy it looks.
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.012 s -- in org.openmrs.module.chartsearchai.reference.CrossReactivityGroupsContextTest
[INFO] Running org.openmrs.module.chartsearchai.reference.JsonDrugReferenceSourceTest
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:48,453| Drug-reference data validity — dataset-missing-a-required-table (reported, 1): a 'json' document must declare [entries]; this one does not, so it parsed to no entries at all. The data is left as loaded — the fix is in the file: either it is not a document of the format its reader expects, or, where a document carrying none of them is intended, the missing table has to be declared empty. Reading it as empty here would load an export truncated before it wrote that table as a complete one.
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 s -- in org.openmrs.module.chartsearchai.reference.JsonDrugReferenceSourceTest
[INFO] Running org.openmrs.module.chartsearchai.reference.ActiveOrderInteractionPhraseTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.030 s -- in org.openmrs.module.chartsearchai.reference.ActiveOrderInteractionPhraseTest
[INFO] Running org.openmrs.module.chartsearchai.reference.InteractionPartnerGroupingTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 s -- in org.openmrs.module.chartsearchai.reference.InteractionPartnerGroupingTest
[INFO] Running org.openmrs.module.chartsearchai.reference.InteractionRouteVariantTest
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:48,486| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:48,487| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:48,488| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:48,489| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:48,490| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:48,490| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:48,491| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:48,491| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:48,492| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.006 s -- in org.openmrs.module.chartsearchai.reference.InteractionRouteVariantTest
[INFO] Running org.openmrs.module.chartsearchai.reference.OrderedSubjectRowTest
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:48,494| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:48,494| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:48,494| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:48,495| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:48,495| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:48,496| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in org.openmrs.module.chartsearchai.reference.OrderedSubjectRowTest
[INFO] Running org.openmrs.module.chartsearchai.reference.QuerystoreDrugOrderDisplayedNameTest
WARN - PersonName.getFullName(417) |2026-09-07T00:26:48,518| No name layout format set
WARN - PersonName.getFullName(417) |2026-09-07T00:26:48,519| No name layout format set
WARN - PersonName.getFullName(417) |2026-09-07T00:26:48,519| No name layout format set
WARN - PersonName.getFullName(417) |2026-09-07T00:26:48,519| No name layout format set
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.023 s -- in org.openmrs.module.chartsearchai.reference.QuerystoreDrugOrderDisplayedNameTest
[INFO] Running org.openmrs.module.chartsearchai.reference.CrossReactivityClassChoiceTest
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 s -- in org.openmrs.module.chartsearchai.reference.CrossReactivityClassChoiceTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DrugReferenceLoadConcurrencyTest
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:48,530| Drug-reference safety arms over the 4 entr(ies) read from none: doseCeilings=published (4), handAuthoredRules=published (4), atcCodes=published (4), interactions=published (4), conditionRules=published (3)
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:48,531| Drug-reference safety arms over the 0 entr(ies) read from none: doseCeilings=absent (0), handAuthoredRules=absent (0), atcCodes=absent (0), interactions=absent (0), conditionRules=absent (0)
WARN - DrugReferenceService.ensureLoaded(1611) |2026-09-07T00:26:48,532| Loaded 0 drug-reference entries — drug-safety checking is INERT: no interaction, allergy or contraindication warning can be raised, and every safety question will answer as though there were nothing to find. chartsearchai.drugReference.sourceFormat=ddinter (parser in use: ddinter), chartsearchai.drugReference.dataFilePath=, read from none. The usual cause is a format/path mismatch: each source format parses only its own shape and returns nothing — without failing — for another's.
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.006 s -- in org.openmrs.module.chartsearchai.reference.DrugReferenceLoadConcurrencyTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DrugReferenceValidityContextTest
INFO - ReferenceDataFiles.readOperatorFile(351) |2026-09-07T00:26:48,539| DDInter drug-reference entries file 'chartsearchai/ddi_knowledge_base.json' not available (Model file not found: /var/folders/38/70vr92w528b6f4zr0wxvd1600000gn/T/appdir-for-unit-tests-4728846297741994847/chartsearchai/ddi_knowledge_base.json. Set the correct relative path in chartsearchai.drugReference.dataFilePath); using bundled default
INFO - DdiDrugReferenceSource.parse(318) |2026-09-07T00:26:48,852| Parsed 2283 DDInter drug-reference entries
INFO - ReferenceDataFiles.loadWithClasspathFallback(228) |2026-09-07T00:26:48,852| Loaded 2283 DDInter drug-reference entries from bundled default /chartsearchai/ddi-knowledge-base.json
INFO - DrugReferenceValidity.logTo(382) |2026-09-07T00:26:48,860| Drug-reference data validity — self-paired-interaction-rows (dropped, 28): 28 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible. (in the dataset the module ships, so the remedy is a data fix upstream rather than a change to this deployment)
INFO - DrugReferenceValidity.logTo(382) |2026-09-07T00:26:48,861| Drug-reference data validity — alias-names-another-substance (reported, 18): 18 published name(s) denote a DIFFERENT substance in this dataset, so a question or a chart string carrying one resolves the wrong drug. The data is left as loaded — the fix is in the dataset. [Omeprazole publishes 'esomeprazole', which is Esomeprazole's own name, Pfizer-BioNTech Covid-19 Vaccine publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Trastuzumab emtansine publishes 'trastuzumab deruxtecan', which is Trastuzumab deruxtecan's own name, Levoketoconazole publishes 'ketoconazole', which is Ketoconazole's own name, Tozinameran (12y+) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Tozinameran (5y-11y) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Tozinameran (6m-4y) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Gallium citrate ga-67 publishes 'gallium nitrate', which is Gallium nitrate's own name] and 10 more (in the dataset the module ships, so the remedy is a data fix upstream rather than a change to this deployment)
INFO - DrugReferenceValidity.logTo(382) |2026-09-07T00:26:48,861| Drug-reference data validity — derivative-merged-with-its-parent-substance (reported, 1): 1 row(s) are keyed as ONE substance with rows their own name says they only DERIVE from, so a chip, a record or a citation about the substance can be named after the derivative and carry its rules. The data is left as loaded — the fix is in the dataset, which has to stop filing the derivative under the parent's substance: in a DDInter-shaped file by giving it its own drugbank_id, which is what keeps every derivative this module already separates apart, and in a curated file by giving it its own substanceName. [Fluoroestradiol f-18 is filed as 'estradiol' beside [Estradiol, Estradiol (topical)]] (in the dataset the module ships, so the remedy is a data fix upstream rather than a change to this deployment)
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:48,862| Drug-reference safety arms over the 2283 entr(ies) read from classpath:/chartsearchai/ddi-knowledge-base.json: doseCeilings=absent (0), handAuthoredRules=absent (0), atcCodes=published (1839), interactions=published (2283), conditionRules=absent (0)
INFO - DdiDrugReferenceSource.parse(318) |2026-09-07T00:26:49,092| Parsed 2283 DDInter drug-reference entries
INFO - ReferenceDataFiles.loadWithClasspathFallback(228) |2026-09-07T00:26:49,092| Loaded 2283 DDInter drug-reference entries from bundled default /chartsearchai/ddi-knowledge-base.json
INFO - DrugReferenceValidity.logTo(382) |2026-09-07T00:26:49,101| Drug-reference data validity — self-paired-interaction-rows (dropped, 28): 28 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible. (in the dataset the module ships, so the remedy is a data fix upstream rather than a change to this deployment)
INFO - DrugReferenceValidity.logTo(382) |2026-09-07T00:26:49,101| Drug-reference data validity — alias-names-another-substance (reported, 18): 18 published name(s) denote a DIFFERENT substance in this dataset, so a question or a chart string carrying one resolves the wrong drug. The data is left as loaded — the fix is in the dataset. [Omeprazole publishes 'esomeprazole', which is Esomeprazole's own name, Pfizer-BioNTech Covid-19 Vaccine publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Trastuzumab emtansine publishes 'trastuzumab deruxtecan', which is Trastuzumab deruxtecan's own name, Levoketoconazole publishes 'ketoconazole', which is Ketoconazole's own name, Tozinameran (12y+) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Tozinameran (5y-11y) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Tozinameran (6m-4y) publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Gallium citrate ga-67 publishes 'gallium nitrate', which is Gallium nitrate's own name] and 10 more (in the dataset the module ships, so the remedy is a data fix upstream rather than a change to this deployment)
INFO - DrugReferenceValidity.logTo(382) |2026-09-07T00:26:49,101| Drug-reference data validity — derivative-merged-with-its-parent-substance (reported, 1): 1 row(s) are keyed as ONE substance with rows their own name says they only DERIVE from, so a chip, a record or a citation about the substance can be named after the derivative and carry its rules. The data is left as loaded — the fix is in the dataset, which has to stop filing the derivative under the parent's substance: in a DDInter-shaped file by giving it its own drugbank_id, which is what keeps every derivative this module already separates apart, and in a curated file by giving it its own substanceName. [Fluoroestradiol f-18 is filed as 'estradiol' beside [Estradiol, Estradiol (topical)]] (in the dataset the module ships, so the remedy is a data fix upstream rather than a change to this deployment)
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:49,102| Drug-reference safety arms over the 2283 entr(ies) read from classpath:/chartsearchai/ddi-knowledge-base.json: doseCeilings=absent (0), handAuthoredRules=absent (0), atcCodes=published (1839), interactions=published (2283), conditionRules=absent (0)
INFO - ReferenceDataFiles.readOperatorFile(351) |2026-09-07T00:26:49,111| drug-reference entries file 'chartsearchai/h156-absent-drug-reference.json' not available (Model file not found: /var/folders/38/70vr92w528b6f4zr0wxvd1600000gn/T/appdir-for-unit-tests-4728846297741994847/chartsearchai/h156-absent-drug-reference.json. Set the correct relative path in chartsearchai.drugReference.dataFilePath); using bundled default
INFO - ReferenceDataFiles.loadWithClasspathFallback(228) |2026-09-07T00:26:49,112| Loaded 4 drug-reference entries from bundled default /chartsearchai/drug-reference.json
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,112| Drug-reference data validity — configured-data-file-not-read (reported, 1): chartsearchai.drugReference.dataFilePath names 'chartsearchai/h156-absent-drug-reference.json', which could not be read, so 'classpath:/chartsearchai/drug-reference.json' was reached for in its place. Whatever count you see is therefore a count of a dataset nobody configured rather than of your file, however healthy it looks.
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:49,112| Drug-reference safety arms over the 4 entr(ies) read from classpath:/chartsearchai/drug-reference.json: doseCeilings=published (4), handAuthoredRules=published (4), atcCodes=published (4), interactions=published (4), conditionRules=published (3)
INFO - ReferenceDataFiles.readOperatorFile(344) |2026-09-07T00:26:49,124| Loaded 2 drug-reference entries from /var/folders/38/70vr92w528b6f4zr0wxvd1600000gn/T/appdir-for-unit-tests-4728846297741994847/chartsearchai/h288-null-interaction.json
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,124| Drug-reference data validity — null-list-element (dropped, 4): 4 null element(s) inside an entry's own lists were dropped: a null is not a value, and the consumers of an alias, a code, an age band or a rule dereference it — the safety arms then raise no warning at all for the request, rather than one fewer. The fix is in the file. Entries: [Ibuprofen tablets 400mg, Ibuprofen]
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:49,124| Drug-reference safety arms over the 2 entr(ies) read from appdata:chartsearchai/h288-null-interaction.json: doseCeilings=absent (0), handAuthoredRules=absent (0), atcCodes=published (1), interactions=absent (0), conditionRules=absent (0)
INFO - DdiDrugReferenceSource.parse(318) |2026-09-07T00:26:49,421| Parsed 14 DDInter drug-reference entries
INFO - ReferenceDataFiles.readOperatorFile(344) |2026-09-07T00:26:49,421| Loaded 14 DDInter drug-reference entries from /var/folders/38/70vr92w528b6f4zr0wxvd1600000gn/T/appdir-for-unit-tests-4728846297741994847/chartsearchai/h196-item4-slice.json
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,421| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,421| Drug-reference data validity — alias-names-another-substance (reported, 1): 1 published name(s) denote a DIFFERENT substance in this dataset, so a question or a chart string carrying one resolves the wrong drug. The data is left as loaded — the fix is in the dataset. [Levoketoconazole publishes 'ketoconazole', which is Ketoconazole's own name]
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,421| Drug-reference data validity — derivative-merged-with-its-parent-substance (reported, 1): 1 row(s) are keyed as ONE substance with rows their own name says they only DERIVE from, so a chip, a record or a citation about the substance can be named after the derivative and carry its rules. The data is left as loaded — the fix is in the dataset, which has to stop filing the derivative under the parent's substance: in a DDInter-shaped file by giving it its own drugbank_id, which is what keeps every derivative this module already separates apart, and in a curated file by giving it its own substanceName. [Fluoroestradiol f-18 is filed as 'estradiol' beside [Estradiol, Estradiol (topical)]]
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:49,422| Drug-reference safety arms over the 14 entr(ies) read from appdata:chartsearchai/h196-item4-slice.json: doseCeilings=absent (0), handAuthoredRules=absent (0), atcCodes=published (9), interactions=published (12), conditionRules=absent (0)
INFO - ReferenceDataFiles.readOperatorFile(344) |2026-09-07T00:26:49,432| Loaded 3 drug-reference entries from /var/folders/38/70vr92w528b6f4zr0wxvd1600000gn/T/appdir-for-unit-tests-4728846297741994847/chartsearchai/h211-declared.json
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:49,432| Drug-reference safety arms over the 3 entr(ies) read from appdata:chartsearchai/h211-declared.json: doseCeilings=absent (0), handAuthoredRules=published (2), atcCodes=published (3), interactions=absent (0), conditionRules=absent (0)
INFO - DdiDrugReferenceSource.parse(318) |2026-09-07T00:26:49,441| Parsed 9 DDInter drug-reference entries
INFO - ReferenceDataFiles.readOperatorFile(344) |2026-09-07T00:26:49,442| Loaded 9 DDInter drug-reference entries from /var/folders/38/70vr92w528b6f4zr0wxvd1600000gn/T/appdir-for-unit-tests-4728846297741994847/chartsearchai/h196-slice.json
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,442| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,442| Drug-reference data validity — alias-names-another-substance (reported, 2): 2 published name(s) denote a DIFFERENT substance in this dataset, so a question or a chart string carrying one resolves the wrong drug. The data is left as loaded — the fix is in the dataset. [Pfizer-BioNTech Covid-19 Vaccine publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Trastuzumab emtansine publishes 'trastuzumab deruxtecan', which is Trastuzumab deruxtecan's own name]
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:49,442| Drug-reference safety arms over the 9 entr(ies) read from appdata:chartsearchai/h196-slice.json: doseCeilings=absent (0), handAuthoredRules=absent (0), atcCodes=published (6), interactions=published (5), conditionRules=absent (0)
INFO - ReferenceDataFiles.readOperatorFile(344) |2026-09-07T00:26:49,451| Loaded 1 drug-reference entries from /var/folders/38/70vr92w528b6f4zr0wxvd1600000gn/T/appdir-for-unit-tests-4728846297741994847/chartsearchai/h288-null-element.json
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,451| Drug-reference data validity — null-list-element (dropped, 2): 2 null element(s) inside an entry's own lists were dropped: a null is not a value, and the consumers of an alias, a code, an age band or a rule dereference it — the safety arms then raise no warning at all for the request, rather than one fewer. The fix is in the file. Entries: [Ibuprofen]
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:49,452| Drug-reference safety arms over the 1 entr(ies) read from appdata:chartsearchai/h288-null-element.json: doseCeilings=absent (0), handAuthoredRules=absent (0), atcCodes=published (1), interactions=absent (0), conditionRules=absent (0)
INFO - ReferenceDataFiles.readOperatorFile(344) |2026-09-07T00:26:49,462| Loaded 2 drug-reference entries from /var/folders/38/70vr92w528b6f4zr0wxvd1600000gn/T/appdir-for-unit-tests-4728846297741994847/chartsearchai/h150-blank-alias.json
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,462| Drug-reference data validity — blank-alias (dropped, 1): 1 alias(es) naming nothing (blank, or nothing but combining marks) were dropped: such a token matches at a word boundary in text it has nothing to do with, so the entry's rules would fire for a patient with an unrelated allergy. Entries: [Warfarin]
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:49,462| Drug-reference safety arms over the 2 entr(ies) read from appdata:chartsearchai/h150-blank-alias.json: doseCeilings=absent (0), handAuthoredRules=published (2), atcCodes=published (2), interactions=absent (0), conditionRules=absent (0)
INFO - ReferenceDataFiles.readOperatorFile(344) |2026-09-07T00:26:49,471| Loaded 0 DDInter drug-reference entries from /var/folders/38/70vr92w528b6f4zr0wxvd1600000gn/T/appdir-for-unit-tests-4728846297741994847/chartsearchai/h242-wrong-parser.json
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,471| Drug-reference data validity — dataset-missing-a-required-table (reported, 2): a 'ddinter' document must declare [drugs, interactions]; this one does not, so it parsed to no entries at all. The data is left as loaded — the fix is in the file: either it is not a document of the format its reader expects, or, where a document carrying none of them is intended, the missing table has to be declared empty. Reading it as empty here would load an export truncated before it wrote that table as a complete one.
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:49,471| Drug-reference safety arms over the 0 entr(ies) read from appdata:chartsearchai/h242-wrong-parser.json: doseCeilings=absent (0), handAuthoredRules=absent (0), atcCodes=absent (0), interactions=absent (0), conditionRules=absent (0)
WARN - DrugReferenceService.ensureLoaded(1611) |2026-09-07T00:26:49,471| Loaded 0 drug-reference entries — drug-safety checking is INERT: no interaction, allergy or contraindication warning can be raised, and every safety question will answer as though there were nothing to find. chartsearchai.drugReference.sourceFormat=ddinter (parser in use: ddinter), chartsearchai.drugReference.dataFilePath=chartsearchai/h242-wrong-parser.json, read from appdata:chartsearchai/h242-wrong-parser.json. The usual cause is a format/path mismatch: each source format parses only its own shape and returns nothing — without failing — for another's.
INFO - DdiDrugReferenceSource.parse(318) |2026-09-07T00:26:49,480| Parsed 8 DDInter drug-reference entries
INFO - ReferenceDataFiles.readOperatorFile(344) |2026-09-07T00:26:49,480| Loaded 8 DDInter drug-reference entries from /var/folders/38/70vr92w528b6f4zr0wxvd1600000gn/T/appdir-for-unit-tests-4728846297741994847/chartsearchai/h196-item4-edges.json
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,480| Drug-reference data validity — blank-alias (dropped, 2): 2 alias(es) naming nothing (blank, or nothing but combining marks) were dropped: such a token matches at a word boundary in text it has nothing to do with, so the entry's rules would fire for a patient with an unrelated allergy. Entries: [Bupivacaine hcl-2, Marcaine]
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,481| Drug-reference data validity — alias-names-another-substance (reported, 2): 2 published name(s) denote a DIFFERENT substance in this dataset, so a question or a chart string carrying one resolves the wrong drug. The data is left as loaded — the fix is in the dataset. [Levoketoconazole publishes 'ketoconazole', which is Ketoconazole's own name, Levoketoconazole (oral) publishes 'ketoconazole', which is Ketoconazole's own name]
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,481| Drug-reference data validity — derivative-merged-with-its-parent-substance (reported, 1): 1 row(s) are keyed as ONE substance with rows their own name says they only DERIVE from, so a chip, a record or a citation about the substance can be named after the derivative and carry its rules. The data is left as loaded — the fix is in the dataset, which has to stop filing the derivative under the parent's substance: in a DDInter-shaped file by giving it its own drugbank_id, which is what keeps every derivative this module already separates apart, and in a curated file by giving it its own substanceName. [Fluoroestradiól f-18 is filed as 'estradiol' beside [Estradiol]]
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:49,481| Drug-reference safety arms over the 8 entr(ies) read from appdata:chartsearchai/h196-item4-edges.json: doseCeilings=absent (0), handAuthoredRules=absent (0), atcCodes=published (6), interactions=published (8), conditionRules=absent (0)
INFO - DdiDrugReferenceSource.parse(318) |2026-09-07T00:26:49,494| Parsed 3 DDInter drug-reference entries
INFO - ReferenceDataFiles.readOperatorFile(344) |2026-09-07T00:26:49,494| Loaded 3 DDInter drug-reference entries from /var/folders/38/70vr92w528b6f4zr0wxvd1600000gn/T/appdir-for-unit-tests-4728846297741994847/chartsearchai/h242-empty-table.json
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:49,494| Drug-reference safety arms over the 3 entr(ies) read from appdata:chartsearchai/h242-empty-table.json: doseCeilings=absent (0), handAuthoredRules=absent (0), atcCodes=published (3), interactions=absent (0), conditionRules=absent (0)
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,503| Drug-reference data validity — null-list-element (dropped, 4): 4 null element(s) inside an entry's own lists were dropped: a null is not a value, and the consumers of an alias, a code, an age band or a rule dereference it — the safety arms then raise no warning at all for the request, rather than one fewer. The fix is in the file. Entries: [Ibuprofen tablets 400mg, Ibuprofen]
WARN - JsonDrugReferenceSource.parse(127) |2026-09-07T00:26:49,515| Dropped 2 unusable drug-reference entries (blank id or name)
INFO - ReferenceDataFiles.readOperatorFile(344) |2026-09-07T00:26:49,524| Loaded 3 drug-reference entries from /var/folders/38/70vr92w528b6f4zr0wxvd1600000gn/T/appdir-for-unit-tests-4728846297741994847/chartsearchai/h211-stem-only.json
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,525| Drug-reference data validity — entry-not-named-by-its-own-aliases (repaired, 3): 3 entr(ies) whose aliases omitted their own name were given it, so the strongest claimant on a name is always among the entries that name matches. Entries: [Ibuprofen tablets, Ibuprofen suspension, Ibuprofen]
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,525| Drug-reference data validity — rules-without-a-substance-identity (reported, 2): 2 rule-bearing entr(ies) share a published name with another entry while declaring no substanceName, so each is its own substance and only the strongest claimant on that name is put in play — the others' rules are silently dropped. Fix: declare substanceName on the rows that are one substance AND name a presentation with a trailing parenthesized qualifier ('Ibuprofen (tablets)', not 'Ibuprofen tablets'), which is what the shipped datasets do; the claim alone is vetoed by the display stem. [Ibuprofen tablets shares 'ibuprofen' with [Ibuprofen tablets, Ibuprofen suspension, Ibuprofen], Ibuprofen suspension shares 'ibuprofen' with [Ibuprofen tablets, Ibuprofen suspension, Ibuprofen]]
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:49,525| Drug-reference safety arms over the 3 entr(ies) read from appdata:chartsearchai/h211-stem-only.json: doseCeilings=absent (0), handAuthoredRules=published (2), atcCodes=published (3), interactions=absent (0), conditionRules=absent (0)
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,534| Drug-reference data validity — entry-not-named-by-its-own-aliases (repaired, 3): 3 entr(ies) whose aliases omitted their own name were given it, so the strongest claimant on a name is always among the entries that name matches. Entries: [Ibuprofen tablets, Ibuprofen suspension, Ibuprofen]
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,534| Drug-reference data validity — rules-without-a-substance-identity (reported, 2): 2 rule-bearing entr(ies) share a published name with another entry while declaring no substanceName, so each is its own substance and only the strongest claimant on that name is put in play — the others' rules are silently dropped. Fix: declare substanceName on the rows that are one substance AND name a presentation with a trailing parenthesized qualifier ('Ibuprofen (tablets)', not 'Ibuprofen tablets'), which is what the shipped datasets do; the claim alone is vetoed by the display stem. [Ibuprofen tablets shares 'ibuprofen' with [Ibuprofen tablets, Ibuprofen suspension, Ibuprofen], Ibuprofen suspension shares 'ibuprofen' with [Ibuprofen tablets, Ibuprofen suspension, Ibuprofen]]
INFO - ReferenceDataFiles.readOperatorFile(344) |2026-09-07T00:26:49,543| Loaded 3 drug-reference entries from /var/folders/38/70vr92w528b6f4zr0wxvd1600000gn/T/appdir-for-unit-tests-4728846297741994847/chartsearchai/h156-typo.json
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,544| Drug-reference data validity — configured-source-format-not-used (reported, 1): chartsearchai.drugReference.sourceFormat is 'jsonn', which matches no adapter, so the 'json' parser is in force instead.
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:49,544| Drug-reference safety arms over the 3 entr(ies) read from appdata:chartsearchai/h156-typo.json: doseCeilings=absent (0), handAuthoredRules=published (2), atcCodes=published (3), interactions=absent (0), conditionRules=absent (0)
INFO - ReferenceDataFiles.readOperatorFile(344) |2026-09-07T00:26:49,553| Loaded 0 drug-reference entries from /var/folders/38/70vr92w528b6f4zr0wxvd1600000gn/T/appdir-for-unit-tests-4728846297741994847/chartsearchai/h242-curated-parser.json
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,553| Drug-reference data validity — dataset-missing-a-required-table (reported, 1): a 'json' document must declare [entries]; this one does not, so it parsed to no entries at all. The data is left as loaded — the fix is in the file: either it is not a document of the format its reader expects, or, where a document carrying none of them is intended, the missing table has to be declared empty. Reading it as empty here would load an export truncated before it wrote that table as a complete one.
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:49,553| Drug-reference safety arms over the 0 entr(ies) read from appdata:chartsearchai/h242-curated-parser.json: doseCeilings=absent (0), handAuthoredRules=absent (0), atcCodes=absent (0), interactions=absent (0), conditionRules=absent (0)
WARN - DrugReferenceService.ensureLoaded(1611) |2026-09-07T00:26:49,553| Loaded 0 drug-reference entries — drug-safety checking is INERT: no interaction, allergy or contraindication warning can be raised, and every safety question will answer as though there were nothing to find. chartsearchai.drugReference.sourceFormat=json (parser in use: json), chartsearchai.drugReference.dataFilePath=chartsearchai/h242-curated-parser.json, read from appdata:chartsearchai/h242-curated-parser.json. The usual cause is a format/path mismatch: each source format parses only its own shape and returns nothing — without failing — for another's.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,563| Drug-reference data validity — blank-alias (dropped, 1): 1 alias(es) naming nothing (blank, or nothing but combining marks) were dropped: such a token matches at a word boundary in text it has nothing to do with, so the entry's rules would fire for a patient with an unrelated allergy. Entries: [́]
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,563| Drug-reference data validity — entry-not-named-by-its-own-aliases (reported, 2): 2 entr(ies) whose display NAME names nothing (blank once folded, or nothing but combining marks or punctuation) were left unnamed rather than repaired: giving such an entry its own name as an alias would put back exactly what the blank-alias rule drops, and would leave it answering isNamed for a string nothing can match. Its own NAME then reaches no name-driven arm, so the entry is findable only by whatever other aliases it carries and by nothing at all where it carries none; fix the name in the file. Entries: [́, ---]
INFO - DdiDrugReferenceSource.parse(318) |2026-09-07T00:26:49,572| Parsed 6 DDInter drug-reference entries
INFO - ReferenceDataFiles.readOperatorFile(344) |2026-09-07T00:26:49,573| Loaded 6 DDInter drug-reference entries from /var/folders/38/70vr92w528b6f4zr0wxvd1600000gn/T/appdir-for-unit-tests-4728846297741994847/chartsearchai/h152-self-paired.json
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,573| Drug-reference data validity — self-paired-interaction-rows (dropped, 3): 3 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:49,573| Drug-reference safety arms over the 6 entr(ies) read from appdata:chartsearchai/h152-self-paired.json: doseCeilings=absent (0), handAuthoredRules=absent (0), atcCodes=published (5), interactions=published (6), conditionRules=absent (0)
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,582| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,582| Drug-reference data validity — alias-names-another-substance (reported, 2): 2 published name(s) denote a DIFFERENT substance in this dataset, so a question or a chart string carrying one resolves the wrong drug. The data is left as loaded — the fix is in the dataset. [Pfizer-BioNTech Covid-19 Vaccine publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Trastuzumab emtansine publishes 'trastuzumab deruxtecan', which is Trastuzumab deruxtecan's own name]
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,588| Drug-reference data validity — dataset-missing-a-required-table (reported, 1): a 'json' document must declare [entries]; this one does not, so it parsed to no entries at all. The data is left as loaded — the fix is in the file: either it is not a document of the format its reader expects, or, where a document carrying none of them is intended, the missing table has to be declared empty. Reading it as empty here would load an export truncated before it wrote that table as a complete one.
WARN - DrugReferenceService.ensureLoaded(1611) |2026-09-07T00:26:49,588| Loaded 0 drug-reference entries — drug-safety checking is INERT: no interaction, allergy or contraindication warning can be raised, and every safety question will answer as though there were nothing to find. chartsearchai.drugReference.sourceFormat=json (parser in use: json), chartsearchai.drugReference.dataFilePath=chartsearchai/h156-honoured.json, read from appdata:chartsearchai/h156-honoured.json. The usual cause is a format/path mismatch: each source format parses only its own shape and returns nothing — without failing — for another's.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,595| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,595| Drug-reference data validity — alias-names-another-substance (reported, 2): 2 published name(s) denote a DIFFERENT substance in this dataset, so a question or a chart string carrying one resolves the wrong drug. The data is left as loaded — the fix is in the dataset. [Pfizer-BioNTech Covid-19 Vaccine publishes 'moderna covid-19 vaccine', which is Moderna covid-19 vaccine's own name, Trastuzumab emtansine publishes 'trastuzumab deruxtecan', which is Trastuzumab deruxtecan's own name]
INFO - ReferenceDataFiles.readOperatorFile(344) |2026-09-07T00:26:49,604| Loaded 0 DDInter drug-reference entries from /var/folders/38/70vr92w528b6f4zr0wxvd1600000gn/T/appdir-for-unit-tests-4728846297741994847/chartsearchai/h242-no-table.json
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,604| Drug-reference data validity — dataset-missing-a-required-table (reported, 1): a 'ddinter' document must declare [interactions]; this one does not, so it parsed to no entries at all, discarding the 3 row(s) it does carry. The data is left as loaded — the fix is in the file: either it is not a document of the format its reader expects, or, where a document carrying none of them is intended, the missing table has to be declared empty. Reading it as empty here would load an export truncated before it wrote that table as a complete one.
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:49,604| Drug-reference safety arms over the 0 entr(ies) read from appdata:chartsearchai/h242-no-table.json: doseCeilings=absent (0), handAuthoredRules=absent (0), atcCodes=absent (0), interactions=absent (0), conditionRules=absent (0)
WARN - DrugReferenceService.ensureLoaded(1611) |2026-09-07T00:26:49,604| Loaded 0 drug-reference entries — drug-safety checking is INERT: no interaction, allergy or contraindication warning can be raised, and every safety question will answer as though there were nothing to find. chartsearchai.drugReference.sourceFormat=ddinter (parser in use: ddinter), chartsearchai.drugReference.dataFilePath=chartsearchai/h242-no-table.json, read from appdata:chartsearchai/h242-no-table.json. The usual cause is a format/path mismatch: each source format parses only its own shape and returns nothing — without failing — for another's.
[INFO] Tests run: 23, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.073 s -- in org.openmrs.module.chartsearchai.reference.DrugReferenceValidityContextTest
[INFO] Running org.openmrs.module.chartsearchai.reference.ActiveOrderAtcAttributionTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in org.openmrs.module.chartsearchai.reference.ActiveOrderAtcAttributionTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DrugSafetyInteractionScreeningTest
WARN - DrugSafetyValidator.addActiveOrderPairInteractions(6366) |2026-09-07T00:26:49,615| Interaction screening across 6 active-order reference entries found 15 pair(s) above the severity floor; reporting the 10 most severe and WITHHOLDING 5: Simvastatin x Ciprofloxacin (Moderate); Ciprofloxacin x Clarithromycin (Moderate); Ciprofloxacin x Fluconazole (Moderate); Simvastatin x Warfarin (Minor); Clarithromycin x Fluconazole (Minor)
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,618| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,621| Drug-reference data validity — self-paired-interaction-rows (dropped, 1): 1 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
[INFO] Tests run: 28, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.012 s -- in org.openmrs.module.chartsearchai.reference.DrugSafetyInteractionScreeningTest
[INFO] Running org.openmrs.module.chartsearchai.reference.SelfNamedAllergyRuleFoldTest
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 s -- in org.openmrs.module.chartsearchai.reference.SelfNamedAllergyRuleFoldTest
[INFO] Running org.openmrs.module.chartsearchai.reference.AtcLoadValidityChannelTest
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,638| Drug-reference data validity — no-line-yielded-an-entry (reported, 1): this 'atc' document was read and none of its 3 content line(s) produced an entry, so the entry count of 0 describes a document whose content was discarded rather than an empty file. The data is left as loaded — the fix is in the file: the likeliest cause is a document of another format, since a line this parser cannot read is skipped rather than refused. A document carrying no content lines at all reports nothing here, which is what separates the two.
WARN - DrugReferenceService.ensureLoaded(1611) |2026-09-07T00:26:49,638| Loaded 0 drug-reference entries — drug-safety checking is INERT: no interaction, allergy or contraindication warning can be raised, and every safety question will answer as though there were nothing to find. chartsearchai.drugReference.sourceFormat=atc (parser in use: atc), chartsearchai.drugReference.dataFilePath=chartsearchai/h266-atc-unsplittable.tsv, read from appdata:chartsearchai/h266-atc-unsplittable.tsv. The usual cause is a format/path mismatch: each source format parses only its own shape and returns nothing — without failing — for another's.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,659| Drug-reference data validity — no-line-yielded-an-entry (reported, 1): this 'atc' document was read and none of its 1960 content line(s) produced an entry, so the entry count of 0 describes a document whose content was discarded rather than an empty file. The data is left as loaded — the fix is in the file: the likeliest cause is a document of another format, since a line this parser cannot read is skipped rather than refused. A document carrying no content lines at all reports nothing here, which is what separates the two.
WARN - DrugReferenceService.ensureLoaded(1611) |2026-09-07T00:26:49,659| Loaded 0 drug-reference entries — drug-safety checking is INERT: no interaction, allergy or contraindication warning can be raised, and every safety question will answer as though there were nothing to find. chartsearchai.drugReference.sourceFormat=atc (parser in use: atc), chartsearchai.drugReference.dataFilePath=chartsearchai/h266-not-an-atc-export.json, read from appdata:chartsearchai/h266-not-an-atc-export.json. The usual cause is a format/path mismatch: each source format parses only its own shape and returns nothing — without failing — for another's.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,663| Drug-reference data validity — configured-data-file-not-read (reported, 1): chartsearchai.drugReference.dataFilePath names 'chartsearchai/h266-no-such-atc-export.tsv', which could not be read, and nothing was read in its place, so no dataset is in force at all. A count of 0 here is a file that was named and not found, which is not the same state as a deployment that configured nothing.
WARN - DrugReferenceService.ensureLoaded(1611) |2026-09-07T00:26:49,664| Loaded 0 drug-reference entries — drug-safety checking is INERT: no interaction, allergy or contraindication warning can be raised, and every safety question will answer as though there were nothing to find. chartsearchai.drugReference.sourceFormat=atc (parser in use: atc), chartsearchai.drugReference.dataFilePath=chartsearchai/h266-no-such-atc-export.tsv, read from none. The usual cause is a format/path mismatch: each source format parses only its own shape and returns nothing — without failing — for another's.
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,668| Drug-reference data validity — configured-data-file-not-read (reported, 1): chartsearchai.drugReference.dataFilePath names 'chartsearchai/h266-no-such-atc-export.tsv', which could not be read, and nothing was read in its place, so no dataset is in force at all. A count of 0 here is a file that was named and not found, which is not the same state as a deployment that configured nothing.
WARN - DrugReferenceService.ensureLoaded(1611) |2026-09-07T00:26:49,668| Loaded 0 drug-reference entries — drug-safety checking is INERT: no interaction, allergy or contraindication warning can be raised, and every safety question will answer as though there were nothing to find. chartsearchai.drugReference.sourceFormat=atc (parser in use: atc), chartsearchai.drugReference.dataFilePath=chartsearchai/h266-no-such-atc-export.tsv, read from none. The usual cause is a format/path mismatch: each source format parses only its own shape and returns nothing — without failing — for another's.
WARN - DrugReferenceService.ensureLoaded(1611) |2026-09-07T00:26:49,676| Loaded 0 drug-reference entries — drug-safety checking is INERT: no interaction, allergy or contraindication warning can be raised, and every safety question will answer as though there were nothing to find. chartsearchai.drugReference.sourceFormat=atc (parser in use: atc), chartsearchai.drugReference.dataFilePath=chartsearchai/h266-empty-atc-export.tsv, read from appdata:chartsearchai/h266-empty-atc-export.tsv. The usual cause is a format/path mismatch: each source format parses only its own shape and returns nothing — without failing — for another's.
INFO - ReferenceDataFiles.readOperatorFile(351) |2026-09-07T00:26:49,681| ATC drug-reference entries file 'chartsearchai/h266-no-such-atc-export.tsv' not available (Model file not found: /var/folders/38/70vr92w528b6f4zr0wxvd1600000gn/T/appdir-for-unit-tests-4728846297741994847/chartsearchai/h266-no-such-atc-export.tsv. Set the correct relative path in chartsearchai.drugReference.dataFilePath); running empty
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:49,681| Drug-reference data validity — configured-data-file-not-read (reported, 1): chartsearchai.drugReference.dataFilePath names 'chartsearchai/h266-no-such-atc-export.tsv', which could not be read, and nothing was read in its place, so no dataset is in force at all. A count of 0 here is a file that was named and not found, which is not the same state as a deployment that configured nothing.
INFO - DrugReferenceService.ensureLoaded(1601) |2026-09-07T00:26:49,681| Drug-reference safety arms over the 0 entr(ies) read from none: doseCeilings=absent (0), handAuthoredRules=absent (0), atcCodes=absent (0), interactions=absent (0), conditionRules=absent (0)
WARN - DrugReferenceService.ensureLoaded(1611) |2026-09-07T00:26:49,681| Loaded 0 drug-reference entries — drug-safety checking is INERT: no interaction, allergy or contraindication warning can be raised, and every safety question will answer as though there were nothing to find. chartsearchai.drugReference.sourceFormat=atc (parser in use: atc), chartsearchai.drugReference.dataFilePath=chartsearchai/h266-no-such-atc-export.tsv, read from none. The usual cause is a format/path mismatch: each source format parses only its own shape and returns nothing — without failing — for another's.
WARN - DrugReferenceService.ensureLoaded(1611) |2026-09-07T00:26:49,686| Loaded 0 drug-reference entries — drug-safety checking is INERT: no interaction, allergy or contraindication warning can be raised, and every safety question will answer as though there were nothing to find. chartsearchai.drugReference.sourceFormat=atc (parser in use: atc), chartsearchai.drugReference.dataFilePath=chartsearchai/ddi_knowledge_base.json, read from none. The usual cause is a format/path mismatch: each source format parses only its own shape and returns nothing — without failing — for another's.
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.059 s -- in org.openmrs.module.chartsearchai.reference.AtcLoadValidityChannelTest
[INFO] Running org.openmrs.module.chartsearchai.reference.SubjectMatterScopedContraindicationTest
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.010 s -- in org.openmrs.module.chartsearchai.reference.SubjectMatterScopedContraindicationTest
[INFO] Running org.openmrs.module.chartsearchai.reference.ConditionRuleBoundaryCorroborationTest
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.007 s -- in org.openmrs.module.chartsearchai.reference.ConditionRuleBoundaryCorroborationTest
[INFO] Running org.openmrs.module.chartsearchai.reference.AtcDrugReferenceSourceTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 s -- in org.openmrs.module.chartsearchai.reference.AtcDrugReferenceSourceTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DrugSafetyDiacriticOrderNameTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.006 s -- in org.openmrs.module.chartsearchai.reference.DrugSafetyDiacriticOrderNameTest
[INFO] Running org.openmrs.module.chartsearchai.reference.UnclassifyingAtcVetoSetTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.271 s -- in org.openmrs.module.chartsearchai.reference.UnclassifyingAtcVetoSetTest
[INFO] Running org.openmrs.module.chartsearchai.reference.FoldedChipOnePartnerNameTest
[INFO] Tests run: 20, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.010 s -- in org.openmrs.module.chartsearchai.reference.FoldedChipOnePartnerNameTest
[INFO] Running org.openmrs.module.chartsearchai.reference.OneNameAcrossChipAndInjectedRecordTest
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:49,995| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Naproxen 500mg [order-uuid-1]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:49,996| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Naproxen 500mg [order-naproxen]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:49,998| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Warfarin 5mg [order-uuid-1]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:49,999| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [[ATC A01AD05, B01AC06, N02BA01] [order-nameless]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:50,000| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Aspirin 81mg [order-uuid-1]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:50,000| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Omeprazole 20mg [order-uuid-1]]
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:50,001| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Aspirin 81mg [order-uuid-1]]
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.006 s -- in org.openmrs.module.chartsearchai.reference.OneNameAcrossChipAndInjectedRecordTest
[INFO] Running org.openmrs.module.chartsearchai.reference.AtcCrossReactivityLicensingTest
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 s -- in org.openmrs.module.chartsearchai.reference.AtcCrossReactivityLicensingTest
[INFO] Running org.openmrs.module.chartsearchai.reference.FoldedOperandTest
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.353 s -- in org.openmrs.module.chartsearchai.reference.FoldedOperandTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DrugInPlayFindingStrengthOrderTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 s -- in org.openmrs.module.chartsearchai.reference.DrugInPlayFindingStrengthOrderTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DrugSafetyChipLabelTest
WARN - DrugReferenceValidity.logTo(386) |2026-09-07T00:26:50,912| Drug-reference data validity — self-paired-interaction-rows (dropped, 2): 2 interaction row(s) pair a drug with itself or with another route/formulation row of the same substance, and a drug cannot interact with itself, so they were dropped. The fix is in the dataset; the count is how a refresh introducing more of them becomes visible.
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.837 s -- in org.openmrs.module.chartsearchai.reference.DrugSafetyChipLabelTest
[INFO] Running org.openmrs.module.chartsearchai.reference.PerRequestSubstanceSubjectTest
WARN - DrugReferenceInjector.unrepresentedActiveOrders(797) |2026-09-07T00:26:51,526| Active-order reconciliation: 1 of 1 active drug order(s) have no drug-order record in the retrieved chart, so the answer could deny a medication the drug-safety chips name; injecting them as records. The chart is built from the querystore index, so this normally means that index is behind the OrderService read (querystore owns indexing). Unrepresented: [Warfarin [order-Warfarin]]
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.329 s -- in org.openmrs.module.chartsearchai.reference.PerRequestSubstanceSubjectTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DrugClassNoteInjectionToggleTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.010 s -- in org.openmrs.module.chartsearchai.reference.DrugClassNoteInjectionToggleTest
[INFO] Running org.openmrs.module.chartsearchai.reference.DoseCeilingAttributionTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in org.openmrs.module.chartsearchai.reference.DoseCeilingAttributionTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 1894, Failures: 0, Errors: 0, Skipped: 57
[INFO] 
[INFO] 
[INFO] --- jar:3.4.1:jar (default-jar) @ chartsearchai-api ---
[INFO] Building jar: /Users/danielkayiwa/.claude/pipeline/worktrees/openmrs-openmrs-module-chartsearchai-377/api/target/chartsearchai-api-1.0.0-SNAPSHOT.jar
[INFO] 
[INFO] --- install:3.1.2:install (default-install) @ chartsearchai-api ---
[INFO] Installing /Users/danielkayiwa/.claude/pipeline/worktrees/openmrs-openmrs-module-chartsearchai-377/api/pom.xml to /Users/danielkayiwa/.claude/pipeline/m2/slot-4/org/openmrs/module/chartsearchai-api/1.0.0-SNAPSHOT/chartsearchai-api-1.0.0-SNAPSHOT.pom
[INFO] Installing /Users/danielkayiwa/.claude/pipeline/worktrees/openmrs-openmrs-module-chartsearchai-377/api/target/chartsearchai-api-1.0.0-SNAPSHOT.jar to /Users/danielkayiwa/.claude/pipeline/m2/slot-4/org/openmrs/module/chartsearchai-api/1.0.0-SNAPSHOT/chartsearchai-api-1.0.0-SNAPSHOT.jar
[INFO] 
[INFO] ---------------< org.openmrs.module:chartsearchai-omod >----------------
[INFO] Building Chart Search AI Module - OMOD 1.0.0-SNAPSHOT              [3/3]
[INFO]   from omod/pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
Downloading from archetype: https://mavenrepo.openmrs.org/public/org/openmrs/web/openmrs-web/2.9.0-SNAPSHOT/maven-metadata.xml
Downloading from openmrs-repo-thirdparty: https://mavenrepo.openmrs.org/thirdparty/org/openmrs/web/openmrs-web/2.9.0-SNAPSHOT/maven-metadata.xml
Downloading from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/web/openmrs-web/2.9.0-SNAPSHOT/maven-metadata.xml
Downloading from mks-nexus-public: https://nexus.mekomsolutions.net/repository/maven-public/org/openmrs/web/openmrs-web/2.9.0-SNAPSHOT/maven-metadata.xml
Downloading from sonatype-central-snapshots: https://central.sonatype.com/repository/maven-snapshots/org/openmrs/web/openmrs-web/2.9.0-SNAPSHOT/maven-metadata.xml
Downloading from sonatype-nexus: https://oss.sonatype.org/content/repositories/releases/org/openmrs/web/openmrs-web/2.9.0-SNAPSHOT/maven-metadata.xml
Progress (1): 545/992 B
Progress (1): 992 B    
                   
Downloaded from archetype: https://mavenrepo.openmrs.org/public/org/openmrs/web/openmrs-web/2.9.0-SNAPSHOT/maven-metadata.xml (992 B at 808 B/s)
Progress (1): 545/992 B
Progress (1): 992 B    
                   
Downloaded from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/web/openmrs-web/2.9.0-SNAPSHOT/maven-metadata.xml (992 B at 661 B/s)
Downloading from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/web/openmrs-web/2.9.0-SNAPSHOT/openmrs-web-2.9.0-20260904.122951-82.pom
Progress (1): 0.5/5.9 kB
Progress (1): 1.9/5.9 kB
Progress (1): 3.2/5.9 kB
Progress (1): 4.7/5.9 kB
Progress (1): 5.9 kB    
                    
Downloaded from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/web/openmrs-web/2.9.0-SNAPSHOT/openmrs-web-2.9.0-20260904.122951-82.pom (5.9 kB at 13 kB/s)
Downloading from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/web/openmrs-web/2.9.0-SNAPSHOT/openmrs-web-2.9.0-20260904.122951-82.jar
Progress (1): 0.5/186 kB
Progress (1): 1.9/186 kB
Progress (1): 3.3/186 kB
Progress (1): 4.7/186 kB
Progress (1): 6.1/186 kB
Progress (1): 7.5/186 kB
Progress (1): 8.9/186 kB
Progress (1): 10/186 kB 
Progress (1): 12/186 kB
Progress (1): 13/186 kB
Progress (1): 14/186 kB
Progress (1): 16/186 kB
Progress (1): 16/186 kB
Progress (1): 17/186 kB
Progress (1): 19/186 kB
Progress (1): 20/186 kB
Progress (1): 21/186 kB
Progress (1): 23/186 kB
Progress (1): 24/186 kB
Progress (1): 26/186 kB
Progress (1): 27/186 kB
Progress (1): 28/186 kB
Progress (1): 30/186 kB
Progress (1): 31/186 kB
Progress (1): 33/186 kB
Progress (1): 34/186 kB
Progress (1): 35/186 kB
Progress (1): 37/186 kB
Progress (1): 38/186 kB
Progress (1): 40/186 kB
Progress (1): 41/186 kB
Progress (1): 42/186 kB
Progress (1): 44/186 kB
Progress (1): 45/186 kB
Progress (1): 47/186 kB
Progress (1): 48/186 kB
Progress (1): 49/186 kB
Progress (1): 51/186 kB
Progress (1): 52/186 kB
Progress (1): 53/186 kB
Progress (1): 55/186 kB
Progress (1): 56/186 kB
Progress (1): 58/186 kB
Progress (1): 59/186 kB
Progress (1): 60/186 kB
Progress (1): 62/186 kB
Progress (1): 63/186 kB
Progress (1): 65/186 kB
Progress (1): 66/186 kB
Progress (1): 67/186 kB
Progress (1): 69/186 kB
Progress (1): 70/186 kB
Progress (1): 72/186 kB
Progress (1): 73/186 kB
Progress (1): 74/186 kB
Progress (1): 76/186 kB
Progress (1): 77/186 kB
Progress (1): 79/186 kB
Progress (1): 80/186 kB
Progress (1): 81/186 kB
Progress (1): 83/186 kB
Progress (1): 84/186 kB
Progress (1): 86/186 kB
Progress (1): 87/186 kB
Progress (1): 88/186 kB
Progress (1): 90/186 kB
Progress (1): 91/186 kB
Progress (1): 93/186 kB
Progress (1): 94/186 kB
Progress (1): 95/186 kB
Progress (1): 97/186 kB
Progress (1): 98/186 kB
Progress (1): 100/186 kB
Progress (1): 101/186 kB
Progress (1): 102/186 kB
Progress (1): 104/186 kB
Progress (1): 105/186 kB
Progress (1): 107/186 kB
Progress (1): 108/186 kB
Progress (1): 109/186 kB
Progress (1): 111/186 kB
Progress (1): 112/186 kB
Progress (1): 114/186 kB
Progress (1): 115/186 kB
Progress (1): 116/186 kB
Progress (1): 118/186 kB
Progress (1): 119/186 kB
Progress (1): 121/186 kB
Progress (1): 122/186 kB
Progress (1): 123/186 kB
Progress (1): 125/186 kB
Progress (1): 126/186 kB
Progress (1): 128/186 kB
Progress (1): 129/186 kB
Progress (1): 130/186 kB
Progress (1): 132/186 kB
Progress (1): 133/186 kB
Progress (1): 135/186 kB
Progress (1): 136/186 kB
Progress (1): 137/186 kB
Progress (1): 139/186 kB
Progress (1): 140/186 kB
Progress (1): 142/186 kB
Progress (1): 143/186 kB
Progress (1): 144/186 kB
Progress (1): 146/186 kB
Progress (1): 147/186 kB
Progress (1): 149/186 kB
Progress (1): 150/186 kB
Progress (1): 151/186 kB
Progress (1): 153/186 kB
Progress (1): 154/186 kB
Progress (1): 156/186 kB
Progress (1): 157/186 kB
Progress (1): 158/186 kB
Progress (1): 160/186 kB
Progress (1): 161/186 kB
Progress (1): 163/186 kB
Progress (1): 164/186 kB
Progress (1): 165/186 kB
Progress (1): 167/186 kB
Progress (1): 168/186 kB
Progress (1): 170/186 kB
Progress (1): 171/186 kB
Progress (1): 172/186 kB
Progress (1): 174/186 kB
Progress (1): 175/186 kB
Progress (1): 177/186 kB
Progress (1): 178/186 kB
Progress (1): 179/186 kB
Progress (1): 181/186 kB
Progress (1): 182/186 kB
Progress (1): 184/186 kB
Progress (1): 185/186 kB
Progress (1): 186 kB    
                    
Downloaded from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/web/openmrs-web/2.9.0-SNAPSHOT/openmrs-web-2.9.0-20260904.122951-82.jar (186 kB at 180 kB/s)
Downloading from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/web/openmrs-web/2.9.0-SNAPSHOT/openmrs-web-2.9.0-20260904.122951-82-tests.jar
Progress (1): 0.4/60 kB
Progress (1): 1.8/60 kB
Progress (1): 3.2/60 kB
Progress (1): 4.6/60 kB
Progress (1): 6.0/60 kB
Progress (1): 7.4/60 kB
Progress (1): 8.8/60 kB
Progress (1): 10/60 kB 
Progress (1): 12/60 kB
Progress (1): 13/60 kB
Progress (1): 14/60 kB
Progress (1): 16/60 kB
Progress (1): 16/60 kB
Progress (1): 17/60 kB
Progress (1): 19/60 kB
Progress (1): 20/60 kB
Progress (1): 21/60 kB
Progress (1): 23/60 kB
Progress (1): 24/60 kB
Progress (1): 26/60 kB
Progress (1): 27/60 kB
Progress (1): 28/60 kB
Progress (1): 30/60 kB
Progress (1): 31/60 kB
Progress (1): 33/60 kB
Progress (1): 34/60 kB
Progress (1): 35/60 kB
Progress (1): 37/60 kB
Progress (1): 38/60 kB
Progress (1): 40/60 kB
Progress (1): 41/60 kB
Progress (1): 42/60 kB
Progress (1): 44/60 kB
Progress (1): 45/60 kB
Progress (1): 47/60 kB
Progress (1): 48/60 kB
Progress (1): 49/60 kB
Progress (1): 51/60 kB
Progress (1): 52/60 kB
Progress (1): 53/60 kB
Progress (1): 55/60 kB
Progress (1): 56/60 kB
Progress (1): 58/60 kB
Progress (1): 59/60 kB
Progress (1): 60 kB   
                   
Downloaded from openmrs-repo: https://mavenrepo.openmrs.org/public/org/openmrs/web/openmrs-web/2.9.0-SNAPSHOT/openmrs-web-2.9.0-20260904.122951-82-tests.jar (60 kB at 94 kB/s)
[INFO] 
[INFO] --- clean:3.2.0:clean (default-clean) @ chartsearchai-omod ---
[INFO] Deleting /Users/danielkayiwa/.claude/pipeline/worktrees/openmrs-openmrs-module-chartsearchai-377/omod/target
[INFO] 
[INFO] --- openmrs:1.0.1:initialize-module (init) @ chartsearchai-omod ---
[INFO] 
[INFO] --- clean:3.2.0:clean (Drop api classes unpacked by a previous packaging build) @ chartsearchai-omod ---
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ chartsearchai-omod ---
[INFO] Copying 1 resource from src/main/resources to target/classes
[INFO] 
[INFO] --- compiler:3.13.0:compile (default-compile) @ chartsearchai-omod ---
[INFO] Recompiling the module because of changed dependency.
[INFO] Compiling 1 source file with javac [debug target 11] to target/classes
[WARNING] system modules path not set in conjunction with -source 11
[INFO] Annotation processing is enabled because one or more processors were found
  on the class path. A future release of javac may disable annotation processing
  unless at least one processor is specified by name (-processor), or a search
  path is specified (--processor-path, --processor-module-path), or annotation
  processing is enabled explicitly (-proc:only, -proc:full).
  Use -Xlint:-options to suppress this message.
  Use -proc:none to disable annotation processing.
[INFO] 
[INFO] --- resources:3.3.1:testResources (default-testResources) @ chartsearchai-omod ---
[INFO] skip non existing resourceDirectory /Users/danielkayiwa/.claude/pipeline/worktrees/openmrs-openmrs-module-chartsearchai-377/omod/src/test/resources
[INFO] 
[INFO] --- compiler:3.13.0:testCompile (default-testCompile) @ chartsearchai-omod ---
[INFO] Recompiling the module because of changed dependency.
[INFO] Compiling 25 source files with javac [debug target 11] to target/test-classes
[WARNING] system modules path not set in conjunction with -source 11
[INFO] Annotation processing is enabled because one or more processors were found
  on the class path. A future release of javac may disable annotation processing
  unless at least one processor is specified by name (-processor), or a search
  path is specified (--processor-path, --processor-module-path), or annotation
  processing is enabled explicitly (-proc:only, -proc:full).
  Use -Xlint:-options to suppress this message.
  Use -proc:none to disable annotation processing.
[INFO] /Users/danielkayiwa/.claude/pipeline/worktrees/openmrs-openmrs-module-chartsearchai-377/omod/src/test/java/org/openmrs/module/chartsearchai/web/rest/ChartSearchAiRestControllerTest.java: /Users/danielkayiwa/.claude/pipeline/worktrees/openmrs-openmrs-module-chartsearchai-377/omod/src/test/java/org/openmrs/module/chartsearchai/web/rest/ChartSearchAiRestControllerTest.java uses or overrides a deprecated API.
[INFO] /Users/danielkayiwa/.claude/pipeline/worktrees/openmrs-openmrs-module-chartsearchai-377/omod/src/test/java/org/openmrs/module/chartsearchai/web/rest/ChartSearchAiRestControllerTest.java: Recompile with -Xlint:deprecation for details.
[INFO] 
[INFO] --- surefire:3.5.5:test (default-test) @ chartsearchai-omod ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running org.openmrs.module.chartsearchai.web.rest.ChartSearchAiStreamKeepAliveTest
WARN - ChartSearchAiRestController$SseKeepAlive.write(1576) |2026-09-07T00:27:07,892| SSE keep-alive failed; the schedule continues
java.lang.IllegalStateException: response already recycled
	at org.openmrs.module.chartsearchai.web.rest.ChartSearchAiStreamKeepAliveTest$RefusingSink.write(ChartSearchAiStreamKeepAliveTest.java:377) ~[test-classes/:?]
	at java.base/java.io.OutputStream.write(OutputStream.java:124) ~[?:?]
	at org.openmrs.module.chartsearchai.web.rest.ChartSearchAiRestController$SseKeepAlive.write(ChartSearchAiRestController.java:1562) ~[classes/:?]
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:572) ~[?:?]
	at java.base/java.util.concurrent.FutureTask.runAndReset(FutureTask.java:358) ~[?:?]
	at java.base/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:305) ~[?:?]
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144) ~[?:?]
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642) ~[?:?]
	at java.base/java.lang.Thread.run(Thread.java:1583) [?:?]
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 5.970 s -- in org.openmrs.module.chartsearchai.web.rest.ChartSearchAiStreamKeepAliveTest
[INFO] Running org.openmrs.module.chartsearchai.web.rest.ChartSearchAiSearchResponseGroupingTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.065 s -- in org.openmrs.module.chartsearchai.web.rest.ChartSearchAiSearchResponseGroupingTest
[INFO] Running org.openmrs.module.chartsearchai.web.rest.ChartSearchAiUnfaithfulRenderingTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.126 s -- in org.openmrs.module.chartsearchai.web.rest.ChartSearchAiUnfaithfulRenderingTest
[INFO] Running org.openmrs.module.chartsearchai.web.rest.ChartSearchAiMisattributedOrderCitationTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.012 s -- in org.openmrs.module.chartsearchai.web.rest.ChartSearchAiMisattributedOrderCitationTest
[INFO] Running org.openmrs.module.chartsearchai.web.rest.ChartSearchAiSafetyWarningSeverityWireTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.028 s -- in org.openmrs.module.chartsearchai.web.rest.ChartSearchAiSafetyWarningSeverityWireTest
[INFO] Running org.openmrs.module.chartsearchai.web.rest.ChartSearchAiStreamEventOrderTest
WARN - ChartSearchAiRestController.lambda$streamAnswer$1(577) |2026-09-07T00:27:08,632| Ungrounded-answer consumer fired more than once; ignoring
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.010 s -- in org.openmrs.module.chartsearchai.web.rest.ChartSearchAiStreamEventOrderTest
[INFO] Running org.openmrs.module.chartsearchai.web.rest.ChartSearchAiDrugReferenceStatusTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.541 s -- in org.openmrs.module.chartsearchai.web.rest.ChartSearchAiDrugReferenceStatusTest
[INFO] Running org.openmrs.module.chartsearchai.web.rest.ChartSearchAiUnresolvedDrugClassTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.006 s -- in org.openmrs.module.chartsearchai.web.rest.ChartSearchAiUnresolvedDrugClassTest
[INFO] Running org.openmrs.module.chartsearchai.web.rest.ChartSearchAiRestControllerTest
WARN - ChartSearchAiRestController.validateQuestion(1047) |2026-09-07T00:27:09,186| Rejected question containing prompt injection pattern
WARN - ChartSearchAiRestController.validateQuestion(1047) |2026-09-07T00:27:09,189| Rejected question containing prompt injection pattern
WARN - ChartSearchAiRestController.validateQuestion(1047) |2026-09-07T00:27:09,189| Rejected question containing prompt injection pattern
WARN - ChartSearchAiRestController.validateQuestion(1047) |2026-09-07T00:27:09,190| Rejected question containing prompt injection pattern
WARN - ChartSearchAiRestController.validateQuestion(1047) |2026-09-07T00:27:09,191| Rejected question containing prompt injection pattern
WARN - ChartSearchAiRestController.validateQuestion(1047) |2026-09-07T00:27:09,192| Rejected question containing prompt injection pattern
WARN - ChartSearchAiRestController.validateQuestion(1047) |2026-09-07T00:27:09,193| Rejected question containing prompt injection pattern
WARN - ChartSearchAiRestController.validateQuestion(1047) |2026-09-07T00:27:09,194| Rejected question containing prompt injection pattern
WARN - ChartSearchAiRestController.validateQuestion(1047) |2026-09-07T00:27:09,196| Rejected question containing prompt injection pattern
WARN - ChartSearchAiRestController.validateQuestion(1047) |2026-09-07T00:27:09,198| Rejected question containing prompt injection pattern
WARN - ChartSearchAiRestController.validateQuestion(1047) |2026-09-07T00:27:09,199| Rejected question containing prompt injection pattern
ERROR - ChartSearchAiRestController.handleUnexpected(921) |2026-09-07T00:27:09,200| Unhandled exception in chartsearchai REST controller
java.lang.RuntimeException: sensitive internal detail
	at org.openmrs.module.chartsearchai.web.rest.ChartSearchAiRestControllerTest.handleUnexpected_shouldMapToCleanInternalServerError(ChartSearchAiRestControllerTest.java:224) ~[test-classes/:?]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[?:?]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[?:?]
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:767) ~[junit-platform-commons-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$8(TestMethodTestDescriptor.java:217) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:213) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:138) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68) ~[junit-jupiter-engine-5.11.4.jar:5.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:156) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[?:?]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.11.4.jar:1.11.4]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) [junit-platform-launcher-1.10.1.jar:1.10.1]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.executeWithoutCancellationToken(LauncherAdapter.java:60) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.LauncherAdapter.execute(LauncherAdapter.java:52) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:203) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:168) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:136) [surefire-junit-platform-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) [surefire-booter-3.5.5.jar:3.5.5]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) [surefire-booter-3.5.5.jar:3.5.5]
[INFO] Tests run: 32, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.017 s -- in org.openmrs.module.chartsearchai.web.rest.ChartSearchAiRestControllerTest
[INFO] Running org.openmrs.module.chartsearchai.web.rest.ChartSearchAiAuditReferenceSliceTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.015 s -- in org.openmrs.module.chartsearchai.web.rest.ChartSearchAiAuditReferenceSliceTest
[INFO] Running org.openmrs.module.chartsearchai.web.rest.ChartSearchAiConditionRuleCoverageTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.010 s -- in org.openmrs.module.chartsearchai.web.rest.ChartSearchAiConditionRuleCoverageTest
[INFO] Running org.openmrs.module.chartsearchai.web.rest.ChartSearchAiAuditSearchModeTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.007 s -- in org.openmrs.module.chartsearchai.web.rest.ChartSearchAiAuditSearchModeTest
[INFO] Running org.openmrs.module.chartsearchai.web.rest.ChartSearchAiReferenceGroundingWithholdingTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.014 s -- in org.openmrs.module.chartsearchai.web.rest.ChartSearchAiReferenceGroundingWithholdingTest
[INFO] Running org.openmrs.module.chartsearchai.web.rest.ChartSearchAiInteractionPairExtentTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.008 s -- in org.openmrs.module.chartsearchai.web.rest.ChartSearchAiInteractionPairExtentTest
[INFO] Running org.openmrs.module.chartsearchai.web.rest.ChartSearchAiChartOrderBridgeTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.018 s -- in org.openmrs.module.chartsearchai.web.rest.ChartSearchAiChartOrderBridgeTest
[INFO] Running org.openmrs.module.chartsearchai.web.rest.ChartSearchAiReferenceProvenanceTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.006 s -- in org.openmrs.module.chartsearchai.web.rest.ChartSearchAiReferenceProvenanceTest
[INFO] Running org.openmrs.module.chartsearchai.web.rest.ChartSearchAiStreamingTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.010 s -- in org.openmrs.module.chartsearchai.web.rest.ChartSearchAiStreamingTest
[INFO] Running org.openmrs.module.chartsearchai.web.rest.ChartSearchAiReferenceGroupingTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.008 s -- in org.openmrs.module.chartsearchai.web.rest.ChartSearchAiReferenceGroupingTest
[INFO] Running org.openmrs.module.chartsearchai.GlobalPropertyDefaultsTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 s -- in org.openmrs.module.chartsearchai.GlobalPropertyDefaultsTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 138, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] 
[INFO] --- dependency:3.7.0:unpack-dependencies (Expand moduleApplicationContext and messages) @ chartsearchai-omod ---
[INFO] 
[INFO] --- jar:3.4.1:jar (default-jar) @ chartsearchai-omod ---
[INFO] Building jar: /Users/danielkayiwa/.claude/pipeline/worktrees/openmrs-openmrs-module-chartsearchai-377/omod/target/chartsearchai-1.0.0-SNAPSHOT.jar
[INFO] 
[INFO] --- openmrs:1.0.1:package-module (pack) @ chartsearchai-omod ---
[INFO] Packaging OpenMRS module
[INFO] Building jar: /Users/danielkayiwa/.claude/pipeline/worktrees/openmrs-openmrs-module-chartsearchai-377/omod/target/chartsearchai-1.0.0-SNAPSHOT.omod
[INFO] 
[INFO] --- install:3.1.2:install (default-install) @ chartsearchai-omod ---
[INFO] Installing /Users/danielkayiwa/.claude/pipeline/worktrees/openmrs-openmrs-module-chartsearchai-377/omod/pom.xml to /Users/danielkayiwa/.claude/pipeline/m2/slot-4/org/openmrs/module/chartsearchai-omod/1.0.0-SNAPSHOT/chartsearchai-omod-1.0.0-SNAPSHOT.pom
[INFO] Installing /Users/danielkayiwa/.claude/pipeline/worktrees/openmrs-openmrs-module-chartsearchai-377/omod/target/chartsearchai-1.0.0-SNAPSHOT.omod to /Users/danielkayiwa/.claude/pipeline/m2/slot-4/org/openmrs/module/chartsearchai-omod/1.0.0-SNAPSHOT/chartsearchai-omod-1.0.0-SNAPSHOT.jar
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for Chart Search AI Module 1.0.0-SNAPSHOT:
[INFO] 
[INFO] Chart Search AI Module ............................. SUCCESS [  2.481 s]
[INFO] Chart Search AI Module - API ....................... SUCCESS [01:30 min]
[INFO] Chart Search AI Module - OMOD ...................... SUCCESS [ 14.149 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  01:47 min
[INFO] Finished at: 2026-09-07T00:27:11+03:00
[INFO] ------------------------------------------------------------------------, and Bash became unable to create its own output file, so no command, edit or state write was possible. The run had to hand back with a blocking finding outstanding and could not even record the override. Isolated-worktree review agents are not free.
- resolve-ticket:Step 3 — the gate ran twice and both passes' blocking objections SETTLED rather than deadlocked, which is outcome 2. Pass 2 raised three blocking objections at once, all adopted; no third pass was run, per the rule against re-gating a settled question.

## Declined
- (none this run — every finding from both review rounds and all four harden lenses was implemented)
- Deferred without implementing: extracting the three fidelity tests' shared `TestableService`/`StubProvider` harness. If we ship without this, nothing breaks today — but a fourth fidelity check copies the harness from whichever sibling its author opens, and one sibling has already drifted onto a shorter `validate` overload that leaves its stub inert. Not taken because extraction means editing two existing test files; the hazard is instead named in a comment at the copy site, which is where the next author reads.
- Deferred: the root CLAUDE.md is 201 bytes under its 23,000 budget. If we ship without trimming further, the next bullet anyone adds reddens ProjectInstructionsGuardTest and its author must either trim someone else's rule or raise the budget in the same commit as the prose that overflowed it — which that guard's own javadoc calls the thing not to do.

## Assumptions review overturned
- A3 "report-only, no wire key" (plan v1) -> published as `misattributedOrderCitations`, because ADR Decision 74's own stated trigger is met verbatim by this ticket · gate pass 2
- A2 "the allow-list admits `order`" -> dropped; the same type covers "Test order:" and "Referral order:", and its only production appearance is in code with no caller since #51 · gate pass 2 + harden
- "the run is bounded on one side only, and the asymmetry is a decision" -> bounded on the near side by the claim's own clause · round 1
