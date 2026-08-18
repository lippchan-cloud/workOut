## ADDED Requirements

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
