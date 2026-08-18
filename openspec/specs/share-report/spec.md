# share-report

## Purpose

Create a read-only H5 report for a calendar filter range, identified by a random token, readable without login.

## Requirements

### Requirement: User can create a shareable report for the current calendar range
An authenticated user SHALL create a read-only share via `POST /api/v1/shareReports` using the same mutually exclusive period params as list/export (`date` / `yearMonth` / `from`+`to`). Identity MUST come from the JWT. The current profile MUST have both heightCm and weightKg; otherwise the API MUST return HTTP 400 with message 「请先填写身高和体重」 and MUST NOT persist a share. On success `data` MUST include `id` (random token, not a sequential integer as the public identifier) and `url` equal to `{publicBaseUrl}/report/{id}` where `publicBaseUrl` comes from `WORKOUT_PUBLIC_BASE_URL` (default `http://localhost:8080`). Creating MUST snapshot display name, in-range records, and body-history points in one load path (no N+1). Missing token MUST yield HTTP 401.

#### Scenario: Create share returns non-sequential id and configured url
- **GIVEN** authenticated user has height and weight filled
- **AND** public base URL is `http://localhost:8080`
- **WHEN** the user `POST /api/v1/shareReports?date=2026-08-18`
- **THEN** response `code` is 200
- **AND** `data.id` is a non-empty token that is not a short incrementing decimal id
- **AND** `data.url` is `http://localhost:8080/report/{data.id}`

#### Scenario: Share without height or weight is 400
- **GIVEN** authenticated user has no height or no weight on current profile
- **WHEN** the user `POST /api/v1/shareReports?date=2026-08-18`
- **THEN** response is HTTP 400
- **AND** message is 「请先填写身高和体重」

#### Scenario: Share without JWT is 401
- **WHEN** a client `POST /api/v1/shareReports?date=2026-08-18` without Authorization
- **THEN** response is HTTP 401

### Requirement: Public report is readable by token without login
`GET /api/v1/reports/{id}` MUST be publicly accessible (no JWT). `{id}` is the share token. Response `data` MUST include `from`, `to`, `displayName`, `records`, `bodyHistory`, and `advice`. `advice` MUST be null or absent of medical content (placeholder only). Unknown token MUST yield HTTP 404 with message 「报告不存在」. The payload MUST NOT include another user's records. SPA route `/report/:id` MUST render without the three-tab shell. The report page MUST offer a control 「回首页」 that navigates to `/`. The records list MUST not squeeze time and content onto one cramped line (stack or spaced columns); CONSUME green / INTAKE red remain.

#### Scenario: Anonymous get by token returns snapshot
- **GIVEN** a share exists with displayName `小明` and a CONSUME record `跑步` in range
- **WHEN** an unauthenticated client `GET /api/v1/reports/{id}`
- **THEN** response `code` is 200
- **AND** `data.displayName` is `小明`
- **AND** `data.records` contains content `跑步`
- **AND** `data.from` and `data.to` are present
- **AND** `data.advice` is null or empty

#### Scenario: Unknown report id is 404
- **WHEN** a client `GET /api/v1/reports/does-not-exist`
- **THEN** response is HTTP 404
- **AND** message is 「报告不存在」

#### Scenario: Report page is outside the tab shell
- **GIVEN** a visitor opens `/report/{id}`
- **THEN** the page shows 用户名称, 事项列表, 成长曲线, and 建议分析
- **AND** the bottom tabs 记录 / 日历 / 我的 are not shown

#### Scenario: Report page can go home
- **GIVEN** a visitor opens `/report/{id}`
- **THEN** a 回首页 control is visible
- **AND** activating it navigates to `/`

#### Scenario: Report record list is not cramped
- **GIVEN** a visitor opens `/report/{id}` with a CONSUME record `跑步`
- **THEN** the list shows time and content on separate lines or spaced columns
- **AND** the row remains consume-green
