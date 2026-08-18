## MODIFIED Requirements

### Requirement: Calendar offers a trends secondary page
The calendar Tab MUST NOT keep 「变化曲线」 as the primary path. Growth curves MUST live on `/profile/body`. `/calendar/trends` MAY redirect to `/profile/body`. The bottom Tab 「日历」 remains calendar-level primary navigation. The calendar MUST offer 导出 and 分享 for the current filter range. 分享 MUST use the same height/weight gate as export.

#### Scenario: Open trends from calendar
- **GIVEN** authenticated user is on `/calendar`
- **THEN** the calendar does not show a primary 变化曲线 button that stays on the calendar tab
- **AND** 导出 is visible
- **AND** 分享 is visible

#### Scenario: Back from trends returns to calendar
- **GIVEN** authenticated user had bookmarked `/calendar/trends`
- **WHEN** the client opens that URL
- **THEN** the user is taken to `/profile/body` (redirect) or the calendar no longer hosts the curve as its main path

## ADDED Requirements

### Requirement: Calendar share copies or shows an H5 link
Clicking 分享 on the calendar MUST call `POST /api/v1/shareReports` with the current filter. On success the UI MUST present the returned `url` (copyable). Incomplete height/weight MUST intercept like export and guide to `/profile/body` without creating a share.

#### Scenario: Share with complete profile shows url
- **GIVEN** authenticated user has height and weight
- **WHEN** the user clicks 分享 on `/calendar`
- **THEN** the client posts a share for the current range
- **AND** the returned url is shown

#### Scenario: Share intercepts incomplete profile
- **GIVEN** authenticated user is on `/calendar`
- **AND** profile has no height or no weight
- **WHEN** the user clicks 分享
- **THEN** the client does not create a share
- **AND** the user is guided to `/profile/body`
