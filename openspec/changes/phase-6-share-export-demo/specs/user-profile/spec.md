## ADDED Requirements

### Requirement: Growth curve zoom controls show minus and plus
The growth-curve zoom controls MUST display visible `−` (zoom out) and `+` (zoom in). Accessible names MUST remain 缩小 and 放大 so `getByRole` with those names still succeeds. Zoom behavior (time precision, not CSS scale) is unchanged.

#### Scenario: Zoom buttons are minus and plus
- **GIVEN** the growth curve is visible with data
- **THEN** the zoom-out control has accessible name 缩小 and visible text `−`
- **AND** the zoom-in control has accessible name 放大 and visible text `+`
