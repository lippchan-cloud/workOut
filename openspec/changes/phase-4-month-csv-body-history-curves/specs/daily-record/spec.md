## ADDED Requirements

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
