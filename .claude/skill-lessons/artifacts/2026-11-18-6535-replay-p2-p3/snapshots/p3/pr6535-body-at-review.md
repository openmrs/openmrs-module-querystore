

<!--- Add a pull request title above in this format -->
<!--- real example: 'TRUNK-5111: Replace use of deprecated isVoided' -->
<!--- 'TRUNK-JiraIssueNumber: JiraIssueTitle' -->
## Description of what I changed
<!--- Describe your changes in detail -->
<!--- It can simply be your commit message, which you must have -->
I reduced the number of storage service I/O operations performed when reading a complex obs, by removing redundant existence probes and separate metadata fetches. Reading a complex obs previously cost 5 filesystem operations on local storage (three `Files.exists` calls, a stat, and a stream open) or three S3 round-trips of HEAD/GET/HEAD; it now costs 3 filesystem operations on local storage or two S3 round-trips of HEAD/GET.

The one remaining `Files.exists` / HEAD is the layout probe in `AbstractHandler`, which decides whether a key resolves against the current or the legacy storage location. It is not redundant: without it a read would have to probe both layouts. Everything after it is a single combined data+metadata read, so data and metadata no longer cost separate operations.

## Issue I worked on
<!--- This project only accepts pull requests related to open issues -->
<!--- Want a new feature or change? Discuss it in an issue first! -->
<!--- Found a bug? Point us to the issue/or create one so we can reproduce it! -->
<!--- Just add the issue number at the end: -->
https://openmrs.atlassian.net/browse/TRUNK-6682

## Checklist: I completed these to help reviewers :)
<!--- Put an `x` in the box if you did the task -->
<!--- If you forgot a task please follow the instructions below -->
- [x] My IDE is configured to follow the [**code style**](https://wiki.openmrs.org/display/docs/Java+Conventions) of this project.

  No? Unsure? -> [configure your IDE](https://wiki.openmrs.org/display/docs/How-To+Setup+And+Use+Your+IDE), format the code and add the changes with `git add . && git commit --amend`

- [x] I have **added tests** to cover my changes. (If you refactored
  existing code that was well tested you do not have to add tests)

  No? -> write tests and add them to this commit `git add . && git commit --amend`

- [x] I ran `./mvnw clean package` right before creating this pull request and
  added all formatting changes to my commit.

  No? -> execute above command

- [x] All new and existing **tests passed**.

  No? -> figure out why and add the fix to your commit. It is your responsibility to make sure your code works.

- [x] My pull request is **based on the latest changes** of the master branch.

  No? Unsure? -> execute command `git pull --rebase upstream master`


