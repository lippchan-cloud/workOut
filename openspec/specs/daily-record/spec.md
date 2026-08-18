# daily-record

## Purpose

Current-user consume/intake records, including list-by-period, get-by-id for the detail page, and xlsx export with a records sheet (no body columns) plus a growth-curve sheet.

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
`GET /api/v1/dailyRecords/exportCsv` MUST keep the same URL and period params. The download MUST be an Excel workbook (xlsx), not a text CSV: Content-Type MUST be the OpenXML spreadsheet MIME type and the filename MUST end with `.xlsx`. The workbook MUST contain sheet 「事项列表」 with header `记录时间,类型,内容` only (MUST NOT include nickname, height, or weight columns). It MUST also contain sheet 「成长曲线」 with header `时间,身高cm,体重kg` and the user's body-history points (body data lives only on this sheet). Empty period MUST still produce both sheets (事项列表 header-only). Chinese type labels 消耗/摄入 remain. The current profile MUST have both height and weight; otherwise HTTP 400 「请先填写身高和体重」. History MAY still be loaded once for the curve sheet (never per record).

#### Scenario: Export header includes body columns and BOM
- **GIVEN** authenticated user has height and weight filled
- **AND** the user exports a day with no records
- **WHEN** `GET /api/v1/dailyRecords/exportCsv?date=2026-08-18`
- **THEN** the file is an xlsx workbook
- **AND** sheet 「事项列表」 header is `记录时间,类型,内容`
- **AND** sheet 「事项列表」 header does not include `身高cm` or `昵称`
- **AND** sheet 「成长曲线」 exists with header `时间,身高cm,体重kg`

#### Scenario: Rows align to history at recordedAt
- **GIVEN** user has height and weight filled
- **AND** user saved height `170` then created a record at T1 with content `早训`
- **AND** later saved height `180` then created a record at T2 with content `晚训`
- **WHEN** the user exports a period covering both records
- **THEN** the `早训` row on 「事项列表」 does not contain a height column value `170`
- **AND** the `晚训` row on 「事项列表」 does not contain a height column value `180`
- **AND** sheet 「成长曲线」 contains `170` and `180`

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
