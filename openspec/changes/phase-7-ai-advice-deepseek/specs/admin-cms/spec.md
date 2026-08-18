## ADDED Requirements

### Requirement: Admin can assign API keys to users singly or in batch
The system SHALL expose admin endpoints to set a user's DeepSeek API key: update one user by `userId`, and batch-update a list of `userIds` with the same key. Only authenticated `ADMIN` MAY call these endpoints. Missing token MUST yield HTTP 401. `USER` JWT MUST yield HTTP 403. Responses and lists MUST return `keyMask` only, never the full key. SPA routes `/cms/api-keys` MUST provide UI for single-user update and batch update.

#### Scenario: Admin sets one user key
- **GIVEN** an ADMIN JWT
- **WHEN** client `PUT /api/v1/admin/apiKeys/{userId}` with `request.apiKey` a non-empty test key
- **THEN** response is HTTP 200
- **AND** `data.keyMask` is present and is not equal to the full key
- **AND** subsequent reads show the user as having a key

#### Scenario: Admin batch sets keys
- **GIVEN** an ADMIN JWT
- **AND** users `u1` and `u2` exist
- **WHEN** client `PUT /api/v1/admin/apiKeys/batch` with `request.userIds` containing both and `request.apiKey`
- **THEN** response is HTTP 200
- **AND** both users show a masked key in the CMS list

#### Scenario: USER cannot manage API keys
- **GIVEN** a USER JWT
- **WHEN** client calls `PUT /api/v1/admin/apiKeys/1` or `GET /api/v1/admin/apiKeys`
- **THEN** response is HTTP 403

### Requirement: Admin can view AI call usage in CMS
CMS function nav MUST include **API Key** and **AI 调用**. `/cms/ai-calls` MUST list call logs from `GET /api/v1/admin/aiCalls` with optional filters `userId` and `apiKeyId`. USER visitors MUST NOT see the tables (same denial as other CMS bars). Deep links MUST fall back to SPA HTML.

#### Scenario: Admin opens AI calls nav
- **GIVEN** a stored token for an `ADMIN` role
- **WHEN** the admin opens `/cms/ai-calls`
- **THEN** the nav shows API Key and AI 调用
- **AND** the page can display call rows or an empty state

#### Scenario: Anonymous ai-calls redirects to login
- **GIVEN** no token in local storage
- **WHEN** the user opens `/cms/ai-calls`
- **THEN** the UI navigates to `/login?redirect=/cms/ai-calls`

## MODIFIED Requirements

### Requirement: CMS has a function nav independent of the three-tab shell
The SPA CMS MUST render with a function bar containing at least 概览, 账户列表, 用户详情, 报告, **API Key**, and **AI 调用**. The current section MUST be visually highlighted. CMS MUST NOT be placed inside the bottom-tab `AppShell`. Unauthenticated visitors of `/cms`, `/cms/accounts`, `/cms/users/:userId`, `/cms/reports`, `/cms/api-keys`, and `/cms/ai-calls` MUST be sent to `/login?redirect=` plus the attempted pathname. Authenticated `USER` role MUST see a Chinese denial and MUST NOT see account, report, API key, or AI call tables. Admins MUST see the nav. Deep-linking `GET /cms` and `GET /cms/accounts` MUST fall back to SPA HTML.

#### Scenario: Anonymous CMS still redirects to login
- **GIVEN** no token in local storage
- **WHEN** the user opens `/cms`
- **THEN** the UI navigates to `/login?redirect=/cms`

#### Scenario: Anonymous CMS child route redirects to login
- **GIVEN** no token in local storage
- **WHEN** the user opens `/cms/reports`
- **THEN** the UI navigates to `/login?redirect=/cms/reports`

#### Scenario: Admin sees highlighted function nav
- **GIVEN** a stored token for an `ADMIN` role
- **WHEN** the user opens `/cms/accounts`
- **THEN** the page shows nav links 概览, 账户列表, 用户详情, 报告, API Key, AI 调用
- **AND** 账户列表 is the current highlighted section
