## MODIFIED Requirements

### Requirement: Calendar tab has a trends level-2 page
Growth curves MUST NOT be required on the calendar month grid. The canonical curve lives on `/profile/body` under 我的. Level 1 remains 记录 / 日历 / 我的. `/calendar/trends` MUST NOT remain the main curve path (redirect to `/profile/body` is allowed).

#### Scenario: Trends is not on the month grid
- **GIVEN** authenticated user is on `/calendar` month mode
- **THEN** the month grid/list screen is not required to render a body-weight chart
- **AND** 变化曲线 is not a required navigation control on the calendar

#### Scenario: Trends empty state
- **GIVEN** authenticated user opens `/profile/body`
- **AND** `GET /api/v1/profile/trends` returns empty `bodyHistory` and empty `recordCounts`
- **THEN** the page shows a Chinese empty state for the curve
- **AND** it does not render a misleading chart as if data existed

## ADDED Requirements

### Requirement: Public report page is outside the three-tab shell
`/report/:id` MUST render as an independent page (like `/cms`), not inside the bottom-tab `AppShell`. Sections MUST appear in order: 用户名称, 事项列表, 成长曲线, then reserved 建议分析 (empty placeholder, no real medical advice).

#### Scenario: Report layout order
- **GIVEN** a visitor opens `/report/{id}` with a valid snapshot
- **THEN** 用户名称 appears above 事项列表
- **AND** 事项列表 appears above 成长曲线
- **AND** 建议分析 appears below as an empty placeholder
