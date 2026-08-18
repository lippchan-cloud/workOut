## ADDED Requirements

### Requirement: User can update own record by id
An authenticated user SHALL update a daily record they own via `PUT /api/v1/dailyRecords/{id}` with `type` `CONSUME` or `INTAKE`, non-empty `content` (1–500 chars after trim), and `recordedAt`. The server MUST bind identity from the JWT and MUST ignore any client-supplied user identity. Empty or whitespace-only content MUST yield HTTP 400 with message 「请填写内容」. Content longer than 500 MUST yield HTTP 400 indicating max 500 characters. A missing or deleted record, or a record owned by another user, MUST yield HTTP 404 (MUST NOT leak existence to other users).

#### Scenario: Owner updates consume record
- **GIVEN** user A created a CONSUME record with content `跑步 30 分钟`
- **WHEN** user A `PUT /api/v1/dailyRecords/{id}` with content `跑步 45 分钟` and a valid `recordedAt`
- **THEN** response `code` is 200
- **AND** subsequent list for that date returns the updated content
- **AND** the row still has `user_id` equal to user A

#### Scenario: Empty content rejected on update
- **GIVEN** user A owns a record
- **WHEN** user A PUTs whitespace-only content
- **THEN** response is HTTP 400 with message 「请填写内容」
- **AND** stored content is unchanged

### Requirement: User can delete own record by id
An authenticated user SHALL delete a daily record they own via `DELETE /api/v1/dailyRecords/{id}`. Identity MUST come from the JWT. Successful delete MUST hide the row from subsequent lists and exports (logical delete is allowed). A missing, already-deleted, or other-user record MUST yield HTTP 404. Queries MUST NOT use per-id loops against the database (no N+1).

#### Scenario: Owner deletes record
- **GIVEN** user A created a record on date D
- **WHEN** user A `DELETE /api/v1/dailyRecords/{id}`
- **THEN** response `code` is 200
- **AND** listing date D no longer includes that id

#### Scenario: Cross-user cannot update or delete
- **GIVEN** user A created a record
- **AND** user B is authenticated
- **WHEN** user B PUTs or DELETEs that id
- **THEN** response is HTTP 404
- **AND** user A's record remains listed for user A

### Requirement: Record form validates immediately and clarifies save destination
The record form SHALL show Chinese validation as the user types or on blur for required content and max 500 characters, without waiting for the server. After a successful create or update, the UI MUST offer clear next actions: 「再记一条」 (stay on form, clear content) and 「回日历」 (navigate to calendar for that record date). Homepage large 「开始记录」 entry MUST remain.

#### Scenario: Live validation on empty content
- **GIVEN** authenticated user is on the consume form
- **WHEN** content is empty and the user attempts to save or the field is blurred empty
- **THEN** the UI shows 「请填写内容」
- **AND** no create API is called

#### Scenario: Save success offers next steps
- **GIVEN** authenticated user saved a record
- **THEN** the UI shows 「再记一条」 and 「回日历」
- **AND** choosing 「再记一条」 keeps the form type and clears content
- **AND** choosing 「回日历」 navigates to `/calendar`

### Requirement: 401 on record APIs returns to login with draft preserved
When a record create/update API returns HTTP 401, the SPA MUST clear the token, navigate to `/login` with a `redirect` back to the form path, and MUST persist the in-progress content, type, and `recordedAt` so they can be restored after login (sessionStorage or equivalent). Query `date` used for backfill MUST be included in the redirect path.

#### Scenario: Expired token keeps draft
- **GIVEN** the consume form has content `跑步` and a datetime filled
- **WHEN** save receives HTTP 401
- **THEN** the user lands on `/login` with redirect to the consume form
- **AND** after a subsequent successful login the form restores `跑步` and the datetime
