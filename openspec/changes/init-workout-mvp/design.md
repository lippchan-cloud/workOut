## Context

Greenfield MVP for workOut. Product/functional/architecture docs in `doc/` (v1.1) already define behavior including JWT auth and per-user data isolation. Repository has no application source yet. This design turns those docs + OpenSpec delta specs into a buildable monolith with mandatory TDD.

Constraints:
- React + Spring Boot + MySQL, not separated deployables
- CLI one-command start
- JWT (not session cookie)
- Asia/Shanghai; week starts Monday
- No email verification / OAuth / calorie features

## Goals / Non-Goals

**Goals:**
- Deliver runnable MVP covering auth, shell, records, calendar, CSV, profile
- Enforce user isolation at API and DB query layer
- Establish Red → Green → Refactor workflow with automated tests as the definition of done for each task

**Non-Goals:**
- Microservices, Redis, message queues
- Record edit/delete, BMI medical advice, social sharing
- Production-grade secrets management beyond local config
- Pixel-perfect design system

## Decisions

### D1: Monorepo layout (Maven parent + frontend module or `frontend/` + `backend/`)

**Choice:** `backend/` Spring Boot Maven project + `frontend/` React (Vite) app; backend `frontend-maven-plugin` or copy `frontend/dist` into `backend/src/main/resources/static` on build.

**Alternatives:** Single Gradle multi-project; CRA. Rejected for simplicity and Vite speed.

**Rationale:** Clear FE/BE test boundaries; one jar/process for CLI UX.

### D2: Persistence

**Choice:** Spring Data JPA + Flyway (or `schema.sql` for MVP) with entities `User`, `DailyRecord`, `Profile`.

**Alternatives:** MyBatis-Plus (aligned with some team skills). Acceptable swap if implementer prefers; queries must still filter by `userId` and forbid N+1 loops.

### D3: Security

**Choice:** Stateless JWT (HMAC), filter once per request; password `BCryptPasswordEncoder`; register/login public; all other `/api/v1/**` authenticated.

**Alternatives:** Session cookie (simpler browser, worse SPA story). Rejected per product decision.

### D4: API envelope

**Choice:** `{ code, msg, data }` with `requestId`/`timestamp`; business validation → HTTP 400; auth failure → HTTP 401; POST bodies wrap fields in `request` per functional doc.

### D5: Frontend auth state

**Choice:** Token in `localStorage`; axios/fetch interceptor attaches Bearer; response 401 → clear + navigate `/login`; route guard on Tab click and protected data loads.

### D6: CSV export

**Choice:** Backend generates UTF-8 BOM CSV stream (recommended in functional doc) so Excel and type labels stay consistent.

### D7: TDD strategy

**Choice:**
1. Backend first for each capability: failing MockMvc/service test → implement → green
2. Frontend: Vitest + Testing Library for auth redirect, form validation messages, calendar color classes
3. No production code merged for a task until its named tests pass
4. Prefer real Spring context + Testcontainers MySQL (or H2 only if Testcontainers blocked); document chosen approach in README

**Package sketch (backend):**
```
com.workout
  WorkOutApplication
  config (Security, Jwt, Web)
  auth (controller, service, jwt)
  record (controller, service, entity, repo)
  profile (...)
  common (ApiResponse, exceptions)
```

**Table sketch:**
- `user(id, username UNIQUE, password_hash, created_at)`
- `daily_record(id, user_id, type, content, recorded_at, created_at)`
- `profile(id, user_id UNIQUE, nickname, height_cm, weight_kg, updated_at)`

## Risks / Trade-offs

| Risk | Mitigation |
| --- | --- |
| JWT secret leaked in repo | Use env/`application-local.yml` gitignored; sample in `application-example.yml` |
| SPA deep-link 404 behind Spring | Add controller forward to `index.html` for non-API routes |
| Clock/timezone flakiness in tests | Fix clock with `Clock` bean / use explicit Instant in tests |
| Over-scoping UI polish | Specs define behavior only; minimal CSS acceptable for MVP |
| Skipping TDD under time pressure | tasks.md checkboxes require RED evidence (fail output) before GREEN |

## Migration Plan

1. Apply Flyway/SQL on empty MySQL schema `workout`
2. Build frontend → copy into static → `spring-boot:run`
3. No production data to migrate (greenfield)
4. Rollback: drop schema / revert commit; no dual-write period

## Open Questions

- Prefer JPA vs MyBatis-Plus for this repo? Default **JPA** unless implementer standardizes on MyBatis-Plus in Task 1.
- Token TTL default: **7 days** unless product asks otherwise.
- Register response: **return JWT immediately** (recommended in functional doc).
