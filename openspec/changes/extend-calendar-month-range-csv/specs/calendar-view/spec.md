## ADDED Requirements

### Requirement: Calendar can show the selected month's records
The Calendar page SHALL provide a visible control to switch to month mode. In month mode the page MUST show a month picker defaulting to the current Asia/Shanghai month, MUST load `GET /api/v1/dailyRecords?yearMonth=YYYY-MM` for the authenticated user, and MUST list returned rows in API order. CONSUME rows MUST render green `#16A34A`; INTAKE rows MUST render red `#DC2626`. Empty months MUST show 「这个月还没有记录」. Week-strip day mode MUST remain available as the default.

#### Scenario: Switch to current month and see rows
- **GIVEN** the user is authenticated on `/calendar`
- **AND** the API will return one CONSUME and one INTAKE for `yearMonth` of the current month
- **WHEN** the user chooses 按月
- **THEN** a month picker is shown
- **AND** both records appear
- **AND** CONSUME uses green styling
- **AND** INTAKE uses red styling

#### Scenario: Empty month message
- **GIVEN** the user is authenticated on `/calendar` in month mode
- **AND** the API returns an empty list for the selected month
- **THEN** the UI shows 「这个月还没有记录」

### Requirement: User can jump to an arbitrary calendar date
In day mode the Calendar MUST provide a date picker labeled so the user can jump to any local date without paging weeks. Choosing a date MUST select that day, MUST show the week strip containing that day (Monday-start), and MUST load that day's list via `date=YYYY-MM-DD`.

#### Scenario: Jump to a date outside the visible week
- **GIVEN** the user is authenticated on `/calendar` in day mode
- **AND** the visible week is the week containing today
- **WHEN** the user picks a date 21 days earlier in the date picker
- **THEN** that date is the selected day
- **AND** the week strip includes that date
- **AND** the client requests records with `date` equal to that YYYY-MM-DD

### Requirement: User can filter records by a custom inclusive date range
The Calendar page SHALL provide a 自定义 mode with two date inputs (start and end), both defaulting to today. The client MUST call `GET /api/v1/dailyRecords?from=&to=` with the chosen inclusive dates. Empty ranges MUST show 「这段时间还没有记录」. If the start is after the end, the UI MUST NOT call the API successfully as a 200 list; it MUST show the server or client validation message.

#### Scenario: Custom range lists matching days
- **GIVEN** the user is authenticated on `/calendar`
- **WHEN** the user chooses 自定义 and sets from `2026-08-01` and to `2026-08-18`
- **THEN** the client requests `from=2026-08-01` and `to=2026-08-18`
- **AND** returned rows are shown in API order

#### Scenario: Empty custom range message
- **GIVEN** the user is in 自定义 mode
- **AND** the API returns an empty list for the selected range
- **THEN** the UI shows 「这段时间还没有记录」
