## MODIFIED Requirements

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
