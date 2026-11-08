# MEMORY.md — La Ruta

Project memory across agent sessions. Keep it under ~50 lines: summarize or remove what no longer helps.
Rules and conventions live in `AGENTS.md`; this file holds current state, decisions and their reasons.

## Current state
- Deployed: backend on Render (free plan, sleeps when idle) with Neon Postgres; frontend on Vercel, installable as a PWA.
- "Showcase" stage done: full redesign (own palette, shared components, clickable star ratings), backend wake-up screen, new PWA icons.
- In progress: agent harness (`AGENTS.md`, `MEMORY.md`, `opencode.json`, OpenCode commands and MCPs).

## Decisions (and why)
- Communities are the core: every bar belongs to one. Any member can create, edit and delete bars; the admin manages
  the community (members, invite code, name, deletion). Joining is by invite code only.
- Search runs on the active community; the app opens on the last one used.
- Leaving a community deletes the member's reviews there and recalculates averages in Java, not via DB cascade; bars stay.
  If the admin leaves, the oldest member becomes admin; if the last member leaves, the community is deleted.
  Deleting an account means leaving all communities.
- Rating writes retry on `OptimisticLockingFailureException` with Spring 7's native `@Retryable` (3 retries, 50 ms),
  placed on the controller so each attempt opens a fresh transaction and reads the new version.
- Bar edits do not retry: the 409 must reach the user, so another member's changes are never silently overwritten.
- The tapa catalog is global and internal: tapas are added to a bar by name (normalized find-or-create);
  the catalog only serves autocomplete and "where is X?".
- Adding a tapa already in a bar returns 409 instead of being idempotent, so a review is never overwritten.
- Always surrogate ids; duplicates are prevented with composite unique constraints, not composite PKs (simpler in JPA).
- NotFound exceptions only for access by id; an empty search returns `[]`.
- A review needs a rating or notes. Bar ratings are 0–10.
- Render's free plan sleeps the backend, so the frontend pings `/actuator/health` at startup and shows a wake-up screen.

## Lessons and mistakes to avoid
- `@Transactional` must be Spring's, not Jakarta's: only Spring's supports `readOnly`.
- Derived query methods must use real entity field names (`Containing`, `IgnoreCase`), or the app fails at startup.
- A retry inside the same transaction repeats with stale data: retry boundaries go outside the transaction.
- The rating `@Retryable` has no test (real concurrency is hard to reproduce): be careful when touching rating code.

## Next steps (roadmap, in order)
1. Agent harness: Angular CLI MCP → `/commit` and `/verify` commands → design skill → Render and Trello MCPs.
2. Demo account: `DemoDataSeeder` behind `app.demo.enabled=true` (never local or tests), community code `DEMOLEON`
   recreated on each startup, invented bars in real León areas, `POST /api/auth/demo`, `demo` username reserved.
3. Welcome screen for logged-out users with a "Try the demo" button.
4. Open Graph meta tags with a share image.
5. README: screenshots, architecture, ER diagram, technical decisions.
6. Outreach: CV, portfolio, LinkedIn post.
- Trello backlog, not part of the roadmap (don't start unless asked): cleaner URL or own domain, service worker,
  community selector in the header, signal forms for login/register, bar + review + tapas in one step, search filters.