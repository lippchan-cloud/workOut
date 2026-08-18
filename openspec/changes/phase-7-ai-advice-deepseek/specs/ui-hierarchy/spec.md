## MODIFIED Requirements

### Requirement: Public report page is outside the three-tab shell
`/report/:id` MUST render as an independent page (like `/cms`), not inside the bottom-tab `AppShell`. Sections MUST appear in order: 用户名称, 事项列表, 成长曲线, then 建议分析. 建议分析 MUST reflect `adviceStatus`: 「未配置 API Key」, 「生成中」, generated text, or failure copy — not a permanent empty medical placeholder. The page MUST include 「回首页」 linking to `/`.

#### Scenario: Report layout order
- **GIVEN** a visitor opens `/report/{id}` with a valid snapshot
- **THEN** 用户名称 appears above 事项列表
- **AND** 事项列表 appears above 成长曲线
- **AND** 建议分析 appears below the curve

#### Scenario: Report advice generating
- **GIVEN** a visitor opens `/report/{id}` with `adviceStatus` `PENDING`
- **THEN** 建议分析 shows 「生成中」

#### Scenario: Report has home link
- **GIVEN** a visitor opens `/report/{id}`
- **THEN** 回首页 is visible
- **AND** it navigates to `/`

### Requirement: CMS deep links stay outside the three-tab shell
CMS routes `/cms`, `/cms/accounts`, `/cms/users/:userId`, `/cms/reports`, `/cms/api-keys`, and `/cms/ai-calls` MUST render outside `AppShell`. Refreshing those paths MUST still serve SPA HTML (not an API 404). The bottom tabs 记录 / 日历 / 我的 MUST NOT appear on CMS pages.

#### Scenario: CMS accounts deep link serves SPA
- **GIVEN** the SPA is served by the Spring Boot static + fallback setup
- **WHEN** the browser requests `GET /cms/accounts`
- **THEN** the response is the SPA HTML document
- **AND** it is not an API JSON 404 body

#### Scenario: CMS api-keys deep link serves SPA
- **GIVEN** the SPA is served by the Spring Boot static + fallback setup
- **WHEN** the browser requests `GET /cms/api-keys`
- **THEN** the response is the SPA HTML document
