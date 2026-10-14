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
```bash
   cd backend
   ./mvnw spring-boot:run
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