## ADDED Requirements

### Requirement: User can get own record by id
An authenticated user SHALL retrieve a daily record they own via `GET /api/v1/dailyRecords/{id}`. Identity MUST come from the JWT. The response `data` MUST include `id`, `type`, `content`, and `recordedAt`. A missing, logically deleted, or other-user record MUST yield HTTP 404 with message 「记录不存在」 (MUST NOT leak existence). The lookup MUST be a single query by id and userId (no extra list scan).

#### Scenario: Owner gets consume record
- **GIVEN** user A created a CONSUME record with content `跑步`
- **WHEN** user A `GET /api/v1/dailyRecords/{id}`
- **THEN** response `code` is 200
- **AND** `data.content` is `跑步`
- **AND** `data.type` is `CONSUME`

#### Scenario: Cross-user get is 404
- **GIVEN** user A created a record
- **AND** user B is authenticated
- **WHEN** user B `GET /api/v1/dailyRecords/{id}`
- **THEN** response is HTTP 404
- **AND** message is 「记录不存在」

#### Scenario: Missing id is 404
- **GIVEN** user A is authenticated
- **WHEN** user A `GET /api/v1/dailyRecords/999999999`
- **THEN** response is HTTP 404
- **AND** message is 「记录不存在」
