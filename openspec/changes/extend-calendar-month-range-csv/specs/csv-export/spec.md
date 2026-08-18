## ADDED Requirements

### Requirement: Export CSV uses the same period as the visible list
When the user clicks 「导出 CSV」 on the Calendar page, the SPA MUST call `GET /api/v1/dailyRecords/exportCsv` with the same mutually exclusive query parameters as the current list request (`date`, or `yearMonth`, or `from` and `to`). Encoding MUST remain UTF-8 with BOM. Columns MUST remain `记录时间`, `类型`, `内容` with types 「消耗」/「摄入」. Row order MUST match the list for that period. Only the current user's rows MUST be included. Unauthenticated clicks MUST redirect to `/login?redirect=/calendar`.

#### Scenario: Export current month
- **GIVEN** authenticated user is in month mode with `yearMonth=2026-08`
- **AND** the user has records in August 2026
- **WHEN** client `GET /api/v1/dailyRecords/exportCsv?yearMonth=2026-08` with Bearer token
- **THEN** response is a CSV file download
- **AND** filename is `workout-2026-08.csv`
- **AND** file begins with UTF-8 BOM
- **AND** header row equals `记录时间,类型,内容`
- **AND** only August rows for the current user are included

#### Scenario: Export custom range
- **GIVEN** authenticated user selected from `2026-08-01` to `2026-08-18`
- **WHEN** client `GET /api/v1/dailyRecords/exportCsv?from=2026-08-01&to=2026-08-18` with Bearer token
- **THEN** filename is `workout-2026-08-01_2026-08-18.csv`
- **AND** rows are those in the inclusive range for the current user

#### Scenario: Export same-day range uses daily filename
- **WHEN** export is requested with `from=2026-08-18&to=2026-08-18`
- **THEN** filename is `workout-2026-08-18.csv`

#### Scenario: Month mode UI download uses yearMonth
- **GIVEN** authenticated user on `/calendar` in 按月 with month `2026-08`
- **WHEN** the user clicks 「导出 CSV」
- **THEN** the browser download is triggered
- **AND** the request URL includes `yearMonth=2026-08` and does not include `date`

### Requirement: Empty period still exports header-only CSV
Exporting a month or range with no rows MUST succeed and contain only the header row (plus BOM). Day-mode empty export behavior MUST remain header-only.

#### Scenario: Export empty month
- **GIVEN** authenticated user has no records in 2026-02
- **WHEN** export is requested for `yearMonth=2026-02`
- **THEN** CSV body (excluding BOM) is only `记录时间,类型,内容`

### Requirement: Period export validates the same as list
`exportCsv` MUST reject mixed or invalid period parameters with HTTP 400 using the same rules as the list API. Missing Authorization MUST remain HTTP 401.

#### Scenario: Export mixing date and yearMonth
- **WHEN** client `GET /api/v1/dailyRecords/exportCsv?date=2026-08-18&yearMonth=2026-08` with Bearer token
- **THEN** response is HTTP 400

#### Scenario: Export without token still 401
- **WHEN** export is called with `yearMonth=2026-08` and no Authorization
- **THEN** response is HTTP 401
