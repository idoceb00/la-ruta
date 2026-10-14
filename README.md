# La Ruta

## Local development

### Requirements
- Java 21
- Docker

### Setup
1. Create your local environment file and fill in the values:
```bash
   cp .env.example .env
```
   Generate the JWT secret with `openssl rand -base64 32`.
2. Start PostgreSQL:
```bash
   docker compose up -d
```
3. Run the backend:
The backend runs with the `dev` profile locally, which loads the root `.env`
and enables SQL logging and the API docs:

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

In IntelliJ, set `dev` in the run configuration's active profiles.

API docs (dev only): http://localhost:8080/swagger-ui.html

To run the backend image against the local database:

```bash
cd backend
docker build -t la-ruta-backend .
docker run --rm -p 8080:8080 --env-file ../.env -e PGHOST=host.docker.internal la-ruta-backend
```

### Database
The schema is managed with Flyway. Migrations live in `backend/src/main/resources/db/migration` and are applied automatically on startup.

To reset the local database: `docker compose down -v && docker compose up -d`.

### Tests
```bash
cd backend
./mvnw test
```
Integration tests run against a real PostgreSQL instance with Testcontainers, so Docker must be running.