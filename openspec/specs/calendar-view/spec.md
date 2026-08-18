# calendar-view

## Purpose

Week calendar for reviewing personal consume/intake records, with small week navigation, day-count badges, list times of day, an addressable record detail page, export and share, and growth curves hosted under 我的.

## Requirements

### Requirement: Calendar list items can be edited and deleted
Each listed record MUST be openable as a detail page. The day list MUST NOT expose 编辑 / 删除 as the only path; clicking a row MUST navigate to `/calendar/records/{id}`. The detail page MUST show type, content, and recorded time in a read-only layout, then offer 编辑 and 删除. Edit MUST open the record form populated with that item. Delete MUST ask for confirmation in Chinese before calling `DELETE`. After successful delete the user MUST return to the calendar for that record's date. Colors remain CONSUME green / INTAKE red.

#### Scenario: Open detail from calendar list
- **GIVEN** authenticated user sees a CONSUME row `跑步` on the calendar
- **WHEN** user clicks that row
- **THEN** the URL is `/calendar/records/{id}`
- **AND** the detail page shows `跑步`
- **AND** 编辑 and 删除 are available on the detail page

#### Scenario: Edit from detail
- **GIVEN** authenticated user is on the detail page for a CONSUME row `跑步`
- **WHEN** user clicks 编辑
- **THEN** the consume form is shown with content `跑步`

#### Scenario: Delete requires confirmation on detail
- **GIVEN** authenticated user is on the detail page
- **WHEN** user clicks 删除
- **THEN** the UI asks for confirmation
- **AND** the delete API is not called until the user confirms
- **WHEN** the user confirms
- **THEN** the delete API is called
- **AND** the user returns to the calendar

### Requirement: User can backfill a selected day
In day mode, the selected day MUST offer a 「补记」 action that opens the record type picker with that date applied to `recordedAt`. 「补记」 MUST be a secondary or text-sized control and MUST NOT use the same size as the week-strip day cells' primary visual weight or the homepage hero. Query parameter `date=YYYY-MM-DD` MUST be honored.

#### Scenario: Backfill from selected day
- **GIVEN** authenticated user selected date `2026-08-10` on the calendar
- **WHEN** user clicks 补记 then 消耗
- **THEN** the consume form is shown
- **AND** the datetime control uses date `2026-08-10`

### Requirement: Week navigation is a small control and day cells are the visual subject
Day-mode calendar MUST show 上一周 / 下一周 as small week-nav controls beside or above the week strip. Day cells MUST be the visual subject (larger hit area than week-nav). Each day cell MUST support pointer hover and keyboard `:focus-visible` styling.

#### Scenario: Prev and next week are small
- **GIVEN** authenticated user is on `/calendar` day mode
- **THEN** 上一周 and 下一周 have class `week-nav-btn`
- **AND** clicking 上一周 changes the visible week strip

#### Scenario: Day cell hover and focus
- **GIVEN** authenticated user is on `/calendar` day mode
- **THEN** each day cell is a focusable button
- **AND** day cells include a hover/focus style class `week-day`

### Requirement: Days with records show a count badge
For the visible week, the UI MUST display a count badge on the top-right of each day cell whose record count is greater than 0. Counts MUST be aggregated from a single `GET /api/v1/dailyRecords?from=<weekStart>&to=<weekEnd>` response (Monday–Sunday, Asia/Shanghai dates). The UI MUST NOT issue one list request per day of the week.

#### Scenario: Badge shows the day's count
- **GIVEN** authenticated user has 2 records on a day in the visible week and 0 on the other days
- **WHEN** the week records response is rendered
- **THEN** that day cell shows a badge with `2`
- **AND** days with zero records do not show a numeric badge
- **AND** the week fetch URL includes `from=` and `to=` rather than seven `date=` calls

### Requirement: Record detail is addressable and returns to the selected day
`/calendar/records/:id` MUST load the record with `GET /api/v1/dailyRecords/{id}` so a refresh of the detail URL still opens the page. Missing or unauthorized records MUST show a Chinese not-found message and a way back to the calendar. Returning from detail MUST restore the calendar on that record's local date (`/calendar?date=YYYY-MM-DD` or equivalent).

#### Scenario: Direct detail URL loads via GET by id
- **GIVEN** authenticated user opens `/calendar/records/9`
- **THEN** the client requests `GET /api/v1/dailyRecords/9`
- **AND** the detail content is shown from that response

#### Scenario: Browser-style back restores calendar date
- **GIVEN** authenticated user opened a record whose local date is `2026-08-10`
- **WHEN** the user clicks 返回 on the detail page
- **THEN** the calendar is shown with date `2026-08-10` selected

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
