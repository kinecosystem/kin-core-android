# Contributing to kin-sdk-core-android

Thank you very much for helping out with this project!
Please read the following guidelines before making proposals:

## Guidelines

* All suggestions should be raised as an issue or a pull request.
* To submit your pull request, fork the repository, add your commit and submit a pull request to our `dev` branch.
* Git commit messages should include a short title and a summary explaining the issue and how it has been handled.
[Here is a handy guide on writing good commit messages](https://chris.beams.io/posts/git-commit/)
* Please adhere to existing code style. We use the [Google Java style guide](https://google.github.io/styleguide/javaguide.html)
to style our code with only exceptions of allowing a column limit of 120 instead of 100 and using 4 spaces for indentation instead of 2.
* The project includes [codeStyleSettings.xml](.idea/codeStyleSettings.xml) under the .idea folder. We used 
[Google Java style guide for IntelliJ](https://github.com/google/styleguide/blob/gh-pages/intellij-java-google-style.xml)
to generate this file, after adapting the column limit and identation as specified above. To format your code using the style 
guide on Android Studio, simply go to `Code -> Reformat Code`

**We look forward to your contributions!**

# Notes

## Git Flow

The following describes our internal development team Git flow.
If you're an outside collaborator - this section is provided here as general knowledge.
However, you are not required to read or follow it.

### Merge vs. Rebase

- When creating and starting work on a new feature branch - it is fine to
squash (`rebase --interactive`), `amend`, or `push --force`.
- Before opening a pull request, you should rebase from the primary `dev` branch.
- Once a pull request has been opened, we stop `rebase`, `amend`, or `push --force`.
  - All changes to the branch from here on will be only via new commits added to the branch.
  - Specifically, fetching changes from other branches (i.e. `dev`)
will be done via merging from it (`merge --no-ff`).
  - The reason behind this is that this allows you and everyone else reviewing
the branch to see all changes that were made since the pull request was opened -
updates following PR comments, merge conflicts, etc. Resolving contflicts using rebase, in  contrast,
are "hidden" and hard to track.
- Avoid comitting binaries e.g. `.aar` files.
  - This increases the repository size by a large amount and is unnecessary.
  - Instead, you should create a bootstrap script that downloads these binaries
from wherever they are stored.
- Branch name structure should be `ISSUE NUMBER/feature name` e.g. `KIN-1234/my-new-feature`.
Do not include your name in the branch name. There is no need for each branch to have a single owner.
- Do not use `// created by` header comments from code files. This creates the wrong impression
that there is a single owner for every file.
- Do not use a license section in code files. We mostly use global [`LICENSE`](LICENSE)
files for entire repositories. **Note** There might be some exceptions to this
when dealing with 3rd-party libraries.


![Kin Token](kin_android.png)
