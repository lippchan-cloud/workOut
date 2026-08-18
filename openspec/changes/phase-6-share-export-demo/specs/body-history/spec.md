## MODIFIED Requirements

### Requirement: Growth curve axes have units time pan and granularity zoom
The growth curve MUST label Y-axis with a concrete unit (`cm` for height, `kg` for weight) and X-axis with time. When there are many points the chart MUST allow horizontal pan (drag). Zoom in/out MUST change the time-axis precision among hour, day, week, and month — it MUST NOT be implemented as a CSS scale of the SVG. Points MUST be placed by actual `changedAt`, not equally spaced by index. Zoom-out and zoom-in controls MUST be visibly `−` and `+` with accessible names 缩小 and 放大.

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
