## ADDED Requirements

### Requirement: Create consume or intake record for current user
An authenticated user SHALL create a daily record with `type` `CONSUME` or `INTAKE`, non-empty `content` (1–500 chars after trim), and `recordedAt`. The server MUST bind `userId` from the JWT and MUST ignore any client-supplied user identity.

#### Scenario: Create consume record
- **GIVEN** user A is authenticated
- **WHEN** `POST /api/v1/dailyRecords` with type `CONSUME`, content `跑步 30 分钟`, and a valid `recordedAt`
- **THEN** response `code` is 200
- **AND** returned record has matching type/content
- **AND** the row in database has `user_id` equal to user A

#### Scenario: Empty content rejected
- **GIVEN** user A is authenticated
- **WHEN** posting content that is empty or whitespace-only
- **THEN** response is HTTP 400 with message 「请填写内容」
- **AND** no row is inserted

#### Scenario: Content longer than 500 rejected
- **GIVEN** user A is authenticated
- **WHEN** posting content of 501 characters
- **THEN** response is HTTP 400 with message indicating max 500 characters

### Requirement: Record time defaults to operation time but may be overridden
The client SHALL default datetime controls to local now. The server MUST persist the submitted `recordedAt`. Users MAY record into past or future dates.

#### Scenario: Custom recordedAt persisted
- **GIVEN** user A is authenticated
- **WHEN** posting a record with `recordedAt` set to yesterday 07:30 local time
- **THEN** the stored `recordedAt` matches that instant (within minute precision)

### Requirement: Users cannot see each other's records
Queries and exports MUST only return records belonging to the authenticated user.

#### Scenario: Cross-user isolation
- **GIVEN** user A created a record on date D
- **AND** user B is authenticated with no records on D
- **WHEN** user B queries records for date D
- **THEN** the list is empty
- **AND** user A's record is not included
