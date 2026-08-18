## MODIFIED Requirements

### Requirement: CSV export includes point-in-time body columns
`GET /api/v1/dailyRecords/exportCsv` MUST keep the same URL and period params. The download MUST be an Excel workbook (xlsx), not a text CSV: Content-Type MUST be the OpenXML spreadsheet MIME type and the filename MUST end with `.xlsx`. The workbook MUST contain sheet 「事项列表」 with header `记录时间,类型,内容,昵称,身高cm,体重kg` and rows aligned to profile history at each `recordedAt` (latest history with `changedAt <= recordedAt`; load history once, never per record). It MUST also contain sheet 「成长曲线」 with header `时间,身高cm,体重kg` and the user's body-history points. Empty period MUST still produce both sheets (事项列表 header-only). Current profile MUST NOT overwrite earlier days when a later change exists. Chinese type labels 消耗/摄入 remain. The current profile MUST have both height and weight; otherwise HTTP 400 「请先填写身高和体重」.

#### Scenario: Export header includes body columns and BOM
- **GIVEN** authenticated user has height and weight filled
- **AND** the user exports a day with no records
- **WHEN** `GET /api/v1/dailyRecords/exportCsv?date=2026-08-18`
- **THEN** the file is an xlsx workbook
- **AND** sheet 「事项列表」 header is `记录时间,类型,内容,昵称,身高cm,体重kg`
- **AND** sheet 「成长曲线」 exists with header `时间,身高cm,体重kg`

#### Scenario: Rows align to history at recordedAt
- **GIVEN** user has height and weight filled
- **AND** user saved height `170` then created a record at T1 with content `早训`
- **AND** later saved height `180` then created a record at T2 with content `晚训`
- **WHEN** the user exports a period covering both records
- **THEN** the `早训` row on 「事项列表」 contains `170`
- **AND** the `晚训` row contains `180`
- **AND** the `早训` row does not contain `180` as its height cell

## ADDED Requirements

### Requirement: Export requires current height and weight
Export MUST reject when the authenticated user's current profile is missing heightCm or weightKg. The API MUST return HTTP 400 with message 「请先填写身高和体重」 and MUST NOT return a workbook. The calendar UI MUST intercept the same condition before calling export, show a Chinese prompt, and guide the user to `/profile/body`.

#### Scenario: Export without height is 400
- **GIVEN** authenticated user has weight but no height
- **WHEN** `GET /api/v1/dailyRecords/exportCsv?date=2026-08-18`
- **THEN** response is HTTP 400
- **AND** message is 「请先填写身高和体重」

#### Scenario: Calendar intercepts incomplete profile before export
- **GIVEN** authenticated user is on `/calendar`
- **AND** profile has no height or no weight
- **WHEN** the user clicks 导出
- **THEN** the client does not download a workbook
- **AND** the user is guided to `/profile/body`
