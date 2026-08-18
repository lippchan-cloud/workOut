## ADDED Requirements

### Requirement: User can save and load personal profile
Each authenticated user SHALL have at most one profile. Fields: optional `nickname` (0–32), optional `heightCm` (50.0–250.0, one decimal), optional `weightKg` (20.0–300.0, one decimal). `GET /api/v1/profile` and `PUT /api/v1/profile` MUST operate on the current user only. MVP MUST NOT compute or display BMI advice.

#### Scenario: Save and reload profile
- **GIVEN** user A is authenticated
- **WHEN** user saves nickname `小明`, height `175.0`, weight `70.0`
- **THEN** response succeeds
- **AND** a subsequent GET returns the same values

#### Scenario: Height out of range rejected
- **GIVEN** user A is authenticated
- **WHEN** user saves height `300`
- **THEN** response is HTTP 400 with message about height range 50–250

#### Scenario: Profile isolation
- **GIVEN** user A saved a profile
- **WHEN** user B gets profile
- **THEN** user B does not receive user A's nickname/height/weight

### Requirement: User can log out from profile page
The Profile page SHALL provide logout that clears the local JWT and returns the UI to the unauthenticated Record shell.

#### Scenario: Logout clears token
- **GIVEN** user is logged in on `/profile`
- **WHEN** user clicks 退出登录
- **THEN** token is removed from local storage
- **AND** subsequent Tab clicks require login again
