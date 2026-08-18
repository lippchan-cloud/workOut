## MODIFIED Requirements

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
