---
description: Propose and create Conventional Commits for the current changes
agent: plan
---

Current branch: !`git branch --show-current`

Status:
!`git status --short`

Staged diff:
!`git diff --staged`

Unstaged diff:
!`git diff`

Recent commits (style reference):
!`git log --oneline -10`

Extra instructions: $ARGUMENTS

Plan the commits for these changes following the commit rules in `AGENTS.md`:

1. Read untracked files (`??` in the status) yourself: their content is not in the diff.
2. Group the changes into logical commits, one per logical change. Group by whole files. If a single file mixes
   unrelated changes, say so and ask how to split it instead of guessing.
3. Choose the type carefully:
   - `feat`/`fix`: user-visible behavior. `refactor`: no visible change at all.
   - `style`: code formatting only, never CSS or visual changes.
   - `build`: dependencies, build and tooling config. `ci`: workflows. `docs`, `test`, `perf` as usual. Never `chore`.
4. Messages: `type: subject` (use a scope only if recent commits do), imperative, lowercase, no final period,
   max 72 characters. Add a body only when the reason is not obvious from the subject.
5. Show the plan as a numbered list (message + files) and stop. Wait for my confirmation before running anything.

After I confirm:
- Stage exact paths with `git add <files>`. Never `git add -A` or `git add .`.
- Create the commits in order and show `git log --oneline` for them at the end.
- Never push, amend, rebase or use `--no-verify`.

Stop and warn me instead of planning if:
- The current branch is `main`: propose a `feat/` branch name first.
- Any change touches `.env*` or looks like a secret (keys, tokens, passwords).