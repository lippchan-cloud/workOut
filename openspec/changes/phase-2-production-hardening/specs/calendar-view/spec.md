## MODIFIED Requirements

### Requirement: Selecting a day lists that day's records in ascending time
For the selected local calendar date (00:00:00–23:59:59 Asia/Shanghai), the system SHALL list the current user's records ordered by `recordedAt` ascending, then `id` ascending. CONSUME rows MUST render green; INTAKE rows MUST render red. The UI MUST distinguish three states and MUST NOT treat a request failure as an empty list: loading MUST show a loading message; failure MUST show an error with a retry action; a successful empty list MUST show 「这一天还没有记录」 (month: 「这个月还没有记录」; range: 「这段时间还没有记录」).

#### Scenario: Ordered colored list
- **GIVEN** authenticated user has on date D: INTAKE at 08:00 and CONSUME at 07:30
- **WHEN** user selects date D
- **THEN** CONSUME appears before INTAKE
- **AND** CONSUME uses green styling
- **AND** INTAKE uses red styling

#### Scenario: Empty day message
- **GIVEN** authenticated user has no records on date D
- **AND** the list request succeeded
- **WHEN** user selects date D
- **THEN** the UI shows 「这一天还没有记录」
- **AND** the UI does not show the load-failure message

#### Scenario: Loading is not empty
- **GIVEN** authenticated user opens calendar while the list request is in flight
- **THEN** the UI shows a loading message
- **AND** the UI does not show 「这一天还没有记录」

#### Scenario: Failure is not empty and can retry
- **GIVEN** authenticated user opens calendar
- **WHEN** the list request fails (network or non-401 HTTP error)
- **THEN** the UI shows a failure message with a retry control
- **AND** the UI does not show 「这一天还没有记录」
- **AND** clicking retry issues the list request again

## ADDED Requirements

### Requirement: Calendar list items can be edited and deleted
Each listed record MUST provide edit and delete actions. Edit MUST open the record form populated with that item (type, content, recordedAt). Delete MUST ask for confirmation in Chinese before calling `DELETE`. After successful delete the list MUST refresh. Colors remain CONSUME green / INTAKE red.

#### Scenario: Edit from calendar
- **GIVEN** authenticated user sees a CONSUME row `跑步` on the calendar
- **WHEN** user clicks 编辑
- **THEN** the consume form is shown with content `跑步`

#### Scenario: Delete requires confirmation
- **GIVEN** authenticated user sees a row on the calendar
- **WHEN** user clicks 删除
- **THEN** the UI asks for confirmation
- **AND** the delete API is not called until the user confirms
- **WHEN** the user confirms
- **THEN** the delete API is called
- **AND** the row disappears after success

### Requirement: User can backfill a selected day
In day mode, the selected day MUST offer a 「补记」 action that opens the record type picker (or form) with that date applied to `recordedAt`, without requiring the homepage 「开始记录」 three-step path. Query parameter `date=YYYY-MM-DD` MUST be honored.

#### Scenario: Backfill from selected day
- **GIVEN** authenticated user selected date `2026-08-10` on the calendar
- **WHEN** user clicks 补记 then 消耗
- **THEN** the consume form is shown
- **AND** the datetime control uses date `2026-08-10`
