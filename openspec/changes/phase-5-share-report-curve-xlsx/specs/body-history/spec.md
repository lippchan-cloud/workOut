## MODIFIED Requirements

### Requirement: Profile changes are stored as history snapshots
When an authenticated user saves body profile fields, the system MUST persist a history row if nickname, height, or weight differs from the latest snapshot (including the first save). Each row MUST store `changedAt` and the post-save snapshot of nickname, heightCm, and weightKg. `changedAt` MUST use the client-supplied `changedAt` from `PUT /api/v1/profile` when present (ISO instant, Asia/Shanghai local datetime from the form); when omitted it MUST default to server now. The current profile `updatedAt` MUST use the same timestamp. Identity MUST come from the JWT. Queries MUST load a user's history in one call (no per-row loops against the database).

#### Scenario: First save creates a snapshot
- **GIVEN** authenticated user has no history
- **WHEN** the user `PUT /api/v1/profile` with heightCm `175` and weightKg `70`
- **THEN** `GET /api/v1/profile/trends` returns one `bodyHistory` item
- **AND** that item has heightCm `175` and weightKg `70`

#### Scenario: Second save with a change appends a new snapshot
- **GIVEN** authenticated user already saved heightCm `175`
- **WHEN** the user `PUT /api/v1/profile` with heightCm `176`
- **THEN** `bodyHistory` contains two items in `changedAt` ascending order
- **AND** the later item has heightCm `176`

#### Scenario: Unchanged save does not append
- **GIVEN** authenticated user already saved heightCm `175` and weightKg `70`
- **WHEN** the user `PUT /api/v1/profile` with the same values
- **THEN** `bodyHistory` size remains 1

#### Scenario: Client supplied changedAt is stored
- **GIVEN** authenticated user has no history
- **WHEN** the user `PUT /api/v1/profile` with heightCm `175`, weightKg `70`, and changedAt `2026-08-01T08:00:00+08:00`
- **THEN** `bodyHistory` has one item whose changedAt is `2026-08-01T08:00:00+08:00` (same instant)

## ADDED Requirements

### Requirement: Growth curve axes have units time pan and granularity zoom
The growth curve MUST label Y-axis with a concrete unit (`cm` for height, `kg` for weight) and X-axis with time. When there are many points the chart MUST allow horizontal pan (drag). Zoom in/out MUST change the time-axis precision among hour, day, week, and month — it MUST NOT be implemented as a CSS scale of the SVG. Points MUST be placed by actual `changedAt`, not equally spaced by index.

#### Scenario: Height series shows cm and a time axis
- **GIVEN** authenticated user has body history with height values
- **WHEN** the user views the growth curve and selects 身高
- **THEN** the chart exposes unit `cm`
- **AND** the chart exposes a time axis

#### Scenario: Zoom changes time precision
- **GIVEN** the growth curve is visible with precision day
- **WHEN** the user clicks 放大
- **THEN** the axis precision becomes finer than day (hour)
- **AND** the SVG is not merely CSS-scaled
