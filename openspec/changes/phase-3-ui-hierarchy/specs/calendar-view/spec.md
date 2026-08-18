## MODIFIED Requirements

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

## ADDED Requirements

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
