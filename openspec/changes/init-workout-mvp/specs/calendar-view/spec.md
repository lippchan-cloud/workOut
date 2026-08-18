## ADDED Requirements

### Requirement: Calendar shows a week starting Monday with today selected
The Calendar page SHALL display seven days for the week containing today, with Monday as the first day. Today MUST be selected by default and visually marked.

#### Scenario: Enter calendar on Wednesday
- **GIVEN** today is a Wednesday and user is authenticated
- **WHEN** user opens `/calendar`
- **THEN** the week strip includes Monday through Sunday of the current week
- **AND** today is the selected day
- **AND** today has a distinct “今” or highlight marker

### Requirement: Selecting a day lists that day's records in ascending time
For the selected local calendar date (00:00:00–23:59:59 Asia/Shanghai), the system SHALL list the current user's records ordered by `recordedAt` ascending, then `id` ascending. CONSUME rows MUST render green; INTAKE rows MUST render red. Empty days MUST show 「这一天还没有记录」.

#### Scenario: Ordered colored list
- **GIVEN** authenticated user has on date D: INTAKE at 08:00 and CONSUME at 07:30
- **WHEN** user selects date D
- **THEN** CONSUME appears before INTAKE
- **AND** CONSUME uses green styling
- **AND** INTAKE uses red styling

#### Scenario: Empty day message
- **GIVEN** authenticated user has no records on date D
- **WHEN** user selects date D
- **THEN** the UI shows 「这一天还没有记录」

### Requirement: User can navigate previous and next weeks
The Calendar MUST provide controls to move to the previous week and the next week while preserving selection rules within the newly shown week.

#### Scenario: Go to previous week
- **GIVEN** calendar shows the current week
- **WHEN** user clicks previous week
- **THEN** the week strip shows the seven days of the previous week
