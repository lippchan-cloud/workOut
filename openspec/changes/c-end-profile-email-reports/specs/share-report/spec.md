## ADDED Requirements

### Requirement: Owner can list own share reports
`GET /api/v1/shareReports` MUST require a Bearer JWT and MUST return only shares owned by that user, newest first. `data.list[]` items MUST include `id` (public token), `from`, `to`, `createdAt`. The payload MUST NOT include another user's shares or snapshot JSON. Missing token MUST yield HTTP 401. Loading MUST use a single query by userId, never a per-row loop. The SPA `/profile/reports` MUST render this list and link each row to `/report/{id}`. Empty list MUST show a Chinese empty state.

#### Scenario: Owner lists own reports
- **GIVEN** authenticated user `alice` has created a share with token `tokA`
- **WHEN** client `GET /api/v1/shareReports` with alice Bearer token
- **THEN** response `code` is 200
- **AND** `data.list` contains an item whose `id` is `tokA`

#### Scenario: Other user cannot see alice shares
- **GIVEN** user `bob` has a valid JWT and no shares
- **AND** `alice` owns share `tokA`
- **WHEN** client `GET /api/v1/shareReports` with bob Bearer token
- **THEN** response `code` is 200
- **AND** `data.list` does not contain `tokA`

#### Scenario: List without JWT is 401
- **WHEN** a client `GET /api/v1/shareReports` without Authorization
- **THEN** response is HTTP 401

#### Scenario: Profile reports page shows empty state
- **GIVEN** an authenticated user has no shares
- **WHEN** the user opens `/profile/reports`
- **THEN** the page shows a Chinese empty state
