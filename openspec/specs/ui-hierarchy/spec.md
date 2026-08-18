# ui-hierarchy

## Purpose

Define the three-level information architecture and button visual hierarchy of the workOut SPA.

## Requirements

### Requirement: App uses three-level information architecture
The SPA SHALL present a three-level hierarchy. Level 1 MUST be the bottom tabs 记录, 日历, 我的. Level 2 MUST be the tab's choice surface (record type picker, calendar week strip, profile option list). Level 3 MUST be a concrete form or read-only detail. Browser back from level 3 MUST return to that tab's level 2, not dump the user onto an unrelated tab.

#### Scenario: Record tab already follows the hierarchy
- **GIVEN** an authenticated user is on `/`
- **WHEN** the user clicks 开始记录 then 消耗
- **THEN** the consume form (level 3) is shown
- **AND** browser back returns to the type picker (level 2)

#### Scenario: Profile tab starts at level 2
- **GIVEN** an authenticated user opens `/profile`
- **THEN** the page shows three options 身体资料, 账号安全, 退出登录
- **AND** the page does not show height/weight inputs or password fields on the same view

### Requirement: Buttons follow primary secondary and text sizes
Interactive controls SHALL use three visual levels: primary CTA (solid consume-green, intake-red, or accent; min-height 44px), secondary (ghost/outline), and small text controls (week prev/next, inline text actions). The homepage 开始记录 control MUST remain a large hero button with refined radius and type size, and MUST NOT be styled as a small text control. Week prev/next MUST use the small text control style and MUST NOT use the same class/size as the homepage hero or primary block CTA.

#### Scenario: Week switchers are small
- **GIVEN** authenticated user is on the calendar day mode
- **THEN** 上一周 and 下一周 are rendered as small week-nav controls
- **AND** they do not have the `btn-record-hero` or `btn-block` class

#### Scenario: Record hero remains large
- **GIVEN** authenticated user is on `/`
- **THEN** 开始记录 is a large hero button
- **AND** it remains the primary entry into the record type picker

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
