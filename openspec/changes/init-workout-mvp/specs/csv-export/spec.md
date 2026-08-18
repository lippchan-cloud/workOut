## ADDED Requirements

### Requirement: Export selected day as CSV for current user
An authenticated user SHALL export the currently selected calendar date as a downloadable CSV named `workout-YYYY-MM-DD.csv`. Encoding MUST be UTF-8 with BOM. Columns MUST be `记录时间`, `类型`, `内容` with types shown as 「消耗」/「摄入」. Row order MUST match the day list (time ascending).

#### Scenario: Export day with data
- **GIVEN** authenticated user has records on 2026-08-18
- **WHEN** client `GET /api/v1/dailyRecords/exportCsv?date=2026-08-18` with Bearer token
- **THEN** response is a CSV file download
- **AND** filename is `workout-2026-08-18.csv`
- **AND** header row equals `记录时间,类型,内容`
- **AND** file begins with UTF-8 BOM
- **AND** only the current user's rows are included

#### Scenario: Export empty day
- **GIVEN** authenticated user has no records on date D
- **WHEN** export is requested for D
- **THEN** CSV contains only the header row
- **AND** the UI may show 「当日暂无记录，已导出空表」

### Requirement: Export requires authentication
Unauthenticated export requests MUST be rejected with HTTP 401. The SPA SHALL redirect to login when export is clicked without a token.

#### Scenario: Export without token
- **WHEN** export API is called without Authorization
- **THEN** response is HTTP 401
