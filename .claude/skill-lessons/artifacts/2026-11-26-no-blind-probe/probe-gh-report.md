## G. Owed whole-PR agent, run with `123 --no-blind`

**Re-run it this round?** No. Line 10 says: "With `--no-blind` … Step 2 starts none this round; say so in the round record, and owe none." The owed agent is a Step 2 whole-PR agent, so it is not started.

**What this round's record says:** that Step 2 started no blind agents because of `--no-blind` (line 10, "say so in the round record"). Line 22 also requires you, before the verdict, to "run or apply each owed item, or say it is still owed". Since running it is ruled out, the record has to say something about the earlier debt.

**Still owed afterwards? The text doesn't decide this.** "Owe none" can be read two ways:
- this round creates no new debt, so the earlier agent carries forward under line 22's "say it is still owed";
- the flag cancels the earlier debt as well.

Neither line 10 nor line 22 settles which reading is meant. My own reading would be the first, but that's a guess, so I'd ask the user.

## H. Rushed pass, judgment-call blocker, run with `123 --no-blind`

**Do the conditions call for a refutation pass?** Yes. Line 136 says "Reach for it, where you can start one, when **two or more** of these hold". At least two hold:
- "the finding is a **judgment call** … 'is this actually a race?'" (line 138)
- "the pass is **rushed or low-effort**" (line 140)
- Possibly a third: "reviewing **solo in one pass**". Step 2's agents don't count toward a fan-out (line 139), and `--no-blind` means none ran anyway.

**Does `--no-blind` decide it? The text doesn't decide this.** Line 10 names only Step 2: "Step 2 starts none this round", plus the carve-out "Step 3's refuter of a fix you recommend still runs." It says nothing about Step 3's refuter for a *finding*. Line 145 calls that reader a blind agent too ("a blind agent, here or for a finding above, is `~/.claude/bin/blind-run` …"). So two readings conflict:
- Line 10's "a review without blind agents" plus a carve-out that only saves the fix refuter suggests the finding refuter is off.
- Its literal scope, "Step 2 starts none", leaves the finding refuter alone.

"Where you can start one" (line 136) doesn't settle whether the user's flag counts as "can't". I'd ask the user before starting it.
