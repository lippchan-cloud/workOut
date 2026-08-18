## ADDED Requirements

### Requirement: Saving body profile writes change history
Saving nickname, height, or weight on `/profile/body` MUST result in a persisted history snapshot when values change, so later CSV export and trends can resolve body data as of a record time. The profile GET/PUT contract for current values remains; this requirement adds the side effect of history, not new form fields.

#### Scenario: Body save is reflected in trends history
- **GIVEN** authenticated user is on `/profile/body`
- **WHEN** the user saves height `175` and weight `70`
- **THEN** a subsequent `GET /api/v1/profile/trends` includes those values in `bodyHistory`
