# admin-cms

## Purpose

Administrator-only CMS outside the three-tab shell: function nav, account list, user detail, and existing share-report listing.

## Requirements

### Requirement: CMS has a function nav independent of the three-tab shell
The SPA CMS MUST render with a function bar containing at least 概览, 账户列表, 用户详情, and 报告. The current section MUST be visually highlighted. CMS MUST NOT be placed inside the bottom-tab `AppShell`. Unauthenticated visitors of `/cms`, `/cms/accounts`, `/cms/users/:userId`, and `/cms/reports` MUST be sent to `/login?redirect=` plus the attempted pathname. Authenticated `USER` role MUST see a Chinese denial and MUST NOT see account or report tables. Admins MUST see the nav. Deep-linking `GET /cms` and `GET /cms/accounts` MUST fall back to SPA HTML.

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
- **THEN** the page shows nav links 概览, 账户列表, 用户详情, 报告
- **AND** 账户列表 is the current highlighted section

### Requirement: Admin can open user detail from the account list
The system SHALL expose `GET /api/v1/admin/accounts/{userId}` only to an authenticated `ADMIN`. Missing token MUST yield HTTP 401. `USER` JWT MUST yield HTTP 403. Unknown userId MUST yield HTTP 404. Successful `data` MUST include `userId`, `username`, `role`, `createdAt`, `nickname`, `heightCm`, `weightKg` (null when no profile), `recordCount`, `recentRecords` (up to 5 items with id, type, content, recordedAt), and `shares` (existing share tokens for that user: `id`, `from`, `to`, `createdAt`). The JSON MUST NOT include `passwordHash` or `password`. Loading MUST use batch/single queries per dataset, never a per-record repository loop. Identity MUST come from the JWT operator, never from a client-supplied operator id. The SPA `/cms/users/:userId` MUST show these fields; username in the account table MUST link to that path. `/cms/users` without an id MUST prompt the admin to pick a user from 账户列表. Share rows MUST link to the public `/report/{id}` and MUST NOT create a new share.

#### Scenario: Admin JWT loads user detail
- **GIVEN** user `cms_admin` has role `ADMIN` and a valid JWT
- **AND** user `cms_alice` has profile nickname `阿丽` and at least one daily record
- **WHEN** client calls `GET /api/v1/admin/accounts/{aliceUserId}` with the admin Bearer token
- **THEN** response is HTTP 200
- **AND** `data.username` is `cms_alice`
- **AND** `data.nickname` is `阿丽`
- **AND** `data.recordCount` is at least 1
- **AND** `data.recentRecords` is a non-empty array
- **AND** the raw JSON does not contain `passwordHash`

#### Scenario: Regular user cannot load user detail
- **GIVEN** user `cms_user` has role `USER` and a valid JWT
- **WHEN** client calls `GET /api/v1/admin/accounts/{anyUserId}` with that Bearer token
- **THEN** response is HTTP 403

#### Scenario: Anonymous detail request is 401
- **GIVEN** no Authorization header
- **WHEN** client calls `GET /api/v1/admin/accounts/1`
- **THEN** response is HTTP 401

#### Scenario: Account username opens CMS user detail
- **GIVEN** a stored token for an `ADMIN` role
- **AND** the accounts API returns user `cms_alice` with userId `1`
- **WHEN** the admin opens `/cms/accounts` and follows the username link
- **THEN** the location is `/cms/users/1`
- **AND** the page shows `cms_alice` and nickname `阿丽`

### Requirement: Admin can list existing share reports
The system SHALL expose `GET /api/v1/admin/reports` only to an authenticated `ADMIN`. Missing token MUST yield HTTP 401. `USER` JWT MUST yield HTTP 403. Successful `data.list` items MUST include `id` (public token), `userId`, `username`, `from`, `to`, `createdAt`. Usernames MUST be loaded in batch (no per-row user query). The SPA `/cms/reports` MUST render this list and provide a control to open `/report/{id}` (same tab or new window). CMS MUST NOT generate a share on behalf of the listed user.

#### Scenario: Admin JWT lists shares
- **GIVEN** an ADMIN JWT
- **AND** user `cms_alice` has created a share for date `2026-08-18`
- **WHEN** client calls `GET /api/v1/admin/reports` with the admin Bearer token
- **THEN** response is HTTP 200
- **AND** `data.list` contains an item whose `username` is `cms_alice`
- **AND** that item has `id`, `userId`, `from`, `to`, `createdAt`

#### Scenario: Regular user cannot list admin reports
- **GIVEN** a USER JWT
- **WHEN** client calls `GET /api/v1/admin/reports` with that Bearer token
- **THEN** response is HTTP 403

#### Scenario: Anonymous reports list is 401
- **WHEN** client calls `GET /api/v1/admin/reports` without Authorization
- **THEN** response is HTTP 401

#### Scenario: CMS reports page lists shares
- **GIVEN** a stored token for an `ADMIN` role
- **AND** the reports API returns a share whose username is `cms_alice`
- **WHEN** the admin opens `/cms/reports`
- **THEN** the page shows `cms_alice`
- **AND** a link to `/report/{id}` is present
