## ADDED Requirements

### Requirement: Calendar lists show recorded time of day
Each visible list row on the calendar (day, month, and custom range) MUST display the record's `recordedAt` local time including hours and minutes, not content alone. Day mode MAY show `HH:mm`; month and range modes MUST show date plus `HH:mm` (Asia/Shanghai). Existing ISO `recordedAt` on the list DTO is sufficient; the client MUST format it.

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

### Requirement: Calendar offers a trends secondary page
The calendar Tab MUST provide a level-2 entry 「变化曲线」 that navigates to `/calendar/trends`. The trends page MUST NOT be stacked inside the month grid on the same screen. Returning from trends MUST restore `/calendar`. The bottom Tab 「日历」 MUST remain the only calendar-level primary navigation.

#### Scenario: Open trends from calendar
- **GIVEN** authenticated user is on `/calendar`
- **WHEN** the user clicks 变化曲线
- **THEN** the URL is `/calendar/trends`

#### Scenario: Back from trends returns to calendar
- **GIVEN** authenticated user is on `/calendar/trends`
- **WHEN** the user clicks 返回
- **THEN** the URL is `/calendar`
