## MODIFIED Requirements

### Requirement: Public report page is outside the three-tab shell
`/report/:id` MUST render as an independent page (like `/cms`), not inside the bottom-tab `AppShell`. Sections MUST appear in order: 用户名称, 事项列表, 成长曲线, then reserved 建议分析 (empty placeholder, no real medical advice). The page MUST include 「回首页」 linking to `/`.

#### Scenario: Report layout order
- **GIVEN** a visitor opens `/report/{id}` with a valid snapshot
- **THEN** 用户名称 appears above 事项列表
- **AND** 事项列表 appears above 成长曲线
- **AND** 建议分析 appears below as an empty placeholder

#### Scenario: Report has home link
- **GIVEN** a visitor opens `/report/{id}`
- **THEN** 回首页 is visible
- **AND** it navigates to `/`

## ADDED Requirements

### Requirement: Calendar share is a level-2 page
Creating a share MUST happen on `/calendar/share` (calendar Tab, level 2), not as an inline flash on the calendar home. Browser back / 返回日历 MUST return to `/calendar` (with prior filter query when present). 分享 and 导出 on the calendar home MUST be equal visual weight (secondary/ghost), not one primary-block and one ghost.

#### Scenario: Share is not inline on calendar home
- **GIVEN** authenticated user is on `/calendar`
- **WHEN** the user clicks 分享 (with complete height and weight)
- **THEN** the URL is `/calendar/share` (plus filter query)
- **AND** the share URL is not shown as an inline flash on the calendar home

#### Scenario: Share and export are equal weight
- **GIVEN** authenticated user is on `/calendar`
- **THEN** 分享 and 导出 use the same secondary/ghost block styling
