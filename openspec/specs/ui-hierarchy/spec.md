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
Under the calendar Tab, 「变化曲线」 MUST be a level-2 page (`/calendar/trends`), not a widget inside the month grid. Level 1 remains 记录 / 日历 / 我的. Browser or in-page back from trends MUST return to the calendar list surface, not to 记录 or 我的.

#### Scenario: Trends is not on the month grid
- **GIVEN** authenticated user is on `/calendar` month mode
- **THEN** the month grid/list screen is not required to render a body-weight chart
- **AND** 变化曲线 is a navigation control to `/calendar/trends`

#### Scenario: Trends empty state
- **GIVEN** authenticated user opens `/calendar/trends`
- **AND** `GET /api/v1/profile/trends` returns empty `bodyHistory` and empty `recordCounts`
- **THEN** the page shows a Chinese empty state
- **AND** it does not render a misleading chart as if data existed
