## ADDED Requirements

### Requirement: Unauthenticated admin can list all accounts
The system SHALL expose `GET /api/v1/admin/accounts` without requiring a Bearer JWT. The response envelope MUST be `{ code, msg, data }` with `code` 200. `data.list` MUST contain every registered user with `userId`, `username`, `createdAt`, and profile-visible fields `nickname`, `heightCm`, `weightKg` (null when the user has no profile). The JSON MUST NOT include `passwordHash`, `password`, or any password digest. Listing MUST load users and profiles in batch queries (no per-user repository loop).

#### Scenario: List accounts without token
- **GIVEN** user `cms_alice` is registered and has profile nickname `阿丽`
- **WHEN** client calls `GET /api/v1/admin/accounts` with no Authorization header
- **THEN** response is HTTP 200 and `code` is 200
- **AND** `data.list` contains an item whose `username` is `cms_alice` and `nickname` is `阿丽`
- **AND** that item has `userId` and `createdAt`
- **AND** the raw JSON does not contain `passwordHash` or `password`

#### Scenario: Account without profile still listed
- **GIVEN** user `cms_bob` is registered with no profile row
- **WHEN** client calls `GET /api/v1/admin/accounts` without Authorization
- **THEN** `data.list` contains `cms_bob` with `nickname`, `heightCm`, and `weightKg` equal to null

### Requirement: Temporary CMS page is reachable without login
The SPA MUST provide an independent route `/cms` (not one of the three user Tabs). Unauthenticated users MUST be able to open `/cms` and see a Chinese warning that this access is temporary and will require authentication later. The page MUST render the account list fields (user id, username, created time, nickname, height, weight), MUST show loading / empty / error states, and MUST NOT display a password hash. The login page MUST expose a visible link to `/cms`. Deep-linking `GET /cms` MUST fall back to SPA HTML.

#### Scenario: Open CMS without token
- **GIVEN** no token in local storage
- **WHEN** the user opens `/cms`
- **THEN** the page shows warning text containing 「临时」and 「鉴权」
- **AND** the page shows column labels for 用户ID、用户名、创建时间、昵称、身高、体重
- **AND** the page does not contain the text `passwordHash`

#### Scenario: Login page has CMS entry
- **GIVEN** the user is on `/login`
- **WHEN** the page is rendered
- **THEN** a link named 「后台管理」 navigates to `/cms`

#### Scenario: CMS deep link serves SPA
- **GIVEN** the application is running
- **WHEN** browser requests `GET /cms`
- **THEN** the SPA HTML is returned (forward to index.html)
- **AND** `/api/**` paths are not shadowed

### Requirement: Unauthenticated CMS must not weaken user APIs
Security MUST permit anonymous access only for `GET /api/v1/admin/accounts` among business JSON APIs. `GET /api/v1/profile` and `GET /api/v1/dailyRecords` MUST still return HTTP 401 when Authorization is missing.

#### Scenario: Profile still requires JWT
- **WHEN** client calls `GET /api/v1/profile` without Authorization
- **THEN** response is HTTP 401

#### Scenario: Daily records still require JWT
- **WHEN** client calls `GET /api/v1/dailyRecords?date=2026-08-18` without Authorization
- **THEN** response is HTTP 401
