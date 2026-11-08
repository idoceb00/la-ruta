# AGENTS.md — La Ruta

Web app to save tapas bars in communities (groups of friends), review them and find where each tapa is served. Monorepo: Spring Boot REST API in `backend/` and Angular PWA in `frontend/`. It is the owner's main portfolio project and is used daily, so code must stay clean and idiomatic. The owner knows the backend well and is still learning frontend best practices: briefly explain non-obvious frontend choices.

## Stack and structure

- `backend/`: Java 21, Spring Boot 4.1, Maven wrapper, Spring Security with JWT, JPA/Hibernate, Flyway, PostgreSQL 17.
- `frontend/`: Angular 22, Tailwind CSS v4, Vitest, pnpm (`packageManager: pnpm@12.11.2`).
- Root: `AGENTS.md`, `MEMORY.md`, `opencode.json`, `docker-compose.yml`, `.env.example`.
- Deployment: frontend on Vercel (`vercel.json`); backend on Render via `backend/Dockerfile` (its build skips tests because Testcontainers needs Docker); production database on Neon.
- CI: only `.github/workflows/backend-ci.yml` (`./mvnw verify`). There is no frontend CI.

## Setup

```bash
cp .env.example .env   # then set JWT_SECRET (openssl rand -base64 32)
docker compose up -d   # PostgreSQL 17
```

`.env` lives at the repo root, is read by `docker compose`, and is auto-imported only by the `dev` backend profile.
Without the `dev` profile, export `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD` and `JWT_SECRET` yourself.

## Commands

Backend (from `backend/`):
- Dev server: `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` (SQL logging, Swagger at `/swagger-ui.html`, CORS for `localhost:4200`).
- All tests: `./mvnw test`. One class: `./mvnw test -Dtest=CommunityControllerIntegrationTest` (append `#methodName` for one method).
- CI equivalent: `./mvnw verify`.
- Reset local DB: `docker compose down -v && docker compose up -d` (from the root).

Frontend (from `frontend/`):
- Dev server: `pnpm start`; proxies `/api` and `/actuator/health` to `localhost:8080` via `proxy.conf.json`.
- Build: `pnpm build`; output `dist/laruta-frontend/browser`. It is also the typecheck gate (no lint or typecheck script).
- Tests: `pnpm test --watch=false`. Focused: `--include=src/app/bars/bar-service.spec.ts` or `--filter=BarService`.
- Format: `pnpm exec prettier --write <files>` (print width 100, single quotes). There is no format script.
- Never add an extra `--` before flags: `pnpm test -- --watch=false` is rejected by the Angular builder.

## Conventions

General:
- Conventional Commits with types `feat`, `fix`, `refactor`, `build`, `docs`, `test`, `ci`, `perf`, `style`. One commit per logical change.
- Branches use the `feature/` prefix.
- Code, identifiers, comments and commit messages in English. All user-facing text (UI labels, error messages) in Spanish.

Backend:
- Layers are `controller -> service -> repository` under `com.idoceb00.laruta.backend`. DTOs are Java records; entities are Lombok classes with protected no-arg constructors and domain methods.
- Domain errors are typed exceptions handled in `GlobalExceptionHandler`; controllers return DTOs.
- Integration tests: `@SpringBootTest @AutoConfigureMockMvc @Transactional @Import(TestcontainersConfiguration.class)`, authenticated with `TestAuth.authenticatedAs(userId)`. They roll back. Boot 4 moved `AutoConfigureMockMvc` to `org.springframework.boot.webmvc.test.autoconfigure`.

Frontend:
- Angular 22 style: standalone components, signals and the `@Service()` decorator (from `@angular/core`, not `@Injectable()`). File/class names have no `.component` suffix (`bar-list.ts`, `bar-service.ts`).
- Inject with `inject()`, not constructor parameters. Members used only by the template are `protected readonly`.
- Templates use built-in control flow (`@if`, `@for`, `@switch`), never `*ngIf`/`*ngFor`.
- Async data through `rxResource`; derived state through `computed`/`linkedSignal`. Manual `subscribe` only for one-shot actions (save, delete).
- New forms use signal forms (`@angular/forms/signals`), as in `bar-form.ts`. Login and register still use the old forms.
- Styling: Tailwind theme tokens from `src/styles.css` (`paper`, `surface`, `panel`, `ink`, `muted`, `line`, `wine`, `mustard`), never raw palette colors (`bg-wine`, not `bg-red-800`). Reuse the `appButton` and `appInput` directives.
- The project's design system (theme tokens, Roboto Condensed + Inter, shared components) always takes precedence over any generic design skill: use such skills for layout and hierarchy, never to introduce new fonts or colors.
- Loading and error states use the shared `Loading` and `ErrorBox`. Before creating a UI component, check `src/app/shared/` for an existing one (`SearchBox`, `HighlightText`, `StarRating`…).
- Always build API URLs from `environment.apiUrl` (dev is `''` through the proxy; production points at Render). Never hardcode hosts.

## Data and schema

- Flyway owns the schema; Hibernate only validates (`ddl-auto=validate`). The current schema is the result of applying all migrations in `backend/src/main/resources/db/migration/` in version order: read them and the JPA entities to understand it.
- Any entity or schema change needs a new migration `V<n>__*.sql`. Editing an applied migration fails startup.
- Ratings are denormalized in `RatingStats` (`rating_sum`/`rating_count`) with `@Version` on `Bar`/`BarTapa`. Change them only through the entity's `replaceRating`, never by setting counters.
- Invite codes: 8 uppercase characters from `A-HJ-NP-Z2-9` (no `I`, `O`, `0`, `1`), normalized to trim/uppercase on join.
- Production Postgres is Neon serverless with Hikari `minimum-idle=0`/`maximum-pool-size=5`: no long-lived connection assumptions.

## Security and context (easy to get wrong)

- Everything is scoped to a `Community`. Community-scoped services must call `MembershipService.requireMembership(communityId)`: authentication alone is not authorization.
- JWT is HS256 with a custom `userId` claim. Get the current user from `CurrentUserProvider`; never parse the username or `SecurityContext` directly. JPA auditing fills `createdBy`/`modifiedBy` from it.
- CORS origins come from `CORS_ALLOWED_ORIGINS`; the `dev` profile defaults to `http://localhost:4200`.
- `authInterceptor` attaches `Bearer <token>` except to `/api/auth` and logs out on `401`; the token is in localStorage key `la-ruta.token`.
- `ActiveCommunityService` holds the selected community; bar and tapa screens depend on it.
- `BackendStatusService` starts from `provideAppInitializer` and pings `/actuator/health` with a raw `HttpClient` that bypasses interceptors, because the deployed backend sleeps and must be woken.

## Way of working

- Do only what is asked. Do not add features on your own, and do not start Trello backlog items listed in `MEMORY.md`.
- Small, focused changes; do not rewrite code that already works.
- When unsure about an Angular API, check it with the `angular-cli` MCP (`search_documentation`, `get_best_practices`) instead of relying on memory: older patterns from training data are likely wrong for this version.
- When finished, summarize what changed, any decision the owner should review, and the proposed commits.

## Limits

- ✅ Always: call `requireMembership` in community-scoped services; add a new migration for schema changes; keep user-facing text in Spanish; verify what you changed.
- ✅ Always: update `MEMORY.md` upon completion of each task. 
- ⚠️ Ask first: changing deployment or environment wiring (`vercel.json`, `backend/Dockerfile`, `.github/workflows/`, profile configuration in `application*.yml`, `.env*`); adding dependencies; changing a REST endpoint's contract (both apps depend on it); triggering deploys or changing environment variables through an MCP.
- 🚫 Never: edit an applied migration; connect to a database or run SQL directly (psql, MCP tools, scripts); commit secrets or `.env`; use npm instead of pnpm; use the `chore` commit type.

## Verification

- Backend: run the focused test class for your change, then `./mvnw verify`. Docker must be running (Testcontainers, `postgres:17`).
- Frontend: `pnpm build` (typecheck) and focused tests for the files you touched, then format them with Prettier.
- The full frontend suite is currently red (14 failing specs): several component specs omit required signal inputs (`NG0950`), and `src/app/app.spec.ts` asserts a stale "Hello, laruta-frontend" title. Use focused runs and do not fix unrelated failures.

## Memory

- At the start, read `MEMORY.md` for the project's current state and past decisions.
- When a task is finished, update it: current state, important decisions (with their reason) and mistakes to avoid.
- Keep it short (~50 lines max): summarize or remove what no longer helps.
- If something becomes a permanent rule, propose moving it to `AGENTS.md` instead of keeping it in memory.
- Never store sensitive data (keys, tokens, personal data).