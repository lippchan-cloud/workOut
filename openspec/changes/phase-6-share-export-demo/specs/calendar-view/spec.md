## MODIFIED Requirements

### Requirement: Calendar offers a trends secondary page
The calendar Tab MUST NOT keep 「变化曲线」 as the primary path. Growth curves MUST live on `/profile/body`. `/calendar/trends` MAY redirect to `/profile/body`. The bottom Tab 「日历」 remains calendar-level primary navigation. The calendar MUST offer 导出 and 分享 for the current filter range as **equal-weight** actions (same visual class, e.g. both `btn btn-ghost btn-block`). 分享 MUST use the same height/weight gate as export. Clicking 分享 MUST navigate to `/calendar/share` with the current filter query (`date` / `yearMonth` / `from`&`to`) and MUST NOT POST on the calendar home. The calendar home MUST NOT inline a generated share URL.

#### Scenario: Open trends from calendar
- **GIVEN** authenticated user is on `/calendar`
- **THEN** the calendar does not show a primary 变化曲线 button that stays on the calendar tab
- **AND** 导出 is visible
- **AND** 分享 is visible
- **AND** 分享 and 导出 share the same button class for equal weight

#### Scenario: Back from trends returns to calendar
- **GIVEN** authenticated user had bookmarked `/calendar/trends`
- **WHEN** the client opens that URL
- **THEN** the user is taken to `/profile/body` (redirect) or the calendar no longer hosts the curve as its main path

#### Scenario: Share opens a secondary page
- **GIVEN** authenticated user has height and weight
- **WHEN** the user clicks 分享 on `/calendar`
- **THEN** the client navigates to `/calendar/share` with the current filter query
- **AND** the calendar home does not POST `/api/v1/shareReports`

### Requirement: Calendar share copies or shows an H5 link
Clicking 分享 on the calendar MUST open `/calendar/share` (not create the share on the home). The share secondary page MUST call `POST /api/v1/shareReports` with the filter query from the URL. On success the secondary page MUST present the returned `url` and a copy action. Incomplete height/weight MUST intercept like export and guide to `/profile/body` without opening the secondary page or creating a share.

#### Scenario: Share with complete profile shows url
- **GIVEN** authenticated user has height and weight
- **WHEN** the user clicks 分享 on `/calendar`
- **THEN** the client opens `/calendar/share` for the current range
- **AND** the secondary page posts a share
- **AND** the returned url is shown on the secondary page

#### Scenario: Share intercepts incomplete profile
- **GIVEN** authenticated user is on `/calendar`
- **AND** profile has no height or no weight
- **WHEN** the user clicks 分享
- **THEN** the client does not create a share
- **AND** the user is guided to `/profile/body`

### Requirement: Calendar lists show recorded time of day
Each visible list row on the calendar (day, month, and custom range) MUST display the record's `recordedAt` local time including hours and minutes, not content alone. Day mode MAY show `HH:mm`; month and range modes MUST show date plus `HH:mm` (Asia/Shanghai). Time and content MUST NOT be squeezed onto one cramped line: they MUST stack (time then content) or sit left/right with clear spacing. Colors remain CONSUME green / INTAKE red. Existing ISO `recordedAt` on the list DTO is sufficient; the client MUST format it.

#### Scenario: Month list shows time with content
- **GIVEN** authenticated user is in month mode
- **AND** a CONSUME record `月跑` has `recordedAt` `2026-08-01T07:30:00+08:00`
- **WHEN** the month list is rendered
- **THEN** the row shows `月跑`
- **AND** the row shows `07:30`

#### Scenario: Day list also shows hours and minutes
- **GIVEN** authenticated user is in day mode on `2026-08-18`
- **AND** a record `跑步` has `recordedAt` `2026-08-18T07:30:00+08:00`
- **WHEN** the day list is rendered
- **THEN** the row shows `跑步`
- **AND** the row shows `07:30`

#### Scenario: Time and content are not cramped
- **GIVEN** authenticated user sees a calendar list row
- **THEN** the time and content are in separate visual lines or have distinct columns with spacing
- **AND** CONSUME remains green and INTAKE remains red
