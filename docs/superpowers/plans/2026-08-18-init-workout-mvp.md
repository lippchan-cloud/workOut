# init-workout-mvp Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.
>
> **Source of truth for requirements:** `openspec/changes/init-workout-mvp/` (proposal / specs / design / tasks). Product narrative also in `doc/`.
>
> **This session instruction:** Documentation-only phase is complete. Do **not** write production code until the human explicitly starts `/opsx:apply` or asks to implement.

**Goal:** Build the workOut MVP monolith (JWT auth, three tabs, daily consume/intake records, calendar, CSV export, profile) using OpenSpec specs and strict TDD.

**Architecture:** React (Vite) SPA hosted by Spring Boot static resources; REST `/api/v1`; MySQL; JWT filter; all business rows scoped by `userId`.

**Tech Stack:** Java 17+/Spring Boot 3, JPA + Flyway, Spring Security + JJWT, React + TypeScript + Vitest, MySQL 8 utf8mb4.

## Global Constraints

- TDD: no production code without a failing test first (see OpenSpec `tasks.md`).
- API envelope `{ code, msg, data }`; POST fields under `request`.
- Never trust client-supplied `userId`.
- Colors: CONSUME `#16A34A`, INTAKE `#DC2626`.
- Timezone `Asia/Shanghai`; week starts Monday.
- Out of scope: OAuth, email verify, edit/delete records, BMI advice, microservices.

## File Map (to create during apply)

```
backend/
  pom.xml
  src/main/java/com/workout/...
  src/main/resources/application.yml
  src/main/resources/db/migration/V1__init.sql
  src/test/java/com/workout/...
frontend/
  package.json
  src/App.tsx
  src/pages/{Login,Register,Record,Calendar,Profile}.tsx
  src/auth/...
  src/**/*.test.tsx
openspec/changes/init-workout-mvp/tasks.md  # checkboxes during apply
doc/  # already written
```

## Execution

Follow `openspec/changes/init-workout-mvp/tasks.md` sections 1→8 in order. Each checkbox is a TDD cycle.

After all tasks: `openspec validate init-workout-mvp` then archive via `/opsx:archive` when human confirms.

---

**Plan complete pointer:** OpenSpec change `init-workout-mvp` holds the detailed task checklist. Prefer applying from that file rather than duplicating every test body here.
