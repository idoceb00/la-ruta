# AGENTS.md

La Ruta is a tapas-bar tracker with two apps: a Spring Boot REST API in `backend/` and an Angular PWA in `frontend/`.

## Setup

```bash
cp .env.example .env   # then set JWT_SECRET (openssl rand -base64 32)
docker compose up -d   # PostgreSQL 17
```

`.env` lives at the repo root, is read by `docker compose`, and is auto-imported only by the `dev` backend profile.


## Workflow

- Conventional Commits with types `feat`, `fix`, `refactor`, `build`, `docs`, `test`, `ci`, `perf`, `style`. Never use `chore`. One commit per logical change.
- Branches use the `feat/` prefix.
- Code, identifiers, comments and commit messages in English. All user-facing text (UI labels, error messages shown to users) in Spanish.
- Ask before changing deployment or environment wiring: `vercel.json`, `backend/Dockerfile`, `.github/workflows/`, profile configuration in `application*.yml`, or `.env*` files.

## Backend (`backend/`)

- Java 21, Spring Boot 4.1, Maven wrapper. PostgreSQL schema is owned by Flyway; Hibernate is `ddl-auto=validate`.
- Dev server: `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` (SQL logging, Swagger at `/swagger-ui.html`, CORS for `localhost:4200`).
- Without the `dev` profile, export `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`, and `JWT_SECRET` yourself.
- All tests: `./mvnw test`. One test: `./mvnw test -Dtest=CommunityControllerIntegrationTest` (append `#methodName` for one method).
- CI equivalent: `./mvnw verify` (`.github/workflows/backend-ci.yml`).
- Integration tests use Testcontainers with `postgres:17`, so Docker must be running. They are `@Transactional` and roll back.
- Copy the integration-test template: `@SpringBootTest @AutoConfigureMockMvc @Transactional @Import(TestcontainersConfiguration.class)`, and authenticate MockMvc with `TestAuth.authenticatedAs(userId)`. Boot 4 moved `AutoConfigureMockMvc` to `org.springframework.boot.webmvc.test.autoconfigure`.
- Reset local DB: `docker compose down -v && docker compose up -d`.

### Backend conventions and gotchas

- Layers are `controller -> service -> repository` under `com.idoceb00.laruta.backend`; DTOs are Java records; entities are Lombok classes with protected no-arg constructors and domain methods.
- Everything is scoped to a `Community`. Community-scoped services must call `MembershipService.requireMembership(communityId)`; authentication alone is not authorization.
- JWT is HS256 and carries a custom `userId` claim. Use `CurrentUserProvider` to get the current user; do not parse the username or `SecurityContext` directly.
- JPA auditing fills `createdBy`/`modifiedBy` from `CurrentUserProvider`.
- Any entity/schema change requires a new Flyway migration `backend/src/main/resources/db/migration/V<n>__*.sql`. Editing an already-applied migration or relying on Hibernate DDL will fail startup.
- Ratings are denormalized in `RatingStats` (`rating_sum`/`rating_count`) with `@Version` optimistic locking on `Bar`/`BarTapa`. Change them only through the entity's `replaceRating`, never by setting counters.
- Add domain errors as typed exceptions handled in `GlobalExceptionHandler`; keep controllers returning DTOs.
- Invite codes are 8 uppercase characters from `A-HJ-NP-Z2-9` (no `I`, `O`, `0`, `1`) and are normalized to trim/uppercase on join.
- CORS origins come from `CORS_ALLOWED_ORIGINS`; the `dev` profile defaults to `http://localhost:4200`.
- Production Postgres is Neon serverless; Hikari uses `minimum-idle=0`/`maximum-pool-size=5`, so do not add long-lived connection assumptions.

## Frontend (`frontend/`)

- Angular 22 + Tailwind CSS v4 + Vitest + pnpm. Use `pnpm`, not npm (`packageManager: pnpm@12.11.2`).
- Dev server: `pnpm start`; it proxies `/api` and `/actuator/health` to `localhost:8080` via `proxy.conf.json`.
- Production build: `pnpm build`; output is `dist/laruta-frontend/browser` (used by `vercel.json`). This is also the typecheck gate; there is no lint or standalone typecheck script.
- Tests: `pnpm test --watch=false`. Focused: `pnpm test --watch=false --include=src/app/bars/bar-service.spec.ts` or `--filter=BarService`.
- Do not add an extra `--` before flags (`pnpm test -- --watch=false` is rejected by the Angular builder).
- The full suite is currently red (14 failing specs): several component specs omit required signal inputs (`NG0950`), and `src/app/app.spec.ts` asserts a stale "Hello, laruta-frontend" title. Use focused runs to verify your own change; do not fix unrelated failures.
- Formatting: `pnpm exec prettier --write <files>` (print width 100, single quotes); there is no format script.
- No frontend CI; only the backend workflow exists. Deployments are frontend via Vercel (`vercel.json`) and backend via `backend/Dockerfile`, whose build skips tests because Testcontainers needs a Docker daemon.

### Frontend conventions and gotchas

- Use Angular 22 style: standalone components, signals, and the `@Service()` decorator (from `@angular/core`, not `@Injectable()`). File/class names have no `.component` suffix (`bar-list.ts`, `bar-service.ts`).
- `ActiveCommunityService` holds the selected community and loads it with `rxResource`; bar/tapa screens depend on this context.
- Always build API URLs from `environment.apiUrl`. Dev is `''` (proxy); production points at the Render backend. Never hardcode hosts.
- `authInterceptor` attaches `Bearer <token>` except to `/api/auth` and logs out on `401`; the token is stored under localStorage key `la-ruta.token`.
- `BackendStatusService` is started by `provideAppInitializer` and pings `/actuator/health` with a raw `HttpClient` that bypasses interceptors because the deployed backend sleeps and must be woken.
- Tailwind theme tokens live in `src/styles.css` (`paper`, `surface`, `panel`, `ink`, `muted`, `line`, `wine`, `mustard`). Reuse the shared `appButton` and `appInput` directives instead of inlining button/input styles.
- Use theme tokens, never raw Tailwind palette colors (`bg-wine`, not `bg-red-800`).
- Inject dependencies with `inject()`, not constructor parameters. Members used only by the template are `protected readonly`.
- Templates use built-in control flow (`@if`, `@for`, `@switch`); never `*ngIf`/`*ngFor`.
- Async data goes through `rxResource`; derived state through `computed`/`linkedSignal`. Avoid manual `subscribe` except for one-shot actions (save, delete).
- New forms use signal forms (`@angular/forms/signals`), as in `bar-form.ts`. Login and register still use the old forms; do not migrate them unless asked.
- Show loading and error states with the shared `Loading` and `ErrorBox` components.
- Before creating a UI component, check `src/app/shared/` for an existing one (`SearchBox`, `HighlightText`, `StarRating`…).
