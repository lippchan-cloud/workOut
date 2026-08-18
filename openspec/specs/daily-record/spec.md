# daily-record

## Purpose

Current-user consume/intake records, including list-by-period, get-by-id for the detail page, and CSV export with point-in-time body columns.

## Requirements

### Requirement: User can get own record by id
An authenticated user SHALL retrieve a daily record they own via `GET /api/v1/dailyRecords/{id}`. Identity MUST come from the JWT. The response `data` MUST include `id`, `type`, `content`, and `recordedAt`. A missing, logically deleted, or other-user record MUST yield HTTP 404 with message 「记录不存在」 (MUST NOT leak existence). The lookup MUST be a single query by id and userId (no extra list scan).

#### Scenario: Owner gets consume record
- **GIVEN** user A created a CONSUME record with content `跑步`
- **WHEN** user A `GET /api/v1/dailyRecords/{id}`
- **THEN** response `code` is 200
- **AND** `data.content` is `跑步`
- **AND** `data.type` is `CONSUME`

#### Scenario: Cross-user get is 404
- **GIVEN** user A created a record
- **AND** user B is authenticated
- **WHEN** user B `GET /api/v1/dailyRecords/{id}`
- **THEN** response is HTTP 404
- **AND** message is 「记录不存在」

#### Scenario: Missing id is 404
- **GIVEN** user A is authenticated
- **WHEN** user A `GET /api/v1/dailyRecords/999999999`
- **THEN** response is HTTP 404
- **AND** message is 「记录不存在」

### Requirement: CSV export includes point-in-time body columns
`GET /api/v1/dailyRecords/exportCsv` MUST keep UTF-8 BOM and Chinese type labels. The header MUST be `记录时间,类型,内容,昵称,身高cm,体重kg`. Each data row MUST fill nickname, height, and weight from the user's profile history snapshot effective at that row's `recordedAt` (latest history with `changedAt <= recordedAt`). The exporter MUST load history once per export, not once per record. Empty period MUST still be header-only (new header). Current profile MUST NOT overwrite earlier days when a later change exists.

#### Scenario: Export header includes body columns and BOM
- **GIVEN** authenticated user exports a day with no records
- **WHEN** `GET /api/v1/dailyRecords/exportCsv?date=2026-08-18`
- **THEN** the file starts with UTF-8 BOM
- **AND** the header line is `记录时间,类型,内容,昵称,身高cm,体重kg`

#### Scenario: Rows align to history at recordedAt
- **GIVEN** user saved height `170` then created a record at T1 with content `早训`
- **AND** later saved height `180` then created a record at T2 with content `晚训`
- **WHEN** the user exports a period covering both records
- **THEN** the `早训` row contains `170`
- **AND** the `晚训` row contains `180`
- **AND** the `早训` row does not contain `180` as its height cell
