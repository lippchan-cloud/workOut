# body-history

## Purpose

Persist body-profile change snapshots and resolve the snapshot that was effective at a given record time, including trends series without N+1 queries.

## Requirements

### Requirement: Profile changes are stored as history snapshots
When an authenticated user saves body profile fields, the system MUST persist a history row if nickname, height, or weight differs from the latest snapshot (including the first save). Each row MUST store `changedAt` (server time) and the post-save snapshot of nickname, heightCm, and weightKg. Identity MUST come from the JWT. Queries MUST load a user's history in one call (no per-row loops against the database).

#### Scenario: First save creates a snapshot
- **GIVEN** authenticated user has no history
- **WHEN** the user `PUT /api/v1/profile` with heightCm `175` and weightKg `70`
- **THEN** `GET /api/v1/profile/trends` returns one `bodyHistory` item
- **AND** that item has heightCm `175` and weightKg `70`

#### Scenario: Second save with a change appends a new snapshot
- **GIVEN** authenticated user already saved heightCm `175`
- **WHEN** the user `PUT /api/v1/profile` with heightCm `176`
- **THEN** `bodyHistory` contains two items in `changedAt` ascending order
- **AND** the later item has heightCm `176`

#### Scenario: Unchanged save does not append
- **GIVEN** authenticated user already saved heightCm `175` and weightKg `70`
- **WHEN** the user `PUT /api/v1/profile` with the same values
- **THEN** `bodyHistory` size remains 1

### Requirement: Trends endpoint returns body history and record counts without N+1
An authenticated user SHALL retrieve `GET /api/v1/profile/trends`. Response `data` MUST include `bodyHistory` (snapshots ascending by `changedAt`) and `recordCounts` (Shanghai calendar dates with counts of the user's non-deleted daily records). Record counts MUST come from one grouped query, not one query per day. Missing token MUST yield HTTP 401. Other users' data MUST NOT appear.

#### Scenario: Owner sees own history and counts
- **GIVEN** user A has a profile snapshot and two records on `2026-08-18`
- **WHEN** user A `GET /api/v1/profile/trends`
- **THEN** response `code` is 200
- **AND** `bodyHistory` is non-empty
- **AND** `recordCounts` includes date `2026-08-18` with count `2`

#### Scenario: Trends without token is 401
- **WHEN** a client `GET /api/v1/profile/trends` without Authorization
- **THEN** response is HTTP 401

#### Scenario: Cross-user isolation
- **GIVEN** user A has history and records
- **AND** user B is authenticated with empty profile and no records
- **WHEN** user B `GET /api/v1/profile/trends`
- **THEN** `bodyHistory` does not contain user A's nickname or height
- **AND** `recordCounts` does not include user A's dates

### Requirement: Point-in-time body snapshot is the latest history at or before a timestamp
Resolving "body at recordedAt" MUST select the latest history row whose `changedAt` is less than or equal to that timestamp. If none exists, nickname, height, and weight are empty. Matching MUST happen in memory after at most one history load for the user, never one database call per daily record.

#### Scenario: Later change does not rewrite earlier records
- **GIVEN** user saved height `170` then created a record at time T1
- **AND** later saved height `180` then created a record at time T2
- **WHEN** body snapshots are resolved for those records
- **THEN** the T1 record matches height `170`
- **AND** the T2 record matches height `180`
