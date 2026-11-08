# La Ruta 🍺

A web app to save your group's favourite tapas bars, rate them together, and find in seconds which bar serves which tapa.

[![Live app](https://img.shields.io/badge/Live_app-000000?logo=vercel&logoColor=white)](https://la-ruta-sigma.vercel.app)
[![Backend CI](https://github.com/idoceb00/la-ruta/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/idoceb00/la-ruta/actions/workflows/backend-ci.yml)

![Angular](https://img.shields.io/badge/Angular_22-DD0031?logo=angular&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS_v4-06B6D4?logo=tailwindcss&logoColor=white)
![Java](https://img.shields.io/badge/Java_21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_4-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL_17-4169E1?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white)
![PWA](https://img.shields.io/badge/PWA-5A0FC8?logo=pwa&logoColor=white)

> The backend runs on Render's free plan and sleeps when idle, so the first load may take a moment while it wakes up.

## Why

With so many bars and tapas around, it's easy to forget what each place serves or what it was like.
When you're halfway through a night out and someone asks *"where did they serve morcilla?"*, La Ruta answers from your phone in a few seconds.

## Features

- **Communities:** private groups joined with an invite code. Each group has its own bars.
- **Bars and tapas:** any member can add and edit bars and the tapas they serve.
- **Reviews:** members rate bars and tapas with stars and notes; the app shows the group's average.
- **Search:** find a bar, or find every bar that serves a given tapa.
- **Installable:** works as a PWA on your phone.

## Architecture

```mermaid
flowchart LR
    A[Angular PWA<br/>Vercel] -->|REST + JWT| B[Spring Boot API<br/>Render]
    B --> C[(PostgreSQL<br/>Neon)]
```

### Data model

```mermaid
erDiagram
    USERS ||--o{ MEMBERSHIPS : "belongs to"
    COMMUNITIES ||--|{ MEMBERSHIPS : has
    COMMUNITIES ||--o{ BARS : contains
    BARS ||--o{ BAR_REVIEWS : "reviewed in"
    USERS ||--o{ BAR_REVIEWS : writes
    BARS ||--o{ BAR_TAPAS : serves
    TAPAS ||--o{ BAR_TAPAS : "served at"
    BAR_TAPAS ||--o{ TAPA_REVIEWS : "reviewed in"
    USERS ||--o{ TAPA_REVIEWS : writes

    USERS {
        bigint id PK
        text username UK
        text password_hash
    }
    COMMUNITIES {
        bigint id PK
        text name
        text invite_code UK
    }
    MEMBERSHIPS {
        bigint id PK
        bigint user_id FK
        bigint community_id FK
        text role "ADMIN | MEMBER"
    }
    BARS {
        bigint id PK
        bigint community_id FK
        text name
        text city
        text address
        text zone
        bigint rating_sum
        bigint rating_count
        bigint version
    }
    BAR_REVIEWS {
        bigint id PK
        bigint bar_id FK
        bigint user_id FK
        int rating "0-10"
        text notes
    }
    TAPAS {
        bigint id PK
        text name UK
    }
    BAR_TAPAS {
        bigint id PK
        bigint bar_id FK
        bigint tapa_id FK
        bigint rating_sum
        bigint rating_count
        bigint version
    }
    TAPA_REVIEWS {
        bigint id PK
        bigint bar_tapa_id FK
        bigint user_id FK
        int rating "0-5"
        text notes
        boolean fav
    }
```

Every table also has audit columns (`created_at`, `updated_at`, `created_by`, `modified_by`).
A user has at most one review per bar and per bar tapa, and a tapa appears at most once per bar.

## Technical decisions

- **Community-scoped authorization.** Being logged in is not enough: every operation checks that the user belongs to the resource's community.
- **Concurrent ratings.** Averages are stored precomputed and protected with optimistic locking (`@Version`). Ratings retry automatically on conflict; bar edits don't, so the 409 reaches the user instead of silently overwriting another member's changes.
- **Flyway owns the schema.** Migrations are the single source of truth; Hibernate only validates.
- **Shared tapa catalog.** Tapas are added to a bar by name and reused across communities, which powers autocomplete and "where is X?" searches.
- **Cold starts handled in the UI.** A wake-up screen pings the backend before loading the app, instead of showing errors.
- **Agent-assisted development.** The repo includes an `AGENTS.md` with conventions and limits, plus commands to plan features and prepare commits.

## Testing

- **Backend:** integration tests run against a real PostgreSQL 17 through Testcontainers, not an in-memory database. They run in GitHub Actions with `./mvnw verify`.
- **Frontend:** unit tests with Vitest.

## Project structure

```
la-ruta/
├── backend/             Spring Boot REST API
├── frontend/            Angular PWA
├── AGENTS.md            Rules and conventions for coding agents
└── docker-compose.yml   Local PostgreSQL
```

## Running locally

Requirements: Java 21, Node with pnpm, and Docker.

```bash
cp .env.example .env        # then set JWT_SECRET (openssl rand -base64 32)
docker compose up -d        # PostgreSQL

cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev   # Swagger UI at /swagger-ui.html

cd ../frontend
pnpm install
pnpm start                  # http://localhost:4200
```

## Roadmap

- [ ] Demo account, so anyone can try the app without registering
- [ ] Welcome screen for logged-out users
- [ ] Open Graph preview when sharing the link
- [ ] Accent-insensitive search