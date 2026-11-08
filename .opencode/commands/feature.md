---
description: Plan a new feature before touching any code
agent: plan
---

Feature to add: $ARGUMENTS

Current project state and past decisions: @MEMORY.md

Before writing any code, prepare a plan with:

1. Approach: how you will implement it, following the rules in `AGENTS.md`. Say which apps it touches
   (backend, frontend or both) and whether it changes any REST endpoint contract.
2. Files: each file to create or modify and what changes in it. Include any new Flyway migration.
3. Edge cases and open questions I must decide before starting. Group the questions at the end, numbered.
4. Out of scope: related things you will not do (especially Trello backlog items from `MEMORY.md`).
5. Verification: which tests you will add or run, and how I can check it manually.
6. Commits: the branch name and the planned commits, one per logical change.
7. Memory: what you would update in `MEMORY.md` and, if anything becomes a permanent rule, in `AGENTS.md`.

If the plan relies on an Angular API you are not sure about, check it with the `angular-cli` MCP first.
Do not modify any file until I approve the plan.