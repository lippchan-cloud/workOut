## ADDED Requirements

### Requirement: List records by calendar month for current user
An authenticated user SHALL list the current user's records whose `recordedAt` falls in the Asia/Shanghai calendar month given by `yearMonth=YYYY-MM`. The response envelope MUST be `{ code, msg, data }` with `code` 200. `data.list` MUST be ordered by `recordedAt` ascending then `id` ascending. `data` MUST include `yearMonth`, inclusive `from` (first day of month) and inclusive `to` (last day of month). The query MUST use a single repository call filtered by JWT `userId` (no per-day loop).

#### Scenario: List August 2026 records
- **GIVEN** authenticated user has CONSUME on 2026-08-01 07:30 and INTAKE on 2026-08-31 21:00 Asia/Shanghai
- **AND** has a record on 2026-09-01 00:30 Asia/Shanghai
- **WHEN** client `GET /api/v1/dailyRecords?yearMonth=2026-08` with Bearer token
- **THEN** response `code` is 200
- **AND** `data.yearMonth` is `2026-08`
- **AND** `data.from` is `2026-08-01` and `data.to` is `2026-08-31`
- **AND** `data.list` contains the August consume and intake in time order
- **AND** `data.list` does not contain the September record

#### Scenario: Empty month
- **GIVEN** authenticated user has no records in 2026-02
- **WHEN** client queries `yearMonth=2026-02`
- **THEN** `data.list` is an empty array
- **AND** `data.from` is `2026-02-01` and `data.to` is `2026-02-28`

### Requirement: List records by inclusive date range for current user
An authenticated user SHALL list records in the inclusive Asia/Shanghai date range `from` and `to` (both `YYYY-MM-DD`). Interval MUST be `[from 00:00, to+1 00:00)`. Ordering, isolation, and single-query rules MUST match month listing. `data` MUST include `from` and `to`.

#### Scenario: Custom range across two days
- **GIVEN** authenticated user has a record on 2026-08-17 and one on 2026-08-19
- **WHEN** client `GET /api/v1/dailyRecords?from=2026-08-17&to=2026-08-18` with Bearer token
- **THEN** `data.list` includes the 17th record
- **AND** `data.list` does not include the 19th record
- **AND** `data.from` is `2026-08-17` and `data.to` is `2026-08-18`

#### Scenario: Same-day range equals single-day list
- **GIVEN** authenticated user has two records on 2026-08-18
- **WHEN** client queries `from=2026-08-18&to=2026-08-18`
- **THEN** `data.list` length is 2
- **AND** order matches `GET /api/v1/dailyRecords?date=2026-08-18`

### Requirement: Period query parameters are mutually exclusive and validated
List (and the same rules MUST apply to export) SHALL accept exactly one of: `date`; `yearMonth`; both `from` and `to`. Mixing modes, supplying only `from` or only `to`, `from` after `to`, or an inclusive span longer than 366 days MUST return HTTP 400 with a Chinese `msg`. Existing `date`-only requests MUST keep working.

#### Scenario: Mixing date and yearMonth is rejected
- **WHEN** client `GET /api/v1/dailyRecords?date=2026-08-18&yearMonth=2026-08` with Bearer token
- **THEN** response is HTTP 400
- **AND** `msg` indicates only one filter may be used

#### Scenario: from after to is rejected
- **WHEN** client queries `from=2026-08-20&to=2026-08-18` with Bearer token
- **THEN** response is HTTP 400
- **AND** `msg` indicates the start date must not be after the end date

#### Scenario: range longer than 366 days is rejected
- **WHEN** client queries `from=2025-01-01&to=2026-12-31` with Bearer token
- **THEN** response is HTTP 400
- **AND** `msg` indicates the range cannot exceed 366 days

#### Scenario: date-only query still works
- **GIVEN** authenticated user has records on 2026-08-18
- **WHEN** client `GET /api/v1/dailyRecords?date=2026-08-18` with Bearer token
- **THEN** response is HTTP 200
- **AND** `data.date` is `2026-08-18`
- **AND** `data.list` contains those records

### Requirement: Period queries remain isolated per user
Month and range queries MUST return only rows whose `user_id` equals the JWT user. Other users' rows MUST NOT appear.

#### Scenario: Other user's August record hidden
- **GIVEN** user A has a record in 2026-08
- **AND** user B is authenticated with no August records
- **WHEN** user B queries `yearMonth=2026-08`
- **THEN** `data.list` is empty
